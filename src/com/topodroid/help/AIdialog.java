/** @file AIdialog.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid help dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.help;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.TDX.R;

import java.util.ArrayList;

import android.os.Bundle;
import android.content.Context;

import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;

import android.view.View;
import android.view.View.OnClickListener;

import  java.io.InputStream;
import  java.io.InputStreamReader;
import  java.io.BufferedReader;
import  java.io.IOException;

import java.util.regex.Pattern;
import java.util.HashMap;

public class AIdialog extends MyDialog
                 implements OnClickListener
                 , AdapterView.OnItemSelectedListener
{
  protected IHelpViewer mParent;
  protected AIhelper mHelper;
  protected static String mSystemInstruction = null;
  protected boolean mLocalContext = true; // whether to include the local context in the query
  protected boolean mCanSubmit    = true;

  protected static String mLang   = null; // local language
  protected static String mJargon = null; // jargon dictionary
  protected static String mNames  = null; // dictionary of names
  protected static HashMap<String, String> mManualIndex = null;

  // gemini-2.0 will be shut down 2026-03-31
  // gemini-3.0-flash is not jet available
  final static String[] mModels = { 
    // "gemini-3.0-flash", 
    "gemini-3.0-flash",
    "gemini-2.5-flash", "gemini-2.5-flash-lite", "gemini-2.5-pro",
    "gemini-2.0-flash", "gemini-2.0-pro"
  };


  final static int IDX_MODEL = 1;
  int mIdxModel = IDX_MODEL;

  private Button mBtnSubmit;
  protected TextView mAnswer;

  protected int mRtitle = R.string.title_ai_dialog;
  protected int mRAImodel;  // AI model resource
  protected Pattern mPattern = null;

  // TODO list of help entries
  /** cstr
   */
  public AIdialog( Context context, IHelpViewer parent, String user_key, String page, int r_ai_model )
  {
    super( context, null, R.string.AIdialog );  // nul app
    mParent = parent;
    mRAImodel = r_ai_model;
    // TDLog.v("Man page " + page );
    mHelper = new AIhelper( context, this, user_key, page );
  }

  static public void resetChat()
  {
    AIhelper.resetChat();
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    initLayout(R.layout.ai_dialog, mRtitle );

    ((Button) findViewById( R.id.button_submit ) ).setOnClickListener( this );
    ((Button) findViewById( R.id.button_clear  ) ).setOnClickListener( this );
    ((Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );
    ((Button) findViewById( R.id.button_reset  ) ).setOnClickListener( this );

    Spinner models = (Spinner) findViewById( R.id.model );
    ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, mModels );
    models.setAdapter( adapter );
    models.setOnItemSelectedListener( this );

    mAnswer = (TextView) findViewById( R.id.answer );

    mIdxModel = IDX_MODEL;
    models.setSelection( IDX_MODEL );
  }

  /** react to an item selection
   * @param av    item adapter
   * @param v     item view
   * @param pos   item position
   * @param id    ?
   */
  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) 
  {
    mIdxModel = pos; 
  }

  @Override
  public void onNothingSelected( AdapterView av )
  {
    mIdxModel = IDX_MODEL;
    Spinner models = (Spinner) findViewById( R.id.model );
    models.setSelection( IDX_MODEL );
  }

  /** react to a user tap - only the taps on "man book" are taken
   *  open the associated man page
   * @param v  tapped view
   */
  @Override 
  public void onClick( View v ) 
  {
    if ( v.getId() == R.id.button_submit ) {
      if ( mCanSubmit ) {
        EditText et = (EditText) findViewById( R.id.question );
        String question = et.getText().toString();
        if ( question == null || question.isEmpty() ) {
          et.setError( mContext.getResources().getString( R.string.error_question ) );
        } else {
          mCanSubmit = false;
          Button b = (Button)findViewById(R.id.button_submit);
          b.setOnClickListener( null );
          b.setEnabled( false );
          mHelper.setModel( mModels[mIdxModel], mRAImodel );
          mHelper.ask( question, this, mLocalContext );
          mLocalContext = false;
        }
      }
    } else if ( v.getId() == R.id.button_reset ) { // reset the chat
      mHelper.resetChat();
    } else if ( v.getId() == R.id.button_clear ) { // clear the question text
      // ((TextView) findViewById( R.id.answer )).setText(""); 
      ((EditText) findViewById( R.id.question )).setText("");
    } else {
      dismiss();
    }
  }

  void resetCanSubmit()
  {
    mCanSubmit = true;
    Button v = (Button)findViewById(R.id.button_submit);
    v.setOnClickListener( this );
    v.setEnabled( true );
  }

  // @Override
  // public void onBackPressed()
  // {
  //   super.onBackPressed();
  // }

  /** show the response in the answer textbox
   * @param response  response
   */
  public void showResponse( String response )
  {
    mAnswer.setText( response );
  }

  public void openOnParent( String page )
  {
    dismiss();
    mParent.showManPage( page );
  }

}

