package solver.ls;

public class Timer {

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
        System.out.println("Timers:");
    }
}
