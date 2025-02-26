/* @file Device.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid bluetooth device object
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev;

// import androidx.annotation.RecentlyNonNull;

// import com.topodroid.utils.TDLog;

// SIWEI_TIAN changed on Jun 2022
public class Device
{
  /** Bluetooth advertised name prefixes
   * The device BT model strings are defined only here
   *
   * These strings (without the underscore/dash) can appear also in log statements
   */
  // public final static String NAME_DISTOX_0  = "DistoX0";
  public final static String NAME_DISTOX1   = "DistoX";     // all DistoX1 are advertised as "DistoX" without device code
  public final static String NAME_DISTOX2   = "DistoX-";
  public final static String NAME_DISTOXBLE = "DistoXBLE-";
  public final static String NAME_BRIC      = "BRIC-";
  public final static String NAME_BRIC4     = "BRIC4_";
  public final static String NAME_BRIC5     = "BRIC5-";
  public final static String NAME_BRIC5_2   = "BRIC5_";
  public final static String NAME_SAP5      = "Shetland_";
  public final static String NAME_SAP5_2    = "SAP5_";
  public final static String NAME_SAP6      = "SAP6_";
  public final static String NAME_SAP6_2    = "SAP_";
  public final static String NAME_CAVWAY    = "CavwayX1-";
  
  // supported models and their common names
  public final static String[] mModels = { NAME_DISTOX1, NAME_DISTOX2, NAME_DISTOXBLE,
    NAME_BRIC4, NAME_BRIC5, NAME_BRIC5_2,
    NAME_SAP5, NAME_SAP5_2, NAME_SAP6, NAME_SAP6_2,
    NAME_CAVWAY };

  public final static String[] mModelNames = { "DistoX A3", "DistoX X310", "DistoX BLE",
    "BRIC 4", "BRIC 5", "BRIC 5",
    "SAP 5",  "SAP 5",  "SAP 6",  "SAP 6",
    "Cavway X1" };

  public final static String[] mAdvertisedNames = { "DistoX A3", "DistoX X310", "DistoX BLE",
    "BRIC 4", "BRIC 5", null,
    "SAP 5",  null, "SAP 6", null,
    "Cavway X1" };

  // supported BLE models
  final static String[] mBleModels = { NAME_DISTOXBLE, NAME_BRIC4, NAME_BRIC5, NAME_BRIC5_2, NAME_SAP5, NAME_SAP5_2, NAME_SAP6, NAME_SAP6_2, NAME_CAVWAY };

  // DistoX2 / SAP6 commands
  public static final int LASER_ON         =  1; // 0x36
  public static final int LASER_OFF        =  0; // 0x37
  public static final int MEASURE          =  2; // 0x38
  public static final int MEASURE_DOWNLOAD =  3; // 0x38
  public static final int CALIB_START      = 11; // 0x31 // FIXME_SAP6
  public static final int CALIB_STOP       = 10; // 0x30
  public static final int DEVICE_OFF       = 20; // 0x34

  // FIXME VirtualDistoX
  // static final String ZERO_ADDRESS = "00-00-00-00-00-00";

  private final String mAddress; // device mac address
  public String mModel;    // device model (type string)
  public String mName;     // device name (X310 only)
  public String mNickname; // device nickname
  public  int mType;       // device type
  private int mHead;
  private int mTail;

  public final static int DISTO_NONE     =  0; // device types - used as index in the arrays below
  public final static int DISTO_A3       =  1;
  public final static int DISTO_X310     =  2;
  // public final static int DISTO_X000   =  3; // FIXME VirtualDistoX
  // public final static int DISTO_BLEX   =  4;
  public final static int DISTO_SAP5     =  5; 
  public final static int DISTO_BRIC4    =  6;
  public final static int DISTO_XBLE     =  7; // SIWEI_TIAN
  public final static int DISTO_SAP6     =  8; 
  public final static int DISTO_BRIC5    =  9;
  public final static int DISTO_CAVWAYX1 = 10;

  // SIWEI_TIAN                                       0          1         2          3          4       5       6        7            8       9        10
  final static String[] typeString               = { "Unknown", "A3",     "X310",    "X000",    "BLEX", "SAP5", "BRIC4", "XBLE",      "SAP6", "BRIC5", "CVWY1" };
  private final static String[] typeSimpleString = { "Unknown", "DistoX", "DistoX2", "DistoX0", "BleX", "Sap5", "Bric4", "DistoXBLE", "Sap6", "Bric5", "CVWY1" };
  
  public static String typeToString( int type )
  {
    if ( type < 0 || type >= typeString.length ) return null;
    return typeString[ type ];
  }

  public String getAddress() { return mAddress; }

  // check if this device has given address or nickname
  public boolean hasAddress( String addr )
  {
    return mAddress.equals( addr );
  }

  // check if this device has given address or nickname
  public boolean hasAddressOrNickname( String addr )
  {
    return mAddress.equals( addr ) || ( mNickname != null && mNickname.equals( addr ) );
  }

  public boolean isBT( )  { return mType == DISTO_X310  || mType == DISTO_A3; }
  public boolean isBLE( ) { return mType == DISTO_XBLE || mType == DISTO_BRIC4 || mType == DISTO_BRIC5 
                                || mType == DISTO_SAP5 || mType == DISTO_SAP6  || mType == DISTO_CAVWAYX1; } // SIWEI_TIAN
  public static boolean isBle( int type ) { return type == DISTO_XBLE || type == DISTO_BRIC4 || type == DISTO_BRIC5
                                               ||  type == DISTO_SAP5 || type == DISTO_SAP6  || type == DISTO_CAVWAYX1 ; } // SIWEI_TIAN

  public boolean isDistoX( )    { return mType == DISTO_X310  || mType == DISTO_A3; }
  public boolean isA3( )        { return mType == DISTO_A3; }
  public boolean isX310( )      { return mType == DISTO_X310; }
  public boolean isBric( )      { return mType == DISTO_BRIC4 || mType == DISTO_BRIC5; }
  public boolean isBric4( )     { return mType == DISTO_BRIC4; }
  public boolean isBric5( )     { return mType == DISTO_BRIC5; }
  public boolean isDistoXBLE( ) { return mType == DISTO_XBLE; } // SIWEI_TIAN
  public boolean isSap( )       { return mType == DISTO_SAP5 || mType == DISTO_SAP6; }
  public boolean isSap5( )      { return mType == DISTO_SAP5; }
  public boolean isSap6( )      { return mType == DISTO_SAP6; } // FIXME_SAP6
  public boolean isCavway()     { return mType == DISTO_CAVWAYX1; }
  public boolean isCavwayX1()   { return mType == DISTO_CAVWAYX1; }

  public static boolean isDistoX( int type )    { return type == DISTO_X310 || type == DISTO_A3; }
  public static boolean isA3( int type )        { return type == DISTO_A3; }
  public static boolean isX310( int type )      { return type == DISTO_X310; }
  public static boolean isBric( int type )      { return type == DISTO_BRIC4 || type == DISTO_BRIC5; }
  public static boolean isBric4( int type )     { return type == DISTO_BRIC4; }
  public static boolean isBric5( int type )     { return type == DISTO_BRIC5; }
  public static boolean isDistoXBLE( int type ) { return type == DISTO_XBLE; } // SIWEI_TIAN
  public static boolean isSap( int type )       { return type == DISTO_SAP5 || type == DISTO_SAP6; }
  public static boolean isSap5( int type )      { return type == DISTO_SAP5; }
  public static boolean isSap6( int type )      { return type == DISTO_SAP6; } // FIXME_SAP6
  public static boolean isCavway( int type )    { return type == DISTO_CAVWAYX1; }
  public static boolean isCavwayX1( int type )  { return type == DISTO_CAVWAYX1; }

  public static boolean isDistoX( String bt_name ) { return bt_name.startsWith("DistoX"); }
  public static boolean isSap( String bt_name )    { return bt_name.startsWith("Shetland") || bt_name.startsWith("SAP"); }
  public static boolean isBric( String bt_name )   { return bt_name.startsWith("BRIC"); }
  public static boolean isCavway( String bt_name ) { return bt_name.startsWith("Cavway"); }

  // SIWEI_TIAN
  // public boolean canSendCommand() { return mType == DISTO_X310 mType == DISTO_XBLE || mType == DISTO_BRIC4 || mType == DISTO_BRIC5 || mType == DISTO_SAP6; }
  // public static boolean canSendCommand( int type ) { return type == DISTO_X310 || type == DISTO_XBLE type == DISTO_BRIC4 || type == DISTO_BRIC5 || type == DISTO_SAP6; }


  /** @return the list of the supported BLE models
   */
  public static String[] getBleModels() { return mBleModels; }

  /** @return the device name given the BT name 
   * @param bt_name   BT name
   * @note used by DeviceActivity
   */
  public static String btnameToName( String bt_name )
  {
    // TDLog.v("DEVICE model to name <" + bt_name + ">");
    if ( bt_name.startsWith( NAME_DISTOX2 ) ) return bt_name.replace( NAME_DISTOX2, "" );
    if ( bt_name.startsWith( NAME_DISTOXBLE ) ) return bt_name.replace( NAME_DISTOXBLE , "" );
    if ( bt_name.startsWith( NAME_CAVWAY ) )    return bt_name.replace( NAME_CAVWAY, "" );
    // NAME_DISTOX1 left unchnaged
    if ( bt_name.startsWith( NAME_BRIC4 ) )   return bt_name.replace( NAME_BRIC4, "" );
    if ( bt_name.startsWith( NAME_BRIC5 ) )   return bt_name.replace( NAME_BRIC5, "" );
    if ( bt_name.startsWith( NAME_BRIC5_2 ) ) return bt_name.replace( NAME_BRIC5_2, "" );
    if ( bt_name.startsWith( NAME_SAP5 ) )    return bt_name.replace( NAME_SAP5, "" );
    if ( bt_name.startsWith( NAME_SAP5_2 ) )  return bt_name.replace( NAME_SAP5_2, "" );
    if ( bt_name.startsWith( NAME_SAP6 ) )    return bt_name.replace( NAME_SAP6, "" );
    if ( bt_name.startsWith( NAME_SAP6_2 ) )  return bt_name.replace( NAME_SAP6_2, "" );
    // if ( bt_name.startsWith("Ble-") ) { // FIXME BLE_5
    //   return bt_name.replace("Ble-", "" );
    // }
    return "--";
  }

  /** @return the integer code given the BT name
   * @param bt_name   BT name (model plus code)
   * @note try to order by number of devices in usage
   *    A3     about  600, but it is very old
   *    X310   maybe 3000
   *    XBLE   about  600
   *    BRIC4  about  500, superceded by BRIC5
   *    BRIC5  ?
   *    SAP5   very few, maybe a dozen
   *    SAP6   some, but still not much, probably a couple of dozens
   */
  private static int btnameToType( String bt_name ) 
  {
    if ( bt_name != null ) {
      // TDLog.v( "DEVICE btnameToType " + bt_name );
      if ( bt_name.equals( "X310" )  || bt_name.startsWith( NAME_DISTOX2 ) )   return DISTO_X310;
      if ( bt_name.equals( "XBLE" )  || bt_name.startsWith( NAME_DISTOXBLE ) ) return DISTO_XBLE; // SIWEI_TIAN
      if ( bt_name.equals( "BRIC5" ) || bt_name.startsWith( NAME_BRIC5 ) || bt_name.startsWith( NAME_BRIC5_2 ) ) return DISTO_BRIC5;
      if ( bt_name.equals( "BRIC4" ) || bt_name.startsWith( NAME_BRIC4 ) )     return DISTO_BRIC4; 
      if ( bt_name.equals( "SAP6" )  || bt_name.startsWith( NAME_SAP6 )  || bt_name.startsWith( NAME_SAP6_2 ) )  return DISTO_SAP6;
      if ( bt_name.equals( "SAP5" )  || bt_name.startsWith( NAME_SAP5 )  || bt_name.startsWith( NAME_SAP5_2 ) )  return DISTO_SAP5;
      if ( bt_name.equals( "A3" )    || bt_name.equals( NAME_DISTOX1 ) )       return DISTO_A3;
      if ( bt_name.startsWith( NAME_CAVWAY ) ) return DISTO_CAVWAYX1;
      // if ( bt_name.equals( "BLEX" ) ) return DISTO_BLEX; // FIXME BLE_5
      // if ( bt_name.equals( "X000" ) || bt_name.equals( "DistoX0" ) ) return DISTO_X000; // FIXME VirtualDistoX
    }
    return DISTO_NONE;
  }

  // /** @return the string presentation of the device type
  //  * @param bt_name   bt_name string
  //  */
  // private static String btnameToString( String bt_name ) 
  // {
  //   return typeString[ btnameToType( bt_name ) ];
  // }

  // -------------------------------------------------------------------------------

  /** cstr
   * @param addr     bluetooth address
   * @param bt_name    model string
   * @param h        head (only for A3) 
   * @param t        tail (only for A3)
   * @param name     ???
   * @param nickname device nickname (can be null)
   */
  public Device( String addr, String bt_name, int h, int t, String name, String nickname )
  {
    // TDLog.v( "[1] Device: " + addr + " " + bt_name + " " + name + " addr " + addr );
    mAddress = addr;
    mModel = bt_name;
    mType = btnameToType( bt_name );
    mName = fromName( name );
    mNickname = nickname;
    mHead = h;
    mTail = t;
  }

  /** cstr
   * @param addr     bluetooth address
   * @param bt_name    BT name
   * @param name     ???
   * @param nickname device nickname (can be null)
   */
  public Device( String addr, String bt_name, String name, String nickname )
  {
    // TDLog.v( "[2] Device: " + addr + " " + bt_name + " " + name + " addr " + addr );
    mAddress = addr;
    mModel = bt_name;
    mType = btnameToType( bt_name );
    mName = fromName( name );
    mNickname = nickname;
    mHead = 0;
    mTail = 0;
  }

  // public void dump( )
  // {
  //   TDLog.v( "Device addr " + mAddress + " BT anme " + mModel + " type " + mType + " name " + mName + " nick " + ((mNickname == null)? "null" : mNickname ) );
  // }

  private String fromName( String name )
  {
    // TDLog.v( "DEVICE from name <" + name + "> model <" + mModel + ">" );
    if ( name != null ) name = name.trim();
    if ( name == null || name.length() == 0 || name.equals("null") ){
      name = mModel;
    }
    if ( name.startsWith( NAME_DISTOXBLE ) ) return name.replace( NAME_DISTOXBLE, ""); // SIWEI_TIAN
    if ( name.startsWith( NAME_DISTOX2 ) )   return name.replace( NAME_DISTOX2, "");
    if ( name.startsWith( NAME_SAP6 ) )      return name.replace( NAME_SAP6, ""); // FIXME_SAP6
    if ( name.startsWith( NAME_SAP6_2 ) )    return name.replace( NAME_SAP6_2, "");
    if ( name.startsWith( NAME_BRIC ) )      return name.replace( NAME_BRIC, "");
    if ( name.startsWith( NAME_BRIC4 ) )     return name.replace( NAME_BRIC4, "");
    if ( name.startsWith( NAME_BRIC5 ) )     return name.replace( NAME_BRIC5, "");
    if ( name.startsWith( NAME_BRIC5_2 ) )   return name.replace( NAME_BRIC5_2, "");
    if ( name.startsWith( NAME_CAVWAY ) )    return name.replace( NAME_CAVWAY, "");
    return name;
  }

  /** @return the device nickname, if not null, or the name, otherwise
   */
  public String getNickname()
  {
    if ( mNickname != null && mNickname.length() > 0 ) return mNickname;
    return mName;
  }

  /** @return true if the device supports firmware
   *
   * @note X310 is the only device that has firmware support 
   */
  public boolean hasFirmwareSupport() { return mType == DISTO_X310 || mType == DISTO_XBLE; }

  /** @return string presentation
   */
  // @RecentlyNonNull
  public String toString()
  { 
    // TDLog.v( "to String <" + mName + "> type <" + mType + "> <" + typeString[ mType ] +">" );
    if ( mNickname != null && mNickname.length() > 0 ) {
      return typeString[ mType ] + " " + mName + " " + mNickname;
    }
    return typeString[ mType ] + " " + mName + " " + mAddress;
  }

  /** @return simple string presentation
   */
  public String toSimpleString() { return typeSimpleString[ mType ] + " " + mName; }
  
}
