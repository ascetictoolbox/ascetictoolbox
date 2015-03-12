/**
 * SciMark 2.0 was developed by Roldan Pozo, and Bruce Miller at the 
 * National Institute of Standards and Technology (NIST). 
 * Its code is copyright free and is in the public domain.
 * 
 * http://math.nist.gov/scimark2/index.html
 */
package jnt.scimark2;

/**
 *
 * Provides a stopwatch to measure elapsed time.
 * Example of use:

 * Stopwatch Q = new Stopwatch;
 * Q.start();
 * // code to be timed here ...
 * Q.stop();
 * System.out.println("elapsed time was: " + Q.read() + " seconds.");
 * @author Roldan Pozo
 * @version 14 October 1997, revised 1999-04-24
 */
public class Stopwatch {

    private boolean running;
    private double last_time;
    private double total;

    /**
     * Creates a new Stopwatch
     */
    public Stopwatch() {
        reset();
    }    
    
    /**
     * Return system time (in seconds)
     *
     * @return The system time in seconds
     */
    public final static double seconds() {
        return (System.currentTimeMillis() * 0.001);
    }

    /**
     * Resets the elapsed time.
     */
    public void reset() {
        running = false;
        last_time = 0.0;
        total = 0.0;
    }

    /**
     * Start (and resets) timer
     *
     */
    public void start() {
        if (!running) {
            running = true;
            total = 0.0;
            last_time = seconds();
        }
    }

    /**
     * Resume timing, after stopping. (Does not wipe out accumulated times.)
     */
    public void resume() {
        if (!running) {
            last_time = seconds();
            running = true;
        }
    }

    /**
     * Stops the timer.
     *
     * @return the elapsed time (in seconds)
     */
    public double stop() {
        if (running) {
            total += seconds() - last_time;
            running = false;
        }
        return total;
    }

    /**
     * Provides a reading of the elapsed time.
     *
     * @return the elapsed time (in seconds)
     */
    public double read() {
        if (running) {
            total += seconds() - last_time;
            last_time = seconds();
        }
        return total;
    }

}
