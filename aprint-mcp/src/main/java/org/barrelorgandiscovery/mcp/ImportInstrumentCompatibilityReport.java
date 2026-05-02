package org.barrelorgandiscovery.mcp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Analyse avant / après import : évite les doublons inutiles et documente la compatibilité gamme / instrument source.
 */
public final class ImportInstrumentCompatibilityReport {

	private final String resolvedNewInstrumentName;
	private final String scaleNameFromBook;
	private final int trackCountFromBook;
	private final boolean sourceInstrumentFound;
	private final boolean sourceScaleEqualsBookScale;
	private final boolean scaleWithSameNameInRepository;
	private final boolean scaleDefinitionEqualsRepositoryCopy;
	private final boolean targetInstrumentNameAlreadyUsed;
	private final List<String> repositoryInstrumentsWithEqualScale;

	private ImportInstrumentCompatibilityReport(String resolvedNewInstrumentName, String scaleNameFromBook,
			int trackCountFromBook, boolean sourceInstrumentFound, boolean sourceScaleEqualsBookScale,
			boolean scaleWithSameNameInRepository, boolean scaleDefinitionEqualsRepositoryCopy,
			boolean targetInstrumentNameAlreadyUsed, List<String> repositoryInstrumentsWithEqualScale) {
		this.resolvedNewInstrumentName = resolvedNewInstrumentName;
		this.scaleNameFromBook = scaleNameFromBook;
		this.trackCountFromBook = trackCountFromBook;
		this.sourceInstrumentFound = sourceInstrumentFound;
		this.sourceScaleEqualsBookScale = sourceScaleEqualsBookScale;
		this.scaleWithSameNameInRepository = scaleWithSameNameInRepository;
		this.scaleDefinitionEqualsRepositoryCopy = scaleDefinitionEqualsRepositoryCopy;
		this.targetInstrumentNameAlreadyUsed = targetInstrumentNameAlreadyUsed;
		this.repositoryInstrumentsWithEqualScale = repositoryInstrumentsWithEqualScale;
	}

	public static ImportInstrumentCompatibilityReport analyze(
			org.barrelorgandiscovery.repository.Repository2 repo,
			org.barrelorgandiscovery.scale.Scale bookScale,
			String resolvedNewInstrumentName,
			org.barrelorgandiscovery.instrument.Instrument sourceInstrument) {

		boolean sourceFound = sourceInstrument != null;
		boolean sourceEqualsBook = sourceFound && sourceInstrument.getScale() != null
			&& sourceInstrument.getScale().equals(bookScale);

		boolean nameInRepo = false;
		boolean defEquals = false;
		if (repo != null && bookScale != null) {
			org.barrelorgandiscovery.scale.Scale byName = repo.getScale(bookScale.getName());
			nameInRepo = byName != null;
			defEquals = nameInRepo && byName.equals(bookScale);
		}

		boolean targetUsed = repo != null && resolvedNewInstrumentName != null
			&& !resolvedNewInstrumentName.isEmpty()
			&& repo.getInstrument(resolvedNewInstrumentName) != null;

		List<String> sameScale = new ArrayList<>();
		if (repo != null && bookScale != null) {
			for (org.barrelorgandiscovery.instrument.Instrument ins : repo.listInstruments()) {
				if (ins != null && ins.getScale() != null && ins.getScale().equals(bookScale)) {
					sameScale.add(ins.getName());
				}
			}
		}

		return new ImportInstrumentCompatibilityReport(
			resolvedNewInstrumentName,
			bookScale != null ? bookScale.getName() : null,
			bookScale != null ? bookScale.getTrackNb() : 0,
			sourceFound,
			sourceEqualsBook,
			nameInRepo,
			defEquals,
			targetUsed,
			sameScale);
	}

	public String getResolvedNewInstrumentName() {
		return resolvedNewInstrumentName;
	}

	public String getScaleNameFromBook() {
		return scaleNameFromBook;
	}

	public int getTrackCountFromBook() {
		return trackCountFromBook;
	}

	public boolean isSourceInstrumentFound() {
		return sourceInstrumentFound;
	}

	/**
	 * Si faux, la copie des registerpatch peut être partielle ou incohérente (gammes différentes).
	 */
	public boolean isSourceScaleEqualsBookScale() {
		return sourceScaleEqualsBookScale;
	}

	public boolean isScaleWithSameNameInRepository() {
		return scaleWithSameNameInRepository;
	}

	public boolean isScaleDefinitionEqualsRepositoryCopy() {
		return scaleDefinitionEqualsRepositoryCopy;
	}

	public boolean isTargetInstrumentNameAlreadyUsed() {
		return targetInstrumentNameAlreadyUsed;
	}

	public List<String> getRepositoryInstrumentsWithEqualScale() {
		return repositoryInstrumentsWithEqualScale;
	}

	/**
	 * Indication courte pour l’agent / l’utilisateur.
	 */
	public String getSummaryHint() {
		StringBuilder sb = new StringBuilder();
		if (!repositoryInstrumentsWithEqualScale.isEmpty()) {
			sb.append("Le dépôt contient déjà au moins un instrument avec une gamme identique à ce livre: ");
			sb.append(String.join(", ", repositoryInstrumentsWithEqualScale));
			sb.append(". ");
		}
		if (targetInstrumentNameAlreadyUsed) {
			sb.append("Le nom d’instrument cible est déjà utilisé. ");
		}
		if (sourceInstrumentFound && !sourceScaleEqualsBookScale) {
			sb.append("L’instrument source n’a pas la même gamme que le livre : vérifier les mappings. ");
		}
		if (sb.length() == 0) {
			return "Aucun conflit évident détecté (vérifier quand même les noms et la banque source).";
		}
		return sb.toString().trim();
	}

	public Map<String, Object> toMap() {
		Map<String, Object> m = new HashMap<>();
		if (resolvedNewInstrumentName != null) {
			m.put("resolvedNewInstrumentName", resolvedNewInstrumentName);
		}
		if (scaleNameFromBook != null) {
			m.put("scaleNameFromBook", scaleNameFromBook);
		}
		m.put("trackCountFromBook", trackCountFromBook);
		m.put("sourceInstrumentFound", sourceInstrumentFound);
		m.put("sourceScaleEqualsBookScale", sourceScaleEqualsBookScale);
		m.put("scaleWithSameNameInRepository", scaleWithSameNameInRepository);
		m.put("scaleDefinitionEqualsRepositoryCopy", scaleDefinitionEqualsRepositoryCopy);
		m.put("targetInstrumentNameAlreadyUsed", targetInstrumentNameAlreadyUsed);
		m.put("repositoryInstrumentsWithEqualScale", repositoryInstrumentsWithEqualScale);
		m.put("summaryHint", getSummaryHint());
		return m;
	}
}
