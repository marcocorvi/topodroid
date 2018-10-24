/* @file ShotWindow.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid survey shots management
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.reflect.Method;

import java.io.File;
// import java.io.IOException;
// import java.io.EOFException;
// import java.io.DataInputStream;
// import java.io.DataOutputStream;
// import java.io.BufferedReader;
// import java.io.FileReader;
// import java.io.FileWriter;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
// import java.util.Stack;
import java.util.Locale;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
/* FIXME-23 */
import android.os.StrictMode;

// import android.os.Parcelable;
// import android.os.Debug;
// import android.os.SystemClock;
// import android.os.PowerManager;

import android.app.Activity;
// import android.app.Application;

// import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;

// import android.view.WindowManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.KeyEvent;
// for FRAGMENT
// import android.view.ViewGroup;
// import android.view.LayoutInflater;

import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
// import android.widget.PopupWindow;
// import android.widget.LinearLayout;
// import android.widget.RelativeLayout;
import android.widget.ArrayAdapter;
// import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

// import android.preference.PreferenceManager;

import android.provider.MediaStore;
// import android.provider.Settings.System;

import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;
// import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
// import android.graphics.Paint.FontMetrics;
// import android.graphics.Rect;

import android.net.Uri;

import android.util.Log;

// FIXME-28
// import androidx.recyclerview.widget.RecyclerView;
// import androidx.recyclerview.widget.LinearLayoutManager;

