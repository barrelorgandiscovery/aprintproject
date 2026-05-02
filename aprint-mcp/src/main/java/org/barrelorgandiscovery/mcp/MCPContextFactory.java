package org.barrelorgandiscovery.mcp;

import java.io.OutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.document.Document;
import org.barrelorgandiscovery.search.BookIndexing;
import org.barrelorgandiscovery.search.ScoredDocument;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.gui.aprintng.APrintNGInternalFrame;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookInternalFrame;
import org.barrelorgandiscovery.gui.aprintng.QuickScriptManager;
import org.barrelorgandiscovery.gui.aprintng.VirtualBookScriptConsole;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;

import groovy.lang.Binding;

/**
 * Factory for creating MCP context implementations.
 * 
 * @author APrint Development Team
 */
public class MCPContextFactory {
	
	private static final Logger logger = Logger.getLogger(MCPContextFactory.class);
	
	/**
	 * Create an MCP context for the given application and async jobs manager.
	 * 
	 * @param application The APrint application instance
	 * @param asyncJobsManager The async jobs manager
	 * @return An MCP context implementation
	 */
	public static APrintMCPContext createContext(APrintNGGeneralServices application, AsyncJobsManager asyncJobsManager) {
		return new APrintMCPContextImpl(application, asyncJobsManager);
	}
	
	/**
	 * Implementation of APrintMCPContext.
	 */
	private static class APrintMCPContextImpl implements APrintMCPContext {
		
		private final APrintNGGeneralServices application;
		private final AsyncJobsManager asyncJobsManager;
		private final ConsoleResourceManager consoleResourceManager;
		
		public APrintMCPContextImpl(APrintNGGeneralServices application, AsyncJobsManager asyncJobsManager) {
			this.application = application;
			this.asyncJobsManager = asyncJobsManager;
			this.consoleResourceManager = new ConsoleResourceManager();
		}
		
		@Override
		public APrintNGGeneralServices getApplication() {
			return application;
		}
		
		@Override
		public AsyncJobsManager getAsyncJobsManager() {
			return asyncJobsManager;
		}
		
		@Override
		public APrintNGVirtualBookFrame getCurrentVirtualBookFrame() {
			if (!(application instanceof APrintNG)) {
				return null;
			}
			
			APrintNG aprintNG = (APrintNG) application;
			APrintNGInternalFrame[] frames = aprintNG.listInternalFrames();
			
			// Return the first virtual book frame found (could be enhanced to return the active one)
			for (APrintNGInternalFrame frame : frames) {
				if (frame instanceof APrintNGVirtualBookFrame) {
					return (APrintNGVirtualBookFrame) frame;
				}
			}
			
			return null;
		}
		
		@Override
		public APrintGroovyConsolePanel createGroovyConsolePanel() {
			return new APrintGroovyConsolePanel();
		}
		
		@Override
		public QuickScriptManager getQuickScriptManager() {
			if (!(application instanceof APrintNG)) {
				return null;
			}
			
			APrintNG aprintNG = (APrintNG) application;
			
			// Use reflection to access private field
			try {
				Field field = APrintNG.class.getDeclaredField("scriptManager");
				field.setAccessible(true);
				return (QuickScriptManager) field.get(aprintNG);
			} catch (Exception e) {
				logger.warn("Could not access scriptManager via reflection", e);
				return null;
			}
		}
		
