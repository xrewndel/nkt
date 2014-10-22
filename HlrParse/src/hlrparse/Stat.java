package hlrparse;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Andrew
 */
public class Stat implements Comparable<Stat> {
    public enum VALUES {
        destination, id, stat, IMSI, err, orn, pon, ron, mccmnc, rcn, ppm, onp,
        ocn, ocp, is_ported, rnp, rcp, is_roaming, pnp, pcn, pcp, unknw;
        
        public static final Map<String, VALUES> params = new TreeMap<String, VALUES>();
        static { 
            for (VALUES parameter : values()) params.put(parameter.name(), parameter); 
            params.remove(unknw.name());
        }
        
        private static VALUES fromString(String name) {
            if (params.containsKey(name)) return params.get(name);
            else return unknw;
        }
        
        public static String asString() {
            StringBuilder sb = new StringBuilder();
            for (String s : params.keySet()) sb.append(s).append(";");
            return sb.toString();
        }
    }
    
    public int num = 1;
    Set<String> phones = new TreeSet<String>();
    Map<VALUES, String> data = new HashMap<VALUES, String>();
    
    public Stat () {}
    
    public Stat(String[] s) { 
        for (String item : s) {
            String[] vals = item.split("=");
            setValue(vals[0], vals.length == 2 ? vals[1] : "");
        }
    }
    
    public static Stat init(NodeList nodeList) { 
        Stat stat = new Stat();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            //System.out.println("NODE: " + node.getNodeName() + " = " + node.getTextContent());
            stat.setValue(node.getNodeName(), node.getTextContent());
        }
        
        return stat;
    } 
    
    private void setValue(String name, String val) {
        VALUES value = VALUES.fromString(name);
        switch(value) {
            case destination: 
                phones.add(val);
                data.put(value, val);
                break; 
            case id: case stat: case IMSI: case err: case orn: case pon: case ron:   
            case mccmnc: case rcn: case ppm: case onp: case ocn: case ocp: case is_ported: 
            case rnp: case rcp: case is_roaming: case pnp: case pcn: case pcp:   
                data.put(value, val); break; 
            default: System.out.println("Unknown value " + name);
        }
    }
    
    public String get(VALUES v) { 
        if (data.containsKey(v)) return data.get(v);
        else return "";
    }
    
    public void increment(Stat other) { num++; phones.addAll(other.phones); }

    @Override public int compareTo(Stat other) { return other.num  - this.num; }   //descending order (most filled on the top)
    //@Override public int compareTo(Stat other) { return this.num - other.num; }      //ascending order (most filled is last)
    
    public static Comparator<Stat> ornComparator = new Comparator<Stat>() {
        @Override public int compare(Stat stat1, Stat stat2) {
          return stat1.get(VALUES.orn).compareTo(stat2.get(VALUES.orn)); //ascending order
          //return stat2.orn.compareTo(stat1.orn); //descending order
        }
    };
    
    public static Comparator<Stat> numComparator = new Comparator<Stat>() {
        @Override public int compare(Stat stat1, Stat stat2) {
          return stat1.num - stat2.num; //ascending order
          //return stat2.num - stat1.num; //descending order
        }
    };
    
    @Override public String toString() {
        StringBuilder sb = new StringBuilder().append("Stat{");
        for (VALUES v : VALUES.params.values()) {
            sb.append(v.name()).append("=").append(get(v)).append(", ");
        }
        sb.replace(sb.lastIndexOf(", "), sb.length(), "");
        sb.append("}");
        
    return "Stat{" + "destination=" + get(VALUES.destination) + ", id=" + get(VALUES.id) + ", stat=" + get(VALUES.stat) + 
            ", IMSI=" + get(VALUES.IMSI) + ", err=" + get(VALUES.err) + ", orn=" + get(VALUES.orn) + ", pon=" + get(VALUES.pon) + 
            ", ron=" + get(VALUES.ron) + ", mccmnc=" + get(VALUES.mccmnc) + ", rcn=" + get(VALUES.rcn) + ", ppm=" + get(VALUES.ppm) + 
            ", onp=" + get(VALUES.onp) + ", ocn=" + get(VALUES.ocn) + ", ocp=" + get(VALUES.ocp) + ", is_ported=" + get(VALUES.is_ported) + 
            ", rnp=" + get(VALUES.rnp) + ", rcp=" + get(VALUES.rcp) + ", is_roaming=" + get(VALUES.is_roaming) + 
            ", pnp=" + get(VALUES.pnp) + ", pcn=" + get(VALUES.pcn) + ", pcp=" + get(VALUES.pcp) + '}';
    }
    
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + this.get(VALUES.orn).hashCode();
        hash = 71 * hash + this.get(VALUES.ocn).hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        
        final Stat other = (Stat) obj;
        if (!this.get(VALUES.orn).equals(other.get(VALUES.orn))) return false;
        if (!this.get(VALUES.ocn).equals(other.get(VALUES.ocn))) return false;
        
        return true;
    }
}
