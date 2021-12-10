/* @file DBlockBuffer.java
 *
 * @author marco corvi
 * @date dec 2021
 *
 * @brief TopoDroid buffer of survey data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;

import android.content.Context;


import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

class DBlockBuffer
{
  private ArrayList< DBlock > mBuffer; // array of DBlock's

  /** cstr - prepare an empty data-buffer
   */
  DBlockBuffer( )
  {
    mBuffer = new ArrayList< DBlock >();
  }
 
  /** @return the number of data in the buffer
   */
  int size() 
  {
    return mBuffer.size();
  }

  /** clear the buffer
   */
  void clear()
  {
    mBuffer.clear();
  }

  /** append a data to the buffer
   * @param blk    data tio append
   */
  void add( DBlock blk ) 
  {
    mBuffer.add( blk );
  }

  /** @return the buffer (list) of data
   */ 
  List< DBlock > getBuffer()
  {
    return mBuffer;
  }

}
