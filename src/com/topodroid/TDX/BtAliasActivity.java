/* @file BtAliasActivity.java
 *
 * @author marco corvi
 * @date may 2024
 *
 * @brief TopoDroid BT alias activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.dev.Device;
import com.topodroid.utils.TDLog;
import com.topodroid.help.UserManualActivity;

// import java.util.Locale;

// import android.app.Application;
import android.app.Activity;
import android.os.Bundle;

// import android.content.Context;
// import android.content.Intent;
// import android.content.res.Resources;
// import android.content.res.Configuration;

import android.widget.Spinner;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView;
// import android.widget.LinearLayout;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.KeyEvent;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.Map;

public class BtAliasActivity extends Activity
                         implements OnClickListener
                         , AdapterView.OnItemSelectedListener
                         , OnItemClickListener

{
  // private TopoDroidApp mApp;
  private ListView mList;
  private EditText mETalias;
  private EditText mETcode;
  private Spinner  mSPmodel;
  private Button mBtnHelp;
  private Button mBtnAdd;
  private Button mBtnClose;

  private String mModel = null;
  private int mSelectedPos = -1;
  
  private boolean setBtAlias()
  {
    String alias = mETalias.getText().toString();
    if ( alias == null || alias.isEmpty() ) {
      mETalias.setError( getResources().getString( R.string.bt_alias_missing ) );
      return false;
    }
    String bt_name = null;
    if ( mSelectedPos == 0 ) {
      bt_name = mModel;
    } else {
      String code  = mETcode.getText().toString();
      if ( code == null || code.isEmpty() ) {
        mETcode.setError( getResources().getString( R.string.bt_code_missing ) );
        return false;
      }
      bt_name = mModel + code;
    } 
    if ( bt_name != null ) {
      TopoDroidApp.mDData.setAlias( alias, bt_name );
      return true;
    } else {
      TDLog.Error("Set Alias " + alias + " null BT name" );
    }
    return false;
  }

  private void removeAlias( String alias )
  {
    TopoDroidApp.mDData.deleteAlias( alias );
  }
    
  private void updateDisplay()
  {
    // File[] dirs = TDPath.getTopoDroidFiles( mBaseName );
    Map< String, String > names = TopoDroidApp.mDData.selectAllAlias();
    ArrayAdapter<String> adapter = new ArrayAdapter<>( this, R.layout.menu );
    if ( names != null ) {
      for ( String alias : names.keySet() ) {
        adapter.add( alias + " : " + (String)(names.get( alias )) );
      }
    }
    mList.setAdapter( adapter );
    mList.setDividerHeight( 2 );
  }

  // ---------------------------------------------------------------

  /** react to an item selection
   * @param av    item adapter
   * @param v     item view
   * @param pos   item position
   * @param id    ?
   */
  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) 
  {
    mModel = Device.mModels[ pos ];
    mSelectedPos = pos;
  }

  /** react to a deselection
   * @param av    item adapter
   */
  @Override
  public void onNothingSelected( AdapterView av ) 
  { 
    mModel = null;
    mSelectedPos = -1;
  }

  @Override
  public void onClick( View v ) 
  {
    Button b = (Button)v;
    if ( b == mBtnAdd ) {
      if ( ! setBtAlias() ) return;
      finish();
    } else if ( b == mBtnClose ) {
      finish();
    } else if ( b == mBtnHelp ) {
      String help_page = getResources().getString( R.string.BtAliasActivity );
      /* if ( help_page != null ) always true */ UserManualActivity.showHelpPage( this, help_page );
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( ! ( view instanceof TextView ) ) {
      TDLog.Error("BtAlias view instance of " + view.toString() );
      return;
    }
    String item = ((TextView) view).getText().toString();
    int idx = item.indexOf(" : ");
    removeAlias( item.substring( 0, idx ) );
    updateDisplay();
  }
  
  @Override
  public void onCreate( Bundle b )
  {
    super.onCreate( b );

    TDandroid.setScreenOrientation( this );

    setContentView(R.layout.bt_alias_activity);
    // mApp = (TopoDroidApp) getApplication();
    // mBaseName = TDInstance.cbd;

    getWindow().setSoftInputMode( WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN );

    mList = (ListView) findViewById( R.id.alias_list );
    mList.setOnItemClickListener( this );

    mETalias = (EditText) findViewById( R.id.bt_alias);
    mETcode  = (EditText) findViewById( R.id.bt_code);
    mSPmodel = (Spinner) findViewById( R.id.bt_model );

    mSPmodel.setOnItemSelectedListener( this );
    ArrayAdapter adapter = new ArrayAdapter<>( this, R.layout.menu, Device.mModelNames );
    mSPmodel.setAdapter( adapter );

    mBtnAdd = (Button) findViewById( R.id.button_add );
    mBtnAdd.setOnClickListener( this );
    mBtnClose = (Button) findViewById( R.id.button_close );
    mBtnClose.setOnClickListener( this );
    mBtnHelp = (Button) findViewById( R.id.button_help );
    mBtnHelp.setOnClickListener( this );

    updateDisplay();

    setTitle( R.string.bt_alias_title );
  }

  // @Override
  // public synchronized void onStop()
  // { 
  //   super.onStop();
  // }

  // @Override
  // public void onBackPressed()
  // {
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
        String help_page = getResources().getString( R.string.BtAliasActivity );
        /* if ( help_page != null ) always true */ UserManualActivity.showHelpPage( this, help_page );
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
