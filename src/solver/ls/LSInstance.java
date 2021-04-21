package solver.ls;

import java.util.*;

public class LSInstance {
    private VRPInstance problem;

    public LSInstance(VRPInstance problem) {
        this.problem = problem;
    }

    public Solution solve(Solution feasible) {
        VRPLocalSearch vrpLS = new VRPLocalSearch(problem, feasible.getPaths());
        VRPState solution = vrpLS.search(Settings.vrpSearchDist);
        List<List<Integer>> paths = solution.getPaths();
        return new Solution(problem, paths);
    }
}