public class ShotWindow extends Activity
                          implements OnItemClickListener
                          , OnItemLongClickListener
                        , OnClickListener
                        , OnLongClickListener
                        , ILister
                        , INewPlot
                        , IPhotoInserter
{
  final static private int BTN_DOWNLOAD  = 0;
  final static private int BTN_BLUETOOTH = 1;
  final static private int BTN_PLOT      = 3;
  final static private int BTN_MANUAL    = 5;
  final static private int BTN_SEARCH    = 7;
  final static private int BTN_AZIMUTH   = 8;
  private int boff = 0;
  private boolean diving = false;

  private DataHelper mApp_mData;

  private static final int izons[] = {
                        R.drawable.iz_download,
                        R.drawable.iz_bt,
                        R.drawable.iz_mode,
                        R.drawable.iz_plot,
                        R.drawable.iz_note,
                        R.drawable.iz_plus,
                        R.drawable.iz_station,
                        R.drawable.iz_search,
                        R.drawable.iz_dial,
			R.drawable.iz_empty
                      };

  private static final int izonsno[] = {
                        0,
                        0,
                        0,
                        R.drawable.iz_plot, // TODO_IZ
                        0,
                        0,
                        0
                      };

  private static final int izonsF[] = {
                        R.drawable.iz_left,       // extend LEFT
                        R.drawable.iz_flip,       // extend flip
                        R.drawable.iz_right,      // extend RIGHT
                        R.drawable.iz_highlight,  // multishot dialog: includes // highlight in sketch
                          // R.drawable.iz_highlight,  // highlight in sketch
			  // R.drawable.iz_numbers_no, // renumber shots (first must be leg)
                          // R.drawable.iz_bedding,    // compute bedding
                        R.drawable.iz_delete,     // delete shots
                        R.drawable.iz_cancel,     // cancel
			R.drawable.iz_empty
                      };
  private static final int BTN_HIGHLIGHT = 3; // index of iz_highlight

  private static final int menus[] = {
                        R.string.menu_close,
                        R.string.menu_survey,
                        R.string.menu_recover,
                        R.string.menu_photo,
                        R.string.menu_sensor,
                        R.string.menu_3d,
                        R.string.menu_distox,
                        R.string.menu_options,
                        R.string.menu_help
                     };

  private static final int help_icons[] = {
                          R.string.help_download,
                          R.string.help_remote,
                          R.string.help_display,
                          R.string.help_plot,
                          R.string.help_note,
                          R.string.help_add_shot,
                          R.string.help_current_station,
                          R.string.help_search,
                          R.string.help_azimuth,
                        };
   private static final int help_menus[] = {
                          R.string.help_close,
                          R.string.help_survey_info,
                          R.string.help_undelete,
                          R.string.help_photo,
                          R.string.help_sensor,
                          R.string.help_3d,
                          R.string.help_device,
                          R.string.help_prefs,
                          R.string.help_help
                      };

  private static final int HELP_PAGE = R.string.ShotWindow;

  private TopoDroidApp   mApp;
  private Activity       mActivity;
  private DataDownloader mDataDownloader;
  private DistoXAccuracy mDistoXAccuracy;

  // TODO replace flags with DisplayMode-flag 
  //      N.B. id is in the data adapter
  boolean mFlagSplay  = true;  //!< whether to hide splay shots
  boolean mFlagLatest = false; //!< whether to show the latest splay shots
  boolean mFlagLeg    = true;  //!< whether to hide leg extra shots
  boolean mFlagBlank  = false; //!< whether to hide blank shots

  // private Bundle mSavedState = null;
  // private String mRecentPlot     = null; // moved to TDInstance
  // long   mRecentPlotType = PlotInfo.PLOT_PLAN;

  private int mButtonSize = 42;
  private Button[] mButton1;
  private int mNrButton1 = 0;

  private ListView mList;
  // private int mListPos = -1;
  // private int mListTop = 0;
  private DBlockAdapter mDataAdapter;
  // private ArrayAdapter< String > mDataAdapter;
  private ArrayList< String > mShowSplay;


  // private long mLastExtend; // id of the last-extend-ed splay 

  // private static final String LIST_STATE = "listState";
  // private int mFirstPos = -1;  
  // private int mScroll   = 0;
  // private int mSavePos  = -1;  // shot entry position
  private int mShotPos  = -1;  // shot entry position
  private int mPrevPos  = 0;   // prev shot entry position
  private int mNextPos  = 0;   // next shot entry position
  // private TextView mSaveTextView = null;

  private static long    mSensorId;
  private static long    mPhotoId;
  private static String  mComment;
  private static long    mShotId;   // photo/sensor shot id
  boolean mOnOpenDialog = false;

  // ConnHandler mHandler;

  private Button[] mButtonF;
  private int mNrButtonF = 6; // 8;

  private StationSearch mSearch;

  boolean isBlockMagneticBad( DBlock blk ) { return mDistoXAccuracy.isBlockMagneticBad( blk ); }

  String getBlockExtraString( DBlock blk ) { return mDistoXAccuracy.getBlockExtraString( blk ); }

  public void setRefAzimuth( float azimuth, long fixed_extend )
  {
    TDAzimuth.mFixedExtend = fixed_extend;
    TDAzimuth.mRefAzimuth  = azimuth;
    setRefAzimuthButton();
  }

  public void setRefAzimuthButton()
  {
    if ( ! TDLevel.overNormal ) return;
    if ( BTN_AZIMUTH - boff >= mButton1.length ) return;
    if ( TDAzimuth.mFixedExtend == 0 ) {
      // android.graphics.Matrix m = new android.graphics.Matrix();
      // m.postRotate( TDAzimuth.mRefAzimuth - 90 );
      // if ( mBMdial != null ) // extra care !!!
      {
        // Bitmap bm1 = Bitmap.createScaledBitmap( mBMdial, mButtonSize, mButtonSize, true );
        // Bitmap bm2 = Bitmap.createBitmap( bm1, 0, 0, mButtonSize, mButtonSize, m, true);
	Bitmap bm2 = mDialBitmap.getBitmap( TDAzimuth.mRefAzimuth, mButtonSize );
        mButton1[ BTN_AZIMUTH - boff ].setBackgroundDrawable( new BitmapDrawable( getResources(), bm2 ) );
      }
    } else if ( TDAzimuth.mFixedExtend == -1L ) {
      mButton1[ BTN_AZIMUTH - boff ].setBackgroundDrawable( mBMleft );
    } else {
      mButton1[ BTN_AZIMUTH - boff ].setBackgroundDrawable( mBMright );
    } 
  }

  TopoDroidApp getApp() { return mApp; }

  Set<String> getStationNames() { return mApp_mData.selectAllStations( TDInstance.sid ); }

  // -------------------------------------------------------------------
  // FXIME ok only for numbers
  // String getNextStationName()
  // {
  //   return mApp_mData.getNextStationName( TDInstance.sid );
  // }

  @Override
  public void refreshDisplay( int nr, boolean toast ) 
  {
    setConnectionStatus( mDataDownloader.getStatus() );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        // mLastShotId = mApp_mData.getLastShotId( TDInstance.sid );
        updateDisplay( );
      }
      if ( toast ) {
        if ( TDInstance.device.mType == Device.DISTO_X310 ) nr /= 2;
        TDToast.make( getResources().getQuantityString(R.plurals.read_data, nr, nr ) );
        // TDToast.make( " read_data: " + nr );
      }
    } else { // ( nr < 0 )
      if ( toast ) {
        if ( nr <= -5 ) {
          TDToast.make( getString(R.string.read_fail_with_code) + nr );
	       } else {
          TDToast.make( mApp.DistoXConnectionError[ -nr ] );
        }
      }
    }
  }
    
  void updateDisplay( )
  {
    // Log.v( "DistoX", "update Display() " );
    // highlightBlocks( null );
    // DataHelper data = mApp_mData;
    if ( mApp_mData != null && TDInstance.sid >= 0 ) {
      List<DBlock> list = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
      mDistoXAccuracy = new DistoXAccuracy( list ); 
      // if ( list.size() > 4 ) DistoXAccuracy.setBlocks( list );

      List< PhotoInfo > photos = mApp_mData.selectAllPhotos( TDInstance.sid, TDStatus.NORMAL );
      // TDLog.Log( TDLog.LOG_SHOT, "update Display() shot list size " + list.size() );
      // Log.v( TopoDroidApp.TAG, "update Display() shot list size " + list.size() );
      updateShotList( list, photos );
      
      setTheTitle( );
    } else {
      mApp.clearSurveyReferences();
      finish();
      // TDToast.make( R.string.no_survey );
    }
  }


  public void setTheTitle()
  {
    StringBuilder sb = new StringBuilder();
    if ( TDSetting.mConnectionMode == TDSetting.CONN_MODE_MULTI ) {
      sb.append( "{" );
      if ( TDInstance.device != null ) sb.append( TDInstance.device.getNickname() );
      sb.append( "} " );
    }
    sb.append( mApp.getConnectionStateTitleStr() );
    sb.append( TDInstance.survey );

    setTitleColor( StationPolicy.mTitleColor );
    mActivity.setTitle( sb.toString() );
  }

  boolean isCurrentStationName( String st ) { return mApp.isCurrentStationName( st ); }

  boolean setCurrentStationName( String st ) 
  { 
    mSkipItemClick = true;
    return mApp.setCurrentStationName( st ); 
    // updateDisplay( );
  }

  // add a block to the adapter (ILister interface)
  // called by the RFcommThread after receiving a data packet
  @Override
  public void updateBlockList( long blk_id )
  {
    // Log.v("DistoX", "Shot window update block list. Id: " + blk_id );
    DBlock blk = mApp_mData.selectShot( blk_id, TDInstance.sid );
    if ( blk != null ) {
      updateBlockList( blk );
    }
  }

  @Override
  public void updateBlockList( CalibCBlock blk ) { }
 
  @Override
  public void updateBlockList( DBlock blk )
  {
    // Log.v("DistoX", "Shot window update list. blk id: " + blk.mId + " " + blk.mLength + " " + blk.mBearing + " " + blk.mClino );
    if ( mDataAdapter != null ) {
      // FIXME 3.3.0
      if ( mDataAdapter.addDataBlock( blk ) ) {
        mDistoXAccuracy.addBlock( blk );
        if ( StationPolicy.doBacksight() || StationPolicy.doTripod() ) {
          mApp.assignStationsAll( mDataAdapter.mItems );
        } else {
          mApp.assignStationsAll( mDataAdapter.getItemsForAssign() );
        }
        // mApp_mData.getShotName( TDInstance.sid, blk );

        mList.post( new Runnable() {
          @Override public void run() {
            // Log.v("DistoX", "notify data set changed");
            mDataAdapter.notifyDataSetChanged(); // THIS IS IMPORTANT TO REFRESH THE DATA LIST
            mList.setSelection( mDataAdapter.getCount() - 1 );
          }
        } );
      }
    }
  }

  void setShowIds( boolean show ) { mDataAdapter.show_ids = show; }

  boolean getShowIds() { return mDataAdapter.show_ids; }

  // called only by updateDisplay
  private void updateShotList( List<DBlock> list, List< PhotoInfo > photos )
  {
    // TDLog.Log( TDLog.LOG_SHOT, "updateShotList shots " + list.size() + " photos " + photos.size() );
    mDataAdapter.clear();
    // mList.setAdapter( mDataAdapter );
    if ( list.size() == 0 ) {
      // TDToast.make( R.string.no_shots );
      return;
    }
    processShotList( list );
    mDataAdapter.reviseBlockWithPhotos( photos );
  }

  // called only by updateShotList
  private void processShotList( List<DBlock> list )
  {
    // Log.v("DistoX", "process shot list");
    DBlock prev = null;
    boolean prev_is_leg = false;
    for ( DBlock item : list ) {
      DBlock cur = item;
      // int t = cur.type();
      // TDLog.Log( TDLog.LOG_SHOT, "item " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );

      if ( cur.isSecLeg() || cur.isRelativeDistance( prev ) ) {
        // Log.v( "DistoX", "item close " + cur.type() + " " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
        if ( cur.isBlank() ) {   // FIXME 20140612
          cur.setTypeSecLeg();
          mApp_mData.updateShotLeg( cur.mId, TDInstance.sid, LegType.EXTRA, true ); // cur.mType ); // FIXME 20140616
        }

        // if ( prev != null && prev.isBlank() ) prev.setBlockType( DBlock.BLOCK_BLANK_LEG );
        if ( prev != null ) prev.setTypeBlankLeg();

        if ( mFlagLeg ) { // flag: hide leg extra shots
          // TDLog.Log( TDLog.LOG_SHOT, "close distance");

          if ( mFlagBlank && prev != null && prev.isTypeBlank() ) {
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
          if ( mFlagBlank && prev != null && prev.isTypeBlank() ) {
            if ( ! prev_is_leg ) {
              mDataAdapter.add( prev );
              prev_is_leg = true;
            // } else {
              /* nothing */
            }
          // } else {
          //   /* nothing */
          }
        }
      } else {
        // Log.v( "DistoX", "item not close " + cur.type() + " " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
        // TDLog.Log( TDLog.LOG_SHOT, "not close distance");
        prev_is_leg = false;
        if ( cur.isTypeBlank() ) { // DBlock.isTypeBlank(t)
          prev = cur;
          if ( mFlagBlank ) continue;
        } else if ( cur.isSplay() ) { // DBlock.isSplay(t) 
          prev = null;
          if ( mFlagSplay ) { // do hide splays, except those that are shown.
            // boolean skip = true;
            // for ( String st : mShowSplay ) {
            //   if ( st.equals( cur.mFrom ) ) { skip = false; break; }
            // }
            // if ( skip ) continue;
            if ( ! ( showSplaysContains( cur.mFrom ) || ( mFlagLatest && cur.isRecent() ) ) ) continue;
          }
        } else { // cur.isMainLeg() // t == DBlock.BLOCK_MAIN_LEG
          prev = cur;
        }
      }
      // TDLog.Log( TDLog.LOG_SHOT, "adapter add " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      // Log.v( "DistoX", "shot adapter add " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      mDataAdapter.add( cur );
    }
  }

  // ---------------------------------------------------------------
  // list items click

  private boolean mSkipItemClick = false;

  private void multiSelect( int pos )
  {
    if ( mDataAdapter.multiSelect( pos ) ) {
      mListView.setAdapter( mButtonViewF.mAdapter );
      mListView.invalidate();
      onMultiselect = true;
    } else {
      mListView.setAdapter( mButtonView1.mAdapter );
      mListView.invalidate();
      onMultiselect = false;
    }
  }

  void clearMultiSelect( )
  {
    // mApp.setHighlighted( null ); // FIXME_HIGHLIGHT
    mDataAdapter.clearMultiSelect( );
    mListView.setAdapter( mButtonView1.mAdapter );
    mListView.invalidate();
    onMultiselect = false;
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( CutNPaste.dismissPopupBT() ) return;
    if ( mSkipItemClick ) {
      mSkipItemClick = false;
      return;
    }
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    if ( closeMenu() ) return;

    if ( mDataAdapter.isMultiSelect() ) {
      multiSelect( pos );
      return;
    }

    // TDLog.Log( TDLog.LOG_INPUT, "ShotWindow onItemClick id " + id);
    DBlock blk = mDataAdapter.get(pos);
    // Log.v( "DistoX", "ShotWindow onItemClick id " + id + " pos " + pos + " blk " + blk.mFrom + " " + blk.mTo );
    if ( blk != null ) onBlockClick( blk, pos );
  }

  void onBlockClick( DBlock blk, int pos )
  {
    // Log.v("DistoX", "on block click: on_open " + mOnOpenDialog );
    if ( mOnOpenDialog ) return;
    mOnOpenDialog = true;
    mShotPos = pos;
    DBlock prevBlock = null;
    DBlock nextBlock = null;
    prevBlock = getPreviousLegShot( blk, false );
    nextBlock = getNextLegShot( blk, false );
    (new ShotDialog( mActivity, this, pos, blk, prevBlock, nextBlock )).show();
  }

  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( closeMenu() ) return true;
    if ( CutNPaste.dismissPopupBT() ) return true;

    // TDLog.Log( TDLog.LOG_INPUT, "ShotWindow onItemLongClick id " + id);
    DBlock blk = mDataAdapter.get(pos);
    if ( blk == null ) return false;
    // onBlockLongClick( blk );
    // if ( blk.isSplay() ) {
    //   highlightBlocks( blk );
    // } else {
      multiSelect( pos );
    // }
    return true;
  }

  // FIXME_X2_SPLAY
  // void updateSplayLeg( int pos ) // pos = mDataAdapter pos
  // {
  //   DBlock blk = mDataAdapter.get(pos);
  //   for ( ; blk != null; ) {
  //     long leg = mApp_mData.updateSplayLeg( blk.mId, TDInstance.sid, true );
  //     // Log.v("DistoX", "toggle splay type " + pos + " new leg " + leg );
  //     if ( leg == LegType.NORMAL ) {
  //       blk.setBlockType( DBlock.BLOCK_SPLAY );
  //     } else if ( leg == LegType.XSPLAY ) {
  //       blk.setBlockType( DBlock.BLOCK_X_SPLAY );
  //     } else {
  //       break;
  //     }
  //     // if ( blk.mView != null ) blk.mView.invalidate();
  //     mDataAdapter.updateBlockView( blk.mId );
  //     if ( (--pos) < 0 ) break;
  //     blk = mDataAdapter.get(pos);
  //   }
  // }

  // FIXME_X3_SPLAY from ShotDialog
  void updateSplayLegType( DBlock blk, int leg_type )
  {
    int block_type = DBlock.blockOfSplayLegType[ leg_type ];
    // long leg_type = DBlock.legOfBlockType[ block_type ];
    mApp_mData.updateShotLeg( blk.mId, TDInstance.sid, leg_type, false ); // do not forward
    blk.setBlockType( block_type );
    updateDisplay();
  }

  // from MultiselectDialog
  void updateSplaysLegType( List<DBlock> blks, int leg_type )
  {
    for ( DBlock blk : blks ) {
      int block_type = DBlock.blockOfSplayLegType[ leg_type ];
      // long leg_type = DBlock.legOfBlockType[ block_type ];
      mApp_mData.updateShotLeg( blk.mId, TDInstance.sid, leg_type, false ); // do not forward
      blk.setBlockType( block_type );
    }
    updateDisplay();
    clearMultiSelect();
  }

  // called by ShotDialog "More" button
  void onBlockLongClick( DBlock blk )
  {
    mShotId = blk.mId;
    if ( TDLevel.overNormal ) {
      (new PhotoSensorsDialog(mActivity, this, blk ) ).show();
    } else {
      (new ShotDeleteDialog( mActivity, this, blk ) ).show();
    }
  }


  private void handleMenu( int pos )
  {
    closeMenu();
    int p = 0;
    if ( p++ == pos ) { // CLOSE
      if ( TDSetting.mDataBackup ) {
        TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false ); // try_save
      }
      TopoDroidApp.mShotWindow = null;
      super.onBackPressed();

    } else if ( p++ == pos ) { // SURVEY ACTIVITY
      Intent intent = new Intent( this, SurveyWindow.class );
      intent.putExtra( TDTag.TOPODROID_SURVEY,  0 ); // mustOpen 
      intent.putExtra( TDTag.TOPODROID_OLDSID, -1 ); // old_sid 
      intent.putExtra( TDTag.TOPODROID_OLDID,  -1 ); // old_id 
      startActivityForResult( intent, TDRequest.INFO_ACTIVITY_SHOTWINDOW );
    // } else if ( TDLevel.overBasic && p++ == pos ) { // CURRENT STATION
    //   (new CurrentStationDialog( this, this, mApp )).show();

    } else if ( TDLevel.overBasic && p++ == pos ) { // RECOVER
      List< DBlock > shots1 = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.DELETED );
      List< DBlock > shots2 = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.OVERSHOOT );
      List< DBlock > shots3 = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.CHECK );
      List< PlotInfo > plots     = mApp_mData.selectAllPlots( TDInstance.sid, TDStatus.DELETED );
      if ( shots1.size() == 0 && shots2.size() == 0 && shots3.size() == 0 && plots.size() == 0 ) {
        TDToast.make( R.string.no_undelete );
      } else {
        (new UndeleteDialog(mActivity, this, mApp_mData, TDInstance.sid, shots1, shots2, shots3, plots ) ).show();
      }
      // updateDisplay( );
    } else if ( TDLevel.overNormal && p++ == pos ) { // PHOTO
      mActivity.startActivity( new Intent( mActivity, PhotoActivity.class ) );
    } else if ( TDLevel.overNormal && p++ == pos ) { // SENSORS
      mActivity.startActivity( new Intent( mActivity, SensorListActivity.class ) );
    } else if ( TDLevel.overBasic && p++ == pos ) { // 3D
      if ( TopoDroidApp.exportSurveyAsThSync( ) ) { // make sure to have survey exported as therion
        try {
          Intent intent = new Intent( "Cave3D.intent.action.Launch" );
          intent.putExtra( "survey", TDPath.getSurveyThFile( TDInstance.survey ) );
          mActivity.startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          TDToast.make( R.string.no_cave3d );
        }
      }
    } else if ( TDLevel.overNormal && (! diving) && p++ == pos ) { // DEVICE
      if ( mApp.mBTAdapter.isEnabled() ) {
        mActivity.startActivity( new Intent( Intent.ACTION_VIEW ).setClass( mActivity, DeviceActivity.class ) );
      }
    } else  if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( mActivity, TDPrefActivity.class );
      intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_CATEGORY_SURVEY );
      mActivity.startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      // int nn = mNrButton1; //  + ( TDLevel.overNormal ?  mNrButton2 : 0 );
      new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, menus.length, getResources().getString( HELP_PAGE ) ).show();
    }
  }

