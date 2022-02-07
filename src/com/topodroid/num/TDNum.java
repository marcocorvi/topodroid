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

import com.topodroid.DistoX.TDInstance;
import com.topodroid.DistoX.DBlock;
import com.topodroid.DistoX.StationPolicy;
import com.topodroid.DistoX.SurveyInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Locale;
import java.util.HashMap;

public class TDNum
{
  /** create the numerical centerline
   * @param data     list of survey data
   * @param start    start station
   * @param view     barriers list
   * @param hide     hiding list
   */
  public TDNum( List< DBlock > data, String start, String view, String hide, float decl, String format )
  {
    TDLog.Log( TDLog.LOG_NUM, "data reduction: decl " + decl + " start " + start );
    mDecl = decl;
    surveyExtend   = true;
    surveyAttached = computeNum( data, start, format );
    setStationsHide( hide );
    setStationsBarr( view );
  }

  // public void dump()
  // {
  //   TDLog.Log( TopoDroiaLog.LOG_NUM, "TDNum Stations:" );
  //   for ( NumStation st : mStations ) {
  //     TDLog.Log( TopoDroiaLog.LOG_NUM, "   " + st.name + " S: " + st.s + " E: " + st.e );
  //   }
  //   TDLog.Log( TopoDroiaLog.LOG_NUM, "Shots:" );
  //   for ( NumShot sh : mShots ) {
  //     TDLog.Log( TopoDroiaLog.LOG_NUM, "   From: " + sh.from.name + " To: " + sh.to.name );
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
  private double mInLegErrSum0; // angular error distribution of the data withn the legs - accumulators
  private double mInLegErrSum1;
  private double mInLegErrSum2;
  private double mInLegErr1;    // statistics
  private double mInLegErr2;

