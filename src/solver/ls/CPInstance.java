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
        this.cp = new IloCP();
    }

    public Optional<Solution> solve() throws IloException {
        ArrayList<Set<Integer>> setVars = new ArrayList<>();
        for (int v = 0; v < problem.numVehicles; v++) {
            setVars.add(new HashSet<>());
        }

        return solve(setVars);
    }

    public Optional<Solution> solve(ArrayList<Set<Integer>> setVars) throws IloException {
        // Find inverted setVars
        Set<Integer> unclaimedCustomers = new HashSet<Integer>();
        unclaimedCustomers.add(0);
        for (int c = 1; c < problem.numCustomers; c++) {
            unclaimedCustomers.add(c);
            for (int v = 0; v < problem.numVehicles; v++) {
                if (setVars.get(v).contains(c)) {
                    unclaimedCustomers.remove(c);
                    break;
                }
            }
        }

        // Set up variables
        IloIntVar[][] visitVehicleCustomer = new IloIntVar[problem.numVehicles][];
        int totalVars = 0;
        for (int v = 0; v < problem.numVehicles; v++) {
            Set<Integer> customers = new HashSet<>(setVars.get(v));
            customers.addAll(unclaimedCustomers);
            int[] customerArray = customers.stream()
                    .mapToInt(Integer::intValue)
                    .toArray();
            int maxCustomersForVehicle = problem.numCustomers / (v + 1);
            int numCustomers;
            if (Settings.cpReduceArrLength) {
                numCustomers = Math.min(problem.maxCustomersPerVehicle, maxCustomersForVehicle);
            } else {
                numCustomers = problem.maxCustomersPerVehicle;
            }
            totalVars += numCustomers;
            visitVehicleCustomer[v] = new IloIntVar[numCustomers];
            for (int c = 0; c < numCustomers; c++) {
                visitVehicleCustomer[v][c] = cp.intVar(customerArray);
            }
        }

        for (int v = 0; v < problem.numVehicles; v ++) {
            System.out.println("Vehicle " + v + ":");
            for (int c = 0; c < visitVehicleCustomer[v].length; c ++) {
                System.out.println("  Customer " + c + ": " + visitVehicleCustomer[v][c]);
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

        // And that earlier trucks serve more customers
        for (int v = 0; v < problem.numVehicles - 1; v++) {
            for (int c = 0; c < visitVehicleCustomer[v + 1].length; c++) {
                cp.add(cp.ifThen(
                        cp.eq(visitVehicleCustomer[v][c], 0),
                        cp.eq(visitVehicleCustomer[v + 1][c], 0)));
            }
        }

        // Flatten variable array
        IloIntVar[] allVars = new IloIntVar[totalVars];
        int currVar = 0;
        for (int v = 0; v < problem.numVehicles; v++) {
            for (int c = 0; c < visitVehicleCustomer[v].length; c++) {
                allVars[currVar++] = visitVehicleCustomer[v][c];
            }
        }

        // Enforce that every variable appears exactly once (and 0 fills the rest)
        if (Settings.cpUseDistribute) {
            IloIntExpr[] cards = new IloIntExpr[problem.numCustomers];
            int[] values = new int[problem.numCustomers];
            cards[0] = cp.sum(cp.intExpr(), totalVars - problem.numCustomers);
            values[0] = 0;
            for (int c = 1; c < problem.numCustomers; c++) {
                cards[c] = cp.sum(cp.intExpr(), 1);
                values[c] = c;
            }
            cp.add(cp.distribute(cards, values, allVars));
        } else {
            for (int c = 1; c < problem.numCustomers; c++) {
                cp.add(cp.eq(cp.count(allVars, c), 1));
            }
            cp.add(cp.eq(cp.count(allVars, 0), totalVars - (problem.numCustomers - 1)));
        }

        // Enforces vehicle capacity limit
        for (int v = 0; v < problem.numVehicles; v ++) {
            IloIntExpr totalDemand = cp.intExpr();
            for (int c = 0; c < visitVehicleCustomer[v].length; c ++) {
                totalDemand = cp.sum(totalDemand, cp.element(problem.demandOfCustomer, visitVehicleCustomer[v][c]));
            }
            cp.add(cp.le(totalDemand, problem.vehicleCapacity));
        }

        // Find total distance traveled by trucks
        IloNumExpr originX = cp.sum(cp.numExpr(), problem.xCoordOfCustomer[0]);
        IloNumExpr originY = cp.sum(cp.numExpr(), problem.yCoordOfCustomer[0]);
        IloNumExpr cost = cp.numExpr();
        for (int v = 0; v < problem.numVehicles; v++) {
            IloNumExpr x = originX;
            IloNumExpr y = originY;
            IloNumExpr dist = cp.numExpr();
            for (int c = 0; c < visitVehicleCustomer[v].length; c++) {
                IloIntVar loc = visitVehicleCustomer[v][c];
                IloNumExpr newX = cp.element(problem.xCoordOfCustomer, loc);
                IloNumExpr newY = cp.element(problem.yCoordOfCustomer, loc);

                dist = cp.sum(dist, distance(x, y, newX, newY));
                x = newX;
                y = newY;
            }
            dist = cp.sum(dist, distance(x, y, originX, originY));
            cost = cp.sum(cost, dist);
        }
        cp.addMinimize(cost);

        // Solves
        if (cp.solve()) {
            List<List<Integer>> paths = new ArrayList<>();
            for (int v = 0; v < problem.numVehicles; v++) {
                List<Integer> path = new ArrayList<>();
                for (int c = 0; c < visitVehicleCustomer[v].length; c++) {
                    int loc = (int) cp.getValue(visitVehicleCustomer[v][c]);
                    if (loc == 0) {
                        break;
                    }
                    path.add(loc);
                }
                paths.add(path);
            }
            return Optional.of(new Solution(problem, paths, true));
        } else {
            return Optional.empty();
        }
    }

    private IloNumExpr distance(IloNumExpr x1, IloNumExpr y1, IloNumExpr x2, IloNumExpr y2) throws IloException {
        IloNumExpr oneHalf = cp.sum(cp.numExpr(), 0.5);
        return cp.power(cp.sum(cp.square(cp.diff(x2, x1)), cp.square(cp.diff(y2, y1))), oneHalf);
    }
}
