package gastarter;

import gastarter.ParamsParser.CMD;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 * @author Andrew
 * 2 300 10 10 50 1 3 10
 */
public class GAStarter {
    private static ExecutorService pool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() - 1);
    private static ParamsParser params;
    
    public static void main(String[] args) throws InterruptedException {
        params = new ParamsParser(args);
        
        if (args.length == 0) {
            params.help();
            System.exit(1);
        }
        
        System.out.print("Cores: " + Runtime.getRuntime().availableProcessors() + "\n");
        
        // формируем неизменную часть строки запуска
        String begin = params.fixed();
        if (params.crossover() && params.mutation()) {
            for (int cross = params.crossBegin; cross < params.crossEnd; cross += params.crossStep) {
                for (int mutate = params.crossBegin; mutate <= params.mutateEnd; mutate += params.mutateStep) {
                    String cmd = begin + CMD.f.cmd() + params.files + " 20 " + CMD.cp.cmd() + cross + CMD.mp.cmd() + mutate; // хз что 20 такое. разобраться
                    execute(cmd);
                }
            }
        }
        else if (params.crossover()) {
            
        }
        else if (params.mutation()) {
            if(params.fr() && params.wr()) {
                for (double freeRate = params.frb; freeRate < params.fre; freeRate += params.frs) {
                    for (double wasteRate = params.wrb; wasteRate <= params.wre; wasteRate += params.wrs) {
                        String cmd = begin + CMD.fr.cmd() + round(freeRate) + CMD.wr.cmd() + round(wasteRate);
                        execute(cmd);
                    }
                }
            }
            else if(params.fr() && !params.wr()) {
                for (double freeRate = params.frb; freeRate < params.fre; freeRate += params.frs) {
                    String cmd = begin + CMD.fr.cmd() + round(freeRate);
                    execute(cmd);
                }
            }
            else if(!params.fr() && params.wr()) {
                for (double wasteRate = params.wrb; wasteRate <= params.wre; wasteRate += params.wrs) {
                    String cmd = begin + CMD.wr.cmd() + round(wasteRate);
                    execute(cmd);
                }
            }
        }
        
        pool.shutdown();
    }
    
    private static void execute(String exec) {
        for (int i = 0; i < params.repeat; i++) {
            Runnable task = new Task(exec, params.test);
            pool.execute(task);
            if (!params.test) try { Thread.sleep(1001); } catch (InterruptedException ex) {
                System.out.println(ex);
                ex.printStackTrace();
            }
        }
    }
    
    private static DecimalFormat format() {
        DecimalFormatSymbols decimalFormatSymbols = new DecimalFormatSymbols();
        decimalFormatSymbols.setDecimalSeparator('.');
        //decimalFormatSymbols.setGroupingSeparator(',');
        return new DecimalFormat("#,#0.0", decimalFormatSymbols);
    }
    
    private static String round(double value) {
        return "" + Math.round(value * 100.0 ) / 100.0;
    }
}
