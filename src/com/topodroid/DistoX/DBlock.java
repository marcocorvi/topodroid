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

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDString;
import com.topodroid.math.TDVector;
import com.topodroid.prefs.TDSetting;


// import java.lang.Long;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Locale;

import android.view.View;
import android.widget.TextView;

import android.graphics.Paint; // custom paint

// import android.util.Log;

public class DBlock
{
  // public static final char[] mExtendTag = { '<', '|', '>', ' ', '-', '.', '?', '«', 'I', '»', ' ' };
  private static final char[] mExtendTag = { '<', '|', '>', ' ', '-', '.', '?', ' ', ' ', ' ', ' ' };
  public static final int EXTEND_LEFT   = -1;
  public static final int EXTEND_VERT   =  0;
  public static final int EXTEND_RIGHT  = 1;
  public static final int EXTEND_IGNORE = 2;
  public static final int EXTEND_HIDE   = 3;
  public static final int EXTEND_START  = 4;

  public static final float STRETCH_NONE = 0.0f;

  public static final int EXTEND_UNSET  = 5;
  // public static final int EXTEND_FLEFT  = 6; // LEFT = FLEFT - FVERT
  // public static final int EXTEND_FVERT  = 7;
  // public static final int EXTEND_FRIGHT = 8;
  // public static final int EXTEND_FIGNORE = 9; // overload of IGNORE for splays

  public static final int EXTEND_NONE   = EXTEND_VERT;

  View   mView;
  // private int    mPos;     // position in the list
  int    mVisible; // whether is visible in the list
  boolean mMultiSelected; // whether the block is in multiselect list
  private Paint mPaint;   // user-set block color

  long   mId;
  long   mTime;
  private long   mSurveyId;
  // private String mName;
  public String mFrom;    // N.B. mfrom and mTo must be not null
  public String mTo;
  public float mLength;   // meters
  public float mBearing;  // degrees
  public float mClino;    // degrees
  public float mRoll;     // degrees
  public float mAcceleration;
  public float mMagnetic;
  public float mDip;
  public float mDepth;     // depth at from station
  public String mComment;

  int  mExtend;
  long mFlag;     
  int  mBlockType;   
  int  mShotType;  // 0: DistoX, 1: manual, -1: DistoX backshot
  boolean mWithPhoto;

  private boolean mMultiBad; // whether it disagree with siblings
  private float mStretch;
  private String mAddress; // DistoX address - used only in exports
  // boolean mWasRecent = false; // REVISE_RECENT

  private static final int BLOCK_BLANK      = 0;
  public  static final int BLOCK_MAIN_LEG   = 1; // primary leg shot
  private static final int BLOCK_SEC_LEG    = 2; // additional shot of a centerline leg
  private static final int BLOCK_BLANK_LEG  = 3; // blank centerline leg-shot
  private static final int BLOCK_BACK_LEG   = 4; // 
  // splays must come last
  private static final int BLOCK_SPLAY      = 5;
  private static final int BLOCK_X_SPLAY    = 6; // FIXME_X_SPLAY cross splay
  private static final int BLOCK_H_SPLAY    = 7; // FIXME_H_SPLAY horizontal splay
  private static final int BLOCK_V_SPLAY    = 8; // FIXME_V_SPLAY vertical splay

  private static final long[] legOfBlockType = {
    LegType.NORMAL, // 0 BLANK
    LegType.NORMAL, // 0 LEG
    LegType.EXTRA,  // 1 SEC_LEG
    LegType.NORMAL, // 0 BLANK_LEG
    LegType.BACK,   // 3 BACK_LEG
    LegType.NORMAL, // 0 SPLAY
    LegType.XSPLAY, // 2
    LegType.HSPLAY, // 4
    LegType.VSPLAY, // 5
  };

  static final int[] blockOfSplayLegType = {
    BLOCK_SPLAY,
    -1, // BLOCK_SEC_LEG, // should never occor
    BLOCK_X_SPLAY,
    -1, //  BLOCK_BACK_LEG, // should never occor
    BLOCK_H_SPLAY,
    BLOCK_V_SPLAY,
  };

