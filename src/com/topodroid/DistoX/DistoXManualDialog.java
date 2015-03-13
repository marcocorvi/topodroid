/* @file DistoXManualDialog.java
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
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringWriter;
import java.io.PrintWriter;

import android.app.Activity;
// import android.app.Dialog;
import android.os.Bundle;
// import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;

import android.widget.TextView;
// import android.widget.Button;
// import android.widget.SlidingDrawer;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;


import android.view.View;
import android.view.View.OnClickListener;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import android.webkit.WebView;

import android.util.Log;

public class DistoXManualDialog extends Activity
                                implements OnItemClickListener, OnClickListener
{
  // private TextView mTVtitle;
  // private TextView mTVtext;
  private WebView mTVtext;

  // private Button   mButtonOK;
  // private Button   mButtonCancel;
  // private String   mTitle;

  private void load( String filename )
  {
    StringBuilder html = new StringBuilder();
    // try {
    //   FileReader fr = new FileReader( filename );
    //   BufferedReader br = new BufferedReader( fr );
    //   String line = br.readLine();
    //   while ( line != null ) {
    //     html.append( line );
    //     html.append( " " );
    //     // mTVtext.append( line + "\n" );
    //     line = br.readLine();
    //   }
    //   fr.close();
    //   mTVtext.loadData( html.toString(), "text/html", null );
    // } catch ( IOException e ) {
    //   TopoDroidLog.Log(  TopoDroidLog.LOG_ERR, "load IOexception " + e.toString() );
    // }
    mTVtext.loadUrl("file:///android_asset/man/" + filename );
  }

  private void getManualFromWeb()
  {
    if ( TopoDroidApp.mManual.startsWith("http") ) {
      try {
        // TopoDroidHelp.show( this, R.string.help_topodroid );
        startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( TopoDroidApp.mManual )));
      } catch ( ActivityNotFoundException e ) {
        Toast.makeText( this, "Cannot retrieve user manual from the web", Toast.LENGTH_SHORT ).show();
      }
    }
  }

// -------------------------------------------------------------------
  // SlidingDrawer mDrawer;
  ImageView     mImage;
  ListView      mList;

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.distox_manual_dialog);
    // mTVtitle  = (TextView) findViewById(R.id.manual_title );
    mTVtext   = (WebView) findViewById(R.id.manual_text );
    // mButtonOK = (Button) findViewById(R.id.button_ok );
    // mButtonCancel = (Button) findViewById(R.id.button_cancel );

    // Bundle extras = getIntent().getExtras();
    // String title  = extras.getString( TopoDroidApp.TOPODROID_SURVEY );
    // mTVtitle.setText( mTitle );

    setTitle( R.string.title_manual );
    // load( TopoDroidPath.getManFile( "manual00.txt" ) );
    load( "manual00.htm" );

    // mButtonOK.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );


    // mDrawer = (SlidingDrawer) findViewById( R.id.drawer );
    // mDrawer.unlock();

    mImage  = (ImageView) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mList = (ListView) findViewById( R.id.content );

    ArrayAdapter< String > adapter = new ArrayAdapter<String>(this, R.layout.message );
    adapter.add("Preface");        // manual00
    adapter.add("1. Introducion");
    adapter.add("2. Database");
    adapter.add("3. Auxiliary apps");
    adapter.add("4. Main window");      // manual04
    adapter.add("5. Device window");
    adapter.add("6. Calibration window");
    adapter.add("7. Calibration data");
    adapter.add("8. Survey data");
    adapter.add("9. Shot list");
    adapter.add("10. Survey info");    // manual09
    adapter.add("11. Sketch window");
    adapter.add("12. Sketch drawing");
    adapter.add("13. Cross-sections");
    adapter.add("14. The final map");
    if ( TopoDroidApp.mCosurvey ) adapter.add("15. Co-surveying");
    adapter.add("* Website");
 
    mList.setAdapter( adapter );
    mList.setVisibility( View.GONE );
    mList.invalidate();
    mList.setOnItemClickListener( this );
  }


  @Override 
  public void onClick(View v) 
  {
    // When the user clicks, just finish this activity.
    // onPause will be called, and we save our data there.
    ImageView b = (ImageView) v;
    if ( b == mImage ) {
      // Log.v("DistoX", "clicked image" );
      if ( mList.getVisibility() == View.VISIBLE ) {
        mList.setVisibility( View.GONE );
      } else {
        mList.setVisibility( View.VISIBLE );
      }
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    CharSequence item = ((TextView) view).getText();
    // Log.v("DistoX", "click " + item + " pos " + pos);
    mList.setVisibility( View.GONE );
    int max = ( TopoDroidApp.mCosurvey )? 15 : 14;
    if ( pos <= max ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      // pw.format( "manual%02d.txt", pos );
      // load( TopoDroidPath.getManFile( sw.getBuffer().toString() ) );
      pw.format( "manual%02d.htm", pos );
      load( sw.getBuffer().toString() );
    } else {
      getManualFromWeb();
    }
  }

}


