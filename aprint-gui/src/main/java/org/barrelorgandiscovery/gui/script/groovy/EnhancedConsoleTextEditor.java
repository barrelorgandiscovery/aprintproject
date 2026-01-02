package org.barrelorgandiscovery.gui.script.groovy;

import groovy.lang.Binding;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.text.JTextComponent;

import org.apache.log4j.Logger;
import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;
import org.fife.ui.autocomplete.DefaultCompletionProvider;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.RTextScrollPane;

/**
 * Enhanced console text editor with Groovy syntax highlighting and autocompletion.
 * Uses RSyntaxTextArea for advanced editing features.
 * 
 * @author Enhanced version with syntax highlighting
 */
public class EnhancedConsoleTextEditor extends JPanel {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(EnhancedConsoleTextEditor.class);
    
    private RSyntaxTextArea textEditor;
    private AutoCompletion autoCompletion;
    private DefaultCompletionProvider completionProvider;
    
    /**
     * Constructor
     */
    public EnhancedConsoleTextEditor() {
        this(null);
    }
    
    /**
     * Constructor with binding for autocompletion
     * @param binding The Groovy binding to extract variable names for autocompletion
     */
    public EnhancedConsoleTextEditor(Binding binding) {
        setLayout(new BorderLayout());
        
        // Create RSyntaxTextArea with Groovy syntax highlighting
        textEditor = new RSyntaxTextArea();
        textEditor.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_GROOVY);
        textEditor.setCodeFoldingEnabled(true);
        textEditor.setAntiAliasingEnabled(true);
        textEditor.setAutoIndentEnabled(true);
        textEditor.setBracketMatchingEnabled(true);
        textEditor.setPaintMatchedBracketPair(true);
        textEditor.setPaintTabLines(true);
        textEditor.setTabsEmulated(true);
        textEditor.setTabSize(4);
        
