/* @file DrawingLabelPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: label-point
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PointScale;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

public class DrawingLabelPath extends DrawingPointPath
{
  private final static float PDF_SCALE = 0.4f;

  /** cstr
   * @param text     label text
   * @param off_x    X coord
   * @param off_y    Y coord
   * @param scale    text scale (Therion point scale)
   * @param options  additional Therion point options
   * @param scrap    point scrap index
   */
  DrawingLabelPath( String text, float off_x, float off_y, int scale, String options, int scrap )
  {
    super( BrushManager.getPointLabelIndex(), off_x, off_y, scale, text, options, scrap );
    // mPointText = text;
    // setPaint( BrushManager.pointPaint[ BrushManager.POINT_LABEL ] );
    // mPaint = BrushManager.pointPaint[ BrushManager.POINT_LABEL ];
    // paint = new Paint();
    // paint.setDither(true);
    // paint.setColor( 0xffffffff );
    // paint.setStyle(Paint.Style.STROKE);
    // paint.setStrokeJoin(Paint.Join.ROUND);
    // paint.setStrokeCap(Paint.Cap.ROUND);
    // paint.setStrokeWidth( WIDTH_CURRENT );

    // makeStraightPath( 0, 0, 20*mPointText.length(), 0, cx, cy );
    doSetScale( mScale );
  }

  /** deserialize from an input stream
   * @param version   stream version
   * @param dis       input stream
   * @param x         offset X coord [scene ?]
   * @param y         offset Y coord
   */
  public static DrawingLabelPath loadDataStream( int version, DataInputStream dis, float x, float y )
  {
    float ccx, ccy;
    int scale;
    int level = DrawingLevel.LEVEL_DEFAULT;
    int scrap = 0;
    float orientation = 0;
    // int type;
    String text, options;
    try {
      ccx = x + dis.readFloat( );
      ccy = y + dis.readFloat( );
      // String th_name = dis.readUTF( );
      // type = BrushManager.getPointLabelIndex();
      // if ( version > 401147 ) group = dis.readUTF() // label has null group and is guaranteed to exist
      if ( version > 207043 ) orientation = dis.readFloat( );
      scale = dis.readInt( );
      if ( version > 401090 ) level = dis.readInt();
      if ( version > 401160 ) scrap = dis.readInt();
      text = dis.readUTF();
      options = dis.readUTF();

      // TDLog.Log( TDLog.LOG_PLOT, "Label <" + text + " " + ccx + " " + ccy + " scale " + scale + " (" + options + ")" );
      // TDLog.v( "Label <" + text + "> " + ccx + " " + ccy + " scale " + scale + " (" + options + ")" );

      DrawingLabelPath ret = new DrawingLabelPath( text, ccx, ccy, scale, options, scrap );
      ret.mLevel = level;
      ret.setOrientation( orientation );
      return ret;
    } catch ( IOException e ) {
      TDLog.Error( "LABEL in error " + e.getMessage() );
      // TDLog.v( "LABEL in error " + e.getMessage() );
    }
    return null;
  }

  // static void globDataStream( int version, DataInputStream dis )
  // {
  //   try {
  //     dis.readFloat( );
  //     dis.readFloat( );
  //     if ( version > 207043 ) dis.readFloat( );
  //     dis.readInt( );
  //     if ( version > 401090 ) dis.readInt();
  //     if ( version > 401160 ) dis.readInt();
  //     dis.readUTF();
  //     dis.readUTF();
  //   } catch ( IOException e ) {
  //     TDLog.Error( "LABEL in error " + e.getMessage() );
  //     // TDLog.v( "LABEL in error " + e.getMessage() );
  //   }
  // }

  /** draw the label on the screen
   * @param canvas   canvas
   * @param bbox     clipping rectangle
   */
  @Override
  public void draw( Canvas canvas, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      // TDLog.Log( TDLog.LOG_PATH, "Drawing Label Path::draw " + mPointText );
      canvas.drawTextOnPath( mPointText, mPath, 0f, 0f, mPaint );
    }
  }

  /** draw the label on the screen
   * @param canvas   canvas
   * @param matrix   transform matrix
   * @param scale    scaling factor - not used
   * @param bbox     clipping rectangle
   * @note scale is not used but this signature is necessary because DrawingLabelPath extends DrawingPointPath
   */
  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
    Paint paint = (bbox != null)? mPaint : BrushManager.blackPaint;
    if ( intersects( bbox ) ) {
      // TDLog.Log( TDLog.LOG_PATH, "Drawing Label Path::draw[matrix] " + mPointText );
      setTextSize();
      mTransformedPath = new Path( mPath );
      if ( mLandscape ) {
        Matrix rot = new Matrix();
	rot.postRotate( 90, cx, cy );
        mTransformedPath.transform( rot );
      }
      mTransformedPath.transform( matrix );
      canvas.drawTextOnPath( mPointText, mTransformedPath, 0f, 0f, mPaint );
    }
  }

  /** draw the label on the screen
   * @param canvas    canvas
   * @param matrix    transform matrix
   * @param scale     scaling factor - not used
   * @param bbox      clipping rectangle
   * @param xor_color xor color
   * @note scale is not used but this signature is necessary because DrawingLabelPath extends DrawingPointPath
   * @note text size is reduced by 25 %
   */
  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox, int xor_color )
  {
    // TDLog.v("DRAW xor color " + xor_color + " scale " + scale );
    if ( intersects( bbox ) ) {
      // TDLog.Log( TDLog.LOG_PATH, "Drawing Label Path::draw[matrix] " + mPointText );
      setTextSize();
      mTransformedPath = new Path( mPath );
      if ( mLandscape ) {
        Matrix rot = new Matrix();
        rot.postRotate( 90, cx, cy );
        mTransformedPath.transform( rot );
      }
      mTransformedPath.transform( matrix );
      Paint paint = xorPaint( mPaint, xor_color );
      paint.setTextSize( PDF_SCALE * paint.getTextSize() );
      canvas.drawTextOnPath( mPointText, mTransformedPath, 0f, 0f, paint );
    }
  }

  /** @return the font-size factor according to the point scale
   */
  private float fontSize( )
  {
    switch ( mScale ) {
      case PointScale.SCALE_XS: return 0.50f;
      case PointScale.SCALE_S:  return 0.72f;
      case PointScale.SCALE_L:  return 1.41f;
      case PointScale.SCALE_XL: return 2.00f;
    }
    return 1;
  }

  /** create the path for the label text
   */
  private void makeLabelPath( /* float f */ )
  {
    Rect r = new Rect();
    mPaint.getTextBounds( mPointText, 0, mPointText.length(), r );
    // float len = 20 * f * mPointText.length();
    float len = 2 * r.width(); // FIXME multiplying by 10 is a hack

    float a = (float)(mOrientation) * TDMath.DEG2RAD;
    float ca = len * TDMath.cos( a );
    float sa = len * TDMath.sin( a );
    makeStraightPath( 0, 0, ca, sa, cx, cy );
  }

  /** set the point scale
   * @param scale   (Therion) scale
   */
  @Override
  void setScale( int scale )
  {
    if ( scale != mScale ) doSetScale( scale );
  }

  /** set the point scale and make the label path
   * @param scale   (Therion) scale
   */
  private void doSetScale( int scale )
  {
    mScale = scale;
    float f = fontSize();
    mPaint = BrushManager.labelPaint; // was new Paint( ... )
    mPaint.setTextSize( TDSetting.mLabelSize * f );
    makeLabelPath( /* f */ );
  }

  /** set the orientation
   * @param angle   orientation [degrees] - 0 = vertical
   */
  @Override
  void setOrientation( double angle ) 
  { 
    mOrientation = TDMath.in360( angle );
    makeLabelPath( /* fontSize() */ );
  }

  /** set the size of the text according to the point scale
   */ 
  private void setTextSize()
  {
    float f = 1.0f;
    switch ( mScale ) {
      case PointScale.SCALE_XS: f = 0.50f; break;
      case PointScale.SCALE_S:  f = 0.72f; break;
      case PointScale.SCALE_L:  f = 1.41f; break;
      case PointScale.SCALE_XL: f = 2.00f; break;
    }
    mPaint.setTextSize( TDSetting.mLabelSize * f );
  }

  /** @return Therion string representation of the label point
   */
  @Override
  String toTherion( )
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.US, "point %.2f %.2f label -text \"%s\"", cx*TDSetting.mToTherion, -cy*TDSetting.mToTherion, mPointText );
    toTherionOrientation( pw );
    toTherionOptions( pw );
    pw.format("\n");
    return sw.getBuffer().toString();
  }

