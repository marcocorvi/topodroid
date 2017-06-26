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
import android.graphics.Shader;
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

import android.util.Log;

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
  Shader mLocalShader = null;

  public DrawingAreaPath( int type, int cnt, String prefix, boolean visible )
  {
    super( DrawingPath.DRAWING_PATH_AREA, visible, true );
    mAreaType = type;
    mAreaCnt  = cnt;
    mPrefix   = (prefix != null && prefix.length() > 0)? prefix : "a";
    if ( mAreaType < BrushManager.mAreaLib.mSymbolNr ) {
      setPaint( BrushManager.mAreaLib.getSymbolPaint( mAreaType ) );
    }
    mOrientation = 0.0;
    if ( BrushManager.mAreaLib.isSymbolOrientable( mAreaType ) ) {
      // FIXME AREA_ORIENT 
      mOrientation = BrushManager.getAreaOrientation( type );

      mLocalShader = BrushManager.cloneAreaShader( mAreaType );
      resetPaint();
      mPaint.setShader( mLocalShader );
    }
  }

  // @param id   string "area id" (mPrefix + mAreaCnt )
  public DrawingAreaPath( int type, String id, boolean visible )
  {
    // visible = ?,   closed = true
    super( DrawingPath.DRAWING_PATH_AREA, visible, true );
    // TDLog.Log( TDLog.LOG_PLOT, "Drawing Area Path cstr type " + type + " id " + id );
    mAreaType = type;
    mAreaCnt = 1;
    mPrefix  = "a";
    try {
      int pos = id.lastIndexOf("a") + 1;
      mPrefix  = id.substring(0, pos);
      mAreaCnt = Integer.parseInt( id.substring(pos) );
    } catch ( NumberFormatException e ) {
      TDLog.Error( "Drawing Area Path AreaCnt parse Int error: " + id.substring(1) );
    }
    if ( mAreaType < BrushManager.mAreaLib.mSymbolNr ) {
      setPaint( BrushManager.mAreaLib.getSymbolPaint( mAreaType ) );
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

      // BrushManager.mAreaLib.tryLoadMissingArea( fname );
      type = BrushManager.mAreaLib.getSymbolIndexByFilename( fname );
      // TDLog.Log( TDLog.LOG_PLOT, "A: " + fname + " " + cnt + " " + visible + " " + orientation + " NP " + npt );
      if ( type < 0 ) {
        if ( missingSymbols != null ) missingSymbols.addAreaFilename( fname );
        type = 0;
      }

      DrawingAreaPath ret = new DrawingAreaPath( type, cnt, prefix, visible );
      ret.mOrientation = orientation;
      // setPaint( BrushManager.mAreaLib.getSymbolPaint( mAreaType ) );

      int has_cp;
      float x1, y1, x2, y2, x0, y0;
      x0 = x + dis.readFloat( );
      y0 = y + dis.readFloat( );
      has_cp = dis.read();
      if ( has_cp == 1 ) { // consume 4 floats
        x1 = x + dis.readFloat();
        y1 = y + dis.readFloat();
        x2 = x + dis.readFloat();
        y2 = y + dis.readFloat();
      }
      ret.addStartPoint( x0, y0 );
      // Log.v("DistoX", "A start " + x + " " + y );
      for ( int k=1; k<npt; ++k ) {
        x0 = x + dis.readFloat();
        y0 = y + dis.readFloat();
        has_cp = dis.read();
        // Log.v("DistoX", "A point " + x + " " + y + " " + has_cp );
        if ( has_cp == 1 ) {
          x1 = x + dis.readFloat();
          y1 = y + dis.readFloat();
          x2 = x + dis.readFloat();
          y2 = y + dis.readFloat();
          ret.addPoint3( x1, y1, x2, y2, x0, y0 );
        } else {
          ret.addPoint( x0, y0 );
        }
      }
      ret.retracePath();
      return  ( npt < 3 )? null : ret;
    } catch ( IOException e ) {
      TDLog.Error( "AREA in error " + e.getMessage() );
      // Log.v("DistoX", "AREA in error " + e.getMessage() );
    }
    return null;
  }

  public void setAreaType( int t ) 
  {
    mAreaType = t;
    if ( mAreaType < BrushManager.mAreaLib.mSymbolNr ) {
      setPaint( BrushManager.mAreaLib.getSymbolPaint( mAreaType ) );
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
    if ( ! BrushManager.mAreaLib.isSymbolOrientable( mAreaType ) ) return;
    mOrientation = angle; 
    while ( mOrientation >= 360.0 ) mOrientation -= 360.0;
    while ( mOrientation < 0.0 ) mOrientation += 360.0;
    resetPaint();
  }

  void shiftShaderBy( float dx, float dy, float s )
  {
    if ( mLocalShader != null ) {
      // Log.v( "DistoX", "shift shader by " + dx + " " + dy + " scale " + s + " orient " + mOrientation );
      Matrix mat = new Matrix();
      // shader.getLocalMatrix( mat ); // set shader matrix even if shader did not have one
      mat.postRotate( (float)mOrientation );
      mat.postTranslate( 4*dx, 4*dy );
      mat.postScale( s/4, s/4 );
      mLocalShader.setLocalMatrix( mat );
    }
  }

  private void resetPaint()
  {
    // Log.v("DistoX", "arae path reset paint orientation " + mOrientation );
    // Bitmap bitmap = BrushManager.getAreaBitmap( mAreaType );
    // if ( bitmap != null )
    if ( mLocalShader != null ) {
      Matrix mat = new Matrix();
      mat.postRotate( (float)mOrientation );
      // int w = bitmap.getWidth();
      // int h = bitmap.getHeight();
      // Bitmap bitmap1 = Bitmap.createBitmap( bitmap, 0, 0, w, h, mat, true );
      // Bitmap bitmap2 = Bitmap.createBitmap( bitmap1, w/4, h/4, w/2, h/2 );
      // BitmapShader shader = new BitmapShader( bitmap2,
      //   BrushManager.getAreaXMode( mAreaType ), BrushManager.getAreaYMode( mAreaType ) );
      // mPaint.setShader( shader );
      mLocalShader.setLocalMatrix( mat );
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
    // if ( TDSetting.xTherionAreas ) // NOTE xtherion needs an extra point 
    {
      float dx = mLast.x - mFirst.x;
      float dy = mLast.y - mFirst.y;
      if ( dx*dx + dy*dy > 1.0e-7 ) {
        mFirst.toTherion( pw );
      }
    }
    pw.format("endline\n");
    pw.format("area %s", BrushManager.mAreaLib.getSymbolThName( mAreaType ) );
    if ( BrushManager.mAreaLib.isSymbolOrientable( mAreaType ) ) {
      pw.format(Locale.US, " #orientation %.1f", mOrientation );
    }
    pw.format("\n");
    pw.format("  %s%d\n", mPrefix, mAreaCnt );
    pw.format("endarea\n");
    return sw.getBuffer().toString();
  }

  @Override
  public void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind )
  {
    int layer  = BrushManager.getAreaCsxLayer( mAreaType );
    int type   = 3;
    int cat    = BrushManager.getAreaCsxCategory( mAreaType );
    int pen    = BrushManager.getAreaCsxPen( mAreaType );
    int brush  = BrushManager.getAreaCsxBrush( mAreaType );

    // linetype: 0 spline, 1 bezier, 2 line
    pw.format("          <item layer=\"%d\" cave=\"%s\" branch=\"%s\" name=\"\" type=\"3\" category=\"%d\" linetype=\"2\"",
      layer, cave, branch, cat );
    if ( bind != null ) pw.format(" bind=\"%s\"", bind );
    pw.format(" mergemode=\"0\">\n" );
    pw.format("            <pen type=\"%d\" />\n", pen);
    pw.format("            <brush type=\"%d\" />\n", brush);
    pw.format("            <points data=\"");
    boolean b = true;
    // for ( LinePoint pt : mPoints ) 
    for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) 
    {
      float x = DrawingUtil.sceneToWorldX( pt.x );
      float y = DrawingUtil.sceneToWorldY( pt.y );
      pw.format(Locale.US, "%.2f %.2f ", x, y );
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
    String name = BrushManager.mAreaLib.getSymbolThName( mAreaType );
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
      // TDLog.Log( TDLog.LOG_PLOT, "A " + name + " " + npt );
    } catch ( IOException e ) {
      TDLog.Error( "AREA out error " + e.toString() );
    }
    // return 'A';
  }

}