		@Override
		public String openScriptConsole(String scriptName, String scriptContent, String title, boolean readonly) {
			if (!(application instanceof APrintNG)) {
				throw new IllegalStateException("Application is not an APrintNG instance");
			}
			
			// Get current virtual book frame
			APrintNGVirtualBookFrame frame = getCurrentVirtualBookFrame();
			if (frame == null) {
				throw new IllegalStateException("No VirtualBookFrame available to create console");
			}
			
			if (!(frame instanceof APrintNGVirtualBookInternalFrame)) {
				throw new IllegalStateException("Frame is not an APrintNGVirtualBookInternalFrame instance");
			}
			
			APrintNGVirtualBookInternalFrame vbf = (APrintNGVirtualBookInternalFrame) frame;
			
			// Get or create the dedicated MCP console for this frame
			// This ensures only one console per VirtualBookFrame
			final VirtualBookScriptConsole[] consoleRef = new VirtualBookScriptConsole[1];
			
			try {
				SwingUtilities.invokeAndWait(() -> {
					// Get or create the MCP console (only one per frame)
					consoleRef[0] = vbf.getOrCreateMCPConsole();
					
					// Update script content if provided
					if (consoleRef[0] != null) {
						APrintGroovyConsolePanel panel = consoleRef[0].getConsolePanel();
						if (panel != null) {
							// Set readonly if requested
							if (readonly) {
								panel.setScriptPanelEnabled(false);
							} else {
								panel.setScriptPanelEnabled(true);
							}
							
							// Update script content if provided
							if (scriptName != null) {
								QuickScriptManager qsm = getQuickScriptManager();
								if (qsm != null) {
									try {
										StringBuffer content = qsm.loadScript(scriptName);
										consoleRef[0].setScriptContent(content.toString());
									} catch (Exception e) {
										logger.warn("Failed to load script: " + scriptName, e);
									}
								}
							} else if (scriptContent != null) {
								consoleRef[0].setScriptContent(scriptContent);
							}
							
							// Register console resource for MCP tracking (use frameId as unique identifier)
							String frameId = vbf.getMCPFrameId();
							if (frameId == null) {
								// If frame doesn't have an MCP ID yet, get it from the registry
								APrintNG aprintNG = (APrintNG) application;
								java.util.Map<String, APrintNGVirtualBookFrame> frames = aprintNG.listVirtualBookFrames();
								for (java.util.Map.Entry<String, APrintNGVirtualBookFrame> entry : frames.entrySet()) {
									if (entry.getValue() == vbf) {
										frameId = entry.getKey();
										break;
									}
								}
							}
							
							if (frameId != null) {
								String windowId = "mcp_" + frameId;
								consoleResourceManager.registerConsole(scriptName, windowId, panel, consoleRef[0]);
							}
						}
						
						// Register console window for usage tracking
						application.registerWindowForTracking(consoleRef[0]);
					}
				});
			} catch (Exception e) {
				logger.error("Failed to open script console", e);
				throw new RuntimeException("Failed to open script console: " + e.getMessage(), e);
			}
			
			// Return resource URI based on frameId
			String frameId = vbf.getMCPFrameId();
			if (frameId == null) {
				// Fallback: try to find frameId from registry
				APrintNG aprintNG = (APrintNG) application;
				java.util.Map<String, APrintNGVirtualBookFrame> frames = aprintNG.listVirtualBookFrames();
				for (java.util.Map.Entry<String, APrintNGVirtualBookFrame> entry : frames.entrySet()) {
					if (entry.getValue() == vbf) {
						frameId = entry.getKey();
						break;
					}
				}
			}
			
			if (frameId != null) {
				return "aprint://console/mcp_" + frameId;
			}
			
			// Fallback (should not happen)
			return "aprint://console/mcp_" + System.currentTimeMillis();
		}
		
		@Override
		public ConsoleResourceManager getConsoleResourceManager() {
			return consoleResourceManager;
		}
		
		@Override
		public Map<String, APrintNGVirtualBookFrame> listVirtualBookFrames() {
			if (!(application instanceof APrintNG)) {
				return Collections.emptyMap();
			}
			
			APrintNG aprintNG = (APrintNG) application;
			// Utiliser le FrameRegistry de APrintNG
			return aprintNG.listVirtualBookFrames();
		}
		
		@Override
		public APrintNGVirtualBookFrame getVirtualBookFrame(String frameId) {
			if (!(application instanceof APrintNG)) {
				return null;
			}
			
			APrintNG aprintNG = (APrintNG) application;
			// Utiliser le FrameRegistry de APrintNG
			return aprintNG.getVirtualBookFrame(frameId);
		}
		
		@Override
		public ScriptExecutionResult executeScriptOnFrame(String frameId, String script, boolean captureOutput) {
			// Get the frame
			APrintNGVirtualBookFrame frame = frameId != null ? getVirtualBookFrame(frameId) : getCurrentVirtualBookFrame();
			if (frame == null) {
				throw new IllegalArgumentException("No VirtualBookFrame available");
			}
			
			// Create execution context
			APrintGroovyConsolePanel console = createGroovyConsolePanel();
			Binding binding = console.getCurrentBindingRef();
			
			// Configure context variables
			binding.setProperty("virtualbook", frame.getVirtualBook());
			binding.setProperty("pianoroll", frame.getPianoRoll());
			binding.setProperty("currentinstrument", frame.getCurrentInstrument());
			binding.setProperty("services", application);
			
			// Capture output if requested
			StringBuilder outputCapture = new StringBuilder();
			PrintStream originalOut = System.out;
			PrintStream originalErr = System.err;
			
			if (captureOutput) {
				PrintStream capturedOut = new PrintStream(new OutputStream() {
					@Override
					public void write(int b) {
						outputCapture.append((char) b);
					}
					@Override
					public void write(byte[] b, int off, int len) {
						outputCapture.append(new String(b, off, len));
					}
				});
				System.setOut(capturedOut);
				System.setErr(capturedOut);
			}
			
			try {
				// Execute script
				console.setScriptContent(script);
				Future<Object> future = console.run();
				Object result = future.get(30, TimeUnit.SECONDS); // 30 second timeout
				
				return new ScriptExecutionResult(true, result, outputCapture.toString(), null);
			} catch (Exception e) {
				logger.error("Error executing script on frame", e);
				return new ScriptExecutionResult(false, null, outputCapture.toString(), e.getMessage());
			} finally {
				// Restore original streams
				if (captureOutput) {
					System.setOut(originalOut);
					System.setErr(originalErr);
				}
			}
		}
		
