/* @file DistoXNum.java
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
package com.topodroid.DistoX;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.Locale;

import android.util.Log;

class DistoXNum
{
  NumStation mStartStation; 

  /* bounding box */
  private float mSmin; // south
  private float mSmax;
  private float mEmin; // east
  private float mEmax;
  private float mVmin; // vertical - including duplicate shots
  private float mVmax;
  private float mHmin; // horizontal
  private float mHmax;
  private float mDecl;

  /* statistics - not including survey shots */
  private float mZmin; // Z depth 
  private float mZmax;
  private float mLength;  // survey length 
  private float mExtLen;  // survey "extended" length (on extended profile)
  private float mProjLen; // survey projected length (on horiz plane)
  private int mDupNr;  // number of duplicate shots
  private int mSurfNr; // number of surface shots

  private float mErr0; // angular error distribution
  private float mErr1;
  private float mErr2;

  private void resetStats()
  {
    mLength  = 0.0f;
    mExtLen  = 0.0f;
    mProjLen = 0.0f;
    mDupNr   = 0;
    mSurfNr  = 0;
    mErr0 = mErr1 = mErr2 = 0;
  }

  private void addToStats( TriShot ts )
  {
    int size = ts.blocks.size();
    for ( int i = 0; i < size; ++i ) {
      DBlock blk1 = ts.blocks.get(i);
      for ( int j = i+1; j < size; ++j ) {
        DBlock blk2 = ts.blocks.get(j);
        float e = blk1.relativeAngle( blk2 );
        mErr0 += 1;
        mErr1 += e;
        mErr2 += e*e;
      }
    }
  }

  private void addToStats( boolean d, boolean s, float l, float e, float h )
  {
    if ( d ) ++mDupNr;
    if ( s ) ++mSurfNr;
    if ( ! ( d || s ) ) {
      mLength  += l;
      mExtLen  += e;
      mProjLen += h;
    }
  }

  private void addToStats( boolean d, boolean s, float l, float e, float h, float v )
  {
    if ( d ) ++mDupNr;
    if ( s ) ++mSurfNr;
    if ( ! ( d || s ) ) {
      mLength  += l;
      mExtLen  += e;
      mProjLen += h;
      if ( v < mZmin ) { mZmin = v; }
      if ( v > mZmax ) { mZmax = v; }
    }
  }

  // --------------------------------------------------------

  // FIXME make mStations a hashmap (key station name)
  // private ArrayList<NumStation> mStations;
  private NumStationSet mStations;
  private ArrayList<NumStation> mClosureStations;
  private ArrayList<NumShot>    mShots;
  private ArrayList<NumSplay>   mSplays;
  private ArrayList<String>     mClosures;
  private ArrayList<NumNode>    mNodes;


  int stationsNr()  { return mStations.size(); }
  int shotsNr()     { return mShots.size(); }
  int duplicateNr() { return mDupNr; }
  int surfaceNr()   { return mSurfNr; }
  int splaysNr()    { return mSplays.size(); }
  int loopNr()      { return mClosures.size(); }

  float surveyLength()  { return mLength; }
  float surveyExtLen()  { return mExtLen; }
  float surveyProjLen() { return mProjLen; }
  float surveyTop()     { return -mZmin; } // top must be positive
  float surveyBottom()  { return -mZmax; } // bottom must be negative

  float angleErrorMean()   { return mErr1; } // radians
  float angleErrorStddev() { return mErr2; } // radians

  boolean surveyAttached; //!< whether the survey is attached
  boolean surveyExtend;

  List<NumStation> getStations() { return mStations.getStations(); }
  List<NumStation> getClosureStations() { return mClosureStations; }
  List<NumShot>    getShots()    { return mShots; }
  List<NumSplay>   getSplays()   { return mSplays; }
  List<String>     getClosures() { return mClosures; }

  List<NumSplay>   getSplaysAt( NumStation st )
  {
    ArrayList< NumSplay > ret = new ArrayList<>();
    for ( NumSplay splay : mSplays ) {
      if ( splay.getBlock().isSplay() && st == splay.from ) {
        ret.add( splay );
      }
    }
    return ret;
  }

  // get shots at station st, except shot [st,except]
  List<NumShot> getShotsAt( NumStation st, NumStation except )
  {
    ArrayList<NumShot> ret = new ArrayList<>();
    for ( NumShot shot : mShots ) {
      if ( ( shot.from == st && shot.to   != except ) 
        || ( shot.to   == st && shot.from != except ) ) {
        ret.add( shot );
      }
    }
    return ret;
  }

  /** FIXME there is a problem here:               ,-----B---
   * if the reduction tree has a branch, say 0----A
   *                                               `---C----D
   * when B, C are both hidden the left side of the tree is not shown.
   * If B gets un-hidden the line 0--A--B gets shown as well as C---D
   * and these two pieces remain separated.
   */
  // hide = +1 to hide, -1 to show
  void setStationHidden( String name, int hide )
  {
    // Log.v("DistoX", "Set Station Hidden: " + hide );
    NumStation st = getStation( name );
    if ( st == null ) return;
    st.mBarrierAndHidden = ( st.mHidden == -1 && hide == 1 );
    st.mHidden += hide;
    // Log.v("DistoX", "station " + st.name + " hide " + st.mHidden );
    hide *= 2;
    st = st.mParent;
    while ( st != null ) {
      st.mHidden += hide;
      if ( st.mHidden < 0 ) st.mHidden = 0;
      // Log.v("DistoX", "station " + st.name + " hide " + st.mHidden );
      st = st.mParent;
    }
  }

  private void setStationsHide( String hide )
  {
    if ( hide == null ) return;
    String[] names = hide.split(" ");
    for ( int k=0; k<names.length; ++k ) {
      if ( names[k].length() > 0 ) setStationHidden( names[k], 1 );
    }
  }

  // barrier = +1 (set barrier), -1 (unset barrier)
  void setStationBarrier( String name, int barrier )
  {
    // Log.v("DistoX", "Set Station barrier: " + barrier );
    NumStation st = getStation( name );
    if ( st == null ) return;
    st.mBarrierAndHidden = ( st.mHidden == 1 && barrier == 1 );
    st.mHidden -= barrier;
    barrier *= 2;
    Stack<NumStation> stack = new Stack<NumStation>();
    stack.push( st );
    while ( ! stack.empty() ) {
      st = stack.pop();
      // Log.v("DistoX", "station " + st.name + " hide " + st.mHidden );
      mStations.updateHidden( st, -barrier, stack );

      // for ( NumStation s : mStations ) {
      //   if ( s.mParent == st ) {
      //     s.mHidden -= barrier;
      //     stack.push( s );
      //   }
      // }
    }
  }

  private void setStationsBarr( String barr )
  {
    if ( barr == null ) return;
    String[] names = barr.split(" ");
    for ( int k=0; k<names.length; ++k ) {
      if ( names[k].length() > 0 ) setStationBarrier( names[k], 1 );
    }
  }

  boolean isHidden( String name )
  {
    NumStation st = getStation( name );
    return ( st != null && st.hidden() );
  }

  boolean isBarrier( String name )
  {
    NumStation st = getStation( name );
    return ( st != null && st.barrier() );
  }

  // for the shot FROM-TO
  int canBarrierHidden( String from, String to )
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

  // ================================================================================
  /** create the numerical centerline
   * @param data     list of survey data
   * @param start    start station
   * @param view     barriers list
   * @param hide     hiding list
   */
  DistoXNum( List<DBlock> data, String start, String view, String hide, float decl )
  {
    mDecl = decl;
    surveyExtend   = true;
    surveyAttached = computeNum( data, start );
    setStationsHide( hide );
    setStationsBarr( view );
  }

  // public void dump()
  // {
  //   TDLog.Log( TopoDroiaLog.LOG_NUM, "DistoXNum Stations:" );
  //   for ( NumStation st : mStations ) {
  //     TDLog.Log( TopoDroiaLog.LOG_NUM, "   " + st.name + " S: " + st.s + " E: " + st.e );
  //   }
  //   TDLog.Log( TopoDroiaLog.LOG_NUM, "Shots:" );
  //   for ( NumShot sh : mShots ) {
  //     TDLog.Log( TopoDroiaLog.LOG_NUM, "   From: " + sh.from.name + " To: " + sh.to.name );
  //   }
  // } 

  /** shortest-path algo
   * @param s1  first station
   * @param s2  second station
   */
  private float shortestPath( NumStation s1, NumStation s2 )
  {
    Stack<NumStation> stack = new Stack<NumStation>();
    mStations.setShortestPath( 100000.0f );
    // for ( NumStation s : mStations ) {
    //   s.mShortpathDist = 100000.0f;
    //   // s.path = null;
    // }

    s1.mShortpathDist = 0.0f;
    stack.push( s1 );
    while ( ! stack.empty() ) {
      NumStation s = stack.pop();
      for ( NumShot e : mShots ) {
        if ( e.from == s && e.to != null ) {
          float d = s.mShortpathDist + e.length();
          if ( d < e.to.mShortpathDist ) {
            e.to.mShortpathDist = d;
            // e.to.path = from;
            stack.push( e.to );
          }
        } else if ( e.to == s && e.from != null ) {
          float d = s.mShortpathDist + e.length();
          if ( d < e.from.mShortpathDist ) {
            e.from.mShortpathDist = d;
            // e.from.path = from;
            stack.push( e.from );
          }
        }
      }
    }
    return s2.mShortpathDist;
  }

  // FIXME use hashmap
  NumStation getStation( String id ) 
  {
    if ( id == null ) return null;
    return mStations.getStation( id );
    // for (NumStation st : mStations ) if ( id.equals(st.name) ) return st;
    // return null;
  }

  NumShot getShot( String s1, String s2 )
  {
    if ( s1 == null || s2 == null ) return null;
    for (NumShot sh : mShots ) {
      if ( s1.equals( sh.from.name ) && s2.equals( sh.to.name ) ) return sh;
      if ( s2.equals( sh.from.name ) && s1.equals( sh.to.name ) ) return sh;
    }
    return null;
  }

  NumShot getShot( NumStation st1, NumStation st2 )
  {
    if ( st1 == null || st2 == null ) return null;
    for (NumShot sh : mShots ) {
      if ( ( st1 == sh.from && st2 == sh.to ) ||
           ( st2 == sh.from && st1 == sh.to ) ) return sh;
    }
    return null;
  }

  // return +1 if has shot s1-s2
  //        -1 if has shot s2-s1
  //         0 otherwise
  private int hasShot( String s1, String s2 )
  {
    if ( s1 == null || s2 == null ) return 0;
    for (NumShot sh : mShots ) {
      if ( s1.equals( sh.from.name ) && s2.equals( sh.to.name ) ) return sh.mDirection;
      if ( s2.equals( sh.from.name ) && s1.equals( sh.to.name ) ) return -sh.mDirection;
    }
    return 0;
  }


  private void resetBBox()
  {
    mSmin = 0.0f; // clear BBox
    mSmax = 0.0f;
    mEmin = 0.0f;
    mEmax = 0.0f;
    mHmin = 0.0f;
    mHmax = 0.0f;
    mVmin = 0.0f;
    mVmax = 0.0f;
    mZmin = 0.0f;
    mZmax = 0.0f;
  }

  private void updateBBox( NumSurveyPoint s )
  {
    if ( s.s < mSmin ) mSmin = s.s;
    if ( s.s > mSmax ) mSmax = s.s;
    if ( s.e < mEmin ) mEmin = s.e;
    if ( s.e > mEmax ) mEmax = s.e;
    if ( s.h < mHmin ) mHmin = s.h;
    if ( s.h > mHmax ) mHmax = s.h;
    if ( s.v < mVmin ) mVmin = s.v;
    if ( s.v > mVmax ) mVmax = s.v;
  }

  float surveyNorth() { return (mSmin < 0)? -mSmin : 0; }
  float surveySouth() { return mSmax; }
  float surveyWest() { return (mEmin < 0)? -mEmin : 0; }
  float surveyEast() { return mEmax; }
  float surveySmin() { return mSmin; }
  float surveySmax() { return mSmax; }
  float surveyEmin() { return mEmin; }
  float surveyEmax() { return mEmax; }
  float surveyHmin() { return mHmin; }
  float surveyHmax() { return mHmax; }
  float surveyVmin() { return mVmin; }
  float surveyVmax() { return mVmax; }

  // ----------------------------------------------------------------------------
  /** add a shot to a station (possibly forwarded to the station's node)
   *  a station usually has two shots, s1 and s2, at most
   *  if it has more than two shots, the additional shots are kept on a node
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
   */
  private void addShotToStations( NumShot sh, NumStation st1, NumStation st2 )
  {
    addShotToStation( sh, st1 );
    addShotToStation( sh, st2 );
    mShots.add( sh );
  }

  // ==========================================================================
  // /** insert a set of new shots into the survey
  //  * @param data    list of new shots
  //  *
  //  * @note the new shots are assumed not to close any loop
  //  */
  // public boolean addNewData( List<DBlock> data )
  // {
  //   NumShot lastLeg = null;
  //
  //   boolean ret = true;
  //   NumStation sf, st;
  //   for ( DBlock block : data ) {
  //     switch ( block.getBlockType() ) {
  //       case DBlock.BLOCK_SPLAY:
  //         // Log.v( TopoDroidApp.TAG, "add splay " + block.mFrom );
  //         lastLeg = null;
  //         break;
  //       case DBlock.BLOCK_SEC_LEG:
  //         if ( lastLeg != null ) {
  //           lastLeg.addBlock( block );
  //         }
  //         break;
  //       case DBlock.BLOCK_MAIN_LEG:
  //         sf = getStation( block.mFrom );
  //         st = getStation( block.mTo );
  //         // Log.v( TopoDroidApp.TAG, "add centerline leg " + block.mFrom + " " + block.mTo + " FROM " + sf + " TO " + st );
  //         if ( sf != null ) {
  //           mLength += block.mLength;
  //           if ( st != null ) { // close loop
  //             if ( /* TopoDroidApp.mAutoStations || */ TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) {
  //               NumStation st1 = new NumStation( block.mTo, sf, block.mLength, block.mBearing+ mDecl , block.mClino, block.getStretchedExtend() );
  //               mStations.add( st1 );
  //               st1.addAzimuth( (block.mBearing+ mDecl +180)%360, block.getNegExtend() );
  //               st1.mDuplicate = true;
  //               lastLeg = new NumShot( sf, st1, block, 1, mDecl );
  //               addShotToStations( lastLeg, st1, sf );
  //             } else { // loop-closure
  //               lastLeg = new NumShot( sf, st, block, 1, mDecl );
  //               addShotToStations( lastLeg, sf, st );
  //             }
  //             // if ( ts.duplicate ) { // FIXME
  //             //   ++mDupNr;
  //             // } 
  //             // if ( block.isSurface() ) { // FIXME
  //             //   ++mSurfNr;
  //             // }
  //             // if ( block.isCommented() ) { // FIXME
  //             //   ++mCmtdNr;
  //             // }
  //             // do close loop also on duplicate shots
  //             // need the loop length to compute the fractional closure error
  //             float length = shortestPath( sf, st) + block.mLength;
  //             mClosures.add( getClosureError( st, sf, block.mLength, block.mBearing+ mDecl , block.mClino, length ) );
  //           } 
  //           else
  //           { // add regular from-->to leg' first shot
  //             // FIXME temporary "st" coordinates
  //             st = new NumStation( block.mTo, sf, block.mLength, block.mBearing+ mDecl , block.mClino, block.getStretchedExtend() );
  //             mStations.add( st );
  //             st.addAzimuth( (block.mBearing+ mDecl +180)%360, block.getNegExtend() );
  //             updateBBox( st );
  //             // if ( ts.duplicate ) { // FIXME
  //             //   ++mDupNr;
  //             // } else if ( ts.surface ) {
  //             //   ++mSurfNr;
  //             // } 
  //             mLength += block.mLength;
  //             if ( st.v < mZmin ) { mZmin = st.v; }
  //             if ( st.v > mZmax ) { mZmax = st.v; }
  //             lastLeg = new NumShot( sf, st, block, 1, mDecl );
  //             addShotToStations( lastLeg, st, sf );
  //           }
  //         }
  //         else if ( st != null )
  //         { // sf == null && st != null
  //           sf = new NumStation( block.mFrom, st, -block.mLength, block.mBearing+ mDecl , block.mClino, block.getStretchedExtend() );
  //           mStations.add( sf );
  //           sf.addLeg( block.mBearing+ mDecl , block.getStretchedExtend() );
  //           updateBBox( sf );
  //           // if ( ts.duplicate ) {
  //           //   ++mDupNr;
  //           // } else if ( ts.surface ) {
  //           //   ++mSurfNr;
  //           // } 
  //           mLength += block.mLength;
  //           if ( sf.v < mZmin ) { mZmin = sf.v; }
  //           if ( sf.v > mZmax ) { mZmax = sf.v; }
  //           addShotToStations( new NumShot( st, sf, block, -1), sf, st, mDecl );
  //         }
  //         else 
  //         { // sf == null && st == null
  //           // secondary leg shot ?
  //           if ( lastLeg != null ) {
  //             if ( block.isRelativeDistance( lastLeg.getFirstBlock() ) )  {
  //               lastLeg.addBlock( block );
  //             } else { // splay
  //               lastLeg = null;
  //             }
  //           }
  //           ret = false;
  //         }
  //         break;
  //       default:
  //         // Log.v( TopoDroidApp.TAG, "add unknown " + block.mType + " " + block.mFrom + " " + block.mTo );
  //         lastLeg = null;
  //         break;
  //     }
  //   }

  //   // dump debug
  //   // Log.v( TopoDroidApp.TAG, "shots " + mShots.size() );
  //   // for ( NumShot sh : mShots ) {
  //   //   Log.v( TopoDroidApp.TAG, sh.from.name + "-" + sh.to.name + " [" + sh.blocks.size() + "] " + sh.mLength + " " + sh.mBearing + " " + sh.mClino );
  //   // }

  //   int rev = 0;
  //   for ( DBlock block : data ) {
  //     if ( block.isSplay() ) {
  //       // Log.v( TopoDroidApp.TAG, "add splay " + block.mFrom );
  //       String f = block.mFrom;
  //       rev = 1;
  //       if ( f == null || f.length() == 0 ) {
  //         f = block.mTo;
  //         rev = -1;
  //       }
  //       if ( f != null && f.length() > 0 ) {
  //         sf = getStation( f ); // find station with name "f"
  //         if ( sf != null ) {              // add splay at station
  //           mSplays.add( new NumSplay( sf, rev*block.mLength, block.mBearing+ mDecl , block.mClino,
  //                                     block.getStretchedExtend(), block, mDecl ) );
  //         }
  //       } else {
  //         ret = false;
  //       }
  //     }
  //   }
  //     
  //   return ret;
  // }


  /** correct temporary shots using trilateration
   * @param shots temporary shot list
   */
  private void makeTrilateration( List<TriShot> shots )
  {
    ArrayList<TriCluster> clusters = new ArrayList<>();
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

  // ================================================================

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
    // Log.v("DistoX", "make shot " + sf.name + "-" + st.name + " blocks " + ts.blocks.size() + " E " + blk.getExtend() + " S " + blk.getStretch() );
    // NumShot sh = new NumShot( sf, st, ts.getFirstBlock(), 1, anomaly, mDecl ); // FIXME DIRECTION
    NumShot sh = new NumShot( sf, st, ts.getFirstBlock(), direction, anomaly, mDecl );
    ArrayList<DBlock> blks = ts.getBlocks();
    for ( int k = 1; k < blks.size(); ++k ) {
      sh.addBlock( blks.get(k) );
    }
    return sh;
  }

  /** survey data reduction 
   * return true if all shots are attached
   */
  private boolean computeNum( List<DBlock> data, String start )
  {
    resetBBox();
    resetStats();
    mStartStation = null;
    int nrSiblings = 0;

    // long millis_start = System.currentTimeMillis();
    
    // mStations = new ArrayList<>();
    mStations = new NumStationSet();
    mClosureStations = new ArrayList<>();
    mShots    = new ArrayList<>();
    mSplays   = new ArrayList<>();
    mClosures = new ArrayList<>();
    mNodes    = new ArrayList<>();

    TriShot lastLeg = null;
    List<TriShot> tmpshots   = new ArrayList<>();
    List<TriSplay> tmpsplays = new ArrayList<>();

    for ( DBlock blk : data ) {
      // Log.v("DistoX", "NUM blk type " + blk.mType );
      switch ( blk.getBlockType() ) {

        case DBlock.BLOCK_SPLAY:
        case DBlock.BLOCK_X_SPLAY:
          lastLeg = null;  // clear last-leg
          if ( blk.mFrom != null && blk.mFrom.length() > 0 ) { // normal splay
            tmpsplays.add( new TriSplay( blk, blk.mFrom, blk.getFullExtend(), +1 ) );
          } else if ( blk.mTo != null && blk.mTo.length() > 0 ) { // reversed splay
            tmpsplays.add( new TriSplay( blk, blk.mTo, blk.getFullExtend(), -1 ) );
          }
          break;

        case DBlock.BLOCK_MAIN_LEG:
          lastLeg = new TriShot( blk, blk.mFrom, blk.mTo, blk.getExtend(), blk.getStretch(), +1 );
          lastLeg.duplicate = ( blk.isDuplicate() );
          lastLeg.surface   = ( blk.isSurface() );
          lastLeg.commented = ( blk.isCommented() );
          // lastLeg.backshot  = 0;
          if ( blk.getExtend() > 1 ) surveyExtend = false;
          tmpshots.add( lastLeg );
          break;

        case DBlock.BLOCK_BACK_LEG:
          lastLeg = new TriShot( blk, blk.mFrom, blk.mTo, blk.getExtend(), blk.getStretch(), +1 );
          lastLeg.duplicate = true;
          lastLeg.surface   = ( blk.isSurface() );
          lastLeg.commented = false;
          // lastLeg.backshot  = 0;
          if ( blk.getExtend() > 1 ) surveyExtend = false;
          tmpshots.add( lastLeg );
          break;

        case DBlock.BLOCK_SEC_LEG:
          if (lastLeg != null) lastLeg.addBlock( blk );
          break;
        case DBlock.BLOCK_BLANK_LEG:
        case DBlock.BLOCK_BLANK:
          if (lastLeg != null ) {
            if ( blk.isRelativeDistance( lastLeg.getFirstBlock() ) ) {
              lastLeg.addBlock( blk );
            }
          }
          break;
      }
    }

    if ( TDSetting.mLoopClosure == TDSetting.LOOP_TRIANGLES ) {
      makeTrilateration( tmpshots );
    }

    // if ( TDLog.LOG_DEBUG ) {
    //   Log.v( TDLog.TAG, "DistoXNum::compute tmp-shots " + tmpshots.size() + " tmp-splays " + tmpsplays.size() );
    //   for ( TriShot ts : tmpshots ) ts.Dump();
    // }
    for ( TriShot tsh : tmpshots ) { // clear backshot, sibling, and multibad
      tsh.backshot = 0;
      tsh.sibling  = null;
      tsh.getFirstBlock().mMultiBad = false;
    }

    // dump tmpshots
    // Log.v("DistoXL", "tmp shots " + tmpshots.size() );
    // for ( TriShot tr : tmpshots ) tr.dump();

    for ( int i = 0; i < tmpshots.size(); ++i ) {
      TriShot ts0 = tmpshots.get( i );
      addToStats( ts0 );
      if ( ts0.backshot != 0 ) continue; // skip siblings

      DBlock blk0 = ts0.getFirstBlock();
      // (1) check if ts0 has siblings
      String from = ts0.from;
      String to   = ts0.to;
      // Log.v("DistoXL", "working shot " + from + "-" + to );
      // if ( from == null || to == null ) continue; // FIXME
      TriShot ts1 = ts0; // last sibling (head = the shot itself)
      for ( int j=i+1; j < tmpshots.size(); ++j ) {
        TriShot ts2 = tmpshots.get( j );
        if ( from.equals( ts2.from ) && to.equals( ts2.to ) ) { // chain a positive sibling
          ts1.sibling = ts2;
          ts1 = ts2;
          ts2.backshot = +1;
	  ++ nrSiblings;
        } else if ( from.equals( ts2.to ) && to.equals( ts2.from ) ) { // chain a negative sibling
          ts1.sibling = ts2;
          ts1 = ts2;
          ts2.backshot = -1;
	  ++ nrSiblings;
        }
      }
      // Log.v("DistoXL", "worked shot " + from + "-" + to + " siblings " + nrSiblings );
      
      if ( ts0.sibling != null ) { // (2) check sibling shots agreement
        float dmax = 0.0f;
        float cc = TDMath.cosd( blk0.mClino );
        float sc = TDMath.sind( blk0.mClino );
        float cb = TDMath.cosd( blk0.mBearing + mDecl ); 
        float sb = TDMath.sind( blk0.mBearing + mDecl ); 
        Vector v1 = new Vector( blk0.mLength * cc * sb, blk0.mLength * cc * cb, blk0.mLength * sc );
        ts1 = ts0.sibling;
        while ( ts1 != null ) {
          DBlock blk1 = ts1.getFirstBlock();
          cc = TDMath.cosd( blk1.mClino );
          sc = TDMath.sind( blk1.mClino );
          cb = TDMath.cosd( blk1.mBearing + mDecl ); 
          sb = TDMath.sind( blk1.mBearing + mDecl ); 
          Vector v2 = new Vector( blk1.mLength * cc * sb, blk1.mLength * cc * cb, blk1.mLength * sc );
          float d = ( ( ts1.backshot == -1 )? v1.plus(v2) : v1.minus(v2) ).Length();
          d = d/blk0.mLength + d/blk1.mLength; 
          if ( d > dmax ) dmax = d;
          ts1 = ts1.sibling;
        }
        if ( ( ! StationPolicy.doMagAnomaly() ) && ( dmax > TDSetting.mCloseDistance ) ) {
          blk0.mMultiBad = true;
        }
        // Log.v( "DistoX", "DMAX " + from + "-" + to + " " + dmax );
        
        if ( ! StationPolicy.doMagAnomaly() ) { // (3) remove siblings
          ts1 = ts0.sibling;
          while ( ts1 != null ) {
	    -- nrSiblings;
            // Log.v( "DistoXL", "removing sibling " + ts1.from + "-" + ts1.to + " : " + nrSiblings );
            TriShot ts2 = ts1.sibling;
            tmpshots.remove( ts1 );
            ts1 = ts2;
          }
          ts0.sibling = null;
        // } else {
        //   for ( ts1 = ts0.sibling; ts1 != null; ts1 = ts1.sibling ) {
        //     assert ( ts1.backshot != 0 );
        //     // Log.v("DistoX", from + "-" + to  + " zero backshot sibling " + ts1.from + "-" + ts1.to );
        //   }
        }
      }
    }

    if ( mErr0 > 0 ) {
      mErr1 /= mErr0;
      mErr2 = (float)Math.sqrt( mErr2/mErr0 - mErr1*mErr1 );
    }

    // if ( TDLog.LOG_DEBUG ) {
    //   Log.v( TDLog.TAG, "DistoXNum::compute tmp-shots " + tmpshots.size() + " tmp-splays " + tmpsplays.size() );
    //   for ( TriShot ts : tmpshots ) ts.Dump();
    // }

    mStartStation = new NumStation( start );
    mStartStation.mHasCoords = true;
    mStations.addStation( mStartStation );

    // if ( TDLog.LOG_DEBUG ) Log.v( TDLog.TAG, "start station " + start +  " shots " + tmpshots.size() );
    // dump tmpshots
    // Log.v("DistoXL", "after sibling processing: " + tmpshots.size() );
    // for ( TriShot tr : tmpshots ) tr.dump();

    NumShot sh;

    // two-pass data reduction
    // first-pass all shots with regular extends
    // second-pass any leftover shot
    for ( int pass = 0; pass < 2; ++ pass ) {
      boolean repeat = true;
      while ( repeat ) {
        repeat = false;
        for ( TriShot ts : tmpshots ) {
          if ( ts.used || ts.backshot != 0 ) continue;                  // skip used and siblings
          if ( pass == 0 && DBlock.getExtend(ts.extend) > 1 ) continue; // first pass skip non-extended

          float anomaly = 0;
          if ( StationPolicy.doMagAnomaly() ) {
            // if ( ts.backshot == 0 ) 
            {
              // TDLog.Log(TDLog.LOG_NUM, "shot " + ts.from + " " + ts.to + " <" + ts.backshot + ">" );
              int   nfwd = 1;      // nr of forward
              float bfwd = ts.b(); // forward bearing
              int   nbck = 0;      // nr of backward
              float bbck = 0;      // backward bearing
              for ( TriShot ts1 = ts.sibling; ts1 != null; ts1 = ts1.sibling ) {
                // TDLog.Log(TDLog.LOG_NUM, "sibling " + ts1.from + " " + ts1.to + " <" + ts1.backshot + ">" );
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
	      // Log.v("DistoXX", "anomaly " + anomaly);

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
          } 

          // try to see if any temp-shot station is on the list of stations
          NumStation sf = getStation( ts.from );
          NumStation st = getStation( ts.to );
          // if ( TDLog.LOG_DEBUG ) {
          //   Log.v( TDLog.TAG, "using shot " + ts.from + " " + ts.to + " id " + ts.blocks.get(0).mId );
          //   ts.Dump();
          //   Log.v( TDLog.TAG, "shot " + ts.from + "-" + ts.to + " stations " +
          //     ( ( sf == null )? "null" : sf.name ) + " " + (( st == null )? "null" : st.name ) );
          // }

          int  iext = DBlock.getExtend( ts.extend );
          boolean has_coords = (iext <= 1);
          float ext = DBlock.getReducedExtend( ts.extend, ts.stretch );
          if ( sf != null ) {
            sf.addAzimuth( ts.b(), iext );

            if ( st != null ) { // loop-closure
              // Log.v("DistoXL", "loop closure at " + ts.from + "-" + ts.to );
              if ( /* TDSetting.mAutoStations || */ TDSetting.mLoopClosure == TDSetting.LOOP_NONE ) { // do not close loop
                // if ( TDLog.LOG_DEBUG ) Log.v( TDLog.TAG, "do not close loop");
                // keep loop open: new station( id=ts.to, from=sf, ... )
                float bearing = ts.b() - sf.mAnomaly;
                NumStation st1 = new NumStation( ts.to, sf, ts.d(), bearing, ts.c(), ext, has_coords );
                if ( ! mStations.addStation( st1 ) ) mClosureStations.add( st1 );

                st1.addAzimuth( (ts.b()+180)%360, -iext );
                st1.mAnomaly = anomaly + sf.mAnomaly;
	        // Log.v("DistoXX", "station " + st1.name + " anomaly " + st1.mAnomaly );
                updateBBox( st1 );
                st1.mDuplicate = true;

                sh = makeShotFromTmp( sf, st1, ts, 1, sf.mAnomaly, mDecl );
                addShotToStations( sh, st1, sf );
                // Log.v("DistoXL", "open loop at " + sf.name + " " + st.name + " TO " + ts.to );
              } else { // close loop
                // Log.v("DistoXL", "close loop at " + sf.name + " " + st.name );
                sh = makeShotFromTmp( sf, st, ts, 0, sf.mAnomaly, mDecl ); 
                addShotToStations( sh, sf, st );
              }
              // float length = ts.d();
	      // if ( iext == 0 ) length = TDMath.sqrt( length*length - ts.h()*ts.h() );
              addToStats( ts.duplicate, ts.surface, ts.d(), ((iext == 0)? Math.abs(ts.v()) : ts.d()), ts.h() );

              // do close loop also on duplicate shots
              // need the loop length to compute the fractional closure error
              float length = shortestPath( sf, st) + Math.abs( ts.d() );  // FIXME length
              mClosures.add( getClosureError( st, sf, ts.d(), ts.b(), ts.c(), length ) );
	      // Log.v("DistoXL", "add closure " + sf.name + " " + st.name + " len " + length );
              
              ts.used = true;
              repeat = true;
            }
            else // st null || st isBarrier
            { // forward shot: from --> to
              // Log.v("DistoXL", "forward leg " + ts.from + "-" + ts.to );
              float bearing = ts.b() - sf.mAnomaly;
              st = new NumStation( ts.to, sf, ts.d(), bearing, ts.c(), ext, has_coords );
              if ( ! mStations.addStation( st ) ) mClosureStations.add( st );

              st.addAzimuth( (ts.b()+180)%360, -iext );
              st.mAnomaly = anomaly + sf.mAnomaly;
	      // Log.v("DistoXX", "station " + st.name + " anomaly " + st.mAnomaly );
              updateBBox( st );
              addToStats( ts.duplicate, ts.surface, ts.d(), ((iext == 0)? Math.abs(ts.v()) : ts.d()), ts.h(), st.v );

              // if ( TDLog.LOG_DEBUG ) {
              //   Log.v( TDLog.TAG, "new station F->T id= " + ts.to + " from= " + sf.name + " anomaly " + anomaly + " d " + ts.d() ); 
              //   Log.v( TDLog.TAG, "  " + st.e + " " + st.s + " : " + st.h + " " + st.v );
              // }

              sh = makeShotFromTmp( sf, st, ts, 1, sf.mAnomaly, mDecl );
              addShotToStations( sh, st, sf );
              ts.used = true;
              repeat = true;
            }
          }
          else if ( st != null ) 
          { // sf == null: reversed shot only difference is '-' sign in new NumStation, and the new station is sf
            // if ( TDLog.LOG_DEBUG ) Log.v( TDLog.TAG, "reversed shot " + ts.from + " " + ts.to + " id " + ts.blocks.get(0).mId );
            // Log.v("DistoXL", "reversed leg " + ts.from + "-" + ts.to );
            st.addAzimuth( (ts.b()+180)%360, -iext );
            float bearing = ts.b() - st.mAnomaly;
            sf = new NumStation( ts.from, st, - ts.d(), bearing, ts.c(), ext, has_coords );
            if ( ! mStations.addStation( sf ) ) mClosureStations.add( sf );

            sf.addAzimuth( ts.b(), iext );
            sf.mAnomaly = anomaly + st.mAnomaly; // FIXME
	    // Log.v("DistoXX", "station " + sf.name + " anomaly " + sf.mAnomaly );
            // if ( TDLog.LOG_DEBUG ) {
            //   Log.v( TDLog.TAG, "new station T->F id= " + ts.from + " from= " + st.name + " anomaly " + anomaly ); 
            //   Log.v( TDLog.TAG, "  " + sf.e + " " + sf.s + " : " + sf.h + " " + sf.v );
            // }

            updateBBox( sf );
            addToStats( ts.duplicate, ts.surface, Math.abs(ts.d() ), ts.h(), sf.v );

            // FIXME is st.mAnomaly OK ?
            // N.B. was new NumShot(st, sf, ts.block, -1, mDecl); // FIXME check -anomaly
            sh = makeShotFromTmp( sf, st, ts, -1, st.mAnomaly, mDecl );
            addShotToStations( sh, sf, st );
            ts.used = true;
            repeat = true;
          }
        }
      }
    }
    // if ( TDLog.LOG_DEBUG ) Log.v( TDLog.TAG, "DistoXNum::compute done leg shots, stations  " + mStations.size() );

    // Log.v("DistoXL", "shots " + mShots.size() + " loops " + mClosures.size() + " siblings " + nrSiblings + " tmp " + tmpshots.size() );
    if ( TDSetting.mLoopClosure == TDSetting.LOOP_CYCLES ) {
      // TDLog.Log( TDLog.LOG_NUM, "loop compensation");
      doLoopCompensation( mNodes, mShots );
  
      // recompute station positions
      for ( NumShot sh1 : mShots ) {
        sh1.mUsed = false;
      }
      mStations.setCoords( false );
      // for ( NumStation st : mStations ) { // mark stations as unset
      //   st.mHasCoords = false;
      // }
      mStartStation.mHasCoords = true;

      boolean repeat = true;
      while ( repeat ) {
        repeat = false;
        for ( NumShot sh2 : mShots ) {
          if ( sh2.mUsed ) continue;
          NumStation s1 = sh2.from;
          NumStation s2 = sh2.to;
          float c2 = sh2.clino();
          float b2 = sh2.bearing() + mDecl;
          if ( s1.mHasCoords && ! s2.mHasCoords ) {
            // reset s2 values from the shot
            // float d = sh2.length() * sh2.mDirection; // FIXME DIRECTION
            float d = sh2.length();
            float v = - d * TDMath.sind( c2 );
            float h =   d * TDMath.cosd( c2 );
            float e =   h * TDMath.sind( b2 );
            float s = - h * TDMath.cosd( b2 );
            s2.e = s1.e + e;
            s2.s = s1.s + s;
            s2.v = s1.v + v;
            s2.mHasCoords = true;
            sh2.mUsed = true;
            repeat = true;
            // if ( TDLog.LOG_DEBUG )  Log.v( TDLog.TAG, "reset " + s1.name + "->" + s2.name + " " + e + " " + s + " " + v );
          } else if ( s2.mHasCoords && ! s1.mHasCoords ) {
            // reset s1 values from the shot
            // float d = - sh2.length() * sh2.mDirection; // FIXME DIRECTION
            float d = - sh2.length();
            float v = - d * TDMath.sind( c2 );
            float h =   d * TDMath.cosd( c2 );
            float e =   h * TDMath.sind( b2 );
            float s = - h * TDMath.cosd( b2 );
            s1.e = s2.e + e;
            s1.s = s2.s + s;
            s1.v = s2.v + v;
            s1.mHasCoords = true;
            sh2.mUsed = true;
            repeat = true;
            // if ( TDLog.LOG_DEBUG )  Log.v( TDLog.TAG, "reset " + s1.name + "<-" + s2.name + " " + e + " " + s + " " + v );
          }
          
        }
      }
    }

    mStations.setAzimuths();
    // for ( NumStation st : mStations ) {
    //   st.setAzimuths();
    // }
    for ( TriSplay ts : tmpsplays ) {
      NumStation st = getStation( ts.from );
      if ( st != null ) {
        float extend = st.computeExtend( ts.b( mDecl ), ts.extend ); // FIXME_EXTEND
        mSplays.add( new NumSplay( st, ts.d(), ts.b( mDecl ), ts.c(), extend, ts.block, mDecl ) );
      }
    }

    // long millis_end = System.currentTimeMillis() - millis_start;
    // Log.v("DistoX", "Data reduction " + millis_end + " msec" );

    return (mShots.size() + nrSiblings == tmpshots.size() );
  }

  

  // =============================================================================
  // loop closure-error compensation
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

    NumStep top() 
    {
      return ( pos > 0 )? data[pos-1] : null;
    }

    boolean empty() { return pos == 0; }
  
    void pop()
    {
      if ( pos > 0 ) { 
        --pos;
      }
    }
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

  private void makeCycles( ArrayList<NumCycle> cycles, ArrayList<NumBranch> branches ) 
  {
    int bs = branches.size();
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
          s1.b.use = 0;
          s1.n.use = 0;
          stack.pop();
        } else {
          int k2 = s1.k;
          for ( ; k2<bs; ++k2 ) {
            NumBranch b2 = branches.get(k2);
            if ( b2.use != 0 ) continue;
            NumNode n2 = b2.otherNode( n1 );
            if ( n2 != null && n2.use == 0 ) {
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
      n0.use = 2;
    }
  }


  private void makeSingleLoops( ArrayList<NumBranch> branches, ArrayList<NumShot> shots )
  {
    for ( NumShot shot : shots ) {
      if ( shot.branch != null ) continue;
      // start a branch END_END or LOOP
      NumBranch branch = new NumBranch( NumBranch.BRANCH_LOOP, null );
      NumShot sh0 = shot;
      NumStation sf0 = sh0.from;
      NumStation st0 = sh0.to;
      sh0.mBranchDir = 1;
      // TDLog.Log( TDLog.LOG_NUM, "Start branch from station " + sf0.name );
      while ( st0 != sf0 ) { // follow the shot 
        branch.addShot( sh0 ); // add shot to branch and find next shot
        sh0.branch = branch;
        NumShot sh1 = st0.s1;
        if ( sh1 == sh0 ) { sh1 = st0.s2; }
        if ( sh1 == null ) { // dangling station: END_END branch --> drop
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

  /** for each branch compute the error and distribute it over the
   * branch shots
   */
  private void compensateSingleLoops( ArrayList<NumBranch> branches )
  {
    for ( NumBranch br : branches ) {
      br.computeError();
      // TDLog.Log( TDLog.LOG_NUM, "Single loop branch error " + br.e + " " + br.s + " " + br.v );
      br.compensateError( br.e, br.s, br.v );
    }
  }

  // follow a shot
  // good for a single line without crosses
  private ArrayList<NumShot> followShot( NumBranch br, NumStation st, boolean after )
  {
    ArrayList<NumShot> ret = new ArrayList<>();
    boolean found = true;
    while ( found ) {
      found = false;
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
    return ret;
  }

  /* make branches from this num nodes
   * @param also_cross_end     whether to include branches to end-points
   */
  ArrayList<NumBranch> makeBranches( boolean also_cross_end ) { return makeBranches( mNodes, also_cross_end ); }
  
  /** from the list of nodes make the branches of type cross-cross
   * FIXME there is a flaw:
   * this method does not detect single loops with no hair attached
   */
  private ArrayList<NumBranch> makeBranches( ArrayList<NumNode> nodes, boolean also_cross_end )
  {
    // for ( NumNode nd : nodes ) {
    //   Log.v("DistoX", "node " + nd.station.name + " branches " + nd.branches.size() );
    // }
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
            NumShot sh1 = st0.s1;
            if ( sh1 == sh0 ) { sh1 = st0.s2; }
            if ( sh1 == null ) { // dangling station: CROSS_END branch --> drop
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
    return branches;
  }

  /** matrix inverse: gauss pivoting method
   * @param a    matrix
   * @param nr   size of rows
   * @param nc   size of columns
   * @param nd   row-stride
   */
  static private float invertMatrix( float[] a, int nr, int nc, int nd)
  {
    float  det_val = 1.0f;                /* determinant value */
    int     ij, jr, ki, kj;
    int ii  = 0;                          /* index of diagonal */
    int ir  = 0;                          /* index of row      */

    for (int i = 0; i < nr; ++i, ir += nd, ii = ir + i) {
      det_val *= a[ii];                 /* new value of determinant */
      /* ------------------------------------ */
      /* if value is zero, might be underflow */
      /* ------------------------------------ */
      if (det_val == 0.0f) {
        if (a[ii] == 0.0f) {
          break;                    /* error - exit now */
        } else {                    /* must be underflow */
          det_val = 1.0e-15f;
        }
      }
      float r = 1.0f / a[ii];   /* Calculate Pivot --------------- */
      a[ii] = 1.0f;
      ij = ir;                          /* index of pivot row */
      for (int j = 0; j < nc; ++j) {
        a[ij] = r * a[ij];
        ++ij;                         /* index of next row element */
      }
      ki = i;                           /* index of ith column */
      jr = 0;                           /* index of jth row    */
      for (int k = 0; k < nr; ++k, ki += nd, jr += nd) {
        if (i != k && a[ki] != 0.0f) {
          r = a[ki];                /* pivot target */
          a[ki] = 0.0f;
          ij = ir;                  /* index of pivot row */
          kj = jr;                  /* index of jth row   */
          for (int j = 0; j < nc; ++j) { /* subtract multiples of pivot row from jth row */
            a[kj] -= r * a[ij];
            ++ij;
            ++kj;
          }
        }
      }
    }
    return det_val;
  }

  private void doLoopCompensation( ArrayList< NumNode > nodes, ArrayList< NumShot > shots )
  {
    ArrayList<NumBranch> branches = makeBranches( nodes, false );

    ArrayList< NumBranch > singleBranches = new ArrayList<>();
    makeSingleLoops( singleBranches, shots ); // check all shots without branch
    compensateSingleLoops( singleBranches );

    ArrayList<NumCycle> cycles = new ArrayList<>();
    makeCycles( cycles, branches );

    for ( NumBranch branch : branches ) { // compute branches and cycles errors
      branch.computeError();
    }
    for ( NumCycle cycle : cycles ) {
      cycle.computeError();
      // TDLog.Log( TDLog.LOG_NUM, "cycle error " + cycle.e + " " + cycle.s + " " + cycle.v ); 
    }

    ArrayList<NumCycle> indep_cycles = new ArrayList<>(); // independent cycles
    for ( NumCycle cycle : cycles ) {
      if ( ! cycle.isBranchCovered( indep_cycles ) ) {
        indep_cycles.add( cycle );
      }
    }

    int ls = indep_cycles.size();
    int bs = branches.size();

    float[] alpha = new float[ bs * ls ]; // cycle = row-index, branch = col-index
    float[] aa = new float[ ls * ls ];    // 

    for (int y=0; y<ls; ++y ) {  // branch-cycle matrix
      NumCycle cy = indep_cycles.get(y);
      for (int x=0; x<bs; ++x ) {
        NumBranch bx = branches.get(x);
        alpha[ y*bs + x] = 0.0f;
        int k = cy.branchIndex( bx );
        if ( k < cy.mSize ) {
          // alpha[ y*bs + x] = ( bx.n2 == cy.getNode(k) )? 1.0f : -1.0f;
          alpha[ y*bs + x] = cy.dirs[k];
        }
      }
    }

    for (int y1=0; y1<ls; ++y1 ) { // cycle-cycle matrix
      for (int y2=0; y2<ls; ++y2 ) {
        float a = 0.0f;
        for (int x=0; x<bs; ++x ) a += alpha[ y1*bs + x] * alpha[ y2*bs + x];
        aa[ y1*ls + y2] = a;
      }
    }
    // for (int y1=0; y1<ls; ++y1 ) { // cycle-cycle matrix
    //   StringBuilder sb = new StringBuilder();
    //   for (int y2=0; y2<ls; ++y2 ) {
    //     sb.append( Float.toString(aa[ y1*ls + y2]) ).append("  ");
    //   }
    //   TDLog.Log( TDLog.LOG_NUM, "AA " + sb.toString() );
    // }

    float det = invertMatrix( aa, ls, ls, ls );

    // for (int y1=0; y1<ls; ++y1 ) { // cycle-cycle matrix
    //   StringBuilder sb = new StringBuilder();
    //   for (int y2=0; y2<ls; ++y2 ) {
    //     sb.append( Float.toString(aa[ y1*ls + y2]) ).append("  ");
    //   }
    //   TDLog.Log( TDLog.LOG_NUM, "invAA " + sb.toString() );
    // }


    for (int y=0; y<ls; ++y ) { // compute the closure compensation values
      NumCycle cy = indep_cycles.get(y);
      cy.ce = 0.0f; // corrections
      cy.cs = 0.0f;
      cy.cv = 0.0f;
      for (int x=0; x<ls; ++x ) {
        NumCycle cx = indep_cycles.get(x);
        cy.ce += aa[ y*ls + x] * cx.e;
        cy.cs += aa[ y*ls + x] * cx.s;
        cy.cv += aa[ y*ls + x] * cx.v;
      }
      // TDLog.Log( TDLog.LOG_NUM, "cycle correction " + cy.ce + " " + cy.cs + " " + cy.cv ); 
    }
    
    for (int x=0; x<bs; ++x ) { // correct branches
      NumBranch bx = branches.get(x);
      float e = 0.0f;
      float s = 0.0f;
      float v = 0.0f;
      for (int y=0; y<ls; ++y ) {
        NumCycle cy = indep_cycles.get(y);
        e += alpha[ y*bs + x ] * cy.ce;
        s += alpha[ y*bs + x ] * cy.cs;
        v += alpha[ y*bs + x ] * cy.cv;
      }
      bx.compensateError( -e, -s, -v );
    }
  }


  /** get the string description of the loop closure error(s)
   * need the loop length to compute the percent error
   */
  private String getClosureError( NumStation at, NumStation fr, float d, float b, float c, float len )
  {
    float dv = TDMath.abs( fr.v - d * TDMath.sind(c) - at.v );
    float h0 = d * TDMath.abs( TDMath.cosd(c) );
    float ds = TDMath.abs( fr.s - h0 * TDMath.cosd( b ) - at.s );
    float de = TDMath.abs( fr.e + h0 * TDMath.sind( b ) - at.e );
    float dh = ds*ds + de*de;
    float dl = TDMath.sqrt( dh + dv*dv );
    dh = TDMath.sqrt( dh );
    float error = (dl*100) / len;
    return String.format(Locale.US, "%s-%s %.2f [%.2f %.2f] %.2f%%", fr.name, at.name,  dl, dh, dv, error );
  }
}


