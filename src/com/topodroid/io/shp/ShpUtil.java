/* @file ShpUtil.java
 *
 * @author marco corvi
 * @date nov 2026
 *
 * @brief TopoDroid drawing: shapefile PRJ utility
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * Only WGS84 and UTM are handled. Anything else (e.g. EPSG:3003 Monte Mario) returns null and no .prj is written, as before.
 * To support it, add a case with the WKT for that EPSG code in getPrjCrs.
 * I assume mCRS can contain strings like "WGS84", "UTM32N"-style and "EPSG:326xx".
 * TODO Log num0.mCRS once to confirm, and adjust the parsing if it differs.
 * 
 * Units.
 * The CS coordinates are multiplied by mToUnits.
 * If a CS is in feet or another non-metre unit, the UNIT["Meter",1.0] would be wrong.
 * That’s fine for UTM in metres, but a non-metric CS would need its own WKT.
 * 
 * Vertical coordinate.
 * The WKT only describes the horizontal CRS, which is what GIS software expects for a .prj.
 */
package com.topodroid.io.shp;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDFile;

// import java.io.File;
import java.io.FileOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;   

import java.nio.ByteBuffer;   
import java.nio.MappedByteBuffer;   
import java.nio.ByteOrder;   
import java.nio.channels.FileChannel;   

import java.util.List;

public class ShpUtil
{
  static String WGS84 = "GEOGCS[\"GCS_WGS_1984\",DATUM[\"D_WGS_1984\",SPHEROID[\"WGS_1984\",6378137.0,298.257223563]],PRIMEM[\"Greenwich\",0.0],UNIT[\"Degree\", 0.0174532925199433]]";
 
  static String UTM( int zone, boolean north )
  { 
    int cm = zone * 6 - 183;
    return "PROJCS[\"WGS 84 / UTM zone " + zone + ( north? "N" : "S" ) + "\"," + WGS84
        + ",PROJECTION[\"Transverse_Mercator\"],"
        + "PARAMETER[\"latitude_of_origin\",0],"
        + "PARAMETER[\"central_meridian\"," + cm + "],"
        + "PARAMETER[\"scale_factor\",0.9996],"
        + "PARAMETER[\"false_easting\",500000],"
        + "PARAMETER[\"false_northing\"," + ( north ? "0.0" : "10000000.0" ) + "],"
        + "UNIT[\"metre\",1.0]]";
  }


  /** @return the prj string given the CRS 
   * @param crs   CRS, can be WGS84, or UTMxxH or EPSG:326xx or EPSG:327xx, where xx is the zone (01 .. 60) and H is 'N' or 'S'
   */
  public static String getPrjCrs( String crs ) 
  {
    TDLog.v("SHP util get PRJ for " + crs );
    if ( crs == null ) return null;
    if ( crs.equals( "WGS84" ) ) return WGS84;
    try {
      if ( crs.startsWith( "UTM" ) && crs.length() >= 6 ) {
        int zone = Integer.parseInt( crs.substring(3,5) );
        char h = crs.charAt( 5 );
        if ( zone >= 1 && zone <= 60 && ( h == 'N' || h == 'S' ) ) return UTM( zone, (h == 'N') );
      } else if ( crs.startsWith( "EPSG:326" ) && crs.length() == 10 ) { // WGS84 / UTM north
        return UTM( Integer.parseInt( crs.substring( 8, 10 ) ), true );
      } else if ( crs.startsWith( "EPSG:327" ) && crs.length() == 10 ) { // WGS84 / UTM south
        return UTM( Integer.parseInt( crs.substring( 8, 10 ) ), false );
      }
    } catch ( NumberFormatException e ) { }
    return null;
  }

  /** write a .prj file in the export directory and register it for the archive
   * @param subdir  export directory
   * @param name    shapefile base name (without extension)
   * @param wkt     WKT string
   * @param files   list of files to archive
   */
  public static void writePrjFile( String subdir, String name, String wkt, List< String > files ) throws IOException
  {
    if ( wkt == null ) return;
    String prj_name = name + ".prj";
    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream( TDFile.getMSfile( subdir, prj_name ) );
      fos.write( wkt.getBytes( "US-ASCII" ) );
      fos.flush();
      if ( files != null ) files.add( prj_name );
    } catch ( IOException e ) {
      TDLog.e( "SHP write prj " + e.getMessage() );
      throw e;
    } finally {
      if ( fos != null ) fos.close();
    }
  }

}
