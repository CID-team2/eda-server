package eda.data;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.*;

@Component
public class StatisticsCalculator {
    // calculate mean, stdev
    public <T extends Number> Map<String, Object> getNumericStatistics(List<T> values) {
        if (values.isEmpty())
            return null;

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
    public <T extends Comparable<? super T>> Map<String, Object> getOrdinalStatistics(List<T> values) {
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

    public Object getMode(List<Object> values) {
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
}
