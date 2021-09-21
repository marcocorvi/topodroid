/* @file DrawingStationPath.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid drawing: user-defined station point 
 *        type DRAWING_PATH_STATION
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */

package com.topodroid.Cave3X;

import com.topodroid.utils.TDLog;
// import com.topodroid.math.Point2D;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PointScale;

import android.graphics.Matrix;
import java.util.Locale;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * station points do not shift (!)
 */
public class DrawingStationPath extends DrawingPath
                         implements IDrawingLink
{
  // float mXpos, mYpos;         // X-Y station position (scene): use cx, cy
  private int mScale;         //! symbol scale
  private String mName;       // station name

  // FIXME-COPYPATH
  // @Override
  // DrawingPath copyPath()
  // {
  //   DrawingStationPath ret = new DrawingStationPath( mName, cx, cy, mScale );
  //   copyTo( ret );
  //   return ret;
  // }


  DrawingStationPath( String name, float x, float y, int scale, int scrap )
  {
    super( DrawingPath.DRAWING_PATH_STATION, null, scrap );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + mType + " X " + x + " Y " + y );
    // TDLog.v( "User Station (1) " + mType + " X " + x + " Y " + y );
    // mType = DRAWING_PATH_STATION;
    // mXpos = x;
    // mYpos = y;
    cx = x;
    cy = y;
    mName = (name == null)? "" : name;
    setBBox( cx-10, cx+10, cy-10, cy+10 );

    mScale = PointScale.SCALE_NONE; // scale
    // mPath = null;
    setScale( scale );
    mPaint = BrushManager.getStationPaint();
    // TDLog.v( "Point cstr " + type + " orientation " + mOrientation + " flip " + mFlip );
  }

  public DrawingStationPath( DrawingStationName st, int scale, int scrap )
  {
    super( DrawingPath.DRAWING_PATH_STATION, null, scrap );
    // TDLog.Log( TDLog.LOG_PATH, "Point " + mType + " X " + st.cx + " Y " + st.cy );
    // TDLog.v( "User Station (2) " + mType + " X " + st.cx + " Y " + st.cy );
    // mType = DRAWING_PATH_STATION;
    // mXpos = st.cx;  // st.cx : scene coords
    // mYpos = st.cy;
    cx = st.cx;  // st.cx : scene coords
    cy = st.cy;
    mName = st.getName(); // N.B. st.getName() is not null

    mScale = PointScale.SCALE_NONE; // scale
    // mPath = null;
    setScale( scale );
    mPaint = BrushManager.getStationPaint();
    // TDLog.v( "Point cstr " + type + " orientation " + mOrientation + " flip " + mFlip );
    setBBox( cx - 1, cx + 1, cy - 1, cy + 1 );
  }

  public String name() { return mName; }

  // @implements IDrawingLink
  public float getLinkX() { return cx; }
  public float getLinkY() { return cy; }
  // public Point2D getLink() { return new Point2D(cx,cy); }

  private void setScale( int scale )
  {
    if ( scale != mScale ) {
      // TDLog.Error( "set scale " + scale );
      mScale = scale;
      // station point does not have text
      float f = 1.0f;
      switch ( mScale ) {
        case PointScale.SCALE_XS: f = 0.50f; break;
        case PointScale.SCALE_S:  f = 0.72f; break;
        case PointScale.SCALE_L:  f = 1.41f; break;
        case PointScale.SCALE_XL: f = 2.00f; break;
      }
      Matrix m = new Matrix();
      m.postScale(f,f);
      makePath( BrushManager.getStationPath(), m, cx, cy ); // mXpos, mYpos );
    }  
  }
      
  // int getScale() { return mScale; }

  // public void setPointType( int t ) { mPointType = t; }
  // public int pointType() { return mPointType; }

  // public double xpos() { return mXpos; }
  // public double ypos() { return mYpos; }

  // public double orientation() { return mOrientation; }


  @Override
  public String toTherion( )
  {
    // return String.format(Locale.US, "point %.2f %.2f station -name %s\n", mXpos*TDSetting.mToTherion, -mYpos*TDSetting.mToTherion, mName );
    return String.format(Locale.US, "point %.2f %.2f station -name %s\n", cx*TDSetting.mToTherion, -cy*TDSetting.mToTherion, mName );
  }

  @Override
  void toDataStream( DataOutputStream dos, int scrap )
  {
    try {
      dos.write('U');
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      // if ( version >= 401147 ) dos.writeUTF( "" ); // station has no group
      dos.writeInt( mScale );
      // if ( version >= 401090 ) 
        dos.writeInt( mLevel );
      // if ( version >= 401160 ) 
        dos.writeInt( (scrap >= 0)? scrap : mScrap );
      dos.writeUTF( mName );
    } catch ( IOException e ) {
      TDLog.Error( "ERROR-dos station " + mName );
    }
  }

  public static DrawingStationPath loadDataStream( int version, DataInputStream dis )
  {
    try {
      float x = dis.readFloat();
      float y = dis.readFloat();
      // group = ( version >= 401147 )? dir.readUTF() : null; // station path has no group
      int scale = dis.readInt();
      int level = ( version >= 401090 )? dis.readInt() : DrawingLevel.LEVEL_DEFAULT;
      int scrap = ( version >= 401160 )? dis.readInt() : 0;
      String name = dis.readUTF();
      // TDLog.Log( TDLog.LOG_PLOT, "S " + name + " " + x + " " + y );
      return new DrawingStationPath( name, x, y, scale, scrap );
    } catch ( IOException e ) {
      TDLog.Error( "ERROR-dis station " + e.getMessage() );
    }
    return null;
  }

  // static void globDataStream( int version, DataInputStream dis )
  // {
  //   try {
  //     dis.readFloat();
  //     dis.readFloat();
  //     // group = ( version >= 401147 )? dir.readUTF() : null; // station path has no group
  //     dis.readInt();
  //     if ( version >= 401090 ) dis.readInt();
  //     if ( version >= 401160 ) dis.readInt();
  //     dis.readUTF();
  //   } catch ( IOException e ) {
  //     TDLog.Error( "ERROR-dis station " + e.getMessage() );
  //   }
  // }

