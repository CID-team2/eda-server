package eda.data;

import java.util.*;

public class StatisticsCalculator {
    private StatisticsCalculator() {
        throw new IllegalStateException("Utility class");
    }

    // calculate mean, stdev
    public static <T extends Number> Map<String, Object> getNumericStatistics(List<T> values) {
        if (values.isEmpty())
            return Map.of();

        int count = values.size();
        double sum = 0;
        for (T value : values)
            sum += value.doubleValue();
        double mean = sum / count;
        double sumSquareDev = 0;
        for (T value: values) {
            double doubleValue = value.doubleValue();
            sumSquareDev += (mean - doubleValue) * (mean - doubleValue);
        }
        double variance = sumSquareDev / count;
        double stdev = Math.sqrt(variance);
        return Map.of("mean", mean,
                "stdev", stdev);
    }

    // calculate min, max, quantiles
    public static <T extends Comparable<? super T>> Map<String, Object> getOrdinalStatistics(List<T> values) {
        if (values.isEmpty())
            return Map.of();

        List<T> valuesCopy = new ArrayList<>(values.size());
        valuesCopy.addAll(values);
        Collections.sort(valuesCopy);
        int count = valuesCopy.size();
        Object min = valuesCopy.get(0);
        Object max = valuesCopy.get(count - 1);
        Object q1 = valuesCopy.get(count / 4);
        Object q2 = valuesCopy.get(count / 2);
        Object q3 = valuesCopy.get(count * 3 / 4);

        return Map.of("min", min,
                "max", max,
                "q1", q1,
                "q2", q2,
                "q3", q3);
    }

    public static Object getMode(List<Object> values) {
        if (values.isEmpty())
            return null;

        Map<Object, Integer> count = new HashMap<>();
        for (Object value : values)
            count.merge(value, 1, Integer::sum);

        int maxCount = 0;
        Object mode = null;
        for (Map.Entry<Object, Integer> entry : count.entrySet()) {
            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                mode = entry.getKey();
            }
        }
        return mode;
    }

    // TODO: add methods
    // Boxplot
    // used double values because I need to compute IQR and find whiskers, which is impossible when it is Number
    public static <T extends Number> Map<String, Object> getBoxplot(List<T> values) {
        if (values.isEmpty())
            return Map.of();

        List<Double> valuesCopy = new ArrayList<>(values.size());
        for (T value : values) valuesCopy.add(value.doubleValue());
        Collections.sort(valuesCopy);
        int count = valuesCopy.size();
        double q1 = valuesCopy.get(count / 4);
        double q2 = valuesCopy.get(count / 2);
        double q3 = valuesCopy.get(count * 3 / 4);
        double IQR = q3 - q1;
        double lowerWhiskerLimit = q1 - 1.5 * IQR;
        double upperWhiskerLimit = q3 + 1.5 * IQR;
        double lowerWhisker = valuesCopy.get(0);
        double upperWhisker = valuesCopy.get(count-1);
        List<Double> lowerOutliers = new ArrayList<>();
        List<Double> upperOutliers = new ArrayList<>();
        Iterator<Double> copyIter = valuesCopy.iterator();
        while (copyIter.hasNext()) {
            double item = copyIter.next();
            if (item >= lowerWhiskerLimit) {
                lowerWhisker = item;
                break;
            }
            else
                lowerOutliers.add(item);
        }
        while (copyIter.hasNext()) {
            double item = copyIter.next();
            if (item <= upperWhiskerLimit)
                upperWhisker = item;
            else
                upperOutliers.add(item);
        }

        return Map.of(
                "lowerOutliers", lowerOutliers,
                "lowerWhisker", lowerWhisker,
                "q1", q1,
                "q2", q2,
                "q3", q3,
                "upperWhisker", upperWhisker,
                "upperOutliers", upperOutliers);
    }
    // histogram
    // bar plot
}
