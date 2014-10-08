package hlrparse;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Andrew
 */
public class HLRParse {
    private final static Logger log = Logger.getLogger(HLRParse.class.getSimpleName());
    private static String path = "";
    Map s;
    
    public static void main(String[] args) {
        // set settings for log
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");
        System.setProperty("current.date", dateFormat.format(new Date()));
        // set path
        if (args.length > 1) path = args[1];
        File cfgFile = new File(args[0]);
        PropertyConfigurator.configure(cfgFile.getAbsolutePath());
        
        File[] files = getFilesInDir(path);
        Map<Integer, Stat> map2 = new HashMap<>();
        for (File file : files) {
            List<Stat> stats = read(file.getAbsolutePath());
            for (Stat stat : stats) {
                if (map2.containsKey(stat.hashCode()))
                    map2.get(stat.hashCode()).increment();
                else
                    map2.put(stat.hashCode(), stat);
            }
        }

        map2 = sortByComparator(map2); // sort by num
        List<Stat> list = new ArrayList<Stat>(map2.values());
        log.debug(getFormattedMap(list));
        //Collections.sort(list, new StatComparator());
        Collections.sort(list, Stat.ornComparator);
        log.debug(getFormattedMap(list));
        
    }
    
    private static File[] getFilesInDir(String path) {
        File[] files = new File(path).listFiles(new FilenameFilter() {
            @Override public boolean accept(File directory, String fileName) {
                return fileName.startsWith("hlr.log");
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
    
    // Чтение локального файла построчно
    private static List<Stat> read(String filename) {
        System.out.println("Read " + filename);
        List<Stat> list = new ArrayList<>();
        if (filename.endsWith(".log")) list.addAll(readTxtFile(filename));
        if (filename.endsWith(".bz2")) {
            list.addAll(readBZ2(filename));
        }

        return list;
    }
    
    private static List<Stat> readTxtFile(String filename) {
        List<Stat> list = new ArrayList<>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            while (in.ready()) {
                String s = in.readLine();
                list.addAll(parseString(s));
            }
            in.close();
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
        return list;
    }
    
    private static List<Stat> parseString(String s) {
        List<Stat> list = new ArrayList<>();
        if (s.contains("hlr:findProvider") && s.contains("HlrResponse = HlrResponse{")) {
            //System.out.println("Parse string " + s);
            s = s.replace("}", "");
            String[] splitted = s.substring(s.indexOf("{") + 1).split(", ");
            //for (int i = 0; i < splitted.length; i++) System.out.println(splitted[i]);
            list.add(new Stat(splitted));
        }

        return list;
    }
    
    private static List<Stat> readBZ2(String filename) {
        List<Stat> list = new ArrayList<>();
        try {
            FileInputStream in = new FileInputStream(filename);
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
            
            ByteArrayOutputStream  writer = new ByteArrayOutputStream();
            IOUtils.copy(bzIn, writer, 2048);
            String theString = writer.toString();
            bzIn.close();
            
            String[] s = theString.split("\n");
            for (String item : s) list.addAll(parseString(item));
        } catch (Exception ex) { System.err.println(ex);  }
        
        return list;
    }
    
    private static String getFormattedMap(List<Stat> map) {
        Formatter fmt = new Formatter();
        int maxOrnLen = "ORN".length();
        int maxOcnLen = "OCN".length();
            
        // ищем длину максимального параметра
        for (Stat stat : map) { 
            if (maxOrnLen < stat.orn.length()) maxOrnLen = stat.orn.length();
            if (maxOcnLen < stat.ocn.length()) maxOcnLen = stat.ocn.length();
        }
        String ornLen = "%-" + maxOrnLen + "s ";
        String ocnLen = "%-" + maxOcnLen + "s ";
        
        fmt.format(ornLen + ocnLen + "%s\n", "ORN", "OCN", "Number");
        // добавляем разделитель ---
        for (int i = 0; i <= maxOrnLen + maxOcnLen + "Number".length(); i++) fmt.format("-");
        fmt.format("\n");
        
        // печатаем
        for (Stat stat : map)   
            fmt.format(ornLen + ocnLen + "%s\n", stat.orn, stat.ocn, stat.num);
        
        return fmt.toString();
    }
}