  private static final int[] colors = {
    TDColor.LIGHT_PINK,   // 0 blank
    TDColor.WHITE,        // 1 midline
    TDColor.LIGHT_GRAY,   // 3 sec. leg
    TDColor.VIOLET,       // 4 blank leg
    TDColor.LIGHT_YELLOW, // 6 back leg
    TDColor.LIGHT_BLUE,   // 2 splay
    TDColor.GREEN,        // 5 FIXME_X_SPLAY X splay
    TDColor.DARK_BLUE,    // 7 H_SPLAY
    TDColor.DEEP_BLUE,    // 8 V_SPLAY
    TDColor.GREEN
  };

  static final long FLAG_SURVEY     =  0; // flags
  static final long FLAG_SURFACE    =  1;
  static final long FLAG_DUPLICATE  =  2;
  static final long FLAG_COMMENTED  =  4; // unused // FIXME_COMMENTED
  static final long FLAG_NO_PLAN    =  8;
  static final long FLAG_NO_PROFILE = 16;
  // static final long FLAG_BACKSHOT   = 32;

  static final long FLAG_NO_EXTEND  = 256; // used only in search dialog

         boolean hasFlag( long flag )    { return (mFlag & flag) == flag; }
  public boolean isSurvey()    { return mFlag == FLAG_SURVEY; }
  public boolean isSurface()   { return (mFlag & FLAG_SURFACE)    == FLAG_SURFACE; }
  public boolean isDuplicate() { return (mFlag & FLAG_DUPLICATE)  == FLAG_DUPLICATE; }
  public boolean isCommented() { return (mFlag & FLAG_COMMENTED)  == FLAG_COMMENTED; } // FIXME_COMMENTED
  public boolean isNoPlan()    { return (mFlag & FLAG_NO_PLAN)    == FLAG_NO_PLAN; }
  public boolean isNoProfile() { return (mFlag & FLAG_NO_PROFILE) == FLAG_NO_PROFILE; }
  // boolean isBackshot()  { return (mFlag & FLAG_BACKSHOT)   == FLAG_BACKSHOT; }

  // static boolean isSurvey(int flag) { return flag == FLAG_SURVEY; }
  public static boolean isSurface(long flag)   { return (flag & FLAG_SURFACE)    == FLAG_SURFACE; }
  public static boolean isDuplicate(long flag) { return (flag & FLAG_DUPLICATE)  == FLAG_DUPLICATE; }
  public static boolean isCommented(long flag) { return (flag & FLAG_COMMENTED)  == FLAG_COMMENTED; } // FIXME_COMMENTED
  public static boolean isNoPlan(long flag)    { return (flag & FLAG_NO_PLAN)    == FLAG_NO_PLAN; }
  public static boolean isNoProfile(long flag) { return (flag & FLAG_NO_PROFILE) == FLAG_NO_PROFILE; }
  // static boolean isBackshot(int flag) { return (flag & FLAG_BACKSHOT) == FLAG_BACKSHOT; }

  // void resetFlag() { mFlag = FLAG_SURVEY; }
  void resetFlag( long flag ) { mFlag = flag; }
  void setFlag( long flag ) { mFlag |= flag; }
  // void clearFlag( long flag ) { mFlag &= ~flag; }
  long getFlag() { return mFlag; }
  public int  getReducedFlag() { return (int)(0x07 & mFlag); } // survey-surface-duplicate-commented part of the flag

  int getBlockType() { return mBlockType; }

  void setTypeBlankLeg( ) { if ( mBlockType == BLOCK_BLANK ) mBlockType = BLOCK_BLANK_LEG; }
  void setTypeSecLeg()  { mBlockType = BLOCK_SEC_LEG; }
  void setTypeBackLeg() { mBlockType = BLOCK_BACK_LEG; }
  // void setTypeMainLeg()  { mBlockType = BLOCK_MAIN_LEG; }
  void setTypeSplay()   { mBlockType = BLOCK_SPLAY; }
  
  void resetBlockType( int type ) { mBlockType = type; }

