package Harvester;


import java.io.*;
import java.security.*;
import java.util.LinkedList;
import java.util.List;

/**ß
 * This class does the MD5 hashing and extracts the bytes specified at a given
 * offset
 * @author Tucker Hermans - original implementation
 * @author Nicholas Dunn - added buffering so as to allow large files to be read
 * @date   November 14, 2008
 * 
 */
public class FileHashConverter
{
    
    // Instead of attempting to read the whole file in at once, we read only
    // a portion at a time.  This ensures that we do not run out of memory.
    private final static int BUFFER_SIZE = (int) Math.pow(2, 25);
    
    public enum BASE_FORMAT {
        BASE_32, 
        BASE_16, // hex
        BASE_10, // decimal
        BASE_8   // octal
    }
    
    // There are 2^8 or 256 different byte values.
    public static final int NUM_BYTES_POSSIBLE = (int) Math.pow(2, Byte.SIZE);
    
    
    public static List<String> getFileHashes(File input, List<HashRequest> hashingRequests) 
                                             
        throws FileNotFoundException, IOException, NoSuchAlgorithmException {
        
        
        if (input.length() == 0) {
            throw new IOException("Error, file " + input + " has 0 bytes");
        }
        
        List<String> hashes = new LinkedList<String>();
        
        // Create a buffer big enough to hold either a portion of the file or
        // the whole thing, whichever is smaller
        byte[] buffer = new byte[Math.min(BUFFER_SIZE, (int) input.length())];
        int read = 0;
        
        // Create an input stream from the file; we might not
        // be able to fit the whole file in memory
        FileInputStream fis = new FileInputStream(input);

        List<MessageDigest> algorithms = new LinkedList<MessageDigest>();
        for (HashRequest h: hashingRequests) {
            MessageDigest algorithm = MessageDigest.getInstance(h.getName());
            algorithm.reset();
            algorithms.add(algorithm);
        }
        
        
        // Calculate the hashes of the file
        
        // There's more to the file to be read
        while ( (read = fis.read(buffer)) != -1)  {
            for (MessageDigest algorithm : algorithms) {
                algorithm.update(buffer, 0, read);
            }
        }
        
        for (int i = 0; i < algorithms.size(); i++) {
            MessageDigest algorithm = algorithms.get(i);
            HashRequest request = hashingRequests.get(i);
          
            byte[] messageDigest = algorithm.digest();
        
            // Certain requests are made in base 32, some in 16... this method
            // figures it out for us
            hashes.add(getStringFromBytes(messageDigest, request.getRadix()));
            
            
        }
        return hashes;
    }
    
    
    public static String getStringFromBytes(byte[] bytes, int base) {
        switch (base) {
            case 32:
                return Base32.encode(bytes);
            case 16:
                StringBuffer hexString = new StringBuffer();
                for (int i=0;i<bytes.length;i++) {
                    String hexStr = Integer.toHexString(0xFF &
                                              bytes[i]);
                    // Need to pad with 0 on the left
                    if (hexStr.length() == 1) {
                        hexString.append('0');
                    }
                    hexString.append(hexStr);
                    
                }
                return hexString.toString();
            /*case 10:
                return "";
            case 8:
                return "";*/
            default:
                return "Unsupported radix in getStringFromBytes.";
        }       
    }
    
    
    
    
    public static boolean canExtractBytes(File f, int offset, int numBytes) {
        return f.length() > 0;
    }
    
    
 
    
  

    
    /**
     * Extracts the numBytes bytes starting at offset start in the file.
     * If the file is too small, does not give an error, merely returns an object
     * whose byte string is a warning message that no bytes could be extracted.
     * Note that the bytes are converted into a positive range before being 
     * converted into a string.
     *
     * @param start How many bytes to skip before extracting bytes
     * @param numBytes how many bytes to explor
     * @param input The file to be evaluated
     * @return An object encapsulating the start offset, the number of bytes 
     * extracted, how many of these bytes were distinct, and the actual byte
     * string.  
     */
    public static BytesResult getFileByteHex(int start, int numBytes, File input)  throws
        FileNotFoundException, IOException 
    {
        
        // Check to make sure the specified hex value is not too large
        if ( input.length() < start + numBytes -1) {
            return new BytesResult(start, numBytes, 0, "Not enough bytes for this offset");
        }
        
        // Allocate enough space to read the numBytes bytes
        byte[] fileBytes = new byte[numBytes];
       
        // Create an input stream from the file
        FileInputStream fis = new FileInputStream(input);
        
        // We only care about the bytes starting at this point; ignore all those
        // bytes that come before it
        fis.skip(start);
        
        //  Read the specified bytes into the byte array
        fis.read(fileBytes, 0, numBytes);
        
        
        int numUniqueBytes = countUniqueBytes(fileBytes);
        
        
        StringBuffer hexString = new StringBuffer();
        for(int i = 0; i < numBytes; i++) {
            hexString.append("\\x");
            String hexStr = Integer.toHexString(0xFF &
                                              fileBytes[i]).toUpperCase();
            // Need to pad with 0 on the left
            if (hexStr.length() == 1) {
                hexString.append('0');
                hexString.append(hexStr);
            }
            // It's fine
            else {
                hexString.append(hexStr);
            }
        }
        return new BytesResult(start, numBytes, numUniqueBytes, hexString.toString());
    }

    /**
     * Given an array of raw bytes, determine how many of them are unique.
     * For instance, if there are 8 bytes in the array and all 8 are different,
     * this method would return 8.  If there are 8 repeated bytes, this method
     * would return 1.
     * @param bytes the array to check for uniqueness.
     * @return the number of different byte values in the array
     */
    public static int countUniqueBytes(byte[] bytes) {
        
        int[] counts = new int[NUM_BYTES_POSSIBLE];
        int numUnique = 0;
        for (byte b: bytes) {
            int index = 0xFF & b;
            if (counts[index] == 0) {
                numUnique++;
            }
            counts[index]++;
        }
        return numUnique;
    }
    
   
}