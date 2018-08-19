package org.barrelorgandiscovery.gui.aprint.extensions;

import java.beans.BeanInfo;

import javax.swing.JFrame;

import com.l2fprod.common.beans.BaseBeanInfo;
import com.l2fprod.common.beans.ExtendedPropertyDescriptor;
import com.l2fprod.common.demo.BeanBinder;
import com.l2fprod.common.model.DefaultBeanInfoResolver;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class TestBeanPropertyEditing {

	public static class Test {
		private String name = "";
		private int test = 5;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getTest() {
			return test;
		}

		public void setTest(int test) {
			this.test = test;
		}

	}

	public static class TestBeanInfo extends BaseBeanInfo {

		public TestBeanInfo() {
			super(Test.class);
			ExtendedPropertyDescriptor nameProperty = addProperty("name");
			nameProperty.setShortDescription("name of the name");
			nameProperty.setDisplayName("Nom");

			addProperty("test");
		}

	}

	public static void main(String[] args) {

		JFrame f = new JFrame();

		PropertySheetPanel p = new PropertySheetPanel();
		p.setDescriptionVisible(true);

		Test t = new Test();

		// To check if the bean info has been provided, if it returns null, no
		// bean info is provided
		BeanInfo bi = new DefaultBeanInfoResolver().getBeanInfo(t);

		new BeanBinder(t, p);

		f.getContentPane().add(p);

		f.setVisible(true);
	}

}
