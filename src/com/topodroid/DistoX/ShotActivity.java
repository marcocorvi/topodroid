/* @file ShotActivity.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey shots management
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.FileReader;
// import java.io.FileWriter;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;

import java.util.Date;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.os.Parcelable;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Debug;

// import android.os.SystemClock;
// import android.os.PowerManager;
import android.content.res.Resources;

import android.graphics.Rect;

import android.app.Application;
import android.app.Activity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;
// import android.content.res.ColorStateList;

import android.provider.Settings.System;

// import android.location.LocationManager;

import android.content.Context;
import android.content.Intent;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;
import android.app.Dialog;
import android.widget.Button;

import android.view.WindowManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.preference.PreferenceManager;

import android.view.Menu;
import android.view.MenuItem;

import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.net.Uri;

import android.util.Log;

public class ShotActivity extends Activity
                          implements OnItemClickListener
                        , OnItemLongClickListener
                        , OnClickListener
                        , ILister
                        , INewPlot
{
  private static int izons[] = {
                        R.drawable.iz_download,
                        R.drawable.iz_bt,
                        R.drawable.iz_mode,
                        R.drawable.iz_plot,
                        R.drawable.iz_note,
                        R.drawable.iz_station,
                        R.drawable.iz_plus
                      };

  private static int izonsno[] = {
                        0,
                        0,
                        0,
                        R.drawable.iz_plot, // TODO_IZ
                        0,
                        0,
                        0
                      };

  private static int menus[] = {
                        R.string.menu_survey,
                        // R.string.menu_station,
                        R.string.menu_recover,
                        R.string.menu_photo,
                        R.string.menu_sensor,
                        R.string.menu_3d,
                        R.string.menu_distox,
                        R.string.menu_options,
                        R.string.menu_help
                     };

  private static int help_icons[] = {
                          R.string.help_download,
                          R.string.help_remote,
                          R.string.help_display,
                          R.string.help_plot,
                          R.string.help_note,
                          R.string.help_current_station, // R.string.help_splay,
                          // R.string.help_more,
                          // R.string.help_less,
                          R.string.help_add_shot,
                        };
   private static int help_menus[] = {
                          R.string.help_survey_info,
                          // R.string.help_current_station,
                          R.string.help_undelete,
                          R.string.help_photo,
                          R.string.help_sensor,
                          R.string.help_3d,
                          R.string.help_device,
                          R.string.help_prefs,
                          R.string.help_help
                      };

  private TopoDroidApp mApp;
  private DataDownloader mDataDownloader;

  private static final int SENSOR_ACTIVITY_REQUEST_CODE = 1;
  // private static final int EXTERNAL_ACTIVITY_REQUEST_CODE = 2;
  private static final int INFO_ACTIVITY_REQUEST_CODE = 3;
  static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

  // private static final int REQUEST_DEVICE = 1;
  private static final int REQUEST_ENABLE_BT = 2;

  boolean mSplay = true;  //!< whether to hide splay shots
  boolean mLeg   = true;  //!< whether to hide leg extra shots
  boolean mBlank = false; //!< whether to hide blank shots
  // private Bundle mSavedState = null;
  // long mSecondLastShotId = 0L;
  // long mLastShotId;

  long secondLastShotId() { return mApp.mSecondLastShotId; }

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private DistoXDBlockAdapter   mDataAdapter;
  ArrayList< String > mShowSplay;


  // private long mLastExtend; // id of the last-extend-ed splay 

  // private static final String LIST_STATE = "listState";
  // private int mFirstPos = -1;  
  // private int mScroll   = 0;
  // private int mSavePos  = -1;  // shot entry position
  private int mShotPos  = -1;  // shot entry position
  private int mPrevPos  = 0;   // prev shot entry position
  private int mNextPos  = 0;   // next shot entry position
  // private TextView mSaveTextView = null;

  private Button[] mButton1;
  private int mNrButton1 = 0;;

  static long   mSensorId;
  static long   mPhotoId;
  static String mComment;
  static long   mShotId;   // photo/sensor shot id

  // ConnHandler mHandler;

  TopoDroidApp getApp() { return mApp; }

  // -------------------------------------------------------------------
  // FXIME ok only for numbers
  // String getNextStationName()
  // {
  //   return mApp.mData.getNextStationName( mApp.mSID );
  // }

  // called by numberSplays
  private void tryExtendSplay( DistoXDBlock splay, float bearing, long extend, boolean flip )
  {
    if ( extend == 0 ) return;
    // double db = Math.cos( (bearing - splay.mBearing)*Math.PI/180 );
    // long ext = ( db > TopoDroidApp.mExtendThr )? extend : ( db < -TopoDroidApp.mExtendThr )? -extend : 0;
    if ( TopoDroidSetting.mSplayExtend ) { 
      double db = bearing - splay.mBearing;
      while ( db < -180 ) db += 360;
      while ( db > 180 ) db -= 360;
      db = Math.abs( db );
      long ext = ( db < 90-TopoDroidSetting.mExtendThr )? extend : ( db > 90+TopoDroidSetting.mExtendThr )? -extend : 0;
      if ( flip ) ext = -ext;
      splay.mExtend = ext;
    } else {
      splay.mExtend = 0;
    }
  }

  // private boolean extendSplays()
  // { 
  //   long sid = mApp.mSID;
  //   if ( sid < 0 ) {
  //     Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
  //     return false;
  //   } else {
  //     List<DistoXDBlock> list = mApp.mData.selectShotsAfterId( sid, mLastExtend, TopoDroidApp.STATUS_NORMAL );
  //     int size = list.size();
  //     String from = ""; // shot "from" station
  //     String to   = ""; // shot "to" station
  //     float bearing = 0.0f;    // shot bearing
  //     long extend   = 0L;
  //     int k;
  //     DistoXDBlock prev = null;
  //     for ( k=size - 1; k>=0; --k ) {
  //       DistoXDBlock item = list.get( k );
  //       int t = item.type();
  //       // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "shot " + k + " type " + t + " <" + item.mFrom + "> <" + item.mTo + ">" );
  //       if ( t == DistoXDBlock.BLOCK_MAIN_LEG ) {
  //         from    = item.mFrom;
  //         to      = item.mTo;  
  //         bearing = item.mBearing;
  //         extend  = item.mExtend;
  //       } else if ( t == DistoXDBlock.BLOCK_SPLAY ) {
  //         if ( from.equals( item.mFrom ) || to.equals( item.mFrom ) ) {
  //           tryExtendSplay( item, bearing, extend, to.equals( item.mFrom ) );
  //           mApp.mData.updateShotExtend( item.mId, mApp.mSID, ext );
  //           // FIXME NOTIFY
  //         }
  //       }
  //     }
  //     mLastExtend = mApp.mData.getLastShotId( sid );
  //   }
  //   return true;
  // }

  private void computeMeans( List<DistoXDBlock> list )
  {
    TopoDroidApp.mAccelerationMean = 0.0f;
    TopoDroidApp.mMagneticMean     = 0.0f;
    TopoDroidApp.mDipMean          = 0.0f;
    int size = list.size();
    if ( size > 0 ) {
      int cnt = 0;
      for ( DistoXDBlock blk : list ) {
        if ( blk.mAcceleration > 10.0 ) {
          TopoDroidApp.mAccelerationMean += blk.mAcceleration;
          TopoDroidApp.mMagneticMean     += blk.mMagnetic;
          TopoDroidApp.mDipMean          += blk.mDip;
          ++ cnt;
        }
      }
      if ( cnt > 0 ) {
        TopoDroidApp.mAccelerationMean /= cnt;
        TopoDroidApp.mMagneticMean     /= cnt;
        TopoDroidApp.mDipMean          /= cnt;
      }
      // Log.v( TopoDroidApp.TAG, "Acc " + TopoDroidApp.mAccelerationMean + " Mag " + TopoDroidApp.mMagneticMean 
      //                          + " Dip " + TopoDroidApp.mDipMean );
    }
  }

  // called after mBtnSplays and by SketchNewShotDialog
  ArrayList<DistoXDBlock> numberSplays()
  { 
    ArrayList<DistoXDBlock> updatelist = null;
    TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "numberSplays() ");
    long sid = mApp.mSID;
    if ( sid < 0 ) {
      // Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
      return null;
    } else {
      String prev_from = "";
      updatelist = new ArrayList<DistoXDBlock>();
      List<DistoXDBlock> list = mApp.mData.selectAllShots( sid, TopoDroidApp.STATUS_NORMAL );
      computeMeans( list );

      int size = list.size();
      int from = 0;    // index to start with to assign the from-station
      int k;
      // DistoXDBlock current_leg = null;
      for ( k=0; k<size; ++k ) {
        DistoXDBlock item = list.get( k );
        int t = item.type();
        // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "shot " + k + " type " + t + " <" + item.mFrom + "> <" + item.mTo + ">" );
        if ( t == DistoXDBlock.BLOCK_MAIN_LEG ) {
          // current_leg = item;
          if ( from == k ) { // on a main-leg: move "from" to the next shot
            prev_from = item.mFrom;
            from = k+1;
          } else if ( from < k ) { // on a main-leg and "from" is behind: set splays
            String name = (TopoDroidSetting.mShotAfterSplays) ? item.mFrom : prev_from;
            if ( name != null ) {
              // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "update splays from " + from + " to " + k + " with name: <" + name + ">" );
              // set the index of the last splay to extend at the smallest from 
              for ( ; from < k; ++from ) {
                DistoXDBlock splay = list.get( from );
                splay.setName( name, "" );
                tryExtendSplay( splay, item.mBearing, item.mExtend, false );
                updatelist.add( splay ); 
                // mLastExtend = item.mId;
              }
            }
          }
        } else if ( t == DistoXDBlock.BLOCK_SPLAY || t == DistoXDBlock.BLOCK_SEC_LEG ) {
          // on a splay / sec-leg: jump "from" to the next shot
          from = k+1;
        } else if ( DistoXDBlock.isTypeBlank( t ) && k > 1 ) {
          DistoXDBlock prev = list.get( k-1 );
          if ( item.relativeDistance( prev ) < TopoDroidSetting.mCloseDistance ) {
            item.mType = DistoXDBlock.BLOCK_SEC_LEG;
            // current_leg.setTypeBlankLeg();
            updatelist.add( item ); 
            from = k+1;
          }
        }
      }

      // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "numberSplays() updatelist size " + updatelist.size() );
      if ( updatelist.size() > 0 ) {
        mApp.mData.updateShotNameAndExtend( sid, updatelist );
        // FIXME NOTIFY
      }
    }
    return updatelist;
  }

  @Override
  public void refreshDisplay( int nr, boolean toast ) 
  {
    // mDataDownloader.setConnectionStatus( false );
    setConnectionStatus( false );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        // mLastShotId = mApp.mData.getLastShotId( mApp.mSID );
        updateDisplay( );
      }
      if ( toast ) {
        Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_SHORT ).show();
      }
    } else if ( nr < 0 ) {
      if ( toast ) {
        // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_SHORT ).show();
        Toast.makeText( this, mApp.DistoXConnectionError[ -nr ], Toast.LENGTH_SHORT ).show();
      }
    }
  }
    
  public void updateDisplay( )
  {
    // Log.v( TopoDroidApp.TAG, "updateDisplay() " );

    DataHelper data = mApp.mData;
    if ( data != null && mApp.mSID >= 0 ) {
      List<DistoXDBlock> list = data.selectAllShots( mApp.mSID, TopoDroidApp.STATUS_NORMAL );
      if ( list.size() > 4 ) computeMeans( list );

      List< PhotoInfo > photos = data.selectAllPhotos( mApp.mSID, TopoDroidApp.STATUS_NORMAL );
      // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "updateDisplay() shot list size " + list.size() );
      // Log.v( TopoDroidApp.TAG, "updateDisplay() shot list size " + list.size() );
      updateShotList( list, photos );
      
      setTheTitle( );
    } else {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
    }
  }

  void setTheTitle()
  {
    setTitle( mApp.getConnectionStateTitleStr() + mApp.mySurvey );
    // FIXME setTitleColor( TopoDroidConst.COLOR_NORMAL );
  }

  boolean isCurrentStationName( String st ) { return mApp.isCurrentStationName( st ); }
  void setCurrentStationName( String st ) 
  { 
    mSkipItemClick = true;
    mApp.setCurrentStationName( st ); 
    updateDisplay( );
  }

  // add a block to the adapter
  @Override
  public void updateBlockList( DistoXDBlock blk )
  {
    // Log.v( "DistoX", "updateDisplay blk " + blk.mLength + " " + blk.mBearing + " " + blk.mClino );
    if ( mDataAdapter != null ) {
      mDataAdapter.addBlock( blk );
      mApp.assignStations( mDataAdapter.mItems );

      Message msg = Message.obtain();
      msg.what = MSG_ADD_BLK;
      mListItemsHandler.sendMessage( msg );
    }
  }

  void setShowIds( boolean show ) { mDataAdapter.show_ids = show; }

  boolean getShowIds() { return mDataAdapter.show_ids; }

  private void updateShotList( List<DistoXDBlock> list, List< PhotoInfo > photos )
  {
    TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "updateShotList shots " + list.size() + " photos " + photos.size() );
    mDataAdapter.clear();
    mList.setAdapter( mDataAdapter );
    if ( list.size() == 0 ) {
      // Toast.makeText( this, R.string.no_shots, Toast.LENGTH_SHORT ).show();
      return;
    }
    processShotList( list );
    mDataAdapter.reviseBlockWithPhotos( photos );
  }

  private void processShotList( List<DistoXDBlock> list )
  {
    DistoXDBlock prev = null;
    boolean prev_is_leg = false;
    for ( DistoXDBlock item : list ) {
      DistoXDBlock cur = item;
      int t = cur.type();

      // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "item " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );

      if ( cur.mType == DistoXDBlock.BLOCK_SEC_LEG || cur.relativeDistance( prev ) < TopoDroidSetting.mCloseDistance ) {

        if ( cur.mType == DistoXDBlock.BLOCK_BLANK ) {   // FIXME 20140612
          cur.mType = DistoXDBlock.BLOCK_SEC_LEG;
          mApp.mData.updateShotLeg( cur.mId, mApp.mSID, 1L, true ); // cur.mType ); // FIXME 20140616
        }

        // if ( prev != null && prev.mType == DistoXDBlock.BLOCK_BLANK ) prev.mType = DistoXDBlock.BLOCK_BLANK_LEG;
        if ( prev != null ) prev.setTypeBlankLeg();

        if ( mLeg ) { // flag: hide leg extra shots
          // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "close distance");

          if ( mBlank && prev != null && prev.isTypeBlank() ) {
            // prev was skipped: draw it now
            if ( ! prev_is_leg ) {
              cur = prev;
              prev_is_leg = true;
            } else {
              continue;
            }
          } else {
            continue;
          }
        } else { // do not hide extra leg-shots
          if ( mBlank && prev != null && prev.isTypeBlank() ) {
            if ( ! prev_is_leg ) {
              mDataAdapter.add( prev );
              prev_is_leg = true;
            } else {
              /* nothing */
            }
          } else {
            /* nothing */
          }
        }
      } else {
        // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "not close distance");
        prev_is_leg = false;
        if ( DistoXDBlock.isTypeBlank(t) ) {
          prev = cur;
          if ( mBlank ) continue;
        } else if ( t == DistoXDBlock.BLOCK_SPLAY ) {
          prev = null;
          if ( mSplay ) { // do hide splays, except those that are shown.
            boolean skip = true;
            for ( String st : mShowSplay ) {
              if ( st.equals( cur.mFrom ) ) { skip = false; break; }
            }
            if ( skip ) continue;
          }
        } else { // t == DistoXDBlock.BLOCK_MAIN_LEG
          prev = cur;
        }
      }
      // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "adapter add " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      mDataAdapter.add( cur );
    }
  }

  // ---------------------------------------------------------------
  // list items click

  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( onMenu ) {
      closeMenu();
      return true;
    }
    TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "ShotActivity onItemLongClick id " + id);
    DistoXDBlock blk = mDataAdapter.get(pos);
    mShotId = blk.mId;
    (new PhotoSensorsDialog(this, this, blk ) ).show();
    return true;
  }

  private boolean mSkipItemClick = false;

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mSkipItemClick ) {
      mSkipItemClick = false;
      return;
    }
    if ( mMenu == (ListView)parent ) {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) { // SURVEY ACTIVITY
        Intent intent = new Intent( this, SurveyActivity.class );
        intent.putExtra( TopoDroidApp.TOPODROID_SURVEY,  0 ); // mustOpen 
        intent.putExtra( TopoDroidApp.TOPODROID_OLDSID, -1 ); // old_sid 
        intent.putExtra( TopoDroidApp.TOPODROID_OLDID,  -1 ); // old_id 
        startActivityForResult( intent, INFO_ACTIVITY_REQUEST_CODE );
      // } else if ( TopoDroidSetting.mLevelOverBasic && p++ == pos ) { // CURRENT STATION
      //   (new CurrentStationDialog( this, this, mApp )).show();

      } else if ( TopoDroidSetting.mLevelOverBasic && p++ == pos ) { // RECOVER
        (new DistoXUndelete(this, this, mApp.mData, mApp.mSID ) ).show();
        updateDisplay( );
      } else if ( TopoDroidSetting.mLevelOverNormal && p++ == pos ) { // PHOTO
        startActivity( new Intent( this, PhotoActivity.class ) );
      } else if ( TopoDroidSetting.mLevelOverNormal && p++ == pos ) { // SENSORS
        startActivity( new Intent( this, SensorListActivity.class ) );
      } else if ( TopoDroidSetting.mLevelOverBasic && p++ == pos ) { // 3D
        mApp.exportSurveyAsTh(); // make sure to have survey exported as therion
        try {
          Intent intent = new Intent( "Cave3D.intent.action.Launch" );
          intent.putExtra( "survey", TopoDroidPath.getSurveyThFile( mApp.mySurvey ) );
          startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          Toast.makeText( this, R.string.no_cave3d, Toast.LENGTH_SHORT ).show();
        }
      } else if ( TopoDroidSetting.mLevelOverNormal && p++ == pos ) { // DEVICE
        if ( mApp.mBTAdapter.isEnabled() ) {
          startActivity( new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class ) );
        }
      } else  if ( p++ == pos ) { // OPTIONS
        Intent intent = new Intent( this, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
        startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        // int nn = mNrButton1; //  + ( TopoDroidApp.mLevelOverNormal ?  mNrButton2 : 0 );
        (new HelpDialog(this, izons, menus, help_icons, help_menus, mNrButton1, 8 ) ).show();
      }
      return;
    }
    if ( onMenu ) {
      closeMenu();
      return;
    }

    TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "ShotActivity onItemClick id " + id);
    DistoXDBlock blk = mDataAdapter.get(pos);

    mShotPos = pos;
    // mSavePos = pos;
    // mFirstPos = mList.getFirstVisiblePosition();
    // mScroll   = mList.getScrollY();
    // mSaveTextView = (TextView)view;

    // TextView tv = (TextView)view;
    // String msg = tv.getText().toString();
    // String[] st = msg.split( " ", 6 );
    // String data = st[2] + " " + st[3] + " " + st[4];
      
    DistoXDBlock prevBlock = null;
    DistoXDBlock nextBlock = null;
    // if ( blk.type() == DistoXDBlock.BLOCK_BLANK ) {
      // prevBlock = mApp.mData.selectPreviousLegShot( blk.mId, mApp.mSID );
      prevBlock = getPreviousLegShot( blk, false );
      nextBlock = getNextLegShot( blk, false );
      // if ( prevBlock != null ) {
      //   TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "prev leg " + prevBlock.mFrom + " " + prevBlock.mTo );
      // }
    // }
    (new ShotDialog( this, this, pos, blk, prevBlock, nextBlock )).show();
  }

