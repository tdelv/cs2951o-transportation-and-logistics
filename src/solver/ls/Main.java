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
        Settings.print();

        Path path = Paths.get(input);
        String filename = path.getFileName().toString();
        System.out.println("Instance: " + input);

        try {
            Timer watch = new Timer();
            watch.start();
            VRPInstance problem = new VRPInstance(input);
            Optional<Solution> solution = problem.solve();
            watch.stop();

            if (solution.isPresent()) {
                System.out.println("Instance: " + filename +
                        " Time: " + watch +
                        " Result: " + solution.get().getCost() +
                        " Solution: " + solution.get().toString()
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