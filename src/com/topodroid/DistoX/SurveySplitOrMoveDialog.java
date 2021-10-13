/* @file SurveySplitOrMoveDialog.java
 *
 * @author marco corvi
 * @date sept 2015
 *
 * @brief TopoDroid import file list dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDColor;
import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;

import android.content.Context;

import java.util.List;
// import java.util.ArrayList;

// import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;
import android.content.DialogInterface;

import android.view.View;
import android.view.View.OnClickListener;
// import android.widget.AdapterView.OnItemClickListener;

import android.widget.Spinner;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

class SurveySplitOrMoveDialog extends MyDialog
                   implements AdapterView.OnItemSelectedListener
                   , OnClickListener
{ 
  private final ShotWindow mParent;

  private Button mBtnCancel;
  private Button mBtnSplit;
  private Button mBtnMove;

  private String mSelected = null;
  private String[] mSurveys = null;

  SurveySplitOrMoveDialog( Context context, ShotWindow parent )
  {
    super( context, R.string.SurveySplitOrMoveDialog );
    mParent  = parent;
    mSelected = null;
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    initLayout( R.layout.survey_split_move_dialog, R.string.survey_split_move_title );

    Spinner spin = (Spinner)findViewById( R.id.spin );
    spin.setOnItemSelectedListener( this );

    mBtnSplit  = (Button)findViewById( R.id.button_split );
    mBtnMove   = (Button)findViewById( R.id.button_move  );
    mBtnCancel = (Button)findViewById( R.id.button_cancel );
    mBtnSplit.setOnClickListener( this );
    mBtnMove.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );

    // setTitleColor( TDColor.TITLE_NORMAL );

    List< String > surveys = TopoDroidApp.getSurveyNames();
    int size = surveys.size();
    if ( size > 0 ) {

      mSurveys = new String[ size ];
      for ( int k=0; k<size; ++k ) mSurveys[k] = surveys.get(k);
      ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, mSurveys );
      spin.setAdapter( adapter );
    // } else {
    }
  }

  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) 
  { 
    mSelected = mSurveys[ pos ];
    // TDLog.v( "selected " + mSelected );
  }

  @Override
  public void onNothingSelected( AdapterView av ) { mSelected = null; }


  @Override
  public void onClick( View v ) 
  {
    Button b = (Button)v;
    if ( b == mBtnSplit  ) {
      TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), R.string.survey_split,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mParent.doSplitOrMoveSurvey( null );  // null: split
            dismiss();
          }
        }
      );
    } else if ( b == mBtnMove  ) {
      // TDLog.v( "move " + mSelected );
      String msg = String.format( mParent.getResources().getString( R.string.survey_move ), mSelected );
      TopoDroidAlertDialog.makeAlert( mParent, mParent.getResources(), msg,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            mParent.doSplitOrMoveSurvey( mSelected );
            dismiss();
          }
        }
      );
    }
    dismiss();
  }

}


