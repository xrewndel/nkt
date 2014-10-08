
package hlrparse;

import java.util.Comparator;

/**
 *
 * @author Andrew
 */
public class StatComparator implements Comparator<Stat> {
    @Override public int compare(Stat stat1, Stat stat2) {
        return stat1.orn.compareTo(stat2.orn); //ascending order
        //return stat2.orn.compareTo(stat1.orn); //descending order
    }
}
