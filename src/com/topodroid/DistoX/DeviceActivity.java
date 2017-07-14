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
 */
package com.topodroid.DistoX;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;

import java.io.File;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.IOException;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.os.Bundle;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.BroadcastReceiver;
import android.content.ComponentName;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.RadioButton;
import android.view.View;
import android.view.KeyEvent;
// import android.widget.RadioGroup;
// import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

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
                            , OnItemLongClickListener
                            // , RadioGroup.OnCheckedChangeListener
{
  private TopoDroidApp mApp;

  private TextView mTvAddress;

  private static int izonsno[] = {
                        0,
                        R.drawable.iz_toggle_no,
                        R.drawable.iz_compute_no,
                        0,
                        R.drawable.iz_read_no,
                        0,
                        // R.drawable.iz_remote_no
                     };

  private static int izons[] = {
                        R.drawable.iz_bt,
                        R.drawable.iz_toggle,
                        R.drawable.iz_compute,
                        R.drawable.iz_info,
                        R.drawable.iz_read,
                        R.drawable.iz_sdcard
                     };

  BitmapDrawable mBMtoggle;
  BitmapDrawable mBMtoggle_no;
  BitmapDrawable mBMcalib;
  BitmapDrawable mBMcalib_no;
  BitmapDrawable mBMread;
  BitmapDrawable mBMread_no;

  final int IDX_TOGGLE = 1;
  final int IDX_CALIB  = 2;
  final int IDX_INFO   = 3;
  final int IDX_READ   = 4;
  final int IDX_MEMORY = 5;

  private static int menus[] = {
                        R.string.menu_scan,
                        R.string.menu_pair,
                        R.string.menu_detach,
                        R.string.menu_firmware,
                        R.string.menu_options,
                        R.string.menu_help
                        // CALIB_RESET , R.string.menu_calib_reset
                     };

  private static int help_icons[] = {
                        R.string.help_bluetooth,
                        R.string.help_toggle,
                        R.string.title_calib,
                        R.string.help_info_device,
                        R.string.help_read,
                        R.string.help_sdcard
                        // R.string.help_remote
                     };
  private static int help_menus[] = {
                        R.string.help_scan,
                        R.string.help_pair,
                        R.string.help_detach,
                        R.string.help_firmware,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  // private ArrayAdapter<String> mArrayAdapter;
  private ListItemAdapter mArrayAdapter;
  private ListView mList;

  // private String mAddress;
  private Device mDevice;

  final BroadcastReceiver mPairReceiver = new BroadcastReceiver()
  {
    public void onReceive( Context ctx, Intent intent )
    {
      if ( BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals( intent.getAction() ) ) {
        final int state = intent.getIntExtra( BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR );
        final int prev  = intent.getIntExtra( BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE, BluetoothDevice.ERROR );
        if ( state == BluetoothDevice.BOND_BONDED && prev == BluetoothDevice.BOND_BONDING ) {
          // FIXME Toast.makeText( this, R.string.device_paired, Toast.LENGTH_SHORT).show();
          updateList();
        }
      }
    }
  };

// -------------------------------------------------------------------
  private void setState()
  {
    boolean cntd = mApp.isCommConnected();
    if ( mDevice != null ) { // mAddress.length() > 0 ) {
      mTvAddress.setTextColor( 0xffffffff );
      mTvAddress.setText( String.format( getResources().getString( R.string.using ), mDevice.toString() ) );
      // setButtonRemote();
    } else {
      mTvAddress.setTextColor( 0xffff0000 );
      mTvAddress.setText( R.string.no_device_address );
    }
    // TDLog.Debug("set state updates list");
    updateList();
  }  

  // ---------------------------------------------------------------
  // private Button mButtonHelp;
  private Button[] mButton1;
  private int mNrButton1 = 6; // 7 if ButtonRemote
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  ListView   mMenu;
  Button     mImage;
  // HOVER
  // MyMenuAdapter mMenuAdapter;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;


  // private void setButtonRemote( )
  // {
  //   if ( TDSetting.mLevelOverNormal ) {
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
    updateList();
  }

  void setDeviceName( Device device, String nickname )
  {
    mApp.setDeviceName( device, nickname );
    updateList();
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Debug("device activity on create");
    mApp = (TopoDroidApp) getApplication();

    mDevice  = mApp.mDevice;
    // mAddress = mDevice.mAddress;
    // mAddress = getIntent().getExtras().getString(   TDTag.TOPODROID_DEVICE_ADDR );

    setContentView(R.layout.device_activity);
    mTvAddress = (TextView) findViewById( R.id.device_address );

    mListView = (HorizontalListView) findViewById(R.id.listview);
    int size = mApp.setListViewHeight( mListView );

    Resources res = getResources();
    mNrButton1 = 3;
    if ( TDSetting.mLevelOverNormal ) mNrButton1 += 2; // CALIB-READ INFO
    if ( TDSetting.mLevelOverAdvanced ) mNrButton1 += 1; // MEMORY
    mButton1 = new Button[ mNrButton1 ];

    // int k=0; 
    // mButton1[k++] = MyButton.getButton( this, this, izons[0] );
    // mButton1[k++] = MyButton.getButton( this, this, izons[1] );
    // mButton1[k++] = MyButton.getButton( this, this, izons[2] );
    // if ( TDSetting.mLevelOverNormal   ) mButton1[k++] = MyButton.getButton( this, this, izons[3] ); // INFO
    // if ( TDSetting.mLevelOverNormal   ) mButton1[k++] = MyButton.getButton( this, this, izons[4] ); // CALIB_READ
    // if ( TDSetting.mLevelOverAdvanced ) mButton1[k++] = MyButton.getButton( this, this, izons[5] ); // MEMORY
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( this, this, izons[k] );
    }

    mBMtoggle    = MyButton.getButtonBackground( mApp, res, izons[IDX_TOGGLE] );
    mBMtoggle_no = MyButton.getButtonBackground( mApp, res, izonsno[IDX_TOGGLE] );
    mBMcalib    = MyButton.getButtonBackground( mApp, res, izons[IDX_CALIB] );
    mBMcalib_no = MyButton.getButtonBackground( mApp, res, izonsno[IDX_CALIB] );
    mBMread    = MyButton.getButtonBackground( mApp, res, izons[IDX_READ] );
    mBMread_no = MyButton.getButtonBackground( mApp, res, izonsno[IDX_READ] );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    // mArrayAdapter = new ArrayAdapter<String>( this, R.layout.message );
    mArrayAdapter = new ListItemAdapter( this, R.layout.message );
    mList = (ListView) findViewById(R.id.dev_list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    // mList.setLongClickable( true );
    mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );
    // TDLog.Debug("device activity layout done");

    setState();
    // TDLog.Debug("device activity state done");

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mImage.setBackgroundDrawable( MyButton.getButtonBackground( mApp, res, R.drawable.iz_menu ) );

    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter( res );
    closeMenu();
    // HOVER
    mMenu.setOnItemClickListener( this );
    // TDLog.Debug("device activity create done");
  }

  private void updateList( )
  {
    // TDLog.Debug("device activity update list" );
    // mList.setAdapter( mArrayAdapter );
    mArrayAdapter.clear();
    if ( TDSetting.mLevelOverTester ) { // FIXME VirtualDistoX
      mArrayAdapter.add( "X000" );
    }
    if ( mApp.mBTAdapter != null ) {
      Set<BluetoothDevice> device_set = mApp.mBTAdapter.getBondedDevices(); // get paired devices
      if ( device_set.isEmpty() ) {
        // Toast.makeText(this, R.string.no_paired_device, Toast.LENGTH_SHORT).show();
      } else {
        setTitle( R.string.title_device );
        for ( BluetoothDevice device : device_set ) {
          String addr  = device.getAddress();
          Device dev = mApp.mDData.getDevice( addr );
          if ( dev == null ) {
            String model = device.getName();
            if ( model == null ) {
              TDLog.Error( "WARNING. Null name for device " + addr );
            } else if ( model.startsWith( "DistoX", 0 ) ) {
              String name  = Device.modelToName( model );
              mApp.mDData.insertDevice( addr, model, name );
              dev = mApp.mDData.getDevice( addr );
            }
          }
          if ( dev != null ) {
            // // TDLog.Error( "device " + name );
            // if ( dev.mModel.startsWith("DistoX-") ) {      // DistoX2 X310
            //   mArrayAdapter.add( " X310 " + dev.mName + " " + addr );
            // } else if ( dev.mModel.equals("DistoX") ) {    // DistoX A3
            //   mArrayAdapter.add( " A3 " + dev.mName + " " + addr );
            // } else {
            //   // do not add
            // }
            mArrayAdapter.add( dev.toString() );
          }
        }
      }
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }

    if ( onMenu ) {
      closeMenu();
      return;
    }

    CharSequence item = ((TextView) view).getText();
    // TDLog.Log( TDLog.LOG_INPUT, "DeviceActivity onItemClick() " + item.toString() );
    // String value = item.toString();
    // if ( value.startsWith( "DistoX", 0 ) ) 
    {
      StringBuffer buf = new StringBuffer( item );
      int k = buf.lastIndexOf(" ");
      String[] vals = item.toString().split(" ", 3 );
      String address = ( vals[0].equals("X000") )? Device.ZERO_ADDRESS : vals[2];
      // String address = vals[2]; // FIXME VirtualDistoX

      // if ( vals.length != 3 ) { TODO } // FIXME
      // Log.v("DistoX", "Addr/Name <" + vals[2] + ">");
      if ( mDevice == null || ! ( address.equals( mDevice.mAddress ) || address.equals( mDevice.mNickname ) ) ) {
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
    if ( mDevice == null ) return;
    mApp.setDevice( null );
    mDevice = mApp.mDevice;
    // mAddress = address;
    mApp.disconnectRemoteDevice( true );
    setState();
  }



  void pairDevice()
  {
    if ( mDevice == null ) return;
    BluetoothDevice device = mApp.mBTAdapter.getRemoteDevice( mDevice.mAddress );
    switch ( DeviceUtil.pairDevice( device ) ) {
      case -1: // failure
        // Toast.makeText( this, R.string.pairing_failed, Toast.LENGTH_SHORT).show(); // TODO
        break;
      case 2: // already paired
        // Toast.makeText( this, R.string.device_paired, Toast.LENGTH_SHORT).show();
        break;
      default: // 0: null device
               // 1: paired ok
    }
  }


  @Override
  public void enableButtons( boolean enable ) 
  {
    mButton1[1].setEnabled( enable );
    if ( TDSetting.mLevelOverNormal ) {
      for ( int k=2; k<mNrButton1; ++k ) {
        mButton1[k].setEnabled( enable );
      }
    }
    if ( enable ) {
      setTitleColor( TDColor.NORMAL );
      mButton1[IDX_TOGGLE].setBackgroundDrawable( mBMtoggle );
      mButton1[IDX_CALIB].setBackgroundDrawable( mBMcalib );
      if ( TDSetting.mLevelOverNormal ) {
        mButton1[IDX_READ].setBackgroundDrawable( mBMread);
      }
    } else {
      setTitleColor( TDColor.CONNECTED );
      mButton1[IDX_TOGGLE].setBackgroundDrawable( mBMtoggle_no );
      mButton1[IDX_CALIB].setBackgroundDrawable( mBMcalib_no );
      if ( TDSetting.mLevelOverNormal ) {
        mButton1[IDX_READ].setBackgroundDrawable( mBMread_no );
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

    // TDLog.Log( TDLog.LOG_INPUT, "DeviceActivity onClick() button " + b.getText().toString() ); 

    // FIXME COMMENTED
    // TopoDroidComm comm = mApp.mComm;
    // if ( comm == null ) {
    //   Toast.makeText( this, R.string.connect_failed, Toast.LENGTH_SHORT).show();
    //   return;
    // }

    int k = 0;
    if ( k < mNrButton1 && b == mButton1[k++] ) {         // RESET COMM STATE [This is fast]
      mApp.resetComm();
      setState();
      Toast.makeText(this, R.string.bt_reset, Toast.LENGTH_SHORT).show();
    } else if ( k < mNrButton1 &&  b == mButton1[k++] ) { // CALIBRATION MODE TOGGLE
      if ( mDevice == null ) { // mAddress.length() < 1 ) {
        Toast.makeText( this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else {
        enableButtons( false );
        new CalibToggleTask( this, this, mApp ).execute();
      }
    } else if ( k < mNrButton1 &&  b == mButton1[k++] ) { // CALIBRATIONS
      if ( mApp.mDevice == null ) {
        Toast.makeText(this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else {
        (new CalibListDialog( this, this, mApp )).show();
      }

    } else if ( k < mNrButton1 && b == mButton1[k++] ) {    // INFO mLevelOverNormal
      if ( mDevice == null ) {
        Toast.makeText( this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else {
        // setTitleColor( TDColor.CONNECTED ); // USELESS
        if ( mDevice.mType == Device.DISTO_A3 ) {
          new DeviceA3InfoDialog( this, this, mDevice ).show();
        } else if ( mDevice.mType == Device.DISTO_X310 ) {
          new DeviceX310InfoDialog( this, this, mDevice ).show();
        } else {
          TDLog.Error( "Unknown DistoX type " + mDevice.mType );
        }
        // setTitleColor( TDColor.NORMAL );
      }

    } else if ( k < mNrButton1 && b == mButton1[k++] ) {   // CALIB_READ mLevelOverNormal
      if ( mDevice == null ) { // mAddress.length() < 1 ) {
        Toast.makeText( this, R.string.no_device_address, Toast.LENGTH_SHORT).show();
      } else {
        enableButtons( false );
        new CalibReadTask( this, this, mApp, CalibReadTask.PARENT_DEVICE ).execute();
      }

    } else if ( k < mNrButton1 &&  b == mButton1[k++] ) { // DISTOX MEMORY mLevelOverAdvanced
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
    // TDLog.Debug("device activity on resume" );
    registerReceiver( mPairReceiver, new IntentFilter( BluetoothDevice.ACTION_BOND_STATE_CHANGED ) );
    mApp.resumeComm();
    mApp.mDeviceActivityVisible = true;
    // TDLog.Debug("device activity on resume done" );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    mApp.mDeviceActivityVisible = false;
    unregisterReceiver( mPairReceiver );
  }

  // -----------------------------------------------------------------------------

  boolean readDeviceHeadTail( byte[] command, int[] head_tail )
  {
    // TDLog.Log( TDLog.LOG_DEVICE, "onClick mBtnHeadTail. Is connected " + mApp.isConnected() );
    String ht = mApp.readHeadTail( mDevice.mAddress, command, head_tail );
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
    if ( ! mApp.mDData.updateDeviceHeadTail( mDevice.mAddress, head_tail ) ) {
      Toast.makeText( this, getString(R.string.head_tail_store_failed), Toast.LENGTH_SHORT).show();
    }
  }

  void retrieveDeviceHeadTail( int[] head_tail )
  {
    // Log.v(TopoDroidApp.TAG, "store HeadTail " + mDevice.mAddress + " : " + head_tail[0] + " " + head_tail[1] );
    mApp.mDData.getDeviceHeadTail( mDevice.mAddress, head_tail );
  }

  void readX310Info( DeviceX310InfoDialog dialog )
  {
    ( new InfoReadX310Task( mApp, dialog, mDevice.mAddress ) ).execute();
  }

  void readA3Info( DeviceA3InfoDialog dialog )
  {
    ( new InfoReadA3Task( mApp, dialog, mDevice.mAddress ) ).execute();
  }

  // @param head_tail indices
  void readX310Memory( IMemoryDialog dialog, int[] head_tail, String dumpfile )
  {
    ( new MemoryReadTask( mApp, dialog, Device.DISTO_X310, mDevice.mAddress, head_tail, dumpfile ) ).execute();
  }
 
  // @param head_tail addresses
  void readA3Memory( IMemoryDialog dialog, int[] head_tail, String dumpfile )
  {
    if (    head_tail[0] < 0 || head_tail[0] >= DeviceA3Details.MAX_ADDRESS_A3 
         || head_tail[1] < 0 || head_tail[1] >= DeviceA3Details.MAX_ADDRESS_A3 ) {
      Toast.makeText(this, R.string.device_illegal_addr, Toast.LENGTH_SHORT).show();
      return;
    }
    ( new MemoryReadTask( mApp, dialog, Device.DISTO_A3, mDevice.mAddress, head_tail, dumpfile ) ).execute();
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
    TopoDroidAlertDialog.makeAlert( this, getResources(),
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
    // Log.v("DistoX", "on Activity Result: req. " + request + " res. " + result );
    Bundle extras = (intent != null)? intent.getExtras() : null;
    switch ( request ) {
      case TDRequest.REQUEST_DEVICE:
        if ( result == RESULT_OK ) {
          String address = extras.getString( TDTag.TOPODROID_DEVICE_ACTION );
          // TDLog.Log(TDLog.LOG_DISTOX, "OK " + address );
          if ( address == null ) {
            TDLog.Error( "onActivityResult REQUEST DEVICE: null address");
          } else if ( mDevice == null || ! address.equals( mDevice.mAddress ) ) { // N.B. address != null
            mApp.disconnectRemoteDevice( true );
            mApp.setDevice( address );

            if ( TDSetting.mAutoPair ) { // try to get the system ask for the PIN
              BluetoothDevice btDevice = mApp.mBTAdapter.getRemoteDevice( address );
              // TDLog.Log( TDLog.LOG_BT, "auto-pairing remote device " + btDevice.getAddress()
              //   + " status " + btDevice.getBondState() );
              if ( ! DeviceUtil.isPaired( btDevice ) ) {
                DeviceUtil.pairDevice( btDevice );
                DeviceUtil.bindDevice( btDevice );
                for (int c=0; c<TDSetting.mConnectSocketDelay; ++c ) {
                  if ( DeviceUtil.isPaired( btDevice ) ) break;
                  try {
                    // Thread.yield();
                    Thread.sleep( 100 );
                  } catch ( InterruptedException e ) { }
                }
              }
            }

            mDevice = mApp.mDevice;
            // mAddress = address;
            setState();
          }
        } else if ( result == RESULT_CANCELED ) {
          TDLog.Error( "CANCELED");
          // finish(); // back to survey
        }
        updateList();
        break;
      // case TDRequest.REQUEST_ENABLE_BT:
      //   if ( result == Activity.RESULT_OK ) {
      //     // nothing to do: scanBTDevices(); is called by menu CONNECT
      //   } else {
      //     Toast.makeText(this, R.string.not_enabled, Toast.LENGTH_SHORT).show();
      //     finish();
      //   }
      //   break;
    }
  }

  @Override
  public boolean onSearchRequested()
  {
    // TDLog.Error( "search requested" );
    Intent intent = new Intent( this, TopoDroidPreferences.class );
    intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_DEVICE );
    startActivity( intent );
    return true;
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      case KeyEvent.KEYCODE_SEARCH:
        return onSearchRequested();
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.DeviceActivity );
        if ( help_page != null ) UserManualActivity.showHelpPage( this, help_page );
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  // ---------------------------------------------------------

  private void setMenuAdapter( Resources res )
  {
    // HOVER
    // mMenuAdapter = new MyMenuAdapter( this, this, mMenu, R.layout.menu, new ArrayList< MyMenuItem >() );
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );

    if ( TDSetting.mLevelOverBasic    ) mMenuAdapter.add( res.getString( menus[0] ) );
    if ( TDSetting.mLevelOverBasic    ) mMenuAdapter.add( res.getString( menus[1] ) );
    if ( TDSetting.mLevelOverNormal   ) mMenuAdapter.add( res.getString( menus[2] ) );
    if ( TDSetting.mLevelOverAdvanced ) mMenuAdapter.add( res.getString( menus[3] ) );
    mMenuAdapter.add( res.getString( menus[4] ) );
    mMenuAdapter.add( res.getString( menus[5] ) );
    // mMenuAdapter.add( res.getString( menus[6] ) ); // SERVER
    // CALIB_RESET
    // if ( TDSetting.mLevelOverTester ) mMenuAdapter.add( res.getString( menus[6] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    // HOVER
    // mMenuAdapter.resetBgColor();
    onMenu = false;
  }

  private void handleMenu( int pos )
  {
    closeMenu();
    int p = 0;
    if ( TDSetting.mLevelOverBasic && p++ == pos ) { // SCAN
      Intent scanIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DeviceList.class );
      scanIntent.putExtra( TDTag.TOPODROID_DEVICE_ACTION, DeviceList.DEVICE_SCAN );
      startActivityForResult( scanIntent, TDRequest.REQUEST_DEVICE );
      Toast.makeText(this, R.string.wait_scan, Toast.LENGTH_LONG).show();

    } else if ( TDSetting.mLevelOverBasic && p++ == pos ) { // PAIR
      pairDevice();

    } else if ( TDSetting.mLevelOverNormal && p++ == pos ) { // DETACH
      detachDevice();

    } else if ( TDSetting.mLevelOverAdvanced && p++ == pos ) { // FIRMWARE
      if ( TDSetting.mCommType != 0 ) {
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
      (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length ) ).show();
    // } else if ( TDSetting.mLevelOverTester && p++ == pos ) { // CALIB_RESET
    //   doCalibReset();
    }
  }

  void askCalibReset( final Button b )
  {
    TopoDroidAlertDialog.makeAlert( this, getResources(), getResources().getString( R.string.calib_reset ),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doCalibReset( b );
        }
      }
    );
  }

  private void doCalibReset( Button b )
  {
    // Log.v("DistoX", "CALIB RESET");
    if ( mDevice != null ) {
      long one = (long)Math.round( TopoDroidUtil.FM );
      // if (one > TopoDroidUtil.ZERO ) one = TopoDroidUtil.NEG - one;
      byte low  = (byte)( one & 0xff );
      byte high = (byte)((one >> 8) & 0xff );
      byte zeroNL = CalibAlgo.floatToByteNL( 0 );
      byte[] coeff = new byte[52];
      for ( int k=0; k<52; ++k ) coeff[k] = (byte)0x00;
      coeff[ 2] = low;
      coeff[ 3] = high;
      coeff[12] = low;
      coeff[13] = high;
      coeff[22] = low;
      coeff[23] = high;
      coeff[26] = low;
      coeff[27] = high;
      coeff[36] = low;
      coeff[37] = high;
      coeff[46] = low;
      coeff[47] = high;
      coeff[48] = zeroNL;
      coeff[49] = zeroNL;
      coeff[50] = zeroNL;
      coeff[51] = zeroNL;
      mApp.uploadCalibCoeff( this, coeff, false, b );
    }
  }

  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    String item_str = (String) mArrayAdapter.getItem(pos); // "model name addr"
    if ( item_str.equals("X000") ) return true;
    String vals[] = item_str.split(" ", 3);
    String address = vals[2]; // address or nickname
    Device device = mApp.mDData.getDevice( address );
    if ( device != null ) {
      (new DeviceNameDialog( this, this, device )).show();
    }
    return true;
  }

  void openCalibration( String name )
  {
    int mustOpen = 0;
    mApp.setCalibFromName( name );
    Intent calibIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, CalibActivity.class );
    calibIntent.putExtra( TDTag.TOPODROID_SURVEY, mustOpen ); // FIXME not handled yet
    startActivity( calibIntent );
  }

  void openCalibrationImportDialog()
  {
    if ( mDevice != null ) {
      (new CalibImportDialog( this, this )).show();
    }
  }

  void importCalibFile( String name )
  {
    String filename = TDPath.getCCsvFile( name );
    File file = new File( filename );
    if ( ! file.exists() ) {
      Toast.makeText(this, R.string.file_not_found, Toast.LENGTH_SHORT).show();
    } else {
      switch ( TDExporter.importCalibFromCsv( mApp.mDData, filename, mDevice.mAddress ) ) {
        case 0:
          Toast.makeText(this, R.string.import_calib_ok, Toast.LENGTH_SHORT).show();
          break;
        case -1:
          Toast.makeText(this, R.string.import_calib_no_header, Toast.LENGTH_SHORT).show();
          break;
        case -2:
          Toast.makeText(this, R.string.import_calib_already, Toast.LENGTH_SHORT).show();
          break;
        case -3:
          Toast.makeText(this, R.string.import_calib_mismatch, Toast.LENGTH_SHORT).show();
          break;
        case -4:
          Toast.makeText(this, R.string.import_calib_no_data, Toast.LENGTH_SHORT).show();
          break;
        case -5:
          Toast.makeText(this, R.string.import_calib_io_error, Toast.LENGTH_SHORT).show();
          break;
        default:
          Toast.makeText(this, R.string.import_failed, Toast.LENGTH_SHORT).show();
      }
    }
  }


}

