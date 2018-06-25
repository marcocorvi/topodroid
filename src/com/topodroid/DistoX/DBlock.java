/* @file DBlock.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX survey data
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.lang.Long;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Locale;

import android.view.View;

import android.graphics.Paint; // custom paint

// import android.util.Log;

class DBlock
{
  // public static final char[] mExtendTag = { '<', '|', '>', ' ', '-', '.', '?', '«', 'I', '»', ' ' };
  private static final char[] mExtendTag = { '<', '|', '>', ' ', '-', '.', '?', ' ', ' ', ' ', ' ' };
  static final int EXTEND_LEFT   = -1;
  static final int EXTEND_VERT   =  0;
  static final int EXTEND_RIGHT  = 1;
  static final int EXTEND_IGNORE = 2;
  static final int EXTEND_HIDE   = 3;
  static final int EXTEND_START  = 4;

  static final int EXTEND_UNSET  = 5;
  // public static final int EXTEND_FLEFT  = 6; // LEFT = FLEFT - FVERT
  // public static final int EXTEND_FVERT  = 7;
  // public static final int EXTEND_FRIGHT = 8;
  // public static final int EXTEND_FIGNORE = 9; // overload of IGNORE for splays

  static final int EXTEND_NONE   = EXTEND_VERT;

  View   mView;
  // private int    mPos;     // position in the list
  int    mVisible; // whether is visible in the list
  boolean mMultiSelected; // whether the block is in multiselect list
  Paint  mPaint;

  long   mId;
  long   mTime;
  private long   mSurveyId;
  // private String mName;
  String mFrom;    // N.B. mfrom and mTo must be not null
  String mTo;
  float mLength;   // meters
  float mBearing;  // degrees
  float mClino;    // degrees
  float mRoll;     // degrees
  float mAcceleration;
  float mMagnetic;
  float mDip;
  String mComment;
  private int mExtend;
  private long mFlag;     
  int    mType;   
  int mShotType;  // 0: DistoX, 1: manual
  boolean mWithPhoto;
  boolean mMultiBad; // whether it disagree with siblings

  static final int BLOCK_BLANK      = 0;
  static final int BLOCK_MAIN_LEG   = 1; // primary leg shot
  static final int BLOCK_SPLAY      = 2;
  static final int BLOCK_SEC_LEG    = 3; // additional shot of a centerline leg
  static final int BLOCK_BLANK_LEG  = 4; // blank centerline leg-shot
  static final int BLOCK_X_SPLAY    = 5; // FIXME_X_SPLAY cross splay

  static final long LEG_INVALID = -1L;
  static final long LEG_NORMAL  = 0L;
  static final long LEG_EXTRA   = 1L;
  static final long LEG_XSPLAY  = 2L;

  private static final int[] colors = {
    TDColor.LIGHT_PINK,   // blank
    TDColor.WHITE,        // midline
    TDColor.LIGHT_BLUE,   // splay
    TDColor.LIGHT_GRAY,   // sec. leg
    TDColor.VIOLET,       // blank leg
    TDColor.GREEN,        // FIXME_X_SPLAY X splay
    TDColor.GREEN
  };

  static final long BLOCK_SURVEY     =  0; // flags
  static final long BLOCK_SURFACE    =  1;
  static final long BLOCK_DUPLICATE  =  2;
  static final long BLOCK_COMMENTED  =  4;
  static final long BLOCK_NO_PLAN    =  8;
  static final long BLOCK_NO_PROFILE = 16;
  // public static final long BLOCK_BACKSHOT   = 32;

  boolean isSurvey() { return mFlag == BLOCK_SURVEY; }
  boolean isSurface()   { return (mFlag & BLOCK_SURFACE)    == BLOCK_SURFACE; }
  boolean isDuplicate() { return (mFlag & BLOCK_DUPLICATE)  == BLOCK_DUPLICATE; }
  boolean isCommented() { return (mFlag & BLOCK_COMMENTED)  == BLOCK_COMMENTED; }
  boolean isNoPlan()    { return (mFlag & BLOCK_NO_PLAN)    == BLOCK_NO_PLAN; }
  boolean isNoProfile() { return (mFlag & BLOCK_NO_PROFILE) == BLOCK_NO_PROFILE; }
  // public boolean isBackshot() { return (mFlag & BLOCK_BACKSHOT) == BLOCK_BACKSHOT; }

  // static boolean isSurvey(int flag) { return flag == BLOCK_SURVEY; }
  static boolean isSurface(long flag)   { return (flag & BLOCK_SURFACE)    == BLOCK_SURFACE; }
  static boolean isDuplicate(long flag) { return (flag & BLOCK_DUPLICATE)  == BLOCK_DUPLICATE; }
  static boolean isCommented(long flag) { return (flag & BLOCK_COMMENTED)  == BLOCK_COMMENTED; }
  static boolean isNoPlan(long flag)    { return (flag & BLOCK_NO_PLAN)    == BLOCK_NO_PLAN; }
  static boolean isNoProfile(long flag) { return (flag & BLOCK_NO_PROFILE) == BLOCK_NO_PROFILE; }
  // static public boolean isBackshot(int flag) { return (flag & BLOCK_BACKSHOT) == BLOCK_BACKSHOT; }

  // void resetFlag() { mFlag = BLOCK_SURVEY; }
  void resetFlag( long flag ) { mFlag = flag; }
  void setFlag( long flag ) { mFlag |= flag; }
  // void clearFlag( long flag ) { mFlag &= ~flag; }
  long getFlag() { return mFlag; }

  void setTypeBlankLeg( ) { if ( mType == BLOCK_BLANK ) mType = BLOCK_BLANK_LEG; }
  boolean isTypeBlank() { return mType == BLOCK_BLANK || mType == BLOCK_BLANK_LEG; }
  static boolean isTypeBlank( int t ) { return t == BLOCK_BLANK || t == BLOCK_BLANK_LEG; }

  int type() { return mType; }

  static boolean isSplay( int t ) { return t == BLOCK_SPLAY || t == BLOCK_X_SPLAY; }

  boolean isSplay()  { return mType == BLOCK_SPLAY || mType == BLOCK_X_SPLAY; }
  boolean isXSplay() { return mType == BLOCK_X_SPLAY; }
  boolean isLeg()    { return mType == BLOCK_MAIN_LEG; }

  // static int getExtend( int ext ) { return ( ext < EXTEND_UNSET )? ext : ext - EXTEND_FVERT; }
  static int getExtend( int ext ) { return ext; }
  static int getReducedExtend( int ext ) 
  {
    // if ( ext >= EXTEND_UNSET ) { ext -= EXTEND_FVERT; }
    return ( ext < 2 )? ext : 0;
  }

  // int getExtend() { return ( mExtend < EXTEND_UNSET )? mExtend : mExtend - EXTEND_FVERT; }
  int getExtend() { return mExtend; }
  int getReducedExtend() 
  {
    // int ret = ( mExtend < EXTEND_UNSET )? mExtend : mExtend - EXTEND_FVERT;
    int ret = mExtend;
    return ( ret < 2 )? ret : 0;
  }
  int getFullExtend() { return mExtend; }
  void setExtend( int ext ) { mExtend = ext; }
  boolean flipExtend()
  {
    switch ( mExtend ) {
      case EXTEND_LEFT:   mExtend = EXTEND_RIGHT;  return true;
      case EXTEND_RIGHT:  mExtend = EXTEND_LEFT;   return true;
      // case EXTEND_FLEFT:  mExtend = EXTEND_FRIGHT; return true;
      // case EXTEND_FRIGHT: mExtend = EXTEND_FLEFT;  return true;
    }
    return false;
  }

  // a block is recent if
  //   - its id comes after the given id
  //   - its time is no more than 10 seconds before the given time
  boolean isRecent( ) { return mId >= TopoDroidApp.mSecondLastShotId; }
  boolean isRecent( long time ) { return mId >= TopoDroidApp.mSecondLastShotId && (time-mTime)<10L; }

  boolean isMultiBad() { return mMultiBad; }

  // used by PocketTopo parser only
  DBlock( String f, String t, float d, float b, float c, float r, int e, int type, int shot_type )
  {
    // assert( f != null && t != null );
    mView = null; // view is set by the DBlockAdapter
    // mPos  = 0;
    mVisible = View.VISIBLE;
    mMultiSelected = false;
    mPaint = null;
    mId = 0;
    mTime = 0;
    mSurveyId = 0;
    // mName = "";
    mFrom = f;
    mTo   = t;
    mLength = d;
    mBearing = b;
    mClino = c;
    mRoll = r;
    mAcceleration = 0.0f;
    mMagnetic = 0.0f;
    mDip = 0.0f;
    mComment = "";
    mExtend = e;
    mFlag   = BLOCK_SURVEY;
    mType   = type;
    mShotType = shot_type;
    mWithPhoto = false;
    mMultiBad = false;
  }

  DBlock()
  {
    mView = null; // view is set by the DBlockAdapter
    // mPos  = 0;
    mVisible = View.VISIBLE;
    mMultiSelected = false;
    mPaint = null;
    mId = 0;
    mTime = 0;
    mSurveyId = 0;
    // mName = "";
    mFrom = "";
    mTo   = "";
    mLength = 0.0f;
    mBearing = 0.0f;
    mClino = 0.0f;
    mRoll = 0.0f;
    mAcceleration = 0.0f;
    mMagnetic = 0.0f;
    mDip = 0.0f;
    mComment = "";
    mExtend = EXTEND_RIGHT;
    mFlag   = BLOCK_SURVEY;
    mType   = BLOCK_BLANK;
    mShotType = 0;
    mWithPhoto = false;
    mMultiBad = false;
  }

  void setId( long shot_id, long survey_id )
  {
    mId       = shot_id;
    mSurveyId = survey_id;
  }

  void setName( String from, String to )
  {
    if ( from == null || to == null ) {
      TDLog.Error( "FIXME ERROR DBlock::setName() either from or to is null");
      return;
    }
    mFrom = from.trim();
    mTo   = to.trim();
    if ( mFrom.length() > 0 ) {
      if ( mTo.length() > 0 ) {
        mType = BLOCK_MAIN_LEG;
      } else {
        mType = BLOCK_SPLAY;
      }
    } else {
      if ( mTo.length() > 0 ) {
        mType = BLOCK_SPLAY;
      } else {
        mType = BLOCK_BLANK;
      }
    }
  }

  String Name() { return mFrom + "-" + mTo; }
  
  // x bearing [degrees]
  // public void setBearing( float x ) { // FIXME_EXTEND
  //   mBearing = x;
  //   if ( mBearing < 180 ) {  // east to the right, west to the left
  //     mExtend = EXTEND_RIGHT;
  //   } else {
  //     mExtend = EXTEND_LEFT;
  //   }
  // }

  // {
  //   if ( mFrom == null || mFrom.length() == 0 ) {
  //     if ( mTo == null || mTo.length() == 0 ) {
  //       return BLOCK_BLANK;
  //     }
  //     return BLOCK_SPLAY;
  //   }
  //   if ( mTo == null || mTo.length() == 0 ) {
  //     return BLOCK_SPLAY;
  //   }
  //   return BLOCK_MAIN_LEG;
  // }

  int color() { return colors[ mType ]; }

  // compute relative angle in radians
  float relativeAngle( DBlock b )
  {
    float cc, sc, cb, sb;
    cc = TDMath.cosd( mClino );
    sc = TDMath.sind( mClino );
    cb = TDMath.cosd( mBearing ); 
    sb = TDMath.sind( mBearing ); 
    Vector v1 = new Vector( cc * sb, cc * cb, sc );
    cc = TDMath.cosd( b.mClino );
    sc = TDMath.sind( b.mClino );
    cb = TDMath.cosd( b.mBearing ); 
    sb = TDMath.sind( b.mBearing ); 
    Vector v2 = new Vector( cc * sb, cc * cb, sc );
    return (v1.minus(v2)).Length(); // approximation: 2 * asin( dv/2 );
  }

  private float relativeDistance( DBlock b )
  {
    float cc, sc, cb, sb, len;
    len = mLength;
    cc = TDMath.cosd( mClino );
    sc = TDMath.sind( mClino );
    cb = TDMath.cosd( mBearing ); 
    sb = TDMath.sind( mBearing ); 
    Vector v1 = new Vector( len * cc * sb, len * cc * cb, len * sc );
    len = b.mLength;
    cc = TDMath.cosd( b.mClino );
    sc = TDMath.sind( b.mClino );
    cb = TDMath.cosd( b.mBearing ); 
    sb = TDMath.sind( b.mBearing ); 
    Vector v2 = new Vector( len * cc * sb, len * cc * cb, len * sc );
    return (v1.minus(v2)).Length();
  }

  boolean isRelativeDistance( DBlock b )
  {
    if ( b == null ) return false;
    float dist = relativeDistance( b );
    return ( dist/mLength + dist/b.mLength ) < TDSetting.mCloseDistance;
  }

  
  private void formatFlag( PrintWriter pw )
  {
    if ( isNoPlan() ) {
      pw.format("]_");
    } else if ( isNoProfile() ) {
      pw.format("]~");
    } else if ( isDuplicate() ) {
      pw.format( "]*" );
    } else if ( isSurface() ) {
      pw.format( "]-" );
    // } else if ( isCommented() ) {
    //   pw.format( "^" );
    // } else if ( isBackshot() ) {
    //   pw.format( "+" );
    } else {
      pw.format("]");
    }
  }

  private void formatComment( PrintWriter pw )
  {
    if ( mComment == null || mComment.length() == 0 ) return;
    pw.format(" %s", mComment);
  }

  String toString( boolean show_id )
  {
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;

    // TDLog.Log( TDLog.LOG_DATA,
    //   "DBlock::toString From " + mFrom + " To " + mTo + " data " + mLength + " " + mBearing + " " + mClino );
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if ( show_id ) pw.format("%d ", mId );
    pw.format(Locale.US, "<%s-%s> %.2f %.1f %.1f [%c",
      mFrom, mTo,
      mLength*ul, mBearing*ua, mClino*ua, mExtendTag[ mExtend + 1 ] );
    formatFlag( pw );
    if ( mWithPhoto ) { pw.format("#"); }
    formatComment( pw );
    // TDLog.Log( TDLog.LOG_DATA, sw.getBuffer().toString() );
    return sw.getBuffer().toString();
  }

  String toShortString( boolean show_id )
  {
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if ( show_id ) pw.format("%d ", mId );
    pw.format(Locale.US, "<%s-%s> %.2f %.1f %.1f", mFrom, mTo, mLength*ul, mBearing*ua, mClino*ua );
    return sw.getBuffer().toString();
  }

  String toNote()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("[%c", mExtendTag[ mExtend + 1 ] );
    formatFlag( pw );
    if ( mWithPhoto ) { pw.format("#"); }
    formatComment( pw );
    return sw.getBuffer().toString();
  }

  String dataString( String fmt )
  {
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    return String.format(Locale.US, fmt, mLength*ul, mBearing*ua, mClino*ua );
  }

  String distanceString()
  {
    return String.format(Locale.US, "%.2f", mLength * TDSetting.mUnitLength );
  }

  String bearingString()
  {
    return String.format(Locale.US, "%.1f", mBearing * TDSetting.mUnitAngle );
  }

  String clinoString()
  {
    return String.format(Locale.US, "%.1f", mClino * TDSetting.mUnitAngle );
  }

  // public String extraString( DistoXAccuracy accu )
  // {
  //   return String.format(Locale.US, "A %.1f  M %.1f  D %.1f", 
  //     accu.deltaAcc( mAcceleration ), 
  //     accu.deltaMag( mMagnetic ), 
  //     accu.deltaDip( mDip ) * TDSetting.mUnitAngle
  //   );
  // }

}

