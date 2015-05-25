package utils;

import java.util.Random;

public class Randomizer {

    // Supress default constructor for non-instantiability
    private Randomizer() {
        throw new AssertionError();
    }

    private static final Random r = new Random();

    /**
     * Returns a random number between the min (inclusive) and max (inclusive) values specified.
     *
     * @param min min
     * @param max max
     * @return random number between min and max (both inclusive)
     */
    public static int generate(int min, int max) {
        return r.nextInt(max - min) + min;
    }

}
