package eda.domain.data;

import java.util.*;

public class StatisticCalculator {
    private StatisticCalculator() {
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
    public static <T extends Number> Map<String, Object> getHistogram(List<T> values, double start, double end, int breaks) {
        // 1. convert to double and 2. sort
        List<Double> doubleValues = new ArrayList<>(values.size());
        for (T value: values) doubleValues.add(value.doubleValue());
        Collections.sort(doubleValues);

        // 3. calculate boundaries array
        List<Double> boundaries = new ArrayList<>(breaks+1);
        double width = (end - start) / breaks;
        for (int i = 0; i < breaks; i++) {
            boundaries.add(start + width * i);
        }
        boundaries.add(end);

        // 4. initialize numbers array
        List<Integer> numbers = new ArrayList<>(breaks);
        for (int i = 0; i < breaks; i++) numbers.add(0);

        // 5. count and save outliers
        List<Double> low_outliers = new ArrayList<>();
        List<Double> high_outliers = new ArrayList<>();
        int bin = 0;
        for (double value: doubleValues) {
            if (bin == 0 && value < boundaries.get(bin)) low_outliers.add(value);
            else {
                while (bin < breaks && value >= boundaries.get(bin+1)) bin++;
                if (bin < breaks && value >= boundaries.get(bin)) numbers.set(bin, numbers.get(bin)+1);
                else if (bin == breaks && value == boundaries.get(bin)) numbers.set(bin-1, numbers.get(bin-1)+1);
                else high_outliers.add(value);
            }
        }

        // 6. return
        return Map.of(
                "boundaries", boundaries,
                "numbers", numbers,
                "low_outliers", low_outliers,
                "high_outliers", high_outliers
        );
    }
    // bar plot

    private static <T extends Number> double getCovariance(List<T> values1, List<T> values2, double mean1, double mean2) {
        if (values1.size() != values2.size())
            throw new IllegalArgumentException("size is different - values1.size: %d, values2.size: %d".formatted(
                    values1.size(), values2.size()
            ));

        final int size = values1.size();
        double sum = 0;
        for (int i = 0; i < size; i++) {
            sum += values1.get(i).doubleValue() * values2.get(i).doubleValue();
        }
        return sum / size - mean1 * mean2;
    }

    public static <T extends Number> double[][] getCorrMatrix(List<List<T>> values) {
        List<Double> means = new ArrayList<>();
        List<Double> stdevs = new ArrayList<>();
        for (List<T> list : values) {
            Map<String, Object> numericStatistic = getNumericStatistics(list);
            means.add((Double) numericStatistic.get("mean"));
            stdevs.add((Double) numericStatistic.get("stdev"));
        }

        final int size = values.size();
        double[][] result = new double[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = i; j < size; j++) {
                if (i == j)
                    result[i][j] = 1;
                else {
                    double cov = getCovariance(values.get(i), values.get(j), means.get(i), means.get(j));
                    result[i][j] = cov / (stdevs.get(i) * stdevs.get(j));
                    result[j][i] = result[i][j];
                }
            }
        }

        return result;
    }
}
