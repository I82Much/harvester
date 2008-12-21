
package Harvester;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTable;
import javax.swing.TransferHandler;

/**
 * Handles when user tries to drag files into the main table.  If it's a file
 * or folder, will accept the transfer and pass off the files to the underlying
 * model, which then updates the view.  Draws the table in green when the 
 * drop can be accepted
 * @author Nick Dunn
 * @date   November 24, 2008
 * 
 */
public class DragAndDropHandler extends TransferHandler {

    private PoliceModel model;
    private PoliceView view;
    private JTable table;
    
    public DragAndDropHandler(PoliceModel model, PoliceView view, JTable table) {
        this.model = model;
        this.view = view;
        this.table = table;
    }
    
    
    @Override
    public boolean canImport(TransferHandler.TransferSupport data) {
        if (!data.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            //table.setBackground(Color.RED);
            return false;
        }
        //table.setBackground(Color.GREEN);
        
        return true;
    }
    
    @Override
    public boolean importData(TransferHandler.TransferSupport data) {
        // Check to see if we can handle the file or files
        if (!canImport(data)) {
            return false;
        }
        
        Transferable t = data.getTransferable();
        try {
            List<File> files = (List<File>) t.getTransferData(DataFlavor.javaFileListFlavor);
            model.addFiles(files);
            
        } catch (UnsupportedFlavorException ex) {
            Logger.getLogger(DragAndDropHandler.class.getName()).log(Level.SEVERE, null, ex);
            view.logError(ex.getMessage());
            
        } catch (IOException ex) {
            Logger.getLogger(DragAndDropHandler.class.getName()).log(Level.SEVERE, null, ex);
            view.logError(ex.getMessage());
        }
        
        // We know now that we can handle it.  Take care of the transfer
        return true;
    }
}
