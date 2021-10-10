package eda.data;

import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class StatisticsCalculator {
    // calculate min, max, mean, quantiles
    public Map<String, Object> getBasicStatistics(List<Integer> values) {
        if (values.isEmpty())
            return Map.of();

        Collections.sort(values);
        int count = values.size();
        int min = values.get(0);
        int max = values.get(count - 1);

        BigInteger sum = BigInteger.ZERO;
        for (int value : values)
            sum = sum.add(BigInteger.valueOf(value));
        double mean = sum.doubleValue() / count;
        int q1 = values.get(count / 4);
        int q2 = values.get(count / 2);
        int q3 = values.get(count * 3 / 4);

        return Map.of("min", min,
                "max", max,
                "mean", mean,
                "q1", q1,
                "q2", q2,
                "q3", q3);
    }
}
