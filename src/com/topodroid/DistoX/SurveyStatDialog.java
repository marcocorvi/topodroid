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
 * CHANGES
 * 20120715 created
 */
package com.topodroid.DistoX;

// import java.io.StringWriter;
// import java.io.PrintWriter;

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


public class SurveyStatDialog extends Dialog 
                              // implements View.OnClickListener
{
    private Context mContext;

    private TextView mTextLeg;
    private TextView mTextDuplicate;
    private TextView mTextSurface;
    private TextView mTextSplay;
    private TextView mTextStation;
    private TextView mTextLoop;
    private TextView mTextComponent;

 
    SurveyStat mStat;

    private Button mBtnBack;

    public SurveyStatDialog( Context context, SurveyStat stat )
    {
      super(context);
      mContext = context;
      mStat = stat;
      // TopoDroidLog.Log(TopoDroidLog.LOG_STAT, "SurveyStat cstr");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_stat_dialog);

        getWindow().setLayout( LayoutParams.MATCH_PARENT,
                               LayoutParams.WRAP_CONTENT );

        Resources res = mContext.getResources();

        // TopoDroidLog.Log(TopoDroidLog.LOG_STAT, " SurveyStat onCreate");
        mTextLeg       = (TextView) findViewById(R.id.stat_leg);
        mTextDuplicate = (TextView) findViewById(R.id.stat_duplicate);
        mTextSurface   = (TextView) findViewById(R.id.stat_surface);
        mTextSplay     = (TextView) findViewById(R.id.stat_splay);
        mTextStation   = (TextView) findViewById(R.id.stat_station);
        mTextLoop      = (TextView) findViewById(R.id.stat_loop);
        mTextComponent = (TextView) findViewById(R.id.stat_component);

        // mBtnBack = (Button) findViewById(R.id.btn_back);
        // mBtnBack.setOnClickListener( this );

        mTextLeg.setText( String.format( res.getString(R.string.stat_leg), mStat.countLeg, mStat.lengthLeg ) );
        mTextDuplicate.setText( String.format( res.getString(R.string.stat_duplicate), mStat.countDuplicate, mStat.lengthDuplicate ) );
        mTextSurface.setText( String.format( res.getString(R.string.stat_surface), mStat.countSurface, mStat.lengthSurface ) );
        mTextSplay.setText( String.format( res.getString(R.string.stat_splay), mStat.countSplay ) );
        mTextStation.setText( String.format( res.getString(R.string.stat_station), mStat.countStation ) );
        mTextLoop.setText( String.format( res.getString(R.string.stat_loop), mStat.countLoop ) );
        mTextComponent.setText( String.format( res.getString(R.string.stat_component), mStat.countComponent ) );

        setTitle( res.getString(R.string.survey_info) );
    }

    // @Override
    // public void onClick(View view)
    // {
    //   Button b = (Button)view;
    //   if ( b == mBtnBack ) {
    //     // TopoDroidLog.Log( TopoDroidLog.LOG_STAT, "onClick()" );
    //     dismiss();
    //   }
    // }
}
        

