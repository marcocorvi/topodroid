/* @file UserManualActivity.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid user manual actvity with a webview
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.help;

import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.prefs.TDSetting;
import com.topodroid.DistoX.TDandroid;
import com.topodroid.DistoX.TDPath;
import com.topodroid.DistoX.TDToast;
import com.topodroid.DistoX.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;

import java.net.URI;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Locale;

import android.app.Activity;
import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.content.ActivityNotFoundException;
import android.net.Uri;

import android.widget.TextView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import android.view.View;
import android.view.View.OnClickListener;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.webkit.WebView;
import android.webkit.WebSettings;
import android.webkit.WebViewClient;

public class UserManualActivity extends Activity
                                implements OnItemClickListener, OnClickListener
{
  private static final String NEEDLE = "DistoX/files/man";
  private WebView mTVtext;
  private int mCloseOnBack = 0;

  private void load( WebView view, String filename )
  {
    ++mCloseOnBack;
    // String filepath = TDPath.getManFile( filename );
    // view.loadUrl( filepath );
    // setWebViewSettings( mTVtext );
    // TDLog.v( "MAN-0 filename " + filename );

    if ( filename.startsWith("file:///data" ) ) {
      if ( TDSetting.mLocalManPages ) {
        int pos = filename.indexOf( NEEDLE );
        // TDLog.v( "MAN-1 filename " + filename + " index " + pos );
        if ( pos > 0 ) {
          String name = filename.substring( pos + NEEDLE.length() );
          String pagename = TDPath.getManFileName( name );
          File pagefile = TDFile.getManFile( name );
          // TDLog.v( "MAN-2 pagefile " + pagefile.getPath() );
          // TDLog.v( "MAN-2 pagename " + pagename );
          loadLocal( view, pagefile );
        } 
      }
    } else {
      int pos = filename.lastIndexOf("/");
      if ( pos >= 0 ) filename = filename.substring(pos+1);
      String pagename = TDPath.getManFileName( filename );
      File pagefile = TDFile.getManFile( filename );
      // TDLog.v( "MAN-3 filename " + filename );
      // TDLog.v( "MAN-3 pagename " + pagename );
      // TDLog.v( "MAN-3 pagefile path " + pagefile.getPath() );
      if ( ! ( TDSetting.mLocalManPages && pagefile.exists() ) ) {
        String page = "/android_asset/man/" + filename;
        // TDLog.v( "MAN-4 assets page " + page );
        view.loadUrl( "file://" + page );
      } else {
        // TDLog.v( "MAN-4 local pagefile " + pagefile );
        loadLocal( view, pagefile );
      }
    }
  }
   
  private void loadLocal( WebView view, File pagefile )
  {
    // view.loadUrl( "file://" + page );
    StringBuilder pagedata = new StringBuilder();
    String encoding = "UTF-8";
    String mime = "text/html";
    String baseurl = "file://" + pagefile.getPath();
    // TDLog.v( "MAN-5 baseurl " + baseurl );
    try {
      FileReader fr = new FileReader( pagefile );
      encoding = fr.getEncoding();
      BufferedReader br = new BufferedReader( fr );
      String line;
      while ( ( line = br.readLine() ) != null ) {
        pagedata.append( line );
      }
      fr.close();
    } catch (IOException e ) {}
    view.loadDataWithBaseURL( baseurl, pagedata.toString(), mime, encoding, null );

    // try { 
    //   URI pageuri = pagefile.toURI();
    //   TDLog.v( "MAN url " +  pageuri.toURL().toString() );
    //   view.loadUrl( pageuri.toURL().toString() );
    // } catch ( MalformedURLException e ) {
    //   TDLog.v( "MAN error " + e.getMessage() );
    // }
  }

  // private void getManualFromWeb()
  // {
  //   String manual = getResources().getString( R.string.topodroid_man );
  //   if ( manual.startsWith("http") ) {
  //     viewUrl( manual );
  //   }
  // }
 
  // private void viewUrl( String uri_string )
  // {
  //   try {
  //     startActivity( new Intent( Intent.ACTION_VIEW, Uri.parse( uri_string )));
  //   } catch ( ActivityNotFoundException e ) {
  //     TDToast.makeBad( R.string.no_manual );
  //   }
  // }

// -------------------------------------------------------------------
  // SlidingDrawer mDrawer;
  private ImageView     mImage;
  private ListView      mList;

  private void setWebViewSettings( WebView view )
  {
    // WebSettings ws = view.getSettings();
    // view.getSettings().setAllowContentAccess( true );
    // view.getSettings().setAllowFileAccess( true );
    // view.getSettings().setBlockNetworkImage( false );
    // view.getSettings().setBlockNetworkLoads( false );
    view.getSettings().setLoadsImagesAutomatically( true ); 
    // view.getSettings().setDomStorageEnabled( true ); // does not solve problem with images
    // view.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
    view.getSettings().setAllowFileAccess( true );
    view.getSettings().setAllowContentAccess( true );
    view.getSettings().setAllowFileAccessFromFileURLs( true );
    view.getSettings().setAllowUniversalAccessFromFileURLs( true );

    view.getSettings().setJavaScriptEnabled( false ); // no JS
    view.getSettings().setSupportZoom( true ); 
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    TDandroid.setScreenOrientation( this );

    mCloseOnBack = 0;
    // get intent extra
    String page = null;
    Bundle extras = getIntent().getExtras();
    if ( extras != null ) page = extras.getString( TDTag.TOPODROID_HELP_PAGE );
    if ( page == null ) page = "manual00.htm";

    setContentView(R.layout.distox_manual_dialog);
    mTVtext   = (WebView) findViewById(R.id.manual_text );
 
    setWebViewSettings( mTVtext );

    mTVtext.setWebViewClient( new WebViewClient() {
      @Override 
      public boolean shouldOverrideUrlLoading( WebView view, String url ) {
        ++mCloseOnBack;
        // view.loadUrl( url );
        // TDLog.v( "MAN Web client " + url );
        load( view, url );
        return false;
      }

      @Override
      public void onReceivedError(WebView view, int errorCode, String description, String failingUrl)
      {
        TDLog.Error( "UserMan Load Error: " + description + " url " + failingUrl );
      }
    } );

    setTitle( R.string.title_manual );
    load( mTVtext, page );

    mImage  = (ImageView) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mList = (ListView) findViewById( R.id.content );

    ArrayAdapter< String > adapter = new ArrayAdapter<>(this, R.layout.message );
    adapter.add( getResources().getString( R.string.man_preface ) );
    adapter.add( getResources().getString( R.string.man_intro ) );
    adapter.add( getResources().getString( R.string.man_main ) );
    adapter.add( getResources().getString( R.string.man_device ) );
    adapter.add( getResources().getString( R.string.man_calib ) );
    adapter.add( getResources().getString( R.string.man_gm ) );
    adapter.add( getResources().getString( R.string.man_survey ) );
    adapter.add( getResources().getString( R.string.man_shot ) );
    adapter.add( getResources().getString( R.string.man_info ) );
    adapter.add( getResources().getString( R.string.man_sketch ) );
    adapter.add( getResources().getString( R.string.man_draw ) );
    adapter.add( getResources().getString( R.string.man_xsection ) );
    adapter.add( getResources().getString( R.string.man_overview ) );
    adapter.add( getResources().getString( R.string.man_export ) );
    adapter.add( getResources().getString( R.string.man_project ) );
    adapter.add( getResources().getString( R.string.man_threed ) );
    adapter.add( getResources().getString( R.string.man_index ) );
    adapter.add( getResources().getString( R.string.man_website ) );
 
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
    // TDLog.v( "click " + item + " pos " + pos);
    mList.setVisibility( View.GONE );
    if ( pos <= 16 ) {
      mCloseOnBack = 0;
      load( mTVtext, String.format(Locale.US, "manual%02d.htm", pos ) );
    } else {
      // getManualFromWeb();
      TDToast.makeBad( R.string.no_manual );
    }
  }

  @Override
  public void onBackPressed()
  {
    mCloseOnBack -= 2;
    if ( mCloseOnBack <= 0 ) finish();
    mTVtext.goBack();
  }

  // static void show Help Page( Context context, int class_string )
  // {
  //   Intent intent = new Intent( Intent.ACTION_VIEW );
  //   intent.setClass( context, UserManualActivity.class );
  //   String page = context.getResources().getString( class_string );
  //   if ( page != null ) { 
  //     intent.putExtra( TDTag.TOPODROID_HELP_PAGE, page );
  //   }
  //   context.startActivity( intent );
  // }
  
  public static void showHelpPage( Context context, String page )
  {
    // if ( page == null ) return;
    Intent intent = new Intent( Intent.ACTION_VIEW );
    intent.setClass( context, UserManualActivity.class );
    intent.putExtra( TDTag.TOPODROID_HELP_PAGE, page );
    context.startActivity( intent );
  }

}


