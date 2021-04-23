package solver.ls;

import ilog.concert.IloException;

import java.util.*;

public class VRPState extends AbstractLocalSearchState<Double> {

    private VRPInstance problem;
    private List<List<Integer>> paths;
    private List<List<Double>> badness;

    public VRPState(VRPInstance problem, List<List<Integer>> paths) {
        this.problem = problem;
        this.paths = paths;
    }

    public List<List<Integer>> getPaths() {
        return Utils.doubleClone(this.paths);
    }

    private boolean isFeasible() {
        for (List<Integer> path : paths) {
            int totalDemand = 0;
            for (Integer loc : path) {
                totalDemand += problem.demandOfCustomer[loc];
            }

            if (totalDemand > problem.vehicleCapacity) {
                return false;
            }
        }

        return true;
    }

    @Override
    Double getValue(Optional<Double> prevBest) {
        double totalCost = 0;
        Settings.debug(5, "getValue VRP");
        // For each bin, solve TSP
        for (int i = 0; i < paths.size(); i++) {
            if (prevBest.isPresent() && totalCost > prevBest.get()) {
                break;
            }
            Settings.debug(5, "  solve " + (i + 1) + " / " + paths.size());

            // Solve TSP
            TSPLocalSearch tspLS = new TSPLocalSearch(problem, paths.get(i));
            TSPState best = tspLS.solve(); // GOTO: TSPLocalSearch.solve

            // Update ordering
            List<Integer> order = best.getOrder();
            paths.set(i, order);

            // Add to cost
            totalCost += best.getValueRemember();

        }

        // Calculate badness for each customer
        if (Settings.useBadness) {
            getBadnessCOM(); // GOTO: getBadnessCOM
        }

        return totalCost;
    }

    private void getBadnessDist() {
        double totalBadness = 0;
        badness = new ArrayList<>();
        for (List<Integer> order : paths) {
            List<Double> currentBadness = new ArrayList<>();
            badness.add(currentBadness);
            if (order.size() == 0) {

            } else if (order.size() == 1) {
                currentBadness.add(0.5);
            } else {
                double bad;
                bad = dist(order, 0, 1);
                totalBadness += bad;
                currentBadness.add(bad);
                for (int i = 1; i < order.size() - 1; i++) {
                    bad = dist(order, i - 1, i) + dist(order, i, i + 1);
                    totalBadness += bad;
                    currentBadness.add(bad);
                }
                bad = dist(order, order.size() - 2, order.size() - 1) + dist(order.get(order.size() - 1), 0);
                totalBadness += bad;
                currentBadness.add(bad);
            }
        }

        for (List<Double> currBadness : badness) {
            for (int i = 0; i < currBadness.size(); i ++) {
                currBadness.set(i, currBadness.get(i) / totalBadness * problem.numCustomers);
            }
        }
    }

    private void getBadnessCOM() {
        // Calculate center-of-mass badness

        double totalBadness = 0;
        badness = new ArrayList<>();

        // Find badness for each path
        for (List<Integer> order : paths) {

            // Get center of mass
            double avgX, avgY;
            {
                double totalX = 0;
                double totalY = 0;
                for (Integer c : order) {
                    totalX += problem.xCoordOfCustomer[c];
                    totalY += problem.yCoordOfCustomer[c];
                }
                avgX = totalX / order.size();
                avgY = totalY / order.size();
            }

            List<Double> currentBadness = new ArrayList<>();
            badness.add(currentBadness);

            // Find total distance of customers away from center
            double bad;
            for (int i = 0; i < order.size(); i++) {
                double x = problem.xCoordOfCustomer[order.get(i)];
                double y = problem.yCoordOfCustomer[order.get(i)];
                bad = Utils.dist(x, y, avgX, avgY);
                totalBadness += bad;
                currentBadness.add(bad);
            }
        }

        // Normalize badness
        for (List<Double> currBadness : badness) {
            for (int i = 0; i < currBadness.size(); i ++) {
                currBadness.set(i, currBadness.get(i) / totalBadness * problem.numCustomers);
            }
        }
    }

    private double dist(List<Integer> order, int startInd, int endInd) {
        int start = order.get(startInd);
        int end = order.get(endInd);

        return dist(start, end);
    }

