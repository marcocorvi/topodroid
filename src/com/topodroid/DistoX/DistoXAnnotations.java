/* @file DistoXAnnotations.java
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

// import java.io.File;
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


class DistoXAnnotations extends MyDialog // Activity
                               implements View.OnClickListener
{
  // private TextView mTVtitle;
  private EditText mETtext;
  private Button   mButtonOK;
  // private Button   mButtonCancel;
  private String   mFilename;
  private final String   mTitle;

  DistoXAnnotations( Context context, String title )
  {
    super( context, R.string.DistoXAnnotations );
    mTitle = title;
  }

  private void load( )
  {
    // TDLog.Log( TDLog.LOG_IO, "annotations read from file " + mFilename );
    try {
      FileReader fr = new FileReader( mFilename );
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

  private void save( )
  {
    // TDLog.Log( TDLog.LOG_IO, "annotations save to file " + mFilename );
    try {
      TDPath.checkPath( mFilename );
      FileWriter fw = new FileWriter( mFilename, false );
      PrintWriter pw = new PrintWriter( fw );
      pw.format( "%s", mETtext.getText() );
      fw.flush();
      fw.close();
    } catch ( IOException e ) {
      TDLog.Error(  "save IOexception " + e.toString() );
    }
  }

  public static void append( String title, String text )
  {
    String filename = TDPath.getSurveyNoteFile( title );
    // TDLog.Log( TDLog.LOG_IO, "annotations append to file " + filename );
    try {
      TDPath.checkPath( filename );
      FileWriter fw = new FileWriter( filename, true );
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
    mButtonOK = (Button) findViewById(R.id.button_ok );
    // mButtonCancel = (Button) findViewById(R.id.button_cancel );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );
    mFilename = TDPath.getSurveyNoteFile( mTitle );
    // mTVtitle.setText( mTitle );
    setTitle( R.string.title_note );

    load();

    mButtonOK.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
    ( (Button) findViewById(R.id.button_cancel ) ).setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    if ( b == mButtonOK ) {
      save();
    }
    // mButtonCancel dismiss
    dismiss();
  }

}


