package hlrparse;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Andrew
 */
public class Stat implements Comparable<Stat>{
    public String orn = "";
    public String ocn = "";
    public int num = 1;
    Set<String> phones = new TreeSet<String>();
    
    public Stat(String[] s) { 
        for (String item : s) {
            String[] vals = item.split("=");
            if (vals[0].equals("destination") && vals.length == 2) phones.add(vals[1].trim());
            if (vals[0].equals("orn") && vals.length == 2) orn = vals[1].trim();
            if (vals[0].equals("ocn") && vals.length == 2) ocn = vals[1].trim();
        }
    }
    public Stat(String _orn, String _ocn) { orn = _orn; ocn = _ocn; } // for test
    
    public void increment(Stat other) { num++; phones.addAll(other.phones); }

    @Override public int compareTo(Stat other) { return other.num  - this.num; }   //descending order (most filled on the top)
    //@Override public int compareTo(Stat other) { return this.num - other.num; }      //ascending order (most filled is last)
    
    public static Comparator<Stat> ornComparator = new Comparator<Stat>() {
        @Override public int compare(Stat stat1, Stat stat2) {
          return stat1.orn.compareTo(stat2.orn); //ascending order
          //return stat2.orn.compareTo(stat1.orn); //descending order
        }
    };
    
    public static Comparator<Stat> numComparator = new Comparator<Stat>() {
        @Override public int compare(Stat stat1, Stat stat2) {
          return stat1.num - stat2.num; //ascending order
          //return stat2.num - stat1.num; //descending order
        }
    };
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + this.orn.hashCode();
        hash = 71 * hash + this.ocn.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        final Stat other = (Stat) obj;
        if (!this.orn.equals(other.orn)) return false;
        if (!this.ocn.equals(other.ocn)) return false;
        
        return true;
    }
}
