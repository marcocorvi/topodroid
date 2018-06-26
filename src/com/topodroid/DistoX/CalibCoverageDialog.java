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

// import android.util.Log;

class CalibCoverageDialog extends MyDialog
                          implements View.OnClickListener
{
  private class Direction
  {
    float mCompass;
    float mClino;
    float mValue;

    Direction( float cm, float cl, float v )
    {
      mCompass = cm;
      mClino = cl;
      mValue = v;
    }
  }

  private static final int STEP_Y      =   1; // 180 / HEIGHT
  private static final int DIM_Y       =  37;
  private static final int DIM_Y2      =  18; // (DIM_Y-1)/2
  private static final int DELTA_Y     =   5;
  private static final float DELTA_YF  =   5.0f;
  private static final int WIDTH       = 180;
  private static final int HEIGHT      = 180;
  private static final int AZIMUTH_BIT =  32;
  private static final int DELTA_W     =  20;

  private int[] clino_angles;
  private int[] t_size;
  private int[] t_offset;
  private int t_dim;
  private Direction angles[];
  private float mCoverage;

  private Bitmap mBitmapUp;
  private Bitmap mBitmapDown;
  private List<CalibCBlock> mList;
  private CalibAlgo mCalib; // calibration algorithm

  private TextView mText;
  private ImageView mImageUp;
  private ImageView mImageDown;
  private Button mBtnEval;
  private Button mBtnEvalG;
  private Button mBtnEvalM;
  private Button mBtnEvalCal;
  // private Button mBtnBack;

  CalibCoverageDialog( Context context, List< CalibCBlock > list, CalibAlgo cal )
  {
    super( context, R.string.CalibCoverageDialog );
    
    mCalib = cal;
    mList  = list;
    clino_angles = new int[ DIM_Y ];
    t_size       = new int[ DIM_Y ];
    t_offset     = new int[ DIM_Y ];
    mBitmapUp   = Bitmap.createBitmap( WIDTH+DELTA_W+1, HEIGHT+1, Bitmap.Config.ARGB_8888 );
    mBitmapDown = Bitmap.createBitmap( WIDTH+DELTA_W+1, HEIGHT+1, Bitmap.Config.ARGB_8888 );
    setup();
    evalCoverage( mList,  null );
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
    mText.setText( String.format(Locale.US, "%.2f", mCoverage ) );
  }

  @Override
  public void onClick(View v) 
  {
    Button btn = (Button)v;
    if ( btn == mBtnEval ) {
      evalCoverage( mList, null );
      fillImage();
      reset();
    } else if ( btn == mBtnEvalG ) {
      evalCoverageGM( mList, 0 );
      fillImage();
      reset();
    } else if ( btn == mBtnEvalM ) {
      evalCoverageGM( mList, 1 );
      fillImage();
      reset();
    } else if ( btn == mBtnEvalCal ) {
      if ( mCalib.GetAG() != null ) {
        evalCoverage( mList, mCalib );
        fillImage();
        reset();
      } else {
        TDToast.make( mContext, R.string.no_calib );
      }
    } else {
      dismiss();
    }
  }

  private void setup()
  {
    int i;
    for ( i=0; i<DIM_Y; ++i ) { // clino angles: from +90 to -90
      clino_angles[i] = 90 - DELTA_Y*i;
    }
    t_size[ 0 ] = t_size[DIM_Y-1] = 1;
    for ( i=1; i<DIM_Y2; ++i ) {
      t_size[i] = t_size[DIM_Y-1-i] = AZIMUTH_BIT * i;
    }
    t_size[ DIM_Y2 ] = AZIMUTH_BIT * DIM_Y2; // max azimuth steps 54 at clino 0

    t_offset[0] = 0;
    for ( i=1; i<DIM_Y; ++i ) {
      t_offset[i] = t_offset[i-1] + t_size[i-1];
      // Log.v("DistoX", "J " + i + " off " + t_offset[i] + " size " + t_size[i] );
    }
    t_dim = t_offset[DIM_Y-1] + t_size[DIM_Y-1];
    // Log.v("DistoX", "dim " + t_dim );

    angles = new Direction [ t_dim ];
    for (int k = 0; k<DIM_Y; ++k ){
      float clino = clino_angles[k] * TDMath.DEG2RAD;
      for (int j=t_offset[k]; j<t_offset[k]+t_size[k]; ++j ) {
        angles[j] = new Direction(
                      TDMath.M_PI + ( TDMath.M_2PI * (j - t_offset[k]) ) / t_size[k],
                      clino, 
                      1.0f );
      }
    }
  }

  // compass and clino in radians
  private float cosine( float compass1, float clino1, float compass2, float clino2 )
  {
    double h1 = Math.cos( clino1 );
    double z1 = Math.sin( clino1 );
    double x1 = h1 * Math.cos( compass1 );
    double y1 = h1 * Math.sin( compass1 );
    double h2 = Math.cos( clino2 );
    double z2 = Math.sin( clino2 );
    double x2 = h2 * Math.cos( compass2 );
    double y2 = h2 * Math.sin( compass2 );
    return (float)(x1*x2 + y1*y2 + z1*z2); // cosine of the angle
  }

  private void updateDirections( float compass, float clino, int cnt )
  {
    for (int j=0; j<t_dim; ++j ) {
      float c = cosine( compass, clino, angles[j].mCompass, angles[j].mClino );
      if ( c > 0.0 ) {
        c = c * c;
        angles[j].mValue -= (cnt >= 4)? c*c : c*c * cnt * 0.25f;
        if ( angles[j].mValue < 0.0f ) angles[j].mValue = 0.0f;
      }
    }
  }

  private void evalCoverage( List<CalibCBlock> clist, CalibAlgo transform )
  {
    for (int j=0; j<t_dim; ++j ) angles[j].mValue = 1.0f;

    long old_grp = 0;
    float compass_avg = 0.0f;
    float clino_avg   = 0.0f;
    int cnt_avg = 0;
    for ( CalibCBlock b : clist ) {
      if ( b.mGroup == 0 ) continue;
      if ( transform == null ) {
        b.computeBearingAndClino( );
      } else {
        b.computeBearingAndClino( transform );
      }
      float compass = b.mBearing * TDMath.DEG2RAD;
      float clino   = b.mClino   * TDMath.DEG2RAD;
      if ( b.mGroup == old_grp ) {
        if ( cnt_avg > 0 && Math.abs( compass - compass_avg / cnt_avg ) > 1.5f * TDMath.M_PI ) {
          if ( compass > TDMath.M_PI ) {
            compass -= TDMath.M_2PI; // average around 0
          } else {
            compass += TDMath.M_2PI; // average around 360
          }
        }
        clino_avg   += clino;
        compass_avg += compass;
        cnt_avg     ++;
      } else {
        if ( cnt_avg > 0 ) {
          compass_avg /= cnt_avg;
          clino_avg   /= cnt_avg;
          updateDirections( compass_avg, clino_avg, cnt_avg );
        }
        clino_avg   = clino;
        compass_avg = compass;
        cnt_avg     = 1;
        old_grp     = b.mGroup;
      }
    }
    if ( cnt_avg > 0 ) {
      compass_avg /= cnt_avg;
      clino_avg   /= cnt_avg;
      updateDirections( compass_avg, clino_avg, cnt_avg );
    }

    mCoverage = 0.0f;
    for (int j=0; j<t_dim; ++j ) {
      mCoverage += angles[j].mValue;
    }
    mCoverage = 100.0f * ( 1.0f - mCoverage/t_dim );
  }

  // @param clist  list of CBlocks
  // @param mode   0: G,  1: M
  private void evalCoverageGM( List<CalibCBlock> clist, int mode ) 
  {
    for (int j=0; j<t_dim; ++j ) angles[j].mValue = 1.0f;

    long old_grp = 0;
    float compass_avg = 0.0f;
    float clino_avg   = 0.0f;
    int cnt_avg = 0;

    float f = TopoDroidUtil.FV;
    for ( CalibCBlock b : clist ) {
      if ( b.mGroup <= 0 ) continue;
      Vector v = ( mode == 0 )? new Vector( b.gx/f, b.gy/f, b.gz/f ) : new Vector( b.mx/f, b.my/f, b.mz/f );
      float compass = TDMath.atan2( v.x, v.y ); if ( compass < 0 ) compass += TDMath.M_2PI;
      float clino   = TDMath.atan2( v.z, TDMath.sqrt( v.x * v.x + v.y * v.y ) );
      updateDirections( compass, clino, 1 );
    }
    mCoverage = 0.0f;
    for (int j=0; j<t_dim; ++j ) {
      mCoverage += angles[j].mValue;
    }
    mCoverage = 100.0f * ( 1.0f - mCoverage/t_dim );
  }

  // private void fillImageOld( ) // image is 90 * 180 * 4
  // {
  //   for (int j0=0; j0<HEIGHT; ++j0) {
  //     int j = 2 * j0;
  //     float clino = j - 90.0f;
  //     int j1 = j/10;
  //     int j2 = j1 + 1;
  //     float d = (j%10)/10.0f;
  //     int j1off = t_offset[j1];
  //     int j2off = t_offset[j2];
  //     float amax = 180.0f * TDMath.sqrt( 1 - (clino/90.0f)*(clino/90.0f) );
  //     // if ( amax < 1.0 ) amax = 1.0;
  //     int ioff = (180 - (int)(amax)) / 2;
  //     if (ioff < 0 ) ioff = 0;
  //     int ixold = -1;
  //     for (int i0=0; i0<WIDTH; ++i0) {
  //       int i = 2 * i0;
  //       float compass = ((i + 180)%360); // N middle, W left, E right
  //       int ix = (int)(compass / 180.0f * amax);  // from 0 to 2*amax
  //       ix /= 2;
  //       if ( ix == ixold ) continue;
  //       ixold = ix;
  //       float c1 = compass/360.0f * t_size[j1];
  //       float c2 = compass/360.0f * t_size[j2];
  //       int i11 = (int)(c1); // index in [0, t_size)
  //       int i21 = (int)(c2);
  //       int i12 = (i11 + 1)%t_size[j1];
  //       float d1 = c1 - i11;
  //       int i22 = (i21 + 1)%t_size[j2];
  //       float d2 = c2 - i21;
  //       float v1 = angles[j1off+i11].mValue * (1-d1) + angles[j1off+i12].mValue * d1;
  //       float v2 = angles[j2off+i21].mValue * (1-d2) + angles[j2off+i22].mValue * d2;
  //       float v = v1 * (1-d) + v2 * d;
  //       // int off = (j0*WIDTH + (ioff + ix))*BYTES;
  //       int green = ( v > 254 )? 254 : (int)(254*v);
  //       int red   = 0xff - green;
  //       int col = 0xff000000 | (red << 8 ) | (green << 16);
  //       mBitmap.setPixel( ioff+ix, j0, col );
  //     }
  //   }
  // }


  private void fillImage( ) // image is 90 * 180 * 4
  {
    int H1 = HEIGHT;     // 180
    float H2 = H1 / 2;   // 90
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
	  int j1 = iclino / DELTA_Y;   // range 0..8
	  int j2 = j1 + 1;              //     d
          float d = (iclino%DELTA_Y)/DELTA_YF;  // j1 ------ iclino ------------ j2
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
	  j1 = DIM_Y - 1 - j1;  // range 18 .. 0
	  j2 = DIM_Y - 1 - j2;
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
