/** @file SensorInfo.java
 *
 * @author marco corvi
 * @date aug 2012
 *
 * @brief TopoDroid Sensor info (id, station, comment, value)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

class SensorInfo
{
  long sid;       // survey id
  long id;        // photo id
  long shotid;    // shot id
  String mTitle;   // sensor title
  String mShotName; // shot name
  String mDate;
  String mComment;
  String mType;    // sensor type
  String mValue;   // sensor value
  // public String mUnit;

  SensorInfo( long _sid, long _id, long _shotid, String title, String shotname, String date, String comment,
                     String type, String value )
  {
    sid    = _sid;
    id     = _id;
    shotid = _shotid;
    mTitle  = title;
    mShotName = shotname;
    mDate    = date;
    mComment = comment;
    mType    = type;
    mValue   = value;
  }

  // String getSensorName() 
  // {
  //   return String1.format( "%d-%03d", sid, id );
  // }

  public String toString()
  {
    return id 
           + " <" + ( (mShotName == null)? "-" : mShotName )
           + "> " + mType + " " + mDate + ": " + mValue;
  }

}
