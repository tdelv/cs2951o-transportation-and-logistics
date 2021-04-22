package solver.ls;

import ilog.concert.IloException;

import java.io.FileNotFoundException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        System.out.println("TEST");
        if (args.length == 0) {
            System.out.println("Usage: java Main <file>");
            return;
        }

        CliArgs parser = new CliArgs(args);
        String input = parser.arg(0);
        Settings.verbosity = parser.switchIntegerValue("-verbosity", 0);
        Settings.feasibilityOnly = parser.switchBooleanValue("-feasibilityOnly", false);

        Settings.lsTakeFirst = parser.switchBooleanValue("-lsTakeFirst", true);
        Settings.lsSearchProcedural = parser.switchBooleanValue("-lsSearchProcedural", false);

        Settings.vrpLimitBy = Settings.SearchLimit.valueOf(parser.switchValue("-vrpLimitBy", "time"));
        Settings.vrpSearchDist = parser.switchIntegerValue("-vrpSearchDist", 3);
        Settings.vrpSearchTime = parser.switchDoubleValue("-vrpSearchTime", 30.0);

        Settings.tspSearch = Settings.TSPSearch.valueOf(parser.switchValue("-tspSearch", "nearestNeighbor"));
        Settings.tspLimitBy = Settings.SearchLimit.valueOf(parser.switchValue("-tspLimitBy", "both"));
        Settings.tspSearchDist = parser.switchIntegerValue("-tspSearchDist", 3);
        Settings.tspSearchTime = parser.switchDoubleValue("-tspSearchTime", 1.0);

        Settings.probRandWalk = parser.switchDoubleValue("-probRandWalk", 0.1);
        Settings.probRandWalkFactor = parser.switchDoubleValue("-probRandWalkFactor", 0.95);
        Settings.randMaxDist = parser.switchDoubleValue("-randMaxDist", 0.6);

        Settings.print();

        Path path = Paths.get(input);
        String filename = path.getFileName().toString();
        System.out.println("Instance: " + input);

        try {
            Timer watch = Timer.totalTimer;
            watch.start();
            VRPInstance problem = new VRPInstance(input);
            Optional<Solution> solutionOpt = problem.solve();
            watch.stop();

            if (solutionOpt.isPresent()) {
                Solution solution = solutionOpt.get();
                assert solution.isWellFormed() : "Solution not well formed: " + solution.toString();
                assert solution.isFeasible() : "Solution is not feasible: " + solution.toString();

                Timer.printTimers();

                System.out.println("Instance: " + filename +
                        " Time: " + watch +
                        " Result: " + solution.getCost() +
                        " Solution: " + solution.toString()
                );
            } else {
                System.out.println("Instance: " + filename +
                        " Time: " + watch +
                        " Result: --" +
                        " Solution: --"
                );
            }

        } catch (IloException e) {
            System.out.println("CPLEX error: " + e.getMessage());
            if (Settings.verbosity > 0) {
                e.printStackTrace();
            }
        }
    }
}