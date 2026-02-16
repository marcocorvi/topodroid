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
import com.topodroid.help.HelpDialog;
import com.topodroid.help.AIhelper;
import com.topodroid.help.ValidationCallback;
import com.topodroid.help.IHelpViewer;
import com.topodroid.TDX.TDToast;
import com.topodroid.TDX.R;

import android.os.Bundle;
import android.app.Activity;

import android.content.Context;
// import android.content.DialogInterface;

// import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;

import android.view.View;
import android.view.View.OnClickListener;

import android.text.TextWatcher;
import android.text.Editable;

public class GeminiDialog extends MyDialog
                          implements OnClickListener
{
  private TDPref mPref;
  private IHelpViewer mParent;
  private EditText mETkey;

  // ---------------------------------------------------------------
  /** cstr
   * @param context    context
   * @param app        application
   * @param downloader data downloader
   * @param lister     data lister
   */
  public GeminiDialog( Context context, IHelpViewer parent, TDPref pref )
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

    mETkey = (EditText) findViewById( R.id.api_key );
    mETkey.addTextChangedListener( new TextWatcher() {
      @Override public void beforeTextChanged( CharSequence s, int start, int before, int count ) { }
      @Override public void onTextChanged( CharSequence s, int start, int before, int count ) { }
      @Override public void afterTextChanged( Editable s ) {
        String input = s.toString();
        if ( s.length() < 20 && ! isApiKeyFormatValid( input ) ) {
          mETkey.setTextColor( 0xffff6666 );
        } else {
          mETkey.setTextColor( mContext.getResources().getColor( R.color.text ) );
        } 
      }
    } );
  }

  private boolean isApiKeyFormatValid( String key )
  {
    // TDLog.v("Check format: <" + key + "> len " + key.length() ); 
    final String apiKeyRegex = "^AIza[a-zA-Z0-9\\-_]{30,50}";
    if ( key == null ) return false;
    return key.matches( apiKeyRegex );
  }


  /** respond to a user tap - dismiss the dialog
   * @param v  tapped view
   */
  @Override
  public void onClick(View v) 
  {
    if ( v.getId() == R.id.button_ok ) {
      String key = ((EditText) findViewById( R.id.api_key )).getText().toString().trim();
      if ( key == null || key.isEmpty() ) {
        TDSetting.setGeminiApiKey( "" );
        if ( mPref != null ) mPref.setButtonValue( (key.isEmpty())? "---" : "***" );
      } else if ( ! isApiKeyFormatValid( key ) ) {
        // TDLog.v("API key format not valid");
        // this could be more informative about the error: too short, not starting with "Alza", invalid charcaters
        ((EditText) findViewById( R.id.api_key )).setError( resString(R.string.ai_invalid_key) );
        return;
      } else {
        AIhelper.validateApiKey( key, new ValidationCallback() {
          public void onResult( boolean valid, String response )
          {
            if ( valid ) {
              TDSetting.setGeminiApiKey( key );
              if ( mPref != null ) mPref.setButtonValue( "***" );
              if ( mParent != null ) mParent.showAIdialog();
            } else {
              if ( mParent != null ) {
                mParent.showInvalid( mPref, response );

              }  
              // TDLog.v( response );
            }
          }
        } );
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

