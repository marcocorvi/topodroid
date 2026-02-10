/* @file AIdialog.java
 *
 * @author marco corvi
 * @date nov 2013
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

public class AIdialog extends MyDialog
                 implements OnClickListener
                 , AdapterView.OnItemSelectedListener
{
  UserManualActivity mParent;
  AIhelper mHelper;
  static String mSystemInstruction = null;
  boolean mLocalContext = true; // whether to include the local context in the query

  static String mLang   = null; // local language
  static String mJargon = null; // jargon dictionary
  static String mNames  = null; // dictionary of names

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

  // TODO list of help entries
  /** cstr
   */
  public AIdialog( Context context, UserManualActivity parent, String user_key, String page )
  {
    super( context, null, R.string.AIdialog );  // nul app
    mParent = parent;
    // TDLog.v("Man page " + page );
    mHelper = new AIhelper( context, this, user_key, page );
    if ( mSystemInstruction == null ) mSystemInstruction = getOrderedUserManual( context );
    String lang = TDSetting.mLocale;
    // TDLog.v("Jargon lang: <" + lang + ">" );
    if ( TDString.isNullOrEmpty( lang ) || lang.equals("en") ) {
      if ( mLang != null ) {
        mLang   = null;
        mJargon = null;
      }
    } else if ( ! lang.equals( mLang ) ) {
      mLang = lang;
      mJargon = getJargon( context, mLang );
    }
    if ( mNames == null ) mNames = getNames( context );
    mLocalContext = true;
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    initLayout(R.layout.ai_dialog, R.string.title_ai_dialog );

    ((Button) findViewById( R.id.button_submit ) ).setOnClickListener( this );
    ((Button) findViewById( R.id.button_clear  ) ).setOnClickListener( this );
    ((Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );
    ((Button) findViewById( R.id.button_reset  ) ).setOnClickListener( this );

    Spinner models = (Spinner) findViewById( R.id.model );
    ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, mModels );
    models.setAdapter( adapter );
    models.setOnItemSelectedListener( this );

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
      EditText et = (EditText) findViewById( R.id.question );
      String question = et.getText().toString();
      if ( question == null || question.isEmpty() ) {
        et.setError( mContext.getResources().getString( R.string.error_question ) );
      } else {
        TextView answer = (TextView) findViewById( R.id.answer );
        mHelper.setModel( mModels[mIdxModel] );
        mHelper.ask( question, answer, mLocalContext );
        mLocalContext = false;
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
    TextView answer = (TextView) findViewById( R.id.answer );
    answer.setText( response );
  }

  /** get the man pages in the order according to list.txt
   * @param ctx  context
   */
  private String getOrderedUserManual( Context ctx )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( ctx.getResources().getString( R.string.ai_user ) )
      .append( ctx.getResources().getString( R.string.ai_begin_manual ) );
    try {
      InputStream is = ctx.getAssets().open("man/list.txt");
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String filename;
      while ( ( filename = br.readLine() ) != null ) {
        filename = filename.trim();
        if ( filename.isEmpty() || ! filename.endsWith(".htm") ) continue;
        try {
          InputStream fis = ctx.getAssets().open("man/" + filename );
          int size = fis.available();
          byte[] buffer = new byte[ size ];
          fis.read( buffer );
          String content = new String( buffer, "UTF-8" );
          sb.append("\r\n-- SOURCE_FILE ").append( filename ).append(" --\n");
          sb.append( content );
        } catch (IOException e ) {
          TDLog.e("Could not find man page " + filename );
        }
        // TDLog.v("Read man page " + filename );
      }
      br.close();
    } catch (IOException e ) {
      TDLog.e("Error reading list.txt " + e.getMessage() );
    }
    sb.append( ctx.getResources().getString( R.string.ai_end_manual ) );
    return sb.toString();
  }


  void openPageOnParent( String page )
  {
    dismiss();
    mParent.loadAssetPage( null, page, true );
  }

  /** get the jargon disctionary
   * @param ctx  context
   * @param lang language (2-char ISO code lowercase)
   */
  private String getJargon( Context ctx, String lang )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( String.format( ctx.getResources().getString( R.string.ai_jargon ), lang ) ).append("\n");
    try {
      InputStream is = ctx.getAssets().open("ai/dict.txt");
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String line;
      while ( ( line = br.readLine() ) != null ) {
        line = line.trim();
        if ( line.startsWith("{") ) continue;
        int pos = line.indexOf(":{");
        String term = line.substring(0,pos);
        String translation = null;
        while ( ( line = br.readLine() ) != null ) {  
          line = line.trim();
          if ( line.startsWith("}") ) {
            if ( translation != null ) { // add term-translation
              sb.append("- ").append( term ).append(" -> ").append( translation ).append("\n");
              // TDLog.v("- " + term + " -> " + translation );
            }
            break;
          } 
          if ( translation == null && line.startsWith( lang ) ) {
            pos = line.indexOf(": ");
            if ( pos + 2 < line.length() ) translation = line.substring( pos + 2 );
          }
        }
      }
    } catch (IOException e ) {
      TDLog.e("Error reading list.txt " + e.getMessage() );
    }
    return sb.toString();
  }

  /** get the disctionary of proper names
   * @param ctx  context
   */
  private String getNames( Context ctx )
  {
    StringBuilder sb = new StringBuilder();
    sb.append( ctx.getResources().getString( R.string.ai_names ) ).append("\n");
    try {
      InputStream is = ctx.getAssets().open("ai/names.txt");
      BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
      String line;
      while ( ( line = br.readLine() ) != null ) {
        line = line.trim();
        if ( line.length() > 0 ) {
          sb.append("- ").append( line );
        }
      }
    } catch (IOException e ) {
      TDLog.e("Error reading list.txt " + e.getMessage() );
    }
    return sb.toString();
  }

}

