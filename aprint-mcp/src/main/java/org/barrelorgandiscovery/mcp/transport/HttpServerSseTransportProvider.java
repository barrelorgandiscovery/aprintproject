package org.barrelorgandiscovery.mcp.transport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.mcp.McpJsonMapperProvider;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import io.modelcontextprotocol.common.McpTransportContext;
import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerSession;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import reactor.core.publisher.Mono;

/**
 * HTTP Server-Sent Events (SSE) transport provider for MCP using Java's built-in HttpServer.
 * This implementation provides SSE-based bidirectional communication for the Model Context Protocol.
 * 
 * <p>
 * The transport handles two types of endpoints:
 * <ul>
 * <li>SSE endpoint (/mcp/sse) - Establishes a long-lived connection for server-to-client events</li>
 * <li>Message endpoint (/mcp/message) - Handles client-to-server message requests via POST</li>
 * </ul>
 * 
 * <p>
 * Features:
 * <ul>
 * <li>Session management for multiple client connections</li>
 * <li>Graceful shutdown support</li>
 * <li>Error handling and response formatting</li>
 * <li>CORS support</li>
 * </ul>
 * 
 * @author APrint Development Team
 */
public class HttpServerSseTransportProvider implements McpServerTransportProvider {
	
	private static final Logger logger = Logger.getLogger(HttpServerSseTransportProvider.class);
	
	public static final String MESSAGE_EVENT_TYPE = "message";
	public static final String ENDPOINT_EVENT_TYPE = "endpoint";
	public static final String SESSION_ID_PARAM = "sessionId";

	/**
	 * Binds the MCP HTTP server. Default {@code 127.0.0.1} (loopback only). Set system property
	 * {@code aprint.mcp.bindAddress} to {@code 0.0.0.0} (or {@code *}) to listen on all interfaces
	 * (use only behind a firewall / trusted network).
	 */
	static InetSocketAddress createBindAddress(int port) throws java.net.UnknownHostException {
		String raw = System.getProperty("aprint.mcp.bindAddress", "127.0.0.1");
		if (raw == null || raw.isBlank()) {
			raw = "127.0.0.1";
		}
		String host = raw.trim();
		if ("0.0.0.0".equals(host) || "*".equals(host) || "all".equalsIgnoreCase(host)) {
			logger.warn("MCP HTTP binding to all interfaces (aprint.mcp.bindAddress=" + host
				+ ") — ensure this port is not reachable from untrusted networks (no auth).");
			return new InetSocketAddress(port);
		}
		InetAddress addr = InetAddress.getByName(host);
		logger.info("MCP HTTP binding to " + addr.getHostAddress() + " (aprint.mcp.bindAddress)");
		return new InetSocketAddress(addr, port);
	}
	
	private final McpJsonMapper jsonMapper;
	private final int port;
	private final String sseEndpoint;
	private final String messageEndpoint;
	
	private HttpServer httpServer;
	private ExecutorService executorService;
	private final AtomicBoolean isClosing = new AtomicBoolean(false);
	private final Map<String, McpServerSession> sessions = new ConcurrentHashMap<>();
	private McpServerSession.Factory sessionFactory;
	
	/**
	 * Creates a new HttpServerSseTransportProvider.
	 * 
	 * @param port The port to listen on
	 * @param sseEndpoint The SSE endpoint path (default: "/mcp/sse")
	 * @param messageEndpoint The message endpoint path (default: "/mcp/message")
	 */
	public HttpServerSseTransportProvider(int port, String sseEndpoint, String messageEndpoint) {
		this.port = port;
		this.sseEndpoint = sseEndpoint != null ? sseEndpoint : "/mcp/sse";
		this.messageEndpoint = messageEndpoint != null ? messageEndpoint : "/mcp/message";
		this.jsonMapper = McpJsonMapperProvider.get();
		logger.info("HttpServerSseTransportProvider created: port=" + port + 
			", sseEndpoint=" + this.sseEndpoint + ", messageEndpoint=" + this.messageEndpoint);
	}
	
	/**
	 * Creates a new HttpServerSseTransportProvider with default endpoints.
	 * 
	 * @param port The port to listen on
	 */
	public HttpServerSseTransportProvider(int port) {
		this(port, "/mcp/sse", "/mcp/message");
	}
	
