/* @file SurveyStatDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey stats display dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;
import android.content.res.Resources;

import android.graphics.*;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ImageView;

class SurveyStatDialog extends MyDialog
                              implements View.OnClickListener
{
  private ImageView histG;
  private ImageView histM;
  private ImageView histD;
 
  private SurveyStat mStat;

  // private Button mBtnBack;

  SurveyStatDialog( Context context, SurveyStat stat )
  {
    super( context, null, R.string.SurveyStatDialog ); // null app
    mStat = stat;
    // TDLog.Log(TDLog.LOG_STAT, "SurveyStat cstr");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
      super.onCreate(savedInstanceState);

      initLayout( R.layout.survey_stat_dialog, R.string.survey_info );

      Resources res = mContext.getResources();
      float unit = TDSetting.mUnitLength;
      String unit_str = TDSetting.mUnitLengthStr;

      // TDLog.Log(TDLog.LOG_STAT, " SurveyStat onCreate");
      TextView mTextLeg       = (TextView) findViewById(R.id.stat_leg);
      TextView mTextDuplicate = (TextView) findViewById(R.id.stat_duplicate);
      TextView mTextSurface   = (TextView) findViewById(R.id.stat_surface);
      TextView mTextSplay     = (TextView) findViewById(R.id.stat_splay);
      TextView mTextStation   = (TextView) findViewById(R.id.stat_station);
      TextView mTextLoop      = (TextView) findViewById(R.id.stat_loop);
      TextView mTextComponent = (TextView) findViewById(R.id.stat_component);
      TextView mTextStddevM   = (TextView) findViewById(R.id.stat_stddev_m);
      TextView mTextStddevG   = (TextView) findViewById(R.id.stat_stddev_g);
      TextView mTextStddevD   = (TextView) findViewById(R.id.stat_stddev_dip);
      TextView mTextDevices   = (TextView) findViewById(R.id.stat_devices);
      TextView mTextDates     = (TextView) findViewById(R.id.stat_dates);

      histG = (ImageView) findViewById( R.id.histogramG );
      histM = (ImageView) findViewById( R.id.histogramM );
      histD = (ImageView) findViewById( R.id.histogramD );
      float g = mStat.averageG*TDSetting.mAccelerationThr/2000; // 2000 = 20 * 100 ie percent/20
      float m = mStat.averageM*TDSetting.mMagneticThr/2000;
      float d = TDSetting.mDipThr/20;
      // TDLog.v( "G " + g + " M " + m + " D " + d );
      histG.setImageBitmap( makeHistogramBitmap( mStat.G, mStat.nrMGD, mStat.averageG, g, 400, 100, 40, TDColor.BLUE ) );
      histM.setImageBitmap( makeHistogramBitmap( mStat.M, mStat.nrMGD, mStat.averageM, m, 400, 100, 40, TDColor.BLUE ) );
      histD.setImageBitmap( makeHistogramBitmap( mStat.D, mStat.nrMGD, mStat.averageD, d, 400, 100, 40, TDColor.BLUE ) );

      // mBtnBack = (Button) findViewById(R.id.btn_back);
      // mBtnBack.setOnClickListener( this );
      ( (Button) findViewById(R.id.btn_back) ).setOnClickListener( this );

      mTextLeg.setText( String.format( res.getString(R.string.stat_leg),
                        mStat.countLeg, mStat.lengthLeg * unit, mStat.extLength * unit, mStat.planLength * unit, unit_str ) );
      mTextDuplicate.setText( String.format( res.getString(R.string.survey_stat_duplicate),
                        mStat.countDuplicate, mStat.lengthDuplicate * unit, unit_str ) );
      mTextSurface.setText( String.format( res.getString(R.string.survey_stat_surface),
                        mStat.countSurface, mStat.lengthSurface * unit, unit_str ) );
      mTextSplay.setText( String.format( res.getString(R.string.survey_stat_splay), mStat.countSplay ) );
      mTextStation.setText( String.format( res.getString(R.string.survey_stat_station), mStat.countStation ) );
      mTextLoop.setText( String.format( res.getString(R.string.stat_cycle), mStat.countLoop ) );
      mTextComponent.setText( String.format( res.getString(R.string.stat_component), mStat.countComponent ) );

      mTextStddevM.setText( String.format( res.getString(R.string.stat_stddev_m), mStat.stddevM ) );
      mTextStddevG.setText( String.format( res.getString(R.string.stat_stddev_g), mStat.stddevG ) );
      mTextStddevD.setText( String.format( res.getString(R.string.stat_stddev_dip), mStat.stddevD, mStat.averageD ) );

      mTextDevices.setText( String.format( res.getString(R.string.stat_devices), mStat.deviceNr, mStat.deviceCnt ) );
      mTextDates.setText( String.format( res.getString(R.string.stat_dates), TDUtil.millisToDate( mStat.minMillis * 1000 ), TDUtil.millisToDate( mStat.maxMillis * 1000 ) ) );
  }

    @Override
    public void onClick(View view)
    {
      // Button b = (Button)view;
      // if ( b == mBtnBack ) {
      //   /* nothing */
      // }
      dismiss();
    }

  /** make the bitmap of the histogram of values
   * @param vals   values
   * @param nr     number of values
   * @param ave    average value
   * @param std    std dev
   * @param width  bitmap width
   * @param height bitmap height
   * @param bin    half histogram bins
   * @param col    color
   */
  static private Bitmap makeHistogramBitmap( float[] vals, int nr, float ave, float std, int width, int height, int bin, int col )
  {
    Bitmap bitmap = Bitmap.createBitmap( width+20, height+20, Bitmap.Config.ARGB_8888 );
    int ww = bitmap.getWidth();
    int hh = bitmap.getHeight();
    for ( int j=0; j<hh; ++j ) {
      for ( int i=0; i<ww; ++i ) bitmap.setPixel( i, j, 0 );
    }

    // histogram from -bin*std to +bin*std in step std
    int b_bin = 2*bin + 1; // 20230118 local var
    int[] hist = new int[b_bin];
    for ( int k=0; k<b_bin; ++k ) hist[k] = 0;
    if ( vals != null && nr > 0 ) {
      float aave = 0;
      for ( int k=0; k < nr; ++ k ) aave += vals[k];
      aave /= nr;
      float bave = 0;
      int bnr = 0;
      for ( int k=0; k < nr; ++ k ) {
        int i = bin + (int)( (vals[k]-aave)/std );
        if ( i >= 0 && i < b_bin ) {
          bnr ++;
          bave += vals[k];
        }
      }
      if ( bnr > 0 ) bave /= bnr;

      for ( int k=0; k < nr; ++ k ) {
        int i = bin + (int)( (vals[k]-bave)/std );
        if ( i >= b_bin ) i = b_bin-1;
        if ( i < 0 ) i = 0;
        if ( i >= 0 && i < b_bin ) {
          ++ hist[i];
        }
      }
    }
    int max = 1; // histogram max
    for ( int k=0; k<b_bin; ++k ) if ( hist[k] > max ) max = hist[k];

    int joff = hh-10;
    int dx   = ww / b_bin;
    if ( dx*20 >= ww ) dx --;
    int x, y;
    for ( int k=0; k<b_bin; ++ k ) {
      int brd = ( k == bin )? TDColor.FIXED_RED : TDColor.WHITE;
      int h = (joff * hist[k]) / max;
      x  = dx * k;
      for ( y=joff-h; y <= joff; ++y ) bitmap.setPixel( x, y, brd );
      int x2 = x  + dx-1;
      for ( ++x; x < x2; ++ x ) {
        y = joff-h;
        bitmap.setPixel( x, y, brd );
        for ( ++y; y < joff; ++y ) bitmap.setPixel( x, y, col );
        bitmap.setPixel( x, y, brd );
      }
      for ( y=joff-h; y <= joff; ++y ) bitmap.setPixel( x, y, brd );
    }
    for ( y = 0; y < hh; ++y ) {
      bitmap.setPixel( (bin/2)*dx, y, TDColor.YELLOW );
      bitmap.setPixel( (bin+1+bin/2)*dx, y, TDColor.YELLOW );
    }
    for ( x = 0; x < ww; ++x ) bitmap.setPixel( x, joff, TDColor.FIXED_RED );
    // for ( int k = 5; k <= bin; k+=5 ) {
    //   x  = ioff + dx * k;
    //   int yy = hh - ( ((k%10) == 0 )? 0 : 5 );
    //   for ( y = joff; y < yy; ++y ) bitmap.setPixel( x, y, TDColor.FIXED_RED );
    // }
    // if ( 5  <= bin ) {
    //   x  = ioff + dx * 5;
    //   for ( y = 0; y < joff; ++y ) bitmap.setPixel( x, y, TDColor.YELLOW );
    // }
    // if ( 10  <= bin ) {
    //   x  = ioff + dx * 10;
    //   for ( y = 0; y < joff; ++y ) bitmap.setPixel( x, y, TDColor.FIXED_RED );
    // }
    // for ( int k = 10; ; k += 10 ) {
    //   y = joff - step * k;
    //   if ( y < 0 ) break;
    //   for ( x = 5; x < ioff; ++x ) bitmap.setPixel( x, y, TDColor.FIXED_RED );
    // }
      
    // TDLog.v( "fill image done");
    return bitmap;
  }
}
        

