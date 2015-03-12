/**
 * SciMark 2.0 was developed by Roldan Pozo, and Bruce Miller at the 
 * National Institute of Standards and Technology (NIST). 
 * Its code is copyright free and is in the public domain.
 * 
 * http://math.nist.gov/scimark2/index.html
 */
package jnt.scimark2;

/**
 * SciMark2: A Java numerical benchmark measuring performance of computational
 * kernels for FFTs, Monte Carlo simulation, sparse matrix computations, Jacobi
 * SOR, and dense LU matrix factorizations.
 * 
 * @author Roldan Pozo and Bruce Miller and modified by Richard Kavanagh.
 *
 */
public class CommandLine {

    /**
     * This performs a Benchmark with 5 tests each aiming to calculate the Mflops
     * rating of a physical host's CPU.
     * @param args -h or -help provides help information. -large utilises a larger
     * problem size during the benchmarking. Optionally after -large a minimum time
     * for completion can be specified as well.
     */
    public static void main(String args[]) {
        
        Benchmark benchmarker = new Benchmark();
        
        // look for runtime options
        if (args.length > 0) {

            if (args[0].equalsIgnoreCase("-h")
                    || args[0].equalsIgnoreCase("-help")) {
                System.out.println("Usage: [-large] [minimum_time]");
                return;
            }

            int current_arg = 0;
            if (args[current_arg].equalsIgnoreCase("-large")) {
                benchmarker = new Benchmark(true);
                current_arg++;
            }

            if (args.length > current_arg) {
                benchmarker.setMinimumExperimentTime(Double.valueOf(args[current_arg]).doubleValue());
            }
        }

        // run the benchmark
        Result results = benchmarker.getBenchmarkResult();
        results.print();
    }

}
