/* @file DrawingAreaPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: area-path (areas)
 *
 * The area border (line) path id DrawingPath.mPath
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Shader.TileMode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
// import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

// import android.util.Log;

/**
 */
public class DrawingAreaPath extends DrawingPointLinePath
{
  // private static int area_id_cnt = 0;
  // private statis String makeId() 
  // {
  //   ++ area_id_cnt;
  //   String ret = "a" + area_id_cnt;
  //   return ret;
  // }

  int mAreaType;
  int mAreaCnt;
  double mOrientation;
  String mPrefix;      // border/area name prefix (= scrap name)
  // boolean mVisible; // visible border in DrawingPointLinePath

  public DrawingAreaPath( int type, int cnt, String prefix, boolean visible )
  {
    super( DrawingPath.DRAWING_PATH_AREA, visible, true );
    mAreaType = type;
    mAreaCnt  = cnt;
    mOrientation = 0.0;
    if ( DrawingBrushPaths.mAreaLib.isSymbolOrientable( mAreaType ) ) {
      mOrientation = DrawingBrushPaths.getAreaOrientation( type );
    }
    mPrefix   = (prefix != null && prefix.length() > 0)? prefix : "a";
    if ( mAreaType < DrawingBrushPaths.mAreaLib.mSymbolNr ) {
      setPaint( DrawingBrushPaths.mAreaLib.getSymbolPaint( mAreaType ) );
    }
  }

