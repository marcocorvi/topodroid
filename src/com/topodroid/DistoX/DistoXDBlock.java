/* @file DistoXDBlock.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX survey data
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20120517 length and angle units
 * 20120711 added dataString
 * 20120726 TopoDroid log
 * 20130104 EXTEND enum
 * 20130108 extend :ignore"
 * 20131022 blank-leg color (violet)
 * 20131116 added fields: accel., magn., dip
 * 20140414 added isRecent()
 */
package com.topodroid.DistoX;

// import java.lang.Long;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.util.Locale;

import android.view.View;

// import android.util.Log;

public class DistoXDBlock
{
  private static final float grad2rad = TopoDroidUtil.GRAD2RAD;

  public static final char[] mExtendTag = { '<', '|', '>', 'o', '-', '.' };
  public static final int EXTEND_LEFT = -1;
  public static final int EXTEND_VERT =  0;
  public static final int EXTEND_RIGHT = 1;
  public static final int EXTEND_IGNORE = 2;
  public static final int EXTEND_HIDE   = 3;
  public static final int EXTEND_START  = 4;
  public static final int EXTEND_NONE   = EXTEND_VERT;

  View   mView;
  int    mVisible; // whether is visible in the list

  long   mId;
  long   mSurveyId;
  // private String mName;
  String mFrom;
  String mTo;
  float mLength;   // meters
  float mBearing;  // degrees
  float mClino;    // degrees
  float mRoll;     // degrees
  float mAcceleration;
  float mMagnetic;
  float mDip;
  String mComment;
  long   mExtend;
  long   mFlag;     
  int    mType;    // shot type
  boolean mWithPhoto;

  public static final int BLOCK_BLANK      = 0;
  public static final int BLOCK_MAIN_LEG   = 1; // primary leg shot
  public static final int BLOCK_SPLAY      = 2;
  public static final int BLOCK_SEC_LEG    = 3; // additional shot of a centerline leg
  public static final int BLOCK_BLANK_LEG  = 4; // blank centerline leg-shot

  // block colors:                blank,      centerline, splay,      leg,        blank-leg, ...
  //                              dark-red    white       blue        grey        violet
  private static int[] colors = { 0xffffcccc, 0xffffffff, 0xffccccff, 0xffcccccc, 0xffff33ff, 0xffccffcc };

  public static final int BLOCK_SURVEY     = 0; // flags
  public static final int BLOCK_SURFACE    = 1;
  public static final int BLOCK_DUPLICATE  = 2;

  public boolean isSurvey() { return mFlag == BLOCK_SURVEY; }
  public boolean isSurface() { return mFlag == BLOCK_SURFACE; }
  public boolean isDuplicate() { return mFlag == BLOCK_DUPLICATE; }

  int mShotType;  // 0: DistoX, 1: manual

  // used by PocketTopo parser only
  public DistoXDBlock( String f, String t, float d, float b, float c, float r, int e, int type, int shot_type )
  {
    mView = null; // view is set by the DistoXDBlockAdapter
    mVisible = View.VISIBLE;
    mId = 0;
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
  }

  public DistoXDBlock()
  {
    mView = null; // view is set by the DistoXDBlockAdapter
    mVisible = View.VISIBLE;
    mId = 0;
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
  }

  public void setId( long shot_id, long survey_id )
  {
    mId       = shot_id;
    mSurveyId = survey_id;
  }

