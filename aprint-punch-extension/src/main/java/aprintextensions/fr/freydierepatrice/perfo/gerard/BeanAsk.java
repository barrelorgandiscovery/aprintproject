package aprintextensions.fr.freydierepatrice.perfo.gerard;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GraphicsConfiguration;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.BeanInfo;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;

import org.apache.log4j.Logger;

import com.l2fprod.common.demo.BeanBinder;
import com.l2fprod.common.propertysheet.PropertySheetPanel;

import org.barrelorgandiscovery.gui.aprint.APrint;
import org.barrelorgandiscovery.messages.Messages;
import org.barrelorgandiscovery.tools.SwingUtils;

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
		// test the bean info
		// To check if the bean info has been provided, if it
		// returns null,
		// no
		// bean info is provided

		// looking for the beaninfo associated to the parameters
		// ...

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

		if (bi == null) {
			throw new Exception("no beaninfo for parameter bean " //$NON-NLS-1$
					+ parameterbean.toString());
		}

		final BeanAsk parameterDialog = new BeanAsk(owner, title);

		PropertySheetPanel p = new PropertySheetPanel();
		p.setDescriptionVisible(true);
		p.setSortingCategories(true);

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
		parameterDialog.setSize(400, 600);
		SwingUtils.center(parameterDialog);

		parameterDialog.setModal(true);
		parameterDialog.setVisible(true);

		return parameterDialog.result;
	}

}
