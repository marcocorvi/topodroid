/* @file DeviceSelectDialog.java
 *
 * @author marco corvi
 * @date nov 2016
 *
 * @brief TopoDroid DistoX device selection dialog (for multi-DistoX)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;

// import android.util.Log;

// import java.util.Set;
// import java.util.List;
import java.util.ArrayList;

// import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;
// import android.content.DialogInterface;

import android.widget.TextView;
// import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.view.View;
import android.view.View.OnClickListener;

class DeviceSelectDialog extends MyDialog
                         implements OnItemClickListener
                         , OnClickListener
{
  private final Context mContext;
  private final TopoDroidApp mApp;
  private final DataDownloader mDownloader;
  private final ILister mLister;

  private ListView mList;
  // private Button  mBtnCancel;

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

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );
    ( (Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );

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
    // if ( TDLevel.overTester ) { // FIXME VirtualDistoX
    //   array_adapter.add( "X000" );
    // }
    ArrayList<Device> devices = TopoDroidApp.mDData.getDevices();
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

    // FIXME VirtualDistoX
    // String address = ( vals[0].equals("X000") )? Device.ZERO_ADDRESS : vals[2];
    String address = vals[2];

    mApp.setDevice( address, null ); // FIXME BLE only BT devices
    mLister.setTheTitle();
    mDownloader.toggleDownload();
    mLister.setConnectionStatus( mDownloader.getStatus() );
    mDownloader.doDataDownload( DataType.ALL );
  }

  @Override
  public void onClick(View v) 
  {
    // Button b = (Button) v;
    // if ( b == mBtnCancel ) {
    //   /* nothing */
    // }
    dismiss();
  }

}

