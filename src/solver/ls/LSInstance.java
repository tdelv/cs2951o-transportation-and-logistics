package solver.ls;

import java.util.*;

public class LSInstance {
    private VRPInstance problem;

    public LSInstance(VRPInstance problem) {
        this.problem = problem;
    }

    public Solution solve(Solution feasible) {
        VRPLocalSearch vrpLS = new VRPLocalSearch(problem, feasible.getPaths());
        VRPState solution = null;
        switch (Settings.vrpLimitBy) {
            case dist:
                solution = vrpLS.search(Settings.vrpSearchDist);
                break;
            case time:
                solution = vrpLS.search(Settings.vrpSearchTime);
                break;
            case both:
                solution = vrpLS.search(Settings.vrpSearchDist, Settings.vrpSearchTime);
                break;
            default:
                System.err.println("Unhandled vrpLimitBy: " + Settings.vrpLimitBy);
                System.exit(1);
        }
        List<List<Integer>> paths = solution.getPaths();
        return new Solution(problem, paths);
    }
}
