package gastarter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew
 GA:
 -cfg config (ga3.cfg)
 -c crossover (0 - no, 1 - yes)
 -m mutation (0 - no, 1 - yes)
 -p population size
 -g generations
 -f work files 

 GAStarter:
 -r repeat
 -cb crossover begin(start)
 -cs crossover step
 -ce crossover end
 -mb mutation begin(start)
 -ms mutation step
 -me mutation end
 -frb mutation free rate begin
 -frs mutation free rate step
 -fre mutation free rate end
 -wrb mutation waste rate begin
 -wrs mutation waste rate step
 -wre mutation waste rate end
 -t test (1 or 0). If test - just print task
 */
public class ParamsParser {
    // по умолчанию все отключено
    public static String begin = "java -jar GA.jar";
    public final String cfgDefault = "ga3.cfg";
    public  String cfg = "ga3.cfg";
    public int crossover = 0;
    public int mutation = 0; 
    public int population = 50;
    public int generation  = 1000;
    public int files = 300;
    public int repeat = 3;
    
    public int crossBegin = 0;
    public int crossStep = 1;
    public int crossEnd = 0;
    
    public int mutateBegin = 0;
    public int mutateStep = 1;
    public int mutateEnd = 0;
    
    public double frb = 0d;
    public double frs = 0d;
    public double fre = 0d;
    
    public double wrb = 0d;
    public double wrs = 0d;
    public double wre = 0d;
    
    public boolean test = false;
    
    public ParamsParser (String[] args) {
        if (args.length % 2 > 0) throw new RuntimeException("Every param must has its value");
        
        for (int param = 0; param < args.length; param++) {
            CMD cmd = CMD.fromString(args[param]);
            String val = "";
            try { val = args[param + 1]; } catch (Exception ex) {}
            switch(cmd) {
                case h:     help();                             break;
                case cfg:   cfg = val;                          break;
                case c:     crossover = Integer.valueOf(val);   break;
                case m:     mutation = Integer.valueOf(val);    break;
                case f:     files = Integer.valueOf(val);       break;
                case p:     population = Integer.valueOf(val);  break;
                case g:     generation = Integer.valueOf(val);  break;
                case r:     repeat = Integer.valueOf(val);      break;
                case cb:    crossBegin = Integer.valueOf(val);  break;
                case cs:    crossStep = Integer.valueOf(val);   break;
                case ce:    crossEnd = Integer.valueOf(val);    break;
                case mb:    mutateBegin = Integer.valueOf(val); break;
                case ms:    mutateStep = Integer.valueOf(val);  break;
                case me:    mutateEnd = Integer.valueOf(val);   break;
                case frb:   frb = Double.valueOf(val);          break;
                case frs:   frs = Double.valueOf(val);          break;
                case fre:   fre = Double.valueOf(val);          break;
                case wrb:   wrb = Double.valueOf(val);          break;
                case wrs:   wrs = Double.valueOf(val);          break;
                case wre:   wre = Double.valueOf(val);          break;
                case wr:    wrb = frb; wrs = frs; wre = fre;    break;  // копируем интервал
                case t:     test = val.equals("1");             break;
                default: {
                    System.out.println("Paramter \"" + args[param] + "\" is unknown");
                    System.exit(1);
                }
            }
            param++;
        }
        
        validate();
    }
    
