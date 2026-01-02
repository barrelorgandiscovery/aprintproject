package org.barrelorgandiscovery.gui.aprintng;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.awt.Dialog.ModalityType;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;
import org.barrelorgandiscovery.tools.JMessageBox;

/**
 * Dialog for managing scripts with a better UI for CRUD operations.
 * Provides a list view with actions for Load, Save, Delete, and Rename.
 * 
 * @author Enhanced script management UI
 */
public class ScriptManagerDialog extends JDialog {
    
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(ScriptManagerDialog.class);
    
    private QuickScriptManager scriptManager;
    private JList<String> scriptList;
    private DefaultListModel<String> listModel;
    private JTextField scriptNameField;
    private JButton loadButton;
    private JButton saveButton;
    private JButton deleteButton;
    private JButton renameButton;
    private JButton duplicateButton;
    private JButton refreshButton;
    
    private String selectedScript;
    private ScriptManagerCallback callback;
    
    /**
     * Callback interface for script operations
     */
    public interface ScriptManagerCallback {
        /**
         * Called when a script should be loaded
         * @param scriptName The name of the script to load
         */
        void onLoadScript(String scriptName);
        
        /**
         * Called when a script should be saved
         * @param scriptName The name of the script to save
         * @return true if save was successful
         */
        boolean onSaveScript(String scriptName);
        
        /**
         * Get the current script content for saving
         * @return The script content
         */
        String getCurrentScriptContent();
        
        /**
         * Get the currently edited script name (if any)
         * @return The current script name or null
         */
        String getCurrentScriptName();
    }
    
