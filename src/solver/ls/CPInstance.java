package solver.ls;

import ilog.concert.*;
import ilog.cp.IloCP;

import java.util.*;

public class CPInstance {
    private static Map<VRPInstance, CPInstance> cpInstances = new HashMap<>();
    private VRPInstance problem;
    private IloCP cp;



    private CPInstance(VRPInstance problem) {
        this.problem = problem;
        this.cp = null;
    }

    public static CPInstance getInstance(VRPInstance problem) {
        if (cpInstances.containsKey(problem)) {
            return cpInstances.get(problem);
        }

        CPInstance cpInstance = new CPInstance(problem);
        cpInstances.put(problem, cpInstance);
        return cpInstance;
    }

    private void start() throws IloException {
        this.cp = new IloCP();
        this.cp.setParameter(IloCP.IntParam.RandomSeed, Settings.rand.nextInt(100));
        if (Settings.verbosity < 5) {
            this.cp.setOut(null);
        }
    }

    public Optional<Solution> getFeasible() throws IloException {
        List<List<Integer>> bins = new ArrayList<>();
        for (int v = 0; v < problem.numVehicles; v++) {
            bins.add(new ArrayList<>());
        }

//        return solveBin(bins);
        return this.managerQuadrantBins(bins);
    }

    public Optional<Solution> solveQuadrantBin(List<List<Integer>> bins, int numVehiclesPerQuad,
                                 int numCustomersInQuad,
                                 ArrayList<Integer> customerIndices) throws IloException {
        start();

        IloIntExpr[] vehicleLoads = cp.intVarArray(numVehiclesPerQuad, 0, problem.vehicleCapacity);

        IloIntVar[] whichVehicle = new IloIntVar[numCustomersInQuad];
        for (int c = 0; c < numCustomersInQuad; c ++) {
            Optional<Integer> whichBin = Optional.empty();
            for (int binInd = 0; binInd < bins.size(); binInd ++) {
                if (bins.get(binInd).contains(c)) {
                    whichBin = Optional.of(binInd);
                    break;
                }
            }
            if (whichBin.isPresent()) {
                whichVehicle[c] = cp.intVar(new int[] { whichBin.get() });
            } else {
                whichVehicle[c] = cp.intVar(0, numVehiclesPerQuad);
            }
        }

        int[] demand = new int[numCustomersInQuad];
        for (int c = 0; c < numCustomersInQuad; c ++) {
            int customerIndex = customerIndices.get(c);
            demand[c] = problem.demandOfCustomer[customerIndex];
        }

        IloConstraint pack = cp.pack(vehicleLoads, whichVehicle, demand);
        cp.add(pack);

        // Solves
        Optional<Solution> result;
        if (cp.solve()) {
            List<List<Integer>> newBins = new ArrayList<>();
            for (int v = 0; v < numVehiclesPerQuad; v ++) {
                newBins.add(new ArrayList<>());
            }

            for (int c = 0; c < numCustomersInQuad; c ++) {
                int bin = (int) cp.getValue(whichVehicle[c]);
                newBins.get(bin).add(customerIndices.get(c));
            }

            result = Optional.of(new Solution(problem, newBins));
        } else {
            result = Optional.empty();
        }
        cp.end();
        return result;
    }

