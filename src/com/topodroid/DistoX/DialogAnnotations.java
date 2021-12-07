/* @file DialogAnnotations.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.ui.MyDialog;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

// import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;

// import android.content.Intent;

// import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;


class DialogAnnotations extends MyDialog // Activity
                        implements View.OnClickListener
{
  // private TextView mTVtitle;   // dialog title
  private EditText mETtext;       // annotation text
  private String       mFilename; // annotation file
  private final String mName;     // survey name 

  /** cstr
   * @param context   context
   * @param name      survey name
   */
  DialogAnnotations( Context context, String name )
  {
    super( context, R.string.DialogAnnotations );
    mName = name;
  }

  /** load the content of the annotation from the file
   */
  private void load( )
  {
    // TDLog.Log( TDLog.LOG_IO, "annotations read from file " + mFilename );
    try {
      FileReader fr = TDFile.getFileReader( mFilename );
      BufferedReader br = new BufferedReader( fr );
      String line = br.readLine();
      while ( line != null ) {
        mETtext.append( line + "\n" );
        line = br.readLine();
      }
      fr.close();
    } catch ( IOException e ) {
      TDLog.Error(  "load IOexception " + e.toString() );
    }
  }

  /** save the content of the annotation to the file
   */
  private void save( )
  {
    // TDLog.Log( TDLog.LOG_IO, "annotations save to file " + mFilename );
    try {
      TDPath.checkPath( mFilename );
      FileWriter fw = TDFile.getFileWriter( mFilename, false );
      PrintWriter pw = new PrintWriter( fw );
      pw.format( "%s", mETtext.getText() );
      fw.flush();
      fw.close();
    } catch ( IOException e ) {
      TDLog.Error(  "save IOexception " + e.toString() );
    }
  }

  /** append a text to the annotation
   * @param name    survey name
   * @param text    text to append
   */
  public static void append( String name, String text )
  {
    String filename = TDPath.getSurveyNoteFile( name );
    // TDLog.Log( TDLog.LOG_IO, "annotations append to file " + filename );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = TDFile.getFileWriter( filename, true );
      PrintWriter pw = new PrintWriter( fw );
      pw.format( "%s", text );
      fw.flush();
      fw.close();
    } catch ( IOException e ) {
      TDLog.Error(  "append IOexception " + e.toString() );
    }
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.distox_annotations, R.string.title_note );

    // mTVtitle  = (TextView) findViewById(R.id.note_title );
    mETtext   = (EditText) findViewById(R.id.note_text );
    // mButtonCancel = (Button) findViewById(R.id.button_cancel );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );
    mFilename = TDPath.getSurveyNoteFile( mName );
    // mTVtitle.setText( mName );
    setTitle( R.string.title_note );

    load();

    ( (Button) findViewById(R.id.button_ok ) ).setOnClickListener( this );
    ( (Button) findViewById(R.id.button_cancel ) ).setOnClickListener( this );
  }

  /** implement response to user tap action
   * @param v    tapped view
   */
  @Override
  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    if ( v.getId() == R.id.button_ok ) {
      save();
    }
    // R.id.button_cancel dismiss
    dismiss();
  }

}


