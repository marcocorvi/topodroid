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
package com.topodroid.Cave3X;

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
  private Button   mBtnOK;
  // private Button   mBtnBack;

  private final SurveyWindow mParent;

  SurveyRenameDialog( Context context, SurveyWindow parent )
  {
    super( context, R.string.SurveyRenameDialog );
    mParent = parent;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    initLayout(R.layout.survey_rename_dialog, R.string.title_survey_rename );
    
    mBtnOK   = (Button) findViewById(R.id.btn_ok );
    mBtnOK.setOnClickListener( this );
    // mBtnBack = (Button) findViewById(R.id.btn_back );
    // mBtnBack.setOnClickListener( this );
    ( (Button) findViewById(R.id.btn_back ) ).setOnClickListener( this );

    mEtName = (EditText) findViewById( R.id.et_name );
    mEtName.setText( TDInstance.survey );
  }

  @Override
  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    if ( b == mBtnOK ) {
      // if ( mEtName.getText() == null ) {
      //   mEtName.setError( mContext.getResources().getString( R.string.error_name_required ) );
      //   return;
      // }
      String name = mEtName.getText().toString();
      if ( /* name == null || */ name.length() == 0 ) {
        mEtName.setError( mContext.getResources().getString( R.string.error_name_required ) );
	return;
      }
      if ( ! name.equals( TDInstance.survey ) ) {
        if ( TopoDroidApp.mData.hasSurveyName( name ) ) {
          mEtName.setError( mContext.getResources().getString( R.string.survey_exists ) );
	  return;
        }
        mParent.renameSurvey( name );
      }
    // } else if ( b == mBtnBack ) {
    //   /* nothing */
    }
    dismiss();
  }

}


