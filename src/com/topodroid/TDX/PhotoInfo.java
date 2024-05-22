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

class PhotoInfo
{
  static final int CAMERA_UNDEFINED   = 0;
  static final int CAMERA_TOPODROID   = 1;
  static final int CAMERA_TOPODROID_2 = 2;
  static final int CAMERA_INTENT      = 3;       // camera type

  long sid;       // survey id
  long id;        // photo id
  long shotid;    // shot id
  String mTitle;   // photo title FIXME TITLE
  String mShotName; // shot name
  // public String mName; // photo filename without extension ".jpg" and survey prefix dir = photo id
  String mDate;
  String mComment;
  int mCamera;
  String mGeoCode;   // geomorphology code

  /** cstr
   * @param _sid     survey id
   * @param _id      id 
   * @param _shotid  shot id
   * @param t        title
   * @param sn       shot name
   * @param dt       datetime
   * @param cmt      comment
   * @param camera   camera type
   * @param code     geomorphology code
   */
  PhotoInfo( long _sid, long _id, long _shotid, String t, String sn, String dt, String cmt, int camera, String code )
  {
    sid    = _sid;
    id     = _id;
    shotid = _shotid;
    mTitle  = t; // FIXME TITLE
    mShotName = sn;
    mDate = dt;
    mComment = cmt;
    mCamera  = camera;
    mGeoCode    = (code == null)? "" : code;
  }

  // String getPhotoName() 
  // {
  //   return String.format( "%d-%03d", sid, id );
  // }

  /** @return string presentation
   */
  public String toString()
  {
    return id 
           + " <" + ( (mShotName == null)? "-" : mShotName )
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
           + " <" + ( (mShotName == null)? "-" : mShotName )
           + "> " + mComment + " " + ((mTitle == null)? "-" : mTitle); 
  }

}