  private int mLenCnt;

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
  }

  // add the contribution of the data of a leg to the statistics of in-leg errors
  private void addToInLegError( TriShot ts )
  {
    int size = ts.blocks.size();
    for ( int i = 0; i < size; ++i ) {
      DBlock blk1 = ts.blocks.get(i);
      for ( int j = i+1; j < size; ++j ) {
        DBlock blk2 = ts.blocks.get(j);
        double e = blk1.relativeAngle( blk2 );
        mInLegErrSum0 += 1;
        mInLegErrSum1 += e;
        mInLegErrSum2 += e*e;
      }
    }
  }

  private void computeInLegError()
  {
    if ( mInLegErrSum0 > 0 ) {
      mInLegErr1 = mInLegErrSum1 / mInLegErrSum0;
      mInLegErr2 = Math.sqrt( mInLegErrSum2/mInLegErrSum0 - mInLegErr1*mInLegErr1 );
    }
  }

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

  public int stationsNr()  { return mStations.size(); }
  public int shotsNr()     { return mShots.size(); }
  public int duplicateNr() { return mDupNr; }
  public int surfaceNr()   { return mSurfNr; }
  public int splaysNr()    { return mSplays.size(); }
  public int loopNr()      { return mClosures.size(); }
  public int unattachedShotsNr() { return mUnattachedShots.size(); }

  public float surveyLength()     { return (float)mLength; }
  public float surveyExtLen()     { return (float)mExtLen; }
  public float surveyProjLen()    { return (float)mProjLen; }
  public float surveyTop()        { return (float)mTup; }   // top must be positive
  public float surveyBottom()     { return (float)mTdown; } // bottom must be negative
  public float unattachedLength() { return (float)mUnattachedLength; }

  public float angleErrorMean()   { return (float)mInLegErr1; } // radians
  public float angleErrorStddev() { return (float)mInLegErr2; } // radians

  // -------------------------------------------------------
  // SURVEY DATA 

  private NumStation mStartStation; // origin station
  private float      mDecl;         // magnetic declination

  private NumStationSet mStations;
  private ArrayList< NumStation > mClosureStations;
  private ArrayList< NumShot >    mShots;
  private ArrayList< NumSplay >   mSplays;
  private ArrayList< NumClosure > mClosures;
  private ArrayList< NumNode >    mNodes;
  private ArrayList< DBlock >     mUnattachedShots;

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

  /** @return the list of splays at a given station
   * @param st    station
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
    synchronized( mShots ) {
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
    synchronized( mShots ) {
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
   * FIXME there is a problem here:               ,-----B---
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
    st = st.mParent;
    while ( st != null ) {
      st.mHidden += hide;
      if ( st.mHidden < 0 ) st.mHidden = 0;
      // TDLog.v( "station " + st.name + " hide " + st.mHidden );
      st = st.mParent;
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
    st.mBarrierAndHidden = ( st.mHidden == 1 && barrier == 1 );
    st.mHidden -= barrier;
    barrier *= 2;
    Stack<NumStation> stack = new Stack<NumStation>();
    stack.push( st );
    while ( ! stack.empty() ) {
      st = stack.pop();
      // TDLog.v( "station " + st.name + " hide " + st.mHidden );
      mStations.updateHidden( st, -barrier, stack );

      // for ( NumStation s : mStations ) {
      //   if ( s.mParent == st ) {
      //     s.mHidden -= barrier;
      //     stack.push( s );
      //   }
      // }
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
    synchronized( mShots ) {
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
    synchronized( mShots ) {
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
    synchronized( mShots ) {
      for (NumShot sh : mShots ) {
        if ( s1.equals( sh.from.name ) && s2.equals( sh.to.name ) ) { dir =  sh.mDirection; break; }
        if ( s2.equals( sh.from.name ) && s1.equals( sh.to.name ) ) { dir = -sh.mDirection; break; }
      }
    }
    return dir;
  }

  // ==========================================================================
  // latest data circular buffer
  private class DBlockBuffer
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
   * @param blk    new shot
   * @param format loop closure report format
   * @return true if the shot has been appended
   * @note the new shot is assumed not to close any loop
   */
  public boolean appendData( DBlock blk, DBlock leg, String format )
  {
    if ( blk == null ) return false;
    mBuffer.put( blk );

    if ( leg != null && mLastSplay != null ) {
      // TDLog.v( "num got_leg ");
      removeSplay( mLastSplay );
      appendLeg( mLastSplay.block, leg, format );
      mLastSplay = null;
    } else {
      if ( blk.isSplay() ) {
        // if ( mLastLeg != null ) {
        //   TDLog.v( "num insert leg ");
        //   insertLeg( mLastLeg, format );
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

  private boolean appendLeg( DBlock blk, DBlock leg, String format ) 
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
    insertLeg( mLastLeg, format );
    return true;
  }

  void addClosure( NumClosure closure ) { mClosures.add( closure ); }

  /** insert a leg shot
   * @param ts     leg shot
   * @param format loop closure report format
   */
  private void insertLeg( TriShot ts, String format )
  {
    float anomaly = StationPolicy.doMagAnomaly() ? compensateMagneticAnomaly( ts ) : 0;
    // try to see if any temp-shot station is on the list of stations
    NumStation sf = getStation( ts.from );
    NumStation st = getStation( ts.to );
    int  iext = DBlock.getIntExtend( ts.extend ); // integer extend
    float fext = DBlock.getReducedExtend( ts.extend, ts.stretch ); // float extend - used for station coords
    float aext = fext; // station azimuth extends
    if ( sf != null ) {
      sf.addAzimuth( ts.b(), aext );
      if ( st != null ) { // loop-closure -: need the loop length to compute the fractional closure error
        // do close loop also on duplicate shots
        if ( format != null ) {
          ArrayList< NumShot > shots = getShortestPathShots( sf, st );
          ArrayList< NumShortpath > paths = new ArrayList<>();
          mStations.initShortestPath( paths, 1000000.0f );
          (new ClosureTask( this, format, shots, paths, sf, st, ts.d(), ts.b(), ts.c() )).execute();
        }
        if ( /* TDSetting.mAutoStations || */ TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) { // do not close loop
          addOpenLoopShot( sf, ts, iext, aext, fext, anomaly ); // keep loop open: new station( id=ts.to, from=sf, ... )
        } else { // TDLog.v( "close loop at " + sf.name + " " + st.name );
          NumShot sh = makeShotFromTmp( sf, st, ts, 0, sf.mAnomaly, mDecl ); 
          addShotToStations( sh, sf, st );
        }
        // float length = ts.d();
        // if ( iext == 0 ) length = TDMath.sqrt( length*length - ts.h()*ts.h() );
        addToStats( ts.duplicate, ts.surface, ts.d(), ((iext == 0)? Math.abs(ts.v()) : ts.d()), ts.h() );
      }
      else // st null || st isBarrier
      { // forward shot: from --> to
        addForwardShot( sf, ts, iext, aext, fext, anomaly );
      }
    }
    else if ( st != null ) 
    { // sf == null: reversed shot only difference is '-' sign in new NumStation, and the new station is sf
      addReversedShot( st, ts, iext, aext, fext, anomaly );
    }
  }

  /** make a NumShot from a temporary shot
   * @param sf    from station
   * @param st    to station
   * @param ts    temp shot
   */
  private NumShot makeShotFromTmp( NumStation sf, NumStation st, TriShot ts, int direction, float anomaly, float mDecl )
  {
    if ( ts.reversed != 1 ) {
      TDLog.Error( "making shot from reversed temp " + sf.name + " " + st.name );
    }
    DBlock blk = ts.getFirstBlock();
    // TDLog.v( "make shot " + sf.name + "-" + st.name + " blocks " + ts.blocks.size() + " E " + blk.getIntExtend() + " S " + blk.getStretch() );
    // NumShot sh = new NumShot( sf, st, ts.getFirstBlock(), 1, anomaly, mDecl ); // FIXME DIRECTION
    NumShot sh = new NumShot( sf, st, ts.getFirstBlock(), direction, anomaly, mDecl );
    ArrayList< DBlock > blks = ts.getBlocks();
    for ( int k = 1; k < blks.size(); ++k ) {
      sh.addBlock( blks.get(k) );
    }
    return sh;
  }

  private NumShot makeShotFromTmpDiving( NumStation sf, NumStation st, TriShot ts, int direction, float anomaly, float mDecl, float tdepth )
  {
    if ( ts.reversed != 1 ) {
      TDLog.Error( "making shot from reversed temp " + sf.name + " " + st.name );
    }
    DBlock blk = ts.getFirstBlock();
    // TDLog.v( "make shot " + sf.name + "-" + st.name + " blocks " + ts.blocks.size() + " E " + blk.getIntExtend() + " S " + blk.getStretch() );
    // NumShot sh = new NumShot( sf, st, ts.getFirstBlock(), 1, anomaly, mDecl ); // FIXME DIRECTION
    NumShot sh = new NumShot( sf, st, ts.getFirstBlock(), direction, anomaly, mDecl );
    ArrayList< DBlock > blks = ts.getBlocks();
    for ( int k = 1; k < blks.size(); ++k ) {
      sh.addBlock( blks.get(k) );
    }
    return sh;
  }

  /** initalize the list of shots and splays from the list of survey data
   * @param data     survey data
   * @param shots    (temporary) legs
   * @param splays   (temporary) splays
   * @note mLastLeg remains after the shots have been initalized
   */
  private void initShots( List< DBlock > data, List< TriShot > shots, List< TriSplay > splays )
  {
    mLastLeg = null;
    for ( DBlock blk : data ) {
      // TDLog.v( "NUM blk type " + blk.mType );
      if ( blk.isSplay() ) {
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
   * @param format loop closure report format
   * @return true if all shots are attached
   */
  private boolean computeNum( List< DBlock > data, String start, String format )
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
          float tdepth = depths.get( blk.mTo ).floatValue();
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

    List< TriShot > tmpshots   = new ArrayList<>();
    List< TriSplay > tmpsplays = new ArrayList<>();

    initShots( data, tmpshots, tmpsplays );
    TDLog.Log( TDLog.LOG_NUM, "data " + data.size() + " shots " + tmpshots.size() + " splays " + tmpsplays.size() );

    if ( TDSetting.mLoopClosure == TDSetting.LOOP_TRIANGLES ) {
      makeTrilateration( tmpshots );
    }

    // ---------------------------------- SIBLINGS and BACKSIGHT -------------------------------
    for ( TriShot tsh : tmpshots ) { // clear backshot, sibling, and multibad
      tsh.backshot = 0;
      tsh.sibling  = null;
      tsh.getFirstBlock().setMultiBad( false );
    }

    for ( int i = 0; i < tmpshots.size(); ++i ) {
      TriShot ts0 = tmpshots.get( i );
      addToInLegError( ts0 );
      if ( ts0.backshot != 0 ) continue; // skip siblings

      DBlock blk0 = ts0.getFirstBlock();
      // (1) check if ts0 has siblings
      String from = ts0.from;
      String to   = ts0.to;
      // if ( from == null || to == null ) continue; // FIXME
      TriShot ts1 = ts0; // last sibling (head = the shot itself)
      for ( int j=i+1; j < tmpshots.size(); ++j ) {
        TriShot ts2 = tmpshots.get( j );
        if ( from.equals( ts2.from ) && to.equals( ts2.to ) ) { // TDLog.v( "chain a positive sibling" );
          ts1.sibling = ts2;
          ts1 = ts2;
          ts2.backshot = +1;
	  ++ nrSiblings;
        } else if ( from.equals( ts2.to ) && to.equals( ts2.from ) ) { // TDLog.v( "chain a negative sibling" );
          ts1.sibling = ts2;
          ts1 = ts2;
          ts2.backshot = -1;
	  ++ nrSiblings;
        }
      }

      if ( ts0.sibling != null ) { // TDLog.v( "check sibling shots agreement" );
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
          float d = ( ( ts1.backshot == -1 )? v1.plus(v2) : v1.minus(v2) ).Length();
          d = d/blk0.mLength + d/blk1.mLength; 
          if ( d > dmax ) dmax = d;
          ts1 = ts1.sibling;
        }
        if ( ( ! StationPolicy.doMagAnomaly() ) && ( dmax > TDSetting.mCloseDistance ) ) {
          blk0.setMultiBad( true );
        }
        
        if ( ! StationPolicy.doMagAnomaly() ) { // (3) remove siblings
          ts1 = ts0.sibling;
          while ( ts1 != null ) { // TDLog.v( "NUM removing sibling " + ts1.from + "-" + ts1.to + " : " + nrSiblings );
            -- nrSiblings;
            TriShot ts2 = ts1.sibling;
            tmpshots.remove( ts1 );
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
    computeInLegError();

    // ---------------------------------- DATA REDUCTION -------------------------------
    mStartStation = new NumStation( start );
    mStartStation.setHasExtend( true );
    mStations.addStation( mStartStation );

    // two-pass data reduction
    // first-pass all shots with regular extends
    // second-pass any leftover shot
    for ( int pass = 0; pass < 2; ++ pass ) {
      TDLog.Log( TDLog.LOG_NUM, "data reduction pass " + pass );
      boolean repeat = true;
      while ( repeat ) {
        repeat = false;
        for ( TriShot ts : tmpshots ) {
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

          int  iext = DBlock.getIntExtend( ts.extend ); // integer extend
          float fext = DBlock.getReducedExtend( ts.extend, ts.stretch ); // float extend - used for station coords
          float aext = fext; // station azimuth extends
          if ( sf != null ) {
            sf.addAzimuth( ts.b(), aext );
            if ( st != null ) { // loop-closure -: need the loop length to compute the fractional closure error
              // do close loop also on duplicate shots
	      if ( format != null ) {
                ArrayList< NumShot > shots = getShortestPathShots( sf, st );
                ArrayList< NumShortpath > paths = new ArrayList<>();
                mStations.initShortestPath( paths, 1000000.0f );
                (new ClosureTask( this, format, shots, paths, sf, st, ts.d(), ts.b(), ts.c() )).execute();
              }
              if ( /* TDSetting.mAutoStations || */ TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) { // do not close loop
                addOpenLoopShot( sf, ts, iext, aext, fext, anomaly ); // keep loop open: new station( id=ts.to, from=sf, ... )
              } else { // TDLog.v( "close loop at " + sf.name + " " + st.name );
                NumShot sh = makeShotFromTmp( sf, st, ts, 0, sf.mAnomaly, mDecl ); 
                addShotToStations( sh, sf, st );
              }
              // float length = ts.d();
	      // if ( iext == 0 ) length = TDMath.sqrt( length*length - ts.h()*ts.h() );
              addToStats( ts.duplicate, ts.surface, ts.d(), ((iext == 0)? Math.abs(ts.v()) : ts.d()), ts.h() );
              ts.used = true;
              repeat = true;
            }
            else // st null || st isBarrier
            { // forward shot: from --> to
              addForwardShot( sf, ts, iext, aext, fext, anomaly );
              ts.used = true;
              repeat = true;
            }
          }
          else if ( st != null ) 
          { // sf == null: reversed shot only difference is '-' sign in new NumStation, and the new station is sf
            addReversedShot( st, ts, iext, aext, fext, anomaly );
            ts.used = true;
            repeat = true;
          }
        }
      }
    }

    // ---------------------------------- LOOP CLOSURE -------------------------------
    if ( TDSetting.mLoopClosure == TDSetting.LOOP_CYCLES || TDSetting.mLoopClosure == TDSetting.LOOP_WEIGHTED ) { // TDLog.Log( TDLog.LOG_NUM, "loop compensation");
      TDLog.Log( TDLog.LOG_NUM, "loop closure compensation");
      compensateLoopClosure( mNodes, mShots );
  
      // recompute station positions
      synchronized( mShots ) {
        for ( NumShot sh1 : mShots ) {
          sh1.mUsed = false;
        }
      }
      mStations.reset3DCoords( );       // reset the stations "Has 3D Coords" to false
      mStartStation.setHas3DCoords( );  // except for the start station 

      synchronized( mShots ) {
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
    for ( TriSplay ts : tmpsplays ) {
      insertSplay( ts );
    }
    // long millis_end = System.currentTimeMillis() - millis_start;
    // TDLog.v( "Data reduction " + millis_end + " msec" );
    mUnattachedLength = 0;
    for ( TriShot ts : tmpshots ) {
      if ( ! ts.used ) {
        // TDLog.v( "unattached shot " + ts.from + " " + ts.to + " id " + ts.blocks.get(0).mId );
        mUnattachedShots.add( ts.blocks.get(0) );
        mUnattachedLength += ts.blocks.get(0).mLength;
      }
    }
    TDLog.Log( TDLog.LOG_NUM, "unattached shot length " + mUnattachedLength );
    
    return (mShots.size() + nrSiblings == tmpshots.size() );
  }



  /** insert a reversed shot
   * @param sf    station to which the shot is attached (FROM station of the shot)
   * @param ts    shot
   * @param aext  azimuth extend
   * @param fext  fractional extend
   * @param anomaly magnetic anomaly
   */
  private void addForwardShot( NumStation sf, TriShot ts, int iext, float aext, float fext, float anomaly )
  {
    float bearing = ts.b() - sf.mAnomaly;
    boolean has_coords = (iext <= 1);
    NumStation st = new NumStation( ts.to, sf, ts.d(), bearing + mDecl, ts.c(), fext, has_coords ); // 20200503 added mDecl
    if ( ! mStations.addStation( st ) ) mClosureStations.add( st );

    st.addAzimuth( (ts.b()+180)%360, -aext );
    st.mAnomaly = anomaly + sf.mAnomaly;
    updateBBox( st );
    addToStats( ts.duplicate, ts.surface, ts.d(), ((iext == 0)? Math.abs(ts.v()) : ts.d()), ts.h(), st.v );

    NumShot sh = makeShotFromTmp( sf, st, ts, 1, sf.mAnomaly, mDecl );
    addShotToStations( sh, st, sf );
  }

  /** insert a reversed shot
   * @param st    station to which the shot is attached (TO station of the shot)
   * @param ts    shot
   * @param aext  azimuth extend
   * @param fext  fractional extend
   * @param anomaly magnetic anomaly
   */
  private void addReversedShot( NumStation st, TriShot ts, int iext, float aext, float fext, float anomaly )
  {
    st.addAzimuth( (ts.b()+180)%360, -aext );
    float bearing = ts.b() - st.mAnomaly;
    boolean has_coords = (iext <= 1);
    NumStation sf = new NumStation( ts.from, st, - ts.d(), bearing + mDecl, ts.c(), fext, has_coords ); // 20200503 added mDecl
    if ( ! mStations.addStation( sf ) ) mClosureStations.add( sf );

    sf.addAzimuth( ts.b(), aext );
    sf.mAnomaly = anomaly + st.mAnomaly; 

    updateBBox( sf );
    addToStats( ts.duplicate, ts.surface, Math.abs(ts.d() ), Math.abs( (iext == 0)? ts.v() : ts.d() ), Math.abs(ts.h()), sf.v );

    // FIXME is st.mAnomaly OK ?
    // N.B. was new NumShot(st, sf, ts.block, -1, mDecl); // FIXME check -anomaly
    NumShot sh = makeShotFromTmp( sf, st, ts, -1, st.mAnomaly, mDecl );
    addShotToStations( sh, sf, st );
  }

  /** insert a reversed shot
   * @param sf    station to which the shot is attached (FROM station of the shot)
   * @param ts    shot
   * @param aext  azimuth extend
   * @param fext  fractional extend
   * @param anomaly magnetic anomaly
   */
  private void addOpenLoopShot( NumStation sf, TriShot ts, int iext, float aext, float fext, float anomaly )
  {
    float bearing = ts.b() - sf.mAnomaly;
    boolean has_coords = (iext <= 1);
    NumStation st1 = new NumStation( ts.to, sf, ts.d(), bearing + mDecl, ts.c(), fext, has_coords ); // 20200503 added mDecl
    if ( ! mStations.addStation( st1 ) ) mClosureStations.add( st1 );

    st1.addAzimuth( (ts.b()+180)%360, -aext );
    st1.mAnomaly = anomaly + sf.mAnomaly;
    updateBBox( st1 );
    st1.mDuplicate = true;

    NumShot sh = makeShotFromTmp( sf, st1, ts, 1, sf.mAnomaly, mDecl );
    addShotToStations( sh, st1, sf );
  }

  /** insert a tri-splay into the list of splays
   * @parm ts   tri-splay
   */
  private boolean insertSplay( TriSplay ts )
  {
    if ( ts == null ) return false;
    NumStation st = getStation( ts.from );
    if ( st != null ) {
      float cosine = st.computeExtend( ts.b( mDecl ), ts.extend );
      mSplays.add( new NumSplay( st, ts.d(), ts.b( 0 ), ts.c(), cosine, ts.block, mDecl ) );
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
    synchronized ( mShots ) {
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
      int   nfwd = 1;      // nr of forward
      float bfwd = ts.b(); // forward bearing
      int   nbck = 0;      // nr of backward
      float bbck = 0;      // backward bearing
      for ( TriShot ts1 = ts.sibling; ts1 != null; ts1 = ts1.sibling ) {
        if ( ts1.backshot == 1 ) {
          if ( ts1.b() > ts.b() + 180 ) {
            bfwd += ts1.b() - 360;
          } else if ( ts.b() > ts1.b() + 180 ) {
            bfwd += ts1.b() + 360;
          } else {
            bfwd += ts1.b();
          }
          ++ nfwd;
        } else {
          if ( nbck > 0 ) {
            if ( ts1.b() > ts.b() + 180 ) {
              bbck += ts1.b() - 360;
            } else if ( ts.b() > ts1.b() + 180 ) {
              bbck += ts1.b() + 360;
            } else {
              bbck += ts1.b();
            }
          } else {
            bbck += ts1.b();
          }
          ++ nbck;
        }
      }
      if ( nbck > 0 ) {  // station_anomaly must be subtracted to mearured bearing to get corrected bearing
        anomaly = bbck/nbck - bfwd/nfwd - 180;  // station_anomaly = <backward> - <forward> - 180
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

  class NumStep
  { 
    NumBranch b;
    NumNode n;
    int k;
  
    NumStep( NumBranch b0, NumNode n0, int k0 )
    {
      b = b0;
      n = n0;
      k = k0;
    }
  }

  // LIFO stack of steps
  class NumStack
  {
    int pos;
    int max;
    NumStep[] data;
   
    NumStack( int m )
    {
      max = m;
      data = new NumStep[max];
    }

    int size() { return pos; }

    void push( NumStep step )
    {
      data[pos] = step;
      step.b.use = 1;
      step.n.use = 1;
      ++ pos;
    }

    NumStep top() { return ( pos > 0 )? data[pos-1] : null; }

    boolean empty() { return pos == 0; }
  
    void pop() { if ( pos > 0 ) { --pos; } }
  }

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
   * @param cycles     list of cycles (output)
   * @param branches   list of branches
   *
   * checked with C++ test, however it seems that it can generate ANR (6.0.33 Android-11)
   */
  private void makeIndependentCycles( ArrayList< NumCycle > cycles, ArrayList< NumBranch > branches ) 
  {
    int bs = branches.size();
    // StringBuilder sb = new StringBuilder();
    // for ( int k0 = 0; k0 < bs; ++k0 ) {
    //   NumBranch b0 = branches.get(k0);
    //   sb.append( b0.n1.station.name );
    //   sb.append( "-" );
    //   sb.append( b0.n2.station.name );
    //   sb.append( "  " );
    // }
    // TDLog.v( sb.toString() );
    
    NumStack stack = new NumStack( bs );
    for ( int k0 = 0; k0 < bs; ++k0 ) {
      NumBranch b0 = branches.get(k0);
      if ( b0.use == 2 ) continue;
      NumNode n0 = b0.n1; // start-node for the cycle
      b0.use = 1;         // start-branch is used
      n0.use = 0;         // but start-node is not used
      stack.push( new NumStep(b0, b0.n2, k0 ) ); // step with b0 to the second node (k0 = where start scan branches
      while ( ! stack.empty() ) {
        NumStep s1 = stack.top();
        NumNode n1 = s1.n;
        s1.k ++;
        int k1 = s1.k;
        if ( n1 == n0 ) {
          cycles.add( buildCycle( stack ) );
          // TDLog.v( "found cycle " + cycles.size() + " at " + n1.station.name );
          s1.b.use = 2; // mark the last edge as no more usable
          s1.n.use = 0;
          stack.pop();
        } else {
          int k2 = s1.k;
          for ( ; k2<bs; ++k2 ) {
            NumBranch b2 = branches.get(k2);
            if ( b2.use != 0 ) continue;
            NumNode n2 = b2.otherNode( n1 );
            if ( n2 != null && n2.use == 0 ) {
              b2.use = 1;
              n2.use = 1;
              stack.push( new NumStep( b2, n2, k0 ) );
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
  }

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
    for ( NumBranch br : branches ) {
      br.computeError();
      br.compensateError( -br.e, -br.s, -br.v );
    }
  }

  // follow a shot (good for a single line without crosses)
  private ArrayList< NumShot > followShot( NumBranch br, NumStation st, boolean after )
  {
    ArrayList< NumShot > ret = new ArrayList<>();
    boolean found = true;
    while ( found ) {
      found = false;
      synchronized( mShots ) {
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

  /* make branches from this num nodes
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
          // TDLog.v( "start branch at " + sf0.name + " - " + st0.name );
          while ( st0 != sf0 ) { // follow the shot 
            branch.addShot( sh0 ); // add shot to branch and find next shot
            sh0.branch = branch;
            if ( st0.node != null ) { // end-of-branch
              // TDLog.v( "end of branch at " + st0.node.station.name );
              branch.setLastNode( st0.node );
              branches.add( branch );
              break;
            }
            NumShot sh1 = st0.s1;
            if ( sh1 == sh0 ) { sh1 = st0.s2; }
            if ( sh1 == null ) { // dangling station: BRANCH_CROSS_END branch --> drop
              // mEndBranches.add( branch );
              if ( also_cross_end ) {
                branch.setLastNode( st0.node ); // st0.node always null
                branches.add( branch );
              }
              break;
            }
            if ( sh1.from == st0 ) { // move forward
              sh1.mBranchDir = 1;
              st0 = sh1.to; 
            } else {  
              sh1.mBranchDir = -1; // swap
              st0 = sh1.from;
            }
            // TDLog.v( " move to " + st0.name );
            sh0 = sh1;
          }
          if ( st0 == sf0 ) { // closed-loop ???
            // TDLog.Error( "ERROR closed loop in num branches");
            if ( also_cross_end ) {
              branch.setLastNode( st0.node );
              branches.add( branch );
            }
          }
        }
      }
    } else if ( also_cross_end ) { // no nodes: only end-end lines
      synchronized ( mShots ) {
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
  // Then the closure error of C(i) is distributed anong the branches with the weight,
  //    dB'(j) = w(B(j)) * dB(j)
  //
  private void compensateLoopClosure( ArrayList< NumNode > nodes, ArrayList< NumShot > shots )
  {
    ArrayList< NumBranch > branches = makeBranches( nodes, false );

    ArrayList< NumBranch > singleBranches = new ArrayList<>();
    makeSingleLoops( singleBranches, shots ); // check all shots without branch
    compensateSingleLoops( singleBranches );

    ArrayList< NumCycle > cycles = new ArrayList<>();
    makeIndependentCycles( cycles, branches );

    ArrayList< NumCycle > indep_cycles = cycles;
    // This is not necessary as the cycles are already independent
    // if ( TDSetting.mLoopClosure == ? ) {
    //   indep_cycles = new ArrayList<>(); // independent cycles
    //   for ( NumCycle cycle : cycles ) {
    //     if ( ! cycle.isBranchCovered( indep_cycles ) ) {
    //       indep_cycles.add( cycle );
    //     }
    //   }
    // }

    int ls = indep_cycles.size();
    int bs = branches.size();

    double[] CE = new double[ ls ]; // closure errors : mLoopClosure = NEW
    double[] CS = new double[ ls ];
    double[] CV = new double[ ls ];
    for ( NumBranch branch : branches ) { // compute branches and cycles errors
      branch.computeError();
    }
    for (int y=0; y<ls; ++y ) {  // branch-cycle matrix
      NumCycle cy = indep_cycles.get(y);
      cy.computeError();
      CE[y] = cy.e;
      CS[y] = cy.s;
      CV[y] = cy.v;
    }

    // cycle-branch incidence matrix
    int[] alpha = new int[ bs * ls ]; // cycle = row-index, branch = col-index
    for (int y=0; y<ls; ++y ) {  // branch-cycle matrix
      NumCycle cy = indep_cycles.get(y);
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
    } else { // TDSetting.mLoopClosure == TDSetting.LOOP_CYCLES
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
        NumCycle cy = indep_cycles.get(y);
        cy.ce = 0; // corrections
        cy.cs = 0;
        cy.cv = 0;
        for (int x=0; x<ls; ++x ) {
          NumCycle cx = indep_cycles.get(x);
          cy.ce += aa[ y*ls + x] * cx.e;
          cy.cs += aa[ y*ls + x] * cx.s;
          cy.cv += aa[ y*ls + x] * cx.v;
        }
      }
      
      for (int x=0; x<bs; ++x ) { // correct branches
        NumBranch bx = branches.get(x);
        double e = 0;
        double s = 0;
        double v = 0;
        for (int y=0; y<ls; ++y ) {
          NumCycle cy = indep_cycles.get(y);
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
          double a = Math.atan2( dy, dx ) * 180 / Math.PI;
          if ( a < 0 ) a += 360;
          leg.shot.mAvgLeg.mDecl = (float)(a - leg.a); // per shot declination
        }
      }
    }
  }

  // ------------------------------------------------------------------------------
  // ALGORITMS

  /** compute the 3D vector of a point along a leg
   * @param s     abscissa along the leg: 0 = FROM station, 1 = TO station
   * @param blk   leg
   * N.B. offset for geocoord of origin must be handled by caller
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

}


