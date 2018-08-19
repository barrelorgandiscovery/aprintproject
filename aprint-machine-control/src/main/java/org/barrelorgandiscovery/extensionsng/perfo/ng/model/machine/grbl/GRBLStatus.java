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
  public Integer RXBuffer;

  private static final Pattern PATTERN_STATUS =
      Pattern.compile(
          "([^,]+),MPos:([^,]+),([^,]+),([^,]+),WPos:([^,]+),([^,]+),([^,]+)(,Buf:([^,]+))?(,RX:([^,]+))?");

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
      throw new Exception("cannot parse status :" + s);
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

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("S :")
        .append(status)
        .append(" wpos :")
        .append(workingPosition)
        .append(" buffer :")
        .append(bufferSize)
        .append(", Rx Buffer :")
        .append(RXBuffer);

    return sb.toString();
  }
}
