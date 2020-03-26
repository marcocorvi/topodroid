/* @file MagModel.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid World Magnetic Model 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * Implemented after GeomagneticLibrary.c by
 *  National Geophysical Data Center
 *  NOAA EGC/2
 *  325 Broadway
 *  Boulder, CO 80303 USA
 *  Attn: Susan McLean
 *  Phone:  (303) 497-6478
 *  Email:  Susan.McLean@noaa.gov
 */
package com.topodroid.mag;

// MAGtype_MagneticModel;
class  MagModel
{
  private String ModelName;
  private int nTerms;
  private double EditionDate;
  double epoch; /*Base time of Geomagnetic model epoch (yrs)*/
  double CoefficientFileEndDate; 
  int nMax; /* Maximum degree of spherical harmonic model */
  int nMaxSecVar; /* Maximum degree of spherical harmonic secular model */
  boolean SecularVariationUsed; /* Whether or not the magnetic secular variation vector will be needed by program*/

  double[] Main_Field_Coeff_G; /* C - Gauss coefficients of main geomagnetic model (nT) Index is (n * (n + 1) / 2 + m) */
  double[] Main_Field_Coeff_H; /* C - Gauss coefficients of main geomagnetic model (nT) */
  double[] Secular_Var_Coeff_G; /* CD - Gauss coefficients of secular geomagnetic model (nT/yr) */
  double[] Secular_Var_Coeff_H; /* CD - Gauss coefficients of secular geomagnetic model (nT/yr) */

  void setCoeffs( WMMcoeff[] data )
  {
	int len = data.length;
    for ( int k = 0; k<len; ++k ) {
      WMMcoeff wmm = data[k];
      if ( wmm == null ) continue;
      int index = wmm.index();
      if ( index > nTerms ) {
    	  System.out.println("index > nTerms " + index  + " size " + data.length );
      }
      Main_Field_Coeff_G[ index ]  = wmm.v0;
      Main_Field_Coeff_H[ index ]  = wmm.v1;
      Secular_Var_Coeff_G[ index ] = wmm.v2;
      Secular_Var_Coeff_H[ index ] = wmm.v3;
    }
  }
    
  MagModel( int nt, int nm, int nmsv )
  {
    nTerms = nt;
    nMax   = nm;
    nMaxSecVar = nmsv;

    ModelName = "";
    EditionDate = 0;
    epoch = 0;
    CoefficientFileEndDate = 0;
    SecularVariationUsed = false;

    Main_Field_Coeff_G = new double[ nTerms + 1 ];
    Main_Field_Coeff_H = new double[ nTerms + 1 ];
    Secular_Var_Coeff_G = new double[ nTerms + 1 ];
    Secular_Var_Coeff_H = new double[ nTerms + 1 ];
    for ( int i=0; i<= nTerms; ++ i ) {
      Main_Field_Coeff_G[i] = 0;
      Main_Field_Coeff_H[i] = 0;
      Secular_Var_Coeff_G[i] = 0;
      Secular_Var_Coeff_H[i] = 0;
    }
  }
  /* This function assigns the first nMax degrees of the Source model to the Assignee model,
   * leaving the other coefficients untouched*/
  void assignCoeffs( MagModel Source, int nMax, int nMaxSecVar)
  {
    // assert(nMax <= Source.nMax);
    // assert(nMax <= Assignee.nMax);
    // assert(nMaxSecVar <= Source.nMaxSecVar);
    // assert(nMaxSecVar <= Assignee.nMaxSecVar);
    for ( int n = 1; n <= nMaxSecVar; n++) {
      for ( int m = 0; m <= n; m++) {
        int index = WMMcoeff.index( n, m );
        Main_Field_Coeff_G[index]  = Source.Main_Field_Coeff_G[index];
        Main_Field_Coeff_H[index]  = Source.Main_Field_Coeff_H[index];
        Secular_Var_Coeff_G[index] = Source.Secular_Var_Coeff_G[index];
        Secular_Var_Coeff_H[index] = Source.Secular_Var_Coeff_H[index];
      }
    }
    for ( int n = nMaxSecVar + 1; n <= nMax; n++) {
      for ( int m = 0; m <= n; m++) {
        int index = WMMcoeff.index( n, m );
        Main_Field_Coeff_G[index] = Source.Main_Field_Coeff_G[index];
        Main_Field_Coeff_H[index] = Source.Main_Field_Coeff_H[index];
      }
    }
  } /*MAG_AssignMagneticModelCoeffs*/

  // void setHeaderValues( String[] values )
  // {
  //   ModelName = values[MODELNAME];
  //   try {
  //     epoch      = Double.parseDouble( values[MODELSTARTYEAR] );
  //     nMax       = Integer.parseInt( values[INTSTATICDEG] );
  //     nMaxSecVar = Integer.parseInt( values[INTSECVARDEG] );
  //     CoefficientFileEndDate = Double.parseDouble( values[MODELENDYEAR] );
  //     SecularVariationUsed = ( nMaxSecVar > 0 );
  //   } catch ( NumberFormatException e ) { }
  // }

  /* Time change the Model coefficients from the base year of the model using secular variation coefficients.
    Store the coefficients of the static model with their values advanced from epoch t0 to epoch t.
    Copy the SV coefficients.  If input "t���������" is the same as "t0", then this is merely a copy operation.
    If the address of "model" is the same as the address of "model", then this procedure overwrites
    the given item "model".
  INPUT: date
         model
   */
  MagModel getTimelyModifyModel( MagDate date )
  {
    float dy = (float)(date.DecimalYear - epoch);
    MagModel ret = new MagModel( nTerms, nMax, nMaxSecVar );
    ret.ModelName   = ModelName;
    ret.EditionDate = EditionDate;
    ret.epoch       = epoch;
    int a = ret.nMaxSecVar;
    int b = WMMcoeff.index(a,a); // (a * (a + 1)) / 2 + a;
    for ( int n = 1; n <= nMax; n++) {
      for ( int m = 0; m <= n; m++) {
        int index = WMMcoeff.index(n,m); // (n * (n + 1)) / 2 + m;
        if (index <= b) {
          ret.Main_Field_Coeff_H[index] = Main_Field_Coeff_H[index] + dy * Secular_Var_Coeff_H[index];
          ret.Main_Field_Coeff_G[index] = Main_Field_Coeff_G[index] + dy * Secular_Var_Coeff_G[index];
          ret.Secular_Var_Coeff_H[index] = Secular_Var_Coeff_H[index]; /* need a copy of the sec var to calculate secular change */
          ret.Secular_Var_Coeff_G[index] = Secular_Var_Coeff_G[index];
        } else {
          ret.Main_Field_Coeff_H[index] = Main_Field_Coeff_H[index];
          ret.Main_Field_Coeff_G[index] = Main_Field_Coeff_G[index];
        }
      }
    }
    return ret;
  } /* MAG_TimelyModifyMagneticModel */
}
