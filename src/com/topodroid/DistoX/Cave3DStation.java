/** @file Cave3DStation.java
 *
 * @author marco corvi
 * @date mav 2020
 *
 * @brief Cave3D station
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

// 3D vcetor (E, N, Up)
public class Cave3DStation extends Vector3D
{
  public static final int FLAG_NONE    = 0;
  public static final int FLAG_FIXED   = 1;
  public static final int FLAG_PAINTED = 2;

  int vertex;     // index of vertex (coords) in the array of vertices 
                  // to get the coords use 3*vertex+0, 3*vertex+1, 3*vertex+2
  int mId;
  int mSid;       // survey id
  Cave3DSurvey mSurvey;

  // double e, n, z;  // north east, vertical (upwards) : e=x, n=y, z=z
  private String short_name;
  private String survey_name;
  private String full_name;

  // double temp;          // station tempertaure [Celcius] TEMPERATURE
  double depth;         // depth from Zmax: positive and scaled in [0,1] : 1.0 deepest
  double surface_depth; // depth beneath the surface
  int flag;       // station flag (not used)
  String comment; // not used

  double pathlength; // path length
  Cave3DStation pathprev;

  // -------------------------------------------------------------------------
  void serialize( DataOutputStream dos ) throws IOException
  {
    dos.writeInt( mId );
    dos.writeInt( mSid );
    dos.writeUTF( full_name );
    dos.writeInt( flag );
    dos.writeDouble( x );
    dos.writeDouble( y );
    dos.writeDouble( z );
    TDLog.v("ser. station " + mId + " " + mSid + " <" + full_name + "> " + x + " " + y + " " + z );
  }

  static Cave3DStation deserialize( DataInputStream dis, int version ) throws IOException 
  {
    int id  = dis.readInt();
    int sid = dis.readInt();
    String full_name  = dis.readUTF();
    int flag = dis.readInt();
    double x = dis.readDouble();
    double y = dis.readDouble();
    double z = dis.readDouble();
    TDLog.v("deser. station " + id + " " + sid + " <" + full_name + "> " + x + " " + y + " " + z );
    return new Cave3DStation( full_name, x, y, z, id, sid, flag, "" );
  }

  // -------------------------------------------------------------------------

  public Cave3DStation( String nm, double e0, double n0, double z0 )
  {
    super( e0, n0, z0 );
    init( nm, -1, -1, null, FLAG_NONE, null );
  }

  public Cave3DStation( String nm, double e0, double n0, double z0, Cave3DSurvey survey )
  {
    super( e0, n0, z0 );
    int sid = ( survey == null )? -1 : survey.mId;
    init ( nm, -1, sid, survey, FLAG_NONE, null );
  }

  // Cave3DStation( String nm, double e0, double n0, double z0, Cave3DSurvey survey, int fl, String cmt )
  // {
  //   super( e0, n0, z0 );
  //   init ( nm, survey, fl, cmt );
  // }

  public Cave3DStation( String nm, double e0, double n0, double z0, int id, int sid, int fl, String cmt )
  {
    super( e0, n0, z0 );
    init( nm, id, sid, null, fl, cmt );
  }

  void setSurvey( Cave3DSurvey survey )
  {
    mSurvey = survey;
    mSid    = survey.mId;
  }

  public boolean hasName( String nm ) { return full_name != null && full_name.equals( nm ); }
  public String  getFullName()  { return full_name; }
  public String  getShortName() { return short_name; }
  public String  getSurvey()    { return survey_name; }
  public void setFlag( int fl ) { flag = fl; }

  void setPathlength( double len, Cave3DStation prev ) { pathlength = len; pathprev = prev; }

  double getPathlength() { return pathlength; }
  double getFinalPathlength() { return (pathprev == null)? -1.0 : pathlength; }
  Cave3DStation getPathPrevious() { return pathprev; }

  Vector3D toVector3D() { return new Vector3D( x, y, z ); }
  Point2D  toPoint2D()  { return new Point2D( x, y ); }

  // boolean coincide( Cave3DStation p, double eps ) this is Vector3D::coincide()

  // double distance3D( Cave3DStation p ) this is Vector3D::distance3D()

  // ---------------------- CSTR
  private void init( String nm, int id, int sid, Cave3DSurvey survey, int fl, String cmt )
  {
    setName( nm );
    mId     = id;
    mSid    = sid;
    mSurvey = survey;
    flag    = fl;
    comment = cmt;
    pathlength = Float.MAX_VALUE;
    pathprev = null;
    surface_depth = 0;
    // temp = 0; // TEMPERATURE
  }

  public void setName( String nm )
  {
    full_name = nm;
    if ( full_name != null ) {
      int index = full_name.indexOf("@");
      if ( index > 0 ) {
        short_name  = full_name.substring( 0, index );
        survey_name = full_name.substring( index+1 );
      } else {
        short_name  = full_name;
        survey_name = "";
      }
    } else {
      short_name = "";
      survey_name = "";
    }
  }

  // add a discriminating number to the station name
  public void addToName( int number )
  {
    short_name = short_name + "-" + number;
    full_name  = short_name + "@" + survey_name;
  }

}

