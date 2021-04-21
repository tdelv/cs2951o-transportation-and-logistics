package solver.ls;

import java.util.Random;

public class Settings {
    // Overall
    static Random rand = new Random(0);
    static int verbosity = 0;

    // CP
    static boolean cpUseDistribute = false;
    static boolean cpReduceArrLength = true;

    // LS
    enum SearchLimit {
        dist,
        time,
        both
    }

    // VRP
    static SearchLimit vrpLimitBy = SearchLimit.dist;
    static int vrpSearchDist = 3;
    static double vrpSearchTime = 30.0;

    // TSP
    static SearchLimit tspLimitBy = SearchLimit.dist;
    static int tspSearchDist = 3;
    static double tspSearchTime = 3.0;

    // Rand Walk
    static double probRandWalk = 0.1;
    static double probRandWalkFactor = 0.95;

    public static void print() {
        System.out.println("Settings:");
        System.out.println("  Verbosity: " + verbosity);
        System.out.println("  CP:");
        System.out.println("    cpUseDistribute: " + cpUseDistribute);
        System.out.println("    cpReduceArrLength: " + cpReduceArrLength);
        System.out.println("  LS:");
        System.out.println("    vrp:");
        System.out.println("      vrpLimitBy: " + vrpLimitBy);
        System.out.println("      vrpSearchDist: " + vrpSearchDist);
        System.out.println("      vrpSearchTime: " + vrpSearchTime);
        System.out.println("    tsp:");
        System.out.println("      tspLimitBy: " + tspLimitBy);
        System.out.println("      tspSearchDist: " + tspSearchDist);
        System.out.println("      tspSearchTime: " + tspSearchTime);
        System.out.println("    rand walk:");
        System.out.println("      probRandWalk: " + probRandWalk);
        System.out.println("      probRandWalkFactor: " + probRandWalkFactor);
    }
}
