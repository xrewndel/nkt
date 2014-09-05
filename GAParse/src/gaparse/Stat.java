package gaparse;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Andrew
 */
public class Stat {
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
