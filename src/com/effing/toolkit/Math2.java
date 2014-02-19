package com.effing.toolkit;

import java.util.Collections;
import java.util.List;

public final class Math2 {
	public static double getMean(List<Double> values) {
        double mean = 0.0;
        if ((values != null) && (values.size() > 0)) {
            for (double value : values) {
                mean += value;
            }
            mean /= values.size();
        }
        return mean;
    }

    public static double getStandardDeviation(List<Double> values) {
        double deviation = 0.0;
        if ((values != null) && (values.size() > 1)) {
            double mean = getMean(values);
            for (double value : values) {
                double delta = value-mean;
                deviation += delta*delta;
            }
            deviation = Math.sqrt(deviation/values.size());
        }
        return deviation;
    }

    public static double getMedian(List<Double> values) {
        double median = 0.0;
        if (values != null) {
            int numValues = values.size();
            if (numValues > 0) {
                Collections.sort(values);
                if ((numValues%2) == 0) {
                    median = (values.get((numValues/2)-1)+values.get(numValues/2))/2.0;
                } else {
                    median = values.get(numValues/2);
                }
            }
        }
        return median;
    }

}
