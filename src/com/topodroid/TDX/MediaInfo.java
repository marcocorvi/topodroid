/* @file MediaInfo.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid Media info (sid, id, item_id, date, type )
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import androidx.annotation.RecentlyNonNull;

class MediaInfo
{
  static final int MEDIA_UNKNOWN = 0;  // media types
  static final int MEDIA_AUDIO   = 1;
  static final int MEDIA_PHOTO   = 2;
  static final int MEDIA_SENSOR  = 3;

  static final int TYPE_UNDEFINED = 0; // reference item types
  static final int TYPE_SHOT      = 1;
  static final int TYPE_PLOT      = 2;
  static final int TYPE_XSECTION  = 3; // FIXME when is this used ?

  protected final int  mMediaType; // either MEDIA_AUDIO or MEDIA_PHOTO or MEDIA_SENSOR
  protected final long sid;     // survey id (assigned but never used)
  protected final long id;      // media id (audio id or photo id)
  protected final long mItemId; // reference item ID: shot ID or plot ID (for audio: old fileIdx)
  protected final String mDate;
  protected int mItemType;     // reference item type

  /** cstr
   * @param _sid    survey ID
   * @param _id     media ID
   * @param item_id reference item ID
   * @param dt      date
   * @param type    reference item type
   */
  MediaInfo( int media_type, long _sid, long _id, long item_id, String dt, int type )
  {
    mMediaType = media_type;
    sid     = _sid;
    id      = _id;
    mItemId = item_id;
    mDate   = dt;
    mItemType = type;
  }

  /** @return the type of media
   */
  public int getMediaType() { return mMediaType; }

  /** @return the survey ID of this media
   */
  public long getSurveyId() { return sid; }

  /** @return the ID of this media
   */
  public long getId() { return id; }

  /** @return the reference type
   */
  public int getItemType() { return mItemType; }

  /** @return the reference item ID
   */
  public long getItemId() { return mItemId; }

  /** @return true if this media has the given item id
   * @param item_id   given item ID
   */
  public boolean hasItemId( long item_id ) { return mItemId == item_id; }

  /** @return the date string
   */
  public String getDate() { return mDate; }


  // @RecentlyNonNull
  /** @return the string presentation of this audio info
   */
  public String toString()
  {
    switch ( mItemType ) {
      case TYPE_SHOT:     return id + " <" + mDate + "> ";
      case TYPE_PLOT:     return id + " {" + mDate + "} ";
      case TYPE_XSECTION: return id + " [" + mDate + "] ";
    }
    return id + " - " + mDate;
  }

  /** @return the full string presentation of this audio info
   * @param item_name   name of the reference item (can be null)
   */
  public String getFullString( String item_name )
  {
    if ( item_name != null ) {
      switch ( mItemType ) {
        case TYPE_SHOT:     return id + ": " + item_name + " <" + mDate  + "> ";
        case TYPE_PLOT:     return id + ": " + item_name + " {" + mDate  + "} ";
        case TYPE_XSECTION: return id + ": " + item_name + " [" + mDate  + "] ";
      }
      return id + ": " + item_name + " - " + mDate;
    } else {
      switch ( mItemType ) {
        case TYPE_SHOT:     return id + ": <" + mDate  + "> ";
        case TYPE_PLOT:     return id + ": {" + mDate  + "} ";
        case TYPE_XSECTION: return id + ": [" + mDate  + "] ";
      }
      return id + ": " + mDate;
    }
  }
}
