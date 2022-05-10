/* @file OnItemSelectedListener.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: item type selection listener
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

/**
 */
interface OnItemSelectedListener 
{
  void pointSelected( int index, boolean update_recent );
  void lineSelected( int index, boolean update_recent );
  void areaSelected( int index, boolean update_recent );
}