        // Set a nice font
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 13);
        textEditor.setFont(font);
        
        // Try to load a theme (default if not available)
        try {
            Theme theme = Theme.load(getClass().getResourceAsStream(
                "/org/fife/ui/rsyntaxtextarea/themes/default.xml"));
            if (theme != null) {
                theme.apply(textEditor);
            }
        } catch (Exception e) {
            logger.debug("Could not load RSyntaxTextArea theme, using defaults", e);
            // Use default colors
            textEditor.setBackground(Color.WHITE);
            textEditor.setForeground(Color.BLACK);
        }
        
        // Create scroll pane with line numbers
        RTextScrollPane scrollPane = new RTextScrollPane(textEditor);
        scrollPane.setLineNumbersEnabled(true);
        scrollPane.setVerticalScrollBarPolicy(RTextScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(RTextScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Setup autocompletion
        setupAutocompletion(binding);
    }
    
    /**
     * Setup autocompletion with Groovy keywords and binding variables
     */
    private void setupAutocompletion(Binding binding) {
        completionProvider = new DefaultCompletionProvider();
        
        // Add Groovy keywords
        addGroovyKeywords(completionProvider);
        
        // Add common Groovy methods and classes
        addGroovyCommonCompletions(completionProvider);
        
        // Add binding variables if available
        if (binding != null) {
            addBindingVariables(completionProvider, binding);
        }
        
        // Create and configure auto-completion
        autoCompletion = new AutoCompletion(completionProvider);
        // Don't set trigger key - let it use default behavior (auto-activation on typing)
        autoCompletion.setAutoActivationEnabled(true);
        autoCompletion.setAutoActivationDelay(300); // 300ms delay
        autoCompletion.setShowDescWindow(true);
        autoCompletion.setParameterAssistanceEnabled(true);
        autoCompletion.install(textEditor);
    }
    
    /**
     * Add Groovy keywords to completion provider
     */
    private void addGroovyKeywords(DefaultCompletionProvider provider) {
        String[] keywords = {
            "as", "assert", "break", "case", "catch", "class", "const", "continue",
            "def", "default", "do", "else", "enum", "extends", "final", "finally",
            "for", "goto", "if", "implements", "import", "in", "instanceof",
            "interface", "new", "null", "package", "return", "static", "super",
            "switch", "this", "throw", "throws", "trait", "try", "var", "void",
            "while", "with", "true", "false"
        };
        
        for (String keyword : keywords) {
            provider.addCompletion(new BasicCompletion(provider, keyword, 
                "Groovy keyword: " + keyword));
        }
    }
    
    /**
     * Add common Groovy methods and classes
     */
    private void addGroovyCommonCompletions(DefaultCompletionProvider provider) {
        // Common Groovy methods
        provider.addCompletion(new BasicCompletion(provider, "println", 
            "println(Object obj)", "Prints an object to the console"));
        provider.addCompletion(new BasicCompletion(provider, "print", 
            "print(Object obj)", "Prints an object without newline"));
        provider.addCompletion(new BasicCompletion(provider, "each", 
            "each(Closure closure)", "Iterates over a collection"));
        provider.addCompletion(new BasicCompletion(provider, "collect", 
            "collect(Closure closure)", "Transforms a collection"));
        provider.addCompletion(new BasicCompletion(provider, "find", 
            "find(Closure closure)", "Finds first matching element"));
        provider.addCompletion(new BasicCompletion(provider, "findAll", 
            "findAll(Closure closure)", "Finds all matching elements"));
        provider.addCompletion(new BasicCompletion(provider, "grep", 
            "grep(Object filter)", "Filters collection"));
        provider.addCompletion(new BasicCompletion(provider, "inject", 
            "inject(Object initialValue, Closure closure)", "Reduces a collection"));
        provider.addCompletion(new BasicCompletion(provider, "sort", 
            "sort(Closure closure)", "Sorts a collection"));
        provider.addCompletion(new BasicCompletion(provider, "join", 
            "join(String separator)", "Joins collection elements"));
        
        // Common classes
        provider.addCompletion(new BasicCompletion(provider, "List", 
            "List", "Groovy list"));
        provider.addCompletion(new BasicCompletion(provider, "Map", 
            "Map", "Groovy map"));
        provider.addCompletion(new BasicCompletion(provider, "Set", 
            "Set", "Groovy set"));
        provider.addCompletion(new BasicCompletion(provider, "Range", 
            "Range", "Groovy range"));
        provider.addCompletion(new BasicCompletion(provider, "String", 
            "String", "String class"));
        provider.addCompletion(new BasicCompletion(provider, "Integer", 
            "Integer", "Integer class"));
        provider.addCompletion(new BasicCompletion(provider, "Double", 
            "Double", "Double class"));
        provider.addCompletion(new BasicCompletion(provider, "Boolean", 
            "Boolean", "Boolean class"));
        provider.addCompletion(new BasicCompletion(provider, "Date", 
            "Date", "Date class"));
        provider.addCompletion(new BasicCompletion(provider, "File", 
            "File", "File class"));
        provider.addCompletion(new BasicCompletion(provider, "Closure", 
            "Closure", "Closure class"));
    }
    
    /**
     * Add variables from the Groovy binding to autocompletion
     */
    @SuppressWarnings("unchecked")
    private void addBindingVariables(DefaultCompletionProvider provider, Binding binding) {
        try {
            Set<String> variableNames = binding.getVariables().keySet();
            for (String varName : variableNames) {
                Object value = binding.getVariable(varName);
                String description = "Variable: " + varName;
                if (value != null) {
                    description += " (" + value.getClass().getSimpleName() + ")";
                }
                provider.addCompletion(new BasicCompletion(provider, varName, description));
            }
        } catch (Exception e) {
            logger.debug("Error adding binding variables to autocompletion", e);
        }
    }
    
    /**
     * Update the binding for autocompletion (call this when binding changes)
     */
    public void updateBinding(Binding binding) {
        if (completionProvider != null && binding != null) {
            // Recreate autocompletion with updated binding
            setupAutocompletion(binding);
        }
    }
    
    /**
     * Get the text editor component
     * @return the RSyntaxTextArea
     */
    public RSyntaxTextArea getTextEditor() {
        return textEditor;
    }
    
    /**
     * Get the text editor as JTextComponent for compatibility
     * @return the text editor
     */
    public JTextComponent getTextComponent() {
        return textEditor;
    }
    
    /**
     * Set text content
     */
    public void setText(String text) {
        textEditor.setText(text);
    }
    
    /**
     * Get text content
     */
    public String getText() {
        return textEditor.getText();
    }
    
    /**
     * Set caret position
     */
    public void setCaretPosition(int position) {
        textEditor.setCaretPosition(position);
    }
    
    /**
     * Get caret position
     */
    public int getCaretPosition() {
        return textEditor.getCaretPosition();
    }
    
    /**
     * Set editable
     */
    public void setEditable(boolean editable) {
        textEditor.setEditable(editable);
    }
    
    /**
     * Is editable
     */
    public boolean isEditable() {
        return textEditor.isEditable();
    }
    
    /**
     * Set enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if (textEditor != null) {
            textEditor.setEnabled(enabled);
            textEditor.setEditable(enabled);
        }
    }
}