    public Optional<Solution> managerQuadrantBins(List<List<Integer>> bins) throws IloException{

        Optional<Solution> quadResults;

        if (problem.numVehicles < 8) {

            // Get customers in each quadrant
            ArrayList<Integer> upperQuadCustomerIndices = new ArrayList<>();
            ArrayList<Integer> lowerQuadCustomerIndices = new ArrayList<>();

            for (int c =1; c < problem.numCustomers; c ++) {
                int customerQuad = problem.quadrantOfCustomer[c];
                if ((customerQuad == 1) || (customerQuad == 2)) {
                    upperQuadCustomerIndices.add(c);
                }
                else {
                    lowerQuadCustomerIndices.add(c);
                }
//                System.out.println(upperQuadCustomerIndices);
//                System.out.println(lowerQuadCustomerIndices);
            }

            // Get customer demand in each quadrant
            int upperQuadSumDemand = 0;
            for (Integer customer : upperQuadCustomerIndices) {
                upperQuadSumDemand += problem.demandOfCustomer[customer];
            }
            int lowerQuadSumDemand = 0;
            for (Integer customer : lowerQuadCustomerIndices) {
                lowerQuadSumDemand += problem.demandOfCustomer[customer];
            }
            int totalCustomerDemand = upperQuadSumDemand + lowerQuadSumDemand;

            // Get number of vehicles in each quadrant
//            int upperQuadVehicles = (int) Math.ceil(problem.numVehicles / 2.0);
//            int lowerQuadVehicles = (int) Math.floor(problem.numVehicles / 2.0);
            double proportionOfDemand = ((double) upperQuadSumDemand) / totalCustomerDemand;
            System.out.println("proportion of demand = " + proportionOfDemand);
            int upperQuadVehicles = (int) Math.round(problem.numVehicles *
                    proportionOfDemand);
            int lowerQuadVehicles = problem.numVehicles - upperQuadVehicles;

            // Solve
            Optional<Solution> upperQuadSol = this.solveQuadrantBin(bins, upperQuadVehicles,
                    upperQuadCustomerIndices.size(), upperQuadCustomerIndices);
            Optional<Solution> lowerQuadSol = this.solveQuadrantBin(bins, lowerQuadVehicles,
                    lowerQuadCustomerIndices.size(), lowerQuadCustomerIndices);


            if (upperQuadSol.isPresent() && lowerQuadSol.isPresent()) {
                System.out.println("yay quads worked");
                // Combine solutions
                List<List<Integer>> joinedPaths = upperQuadSol.get().getPaths();
                joinedPaths.addAll(lowerQuadSol.get().getPaths());
                System.out.println("joined paths is " + joinedPaths);
                return Optional.of(new Solution(problem, joinedPaths));
            } else {
                System.out.println("sad quads no work");
                System.out.println("Vehicles:");
                System.out.println(upperQuadVehicles + " " + lowerQuadVehicles);
                return this.solveBin(bins);
            }
        }
        else {

            // Get customers in each quadrant
            ArrayList<Integer> quad1CustomerIndices = new ArrayList<>();
            ArrayList<Integer> quad2CustomerIndices = new ArrayList<>();
            ArrayList<Integer> quad3CustomerIndices = new ArrayList<>();
            ArrayList<Integer> quad4CustomerIndices = new ArrayList<>();

            for (int c =1; c < problem.numCustomers-1; c ++) {
                int customerQuad = problem.quadrantOfCustomer[c];
                if (customerQuad == 1) {
                    quad1CustomerIndices.add(c);
                }
                else if (customerQuad == 2){
                    quad2CustomerIndices.add(c);
                }
                else if (customerQuad == 3) {
                    quad3CustomerIndices.add(c);
                } else {
                    quad4CustomerIndices.add(c);
                }
            }

            // Get number of vehicles in each quadrant
//            int upperQuadVehicles = (int) Math.ceil(problem.numVehicles / 2.0);
//            int lowerQuadVehicles = (int) Math.floor(problem.numVehicles / 2.0);
//
//            int quad1Vehicles = (int) Math.ceil(upperQuadVehicles / 2.0);
//            int quad2Vehicles = (int) Math.floor(upperQuadVehicles / 2.0);
//            int quad3Vehicles = (int) Math.ceil(lowerQuadVehicles / 2.0);
//            int quad4Vehicles = (int) Math.floor(lowerQuadVehicles / 2.0);

            int quad1SumDemand = 0;
            for (Integer customer : quad1CustomerIndices) {
                quad1SumDemand += problem.demandOfCustomer[customer];
            }
            int quad2SumDemand = 0;
            for (Integer customer : quad2CustomerIndices) {
                quad2SumDemand += problem.demandOfCustomer[customer];
            }
            int quad3SumDemand = 0;
            for (Integer customer : quad3CustomerIndices) {
                quad3SumDemand += problem.demandOfCustomer[customer];
            }
            int quad4SumDemand = 0;
            for (Integer customer : quad4CustomerIndices) {
                quad4SumDemand += problem.demandOfCustomer[customer];
            }
            int totalCustomerDemand = quad1SumDemand + quad2SumDemand + quad3SumDemand + quad4SumDemand;

            double q1ProportionOfDemand = ((double) quad1SumDemand) / totalCustomerDemand;
//            System.out.println("proportion of demand = " + proportionOfDemand);
            int quad1Vehicles = (int) Math.round(problem.numVehicles *
                    q1ProportionOfDemand);
            double q2ProportionOfDemand = ((double) quad2SumDemand) / totalCustomerDemand;
            int quad2Vehicles = (int) Math.round(problem.numVehicles *
                    q2ProportionOfDemand);
            double q3ProportionOfDemand = ((double) quad3SumDemand) / totalCustomerDemand;
            int quad3Vehicles = (int) Math.round(problem.numVehicles *
                    q3ProportionOfDemand);
            int quad4Vehicles = problem.numVehicles - quad1Vehicles - quad2Vehicles - quad3Vehicles;

            // Solve
            Optional<Solution> quad1Sol = this.solveQuadrantBin(bins, quad1Vehicles,
                    quad1CustomerIndices.size(), quad1CustomerIndices);
            Optional<Solution> quad2Sol = this.solveQuadrantBin(bins, quad2Vehicles,
                    quad2CustomerIndices.size(), quad2CustomerIndices);
            Optional<Solution> quad3Sol = this.solveQuadrantBin(bins, quad3Vehicles,
                    quad3CustomerIndices.size(), quad3CustomerIndices);
            Optional<Solution> quad4Sol = this.solveQuadrantBin(bins, quad4Vehicles,
                    quad4CustomerIndices.size(), quad4CustomerIndices);

            if (quad1Sol.isPresent() && quad2Sol.isPresent() && quad3Sol.isPresent()
                    && quad4Sol.isPresent()) {
                System.out.println("yay quads worked");
                // Combine solutions
                List<List<Integer>> joinedPaths = quad1Sol.get().getPaths();
                joinedPaths.addAll(quad2Sol.get().getPaths());
                joinedPaths.addAll(quad3Sol.get().getPaths());
                joinedPaths.addAll(quad4Sol.get().getPaths());
                System.out.println("joined paths is " + joinedPaths);
                return Optional.of(new Solution(problem, joinedPaths));
            } else {
                System.out.println("sad quads no work");
                System.out.println("Vehicles:");
                System.out.println(quad1Vehicles + " " + quad2Vehicles + " " + quad3Vehicles +
                        " " + quad4Vehicles);
                return this.solveBin(bins);
            }
        }
    }

