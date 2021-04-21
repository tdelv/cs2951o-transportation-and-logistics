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
        State current = best;

        double probRandWalk = Settings.probRandWalk;

        int dist = 1;
        Timer timer = new Timer();
        timer.start();
        while (!maxDist.isPresent() || dist <= maxDist.get()) {
            if (Settings.verbosity >= 3) {
                current.print();
            }
            // Take a random step
            if (dist == 1 && (Settings.rand.nextDouble() < probRandWalk)) {
                if (Settings.verbosity >= 3) {
                    System.out.println("Random step!" + best.getProblem());
                }
                probRandWalk *= Settings.probRandWalkFactor;
                current = (State) current.getRandom(Settings.rand.nextDouble() * Settings.randMaxDist);
                continue;
            }

            Optional<State> newCurrent = Optional.empty();
            for (State neighbor : getValidNeighbors(current, dist)) {
                if (Settings.rand.nextDouble() < probRandWalk) {
                    if (Settings.verbosity >= 3) {
                        System.out.println("Random neighbor! " + best.getProblem());
                    }
                    neighbor = (State) current.getRandom(Settings.rand.nextDouble() * Settings.randMaxDist);
                }
                if (maxTime.isPresent() && timer.getCurrentTime() > maxTime.get()) {
                    current = newCurrent.orElse(current);
                    return min(current, best);
                }

                if (newCurrent.isPresent()) {
                    newCurrent = Optional.of(min(newCurrent.get(), neighbor));
                } else {
                    if (lt(neighbor, current)) {
                        newCurrent = Optional.of(neighbor);

                        if (Settings.lsTakeFirst) {
                            best = min(best, neighbor);
                            break;
                        }
                    }
                }

                best = min(best, neighbor);
            }

            if (newCurrent.isPresent()) {
                current = newCurrent.get();
                dist = 1;
            } else {
                dist += 1;
            }
        }

        return best;
    }

    private boolean lt(State s1, State s2) {
        return s1.getValueRemember().compareTo(s2.getValueRemember()) < 0;
    }

    private State min(State s1, State s2) {
        return lt(s1, s2) ? s1 : s2;
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
