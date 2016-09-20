package com.tudelft.sps.indoorlocalizer;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by EinNarr on 2016/6/9.
 */
public class ArrayListFunction {
    public static double getMean(ArrayList<Double> data) {
        double sum = 0;
        for (double d : data) {
            sum += d;
        }
        return sum / data.size();
    }

    public static double getSTD(ArrayList<Double> data) {
        double mean = getMean(data);
        double temp = 0;
        for (double d : data) {
            temp += (mean - d) * (mean - d);
        }
        return Math.sqrt(temp / data.size());
    }

    public static double getRange(ArrayList<Double> data) {
        double max = Collections.max(data);
        double min = Collections.min(data);
        return max - min;
    }
}
