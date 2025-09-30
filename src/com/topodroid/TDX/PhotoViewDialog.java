/* @file PhotoViewDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo comment dialog (to enter the comment of the photo)
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

import android.widget.ImageView;
import android.widget.Button;

import android.view.View;

class PhotoViewDialog extends MyDialog
                  implements View.OnClickListener
{
  private TDImage mTdImage = null;

  private ImageView mView2;
  private String mTitle = null;
  // private Button   mButtonCancel;

  /** cstr
   * @param context   context
   * @param photo     photo info
   * @note called by StationPhotoDialog
   */
  PhotoViewDialog( Context context, PhotoInfo photo )
  {
    super( context, null, R.string.PhotoViewDialog ); // null app
    // TDLog.Log( TDLog.LOG_PHOTO, "Photo Dialog");
    // TDLog.v("photo dialog id " + photo.id );
    String filename = null;
    if ( photo.mFormat == PhotoInfo.FORMAT_JPEG ) {
      filename = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(photo.id) );
    } else if ( photo.mFormat == PhotoInfo.FORMAT_PNG ) {
      filename = TDPath.getSurveyPngFile( TDInstance.survey, Long.toString(photo.id) );
    }
    mTdImage = new TDImage( filename );
    mTitle   = photo.mTitle;
  }

  // /** cstr
  //  * @param context   context
  //  * @param filename  file path
  //  */
  // PhotoViewDialog( Context context, String filename )
  // {
  //   super( context, null, R.string.PhotoViewDialog ); // null app
  //   // TDLog.v("photo dialog file " + filename );
  //   // TDLog.Log( TDLog.LOG_PHOTO, "Photo Dialog");
  //   mTdImage = new TDImage( filename );
  // }

  /** cstr
   * @param context   context
   * @param filename  file path
   * @param title     dialog title
   * @param called by PhotoEditDialog DrawingPhotoEditDialog DrawingLineSectionDialog
   */
  PhotoViewDialog( Context context, String filename, String title )
  {
    super( context, null, R.string.PhotoViewDialog ); // null app
    // TDLog.v("photo dialog file " + filename );
    // TDLog.Log( TDLog.LOG_PHOTO, "Photo Dialog");
    mTdImage = new TDImage( filename );
    mTitle   = title;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    // TDLog.Log(  TDLog.LOG_PHOTO, "Photo Dialog onCreate" );
    initLayout(R.layout.photo_dialog, R.string.title_photo_view );
    
    mView2 = (ImageView) findViewById( R.id.photo_view );
    if ( ! mTdImage.fillImageView( mView2, (int)(TopoDroidApp.mDisplayWidth), (int)(TopoDroidApp.mDisplayHeight), true ) ) {
      if ( mTdImage != null ) mTdImage.recycleImages();
      dismiss();
    }
    mView2.invalidate();

    ( (Button) findViewById(R.id.photo_back ) ).setOnClickListener( this );
    if (mTitle != null) setTitle( mTitle );
  }

  /** implements click listener
   * @param v   tapped view
   */
  @Override
  public void onClick(View v) 
  {
    if ( mTdImage != null ) mTdImage.recycleImages();
    dismiss();
  }

}