		@Override
		public ActiveWindowInfo getActiveWindow() {
			// Delegate to the application's getActiveWindow() method
			// This centralizes all Swing/visual library usage in aprint-gui
			org.barrelorgandiscovery.gui.aprintng.ActiveWindowInfo guiActiveWindow = application.getActiveWindow();
			
			if (guiActiveWindow == null) {
				return null;
			}
			
			// Convert from aprint-gui ActiveWindowInfo to aprint-mcp ActiveWindowInfo
			// Try to find the actual resource URI for script consoles from the console manager
			String resourceUri = guiActiveWindow.getResourceUri();
			if (guiActiveWindow.getType() == org.barrelorgandiscovery.gui.aprintng.ActiveWindowInfo.WindowType.SCRIPT_CONSOLE) {
				// Try to find the actual resource URI from the console manager
				for (Map.Entry<String, ConsoleResource> entry : consoleResourceManager.getAllConsoles().entrySet()) {
					if (entry.getValue().getDialog().getTitle().equals(guiActiveWindow.getTitle())) {
						resourceUri = entry.getKey();
						break;
					}
				}
			}
			
			// Convert WindowType enum
			ActiveWindowInfo.WindowType mcpType;
			switch (guiActiveWindow.getType()) {
				case VIRTUAL_BOOK_FRAME:
					mcpType = ActiveWindowInfo.WindowType.VIRTUAL_BOOK_FRAME;
					break;
				case SCRIPT_CONSOLE:
					mcpType = ActiveWindowInfo.WindowType.SCRIPT_CONSOLE;
					break;
				default:
					mcpType = ActiveWindowInfo.WindowType.UNKNOWN;
			}
			
			return new ActiveWindowInfo(
				mcpType,
				guiActiveWindow.getWindowId(),
				guiActiveWindow.getTitle(),
				guiActiveWindow.getFrameId(),
				resourceUri
			);
		}
		
		@Override
		public String getConsoleScript(String resourceUri) {
			try {
				ConsoleResource resource = consoleResourceManager.getConsoleResource(resourceUri);
				if (resource == null) {
					return null;
				}
				APrintGroovyConsolePanel panel = resource.getConsole();
				if (panel == null) {
					return null;
				}
				final String[] out = new String[1];
				Runnable read = () -> out[0] = panel.getScriptContent();
				if (SwingUtilities.isEventDispatchThread()) {
					read.run();
				} else {
					SwingUtilities.invokeAndWait(read);
				}
				return out[0];
			} catch (Exception e) {
				logger.error("Error getting console script: " + resourceUri, e);
				return null;
			}
		}
		
		@Override
		public String[] listInstruments() {
			org.barrelorgandiscovery.repository.Repository2 repository = application.getRepository();
			if (repository == null) {
				return new String[0];
			}
			
			try {
				org.barrelorgandiscovery.instrument.Instrument[] instruments = repository.listInstruments();
				if (instruments == null) {
					return new String[0];
				}
				
				String[] names = new String[instruments.length];
				for (int i = 0; i < instruments.length; i++) {
					names[i] = instruments[i].getName();
				}
				return names;
			} catch (Exception e) {
				logger.error("Error listing instruments", e);
				return new String[0];
			}
		}
		
		@Override
		public InstrumentInfo getInstrumentInfo(String instrumentName) {
			org.barrelorgandiscovery.repository.Repository2 repository = application.getRepository();
			if (repository == null || instrumentName == null) {
				return null;
			}
			
			try {
				org.barrelorgandiscovery.instrument.Instrument instrument = repository.getInstrument(instrumentName);
				if (instrument == null) {
					return null;
				}
				
				org.barrelorgandiscovery.scale.Scale scale = instrument.getScale();
				String scaleName = scale != null ? scale.getName() : null;
				int trackNb = scale != null ? scale.getTrackNb() : 0;
				
				return new InstrumentInfo(
					instrument.getName(),
					scaleName,
					instrument.getDescriptionUrl(),
					instrument.getThumbnail() != null,
					instrument.getMiniPicture() != null,
					trackNb
				);
			} catch (Exception e) {
				logger.error("Error getting instrument info: " + instrumentName, e);
				return null;
			}
		}
		
		@Override
		public String[] listScales() {
			org.barrelorgandiscovery.repository.Repository2 repository = application.getRepository();
			if (repository == null) {
				return new String[0];
			}
			
			try {
				return repository.getScaleNames();
			} catch (Exception e) {
				logger.error("Error listing scales", e);
				return new String[0];
			}
		}
		
