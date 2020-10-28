package org.barrelorgandiscovery.gui.aprint.extensions;

import javax.swing.JFrame;

import org.barrelorgandiscovery.gui.aprintng.VirtualBookMetadataBeanInfo;
import org.barrelorgandiscovery.virtualbook.VirtualBookMetadata;

import com.l2fprod.common.demo.BeanBinder;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class TestBeanMetadata {

	public static void main(String[] args) throws Exception {

		JFrame f = new JFrame();
		PropertySheetPanel p = new PropertySheetPanel();
		f.getContentPane().add(p);
		f.setSize(400, 400);

		
		VirtualBookMetadata m = new VirtualBookMetadata();
		m.setArranger("testarranger");
		new BeanBinder(m, p, new VirtualBookMetadataBeanInfo());

		f.setVisible(true);
	}
}
