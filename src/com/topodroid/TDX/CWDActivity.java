/* @file CWDActivity.java
 *
 * @author marco corvi
 * @date feb 2015
 *
 * @brief TopoDroid CWD activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.CWDfolder;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDTag;
// import com.topodroid.utils.TDFile;
import com.topodroid.prefs.TDSetting;
import com.topodroid.help.UserManualActivity;

// import java.io.File;
import java.util.Locale;
// import java.util.List;
// import java.util.ArrayList;

// import android.app.Application;
import android.app.Activity;
import android.os.Bundle;

// import android.content.Context;
import android.content.Intent;
// import android.content.res.Resources;
// import android.content.res.Configuration;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.LinearLayout;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.KeyEvent;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class CWDActivity extends Activity
                         implements OnItemClickListener
                                  , OnItemLongClickListener
                                  , OnClickListener

{
  // private TopoDroidApp mApp;
  private ListView mList;
  private EditText mETcwd;
  // private TextView mTVcbd;
  private Button mBtnOK;
  // private Button mBtnChange;
  private Button mBtnCancel;
  // private LinearLayout mLayoutcbd;
  
  // private String mBaseName;

  // void setBasename( String basename ) 
  // { 
  //   // TDLog.v( "set  base name " + basename );
  //   mBaseName = basename;
  //   updateDisplay();
  // }

  // /** react to a change in the configuration
  //  * @param cfg   new configuration
  //  */
  // @Override
  // public void onConfigurationChanged( Configuration new_cfg )
  // {
  //   super.onConfigurationChanged( new_cfg );
  // }

  private boolean setCwdPreference()
  {
    String dir_name = mETcwd.getText().toString();
    // String base_name = TDPath.getCurrentBaseDir();

    // CURRENT WORK DIRECTORY
    dir_name = dir_name.trim();
    int check = CWDfolder.checkName( dir_name );
    if ( check == CWDfolder.NAME_EMPTY ) {
      TDToast.makeBad( R.string.empty_cwd );
      return false;
    } else if ( check == CWDfolder.NAME_BAD ) {
      TDToast.makeBad( R.string.bad_cwd );
      return false;
    }
    dir_name = CWDfolder.folderName( dir_name );

    // return the result to TDPrefActivity
    if ( TDPath.checkBasePath( dir_name /*, base_name */ ) ) {
      // TDLog.Log( TDLog.LOG_PATH, "CWD set dir <" + dir_name + /* "> base <" + base_name + */ ">" );
      TopoDroidApp.setCWDPreference( dir_name /* , base_name */ );
      Intent intent = new Intent();
      intent.putExtra( TDTag.TOPODROID_CWD, dir_name );
      setResult( RESULT_OK, intent );
    } else {
      // TDLog.Log( TDLog.LOG_PATH, "CWD Failed to set dir <" + dir_name + /* "> base <" + base_name + */ ">" );
      setResult( RESULT_CANCELED );
    }
    return true;
  }
    
  private void updateDisplay()
  {
    // File[] dirs = TDPath.getTopoDroidFiles( mBaseName );
    String[] dirs = TDPath.getTopoDroidFiles( TDInstance.cbd );
    ArrayAdapter<String> adapter = new ArrayAdapter<>( this, R.layout.menu );
    if ( dirs != null ) {
      for ( String item : dirs ) {
        adapter.add( item );
      }
    }
    mList.setAdapter( adapter );
    mList.setOnItemClickListener( this );
    mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );

    mETcwd.setText( TDInstance.cwd );
    // if ( TDLevel.overExpert ) {
    //   mTVcbd.setText( mBaseName );
    // } else{
      // mLayoutcbd.setVisibility( View.GONE );
    // }
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( ! ( view instanceof TextView ) ) {
      TDLog.e("CWD view instance of " + view.toString() );
      return;
    }
    CharSequence item = ((TextView) view).getText();
    mETcwd.setText( item );
  }

  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( ! ( view instanceof TextView ) ) {
      TDLog.e("CWD view instance of " + view.toString() );
      return false;
    }
    CharSequence item = ((TextView) view).getText();
    mETcwd.setText( item );
    setCwdAndFinish();
    return true;
  }

  private void setCwdAndFinish()
  {
    setCwdPreference();
    finish();
  }

  @Override
  public void onClick( View v ) 
  {
    Button b = (Button)v;
    if ( b == mBtnOK ) {
      setCwdAndFinish();
    } else if ( b == mBtnCancel ) {
      finish();
    // } else if ( b == mBtnChange ) {
    //   new CBDdialog( this, this, mBaseName ).show();
    }
  }
  
  @Override
  public void onCreate( Bundle b )
  {
    super.onCreate( b );

    getWindow().getDecorView().setSystemUiVisibility( TDSetting.mUiVisibility );

    TDandroid.setScreenOrientation( this );

    setContentView(R.layout.cwd_activity);
    // mApp = (TopoDroidApp) getApplication();
    // mBaseName = TDInstance.cbd;

    getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN );

    // mLayoutcbd = (LinearLayout)findViewById( R.id.layout_cbd );

    mList = (ListView) findViewById( R.id.cwd_list );
    mETcwd = (EditText) findViewById( R.id.cwd_text );
    // mTVcbd = (TextView) findViewById( R.id.cbd_text );
    mBtnOK = (Button) findViewById( R.id.button_ok );
    mBtnOK.setOnClickListener( this );
    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );
    // mBtnChange = (Button) findViewById( R.id.button_change );
    // if ( TDPath.BELOW_ANDROID_10 && TDLevel.overExpert ) {
    //   mBtnChange.setOnClickListener( this );
    // } else {
    //   mBtnChange.setVisibility( View.GONE );
    // }

    updateDisplay();

    setTitle( R.string.cwd );
  }

  // @Override
  // public synchronized void onStop()
  // { 
  //   super.onStop();
  // }

  // @Override
  // public void onBackPressed()
  // {
  //   // setCwdPreference();
  //   finish();
  // }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      case KeyEvent.KEYCODE_MENU:   // HARDWARE MENU (82)
        String help_page = getResources().getString( R.string.CWDActivity );
        /* if ( help_page != null ) always true */ UserManualActivity.showHelpPage( this, help_page );
        return true;
      // case KeyEvent.KEYCODE_SEARCH:
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.e( "key down: code " + code );
    }
    return false;
  }

}
