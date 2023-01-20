/* @file IExporter.java
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

// import android.net.Uri;

public interface IExporter 
{
  /**
   * @param type         export type
   * @param filename     export filename
   * @param prefix       station names export-prefix
   * @param second       whether to export the second view (only plot or profile in DrawingWindow)
   */
  void doExport( String type, String filename, String prefix, boolean second );

  // void doExport( Uri uri );
}

