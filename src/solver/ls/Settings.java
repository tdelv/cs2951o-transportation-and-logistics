package solver.ls;

import java.util.Random;

public class Settings {
    // Overall
    static Random rand = new Random(0);
    static int verbosity = 0;

    // CP
    static boolean cpUseDistribute = false;
    static boolean cpReduceArrLength = true;

    public static void print() {
        System.out.println("Settings:");
        System.out.println("  Verbosity: " + verbosity);
        System.out.println("  CP:");
        System.out.println("    cpUseDistribute: " + cpUseDistribute);
        System.out.println("    cpReduceArrLength: " + cpReduceArrLength);
    }
}
