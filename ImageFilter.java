

package Harvester;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/*
 * @author Nicholas Dunn
 * @date   November 13, 2008
 * 
 * Based on code from http://java.sun.com/docs/books/tutorial/uiswing/components/filechooser.html
 */
public class ImageFilter extends FileFilter implements java.io.FileFilter {
    
    public final static String jpeg = "jpeg";
    public final static String jpg = "jpg";
    public final static String gif = "gif";
    public final static String tiff = "tiff";
    public final static String tif = "tif";
    public final static String png = "png";
    public final static String bmp = "bmp";
    

    
    public String getDescription() {
        return "Image Files";
    }
    
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
            if (extension != null) {
                if (extension.equals(tiff) ||
                    extension.equals(tif)  ||
                    extension.equals(gif)  ||
                    extension.equals(jpeg) ||
                    extension.equals(jpg)  ||
                    extension.equals(png)  ||
                    extension.equals(bmp)) {
                        return true;
                } else {    
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
