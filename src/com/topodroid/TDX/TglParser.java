/** @file TglParser.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D file parser 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDFile;
import com.topodroid.c3out.ExportData;
import com.topodroid.c3out.ExportKML;
import com.topodroid.c3out.ExportCGAL;
import com.topodroid.c3out.ExportLAS;
import com.topodroid.c3out.ExportDXF;
import com.topodroid.c3out.ExportSHP;
import com.topodroid.c3out.ExportSTL;
import com.topodroid.c3in.LoxBitmap;
import com.topodroid.c3walls.bubble.BubbleComputer;
import com.topodroid.c3walls.hull.HullComputer;
import com.topodroid.c3walls.cw.CWTriangle;
import com.topodroid.c3walls.cw.CWBorder;
import com.topodroid.c3walls.cw.CWConvexHull;
import com.topodroid.c3walls.cw.ConvexHullComputer;
import com.topodroid.c3walls.pcrust.PowercrustComputer;

// import java.io.File;
import java.io.IOException;
// import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.BufferedReader;
// import java.io.FileWriter;
// import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
// import java.io.FileOutputStream;
// import java.io.BufferedInputStream;
// import java.io.BufferedOutputStream;

import java.util.ArrayList;
import java.util.Stack;
import java.util.regex.Pattern;
import java.util.Locale;

// import android.widget.Toast;

import android.os.AsyncTask;

// import android.net.Uri;

public class TglParser
{
  public static final int WALL_NONE       = 0;
  public static final int WALL_CW         = 1;
  public static final int WALL_HULL       = 2;
  public static final int WALL_POWERCRUST = 3;
  public static final int WALL_TUBE       = 4;
  public static final int WALL_BUBBLE     = 5;
  public static final int WALL_DELAUNAY   = 6; // not included
  public static final int WALL_MAX        = 3;

  public static final int SPLAY_USE_SKIP     = 0;
  public static final int SPLAY_USE_NORMAL   = 1;
  public static final int SPLAY_USE_XSECTION = 2;
  public static int mSplayUse = SPLAY_USE_NORMAL;

  boolean do_render; // whether ready to render
  protected TopoGL mApp;
  // boolean has_temperature = false; // TEMPERATURE

  protected ArrayList< Cave3DSurvey >   surveys;
  protected ArrayList< Cave3DFix >      fixes;
  protected ArrayList< Cave3DStation >  stations;
  protected ArrayList< Cave3DShot >     shots;   // centerline shots
  protected ArrayList< Cave3DShot >     splays;  // splay shots
  protected ArrayList< Cave3DXSection > xsections = null;

  PowercrustComputer powercrustcomputer = null;
  ConvexHullComputer convexhullcomputer = null;
  HullComputer hullcomputer = null;
  TubeComputer tubecomputer = null;
  BubbleComputer bubblecomputer = null;

  // private ArrayList< CWConvexHull > walls   = null;
  // private ArrayList< CWBorder >     borders = null;

  protected DEMsurface mSurface;
  protected LoxBitmap  mBitmap = null;
  public double mCaveLength;
  protected String mName;     // survey base name

  // Cave3DStation mCenterStation = null;
  protected Cave3DStation mStartStation = null;
  protected Cave3DFix mOrigin = null; // coordinates of the origin station

  Cave3DFix getOrigin() { return mOrigin; }
  boolean hasOrigin() { return mOrigin != null; }
  boolean isWGS84() { return mOrigin != null && mOrigin.isWGS84(); }
  boolean hasWGS84() { return mOrigin != null && mOrigin.hasWGS84(); }
  double getSNradius() { return ( mOrigin != null )? mOrigin.getSNradius() : 1.0f; }
  double getWEradius() { return ( mOrigin != null )? mOrigin.getWEradius() : 1.0f; }
  double lngToEast( double lng, double lat, double alt ) { return (mOrigin != null)? mOrigin.lngToEast( lng, lat, alt ) : 0.0; }
  double latToNorth( double lat, double alt ) { return (mOrigin != null)? mOrigin.latToNorth( lat, alt ) : 0.0; }

  boolean isEmpty() { return shots.size() == 0 && splays.size() == 0; }

  // void setStartStation( Cave3DStation station ) { mStartStation = station; }
  void clearStartStation( ) { mStartStation = null; }
  void setStartStation( String fullname ) { mStartStation = getStation( fullname ); }

  // ----------------------------------------------------------------

  // TODO VECTOR
  protected double x0, y0, z0;       // model center coords (remain fixed)

  public double emin, emax, nmin, nmax, zmin, zmax; // survey bounds

  // ------------------------- LINE UTIL
  static Pattern pattern = Pattern.compile( "\\s+" );

  protected static String[] splitLine( String line )
  {
     return pattern.split(line); // line.split( "\\s+" );
  }
  // -------------------------------------

  public String getName() { return mName; }

  // void centerAtStation( Cave3DStation st ) { mCenterStation = st; }
   
  protected boolean mSurfaceFlipped = false; // somehow lox file cane be surface-flipped FIXME FIXME FIXME
  boolean surfaceFlipped() { return mSurfaceFlipped; }

  // public double getEmin() { return emin; }
  // public double getEmax() { return emax; }
  // public double getNmin() { return nmin; }
  // public double getNmax() { return nmax; }
  // public double getVmin() { return zmin; }
  // public double getVmax() { return zmax; }

  public int getStationNumber()  { return (stations == null)?  0 : stations.size(); }
  public int getShotNumber()     { return (shots == null)?     0 : shots.size(); }
  public int getSplayNumber()    { return (splays == null)?    0 : splays.size(); }
  public int getSurveyNumber()   { return (surveys == null)?   0 : surveys.size(); }
  public int getXSectionNumber() { return (xsections == null)? 0 : xsections.size(); }

  public ArrayList< Cave3DSurvey >   getSurveys()   { return surveys; }
  public ArrayList< Cave3DShot >     getShots()     { return shots; }
  public ArrayList< Cave3DShot >     getSplays()    { return splays; }
  public ArrayList< Cave3DStation >  getStations()  { return stations; }
  public ArrayList< Cave3DFix >      getFixes()     { return fixes; }
  public ArrayList< Cave3DXSection > getXSections() { return xsections; }

  public Cave3DShot getShot( int k ) { return shots.get(k); }

  public DEMsurface getSurface() { return mSurface; }
  public boolean hasSurface() { return mSurface != null; }
  public boolean hasWall() 
  { return convexhullcomputer != null
        || ( powercrustcomputer != null && powercrustcomputer.hasTriangles() );
  }
  public boolean hasPlanview() { return powercrustcomputer != null && powercrustcomputer.hasPlanview(); }

  double getConvexHullVolume() { return ( convexhullcomputer == null )? 0 : convexhullcomputer.getVolume(); }
  double getPowercrustVolume() { return ( powercrustcomputer == null )? 0 : powercrustcomputer.getVolume(); }

  public Cave3DXSection getXSectionAt( Cave3DStation st ) 
  {
    if ( st == null ) return null;
    for ( Cave3DXSection xsection : xsections ) if ( xsection.station == st ) return xsection;
    return null;
  }

  // UNUSED
  // protected boolean checkPath( String path )
  // {
  //   if ( path == null ) return false;
  //   File file = new File( path );
  //   if ( ! file.exists() ) {
  //     TDToast.makeBad( R.string.error_file_not_found, path );
  //     return false;
  //   }
  //   if ( ! file.canRead() ) {
  //     TDToast.makeBad( R.string.error_file_not_readable, path );
  //     return false;
  //   }
  //   return true;
  // }

  private void initStationsPathlength()
  {
    for ( Cave3DStation st : stations ) st.setPathlength( Float.MAX_VALUE, null, 0, 0 );
  }

  TglMeasure computeCavePathlength( Cave3DStation s2 )
  { 
    Cave3DStation s1 = mStartStation;
    if ( s1 == null ) return null;
    if ( s2 == null || s2 == s1 ) return null;
    initStationsPathlength( );
    s1.setPathlength( 0, null, 0, 0 );
    Stack<Cave3DStation> stack = new Stack<Cave3DStation>();
    stack.push( s1 );
    while ( ! stack.empty() ) {
      Cave3DStation s0 = stack.pop();
      ArrayList< Cave3DShot > legs = getLegsAt( s0, true );
      for ( Cave3DShot leg : legs ) {
        Cave3DStation s3 = leg.getOtherStation( s0 );
        if ( s3 != null ) {
          double d = s0.getPathlength() + leg.len;
          if ( s3.getPathlength() > d ) {
            // compute dpos and dneg
            double dpos = s0.getPathDistPos();
            double dneg = s0.getPathDistNeg();
            double cln  = Math.abs(leg.cln) * 180.0 / Math.PI; // degrees
            if ( cln > 10.0 ) {
              double dz = leg.len * Math.sin( leg.cln );
              if ( cln < 30.0 ) dz *= ( 30.0 - cln )/20.0;
              if ( s0 == leg.to_station ) dz = -dz; // if leg = s3--s0 reverse dz
              if ( dz < 0 ) {
                dneg += dz;
              } else {
                dpos += dz;
              }
            }
            s3.setPathlength( d, s0, dpos, dneg );
            stack.push( s3 );
          }
        }
      }
    }
    return new TglMeasure( mApp.getResources(), s1, s2, s2.getFinalPathlength(), s2.getPathDistPos(), s2.getPathDistNeg() );
  }

  /** @return a survey by the name
   * @param name   name of the survey
   */
  public Cave3DSurvey getSurvey( String name )
  {
    if ( name == null ) return null;
    for ( Cave3DSurvey s : surveys ) if ( name.equals( s.name ) ) return s;
    return null;
  }

  /** @return a survey by the ID
   * @param id   ID of the survey
   */
  public Cave3DSurvey getSurvey( int id )
  {
    if ( id < 0 ) return null;
    for ( Cave3DSurvey s : surveys ) if ( s.mId == id ) return s;
    return null;
  }

  /** @return a station by the ID
   * @param id   ID of the station
   */
  public Cave3DStation getStation( int id )
  {
    if ( id < 0 ) return null;
    for ( Cave3DStation s : stations ) if ( s.mId == id ) return s;
    return null;
  }

  /** @return a station by the full-name
   * @param fullname   full-name of the station
   */
  public Cave3DStation getStation( String fullname )
  {
    if ( fullname == null ) return null;
    for ( Cave3DStation s : stations ) if ( fullname.equals( s.getFullName() ) ) return s;
    return null;
  }

  /** @return a station index by the full-name
   * @param fullname   full-name of the station
   */
  private int getStationIndex( String fullname )
  {
    if ( fullname == null ) return -1;
    for ( int k = 0; k < stations.size(); ++k ) if ( fullname.equals( stations.get(k).getFullName() ) ) return k;
    return -1;
  }

  public double getCaveZMin()   { return zmin; }
  public double getCaveDepth()  { return zmax - zmin; }
  public double getCaveLength() { return mCaveLength; }

  /** @return the array of the station coordinates 
   * for each station the array contains the X, Y, and Z coordinates
   */
  public double[] getStationVertices()
  {
    double[] v = new double[ 3 * stations.size() ];
    int k = 0;
    int k3 = 0;
    for ( Cave3DStation s : stations ) {
      s.vertex = k++;
      v[k3++] = s.x; // X horizontal
      v[k3++] = s.y; // Y vertical
      v[k3++] = s.z; // Z depth
    }
    return v;
  }

  /** @return the array of the splay endpoints coordinates 
   * for each endpoint the array contains the X, Y, and Z coordinates
   */
  public double[] getSplaysEndpoints()
  {
    double[] v = new double[ 3 * splays.size() ];
    int k3=0;
    for ( Cave3DShot sp : splays ) {
      Vector3D vv = sp.toPoint3D();
      if ( vv != null ) {
        v[k3++] = vv.x;
        v[k3++] = vv.y;
        v[k3++] = vv.z;
      }
    } 
    return v;
  }
 
  /** 3D vertices of the centerline shots
   */
  // public double[] getShotVertices()
  // {
  //   double v[] = new double[ 3 * 2 * shots.size() ];
  //   int k = 0;
  //   for ( Cave3DShot sh : shots ) {
  //     Cave3DStation sf = sh.from_station;
  //     Cave3DStation st = sh.to_station;
  //     // TDLog.v("getShotVertices shot " + sh.from + " " + sh.to );
  //     if ( sf != null && st != null ) {
  //       v[k++] = sf.e;
  //       v[k++] = sf.z;
  //       v[k++] = sf.n;
  //       v[k++] = st.e;
  //       v[k++] = st.z;
  //       v[k++] = st.n;
  //     } else {
  //       if ( sf == null ) TDLog.Error( " shot has null from station" );
  //       if ( st == null ) TDLog.Error( " shot has null to station" );
  //     }
  //   }
  //   return v;
  // }


  /** 3D vertices of the splay shots
   */
  // public double[] getSplayVertices()
  // {
  //   double v[] = new double[ 3 * 2 * splays.size() ];
  //   int k = 0;
  //   for ( Cave3DShot sh : splays ) {
  //     Cave3DStation sf = sh.from_station;
  //     Cave3DStation st = sh.to_station;
  //     // TDLog.v("getShotVertices shot " + sh.from + " " + sh.to );
  //     if ( sf != null ) {
  //       v[k++] = sf.e;
  //       v[k++] = sf.z;
  //       v[k++] = sf.n;
  //       v[k++] = st.e;
  //       v[k++] = st.z;
  //       v[k++] = st.n;
  //     } else {
  //       if ( sf == null ) TDLog.Error( " shot has null from station" );
  //     }
  //   }
  //   return v;
  // }

  /** get the legs with FROM at "station"
   * @param station    FROM station
   */
  public ArrayList< Cave3DShot > getLegsAtFrom( Cave3DStation station )
  {
    ArrayList< Cave3DShot > ret = new ArrayList< Cave3DShot >();
    for ( Cave3DShot shot : shots ) {
      if ( shot.from_station == station ) {
        ret.add( shot );
      }
    }
    return ret;
  }

  /** get the legs with TO at "station"
   * @param station    TO station
   */
  public ArrayList< Cave3DShot > getLegsAtTo( Cave3DStation station )
  {
    ArrayList< Cave3DShot > ret = new ArrayList< Cave3DShot >();
    for ( Cave3DShot shot : shots ) {
      if ( shot.to_station == station ) {
        ret.add( shot );
      }
    }
    return ret;
  }

  /** get the legs at "station"
   * @param station   station
   * @param reverse   if true reverse leg if at to-station
   */
  public ArrayList< Cave3DShot > getLegsAt( Cave3DStation station, boolean reverse )
  {
    ArrayList< Cave3DShot > ret = new ArrayList< Cave3DShot >();
    for ( Cave3DShot shot : shots ) {
      if ( shot.from_station == station ) {
        ret.add( shot );
      } else if ( shot.to_station == station ) {
        if ( reverse ) {
          double b = shot.ber + Math.PI;
          if ( b > 2*Math.PI ) b -= 2*Math.PI;
          ret.add( new Cave3DShot( station, shot.from_station, shot.len, b, -shot.cln, shot.mFlag, shot.mMillis) );
        } else {
          ret.add( shot );
        }
      }
    }
    return ret;
  }

  /** get the legs at "station"
   *  the legs that do not connect "other" are reversed
   *  Therefore the return has legs
   *      station - other
   *      xxx - station
   */
  public ArrayList< Cave3DShot > getLegsAt( Cave3DStation station, Cave3DStation other )
  {
    // TDLog.v( "get legs at " + station.name + " other " + other.name );
    ArrayList< Cave3DShot > ret = new ArrayList< Cave3DShot >();
    for ( Cave3DShot shot : shots ) { // add survey legs too1
      if ( shot.from_station == station ) {
        if ( shot.to_station == other ) {
          // TDLog.v( "add direct shot " + shot.from + "-" + shot.to );
          ret.add( shot );
        } else {
          // TDLog.v( "add other shot " + shot.to + "-" + shot.from );
          double b = shot.ber + Math.PI;
          if ( b > 2*Math.PI ) b -= 2*Math.PI;
          ret.add( new Cave3DShot( null, station, shot.len, b, -shot.cln, shot.mFlag, shot.mMillis) ); // stations not important
        }
      } else if ( shot.to_station == station ) {
        if ( shot.from_station == other ) {
          // TDLog.v( "add reversed shot " + shot.to + "-" + shot.from );
          double b = shot.ber + Math.PI;
          if ( b > 2*Math.PI ) b -= 2*Math.PI;
          ret.add( new Cave3DShot( null, station, shot.len, b, -shot.cln, shot.mFlag, shot.mMillis) ); // stations not important
        } else {
          // TDLog.v( "add other shot " + shot.from + "-" + shot.to );
          ret.add( shot );
        }
      }
    }
    return ret;
  }

  /** get the legs at "station" except the leg that connects to "other"
   */
  public ArrayList< Cave3DShot > getLegsAtExcept( Cave3DStation station, Cave3DStation other )
  {
    // TDLog.v( "get legs at " + station.name + " other " + other.name );
    ArrayList< Cave3DShot > ret = new ArrayList< Cave3DShot >();
    for ( Cave3DShot shot : shots ) { // add survey legs too1
      if ( shot.from_station == station ) {
        if ( shot.to_station != other ) {
          ret.add( shot );
        } 
      } else if ( shot.to_station == station ) {
        if ( shot.from_station != other ) {
          // TDLog.v( "add reversed shot " + shot.to + "-" + shot.from );
          double b = shot.ber + Math.PI;
          if ( b > 2*Math.PI ) b -= 2*Math.PI;
          ret.add( new Cave3DShot( null, station, shot.len, b, -shot.cln, shot.mFlag, shot.mMillis) ); // stations not important
        } 
      }
    }
    return ret;
  }

  public ArrayList< Cave3DShot > getSplayAt( Cave3DStation station, boolean also_legs )
  {
    ArrayList< Cave3DShot > ret = new ArrayList< Cave3DShot >();
    for ( Cave3DShot splay : splays ) {
      if ( splay.from_station == station ) {
        ret.add( splay );
      }
    }
    if ( also_legs ) {
      for ( Cave3DShot shot : shots ) { // add survey legs too1
        if ( shot.from_station == station ) {
          ret.add( shot );
        } else if ( shot.to_station == station ) {
          double b = shot.ber + Math.PI;
          if ( b > 2*Math.PI ) b -= 2*Math.PI;
          ret.add( new Cave3DShot( station, null, shot.len, b, -shot.cln, shot.mFlag, shot.mMillis ) ); // stations not important
        }
      }
    }
    return ret;
  }

  protected void setStationDepths( )
  {
    double deltaz = zmax - zmin + 0.001f;
    int k = 0;
    for ( Cave3DStation s : stations ) {
      s.depth = (zmax - s.z)/deltaz; // Z depth
      // TDLog.v("Depth " + s.name + " " + s.depth ); 
    }  
    if ( stations.size() > 0 ) do_render = true;
  }

  // @param filename either a filename or a name 
  //        in the first case the name is the part after the last '/' and before the '.'
  public TglParser( TopoGL app, String filename )
  {
    // TDLog.v("TglParser parsing " + filename );
    int pos = filename.lastIndexOf('/');
    if ( pos > 0 ) {
      mName = filename.substring(pos+1);
      pos = mName.lastIndexOf('.');
      if ( pos > 0 ) mName = mName.substring(0,pos);
    } else {
      mName = filename;
    }
    init( app, mName );
  }

  /** initialize the parser structures
   * @param app   3D viewer activity
   * @param name  model name ?
   */
  private void init( TopoGL app, String name )
  {
    // TDLog.v("TglParser init " + name );
    mApp      = app;
    do_render = false;
    mOrigin   = null;
    mName     = name;
    mSurface  = null;

    fixes     = new ArrayList< Cave3DFix >();
    shots     = new ArrayList< Cave3DShot >();
    splays    = new ArrayList< Cave3DShot >();
    surveys   = new ArrayList< Cave3DSurvey >();
    stations  = new ArrayList< Cave3DStation >();
    xsections = new ArrayList< Cave3DXSection >();
  }

  /** compute the bounding box of the 3D model
   */
  protected void computeBoundingBox()
  {
    if ( stations.size() == 0 ) return;
    emin = emax = stations.get(0).x;
    nmin = nmax = stations.get(0).y;
    zmin = zmax = stations.get(0).z;
    for ( Cave3DStation s : stations ) {
      if ( nmin > s.y )      nmin = s.y;
      else if ( nmax < s.y ) nmax = s.y;
      if ( emin > s.x )      emin = s.x;
      else if ( emax < s.x ) emax = s.x;
      if ( zmin > s.z )      zmin = s.z;
      else if ( zmax < s.z ) zmax = s.z;
    }
    x0 = (emax + emin)/2;  // survey center point
    y0 = (nmax + nmin)/2;
    z0 = (zmax + zmin)/2;
  }

  // ---------------------------------------- EXPORT
  // This is executed in ExportTask
  public boolean exportShp( DataOutputStream zos, String pathname, ExportData export )
  {
    ExportSHP shp = new ExportSHP();
    if ( export.mWalls ) {
      if ( convexhullcomputer != null ) {
        for ( CWConvexHull cw : convexhullcomputer.getWalls() ) {
          synchronized( cw ) {
            for ( CWTriangle f : cw.mFace ) shp.add( f );
          }
        }
      } 
      if ( powercrustcomputer != null && powercrustcomputer.hasTriangles() ) {
        shp.mTriangles = powercrustcomputer.getTriangles();
        shp.mVertex    = powercrustcomputer.getVertices();
      }
    }
    String filepath = TDPath.getC3exportPath( export.mName ); // export temporary folder for shp files - fullpath
    // TDLog.v("export SHP: name " + export.mName + " path " + filepath );
    return shp.exportASCII( zos, filepath, export.mName, this, true, export.mSplays, export.mWalls );
  }

  public boolean exportModelBinary( int type, DataOutputStream dos, ExportData export ) // , boolean overwrite )
  { 
    boolean ret = false;
    if ( type == ModelType.LAS_BINARY ) { // LAS v. 1.2
      ret = ExportLAS.exportBinary( dos, this, export.mSplays, export.mWalls, export.mSurface );
    } else if ( type == ModelType.STL_BINARY ) {
      ExportSTL stl = new ExportSTL();
      if ( export.mWalls ) {
        if ( convexhullcomputer != null ) {
          for ( CWConvexHull cw : convexhullcomputer.getWalls() ) {
            synchronized( cw ) {
              for ( CWTriangle f : cw.mFace ) stl.add( f );
            }
          }
        } else if ( powercrustcomputer != null && powercrustcomputer.hasTriangles() ) {
          stl.mTriangles = powercrustcomputer.getTriangles();
          stl.mVertex    = powercrustcomputer.getVertices();
        } else { 
          // TDLog.v("TODO other walls models"); // TODO
        }
      }
      ret = stl.exportBinary( dos, export.mSplays, export.mWalls, export.mSurface );
    }
    if ( dos != null ) {
      try {
        dos.flush();
        dos.close();
      } catch ( IOException e ) { }
    }
    return ret;
  }

  public boolean exportModelAscii( int type, BufferedWriter osw, ExportData export ) // , boolean overwrite )
  { 
    boolean ret = false;
    if ( type == ModelType.SERIAL ) { // serialization
      // TDLog.v("3D export serial");
      ret = writeWalls( osw );
    } else {                          // model export 
      if ( type == ModelType.KML_ASCII ) { // KML export ASCII
        // TDLog.v("3D export KML");
        ExportKML kml = new ExportKML();
        if ( export.mWalls ) {
          if ( convexhullcomputer != null ) {
            for( CWConvexHull cw : convexhullcomputer.getWalls() ) {
              synchronized( cw ) {
                for ( CWTriangle f : cw.mFace ) kml.add( f );
              }
            }
          } else if ( powercrustcomputer != null && powercrustcomputer.hasTriangles() ) {
            kml.mTriangles = powercrustcomputer.getTriangles();
          }
        }
        ret = kml.exportASCII( osw, this, export.mSplays, export.mWalls, export.mSurface );
      } else if ( type == ModelType.CGAL_ASCII ) { // CGAL export: only stations and splay-points
        // TDLog.v("3D export CGAL");
        ret = (new ExportCGAL()).exportASCII( osw, this, export.mSplays, export.mWalls, export.mSurface );
      } else if ( type == ModelType.DXF_ASCII ) { // DXF
        // TDLog.v("3D export DXF");
        ExportDXF dxf = new ExportDXF();
        if ( export.mWalls ) {
          if ( convexhullcomputer != null ) {
            for ( CWConvexHull cw : convexhullcomputer.getWalls() ) {
              synchronized( cw ) {
                for ( CWTriangle f : cw.mFace ) dxf.add( f );
              }
            }
          } else if ( powercrustcomputer != null && powercrustcomputer.hasTriangles() ) {
            TDToast.makeWarn (R.string.powercrust_dxf_not_supported );
            return false;
          }
        }
        ret = dxf.exportASCII( osw, this, true, export.mSplays, export.mWalls, true ); // true = version13
      // } else if ( type == ModelType.SHP_ASCII ) { // SHP
      //   ExportSHP shp = new ExportSHP();
      //   if ( export.mWalls ) {
      //     if ( convexhullcomputer != null ) {
      //       for ( CWConvexHull cw : convexhullcomputer.getWalls() ) {
      //         synchronized( cw ) {
      //           for ( CWTriangle f : cw.mFace ) shp.add( f );
      //         }
      //       }
      //     } 
      //     if ( powercrustcomputer != null && powercrustcomputer.hasTriangles() ) {
      //       shp.mTriangles = powercrustcomputer.getTriangles();
      //       shp.mVertex    = powercrustcomputer.getVertices();
      //     }
      //   }
      //   ret = shp.exportASCII( data_output_stream?, filepath?, export.mName, this, true, export.mSplays, export.mWalls );
      } else if ( type == ModelType.STL_ASCII ) {
        // TDLog.v("3D export STL");
        ExportSTL stl = new ExportSTL();
        if ( export.mWalls ) {
          if ( convexhullcomputer != null ) {
            for ( CWConvexHull cw : convexhullcomputer.getWalls() ) {
              synchronized( cw ) {
                for ( CWTriangle f : cw.mFace ) stl.add( f );
              }
            }
          } else if ( powercrustcomputer != null && powercrustcomputer.hasTriangles() ) {
            stl.mTriangles = powercrustcomputer.getTriangles();
            stl.mVertex    = powercrustcomputer.getVertices();
          }
        }
        ret = stl.exportASCII( osw, export.mSplays, export.mWalls, export.mSurface );
      }
    }
    if ( osw != null ) {
      try {
        osw.flush();
        osw.close();
      } catch ( IOException e ) { }
    }
    return ret;
  }

  private boolean writeWalls( BufferedWriter fw )
  {
    // if ( Cave3D.mWallConvexHull ) {
    boolean ret = false;
    if ( convexhullcomputer != null ) {
      PrintWriter out = new PrintWriter( fw );
      out.format(Locale.US, "E %d %d\n", convexhullcomputer.getWallsSize(), convexhullcomputer.getBordersSize() );
      for ( CWConvexHull wall : convexhullcomputer.getWalls() ) {
        wall.writeHull( out );
      }
      for ( CWBorder border : convexhullcomputer.getBorders() ) {
        border.writeBorder( out );
      }
      ret = true;
    } else if ( powercrustcomputer != null && powercrustcomputer.hasTriangles() ) {
      // TODO
    }
    return ret;
  }

  // private boolean checkFile( String pathname, boolean overwrite )
  // {
  //   // TDLog.v( "Check file " + pathname + " overwrite " + overwrite );
  //   if ( (new File(pathname)).exists() && ! overwrite ) {
  //     TDToast.makeWarn( TDInstance.formatString( R.string.warning_not_overwrite, pathname ) );
  //     return false;
  //   }
  //   return true;
  // }

  protected double getGridSize()
  {
    double dx = emax - emin;
    double dy = nmax - nmin;
    double d = Math.sqrt( dx*dx + dy*dy );
    double grid_size = d / 10;
    if ( grid_size > 50 ) { grid_size = 50; }
    else if ( grid_size > 20 ) { grid_size = 20; }
    else if ( grid_size > 10 ) { grid_size = 10; }
    else if ( grid_size >  5 ) { grid_size =  5; }
    else if ( grid_size >  2 ) { grid_size =  2; }
    else if ( grid_size >  1 ) { grid_size =  1; }
    else if ( grid_size > 0.5f ) { grid_size = 0.5f; }
    else if ( grid_size > 0.2f ) { grid_size = 0.2f; }
    else if ( grid_size > 0.1f ) { grid_size = 0.1f; }
    return grid_size;
  }

  // ------------------------ 3D MODEL: CONVEX HULL
  public void makeConvexHull( )
  {
    if ( shots == null ) return;
    if ( WALL_CW < WALL_MAX /* && Cave3D.mWallConvexHull */ ) {
      convexhullcomputer = new ConvexHullComputer( this, shots );
      if ( convexhullcomputer != null ) {
        (new AsyncTask<Void, Void, Boolean>() {
          public Boolean doInBackground( Void ... v ) {
            return convexhullcomputer.computeWalls( );
          }
          public void onPostExecute( Boolean b )
          {
            if ( ! b ) convexhullcomputer = null;
            if ( mApp != null ) mApp.notifyWall( WALL_CW, b );
          }
        }).execute();
      } else {
        // TDToast.makeBad( "failed to create convex hull object" );
      }
    }
    // TDToast.make( "computing convex hull walls" );
  }

  // FIXME skip -------------------------- WALL_TUBE triangles
  public void makeTube( )
  {
    if ( shots == null ) return;
    if ( WALL_TUBE < WALL_MAX ) {
      tubecomputer = new TubeComputer( this, shots );
      if ( tubecomputer != null ) {
        (new AsyncTask< Void, Void, Boolean >() {
          public Boolean doInBackground( Void ... v ) {
            return tubecomputer.computeWalls();
          }
          public void onPostExecute( Boolean b ) {
            if ( ! b ) tubecomputer = null;
            if ( mApp != null ) mApp.notifyWall( WALL_TUBE, b );
          }
        }).execute();
      } else {
        // TDToast.makeBad( "failed to create hull object" );
      }
    }
  }


  // FIXME skip -------------------------- WALL_BUBBLE triangles
  public void makeBubble( )
  {
    if ( shots == null ) {
      TDLog.Error("make bubble - no shots");
      return;
    }
    // TDLog.v("make bubble");
    if ( WALL_BUBBLE < WALL_MAX ) {
      bubblecomputer = new BubbleComputer( this );
      if ( bubblecomputer != null ) {
        // TDLog.v("compute bubble");
        (new AsyncTask< Void, Void, Boolean >() {
          public Boolean doInBackground( Void ... v ) {
            return bubblecomputer.computeWalls();
          }
          public void onPostExecute( Boolean b ) {
            // TDLog.v("compute bubble: " + b );
            if ( ! b ) bubblecomputer = null;
            if ( mApp != null ) mApp.notifyWall( WALL_BUBBLE, b );
          }
        }).execute();
      } else {
        // TDToast.makeBad( "failed to create hull object" );
      }
    }
  }

  // FIXME skip -------------------------- WALL_HULL triangles
  public void makeHull( )
  {
    if ( shots == null ) return;
    if ( WALL_HULL < WALL_MAX ) {
      hullcomputer = new HullComputer( this, shots );
      if ( hullcomputer != null ) {
        (new AsyncTask< Void, Void, Boolean >() {
          public Boolean doInBackground( Void ... v ) {
            return hullcomputer.computeWalls();
          }
          public void onPostExecute( Boolean b ) {
            if ( ! b ) hullcomputer = null;
            if ( mApp != null ) mApp.notifyWall( WALL_HULL, b );
          }
        }).execute();
      } else {
        // TDToast.makeBad( "failed to create hull object" );
      }
    }
  }
  

  /* // FIXME skip ------------------------- WALL_DELAUNAY triangles
  public void makeDelaunay()
  {
    triangles_delaunay = null;
    if ( WALL_DELAUNAY < WALL_MAX ) {
      triangles_delaunay = new ArrayList< Triangle3D >();
      for ( Cave3DStation st : stations ) {
        ArrayList< Cave3DShot > station_splays = .getSplayAt( st, false );
        int ns = station_splays.size();
        
        if ( ns >= 4 ) {
          Vector3D[] vec = new Vector3D[ns]; // vector of the splays at station "st"
          for ( int n=0; n<ns; ++n ) {
            Cave3DShot sh = station_splays.get( n );
            double h = sh.len * Math.cos( sh.cln );
            vec[n] = new Vector3D( h * Math.sin(sh.ber), h * Math.cos(sh.ber), sh.len * Math.sin(sh.cln) );
          }
          Cave3DDelaunay delaunay = new Cave3DDelaunay( vec );
          delaunay.insertTrianglesIn( triangles_delaunay, st );
        }
      }
    }
  }
  */


  // ------------------------ 3D MODEL: POWERCRUST
  public void makePowercrust( )
  {
    if ( shots == null ) return;
    if ( WALL_POWERCRUST < WALL_MAX /* && Cave3D.mWallPowercrust */ ) {
      powercrustcomputer = new PowercrustComputer( this, stations, shots );
      (new AsyncTask<Void, Void, Boolean>() {
          public Boolean doInBackground( Void ... v ) {
            return powercrustcomputer.computePowercrust( );
          }

          public void onPostExecute( Boolean b )
          {
            if ( ! b ) powercrustcomputer = null;
            if ( mApp != null ) mApp.notifyWall( WALL_POWERCRUST, b );
          }
      }).execute();
    }
  }

  /* unused
  public boolean serialize( String filepath )
  {
    // TDLog.v("Parser serialize " + filepath );
    try { 
      FileOutputStream fos = Cave3DFile.getFileOutputStream( filepath );
      BufferedOutputStream bos = new BufferedOutputStream ( fos );
      DataOutputStream dos = new DataOutputStream( bos );
      dos.write('V'); // version
      dos.writeInt( TopoGL.VERSION_CODE );
      dos.write('P'); // parser
      serialize( dos );
      dos.close();
      fos.close();
    } catch ( FileNotFoundException e ) {
      TDLog.Error("Export Data file: " + e.getMessage() );
      return false;
    } catch ( IOException e ) {
      TDLog.Error("Export Data i/o: " + e.getMessage() );
      return false;
    }
    return true;
  }
  */

  /* unused
  public boolean deserialize( String filepath )
  {
    // TDLog.v("Parser deserialize " + filepath );
    try { 
      FileInputStream fis = Cave3DFile.getFileInputStream( filepath );
      BufferedInputStream bis = new BufferedInputStream ( fis );
      DataInputStream dis = new DataInputStream( bis );
      int what dis.read(); // 'V' version
      int version = dis.readInt( );
      what = dis.read(); // 'P' parser
      deserialize( dis, version );
      dis.close();
      fis.close();
    } catch ( FileNotFoundException e ) {
      TDLog.Error("Import Data file: " + e.getMessage() );
      return false;
    } catch ( IOException e ) {
      TDLog.Error("Import Data i/o: " + e.getMessage() );
      return false;
    }
    return true;
  }
  */

  /** add a fix. The following fix fields are assumed initialized:
   *   name, longitude, latitude, altitude 
   * @param fix   tix to add
   */
  public void addFix( Cave3DFix fix ) { fixes.add( fix ); }

  /** @return true if the model has at least one fix
   */
  public boolean hasFixes() { return fixes != null && fixes.size() > 0; }

  /** add a shot. The following shot fields are assumed initialized:
   *     surveyId, from, to, len, ber, cln, flags, millis
   * @note The shot stations are set by this method
   * @param shot  shot to add
   */
  public void addShot( Cave3DShot shot ) 
  { 
    Cave3DStation fstation = getStation( shot.from );
    shot.setFromStation( fstation );
    if ( shot.to == null || shot.to.length() == 0 ) {
      splays.add( shot );
      shot.to = "";
      shot.setToStation( null );
    } else {
      shots.add( shot );
      Cave3DStation tstation = getStation( shot.to );
      shot.setToStation( tstation );
    }
  }


  // must be written in this order because deserialization creates the connections
  public void serialize( DataOutputStream dos ) throws IOException
  {
    // TDLog.v("serialize: surveys " + surveys.size() + " " + stations.size() + " " + shots.size() + " " + splays.size() );
    dos.write('C');
    dos.writeInt( surveys.size() );
    for ( Cave3DSurvey survey : surveys ) survey.serialize( dos );
    dos.write('P'); // POINT
    dos.writeInt( stations.size() );
    for ( Cave3DStation station : stations ) station.serialize( dos );
    dos.write('L'); // LEG
    dos.writeInt( shots.size() );
    for ( Cave3DShot shot : shots ) shot.serialize( dos );
    dos.write('S'); // SPLAY
    dos.writeInt( splays.size() );
    for ( Cave3DShot splay : splays ) splay.serialize( dos );
    dos.write('F'); // FIX
    dos.writeInt( fixes.size() );
    for ( Cave3DFix fix : fixes ) fix.serialize( dos );
    dos.write('X');
    dos.writeInt( xsections.size() );
    for ( Cave3DXSection xsection : xsections ) xsection.serialize( dos );
    dos.write('E');
  }

  public void deserialize( DataInputStream dis, int version ) throws IOException
  {
    int what = 0;
    boolean done = false;
    while ( ! done ) {
      int nr = 0;
      what = dis.read();
      switch ( what ) {
        case 'C':
          nr = dis.readInt( );
          surveys.clear();
          for ( int k=0; k<nr; ++k ) surveys.add( Cave3DSurvey.deserialize( dis, version ) );
          break;
        case 'P':
          nr = dis.readInt( );
          stations.clear();
          for ( int k=0; k<nr; ++k ) {
            Cave3DStation st = Cave3DStation.deserialize( dis, version );
            stations.add( st );
            Cave3DSurvey survey = getSurvey( st.mSid );
            survey.addStation( st );
          }
          break;
        case 'L':
          nr = dis.readInt( );
          shots.clear();
          for ( int k=0; k<nr; ++k ) {
            Cave3DShot shot = Cave3DShot.deserialize( dis, version );
            shots.add( shot );
            Cave3DSurvey survey = getSurvey( shot.mSurveyId );
            survey.addShot( shot );
            Cave3DStation st = getStation( shot.from );
            if ( st != null ) shot.setFromStation( st );
            st = getStation( shot.to );
            if ( st != null ) shot.setToStation( st );
          }
          break;
        case 'S':
          nr = dis.readInt( );
          splays.clear();
          for ( int k=0; k<nr; ++k ) {
            Cave3DShot splay = Cave3DShot.deserialize( dis, version );
            splays.add( splay );
            Cave3DSurvey survey = getSurvey( splay.mSurveyId );
            survey.addSplay( splay );
            Cave3DStation st = getStation( splay.from );
            if ( st != null ) splay.setFromStation( st );
            st = getStation( splay.to );
            if ( st != null ) splay.setToStation( st );
          }
          break;
        case 'F':
          nr = dis.readInt( );
          fixes.clear();
          for ( int k=0; k<nr; ++k ) {
            Cave3DFix fix = Cave3DFix.deserialize( dis, version );
            fixes.add( fix );
          }
          break;
        case 'X':
          nr = dis.readInt( );
          xsections.clear();
          for ( int k=0; k<nr; ++k ) xsections.add( Cave3DXSection.deserialize( dis, version, stations ) );
          break;
        default:
          done = true;
          break;
      }
    }
  }   

  static protected BufferedReader getBufferedReader( InputStreamReader isr, String filename )
  {
    try {
      if ( isr == null ) {
        isr = new InputStreamReader( new FileInputStream( filename ) );
      }
      return new BufferedReader( isr );
    } catch ( FileNotFoundException e ) {
    }
    return null;
  }

  static protected String extractName( String filename )
  {
    int pos = filename.lastIndexOf( '/' );
    if ( pos < 0 ) { pos = 0; } else { ++pos; }
    int ext = filename.lastIndexOf( '.' ); if ( ext < 0 ) ext = filename.length();
    return filename.substring( pos, ext );
  }

  private double temperature_weight( double len, double cln )
  {
    double h = len * Math.sin( cln );
    return 1.0 / (0.1 + Math.abs(h) );
  }

  /** compute the stations temperature, by relaxing from the temperature at a set of stations
   *  @param filename   pathname of file with (station_name, temp) pairs
   *  called by TopoGL when a temperature file is read
   */
  // TEMPERATURE
  // boolean computeTemperature( InputStreamReader isr, String filename )
  // {
  //   has_temperature = false; 
  //   int sz = stations.size();
  //   if ( sz <= 0 ) return false;
  //   boolean[] kfix = new boolean[ sz ];

  //   for ( int k = 0; k < sz; ++k ) {
  //     kfix[ k ] = false;
  //     stations.get( k ).temp = -1000;
  //     // TDLog.v("station " + stations.get( k ).getName() );
  //   }
 
  //   double sum = 0;
  //   int nr_fix = 0;
  //   try {
  //     TDLog.v("filename: " + filename );
  //     BufferedReader br = getBufferedReader( isr, filename );
  //     String line;
  //     while ( (line = br.readLine() ) != null ) {
  //       String[] vals = line.trim().split(" ");
  //       // fullname = vals[0]   temp = vals[1]
  //       int idx = getStationIndex( vals[0] );
  //       if ( idx < 0 ) {
  //         TDLog.v("missing station " + vals[0] );
  //       } else {
  //         kfix[idx] = true;
  //         double t = Double.parseDouble( vals[1] );
  //         stations.get( idx ).temp = t;
  //         sum += t;
  //         nr_fix ++;
  //       }
  //     }
  //   } catch ( IOException e ) {
  //     return false;
  //   }
  //   if ( nr_fix < 2 ) return false;
  //   double t_ave = sum / nr_fix;
  //   for ( Cave3DStation st : stations ) {
  //     if ( st.temp < -999 ) st.temp = t_ave;
  //   }
  //        
  //   double delta = 0;
  //   int iter = 0;
  //   do {
  //     delta = 0;
  //     for ( int k = 0; k < sz; ++k ) {
  //       Cave3DStation s1 = stations.get( k );
  //       if ( ! kfix[k] ) {
  //         double tn = 0;
  //         double td = 0;
  //         for ( Cave3DShot leg : getLegsAtFrom( s1 ) ) {
  //           Cave3DStation s2 = leg.to_station;
  //           double w = temperature_weight( leg.len, leg.cln ); 
  //           // TDLog.v("t." + s2.name + " " + s2.temp + " " + w );
  //           tn += (s2.temp - s1.temp) * w;
  //           td += w;
  //         }
  //         for ( Cave3DShot leg : getLegsAtTo( s1 ) ) {
  //           Cave3DStation s2 = leg.from_station;
  //           double w = temperature_weight( leg.len, - leg.cln ); 
  //           // TDLog.v("f." + s2.name + " " + s2.temp + " " + w );
  //           tn += (s2.temp - s1.temp) * w;
  //           td += w;
  //         }
  //         if ( td > 0 ) {
  //           double dt = tn / td;
  //           if ( Math.abs(dt) > delta ) delta = Math.abs(dt);
  //           s1.temp += dt;
  //           // TDLog.v("set " + s1.name + " " + s1.temp );
  //         }
  //       // } else {
  //         // TDLog.v("Fix " + s1.name + " " + s1.temp );
  //       }
  //     }
  //     ++ iter;
  //     if ( iter % 10 == 0 ) TDLog.v("Temp-relax " + iter + " delta " + delta );
  //   } while ( delta > 0.01 && iter < 1000 );
  //   TDLog.v("Temp-relax final delta " + delta + " iter " + iter );
  //   has_temperature = true;
  //   return true;
  // }

}