    /**
     * Constructor
     */
    public ScriptManagerDialog(Window owner, QuickScriptManager scriptManager, 
            ScriptManagerCallback callback) {
        super(owner, "Script Manager", ModalityType.APPLICATION_MODAL);
        this.scriptManager = scriptManager;
        this.callback = callback;
        
        initComponents();
        refreshScriptList();
        updateButtonStates();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        
        // Main panel with border
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Script list panel
        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(new TitledBorder("Available Scripts"));
        
        listModel = new DefaultListModel<>();
        scriptList = new JList<>(listModel);
        scriptList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        scriptList.setVisibleRowCount(12);
        
        // Double-click to load
        scriptList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    loadSelectedScript();
                }
            }
        });
        
        // Selection listener
        scriptList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    updateSelectedScript();
                    updateButtonStates();
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(scriptList);
        scrollPane.setPreferredSize(new Dimension(300, 300));
        listPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Script name input panel
        JPanel namePanel = new JPanel(new BorderLayout(5, 5));
        namePanel.setBorder(new TitledBorder("Script Name"));
        scriptNameField = new JTextField();
        scriptNameField.setToolTipText("Enter script name (without extension)");
        namePanel.add(scriptNameField, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 5));
        
        // Toolbar with actions
        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.setBorderPainted(false);
        
        loadButton = new JButton("Load");
        loadButton.setIcon(new ImageIcon(getClass().getResource("tool_dock.png")));
        loadButton.setToolTipText("Load the selected script into the editor");
        loadButton.addActionListener(e -> loadSelectedScript());
        toolbar.add(loadButton);
        
        toolbar.addSeparator();
        
        saveButton = new JButton("Save");
        saveButton.setIcon(new ImageIcon(getClass().getResource("filesave.png")));
        saveButton.setToolTipText("Save current script content with the entered name");
        saveButton.addActionListener(e -> saveScript());
        toolbar.add(saveButton);
        
        toolbar.addSeparator();
        
        renameButton = new JButton("Rename");
        renameButton.setToolTipText("Rename the selected script");
        renameButton.addActionListener(e -> renameScript());
        toolbar.add(renameButton);
        
        duplicateButton = new JButton("Duplicate");
        duplicateButton.setToolTipText("Create a copy of the selected script");
        duplicateButton.addActionListener(e -> duplicateScript());
        toolbar.add(duplicateButton);
        
        toolbar.addSeparator();
        
        deleteButton = new JButton("Delete");
        deleteButton.setIcon(new ImageIcon(getClass().getResource("stop.png")));
        deleteButton.setToolTipText("Delete the selected script");
        deleteButton.addActionListener(e -> deleteSelectedScript());
        toolbar.add(deleteButton);
        
        toolbar.addSeparator();
        
        refreshButton = new JButton("Refresh");
        refreshButton.setToolTipText("Refresh the script list");
        refreshButton.addActionListener(e -> refreshScriptList());
        toolbar.add(refreshButton);
        
        buttonsPanel.add(toolbar);
        
        // Status label
        JLabel statusLabel = new JLabel(" ");
        statusLabel.setBorder(new EmptyBorder(5, 5, 5, 5));
        
        // Layout
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.add(listPanel, BorderLayout.CENTER);
        centerPanel.add(namePanel, BorderLayout.SOUTH);
        
        mainPanel.add(centerPanel, BorderLayout.CENTER);
        mainPanel.add(buttonsPanel, BorderLayout.NORTH);
        mainPanel.add(statusLabel, BorderLayout.SOUTH);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Close button panel
        JPanel closePanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dispose());
        closePanel.add(closeButton);
        add(closePanel, BorderLayout.SOUTH);
        
        pack();
        setLocationRelativeTo(getOwner());
        setMinimumSize(new Dimension(400, 450));
    }
    
    /**
     * Refresh the script list from the script manager
     */
    private void refreshScriptList() {
        try {
            String[] scripts = scriptManager.listQuickScripts();
            listModel.clear();
            for (String script : scripts) {
                listModel.addElement(script);
            }
            
            // Select current script if callback provides it
            if (callback != null) {
                String currentScript = callback.getCurrentScriptName();
                if (currentScript != null) {
                    int index = listModel.indexOf(currentScript);
                    if (index >= 0) {
                        scriptList.setSelectedIndex(index);
                        scriptNameField.setText(currentScript);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error refreshing script list", e);
            JMessageBox.showMessage(this, "Error loading scripts: " + e.getMessage());
        }
    }
    
    /**
     * Update selected script from list selection
     */
    private void updateSelectedScript() {
        selectedScript = scriptList.getSelectedValue();
        if (selectedScript != null) {
            scriptNameField.setText(selectedScript);
        }
    }
    
    /**
     * Update button states based on selection
     */
    private void updateButtonStates() {
        boolean hasSelection = selectedScript != null;
        boolean hasName = scriptNameField.getText().trim().length() > 0;
        
        loadButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
        renameButton.setEnabled(hasSelection);
        duplicateButton.setEnabled(hasSelection);
        saveButton.setEnabled(hasName && callback != null);
    }
    
    /**
     * Load the selected script
     */
    private void loadSelectedScript() {
        if (selectedScript == null) {
            JMessageBox.showMessage(this, "Please select a script to load");
            return;
        }
        
        if (callback != null) {
            callback.onLoadScript(selectedScript);
            dispose();
        }
    }
    
    /**
     * Save the current script content
     */
    private void saveScript() {
        String scriptName = scriptNameField.getText().trim();
        if (scriptName.isEmpty()) {
            JMessageBox.showMessage(this, "Please enter a script name");
            return;
        }
        
        if (callback != null) {
            boolean success = callback.onSaveScript(scriptName);
            if (success) {
                refreshScriptList();
                // Select the saved script
                int index = listModel.indexOf(scriptName);
                if (index >= 0) {
                    scriptList.setSelectedIndex(index);
                }
                JOptionPane.showMessageDialog(this, "Script saved: " + scriptName);
            }
        }
    }
    
    /**
     * Delete the selected script
     */
    private void deleteSelectedScript() {
        if (selectedScript == null) {
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to delete the script '" + selectedScript + "'?",
            "Confirm Delete",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                String scriptToDelete = selectedScript;
                scriptManager.deleteScript(scriptToDelete);
                refreshScriptList();
                scriptNameField.setText("");
                selectedScript = null;
                JOptionPane.showMessageDialog(this, "Script deleted: " + scriptToDelete);
            } catch (Exception e) {
                logger.error("Error deleting script", e);
                JMessageBox.showMessage(this, "Error deleting script: " + e.getMessage());
            }
        }
    }
    
    /**
     * Rename the selected script
     */
    private void renameScript() {
        if (selectedScript == null) {
            JMessageBox.showMessage(this, "Please select a script to rename");
            return;
        }
        
        String newName = JOptionPane.showInputDialog(
            this,
            "Enter new name for script:",
            "Rename Script",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (newName != null && !newName.trim().isEmpty()) {
            newName = newName.trim();
            try {
                // Load old script
                StringBuffer content = scriptManager.loadScript(selectedScript);
                
                // Save with new name
                scriptManager.saveScript(newName, content);
                
                // Delete old script
                scriptManager.deleteScript(selectedScript);
                
                refreshScriptList();
                
                // Select the renamed script
                int index = listModel.indexOf(newName);
                if (index >= 0) {
                    scriptList.setSelectedIndex(index);
                }
                
                JOptionPane.showMessageDialog(this, "Script renamed to: " + newName);
            } catch (Exception e) {
                logger.error("Error renaming script", e);
                JMessageBox.showMessage(this, "Error renaming script: " + e.getMessage());
            }
        }
    }
    
    /**
     * Duplicate the selected script
     */
    private void duplicateScript() {
        if (selectedScript == null) {
            JMessageBox.showMessage(this, "Please select a script to duplicate");
            return;
        }
        
        String newName = JOptionPane.showInputDialog(
            this,
            "Enter name for the copy:",
            "Duplicate Script",
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (newName != null && !newName.trim().isEmpty()) {
            newName = newName.trim();
            try {
                // Load original script
                StringBuffer content = scriptManager.loadScript(selectedScript);
                
                // Save as new script
                scriptManager.saveScript(newName, content);
                
                refreshScriptList();
                
                // Select the new script
                int index = listModel.indexOf(newName);
                if (index >= 0) {
                    scriptList.setSelectedIndex(index);
                }
                
                JOptionPane.showMessageDialog(this, "Script duplicated as: " + newName);
            } catch (Exception e) {
                logger.error("Error duplicating script", e);
                JMessageBox.showMessage(this, "Error duplicating script: " + e.getMessage());
            }
        }
    }
}

