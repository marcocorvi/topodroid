/* @file PhotoActivity.java
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

import java.io.File;
// import java.io.IOException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
// import android.os.Handler;
// import android.os.Message;

// import android.app.Application;
import android.app.Activity;
// import android.app.Dialog;

// import android.content.Context;
// import android.content.Intent;
// import android.content.ActivityNotFoundException;

import android.view.View;
// import android.view.View.OnClickListener;
import android.view.KeyEvent;
// import android.view.Menu;
// import android.view.MenuItem;
// import android.view.SubMenu;
// import android.view.MenuInflater;
// import android.content.res.ColorStateList;

// import android.location.LocationManager;


// import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
// import android.widget.Toast;
// import android.widget.Button;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
// import android.preference.PreferenceManager;

// import android.provider.MediaStore;
// import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;

public class PhotoActivity extends Activity
                           implements OnItemClickListener, ILister
{
  private TopoDroidApp mApp;
  private DataHelper mApp_mData;

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private PhotoAdapter   mDataAdapter;
  private long mShotId = -1;   // id of the shot

  private String mSaveData = "";
  private TextView mSaveTextView = null;
  private PhotoInfo mSavePhoto = null;

  String mPhotoStation;
  String mPhotoComment;
  long   mPhotoId;

  // -------------------------------------------------------------------
  // ILister interface

  @Override
  public void refreshDisplay( int nr, boolean toast )
  {
    updateDisplay( );
  }

  @Override
  public void updateBlockList( CalibCBlock blk ) { }

  @Override
  public void updateBlockList( DBlock blk ) { }
  
  @Override
  public void updateBlockList( long blk_id ) { }
  
  @Override
  public void setConnectionStatus( int status ) { }

  @Override
  public void setRefAzimuth( float azimuth, long fixed_extend ) { }
    
  public void setTheTitle() { }

  // ----------------------------------------------------------------------

  private void updateDisplay( )
  {
    // TDLog.Log( TDLog.LOG_PHOTO, "updateDisplay() status: " + StatusName() + " forcing: " + force_update );
    if ( mApp_mData != null && mApp.mSID >= 0 ) {
      List< PhotoInfo > list = mApp_mData.selectAllPhotos( mApp.mSID, TDStatus.NORMAL );
      // TDLog.Log( TDLog.LOG_PHOTO, "update shot list size " + list.size() );
      updatePhotoList( list );
      setTitle( mApp.mySurvey );
    // } else {
    //   TDToast.make( this, R.string.no_survey );
    }
  }

  // -------------------------------------------------------------------

  private void updatePhotoList( List< PhotoInfo > list )
  {
    // TDLog.Log(TDLog.LOG_PHOTO, "updatePhotoList size " + list.size() );
    mDataAdapter.clear();
    mList.setAdapter( mDataAdapter );
    if ( list.size() == 0 ) {
      TDToast.make( this, R.string.no_photos );
      finish();
    }
    for ( PhotoInfo item : list ) {
      mDataAdapter.add( item );
    }
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int position, long id)
  {
    // CharSequence item = ((TextView) view).getText();
    // String value = item.toString();
    // TDLog.Log(  TDLog.LOG_INPUT, "PhotoDialog onItemClick() " + value );

    // if ( value.equals( getResources().getString( R.string.back_to_survey ) ) ) {
    //   updateDisplay( );
    //   return;
    // }
    // // setListPos( position  );
    startPhotoDialog( (TextView)view, position );
  }

  private void startPhotoDialog( TextView tv, int pos )
  {
     mSavePhoto = mDataAdapter.get(pos);
     String filename = TDPath.getSurveyJpgFile( mApp.mySurvey, Long.toString(mSavePhoto.id) );
     (new PhotoEditDialog( this, this, mApp, mSavePhoto, filename )).show();
  }


  // ---------------------------------------------------------------
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.main_photo);
    mApp = (TopoDroidApp) getApplication();
    mApp_mData = TopoDroidApp.mData;
    mDataAdapter = new PhotoAdapter( this, R.layout.row, new ArrayList< PhotoInfo >() );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    updateDisplay( );
  }

  // ------------------------------------------------------------------

  public void dropPhoto( PhotoInfo photo )
  {
    mApp_mData.deletePhoto( photo.sid, photo.id );
    TopoDroidUtil.deleteFile( TDPath.getSurveyJpgFile( mApp.mySurvey, Long.toString(photo.id) ) );
    updateDisplay( ); // FIXME
  }

  public void updatePhoto( PhotoInfo photo, String comment )
  {
    // TDLog.Log( TDLog.LOG_PHOTO, "updatePhoto comment " + comment );
    if ( mApp_mData.updatePhoto( photo.sid, photo.id, comment ) ) {
      // if ( mApp.mListRefresh ) {
      //   // This works but it refreshes the whole list
      //   mDataAdapter.notifyDataSetChanged();
      // } else {
      //   mSavePhoto.mComment = comment;
      // }
      updateDisplay( ); // FIXME
    } else {
      TDToast.make( this, R.string.no_db );
    }
  }

  // public void notifyDisconnected()
  // {
  // }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        String help_page = getResources().getString( R.string.PhotoActivity );
        if ( help_page != null ) UserManualActivity.showHelpPage( this, help_page );
        return true;
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
      // case KeyEvent.KEYCODE_SEARCH:
        // return onSearchRequested();
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  public void enableBluetoothButton( boolean enable ) { } 

}
