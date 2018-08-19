package org.barrelorgandiscovery.scale;

import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.HashCodeUtils;

public class ReferencedState {

	private String name;
	private String libelle;

	public ReferencedState(String name, String libelle) {
		this.name = name;
		this.libelle = libelle;
	}

	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return libelle;
	}

	@Override
	public boolean equals(Object obj) {

		if (obj == null)
			return false;

		if (obj.getClass() != ReferencedState.class)
			return false;

		ReferencedState eval = (ReferencedState) obj;

		if (eval.name == null) {
			if (name == null)
				return true;
			return false;
		}

		assert eval.name != null;

		return eval.name.equals(name);
	}

	@Override
	public int hashCode() {
		int s = HashCodeUtils.SEED;
		s = HashCodeUtils.hash(s, name);
		return s;
	}

	private static ReferencedState[] all = new ReferencedState[] {
			new ReferencedState(null, Messages.getString("ReferencedState.0")), //$NON-NLS-1$
			new ReferencedState(Scale.GAMME_STATE_INPROGRESS, Messages
					.getString("ReferencedState.1")), //$NON-NLS-1$
			new ReferencedState(Scale.GAMME_STATE_COMPLETED, Messages
					.getString("ReferencedState.2")) }; //$NON-NLS-1$

	public static ReferencedState[] listReferencedState() {
		return all;
	}

	public static ReferencedState fromInternalValue(String value) {
		if (value == null)
			return all[0];

		for (int i = 1; i < all.length; i++) {
			if (all[i].getName().equals(value))
				return all[i];
		}
		return null;
	}

}
