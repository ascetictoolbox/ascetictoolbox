package es.bsc.power_button_presser.historicaldata;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HistoricalCpuDemandTest {
    
    @Test
    public void addCpuDemandPointWithoutSurpassingMaxNumElems() {
        HistoricalCpuDemand historicalCpuDemand = new HistoricalCpuDemand(1000);
        historicalCpuDemand.addCpuDemandPoint(10);
        assertEquals(1, historicalCpuDemand.getCpuDemandValues().size());
        assertEquals(10, (int) historicalCpuDemand.getCpuDemandValues().get(0));
    }
    
    @Test
    public void addCpuDemandPointSurpassingMaxNumElems() {
        HistoricalCpuDemand historicalCpuDemand = new HistoricalCpuDemand(1);
        historicalCpuDemand.addCpuDemandPoint(10);
        historicalCpuDemand.addCpuDemandPoint(20);
        assertEquals(1, historicalCpuDemand.getCpuDemandValues().size());
        assertEquals(20, (int) historicalCpuDemand.getCpuDemandValues().get(0));
    }
    
}