    public Optional<Solution> solveBin(List<List<Integer>> bins) throws IloException {
        start();

        IloIntExpr[] vehicleLoads = cp.intVarArray(problem.numVehicles, 0, problem.vehicleCapacity);

        IloIntVar[] whichVehicle = new IloIntVar[problem.numCustomers - 1];
        for (int c = 1; c < problem.numCustomers; c ++) {
            Optional<Integer> whichBin = Optional.empty();
            for (int binInd = 0; binInd < bins.size(); binInd ++) {
                if (bins.get(binInd).contains(c)) {
                    whichBin = Optional.of(binInd);
                    break;
                }
            }
            if (whichBin.isPresent()) {
                whichVehicle[c - 1] = cp.intVar(new int[] { whichBin.get() });
            } else {
                whichVehicle[c - 1] = cp.intVar(0, problem.numVehicles - 1);
            }
        }

        int[] demand = new int[problem.numCustomers - 1];
        for (int c = 0; c < problem.numCustomers - 1; c ++) {
            demand[c] = problem.demandOfCustomer[c + 1];
        }

        IloConstraint pack = cp.pack(vehicleLoads, whichVehicle, demand);
        cp.add(pack);

        IloNumExpr wanderingTrucks = cp.numExpr();

        for (int c1 = 1; c1 < problem.numCustomers; c1 ++) {
            for (int c2 = c1; c2 < problem.numCustomers; c2 ++) {
//                if (problem.quadrantOfCustomer[c1] != problem.quadrantOfCustomer[c2]){
//                    IloNumVar truckIsWandering = cp.numVar(0,1);
//
//                    cp.add(cp.ifThenElse(
//                            cp.eq(whichVehicle[c1-1], whichVehicle[c2-1]),
//                            cp.eq(truckIsWandering, 1),
//                            cp.eq(truckIsWandering, 0)));
//
//                    cp.add(cp.ifThenElse(
//                            cp.eq(whichVehicle[c1-1], whichVehicle[c2-1]),
//                            cp.eq(truckIsWandering, 1),
//                            cp.eq(truckIsWandering, 0)));
//
//                    wanderingTrucks = cp.sum(wanderingTrucks, truckIsWandering);
//                }

                IloNumVar customerSparsity = cp.numVar(0,1000000);

                int customerPairDistance = (int) Math.sqrt(Math.pow(
                        problem.xCoordOfCustomer[c1] - problem.xCoordOfCustomer[c2], 2)
                + Math.pow(problem.yCoordOfCustomer[c1]-problem.yCoordOfCustomer[c2], 2));

                cp.add(cp.ifThenElse(
                        cp.eq(whichVehicle[c1-1], whichVehicle[c2-1]),
                        cp.eq(customerSparsity, customerPairDistance),
                        cp.eq(customerSparsity, 0)));

                wanderingTrucks = cp.sum(wanderingTrucks, customerSparsity);

            }
        }

        cp.add(cp.minimize(wanderingTrucks));
//        cp.add(cp.le(wanderingTrucks, 4000));

        cp.setParameter(IloCP.DoubleParam.TimeLimit, 15);

        // Solves
        Optional<Solution> result;
        if (cp.solve()) {
            List<List<Integer>> newBins = new ArrayList<>();
            for (int v = 0; v < problem.numVehicles; v ++) {
                newBins.add(new ArrayList<>());
            }

            for (int c = 1; c < problem.numCustomers; c ++) {
                int bin = (int) cp.getValue(whichVehicle[c - 1]);
                newBins.get(bin).add(c);
            }

            result = Optional.of(new Solution(problem, newBins));
        } else {
            result = Optional.empty();
        }
        cp.end();
        return result;
    }

