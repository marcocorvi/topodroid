/** @flile Sketch3dInfo.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3D sketch: path types (points, lines, and areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * 20130220 created
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Locale;

import android.graphics.PointF;
import android.graphics.Path;

import android.util.FloatMath;
import android.util.Log;

class Sketch3dInfo extends SketchShot
{
  static final float mXScale = 1.0f;
  static final float DEG2RAD = (float)(Math.PI / 180);
  long   surveyId;
  long   id;
  String name;   // sketch name
  String start;  // start station (origin of ref. system, for NUM)
  // String st1;    // current station
  // String st2;    // forward station
  float  xoffset_top,  yoffset_top,  zoom_top;   // 2d scene offset, and zoom
  float  xoffset_side, yoffset_side, zoom_side;  // 2d scene offset, and zoom
  float  xoffset_3d,   yoffset_3d,   zoom_3d;    // 3d scene offset, and zoom
  float  east, south, vert; // 3d origin
  float  azimuth, clino;    // 3d view angles [deg]

  float xcenter;
  float ycenter;
  // private float sin_alpha; // for the SIDE view
  // private float cos_alpha;
  // private float sin_gamma;
  // private float cos_gamma; // for the TOP view
  private float shotBearing;  // shot azimuth
  private float shotClino;    // shot clino

  NumStation station1;  // station 1
  NumStation station2;  // station 2
  // NumShot    shot;      // current shot
  float  ne, ns, nv;    // unit vector in the direction of sight
  float  nxx, nxy, nxz; // unit vector of X-axis in the projection
  float  nyx, nyy, nyz; // unit vector of Y-axis in the projection
  private float de1, ds1, dv1; // difference: Station2 - Station1
  // private float dh1;
  // float  dvdh;               // ratio DV/DH
  // private float h0;
  private float x1, y1, z1;  // station1 - origin (world coords)
  private float x2, y2, z2;  // station2 - origin (world coords)
  private float ux, uy, uz;  // shot unit vector
  private PointF p1, p2;     // work points

  Sketch3dInfo()
  {
    super(null, null);
    p1 = new PointF();
    p2 = new PointF();
    start = "0";
  }

  Vector shotUnit()
  {
    return new Vector( ux, uy, uz );
  }

  Vector projectionOnShot( Vector v )
  {
    float vu = ux * v.x + uy * v.y + uz * v.z;
    return new Vector( ux * vu, uy * vu, uz * vu );
  }
  
  /** check if a triangle is forward
   */
  boolean isForward( SketchTriangle t )
  {
    return t.normal.x * ne + t.normal.y * ns + t.normal.z * nv > 0.0f;
  }

  
  void resetDirection()
  {
    azimuth = 0.0f;
    clino = 0.0f;
    setDirection();
  }

  /** reset the direction of the viewpoint
   * @param a   azimuth [degrees]
   * @param c   clino [degrees]
   */
  void resetDirection( float a, float c )
  {
    azimuth = a;
    clino   = c;
    setDirection();
  }

  /**
   * @param da   variation of azimuth
   * @param dc   variation of clino
   */
  void rotateBy3d( float da, float dc )
  { 
    worldToScene( x1, y1, z1, p1 );
    azimuth -= da;
    if ( azimuth > 360 ) azimuth -= 360;
    if ( azimuth < 0 ) azimuth += 360;
    dc += clino;
    if ( dc >  180 ) dc -= 360;
    if ( dc < -180 ) dc += 360;
    clino = dc;

    setDirection();

    worldToScene( x1, y1, z1, p2 );
    xoffset_3d += (p1.x - p2.x);
    yoffset_3d += (p1.y - p2.y);
  }

  /** compute the triplet of unit vectors
   *           ^
         -vert |
               |   ,(sa*cc, -ca*cc, -sc)_esv = -No
Nx=(-ca,-sa,0) | ,' 
              \|_________ east
              / \
       south /   \ Ny = No ^ Nx
   */
  void setDirection()
  {
    float cc = FloatMath.cos( clino * DEG2RAD ); // cos and sin of clino and azimuth
    float sc = FloatMath.sin( clino * DEG2RAD );
    float ca = FloatMath.cos( (azimuth+180) * DEG2RAD );
    float sa = FloatMath.sin( (azimuth+180) * DEG2RAD );
    ne = - sa * cc;
    ns =   ca * cc;
    nv =   sc;
    nxx = - ca;
    nxy = - sa;
    nxz = 0;
    nyx =   sa * sc;
    nyy = - ca * sc;
    nyz =   cc;
  }

  String getDirectionString()
  {
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, "%.0f %.0f", azimuth, clino ); // can skip Locale
    return sw.getBuffer().toString();
  }

  String getShotString()
  { 
    StringWriter sw = new StringWriter();
    PrintWriter  pw = new PrintWriter( sw );
    pw.format(Locale.ENGLISH, "%s-%s %.0f %.0f", st1, st2, shotBearing, shotClino ); // can skip Locale
    return sw.getBuffer().toString();
  }
  
  void setStations( NumStation s1, NumStation s2, DistoXDBlock blk, boolean set_origin )
  {
    if ( set_origin ) {
      east  = s1.e;
      south = s1.s;
      vert  = s1.v;
      xoffset_top  = xcenter;
      yoffset_top  = ycenter;
      xoffset_side = xcenter;
      yoffset_side = ycenter;
      xoffset_3d   = xcenter;
      yoffset_3d   = ycenter;
    } else {
      /* anything to do ? */
    }
    if ( blk != null ) {
      shotBearing = blk.mBearing;
      shotClino   = blk.mClino;
    } else {
      shotBearing = 0f;
      shotClino   = 0f;
    }

    st1 = s1.name;
    st2 = s2.name;
    station1 = s1;
    station2 = s2;
    de1 = station2.e - station1.e;
    ds1 = station2.s - station1.s;
    dv1 = station2.v - station1.v;
    float dh1 = FloatMath.sqrt( de1*de1 + ds1*ds1 );
    if ( dh1 < 0.01f ) dh1 += 0.01f; // regularize by adding 1 cm
    // sin_alpha = de1/dh1;
    // cos_alpha = ds1/dh1;
    // dvdh = dv1 / dh1;
    // len is guaranteed non-zero (since dh1 >= 0.01)
    float len = FloatMath.sqrt( dh1*dh1 + dv1*dv1 );
    // sin_gamma = dv1 / len; // == uz
    // cos_gamma = dh1 / len;
    azimuth = 0.0f;
    clino   = 0.0f;
    x1 = (station1.e - east);
    y1 = (station1.s - south);
    z1 = (station1.v - vert);
    x2 = (station2.e - east);
    y2 = (station2.s - south);
    z2 = (station2.v - vert);

    ux = de1 / len;
    uy = ds1 / len;
    uz = dv1 / len;

    // float det = 1.0f/(ds1*ds1 + de1*de1);
    // float a1 = station1.e * ds1 - station1.s * de1;
    // h0 = (station1.e-east)*sin_alpha + (station1.s-south)*cos_alpha;
  }

  float distance3d( Vector v )
  {
    float c = v.x * ux + v.y * uy + v.z * uz;
    float x = v.x - ux * c;
    float y = v.y - uy * c;
    float z = v.z - uz * c;
    return FloatMath.sqrt( x*x + y*y + z*z );
  }


  /** 
   * @param x    X scene coord
   * @param y    Y scene coord
   *
   * (x1,y1,z1) = station1 - origin
   * (x2,y2,z2) = station2 - origin
   *
   * (xx1,yy1) station1 scene coords
   */
  float sceneProjOnShot( float x, float y, Vector v )
  {
    float ret = -2.0f; // line abscissa: 0 at station1, 1 at station2
    float xx1 = nxx * x1 + nxy * y1 + nxz * z1;
    float yy1 = nyx * x1 + nyy * y1 + nyz * z1;
    float xx2 = nxx * x2 + nxy * y2 + nxz * z2;
    float yy2 = nyx * x2 + nyy * y2 + nyz * z2;
    float a = yy2 - yy1;
    float b = xx1 - xx2;
    float c = xx2 * yy1 - xx1 * yy2;
    float det = a*a + b*b;
    
    // distance form the line:
    float d = Math.abs( a * x + b * y + c )/FloatMath.sqrt(det); 
    xx2 -= xx1;
    yy2 -= yy1;
    if ( d > 0.1f * (Math.abs(xx2) + Math.abs(yy2)) ) return -2.0f;

    float xx0 = ( b*b*x - a*b*y - a*c)/det;
    float yy0 = (-a*b*x + a*a*y - b*c)/det;
    if ( Math.abs(xx2) > Math.abs(yy2) ) { // ratio on X
      // float xx0 = ( b*b*x - a*b*y - a*c)/det;
      ret = ( xx0  - xx1 ) / xx2;
    } else {
      // float yy0 = (-a*b*x + a*a*y - b*c)/det;
      ret = ( yy0 - yy1 ) / yy2;
    }
    v.x = station1.e + ret * de1;
    v.y = station1.s + ret * ds1;
    v.z = station1.v + ret * dv1;
    return ret;
  }

  /** 
   * subtract the origin from the vector
   * and compute the projection on the scene
   */
  float worldToSceneOrigin( Vector v, PointF p )
  {
    if ( v == null ) return 0f;
    return worldToSceneOrigin( v.x, v.y, v.z, p );
  }

  float worldToSceneOrigin( float x, float y, float z, PointF p ) 
  {
    x -= east;
    y -= south;
    z -= vert;
    p.x = nxx * x + nxy * y + nxz * z;
    p.y = nyx * x + nyy * y + nyz * z;
    return ne * x + ns * y + nv * z;
  }

  private void worldToScene( float x, float y, float z, PointF p ) 
  {
    p.x = nxx * x + nxy * y + nxz * z;
    p.y = nyx * x + nyy * y + nyz * z;
  }

  float canvasToSceneX( float x )
  { 
    return (x)/zoom_3d   - xoffset_3d;
  }

  float canvasToSceneY( float y )
  {
     return (y)/zoom_3d   - yoffset_3d;
  }

  float sceneToCanvasX( float x )
  {
    return (x+xoffset_3d)   * zoom_3d;
  }

  float sceneToCanvasYtop( float y )
  { 
    return   (y+yoffset_3d)   * zoom_3d;
  }

  Vector sceneToWorld( float x, float y )
  {
    return new Vector( nxx * x + nyx * y, nxy * x + nyy * y, nxz * x + nyz * y );
  }

  Vector sceneToWorld( LinePoint p ) { return sceneToWorld( p.mX, p.mY ); }

  Vector sceneToWorld( PointF p ) { return sceneToWorld( p.x, p.y ); }

  void shiftOffset3d( float x, float y )
  {
    xoffset_3d += x / zoom_3d;
    yoffset_3d += y / zoom_3d;
  }

  void changeZoom3d( float f )
  {
    float z = zoom_3d;
    zoom_3d *= f;
    z = 1/z - 1/zoom_3d;
    xoffset_3d -= xcenter * z;
    yoffset_3d -= ycenter * z;
  }

  void resetZoom3d( float x, float y, float z )
  {
    xoffset_3d = x/2;
    yoffset_3d = y/2;
    zoom_3d = z; 
  }

  /** the line point(s) are already in the scene reference frame
   */
  Vector projTo3d( LinePoint p )
  {
    return sceneToWorld( p );
  }
}
