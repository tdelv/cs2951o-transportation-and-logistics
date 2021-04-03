package solver.ls;

import java.io.FileNotFoundException;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage: java Main <file>");
            return;
        }

        CliArgs parser = new CliArgs(args);
        String input = parser.arg(0);
        Settings.verbosity = parser.switchIntegerValue("-verbosity", 0);

        Path path = Paths.get(input);
        String filename = path.getFileName().toString();
        System.out.println("Instance: " + input);

        Timer watch = new Timer();
        watch.start();
        VRPInstance instance = new VRPInstance(input);
        watch.stop();

        System.out.println(
                "Instance: " + filename +
                " Time: " + String.format("%.2f", watch.getTotalTime()) +
                " Result: " + "N/A" + " Solution: N/A");
    }
}