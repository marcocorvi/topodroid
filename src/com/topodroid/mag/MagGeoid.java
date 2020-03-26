/* @file MagGeoid.java
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

// MAGtype_Geoid
class MagGeoid
{
  private int NumbGeoidCols;
  private int NumbGeoidRows;
  private int NumbHeaderItems;
  private int ScaleFactor;
  private int NumbGeoidElevs;
  private boolean Geoid_Initialized;
  private boolean UseGeoid;
  private float[] GeoidHeightBuffer;  // geoid height relative to ellipsoid

  /* Sets EGM-96 model file parameters */
  MagGeoid( float[] heights )
  {
    NumbGeoidCols = 1441; // 360 degrees of longitude at 15 minute spacing
    NumbGeoidRows = 721;  // 180 degrees of latitude  at 15 minute spacing
    NumbHeaderItems = 6;  // min, max lat, min, max long, lat, long spacing
    ScaleFactor     = 4;  // 4 grid cells per degree at 15 minute spacing
    NumbGeoidElevs = NumbGeoidCols * NumbGeoidRows; // 1.038.961
    Geoid_Initialized = false; // not initialized
    GeoidHeightBuffer = null;
    UseGeoid = MagUtil.MAG_USE_GEOID;

    if ( heights != null ) {
      GeoidHeightBuffer = heights;
      Geoid_Initialized = true;
    }
  }

  /*
   * The function Convert_Geoid_To_Ellipsoid_Height converts the specified WGS84
   * Geoid height at the specified geodetic coordinates to the equivalent
   * ellipsoid height, using the EGM96 gravity model.
   *
   *   geodetic->phi        : Geodetic latitude in degress           (input)
   *    geodetic->lambda     : Geodetic longitude in degrees          (input)
   *    geodetic->HeightAboveEllipsoid	     : Ellipsoid height, in kilometers         (output)
   *    geodetic->HeightAboveGeoid: Geoid height, in kilometers           (input)
   *
          CALLS : MAG_GetGeoidHeight (
   */
  void convertGeoidToEllipsoidHeight( MagGeodetic geodetic )
  {
    if ( UseGeoid ) { /* Geoid correction required */
      MagLatLong rep = equivalentLatLon(geodetic.phi, geodetic.lambda ); // ensure lat < 90
      double delta   = getGeoidHeight( rep );
      geodetic.HeightAboveEllipsoid = geodetic.HeightAboveGeoid + delta / 1000;
      /*  Input and output should be km. getGeoidHeight returns Geoid height in m - Hence division by 1000 */
    } else { /* Geoid correction not required, copy the MSL height to Ellipsoid height */
      geodetic.HeightAboveEllipsoid = geodetic.HeightAboveGeoid;
    }
  } 

  void convertEllipsoidToGeoidHeight( MagGeodetic geodetic )
  {
    if ( UseGeoid ) { /* Geoid correction required */
      MagLatLong rep = equivalentLatLon( geodetic.phi, geodetic.lambda ); // ensure lat < 90
      double delta   = getGeoidHeight( rep );
      geodetic.HeightAboveGeoid = geodetic.HeightAboveEllipsoid - delta / 1000;
      /*  Input and output should be km. getGeoidHeight returns Geoid height in m - Hence division by 1000 */
    } else { /* Geoid correction not required, copy the MSL height to Ellipsoid height */
      geodetic.HeightAboveGeoid = geodetic.HeightAboveEllipsoid;
    }
  } 

  /*This function takes a latitude and longitude that are ordinarily out of range 
   and gives in range values that are equivalent on the Earth's surface.  This is
   required to get correct values for the geoid function.*/
  private MagLatLong equivalentLatLon(double lat, double lon )
  {
    MagLatLong ret = new MagLatLong();
    double colat = 90 - lat;
    ret.lng = lon;
    if (colat < 0) colat = -colat;
    while (colat > 360) colat -= 360;
    if (colat > 180) {
        colat-=180;
        ret.lng = ret.lng+180;
    }
    ret.lat = 90 - colat;
    if (ret.lng > 360) ret.lng-=360;
    if (ret.lng < -180) ret.lng+=360;
    return ret;
  }

  /** The  function getGeoidHeight returns the height of the EGM96 geiod above or below the WGS84 ellipsoid,
   * at the specified geodetic coordinates, using a grid of height adjustments from the EGM96 gravity model.
   *    Latitude            : Geodetic latitude in radians           (input)
   *    Longitude           : Geodetic longitude in radians          (input)
   *    DeltaHeight         : Height Adjustment, in meters.          (output)
   */
  private double getGeoidHeight( MagLatLong rep )
  {
    if (  ( ! Geoid_Initialized )
       || (( rep.lat < -90) || (rep.lat > 90)) /* Latitude out of range */
       || ((rep.lng < -180) || (rep.lng > 360)) ) { /* Longitude out of range */
      return 0;
    }
    /*  Compute X and Y Offsets into geoid Height Array:                          */

    double OffsetX = ( rep.lng < 0.0) ?  (rep.lng + 360.0) * ScaleFactor
                                      : rep.lng * ScaleFactor;
    double OffsetY = (90.0 - rep.lat) * ScaleFactor;

    /*  Find Four Nearest geoid Height Cells for specified Latitude, Longitude;   */
    /*  Assumes that (0,0) of geoid Height Array is at Northwest corner:          */
    int PostX = (int)(OffsetX);
    if((PostX + 1) == NumbGeoidCols) PostX--;
    int PostY = (int)(OffsetY);
    if((PostY + 1) == NumbGeoidRows) PostY--;

    int Index = (PostY * NumbGeoidCols + PostX);
    double ElevationNW = (double) GeoidHeightBuffer[ Index ];
    double ElevationNE = (double) GeoidHeightBuffer[ Index + 1 ];
    Index = ((PostY + 1) * NumbGeoidCols + PostX);
    double ElevationSW = (double) GeoidHeightBuffer[ Index ];
    double ElevationSE = (double) GeoidHeightBuffer[ Index + 1 ];

    /*  Perform Bi-Linear Interpolation to compute Height above Ellipsoid:        */
    double DeltaX = OffsetX - PostX;
    double DeltaY = OffsetY - PostY;
    double UpperY = ElevationNW + DeltaX * (ElevationNE - ElevationNW);
    double LowerY = ElevationSW + DeltaX * (ElevationSE - ElevationSW);
    return UpperY + DeltaY * (LowerY - UpperY);
  } 

}

