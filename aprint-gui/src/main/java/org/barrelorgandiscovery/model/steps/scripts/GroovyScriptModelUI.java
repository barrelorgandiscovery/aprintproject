package org.barrelorgandiscovery.model.steps.scripts;

import java.awt.BorderLayout;
import java.io.FileInputStream;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultEditorKit.CopyAction;
import javax.swing.text.DefaultEditorKit.CutAction;
import javax.swing.text.DefaultEditorKit.PasteAction;

import org.apache.commons.vfs2.provider.AbstractFileObject;
import org.barrelorgandiscovery.gui.etl.JConfigurePanel;
import org.barrelorgandiscovery.gui.tools.APrintFileChooser;
import org.barrelorgandiscovery.gui.tools.VFSFileNameExtensionFilter;
import org.barrelorgandiscovery.tools.JMessageBox;
import org.barrelorgandiscovery.tools.StreamsTools;

import groovy.ui.ConsoleTextEditor;

/**
 * UI for modifying a groovy script
 * 
 * @author pfreydiere
 *
 */
public class GroovyScriptModelUI extends JConfigurePanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4562608083582707130L;
	
	private GroovyScriptModelStep groovyScriptModelStep;
	private ConsoleTextEditor cte;

	public GroovyScriptModelUI(GroovyScriptModelStep s) {
		this.groovyScriptModelStep = s;
		initComponents();

	}

	protected void initComponents() {

		// add menu for loading script from file
		setLayout(new BorderLayout());
		JMenuBar menu = new JMenuBar();
		add(menu, BorderLayout.NORTH);

		JMenu fileMenu = new JMenu("File");
		menu.add(fileMenu);

		JMenuItem newEmptyFromTemplate = new JMenuItem("New from basic template ..");
		fileMenu.add(newEmptyFromTemplate);
		newEmptyFromTemplate.addActionListener((e) -> {
			try {
				// default content
				String scriptContent = "import org.barrelorgandiscovery.model.steps.scripts.*;\n"
						+ "import org.barrelorgandiscovery.model.*;\n"
						+ "import org.barrelorgandiscovery.model.type.*;\n"
						+ "import org.barrelorgandiscovery.timed.*;\n"
						+ "import org.barrelorgandiscovery.virtualbook.*\n" + "import org.barrelorgandiscovery.xml.*\n"
						+ "\n" + "import java.io.File;\n" + "\n" + "class T extends ModelGroovyScript {\n" + "\n"
						+ "   def console\n" + "\n" + "   String getLabel() { \"Hello Script\" }" + "\n"
						+ "   // cette fonction est appelee par le model editor\n"
						+ "   // pour connaitre les parametres et leur type\n"
						+ "   ModelParameter[] configureParameters() {\n"
						+ "       [ newParameter(true,\"fichier book\",newJavaType(File.class)), \n"
						+ "         newParameter(false,\"book\",newJavaType(VirtualBook.class))]\n" + "   }\n" + "\n"
						+ "   Map execute(Map m) {\n"
						+ "       // m contient les valeures des parametres passes au processeur\n"
						+ "       console.println(\"hello :\")\n" + "       console.println(m)\n"
						+ "       // on retourne le parametre \"book\" contenant un objet Virtualbook\n"
						+ "       return [book:VirtualBookXmlIO.read(m[\"fichier book\"]).virtualBook]\n" + "   }\n"
						+ "\n" + "}\n" + "new T(console:out) // le script retourne une instanciation de la classe";
				cte.getTextEditor().setText(scriptContent);

			} catch (Exception ex) {
				JMessageBox.showError(this, ex);
			}

		});

		JMenuItem importItem = new JMenuItem("Import script from file ...");
		fileMenu.add(importItem);
		importItem.addActionListener((e) -> {
			try {

				APrintFileChooser choose = new APrintFileChooser();
				choose.setFileFilter(new VFSFileNameExtensionFilter("Script box", "scriptbox"));
				if (choose.showOpenDialog(this) == APrintFileChooser.APPROVE_OPTION) {

					AbstractFileObject selectedFile = choose.getSelectedFile();
					if (selectedFile != null) {
						String scriptContent = StreamsTools
								.fullyReadUTF8StringFromStream(selectedFile.getInputStream());
						cte.getTextEditor().setText(scriptContent);
					}
				}

			} catch (Exception ex) {
				JMessageBox.showError(this, ex);
			}
		});

		JMenu editMenu = new JMenu("Edit");
		menu.add(editMenu);

		CopyAction actCopy = new DefaultEditorKit.CopyAction();
		actCopy.putValue(Action.NAME, "Copy");
		editMenu.add(actCopy);

		CutAction actCut = new DefaultEditorKit.CutAction();
		actCut.putValue(Action.NAME, "Cut");
		editMenu.add(actCut);

		PasteAction actPaste = new DefaultEditorKit.PasteAction();
		actPaste.putValue(Action.NAME, "Paste");
		editMenu.add(actPaste);

		// create editor panel
		cte = new ConsoleTextEditor();

		cte.getTextEditor().setText(groovyScriptModelStep.getScriptContent());

		cte.getTextEditor().setComponentPopupMenu(createPopupMenu());

		add(cte, BorderLayout.CENTER);

	}

	/* Methode de construction du menu contextuel */
	private JPopupMenu createPopupMenu() {
		JPopupMenu popupMenu = new JPopupMenu();

		CopyAction actCopy = new DefaultEditorKit.CopyAction();
		actCopy.putValue(Action.NAME, "Copy");

		CutAction actCut = new DefaultEditorKit.CutAction();
		actCut.putValue(Action.NAME, "Cut");

		PasteAction actPaste = new DefaultEditorKit.PasteAction();
		actPaste.putValue(Action.NAME, "Paste");

		popupMenu.add(actCopy);
		popupMenu.add(actCut);
		popupMenu.add(actPaste);

		return popupMenu;
	}

	@Override
	public boolean apply() throws Exception {
		String content = cte.getTextEditor().getText();
		groovyScriptModelStep.setScriptContent(content);
		try {
			if (content != null && !content.trim().isEmpty()) {
				groovyScriptModelStep.compileScript();
				groovyScriptModelStep.evaluateScriptParameters();
			}
		} catch (Exception ex) {
			JMessageBox.showError(this, ex);
			return false;
		}
		return true;
	}

}
