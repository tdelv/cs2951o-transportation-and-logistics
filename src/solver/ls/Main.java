package solver.ls;

import ilog.concert.IloException;

import java.io.FileNotFoundException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Main <file>");
            return;
        }

        CliArgs parser = new CliArgs(args);
        String input = parser.arg(0);
        Settings.verbosity = parser.switchIntegerValue("-verbosity", 0);
        Settings.cpUseDistribute = parser.switchBooleanValue("-cpUseDistribute", false);
        Settings.cpReduceArrLength = parser.switchBooleanValue("-cpReduceArrLength", true);
        Settings.print();

        Path path = Paths.get(input);
        String filename = path.getFileName().toString();
        System.out.println("Instance: " + input);

        try {
            Timer watch = new Timer();
            watch.start();
            VRPInstance problem = new VRPInstance(input);
            Optional<Solution> solutionOpt = problem.solve();
            watch.stop();

            if (solutionOpt.isPresent()) {
                Solution solution = solutionOpt.get();
                if (Settings.verbosity > 0) {
                    assert solution.isWellFormed() : "Solution not well formed: " + solution.toString();
                    assert solution.isFeasible() : "Solution is not feasible: " + solution.toString();
                }

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
            System.out.println("CPLEX error:" + e.getMessage());
            if (Settings.verbosity > 0) {
                e.printStackTrace();
            }
        }
    }
}