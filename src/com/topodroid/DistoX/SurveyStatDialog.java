/* @file SurveyStatDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey stats display dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;
import android.content.res.Resources;

import android.graphics.*;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.widget.Button;


public class SurveyStatDialog extends MyDialog 
                              implements View.OnClickListener
{
    private TextView mTextLeg;
    private TextView mTextDuplicate;
    private TextView mTextSurface;
    private TextView mTextSplay;
    private TextView mTextStation;
    private TextView mTextLoop;
    private TextView mTextComponent;
    private TextView mTextStddevM;
    private TextView mTextStddevG;
    private TextView mTextStddevDip;

 
    SurveyStat mStat;

    private Button mBtnBack;

    public SurveyStatDialog( Context context, SurveyStat stat )
    {
      super( context, R.string.SurveyStatDialog );
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
        mTextLeg       = (TextView) findViewById(R.id.stat_leg);
        mTextDuplicate = (TextView) findViewById(R.id.stat_duplicate);
        mTextSurface   = (TextView) findViewById(R.id.stat_surface);
        mTextSplay     = (TextView) findViewById(R.id.stat_splay);
        mTextStation   = (TextView) findViewById(R.id.stat_station);
        mTextLoop      = (TextView) findViewById(R.id.stat_loop);
        mTextComponent = (TextView) findViewById(R.id.stat_component);
        mTextStddevM   = (TextView) findViewById(R.id.stat_stddev_m);
        mTextStddevG   = (TextView) findViewById(R.id.stat_stddev_g);
        mTextStddevDip = (TextView) findViewById(R.id.stat_stddev_dip);

        mBtnBack = (Button) findViewById(R.id.btn_back);
        mBtnBack.setOnClickListener( this );

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

        mTextStddevM.setText( String.format( res.getString(R.string.stat_stddev_m), mStat.stddevM ) );
        mTextStddevG.setText( String.format( res.getString(R.string.stat_stddev_g), mStat.stddevG ) );
        mTextStddevDip.setText( String.format( res.getString(R.string.stat_stddev_dip), mStat.stddevDip, mStat.averageDip ) );

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
        

