package Harvester;


import java.io.*;
import java.security.*;
import java.util.LinkedList;
import java.util.List;

/**
 * This class does the MD5 hashing and extracts the bytes specified at a given
 * offset
 * @author Tucker Hermans - original implementation
 * @author Nicholas Dunn - added buffering so as to allow large files to be read
 * @date   November 14, 2008
 * 
 */
public class FileHashConverter
{
    // Constants
    private final static int DEFAULT_8_BYTE_START = 4000;
    private final static int BYTE_COUNT = 8; // The number of bytes to convert

    private final static int BUFFER_SIZE = (int) Math.pow(2, 25);
    
    public enum BASE_FORMAT {
        BASE_32, 
        BASE_16, // hex
        BASE_10, // decimal
        BASE_8   // octal
    }
    
    /**
     * Get the MD5 for the file
     *
     * @param input The file object to be looked at
     * @return The MD5 of the file
     */
    public static String getFileMD5Hash(String input) throws
        FileNotFoundException, IOException, NoSuchAlgorithmException
    {
        return getFileMD5Hash(new File(input));
    }

    
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
    
    
    /**
     * Get the MD5 for the file.
     * @see http://www.javalobby.org/java/forums/t84420.html
     *
     * @param input The file object to be looked at
     * @return The MD5 of the file
     */
    public static String getFileMD5Hash(File input) throws
        FileNotFoundException, IOException, NoSuchAlgorithmException
    {
        
        byte[] buffer = new byte[Math.min(BUFFER_SIZE, (int) input.length())];
         
        int read = 0;
        
        // Create an input stream from the file; we might not
        // be able to fit the whole file in memory
        FileInputStream fis = new FileInputStream(input);

        // Get an instance of the MD5 hashing algorithm
        MessageDigest algorithm = MessageDigest.getInstance("MD5");
        algorithm.reset();
        
        
        // Calculate the MD5 of the file
        
        // There's more to the file to be read
        while ( (read = fis.read(buffer)) != -1)  {
            algorithm.update(buffer, 0, read);
        }
        
        
        
        // The result of the hashing algorithm
        byte[] messageDigest = algorithm.digest();

        StringBuffer hexString = new StringBuffer();
        for (int i=0;i<messageDigest.length;i++) {
            hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
        }

        // Save our file MD5 hex
        return hexString.toString();
    }

    
    public static boolean canExtractBytes(File f, int offset, int numBytes) {
        //return f.length() >= offset + numBytes;
        //return f.length() >= numBytes;
        return f.length() > 0;
    }
    
    
    /**
     * Default hex calculation to file byte 4000 
     *
     * @param input The file to be evaluated
     * @return The hex values to the default 8 bytes
     */
    public static String getFileByteHex(File input)  throws
        FileNotFoundException, IOException, NoSuchAlgorithmException, Exception
    {
        return getFileByteHex(DEFAULT_8_BYTE_START, BYTE_COUNT, input);
    }

    /**
     * Default hex calculation to file byte 4000 (last 8 if file is too small)
     *
     * @param start How many bytes to skip before extracting bytes
     * @param numBytes how many bytes to explor
     * @param input The file to be evaluated
     * @return The hex values to the numBytes bytes starting at start
     */
    public static String getFileByteHex(int start, int numBytes, File input)  throws
        FileNotFoundException, IOException, NoSuchAlgorithmException, Exception
    {
        
        // Check to make sure the specified hex value is not too large
        if ( input.length() < start + numBytes -1) {
            //throw new Exception("File size too small for byte start point " +
              //                  start);
            return "Not enough bytes for this offset";
        }
        
        // Allocate enough space to read the numBytes bytes
        byte[] fileBytes = new byte[numBytes];
       
        // Create an input stream from the file
        FileInputStream fis = new FileInputStream(input);
        
        // We only care about the bytes starting at this point; ignore all those
        // bytes that come before it
        fis.skip(start);
        
        
        
        //  Read the specified bytes into the byte array
        int numBytesRead = fis.read(fileBytes, 0, numBytes);
        
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
        return hexString.toString();
    }

    /**
     * Get default formatted text output
     *
     * @param myFile File object to be used
     * @return The formatted combination of the MD5 with default Hex values
     */
    public static String getFormattedText(String myFile)  throws
        FileNotFoundException, IOException, NoSuchAlgorithmException, Exception
    {
        return getFormattedText(DEFAULT_8_BYTE_START, new File(myFile));
    }


    /**
     * Get default formatted text output
     *
     * @param myFile File object to be used
     * @return The formatted combination of the MD5 with default Hex values
     */
    public static String getFormattedText(File myFile) throws
        FileNotFoundException, IOException, NoSuchAlgorithmException, Exception
    {
        return getFormattedText(DEFAULT_8_BYTE_START, myFile);
    }

    /**
     * Get formatted text output for specifc byte position
     *
     * @param start The first byte of 8 to calculate the hex values of
     * @param myFile File object to be used
     * @return The formatted combination of the MD5 with default Hex values
     */
    public static String getFormattedText(int start, File myFile) throws
        FileNotFoundException, IOException, NoSuchAlgorithmException, Exception
    {
        return getFileMD5Hash(myFile).concat(", ").
            concat(getFileByteHex(start, BYTE_COUNT, myFile));
    }


    // Main method to test the program
    public static void main(String[] args)
    {
        for(String s : args) {
            try {
                System.out.println(getFormattedText(s));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }
}