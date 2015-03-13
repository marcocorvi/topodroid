/** @file SymbolLine.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: line symbol
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121201 created
 * 20121210 added mHasEffect flag
 * 20121211 locale
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileReader;
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
import android.graphics.PathDashPathEffect.Style;
import android.graphics.Matrix;

// import android.util.Log;

public class SymbolLine extends Symbol
                        implements SymbolInterface
{
  String mName;       // local name
  Paint  mPaint;      // forward paint
  Paint  mRevPaint;   // reverse paint
  boolean mHasEffect;
  Path mPath;

  public String getName()  { return mName; }
  public String getThName( ) { return mThName; }
  public Paint  getPaint() { return mPaint; }
  public Path   getPath()  { return mPath; }
  public boolean isOrientable() { return false; }
  public boolean isEnabled() { return mEnabled; }
  public void setEnabled( boolean enabled ) { mEnabled = enabled; }
  public void toggleEnabled() { mEnabled = ! mEnabled; }
  public void rotate( float angle ) { }

  // width = 1;
  // no effect
  SymbolLine( String name, String th_name, int color )
  {
    super( th_name );
    init( name, color, 1 );
    makePath();
  }

  // no effect
  SymbolLine( String name, String th_name, int color, float width )
  {
    super( th_name );
    init( name, color, width );
    makePath();
  }

  SymbolLine( String name, String th_name, int color, float width, PathEffect effect_dir, PathEffect effect_rev )
  {
    super( th_name );
    init( name, color, width );
    mPaint.setPathEffect( effect_dir );
    mRevPaint.setPathEffect( effect_rev );
    makePath();
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
    mPaint.setStrokeWidth( width * TopoDroidSetting.mLineThickness );
    mRevPaint = new Paint (mPaint );
    mHasEffect = false;
  }

  SymbolLine( String filepath, String locale, String iso ) 
  {
    readFile( filepath, locale, iso );
    makePath();
  }

  void makePath()
  {
    mPath = new Path();
    mPath.moveTo(-50, 0 );
    mPath.lineTo( 50, 0 );
  }

  /** create a symbol reading it from a file
   *  The file syntax is 
   *      symbol line
   *      name NAME
   *      th_name THERION_NAME
   *      color 0xHHHHHH_COLOR 0xAA_ALPHA
   *      width WIDTH
   *      effect
   *      endeffect
   *      endsymbol
   */
  void readFile( String filename, String locale, String iso )
  {
    // Log.v(  TopoDroidApp.TAG, "load line file " + filename );
    float unit = TopoDroidSetting.mUnit * TopoDroidSetting.mLineThickness;
    String name    = null;
    String th_name = null;
    mHasEffect = false;
    int color  = 0;
    int alpha  = 0xcc;
    float width  = 1;
    Path path_dir = null;
    Path path_rev = null;
    DashPathEffect dash = null;
    PathDashPathEffect effect = null;
    PathDashPathEffect rev_effect = null;
    boolean moved_to = false;
    float xmin=0, xmax=0;

    try {
      // FileReader fr = new FileReader( filename );
      FileInputStream fr = new FileInputStream( filename );
      BufferedReader br = new BufferedReader( new InputStreamReader( fr, iso ) );
      String line;
      while ( (line = br.readLine()) != null ) {
        line.trim();
        String[] vals = line.split(" ");
        int s = vals.length;
        for (int k=0; k<s; ++k ) {
  	  if ( vals[k].startsWith( "#" ) ) break;
          if ( vals[k].length() == 0 ) continue;
  	  if ( vals[k].equals("symbol") ) {
  	    name    = null;
  	    th_name = null;
  	    color   = 0x00000000;
  	  } else if ( vals[k].equals("name") || vals[k].equals(locale) ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
              name = (new String( vals[k].getBytes(iso) )).replace("_", " ");
  	    }
  	  } else if ( vals[k].equals("th_name") ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
  	      th_name = vals[k];
  	    }
          } else if ( vals[k].equals("csurvey") ) {
            // syntax: 
            //    csurvey <layer> <type> <category> <pen>
            try {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxLayer = Integer.parseInt( vals[k] );
              }
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxType = Integer.parseInt( vals[k] );
              }
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxCategory = Integer.parseInt( vals[k] );
              }
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxPen = Integer.parseInt( vals[k] );
              }
            } catch ( NumberFormatException e ) {
              TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "parse csurvey error: " + line );
            }
  	  } else if ( vals[k].equals("color") ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
  	      color = Integer.decode( vals[k] );
            }
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
  	      alpha = Integer.decode( vals[k] );
  	    }
  	  } else if ( vals[k].equals("width") ) {
            try {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;    
  	      if ( k < s ) {
  	        width = Integer.parseInt( vals[k] ) * TopoDroidSetting.mLineThickness;
              }
            } catch ( NumberFormatException e ) {
              TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "parse width error: " + line );
            }
  	  } else if ( vals[k].equals("dash") ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
              int k1 = k;
              int ndash = 0;
              while ( k1 < s ) {
                while ( k1 < s && vals[k1].length() == 0 ) ++k1;
                ++ ndash;
                ++k1;
              }
              ndash = ndash - (ndash % 2);
              if ( ndash > 0 ) {
                try {
                  float[] x = new float[ndash];
  	          x[0] = Float.parseFloat( vals[k] ) * unit;
                  for (int n=1; n<ndash; ++n ) {
                    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	            x[n] = Float.parseFloat( vals[k] ) * unit;
                  }  
                  dash = new DashPathEffect( x, 0 );
                } catch ( NumberFormatException e ) {
                 TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "parse dash error: " + line );
                }
              }
            }
  	  } else if ( vals[k].equals("effect") ) {
            path_dir = new Path();
            // path_dir.moveTo(0,0);
            moved_to = false;
            while ( (line = br.readLine() ) != null ) {
              line.trim();
              vals = line.split(" ");
              s = vals.length;
              k = 0;
  	      while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                if ( vals[k].equals("moveTo") ) {
                  try {
                    if ( ! moved_to ) {
  	              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              if ( k < s ) {
  	                float x = Float.parseFloat( vals[k] ) * unit;
  	                ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	                if ( k < s ) {
  	                   float y = Float.parseFloat( vals[k] ) * unit;
                           path_dir.moveTo( x, y );
                           xmin = xmax = x;
                           moved_to = true;
                        }
                      }
                    }
                  } catch ( NumberFormatException e ) {
                    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "parse moveTo point error: " + line );
                  }
                } else if ( vals[k].equals("lineTo") ) { 
                  try {
  	            ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	            if ( k < s ) {
  	              float x = Float.parseFloat( vals[k] ) * unit;
  	              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	              if ( k < s ) {
  	                float y = Float.parseFloat( vals[k] ) * unit;
                        path_dir.lineTo( x, y );
                        if ( x < xmin ) xmin = x; else if ( x > xmax ) xmax = x;
                      }
                    }
                  } catch ( NumberFormatException e ) {
                    TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "parse lineTo point error: " + line );
                  }
                } else if ( vals[k].equals("endeffect") ) {
                  path_dir.close();
                  path_rev = new Path( path_dir );
                  effect = new PathDashPathEffect( path_dir, (xmax-xmin), 0, PathDashPathEffect.Style.MORPH );
                  Matrix m = new Matrix();
                  m.postRotate( 180 );
                  path_rev.transform( m );
                  rev_effect = new PathDashPathEffect( path_rev, (xmax-xmin), 0, PathDashPathEffect.Style.MORPH );
                  break;
                }
              }
            }
  	  } else if ( vals[k].equals("endsymbol") ) {
  	    if ( name == null ) {
  	    } else if ( th_name == null ) {
  	    } else {
              mName   = name;
              mThName = th_name;
              mPaint  = new Paint();
              mPaint.setDither(true);
              mPaint.setColor( color );
              mPaint.setAlpha( alpha );
              mPaint.setStyle(Paint.Style.STROKE);
              mPaint.setStrokeJoin(Paint.Join.ROUND);
              mPaint.setStrokeCap(Paint.Cap.ROUND);
              mRevPaint = new Paint( mPaint );
              if ( effect != null ) {
                mHasEffect = true;
                // mPaint.setStrokeWidth( 4 );
                // mRevPaint.setStrokeWidth( 4 );
                if ( dash != null ) {
                  mPaint.setPathEffect( new ComposePathEffect( effect, dash ) );
                  mRevPaint.setPathEffect( new ComposePathEffect( rev_effect, dash ) );
                } else {
                  mPaint.setPathEffect( effect );
                  mRevPaint.setPathEffect( rev_effect );
                }
              } else if ( dash != null ) {
                mPaint.setPathEffect( dash );
                mRevPaint.setPathEffect( dash );
              } else {
                mPaint.setStrokeWidth( width * TopoDroidSetting.mLineThickness );
                mRevPaint.setStrokeWidth( width * TopoDroidSetting.mLineThickness );
              }
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

