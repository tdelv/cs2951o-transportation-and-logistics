package solver.ls;

import ilog.concert.IloException;

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
        // If already solved, return past result
        Set<Integer> bin = new HashSet<>(this.locations);
        if (solved.containsKey(bin)) {
            return solved.get(bin);
        }

        // (Jump to line 60)
        switch (Settings.tspSearch) {
            case localSearch: {
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
            case cp: {
                try {
                    CPInstance cpInstance = CPInstance.getInstance(problem);
                    TSPState solution = new TSPState(problem, cpInstance.solveTSP(locations));
                    solved.put(bin, solution);
                    return solution;
                } catch (IloException e) {
                    e.printStackTrace();
                    System.exit(1);
                    return null;
                }
            }
            case nearestNeighbor: {
                // Do nearest neighbor
                Timer.tspTimer.start();
                TSPState result = this.getInitial(); // GOTO: getInitial
                Timer.tspTimer.stop();
                return result;
            }
            default:
                System.err.println("Unhandled tspSearch: " + Settings.tspSearch);
                System.exit(1);
                return null;
        }
    }

    @Override
    TSPState getInitial() {
        // Perform nearest neighbor

        Set<Integer> toVisit = new HashSet<>(locations);
        double currX = problem.xCoordOfCustomer[0];
        double currY = problem.yCoordOfCustomer[0];
        List<Integer> path = new ArrayList<>();

        // Take from toVisit the nearest neighbor until empty
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
