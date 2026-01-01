package org.barrelorgandiscovery.mcp;

import java.util.Map;

import org.barrelorgandiscovery.AsyncJobsManager;
import org.barrelorgandiscovery.gui.aprintng.APrintNG;
import org.barrelorgandiscovery.gui.aprintng.APrintNGGeneralServices;
import org.barrelorgandiscovery.gui.aprintng.APrintNGVirtualBookFrame;
import org.barrelorgandiscovery.gui.aprintng.QuickScriptManager;
import org.barrelorgandiscovery.gui.script.groovy.APrintGroovyConsolePanel;

/**
 * Context interface for MCP server to access APrint application services.
 * This provides a clean interface for the MCP server to interact with
 * the application without tight coupling.
 * 
 * @author APrint Development Team
 */
public interface APrintMCPContext {
	
	/**
	 * Get the main APrint application instance
	 */
	APrintNGGeneralServices getApplication();
	
	/**
	 * Get the async jobs manager
	 */
	AsyncJobsManager getAsyncJobsManager();
	
	/**
	 * Get the current active virtual book frame, if any
	 */
	APrintNGVirtualBookFrame getCurrentVirtualBookFrame();
	
	/**
	 * Create a Groovy console panel for script execution
	 */
	APrintGroovyConsolePanel createGroovyConsolePanel();
	
	/**
	 * Get the QuickScriptManager instance
	 * @return QuickScriptManager or null if not available
	 */
	QuickScriptManager getQuickScriptManager();
	
	/**
	 * Open a visual console dialog with a script loaded
	 * @param scriptName Name of the script to load (null for empty console)
	 * @param scriptContent Initial script content (used if scriptName is null)
	 * @param title Window title
	 * @param readonly If true, script is read-only
	 * @return Resource URI for the opened console (e.g., "aprint://console/{windowId}")
	 */
	String openScriptConsole(String scriptName, String scriptContent, String title, boolean readonly);
	
	/**
	 * Get the console resource manager for tracking open consoles
	 * @return ConsoleResourceManager instance
	 */
	ConsoleResourceManager getConsoleResourceManager();
	
	/**
	 * Liste toutes les VirtualBookFrame ouvertes avec leurs IDs
	 * @return Map de frameId -> APrintNGVirtualBookFrame
	 */
	Map<String, APrintNGVirtualBookFrame> listVirtualBookFrames();
	
	/**
	 * Récupère une VirtualBookFrame par son ID
	 * @param frameId ID de la frame
	 * @return La frame ou null si elle n'existe pas ou est fermée
	 */
	APrintNGVirtualBookFrame getVirtualBookFrame(String frameId);
	
	/**
	 * Exécute un script Groovy directement sur une VirtualBookFrame
	 * @param frameId ID de la frame (null pour utiliser la première disponible)
	 * @param script Code Groovy à exécuter
	 * @param captureOutput Si true, capture stdout/stderr
	 * @return Résultat de l'exécution avec sortie capturée
	 */
	ScriptExecutionResult executeScriptOnFrame(String frameId, String script, boolean captureOutput);
	
	/**
	 * Obtient la fenêtre active actuellement (VirtualBookFrame ou console de script)
	 * @return Informations sur la fenêtre active (type, ID, titre, etc.) ou null si aucune fenêtre active
	 */
	ActiveWindowInfo getActiveWindow();
	
	/**
	 * Récupère le script ouvert dans une console de script
	 * @param resourceUri URI de la ressource console (ex: "aprint://console/{windowId}")
	 * @return Le contenu du script ou null si la console n'existe pas
	 */
	String getConsoleScript(String resourceUri);
	
	/**
	 * Liste tous les instruments disponibles dans le repository
	 * @return Tableau des noms d'instruments
	 */
	String[] listInstruments();
	
	/**
	 * Obtient les informations détaillées d'un instrument par son nom
	 * @param instrumentName Nom de l'instrument
	 * @return Informations sur l'instrument (nom, gamme, description, etc.) ou null si non trouvé
	 */
	InstrumentInfo getInstrumentInfo(String instrumentName);
	
