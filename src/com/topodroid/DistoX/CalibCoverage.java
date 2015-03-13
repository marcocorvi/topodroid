/** @file CalibCoverage.java
 *
 * @author marco corvi
 * @date jan 2012
 *
 * @brief TopoDroid calibration data distribution display
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

import android.app.Dialog;
import android.os.Bundle;

import android.widget.Toast;

import android.content.Context;

import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.View;

public class CalibCoverage extends Dialog
                           implements View.OnClickListener
{
  private class Direction
  {
    public float mCompass;
    public float mClino;
    public float mValue;

    public Direction( float cm, float cl, float v )
    {
      mCompass = cm;
      mClino = cl;
      mValue = v;
    }
  }

  private static final int DIMY = 19;
  private static final int WIDTH = 180;
  private static final int HEIGHT = 90;
  private static final int AZIMUTH_BIT = 16;

  private int[] clino_angles;
  private int[] t_size;
  private int[] t_offset;
  private int t_dim;
  private Direction angles[];
  private float mCoverage;

  private Bitmap mBitmap;
  private List<CalibCBlock> mList;
  private Calibration mCalib;

  private TextView mText;
  private ImageView mImage;
  private Button mBtnEval;
  private Button mBtnEvalCal;
  // private Button mBtnBack;
  private Context mContext;

  public CalibCoverage( Context context, List< CalibCBlock > list, Calibration cal )
  {
    super( context );
    mContext = context;
    mCalib = cal;
    mList  = list;
    clino_angles = new int[ DIMY ];
    t_size       = new int[ DIMY ];
    t_offset     = new int[ DIMY ];
    mBitmap = Bitmap.createBitmap( WIDTH, HEIGHT, Bitmap.Config.ARGB_8888 );
    setup();
    evalCoverage( mList,  null );
    fillImage();
  }
  

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );
    setContentView(R.layout.calib_coverage);

    setTitle( mContext.getResources().getString( R.string.title_coverage ) );

    mText  = (TextView) findViewById( R.id.coverage_value );
    mImage = (ImageView) findViewById( R.id.coverage_image );
    mBtnEval    = (Button) findViewById( R.id.coverage_eval );
    mBtnEvalCal = (Button) findViewById( R.id.coverage_eval_cal );
    // mBtnBack    = (Button) findViewById( R.id.coverage_back );
    mBtnEval.setOnClickListener( this );
    mBtnEvalCal.setOnClickListener( this );
    // mBtnBack.setOnClickListener( this );
    reset();
  }

  private void reset()
  {
    mImage.setImageBitmap( mBitmap );
    mText.setText( Float.toString( mCoverage ) );
  }

  public void onClick(View v) 
  {
    Button btn = (Button)v;
    if ( btn == mBtnEval ) {
      evalCoverage( mList, null );
      fillImage();
      reset();
    } else if ( btn == mBtnEvalCal ) {
      if ( mCalib.GetAG() != null ) {
        evalCoverage( mList, mCalib );
        fillImage();
        reset();
      } else {
        Toast.makeText( mContext, R.string.no_calib, Toast.LENGTH_SHORT ).show();
      }
    } else {
      dismiss();
    }
  }

  private void setup()
  {
    int i;
    for ( i=0; i<19; ++i ) { // clino angles: from +90 to -90
      clino_angles[i] = 90 - 10*i;
    }
    t_size[ 0 ] = t_size[18] = 1;
    for ( i=1; i<9; ++i ) {
      t_size[i] = t_size[18-i] = AZIMUTH_BIT * i;
    }
    t_size[ 9 ] = AZIMUTH_BIT * 9; // max azimuth steps 54 at clino 0

    t_offset[0] = 0;
    for ( i=1; i<19; ++i ) {
      t_offset[i] = t_offset[i-1] + t_size[i-1];
    }
    t_dim = t_offset[18] + t_size[18];

    angles = new Direction [ t_dim ];
    for (int k = 0; k<19; ++k ){
      float clino = clino_angles[k] * TopoDroidUtil.GRAD2RAD;
      for (int j=t_offset[k]; j<t_offset[k]+t_size[k]; ++j ) {
        angles[j] = new Direction(
                      TopoDroidUtil.M_PI + ( TopoDroidUtil.M_2PI * (j - t_offset[k]) ) / t_size[k],
                      clino, 
                      1.0f );
      }
    }
  }

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

  private void evalCoverage( List<CalibCBlock> clist, Calibration transform )
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
      float compass = b.mBearing * TopoDroidUtil.GRAD2RAD;
      float clino   = b.mClino   * TopoDroidUtil.GRAD2RAD;
      if ( b.mGroup == old_grp ) {
        if ( cnt_avg > 0 && Math.abs( compass - compass_avg / cnt_avg ) > 1.5f * TopoDroidUtil.M_PI ) {
          if ( compass > TopoDroidUtil.M_PI ) {
            compass -= TopoDroidUtil.M_2PI; // average around 0
          } else {
            compass += TopoDroidUtil.M_2PI; // average around 360
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

  private void fillImage( ) // image is 90 * 180 * 4
  {

    for (int j0=0; j0<HEIGHT; ++j0) {
      int j = 2 * j0;
      float clino = j - 90.0f;
      int j1 = j/10;
      int j2 = j1 + 1;
      float d = (j%10)/10.0f;
      int j1off = t_offset[j1];
      int j2off = t_offset[j2];
      float amax = 180.0f * (float)( Math.sqrt( 1.0 - (clino/90.0f)*(clino/90.0f) ) );
      // if ( amax < 1.0 ) amax = 1.0;
      int ioff = (180 - (int)(amax)) / 2;
      if (ioff < 0 ) ioff = 0;
      int ixold = -1;
      for (int i0=0; i0<WIDTH; ++i0) {
        int i = 2 * i0;
        float compass = ((i + 180)%360); // N middle, W left, E right
        int ix = (int)(compass / 180.0f * amax);  // from 0 to 2*amax
        ix /= 2;
        if ( ix == ixold ) continue;
        ixold = ix;
        float c1 = compass/360.0f * t_size[j1];
        float c2 = compass/360.0f * t_size[j2];
        int i11 = (int)(c1); // index in [0, t_size)
        int i21 = (int)(c2);
        int i12 = (i11 + 1)%t_size[j1];
        float d1 = c1 - i11;
        int i22 = (i21 + 1)%t_size[j2];
        float d2 = c2 - i21;
        float v1 = angles[j1off+i11].mValue * (1-d1) + angles[j1off+i12].mValue * d1;
        float v2 = angles[j2off+i21].mValue * (1-d2) + angles[j2off+i22].mValue * d2;
        float v = v1 * (1-d) + v2 * d;
        // int off = (j0*WIDTH + (ioff + ix))*BYTES;
        int green = ( v > 254 )? 254 : (int)(254*v);
        int red   = 0xff - green;
        int col = 0xff000000 | (red << 8 ) | (green << 16);
        mBitmap.setPixel( ioff+ix, j0, col );
      }
    }
  }
}
