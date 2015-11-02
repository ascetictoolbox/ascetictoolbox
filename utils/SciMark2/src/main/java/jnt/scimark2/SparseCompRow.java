/**
 * SciMark 2.0 was developed by Roldan Pozo, and Bruce Miller at the National
 * Institute of Standards and Technology (NIST). Its code is copyright free and
 * is in the public domain.
 *
 * http://math.nist.gov/scimark2/index.html
 */
package jnt.scimark2;

public class SparseCompRow {

    /**
     * multiple iterations used to make kernel have roughly same granularity as
     * other Scimark kernels.
     *
     * @param N
     * @param nz
     * @param num_iterations
     * @return
     */
    public static double num_flops(int N, int nz, int num_iterations) {
        /* Note that if nz does not divide N evenly, then the
         actual number of nonzeros used is adjusted slightly.
         */
        int actual_nz = (nz / N) * N;
        return ((double) actual_nz) * 2.0 * ((double) num_iterations);
    }

    /**
     * computes a matrix-vector multiply with a sparse matrix held in
     * compress-row format. If the size of the matrix in MxN with nz nonzeros,
     * then the val[] is the nz nonzeros, with its ith entry in column col[i].
     * The integer vector row[] is of size M+1 and row[i] points to the
     * beginning of the ith row in col[].
     *
     * @param y
     * @param val
     * @param row
     * @param col
     * @param x
     * @param NUM_ITERATIONS
     */
    public static void matmult(double y[], double val[], int row[],
            int col[], double x[], int NUM_ITERATIONS) {
        int M = row.length - 1;

        for (int reps = 0; reps < NUM_ITERATIONS; reps++) {

            for (int r = 0; r < M; r++) {
                double sum = 0.0;
                int rowR = row[r];
                int rowRp1 = row[r + 1];
                for (int i = rowR; i < rowRp1; i++) {
                    sum += x[col[i]] * val[i];
                }
                y[r] = sum;
            }
        }
    }

}
