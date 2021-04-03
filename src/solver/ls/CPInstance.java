package solver.ls;

import ilog.concert.IloException;
import ilog.cp.IloCP;
import ilog.cplex.IloCplex;

import java.util.Optional;

public class CPInstance {
    private VRPInstance problem;
    private IloCP cp;

    public CPInstance(VRPInstance problem) throws IloException {
        this.problem = problem;
        this.cp = new IloCP();
    }

    public Optional<Solution> solve() {
        return Optional.empty();
    }
}
