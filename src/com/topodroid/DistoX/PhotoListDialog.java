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
package com.topodroid.DistoX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDStatus;
import com.topodroid.ui.MyDialog;
// import com.topodroid.prefs.TDSetting;

import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;

import android.content.Context;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;

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

  public PhotoListDialog( Context ctx, DataHelper data_helper )
  {
    super( ctx, R.string.PhotoListDialog );
    mApp_mData = data_helper;
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

  private void updateDisplay( )
  {
    // TDLog.Log( TDLog.LOG_PHOTO, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    if ( mApp_mData != null && TDInstance.sid >= 0 ) {
      List< PhotoInfo > list = mApp_mData.selectAllPhotos( TDInstance.sid, TDStatus.NORMAL );
      // TDLog.Log( TDLog.LOG_PHOTO, "update shot list size " + list.size() );
      updatePhotoList( list );
      setTitle( TDInstance.survey );
    // } else {
    //   TDToast.makeBad( R.string.no_survey );
    }
  }

  // -------------------------------------------------------------------

  private void updatePhotoList( List< PhotoInfo > list )
  {
    // TDLog.Log(TDLog.LOG_PHOTO, "updatePhotoList size " + list.size() );
    // TDLog.v( "photo activity, update photo list " );
    // TDLog.v( "photo activity, update photo list. size " + list.size() );
    if ( list.size() == 0 ) {
      TDToast.makeBad( R.string.no_photos );
      dismiss();
    }
    mDataAdapter.clear();
    for ( PhotoInfo info : list ) mDataAdapter.add( info );
    // mList.setAdapter( mDataAdapter );
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    PhotoInfo info = mDataAdapter.get(pos); // mSavePhoto
    // String filename = TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(info.id) );
    // TDLog.v( "Photo file <" + filename + "> id " + info.id );
    PhotoEditDialog ped = (new PhotoEditDialog( mContext, this, info ));
    ped.show();
    // TDLog.v( "photo activity started photo edit dialog");
  }


  // ---------------------------------------------------------------
  
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

  public void dropPhoto( PhotoInfo photo )
  {
    mApp_mData.deletePhoto( photo.sid, photo.id );
    TDFile.deleteFile( TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(photo.id) ) );
    updateDisplay( ); // FIXME
  }

  public void updatePhoto( PhotoInfo photo, String comment )
  {
    // TDLog.Log( TDLog.LOG_PHOTO, "updatePhoto comment " + comment );
    if ( mApp_mData.updatePhoto( photo.sid, photo.id, comment ) ) {
      updateDisplay( ); // FIXME
    } else {
      TDToast.makeBad( R.string.no_db );
    }
  }

  // public void notifyDisconnected()
  // {
  // }

  public void enableBluetoothButton( boolean enable ) { } 

}
