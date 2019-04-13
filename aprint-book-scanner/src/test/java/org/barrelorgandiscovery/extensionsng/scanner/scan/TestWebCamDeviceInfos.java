package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.util.List;

import org.junit.Test;

import com.github.sarxos.webcam.Webcam;

public class TestWebCamDeviceInfos {

	
	@Test
	public void testDisplayWebCam() throws Exception {
		
		List<Webcam> webcams = Webcam.getWebcams();
		webcams.stream().forEach( (w) -> {
			System.out.println(w);
			System.out.println(w.getFPS());
			
		});
		
		System.out.println();
		
		
	}
	
}