//   @Override
//   void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
//   { 
//     // int size = mScale - PointScale.SCALE_XS;
//     // int layer  = 6;
//     // int type   = 8;
//     // int cat    = 81;
//     pw.format("<item layer=\"6\" cave=\"%s\" branch=\"%s\" type=\"8\" category=\"81\" transparency=\"0.00\"",
//       cave, branch );
//     if ( bind != null ) pw.format( " bind=\"%s\"", bind );
//     pw.format(" text=\"%s\" textrotatemode=\"1\" >\n", mPointText );
//     pw.format("  <pen type=\"10\" />\n");
//     pw.format("  <brush type=\"7\" />\n");
//     float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
//     float y = DrawingUtil.sceneToWorldY( cx, cy );
//     pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
//     pw.format("  <font type=\"0\" />\n");
//     pw.format("</item>\n");
//   }

  /** write the label point in cSurvey format
   * @param pw        output writer
   * @param survey    (cSurvey) survey name
   * @param cave      cSurvey cave name
   * @param branch    cSurvey branch name
   * @param bind      cSurvey binding
   */
  @Override
  void toTCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
  { 
    // int size = mScale - PointScale.SCALE_XS;
    pw.format("<item type=\"point\" name=\"label\" cave=\"%s\" branch=\"%s\" text=\"%s\" ",
      cave, branch, mPointText );
    if ( bind != null ) pw.format( " bind=\"%s\"", bind );
    pw.format(Locale.US, "scale=\"%d\" orientation=\"%.2f\" options=\"%s\" >\n", mScale, mOrientation, ((mOptions==null)? "" : mOptions) );
    float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
    float y = DrawingUtil.sceneToWorldY( cx, cy );
    pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
    pw.format("</item>\n");
  }

  /** serialize the label point
   * @param dos    output stream
   * @param scrap  scrap index
   */
  @Override
  void toDataStream( DataOutputStream dos, int scrap )
  {
    // label has null group
    try {
      dos.write( 'T' );
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      // dos.writeUTF( BrushManager.getPointThName(mPointType) );
      // if ( version >= 401147 ) dos.writeUTF( "" ); // label has null group
      dos.writeFloat( (float)mOrientation ); // from version 2.7.4e
      dos.writeInt( mScale );
      // if ( version >= 401090 ) 
        dos.writeInt( mLevel );
      // if ( version >= 401160 ) 
        dos.writeInt( (scrap >= 0)? scrap : mScrap );
      dos.writeUTF( ( mPointText != null )? mPointText : "" );
      dos.writeUTF( ( mOptions != null )? mOptions : "" );
      // TDLog.Log( TDLog.LOG_PLOT, "T " + " " + cx + " " + cy );
    } catch ( IOException e ) {
      TDLog.Error( "LABEL out error " + e.toString() );
    }
  }

}

