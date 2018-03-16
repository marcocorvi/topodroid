/** @file SketchPainter.java
 *
 * @author marco corvi
 * @date feb 2013
 *
 * @brief TopoDroid 3d sketch: path types (points, lines, and areas)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Paint;

class SketchPainter
{
  // public static final int redColor  = 0xffff3333;
  // public static final int blueColor = 0xff3399ff;
  Paint whitePaint;
  Paint redPaint;
  Paint greenPaint;
  Paint bluePaint;
  Paint blackPaint;
  // Paint previewPaint;     // grey    c1c1c1
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
    whitePaint.setColor( TDColor.WHITE );
    whitePaint.setStyle(Paint.Style.STROKE);
    whitePaint.setStrokeJoin(Paint.Join.ROUND);
    whitePaint.setStrokeCap(Paint.Cap.ROUND);

    redPaint   = new Paint();
    redPaint.setDither(true);
    redPaint.setColor( TDColor.PINK );
    redPaint.setStyle(Paint.Style.STROKE);
    redPaint.setStrokeJoin(Paint.Join.ROUND);
    redPaint.setStrokeCap(Paint.Cap.ROUND);
    redPaint.setStrokeWidth( 2 );

    greenPaint   = new Paint();
    greenPaint.setDither(true);
    greenPaint.setColor( TDColor.GREEN );
    greenPaint.setStyle(Paint.Style.STROKE);
    greenPaint.setStrokeJoin(Paint.Join.ROUND);
    greenPaint.setStrokeCap(Paint.Cap.ROUND);
    greenPaint.setStrokeWidth( 2 );

    bluePaint   = new Paint();
    bluePaint.setDither(true);
    bluePaint.setColor( TDColor.BLUE );
    bluePaint.setStyle(Paint.Style.STROKE);
    bluePaint.setStrokeJoin(Paint.Join.ROUND);
    bluePaint.setStrokeCap(Paint.Cap.ROUND);
    bluePaint.setStrokeWidth( 2 );

    blackPaint   = new Paint();
    blackPaint.setDither(true);
    blackPaint.setColor( TDColor.LIGHT_BLUE );
    blackPaint.setStyle(Paint.Style.STROKE);
    blackPaint.setStrokeJoin(Paint.Join.ROUND);
    blackPaint.setStrokeCap(Paint.Cap.ROUND);
    blackPaint.setStrokeWidth( 2 );

    // previewPaint = new Paint();
    // previewPaint.setColor( TDColor.LIGHT_GRAY );
    // previewPaint.setStyle(Paint.Style.STROKE);
    // previewPaint.setStrokeJoin(Paint.Join.ROUND);
    // previewPaint.setStrokeCap(Paint.Cap.ROUND);

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
    borderLinePaint.setColor( TDColor.LIGHT_GREEN );
    borderLinePaint.setStyle(Paint.Style.STROKE);
    borderLinePaint.setStrokeJoin(Paint.Join.ROUND);
    borderLinePaint.setStrokeCap(Paint.Cap.ROUND);

    insidePaint = new Paint();
    insidePaint.setColor( TDColor.LIGHT_BLUE );
    insidePaint.setStyle(Paint.Style.FILL);
    insidePaint.setStrokeJoin(Paint.Join.ROUND);
    insidePaint.setStrokeCap(Paint.Cap.ROUND);

    surfaceForPaint = new Paint();
    // surfaceForPaint.setColor(0x66666666);
    surfaceForPaint.setColor( TDColor.ORANGE );
    // surfaceForPaint.setStyle(Paint.Style.FILL);
    surfaceForPaint.setStyle(Paint.Style.STROKE);
    surfaceForPaint.setStrokeJoin(Paint.Join.ROUND);
    surfaceForPaint.setStrokeCap(Paint.Cap.ROUND);

    surfaceBackPaint = new Paint();
    surfaceBackPaint.setColor( TDColor.BROWN );
    surfaceBackPaint.setStyle(Paint.Style.FILL);
    // surfaceBackPaint.setStyle(Paint.Style.STROKE);
    surfaceBackPaint.setStrokeJoin(Paint.Join.ROUND);
    surfaceBackPaint.setStrokeCap(Paint.Cap.ROUND);

    // areaPaint = new Paint();
    // areaPaint.setColor(0x99cc6633);
    // areaPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    // areaPaint.setStrokeJoin(Paint.Join.ROUND);
    // areaPaint.setStrokeCap(Paint.Cap.ROUND);

    vertexPaint = new Paint();
    vertexPaint.setDither(true);
    vertexPaint.setColor( TDColor.BACK_GREEN );
    vertexPaint.setStyle(Paint.Style.FILL);
    vertexPaint.setStrokeJoin(Paint.Join.ROUND);
    vertexPaint.setStrokeCap(Paint.Cap.ROUND);

    backVertexPaint = new Paint();
    backVertexPaint.setDither(true);
    backVertexPaint.setColor( TDColor.BACK_VIOLET );
    backVertexPaint.setStyle(Paint.Style.STROKE);
    backVertexPaint.setStrokeJoin(Paint.Join.ROUND);
    backVertexPaint.setStrokeCap(Paint.Cap.ROUND);

    setStrokeWidths();
  }

  private void setStrokeWidths()
  {
    whitePaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    // previewPaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    // topLinePaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    // sideLinePaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    borderLinePaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    insidePaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    surfaceForPaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    surfaceBackPaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    // areaPaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    vertexPaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    backVertexPaint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
  }
}