		@Override
		public ScaleInfo getScaleInfo(String scaleName) {
			org.barrelorgandiscovery.repository.Repository2 repository = application.getRepository();
			if (repository == null || scaleName == null) {
				return null;
			}
			
			try {
				org.barrelorgandiscovery.scale.Scale scale = repository.getScale(scaleName);
				if (scale == null) {
					return null;
				}
				
				return new ScaleInfo(
					scale.getName(),
					scale.getWidth(),
					scale.getTrackNb(),
					scale.getSpeed(),
					scale.getInformations(),
					scale.getState(),
					scale.getContact(),
					scale.isBookMovingRightToLeft()
				);
			} catch (Exception e) {
				logger.error("Error getting scale info: " + scaleName, e);
				return null;
			}
		}
		
		@Override
		public SwingComponentInfo getComponentInfo(String windowId, String componentPath) {
			org.barrelorgandiscovery.gui.aprintng.SwingComponentInfo guiInfo = 
				application.getComponentInfo(windowId, componentPath);
			
			if (guiInfo == null) {
				return null;
			}
			
			// Convert bounds
			java.util.Map<String, Object> boundsMap = null;
			if (guiInfo.getBounds() != null) {
				boundsMap = new java.util.HashMap<>();
				boundsMap.put("x", guiInfo.getBounds().x);
				boundsMap.put("y", guiInfo.getBounds().y);
				boundsMap.put("width", guiInfo.getBounds().width);
				boundsMap.put("height", guiInfo.getBounds().height);
			}
			
			return new SwingComponentInfo(
				guiInfo.getComponentId(),
				guiInfo.getComponentPath(),
				guiInfo.getClassName(),
				guiInfo.getName(),
				guiInfo.getText(),
				guiInfo.isVisible(),
				guiInfo.isEnabled(),
				boundsMap,
				guiInfo.getProperties(),
				guiInfo.getChildIds(),
				guiInfo.getParentId()
			);
		}
		
		@Override
		public java.util.List<SwingComponentInfo> listComponents(String windowId, String filterType, int maxDepth) {
			java.util.List<org.barrelorgandiscovery.gui.aprintng.SwingComponentInfo> guiList = 
				application.listComponents(windowId, filterType, maxDepth);
			
			java.util.List<SwingComponentInfo> result = new java.util.ArrayList<>();
			for (org.barrelorgandiscovery.gui.aprintng.SwingComponentInfo guiInfo : guiList) {
				// Convert bounds
				java.util.Map<String, Object> boundsMap = null;
				if (guiInfo.getBounds() != null) {
					boundsMap = new java.util.HashMap<>();
					boundsMap.put("x", guiInfo.getBounds().x);
					boundsMap.put("y", guiInfo.getBounds().y);
					boundsMap.put("width", guiInfo.getBounds().width);
					boundsMap.put("height", guiInfo.getBounds().height);
				}
				
				result.add(new SwingComponentInfo(
					guiInfo.getComponentId(),
					guiInfo.getComponentPath(),
					guiInfo.getClassName(),
					guiInfo.getName(),
					guiInfo.getText(),
					guiInfo.isVisible(),
					guiInfo.isEnabled(),
					boundsMap,
					guiInfo.getProperties(),
					guiInfo.getChildIds(),
					guiInfo.getParentId()
				));
			}
			
			return result;
		}
		
		@Override
		public java.util.List<SwingComponentInfo> findComponents(String windowId, ComponentSearchCriteria criteria) {
			org.barrelorgandiscovery.gui.aprintng.ComponentSearchCriteria guiCriteria = 
				new org.barrelorgandiscovery.gui.aprintng.ComponentSearchCriteria(
					criteria.getName(),
					criteria.getType(),
					criteria.getActionCommand(),
					criteria.getText()
				);
			
			java.util.List<org.barrelorgandiscovery.gui.aprintng.SwingComponentInfo> guiList = 
				application.findComponents(windowId, guiCriteria);
			
			java.util.List<SwingComponentInfo> result = new java.util.ArrayList<>();
			for (org.barrelorgandiscovery.gui.aprintng.SwingComponentInfo guiInfo : guiList) {
				// Convert bounds
				java.util.Map<String, Object> boundsMap = null;
				if (guiInfo.getBounds() != null) {
					boundsMap = new java.util.HashMap<>();
					boundsMap.put("x", guiInfo.getBounds().x);
					boundsMap.put("y", guiInfo.getBounds().y);
					boundsMap.put("width", guiInfo.getBounds().width);
					boundsMap.put("height", guiInfo.getBounds().height);
				}
				
				result.add(new SwingComponentInfo(
					guiInfo.getComponentId(),
					guiInfo.getComponentPath(),
					guiInfo.getClassName(),
					guiInfo.getName(),
					guiInfo.getText(),
					guiInfo.isVisible(),
					guiInfo.isEnabled(),
					boundsMap,
					guiInfo.getProperties(),
					guiInfo.getChildIds(),
					guiInfo.getParentId()
				));
			}
			
			return result;
		}
		
