package org.barrelorgandiscovery.gui.script.groovy;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import java.awt.BorderLayout;

/**
 * Replacement for groovy.ui.ConsoleTextEditor which was removed in Groovy 4.0.
 * This is a simple wrapper around JTextPane in a JScrollPane.
 * 
 * @author Migration from Groovy 1.7.3 to 4.0
 */
public class ConsoleTextEditor extends JPanel {
    
    private static final long serialVersionUID = 1L;
    private JTextPane textEditor;
    
    public ConsoleTextEditor() {
        setLayout(new BorderLayout());
        textEditor = new JTextPane();
        JScrollPane scrollPane = new JScrollPane(textEditor);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(scrollPane, BorderLayout.CENTER);
    }
    
    /**
     * Get the text editor component
     * @return the JTextPane
     */
    public JTextPane getTextEditor() {
        return textEditor;
    }
}

