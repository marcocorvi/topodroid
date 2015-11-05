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
  private WebView mTVtext;

  private void load( String filename )
  {
    StringBuilder html = new StringBuilder();
    mTVtext.loadUrl("file:///android_asset/man/" + filename );
  }

  private void getManualFromWeb()
  {
    if ( TopoDroidApp.mManual.startsWith("http") ) {
      try {
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
    mTVtext   = (WebView) findViewById(R.id.manual_text );

    setTitle( R.string.title_manual );
    load( "manual00.htm" );

    mImage  = (ImageView) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mList = (ListView) findViewById( R.id.content );

    ArrayAdapter< String > adapter = new ArrayAdapter<String>(this, R.layout.message );
    adapter.add("Preface");        // manual00
    adapter.add("1. Introducion");
    adapter.add("2. Main window");      // manual04
    adapter.add("3. Device window");
    adapter.add("4. Calibration window");
    adapter.add("5. Calibration data");
    adapter.add("6. Survey data");
    adapter.add("7. Shot list");
    adapter.add("8. Survey info");    // manual09
    adapter.add("9. Sketch window");
    adapter.add("10. Sketch drawing");
    adapter.add("11. Cross-sections");
    adapter.add("12. Overview window");
    adapter.add("13. Import/Export");
    adapter.add("14. Index");
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
    if ( pos <= 14 ) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter( sw );
      pw.format( "manual%02d.htm", pos );
      load( sw.getBuffer().toString() );
    } else {
      getManualFromWeb();
    }
  }

  @Override
  public void onBackPressed()
  {
    String url = mTVtext.getUrl();
    if ( url.indexOf("manual") >= 0 ) finish();
    mTVtext.goBack();
  }

}


