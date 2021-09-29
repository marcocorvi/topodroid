/* @file PhotoDialog.java
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
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDImage;

import android.os.Bundle;
import android.content.Context;

import android.widget.ImageView;
import android.widget.Button;

import android.view.View;

class PhotoDialog extends MyDialog
                  implements View.OnClickListener
{
  private TDImage mTdImage = null;

  private ImageView mView2;
  // private Button   mButtonCancel;

  /**
   * @param context   context
   * @param photo     photo info
   */
  PhotoDialog( Context context, PhotoInfo photo )
  {
    super( context, R.string.PhotoDialog );
    // TDLog.Log( TDLog.LOG_PHOTO, "Photo Dialog");
    // TDLog.v("photo dialog id " + photo.id );
    String filename = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(photo.id) );
    mTdImage = new TDImage( filename );
  }

  PhotoDialog( Context context, String filename )
  {
    super( context, R.string.PhotoDialog );
    // TDLog.v("photo dialog file " + filename );
    // TDLog.Log( TDLog.LOG_PHOTO, "Photo Dialog");
    mTdImage = new TDImage( filename );
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
  }

  @Override
  public void onClick(View v) 
  {
    if ( mTdImage != null ) mTdImage.recycleImages();
    dismiss();
  }

}

