package org.barrelorgandiscovery.testregression;

import java.util.Base64;
import java.util.Base64.Decoder;

import org.junit.jupiter.api.Test;


public class TestBase64Decoder {

	@Test
	public void testDecoder() throws Exception {

		String s = "IyBHYW1tZSBGaWxlIDQ5IExpbW9uYWlyZSAtIDI3IGTDqWMuIDIwMjAKdmVyc2lvbj02CiMgR2Ft\r\n"
				+ "bWUgZGVzY3JpcHRpb24gCndpZHRoPTIwMC4wCm5hbWU9NDkgTGltb25haXJlCmludGVydHJhY2s9\r\n"
				+ "My41CmZpcnN0dHJhY2tvZmZzZXQ9MTYuMApwZXJmb3JhdGlvbndpZHRoPTQuMApzcGVlZD02MC4w\r\n"
				+ "CnRyYWNrbnVtYmVyPTQ5CmluZm9zPW51bGwKc3RhdGU9SU5QUk9HUkVTUwpwcmVmZXJyZWR2aWV3\r\n"
				+ "ZWRpbnZlcnRlZD10cnVlCnJlbmRlcmluZz1kZWZhdWx0CgoKCiMgcmVnaXN0ZXJzIGRlc2NyaXB0\r\n"
				+ "aW9uIApwaXBlc3RvcGdyb3VwY291bnQ9NQpwaXBlc3RvcGdyb3VwLjA9QkFTU0UKcGlwZXN0b3Bk\r\n"
				+ "ZWZuYW1lLjA9Qk9VUkRPTgpwaXBlc3RvcGRlZnJlZ2lzdGVyY29udHJvbGxlZC4wPWZhbHNlCnBp\r\n"
				+ "cGVzdG9wZGVmbmFtZS4wPVRST01CT05FCnBpcGVzdG9wZGVmcmVnaXN0ZXJjb250cm9sbGVkLjA9\r\n"
				+ "ZmFsc2UKcGlwZXN0b3Bncm91cC4xPUFDQ09NUEFHTkVNRU5UCnBpcGVzdG9wZ3JvdXAuMj1DSEFO\r\n"
				+ "VApwaXBlc3RvcGRlZm5hbWUuMj1DTEFSSU5FVFRFCnBpcGVzdG9wZGVmcmVnaXN0ZXJjb250cm9s\r\n"
				+ "bGVkLjI9ZmFsc2UKcGlwZXN0b3Bncm91cC4zPUNPTlRSRUNIQU1QCnBpcGVzdG9wZGVmbmFtZS4z\r\n"
				+ "PUZMVVRFCnBpcGVzdG9wZGVmcmVnaXN0ZXJjb250cm9sbGVkLjM9ZmFsc2UKcGlwZXN0b3Bncm91\r\n"
				+ "cC40PUNIQU5UMwpwaXBlc3RvcGRlZm5hbWUuND1UUk9NUEVUVEUKcGlwZXN0b3BkZWZyZWdpc3Rl\r\n"
				+ "cmNvbnRyb2xsZWQuND1mYWxzZQojIHByb3BlcnRpZXMgCiMgdHJhY2sgZGVzY3JpcHRpb24gCnJl\r\n"
				+ "Z2lzdGVyc2V0PUJBU1NFCjA9RzMKMT1DMwoyPUQzCjM9RjMKND1wNDksNC4wLDQuMApyZWdpc3Rl\r\n"
				+ "cnNldD1BQ0NPTVBBR05FTUVOVAo1PUczCjY9QTMKNz1CMwo4PUM0Cjk9RDQKMTA9RTQKMTE9RjQK\r\n"
				+ "MTI9RiM0CjEzPUc0CnJlZ2lzdGVyc2V0PUNIQU5UCjE0PUc0CjE1PUE0CjE2PXAzOCw0LjAsNC4w\r\n"
				+ "CjE3PUI0CjE4PUM1CjE5PUMjNQoyMD1ENQoyMT1FNQoyMj1GNQoyMz1GIzUKMjQ9RzUKMjU9QTUK\r\n"
				+ "MjY9QjUKMjc9QzYKMjg9cDQwLDQuMCw0LjAKcmVnaXN0ZXJzZXQ9Q09OVFJFQ0hBTVAKMjk9RzQK\r\n"
				+ "MzA9QTQKMzE9QjQKMzI9QzUKMzM9QyM1CjM0PUQ1CjM1PUU1CjM2PUY1CjM3PUYjNQozOD1HNQpy\r\n"
				+ "ZWdpc3RlcnNldD1DSEFOVDMKMzk9RzQKNDA9QTQKNDE9QjQKNDI9QzUKNDM9QyM1CjQ0PUQ1CjQ1\r\n"
				+ "PUU1CjQ2PUY1CjQ3PUYjNQo0OD1HNQo=";

		Decoder decoder = Base64.getMimeDecoder();
		byte[] result = decoder.decode(s);
		System.out.println(new String(result, "UTF-8"));
	}

}
