package solver.ls;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.Optional;

public class LPInstance {
    private VRPInstance problem;
    private IloCplex cplex;

    public LPInstance(VRPInstance problem) throws IloException {
        this.problem = problem;
        this.cplex = new IloCplex();
    }

    public Optional<Solution> solve() {
        return Optional.empty();
    }
}
