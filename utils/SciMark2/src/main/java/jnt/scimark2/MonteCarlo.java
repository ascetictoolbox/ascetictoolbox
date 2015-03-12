/**
 * SciMark 2.0 was developed by Roldan Pozo, and Bruce Miller at the National
 * Institute of Standards and Technology (NIST). Its code is copyright free and
 * is in the public domain.
 *
 * http://math.nist.gov/scimark2/index.html
 */
package jnt.scimark2;

/**
 * Estimate Pi by approximating the area of a circle.
 *
 * How: generate N random numbers in the unit square, (0,0) to (1,1) and see how
 * are within a radius of 1 or less, i.e.
 * <pre>  *
 * sqrt(x^2 + y^2) < r
 *
 * </pre> since the radius is 1.0, we can square both sides and avoid a sqrt()
 * computation:
 * <pre>
 *
 * x^2 + y^2 <= 1.0
 *
 * </pre> this area under the curve is (Pi * r^2)/ 4.0, and the area of the unit
 * of square is 1.0, so Pi can be approximated by
 * <pre>
 * # points with x^2+y^2 < 1
 * Pi =~ 		--------------------------  * 4.0
 * total # points
 *
 * </pre>
 *
 */
public class MonteCarlo {

    final static int SEED = 113;

    /**
     * 
     * @param numSamples
     * @return 
     */
    public static final double num_flops(int numSamples) {
        // 3 flops in x^2+y^2 and 1 flop in random routine
        return ((double) numSamples) * 4.0;
    }

    /**
     * 
     * @param numSamples
     * @return 
     */
    public static final double integrate(int numSamples) {

        Random rndGenerator = new Random(SEED);

        int underCurve = 0;
        for (int count = 0; count < numSamples; count++) {
            double x = rndGenerator.nextDouble();
            double y = rndGenerator.nextDouble();

            if (x * x + y * y <= 1.0) {
                underCurve++;
            }
        }
        return ((double) underCurve / numSamples) * 4.0;
    }

}
