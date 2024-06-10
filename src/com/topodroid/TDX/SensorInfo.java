/* @file SensorInfo.java
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
package com.topodroid.TDX;

class SensorInfo extends MediaInfo
{
  String mTitle;    // sensor title
  String mShotName; // shot name
  String mComment;  // comment
  private String mSensor;     // sensor type
  String mValue;    // sensor value
  // public String mUnit;

  SensorInfo( long _sid, long _id, long _shotid, String title, String shotname, String date, String comment, String sensor, String value, int type )
  {
    super( MediaInfo.MEDIA_SENSOR, _sid, _id, _shotid, date, type );
    mTitle    = title;
    mShotName = shotname;
    mComment  = comment;
    mSensor   = sensor;
    mValue    = value;
  }

  /** @return the type of the sensor
   */
  String getSensorType() { return mSensor; }

  /** @return the value of the sensor
   */
  String getValue() { return mValue; }

  // String getSensorName() 
  // {
  //   return String1.format( "%d-%03d", sid, id );
  // }

  public String toString()
  {
    return id 
           + " <" + ( (mShotName == null)? "-" : mShotName )
           + "> " + mSensor + " " + mDate + ": " + mValue;
  }

}
