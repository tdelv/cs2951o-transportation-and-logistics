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

        return solve(bins);
    }

    public Optional<Solution> solve(List<List<Integer>> bins) throws IloException {
        start();

        IloIntExpr[] vehicleLoads = new IloIntExpr[problem.numVehicles];
        Arrays.fill(vehicleLoads, cp.sum(cp.intExpr(), problem.vehicleCapacity));

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

        cp.add(cp.pack(vehicleLoads, whichVehicle, demand));

        // Solves
        if (cp.solve()) {
            List<List<Integer>> newBins = new ArrayList<>();
            for (int v = 0; v < problem.numVehicles; v ++) {
                newBins.add(new ArrayList<>());
            }

            for (int c = 1; c < problem.numCustomers; c ++) {
                int bin = (int) cp.getValue(whichVehicle[c - 1]);
                newBins.get(bin).add(c);
            }

            cp.end();
            return Optional.of(new Solution(problem, newBins));
        } else {
            cp.end();
            return Optional.empty();
        }
    }
}
