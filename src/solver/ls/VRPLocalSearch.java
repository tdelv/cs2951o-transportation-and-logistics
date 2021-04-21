package solver.ls;

import java.util.List;

public class VRPLocalSearch extends AbstractLocalSearch<Double, VRPState> {

    private VRPInstance problem;
    private VRPState initial;

    public VRPLocalSearch(VRPInstance problem, List<List<Integer>> initial) {
        this.problem = problem;
        this.initial = new VRPState(problem, initial);
    }

    @Override
    VRPState getInitial() {
        return this.initial;
    }
}
