/* @file DistoXStatDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid stats display dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;
import android.content.res.Resources;

import android.graphics.*;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;

import android.widget.ArrayAdapter;

public class DistoXStatDialog extends MyDialog 
                              implements View.OnClickListener
{
    private DistoXNum mNum;
    private String mOrigin;
    private float mAzimuth;
    SurveyStat mStat;

    private TextView mTextOrigin;
    private TextView mTextAzimuth;
    private TextView mTextLength;
    private TextView mTextProjLen;
    private TextView mTextWENS;
    private TextView mTextZminmax;
    private TextView mTextStations;
    private TextView mTextShots;
    private TextView mTextSplays;
    private ListView mList;
    
    private TextView mTextLeg;
    private TextView mTextDuplicate;
    private TextView mTextSurface;
    private TextView mTextSplay;
    private TextView mTextStation;
    private TextView mTextLoop;
    private TextView mTextComponent;
    private TextView mTextAngleErr;


    private Button mBtnBack;

    public DistoXStatDialog( Context context, DistoXNum num, String origin, float azimuth, SurveyStat stat )
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

        mTextOrigin   = (TextView) findViewById(R.id.text_stat_origin);
        mTextAzimuth  = (TextView) findViewById(R.id.text_stat_azimuth);
        mTextLength   = (TextView) findViewById(R.id.text_stat_length);
        mTextProjLen  = (TextView) findViewById(R.id.text_stat_projlen);
        mTextWENS     = (TextView) findViewById(R.id.text_stat_wens);
        mTextZminmax  = (TextView) findViewById(R.id.text_stat_zminmax);
        mTextStations = (TextView) findViewById(R.id.text_stat_stations);
        mTextShots    = (TextView) findViewById(R.id.text_stat_shots);
        mTextSplays   = (TextView) findViewById(R.id.text_stat_splays);

        mTextLeg       = (TextView) findViewById(R.id.stat_leg);
        mTextDuplicate = (TextView) findViewById(R.id.stat_duplicate);
        mTextSurface   = (TextView) findViewById(R.id.stat_surface);
        mTextSplay     = (TextView) findViewById(R.id.stat_splay);
        mTextStation   = (TextView) findViewById(R.id.stat_station);
        mTextLoop      = (TextView) findViewById(R.id.stat_loop);
        mTextComponent = (TextView) findViewById(R.id.stat_component);

        mTextAngleErr  = (TextView) findViewById(R.id.text_stat_angle_error);

        mTextLeg.setText( String.format( res.getString(R.string.stat_leg),
                          mStat.countLeg, mStat.lengthLeg * unit, unit_str ) );
        mTextDuplicate.setText( String.format( res.getString(R.string.stat_duplicate),
                          mStat.countDuplicate, mStat.lengthDuplicate * unit, unit_str ) );
        mTextSurface.setText( String.format( res.getString(R.string.stat_surface),
                          mStat.countSurface, mStat.lengthSurface * unit, unit_str ) );
        mTextSplay.setText( String.format( res.getString(R.string.stat_splay), mStat.countSplay ) );
        mTextStation.setText( String.format( res.getString(R.string.stat_station), mStat.countStation ) );
        mTextLoop.setText( String.format( res.getString(R.string.stat_loop), mStat.countLoop ) );
        mTextComponent.setText( String.format( res.getString(R.string.stat_component), mStat.countComponent ) );

        mTextAngleErr.setText( String.format( res.getString(R.string.stat_angle_error), 
            mNum.angleErrorMean() * TDMath.RAD2DEG, mNum.angleErrorStddev() * TDMath.RAD2DEG ) );

   
        // mList.setOnItemClickListener( this );
        List< String > cls = mNum.getClosures();
        if ( cls.size() == 0 ) {
          ((TextView)findViewById( R.id.text_stat_loops )).setText( R.string.loop_none );
        } else {
          mList = (ListView) findViewById(R.id.list);
          mList.setAdapter( new ArrayAdapter<>( mContext, R.layout.row, cls ) );
        }

        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener( this );

        mTextOrigin.setText( String.format( res.getString(R.string.stat_origin), mOrigin ) );

        if ( mAzimuth < 0 ) {
          mTextAzimuth.setVisibility( View.GONE );
        } else {
          mTextAzimuth.setText( String.format( res.getString(R.string.stat_azimuth), mAzimuth ) );
        }

        mTextLength.setText( String.format( res.getString(R.string.stat_length),
                             mNum.surveyLength() * unit, unit_str ) );
        mTextProjLen.setText( String.format( res.getString(R.string.stat_projlen),
                              mNum.surveyProjLen() * unit, unit_str ) );
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
        mTextStations.setText(String.format( res.getString(R.string.stat_station),
                                             mNum.stationsNr() ) );

        mTextShots.setText( String.format( res.getString(R.string.stat_shot),
                                           mNum.shotsNr(),
                                           mNum.duplicateNr(),
                                           mNum.surfaceNr() ) );
          
        mTextSplays.setText( String.format( res.getString(R.string.stat_splay),
                                            mNum.splaysNr() ) );

    }

    @Override
    public void onClick(View view)
    {
      Button b = (Button)view;
      if ( b == mBtnBack ) {
        /* nothing */
      }
      dismiss();
    }
}
        

