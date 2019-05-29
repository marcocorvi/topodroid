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
package com.topodroid.DistoX;

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

// import android.util.Log;

/**
 */
class DrawingLabelPath extends DrawingPointPath
{
  DrawingLabelPath( String text, float off_x, float off_y, int scale, String options )
  {
    super( BrushManager.mPointLib.mPointLabelIndex, off_x, off_y, scale, text, options );
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

  static DrawingLabelPath loadDataStream( int version, DataInputStream dis, float x, float y )
  {
    float ccx, ccy;
    int scale;
    int level = DrawingLevel.LEVEL_DEFAULT;
    float orientation = 0;
    // int type;
    String text, options;
    try {
      ccx = x + dis.readFloat( );
      ccy = y + dis.readFloat( );
      // String th_name = dis.readUTF( );
      // type = BrushManager.getPointLabelIndex();
      if ( version > 207043 ) orientation = dis.readFloat( );
      scale = dis.readInt( );
      if ( version > 401090 ) level = dis.readInt();
      text = dis.readUTF();
      options = dis.readUTF();

      // TDLog.Log( TDLog.LOG_PLOT, "Label <" + text + " " + ccx + " " + ccy + " scale " + scale + " (" + options + ")" );
      DrawingLabelPath ret = new DrawingLabelPath( text, ccx, ccy, scale, options );
      ret.mLevel = level;
      ret.setOrientation( orientation );
      return ret;
    } catch ( IOException e ) {
      TDLog.Error( "LABEL in error " + e.getMessage() );
      // Log.v("DistoX", "LABEL in error " + e.getMessage() );
    }
    return null;
  }

  @Override
  public void draw( Canvas canvas, RectF bbox )
  {
    if ( intersects( bbox ) ) {
      // TDLog.Log( TDLog.LOG_PATH, "Drawing Label Path::draw " + mPointText );
      canvas.drawTextOnPath( mPointText, mPath, 0f, 0f, mPaint );
    }
  }

  @Override
  public void draw( Canvas canvas, Matrix matrix, float scale, RectF bbox )
  {
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

  private float fontSize( )
  {
    switch ( mScale ) {
      case SCALE_XS: return 0.50f;
      case SCALE_S:  return 0.72f;
      case SCALE_L:  return 1.41f;
      case SCALE_XL: return 2.00f;
    }
    return 1;
  }

  private void makeLabelPath( float f )
  {
    Rect r = new Rect();
    mPaint.getTextBounds( mPointText, 0, mPointText.length(), r );
    // float len = 20 * f * mPointText.length();
    float len = 10 * r.width(); // FIXME multiplying by 10 is a hack

    float a = (float)(mOrientation) * TDMath.DEG2RAD;
    float ca = len * TDMath.cos( a );
    float sa = len * TDMath.sin( a );
    makeStraightPath( 0, 0, ca, sa, cx, cy );
  }

  @Override
  void setScale( int scale )
  {
    if ( scale != mScale ) doSetScale( scale );
  }

  private void doSetScale( int scale )
  {
    mScale = scale;
    float f = fontSize();
    mPaint = new Paint( BrushManager.labelPaint );
    mPaint.setTextSize( TDSetting.mLabelSize * f );
    makeLabelPath( f );
  }

  @Override
  void setOrientation( double angle ) 
  { 
    mOrientation = TDMath.in360( angle );
    float f = fontSize();
    makeLabelPath( f );
  }


  private void setTextSize()
  {
    float f = 1.0f;
    switch ( mScale ) {
      case SCALE_XS: f = 0.50f; break;
      case SCALE_S:  f = 0.72f; break;
      case SCALE_L:  f = 1.41f; break;
      case SCALE_XL: f = 2.00f; break;
    }
    mPaint.setTextSize( TDSetting.mLabelSize * f );
  }

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

  @Override
  void toCsurvey( PrintWriter pw, String survey, String cave, String branch, String bind /* , DrawingUtil mDrawingUtil */ )
  { 
    // int size = mScale - SCALE_XS;
    // int layer  = 6;
    // int type   = 8;
    // int cat    = 81;
    pw.format("<item layer=\"6\" cave=\"%s\" branch=\"%s\" type=\"8\" category=\"81\" transparency=\"0.00\"",
      cave, branch );
    if ( bind != null ) pw.format( " bind=\"%s\"", bind );
    pw.format(" text=\"%s\" textrotatemode=\"1\" >\n", mPointText );
    pw.format("  <pen type=\"10\" />\n");
    pw.format("  <brush type=\"7\" />\n");
    float x = DrawingUtil.sceneToWorldX( cx, cy ); // convert to world coords.
    float y = DrawingUtil.sceneToWorldY( cx, cy );
    pw.format(Locale.US, " <points data=\"%.2f %.2f \" />\n", x, y );
    pw.format("  <font type=\"0\" />\n");
    pw.format("</item>\n");
  }

  @Override
  void toDataStream( DataOutputStream dos )
  {
    try {
      dos.write( 'T' );
      dos.writeFloat( cx );
      dos.writeFloat( cy );
      // dos.writeUTF( BrushManager.mPointLib.getSymbolThName(mPointType) );
      dos.writeFloat( (float)mOrientation ); // from version 2.7.4e
      dos.writeInt( mScale );
      // if ( version >= 401090 ) 
        dos.writeInt( mLevel );
      dos.writeUTF( ( mPointText != null )? mPointText : "" );
      dos.writeUTF( ( mOptions != null )? mOptions : "" );
      // TDLog.Log( TDLog.LOG_PLOT, "T " + " " + cx + " " + cy );
    } catch ( IOException e ) {
      TDLog.Error( "LABEL out error " + e.toString() );
    }
  }
}

