/**
 * Copyright 2016 Barcelona Super Computing Center
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package eu.ascetic.paas.self.adaptation.manager.utils;

/**
 * Systematically generate combinations.
 */
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Raimon
 */
public class CombinationGenerator {

    private int[] a;
    private int n;
    private int r;
    private BigInteger numLeft;
    private BigInteger total;

    /**
     *
     * @param n
     * @param r
     */
    public CombinationGenerator(int n, int r) {
        if (r > n) {
            throw new IllegalArgumentException();
        }
        if (n < 1) {
            throw new IllegalArgumentException();
        }
        this.n = n;
        this.r = r;
        a = new int[r];
        BigInteger nFact = getFactorial(n);
        BigInteger rFact = getFactorial(r);
        BigInteger nminusrFact = getFactorial(n - r);
        total = nFact.divide(rFact.multiply(nminusrFact));
        reset();
    }

    /**
     *
     */
    public void reset() {
        for (int i = 0; i < a.length; i++) {
            a[i] = i;
        }
        numLeft = new BigInteger(total.toString());
    }

    /**
     * Return number of combinations not yet generated
     *
     * @return
     */
    public BigInteger getNumLeft() {
        return numLeft;
    }

    /**
     * Are there more combinations?
     *
     * @return
     */
    public boolean hasMore() {
        return numLeft.compareTo(BigInteger.ZERO) == 1;
    }

    /**
     * Return total number of combinations
     *
     * @return
     */
    public BigInteger getTotal() {
        return total;
    }

    /**
     *
     * @param n
     * @return
     */
    public static BigInteger getFactorial(int n) {
        BigInteger fact = BigInteger.ONE;
        for (int i = n; i > 1; i--) {
            fact = fact.multiply(new BigInteger(Integer.toString(i)));
        }
        return fact;
    }

    /**
     * Generate next combination (algorithm from Rosen p. 286)
     *
     * @return
     */
    public int[] getNext() {

        if (numLeft.equals(total)) {
            numLeft = numLeft.subtract(BigInteger.ONE);
            return a;
        }

        int i = r - 1;
        while (a[i] == n - r + i) {
            i--;
        }
        a[i] = a[i] + 1;
        for (int j = i + 1; j < r; j++) {
            a[j] = a[i] + j - i;
        }

        numLeft = numLeft.subtract(BigInteger.ONE);
        return a;

    }

    /**
     *
     * @param input
     * @return
     */
    public static List<String> getCombinations(String input) {
        int k = 0;
        String[] elements = input.split("\\,");
        List<String> combinations = new ArrayList<>();

        StringBuilder combination = new StringBuilder();
        for (int j = 1; j <= elements.length; j++) {
            int[] indices;
            CombinationGenerator x = new CombinationGenerator(elements.length, j);
            while (x.hasMore()) {
                combination.delete(0, combination.length());
                indices = x.getNext();
                for (int i = 0; i < indices.length; i++) {
                    if (i > 0) {
                        combination.append(",");
                    }
                    combination.append(elements[indices[i]]);
                }
                combinations.add(combination.toString());
                //System.out.println ("(" + k + ") " + combination.toString ());
                k++;
            }
        }

        return (combinations);
    }
}
