package net.sourceforge.jFuzzyLogic.testCases;

import java.io.File;
import junit.framework.TestCase;
import net.sourceforge.jFuzzyLogic.FIS;
import net.sourceforge.jFuzzyLogic.Gpr;
import net.sourceforge.jFuzzyLogic.JFuzzyLogic;
import org.junit.Assert;
import org.junit.Test;

public class TestCaseCommandLine extends TestCase {

    public static final double EPSILON = 1e-6;

    @Test
    public void test() {
        Gpr.debug("Test");

        // Prepare command line
        String fileName = new File(".").getAbsolutePath();
        fileName = fileName.substring(0, fileName.length() - 1);
        fileName = fileName + "tests" + File.separator + "tipper.fcl";
        String args[] = {"-noCharts", "-e", fileName, "8.5", "9"};

        // Run
        JFuzzyLogic jFuzzyLogic = new JFuzzyLogic(args);
        jFuzzyLogic.run();
        FIS fis = jFuzzyLogic.getFis();

        // Check input variables
        Assert.assertEquals(fis.getVariable("food").getValue(), 8.5, EPSILON);
        Assert.assertEquals(fis.getVariable("service").getValue(), 9, EPSILON);
    }

}