  public boolean isTypeBlank() { return mBlockType == BLOCK_BLANK || mBlockType == BLOCK_BLANK_LEG; }
  public static boolean isTypeBlank( int t ) { return t == BLOCK_BLANK || t == BLOCK_BLANK_LEG; }
  public boolean isBlank()      { return mBlockType == BLOCK_BLANK; }
  public boolean isLeg()        { return mBlockType == BLOCK_MAIN_LEG || mBlockType == BLOCK_BACK_LEG; }
  public boolean isMainLeg()    { return mBlockType == BLOCK_MAIN_LEG; }
  public boolean isBackLeg()    { return mBlockType == BLOCK_BACK_LEG; }
  public boolean isSecLeg()     { return mBlockType == BLOCK_SEC_LEG; }

  public static boolean isSplay( int t ) { return t >= BLOCK_SPLAY; }
  public boolean isSplay()      { return mBlockType >= BLOCK_SPLAY; }
  public boolean isOtherSplay() { return mBlockType >  BLOCK_SPLAY; }
  public boolean isPlainSplay() { return mBlockType == BLOCK_SPLAY; }
  public boolean isXSplay()     { return mBlockType == BLOCK_X_SPLAY; }
  public boolean isHSplay()     { return mBlockType == BLOCK_H_SPLAY; }
  public boolean isVSplay()     { return mBlockType == BLOCK_V_SPLAY; }

  long getLegType() { return legOfBlockType[ mBlockType ]; }
  // {
  //   if ( mBlockType == BLOCK_SEC_LEG )  return LegType.EXTRA;
  //   if ( mBlockType == BLOCK_X_SPLAY )  return LegType.XSPLAY;
  //   if ( mBlockType == BLOCK_H_SPLAY )  return LegType.HSPLAY;
  //   if ( mBlockType == BLOCK_V_SPLAY )  return LegType.VSPLAY;
  //   if ( mBlockType == BLOCK_BACK_LEG ) return LegType.BACK;
  //   // if ( mBlockType == BLOCK_BLANK    ) return LegType.INVALID;
  //   return LegType.NORMAL;
  // }

  void setBlockType( int leg_type )
  {
     switch ( leg_type ) {
       case LegType.EXTRA:  mBlockType = BLOCK_SEC_LEG;  break;
       case LegType.XSPLAY: mBlockType = BLOCK_X_SPLAY;  break;
       case LegType.BACK:   mBlockType = BLOCK_BACK_LEG; break;
       case LegType.HSPLAY: mBlockType = BLOCK_H_SPLAY;  break;
       case LegType.VSPLAY: mBlockType = BLOCK_V_SPLAY;  break;
       default: /* nothing */
     }
  }

  void setAddress( String address ) { mAddress = address; } // used by DataHelper
  String getAddress() { return mAddress; } // used by the data exported


  // static int getIntExtend( int ext ) { return ( ext < EXTEND_UNSET )? ext : ext - EXTEND_FVERT; }
  public static int getIntExtend( int ext ) { return ext; }
  public static float getReducedExtend( int ext, float stretch ) 
  {
    // if ( ext >= EXTEND_UNSET ) { ext -= EXTEND_FVERT; }
    return ( ext < 2 )? ext + stretch : 0;
  }

  // int getIntExtend() { return ( mExtend < EXTEND_UNSET )? mExtend : mExtend - EXTEND_FVERT; }
  public int getIntExtend() { return mExtend; }
  public float getReducedExtend() { return ( mExtend < 2 )? mExtend + mStretch : 0.0f; }
  public int   getReducedIntExtend() { return ( mExtend < 2 )? mExtend : 0; }

  // int getFullExtend() { return mExtend; } // 20191002 same as getIntExtend()
  void setExtend( int ext, float stretch ) { mExtend = ext; mStretch = stretch; }
  public boolean hasStretch( float stretch ) { return Math.abs( mStretch - stretch ) < 0.01f; }
  public boolean hasStretch( ) { return Math.abs( mStretch ) < 0.01f; }

  // void setStretch( float stretch ) { mStretch = stretch; } // ununsed
  public float getStretch() { return mStretch; }
  public float getStretchedExtend() { return mExtend + mStretch; }

