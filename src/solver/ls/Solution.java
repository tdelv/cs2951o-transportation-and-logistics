package solver.ls;

import java.util.*;

public class Solution {
    private VRPInstance problem;
    private List<List<Integer>> paths;
    private boolean isOptimal;
    private Optional<Double> cost;
    private Optional<Boolean> isWellFormed;
    private Optional<Boolean> isFeasible;

    public Solution(VRPInstance problem, List<List<Integer>> paths) {
        this(problem, paths, false);
    }

    public Solution(VRPInstance problem, List<List<Integer>> paths, boolean isOptimal) {
        this.problem = problem;
        this.paths = paths;
        this.isOptimal = isOptimal;
        this.cost = Optional.empty();
        this.isWellFormed = Optional.empty();
        this.isFeasible = Optional.empty();
    }

    public void setOptimal(boolean isOptimal) {
        this.isOptimal = isOptimal;
    }

    public String toString() {
        StringBuilder ret = new StringBuilder();

        ret.append(isOptimal ? 1 : 0);

        for (List<Integer> path : paths) {
            ret.append(" " + 0);
            for (int loc : path) {
                ret.append(" ").append(loc);
            }
            ret.append(" " + 0);
        }

        return ret.toString();
    }

    public double getCost() {
        if (this.cost.isPresent()) {
            return this.cost.get();
        } else {
            double cost = 0;

            for (List<Integer> path : paths) {
                double x = 0, y = 0;
                for (int loc : path) {
                    double newX = problem.xCoordOfCustomer[loc];
                    double newY = problem.yCoordOfCustomer[loc];
                    double dist = Utils.dist(x, y, newX, newY);

                    cost += dist;
                    x = newX;
                    y = newY;
                }
                cost += Utils.dist(x, y, 0, 0);
            }

            this.cost = Optional.of(cost);
            return cost;
        }
    }

    public boolean isWellFormed() {
        if (this.isWellFormed.isPresent()) {
            return this.isWellFormed.get();
        } else {
            boolean isWellFormed = true;

            isWellFormed &= paths.size() == problem.numVehicles;

            Set<Integer> locs = new HashSet<>();
            for (int i = 1; i < problem.numCustomers; i ++) {
                locs.add(i);
            }
            for (List<Integer> path : paths) {
                for (int loc : path) {
                    if (locs.contains(loc)) {
                        locs.remove(loc);
                    } else {
                        isWellFormed = false;
                    }
                }
            }
            isWellFormed &= locs.isEmpty();

            this.isWellFormed = Optional.of(isWellFormed);
            return isWellFormed;
        }
    }

    public boolean isFeasible() {
        if (this.isFeasible.isPresent()) {
            return this.isFeasible.get();
        } else {
            boolean isFeasible = true;

            for (List<Integer> path : paths) {
                int carry = 0;
                for (int loc : path) {
                    carry += problem.demandOfCustomer[loc];
                }

                if (carry > problem.vehicleCapacity) {
                    isFeasible = false;
                    break;
                }
            }

            this.isFeasible = Optional.of(isFeasible);
            return isFeasible;
        }
    }

    public List<List<Integer>> getPaths(boolean copy) {
        if (copy) {
            List<List<Integer>> paths = new ArrayList<>();

            for (List<Integer> path : this.paths) {
                paths.add(new ArrayList<>(path));
            }

            return paths;
        } else {
            return this.paths;
        }
    }

    public List<List<Integer>> getPaths() {
        return getPaths(true);
    }

}
