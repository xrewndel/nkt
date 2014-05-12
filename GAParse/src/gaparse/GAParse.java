package gaparse;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Andrew
 */
public class GAParse {
    private final static Logger log = Logger.getLogger(GAParse.class.getSimpleName());
    private static String path = "D:/tmp/ga";
    private static List<Stat> stats = new LinkedList<>();
    Map s;
    
    public static void main(String[] args) {
        // set settings for log
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        System.setProperty("current.date", dateFormat.format(new Date()));
        File cfgFile = new File(args[0]);
        PropertyConfigurator.configure(cfgFile.getAbsolutePath());
        
        // set path
        if (args.length > 1) path = args[1];

        File[] files = getFilesInDir(path);
        for (int i = 0; i < files.length; i++) {
            //log.info("#" + files[i].getName().indexOf("_"));
            log.info("File: " + files[i].getName());
            String[] s = files[i].getName().substring(29).replaceAll("_", "").replaceAll("mutate", "").replaceAll(".log", "").split("-");
            int cross = Integer.valueOf(s[0]);
            int mutate = Integer.valueOf(s[1]);
            log.info("Cross: " + cross);
            log.info("Mutate: " + mutate);
            
            try {
                List<Integer> l = read(files[i].getAbsolutePath());
                log.info("Fitess: " + l);
                
                Stat stat = new Stat(cross, mutate);
                if (!stats.contains(stat)) { stat.add(l); stats.add(stat); }
                else {
                    for (Stat st : stats) {
                        if (st.equals(stat)) st.add(l);
                    }
                }
            } catch (IOException ex) { log.info(ex);  }
            
            
        }
        
        Map<String, Double> map = new HashMap<String, Double>();
        for (Stat st : stats) {
            log.info("Stat: " + st.toString());
            log.info("Sum: " + st.sum());
            log.info("Avg: " + st.avg());
            map.put(st.id(), st.avg());
        }
        Map<String, Double> sortedMap = sortByComparator(map);
        
        log.info("Map: " + map);
        log.info("sortedMap (from best to worst): "); printMap(sortedMap);
    }
    
    public static File[] getFilesInDir(String path) {
        File[] files = new File(path).listFiles(new FilenameFilter() {
            @Override public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".log");
            }
        });
        
        return files;
    }
    
    private static Map sortByComparator(Map unsortMap) {
        List list = new LinkedList(unsortMap.entrySet());
 
        // sort list based on comparator
        Collections.sort(list, new Comparator() {
            @Override public int compare(Object o1, Object o2) {
                    return ((Comparable) ((Map.Entry) (o1)).getValue())
                           .compareTo(((Map.Entry) (o2)).getValue());
            }
        });

        // put sorted list into map again
        //LinkedHashMap make sure order in which keys were inserted
        Map sortedMap = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
                Map.Entry entry = (Map.Entry) it.next();
                sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }
    
    public static void printMap(Map<String, Double> map){
        for (String s : map.keySet()) { System.out.println(s + ": " + map.get(s)); }
    }
    
    // Чтение локального файла построчно
    public static List<Integer> read(String filename) throws IOException {
        List<Integer> l = new ArrayList<>();
        BufferedReader in = new BufferedReader(new FileReader(filename));

        while (in.ready()) {
            String s = in.readLine();
            if (s.startsWith("2.")) l.add(Integer.valueOf(s.substring(4).trim()));
        }
        in.close();
        
        return l;
    }
    
    static class Stat {
        private int idx;
        private int cross;
        private int mutate;
        private List<Integer> generation = new ArrayList<>();

        public Stat(int cr, int mut) { cross = cr; mutate = mut; }
        public Stat(int cr, int mut, int gen) {
            cross = cr;
            mutate = mut;
            generation.add(gen);
        }
        
        public void add(int val) { generation.add(val); }
        public void add(List<Integer> val) { generation.addAll(val); }
        public int sum() { 
            int sum = 0;
            for (Integer i : generation) { sum += i; }
            return sum;
        }
        
        public double avg() { return (double)sum() / generation.size(); }
        public int size() { return generation.size(); }
        
        @Override public String toString() { return "" + cross + "/" + mutate + ": " + generation.size() + "(" + generation + ")"; }
        public String id() { return "" + cross + "/" + mutate; }
        
        @Override public int hashCode() {
            int hash = 5;
            hash = 17 * hash + this.cross;
            hash = 17 * hash + this.mutate;
            return hash;
        }

        @Override public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Stat other = (Stat) obj;
            if (this.cross != other.cross) {
                return false;
            }
            if (this.mutate != other.mutate) {
                return false;
            }
            return true;
        }
    }
    
    
}
