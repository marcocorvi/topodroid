/** @file Cave3DShot.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief 3D: shot
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.TDX.DBlock;
// import com.topodroid.utils.TDLog;
import com.topodroid.math.TDVector;
import com.topodroid.utils.TDMath;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Cave3DShot
{
  static final double DEG2RAD = (Math.PI/180);

  static final long FLAG_SURVEY     = DBlock.FLAG_SURVEY;    // 0; // flags
  static final long FLAG_SURFACE    = DBlock.FLAG_SURFACE;   // 1;
  static final long FLAG_DUPLICATE  = DBlock.FLAG_DUPLICATE; // 2;
  static final long FLAG_COMMENTED  = DBlock.FLAG_COMMENTED; // 4; // lox-flag NOT_VISIBLE
  // static final long FLAG_NO_PLAN    =  8;
  // static final long FLAG_NO_PROFILE = 16;
  static final long FLAG_BACKSHOT   = DBlock.FLAG_BACKSHOT;  // 32;

  public double len, ber, cln;      // radians
  boolean used = false;
  private long mFlag;    // shot flag
  long mMillis;  // timestamp [msec]

  public String from;
  public String to;
  public Cave3DStation from_station;
  public Cave3DStation to_station;  // null for splay shots
  public Cave3DSurvey mSurvey;
  private int mSurveyNr;
  public int mSurveyId;  // survey ID for bluetooth
  public int mColor = 0; // survey color - used by parser to pass the value to survey

  // same as in DBlock
  public boolean isSurvey()    { return mFlag == FLAG_SURVEY; }
  public boolean isSurface()   { return (mFlag & FLAG_SURFACE)    == FLAG_SURFACE; }
  public boolean isDuplicate() { return (mFlag & FLAG_DUPLICATE)  == FLAG_DUPLICATE; }
  public boolean isCommented() { return (mFlag & FLAG_COMMENTED)  == FLAG_COMMENTED; } 
  public boolean isBackshot()  { return (mFlag & FLAG_BACKSHOT)   == FLAG_BACKSHOT; }
  public boolean isUsed()      { return used; }
  public void setUsed() { used = true; }

  public long getFlag() { return mFlag; }

  // used by manual legs
  // public void setFlags( boolean s, boolean d, boolean c, boolean b )
  // {
  //   mFlag = FLAG_SURVEY
  //         | ( s ? FLAG_SURFACE : 0 )
  //         | ( d ? FLAG_DUPLICATE : 0 )
  //         | ( c ? FLAG_COMMENTED : 0 )
  //         | ( b ? FLAG_BACKSHOT : 0 );
  // }
    

  public double length( ) { return len; }
  // public double bearing() { return ber; }
  // public double clino()   { return cln; }

  // -----------------------------------------------------

  void serialize( DataOutputStream dos ) throws IOException
  {
    dos.writeInt( mSurveyId );
    dos.writeUTF( from );
    dos.writeUTF( to );
    dos.writeDouble( len );
    dos.writeDouble( ber );
    dos.writeDouble( cln );
    dos.writeLong( mFlag );
    dos.writeLong( mMillis );
    dos.writeInt( mColor );
    // TDLog.v("ser. shot <" + from + "=" + to + "> " + len + " " + ber + " " + cln );
  }

  static Cave3DShot deserialize( DataInputStream dis, int version ) throws IOException
  {
    int id      = dis.readInt( );
    String from = dis.readUTF( );
    String to   = dis.readUTF( );
    double len  = dis.readDouble( );
    double ber  = dis.readDouble( );
    double cln  = dis.readDouble( );
    long flag   = dis.readLong( );
    long millis = dis.readLong( );
    int  color  = dis.readInt( );
    // int  color  = (version > 601054)? dis.readInt( ) : 0;
    // TDLog.v("deserialized shot <" + from + "=" + to + "> " + len + " " + ber + " " + cln );
    Cave3DShot shot = new Cave3DShot( from, to, len, ber, cln, flag, millis, color );
    shot.mSurveyId = id;
    return shot;
  }


  // /** cstr - b/c in degrees
  //  * @param f    from station name
  //  * @param t    to station name
  //  * @param l    length
  //  * @param b    azimuth
  //  * @param c    clino
  //  * @param flag flag
  //  * @param millis timestamp
  //  */
  // public Cave3DShot( String f, String t, double l, double b, double c, long flag, long millis )
  // {
  //   from = f;
  //   to   = t;
  //   len = l;
  //   ber = b * DEG2RAD;
  //   cln = c * DEG2RAD;
  //   used = false;
  //   from_station = null;
  //   to_station   = null;
  //   mSurvey = null; // survey and surveyNr are updated when the shot is added to a survey
  //   mSurveyNr = 0;
  //   mSurveyId = -1;
  //   mFlag = flag;
  //   mMillis = millis;
  // }

  /** cstr - b/c in degrees
   * @param f    from station name
   * @param t    to station name
   * @param l    length
   * @param b    azimuth
   * @param c    clino
   * @param flag flag
   * @param millis timestamp
   */
  public Cave3DShot( String f, String t, double l, double b, double c, long flag, long millis, int color )
  {
    from = f;
    to   = t;
    len = l;
    ber = b * DEG2RAD;
    cln = c * DEG2RAD;
    used = false;
    from_station = null;
    to_station   = null;
    mSurvey = null; // survey and surveyNr are updated when the shot is added to a survey
    mSurveyNr = 0;
    mSurveyId = -1;
    mFlag = flag;
    mMillis = millis;
    mColor  = color;
  }

  /** cstr, used for cave path-length between stations - b,c radians
   * @param f    from station
   * @param t    to station
   * @param l    length
   * @param b    azimuth
   * @param c    clino
   * @param flag flag
   * @param millis timestamp
   */
  public Cave3DShot( Cave3DStation f, Cave3DStation t, double l, double b, double c, long flag, long millis, int color )
  {
    from = (f!=null)? f.getFullName() : null;
    to   = (t!=null)? t.getFullName() : null;
    len = l;
    ber = b;
    cln = c;
    used = false;
    from_station = f;
    to_station   = t;
    mSurvey   = null; // survey and surveyNr are updated when the shot is added to a survey
    mSurveyNr = 0;
    mSurveyId = -1;
    mFlag = flag;
    mMillis = millis;
    mColor = color;
  }

  /** @return true if the station has a survey
   */
  boolean hasSurvey() { return mSurvey != null; }

  /** set the station survey
   * @param survey   survey
   * @note used also by Parser3D
   */
  public void setSurvey( Cave3DSurvey survey ) 
  { 
    mSurvey   = survey;
    mSurveyNr = survey.number;
    mSurveyId = survey.mId;
  }

  /** @return the station survey (or null)
   */
  Cave3DSurvey getSurvey() { return mSurvey; }

  /** @return the survey index (number)
   */
  int getSurveyNr() { return mSurveyNr; }

  /** @return the ID of the station survey 
   */
  int getSurveyId() { return mSurveyId; }

  /** set the FROM station
   * @param st   station
   */
  void setFromStation( Cave3DStation st )
  { 
    from_station = st; 
    from = (st!=null)? st.getFullName() : null;
  }

  /** set the TO station
   * @param st   station
   */
  void setToStation( Cave3DStation st )
  { 
    to_station = st; 
    to = (st!=null)? st.getFullName() : null;
  }

  /* dot product 
   * ( cc1 * cb1, cc1 * sb1, sc1 ) * ( cc2 * cb2, cc2 * sb2, sc2 )
   *   = cc1 * cc2 * cos(b1-b2) + sc1 * sc2
   */
  public double dotProduct( Cave3DShot sh )
  {
    return Math.cos( ber - sh.ber ) * Math.cos( cln ) * Math.cos( sh.cln ) + Math.sin( cln ) * Math.sin( sh.cln );
  }

  // dot product with a vector (E, N, Z)
  public double dotProduct( Vector3D v )
  {
    return (Math.cos(ber)*v.y + Math.sin(ber)*v.x) * Math.cos( cln ) + Math.sin( cln ) * v.z;
  }

  public Cave3DStation getStationFromStation( Cave3DStation st ) 
  {
    if ( st.getFullName().equals( from ) ) {
      double dz = len * Math.sin( cln );
      double dh = len * Math.cos( cln );
      return new Cave3DStation( to, 
                          st.x + (dh * Math.sin(ber)),
                          st.y + (dh * Math.cos(ber)),
                          st.z + (dz) );
    } else if ( st.getFullName().equals( to ) ) {
      double dz = len * Math.sin( cln );
      double dh = len * Math.cos( cln );
      return new Cave3DStation( from,
                          st.x - (dh * Math.sin(ber)),
                          st.y - (dh * Math.cos(ber)),
                          st.z - (dz) );
    } else {
      return null;
    }
  }

  // average depth of the shot
  double depth()
  {
    if ( to_station == null ) return 0.0f;
    return (from_station.depth + to_station.depth)/2;
  }

  /** return the 3D vector (E, N, Up )
   */
  public Vector3D toVector3D() 
  {
    double h = len * Math.cos(cln);
    return new Vector3D( (h * Math.sin(ber)), (h * Math.cos(ber)), (len * Math.sin(cln)) );
  }

  /** @return the TD vector (E, N, Up )
   * @note used by SketchWindow
   */
  public TDVector toTDVector()
  {
    double h = len * Math.cos(cln);
    return new TDVector( (float)(h * Math.sin(ber)), (float)(h * Math.cos(ber)), (float)(len * Math.sin(cln)) );
  }

  // makes sense only for splays
  public Vector3D toPoint3D()
  {
    int sign = 1;
    Cave3DStation st = from_station;
    if ( st == null ) {
      st = to_station;
      sign = -1;
    }
    if ( st == null ) return null;
    double h = sign * len * Math.cos(cln);
    return new Vector3D( st.x + (h * Math.sin(ber)), st.y + (h * Math.cos(ber)), st.z + sign * (len * Math.sin(cln)) );
  }

  Cave3DStation getOtherStation( Cave3DStation st )
  {
    if ( st == from_station ) return to_station;
    if ( st == to_station )   return from_station;
    return null;
  }

}

