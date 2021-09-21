/** @file ExportSHP.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Shapefile exporter
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

import com.topodroid.utils.TDLog;
import com.topodroid.Cave3X.TglParser;
import com.topodroid.Cave3X.Triangle3D;
import com.topodroid.Cave3X.Vector3D;
import com.topodroid.Cave3X.Cave3DStation;
import com.topodroid.Cave3X.Cave3DShot;
import com.topodroid.Cave3X.Cave3DFile;
import com.topodroid.c3walls.cw.CWFacet;
import com.topodroid.c3walls.cw.CWPoint;


import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import java.io.File;
import java.io.IOException;


public class ExportSHP
{
  ArrayList< CWFacet > mFacets;
  public ArrayList< Triangle3D > mTriangles; // powercrust triangles

  public Vector3D[] mVertex; // triangle vertices
  Vector3D mMin;
  Vector3D mMax;
  double xoff, yoff, zoff; // offset to have positive coords values
  double scale;       // scale factor

  public ExportSHP()
  {
    mFacets = new ArrayList< CWFacet >();
    mTriangles = null;
    mVertex    = null; 
    resetMinMax();
  }

  private void resetMinMax()
  {
    xoff = 0;
    yoff = 0;
    zoff = 0;
    scale = 1.0f;
    mMin = new Vector3D();
    mMax = new Vector3D();
  }

  public void add( CWFacet facet ) { mFacets.add( facet ); }

  public void add( CWPoint v1, CWPoint v2, CWPoint v3 )
  {
     mFacets.add( new CWFacet( v1, v2, v3 ) );
  }

  // ---------------------------------------------------------------------------

  // @param name survey/model name
  public boolean exportASCII( String name, TglParser data, boolean b_legs, boolean b_splays, boolean b_walls )
  {
    if ( data == null ) return false;

    boolean ret = true;
    ArrayList<File> files = new ArrayList<File>();

    String filepath = Cave3DFile.getFilepath( name ); // export temporary folder for shp files
    String filename = filepath + ".shz";              // export shp file (zipped)
    // TDLog.v( "export SHP: L " + b_legs + " S " + b_splays + " W " + b_walls + "> " + filename ); 

    File path = new File(filepath);
    if ( ! path.exists() ) {
      path.mkdirs();
    }

    if ( ret )             ret &= exportStations( filepath, files, data.getStations() );
    if ( ret && b_legs )   ret &= exportShots( filepath, files, data.getShots(), "leg" );
    if ( ret && b_splays ) ret &= exportShots( filepath, files, data.getSplays(), "splay" );
    if ( ret && b_walls ) {
      ret &= exportFacets( filepath, files, mFacets );
      if ( ret && mTriangles != null ) ret &= exportTriangles( filepath, files, mTriangles );
    }

    if ( ret ) {
      Archiver zipper = new Archiver( );
      zipper.compressFiles( filename, files );
    }
    deleteDir( path ); // delete temporary shapedir

    return true;
  }

  private boolean exportStations( String filepath, List<File> files, List< Cave3DStation> stations )
  {
    // TDLog.v( "SHP Export stations " + stations.size() );
    boolean ret = false;
    try {
      ShpPointz shp = new ShpPointz( filepath + "/station",  files );
      // shp.setYYMMDD( info.date );
      ret = shp.writeStations( stations );
    } catch ( IOException e ) {
      TDLog.v( "SHP Failed station export: " + e.getMessage() );
    }
    return ret;
  }
    
  private boolean exportShots( String filepath, List<File> files, List< Cave3DShot> shots, String name )
  {
    // TDLog.v( "SHP Export " + name + " shots " + shots.size() );
    boolean ret = false;
    try {
      ShpPolylinez shp = new ShpPolylinez( filepath + "/" + name, files );
      // shp.setYYMMDD( info.date );
      ret = shp.writeShots( shots, name );
    } catch ( IOException e ) {
      TDLog.v( "SHP Failed " + name + " export: " + e.getMessage() );
    }
    return ret;
  }

  private boolean exportFacets( String filepath, List<File> files, List< CWFacet > facets )
  {
    // TDLog.v( "SHP Export facets " + facets.size() );
    boolean ret = false;
    try {
      ShpPolygonz shp = new ShpPolygonz( filepath + "/facet", files );
      // shp.setYYMMDD( info.date );
      ret = shp.writeFacets( facets );
    } catch ( IOException e ) {
      TDLog.v( "SHP Failed facet export: " + e.getMessage() );
    }
    return ret;
  }

  private boolean exportTriangles( String filepath, List<File> files, List< Triangle3D > triangles )
  {
    // TDLog.v( "SHP Export triangles " + triangles.size() );
    boolean ret = false;
    try {
      ShpPolygonz shp = new ShpPolygonz( filepath + "/triangle", files );
      // shp.setYYMMDD( info.date );
      ret = shp.writeTriangles( mTriangles );
    } catch ( IOException e ) {
      TDLog.v( "SHP Failed triangle export: " + e.getMessage() );
    }
    return ret;
  }

  static void deleteDir( File dir )
  {
    if ( dir != null && dir.exists() ) {
      File[] files = dir.listFiles();
      if ( files != null ) {
        for (File file : files ) {
          if (file.isFile()) {
            if ( ! file.delete() ) TDLog.v( "SHP File delete failed " + file.getName() ); 
          }
        }
      }
      if ( ! dir.delete() ) TDLog.v( "SHP Dir delete failed " + dir.getName() );
    }
  }

}

