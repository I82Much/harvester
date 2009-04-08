/*
 * PoliceView.java
 */

package Harvester;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.Timer;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableRowSorter;

/**
 * The application's main frame.
 */
public class PoliceView extends FrameView implements ListSelectionListener {
              
    // Handles right clicking in the JTable
    class PopupListener extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            showPopup(e);
        }
        @Override
        public void mouseReleased(MouseEvent e) {
            showPopup(e);
        }
        private void showPopup(MouseEvent e) {
          if (e.isPopupTrigger()) {
            popupMenu.show(e.getComponent(), e.getX(), e.getY());
          }
        }
    }
    
    class ClickableListener extends MouseAdapter {
        @Override
        public void mouseEntered(MouseEvent e) {
            e.getComponent().setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        @Override
        public void mouseExited(MouseEvent e) {
            e.getComponent().setCursor(Cursor.getDefaultCursor());
        }
    }
    
    
   
    
    
    // Handles right clicking in the JTable
    class DoubleClickListener extends MouseAdapter {
        
        public PoliceView view;
        public DoubleClickListener(PoliceView view) {
            this.view = view;
        }
        
        @Override
        public void mousePressed(MouseEvent e) {
            // Double clicked
            if (!e.isPopupTrigger() && e.getClickCount() == 2) {
                view.openFiles();
            }
        }
    }
    
    
    class TableRepainter implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent pce) {
            // They stopped trying to drag into it, reset the color of
            // the table
            if (pce.getNewValue() == null) {
                ((Component) pce.getSource()).setBackground(null);
            }
            
        }
    }
    
    
    
    
    class TableRightClickListener implements ActionListener {
        private PoliceView view;
        private PoliceModel model;
        private JTable table;
        
        public static final String EXTRACT_STRING = "Extract different bytes";
        public static final String DELETE_STRING = "Delete selected row(s)";
        public static final String LAUNCH_STRING = "Open file(s) in default program";
        
        public TableRightClickListener(PoliceView view, PoliceModel model, 
                                            JTable table) {
            this.view = view;
            this.model = model;
            this.table = table;
        }
        public void actionPerformed(ActionEvent e) {
            if (e.getActionCommand().equals(EXTRACT_STRING)) {
                view.extractNewBytes();
            }
            else if (e.getActionCommand().equals(DELETE_STRING)) {
                view.deleteRows();
            }
            else if (e.getActionCommand().equals(LAUNCH_STRING)) {
                view.openFiles();
            }
        }

    }
    
    
  
    
    public PoliceView(SingleFrameApplication app, PoliceModel model) {
        super(app);
        this.model = model;
        

        
        
        desktop = null;
        // Before more Desktop API is used, first check 
        // whether the API is supported by this particular 
        // virtual machine (VM) on this particular host.
        if (Desktop.isDesktopSupported()) {
            desktop = Desktop.getDesktop();
        }
        
        
        initComponents();
        
        popupMenu = new JPopupMenu();
        JMenuItem extractBytes = new JMenuItem(TableRightClickListener.EXTRACT_STRING);
        JMenuItem launchFile = new JMenuItem(TableRightClickListener.LAUNCH_STRING);
        
        
        TableRightClickListener rightClicker =
                    new TableRightClickListener(this, model, displayTable);
        extractBytes.addActionListener(rightClicker);
        JMenuItem deleteRows = new JMenuItem(TableRightClickListener.DELETE_STRING);
        deleteRows.addActionListener(rightClicker);
        
        launchFile.addActionListener(rightClicker);
        
        popupMenu.add(extractBytes);
        popupMenu.add(launchFile);
        
        popupMenu.add(deleteRows);
        
        // Handle right clicks in JTable
        displayTable.addMouseListener(new PopupListener());
        
        
        popup = new JPopupMenu();

        ActionListener actionListener = new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                //System.out.println("Selected: " + actionEvent.getActionCommand());
            }
        };

       // Cut
        JMenuItem cutMenuItem = new JMenuItem("Cut");
        cutMenuItem.addActionListener(actionListener);
        popup.add(cutMenuItem);

        // Copy
        JMenuItem copyMenuItem = new JMenuItem("Copy");
        copyMenuItem.addActionListener(actionListener);
        popup.add(copyMenuItem);

        // Paste
        JMenuItem pasteMenuItem = new JMenuItem("Paste");
        pasteMenuItem.addActionListener(actionListener);
        pasteMenuItem.setEnabled(false);
        popup.add(pasteMenuItem);

        
        
        
        
        // If we have the capability to open files, add a double click listener;
        // double clicking on a file will make it open
        if (desktop != null && desktop.isSupported(Desktop.Action.OPEN)) {
            displayTable.addMouseListener(new DoubleClickListener(this));
        }
        // We do not, so gray out the option to do so in right click menu
        else {
            launchFile.setEnabled(false);
        }
        
        displayTable.getSelectionModel().addListSelectionListener(this);
        
        
        fixButtonStatuses();
        
        loadFileChooser = new JFileChooser();
        loadFileChooser.setFileFilter(new ImageFilter());
        loadFileChooser.setFileFilter(new VideoFilter());
        loadFileChooser.setMultiSelectionEnabled(true);    
        // Allow user to choose both files and directories
        loadFileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); 
        
        saveFileChooser = new JFileChooser();
        saveFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        saveFileChooser.setFileFilter(new TextFilter());
        
        
        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        progressBar.setVisible(true);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(false);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String)(evt.getNewValue());
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer)(evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });
        mainPanel.setTransferHandler(new DragAndDropHandler(model, this, displayTable));

        addTableShortcuts();
        
    }
    
    
    /**
     * Sets up the JTable to listen to keyboard strokes.
     * Enter launches files (like double clicking does) and hitting delete
     * or backspace deletes the selected rows
     */
    private void addTableShortcuts() {
        InputMap map = displayTable.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER,0), OPEN_STRING);
        map.put(KeyStroke.getKeyStroke("control D"), DELETE_STRING); 
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE,0), DELETE_STRING); 
        map.put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE,0), DELETE_STRING); 
        
        displayTable.getActionMap().put(OPEN_STRING, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                openFiles();
            }
        });
        displayTable.getActionMap().put(DELETE_STRING, new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                deleteRows();
            }
        });
    }
    

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = PoliceApp.getApplication().getMainFrame();
            aboutBox = new PoliceAboutBox(mainFrame, desktop);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        PoliceApp.getApplication().show(aboutBox);
    }
    
    @Action
    public void showOptionsBox() {
        if (options == null) {
            JFrame mainFrame = PoliceApp.getApplication().getMainFrame();
            //options = new PoliceOptionsBox(mainFrame);
            options.setLocationRelativeTo(mainFrame);
        }
        PoliceApp.getApplication().show(options);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        displayTable = new javax.swing.JTable();
        jPanel2 = new javax.swing.JPanel();
        MSPCCUIcon = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        addressLine1 = new javax.swing.JLabel();
        addressLine2 = new javax.swing.JLabel();
        phoneLabel = new javax.swing.JLabel();
        faxLabel = new javax.swing.JLabel();
        dirigoStatePoliceLogo = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem3 = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        buttonPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        add = new javax.swing.JButton();
        delete = new javax.swing.JButton();
        save = new javax.swing.JButton();
        saveAll = new javax.swing.JButton();
        progressBar = new javax.swing.JProgressBar();
        jScrollPane2 = new javax.swing.JScrollPane();
        Console = new Harvester.JPopupTextArea();

        mainPanel.setName("mainPanel"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        displayTable.setModel(model);
        displayTable.setComponentPopupMenu(popupMenu);
        displayTable.setDragEnabled(true);
        displayTable.setName("displayTable"); // NOI18N
        jScrollPane1.setViewportView(displayTable);
        displayTable.setDefaultRenderer(Object.class, new TableRenderer());
        TableRowSorter sorter = new TableRowSorter(model);
        sorter.setComparator(PoliceModel.NUM_UNIQUE_BYTES_INDEX, new NumericalComparator());
        displayTable.setRowSorter(sorter);

        // Ensure that the user can drop anywhere in the table
        displayTable.setFillsViewportHeight(true);
        // Show a hand icon when mousing over the header
        displayTable.getTableHeader().addMouseListener(new ClickableListener());

        jPanel2.setName("jPanel2"); // NOI18N
        jPanel2.setLayout(new java.awt.BorderLayout());

        MSPCCUIcon.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(Harvester.PoliceApp.class).getContext().getResourceMap(PoliceView.class);
        MSPCCUIcon.setIcon(resourceMap.getIcon("MSPCCUIcon.icon")); // NOI18N
        MSPCCUIcon.setText(resourceMap.getString("MSPCCUIcon.text")); // NOI18N
        MSPCCUIcon.setToolTipText(resourceMap.getString("MSPCCUIcon.toolTipText")); // NOI18N
        MSPCCUIcon.setName("MSPCCUIcon"); // NOI18N
        jPanel2.add(MSPCCUIcon, java.awt.BorderLayout.LINE_START);
        MSPCCUIcon.addMouseListener(new HyperlinkListener(desktop, "http://www.mcctf.org"));

        jPanel3.setName("jPanel3"); // NOI18N

        addressLine1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        addressLine1.setText(resourceMap.getString("addressLine1.text")); // NOI18N
        addressLine1.setName("addressLine1"); // NOI18N

        addressLine2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        addressLine2.setText(resourceMap.getString("addressLine2.text")); // NOI18N
        addressLine2.setName("addressLine2"); // NOI18N

        phoneLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        phoneLabel.setText(resourceMap.getString("phoneLabel.text")); // NOI18N
        phoneLabel.setName("phoneLabel"); // NOI18N

        faxLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        faxLabel.setText(resourceMap.getString("faxLabel.text")); // NOI18N
        faxLabel.setName("faxLabel"); // NOI18N

        org.jdesktop.layout.GroupLayout jPanel3Layout = new org.jdesktop.layout.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(phoneLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                    .add(faxLabel, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                    .add(addressLine2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE)
                    .add(addressLine1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 289, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel3Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .add(addressLine1)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(addressLine2)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(phoneLabel)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(faxLabel)
                .addContainerGap())
        );

        jPanel2.add(jPanel3, java.awt.BorderLayout.CENTER);

        dirigoStatePoliceLogo.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        dirigoStatePoliceLogo.setIcon(resourceMap.getIcon("dirigoStatePoliceLogo.icon")); // NOI18N
        dirigoStatePoliceLogo.setToolTipText(resourceMap.getString("dirigoStatePoliceLogo.toolTipText")); // NOI18N
        dirigoStatePoliceLogo.setName("dirigoStatePoliceLogo"); // NOI18N
        jPanel2.add(dirigoStatePoliceLogo, java.awt.BorderLayout.LINE_END);
        dirigoStatePoliceLogo.addMouseListener(new HyperlinkListener(desktop, "http://www.state.me.us/dps/msp/"));

        org.jdesktop.layout.GroupLayout mainPanelLayout = new org.jdesktop.layout.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jScrollPane1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.TRAILING, jPanel2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE))
                .addContainerGap())
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel2, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 337, Short.MAX_VALUE))
        );

        mainPanel.addPropertyChangeListener("dropLocation", new TableRepainter());

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(Harvester.PoliceApp.class).getContext().getActionMap(PoliceView.class, this);
        jMenuItem1.setAction(actionMap.get("launchFileChooser")); // NOI18N
        jMenuItem1.setName("jMenuItem1"); // NOI18N
        fileMenu.add(jMenuItem1);

        jMenuItem2.setAction(actionMap.get("saveToDisk")); // NOI18N
        jMenuItem2.setName("jMenuItem2"); // NOI18N
        fileMenu.add(jMenuItem2);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        jMenu1.setAction(actionMap.get("configureOffset")); // NOI18N
        jMenu1.setText(resourceMap.getString("jMenu1.text")); // NOI18N
        jMenu1.setName("jMenu1"); // NOI18N

        jMenuItem3.setAction(actionMap.get("configureOffset")); // NOI18N
        jMenuItem3.setText(resourceMap.getString("jMenuItem3.text")); // NOI18N
        jMenuItem3.setName("jMenuItem3"); // NOI18N
        jMenu1.add(jMenuItem3);

        jMenuItem4.setAction(actionMap.get("configureNumBytes")); // NOI18N
        jMenuItem4.setText(resourceMap.getString("jMenuItem4.text")); // NOI18N
        jMenuItem4.setToolTipText(resourceMap.getString("jMenuItem4.toolTipText")); // NOI18N
        jMenuItem4.setName("jMenuItem4"); // NOI18N
        jMenu1.add(jMenuItem4);

        jMenuItem5.setAction(actionMap.get("changeSavedColumns")); // NOI18N
        jMenuItem5.setText(resourceMap.getString("jMenuItem5.text")); // NOI18N
        jMenuItem5.setToolTipText(resourceMap.getString("jMenuItem5.toolTipText")); // NOI18N
        jMenuItem5.setName("jMenuItem5"); // NOI18N
        jMenu1.add(jMenuItem5);

        menuBar.add(jMenu1);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setToolTipText(resourceMap.getString("aboutMenuItem.toolTipText")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new java.awt.BorderLayout());

        statusLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        statusLabel.setText(resourceMap.getString("statusLabel.text")); // NOI18N
        statusLabel.setName("statusLabel"); // NOI18N
        jPanel1.add(statusLabel, java.awt.BorderLayout.PAGE_START);

        buttonPanel.setName("buttonPanel"); // NOI18N
        buttonPanel.setLayout(new java.awt.BorderLayout());

        jPanel5.setName("jPanel5"); // NOI18N

        add.setAction(actionMap.get("addOrCancel")); // NOI18N
        add.setName("add"); // NOI18N
        jPanel5.add(add);

        delete.setAction(actionMap.get("deleteRows")); // NOI18N
        delete.setText(resourceMap.getString("delete.text")); // NOI18N
        delete.setName("delete"); // NOI18N
        jPanel5.add(delete);

        save.setAction(actionMap.get("saveToDisk")); // NOI18N
        save.setText(resourceMap.getString("save.text")); // NOI18N
        save.setName("save"); // NOI18N
        jPanel5.add(save);

        saveAll.setAction(actionMap.get("saveAllToDisk")); // NOI18N
        saveAll.setText(resourceMap.getString("saveAll.text")); // NOI18N
        saveAll.setName("saveAll"); // NOI18N
        jPanel5.add(saveAll);

        buttonPanel.add(jPanel5, java.awt.BorderLayout.CENTER);

        jPanel1.add(buttonPanel, java.awt.BorderLayout.PAGE_END);

        progressBar.setName("progressBar"); // NOI18N
        progressBar.setStringPainted(true);
        jPanel1.add(progressBar, java.awt.BorderLayout.CENTER);
        progressBar.setVisible(false);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        Console.setColumns(20);
        Console.setRows(5);
        Console.setName("Console"); // NOI18N
        jScrollPane2.setViewportView(Console);

        org.jdesktop.layout.GroupLayout statusPanelLayout = new org.jdesktop.layout.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(org.jdesktop.layout.GroupLayout.TRAILING, statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.TRAILING)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE)
                    .add(org.jdesktop.layout.GroupLayout.LEADING, jPanel1, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 729, Short.MAX_VALUE))
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .add(jPanel1, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED)
                .add(jScrollPane2, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, 109, Short.MAX_VALUE)
                .addContainerGap())
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents

    
    public void	valueChanged(ListSelectionEvent e) {
        fixButtonStatuses();
        displayTable.requestFocus();
    }
    
    
    
    
    @Action
    public void changeSettings() {
        
    }
    
    @Action
    public void addOrCancel() {
        if (model.isAddingFiles()) {
            cancelAddingFiles();
        }
        else {
            launchFileChooser();
        }
    } 
    
    
    @Action
    public void launchFileChooser() {
        JFrame mainFrame = PoliceApp.getApplication().getMainFrame();
        int result = loadFileChooser.showDialog(mainFrame, "Add");
        if (result == JFileChooser.CANCEL_OPTION) {
           setStatusText("Canceled save");
           return;
        }
        
        File[] files = loadFileChooser.getSelectedFiles();
        model.addFiles(files, loadFileChooser.getFileFilter());
    }
    
    @Action
    public void cancelAddingFiles() {
        model.cancelBackgroundTask();
    }

    
    /**
     * Pops up a file chooser and returns the file chosen for saving.
     * @return the file to save to, or null if user cancels.
     */
    public File getSaveFile() {
        JFrame mainFrame = PoliceApp.getApplication().getMainFrame();
        
        File defaultSave = new File(PoliceModel.DEFAULT_OUTPUT_NAME);
        
        saveFileChooser.setSelectedFile(defaultSave);
        
        int result = saveFileChooser.showSaveDialog(mainFrame);
        
        if (result == JFileChooser.CANCEL_OPTION) {
            setStatusText("Canceled save");
            return null;
        }
        File output = saveFileChooser.getSelectedFile();
        
        // The file already exists.  Make sure they want to overwrite it
        if (output.exists()) {
            int selection = JOptionPane.showConfirmDialog(
                    mainFrame,
                    "File exists, do you wish to overwrite?",
                    "Confirm save",
                    JOptionPane.YES_NO_CANCEL_OPTION, 
                    JOptionPane.WARNING_MESSAGE);
            if (selection == JOptionPane.CANCEL_OPTION) {
                setStatusText("Canceled save");
                return null;
            }
            else if (selection == JOptionPane.YES_OPTION) {
                return output;
            }
            else if (selection == JOptionPane.NO_OPTION) {
                return getSaveFile();
            }
                    
        }
        return output;
    }
    
    @Action
    public void saveToDisk() {
        File output = getSaveFile();
        if (output == null) {
            return;
        }
            
        int [] selections = displayTable.getSelectedRows();
        for (int i = 0; i < selections.length; i++) {
            selections[i] = displayTable.convertRowIndexToModel(selections[i]);
        }
        
        model.save(output, selections);
        
        fixButtonStatuses();
    }
    
    @Action
    public void saveAllToDisk() {
        File output = getSaveFile();
        if (output == null) {
            return;
        }
        
        int [] selections = new int[displayTable.getRowCount()];
        for (int i = 0; i < selections.length; i++) {
            selections[i] = displayTable.convertRowIndexToModel(i);
        }
        
        model.save(output, selections);
        fixButtonStatuses();
        
    }
    
    // Given a new String, add it to the Console
    public void logError(String s) {
        Console.append(s);
    }
    
    // Change the text of the status label
    public void setStatusText(String s) {
        statusLabel.setText(s);
    }
    
    public void fixButtonStatuses() {
        
        if (model.isAddingFiles()) {
            add.setText("Stop Adding Files");
            add.setToolTipText("Stop adding the current batch of files");
        }
        else {
            add.setText("Add Files");
            add.setToolTipText("Choose files to hash");
        }
        
        saveAll.setEnabled(model.canSave());
        
        // They can only save selected rows or delete selected rows if there are
        // rows to be saved or deleted
        save.setEnabled(displayTable.getSelectedRowCount() > 0);
        delete.setEnabled(displayTable.getSelectedRowCount() > 0);
    }

    
    
    
    @Action
    public void openFiles() {
        int [] selections = displayTable.getSelectedRows();
        for (int i = 0; i < selections.length; i++) {
            selections[i] = displayTable.convertRowIndexToModel(selections[i]);
        }
        model.openFiles(desktop, selections);
    }
    

    @Action
    public void deleteRows() {
        // Because we allow the user to sort the columns, we need to ensure that
        // we translate back into model coordinates before removing rows
        int [] selections = displayTable.getSelectedRows();
        for (int i = 0; i < selections.length; i++) {
            selections[i] = displayTable.convertRowIndexToModel(selections[i]);
        }
        model.removeRows(selections);
        
        fixButtonStatuses();
    }
    
    @Action
    public void changeSavedColumns() {
        CheckboxDialog dialog = 
                new CheckboxDialog(PoliceApp.getApplication().getMainFrame(), 
                                   "Which columns do you want written to file?", 
                                   PoliceModel.COLUMN_HEADERS, 
                                   model.getColumnsToSave());
        // Launch the dialog
        dialog.select();
        model.setColumnsToSave(dialog.getSelections());
        dialog = null;
        
    }
    

    public javax.swing.JProgressBar getProgressBar() {
        return progressBar;
    }
    
    public javax.swing.JLabel getProgressLabel() {
        return statusLabel;
    }
    
    public javax.swing.JFileChooser getLoadFileChooser() {
        return loadFileChooser;
    }
    
    @Action
    public void emailNick() {
        email("ndunn2@bowdoin.edu");
    }

    @Action
    public void emailTucker() {
        
    }
    
    @Action
    public void emailJeremy() {
        
    }
    
    @Action
    public void email(String address) {
        
    }
    
    @Action
    public void launchBrowser(String url) {
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            URI uri = null;
            try {
                uri = new URI(url);
                desktop.browse(uri);
            }
            catch(IOException ioe) {
                ioe.printStackTrace();
                logError(ioe.getLocalizedMessage());
            }
            catch(URISyntaxException use) {
                use.printStackTrace();
                logError(use.getLocalizedMessage());
            }
        }
    }
    
    /**
     * Queries user as to what bytes he wants extracted, then
     * tells the model to fetch these bytes.  Does not change the
     * global setting for which bytes to extract.
     * 
     */
    public void extractNewBytes() {
        JFrame mainFrame = PoliceApp.getApplication().getMainFrame();
        
        String s = (String)JOptionPane.showInputDialog(
                    mainFrame,
                    "Enter byte offset:",
                    "Edit byte offset",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "" + model.getOffset());
        
        int offset = INVALID;
        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            // Convert the string to an integer
            try {
                offset = Integer.parseInt(s);
            }
            catch (NumberFormatException e) {
                setStatusText("Error: " + s + " is not a number.");
                return;
            }
        }
        
        
        int numBytes = INVALID;
        s = (String)JOptionPane.showInputDialog(
                    mainFrame,
                    "Enter number of bytes to extract:",
                    "Edit number of bytes",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "" + model.getNumBytes());
        
        
        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            // Convert the string to an integer
            try {
                numBytes = Integer.parseInt(s);
            }
            catch (NumberFormatException e) {
                setStatusText("Error: " + s + " is not a number.");
                return;
            }
        }
        
        // User has specified new bytes to extract.  Tell the model to
        // make these changes.
        if (offset != INVALID && numBytes != INVALID) {
            int [] selections = displayTable.getSelectedRows();
            for (int i = 0; i < selections.length; i++) {
                selections[i] = displayTable.convertRowIndexToModel(selections[i]);
            }
            setStatusText("Editing the offsets of " + selections.length + " files.");
            model.editOffsets(selections, offset, numBytes);
        }
        
    }
    
    @Action
    public void configureOffset() {
        JFrame mainFrame = PoliceApp.getApplication().getMainFrame();
        
        String s = (String)JOptionPane.showInputDialog(
                    mainFrame,
                    "Enter byte offset:",
                    "Edit byte offset",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "" + model.getOffset());
        
        
        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            // Convert the string to an integer
            try {
                int offset = Integer.parseInt(s);
                model.setOffset(offset);
            }
            catch (NumberFormatException e) {
                setStatusText("Error: " + s + " is not a number.");
            }
            return;
        }

        //If you're here, the return value was null/empty.
        setStatusText("Cancelled editing byte offset");

    }
    
     @Action
    public void configureNumBytes() {
        JFrame mainFrame = PoliceApp.getApplication().getMainFrame();
        
        String s = (String)JOptionPane.showInputDialog(
                    mainFrame,
                    "Enter number of bytes to extract:",
                    "Edit number of bytes",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    "" + model.getNumBytes());
        
        
        //If a string was returned, say so.
        if ((s != null) && (s.length() > 0)) {
            // Convert the string to an integer
            try {
                int offset = Integer.parseInt(s);
                model.setNumBytes(offset);
            }
            catch (NumberFormatException e) {
                setStatusText("Error: " + s + " is not a number.");
            }
            return;
        }

        //If you're here, the return value was null/empty.
        setStatusText("Cancelled editing number of bytes");

    }
     
    
     
     
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private Harvester.JPopupTextArea Console;
    private javax.swing.JLabel MSPCCUIcon;
    private javax.swing.JButton add;
    private javax.swing.JLabel addressLine1;
    private javax.swing.JLabel addressLine2;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton delete;
    private javax.swing.JLabel dirigoStatePoliceLogo;
    private javax.swing.JTable displayTable;
    private javax.swing.JLabel faxLabel;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JLabel phoneLabel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton save;
    private javax.swing.JButton saveAll;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables

    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;

    private JDialog aboutBox;
    private PoliceModel model;
    private JDialog options;
    private JPopupMenu popupMenu, popup;
    private JFileChooser loadFileChooser;
    private JFileChooser saveFileChooser;
    
    private Desktop desktop;
    
    private static final String DELETE_STRING = "deleteFiles";
    private static final String OPEN_STRING = "openFiles";
    
    private static final int INVALID = -1;
    
}
