package es.bsc.clopla_benchmarking.models;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class ClusterLoadTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void cpuLoadCannotBeNegative() {
        exception.expect(IllegalArgumentException.class);
        new ClusterLoad(-1, 1, 1);
    }

    @Test
    public void ramLoadCannotBeNegative() {
        exception.expect(IllegalArgumentException.class);
        new ClusterLoad(1, -1, 1);
    }

    @Test
    public void diskLoadCannotBeNegative() {
        exception.expect(IllegalArgumentException.class);
        new ClusterLoad(1, 1, -1);
    }

}