		@Override
		public Object getComponentValue(String windowId, String componentPath) {
			return application.getComponentValue(windowId, componentPath);
		}
		
		@Override
		public Object getComponentProperty(String windowId, String componentPath, String propertyName) {
			return application.getComponentProperty(windowId, componentPath, propertyName);
		}
		
		@Override
		public java.util.List<ActiveWindowInfo> listAllWindows() {
			java.util.List<org.barrelorgandiscovery.gui.aprintng.ActiveWindowInfo> guiWindows = 
				application.listAllWindows();
			
			java.util.List<ActiveWindowInfo> result = new java.util.ArrayList<>();
			for (org.barrelorgandiscovery.gui.aprintng.ActiveWindowInfo guiWindow : guiWindows) {
				// Convert WindowType enum
				ActiveWindowInfo.WindowType mcpType;
				switch (guiWindow.getType()) {
					case VIRTUAL_BOOK_FRAME:
						mcpType = ActiveWindowInfo.WindowType.VIRTUAL_BOOK_FRAME;
						break;
					case SCRIPT_CONSOLE:
						mcpType = ActiveWindowInfo.WindowType.SCRIPT_CONSOLE;
						break;
					default:
						mcpType = ActiveWindowInfo.WindowType.UNKNOWN;
				}
				
				// Try to find the actual resource URI for script consoles
				String resourceUri = guiWindow.getResourceUri();
				if (guiWindow.getType() == org.barrelorgandiscovery.gui.aprintng.ActiveWindowInfo.WindowType.SCRIPT_CONSOLE) {
					for (Map.Entry<String, ConsoleResource> entry : consoleResourceManager.getAllConsoles().entrySet()) {
						if (entry.getValue().getDialog().getTitle().equals(guiWindow.getTitle())) {
							resourceUri = entry.getKey();
							break;
						}
					}
				}
				
				result.add(new ActiveWindowInfo(
					mcpType,
					guiWindow.getWindowId(),
					guiWindow.getTitle(),
					guiWindow.getFrameId(),
					resourceUri
				));
			}
			
			return result;
		}
		
		@Override
		public boolean activateWindow(String windowId) {
			return application.activateWindow(windowId);
		}
		
		@Override
		public java.util.List<WindowActivationEvent> getWindowActivationHistory(int limit) {
			java.util.List<org.barrelorgandiscovery.gui.aprintng.WindowActivationHistory.ActivationEvent> guiHistory = 
				application.getWindowActivationHistory(limit);
			
			java.util.List<WindowActivationEvent> result = new java.util.ArrayList<>();
			for (org.barrelorgandiscovery.gui.aprintng.WindowActivationHistory.ActivationEvent guiEvent : guiHistory) {
				result.add(new WindowActivationEvent(
					guiEvent.getTimestamp(),
					guiEvent.getWindowId(),
					guiEvent.getWindowType(),
					guiEvent.getTitle(),
					guiEvent.getFrameId(),
					guiEvent.getResourceUri()
				));
			}
			return result;
		}
		
		@Override
		public java.util.List<WindowActivationEvent> getWindowActivationHistoryForWindow(String windowId) {
			java.util.List<org.barrelorgandiscovery.gui.aprintng.WindowActivationHistory.ActivationEvent> guiHistory = 
				application.getWindowActivationHistoryForWindow(windowId);
			
			java.util.List<WindowActivationEvent> result = new java.util.ArrayList<>();
			for (org.barrelorgandiscovery.gui.aprintng.WindowActivationHistory.ActivationEvent guiEvent : guiHistory) {
				result.add(new WindowActivationEvent(
					guiEvent.getTimestamp(),
					guiEvent.getWindowId(),
					guiEvent.getWindowType(),
					guiEvent.getTitle(),
					guiEvent.getFrameId(),
					guiEvent.getResourceUri()
				));
			}
			return result;
		}
		
		@Override
		public WindowActivationEvent getCurrentActiveWindowFromHistory() {
			org.barrelorgandiscovery.gui.aprintng.WindowActivationHistory.ActivationEvent guiEvent = 
				application.getCurrentActiveWindowFromHistory();
			
			if (guiEvent == null) {
				return null;
			}
			
			return new WindowActivationEvent(
				guiEvent.getTimestamp(),
				guiEvent.getWindowId(),
				guiEvent.getWindowType(),
				guiEvent.getTitle(),
				guiEvent.getFrameId(),
				guiEvent.getResourceUri()
			);
		}
		
