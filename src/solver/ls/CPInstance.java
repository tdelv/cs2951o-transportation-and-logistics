package solver.ls;

import ilog.concert.*;
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

        return solveBin(bins);
    }

    public Optional<Solution> solveBin(List<List<Integer>> bins) throws IloException {
        start();

        IloIntExpr[] vehicleLoads = cp.intVarArray(problem.numVehicles, 0, problem.vehicleCapacity);

        IloIntVar[] whichVehicle = new IloIntVar[problem.numCustomers - 1];
        for (int c = 1; c < problem.numCustomers; c ++) {
            Optional<Integer> whichBin = Optional.empty();
            for (int binInd = 0; binInd < bins.size(); binInd ++) {
                if (bins.get(binInd).contains(c)) {
                    whichBin = Optional.of(binInd);
                    break;
                }
            }
            if (whichBin.isPresent()) {
                whichVehicle[c - 1] = cp.intVar(new int[] { whichBin.get() });
            } else {
                whichVehicle[c - 1] = cp.intVar(0, problem.numVehicles - 1);
            }
        }

        int[] demand = new int[problem.numCustomers - 1];
        for (int c = 0; c < problem.numCustomers - 1; c ++) {
            demand[c] = problem.demandOfCustomer[c + 1];
        }

        IloConstraint pack = cp.pack(vehicleLoads, whichVehicle, demand);
        cp.add(pack);

        // Solves
        Optional<Solution> result;
        if (cp.solve()) {
            List<List<Integer>> newBins = new ArrayList<>();
            for (int v = 0; v < problem.numVehicles; v ++) {
                newBins.add(new ArrayList<>());
            }

            for (int c = 1; c < problem.numCustomers; c ++) {
                int bin = (int) cp.getValue(whichVehicle[c - 1]);
                newBins.get(bin).add(c);
            }

            result = Optional.of(new Solution(problem, newBins));
        } else {
            result = Optional.empty();
        }
        cp.end();
        return result;
    }

    public List<Integer> solveTSP(List<Integer> bin) throws IloException {
        start();

        int[] binValues = bin.stream().mapToInt(Integer::intValue).toArray();

        IloIntVar[] vars = cp.intVarArray(bin.size(), binValues, "TSP");

        cp.add(cp.allDiff(vars));

        IloNumExpr cost = cp.numExpr();
        IloNumExpr startX = cp.sum(cp.numExpr(), problem.xCoordOfCustomer[0]);
        IloNumExpr startY = cp.sum(cp.numExpr(), problem.yCoordOfCustomer[0]);
        IloNumExpr currX = startX;
        IloNumExpr currY = startY;

        for (int i = 0; i < vars.length; i ++) {
            IloNumExpr newX = cp.element(problem.xCoordOfCustomer, vars[i]);
            IloNumExpr newY = cp.element(problem.yCoordOfCustomer, vars[i]);
            cost = cp.sum(cost, distance(currX, currY, newX, newY));
            currX = newX;
            currY = newY;
        }

        cost = cp.sum(cost, distance(currX, currY, startX, startY));
        cp.addMinimize(cost);

        cp.setParameter(IloCP.DoubleParam.TimeLimit, Settings.tspSearchTime);

        List<Integer> result;
        if (cp.solve()) {
            result = new ArrayList<>();
            for (int i = 0; i < vars.length; i ++) {
                result.add((int) cp.getValue(vars[i]));
            }
        } else {
            System.err.println("TSP was unsat.");
            System.exit(1);
            result = null;
        }
        cp.end();
        return result;
    }

    private IloNumExpr distance(IloNumExpr x1, IloNumExpr y1, IloNumExpr x2, IloNumExpr y2) throws IloException {
        IloNumExpr oneHalf = cp.sum(cp.numExpr(), 0.5);
        return cp.power(cp.sum(cp.square(cp.diff(x2, x1)), cp.square(cp.diff(y2, y1))), oneHalf);
    }
}
