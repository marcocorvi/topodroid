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
 * CHANGES
 * 20120530 loop closures
 * 20120702 shot surface flag
 * 20120726 TopoDroid log
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

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

public class DistoXStatDialog extends Dialog 
                              // implements View.OnClickListener
{
    private Context mContext;

    private TextView mTextLength;
    private TextView mTextWENS;
    private TextView mTextZminmax;
    private TextView mTextStations;
    private TextView mTextShots;
    private TextView mTextSplays;
    private ListView mList;

    // private Button mBtnCancel;

    private DistoXNum mNum;
    public DistoXStatDialog( Context context, DistoXNum num )
    {
      super(context);
      mContext = context;
      mNum = num;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.distox_stat_dialog);
        getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

        mTextLength   = (TextView) findViewById(R.id.text_stat_length);
        mTextWENS     = (TextView) findViewById(R.id.text_stat_wens);
        mTextZminmax  = (TextView) findViewById(R.id.text_stat_zminmax);
        mTextStations = (TextView) findViewById(R.id.text_stat_stations);
        mTextShots    = (TextView) findViewById(R.id.text_stat_shots);
        mTextSplays   = (TextView) findViewById(R.id.text_stat_splays);

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

        Resources res = mContext.getResources();

        mTextLength.setText( String.format( res.getString(R.string.stat_length),
                                            mNum.surveyLength() ) );
        mTextWENS.setText( String.format( res.getString(R.string.stat_wens),
                                          mNum.surveyWest(),
                                          mNum.surveyEast(),
                                          mNum.surveyNorth(),
                                          mNum.surveySouth()
                          ) );
        mTextZminmax.setText( String.format( res.getString(R.string.stat_depth),
                                             mNum.surveyTop(),
                                             mNum.surveyBottom() ) );
        mTextStations.setText(String.format( res.getString(R.string.stat_station),
                                             mNum.stationsNr() ) );

        mTextShots.setText( String.format( res.getString(R.string.stat_shot),
                                           mNum.shotsNr(),
                                           mNum.duplicateNr(),
                                           mNum.surfaceNr() ) );
          
        mTextSplays.setText( String.format( res.getString(R.string.stat_splay),
                                            mNum.splaysNr() ) );

        setTitle( R.string.title_stats );
    }

    // @Override
    // public void onClick(View view)
    // {
    //   Button b = (Button)view;
    //   if ( b == mBtnCancel ) {
    //     // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "StatDialog onClick()" );
    //     dismiss();
    //   }
    // }
}
        

