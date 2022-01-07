/** @file TdmSurvey.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager survey object
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDMath;
import com.topodroid.DistoX.DBlock;
import com.topodroid.DistoX.DataHelper;
import com.topodroid.DistoX.SurveyInfo;

import java.util.ArrayList;
import java.util.List;

// import java.io.BufferedReader;

// import android.util.FloatMath;

public class TdmSurvey
{
  String mName; // survey name = db_name
  TdmSurvey mParent;
  TdmStation mStartStation;
  SurveyInfo mInfo = null;
  boolean    mLoadedData = false;
  float      mDeclination; // declination [radians]

  ArrayList< TdmShot >    mShots;
  ArrayList< TdmStation > mStations;
  ArrayList< TdmSurvey >  mSurveys;  // child surveys
  ArrayList< TdmEquate >  mEquates;
  // ArrayList< TdFix >     mFixes;

  /** cstr
   * @param name    survey name
   */
  TdmSurvey( String name )
  {
    mName   = name;
    mParent = null;
    mShots    = new ArrayList< TdmShot >();
    mStations = null;
    mSurveys  = new ArrayList< TdmSurvey >();
    mEquates  = new ArrayList< TdmEquate >();
    // mFixes    = new ArrayList< TdFix >();
    mDeclination = 0;
  }

  /** cstr
   * @param name    survey name
   * @param parent  survey parent
   */
  TdmSurvey( String name, TdmSurvey parent )
  {
    mName   = name;
    mParent = parent;
    mShots    = new ArrayList< TdmShot >();
    mStations = null;
    mSurveys  = new ArrayList< TdmSurvey >();
    mEquates  = new ArrayList< TdmEquate >();
    // mFixes    = new ArrayList< TdFix >();
    mDeclination = 0;
  }

  /** get a child survey
   * @param ns     survey namespace
   * @param pos    survey position in the namespace (0 = immediate child)
   * @return the child survey, or null if not found
   * @note recursive
   */
  TdmSurvey getSurvey( String[] ns, int pos )
  {
    String name = ns[pos];
    for ( TdmSurvey s : mSurveys ) {
      if ( name.equals( s.mName ) ) {
        if ( pos == 0 ) return s;
        return s.getSurvey( ns, pos-1 );
      }
    }
    return null;
  }

  /** load survey from the database
   * @param data    database helper
   *
   * @note declination is saved in a class field - it could be added to the shot bearing here
   *       however it is added when the stations coords are computed 
   *       @see computeStations()
   */
  void loadSurveyData( DataHelper data ) 
  {
    if ( mLoadedData ) return;
    if ( mInfo == null ) {
      mInfo = data.getSurveyInfo( mName );
    }
    if ( mInfo != null ) {
      mDeclination = mInfo.getDeclination() * TDMath.DEG2RAD;
      List< DBlock > blks = data.getSurveyReducedData( mInfo.id );
      for ( DBlock blk : blks ) {
        addShot( blk.mFrom, blk.mTo, blk.mLength, blk.mBearing, blk.mClino, blk.getIntExtend() );
      }
      mLoadedData = true;
      // TDLog.v("Survey " + mName + " loaded data " + mShots.size() );
    } else {
      TDLog.Error("TdManager Survey " + mName + ": unable to get survey info");
    }
  }

  // void addFix( TdFix fix ) { mFixes.add( fix ); }

  /** add an equate to the survey
   * @param equate   equate to add
   */
  void addEquate( TdmEquate equate ) { mEquates.add( equate ); }

  /** add a sub-survey to the survey
   * @param survey   sub-survey to add
   */
  void addSurvey( TdmSurvey survey )
  {
    mSurveys.add( survey );
    survey.mParent = this;
  }

  /** get the last-name of the survey
   * @return the survey name
   */
  String getName()   { return mName; }

  /** get the full name of the survey: "last_name.parent_full_name"
   * @return the survey full-name
   */
  String getFullName() 
  { 
    if ( mParent != null ) {
      return mName + "." + mParent.getFullName();
    }
    return mName;
  }

  /** get survey id
   * @return the survey id in the database table, or -1 if the survey is not from the database
   */
  long getId() 
  {
    if ( mInfo == null ) return -1;
    return mInfo.id;
  }

  /** data reduction
   * data reduction consumes the equates that are resolved inside the survey stations
   * without considering the child surveys
   */
  void reduce()
  {
    computeStations();
    for ( TdmSurvey s : mSurveys ) s.reduce();
  }

  // void addShot( TdmShot shot ) { mShots.add( shot ); }

  /** add a shot to the survey
   * @param from     FROM station
   * @param to       TO station
   * @param d        length
   * @param b        bearing (azimuth)
   * @param c        clino
   * @param e        extend (integer)
   * @note if either FROM or TO is null the shot is not inserted in the survey
   */
  void addShot( String from, String to, float d, float b, float c, int e )
  {
    if ( from == null || to == null ) return;
    mShots.add( new TdmShot( from, to, d, b, c, e, this ) );
  }

  /** get a station by the the name
   * @param name    station name
   */
  TdmStation getStation( String name )
  {
    if ( name == null || name.equals("") ) return null;
    for ( TdmStation st : mStations ) {
      if ( st.mName.equals( name ) ) return st;
    }
    return null;
  }

  // ---------------------------------------------------------------

  /** compute the stations coordinates (for data reduction)
   */
  private void computeStations()
  {
    mStations = new ArrayList< TdmStation >();
    mStartStation = null;
    if ( mShots.size() == 0 ) return;

    // reset shots stations
    for ( TdmShot sh : mShots ) sh.setTdmStations( null, null );

    TdmStation fs=null, ts=null;
    boolean repeat = true;
    while ( repeat ) {
      repeat = false;
      for ( TdmShot sh : mShots ) {
        if ( sh.mFromStation != null ) continue; // shot already got stations
        if ( mStartStation == null ) {
          fs = new TdmStation( sh.mFrom, 0, 0, 0, 0, this );
	  mStartStation = fs;
          mStations.add( mStartStation );
          // angles are already in radians
          float h = (float)Math.cos( sh.mClino ) * sh.mLength;
          float v = (float)Math.sin( sh.mClino ) * sh.mLength;
          float e =   h * (float)Math.sin( sh.mBearing + mDeclination );
          float s = - h * (float)Math.cos( sh.mBearing + mDeclination );
          ts = new TdmStation( sh.mTo, e, s, h*sh.mExtend, v, this );
          mStations.add( ts );
          sh.setTdmStations( fs, ts );
          repeat = true;
        } else {
          // TDLog.v("Shot " + sh.mFrom + " " + ( ( sh.mTo == null )? "-" : sh.mTo ) );
          fs = getStation( sh.mFrom );
          ts = getStation( sh.mTo );
          if ( fs != null ) {
            if ( ts == null ) {  // FROM exists and TO does not exists
              float h = (float)Math.cos( sh.mClino ) * sh.mLength;
              float v = (float)Math.sin( sh.mClino ) * sh.mLength;
              float e =   h * (float)Math.sin( sh.mBearing + mDeclination );
              float s = - h * (float)Math.cos( sh.mBearing + mDeclination );
              ts = new TdmStation( sh.mTo, fs.e+e, fs.s+s, fs.h+h*sh.mExtend, fs.v+v, this );
              mStations.add( ts );
              repeat = true;
            } else {
	      // skip: both shot stations exist
	    }
            sh.setTdmStations( fs, ts );
          } else if ( ts != null ) { // FROM does not exists, but TO exists
            float h = (float)Math.cos( sh.mClino ) * sh.mLength;
            float v = (float)Math.sin( sh.mClino ) * sh.mLength;
            float e =   h * (float)Math.sin( sh.mBearing + mDeclination );
            float s = - h * (float)Math.cos( sh.mBearing + mDeclination );
            fs = new TdmStation( sh.mFrom, ts.e-e, ts.s-s, ts.h-h*sh.mExtend, ts.v-v, this );
            mStations.add( fs );
            sh.setTdmStations( fs, ts );
            repeat = true;
          } else { // the two shot stations do not exist: check equates
	    boolean skip_equate = false;
	    for ( TdmEquate eq : mEquates ) {
	      if ( skip_equate ) break;
	      if ( eq.contains( sh.mFrom ) ) {
		for ( String st : eq.mStations ) if ( ! st.equals( sh.mFrom  ) ) {
		  if ( ( fs = getStation( st ) ) != null ) {
                    float h = (float)Math.cos( sh.mClino ) * sh.mLength;
                    float v = (float)Math.sin( sh.mClino ) * sh.mLength;
                    float e =   h * (float)Math.sin( sh.mBearing + mDeclination );
                    float s = - h * (float)Math.cos( sh.mBearing + mDeclination );
                    ts = new TdmStation( sh.mTo, fs.e+e, fs.s+s, fs.h+h*sh.mExtend, fs.v+v, this );
                    mStations.add( ts );
                    sh.setTdmStations( fs, ts );
		    skip_equate = true;
		    break;
		  }
		}
              } else if ( eq.contains( sh.mTo ) ) {
	        for ( String st : eq.mStations ) if ( ! st.equals( sh.mTo ) ) {
		  if ( ( ts = getStation( st ) ) != null ) {
                    float h = (float)Math.cos( sh.mClino ) * sh.mLength;
                    float v = (float)Math.sin( sh.mClino ) * sh.mLength;
                    float e =   h * (float)Math.sin( sh.mBearing + mDeclination );
                    float s = - h * (float)Math.cos( sh.mBearing + mDeclination );
                    fs = new TdmStation( sh.mFrom, ts.e-e, ts.s-s, ts.h-h*sh.mExtend, ts.v-v, this );
                    mStations.add( fs );
                    sh.setTdmStations( fs, ts );
		    skip_equate = true;
		    break;
                  }
                }
              }
            }
	    if ( skip_equate ) repeat = true;
	  }
        }
      }
    }
  }

}


