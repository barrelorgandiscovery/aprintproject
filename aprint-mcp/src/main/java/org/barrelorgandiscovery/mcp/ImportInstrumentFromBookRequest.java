package org.barrelorgandiscovery.mcp;

import java.util.HashMap;
import java.util.Map;

/**
 * Paramètres MCP pour l'import d'une gamme depuis un .book et la création d'un instrument cloné.
 * Utiliser {@link #dryRun} pour une analyse sans écriture ; {@link #allowOverwrite} pour remplacer un instrument existant.
 */
public final class ImportInstrumentFromBookRequest {

	private final String referenceBookPath;
	private final String sourceInstrumentName;
	private final String newInstrumentName;
	private final boolean dryRun;
	private final boolean allowOverwrite;
	private final boolean abortIfCompatibleInstrumentExists;

	public ImportInstrumentFromBookRequest(String referenceBookPath, String sourceInstrumentName,
			String newInstrumentName, boolean dryRun, boolean allowOverwrite,
			boolean abortIfCompatibleInstrumentExists) {
		this.referenceBookPath = referenceBookPath != null ? referenceBookPath.trim() : "";
		this.sourceInstrumentName = sourceInstrumentName != null ? sourceInstrumentName.trim() : "";
		this.newInstrumentName = newInstrumentName != null ? newInstrumentName.trim() : "";
		this.dryRun = dryRun;
		this.allowOverwrite = allowOverwrite;
		this.abortIfCompatibleInstrumentExists = abortIfCompatibleInstrumentExists;
	}

	/** Import réel ; pour analyse seule, utiliser {@link #isDryRun()}. */
	public ImportInstrumentFromBookRequest(String referenceBookPath, String sourceInstrumentName,
			String newInstrumentName) {
		this(referenceBookPath, sourceInstrumentName, newInstrumentName, false, false, false);
	}

	public String getReferenceBookPath() {
		return referenceBookPath;
	}

	public String getSourceInstrumentName() {
		return sourceInstrumentName;
	}

	public String getNewInstrumentName() {
		return newInstrumentName;
	}

	public boolean isDryRun() {
		return dryRun;
	}

	/** Si le nom cible existe déjà, autoriser l’écrasement (sinon erreur). */
	public boolean isAllowOverwrite() {
		return allowOverwrite;
	}

	/**
	 * Si vrai et qu’au moins un instrument du dépôt a déjà la même gamme (définition identique),
	 * l’import est refusé pour éviter les doublons inutiles.
	 */
	public boolean isAbortIfCompatibleInstrumentExists() {
		return abortIfCompatibleInstrumentExists;
	}

	public boolean hasSourceInstrumentName() {
		return !sourceInstrumentName.isEmpty();
	}

	public boolean hasNewInstrumentName() {
		return !newInstrumentName.isEmpty();
	}

	public Map<String, Object> toMap() {
		Map<String, Object> m = new HashMap<>();
		m.put("referenceBookPath", referenceBookPath);
		m.put("sourceInstrumentName", sourceInstrumentName);
		m.put("newInstrumentName", newInstrumentName);
		m.put("dryRun", dryRun);
		m.put("allowOverwrite", allowOverwrite);
		m.put("abortIfCompatibleInstrumentExists", abortIfCompatibleInstrumentExists);
		return m;
	}
}
