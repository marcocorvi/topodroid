/* @file SymbolArea.java
 *
 * @author marco corvi
 * @date dec 2012
 *
 * @brief TopoDroid drawing: area symbol
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import java.io.File;
// import java.io.FileReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Shader.TileMode;

// import android.util.Log;

class SymbolArea extends Symbol
{
  String mName;
  int mColor;
  boolean mCloseHorizontal;
  boolean mOrientable; // PRIVATE
  // FIXME AREA_ORIENT
  double mOrientation;

  private Paint mPaint;
  private Path mPath;
  Bitmap       mBitmap;
  Bitmap       mShaderBitmap = null;
  BitmapShader mShader; // paint bitmap shader
  TileMode mXMode;
  TileMode mYMode;

  @Override public String getName()  { return mName; }
  @Override public String getThName( ) { return mThName; }
  @Override public Paint  getPaint() { return mPaint; }
  @Override public Path   getPath()  { return mPath; }
  @Override public boolean isOrientable() { return mOrientable; }

  // @Override public boolean isEnabled() { return mEnabled; }
  // @Override public void setEnabled( boolean enabled ) { mEnabled = enabled; }
  // @Override public void toggleEnabled() { mEnabled = ! mEnabled; }

  // FIXME AREA_ORIENT
  @Override public boolean setAngle( float angle ) 
  {
    if ( mBitmap == null ) return false;
    mOrientation = angle;
    // TDLog.Error( "ERROR area symbol set orientation " + angle + " not supported" );
    android.graphics.Matrix m = new android.graphics.Matrix();
    m.preRotate( (float)mOrientation );
    mShader.setLocalMatrix( m );
    return true;
  }

  // FIXME AREA_ORIENT
  @Override public int getAngle() { return (int)mOrientation; }

  /** 
   * color 0xaarrggbb
   * level canvas level
   */
  SymbolArea( String name, String th_name, String fname, int color, Bitmap bitmap, TileMode xmode, TileMode ymode,
              boolean close_horizontal, int level )
  {
    super( th_name, fname );
    mName   = name;
    mColor  = color;
    mLevel  = level;

    mBitmap = bitmap;
    mXMode  = xmode;
    mYMode  = ymode;
    mCloseHorizontal = close_horizontal;
    mOrientable  = false;
    // FIXME AREA_ORIET mOrientation = 0;
    mPaint = new Paint();
    mPaint.setDither(true);
    mPaint.setColor( mColor );
    mPaint.setAlpha( 0x66 );
    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth( TDSetting.mLineThickness );
    makeShader( mBitmap, mXMode, mYMode, true );
    makePath();
  }

  // FIXME AREA_ORIENT
  void rotateGradArea( double a ) 
  {
    if ( mOrientable ) {
      // TDLog.Error( "ERROR area symbol rotate by " + a + " not implementd" );
      // mOrientation += a;
      // while ( mOrientation >= 360 ) mOrientation -= 360;
      // while ( mOrientation < 0 ) mOrientation += 360;
      mOrientation = TDMath.in360( mOrientation + a );
      if ( mShader != null ) {
        android.graphics.Matrix m = new android.graphics.Matrix();
        m.preRotate( (float)mOrientation );
        mShader.setLocalMatrix( m );
      }
    }
  }

  private void makePath()
  {
    mPath = new Path();
    if ( mCloseHorizontal ) {
      mPath.moveTo( -10, -5 );
      mPath.cubicTo( -10, 0, -5, 5,  0,  5 );
      mPath.cubicTo(   5, 5,  10, 0, 10, -5 );
      mPath.close();
    } else {
      mPath.addCircle( 0, 0, 10, Path.Direction.CCW );
    }
  }

  SymbolArea( String filepath, String fname, String locale, String iso )
  {
    super( null, fname );
    mOrientable  = false;
    // FIXME AREA_ORIENT
    mOrientation = 0;

    readFile( filepath, locale, iso );
    makePath();
  }

  private Bitmap makeBitmap( int[] pxl, int w1, int h1 )
  {
    if ( w1 > 0 && h1 > 0 ) {
      int w2 = w1 * 2;
      int h2 = h1 * 2;
      int[] pxl2 = new int[ w2 * h2 ];
      for ( int j=0; j<h1; ++j ) {
        for ( int i=0; i < w1; ++i ) {
          pxl2[j*w2+i] = pxl2[j*w2+i+w1] = pxl2[(j+h1)*w2+i] = pxl2[(j+h1)*w2+i+w1] = pxl[j*w1+i];
        }
      }
      Bitmap bitmap = Bitmap.createBitmap( w2, h2, Bitmap.Config.ARGB_8888 );
      bitmap.setPixels( pxl2, 0, w2, 0,     0,      w2, h2 );
      // bitmap.setPixels( pxl, 0, width, width, 0,      width, height );
      // bitmap.setPixels( pxl, 0, width, 0,     height, width, height );
      // bitmap.setPixels( pxl, 0, width, width, height, width, height );
      return bitmap;
    }
    return null;
  }

  private void makeShader( Bitmap bitmap, TileMode xmode, TileMode ymode, boolean subimage ) 
  {
    if ( bitmap == null ) return;
    int width  = bitmap.getWidth();
    int height = bitmap.getHeight();
    if ( width > 0 && height > 0 ) {
      if ( subimage ) {
        int w1 = width / 2;
        int h1 = height / 2;
        mShaderBitmap = Bitmap.createBitmap( bitmap, w1/2, h1/2, w1, h1 );
      }
      mShader = new BitmapShader( mShaderBitmap, xmode, ymode );
      mPaint.setShader( mShader );
    }
  }

  /** create a symbol reading it from a file
   *  The file syntax is 
   *      symbol area
   *      name NAME
   *      th_name THERION_NAME
   *      color 0xHHHHHH_COLOR 0xAA_ALPHA
   *      endsymbol
   */
  private void readFile( String filename, String locale, String iso )
  {
    // Log.v(  TopoDroidApp.TAG, "SymbolPoint::readFile " + filename + " " + locale + " " + iso );
  
    String name    = null;
    String th_name = null;
    int color      = 0;
    int alpha      = 0x66;
    int alpha_bg   = 0x33;
    mBitmap    = null;
    int width  = 0;
    int height = 0;
    mXMode = TileMode.REPEAT;
    mYMode = TileMode.REPEAT;
    int[] pxl = null;

    try {
      // FileReader fr = new FileReader( filename );
      // TDLog.Log( TDLog.LOG_IO, "read symbol area file <" + filename + ">" );
      FileInputStream fr = new FileInputStream( filename );
      BufferedReader br = new BufferedReader( new InputStreamReader( fr, iso ) );
      boolean insymbol = false;
      String line;
      line = br.readLine();
      while ( line != null ) {
        line = line.trim();
        String[] vals = line.split(" ");
        int s = vals.length;
        for (int k=0; k<s; ++k ) {
  	  if ( vals[k].startsWith( "#" ) ) break;
          if ( vals[k].length() == 0 ) continue;
          if ( ! insymbol ) {
  	    if ( vals[k].equals("symbol") ) {
  	      name    = null;
  	      th_name = null;
  	      mColor  = TDColor.TRANSPARENT;
              insymbol = true;
            }
  	  } else {
            if ( vals[k].equals("name") || vals[k].equals(locale) ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                // Log.v(  TopoDroidApp.TAG, "found name " + vals[k] );
                name = (new String( vals[k].getBytes(iso) )).replace("_", " "); // should .trim(); for tab etc.
  	      }
	      // Log.v("DistoX", "area name <" + name + "> locale " + locale );
  	    } else if ( vals[k].equals("th_name") ) {  // therion name must not have spaces
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
  	        th_name = vals[k]; // should .trim(); for tab etc. ? no: require syntax without tabs etc.
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
                TDLog.Error( filename + " parse error: " + line );
              }
            } else if ( vals[k].equals("level") ) {
              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                try {
                  mLevel = ( Integer.parseInt( vals[k] ) );
                } catch( NumberFormatException e ) { }
              }
  	    } else if ( vals[k].equals("color") ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                try {
  	          color = Integer.decode( vals[k] );
                } catch( NumberFormatException e ) { }
              }
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                try {
  	          alpha = Integer.decode( vals[k] );
                } catch( NumberFormatException e ) { }
  	      }
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                try {
  	          alpha_bg = Integer.decode( vals[k] );
                } catch( NumberFormatException e ) { }
  	      }
  	    } else if ( vals[k].equals("bitmap") ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
              if ( k < s ) {
                try {
                  width = Integer.parseInt( vals[k] );
  	          ++k; while ( k < s && vals[k].length() == 0 ) ++k;
                  if ( k < s ) {
                    height = Integer.parseInt( vals[k] );
                    mXMode = TileMode.REPEAT;
                    mYMode = TileMode.REPEAT;
  	            ++k; while ( k < s && vals[k].length() == 0 ) ++k;
                    if ( k < s ) {
                      if ( vals[k].equals("M") ) { mXMode = TileMode.MIRROR; }
  	              ++k; while ( k < s && vals[k].length() == 0 ) ++k;
                      if ( k < s ) {
                        if ( vals[k].equals("M") ) { mYMode = TileMode.MIRROR; }
                      }
                    }
                    if ( width > 0 && height > 0 ) {
                      pxl = new int[ width * height];
                      int col = (color & 0x00ffffff) | ( alpha_bg << 24 ) ;
                      for ( int j=0; j<height; ++j ) {
                        for ( int i=0; i<width; ++i ) pxl[j*width+i] = col;
                      }
                      col = (color & 0x00ffffff) | ( alpha << 24 );
                      // Log.v("DistoX", "bitmap " + width + " " + height );
                      for ( int j=0; j<height; ++j ) {
                        line = br.readLine().trim();
                        // Log.v("DistoX", "bitmap line <" + line + ">" );
                        if ( line.startsWith("endbitmap") ) {
                          mBitmap = makeBitmap( pxl, width, height );
                          pxl = null;
                          break;
                        }
                        for ( int i=0; i<width && i <line.length(); ++i ) if ( line.charAt(i) == '1' ) pxl[j*width+i] = col;
                      }
                    }
                  }   
                } catch ( NumberFormatException e ) {
                  TDLog.Error( filename + " parse bitmap error: " + line );
                }
              }
  	    } else if ( vals[k].equals("endbitmap") ) {
              mBitmap = makeBitmap( pxl, width, height );
              pxl = null;
  	    } else if ( vals[k].equals("orientable") ) {
              mOrientable = true;
  	    } else if ( vals[k].equals("close-horizontal") ) {
              mCloseHorizontal = true;

  	    } else if ( vals[k].equals("endsymbol") ) {
  	      if ( name != null && th_name != null ) { // at this point if both are not null, they have both positive length
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
                mPaint.setStrokeWidth( TDSetting.mLineThickness );
  	      }
              insymbol = false;
            }
          }
        }
        line = br.readLine();
      }
    } catch ( FileNotFoundException e ) {
      TDLog.Error( "File not found: " + e.getMessage() );
    } catch( IOException e ) {
      TDLog.Error( "I/O error: " + e.getMessage() );
    }
    makeShader( mBitmap, mXMode, mYMode, true );
  }
}
