/* @file PhotoInfo.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo info (id, station, comment)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

class PhotoInfo extends MediaInfo
{
  static final int CAMERA_UNDEFINED   = 0;
  static final int CAMERA_TOPODROID   = 1;
  static final int CAMERA_TOPODROID_2 = 2;
  static final int CAMERA_INTENT      = 3;       // camera type

  String mTitle;   // photo title FIXME TITLE
  String mItemName; // shot name
  // public String mName; // photo filename without extension ".jpg" and survey prefix dir = photo id
  String mComment;
  int mCamera;
  String mGeoCode;   // geomorphology code

  /** cstr
   * @param _sid     survey id
   * @param _id      id 
   * @param item_id  reference item ID: shot id or plot ID
   * @param t        title
   * @param sn       reference name: shot name or plot name
   * @param dt       datetime
   * @param cmt      comment
   * @param camera   camera type
   * @param code     geomorphology code
   * @param type     reference item type
   */
  PhotoInfo( long _sid, long _id, long item_id, String t, String sn, String dt, String cmt, int camera, String code, int type )
  {
    super( MediaInfo.MEDIA_PHOTO, _sid, _id, item_id, dt, type );
    mTitle  = t; // FIXME TITLE
    mItemName = sn;
    mComment = cmt;
    mCamera  = camera;
    mGeoCode = (code == null)? "" : code;
  }

  // String getPhotoName() 
  // {
  //   return String.format( "%d-%03d", sid, id );
  // }

  /** @return string presentation
   */
  @Override
  public String toString()
  {
    return id 
           + " <" + ( (mItemName == null)? "-" : mItemName )
           + "> " + mComment; 
  }

  /** @return the geomorphology code
   */
  public String getGeoCode() { return mGeoCode; }

  /** set the geomorphology code
   * @param code   new code
   */
  public void setGeoCode( String geocode ) { mGeoCode = (geocode == null)? "" : geocode; }

  /** @return debug string presentation
   */
  String debugString()
  {
    return id  
           + " <" + ( (mItemName == null)? "-" : mItemName )
           + "> " + mComment + " " + ((mTitle == null)? "-" : mTitle); 
  }

}
