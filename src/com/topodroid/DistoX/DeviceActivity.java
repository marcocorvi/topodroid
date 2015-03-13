/* @file DeviceActivity.java
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

import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.IOException;


import android.app.Activity;
import android.os.Bundle;
import android.os.AsyncTask;

import android.content.Intent;
import android.content.res.Resources;
import android.content.DialogInterface;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.RadioButton;
import android.view.View;
// import android.widget.RadioGroup;
// import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import android.view.Menu;
import android.view.MenuItem;

import android.widget.Toast;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.util.Log;

import android.bluetooth.BluetoothDevice;

public class DeviceActivity extends Activity
                            implements View.OnClickListener
                            , OnItemClickListener
                            , IEnableButtons
                            // , OnItemLongClickListener
                            // , RadioGroup.OnCheckedChangeListener
{
  private static final int REQUEST_DEVICE    = 1;

  private TopoDroidApp mApp;

  private TextView mTvAddress;

  // private static int icons00no[];
  // private static int iconsno[] = {
  //                       0,
  //                       R.drawable.ic_toggle_no,
  //                       0,
  //                       R.drawable.ic_read_no,
  //                       0,
  //                       // R.drawable.ic_remote_no
  //                    };
  // private static int ixonsno[] = {
  //                       0,
  //                       R.drawable.ix_toggle_no,
  //                       0,
  //                       R.drawable.ix_read_no,
  //                       0,
  //                       // R.drawable.ix_remote_no
  //                     };
  private static int izonsno[] = {
                        0,
                        R.drawable.iz_toggle_no,
                        0,
                        R.drawable.iz_read_no,
                        0,
                        // R.drawable.iz_remote_no
                     };

  // private static int icons00[];
  // private static int icons[] = {
  //                       R.drawable.ic_bt,
  //                       R.drawable.ic_toggle,
  //                       R.drawable.ic_sdcard,
  //                       R.drawable.ic_read,
  //                       R.drawable.ic_info
  //                       // R.drawable.ic_remote,
  //                    };
  // private static int ixons[] = {
  //                       R.drawable.ix_bt,
  //                       R.drawable.ix_toggle,
  //                       R.drawable.ix_sdcard,
  //                       R.drawable.ix_read,
  //                       R.drawable.ix_info
  //                       // R.drawable.ix_remote,
  //                     };
  private static int izons[] = {
                        R.drawable.iz_bt,
                        R.drawable.iz_toggle,
                        R.drawable.iz_sdcard,
                        R.drawable.iz_read,
                        R.drawable.iz_info
                        // R.drawable.iz_remote,
                     };

  private static int indexButtonDownload = 1;
  private static int indexButtonRead     = 2;
  // private static int indexButtonRemote   = 5;

  BitmapDrawable mBMtoggle;
  BitmapDrawable mBMtoggle_no;
  BitmapDrawable mBMread;
  BitmapDrawable mBMread_no;

  private static int menus[] = {
                        R.string.menu_scan,
                        R.string.menu_detach,
                        R.string.menu_calib,
                        R.string.menu_firmware,
                        R.string.menu_options,
                        R.string.menu_help
                     };

  private static int help_icons[] = {
                        R.string.help_bluetooth,
                        R.string.help_toggle,
                        R.string.help_sdcard,
                        R.string.help_read,
                        R.string.help_info_device
                        // R.string.help_remote
                     };
  private static int help_menus[] = {
                        R.string.help_scan,
                        R.string.help_detach,
                        R.string.title_calib,
                        R.string.help_firmware,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  // private ArrayAdapter<String> mArrayAdapter;
  private ListItemAdapter mArrayAdapter;
  private ListView mList;

  // private String mAddress;
  private Device mDevice;

  private MenuItem mMIscan;
  private MenuItem mMIdetach;
  private MenuItem mMIcalibs = null;
  private MenuItem mMIfirmware = null;
  private MenuItem mMIoptions;
  private MenuItem mMIhelp;

// -------------------------------------------------------------------
  private void setState()
  {
    boolean cntd = mApp.isCommConnected();
    if ( mDevice != null ) { // mAddress.length() > 0 ) {
      mTvAddress.setTextColor( 0xffffffff );
      mTvAddress.setText( String.format( getResources().getString( R.string.using ), mDevice.mName, mDevice.mAddress ) );
      if ( mMIfirmware != null ) {
        mMIfirmware.setEnabled( true );
      }
      // setButtonRemote();
    } else {
      mTvAddress.setTextColor( 0xffff0000 );
      mTvAddress.setText( R.string.no_device_address );
      if ( mMIfirmware != null ) {
        mMIfirmware.setEnabled( false );
      }
    }

    updateList();
  }  

  // ---------------------------------------------------------------
  // private Button mButtonHelp;
  private Button[] mButton1;
  private int mNrButton1 = 5; // 6 if ButtonRemote
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  ListView   mMenu;
  Button     mImage;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;


  // private void setButtonRemote( )
  // {
  //   if ( TopoDroidSetting.mLevelOverNormal ) {
  //     if ( mDevice != null && mDevice.mType == Device.DISTO_X310 ) {
  //       mButton1[ indexButtonRemote ].setEnabled( true );
  //       mButton1[ indexButtonRemote ].setBackgroundResource( icons00[ indexButtonRemote ] );
  //     } else {
  //       mButton1[ indexButtonRemote ].setEnabled( false );
  //       mButton1[ indexButtonRemote ].setBackgroundResource( icons00no[ indexButtonRemote ] );
  //     }
  //   }
  // }

  void setDeviceModel( Device device, int model )
  {
    mApp.setDeviceModel( device, model );
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    mApp = (TopoDroidApp) getApplication();

    mDevice  = mApp.mDevice;
    // mAddress = mDevice.mAddress;

    // mAddress = getIntent().getExtras().getString(   TopoDroidApp.TOPODROID_DEVICE_ADDR );

    setContentView(R.layout.device_activity);
    mTvAddress = (TextView) findViewById( R.id.device_address );

    mListView = (HorizontalListView) findViewById(R.id.listview);
    int size = mApp.setListViewHeight( mListView );
    // icons00   = ( TopoDroidSetting.mSizeButtons == 2 )? ixons : icons;
    // icons00no = ( TopoDroidSetting.mSizeButtons == 2 )? ixonsno : iconsno;

    mNrButton1 = TopoDroidSetting.mLevelOverNormal ? 5 : 2;
    mButton1 = new Button[ mNrButton1 ];
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      // mButton1[k].setBackgroundResource( icons00[k] );
      BitmapDrawable bm2 = mApp.setButtonBackground( mButton1[k], size, izons[k] );
      if ( k == 1 ) {
        mBMtoggle = bm2;
      } else if ( k == 3 ) {
        mBMread = bm2;
      }
    }
    mBMtoggle_no = mApp.setButtonBackground( null, size, izonsno[1] );
    mBMread_no = mApp.setButtonBackground( null, size, izonsno[3] );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    // mArrayAdapter = new ArrayAdapter<String>( this, R.layout.message );
    mArrayAdapter = new ListItemAdapter( this, R.layout.message );
    mList = (ListView) findViewById(R.id.dev_list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    // mList.setLongClickable( true );
    // mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );

    setState();

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    // mImage.setBackgroundResource( ( TopoDroidSetting.mSizeButtons == 2 )? R.drawable.ix_menu : R.drawable.ic_menu );
    mApp.setButtonBackground( mImage, size, R.drawable.iz_menu );
    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter();
    closeMenu();
    mMenu.setOnItemClickListener( this );
  }

  private void updateList( )
  {
    // TopoDroidLog.Log(TopoDroidLog.LOG_MAIN, "updateList" );
    // mList.setAdapter( mArrayAdapter );
    mArrayAdapter.clear();
    if ( mApp.mBTAdapter != null ) {
      Set<BluetoothDevice> device_set = mApp.mBTAdapter.getBondedDevices(); // get paired devices
      if ( device_set.isEmpty() ) {
        // Toast.makeText(this, R.string.no_paired_device, Toast.LENGTH_SHORT).show();
      } else {
        setTitle( R.string.title_device );
        for ( BluetoothDevice device : device_set ) {
          String addr  = device.getAddress();
          Device dev = mApp.mData.getDevice( addr );
          if ( dev == null ) {
            String model = device.getName();
            if ( model.startsWith( "DistoX", 0 ) ) {
              String name  = Device.modelToName( model );
              mApp.mData.insertDevice( addr, model, name );
              dev = mApp.mData.getDevice( addr );
            }
          }
          if ( dev != null ) {
            // TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "device " + name );
            if ( dev.mModel.startsWith("DistoX-") ) {      // DistoX2 X310
              mArrayAdapter.add( " X310 " + dev.mName + " " + addr );
            } else if ( dev.mModel.equals("DistoX") ) {    // DistoX A3
              mArrayAdapter.add( " A3 " + dev.mName + " " + addr );
            } else {
              // do not add
            }
          }
        }
      }
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) { // SCAN
        Intent scanIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceList.class );
        scanIntent.putExtra( TopoDroidApp.TOPODROID_DEVICE_ACTION, DeviceList.DEVICE_SCAN );
        startActivityForResult( scanIntent, REQUEST_DEVICE );
        Toast.makeText(this, R.string.wait_scan, Toast.LENGTH_LONG).show();
      } else if ( TopoDroidSetting.mLevelOverBasic && p++ == pos ) { // DETACH
        detachDevice();
      } else if ( mApp.VERSION30 && p++ == pos ) { // CALIB
        if ( mApp.mDevice == null ) {
          Toast.makeText(this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
        } else {
          (new CalibListDialog( this, this, mApp )).show();
        }
      } else if ( TopoDroidSetting.mBootloader && p++ == pos ) { // FIRMWARE
        if ( TopoDroidSetting.mCommType != 0 ) {
          Toast.makeText( this, "Connection mode must be \"on-demand\"", Toast.LENGTH_LONG).show();
        } else {
          mApp.resetComm();
          (new FirmwareDialog( this, this, mApp )).show();
        }
      } else if ( p++ == pos ) { // OPTIONS
        Intent intent = new Intent( this, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_DEVICE );
        startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 6 ) ).show();
      }
      return;
    }

    if ( onMenu ) {
      closeMenu();
      return;
    }


    CharSequence item = ((TextView) view).getText();
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DeviceActivity onItemClick() " + item.toString() );
    // String value = item.toString();
    // if ( value.startsWith( "DistoX", 0 ) ) 
    {
      StringBuffer buf = new StringBuffer( item );
      int k = buf.lastIndexOf(" ");
      String address = buf.substring(k+1);
      if ( mDevice == null || ! address.equals( mDevice.mAddress ) ) {
        mApp.setDevice( address );
        mDevice = mApp.mDevice;
        // mAddress = address;
        mApp.disconnectRemoteDevice( true );
        setState();
      }
    }
  }

  void detachDevice()
  {
    if ( mDevice != null ) {
      mApp.setDevice( null );
      mDevice = mApp.mDevice;
      // mAddress = address;
      mApp.disconnectRemoteDevice( true );
      setState();
    }
  }

  @Override
  public void enableButtons( boolean enable ) 
  {
    mButton1[1].setEnabled( enable );
    if ( TopoDroidSetting.mLevelOverNormal ) {
      for ( int k=2; k<mNrButton1; ++k ) {
        mButton1[k].setEnabled( enable );
      }
    }
    if ( enable ) {
      setTitleColor( TopoDroidConst.COLOR_NORMAL );
      // mButton1[1].setBackgroundResource( icons00[1] );
      mButton1[3].setBackgroundDrawable( mBMtoggle );
      if ( TopoDroidSetting.mLevelOverNormal ) {
        // mButton1[3].setBackgroundResource( icons00[3] );
        mButton1[3].setBackgroundDrawable( mBMread);
        // mButton1[indexButtonRemote].setBackgroundResource( icons00[5] );
      }
    } else {
      setTitleColor( TopoDroidConst.COLOR_CONNECTED );
      // mButton1[1].setBackgroundResource( icons00no[1] );
      mButton1[3].setBackgroundDrawable( mBMtoggle_no );
      if ( TopoDroidSetting.mLevelOverNormal ) {
        // mButton1[3].setBackgroundResource( icons00no[3] );
        mButton1[3].setBackgroundDrawable( mBMread_no );
        // mButton1[indexButtonRemote].setBackgroundResource( icons00no[5] );
      }
    }
  }

  @Override
  public void onClick(View v) 
  {
    if ( onMenu ) {
      closeMenu();
      return;
    }

    Button b = (Button) v;

    if ( b == mImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      return;
    }

    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DeviceActivity onClick() button " + b.getText().toString() ); 

    // FIXME COMMENTED
    // DistoXComm comm = mApp.mComm;
    // if ( comm == null ) {
    //   Toast.makeText( this, R.string.connect_failed, Toast.LENGTH_SHORT).show();
    //   return;
    // }

    int k = 0;
    if ( k < mNrButton1 && b == mButton1[k++] ) { // 4: RESET COMM STATE [This is fast]
      mApp.resetComm();
      setState();
      Toast.makeText(this, R.string.bt_reset, Toast.LENGTH_SHORT).show();
    } else if ( k < mNrButton1 &&  b == mButton1[k++] ) {          // DISTOX CALIBRATION MODE TOGGLE
      if ( mDevice == null ) { // mAddress.length() < 1 ) {
        Toast.makeText( this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else {
        enableButtons( false );
        new CalibToggleTask( this, this, mApp ).execute();
      }
    } else if ( k < mNrButton1 &&  b == mButton1[k++] ) { // DISTOX MEMORY
      if ( mDevice == null ) { // mAddress.length() < 1 ) {
        Toast.makeText( this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else {
        if ( mDevice.mType == Device.DISTO_A3 ) {
          new DeviceA3MemoryDialog( this, this ).show();
        } else if ( mDevice.mType == Device.DISTO_X310 ) {
          new DeviceX310MemoryDialog( this, this ).show();
        } else {
          Toast.makeText( this, "Unknown DistoX type " + mDevice.mType, Toast.LENGTH_SHORT).show();
        }
      }
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {   // 2: DISTOX CALIBRATION COEFFS READ
      if ( mDevice == null ) { // mAddress.length() < 1 ) {
        Toast.makeText( this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else {
        enableButtons( false );
        new CalibReadTask( this, this, mApp ).execute();
      }

    } else if ( k < mNrButton1 && b == mButton1[k++] ) {    // 3: DISTOX INFO
      if ( mDevice == null ) {
        Toast.makeText( this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else {
        if ( mDevice.mType == Device.DISTO_A3 ) {
          new DeviceA3InfoDialog( this, this, mDevice ).show();
        } else if ( mDevice.mType == Device.DISTO_X310 ) {
          new DeviceX310InfoDialog( this, this, mDevice ).show();
        } else {
          Toast.makeText( this, "Unknown DistoX type " + mDevice.mType, Toast.LENGTH_SHORT).show();
        }
      }

    // } else if ( k < mNrButton1 && b == mButton1[k++] ) { // 5: REMOTE
    //   if ( mDevice == null ) {
    //     Toast.makeText( this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
    //   } else {
    //     if ( mDevice.mType == Device.DISTO_X310 ) {
    //       ( new DeviceRemote( this, this, mApp )).show();
    //     } else {
    //       /* nothing */
    //     }
    //   }

    }
    setState();
  }

  @Override
  public void onStart()
  {
    super.onStart();
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    mApp.resumeComm();
  }

  // -----------------------------------------------------------------------------

  boolean readDeviceHeadTail( int[] head_tail )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_DEVICE, "onClick mBtnHeadTail. Is connected " + mApp.isConnected() );
    String ht = mApp.readHeadTail( mDevice.mAddress, head_tail );
    if ( ht == null ) {
      Toast.makeText( this, R.string.head_tail_failed, Toast.LENGTH_SHORT).show();
      return false;
    }
    // Log.v( TopoDroidApp.TAG, "Head " + head_tail[0] + " tail " + head_tail[1] );
    // Toast.makeText( this, getString(R.string.head_tail) + ht, Toast.LENGTH_SHORT).show();
    return true;
  }

  // reset data from stored-tail (inclusive) to current-tail (exclusive)
  private void doResetA3DeviceHeadTail( int[] head_tail )
  {
    int from = head_tail[0];
    int to   = head_tail[1];
    // Log.v(TopoDroidApp.TAG, "do reset from " + from + " to " + to );
    int n = mApp.swapHotBit( mDevice.mAddress, from, to );
  }

  void storeDeviceHeadTail( int[] head_tail )
  {
    // Log.v(TopoDroidApp.TAG, "store HeadTail " + mDevice.mAddress + " : " + head_tail[0] + " " + head_tail[1] );
    if ( ! mApp.mData.updateDeviceHeadTail( mDevice.mAddress, head_tail ) ) {
      Toast.makeText( this, getString(R.string.head_tail_store_failed), Toast.LENGTH_SHORT).show();
    }
  }

  void retrieveDeviceHeadTail( int[] head_tail )
  {
    // Log.v(TopoDroidApp.TAG, "store HeadTail " + mDevice.mAddress + " : " + head_tail[0] + " " + head_tail[1] );
    mApp.mData.getDeviceHeadTail( mDevice.mAddress, head_tail );
  }

  private void writeMemoryDumpToFile( String dumpfile, ArrayList< MemoryOctet > memory )
  {
    if ( dumpfile == null ) return;
    dumpfile.trim();
    if ( dumpfile.length() == 0 ) return;
    try { 
      TopoDroidApp.checkPath( dumpfile );
      FileWriter fw = new FileWriter( TopoDroidPath.getDumpFile( dumpfile ) );
      PrintWriter pw = new PrintWriter( fw );
      for ( MemoryOctet m : memory ) {
        m.printHexString( pw );
        pw.format(" " + m.toString() + "\n");
      }
      fw.flush();
      fw.close();
    } catch ( IOException e ) {
    }
  }

  void readX310Memory( final int[] head_tail, String dumpfile )
  {
    ArrayList< MemoryOctet > memory = new ArrayList< MemoryOctet >();
    int n = mApp.readX310Memory( mDevice.mAddress, head_tail[0], head_tail[1], memory );
    if ( n <= 0 ) return;
    writeMemoryDumpToFile( dumpfile, memory );
    (new MemoryListDialog(this, this, memory)).show();
  }

  void readA3Memory( final int[] head_tail, String dumpfile )
  {
    if ( head_tail[0] < 0 || head_tail[0] >= 0x8000 || head_tail[1] < 0 || head_tail[1] >= 0x8000 ) {
      Toast.makeText(this, R.string.device_illegal_addr, Toast.LENGTH_SHORT).show();
      return;
    }
    ArrayList< MemoryOctet > memory = new ArrayList< MemoryOctet >();
    int from = head_tail[0];
    int to   = head_tail[1];
    // Log.v(TopoDroidApp.TAG, "read-memory from " + from + " to " + to );
    int n = mApp.readA3Memory( mDevice.mAddress, from, to, memory );
    if ( n == 0 ) {
      Toast.makeText(this, "no data", Toast.LENGTH_SHORT).show();
      return;
    } 
    // Toast.makeText(this, "read " + n + " data", Toast.LENGTH_SHORT).show();
    writeMemoryDumpToFile( dumpfile, memory );
    (new MemoryListDialog(this, this, memory)).show();

  }

  // X310 data memory is read-only
  // void resetX310DeviceHeadTail( final int[] head_tail )
  // {
  //   int n = mApp.resetX310Memory( mDevice.mAddress, head_tail[0], head_tail[1] );
  //   Toast.makeText(this, "X310 memory reset " + n + " data", Toast.LENGTH_SHORT ).show();
  // }

  // reset device from stored-tail to given tail
  void resetA3DeviceHeadTail( final int[] head_tail )
  {
    // Log.v(TopoDroidApp.TAG, "reset device from " + head_tail[0] + " to " + head_tail[1] );
    if ( head_tail[0] < 0 || head_tail[0] >= 0x8000 || head_tail[1] < 0 || head_tail[1] >= 0x8000 ) {
      Toast.makeText(this, R.string.device_illegal_addr, Toast.LENGTH_SHORT).show();
      return;
    } 
    // TODO ask confirm
    new TopoDroidAlertDialog( this, getResources(),
                              getResources().getString( R.string.device_reset ) + " ?",
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doResetA3DeviceHeadTail( head_tail );
        }
      }
    );
  }


  // -----------------------------------------------------------------------------

  public void onActivityResult( int request, int result, Intent intent ) 
  {
    // Log.v("DistoX", "onActivityResult req. " + request + " res. " + result );
    Bundle extras = (intent != null)? intent.getExtras() : null;
    switch ( request ) {
      case REQUEST_DEVICE:
        if ( result == RESULT_OK ) {
          String address = extras.getString( TopoDroidApp.TOPODROID_DEVICE_ACTION );
          TopoDroidLog.Log(TopoDroidLog.LOG_DISTOX, "OK " + address );
          if ( address == null ) {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "onActivityResult REQUEST_DEVICE: null address");
          } else if ( mDevice == null || ! address.equals( mDevice.mAddress ) ) {
            mApp.disconnectRemoteDevice( true );
            Toast.makeText(this, R.string.device_pairing, Toast.LENGTH_LONG).show();
            mApp.setDevice( address );
            // try to get the system ask for the PIN
            // mApp.connectRemoteDevice( address, null ); // null ILister
            // mApp.disconnectRemoteDevice( true );
            mDevice = mApp.mDevice;
            // mAddress = address;
            setState();
          }
        } else if ( result == RESULT_CANCELED ) {
          TopoDroidLog.Log( TopoDroidLog.LOG_DISTOX, "CANCELED");
          // finish(); // back to survey
        }
        updateList();
        break;
      // case REQUEST_ENABLE_BT:
      //   if ( result == Activity.RESULT_OK ) {
      //     // nothing to do: scanBTDevices(); is called by menu CONNECT
      //   } else {
      //     Toast.makeText(this, R.string.not_enabled, Toast.LENGTH_SHORT).show();
      //     finish();
      //   }
      //   break;
    }
  }

  // ---------------------------------------------------------
  /* MENU

  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    mMIscan    = menu.add( R.string.menu_scan );
    mMIdetach  = menu.add( R.string.menu_detach );
    if ( mApp.VERSION30 ) mMIcalibs  = menu.add( R.string.title_calib );
    if ( mApp.mBootloader ) mMIfirmware = menu.add( R.string.menu_firmware );
    mMIoptions = menu.add( R.string.menu_options );
    mMIhelp    = menu.add( R.string.menu_help  );

    mMIscan.setIcon(    icons[6] );
    mMIdetach.setIcon(  icons[7] );
    if ( mApp.VERSION30 ) mMIcalibs.setIcon(  icons[8] );
    if ( mApp.mBootloader ) mMIfirmware.setIcon( icons[9] );
    mMIoptions.setIcon( icons[10] );
    mMIhelp.setIcon(    icons[11] );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    if ( item == mMIoptions ) {         // OPTIONS DIALOG
      Intent intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_DEVICE );
      startActivity( intent );
    } else if ( item == mMIdetach  ) {  // DETACH DEVICE
      detachDevice();
    } else if ( mApp.VERSION30 && item == mMIcalibs  ) {  // CALIBRATIONS
      if ( mApp.mDevice == null ) {
        Toast.makeText(this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else {
        // List<String> calibs = mApp.mData.selectDeviceCalibs( mApp.mDevice.mAddress );
        // Log.v("DistoX", "calibs " + calibs.size() );
        (new CalibListDialog( this, this, mApp )).show();
      }
    } else if ( item == mMIscan ) {     // SCAN
      Intent scanIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceList.class );
      scanIntent.putExtra( TopoDroidApp.TOPODROID_DEVICE_ACTION, DeviceList.DEVICE_SCAN );
      startActivityForResult( scanIntent, REQUEST_DEVICE );
      Toast.makeText(this, R.string.wait_scan, Toast.LENGTH_LONG).show();
    } else if ( item == mMIhelp  ) {    // HELP DIALOG
      (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 6 ) ).show();
    } else if ( item == mMIfirmware ) { // FIRMWARE
      (new FirmwareDialog( this, this, mApp )).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  */

  private void setMenuAdapter()
  {
    Resources res = getResources();
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    mMenuAdapter.add( res.getString( menus[0] ) );
    if ( TopoDroidSetting.mLevelOverBasic ) mMenuAdapter.add( res.getString( menus[1] ) );
    if ( mApp.VERSION30 )   mMenuAdapter.add( res.getString( menus[2] ) );
    if ( TopoDroidSetting.mBootloader ) mMenuAdapter.add( res.getString( menus[3] ) );
    mMenuAdapter.add( res.getString( menus[4] ) );
    mMenuAdapter.add( res.getString( menus[5] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  String readDistoXCode()
  {
    byte[] ret = mApp.readMemory( mDevice.mAddress, 0x8008 );
    if ( ret == null ) return getResources().getString( R.string.device_busy );
    int code = MemoryOctet.toInt( ret[1], ret[0] );
    return String.format( getResources().getString( R.string.device_code ), code );
  }

  String readX310firmware()
  {
    byte[] ret = mApp.readMemory( mDevice.mAddress, 0xe000 );
    if ( ret == null ) return getResources().getString( R.string.device_busy );
    return String.format( getResources().getString( R.string.device_firmware ), ret[0], ret[1] );
  }

  String readX310hardware()
  {
    byte[] ret = mApp.readMemory( mDevice.mAddress, 0xe004 );
    if ( ret == null ) return getResources().getString( R.string.device_busy );
    return String.format( getResources().getString( R.string.device_hardware ), ret[0], ret[1] );
  }


  byte readA3status()
  {
    byte[] ret = mApp.readMemory( mDevice.mAddress, 0x8000 );
    return ( ret == null )? 0 : ret[0];
  }

  // @Override 
  // public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  // {
  //   String item_str = (String) mArrayAdapter.getItem(pos); // "model name addr"
  //   String val[] = item_str.split(" ");

  //   return true;
  // }

  void openCalibration( String name )
  {
    int mustOpen = 0;
    mApp.setCalibFromName( name );
    Intent calibIntent = new Intent( Intent.ACTION_EDIT ).setClass( this, CalibActivity.class );
    calibIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY, mustOpen ); // FIXME not handled yet
    startActivity( calibIntent );
  }


}

