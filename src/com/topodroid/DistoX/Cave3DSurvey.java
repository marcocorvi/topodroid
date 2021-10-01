/** @file Cave3DSurvey.java
 *
 * @author marco corvi
 * @date may 2020
 *
 * @brief Cave3D survey
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;

import java.util.List;
import java.util.ArrayList;

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class Cave3DSurvey
{
  private static int count = 0;

  public int number; // survey index
  int mId;    // id 
  int mPid;   // parend Id
  String name;
  boolean visible;

  ArrayList< Cave3DShot > mShots;
  ArrayList< Cave3DShot > mSplays;
  ArrayList< Cave3DStation > mStations;

  // int mNrShots;
  // int mNrSplays;
  double mLenShots;
  double mLenSplays;

  void serialize( DataOutputStream dos ) throws IOException
  {
    dos.writeInt( mId );
    dos.writeUTF( name );
  }

  static Cave3DSurvey deserialize( DataInputStream dis, int version ) throws IOException
  {
    int id = dis.readInt();
    String name = dis.readUTF();
    TDLog.v("Cave3D survey deser. " + id + " " + name );
    return new Cave3DSurvey( name, id, -1 );
  }

  // ------------------------------------------------------ 

  public Cave3DSurvey( String n )
  {
    number = count; ++ count;
    init( n, -1, -1 );
  }

  public Cave3DSurvey( String n, int id, int pid )
  {
    number = count; ++ count;
    init( n, id, pid );
  }

  public boolean hasName( String nm ) { return name != null && name.equals( nm ); }
  public String  getName() { return name; }

  // void addShot( String from, String to, double len, double ber, double cln ) { addShot( new Cave3DShot( from, to, ber, len, cln, 0, 0 ) ); }
  
  // void addSplay( String from, double len, double ber, double cln ) { addSplay( new Cave3DShot( from, null, ber, len, cln, 0, 0 ) ); }

  public void addShot( Cave3DShot sh ) 
  { 
    mShots.add( sh );
    sh.setSurvey( this );
    // mNrShots ++;
    mLenShots += sh.len;
  }

  public void addSplay( Cave3DShot sh )
  {
    mSplays.add( sh );
    sh.setSurvey( this );
    // mNrSplays ++;
    mLenSplays += sh.len;
  }

  public Cave3DStation addStation( Cave3DStation st )
  { 
    mStations.add( st );
    st.setSurvey( this );
    return st;
  }
    
  public Cave3DStation getStation( String name ) 
  {
    if ( name == null || name.length() == 0 ) return null;
    if ( name.equals("-") || name.equals(".") ) return null;
    for ( Cave3DStation st : mStations ) if ( name.equals( st.getFullName() ) ) return st;
    return null;
  }

  List< Cave3DShot > getShots()       { return mShots; }
  List< Cave3DShot > getSplays()      { return mSplays; }
  List< Cave3DStation > getStations() { return mStations; }

  // --------------------------- DATA REDUCTION
  void reduce()
  {
    mLenShots  = 0.0;
    mLenSplays = 0.0;
    addStation( new Cave3DStation( mShots.get(0).from, 0f, 0f, 0f ) );
    int used_shots = 0; // check connectedness
    int size = 0;
    while ( size < mStations.size() ) {
      size = mStations.size();
      for ( Cave3DShot sh : mShots ) {
        if ( sh.hasSurvey() ) continue;
        Cave3DStation fr = getStation( sh.from );
        if ( fr != null ) {
          sh.from_station = fr;
          markShotUsed( sh );
          ++used_shots;
          Cave3DStation to = getStation( sh.to, size );
          if ( to == null ) to = addStation( sh.getStationFromStation( fr ) );
          sh.to_station = to;
        } else {
          Cave3DStation to = getStation( sh.to, size );
          if ( to != null ) {
            sh.to_station = to;
            markShotUsed( sh );
            ++used_shots;
            sh.from_station = addStation( sh.getStationFromStation( to ) );
          }
        }
      }
    }
    // TDLog.v("shots " + mShots.size() + " used " + used_shots );
    int used_splays = 0; // check
    for ( Cave3DShot sp : mSplays ) {
      Cave3DStation st = getStation( sp.from );
      if ( st != null ) {
        sp.from_station = st;
        markSplayUsed( sp );
        ++ used_splays;
      } else {
        st = getStation( sp.to );
        if ( st != null ) {
          sp.from_station = st;
          markSplayUsed( sp );
          ++ used_splays;
        }
      }
    }
    // TDLog.v("splays " + mSplays.size() + " used " + used_splays );
  }

  // get station starting from index id
  private Cave3DStation getStation( String name, int idx ) 
  {
    if ( name == null || name.length() == 0 ) return null;
    if ( name.equals("-") || name.equals(".") ) return null;
    for ( ; idx < mStations.size(); ++idx ) {
      Cave3DStation st = mStations.get(idx);
      if ( name.equals( st.getFullName() ) ) return st;
    }
    return null;
  }
 
  private void markShotUsed( Cave3DShot sh )
  {
    mLenShots += sh.len;
    sh.setSurvey( this );
  }
 
  private void markSplayUsed( Cave3DShot sh )
  {
    mLenSplays += sh.len;
    sh.setSurvey( this );
  }
  //

  // --------------------------- STATS
  int getShotNr()    { return mShots.size(); }   // mNrShots
  int getSplayNr()   { return mSplays.size(); }  // mNrSplays
  int getStationNr() { return mStations.size(); }

  double getShotLenght()  { return mLenShots; }
  double getSplayLenght() { return mLenSplays; }

  // ---------------------------- INIT
  private void init( String n, int id, int pid )
  {
    mId  = id;
    mPid = pid;
    name = n;
    visible = true;
    // mNrShots  = 0;
    // mNrSplays = 0;
    mShots    = new ArrayList< Cave3DShot >();
    mSplays   = new ArrayList< Cave3DShot >();
    mStations = new ArrayList< Cave3DStation >();
  }
}


