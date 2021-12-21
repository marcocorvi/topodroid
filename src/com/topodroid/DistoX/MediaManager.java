/* @file MediaManager.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid photo acquisition management
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;

// import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;

class MediaManager
{
  private DataHelper mData;

  private long    mPhotoId = -1;
  private long    mAudioId = 0;  // audio-negative id
  private String  mComment;
  private int     mCamera = PhotoInfo.CAMERA_UNDEFINED;
  private long    mShotId;   // photo/sensor shot id
  // private File    mImageFile;
  // private File    mAudioFile;
  private String  mImageFilepath;
  private String  mAudioFilepath;
  private float   mMediaX, mMediaY;

  MediaManager( DataHelper data )
  {
    mData = data;
    mImageFilepath = null;
    mAudioFilepath = null;
  }

  long prepareNextPhoto( long sid, String comment, int camera )
  {
    mShotId       = sid;
    mComment = comment;
    mCamera  = camera;
    mPhotoId = mData.nextPhotoId( TDInstance.sid );
    mImageFilepath = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mPhotoId) ); // photo file is "survey/id.jpg"
    // mImageFile = TDFile.getTopoDroidFile( mImageFilepath );
    return mPhotoId;
  }

  /** return the next negative audio index (ID)
   * @param sid        shot id
   * @param comment    
   */
  long prepareNextAudioNeg( long sid, String comment )
  {
    mShotId  = sid;
    mComment = comment;
    mAudioId = mData.nextAudioNegId( TDInstance.sid ); // negative id's are for sketch audios
    mAudioFilepath = TDPath.getSurveyWavFile( TDInstance.survey, Long.toString(mAudioId) ); // audio file is "survey/id.wav"
    // mAudioFile = TDFile.getTopoDroidFile( mAudioFilepath );
    return mAudioId;
  }

  /** @return true if the photos are taken by TopoDroid
   */
  boolean isTopoDroidCamera() { return (mCamera == PhotoInfo.CAMERA_TOPODROID); }

  /** set the camera
   * @param camera   camera index
   */
  void setCamera( int camera ) { mCamera = camera; }

  /** @return media camera index
   */
  int getCamera()  { return mCamera; }

  /** @return shot ID
   */
  long getShotId()  { return mShotId; }

  /** @return media comment
   */
  String getComment() { return mComment; }

  /** @return photo ID
   */
  long getPhotoId() { return mPhotoId; }

  /** @return audio ID
   */
  long getAudioId() { return mAudioId; }

  // File getImageFile() { return mImageFile; }

  /** @return the photo file full path
   */
  String getImageFilepath() { return mImageFilepath; } 

  /** set the media point coordinates
   * @param x   X coordinate
   * @param y   Y coordinate
   */
  void setPoint( float x, float y ) { mMediaX = x; mMediaY = y; }

  /** @return media point X coordinate
   */
  float getX() { return mMediaX; }

  /** @return media point Y coordinate
   */
  float getY() { return mMediaY; }

  /** store the photo - and clear the photo filepath
   * @param bitmap      ...
   * @param compression compression mode
   * @return true if successful
   */
  boolean savePhoto( Bitmap bitmap, int compression )
  { 
    boolean ret = false;
    if ( mImageFilepath != null ) {
      try {
        FileOutputStream fos = TDFile.getFileOutputStream( mImageFilepath );
        bitmap.compress( Bitmap.CompressFormat.JPEG, compression, fos );
        fos.flush();
        fos.close();
        mData.insertPhoto( TDInstance.sid, mPhotoId, mShotId, "", TDUtil.currentDate(), mComment, mCamera );
        ret = true;
      } catch ( FileNotFoundException e ) {
        TDLog.Error("cannot save photo: file not found");
      } catch ( IOException e ) {
       TDLog.Error("cannot save photo: i/o error");
      }
    } else {
      TDLog.Error("cannot save photo: null file" );
    }
    mImageFilepath = null;
    return ret;
  }

  // void insertPhoto()
  // {
  // }

}
