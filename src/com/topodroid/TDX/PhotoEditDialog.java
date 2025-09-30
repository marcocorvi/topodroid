/* @file PhotoEditDialog.java
 *
 * @author marco corvi
 * @date july 2012
 *
 * @brief TopoDroid photo edit dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDImage;

import android.os.Bundle;
import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;

class PhotoEditDialog extends MyDialog
                      implements View.OnClickListener
                      , IGeoCoder
{
  // private final PhotoActivity mParent;
  private final PhotoListDialog mParent;
  private PhotoInfo mPhoto;
  private String mFilename;
  private String mGeoCode; // geocode

  private EditText mETcomment;  // photo comment
  private ImageView mIVimage;   // photo image
  // private Button   mButtonOK;
  // private Button   mButtonDelete;
  // private Button   mButtonCancel;
  // private int mOrientation = 0;
  // private String mDate = "";
  private boolean mAtShot;

  private TDImage mTdImage = null;

  /**
   * @param context   context
   * @param parent    photo listing
   * @param photo     photo info
   */
  PhotoEditDialog( Context context, PhotoListDialog parent, PhotoInfo photo )
  {
    super( context, null, R.string.PhotoEditDialog ); // null app
    mParent = parent;
    mPhoto  = photo;
    // TDLog.v("photo edit dialog id " + photo.id + " ref. type " + photo.getRefType() );
    if ( mPhoto.getRefType() == MediaInfo.TYPE_XSECTION ) {
      if ( mPhoto.mFormat == PhotoInfo.FORMAT_JPEG ) {
        mFilename = TDPath.getSurveyJpgFile( TDInstance.survey, mPhoto.mItemName );
      } else {
        mFilename = TDPath.getSurveyPngFile( TDInstance.survey, mPhoto.mItemName );
      }
    } else {
      if ( mPhoto.mFormat == PhotoInfo.FORMAT_JPEG ) {
        mFilename = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mPhoto.id) );
      } else {
        mFilename = TDPath.getSurveyPngFile( TDInstance.survey, Long.toString(mPhoto.id) );
      }
    }
    mGeoCode  = mPhoto.getGeoCode();
    mAtShot   = ( mPhoto.getRefType() == MediaInfo.TYPE_SHOT );
    // TDLog.v("PhotoEditDialog file " + mFilename + " at shot " + mAtShot );
    mTdImage = new TDImage( mFilename );
    // TDLog.v( "photo edit dialog: " + photo.debugString() + " image width " + mTdImage.width() );
    // TDLog.v( "photo edit dialog: " + mFilename );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_PHOTO, "onCreate" );
    initLayout( R.layout.photo_edit_dialog, R.string.title_photo_comment );

    // TDLog.v( "photo edit dialog on create");
    mIVimage      = (ImageView) findViewById( R.id.photo_image );
    mETcomment    = (EditText) findViewById( R.id.photo_comment );
    Button buttonOK     = (Button) findViewById( R.id.photo_ok );
    Button buttonDelete = (Button) findViewById( R.id.photo_delete );
    Button buttonCode = (Button) findViewById( R.id.photo_geocode );
    // mButtonCancel = (Button) findViewById( R.id.photo_cancel );
    
    float a = mTdImage.azimuth();
    float c = mTdImage.clino();

    // TDLog.v( "photo edit dialog on create. Azimuth " + a + " Clino " + c );

    ((TextView) findViewById( R.id.photo_azimuth )).setText(
       String.format( mContext.getResources().getString( R.string.photo_azimuth_clino ), a, c ) );
    ((TextView) findViewById( R.id.photo_date )).setText( mPhoto.mDate );

    if ( mPhoto.mComment != null ) {
      mETcomment.setText( mPhoto.mComment );
    }
    if ( mTdImage.fillImageView( mIVimage, mTdImage.width()/8, mTdImage.height()/8, true ) ) {
      mIVimage.setOnClickListener( this );
    } else {
      mIVimage.setVisibility( View.GONE );
    }

    buttonOK.setOnClickListener( this );
    if ( TDLevel.overExpert ) {
      GeoCodes geocodes = TopoDroidApp.getGeoCodes();
      if ( geocodes.size() > 0 ) {
        buttonCode.setOnClickListener( this );
      } else {
        buttonCode.setVisibility( View.GONE );
      }
    } else {
      buttonCode.setVisibility( View.GONE );
    }
    // if ( mAtShot ) {
      buttonDelete.setOnClickListener( this );
    // } else {
    //   buttonDelete.setVisibility( View.GONE );
    // }

    // mButtonCancel.setOnClickListener( this );
    
    // TDLog.v( "photo edit dialog on create done");
  }

  /** react to a user tap
   * @param v   tapped view: it can be OK button, DELETE button, or the image thumbnail
   */
  @Override
  public void onClick(View v) 
  {
    // Button b = (Button) v;
    // TDLog.Log( TDLog.LOG_INPUT, "PhotoEditDialog onClick() " + b.getText().toString() );

    int vid = v.getId();
    if ( vid == R.id.photo_ok ) {
      if ( mETcomment.getText() == null ) {
        mParent.updatePhoto( mPhoto, "", mGeoCode );
      } else {
        mParent.updatePhoto( mPhoto, mETcomment.getText().toString(), mGeoCode );
      }
    } else if ( vid == R.id.photo_delete ) {
      mParent.dropPhoto( mPhoto );
    } else if ( vid == R.id.photo_image ) {
      // (new PhotoViewDialog( mContext, mPhoto )).show();
      (new PhotoViewDialog( mContext, mFilename, mPhoto.mTitle )).show();
      return;
    } else if ( vid == R.id.photo_geocode ) {
      (new GeoCodeDialog( mContext, this, mGeoCode )).show();
      return;
    }
    if ( mTdImage != null ) mTdImage.recycleImages();
    dismiss();
  }

  /** react to a user tap on BACK - recycle the image
   */
  @Override
  public void onBackPressed()
  {
    if ( mTdImage != null ) mTdImage.recycleImages();
    dismiss();
  }

  // interface IGeoCoder
  public void setGeoCode( String geocode ) { mGeoCode = (geocode == null)? "" : geocode; }
}