// ---------------------------------------------------------------

  void askPhotoComment( )
  {
    (new PhotoCommentDialog(this, this) ).show();
  }


  void doTakePhoto( String comment )
  {
    mComment = comment;
    mPhotoId      = mApp.mData.nextPhotoId( mApp.mSID );


    // imageFile := PHOTO_DIR / surveyId / photoId .jpg
    File imagefile = new File( TopoDroidPath.getSurveyJpgFile( mApp.mySurvey, Long.toString(mPhotoId) ) );
    // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "photo " + imagefile.toString() );
    try {
      Uri outfileuri = Uri.fromFile( imagefile );
      Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
      intent.putExtra( MediaStore.EXTRA_OUTPUT, outfileuri );
      intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
      startActivityForResult( intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE );
    } catch ( ActivityNotFoundException e ) {
      Toast.makeText( this, "No image capture mApp", Toast.LENGTH_SHORT ).show();
    }
  }

  void askSensor( )
  {
    mSensorId = mApp.mData.nextSensorId( mApp.mSID );
    TopoDroidLog.Log( TopoDroidLog.LOG_SENSOR, "sensor " + mSensorId );
    Intent intent = new Intent( this, SensorActivity.class );
    startActivityForResult( intent, SENSOR_ACTIVITY_REQUEST_CODE );
  }

  // void askExternal( )
  // {
  //   mSensorId = mApp.mData.nextSensorId( mApp.mSID );
  //   TopoDroidLog.Log( TopoDroidLog.LOG_SENSOR, "sensor " + mSensorId );
  //   Intent intent = new Intent( this, ExternalActivity.class );
  //   startActivityForResult( intent, EXTERNAL_ACTIVITY_REQUEST_CODE );
  // }

  void askShot( )
  {
    // mSecondLastShotId = mApp.lastShotId( );
    DistoXDBlock last_blk = null; // mApp.mData.selectLastLegShot( mApp.mSID );
    (new ShotNewDialog( this, mApp, this, last_blk, mShotId )).show();
  }

  // called by PhotoSensorDialog to split the survey
  //
  void askSurvey( )
  {
    new TopoDroidAlertDialog( this, getResources(),
                      getResources().getString( R.string.survey_split ),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doSplitSurvey();
        }
    } );
  }

  void doSplitSurvey()
  {
    long old_sid = mApp.mSID;
    long old_id  = mShotId;
    // Log.v( TopoDroidApp.TAG, "askSurvey " + old_sid + " " + old_id );
    if ( mApp.mShotActivity != null ) {
      mApp.mShotActivity.finish();
      mApp.mShotActivity = null;
    }
    if ( mApp.mSurveyActivity != null ) {
      mApp.mSurveyActivity.finish();
      mApp.mSurveyActivity = null;
    }
    mApp.mActivity.startSplitSurvey( old_sid, old_id ); // SPLIT SURVEY
  }

  void askDelete( )
  {
    mApp.mData.deleteShot( mShotId, mApp.mSID, true );
    updateDisplay( ); 
  }

  void insertPhoto( )
  {
    // long shotid = 0;
    SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
    mApp.mData.insertPhoto( mApp.mSID, mPhotoId, mShotId, "", sdf.format( new Date() ), mComment ); // FIXME TITLE has to go
    // FIXME NOTIFY ? no
  }

  // void deletePhoto( PhotoInfo photo ) 
  // {
  //   mApp.mData.deletePhoto( mApp.mSID, photo.id );
  //   File imagefile = new File( mApp.getSurveyJpgFile( Long.toString(photo.id) ) );
  //   try {
  //     imagefile.delete();
  //   } catch ( IOException e ) { }
  // }

  @Override
  protected void onActivityResult( int reqCode, int resCode, Intent data )
  {
    switch ( reqCode ) {
      case CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE:
        if ( resCode == Activity.RESULT_OK ) {
          // (new PhotoCommentDialog(this, this) ).show();
          insertPhoto();
        } else {
          // mApp.mData.deletePhoto( mApp.mSID, mPhotoId );
        }
        break;
      case SENSOR_ACTIVITY_REQUEST_CODE:
      // case EXTERNAL_ACTIVITY_REQUEST_CODE:
        if ( resCode == Activity.RESULT_OK ) {
          Bundle extras = data.getExtras();
          String type  = extras.getString( TopoDroidApp.TOPODROID_SENSOR_TYPE );
          String value = extras.getString( TopoDroidApp.TOPODROID_SENSOR_VALUE );
          String comment = extras.getString( TopoDroidApp.TOPODROID_SENSOR_COMMENT );
          TopoDroidLog.Log( TopoDroidLog.LOG_SENSOR, "insert sensor " + type + " " + value + " " + comment );

          SimpleDateFormat sdf = new SimpleDateFormat( "yyyy.MM.dd", Locale.US );
          mApp.mData.insertSensor( mApp.mSID, mSensorId, mShotId, "", 
                                  sdf.format( new Date() ),
                                  comment,
                                  type,
                                  value );
          // FIXME NOTIFY ? no
        }
        break;
      case INFO_ACTIVITY_REQUEST_CODE:
        if ( resCode == Activity.RESULT_OK ) {
          finish();
        }
        break;
    }
  }

  // ---------------------------------------------------------------
  // private Button mButtonHelp;
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  HorizontalButtonView mButtonView2;
  ListView   mMenu;
  Button     mImage;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;

  BitmapDrawable mBMdownload;
  BitmapDrawable mBMdownload_on;
  // BitmapDrawable mBMadd;
  BitmapDrawable mBMplot;
  BitmapDrawable mBMplot_no;

  private Handler mListItemsHandler;
  static final int MSG_ADD_BLK = 1;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView(R.layout.shot_activity);
    mApp = (TopoDroidApp) getApplication();
    mApp.mShotActivity = this; // FIXME
    mDataDownloader = mApp.mDataDownloader; // new DataDownloader( this, mApp );

    mShowSplay   = new ArrayList< String >();
    mDataAdapter = new DistoXDBlockAdapter( this, this, R.layout.row, new ArrayList<DistoXDBlock>() );

    mListItemsHandler = new Handler() {
      @Override
      public void handleMessage( Message msg ) {
        switch (msg.what) {
        case MSG_ADD_BLK:
          mDataAdapter.notifyDataSetChanged();
          break;
        }
        super.handleMessage( msg );
      }
    };

    mListView = (HorizontalListView) findViewById(R.id.listview);
    int size = mApp.setListViewHeight( mListView );
    // icons00   = ( TopoDroidSetting.mSizeButtons == 2 )? ixons : icons;
    // icons00no = ( TopoDroidSetting.mSizeButtons == 2 )? ixonsno : iconsno;

    mNrButton1 = TopoDroidSetting.mLevelOverBasic ? 7 : 5;
    // mNrButton2 = 7;
    mButton1 = new Button[ mNrButton1 ];
    // mButton2 = new Button[ mNrButton2 ];
    int k;
    for ( k=0; k<mNrButton1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      // mButton1[k].setBackgroundResource(  icons00[k] );

      BitmapDrawable bm2 = mApp.setButtonBackground( mButton1[k], size, izons[k] );
      if ( k == 0 ) mBMdownload = bm2;
      if ( k == 3 ) mBMplot = bm2;
      // if ( k == 6 ) mBMadd  = bm2;
    }
    mBMplot_no = mApp.setButtonBackground( null, size, R.drawable.iz_plot_no );
    mBMdownload_on = mApp.setButtonBackground( null, size, R.drawable.iz_download_on );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mListView.setAdapter( mButtonView1.mAdapter );

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setLongClickable( true );
    if ( TopoDroidSetting.mLevelOverNormal ) {
      mList.setOnItemLongClickListener( this );
    }
    mList.setDividerHeight( 2 );
    // mList.setSmoothScrollbarEnabled( true );

    // restoreInstanceFromData();
    // mLastExtend = mApp.mData.getLastShotId( mApp.mSID );
    List<DistoXDBlock> list = mApp.mData.selectAllShots( mApp.mSID, TopoDroidApp.STATUS_NORMAL );
    // mSecondLastShotId = mApp.lastShotId( );

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );

    // mImage.setBackgroundResource( 
    //   ( TopoDroidSetting.mSizeButtons == 2 )? R.drawable.ix_menu : R.drawable.ic_menu );
    mApp.setButtonBackground( mImage, size, R.drawable.iz_menu);

    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter();
    closeMenu();
    mMenu.setOnItemClickListener( this );

    // updateDisplay( );

  }

  void enableSketchButton( boolean enabled )
  {
    mApp.mEnableZip = enabled;
    mButton1[3].setEnabled( enabled ); // FIXME SKETCH BUTTON 
    mButton1[3].setBackgroundDrawable( enabled ? mBMplot : mBMplot_no );
  }

  @Override
  public synchronized void onStart() 
  {
    super.onStart();
    // Debug.startMethodTracing( "distox" );
    // Log.v( "DistoX", "Shot Activity onStart() " );
    if ( mDataDownloader != null ) {
      mDataDownloader.registerLister( this );
    }
  }

  @Override
  public synchronized void onStop() 
  {
    // Debug.stopMethodTracing( );
    super.onStop();
    // Log.v( "DistoX", "Shot Activity onStop() " + ((mDataDownloader!=null)? "with DataDownloader":"") );
    if ( mDataDownloader != null ) {
      mDataDownloader.unregisterLister( this );
      mDataDownloader.onPause();
    }
    mApp.disconnectRemoteDevice( false );
  }

  @Override
  public synchronized void onPause() 
  {
    super.onPause();
    // Log.v( "DistoX", "Shot Activity onPause() " + ((mDataDownloader!=null)? "with DataDownloader":"") );
    saveInstanceToData();

    // mApp.unregisterConnListener( mHandler );
    // if ( mApp.mComm != null ) { mApp.mComm.suspend(); }
    // FIXME NOTIFY unregister ILister
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();

    // FIXME NOTIFY register ILister
    // if ( mApp.mComm != null ) { mApp.mComm.resume(); }
    // Log.v( "DistoX", "Shot Activity onResume()" );
    
    restoreInstanceFromData();
    updateDisplay( );

    if ( mDataDownloader != null ) mDataDownloader.onResume();

    // mApp.registerConnListener( mHandler );
    setConnectionStatus( mDataDownloader.mConnected );
  }

  // @Override
  // public synchronized void onDestroy() 
  // {
  //   super.onDestroy();
  //   saveInstanceToData();
  // }

  // FIXME NOTIFY: the display mode is local - do not notify
  private void restoreInstanceFromData()
  { 
    String shots = mApp.mData.getValue( "DISTOX_SHOTS" );
    if ( shots != null ) {
      String[] vals = shots.split( " " );
      mSplay  = vals[0].equals("1");
      mLeg    = vals[1].equals("1");
      mBlank  = vals[2].equals("1");
      setShowIds( vals[3].equals("1") );
      // Log.v("DistoX", "restore from data mSplay " + mSplay );
    }
  }
    
  private void saveInstanceToData()
  {
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter( sw );
    pw.format("%d %d %d %d", mSplay?1:0, mLeg?1:0, mBlank?1:0, getShowIds()?1:0 );
    mApp.mData.setValue( "DISTOX_SHOTS", sw.getBuffer().toString() );
    // Log.v("DistoX", "save to data mSplay " + mSplay );
  }


  public void onClick(View view)
  {
    if ( onMenu ) {
      closeMenu();
      return;
    }
    Button b = (Button)view;
    if ( b == mImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      return;
    }

    if ( b != null ) {
      Intent intent;

      int k1 = 0;
      // int k2 = 0;
      if ( k1 < mNrButton1 && b == mButton1[k1++] ) {        // mBtnDownload
        setConnectionStatus( ! mDataDownloader.mConnected );  // immediate button feedback ? FIXME
        mDataDownloader.downloadData( );
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // mBtnReset
        mDataDownloader.disconnect();
        switch ( mApp.distoType() ) {
          case Device.DISTO_A3:
            mApp.resetComm();
            Toast.makeText(this, R.string.bt_reset, Toast.LENGTH_SHORT).show();
            break;
          case Device.DISTO_X310:
            (new DeviceRemote( this, this, mApp )).show();
            break;
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // mBtnDisplay 
        new ShotDisplayDialog( this, this ).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // mBtnSketch
        new PlotListDialog( this, this, mApp ).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // mBtnNote
        if ( mApp.mySurvey != null ) {
          (new DistoXAnnotations( this, mApp.mySurvey )).show();
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // mBtnSplays mBtnStations
        // ArrayList<DistoXDBlock> list = numberSplays(); // SPLAYS
        // if ( list != null && list.size() > 0 ) {
        //   updateDisplay( );
        // }

        // STATIONS
        (new CurrentStationDialog( this, this, mApp )).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // mBtnAdd was mBtnMore
        // mSecondLastShotId = mApp.lastShotId( );
        DistoXDBlock last_blk = mApp.mData.selectLastLegShot( mApp.mSID );
        // Log.v( "DistoX", "last blk: " + last_blk.toString() );
        (new ShotNewDialog( this, mApp, this, last_blk, -1L )).show();

      //   mListView.setAdapter( mButtonView2.mAdapter );
      //   mListView.invalidate();
      //
      // 
      // } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // mBtnLess
      //   mListView.setAdapter( mButtonView1.mAdapter );
      //   mListView.invalidate();
      // } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // mBtnDevice
      //   if ( mApp.mBTAdapter.isEnabled() ) {
      //     intent = new Intent( Intent.ACTION_EDIT ).setClass( this, DeviceActivity.class );
      //     startActivity( intent );
      //   }
      // } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // mBtnAdd 
      //   // mSecondLastShotId = mApp.lastShotId( );
      //   DistoXDBlock last_blk = mApp.mData.selectLastLegShot( mApp.mSID );
      //   // Log.v( "DistoX", "last blk: " + last_blk.toString() );
      //   (new ShotNewDialog( this, mApp, this, last_blk, -1L )).show();
      // // } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // mBtnInfo
      // //   intent = new Intent( this, SurveyActivity.class );
      // //   intent.putExtra( TopoDroidApp.TOPODROID_SURVEY,  0 ); // mustOpen 
      // //   intent.putExtra( TopoDroidApp.TOPODROID_OLDSID, -1 ); // old_sid 
      // //   intent.putExtra( TopoDroidApp.TOPODROID_OLDID,  -1 ); // old_id 
      // //   startActivityForResult( intent, INFO_ACTIVITY_REQUEST_CODE );

      // } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // mBtnUndelete
      //   (new DistoXUndelete(this, this, mApp.mData, mApp.mSID ) ).show();
      //   updateDisplay( );
      // } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // mBtnCamera
      //   startActivity( new Intent( this, PhotoActivity.class ) );
      // } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // mBtnSensor
      //   startActivity( new Intent( this, SensorListActivity.class ) );
      // } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // mBtn3D
      //   mApp.exportSurveyAsTh(); // make sure to have survey exported as therion
      //   try {
      //     intent = new Intent( "Cave3D.intent.action.Launch" );
      //     intent.putExtra( "survey", mApp.getSurveyThFile() );
      //     startActivity( intent );
      //   } catch ( ActivityNotFoundException e ) {
      //     Toast.makeText( this, R.string.no_cave3d, Toast.LENGTH_SHORT ).show();
      //   }
      }
    }
  }

  // ------------------------------------------------------------------

  public boolean hasSurveyPlot( String name )
  {
    return mApp.mData.hasSurveyPlot( mApp.mSID, name+"p" );
  }
 
  public boolean hasSurveyStation( String start )
  {
    return mApp.mData.hasSurveyStation( mApp.mSID, start );
  }

  public void makeNewPlot( String name, String start )
  {
    // plot-id -1, status 0, azimuth 0.0f
    // long mPIDp = mApp.mData.insertPlot( mApp.mSID, -1L, name+"p",
    //              PlotInfo.PLOT_PLAN, 0L, start, "", 0, 0, TopoDroidApp.mScaleFactor, 0.0f );
    // long mPIDs = mApp.mData.insertPlot( mApp.mSID, -1L, name+"s",
    //              PlotInfo.PLOT_EXTENDED, 0L, start, "", 0, 0, TopoDroidApp.mScaleFactor, 0.0f );
    long mPIDp = mApp.insert2dPlot( mApp.mSID, name, start );

    if ( mPIDp >= 0 ) {
      long mPIDs = mPIDp + 1L; // FIXME !!! this is true but not guaranteed
      startDrawingActivity( start, name+"p", mPIDp, name+"s", mPIDs, PlotInfo.PLOT_PLAN );
    // } else {
    //   Toast.makeText( this, R.string.plot_duplicate_name, Toast.LENGTH_LONG).show();
    }
    // updateDisplay( );
  }

  // FIXME_SKETCH_3D
  public void makeNewSketch3d( String name, String st1, String st2 )
  {
    // FIXME xoffset yoffset, east south and vert (downwards)
    if ( st2 != null ) {
      if ( ! mApp.mData.hasShot( mApp.mSID, st1, st2 ) ) {
        Toast.makeText( this, R.string.no_shot_between_stations, Toast.LENGTH_SHORT).show();
        return;
      }
    } else {
      st2 = mApp.mData.nextStation( mApp.mSID, st1 );
    }
    if ( st2 != null ) {
      float e = 0.0f; // NOTE (e,s,v) are the coord of station st1, and st1 is taken as the origin of the ref-frame
      float s = 0.0f;
      float v = 0.0f;
      long mPID = mApp.mData.insertSketch3d( mApp.mSID, -1L, name, 0L, st1, st1, st2,
                                            0, // mApp.mDisplayWidth/(2*TopoDroidApp.mScaleFactor),
                                            0, // mApp.mDisplayHeight/(2*TopoDroidApp.mScaleFactor),
                                            10 * TopoDroidApp.mScaleFactor,
                                            0, 0, 10 * TopoDroidApp.mScaleFactor,
                                            0, 0, 10 * TopoDroidApp.mScaleFactor,
                                            e, s, v, 180, 0 );
      if ( mPID >= 0 ) {
        startSketchActivity( name );
      }
    } else {
      Toast.makeText( this, "no to station", Toast.LENGTH_SHORT).show();
    }
  }
  // END_SKETCH_3D

  // public void startPlotDialog( String name, String type ) // name = plot/sketch3d name
  // {
  //   // FIXME SKETCH-3D
  //     PlotInfo plot1 =  mApp.mData.getPlotInfo( mApp.mSID, name+"p" );
  //     if ( plot1 != null ) {
  //       PlotInfo plot2 = mApp.mData.getPlotInfo( mApp.mSID, name+"s" );
  //       ( new PlotDialog( this, this, plot1, plot2 )).show();
  //       return;
  //     }
  //   Toast.makeText( this, R.string.plot_not_found, Toast.LENGTH_SHORT).show();
  // }

  public void startExistingPlot( String name, long type ) // name = plot/sketch3d name
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "startExistingPlot \"" + name + "\" type " + type + " sid " + mApp.mSID );

    // FIXME_SKETCH_3D
    if ( type == PlotInfo.PLOT_SKETCH_3D ) {
      Sketch3dInfo sketch = mApp.mData.getSketch3dInfo( mApp.mSID, name );
      if ( sketch != null ) {
        startSketchActivity( sketch.name );
        return;
      }
    } else {
    // END_SKETCH_3D
      PlotInfo plot1 =  mApp.mData.getPlotInfo( mApp.mSID, name+"p" );
      if ( plot1 != null ) {
        PlotInfo plot2 =  mApp.mData.getPlotInfo( mApp.mSID, name+"s" );
        startDrawingActivity( plot1.start, plot1.name, plot1.id, plot2.name, plot2.id, type );
        return;
      }
    }
    Toast.makeText( this, R.string.plot_not_found, Toast.LENGTH_SHORT).show();
  }
 
  // FIXME_SKETCH_3D
  private void startSketchActivity( String name )
  {
    if ( mApp.mSID < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
      return;
    }

    // notice when starting the SketchActivity the remote device is disconnected 
    // FIXME mApp.disconnectRemoteDevice();

    // TODO
    Intent sketchIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, SketchActivity.class );
    sketchIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_ID, mApp.mSID );
    sketchIntent.putExtra( TopoDroidApp.TOPODROID_SKETCH_NAME, name );
    startActivity( sketchIntent );
  }
  // END_SKETCH_3D

  private void startDrawingActivity( String start, String plot1_name, long plot1_id,
                                                   String plot2_name, long plot2_id, long type )
  {
    if ( mApp.mSID < 0 || plot1_id < 0 || plot2_id < 0 ) {
      Toast.makeText( this, R.string.no_survey, Toast.LENGTH_SHORT ).show();
      return;
    }
    
    // notice when starting the DrawingActivity the remote device is disconnected 
    // FIXME mApp.disconnectRemoteDevice();

    Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_SURVEY_ID, mApp.mSID );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_NAME, plot1_name );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_NAME2, plot2_name );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_TYPE, type );
    drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_FROM, start );
    // drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_ID, plot1_id ); // not necessary
    // drawIntent.putExtra( TopoDroidApp.TOPODROID_PLOT_ID2, plot2_id ); // not necessary

    startActivity( drawIntent );
  }

  // ---------------------------------------------------------------------------------

  // public void dropShot( DistoXDBlock blk )
  // {
  //   mApp.mData.deleteShot( blk.mId, mApp.mSID );
  //   updateDisplay( ); // FIXME
  // }

  public DistoXDBlock getNextBlankLegShot( DistoXDBlock blk )
  {
    DistoXDBlock ret = null;
    long id = 0;
    for ( int k=0; k<mDataAdapter.size(); ++k ) {
      DistoXDBlock b = mDataAdapter.get(k);
      if ( b.isTypeBlank() ) {
        id = b.mId - 1;
        break;
      }
    }
    List<DistoXDBlock> list = mApp.mData.selectShotsAfterId( mApp.mSID, id , 0 );
    for ( DistoXDBlock b : list ) {
      if ( b.isTypeBlank() ) {
        // Log.v( TopoDroidApp.TAG, "BLANK " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null ) {
          if ( ret.relativeDistance( b ) < TopoDroidSetting.mCloseDistance ) return ret;
        }
        ret = b;
      } else if ( b.mType == DistoXDBlock.BLOCK_SEC_LEG ) {
        // Log.v( TopoDroidApp.TAG, "LEG " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null ) {
          if ( ret.relativeDistance( b ) < TopoDroidSetting.mCloseDistance ) return ret;
        }
      } else {
        // Log.v( TopoDroidApp.TAG, "OTHER " + b.mLength + " " + b.mBearing + " " + b.mClino );
        ret = null;
      }
    }
    return null;
  }

  // get the next centerline shot and set mNextPos index
  public DistoXDBlock getNextLegShot( DistoXDBlock blk, boolean move_down )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "getNextLegShot: pos " + mShotPos );
    if ( blk == null ) {
      // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "   block is null");
      return null;
    }
    if ( move_down ) {
      mPrevPos = mShotPos;
      mShotPos = mNextPos;
      mNextPos = mPrevPos; // the old mShotPos;
    } else {
      mNextPos = mShotPos;
    }
    while ( mNextPos < mDataAdapter.size() && blk != mDataAdapter.get(mNextPos) ) ++ mNextPos;
    ++ mNextPos; // one position after blk
    while ( mNextPos < mDataAdapter.size() ) {
      DistoXDBlock b = mDataAdapter.get(mNextPos);
      int t = b.type();
      if ( t == DistoXDBlock.BLOCK_MAIN_LEG ) {
        return b;
      } else if (    DistoXDBlock.isTypeBlank( t )
                  && mNextPos+1 < mDataAdapter.size()
                  && b.relativeDistance( mDataAdapter.get(mNextPos+1) ) < TopoDroidSetting.mCloseDistance ) {
        return b;
      }
      ++ mNextPos;
    }
    return null;
  }

  // get the previous centerline shot and set the mPrevPos index
  public DistoXDBlock getPreviousLegShot( DistoXDBlock blk, boolean move_up )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "getPreviousLegShot: pos " + mShotPos );
    if ( blk == null ) return null;
    if ( move_up ) {
      mNextPos = mShotPos;
      mShotPos = mPrevPos;
      mPrevPos = mNextPos; // the old mShotPos;
    } else {
      mPrevPos = mShotPos;
    }
    while ( mPrevPos >= 0 && blk != mDataAdapter.get(mPrevPos) ) -- mPrevPos;
    while ( mPrevPos > 0 ) {
      -- mPrevPos;
      DistoXDBlock b = mDataAdapter.get(mPrevPos);
      if ( b.type() == DistoXDBlock.BLOCK_MAIN_LEG ) {
        return b;
      }
    }
    return null;
  }

  void updateShotDistanceBearingClino( float d, float b, float c, DistoXDBlock blk )
  {
    // Log.v("DistoX", "update shot DBC length " + d );
    mApp.mData.updateShotDistanceBearingClino( blk.mId, mApp.mSID, d, b, c, true );
    blk.mLength  = d;
    blk.mBearing = b;
    blk.mClino   = c;
    mDataAdapter.updateBlockView( blk );
  }

  void updateShot( String from, String to, long extend, long flag, boolean leg, String comment, DistoXDBlock blk )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_SHOT, "updateShot From >" + from + "< To >" + to + "< comment " + comment );
    blk.setName( from, to );

    int ret = mApp.mData.updateShot( blk.mId, mApp.mSID, from, to, extend, flag, leg?1:0, comment, true );

    if ( ret == -1 ) {
      Toast.makeText( this, R.string.no_db, Toast.LENGTH_SHORT ).show();
    // } else if ( ret == -2 ) {
    //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_SHORT ).show();
    } else {
      // update same shots of the given block
      List< DistoXDBlock > blk_list = mApp.mData.selectShotsAfterId( blk.mId, mApp.mSID, 0L );
      for ( DistoXDBlock blk1 : blk_list ) {
        if ( blk1.relativeDistance( blk ) > TopoDroidSetting.mCloseDistance ) break;
        mApp.mData.updateShotLeg( blk1.mId, mApp.mSID, 1L, true );
      }
    }
    mDataAdapter.updateBlockView( blk );
  }

  
  // this method is called by ShotDialog() with to.length() == 0 ie to == ""
  // and blk splay shot
  public void updateSplayShots( String from, String to, long extend, long flag, boolean leg, String comment, DistoXDBlock blk )
  {
    ArrayList< DistoXDBlock > splays = mDataAdapter.getSplaysAtId( blk.mId );
    for ( DistoXDBlock b : splays ) {
      if ( b.mId == blk.mId ) {
        blk.setName( from, to );
        // FIXME leg should be 0
        int ret = mApp.mData.updateShot( blk.mId, mApp.mSID, from, to, extend, flag, leg?1:0, comment, true );

        if ( ret == -1 ) {
          Toast.makeText( this, R.string.no_db, Toast.LENGTH_SHORT ).show();
        // } else if ( ret == -2 ) {
        //   Toast.makeText( this, R.string.makes_cycle, Toast.LENGTH_SHORT ).show();
        } else {
          // // update same shots of the given block: SHOULD NOT HAPPEN
          // List< DistoXDBlock > blk_list = mApp.mData.selectShotsAfterId( blk.mId, mApp.mSID, 0L );
          // for ( DistoXDBlock blk1 : blk_list ) {
          //   if ( blk1.relativeDistance( blk ) > mApp.mCloseDistance ) break;
          //   mApp.mData.updateShotLeg( blk1.mId, mApp.mSID, 1L, true );
          // }
        }
      } else {
        b.setName( from, to );
        mApp.mData.updateShotName( b.mId, mApp.mSID, from, to, true );
      }
      mDataAdapter.updateBlockView( b );
    }
  }

  // ------------------------------------------------------------------------

  @Override
  public void onBackPressed () // askClose
  {
    new TopoDroidAlertDialog( this, getResources(),
                      getResources().getString( R.string.ask_close_survey ),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          finish();
        }
      }
    );
  }

  // ---------------------------------------------------------
  /* MENU

  // private MenuItem mMIsymbol;
  private MenuItem mMIsurvey;
  private MenuItem mMIoptions;
  private MenuItem mMIhelp;


  @Override
  public boolean onCreateOptionsMenu(Menu menu) 
  {
    super.onCreateOptionsMenu( menu );

    // mMIsymbol  = menu.add( R.string.menu_palette );
    mMIsurvey  = menu.add( R.string.menu_survey );
    mMIoptions = menu.add( R.string.menu_options );
    mMIhelp    = menu.add( R.string.menu_help  );

    // mMIsymbol.setIcon( R.drawable.ic_symbol );
    mMIsurvey.setIcon(  icons[14] );
    mMIoptions.setIcon( icons[15] );
    mMIhelp.setIcon(    icons[16] );

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) 
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "TopoDroidActivity onOptionsItemSelected() " + item.toString() );
    // Handle item selection
    // if ( item == mMIsymbol ) { 
    //   DrawingBrushPaths.makePaths( getResources() );
    //   (new SymbolEnableDialog( this, this )).show();
    // } else 
    if ( item == mMIsurvey ) { // SURVEY ACTIVITY 
      Intent intent = new Intent( this, SurveyActivity.class );
      intent.putExtra( TopoDroidApp.TOPODROID_SURVEY,  0 ); // mustOpen 
      intent.putExtra( TopoDroidApp.TOPODROID_OLDSID, -1 ); // old_sid 
      intent.putExtra( TopoDroidApp.TOPODROID_OLDID,  -1 ); // old_id 
      startActivityForResult( intent, INFO_ACTIVITY_REQUEST_CODE );
    } else if ( item == mMIoptions ) { // OPTIONS DIALOG
      Intent intent = new Intent( this, TopoDroidPreferences.class );
      intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SURVEY );
      startActivity( intent );
    } else if ( item == mMIhelp  ) { // HELP DIALOG
      int nn = mNrButton1; // + ( TopoDroidApp.mLevelOverNormal ?  mNrButton2 : 0 );
      (new HelpDialog(this, izons, menus, help_icons, help_menus, nn, 3 ) ).show();
    } else {
      return super.onOptionsItemSelected(item);
    }
    return true;
  }

  */

  private void setMenuAdapter()
  {
    int k = 0;
    Resources res = getResources();
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    mMenuAdapter.add( res.getString( menus[k++] ) );
    if ( TopoDroidSetting.mLevelOverBasic  ) mMenuAdapter.add( res.getString( menus[k++] ) );
    // if ( TopoDroidSetting.mLevelOverBasic  ) mMenuAdapter.add( res.getString( menus[k++] ) );
    if ( TopoDroidSetting.mLevelOverNormal ) mMenuAdapter.add( res.getString( menus[k++] ) );
    if ( TopoDroidSetting.mLevelOverNormal ) mMenuAdapter.add( res.getString( menus[k++] ) );
    if ( TopoDroidSetting.mLevelOverBasic  ) mMenuAdapter.add( res.getString( menus[k++] ) );
    if ( TopoDroidSetting.mLevelOverNormal ) mMenuAdapter.add( res.getString( menus[k++] ) );
    mMenuAdapter.add( res.getString( menus[k++] ) );
    mMenuAdapter.add( res.getString( menus[k++] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }


  void deletePlot( long pid1, long pid2 )
  {
    mApp.mData.deletePlot( pid1, mApp.mSID );
    mApp.mData.deletePlot( pid2, mApp.mSID );
    // FIXME NOTIFY
  }

  void recomputeItems( String st )
  {
    if ( ! mShowSplay.remove( st ) ) {
      mShowSplay.add( st );
    }
    updateDisplay( );
  }


  public void setConnectionStatus( boolean connected )
  { 
    if ( mApp.mDevice == null ) {
      mButton1[ 0 ].setVisibility( View.GONE );
    } else {
      mButton1[ 0 ].setVisibility( View.VISIBLE );
      if ( connected ) {
        mButton1[0].setBackgroundDrawable( mBMdownload_on );
      } else {
        mButton1[0].setBackgroundDrawable( mBMdownload );
      }
    }
  }

  public void notifyDisconnected()
  {
    if ( TopoDroidSetting.mAutoReconnect && TopoDroidSetting.mConnectionMode == TopoDroidSetting.CONN_MODE_CONTINUOUS ) {
      // Log.v("DistoX", "notify disconnected: auto reconnect ");
      try {
        do {
          Thread.sleep( 100 );
          // Log.v("DistoX", "notify disconnected: try reconnect " + mDataDownloader.mConnected );
          mDataDownloader.downloadData( ); 
        } while ( mDataDownloader.mConnected == false );
      } catch ( InterruptedException e ) { }
      // Log.v("DistoX", "notify disconnected: reconnected " + mDataDownloader.mConnected );
    }
  }

}
