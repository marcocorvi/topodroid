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


class PageLink
{
  int mStart, mEnd; // start and end position in the AI response text
  String mFilename; // filename of man-page
  String mLinkText; // link text

  PageLink( int start, int end, String filename )
  {
    mStart = start;
    mEnd   = end;
    mFilename = filename;
    mLinkText = null;
  }
}
