package org.barrelorgandiscovery.testjava;

import org.junit.jupiter.api.Test;

/**
 * test for Java Type casting with base class used with childfirst classloader
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
