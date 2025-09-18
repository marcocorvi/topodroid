/* @file DeviceSearch.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid device search activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDTag;
import com.topodroid.dev.Device;
import com.topodroid.dev.DeviceUtil;

import java.util.Set;

import android.app.Activity;
import android.os.Bundle;

import android.content.Intent;

// import android.util.Log;

import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.Context;


public class DeviceSearch extends Activity
                          implements OnItemClickListener
                          , OnClickListener
{ 
  private TopoDroidApp mApp;

  static final int DEVICE_PAIR = 0x1;
  static final int DEVICE_SCAN = 0x2;

  private static final int STATUS_WAIT = 0;
  private static final int STATUS_SCAN = 1;
  
  private ArrayAdapter<String> mArrayAdapter;
  private Button mBtnSearch;
  private Button mBtnClear;
  private TextView mTvSearch;
  private TextView mTvInfo;
  private ListView mList;

  private int mStatus = STATUS_WAIT;

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    TDandroid.setScreenOrientation( this );

    setContentView(R.layout.device_search);
    mApp = (TopoDroidApp) getApplication();

    mArrayAdapter = new ArrayAdapter<>( this, R.layout.message );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    mBtnSearch = (Button)findViewById( R.id.button_search );
    mBtnClear  = (Button)findViewById( R.id.button_clear );
    mTvSearch  = (TextView)findViewById( R.id.text_search );
    mTvInfo    = (TextView)findViewById( R.id.text_info );

    mBtnSearch.setOnClickListener( this );
    mBtnClear.setOnClickListener( this );

    // setTitleColor( TDColor.TITLE_NORMAL );

    /*
    int command; // = 0;
    try {
      command = getIntent().getExtras().getInt( TDTag.TOPODROID_DEVICE_ACTION );
    } catch ( NullPointerException e ) {
      TDLog.e("Missing TOPODROID_DEVICE_ACTION");
      return;
    }

    // TDLog.Log( TDLog.LOG_BT, "command " + command );
    switch ( command )
    {
      case DEVICE_PAIR:
        showPairedDevices();
        break;
      case DEVICE_SCAN:
        scanBTDevices();
        break;
      default:  // 0x0 or unknown
         // TDLog.Log(TDLog.LOG_BT, "Unknown intent command! ("+command+")");
    }
    */

    setTitle( R.string.title_bt_search );

    if ( ! TDandroid.checkLocation( this ) ) {
      mTvInfo.setText( R.string.location_enabled );
    }
  }

  @Override
  public void onClick( View v ) 
  {
    TDLog.v("on click: status " + mStatus );
    if ( v.getId() == R.id.button_search ) {
      if ( mStatus == STATUS_WAIT ) {
        scanBTDevices();
      } else if ( mStatus == STATUS_SCAN ) {
        resetReceiver();
      }
    } else if ( v.getId() == R.id.button_clear ) {
      if ( mStatus == STATUS_WAIT ) {
        mArrayAdapter.clear();
      }
    }
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    if ( ! ( view instanceof TextView ) ) {
      TDLog.e("device list view instance of " + view.toString() );
      return;
    }
    CharSequence item = ((TextView) view).getText();
    String value = item.toString();
    // if ( value.startsWith( "DistoX", 0 ) || value.startsWith( "A3", 0 ) || value.startsWith( "X310", 0 ) ) 
    {
      StringBuffer buf = new StringBuffer( item );
      int k = buf.lastIndexOf(" ");
      String address = buf.substring(k+1);
      // TDToast.make( address );
      // TDLog.v( "device list item click Address " + address );
      pairDevice( address );
      Intent intent = new Intent();
      intent.putExtra( TDTag.TOPODROID_DEVICE_ACTION, address );
      setResult( RESULT_OK, intent );
    // } else {
    //   setResult( RESULT_CANCELED );
    }
    finish();
  }

  
  // private void showPairedDevices()
  // {
  //   Set<BluetoothDevice> device_set = DeviceUtil.getBondedDevices();
  //   if ( device_set != null ) {
  //     if ( device_set.isEmpty() ) {
  //       TDToast.makeBad(R.string.no_paired_device );
  //     } else {
  //       setTitle( R.string.title_device );
  //       mArrayAdapter.clear();
  //       for ( BluetoothDevice device : device_set ) {
  //         // Log.v("DistoX", "paired device <" + device.getName() + "> " + device.getAddress() );
  //         mArrayAdapter.add( "DistoX " + device.getName() + " " + device.getAddress() );
  //      }
  //     }
  //     // TDLog.Log( TDLog.LOG_BT, "showPairedDevices n. " + mArrayAdapter.getCount() );
  //   } else {
  //     TDToast.makeBad(R.string.not_available );
  //   }
  // }

  private BroadcastReceiver mBTReceiver = null;

  /** start a BT scan
   */
  private void scanBTDevices()
  {
    // TDLog.v( "scan BT devices" );
    mArrayAdapter.clear();
    // resetReceiver(); // FIXME should not be necessary
    mStatus = STATUS_SCAN;
    mBtnSearch.setText( R.string.stop_scan );
    mBTReceiver = new BroadcastReceiver() 
    {
      @Override
      public void onReceive( Context ctx, Intent data )
      {
        String action = data.getAction();
        TDLog.v( "BT on receive: action " + action );
        if ( DeviceUtil.ACTION_DISCOVERY_STARTED.equals( action ) ) {
          // TDLog.v( "onReceive BT DISCOVERY STARTED" );
          mTvInfo.setText( ctx.getResources().getString( R.string.scan_started ) );
          // setTitle(  R.string.title_discover );
        } else if ( DeviceUtil.ACTION_DISCOVERY_FINISHED.equals( action ) ) {
          // TDLog.v( "onReceive BT DISCOVERY FINISHED, found " + mArrayAdapter.getCount() );
          // setTitle( R.string.title_device );
          // mTvInfo.setText( ctx.getResources().getString( R.string.scan_finished ) );
          resetReceiver();
          if ( mArrayAdapter.getCount() < 1 ) { 
            // TDToast.makeBad( R.string.no_device_found );
            mTvInfo.setText( ctx.getResources().getString( R.string.no_device_found ) );
            // finish(); // no need to keep list of scanned distox open
          }
        } else if ( DeviceUtil.ACTION_FOUND.equals( action ) ) {
          BluetoothDevice device = data.getParcelableExtra( DeviceUtil.EXTRA_DEVICE );
          String device_addr = device.getAddress();
          String model = device.getName();
          if ( device.getBondState() != DeviceUtil.BOND_BONDED ) {
            TDLog.v( "onReceive BT device <" + model + "> not bonded, address " + device_addr);
            if ( model != null && Device.isKnownDevice( model ) ) { // DistoX and DistoX2
              String name = Device.btnameToName( model );
              // TDLog.v( "BT scan receiver add <" + name + "> " + device_addr ); 
              // TopoDroidApp.mDData.insertDevice( device_addr, model, name );
              mArrayAdapter.add( Device.btnameToModel(model) + " " + name + " " + device_addr );
            }
          } else {
            TDLog.v( "onReceive BT device <" + model + "> bonded, address " + device_addr);
          }
        } else if ( DeviceUtil.ACTION_ACL_CONNECTED.equals( action ) ) {
          TDLog.v( "ACL_CONNECTED");
        } else if ( DeviceUtil.ACTION_ACL_DISCONNECT_REQUESTED.equals( action ) ) {
          TDLog.v( "ACL_DISCONNECT_REQUESTED");
        } else if ( DeviceUtil.ACTION_ACL_DISCONNECTED.equals( action ) ) {
          // Bundle extra = data.getExtras();
          // String device = extra.getString( DeviceUtil.EXTRA_DEVICE ).toString();
          // Log.v("DistoX", "DeviceSearch ACL_DISCONNECTED from " + device );
          TDLog.v( "ACL_DISCONNECTED");
        }
      }
    };
    IntentFilter foundFilter = new IntentFilter( DeviceUtil.ACTION_FOUND );
    IntentFilter startFilter = new IntentFilter( DeviceUtil.ACTION_DISCOVERY_STARTED );
    IntentFilter finishFilter = new IntentFilter( DeviceUtil.ACTION_DISCOVERY_FINISHED );
    // IntentFilter connectedFilter = new IntentFilter( DeviceUtil.ACTION_ACL_CONNECTED );
    // IntentFilter disconnectRequestFilter = new IntentFilter( DeviceUtil.ACTION_ACL_DISCONNECT_REQUESTED );
    // IntentFilter disconnectedFilter = new IntentFilter( DeviceUtil.ACTION_ACL_DISCONNECTED );

    // IntentFilter uuidFilter  = new IntentFilter( myUUIDaction );
    // IntentFilter bondFilter  = new IntentFilter( DeviceUtil.ACTION_BOND_STATE_CHANGED );

    registerReceiver( mBTReceiver, foundFilter );
    registerReceiver( mBTReceiver, startFilter );
    registerReceiver( mBTReceiver, finishFilter );
    // registerReceiver( mBTReceiver, connectedFilter );
    // registerReceiver( mBTReceiver, disconnectRequestFilter );
    // registerReceiver( mBTReceiver, disconnectedFilter );
    // registerReceiver( mBTReceiver, uuidFilter );
    // registerReceiver( mBTReceiver, bondFilter );

    // mArrayAdapter.clear();
    if ( ! DeviceUtil.startDiscovery() ) {
      resetReceiver();
      mTvSearch.setText( R.string.bt_busy );
    } else {
      mTvSearch.setText( R.string.scanning );
    }
  }

  @Override
  public void onStop()
  {
    super.onStop();
    resetReceiver();
  }

  /** unregister the BT receiver and discart it
   */
  private void resetReceiver()
  {
    // TDLog.v(   "reset BT receiver");
    mStatus = STATUS_WAIT;
    mBtnSearch.setText( R.string.start_scan );
    mTvSearch.setText( R.string.idle );
    mTvInfo.setText( R.string.scan_finished );
    if ( mBTReceiver != null ) {
      unregisterReceiver( mBTReceiver );
      mBTReceiver = null;
    }
  }

  /** pair the android and the current device
   * @param address   device BT address
   */
  private void pairDevice( String address )
  {
    BluetoothDevice device = DeviceUtil.getRemoteDevice( address );
    if ( device == null ) {
      // TDLog.v( "no BT device for address " + address );
      return;
    }
    // TDLog.v( "onReceive BT DEVICES FOUND, name " + device.getName() );
    if ( device.getBondState() == DeviceUtil.BOND_BONDED ) {
      TDLog.v( "BT device " + address + " already bonded" );
      return;
    }
    int res = DeviceUtil.pairDevice( device );
    TDLog.v( "BT device " + address + " try to bond: result " + res );
    switch ( res ) {
      case -1: // failure
        // TDToast.makeBad( R.string.pairing_failed ); // TODO
        break;
      case 2: // already paired
        // TDToast.make( R.string.device_paired ); 
        break;
      default: // 0: null device
               // 1: paired ok
    }
  }

}

