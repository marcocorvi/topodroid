/** @file DialogRose.java
 *
 * @author marco corvi
 * @date jan 2012
 *
 * @brief Cave3D Rose dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.DistoX.R;

// import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Paint;

import android.app.Dialog;
import android.os.Bundle;

// import android.widget.Toast;

import android.content.Context;
import android.util.DisplayMetrics;

import android.widget.ImageView;
// import android.view.View;

class DialogRose extends Dialog
{
  private static int SIDE  = 180;
  private static int CX = SIDE/2;
  private static int CY = SIDE/2;
  private static int RADIUS = SIDE/2 - 10;

  int mNr;

  private Bitmap mBitmap;
  private Canvas mCanvas;

  private ImageView mImage;
  private Context mContext;

  public DialogRose( Context context, TglParser parser )
  {
    super( context );
    mContext = context;

    DisplayMetrics dm = context.getResources().getDisplayMetrics();
    // double density  = dm.density;
    // mDisplayWidth  = dm.widthPixels;
    // mDisplayHeight = dm.heightPixels;
    SIDE = dm.widthPixels;
    CX = SIDE/2;
    CY = SIDE/2;
    RADIUS = (SIDE - 20)/2;

    mBitmap = Bitmap.createBitmap( SIDE, SIDE, Bitmap.Config.ARGB_8888 );
    mCanvas = new Canvas( mBitmap );
    evalRoseDiagram( parser );
    // fillImage();
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    setContentView( R.layout.cave3d_diagram );
    mImage = (ImageView) findViewById( R.id.image );
    reset();
  }

  private void reset()
  {
    mImage.setImageBitmap( mBitmap );
  }

  private void evalRoseDiagram( TglParser parser )
  {
    mNr = 72;
    double del = Math.PI/mNr;       // 1/2 * 2*PI / N
    double eps = 4.0 * Math.PI/mNr; // 2 * 2*PI / N
    RoseDiagram rd = new RoseDiagram( mNr );
    int ns = parser.getShotNumber();
    rd.reset();
    for (int k=0; k<ns; ++k ) {
      Cave3DShot shot = parser.getShot( k );
      rd.add( shot.len, shot.ber, shot.cln, eps ); // angles in radians
    }
    double max = rd.maxValue();
    for ( int k=0; k<mNr; ++k ) {
      double a = k * Math.PI / (mNr/2.0);
      double a1 = a - del;
      double a2 = a + del;
      double ca1 = Math.cos( a1 );
      double sa1 = Math.sin( a1 );
      double ca2 = Math.cos( a2 );
      double sa2 = Math.sin( a2 );
      double v = rd.mValue[k] / max;
      float x1 = (float)(CX + v * RADIUS * sa1);
      float y1 = (float)(CY - v * RADIUS * ca1);
      float x2 = (float)(CX + v * RADIUS * sa2);
      float y2 = (float)(CY - v * RADIUS * ca2);
      int col = (int)(0xff * v);
      // fill path CX,CY x1,y1, x2,y2
      Path path = new Path();
      path.moveTo( CX, CY );
      path.lineTo( x2, y2 );
      path.lineTo( x1, y1 );
      path.close();
      Paint paint = new Paint();
      paint.setARGB( 0xcc, col, col, col );
      paint.setStyle( Paint.Style.FILL );
      mCanvas.drawPath( path, paint );
    } 
  }
}
