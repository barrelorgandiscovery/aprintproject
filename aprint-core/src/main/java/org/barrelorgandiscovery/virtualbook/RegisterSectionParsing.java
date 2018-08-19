package org.barrelorgandiscovery.virtualbook;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.scale.AbstractRegisterCommandDef;
import org.barrelorgandiscovery.scale.AbstractTrackDef;
import org.barrelorgandiscovery.scale.PipeStop;
import org.barrelorgandiscovery.scale.PipeStopGroup;
import org.barrelorgandiscovery.scale.PipeStopGroupList;
import org.barrelorgandiscovery.scale.RegisterCommandStartDef;
import org.barrelorgandiscovery.scale.RegisterSetCommandResetDef;
import org.barrelorgandiscovery.scale.Scale;

/**
 * This class do the parsing of the registers command into RegistrationSections
 * 
 * @author Freydiere Patrice
 * 
 */
class RegisterSectionParsing {

	private static Logger logger = Logger
			.getLogger(RegisterSectionParsing.class);

	private VirtualBook book;

	public RegisterSectionParsing(VirtualBook book) {
		assert book != null;
		this.book = book;
	}

	static class REvent {
		public REvent(long timestamp, AbstractRegisterCommandDef rd,
				boolean start_stop) {
			this.timestamp = timestamp;
			this.rd = rd;
			this.start_stop = start_stop;
		}

		public long timestamp;
		public AbstractRegisterCommandDef rd;
		public boolean start_stop;
	}

	private ArrayList<RegistrationSection> currentRegistration = new ArrayList<RegistrationSection>();

	private RegistrationSection current = null;

	public ArrayList<RegistrationSection> parse() throws Exception {

		// convert the holes into registration events

		TreeSet<REvent> events = new TreeSet<REvent>(new Comparator<REvent>() {

			public int compare(REvent o1, REvent o2) {

				if (o1.timestamp < o2.timestamp)
					return -1;
				if (o1.timestamp > o2.timestamp)
					return 1;

				assert o1.timestamp == o2.timestamp;

				// on met les commandes reset après les autres pour le
				// traitement
				if (o1.rd instanceof RegisterSetCommandResetDef
						&& o2.rd instanceof RegisterCommandStartDef)
					return 1;

				if (o1.rd instanceof RegisterCommandStartDef
						&& o2.rd instanceof RegisterSetCommandResetDef)
					return -1;

				if (o1.rd.hashCode() < o2.rd.hashCode()) {
					return 1;
				} else {
					return -1;
				}
			}
		});

		Scale gamme = book.getScale();
		AbstractTrackDef[] tds = gamme.getTracksDefinition();

		List<Hole> notesCopy = book.getOrderedHolesCopy();
		for (Hole h : notesCopy) {
			int piste = h.getTrack();

			AbstractTrackDef current = tds[piste];
			if (current instanceof AbstractRegisterCommandDef) {
				AbstractRegisterCommandDef arcd = (AbstractRegisterCommandDef) current;

				// add the begin
				long s = h.getTimestamp()
						+ gamme.mmToTime(Double.isNaN(arcd.getRetard()) ? 0.0
								: arcd.getRetard());
				events.add(new REvent(s, arcd, true));

				// add the end

				long stop = s + h.getTimeLength();
				events.add(new REvent(stop, arcd, false));

			}
		}

		current = null; // pas de section en cours

		for (REvent r : events) {

			if (logger.isDebugEnabled())
				logger.debug("event :" + r.timestamp + " type : "
						+ r.start_stop + " abd :" + r.rd);

			if (r.rd instanceof RegisterCommandStartDef) {
				RegisterCommandStartDef sd = (RegisterCommandStartDef) r.rd;
				activateRegister(r.timestamp, sd.getRegisterSetName(), sd
						.getRegisterInRegisterSet(), r.start_stop);
			} else if (r.rd instanceof RegisterSetCommandResetDef) {
				RegisterSetCommandResetDef scr = (RegisterSetCommandResetDef) r.rd;
				deactivateRegisterSet(r.timestamp, scr.getRegisterSet(),
						r.start_stop);
			}
		}

		// ok we have a chain of RegistrationSections, current is the last one
		// !!

		ArrayList<RegistrationSection> retvalue = new ArrayList<RegistrationSection>();

		RegistrationSection c = current;

		while (c != null) {
			retvalue.add(0, c);
			c = c.getPrevious();
		}

		return retvalue;

	}

	private HashMap<String, TreeSet<String>> activatedRegisterHoles = new HashMap<String, TreeSet<String>>();

	private void activate(String registerset, String register) {
		TreeSet<String> treeSet = activatedRegisterHoles.get(registerset);
		if (treeSet == null)
			treeSet = new TreeSet<String>();

		treeSet.add(register);

		activatedRegisterHoles.put(registerset, treeSet);

	}

	private boolean isActivated(String registerset, String register) {
		TreeSet<String> treeSet = activatedRegisterHoles.get(registerset);
		if (treeSet == null)
			treeSet = new TreeSet<String>();

		return treeSet.contains(register);
	}

