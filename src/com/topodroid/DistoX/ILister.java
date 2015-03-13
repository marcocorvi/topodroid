/* @file ILister.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid lister interface
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

public interface ILister 
{
  /**
   * @param nr    number of new shots
   * @param toast whether to toast feedbacks
   */
  public void refreshDisplay( int nr, boolean toast );

  public void updateBlockList( DistoXDBlock blk );

  public void setConnectionStatus( boolean connected );

  public void notifyDisconnected();

}

