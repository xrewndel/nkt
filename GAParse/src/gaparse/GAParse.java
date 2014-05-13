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
                Map<String, List<Integer>> map = read(files[i].getAbsolutePath());
                log.info("Fitess: " + map);
                
                Stat stat = new Stat(cross, mutate);
                if (!stats.contains(stat)) { 
                    stat.add(map.get("gen"), map.get("time"));
                    stats.add(stat); }
                else {
                    for (Stat st : stats) {
                        if (st.equals(stat)) {
                            st.add(map.get("gen"), map.get("time"));
                        }
                    }
                }
            } catch (IOException ex) { log.info(ex);  }
            
            
        }
        
        Map<String, Double> mapGen = new HashMap<>();
        for (Stat st : stats) {
            log.info("Stat: " + st.toString());
            log.info("Sum: " + st.sumGen());
            log.info("Avg: " + st.avgGen());
            mapGen.put(st.id(), st.avgGen());
        }
        Map<String, Double> sortedMapGen = sortByComparator(mapGen);
        
        Map<String, Double> mapTime = new HashMap<>();
        for (Stat st : stats) {
            mapTime.put(st.id(), st.avgTime());
        }
        Map<String, Double> sortedMapTime = sortByComparator(mapTime);
        
        log.info("Map: " + mapGen);
        log.info("");
        log.info("Sorted by generation (from best to worst): "); printMap(sortedMapGen);
        log.info("");
        log.info("Sorted by time, sec (from best to worst): "); printMap(sortedMapTime);
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
        for (String s : map.keySet()) { log.info(s + ": " + map.get(s)); }
    }
    
    // Чтение локального файла построчно
    public static Map<String, List<Integer>> read(String filename) throws IOException {
        List<Integer> gen = new ArrayList<>();
        List<Integer> time = new ArrayList<>();

        BufferedReader in = new BufferedReader(new FileReader(filename));
        while (in.ready()) {
            String s = in.readLine();
            if (s.startsWith("2."))  gen.add(Integer.valueOf(s.substring(s.indexOf(" ")).trim()));
            if (s.startsWith("Exec time:")) {
                String[] minSec = s.substring(11).trim().replaceAll("m", "").replaceAll("sec", "").split(" ");
                //System.out.println("Min:" + minSec[0]);
                //System.out.println("Sec:" + minSec[1]);
                int total = Integer.valueOf(minSec[0]) * 60 + Integer.valueOf(minSec[1]);
                //System.out.println("Total:" + total);
                time.add(total);
            }
        }
        in.close();
        
        Map<String, List<Integer>> map = new HashMap<>();
        map.put("gen", gen);
        map.put("time", time);
        return map;
    }
    
    static class Stat {
        private int cross;
        private int mutate;
        private List<Integer> generation = new ArrayList<>();
        private List<Integer> time = new ArrayList<>();

        public Stat(int cr, int mut) { cross = cr; mutate = mut; }
        public Stat(int cr, int mut, int gen) {
            cross = cr;
            mutate = mut;
            generation.add(gen);
        }
        
        public void add(List<Integer> g, List<Integer> t) { generation.addAll(g); time.addAll(t);}
        public int sumGen() { 
            int sum = 0;
            for (Integer i : generation) { sum += i; }
            return sum;
        }
        public int sumTime() { 
            int sum = 0;
            for (Integer i : time) { sum += i; }
            return sum;
        }
        
        public double avgGen() { return (double)sumGen() / generation.size(); }
        public double avgTime() { return (double)sumTime() / time.size(); }
        public int size() { return generation.size(); }
        
        @Override public String toString() { return id() + ": " + generation.size() + "(" + generation + ")"; }
        public String id() { return cross + "/" + mutate; }
        
        @Override public int hashCode() {
            int hash = 5;
            hash = 17 * hash + this.cross;
            hash = 17 * hash + this.mutate;
            return hash;
        }

        @Override public boolean equals(Object obj) {
            if (obj == null) { return false; }
            if (getClass() != obj.getClass()) { return false; }
            final Stat other = (Stat) obj;
            if (this.cross != other.cross) { return false; }
            if (this.mutate != other.mutate) { return false; }
            return true;
        }
    }
    
    
}
