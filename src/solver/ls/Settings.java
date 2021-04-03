package solver.ls;

import java.util.Random;

public class Settings {
    static Random rand = new Random(0);
    static int verbosity = 0;

    public static void print() {
        System.out.println("Settings:");
        System.out.println("  Verbosity: " + verbosity);
    }
}