// ---------------------------------------------------------------

  void askPhotoComment( )
  {
    (new PhotoCommentDialog(mActivity, this ) ).show();
  }


  void doTakePhoto( String comment, int camera )
  {
    mComment = comment;
    mPhotoId = mApp_mData.nextPhotoId( TDInstance.sid );

    // imageFile := PHOTO_DIR / surveyId / photoId .jpg
    File imagefile = new File( TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mPhotoId) ) );
    // TDLog.Log( TDLog.LOG_SHOT, "photo " + imagefile.toString() );
    if ( camera == 1 ) { // TopoDroid camera
      new QCamCompass( this,
                       (new MyBearingAndClino( mApp, imagefile)),
                       // null, -1L, // drawer, pid // DO NOT USE THIS
                       this,
                       false, false).show();  // false = with_box, false=with_delay
    } else {
      boolean ok = true;
      /* FIXME-23 */
      if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) { // build version 24
        try {
          Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposed");
          m.invoke( null );
        } catch ( Exception e ) { ok = false; }
      }
      /* */
      if ( ok ) {
        try {
          Uri outfileuri = Uri.fromFile( imagefile );
          Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
          intent.putExtra( MediaStore.EXTRA_OUTPUT, outfileuri );
          intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
          startActivityForResult( intent, TDRequest.CAPTURE_IMAGE_SHOTWINDOW );
        } catch ( ActivityNotFoundException e ) {
          TDToast.make( R.string.no_capture_app );
        }
      } else {
        TDToast.make( "NOT IMPLEMENTED YET" );
      }
    }
  }

  void askSensor( )
  {
    mSensorId = mApp_mData.nextSensorId( TDInstance.sid );
    // TDLog.Log( TDLog.LOG_SENSOR, "sensor " + mSensorId );
    Intent intent = new Intent( mActivity, SensorActivity.class );
    startActivityForResult( intent, TDRequest.SENSOR_ACTIVITY_SHOTWINDOW );
  }

  // void askExternal( )
  // {
  //   mSensorId = mApp_mData.nextSensorId( TDInstance.sid );
  //   TDLog.Log( TDLog.LOG_SENSOR, "sensor " + mSensorId );
  //   Intent intent = new Intent( this, ExternalActivity.class );
  //   startActivityForResult( intent, TDRequest.EXTERNAL_ACTIVITY );
  // }

  // called to insert a manual shot after a given shot
  void dialogInsertShotAt( DBlock blk )
  {
    (new ShotNewDialog( this, mApp, this, blk, mShotId )).show();
  }

  // return id of inserted leg
  long insertDuplicateLeg( String from, String to, float distance, float bearing, float clino, int extend )
  {
    return mApp.insertDuplicateLeg( from, to, distance, bearing, clino, extend );
    // updateDisplay( ); 
  }

  void insertLRUDatStation( long at, String station, float bearing, float clino,
                            String left, String right, String up, String down )
  {
    mApp.insertLRUDatStation( at, station, bearing, clino, left, right, up, down );
    updateDisplay( ); 
  }

  // called by PhotoSensorDialog to split the survey
  // void askSurvey( )
  // {
  //   TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.survey_split,
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         doSplitSurvey();
  //       }
  //   } );
  // }
  
  void doSplitSurvey()
  {
    long old_sid = TDInstance.sid;
    long old_id  = mShotId;
    // Log.v( TopoDroidApp.TAG, "split survey " + old_sid + " " + old_id );
    if ( mApp.mShotWindow != null ) {
      if ( TDSetting.mDataBackup ) {
        TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false ); // try_save
      }
      mApp.mShotWindow.finish();
      mApp.mShotWindow = null;
    }
    if ( mApp.mSurveyWindow != null ) {
      mApp.mSurveyWindow.finish();
      mApp.mSurveyWindow = null;
    }
    mApp.mActivity.startSplitSurvey( old_sid, old_id ); // SPLIT SURVEY
  }

  void doDeleteShot( long id, DBlock blk, int status, boolean leg )
  {
    mApp_mData.deleteShot( id, TDInstance.sid, status, true ); // forward = true
    if ( blk != null && blk.isMainLeg() ) { //  DBlock.BLOCK_MAIN_LEG 
      if ( leg ) {
        for ( ++id; ; ++id ) {
          DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
          if ( b == null || ! b.isSecLeg() ) { // != DBlock.BLOCK_SEC_LEG 
            break;
	  }
          mApp_mData.deleteShot( id, TDInstance.sid, status, true ); // forward = true
        }
      } else { // set station to next leg shot
        ++id;
        DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
        if ( b != null && b.isSecLeg() ) { //  DBlock.BLOCK_SEC_LEG --> leg-flag = 0
          mApp_mData.updateShot( id, TDInstance.sid, blk.mFrom, blk.mTo, blk.getFullExtend(), blk.getFlag(), 0, blk.mComment, true ); // forward = true
          mApp_mData.updateShotStatus( id, TDInstance.sid, 0, true ); // status normal, forward = true
        }
      }
    }
    updateDisplay( ); 
  }

  public void insertPhoto( )
  {
    // long shotid = 0;
    mApp_mData.insertPhoto( TDInstance.sid, mPhotoId, mShotId, "", TopoDroidUtil.currentDate(), mComment ); // FIXME TITLE has to go
    // FIXME NOTIFY ? no
  }

  // void deletePhoto( PhotoInfo photo ) 
  // {
  //   mApp_mData.deletePhoto( TDInstance.sid, photo.id );
  //   File imagefile = new File( mApp.getSurveyJpgFile( Long.toString(photo.id) ) );
  //   try {
  //     imagefile.delete();
  //   } catch ( IOException e ) { }
  // }

  @Override
  protected void onActivityResult( int reqCode, int resCode, Intent data )
  {
    TDLog.Log( TDLog.LOG_DEBUG, "on Activity Result: request " + reqCode + " result " + resCode );
    switch ( reqCode ) {
      case TDRequest.CAPTURE_IMAGE_SHOTWINDOW:
        mApp.resetLocale();
        if ( resCode == Activity.RESULT_OK ) { // RESULT_OK = -1 (0xffffffff)
          // (new PhotoCommentDialog(this, this) ).show();
          insertPhoto();
        // } else {
          // mApp_mData.deletePhoto( TDInstance.sid, mPhotoId );
        }
        break;
      case TDRequest.SENSOR_ACTIVITY_SHOTWINDOW:
      // case TDRequest.EXTERNAL_ACTIVITY:
        if ( resCode == Activity.RESULT_OK ) {
          Bundle extras = data.getExtras();
          if ( extras != null ) {
            String type  = extras.getString( TDTag.TOPODROID_SENSOR_TYPE );
            String value = extras.getString( TDTag.TOPODROID_SENSOR_VALUE );
            String comment = extras.getString( TDTag.TOPODROID_SENSOR_COMMENT );
            // TDLog.Log( TDLog.LOG_SENSOR, "insert sensor " + type + " " + value + " " + comment );

            mApp_mData.insertSensor( TDInstance.sid, mSensorId, mShotId, "",
                                  TopoDroidUtil.currentDate(),
                                  comment,
                                  type,
                                  value );
            // FIXME NOTIFY ? no
          }
        }
        break;
      case TDRequest.INFO_ACTIVITY_SHOTWINDOW:
        if ( resCode == Activity.RESULT_OK ) {
          if ( TDSetting.mDataBackup ) {
            TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false ); // try_save
	  }
          finish();
        }
        break;
    }
    setRefAzimuthButton( );
  }

  // ---------------------------------------------------------------
  // private Button mButtonHelp;
  private HorizontalListView mListView;
  private HorizontalButtonView mButtonView1;
  private HorizontalButtonView mButtonViewF;
  private ListView   mMenu = null;
  private Button     mImage;
  // HOVER
  // MyMenuAdapter mMenuAdapter;
  private ArrayAdapter< String > mMenuAdapter;
  private boolean onMenu = false;
  private boolean onMultiselect = false;

  private BitmapDrawable mBMbluetooth;
  private BitmapDrawable mBMbluetooth_no;
  private BitmapDrawable mBMdownload;
  private BitmapDrawable mBMdownload_on;
  private BitmapDrawable mBMdownload_wait;
  private BitmapDrawable mBMdownload_no;
  // BitmapDrawable mBMadd;
  private BitmapDrawable mBMplot;
  // Bitmap mBMdial;
  // Bitmap mBMdial_transp;
  private MyTurnBitmap mDialBitmap;
  private BitmapDrawable mBMplot_no;
  private BitmapDrawable mBMleft;
  private BitmapDrawable mBMright;

  // void refreshList()
  // {
  //   Log.v("DistoX", "refresh display" );
  //   mDataAdapter.notifyDataSetChanged();
  //   mList.invalidate();
  // }
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );
    setContentView( R.layout.shot_activity );
    mApp = (TopoDroidApp) getApplication();
    mApp_mData = TopoDroidApp.mData;
    mApp.mShotWindow = this; // FIXME
    mDataDownloader = mApp.mDataDownloader; // new DataDownloader( this, mApp );
    mActivity = this;
    mOnOpenDialog = false;
    mDistoXAccuracy = new DistoXAccuracy( ); 

    // FIXME-28
    // RecyclerView rv = (RecyclerView) findViewById( R.id.recycler_view );
    // LinearLayoutManager lm = new LinearLayoutManager( this );
    // rv.setLayoutManager( lm );

    mShowSplay   = new ArrayList<>();
    mDataAdapter = new DBlockAdapter( this, this, R.layout.dblock_row, new ArrayList<DBlock>() );

    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlacholder( true );
    mButtonSize = TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    Resources res = getResources();
    mNrButton1 = TDLevel.overExpert ? 9
               : TDLevel.overNormal ? 8
               : TDLevel.overBasic ?  6 : 5;
    diving = ( TDInstance.datamode == SurveyInfo.DATAMODE_DIVING );
    if ( diving ) {
      mNrButton1 -= 2;
      boff = 2;
    } else {
      boff = 0;
    }
    mButton1 = new Button[ mNrButton1 + 1 ];
    for ( int k=0; k < mNrButton1; ++k ) {
      int kk = k+boff;
      mButton1[k] = MyButton.getButton( this, this, izons[kk] );
    }
    mButton1[mNrButton1] = MyButton.getButton( this, this, R.drawable.iz_empty );
    // mBMdial          = BitmapFactory.decodeResource( res, R.drawable.iz_dial_transp ); // FIXME AZIMUTH_DIAL
    // mBMdial_transp   = BitmapFactory.decodeResource( res, R.drawable.iz_dial_transp );
    mDialBitmap      = TopoDroidApp.getDialBitmap( res );

    mBMplot     = MyButton.getButtonBackground( mApp, res, izons[BTN_PLOT] );
    mBMplot_no  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_plot_no );
    mBMleft     = MyButton.getButtonBackground( mApp, res, R.drawable.iz_left );
    mBMright    = MyButton.getButtonBackground( mApp, res, R.drawable.iz_right );
    if ( ! diving ) {
      mBMdownload = MyButton.getButtonBackground( mApp, res, izons[BTN_DOWNLOAD] );
      mBMbluetooth = MyButton.getButtonBackground( mApp, res, izons[BTN_BLUETOOTH] );
      mBMdownload_on   = MyButton.getButtonBackground( mApp, res, R.drawable.iz_download_on );
      mBMdownload_wait = MyButton.getButtonBackground( mApp, res, R.drawable.iz_download_wait );
      mBMdownload_no   = MyButton.getButtonBackground( mApp, res, R.drawable.iz_download_no );
      mBMbluetooth_no  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_bt_no );
    }


    if ( TDLevel.overBasic ) {
      if ( ! diving ) mButton1[ BTN_DOWNLOAD ].setOnLongClickListener( this );
      mButton1[ BTN_PLOT - boff ].setOnLongClickListener( this );
      mButton1[ BTN_MANUAL - boff ].setOnLongClickListener( this );
      if ( TDLevel.overExpert ) {
        mButton1[ BTN_SEARCH - boff ].setOnLongClickListener( this );
      }
    }

    if ( ! TDLevel.overExpert ) mNrButtonF --;
    mButtonF = new Button[ mNrButtonF + 1 ];
    int k0 = 0;
    for ( int k=0; k < mNrButtonF; ++k ) {
      if ( k == BTN_HIGHLIGHT && ! TDLevel.overExpert ) continue; 
      mButtonF[k0] = MyButton.getButton( this, this, izonsF[k] );
      ++k0;
    }
    mButtonF[mNrButtonF] = MyButton.getButton( this, this, R.drawable.iz_empty );

    TDAzimuth.resetRefAzimuth( this, 90 );
    // setRefAzimuthButton( ); // called by mApp.resetRefAzimuth

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mButtonViewF = new HorizontalButtonView( mButtonF );
    mListView.setAdapter( mButtonView1.mAdapter );
    onMultiselect = false;

    mList = (ListView) findViewById(R.id.list);
    mList.setAdapter( mDataAdapter );
    mList.setOnItemClickListener( this );
    mList.setLongClickable( true );
    mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );
    // mList.setSmoothScrollbarEnabled( true );
    // mList.setFastScrollAlwaysVisible( true ); // API-11
    // mList.setFastScrollEnabled( true );

    // restoreInstanceFromData();
    // mLastExtend = mApp_mData.getLastShotId( TDInstance.sid );
    // List<DBlock> list = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mImage.setBackgroundDrawable( MyButton.getButtonBackground( mApp, res, R.drawable.iz_menu ) );

    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter( getResources() );
    onMenu = true;
    closeMenu();
    // HOVER
    mMenu.setOnItemClickListener( this );

    // CutNPaste.dismissPopupBT();

    if ( mDataDownloader != null ) {
      mApp.registerLister( this );
    }

    mSearch = new StationSearch();
  }

  // void enableSketchButton( boolean enabled )
  // {
  //   mApp.mEnableZip = enabled;
  //   mButton1[ BTN_PLOT - boff ].setEnabled( enabled ); // FIXME SKETCH BUTTON 
  //   mButton1[ BTN_PLOT - boff ].setBackgroundDrawable( enabled ? mBMplot : mBMplot_no );
  // }

  // @Override
  // public void onStart() 
  // {
  //   super.onStart();
  //   // Debug.startMethodTracing( "distox" );
  //   // Log.v( "DistoX", "Shot Activity onStart() " );
  // }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    // Log.v("DistoX", "ShotWindow onDestroy()" );
    if ( mDataDownloader != null ) {
      mApp.unregisterLister( this );
      mDataDownloader.onStop();
    }
    mApp.disconnectRemoteDevice( false );

    if ( doubleBackHandler != null ) {
      doubleBackHandler.removeCallbacks( doubleBackRunnable );
    }
  }

  // @Override
  // public synchronized void onStop() 
  // {
  //   // Debug.stopMethodTracing( );
  //   super.onStop();
  //   // Log.v("DistoX", "ShotWindow onStop()" );
  // }

  @Override
  public synchronized void onPause() 
  {
    super.onPause();
    // Log.v("DistoX", "ShotWindow onPause()" );
    saveInstanceToData();

    // mApp.unregisterConnListener( mHandler );
    // if ( mApp.mComm != null ) { mApp.mComm.suspend(); }
    // FIXME NOTIFY unregister ILister
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    mApp.resetLocale();

    // FIXME NOTIFY register ILister
    // if ( mApp.mComm != null ) { mApp.mComm.resume(); }
    // Log.v( "DistoX", "Shot Activity on Resume()" );
    
    restoreInstanceFromData();
    updateDisplay( );

    if ( mDataDownloader != null ) {
      mDataDownloader.onResume();
      // mApp.registerConnListener( mHandler );
      setConnectionStatus( mDataDownloader.getStatus() );
    }
    setRefAzimuthButton( );
  }

  // --------------------------------------------------------------

  private boolean doubleBack = false;
  private Handler doubleBackHandler = new Handler();
  private Toast   doubleBackToast = null;

  private final Runnable doubleBackRunnable = new Runnable() {
    @Override 
    public void run() {
      doubleBack = false;
      if ( doubleBackToast != null ) doubleBackToast.cancel();
      doubleBackToast = null;
    }
  };

  @Override
  public void onBackPressed () // askClose
  {
    if ( closeMenu() ) return;
    if ( CutNPaste.dismissPopupBT() ) return;
    if ( onMultiselect ) {
      clearMultiSelect();
      return;
    }

    if ( doubleBack ) {
      if ( doubleBackToast != null ) doubleBackToast.cancel();
      doubleBackToast = null;
      DrawingSurface.clearCache();
      if ( TDSetting.mDataBackup ) {
        TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false ); // try_save
      }
      TopoDroidApp.mShotWindow = null;
      super.onBackPressed();
      return;
    }
    doubleBack = true;
    doubleBackToast = TDToast.makeToast( R.string.double_back );
    doubleBackHandler.postDelayed( doubleBackRunnable, 1000 );
  }

  // --------------------------------------------------------------

  // FIXME NOTIFY: the display mode is local - do not notify
  private void restoreInstanceFromData()
  { 
    String shots = mApp_mData.getValue( "DISTOX_SHOTS" );
    if ( shots != null ) {
      String[] vals = shots.split( " " );
      // FIXME assert( vals.length > 3 );
      mFlagSplay  = vals[0].equals( TDString.ONE );
      mFlagLeg    = vals[1].equals( TDString.ONE );
      mFlagBlank  = vals[2].equals( TDString.ONE );
      setShowIds( vals[3].equals( TDString.ONE ) );
      mFlagLatest = ( vals.length > 4) && vals[4].equals( TDString.ONE );
      // Log.v("DistoX", "restore from data mFlagSplay " + mFlagSplay );
    }
  }
    
  private void saveInstanceToData()
  {
    mApp_mData.setValue( "DISTOX_SHOTS",
      String.format(Locale.US, "%d %d %d %d %d", mFlagSplay?1:0, mFlagLeg?1:0, mFlagBlank?1:0, getShowIds()?1:0, mFlagLatest?1:0 ) );
    // Log.v("DistoX", "save to data mFlagSplay " + mFlagSplay );
  }

  void doBluetooth( Button b ) // BLUETOOTH
  {
    if ( ! mDataDownloader.isDownloading() ) {
      if ( TDLevel.overAdvanced
             && TDInstance.distoType() == Device.DISTO_X310 
	     && TDSetting.mConnectionMode != TDSetting.CONN_MODE_MULTI
	  ) {
        CutNPaste.showPopupBT( mActivity, this, mApp, b, false );
      } else {
        mDataDownloader.setDownload( false );
        mDataDownloader.stopDownloadData();
        setConnectionStatus( mDataDownloader.getStatus() );
        mApp.resetComm();
        TDToast.make(R.string.bt_reset );
      }
    // } else { // downloading: nothing
    }
  }

  @Override 
  public boolean onLongClick( View view )
  {
    if ( closeMenu() ) return true;
    if ( CutNPaste.dismissPopupBT() ) return true;

    Button b = (Button)view;
    if ( ! diving && b == mButton1[ BTN_DOWNLOAD ] ) { // MULTI-DISTOX
      if (   TDSetting.mConnectionMode == TDSetting.CONN_MODE_MULTI
          && ! mDataDownloader.isDownloading() 
          && TopoDroidApp.mDData.getDevices().size() > 1 ) {
        (new DeviceSelectDialog( this, mApp, mDataDownloader, this )).show();
      } else {
        mDataDownloader.toggleDownload();
        setConnectionStatus( mDataDownloader.getStatus() );
        mDataDownloader.doDataDownload( );
      }
    } else if ( b == mButton1[ BTN_PLOT - boff ] ) {
      if ( TDInstance.recentPlot != null ) {
        startExistingPlot( TDInstance.recentPlot, TDInstance.recentPlotType, null );
      } else {
        // onClick( view ); // fall back to onClick
        new PlotListDialog( mActivity, this, mApp, null ).show();
      }
    } else if ( b == mButton1[ BTN_MANUAL - boff ] ) {
      new SurveyCalibrationDialog( mActivity, mApp ).show();
    } else if ( b == mButton1[ BTN_SEARCH - boff ] ) { // next search pos
      jumpToPos( mSearch.nextPos() );
    }
    return true;
  } 

  void searchStation( String name, boolean splays )
  {
    mSearch.set( name, mDataAdapter.searchStation( name, splays ) );
    if ( ! jumpToPos( mSearch.nextPos() ) ) {
      TDToast.make( R.string.station_not_found );
    }
  }

  boolean jumpToPos( final int pos ) 
  {
    if ( pos < 0 ) return false;
    mList.post( new Runnable() {
      @Override
      public void run() {
        mList.setSelection( pos );
        View v = mList.getChildAt( pos );
        if ( v != null ) v.requestFocus();
      }
    } );
    return true;
  }
   

  @Override 
  public void onClick(View view)
  {
    if ( closeMenu() ) return;
    if ( CutNPaste.dismissPopupBT() ) return;

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
      int kf = 0;
      // int k2 = 0;
      if ( ! diving ) {
        if ( k1 < mNrButton1 && b == mButton1[k1++] ) {        // DOWNLOAD
          if ( TDInstance.device != null ) {
            setConnectionStatus( 2 ); // turn arrow orange
            // TDLog.Log( TDLog.LOG_INPUT, "Download button, mode " + TDSetting.mConnectionMode );
            mDataDownloader.toggleDownload();
            setConnectionStatus( mDataDownloader.getStatus() );
            mDataDownloader.doDataDownload( );
          }
	  return;
        } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // BT RESET
          doBluetooth( b );
	  return;
        }
      }
      if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // DISPLAY 
        new ShotDisplayDialog( mActivity, this ).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // SKETCH
        new PlotListDialog( mActivity, this, mApp, null ).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // NOTE
        if ( TDInstance.survey != null ) {
          (new DistoXAnnotations( mActivity, TDInstance.survey )).show();
        }

      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // ADD MANUAL SHOT
        if ( TDLevel.overBasic ) {
          DBlock last_blk = mApp_mData.selectLastLegShot( TDInstance.sid );
          // Log.v( "DistoX", "last blk: " + last_blk.toString() );
          (new ShotNewDialog( mActivity, mApp, this, last_blk, -1L )).show();
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // STATIONS
        if ( TDLevel.overNormal ) {
          (new CurrentStationDialog( mActivity, this, mApp )).show();
          // ArrayList<DBlock> list = numberSplays(); // SPLAYS splays numbering no longer active
          // if ( list != null && list.size() > 0 ) {
          //   updateDisplay( );
          // }
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // SEARCH
        new StationSearchDialog( mActivity, this, mSearch.getName() ).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // AZIMUTH
        if ( TDLevel.overNormal ) {
          if ( TDSetting.mAzimuthManual ) {
            setRefAzimuth( 0, - TDAzimuth.mFixedExtend );
          } else {
            (new AzimuthDialDialog( mActivity, this, TDAzimuth.mRefAzimuth, mDialBitmap )).show();
          }
        }

      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // LEFT reset stretch
        for ( DBlock blk : mDataAdapter.mSelect ) {
          blk.setExtend( DBlock.EXTEND_LEFT, DBlock.STRETCH_NONE );
          mApp_mData.updateShotExtend( blk.mId, TDInstance.sid, DBlock.EXTEND_LEFT, DBlock.STRETCH_NONE, true );
        }
        clearMultiSelect( );
        updateDisplay();
      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // FLIP
        for ( DBlock blk : mDataAdapter.mSelect ) {
          if ( blk.flipExtendAndStretch() ) {
            mApp_mData.updateShotExtend( blk.mId, TDInstance.sid, blk.getExtend(), blk.getStretch(), true );
          }
        }
        clearMultiSelect( );
        updateDisplay();
      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // RIGHT reset stretch
        for ( DBlock blk : mDataAdapter.mSelect ) {
          blk.setExtend( DBlock.EXTEND_RIGHT, DBlock.STRETCH_NONE );
          mApp_mData.updateShotExtend( blk.mId, TDInstance.sid, DBlock.EXTEND_RIGHT, DBlock.STRETCH_NONE, true );
        }
        clearMultiSelect( );
        updateDisplay();
        // mList.invalidate(); // NOTE not enough to see the change in the list immediately
      } else if ( TDLevel.overExpert && kf < mNrButtonF && b == mButtonF[kf++] ) { // HIGHLIGHT
        // ( blks == null || blks.size() == 0 ) cannot happen
        (new MultishotDialog( mActivity, this, mDataAdapter.mSelect )).show();
      //   highlightBlocks( mDataAdapter.mSelect );
      //   // clearMultiSelect( );
      //   // updateDisplay();
      // } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // RENUMBER SELECTED SHOTS
      //   renumberBlocks( mDataAdapter.mSelect );
      //   clearMultiSelect( );
      //   mList.invalidate();
      // } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // BEDDING
      //   computeBedding( mDataAdapter.mSelect );
      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // DELETE
        askMultiDelete();
      } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // CANCEL
        clearMultiSelect( );
        mList.invalidate();
      }
    }
  }

  private void askMultiDelete()
  {
    Resources res = getResources();
    TopoDroidAlertDialog.makeAlert( mActivity, res, res.getString(R.string.shots_delete),
      res.getString(R.string.button_ok), 
      res.getString(R.string.button_cancel),
      new DialogInterface.OnClickListener() { // ok handler
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doMultiDelete();
        } },
      new DialogInterface.OnClickListener() { // cancel handler
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          clearMultiSelect( );
          mList.invalidate();
        } }
    );
  }

  void doMultiDelete()
  {
    for ( DBlock blk : mDataAdapter.mSelect ) {
      long id = blk.mId;
      mApp_mData.deleteShot( id, TDInstance.sid, TDStatus.DELETED, true ); // forward = true
      if ( /* blk != null && */ blk.isMainLeg() ) { // == DBlock.BLOCK_MAIN_LEG 
        if ( mFlagLeg ) {
          for ( ++id; ; ++id ) {
            DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
            if ( b == null || ! b.isSecLeg() ) { // != DBlock.BLOCK_SEC_LEG
              break;
	    }
            mApp_mData.deleteShot( id, TDInstance.sid, TDStatus.DELETED, true ); // forward = true
          }
        } else { // set station to next leg shot
          ++id;
          DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
          if ( b != null && b.isSecLeg() ) { // DBlock.BLOCK_SEC_LEG --> leg-flag 0
            mApp_mData.updateShot( id, TDInstance.sid, blk.mFrom, blk.mTo, blk.getFullExtend(), blk.getFlag(), 0, blk.mComment, true ); // forward = true
            mApp_mData.updateShotStatus( id, TDInstance.sid, 0, true ); // status normal, forward = true
          }
        }
      }
    }
    clearMultiSelect( );
    updateDisplay( ); 
  }

  // ------------------------------------------------------------------

  public boolean hasSurveyPlot( String name )
  {
    return mApp_mData.hasSurveyPlot( TDInstance.sid, name+"p" );
  }
 
  public boolean hasSurveyStation( String start )
  {
    return mApp_mData.hasSurveyStation( TDInstance.sid, start );
  }

  public void makeNewPlot( String name, String start, boolean extended, int project )
  {
    long mPIDp = mApp.insert2dPlot( TDInstance.sid, name, start, extended, project );

    if ( mPIDp >= 0 ) {
      long mPIDs = mPIDp + 1L; // FIXME !!! this is true but not guaranteed
      startDrawingWindow( start, name+"p", mPIDp, name+"s", mPIDs, PlotInfo.PLOT_PLAN, start, false ); // default no-landscape
    // } else {
    //   TDToast.make( R.string.plot_duplicate_name );
    }
    // updateDisplay( );
  }