    private double dist(int start, int end) {
        return Utils.dist(
                problem.xCoordOfCustomer[start], problem.yCoordOfCustomer[start],
                problem.xCoordOfCustomer[end], problem.yCoordOfCustomer[end]);
    }

    @Override
    Generator<AbstractLocalSearchState<Double>> getNeighbors() {
        return new Generator<AbstractLocalSearchState<Double>>() {
            @Override
            protected void run() throws InterruptedException {
                for (int binInd0 = 0; binInd0 < paths.size(); binInd0 ++) {
                    for (int binInd1 = binInd0 + 1; binInd1 < paths.size(); binInd1 ++) {
                        for (int i = 0; i < paths.get(binInd0).size(); i ++) {
                            List<List<Integer>> moveForwards = new ArrayList<>(paths);
                            List<Integer> bin0 = new ArrayList<>(moveForwards.get(binInd0));
                            List<Integer> bin1 = new ArrayList<>(moveForwards.get(binInd1));
                            moveForwards.set(binInd0, bin0);
                            moveForwards.set(binInd1, bin1);

                            bin1.add(bin0.remove(i));

                            this.yield(new VRPState(problem, moveForwards));
                        }

                        for (int j = 0; j < paths.get(binInd1).size(); j ++) {
                            List<List<Integer>> moveBackwards = new ArrayList<>(paths);
                            List<Integer> bin0 = new ArrayList<>(moveBackwards.get(binInd0));
                            List<Integer> bin1 = new ArrayList<>(moveBackwards.get(binInd1));
                            moveBackwards.set(binInd0, bin0);
                            moveBackwards.set(binInd1, bin1);

                            bin0.add(bin1.remove(j));

                            this.yield(new VRPState(problem, moveBackwards));
                        }

//                        for (int i = 0; i < paths.get(binInd0).size(); i ++) {
//                            for (int j = 0; j < paths.get(binInd1).size(); j ++) {
//                                List<List<Integer>> moveBoth = new ArrayList<>(paths);
//                                List<Integer> bin0 = new ArrayList<>(moveBoth.get(binInd0));
//                                List<Integer> bin1 = new ArrayList<>(moveBoth.get(binInd1));
//                                moveBoth.set(binInd0, bin0);
//                                moveBoth.set(binInd1, bin1);
//
//                                int value0 = bin0.remove(i);
//                                int value1 = bin1.remove(j);
//                                bin0.add(value1);
//                                bin1.add(value0);
//
//                                this.yield(new VRPState(problem, moveBoth));
//                            }
//                        }
                    }
                }
            }
        };
    }

    @Override
    boolean isValid() {
        return this.isFeasible();
    }

    @Override AbstractLocalSearchState<Double> getRandom(double dist) {
        try {
            this.getValueRemember();
            Timer.lsNeighbor.start();
            List<List<Integer>> bins = Utils.doubleClone(paths);

            // Go through bins and randomly remove customers
            for (int binInd = 0; binInd < bins.size(); binInd ++) {
                List<Integer> bin = bins.get(binInd);
                for (int i = bin.size() - 1; i >= 0; i --) {
                    if (Settings.rand.nextDouble() < dist) {
                        bin.remove(i);
                    } else if (Settings.useBadness && Settings.rand.nextDouble() < this.badness.get(binInd).get(i) * dist) {
                        bin.remove(i);
                    }
                }
            }

            // Resolve problem with partial bins
            Settings.debug(5, "Start VRP CP");
            Solution solution = CPInstance.getInstance(problem).solveBin(bins).get(); // GOTO: CPInstance.solveBin
            Settings.debug(5, "End VRP CP");
            Timer.lsNeighbor.stop();

            return new VRPState(problem, solution.getPaths());
        } catch (IloException e) {
            e.printStackTrace();
            return this;
        }
    }

    @Override
    void print() {
        System.out.println("VRPState: " + this.getValueRemember());
        for (List<Integer> path : paths) {
            System.out.print(" ");
            for (Integer loc : path) {
                System.out.print(" " + loc);
            }
            System.out.println();
        }
        System.out.println();
    }

    @Override
    String getProblem() { return "VRP"; }
}
