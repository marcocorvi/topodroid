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
package com.topodroid.DistoX;

import android.util.Log;

import java.util.List;
import java.util.Locale;

import android.graphics.Bitmap;
// import android.graphics.Bitmap.Config;

// import android.app.Dialog;
import android.os.Bundle;

// import android.widget.Toast;
import android.content.Context;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

class CalibCoverageDialog extends MyDialog
                          implements View.OnClickListener
{
  private static final int STEP_Y      =   1; // 180 / HEIGHT
  private static final float DELTA_YF  =   5.0f;
  private static final int WIDTH       = 180;
  private static final int HEIGHT      = 180;
  private static final int DELTA_W     =  20;

  private float mCoverageValue;
  private float[] mDeviations;

  private Bitmap mBitmapUp;
  private Bitmap mBitmapDown;
  private List<CalibCBlock> mList;  // list of calibration shots
  private CalibAlgo mCalib;         // calibration algorithm

  private TextView mText;
  private ImageView mImageUp;
  private ImageView mImageDown;
  private Button mBtnEval;
  private Button mBtnEvalG;
  private Button mBtnEvalM;
  private Button mBtnEvalCal;
  // private Button mBtnBack;

  private CalibCoverage mCoverage;

  CalibCoverageDialog( Context context, List< CalibCBlock > list, CalibAlgo cal )
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
    //     Log.v("DistoX-COVER", k + " " + mDeviations[k] );
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
    mImageUp   = (ImageView) findViewById( R.id.coverage_image_up );
    mImageDown = (ImageView) findViewById( R.id.coverage_image_down );
    mBtnEval    = (Button) findViewById( R.id.coverage_eval );
    mBtnEvalG   = (Button) findViewById( R.id.coverage_g   );
    mBtnEvalM   = (Button) findViewById( R.id.coverage_m   );
    mBtnEvalCal = (Button) findViewById( R.id.coverage_eval_cal );
    // mBtnBack    = (Button) findViewById( R.id.coverage_back );
    mBtnEval.setOnClickListener( this );
    mBtnEvalG.setOnClickListener( this );
    mBtnEvalM.setOnClickListener( this );
    if ( mCalib != null ) {
      mBtnEvalCal.setOnClickListener( this );
    } else {
      mBtnEvalCal.setVisibility( View.GONE );
    }
    // mBtnBack.setOnClickListener( this );
    reset();
  }

  private void reset()
  {
    mImageUp.setImageBitmap( mBitmapUp );
    mImageDown.setImageBitmap( mBitmapDown );
    mText.setText( String.format(Locale.US, "%.2f", mCoverageValue ) );
  }

  @Override
  public void onClick(View v) 
  {
    Button btn = (Button)v;
    if ( btn == mBtnEval ) {
      mCoverageValue = mCoverage.evalCoverage( mList, null );
      fillImage();
      reset();
    } else if ( btn == mBtnEvalG ) {
      mCoverageValue = mCoverage.evalCoverageGM( mList, 0 );
      fillImage();
      reset();
    } else if ( btn == mBtnEvalM ) {
      mCoverageValue = mCoverage.evalCoverageGM( mList, 1 );
      fillImage();
      reset();
    } else if ( btn == mBtnEvalCal ) {
      if ( mCalib.GetAG() != null ) {
        mCoverageValue = mCoverage.evalCoverage( mList, mCalib );
        fillImage();
        reset();
      } else {
        TDToast.makeBad( R.string.no_calib );
      }
    } else {
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
    for (int j0=0; j0<=H1; ++j0) {
      float j = j0 - H2; 
      for (int i0=0; i0<=H1; ++i0) {
        float i = i0 - H2;  // range -90 .. +90
	float clino = TDMath.sqrt(i*i + j*j);
	if ( clino < H2 ) {
	  int iclino = (int)( (H2 - clino) * STEP_Y ); // range [0,90)
          float compass = ( TDMath.atan2( i, -j ) + TDMath.M_PI ) / TDMath.M_2PI; // range [0, 1]
	  // UP HEMISPHERE
	  int j1 = iclino / CalibCoverage.DELTA_Y;   // range 0..8
	  int j2 = j1 + 1;              //     d
          float d = (iclino%CalibCoverage.DELTA_Y) / DELTA_YF;  // j1 ------ iclino ------------ j2
	  int j1off = t_offset[j1];
	  int j2off = t_offset[j2];
	  float c1 = ( compass * t_size[j1] );
	  float c2 = ( compass * t_size[j2] );
          int i11 = (int)(c1) % t_size[j1]; // index in [0, t_size)
          int i21 = (int)(c2) % t_size[j2];
          int i12 = (i11 + 1) % t_size[j1];
          float d1 = c1 - i11;
          int i22 = (i21 + 1) % t_size[j2];
          float d2 = c2 - i21;
	  // if ( j1off+i11 >= t_dim || j1off+i12 >= t_dim ) {
	  //         Log.v("DistoX", "OOB north " + i0 + " " + j0 + " J " + j1 + " " + j2 + " I11 " + i11 + " " + i21 );
	  // }
	  // if ( j2off+i21 >= t_dim || j2off+i22 >= t_dim ) {
	  //         Log.v("DistoX", "OOB north " + i0 + " " + j0 + " J " + j1 + " " + j2 + " I11 " + i11 + " " + i21 );
	  // }
          float v1 = angles[j1off+i11].mValue * (1-d1) + angles[j1off+i12].mValue * d1;
          float v2 = angles[j2off+i21].mValue * (1-d2) + angles[j2off+i22].mValue * d2;
          float v = v1 * (1-d) + v2 * d;
          // int off = (j0*WIDTH + (ioff + ix))*BYTES;
          int green = ( v > 254 )? 254 : (int)(254*v);
          int red   = 0xff - green;
          int col = 0xff000000 | (red << 8 ) | (green << 16);
          mBitmapUp.setPixel( i0, j0, col );
	  // DOWN HEMISPHERE
	  j1 = CalibCoverage.DIM_Y - 1 - j1;  // range 18 .. 0
	  j2 = CalibCoverage.DIM_Y - 1 - j2;
	  j1off = t_offset[j1];
	  j2off = t_offset[j2];
	  c1 = ( compass * t_size[j1] );
	  c2 = ( compass * t_size[j2] );
          i11 = (int)(c1) % t_size[j1]; // index in [0, t_size)
          i21 = (int)(c2) % t_size[j2];
          i12 = (i11 + 1) % t_size[j1];
          d1 = c1 - i11;
          i22 = (i21 + 1) % t_size[j2];
          d2 = c2 - i21;
	  // if ( j1off+i11 >= t_dim || j1off+i12 >= t_dim ) {
	  //         Log.v("DistoX", "OOB south " + i0 + " " + j0 + " J " + j1 + " " + j2 + " I11 " + i11 + " " + i21 );
	  // }
	  // if ( j2off+i21 >= t_dim || j2off+i22 >= t_dim ) {
	  //         Log.v("DistoX", "OOB south " + i0 + " " + j0 + " J " + j1 + " " + j2 + " I11 " + i11 + " " + i21 );
	  // }
          v1 = angles[j1off+i11].mValue * (1-d1) + angles[j1off+i12].mValue * d1;
          v2 = angles[j2off+i21].mValue * (1-d2) + angles[j2off+i22].mValue * d2;
          v = v1 * (1-d) + v2 * d;
          // int off = (j0*WIDTH + (ioff + ix))*BYTES;
          green = ( v > 254 )? 254 : (int)(254*v);
          red   = 0xff - green;
          col = 0xff000000 | (red << 8 ) | (green << 16);
          mBitmapDown.setPixel( i0, j0, col );
        }
      }
    }
  }

}
