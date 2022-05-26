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
package com.topodroid.TDX;

// import androidx.annotation.RecentlyNonNull;

class AudioInfo
{
  private final long sid;  // survey id (assigned but never used)
  final long id;           // audio id
  final long fileIdx;      // shot id
  private final String mDate;

  /** cstr
   * @param _sid    survey ID
   * @param _id     audio ID
   * @param fileidx shot ID
   * @param dt      date
   */
  AudioInfo( long _sid, long _id, long fileidx, String dt )
  {
    sid     = _sid;
    id      = _id;
    fileIdx = fileidx;
    mDate   = dt;
  }

  /** @return the shot ID
   */
  int getFileNumber() { return (int)fileIdx; }

  // String getAudioName() 
  // {
  //   return String.format( "%d-%03d", sid, id );
  // }

  // @RecentlyNonNull
  /** @return the string presentation of this audio info
   */
  public String toString()
  {
    return id + " <" + mDate  + "> ";
  }

  /** @return the full string presentation of this audio info
   * @param shot_name   name of the shot
   */
  public String getFullString( String shot_name )
  {
    return id + ": " + shot_name + " <" + mDate  + "> ";
  }

}
