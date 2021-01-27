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

import com.topodroid.utils.TDLog;

import android.util.Log;

public class Device
{
  // commands
  static final int CALIB_OFF        = 0x30;
  static final int CALIB_ON         = 0x31;
  static final int SILENT_ON        = 0x32;
  static final int SILENT_OFF       = 0x33;
  static final int DISTOX_OFF       = 0x34;
  static final int DISTOX_35        = 0x35;
  public static final int LASER_ON         =  1; // 0x36
  public static final int LASER_OFF        =  0; // 0x37
  public static final int MEASURE          =  2; // 0x38
  public static final int MEASURE_DOWNLOAD =  3; // 0x38

  // FIXME VirtualDistoX
  // static final String ZERO_ADDRESS = "00-00-00-00-00-00";

  public String mAddress;  // device mac address
  public String mModel;    // device model (type string)
  public String mName;     // device name (X310 only)
  public String mNickname;  // device nickname
  public  int mType;        // device type
  private int mHead;
  private int mTail;

  public final static int DISTO_NONE  = 0; // device types - used as index in the arrays below
  public final static int DISTO_A3    = 1;
  public final static int DISTO_X310  = 2;
  // public final static int DISTO_X000 = 3; // FIXME VirtualDistoX
  // public final static int DISTO_BLEX  = 4;
  public final static int DISTO_SAP5  = 5; 
  public final static int DISTO_BRIC4 = 6;

  final static String[] typeString = { "Unknown", "A3", "X310", "X000", "BLEX", "SAP5", "BRIC4" };
  private final static String[] typeSimpleString = { "Unknown", "DistoX", "DistoX2", "DistoX0", "BleX", "Sap5", "Bric4" };
  
  public static String typeToString( int type )
  {
    if ( type < 0 || type >= typeString.length ) return null;
    return typeString[ type ];
  }

  public boolean isBT( )  { return mType == DISTO_X310  || mType == DISTO_A3; }
  public boolean isBLE( ) { return mType == DISTO_BRIC4 || mType == DISTO_SAP5 /* || mType == DISTO_BLEX */ ; }
  public static boolean isBle( int type ) { return type == DISTO_BRIC4 || type == DISTO_SAP5 /* || mType == DISTO_BLEX */ ; }

  public boolean isDistoX( )  { return mType == DISTO_X310  || mType == DISTO_A3; }
  public boolean isA3( )      { return mType == DISTO_A3; }
  public boolean isX310( )    { return mType == DISTO_X310; }
  public boolean isSap( )     { return mType == DISTO_SAP5; }
  public boolean isBric( )    { return mType == DISTO_BRIC4; }

  public static boolean isDistoX( int type )  { return type == DISTO_X310 || type == DISTO_A3; }
  public static boolean isA3( int type )      { return type == DISTO_A3; }
  public static boolean isX310( int type )    { return type == DISTO_X310; }
  public static boolean isSap( int type )     { return type == DISTO_SAP5; }
  public static boolean isBric( int type )    { return type == DISTO_BRIC4; }

  public boolean canSendCommand() { return mType == DISTO_X310 || mType == DISTO_BRIC4; }
  public static boolean canSendCommand( int type ) { return type == DISTO_X310 || type == DISTO_BRIC4; }

  public static String modelToName( String model )
  {
    Log.v("BRIC", "model to name <" + model + ">");
    if ( model.startsWith("DistoX-") ) {
      return model.replace("DistoX-", "" );
    } 
    if ( model.startsWith("Shetland") ) {
      return model.replace("Shetland_", "SAP-" );
    }
    if ( model.startsWith("BRIC4_") ) {
      return model.replace("BRIC4_", "" );
    }
    // if ( model.startsWith("Ble-") ) { // FIXME BLE_5
    //   return model.replace("Ble-", "" );
    // }
    return "--";
  }

  public static int modelToType( String model ) 
  {
    if ( model != null ) {
      // TDLog.Log( TDLog.LOG_DEBUG, "modelToType " + model );
      if ( model.equals( "X310" )  || model.startsWith( "DistoX-" ) )   return DISTO_X310;
      if ( model.equals( "A3" )    || model.equals( "DistoX" ) )        return DISTO_A3;
      if ( model.equals( "BRIC4" ) || model.startsWith( "BRIC4" ) )     return DISTO_BRIC4; 
      if ( model.equals( "SAP5" )  || model.startsWith( "Shetland_" ) ) return DISTO_SAP5;
      // if ( model.equals( "BLEX" ) ) return DISTO_BLEX; // FIXME BLE_5
      // if ( model.equals( "X000" ) || model.equals( "DistoX0" ) ) return DISTO_X000; // FIXME VirtualDistoX
    }
    return DISTO_NONE;
  }

  public static String modelToString( String model ) 
  {
    return typeString[ modelToType( model ) ];
  }

  // -------------------------------------------------------------------------------

  // nickname can be null
  public Device( String addr, String model, int h, int t, String name, String nickname )
  {
    // Log.v("DistoX-BLEX", "device " + addr + " " + model + " " + name );
    mAddress = addr;
    mModel = model;
    mType = modelToType( model );
    mName = fromName( name );
    mNickname = nickname;
    mHead = h;
    mTail = t;
  }

  // nickname can be null
  public Device( String addr, String model, String name, String nickname )
  {
    // Log.v("DistoX-BLEX", "device " + addr + " " + model + " " + name );
    mAddress = addr;
    mModel = model;
    mType = modelToType( model );
    mName = fromName( name );
    mNickname = nickname;
    mHead = 0;
    mTail = 0;
  }

  private String fromName( String name )
  {
    if ( name != null ) name = name.trim();
    if ( name == null || name.length() == 0 || name.equals("null") ){
      name = mModel;
    }
    if ( name.startsWith("SAP5_" ) ) return name.replace("SAP5_", "");
    if ( name.startsWith("BRIC-" ) ) return name.replace("BRIC-", "");
    return name;
  }

  public String getNickname()
  {
    if ( mNickname != null && mNickname.length() > 0 ) return mNickname;
    return mName;
  }

  // X310 is the only device that has firmware support 
  public boolean hasFirmwareSupport() { return mType == DISTO_X310; }


  public String toString() 
  { 
    if ( mNickname != null && mNickname.length() > 0 ) {
      return typeString[ mType ] + " " + mName + " " + mNickname;
    }
    return typeString[ mType ] + " " + mName + " " + mAddress;
  }

  public String toSimpleString() { return typeSimpleString[ mType ] + " " + mName; }
  
}
