/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package jnt.scimark2;

/**
 * This stores the results of the benchmarking process. The benchmarks that are
 * run are: Fast Fourier Transforms (FFTs), Jacobi Successive Over-relaxation
 * (SOR), Sparse matrix-multiply, Monte Carlo integration, and dense LU matrix
 * factorisation.
 *
 * @author Richard Kavanagh
 */
public class Result {

    //Settings
    private final int fftSize;
    private final int sorSize;
    private final int sparseSizeM;
    private final int SparseSizeNz;
    private final int luSize;
    //Results
    private double fft;
    private double sor;
    private double monteCarlo;
    private double sparseMatmult;
    private double lu;

    /**
     * This creates a new result file. The settings the results were obtained
     * should be entered.
     *
     * @param fftSize
     * @param sorSize
     * @param sparseSizeM
     * @param SparseSizeNz
     * @param luSize
     */
    public Result(int fftSize, int sorSize, int sparseSizeM, int SparseSizeNz, int luSize) {
        this.fftSize = fftSize;
        this.sorSize = sorSize;
        this.sparseSizeM = sparseSizeM;
        this.SparseSizeNz = SparseSizeNz;
        this.luSize = luSize;
    }

    /**
     * This returns the results for the Fast Fourier Transforms (FFTs)
     * benchmark.
     *
     * @return the result for the Fast Fourier Transforms (FFTs) benchmark
     */
    public double getFft() {
        return fft;
    }

    /**
     * This sets the results for the Fast Fourier Transforms (FFTs) benchmark.
     *
     * @param fft the result for the Fast Fourier Transforms (FFTs) benchmark
     */
    public void setFft(double fft) {
        this.fft = fft;
    }

    /**
     * This returns the results for the Jacobi Successive Over-relaxation (SOR)
     * benchmark.
     *
     * @return the result for the Jacobi Successive Over-relaxation (SOR)
     * benchmark
     */
    public double getSor() {
        return sor;
    }

    /**
     * This sets the results for the Jacobi Successive Over-relaxation (SOR)
     * benchmark.
     *
     * @param sor the result for the Jacobi Successive Over-relaxation (SOR)
     * benchmark
     */
    public void setSor(double sor) {
        this.sor = sor;
    }

    /**
     * This returns the results for the Monte Carlo integration benchmark.
     *
     * @return the result for the Monte Carlo integration benchmark
     */
    public double getMonteCarlo() {
        return monteCarlo;
    }

    /**
     * This returns the results for the Monte Carlo integration benchmark.
     *
     * @param monteCarlo the result for the Monte Carlo integration benchmark
     */
    public void setMonteCarlo(double monteCarlo) {
        this.monteCarlo = monteCarlo;
    }

    /**
     * This returns the results for the Sparse matrix-multiply benchmark.
     *
     * @return the result for the Sparse matrix-multiply benchmark
     */
    public double getSparseMatmult() {
        return sparseMatmult;
    }

    /**
     * This sets the results for the Sparse matrix-multiply benchmark.
     *
     * @param sparseMatmult the result for the Sparse matrix-multiply benchmark
     */
    public void setSparseMatmult(double sparseMatmult) {
        this.sparseMatmult = sparseMatmult;
    }

    /**
     * This returns the results for the LU matrix factorisation benchmark.
     *
     * @return the result for the LU matrix factorisation benchmark
     */
    public double getLu() {
        return lu;
    }

    /**
     * This gets the results for the LU matrix factorisation benchmark.
     *
     * @param lu the result for the LU matrix factorisation benchmark
     */
    public void setLu(double lu) {
        this.lu = lu;
    }

    /**
     * This prints the results out to standard out, in the same way as the
     * original SciMark 2.0 was developed by Roldan Pozo, and Bruce Miller at
     * the National Institute of Standards and Technology (NIST).
     */
    public void print() {
        System.out.println();
        System.out.println("SciMark 2.0a");
        System.out.println();
        System.out.println("Composite Score: " + getCompositeScore());
        System.out.print("FFT (" + fftSize + "): ");
        if (fft == 0.0) {
            System.out.println(" ERROR, INVALID NUMERICAL RESULT!");
        } else {
            System.out.println(fft);
        }

        System.out.println("SOR (" + sorSize + "x" + sorSize + "): "
                + "  " + sor);
        System.out.println("Monte Carlo : " + monteCarlo);
        System.out.println("Sparse matmult (N=" + sparseSizeM
                + ", nz=" + SparseSizeNz + "): " + sparseMatmult);
        System.out.print("LU (" + luSize + "x" + luSize + "): ");
        if (lu == 0.0) {
            System.out.println(" ERROR, INVALID NUMERICAL RESULT!");
        } else {
            System.out.println(lu);
        }

        // print out System info
        System.out.println();
        System.out.println("java.vendor: " + System.getProperty("java.vendor"));
        System.out.println("java.version: " + System.getProperty("java.version"));
        System.out.println("os.arch: " + System.getProperty("os.arch"));
        System.out.println("os.name: " + System.getProperty("os.name"));
        System.out.println("os.version: " + System.getProperty("os.version"));
    }

    /**
     * This returns the composite score of all of the benchmark results.
     *
     * @return The average score of all of the results obtained.
     */
    public double getCompositeScore() {
        return (fft + sor + monteCarlo + sparseMatmult + lu) / 5;
    }

}
