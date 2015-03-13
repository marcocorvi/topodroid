/** @file DeviceRemote.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device remote control
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
import android.content.Intent;
// import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

class DeviceRemote extends Dialog
                   implements View.OnClickListener
{
  private Button mBTlaserOn;
  private Button mBTlaserOff;
  private Button mBTmeasure;
  private Button mBTreset;
  private CheckBox mCBdownload;

  // private DataDowloader mDataDowloader;
  private ArrayList<ILister> mLister;

  // private Button mBTback;

  // DeviceActivity mParent;
  TopoDroidApp   mApp;

  DeviceRemote( Context context,
                ILister lister,
                TopoDroidApp app )
  {
    super( context );
    mApp    = app;
    mLister = new ArrayList<ILister>();
    if ( lister != null ) mLister.add( lister );
  }


  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView( R.layout.device_remote );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( mApp.getResources().getString( R.string.remote_title ) );

    mBTlaserOn  = (Button) findViewById( R.id.remote_on );
    mBTlaserOff = (Button) findViewById( R.id.remote_off );
    mBTmeasure  = (Button) findViewById( R.id.remote_measure );
    mBTreset    = (Button) findViewById( R.id.remote_reset );
    // mBTback     = (Button) findViewById( R.id.button_back );
    mCBdownload = (CheckBox) findViewById( R.id.remote_download );

    mBTlaserOn.setOnClickListener( this );
    mBTlaserOff.setOnClickListener( this );
    mBTmeasure.setOnClickListener( this );
    mBTreset.setOnClickListener( this );
    // mBTback.setOnClickListener( this );
  }

  @Override
  public void onClick( View view )
  {
    boolean download = mCBdownload.isChecked();
    switch ( view.getId() ) {
      case R.id.remote_on:
        mApp.setX310Laser( 1, mLister );
        break;
      case R.id.remote_off:
        mApp.setX310Laser( 0, mLister );
        break;
      case R.id.remote_measure:
        mApp.setX310Laser( (download ? 3 : 2), mLister );
        // if ( mDataDownloader != null ) mDataDownloader.downloadData();
        break;
      case R.id.remote_reset:
        mApp.resetComm();
        Toast.makeText(mApp, R.string.bt_reset, Toast.LENGTH_SHORT).show();
        break;
      // case R.id.button_back:
      //   dismiss();
      //   break;
    }
  }

}
