/* @file DrawingPhotoEditDialog.java
 *
 * @author marco corvi
 * @date june 2017
 *
 * @brief TopoDroid drawing photo-item edit dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDImage;
// import com.topodroid.prefs.TDSetting;

import android.os.Bundle;
import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;

import java.util.Locale;

class DrawingPhotoEditDialog extends MyDialog
                             implements View.OnClickListener
                             , IGeoCoder
{
  // private final TopoDroidApp  mApp;
  // private final DrawingWindow mParent;
  private DrawingPhotoPath mPhoto;
  private String mFilename; // = null;
  private String mGeoCode;     // geocode

  private EditText mETcomment;  // photo comment
  private EditText mETsize;     // photo size (horizontal width) [m]
  // private ImageView mIVimage;   // photo image
  // private Button   mButtonOK;
  // private Button   mButtonDelete;
  // private Button   mButtonCancel;
  private TDImage mTdImage; // = null;

  private float mAzimuth = 0;
  private float mClino   = 0;
  private int mOrientation = 0;
  private String mDate = "";

  /** cstr
   * @param context   context
   * @param photo     drawing photo item
   */
  DrawingPhotoEditDialog( Context context, /* DrawingWindow parent, TopoDroidApp app, */ DrawingPhotoPath photo )
  {
    super( context, null, R.string.DrawingPhotoEditDialog ); // null app
    // mParent = parent;
    // mApp    = app;
    mPhoto    = photo;
    mFilename = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mPhoto.mId) );
    mGeoCode     = mPhoto.getCode();
    // TDLog.v( "DrawingPhotoEditDialog code <" + mGeoCode + ">" );
    
    mTdImage = new TDImage( mFilename );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_PHOTO, "onCreate" );
    initLayout( R.layout.drawing_photo_edit_dialog, R.string.title_photo_comment );

    ImageView iVimage = (ImageView) findViewById( R.id.photo_image );
    mETcomment        = (EditText) findViewById( R.id.photo_comment );
    mETsize           = (EditText) findViewById( R.id.photo_size );
    Button buttonOK   = (Button) findViewById( R.id.photo_ok );
    Button buttonCode = (Button) findViewById( R.id.photo_geocode );
    // mButtonDelete = (Button) findViewById( R.id.photo_delete );
    // mButtonCancel = (Button) findViewById( R.id.photo_cancel );

    ((TextView) findViewById( R.id.photo_azimuth )).setText(
       String.format( mContext.getResources().getString( R.string.photo_azimuth_clino ), mTdImage.azimuth(), mTdImage.clino() ) );
    ((TextView) findViewById( R.id.photo_date )).setText( mTdImage.date() );

    if ( mPhoto.mPointText != null ) {
      mETcomment.setText( mPhoto.mPointText );
    }
    mETsize.setText( String.format(Locale.US, "%.2f", mPhoto.getPhotoSize() ) );
    if ( mTdImage.fillImageView( iVimage, mTdImage.width()/8, mTdImage.height()/8, true ) ) {
      iVimage.setOnClickListener( this );
    } else {
      iVimage.setVisibility( View.GONE );
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
    // mButtonDelete.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );

  }

  /** react to user tap on a button
   * @param v tapped button
   * actions: 
   *   - ok: save the changes
   *   - image: display the photo image
   */
  @Override
  public void onClick(View v) 
  {
    // Button b = (Button) v;
    // TDLog.Log(  TDLog.LOG_INPUT, "DrawingPhotoEditDialog onClick() " + b.getText().toString() );

    int vid = v.getId();
    if ( vid == R.id.photo_ok ) {
      String comment = ( mETcomment.getText() == null )? "" : mETcomment.getText().toString();
      mPhoto.setPointText( comment );
      mPhoto.setCode( mGeoCode );
      try {
        float size = Float.parseFloat( mETsize.getText().toString() );
        if ( size < 1 ) size = 1; // min size 1 meter
        mPhoto.setPhotoSize( size );
      } catch ( NumberFormatException e ) {
      }
      TopoDroidApp.mData.updatePhoto( TDInstance.sid, mPhoto.mId, comment, mGeoCode );
    // } else if ( vid == R.id.photo_delete ) {
    //   mParent.dropPhoto( mPhoto );
    //   break;
    } else if ( vid == R.id.photo_image ) {
      // TopoDroidApp.viewPhoto( mContext, mFilename );
      if ( mTdImage != null ) {
        (new PhotoViewDialog( mContext, mFilename )).show();
      }
      return;
    } else if ( vid == R.id.photo_geocode ) {
      (new GeoCodeDialog( mContext, this, mGeoCode )).show();
      return;
    }
    if ( mTdImage != null ) mTdImage.recycleImages();
    dismiss();
  }

  /** react to user tap on the hardware back button
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