/* FIXME BEGIN SKETCH_3D */
  public void makeNewSketch3d( String name, String st1, String st2 )
  {
    // FIXME xoffset yoffset, east south and vert (downwards)
    if ( st2 != null ) {
      if ( ! mApp_mData.hasShot( TDInstance.sid, st1, st2 ) ) {
        TDToast.make( R.string.no_shot_between_stations );
        return;
      }
    } else {
      st2 = mApp_mData.nextStation( TDInstance.sid, st1 );
    }
    if ( st2 != null ) {
      float e = 0.0f; // NOTE (e,s,v) are the coord of station st1, and st1 is taken as the origin of the ref-frame
      float s = 0.0f;
      float v = 0.0f;
      long mPID = mApp_mData.insertSketch3d( TDInstance.sid, -1L, name, 0L, st1, st1, st2,
                                            0, // mApp.mDisplayWidth/(2*TopoDroidApp.mScaleFactor),
                                            0, // mApp.mDisplayHeight/(2*TopoDroidApp.mScaleFactor),
                                            10 * TopoDroidApp.mScaleFactor,
                                            0, 0, 10 * TopoDroidApp.mScaleFactor,
                                            0, 0, 10 * TopoDroidApp.mScaleFactor,
                                            e, s, v, 180, 0 );
      if ( mPID >= 0 ) {
        startSketchWindow( name );
      }
    } else {
      TDToast.make( R.string.no_to_station );
    }
  }
 
  void startSketchWindow( String name )
  {
    if ( TDInstance.sid < 0 ) {
      TDToast.make( R.string.no_survey );
      return;
    }

    if ( ! mApp_mData.hasSketch3d( TDInstance.sid, name ) ) {
      TDToast.make( R.string.no_sketch );
      return;
    }

    // notice when starting the SketchWindow the remote device is disconnected 
    // FIXME mApp.disconnectRemoteDevice();

    // TODO
    Intent sketchIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, SketchWindow.class );
    sketchIntent.putExtra( TDTag.TOPODROID_SURVEY_ID, TDInstance.sid );
    sketchIntent.putExtra( TDTag.TOPODROID_SKETCH_NAME, name );
    startActivity( sketchIntent );
  }
