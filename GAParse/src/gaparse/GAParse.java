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
    private static String path = "D:\\projects\\ga\\dist\\log";
    private static List<Stat> stats = new LinkedList<>();
    Map s;
    
    public static void main(String[] args) {
        int cross = -1;
        int mutate = -1;
        int fr = -1;
        int wr = -1;
        Double bestFitness = 0d;
        int bestfr = fr;
        int bestwr = wr;
        String bestFile = "";
        // set settings for log
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        System.setProperty("current.date", dateFormat.format(new Date()));
        File cfgFile = new File(args[0]);
        PropertyConfigurator.configure(cfgFile.getAbsolutePath());
        
        // set path
        if (args.length > 1) path = args[1];

        File[] files = getFilesInDir(path);
        for (File file : files) {
            //log.info("#" + files[i].getName().indexOf("_"));
            log.info("File: " + file.getName());
            String[] t = file.getName().substring(23).replaceAll(".log", "").split("-");
            for (String t1 : t) {
                String[] q = t1.split("_");
                switch(q[0]) {
                    case "c": cross = Integer.valueOf(q[1]);    break;
                    case "cross": cross = Integer.valueOf(q[1]);    break;
                    case "m": mutate = Integer.valueOf(q[1]);   break;
                    case "mutate": mutate = Integer.valueOf(q[1]);   break;
                    case "fr": fr = Integer.valueOf(q[1]);      break;
                    case "wr": wr = Integer.valueOf(q[1]);      break;
                    default: System.out.println("Param " + q[0] + " is uknown");
                }
            }
            log.info("Cross: " + cross);
            log.info("Mutate: " + mutate);
            log.info("Free rate: " + fr);
            log.info("Waste rate: " + wr);
            
            
            // среднее считать - за сколько поколений в среднем достигается максимальнй фитнес - проще в расчетах писать и пихать в файл
            try {
                Map map = read(file.getAbsolutePath());
                log.info("Map: " + map);
                List<Double> fitness = (List<Double>) map.get("fitness");
                log.info("Fitness: " + fitness);
                for (Double fit : fitness) {
                    if (fit > bestFitness) {
                        bestFitness = fit;
                        bestFile = file.getAbsolutePath();
                        if (fr == -1) {
                            double d = (double) map.get("fr");
                            System.out.println("d1: " + d);
                            //bestfr = (int) map.get("fr");
                            bestfr = (int) (d * 100);
                            System.out.println("bestfr: " + bestfr);
                        } 
                        else bestfr = fr;
                        
                        if (wr == -1) {
                            double d2 = (double) map.get("wr");
                            System.out.println("d2: " + d2);
                            //bestwr = (int) map.get("wr");
                            bestwr = (int) (d2 * 100);
                            System.out.println("bestwr: " + bestwr);
                        } else bestwr = wr;
                    }
                }
                
                Stat stat = new Stat(cross, mutate);
                if (!stats.contains(stat)) { 
                    stat.add((List<Integer>)map.get("gen"), (List<Integer>)map.get("time"));
                    stats.add(stat); }
                else {
                    for (Stat st : stats) {
                        if (st.equals(stat)) {
                            st.add((List<Integer>)map.get("gen"), (List<Integer>)map.get("time"));
                        }
                    }
                }
            }catch (IOException ex) { log.info(ex);  }
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
        log.info("Best fitness: " + bestFitness + ". Fr: " + bestfr + ". Wr: " + bestwr + ". File " + bestFile);
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
    public static Map read(String filename) throws IOException {
        List<Integer> gen = new ArrayList<>();
        List<Integer> time = new ArrayList<>();
        List<Double> fitness = new ArrayList<>();
        double fr = -1d;
        double wr = -1d;

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
            if (s.startsWith("Fitness"))  fitness.add(Double.valueOf(s.substring(s.indexOf("=") + 1).trim()));
            if (s.startsWith("Mutate free rate"))  fr = Double.valueOf(s.substring(s.indexOf(":") + 1).trim());
            if (s.startsWith("Mutate waste rate"))  wr = Double.valueOf(s.substring(s.indexOf(":") + 1).trim());
        }
        in.close();
        
        Map map = new HashMap<>();
        map.put("gen", gen);
        map.put("time", time);
        map.put("fitness", fitness);
        map.put("fr", fr);
        map.put("wr", wr);
        return map;
    }
    
    public static String arrToStr(String[] arr) {
        StringBuilder sb = new StringBuilder();
        if (arr.length % 2 == 0)
            for (int i = 0; i < arr.length; i++) {
                sb.append(arr[i]);
                if (i % 2 == 0) sb.append("\n");
                else sb.append(": ");
            }
        else 
            for (String arr1 : arr) {  sb.append(arr1).append(" "); }
        return sb.toString();
    }
}
