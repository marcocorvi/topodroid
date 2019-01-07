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
package com.topodroid.DistoX;

// import java.io.IOException;

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

// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;

// import android.media.ExifInterface;

// import android.util.Log;

class DrawingPhotoEditDialog extends MyDialog
                             implements View.OnClickListener
{
  // private final TopoDroidApp  mApp;
  private final DrawingWindow mParent;
  private DrawingPhotoPath mPhoto;
  // private String mFilename;

  private EditText mETcomment;  // photo comment
  // private ImageView mIVimage;   // photo image
  // private Button   mButtonOK;
  // private Button   mButtonDelete;
  // private Button   mButtonCancel;
  private TDImage mTdImage = null;

  private float mAzimuth = 0;
  private float mClino   = 0;
  private int mOrientation = 0;
  private String mDate = "";

  private int mImageWidth;
  private int mImageHeight;
  private View     mContentView;
  private boolean  mOnView2;
  private ImageView mView2;

  /**
   * @param context   context
   */
  DrawingPhotoEditDialog( Context context, DrawingWindow parent, /* TopoDroidApp app, */ DrawingPhotoPath photo )
  {
    super( context, R.string.DrawingPhotoEditDialog );
    mParent = parent;
    // mApp    = app;
    mPhoto  = photo;
    String mFilename = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mPhoto.mId) );
    // TDLog.Log(TDLog.LOG_PHOTO, "DrawingPhotoEditDialog " + mFilename);
    
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
    Button buttonOK   = (Button) findViewById( R.id.photo_ok );
    // mButtonDelete = (Button) findViewById( R.id.photo_delete );
    // mButtonCancel = (Button) findViewById( R.id.photo_cancel );

    ((TextView) findViewById( R.id.photo_azimuth )).setText(
       String.format( mContext.getResources().getString( R.string.photo_azimuth_clino ), mTdImage.azimuth(), mTdImage.clino() ) );
    ((TextView) findViewById( R.id.photo_date )).setText( mTdImage.date() );

    if ( mPhoto.mPointText != null ) {
      mETcomment.setText( mPhoto.mPointText );
    }
    if ( mTdImage.fillImageView( iVimage, mTdImage.width()/8 ) ) {
      iVimage.setOnClickListener( this );
    } else {
      iVimage.setVisibility( View.GONE );
    }

    buttonOK.setOnClickListener( this );
    // mButtonDelete.setOnClickListener( this );
    // mButtonCancel.setOnClickListener( this );

    mContentView = (View) findViewById( R.id.content_view );
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
        TopoDroidApp.mData.updatePhoto( TDInstance.sid, mPhoto.mId, comment );
        break;
      // case R.id.photo_delete:
      //   mParent.dropPhoto( mPhoto );
      //   break;
      case R.id.photo_image:
        // TopoDroidApp.viewPhoto( mContext, mFilename );
	if ( mTdImage != null ) {
	  if ( mView2 == null ) {
            mView2 = new ImageView( mContext );
  	  }
	  if ( mTdImage.fillImageView( mView2 ) ) {
	    setContentView( mView2 );
	    mOnView2 = true;
	  }
	}
        return;
    }
    if ( mTdImage != null ) mTdImage.recycleImages();
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    if ( mOnView2 ) {
      mOnView2 = false;
      setContentView( mContentView );
      return;
    }
    if ( mTdImage != null ) mTdImage.recycleImages();
    dismiss();
  }

}

