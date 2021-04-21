package solver.ls;

import javax.swing.text.html.Option;
import java.util.Optional;

abstract class AbstractLocalSearch<T extends Comparable<T>, State extends AbstractLocalSearchState<T>> {
    abstract State getInitial();

    public State search(int maxDist) {
        return search(Optional.of(maxDist), Optional.empty());
    }

    public State search(double maxTime) {
        return search(Optional.empty(), Optional.of(maxTime));
    }

    public State search(int maxDist, double maxTime) {
        return search(Optional.of(maxDist), Optional.of(maxTime));
    }

    private State search(Optional<Integer> maxDist, Optional<Double> maxTime) {
        State best = getInitial();

        int dist = 1;
        Timer timer = new Timer();
        timer.start();
        while (!maxDist.isPresent() || dist <= maxDist.get()) {
            T bestValue = best.getValueRemember();
            Optional<State> newBest = Optional.empty();
            for (State neighbor : getValidNeighbors(best, dist)) {
                if (maxTime.isPresent() && timer.getCurrentTime() > maxTime.get()) {
                    return newBest.orElse(best);
                }
                T neighborValue = neighbor.getValueRemember();
                if (neighborValue.compareTo(bestValue) < 0) {
                    if (!newBest.isPresent() || neighborValue.compareTo(newBest.get().getValueRemember()) < 0) {
                        newBest = Optional.of(neighbor);
                    }
                }
            }

            if (newBest.isPresent()) {
                best = newBest.get();
                dist = 1;
            } else {
                dist += 1;
            }
        }

        return best;
    }

    private Generator<State> getNeighbors(State state, int dist) {
        return new Generator<State>() {
            @Override
            protected void run() throws InterruptedException {
                if (dist == 0) {
                    this.yield(state);
                    return;
                }

                for (Object swapObj : state.getNeighbors()) {
                    State swapState = (State) swapObj;
                    for (State neighbor : getNeighbors(swapState, dist - 1)) {
                        this.yield(neighbor);
                    }
                }
            }
        };
    }

    Generator<State> getValidNeighbors(State state, int dist) {
        return new Generator<State>() {
            @Override
            protected void run() throws InterruptedException {
                for (State neighbor : getNeighbors(state, dist)) {
                    if (neighbor.isValid()) {
                        this.yield(neighbor);
                    }
                }
            }
        };
    }
}
