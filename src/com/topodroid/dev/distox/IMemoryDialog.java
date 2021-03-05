/* @file IMemoryDialog.java
 *
 * @author marco corvi 
 * @date nov 2013
 *
 * @brief TopoDroid bearing and clino interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.dev.distox;

import com.topodroid.packetX.MemoryOctet;

import java.util.ArrayList;

public interface IMemoryDialog
{
  void updateList( ArrayList<MemoryOctet> memory );
}
