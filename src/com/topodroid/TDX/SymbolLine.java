/* @file SymbolLine.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: line symbol
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.util.TDLog;
import com.topodroid.util.TDFile;
import com.topodroid.util.TDColor;
import com.topodroid.prefs.TDSetting;

import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;


import android.graphics.Path;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.graphics.ComposePathEffect;
import android.graphics.DashPathEffect;
import android.graphics.PathDashPathEffect;
// import android.graphics.PathDashPathEffect.Style;
// import android.graphics.Matrix;

public class SymbolLine extends Symbol
{
  String mName;       // local name
  Paint  mPaint;      // forward paint
  Paint  mRevPaint;   // reverse paint
  boolean mHasEffect; // whether the line paint has path-effect
  Path mPath;
  boolean mStyleStraight;
  boolean mClosed = false;
  int mClippable = 0;  // 0: no, 1: normal, 2: both
  int mStyleX;            // X times (one out of how many point to use)
  Path mPathDir = null;
  Path mPathRev = null;
  float mEffectLen = 0;
  float[] mDashX = null;

  @Override public String getName()  { return mName; }
  // @Override public String getThName( ) { return mThName; } // same as in Symbol.java

  @Override public Paint  getPaint() { return mPaint; }

  // /** @return the line color - default to black = use Symbol::getColor
  //  */
  // @Override public int getColor() { return (mPaint == null)? 0 : mPaint.getColor(); }

  @Override public Path   getSymbolPath()  { return mPath; }

  // @Override public boolean isOrientable() { return false; }
  // @Override public boolean isEnabled() { return mEnabled; }
  // @Override public void setEnabled( boolean enabled ) { mEnabled = enabled; }
  // @Override public void toggleEnabled() { mEnabled = ! mEnabled; }
  // @Override public boolean setAngle( float angle ) { }
  // @Override public int getAngle() { return 0; }

  // width = 1;
  // no effect
  SymbolLine( String name, String th_name, String group, String fname, int color, int level, int rt )
  {
    super( Symbol.TYPE_LINE, th_name, group, fname, rt );
    init( name, color, 1 );
    makeLinePath();
    mLevel = level;
    // TDLog.v("LINE-1 " + th_name + " group " + group );
  }

  // no effect
  SymbolLine( String name, String th_name, String group, String fname, int color, float width, int level, int rt )
  {
    super( Symbol.TYPE_LINE, th_name, group, fname, rt );
    init( name, color, width );
    makeLinePath();
    mLevel = level;
    // TDLog.v("LINE-2 " + th_name + " group " + group );
  }

  SymbolLine( String name, String th_name, String group, String fname, int color, float width, PathEffect effect_dir, PathEffect effect_rev, int level, int rt )
  {
    super( Symbol.TYPE_LINE, th_name, group, fname, rt );
    init( name, color, width );
    mPaint.setPathEffect( effect_dir );
    mRevPaint.setPathEffect( effect_rev );
    mHasEffect = true;
    makeLinePath();
    mLevel = level;
  }

  private void init( String name, int color, float width )
  {
    mName   = name;
    mPaint  = new Paint();
    mPaint.setDither(true);
    mPaint.setColor( color );
    mPaint.setStyle(Paint.Style.STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth( width * TDSetting.mLineThickness );
    mRevPaint = new Paint (mPaint );
    mHasEffect     = false;
    mStyleStraight = false;
    mClosed        = false;
    mClippable     = 0;
    mStyleX = 1;
  }

  SymbolLine( String filepath, String fname, String locale, String iso ) 
  {
    super( Symbol.TYPE_LINE, null, null, fname, Symbol.W2D_DETAIL_SHP );
    mStyleStraight = false;
    mClosed        = false;
    mClippable     = 0;
    mStyleX = 1;
    readFile( filepath, locale, iso );
    if ( mClosed ) mClippable = 0; // enforce closed lines are not clippable
    makeLinePath();
  }

  private void makeLinePath()
  {
    mPath = new Path();
    mPath.moveTo(-50, 0 );
    mPath.lineTo( 50, 0 );
  }


  private int k_val; // index in array vals[]

  private float nextFloat( String[] vals, int s, float unit ) throws NumberFormatException
  {
    ++k_val; while ( k_val < s && vals[k_val].length() == 0 ) ++k_val;
    if ( k_val < s ) {
      return Float.parseFloat( vals[k_val] ) * unit;
    }
    throw new NumberFormatException();
  }

  private int nextInt( String[] vals, int s ) throws NumberFormatException
  {
    ++k_val; while ( k_val < s && vals[k_val].length() == 0 ) ++k_val;
    if ( k_val < s ) {
      try {
        return Integer.parseInt( vals[k_val] );
      } catch( NumberFormatException e ) {
        TDLog.e("Non-integer value");
      }
    }
    throw new NumberFormatException();
  }

  /** create a symbol reading it from a file
   *  The file syntax is 
   *      symbol line
   *      name NAME
   *      th_name THERION_NAME
   *      group GROUP_NAME
   *      color 0xHHHHHH_COLOR 0xAA_ALPHA
   *      width WIDTH
   *      clippable no
   *      closed yes
   *      dash x1 y1 x2 y2 ...
   *      style straight | xN
   *      effect
   *        command: moveTo lineTo cubicTo addCircle
   *      endeffect
   *      endsymbol
   */
  private void readFile( String filename, String locale, String iso )
  {
    // TDLog.v( "SL load line file " + filename );
    float unit = TDSetting.mUnitLines * TDSetting.mLineThickness;
    String name    = null;
    String th_name = null;
    String group   = null;
    mHasEffect = false;
    int color  = 0;
    int alpha  = 0xcc;
    float width  = 1;
    mPathDir = null;
    mPathRev = null;
    DashPathEffect dash = null;
    PathDashPathEffect dir_effect = null;
    PathDashPathEffect rev_effect = null;
    float xmin=0, xmax=0;
    float ymin=0, ymax=0;
    String options = null;

    try {
      // FileReader fr = TDFile.getFileReader( filename );
      // TDLog.Log( TDLog.LOG_IO, "read symbol line file <" + filename + ">" );
      FileInputStream fr = TDFile.getFileInputStream( filename );
      BufferedReader br = new BufferedReader( new InputStreamReader( fr, iso ) );
      String line;
      boolean in_symbol = false; // 20230118 local var
      // TDLog.v( "read symbol line file <" + filename + "> in_symbol " + in_symbol );
      while ( (line = br.readLine()) != null ) {
        line = line.trim();
        String[] vals = line.split(" ");
        int s = vals.length;
        for (int k=0; k<s; ++k ) {
  	  if ( vals[k].startsWith( "#" ) ) break;
          if ( vals[k].length() == 0 ) continue;
          if ( ! in_symbol ) {
            // TDLog.v("SL not in symbol " + line );
  	    if ( vals[k].equals("symbol" ) ) {
  	      name    = null;
  	      th_name = null;
              group   = null;
  	      color   = TDColor.TRANSPARENT;
              in_symbol = true;
              // TDLog.v("SL " + filename + " in symbol" );
              break;
            }
          } else {
  	    if ( vals[k].equals("name") || vals[k].equals(locale) ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                name = (new String( vals[k].getBytes(iso) )).replace("_", " ");
  	      }
              // TDLog.v("SL " + filename + " name " + name );
  	    } else if ( vals[k].equals("th_name") ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                // 2023-01-31 th_name = deprefix_u( vals[k] );
                th_name = vals[k];
  	      }
  	    } else if ( vals[k].equals("group") ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
  	        group = vals[k];
  	      }
            } else if ( vals[k].equals("options") ) {
              StringBuilder sb = new StringBuilder();
              boolean space = false;
              for ( ++k; k < s; ++k ) {
                if ( vals[k].length() > 0 ) {
                  if ( space ) { sb.append(" "); } else { space = true; }
                  sb.append( vals[k] );
                }
              }
              options = sb.toString();
            } else if ( vals[k].equals("level") ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                try {
                  mLevel = ( Integer.parseInt( vals[k] ) );
                } catch( NumberFormatException e ) {
                  TDLog.e("Non-integer level");
                }
              }
            } else if ( vals[k].equals("roundtrip") ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                try {
                  mRoundTrip = ( Integer.parseInt( vals[k] ) );
                } catch( NumberFormatException e ) {
                  TDLog.e("Non-integer roundtrip");
                }
              }
  	    } else if ( vals[k].equals("closed") ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s && vals[k].equals("yes") ) {
                mClosed = true;
              }
            } else if ( vals[k].equals("clip") ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                if ( vals[k].equals("no") ) {
                  mClippable = 0;
                } else if ( vals[k].equals("yes") ) {
                  mClippable = 1;
                } else if ( vals[k].equals("both") ) {
                  mClippable = 2;
                }
              }
            } else if ( vals[k].equals("csurvey") ) {
              // syntax: csurvey <layer> <type> <category> <pen>
  	    } else if ( vals[k].equals("color") ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                try {
  	          color = Integer.decode( vals[k] );
                } catch ( NumberFormatException e ) {
                  TDLog.e("Non-integer color");
                }
              }
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                try {
  	          alpha = Integer.decode( vals[k] );
                } catch ( NumberFormatException e ) {
                  TDLog.e("Non-integer alpha");
                }
  	      }
  	    } else if ( vals[k].equals("width") ) {
              try {
                k_val = k;
                width = nextInt( vals, s ) * TDSetting.mLineThickness;
  	        // ++k; while ( k < s && vals[k].length() == 0 ) ++k;    
  	        // if ( k < s ) {
  	        //   width = Integer.parseInt( vals[k] ) * TDSetting.mLineThickness;
                // }
              } catch ( NumberFormatException e ) {
                TDLog.e( filename + " parse width error: " + line );
              }
  	    } else if ( vals[k].equals("dash") ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                int k1 = k;
                int n_dash = 0; // 20230118 local var number of dash+space
                while ( k1 < s ) {
                  while ( k1 < s && vals[k1].length() == 0 ) ++k1;
                  ++ n_dash;
                  ++k1;
                }
                n_dash = n_dash - (n_dash % 2);
                if ( n_dash > 0 ) {
                  try {
                    mDashX = new float[n_dash];
                    mDashX[0] = Float.parseFloat( vals[k] ) * unit;
                    k_val = k;
                    for (int n=1; n<n_dash; ++n ) {
                      mDashX[n] = nextFloat( vals, s, unit );
                      // ++k; while ( k < s && vals[k].length() == 0 ) ++k;
                      // mDashX[n] = Float.parseFloat( vals[k] ) * unit;
                    }  
                    dash = new DashPathEffect( mDashX, 0 );
                  } catch ( NumberFormatException e ) {
                   TDLog.e( filename + " parse dash error: " + line );
                  }
                }
              }
  	    } else if ( vals[k].equals("style") ) { // STYLE
              for ( ++ k; k < s; ++k ) {
  	        if ( vals[k].length() == 0 ) continue;
                if ( vals[k].equals("straight") ) {
                  mStyleStraight = true;
                } else if ( vals[k].startsWith("x") ) {
                  try {
                    mStyleX = Integer.parseInt( vals[k].substring(1) );
                    if ( mStyleX <= 0 ) mStyleX = ItemDrawer.POINT_MAX; // FIXME INT_MAX
                  } catch ( NumberFormatException e ) {
                    TDLog.e("Non-integer style X");
                  }
                }
              }
  	    } else if ( vals[k].equals("effect") ) {
              // TDLog.v("SL effect begins");
              mPathDir = new Path();
              mPathRev = new Path();
              // mPathDir.moveTo(0,0);
              // mPathRev.moveTo(0,0);
              boolean moved_to = false;
              while ( (line = br.readLine() ) != null ) {
                line = line.trim();
                vals = line.split(" ");
                s = vals.length;
                k = 0;
  	        while ( k < s && vals[k].length() == 0 ) ++k;
                if ( k < s ) {
                  if ( vals[k].equals("moveTo") ) {
                    try {
                      // if ( ! moved_to ) {
                        k_val = k;
                        float x = nextFloat( vals, s, unit );
                        float y = nextFloat( vals, s, unit );
                        mPathDir.moveTo( x, y );
                        mPathRev.moveTo( x, -y );
                        if ( ! moved_to ) {
                          xmin = xmax = x;
                          moved_to = true;
                        }
	                if ( y > ymax ) { ymax = y; } else if ( y < ymin ) { ymin = y; }
  	                // ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	                // if ( k < s ) {
  	                //   float x = Float.parseFloat( vals[k] ) * unit;
  	                //   ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	                //   if ( k < s ) {
  	                //      float y = Float.parseFloat( vals[k] ) * unit;
                        //      mPathDir.moveTo( x, y );
                        //      mPathRev.moveTo( x, -y );
                        //      xmin = xmax = x;
                        //      moved_to = true;
                        //   }
                        // }
                      // }
                    } catch ( NumberFormatException e ) {
                      TDLog.e( filename + " parse moveTo point error: " + line );
                    }
                  } else if ( vals[k].equals("lineTo") ) { 
                    try {
                      k_val = k;
                      float x = nextFloat( vals, s, unit );
                      float y = nextFloat( vals, s, unit );
                      mPathDir.lineTo( x, y );
                      mPathRev.lineTo( x, -y );
                      if ( x < xmin ) xmin = x; else if ( x > xmax ) xmax = x;
	              if ( y > ymax ) { ymax = y; } else if ( y < ymin ) { ymin = y; }

  	              // ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              // if ( k < s ) {
  	              //   float x = Float.parseFloat( vals[k] ) * unit;
  	              //   ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              //   if ( k < s ) {
  	              //     float y = Float.parseFloat( vals[k] ) * unit;
                      //     mPathDir.lineTo( x, y );
                      //     mPathRev.lineTo( x, -y );
                      //     if ( x < xmin ) xmin = x; else if ( x > xmax ) xmax = x;
                      //   }
                      // }
                    } catch ( NumberFormatException e ) {
                      TDLog.e( filename + " parse lineTo point error: " + line );
                    }
                  } else if ( vals[k].equals("cubicTo") ) { 
                    try {
                      k_val = k;
                      float x1 = nextFloat( vals, s, unit );
                      float y1 = nextFloat( vals, s, unit );
                      float x2 = nextFloat( vals, s, unit );
                      float y2 = nextFloat( vals, s, unit );
                      float x3 = nextFloat( vals, s, unit );
                      float y3 = nextFloat( vals, s, unit );
                      mPathDir.cubicTo( x1,  y1, x2,  y2, x3,  y3 );
                      mPathRev.cubicTo( x1, -y1, x2, -y2, x3, -y3 );
                      if ( x1 < xmin ) xmin = x1; else if ( x1 > xmax ) xmax = x1;
                      if ( x2 < xmin ) xmin = x2; else if ( x2 > xmax ) xmax = x2;
                      if ( x3 < xmin ) xmin = x3; else if ( x3 > xmax ) xmax = x3;
	              if ( y1 > ymax ) { ymax = y1; } else if ( y1 < ymin ) { ymin = y1; }
	              if ( y2 > ymax ) { ymax = y2; } else if ( y2 < ymin ) { ymin = y2; }
	              if ( y3 > ymax ) { ymax = y3; } else if ( y3 < ymin ) { ymin = y3; }

  	              // ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              // if ( k < s ) {
  	              //   float x1 = Float.parseFloat( vals[k] ) * unit;
  	              //   ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              //   if ( k < s ) {
  	              //     float y1 = Float.parseFloat( vals[k] ) * unit;
  	              //     ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              //     if ( k < s ) {
  	              //       float x2 = Float.parseFloat( vals[k] ) * unit;
  	              //       ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              //       if ( k < s ) {
  	              //         float y2 = Float.parseFloat( vals[k] ) * unit;
  	              //         ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              //         if ( k < s ) {
  	              //           float x3 = Float.parseFloat( vals[k] ) * unit;
  	              //           ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              //           if ( k < s ) {
  	              //             float y3 = Float.parseFloat( vals[k] ) * unit;
                      //             mPathDir.cubicTo( x1, y1, x2, y2, x3, y3 );
                      //             mPathRev.cubicTo( x1, -y1, x2, -y2, x3, -y3 );
                      //             if ( x1 < xmin ) xmin = x1; else if ( x1 > xmax ) xmax = x1;
                      //             if ( x2 < xmin ) xmin = x2; else if ( x2 > xmax ) xmax = x2;
                      //             if ( x3 < xmin ) xmin = x3; else if ( x3 > xmax ) xmax = x3;
                      //           }
                      //         }
                      //       }
                      //     }
                      //   }
                      // }
                    } catch ( NumberFormatException e ) {
                      TDLog.e( filename + " parse lineTo point error: " + line );
                    }
                  } else if ( vals[k].equals("addCircle") ) { 
                    try {
                      k_val = k;
                      float x = nextFloat( vals, s, unit );
                      float y = nextFloat( vals, s, unit );
                      float r = nextFloat( vals, s, unit );
                      mPathDir.addCircle( x,  y, r, Path.Direction.CCW );
                      mPathRev.addCircle( x, -y, r, Path.Direction.CCW );
                      if ( x-r < xmin ) xmin = x-r;
                      if ( x+r > xmax ) xmax = x+r;
	              if ( y+r > ymax ) { ymax = y+r; } else if ( y-r < ymin ) { ymin = y-r; }
                    } catch ( NumberFormatException e ) {
                      TDLog.e( filename + " parse lineTo point error: " + line );
                    }
                  } else if ( vals[k].equals("endeffect") ) {
                    // TDLog.v("SL effect ends");
                    // mPathDir.close();
                    // mPathRev.close();
                    mEffectLen = xmax-xmin;
                    dir_effect = new PathDashPathEffect( mPathDir, (xmax-xmin), 0, PathDashPathEffect.Style.MORPH );
                    rev_effect = new PathDashPathEffect( mPathRev, (xmax-xmin), 0, PathDashPathEffect.Style.MORPH );
                    break;
                  }
                }
              }
  	    } else if ( vals[k].equals("endsymbol") ) {
  	      if ( name != null && th_name != null ) { 
                // TDLog.v("SL " + filename + " end-symbol is ok");
                mName   = name;
                setThName( th_name );
                mGroup  = group;
                mDefaultOptions = options;
                mPaint  = new Paint();
                mPaint.setDither(true);
                mPaint.setColor( color );
                mPaint.setAlpha( alpha );
                mPaint.setStyle(Paint.Style.STROKE);
                mPaint.setStrokeJoin(Paint.Join.ROUND);
                mPaint.setStrokeCap(Paint.Cap.ROUND);
                mRevPaint = new Paint( mPaint );
                if ( dir_effect != null ) {
                  mHasEffect = true;
                  // mPaint.setStrokeWidth( 4 );
                  // mRevPaint.setStrokeWidth( 4 );
                  if ( dash != null ) {
                    mPaint.setPathEffect( new ComposePathEffect( dir_effect, dash ) );
                    mRevPaint.setPathEffect( new ComposePathEffect( rev_effect, dash ) );
                  } else {
                    mPaint.setPathEffect( dir_effect );
                    mRevPaint.setPathEffect( rev_effect );
                  }
                } else if ( dash != null ) {
                  mPaint.setPathEffect( dash );
                  mRevPaint.setPathEffect( dash );
                // } else {
                //   mPaint.setStrokeWidth( width * TDSetting.mLineThickness );
                //   mRevPaint.setStrokeWidth( width * TDSetting.mLineThickness );
                }
	        float dy = ymax - ymin + 1;
                mPaint.setStrokeWidth( dy * width * TDSetting.mLineThickness );
                mRevPaint.setStrokeWidth( dy * width * TDSetting.mLineThickness );
  	      }
              in_symbol = false;
            }
          }
        }
      }
    } catch ( FileNotFoundException e ) {
      // FIXME
    } catch( IOException e ) {
      // FIXME
    }
  }

}

