package Harvester;

/**
 * This class holds the result of extracting bytes from the file.  It includes
 * such information as the number of bytes extracted, where the bytes came from,
 * what the bytes are, and the number of unique bytes in the file.
 * @author Nick
 */
public class BytesResult {
    public int offset;
    public int numBytes;
    public int numUniqueBytes;
    public String byteString;
    
    public BytesResult(int offset, int numBytes, int numUniqueBytes, 
                        String byteString) {
        this.offset = offset;
        this.numBytes = numBytes;
        this.numUniqueBytes = numUniqueBytes;
        this.byteString = byteString;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getNumBytes() {
        return numBytes;
    }

    public void setNumBytes(int numBytes) {
        this.numBytes = numBytes;
    }

    public int getNumUniqueBytes() {
        return numUniqueBytes;
    }

    public void setNumUniqueBytes(int numUniqueBytes) {
        this.numUniqueBytes = numUniqueBytes;
    }

    public String getByteString() {
        return byteString;
    }

    public void setByteString(String byteString) {
        this.byteString = byteString;
    }
    
    
    
}