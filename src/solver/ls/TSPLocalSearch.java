package solver.ls;

import java.util.*;

public class TSPLocalSearch extends AbstractLocalSearch<Double, TSPState> {
    private VRPInstance problem;
    private List<Integer> locations;
    private static Map<Set<Integer>, TSPState> solved = new HashMap<>();

    public TSPLocalSearch(VRPInstance problem, List<Integer> locations) {
        this.problem = problem;
        this.locations = locations;
    }

    public TSPState solve() {
        Set<Integer> bin = new HashSet<>(this.locations);
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
        Set<Integer> toVisit = new HashSet<>(locations);
        double currX = problem.xCoordOfCustomer[0];
        double currY = problem.yCoordOfCustomer[0];
        List<Integer> path = new ArrayList<>();
        while (!toVisit.isEmpty()) {
            Optional<Integer> best = Optional.empty();
            Optional<Double> bestDist = Optional.empty();
            for (Integer next : toVisit) {
                double dist = Utils.dist(currX, currY, problem.xCoordOfCustomer[next], problem.yCoordOfCustomer[next]);
                if (!bestDist.isPresent() || dist <bestDist.get()) {
                    best = Optional.of(next);
                    bestDist = Optional.of(dist);
                }
            }

            path.add(best.get());
            toVisit.remove(best.get());
            currX = problem.xCoordOfCustomer[best.get()];
            currY = problem.yCoordOfCustomer[best.get()];
        }
        return new TSPState(problem, path);
    }
}
