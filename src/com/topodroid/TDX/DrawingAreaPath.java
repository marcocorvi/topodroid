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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.math.TDVector;
// import com.topodroid.prefs.TDSetting;

import com.topodroid.num.TDNum;

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
  // private static String makeId()
  // {
  //   ++ area_id_cnt;
  //   String ret = "a" + area_id_cnt;
  //   return ret;
  // }

  int mAreaType;
  int mAreaCnt;
  double mOrientation;
  public String mPrefix;      // border/area name prefix (= scrap name) // TH2EDIT package
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

  // this method is used only for TH2EDIT to prevent area index change
  public DrawingAreaPath( int type, int cnt, String id, boolean visible, int scrap, boolean th2_edit ) // TH2EDIT
  {
    super( DrawingPath.DRAWING_PATH_AREA, visible, true, scrap );
    mAreaType = type;
    mAreaCnt  = cnt;
    mPrefix   = id;
    int pos = 1 + id.lastIndexOf("a");
    if ( pos > 0 && pos < id.length() ) {
      try {
        mAreaCnt = Integer.parseInt( id.substring( pos ) );
        mPrefix  = id.substring(0, pos);
      } catch ( NumberFormatException e ) {
        TDLog.e( e.getMessage() );
      }
    }
    // TDLog.v("AREA " + id + " count " + mPrefix + " " + mAreaCnt );
    if ( BrushManager.hasArea( mAreaType ) ) { 
      setPathPaint( BrushManager.getAreaPaint( mAreaType ) );
    // } else {
    //   // TDLog.v("PAINT area (1) not in lib " + mAreaType + " out of " + BrushManager.mAreaLib.size() );
    }
    mOrientation = 0.0;
    // if ( BrushManager.isAreaOrientable( mAreaType ) ) {
    //   mOrientation = BrushManager.getAreaOrientation( type );
    //   mLocalShader = BrushManager.cloneAreaShader( mAreaType );
    //   resetPathPaint();
    //   mPaint.setShader( mLocalShader );
    // }
    mLevel = BrushManager.getAreaLevel( type );
  }

  public DrawingAreaPath( int type, int cnt, String prefix, boolean visible, int scrap ) // TH2EDIT package
  {
    super( DrawingPath.DRAWING_PATH_AREA, visible, true, scrap );
    mAreaType = type;
    mAreaCnt  = cnt;
    mPrefix   = (prefix != null && prefix.length() > 0)? prefix : "a";
    // TDLog.v("AREA " + prefix + " count " + cnt );
    if ( BrushManager.hasArea( mAreaType ) ) { 
      setPathPaint( BrushManager.getAreaPaint( mAreaType ) );
    // } else {
    //   // TDLog.v("PAINT area (1) not in lib " + mAreaType + " out of " + BrushManager.mAreaLib.size() );
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
  public DrawingAreaPath( int type, String id, boolean visible, int scrap ) // TH2EDIT package
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
      // TDLog.v("AREA id <" + id + "> prefix " + mPrefix + " count " + mAreaCnt );
    } catch ( NumberFormatException e ) {
      TDLog.e( "Drawing Area Path AreaCnt parse int error: " + id.substring(1) );
    }
    if ( BrushManager.hasArea( mAreaType ) ) {
      setPathPaint( BrushManager.getAreaPaint( mAreaType ) );
    // } else {
    //   // TDLog.v("PAINT area (2) not in lib " + mAreaType );
    }
    mLevel = BrushManager.getAreaLevel( type );
  }

  /** factory: create a area path from the data stream
   * @param version serialize version
   * @param dis     input data stream
   * @param x       offset X coord [scene ?]
   * @param y       offset Y coord
   * @return the deserialized area path
   */
  public static DrawingAreaPath loadDataStream( int version, DataInputStream dis, float x, float y /*, SymbolsPalette missingSymbols */ )
  {
    int type, cnt;
    boolean visible;
    float orientation;
    int level = DrawingLevel.LEVEL_DEFAULT;
    int scrap = 0;
    String thname, prefix;
    String group = null;
    String options = null;
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

      
      // BrushManager.tryLoadMissingArea( thname ); // LOAD_MISSING

      type = BrushManager.getAreaIndexByThNameOrGroup( thname, group );
      // TDLog.Log( TDLog.LOG_PLOT, "A: " + thname + " " + cnt + " " + visible + " " + orientation + " NP " + npt );
      // TDLog.v( "Area: " + type + " " + thname + " " + cnt + " " + visible + " " + orientation + " NP " + npt );
      if ( type < 0 ) {
        // FIXME-MISSING if ( missingSymbols != null ) missingSymbols.addAreaFilename( thname );
        type = 0;
        options = "-symbol " + thname;
      }

      DrawingAreaPath ret = new DrawingAreaPath( type, cnt, prefix, visible, scrap );
      ret.addOption( options ); // does nothing is options is null
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
      // TDLog.v( "A start " + x + " " + y );
      for ( int k=1; k<npt; ++k ) {
        x0 = x + dis.readFloat();
        y0 = y + dis.readFloat();
        has_cp = dis.read();
        // TDLog.v( "A point " + x + " " + y + " " + has_cp );
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
      TDLog.e( "AREA in error " + e.getMessage() );
      // TDLog.v( "AREA in error " + e.getMessage() );
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
  //       // TDLog.v( "A point " + x + " " + y + " " + has_cp );
  //       if ( has_cp == 1 ) {
  //         dis.readFloat();
  //         dis.readFloat();
  //         dis.readFloat();
  //         dis.readFloat();
  //       }
  //     }
  //   } catch ( IOException e ) {
  //     TDLog.e( "AREA in error " + e.getMessage() );
  //     // TDLog.v( "AREA in error " + e.getMessage() );
  //   }
  // }

  /** set the area type (index)
   * @param t   area new type
   */
  public void setAreaType( int t ) // TH2EDIT package
  {
    mAreaType = t;
    if ( BrushManager.hasArea( mAreaType ) ) {
      setPathPaint( BrushManager.getAreaPaint( mAreaType ) );
      // FIXME shader ?
    }
  }

  /** set the area paint
   * @param paint  area paint
   */
  @Override
  void setPathPaint( Paint paint ) 
  { 
    mPaint = new Paint( paint );
    // TDLog.v("set area color " + mPaint.getColor() );
    mPaint.setStyle( isVisible() ? Paint.Style.FILL_AND_STROKE : Paint.Style.FILL );
  }

  /** set the area border-visibility
   * @param visible   visibility of the border
   */
  @Override
  void setVisible( boolean visible )
  {
    super.setVisible( visible );
    mPaint.setStyle( visible ? Paint.Style.FILL_AND_STROKE : Paint.Style.FILL );
  }

  /** @return the area index (ie, type)
   */
  int areaType() { return mAreaType; }

  /** @return the area Therion name
   */
  public String getThName() { return BrushManager.getAreaThName( mAreaType ); }

  /** @return the area full (with possible prefix) Therion name
   */
  public String getFullThName() { return BrushManager.getAreaFullThName( mAreaType ); }

  /** @return the area Therion type (possibly incuding the prefix), with ':' replaced by '_'
   */
  public String getFullThNameEscapedColon() { return  BrushManager.getAreaFullThNameEscapedColon( mAreaType ); }

  /** set the area orientation angle
   * @param angle  orientation angle [degrees]
   */
  @Override
  public void setOrientation( double angle )  // TH2EDIT package
  { 
    // TDLog.v( "Area path set orientation " + angle );
    if ( ! BrushManager.isAreaOrientable( mAreaType ) ) return;
    mOrientation = TDMath.in360( angle );
    resetPathPaint();
  }

  void shiftShaderBy( float dx, float dy, float s )
  {
    if ( mLocalShader != null ) {
      // TDLog.v( "shift shader by " + dx + " " + dy + " scale " + s + " orient " + mOrientation );
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
    // TDLog.v( "area path reset paint orientation " + mOrientation );
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
  void drawPath( Path path, Canvas canvas, int xor_color )
  {
    if ( mPaint != null ) {
      canvas.save();
      canvas.clipPath( path );
      canvas.drawPaint( xorPaint( mPaint, xor_color ) );
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
    pw.format("area %s", getFullThName( ) );
    if ( BrushManager.isAreaOrientable( mAreaType ) ) {
      pw.format(Locale.US, " #orientation %.1f", mOrientation );
    }
    pw.format("\n");
    pw.format("  %s%d\n", mPrefix, mAreaCnt );
    pw.format("endarea\n");
    return sw.getBuffer().toString();
  }

//   @Override
//   void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /*, DrawingUtil mDrawingUtil */ )
//   {
//     int layer  = BrushManager.getAreaCsxLayer( mAreaType );
//     int type   = 3;
//     int cat    = BrushManager.getAreaCsxCategory( mAreaType );
//     int pen    = BrushManager.getAreaCsxPen( mAreaType );
//     int brush  = BrushManager.getAreaCsxBrush( mAreaType );
// 
//     // linetype: 0 spline, 1 bezier, 2 line
//     pw.format("          <item layer=\"%d\" cave=\"%s\" branch=\"%s\" name=\"\" type=\"3\" category=\"%d\" linetype=\"2\"",
//       layer, cave, branch, cat );
//     if ( bind != null ) pw.format(" bind=\"%s\"", bind );
//     // FIXME CLOSE
//     pw.format(" mergemode=\"0\">\n" );
//     pw.format("            <pen type=\"%d\" />\n", pen);
//     pw.format("            <brush type=\"%d\" />\n", brush);
//     pw.format("            <points data=\"");
//     boolean b = true;
//     // for ( LinePoint pt : mPoints ) 
//     for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) 
//     {
//       float x = DrawingUtil.sceneToWorldX( pt.x, pt.y );
//       float y = DrawingUtil.sceneToWorldY( pt.x, pt.y );
//       pw.format(Locale.US, "%.2f %.2f ", x, y );
//       if ( b ) { pw.format("B "); b = false; }
//     }
//     pw.format("\" />\n");
//     pw.format("          </item>\n");
//   }

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
      TDLog.e("null area name");
      name = SymbolLibrary.USER;
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
      // TDLog.v( "A to stream: " + name + " " + mAreaCnt + " " + isVisible() + " " + mOrientation + " np " + npt );
      for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) {
        pt.toDataStream( dos );
      }
      // TDLog.Log( TDLog.LOG_PLOT, "A " + name + " " + npt );
    } catch ( IOException e ) {
      TDLog.e( "AREA out error " + e.toString() );
    }
    // return 'A';
  }


  @Override
  void toCave3D( PrintWriter pw, int type, DrawingCommandManager cmd, TDNum num )
  {
    if ( size() < 2 ) return;
    String name = getThName();
    int color   = BrushManager.getAreaColor( mAreaType );
    float red   = ((color >> 16)&0xff)/255.0f;
    float green = ((color >>  8)&0xff)/255.0f;
    float blue  = ((color      )&0xff)/255.0f;
    float alpha = ((color >> 24)&0xff)/255.0f;
    pw.format( Locale.US, "AREA %s %.2f %.2f %.2f %.2f\n", name, red, green, blue, alpha );
    for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) {
      pt.toCave3D( pw, type, cmd, num );
    }
    if ( mFirst != null ) mFirst.toCave3D( pw, type, cmd, num );
    pw.format( Locale.US, "ENDAREA\n" );
  }

  @Override
  void toCave3D( PrintWriter pw, int type, TDVector V1, TDVector V2 )
  {
    if ( size() < 2 || mFirst == null ) return; // 20230118 added test on mFirst
    String name = getThName();
    int color   = BrushManager.getAreaColor( mAreaType );
    float red   = ((color >> 16)&0xff)/255.0f;
    float green = ((color >>  8)&0xff)/255.0f;
    float blue  = ((color      )&0xff)/255.0f;
    float alpha = ((color >> 24)&0xff)/255.0f;
    pw.format( Locale.US, "AREA %s %.2f %.2f %.2f %.2f\n", name, red, green, blue, alpha );
    for ( LinePoint pt = mFirst; pt != null; pt = pt.mNext ) {
      pt.toCave3D( pw, type, V1, V2 );
    }
    mFirst.toCave3D( pw, type, V1, V2 );
    pw.format( Locale.US, "ENDAREA\n" );
  }

}

