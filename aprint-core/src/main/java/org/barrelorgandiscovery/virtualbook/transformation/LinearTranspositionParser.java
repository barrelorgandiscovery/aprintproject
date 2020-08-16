package org.barrelorgandiscovery.virtualbook.transformation;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.scale.ScaleManager;
import org.barrelorgandiscovery.tools.LineParser;
import org.barrelorgandiscovery.tools.MidiHelper;


public class LinearTranspositionParser implements LineParser {

	private static final Logger logger = Logger
			.getLogger(LinearTranspositionParser.class);

	private ScaleManager gm = null;

	private Scale gamme_source = null;

	private Scale gamme_destination = null;

	private String name = null;

	private boolean applydischarge = true;

	private LinearTransposition transposition;

	public LinearTranspositionParser(ScaleManager gm) {
		this.gm = gm;
	}

	public void lineParsed(String key, String value, int line) throws Exception {

		try {

			if ("name".equals(key)) { //$NON-NLS-1$

				name = value;

			} else if ("applydischarge".equals(key)) { //$NON-NLS-1$
				if ("false".equals(value)) { //$NON-NLS-1$
					applydischarge = false;
				}

				logger.debug("Apply Discharge " + applydischarge); //$NON-NLS-1$

			} else if ("gammesource".equals(key)) { //$NON-NLS-1$
				gamme_source = gm.getScale(value);
				if (gamme_source == null) {
					throw new TranspositionException(Messages.getString("LinearTranspositionParser.5") + value //$NON-NLS-1$
							+ Messages.getString("LinearTranspositionParser.6")); //$NON-NLS-1$
				}
			} else if ("gammedestination".equals(key)) { //$NON-NLS-1$
				gamme_destination = gm.getScale(value);
				if (gamme_destination == null) {
					throw new TranspositionException(Messages.getString("LinearTranspositionParser.8") + value //$NON-NLS-1$
							+ Messages.getString("LinearTranspositionParser.9")); //$NON-NLS-1$
				}

				if (gamme_source == null) {
					throw new TranspositionException(
							Messages.getString("LinearTranspositionParser.10")); //$NON-NLS-1$
				}

				if (name == null) {
					throw new TranspositionException(
							Messages.getString("LinearTranspositionParser.11")); //$NON-NLS-1$
				}

				// on a les deux gammes, on effectue la correspondance
				transposition = new LinearTransposition(gamme_source,
						gamme_destination, name, applydischarge, false);

			} else {
				if (transposition == null) {
					throw new TranspositionException(
							Messages.getString("LinearTranspositionParser.12")); //$NON-NLS-1$
				}
				
				// lecture des valeures de transposition

				int source;

				try {
					source = Integer.parseInt(key);

				} catch (NumberFormatException ex) {
					// on essaye avec la notation anglaise
					source = MidiHelper.midiCode(key); // sinon, exception ...
				}

				final String[] correspondances = value.split(","); //$NON-NLS-1$
				for (int i = 0; i < correspondances.length; i++) {
					final String temp = correspondances[i].trim();
					if (!"".equals(temp)) { //$NON-NLS-1$
						final int destination = Integer.parseInt(temp);
						logger.debug("correspondance " + source + " -> " //$NON-NLS-1$ //$NON-NLS-2$
								+ destination);
						transposition.setCorrespondance(source, destination);
					}
				}
			}

		} catch (Exception ex) {
			throw new TranspositionException(Messages.getString("LinearTranspositionParser.17") + line, ex); //$NON-NLS-1$
		}

	}

	/**
	 * Récupère la transposition
	 * 
	 * @return
	 */
	public LinearTransposition getTransposition() {
		return transposition;
	}

}
