package org.barrelorgandiscovery.perfo.gui;

import java.awt.BorderLayout;
import java.io.File;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.lf5.LF5Appender;
import org.barrelorgandiscovery.prefs.FilePrefsStorage;

import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.form.FormAccessor;

public class JParameter extends JPanel implements IPunchMachinePanelActivate, IPunchParameters {

	private static final String SPACEPROPERTY_PROPERTY = "spaceproperty";

	private static final String OFFSETNUM_PROPERTY = "offsetnum";

	/** */
	private static final long serialVersionUID = -2340992996233999205L;

	private static Logger logger = Logger.getLogger(JParameter.class);

	private JNumericValue spaceComponent;

	private JNumericValue numOffset;

	private Navigation navigation;

	public JParameter(Navigation navigation) throws Exception {
		this.navigation = navigation;
		initComponents();
	}

	protected void initComponents() throws Exception {
		FormPanel fp = new FormPanel(getClass().getResourceAsStream("parameters.jfrm"));

		logger.debug("reading preferences");
		FilePrefsStorage prefsStorage = new FilePrefsStorage(new File("punchperfs.properties"));
		prefsStorage.load();

		FormAccessor fa = fp.getFormAccessor();
		numOffset = new JNumericValue();
		numOffset.setValue(prefsStorage.getDoubleProperty(OFFSETNUM_PROPERTY, 0.0d));
		numOffset.setChangeListener((e) -> {
			try {
				prefsStorage.setDoubleProperty(OFFSETNUM_PROPERTY, numOffset.getValue());
				prefsStorage.save();
			} catch (Exception ex) {
				logger.error("error saving property " + ex.getMessage(), ex);
			}
		});

		spaceComponent = new JNumericValue();
		spaceComponent.setValue(prefsStorage.getDoubleProperty(SPACEPROPERTY_PROPERTY, 120.0));
		spaceComponent.setChangeListener((e) -> {
			try {
				prefsStorage.setDoubleProperty(SPACEPROPERTY_PROPERTY, spaceComponent.getValue());
				prefsStorage.save();
			} catch (Exception ex) {
				logger.error("error saving property " + ex.getMessage(), ex);
			}
		});

		
		
		fa.replaceBean("offsetcomponent", numOffset);
		fa.replaceBean("spacecomponent", spaceComponent);

		AbstractButton movepanel = fp.getButton("btnmachine");
		movepanel.setText("Move Machine");
		movepanel.setIcon(new ImageIcon(getClass().getResource("kcontrol.png")));
		movepanel.addActionListener((e) -> {
			navigation.navigateTo(JParameter.this, PunchScreen.MachineMove);
		});

		JLabel lbloffset = fp.getLabel("lbloffset");
		lbloffset.setText("Offset");

		JLabel lblspace = fp.getLabel("space");
		lblspace.setText("Inter book");

		JLabel lblmchinecontrol = fp.getLabel("machinecontrol");
		lblmchinecontrol.setText("Move");

		AbstractButton btnback = fp.getButton("btnback");
		btnback.setText("Back");
		btnback.setIcon(new ImageIcon(getClass().getResource("back.png")));
		btnback.addActionListener((e) -> {
			navigation.navigateTo(this, PunchScreen.Hello);
		});

		// to be read
		// numOffset.setValue(0.0);
		//spaceComponent.setValue(10.0);

		setLayout(new BorderLayout());
		add(fp, BorderLayout.CENTER);
	}

	@Override
	public void activate() {
	}

	@Override
	public double getOffset() {
		return numOffset.getValue();
	}

	@Override
	public double getSpace() {
		return spaceComponent.getValue();
	}

	// Test
	public static void main(String[] args) throws Exception {
		BasicConfigurator.configure(new LF5Appender());
		JFrame f = new JFrame();
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		f.setSize(480, 320);
		f.getContentPane().setLayout(new BorderLayout());
		Navigation n = new Navigation() {
			@Override
			public void navigateTo(IPunchMachinePanelActivate punchPanel, PunchScreen newScreen) {
				logger.debug(newScreen);
			}
		};
		f.getContentPane().add(new JParameter(n), BorderLayout.CENTER);
		f.setVisible(true);
	}

}
