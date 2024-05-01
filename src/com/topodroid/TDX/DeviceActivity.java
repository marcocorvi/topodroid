/* @file DeviceActivity.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX device activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.dev.distox_ble.DistoXBLEInfoDialog; // SIWEI
import com.topodroid.dev.distox_ble.DistoXBLEInfoReadTask;
import com.topodroid.dev.distox_ble.DistoXBLEMemoryDialog;
// import com.topodroid.dev.distox_ble.XBLEFirmwareDialog;

import com.topodroid.ui.TDProgress;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDString;
import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDUtil;
// import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDRequest;
import com.topodroid.utils.TDLocale;
import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;
import com.topodroid.packetX.PacketDialog;
import com.topodroid.dev.Device;
import com.topodroid.dev.DeviceUtil;
import com.topodroid.dev.distox.MemoryReadTask;
import com.topodroid.dev.distox.IMemoryDialog;
import com.topodroid.dev.distox2.DeviceX310MemoryDialog;
import com.topodroid.dev.distox2.DeviceX310InfoDialog;
import com.topodroid.dev.distox2.InfoReadX310Task;
import com.topodroid.dev.distox2.FirmwareDialog;
import com.topodroid.dev.distox1.DeviceA3MemoryDialog;
import com.topodroid.dev.distox1.DeviceA3InfoDialog;
import com.topodroid.dev.distox1.InfoReadA3Task;
import com.topodroid.dev.distox1.DeviceA3Details;
// import com.topodroid.dev.ble.BleScanDialog; // BLE_SCAN
import com.topodroid.dev.bric.BricMemoryDialog;
import com.topodroid.dev.bric.MemoryBricTask;
import com.topodroid.dev.bric.BricInfoDialog;
import com.topodroid.dev.bric.InfoReadBricTask;
import com.topodroid.calib.CalibCoeffDialog;
import com.topodroid.calib.CalibImportDialog;
import com.topodroid.calib.CalibListDialog;
import com.topodroid.calib.CalibToggleTask;
import com.topodroid.calib.CalibReadTask;
import com.topodroid.calib.CalibAlgo;
import com.topodroid.calib.ICoeffDisplayer;
import com.topodroid.calib.CalibExport;

import java.util.Set;
import java.util.Locale;
// import java.util.List;
import java.util.ArrayList;

import java.io.File; // private app files (ccsv)

// import java.lang.reflect.Method;
// import java.lang.reflect.InvocationTargetException;

import android.app.Activity;
import android.os.Bundle;
import android.os.ParcelUuid;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.BroadcastReceiver;
// import android.content.ComponentName;
import android.content.res.Configuration;

import android.widget.TextView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
// import android.widget.RadioButton;
import android.view.View;
import android.view.KeyEvent;
// import android.widget.RadioGroup;
// import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

// import android.net.Uri;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothClass;
// import android.bluetooth.BluetoothAdapter;

public class DeviceActivity extends Activity
                            implements View.OnClickListener
                            , OnItemClickListener
                            , ICoeffDisplayer
                            , OnItemLongClickListener
                            // , RadioGroup.OnCheckedChangeListener
{
  private TopoDroidApp mApp;
  private DeviceHelper mApp_mDData;

  // referrer ( getReferrer() is from API-22 ) // FIXME
  // public final int REFERRER_NONE = 0;
  // public final int REFERRER_MAIN = 1;
  // public final int REFERRER_SHOT = 2;
  // private int mReferrer = REFERRER_NONE;
  // 
  // public int getMyReferrer() { return mReferrer; }

  // public static final int MODE_NORMAL = 0;
  // public static final int MODE_SELECT = 1;
  // private int mMode = MODE_NORMAL; // mode of work of the activity
  
  public static boolean mDeviceActivityVisible = false;

  private TextView mTvAddress;
  private TextView mTvAddressB;

  private static final int[] izonsno = {
                        0,
                        0,
                        R.drawable.iz_toggle_no,
                        R.drawable.iz_2compute_no,
                        R.drawable.iz_read_no,
                        0,
                        // R.drawable.iz_remote_no
                     };

  private static final int[] izons = {
                        R.drawable.iz_bt,
                        R.drawable.iz_info,
                        R.drawable.iz_toggle,
                        R.drawable.iz_2compute,
                        R.drawable.iz_read,
                        R.drawable.iz_sdcard
			// R.drawable.iz_empty // EMPTY
                     };

  private BitmapDrawable mBMtoggle;
  private BitmapDrawable mBMtoggle_no;
  private BitmapDrawable mBMcalib;
  private BitmapDrawable mBMcalib_no;
  private BitmapDrawable mBMread;
  private BitmapDrawable mBMread_no;

  // static final private int IDX_BT     = 0;
  static final private int IDX_INFO   = 1;
  static final private int IDX_TOGGLE = 2;
  static final private int IDX_CALIB  = 3;
  static final private int IDX_READ   = 4;
  static final private int IDX_MEMORY = 5;

  private static final int[] menus = {
                        // R.string.menu_scan,
                        // R.string.menu_scan_ble, // FIXME_SCAN_BRIC
                        // R.string.menu_pair,
                        R.string.menu_detach,
                        R.string.menu_firmware,
                        R.string.menu_device_add, // BT_NONAME
                        R.string.menu_packets,
                        R.string.menu_options,
                        R.string.menu_help
                        // CALIB_RESET , R.string.menu_calib_reset
                     };

  private static final int[] help_icons = {
                        R.string.help_bluetooth,
                        R.string.help_info_device,
                        R.string.help_toggle,
                        R.string.title_calib,
                        R.string.help_read,
                        R.string.help_sdcard
                        // R.string.help_remote
                     };
  private static final int[] help_menus = {
                        // R.string.help_scan,
                        // R.string.help_scan_ble, // FIXME_SCAN_BRIC
                        // R.string.help_pair,
                        R.string.help_detach,
                        R.string.help_firmware,
                        // R.string.help_device_add, // UNNAMED
                        R.string.help_packets,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  private static final int HELP_PAGE = R.string.DeviceActivity;

  private ListItemAdapter mArrayAdapter; // populated by updateList
  private ListView mList;

  private ArrayList< BluetoothDevice > mNonameList = null;  // BT_NONAME
  private boolean mHasNonameDevice = false;

  private Device currDeviceA() { return TDInstance.getDeviceA(); }
  private Device currDeviceB() { return TDInstance.getDeviceB(); }

  private boolean mHasBLE     = false; // BRIC default to false

  private final BroadcastReceiver mPairReceiver = new BroadcastReceiver()
  {
    public void onReceive( Context ctx, Intent intent )
    {
      if ( DeviceUtil.ACTION_BOND_STATE_CHANGED.equals( intent.getAction() ) ) {
        final int state = intent.getIntExtra( DeviceUtil.EXTRA_BOND_STATE, DeviceUtil.ERROR );
        final int prev  = intent.getIntExtra( DeviceUtil.EXTRA_PREVIOUS_BOND_STATE, DeviceUtil.ERROR );
        if ( state == DeviceUtil.BOND_BONDED && prev == DeviceUtil.BOND_BONDING ) {
          // FIXME TDToast.make( R.string.device_paired );
          updateList( true );
        }
      }
    }
  };

  // BT_NONAME
  /** update the list of devices with no name
   * @return true if there is at least one device with no name
   */
  private boolean updateNonameList()
  {
    if ( mNonameList == null ) {
      mNonameList = new ArrayList< BluetoothDevice >();
    } else {
      mNonameList.clear();
    }
    Set<BluetoothDevice> device_set = DeviceUtil.getBondedDevices(); // get paired devices
    if ( device_set == null || device_set.isEmpty() ) return false;
    for ( BluetoothDevice device : device_set ) {
      if ( device.getName() == null && device.getType() == 2 ) { // only type 2 (BLE)
        mNonameList.add( device );
      }
    }
    return ! mNonameList.isEmpty();
  }

