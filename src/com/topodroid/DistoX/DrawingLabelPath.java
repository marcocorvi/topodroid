/* @file DrawingLabelPath.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: label-point
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * 
 * 20140610 label size
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;

/**
 */
public class DrawingLabelPath extends DrawingPointPath
{
  private static float toTherion = TopoDroidConst.TO_THERION;
  public String mText;
  // private Paint paint;

  public DrawingLabelPath( String text, float off_x, float off_y, int scale, String options )
  {
    super( DrawingBrushPaths.mPointLib.mPointLabelIndex, off_x, off_y, scale, options );
    mText = text;
    // setPaint( DrawingBrushPaths.pointPaint[ DrawingBrushPaths.POINT_LABEL ] );
    // mPaint = DrawingBrushPaths.pointPaint[ DrawingBrushPaths.POINT_LABEL ];
    // paint = new Paint();
    // paint.setDither(true);
    // paint.setColor( 0xffffffff );
    // paint.setStyle(Paint.Style.STROKE);
    // paint.setStrokeJoin(Paint.Join.ROUND);
    // paint.setStrokeCap(Paint.Cap.ROUND);
    // paint.setStrokeWidth( WIDTH_CURRENT );

    makeStraightPath( 0, 0, 20*mText.length(), 0, cx, cy );
  }

  @Override
  public void draw( Canvas canvas )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_PATH, "DrawingLabelPath::draw " + mText );
    canvas.drawTextOnPath( mText, mPath, 0f, 0f, mPaint );
  }

  @Override
  public void draw( Canvas canvas, Matrix matrix )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_PATH, "DrawingLabelPath::draw[matrix] " + mText );
    setTextSize();
    mTransformedPath = new Path( mPath );
    mTransformedPath.transform( matrix );
    canvas.drawTextOnPath( mText, mTransformedPath, 0f, 0f, mPaint );
  }

  @Override
  public String getText() { return mText; }

  @Override
  public void setText( String text ) { mText = text; }

  @Override
  public void setScale( int scale )
  {
    if ( scale != mScale ) {
      mScale = scale;
      float f = 1.0f;
      switch ( mScale ) {
        case SCALE_XS: f = 0.50f; break;
        case SCALE_S:  f = 0.72f; break;
        case SCALE_L:  f = 1.41f; break;
        case SCALE_XL: f = 2.00f; break;
      }
      mPaint = new Paint( DrawingBrushPaths.labelPaint );
      mPaint.setTextSize( TopoDroidSetting.mLabelSize * f );
      makeStraightPath( 0, 0, 20*f*mText.length(), 0, cx, cy );
    }
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
    mPaint.setTextSize( TopoDroidSetting.mLabelSize * f );
  }

  @Override
  public String toTherion()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.ENGLISH, "point %.2f %.2f label -text \"%s\" ", cx*toTherion, -cy*toTherion, mText );
    toTherionOptions( pw );
    pw.format("\n");
    return sw.getBuffer().toString();
  }

  @Override
  public void toCsurvey( PrintWriter pw )
  { 
    // int size = mScale - SCALE_XS;
    // int layer  = 6;
    // int type   = 8;
    // int cat    = 81;
    pw.format("<item layer=\"6\" type=\"8\" category=\"81\" transparency=\"0.00\"" );
    pw.format(" text=\"%s\" textrotatemode=\"1\" >\n", mText );
    pw.format("  <pen type=\"10\" />\n");
    pw.format("  <brush type=\"7\" />\n");
    float x = DrawingActivity.sceneToWorldX( cx ); // convert to world coords.
    float y = DrawingActivity.sceneToWorldY( cy );
    pw.format(Locale.ENGLISH, " <points data=\"%.2f %.2f \" />\n", x, y );
    pw.format("  <font type=\"0\" />\n");
    pw.format("</item>\n");
  }
}

