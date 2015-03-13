/** @file SketchPainter.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid 3d sketch: path types (points, lines, and areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * 20130220 created
 */
package com.topodroid.DistoX;

import android.graphics.Paint;

class SketchPainter
{
  public static final int redColor  = 0xffff3333;
  public static final int blueColor = 0xff3399ff;
  Paint whitePaint;
  Paint redPaint;
  Paint greenPaint;
  Paint bluePaint;
  Paint blackPaint;
  Paint previewPaint;     // grey    c1c1c1
  Paint insidePaint;      // bluish  3366ff
  Paint borderLinePaint;  //         0033ff
  Paint surfaceForPaint;  // aqua    00cc99
  Paint surfaceBackPaint; // reddish cc6633
  // Paint areaPaint;        //         cc6633
  Paint vertexPaint;      //         00ff33
  Paint backVertexPaint;  //         ff0066

  SketchPainter()
  {
    makePaints();
  }

  Paint getLinePaint( int view ) 
  {
    return whitePaint;
  }

  private void makePaints()
  {
    whitePaint = new Paint();
    whitePaint.setColor(0xFFffffff);
    whitePaint.setStyle(Paint.Style.STROKE);
    whitePaint.setStrokeJoin(Paint.Join.ROUND);
    whitePaint.setStrokeCap(Paint.Cap.ROUND);
    redPaint   = new Paint();
    redPaint.setDither(true);
    redPaint.setColor( 0xccff0000 );
    redPaint.setStyle(Paint.Style.STROKE);
    redPaint.setStrokeJoin(Paint.Join.ROUND);
    redPaint.setStrokeCap(Paint.Cap.ROUND);
    redPaint.setStrokeWidth( 2 );
    greenPaint   = new Paint();
    greenPaint.setDither(true);
    greenPaint.setColor( 0xcc00ff33 );
    greenPaint.setStyle(Paint.Style.STROKE);
    greenPaint.setStrokeJoin(Paint.Join.ROUND);
    greenPaint.setStrokeCap(Paint.Cap.ROUND);
    greenPaint.setStrokeWidth( 2 );
    bluePaint   = new Paint();
    bluePaint.setDither(true);
    bluePaint.setColor( 0xcc0000ff);
    bluePaint.setStyle(Paint.Style.STROKE);
    bluePaint.setStrokeJoin(Paint.Join.ROUND);
    bluePaint.setStrokeCap(Paint.Cap.ROUND);
    bluePaint.setStrokeWidth( 2 );
    blackPaint   = new Paint();
    blackPaint.setDither(true);
    blackPaint.setColor( 0xff00ffff);
    blackPaint.setStyle(Paint.Style.STROKE);
    blackPaint.setStrokeJoin(Paint.Join.ROUND);
    blackPaint.setStrokeCap(Paint.Cap.ROUND);
    blackPaint.setStrokeWidth( 2 );

    previewPaint = new Paint();
    previewPaint.setColor(0xFFC1C1C1);
    previewPaint.setStyle(Paint.Style.STROKE);
    previewPaint.setStrokeJoin(Paint.Join.ROUND);
    previewPaint.setStrokeCap(Paint.Cap.ROUND);
    // topLinePaint = new Paint();
    // topLinePaint.setColor(0x99cc6633);
    // topLinePaint.setStyle(Paint.Style.STROKE);
    // topLinePaint.setStrokeJoin(Paint.Join.ROUND);
    // topLinePaint.setStrokeCap(Paint.Cap.ROUND);
    // sideLinePaint = new Paint();
    // // sideLinePaint.setColor(0xFF3333ff);
    // sideLinePaint.setColor(0x99cc9900);
    // sideLinePaint.setStyle(Paint.Style.STROKE);
    // sideLinePaint.setStrokeJoin(Paint.Join.ROUND);
    // sideLinePaint.setStrokeCap(Paint.Cap.ROUND);

    borderLinePaint = new Paint();
    borderLinePaint.setColor(0xff0033ff);
    borderLinePaint.setStyle(Paint.Style.STROKE);
    borderLinePaint.setStrokeJoin(Paint.Join.ROUND);
    borderLinePaint.setStrokeCap(Paint.Cap.ROUND);

    insidePaint = new Paint();
    insidePaint.setColor(0xff3366ff);
    insidePaint.setStyle(Paint.Style.FILL);
    insidePaint.setStrokeJoin(Paint.Join.ROUND);
    insidePaint.setStrokeCap(Paint.Cap.ROUND);

    surfaceForPaint = new Paint();
    // surfaceForPaint.setColor(0x66666666);
    surfaceForPaint.setColor(0x6600cc99);
    // surfaceForPaint.setStyle(Paint.Style.FILL);
    surfaceForPaint.setStyle(Paint.Style.STROKE);
    surfaceForPaint.setStrokeJoin(Paint.Join.ROUND);
    surfaceForPaint.setStrokeCap(Paint.Cap.ROUND);

    surfaceBackPaint = new Paint();
    surfaceBackPaint.setColor(0x44cc6633);
    // surfaceBackPaint.setStyle(Paint.Style.FILL);
    surfaceBackPaint.setStyle(Paint.Style.STROKE);
    surfaceBackPaint.setStrokeJoin(Paint.Join.ROUND);
    surfaceBackPaint.setStrokeCap(Paint.Cap.ROUND);

    // areaPaint = new Paint();
    // areaPaint.setColor(0x99cc6633);
    // areaPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    // areaPaint.setStrokeJoin(Paint.Join.ROUND);
    // areaPaint.setStrokeCap(Paint.Cap.ROUND);

    vertexPaint = new Paint();
    vertexPaint.setDither(true);
    vertexPaint.setColor( 0x9900ff66 );
    vertexPaint.setStyle(Paint.Style.FILL);
    vertexPaint.setStrokeJoin(Paint.Join.ROUND);
    vertexPaint.setStrokeCap(Paint.Cap.ROUND);

    backVertexPaint = new Paint();
    backVertexPaint.setDither(true);
    backVertexPaint.setColor( 0x99ff0066 );
    backVertexPaint.setStyle(Paint.Style.STROKE);
    backVertexPaint.setStrokeJoin(Paint.Join.ROUND);
    backVertexPaint.setStrokeCap(Paint.Cap.ROUND);

    setStrokeWidths();
  }

  void setStrokeWidths()
  {
    whitePaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
    previewPaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
    // topLinePaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
    // sideLinePaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
    borderLinePaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
    insidePaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
    surfaceForPaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
    surfaceBackPaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
    // areaPaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
    vertexPaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
    backVertexPaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
  }
}
