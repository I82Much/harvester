
package Harvester;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * Class handles the rendering of the table of data.  Rows will appear color
 * coded according to their file type.  Furthermore, rows that have too few
 * unique hex bytes (e.g. all of the hex bytes are the same) show up in 
 * a different color
 * @see http://www.java-forums.org/awt-swing/541-how-change-color-jtable-row-having-particular-value.html
 * @author Nick Dunn
 * @date   November 16, 2008
 */
public class TableRenderer extends DefaultTableCellRenderer {

    public static final Color UNKNOWN_COLOR = Color.RED;
    public static final Color VIDEO_COLOR = Color.BLACK;
    public static final Color IMAGE_COLOR = Color.BLACK;
    
    public static final Color NOT_UNIQUE_COLOR = Color.MAGENTA;
    
    public static final Font MONO = new Font("Monospaced", Font.PLAIN, 13);
    
    @Override
    public Component getTableCellRendererComponent(JTable table, 
                                                    Object value, 
                                                    boolean isSelected, 
                                                    boolean hasFocus, 
                                                    int row, 
                                                    int col)
{
        Component comp = super.getTableCellRendererComponent(
                          table,  value, isSelected, hasFocus, row, col);
                          
        
        // Make the numerical stuff monospaced
        if (col == PoliceModel.HEX_BYTES_INDEX || 
            col == PoliceModel.SHA_1_INDEX     ||
            col == PoliceModel.MD5_INDEX          ) {
            comp.setFont(MONO);
        }
          
        
        // Since we allow sorting, we need to convert back to model coordinates
        int modelIndex = table.convertRowIndexToModel(row);
        
        String s =  table.getModel().getValueAt(modelIndex, 
                                                PoliceModel.FILE_TYPE_INDEX).toString();

        if(s.equals( PoliceModel.IMAGE_STRING) )
        {
            comp.setForeground(IMAGE_COLOR);
        }
        else if (s.equals(PoliceModel.VIDEO_STRING))
        {
             comp.setForeground(VIDEO_COLOR);
        }
        else {
            comp.setForeground(UNKNOWN_COLOR);
        }
       
        PoliceModel model = (PoliceModel) table.getModel();
        if (!model.isUnique(modelIndex)) {
            comp.setForeground(NOT_UNIQUE_COLOR);
        }
      
        return comp;
    }
    
}
