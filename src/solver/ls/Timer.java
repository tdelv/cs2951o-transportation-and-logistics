package solver.ls;

public class Timer {

    public static Timer totalTimer = new Timer();
    public static Timer cpTimer = new Timer();
    public static Timer lsTimer = new Timer();
    public static Timer tspTimer = new Timer();
    public static Timer tspGetValue = new Timer();
    public static Timer tspGetNeighbors = new Timer();

    private long startTime;
    private long stopTime;
    private boolean running;
    private double totalTime;

    private final double nano = 1000000000.0;

    public Timer() {
        super();
    }

    public void reset() {
        this.startTime = 0;
        this.running = false;

        this.totalTime = 0;
    }

    public void start() {
        assert !running : "Called start on stopped timer.";
        this.startTime = System.nanoTime();
        this.running = true;
    }

    public void stop() {
        assert running : "Called stop on running timer.";
        this.stopTime = System.nanoTime();
        this.running = false;
        this.totalTime += this.getCurrentTime();
    }

    public double getCurrentTime() {
        double elapsed;
        if (running) {
            elapsed = ((System.nanoTime() - startTime) / nano);
        } else {
            elapsed = ((stopTime - startTime) / nano);
        }
        return elapsed;
    }

    public double getTotalTime() {
        if (running) {
            return this.totalTime + this.getCurrentTime();
        } else {
            return this.totalTime;
        }
    }

    public void addTime(double toAdd) {
        this.totalTime += toAdd;
    }

    public static void printTimers() {
        System.out.println("Total time: " + totalTimer);
        System.out.println("  CP feasible: " + cpTimer);
        System.out.println("  LS search: " + lsTimer);
        System.out.println("    TSP solve: " + tspTimer);
        System.out.println("      TSP getValue: " + tspGetValue);
        System.out.println("      TSP getNeighbors: " + tspGetNeighbors);

    }

    @Override
    public String toString() {
        return String.format("%.2f", this.getTotalTime());
    }
}
