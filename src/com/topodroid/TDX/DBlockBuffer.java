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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;

import android.content.Context;


import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Comparator;

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

  /** sort the data in the buffer by increasing ID.
   * This is used to reorder the DBlock's before appending them to a survey.
   */
  void sort()
  {
    if ( TDandroid.BELOW_API_24 ) { // implement sorting
      int len = mBuffer.size();
      for ( int k1 = 0; k1 < len-1; ++k1 ) {
        DBlock blk1 = mBuffer.get(k1);
        for ( int k2 = k1 + 1; k2 < len; ++ k2 ) {
          DBlock blk2 = mBuffer.get(k2);
          if ( blk1.mId > blk2.mId ) {
            mBuffer.set( k2, blk1 );
            blk1 = blk2; 
          }
        }
        mBuffer.set( k1, blk1 );
      }
    } else {
      mBuffer.sort( new Comparator<DBlock>() {
        public int compare( DBlock b1, DBlock b2 ) { return (b1.mId < b2.mId)? -1 : (b1.mId == b2.mId)? 0 : 1; }
      } );
    }
  }

}
