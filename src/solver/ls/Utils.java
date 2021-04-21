package solver.ls;

import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static double dist(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    }

    public static <T> List<List<T>> doubleClone(List<List<T>> origList) {
        List<List<T>> newList = new ArrayList<>();
        for (List<T> inner : origList) {
            newList.add(new ArrayList<>(inner));
        }
        return newList;
    }
}