	private void deactivate(String registerset, String register) {
		TreeSet<String> treeSet = activatedRegisterHoles.get(registerset);
		if (treeSet == null)
			treeSet = new TreeSet<String>();

		treeSet.remove(register);

		activatedRegisterHoles.put(registerset, treeSet);
	}

	private TreeSet<String> currentlyMaintainedRegisterSetReset = new TreeSet<String>();

	private boolean isGlobalResetActivated = false;

	private void activateRegister(long timestamp, String registerset,
			String register, boolean start_stop) throws Exception {

		logger.debug("activate Register " + timestamp + " registerset "
				+ registerset + " register " + register + " start/stop "
				+ libelleStartStop(start_stop));
		// new registration section
		if (start_stop) {

			logger.debug("start activate Register");

			logger.debug("creating a new section ... ");

			if (current != null && current.getStart() == timestamp) {
				// add a register in the current section ...
				current.addRegister(registerset, register);
			} else {
				// new section ...
				RegistrationSection r = chainNewSection(timestamp);
				r.addRegister(registerset, register);
				current = r;
			}

			activate(registerset, register);

		} else {
			logger.debug("stop activate register");

			if (currentlyMaintainedRegisterSetReset.contains(registerset)
					|| isGlobalResetActivated) {
				// reset is still maintained, we need to add a new section with
				// all the remained registers
				// local to the registerSet
				
				RegistrationSection r = chainNewSection(timestamp);

				// unset the register ...
				
				r.removeRegisters(registerset);
				// r.removeRegister(registerset, register);
				current = r;

			} else {
				logger
						.debug("no reset activated, nothing to do, the registration is the same");

			}

			deactivate(registerset, register);

		}
	}

	final private String libelleStartStop(boolean start_stop) {
		return (start_stop ? "start" : "stop");
	}

	private RegistrationSection chainNewSection(long timestamp) {

		RegistrationSection r = new RegistrationSection(current);
		r.setPrevious(current);
		r.setStart(timestamp);
		if (current == null)
			r.setGamme(book.getScale());
		return r;
	}

	private void deactivateRegisterSet(long timestamp, String registerset,
			boolean start_stop) throws Exception {

		logger.debug("deactivateRegisterSet " + timestamp + " registerset "
				+ registerset + libelleStartStop(start_stop));
		if (start_stop) {

			logger.debug("start");

			RegistrationSection chainNewSection = chainNewSection(timestamp);

			boolean hasChanged = false;

			Scale gamme = book.getScale();
			PipeStopGroupList registerSetList = gamme.getPipeStopGroupList();

			// we look if there is changes between the two sections ...
			if (registerset == null || "ALL".equalsIgnoreCase(registerset)) {
				logger.debug("global reset");

				for (PipeStopGroup rs : registerSetList) {
					PipeStop[] registersInRegisterSet = rs
							.getRegisteredControlledPipeStops();
					for (int i = 0; i < registersInRegisterSet.length; i++) {
						PipeStop currentEvaluatedRegister = registersInRegisterSet[i];

						if (logger.isDebugEnabled())
							logger.debug("currentEvaluatedRegister :"
									+ currentEvaluatedRegister);

						if (chainNewSection.hasRegister(rs.getName(),
								currentEvaluatedRegister.getName())
								&& !isActivated(rs.getName(),
										currentEvaluatedRegister.getName())) {
							hasChanged = true;
							chainNewSection.removeRegister(rs.getName(),
									currentEvaluatedRegister.getName());

						}
					}
				}

				isGlobalResetActivated = true;

			} else {
				logger.debug("local reset");

				assert registerset != null;
				PipeStopGroup rs = registerSetList.get(registerset);

				PipeStop[] registersInRegisterSet = rs
						.getRegisteredControlledPipeStops();
				for (int i = 0; i < registersInRegisterSet.length; i++) {
					String currentEvaluatedRegister = registersInRegisterSet[i]
							.getName();

					String currentregistersetname = rs.getName();
					logger.debug("evaluating register "
							+ currentEvaluatedRegister);
					boolean isinprevioussection = chainNewSection.hasRegister(
							currentregistersetname, currentEvaluatedRegister);
					boolean iscurrentlyseteactivated = isActivated(
							currentregistersetname, currentEvaluatedRegister);

					if (isinprevioussection && !iscurrentlyseteactivated) {
						hasChanged = true;
						logger.debug("reset the register "
								+ currentEvaluatedRegister + " in new section");
						chainNewSection.removeRegister(currentregistersetname,
								currentEvaluatedRegister);

					}

				}
				assert !currentlyMaintainedRegisterSetReset
						.contains(registerset);
				currentlyMaintainedRegisterSetReset.add(registerset);

			}

			if (hasChanged) {
				current = chainNewSection;
				logger.debug("new registration section");
			}

		} else {

			logger.debug("reset stop");

			if (registerset == null || "ALL".equals(registerset)) {
				// assert isGlobalResetActivated;
				isGlobalResetActivated = false;

			} else {
				// stop , nothing to do !!, just maintain the state
				assert currentlyMaintainedRegisterSetReset
						.contains(registerset);
				currentlyMaintainedRegisterSetReset.remove(registerset);
			}
		}

	}
}
