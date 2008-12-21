/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Harvester;

import java.awt.Desktop;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;
import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;


/**
 *
 * @author Nick Dunn
 * @date   November 15, 2008
 */
public class PoliceModel extends AbstractTableModel {

    public static final boolean DEBUG = true;
    
    private Preferences prefs;
    
    // Which police department/organization is submitting the information?
    private String department;
    // Which individual is using the software?
    private String name;
    
    private PoliceView view;
   
    private List <TableRow> data;
    
    // The MD5 hash we want a hexadecimal result
    private final HashRequest MD5 = new HashRequest("MD5", 16);
    // The SHA_1 hash we want a base 32 result
    private final HashRequest SHA_1 = new HashRequest("SHA-1", 32);
    private List <HashRequest> hashTypes;
    
    private SwingWorker backgroundTask;
    
    private FileFilter videoFilter;
    private FileFilter imageFilter;
    
    public static final int NUM_COLS = 6;
    public static final int FILES_INDEX = 0;
    public static final int MD5_INDEX = 1;
    public static final int SHA_1_INDEX = 2;
    public static final int BYTES_EXTRACTED_INDEX = 3;
    public static final int HEX_BYTES_INDEX = 4;
    public static final int FILE_TYPE_INDEX = 5;
    
    
    public static final String DEFAULT_OUTPUT_NAME = "output.txt";
    
    public static final String FILES_STRING = "File Name";
    public static final String MD5_STRING = "MD5 Hash (Base 16)";
    public static final String SHA_1_STRING = "SHA-1 Hash (Base 32)";
    
    public static final String BYTES_EXTRACTED_STRING = "Bytes Extracted";
    
    
    public static final String HEX_BYTES_STRING = "Hex bytes";
    public static final String FILE_TYPE_STRING = "File Type";
    
    public static final String IMAGE_STRING = "Image";
    public static final String VIDEO_STRING = "Video";
    public static final String UNKNOWN_STRING = "Unknown";
    
    
    public static final String EMPTY_BYTES = "\\x00\\x00\\x00\\x00";
    
    
    public static final String LINE_SEPARATOR = System.getProperty("line.separator");
    public static final String COLUMN_SEPARATOR = ",";
    
    
    public static final int DEFAULT_NUM_BYTES = 10;
    public static final int DEFAULT_BYTE_OFFSET = 43000;
    
    public static final String DEFAULT_NUM_BYTES_STRING = "defaultNumBytes";
    public static final String DEFAULT_BYTE_OFFSET_STRING = "defaultByteOffset";
    
    
    
    private int numBytes;
    private int offset;
    
    public PoliceModel() {
        videoFilter = new VideoFilter();
        imageFilter = new ImageFilter();
        
        
        // We run these hashing algorithms on each file
        hashTypes = new LinkedList<HashRequest>();
        hashTypes.add(MD5);
        hashTypes.add(SHA_1);
        
        // Initialize the preferences for the application
        prefs = Preferences.userNodeForPackage(this.getClass());
        
        numBytes = prefs.getInt(DEFAULT_NUM_BYTES_STRING, DEFAULT_NUM_BYTES);
        offset = prefs.getInt(DEFAULT_BYTE_OFFSET_STRING, DEFAULT_BYTE_OFFSET);
                
        data = new ArrayList<TableRow>();
    }
    
    public void setView(PoliceView view) {
        this.view = view;
    }
    
    
    public int getOffset() { return offset; }
    
    public void setOffset(int offset) { 
        
        if (offset < 0) {
            view.setStatusText("Error, offset must be positive");
            return;
        }
        
        view.setStatusText("Changing offset from " + this.offset + " to " + offset);
        this.offset = offset; 
        prefs.putInt(DEFAULT_BYTE_OFFSET_STRING, offset);
        fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }
    
    public int getNumBytes() { return numBytes; }
    
    public void setNumBytes(int numBytes) { 
        if (numBytes < 0) {
            view.setStatusText("Error, number of bytes must be positive");
            return;
        }
        
        view.setStatusText("Changing number of bytes from " + this.numBytes + 
                            " to " + numBytes);
        
        this.numBytes = numBytes; 
        prefs.putInt(DEFAULT_NUM_BYTES_STRING, numBytes);
        
        fireTableChanged(new TableModelEvent(this, TableModelEvent.HEADER_ROW));
    }
    