  // called only by ShotWindow
  boolean flipExtendAndStretch()
  {
    mStretch = - mStretch;
    switch ( mExtend ) {
      case EXTEND_LEFT:   mExtend = EXTEND_RIGHT;  return true;
      case EXTEND_RIGHT:  mExtend = EXTEND_LEFT;   return true;
      // case EXTEND_FLEFT:  mExtend = EXTEND_FRIGHT; return true;
      // case EXTEND_FRIGHT: mExtend = EXTEND_FLEFT;  return true;
    }
    return ( Math.abs( mStretch ) > 0.01f );
  }

  // a block is recent if
  //   - its id comes after the given id
  //   - its time is no more than 10 seconds before the given time
  boolean isRecent( )
  {
    if ( ! TDSetting.mShotRecent ) return false;
    if ( TDSetting.isConnectionModeContinuous() ) return isTimeRecent( System.currentTimeMillis()/1000 );
    return mId >= TDInstance.secondLastShotId;
  }

  private boolean isTimeRecent( long time ) { return mId >= TDInstance.secondLastShotId && (time-mTime) < TDSetting.mRecentTimeout; }

  boolean isMultiBad() { return mMultiBad; }
  public void setMultiBad( boolean multibad ) { mMultiBad = multibad; }

  Paint getPaint() { return mPaint; }

  int getPaintColor() { return (mPaint==null)? 0 : mPaint.getColor(); }

  void clearPaint() { 
    // Log.v("DistoX", "Block " + mId + " clear paint");
    mPaint = null;
  }

