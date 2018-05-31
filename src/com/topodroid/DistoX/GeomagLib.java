/* @file GeomagLib.java
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
package com.topodroid.DistoX;

class GeomagLib
{
  /* The main subroutine that calls a sequence of WMM sub-functions to calculate the magnetic
   * field elements for a single point.  The function expects the model coefficients and point
   * coordinates as input and returns the magnetic field elements and their rate of change.
   * Though, this subroutine can be called successively to calculate a time series, profile or grid
   * of magnetic field, these are better achieved by the subroutine MAG_Grid.
   */
  MagElement MAG_Geomag(  MagEllipsoid ellip,
                          MagSpherical spherical,
                          MagGeodetic geodetic,
                          MagModel model )
  {
    int nmax = model.nMax;
    // int NumTerms = ((nmax + 1) * (nmax + 2)) / 2; 
    MagHarmonic sph_vars  = sphericalHarmonicVariables( ellip, spherical, nmax ); // Spherical Harmonic vars
    MagLegendre legendre  = associatedLegendreFunction( spherical, nmax );        // Compute ALF  
    MagVector sumSphCoeff = summation( legendre, model, sph_vars, spherical );    // Accumulate sph. harm. coeffs 
    MagVector sumSecVarCoeff = sumSecVar( legendre, model, sph_vars, spherical ); //Sum Sec. Var. Coeffs 
    MagVector result    = rotateVector( spherical, geodetic, sumSphCoeff);     // Computed Magn. fld to Geodeitic coords
    MagVector resultSV  = rotateVector( spherical, geodetic, sumSecVarCoeff);  // sec. var. fld comps to Geodetic coords
    MagElement elems   = calculateGeoMagneticElements( result );             // Geomagn. elems Eq. 19 , WMM Tech rep
    calculateSecularVariationElements( resultSV, elems );                     // sec var of each of the Geomagn elems
    return elems;
  }

  void MAG_Gradient( MagEllipsoid ellip,
                     MagGeodetic geodetic,
                     MagModel model,
                     MagGradient Gradient )
  {
    // It should be noted that the x[2], y[2], and z[2] variables are NOT the same
    //  coordinate system as the directions in which the gradients are taken.  These
    //  variables represent a Cartesian coordinate system where the Earth's center is
    //  the origin, 'z' points up toward the North (rotational) pole and 'x' points toward
    //  the prime meridian.  'y' points toward longitude = 90 degrees East.  
    //  The gradient is preformed along a local Cartesian coordinate system with the
    //  origin at geodetic.  'z' points down toward the Earth's core, x points 
    //  North, tangent to the local longitude line, and 'y' points East, tangent to
    //  the local latitude line.
  
    double phiDelta = 0.01;
    // double DeltaY = 0.01;
    double hDelta = -1;
  
    //Initialization
    MagSpherical spherical = ellip.geodeticToSpherical( geodetic );
    MagElement elems = MAG_Geomag( ellip, spherical, geodetic, model );
    MagGeodetic adjGeodetic = new MagGeodetic( geodetic );
  
    // Gradient along x
    adjGeodetic.phi = geodetic.phi + phiDelta;
  
    spherical = ellip.geodeticToSpherical( adjGeodetic );
    MagElement AdjGeoMagneticElements0 = MAG_Geomag( ellip, spherical, adjGeodetic, model );
  
    MagVector X0 = spherical.toCartesian( );
    adjGeodetic.phi = geodetic.phi - phiDelta;
    spherical = ellip.geodeticToSpherical( adjGeodetic );
    MagElement AdjGeoMagneticElements1 = MAG_Geomag(ellip, spherical, adjGeodetic, model );
    MagVector X1 = spherical.toCartesian( );
  
    double distance = X0.distance(X1);
    Gradient.GradPhi = AdjGeoMagneticElements0.subtract( AdjGeoMagneticElements1 );
    Gradient.GradPhi.scale( 1 / distance);
    adjGeodetic = new MagGeodetic( geodetic );
  
    // Gradient along y
    // It is perhaps noticeable that the method here for calculation is substantially
    // different than that for the gradient along x.  As we near the North pole
    // the longitude lines approach each other, and the calculation that works well
    // for latitude lines becomes unstable when 0.01 degrees represents sufficiently
    // small numbers, and fails to function correctly at all at the North Pole
    spherical = ellip.geodeticToSpherical( geodetic );
    Gradient.GradLambda = MAG_GradY(ellip, spherical, geodetic, model, elems );
    
    // Gradient along z
    adjGeodetic.HeightAboveEllipsoid = geodetic.HeightAboveEllipsoid + hDelta;
    adjGeodetic.HeightAboveGeoid     = geodetic.HeightAboveGeoid + hDelta;
    spherical = ellip.geodeticToSpherical( adjGeodetic );
  
    AdjGeoMagneticElements0 = MAG_Geomag( ellip, spherical, adjGeodetic, model );
    X0 = spherical.toCartesian( );
    adjGeodetic.HeightAboveEllipsoid = geodetic.HeightAboveEllipsoid - hDelta;
    adjGeodetic.HeightAboveGeoid = geodetic.HeightAboveGeoid - hDelta;
    spherical = ellip.geodeticToSpherical( adjGeodetic );
    AdjGeoMagneticElements1 = MAG_Geomag( ellip, spherical, adjGeodetic, model );
    X1 = spherical.toCartesian( );
  
    distance = X0.distance( X1 );
    Gradient.GradZ = AdjGeoMagneticElements0.subtract( AdjGeoMagneticElements1 );
    Gradient.GradZ.scale( 1/distance );
    // adjGeodetic = new MagGeodetic(geodetic);
  }

  MagErrors MAG_BaseErrors(double DeclCoef, double DeclBaseline, double InclOffset, double FOffset,
                          double Multiplier, double H )
  {
    double declHorizontalAdjustmentSq;
    declHorizontalAdjustmentSq = (DeclCoef/H) * (DeclCoef/H);
    return new MagErrors( 
      Math.sqrt(declHorizontalAdjustmentSq + DeclBaseline*DeclBaseline) * Multiplier,
      InclOffset*Multiplier,
      FOffset*Multiplier );
  }

  private MagElement calculateGeoMagneticElements(MagVector result )
  {
    MagElement elems = new MagElement( );
    elems.X = result.x;
    elems.Y = result.y;
    elems.Z = result.z;
    elems.H = Math.sqrt(result.x * result.x + result.y * result.y); // horiz magn. fld strength
    elems.F = Math.sqrt(elems.H * elems.H + result.z * result.z); // magn. fld strength
    elems.Decl = TDMath.RAD2DEG*( Math.atan2(elems.Y, elems.X) ); // angle Magn. Fld and North (pos. east)
    elems.Incl = TDMath.RAD2DEG*( Math.atan2(elems.Z, elems.H) ); // angle Magn. Fld and horiz plan (pos. down)
    return elems;
  } /*MAG_CalculateGeoMagneticElements */

  /** Computes the grid variation for |latitudes| > MAG_MAX_LAT_DEGREE
   * Grivation (or grid variation) is the angle between grid north and
   * magnetic north. This routine calculates Grivation for the Polar Stereographic
   * projection for polar locations (Latitude => |55| deg). Otherwise, it computes the grid
   * variation in UTM projection system. However, the UTM projection codes may be used to compute
   * the grid variation at any latitudes.
   */
  void calculateGridVariation( MagGeodetic location, MagElement elements )
  {
    if(location.phi >= MagUtil.MAG_PS_MAX_LAT_DEGREE) {
      elements.GV = elements.Decl - location.lambda;
    } else if (location.phi <= MagUtil.MAG_PS_MIN_LAT_DEGREE) {
      elements.GV = elements.Decl + location.lambda;
    } else {
      MagUTMParams UTMParameters = MAG_GetTransverseMercator(location );
      if ( UTMParameters != null ) {
        elements.GV = elements.Decl - UTMParameters.ConvergenceOfMeridians;
      } else {
        TDLog.Error("Null UTM params");
        elements.GV = elements.Decl;
      }
    }
  } 

  private MagElement calculateGradientElements( MagVector grad, MagElement elems )
  {
    MagElement ret = new MagElement( elems ); // FIXME copy elems
    ret.X = grad.x;
    ret.Y = grad.y;
    ret.Z = grad.z;
    ret.H = (ret.X * elems.X + ret.Y * elems.Y) / elems.H;
    ret.F = (ret.X * elems.X + ret.Y * elems.Y + ret.Z * elems.Z) / elems.F;
    ret.Decl = TDMath.RAD2DEG * (elems.X * ret.Y - elems.Y * ret.X) / (elems.H * elems.H);
    ret.Incl = TDMath.RAD2DEG * (elems.H * ret.Z - elems.Z * ret.H) / (elems.F * elems.F);
    ret.GV = ret.Decl;
    return ret;
  }

  /** This takes the Magnetic Variation in x, y, and z and uses it to calculate the secular variation
   *  of each of the Geomagnetic elements.
   */
  private void calculateSecularVariationElements( MagVector var, MagElement elems )
  {
    elems.Xdot = var.x;
    elems.Ydot = var.y;
    elems.Zdot = var.z;
    elems.Hdot = (elems.X * elems.Xdot + elems.Y * elems.Ydot) / elems.H; /* See equation 19 in the WMM technical report */
    elems.Fdot = (elems.X * elems.Xdot + elems.Y * elems.Ydot + elems.Z * elems.Zdot) / elems.F;
    elems.Decldot = TDMath.RAD2DEG * (elems.X * elems.Ydot - elems.Y * elems.Xdot) / (elems.H * elems.H);
    elems.Incldot = TDMath.RAD2DEG * (elems.H * elems.Zdot - elems.Z * elems.Hdot) / (elems.F * elems.F);
    elems.GVdot = elems.Decldot;
  } /*MAG_CalculateSecularVariationElements*/

  MagElement MAG_ErrorCalc(MagElement B )
  {
    MagElement errors = new MagElement(); 
    /*errors.Decl, errors.Incl, errors.F are all assumed to exist*/
    double cos2D = Math.cos( TDMath.DEG2RAD*B.Decl) * Math.cos( TDMath.DEG2RAD*B.Decl);
    double cos2I = Math.cos( TDMath.DEG2RAD*B.Incl) * Math.cos( TDMath.DEG2RAD*B.Incl);
    double sin2D = Math.sin( TDMath.DEG2RAD*B.Decl) * Math.sin( TDMath.DEG2RAD*B.Decl);
    double sin2I = Math.sin( TDMath.DEG2RAD*B.Incl) * Math.sin( TDMath.DEG2RAD*B.Incl);
    double eD = TDMath.DEG2RAD * errors.Decl;
    double eI = TDMath.DEG2RAD * errors.Incl;
    double EDSq = eD*eD;
    double EISq = eI*eI;
    errors.X = Math.sqrt(cos2D*cos2I*errors.F*errors.F+B.F*B.F*sin2D*cos2I*EDSq+B.F*B.F*cos2D*sin2I*EISq);
    errors.Y = Math.sqrt(sin2D*cos2I*errors.F*errors.F+B.F*B.F*cos2D*cos2I*EDSq+B.F*B.F*sin2D*sin2I*EISq);
    errors.Z = Math.sqrt(sin2I*errors.F*errors.F+B.F*B.F*cos2I*EISq);
    errors.H = Math.sqrt(cos2I*errors.F*errors.F+B.F*B.F*sin2I*EISq);
    return errors;
  }

  /* Gets the UTM Parameters for a given Latitude and Longitude.
   */
  private MagUTMParams MAG_GetTransverseMercator(MagGeodetic geodetic )
  {
    /*   Get the map projection  parameters */
    double Lambda = TDMath.DEG2RAD * geodetic.lambda;
    double Phi    = TDMath.DEG2RAD * geodetic.phi;

    MagUTMParams utm0 = MAG_GetUTMParameters( Phi, Lambda );
    if ( utm0 == null ) return null; // out of UTM range

    double K0 = 0.9996;
    double falseN = 0;
    double falseE = 500000;
    // if (HemiSphere == 'n' || HemiSphere == 'N') falseN = 0;
    if (utm0.HemiSphere == 's' || utm0.HemiSphere == 'S') falseN = 10000000;

    /* WGS84 ellipsoid */
    double Eps    = 0.081819190842621494335;
    double Epssq  = 0.0066943799901413169961;
    double K0R4   = 6367449.1458234153093;
    double K0R4oa = 0.99832429843125277950;
    double Acoeff[] = {
      8.37731820624469723600E-04,
      7.60852777357248641400E-07,
      1.19764550324249124400E-09,
      2.42917068039708917100E-12,
      5.71181837042801392800E-15,
      1.47999793137966169400E-17,
      4.10762410937071532000E-20,
      1.21078503892257704200E-22
    };

    /*   Execution of the forward T.M. algorithm  */
    boolean XYonly = false;
    return MAG_TMfwd4(Eps, Epssq, K0R4, K0R4oa, Acoeff, utm0, K0, falseE, falseN, XYonly, Lambda, Phi );
  }

  /** The function MAG_GetUTMParameters converts geodetic (latitude and
   * longitude) coordinates to UTM projection parameters (zone, hemisphere and central meridian)
   * If any errors occur, the error code(s) are returned
   * by the function, otherwise TRUE is returned.
   *    Latitude          : Latitude in radians                 (input)
   *    Longitude         : Longitude in radians                (input)
   *    Zone              : UTM zone                            (output)
   *    HemiSphere        : North or South hemisphere           (output)
   *    CentralMeridian	: Central Meridian of the UTM Zone in radians	   (output)
   */
  private MagUTMParams  MAG_GetUTMParameters(double Latitude, double Longitude )
  {
    if ( (Latitude < TDMath.DEG2RAD*MagUtil.MAG_UTM_MIN_LAT_DEGREE)
      || (Latitude > TDMath.DEG2RAD*MagUtil.MAG_UTM_MAX_LAT_DEGREE) ) {
      /* Latitude out of range */
      return null;
    }
    if ((Longitude < - TDMath.M_PI) || (Longitude > (2 * TDMath.M_PI))) {
      /* Longitude out of range */
      return null;
    }

    if (Longitude < 0) Longitude += (2 * TDMath.M_PI) + 1.0e-10;
    long Lat_Degrees  = (long) (Latitude  * TDMath.RAD2DEG );
    long Long_Degrees = (long) (Longitude * TDMath.RAD2DEG );
    long temp_zone = (Longitude < TDMath.M_PI )? (long) (31 + ((Longitude * TDMath.RAD2DEG ) / 6.0))
                                        : (long) (((Longitude * TDMath.RAD2DEG ) / 6.0) - 29);
    if (temp_zone > 60) temp_zone = 1;
    /* UTM special cases */
    if((Lat_Degrees > 55) && (Lat_Degrees < 64) && (Long_Degrees > -1) && (Long_Degrees < 3)) temp_zone = 31;
    if((Lat_Degrees > 55) && (Lat_Degrees < 64) && (Long_Degrees > 2) && (Long_Degrees < 12)) temp_zone = 32;
    if((Lat_Degrees > 71) && (Long_Degrees > -1) && (Long_Degrees < 9)) temp_zone = 31;
    if((Lat_Degrees > 71) && (Long_Degrees > 8) && (Long_Degrees < 21)) temp_zone = 33;
    if((Lat_Degrees > 71) && (Long_Degrees > 20) && (Long_Degrees < 33)) temp_zone = 35;
    if((Lat_Degrees > 71) && (Long_Degrees > 32) && (Long_Degrees < 42)) temp_zone = 37;

    MagUTMParams ret = new MagUTMParams();
    ret.Zone = (int)temp_zone;
    ret.HemiSphere = (Latitude < 0)? 'S' : 'N';
    ret.CentralMeridian = (temp_zone >= 31)? (6 * temp_zone - 183) : (6 * temp_zone + 177);
    return ret;
  } 

  /* Rotate the Magnetic Vectors to Geodetic Coordinates
   * Equation 16, WMM Technical report
   */
  private MagVector rotateVector( MagSpherical spherical,
             MagGeodetic geodetic,
	     MagVector MagneticResultsSph )
  {
    /* Difference between the spherical and Geodetic latitudes */
    double Psi = TDMath.DEG2RAD * (spherical.phig - geodetic.phi);

    /* Rotate spherical field components to the Geodetic system */
    return new MagVector(
      MagneticResultsSph.x * Math.cos(Psi) - MagneticResultsSph.z * Math.sin(Psi),
      MagneticResultsSph.y,
      MagneticResultsSph.x * Math.sin(Psi) + MagneticResultsSph.z * Math.cos(Psi)
    );
  } 

  /**  Transverse Mercator forward equations including point-scale and CoM
   *   Algorithm developed by: C. Rollins   August 7, 2006
   *   C software written by:  K. Robins
   * Constants fixed by choice of ellipsoid and choice of projection parameters
   *     Eps          Eccentricity (epsilon) of the ellipsoid
   *     Epssq        Eccentricity squared
   *   ( R4           Meridional isoperimetric radius   )
   *   ( K0           Central scale factor              )
   *     K0R4         K0 times R4
   *     K0R4oa       K0 times Ratio of R4 over semi-major axis
   *     Acoeff       Trig series coefficients, omega as a function of chi
   *     Lam0         Longitude of the central meridian in radians
   *     K0           Central scale factor, for example, 0.9996 for UTM
   *     falseE       False easting, for example, 500000 for UTM
   *     falseN       False northing
   * Processing option
   *       XYonly     If one (1), then only X and Y will be properly computed.
   *                  Values returned for point-scale and CoM will merely be the
   *     	     trivial values for points on the central meridian
   * Input items that identify the point to be converted
   *       Lambda       Longitude (from Greenwich) in radians
   *       Phi          Latitude in radians
   * Output items
   *       X            X coordinate (Easting) in meters
   *       Y            Y coordinate (Northing) in meters
   *       pscale       point-scale (dimensionless)
   *       CoM          Convergence-of-meridians in radians
   */
  private MagUTMParams MAG_TMfwd4(double Eps, double Epssq, double K0R4, double K0R4oa,
        double Acoeff[], MagUTMParams utm0, double K0, double falseE,
        double falseN, boolean XYonly, double Lambda, double Phi )
  {
    /* Ellipsoid to sphere
       Convert longitude (Greenwhich) to longitude from the central meridian
       It is unnecessary to find the (-Pi, Pi] equivalent of the result.
       Compute its cosine and sine.
     */
    double Lam = Lambda - (utm0.CentralMeridian * TDMath.DEG2RAD);
    double CLam = Math.cos(Lam); // Longitude
    double SLam = Math.sin(Lam);
    double CPhi = Math.cos(Phi); // Latitude 
    double SPhi = Math.sin(Phi);

    /*   Convert geodetic latitude, Phi, to conformal latitude, Chi
         Only the cosine and sine of Chi are actually needed.        */
    double P = Math.exp(Eps * MagUtil.ATanH(Eps * SPhi));
    double part1 = (1 + SPhi) / P;
    double part2 = (1 - SPhi) * P;
    double denom = 1 / (part1 + part2);
    double CChi = 2 * CPhi * denom;
    double SChi = (part1 - part2) * denom;

    /* Sphere to first plane
       Apply spherical theory of transverse Mercator to get (u,v) coordinates
       Note the order of the arguments in Fortran's version of ArcTan, i.e.
                 atan2(y, x) = ATan(y/x)
       The two argument form of ArcTan is needed here.
     */
    double T = CChi * SLam;
    double U = MagUtil.ATanH(T);
    double V = Math.atan2(SChi, CChi * CLam);

    /* Trigonometric multiple angles
       Compute Cosh of even multiples of U
       Compute Sinh of even multiples of U
       Compute Cos  of even multiples of V
       Compute Sin  of even multiples of V
     */
    double Tsq = T * T;
    double denom2 = 1 / (1 - Tsq);
    double c2u = (1 + Tsq) * denom2;
    double s2u = 2 * T * denom2;
    double c2v = (-1 + CChi * CChi * (1 + CLam * CLam)) * denom2;
    double s2v = 2 * CLam * CChi * SChi * denom2;

    double c4u = 1 + 2 * s2u * s2u;
    double s4u = 2 * c2u * s2u;
    double c4v = 1 - 2 * s2v * s2v;
    double s4v = 2 * c2v * s2v;

    double c6u = c4u * c2u + s4u * s2u;
    double s6u = s4u * c2u + c4u * s2u;
    double c6v = c4v * c2v - s4v * s2v;
    double s6v = s4v * c2v + c4v * s2v;

    double c8u = 1 + 2 * s4u * s4u;
    double s8u = 2 * c4u * s4u;
    double c8v = 1 - 2 * s4v * s4v;
    double s8v = 2 * c4v * s4v;

    /*   First plane to second plane
         Accumulate terms for X and Y
     */
    double Xstar = Acoeff[3] * s8u * c8v;
    Xstar = Xstar + Acoeff[2] * s6u * c6v;
    Xstar = Xstar + Acoeff[1] * s4u * c4v;
    Xstar = Xstar + Acoeff[0] * s2u * c2v;
    Xstar = Xstar + U;

    double Ystar = Acoeff[3] * c8u * s8v;
    Ystar = Ystar + Acoeff[2] * c6u * s6v;
    Ystar = Ystar + Acoeff[1] * c4u * s4v;
    Ystar = Ystar + Acoeff[0] * c2u * s2v;
    Ystar = Ystar + V;

    MagUTMParams utm = new MagUTMParams();
    utm.Zone            = utm0.Zone; /*UTM Zone*/
    utm.HemiSphere      = utm0.HemiSphere;
    utm.CentralMeridian = utm0.CentralMeridian; /* Central Meridian of the UTM Zone */

    /*   Apply isoperimetric radius, scale adjustment, and offsets  */
    utm.Easting  = K0R4 * Xstar + falseE; // UTM Easting (X) in meters 
    utm.Northing = K0R4 * Ystar + falseN; // UTM Northing (Y) in meters

    /*  Point-scale and CoM */
    if ( XYonly ) {
      utm.PointScale = K0;
      utm.ConvergenceOfMeridians = 0; /* Convergence of meridians of the UTM Zone and location */
    } else {
      double sig1 = 8 * Acoeff[3] * c8u * c8v;
      sig1 = sig1 + 6 * Acoeff[2] * c6u * c6v;
      sig1 = sig1 + 4 * Acoeff[1] * c4u * c4v;
      sig1 = sig1 + 2 * Acoeff[0] * c2u * c2v;
      sig1 = sig1 + 1;

      double sig2 = 8 * Acoeff[3] * s8u * s8v;
      sig2 = sig2 + 6 * Acoeff[2] * s6u * s6v;
      sig2 = sig2 + 4 * Acoeff[1] * s4u * s4v;
      sig2 = sig2 + 2 * Acoeff[0] * s2u * s2v;

      /*    Combined square roots  */
      double comroo = Math.sqrt((1 - Epssq * SPhi * SPhi) * denom2 * (sig1 * sig1 + sig2 * sig2));
      utm.PointScale = K0R4oa * 2 * denom * comroo;
      utm.ConvergenceOfMeridians = TDMath.RAD2DEG * ( Math.atan2(SChi * SLam, CLam) + Math.atan2(sig2, sig1) );
    }
    return utm;
  }

  /** Computes  all of the Schmidt-semi normalized associated Legendre functions up to degree nMax.
   * If nMax <= 16, function MAG_PcupLow is used.
   * Otherwise MAG_PcupHigh is called.
   * INPUT  spherical 	A data structure with the following elements
                                  double lambda; ( longitude)
                                  double phig;   ( geocentric latitude )
                                  double r;  	 ( distance from the center of the ellipsoid)
             nMax        	integer 	 ( Maxumum degree of spherical harmonic secular model)
   * OUTPUT  legendre data structure with the following elements
                                  double[] Pcup;  ( store Legendre Function  )
                                  double[] dPcup; ( store  Derivative of Lagendre function )
   */
  private MagLegendre associatedLegendreFunction(MagSpherical spherical, int nMax )
  {
    double sin_phi = Math.sin( TDMath.DEG2RAD * spherical.phig ); // sin  (geocentric latitude)

    if (nMax <= 16 || (1 - Math.abs(sin_phi)) < 1.0e-10 ) { // If nMax is less tha 16 or at the poles
      return MAG_PcupLow( sin_phi, nMax );
    } 
    return MAG_PcupHigh( sin_phi, nMax );
  }

  /** Check if the latitude is equal to -90 or 90. If it is,
   * offset it by 1e-5 to avoid division by zero. This is not currently used in the Geomagnetic
   * main function. This may be used to avoid calling specialSummation.
   * The function updates the input data structure.
   */
  void checkGeographicPole( MagGeodetic coord )
  {
    coord.phi = coord.phi < (-90.0 + MagUtil.MAG_GEO_POLE_TOLERANCE) ? (-90.0 + MagUtil.MAG_GEO_POLE_TOLERANCE) 
              : coord.phi;
    coord.phi = coord.phi > (90.0 - MagUtil.MAG_GEO_POLE_TOLERANCE) ? (90.0 - MagUtil.MAG_GEO_POLE_TOLERANCE)
              : coord.phi;
  } 

  /** Computes Spherical variables
   *  Variables computed are (a/r)^(n+2), cos_m(lamda) and sin_m(lambda) for spherical harmonic
   *  summations. (Equations 10-12 in the WMM Technical Report)
   */
  private MagHarmonic sphericalHarmonicVariables( MagEllipsoid ellip, MagSpherical spherical, int nMax )
  {
    MagHarmonic vars = new MagHarmonic( nMax );
    double cos_lambda = Math.cos(TDMath.DEG2RAD * spherical.lambda);
    double sin_lambda = Math.sin(TDMath.DEG2RAD * spherical.lambda);
    /* for n = 0 ... model_order, compute (Radius of Earth / Spherical radius r)^(n+2)
       for n  1..nMax-1 (this is much faster than calling pow MAX_N+1 times).      */
    vars.RelativeRadiusPower[0] = (ellip.re / spherical.r) * (ellip.re / spherical.r);
    for ( int n = 1; n <= nMax; n++) {
      vars.RelativeRadiusPower[n] = vars.RelativeRadiusPower[n - 1] * (ellip.re / spherical.r);
    }
    /* Compute cos(m*lambda), sin(m*lambda) for m = 0 ... nMax
           cos(a + b) = cos(a)*cos(b) - sin(a)*sin(b)
           sin(a + b) = cos(a)*sin(b) + sin(a)*cos(b)
     */
    vars.cos_mlambda[0] = 1.0;
    vars.sin_mlambda[0] = 0.0;
    vars.cos_mlambda[1] = cos_lambda;
    vars.sin_mlambda[1] = sin_lambda;
    for ( int m = 2; m <= nMax; m++) {
      vars.cos_mlambda[m] = vars.cos_mlambda[m - 1] * cos_lambda - vars.sin_mlambda[m - 1] * sin_lambda;
      vars.sin_mlambda[m] = vars.cos_mlambda[m - 1] * sin_lambda + vars.sin_mlambda[m - 1] * cos_lambda;
    }
    return vars;
  }

  private MagElement MAG_GradY( MagEllipsoid ellip,
                  MagSpherical spherical,
                  MagGeodetic geodetic,
                  MagModel model,
                  MagElement elems )
  {
    int nmax = model.nMax;
    // int NumTerms = ((nmax + 1) * (nmax + 2)) / 2; 
    MagHarmonic sph_vars = sphericalHarmonicVariables(ellip, spherical, nmax ); // Spherical Harmonic variables
    MagLegendre legendre = associatedLegendreFunction(spherical, nmax ); // Compute ALF
    MagVector gradYSph = gradYSummation(legendre, model, sph_vars, spherical ); // Accumulate sph. harm. coeffs
    MagVector gradYgeo = rotateVector(spherical, geodetic, gradYSph ); // computed Magn. fld to Geodetic coords
    MagElement gradYelems = calculateGradientElements(gradYgeo, elems); // Geomagn. elems Equation 18 , WMM Tech rep
    return gradYelems;
  }

  private MagVector gradYSummation( MagLegendre legendre,
                            MagModel model,
        		    MagHarmonic sph_vars,
        		    MagSpherical spherical )
  {
    MagVector grad = new MagVector( 0, 0, 0 );
    for ( int n = 1; n <= model.nMax; n++) {
        for ( int m = 0; m <= n; m++) {
            int index = (n * (n + 1) / 2 + m);
            grad.z -= sph_vars.RelativeRadiusPower[n] *
                    (-1 * model.Main_Field_Coeff_G[index] * sph_vars.sin_mlambda[m] +
                    model.Main_Field_Coeff_H[index] * sph_vars.cos_mlambda[m])
                    * (double) (n + 1) * (double) (m) * legendre. Pcup[index] * (1/spherical.r);
            grad.y += sph_vars.RelativeRadiusPower[n] *
                    (model.Main_Field_Coeff_G[index] * sph_vars.cos_mlambda[m] +
                    model.Main_Field_Coeff_H[index] * sph_vars.sin_mlambda[m])
                    * (double) (m * m) * legendre. Pcup[index] * (1/spherical.r);
            grad.x -= sph_vars.RelativeRadiusPower[n] *
                    (-1 * model.Main_Field_Coeff_G[index] * sph_vars.sin_mlambda[m] +
                    model.Main_Field_Coeff_H[index] * sph_vars.cos_mlambda[m])
                    * (double) (m) * legendre. dPcup[index] * (1/spherical.r);
        }
    }
    double cos_phi = Math.cos( TDMath.DEG2RAD * spherical.phig );
    if ( Math.abs(cos_phi) > 1.0e-10 ) {
        grad.y = grad.y / (cos_phi * cos_phi);
        grad.x = grad.x / (cos_phi);
        grad.z = grad.z / (cos_phi);
    } else {
        // Special calculation for component - By - at Geographic poles.
        // If the user wants to avoid using this function,  please make sure that
        // the latitude is not exactly +/-90. An option is to make use the function
        // MAG_CheckGeographicPoles.
        //
       // specialSummation(model, sph_vars, spherical, GradY); 
    }
    return grad;
  }

  /* This function evaluates all of the Schmidt-semi normalized associated Legendre
   * functions up to degree nMax. The functions are initially scaled by
   * 10^280 sin^m in order to minimize the effects of underflow at large m
   * near the poles (see Holmes and Featherstone 2002, J. Geodesy, 76, 279-299).
   * Note that this function performs the same operation as MAG_PcupLow.
   * However this function also can be used for high degree (large nMax) models.
   *
   * Notes: Adopted from the FORTRAN code written by Mark Wieczorek September 25, 2005.
   * Manoj Nair, Nov, 2009 Manoj.C.Nair@Noaa.Gov
   *
   * Change from the previous version
   * The prevous version computes the derivatives as
   * dP(n,m)(x)/dx, where x = sin(latitude) (or cos(colatitude) ).
   * However, the WMM Geomagnetic routines requires dP(n,m)(x)/dlatitude.
   * Hence the derivatives are multiplied by sin(latitude).
   * Removed the options for CS phase and normalizations.
   *
   * Note: In geomagnetism, the derivatives of ALF are usually found with respect to the colatitudes.
   * Here the derivatives are found with respect * to the latitude. The difference is a sign reversal
   * for the derivative of the Associated Legendre Functions.
   *
   * The derivatives can't be computed for latitude = |90| degrees.
   */
  private MagLegendre MAG_PcupHigh( double x, int nMax)
  {
    int NumTerms = ((nMax + 1) * (nMax + 2) / 2);
    if ( Math.abs(x) == 1.0 ) {
      // printf("Error in PcupHigh: derivative cannot be calculated at poles\n");
      return null;
    }
    MagLegendre legendre = new MagLegendre( NumTerms );

    double[] f1 = new double[ NumTerms + 1 ];
    double[] PreSqr = new double[ NumTerms + 1 ];
    double[] f2 = new double[ NumTerms + 1 ];
    double scalef = 1.0e-280;

    for ( int n = 0; n <= 2 * nMax + 1; ++n) PreSqr[n] = Math.sqrt((double) (n));

    int k = 2;
    for ( int n = 2; n <= nMax; n++) {
      k = k + 1;
      f1[k] = (double) (2 * n - 1) / (double) (n);
      f2[k] = (double) (n - 1) / (double) (n);
      for ( int m = 1; m <= n - 2; m++) {
        k = k + 1;
        f1[k] = (double) (2 * n - 1) / PreSqr[n + m] / PreSqr[n - m];
        f2[k] = PreSqr[n - m - 1] * PreSqr[n + m - 1] / PreSqr[n + m] / PreSqr[n - m];
      }
      k = k + 2;
    }

    // z = sin (geocentric latitude) 
    double z = Math.sqrt((1.0 - x)*(1.0 + x));
    double pm2 = 1.0;

    legendre.Pcup[0]  = 1.0;
    legendre.dPcup[0] = 0.0;
    if (nMax == 0 ) return legendre;

    double pm1 = x;
    legendre.Pcup[1]  = pm1;
    legendre.dPcup[1] = z;
    k = 1;

    for ( int n = 2; n <= nMax; n++) {
      k = k + n;
      double plm = f1[k] * x * pm1 - f2[k] * pm2;
      legendre.Pcup[k] = plm;
      legendre.dPcup[k] = (double) (n) * (pm1 - x * plm) / z;
      pm2 = pm1;
      pm1 = plm;
    }

    double pmm = PreSqr[2] * scalef;
    double rescalem = 1.0 / scalef;
    int kstart = 0;

    for ( int m = 1; m <= nMax - 1; ++m) {
      rescalem = rescalem * z;
      // Calculate Pcup(m,m)
      kstart = kstart + m + 1;
      pmm = pmm * PreSqr[2 * m + 1] / PreSqr[2 * m];
      legendre.Pcup[kstart] = pmm * rescalem / PreSqr[2 * m + 1];
      legendre.dPcup[kstart] = -((double) (m) * x * legendre.Pcup[kstart] / z);
      pm2 = pmm / PreSqr[2 * m + 1];
      // Calculate Pcup(m+1,m)
      k = kstart + m + 1;
      pm1 = x * PreSqr[2 * m + 1] * pm2;
      legendre.Pcup[k] = pm1*rescalem;
      legendre.dPcup[k] = ((pm2 * rescalem) * PreSqr[2 * m + 1] - x * (m + 1) * legendre.Pcup[k]) / z;
      // Calculate Pcup(n,m)
      for ( int n = m + 2; n <= nMax; ++n) {
        k = k + n;
        double plm = x * f1[k] * pm1 - f2[k] * pm2;
        legendre.Pcup[k] = plm*rescalem;
        legendre.dPcup[k] = (PreSqr[n + m] * PreSqr[n - m] * (pm1 * rescalem) - n * x * legendre.Pcup[k]) / z;
        pm2 = pm1;
        pm1 = plm;
      }
    }

    // Calculate Pcup(nMax,nMax) // m == nMax
    rescalem = rescalem*z;
    kstart = kstart + nMax + 1; 
    pmm = pmm / PreSqr[2 * nMax];
    legendre.Pcup[kstart] = pmm * rescalem;
    legendre.dPcup[kstart] = - nMax * x * legendre.Pcup[kstart] / z;
    return legendre;
  }

  /** This function evaluates all of the Schmidt-semi normalized associated Legendre functions up to degree nMax.
   * Notes: Overflow may occur if nMax > 20 , especially for high-latitudes.  Use MAG_PcupHigh for large nMax.
   * Note: In geomagnetism, the derivatives of ALF are usually found with
   * respect to the colatitudes. Here the derivatives are found with respect
   * to the latitude. The difference is a sign reversal for the derivative of
   * the Associated Legendre Functions.
   */
  private MagLegendre MAG_PcupLow( double x, int nMax )
  {
    int NumTerms = ((nMax + 1) * (nMax + 2) / 2);
    MagLegendre legendre = new MagLegendre( NumTerms );

    legendre.Pcup[0]  = 1.0;
    legendre.dPcup[0] = 0.0;

    /*sin (geocentric latitude) - sin_phi */
    double z = Math.sqrt((1.0 - x) * (1.0 + x));
    double[] schmidtQuasiNorm = new double[ NumTerms + 1 ];

    //First,	Compute the Gauss-normalized associated Legendre WMMcoeff.index(n-1,m); // functions
    for ( int n = 1; n <= nMax; n++) {
      for ( int m = 0; m <= n; m++) {
        int index = WMMcoeff.index(n,m); // n * (n + 1) / 2 + m;
        if (n == m) {
          int index1 = WMMcoeff.index(n-1,m-1); // (n - 1) * n / 2 + m - 1;
          legendre.Pcup [index] = z * legendre.Pcup[ index1 ];
          legendre.dPcup[index] = z * legendre.dPcup[index1] + x * legendre.Pcup[index1];
        } else if (n == 1 && m == 0) {
          int index1 = WMMcoeff.index(n-1,m); // (n - 1) * n / 2 + m;
          legendre.Pcup[index] = x * legendre.Pcup[index1];
          legendre.dPcup[index] = x * legendre.dPcup[index1] - z * legendre.Pcup[index1];
        } else if (n > 1 && n != m) {
          int index1 = WMMcoeff.index(n-2,m); // (n - 2) * (n - 1) / 2 + m;
          int index2 = WMMcoeff.index(n-1,m); // (n - 1) * n / 2 + m;
          if (m > n - 2) {
            legendre.Pcup[index] = x * legendre.Pcup[index2];
            legendre.dPcup[index] = x * legendre.dPcup[index2] - z * legendre.Pcup[index2];
          } else {
            double k = (double) (((n - 1) * (n - 1)) - (m * m)) / (double) ((2 * n - 1) * (2 * n - 3));
            legendre.Pcup[index] = x * legendre.Pcup[index2] - k * legendre.Pcup[index1];
            legendre.dPcup[index] = x * legendre.dPcup[index2] - z * legendre.Pcup[index2] - k * legendre.dPcup[index1];
          }
        }
      }
    }
    // Compute the ration between the the Schmidt quasi-normalized associated Legendre
    // functions and the Gauss-normalized version. 

    schmidtQuasiNorm[0] = 1.0;
    for ( int n = 1; n <= nMax; n++) {
      int index  = WMMcoeff.index(n,0);   // (n * (n + 1) / 2);
      int index1 = WMMcoeff.index(n-1,0); // (n - 1) * n / 2;
      /* for m = 0 */
      schmidtQuasiNorm[index] = schmidtQuasiNorm[index1] * (double) (2 * n - 1) / (double) n;

      for ( int m = 1; m <= n; m++) {
        index  = WMMcoeff.index(n,m);   // (n * (n + 1)) / 2 + m;
        index1 = WMMcoeff.index(n,m-1); // (n * (n + 1)) / 2 + m - 1;
        schmidtQuasiNorm[index] = schmidtQuasiNorm[index1] 
          * Math.sqrt((double) ((n - m + 1) * (m == 1 ? 2 : 1)) / (double) (n + m));
      }
    }

    // Converts the  Gauss-normalized associated Legendre functions to the Schmidt quasi-normalized version
    // using pre-computed relation stored in the variable schmidtQuasiNorm 

    for ( int n = 1; n <= nMax; n++) {
      for ( int m = 0; m <= n; m++) {
        int index = WMMcoeff.index(n,m); // (n * (n + 1)) / 2 + m;
        legendre.Pcup[index]  =   legendre.Pcup[index] * schmidtQuasiNorm[index];
        legendre.dPcup[index] = - legendre.dPcup[index] * schmidtQuasiNorm[index];
        // The sign is changed since the new WMM routines use derivative with respect to latitude insted of co-latitude 
      }
    }
    return legendre;
  } 

  /** This Function sums the secular variation coefficients to get the secular variation of the Magnetic vector.
   */
  private MagVector sumSecVar( MagLegendre legendre,
                       MagModel model,
		       MagHarmonic sph_vars,
		       MagSpherical spherical )
  {
    model.SecularVariationUsed = true;
    MagVector ret = new MagVector( 0, 0, 0 );
    for ( int n = 1; n <= model.nMaxSecVar; n++) {
        for ( int m = 0; m <= n; m++) {
            int index = (n * (n + 1) / 2 + m);
            /*		    nMax  	(n+2) 	  n     m            m           m
                    Bz =   -SUM (a/r)   (n+1) SUM  [g cos(m p) + h sin(m p)] P (sin(phi))
                                    n=1      	      m=0   n            n           n  */
            /*  Derivative with respect to radius.*/
            ret.z -= sph_vars.RelativeRadiusPower[n] *
                    (model.Secular_Var_Coeff_G[index] * sph_vars.cos_mlambda[m] +
                    model.Secular_Var_Coeff_H[index] * sph_vars.sin_mlambda[m])
                    * (double) (n + 1) * legendre.Pcup[index];
            /*		  1 nMax  (n+2)    n     m            m           m
                    By =    SUM (a/r) (m)  SUM  [g cos(m p) + h sin(m p)] dP (sin(phi))
                               n=1             m=0   n            n           n  */
            /* Derivative with respect to longitude, divided by radius. */
            ret.y += sph_vars.RelativeRadiusPower[n] *
                    (model.Secular_Var_Coeff_G[index] * sph_vars.sin_mlambda[m] -
                    model.Secular_Var_Coeff_H[index] * sph_vars.cos_mlambda[m])
                    * (double) (m) * legendre.Pcup[index];
            /*		   nMax  (n+2) n     m            m           m
                    Bx = - SUM (a/r)   SUM  [g cos(m p) + h sin(m p)] dP (sin(phi))
                               n=1         m=0   n            n           n  */
            /* Derivative with respect to latitude, divided by radius. */
            ret.x -= sph_vars.RelativeRadiusPower[n] *
                    (model.Secular_Var_Coeff_G[index] * sph_vars.cos_mlambda[m] +
                    model.Secular_Var_Coeff_H[index] * sph_vars.sin_mlambda[m])
                    * legendre.dPcup[index];
        }
    }
    double cos_phi = Math.cos( TDMath.DEG2RAD * spherical.phig );
    if ( Math.abs(cos_phi) > 1.0e-10) {
        ret.y = ret.y / cos_phi;
    } else {
        /* Special calculation for component By at Geographic poles */
        secVarSummationSpecial(model, sph_vars, spherical, ret );
    }
    return ret;
  } /*sumSecVar*/

  /** Special calculation for the secular variation summation at the poles.
   */
  private void secVarSummationSpecial( MagModel model,
                                   MagHarmonic sph_vars,
				   MagSpherical spherical,
				   MagVector res )
  {
    double[] PcupS = new double[ model.nMaxSecVar + 1 ];
    PcupS[0] = 1;
    double schmidtQuasiNorm1 = 1.0;
    res.y = 0.0;
    double sin_phi = Math.sin( TDMath.DEG2RAD * spherical.phig );
    for ( int n = 1; n <= model.nMaxSecVar; n++) {
        int index = (n * (n + 1) / 2 + 1);
        double schmidtQuasiNorm2 = schmidtQuasiNorm1 * (double) (2 * n - 1) / (double) n;
        double schmidtQuasiNorm3 = schmidtQuasiNorm2 * Math.sqrt((double) (n * 2) / (double) (n + 1));
        schmidtQuasiNorm1 = schmidtQuasiNorm2;
        if (n == 1) {
            PcupS[n] = PcupS[n - 1];
        } else {
            double k = (double) (((n - 1) * (n - 1)) - 1) / (double) ((2 * n - 1) * (2 * n - 3));
            PcupS[n] = sin_phi * PcupS[n - 1] - k * PcupS[n - 2];
        }
        /*		  1 nMax  (n+2)    n     m            m           m
                By =    SUM (a/r) (m)  SUM  [g cos(m p) + h sin(m p)] dP (sin(phi))
                           n=1             m=0   n            n           n  */
        /* Derivative with respect to longitude, divided by radius. */
        res.y += sph_vars.RelativeRadiusPower[n] *
                (model.Secular_Var_Coeff_G[index] * sph_vars.sin_mlambda[1] -
                model.Secular_Var_Coeff_H[index] * sph_vars.cos_mlambda[1])
                * PcupS[n] * schmidtQuasiNorm3;
    }
  }/*SecVarSummationSpecial*/

  /** Computes Geomagnetic Field Elements X, Y and Z in Spherical coordinate system using
   *  spherical harmonic summation.
   *  The vector Magnetic field is given by -grad V, where V is Geomagnetic scalar potential
   *  The gradient in spherical coordinates is given by:
   *                   dV ^     1 dV ^        1     dV ^
   *  grad V = -- r  +  - -- t  +  -------- -- p
   *                   dr       r dt       r sin(t) dp
   */
  private MagVector summation( MagLegendre legendre,
                       MagModel model,
		MagHarmonic sph_vars,
		MagSpherical spherical )
  {
    MagVector ret = new MagVector( 0, 0, 0 );
    for ( int n = 1; n <= model.nMax; n++) {
        for ( int m = 0; m <= n; m++) {
            int index = (n * (n + 1) / 2 + m);
            /*		    nMax  	(n+2) 	  n     m            m           m
                    Bz =   -SUM (a/r)   (n+1) SUM  [g cos(m p) + h sin(m p)] P (sin(phi))
                                    n=1      	      m=0   n            n           n  */
            /* Equation 12 in the WMM Technical report.  Derivative with respect to radius.*/
            ret.z -= sph_vars.RelativeRadiusPower[n] *
                    (model.Main_Field_Coeff_G[index] * sph_vars.cos_mlambda[m] +
                    model.Main_Field_Coeff_H[index] * sph_vars.sin_mlambda[m])
                    * (double) (n + 1) * legendre.Pcup[index];
            /*		  1 nMax  (n+2)    n     m            m           m
                    By =    SUM (a/r) (m)  SUM  [g cos(m p) + h sin(m p)] dP (sin(phi))
                               n=1             m=0   n            n           n  */
            /* Equation 11 in the WMM Technical report. Derivative with respect to longitude, divided by radius. */
            ret.y += sph_vars.RelativeRadiusPower[n] *
                    (model.Main_Field_Coeff_G[index] * sph_vars.sin_mlambda[m] -
                    model.Main_Field_Coeff_H[index] * sph_vars.cos_mlambda[m])
                    * (double) (m) * legendre.Pcup[index];
            /*		   nMax  (n+2) n     m            m           m
                    Bx = - SUM (a/r)   SUM  [g cos(m p) + h sin(m p)] dP (sin(phi))
                               n=1         m=0   n            n           n  */
            /* Equation 10  in the WMM Technical report. Derivative with respect to latitude, divided by radius. */
            ret.x -= sph_vars.RelativeRadiusPower[n] *
                    (model.Main_Field_Coeff_G[index] * sph_vars.cos_mlambda[m] +
                    model.Main_Field_Coeff_H[index] * sph_vars.sin_mlambda[m])
                    * legendre.dPcup[index];
        }
    }

    double cos_phi = Math.cos( TDMath.DEG2RAD * spherical.phig );
    if ( Math.abs(cos_phi) > 1.0e-10) {
        ret.y = ret.y / cos_phi;
    } else {
        /* Special calculation for component - By - at Geographic poles.
         * If the user wants to avoid using this function,  please make sure that
         * the latitude is not exactly +/-90. An option is to make use the function
         * MAG_CheckGeographicPoles.
         */
        specialSummation(model, sph_vars, spherical, ret);
    }
    return ret;
  } /*summation */

  /** Special calculation for the component By at Geographic poles.
   * See Section 1.4, "SINGULARITIES AT THE GEOGRAPHIC POLES", WMM Technical report
   */
  private void specialSummation( MagModel model,
                             MagHarmonic sph_vars,
			     MagSpherical spherical,
			     MagVector res )
  {
    double[] PcupS = new double[ model.nMax + 1 ];
    PcupS[0] = 1;
    double schmidtQuasiNorm1 = 1.0;
    res.y = 0.0;
    double sin_phi = Math.sin( TDMath.DEG2RAD * spherical.phig );
    for ( int n = 1; n <= model.nMax; n++) {
        /*Compute the ration between the Gauss-normalized associated Legendre
          functions and the Schmidt quasi-normalized version. This is equivalent to
          sqrt((m==0?1:2)*(n-m)!/(n+m!))*(2n-1)!!/(n-m)!  */
        int index = (n * (n + 1) / 2 + 1);
        double schmidtQuasiNorm2 = schmidtQuasiNorm1 * (double) (2 * n - 1) / (double) n;
        double schmidtQuasiNorm3 = schmidtQuasiNorm2 * Math.sqrt((double) (n * 2) / (double) (n + 1));
        schmidtQuasiNorm1 = schmidtQuasiNorm2;
        if (n == 1) {
            PcupS[n] = PcupS[n - 1];
        } else {
            double k = (double) (((n - 1) * (n - 1)) - 1) / (double) ((2 * n - 1) * (2 * n - 3));
            PcupS[n] = sin_phi * PcupS[n - 1] - k * PcupS[n - 2];
        }
        /*		  1 nMax  (n+2)    n     m            m           m
                By =    SUM (a/r) (m)  SUM  [g cos(m p) + h sin(m p)] dP (sin(phi))
                           n=1             m=0   n            n           n  */
        /* Equation 11 in the WMM Technical report. Derivative with respect to longitude, divided by radius. */
        res.y += sph_vars.RelativeRadiusPower[n] *
                (model.Main_Field_Coeff_G[index] * sph_vars.sin_mlambda[1] -
                model.Main_Field_Coeff_H[index] * sph_vars.cos_mlambda[1])
                * PcupS[n] * schmidtQuasiNorm3;
    }
  }
}
