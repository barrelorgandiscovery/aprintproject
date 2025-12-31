package org.barrelorgandiscovery.mcp.transport;

import java.io.PrintWriter;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import io.modelcontextprotocol.json.McpJsonMapper;
import io.modelcontextprotocol.json.TypeRef;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransport;
import reactor.core.publisher.Mono;

/**
 * Implementation of McpServerTransport for Java HttpServer SSE sessions.
 * This class handles the transport-level communication for a specific client session
 * using Java's built-in HttpServer (not Servlet-based).
 * 
 * @author APrint Development Team
 */
public class HttpServerMcpSessionTransport implements McpServerTransport {
	
	private static final Logger logger = Logger.getLogger(HttpServerMcpSessionTransport.class);
	
	private final String sessionId;
	private final PrintWriter writer;
	private final McpJsonMapper jsonMapper;
	private final AtomicBoolean isClosed = new AtomicBoolean(false);
	private final Runnable onClose;
	
	/**
	 * Creates a new session transport with the specified ID and SSE writer.
	 * 
	 * @param sessionId The unique identifier for this session
	 * @param writer The writer for sending server events to the client
	 * @param jsonMapper The JSON mapper for serialization
	 * @param onClose Callback to execute when the transport is closed
	 */
	public HttpServerMcpSessionTransport(String sessionId, PrintWriter writer, 
			McpJsonMapper jsonMapper, Runnable onClose) {
		this.sessionId = sessionId;
		this.writer = writer;
		this.jsonMapper = jsonMapper;
		this.onClose = onClose;
		logger.debug("Session transport " + sessionId + " initialized with SSE writer");
	}
	
	/**
	 * Sends a JSON-RPC message to the client through the SSE connection.
	 * 
	 * @param message The JSON-RPC message to send
	 * @return A Mono that completes when the message has been sent
	 */
	@Override
	public Mono<Void> sendMessage(McpSchema.JSONRPCMessage message) {
		return Mono.fromRunnable(() -> {
			if (isClosed.get()) {
				logger.warn("Attempted to send message to closed session: " + sessionId);
				return;
			}
			
			try {
				String jsonText = jsonMapper.writeValueAsString(message);
				sendSSEEvent(writer, "message", jsonText);
				logger.debug("Message sent to session " + sessionId);
			} catch (Exception e) {
				logger.error("Failed to send message to session " + sessionId + ": " + e.getMessage(), e);
				close();
			}
		});
	}
	
	/**
	 * Converts data from one type to another using the configured JsonMapper.
	 * 
	 * @param data The source data object to convert
	 * @param typeRef The target type reference
	 * @param <T> The target type
	 * @return The converted object of type T
	 */
	@Override
	public <T> T unmarshalFrom(Object data, TypeRef<T> typeRef) {
		return jsonMapper.convertValue(data, typeRef);
	}
	
	/**
	 * Initiates a graceful shutdown of the transport.
	 * 
	 * @return A Mono that completes when the shutdown is complete
	 */
	@Override
	public Mono<Void> closeGracefully() {
		return Mono.fromRunnable(() -> {
			logger.debug("Closing session transport gracefully: " + sessionId);
			close();
		});
	}
	
	/**
	 * Closes the transport immediately.
	 */
	@Override
	public void close() {
		if (isClosed.compareAndSet(false, true)) {
			logger.debug("Closing session transport: " + sessionId);
			if (onClose != null) {
				onClose.run();
			}
		}
	}
	
	/**
	 * Sends an SSE event to the client.
	 * 
	 * @param writer The PrintWriter to write to
	 * @param eventType The event type (e.g., "message", "endpoint")
	 * @param data The event data
	 */
	private void sendSSEEvent(PrintWriter writer, String eventType, String data) {
		try {
			writer.print("event: " + eventType + "\n");
			// SSE data can be multi-line, each line must be prefixed with "data: "
			String[] lines = data.split("\n");
			for (String line : lines) {
				writer.print("data: " + line + "\n");
			}
			writer.print("\n"); // Empty line to end the event
			writer.flush();
		} catch (Exception e) {
			logger.error("Failed to write SSE event: " + e.getMessage(), e);
			throw new RuntimeException("Failed to send SSE event", e);
		}
	}
	
	/**
	 * Get the session ID.
	 * 
	 * @return The session ID
	 */
	public String getSessionId() {
		return sessionId;
	}
	
	/**
	 * Check if the transport is closed.
	 * 
	 * @return true if closed, false otherwise
	 */
	public boolean isClosed() {
		return isClosed.get();
	}
}

