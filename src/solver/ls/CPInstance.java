package solver.ls;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloNumExpr;
import ilog.cp.IloCP;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CPInstance {
    private VRPInstance problem;
    private IloCP cp;

    public CPInstance(VRPInstance problem) throws IloException {
        this.problem = problem;
        this.cp = new IloCP();
    }

    public Optional<Solution> solve() throws IloException {
        IloIntVar[][] visitVehicleCustomer = new IloIntVar[problem.numVehicles][problem.maxCustomersPerVehicle];
        for (int v = 0; v < problem.numVehicles; v ++) {
            for (int c = 0; c < problem.maxCustomersPerVehicle; c ++) {
                visitVehicleCustomer[v][c] = cp.intVar(0, problem.numVehicles);
            }
        }

        for (int v = 0; v < problem.numVehicles; v ++) {
            for (int c = 0; c < problem.maxCustomersPerVehicle - 1; c ++) {
                cp.add(cp.ifThen(
                        cp.eq(visitVehicleCustomer[v][c], 0),
                        cp.eq(visitVehicleCustomer[v][c + 1], 0)));
            }
        }

        IloIntVar[] allVars = new IloIntVar[problem.numVehicles * problem.maxCustomersPerVehicle];
        for (int v = 0; v < problem.numVehicles; v ++) {
            for (int c = 0; c < problem.maxCustomersPerVehicle - 1; c ++) {
                allVars[v * problem.numCustomers + c] = visitVehicleCustomer[v][c];
            }
        }

        for (int c = 1; c <= problem.numCustomers; c ++) {
            cp.add(cp.eq(cp.count(allVars, c), 1));
        }
        // Redundant
        cp.add(cp.eq(cp.count(allVars, 0), problem.numVehicles * problem.maxCustomersPerVehicle - problem.numCustomers));

        IloNumExpr cost = cp.numExpr();
        for (int v = 0; v < problem.numVehicles; v ++) {
            IloNumExpr x = cp.numExpr(), y = cp.numExpr();
            IloNumExpr dist = cp.numExpr();
            for (int c = 0; c < problem.maxCustomersPerVehicle - 1; c ++) {
                IloIntVar loc = visitVehicleCustomer[v][c];
                IloNumExpr newX = cp.element(problem.xCoordOfCustomer, loc);
                IloNumExpr newY = cp.element(problem.yCoordOfCustomer, loc);

                dist = cp.sum(dist, distance(x, y, newX, newY));
                x = newX;
                y = newY;
            }
            dist = cp.sum(dist, distance(x, y, cp.numExpr(), cp.numExpr()));
            cost = cp.sum(cost, dist);
        }
        cp.addMinimize(cost);

        if (cp.solve()) {
            List<List<Integer>> paths = new ArrayList<>();
            for (int v = 0; v < problem.numVehicles; v ++) {
                List<Integer> path = new ArrayList<>();
                for (int c = 0; c < problem.maxCustomersPerVehicle; c ++) {
                    int loc = (int)cp.getValue(visitVehicleCustomer[v][c]);
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
