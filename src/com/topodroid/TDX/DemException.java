/* @file DemException.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid DEM exception
 *
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * ----------------------------------------------------------
 */
package com.topodroid.TDX;

class DemException extends Exception
{

  /** cstr
   * @param what exception message
   */
  public DemException( String what ) 
  {
    super( what );
  }

}
