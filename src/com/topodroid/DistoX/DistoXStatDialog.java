/* @file DistoXStatDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid plot stats display dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

import java.util.List;
// import java.util.ArrayList;

import android.os.Bundle;
import android.content.Context;
import android.content.res.Resources;

// import android.graphics.*;
import android.view.View;
// import android.view.View.OnClickListener;

import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.LinearLayout;

import android.widget.ArrayAdapter;

class DistoXStatDialog extends MyDialog
                               implements View.OnClickListener
{
    private DistoXNum mNum;
    private String mOrigin;
    private float mAzimuth;
    private SurveyStat mStat;

    // private Button mBtnBack;

    DistoXStatDialog( Context context, DistoXNum num, String origin, float azimuth, SurveyStat stat )
    {
      super( context, R.string.DistoXStatDialog );
      mNum    = num;
      mOrigin = origin;
      mAzimuth = azimuth;
      mStat   = stat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initLayout( R.layout.distox_stat_dialog, R.string.title_stats );

        Resources res = mContext.getResources();
        float unit = TDSetting.mUnitLength;
        String unit_str = TDSetting.mUnitLengthStr;

        TextView mTextOrigin   = (TextView) findViewById(R.id.text_stat_origin);
        TextView mTextAzimuth  = (TextView) findViewById(R.id.text_stat_azimuth);
        TextView mTextLength   = (TextView) findViewById(R.id.text_stat_length);
        TextView mTextExtLen   = (TextView) findViewById(R.id.text_stat_extlen);
        TextView mTextProjLen  = (TextView) findViewById(R.id.text_stat_projlen);
        TextView mTextWENS     = (TextView) findViewById(R.id.text_stat_wens);
        TextView mTextZminmax  = (TextView) findViewById(R.id.text_stat_zminmax);
        // TextView mTextStations = (TextView) findViewById(R.id.text_stat_stations);
        // TextView mTextShots    = (TextView) findViewById(R.id.text_stat_shots);
        // TextView mTextSplays   = (TextView) findViewById(R.id.text_stat_splays);

        TextView mTextLeg       = (TextView) findViewById(R.id.stat_leg);
        TextView mTextDuplicate = (TextView) findViewById(R.id.stat_duplicate);
        TextView mTextSurface   = (TextView) findViewById(R.id.stat_surface);
        TextView mTextSplay     = (TextView) findViewById(R.id.stat_splay);
        TextView mTextStation   = (TextView) findViewById(R.id.stat_station);
        TextView mTextDangling  = (TextView) findViewById(R.id.stat_dangling);
        TextView mTextLoop      = (TextView) findViewById(R.id.stat_loop);
        TextView mTextComponent = (TextView) findViewById(R.id.stat_component);

        TextView mTextAngleErr  = (TextView) findViewById(R.id.text_stat_angle_error);

        // mNum.shotsNr() = mNum.stationsNr() - mNum.loopsNr()

        mTextLeg.setText( String.format( res.getString(R.string.stat_leg),
          mStat.countLeg, mStat.lengthLeg * unit, mStat.extLength * unit, mStat.planLength * unit, unit_str ) );
        mTextDuplicate.setText( String.format( res.getString(R.string.stat_duplicate),
          mStat.countDuplicate, mNum.duplicateNr(), mStat.lengthDuplicate * unit, unit_str ) );
        mTextSurface.setText( String.format( res.getString(R.string.stat_surface),
          mStat.countSurface, mNum.surfaceNr(), mStat.lengthSurface * unit, unit_str ) );
        mTextSplay.setText( String.format( res.getString(R.string.stat_splay),
          mStat.countSplay, mNum.splaysNr() ) );
        mTextStation.setText( String.format( res.getString(R.string.stat_station),
          mStat.countStation, mNum.stationsNr() ) );

        if ( mNum.unattachedShotsNr() > 0 ) {
          mTextDangling.setText( String.format( res.getString(R.string.stat_dangling),
            mNum.unattachedShotsNr(), mNum.unattachedLength() * unit, unit_str ) );
          mTextDangling.setOnClickListener( this );
        } else {
          mTextDangling.setVisibility( View.GONE );
        }

        if ( mStat.countLoop > 0 ) {
          mTextLoop.setText( String.format( res.getString(R.string.stat_cycle), mStat.countLoop ) );
        } else {
          mTextLoop.setVisibility( View.GONE );
        }

        if ( mStat.countComponent > 1 ) {
          mTextComponent.setText( String.format( res.getString(R.string.stat_component), mStat.countComponent ) );
        } else {
          mTextComponent.setVisibility( View.GONE );
        }

        mTextAngleErr.setText( String.format( res.getString(R.string.stat_angle_error), 
            mNum.angleErrorMean() * TDMath.RAD2DEG, mNum.angleErrorStddev() * TDMath.RAD2DEG ) );

   
        List< String > cls = mNum.getClosures();
	int nr_loop = cls.size();
        if ( nr_loop == 0 ) {
          ((TextView)findViewById( R.id.text_stat_loops )).setText( R.string.loop_none );
        } else {
          ((TextView)findViewById( R.id.text_stat_loops )).setText( String.format( res.getString(R.string.stat_loop), nr_loop ) );
	  LinearLayout list = (LinearLayout) findViewById( R.id.list );
          LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 10, 10, 20, 20 );
	  for ( String cl : cls ) {
            TextView tv = new TextView( mContext );
	    tv.setText( cl );
	    list.addView( tv, lp );
	  }
        }

        // mBtnBack = (Button) findViewById(R.id.btn_back);
        // mBtnBack.setOnClickListener( this );
        ( (Button) findViewById(R.id.btn_back) ).setOnClickListener( this );

        mTextOrigin.setText( String.format( res.getString(R.string.stat_origin), mOrigin ) );

        if ( mAzimuth < 0 ) {
          mTextAzimuth.setVisibility( View.GONE );
        } else {
          mTextAzimuth.setText( String.format( res.getString(R.string.stat_azimuth), mAzimuth ) );
        }

        mTextLength.setText( String.format( res.getString(R.string.stat_length), mNum.surveyLength() * unit, unit_str ) );
        mTextExtLen.setText( String.format( res.getString(R.string.stat_extlen), mNum.surveyExtLen() * unit, unit_str ) );
        mTextProjLen.setText( String.format( res.getString(R.string.stat_projlen), mNum.surveyProjLen() * unit, unit_str ) );
        mTextWENS.setText( String.format( res.getString(R.string.stat_wens),
                                          mNum.surveyWest()  * unit,
                                          mNum.surveyEast()  * unit,
                                          mNum.surveyNorth() * unit,
                                          mNum.surveySouth() * unit,
                                          unit_str
                          ) );
        mTextZminmax.setText( String.format( res.getString(R.string.stat_depth),
                                             mNum.surveyTop()    * unit, unit_str,
                                             mNum.surveyBottom() * unit, unit_str ) );
        // mTextStations.setText(String.format( res.getString(R.string.stat_station), mNum.stationsNr() ) );

        // mTextShots.setText( String.format( res.getString(R.string.stat_shot),
	//   mNum.shotsNr(), mNum.duplicateNr(), mNum.surfaceNr() ) );
          
        // mTextSplays.setText( String.format( res.getString(R.string.stat_splay), mNum.splaysNr() ) );

    }

    @Override
    public void onClick(View view)
    {
      // Button b = (Button)view;
      // if ( b == mBtnBack ) {
      //   /* nothing */
      // }
      if ( view.getId() == R.id.stat_dangling ) {
        dismiss();
        if ( mNum.unattachedShotsNr() > 0 ) {
          // Log.v("DistoXD", "dangling list");
          (new DanglingShotsDialog( mContext, mNum )).show();
        }
        return;
      }
      dismiss();
    }
}
        

