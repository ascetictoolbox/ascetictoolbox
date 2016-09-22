/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.ascetic.paas.self.adaptation.manager.utils;


//--------------------------------------
// Systematically generate combinations.
//--------------------------------------

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CombinationGenerator {

  private int[] a;
  private int n;
  private int r;
  private BigInteger numLeft;
  private BigInteger total;

  //------------
  // Constructor
  //------------

  public CombinationGenerator (int n, int r) {
    if (r > n) {
      throw new IllegalArgumentException ();
    }
    if (n < 1) {
      throw new IllegalArgumentException ();
    }
    this.n = n;
    this.r = r;
    a = new int[r];
    BigInteger nFact = getFactorial (n);
    BigInteger rFact = getFactorial (r);
    BigInteger nminusrFact = getFactorial (n - r);
    total = nFact.divide (rFact.multiply (nminusrFact));
    reset ();
  }

  //------
  // Reset
  //------

  public void reset () {
    for (int i = 0; i < a.length; i++) {
      a[i] = i;
    }
    numLeft = new BigInteger (total.toString ());
  }

  //------------------------------------------------
  // Return number of combinations not yet generated
  //------------------------------------------------

  public BigInteger getNumLeft () {
    return numLeft;
  }

  //-----------------------------
  // Are there more combinations?
  //-----------------------------

  public boolean hasMore () {
    return numLeft.compareTo (BigInteger.ZERO) == 1;
  }

  //------------------------------------
  // Return total number of combinations
  //------------------------------------

  public BigInteger getTotal () {
    return total;
  }

  //------------------
  // Compute factorial
  //------------------

  private static BigInteger getFactorial (int n) {
    BigInteger fact = BigInteger.ONE;
    for (int i = n; i > 1; i--) {
      fact = fact.multiply (new BigInteger (Integer.toString (i)));
    }
    return fact;
  }

  //--------------------------------------------------------
  // Generate next combination (algorithm from Rosen p. 286)
  //--------------------------------------------------------

  public int[] getNext () {

    if (numLeft.equals (total)) {
      numLeft = numLeft.subtract (BigInteger.ONE);
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

    numLeft = numLeft.subtract (BigInteger.ONE);
    return a;

  }

  public static List<String> getCombinations(String input)
  {
    int k = 0;
    String[] elements = input.split("\\,");
    List<String> combinations = new ArrayList<String>();

    StringBuilder combination = new StringBuilder ();
    for(int j=1; j <= elements.length; j++){
        int[] indices;
        CombinationGenerator x = new CombinationGenerator (elements.length, j);
        while (x.hasMore ()) {
          combination.delete(0, combination.length());
          indices = x.getNext ();
          for (int i = 0; i < indices.length; i++) {
            if(i > 0){
                combination.append (",");
            }
            combination.append (elements[indices[i]]);
          }
          combinations.add(combination.toString());
          //System.out.println ("(" + k + ") " + combination.toString ());
          k++;
        }
    }

    return (combinations);
  }

  public static void main(String... args)
  {
    String all_filters = "type:Obres Teatre,moment:Aquest Divendres,moment:Aquesta Nit," + 
      "moment:Aquest Dissabte,moment:Aquest Diumenge,moment:Aquest Dimarts,moment:Aquest Dimecres," + 
      "moment:Aquest Dijous,location:Teatre Romea,city:Barcelona,district:Ciutat Vella," + 
      "day:Diumenge 29 Abril 2012,day:Dimarts 1 Maig 2012,day:Dimecres 2 Maig 2012,ymonth:Abril 2012," + 
      "ymonth:Maig 2012,ymonth:Juny 2012,lang:ca";
    List<String> combinations = CombinationGenerator.getCombinations(all_filters);
    System.out.println( "combinations:" );
    int i = 0;
    for(String comb : combinations){
      System.out.println( comb.toString() );
      i++;
    }
    System.out.println( "Generated a total of " + i + " combinations." );
  }
}