//   @Override
//   void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ ) { }
  // { 
  //   int size = mScale - PointScale.SCALE_XS;
  //   int layer  = BrushManager.getPointCsxLayer( mPointType );
  //   int type   = BrushManager.getPointCsxType( mPointType );
  //   int cat    = BrushManager.getPointCsxCategory( mPointType );
  //   String csx = BrushManager.getPointCsx( mPointType );
  //   pw.format("<item layer=\"%d\" cave=\"%s\" branch=\"%s\" type=\"%d\" category=\"%d\" transparency=\"0.00\" data=\"",
  //     layer, cave, branch, type, cat );
  //   pw.format("&lt;?xml version=&quot;1.0&quot; encoding=&quot;UTF-8&quot;?&gt;&lt;!DOCTYPE svg PUBLIC &quot;-//W3C//DTD SVG 1.1//EN&quot; &quot;http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd&quot;[]&gt;&lt;svg xmlns=&quot;http://www.w3.org/2000/svg&quot; xml:space=&quot;preserve&quot; style=&quot;shape-rendering:geometricPrecision; text-rendering:geometricPrecision; image-rendering:optimizeQuality; fill-rule:evenodd; clip-rule:evenodd&quot; xmlns:xlink=&quot;http://www.w3.org/1999/xlink&quot;&gt;&lt;defs&gt;&lt;style type=&quot;text/css&quot;&gt;&lt;![CDATA[ .str0 {stroke:#1F1A17;stroke-width:0.2} .fil0 {fill:none} ]]&gt;&lt;/style&gt;&lt;/defs&gt;&lt;g id=&quot;Livello_%d&quot;&gt;", layer );
  //   pw.format("%s", csx );
  //   pw.format("&lt;/g&gt;&lt;/svg&gt;\" ");
  //   if ( bind != null ) pw.format(" bind=\"%s\" ", bind );
  //   pw.format(Locale.US, "dataformat=\"0\" signsize=\"%d\" angle=\"%.2f\" >\n", size, mOrientation );
  //   pw.format("  <pen type=\"10\" />\n");
  //   pw.format("  <brush type=\"7\" />\n");
  //   float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
  //   float y = DrawingUtil.sceneToWorldY( cx, cy );
  //   pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
  //   pw.format("  <datarow>\n");
  //   pw.format("  </datarow>\n");
  //   pw.format("</item>\n");

  //   // TDLog.v( "toCSurevy() Point " + mPointType + " (" + x + " " + y + ") orientation " + mOrientation );
  // }

  @Override
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
  { 
    pw.format("<item type=\"point\" name=\"station\" cave=\"%s\" branch=\"%s\" text=\"\" ", cave, branch );
    if ( bind != null ) pw.format(" bind=\"%s\" ", bind );
   pw.format(Locale.US, "scale=\"%d\" orientation=\"0.0\" options=\"-name=%s\" >\n", mScale, mName );
    float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
    float y = DrawingUtil.sceneToWorldY( cx, cy );
    pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
    pw.format("</item>\n");
    // TDLog.v( "toCSurevy() Point " + mPointType + " (" + x + " " + y + ") orientation " + mOrientation );
  }

}