    public boolean crossover() { return crossover == 1; }
    public boolean mutation() { return mutation == 1; }
    public boolean fr() { return frb > 0 && fre > 0; }
    public boolean wr() { return wrb > 0 && wre > 0; }
    public boolean cross() { return crossBegin >0 && crossEnd > 0; }
    public boolean mutate() { return mutateBegin >0 && mutateEnd > 0; }
    public String fixed() {
        StringBuilder sb = new StringBuilder();
        sb.append(begin);
        if (!cfg.equals(cfgDefault)) sb.append(CMD.cfg.cmd()).append(cfg);
        sb.append(CMD.c.cmd()).append(crossover);
        sb.append(CMD.m.cmd()).append(mutation);
        
         return sb.toString();
    }
    private void validate() {
        if (files < 0) throw new RuntimeException(CMD.f.param() + " < 0");
        if (population < 0) throw new RuntimeException(CMD.p.param() + " < 0");
        if (generation < 0) throw new RuntimeException(CMD.g.param() + " < 0");
        if (repeat < 0) throw new RuntimeException(CMD.r.param() + " < 0");
        
        if (crossBegin < 0) throw new RuntimeException(CMD.cb.param() + " < 0");
        if (crossStep < 0) throw new RuntimeException(CMD.cs.param() + " < 0");
        if (crossEnd < 0) throw new RuntimeException(CMD.ce.param() + " < 0");
        if (crossEnd < crossBegin) throw new RuntimeException(CMD.ce.param() + " < " + CMD.cb.param());
        if (crossBegin > 0 && crossEnd > 0) crossover = 1;
        
        if (mutateBegin < 0) throw new RuntimeException(CMD.mb.param() + " < 0");
        if (mutateStep < 0) throw new RuntimeException(CMD.ms.param() + " < 0");
        if (mutateEnd < 0) throw new RuntimeException(CMD.me.param() + " < 0");
        if (mutateEnd < mutateBegin) throw new RuntimeException(CMD.me.param() + " < " + CMD.mb.param());
        if (mutateBegin > 0 && mutateEnd > 0) mutation = 1;
        
        if (frb < 0) throw new RuntimeException(CMD.frb.param() + " < 0");
        if (frs < 0) throw new RuntimeException(CMD.frs.param() + " < 0");
        if (fre < 0) throw new RuntimeException(CMD.fre.param() + " < 0");
        if (fre < frb) throw new RuntimeException(CMD.fre.param() + " < " + CMD.frb.param());
        if (fre > 0 && fre > 0) mutation = 1;
        
        if (wrb < 0) throw new RuntimeException(CMD.wrb.param() + " < 0");
        if (wrs < 0) throw new RuntimeException(CMD.wrs.param() + " < 0");
        if (wre < 0) throw new RuntimeException(CMD.wre.param() + " < 0");
        if (wre < wrb) throw new RuntimeException(CMD.wre.param() + " < " + CMD.wrb.param());
        if (wre > 0 && wre > 0) mutation = 1;
    }
    
    public final void help() {
        StringBuilder sb = new StringBuilder();
        sb.append(begin).append(CMD.help);
        sb.append("\n\tfrb, frs, fre, wrb, wrs, wre are doubles and specified like 0.01, 0.5");
        System.out.println(sb);
    }
    
    public enum CMD { 
        //GA
        cfg("-cfg", "\n\t-cfg file.cfg"),
        c("-c", "\n\t-c crossover"),
        m("-m", "\n\t-m mutation"),
        f("-f", "\n\t-f files"),
        p("-p", "\n\t-p populatio"),
        g("-g", "\n\t-g generations"),
        cp("-cp", ""), // crossRate
        mp("-mp", ""), // mutateRate
        fr("-fr", ""), // freeRate
        wr("-wr", ""), // wasteRate
        
        // GA Starter
        r("-r", "\n\t-r repeat"),
        cb("-cb", "\n\n\t-cb crossover begin(start)"),
        cs("-cs", "\n\t-cs crossover step"),
        ce("-ce", "\n\t-ce crossover end"),
        mb("-mb", "\n\n\t-mb mutation begin(start)"),
        ms("-ms", "\n\t-ms mutation step"),
        me("-me", "\n\t-me mutation end"),
        frb("-frb", "\n\n\t-frb mutation free rate begin"),
        frs("-frs", "\n\t-frs mutation free rate step"),
        fre("-fre", "\n\t-fre mutation free rate end"),
        wrb("-wrb", "\n\n\t-wrb mutation waste rate begin"),
        wrs("-wrs", "\n\t-wrs mutation waste rate step"),
        wre("-wre", "\n\t-wre mutation waste rate end"),
        t("-t", "\n\n\t-t test"),
        h("-h", ""),
        undef ("", "");
        
        private final String name;
        private final String hlp;
        CMD(String n, String h) { name = n; hlp = h; }
        private static final Map<String, CMD> params = new HashMap<>();
        static { for (CMD parameter : values()) params.put(parameter.name, parameter); }
        
        private static final StringBuilder help = new StringBuilder();
        static { for (CMD parameter : values()) help.append(parameter.hlp); }
        
        private static CMD fromString(String name) {
            if (params.containsKey(name)) return params.get(name);
            else return CMD.undef;
        }
        
        //public String hlp() { return hlp; }
        public String cmd() { return " " + name + " "; }
        public String param() { return name; }
    }
}
