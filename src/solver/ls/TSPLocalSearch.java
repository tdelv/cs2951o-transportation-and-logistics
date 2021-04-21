package solver.ls;

import java.util.*;

public class TSPLocalSearch extends AbstractLocalSearch<Double, TSPState> {
    private VRPInstance problem;
    private TSPState initial;
    private static Map<Set<Integer>, TSPState> solved = new HashMap<>();

    public TSPLocalSearch(VRPInstance problem, List<Integer> locations) {
        this.problem = problem;
        this.initial = new TSPState(problem, locations);
    }

    public TSPState solve() {
        Set<Integer> bin = new HashSet<>(this.initial.getOrder());
        if (solved.containsKey(bin)) {
            return solved.get(bin);
        }

        Timer.tspTimer.start();
        TSPState solution = null;
        switch (Settings.tspLimitBy) {
            case dist:
                solution = this.search(Settings.tspSearchDist);
                break;
            case time:
                solution = this.search(Settings.tspSearchTime);
                break;
            case both:
                solution = this.search(Settings.tspSearchDist, Settings.tspSearchTime);
                break;
            default:
                System.err.println("Unhandled tspLimitBy: " + Settings.tspLimitBy);
                System.exit(1);
        }
        Timer.tspTimer.stop();
        solved.put(bin, solution);
        return solution;
    }

    @Override
    TSPState getInitial() {
        return this.initial;
    }
}
