
package Harvester;

import java.io.File;
import javax.swing.filechooser.FileFilter;




/**
 *
 * @author Nick
 */
public class TextFilter extends FileFilter implements java.io.FileFilter {
    public static final String txt = "txt";
    public static final String text = "text";
    public static final String doc = "doc";
    
    
    
    public String getDescription() {
        return "Text Files";
    }
    
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = getExtension(f);
        
        System.out.println(extension);
        
        if (extension != null) {
            if (extension.equals(txt) ||
                extension.equals(text) ||
                extension.equals(doc)) {
                    
                return true;
            }
            else {    
                return false;
            }
        }
        return false;
    }
    
    /*
     * Get the extension of a file.
     */  
    public static String getExtension(File f) {
        String ext = null;
        String s = f.getName();
        int i = s.lastIndexOf('.');

        if (i > 0 &&  i < s.length() - 1) {
            ext = s.substring(i+1).toLowerCase();
        }
        return ext;
    }

}