// -------------------------------------------------------------------
  /** set the state of the interface
   * @param   check_noname whether to check for devices with no name
   */
  private void setState( boolean check_noname )
  {
    boolean cntd = mApp.isCommConnected();
    if ( currDeviceA() != null ) {
      mTvAddress.setTextColor( 0xffffffff );
      String str = currDeviceA().toString();
      // TDLog.v( "using <" + str + ">" );
      mTvAddress.setText( String.format( getResources().getString( R.string.using ), currDeviceA().toString() ) );
      // setButtonRemote();
    } else {
      mTvAddress.setTextColor( 0xffff0000 );
      mTvAddress.setText( R.string.no_device_address );
    }
    if ( TDSetting.mSecondDistoX ) {
      if ( currDeviceB() != null ) { 
        mTvAddressB.setTextColor( 0xffffcc33 );
        mTvAddressB.setText( String.format( getResources().getString( R.string.using ), currDeviceB().toString() ) );
      } else {
        mTvAddressB.setText( "" );
      }
    } else {
      mTvAddressB.setVisibility( View.GONE );
    }
    // TDLog.Debug("set state updates list");
    updateList( check_noname );
  }  

  // ---------------------------------------------------------------
  // private Button mButtonHelp;
  private Button[] mButton1;
  private int mNrButton1 = 6; // 7 if ButtonRemote
  private MyHorizontalListView mListView;
  private MyHorizontalButtonView mButtonView1;
  private ListView   mMenu;
  private Button     mImage;
  private boolean onMenu;


  // private void setButtonRemote( )
  // {
  //   if ( TDLevel.overNormal ) {
  //     if ( currDeviceA() != null && currDeviceA().mType == Device.DISTO_X310 ) {
  //       mButton1[ indexButtonRemote ].setEnabled( true );
  //       mButton1[ indexButtonRemote ].setBackgroundResource( icons00[ indexButtonRemote ] );
  //     } else {
  //       mButton1[ indexButtonRemote ].setEnabled( false );
  //       mButton1[ indexButtonRemote ].setBackgroundResource( icons00no[ indexButtonRemote ] );
  //     }
  //   }
  // }

  // /**
  //  * @param device    device
  //  * @param model     device model
  //  */
  // void setDeviceModel( Device device, int model )
  // {
  //   TopoDroidApp.setDeviceModel( device, model );
  //   updateList( false );
  // }

  /** set the model of the bluetooth device in the database of the devices 
   * @param device   bluetooth device (must agree with the primary device)
   * @param model    device type
   *
   * @note nothing if the device is not the primary device
   */
  void setDeviceModel( Device device, int model )
  {
    if ( device != null && device == TDInstance.getDeviceA() ) {
      if ( device.mType != model ) {
        if ( Device.isA3( model ) ) {
          mApp_mDData.updateDeviceModel( device.getAddress(), Device.NAME_DISTOX1 );
          device.mType = model;
        } else if ( Device.isX310( model ) ) {
          mApp_mDData.updateDeviceModel( device.getAddress(), Device.NAME_DISTOX2 + "0000" );
          device.mType = model;
        } else if ( Device.isSap5( model ) ) { 
          mApp_mDData.updateDeviceModel( device.getAddress(), Device.NAME_SAP5 + "0000" );
          device.mType = model;
        } else if ( Device.isSap6( model ) ) { // FIXME_SAP6
          TDLog.v("SAP6 set device model: addr " + device.getAddress() + " type " + model );
          mApp_mDData.updateDeviceModel( device.getAddress(), Device.NAME_SAP6 + "0000" );
          device.mType = model;
        } else if ( Device.isBric4( model ) ) {
          mApp_mDData.updateDeviceModel( device.getAddress(), Device.NAME_BRIC4 + "0000" );
          device.mType = model;
        } else if ( Device.isBric5( model ) ) {
          mApp_mDData.updateDeviceModel( device.getAddress(), Device.NAME_BRIC5 + "0000" );
          device.mType = model;
        // } else if ( Device.isX000( model ) ) { // FIXME VirtualDistoX
        //   mDData.updateDeviceModel( device.getAddress(), Device.NAME_DISTOX_0" );
        //   device.mType = model;
        } else if ( Device.isDistoXBLE( model ) ) { // SIWEI_TIAN
          mApp_mDData.updateDeviceModel(device.getAddress(), Device.NAME_DISTOXBLE + "0000");
          device.mType = model;
        } else if ( Device.isCavway( model ) ) {
          mApp_mDData.updateDeviceModel( device.getAddress(), Device.NAME_CAVWAY + "0000" );
          device.mType = model;
        }
      }
      updateList( false );
    }
  }

  /**
   * @param device    device
   * @param nickname  device nickname
   */
  void setDeviceNickname( Device device, String nickname )
  {
    TopoDroidApp.setDeviceName( device, nickname );
    updateList( false );
  }


  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    TDandroid.setScreenOrientation( this );

    // mReferrer = REFERRER_NONE; // FIXME
    // Bundle extras = getIntent().getExtras();
    // if ( extras != null ) {
    //   try { 
    //     String mode = extras.getString( TDTag.TOPODROID_DEVICE_MODE );
    //     if ( mode != null ) mReferrer = Integer.parseInt( mode );
    //   } catch ( Exception e ) { }
    // }
    // TDLog.v( "device mode " + mReferrer );
    
    // Uri referrer = getReferrer(); // API 22 (5.1)
    // TDLog.v( "device referrer " + referrer.toString() );
    
    // mMode = MODE_NORMAL;
    // Bundle extras = getIntent().getExtras();
    // if ( extras != null ) {
    //   try { 
    //     String mode = extras.getString( TDTag.TOPODROID_DEVICE_MODE );
    //     if ( mode != null ) mMode = Integer.parseInt( mode );
    //   } catch ( Exception e ) { }
    // }
    // TDLog.v( "device mode " + mMode );

    // TDLog.Debug("device activity on create");
    mApp = (TopoDroidApp) getApplication();
    mApp_mDData  = TopoDroidApp.mDData;
    // mCurrDevice  = TDInstance.getDeviceA(); // in onResume
    // mCurrDeviceB = TDInstance.getDeviceB();
    mHasBLE      = TDandroid.checkBluetoothLE( this ); // FIXME_SCAN_BRIC

    // mAddress = currDeviceA().getAddress();
    // mAddress = getIntent().getExtras().getString(   TDTag.TOPODROID_DEVICE_ADDR );

    setContentView(R.layout.device_activity);
    mTvAddress  = (TextView) findViewById( R.id.device_address );
    mTvAddressB = (TextView) findViewById( R.id.device_address_b );

    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlaceholder(true);
    /* int size = */ TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    Resources res = getResources();
    // if ( mMode == MODE_NORMAL ) {
      mNrButton1 = 4;
      if ( TDLevel.overNormal )   mNrButton1 += 1; // CALIB-READ
      if ( TDLevel.overAdvanced ) mNrButton1 += 1; // MEMORY
    // } else { // if ( mMode == MODE_SELECT ) 
    //   mNrButton1 = 1; // only BT reset
    // }
    mButton1 = new Button[ mNrButton1 + 1 ];

    // int k=0; 
    // mButton1[k++] = MyButton.getButton( this, this, izons[0] );
    // mButton1[k++] = MyButton.getButton( this, this, izons[1] );
    // mButton1[k++] = MyButton.getButton( this, this, izons[2] );
    // if ( TDLevel.overNormal   ) mButton1[k++] = MyButton.getButton( this, this, izons[3] ); // INFO
    // if ( TDLevel.overNormal   ) mButton1[k++] = MyButton.getButton( this, this, izons[4] ); // CALIB_READ
    // if ( TDLevel.overAdvanced ) mButton1[k++] = MyButton.getButton( this, this, izons[5] ); // MEMORY
    for ( int k=0; k < mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( this, this, izons[k] );
    }
    mButton1[mNrButton1] = MyButton.getButton( this, null, R.drawable.iz_empty );

    // if ( mMode == MODE_NORMAL ) {
      mBMtoggle    = MyButton.getButtonBackground( mApp, res, izons[IDX_TOGGLE] );
      mBMtoggle_no = MyButton.getButtonBackground( mApp, res, izonsno[IDX_TOGGLE] );
      mBMcalib    = MyButton.getButtonBackground( mApp, res, izons[IDX_CALIB] );
      mBMcalib_no = MyButton.getButtonBackground( mApp, res, izonsno[IDX_CALIB] );
      mBMread    = MyButton.getButtonBackground( mApp, res, izons[IDX_READ] );
      mBMread_no = MyButton.getButtonBackground( mApp, res, izonsno[IDX_READ] );
    // }

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    mArrayAdapter = new ListItemAdapter( this, R.layout.message );
    mList = (ListView) findViewById(R.id.dev_list);
    mList.setAdapter( mArrayAdapter );
    mList.setOnItemClickListener( this );
    // mList.setLongClickable( true );
    mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );
    // TDLog.Debug("device activity layout done");

    // setState( true );
    // TDLog.Debug("device activity state done");

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( mApp, res, R.drawable.iz_menu ) );

    mMenu = (ListView) findViewById( R.id.menu );
    mMenu.setOnItemClickListener( this );
    // TDLog.Debug("device activity create done");

    showDistoXButtons();
  }

  private void updateList( boolean check_noname )
  {
    // TDLog.v("device activity update list - check noname " + check_noname );
    if ( check_noname ) mHasNonameDevice = updateNonameList(); // BT_NONAME
    mArrayAdapter.clear();
    // if ( TDLevel.overTester ) { // FIXME VirtualDistoX
    //   mArrayAdapter.add( "X000" );
    // }
    // Map< String, String > bt_aliases = TopoDroid.mDData.selectAllAlias(); // BT_ALIAS

    Set<BluetoothDevice> device_set = DeviceUtil.getBondedDevices(); // get paired devices
    if ( device_set == null || device_set.isEmpty() ) {
      // TDToast.make(R.string.no_paired_device );
    } else { 
      setTitle( R.string.title_device );
      for ( BluetoothDevice device : device_set ) {
        String addr = device.getAddress();
        Device dev = mApp_mDData.getDevice( addr );
// ----------- DEBUG
//         int type = device.getType(); // 0 = unknown, 1 = classis, 2 = LE, 3 both
//         if ( type == 2 ) {
//           BluetoothClass bt_class = device.getBluetoothClass();
//           ParcelUuid[] uuids = device.getUuids();
//           TDLog.v("BT class " + bt_class.toString() + " " + device.getName() + " class " + bt_class.getMajorDeviceClass() + " " + bt_class.getDeviceClass() + " " + addr );
//           if ( uuids != null ) {
//             for ( ParcelUuid puuid : uuids ) {
//               // UUID uuid = puuid.getUuid();
//               TDLog.v("uuid " + puuid.toString() );
//             }
//           } else {
//             TDLog.v("no uuid ");
//           }
//         }
// ----------- END DEBUG
        if ( dev == null ) {
          String bt_name = null;
          try {
            bt_name = device.getName();
          } catch( SecurityException e ) {
            TDLog.Error("SECURITY " + e.getMessage() );
          }
          if ( bt_name == null ) {
            if ( device.getType() == 2 ) { // Bluetooth LE
              TDToast.makeWarn( String.format( getResources().getString( R.string.device_no_name ), addr ) );
              TDLog.v( "WARNING. Null name for device " + addr );
            }
          } else {
            // String aliased = (String)bt_aliases.get( bt_name ); // BT_ALIAS
            String aliased = TopoDroidApp.mDData.getAliasName( bt_name );
            if ( aliased != null ) bt_name = aliased;

            String name = Device.btnameToName( bt_name );
            TDLog.v("BLE " + "Device Activity: bt name <" + bt_name + "> name <" + name + ">" );
            if ( Device.isDistoX( bt_name ) ) {
              mApp_mDData.insertDevice( addr, bt_name, name, null );
              dev = mApp_mDData.getDevice( addr );
            } else if ( Device.isSap( bt_name ) ) { // FIXME SHETLAND FIXME_SAP6
              // if ( TDLevel.overExpert ) {
                TDLog.v("SAP shetland device name " + bt_name + " --> name " + name );
                mApp_mDData.insertDevice( addr, bt_name, name, null );
                dev = mApp_mDData.getDevice( addr );
              // }
            } else if ( Device.isBric( bt_name ) ) {
              // if ( TDLevel.overExpert ) {
                mApp_mDData.insertDevice( addr, bt_name, name, null );
                dev = mApp_mDData.getDevice( addr );
              // }
            }
          }
        } else {
          // if ( ! TDLevel.overExpert ) { // drop SAP and BRIC
          //   if ( dev.isSap() || dev.isBric() ) dev = null;
          // }
        }
        if ( dev != null ) {
          // // TDLog.Error( "device " + name );
          // if ( dev.mModel.startsWith( Device.NAME_DISTOX2 ) ) {      // DistoX2 X310
          //   mArrayAdapter.add( " X310 " + dev.mName + " " + addr );
          // } else if ( dev.mModel.equals( Device.NAME_DISTOX1 ) ) {    // DistoX A3
          //   mArrayAdapter.add( " A3 " + dev.mName + " " + addr );
          // } else {
          //   // do not add
          // }
          mArrayAdapter.add( dev.toString() );
        }
      }
    }
  }
    
  /** react on user tap on an item in the list
   * @param parent  adapter parent
   * @param view    item view
   * @param pos     item position
   * @param id      view id
   */
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

    if ( ! ( view instanceof TextView ) ) {
      TDLog.Error("device activity view instance of " + view.toString() );
      return;
    }
    CharSequence item = ((TextView) view).getText();
    StringBuffer buf = new StringBuffer( item );
    int k = buf.lastIndexOf(" ");
    String[] vals = item.toString().split(" ", 3 );

    // FIXME VirtualDistoX
    // String address = ( vals[0].equals("X000") )? Device.ZERO_ADDRESS : vals[2];
    String model   = vals[0];
    String name    = vals[1];
    String address = vals[2];

    // if ( vals.length != 3 ) { TODO } // FIXME
    // TDLog.v( "Addr/Name <" + vals[2] + ">");
    if ( currDeviceA() == null || ! currDeviceA().hasAddressOrNickname( address ) ) {
      mApp.setDevicePrimary( address, model, name, null );
      // mCurrDevice = TDInstance.getDeviceA();
      mApp.disconnectRemoteDevice( true ); // new DataStopTask( mApp, null, null );
      setState( false );
      showDistoXButtons();
    }
  }

  /** clear the current (active) device
   */
  private void detachDevice()
  {
    // if ( currDeviceB() != null ) {
    //   mCurrDeviceB == null;
    //   mApp.setDeviceB( null, null );
    // }
    if ( currDeviceA() == null ) return;
    mApp.setDevicePrimary( null, null, null, null );
    // mCurrDevice = TDInstance.getDeviceA();
    mApp.disconnectRemoteDevice( true ); // new DataStopTask( mApp, null, null );
    enableButtons( false );
    setState( false );
  }

  // pair the android and the current device
  private void pairDevice()
  {
    if ( currDeviceA() == null ) return;
    BluetoothDevice device = DeviceUtil.getRemoteDevice( currDeviceA().getAddress() );
    if ( device == null ) return;
    switch ( DeviceUtil.pairDevice( device ) ) {
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

  // interface ICoeffDisplayer
  // @Implements
  public boolean isActivityFinishing() { return this.isFinishing(); }

  // @Implements
  public void displayCoeff( TDVector bg, TDMatrix ag, TDVector bm, TDMatrix am, TDVector nL )
  {
    (new CalibCoeffDialog( this, null, bg, ag, bm, am, nL, null, 0.0f, 0.0f, 0.0f, 0.0f, 0, 0.0f, 0.0f, null /*, false */ ) ).show();
  }

  // @Implements
  public void enableButtons( boolean enable ) 
  {
    // if ( mMode == MODE_SELECT ) return; // nothing in mode SELECT
    mButton1[1].setEnabled( enable );
    if ( TDLevel.overNormal ) {
      for ( int k=2; k<mNrButton1; ++k ) {
        mButton1[k].setEnabled( enable );
      }
    }
    if ( enable ) {
      setTitleColor( TDColor.TITLE_NORMAL );
      TDandroid.setButtonBackground( mButton1[IDX_TOGGLE], mBMtoggle );
      TDandroid.setButtonBackground( mButton1[IDX_CALIB], mBMcalib );
      if ( TDLevel.overNormal ) {
        TDandroid.setButtonBackground( mButton1[IDX_READ], mBMread);
      }
    } else {
      setTitleColor( TDColor.CONNECTED );
      TDandroid.setButtonBackground( mButton1[IDX_TOGGLE], mBMtoggle_no );
      TDandroid.setButtonBackground( mButton1[IDX_CALIB], mBMcalib_no );
      if ( TDLevel.overNormal ) {
        TDandroid.setButtonBackground( mButton1[IDX_READ], mBMread_no );
      }
    }
  }

  /** display the buttons for device actions
   * @note the set of buttons depends on the current device
   */
  public void showDistoXButtons( )
  {
    // if ( mMode == MODE_SELECT ) return; // nothing in mode SELECT
    if ( TDInstance.isDeviceDistoX() ) {
      for ( int k=1; k<mNrButton1; ++k ) mButton1[k].setVisibility( View.VISIBLE );
      TDandroid.setButtonBackground( mButton1[IDX_TOGGLE], mBMtoggle ); // this should already be enableButtons()
      TDandroid.setButtonBackground( mButton1[IDX_CALIB], mBMcalib );
      if ( TDLevel.overNormal ) {
        TDandroid.setButtonBackground( mButton1[IDX_READ], mBMread_no );
      }
    } else if ( TDInstance.isDeviceXBLE()) { // SIWEI Changed on Jun 2022
      for ( int k=1; k<mNrButton1; ++k ) mButton1[k].setVisibility( View.VISIBLE ); // FIXME is this OK ?
      TDandroid.setButtonBackground( mButton1[IDX_TOGGLE], mBMtoggle ); // this should already be enableButtons()
      TDandroid.setButtonBackground( mButton1[IDX_CALIB], mBMcalib );
      if ( TDLevel.overNormal ) {
        TDandroid.setButtonBackground( mButton1[IDX_READ], mBMread_no );
      }
    } else if ( TDInstance.isDeviceBric() ) {
      mButton1[IDX_INFO].setVisibility( View.VISIBLE );
      for ( int k=2; k<mNrButton1; ++k ) mButton1[k].setVisibility( (k == IDX_MEMORY )? View.VISIBLE : View.GONE );
    } else {
      for ( int k=1; k<mNrButton1; ++k ) mButton1[k].setVisibility( View.GONE );
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
    //   TDToast.makeBad( R.string.connect_failed );
    //   return;
    // }

    int k = 0;
    if ( k < mNrButton1 && b == mButton1[k++] ) {         // RESET COMM STATE [This is fast]
      mApp.resetComm();
      // setState( false ); // not necessary - moved to the end
      TDToast.make( R.string.bt_reset );
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {    // INFO TDLevel.overNormal
      if ( currDeviceA() == null ) {
        TDToast.makeBad( R.string.no_device_address );
      } else {
        // setTitleColor( TDColor.CONNECTED ); // USELESS
        if ( currDeviceA().mType == Device.DISTO_A3 ) {
          new DeviceA3InfoDialog( this, this, currDeviceA() ).show();
        } else if ( currDeviceA().mType == Device.DISTO_X310 ) {
          // currDeviceA().dump();
          new DeviceX310InfoDialog( this, this, currDeviceA() ).show();
        } else if ( currDeviceA().mType == Device.DISTO_XBLE ) { // SIWEI_TIAN
          // currDeviceA().dump();
          DistoXBLEInfoDialog info =  new DistoXBLEInfoDialog( this, this, currDeviceA(), mApp );
          info.show();
          (new DistoXBLEInfoReadTask( mApp, info )).execute();
        } else if ( currDeviceA().mType == Device.DISTO_BRIC4 || currDeviceA().mType == Device.DISTO_BRIC5 ) {
          BricInfoDialog info = new BricInfoDialog( this, this, getResources(), currDeviceA() );
          info.show();
          (new InfoReadBricTask( mApp, info )).execute();
        } else if ( currDeviceA().mType == Device.DISTO_SAP5 || currDeviceA().mType == Device.DISTO_SAP6 ) {
          TDToast.makeBad( R.string.unsupported_device_type );
        } else {
          // TDLog.Error( "Unknown device type " + currDeviceA().mType );
          TDToast.makeBad( String.format(Locale.US, getResources().getString( R.string.unknown_device_type ), currDeviceA().mType ) );
        }
        // setTitleColor( TDColor.TITLE_NORMAL );
      }

    } else if ( k < mNrButton1 &&  b == mButton1[k++] ) { // CALIBRATION MODE TOGGLE
      if ( currDeviceA() == null ) { 
        TDToast.makeBad( R.string.no_device_address );
      } else {
        enableButtons( false );
        new CalibToggleTask( this, mApp ).execute();
      }
    } else if ( k < mNrButton1 &&  b == mButton1[k++] ) { // CALIBRATIONS
      if ( TDInstance.getDeviceA() == null ) {
        TDToast.makeBad( R.string.no_device_address );
      } else {
        (new CalibListDialog( this, this /*, mApp */ )).show();
      }
    } else if ( k < mNrButton1 && b == mButton1[k++] ) {   // CALIB_READ TDLevel.overNormal
      if ( currDeviceA() == null ) { 
        TDToast.makeBad( R.string.no_device_address );
      } else {
        enableButtons( false );
        new CalibReadTask( this, mApp, CalibReadTask.PARENT_DEVICE ).execute();
      }

    } else if ( k < mNrButton1 &&  b == mButton1[k++] ) { // DISTOX MEMORY TDLevel.overAdvanced
      if ( currDeviceA() == null ) {
        TDToast.makeBad( R.string.no_device_address );
      } else {
        if ( currDeviceA().mType == Device.DISTO_A3 ) {
          new DeviceA3MemoryDialog( this, this ).show();
        } else if ( currDeviceA().mType == Device.DISTO_X310 ) {
          new DeviceX310MemoryDialog( this, this ).show();
        } else if ( currDeviceA().mType == Device.DISTO_BRIC4 || currDeviceA().mType == Device.DISTO_BRIC5 ) {
          (new BricMemoryDialog( this, this, getResources() ) ).show();
        } else if ( currDeviceA().mType == Device.DISTO_XBLE ) { // SIWEI
          new DistoXBLEMemoryDialog(this,this).show();
        } else if ( currDeviceA().mType == Device.DISTO_SAP5 || currDeviceA().mType == Device.DISTO_SAP6 ) {
          TDToast.makeBad( R.string.unsupported_device_type );
        } else {
          TDToast.makeBad( String.format(Locale.US, getResources().getString( R.string.unknown_device_type ), currDeviceA().mType ) );
        }
      }
    }
    setState( false );
  }

  /** add a device that has been manually entered
   * @param address   device address
   * @param bt_name   device BT name (model-number)
   * @param nickname  device nickname (can be null)
   */ 
  public void addDevice( String address, String bt_name, String nickname )
  {
    TDLog.v( "add device: " + address + " " + bt_name + " " + nickname );
    Device device = mApp_mDData.getDevice( address );
    if ( device != null ) {
      TDToast.makeWarn( R.string.device_already_present );
      return;
    }
    mApp_mDData.insertDevice( address, bt_name, Device.btnameToName( bt_name ), nickname );
    updateList( false );
  }

  /** set of BRIC4 datetime
   * @param yy   year [YYYY]
   * @param mm   month [1..12]
   * @param dd   day of the month [1..31]
   * @param HH   hour [0..23]
   * @param MM   minute [0..59]
   * @param SS   second [0..59]
   */
  public void doBricMemoryReset( int yy, int mm, int dd, int HH, int MM, int SS )
  {
    // TDLog.v( "Device activity - BRIC memory reset " + yy + " " + mm + " " + dd + " " + HH + " " + MM + " " + SS );
    new MemoryBricTask( mApp, yy, mm, dd, HH, MM, SS  ).execute();
  }

  /** clear BRIC4 memory
   */
  public void doBricMemoryClear()
  {
    // TDLog.v( "Device activity - BRIC memory clear ");
    TopoDroidAlertDialog.makeAlert( this, getResources(), getResources().getString(R.string.bric_ask_memory_clear),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          new MemoryBricTask( mApp ).execute();
        }
      }
    );
  }

  @Override
  public void onStart()
  {
    super.onStart();
    TDLocale.resetTheLocale();
    setMenuAdapter( getResources() );
    closeMenu();
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    // TDLog.Debug("device activity on resume" );
    registerReceiver( mPairReceiver, new IntentFilter( DeviceUtil.ACTION_BOND_STATE_CHANGED ) );
    if ( mApp == null ) mApp = (TopoDroidApp) getApplication();
    if ( mApp_mDData == null ) mApp_mDData = TopoDroidApp.mDData;
    mApp.resumeComm();
    mDeviceActivityVisible = true;
    // mCurrDevice  = TDInstance.getDeviceA();
    // mCurrDeviceB = TDInstance.getDeviceB();
    setState( true );
    // TDLog.Debug("device activity on resume done" );
  }

  @Override
  public void onPause()
  {
    super.onPause();
    mDeviceActivityVisible = false;
    unregisterReceiver( mPairReceiver );
  }

  // -----------------------------------------------------------------------------

  /** clear A3 memory
   */
  public void doClearA3Memory()
  {
    int[] ht = new int[2]; // ht[0] (head) is ahead of ht[1] (tail)
    if ( ! readDeviceHeadTail( DeviceA3Details.HeadTail, ht ) ) return;
    // TDLog.v("head " + ht[0] + " tail " +  ht[1] );
    doResetA3DeviceHeadTail( ht, false ); // false: clear hot bit
  }
    

  /** read a block of DistoX A3 memory
   * @param command ...
   * @param head_tail  memory block bounds
   * @return true on success
   * @note called only by DeviceA3MemoryDialog
   */
  public boolean readDeviceHeadTail( byte[] command, int[] head_tail )
  {
    // TDLog.Log( TDLog.LOG_DEVICE, "onClick mBtnHeadTail. Is connected " + mApp.isConnected() );
    if ( mApp.readA3HeadTail( currDeviceA().getAddress(), command, head_tail ) == null ) {
      TDToast.makeBad( R.string.head_tail_failed );
      return false;
    }
    return true;
  }

  /** check a block of DistoX A3 memory
   * @param ht  memory block bounds [addresses]
   * @return true on success
   * @note when is this used ?
   */
  private boolean checkA3headtail( int[] ht )
  {
    if ( ! DeviceA3Details.checkHeadTail( ht ) ) {
      TDToast.makeBad(R.string.device_illegal_addr );
      return false;
    }
    return true;
  }

  /** reset data from stored-tail (inclusive) to current-tail (exclusive)
   * @param head_tail  memory block bounds [addresses]
   * @param on_off true: set, false: clear
   */
  private void doResetA3DeviceHeadTail( int[] head_tail, boolean on_off )
  {
    int from = head_tail[1]; // tail
    int to   = head_tail[0]; // head
    if ( ! checkA3headtail( head_tail ) ) return;
    // TDLog.v("do reset from " + from + " to " + to );
    int n = mApp.swapA3HotBit( currDeviceA().getAddress(), from, to, on_off );
    // ( new SwapHotBitTask( mApp, Device.DISTO_A3, currDeviceA().getAddress(), head_tail, on_off ) ).execute();
    if ( n < 0 ) {
      TDLog.e("failed reset A3 device HeadTail");
    }
  }

  /** store the memory bounds in the database
   * @param head_tail  memory block bounds
   * @note A3 head_tail are addresses, X310 indices
   */
  public void storeDeviceHeadTail( int[] head_tail )
  {
    // TDLog.v("store HeadTail " + currDeviceA().getAddress() + " : " + head_tail[0] + " " + head_tail[1] );
    if ( ! mApp_mDData.updateDeviceHeadTail( currDeviceA().getAddress(), head_tail ) ) {
      TDToast.makeBad( R.string.head_tail_store_failed );
    }
  }

  /** retrieve the memory bounds from the database
   * @param head_tail  memory block bounds
   */
  public void retrieveDeviceHeadTail( int[] head_tail )
  {
    // TDLog.v("store Head Tail " + currDeviceA().getAddress() + " : " + head_tail[0] + " " + head_tail[1] );
    mApp_mDData.getDeviceHeadTail( currDeviceA().getAddress(), head_tail );
  }

  public void readX310Info( DeviceX310InfoDialog dialog )
  {
    ( new InfoReadX310Task( mApp, dialog, currDeviceA().getAddress() ) ).execute();
  }

  public void readA3Info( DeviceA3InfoDialog dialog )
  {
    ( new InfoReadA3Task( mApp, dialog, currDeviceA().getAddress() ) ).execute();
  }

  /** read X310 memory
   * @param dialog     memory display dialog
   * @param head_tail  memory block bounds [indices]
   * @param dumpfile   filename to dump
   */
  public void readX310Memory( IMemoryDialog dialog, int[] head_tail, String dumpfile )
  {
    ( new MemoryReadTask( mApp, dialog, Device.DISTO_X310, currDeviceA().getAddress(), head_tail, dumpfile ) ).execute();
  }

  /** read XBLE memory
   * @param dialog     memory display dialog
   * @param head_tail  memory block bounds [indices]
   * @param dumpfile   filename to dump
   */
  public void readXBLEMemory( IMemoryDialog dialog, int[] head_tail, String dumpfile )
  {
    ( new MemoryReadTask( mApp, dialog, Device.DISTO_XBLE, currDeviceA().getAddress(), head_tail, dumpfile ) ).execute();
  }
 
  /** read A3 memory
   * @param dialog     memory display dialog
   * @param head_tail  memory block bounds [addresses]
   * @param dumpfile   filename to dump
   */
  public void readA3Memory( IMemoryDialog dialog, int[] head_tail, String dumpfile )
  {
    if ( checkA3headtail( head_tail ) ) {
      ( new MemoryReadTask( mApp, dialog, Device.DISTO_A3, currDeviceA().getAddress(), head_tail, dumpfile ) ).execute();
    }
  }

  // X310 data memory is read-only
  // void resetX310DeviceHeadTail( final int[] head_tail )
  // {
  //   int n = mApp.resetX310Memory( currDeviceA().getAddress(), head_tail[0], head_tail[1] );
  //   TDToast.make("X310 memory reset " + n + " data" ); // FIXME string
  // }

  /** reset device from stored-tail to given tail
   * @param head_tail  memory block bounds
   * @note called only by DeviceA3MemoryDialog
   */
  public void resetA3DeviceHeadTail( final int[] head_tail )
  {
    // TDLog.v("reset device from " + head_tail[0] + " to " + head_tail[1] );
    if ( checkA3headtail( head_tail ) ) {
      // TODO ask confirm
      TopoDroidAlertDialog.makeAlert( this, getResources(), getResources().getString( R.string.device_reset ),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doResetA3DeviceHeadTail( head_tail, true ); // true: set hot bit
          }
        }
      );
    }
  }


  // -----------------------------------------------------------------------------

  // UNUSED
  // public void addBleDevice( BluetoothDevice device ) // TODO BLEX
  // {
  //   if ( device == null ) return;
  //   String address = device.getAddress();
  //   String name    = null;
  //   try {
  //     name = device.getName();
  //   } catch( SecurityException e ) {
  //     TDLog.e("SECURITY " + e.getMessage() );
  //   }
  //   // if ( currDeviceA() == null || ! address.equals( currDeviceA().getAddress() ) ) { // N.B. address != null
  //     mApp.disconnectRemoteDevice( true ); // new DataStopTask( mApp, null, null );
  //     mApp.setDevicePrimary( address, null, name, device );
  //     // mCurrDevice = TDInstance.getDeviceA();
  //     // showDistoXButtons();
  //     setState( true );
  //   // }
  //   updateList( true );
  //   // TDLog.v("BLE " + "Device Activity: add ble device " + currDeviceA().mName + "/" + currDeviceA().getAddress() + "/" + currDeviceA().mModel );
  // }

  // public void onActivityResult( int request, int result, Intent intent ) 
  // {
  //   // TDLog.v( "on Activity Result: req. " + request + " res. " + result );
  //   Bundle extras = (intent != null)? intent.getExtras() : null;
  //   if ( extras == null ) return;
  //   switch ( request ) {
  //     case TDRequest.REQUEST_DEVICE:
  //       if ( result == RESULT_OK ) {
  //         String address = extras.getString( TDTag.TOPODROID_DEVICE_ACTION );
  //         // TDLog.Log(TDLog.LOG_DISTOX, "OK " + address );
  //         if ( address == null ) {
  //           TDLog.e( "onActivityResult REQUEST DEVICE: null address");
  //         } else if ( currDeviceA() == null || ! address.equals( currDeviceA().getAddress() ) ) { // N.B. address != null
  //           mApp.disconnectRemoteDevice( true ); // new DataStopTask( mApp, null, null );
  //           mApp.setDevicePrimary( address, null, null, null );
  //           DeviceUtil.checkPairing( address );
  //           // mCurrDevice = TDInstance.getDeviceA();
  //           showDistoXButtons();
  //           setState( true );
  //         }
  //       } else if ( result == RESULT_CANCELED ) {
  //         TDLog.Error( "CANCELED");
  //         // finish(); // back to survey
  //       }
  //       mHasNonameDevice = updateNonameList();
  //       updateList( true );
  //       break;
  //     // case TDRequest.REQUEST_ENABLE_BT:
  //     //   if ( result == Activity.RESULT_OK ) {
  //     //     // nothing to do: scanBTDevices(); is called by menu CONNECT
  //     //   } else {
  //     //     TDToast.makeBad(R.string.not_enabled );
  //     //     finish();
  //     //   }
  //     //   break;
  //   }
  // }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      case KeyEvent.KEYCODE_MENU:   // HARDWARE MENU (82)
        UserManualActivity.showHelpPage( this, getResources().getString( HELP_PAGE ) );
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  // ---------------------------------------------------------

  /** create the menu list
   * @param res   app resources
   */
  private void setMenuAdapter( Resources res )
  {
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<>(this, R.layout.menu );

    int k = -1;
    // ++k; if ( TDLevel.overBasic    ) menu_adapter.add( res.getString( menus[k] ) );         // BT_SCAN
    // ++k; if ( TDLevel.overExpert && mHasBLE ) menu_adapter.add( res.getString( menus[k] ) ); // FIXME_SCAN_BRIC BLE_SCAN
    // ++k; if ( TDLevel.overBasic    ) menu_adapter.add( res.getString( menus[k] ) );
    ++k; if ( TDLevel.overNormal   ) menu_adapter.add( res.getString( menus[k] ) );
    ++k; if ( TDLevel.overAdvanced ) menu_adapter.add( res.getString( menus[k] ) );
    ++k; if ( TDSetting.mUnnamedDevice ) menu_adapter.add( res.getString( menus[k] ) ); // BT_NONAME
    ++k; if ( TDLevel.overExpert && TDSetting.mPacketLog ) menu_adapter.add( res.getString( menus[k] ) ); // PACKET_LOG
    ++k; menu_adapter.add( res.getString( menus[k] ) );
    ++k; menu_adapter.add( res.getString( menus[k] ) );
    // ++k; if ( TDLevel.overTester ) menu_adapter.add( res.getString( menus[k] ) ); // CALIB_RESET
    mMenu.setAdapter( menu_adapter );
    mMenu.invalidate();
  }

  /** hide menu list
   */
  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  /** handle a tap on a menu
   * @param pos  index of the tapped menu
   */
  private void handleMenu( int pos )
  {
    closeMenu();
    int p = 0;
    // if ( TDLevel.overBasic && p++ == pos ) { // BT_SCAN
    //   Intent scanIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DeviceList.class );
    //   scanIntent.putExtra( TDTag.TOPODROID_DEVICE_ACTION, DeviceList.DEVICE_SCAN );
    //   startActivityForResult( scanIntent, TDRequest.REQUEST_DEVICE );
    //   TDToast.makeLong(R.string.wait_scan );

    // } else if ( TDLevel.overExpert && mHasBLE && p++ == pos ) { // FIXME_SCAN_BRIC BLE_SCAN
    //   BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    //   (new BleScanDialog( this, this, adapter, null )).show();
    // } else if ( TDLevel.overBasic && p++ == pos ) { // PAIR
    //   pairDevice();
    // } else 
    if ( TDLevel.overNormal && p++ == pos ) { // DETACH
      detachDevice();

    } else if ( TDLevel.overAdvanced && p++ == pos ) { // FIRMWARE
      if ( TDInstance.deviceType() == Device.DISTO_X310 ) {
        // if ( TDSetting.mCommType != 0 ) {
        //   TDToast.makeLong( "Connection mode must be \"on-demand\"" ); // FIXME string
        // } else {
          mApp.resetComm();
          (new FirmwareDialog( this, this, getResources(), mApp )).show();
        // }
      } else if ( TDInstance.deviceType() == Device.DISTO_XBLE ) { // SIWEI TIAN
        mApp.resetComm();
        // (new XBLEFirmwareDialog( this, getResources(), mApp )).show();
        (new FirmwareDialog( this, this, getResources(), mApp )).show();
      } else {
        TDToast.makeLong( R.string.firmware_not_supported );
      }
    } else if ( TDSetting.mUnnamedDevice && p++ == pos ) { // MANUALLY ADD UNNAMED // BT_NONAME
      if ( mHasNonameDevice ) {
        (new DeviceAddDialog( this, this, mNonameList )).show();
      } else {
        TDToast.make( R.string.device_all_named );
      }
    } else if ( TDLevel.overExpert && TDSetting.mPacketLog && p++ == pos ) { // PACKET_LOG
      (new PacketDialog( this )).show();

    } else if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( this, com.topodroid.prefs.TDPrefActivity.class );
      intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_DEVICE );
      startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
    // } else if ( TDLevel.overTester && p++ == pos ) { // CALIB_RESET
    //   doCalibReset();
    }
  }

  /** ask confirm for a calibration reset
   * @param b ???
   */
  public void askCalibReset( final Button b )
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

  /** perform a calibration reset
   * @param b ???
   */
  private void doCalibReset( Button b )
  {
    // TDLog.v( "CALIB RESET");
    if ( currDeviceA() != null ) {
      long one = Math.round( TDUtil.FM );
      // if (one > TDUtil.ZERO ) one = TDUtil.NEG - one;
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
      mApp.uploadCalibCoeff( coeff, false, b );
    }
  }

  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    String item_str = mArrayAdapter.getItem(pos); // "model name addr"
    if ( item_str == null || item_str.equals("X000") ) return true;
    String[] vals = item_str.split(" ", 3);
    String address = vals[2]; // address or nickname
    Device device = mApp_mDData.getDevice( address );
    if ( device != null ) {
      (new DeviceNameDialog( this, this, device )).show();
    }
    return true;
  }
    
  /** set secondary device
   * @param address  BT address of secondary device
   */
  void setSecondDevice( String address )
  {  
    if ( currDeviceB() == null || ! address.equals( currDeviceB().getAddress() ) ) {
      mApp.setDeviceB( address );
      // mCurrDeviceB = TDInstance.getDeviceB();
      mApp.disconnectRemoteDevice( true ); // new DataStopTask( mApp, null, null );
      setState( false );
    }
  }

  /** open a calibration
   * @param name   calibration name
   */
  public void openCalibration( String name )
  {
    int mustOpen = 0;
    mApp.setCalibFromName( name );
    Intent calibIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, CalibActivity.class );
    calibIntent.putExtra( TDTag.TOPODROID_SURVEY, mustOpen ); // FIXME not handled yet
    startActivity( calibIntent );
  }

  /** open the calibration-import dialog
   */
  public void openCalibrationImportDialog()
  {
    if ( currDeviceA() != null ) {
      (new CalibImportDialog( this, this )).show();
    }
  }

  /** import a calibration file
   * @param name   calibration file
   */
  public void importCalibFile( String name )
  {
    // String filename = TDPath.getCCsvFile( name );
    // File file = TDFile.getFile( filename );
    File file = TDPath.getCcsvFile( name ); // PRIVATE FILE
    if ( ! file.exists() ) {
      TDToast.makeBad(R.string.file_not_found );
    } else {
      // FIXME_SYNC this is sync ... ok because calib file is small
      switch ( CalibExport.importCalibFromCsv( mApp_mDData, file, currDeviceA().getAddress() ) ) {
        case 0:
          TDToast.make(R.string.import_calib_ok );
          break;
        case -1:
          TDToast.makeBad(R.string.import_calib_no_header );
          break;
        case -2:
          TDToast.makeBad(R.string.import_calib_already );
          break;
        case -3:
          TDToast.makeBad(R.string.import_calib_mismatch );
          break;
        case -4:
          TDToast.makeBad(R.string.import_calib_no_data );
          break;
        case -5:
          TDToast.makeBad(R.string.import_calib_io_error );
          break;
        default:
          TDToast.makeBad(R.string.import_failed );
      }
    }
  }

  // from ScanBLEDialog
  public void setBLEDevice( BluetoothDevice bt_device )
  {
    // TDLog.v("BLE " + "Device Activity: TODO set bluetooth LE device");
    TDToast.make( "TODO set bluetooth LE device not implemented" );
    // set bt_device as current
  }

  /** react to a change in the configuration
   * @param new_cfg   new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration new_cfg )
  {
    super.onConfigurationChanged( new_cfg );
    TDLocale.resetTheLocale();
  }

  /** open a progress dialog to ask whether to upload the firmware
   * @param filename   filename
   * @param msg        dialog message
   */
  public void askFirmwareUpload( String filename, String msg )
  {
    File file = TDPath.getBinFile( filename );
    long len = file.length();
    TDProgress progress = new TDProgress( this, mApp, filename, len, msg, TDProgress.PROGRESS_UPLOAD );
    // progress.setText( msg );
    progress.show();
  }

  /** open a progress dialog to ask whether to download the firmware
   * @param filename   filename
   * @param msg        dialog message
   */
  public void askFirmwareDownload( String filename, String msg )
  {
    TDProgress progress = new TDProgress( this, mApp, filename, 16384, msg, TDProgress.PROGRESS_DOWNLOAD ); // 16384 estimated firmware length
    // progress.setText( msg );
    progress.show();
  }

}