		@Override
		public String createFrameSnapshot(String windowId) {
			try {
				// Get the window component
				java.awt.Window window = getWindowForSnapshot(windowId);
				if (window == null || !window.isVisible()) {
					logger.warn("Window not found or not visible: " + windowId);
					return null;
				}
				
				// Capture screenshot using Robot
				final java.awt.image.BufferedImage[] imageRef = new java.awt.image.BufferedImage[1];
				final Exception[] exceptionRef = new Exception[1];
				
				javax.swing.SwingUtilities.invokeAndWait(() -> {
					try {
						// Get bounds in screen coordinates
						java.awt.Point location = window.getLocationOnScreen();
						java.awt.Dimension size = window.getSize();
						java.awt.Rectangle bounds = new java.awt.Rectangle(location.x, location.y, size.width, size.height);
						
						java.awt.Robot robot = new java.awt.Robot(window.getGraphicsConfiguration().getDevice());
						imageRef[0] = robot.createScreenCapture(bounds);
					} catch (Exception e) {
						exceptionRef[0] = e;
					}
				});
				
				if (exceptionRef[0] != null) {
					logger.error("Error capturing screenshot", exceptionRef[0]);
					return null;
				}
				
				if (imageRef[0] == null) {
					logger.warn("Failed to capture screenshot");
					return null;
				}
				
				// Convert to PNG and encode as base64
				java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
				javax.imageio.ImageIO.write(imageRef[0], "PNG", baos);
				byte[] imageBytes = baos.toByteArray();
				return java.util.Base64.getEncoder().encodeToString(imageBytes);
				
			} catch (Exception e) {
				logger.error("Error creating frame snapshot: " + windowId, e);
				return null;
			}
		}
		
		@Override
		public LibrarySearchResult searchIndexedBooks(String query, int maxResults) {
			if (!(application instanceof APrintNG)) {
				return LibrarySearchResult.failure("Book indexing not available (APrintNG context required)");
			}
			APrintNG ng = (APrintNG) application;
			BookIndexing bi = ng.getBookIndexing();
			if (bi == null) {
				return LibrarySearchResult.failure("Book index not initialized");
			}
			int limit = maxResults <= 0 ? 50 : Math.min(maxResults, 200);
			try {
				ScoredDocument[] hits = bi.search(query);
				List<IndexedBookHit> out = new ArrayList<>();
				for (int i = 0; i < hits.length && i < limit; i++) {
					Document d = hits[i].document;
					double sc = hits[i].score;
					out.add(new IndexedBookHit(
						sc,
						d.get(BookIndexing.NAME_FIELD),
						d.get(BookIndexing.SCALE_FIELD),
						d.get(BookIndexing.INSTRUMENT_FIELD),
						d.get(BookIndexing.GENRE_FIELD),
						d.get(BookIndexing.DESCRIPTION_FIELD),
						d.get(BookIndexing.FILEREF_FIELD)));
				}
				return LibrarySearchResult.ok(out);
			} catch (Exception e) {
				logger.error("searchIndexedBooks", e);
				return LibrarySearchResult.failure(
					e.getMessage() != null ? e.getMessage() : "search failed");
			}
		}
		
