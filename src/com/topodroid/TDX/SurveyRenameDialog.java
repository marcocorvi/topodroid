/* @file SurveyRenameDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey Rename dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDString;
import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;

// import android.app.Dialog;
// import android.app.Activity;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;

import android.widget.EditText;
import android.widget.Button;
import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;
// import android.view.ViewGroup.LayoutParams;


class SurveyRenameDialog extends MyDialog
                                implements View.OnClickListener
{
  private EditText mEtName;
  private EditText mEtPrefix;
  private Button   mBtnRename;
  private Button   mBtnPrefix;
  // private Button   mBtnBack;

  private final SurveyWindow mParent;

  SurveyRenameDialog( Context context, SurveyWindow parent )
  {
    super( context, null, R.string.SurveyRenameDialog ); // null app
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    initLayout(R.layout.survey_rename_dialog, R.string.title_survey_rename );
    
    mBtnRename = (Button) findViewById(R.id.btn_rename );
    mBtnPrefix = (Button) findViewById(R.id.btn_prefix );
    ( (Button) findViewById(R.id.btn_back ) ).setOnClickListener( this );
    mBtnRename.setOnClickListener( this );

    mEtName = (EditText) findViewById( R.id.et_name );
    mEtName.setText( TDInstance.survey );
    mEtPrefix = (EditText) findViewById( R.id.et_prefix );
    if ( TDLevel.overExpert ) {
      mBtnPrefix.setOnClickListener( this );
    } else {
      mBtnPrefix.setVisibility( View.GONE );
      mEtPrefix.setVisibility( View.GONE );
    }
  }

  @Override
  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    if ( b == mBtnRename ) {
      String name = mEtName.getText().toString();
      if ( ! TDString.checkName( name, mEtName, mContext.getResources() ) ) {
	return;
      }
      name = TDString.spacesToUnderscore( name ); // this trims the string
      if ( ! name.equals( TDInstance.survey ) ) {
        if ( TopoDroidApp.mData.hasSurveyName( name ) ) {
          mEtName.setError( mContext.getResources().getString( R.string.survey_exists ) );
	  return;
        }
        mParent.renameSurvey( name );
      }
    } else if ( TDLevel.overExpert && b == mBtnPrefix ) {
      String prefix = TDString.spacesToUnderscores( mEtPrefix.getText().toString() );
      if ( TDString.isNullOrEmpty( prefix ) ) {
	/* nothing */
      } else {
        mParent.prefixStations( prefix );
      }
    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

}


