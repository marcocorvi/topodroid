/* @file AudioInfo.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid Audio info (id, sid, shot_id, date )
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class AudioInfo
{
  private final long sid;       // survey id
  final long id;        // audio id
  final long fileIdx;    // shot id
  private final String mDate;

  AudioInfo( long _sid, long _id, long fileidx, String dt )
  {
    sid     = _sid;
    id      = _id;
    fileIdx = fileidx;
    mDate   = dt;
  }

  int getFileNumber() { return (int)fileIdx; }

  // String getAudioName() 
  // {
  //   return String.format( "%d-%03d", sid, id );
  // }

  public String toString()
  {
    return id + " <" + mDate  + "> ";
  }

  public String getFullString( String shot_name )
  {
    return id + ": " + shot_name + " <" + mDate  + "> ";
  }

}
