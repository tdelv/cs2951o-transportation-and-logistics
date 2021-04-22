package solver.ls;

import java.util.Optional;

public abstract class AbstractLocalSearchState<T extends Comparable<T>> {
    abstract T getValue(Optional<T> best);

    private Optional<T> value = Optional.empty();
    public T getValueRemember() {
        return getValueRemember(Optional.empty());
    }

    public T getValueRemember(T best) {
        return getValueRemember(Optional.of(best));
    }

    public T getValueRemember(Optional<T> best) {
        if (value.isPresent()) {
            return value.get();
        } else {
            T result = getValue(best);
            value = Optional.of(result);
            return result;
        }
    }

    abstract Generator<AbstractLocalSearchState<T>> getNeighbors();

    abstract boolean isValid();

    abstract AbstractLocalSearchState<T> getRandom(double dist);

    abstract void print();

    abstract String getProblem();
}
