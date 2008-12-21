/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package Harvester;

import java.awt.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.SwingWorker;

/**
 * This class does all the heavy lifting in a background thread while the GUI
 * remains responsive.  It is given a list of files that the user wants to add
 * and then it calls the methods to hash the files as well as extract the bytes
 * from a given offset.  Objects of this class periodically pass the chunks
 * of data that they have finished processing to the Model, which in turn updates
 * the JTable displaying the data.  This leads to intermediate results being
 * displayed as soon as they are available, as opposed to after all the processing
 * has been completed.
 * @see http://java.sun.com/docs/books/tutorial/uiswing/concurrency/interim.html
 * @author Nick Dunn
 * @date   November 16, 2008
 */
public class BackgroundWorker extends SwingWorker<List<TableRow>, TableRow> {
    
    private List<File> files;
    private List<HashRequest> hashAlgorithms;
    private PoliceModel model;
    private PoliceView view;
    private int offset;
    private int numBytes;
    
    public BackgroundWorker(List <File> files, List <HashRequest> hashAlgorithms,
                            PoliceModel model, PoliceView view,
                            int offset, int numBytes) {
        this.files = files;
        this.hashAlgorithms = hashAlgorithms;
        this.model = model;
        this.view = view;
        this.offset = offset;
        this.numBytes = numBytes;
    }
        
    @Override
    protected List<TableRow> doInBackground() throws Exception {
        JFrame mainFrame = PoliceApp.getApplication().getMainFrame();
        mainFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        
        
        List<TableRow> results = new ArrayList<TableRow>();
        
        // For each file, do the three things we need: determine its hash, the
        // hex bytes, and its file type.
        for (File f : files) {
            // If user has hit the cancel button
            // then stop adding files
            if (isCancelled() || Thread.interrupted()) {
                return results;
            }
            List<String> hashes = FileHashConverter.getFileHashes(f, hashAlgorithms);
            
            String hexBytes = FileHashConverter.getFileByteHex(offset, numBytes, f);
            String fileType = model.getFileType(f); 
            
            TableRow current = new TableRow(f, hashes.get(0), hashes.get(1), 
                                            hexBytes, fileType, offset, numBytes);
            
            // Results holds everything completed so far
            results.add(current);
            // We are finished with this file, let the thread know
            publish(current);
            
            // Calculate the percent done; this will in turn tell the JProgressBar
            // that is listening to us
            setProgress(100 * results.size() / files.size());
        }
        return results;
    }
    
    /**
     * When enough calls have been made to publish(), this method takes the list
     * of completed chunks and passes them off to the table for immediate updating.
     * This means that we see results as soon as they are ready; we do not have
     * to wait for every file to be processed
     * @param chunks
     */
    @Override
    protected void process(List<TableRow> chunks) {
        model.addRows(chunks);
    }
    
    
   
    
    /**
     * Upon finishing execution, this method is called.  We hide the progress
     * bar, reset its value back to 0, and update the status label with the
     * total number of files added
     */
    @Override
    protected void done() {
        JFrame mainFrame = PoliceApp.getApplication().getMainFrame();
        mainFrame.setCursor(Cursor.getDefaultCursor());
        
        JProgressBar bar = view.getProgressBar();
        bar.setValue(0);
        bar.setVisible(false);
        
        // Since we are finished, we need to change the Cancel button to be
        // an "Add" button.
        view.fixButtonStatuses();
        
        if (isCancelled()) {
            view.setStatusText("Cancelled adding files");
        }
        else {
            try {
                int numFiles = get().size();
                String file = (numFiles > 1 ? "files" : "file");
                view.setStatusText("Successfully added " + numFiles + " " + file);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    
}