	/**
	 * Liste toutes les gammes (scales) disponibles dans le repository
	 * @return Tableau des noms de gammes
	 */
	String[] listScales();
	
	/**
	 * Obtient les informations détaillées d'une gamme par son nom
	 * @param scaleName Nom de la gamme
	 * @return Informations sur la gamme ou null si non trouvée
	 */
	ScaleInfo getScaleInfo(String scaleName);
	
	/**
	 * Obtient les informations sur un composant Swing
	 * @param windowId ID de la fenêtre (frameId ou console resource URI)
	 * @param componentPath Chemin du composant (ex: "frame_1/toolbarPanel/button_0")
	 * @return Informations sur le composant ou null si non trouvé
	 */
	SwingComponentInfo getComponentInfo(String windowId, String componentPath);
	
	/**
	 * Liste tous les composants d'une fenêtre
	 * @param windowId ID de la fenêtre
	 * @param filterType Filtre optionnel par type de composant (ex: "JButton")
	 * @param maxDepth Profondeur maximale de parcours (défaut: 10)
	 * @return Liste des informations sur les composants
	 */
	java.util.List<SwingComponentInfo> listComponents(String windowId, String filterType, int maxDepth);
	
	/**
	 * Trouve des composants selon des critères
	 * @param windowId ID de la fenêtre
	 * @param criteria Critères de recherche
	 * @return Liste des composants correspondants
	 */
	java.util.List<SwingComponentInfo> findComponents(String windowId, ComponentSearchCriteria criteria);
	
	/**
	 * Obtient la valeur d'un composant
	 * @param windowId ID de la fenêtre
	 * @param componentPath Chemin du composant
	 * @return Valeur du composant (texte, sélection, etc.) ou null
	 */
	Object getComponentValue(String windowId, String componentPath);
	
	/**
	 * Obtient une propriété spécifique d'un composant
	 * @param windowId ID de la fenêtre
	 * @param componentPath Chemin du composant
	 * @param propertyName Nom de la propriété
	 * @return Valeur de la propriété ou null
	 */
	Object getComponentProperty(String windowId, String componentPath, String propertyName);
	
	/**
	 * Liste toutes les fenêtres ouvertes (VirtualBookFrames, consoles, etc.)
	 * @return Liste des informations sur les fenêtres, ordonnées par utilisation la plus récente
	 */
	java.util.List<ActiveWindowInfo> listAllWindows();
	
	/**
	 * Active/met le focus sur une fenêtre par son ID
	 * @param windowId ID de la fenêtre à activer
	 * @return true si la fenêtre a été trouvée et activée, false sinon
	 */
	boolean activateWindow(String windowId);
	
	/**
	 * Obtient l'historique d'activation des fenêtres
	 * @param limit Nombre maximum d'événements à retourner (0 pour tous)
	 * @return Liste des événements d'activation (plus récents en premier)
	 */
	java.util.List<WindowActivationEvent> getWindowActivationHistory(int limit);
	
	/**
	 * Obtient l'historique d'activation pour une fenêtre spécifique
	 * @param windowId ID de la fenêtre
	 * @return Liste des événements d'activation pour cette fenêtre
	 */
	java.util.List<WindowActivationEvent> getWindowActivationHistoryForWindow(String windowId);
	
	/**
	 * Obtient la fenêtre active actuelle depuis l'historique
	 * @return Événement de la fenêtre active actuelle ou null
	 */
	WindowActivationEvent getCurrentActiveWindowFromHistory();
	
	/**
	 * Crée un snapshot (capture d'écran) d'une fenêtre et le retourne en base64
	 * @param windowId ID de la fenêtre (frameId ou console resource URI). Si null, utilise la fenêtre active
	 * @return String base64 de l'image PNG, ou null si la fenêtre n'a pas pu être capturée
	 */
	String createFrameSnapshot(String windowId);
}

