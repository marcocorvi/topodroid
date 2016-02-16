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
                              // implements View.OnClickListener
{
    private DistoXNum mNum;
    private String mOrigin;
    SurveyStat mStat;

    private TextView mTextOrigin;
    private TextView mTextLength;
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


    // private Button mBtnCancel;

    public DistoXStatDialog( Context context, DistoXNum num, String origin, SurveyStat stat )
    {
      super( context, R.string.DistoXStatDialog );
      mNum    = num;
      mOrigin = origin;
      mStat   = stat;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initLayout( R.layout.distox_stat_dialog, R.string.title_stats );

        Resources res = mContext.getResources();
        float unit = TDSetting.mUnitLength;

        mTextOrigin   = (TextView) findViewById(R.id.text_stat_origin);
        mTextLength   = (TextView) findViewById(R.id.text_stat_length);
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

        mTextLeg.setText( String.format( res.getString(R.string.stat_leg), mStat.countLeg, mStat.lengthLeg * unit ) );
        mTextDuplicate.setText( String.format( res.getString(R.string.stat_duplicate), mStat.countDuplicate, mStat.lengthDuplicate * unit ) );
        mTextSurface.setText( String.format( res.getString(R.string.stat_surface), mStat.countSurface, mStat.lengthSurface * unit ) );
        mTextSplay.setText( String.format( res.getString(R.string.stat_splay), mStat.countSplay ) );
        mTextStation.setText( String.format( res.getString(R.string.stat_station), mStat.countStation ) );
        mTextLoop.setText( String.format( res.getString(R.string.stat_loop), mStat.countLoop ) );
        mTextComponent.setText( String.format( res.getString(R.string.stat_component), mStat.countComponent ) );

   
        // mList.setOnItemClickListener( this );
        List< String > cls = mNum.getClosures();
        if ( cls.size() == 0 ) {
          ((TextView)findViewById( R.id.text_stat_loops )).setText( R.string.loop_none );
        } else {
          mList = (ListView) findViewById(R.id.list);
          mList.setAdapter( new ArrayAdapter<String>( mContext, R.layout.row, cls ) );
        }

        // mBtnCancel = (Button) findViewById(R.id.button_cancel);
        // mBtnCancel.setOnClickListener( this );

        mTextOrigin.setText( String.format( res.getString(R.string.stat_origin), mOrigin ) );

        mTextLength.setText( String.format( res.getString(R.string.stat_length), mNum.surveyLength() * unit ) );
        mTextWENS.setText( String.format( res.getString(R.string.stat_wens),
                                          mNum.surveyWest()  * unit,
                                          mNum.surveyEast()  * unit,
                                          mNum.surveyNorth() * unit,
                                          mNum.surveySouth() * unit
                          ) );
        mTextZminmax.setText( String.format( res.getString(R.string.stat_depth),
                                             mNum.surveyTop()    * unit,
                                             mNum.surveyBottom() * unit ) );
        mTextStations.setText(String.format( res.getString(R.string.stat_station),
                                             mNum.stationsNr() ) );

        mTextShots.setText( String.format( res.getString(R.string.stat_shot),
                                           mNum.shotsNr(),
                                           mNum.duplicateNr(),
                                           mNum.surfaceNr() ) );
          
        mTextSplays.setText( String.format( res.getString(R.string.stat_splay),
                                            mNum.splaysNr() ) );

    }

    // @Override
    // public void onClick(View view)
    // {
    //   Button b = (Button)view;
    //   if ( b == mBtnCancel ) {
    //     // TDLog.Log( TDLog.LOG_INPUT, "StatDialog onClick()" );
    //     dismiss();
    //   }
    // }
}
        

