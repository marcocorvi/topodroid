/** @file DialogIco.java
 *
 * @author marco corvi
 * @date jan 2012
 *
 * @brief Cave3D Ico dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;

import java.util.Locale;

import java.io.StringWriter;
import java.io.PrintWriter;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.Paint.Style;

import android.app.Dialog;
import android.os.Bundle;

// import android.widget.Toast;

import android.content.Context;

import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View.OnTouchListener;
import android.view.View;
import android.view.MotionEvent;

import android.util.DisplayMetrics;

class DialogIco extends MyDialog
                implements OnTouchListener
{
  private static int SIDE  = 180;
  private static int CX = SIDE/2;
  private static int CY = SIDE/2;
  private static int RADIUS = SIDE/2 - 10;

  int mNr;
  private double mTheta; // clino: theta=0 --> +90, theta=PI/2 --> 0, theta=PI --> -90
  private double mPhi;   // azimuth: phi=0 --> North, phi=PI/2 --> East

  private ImageView mImage;
  private TextView mText;
  private IcoDiagram diagram;
  private double mMax;

  double n1x, n1y, n1z;
  double n2x, n2y, n2z;
  double n3x, n3y, n3z;

  Paint green, cyan, red;

  public DialogIco( Context context, TglParser parser )
  {
    super( context, R.string.DialogIco );

    DisplayMetrics dm = context.getResources().getDisplayMetrics();
    // double density  = dm.density;
    // mDisplayWidth  = dm.widthPixels;
    // mDisplayHeight = dm.heightPixels;
    SIDE = dm.widthPixels;
    CX = SIDE/2;
    CY = SIDE/2;
    RADIUS = (SIDE - 20)/2;

    mTheta  = Math.PI/2.0;
    mPhi    = 0.0;
    prepareIcoDiagram( parser );

    green = new Paint();
    green.setARGB( 0xff, 0, 0xff, 0 );
    green.setStyle( Paint.Style.FILL_AND_STROKE );
    green.setStrokeWidth( 4 );
    cyan = new Paint();
    cyan.setARGB( 0xff, 0, 0xff, 0xff );
    cyan.setStyle( Paint.Style.FILL_AND_STROKE );
    cyan.setStrokeWidth( 4 );
    red = new Paint();
    red.setARGB( 0xff, 0xff, 0, 0xff );
    red.setStyle( Paint.Style.FILL_AND_STROKE );
    red.setStrokeWidth( 4 );
  }

  private void computeNVectors( )
  {
    double ct = Math.cos(mTheta);
    double st = Math.sin(mTheta);
    double cp = Math.cos(mPhi);
    double sp = Math.sin(mPhi);

    // x = East   y = North     z = Up
    n1x =  cp;    n1y = -sp;    n1z = 0;   // V1: vector in the horizontal plane orthogonal to the view V3=(sp*cg, cp*cg, sg )
                                           // g = PI/2 - t therefore cg = st, sg = ct 
                                           // V3 = ( sp*st, cp*st, ct )
    n2x = -ct*sp; n2y = -ct*cp; n2z = st;  // V2 = V1 ^ V3 = ( cp, -sp, 0 ) ^ ( sp*st, cp*st, ct ) = ( -sp*ct, -(cp*ct), cp*cp*st - (-sp)*sp*st )
    n3x = st*sp;  n3y = st*cp;  n3z = ct;  // V3  
                                           // East = (1,0,0) projects to (n1x, n2x)
                                           // North = (0,1,0) projects to (n1x, n2y)
                                           // Vert = (0,0,1) projects to (n1z, n2z)

    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format(Locale.US, 
      mContext.getResources().getString( R.string.viewing_at ), // "Viewing at clino %.0f azimuth %.0f",
      90 - mTheta*180/Math.PI, 
      mPhi*180/Math.PI );
    mText.setText( sw.getBuffer().toString() );
  }

  private void render()
  { 
    Bitmap bitmap = Bitmap.createBitmap( SIDE, SIDE, Bitmap.Config.ARGB_8888 );
    mImage.setImageBitmap( bitmap );
    computeNVectors();
    evalIcoDiagram( new Canvas( bitmap ) );
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    initLayout( R.layout.cave3d_diagram, -1 ); // -1 = Window.FEATURE_NO_TITLE

    mText  = (TextView) findViewById( R.id.viewpoint );
    mImage = (ImageView) findViewById( R.id.image );
    mImage.setOnTouchListener( this );
    render();
  }


  private void prepareIcoDiagram( TglParser parser )
  { 
    mNr = 8;
    diagram = new IcoDiagram( mNr );
    double eps = diagram.mEps;
    int ns = parser.getShotNumber();
    diagram.reset();
    for (int k=0; k<ns; ++k ) {
      Cave3DShot shot = parser.getShot( k );
      diagram.add( shot.len, shot.ber, shot.cln, eps ); // angles in radians
    }
    mMax = diagram.maxValue();
    // TDLog.v( "TopoGL eps " + eps + " max " + mMax );
  }

  private void evalIcoDiagram( Canvas canvas )
  { 
    for ( int k=0; k<diagram.mPointNr; ++k ) {
      IcoPoint p = diagram.getDirection( k );
      
      double x = (n1x * p.x + n1y * p.y + n1z * p.z)/IcoPoint.R;
      double y = (n2x * p.x + n2y * p.y + n2z * p.z)/IcoPoint.R;

      double v = diagram.mValue[k] / mMax;
      float dx = (float)(  v * RADIUS * x);
      float dy = (float)(- v * RADIUS * y); // north is upward, but screen Y is downward
      int col = (int)(0xff * v);
      // TDLog.v( "TopoGL path to " + dx + " " + dy );
      Path path = new Path();
      path.moveTo( CX, CY );
      path.lineTo( CX + dx + dy/20, CY + dy - dx/20 );
      path.lineTo( CX + dx - dy/20, CY + dy + dx/20 );
      path.close();
      Paint paint = new Paint();
      paint.setARGB( 0xbb, col, col, col );
      paint.setStyle( Paint.Style.FILL );
      paint.setStrokeWidth( 2 );
      canvas.drawPath( path, paint );
    } 
    Path east = new Path();
    east.moveTo( CX, CY );
    east.lineTo( CX + (float)(n1x*400), CY - (float)(n2x*400) );
    canvas.drawPath( east, green );

    Path north = new Path();
    north.moveTo( CX, CY );
    north.lineTo( CX + (float)(n1y*400), CY - (float)(n2y*400) );
    canvas.drawPath( north, cyan );

    Path vert = new Path();
    vert.moveTo( CX, CY );
    vert.lineTo( CX + (float)(n1z*400), CY - (float)(n2z*400) );
    canvas.drawPath( vert, red );
    
    
  }

  double mSaveX, mSaveY;

  @Override
  public boolean onTouch( View v, MotionEvent e )
  {
    if ( e.getAction() == MotionEvent.ACTION_DOWN ) {
      mSaveX = e.getX();
      mSaveY = e.getY();
      return true;
    } else if ( e.getAction() == MotionEvent.ACTION_MOVE ) {
      return true;
    } else if ( e.getAction() == MotionEvent.ACTION_UP ) {
      double x = (e.getX() - mSaveX)/SIDE;
      double y = (e.getY() - mSaveY)/SIDE;
      changeThetaPhi( x, y );
      render();
      return true;
    }
    return false;
  }

  private void changeThetaPhi( double x, double y )
  {
    mTheta += y;
    if ( mTheta < Math.PI/2 ) mTheta = Math.PI/2;
    if ( mTheta > Math.PI ) mTheta = Math.PI;
    mPhi += x;
    if ( mPhi < 0 ) mPhi += 2*Math.PI;
    if ( mPhi > 2*Math.PI ) mPhi -= 2*Math.PI;
  }
   
}
