package solver.ls;

import javax.swing.text.html.Option;
import java.util.Optional;

abstract class AbstractLocalSearch<T extends Comparable<T>, State extends AbstractLocalSearchState<T>> {
    abstract State getInitial();

    public State search(int maxDist) {
        return search(Optional.of(maxDist), Optional.empty());
    }

    public State search(double maxTime) {
        return search(Optional.empty(), Optional.of(maxTime)); // GOTO: VRPLocalSearch.search
    }

    public State search(int maxDist, double maxTime) {
        return search(Optional.of(maxDist), Optional.of(maxTime));
    }

    private State search(Optional<Integer> maxDist, Optional<Double> maxTime) {
        if (Settings.lsSearchProcedural) {
            return searchProcedural(maxDist, maxTime);
        } else {
            return searchRandom(maxDist, maxTime); // GOTO: VRPLocalSearch.searchRandom
        }
    }

    private State searchRandom(Optional<Integer> maxDist, Optional<Double> maxTime) {
        State best = getInitial();
        State current = best;

        // Parameters for search

        // Where to reset randDist to when find improvement or reach minimum
        double startRandDist = 1;
        double startRandDistFactor = Settings.startRandDistFactor;

        // Minimum randDist to reach
        double minDist = Settings.minDist;
        double minDistFactor = Settings.minDistFactor;

        // The distance to randomly search within (range [0.0, 1.0])
        double randDist = startRandDist;
        double randDistFactor = Settings.randDistFactor;

        Timer timer = new Timer();
        timer.start();
        int count = 0; // Unused
        while ((!maxDist.isPresent() || count++ <= maxDist.get()) && (!maxTime.isPresent() || timer.getCurrentTime() < maxTime.get())) {
            // Go back to startRandDist if randDist too small
            if (randDist < minDist) {
                minDist *= minDistFactor;
                minDist = Math.max(minDist, 0.01);
                randDist = startRandDist;
                startRandDist *= startRandDistFactor;
                // Reset search to start if startRandDist too small
                if (startRandDist < 0.005) {
                    startRandDist = 1;
                    startRandDistFactor = Settings.startRandDistFactor;
                    minDist = Settings.minDist;
                    minDistFactor = Settings.minDistFactor;
                    randDist = startRandDist;
                    randDistFactor = Settings.randDistFactor;
                }
                Settings.debug(1, "Restart; Min: " + minDist + "; RandDist: " + randDist);
            }
            Settings.debug(8, "RandDist: " + randDist);

            // Get a random state within distance randDist
            State next = (State) current.getRandom(randDist); // GOTO: VRPState.getRandom

            // If this is an improvement, update and reset to startRandDist;
            // otherwise, shrink search radius
            if (lt(next, current)) { // GOTO: VRPState.getValue
                Settings.debug(1, "Better at: " + randDist);
                randDist = startRandDist;
                startRandDist *= startRandDistFactor;
                current = next;
                best = min(best, next);
            } else {
                randDist *= randDistFactor;
            }
        }

        return best;
    }

    private State searchProcedural(Optional<Integer> maxDist, Optional<Double> maxTime) {
        State best = getInitial();
        State current = best;

        double probRandWalk = Settings.probRandWalk;
        double randMaxDist = Settings.randMaxDist;

        int dist = 1;
        Timer timer = new Timer();
        timer.start();
        while (!maxDist.isPresent() || dist <= maxDist.get()) {
            if (Settings.verbosity >= 3) {
                current.print();
            }
            // Take a random step
            if (dist == 1 && (Settings.rand.nextDouble() < probRandWalk)) {
                Settings.debug(3, "Random step! " + best.getProblem());
                current = (State) current.getRandom(Settings.rand.nextDouble() * randMaxDist);
                randMaxDist *= Settings.probRandWalkFactor;
                probRandWalk *= Settings.probRandWalkFactor;
                continue;
            }

            Optional<State> newCurrent = Optional.empty();
            for (State neighbor : getValidNeighbors(current, dist)) {
                if (best.getProblem().equals("VRP") && Settings.rand.nextDouble() < probRandWalk) {
                    Settings.debug(3, "Random neighbor! " + best.getProblem());
                    neighbor = (State) current.getRandom(Settings.rand.nextDouble() * Settings.randMaxDist);
                }
                if (maxTime.isPresent() && timer.getCurrentTime() > maxTime.get()) {
                    current = newCurrent.orElse(current);
                    return min(current, best);
                }

                neighbor.getValueRemember(current.getValueRemember());
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
                        if (state.getProblem().equals("VRP")) {
                            System.out.println("Found valid.");
                        }
                        this.yield(neighbor);
                    } else {
                        if (state.getProblem().equals("VRP")) {
                            System.out.println("Found invalid.");
                        }
                    }
                }
            }
        };
    }
}
