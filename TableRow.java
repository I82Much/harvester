package Harvester;

import java.io.File;

//TODO: rename this class
/**
 * Houses all of the information needed for a row of our table
 * @author Nick Dunn
 * 
 */
public class TableRow {
    private File file;
    private String md5Hash;
    private String sha1Hash;
    private String fileType;
    private BytesResult hexBytes;
     
    
    public TableRow(File f, String md5, String sha1Hash,
                    BytesResult hex, String type) {
        this.file = f;
        this.md5Hash = md5;
        this.sha1Hash = sha1Hash;
        this.hexBytes = hex;
        this.fileType = type;
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
        return hexBytes.getByteString();
    }
    
    public String getFileType() {
        return fileType;
    }
   
    public int getNumUniqueBytes() {
        return hexBytes.getNumUniqueBytes();
    }
       
    public int getOffset() { return hexBytes.getOffset(); }
    
    public int getNumBytes() { return hexBytes.getNumBytes(); }
    
    public void setOffset(int offset) { hexBytes.setOffset(offset); }
    
    public void setNumBytes(int numBytes) { hexBytes.setNumBytes(numBytes); }
    
    public void setHexBytes(String newHexBytes) { hexBytes.setByteString(newHexBytes); }
   
    public void setHexBytesResult(BytesResult result) { 
        this.hexBytes = result;
    }
    
    
    
    public String getByteString() { 
        StringBuilder s = new StringBuilder();
        int offset = hexBytes.getOffset();
        int numBytes = hexBytes.getNumBytes();
        s.append(offset);
        s.append(" to ");
        s.append((offset + numBytes - 1));
        return s.toString();
    }
}
