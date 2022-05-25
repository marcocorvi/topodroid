/** @file DataBuffer.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief 3D: directly allocated data buffer with memory management
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;

import java.nio.ByteOrder;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

class DataBuffer
{
  public final static int SHORT = 2;
  public final static int FLOAT = 4;

  private int mCapacity;
  private int mDelta;
  private int mPos;
  private int mType; 

  private ByteBuffer mData;

  // ===========================================================
  // static creators

  /** create a float data buffer
   * @param cap      capacity
   * @param delta    ...
   */
  static DataBuffer createFloatBuffer( int cap, int delta ) { return createDataBuffer( FLOAT, cap, delta ); }

  /** create a float data buffer
   * @param cap      capacity
   */
  static DataBuffer createFloatBuffer( int cap ) { return createDataBuffer( FLOAT, cap, cap ); }

  /** create an integer (short) data buffer
   * @param cap      capacity
   * @param delta    ...
   */
  static DataBuffer createShortBuffer( int cap, int delta ) { return createDataBuffer( SHORT, cap, delta ); }

  /** create an integer (short) data buffer
   * @param cap      capacity
   */
  static DataBuffer createShortBuffer( int cap ) { return createDataBuffer( SHORT, cap, cap ); }

  /** #return the underlying buffer as bytes
   */
  ByteBuffer asByte() { return mData; }

  /** @return the buffer as floats
   */
  FloatBuffer asFloat() { mData.rewind(); return mData.asFloatBuffer( ); }

  /** @return the buffer as shorts
   */
  ShortBuffer asShort() { mData.rewind(); return mData.asShortBuffer( ); }

  /** append a float value to the buffer
   * @param value   float value to append
   * @return the data buffer as float
   */
  FloatBuffer addFloat( float value ) 
  {
    // assert( mType == FLOAT );
    extend( mType );
    mData.putFloat( mPos, value );
    mPos += mType;
    // mData.position( mPos );
    return mData.asFloatBuffer();
  }

  /** append an array of float values to the buffer
   * @param values   float values to append
   * @return the data buffer as float
   */
  FloatBuffer addFloats( float[] values )
  {
    // assert( mType == FLOAT );
    extend( mType * values.length );
    // TDLog.v("Data Buffer add floats: cap " + mCapacity + "/" + mData.capacity() + " limit " + mData.limit() + " pos " + mPos + "/" + mData.position() + " len " + values.length );
    for ( int k=0; k<values.length; ++k ) {
      mData.putFloat( mPos, values[k] );
      mPos += mType;
    }
    // mData.position( mPos );
    return mData.asFloatBuffer();
  }

  /** append a short value to the buffer
   * @param value   short value to add
   * @return the data buffer as short
   */
  ShortBuffer addShort( short value ) 
  {
    // assert( mType == SHORT );
    extend( mType );
    mData.putShort( mPos, value );
    mPos += mType;
    // mData.position( mPos );
    return mData.asShortBuffer();
  }

  /** append an array of short values to the buffer
   * @param values   short values to add
   * @return the data buffer as short
   */
  ShortBuffer addShorts( short[] values )
  {
    // assert( mType == SHORT );
    extend( mType * values.length );
    for ( int k=0; k<values.length; ++k ) {
      mData.putShort( mPos, values[k] );
      mPos += mType;
    }
    // mData.position( mPos );
    return mData.asShortBuffer();
  }

  /** @return the number of values stored in the buffer
   */
  int size() { return mPos / mType; }

  /** @return the buffer capacity for the type of the values
   */
  int capacity() { return mCapacity / mType; }

  /** @return a float data buffer of given size
   * @param count   number of floats in the buffer
   */ 
  static FloatBuffer getFloatBuffer( int count )
  {
    try {
      DataBuffer db = new DataBuffer( FLOAT, count * 4, count * 4 ); // 4 bytes / float
      return db.mData.asFloatBuffer();
    } catch ( OutOfMemoryError e ) { }
    return null;
  }

  // ----------------------------------------------------------------

  /** create a data buffer
   * @param type  data type
   * @param cap   capacity
   * @param delta ...
   * @return the newly created data buffer
   */
  private static DataBuffer createDataBuffer( int type, int cap, int delta )
  {
    try {
      return new DataBuffer( type, type*cap, type*delta );
    } catch ( OutOfMemoryError e ) { }
    return null;
  }

  /** create a data buffer
   * @param type  data type
   * @param cap   capacity
   * @return the newly created data buffer
   */
  private static DataBuffer createDataBuffer( int type, int cap )
  {
    try {
      return new DataBuffer( type, type*cap, type*cap );
    } catch ( OutOfMemoryError e ) { }
    return null;
  }

  /** cstr
   * @param type  data type
   * @param cap     initial capacity
   * @param delta   capacity increment
   */
  private DataBuffer( int type, int cap, int delta ) 
  {
    mCapacity = cap;
    mDelta    = delta;
    mPos    = 0;
    mType     = type;
    mData = ByteBuffer.allocateDirect( mCapacity );
    mData.order( ByteOrder.nativeOrder() );
    // TDLog.v("Data Buffer cstr cap " + mCapacity + " pos " + mPos );
  }

  /** reallocate the data buffer
   */
  private void realloc()
  {
    ByteBuffer buf = mData;
    mData = ByteBuffer.allocateDirect( mCapacity );
    mData.order( ByteOrder.nativeOrder() );
    buf.rewind();
    mData.put( buf );
    // if ( mPos > 0 ) mData.put( buf.array(), 0, mPos );
    int last = mData.limit();
    for ( int k=mData.position(); k<last; ++k) mData.put( (byte)0 );
    mData.position( mPos );
    // TDLog.v("Data Buffer realloc cap " + mCapacity + "/" + mData.capacity() 
    //               + " pos " + mPos + "/" + mData.position() + " limit /" + mData.limit() );
  }

  /** increase the pos by delta
   * @param delta  amount by which pos is increased
   */
  private void extend( int delta )
  {
    if ( mPos + delta > mCapacity ) {
      // TDLog.v("Data Buffer extend pos " + mPos + " delta " + delta + " cap " + mCapacity );
      while ( mPos + delta > mCapacity ) mCapacity += mDelta;
      realloc();
    }
  }
  
}

