/* @file LoxBitmap.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief loch bitmap
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.c3in;

import com.topodroid.utils.TDLog;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class LoxBitmap
{
  int sid; // surface
  int type;
  int size;
  double[] calib;
  double[] calib_inv;
  double det; // calib det
  byte[] data;
  int data_offset;

  int width;
  int height;
  Bitmap image = null;

  public LoxBitmap( int _sid, int tp, int sz, double[] c, byte[] d, int d_off )
  {
    sid = _sid;
    type = tp;
    size = sz;
    data = d;
    data_offset = d_off;
    width  = 0;
    height = 0;
    calib     = new double[6];
    calib_inv = new double[6];
    for ( int k=0; k<6; ++k ) calib[k]= c[k];

    Data2RGB();
    // TDLog.v("Bitmap " + String.format("calib %.2f %.6f %.6f   %.2f %.6f %.6f", c[0], c[2], c[3], c[1], c[4], c[5] ) );
    det = calib[2] * calib[5] - calib[3] * calib[4];
    calib_inv[2] =  calib[5] / det;
    calib_inv[3] = -calib[3] / det;
    calib_inv[4] = -calib[4] / det;
    calib_inv[5] =  calib[2] / det;
    calib_inv[0] = - calib_inv[2] * calib[0] + calib_inv[3] * calib[1];
    calib_inv[1] =   calib_inv[4] * calib[0] - calib_inv[5] * calib[1];
  }
  // Therion transformation
  // e = calib[0] + calib[2] * i + calib[4] * j
  // n = calib[1] + calib[3] * i + calib[5] * j

  int Surface() { return sid; }
  int Type() { return type; }
  double Calib( int k ) { return calib[k]; }
  int DataSize() { return size; }
  byte[] Data()  { return data; }
  int DataOffset() { return data_offset; }

  void recycle()
  {
    if ( image != null ) image.recycle();
    image = null;
    width = height = 0;
  }

  private double ENtoI( double e, double n )
  {
    return calib_inv[0] + calib_inv[2] * e + calib_inv[4] * n;
  }

  private double ENtoJ( double e, double n ) // J image coord (from bottom upward)
  {
    return calib_inv[1] + calib_inv[3] * e + calib_inv[5] * n;
  }

  public Bitmap getBitmap( double e1, double n1, double e2, double n2 )
  { 
    // TDLog.v("Bitmap create E " + e1 + " " + e2 + " N " + n1 + " " + n2 );
    int d1 = width;
    int d2 = height;
    Bitmap ret = Bitmap.createBitmap( d1, d2, Bitmap.Config.ARGB_8888 );
    if ( ret == null ) {
      TDLog.Error("Bitmap Failed create bitmap " + d1 + "x" + d2 );
      return null;
    }
    double dx = (e2-e1)/(d1-1);
    double dy = (n2-n1)/(d2-1);

    for ( int y = 0; y < d2; ++y ) {
      double n = n1 + y * dy;
      for ( int x = 0; x < d1; ++x ) {
        double e = e1 + x * dx;
        int i = (int)ENtoI( e, n );
        if ( i < 0 || i >= width ) {
          ret.setPixel( x, y, 0 );
        } else {
          int j = (int)ENtoJ( e, n );
          if ( j < 0 || j >= height ) {
            ret.setPixel( x, y, 0 );
          } else {
            // TDLog.v("Bitmap pixel " + x + " " + y + " from " + i + " " + j );
            ret.setPixel( x, d2-1-y, image.getPixel( i, height - 1 - j ) );
          }
        }
      }
    }
    return ret;
  }

  private boolean isPNG( byte[] data, int off )
  {
    return data[0+off] == (byte)(0x89) 
        && data[1+off] == (byte)(0x50)
        && data[2+off] == (byte)(0x4e)
        && data[3+off] == (byte)(0x47);
  }
  
  private boolean isJPG( byte[] data, int off ) 
  {
    return data[4+off] == (byte)(0x00) 
        && data[5+off] == (byte)(0x10)
        && data[6+off] == (byte)(0x4a)
        && data[7+off] == (byte)(0x46);
  }

  private void Data2RGB()
  {
    image = null;
    int len = data.length - data_offset;
    if ( isJPG( data, data_offset ) ) {
      // TDLog.v("Bitmap JPG image type " + type + " length " + len + " size " + size );
      image = BitmapFactory.decodeByteArray( data, data_offset, size );
    } else if ( isPNG( data, data_offset ) ) {
      // TDLog.v("Bitmap PNG image type " + type + " length " + len + " size " + size );
      image = BitmapFactory.decodeByteArray( data, data_offset, size );
    } else {
      TDLog.v("Bitmap Unexpected image type " + type );
    }
    if ( image != null ) {
      width  = image.getWidth();
      height = image.getHeight();
      // TDLog.v("Bitmap image " + width + "x" + height );
    }
  }      

}