		@Override
		public ImportInstrumentFromBookResult importInstrumentFromBook(ImportInstrumentFromBookRequest request) {
			if (request == null) {
				return ImportInstrumentFromBookResult.failure("request is null", null, null, null, null);
			}
			String referenceBookPath = request.getReferenceBookPath();
			String srcName = request.getSourceInstrumentName();
			try {
				if (referenceBookPath.isEmpty()) {
					return ImportInstrumentFromBookResult.failure(
						"referenceBookPath is required (absolute path to a .book containing the gamme)",
						null, null, srcName, request.getNewInstrumentName());
				}
				if (!request.hasSourceInstrumentName()) {
					return ImportInstrumentFromBookResult.failure(
						"sourceInstrumentName is required (repository instrument to clone the sound bank from)",
						referenceBookPath, null, null, request.getNewInstrumentName());
				}
				org.barrelorgandiscovery.repository.Repository2 repo = application.getRepository();
				if (repo == null) {
					return ImportInstrumentFromBookResult.failure("No repository configured",
						referenceBookPath, null, srcName, request.getNewInstrumentName());
				}
				if (!request.isDryRun() && repo.isReadOnly()) {
					return ImportInstrumentFromBookResult.failure("Repository is read-only",
						referenceBookPath, null, srcName, request.getNewInstrumentName());
				}
				java.io.File bookFile = new java.io.File(referenceBookPath);
				final String normalizedPath;
				if (!bookFile.isFile()) {
					normalizedPath = referenceBookPath;
				} else {
					String resolved;
					try {
						resolved = bookFile.getCanonicalPath();
					} catch (java.io.IOException e) {
						resolved = bookFile.getAbsolutePath();
					}
					normalizedPath = resolved;
				}
				if (!bookFile.isFile()) {
					return ImportInstrumentFromBookResult.failure("File not found: " + referenceBookPath,
						normalizedPath, null, srcName, request.getNewInstrumentName());
				}
				final Throwable[] err = new Throwable[1];
				final org.barrelorgandiscovery.scale.Scale[] scaleHolder =
					new org.barrelorgandiscovery.scale.Scale[1];
				final String[] designedInstrumentHolder = new String[1];
				final String[] resolvedNewInstrumentName = new String[1];
				final ImportInstrumentCompatibilityReport[] compatHolder =
					new ImportInstrumentCompatibilityReport[1];
				final ImportInstrumentFromBookResult[] dryOrImportResult =
					new ImportInstrumentFromBookResult[1];
				javax.swing.SwingUtilities.invokeAndWait(() -> {
					try {
						org.barrelorgandiscovery.xml.VirtualBookXmlIO.VirtualBookResult r =
							org.barrelorgandiscovery.xml.VirtualBookXmlIO.read(bookFile);
						if (r == null || r.virtualBook == null) {
							throw new Exception("Could not read virtual book from file");
						}
						designedInstrumentHolder[0] = r.preferredInstrumentName;
						if (request.hasNewInstrumentName()) {
							resolvedNewInstrumentName[0] = request.getNewInstrumentName();
						} else {
							String pref = r.preferredInstrumentName;
							if (pref != null && !pref.trim().isEmpty()) {
								resolvedNewInstrumentName[0] = pref.trim();
							} else {
								throw new Exception(
									"Provide newInstrumentName or set DesignedInstrumentName in the book metadata");
							}
						}
						org.barrelorgandiscovery.scale.Scale scale = r.virtualBook.getScale();
						if (scale == null) {
							throw new Exception("Book has no embedded scale");
						}
						org.barrelorgandiscovery.instrument.Instrument source = repo.getInstrument(srcName);
						ImportInstrumentCompatibilityReport compat = ImportInstrumentCompatibilityReport.analyze(
							repo, scale, resolvedNewInstrumentName[0], source);
						compatHolder[0] = compat;
						if (!compat.isSourceInstrumentFound()) {
							throw new Exception("Source instrument not found: " + srcName);
						}
						if (request.isDryRun()) {
							ScaleInfo preview = new ScaleInfo(
								scale.getName(),
								scale.getWidth(),
								scale.getTrackNb(),
								scale.getSpeed(),
								scale.getInformations(),
								scale.getState(),
								scale.getContact(),
								scale.isBookMovingRightToLeft());
							String hint = compat.getSummaryHint();
							dryOrImportResult[0] = ImportInstrumentFromBookResult.dryRunOnly(
								"dryRun: aucune écriture. " + hint,
								normalizedPath,
								designedInstrumentHolder[0],
								srcName,
								resolvedNewInstrumentName[0],
								preview,
								compat);
							return;
						}
						if (request.isAbortIfCompatibleInstrumentExists()
							&& !compat.getRepositoryInstrumentsWithEqualScale().isEmpty()) {
							throw new Exception(
								"Import annulé : le dépôt a déjà un instrument avec cette gamme ("
									+ String.join(", ", compat.getRepositoryInstrumentsWithEqualScale())
									+ "). Utilisez un instrument existant ou désactivez abortIfCompatibleInstrumentExists.");
						}
						if (compat.isTargetInstrumentNameAlreadyUsed() && !request.isAllowOverwrite()) {
							throw new Exception(
								"Un instrument nommé « " + resolvedNewInstrumentName[0]
									+ " » existe déjà. Passez allowOverwrite=true pour remplacer, ou changez le nom.");
						}
						if (compat.isTargetInstrumentNameAlreadyUsed() && request.isAllowOverwrite()) {
							repo.deleteInstrument(repo.getInstrument(resolvedNewInstrumentName[0]));
						}
						repo.saveScale(scale);
						String insName = resolvedNewInstrumentName[0];
						String descUrl = "Sound bank from \"" + srcName + "\" (import from book)";
						org.barrelorgandiscovery.instrument.Instrument ins =
							new org.barrelorgandiscovery.instrument.Instrument(
								insName,
								scale,
								source.getSoundBankStream(),
								source.getThumbnail(),
								descUrl);
						org.barrelorgandiscovery.instrument.RegisterSoundLink srcLinks =
							source.getRegisterSoundLink();
						org.barrelorgandiscovery.instrument.RegisterSoundLink dstLinks =
							ins.getRegisterSoundLink();
						for (String group : srcLinks.getPipeStopGroupNamesInWhichThereAreMappings()) {
							for (String ps : srcLinks.getPipeStopNamesInWhichThereAreMappings(group)) {
								try {
									int preset = srcLinks.getInstrumentNumber(group, ps);
									dstLinks.defineLink(group, ps, preset);
								} catch (Exception ex) {
									logger.warn("Register mapping skipped " + group + "/" + ps + ": "
										+ ex.getMessage());
								}
							}
						}
						try {
							if (srcLinks.getDrumSoundBank() >= 0) {
								dstLinks.setDrumSoundBank(srcLinks.getDrumSoundBank());
							}
						} catch (Exception ignored) {
							// optional
						}
						repo.saveInstrument(ins);
						scaleHolder[0] = scale;
					} catch (Exception e) {
						err[0] = e;
					}
				});
				if (dryOrImportResult[0] != null) {
					return dryOrImportResult[0];
				}
				if (err[0] != null) {
					Throwable t = err[0];
					while (t instanceof java.lang.reflect.InvocationTargetException && t.getCause() != null) {
						t = t.getCause();
					}
					return ImportInstrumentFromBookResult.failure(t, normalizedPath,
						designedInstrumentHolder[0], srcName, request.getNewInstrumentName(), compatHolder[0]);
				}
				String insName = resolvedNewInstrumentName[0];
				org.barrelorgandiscovery.scale.Scale sc = scaleHolder[0];
				ScaleInfo scaleInfo = new ScaleInfo(
					sc.getName(),
					sc.getWidth(),
					sc.getTrackNb(),
					sc.getSpeed(),
					sc.getInformations(),
					sc.getState(),
					sc.getContact(),
					sc.isBookMovingRightToLeft());
				String msg = "Gamme et instrument enregistrés dans le dépôt. Sélectionnez « " + insName
					+ " » pour la lecture (redémarrer APrint si l’instrument n’apparaît pas).";
				return ImportInstrumentFromBookResult.successAfterImport(msg, normalizedPath,
					designedInstrumentHolder[0], srcName, insName, scaleInfo, compatHolder[0]);
			} catch (Exception e) {
				logger.error("importInstrumentFromBook", e);
				return ImportInstrumentFromBookResult.failure(e, referenceBookPath, null, srcName,
					request.getNewInstrumentName());
			}
		}
		
