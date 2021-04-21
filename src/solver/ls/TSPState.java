package solver.ls;

import java.util.ArrayList;
import java.util.List;

public class TSPState extends AbstractLocalSearchState<Double> {
    private VRPInstance problem;
    public List<Integer> order;

    public TSPState(VRPInstance problem, List<Integer> order) {
        this.problem = problem;
        this.order = order;
    }

    public List<Integer> getOrder() {
        return new ArrayList<>(this.order);
    }

    @Override
    public Double getValue() {
        Timer.tspGetValue.start();
        double dist = 0;
        double currX = 0, currY = 0;
        for (Integer loc : order) {
            double newX = problem.xCoordOfCustomer[loc];
            double newY = problem.yCoordOfCustomer[loc];
            dist += Utils.dist(currX, currY, newX, newY);
            currX = newX;
            currY = newY;
        }
        dist += Utils.dist(currX, currY, 0, 0);
        Timer.tspGetValue.stop();
        return dist;
    }

    @Override
    public Generator<AbstractLocalSearchState<Double>> getNeighbors() {
        return new Generator<AbstractLocalSearchState<Double>>() {
            @Override
            protected void run() throws InterruptedException {
                for (int i = 0; i < order.size(); i ++) {
                    for (int j = i + 1; j < order.size(); j ++) {
                        Timer.tspGetNeighbors.start();
                        List<Integer> newOrder = new ArrayList<>(order);
                        int temp = newOrder.get(i);
                        newOrder.set(i, newOrder.get(j));
                        newOrder.set(j, temp);
                        Timer.tspGetNeighbors.stop();
                        this.yield(new TSPState(problem, newOrder));
                    }
                }
            }
        };
    }

    @Override
    boolean isValid() {
        return true;
    }

    @Override
    AbstractLocalSearchState<Double> getRandom(double dist) {
        int trueDist = (int) Math.ceil(dist * order.size());

        List<Integer> newOrder = new ArrayList<>(order);

        for (int i = 0; i < trueDist; i ++) {
            int ind0 = Settings.rand.nextInt(newOrder.size());
            int ind1 = Settings.rand.nextInt(newOrder.size());
            Integer temp = newOrder.get(ind0);
            newOrder.set(ind0, newOrder.get(ind1));
            newOrder.set(ind1, temp);
        }

        return new TSPState(problem, newOrder);
    }

    @Override
    void print() {
        System.out.print("TSPState:");
        for (Integer loc : order) {
            System.out.print(" " + loc);
        }
        System.out.println();
    }

    @Override
    String getProblem() { return "TSP"; }
}

