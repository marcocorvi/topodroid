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
 */
package com.topodroid.DistoX;

public interface ILister 
{
  /**
   * @param nr    number of new shots
   * @param toast whether to toast feedbacks
   * 
   * refreshDisplay is called by
   *   - DataDownloadTask.onPostExecute
   *   - ShotNewDialog.onClick
   *   - ListerHandler.handleMessage LISTER_REFRESH
   */
  public void refreshDisplay( int nr, boolean toast );

  /** updateBlockList is called by
   *   - DistoXComm.run()
   *   + updateBlockList( long ) for each class that implements this method
   */
  public void updateBlockList( DBlock blk );

  /** updateBlockList is called by
   *   - ListerHandler.handleMessage LISTER_BLOCK_ID
   */
  public void updateBlockList( long blk_id );

  /** called by
   *   - ListerHandler.handleMessage LISTER_STATUS
   *   - TopoDroidApp.notifyStatus() which is called by
   *      - DataDownloader.notifyConnectionStatus()
   *      - DistoXComm.onReceive()
   * 
   * effects:
   * DrawingWindow: change button[0] background 
   * GMACtivity: nothing
   * PhotoActivity: nothing
   * ShotWindow: change button[0] background
   * SketchWindow: nothing (TODO)
   */
  public void setConnectionStatus( int status ); // 0 off, 1 on, 2 wait
 
  // public void notifyDisconnected();

  /** called by 
   *   - AzimuthDialog
   *   - ListerHandler.handleMessage LISTER_REF_AZIMUTH <-- not necessary
   * 
   * implemented
   * DrawingWindow
   * GMActivity: nothing
   * PhotoActivity: nothing
   * ShotWindow
   * SketchWindow: TODO
   */
  public void setRefAzimuth( float azimuth, long fixed_extend );

  /** set the title
   *
   */
  public void setTheTitle();

}

