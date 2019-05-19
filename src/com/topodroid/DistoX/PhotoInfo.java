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
package com.topodroid.DistoX;

class PhotoInfo
{
  static final int CAMERA_UNDEFINED = 0;
  static final int CAMERA_TOPODROID = 1;
  static final int CAMERA_INTENT    = 2;       // camera type

  long sid;       // survey id
  long id;        // photo id
  long shotid;    // shot id
  String mTitle;   // photo title FIXME TITLE
  String mShotName; // shot name
  // public String mName; // photo filename without extension ".jpg" and survey prefix dir = photo id
  String mDate;
  String mComment;
  int mCamera;

  PhotoInfo( long _sid, long _id, long _shotid, String t, String sn, String dt, String cmt, int camera )
  {
    sid    = _sid;
    id     = _id;
    shotid = _shotid;
    mTitle  = t; // FIXME TITLE
    mShotName = sn;
    mDate = dt;
    mComment = cmt;
    mCamera  = camera;
  }

  // String getPhotoName() 
  // {
  //   return String.format( "%d-%03d", sid, id );
  // }

  public String toString()
  {
    return id 
           + " <" + ( (mShotName == null)? "-" : mShotName )
           + "> " + mComment; 
  }

  String debugString()
  {
    return id  
           + " <" + ( (mShotName == null)? "-" : mShotName )
           + "> " + mComment + " " + ((mTitle == null)? "-" : mTitle); 
  }

}
