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
    
    private String destination = "";
    private String id = "";
    private String stat = "";
    private String IMSI = "";
    private String err = "";
    private String orn = "";
    private String pon = "";
    private String ron = "";
    private String mccmnc = "";
    private String rcn = "";
    private String ppm = "";
    private String onp = "";
    private String ocn = "";
    private String ocp = "";
    private String is_ported = "";
    private String rnp = "";
    private String rcp = "";
    private String is_roaming = "";
    private String pnp = "";
    private String pcn = "";
    private String pcp = "";
    
    public int num = 1;
    Set<String> phones = new TreeSet<String>();
    //String[] raw;
    Map<VALUES, String> data = new HashMap<VALUES, String>();
    
    public Stat () {}
    
    public Stat(String[] s) { 
        //raw = s;
        for (String item : s) {
            String[] vals = item.split("=");
            /*
            if (vals[0].equals("destination") && vals.length == 2) {
                destination = vals[1].trim();
                phones.add(vals[1].trim());
            }
            if (vals[0].equals("id")   && vals.length == 2)         id = vals[1].trim();
            if (vals[0].equals("stat") && vals.length == 2)         stat = vals[1].trim();
            if (vals[0].equals("IMSI") && vals.length == 2)         IMSI = vals[1].trim();
            if (vals[0].equals("err")  && vals.length == 2)         err = vals[1].trim();
            if (vals[0].equals("orn")  && vals.length == 2)         orn = vals[1].trim();
            if (vals[0].equals("pon")  && vals.length == 2)         pon = vals[1].trim();
            if (vals[0].equals("ron")  && vals.length == 2)         ron = vals[1].trim();
            if (vals[0].equals("mccmnc") && vals.length == 2)       mccmnc = vals[1].trim();
            if (vals[0].equals("rcn")  && vals.length == 2)         rcn = vals[1].trim();
            if (vals[0].equals("ppm")  && vals.length == 2)         ppm = vals[1].trim();
            if (vals[0].equals("onp")  && vals.length == 2)         onp = vals[1].trim();
            if (vals[0].equals("ocn")  && vals.length == 2)         ocn = vals[1].trim();
            if (vals[0].equals("ocp")  && vals.length == 2)         ocp = vals[1].trim();
            if (vals[0].equals("is_ported") && vals.length == 2)    is_ported = vals[1].trim();
            if (vals[0].equals("rnp")  && vals.length == 2)         rnp = vals[1].trim();
            if (vals[0].equals("rcp")  && vals.length == 2)         rcp = vals[1].trim();
            if (vals[0].equals("is_roaming") && vals.length == 2)   is_roaming = vals[1].trim();
            if (vals[0].equals("pnp")  && vals.length == 2)         pnp = vals[1].trim();
            if (vals[0].equals("pcn")  && vals.length == 2)         pcn = vals[1].trim();
            if (vals[0].equals("pcp")  && vals.length == 2)         pcp = vals[1].trim();
            */
            setValue(vals[0], vals.length == 2 ? vals[1] : "");
        }
    }
    
    public static Stat init(NodeList nodeList) { 
        Stat stat = new Stat();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            //System.out.println("NODE: " + node.getNodeName() + " = " + node.getTextContent());
            stat.setValue(node.getNodeName(), node.getTextContent());
            //System.out.println("SETTED: " + stat.toString());
        }
        
        //System.out.println("INIT: " + stat.toString());
        return stat;
    } 
    
    private void setValue(String name, String val) {
        //System.out.println("Name:" + name);
        VALUES value = VALUES.fromString(name);
        //System.out.println("VALUE:" + value);
        switch(value) {
            case destination: 
                destination = val;
                phones.add(val);
                data.put(value, val);
                break; 
            case id:    id = val;   data.put(value, val); break; 
            case stat:  stat = val; data.put(value, val); break; 
            case IMSI:  IMSI = val; data.put(value, val); break;
            case err:   err = val;  data.put(value, val); break;
            case orn:   orn = val;  data.put(value, val); break;
            case pon:   pon = val;  data.put(value, val); break; 
            case ron:   ron = val;  data.put(value, val); break;
            case mccmnc: mccmnc = val; data.put(value, val); break; 
            case rcn:   rcn = val;  data.put(value, val); break; 
            case ppm:   ppm = val;  data.put(value, val); break; 
            case onp:   onp = val;  data.put(value, val); break; 
            case ocn:   ocn = val;  data.put(value, val); break; 
            case ocp:   ocp = val;  data.put(value, val); break; 
            case is_ported: is_ported = val; data.put(value, val); break; 
            case rnp:   rnp = val;  data.put(value, val); break; 
            case rcp:   rcp = val;  data.put(value, val); break; 
            case is_roaming: is_roaming = val; data.put(value, val); break; 
            case pnp:   pnp = val;  data.put(value, val); break; 
            case pcn:   pcn = val;  data.put(value, val); break; 
            case pcp:   pcp = val;  data.put(value, val); break; 
            default: //System.out.println("Unknown value " + name);
        }
        
        //System.out.println("THIS: " + stat.toString());
    }
    
    public String get(VALUES v) { 
        /*
        for (String item : raw) {
            String[] vals = item.split("=");
            VALUES value = VALUES.fromString(vals[0]);
            if (v.equals(value) && vals.length == 2)
                return vals[1].trim();
        }
        
        return "";
        */
        
        if (data.containsKey(v)) return data.get(v);
        else return "";
        
    }
    
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
    
    @Override public String toString() {
        return "Stat{" + "destination=" + destination + ", id=" + id + ", stat=" + stat + 
                ", IMSI=" + IMSI + ", err=" + err + ", orn=" + orn + ", pon=" + pon + 
                ", ron=" + ron + ", mccmnc=" + mccmnc + ", rcn=" + rcn + ", ppm=" + ppm + 
                ", onp=" + onp + ", ocn=" + ocn + ", ocp=" + ocp + ", is_ported=" + is_ported + 
                ", rnp=" + rnp + ", rcp=" + rcp + ", is_roaming=" + is_roaming + 
                ", pnp=" + pnp + ", pcn=" + pcn + ", pcp=" + pcp + '}';
    }
    
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
