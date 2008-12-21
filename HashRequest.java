
package Harvester;

/**
 *
 * @author Nick
 */
public class HashRequest {
    private String algorithmName;
    // In what base do you want the string representation of the hash returned?
    private int radix;
    
    public HashRequest(String algorithmName, int radix) {
        this.algorithmName = algorithmName;
        this.radix = radix;
    }
    
    public String getName() { return algorithmName; }
    
    public int getRadix() { return radix; }
    
}
