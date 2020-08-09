package org.barrelorgandiscovery.testjava;

import org.junit.Test;

/**
 * test for Java Type casting with base class
 * @author pfreydiere
 *
 */
public class TestCast {

	public static abstract class Base {

	}

	public static class Point extends Base {
	}

	@Test
	public void testCast() throws Exception {
		Point[] b = new Point[10];
		Base[] baseClassArrayCast = (Base[])b;
		System.out.println(baseClassArrayCast);
	}

}
