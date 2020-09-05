package org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GRBLStatus {

	public static class Pos {
		public double x;
		public double y;
		public double z;

		@Override
		public String toString() {
			return "" + x + "," + y + "," + z;
		}
	}

	public String status;
	public Pos machinePosition;
	public Pos workingPosition;
	public Integer bufferSize;
	public Integer availableCommandsInPlannedBuffer;
	public Integer RXBuffer;

	// this is the GRBL punch status parsing
	private static final Pattern PATTERN_STATUS = Pattern
			.compile("([^,]+),MPos:([^,]+),([^,]+),([^,]+),WPos:([^,]+),([^,]+),([^,]+)(,Buf:([^,]+))?(,RX:([^,]+))?");

	// Run|MPos:0.000,16.276,0.000|Bf:14,31|FS:500,0
	// Run|MPos:0.000,83.252,0.000|Bf:14,31|FS:500,0|Ov:100,100,100
	private static final Pattern PATTERN_STATUS11 = Pattern
			.compile("([^|]+)\\|MPos:([^,|]+),([^,|]+),([^,|]+)(\\|Bf:([^,|]+),([^,|]+))?(\\|FS:([^,|])+,([^,|])+)?(\\|.*)?");
	
	
	
	private static Pos readPosFromMatcher(Matcher m, int groupstart) {
		Pos p = new Pos();

		p.x = Double.parseDouble(m.group(groupstart));
		p.y = Double.parseDouble(m.group(groupstart + 1));
		p.z = Double.parseDouble(m.group(groupstart + 2));

		return p;
	}

	public static GRBLStatus parse(String s) throws Exception {
		Matcher m = PATTERN_STATUS.matcher(s);
		if (!m.matches()) {
			// no match for regular 0.x version,
			// try 11 version
			return parse11(s);
		}

		GRBLStatus status = new GRBLStatus();
		status.status = m.group(1);
		status.machinePosition = readPosFromMatcher(m, 2);
		status.workingPosition = readPosFromMatcher(m, 5);

		// TODO hardening this
		if (m.group(9) != null) {
			status.bufferSize = Integer.parseInt(m.group(9));
		}
		if (m.group(11) != null) {
			status.RXBuffer = Integer.parseInt(m.group(11));
		}
		return status;
	}

	public static GRBLStatus parse11(String s) throws Exception {
		Matcher m = PATTERN_STATUS11.matcher(s);
		if (!m.matches()) {
			throw new Exception("cannot parse status :" + s);
		}

		GRBLStatus status = new GRBLStatus();
		status.status = m.group(1);
		// no machine position
		status.machinePosition = readPosFromMatcher(m, 2);
		// only one status
		// status.workingPosition = readPosFromMatcher(m, 2);

		if (m.group(5) != null) {
			// TODO hardening this
			if (m.group(6) != null) {
				status.availableCommandsInPlannedBuffer = Integer.parseInt(m.group(6));
			}
			if (m.group(7) != null) {
				status.RXBuffer = Integer.parseInt(m.group(7));
			}

		}
		return status;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("S :").append(status).append(" wpos :").append(workingPosition).append(" buffer :").append(bufferSize)
				.append(", Rx Buffer :").append(RXBuffer);

		return sb.toString();
	}
}
