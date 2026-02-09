/* @file GeminiDialog.java
 *
 * @author marco corvi
 * @date nov 2016
 *
 * @brief TopoDroid AI API key dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.prefs;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.help.AIhelper;
import com.topodroid.TDX.R;

import android.os.Bundle;

import android.content.Context;
// import android.content.DialogInterface;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import android.view.View;
import android.view.View.OnClickListener;

public class GeminiDialog extends MyDialog
                          implements OnClickListener
{
  private TDPref mPref;
  private UserManualActivity mParent;

  // ---------------------------------------------------------------
  /** cstr
   * @param context    context
   * @param app        application
   * @param downloader data downloader
   * @param lister     data lister
   */
  public GeminiDialog( Context context, UserManualActivity parent, TDPref pref )
  {
    super( context, null, 0 ); // 0: no help resource
    mParent = parent;
    mPref   = pref;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
 
    // TDLog.v( "device select dialog init layout");
    initLayout( R.layout.gemini_dialog, R.string.title_gemini );

    ( (Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );
    ( (Button) findViewById( R.id.button_ok ) ).setOnClickListener( this );
    ( (Button) findViewById( R.id.button_view ) ).setOnClickListener( this );
  }

  /** respond to a user tap - dismiss the dialog
   * @param v  tapped view
   */
  @Override
  public void onClick(View v) 
  {
    if ( v.getId() == R.id.button_ok ) {
      String key = ((EditText) findViewById( R.id.api_key )).getText().toString();
      if ( key == null ) key = "";
      TDSetting.setGeminiApiKey( key );
      if ( ! key.isEmpty() ) {
        AIhelper.validateApiKey( key, new AIhelper.ValidationCallback() {
          public void onResult( boolean valid, String response )
          {
            if ( valid ) {
              if ( mPref != null ) mPref.setButtonValue( "***" );
              if ( mParent != null ) mParent.showAIdialog();
            } else {
              TDLog.v( response );
            }
          }
        } );
      } else { 
        if ( mPref != null ) mPref.setButtonValue( (key.isEmpty())? "" : "***" );
      }
    } else if ( v.getId() == R.id.button_view ) {
      if ( TDSetting.mGeminiApiKey != null ) {
        EditText et = (EditText) findViewById( R.id.api_key );
        et.setText( TDSetting.mGeminiApiKey );
      }
      return;
    }
    dismiss();
  }

}

