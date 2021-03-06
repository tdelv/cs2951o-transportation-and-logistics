package solver.ls;

import java.util.Random;

public class Settings {
    // Overall
    static Random rand = new Random(0);
    static int verbosity = 0;
    static boolean feasibilityOnly = false;

    // LS
    enum SearchLimit {
        dist,
        time,
        both
    }
    static boolean lsTakeFirst = true;
    static boolean lsSearchProcedural = false;

    // VRP
    static SearchLimit vrpLimitBy = SearchLimit.time;
    static int vrpSearchDist = 3;
    static double vrpSearchTime = 30.0;

    // TSP
    enum TSPSearch {
        localSearch,
        cp,
        nearestNeighbor
    }
    static TSPSearch tspSearch = TSPSearch.nearestNeighbor;
    static SearchLimit tspLimitBy = SearchLimit.both;
    static int tspSearchDist = 3;
    static double tspSearchTime = 1.0;

    // Rand Walk
    static double probRandWalk = 0.1;
    static double probRandWalkFactor = 0.95;
    static double randMaxDist = 0.6;

    public static void print() {
        System.out.println("Settings:");
        System.out.println("  Verbosity: " + verbosity);
        System.out.println("  Check feasibility only: " + feasibilityOnly);
        System.out.println("  CP:");
        System.out.println("  LS:");
        System.out.println("    lsTakeFirst: " + lsTakeFirst);
        System.out.println("    lsSearchProcedural: " + lsSearchProcedural);
        System.out.println("    vrp:");
        System.out.println("      vrpLimitBy: " + vrpLimitBy);
        System.out.println("      vrpSearchDist: " + vrpSearchDist);
        System.out.println("      vrpSearchTime: " + vrpSearchTime);
        System.out.println("    tsp:");
        System.out.println("      tspSearch: " + tspSearch);
        System.out.println("      tspLimitBy: " + tspLimitBy);
        System.out.println("      tspSearchDist: " + tspSearchDist);
        System.out.println("      tspSearchTime: " + tspSearchTime);
        System.out.println("    rand walk:");
        System.out.println("      probRandWalk: " + probRandWalk);
        System.out.println("      probRandWalkFactor: " + probRandWalkFactor);
        System.out.println("      randMaxDist: " + randMaxDist);
    }

    public static void debug(int priority, String message) {
        if (Settings.verbosity >= priority) {
            System.out.println(message);
        }
    }
}
