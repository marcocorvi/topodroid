/** @file SymbolArea.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: area symbol
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20121201 created
 * 20121211 locale
 * 20131119 area color member-field
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Paint;
import android.graphics.Path;

// import android.util.Log;

class SymbolArea extends Symbol
                 implements SymbolInterface
{
  String mName;
  int mColor;
  Paint mPaint;
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

  /** 
   * color 0xaarrggbb
   */
  SymbolArea( String name, String th_name, int color )
  {
    super( th_name );
    mName = name;
    mColor = color;
    mPaint = new Paint();
    mPaint.setDither(true);
    mPaint.setColor( mColor );
    mPaint.setAlpha( 0x66 );
    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth( TopoDroidSetting.mLineThickness );
    makePath();
  }

  void makePath()
  {
    mPath = new Path();
    mPath.addCircle( 0, 0, 10, Path.Direction.CCW );
  }

  SymbolArea( String filepath, String locale, String iso )
  {
    readFile( filepath, locale, iso );
    makePath();
  }

  /** create a symbol reading it from a file
   *  The file syntax is 
   *      symbol area
   *      name NAME
   *      th_name THERION_NAME
   *      color 0xHHHHHH_COLOR 0xAA_ALPHA
   *      endsymbol
   */
  void readFile( String filename, String locale, String iso )
  {
    // Log.v(  TopoDroidApp.TAG, "SymbolPoint::readFile " + filename + " " + locale + " " + iso );
  
    String name    = null;
    String th_name = null;
    int color      = 0;
    int alpha      = 0x66;

    try {
      // FileReader fr = new FileReader( filename );
      FileInputStream fr = new FileInputStream( filename );
      BufferedReader br = new BufferedReader( new InputStreamReader( fr, iso ) );
      String line;
      line = br.readLine();
      while ( line != null ) {
        line.trim();
        String[] vals = line.split(" ");
        int s = vals.length;
        for (int k=0; k<s; ++k ) {
  	  if ( vals[k].startsWith( "#" ) ) break;
          if ( vals[k].length() == 0 ) continue;
  	  if ( vals[k].equals("symbol") ) {
  	    name    = null;
  	    th_name = null;
  	    mColor   = 0x00000000;
  	  } else if ( vals[k].equals("name") || vals[k].equals(locale) ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
              // Log.v(  TopoDroidApp.TAG, "found name " + vals[k] );
              name = (new String( vals[k].getBytes(iso) )).replace("_", " ");
  	    }
  	  } else if ( vals[k].equals("th_name") ) {
  	    ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	    if ( k < s ) {
  	      th_name = vals[k];
  	    }
          } else if ( vals[k].equals("csurvey") ) {
            // csurvey <layer> <category> <pen_type> <brush_type>
            try {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxLayer = Integer.parseInt( vals[k] );
              }
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxCategory = Integer.parseInt( vals[k] );
              }
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxPen = Integer.parseInt( vals[k] );
              }
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                mCsxBrush = Integer.parseInt( vals[k] );
              }
            } catch ( NumberFormatException e ) {
              TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "parse error: " + line );
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
  	  } else if ( vals[k].equals("endsymbol") ) {
  	    if ( name == null ) {
  	    } else if ( th_name == null ) {
  	    } else {
              mName   = name;
              mThName = th_name;
              mPaint  = new Paint();
              mPaint.setDither(true);
              mColor = (alpha << 24) | color;
              mPaint.setColor( color );
              mPaint.setAlpha( alpha );
              // mPaint.setStyle(Paint.Style.STROKE);
              mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
              mPaint.setStrokeJoin(Paint.Join.ROUND);
              mPaint.setStrokeCap(Paint.Cap.ROUND);
              mPaint.setStrokeWidth( TopoDroidSetting.mLineThickness );
  	    }
          }
        }
        line = br.readLine();
      }
    } catch ( FileNotFoundException e ) {
      // FIXME
    } catch( IOException e ) {
      // FIXME
    }
  }
}
