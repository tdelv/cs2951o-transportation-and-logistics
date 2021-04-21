package solver.ls;

import ilog.concert.IloException;

import java.io.File;
import java.io.FileNotFoundException;

import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;

public class VRPInstance {
    // VRP Input Parameters
    int numCustomers;                // the number of customers
    int numVehicles;            // the number of vehicles
    int vehicleCapacity;            // the capacity of the vehicles
    int[] demandOfCustomer;        // the demand of each customer
    double[] xCoordOfCustomer;    // the x coordinate of each customer
    double[] yCoordOfCustomer;    // the y coordinate of each customer

    int maxCustomersPerVehicle;

    public VRPInstance(String fileName) {
        Scanner read = null;
        try {
            read = new Scanner(new File(fileName));
        } catch (FileNotFoundException e) {
            System.out.println("Error: in VRPInstance() " + fileName + "\n" + e.getMessage());
            System.exit(-1);
        }

        numCustomers = read.nextInt();
        numVehicles = read.nextInt();
        vehicleCapacity = read.nextInt();

        System.out.println("Number of customers: " + numCustomers);
        System.out.println("Number of vehicles: " + numVehicles);
        System.out.println("Vehicle capacity: " + vehicleCapacity);

        demandOfCustomer = new int[numCustomers];
        xCoordOfCustomer = new double[numCustomers];
        yCoordOfCustomer = new double[numCustomers];

        for (int i = 0; i < numCustomers; i++) {
            demandOfCustomer[i] = read.nextInt();
            xCoordOfCustomer[i] = read.nextDouble();
            yCoordOfCustomer[i] = read.nextDouble();
        }

        // Find maximum number of customers a vehicle can deliver
        int[] sortedDemands = Arrays.copyOf(demandOfCustomer, demandOfCustomer.length);
        Arrays.sort(sortedDemands);
        maxCustomersPerVehicle = 0;
        int totalDemand = 0;
        for (int c = 0; c < numCustomers; c ++) {
            totalDemand += sortedDemands[c];
            if (totalDemand > vehicleCapacity) {
                break;
            }
            maxCustomersPerVehicle++;
        }
    }

    public Optional<Solution> solve() throws IloException {
        CPInstance cpInstance = new CPInstance(this);
        Timer.cpTimer.start();
        Optional<Solution> feasible = cpInstance.getFeasible();
        Timer.cpTimer.stop();
        if (!feasible.isPresent()) {
            return feasible;
        }
        if (Settings.verbosity > 1) {
            System.out.println("Initial feasibility cost: " + feasible.get().getCost());
        }

        LSInstance lsInstance = new LSInstance(this);
        Timer.lsTimer.start();
        Optional<Solution> solution = Optional.of(lsInstance.solve(feasible.get()));
        Timer.lsTimer.stop();
        return solution;
    }
}
