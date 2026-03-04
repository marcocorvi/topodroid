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
import com.topodroid.TDX.TDToast;
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
  protected AIhelper mHelper = null; 
  // protected AIlocalModel mLocalModel = null; // GEMMA3
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

  // GEMMA3
  // // list of help entries - names must coincide with sections in llm-settings.txt
  // static final protected String[] mLLMindex = {
  //   "GENERAL", "DEVICE", "EXPORT_ENABLE", "EXPORT_DATA", "EXPORT_SKETCH",
  //   "DATA", "DATA_PROCESS", 
  //   "SKETCH", "SKETCH_EDIT", "SKETCH_DRAW", "3D" 
  // };
  // protected static String[] mLLMsystemInstruction = null;

  final static int IDX_MODEL = 1;
  int mIdxModel = IDX_MODEL; // index of Gemini model / index of local setting section

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
    mParent.setAIbuttonEnabled( false );
    mRAImodel = r_ai_model;
    TDLog.v("AI dialog: page " + page + " model " + r_ai_model );
    if ( user_key != null ) {
      mHelper = new AIhelper( context, this, user_key, page );
      mIdxModel = IDX_MODEL;
    // } else { // GEMMA3
    //   mLocalModel = new AIlocalModel( context, this );
    //   mIdxModel = 0;
    }
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
    // ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, ( mHelper != null )? mModels : mLLMindex ); // GEMMA3
    ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, mModels );
    models.setAdapter( adapter );
    models.setOnItemSelectedListener( this );
    models.setSelection( mIdxModel );

    mAnswer = (TextView) findViewById( R.id.answer );
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
    models.setSelection( mIdxModel );
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
          if ( mHelper != null ) {
            mHelper.setModel( mModels[mIdxModel], mRAImodel );
            mHelper.ask( question, this, mLocalContext );
          // } else if ( mLocalModel != null ) { // GEMMA3
          //   mLocalModel.setPreamble( mLLMsystemInstruction[mIdxModel] );
          //   mLocalModel.askLocalModel( mContext, question, this );
          } else {
            TDToast.makeWarn( R.string.ai_no_model );
          }
          mLocalContext = false;
        }
      }
    } else if ( v.getId() == R.id.button_reset ) { // reset the chat
      if ( mHelper != null ) {
        mHelper.resetChat();
      // } else if ( mLocalModel != null ) { // GEMMA3
      //   mLocalModel.resetChat();
      }
    } else if ( v.getId() == R.id.button_clear ) { // clear the question and answer texts
      // ((TextView) findViewById( R.id.answer )).setText(""); 
      ((EditText) findViewById( R.id.question )).setText("");
      mAnswer.setText("");
    } else {
      mParent.setAIbuttonEnabled( true );
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

  /** apend a partila response to the text view
   * @param response  partial response
   */
  public void appendResponse( String response )
  {
    mAnswer.append( response );
  }

  public void openOnParent( String page )
  {
    dismiss();
    mParent.showManPage( page );
  }

  @Override
  public void onBackPressed()
  {
    mParent.setAIbuttonEnabled( true );
    super.onBackPressed();
  }

}

