/** @file Device.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX device object
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 201311   added model and type info
 */
package com.topodroid.DistoX;

import android.util.Log;

class Device
{
  String mAddress; // device mac address
  String mModel;   // device model (type string)
  String mName;    // device name (X310 only)
  int mType;       // device type
  int mHead;
  int mTail;

  final static int DISTO_NONE = 0;
  final static int DISTO_A3   = 1;
  final static int DISTO_X310 = 2;
  final static String[] typeString = { "Unknown", "A3", "X310" };
  
  static String typeToString( int type ) { return typeString[ type ]; }

  static String modelToName( String model )
  {
    if ( model.startsWith("DistoX-") ) {
      return model.replace("DistoX-", "" );
    }
    return "-";
  }

  static int  stringToType( String model ) 
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "stringToType " + model );
    if ( model.equals( "X310" ) || model.startsWith( "DistoX-" ) ) return DISTO_X310;
    if ( model.equals( "A3" ) || model.equals( "DistoX" ) ) return DISTO_A3;
    return DISTO_NONE;
  }

  Device( String addr, String model, int h, int t, String name )
  {
    mAddress = addr;
    mModel = model;
    mType = stringToType( model );
    mName = ( name == null )? "-" : name;
    if ( mName.equals("null") ) mName = "-";
    mHead = h;
    mTail = t;
  }

  Device( String addr, String model, String name )
  {
    mAddress = addr;
    mModel = model;
    mType = stringToType( model );
    mName = ( name == null )? "-" : name;
    if ( mName.equals("null") ) mName = "-";
    mHead = 0;
    mTail = 0;
  }

  public String toString() { return typeString[ mType ] + " " + mName + " " + mAddress; }
  
  
}
