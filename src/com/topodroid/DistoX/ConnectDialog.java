/* @file ConnectDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX device activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120523 radio buttons: batch - continuous
 * 20120525 using mApp.mConnectionMode
 * 20120715 per-category preferences
 * 20120726 TopoDroid log
 * 20121121 bug-fix check that device is "DistoX" to put it on the list
 * 20131201 button bar new interface. reorganized actions
 * 20140719 write memory dump to file (X310 only)
 */
package com.topodroid.DistoX;

// import java.Thread;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import android.app.Dialog;
import android.os.Bundle;
import android.os.AsyncTask;

import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import android.widget.Toast;

// import android.util.Log;

import android.bluetooth.BluetoothDevice;

public class ConnectDialog extends Dialog
                           implements View.OnClickListener
                           , OnItemClickListener
{
  private static final int REQUEST_DEVICE    = 1;

  private TextView mTVaddress;

  // private ArrayAdapter<String> mArrayAdapter;
  private ListItemAdapter mArrayAdapter;
  private ListView mList;

  // private String mAddress;

  // private Button mBtnCancel;
  private Button mBtnConnect;
  private Button mBtnDisconnect;
  // private Button mBtnStart;
  // private Button mBtnStop;
  private Button mBtnSync;

  private TextView mTVstate;

  private Context mContext;
  private TopoDroidApp mApp;

  private String mName;
  Set<BluetoothDevice> mDevices;

  // void setButtons( int state ) 
  // {
  //   switch ( state ) {
  //     case SyncService.STATE_NONE:
  //       mBtnConnect.setText( R.string.menu_attach );
  //       mBtnStart.setText( R.string.button_start );
  //       break;
  //     case SyncService.STATE_LISTEN:
  //       mBtnStart.setText( R.string.menu_ready );
  //       break;
  //     case SyncService.STATE_CONNECTING:
  //       break;
  //     case SyncService.STATE_CONNECTED:
  //       mBtnConnect.setText( R.string.menu_attached );
  //       break;
  //   }
  // }

  ConnectDialog( Context context, TopoDroidApp app )
  {
    super( context );
    mContext = context;
    mApp     = app;
    mName    = null; // mApp.getConnectedDeviceName();
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    // mAddress = getIntent().getExtras().getString(   TopoDroidApp.TOPODROID_DEVICE_ADDR );

    setContentView( R.layout.connect_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    mTVaddress = (TextView) findViewById( R.id.device_address );
    mTVstate   = (TextView) findViewById( R.id.conn_state );

    if ( mApp.getAcceptState() == SyncService.STATE_LISTEN ) {
      mTVstate.setText( "LISTEN | " + mApp.getConnectionStateStr() );
    } else {
      mTVstate.setText( mApp.getConnectionStateStr() );
    }

    // mArrayAdapter = new ArrayAdapter<String>( this, R.layout.message );
    mArrayAdapter = new ListItemAdapter( mContext, R.layout.message );
    mList = (ListView) findViewById(R.id.dev_list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    // mList.setLongClickable( true );
    // mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnConnect = (Button) findViewById( R.id.button_connect );
    mBtnDisconnect = (Button) findViewById( R.id.button_disconnect );
    // mBtnStart = (Button) findViewById( R.id.button_start );
    // mBtnStop = (Button) findViewById( R.id.button_stop );
    mBtnSync = (Button) findViewById( R.id.button_sync );

    // mBtnCancel.setOnClickListener( this );
    mBtnConnect.setOnClickListener( this );
    mBtnDisconnect.setOnClickListener( this );
    // mBtnStart.setOnClickListener( this );
    // mBtnStop.setOnClickListener( this );
    mBtnSync.setOnClickListener( this );

    setTitle( R.string.title_device );
    updateList();
  }

  private void updateList( )
  {
    // TopoDroidLog.Log(TopoDroidLog.LOG_MAIN, "updateList" );
    // mList.setAdapter( mArrayAdapter );
    mArrayAdapter.clear();
    if ( mApp.mBTAdapter != null ) {
      mDevices = mApp.mBTAdapter.getBondedDevices(); // get paired devices
      if ( mDevices.isEmpty() ) {
        // Toast.makeText(this, R.string.no_paired_device, Toast.LENGTH_SHORT).show();
      } else {
        for ( BluetoothDevice device : mDevices ) {
          // String addr = device.getAddress();
          String name = device.getName();
          if ( ! name.startsWith( "DistoX", 0 ) ) {
            mArrayAdapter.add( name );
          }
        }
      }
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    CharSequence item = ((TextView) view).getText();
    String name = item.toString();
    // if ( mName != null && ! mName.equals( name ) ) {
    //   if ( mApp.getConnectionType() == SyncService.STATE_CONNECTED ) {
    //     disconnectDevice(); // FIXME do this ?
    //   // } else if ( mApp.getConnectionType() == SyncService.STATE_LISTEN ) {
    //   //   stopDevice();
    //   }
    // }
    mName = name;
    mTVaddress.setText( mName );
  }

  private void connectDevice()
  {
    // Log.v("DistoX", "connectDevice state " + mApp.getConnectionStateStr() );
    for ( BluetoothDevice device : mDevices ) {
      if ( mName.equals( device.getName() ) ) {
        mApp.connectRemoteTopoDroid( device );
        break;
      }
    }
  }

  private boolean disconnectDevice()
  {
    // Log.v("DistoX", "disconnectDevice state " + mApp.getConnectionStateStr() );
    // Let the user choose which device to disconnect from
    // if ( mName == null ) return false; // <-- mName != null is guaranteed
    for ( BluetoothDevice device : mDevices ) {
      if ( mName.equals( device.getName() ) ) {
        // Log.v("DistoX", "disconnectDevice " + mName );
        mApp.disconnectRemoteTopoDroid( device );
        return true;
      }
    }
    return false;
  }

  private void syncDevice()
  {
    for ( BluetoothDevice device : mDevices ) {
      if ( mName.equals( device.getName() ) ) {
        // Log.v("DistoX", "syncDevice " + mName );
        mApp.syncRemoteTopoDroid( device );
      }
    }
  }

  // private void startDevice()
  // {
  //   Log.v("DistoX", "startDevice state " + mApp.getConnectionStateStr() );
  //   if ( mApp.getConnectState() != SyncService.STATE_NONE ) return;
  //   if ( mApp.getConnectionType() == SyncService.STATE_NONE ) {
  //     mApp.startRemoteTopoDroid( );
  //   }
  // }

  // private void stopDevice()
  // {
  //   Log.v("DistoX", "stopDevice state " + mApp.getConnectionStateStr() );
  //   if ( mApp.getConnectState() != SyncService.STATE_CONNECTED 
  //     && mApp.getConnectState() != SyncService.STATE_LISTEN ) return;
  //   if ( mApp.getConnectionType() == SyncService.STATE_LISTEN ) {
  //     mApp.stopRemoteTopoDroid( );
  //   }
  // }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button) v;
    // if ( b == mBtnStart ) {
    //   startDevice();
    //   dismiss();
    // } else if ( b == mBtnStop ) {
    //   stopDevice();
    //   dismiss();
    // } else
    if ( b == mBtnConnect ) {
      if ( mApp.getConnectState() != SyncService.STATE_NONE ) {
        Toast.makeText( mContext, R.string.connected_already, Toast.LENGTH_SHORT).show();
      } else if ( mName != null ) {
        connectDevice();
        dismiss();
      } else {
        Toast.makeText( mContext, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } 
    } else if ( b == mBtnDisconnect ) {
      if ( mApp.getConnectState() != SyncService.STATE_CONNECTED ) {
        Toast.makeText( mContext, R.string.connected_none, Toast.LENGTH_SHORT).show();
      } else if ( mName != null ) {
        if ( disconnectDevice() ) {
          dismiss();
        }
      } else {
        Toast.makeText( mContext, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      }
    } else if ( b == mBtnSync ) {
      if ( mApp.getConnectState() != SyncService.STATE_CONNECTED ) {
        Toast.makeText( mContext, R.string.connected_none, Toast.LENGTH_SHORT).show();
      } else if ( mName != null ) {
        syncDevice();
        dismiss();
      } else {
        Toast.makeText( mContext, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      }
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    //   dismiss();
    } 
  }

}

