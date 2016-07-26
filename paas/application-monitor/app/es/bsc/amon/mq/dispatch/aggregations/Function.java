package es.bsc.amon.mq.dispatch.aggregations;

import java.util.List;

/**
 * Created by mmacias on 2/6/16.
 */
public abstract class Function {
    public abstract double calculate(double[] values);
}
