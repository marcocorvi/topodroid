/** @file SensorInfo.java
 *
 * @author marco corvi
 * @date aug 2012
 *
 * @brief TopoDroid Sensor info (id, station, comment, value)
 * --------------------------------------------------------
 *  Copyright: This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120822 created
 */
package com.topodroid.DistoX;

class SensorInfo
{
  public long sid;       // survey id
  public long id;        // photo id
  public long shotid;    // shot id
  public String mTitle;   // sensor title
  public String mShotName; // shot name
  public String mDate;
  public String mComment;
  public String mType;    // sensor type
  public String mValue;   // sensor value
  // public String mUnit;

  public SensorInfo( long _sid, long _id, long _shotid, String title, String shotname, String date, String comment, 
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
