package es.bsc.amon.mq.dispatch.aggregations;

import java.util.Arrays;
import java.util.Collections;

/**
 * Created by mmacias on 3/6/16.
 */
public class PercentileFunction extends Function {
    int percentile;

    public PercentileFunction(int percentile) {
        this.percentile = percentile;
    }

    @Override
    public double calculate(double[] values) {
        Arrays.sort(values);
        int index = (int)((percentile * (values.length - 1)) / 100);
        double val;
        if(index >= values.length - 1) {
            val = values[values.length - 1];
        } else {
            // hack: we calculate the linear interpolation between both values
            // to do some "significative" information when there are very few measueres
            val = values[index] * percentile + values[index+1] * (100-percentile);
            val /= 100.0;
        }
        return val;
    }
}
