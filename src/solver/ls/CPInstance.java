package solver.ls;

import ilog.concert.IloException;
import ilog.concert.IloIntExpr;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.cp.IloCP;

import java.util.*;

public class CPInstance {
    private VRPInstance problem;
    private IloCP cp;

    public CPInstance(VRPInstance problem) throws IloException {
        this.problem = problem;
        this.cp = null;
    }

    private void start() {
        this.cp = new IloCP();
        if (Settings.verbosity < 5) {
            this.cp.setOut(null);
        }
    }

    public Optional<Solution> getFeasible() throws IloException {
        List<List<Integer>> bins = new ArrayList<>();
        for (int v = 0; v < problem.numVehicles; v++) {
            bins.add(new ArrayList<>());
        }

        Optional<Solution> feasible = solve(bins, false);

        return feasible;
    }

    public Optional<Solution> solve(List<List<Integer>> bins) throws IloException {
        return this.solve(bins, true);
    }

    public Optional<Solution> solve(List<List<Integer>> bins, boolean minimize) throws IloException {
        start();
        // Find unclaimed customers
        int[] customerArray;
        {
            List<Integer> unclaimedCustomers = new ArrayList<Integer>();
            for (int c = 1; c < problem.numCustomers; c++) {
                boolean unclaimed = true;
                for (int v = 0; v < problem.numVehicles; v++) {
                    if (bins.get(v).contains(c)) {
                        unclaimed = false;
                        break;
                    }
                }
                if (unclaimed) {
                    unclaimedCustomers.add(c);
                }
            }
            unclaimedCustomers.add(0);
            customerArray = unclaimedCustomers.stream()
                .mapToInt(Integer::intValue)
                .toArray();
        }

        int numToClaim = customerArray.length - 1;

        // Set up variables
        IloIntVar[][] visitVehicleCustomer = new IloIntVar[problem.numVehicles][];
        for (int v = 0; v < problem.numVehicles; v++) {
            visitVehicleCustomer[v] = cp.intVarArray(numToClaim, customerArray, "");
        }

        if (Settings.verbosity >= 5) {
            for (int v = 0; v < problem.numVehicles; v++) {
                System.out.println("Vehicle " + v + ":");
                for (int c = 0; c < visitVehicleCustomer[v].length; c++) {
                    System.out.println("  Customer " + c + ": " + visitVehicleCustomer[v][c]);
                }
            }
        }

        // Enforce that zeroes only appear as tail of sequence
        for (int v = 0; v < problem.numVehicles; v++) {
            for (int c = 0; c < visitVehicleCustomer[v].length - 1; c++) {
                cp.add(cp.ifThen(
                        cp.eq(visitVehicleCustomer[v][c], 0),
                        cp.eq(visitVehicleCustomer[v][c + 1], 0)));
            }
        }

        // Flatten variable array
        IloIntVar[] allVars = new IloIntVar[numToClaim * problem.numVehicles];
        int currVar = 0;
        for (int v = 0; v < problem.numVehicles; v++) {
            for (int c = 0; c < visitVehicleCustomer[v].length; c++) {
                allVars[currVar++] = visitVehicleCustomer[v][c];
            }
        }

        // Enforce that every unclaimed customer appears exactly once (and 0 fills the rest)
        for (int c = 0; c < customerArray.length - 1; c ++) {
            cp.add(cp.eq(cp.count(allVars, customerArray[c]), 1));
        }
        cp.add(cp.eq(cp.count(allVars, 0), (numToClaim * problem.numVehicles) - numToClaim));

        // Enforces vehicle capacity limit
        for (int v = 0; v < problem.numVehicles; v ++) {
            IloIntExpr totalDemand = cp.intExpr();
            for (int c : bins.get(v)) {
                totalDemand = cp.sum(totalDemand, problem.demandOfCustomer[c]);
            }
            for (int c = 0; c < visitVehicleCustomer[v].length; c ++) {
                totalDemand = cp.sum(totalDemand, cp.element(problem.demandOfCustomer, visitVehicleCustomer[v][c]));
            }
            cp.add(cp.le(totalDemand, problem.vehicleCapacity));
        }

        // Solves
        if (cp.solve()) {
            List<List<Integer>> paths = new ArrayList<>();
            for (int v = 0; v < problem.numVehicles; v++) {
                List<Integer> path = new ArrayList<>(bins.get(v));
                for (int c = 0; c < visitVehicleCustomer[v].length; c++) {
                    int loc = (int) cp.getValue(visitVehicleCustomer[v][c]);
                    if (loc == 0) {
                        break;
                    }
                    path.add(loc);
                }
                paths.add(path);
            }
            cp.end();
            return Optional.of(new Solution(problem, paths));
        } else {
            cp.end();
            return Optional.empty();
        }
    }
}
