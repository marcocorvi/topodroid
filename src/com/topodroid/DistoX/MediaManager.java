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

import android.util.Log;

import java.io.File;
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
  private File    mImageFile;
  private File    mAudioFile;
  private float   mMediaX, mMediaY;

  MediaManager( DataHelper data )
  {
    mData = data;
    mImageFile = null;
    mAudioFile = null;
  }

  long prepareNextPhoto( long sid, String comment, int camera )
  {
    mShotId       = sid;
    mComment = comment;
    mCamera  = camera;
    mPhotoId = mData.nextPhotoId( TDInstance.sid );
    mImageFile = new File( TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mPhotoId) ) ); // photo file is "survey/id.jpg"
    return mPhotoId;
  }

  // @param sid        shot id
  // @param comment    
  // @return the next audio negative ID
  long prepareNextAudioNeg( long sid, String comment )
  {
    mShotId  = sid;
    mComment = comment;
    mAudioId = mData.nextAudioNegId( TDInstance.sid ); // negative id's are for sketch audios
    mAudioFile = new File( TDPath.getSurveyAudioFile( TDInstance.survey, Long.toString(mAudioId) ) ); // audio file is "survey/id.wav"
    return mAudioId;
  }

  boolean isTopoDroidCamera() { return (mCamera == PhotoInfo.CAMERA_TOPODROID); }

  void setCamera( int camera ) { mCamera = camera; }
  void setPoint( float x, float y ) { mMediaX = x; mMediaY = y; }

  String getComment() { return mComment; }
  int getCamera()  { return mCamera; }

  long getShotId()  { return mShotId; }
  long getPhotoId() { return mPhotoId; }
  long getAudioId() { return mAudioId; }

  File getImagefile() { return mImageFile; }

  float getX() { return mMediaX; }
  float getY() { return mMediaY; }

  boolean savePhoto( Bitmap bitmap, int compression )
  { 
    boolean ret = false;
    if ( mImageFile != null ) {
      try {
        FileOutputStream fos = new FileOutputStream( mImageFile );
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
    mImageFile = null;
    return ret;
  }

  // void insertPhoto()
  // {
  // }

}
