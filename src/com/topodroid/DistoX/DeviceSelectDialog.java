/* @file DeviceActivity.java
 *
 * @author marco corvi
 * @date nov 2016
 *
 * @brief TopoDroid DistoX device selection dialog (for multi-DistoX)
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
import android.content.DialogInterface;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import android.view.View;
import android.view.ViewGroup.LayoutParams;

import android.util.Log;

import android.bluetooth.BluetoothDevice;

public class DeviceSelectDialog extends MyDialog
                                implements OnItemClickListener
{
  private Context mContext;
  private TopoDroidApp mApp;
  private DataDownloader mDownloader;
  private ILister mLister;

  private ListView mList;

  // ---------------------------------------------------------------
  DeviceSelectDialog( Context context, TopoDroidApp app, DataDownloader downloader, ILister lister )
  {
    super( context, R.string.DeviceSelectDialog );
    mContext = context;
    mApp = app;
    mDownloader = downloader;
    mLister = lister;
    // Log.v("DistoX", "device select dialog created");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
 
    // Log.v("DistoX", "device select dialog init layout");
    initLayout( R.layout.device_select_dialog, R.string.title_device_select );

    mList = (ListView) findViewById(R.id.dev_list);
    mList.setOnItemClickListener( this );
    // mList.setLongClickable( true );
    mList.setDividerHeight( 2 );
    updateList();
  }

  private void updateList( )
  {
    ListItemAdapter array_adapter = new ListItemAdapter( mContext, R.layout.message );
    // mArrayAdapter.clear();
    if ( TDSetting.mLevelOverExperimental ) { // FIXME VirtualDistoX
      array_adapter.add( "X000" );
    }
    ArrayList<Device> devices = mApp.mDData.getDevices();
    for ( Device device : devices ) {
      // String addr  = device.mAddress;
      // String model = device.mName;
      // String name  = device.mName;
      // String nick  = device.mNickname;
      array_adapter.add( device.toString() );
    }
    mList.setAdapter( array_adapter );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    dismiss();
    CharSequence item = ((TextView) view).getText();
    // TDLog.Log( TDLog.LOG_INPUT, "DeviceActivity onItemClick() " + item.toString() );
    StringBuffer buf = new StringBuffer( item );
    int k = buf.lastIndexOf(" ");
    String[] vals = item.toString().split(" ", 3 );
    String address = ( vals[0].equals("X000") )? Device.ZERO_ADDRESS : vals[2];
    // String address = vals[2]; // FIXME VirtualDistoX
    mApp.setDevice( address );
    mLister.setTheTitle();
    mDownloader.doDataDownload();
  }

}

