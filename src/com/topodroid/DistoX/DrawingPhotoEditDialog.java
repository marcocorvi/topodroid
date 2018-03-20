/* @file DrawingPhotoEditDialog.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid drawing photo-item edit dialog 
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

class DrawingPhotoEditDialog extends MyDialog
                             implements View.OnClickListener
{
  private TopoDroidApp  mApp;
  private DrawingWindow mParent;
  private DrawingPhotoPath mPhoto;
  private String mFilename;

  private EditText mETcomment;  // photo comment
  // private ImageView mIVimage;   // photo image
  // private Button   mButtonOK;
  // private Button   mButtonDelete;
  // private Button   mButtonCancel;
  private float mAzimuth = 0;
  private float mClino   = 0;
  private int mOrientation = 0;
  private String mDate = "";

  /**
   * @param context   context
   */
  DrawingPhotoEditDialog( Context context, DrawingWindow parent, TopoDroidApp app, DrawingPhotoPath photo )
  {
    super( context, R.string.DrawingPhotoEditDialog );
    mParent = parent;
    mApp    = app;
    mPhoto  = photo;
    mFilename = TDPath.getSurveyJpgFile( mApp.mySurvey, Long.toString(mPhoto.mId) );
    TDLog.Log(TDLog.LOG_PHOTO, "DrawingPhotoEditDialog " + mFilename);

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
        mAzimuth = Integer.parseInt( b.substring(0,k) ) / 100.0f;
        k = c.indexOf('/');
        mClino = Integer.parseInt( c.substring(0,k) ) / 100.0f;
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
    initLayout( R.layout.drawing_photo_edit_dialog, R.string.title_photo_comment );

    ImageView iVimage      = (ImageView) findViewById( R.id.photo_image );
    mETcomment    = (EditText) findViewById( R.id.photo_comment );
    Button buttonOK     = (Button) findViewById( R.id.photo_ok );
    // mButtonDelete = (Button) findViewById( R.id.photo_delete );
    // mButtonCancel = (Button) findViewById( R.id.photo_cancel );

    ((TextView) findViewById( R.id.photo_azimuth )).setText(
       String.format( mContext.getResources().getString( R.string.photo_azimuth_clino ), mAzimuth, mClino ) );
    ((TextView) findViewById( R.id.photo_date )).setText( mDate );

    if ( mPhoto.mPointText != null ) {
      mETcomment.setText( mPhoto.mPointText );
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
          MyBearingAndClino.applyOrientation( iVimage, image2, mOrientation );
          // mIVimage.setHeight( h2 );
          // mIVimage.setWidth( w2 );
          iVimage.setOnClickListener( this );
        } else {
          iVimage.setVisibility( View.GONE );
        }
      }
    } catch ( OutOfMemoryError e ) {
      TDToast.make( mParent, R.string.null_bitmap );
    }

    buttonOK.setOnClickListener( this );
    // mButtonDelete.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );
  }

  @Override
  public void onClick(View v) 
  {
    // Button b = (Button) v;
    // TDLog.Log(  TDLog.LOG_INPUT, "DrawingPhotoEditDialog onClick() " + b.getText().toString() );

    switch ( v.getId() ) {
      case R.id.photo_ok:
        String comment = ( mETcomment.getText() == null )? "" : mETcomment.getText().toString();
        mPhoto.setPointText( comment );
        TopoDroidApp.mData.updatePhoto( mApp.mSID, mPhoto.mId, comment );
        break;
      // case R.id.photo_delete:
      //   mParent.dropPhoto( mPhoto );
      //   break;
      case R.id.photo_image:
        mApp.viewPhoto( mContext, mFilename );
        return;
    }
    dismiss();
  }

}