  // @param id   string "area id" (mPrefix + mAreaCnt )
  public DrawingAreaPath( int type, String id, boolean visible )
  {
    // visible = ?,   closed = true
    super( DrawingPath.DRAWING_PATH_AREA, visible, true );
    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "Drawing Area Path cstr type " + type + " id " + id );
    mAreaType = type;
    mAreaCnt = 1;
    mPrefix  = "a";
    try {
      int pos = id.lastIndexOf("a") + 1;
      mPrefix  = id.substring(0, pos);
      mAreaCnt = Integer.parseInt( id.substring(pos) );
    } catch ( NumberFormatException e ) {
      TopoDroidLog.Error( "Drawing Area Path AreaCnt parse Int error: " + id.substring(1) );
    }
    if ( mAreaType < DrawingBrushPaths.mAreaLib.mSymbolNr ) {
      setPaint( DrawingBrushPaths.mAreaLib.getSymbolPaint( mAreaType ) );
    }
  }

  static DrawingAreaPath loadDataStream( int version, DataInputStream dis, float x, float y, SymbolsPalette missingSymbols )
  {
    int type, cnt;
    boolean visible;
    float orientation;
    String fname, prefix;
    try {
      fname = dis.readUTF();
      prefix = dis.readUTF();
      cnt = dis.readInt();
      visible = ( dis.read( ) == 1 );
      orientation = dis.readFloat( );
      int npt = dis.readInt( );

      // DrawingBrushPaths.mAreaLib.tryLoadMissingArea( fname );
      type = DrawingBrushPaths.mAreaLib.getSymbolIndexByFilename( fname );
      // Log.v("DistoX", "A: " + fname + " " + cnt + " " + visible + " " + orientation + " NP " + npt );
      if ( type < 0 ) {
        if ( missingSymbols != null ) missingSymbols.addAreaFilename( fname );
        type = 0;
      }

      DrawingAreaPath ret = new DrawingAreaPath( type, cnt, prefix, visible );
      // setPaint( DrawingBrushPaths.mAreaLib.getSymbolPaint( mAreaType ) );

      int has_cp;
      float mX1, mY1, mX2, mY2, mX, mY;
      mX = x + dis.readFloat( );
      mY = y + dis.readFloat( );
      has_cp = dis.read();
      ret.addStartPoint( mX, mY );
      // Log.v("DistoX", "A start " + mX + " " + mY );
      for ( int k=1; k<npt; ++k ) {
        mX = x + dis.readFloat();
        mY = y + dis.readFloat();
        has_cp = dis.read();
        // Log.v("DistoX", "A point " + mX + " " + mY + " " + has_cp );
        if ( has_cp == 1 ) {
          mX1 = x + dis.readFloat();
          mY1 = y + dis.readFloat();
          mX2 = x + dis.readFloat();
          mY2 = y + dis.readFloat();
          ret.addPoint3( mX1, mY1, mX2, mY2, mX, mY );
        } else {
          ret.addPoint( mX, mY );
        }
      }
      ret.retracePath();
      return  ( npt < 3 )? null : ret;
    } catch ( IOException e ) {
      TopoDroidLog.Error( "AREA in error " + e.toString() );
    }
    return null;
  }

  public void setAreaType( int t ) 
  {
    mAreaType = t;
    if ( mAreaType < DrawingBrushPaths.mAreaLib.mSymbolNr ) {
      setPaint( DrawingBrushPaths.mAreaLib.getSymbolPaint( mAreaType ) );
    }
  }

  @Override
  public void setPaint( Paint paint ) 
  { 
    mPaint = new Paint( paint );
  }

  public int areaType() { return mAreaType; }

  @Override
  public void setOrientation( double angle ) 
  { 
    // Log.v( "DistoX", "Area path set orientation " + angle );
    if ( ! DrawingBrushPaths.mAreaLib.isSymbolOrientable( mAreaType ) ) return;
    mOrientation = angle; 
    while ( mOrientation >= 360.0 ) mOrientation -= 360.0;
    while ( mOrientation < 0.0 ) mOrientation += 360.0;
    resetPaint();
  }

  private void resetPaint()
  {
    // Log.v("DistoX", "arae path reset paint orientation " + mOrientation );
    Bitmap bitmap = DrawingBrushPaths.getAreaBitmap( mAreaType );
    if ( bitmap != null ) {
      Matrix mat = new Matrix();
      int w = bitmap.getWidth();
      int h = bitmap.getHeight();
      mat.postRotate( (float)mOrientation );
      Bitmap bitmap1 = Bitmap.createBitmap( bitmap, 0, 0, w, h, mat, true );
      Bitmap bitmap2 = Bitmap.createBitmap( bitmap1, w/4, h/4, w/2, h/2 );
      BitmapShader shader = new BitmapShader( bitmap2,
        DrawingBrushPaths.getAreaXMode( mAreaType ), DrawingBrushPaths.getAreaYMode( mAreaType ) );
      mPaint.setShader( shader );
    }
  }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line border -id %s%d -close on", mPrefix, mAreaCnt );
    if ( ! isVisible() ) pw.format(" -visibility off");
    // for ( LinePoint pt : mPoints ) 
    pw.format("\n");

    for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) 
    {
      pt.toTherion( pw );
    }
    if ( TopoDroidSetting.mXTherionAreas ) { // NOTE xtherion needs an extra point 
      float dx = mLast.mX - mFirst.mX;
      float dy = mLast.mY - mFirst.mY;
      if ( dx*dx + dy*dy > 1.0e-7 ) {
        mFirst.toTherion( pw );
      }
    }
    pw.format("endline\n");
    pw.format("area %s", DrawingBrushPaths.mAreaLib.getSymbolThName( mAreaType ) );
    if ( DrawingBrushPaths.mAreaLib.isSymbolOrientable( mAreaType ) ) {
      pw.format(Locale.ENGLISH, " #orientation %.1f", mOrientation );
    }
    pw.format("\n");
    pw.format("  %s%d\n", mPrefix, mAreaCnt );
    pw.format("endarea\n");
    return sw.getBuffer().toString();
  }

  @Override
  public void toCsurvey( PrintWriter pw )
  {
    int layer  = DrawingBrushPaths.getAreaCsxLayer( mAreaType );
    int type   = 3;
    int cat    = DrawingBrushPaths.getAreaCsxCategory( mAreaType );
    int pen    = DrawingBrushPaths.getAreaCsxPen( mAreaType );
    int brush  = DrawingBrushPaths.getAreaCsxBrush( mAreaType );

    // linetype: 0 line, 1 spline, 2 bezier
    pw.format("          <item layer=\"%d\" name=\"\" type=\"3\" category=\"%d\" linetype=\"1\" mergemode=\"0\">\n",
      layer, cat );
    pw.format("            <pen type=\"%d\" />\n", pen);
    pw.format("            <brush type=\"%d\" />\n", brush);
    pw.format("            <points data=\"");
    boolean b = true;
    // for ( LinePoint pt : mPoints ) 
    for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) 
    {
      float x = DrawingUtil.sceneToWorldX( pt.mX );
      float y = DrawingUtil.sceneToWorldY( pt.mY );
      pw.format(Locale.ENGLISH, "%.2f %.2f ", x, y );
      if ( b ) { pw.format("B "); b = false; }
    }
    pw.format("\" />\n");
    pw.format("          </item>\n");
  }

  @Override
  LinePoint next( LinePoint lp )
  {
    if ( lp == null ) return null;
    if ( lp.mNext == null ) return mFirst;
    return lp.mNext;
  }

  @Override
  LinePoint prev( LinePoint lp )
  {
    if ( lp == null ) return null;
    if ( lp.mPrev == null ) return mLast;
    return lp.mPrev;
  }

  @Override
  void toDataStream( DataOutputStream dos ) 
  {
    String name = DrawingBrushPaths.mAreaLib.getSymbolThName( mAreaType );
    try {
      dos.write( 'A' );
      dos.writeUTF( name );
      dos.writeUTF( (mPrefix != null)? mPrefix : "" );
      dos.writeInt( mAreaCnt );
      dos.write( isVisible()? 1 : 0 );
      dos.writeFloat( (float)mOrientation );

      int npt = size(); // number of line points
      dos.writeInt( npt );
      // Log.v("DistoX", "A to stream: " + name + " " + mAreaCnt + " " + isVisible() + " " + mOrientation + " np " + npt );
      for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) {
        pt.toDataStream( dos );
      }
    } catch ( IOException e ) {
      TopoDroidLog.Error( "AREA out error " + e.toString() );
    }
    // return 'A';
  }

}

