package org.barrelorgandiscovery.mcp;

import java.util.HashMap;
import java.util.Map;

/**
 * Result of a script execution on a VirtualBookFrame.
 * Encapsulates the execution result, output, errors, and timing information.
 * 
 * @author APrint Development Team
 */
public class ScriptExecutionResult {
	
	private final boolean success;
	private final Object result;
	private final String output;
	private final String error;
	private final long executionTime;
	
	public ScriptExecutionResult(boolean success, Object result, String output, String error) {
		this.success = success;
		this.result = result;
		this.output = output != null ? output : "";
		this.error = error != null ? error : "";
		this.executionTime = System.currentTimeMillis();
	}
	
	public boolean isSuccess() {
		return success;
	}
	
	public Object getResult() {
		return result;
	}
	
	public String getOutput() {
		return output;
	}
	
	public String getError() {
		return error;
	}
	
	public long getExecutionTime() {
		return executionTime;
	}
	
	/**
	 * Convert to a Map for JSON serialization
	 */
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<>();
		map.put("success", success);
		map.put("result", result != null ? result.toString() : null);
		map.put("output", output);
		map.put("error", error);
		map.put("executionTime", executionTime);
		return map;
	}
}

