package org.barrelorgandiscovery.extensionsng.perfo.ng.model.plan;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.barrelorgandiscovery.extensionsng.perfo.ng.model.machine.grbl.GRBLStatus;
import org.junit.Test;

public class TestGRBL11StatusParsing {

	
	@Test
	public void test() throws Exception {

		// Run|MPos:0.000,16.276,0.000|Bf:14,31|FS:500,0
		Pattern p = Pattern
				.compile("([^|]+)\\|MPos(.*)");
		//|MPos:([^,|]+),([^,|]+),([^,|]+)
		String s =  "Run|MPos:0.000,16.276,0.000";
		Matcher m = p.matcher(s);
		
		m.matches();
		for (int i = 1 ; i < m.groupCount(); i ++) {
			System.out.println("group " + i + " :" + m.group(i));
		}
		
	}
	
	@Test
	public void testStatusParsing() throws Exception {
		String s =  "Run|MPos:0.000,16.276,0.000|Bf:14,31|FS:500,0";
		GRBLStatus status = GRBLStatus.parse11(s);
		
		System.out.println(status);
		
	}
	
}