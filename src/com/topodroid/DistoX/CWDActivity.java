/* @file CWDActivity.java
 *
 * @author marco corvi
 * @date feb 2015
 *
 * @brief TopoDroid CWD activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.util.List;
import java.util.ArrayList;

import android.app.Application;
import android.app.Activity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.LinearLayout;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.KeyEvent;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import android.util.Log;

public class CWDActivity extends Activity
                         implements OnItemClickListener
                                  , OnClickListener

{
  private TopoDroidApp mApp;

  private ListView mList;
  private EditText mETcwd;
  private EditText mETcbd;
  private Button mBtnOK;

  private void setPreference()
  {
    String dir_name  = mETcwd.getText().toString();
    String base_name = TDLevel.overExpert ? mETcbd.getText().toString() : mApp.mCBD ;
    if ( dir_name == null ) return;
    dir_name.trim();
    if ( dir_name.length() == 0 ) return;
    if ( dir_name.indexOf("/") >= 0 ) {
      Toast.makeText( this, R.string.bad_cwd, Toast.LENGTH_SHORT ).show();
      return;
    }
    if ( ! dir_name.toUpperCase().startsWith( "TOPODROID" ) ) {
      dir_name = "TopoDroid-" + dir_name;
    } else { 
      dir_name = "TopoDroid" + dir_name.substring(9);
    }
    if ( base_name == null || base_name.length() == 0 ) base_name = TDPath.PATH_BASEDIR;

    if ( TDPath.checkBasePath( dir_name ) ) {
      Log.v("DistoX", "dir name <" + dir_name + "> base dir <" + base_name + ">" );
      mApp.setCWDPreference( dir_name, base_name );
      Intent intent = new Intent();
      intent.putExtra( TDTag.TOPODROID_CWD, dir_name );
      setResult( RESULT_OK, intent );
    } else {
      setResult( RESULT_CANCELED );
    }
  }
    
  public void updateDisplay( )
  {
    File[] dirs = TDPath.getTopoDroidFiles();
    ArrayAdapter<String> adapter = new ArrayAdapter<>( this, R.layout.menu );
    for ( File item : dirs ) {
      adapter.add( item.getName() );
    }
    mList.setAdapter( adapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mETcwd.setText( mApp.mCWD );
    if ( TDLevel.overExpert ) {
      mETcbd.setText( mApp.mCBD );
    } else{
      mETcbd.setVisibility( View.GONE );
    }
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    CharSequence item = ((TextView) view).getText();
    mETcwd.setText( item );
  }

  @Override
  public void onClick( View v ) 
  {
    Button b = (Button)v;
    if ( b == mBtnOK ) {
      setPreference();
      finish();
    }
  }
  
  @Override
  public void onCreate( Bundle b )
  {
    super.onCreate( b );
    setContentView(R.layout.cwd_activity);
    mApp = (TopoDroidApp) getApplication();

    getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN );

    mList = (ListView) findViewById( R.id.cwd_list );
    mETcwd = (EditText) findViewById( R.id.cwd_text );
    mETcbd = (EditText) findViewById( R.id.cbd_text );
    mBtnOK = (Button) findViewById( R.id.button_ok );
    
    mBtnOK.setOnClickListener( this );
    updateDisplay();
  }

  // @Override
  // public synchronized void onStop()
  // { 
  //   super.onStop();
  // }

  // @Override
  // public void onBackPressed()
  // {
  //   // setPreference();
  //   finish();
  // }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.CWDActivity );
        if ( help_page != null ) UserManualActivity.showHelpPage( this, help_page );
        return true;
      // case KeyEvent.KEYCODE_SEARCH:
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

}