	/**
	 * Starts the HTTP server and begins accepting connections.
	 * 
	 * @throws IOException if the server cannot be started
	 */
	public void start() throws IOException {
		if (httpServer != null) {
			logger.warn("HTTP server is already started");
			return;
		}
		
		logger.info("Starting MCP HTTP Server on port " + port + "...");
		
		InetSocketAddress address = createBindAddress(port);
		httpServer = HttpServer.create(address, 0);
		executorService = Executors.newCachedThreadPool();
		httpServer.setExecutor(executorService);
		
		// SSE endpoint for establishing connections
		httpServer.createContext(sseEndpoint, new SSEHandler());
		logger.info("Created SSE endpoint: " + sseEndpoint);
		
		// Message endpoint for POST requests
		httpServer.createContext(messageEndpoint, new MessageHandler());
		logger.info("Created message endpoint: " + messageEndpoint);
		
		// Health check endpoint
		httpServer.createContext("/mcp/health", new HealthHandler());
		logger.info("Created health endpoint: /mcp/health");
		
		httpServer.start();
		
		logger.info("MCP HTTP Server started successfully on " + address.getAddress().getHostAddress() + ":" + port);
		logger.info("SSE endpoint: http://" + address.getAddress().getHostAddress() + ":" + port + sseEndpoint);
		logger.info("Message endpoint: http://" + address.getAddress().getHostAddress() + ":" + port + messageEndpoint);
	}
	
	/**
	 * Stops the HTTP server and closes all sessions.
	 */
	public void stop() {
		if (httpServer == null) {
			return;
		}
		
		logger.info("Stopping MCP HTTP Server...");
		isClosing.set(true);
		
		// Close all sessions
		for (McpServerSession session : sessions.values()) {
			try {
				session.closeGracefully().block();
			} catch (Exception e) {
				logger.warn("Error closing session: " + e.getMessage());
			}
		}
		sessions.clear();
		
		// Stop the server
		httpServer.stop(0);
		httpServer = null;
		
		if (executorService != null) {
			executorService.shutdown();
			executorService = null;
		}
		
		logger.info("MCP HTTP Server stopped");
	}
	
	@Override
	public void setSessionFactory(McpServerSession.Factory sessionFactory) {
		this.sessionFactory = sessionFactory;
		logger.info("Session factory set");
	}
	
	@Override
	public Mono<Void> notifyClients(String method, Object params) {
		// Notify all active sessions
		return Mono.fromRunnable(() -> {
			for (McpServerSession session : sessions.values()) {
				session.sendNotification(method, params).subscribe(
					null,
					error -> logger.warn("Failed to send notification: " + error.getMessage())
				);
			}
		});
	}
	
	@Override
	public Mono<Void> closeGracefully() {
		return Mono.fromRunnable(() -> stop());
	}
	
	/**
	 * Handler for SSE connections (GET requests).
	 */
	private class SSEHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			// Handle OPTIONS for CORS preflight
			if ("OPTIONS".equals(exchange.getRequestMethod())) {
				exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
				exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
				exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
				exchange.sendResponseHeaders(200, -1);
				return;
			}
			
			if (!"GET".equals(exchange.getRequestMethod())) {
				logger.warn("SSE endpoint received non-GET request: " + exchange.getRequestMethod());
				sendResponse(exchange, 405, "Method Not Allowed: SSE endpoint only accepts GET", "text/plain");
				return;
			}
			
			if (isClosing.get()) {
				sendResponse(exchange, 503, "Service Unavailable", "text/plain");
				return;
			}
			
			if (sessionFactory == null) {
				logger.error("Session factory not set!");
				sendResponse(exchange, 500, "Internal Server Error", "text/plain");
				return;
			}
			
			// Set SSE headers
			exchange.getResponseHeaders().set("Content-Type", "text/event-stream");
			exchange.getResponseHeaders().set("Cache-Control", "no-cache");
			exchange.getResponseHeaders().set("Connection", "keep-alive");
			exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
			exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
			exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
			
			exchange.sendResponseHeaders(200, 0);
			
			OutputStream os = exchange.getResponseBody();
			PrintWriter writer = new PrintWriter(
				new OutputStreamWriter(os, StandardCharsets.UTF_8), true);
			
			String sessionId = UUID.randomUUID().toString();
			
			// Create session transport
			HttpServerMcpSessionTransport sessionTransport = new HttpServerMcpSessionTransport(
				sessionId, writer, jsonMapper, () -> sessions.remove(sessionId));
			
