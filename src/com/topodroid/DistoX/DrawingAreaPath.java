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
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
// import com.topodroid.prefs.TDSetting;

import android.util.Log;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;
// import android.graphics.Bitmap;
// import android.graphics.BitmapShader;
import android.graphics.Shader;
// import android.graphics.Shader.TileMode;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
// import java.util.Iterator;
// import java.util.List;
// import java.util.ArrayList;
import java.util.Locale;

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
  private Shader mLocalShader = null;

  // FIXME-COPYPATH
  // @Override
  // DrawingPath copyPath()
  // {
  //   DrawingAreaPath ret = new DrawingAreaPath( mAreaType, mAreaCnt, mPrefix, isVisible() );
  //   copyTo( ret );
  //   return ret;
  // }

  DrawingAreaPath( int type, int cnt, String prefix, boolean visible, int scrap )
  {
    super( DrawingPath.DRAWING_PATH_AREA, visible, true, scrap );
    mAreaType = type;
    mAreaCnt  = cnt;
    mPrefix   = (prefix != null && prefix.length() > 0)? prefix : "a";
    if ( BrushManager.hasArea( mAreaType ) ) { 
      setPathPaint( BrushManager.getAreaPaint( mAreaType ) );
    // } else {
    //   Log.v("DistoX-PAINT", "area (1) not in lib " + mAreaType + " out of " + BrushManager.mAreaLib.size() );
    }
    mOrientation = 0.0;
    if ( BrushManager.isAreaOrientable( mAreaType ) ) {
      // FIXME AREA_ORIENT 
      mOrientation = BrushManager.getAreaOrientation( type );

      mLocalShader = BrushManager.cloneAreaShader( mAreaType );
      resetPathPaint();
      mPaint.setShader( mLocalShader );
    }
    mLevel = BrushManager.getAreaLevel( type );
  }

  // @param id   string "area id" (mPrefix + mAreaCnt )
  DrawingAreaPath( int type, String id, boolean visible, int scrap )
  {
    // visible = ?,   closed = true
    super( DrawingPath.DRAWING_PATH_AREA, visible, true, scrap );
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
    if ( BrushManager.hasArea( mAreaType ) ) {
      setPathPaint( BrushManager.getAreaPaint( mAreaType ) );
    // } else {
    //   Log.v("DistoX-PAINT", "area (2) not in lib " + mAreaType );
    }
    mLevel = BrushManager.getAreaLevel( type );
  }


  static DrawingAreaPath loadDataStream( int version, DataInputStream dis, float x, float y /*, SymbolsPalette missingSymbols */ )
  {
    int type, cnt;
    boolean visible;
    float orientation;
    int level = DrawingLevel.LEVEL_DEFAULT;
    int scrap = 0;
    String thname, prefix;
    String group = null;
    try {
      thname = dis.readUTF();
      if ( version >= 401147 ) group = dis.readUTF();
      prefix = dis.readUTF();
      cnt = dis.readInt();
      visible = ( dis.read( ) == 1 );
      orientation = dis.readFloat( );
      if ( version >= 401090 ) level = dis.readInt();
      if ( version >= 401160 ) scrap = dis.readInt();
      int npt = dis.readInt( );

      BrushManager.tryLoadMissingArea( thname );
      type = BrushManager.getAreaIndexByThNameOrGroup( thname, group );
      // TDLog.Log( TDLog.LOG_PLOT, "A: " + thname + " " + cnt + " " + visible + " " + orientation + " NP " + npt );
      // Log.v( "DistoX-PLOT", "A: " + type + " " + thname + " " + cnt + " " + visible + " " + orientation + " NP " + npt );
      if ( type < 0 ) {
        // FIXME-MISSING if ( missingSymbols != null ) missingSymbols.addAreaFilename( thname );
        type = 0;
      }

      DrawingAreaPath ret = new DrawingAreaPath( type, cnt, prefix, visible, scrap );
      ret.mLevel       = level;
      ret.mOrientation = orientation;
      // setPathPaint( BrushManager.getAreaPaint( mAreaType ) );

      int has_cp;
      float x0, y0, t;
      x0 = x + dis.readFloat( );
      y0 = y + dis.readFloat( );
      has_cp = dis.read();
      if ( has_cp == 1 ) { // consume 4 floats
        /* x1 = x + */ dis.readFloat();
        /* y1 = y + */ dis.readFloat();
        /* x2 = x + */ dis.readFloat();
        /* y2 = y + */ dis.readFloat();
      }
      ret.addStartPoint( x0, y0 );
      // Log.v("DistoX", "A start " + x + " " + y );
      for ( int k=1; k<npt; ++k ) {
        x0 = x + dis.readFloat();
        y0 = y + dis.readFloat();
        has_cp = dis.read();
        // Log.v("DistoX", "A point " + x + " " + y + " " + has_cp );
        if ( has_cp == 1 ) {
          float x1 = x + dis.readFloat();
          float y1 = y + dis.readFloat();
          float x2 = x + dis.readFloat();
          float y2 = y + dis.readFloat();
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

  // static void globDataStream( int version, DataInputStream dis )
  // {
  //   try {
  //     dis.readUTF();
  //     if ( version >= 401147 ) dis.readUTF();
  //     dis.readUTF();
  //     dis.readInt();
  //     dis.read( );
  //     dis.readFloat( );
  //     if ( version >= 401090 ) dis.readInt();
  //     if ( version >= 401160 ) dis.readInt();
  //     int npt = dis.readInt( );
  //     int has_cp;
  //     dis.readFloat( );
  //     dis.readFloat( );
  //     has_cp = dis.read();
  //     if ( has_cp == 1 ) { // consume 4 floats
  //       dis.readFloat();
  //       dis.readFloat();
  //       dis.readFloat();
  //       dis.readFloat();
  //     }
  //     for ( int k=1; k<npt; ++k ) {
  //       dis.readFloat();
  //       dis.readFloat();
  //       has_cp = dis.read();
  //       // Log.v("DistoX", "A point " + x + " " + y + " " + has_cp );
  //       if ( has_cp == 1 ) {
  //         dis.readFloat();
  //         dis.readFloat();
  //         dis.readFloat();
  //         dis.readFloat();
  //       }
  //     }
  //   } catch ( IOException e ) {
  //     TDLog.Error( "AREA in error " + e.getMessage() );
  //     // Log.v("DistoX", "AREA in error " + e.getMessage() );
  //   }
  // }

  void setAreaType( int t )
  {
    mAreaType = t;
    if ( BrushManager.hasArea( mAreaType ) ) {
      setPathPaint( BrushManager.getAreaPaint( mAreaType ) );
      // FIXME shader ?
    }
  }

  @Override
  void setPathPaint( Paint paint ) 
  { 
    mPaint = new Paint( paint );
    // Log.v("DistoX-PAINT", "set area color " + mPaint.getColor() );
    mPaint.setStyle( isVisible() ? Paint.Style.FILL_AND_STROKE : Paint.Style.FILL );
  }

  @Override
  void setVisible( boolean visible )
  {
    super.setVisible( visible );
    mPaint.setStyle( visible ? Paint.Style.FILL_AND_STROKE : Paint.Style.FILL );
  }

  int areaType() { return mAreaType; }

  public String getThName() { return BrushManager.getAreaThName( mAreaType ); }

  @Override
  void setOrientation( double angle ) 
  { 
    // Log.v( "DistoX", "Area path set orientation " + angle );
    if ( ! BrushManager.isAreaOrientable( mAreaType ) ) return;
    mOrientation = TDMath.in360( angle );
    resetPathPaint();
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

  private void resetPathPaint()
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
  void drawPath( Path path, Canvas canvas )
  {
    if ( mPaint != null ) {
      canvas.save();
      canvas.clipPath( path );
      canvas.drawPaint( mPaint );
      if ( isVisible() ) canvas.drawPath( path, BrushManager.borderPaint ); // ??? NullPointerException reported here`
      canvas.restore();
    }
  }

  @Override
  String toTherion( )
  {
    if ( mLast == null || mFirst == null ) return null;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("line border -id %s%d -close on", mPrefix, mAreaCnt );
    if ( ! isVisible() ) pw.format(" -visibility off");
    // for ( LinePoint pt : mPoints ) 
    pw.format("\n");

    // for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) 
    // {
    //   pt.toTherion( pw );
    // }
    // // if ( TDSetting.xTherionAreas ) // NOTE xtherion needs an extra point 
    // {
    //   float dx = mLast.x - mFirst.x;
    //   float dy = mLast.y - mFirst.y;
    //   if ( dx*dx + dy*dy > 1.0e-7 ) {
    //     mFirst.toTherion( pw );
    //   }
    // }
    toTherionPoints( pw, true );

    pw.format("endline\n");
    pw.format("area %s", getThName( ) );
    if ( BrushManager.isAreaOrientable( mAreaType ) ) {
      pw.format(Locale.US, " #orientation %.1f", mOrientation );
    }
    pw.format("\n");
    pw.format("  %s%d\n", mPrefix, mAreaCnt );
    pw.format("endarea\n");
    return sw.getBuffer().toString();
  }

  @Override
  void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /*, DrawingUtil mDrawingUtil */ )
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
    // FIXME CLOSE
    pw.format(" mergemode=\"0\">\n" );
    pw.format("            <pen type=\"%d\" />\n", pen);
    pw.format("            <brush type=\"%d\" />\n", brush);
    pw.format("            <points data=\"");
    boolean b = true;
    // for ( LinePoint pt : mPoints ) 
    for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) 
    {
      float x = DrawingUtil.sceneToWorldX( pt.x, pt.y );
      float y = DrawingUtil.sceneToWorldY( pt.x, pt.y );
      pw.format(Locale.US, "%.2f %.2f ", x, y );
      if ( b ) { pw.format("B "); b = false; }
    }
    pw.format("\" />\n");
    pw.format("          </item>\n");
  }

  @Override
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /*, DrawingUtil mDrawingUtil */ )
  {
    // linetype: 0 spline, 1 bezier, 2 line
    String name = getThName( );
    pw.format(Locale.US, "          <item type=\"area\" name=\"%s\" cave=\"%s\" branch=\"%s\" orientation=\"%.2f\" options=\"%s\" ",
      name, cave, branch, mOrientation, ( (mOptions== null)? "" : mOptions )
    );
    if ( bind != null ) pw.format(" bind=\"%s\"", bind );
    pw.format(" >\n" );
    toCsurveyPoints( pw, true, false );
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
  void toDataStream( DataOutputStream dos, int scrap )
  {
    String name  = getThName( );
    if ( name == null ) {
      TDLog.Error("null area name");
      name = "user";
    }
    String group = BrushManager.getAreaGroup( mAreaType );
    try {
      dos.write( 'A' );
      dos.writeUTF( name );
      // if ( version >= 401147 )
        dos.writeUTF( (group != null)? group : "" );
      dos.writeUTF( (mPrefix != null)? mPrefix : "" );
      dos.writeInt( mAreaCnt );
      dos.write( isVisible()? 1 : 0 );
      dos.writeFloat( (float)mOrientation );
      // if ( version >= 401090 )
        dos.writeInt( mLevel );
      // if ( version >= 401160 )
        dos.writeInt( (scrap >= 0)? scrap : mScrap );

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

