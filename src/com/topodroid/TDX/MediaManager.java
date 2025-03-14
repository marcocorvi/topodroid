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
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
// import com.topodroid.utils.TDFile;
// import com.topodroid.utils.TDUtil;

// import java.io.File;
// import java.io.FileOutputStream;
// import java.io.FileNotFoundException;
// import java.io.IOException;

// import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;

class MediaManager
{
  private DataHelper mData;

  private long    mPhotoId = -1;
  private long    mAudioId = 0;  // audio-negative id
  private String  mComment;
  private String  mCode;
  private float   mSize = 1;     // photo size (horizontal width) [m]
  private int     mCamera = PhotoInfo.CAMERA_UNDEFINED;
  private long    mItemId;   // photo/sensor reference item ID: shot ID or plot ID
  private long    mItemType; // reference item type
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

  /**
   * @param item_id  item ID
   * @param comment  description
   * @param size     TODO
   * @param camera   camera type
   * @param code     geomorphology code
   * @param rettype  reference item type
   */
  long prepareNextPhoto( long item_id, String comment, float size, int camera, String code, long reftype )
  {
    mItemId   = item_id;
    mItemType = reftype;
    mComment  = comment;
    mCode     = code;
    mSize     = size;
    mCamera   = camera;
    mPhotoId  = mData.nextPhotoId( TDInstance.sid );
    mImageFilepath = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mPhotoId) ); // photo file is "survey/id.jpg"
    TDLog.v("Media Manager prepare photo id " + mPhotoId );
    // mImageFile = TDFile.getTopoDroidFile( mImageFilepath );
    return mPhotoId;
  }

  /** @return the next audio index (ID)
   * @param item_id    reference item ID: plot ID because this method is not used for shot ID
   * @param comment    audio comment
   * @param reftype    reference item type (always TYPE_PLOT)
   * @note this is used only be DrawingWindow which calls it with item_id = pid
   */
  long prepareNextAudio( long item_id, String comment, long reftype )
  {
    mItemId   = item_id;
    mItemType = reftype;
    mComment  = comment;
    mCode     = null;
    mAudioId  = mData.nextAudioId( TDInstance.sid ); // , mItemId, mItemType );
    String audio_name = MediaInfo.getMediaName( mAudioId, (int)mItemType );
    mAudioFilepath = TDPath.getSurveyWavFile( TDInstance.survey, audio_name ); // audio file is "survey/idX.wav"
    // mAudioFile = TDFile.getTopoDroidFile( mAudioFilepath );
    return mAudioId;
  }

  // /** @return true if the photos are taken by TopoDroid
  //  */
  // boolean isTopoDroidCamera() { return (mCamera == PhotoInfo.CAMERA_TOPODROID); }

  /** set the camera
   * @param camera   camera index
   */
  void setCamera( int camera ) { mCamera = camera; }

  /** @return media camera index
   */
  int getCamera()  { return mCamera; }

  /** set reference item ID and type
   * @param id    item ID
   * @param reftype reference item type
   */
  void setReferenceItem( long id, long reftype )
  {
    mItemId   = id;
    mItemType = reftype;
  }

  /** @return reference item ID
   */
  long getItemId()  { return mItemId; }

  /** @return the reference item type, either TYPE_SHOT or TYPE_PLOT
   */
  long getItemType() { return mItemType; }

  /** @return media comment
   */
  String getComment() { return mComment; }

  /** set media comment
   * @param  comment  new media comment
   */
  void setComment( String comment ) { mComment = comment; }

  /** @return current media geomorphology code
   */
  String getCode() { return mCode; }

  /** @return current photo size (horizontal width) [m]
   */
  float getPhotoSize() { return mSize; }

  /** @return current photo ID
   */
  long getPhotoId() { return mPhotoId; }

  /** @return current audio ID
   */
  long getAudioId() { return mAudioId; }

  // File getImageFile() { return mImageFile; }

  /** @return the current photo file full path
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

  // /** store the photo - and clear the photo filepath - NOT USED
  //  * @param bitmap      ...
  //  * @param compression compression mode
  //  * @return true if successful
  //  */
  // boolean savePhotoFile( Bitmap bitmap, int compression )
  // { 
  //   boolean ret = false;
  //   if ( mImageFilepath != null ) {
  //     try {
  //       FileOutputStream fos = TDFile.getFileOutputStream( mImageFilepath );
  //       bitmap.compress( Bitmap.CompressFormat.JPEG, compression, fos );
  //       fos.flush();
  //       fos.close();
  //       mData.insertPhotoRecord( TDInstance.sid, mPhotoId, mItemId, "", TDUtil.currentDate(), mComment, mCamera, mCode );
  //       ret = true;
  //     } catch ( FileNotFoundException e ) {
  //       TDLog.e("cannot save photo: file not found");
  //     } catch ( IOException e ) {
  //      TDLog.e("cannot save photo: i/o error");
  //     }
  //   } else {
  //     TDLog.e("cannot save photo: null file" );
  //   }
  //   mImageFilepath = null;
  //   return ret;
  // }

  // boolean insertPhoto()
  // {
  //   return false;
  // }

}
