/* @file ILister.java
 *
 * @author marco corvi
 * @date dec 2011
 *
 * @brief TopoDroid lister interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

public interface ILister 
{
  /**
   * @param nr    number of new shots
   * @param toast whether to toast feedbacks
   * 
   * refreshDisplay is called by
   *   - DataDownloadTask.onPostExecute
   *   - ShotNewDialog.onClick
   *   - ListerHandler.handleMessage Lister.LIST_REFRESH
   */
  void refreshDisplay( int nr, boolean toast );

  /** 
   * @param blk_id id (of the data-block) from which to update the list
   *
   *  updateBlockList is called by
   *   - ListerHandler.handleMessage Lister.BLOCK_ID
   */
  void updateBlockList( long blk_id );

  /**
   * @param status   current status ( 0: off,  1: on,  2: wait )A
   *
   * called by
   *   - ListerHandler.handleMessage Lister.STATE
   *   - TopoDroidApp.notifyStatus() which is called by
   *      - DataDownloader.notifyConnectionStatus()
   *      - DistoXComm.onReceive()
   * 
   * effects:
   * DrawingWindow: change button[0] background 
   * GMActivity: nothing
   * PhotoActivity: nothing
   * ShotWindow: change button[0] background
   * SketchWindow: nothing (TODO)
   */
  void setConnectionStatus( int status ); 
 
  // void notifyDisconnected();

  /** called by 
   *   - AzimuthDialog
   *   - ListerHandler.handleMessage Lister.REF_AZIMUTH <-- not necessary
   * 
   * implemented
   * DrawingWindow
   * GMActivity: nothing
   * PhotoActivity: nothing
   * ShotWindow
   * SketchWindow: TODO
   */
  void setRefAzimuth( float azimuth, long fixed_extend );

  /** set the title
   *
   */
  void setTheTitle();

  /** enable/disable theBluetooth button
   * @param enable whether to enable or disable
   */
  void enableBluetoothButton( boolean enable );

}

