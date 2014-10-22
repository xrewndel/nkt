package hlrparse;

import hlrparse.Stat.VALUES;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.Set;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.NodeList;

/**
 *
 * @author Andrew
 */
public class HLRParse {
    private final static Logger log = Logger.getLogger(outType.txt.name());
    private final static Logger csvLog = Logger.getLogger(outType.csv.name());
    private final static Logger csvAllLog = Logger.getLogger(outType.csvAll.name());
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
        
        List<Stat> bundle = new ArrayList<Stat>();
        File[] files = getFilesInDir(path);
        Map<Integer, Stat> map2 = new HashMap<Integer, Stat>();
        for (File file : files) {
            List<Stat> stats = read(file.getAbsolutePath());
            bundle.addAll(stats);
            for (Stat stat : stats) {
                if (map2.containsKey(stat.hashCode()))
                    map2.get(stat.hashCode()).increment(stat);
                else
                    map2.put(stat.hashCode(), stat);
            }
        }
        //System.out.println("BUNDLE: " + bundle.size());
        //for (Stat stat : bundle) System.out.println(stat);

        map2 = sortByComparator(map2); // sort by num
        List<Stat> list = new ArrayList<Stat>(map2.values());
        log.debug(format(list, outType.txt));
        //Collections.sort(list, new StatComparator());
        Collections.sort(list, Stat.ornComparator);
        log.debug(format(list, outType.txt));
        csvLog.debug(format(list, outType.csv));
        csvAllLog.debug(format(bundle, outType.csvAll));
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
        List<Stat> list = new ArrayList<Stat>();
        if (filename.endsWith(".log")) list.addAll(readTxtFile(filename));
        if (filename.endsWith(".bz2")) list.addAll(readBZ2(filename));

        return list;
    }
    
    private static List<Stat> readTxtFile(String filename) {
        List<Stat> list = new ArrayList<Stat>();
        List<String> strList = new ArrayList<String>();
        try {
            BufferedReader in = new BufferedReader(new FileReader(filename));
            while (in.ready()) {
                // for string
                //String s = in.readLine();
                //list.addAll(parseString(s));
                
                //for xml
                strList.add(in.readLine());
            }
            in.close();
            list.addAll(extractXml(strList));
        } catch (Exception ex) {
            System.out.println(ex);
        }
        
        //extractXmlTxt(filename);
        
        return list;
    }
    
//    private static List<Stat> extractXmlTxt(String filename) {
//        List<Stat> list = new ArrayList<Stat>();
//        List<String> strList = new ArrayList<String>();
//        try {
//            BufferedReader in = new BufferedReader(new FileReader(filename));
//            while (in.ready()) {
//                strList.add(in.readLine());
//            }
//            in.close();
//            list.addAll(extractXml(strList));
//        } catch (Exception ex) {
//            System.out.println(ex);
//        }
//        
//        return list;
//    }
    
