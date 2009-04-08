package Harvester;

/**
 * This class holds various utility methods we need.
 * @author Nick
 */
public class Utility {
    
    
    /**
     * Given a String of 1's and 0's, returns a boolean array that corresponds.
     * @param pattern a String of 1's and 0's.  A '1' at position i indicates 
     * that the column with index i should be written to file
     * @return a boolean array that corresponds with the pattern in the bit string.
     */
    public static final boolean[] fromBitString(String pattern) {
        char[] charArray = pattern.toCharArray();
        boolean[] vals = new boolean[charArray.length];

        for (int i = 0; i < vals.length; i++) {
            if (charArray[i] == '1') {
                vals[i] = true;
            }
            else {
                vals[i] = false;
            }
        }
        return vals;
    }
    
    /**
     * Given an array of booleans, returns a String whose ith character is
     * '1' if the ith entry in pattern is true, else '0'.
     * @param pattern the boolean array that corresponds.
     * @return a a String whose ith character is
     * '1' if the ith entry in pattern is true, else '0'.
     */
    public static final String toBitString(boolean[] pattern) {
        StringBuilder builder = new StringBuilder();
        for (boolean b : pattern) {
            builder.append(b ? '1' : '0');
        }
        return builder.toString();
    }
    
}