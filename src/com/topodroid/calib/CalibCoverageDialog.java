/* @file CalibCoverageDialog.java
 *
 * @author marco corvi
 * @date jan 2012
 *
 * @brief TopoDroid calibration data distribution display
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.R;
import com.topodroid.TDX.TDToast;

import java.util.List;
import java.util.Locale;

import android.graphics.Bitmap;
// import android.graphics.Bitmap.Config;

// import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

public class CalibCoverageDialog extends MyDialog
                          implements View.OnClickListener
{
  private static final int STEP_Y      =   1; // 180 / HEIGHT
  private static final float DELTA_YF  =   CalibCoverage.DELTA_Y;
  private static final int WIDTH       = 180;
  private static final int HEIGHT      = 180;
  private static final int DELTA_W     =  20; // extra bitmap width 

  private float mCoverageValue;
  private float[] mDeviations;

  private final Bitmap mBitmapUp;
  private final Bitmap mBitmapDown;
  private final List< CBlock > mList;  // list of calibration shots
  private final CalibAlgo mCalib;         // calibration algorithm

  private TextView mText;
  private ImageView mImageUp;
  private ImageView mImageDown;
  private Button mBtnEval;
  private Button mBtnEvalG;
  private Button mBtnEvalM;
  private Button mBtnEvalRoll;
  private Button mBtnEvalCal;
  private Button mBtnBack;

  private CalibCoverage mCoverage;

  public CalibCoverageDialog( Context context, List< CBlock > list, CalibAlgo cal )
  {
    super( context, R.string.CalibCoverageDialog );
    
    mCalib = cal;
    mList  = list;
    mCoverage = new CalibCoverage( );
    mBitmapUp   = Bitmap.createBitmap( WIDTH+DELTA_W+1, HEIGHT+1, Bitmap.Config.ARGB_8888 );
    mBitmapDown = Bitmap.createBitmap( WIDTH+DELTA_W+1, HEIGHT+1, Bitmap.Config.ARGB_8888 );
    mCoverageValue = mCoverage.evalCoverage( list,  null );
    // mDeviations = mCoverage.evalDeviations( list );
    // for ( int k= 180; k>=0; --k ) {
    //   if ( mDeviations[k] > 0 ) {
    //     TDLog.v("COVER" + k + " " + mDeviations[k] );
    //   }
    // }

    fillImage();
  }
  

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    initLayout( R.layout.calib_coverage_dialog, R.string.title_coverage );

    mText  = (TextView) findViewById( R.id.coverage_value );
    mImageUp     = (ImageView) findViewById( R.id.coverage_image_up );
    mImageDown   = (ImageView) findViewById( R.id.coverage_image_down );
    mBtnEval     = (Button) findViewById( R.id.coverage_eval );
    mBtnEvalG    = (Button) findViewById( R.id.coverage_g   );
    mBtnEvalM    = (Button) findViewById( R.id.coverage_m   );
    mBtnEvalRoll = (Button) findViewById( R.id.coverage_roll );
    mBtnEvalCal  = (Button) findViewById( R.id.coverage_eval_cal );
    mBtnBack    = (Button) findViewById( R.id.coverage_back );
    mBtnEval.setOnClickListener( this );
    mBtnEvalG.setOnClickListener( this );
    mBtnEvalM.setOnClickListener( this );
    mBtnEvalRoll.setOnClickListener( this );
    if ( mCalib != null ) {
      mBtnEvalCal.setOnClickListener( this );
    } else {
      mBtnEvalCal.setVisibility( View.GONE );
    }
    mBtnBack.setOnClickListener( this );
    reset( R.string.cover_data );
  }

  private void reset( int res )
  {
    mImageUp.setImageBitmap( mBitmapUp );
    mImageDown.setImageBitmap( mBitmapDown );
    String format = mContext.getResources().getString(res);
    // TDLog.v("COVER res " + res + " format " + format );

    mText.setText( String.format(Locale.US, mContext.getResources().getString(res), mCoverageValue ) );
  }

  @Override
  public void onClick(View v) 
  {
    int id = v.getId();
    if ( id == R.id.coverage_eval ) { 
      mCoverageValue = mCoverage.evalCoverage( mList, null );
      fillImage();
      reset( R.string.cover_data );
    } else if ( id == R.id.coverage_g ) {
      mCoverageValue = mCoverage.evalCoverageGM( mList, 0 );
      fillImage();
      reset( R.string.cover_g );
    } else if ( id == R.id.coverage_m ) { 
      mCoverageValue = mCoverage.evalCoverageGM( mList, 1 );
      fillImage();
      reset( R.string.cover_m );
    } else if ( id == R.id.coverage_roll ) { 
      mCoverageValue = mCoverage.evalCoverageRoll( mList, null );
      fillImage();
      reset( R.string.cover_r );
    } else if ( id == R.id.coverage_eval_cal ) {
      if ( mCalib.GetAG() != null ) {
        mCoverageValue = mCoverage.evalCoverage( mList, mCalib );
        fillImage();
        reset( R.string.cover_calib );
      } else {
        TDToast.makeBad( R.string.no_calib );
      }
    } else { // id == R.id. mBtnBack
      dismiss();
    }
  }


  private void fillImage( ) // image is 90 * 180 * 4
  {
    CalibCoverage.Direction[] angles = mCoverage.getDirections();
    int[] t_size   = mCoverage.getTSize();
    int[] t_offset = mCoverage.getTOffset();

    int H1 = HEIGHT;     // 180
    float H2 = (float)H1 / 2;   // 90
    // int W2 = WIDTH / 2 + DELTA_W;  // 180 + 20
    // int j1min = 1000; // DEBUG
    // int j1max = -1000;
    // int upperj2max = 0;
    // int belowj2min = 37;

    for (int j0=0; j0<=H1; ++j0) {
      float j = j0 - H2;  // pixel (i,j) j-index
      for (int i0=0; i0<=H1; ++i0) {
        float i = i0 - H2;  // range -90 .. +90 :pixel (i,j) i-index
	float radius = TDMath.sqrt(i*i + j*j);
	if ( radius < H2 ) { // if pixel is inside the circle
	  int iclino = (int)( (radius) * STEP_Y ); // range [0,90)
          float compass = ( TDMath.atan2( i, -j ) + TDMath.M_PI ) / TDMath.M_2PI; // range [0, 1]
	  // UP HEMISPHERE iliear interpolation:
          //   j2:    i21 - i22    ...  d2
          //          /       \         d
          //   j1:  i11 ----- i12  ...  d1
	  int j1 = iclino / CalibCoverage.DELTA_Y;   // range 0..8
          // if ( j1 < j1min ) j1min = j1;
          // if ( j1 > j1max ) j1max = j1;
	  int j2 = j1 + 1;              //     d
          // if ( j2 > upperj2max ) upperj2max = j2;

          float d = (iclino % CalibCoverage.DELTA_Y) / DELTA_YF;  // j1 ------ iclino ------------ j2
	  int j1off = t_offset[j1];
	  int j2off = t_offset[j2];
	  float c1 = ( compass * t_size[j1] );
	  float c2 = ( compass * t_size[j2] );
          int i11 = (int)(c1) % t_size[j1]; // index in [0, t_size)
          int i21 = (int)(c2) % t_size[j2];
          int i12 = (i11 + 1) % t_size[j1];
          int i22 = (i21 + 1) % t_size[j2];
          float d1 = c1 - i11;
          float d2 = c2 - i21;
	  // if ( j1off+i11 >= t_dim || j1off+i12 >= t_dim ) {
	  //        TDLog.v("COVER OOB north " + i0 + " " + j0 + " J " + j1 + " " + j2 + " I11 " + i11 + " " + i21 );
	  // }
	  // if ( j2off+i21 >= t_dim || j2off+i22 >= t_dim ) {
	  //        TDLog.v("COVER OOB north " + i0 + " " + j0 + " J " + j1 + " " + j2 + " I11 " + i11 + " " + i21 );
	  // }
          float v1 = angles[j1off+i11].getValue() * (1-d1) + angles[j1off+i12].getValue() * d1;
          float v2 = angles[j2off+i21].getValue() * (1-d2) + angles[j2off+i22].getValue() * d2;
          float v = v1 * (1-d) + v2 * d;
          // int off = (j0*WIDTH + (ioff + ix))*BYTES;
          int green = ( v > 254 )? 254 : (int)(254*v);
          int red   = 0xff - green;
          int col = 0xff000000 | (red << 8 ) | (green << 16);
          mBitmapUp.setPixel( i0, j0, col );
	  // DOWN HEMISPHERE
	  j1 = CalibCoverage.DIM_Y - 1 - j1;  // range 18 .. 0
	  j2 = CalibCoverage.DIM_Y - 1 - j2;
          // if ( j2 < belowj2min ) belowj2min = j2;
	  j1off = t_offset[j1];
	  j2off = t_offset[j2];
	  c1 = ( compass * t_size[j1] );
	  c2 = ( compass * t_size[j2] );
          i11 = (int)(c1) % t_size[j1]; // index in [0, t_size)
          i21 = (int)(c2) % t_size[j2];
          i12 = (i11 + 1) % t_size[j1];
          i22 = (i21 + 1) % t_size[j2];
          d1 = c1 - i11;
          d2 = c2 - i21;
	  // if ( j1off+i11 >= t_dim || j1off+i12 >= t_dim ) {
	  //         TDLog.v("COVER OOB south " + i0 + " " + j0 + " J " + j1 + " " + j2 + " I11 " + i11 + " " + i21 );
	  // }
	  // if ( j2off+i21 >= t_dim || j2off+i22 >= t_dim ) {
	  //         TDLog.v("COVER OOB south " + i0 + " " + j0 + " J " + j1 + " " + j2 + " I11 " + i11 + " " + i21 );
	  // }
          v1 = angles[j1off+i11].getValue() * (1-d1) + angles[j1off+i12].getValue() * d1;
          v2 = angles[j2off+i21].getValue() * (1-d2) + angles[j2off+i22].getValue() * d2;
          v = v1 * (1-d) + v2 * d;
          // int off = (j0*WIDTH + (ioff + ix))*BYTES;
          green = ( v > 254 )? 254 : (int)(254*v);
          red   = 0xff - green;
          col = 0xff000000 | (red << 8 ) | (green << 16);
          mBitmapDown.setPixel( i0, j0, col );
        }
      }
    }
    // TDLog.v( " upperJ2min " + upperj2max + " belowJ2max " + belowj2min );
    // TDLog.v( " J1min " + j1min + " J1max " + j1max + " DIM_Y " + CalibCoverage.DIM_Y + " " + CalibCoverage.DIM_Y2 );
    // prints 0 18 37 18 
    // therefore upper hemisphere ranges [ 0, 1] [ 1, 2] ... [18,19]
    //           lower hemisphere ranges [36,35] [35,34] ... [18,17]
  }

}
