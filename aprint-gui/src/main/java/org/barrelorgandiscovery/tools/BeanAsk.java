package org.barrelorgandiscovery.tools;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;

import javax.swing.JButton;
import javax.swing.JDialog;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.repository.Repository2;
import org.barrelorgandiscovery.scale.Scale;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFile;
import org.barrelorgandiscovery.virtualbook.transformation.importer.MidiFilePropertyEditor;

import com.l2fprod.common.demo.BeanBinder;
import com.l2fprod.common.propertysheet.Property;
import com.l2fprod.common.propertysheet.PropertyEditorRegistry;
import com.l2fprod.common.propertysheet.PropertyRendererRegistry;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

public class BeanAsk extends JDialog {

	private static Logger logger = Logger.getLogger(BeanAsk.class);

	public BeanAsk() throws HeadlessException {
		super();
	}

	public BeanAsk(Dialog owner, boolean modal) throws HeadlessException {
		super(owner, modal);
	}

	public BeanAsk(Dialog owner, String title, boolean modal,
			GraphicsConfiguration gc) throws HeadlessException {
		super(owner, title, modal, gc);
	}

	public BeanAsk(Dialog owner, String title, boolean modal)
			throws HeadlessException {
		super(owner, title, modal);
	}

	public BeanAsk(Dialog owner, String title) throws HeadlessException {
		super(owner, title);

	}

	public BeanAsk(Dialog owner) throws HeadlessException {
		super(owner);

	}

	public BeanAsk(Frame owner, boolean modal) throws HeadlessException {
		super(owner, modal);

	}

	public BeanAsk(Frame owner, String title, boolean modal,
			GraphicsConfiguration gc) {
		super(owner, title, modal, gc);

	}

	public BeanAsk(Frame owner, String title, boolean modal)
			throws HeadlessException {
		super(owner, title, modal);

	}

	public BeanAsk(Frame owner, String title) throws HeadlessException {
		super(owner, title);

	}

	public BeanAsk(Frame owner) throws HeadlessException {
		super(owner);

	}

	private Object result = null;

	public static Object askForParameters(Frame owner, String title,
			final Object parameterbean) throws Exception {

		ClassLoader cl = parameterbean.getClass().getClassLoader();

		BeanInfo bi;

		String beaninfoclassname = parameterbean.getClass().getName()
				+ "BeanInfo"; //$NON-NLS-1$

		logger.debug("loading class " + beaninfoclassname //$NON-NLS-1$
				+ " in the extension classloader"); //$NON-NLS-1$
		try {

			bi = (BeanInfo) cl.loadClass(beaninfoclassname).newInstance();

		} catch (Exception ex) {
			logger.error("no beaninfo for parameter bean " //$NON-NLS-1$
					+ parameterbean.toString(), ex);
			throw new Exception("no parameter beaninfo for parameters", ex); //$NON-NLS-1$
		}

		return askForParameters(owner, title, parameterbean, bi);

	}

	/**
	 * Ask for properties, contained in a property array
	 * 
	 * @param owner
	 * @param title
	 * @param inputs
	 * @return
	 * @throws Exception
	 */
	public static Property[] askForProperties(Frame owner, String title,
			Property[] inputs, Repository2 repository) throws Exception {
		
		final BeanAsk parameterDialog = new BeanAsk(owner, title);

		final PropertySheetPanel p = createPropertySheetPanel(repository);

		p.setProperties(inputs);

		Container contentPane = parameterDialog.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(p, BorderLayout.CENTER);

		JButton ok = new JButton(Messages.getString("APrint.240")); //$NON-NLS-1$
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				parameterDialog.result = p.getProperties();

				parameterDialog.setVisible(false);
				logger.debug(" - OK parameters modified - "); //$NON-NLS-1$
				parameterDialog.dispose();
			}
		});

		contentPane.add(ok, BorderLayout.SOUTH);
		parameterDialog.setSize(400, 300);
		SwingUtils.center(parameterDialog);

		parameterDialog.setModal(true);
		parameterDialog.setVisible(true);

		return (Property[]) parameterDialog.result;
	}

	/**
	 * Ask for Object values
	 * 
	 * @param owner
	 * @param title
	 * @param parameterbean
	 * @param bi
	 * @return
	 * @throws Exception
	 */
	public static Object askForParameters(Frame owner, String title,
			final Object parameterbean, BeanInfo bi) throws Exception {
		// test the bean info
		// To check if the bean info has been provided, if it
		// returns null,
		// no
		// bean info is provided

		// looking for the beaninfo associated to the parameters
		// ...

		ClassLoader cl = parameterbean.getClass().getClassLoader();

		if (bi == null) {
			throw new Exception("no beaninfo for parameter bean " //$NON-NLS-1$
					+ parameterbean.toString());
		}

		final BeanAsk parameterDialog = new BeanAsk(owner, title);

		PropertySheetPanel p = createPropertySheetPanel(null);

		new BeanBinder(parameterbean, p, bi);

		Container contentPane = parameterDialog.getContentPane();
		contentPane.setLayout(new BorderLayout());
		contentPane.add(p, BorderLayout.CENTER);

		JButton ok = new JButton(Messages.getString("APrint.240")); //$NON-NLS-1$
		ok.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				parameterDialog.result = parameterbean;

				parameterDialog.setVisible(false);
				logger.debug(" - OK parameters modified - "); //$NON-NLS-1$
				parameterDialog.dispose();
			}
		});

		contentPane.add(ok, BorderLayout.SOUTH);
		parameterDialog.setSize(400, 300);
		SwingUtils.center(parameterDialog);

		parameterDialog.setModal(true);
		parameterDialog.setVisible(true);

		return parameterDialog.result;
	}

	/**
	 * register properties editor
	 * 
	 * @return
	 */
	private static PropertySheetPanel createPropertySheetPanel(
			Repository2 services) {
		PropertySheetPanel p = new PropertySheetPanel();
		p.setDescriptionVisible(true);
		p.setSortingCategories(true);

		PropertyEditorRegistry pr = new PropertyEditorRegistry();
		pr.registerDefaults();
		pr.registerEditor(MidiFile.class, MidiFilePropertyEditor.class);

		PropertyRendererRegistry prr = new PropertyRendererRegistry();
		prr.registerDefaults();
		prr.registerRenderer(Image.class, ImageCellRenderer.class);

		if (services != null) {
			pr.registerEditor(Scale.class, new ScaleChooserPropertyEditor(
					services));
		}

		p.setEditorFactory(pr);
		p.setRendererFactory(prr);
		return p;
	}

}
