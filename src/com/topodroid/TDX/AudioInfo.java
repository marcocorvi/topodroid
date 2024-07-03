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

class AudioInfo extends MediaInfo
{
  /** cstr
   * @param sid    survey ID
   * @param id     audio ID
   * @param item_id reference item ID
   * @param dt      date
   * @param type    reference item type
   */
  AudioInfo( long sid, long id, long item_id, String dt, int type )
  {
    super( MediaInfo.MEDIA_AUDIO, sid, id, item_id, dt, type );
  }

  // /** @return the shot ID
  //  */
  // int getFileNumber() { return (int)mItemId; }

  // String getAudioName() 
  // {
  //   return String.format( "%d-%03d", sid, id );
  // }

}
