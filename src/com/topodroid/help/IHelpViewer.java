/* @file IHelpViewer.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid help viewer interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.help;

import com.topodroid.prefs.TDPref;

public interface IHelpViewer
{
  public void showManPage( String page );

  public void showAIdialog();

  public void showInvalid( final TDPref pref, final String response );
}