/* END SKETCH_3D */

  // called either by a long-tap on plot button 
  //        or a highlights
  void startExistingPlot( String name, long type, String station ) // name = plot/sketch3d name
  {
    // TDLog.Log( TDLog.LOG_SHOT, "start Existing Plot \"" + name + "\" type " + type + " sid " + TDInstance.sid );
    if ( type != PlotInfo.PLOT_SKETCH_3D ) {
      PlotInfo plot1 =  mApp_mData.getPlotInfo( TDInstance.sid, name+"p" );
      if ( plot1 != null ) {
        TDInstance.setRecentPlot( name, type );
        PlotInfo plot2 =  mApp_mData.getPlotInfo( TDInstance.sid, name+"s" );
        startDrawingWindow( plot1.start, plot1.name, plot1.id, plot2.name, plot2.id, type, station, plot1.isLandscape() );
        return;
      } else {
        TDInstance.setRecentPlot( null, 0L );
      }
/* FIXME BEGIN SKETCH_3D */
    } else {
      Sketch3dInfo sketch = mApp_mData.getSketch3dInfo( TDInstance.sid, name );
      if ( sketch != null ) {
        startSketchWindow( sketch.name );
        return;
      }
/* END SKETCH_3D */
    }
    TDToast.make( R.string.plot_not_found );
  }

  private void startDrawingWindow( String start, String plot1_name, long plot1_id,
                                   String plot2_name, long plot2_id, long type, String station, boolean landscape )
  {
    if ( TDInstance.sid < 0 || plot1_id < 0 || plot2_id < 0 ) {
      TDToast.make( R.string.no_survey );
      return;
    }
    
    // notice when starting the DrawingWindow the remote device is disconnected 
    // FIXME mApp.disconnectRemoteDevice();
    
    Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingWindow.class );
    drawIntent.putExtra( TDTag.TOPODROID_SURVEY_ID, TDInstance.sid );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_NAME, plot1_name );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_NAME2, plot2_name );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_TYPE, type );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_FROM, start );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_TO, "" );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_AZIMUTH, 0.0f );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_CLINO, 0.0f );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_MOVE_TO, ((station==null)? "" : station) );
    drawIntent.putExtra( TDTag.TOPODROID_PLOT_LANDSCAPE, landscape );
    // drawIntent.putExtra( TDTag.TOPODROID_PLOT_ID, plot1_id ); // not necessary
    // drawIntent.putExtra( TDTag.TOPODROID_PLOT_ID2, plot2_id ); // not necessary

    startActivity( drawIntent );
  }

  // ---------------------------------------------------------------------------------

  public DBlock getNextBlankLegShot( DBlock blk )
  {
    DBlock ret = null;
    long id = 0;
    for ( int k=0; k<mDataAdapter.size(); ++k ) {
      DBlock b = mDataAdapter.get(k);
      if ( b == null ) return null;
      if ( b.isTypeBlank() ) {
        id = b.mId - 1;
        break;
      }
    }
    List<DBlock> list = mApp_mData.selectShotsAfterId( TDInstance.sid, id , 0 );
    for ( DBlock b : list ) {
      if ( b.isTypeBlank() ) {
        // Log.v( TopoDroidApp.TAG, "BLANK " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null && ret.isRelativeDistance( b ) ) return ret;
        ret = b;
      } else if ( b.isSecLeg() ) {
        // Log.v( TopoDroidApp.TAG, "LEG " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null &&  ret.isRelativeDistance( b ) ) return ret;
      } else {
        // Log.v( TopoDroidApp.TAG, "OTHER " + b.mLength + " " + b.mBearing + " " + b.mClino );
        ret = null;
      }
    }
    return null;
  }

  // get the next centerline shot and set mNextPos index
  public DBlock getNextLegShot( DBlock blk, boolean move_down )
  {
    // TDLog.Log( TDLog.LOG_SHOT, "getNextLegShot: pos " + mShotPos );
    if ( blk == null ) {
      // TDLog.Log( TDLog.LOG_SHOT, "   block is null");
      return null;
    }
    if ( move_down ) {
      mPrevPos = mShotPos;
      mShotPos = mNextPos;
      mNextPos = mPrevPos; // the old mShotPos;
    } else {
      mNextPos = mShotPos;
    }
	// OLD CODE
	// while ( mNextPos < mDataAdapter.size() && blk != mDataAdapter.get(mNextPos) ) ++ mNextPos;
    // ++ mNextPos; // one position after blk
    // while ( mNextPos < mDataAdapter.size() ) {
    //   DBlock b = mDataAdapter.get(mNextPos);
    //   int t = b.getBlockType();
    //   if ( t == DBlock.BLOCK_MAIN_LEG ) {
    //     return b;
    //   } else if (    DBlock.isTypeBlank( t )
    //               && mNextPos+1 < mDataAdapter.size()
    //               && b.isRelativeDistance( mDataAdapter.get(mNextPos+1) ) ) {
    //     return b;
    //   }
    //   ++ mNextPos;
    // }
    // NEW CODE
    for ( ; mNextPos < mDataAdapter.size(); ++ mNextPos ) {
      DBlock b = mDataAdapter.get(mNextPos);
      if ( b == null ) return null;
      if ( b == blk ) break;
    }
    // start at one position after blk
    for ( ++mNextPos; mNextPos < mDataAdapter.size(); ++mNextPos ) {
      DBlock b = mDataAdapter.get(mNextPos);
      if ( b == null ) return null;
      // int t = b.getBlockType();
      if ( b.isMainLeg() ) { // t == DBlock.BLOCK_MAIN_LEG 
        return b;
      } else if (    b.isTypeBlank( )
                  && mNextPos+1 < mDataAdapter.size()
                  && b.isRelativeDistance( mDataAdapter.get(mNextPos+1) ) ) {
        return b;
      }
    }
    return null;
  }

  // get the previous centerline shot and set the mPrevPos index
  public DBlock getPreviousLegShot( DBlock blk, boolean move_up )
  {
    // TDLog.Log( TDLog.LOG_SHOT, "getPreviousLegShot: pos " + mShotPos );
    if ( blk == null ) return null;
    if ( move_up ) {
      mNextPos = mShotPos;
      mShotPos = mPrevPos;
      mPrevPos = mNextPos; // the old mShotPos;
    } else {
      mPrevPos = mShotPos;
    }
	// OLD CODE
	// while ( mPrevPos >= 0 && blk != mDataAdapter.get(mPrevPos) ) -- mPrevPos;
    // while ( mPrevPos > 0 ) {
    //   -- mPrevPos;
    //   DBlock b = mDataAdapter.get(mPrevPos);
    //   if ( b.isMainLeg() ) {
    //     return b;
    //   }
    // }
    // NEW CODE
    for ( ; mPrevPos >= 0; -- mPrevPos ) {
      DBlock b = mDataAdapter.get(mPrevPos);
      if ( b == null ) return null;
      if ( b == blk ) break;
    }
    for ( --mPrevPos; mPrevPos >= 0; --mPrevPos ) {
      DBlock b = mDataAdapter.get(mPrevPos);
      if ( b == null || b.isMainLeg() ) {
        return b;
      }
    }
    /* if ( mPrevPos < 0 ) */ mPrevPos = 0;
    return null;
  }

  void updateShotDistanceBearingClino( float d, float b, float c, DBlock blk )
  {
    // Log.v("DistoX", "update shot DBC length " + d );
    mApp_mData.updateShotDistanceBearingClino( blk.mId, TDInstance.sid, d, b, c, true );
    blk.mLength  = d;
    blk.mBearing = b;
    blk.mClino   = c;
    mDataAdapter.updateBlockView( blk.mId );
  }

  void updateShotDepthBearingDistance( float p, float b, float d, DBlock blk )
  {
    // Log.v("DistoX", "update shot DBC length " + d );
    mApp_mData.updateShotDepthBearingDistance( blk.mId, TDInstance.sid, p, b, d, true );
    blk.mDepth   = p;
    blk.mBearing = b;
    blk.mLength  = d;
    mDataAdapter.updateBlockView( blk.mId );
  }

  // @param leg leg data-helper value (0 normal, 1 sec, 2 x-splay, 3 back, 4 h-splay, 5 v-splay
  void updateShot( String from, String to, int extend, float stretch, long flag, long leg, String comment, DBlock blk )
  {
    // TDLog.Log( TDLog.LOG_SHOT, "update Shot From >" + from + "< To >" + to + "< comment " + comment );
    // Log.v("DistoX", "update shot " + from + "-" + to + " leg " + leg + "/" + blk.getLegType() + " blk type " + blk.getBlockType() );
    blk.setBlockName( from, to, (leg == LegType.BACK) );

    int ret = mApp_mData.updateShot( blk.mId, TDInstance.sid, from, to, extend, flag, leg, comment, true );

    if ( ret == -1 ) {
      TDToast.make( R.string.no_db );
    // } else if ( ret == -2 ) {
    //   TDToast.make( R.string.makes_cycle );
    } else {
      // update same shots of the given block
      List< DBlock > blk_list = mApp_mData.selectShotsAfterId( blk.mId, TDInstance.sid, 0L );
      for ( DBlock blk1 : blk_list ) {
        if ( ! blk1.isRelativeDistance( blk ) ) break;
        mApp_mData.updateShotLeg( blk1.mId, TDInstance.sid, LegType.EXTRA, true );
      }
    }

    if ( leg == LegType.EXTRA ) {
      blk.setTypeSecLeg();
    } else if ( leg == LegType.BACK ) {
      blk.setTypeBackLeg();
    // } else if ( blk.isBackLeg() ) {
    //   blk.setTypeMainLeg();
    }

    DBlock blk3 = mDataAdapter.updateBlockView( blk.mId );
    if ( blk3 != blk && blk3 != null ) {
      blk3.setBlockName( from, to, (leg == LegType.BACK) );
      blk3.setExtend( extend, stretch );
      blk3.resetFlag( flag );
      blk3.mComment = comment;
      // FIXME if ( leg == LegType.EXTRA ) blk3.setBlockType( DBlock.BLOCK_SEC_LEG );
      mDataAdapter.updateBlockView( blk3.mId );
    }
  }

  /* FIXME_HIGHLIGHT
  // open the sketch and highlight block in the sketch
  // called by MultishotDialog
  void highlightBlocks( List<DBlock> blks )  // HIGHLIGHT
  {
    mApp.setHighlighted( blks );
    // Log.v("DistoX", "highlight blocks [0] " + ( (blks==null)? "null" : blks.size() ) );
    if ( blks == null || blks.size() == 0 ) return;
    // now if there is a plot open it
    if ( TDInstance.recentPlot != null ) {
      startExistingPlot( TDInstance.recentPlot, TDInstance.recentPlotType, blks.get(0).mFrom );
    }
    clearMultiSelect( );
  }
  */

  // open the sketch and highlight block in the sketch
  // called by MultishotDialog
  void colorBlocks( List<DBlock> blks, int color )  // HIGHLIGHT
  {
    // Log.v("DistoX", "highlight blocks [0] " + ( (blks==null)? "null" : blks.size() ) );
    if ( blks == null || blks.size() == 0 ) return;
    for ( DBlock blk : blks ) {
      blk.clearPaint();
      // mApp_mData.updateShotColor( blk.mId, TDInstance.sid, color, false ); // do not forward color
    }
    mApp_mData.updateShotsColor( blks, TDInstance.sid, color, false ); // do not forward color
    // if ( TDInstance.recentPlot != null ) {
    //   startExistingPlot( TDInstance.recentPlot, TDInstance.recentPlotType, blks.get(0).mFrom );
    // }
    clearMultiSelect( );
  }

  // called by MultishotDialog
  void renumberBlocks( List<DBlock> blks, String from, String to )  // RENUMBER SELECTED BLOCKS
  {
    DBlock blk = blks.get(0); // blk is guaranteed to exists
    if ( ! ( from.equals(blk.mFrom) && to.equals(blk.mTo) ) ) {
      blk.setBlockName( from, to, blk.isBackLeg() );
      mApp_mData.updateShotName( blk.mId, TDInstance.sid, from, to, true );
    }
    if ( blk.isLeg() ) {
      mApp.assignStationsAfter( blk, blks /*, stations */ );
      updateDisplay();
      // mList.invalidate();
    // } else if ( blk.isSplay() ) { // FIXME RENUMBER ONLY SPLAYS
    //   for ( DBlock b : blks ) {
    //     if ( b == blk ) continue;
    //     b.setBlockName( from, to );
    //     mApp_mData.updateShotName( b.mId, TDInstance.sid, from, to, true );
    //   }
    //   updateDisplay();
    } else {
      TDToast.make( R.string.no_leg_first );
    }
    clearMultiSelect( );
  }

  void swapBlocksName( List<DBlock> blks )  // SWAP SELECTED BLOCKS STATIONS
  {
    Log.v("DistoX", "swap list size " + blks.size() );
    for ( DBlock blk : blks ) {
      String from = blk.mTo;
      String to   = blk.mFrom;
      Log.v("DistoX", "swap block to <" + from + "-" + to + ">" );
      blk.setBlockName( from, to );
      // mApp_mData.updateShotName( blk.mId, TDInstance.sid, from, to, true );
    }
    mApp_mData.updateShotsName( blks, TDInstance.sid, true );
    updateDisplay();
  }

  /** bedding: solve Sum (A*x + B*y + C*z + 1)^2 minimum
   *   xx  xy  xz   A   -xn
   *   xy  yy  yz * B = -yn
   *   xz  yz  zz   C   -zn
   *
   *   det = xx * (yy * zz - yz^2) - xy * (xy * zz - xz * yz) + xz * (xy * yz - yy * xz) 
   *       = xx yy zz - xx yz^2 - yy xz^2 - zz xy^2 + 2 xy xz yz
   *  the DIP is downwards and 90 deg. right from the STRIKE, thus
   *    D x S = N
   *    S x N = D
   *    S = ( Z x N ) normalized
   *
   *          ^ Z
   *          |  Normal
   *          | /
   *          |/______ Strike
   *           \
   *            \ Dip
   *  The dip angle is measured from the horizontal plane
   *  The strike from the north
   */
  String computeBedding( List<DBlock> blks ) // BEDDING
  {
    String strike_dip = getResources().getString(R.string.few_data);
    if ( blks != null && blks.size() > 2 ) {
      DBlock b0 = blks.get(0);
      String st = b0.mFrom;
      float xx = 0, xy = 0, yy = 0, xz = 0, zz = 0, yz = 0, xn = 0, yn = 0, zn = 0;
      int nn = 0;
      if ( st != null && st.length() > 0 ) {
        for ( DBlock b : blks ) {
          if ( st.equals( b.mFrom ) ) {
            float h = TDMath.cosd(b.mClino);
            float z = TDMath.sind(b.mClino);
            float y = h * TDMath.cosd(b.mBearing);
            float x = h * TDMath.sind(b.mBearing);
            xx += x * x;
            xy += x * y;
            xz += x * z;
            yy += y * y;
            yz += y * z;
            zz += z * z;
            xn += x;
            yn += y;
            zn += z;
            ++ nn;
          }
        }
      } else {
        st = b0.mTo;
        if ( st != null && st.length() > 0 ) {
          for ( DBlock b : blks ) {
            if ( st.equals( b.mTo ) ) {
              float h = TDMath.cosd(b.mClino);
              float z = TDMath.sind(b.mClino);       // vertical upwards
              float y = h * TDMath.cosd(b.mBearing); // north
              float x = h * TDMath.sind(b.mBearing); // east
              xx += x * x;
              xy += x * y;
              xz += x * z;
              yy += y * y;
              yz += y * z;
              zz += z * z;
              xn += x;
              yn += y;
              zn += z;
	      ++ nn;
            }
          }
	}
      }
      if ( nn >= 3 ) {
	String strike_fmt = getResources().getString( R.string.strike_dip );
	String strike_regex = strike_fmt.replaceAll("%\\d\\$.0f", "\\-??\\\\d+"); 
	// Log.v("DistoX", "Strike regex: <<" + strike_regex + ">>");
        Matrix m = new Matrix( new Vector(xx, xy, xz), new Vector(xy, yy, yz), new Vector(xz, yz, zz) );
        Matrix minv = m.InverseT(); // m is self-transpose
        Vector n0 = new Vector( -xn, -yn, -zn );
        Vector n1 = minv.timesV( n0 ); // n1 = (a,b,c)
        if ( n1.z < 0 ) { n1.x = - n1.x; n1.y = - n1.y; n1.z = - n1.z; } // make N positive Z
        n1.normalize();
        // Log.v("DistoX", "Plane normal " + n1.x + " " + n1.y + " " + n1.z );

        // Vector z0 = new Vector( 0, 0, 1 );
        // Vector stk = z0.cross( n1 );  // strike = ( -n1.y, n1.x, 0 );
        // stk.normalized();
        // Vector dip = n1.cross( stk ); // dip
        // float astk = TDMath.atan2d( stk.x, stk.y ); // TDMath.atan2d( -n1.y, n1.x );
        // float adip = TDMath.acosd( dip.z ); // TDMath.asind( n1.z );
        float astk = TDMath.atan2d( -n1.y, n1.x );
        float adip = 90 - TDMath.asind( n1.z );
        strike_dip = String.format( strike_fmt, astk, adip );
        // TDToast.make( strike_dip );
        if ( b0.mComment != null && b0.mComment.length() > 0 ) {
	  if ( b0.mComment.matches( ".*" + strike_regex + ".*" ) ) {
	    // Log.v("DistoX", "Strike regex is contained");
            b0.mComment = b0.mComment.replaceAll( strike_regex, strike_dip );
	  } else {
            b0.mComment = b0.mComment + " " + strike_dip;
          }
  	} else {
          b0.mComment = strike_dip;
	}
	// Log.v("DistoX", "Comment <<" + b0.mComment + ">>");
	mApp_mData.updateShotComment( b0.mId, TDInstance.sid, b0.mComment, false ); // FIXME no forward
	if ( b0.mView != null ) b0.mView.invalidate();
      }
    }
    // clearMultiSelect( );
    // mList.invalidate();
    return strike_dip;
  }
  
  // NOTE called only by ShotDialog.saveDBlock() with to.length() == 0 ie to == "" and blk splay shot
  // update stations for all splay blocks with sme from as this block
  // @param extend   this block new extend
  // @param flag     this block new flag
  // ...
  // @param blk      this block
  //
  public void updateSplayShots( String from, String to, long extend, long flag, long leg, String comment, DBlock blk )
  {
    ArrayList< DBlock > splays = mDataAdapter.getSplaysAtId( blk.mId, blk.mFrom );
    for ( DBlock b : splays ) {
      if ( b.mId == blk.mId ) {
        blk.setBlockName( from, to );
        // FIXME leg should be LegType.NORMAL
        int ret = mApp_mData.updateShot( blk.mId, TDInstance.sid, from, to, extend, flag, leg, comment, true );

        if ( ret == -1 ) {
          TDToast.make( R.string.no_db );
        // } else if ( ret == -2 ) {
        //   TDToast.make( R.string.makes_cycle );
        // } else {
          // // update same shots of the given block: SHOULD NOT HAPPEN
          // List< DBlock > blk_list = mApp_mData.selectShotsAfterId( blk.mId, TDInstance.sid, 0L );
          // for ( DBlock blk1 : blk_list ) {
          //   if ( ! blk1.isRelativeDistance( blk ) ) break;
          //   mApp_mData.updateShotLeg( blk1.mId, TDInstance.sid, LegType.Extra, true );
          // }
        }
      } else {
        b.setBlockName( from, to );
        mApp_mData.updateShotName( b.mId, TDInstance.sid, from, to, true ); // FIXME use
	// mApp_mData.updateShotNames( splays, TDInstance.sid, false );
      }
      mDataAdapter.updateBlockView( b.mId );
    }
  }

  // ------------------------------------------------------------------------

  @Override
  public boolean onSearchRequested()
  {
    // TDLog.Error( "search requested" );
    Intent intent = new Intent( mActivity, TDPrefActivity.class );
    intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_CATEGORY_SURVEY );
    mActivity.startActivity( intent );
    return true;
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        onBackPressed();
        return true;
      case KeyEvent.KEYCODE_SEARCH:
        return onSearchRequested();
      case KeyEvent.KEYCODE_MENU:   // HARDWRAE MENU (82)
        UserManualActivity.showHelpPage( mActivity, getResources().getString( HELP_PAGE ));
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  // ---------------------------------------------------------

  private void setMenuAdapter( Resources res )
  {
    int k = 0;
    // HOVER
    // mMenuAdapter = new MyMenuAdapter( mActivity, this, mMenu, R.layout.menu, new ArrayList< MyMenuItem >() );
    mMenuAdapter = new ArrayAdapter<>(mActivity, R.layout.menu );

    mMenuAdapter.add( res.getString( menus[k++] ) );                                      // menu_survey
    mMenuAdapter.add( res.getString( menus[k++] ) );                                      // menu_close
    if ( TDLevel.overBasic  ) mMenuAdapter.add( res.getString( menus[k] ) ); k++; // menu_recover
    if ( TDLevel.overNormal ) mMenuAdapter.add( res.getString( menus[k] ) ); k++; // menu_photo  
    if ( TDLevel.overNormal ) mMenuAdapter.add( res.getString( menus[k] ) ); k++; // menu_sensor
    if ( TDLevel.overBasic  ) mMenuAdapter.add( res.getString( menus[k] ) ); k++; // menu_3d
    if ( TDLevel.overNormal && ! diving ) mMenuAdapter.add( res.getString( menus[k] ) ); k++; // menu_distox
    mMenuAdapter.add( res.getString( menus[k++] ) );  // menu_options
    mMenuAdapter.add( res.getString( menus[k++] ) );  // menu_help
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private boolean closeMenu()
  {
    if ( onMenu ) {
      mMenu.setVisibility( View.GONE );
      // HOVER
      // mMenuAdapter.resetBgColor();
      onMenu = false;
      return true;
    }
    return false;
  }


  void deletePlot( long pid1, long pid2 )
  {
    mApp_mData.deletePlot( pid1, TDInstance.sid );
    mApp_mData.deletePlot( pid2, TDInstance.sid );
    // FIXME NOTIFY
  }

  void recomputeItems( String st, int pos )
  {
    if ( mFlagSplay ) {
      if ( ! mShowSplay.remove( st ) ) {
        mShowSplay.add( st );
      }
      updateDisplay( );
      mList.setSelection( (pos>5)? pos-5 : 0 );
    }
  }

  private boolean showSplaysContains( String name ) 
  {
    for ( String st : mShowSplay ) {
      if ( st.equals( name ) ) return true;
    }
    return false;
  }


  public void setConnectionStatus( int status )
  { 
    if ( diving ) return;
    if ( TDInstance.device == null ) {
      // mButton1[ BTN_DOWNLOAD ].setVisibility( View.GONE );
      mButton1[BTN_DOWNLOAD].setBackgroundDrawable( mBMdownload_no );
      mButton1[BTN_BLUETOOTH].setBackgroundDrawable( mBMbluetooth_no );
    } else {
      // mButton1[ BTN_DOWNLOAD ].setVisibility( View.VISIBLE );
      switch ( status ) {
        case 1:
          mButton1[BTN_DOWNLOAD].setBackgroundDrawable( mBMdownload_on );
          mButton1[BTN_BLUETOOTH].setBackgroundDrawable( mBMbluetooth_no );
          break;
        case 2:
          mButton1[BTN_DOWNLOAD].setBackgroundDrawable( mBMdownload_wait );
          mButton1[BTN_BLUETOOTH].setBackgroundDrawable( mBMbluetooth_no );
          break;
        default:
          mButton1[BTN_DOWNLOAD].setBackgroundDrawable( mBMdownload );
          mButton1[BTN_BLUETOOTH].setBackgroundDrawable( mBMbluetooth );
      }
    }
  }

  public void enableBluetoothButton( boolean enable )
  {
    if ( diving ) return;
    mButton1[BTN_BLUETOOTH].setBackgroundDrawable( enable ? mBMbluetooth : mBMbluetooth_no );
    mButton1[BTN_BLUETOOTH].setEnabled( enable );
  }

  void renumberShotsAfter( DBlock blk )
  {
    // Log.v("DistoX", "renumber shots after " + blk.mLength + " " + blk.mBearing + " " + blk.mClino );
    // NEED TO FORWARD to the APP to change the stations accordingly
 
    List< DBlock > shots;
    // backsight and tripod seem o be OK
    // if ( StationPolicy.doTripod() || StationPolicy.doBacksight() ) {
    //   shots = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
    // } else {
      shots = mApp_mData.selectAllShotsAfter( blk.mId, TDInstance.sid, TDStatus.NORMAL );
    // }
    // Set<String> stations = mApp_mData.selectAllStationsBefore( blk.mId, TDInstance.sid, TDStatus.NORMAL );
    // Log.v("DistoX", "shots " + shots.size() );
    mApp.assignStationsAfter( blk, shots /*, stations */ );

    // DEBUG re-assign all the stations
    // List< DBlock > shots = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
    // mApp.assign Stations( shots );

    updateDisplay();
  }


  // merge this block to the following (or second following) block if this is a leg
  // if success update FROM/TO of the block
  long mergeToNextLeg( DBlock blk )
  {
    long id = mApp_mData.mergeToNextLeg( blk, TDInstance.sid, false );
    // Log.v("DistoX", "merge next leg: block " + blk.mId + " leg " + id );
    if ( id >= 0 && id != blk.mId ) {
      // mDataAdapter.updateBlockName( id, "", "" ); // name has already been updated in DB
      updateDisplay(); // FIXME change only block with id
    }
    return id;
  }

  // ------------------------------------------------------------------
  public void doProjectionDialog( String name, String start )
  {
    new ProjectionDialog( mActivity, this, TDInstance.sid, name, start ).show();
  }

  void doProjectedProfile( String name, String start, int azimuth )
  {
    makeNewPlot( name, start, false, azimuth );
  }

  void startAudio( DBlock blk )
  {
    (new AudioDialog( mActivity, /* this */ null, blk.mId )).show();
  }

}