    public boolean canSave() {
        return data.size() > 0;
    }
    
    
    /**
     * The array of file objects returned by the file chooser does not tell
     * the whole story.  Since we recursively descend through the directories,
     * we need to expand files that are directories and add all of their contents
     * to the list.  
     * Flattening the directory tree like this allows us to accurately estimate
     * how many files we have to add.
     * @param files the list of files that were selected when user clicked "Add"
     * @param filter the file filter that was selected at the time of adding
     * files
     * @return a list of File objects, none of which will be a directory
     */
    public List<File> flattenDirectoryTree(File[] files, javax.swing.filechooser.FileFilter
 filter) {
        LinkedList<File> fileList = new LinkedList<File>();
        
        for (File f : files) {
            addFile(f, fileList, filter);
        }
        return fileList;
    }
    
    
    public void addFile(File f, List<File> fileList, javax.swing.filechooser.FileFilter
 filter) {
        
        if (!f.exists() || !filter.accept(f)) {
            return;
        }
        
        
        // We have to add all the files contained within the directory
        if (f.isDirectory()) {
            addDirectory(f, fileList, filter);
        }
        // We have a file, not a directory.  
        else {
            // If the file is too small, we cannot extract the bytes we need. Ignore
            // the file.
            if (!FileHashConverter.canExtractBytes(f, offset, numBytes)) {
                view.logError("File " + f + " is too small for processing; " +
                              "ignoring" + LINE_SEPARATOR);
                return;
            }
            // The file is fine to add
            else {
                fileList.add(f);
            }
        }
    }
    
   
   
    
    
    public void addDirectory(File dir, List<File> fileList, javax.swing.filechooser.FileFilter
 filter) {
        for (File f: dir.listFiles()) {
            addFile(f, fileList, filter);
        }
    }
    
    
    public void addRows(List <TableRow> rows) {
        int startIndex = data.size();
        data.addAll(rows);
        int endIndex = data.size() - 1;
        fireTableRowsInserted(startIndex, endIndex);
        
        
        //fireTableDataChanged();
    }

    
    public String getFileType(File f) {
        // It's an image file
        if (imageFilter.accept(f)) {
            return IMAGE_STRING;
        }
        // It's a video file
        else if (videoFilter.accept(f)) {
            return VIDEO_STRING;
        }
        // Not sure what it is
        else {
            return UNKNOWN_STRING;
        }
    }
    
