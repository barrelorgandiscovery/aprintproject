package org.barrelorgandiscovery.mcp;

import java.util.HashMap;
import java.util.Map;

/**
 * Résultat structuré de {@link APrintMCPContext#importInstrumentFromBook(ImportInstrumentFromBookRequest)}.
 */
public final class ImportInstrumentFromBookResult {

	private final boolean success;
	private final String message;
	private final String referenceBookPath;
	private final String designedInstrumentNameFromBook;
	private final String sourceInstrumentUsed;
	private final String newInstrumentName;
	private final ScaleInfo importedScale;
	private final boolean dryRun;
	private final boolean importPerformed;
	private final ImportInstrumentCompatibilityReport compatibility;
	private final String errorClass;
	private final String errorDetail;

	private ImportInstrumentFromBookResult(boolean success, String message, String referenceBookPath,
			String designedInstrumentNameFromBook, String sourceInstrumentUsed, String newInstrumentName,
			ScaleInfo importedScale, boolean dryRun, boolean importPerformed,
			ImportInstrumentCompatibilityReport compatibility, String errorClass, String errorDetail) {
		this.success = success;
		this.message = message;
		this.referenceBookPath = referenceBookPath;
		this.designedInstrumentNameFromBook = designedInstrumentNameFromBook;
		this.sourceInstrumentUsed = sourceInstrumentUsed;
		this.newInstrumentName = newInstrumentName;
		this.importedScale = importedScale;
		this.dryRun = dryRun;
		this.importPerformed = importPerformed;
		this.compatibility = compatibility;
		this.errorClass = errorClass;
		this.errorDetail = errorDetail;
	}

	public static ImportInstrumentFromBookResult failure(String message, String referenceBookPath,
			String designedInstrumentNameFromBook, String sourceInstrumentUsed, String newInstrumentName,
			ImportInstrumentCompatibilityReport compatibility) {
		return new ImportInstrumentFromBookResult(false, message, referenceBookPath,
			designedInstrumentNameFromBook, sourceInstrumentUsed, newInstrumentName, null,
			false, false, compatibility, null, null);
	}

	public static ImportInstrumentFromBookResult failure(String message, String referenceBookPath,
			String designedInstrumentNameFromBook, String sourceInstrumentUsed, String newInstrumentName) {
		return failure(message, referenceBookPath, designedInstrumentNameFromBook, sourceInstrumentUsed,
			newInstrumentName, null);
	}

	/**
	 * Échec avec exception : {@link #getMessage()} = résumé, {@link #getErrorDetail()} = chaîne complète des causes.
	 */
	public static ImportInstrumentFromBookResult failure(Throwable cause, String referenceBookPath,
			String designedInstrumentNameFromBook, String sourceInstrumentUsed, String newInstrumentName,
			ImportInstrumentCompatibilityReport compatibility) {
		if (cause == null) {
			return failure("(null throwable)", referenceBookPath, designedInstrumentNameFromBook,
				sourceInstrumentUsed, newInstrumentName, compatibility);
		}
		return new ImportInstrumentFromBookResult(false, McpThrowableFormatter.summary(cause), referenceBookPath,
			designedInstrumentNameFromBook, sourceInstrumentUsed, newInstrumentName, null,
			false, false, compatibility, cause.getClass().getName(), McpThrowableFormatter.fullChain(cause));
	}

	public static ImportInstrumentFromBookResult failure(Throwable cause, String referenceBookPath,
			String designedInstrumentNameFromBook, String sourceInstrumentUsed, String newInstrumentName) {
		return failure(cause, referenceBookPath, designedInstrumentNameFromBook, sourceInstrumentUsed,
			newInstrumentName, null);
	}

	public static ImportInstrumentFromBookResult successAfterImport(String message, String referenceBookPath,
			String designedInstrumentNameFromBook, String sourceInstrumentUsed, String newInstrumentName,
			ScaleInfo importedScale, ImportInstrumentCompatibilityReport compatibility) {
		return new ImportInstrumentFromBookResult(true, message, referenceBookPath,
			designedInstrumentNameFromBook, sourceInstrumentUsed, newInstrumentName, importedScale,
			false, true, compatibility, null, null);
	}

	public static ImportInstrumentFromBookResult dryRunOnly(String message, String referenceBookPath,
			String designedInstrumentNameFromBook, String sourceInstrumentUsed, String newInstrumentName,
			ScaleInfo scalePreview, ImportInstrumentCompatibilityReport compatibility) {
		return new ImportInstrumentFromBookResult(true, message, referenceBookPath,
			designedInstrumentNameFromBook, sourceInstrumentUsed, newInstrumentName, scalePreview,
			true, false, compatibility, null, null);
	}

	public boolean isSuccess() {
		return success;
	}

	public String getMessage() {
		return message;
	}

	public String getReferenceBookPath() {
		return referenceBookPath;
	}

	public String getDesignedInstrumentNameFromBook() {
		return designedInstrumentNameFromBook;
	}

	public String getSourceInstrumentUsed() {
		return sourceInstrumentUsed;
	}

	public String getNewInstrumentName() {
		return newInstrumentName;
	}

	public ScaleInfo getImportedScale() {
		return importedScale;
	}

	public boolean isDryRun() {
		return dryRun;
	}

	public boolean isImportPerformed() {
		return importPerformed;
	}

	public ImportInstrumentCompatibilityReport getCompatibility() {
		return compatibility;
	}

	/** Nom complet de la classe d’erreur, si l’échec provient d’une exception. */
	public String getErrorClass() {
		return errorClass;
	}

	/** Chaîne des causes (types + messages), pour diagnostic MCP. */
	public String getErrorDetail() {
		return errorDetail;
	}

	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("success", success);
		map.put("message", message);
		map.put("dryRun", dryRun);
		map.put("importPerformed", importPerformed);
		if (referenceBookPath != null) {
			map.put("referenceBookPath", referenceBookPath);
		}
		if (designedInstrumentNameFromBook != null) {
			map.put("designedInstrumentNameFromBook", designedInstrumentNameFromBook);
		}
		if (sourceInstrumentUsed != null) {
			map.put("sourceInstrumentUsed", sourceInstrumentUsed);
		}
		if (newInstrumentName != null) {
			map.put("newInstrumentName", newInstrumentName);
		}
		if (importedScale != null) {
			map.put("importedScale", importedScale.toMap());
		}
		if (compatibility != null) {
			map.put("compatibility", compatibility.toMap());
		}
		if (errorClass != null) {
			map.put("errorClass", errorClass);
		}
		if (errorDetail != null) {
			map.put("errorDetail", errorDetail);
		}
		return map;
	}
}
