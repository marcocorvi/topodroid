/** @file ExportSHP.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Shapefile exporter
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.TDX.TglParser;
import com.topodroid.TDX.Triangle3D;
import com.topodroid.TDX.Vector3D;
import com.topodroid.TDX.Cave3DStation;
import com.topodroid.TDX.Cave3DShot;
import com.topodroid.TDX.Archiver;
// import com.topodroid.TDX.Cave3DFile;
// import com.topodroid.TDX.TDPath;
import com.topodroid.c3walls.cw.CWFacet;
import com.topodroid.c3walls.cw.CWPoint;


import java.util.List;
import java.util.ArrayList;
// import java.util.Locale;

// import java.io.File;
import java.io.OutputStream;
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
  public boolean exportASCII( OutputStream zos, String filepath, String name, TglParser data, boolean b_legs, boolean b_splays, boolean b_walls )
  {
    // if ( data == null ) return false;
    if ( ! TDFile.makeMSdir( filepath ) ) {
      TDLog.Error("mkdir " + filepath + " error");
      return false;
    }

    boolean ret = TDFile.hasMSdir( filepath );
    if ( ! ret ) {
      TDLog.Error("no dir " + filepath + " name " + name );
      return false;
    }
    // TDLog.v("shp export. dirname " + filepath + " name " + name );
    ArrayList< String > files = new ArrayList<>(); // will contain strings "stations.shp" ...

    String sub_name  = "c3export/" + name;
    // this is dirname
    // String filepath = TDPath.getC3exportPath( name ); // export temporary folder for shp files - fullpath

    if ( ret )             ret &= exportStations( filepath, files, data.getStations() );
    if ( ret && b_legs )   ret &= exportShots( filepath, files, data.getShots(), "leg" );
    if ( ret && b_splays ) ret &= exportShots( filepath, files, data.getSplays(), "splay" );
    if ( ret && b_walls ) {
      ret &= exportFacets( filepath, files, mFacets );
      if ( ret ) ret &= exportTriangles( filepath, files, mTriangles );
    }


    if ( files.size() == 0 ) ret = false;
    if ( ret ) {
      // TDLog.v( "export SHP: make zip. files " + files.size() );
      Archiver zipper = new Archiver( );
      zipper.compressFiles( zos, sub_name, files );
      try {
        zos.close();
      } catch ( IOException e ) {
        // TODO
      }
    }
    TDFile.deleteMSdir( filepath ); // delete temporary dir

    // TDLog.v( "export SHP: returns " + ret );
    return ret;
  }

  private boolean exportStations( String filepath, List<String> files, List< Cave3DStation> stations )
  {
    if ( stations == null || stations.size() == 0 ) return true;
    // TDLog.v( "SHP Export stations " + stations.size() + " path " + filepath );
    boolean ret = false;
    try {
      ShpPointz shp = new ShpPointz( filepath + "/station", "station",  files );
      // shp.setYYMMDD( info.date );
      ret = shp.writeStations( stations );
    } catch ( IOException e ) {
      TDLog.Error( "SHP Failed station export: " + e.getMessage() );
    }
    return ret;
  }
    
  private boolean exportShots( String filepath, List<String> files, List< Cave3DShot> shots, String name )
  {
    if ( shots == null || shots.size() == 0 ) return true;
    // TDLog.v( "SHP Export " + name + " shots " + shots.size() );
    boolean ret = false;
    try {
      ShpPolylinez shp = new ShpPolylinez( filepath + "/" + name, name, files );
      // shp.setYYMMDD( info.date );
      ret = shp.writeShots( shots, name );
    } catch ( IOException e ) {
      TDLog.Error( "SHP Failed " + name + " export: " + e.getMessage() );
    }
    return ret;
  }

  private boolean exportFacets( String filepath, List<String> files, List< CWFacet > facets )
  {
    if ( facets == null || facets.size() == 0 ) return true;
    // TDLog.v( "SHP Export facets " + facets.size() );
    boolean ret = false;
    try {
      ShpPolygonz shp = new ShpPolygonz( filepath + "/facet", "facet", files );
      // shp.setYYMMDD( info.date );
      ret = shp.writeFacets( facets );
    } catch ( IOException e ) {
      TDLog.Error( "SHP Failed facet export: " + e.getMessage() );
    }
    return ret;
  }

  private boolean exportTriangles( String filepath, List<String> files, List< Triangle3D > triangles )
  {
    if ( triangles == null || triangles.size() == 0 ) return true;
    // TDLog.v( "SHP Export triangles " + triangles.size() );
    boolean ret = false;
    try {
      ShpPolygonz shp = new ShpPolygonz( filepath + "/triangle", "triangle", files );
      // shp.setYYMMDD( info.date );
      ret = shp.writeTriangles( mTriangles );
    } catch ( IOException e ) {
      TDLog.Error( "SHP Failed triangle export: " + e.getMessage() );
    }
    return ret;
  }

}