  void setPaintColor( int color )
  {
    // Log.v("DistoX", "Block " + mId + " set paint color " + color );
    if ( color == 0 ) { 
      mPaint = null;
      // if ( mView != null ) {
      //   TextView tvFrom = (TextView)mView.findViewById( R.id.from );
      //   tvFrom.setBackgroundColor( 0 );
      // }
    } else {
      if ( mPaint == null ) { 
        mPaint = BrushManager.makePaint( color );
      } else {
        mPaint.setColor( color );
      }
      // if ( mView != null ) {
      //   TextView tvFrom = (TextView)mView.findViewById( R.id.from );
      //   tvFrom.setBackgroundColor( color & 0x99ffffff );
      // }
    }

  }

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
    // mName = TDString.EMPTY;
    mFrom = f;
    mTo   = t;
    mLength = d;
    mBearing = b;
    mClino = c;
    mDepth = 0.0f;
    mRoll = r;
    mAcceleration = 0.0f;
    mMagnetic = 0.0f;
    mDip = 0.0f;
    mComment = TDString.EMPTY;
    mExtend = e;
    mFlag   = FLAG_SURVEY;
    mBlockType = type;
    mShotType = shot_type; // distox, distox-backshot, or manual
    mWithPhoto = false;
    mMultiBad = false;
    mStretch  = 0.0f;
    mAddress  = null;
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
    // mName = TDString.EMPTY;
    mFrom = TDString.EMPTY;
    mTo   = TDString.EMPTY;
    mLength = 0.0f;
    mBearing = 0.0f;
    mClino = 0.0f;
    mDepth = 0.0f;
    mRoll = 0.0f;
    mAcceleration = 0.0f;
    mMagnetic = 0.0f;
    mDip = 0.0f;
    mComment = TDString.EMPTY;
    mExtend = EXTEND_RIGHT;
    mFlag   = FLAG_SURVEY;
    mBlockType   = BLOCK_BLANK;
    mShotType = 0;  // distox
    mWithPhoto = false;
    mMultiBad = false;
    mStretch  = 0.0f;
    mAddress  = null;
  }

  public boolean makeClino( float tdepth )
  {
    float v = mDepth - tdepth;
    mClino = TDMath.asind( v / mLength ); // nan if |v| > mLength
    return ( Math.abs(v) <= mLength );
  }

  void setShotType( int type ) { mShotType = type; }
  int  getShotType( ) { return mShotType; }

  boolean isDistoX() { return mShotType <= 0; }
  boolean isDistoXBacksight() { return TDSetting.mDistoXBackshot && mShotType == -1; }
  boolean isForesight() { return mShotType == 0; }
  boolean isBacksight() { return mShotType == -1; }
  boolean isManual() { return mShotType > 0; }

  void setId( long shot_id, long survey_id )
  {
    mId       = shot_id;
    mSurveyId = survey_id;
  }

  void setBlockName( String from, String to ) { setBlockName( from, to, false ); }

  void setBlockName( String from, String to, boolean is_backleg )
  {
    if ( from == null || to == null ) {
      TDLog.Error( "FIXME ERROR DBlock::setName() either from or to is null");
      return;
    }
    mFrom = from.trim();
    mTo   = to.trim();
    if ( mFrom.length() > 0 ) {
      if ( mTo.length() > 0 ) {
        mBlockType = is_backleg ? BLOCK_BACK_LEG : BLOCK_MAIN_LEG;
      } else if ( ! isSplay() ) {
        mBlockType = BLOCK_SPLAY;
      }
    } else {
      if ( mTo.length() == 0 /* && mBlockType != BLOCK_EXTRA */ ) {
        mBlockType = BLOCK_BLANK;
      } else if ( ! isSplay() ) {
        mBlockType = BLOCK_SPLAY;
      }
    }
  }

  public String Name() { return mFrom + "-" + mTo; }
  
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

  
  int getColorByType() { 
    // Log.v("DistoX", "Block " + mId + " color() block type " + mBlockType );
    return colors[ mBlockType ];
  }

  // compute relative angle in radians
  public float relativeAngle( DBlock b )
  {
    float cc, sc, cb, sb;
    cc = TDMath.cosd( mClino );
    sc = TDMath.sind( mClino );
    cb = TDMath.cosd( mBearing ); 
    sb = TDMath.sind( mBearing ); 
    TDVector v1 = new TDVector( cc * sb, cc * cb, sc );
    cc = TDMath.cosd( b.mClino );
    sc = TDMath.sind( b.mClino );
    cb = TDMath.cosd( b.mBearing ); 
    sb = TDMath.sind( b.mBearing ); 
    TDVector v2 = new TDVector( cc * sb, cc * cb, sc );
    return (v1.minus(v2)).Length(); // approximation: 2 * asin( dv/2 );
  }

  private boolean checkRelativeDistance( DBlock b )
  {
    float cc, sc, cb, sb;
    float alen = mLength;
    cc = TDMath.cosd( mClino );
    sc = TDMath.sind( mClino );
    cb = TDMath.cosd( mBearing ); 
    sb = TDMath.sind( mBearing ); 
    TDVector v1 = new TDVector( alen * cc * sb, alen * cc * cb, alen * sc );
    float blen = b.mLength;
    cc = TDMath.cosd( b.mClino );
    sc = TDMath.sind( b.mClino );
    cb = TDMath.cosd( b.mBearing ); 
    sb = TDMath.sind( b.mBearing ); 
    TDVector v2 = new TDVector( blen * cc * sb, blen * cc * cb, blen * sc );
    float d = (v1.minus(v2)).Length();
    return ( d/alen + d/blen < TDSetting.mCloseDistance );
  }

  private boolean checkRelativeDistanceDiving( DBlock b )
  {
    float cb, sb;
    float alen = mLength;
    cb = TDMath.cosd( mBearing ); 
    sb = TDMath.sind( mBearing ); 
    TDVector v1 = new TDVector( alen * sb, alen * cb, mDepth );
    float blen = b.mLength;
    cb = TDMath.cosd( b.mBearing ); 
    sb = TDMath.sind( b.mBearing ); 
    TDVector v2 = new TDVector( blen * sb, blen * cb, b.mDepth );
    float d = (v1.minus(v2)).Length();
    return ( d/alen + d/blen < TDSetting.mCloseDistance );
  }

  public boolean isRelativeDistance( DBlock b )
  {
    if ( b == null ) return false;
    return ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING )? checkRelativeDistanceDiving( b ) : checkRelativeDistance( b );
  }
  
  private void formatFlagPhoto( PrintWriter pw )
  {
    if ( isNoPlan() ) {
      pw.format("]\u00A7");       // section symbol
    } else if ( isNoProfile() ) {
      pw.format("]_");            // low_line
    } else if ( isDuplicate() ) {
      pw.format( "]\u00B2" );     // superscript 2
    } else if ( isSurface() ) {
      pw.format( "]\u00F7" );     // division sign
    // } else if ( isCommented() ) { // commented = gray background
    //   pw.format( "^" );
    // } else if ( isBackshot() ) {
    //   pw.format( "+" );
    } else {
      pw.format("]");
    }
    if ( mWithPhoto ) { pw.format("#"); }
  }

  private void formatComment( PrintWriter pw )
  {
    if ( mComment == null || mComment.length() == 0 ) return;
    pw.format(" %s", mComment);
  }

  String toStringNormal( boolean show_id )
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
      mLength*ul, mBearing*ua, mClino*ua, mExtendTag[ mExtend + 1 ] ); // FIXME mStretch
    formatFlagPhoto( pw );
    formatComment( pw );
    // TDLog.Log( TDLog.LOG_DATA, sw.getBuffer().toString() );
    return sw.getBuffer().toString();
  }

  String toStringDiving( boolean show_id )
  {
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;

    // TDLog.Log( TDLog.LOG_DATA,
    //   "DBlock::toString From " + mFrom + " To " + mTo + " data " + mLength + " " + mBearing + " " + mClino );
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if ( show_id ) pw.format("%d ", mId );
    pw.format(Locale.US, "<%s-%s> %.2f %.1f %.2f [%c",
      mFrom, mTo,
      mLength*ul, mBearing*ua, mDepth*ul, mExtendTag[ mExtend + 1 ] ); // FIXME mStretch
    formatFlagPhoto( pw );
    formatComment( pw );
    // TDLog.Log( TDLog.LOG_DATA, sw.getBuffer().toString() );
    return sw.getBuffer().toString();
  }

  String toShortStringNormal( boolean show_id )
  {
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if ( show_id ) pw.format("%d ", mId );
    pw.format(Locale.US, "<%s-%s> %.2f %.1f %.1f", mFrom, mTo, mLength*ul, mBearing*ua, mClino*ua );
    return sw.getBuffer().toString();
  }

  String toShortStringDiving( boolean show_id )
  {
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if ( show_id ) pw.format("%d ", mId );
    pw.format(Locale.US, "<%s-%s> %.2f %.1f %.2f", mFrom, mTo, mLength*ul, mBearing*ua, mDepth*ul );
    return sw.getBuffer().toString();
  }

  String toNote()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("[%c", mExtendTag[ mExtend + 1 ] ); // FIXME mStretch
    formatFlagPhoto( pw );
    formatComment( pw );
    return sw.getBuffer().toString();
  }

  String dataStringNormal( String fmt )
  {
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    return String.format(Locale.US, fmt, mLength*ul, mBearing*ua, mClino*ua );
  }

  String dataStringDiving( String fmt )
  {
    float ul = TDSetting.mUnitLength;
    float ua = TDSetting.mUnitAngle;
    return String.format(Locale.US, fmt, mLength*ul, mBearing*ua, mDepth*ul );
  }

  String distanceString()
  {
    return String.format(Locale.US, "%.2f", mLength * TDSetting.mUnitLength );
  }

  String bearingString()
  {
    if ( mShotType == -1 ) {
    return String.format(Locale.US, "%.1f*", mBearing * TDSetting.mUnitAngle );
    }
    return String.format(Locale.US, "%.1f", mBearing * TDSetting.mUnitAngle );
  }

  String clinoString()
  {
    if ( mShotType == -1 ) {
      return String.format(Locale.US, "%.1f*", mClino * TDSetting.mUnitAngle );
    }
    return String.format(Locale.US, "%.1f", mClino * TDSetting.mUnitAngle );
  }

  String depthString()
  {
    return String.format(Locale.US, "%.1f", mDepth * TDSetting.mUnitLength );
  }

  // public String extraString( SurveyAccuracy accu )
  // {
  //   return String.format(Locale.US, "A %.1f  M %.1f  D %.1f", 
  //     accu.deltaAcc( mAcceleration ), 
  //     accu.deltaMag( mMagnetic ), 
  //     accu.deltaDip( mDip ) * TDSetting.mUnitAngle
  //   );
  // }

}

