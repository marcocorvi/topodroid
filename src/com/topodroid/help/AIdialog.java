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

import com.topodroid.ui.MyDialog;
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

public class AIdialog extends MyDialog
                 implements OnClickListener
                 , AdapterView.OnItemSelectedListener
{
  UserManualActivity mParent;
  AIhelper mHelper;

  // gemini-2.0 will be shut down 2026-03-31
  // gemini-3.0-flash is not jet available
  final static String[] mModels = { 
    // "gemini-3.0-flash", 
    "gemini-3.0-flash-preview",
    "gemini-2.5-flash", "gemini-2.5-flash-lite", "gemini-2.5-pro",
    "gemini-2.0-flash", "gemini-2.0-pro"
  };
  final static int IDX_MODEL = 1;
  int mIdxModel = IDX_MODEL;

  private Button mBtnSubmit;

  // TODO list of help entries
  /** cstr
   */
  public AIdialog( Context context, UserManualActivity parent, String user_key )
  {
    super( context, null, R.string.AIdialog );  // nul app
    mParent = parent;
    mHelper = new AIhelper( this, user_key );
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    initLayout(R.layout.ai_dialog, R.string.title_ai_dialog );

    ((Button) findViewById( R.id.button_submit ) ).setOnClickListener( this );
    ((Button) findViewById( R.id.button_clear  ) ).setOnClickListener( this );
    ((Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );

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
        mHelper.ask( question, answer );
      }
    } else if ( v.getId() == R.id.button_clear ) {
      ((TextView) findViewById( R.id.answer )).setText("");
    } else {
      if ( mHelper != null ) mHelper.mDoReport = false;
      dismiss();
    }
  }

  @Override
  public void onBackPressed()
  {
    if ( mHelper != null ) mHelper.mDoReport = false;
    super.onBackPressed();
  }

  /** show the response in the answer textbox
   * @param response  response
   */
  public void showResponse( String response )
  {
    TextView answer = (TextView) findViewById( R.id.answer );
    answer.setText( response );
  }

}

