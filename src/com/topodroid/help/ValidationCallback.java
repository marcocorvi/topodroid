/** @file ValidationCallback.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid API key validation response
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.help;

public interface ValidationCallback
{
  public void onResult( boolean valid, String response );
}

