package org.barrelorgandiscovery.gui.script.groovy;

import groovy.lang.Binding;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;

import org.apache.log4j.Logger;

/**
 * Replacement for groovy.ui.ConsoleTextEditor which was removed in Groovy 4.0.
 * This wrapper uses EnhancedConsoleTextEditor (with syntax highlighting) if available,
 * otherwise falls back to a simple JTextPane.
 * 
 * @author Migration from Groovy 1.7.3 to 4.0
 * @author Enhanced with syntax highlighting support
 */
public class ConsoleTextEditor extends JPanel {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ConsoleTextEditor.class);
    
    private JTextComponent textEditor;
    private EnhancedConsoleTextEditor enhancedEditor;
    private boolean useEnhancedEditor;
    
    /**
     * Constructor - uses enhanced editor if available
     */
    public ConsoleTextEditor() {
        this(null);
    }
    
    /**
     * Constructor with binding for autocompletion
     * @param binding The Groovy binding for autocompletion (can be null)
     */
    public ConsoleTextEditor(Binding binding) {
        setLayout(new BorderLayout());
        
        // Try to use enhanced editor with syntax highlighting
        try {
            enhancedEditor = new EnhancedConsoleTextEditor(binding);
            textEditor = enhancedEditor.getTextComponent();
            useEnhancedEditor = true;
            add(enhancedEditor, BorderLayout.CENTER);
            logger.debug("Using enhanced console editor with syntax highlighting");
        } catch (NoClassDefFoundError | ExceptionInInitializerError e) {
            // RSyntaxTextArea not available, fall back to simple editor
            logger.debug("RSyntaxTextArea not available, using simple JTextPane", e);
            useEnhancedEditor = false;
            textEditor = new JTextPane();
            JScrollPane scrollPane = new JScrollPane(textEditor);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            add(scrollPane, BorderLayout.CENTER);
        }
    }
    
    /**
     * Update the binding for autocompletion (only works with enhanced editor)
     */
    public void updateBinding(Binding binding) {
        if (useEnhancedEditor && enhancedEditor != null) {
            enhancedEditor.updateBinding(binding);
        }
    }
    
    /**
     * Get the text editor component
     * @return the JTextComponent (JTextPane or RSyntaxTextArea)
     */
    public JTextComponent getTextEditor() {
        return textEditor;
    }
    
    /**
     * Check if enhanced editor is being used
     * @return true if using enhanced editor with syntax highlighting
     */
    public boolean isEnhanced() {
        return useEnhancedEditor;
    }
}

