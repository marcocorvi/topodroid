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

import android.util.Log;

class PhotoEditDialog extends MyDialog
                      implements View.OnClickListener
{
  private final PhotoActivity mParent;
  private PhotoInfo mPhoto;
  private String mFilename;

  private View     mContentView;
  private boolean  mOnView2;
  private ImageView mView2;

  private EditText mETcomment;  // photo comment
  private ImageView mIVimage;   // photo image
  private Button   mButtonOK;
  private Button   mButtonDelete;
  // private Button   mButtonCancel;
  private int mOrientation = 0;
  private String mDate = "";
  private boolean mAtShot;

  private TDImage mTdImage = null;

  /**
   * @param context   context
   */
  PhotoEditDialog( Context context, PhotoActivity parent, PhotoInfo photo )
  {
    super( context, R.string.PhotoEditDialog );
    mParent = parent;
    mPhoto  = photo;
    // mFilename = filename;
    mFilename = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mPhoto.id) );
    mAtShot   = (mPhoto.shotid >= 0);
    // TDLog.Log(TDLog.LOG_PHOTO, "PhotoEditDialog " + mFilename);
    mTdImage = new TDImage( mFilename );
    // Log.v("DistoX", "photo edit dialog: " + photo.debugString() + " image width " + mTdImage.width() );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.Log( TDLog.LOG_PHOTO, "onCreate" );
    initLayout( R.layout.photo_edit_dialog, R.string.title_photo_comment );

    // Log.v("DistoX", "photo edit dialog on create");
    mIVimage      = (ImageView) findViewById( R.id.photo_image );
    mETcomment    = (EditText) findViewById( R.id.photo_comment );
    mButtonOK     = (Button) findViewById( R.id.photo_ok );
    mButtonDelete = (Button) findViewById( R.id.photo_delete );
    // mButtonCancel = (Button) findViewById( R.id.photo_cancel );
    
    float a = mTdImage.azimuth();
    float c = mTdImage.clino();

    // Log.v("DistoX", "photo edit dialog on create. Azimuth " + a + " Clino " + c );


    ((TextView) findViewById( R.id.photo_azimuth )).setText(
       String.format( mContext.getResources().getString( R.string.photo_azimuth_clino ), a, c ) );
    ((TextView) findViewById( R.id.photo_date )).setText( mDate );

    if ( mPhoto.mComment != null ) {
      mETcomment.setText( mPhoto.mComment );
    }
    if ( mTdImage.fillImageView( mIVimage, mTdImage.width()/8 ) ) {
      mIVimage.setOnClickListener( this );
    } else {
      mIVimage.setVisibility( View.GONE );
    }

    mButtonOK.setOnClickListener( this );
    if ( mAtShot ) {
      mButtonDelete.setOnClickListener( this );
    } else {
      mButtonDelete.setVisibility( View.GONE );
    }
    // mButtonCancel.setOnClickListener( this );
    
    mContentView = findViewById( R.id.view_one );
    mOnView2 = false;
    // Log.v("DistoX", "photo edit dialog on create done");
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
        // TopoDroidApp.viewPhoto( mContext, mFilename );
	if ( mView2 == null ) {
          mView2 = new ImageView( mContext );
	}
	if ( mTdImage.fillImageView( mView2 ) ) {
	  setContentView( mView2 );
	  mOnView2 = true;
          return;
	}
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

