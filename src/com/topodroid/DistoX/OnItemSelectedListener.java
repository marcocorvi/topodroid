/* @file OnItemSelectedListener.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid drawing: item type selection listener
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

/**
 */
public interface OnItemSelectedListener 
{
  public void pointSelected( int index, boolean update_recent );
  public void lineSelected( int index, boolean update_recent );
  public void areaSelected( int index, boolean update_recent );
}
