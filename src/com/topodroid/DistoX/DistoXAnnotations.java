/* @file DistoXAnnotations.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid survey shot dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20130307 made Annotations into a dialog
 * 201311   icon for the button OK
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

// import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;

import android.content.Intent;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;


public class DistoXAnnotations extends Dialog // Activity
                               implements View.OnClickListener
{
  // private TextView mTVtitle;
  private EditText mETtext;
  private Button   mButtonOK;
  // private Button   mButtonCancel;
  private String   mFilename;
  private String   mTitle;

  DistoXAnnotations( Context context, String title )
  {
    super( context );
    mTitle = title;
  }

  private void load( )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_NOTE, "reading from file " + mFilename );
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
      TopoDroidLog.Log(  TopoDroidLog.LOG_ERR, "load IOexception " + e.toString() );
    }
  }

  private void save( )
  {
    try {
      TopoDroidApp.checkPath( mFilename );
      FileWriter fw = new FileWriter( mFilename, false );
      PrintWriter pw = new PrintWriter( fw );
      pw.format( "%s", mETtext.getText() );
      fw.flush();
      fw.close();
    } catch ( IOException e ) {
      TopoDroidLog.Log(  TopoDroidLog.LOG_ERR, "save IOexception " + e.toString() );
    }
  }

  public static void append( String title, String text )
  {
    String filename = TopoDroidPath.getSurveyNoteFile( title );
    try {
      TopoDroidApp.checkPath( filename );
      FileWriter fw = new FileWriter( filename, true );
      PrintWriter pw = new PrintWriter( fw );
      pw.format( "%s", text );
      fw.flush();
      fw.close();
    } catch ( IOException e ) {
      TopoDroidLog.Log(  TopoDroidLog.LOG_ERR, "append IOexception " + e.toString() );
    }
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.distox_annotations);
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    // mTVtitle  = (TextView) findViewById(R.id.note_title );
    mETtext   = (EditText) findViewById(R.id.note_text );
    mButtonOK = (Button) findViewById(R.id.button_ok );
    // mButtonCancel = (Button) findViewById(R.id.button_cancel );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );
    mFilename = TopoDroidPath.getSurveyNoteFile( mTitle );
    // mTVtitle.setText( mTitle );
    setTitle( R.string.title_note );

    load();

    mButtonOK.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    Button b = (Button) v;
    if ( b == mButtonOK ) {
      save();
    }
    dismiss();
  }

}


