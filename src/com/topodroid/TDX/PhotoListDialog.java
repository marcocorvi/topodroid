/* @file PhotoListDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey photo listing
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;
import android.os.AsyncTask;

import android.content.Context;

import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.KeyEvent;

// import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.preference.PreferenceManager;

public class PhotoListDialog extends MyDialog
                             implements OnItemClickListener
                             // , ILister
{
  private DataHelper mApp_mData;

  private ListView mList;
  private PhotoAdapter mDataAdapter;
  private long mShotId = -1;   // id of the shot

  private String mSaveData = "";
  private TextView mSaveTextView = null;

  String mPhotoStation;
  String mPhotoComment;
  long   mPhotoId;
  // List< PhotoInfo > mPhotos;

  /** cstr
   * @param ctx         context
   * @param data_helper database class
   */
  public PhotoListDialog( Context ctx, DataHelper data_helper )
  {
    super( ctx, null, R.string.PhotoListDialog ); // null app
    mApp_mData = data_helper;
    // mPhotos    = photos;
  }


  // -------------------------------------------------------------------
  // ILister interface
  /*
  @Override
  public void refreshDisplay( int nr, boolean toast )
  {
    updateDisplay( );
  }

  @Override
  public void updateBlockList( long blk_id ) { }
  
  @Override
  public void setConnectionStatus( int status ) { }

  @Override
  public void setRefAzimuth( float azimuth, long fixed_extend ) { }
    
  public void setTheTitle() { }
  */

  // ----------------------------------------------------------------------

  /** update the list
   */
  private void updateDisplay( )
  {
    // TDLog.Log( TDLog.LOG_PHOTO, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    if ( mApp_mData != null && TDInstance.sid >= 0 ) {
      List< PhotoInfo > list = mApp_mData.selectAllPhotos( TDInstance.sid, TDStatus.NORMAL );
      // TDLog.v( "update photos list size " + list.size() );
      updatePhotoList( list );
      // setTitle( TDInstance.survey );
    // } else {
    //   TDToast.makeBad( R.string.no_survey );
    }
  }

  // -------------------------------------------------------------------

  /** update the list og photos
   * @param list  list of photos
   */
  private void updatePhotoList( List< PhotoInfo > list )
  {
    // TDLog.Log(TDLog.LOG_PHOTO, "updatePhotoList size " + list.size() );
    // TDLog.v( "photo activity, update photo list " );
    // TDLog.v( "photo activity, update photo list. size " + list.size() );
    if ( TDUtil.isEmpty(list) ) {
      TDToast.makeBad( R.string.no_photos );
      dismiss();
    }
    mDataAdapter.clear();
    for ( PhotoInfo info : list ) mDataAdapter.add( info );
    // mList.setAdapter( mDataAdapter );
  }

  /** implements item click listener: react to a tap on a photo entry in the list
   * @param parent  view parent container
   * @param view    clicked item view
   * @param pos     position of the item in the container
   * @param id      item id (?)
   */
  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    PhotoInfo info = mDataAdapter.get(pos); // mSavePhoto
    // String filename = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(info.id) );
    // TDLog.v( "Photo info: id " + info.id + " reference " + info.mItemName + " item " + info.mItemId + " type " + info.mRefType );
    PhotoEditDialog ped = (new PhotoEditDialog( mContext, this, info ));
    ped.show();
    // TDLog.v( "photo activity started photo edit dialog");
  }

  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    initLayout(R.layout.photo_list_dialog, R.string.title_photo_list );

    mList = (ListView) findViewById( R.id.list );
    mList.setOnItemClickListener( this );
    // mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );
    // ArrayList< String > names = new ArrayList<>();

    // mApp = (TopoDroidApp) getApplication();
    mApp_mData = TopoDroidApp.mData;
    mDataAdapter = new PhotoAdapter( mContext, R.layout.row, new ArrayList< PhotoInfo >() );
    mList.setAdapter( mDataAdapter );

    updateDisplay( );

    ( (Button)findViewById( R.id.button_back )).setOnClickListener( new View.OnClickListener() {
      @Override public void onClick( View v ) 
      {
        dismiss();
      }
    } );
  }

  // ------------------------------------------------------------------

  /** remove a photo - delete the photo file
   * @param photo    info of the photo to remove
   */
  public void dropPhoto( PhotoInfo photo )
  {
    long photo_id = photo.getId();
    mApp_mData.deletePhotoRecord( photo.getSurveyId(), photo_id );
    String jpg_path = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString( photo.id ) );
    TDFile.deleteFile( jpg_path );
    String png_path = jpg_path.replace(".jpg", ".png" );
    if ( TDFile.hasTopoDroidFile( png_path ) ) {
      TDFile.deleteFile( png_path );
    }
    if ( photo.getRefType() == MediaInfo.TYPE_PLOT ) {
      // get the plot tdr file
      new AsyncTask<PhotoInfo,Void,Boolean>() {
        @Override protected Boolean doInBackground(PhotoInfo... args)
        {
          ShotWindow.mWaitPlot = true;
          PhotoInfo photo = args[0];
          long photo_id = photo.getId();
          long pid = photo.getItemId(); // plot ID
          String plot_name = mApp_mData.getPlotName( TDInstance.sid, pid );
          String plot_file = TDPath.getSurveyPlotTdrFile( TDInstance.survey, plot_name ); // full path
          boolean ret = DrawingIO.changeTdrFileDropMedia( plot_file, MediaInfo.MEDIA_PHOTO, photo_id ); 
          ShotWindow.mWaitPlot = false;
          return ret;
        }
        
        @Override protected void onPostExecute( Boolean result )
        { // TODO
        }
      }.execute( photo );
    }
    // mPhotos.remove( photo );
    updateDisplay( ); // FIXME
  }

  /** update a photo comment in the photo info
   * @param photo    info of the photo
   * @param comment  photo comment
   */
  public void updatePhoto( PhotoInfo photo, String comment, String code )
  {
    // TDLog.Log( TDLog.LOG_PHOTO, "updatePhoto comment " + comment );
    if ( mApp_mData.updatePhotoCommentAndCode( photo.sid, photo.id, comment, code ) ) {
      // need to update the PhotoInfo ?
      updateDisplay( ); // FIXME
    } else {
      TDToast.makeBad( R.string.no_db );
    }
  }

  // public void notifyDisconnected()
  // {
  // }

  /** implements enable-bluetooth - nothing to do
   */
  public void enableBluetoothButton( boolean enable ) { } 

}
