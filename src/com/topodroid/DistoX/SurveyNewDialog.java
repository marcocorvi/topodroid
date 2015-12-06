/* @file SurveyNewDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.List;

// import java.io.File;
// import java.io.IOException;

// import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DatePickerDialog;

import android.content.Context;
// import android.content.Intent;

import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;

import android.widget.Toast;

// import android.util.Log;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.View.OnClickListener;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

public class SurveyNewDialog extends Dialog
                             implements View.OnClickListener
{
  private TopoDroidActivity mParent;
  private Context mContext;

  private EditText mEditName;
  private Button mEditDate;
  private EditText mEditTeam;
  private EditText mEditDecl;
  private EditText mEditStation;
  private EditText mEditComment;

  MyDateSetListener mDateListener;

  private Button mBTsave;
  private Button mBTopen;
  // private Button mBTback;

  private TopoDroidApp mApp;
  private SurveyInfo info;

  private long mOldSid = -1L;
  private long mOldId  = -1L;

// -------------------------------------------------------------------
  public SurveyNewDialog( Context context, TopoDroidActivity parent, long old_sid, long old_id )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mApp = (TopoDroidApp) mParent.getApplication();
    mOldSid = old_sid;
    mOldId  = old_id;
  }

  @Override
  protected void onCreate( Bundle savedInstanceState) 
  {
    super.onCreate( savedInstanceState );

    setContentView(R.layout.survey_new_dialog);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( R.string.title_survey );

    mEditName    = (EditText) findViewById(R.id.survey_name);
    mEditDate    = (Button) findViewById(R.id.survey_date);
    mEditTeam    = (EditText) findViewById(R.id.survey_team);
    mEditStation = (EditText) findViewById(R.id.survey_station);
    mEditDecl    = (EditText) findViewById(R.id.survey_decl);
    mEditComment = (EditText) findViewById(R.id.survey_comment);

    mDateListener = new MyDateSetListener( mEditDate );
    mEditDate.setOnClickListener( this );

    mEditStation.setText( TopoDroidSetting.mInitStation );

    if ( TopoDroidSetting.mDefaultTeam.length() > 0 ) {
      mEditTeam.setText( TopoDroidSetting.mDefaultTeam );
    }

    mEditDate.setText( TopoDroidUtil.currentDate() );

    mBTsave = (Button) findViewById( R.id.surveySave );
    mBTopen = (Button) findViewById( R.id.surveyOpen );
    // mBTback = (Button) findViewById( R.id.surveyBack );
    mBTsave.setOnClickListener( this );
    mBTopen.setOnClickListener( this );
    // mBTback.setOnClickListener( this );
  }

  // ------------------------------------------
   
  @Override
  public void onClick(View view)
  {
    Button b = (Button)view;

    // if ( b == mBTback ) {
    //  // Log.v( TopoDroidApp.TAG, "new survey back ");
    //  dismiss();
    //}
    if ( b == mEditDate ) {
      String date = mEditDate.getText().toString();
      int y = TopoDroidUtil.dateParseYear( date );
      int m = TopoDroidUtil.dateParseMonth( date );
      int d = TopoDroidUtil.dateParseDay( date );
      new DatePickerDialog( mContext, mDateListener, y, m, d ).show();
      return;
    }

    // if ( mEditName.getText() == null ) return;
    String name = mEditName.getText().toString();
    if ( name == null || name.length() == 0 ) {
      String error = mContext.getResources().getString( R.string.error_name_required );
      mEditName.setError( error );
      return;
    }
    name = TopoDroidUtil.noSpaces( name );
    if ( ! saveSurvey( name ) ) {
      String error = mContext.getResources().getString( R.string.survey_exists );
      mEditName.setError( error );
      return;
    }

    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "SurveyDialog onClick() " + item.toString() );
    if ( b == mBTsave ) {
      // Log.v( TopoDroidApp.TAG, "new survey save ");
      dismiss();
      mParent.updateDisplay( );
    } else if ( b == mBTopen ) {
      // Log.v( TopoDroidApp.TAG, "new survey open ");
      dismiss();
      mParent.doOpenSurvey( name );
    }
    dismiss();
  }

  // ---------------------------------------------------------------

  private boolean saveSurvey( String name )
  {
    // FIXME FORCE NAMES WITHOUT SPACES
    name = TopoDroidUtil.noSpaces( name );
    name = name.trim();
    if ( name.length() == 0 ) return false;
    if ( mApp.hasSurveyName( name ) ) { // name already exists
      // Toast.makeText( mContext, R.string.survey_exists, Toast.LENGTH_SHORT).show();
      return false;
    }

    String date = mEditDate.getText().toString();
    String team = mEditTeam.getText().toString();
    String comment = mEditComment.getText().toString();
    double decl = 0.0;
    if ( mEditDecl.getText() != null ) {
      String decl_str = mEditDecl.getText().toString();
      if ( decl_str != null && decl_str.length() > 0 ) {
        decl_str = decl_str.replace(',', '.');
        try {
          decl = Double.parseDouble( decl_str );
        } catch ( NumberFormatException e ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "parse Double error: declination " + decl_str );
        }
      }
    }

    String init_station = TopoDroidSetting.mInitStation;
    if ( mEditStation.getText() != null ) {
      String station = mEditStation.getText().toString().replaceAll("\\s+", "");
      if ( station.length() > 0 ) {
        init_station = station;
      }
    }
      
    if ( date != null ) { date = date.trim(); }
    if ( team != null ) { team = team.trim(); }
    if ( comment != null ) { comment = comment.trim(); }

    mApp.setSurveyFromName( name, true ); // save survey name: tell app to set it into the database
    
    if ( team == null ) team = "";
    mApp.mData.updateSurveyInfo( mApp.mSID, date, team, decl, comment, init_station, true );

    if ( mOldSid >= 0L && mOldId >= 0L ) {  // SPLIT_SURVEY
      mApp.mData.transferShots( mApp.mSID, mOldSid, mOldId );
      mOldSid = -1L;
      mOldId = -1l;
    }
    
    return true;
  }

}
