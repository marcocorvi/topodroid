/* @file DeviceXBLETakeShot.java
 *
 * @author siwei tian
 * @date aug 2022
 *
 * @brief TopoDroid DistoXBLE shooting class
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox_ble;


import com.topodroid.TDX.ILister;
import com.topodroid.TDX.ListerHandler;
import com.topodroid.TDX.TopoDroidApp;
import com.topodroid.dev.RemoteShotTask; // MM

public class DistoXBLETakeShot extends RemoteShotTask
{

  /** cstr
   * @param ilister    lister that started the task
   * @param lister     shot data lister
   * @param app        application
   * @param nr         number of shots to take
   * @param data_type  expected shot type
   */
  public DistoXBLETakeShot(ILister ilister, ListerHandler lister, TopoDroidApp app, int nr, int data_type )
  {
    super( ilister, lister, app, nr, data_type, DistoXBLEComm.class );
  } 

}