    public List<Integer> solveTSP(List<Integer> bin) throws IloException {
        start();

        int[] binValues = bin.stream().mapToInt(Integer::intValue).toArray();

        IloIntVar[] vars = cp.intVarArray(bin.size(), binValues, "TSP");

        cp.add(cp.allDiff(vars));

        IloNumExpr cost = cp.numExpr();
        IloNumExpr startX = cp.sum(cp.numExpr(), problem.xCoordOfCustomer[0]);
        IloNumExpr startY = cp.sum(cp.numExpr(), problem.yCoordOfCustomer[0]);
        IloNumExpr currX = startX;
        IloNumExpr currY = startY;

        for (int i = 0; i < vars.length; i ++) {
            IloNumExpr newX = cp.element(problem.xCoordOfCustomer, vars[i]);
            IloNumExpr newY = cp.element(problem.yCoordOfCustomer, vars[i]);
            cost = cp.sum(cost, distance(currX, currY, newX, newY));
            currX = newX;
            currY = newY;
        }

        cost = cp.sum(cost, distance(currX, currY, startX, startY));
        cp.addMinimize(cost);

        cp.setParameter(IloCP.DoubleParam.TimeLimit, Settings.tspSearchTime);

        List<Integer> result;
        if (cp.solve()) {
            result = new ArrayList<>();
            for (int i = 0; i < vars.length; i ++) {
                result.add((int) cp.getValue(vars[i]));
            }
        } else {
            System.err.println("TSP was unsat.");
            System.exit(1);
            result = null;
        }
        cp.end();
        return result;
    }

    private IloNumExpr distance(IloNumExpr x1, IloNumExpr y1, IloNumExpr x2, IloNumExpr y2) throws IloException {
        IloNumExpr oneHalf = cp.sum(cp.numExpr(), 0.5);
        return cp.power(cp.sum(cp.square(cp.diff(x2, x1)), cp.square(cp.diff(y2, y1))), oneHalf);
    }
}
