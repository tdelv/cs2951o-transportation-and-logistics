package solver.ls;

import java.util.Optional;

public abstract class AbstractLocalSearchState<T extends Comparable<T>> {
    abstract T getValue();

    private Optional<T> value = Optional.empty();
    public T getValueRemember() {
        if (value.isPresent()) {
            return value.get();
        } else {
            T result = getValue();
            value = Optional.of(result);
            return result;
        }
    }

    abstract Generator<AbstractLocalSearchState<T>> getNeighbors();

    abstract boolean isValid();

    abstract AbstractLocalSearchState<T> getRandom(double dist);
}
