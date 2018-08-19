package org.barrelorgandiscovery.model.steps.scripts;

import java.awt.BorderLayout;

import org.barrelorgandiscovery.gui.etl.JConfigurePanel;
import org.barrelorgandiscovery.tools.JMessageBox;

import groovy.ui.ConsoleTextEditor;

/**
 * UI for modifying a groovy script
 * 
 * @author pfreydiere
 *
 */
public class GroovyScriptModelUI extends JConfigurePanel {

	private GroovyScriptModelStep groovyScriptModelStep;
	private ConsoleTextEditor cte;

	public GroovyScriptModelUI(GroovyScriptModelStep s) {
		this.groovyScriptModelStep = s;
		initComponents();

	}

	protected void initComponents() {

		// create editor panel
		cte = new ConsoleTextEditor();
		cte.getTextEditor().setText(groovyScriptModelStep.getScriptContent());

		setLayout(new BorderLayout()); 
		add(cte,BorderLayout.CENTER);
		
	}

	@Override
	public boolean apply() throws Exception {
		String content = cte.getTextEditor().getText();
		groovyScriptModelStep.setScriptContent(content);
		try {
			if (content != null && !content.trim().isEmpty())
			{
				groovyScriptModelStep.compileScript();
			}
		} catch(Exception ex) {
			JMessageBox.showError(this, ex);
			return false;
		}
		return true;
	}

}