//    private static List<Stat> extractXmlBZ2(String filename) {
//        List<Stat> list = new ArrayList<Stat>();
//        try {
//            FileInputStream in = new FileInputStream(filename);
//            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
//            
//            ByteArrayOutputStream  writer = new ByteArrayOutputStream();
//            IOUtils.copy(bzIn, writer, 2048);
//            String theString = writer.toString();
//            bzIn.close();
//            
//            String[] str = theString.split("\n");
//            List<String> strList = new ArrayList<String>(Arrays.asList(str));
//            list.addAll(extractXml(strList));
//        } catch (Exception ex) { System.err.println(ex);  }
//        
//        return list;
//    }
    
    private static List<Stat> extractXml(List<String> str) {
        List<Stat> list = new ArrayList<Stat>();
        StringBuilder xmlAsString = new StringBuilder();
        boolean xml = false;
        
        for (String s : str) {
            if (s.startsWith("<hlr>")) { 
                xml = true; 
                xmlAsString = new StringBuilder(); 
            }
            if (s.equals("</hlr>")) {
                xml = false;
                xmlAsString.append(s);
                // parse xml
                //System.out.println("XML: " + xmlAsString);
                Stat stat = parseXml(xmlAsString.toString());
                //System.out.println(stat);
                list.add(stat);
            }

            if (xml) xmlAsString.append(s);
        }
            
        return list;
    }
    
    private static Stat parseXml(String str) {
        Stat stat = new Stat();
        try {
            ByteArrayInputStream stream = new ByteArrayInputStream(str.getBytes());
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = builder.parse(stream);
            NodeList nodeList = doc.getDocumentElement().getChildNodes();
            stat = Stat.init(nodeList);
        } catch(Exception ex) {  
            System.out.println(ex);  
        }
        
        return stat;
    }
    
    private static List<Stat> parseString(String s) {
        List<Stat> list = new ArrayList<Stat>();
        if (s.contains("hlr:findProvider") && s.contains("HlrResponse = HlrResponse{")) {
            //System.out.println("Parse string " + raw);
            s = s.replace("}", "");
            String[] splitted = s.substring(s.indexOf("{") + 1).split(", ");
            //for (int i = 0; i < splitted.length; i++) System.out.println(splitted[i]);
            list.add(new Stat(splitted));
        }

        return list;
    }
    
    private static List<Stat> readBZ2(String filename) {
        List<Stat> list = new ArrayList<Stat>();
        try {
            FileInputStream in = new FileInputStream(filename);
            BZip2CompressorInputStream bzIn = new BZip2CompressorInputStream(in);
            
            ByteArrayOutputStream  writer = new ByteArrayOutputStream();
            IOUtils.copy(bzIn, writer, 2048);
            String theString = writer.toString();
            bzIn.close();
            
            String[] arr = theString.split("\n");
            // for string
            //for (String item : arr) list.addAll(parseString(item));
            
            // for xml
            List<String> strList = new ArrayList<String>(Arrays.asList(arr));
            list.addAll(extractXml(strList));
        } catch (Exception ex) { System.err.println(ex);  }
        
        //extractXmlBZ2(filename);
        
        return list;
    }
    
    private static String format(List<Stat> stats, outType type) {
        StringBuilder sb;
        switch(type) {
            case csv:
                sb = new StringBuilder();
                sb.append("ORN;").append("OCN;").append("Number;").append("Phones").append("\n");
                for (Stat stat : stats)   
                    sb.append(stat.get(VALUES.orn)).append(";")
                            .append(stat.get(VALUES.ocn)).append(";")
                            .append(stat.num).append(";")
                            .append(setAsSCV(stat.phones))
                            .append("\n");
                return sb.toString();
                
            case csvAll:
                sb = new StringBuilder().append(VALUES.asString()).append("\n");
                for (Stat stat : stats) {   
                    for (VALUES v : VALUES.params.values()) {
                        sb.append(stat.get(v)).append(";");
                    }
                    sb.append("\n");
                }
                return sb.toString();
            
            default:
                Formatter fmt = new Formatter();
                int maxOrnLen = "ORN".length();
                int maxOcnLen = "OCN".length();

                // ищем длину максимального параметра
                for (Stat stat : stats) { 
                    if (maxOrnLen < stat.get(VALUES.orn).length()) maxOrnLen = stat.get(VALUES.orn).length();
                    if (maxOcnLen < stat.get(VALUES.ocn).length()) maxOcnLen = stat.get(VALUES.ocn).length();
                }
                String ornLen = "%-" + maxOrnLen + "s ";
                String ocnLen = "%-" + maxOcnLen + "s ";
                String numLen = "%-" + "Number".length() + "s ";

                fmt.format(ornLen + ocnLen + numLen + "%s\n", "ORN", "OCN", "Number", "Phones");
                // добавляем разделитель ---
                for (int i = 0; i <= maxOrnLen + maxOcnLen + "Number".length() + "Phones".length(); i++) 
                    fmt.format("-");
                fmt.format("\n");

                // печатаем
                for (Stat stat : stats)   
                    fmt.format(ornLen + ocnLen + numLen + "%s\n", stat.get(VALUES.orn), stat.get(VALUES.ocn), stat.num, stat.phones.toString());

                return fmt.toString();
        }
    }
    
    private static String setAsSCV(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        for (String s : set) sb.append(s).append(";");
        return sb.toString();
    }
    
    private enum outType { txt, csv, csvAll; }
}