  public void setName( String from, String to ) 
  {
    if ( from == null || to == null ) return; // FIXME ERROR
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

  public String Name() { return mFrom + "-" + mTo; }
  
  public void setBearing( float x ) {
    mBearing = x;
    if ( mBearing < 3.14 ) {  // east to the right, west to the left
      mExtend = EXTEND_RIGHT;
    } else {
      mExtend = EXTEND_LEFT;
    }
  }

  void setTypeBlankLeg( )
  {
    if ( mType == BLOCK_BLANK ) mType = BLOCK_BLANK_LEG;
  }

  boolean isTypeBlank() 
  {
    return mType == BLOCK_BLANK || mType == BLOCK_BLANK_LEG;
  }
  
  static boolean isTypeBlank( int t ) 
  {
    return t == BLOCK_BLANK || t == BLOCK_BLANK_LEG;
  }


  public int type() { return mType; }
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

  public int color()
  {
    // Log.v( "DistoX", "block " + mId + " type " + mType );
    return colors[ mType ];
  }

  public float relativeDistance( DistoXDBlock b )
  {
    if ( b == null ) return 10000.0f; // a large distance
    float cc = (float)Math.cos(mClino * grad2rad);
    float sc = (float)Math.sin(mClino * grad2rad);
    float cb = (float)Math.cos(mBearing * grad2rad); 
    float sb = (float)Math.sin(mBearing * grad2rad); 
    Vector v1 = new Vector( mLength * cc * sb, mLength * cc * cb, mLength * sc );
    cc = (float)Math.cos(b.mClino * grad2rad);
    sc = (float)Math.sin(b.mClino * grad2rad);
    cb = (float)Math.cos(b.mBearing * grad2rad); 
    sb = (float)Math.sin(b.mBearing * grad2rad); 
    Vector v2 = new Vector( b.mLength * cc * sb, b.mLength * cc * cb, b.mLength * sc );
    float dist = (v1.minus(v2)).Length();
    return dist/mLength + dist/b.mLength; 
  }

  public String toString( boolean show_id )
  {
    float ul = TopoDroidSetting.mUnitLength;
    float ua = TopoDroidSetting.mUnitAngle;

    TopoDroidLog.Log( TopoDroidLog.LOG_DATA, "DBlock::toString From " + mFrom + " To " + mTo + " data " + mLength + " " + mBearing + " " + mClino );
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    if ( show_id ) pw.format("%d ", mId );
    pw.format(Locale.ENGLISH, "<%s-%s> %.2f %.1f %.1f [%c]",
      mFrom, mTo,
      mLength*ul, mBearing*ua, mClino*ua, mExtendTag[ (int)(mExtend) + 1 ] );
    if ( mFlag == BLOCK_DUPLICATE ) {
      pw.format( "*" );
    } else if ( mFlag == BLOCK_SURFACE ) {
      pw.format( "-" );
    }
    if ( mComment != null && mComment.length() > 0 ) {
      pw.format(" N");
    } 
    if ( mWithPhoto ) { pw.format(" #"); }
    TopoDroidLog.Log( TopoDroidLog.LOG_DATA, sw.getBuffer().toString() );
    return sw.getBuffer().toString();
  }

  String toNote()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format("[%c]", mExtendTag[ (int)(mExtend) + 1 ] );
    if ( mFlag == BLOCK_DUPLICATE ) {
      pw.format( "*" );
    } else if ( mFlag == BLOCK_SURFACE ) {
      pw.format( "-" );
    }
    if ( mComment != null && mComment.length() > 0 ) {
      pw.format(" N");
    } 
    if ( mWithPhoto ) { pw.format(" #"); }
    return sw.getBuffer().toString();
  }

  public String dataString()
  {
    float ul = TopoDroidSetting.mUnitLength;
    float ua = TopoDroidSetting.mUnitAngle;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.ENGLISH, "L %.2f  N %.1f  I %.1f", mLength*ul, mBearing*ua, mClino*ua );
    return sw.getBuffer().toString();
  }

  public String distanceString()
  {
    float ul = TopoDroidSetting.mUnitLength;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.ENGLISH, "%.2f", mLength*ul );
    return sw.getBuffer().toString();
  }

  public String bearingString()
  {
    float ua = TopoDroidSetting.mUnitAngle;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.ENGLISH, "%.1f", mBearing*ua );
    return sw.getBuffer().toString();
  }

  public String clinoString()
  {
    float ua = TopoDroidSetting.mUnitAngle;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.ENGLISH, "%.1f", mClino*ua );
    return sw.getBuffer().toString();
  }

  public String extraString()
  {
    float ua = TopoDroidSetting.mUnitAngle;
    StringWriter sw = new StringWriter();
    PrintWriter pw  = new PrintWriter(sw);
    pw.format(Locale.ENGLISH, "A %.2f  M %.1f  D %.1f", mAcceleration, mMagnetic, mDip*ua );
    return sw.getBuffer().toString();
  }

  boolean isAcceptable( )
  {
    if ( mAcceleration == 0.0f || mMagnetic == 0.0f ) return true;
    return TopoDroidApp.isBlockAcceptable( mAcceleration, mMagnetic, mDip );
    // if ( ! ret ) {
    //   Log.v( TopoDroidApp.TAG, "unacceptable " + mAcceleration + " " + mMagnetic + " " + mDip );
    // }
  }

  boolean isRecent( long id ) { return mId >= id; }

}