    // If user does not specify the file filter, default to accepting all
    public void addFiles(Collection<File> files) {
        addFiles(files, view.getLoadFileChooser().getAcceptAllFileFilter());
    }
    
    
    
    
    public void addFiles(Collection<File> files, javax.swing.filechooser.FileFilter filter) {
        File[] fileArray = (File[]) files.toArray();
        addFiles(fileArray, filter);
    }
    
    
    
    
    // Given an array of files, repeatedly call addFile() on each element
    public void addFiles(File[] array, 
                        javax.swing.filechooser.FileFilter filter) {
        
        // We recursively add all files found within folders.  This method finds
        // all the files that are NOT directories after traversing the whole 
        // tree structure.  As such we know exactly how many files we have to add.
        List <File> flattenedFiles = flattenDirectoryTree(array, filter);
        
        backgroundTask = new BackgroundWorker(flattenedFiles, hashTypes,
                                               this, view, offset, numBytes);
        backgroundTask.execute();
        
        
        view.getProgressLabel().setText("Adding " + flattenedFiles.size() + 
                                        " files.");
        
        final JProgressBar progressBar = view.getProgressBar();
        
        // Make the progress bar visible so that user can tell how much is left
        // to do
        progressBar.setVisible(true);
        // Make the progress bar listen to how much work has been completed
        backgroundTask.addPropertyChangeListener(
                new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        if (evt.getPropertyName().equals("progress")) {
                             progressBar.setValue((Integer) evt.getNewValue());
                        }        
                    }
        });
        
        view.fixButtonStatuses();
    }
    
    public void cancelBackgroundTask() {
        if (backgroundTask != null) {
            backgroundTask.cancel(true);
        }
        
    }
    
    /**
     * 
     * @return true if we are currently adding files, else false
     */
    public boolean isAddingFiles() {
        return backgroundTask != null && !backgroundTask.isDone();
    }
    
    
    
    public void openFiles(Desktop desktop, int[] rows) {
        
            for (int row : rows) {
                try {
                    desktop.open(data.get(row).getFile());
                } catch (IOException ex) {
                    Logger.getLogger(PoliceModel.class.getName()).log(Level.SEVERE, null, ex);
                    view.logError(ex.getLocalizedMessage());
                }
            }
        
    }
    
    
    public void removeRows(int[] rows) {
        // Make sure the rows are sorted
        Arrays.sort(rows);
        
        // Must delete in reverse order to preserve the ordering
        for (int i = rows.length - 1; i > -1; i--) {
            removeRow( rows[i] );
        }
        view.setStatusText("Deleted " + rows.length + 
                            " records");
        fireTableDataChanged();
    }
    
    
    public void removeRow(int row) {
        data.remove(row);
        
    }
    
    
    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case FILES_INDEX:
                return FILES_STRING;
            case MD5_INDEX:
                return MD5_STRING;
            case SHA_1_INDEX:
                return SHA_1_STRING;
            case BYTES_EXTRACTED_INDEX:
                return BYTES_EXTRACTED_STRING;
            case HEX_BYTES_INDEX:
                return HEX_BYTES_STRING;
            case FILE_TYPE_INDEX:
                return FILE_TYPE_STRING;
            default:
                return "Error: Invalid columnIndex in getColumnName";
        }
        
    }
    
    /**
     * 
     * @return the number of columns in the table
     */
    public int getColumnCount() { return NUM_COLS; }
    
    /**
     * 
     * @return the number of rows in the table
     */
    public int getRowCount() { return data.size();}
    
    /**
     * This is how the table actually gets filled.  
     * @param row the index of the row of the cell to fetch
     * @param col the index of the column of the cell to fetch
     * @return the data at (row, column) in the table
     */
    public Object getValueAt(int row, int col) { 
        switch(col) {
            case FILES_INDEX:
                return data.get(row).getFile().getName();
                
            case MD5_INDEX:
                return data.get(row).getMD5Hash();
            case SHA_1_INDEX:
                return data.get(row).getSha1Hash();
            case HEX_BYTES_INDEX:
                return data.get(row).getHexBytes();
                
            case BYTES_EXTRACTED_INDEX:
                return data.get(row).getByteString();
            case FILE_TYPE_INDEX:
                return data.get(row).getFileType();
            default:
                return "Error in PoliceModel::getValueAt()";
        }
    }
    
    
    
    
    /**
     * Given an output file and an array of selected indices, we go
     * through and write all the selected records to the file.
     * @param output the file to which we should write the records
     * @param selections the indices (in terms of this model) of the selected
     *        rows.
     */
    public void save(File output, int[] selections) {
        try {
            BufferedWriter outputStream = 
                new BufferedWriter(new FileWriter(output));
            
            // Write the header
            outputStream.write(FILES_STRING);
            outputStream.write(COLUMN_SEPARATOR);
            outputStream.write(MD5_STRING);
            outputStream.write(COLUMN_SEPARATOR);
            outputStream.write(SHA_1_STRING);
            outputStream.write(COLUMN_SEPARATOR);
            outputStream.write("Bytes extracted");
            outputStream.write(COLUMN_SEPARATOR);
            outputStream.write("Hex data with a \\x prior to each hex value");
           
            outputStream.write(LINE_SEPARATOR);
            
            
            StringBuilder builder = new StringBuilder();
            
            // Write each line to a separate line in the file
            for (int i = 0; i < selections.length; i++) {
                int index = selections[i];
                
                builder.setLength(0);
                
                TableRow current = data.get(index);
                
                builder.append(current.getFile().getName()); 
                builder.append(COLUMN_SEPARATOR); 
                builder.append(current.getMD5Hash());
                builder.append(COLUMN_SEPARATOR); 
                builder.append(current.getSha1Hash());
                builder.append(COLUMN_SEPARATOR); 
                builder.append(current.getByteString());
                builder.append(COLUMN_SEPARATOR);
                builder.append(current.getHexBytes());
                builder.append(LINE_SEPARATOR);
                
                outputStream.write(builder.toString());
            }
            outputStream.close();
            
            view.setStatusText("Successfully wrote " + selections.length + 
                            " records to " + output);
        }
        catch (IOException e) {
            view.logError("Could not open file " + output + 
                          " for writing." + LINE_SEPARATOR);
            return;
        }
    }

}
