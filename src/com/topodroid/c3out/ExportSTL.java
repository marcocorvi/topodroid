/** @file ExportSTL.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief Walls STL exporter
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3out;

// import com.topodroid.TDX.TopoGL;
import com.topodroid.TDX.Triangle3D;
import com.topodroid.TDX.Vector3D;
import com.topodroid.c3walls.cw.CWFacet;
import com.topodroid.c3walls.cw.CWPoint;

import com.topodroid.utils.TDLog;

import java.util.ArrayList;
import java.util.Locale;

import java.io.BufferedWriter;
import java.io.PrintWriter;
// import java.io.PrintStream;
import java.io.DataOutputStream;
import java.io.BufferedOutputStream;
// import java.io.FileNotFoundException;
import java.io.IOException;

public class ExportSTL
{
  ArrayList<CWFacet> mFacets;

  public ArrayList< Triangle3D > mTriangles; // powercrust triangles
  public Vector3D[] mVertex; // triangle vertices
  Vector3D mMin;
  Vector3D mMax;
  double x, y, z; // offset to have positive coords values
  double s;       // scale factor

  public ExportSTL()
  {
    mFacets = new ArrayList< CWFacet >();
    mTriangles = null;
    mVertex    = null; 
    resetMinMax();
  }

  private void resetMinMax()
  {
    x = 0;
    y = 0;
    z = 0;
    s = 1.0f;
    mMin = new Vector3D();
    mMax = new Vector3D();
  }

  public void add( CWFacet facet ) { mFacets.add( facet ); }

  public void add( CWPoint v1, CWPoint v2, CWPoint v3 )
  {
     mFacets.add( new CWFacet( v1, v2, v3 ) );
  }

  private void makePositiveCoords()
  { 
    resetMinMax();
    for ( CWFacet facet : mFacets ) {
      facet.v1.minMax( mMin, mMax );
      facet.v2.minMax( mMin, mMax );
      facet.v3.minMax( mMin, mMax );
    }
    if ( mVertex != null ) {
      int len = mVertex.length;
      for ( int k=0; k<len; ++k ) {
        mVertex[k].minMax( mMin, mMax );
      }
    }
    x = - mMin.x;
    y = - mMin.y;
    z = - mMin.z;
    // s = mMax.x - mMin.x;
    // if ( s < mMax.y - mMin.y) s = mMax.y - mMin.y;
    // if ( s < mMax.z - mMin.z) s = mMax.z - mMin.z;
    // s = 1.0f/s;
    s = 1;
  }
  
  // splays, walls, surface   NOT USED
  public boolean exportASCII( BufferedWriter osw, boolean splays, boolean walls, boolean surface )
  {
    makePositiveCoords();
    String name = "Cave3D";
    boolean ret = true;
    PrintWriter pw = new PrintWriter( osw );
    pw.format(Locale.US, "solid %s\n", name );
    for ( CWFacet facet : mFacets ) {
      pw.format(Locale.US, "  facet normal %.3f %.3f %.3f\n", facet.un.x, facet.un.y, facet.un.z );
      pw.format(Locale.US, "    outer loop\n");
      pw.format(Locale.US, "      vertex %.3f %.3f %.3f\n", (x+facet.v1.x)*s, (y+facet.v1.y)*s, (z+facet.v1.z)*s );
      pw.format(Locale.US, "      vertex %.3f %.3f %.3f\n", (x+facet.v2.x)*s, (y+facet.v2.y)*s, (z+facet.v2.z)*s );
      pw.format(Locale.US, "      vertex %.3f %.3f %.3f\n", (x+facet.v3.x)*s, (y+facet.v3.y)*s, (z+facet.v3.z)*s );
      pw.format(Locale.US, "    endloop\n");
      pw.format(Locale.US, "  endfacet\n");
    }
    if ( mTriangles != null ) {
      for ( Triangle3D t : mTriangles ) {
        int size = t.size;
        Vector3D n = t.normal;
        pw.format(Locale.US, "  facet normal %.3f %.3f %.3f\n", n.x, n.y, n.z );
        pw.format(Locale.US, "    outer loop\n");
        for ( int k=0; k<size; ++k ) {
          Vector3D v = t.vertex[k];
          pw.format(Locale.US, "      vertex %.3f %.3f %.3f\n", (x+v.x)*s, (y+v.y)*s, (z+v.z)*s );
        }
        pw.format(Locale.US, "    endloop\n");
        pw.format(Locale.US, "  endfacet\n");
      }
    }
    pw.format(Locale.US, "endsolid %s\n", name );
    try {
      osw.close();
    } catch ( IOException e ) { // ???
      return false;
    }
    return ret;
  }

  private void intToByte( int i, byte[] b )
  {
    b[0] = (byte)(  i      & 0xff );
    b[1] = (byte)( (i>>8)  & 0xff );
    b[2] = (byte)( (i>>16) & 0xff );
    b[3] = (byte)( (i>>24) & 0xff );
  }

  private void floatToByte( double d, byte[] b )
  {
    float f = (float)d;
    int i = Float.floatToIntBits( f );
    b[0] = (byte)(  i      & 0xff );
    b[1] = (byte)( (i>>8)  & 0xff );
    b[2] = (byte)( (i>>16) & 0xff );
    b[3] = (byte)( (i>>24) & 0xff );
  }

  // int toIntLEndian( byte val[] ) 
  // {
  //   return val[0] | ( ((int)val[1]) << 8 ) | ( ((int)(val[2])) << 16 ) | ( ((int)(val[3])) << 24 );
  // }

  // float toFloatLEndian( byte val[] ) 
  // {
  //   return (float)( val[0] | ( ((int)val[1]) << 8 ) | ( ((int)(val[2])) << 16 ) | ( ((int)(val[3])) << 24 ) );
  // }

  // splays, walls, surface   NOT USED
  public boolean exportBinary( DataOutputStream dos, boolean splays, boolean walls, boolean surface ) 
  {
    makePositiveCoords();
    String name = "Cave3D";
    boolean ret = true;
    byte[] header = new byte[80];
    byte[] b4 = new byte[4];
    byte[] b2  = new byte[2];
    for (int k=0; k<80; ++k) header[k] = (byte)0;
    b2[0] = (byte)0;
    b2[1] = (byte)0;
    try {
      BufferedOutputStream bw = new BufferedOutputStream( dos ); 
      bw.write( header, 0, 80 );
      int sz = mFacets.size();
      if ( mTriangles != null ) sz += mTriangles.size();
      intToByte( sz, b4 ); bw.write( b4 );
      for ( CWFacet facet : mFacets ) {
        floatToByte(   facet.un.x, b4 ); bw.write( b4, 0, 4 );
        floatToByte(   facet.un.y, b4 ); bw.write( b4, 0, 4 );
        floatToByte(   facet.un.z, b4 ); bw.write( b4, 0, 4 );
        floatToByte( (x+facet.v1.x)*s, b4 ); bw.write( b4, 0, 4 );
        floatToByte( (y+facet.v1.y)*s, b4 ); bw.write( b4, 0, 4 );
        floatToByte( (z+facet.v1.z)*s, b4 ); bw.write( b4, 0, 4 );
        floatToByte( (x+facet.v2.x)*s, b4 ); bw.write( b4, 0, 4 );
        floatToByte( (y+facet.v2.y)*s, b4 ); bw.write( b4, 0, 4 );
        floatToByte( (z+facet.v2.z)*s, b4 ); bw.write( b4, 0, 4 );
        floatToByte( (x+facet.v3.x)*s, b4 ); bw.write( b4, 0, 4 );
        floatToByte( (y+facet.v3.y)*s, b4 ); bw.write( b4, 0, 4 );
        floatToByte( (z+facet.v3.z)*s, b4 ); bw.write( b4, 0, 4 );
        bw.write( b2, 0, 2 );
      }
      if ( mTriangles != null ) {
        for ( Triangle3D t : mTriangles ) {
          int size = t.size;
          Vector3D n = t.normal;
          Vector3D v0 = t.vertex[0];
          Vector3D v1 = t.vertex[1];
          for ( int k=2; k<size; ++k ) {
            floatToByte( n.x, b4 ); bw.write( b4, 0, 4 );
            floatToByte( n.y, b4 ); bw.write( b4, 0, 4 );
            floatToByte( n.z, b4 ); bw.write( b4, 0, 4 );
            Vector3D v2 = t.vertex[k];
            floatToByte( (x+v0.x)*s, b4 ); bw.write( b4, 0, 4 );
            floatToByte( (y+v0.y)*s, b4 ); bw.write( b4, 0, 4 );
            floatToByte( (z+v0.z)*s, b4 ); bw.write( b4, 0, 4 );
            floatToByte( (x+v1.x)*s, b4 ); bw.write( b4, 0, 4 );
            floatToByte( (y+v1.y)*s, b4 ); bw.write( b4, 0, 4 );
            floatToByte( (z+v1.z)*s, b4 ); bw.write( b4, 0, 4 );
            floatToByte( (x+v2.x)*s, b4 ); bw.write( b4, 0, 4 );
            floatToByte( (y+v2.y)*s, b4 ); bw.write( b4, 0, 4 );
            floatToByte( (z+v2.z)*s, b4 ); bw.write( b4, 0, 4 );
            bw.write( b2, 0, 2 );
            v1 = v2;
          }
        }
      }
    } catch( IOException e ) {
      TDLog.Error( "STL IO error " + e.getMessage() );
      ret = false;
    } finally {
      try {
        dos.close();
      } catch ( IOException e ) {}
    }
    return ret;
  }

}