		/**
		 * Helper method to get a Window component by windowId
		 */
		private java.awt.Window getWindowForSnapshot(String windowId) {
			if (windowId == null || windowId.isEmpty()) {
				// Get active window
				ActiveWindowInfo activeWindow = getActiveWindow();
				if (activeWindow == null) {
					return null;
				}
				windowId = activeWindow.getWindowId();
			}
			
			// Try to get as VirtualBookFrame
			Map<String, APrintNGVirtualBookFrame> frames = listVirtualBookFrames();
			APrintNGVirtualBookFrame frame = frames.get(windowId);
			if (frame != null && frame instanceof java.awt.Window) {
				return (java.awt.Window) frame;
			}
			
			// Try to get as console (check if it's a console resource URI)
			if (windowId.startsWith("aprint://console/")) {
				ConsoleResource resource = consoleResourceManager.getConsoleResource(windowId);
				if (resource != null) {
					java.awt.Window dialog = resource.getDialog();
					if (dialog != null && dialog.isVisible()) {
						return dialog;
					}
				}
				return null;
			}
			
			// Try to get as main window
			if (windowId.equals("main") || windowId.equals("aprintng")) {
				if (application instanceof APrintNG) {
					APrintNG aprintNG = (APrintNG) application;
					return aprintNG;
				}
			}
			
			// Try to find in all InternalFrames
			if (application instanceof APrintNG) {
				APrintNG aprintNG = (APrintNG) application;
				APrintNGInternalFrame[] allInternalFrames = aprintNG.listInternalFrames();
				for (APrintNGInternalFrame internalFrame : allInternalFrames) {
					if (internalFrame != null && !internalFrame.isDisposed()) {
						// Try to match by checking if it's a window and matches the ID
						if (internalFrame instanceof java.awt.Window) {
							java.awt.Window w = (java.awt.Window) internalFrame;
							String frameName = w.getName();
							if (windowId.equals(frameName)) {
								return w;
							}
							// Also check title for JFrame
							if (w instanceof javax.swing.JFrame) {
								String title = ((javax.swing.JFrame) w).getTitle();
								if (windowId.equals(title)) {
									return w;
								}
							}
						}
					}
				}
			}
			
			// Try to find in all windows
			java.awt.Window[] allWindows = java.awt.Window.getWindows();
			for (java.awt.Window w : allWindows) {
				if (w.isVisible()) {
					String name = w.getName();
					if (windowId.equals(name)) {
						return w;
					}
					if (w instanceof javax.swing.JFrame) {
						String title = ((javax.swing.JFrame) w).getTitle();
						if (windowId.equals(title)) {
							return w;
						}
					}
				}
			}
			
			return null;
		}
	}
}

