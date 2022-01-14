/* @file SymbolPoint.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: point symbol
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDString;
// import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;
import com.topodroid.math.BezierCurve;
import com.topodroid.math.Point2D;
import com.topodroid.io.dxf.SymbolPointDxf;
// import com.topodroid.dxf.SymbolPointDxf;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Locale;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.Matrix;

public class SymbolPoint extends Symbol
{
  static final private float dxfScale = 0.05f; // 1 / 20 TopoDroid has 20 units = 1 m
  static final private float csxScale = 5.00f;
  static final private float csxdxfScale = csxScale * dxfScale;
  Paint  mPaint;
  Path   mPath;
  Path   mOrigPath; // PRIVATE
  String mName;
  private SymbolPointDxf mDxf;
  private String mSvg;
  private String mXvi;

  int mHasText;         // whether the point has a text (1), value (2), or none (0)
  boolean mOrientable; // PRIVATE
  double mOrientation;      // orientation [degrees]
  // SymbolPointBasic mPoint1; // basic point

  // boolean hasText() { return mHasText; }
  // double orientation() { return mOrientation; }

  /** @return true if the point is orientable
   */
  @Override public boolean isOrientable() { return mOrientable; }

  // @Override public boolean isEnabled() { return mEnabled; }
  // @Override public void setEnabled( boolean enabled ) { mEnabled = enabled; }
  // @Override public void toggleEnabled() { mEnabled = ! mEnabled; }

  /** set the point orientation angle
   * @param angle   orientation angle [degrees]
   * @return true if the orientation has been set
   */
  @Override
  public boolean setAngle( float angle )
  {
    if ( ! mOrientable ) return false;
    float a = angle - (float)mOrientation;
    if ( Math.abs(a) > 1 ) {
      rotateGradP( a );
      return true;
    }
    return false;
  }

  /** @return the point orientation angle [degrres]
   */
  @Override public int getAngle() { return (int)mOrientation; } 

  /** @return the point local name
   */
  @Override public String getName( ) { return mName; }
  // @Override public String getThName( ) { return mThName; } // same as in Symbol.java

  /** @return the point in DXVI format
   */
  public SymbolPointDxf getDxf() { return mDxf; }

  /** @return the point in XVI format
   */
  public String getSvg( ) { return mSvg; }

  /** @return the point in XVI format
   */
  public String getXvi( ) { return mXvi; }

  /** @return the point path
   */
  @Override public Path getPath( ) { return mPath; }

  /** @return the point original path
   */
  Path getOrigPath( ) { return mOrigPath; }
 
  /** @return the point paint
   */
  @Override public Paint getPaint( ) { return mPaint; }

  /** cstr
   * @param pathname file path
   * @param fname    file name (must coincide with Therion name)
   * @param locale   current locale
   * @param iso      ...
   */
  SymbolPoint( String pathname, String fname, String locale, String iso )
  {
    super( null, null, fname, Symbol.W2D_DETAIL_SYM );
    mOrientable = false;
    mHasText = 0;
    mOrientation = 0.0;
    readFile( pathname, locale, iso );
  }

  /** cstr
   * @param n1      name
   * @param tn1     Therion name
   * @param group   group
   * @param fname   file name
   * @param c1      color
   * @param path    path
   * @param orientable whether the point is orientable
   * @param level      symbol level
   * @param rt         ...
   */
  SymbolPoint( String n1, String tn1, String group, String fname, int c1, String path, boolean orientable, int level, int rt )
  {
    super( tn1, group, fname, rt );
    mName  = n1;
    mDxf   = null;
    mPaint = makePaint( c1, Paint.Style.STROKE ); // FIXME style
    makePointPath( path );
    mOrigPath = new Path( mPath );
    
    mOrientable = orientable;
    mHasText = 0;
    mOrientation = 0.0;
    mLevel = level;
  }

  /** cstr
   * @param n1      name
   * @param tn1     Therion name
   * @param group   group
   * @param fname   file name
   * @param c1      color
   * @param path    path
   * @param orientable whether the point is orientable
   * @param has_text   whether the point can have text (0 no, 1 text, 2 value)
   * @param level      symbol level
   * @param rt         ...
   */
  SymbolPoint( String n1, String tn1, String group, String fname, int c1, String path, boolean orientable, int has_text, int level, int rt )
  {
    super( tn1, group, fname, rt ); // FIXME fname
    mName  = n1;
    mDxf   = null;
    mPaint = makePaint( c1, Paint.Style.STROKE ); //FIXME style
    makePointPath( path );
    mOrigPath = new Path( mPath );

    mOrientable = orientable;
    mHasText = has_text;
    mOrientation = 0.0;
    mLevel = level;
  }

  /** rotate the orienttaion of the point symbol
   * @param a   rotation angle [degrees]
   */
  void rotateGradP( double a )
  {
    if ( mOrientable ) {
      mOrientation += a;
      if ( mOrientation > 360.0 ) mOrientation -= 360.0;
      if ( mOrientation < 0.0 )   mOrientation += 360.0;
      Matrix m = new Matrix();
      m.postRotate( (float)(a) );
      mPath.transform( m );
    }
  }

  /** reset the orienttaion of the point symbol
   */
  void resetOrientation()
  {
    if ( mOrientable && mOrientation != 0.0 ) {
      Matrix m = new Matrix();
      m.postRotate( (float)(-mOrientation) );
      mPath.transform( m );
      mOrientation = 0.0;
    }
  }

  /** create a symbol reading it from a file
   * @param pathname  file path
   * @param locale    locale
   * @param iso       ...
   *
   *  The file syntax is 
   *      symbol point
   *      name NAME
   *      th_name THERION_NAME
   *      has_text yes | NO
   *      has_value yes | NO
   *      orientation yes | NO
   *      color 0xHHHHHH_COLOR
   *      style fill | STROKE 
   *      path
   *        MULTILINE_PATH_STRING
   *      endpath
   *      endsymbol
   */
  private void readFile( String pathname, String locale, String iso )
  {
    // TDLog.v( "Symbol Point read File " + pathname + " locale " + locale );
 
    String name    = null;
    String th_name = null;
    String group   = null;
    int color      = 0;
    Paint.Style style = Paint.Style.STROKE;
    String path    = null;
    String options = null;
    int cnt   = 0;

    try {
      // FileReader fr = TDFile.getFileReader( pathname );
      // TDLog.Log( TDLog.LOG_IO, "read symbol point file <" + filename + ">" );
      FileInputStream fr = TDFile.getFileInputStream( pathname );
      BufferedReader br = new BufferedReader( new InputStreamReader( fr, iso ) );
      String line;
      line = br.readLine();
      boolean insymbol = false;
      while ( line != null ) {
        line = line.trim().replaceAll("\\s+", " ");
        String[] vals = line.split(" ");
        int s = vals.length;
        for (int k=0; k<s; ++k ) {
          if ( vals[k].startsWith( "#" ) ) break;
          if ( vals[k].length() == 0 ) continue; // not needed
          if ( ! insymbol ) {
            if ( vals[k].equals("symbol" ) ) {
              name = null;
              th_name = null;
              color = TDColor.TRANSPARENT;
              path = null;
              insymbol = true;
            }
          } else {
            if ( vals[k].equals("name") || vals[k].equals(locale) ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                // name = (new String( vals[k].getBytes(iso) )).replace("_", " ");
                name = vals[k].replace("_", " ");
                // TDLog.v( "set name " + name );
                // TDLog.v("POINT " + "name " + k + " / " + s + " " + name );
              }
            } else if ( vals[k].equals("th_name") ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                th_name = deprefix_u( vals[k] );
                // TDLog.v("POINT " + "symbol " + pathname + " th name " + k + " / " + s + " " + th_name );
              }
            } else if ( vals[k].equals("group") ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                group = vals[k];
              }
            } else if ( vals[k].equals("options") ) {
              StringBuilder sb = new StringBuilder();
              boolean space = false;
              for ( ++k; k < s; ++k ) {
                if ( vals[k].length() > 0 ) {
                  if ( space ) { sb.append(" "); } else { space = true; }
                  sb.append( vals[k] );
                }
              }
              options = sb.toString();
            } else if ( vals[k].equals("level") ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                try {
                  mLevel = ( Integer.parseInt( vals[k] ) );
                } catch( NumberFormatException e ) {
                  TDLog.Error("Non-integer level");
                }
              }
            } else if ( vals[k].equals("roundtrip") ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                try {
                  mRoundTrip = ( Integer.parseInt( vals[k] ) );
                } catch( NumberFormatException e ) {
                  TDLog.Error("Non-integer roundtrip");
                }
              }
            } else if ( vals[k].equals("orientation") ) {
              if ( cnt == 0 ) {
                ++k; while ( k < s && vals[k].length() == 0 ) ++k;
                if ( k < s ) {
                  mOrientable = ( vals[k].equals("yes") || vals[k].equals( TDString.ONE ) );
                }
              }
            } else if ( vals[k].equals("has_text") ) {
              if ( cnt == 0 ) {
                ++k; while ( k < s && vals[k].length() == 0 ) ++k;
                if ( k < s ) {
                  mHasText = ( vals[k].equals("yes") || vals[k].equals( TDString.ONE ) )? 1 : 0;
                  if ( vals[k].equals( TDString.TWO ) ) mHasText = 2;
                }
              }
            } else if ( vals[k].equals("has_value") ) {
              if ( cnt == 0 ) {
                ++k; while ( k < s && vals[k].length() == 0 ) ++k;
                if ( k < s ) {
                  mHasText = ( vals[k].equals("yes") || vals[k].equals( TDString.ONE ) )? 2 : 0;
                  if ( vals[k].equals( TDString.ONE ) ) mHasText = 1;
                }
              }
            } else if ( vals[k].equals("style") ) {
              if ( cnt == 0 ) {
                ++k; while ( k < s && vals[k].length() == 0 ) ++k;
                if ( k < s ) {
                  if ( vals[k].equals("fill") ) {
                  //  style = Paint.Style.FILL;
                  // } else if ( vals[k].equals("fill-stroke") ) {
                    style = Paint.Style.FILL_AND_STROKE;
                  }
                }
              }
            } else if ( vals[k].equals("color") ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                try {
                  color = Integer.decode( vals[k] ) | 0xff000000;
                } catch ( NumberFormatException e ) {
                  TDLog.Error("Non-integer color");
                }
              }
            } else if ( vals[k].equals("csurvey") ) {
              // ignore
            } else if ( vals[k].equals("path") ) {
              path = br.readLine();
              if ( path != null ) {
                while ( ( line = br.readLine() ) != null ) {
                  if ( line.startsWith( "endpath" ) ) break;
                  path = path + " " + line;
                }
              }
            } else if ( vals[k].equals("endsymbol") ) {
              if ( name == null ) {
                TDLog.Error("NULL name " + pathname );
              } else if ( th_name == null ) {
                TDLog.Error("NULL th_name " + pathname );
              } else if ( path == null ) {
                TDLog.Error("NULL path " + pathname);
              } else {
                if ( cnt == 0 ) {
                  mName   = name;
                  setThName( th_name );
                  mGroup  = group;
                  mPaint  = makePaint( color, style );
                  makePointPath( path );
                  mOrigPath = new Path( mPath );
                  mDefaultOptions = options;
                  // TDLog.v("POINT " + "Name " + mName + " ThName " + getThName() );
                  // mPoint1 = new SymbolPointBasic( name, th_name, null, fname, color, path );
                // } else if ( cnt == 1 ) {
                //   if ( mOrientable == true ) {
                //     // ERROR point1 is orientable
                //   } else {
                //     mPoint2 = new SymbolPointBasic( name, th_name, null, fname, color, path );
                //     mOrientable = true;
                //   }
                } else {
                  TDLog.Error("Only one point per file " + pathname);
                }
                ++ cnt;
              }
              insymbol = false;
            }
          }
        }
        line = br.readLine();
      }
    } catch ( FileNotFoundException e ) {// FIXME
    } catch( IOException e ) {// FIXME
    }
    mOrientation = 0.0;
  }

  // private void makePointPath()
  // {
  //   mPath = new Path();
  //   mPath.moveTo(0,0);
  //   String pname = "P_" + getThName().replace(':', '-');
  //   mDxf  = "  0\nLINE\n  8\n" + pname + "\n" 
  //         + "  100\nAcDbEntity\n  100\nAcDbLine\n"
  //         + "  10\n0.0\n  20\n0.0\n  30\n0.0\n"
  //         + "  11\n1.0\n  21\n0.0\n  31\n0.0\n"; // 1 mm long
  //   mSvg = "";
  //   mXvi = "";
  // }

  /** Make the path from its string description
   * @param path   path string description
   *
   * The path string description is composed of the following directives
   *     - "moveTo X Y"
   *     - "lineTo X Y"
   *     - "cubicTo X1 Y1 X2 Y2 X Y"
   *     - "addCircle X Y R"
   *     - "arcTo X1 Y1 X2 Y2 A0 DA"
   */
  private void makePointPath( String path )
  {
    mDxf = new SymbolPointDxf();
    if ( path == null ) {
      mPath = new Path();
      mPath.moveTo(0,0);
      String pname = "P_" + getThName().replace(':', '-');

      mDxf.line( pname, 0, 0, 1, 0 );

      mSvg = "";
      mXvi = "";
      return;
    }
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter( sw ); // DXF writer
    StringWriter sv1 = new StringWriter();
    PrintWriter pv1  = new PrintWriter( sv1 ); // SVG path writer
    // StringWriter sv2 = new StringWriter();
    // PrintWriter pv2  = new PrintWriter( sv2 ); // CSX circle writer
    StringWriter sv3 = new StringWriter();
    PrintWriter pv3  = new PrintWriter( sv3 ); // SVG circle writer
    StringWriter sv4 = new StringWriter();
    PrintWriter pv4  = new PrintWriter( sv4 ); // XVI writer: Lines / Cubics

    float x00=0, y00=0;  // last drawn point1
    String pname = "P_" + getThName().replace(':', '-');

    float unit = TDSetting.mUnitIcons;
    mPath = new Path();
    String[] vals = path.split(" ");
    int s = vals.length;
    for ( int k = 0; k<s; ++k ) {
      float x0=0, y0=0, x1=0, y1=0, x2=0, y2=0;
      if ( "moveTo".equals( vals[k] ) ) {
        try {
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) {
            y0 = Float.parseFloat( vals[k] );
            mPath.moveTo( x0*unit, y0*unit );
            x00 = x0 * dxfScale;
            y00 = y0 * dxfScale;

            pv1.format(Locale.US, "M %.2f %.2f ", x00*csxScale, y00*csxScale );
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( path + " parse moveTo error" );
        }
      } else if ( "lineTo".equals( vals[k] ) ) {      
        try {
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { 
            y0 = Float.parseFloat( vals[k] ); 
            mPath.lineTo( x0*unit, y0*unit );
            float x01 = x0 * dxfScale;
            float y01 = y0 * dxfScale;

            mDxf.line(pname, x00, -y00, x01, -y01 );

	    pv4.format(Locale.US, "L %.2f %.2f", x00, y00 );
	    pv4.format(Locale.US, " %.2f %.2f ", x01, y01 );
            
            x00 = x01;
            y00 = y01;
            
            pv1.format(Locale.US, "L %.2f %.2f ", x00*csxScale, y00*csxScale );
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( path + " parse lineTo error" );
        }
      } else if ( "cubicTo".equals( vals[k] ) ) {
        // cp1x cp1y cp2x cp2y p2x p2y
        try {
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;  // CP1
          if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { y0 = Float.parseFloat( vals[k] ); }

          ++k; while ( k < s && vals[k].length() == 0 ) ++k;  // CP2
          if ( k < s ) { x1 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { y1 = Float.parseFloat( vals[k] ); }

          ++k; while ( k < s && vals[k].length() == 0 ) ++k;  // P2
          if ( k < s ) { x2 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { 
            y2 = Float.parseFloat( vals[k] ); 
            mPath.cubicTo( x0*unit, y0*unit, x1*unit, y1*unit, x2*unit, y2*unit );
	    pv4.format(Locale.US, "C %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f ", x00, y00, x0*dxfScale, y0*dxfScale, x1*dxfScale, y1*dxfScale, x2*dxfScale, y2*dxfScale );

           
            // cubic to 8-segment polyline
         //    DrawingDxf.printString( pw, 0, "POLYLINE" );
         //    // handle = DrawingDxf.inc(handle);
         //    DrawingDxf.printString( pw, 8, pname );
         //    DrawingDxf.printAcDb( pw, -1, "AcDbEntity", "AcDbLine" );
         //    // DrawingDxf.printInt(  pw, 39, 1 ); // line thickness
         //    // DrawingDxf.printInt(  pw, 40, 1 ); // start width
         //    // DrawingDxf.printInt(  pw, 41, 1 ); // end width
         //    DrawingDxf.printInt( pw, 66, 1 ); // group 1
         //    DrawingDxf.printInt( pw, 70, 8 + 0 ); // polyline flag 8 = 3D polyline, 1 = closed 
         //    DrawingDxf.printInt( pw, 75, 0 ); // 6 cubic spline, 5 quad spline, 0
         //    DrawingDxf.printXYZ( pw, 0.0f, 0.0f, 0.0f, 0 ); // position
         //    BezierCurve bc = new BezierCurve( x00, -y00, x0*dxfScale, -y0*dxfScale, x1*dxfScale, -y1*dxfScale, x2*dxfScale, -y2*dxfScale );
         //    DrawingDxf.printString( pw, 0, "VERTEX" ); 
         //    DrawingDxf.printString( pw, 8, pname ); // layer
         //    DrawingDxf.printXYZ( pw, x00, -y00, 0.0f, 0 );
         //    for ( int n=1; n < 8; ++n ) { //8 point
         //      Point2D pb = bc.evaluate( (float)n / (float)8 );
         //      // handle = DrawingDxf.printLinePoint( pw, scale, handle, pname, pb.x, pb.y );
         //      DrawingDxf.printString( pw, 0, "VERTEX" );
         //      DrawingDxf.printString( pw, 8, pname );
         //      DrawingDxf.printXYZ( pw, pb.x, pb.y, 0.0f, 0 );
         //    }
         //    // handle = DrawingDxf.printLinePoint( pw, scale, handle, pname, x2*dxfScale, -y2*dxfScale );
         //    DrawingDxf.printString( pw, 0, "VERTEX" );
         //    DrawingDxf.printString( pw, 8, pname );
         //    DrawingDxf.printXYZ( pw, x2*dxfScale, -y2*dxfScale, 0.0f, 0 ); 
         //    // x0 = x3;
         //    // y0 = y3;
         //    //pw.printf("  0%sSEQEND%s", DrawingDxf.EOL, DrawingDxf.EOL );
         //    DrawingDxf.printString( pw, 0, "SEQEND" );
         //    if ( TDSetting.mAcadVersion >= 13 ) {
         //        handle = DrawingDxf.inc(handle);
         //        DrawingDxf.printHex( pw, 5, handle );
         //    }
           int np = 8; // 8+1 points
           float[] xx = new float[ np+1 ];
           float[] yy = new float[ np+1 ];
           xx[0] =  x00;
           yy[0] = -y00;
           BezierCurve bc = new BezierCurve( x00, -y00, x0*dxfScale, -y0*dxfScale, x1*dxfScale, -y1*dxfScale, x2*dxfScale, -y2*dxfScale );
           for ( int n=1; n < np; ++n ) { 
             Point2D pb = bc.evaluate( (float)n / (float)np );
             // TDLog.v( "point " + n + " " + pb.x + " " + pb.y );
             xx[n] = pb.x;
             yy[n] = pb.y;
           }
           xx[np] =  x2*dxfScale;
           yy[np] = -y2*dxfScale;

           mDxf.polyline( pname, xx, yy );
      
            // x00 /= dxfScale; // not needed
            // y00 /= dxfScale;

            x00 = x2 * dxfScale;
            y00 = y2 * dxfScale;

            pv1.format(Locale.US, "C %.2f %.2f %.2f %.2f %.2f %.2f ",
               x0*csxdxfScale, y0*csxdxfScale, x1*csxdxfScale, y1*csxdxfScale, x2*csxdxfScale, y2*csxdxfScale );
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( path + " parse cubicTo error" );
        }
      } else if ( "addCircle".equals( vals[k] ) ) {
        try {
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }  // center X coord
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { y0 = Float.parseFloat( vals[k] ); }  // center Y coord
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) {
            x1 = Float.parseFloat( vals[k] );                 // radius
            mPath.addCircle( x0*unit, y0*unit, x1*unit, Path.Direction.CCW );

            mDxf.circle( pname, x0*dxfScale, -y0*dxfScale, x1*dxfScale );

	    pv4.format(Locale.US, "C %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f ", 
              (x0-x1)*dxfScale, y0*dxfScale, (x0-x1)*dxfScale, (y0-x1)*dxfScale, (x0+x1)*dxfScale, (y0-x1)*dxfScale, (x0+x1)*dxfScale, y0*dxfScale );
	    pv4.format(Locale.US, "C %.2f %.2f %.2f %.2f %.2f %.2f %.2f %.2f ", 
              (x0-x1)*dxfScale, y0*dxfScale, (x0-x1)*dxfScale, (y0+x1)*dxfScale, (x0+x1)*dxfScale, (y0+x1)*dxfScale, (x0+x1)*dxfScale, y0*dxfScale );

            // pv2.format(Locale.US,
            //   "&lt;circle cx=&quot;%.2f&quot; cy=&quot;%.2f&quot; r=&quot;%.2f&quot; /&gt;",
            //   x0*csxdxfScale, y0*csxdxfScale, x1*csxdxfScale );
            pv3.format(Locale.US,
              "<circle cx=\"%.2f\" cy=\"%.2f\" r=\"%.2f\" />",
              x0*csxdxfScale, y0*csxdxfScale, x1*csxdxfScale );
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( path + " parse circle error" );
        }
      } else if ( "arcTo".equals( vals[k] ) ) {
        // (x0,y0) top-left corner of rect
        // (x1,y1) bottom-right corner of rect
        // x2 start-angle [degrees]
        // y2 sweep angle (clockwise) [degrees]
        // 
        //    (x0,y0) +-----=-----+
        //            |     |     |
        //            |=====+=====| 0 angle (?)
        //            |     |     | | sweep direction
        //            +-----=-----+ V
        //
        try {
          ++k; while ( k < s && vals[k].length() == 0 ) ++k; // RECTANGLE first endpoint
          if ( k < s ) { x0 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { y0 = Float.parseFloat( vals[k] ); }

          ++k; while ( k < s && vals[k].length() == 0 ) ++k; // RECTANGLE second endpoint
          if ( k < s ) { x1 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { y1 = Float.parseFloat( vals[k] ); }

          ++k; while ( k < s && vals[k].length() == 0 ) ++k;  // FROM, TT angles [deg]
          if ( k < s ) { x2 = Float.parseFloat( vals[k] ); }
          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
          if ( k < s ) { 
            y2 = Float.parseFloat( vals[k] ); 
            mPath.arcTo( new RectF(x0*unit, y0*unit, x1*unit, y1*unit), x2, y2 );

            mDxf.arc( pname, (x0+x1)/2*dxfScale, -(y0+y1)/2*dxfScale, x1*dxfScale, x2, y2 );

	    // TODO arcTo for XVI

            float cx = (x1+x0)/2;
            float cy = (y1+y0)/2;
            float rx = (x1-x0)/2;
            float ry = (y1-y0)/2;
    
            float x0i = (cx + rx * TDMath.cosd( x2 ) )* dxfScale; // initial point
            float y0i = (cy + ry * TDMath.sind( x2 ) )* dxfScale;
            x00 = (cx + rx * TDMath.cosd( x2+y2 ) )* dxfScale;    // final point
            y00 = (cy + ry * TDMath.sind( x2+y2 ) )* dxfScale;
            
            // mode to (x00, y00)
            pv1.format(Locale.US, "M %.2f %.2f ", x0i*csxScale, y0i*csxScale );
            pv1.format(Locale.US, "A %.2f %.2f 0 1 %.2f %.2f ", rx*csxdxfScale, ry*csxdxfScale, x00*csxScale, y00*csxScale );
          }
        } catch ( NumberFormatException e ) {
          TDLog.Error( path + " parse arcTo error" );
        }
      }
    }
    mSvg = "<path d=\"" + sv1.getBuffer().toString() + "\"/> " + sv3.getBuffer().toString();
    mXvi = sv4.getBuffer().toString();
  }

  /** @return a paint
   * @param color   paint color
   * @param style   paint style
   */
  static Paint makePaint( int color, Paint.Style style )
  {
    Paint ret = new Paint();
    ret.setDither(true);
    ret.setColor( color );
    ret.setStyle( style );
    ret.setStrokeJoin(Paint.Join.ROUND);
    ret.setStrokeCap(Paint.Cap.ROUND);
    ret.setStrokeWidth( 1 );
    return ret;
  }
}
