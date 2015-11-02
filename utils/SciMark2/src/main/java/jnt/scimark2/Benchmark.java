/**
 * SciMark 2.0 was developed by Roldan Pozo, and Bruce Miller at the National
 * Institute of Standards and Technology (NIST). Its code is copyright free and
 * is in the public domain.
 *
 * http://math.nist.gov/scimark2/index.html
 */
package jnt.scimark2;

/**
 * This is a variation of the SciMark2 benchmark. It is designed in a
 * more object oriented fashion. Benchmark is the main entry point for
 * performing benchmarks with the suite of tests.
 *
 * SciMark2: A Java numerical benchmark measuring performance of computational
 * kernels for FFTs, Monte Carlo simulation, sparse matrix computations, Jacobi
 * SOR, and dense LU matrix factorisations.
 * 
 * @author Roldan Pozo and Bruce Miller and modified by Richard Kavanagh.
 *
 */
public class Benchmark {

    // default to the (small) cache-contained version
    private double minimumExperimentTime = Constants.RESOLUTION_DEFAULT;
    private int fftSize = Constants.FFT_SIZE;
    private int sorSize = Constants.SOR_SIZE;
    private int sparseSizeM = Constants.SPARSE_SIZE_M;
    private int SparseSizeNz = Constants.SPARSE_SIZE_nz;
    private int luSize = Constants.LU_SIZE;

    /**
     * Creates a new benchmark test instance, using the default small instance.
     */
    public Benchmark() {
    }

    /**
     * Creates a new benchmark test instance.
     *
     * @param large If the instance type should use the larger test parameters.
     */
    public Benchmark(boolean large) {
        if (large == true) {
            fftSize = Constants.LG_FFT_SIZE;
            sorSize = Constants.LG_SOR_SIZE;
            sparseSizeM = Constants.LG_SPARSE_SIZE_M;
            SparseSizeNz = Constants.LG_SPARSE_SIZE_nz;
            luSize = Constants.LG_LU_SIZE;
        }
    }

    /**
     * This performs a benchmark with the SciMark 2.0 tests. This is a set of 5
     * individual tests aiming to report on the Mflops, rating of a physical
     * hosts CPU.
     *
     * @return The results of the benchmark.
     */
    public Result getBenchmarkResult() {
        Random rnd = new Random(Constants.RANDOM_SEED);
        Result result = new Result(fftSize, sorSize, sparseSizeM, SparseSizeNz, luSize);

        result.setFft(Kernel.measureFFT(fftSize, minimumExperimentTime, rnd));
        result.setSor(Kernel.measureSOR(sorSize, minimumExperimentTime, rnd));
        result.setMonteCarlo(Kernel.measureMonteCarlo(minimumExperimentTime, rnd));
        result.setSparseMatmult(Kernel.measureSparseMatmult(sparseSizeM,
                SparseSizeNz, minimumExperimentTime, rnd));
        result.setLu(Kernel.measureLU(luSize, minimumExperimentTime, rnd));
        return result;
    }

    /**
     * This returns the minimum time of the experiment
     *
     * @return the minimum time for which the experiment should run (Units:
     * seconds)
     */
    public double getMinimumExperimentTime() {
        return minimumExperimentTime;
    }

    /**
     * This allows the minimum time of the experiment to be set
     *
     * @param minExperimentTime the minimum time for which the experiment should
     * run (Units: seconds)
     */
    public void setMinimumExperimentTime(double minExperimentTime) {
        this.minimumExperimentTime = minExperimentTime;
    }

}
