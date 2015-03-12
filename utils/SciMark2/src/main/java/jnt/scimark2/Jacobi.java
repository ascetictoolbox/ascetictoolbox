/**
 * SciMark 2.0 was developed by Roldan Pozo, and Bruce Miller at the 
 * National Institute of Standards and Technology (NIST). 
 * Its code is copyright free and is in the public domain.
 * 
 * http://math.nist.gov/scimark2/index.html
 */
package jnt.scimark2;

public class Jacobi {

    /**
     * 
     * @param M
     * @param N
     * @param numIterations
     * @return 
     */
    public static final double num_flops(int M, int N, int numIterations) {
        double Md = (double) M;
        double Nd = (double) N;
        double numIterD = (double) numIterations;

        return (Md - 1) * (Nd - 1) * numIterD * 6.0;
    }

    /**
     * 
     * @param omega
     * @param G
     * @param numIterations 
     */
    public static final void SOR(double omega, double G[][], int numIterations) {
        int M = G.length;
        int N = G[0].length;

        double omegaOverFour = omega * 0.25;
        double oneMinusOmega = 1.0 - omega;

		// update interior points
        //
        int Mm1 = M - 1;
        int Nm1 = N - 1;
        for (int p = 0; p < numIterations; p++) {
            for (int i = 1; i < Mm1; i++) {
                double[] Gi = G[i];
                double[] Gim1 = G[i - 1];
                double[] Gip1 = G[i + 1];
                for (int j = 1; j < Nm1; j++) {
                    Gi[j] = omegaOverFour * (Gim1[j] + Gip1[j] + Gi[j - 1]
                            + Gi[j + 1]) + oneMinusOmega * Gi[j];
                }
            }
        }
    }
}
