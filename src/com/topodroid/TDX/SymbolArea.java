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
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDColor;
import com.topodroid.prefs.TDSetting;

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

public class SymbolArea extends Symbol
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

  @Override public Paint  getPaint() { return mPaint; }

  // /** @return the area color - default to black = use Symbol::getColor
  //  */
  // @Override public int getColor() { return (mPaint == null)? 0 : mPaint.getColor(); }

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
    // TDLog.e( "ERROR area symbol set orientation " + angle + " not supported" );
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
  SymbolArea( String name, String th_name, String group, String fname, int color, Bitmap bitmap, TileMode x_mode, TileMode y_mode,
              boolean close_horizontal, int level, int rt )
  {
    super( Symbol.TYPE_AREA, th_name, group, fname, rt );
    mName   = name;
    mColor  = color;
    mLevel  = level;

    mBitmap = bitmap;
    mXMode  = x_mode;
    mYMode  = y_mode;
    mCloseHorizontal = close_horizontal;
    mOrientable  = false;
    // FIXME AREA_ORIENT mOrientation = 0;
    mPaint = new Paint();
    mPaint.setDither(true);
    mPaint.setColor( mColor );
    mPaint.setAlpha( 0x66 );
    mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    mPaint.setStrokeJoin(Paint.Join.ROUND);
    mPaint.setStrokeCap(Paint.Cap.ROUND);
    mPaint.setStrokeWidth( TDSetting.mLineThickness );
    makeShader( mBitmap, mXMode, mYMode, true );
    makeAreaPath();
  }

  // FIXME AREA_ORIENT
  void rotateGradArea( double a ) 
  {
    if ( mOrientable ) {
      // TDLog.e( "ERROR area symbol rotate by " + a + " not implemented" );
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

  private void makeAreaPath()
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
    super( Symbol.TYPE_AREA, null, null, fname, Symbol.W2D_DETAIL_SHP );
    mOrientable  = false;
    // FIXME AREA_ORIENT
    mOrientation = 0;

    readFile( filepath, locale, iso );
    makeAreaPath();
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

  /** ???
   * @param bitmap   tile bitmap
   * @param x_mode   tiling mode in X direction 
   * @param y_mode   tiling mode in Y direction 
   * @param subimage ???
   */
  private void makeShader( Bitmap bitmap, TileMode x_mode, TileMode y_mode, boolean subimage ) 
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
      mShader = new BitmapShader( mShaderBitmap, x_mode, y_mode );
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
    // TDLog.v(  "Area read " + filename + " " + locale + " " + iso );
  
    String name    = null;
    String th_name = null;
    String group   = null;
    int color      = 0;
    int alpha      = 0x66;
    int alpha_bg   = 0x33;
    mBitmap    = null;
    int width  = 0;
    int height = 0;
    mXMode = TileMode.REPEAT;
    mYMode = TileMode.REPEAT;
    int[] pxl = null;
    String options = null;

    try {
      // FileReader fr = TDFile.getFileReader( filename );
      // TDLog.Log( TDLog.LOG_IO, "read symbol area file <" + filename + ">" );
      FileInputStream fr = TDFile.getFileInputStream( filename );
      BufferedReader br = new BufferedReader( new InputStreamReader( fr, iso ) );
      boolean in_symbol = false; // 20230118 local var
      String line;
      line = br.readLine();
      while ( line != null ) {
        line = line.trim();
        String[] vals = line.split(" ");
        int s = vals.length;
        for (int k=0; k<s; ++k ) {
  	  if ( vals[k].startsWith( "#" ) ) break;
          if ( vals[k].length() == 0 ) continue;
          if ( ! in_symbol ) {
  	    if ( vals[k].equals("symbol") ) {
  	      name    = null;
  	      th_name = null;
  	      mColor  = TDColor.TRANSPARENT;
              in_symbol = true;
            }
  	  } else {
            if ( vals[k].equals("name") || vals[k].equals(locale) ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                // TDLog.v( "found name " + vals[k] );
                name = (new String( vals[k].getBytes(iso) )).replace("_", " "); // should .trim(); for tab etc.
  	      }
	      // TDLog.v( "area name <" + name + "> locale " + locale );
  	    } else if ( vals[k].equals("th_name") ) {  // therion name must not have spaces
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                // should .trim(); for tab etc. ? no: require syntax without tabs etc.
  	        // 2023-01-31 th_name = deprefix_u( vals[k] );
                th_name = vals[k];
  	      }
  	    } else if ( vals[k].equals("group") ) {  
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
  	        group = vals[k]; // should .trim(); for tab etc. ? no: require syntax without tabs etc.
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
            } else if ( vals[k].equals("csurvey") ) {
              // csurvey <layer> <category> <pen_type> <brush_type>
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
  	    } else if ( vals[k].equals("color") ) {
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                try {
  	          color = Integer.decode( vals[k] );
                } catch( NumberFormatException e ) {
                  TDLog.e("Non-integer color");
                }
              }
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                try {
  	          alpha = Integer.decode( vals[k] );
                } catch( NumberFormatException e ) {
                  TDLog.e("Non-integer alpha");
                }
  	      }
  	      ++k; while ( k < s && vals[k].length() == 0 ) ++k;
  	      if ( k < s ) {
                try {
  	          alpha_bg = Integer.decode( vals[k] );
                } catch( NumberFormatException e ) {
                  TDLog.e("Non-integer alpha-bg");
                }
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
                      // TDLog.v( "bitmap " + width + " " + height );
                      for ( int j=0; j<height; ++j ) {
                        line = br.readLine().trim();
                        // TDLog.v( "bitmap line <" + line + ">" );
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
                  TDLog.e( filename + " parse bitmap error: " + line );
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
                setThName( th_name );
                mGroup  = group;
                mDefaultOptions = options;
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
              in_symbol = false;
            }
          }
        }
        line = br.readLine();
      }
    } catch ( FileNotFoundException e ) {
      TDLog.e( "File not found: " + e.getMessage() );
    } catch( IOException e ) {
      TDLog.e( "I/O error: " + e.getMessage() );
    }
    makeShader( mBitmap, mXMode, mYMode, true );
  }
}
