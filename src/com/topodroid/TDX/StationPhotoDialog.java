/* @file StationPhotoDialog.java
 *
 * @author marco corvi
 * @date july 2012
 *
 * @brief TopoDroid station photo edit dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDImage;

import android.os.Bundle;
import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Button;
import android.view.View;

class StationPhotoDialog extends MyDialog
                      implements View.OnClickListener
{
  // private final PhotoActivity mParent;
  private final CurrentStationDialog mParent;
  private StationInfo mStation;
  private PhotoInfo mPhoto = null;
  private String mFilename = null;

  // private EditText mETcomment;  // photo comment
  private ImageView mIVimage;   // photo image
  // private Button   mButtonRetake;
  // private Button   mButtonDelete;
  // private Button   mButtonCancel;
  // private int mOrientation = 0;
  // private String mDate = "";

  private TDImage mTdImage = null;

  /**
   * @param context   context
   * @param parent    current station dialog
   * @param station   station info
   */
  StationPhotoDialog( Context context, CurrentStationDialog parent, StationInfo station )
  {
    super( context, null, R.string.StationPhotoDialog ); // null app
    // TDLog.v("photo edit dialog id " + photo.id );
    mParent  = parent;
    mStation = station;
    long photoId = mStation.getPhotoId();
    TDLog.v("station " + station.mName + " photo ID " + photoId );
    if ( photoId > 0 ) {
      mPhoto = TopoDroidApp.mData.selectPhotoById( TDInstance.sid, photoId, MediaInfo.TYPE_STATION );
      if ( mPhoto != null ) {
        if ( mPhoto.mFormat == PhotoInfo.FORMAT_JPEG ) {
          mFilename = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mPhoto.id) );
        } else {
          mFilename = TDPath.getSurveyPngFile( TDInstance.survey, Long.toString(mPhoto.id) );
        }
      }
      if ( mFilename != null ) {
        TDLog.v("StationPhotoDialog file " + mFilename);
        mTdImage = new TDImage( mFilename );
      } 
    }
    assert( photoId == 0 || photoId == mPhoto.getId() );

    // TDLog.v( "photo edit dialog: " + photo.debugString() + " image width " + mTdImage.width() );
    // TDLog.v( "photo edit dialog: " + mFilename );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_PHOTO, "onCreate" );
    initLayout( R.layout.station_photo_dialog, R.string.title_station_photo );

    // TDLog.v( "photo edit dialog on create");
    mIVimage      = (ImageView) findViewById( R.id.photo_image );
    // mETcomment    = (EditText) findViewById( R.id.photo_comment );
    Button buttonRetake = (Button) findViewById( R.id.photo_retake );
    Button buttonDelete = (Button) findViewById( R.id.photo_delete );
    // mButtonCancel = (Button) findViewById( R.id.photo_cancel );
    
    if ( mTdImage != null ) {
      float a = mTdImage.azimuth();
      float c = mTdImage.clino();
      // TDLog.v( "photo edit dialog on create. Azimuth " + a + " Clino " + c );
      ((TextView) findViewById( R.id.photo_azimuth )).setText(
        String.format( resString( R.string.photo_azimuth_clino ), a, c ) );
    }
    if ( mPhoto != null ) {
      ((TextView) findViewById( R.id.photo_date )).setText( mPhoto.mDate );
      buttonDelete.setOnClickListener( this );
    } else {
      ((TextView) findViewById( R.id.photo_date )).setText( TDUtil.currentDate() );
      buttonDelete.setVisibility( View.GONE );
    }

    // if ( mPhoto.mComment != null ) {
    //   mETcomment.setText( mPhoto.mComment );
    // }
    if ( mFilename != null ) {
      if ( mTdImage.fillImageView( mIVimage, mTdImage.width()/8, mTdImage.height()/8, true ) ) {
        mIVimage.setOnClickListener( this );
      } else {
        mIVimage.setVisibility( View.GONE );
      }
    } else {
      mIVimage.setVisibility( View.GONE );
    }

    buttonRetake.setOnClickListener( this );

    // mButtonCancel.setOnClickListener( this );
    
    // TDLog.v( "photo edit dialog on create done");
  }

  private void deletePhoto()
  {
    if ( mPhoto == null ) return;
    TopoDroidApp.mData.deletePhotoRecord( TDInstance.sid, mPhoto.getId() );
    TopoDroidApp.mData.updateStationPhoto( TDInstance.sid, mStation.mName, 0 );
    mStation.mPhotoId = 0;
    mPhoto = null;
  }

  /** react to a user tap
   * @param v   tapped view: it can be OK button, DELETE button, or the image thumbnail
   */
  @Override
  public void onClick(View v) 
  {
    int vid = v.getId();
    if ( vid == R.id.photo_retake ) {
      deletePhoto();
      // take a new photo
      mParent.takePhoto();
    } else if ( vid == R.id.photo_delete ) {
      deletePhoto();
    } else if ( vid == R.id.photo_image ) {
      (new PhotoViewDialog( mContext, mPhoto )).show();
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

}

