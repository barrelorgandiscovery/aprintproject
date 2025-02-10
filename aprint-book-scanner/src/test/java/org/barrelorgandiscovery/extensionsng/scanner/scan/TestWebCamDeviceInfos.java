package org.barrelorgandiscovery.extensionsng.scanner.scan;

import java.util.List;

import com.github.sarxos.webcam.Webcam;

public class TestWebCamDeviceInfos {

	
	public static void main() throws Exception {
		
		List<Webcam> webcams = Webcam.getWebcams();
		webcams.stream().forEach( (w) -> {
			System.out.println(w);
			System.out.println(w.getFPS());
			
		});
		
		System.out.println();
		
	}
	
}