			// Create session using factory
			McpServerSession session = sessionFactory.create(sessionTransport);
			sessions.put(sessionId, session);
			
			logger.info("New SSE connection established: " + sessionId);
			
			// Send initial endpoint event
			String endpointUrl = buildEndpointUrl(sessionId);
			sendSSEEvent(writer, ENDPOINT_EVENT_TYPE, endpointUrl);
			
			// Keep connection alive with periodic heartbeats
			// The session transport will handle message sending
			try {
				int heartbeatCount = 0;
				while (!sessionTransport.isClosed() && !isClosing.get()) {
					Thread.sleep(30000); // 30 seconds
					if (!sessionTransport.isClosed()) {
						// Send heartbeat
						writer.print(": heartbeat\n\n");
						writer.flush();
						heartbeatCount++;
						if (heartbeatCount % 10 == 0) {
							logger.debug("SSE connection " + sessionId + " still alive (heartbeat " + heartbeatCount + ")");
						}
					}
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				logger.info("SSE connection interrupted: " + sessionId);
			} catch (Exception e) {
				logger.warn("Error in SSE connection loop: " + e.getMessage());
			} finally {
				sessions.remove(sessionId);
				try {
					session.close();
				} catch (Exception e) {
					logger.warn("Error closing session: " + e.getMessage());
				}
				try {
					os.close();
				} catch (IOException e) {
					logger.warn("Error closing output stream: " + e.getMessage());
				}
				logger.info("SSE connection closed: " + sessionId);
			}
		}
	}
	
	/**
	 * Handler for message requests (POST requests).
	 */
	private class MessageHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if ("OPTIONS".equals(exchange.getRequestMethod())) {
				// Handle CORS preflight
				exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
				exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "POST, OPTIONS");
				exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");
				exchange.sendResponseHeaders(200, -1);
				return;
			}
			
			if (!"POST".equals(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
				return;
			}
			
			if (isClosing.get()) {
				sendResponse(exchange, 503, "Service Unavailable", "text/plain");
				return;
			}
			
			// Get session ID from query parameter
			String query = exchange.getRequestURI().getQuery();
			String sessionId = extractSessionId(query);
			
			// Check if this is an initialization request (might not have sessionId yet)
			// For SSE transport, we need to establish the SSE connection first via GET /mcp/sse
			// But some clients might try to POST initialization messages
			if (sessionId == null) {
				logger.warn("Message request without session ID - this might be an initialization attempt");
				logger.warn("For SSE transport, clients should: 1) GET /mcp/sse to establish connection, 2) POST to /mcp/message?sessionId=...");
				
				// Try to read the message to see if it's an initialize request
				try {
					InputStream requestBody = exchange.getRequestBody();
					BufferedReader reader = new BufferedReader(
						new InputStreamReader(requestBody, StandardCharsets.UTF_8));
					
					StringBuilder body = new StringBuilder();
					String line;
					while ((line = reader.readLine()) != null) {
						body.append(line);
					}
					
					String bodyStr = body.toString().trim();
					if (!bodyStr.isEmpty()) {
						// Just log that we received a message without sessionId
						logger.info("Received message body without sessionId (length: " + bodyStr.length() + ")");
						if (bodyStr.contains("\"method\"") && bodyStr.contains("initialize")) {
							logger.info("This appears to be an initialize request - client should first GET /mcp/sse");
						}
					}
				} catch (Exception e) {
					logger.debug("Could not parse message body: " + e.getMessage());
				}
				
				sendResponse(exchange, 400, "Bad Request: missing sessionId. Please establish SSE connection first via GET /mcp/sse", "text/plain");
				return;
			}
			
			McpServerSession session = sessions.get(sessionId);
			if (session == null) {
				logger.warn("Message request for unknown session: " + sessionId);
				logger.warn("Active sessions: " + sessions.keySet());
				sendResponse(exchange, 404, "Session Not Found: " + sessionId, "text/plain");
				return;
			}
			
			try {
				// Read request body
				InputStream requestBody = exchange.getRequestBody();
				BufferedReader reader = new BufferedReader(
					new InputStreamReader(requestBody, StandardCharsets.UTF_8));
				
				StringBuilder body = new StringBuilder();
				String line;
				while ((line = reader.readLine()) != null) {
					body.append(line);
				}
				
				String bodyStr = body.toString().trim();
				logger.info("Received message for session " + sessionId + " (length: " + bodyStr.length() + "): " + 
					(bodyStr.length() > 200 ? bodyStr.substring(0, 200) + "..." : bodyStr));
				
				if (bodyStr.isEmpty()) {
					logger.warn("Empty message body for session " + sessionId);
					sendResponse(exchange, 400, "Bad Request: empty body", "text/plain");
					return;
				}
				
				// Parse JSON-RPC message using SDK's deserialization method
				// This is required because JSONRPCMessage is a sealed interface and cannot be deserialized directly
				McpSchema.JSONRPCMessage message;
				try {
					// Use the SDK's static method to properly deserialize JSON-RPC messages
					message = McpSchema.deserializeJsonRpcMessage(jsonMapper, bodyStr);
					logger.debug("Successfully parsed JSON-RPC message for session " + sessionId);
				} catch (Exception e) {
					logger.error("Failed to parse JSON-RPC message for session " + sessionId + ": " + e.getMessage());
					logger.error("Message body was: " + bodyStr);
					logger.error("Exception type: " + e.getClass().getName());
					if (e.getCause() != null) {
						logger.error("Caused by: " + e.getCause().getClass().getName() + " - " + e.getCause().getMessage());
					}
					sendResponse(exchange, 400, "Bad Request: invalid JSON-RPC - " + e.getMessage(), "text/plain");
					return;
				}
				
				// Handle message through session
				McpTransportContext context = McpTransportContext.EMPTY;
				try {
					session.handle(message).contextWrite(reactor.util.context.Context.of(
						McpTransportContext.KEY, context)).block();
					logger.debug("Message handled successfully for session " + sessionId);
				} catch (Exception e) {
					logger.error("Error handling message in session: " + e.getMessage(), e);
					// Still send 200 OK - errors are sent via SSE
					// The session transport will send error responses over SSE
				}
				
				// Send success response (actual response is sent via SSE)
				exchange.getResponseHeaders().set("Content-Type", "application/json");
				exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
				exchange.sendResponseHeaders(200, -1);
				exchange.close();
				
			} catch (Exception e) {
				logger.error("Error handling message: " + e.getMessage(), e);
				logger.error("Stack trace: ", e);
				try {
					sendResponse(exchange, 500, "Internal Server Error: " + e.getMessage(), "text/plain");
				} catch (IOException ioException) {
					logger.error("Failed to send error response", ioException);
				}
			}
		}
	}
	
	/**
	 * Handler for health check endpoint.
	 */
	private class HealthHandler implements HttpHandler {
		@Override
		public void handle(HttpExchange exchange) throws IOException {
			if (!"GET".equals(exchange.getRequestMethod())) {
				sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
				return;
			}
			
			exchange.getResponseHeaders().set("Content-Type", "application/json");
			exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
			String response = "{\"status\":\"ok\",\"sessions\":" + sessions.size() + "}";
			byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, responseBytes.length);
			exchange.getResponseBody().write(responseBytes);
			exchange.close();
		}
	}
	
	/**
	 * Sends an HTTP response.
	 */
	private void sendResponse(HttpExchange exchange, int statusCode, String message, String contentType) 
			throws IOException {
		exchange.getResponseHeaders().set("Content-Type", contentType);
		byte[] responseBytes = message.getBytes(StandardCharsets.UTF_8);
		exchange.sendResponseHeaders(statusCode, responseBytes.length);
		exchange.getResponseBody().write(responseBytes);
		exchange.close();
	}
	
	/**
	 * Sends an SSE event.
	 */
	private void sendSSEEvent(PrintWriter writer, String eventType, String data) {
		writer.print("event: " + eventType + "\n");
		writer.print("data: " + data + "\n\n");
		writer.flush();
	}
	
	/**
	 * Builds the message endpoint URL for a session.
	 */
	private String buildEndpointUrl(String sessionId) {
		return "http://localhost:" + port + messageEndpoint + "?sessionId=" + sessionId;
	}
	
	/**
	 * Extracts session ID from query string.
	 */
	private String extractSessionId(String query) {
		if (query == null) {
			return null;
		}
		String[] params = query.split("&");
		for (String param : params) {
			String[] parts = param.split("=", 2);
			if (parts.length == 2 && SESSION_ID_PARAM.equals(parts[0])) {
				return parts[1];
			}
		}
		return null;
	}
}

