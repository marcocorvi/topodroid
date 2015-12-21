/* @file MagElement.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid World Magnetic Model 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
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
package com.topodroid.DistoX;


// MAGtype_GeoMagneticElements;
class MagElement
{
  double Decl; /* 1. Angle between the magnetic field vector and true north, positive east*/
  double Incl; /*2. Angle between the magnetic field vector and the horizontal plane, positive down*/
  double F; /*3. Magnetic Field Strength*/
  double H; /*4. Horizontal Magnetic Field Strength*/
  double X; /*5. Northern component of the magnetic field vector*/
  double Y; /*6. Eastern component of the magnetic field vector*/
  double Z; /*7. Downward component of the magnetic field vector*/
  double GV; /*8. The Grid Variation*/
  double Decldot; /*9. Yearly Rate of change in declination*/
  double Incldot; /*10. Yearly Rate of change in inclination*/
  double Fdot; /*11. Yearly rate of change in Magnetic field strength*/
  double Hdot; /*12. Yearly rate of change in horizontal field strength*/
  double Xdot; /*13. Yearly rate of change in the northern component*/
  double Ydot; /*14. Yearly rate of change in the eastern component*/
  double Zdot; /*15. Yearly rate of change in the downward component*/
  double GVdot; /*16. Yearly rate of change in grid variation*/

  // void dump()
  // {
  //   System.out.println("Decl " + Decl + "/" + Decldot + " Incl " + Incl + "/" + Incldot 
  //     + " F " + F + "/" + Fdot + " H " + H + "/" + Hdot );
  // }

  MagElement( )
  {
  }

  MagElement( MagVector V )
  {
    Xdot = V.x;
    Ydot = V.y;
    Zdot = V.z;
  }

  // MAG_GeoMagneticElementsAssign
  MagElement( MagElement other)
  {
    X = other.X;
    Y = other.Y;
    Z = other.Z;
    H = other.H;
    F = other.F;
    Decl = other.Decl;
    Incl = other.Incl;
    GV = other.GV;
    Xdot = other.Xdot;
    Ydot = other.Ydot;
    Zdot = other.Zdot;
    Hdot = other.Hdot;
    Fdot = other.Fdot;
    Decldot = other.Decldot;
    Incldot = other.Incldot;
    GVdot = other.GVdot;
  }

  // MAG_GeoMagneticElementsScale
  void scale( double factor )
  {
    /*This function scales all the geomagnetic elements to scale a vector use 
     MAG_MagneticResultsScale*/
    X *= factor;
    Y *= factor;
    Z *= factor;
    H *= factor;
    F *= factor;
    Incl *= factor;
    Decl *= factor;
    GV *= factor;
    Xdot *= factor;
    Ydot *= factor;
    Zdot *= factor;
    Hdot *= factor;
    Fdot *= factor;
    Incldot *= factor;
    Decldot *= factor;
    GVdot *= factor;
  }

  // MAG_GeoMagneticElementsSubtract
  MagElement subtract( MagElement subtrahend )
  {
    /*This algorithm does not result in the difference of F being derived from 
     the Pythagorean theorem.  This function should be used for computing residuals
     or changes in elements.*/
    MagElement difference = new MagElement( this );
    difference.X -= subtrahend.X;
    difference.Y -= subtrahend.Y;
    difference.Z -= subtrahend.Z;
    difference.H -= subtrahend.H;
    difference.F -= subtrahend.F;
    difference.Decl -= subtrahend.Decl;
    difference.Incl -= subtrahend.Incl;
    difference.Xdot -= subtrahend.Xdot;
    difference.Ydot -= subtrahend.Ydot;
    difference.Zdot -= subtrahend.Zdot;
    difference.Hdot -= subtrahend.Hdot;
    difference.Fdot -= subtrahend.Fdot;
    difference.Decldot -= subtrahend.Decldot;
    difference.Incldot -= subtrahend.Incldot;
    difference.GV -= subtrahend.GV;
    difference.GVdot -= subtrahend.GVdot;
    return difference;
  }

}
