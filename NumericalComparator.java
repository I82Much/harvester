package Harvester;

import java.util.Comparator;

/**
 * This class allows Integers to be sorted by the
 * numerical value rather than the lexicographic order, as they are by default.
 * @date   January 16, 2008
 * @author Nick
 */
public class NumericalComparator implements Comparator<Integer> {
    
    public int compare(Integer o1, Integer o2) {
        return o1.intValue() - o2.intValue();
    }
    
}