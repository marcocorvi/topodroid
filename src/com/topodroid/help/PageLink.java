/** @file PageLink.java
 *
 * @author marco corvi
 * @date feb 2026
 *
 * @brief TopoDroid help dialog link to a user-man page
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.help;


public class PageLink
{
  public int mStart, mEnd; // start and end position in the AI response text
  public String mFilename; // filename of man-page
  public String mLinkText; // link text

  public PageLink( int start, int end, String filename )
  {
    mStart = start;
    mEnd   = end;
    mFilename = filename;
    mLinkText = null;
  }
}
