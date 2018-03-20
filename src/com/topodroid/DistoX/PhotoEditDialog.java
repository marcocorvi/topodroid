/* @file PhotoEditDialog.java
 *
 * @author marco corvi
 * @date july 2012
 *
 * @brief TopoDroid photo edit dialog 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.IOException;

// import android.app.Dialog;
import android.os.Bundle;

// import android.content.Intent;
import android.content.Context;
// import android.net.Uri;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
// import android.widget.Toast;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;
// import android.view.View.OnKeyListener;
// import android.view.KeyEvent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.media.ExifInterface;

// import android.util.Log;

public class PhotoEditDialog extends MyDialog
                             implements View.OnClickListener
{
  private TopoDroidApp  mApp;
  private PhotoActivity mParent;
  private PhotoInfo mPhoto;
  private String mFilename;

  private EditText mETcomment;  // photo comment
  private ImageView mIVimage;   // photo image
  private Button   mButtonOK;
  private Button   mButtonDelete;
  // private Button   mButtonCancel;
  private float mAzimuth = 0;
  private float mClino   = 0;
  private int mOrientation = 0;
  private String mDate = "";
  private boolean mAtShot;

  /**
   * @param context   context
   */
  PhotoEditDialog( Context context, PhotoActivity parent, TopoDroidApp app, PhotoInfo photo, String filename )
  {
    super( context, R.string.PhotoEditDialog );
    mParent = parent;
    mApp    = app;
    mPhoto  = photo;
    mFilename = filename;
    mAtShot   = (mPhoto.shotid >= 0);
    // TDLog.Log(TDLog.LOG_PHOTO, "PhotoEditDialog " + mFilename);

    mAzimuth = mClino = 0;
    try {
      ExifInterface exif = new ExifInterface( mFilename );
      // mAzimuth = exif.getAttribute( "GPSImgDirection" );
      mOrientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, 0 );
      // Log.v("DistoX", "Photo edit orientation " + mOrientation );
      String b = exif.getAttribute( ExifInterface.TAG_GPS_LONGITUDE );
      String c = exif.getAttribute( ExifInterface.TAG_GPS_LATITUDE );
      mDate = exif.getAttribute( ExifInterface.TAG_DATETIME );
      if ( mDate == null ) mDate = "";
      if ( b != null && c != null ) {
        int k = b.indexOf('/');
	if ( k >= 0 ) {
          try { mAzimuth = Integer.parseInt( b.substring(0,k) ) / 100.0f; } catch ( NumberFormatException e ) { }
	}
        k = c.indexOf('/');
	if ( k >= 0 ) {
          try { mClino = Integer.parseInt( c.substring(0,k) ) / 100.0f; } catch ( NumberFormatException e ) { }
	}
        // Log.v("DistoX", "Long <" + bearing + "> Lat <" + clino + ">" );
      }
    } catch ( IOException e ) {
      TDLog.Error("failed exif interface " + mFilename );
    }
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_PHOTO, "onCreate" );
    initLayout( R.layout.photo_edit_dialog, R.string.title_photo_comment );

    mIVimage      = (ImageView) findViewById( R.id.photo_image );
    mETcomment    = (EditText) findViewById( R.id.photo_comment );
    mButtonOK     = (Button) findViewById( R.id.photo_ok );
    mButtonDelete = (Button) findViewById( R.id.photo_delete );
    // mButtonCancel = (Button) findViewById( R.id.photo_cancel );

    ((TextView) findViewById( R.id.photo_azimuth )).setText(
       String.format( mContext.getResources().getString( R.string.photo_azimuth_clino ), mAzimuth, mClino ) );
    ((TextView) findViewById( R.id.photo_date )).setText( mDate );

    if ( mPhoto.mComment != null ) {
      mETcomment.setText( mPhoto.mComment );
    }
    try {
      // public String getSurveyJpgFile( String name )
      // public String name;    // photo filename without extension ".jpg" and survey prefix dir
      // String filename = TopoDroidApp.APP_FOTO_PATH + mPhoto.name + ".jpg";
      BitmapFactory.Options bfo = new BitmapFactory.Options();
      bfo.inJustDecodeBounds = true;
      BitmapFactory.decodeFile( mFilename, bfo );
      int required_size = TDSetting.mThumbSize;
      int scale = 1;
      while ( bfo.outWidth/scale/2 > required_size || bfo.outHeight/scale/2 > required_size ) {
        scale *= 2;
      }
      bfo.inJustDecodeBounds = false;
      bfo.inSampleSize = scale;
      Bitmap image = BitmapFactory.decodeFile( mFilename, bfo );
      if ( image != null ) {
        int w2 = image.getWidth() / 8;
        int h2 = image.getHeight() / 8;
        Bitmap image2 = Bitmap.createScaledBitmap( image, w2, h2, true );
        if ( image2 != null ) {
          MyBearingAndClino.applyOrientation( mIVimage, image2, mOrientation );
          // mIVimage.setHeight( h2 );
          // mIVimage.setWidth( w2 );
          mIVimage.setOnClickListener( this );
        } else {
          mIVimage.setVisibility( View.GONE );
        }
      }
    } catch ( OutOfMemoryError e ) {
      TDToast.make( mParent, R.string.null_bitmap );
    }

    mButtonOK.setOnClickListener( this );
    if ( mAtShot ) {
      mButtonDelete.setOnClickListener( this );
    } else {
      mButtonDelete.setVisibility( View.GONE );
    }
    // mButtonCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    // Button b = (Button) v;
    // TDLog.Log(  TDLog.LOG_INPUT, "PhotoEditDialog onClick() " + b.getText().toString() );

    switch ( v.getId() ) {
      case R.id.photo_ok:
        if ( mETcomment.getText() == null ) {
          mParent.updatePhoto( mPhoto, "" );
        } else {
          mParent.updatePhoto( mPhoto, mETcomment.getText().toString() );
        }
        break;
      case R.id.photo_delete:
        mParent.dropPhoto( mPhoto );
        break;
      case R.id.photo_image:
        mApp.viewPhoto( mContext, mFilename );
        return;
    }
    dismiss();
  }

}

