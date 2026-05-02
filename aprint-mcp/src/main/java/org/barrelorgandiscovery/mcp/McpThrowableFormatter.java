package org.barrelorgandiscovery.mcp;

/**
 * Formate les {@link Throwable} pour les réponses MCP (message lisible + chaîne des causes).
 */
public final class McpThrowableFormatter {

	private McpThrowableFormatter() {
	}

	/**
	 * Résumé court : nom simple de la classe + message si présent.
	 */
	public static String summary(Throwable t) {
		if (t == null) {
			return "null";
		}
		String cn = t.getClass().getSimpleName();
		String m = t.getMessage();
		if (m != null && !m.trim().isEmpty()) {
			return cn + ": " + m;
		}
		if (t instanceof UnsupportedOperationException) {
			return cn + " (souvent dépôt ou stockage en lecture seule)";
		}
		return cn;
	}

	/**
	 * Chaîne complète : types + messages + causes imbriquées.
	 */
	public static String fullChain(Throwable t) {
		if (t == null) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		int depth = 0;
		Throwable cur = t;
		while (cur != null && depth < 20) {
			if (depth > 0) {
				sb.append(" | Caused by: ");
			}
			sb.append(cur.getClass().getName());
			String m = cur.getMessage();
			if (m != null && !m.trim().isEmpty()) {
				sb.append(": ").append(m);
			}
			cur = cur.getCause();
			depth++;
		}
		if (depth >= 20) {
			sb.append(" | …");
		}
		return sb.toString();
	}
}
