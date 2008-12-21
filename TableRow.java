package Harvester;

import java.io.File;

/**
 * Houses all of the information needed for a row of our table
 * @author Nick Dunn
 */
public class TableRow {
    private File file;
    private String md5Hash;
    private String sha1Hash;
    private String hexBytes;
    private String fileType;
    private int offset;
    private int numBytes;
    
    
    public TableRow(File f, String md5, String sha1Hash,
                    String hex, String type, int offset,
                    int numBytes) {
        this.file = f;
        this.md5Hash = md5;
        this.sha1Hash = sha1Hash;
        this.hexBytes = hex;
        this.fileType = type;
        this.offset = offset;
        this.numBytes = numBytes;
    }
    
    public File getFile() {
        return file;
    }
    
    public String getMD5Hash() { 
        return md5Hash; 
    }
    
    public String getSha1Hash() {
        return sha1Hash;
    }
    
    public String getHexBytes() {
        return hexBytes;
    }
    
    public String getFileType() {
        return fileType;
    }
    
    public int getOffset() { return offset; }
    
    public int getNumBytes() { return numBytes; }
    
    public void setOffset(int offset) { this.offset = offset; }
    
    public void setNumBytes(int numBytes) { this.numBytes = numBytes; }
    
    public void setHexBytes(String hexBytes) { this.hexBytes = hexBytes; }
   
    public String getByteString() { 
        StringBuilder s = new StringBuilder();
        s.append(offset);
        s.append(" to ");
        s.append((offset + numBytes - 1));
        return s.toString();
    }
}
