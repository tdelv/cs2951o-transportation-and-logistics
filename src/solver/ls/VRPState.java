package solver.ls;

import ilog.concert.IloException;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VRPState extends AbstractLocalSearchState<Double> {

    private VRPInstance problem;
    private List<List<Integer>> paths;

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
    Double getValue() {
        double totalCost = 0;
        for (int i = 0; i < paths.size(); i ++) {
            TSPLocalSearch tspLS = new TSPLocalSearch(problem, paths.get(i));
            TSPState best = tspLS.solve();
            paths.set(i, best.getOrder());
            totalCost += best.getValueRemember();
        }

        return totalCost;
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
            CPInstance cpInstance = new CPInstance(problem);
            List<Set<Integer>> bins = new ArrayList<>();
            for (List<Integer> path : paths) {
                bins.add(new HashSet<>(path));
            }

            int trueDist = (int) Math.ceil(dist * problem.numCustomers);

            List<Set<Integer>> nonEmptyBins = new ArrayList<>(bins);
            for (int i = nonEmptyBins.size() - 1; i >= 0; i --) {
                if (nonEmptyBins.get(i).isEmpty()) {
                    nonEmptyBins.remove(i);
                }
            }

            for (int i = 0; i < trueDist; i ++) {
                if (nonEmptyBins.isEmpty()) {
                    break;
                }
                int binInd = Settings.rand.nextInt(nonEmptyBins.size());
                Set<Integer> bin = nonEmptyBins.get(binInd);
                int locInd = Settings.rand.nextInt(bin.size());
                Integer remove = new ArrayList<>(bin).get(locInd);
                bin.remove(remove);
                if (bin.isEmpty()) {
                    nonEmptyBins.remove(binInd);
                }
            }

            Solution solution = cpInstance.solve(bins, false).get();
            return new VRPState(problem, solution.getPaths());
        } catch (IloException e) {
            e.printStackTrace();
            return this;
        }
    }

    @Override
    void print() {
        System.out.println("VRPState: " + this.getValue());
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
