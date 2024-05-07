/* @file TDNum.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid centerline computation
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.num;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;

import com.topodroid.TDX.TDInstance;
import com.topodroid.TDX.DBlock;
import com.topodroid.TDX.StationPolicy;
import com.topodroid.TDX.SurveyInfo;
import com.topodroid.TDX.TDandroid;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Comparator;
// import java.util.Locale;
import java.util.HashMap;

public class TDNum
{
  public int nrCompensatedLoops;
  public int nrInaccurateLoops;

  /** cstr: create the numerical centerline
   * @param data     list of survey data
   * @param start    start station
   * @param view     barriers list
   * @param hide     hiding list
   * @param decl     magnetic declination (possibly including the convergence)
   * @param loop_fmt loop closure report format
   * @param midline_only  whether to reduce only the midline (no compensation)
   */
  public TDNum( List< DBlock > data, String start, String view, String hide, float decl, String loop_fmt, boolean midline_only )
  {
    // TDLog.v( "data reduction: decl " + decl + " start " + start );
    mDecl = decl;
    surveyExtend   = true;
    nrCompensatedLoops = 0;
    nrInaccurateLoops  = 0;
    surveyAttached = computeNum( data, start, loop_fmt, midline_only );
    setStationsHide( hide );
    setStationsBarr( view );
  }

  public TDNum( List< DBlock > data, String start, String view, String hide, float decl, String loop_fmt )
  {
    this( data, start, view, hide, decl, loop_fmt, false );
  }

  // public void dump( )
  // {
  //   TDLog.v( "Num Stations:" );
  //   for ( NumStation st : mStations ) {
  //     TDLog.v( "   " + st.name + " S: " + st.s + " E: " + st.e );
  //   }
  //   TDLog.v( "Shots:" );
  //   for ( NumShot sh : mShots ) {
  //     TDLog.v( "   From: " + sh.from.name + " To: " + sh.to.name );
  //   }
  // } 

  // ------------------------------------------------------
  // BOUNDING BOX 
  private double mSmin; // south
  private double mSmax;
  private double mEmin; // east
  private double mEmax;
  private double mHmin; // horizontal
  private double mHmax;

  public float surveyNorth() { return (float)( (mSmin < 0)? -mSmin : 0 ); }
  public float surveySouth() { return (float)( mSmax ); }
  public float surveyWest()  { return (float)( (mEmin < 0)? -mEmin : 0 ); }
  public float surveyEast()  { return (float)( mEmax ); }
  public float surveySmin()  { return (float)( mSmin ); }
  public float surveySmax()  { return (float)( mSmax ); }
  public float surveyEmin()  { return (float)( mEmin ); }
  public float surveyEmax()  { return (float)( mEmax ); }
  public float surveyHmin()  { return (float)( mHmin ); }
  public float surveyHmax()  { return (float)( mHmax ); }
  public float surveyVmin()  { return (float)( mVmin ); }
  public float surveyVmax()  { return (float)( mVmax ); }

  private void resetBBox()
  {
    mSmin  = 0; // clear BBox
    mSmax  = 0;
    mEmin  = 0;
    mEmax  = 0;
    mHmin  = 0;
    mHmax  = 0;
    mTup   = 0;
    mTdown = 0;
    mVmin  = 0;
    mVmax  = 0;
  }

  /** update the bounding box to include a new point
   * @param s    new survey point
   */
  private void updateBBox( NumSurveyPoint s )
  {
    if ( s.s < mSmin ) mSmin = s.s; else if ( s.s > mSmax ) mSmax = s.s;
    if ( s.e < mEmin ) mEmin = s.e; else if ( s.e > mEmax ) mEmax = s.e;
    if ( s.h < mHmin ) mHmin = s.h; else if ( s.h > mHmax ) mHmax = s.h;
    double t = - s.v;
    if ( t < mTdown ) mTdown = t; else if ( t > mTup ) mTup = t;
  }

  // --------------------------------------------------------------------
  // STATISTICS

  /* statistics - not including survey shots */
  private double mVmin;    // Z vertical (downwards)
  private double mVmax;
  private double mTup;     // Z station depth (pos. upwards)
  private double mTdown;   //                 (neg. downwards)
  private double mLength;  // survey length 
  private double mExtLen;  // survey "extended" length (on extended profile)
  private double mProjLen; // survey projected length (on horiz plane)
  private double mUnattachedLength;
  private int mDupNr;  // number of duplicate shots
  private int mSurfNr; // number of surface shots
  private double mInLegErrSum0; // angular error distribution of the data within the legs - accumulators
  private double mInLegErrSum1;
  private double mInLegErrSum2;
  private double mInLegErr1;    // statistics
  private double mInLegErr2;
  private double mLegSigmaSum;  // sum of legs sigmas (accumulator)
  private double mVmaxSurvey;   // V downward
  private double mVminSurvey;
  // private double mVmaxSurface; // not used
  // private double mVminSurface;

  private int mLenCnt;
  private int mLegCnt;

  /** reset the counters for the statistics
   */
  private void resetStats()
  {
    mLenCnt  = 0;
    mLength  = 0;
    mExtLen  = 0;
    mProjLen = 0;
    mUnattachedLength = 0;
    mDupNr   = 0;
    mSurfNr  = 0;
    mInLegErrSum0 = mInLegErrSum1 = mInLegErrSum2 = 0;
    mInLegErr1 = mInLegErr2 = 0;
    mLegSigmaSum = 0;
    mLegCnt = 0;
    mVmaxSurvey  = 0;
    mVminSurvey  = 0;
    // mVmaxSurface = 0;
    // mVminSurface = 0;
  }

  /** add the contribution of the data of a leg to the statistics of in-leg errors
   * @param ts   leg shot
   */
  private void addToInLegError( TriShot ts )
  {
    int size = ts.blocks.size();
    double d2 = 0;
    for ( int i = 0; i < size; ++i ) {
      DBlock blk1 = ts.blocks.get(i);
      for ( int j = i+1; j < size; ++j ) {
        DBlock blk2 = ts.blocks.get(j);
        double e = blk1.relativeAngle( blk2 );
        mInLegErrSum0 += 1;
        mInLegErrSum1 += e;
        mInLegErrSum2 += e*e;

        d2 += blk1.relativeSquareDistance( blk2 );
      }
    }
    if ( d2 > 0 ) {
      mLegSigmaSum += Math.sqrt( d2 );
      mLegCnt ++;
    }
  }

  /** compute the in-leg error (mean and stddev)
   */
  private void computeInLegError()
  {
    if ( mInLegErrSum0 > 0 ) {
      mInLegErr1 = mInLegErrSum1 / mInLegErrSum0;
      mInLegErr2 = Math.sqrt( mInLegErrSum2/mInLegErrSum0 - mInLegErr1*mInLegErr1 );
    }
  }

  /** add data to the counters for the statistics
   * @param d     whether it is duplicate leg
   * @param s     whether it is surface leg
   * @param l     shot length
   * @param e     shot extent
   * @param h     shot horizontal extent
   */
  private void addToStats( boolean d, boolean s, double l, double e, double h )
  {
    if ( d ) ++mDupNr;
    if ( s ) ++mSurfNr;
    if ( ! ( d || s ) ) {
      mLength  += l;
      mExtLen  += e;
      mProjLen += h;
      mLenCnt ++;
    }
  }

  /** add data to the counters for the statistics
   * @param d     whether it is duplicate leg
   * @param s     whether it is surface leg
   * @param l     shot length
   * @param e     shot extent
   * @param h     shot horizontal extent
   * @param v     vertical value
   */
  private void addToStats( boolean d, boolean s, double l, double e, double h, double v )
  {
    if ( d ) ++mDupNr;
    if ( s ) ++mSurfNr;
    if ( ! ( d || s ) ) {
      mLength  += l;
      mExtLen  += e;
      mProjLen += h;
      if ( v < mVmin ) { mVmin = v; }
      if ( v > mVmax ) { mVmax = v; }
      mLenCnt ++;
    }
  }

  /** update vertical stats - for splays
   * @param v   vertical coord of splay endpoimt
   */
  private void addToVStats( double v )
  {
    if ( v > mVmaxSurvey ) { mVmaxSurvey = v; } else if ( v < mVminSurvey ) { mVminSurvey = v; }
  }

  /** add station Z to stats
   * @param ns station
   * @param reduction_type station reduction type
   */
  private void addToStat( NumStation ns, int reduction_type )
  {
    double v = ns.v;
    switch ( reduction_type ) { 
      case NumStation.STATION_SURVEY:
        addToVStats( v );
        // if ( v > mVmaxSurvey ) { mVmaxSurvey = v; } else if ( v < mVminSurvey ) { mVminSurvey = v; }
        break;
      // case NumStation.STATION_SURFACE:
      //   if ( v > mVmaxSurface ) { mVmaxSurface = v; } else if ( v < mVminSurface ) { mVminSurface = v; }
      //   break;
    }
  }

  /** @return the number of stations
   */
  public int stationsNr()  { return mStations.size(); }

  /** @return the number of (leg) shots
   */
  public int shotsNr()     { return mShots.size(); }

  /** @return the number of duplicate (leg) shots
   */
  public int duplicateNr() { return mDupNr; }

  /** @return the number of surface (leg) shots
   */
  public int surfaceNr()   { return mSurfNr; }

  /** @return the number of splays
   */
  public int splaysNr()    { return mSplays.size(); }

  /** @return the number of loops
   */
  public int loopNr()      { return mClosures.size(); }

  /** @return the number of unattached (leg) shots
   */
  public int unattachedShotsNr() { return mUnattachedShots.size(); }

  /** @return the length of the survey midline 
   */
  public float surveyLength()     { return (float)mLength; }

  /** @return the length of the survey extent line 
   */
  public float surveyExtLen()     { return (float)mExtLen; }

  /** @return the length of the survey horizontal extent line 
   */
  public float surveyProjLen()    { return (float)mProjLen; }

  public float surveyTop()        { return (float)mTup; }   // top must be positive

  public float surveyBottom()     { return (float)mTdown; } // bottom must be negative

  public float surveyZmax()       { return -(float)mVminSurvey; }
  public float surveyZmin()       { return -(float)mVmaxSurvey; }

  // public float surfaceZmax()       { return -(float)mVminSurface; }
  // public float surfaceZmin()       { return -(float)mVmaxSurface; }

  /** @return the length of the survey unattached legs
   */
  public float unattachedLength() { return (float)mUnattachedLength; }

  /** @return the mean angle error [radians]
   */
  public float angleErrorMean()   { return (float)mInLegErr1; }

  /** @return the std-dev of the angle error [radians]
   */
  public float angleErrorStddev() { return (float)mInLegErr2; }

  /** @return the average sigma of in-leg shots [m]
   */
  public float legSigma() { return (float)mLegSigmaSum/mLegCnt; }

  // -------------------------------------------------------
  // SURVEY DATA 

  private NumStation mStartStation; // origin station
  private float      mDecl;         // magnetic declination (possibly including -convergence)

  private final Object mShotLock = new Object(); // 20230118 new, to synchronize mShots

  private NumStationSet mStations;
  private ArrayList< NumStation > mClosureStations;
  private ArrayList< NumShot >    mShots;
  private ArrayList< NumSplay >   mSplays;
  private ArrayList< NumClosure > mClosures;
  private ArrayList< NumNode >    mNodes;
  private ArrayList< DBlock >     mUnattachedShots;
  private ArrayList< NumCycle >   mBadLoops;

  public String getOriginStation() { return (mStartStation == null)? null : mStartStation.name; }
  public NumStation getOrigin()    { return mStartStation; }

  public boolean surveyAttached; //!< whether the survey is attached
  public boolean surveyExtend;

  public List< NumStation > getStations() { return mStations.getStations(); }
  public List< NumStation > getClosureStations() { return mClosureStations; }
  public List< NumShot >    getShots()    { return mShots; }
  public List< NumSplay >   getSplays()   { return mSplays; }
  public List< NumClosure > getClosures() { return mClosures; }
  public List< DBlock >     getUnattached() { return mUnattachedShots; }
  public List< NumCycle >   getBadLoops() { return mBadLoops; }

  /** @return the magnetic declination (possibly with -convergence)
   */
  public float getDeclination() { return mDecl; }

  /** @return the list of splays at a given station
   * @param st    station
   * @note only splays FROM station is considered
   */
  public List< NumSplay >   getSplaysAt( NumStation st )
  {
    ArrayList< NumSplay > ret = new ArrayList<>();
    for ( NumSplay splay : mSplays ) {
      if ( splay.getBlock().isSplay() && st == splay.from ) {
        ret.add( splay );
      }
    }
    return ret;
  }

  /** @return the splay of a given data-block, or null if not found
   * @param blk   data block
   */
  public NumSplay getSplayOf( DBlock blk )
  {
    long bid = blk.mId;
    for ( NumSplay splay : mSplays ) {
      if ( splay.getBlock().mId == bid ) return splay;
    }
    return null;
  }

  /** drop the last splay
   * @note for the incremental update
   */
  public void dropLastSplay()
  {
    int sz = mSplays.size();
    if ( sz > 0 ) mSplays.remove( sz - 1 );
  }

  /** @return the last splay
   * @note for the incremental update
   */
  public NumSplay getLastSplay() 
  {
    int sz = mSplays.size();
    return ( sz == 0 )? null : mSplays.get( sz - 1 );
  }

  /** @return the last shot
   * @note for the incremental update
   */
  public NumShot getLastShot()
  {
    NumShot ret = null;
    synchronized( mShotLock ) {
      int sz = mShots.size();
      if ( sz > 0 ) ret = mShots.get( sz - 1 );
    }
    return ret;
  }

  /** @return the list of the shots at station st, except shot [st,except]
   * @param st     given AT station, either FROM or TO
   * @param except excluded station, respectively To or FROM
   */
  public List< NumShot > getShotsAt( NumStation st, NumStation except )
  {
    ArrayList< NumShot > ret = new ArrayList<>();
    synchronized( mShotLock ) {
      for ( NumShot shot : mShots ) {
        if ( ( shot.from == st && shot.to   != except )
          || ( shot.to   == st && shot.from != except ) ) {
          ret.add( shot );
        }
      }
    }
    return ret;
  }

  /** @return the station closest to a 2D point (x,y)
   * @param type   plot type
   * @param x      point X coord
   * @param y      point Y coord
   */
  public NumStation getClosestStation( long type, double x, double y ) { return mStations.getClosestStation( type, x, y ); }

  /** set a station either hidden or shown
   * @param name   station name
   * @param hide   whether to hide or show: +1 to hide, -1 to show
   *
   * 20230318 fixed:
   * there was a problem here:               ,-----B---
   * if the reduction tree has a branch, say 0----A
   *                                               `---C----D
   * when B, C are both hidden the left side of the tree is not shown.
   * If B gets un-hidden the line 0--A--B gets shown as well as C---D
   * and these two pieces remain separated.
   */
  public void setStationHidden( String name, int hide )
  {
    // TDLog.v( "Set Station Hidden: " + hide );
    NumStation st = getStation( name );
    if ( st == null ) return;
    st.mBarrierAndHidden = ( st.mHidden == -1 && hide == 1 );
    st.mHidden += hide;
    // TDLog.v( "station " + st.name + " hide " + st.mHidden );
    hide *= 2;
    // st = st.parent(); // 20230318 fixed
    // while ( st != null ) {
    //   st.mHidden += hide;
    //   if ( st.mHidden < 0 ) st.mHidden = 0;
    //   // TDLog.v( "station " + st.name + " hide " + st.mHidden );
    //   st = st.parent();
    // }
    if ( mStartStation.name.equals( name ) ) return;
    Stack<NumStation> stack = new Stack<NumStation>();
    stack.push( mStartStation );
    // TDLog.v("push " + mStartStation.name );
    while ( ! stack.empty() ) {
      st = stack.pop();
      // TDLog.v("pop " + st.name );
      if ( ! st.name.equals( name ) ) {
        st.mHidden += hide;
        if ( st.sibling() != null ) {
          stack.push( st.sibling() );
          // TDLog.v("push sibling " + st.sibling().name );
        }
        if ( st.child() != null ) {
          stack.push( st.child() );
          // TDLog.v("push child " + st.child().name );
        }
      }
    }    
  }

  /** set the hidden stations
   * @param hide   string with the names of the hidden stations
   */
  private void setStationsHide( String hide )
  {
    if ( hide == null ) return;
    String[] names = hide.split(" ");
    for ( int k=0; k<names.length; ++k ) {
      if ( names[k].length() > 0 ) setStationHidden( names[k], 1 );
    }
  }

  /** set a station either barrier or non-barrier
   * @param name    station name
   * @param barrier whether to barrier or not: +1 to barrier, -1 not to barrier
   */
  public void setStationBarrier( String name, int barrier )
  {
    // TDLog.v( "Set Station barrier: " + barrier );
    NumStation st = getStation( name );
    if ( st == null ) return;
    // TDLog.v( "station " + st.name + " barr " + st.mHidden );
    st.mBarrierAndHidden = ( st.mHidden == 1 && barrier == 1 );
    st.mHidden -= barrier;
    if ( mStartStation == st ) return;
    barrier *= 2;
    // stack.push( st ); // 20230318 fixed
    // while ( ! stack.empty() ) {
    //   st = stack.pop();
    //   mStations.updateHidden( st, -barrier, stack );
    // }
    if ( st.child() != null ) {
      Stack<NumStation> stack = new Stack<NumStation>();
      stack.push( st.child() );
      // TDLog.v("push " + st.name );
      while ( ! stack.empty() ) {
        st = stack.pop();
        // TDLog.v("pop " + st.name );
        st.mHidden -= barrier;
        if ( st.sibling() != null ) {
          stack.push( st.sibling() );
          // TDLog.v("push sibling " + st.sibling().name );
        }
        if ( st.child() != null ) {
          stack.push( st.child() );
          // TDLog.v("push child " + st.child().name );
        }
      }
    }
  }

  /** set the barrier stations
   * @param barr   string with the names of the barrier stations
   */
  private void setStationsBarr( String barr )
  {
    if ( barr == null ) return;
    String[] names = barr.split(" ");
    for ( int k=0; k<names.length; ++k ) {
      if ( names[k].length() > 0 ) setStationBarrier( names[k], 1 );
    }
  }

  /** @return true if the station is hidden
   * @param name  station name
   */
  public boolean isHidden( String name )
  {
    NumStation st = getStation( name );
    return ( st != null && st.hidden() );
  }

  /** @return true if the station is barrier
   * @param name  station name
   */
  public boolean isBarrier( String name )
  {
    NumStation st = getStation( name );
    return ( st != null && st.barrier() );
  }

  // @note for the shot FROM-TO
  public int canBarrierHidden( String from, String to )
  {
    int has_shot = hasShot( from, to );
    if ( has_shot == 0 ) return 0;
    int ret = 0;
    if ( has_shot == 1 ) {
      if ( ! isHidden( from ) )  ret |= 0x02;
      if ( ! isBarrier( to ) )   ret |= 0x04;
    } else { // has_shot == -1
      if ( ! isBarrier( from ) ) ret |= 0x01;
      if ( ! isHidden( to ) )    ret |= 0x08;
    }
    return ret;
  }

  /** @return the station from the ID (name)
   * @param id    station ID (name)
   */
  public NumStation getStation( String id ) 
  {
    if ( id == null ) return null;
    return mStations.getStation( id );
    // for (NumStation st : mStations ) if ( id.equals(st.name) ) return st;
    // return null;
  }

  /** @return get the shot between two stations
   * @param s1   first station name
   * @param s2   second station name
   */
  public NumShot getShot( String s1, String s2 )
  {
    if ( s1 == null || s2 == null ) return null;
    NumShot ret = null;
    synchronized( mShotLock ) {
      for (NumShot sh : mShots ) {
        if ( s1.equals( sh.from.name ) && s2.equals( sh.to.name ) ) { ret = sh; break; }
        if ( s2.equals( sh.from.name ) && s1.equals( sh.to.name ) ) { ret = sh; break; }
      }
    }
    return ret;
  }

  /** @return get the shot between two stations
   * @param st1   first station
   * @param st2   second station
   */
  public NumShot getShot( NumStation st1, NumStation st2 )
  {
    if ( st1 == null || st2 == null ) return null;
    NumShot ret = null;
    synchronized( mShotLock ) {
      for (NumShot sh : mShots ) {
        if ( ( st1 == sh.from && st2 == sh.to ) || ( st2 == sh.from && st1 == sh.to ) ) { ret = sh; break; }
      }
    }
    return ret;
  }

  /** check if there is the shot between two stations
   * @param s1   first station name
   * @param s2   second station name
   * @return +1 if has shot s1-s2
   *         -1 if has shot s2-s1
   *          0 otherwise
   */
  private int hasShot( String s1, String s2 )
  {
    if ( s1 == null || s2 == null ) return 0;
    int dir = 0;
    synchronized( mShotLock ) {
      for (NumShot sh : mShots ) {
        if ( s1.equals( sh.from.name ) && s2.equals( sh.to.name ) ) { dir =  sh.mDirection; break; }
        if ( s2.equals( sh.from.name ) && s1.equals( sh.to.name ) ) { dir = -sh.mDirection; break; }
      }
    }
    return dir;
  }

  // ==========================================================================
  // latest data circular buffer
  private static class DBlockBuffer
  {
    int N;
    int pos;
    DBlock[] mBlk;
  
    DBlockBuffer( int n ) 
    {
      N = n;
      pos = -1;
      mBlk = new DBlock[ N ];
      for ( n=0; n<N; ++n ) mBlk[n] = null;
    }
  
    void put( DBlock blk ) { pos = (pos+1)%N; mBlk[pos] = blk; }
   
    DBlock get( ) 
    { 
      DBlock ret = mBlk[pos];
      mBlk[pos] = null;
      pos = (pos+N-1)%N;
      return ret;
    }
  }

  private DBlockBuffer mBuffer = new DBlockBuffer( 4 );

  private TriSplay     mLastSplay;
  private TriShot      mLastLeg;      // last leg

  /** insert a new shot into the survey
   * @param blk       new shot
   * @param leg       ???
   * @param loop_fmt  loop closure report format
   * @return true if the shot has been appended
   * @note the new shot is assumed not to close any loop
   */
  public boolean appendData( DBlock blk, DBlock leg, String loop_fmt )
  {
    if ( blk == null ) return false;
    mBuffer.put( blk );

    if ( leg != null && mLastSplay != null ) {
      // TDLog.v( "num got_leg ");
      removeSplay( mLastSplay );
      appendLeg( mLastSplay.block, leg, loop_fmt );
      mLastSplay = null;
    } else {
      if ( blk.isSplay() ) {
        // if ( mLastLeg != null ) {
        //   // TDLog.v( "num insert leg ");
        //   insertLeg( mLastLeg, loop_fmt );
        // }
        mLastLeg = null;  // clear last-leg
        TriSplay splay = null;
        if ( blk.mFrom != null && blk.mFrom.length() > 0 ) { // normal splay
          splay = new TriSplay( blk, blk.mFrom, blk.getIntExtend(), +1 );
          mLastSplay = splay;
        } else if ( blk.mTo != null && blk.mTo.length() > 0 ) { // reversed splay
          splay = new TriSplay( blk, blk.mTo, blk.getIntExtend(), -1 );
          mLastSplay = splay;
        }
        // TDLog.v( "num append SPLAY " + blk.mId + " splays " + mSplays.size() );
        return insertSplay( splay ); // add splay to network (null is checked by the routine)
      } else if ( blk.isSecLeg() ) {
        // TDLog.v( "num append SEC-LEG " + blk.mId );
        // if (mLastLeg == null) return false;
        // mLastLeg.addBlock( blk );
        return false;
      } else if ( blk.isTypeBlank() ) {
        // TDLog.v( "num append BLANK " + blk.mId );
        // if (mLastLeg == null || ! blk.isRelativeDistance( mLastLeg.getFirstBlock() ) ) return false;
        // mLastLeg.addBlock( blk );
        return false;
      } else {
        TDLog.Error("num: append LEG " + blk.mId + " should not happen ");
        // return appendLeg( blk );
        return false;
      }
    }
    return true;
  }

  /** append a leg to the survey network (?)
   * @param blk       new shot
   * @param leg       ???
   * @param loop_fmt  loop closure report format
   */
  private boolean appendLeg( DBlock blk, DBlock leg, String loop_fmt ) 
  {
    if ( leg.isMainLeg() ) {
      // TDLog.v( "num append with leg " + leg.mId + " <" + leg.mFrom + "-" + leg.mTo + ">" );
      mLastLeg = new TriShot( leg, leg.mFrom, leg.mTo, leg.getIntExtend(), leg.getStretch(), +1 );
      mLastLeg.duplicate = ( leg.isDuplicate() );
      mLastLeg.surface   = ( leg.isSurface() );
      mLastLeg.commented = ( leg.isCommented() ); // FIXME_COMMENTED
      // mLastLeg.backshot  = 0;
      if ( leg.getIntExtend() > 1 ) surveyExtend = false;
      addToInLegError( mLastLeg );
      computeInLegError();
    } else if ( leg.isBackLeg() ) {
      // TDLog.v( "num append with backleg " + leg.mId + " <" + leg.mFrom + "-" + leg.mTo + ">" );
      mLastLeg = new TriShot( leg, leg.mFrom, leg.mTo, leg.getIntExtend(), leg.getStretch(), +1 );
      mLastLeg.duplicate = true;
      mLastLeg.surface   = ( leg.isSurface() );
      mLastLeg.commented = false;
      // mLastLeg.backshot  = 0;
      if ( leg.getIntExtend() > 1 ) surveyExtend = false;
      addToInLegError( mLastLeg );
      computeInLegError();
    } else {
      TDLog.Error("num: append should not happen " + leg.mId + " <" + leg.mFrom + "-" + leg.mTo + ">" );
      return false; // should not happen
    }
    DBlock blk1 = mBuffer.get();
    for ( ; blk1 != null && blk1 != blk; blk1 = mBuffer.get() ) {
      mLastLeg.addBlock( blk1 );
    }
    insertLeg( mLastLeg, loop_fmt );
    return true;
  }

  void addClosure( NumClosure closure ) { mClosures.add( closure ); }

  /** insert a leg shot
   * @param ts       leg shot
   * @param shot_fmt shot report format
   */
  private void insertLeg( TriShot ts, String shot_fmt )
  {
    float anomaly = StationPolicy.doMagAnomaly() ? compensateMagneticAnomaly( ts ) : 0;
    // try to see if any temp-shot station is on the list of stations
    NumStation sf = getStation( ts.from );
    NumStation st = getStation( ts.to );
    int  i_ext = DBlock.getIntExtend( ts.extend ); // integer extend
    float f_ext = DBlock.getReducedExtend( ts.extend, ts.stretch ); // float extend - used for station coords
    float a_ext = f_ext; // station azimuth extends
    if ( sf != null ) {
      sf.addAzimuth( ts.b(), a_ext );
      if ( st != null ) { // loop-closure -: need the loop length to compute the fractional closure error
        // do close loop also on duplicate shots
        if ( shot_fmt != null ) {
          ArrayList< NumShot > shots = getShortestPathShots( sf, st );
          ArrayList< NumShortpath > paths = new ArrayList<>();
          mStations.initShortestPath( paths, 1000000.0f );
          (new ClosureTask( this, shot_fmt, shots, paths, sf, st, ts.d(), ts.b(), ts.c() )).execute();
        }
        if ( /* TDSetting.mAutoStations || */ TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) { // do not close loop
          addOpenLoopShot( sf, ts, i_ext, a_ext, f_ext, anomaly ); // keep loop open: new station( id=ts.to, from=sf, ... )
        } else { // TDLog.v( "close loop at " + sf.name + " " + st.name );
          NumShot sh = makeShotFromTmp( sf, st, ts, 0, sf.mAnomaly, mDecl ); 
          addShotToStations( sh, sf, st );
        }
        // float length = ts.d();
        // if ( i_ext == 0 ) length = TDMath.sqrt( length*length - ts.h()*ts.h() );
        addToStats( ts.duplicate, ts.surface, ts.d(), ((i_ext == 0)? Math.abs(ts.v()) : ts.d()), ts.h() );
      }
      else // st null || st isBarrier
      { // forward shot: from --> to
        addForwardShot( sf, ts, i_ext, a_ext, f_ext, anomaly );
      }
    }
    else if ( st != null ) 
    { // sf == null: reversed shot only difference is '-' sign in new NumStation, and the new station is sf
      addReversedShot( st, ts, i_ext, a_ext, f_ext, anomaly );
    }
  }

  /** make a NumShot from a temporary shot
   * @param sf    from station
   * @param st    to station
   * @param ts    temp shot
   * @param direction ???
   * @param anomaly   magnetic anomaly angle
   * @param decl      magnetic declination
   */
  private NumShot makeShotFromTmp( NumStation sf, NumStation st, TriShot ts, int direction, float anomaly, float decl )
  {
    if ( ts.reversed != 1 ) {
      TDLog.Error( "making shot from reversed temp " + sf.name + " " + st.name );
    }
    DBlock blk = ts.getFirstBlock();
    // TDLog.v( "make shot " + sf.name + "-" + st.name + " blocks " + ts.blocks.size() + " E " + blk.getIntExtend() + " S " + blk.getStretch() );
    // NumShot sh = new NumShot( sf, st, ts.getFirstBlock(), 1, anomaly, decl ); // FIXME DIRECTION
    NumShot sh = new NumShot( sf, st, ts.getFirstBlock(), direction, anomaly, decl );
    ArrayList< DBlock > blks = ts.getBlocks();
    for ( int k = 1; k < blks.size(); ++k ) {
      sh.addBlock( blks.get(k) );
    }
    return sh;
  }

  private NumShot makeShotFromTmpDiving( NumStation sf, NumStation st, TriShot ts, int direction, float anomaly, float decl, float tdepth )
  {
    if ( ts.reversed != 1 ) {
      TDLog.Error( "making shot from reversed temp " + sf.name + " " + st.name );
    }
    DBlock blk = ts.getFirstBlock();
    // TDLog.v( "make shot " + sf.name + "-" + st.name + " blocks " + ts.blocks.size() + " E " + blk.getIntExtend() + " S " + blk.getStretch() );
    // NumShot sh = new NumShot( sf, st, ts.getFirstBlock(), 1, anomaly, decl ); // FIXME DIRECTION
    NumShot sh = new NumShot( sf, st, ts.getFirstBlock(), direction, anomaly, decl );
    ArrayList< DBlock > blks = ts.getBlocks();
    for ( int k = 1; k < blks.size(); ++k ) {
      sh.addBlock( blks.get(k) );
    }
    return sh;
  }

  /** initialize the list of shots and splays from the list of survey data
   * @param data     survey data
   * @param shots    (temporary) legs
   * @param splays   (temporary) splays
   * @param complete initialize all shots, not only midline
   * @note mLastLeg remains after the shots have been initialized
   */
  private void initShots( List< DBlock > data, List< TriShot > shots, List< TriSplay > splays, boolean complete )
  {
    mLastLeg = null;
    for ( DBlock blk : data ) {
      // TDLog.v( "NUM blk type " + blk.mType );
      if ( blk.isSplay() && complete ) {
        mLastLeg = null;  // clear last-leg
        if ( blk.mFrom != null && blk.mFrom.length() > 0 ) { // normal splay
          splays.add( new TriSplay( blk, blk.mFrom, blk.getIntExtend(), +1 ) );
        } else if ( blk.mTo != null && blk.mTo.length() > 0 ) { // reversed splay
          splays.add( new TriSplay( blk, blk.mTo, blk.getIntExtend(), -1 ) );
        }
      } else if ( blk.isMainLeg() ) {
        mLastLeg = new TriShot( blk, blk.mFrom, blk.mTo, blk.getIntExtend(), blk.getStretch(), +1 );
        mLastLeg.duplicate = ( blk.isDuplicate() );
        mLastLeg.surface   = ( blk.isSurface() );
        mLastLeg.commented = ( blk.isCommented() ); // FIXME_COMMENTED
        // mLastLeg.backshot  = 0;
        if ( blk.getIntExtend() > 1 ) surveyExtend = false;
        shots.add( mLastLeg );
      } else if ( blk.isBackLeg() ) {
        mLastLeg = new TriShot( blk, blk.mFrom, blk.mTo, blk.getIntExtend(), blk.getStretch(), +1 );
        mLastLeg.duplicate = true;
        mLastLeg.surface   = ( blk.isSurface() );
        mLastLeg.commented = false;
        // mLastLeg.backshot  = 0;
        if ( blk.getIntExtend() > 1 ) surveyExtend = false;
        shots.add( mLastLeg );
      } else if ( blk.isSecLeg() ) {
        if (mLastLeg != null) mLastLeg.addBlock( blk );
      } else if ( blk.isTypeBlank() ) {
        if (mLastLeg != null ) {
          if ( blk.isRelativeDistance( mLastLeg.getFirstBlock() ) ) {
            mLastLeg.addBlock( blk );
          }
        }
      }
    }
  }

  /** survey data reduction 
   * @param data   shot list
   * @param start  start station
   * @param path_fmt path report format
   * @param midline_only  whether to reduce only the midline (no compensation)
   * @return true if all shots are attached
   */
  private boolean computeNum( List< DBlock > data, String start, String path_fmt, boolean midline_only )
  {
    if ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING ) { // preprocess: convert diving-mode data to normal form
      HashMap< String, Float > depths = new HashMap< String, Float >();
      for ( DBlock blk : data ) { // prepare stations depths
	if ( blk.mFrom != null && blk.mFrom.length() > 0 && blk.mTo != null && blk.mTo.length() > 0 ) {
          // TDLog.v( blk.mFrom + " depth " + blk.mDepth );
          // depths.putIfAbsent( blk.mFrom, new Float( blk.mDepth ) );
          if ( ! depths.containsKey(blk.mFrom) ) depths.put( blk.mFrom, Float.valueOf( blk.mDepth ) );
        }
      }
      // boolean depth_error = false;
      // String error = TDString.EMPTY;
      for ( DBlock blk : data ) { // set dblock clino
	if ( blk.mTo != null && blk.mTo.length() > 0 && depths.containsKey( blk.mTo ) ) {
          float tdepth = depths.get( blk.mTo ).floatValue(); // FIXME may null pointer
	  if ( ! blk.makeClino( tdepth ) ) {
	    // depth_error = true;
	    TDLog.Error("Failed make clino: " +  blk.mFrom + "-" + blk.mTo + " (" + tdepth + ") " );
	  }
        }
      }
      // if ( depth_error ) {
      //   TDToast.make( R.string.depth_error );
      // }
    }

    resetBBox();
    resetStats();
    mStartStation = null;
    int nrSiblings = 0;

    // long millis_start = System.currentTimeMillis();
    
    mStations = new NumStationSet();
    mClosureStations = new ArrayList<>();
    mShots    = new ArrayList<>();
    mSplays   = new ArrayList<>();
    mClosures = new ArrayList<>();
    mNodes    = new ArrayList<>();
    mUnattachedShots = new ArrayList<>();
    mBadLoops = new ArrayList<>();

    List< TriShot > tmp_shots   = new ArrayList<>();
    List< TriSplay > tmp_splays = new ArrayList<>();

    initShots( data, tmp_shots, tmp_splays, ! midline_only );
    // TDLog.Log( TDLog.LOG_NUM, "data " + data.size() + " shots " + tmp_shots.size() + " splays " + tmp_splays.size() );

    if ( ! midline_only && TDSetting.mLoopClosure == TDSetting.LOOP_TRIANGLES ) {
      makeTrilateration( tmp_shots );
    }

    // ---------------------------------- SIBLINGS and BACKSIGHT -------------------------------
    for ( TriShot tsh : tmp_shots ) { // clear backshot, sibling, and multibad
      tsh.backshot = 0;    
      tsh.sibling  = null; // link to sibling shots
      tsh.getFirstBlock().setMultiBad( false );
    }

    for ( int i = 0; i < tmp_shots.size(); ++i ) {
      TriShot ts0 = tmp_shots.get( i );
      addToInLegError( ts0 );
      if ( ts0.backshot != 0 ) continue; // skip siblings

      DBlock blk0 = ts0.getFirstBlock();
      // (1) check if ts0 has siblings
      String from = ts0.from;
      String to   = ts0.to;
      // if ( from == null || to == null ) continue; // FIXME
      TriShot ts1 = ts0; // last sibling (head = the shot itself)
      for ( int j=i+1; j < tmp_shots.size(); ++j ) {
        TriShot ts2 = tmp_shots.get( j );
        if ( from.equals( ts2.from ) && to.equals( ts2.to ) ) { 
          // TDLog.v( "chain a positive sibling " + from + " " + to );
          ts1.sibling = ts2;
          ts1 = ts2;
          ts2.backshot = 1;
	  ++ nrSiblings;
        } else if ( from.equals( ts2.to ) && to.equals( ts2.from ) ) { 
          // TDLog.v( "chain a negative sibling " + from + " " + to );
          ts1.sibling = ts2;
          ts1 = ts2;
          ts2.backshot = -1;
	  ++ nrSiblings;
        }
      }

      if ( ts0.sibling != null ) { 
        // TDLog.v( "check sibling shots agreement " + ts0.from + " " + ts0.to );
        float dmax = 0.0f;
        float cc = TDMath.cosd( blk0.mClino );
        float sc = TDMath.sind( blk0.mClino );
        float cb = TDMath.cosd( blk0.mBearing + mDecl ); 
        float sb = TDMath.sind( blk0.mBearing + mDecl ); 
        TDVector v1 = new TDVector( blk0.mLength * cc * sb, blk0.mLength * cc * cb, blk0.mLength * sc );
        ts1 = ts0.sibling;
        while ( ts1 != null ) {
          DBlock blk1 = ts1.getFirstBlock();
          cc = TDMath.cosd( blk1.mClino );
          sc = TDMath.sind( blk1.mClino );
          cb = TDMath.cosd( blk1.mBearing + mDecl ); 
          sb = TDMath.sind( blk1.mBearing + mDecl ); 
          TDVector v2 = new TDVector( blk1.mLength * cc * sb, blk1.mLength * cc * cb, blk1.mLength * sc );
          float d = ( ( ts1.backshot == -1 )? v1.plus(v2) : v1.minus(v2) ).length();
          d = d/blk0.mLength + d/blk1.mLength; 
          if ( d > dmax ) dmax = d;
          ts1 = ts1.sibling;
        }
        if ( ( ! StationPolicy.doMagAnomaly() ) && ( dmax > TDSetting.mSiblingThrD /* TDSetting.mCloseDistance */ ) ) {
          // TDLog.v("Bad Block " + blk0.mFrom + " " + blk0.mTo + " dist " + dmax + " CloseDist " + TDSetting.mCloseDistance + " Sibling " + TDSetting.mSiblingThrD );
          blk0.setMultiBad( true );
        }
        
        if ( ! StationPolicy.doMagAnomaly() ) { // (3) remove siblings
          ts1 = ts0.sibling;
          while ( ts1 != null ) { // TDLog.v( "NUM removing sibling " + ts1.from + "-" + ts1.to + " : " + nrSiblings );
            -- nrSiblings;
            TriShot ts2 = ts1.sibling;
            tmp_shots.remove( ts1 );
            ts1 = ts2;
          }
          ts0.sibling = null;
        // } else {
        //   for ( ts1 = ts0.sibling; ts1 != null; ts1 = ts1.sibling ) {
        //     assert ( ts1.backshot != 0 );
        //     // TDLog.v( from + "-" + to  + " zero backshot sibling " + ts1.from + "-" + ts1.to );
        //   }
        }
      }
    }

    // TDLog.v( "compute leg error ...");
    if ( ! midline_only ) computeInLegError();

    // ---------------------------------- DATA REDUCTION -------------------------------
    mStartStation = new NumStation( start /*, NumStation.STATION_ORIGIN */ );
    // addToStat( mStartStation ); // useless
    mStartStation.setHasExtend( true );
    mStations.addStation( mStartStation );

    // two-pass data reduction
    // first-pass all shots with regular extends
    // second-pass any leftover shot
    for ( int pass = 0; pass < 2; ++ pass ) {
      // TDLog.Log( TDLog.LOG_NUM, "data reduction pass " + pass );
      boolean repeat = true;
      while ( repeat ) {
        repeat = false;
        for ( TriShot ts : tmp_shots ) {
          if ( ts.used || ts.backshot != 0 ) continue;                  // skip used and siblings
          if ( pass == 0 && DBlock.getIntExtend(ts.extend) > 1 ) continue; // first pass skip non-extended

          // float anomaly = 0;
          // if ( StationPolicy.doMagAnomaly() ) {
          //   anomaly = compensateMagneticAnomaly( ts, anomaly );
          // } 
          float anomaly = StationPolicy.doMagAnomaly() ? compensateMagneticAnomaly( ts ) : 0;

          // try to see if any temp-shot station is on the list of stations
          NumStation sf = getStation( ts.from );
          NumStation st = getStation( ts.to );

          int  i_ext = DBlock.getIntExtend( ts.extend ); // integer extend
          float f_ext = DBlock.getReducedExtend( ts.extend, ts.stretch ); // float extend - used for station coords
          float a_ext = f_ext; // station azimuth extends
          if ( sf != null ) {
            sf.addAzimuth( ts.b(), a_ext );
            if ( st != null ) { // loop-closure -: need the loop length to compute the fractional closure error
              // do close loop also on duplicate shots
	      if ( path_fmt != null ) {
                ArrayList< NumShot > shots = getShortestPathShots( sf, st );
                ArrayList< NumShortpath > paths = new ArrayList<>();
                mStations.initShortestPath( paths, 1000000.0f );
                (new ClosureTask( this, path_fmt, shots, paths, sf, st, ts.d(), ts.b(), ts.c() )).execute();
              }
              if ( /* TDSetting.mAutoStations || */ TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) { // do not close loop
                addOpenLoopShot( sf, ts, i_ext, a_ext, f_ext, anomaly ); // keep loop open: new station( id=ts.to, from=sf, ... )
              } else { // TDLog.v( "close loop at " + sf.name + " " + st.name );
                NumShot sh = makeShotFromTmp( sf, st, ts, 0, sf.mAnomaly, mDecl ); 
                addShotToStations( sh, sf, st );
              }
              // float length = ts.d();
	      // if ( i_ext == 0 ) length = TDMath.sqrt( length*length - ts.h()*ts.h() );
              addToStats( ts.duplicate, ts.surface, ts.d(), ((i_ext == 0)? Math.abs(ts.v()) : ts.d()), ts.h() );
              ts.used = true;
              repeat = true;
            }
            else // st null || st isBarrier
            { // forward shot: from --> to
              addForwardShot( sf, ts, i_ext, a_ext, f_ext, anomaly );
              ts.used = true;
              repeat = true;
            }
          }
          else if ( st != null ) 
          { // sf == null: reversed shot only difference is '-' sign in new NumStation, and the new station is sf
            addReversedShot( st, ts, i_ext, a_ext, f_ext, anomaly );
            ts.used = true;
            repeat = true;
          }
        }
      }
    }

    if ( midline_only ) {
      return (mShots.size() + nrSiblings == tmp_shots.size() );
    }
    // ---------------------------------- LOOP CLOSURE -------------------------------
    if ( TDSetting.mLoopClosure == TDSetting.LOOP_CYCLES
      || TDSetting.mLoopClosure == TDSetting.LOOP_WEIGHTED 
      || TDSetting.mLoopClosure == TDSetting.LOOP_SELECTIVE ) {
      // TDLog.v( "NUM loop closure compensation " + TDSetting.mLoopClosure );
      compensateLoopClosure( mNodes, mShots );
  
      // recompute station positions
      synchronized( mShotLock ) {
        for ( NumShot sh1 : mShots ) {
          sh1.mUsed = false;
        }
      }
      mStations.reset3DCoords( );       // reset the stations "Has 3D Coords" to false
      mStartStation.setHas3DCoords( );  // except for the start station 

      synchronized( mShotLock ) {
        boolean repeat = true;
        while ( repeat ) {
          repeat = false;
          for ( NumShot sh2 : mShots ) {
            if ( sh2.mUsed ) continue;
            NumStation s1 = sh2.from;
            NumStation s2 = sh2.to;
            float c2 = sh2.clino();
            float b2 = sh2.bearing(); // 20200503 bearing() already has declination; was + mDecl;
            if ( s1.has3DCoords() && ! s2.has3DCoords() ) { // reset s2 values from the shot
              // float d = sh2.length() * sh2.mDirection; // FIXME DIRECTION
              float d = sh2.length();
              double v = - d * TDMath.sinDd( c2 );
              double h =   d * TDMath.cosDd( c2 );
              double e =   h * TDMath.sinDd( b2 );
              double s = - h * TDMath.cosDd( b2 );
              s2.e = s1.e + e;
              s2.s = s1.s + s;
              s2.v = s1.v + v;
              s2.setHas3DCoords( );
              sh2.mUsed = true;
              repeat = true;
            } else if ( s2.has3DCoords() && ! s1.has3DCoords() ) { // reset s1 values from the shot
              // float d = - sh2.length() * sh2.mDirection; // FIXME DIRECTION
              float d = - sh2.length();
              double v = - d * TDMath.sinDd( c2 );
              double h =   d * TDMath.cosDd( c2 );
              double e =   h * TDMath.sinDd( b2 );
              double s = - h * TDMath.cosDd( b2 );
              s1.e = s2.e + e;
              s1.s = s2.s + s;
              s1.v = s2.v + v;
              s1.setHas3DCoords( );
              sh2.mUsed = true;
              repeat = true;
            }
          }
        }
      }
    }

    // ---------------------------------- INSERT SPLAYS -------------------------------
    // TDLog.v( "insert splays");
    mStations.setAzimuths();
    // for ( NumStation st : mStations ) st.setAzimuths();
    for ( TriSplay ts : tmp_splays ) {
      insertSplay( ts );
    }
    // long millis_end = System.currentTimeMillis() - millis_start;
    // TDLog.v( "Data reduction " + millis_end + " msec" );
    mUnattachedLength = 0;
    for ( TriShot ts : tmp_shots ) {
      if ( ! ts.used ) {
        // TDLog.v( "unattached shot " + ts.from + " " + ts.to + " id " + ts.blocks.get(0).mId );
        mUnattachedShots.add( ts.blocks.get(0) );
        mUnattachedLength += ts.blocks.get(0).mLength;
      }
    }
    // TDLog.Log( TDLog.LOG_NUM, "unattached shot length " + mUnattachedLength );
    
    return (mShots.size() + nrSiblings == tmp_shots.size() );
  }



  /** insert a reversed shot
   * @param sf    station to which the shot is attached (FROM station of the shot)
   * @param ts    shot
   * @param i_ext TODO
   * @param a_ext  azimuth extend
   * @param f_ext  fractional extend
   * @param anomaly magnetic anomaly
   */
  private void addForwardShot( NumStation sf, TriShot ts, int i_ext, float a_ext, float f_ext, float anomaly )
  {
    float bearing = ts.b() - sf.mAnomaly;
    boolean has_coords = (i_ext <= 1);
    NumStation st = new NumStation( ts.to, sf, ts.d(), bearing + mDecl, ts.c(), f_ext, has_coords /*, ts.getReductionType() */ ); // 20200503 added mDecl
    addToStat( st, ts.getReductionType() );
    if ( ! mStations.addStation( st ) ) mClosureStations.add( st );

    st.addAzimuth( (ts.b()+180)%360, -a_ext );
    st.mAnomaly = anomaly + sf.mAnomaly;
    updateBBox( st );
    addToStats( ts.duplicate, ts.surface, ts.d(), ((i_ext == 0)? Math.abs(ts.v()) : ts.d()), ts.h(), st.v );

    NumShot sh = makeShotFromTmp( sf, st, ts, 1, sf.mAnomaly, mDecl );
    addShotToStations( sh, st, sf );
  }

  /** insert a reversed shot
   * @param st    station to which the shot is attached (TO station of the shot)
   * @param ts    shot
   * @param i_ext TODO
   * @param a_ext  azimuth extend
   * @param f_ext  fractional extend
   * @param anomaly magnetic anomaly
   */
  private void addReversedShot( NumStation st, TriShot ts, int i_ext, float a_ext, float f_ext, float anomaly )
  {
    st.addAzimuth( (ts.b()+180)%360, -a_ext );
    float bearing = ts.b() - st.mAnomaly;
    boolean has_coords = (i_ext <= 1);
    NumStation sf = new NumStation( ts.from, st, - ts.d(), bearing + mDecl, ts.c(), f_ext, has_coords /*, ts.getReductionType() */ ); // 20200503 added mDecl
    addToStat( sf, ts.getReductionType() );
    if ( ! mStations.addStation( sf ) ) mClosureStations.add( sf );

    sf.addAzimuth( ts.b(), a_ext );
    sf.mAnomaly = anomaly + st.mAnomaly; 

    updateBBox( sf );
    addToStats( ts.duplicate, ts.surface, Math.abs(ts.d() ), Math.abs( (i_ext == 0)? ts.v() : ts.d() ), Math.abs(ts.h()), sf.v );

    // FIXME is st.mAnomaly OK ?
    // N.B. was new NumShot(st, sf, ts.block, -1, mDecl); // FIXME check -anomaly
    NumShot sh = makeShotFromTmp( sf, st, ts, -1, st.mAnomaly, mDecl );
    addShotToStations( sh, sf, st );
  }

  /** insert a reversed shot
   * @param sf    station to which the shot is attached (FROM station of the shot)
   * @param ts    shot
   * @param i_ext TODO
   * @param a_ext  azimuth extend
   * @param f_ext  fractional extend
   * @param anomaly magnetic anomaly
   */
  private void addOpenLoopShot( NumStation sf, TriShot ts, int i_ext, float a_ext, float f_ext, float anomaly )
  {
    float bearing = ts.b() - sf.mAnomaly;
    boolean has_coords = (i_ext <= 1);
    NumStation st1 = new NumStation( ts.to, sf, ts.d(), bearing + mDecl, ts.c(), f_ext, has_coords /*, ts.getReductionType() */ ); // 20200503 added mDecl
    // addToStat( st1, ts.getReductionType() ); // FIXME should do this ?
    if ( ! mStations.addStation( st1 ) ) mClosureStations.add( st1 );

    st1.addAzimuth( (ts.b()+180)%360, -a_ext );
    st1.mAnomaly = anomaly + sf.mAnomaly;
    updateBBox( st1 );
    st1.mDuplicate = true;

    NumShot sh = makeShotFromTmp( sf, st1, ts, 1, sf.mAnomaly, mDecl );
    addShotToStations( sh, st1, sf );
  }

  /** insert a tri-splay into the list of splays
   * @param ts   tri-splay
   * @pre stations azimuths precomputed
   */
  private boolean insertSplay( TriSplay ts )
  {
    if ( ts == null ) return false;
    NumStation st = getStation( ts.from );
    if ( st != null ) {
      float cosine = st.computeExtend( ts.b( mDecl ), ts.extend );
      NumSplay splay = new NumSplay( st, ts.d(), ts.b( 0 ), ts.c(), cosine, ts.block, mDecl );
      mSplays.add( splay );
      // TDLog.v("SPLAY " + ts.block.mId + " flag " + splay.getReducedFlag() + " v " + splay.v );
      if ( splay.getReducedFlag() == 0 ) addToVStats( splay.v ); // skip DUPLICATE SURFACE COMMENTED
      return true;
    }
    return false;
  }

  // this is called to remove the last splay added
  private void removeSplay( TriSplay ts )
  {
    int sz = mSplays.size() - 1;
    for ( int k = sz; k >= 0; --k ) {
      NumSplay sp = mSplays.get( k );
      if ( sp.getBlock() == ts.block ) {
        // TDLog.v( "removing splay " + sz + " for " + ts.block.mId );
        mSplays.remove( k );
        break;
      }
    }
  }

  /** add a shot to a station (possibly forwarded to the station's node)
   *  a station usually has two shots, s1 and s2, at most
   *  if it has more than two shots, the additional shots are kept on a node
   * @param sh   shot
   * @param st   station
   */
  private void addShotToStation( NumShot sh, NumStation st )
  {
    if ( st.s1 == null ) {
      st.s1 = sh;
      return;
    }
    if ( st.s2 == null ) {
      st.s2 = sh;
      return;
    } 
    if ( st.node == null ) {
      st.node = new NumNode( NumNode.NODE_CROSS, st );
      mNodes.add( st.node );
    }
    st.node.addShot( sh );
  }

  /** add a shot to its two stations and insert the shot in the list of shots
   * @param sh   shot
   * @param st1  first station
   * @param st2  second station
   */
  private void addShotToStations( NumShot sh, NumStation st1, NumStation st2 )
  {
    addShotToStation( sh, st1 );
    addShotToStation( sh, st2 );
    synchronized ( mShotLock ) {
      mShots.add( sh );
    }
  }
 
  // =========================================================================
  // MAGNETIC ANOMALY COMPENSATION

  private float compensateMagneticAnomaly( TriShot ts )
  {
    float anomaly = 0;
    // if ( ts.backshot == 0 ) 
    {
      int   n_fwd = 1;      // nr of forward
      float b_fwd = ts.b(); // forward bearing
      int   n_bck = 0;      // nr of backward
      float b_bck = 0;      // backward bearing
      for ( TriShot ts1 = ts.sibling; ts1 != null; ts1 = ts1.sibling ) {
        if ( ts1.backshot == 1 ) {
          if ( ts1.b() > ts.b() + 180 ) {
            b_fwd += ts1.b() - 360;
          } else if ( ts.b() > ts1.b() + 180 ) {
            b_fwd += ts1.b() + 360;
          } else {
            b_fwd += ts1.b();
          }
          ++ n_fwd;
        } else {
          if ( n_bck > 0 ) {
            if ( ts1.b() > ts.b() + 180 ) {
              b_bck += ts1.b() - 360;
            } else if ( ts.b() > ts1.b() + 180 ) {
              b_bck += ts1.b() + 360;
            } else {
              b_bck += ts1.b();
            }
          } else {
            b_bck += ts1.b();
          }
          ++ n_bck;
        }
      }
      if ( n_bck > 0 ) {  // station_anomaly must be subtracted to measured bearing to get corrected bearing
        anomaly = b_bck/n_bck - b_fwd/n_fwd - 180;  // station_anomaly = <backward> - <forward> - 180
        if ( anomaly < -180 ) anomaly += 360;
      }
      // TDLog.v( "anomaly " + anomaly);

      // A_north       B_north
      // |              \
      // +---------------+
      // A              B  \
      //                     \ C
      // A->B = alpha
      // B->A = alpha + PI + anomaly     B->A_true = B->A - anomaly
      // anomaly = B->A - PI - A->B
      //                                 B->C_true = B->C - anomaly
    }
    return anomaly;
  }

  // =============================================================================
  // LOOP CLOSURE-ERROR COMPENSATION

  /** a cycle step: a branch from a node
   */
  static class NumStep
  { 
    NumBranch b; // branch of this step
    NumNode n;   // ???
    int k;       // index of steps on the same cycle
  
    NumStep( NumBranch b0, NumNode n0, int k0 )
    {
      b = b0;
      n = n0;
      k = k0;
    }
  }

  /** LIFO stack of steps
   * the stack is implemented as a fixed-size array - this is ok because small stacks can be expected
   */
  class NumStack
  {
    int pos; // number of items on the stack 
    int max;
    NumStep[] data;
   
    /** cstr
     * @param m  max stack size 
     */
    NumStack( int m )
    {
      max = m;
      pos = 0;
      data = new NumStep[max];
    }

    /** @return the number of items on the stack
     */
    int size() { return pos; }

    void push( NumStep step )
    {
      data[pos] = step;
      step.b.use = 1;
      step.n.use = 1;
      ++ pos;
    }

    /** @return the item on the top of the stack (or null if the stack is empty)
     */
    NumStep top() { return ( pos > 0 )? data[pos-1] : null; }

    /** @return true if the stack is empty
     */
    boolean empty() { return pos == 0; }
  
    /** remove the top item from the stack
     */
    void pop() { if ( pos > 0 ) { --pos; } }
  }

  /** build a cycle
   * @param stack   stack of cycle steps
   * @return the cycle made of the branches of the steps in the stack
   */
  private NumCycle buildCycle( NumStack stack )
  {
    int sz = stack.size();
    NumCycle cycle = new NumCycle( sz );
    for (int k=0; k<sz; ++k ) {
      cycle.addBranch( stack.data[k].b, stack.data[k].n );
    }
    return cycle;
  }

  /** identifies independent cycles from the set of branches
   * @return list of independent cycles
   * @param branches   list of branches
   *
   * checked with C++ test, however it seems that it can generate ANR (6.0.33 Android-11)
   */
  private ArrayList< NumCycle > makeIndependentCycles( ArrayList< NumBranch > branches ) 
  {
    ArrayList< NumCycle > cycles = new ArrayList<>();
    int bs = branches.size();
    // TDLog.v("NUM make independent cycles - branches " + + bs );
    if ( bs == 0 ) return cycles;

    // TDLog.v("Independent cycles on " + bs + " branches ");
    // StringBuilder sb = new StringBuilder();
    // for ( int k0 = 0; k0 < bs; ++k0 ) {
    //   NumBranch b0 = branches.get(k0);
    //   sb.append( b0.n1.station.name );
    //   sb.append( "-" );
    //   sb.append( b0.n2.station.name );
    //   sb.append( "  " );
    // }
    // TDLog.v( sb.toString() );
    
    int cnt = 0;
    NumStack stack = new NumStack( bs );
    for ( int k0 = 0; k0 < bs; ++k0 ) {
      NumBranch b0 = branches.get(k0);
      if ( b0.use == 2 ) continue;
      NumNode n0 = b0.n1; // start-node for the cycle
      b0.use = 1;         // start-branch is used
      n0.use = 0;         // but start-node is not used
      // TDLog.v("STACK push [" + k0 + "] " + b0.toString() );
      stack.push( new NumStep(b0, b0.n2, k0 ) ); // step: n1 <--- b0 (k0) --> n2
      while ( ! stack.empty() ) {
        // TDLog.v("BRANCH stack " + stack.size() );
        NumStep s1 = stack.top();
        NumNode n1 = s1.n;
        // s1.k ++; // increase branch index
        if ( n1 == n0 ) { // got a cycle
          NumCycle cycle = buildCycle( stack );
          cycles.add( cycle );
          // TDLog.v( "found cycle " + cycles.size() + " at " + n1.station.name + ": " + cycle.toString() );
          s1.b.use = 2; // mark the last edge as no more usable
          s1.n.use = 0;
          stack.pop();
          // TDLog.v("STACK pop");
        } else {
          s1.k ++; // increase branch index
          int k2 = s1.k;
          for ( ; k2<bs; ++k2 ) {
            NumBranch b2 = branches.get(k2);
            // TDLog.v("BRANCH ["+ k2 + "] use " + b2.use );
            if ( b2.use != 0 ) continue;
            NumNode n2 = b2.otherNode( n1 );
            if ( n2 != null && n2.use == 0 ) {
              b2.use = 1;
              n2.use = 1;
              // TDLog.v("STACK push [" + k2 + "] " + b2.toString() );
              stack.push( new NumStep( b2, n2, k0 ) );  // restart branch scan from k0
              s1.k = k2;
              break;
            }
          }
          if ( k2 == bs ) {
            s1.b.use = 0;
            s1.n.use = 0;
            stack.pop();
          }
        }
      }
      b0.use = 2;
      // n0.use = 2;
    }
    return cycles;
  }

  /** make the single loops
   * @param branches     survey branches
   * @param shots        survey shots
   * TODO explain the logic of this method
   */
  private void makeSingleLoops( ArrayList< NumBranch > branches, ArrayList< NumShot > shots )
  {
    for ( NumShot shot : shots ) {
      if ( shot.branch != null ) continue;
      // start a branch BRANCH_END_END or BRANCH_LOOP
      NumBranch branch = new NumBranch( NumBranch.BRANCH_LOOP, null );
      NumShot sh0 = shot;
      NumStation sf0 = sh0.from;
      NumStation st0 = sh0.to;
      sh0.mBranchDir = 1;
      while ( st0 != sf0 ) { // follow the shot 
        branch.addShot( sh0 ); // add shot to branch and find next shot
        sh0.branch = branch;
        NumShot sh1 = st0.s1;
        if ( sh1 == sh0 ) { sh1 = st0.s2; }
        if ( sh1 == null ) { // dangling station: BRANCH_END_END branch --> drop
          // mEndBranches.add( branch );
          break;
        }
        if ( sh1.from == st0 ) { // move forward
          sh1.mBranchDir = 1;
          st0 = sh1.to;
        } else { 
          sh1.mBranchDir = -1; // swap
          st0 = sh1.from;
        }
        sh0 = sh1; // move forward
      }
      if ( st0 == sf0 ) { // closed-loop branch has no node
        branch.addShot( sh0 ); // add last shot to branch
        sh0.branch = branch;
        branches.add( branch );
      }
    }
  }

  /** for each branch compute the error and distribute it over the branch shots
   * @param branches  loops branches
   */
  private void compensateSingleLoops( ArrayList< NumBranch > branches )
  {
    if ( branches.size() == 0 ) return;
    // TDLog.v("NUM compensate single loops " + branches.size() );
    for ( NumBranch br : branches ) {
      br.computeError();
      if ( TDSetting.mLoopClosure == TDSetting.LOOP_SELECTIVE ) { // find the basis of loops with smallest error
        double error  = Math.sqrt( br.e * br.e + br.s * br.s + br.v * br.v);
        double length = br.length();
        if ( error > length * TDSetting.mLoopThr / 100.0 ) {
          // TDLog.v("SELECTIVE skip single loop " + br.toString() );
          ++ nrInaccurateLoops;
          NumCycle c1 = new NumCycle(1);
          c1.addBranch( br, br.n2 ); // single loop base at branch node-2 so that branch is pos. directed
          mBadLoops.add( c1 );
          c1.setBadLoopShots();
          continue;
        }
      }
      // TDLog.v("LOOP compensate " + br.toString() );
      br.compensateError( -br.e, -br.s, -br.v );
    }
  }

  /** follow a shot (good for a single line without crosses) to make a branch
   * @param br     branch (to which shots are added) 
   * @param st     starting station
   * @param after  whether the direction is forward (FROM -> TO) or backward (To -> FROM)
   * @return list of (leg) shots that follow the station, as far as possible
   */
  private ArrayList< NumShot > followShot( NumBranch br, NumStation st, boolean after )
  {
    ArrayList< NumShot > ret = new ArrayList<>();
    boolean found = true;
    while ( found ) {
      found = false;
      synchronized( mShotLock ) {
        for ( NumShot shot1 : mShots ) {
          if ( shot1.branch != null ) continue;
          if ( shot1.from == st ) {
            shot1.mBranchDir = after? 1 : -1;
            st = shot1.to;
            found = true;
          } else if ( shot1.to == st ) {
            shot1.mBranchDir = after? -1 : 1;
            st = shot1.from;
            found = true;
          }
          if ( found ) {
            shot1.branch = br;
            ret.add( shot1 );
            break;
          } 
        }
      }
    }
    return ret;
  }

  /** make branches from this num nodes
   * @param also_cross_end     whether to include branches to end-points
   */
  public ArrayList< NumBranch > makeBranches( boolean also_cross_end ) 
  {
    return makeBranches( mNodes, also_cross_end );
  }
  
  /** from the list of nodes make the branches of type cross-cross
   * FIXME there is a flaw: this method does not detect single loops with no hair attached
   * @param nodes          network nodes
   * @param also_cross_end whether to include branches to end-points
   */
  private ArrayList< NumBranch > makeBranches( ArrayList< NumNode > nodes, boolean also_cross_end )
  {
    // TDLog.v("make branches - nodes " + nodes.size() );
    ArrayList< NumBranch > branches = new ArrayList<>();
    if ( nodes.size() > 0 ) {
      for ( NumNode node : nodes ) {
        for ( NumShot shot : node.shots ) {
          if ( shot.branch != null ) continue;
          NumBranch branch = new NumBranch( NumBranch.BRANCH_CROSS_CROSS, node );
          NumStation sf0 = node.station;
          NumShot sh0    = shot;
          NumStation st0 = sh0.to;
          if ( sh0.from == sf0 ) { 
            sh0.mBranchDir = 1;
            // st0 = sh0.to;
          } else {
            sh0.mBranchDir = -1; // swap stations
            st0 = sh0.from;
          }
          while ( st0 != sf0 ) { // follow the shot 
            branch.addShot( sh0 ); // add shot to branch and find next shot
            sh0.branch = branch;
            if ( st0.node != null ) { // end-of-branch
              branch.setLastNode( st0.node );
              branches.add( branch );
              break;
            }
            NumShot sh1 = st0.s1; // try station first node
            if ( sh1 == sh0 ) { sh1 = st0.s2; } // use station second node
            if ( sh1 == null ) { // dangling station: BRANCH_CROSS_END branch --> drop
              // TDLog.v("BRANCH_CROSS_END branch");
              // mEndBranches.add( branch );
              if ( also_cross_end ) {
                branch.setLastNode( st0.node ); // st0.node always null
                branches.add( branch );
              }
              break;
            }
            if ( sh1.from == st0 ) { // forward shot: next station is TO
              sh1.mBranchDir = 1;
              st0 = sh1.to; 
            } else {  
              sh1.mBranchDir = -1; // backward shot: next station is FROM
              st0 = sh1.from;
            }
            sh0 = sh1;
          }
          if ( st0 == sf0 ) { // closed-loop: this is not an error (just a very weird case)
            branch.addShot( sh0 ); // add shot to branch and find next shot
            sh0.branch = branch;
            branch.setLastNode( st0.node );
            branches.add( branch );
            // if ( also_cross_end ) {
            //   branch.setLastNode( st0.node );
            //   branches.add( branch );
            // }
          }
        }
      }
    } else if ( also_cross_end ) { // no nodes: only end-end lines
      synchronized ( mShotLock ) {
        for ( NumShot shot : mShots ) {
          if ( shot.branch != null ) continue;
          NumBranch branch = new NumBranch( NumBranch.BRANCH_END_END, null );
          shot.branch = branch;

          ArrayList< NumShot > shots_after  = followShot( branch, shot.to,   true );
          ArrayList< NumShot > shots_before = followShot( branch, shot.from, false );
          for ( int k=shots_before.size() - 1; k>=0; --k ) {
            NumShot sh = shots_before.get( k );
            branch.addShot( sh );
          }
          branch.addShot( shot );
          for ( NumShot sh : shots_after ) {
            branch.addShot( sh );
          }
          branch.setLastNode( null );
          branches.add( branch );
        }
      }
    }
    // TDLog.v( "found branches " + branches.size() );
    return branches;
  }

  // loop closure compensations
  // 0) a cycle is a sequence of branches: C = { b1, b2, ... }
  // 1) find a set of independent cycles, C1, C2, ... such than no linear combination of them vanishes
  //    in the basis of the branches
  //    With N nodes and B branches connecting them (ie, a connected graph)
  //    the number of independent cycles is C = 1 + B - N
  // 2) write the relation between branches and independent cycles with the incidence matrix
  //      | A11 A12 A13 ... |  | B1 |      | C1 |
  //      | A21 A22 A23 ... |  | B2 |   =  | C2 |
  //      | A31 A32 ... ... |  | .. |      | .. |
  //    this is C rows, B columns matrix.
  // 3) compute the pseudo inverse. First compute A' = At * A which is BxB matrix
  //    then the inverse A'-1 of A', 
  //    Then  A" = A'-1 * At which is BxC (it is the product of BxB times BxC)  
  // 4) The closure error compensation of the branches is 
  //        dB = - A" dC
  //    where dC are the closure error of the cycles. That is
  //        dB(j) = - Sum A"(j,i) * dC(i)
  // 
  // Weighting the compensations.
  // In the incidence matrix instead of the integers, 0, +1, -1, put the weight of the branch 
  // (eg, the length) multiplied by the integer.
  // Then the closure error of C(i) is distributed among the branches with the weight,
  //    dB'(j) = w(B(j)) * dB(j)
  //
  private void compensateLoopClosure( ArrayList< NumNode > nodes, ArrayList< NumShot > shots )
  {
    // TDLog.v("NUM loop compensation");
    ArrayList< NumBranch > branches = makeBranches( nodes, false );

    // TDLog.v("NUM branches " + branches.size() );

    ArrayList< NumBranch > singleBranches = new ArrayList<>();
    makeSingleLoops( singleBranches, shots ); // check all shots without branch
    // TDLog.v("NUM single loops " + singleBranches.size() );

    // can use a global multi-loop compensation
    // for ( NumBranch br : branches ) singleBranches.add( br );
    // compensateMultiLoops( singleBranches );

    // of separate multi-loop followed by single-loop
    compensateMultiLoops( branches );
    compensateSingleLoops( singleBranches );
  }

  private void compensateMultiLoops( ArrayList< NumBranch > branches )
  {
    int bs = branches.size();
    if ( bs == 0 ) {
      // TDLog.v("NUM loop compensation no branch"); 
      return;
    }
    if ( bs > 1 ) { // 20230118 added test
      if ( TDandroid.AT_LEAST_API_24 ) {
        branches.sort(new Comparator<NumBranch>() { // API_24
          public int compare(NumBranch b1, NumBranch b2) {
            return (b1.length() <= b2.length()) ? -1 : 1;
          }
        });
      } else { // 20230118 sort for API before 24
        NumBranch b1, b2;
        int k = 1;
        while ( k < bs ) {
          b1 = branches.get( k-1 );
          b2 = branches.get( k );
          if ( b1.length() > b2.length() ) { // swap
            branches.set( k-1, b2 );
            branches.set( k,   b1 );
            if ( k > 1 ) { 
              --k;
            } else {
              ++k;
            }
          } else {
            ++k;
          }
        }
      }
    }
    // for ( NumBranch b : branches ) {
    //   TDLog.v("BRANCH: " + b.toString( 1 ) );
    // }

    ArrayList< NumCycle > cycles = makeIndependentCycles( branches );
    // TDLog.v("NUM independent loops " + cycles.size() );
    // for ( NumCycle cy : cycles ) {
    //   TDLog.v("CYCLE: " + cy.toString() );
    // }

    // TDLog.v("Branches " + branches.size() + " single " + singleBranches.size() + " cycles " + cycles.size() );
    // This is not necessary as the cycles are already independent
    // if ( TDSetting.mLoopClosure == ? ) {
    //   independent_cycles = new ArrayList<>(); // independent cycles
    //   for ( NumCycle cycle : cycles ) {
    //     if ( ! cycle.isBranchCovered( independent_cycles ) ) {
    //       independent_cycles.add( cycle );
    //     }
    //   }
    // }
    int ls = cycles.size();
    if ( ls == 0 ) {
      // TDLog.v("NUM loop compensation no cycle"); 
      return;
    }

    double[] CE = new double[ ls ]; // closure errors : mLoopClosure = NEW
    double[] CS = new double[ ls ];
    double[] CV = new double[ ls ];
    for ( NumBranch branch : branches ) { // compute branches and cycles errors
      branch.computeError();
    }
    for (int y=0; y<ls; ++y ) {  // cycle closure errors
      NumCycle cy = cycles.get(y);
      cy.computeError();
      CE[y] = cy.e;
      CS[y] = cy.s;
      CV[y] = cy.v;
    }

    // ArrayList< NumCycle > independent_cycles; // 2022-09-23 replaced with cycles

    if ( TDSetting.mLoopClosure == TDSetting.LOOP_SELECTIVE ) { // find the basis of loops with smallest error
      // TDLog.v("LOOP selective policy");
      // FIXME only up to composition of three cycles are considered
      ArrayList< NumCycle > tmp_cycles = new ArrayList<>();
      for ( int k1 = 0; k1 < ls; ++k1 ) {
        NumCycle c1 = cycles.get( k1 );
        // TDLog.v("Cycle error " + c1.error() + " len " + c1.length() );
        if ( c1.error() < c1.length() * TDSetting.mLoopThr / 100.0f ) {
          tmp_cycles.add( c1 );
        } else {
          ++ nrInaccurateLoops;
          mBadLoops.add( c1 );
          c1.setBadLoopShots();
        }
      }
      cycles = tmp_cycles;
      ls = cycles.size(); // independent_cycles
      // TDLog.v("LOOP accurate " + ls + " inaccurate " + nrInaccurateLoops );
    }

    if ( ls == 0 ) {
      // TDLog.v("LOOP no loop to correct");
      return;
    }

    // cycle-branch incidence matrix
    // TDLog.v("LOOP cycles " + ls + " branches " + bs );
    int[] alpha = new int[ bs * ls ]; // cycle = row-index, branch = col-index
    for (int y=0; y<ls; ++y ) {  // branch-cycle matrix
      NumCycle cy = cycles.get(y); // independent_cycles
      for (int x=0; x<bs; ++x ) {
        alpha[ y*bs + x] = cy.getBranchDir( branches.get(x) );
        /* old way
        NumBranch bx = branches.get(x);
        alpha[ y*bs + x] = 0.0f;
        // int k = cy.getBranchIndex( bx );
        int dir = cy.getBranchDir( bx );
        if ( dir != 0 ) { // ( k >= 0 ) 
          // alpha[ y*bs + x] = ( bx.n2 == cy.getNode(k) )? 1.0f : -1.0f;
          alpha[ y*bs + x] = dir; // cy.dirs[k];
        }
        */
      }
    }

    if ( TDSetting.mLoopClosure == TDSetting.LOOP_WEIGHTED ) {
      nrCompensatedLoops = ls;
      // nrInaccurateLoops = 0;
      double[] WE = new double[ bs ]; // branch coordinates weights 
      double[] WS = new double[ bs ];
      double[] WV = new double[ bs ];
      for (int x=0; x<bs; ++x ) {
        NumBranch br = branches.get( x );
        WE[ x ] = br.eWeight();
        WS[ x ] = br.sWeight();
        WV[ x ] = br.vWeight();
      }

      double[] DE = new double[ bs ]; // branch error compensation 
      double[] DS = new double[ bs ];
      double[] DV = new double[ bs ];

      LoopUtil.correctCycles( alpha, CE, WE, DE, bs, ls );
      LoopUtil.correctCycles( alpha, CS, WS, DS, bs, ls );
      LoopUtil.correctCycles( alpha, CV, WV, DV, bs, ls );
      
      for (int x=0; x<bs; ++x ) { // correct branches
        NumBranch bx = branches.get(x);
        bx.compensateError( DE[x], DS[x], DV[x] );
      }

    } else { // TDSetting.mLoopClosure == TDSetting.LOOP_CYCLES || TDSetting.LOOP_SELECTIVE 
      // TDLog.v("LOOP compensating ..." );
      double[] aa = new double[ ls * ls ];    // 
      for (int y1=0; y1<ls; ++y1 ) { // cycle-cycle matrix
        for (int y2=0; y2<ls; ++y2 ) {
          double a = 0.0;
          for (int x=0; x<bs; ++x ) a += alpha[ y1*bs + x] * alpha[ y2*bs + x];
          aa[ y1*ls + y2] = a;
        }
      }

      // float det = invertMatrix( aa, ls, ls, ls );
      double det = LoopUtil.computeInverse( aa, ls, ls, ls );

      for (int y=0; y<ls; ++y ) { // compute the closure compensation values
        NumCycle cy = cycles.get(y); // independent_cycles
        cy.resetCorrections();
        for (int x=0; x<ls; ++x ) {
          NumCycle cx = cycles.get(x); // independent_cycles
          cy.ce += aa[ y*ls + x] * cx.e;
          cy.cs += aa[ y*ls + x] * cx.s;
          cy.cv += aa[ y*ls + x] * cx.v;
        }
        // cy.applyCorrection = true; // NOTE apply correction is binary [no/yes] - it could be a value in [0,1]
        // if ( TDSetting.mLoopClosure == TDSetting.LOOP_SELECTIVE ) {
        //   double err = cy.error();
        //   if ( err > 0.0 ) {
        //     double cy_len = cy.length();
        //     if ( err > cy_len * TDSetting.mLoopThr / 100.0 ) { // do not compensate loop mis-closure (mLoopThr is a percent)
        //       cy.applyCorrection = false;
        //       ++ nrInaccurateLoops;
        //     }
        //     // TDLog.v("LOOP error " + err + " len " + cy_len + " correct " + cy.applyCorrection );
        //   }
        // }
      }
      // nrCompensatedLoops = ls - nrInaccurateLoops;
      nrCompensatedLoops = ls;

      for (int x=0; x<bs; ++x ) { // correct branches: apply loop compensations
        NumBranch bx = branches.get(x);
        double e = 0;
        double s = 0;
        double v = 0;
        for (int y=0; y<ls; ++y ) {
          NumCycle cy = cycles.get(y); // independent_cycles
          // if ( ! cy.applyCorrection ) continue; // SELECTIVE
          e += alpha[ y*bs + x ] * cy.ce;
          s += alpha[ y*bs + x ] * cy.cs;
          v += alpha[ y*bs + x ] * cy.cv;
        }
        bx.compensateError( -e, -s, -v );
      }
    }
  }

  // -------------------------------------------------------------
  // TRILATERATION 

  /** correct temporary shots using trilateration
   * @param shots temporary shot list
   */
  private void makeTrilateration( List< TriShot > shots )
  {
    ArrayList< TriCluster > clusters = new ArrayList<>();
    for ( TriShot sh : shots ) sh.cluster = null;
    boolean repeat = true;
    while ( repeat ) {
      repeat = false;
      for ( TriShot sh : shots ) {
        if ( sh.cluster != null ) continue;
        repeat = true;
        TriCluster cl = new TriCluster();
        clusters.add( cl );
        cl.addTmpShot( sh );
        cl.addStation( sh.from );
        cl.addStation( sh.to );
        // recursively populate the cluster
        boolean repeat2 = true;
        while ( repeat2 ) {
          repeat2 = false;
          int ns = shots.size();
          for ( int n1 = 0; n1 < ns; ++n1 ) { // stations
            TriShot sh1 = shots.get(n1);
            if ( sh1.cluster != null ) continue;
            if ( cl.containsStation( sh1.from ) ) {
              if ( cl.containsStation( sh1.to ) ) {
                cl.addTmpShot( sh1 );
              } else {
                boolean added = false;
                for ( int n2 = n1+1; n2 < ns; ++n2 ) {
                  TriShot sh2 = shots.get(n2);
                  if ( sh2.cluster != null ) continue;
                  if ( ( sh1.to.equals( sh2.from ) && cl.containsStation( sh2.to ) ) ||
                       ( sh1.to.equals( sh2.to ) && cl.containsStation( sh2.from ) ) ) {
                    cl.addTmpShot( sh2 );
                    added = true;
                  }
                }
                if ( added ) {
                  cl.addStation( sh1.to );
                  cl.addTmpShot( sh1 );
                  repeat2 = true;
                }
              }
            } else if ( cl.containsStation( sh1.to ) ) {
              boolean added = false;
              for ( int n2 = n1+1; n2 < ns; ++n2 ) {
                TriShot sh2 = shots.get(n2);
                if ( sh2.cluster != null ) continue;
                if ( ( sh1.from.equals( sh2.from ) && cl.containsStation( sh2.to ) ) ||
                     ( sh1.from.equals( sh2.to ) && cl.containsStation( sh2.from ) ) ) {
                  cl.addTmpShot( sh2 );
                  added = true;
                }
              }
              if ( added ) {
                cl.addStation( sh1.from );
                cl.addTmpShot( sh1 );
                repeat2 = true;
              }
            }
          }         
          for ( TriShot sh1 : shots ) { // shots (should not be needed)
            if ( sh1.cluster != null ) continue;
            if ( cl.containsStation( sh1.from ) && cl.containsStation( sh1.to ) ) {
              cl.addTmpShot( sh1 );
            }
          }
        }
      }
    }
    // apply trilateration with recursive minimization
    for ( TriCluster cl : clusters ) {
      if ( cl.nrStations() > 2 ) {
        Trilateration trilateration = new Trilateration( cl );
        // use trilateration.points and legs
        for ( TriLeg leg : trilateration.legs ) {
          TriPoint p1 = leg.pi;
          TriPoint p2 = leg.pj;
          // compute azimuth (p2-p1)
          double dx = p2.x - p1.x; // east
          double dy = p2.y - p1.y; // north
          double a = Math.atan2( dx, dy ) * 180 / Math.PI;
          if ( a < 0 ) a += 360;
          // TDLog.v("TRI leg " + p1.name + " " + p2.name + " angle " + a );
          leg.shot.mAvgLeg.mDecl = (float)(a - leg.a); // per shot declination
        }
      }
    }
  }

  // ------------------------------------------------------------------------------
  // ALGORITHMS

  /** compute the 3D vector of a point along a leg
   * @param s     abscissa along the leg: 0 = FROM station, 1 = TO station
   * @param blk   leg
   * N.B. offset for geo-coord of origin must be handled by caller
   */
  public TDVector getCave3Dz( float s, DBlock blk )
  {
    if ( s <= 0 ) {
      NumStation st = getStation( blk.mFrom );
      return new TDVector( (float)st.e, (float)st.s, (float)st.v );
    } else if ( s >= 1 ) {
      NumStation st = getStation( blk.mTo );
      return new TDVector( (float)st.e, (float)st.s, (float)st.v );
    }
    NumStation st1 = getStation( blk.mFrom );
    NumStation st2 = getStation( blk.mTo );
    return new TDVector( (float)( st1.e + (st2.e-st1.e)*s ), (float)( st1.s + (st2.s-st1.s)*s ), (float)( st1.v + ( st2.v - st1.v)*s ) );
  }

  /** @return the list of legs of the shortest path between two stations
   * @param s1    first station 
   * @param s2    second station 
   */
  private ArrayList<NumShot> getShortestPathShots( NumStation s1, NumStation s2 )
  {
    ArrayList< NumShot > ret = new ArrayList<>();
    for ( NumShot e : mShots ) {
      if ( e.from == null || e.to == null ) continue;
      if ( e.from == s1   && e.to == s2   ) continue;
      if ( e.from == s2   && e.to == s1   ) continue;
      ret.add( e );
    }
    return ret;
  }

  /** recenter the survey about the middle
   * @note used only by the projection dialog
   */
  public void recenter()
  {
    double ec = (mEmin + mEmax)/2;
    double sc = (mSmin + mSmax)/2;
    double vc = (mVmin + mVmax)/2;
    // double hc = (mHmin + mHmax)/2;
    double de = (mEmax - mEmin)/2;
    double ds = (mSmax - mSmin)/2;
    double dv = (mVmax - mVmin)/2;
    for ( NumStation st : mStations.getStations() ) {
      st.e -= ec;
      st.s -= sc;
      st.v -= vc;
      // st.h -= hc;
    }
    // shots use FROM-TO stations coords
    for ( NumSplay sp : mSplays ) {
      sp.e -= ec;
      sp.s -= sc;
      sp.v -= vc;
      // sp.h -= hc;
    }
    mEmin = - de;
    mEmax =   de;
    mSmin = - ds;
    mSmax =   ds;
    mVmin = - dv;
    mVmax =   dv;
  }

}


