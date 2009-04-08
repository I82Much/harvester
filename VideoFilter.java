

package Harvester;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/*
 * @author Nicholas Dunn
 * @date   November 14, 2008
 * 
 * Allows a user filter files by video file extension.
 * 
 * Based on code from http://java.sun.com/docs/books/tutorial/uiswing/components/filechooser.html
 * Some extensions taken from http://people.csail.mit.edu/tbuehler/video/extensions.html
 */
public class VideoFilter extends FileFilter implements java.io.FileFilter {
    
    /** The file extension for .avi movies.*/
    public final static String AVI = "avi";
    /** The file extension for .divx movies.*/
    public final static String DIVX = "divx";
    
    /** The file extension for .dv movies.*/
    public final static String DV = "dv";
    
    /** The file extension for .mov movies.*/
    public final static String MOV = "mov";
    
    /** The file extension for .avi movies.*/
    public final static String MOVIE = "movie";
    
    /** The file extension for .mpeg movies.*/
    public final static String MPEG = "mpeg";
    
    /** The file extension for .mpg movies.*/
    public final static String MPG = "mpg";
    
    /** The file extension for .mp4 movies.*/
    public final static String MP4 = "mp4";

    /** The file extension for QuickTime movies.*/
    public final static String QT = "qt";

    
    /** The file extension for RealMedia movies.*/
    public final static String RM = "rm";
    /** The file extension for Windows Media Video movies.*/
    public final static String WMV = "wmv";
    
    
    public String getDescription() {
        return "Movie Files";
    }
    
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }

        String extension = getExtension(f);
        if (extension != null) {
            if (extension.equals(AVI)  ||
                extension.equals(DIVX) ||
                extension.equals(DV)   ||
                extension.equals(MOV)  ||
                extension.equals(MOVIE)||
                extension.equals(MPEG) ||
                extension.equals(MPG)  ||
                extension.equals(MP4)  ||
                extension.equals(QT)   ||
                extension.equals(RM)   ||
                extension.equals(WMV)) {
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
