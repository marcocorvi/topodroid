/* @file SurveyNewDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;

//import java.util.List;

// import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;
import android.app.DatePickerDialog;

import android.content.Context;
// import android.content.Intent;

import android.widget.EditText;
// import android.widget.TextView;
import android.widget.Button;
import android.widget.CheckBox;

import android.view.View;
import android.view.View.OnFocusChangeListener;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnClickListener;
// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

class SurveyNewDialog extends MyDialog
                             implements View.OnClickListener
                             , View.OnLongClickListener
{
  // private final static String EMPTY = "";

  private final MainWindow mParent;

  private EditText mEditName;
  private Button   mEditDate;
  private EditText mEditTeam;
  private EditText mEditDecl;
  private EditText mEditStation;
  private EditText mEditComment;
  private CheckBox mCBxsections;
  private CheckBox mCBdatamode;

  private MyDateSetListener mDateListener;

  private Button mBTsave;
  private Button mBTopen;
  private Button mBTback;

  private final TopoDroidApp mApp;
  private SurveyInfo info;

  private long mOldSid = -1L;
  private long mOldId  = -1L;

// -------------------------------------------------------------------
  SurveyNewDialog( Context context, MainWindow parent, long old_sid, long old_id )
  {
    super( context, R.string.SurveyNewDialog );
    mParent = parent;
    mApp    = (TopoDroidApp) mParent.getApplication();
    mOldSid = old_sid;
    mOldId  = old_id;
  }

  @Override
  protected void onCreate( Bundle savedInstanceState) 
  {
    super.onCreate( savedInstanceState );

    initLayout( R.layout.survey_new_dialog, R.string.title_survey_new );

    mEditName    = (EditText) findViewById(R.id.survey_name);
    mEditDate    = (Button) findViewById(R.id.survey_date);
    mEditTeam    = (EditText) findViewById(R.id.survey_team);
    mEditStation = (EditText) findViewById(R.id.survey_station);
    mEditDecl    = (EditText) findViewById(R.id.survey_decl);
    mEditComment = (EditText) findViewById(R.id.survey_comment);
    mCBxsections = (CheckBox) findViewById(R.id.survey_xsections);
    mCBxsections.setChecked( TDSetting.mSharedXSections );
    mCBdatamode  = (CheckBox) findViewById(R.id.survey_datamode);
    if ( ! ( TDLevel.overExpert && TDSetting.mDivingMode ) ) mCBdatamode.setVisibility( View.GONE );

    mEditDecl.setOnFocusChangeListener( new OnFocusChangeListener() {
      @Override
      public void onFocusChange( View v, boolean hasfocus ) {
        if ( ! hasfocus ) {
          if ( SurveyInfo.declinationOutOfRange( mEditDecl ) ) {
            mEditDecl.setText("");
	  }
	} 
      }
    } );

    mDateListener = new MyDateSetListener( mEditDate );
    mEditDate.setOnClickListener( this );

    // mEditStation.setText( TDSetting.mInitStation );
    mEditStation.setHint( String.format( mContext.getResources().getString( R.string.start_station ), TDSetting.mInitStation ) );
    mEditStation.setOnLongClickListener( this );

    if ( TDSetting.mDefaultTeam.length() > 0 ) {
      mEditTeam.setText( TDSetting.mDefaultTeam );
    }

    mEditDate.setText( TDUtil.currentDate() );

    mBTsave = (Button) findViewById( R.id.surveySave );
    mBTopen = (Button) findViewById( R.id.surveyOpen );
    mBTback = (Button) findViewById( R.id.surveyCancel );
    mBTsave.setOnClickListener( this );
    mBTopen.setOnClickListener( this );
    mBTback.setOnClickListener( this );
  }

  // ------------------------------------------
  @Override
  public boolean onLongClick(View v)
  {
    CutNPaste.makePopup( mContext, (EditText)v );
    return true;
  }
   
  @Override
  public void onClick(View view)
  {
    CutNPaste.dismissPopup();
    Button b = (Button)view;

    if ( b == mBTback ) {
      dismiss();
    }
    if ( b == mEditDate ) {
      String date = mEditDate.getText().toString();
      int y = TDUtil.dateParseYear( date );
      int m = TDUtil.dateParseMonth( date );
      int d = TDUtil.dateParseDay( date );
      new DatePickerDialog( mContext, mDateListener, y, m, d ).show();
      return;
    }

    // if ( mEditName.getText() == null ) return;
    String name = mEditName.getText().toString();
    if ( /* name == null || */ name.length() == 0 ) { // ALWAYS false
      mEditName.setError( mContext.getResources().getString( R.string.error_name_required ) );
      return;
    }
    name = TDUtil.noSpaces( name ); // FIXME FORCE NAMES WITHOUT UNACCEPTABLE CHARACTERS
    if ( ! saveSurvey( name ) ) {
      return;
    }

    // TDLog.Log( TDLog.LOG_INPUT, "SurveyDialog onClick() " + item.toString() );
    if ( b == mBTsave ) {
      // TDLog.v( "new survey save ");
      dismiss();
      mParent.updateDisplay( );
    } else if ( b == mBTopen ) {
      // TDLog.v( "new survey open ");
      dismiss();
      mParent.doOpenSurvey( name );
    }
    dismiss();
  }

  // ---------------------------------------------------------------

  private boolean saveSurvey( String name )
  {
    // if ( name == null ) return false; // guaranteed
    // name = TDUtil.noSpaces( name ); // already checked
    if ( name.length() == 0 ) {
      mEditName.setError( mContext.getResources().getString( R.string.error_name_required ) );
      return false;
    }
    if ( mApp.hasSurveyName( name ) ) { // name already exists
      mEditName.setError( mContext.getResources().getString( R.string.survey_exists ) );
      return false;
    }

    String date = mEditDate.getText().toString();
    String team = mEditTeam.getText().toString();
    String comment = mEditComment.getText().toString();
    double decl = SurveyInfo.declination( mEditDecl );
    // if ( decl >= SurveyInfo.DECLINATION_MAX ) {
    //   mEditDecl.setError( mContext.getResources().getString( R.string.error_invalid_number ) );
    //   return false;
    // }

    String init_station = TDSetting.mInitStation;
    CharSequence station_text = mEditStation.getText(); // split in pieces: 202007 crashes
    if ( station_text != null ) {
      String station = station_text.toString();
      if ( station.length() > 0 ) {
        station = station.replaceAll("\\s+", TDString.EMPTY);
        if ( station.length() > 0 ) init_station = station;
      }
    } // if mEditStation text is empty use setting mInitStation
    if ( init_station == null || init_station.length() == 0 ) init_station = TDString.ZERO;

    // date, team, comment always non-null
    /* if ( date != null ) */ { date = date.trim(); } // else { date = TDString.EMPTY; }
    /* if ( team != null ) */ { team = team.trim(); } // else { team = TDString.EMPTY; }
    /* if ( comment != null ) */ { comment = comment.trim(); } // else { comment = TDString.EMPTY; }

    int xsections = mCBxsections.isChecked() ? SurveyInfo.XSECTION_PRIVATE
                                             : SurveyInfo.XSECTION_SHARED;

    int datamode  = SurveyInfo.DATAMODE_NORMAL;
    if ( TDLevel.overExpert && TDSetting.mDivingMode && mCBdatamode.isChecked() ) datamode = SurveyInfo.DATAMODE_DIVING;

    long sid = mApp.setSurveyFromName( name, datamode, true ); // save survey name: tell app to set it into the database
    if ( sid <= 0 ) {
      TDLog.Error( "Failed to set survey name in DB");
      return false;
    }
    // Note TDInstance.sid == sid
    TopoDroidApp.mData.updateSurveyInfo( TDInstance.sid, date, team, decl, comment, init_station, xsections );

    if ( mOldSid >= 0L && mOldId >= 0L ) {  // SPLIT_SURVEY
      TopoDroidApp.mData.transferShots( TDInstance.sid, mOldSid, mOldId );
      mOldSid = -1L;
      mOldId = -1L;
    }
    
    return true;
  }

  @Override
  public void onBackPressed()
  {
    if ( CutNPaste.dismissPopup() ) return;
    dismiss();
  }

}
