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
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
// import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFeedback;
import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDRequest;
import com.topodroid.utils.TDLocale;
import com.topodroid.utils.TDUtil;
// import com.topodroid.utils.TDVersion;
import com.topodroid.math.TDMatrix;
import com.topodroid.math.TDVector;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyTurnBitmap;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.Device;
import com.topodroid.dev.DeviceUtil;
import com.topodroid.dev.DataType;
import com.topodroid.common.PlotType;
import com.topodroid.common.LegType;
import com.topodroid.common.ExtendType;
import com.topodroid.calib.CalibInfo;
import com.topodroid.calib.CalibAlgo;
import com.topodroid.calib.CalibTransform;
import com.topodroid.packetX.MemoryData;

import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
// import java.util.Stack;
import java.util.Locale;

import android.os.Bundle;
import android.os.Handler;
// /* fixme-23 */
// import java.lang.reflect.Method;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Configuration;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;

import android.view.View;
// import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.EditText;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;


// import android.provider.MediaStore;
// import android.provider.Settings.System;

// import android.graphics.Canvas;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

// import android.net.Uri;

import java.io.FileOutputStream;
import java.io.IOException;

import java.util.TreeSet;

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
  private final static String TAG = "SHOT ";
  private final static boolean LOG = false;

  static boolean mWaitPlot = false; // global flag to prevent opening a plot

  final static private int BTN_DOWNLOAD  = 0;
  final static private int BTN_BLUETOOTH = 1;
  final static private int BTN_PLOT      = 3;
  final static private int BTN_MANUAL    = 5;
  final static private int BTN_AZIMUTH   = 6;
  final static private int BTN_SEARCH    = 8;
  private int boff = 0;
  private int mBTstatus; // status of bluetooth buttons (download and reset)

  private DataHelper mApp_mData;

  private static final int[] izons = {
                        R.drawable.iz_download,
                        R.drawable.iz_bt,
                        R.drawable.iz_mode,
                        R.drawable.iz_plot,
                        R.drawable.iz_note,
                        R.drawable.iz_plus,
                        R.drawable.iz_dial,
                        R.drawable.iz_station,
                        R.drawable.iz_search,
			R.drawable.iz_refresh
			// R.drawable.iz_empty // EMPTY
                      };

  private static final int[] izonsno = {
                        0,
                        0,
                        0,
                        R.drawable.iz_plot, // TODO_IZ
                        0,
                        0,
                        0
                      };

  private static final int[] izonsF = {
                        R.drawable.iz_ignore,     // extend ignore
                        R.drawable.iz_left,       // extend LEFT
                        R.drawable.iz_flip,       // extend flip
                        R.drawable.iz_right,      // extend RIGHT
                        R.drawable.iz_highlight,  // multishot dialog: includes // highlight in sketch
			  // R.drawable.iz_numbers_no, // renumber shots (first must be leg)
                          // R.drawable.iz_bedding,    // compute bedding
                        R.drawable.iz_delete,     // cut shots 
                        R.drawable.iz_copy,       // copy shots 
                        R.drawable.iz_clear       // cancel
			// R.drawable.iz_empty // EMPTY
                      };
  private static final int BTN_MULTISHOT = 4; // index of iz_highlight
  private static final int BTN_COPY      = 6; // index of iz_copy

  private static final int[] menus = { // menu labels
                        R.string.menu_close,
                        R.string.menu_survey,
                        R.string.menu_recover,
                        R.string.menu_photo,
                        R.string.menu_audio,
                        R.string.menu_sensor,
                        R.string.menu_3d,
                        R.string.menu_device,
                        R.string.menu_options,
                        R.string.menu_help,
                        R.string.menu_recover_paste  // 10: extra labels
                     };

  private static final int[] help_icons = {
                          R.string.help_download,
                          R.string.help_remote,
                          R.string.help_display,
                          R.string.help_plot,
                          R.string.help_note,
                          R.string.help_add_shot,
                          R.string.help_azimuth,
                          R.string.help_current_station,
                          R.string.help_search,
			  R.string.help_refresh,
                        };
   private static final int[] help_menus = {
                          R.string.help_close,
                          R.string.help_survey_info,
                          R.string.help_undelete,
                          R.string.help_photo,
                          R.string.help_audio,
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
  private SurveyAccuracy mSurveyAccuracy;
  // private int mNrDevices = 0;

  private static DBlockBuffer mDBlockBuffer = new DBlockBuffer(); // survey-data buffer (created on first call)

  // TODO replace flags with DisplayMode-flag 
  //      N.B. id is in the data adapter
  private boolean mFlagSplay  = true;  //!< whether to hide splay shots
  private boolean mFlagLatest = false; //!< whether to show the latest splay shots
  private boolean mFlagLeg    = true;  //!< whether to hide leg extra shots
  private boolean mFlagBlank  = false; //!< whether to hide blank shots

  boolean isFlagSplay()  { return mFlagSplay; }
  boolean isFlagLatest() { return mFlagLatest; }
  boolean isFlagLeg()    { return mFlagLeg; }
  boolean isFlagBlank()  { return mFlagBlank; }
  boolean isShowIds()    { return mDataAdapter.show_ids; }

  /** @return false if all shots are displayed
   */
  private boolean testFlagSplayLegBlank() 
  { 
    // TDLog.v("Flags splay " + mFlagSplay + " leg " + mFlagLeg + " blank " + mFlagBlank );
    return mFlagSplay || mFlagLeg || mFlagBlank; 
  }

  // void setShowIds( boolean show ) { mDataAdapter.show_ids = show; }

  void setFlags( boolean ids, boolean splay, boolean latest, boolean leg, boolean blank )
  {
    mDataAdapter.show_ids = ids;
    mFlagSplay = splay;
    if ( TDSetting.mShotRecent ) mFlagLatest = latest;
    mFlagLatest = latest;
    mFlagLeg    = leg;
    mFlagBlank  = blank;
    saveInstanceToData();
    // TDLog.v("update display : set flags");
    updateDisplay();
  }



  // private Bundle mSavedState = null;
  // private String mRecentPlot     = null; // moved to TDInstance
  // long   mRecentPlotType = PlotType.PLOT_PLAN;

  private int mButtonSize = 42; // used only by the DialButton
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
  // private static long    mPhotoId;
  // private static String  mPhotoComment;
  // private static int     mPhotoCamera = PhotoInfo.CAMERA_UNDEFINED;
  private static long    mShotId;   // photo/sensor shot id, old shot id
  boolean mOnOpenDialog = false;
  MediaManager mMediaManager;

  // ConnHandler mHandler;

  private Button[] mButtonF;
  private int mNrButtonF = 8;

  // private Button mButtonHelp;
  private MyHorizontalListView mListView;
  private MyHorizontalButtonView mButtonView1;
  private MyHorizontalButtonView mButtonViewF;
  private ListView   mMenu = null;
  private Button     mMenuImage;
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
  Bitmap mBMdial; // FIXME_AZIMUTH_DIAL
  // Bitmap mBMdial_transp;
  private MyTurnBitmap mDialBitmap;

  private BitmapDrawable mBMplot_no;
  private BitmapDrawable mBMleft;
  private BitmapDrawable mBMright;
  // private SearchResult mSearch = null;

  /** @return true if the shot AMD are out-of-threshold from the survey average AMD
   * @param blk  shot data (guatanteed not null)
   */
  boolean isBlockMagneticBad( DBlock blk ) { return /* (mNrDevices == 1) && */ mSurveyAccuracy.isBlockAMDBad( blk ); }

  /** @return the string with how much the data differ from the means
   * @param blk  shot data (guatanteed not null)
   */
  String getBlockExtraString( DBlock blk ) { return /* (mNrDevices != 1) ? "" : */ mSurveyAccuracy.getBlockExtraString( blk ); }

  // --------------------------------------------------------------------------------
  // get a button-1
  // @param idx    index of the button-1
  // private Button button1( int idx ) { return mButton1[ idx - boff ]; }

  /** check if a button is a given button-1
   * @param b    button
   * @param idx  index of button-1
   * @return true if the button is the expected button-1
   */
  private boolean isButton1( Button b, int idx ) { return idx - boff < mNrButton1 && b == mButton1[ idx - boff ]; }

  // --------------------------------------------------------------------------------
  /** set the reference azimuth
   * @param azimuth  reference azimuth [degrees]
   * @param fixed_extend   whether "extend" is fixed (-1 LEFT, 1 RIGHT) or not (0)
   */
  public void setRefAzimuth( float azimuth, long fixed_extend )
  {
    // TDLog.v( TAG + "set Ref Azimuth " + TDAzimuth.mFixedExtend + " -> " + fixed_extend + " azimuth " + azimuth );
    TDAzimuth.mFixedExtend = fixed_extend;
    TDAzimuth.mRefAzimuth  = azimuth;
    setRefAzimuthButton();
  }

  public void setRefAzimuthButton()
  {
    if ( ! TDLevel.overNormal ) return;
    int btn = BTN_AZIMUTH - boff; // button index
    if ( btn >= mNrButton1 ) return;
    // TDLog.v( TAG + "set Ref Azimuth button, manual: " + TDSetting.mAzimuthManual + " fixed: " + TDAzimuth.mFixedExtend + " azimuth: " + TDAzimuth.mRefAzimuth );

    // The ref azimuth can be fixed either by the setting or by the choice in the azimuth dialog 
    if ( ( ! TDSetting.mAzimuthManual ) && TDAzimuth.mFixedExtend == 0 ) { // FIXME FIXED_EXTEND 20240603 the mFixedExtend test was commented
      int extend = (int)(TDAzimuth.mRefAzimuth);
      Bitmap bm2 = mDialBitmap.getBitmap( extend, mButtonSize );
      TDandroid.setButtonBackground( mButton1[ btn ], new BitmapDrawable( getResources(), bm2 ) );
      TopoDroidApp.setSurveyExtend( extend );
    } else if ( TDAzimuth.mFixedExtend == -1L ) {
      TDandroid.setButtonBackground( mButton1[ btn ], mBMleft );
      TopoDroidApp.setSurveyExtend( SurveyInfo.SURVEY_EXTEND_LEFT );
    } else {
      if ( TDAzimuth.mFixedExtend == 0 ) TDAzimuth.mFixedExtend = 1; // set "right"
      TDandroid.setButtonBackground( mButton1[ btn ], mBMright );
      TopoDroidApp.setSurveyExtend( SurveyInfo.SURVEY_EXTEND_RIGHT );
    } 
  }

  TopoDroidApp getApp() { return mApp; }

  Set<String> getStationNames() { return mApp_mData.selectAllStations( TDInstance.sid ); }

  // -------------------------------------------------------------------
  // FIXME ok only for numbers
  // String getNextStationName()
  // {
  //   return mApp_mData.getNextStationName( TDInstance.sid );
  // }

  @Override
  public void refreshDisplay( int nr, boolean toast ) 
  {
    // TDLog.v( TAG + "refresh display " + nr );
    setConnectionStatus( mDataDownloader.getStatus() );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        // mLastShotId = mApp_mData.getLastShotId( TDInstance.sid );
        // TDLog.v("update display : refresh display");
        updateDisplay( );
      }
      if ( toast ) {
        if ( TDInstance.isDeviceX310() ) nr /= 2;
        TDToast.make( getResources().getQuantityString(R.plurals.read_data, nr, nr ) );
        // TDToast.make( " read_data: " + nr );
      }
    } else { // ( nr < 0 )
      if ( toast ) {
        if ( nr <= -5 ) {
          TDToast.makeBad( getString(R.string.read_fail_with_code) + nr );
	} else {
          TDToast.makeBad( mApp.DistoXConnectionError[ -nr ] );
        }
      }
    }
  }

  private List< DBlock > mMyBlocks = null;
  private List< PhotoInfo > mMyPhotos = null;
    
  /** update the display 
   * @return 0: shot list not refreshed, 1: list refreshed but display not updated, 2 list refreshed and display updated
   *        
   */
  int updateDisplay( )
  {
    int ret = 0;
    // highlightBlocks( null );
    if ( mApp_mData != null && TDInstance.sid >= 0 ) {
      mMyBlocks = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
      mSurveyAccuracy = new SurveyAccuracy( mMyBlocks ); 
      // mNrDevices = mApp_mData.getCountDevices( TDInstance.sid );
      // if ( mMyBlocks.size() > 4 ) SurveyAccuracy.setBlocks( mMyBlocks );
      mMyPhotos = mApp_mData.selectAllPhotosShot( TDInstance.sid, TDStatus.NORMAL );
      if ( ! mDataAdapter.isMultiSelect() ) { // FIXME 2024-11-15 check if causes errors
        // TDLog.v("update display updates shot list: " + mMyBlocks.size() );
        updateShotList( mMyBlocks, mMyPhotos );
        // mSurveyAccuracy.debug();
        ret = 2;
      } else {
        // TDLog.v("update display updates shot list: " + mMyBlocks.size() + " adapter is multiselect" );
        ret = 1;
      }
      setTheTitle( );
    } else {
      mApp.clearSurveyReferences();
      doFinish();
      // TDToast.makeWarn( R.string.no_survey );
    }
    return ret;
  }

  public void setTheTitle()
  {
    StringBuilder sb = new StringBuilder();
    if ( TDSetting.isConnectionModeMulti() /* || TDSetting.isConnectionModeDouble() */ ) {
      sb.append( "{" );
      if ( TDInstance.getDeviceA() != null ) sb.append( TDInstance.getDeviceA().getNickname() );
      sb.append( "} " );
    }
    // sb.append( mApp.getConnectionStateTitleStr() ); // IF_COSURVEY
    sb.append( TDInstance.survey );

    if ( onMultiselect ) {
      sb.append( String.format( getResources().getString( R.string.select_size ), mDataAdapter.getMultiSelectSize() ) );
    }

    if ( TDSetting.WITH_IMMUTABLE && ! TDInstance.isSurveyMutable ) { // IMMUTABLE
      mActivity.setTitleColor( 0xffff3333 );
    } else {
      setTitleColor( StationPolicy.mTitleColor );
    }
    mActivity.setTitle( sb.toString() );
  }

  /** @return true if the "current station" name is the specified name
   * @param st   specified name
   */
  boolean isCurrentStationName( String st ) { return mApp.isCurrentStationName( st ); }

  /** set the name of the "current station"
   * @param st   "current station" name
   * @return true if successful (?)
   */
  boolean setCurrentStationName( String st ) 
  { 
    mSkipItemClick = true;
    return mApp.setCurrentStationName( st ); 
    // updateDisplay( );
  }

  /** udate block list AMD with a new block
   * @param blk_id   ID of the new block
   * @note only X2 and XBLE
   */
  @Override
  synchronized public void updateBlockAMDList( long blk_id )
  {
    assert( TDInstance.deviceType() == Device.DISTO_X310 || TDInstance.deviceType() == Device.DISTO_XBLE  );
    DBlock blk = mApp_mData.selectTheShot( blk_id, TDInstance.sid );
    if ( blk == null ) {
      // TDLog.v( TAG + "data null block");
      return;
    }
    if ( ! blk.isScan() ) { // normal data
      mSurveyAccuracy.addBlockAMD( blk );
      mDataAdapter.updateBlockAMD( blk ); // copy blk AMD to the block in the adapter
      // TDLog.v( "update block AMD " + ret );
      mList.post( new Runnable() {
        @Override public void run() {
          // TDLog.v( "list runnable: notify data set AMD changed " + mDataAdapter.getCount() );
          // if ( TDSetting.mBlunderShot )  mDataAdapter.dropBlunders(); // BLUNDER uncomment to drop the blunder from the shot list immediately
          mDataAdapter.notifyDataSetChanged(); // THIS IS IMPORTANT TO REFRESH THE DATA LIST
          mList.setSelection( mDataAdapter.getCount() - 1 );
          refreshShotViews();
        }
      } );
      // mList.invalidate();
    }
  }

  /** add a block to the adapter (ILister interface)
   * @param blk_id   block ID
   * @note called by the RFcommThread after receiving a data packet
   *       this is the only place where assignStationsAll is called: synchronize it
   */
  @Override
  synchronized public void updateBlockList( long blk_id )
  {
    // TDLog.v( "shot window: update block list " + blk_id );
    DBlock blk = mApp_mData.selectTheShot( blk_id, TDInstance.sid );
    if ( blk == null || mDataAdapter == null ) {
      // TDLog.v( TAG + "data null block");
      return;
    }
    if ( TDLog.isStreamFile() ) {
      TDLog.e( TAG + TDLog.threadId() + " update block list. Id: " + blk_id + " is scan: " +  blk.isScan() );
    }
    // FIXME 3.3.0
    boolean add_block = mDataAdapter.addDataBlock( blk ); // avoid double block-adding
    if ( add_block ) {
      boolean scan = blk.isScan();
      boolean ret = false;
      if ( ! scan ) { // normal data
        // 2025-11-04 moved to updateBlockAMDList for X2 and XBLE
        if ( TDInstance.deviceType() == Device.DISTO_CAVWAYX1 ) {
          mSurveyAccuracy.addBlockAMD( blk );
        }

        // mNrDevices = mApp_mData.getCountDevices( TDInstance.sid );
        if ( StationPolicy.doBacksight() || StationPolicy.doTripod() ) {
          // FIXME UNTESTED it was: ret = mApp.assignStationsAll( mDataAdapter.getItems( ) ); 
          ret = mApp.assignStationsAll( mDataAdapter.getItemsForAssign( 2 ) ); // from the 2-nd last leg
        } else {
          ret = mApp.assignStationsAll( mDataAdapter.getItemsForAssign( 1 ) ); // from 1-st last leg
        }
        // mApp_mData.getShotName( TDInstance.sid, blk );
        // TDLog.v( TAG + "data shot window block " + blk.mId + " station assign return " + ret );
      }

      if ( ! ( TDInstance.deviceType() == Device.DISTO_X310 || TDInstance.deviceType() == Device.DISTO_XBLE  ) ) {
        mList.post( new Runnable() {
          @Override public void run() {
            // TDLog.v( "list runnable: notify data set changed " + mDataAdapter.getCount() );
            // if ( TDSetting.mBlunderShot )  mDataAdapter.dropBlunders(); // BLUNDER uncomment to drop the blunder from the shot list immediately
            mDataAdapter.notifyDataSetChanged(); // THIS IS IMPORTANT TO REFRESH THE DATA LIST
            mList.setSelection( mDataAdapter.getCount() - 1 );
            refreshShotViews();
          }
        } );
      }
      // mList.invalidate();
      // mDataAdapter.reviseLatest();
      if ( ret || scan ) { // always update when a leg is received 
        // TDLog.v( TAG + "data shot window got a leg. ret " + ret );
        TopoDroidApp.notifyDrawingUpdateDisplay( blk_id, ret );
      } else if ( ! StationPolicy.isSurveyBackward1() ) {
        if ( ! TDLevel.overExpert || ! TDSetting.mLegOnlyUpdate ) {
          // TDLog.v( TAG + "data notify update drawing");
          TopoDroidApp.notifyDrawingUpdateDisplay( blk_id, ret );
        }
      }
    // } else {
    //   if ( TDLog.isStreamFile() ) TDLog.e( "block " + blk_id + " already on the list");
    }
  }

  /** update the list of shot data with the photo infos
   * @param list    list of shot data
   * @param photos  list of photos
   * first clear the data adapter, then fill it with the shot data, finally with the photo infos
   * @note called only by updateDisplay
   */
  private void updateShotList( List< DBlock > list, List< PhotoInfo > photos )
  {
    if ( list == null ) return;
    // TDLog.v( "update shot list: shots " + list.size() + " photos " + photos.size() );
    mDataAdapter.clear();
    // mList.setAdapter( mDataAdapter );
    if ( TDUtil.isEmpty(list) ) {
      // TDToast.makeWarn( R.string.no_shots );
      return;
    }
    processShotList( list );
    if ( photos != null && ! photos.isEmpty() ) {
      mDataAdapter.reviseBlockWithPhotos( photos );
    }
  }

  /** process the list of shot data and fill the adapter for the display list
   * @param list    list of shot data
   * this is a bit of overkill because the shot leg type are updated in the database
   * @note called only by updateShotList
   */
  private void processShotList( List< DBlock > list )
  {
    int cnt = 0;
    DBlock prev = null;
    boolean prev_is_leg = false;
    boolean check_recent = TDSetting.mShotRecent && mFlagLatest;
    for ( DBlock item : list ) {
      DBlock cur = item;
      // int t = cur.type();
      // TDLog.Log( TDLog.LOG_SHOT, "item " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );

      if ( cur.isSecLeg() || cur.isRelativeDistance( prev ) ) {
        // TDLog.v( TAG + "item close " + cur.type() + " " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
        if ( cur.isBlank() ) {   // FIXME 20140612
          cur.setTypeSecLeg();
          mApp_mData.updateShotLeg( cur.mId, TDInstance.sid, LegType.EXTRA ); // cur.mType ); // FIXME 20140616
        }
        else if ( ! cur.isSecLeg() ) { // FIXME 20201118
          // if ( prev != null && prev.isBlank() ) prev.setBlockLegType( DBlock.BLOCK_BLANK_LEG );
          if ( prev != null ) prev.setTypeBlankLeg();
        }

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
              ++cnt;
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
        // TDLog.v( TAG + "item not close " + cur.type() + " " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
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
            if ( ! ( showSplaysContains( cur.mFrom ) || ( check_recent && cur.isRecent() ) ) ) continue;
          }
        } else { // cur.isMainLeg() // t == DBlock.BLOCK_MAIN_LEG
          prev = cur;
        }
      }
      // TDLog.Log( TDLog.LOG_SHOT, "adapter add " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      // TDLog.v( TAG + "adapter add " + cur.mLength + " " + cur.mBearing + " " + cur.mClino );
      ++cnt;
      mDataAdapter.add( cur );
    }
    // TDLog.v( "process shot list added " + cnt );
  }

  // ---------------------------------------------------------------
  // list items click

  private boolean mSkipItemClick = false;

  /** begin multiselection at a given position
   * @param pos   item position
   * @note the multiselection is closed if it contains only the given position
   */
  private void multiSelect( int pos, boolean long_tap )
  {
    // TDLog.v( TAG + "multiselect " + pos );
    if ( mDataAdapter.multiSelect( pos, long_tap ) ) {
      mListView.setAdapter( mButtonViewF.mAdapter );
      mListView.invalidate();
      onMultiselect = true;
      // FIXME this should not be necessary but it is (???)
      // if ( pos == 0 ) mDataAdapter.notifyDataSetChanged(); // THIS IS USED TO REFRESH THE DATA LIST
    } else {
      mListView.setAdapter( mButtonView1.mAdapter );
      mListView.invalidate();
      onMultiselect = false;
    }
    mDataAdapter.notifyDataSetChanged(); // THIS IS USED TO REFRESH THE DATA LIST
    setTheTitle();
  }

  void addOffset( float offset )
  {
    mDataAdapter.addOffset( mApp_mData, offset );
    clearMultiSelect();
  }

  /** close the multiselection
   */
  void clearMultiSelect( )
  {
    // mApp.setHighlighted( null ); // FIXME_HIGHLIGHT
    mDataAdapter.clearMultiSelect( );
    mListView.setAdapter( mButtonView1.mAdapter );
    mListView.invalidate();
    onMultiselect = false;
    setTheTitle();
  }

  /** react to a user tap on an item (menu entry)
   * @param parent   ...
   * @param view     tapped view
   * @param pos      item position
   * @param id       ...
   */
  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TDLog.v( "shot window: on item click pos " + pos );
    if ( CutNPaste.dismissPopupBT() ) return;
    if ( mSkipItemClick ) {
      mSkipItemClick = false;
      return;
    }
    if ( parent != null && mMenu == (ListView)parent ) {
      handleMenu( pos );
      return;
    }
    if ( closeMenu() ) return;
  }

  /** implement user tap on a shot data
   * @param view  tapped view
   * @param pos   item position
   *
   * if it is in "multiselection" add the item to the multiselection,
   * otherwise perform an "item click"
   */
  public void itemClick( View view, int pos )
  {
    // TDLog.v( "shot window: item click pos " + pos );
    if ( mDataAdapter.isMultiSelect() ) {
      if ( ! TDInstance.isSurveyMutable ) {  // IMMUTABLE
        TDToast.makeWarn("Immutable survey");
        clearMultiSelect();
        return;
      }
      multiSelect( pos, false );
      return;
    }
    DBlock blk = mDataAdapter.get(pos);
    if ( blk != null ) onBlockClick( blk, pos );
  }

  /** perform a user tap on a shot data
   * @param blk   item data-block
   * @param pos   item position
   */
  void onBlockClick( DBlock blk, int pos )
  {
    // TDLog.v( "shot window: block click pos " + pos );
    if ( mOnOpenDialog ) return;
    mOnOpenDialog = true;
    mShotPos = pos;
    DBlock prevBlock = null;
    DBlock nextBlock = null;
    prevBlock = getPreviousLegShot( blk, false );
    nextBlock = getNextLegShot( blk, false );
    (new ShotEditDialog( mActivity, this, pos, blk, prevBlock, nextBlock )).show();
  }

  /** react to a user long-tap on an item (menu entry)
   * @param parent   ...
   * @param view     tapped view
   * @param pos      item position
   * @param id       ...
   */
  @Override 
  public boolean onItemLongClick(AdapterView<?> parent, View view, int pos, long id)
  {
    // TDLog.v( "shot window: on item long click " + pos );
    if ( closeMenu() ) return true;
    if ( CutNPaste.dismissPopupBT() ) return true;
    return false;
  }

  /** implement a user long-tap of a shot data item: start a multiselection
   * @param view     tapped view
   * @param pos      item position
   * @return true if entered multiselection
   */
  public boolean itemLongClick( View view, int pos )
  {
    // TDLog.v( "shot window: item long click " + pos );
    if ( ! TDInstance.isSurveyMutable ) {  // IMMUTABLE
      // TDToast.makeWarn("Immutable survey");
      return false;
    }
    DBlock blk = mDataAdapter.get(pos);
    if ( blk == null ) return false;
    // onBlockLongClick( blk );
    // if ( blk.isSplay() ) {
    //   highlightBlocks( blk );
    // } else {
      multiSelect( pos, true );
    // }
    return true;
  }

  // FIXME_X2_SPLAY
  // @param leg0   old leg type
  // @param leg1   new leg type
  void updateSplayLeg( int pos, long leg0, long leg1 ) // pos = mDataAdapter pos
  {
    // TDLog.v( TAG + "toggle splays " + leg0 + " -> " + leg1 );
    DBlock blk = mDataAdapter.get(pos);
    if ( blk == null || ! blk.isSplay() ) return;
    do {
      // TDLog.v( TAG + "toggle splay type " + pos + " is splay " + blk.isSplay() + " leg " + blk.getLegType() );
      blk.setBlockLegType( (int)leg1 );
      mApp_mData.updateShotLeg( blk.mId, TDInstance.sid, leg1 );
      mDataAdapter.updateBlockView( blk.mId );
      if ( (--pos) < 0 ) break;
      blk = mDataAdapter.get(pos);
    } while ( blk != null && blk.isSplay() && blk.getLegType() == leg0 );
    // TDLog.v("update display : update splay leg");
    updateDisplay();
  }

  // assign splay classes traversing backwards: expected
  //   V-splays from +/-90 to -/+90 ... to +/-90
  //   H-splays all in [-30, +30]
  //   X-splays from +/-90 (to -/+90 and again to +/-90)
  void setSplayClasses( int pos )
  {
    DBlock blk = null;
    do {
      if ( (--pos) < 0 ) return;
      blk = mDataAdapter.get(pos);
    } while ( blk != null && ! blk.isSplay() );
    if ( blk == null ) return;

    int flip = 0;
    int sign = 0;
    long leg1 = LegType.VSPLAY;
    do {
      if ( ! blk.isSplay() ) break;
      switch ( flip ) {
        case 0:
          if ( Math.abs(blk.mClino) < 60 ) { flip = 1; }
          else { sign = ( blk.mClino > 0 ) ? -1 : 1; }
          break;
        case 1:
          if ( sign * blk.mClino > 60 ) { flip = 2; sign = -sign; }
          break;
        case 2:
          if ( sign * blk.mClino > 60 ) { flip = 3; }
          break;
        case 3:
          if ( Math.abs(blk.mClino) < 30 ) { flip = 4; leg1 = LegType.HSPLAY; }
          break;
        case 4:
          if ( Math.abs(blk.mClino) > 60 ) { flip = 5; leg1 = LegType.XSPLAY; }
          break;
      }
      // TDLog.v( TAG + "toggle splay type " + pos + " is splay " + blk.isSplay() + " leg " + blk.getLegType() );
      blk.setBlockLegType( (int)leg1 );
      mApp_mData.updateShotLeg( blk.mId, TDInstance.sid, leg1 );
      mDataAdapter.updateBlockView( blk.mId );
      if ( (--pos) < 0 ) break;
      blk = mDataAdapter.get(pos);
    } while ( blk != null && blk.isSplay() );

    // TDLog.v("update display : set splay class");
    updateDisplay();
  }

  // FIXME_X3_SPLAY from ShotEditDialog
  /** update the leg-type and block-type of a splay
   * @param blk   data block
   * @param leg1  new leg type
   */
  void updateSplayLegType( DBlock blk, long leg1 )
  {
    // TDLog.v( TAG + "update splay " + blk.mId + " leg type " + leg1 );
    mApp_mData.updateShotLeg( blk.mId, TDInstance.sid, leg1 );
    // int block_type = DBlock.blockOfSplayLegType[ (int)leg1 ];
    // // long leg_type = DBlock.legOfBlockType[ block_type ];
    // blk.setBlockType( block_type ); // FIXME 20240716
    blk.setBlockLegType( (int)leg1 );
    // TDLog.v("update display : update splay leg type");
    updateDisplay();
  }

  // from MultiselectDialog
  void updateSplaysLegType( List< DBlock > blks, int leg_type, long flag )
  {
    // int block_type = DBlock.blockOfSplayLegType[ leg_type ];
    for ( DBlock blk : blks ) {
      // long leg_type = DBlock.legOfBlockType[ block_type ];
      long f = blk.resetFlag( flag ); // n.b. keep cavway bits
      blk.setBlockLegType( leg_type );
      // blk.setBlockType( block_type ); // FIXME 20240716
      mApp_mData.updateShotLegFlag( blk.mId, TDInstance.sid, leg_type, f );
    }
    // TDLog.v("update display : update splays leg type");
    updateDisplay();
    clearMultiSelect();
  }

  // called by ShotEditDialog "More" button
  // void onBlockLongClick( DBlock blk )
  // {
  //   mShotId = blk.mId; // save shot id
  //   if ( TDLevel.overNormal ) {
  //     (new ShotEditMoreDialog(mActivity, this, blk ) ).show();
  //   } else {
  //     (new ShotDeleteDialog( mActivity, this, blk ) ).show();
  //   }
  // }

  // ----------------------------------------------------------------------------
  // MENU

  private void handleMenu( int pos )
  {
    closeMenu();
    int p = 0;
    if ( p++ == pos ) { // CLOSE
      // TDLog.v("Shot activity close");
      // if ( TDSetting.mDataBackup ) TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false, false ); // try_save
      // new DataStopTask( mApp, this, mDataDownloader ).execute(); // 20251217
      TopoDroidApp.mShotWindow = null;
      super.onBackPressed();

    } else if ( p++ == pos ) { // SURVEY ACTIVITY
      Intent intent = new Intent( this, SurveyWindow.class );
      intent.putExtra( TDTag.TOPODROID_SURVEY,  0 ); // mustOpen 
      intent.putExtra( TDTag.TOPODROID_OLDSID, -1 ); // old_sid 
      intent.putExtra( TDTag.TOPODROID_OLDID,  -1 ); // old_id 
      startActivityForResult( intent, TDRequest.INFO_ACTIVITY_SHOTWINDOW );
    // } else if ( TDLevel.overBasic && p++ == pos ) { // CURRENT STATION
    //   (new CurrentStationDialog( this, this, mApp, mApp.getCurrentOrLastStation() )).show();

    } else if ( TDLevel.overBasic && p++ == pos ) { // RECOVER
      List< DBlock > shots1 = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.DELETED );
      List< DBlock > shots2 = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.OVERSHOOT );
      List< DBlock > shots3 = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.CHECK );
      List< DBlock > shots4 = (TDLevel.overExpert && TDSetting.mBlunderShot)? mApp_mData.selectAllShots( TDInstance.sid, TDStatus.BLUNDER ) : null;
      List< PlotInfo > plots   = mApp_mData.selectAllPlots( TDInstance.sid, TDStatus.DELETED );
      // the list of deleted plots contains an even number of items: plan-profile pairs
      if (  TDLevel.overAdvanced ) {
        if ( TDUtil.isEmpty(shots1) && TDUtil.isEmpty(shots2) && TDUtil.isEmpty(shots3) && TDUtil.isEmpty(shots4) && TDUtil.isEmpty(plots) && mDBlockBuffer.size() == 0 ) {
          TDToast.makeWarn( R.string.no_undelete_paste );
        } else {
          (new UndeleteDialog(mActivity, this, mApp_mData, TDInstance.sid, shots1, shots2, shots3, shots4, plots, mDBlockBuffer ) ).show();
        }
      } else {
        if ( TDUtil.isEmpty(shots1) && TDUtil.isEmpty(shots2) && TDUtil.isEmpty(shots3) && TDUtil.isEmpty(shots4) && TDUtil.isEmpty(plots) ) {
          TDToast.makeWarn( R.string.no_undelete );
        } else {
          (new UndeleteDialog(mActivity, this, mApp_mData, TDInstance.sid, shots1, shots2, shots3, shots4, plots, null ) ).show();
        }
      }
      // updateDisplay( );
    } else if ( TDLevel.overNormal && p++ == pos ) { // PHOTO
      // mActivity.startActivity( new Intent( mActivity, PhotoActivity.class ) );
      if ( mApp_mData.countAllPhotos( TDInstance.sid, TDStatus.NORMAL ) > 0 ) {
        (new PhotoListDialog( this, mApp_mData )).show();
      } else {
        TDToast.makeWarn( R.string.no_photos );
      }
    } else if ( TDLevel.overExpert && p++ == pos ) { // AUDIO
      List< AudioInfo > audios = mApp_mData.selectAllAudios( TDInstance.sid );
      if ( audios.size() > 0 ) {  
        List< DBlock > shots = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
        (new AudioListDialog( this, this, audios, shots )).show();
      } else { 
        TDToast.makeWarn( R.string.no_audio );
      }
    } else if ( TDSetting.mWithSensors && TDLevel.overNormal && p++ == pos ) { // SENSORS
      mActivity.startActivity( new Intent( mActivity, SensorListActivity.class ) );
    } else if ( TDLevel.overBasic && p++ == pos ) { // 3D
      // if ( TopoDroidApp.exportSurveyAsThSync( ) ) { // make sure to have survey exported as therion
        // int check = TDVersion.checkCave3DVersion( this );
        // // TDLog.v( TAG + "check Cave3D version: " + check );
        // if ( check < 0 ) {
        //   TDToast.makeBad( R.string.no_cave3d );
        // } else if ( check == 0 ) {
          try {
            // FIXME CAVE3D Intent intent = new Intent( "Cave3D.intent.action.Launch" );
            Intent intent = new Intent( Intent.ACTION_VIEW ).setClass( this, com.topodroid.TDX.TopoGL.class );
            intent.putExtra( "INPUT_SURVEY", TDInstance.survey );
            intent.putExtra( "SURVEY_BASE", TDPath.getPathBase() );
            mActivity.startActivity( intent );
          } catch ( ActivityNotFoundException e ) {
            TDToast.makeBad( R.string.no_cave3d );
          }
        // } else {
        //   TDToast.makeBad( R.string.outdated_cave3d );
        // }
      // }
    } else if ( TDLevel.overNormal && (! TDInstance.isDivingMode()) && p++ == pos ) { // DEVICE
      if ( DeviceUtil.isAdapterEnabled() ) {
	    // referrer ? SIWEI
        if ( mDataDownloader.isDownloading() ) {
          TDToast.makeWarn( R.string.no_device_window );
        } else {
          // int mode = mDataDownloader.isDownloading() ? DeviceActivity.MODE_SELECT : DeviceActivity.MODE_NORMAL;
          // Intent intent = new Intent( mActivity, com.topodroid.TDX.DeviceActivity.class );
          // intent.putExtra( TDTag.TOPODROID_DEVICE_MODE, Integer.toString( mode ) );
          // mActivity.startActivity( intent );
          mActivity.startActivity( new Intent( Intent.ACTION_VIEW ).setClass( mActivity, DeviceActivity.class ) );
        }
      }
    } else  if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
      intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_SURVEY );
      mActivity.startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      // int nn = mNrButton1; //  + ( TDLevel.overNormal ?  mNrButton2 : 0 );
      new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
    }
  }

  // ---------------------------------------------------------------

  void askPhotoComment( DBlock blk )
  {
    (new ShotPhotoDialog(mActivity, this, blk.mId, blk.Name() ) ).show();
  }

  /**
   * @param ctx      context
   * @param shot_id  shot ID or 0 for saved station
   * @param title    photo title (or null)
   * @param comment  photo comment
   * @param camera   camera type: 0 use URI, 1 use TopoDroid - not used
   * @param code     geomorphology code
   * @param reftype  type of reference item 
   * @return the ID of the next photo
   * @note called by CurrentStationDialog and ShotPhotoDialog
   */
  long doTakePhoto( Context ctx, long shot_id, String title, String comment, int camera, String code, int reftype )
  {
    // camera = 1;
    // TDLog.v("shot window do take photo: shot id " + shot_id + " title " + title + " reftype " + reftype );
    long ret = mMediaManager.prepareNextPhoto( shot_id, title, comment, 1, camera, code, reftype ); // size 1 m

    // imageFile := PHOTO_DIR / surveyId / photoId .jpg
    // TDLog.Log( TDLog.LOG_SHOT, "photo " + imagefile.toString() );

    // if ( mMediaManager.isTopoDroidCamera() ) {
      // TDLog.v( "take photo with TopoDroid - prepare next photo returns " + ret + " path " + mMediaManager.getImageFilepath() );
      // new QCamCompass( this, this, (new MyBearingAndClino( mApp, mMediaManager.getImageFilepath()) ), this, false, false).show();  // false = with_box, false=with_delay
      MyBearingAndClino bearing_clino = new MyBearingAndClino( mApp, mMediaManager.getImageFilepath());
      new QCamCompass( ctx, this, bearing_clino, this, false, false, camera, mMediaManager).show();  // false = with_box, false=with_delay
    // } else {
    //   // TDLog.v( TAG + "take photo with Android");
    //   try {
    //     Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
    //     if ( intent.resolveActivity( getPackageManager() ) != null ) {
    //       String path = TDPath.getSurveyPhotoDir( TDInstance.survey ) + "/";
    //       // TDLog.v( TAG + "DistoX photo path " + path );
    //       // the URI must be a content resolver Uri
    //       intent.putExtra( android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile( new File( path ) ) );
    //       intent.putExtra( "return-data", true );
    //       startActivityForResult( intent, TDRequest.CAPTURE_IMAGE_SHOTWINDOW );
    //     } else {
    //       TDToast.makeBad( R.string.no_capture_app );
    //     }
    //   } catch ( ActivityNotFoundException e ) {
    //     TDToast.makeBad( R.string.no_capture_app );
    //   }
    // }
    return ret;
  }

  void askSensor( DBlock blk )
  {
    mSensorId = mApp_mData.nextSensorId( TDInstance.sid );
    mShotId   = blk.mId;
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

  // /** called to insert a manual shot after (?) a given shot // INTERMEDIATE_DATA
  //  * @param blk  shot after which to insert the new shot
  //  */
  // void dialogInsertShotAt( DBlock blk )
  // {
  //   (new ShotNewDialog( this, mApp, this, blk, blk.mId )).show();
  // }

  // /** insert a manual intermediate leg - called by ShotEditDialog// INTERMEDIATE_DATA
  //  * @param from     from station
  //  * @param to       to station
  //  * @param distance length
  //  * @param bearing  azimuth
  //  * @param clino    clino
  //  * @param extend   leg extend
  //  * @return id of inserted leg
  //  */
  // long insertDuplicateLeg( String from, String to, float distance, float bearing, float clino, int extend )
  // {
  //   long id = mApp.insertDuplicateLeg( from, to, distance, bearing, clino, extend );
  //   if  ( id >= 0L ) updateDisplay( ); 
  //   return id;
  // }

  // /** insert intermediate LRUD: first insert a duplicate leg from the given station, then insert LRUD at the TO point of the leg
  //  * @param at ???
  //  * @param station   station
  //  * @param bearing   leg azimuth
  //  * @param clino     leg clino
  //  * @param left      left
  //  * @param right     right
  //  * @param up        up  
  //  * @param down      down
  //  */
  // boolean insertLRUDatStation( long at, String station, float bearing, float clino, // INTERMEDIATE_DATA
  //                           String left, String right, String up, String down )
  // {
  //   // TDLog.v( TAG + "insert LRUD " + left + "/" + right + "/" + up + "/" + down + " at " + at );
  //   if ( mApp.insertLRUDatStation( at, station, bearing, clino, left, right, up, down ) < 0L ) return false;
  //   updateDisplay( ); 
  //   return true;
  // }

  // /** called by shot edit dialog to split the survey
  //  * @param shot_id   id of the shot at which to split
  //  */
  // void askSurvey( long shot_id )
  // {
  //   String new_survey = null; // new survey name
  //   TopoDroidAlertDialog.makeAlert( this, getResources(), R.string.survey_split,
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         doSplitOrMoveSurvey( shot_id, new_survey );
  //       }
  //   } );
  // }
  void doSplitOrMoveDialog( long shot_id )
  {
    (new SurveySplitOrMoveDialog( this, this, shot_id )).show();
  }
  
  /** split the survey and move the last shots to a survey of to a new survey
   * @param shot_id  first shot to move
   * @param survey   name of the target survey or null (for a new one)
   */
  void doSplitOrMoveSurvey( long shot_id, String survey )
  {
    long old_sid = TDInstance.sid;
    // long old_id  = shot_id; // mShotId;
    // TDLog.v( TAG + "split survey old: " + old_sid + " " + shot_id + " New: " + ((survey == null)? "null" : survey) );
    if ( TopoDroidApp.mShotWindow != null ) {
      // // if ( TDSetting.mDataBackup ) TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false, false ); // try_save
      TopoDroidApp.mShotWindow.doFinish();
      // TopoDroidApp.mShotWindow = null; // done in doFinish
    }
    if ( TopoDroidApp.mSurveyWindow != null ) {
      TopoDroidApp.mSurveyWindow.finish();
      TopoDroidApp.mSurveyWindow = null;
    }
    if ( survey == null ) {
      // TDLog.v( TAG + "split survey " + old_sid + " " + shot_id );
      TopoDroidApp.mMainActivity.startSplitSurvey( old_sid, shot_id ); // SPLIT SURVEY
    } else {
      // TDLog.v( TAG + "MOVE survey Old: " + old_sid + " " + shot_id + " New: " + survey );
      TopoDroidApp.mMainActivity.startMoveSurvey( old_sid, shot_id, survey ); // MOVE SURVEY
      // FIXME 20251205 TODO renumber moved shots inside the target survey
    }
  }

  // @param id   shot id (actually blk.mId)
  // @param blk  block
  // @param status
  // @param leg  if true delete the whole leg shots
  void doDeleteShot( long id, DBlock blk, int status, boolean leg )
  {
    mApp_mData.deleteShot( id, TDInstance.sid, status );
    if ( blk != null && blk.isMainLeg() ) { //  DBlock.BLOCK_MAIN_LEG 
      if ( leg ) {
        for ( ++id; ; ++id ) {
          DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
          if ( b == null || ! b.isSecLeg() ) { // != DBlock.BLOCK_SEC_LEG 
            break;
	  }
          mApp_mData.deleteShot( id, TDInstance.sid, status );
        }
      } else { // set station to next leg shot
        ++id;
        DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
        if ( b != null && b.isSecLeg() ) { //  DBlock.BLOCK_SEC_LEG --> leg-flag = 0
          long flag = blk.getFlag() | b.cavwayBits();
          mApp_mData.updateShotNameAndDataStatus( id, TDInstance.sid, blk.mFrom, blk.mTo, blk.getIntExtend(), flag, 0, blk.mComment, 0 );
          // mApp_mData.updateShotStatus( id, TDInstance.sid, 0 ); // status normal
        }
      }
    }
    // TDLog.v("update display : do delete shot");
    updateDisplay( ); 
  }

  /** insert the photo that has been just taken
   * @from PhotoInserter interface
   */
  public boolean insertPhoto( )
  {
    // FIXME TITLE has to go
    // TDLog.v("Shot window: insert photo JPEG reftype " + mMediaManager.getRefType() );
    mApp_mData.insertPhotoRecord( TDInstance.sid, mMediaManager.getPhotoId(), mMediaManager.getItemId(), "", TDUtil.currentDateTime(),
      mMediaManager.getComment(), mMediaManager.getCamera(), mMediaManager.getCode(), mMediaManager.getRefType(), PhotoInfo.FORMAT_JPEG );
    // FIXME NOTIFY ? no
    // TDLog.v("update display : insert phoito");
    updateDisplay( ); 
    return true;
  }

  public void insertPhotoBitmap( Bitmap bitmap )
  {
    long photo_id = mApp_mData.nextPhotoId( TDInstance.sid );
    String file_path = TDPath.getSurveyNextImageFilepath( photo_id, PhotoInfo.FORMAT_PNG );
    // TDLog.v("Shot window: insert bitmap photo PNG " + file_path + " reftype " + mMediaManager.getRefType() );
    try {
      FileOutputStream fos = new FileOutputStream( file_path );
      bitmap.compress( Bitmap.CompressFormat.PNG, 0, fos );
      fos.flush();
      fos.close();
      // N.B. use the next photo ID
      mApp_mData.insertPhotoRecord( TDInstance.sid, mMediaManager.getPhotoId()+1, mMediaManager.getItemId(), "", TDUtil.currentDateTime(),
        mMediaManager.getComment(), mMediaManager.getCamera(), mMediaManager.getCode(), mMediaManager.getRefType(), PhotoInfo.FORMAT_PNG );
    } catch ( IOException e ) {
      TDLog.e("BITMAP compress" );
    }
  }

  /** @return the current photo ID in the media manager
   */
  long getPhotoId() { return mMediaManager.getPhotoId(); }


  // void deletePhoto( PhotoInfo photo ) 
  // {
  //   mApp_mData.deletePhoto( TDInstance.sid, photo.id );
  //   File imagefile = TDFile.getFile( mApp.getSurveyJpgFile( Long.toString(photo.id) ) );
  //   try {
  //     imagefile.delete();
  //   } catch ( IOException e ) { }
  // }

  // void refreshList()
  // {
  //   // TDLog.v( TAG + "refresh display" );
  //   mDataAdapter.notifyDataSetChanged();
  //   mList.invalidate();
  // }

  // ---------------------------------------------------------------
  @Override
  protected void onActivityResult( int reqCode, int resCode, Intent intent )
  {
    // TDLog.Log( TDLog.LOG_DEBUG, "on Activity Result: request " + reqCode + " result " + resCode );
    switch ( reqCode ) {
      // case TDRequest.CAPTURE_IMAGE_SHOTWINDOW:
      //   if ( resCode == Activity.RESULT_OK ) { // RESULT_OK = -1 (0xffffffff)
      //     // (new ShotPhotoDialog(this, this, mShotId, blk_name ) ).show();
      //     // Bundle extras = intent.getExtras();
      //     // Bitmap bitmap = (Bitmap) extras.get("data");
      //     // mMediaManager.savePhotoFile( bitmap, 90 ); // compression = 90
      //     // TODO TDToast.makeToast( "saved " + mMediaManager.getImageFilepath() );
      //     Uri uri = intent.getData();
      //     if ( uri != null ) TDLog.v( TAG + "saved " + uri.toString() );
      //   }
      //   break;
      case TDRequest.SENSOR_ACTIVITY_SHOTWINDOW:
      // case TDRequest.EXTERNAL_ACTIVITY:
        if ( resCode == Activity.RESULT_OK ) {
          Bundle extras = intent.getExtras();
          if ( extras != null ) {
            String type  = extras.getString( TDTag.TOPODROID_SENSOR_TYPE );
            String value = extras.getString( TDTag.TOPODROID_SENSOR_VALUE );
            String comment = extras.getString( TDTag.TOPODROID_SENSOR_COMMENT );
            // TDLog.v( TAG + "insert sensor " + type + " " + value + " " + comment + " ID " + mSensorId + " shot " + mShotId );

            mApp_mData.insertSensor( TDInstance.sid, mSensorId, mShotId, "",
                                  TDUtil.currentDateTime(),
                                  comment,
                                  type,
                                  value,
                                  MediaInfo.TYPE_SHOT );
            // FIXME NOTIFY ? no
          }
        }
        break;
      case TDRequest.INFO_ACTIVITY_SHOTWINDOW:
        if ( resCode == Activity.RESULT_OK ) {
          // if ( TDSetting.mDataBackup ) TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false, false ); // try_save
          doFinish();
        }
        break;
    }
    setRefAzimuthButton( );
    // TDLog.v( TAG + "Multiselect [2] " + onMultiselect + " " + mDataAdapter.getMultiSelectSize() );
  }

  // ---------------------------------------------------------------
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate( savedInstanceState );

    getWindow().getDecorView().setSystemUiVisibility( TDSetting.mUiVisibility );

    TDandroid.setScreenOrientation( this );

    setContentView( R.layout.shot_activity );
    mApp = (TopoDroidApp) getApplication();
    mApp_mData = TopoDroidApp.mData;
    TopoDroidApp.mShotWindow = this; // FIXME
    mDataDownloader = mApp.mDataDownloader; // new DataDownloader( this, mApp );
    mActivity = this;
    mOnOpenDialog = false;
    mSurveyAccuracy = new SurveyAccuracy( ); 
    // mNrDevices = mApp_mData.getCountDevices( TDInstance.sid );
    mMediaManager   = new MediaManager( mApp_mData );

    // FIXME-28
    // RecyclerView rv = (RecyclerView) findViewById( R.id.recycler_view );
    // LinearLayoutManager lm = new LinearLayoutManager( this );
    // rv.setLayoutManager( lm );

    mShowSplay   = new ArrayList<>();
    if ( TDSetting.mEditableStations ) {
       mDataAdapter = new DBlockAdapter( this, this, R.layout.dblock_row, new ArrayList< DBlock >() );
    } else {
       mDataAdapter = new DBlockAdapter( this, this, R.layout.dblock_row_noedit, new ArrayList< DBlock >() );
    }

    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlaceholder( true );
    mButtonSize = TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    Resources res = getResources();
    mNrButton1 = TDLevel.overExpert ? 10
               : TDLevel.overAdvanced ? 9
               : TDLevel.overNormal ? 7
               : TDLevel.overBasic ?  6 : 5;
    if ( TDInstance.isDivingMode() ) {
      mNrButton1 -= 3;
      boff = 2;
    } else {
      boff = 0;
    }
    mButton1 = new Button[ mNrButton1 + 1 ]; // last is empty-button
    for ( int k=0; k < mNrButton1; ++k ) { 
      int kk = k+boff;
      mButton1[k] = MyButton.getButton( this, this, izons[kk] );
    }
    mButton1[mNrButton1] = MyButton.getButton( this, null, R.drawable.iz_empty );
    // FIXME_AZIMUTH_DIAL 1,2
    mBMdial          = BitmapFactory.decodeResource( res, R.drawable.iz_dial_transp );
    // mBMdial_transp   = BitmapFactory.decodeResource( res, R.drawable.iz_dial_transp );
    mDialBitmap      = MyTurnBitmap.getTurnBitmap( res );

    mBMplot     = MyButton.getButtonBackground( this, res, izons[BTN_PLOT] );
    mBMplot_no  = MyButton.getButtonBackground( this, res, R.drawable.iz_plot_no );
    mBMleft     = MyButton.getButtonBackground( this, res, R.drawable.iz_left );
    mBMright    = MyButton.getButtonBackground( this, res, R.drawable.iz_right );
    if ( ! TDInstance.isDivingMode() ) {
      mBMdownload = MyButton.getButtonBackground( this, res, izons[BTN_DOWNLOAD] );
      mBMbluetooth = MyButton.getButtonBackground( this, res, izons[BTN_BLUETOOTH] );
      mBMdownload_on   = MyButton.getButtonBackground( this, res, R.drawable.iz_download_on );
      mBMdownload_wait = MyButton.getButtonBackground( this, res, R.drawable.iz_download_wait );
      mBMdownload_no   = MyButton.getButtonBackground( this, res, R.drawable.iz_download_no );
      mBMbluetooth_no  = MyButton.getButtonBackground( this, res, R.drawable.iz_bt_no );
    }
    mBTstatus = ConnectionState.CONN_DISCONNECTED;

    if ( TDLevel.overBasic ) {
      if ( ! TDInstance.isDivingMode() ) mButton1[ BTN_DOWNLOAD ].setOnLongClickListener( this );
      mButton1[ BTN_PLOT - boff ].setOnLongClickListener( this );
      mButton1[ BTN_MANUAL - boff ].setOnLongClickListener( this );
      if ( TDLevel.overAdvanced ) {
        mButton1[ BTN_SEARCH - boff ].setOnLongClickListener( this );
      }
    }

    if ( ! TDLevel.overExpert ) mNrButtonF -= 2;
    mButtonF = new Button[ mNrButtonF + 1 ];
    int k0 = 0;
    for ( int k=0; k < mNrButtonF; ++k ) {
      if ( k == BTN_MULTISHOT && ! TDLevel.overExpert ) continue; 
      if ( k == BTN_COPY      && ! TDLevel.overExpert ) continue; 
      mButtonF[k0] = MyButton.getButton( this, this, izonsF[k] );
      ++k0;
    }
    mButtonF[mNrButtonF] = MyButton.getButton( this, null, R.drawable.iz_empty );

    // TDAzimuth.resetRefAzimuth( this, 90 );
    // setRefAzimuthButton( ); // called by onResume()

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mButtonViewF = new MyHorizontalButtonView( mButtonF );
    mListView.setAdapter( mButtonView1.mAdapter );
    onMultiselect = false;

    mList = (ListView) findViewById(R.id.list);
    // view_group.setDescendantFocusability( ViewGroup.FOCUS_BLOCK_DESCENDANTS ); //  FOCUS_BEFORE_DESCENDANTS FOCUS_AFTER_DESCENDANTS
    mList.setOnScrollListener( new OnScrollListener()
    {
      @Override public void onScroll( AbsListView listview, int first, int count, int total ) { }

      @Override public void onScrollStateChanged( AbsListView list_view, int state )
      { 
        if ( OnScrollListener.SCROLL_STATE_TOUCH_SCROLL == state ) {
          // TDLog.v("list on scroll state changed");
          View focus_view = getCurrentFocus();
          if ( focus_view != null ) focus_view.clearFocus();
        }
      }
    } );

    // https://stackoverflow.com/questions/7100555/preventing-catching-illegalargumentexception-parameter-must-be-a-descendant-of
    mList.setRecyclerListener( new AbsListView.RecyclerListener() {
      @Override
      public void onMovedToScrapHeap( View view )
      {
        // TDLog.v("list on moved to scrap heap");
        if ( view.hasFocus() ) {
          //Optional: also hide keyboard in that case
          if ( view instanceof EditText ) {
            if ( TDSetting.mEditableStations ) {
              view.requestFocus();
            } else {
              view.clearFocus(); //we can put it inside the second if as well, but it makes sense to do it to all scraped views
              InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
              imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
          }
        }
      }
    });

    // TDLog.v("list set adapter");
    mList.setAdapter( mDataAdapter );
    // mList.setOnItemClickListener( this );
    // mList.setLongClickable( true );
    // mList.setOnItemLongClickListener( this );
    mList.setDividerHeight( 2 );
    // mList.setSmoothScrollbarEnabled( true );
    // mList.setFastScrollAlwaysVisible( true ); // REQUIRES API-11
    // mList.setFastScrollEnabled( true );

    // restoreInstanceFromData();
    // mLastExtend = mApp_mData.getLastShotId( TDInstance.sid );
    // List< DBlock > list = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );

    mMenuImage = (Button) findViewById( R.id.handle );
    mMenuImage.setOnClickListener( this );
    TDandroid.setButtonBackground( mMenuImage, MyButton.getButtonBackground( this, res, R.drawable.iz_menu ) );

    mMenu = (ListView) findViewById( R.id.menu );
    mMenu.setOnItemClickListener( this );
    // setMenuAdapter( getResources() );
    // onMenu = true; // force close menu
    // closeMenu();

    // CutNPaste.dismissPopupBT();

    if ( mDataDownloader != null ) {
      mApp.registerLister( this );
    }

    // mSearch = new SearchResult();
    // TDLog.v( TAG + "on create sid " + TDInstance.sid );
  }

  @Override
  public void onStart() 
  {
    super.onStart();
    // TDLog.v( "Shot Activity on Start " );
    // Debug.startMethodTracing( "distox" );
    // TDLog.v( TAG + "on Start() " );
    TDLocale.resetTheLocale();
    setMenuAdapter( getResources() );
    onMenu = true; // force close menu
    closeMenu();
  }

  @Override
  public synchronized void onDestroy() 
  {
    super.onDestroy();
    TDLog.v( "Shot activity on Destroy" );
    new DataStopTask( mApp, this, mDataDownloader ).execute(); // 20251217

    if ( doubleBackHandler != null ) {
      doubleBackHandler.removeCallbacks( doubleBackRunnable );
    }
  }

  @Override
  public synchronized void onStop() 
  {
    // Debug.stopMethodTracing( );
    super.onStop();
    TDLog.v( "Shot activity on Stop" );
    // // if ( TDSetting.mDataBackup && TDSetting.mExportShotsFormat >= 0 ) TopoDroidApp.doExportDataAsync( TDInstance.context, TDSetting.mExportShotsFormat, false, false );
  }

  @Override
  public synchronized void onPause() 
  {
    super.onPause();
    TDLog.v( "Shot activity on Pause" );
    // saveInstanceToData();

    // mApp.unregisterConnListener( mHandler );
    // if ( mApp.mComm != null ) { mApp.mComm.suspend(); }
    // FIXME NOTIFY unregister ILister
  }

  @Override
  public synchronized void onResume() 
  {
    super.onResume();
    TDLog.v( "Shot Activity on Resume SID " + TDInstance.sid );
    // FIXME NOTIFY register ILister
    // if ( mApp.mComm != null ) { mApp.mComm.resume(); }
    // TDLog.v( TAG + "on Resume()" );
    
    if ( TDSetting.WITH_IMMUTABLE ) {
      // if ( mApp_mData != null ) 
      mApp_mData.checkSurveyInfoImmutable( TDInstance.sid ); 
    }

    restoreInstanceFromData();
    TDLog.v("update display : on resume");
    updateDisplay( );
    TDFeedback.reset();

    if ( mDataDownloader != null ) {
      mDataDownloader.onResume();
      // mApp.registerConnListener( mHandler );
      setConnectionStatus( mDataDownloader.getStatus() );
    }
    setRefAzimuthButton( );
    // TDLog.v( TAG + "Multiselect [3] " + onMultiselect + " " + mDataAdapter.getMultiSelectSize() );
  }

  /** @return the name (ILister interface)
   */
  public String name() { return "ShotWindow"; }

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

  private void doFinish()
  {
    new DataStopTask( mApp, this, mDataDownloader ).execute();
    TopoDroidApp.mShotWindow = null;
    finish();
  }

  // back pressed puts the activty on pause
  @Override
  public void onBackPressed () // askClose
  {
    if ( closeMenu() ) return;
    if ( CutNPaste.dismissPopupBT() ) return;
    if ( onMultiselect ) {
      clearMultiSelect();
      return;
    }
    if ( TDSetting.mSingleBack ) {
      TDLog.v("Shot activity on back pressed - single back");
      DrawingSurface.clearManagersCache();
      // new DataStopTask( mApp, this, mDataDownloader ).execute(); // 20251217 
      // if ( TDSetting.mDataBackup ) TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false, false ); // try_save
      TopoDroidApp.mShotWindow = null;
      super.onBackPressed(); 
    } else if ( doubleBack ) {
      TDLog.v("Shot activity on back pressed - double back execute");
      if ( doubleBackToast != null ) doubleBackToast.cancel();
      doubleBackToast = null;
      DrawingSurface.clearManagersCache();
      // new DataStopTask( mApp, this, mDataDownloader ).execute(); // 20251217 
      // if ( TDSetting.mDataBackup ) TopoDroidApp.doExportDataAsync( getApplicationContext(), TDSetting.mExportShotsFormat, false, false ); // try_save
      TopoDroidApp.mShotWindow = null;
      super.onBackPressed();
    } else {
      TDLog.v("Shot activity on back pressed - double back post runnable");
      doubleBack = true;
      doubleBackToast = TDToast.makeToast( R.string.double_back );
      doubleBackHandler.postDelayed( doubleBackRunnable, 1000 );
    }
  }

  // --------------------------------------------------------------

  // FIXME NOTIFY: the display mode is local - do not notify
  private void restoreInstanceFromData()
  { 
    // TDLog.v( TAG + "restore from data");
    if ( mApp_mData == null ) return;
    String shots = mApp_mData.getValue( "DISTOX_SHOTS" );
    if ( shots != null ) {
      String[] vals = shots.split( " " );
      mFlagSplay  = ( vals.length > 0 ) && vals[0].equals( TDString.ONE );
      mFlagLeg    = ( vals.length > 1 ) && vals[1].equals( TDString.ONE );
      mFlagBlank  = ( vals.length > 2 ) && vals[2].equals( TDString.ONE );
      mDataAdapter.show_ids = ( vals.length > 3 ) && vals[3].equals( TDString.ONE );
      mFlagLatest = ( vals.length > 4 ) && vals[4].equals( TDString.ONE );
    } else {
      TDLog.e( TAG + "no saved data");
    }
  }
    
  private void saveInstanceToData()
  {
    // TODO run in a Thread - this is executed when the displaymode is changed ... no need for a thread
    // TDLog.v( TAG + "save to data");
    (new Thread() {
      public void run() {
        mApp_mData.setValue( "DISTOX_SHOTS",
          String.format(Locale.US, "%d %d %d %d %d", mFlagSplay?1:0, mFlagLeg?1:0, mFlagBlank?1:0, isShowIds()?1:0, mFlagLatest?1:0 ) );
      }
    } ).start();
    // TDLog.v( TAG + "save to data mFlagSplay " + mFlagSplay );
  }

  // --------------------------------------------------------------
  private void doBluetooth( Button b ) // BLUETOOTH
  {
    mApp.doBluetoothButton( mActivity, this, b, mDataAdapter.getCount() );
  }

  /** react to a long tap
   * @param view   tapped view 
   * @return true if the long tap has been handled
   * the tapped view can be:
   * - SEARCH: move to the next search result
   * - PLOT: open the most recent plot
   * - MANUAL: open the manual-instruments calibration-dialog
   */
  @Override 
  public boolean onLongClick( View view )
  {
    // TDLog.v( "shot window: on long click " );
    if ( closeMenu() ) return true;
    if ( CutNPaste.dismissPopupBT() ) return true;

    boolean ret = false;
    Button b = (Button)view;
    if ( isButton1( b, BTN_SEARCH ) ) { // next search pos
      int pos = mDataAdapter.nextSearchPosition();
      // TDLog.v( TAG + "next search pos " + pos );
      jumpToPos( pos );
      // if ( mSearch != null ) jumpToPos( mSearch.nextPos() );
      // jumpToPos( mDataAdapter.nextSearchPosition() );
      ret = true;
    } else {
      mDataAdapter.clearSearch();
      if ( ! TDInstance.isDivingMode() && b == mButton1[ BTN_DOWNLOAD ] ) { // MULTI-DEVICE or SECOND-DEVICE
        if ( ! TDInstance.isSurveyMutable ) {  // IMMUTABLE
          TDToast.makeWarn("Immutable survey");
          return true;
        }
        if ( TDInstance.isDeviceDistoX() ) {
          if ( ! mDataDownloader.isDownloading() && TDSetting.isConnectionModeMulti() && TopoDroidApp.mDData.getDevices().size() > 1 ) {
            if ( TDSetting.mSecondDistoX && TDInstance.getDeviceB() != null ) {
              mApp.switchSecondDevice();
              setTheTitle();
              // TDToast.make( String.format( getResources().getString(R.string.using), TDInstance.deviceNickname() ) );
            } else {
              (new DeviceSelectDialog( this, mApp, mDataDownloader, this )).show();
            }
          } else {
            mDataDownloader.toggleDownloading();
            mDataDownloader.doDataDownload( mApp.mListerSet, DataType.DATA_SHOT );
          }
        } else { // TODO something cleverer than falling back to short click
          mDataDownloader.toggleDownloading();
          mDataDownloader.doDataDownload( mApp.mListerSet, DataType.DATA_SHOT );
        }
        ret = true;
      } else if ( isButton1( b, BTN_PLOT ) ) {
        if ( mWaitPlot ) {
          TDToast.make( R.string.pleasewait );
        } else {
          if ( TDInstance.recentPlot != null ) {
            startExistingPlot( TDInstance.recentPlot, TDInstance.recentPlotType, null );
          } else {
            // onClick( view ); // fall back to onClick
            new PlotListDialog( mActivity, this, mApp, null ).show();
          }
        }
        ret = true;
      } else if ( isButton1( b, BTN_MANUAL ) ) {
        if ( ! TDInstance.isSurveyMutable ) {  // IMMUTABLE
          TDToast.makeWarn("Immutable survey");
          return true;
        }
        new SurveyCalibrationDialog( mActivity /*, mApp */ ).show();
        ret = true;
      }
    }
    return ret;
  } 

  @Override 
  public void onClick(View view)
  {
    // TDLog.v( "shot window: on click " );
    if ( closeMenu() ) return;
    if ( CutNPaste.dismissPopupBT() ) return;

    mDataAdapter.clearSearch();
    // TDLog.v( TAG + "Multiselect [1] " + onMultiselect + " " + mDataAdapter.getMultiSelectSize() );

    if ( view instanceof Button ) {
      Button b = (Button)view;
      if ( b == mMenuImage ) {
        if ( mMenu.getVisibility() == View.VISIBLE ) {
          mMenu.setVisibility( View.GONE );
          onMenu = false;
        } else {
          mMenu.setVisibility( View.VISIBLE );
          onMenu = true;
          // if ( onMultiselect ) clearMultiSelect();
        }
        return;
      }

      Intent intent;
      int k1 = 0;
      int kf = 0;
      // int k2 = 0;
      if ( ! TDInstance.isDivingMode() ) {
        if ( k1 < mNrButton1 && b == mButton1[k1++] ) {        // DOWNLOAD
          // TDLog.v( TAG + "pressed download button");
          if ( ! TDInstance.isSurveyMutable ) {  // IMMUTABLE
            TDToast.makeWarn("Immutable survey");
            return;
          }
          if ( TDInstance.getDeviceA() != null ) {
            // mSearch = null; // invalidate search
            // if ( mBTstatus == ConnectionState.CONN_DISCONNECTED ) {
            //   TDToast.make( R.string.connecting );
            // }
            // TDLog.v( TAG + "Download button, mode " + TDSetting.getConnectionMode() );
            // toggle must come first
            boolean downloading = mDataDownloader.toggleDownloading();
            TDFeedback.notifyFeedback( this, downloading && mBTstatus == ConnectionState.CONN_DISCONNECTED );
            // TDLog.v("SHOT download button");
            mApp.notifyListerStatus( mApp.mListerSet, downloading ? ConnectionState.CONN_WAITING : ConnectionState.CONN_DISCONNECTED );
            // TDLog.v( "Download, conn mode " + TDSetting.getConnectionMode() + " download status " + mDataDownloader.getStatus() );
            // setConnectionStatus( mDataDownloader.getStatus() );
            mDataDownloader.doDataDownload( mApp.mListerSet, DataType.DATA_SHOT );
          } else {
            TDLog.e( TAG + "null device A");
          }
	  return;
        } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // BT RESET
          doBluetooth( b );
	  return;
        }
      }
      if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // DISPLAY 
        // mSearch = null; // invalidate search
        new ShotDisplayDialog( mActivity, this ).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // PLOT LIST
        if ( mWaitPlot ) {
          TDToast.make( R.string.pleasewait );
        } else {
          new PlotListDialog( mActivity, this, mApp, null ).show();
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // NOTE
        if ( TDInstance.survey != null ) {
          (new DialogAnnotations( mActivity, TDInstance.survey )).show();
        }

      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // ADD MANUAL SHOT
        if ( ! TDInstance.isSurveyMutable ) {  // IMMUTABLE
          TDToast.makeWarn("Immutable survey");
          return;
        }
        if ( TDLevel.overBasic ) {
          // mSearch = null; // invalidate search
          DBlock last_blk = mApp_mData.selectLastShotWithFromStation( TDInstance.sid );
          (new ShotNewDialog( mActivity, mApp, this, last_blk, -1L )).show();
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // AZIMUTH
        if ( TDLevel.overNormal ) {
          if ( TDSetting.mAzimuthManual ) { // toggle between LEFT and RIGHT "extend"
            setRefAzimuth( TDAzimuth.mRefAzimuth, - TDAzimuth.mFixedExtend );
          } else {
            (new AzimuthDialog( mActivity, this, TDAzimuth.mRefAzimuth, mBMdial )).show();
            // FIXME_AZIMUTH_DIAL (new AzimuthDialog( mActivity, this, TDAzimuth.mRefAzimuth, mDialBitmap )).show();
          }
        }

      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // SAVED STATIONS
        if ( TDLevel.overAdvanced ) {
          openSavedStationDialog( mApp.getCurrentOrLastStation() );
          // ArrayList< DBlock > list = numberSplays(); // SPLAYS splays numbering no longer active
          // if ( list != null && list.size() > 0 ) {
          //   updateDisplay( );
          // }
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // SEARCH
        if ( TDLevel.overAdvanced ) {
          new SearchDialog( mActivity, this, mDataAdapter.getSearchName(), mDataAdapter.isSearchPair() ).show();
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // REFRESH
        if ( TDLevel.overExpert ) {
          // TDLog.v("update display : refresh");
          if ( updateDisplay() > 1 ) {
            TDToast.make( R.string.shot_refreshed );
          }
	}

      } else if ( mDataAdapter.getMultiSelectSize() > 0 ) {
        if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // IGNORE
          for ( DBlock blk : mDataAdapter.getMultiSelect() ) {
            blk.clearExtendAndStretch();
            mApp_mData.updateShotExtend( blk.mId, TDInstance.sid, blk.getIntExtend(), blk.getStretch() );
          }
          mDataAdapter.updateSelectBlocksView();
          clearMultiSelect( );
          // updateDisplay(); // REPLACED
        } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // LEFT reset stretch
          for ( DBlock blk : mDataAdapter.getMultiSelect() ) {
            blk.setExtend( ExtendType.EXTEND_LEFT, ExtendType.STRETCH_NONE );
            mApp_mData.updateShotExtend( blk.mId, TDInstance.sid, ExtendType.EXTEND_LEFT, ExtendType.STRETCH_NONE );
          }
          mDataAdapter.updateSelectBlocksView();
          clearMultiSelect( );
          // updateDisplay(); // REPLACED
        } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // FLIP
          for ( DBlock blk : mDataAdapter.getMultiSelect() ) {
            if ( blk.flipExtendAndStretch() ) {
              mApp_mData.updateShotExtend( blk.mId, TDInstance.sid, blk.getIntExtend(), blk.getStretch() );
            }
          }
          mDataAdapter.updateSelectBlocksView();
          clearMultiSelect( );
          // updateDisplay(); // REPLACED
        } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // RIGHT reset stretch
          for ( DBlock blk : mDataAdapter.getMultiSelect() ) {
            blk.setExtend( ExtendType.EXTEND_RIGHT, ExtendType.STRETCH_NONE );
            mApp_mData.updateShotExtend( blk.mId, TDInstance.sid, ExtendType.EXTEND_RIGHT, ExtendType.STRETCH_NONE );
          }
          mDataAdapter.updateSelectBlocksView();
          clearMultiSelect( );
          // updateDisplay(); REPLACED
        } else if ( TDLevel.overExpert && kf < mNrButtonF && b == mButtonF[kf++] ) { // MULTISHOT
          // ( blks == null || blks.size() == 0 ) cannot happen // TDUtil.isEmpty(blks)
          (new MultishotDialog( mActivity, this, mDataAdapter.getMultiSelect() )).show();
        //   highlightBlocks( mDataAdapter.getMultiSelect() );
        //   // clearMultiSelect( );
        //   // updateDisplay();
        // } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // RENUMBER SELECTED SHOTS
        //   renumberBlocks( mDataAdapter.getMultiSelect() );
        //   clearMultiSelect( );
        //   mList.invalidate();
        // } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // BEDDING
        //   computeBedding( mDataAdapter.getMultiSelect() );
        } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // DELETE - CUT
          askMultiDelete();
        } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // COPY
          if ( testFlagSplayLegBlank() ) {
            TDToast.makeWarn( R.string.survey_copy_all_flags );
          } else if ( ! mDataAdapter.isMultiSelectTail() ) {
            TDToast.makeWarn( R.string.survey_copy_only_tail );
          } else {
            doMultiCopy();
          }
        } else if ( kf < mNrButtonF && b == mButtonF[kf++] ) { // CANCEL
          clearMultiSelect( );
        }
      }
    } else {
      TDLog.e("view not button");
    }
  }

  void openSavedStationDialog( String name )
  {
    (new CurrentStationDialog( mActivity, this, mApp, name )).show();
  }

  // ----------------------------------------------------------------
  // SEARCH

  void searchStation( String name, boolean splays )
  {
    // if ( mSearch != null ) {
      // mSearch.set( name, mDataAdapter.searchStation( name, splays ) );
      mDataAdapter.searchStation( name, splays );
      // if ( ! jumpToPos( mSearch.nextPos() ) ) 
      if ( ! jumpToPos( mDataAdapter.nextSearchPosition() ) ) {
        TDToast.make( R.string.station_not_found );
      }
    // }
  }

  /** search shots that satisfy a specified flag
   * @param flag  search flag (see DBlock)
   */
  void searchShot( long flag ) 
  {
    // if ( mSearch != null ) {
      // mSearch.set( null, mDataAdapter.searchShot( flag ) );
      mDataAdapter.searchShot( flag );
      // if ( ! jumpToPos( mSearch.nextPos() ) ) 
      if ( ! jumpToPos( mDataAdapter.nextSearchPosition() ) ) {
        TDToast.make( R.string.shot_not_found );
      }
    // }
  }

  /** search shots that satisfy a specified flag
   * @param names   station-pair
   */
  void searchLeg( String names )
  {
    String[] vals = TDString.splitOnSpaces( names );
    // if ( mSearch != null ) {
      // mSearch.set( null, mDataAdapter.searchLeg( names ) );
      mDataAdapter.searchLeg( vals[0], vals[1] );
      // if ( ! jumpToPos( mSearch.nextPos() ) ) 
      if ( ! jumpToPos( mDataAdapter.nextSearchPosition() ) ) {
        TDToast.make( R.string.shot_not_found );
      }
    // }
  }

  boolean jumpToPos( final int pos ) 
  {
    if ( pos < 0 ) return false;
    mList.post( new Runnable() {
      @Override
      public void run() {
        // TDLog.v("list jump to pos " + pos );
        mList.setSelection( pos );
        View v = mList.getChildAt( pos );
        if ( v != null ) v.requestFocus();
      }
    } );
    return true;
  }
   
  // ----------------------------------------------------------------
  /** ask whether to cut (delete) a set of shots
   */
  private void askMultiDelete()
  {
    String sts = checkXSections( mDataAdapter.getMultiSelect(), null, null ); // TODO list of xsection stations
    boolean safe = (sts == null);
    Resources res = getResources();
    TopoDroidAlertDialog.makeAlert( mActivity, res, res.getString( safe? R.string.shots_delete : R.string.shots_delete_unsafe ),
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
        } }
    );
  }

  /** cut (delete) a set of shots and put them in the buffer
   * @note package because called (only) by askMultiDelete
   */
  void doMultiDelete()
  {
    if ( TDLevel.overAdvanced ) mDBlockBuffer.clear();
    for ( DBlock blk : mDataAdapter.getMultiSelect() ) {
      if ( TDLevel.overAdvanced ) mDBlockBuffer.add( blk );
      long id = blk.mId;
      mApp_mData.deleteShot( id, TDInstance.sid, TDStatus.DELETED );
      // mSurveyAccuracy.removeBlockAMD( blk ); // not necessary: done by updateDisplay
      if ( /* blk != null && */ blk.isMainLeg() ) { // == DBlock.BLOCK_MAIN_LEG 
        if ( mFlagLeg ) {
          for ( ++id; ; ++id ) {
            DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
            if ( b == null || ! b.isSecLeg() ) { // != DBlock.BLOCK_SEC_LEG
              break;
	    }
            if ( TDLevel.overAdvanced ) mDBlockBuffer.add( b );
            mApp_mData.deleteShot( id, TDInstance.sid, TDStatus.DELETED );
            // mSurveyAccuracy.removeBlockAMD( b );
          }
        } else { // set station to next leg shot
          ++id;
          DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
          if ( b != null && b.isSecLeg() ) { // DBlock.BLOCK_SEC_LEG --> leg-flag 0
            long flag = blk.getFlag() & b.cavwayBits();
            mApp_mData.updateShotNameAndDataStatus( id, TDInstance.sid, blk.mFrom, blk.mTo, blk.getIntExtend(), flag, 0, blk.mComment, 0 );
            // mApp_mData.updateShotStatus( id, TDInstance.sid, 0 ); // status normal
          }
        }
      }
    }
    // TDLog.v( TAG + "BUFFER after cut: size " + mDBlockBuffer.size() );
    clearMultiSelect( );
    // TDLog.v("update display : do multiselect");
    updateDisplay( ); 
  }

  /** copy a set of shots to the buffer
   * @note copying shots in the DBlockBuffer is done only at level TESTER
   */
  void doMultiCopy()
  {
    if ( TDLevel.overAdvanced ) {
      mDBlockBuffer.clear();
      for ( DBlock blk : mDataAdapter.getMultiSelect() ) {
        mDBlockBuffer.add( blk );
        if ( /* blk != null && */ blk.isMainLeg() ) { // == DBlock.BLOCK_MAIN_LEG 
          if ( mFlagLeg ) {
            for ( long id = blk.mId+1; ; ++id ) {
              DBlock b = mApp_mData.selectShot( id, TDInstance.sid );
              if ( b == null || ! b.isSecLeg() ) { // != DBlock.BLOCK_SEC_LEG
                break;
              }
              mDBlockBuffer.add( b );
            }
          }
        }
      }
      // TDLog.v( TAG + "BUFFER after copy: size " + mDBlockBuffer.size() );
    }
    clearMultiSelect( );
    // updateDisplay( ); REPLACED
  }

  // ------------------------------------------------------------------

  public boolean hasSurveyStation( String start )
  {
    return mApp_mData.hasSurveyStation( TDInstance.sid, start );
  }

  // ---------------------------------------------------------------------------------

  public DBlock getNextBlankLegShot( DBlock blk )
  {
    DBlock ret = null;
    long id = 0;
    for ( int k=0; k<mDataAdapter.getCount(); ++k ) {
      DBlock b = mDataAdapter.get(k);
      if ( b == null ) return null;
      if ( b.isTypeBlank() ) {
        id = b.mId - 1;
        break;
      }
    }
    List< DBlock > list = mApp_mData.selectShotsAfterId( TDInstance.sid, id , 0 );
    for ( DBlock b : list ) {
      if ( b.isTypeBlank() ) {
        // TDLog.v( TAG + "BLANK " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null && ret.isRelativeDistance( b ) ) return ret;
        ret = b;
      } else if ( b.isSecLeg() ) {
        // TDLog.v( TAG + "LEG " + b.mLength + " " + b.mBearing + " " + b.mClino );
        if ( ret != null &&  ret.isRelativeDistance( b ) ) return ret;
      } else {
        // TDLog.v( TAG + "OTHER " + b.mLength + " " + b.mBearing + " " + b.mClino );
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
	// while ( mNextPos < mDataAdapter.getCount() && blk != mDataAdapter.get(mNextPos) ) ++ mNextPos;
    // ++ mNextPos; // one position after blk
    // while ( mNextPos < mDataAdapter.getCount() ) {
    //   DBlock b = mDataAdapter.get(mNextPos);
    //   int t = b.getBlockType();
    //   if ( t == DBlock.BLOCK_MAIN_LEG ) {
    //     return b;
    //   } else if (    DBlock.isTypeBlank( t )
    //               && mNextPos+1 < mDataAdapter.getCount()
    //               && b.isRelativeDistance( mDataAdapter.get(mNextPos+1) ) ) {
    //     return b;
    //   }
    //   ++ mNextPos;
    // }
    // NEW CODE
    for ( ; mNextPos < mDataAdapter.getCount(); ++ mNextPos ) {
      DBlock b = mDataAdapter.get(mNextPos);
      if ( b == null ) return null;
      if ( b == blk ) break;
    }
    // start at one position after blk
    for ( ++mNextPos; mNextPos < mDataAdapter.getCount(); ++mNextPos ) {
      DBlock b = mDataAdapter.get(mNextPos);
      if ( b == null ) return null;
      // int t = b.getBlockType();
      if ( b.isMainLeg() ) { // t == DBlock.BLOCK_MAIN_LEG 
        return b;
      } else if (    b.isTypeBlank( )
                  && mNextPos+1 < mDataAdapter.getCount()
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

  /** update the (normal) data of a shot
   * @param d    length
   * @param b    azimuth
   * @param c    clino
   * @param blk  shot block
   */
  void updateShotDistanceBearingClino( float d, float b, float c, DBlock blk )
  {
    // TDLog.v( TAG + "update shot DBC length " + d );
    if ( Math.abs( blk.mLength - d ) < 0.1 && Math.abs( blk.mBearing - b ) < 0.2 && Math.abs( blk.mClino - c ) < 0.2 ) return;
    if ( ! blk.isManual() ) {
      if ( ! blk.isTampered() ) {
        // TDLog.v( "update shot DBC tampered " + blk.mLength + " " + blk.mBearing + " " + blk.mClino + " -> " + d + " " + b + " " + c );
        blk.setTampered();
        // mApp_mData.saveShotDistanceBearingClino( TDInstance.sid, blk.mId, blk.mFlag, blk.mLength, blk.mBearing, blk.mClino, 0 ); // 0: mode normal
        mApp_mData.saveShotDistanceBearingClino( TDInstance.sid, blk );
      }
    }
    mApp_mData.updateShotDistanceBearingClino( blk.mId, TDInstance.sid, d, b, c );
    checkSiblings( blk, blk.mFrom, blk.mTo, d, b, c );
    blk.mLength  = d;
    blk.mBearing = b;
    blk.mClino   = c;
    mDataAdapter.updateBlockView( blk.mId );
  }

  /** update the (depth) data of a shot 
   * @note the params order is depth-azimuth-length
   * @param p    depth
   * @param b    azimuth
   * @param d    length
   * @param blk  shot block
   */
  void updateShotDepthBearingDistance( float p, float b, float d, DBlock blk )
  {
    // TDLog.v( TAG + "update shot DBC length " + d );
    if ( Math.abs( blk.mLength - d ) < 0.1 && Math.abs( blk.mBearing - b ) < 0.2 && Math.abs( blk.mDepth - p ) < 0.2 ) return;
    if ( ! blk.isManual() ) {
      if ( ! blk.isTampered() ) {
        blk.setTampered();
        // mApp_mData.saveShotDepthBearingDistance( TDInstance.sid, blk.mId, blk.mFlag, blk.mDepth, blk.mBearing, blk.mLength, 1 ); // 1: mode diving
        mApp_mData.saveShotDepthBearingDistance( TDInstance.sid, blk );
      }
    }
    mApp_mData.updateShotDepthBearingDistance( blk.mId, TDInstance.sid, p, b, d );
    checkSiblings( blk, blk.mFrom, blk.mTo, d, b, p );
    blk.mLength  = d;
    blk.mBearing = b;
    blk.mDepth   = p;
    mDataAdapter.updateBlockView( blk.mId );
  }

  /** update the shot stations and the flags
   * @param from    FROM station
   * @param to      TO station
   * @param extend  integer extend
   * @param stretch fractional extend
   * @param flag    shot flags (without cavway flags)
   * @param leg     leg data-helper value (0 normal, 1 sec, 2 x-splay, 3 back, 4 h-splay, 5 v-splay
   * @param comment shot comment
   * @param blk     shot data block
   * @note called only by ShotEditDialog 
   */
  void updateShotNameAndFlags( String from, String to, int extend, float stretch, long flag, long leg, String comment, DBlock blk, final boolean renumber )
  {
    // TDLog.v( TAG + "update shot " + from + "-" + to + " leg " + leg + "/" + blk.getLegType() + " blk type " + blk.getBlockType() + " comment " + comment );
    String sts = checkXSections( blk, from, to );
    if ( sts == null ) {
      doUpdateShotNameAndFlags( from, to, extend, stretch, flag, leg, comment, blk );
      if ( renumber ) renumberShotsAfter( blk );
    } else {
      Resources res = getResources();
      TopoDroidAlertDialog.makeAlert( mActivity, res,
        String.format( res.getString( R.string.shot_rename_unsafe ), sts ),
        res.getString(R.string.button_ok), 
        res.getString(R.string.button_cancel),
        new DialogInterface.OnClickListener() { // ok handler
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doUpdateShotNameAndFlags( from, to, extend, stretch, flag, leg, comment, blk );
            if ( renumber ) renumberShotsAfter( blk );
          } },
        null
      );
    }
  }

  /** internal implementation of update the shot stations and the flags
   * @param from    FROM station
   * @param to      TO station
   * @param extend  integer extend
   * @param stretch fractional extend
   * @param flag    shot flags (without cavway flag)
   * @param leg     leg data-helper value (0 normal, 1 sec, 2 x-splay, 3 back, 4 h-splay, 5 v-splay
   * @param comment shot comment
   * @param blk     shot data block
   * @note package because called (only) by alert dialog callback
   */
  void doUpdateShotNameAndFlags( String from, String to, int extend, float stretch, long flag, long leg, String comment, DBlock blk )
  {
    // TDLog.v( "SHOT WINDOW do update name and flags " + blk.mId + " flag " + flag + " leg " + leg + " / " + blk.getLegType() );
    blk.setBlockName( from, to, (leg == LegType.BACK) );
    blk.setBlockLegType( (int)leg );

    long fl = flag | blk.cavwayBits();
    if ( blk.isTampered() ) fl |= DBlock.FLAG_TAMPERED; 
    int ret = mApp_mData.updateShotNameAndData( blk.mId, TDInstance.sid, from, to, extend, fl, leg, comment );

    if ( ret == -1 ) {
      TDToast.makeBad( R.string.no_db );
    // } else if ( ret == -2 ) {
    //   TDToast.make( R.string.makes_cycle );
    } else {
      // update same shots of the given block
      List< DBlock > blk_list = mApp_mData.selectShotsAfterId( blk.mId, TDInstance.sid, 0L );
      for ( DBlock blk1 : blk_list ) {
        if ( ! blk1.isRelativeDistance( blk ) ) break;
        mApp_mData.updateShotLeg( blk1.mId, TDInstance.sid, LegType.EXTRA );
      }
    }

    if ( leg == LegType.EXTRA ) {
      blk.setTypeSecLeg();
    } else if ( leg == LegType.BACK ) {
      blk.setTypeBackLeg();
    // } else if ( blk.isBackLeg() ) {
    //   blk.setTypeMainLeg();
    }

    // TDLog.v( TAG + "update block id " + blk.mId );
    DBlock blk3 = mDataAdapter.updateBlockView( blk.mId );
    if ( blk3 != blk && blk3 != null ) {
      blk3.setBlockName( from, to, (leg == LegType.BACK) );
      blk3.setExtend( extend, stretch );
      blk3.resetFlag( flag );
      blk3.mComment = comment;
      // FIXME if ( leg == LegType.EXTRA ) blk3.setBlockLegType( DBlock.BLOCK_SEC_LEG );
      mDataAdapter.updateBlockView( blk3.mId );
    }
  }

  // FIXME_HIGHLIGHT
  // /** open the sketch and highlight block in the sketch
  //  * @param blks  data blocks list
  //  * @note called by MultishotDialog
  //  */
  /*
  void highlightBlocks( List< DBlock > blks )  // HIGHLIGHT
  {
    mApp.setHighlighted( blks );
    // TDLog.v( TAG + "highlight blocks [0] " + ( (blks==null)? "null" : blks.size() ) );
    if ( blks == null || blks.size() == 0 ) return; // TDUtil.isEmpty(blks)
    // now if there is a plot open it
    if ( TDInstance.recentPlot != null ) {
      startExistingPlot( TDInstance.recentPlot, TDInstance.recentPlotType, blks.get(0).mFrom );
    }
    clearMultiSelect( );
  }
  */

  /** open the sketch and highlight block in the sketch
   * @param blks  data blocks list
   * @param color shot color
   * @note called by MultishotDialog
   */
  void colorBlocks( List< DBlock > blks, int color )  // HIGHLIGHT
  {
    // TDLog.v( TAG + "highlight blocks [0] " + ( (blks==null)? "null" : blks.size() ) );
    if ( TDUtil.isEmpty(blks) ) return;
    for ( DBlock blk : blks ) {
      blk.clearPaint();
      // mApp_mData.updateShotColor( blk.mId, TDInstance.sid, color );
    }
    mApp_mData.updateShotsColor( blks, TDInstance.sid, color );
    // if ( TDInstance.recentPlot != null ) {
    //   startExistingPlot( TDInstance.recentPlot, TDInstance.recentPlotType, blks.get(0).mFrom );
    // }
    clearMultiSelect( );
    // ... and refresh list
    // TDLog.v("update display : color blocks");
    updateDisplay();
  }

  /** update the FROM-TO stations of a shot database record
   * @param bid    block ID
   * @param from   FROM station
   * @param to     TO station
   * @note package because called by the alert dialog callback
   */
  void updateShotName( long bid, String from, String to )
  {
    mApp_mData.updateShotName( bid, TDInstance.sid, from, to );
  }

  /** update the FROM-TO stations of a shot database record
   * @param blk    data block 
   * @param from   FROM station
   * @param to     TO station
   * @note used by the DBlockAdapter to react to edit actions
   */
  void updateDBlockName( DBlock blk, String from, String to )
  {
    // TDLog.v( "update Block Name " + blk.mId + " : " + from + " - " + to );
    String sts = checkXSections( blk, from, to );
    if ( sts == null ) {
      updateShotName( blk.mId, from, to );
      blk.setBlockName( from, to, blk.isBackLeg() ); 
    } else {
      Resources res = getResources();
      TopoDroidAlertDialog.makeAlert( mActivity, res,
        String.format( res.getString( R.string.shot_rename_unsafe ), sts ),
        res.getString(R.string.button_ok), 
        res.getString(R.string.button_cancel),
        new DialogInterface.OnClickListener() { // ok handler
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            updateShotName( blk.mId, from, to );
            blk.setBlockName( from, to, blk.isBackLeg() ); 
          } },
        null
      );
    }
  }

  /** check the sibling shots and toast a warning if there is a bad sibling
   * @param blk    data block (must be a leg)
   * @param from   FROM station
   * @param to     TO station
   * @param d      distance
   * @param b      azimuth [deg.]
   * @param c      clino [deg.]
   * @note used also by ShotEditDialog
   */
  private void checkSiblings( DBlock blk,  String from, String to, float d, float b, float c )
  {
    if ( ! blk.isLeg() ) { // if ( from.length() == 0 || to.length() == 0 )
      // TDLog.v( TAG + "check siblings: block " + blk.mId + " is not a leg");
      return;
    }
    if ( mApp_mData.checkSiblings( blk.mId, TDInstance.sid, from, to, d, b, c ) ) { // bad sibling
      TDToast.makeWarn( R.string.bad_sibling );
    // } else {
    //   // TDLog.v( TAG + "no bad sibling for " + blk.mId );
    }
  }

  /** @return true if no xsection refer to a station of the block, or to FROM / TO
   * @param blk    data block
   * @param from   FROM station (or null)
   * @param to     TO station (or null)
   */
  private String checkXSections( DBlock blk, String from, String to )
  {
    if ( ! blk.isLeg() ) return null;
    ArrayList<String> names = mApp_mData.getXSectionStations( TDInstance.sid );
    TreeSet< String > stations = new TreeSet<>();
    if ( from != null && ! from.equals( blk.mFrom ) ) {
      if ( from.length() > 0 && names.contains( from ) ) {
        stations.add(from);
      }
      if ( blk.mFrom != null && blk.mFrom.length() > 0 && names.contains( blk.mFrom ) ) {
        stations.add(blk.mFrom);
      }
    }
    if ( to != null && ! to.equals( blk.mTo ) ) {
      if ( to.length() > 0 && names.contains( to ) ) {
        stations.add(to);
      }
      if ( blk.mTo != null && blk.mTo.length() > 0 && names.contains( blk.mTo ) ) {
        stations.add(blk.mTo);
      }
    }
    if ( stations.size() == 0 ) return null;
    StringBuilder sb = new StringBuilder();
    for ( String st : stations ) sb.append(" ").append( st );
    return sb.toString();
  }

  /** @return true if no xsection refer to any block station or to FROM / TO
   * @param blks   list of data blocks
   * @param from   FROM station (or null)
   * @param to     TO station (or null)
   */
  private String checkXSections( List< DBlock > blks, String from, String to )
  {
    if ( TDUtil.isEmpty(blks) ) return null;
    ArrayList<String> names = mApp_mData.getXSectionStations( TDInstance.sid );
    TreeSet< String > stations = new TreeSet<>();
    if ( from != null && from.length() > 0 && names.contains( from ) ) {
      stations.add(from);
    }
    if ( to != null && to.length() > 0 && names.contains( to ) ) {
      stations.add(to);
    }
    for ( DBlock blk : blks ) {
      if ( blk.isLeg() ) { 
        if ( blk.mFrom != null && blk.mFrom.length() > 0 && names.contains( blk.mFrom ) ) {
          stations.add(blk.mFrom);
        } 
        if ( blk.mTo != null && blk.mTo.length() > 0 && names.contains( blk.mTo ) ) {
          stations.add(blk.mTo);
        }
      }
    }
    if ( stations.size() == 0 ) return null;
    StringBuilder sb = new StringBuilder();
    for ( String st : stations ) sb.append(" ").append( st );
    return sb.toString();
  }

  /** rename a set of data blocks
   * @param blks    list of blocks to renumber
   * @param from    FROM station to assign to first block
   * @param to      TO station to assign to first block
   * @note only called by MultishotDialog
   * no need to synchronize
   */
  void renumberBlocks( List< DBlock > blks, String from, String to )  // RENUMBER SELECTED BLOCKS
  {
    // TDLog.v( TAG + "renumber Blocks " + blks.size() );
    String sts = checkXSections( blks, from, to );
    if ( sts == null ) {
      doRenumberBlocks( blks, from, to );
    } else {
      Resources res = getResources();
      TopoDroidAlertDialog.makeAlert( mActivity, res, 
        String.format( res.getString( R.string.shots_rename_unsafe ), sts ),
        res.getString(R.string.button_ok), 
        res.getString(R.string.button_cancel),
        new DialogInterface.OnClickListener() { // ok handler
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doRenumberBlocks( blks, from, to );
          } },
        null
      );
    }
  }

  /** reassign stations to a set of blocks
   * @param blks   list of blocks to renumber
   * @param from   from station
   * @param to     to station
   * @note
   *   - if FROM and TO are empty all blocks get empty stations
   *   - if FROM and TO are not empty stations are assigned incrementally with the current policy
   *   - otherwise all blocks are splay and assigned the same stations
   */
  private void doRenumberBlocks( List< DBlock > blks, String from, String to )  // RENUMBER SELECTED BLOCKS
  {
    // TDLog.v( TAG + "do Renumber Blocks - size " + blks.size() );
    if ( from.length() == 0 && to.length() == 0 ) {
      for ( DBlock b : blks ) {
        b.setBlockName( from, to );
        updateShotName( b.mId, from, to );
      }
      // updateDisplay();
      mDataAdapter.updateSelectBlocksView(); // REPLACED updateDisplay
      clearMultiSelect();
      return;
    }

    DBlock blk = blks.get(0); // blk is guaranteed to exists
    if ( ! ( from.equals(blk.mFrom) && to.equals(blk.mTo) ) ) {
      blk.setBlockName( from, to, blk.isBackLeg() );
      updateShotName( blk.mId, from, to );
    }
    if ( blk.isLeg() ) {
      mApp.assignStationsAfter( blk, blks /*, stations */ );
      // TDLog.v("update display : renumber assign stations after");
      updateDisplay();
      // mList.invalidate();
      checkSiblings( blk, from, to, blk.mLength, blk.mBearing, blk.mClino );
    } else if ( blk.isSplay() ) { // FIXME RENUMBER ONLY SPLAYS
      for ( DBlock b : blks ) {
        if ( b == blk ) continue;
        b.setBlockName( from, to );
        updateShotName( b.mId, from, to );
      }
      // updateDisplay();
      mDataAdapter.updateSelectBlocksView(); // REPLACED updateDisplay
    } else {
      TDToast.makeBad( R.string.no_leg_first );
    }
    clearMultiSelect( ); // can move inside ?
  }

  void swapBlocksName( List< DBlock > blks )  // SWAP SELECTED BLOCKS STATIONS
  {
    String sts = checkXSections( blks, null, null );
    if ( sts == null ) {
      doSwapBlocksName( blks );
    } else {
      Resources res = getResources();
      TopoDroidAlertDialog.makeAlert( mActivity, res, 
        String.format( res.getString( R.string.shots_rename_unsafe ), sts ),
        res.getString(R.string.button_ok), 
        res.getString(R.string.button_cancel),
        new DialogInterface.OnClickListener() { // ok handler
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doSwapBlocksName( blks );
          } },
        null
      );
    }
  }

  /** swap from-to stations for a set of blocks
   * @param blks   list of data blocks
   * @note package for the alert dialog callback
   */
  void doSwapBlocksName( List< DBlock > blks )  // SWAP SELECTED BLOCKS STATIONS
  {
    // TDLog.v( TAG + "swap list size " + blks.size() );
    for ( DBlock blk : blks ) {
      String from = blk.mTo;
      String to   = blk.mFrom;
      // TDLog.v( TAG + "swap block to <" + from + "-" + to + ">" );
      blk.setBlockName( from, to );
      // updateShotName( blk.mId, from, to );
      checkSiblings( blk, from, to, blk.mLength, blk.mBearing, blk.mClino );
    }
    mApp_mData.updateShotsName( blks, TDInstance.sid );
    // updateDisplay();
    mDataAdapter.updateSelectBlocksView(); // REPLACED updateDisplay
    mDataAdapter.clearMultiSelect();
  }

  /** bedding: 
   * The eq. of the plane is  N * X = const
   *
   * Solve Sum (A*x + B*y + C*z + 1)^2 minimum
   *   xx  xy  xz   A   -xn
   *   xy  yy  yz * B = -yn
   *   xz  yz  zz   C   -zn
   *
   *   det = xx * (yy * zz - yz^2) - xy * (xy * zz - xz * yz) + xz * (xy * yz - yy * xz) 
   *       = xx yy zz - xx yz^2 - yy xz^2 - zz xy^2 + 2 xy xz yz
   * The normal to the plane is taken in upward direction, ie, Nz > 0
   * The strike is then along Z x N :
   *    S = ( Z x N ) normalized
   * The DIP is downwards and 90 deg. right from the STRIKE, thus
   *    D = S x N
   *
   *          ^ Z                     ^ Z
   *          |  Normal            N  |
   *          | /                   \ |
   *          |/______ Strike   _____\|
   *           \                     /
   *            \ Dip               /
   *
   *  The dip angle is measured from the horizontal plane.
   *  Therefore sin( dip ) = - ( D * Z ) and since D is always downwards this is always positive.
   *
   *  The strike iangle is measured from the north.
   *  Therefore strike can be positive (east) or negative (west).
   *     tan( strike ) = Nx / Ny
   * 
   *  The dip-direction is the direction of the projection of the dip on the horizontal plane.
   *  The strike/dip info is equivalent to dip-direction/dip.
   *  For vertical plane dip-direction is the normal to the plane (or its opposite);
   *  similarly strike can be one of the two orientation of the horizontal line in the plane.
   *  For horizontal plane dip-direction is undefined as well as strike.
   */
  String computeBedding( List< DBlock > blks ) // BEDDING
  {
    String strike_dip = getResources().getString(R.string.few_points);
    if ( blks != null && blks.size() > 2 ) {
      DBlock b0 = blks.get(0);
      String st = b0.mFrom;
      float xx = 0, xy = 0, yy = 0, xz = 0, zz = 0, yz = 0, xn = 0, yn = 0, zn = 0;
      int nn = 0;
      if ( st != null && st.length() > 0 ) {
        for ( DBlock b : blks ) {
          if ( st.equals( b.mFrom ) ) {
            float h = b.mLength * TDMath.cosd(b.mClino);
            float z = b.mLength * TDMath.sind(b.mClino); // upwards
            float y = h * TDMath.cosd(b.mBearing);       // north
            float x = h * TDMath.sind(b.mBearing);       // east
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
        // TDLog.v("BED [1] center " + st + " points " + nn );
      } else {
        st = b0.mTo;
        if ( st != null && st.length() > 0 ) {
          for ( DBlock b : blks ) {
            if ( st.equals( b.mTo ) ) { // use negative length
              float h = - b.mLength * TDMath.cosd(b.mClino);
              float z = - b.mLength * TDMath.sind(b.mClino); // vertical upwards 
              float y =   h * TDMath.cosd(b.mBearing); // north
              float x =   h * TDMath.sind(b.mBearing); // east 
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
        // TDLog.v("BED [2] center " + st + " points " + nn );
      }
      if ( nn >= 3 ) {
	String strike_fmt = getResources().getString( R.string.strike_dip );
	String strike_regex = strike_fmt.replaceAll("%\\d\\$.0f", "\\-??\\\\d+"); 
	// TDLog.v( TAG + "Strike regex: <<" + strike_regex + ">>");
        TDMatrix m = new TDMatrix( new TDVector(xx, xy, xz), new TDVector(xy, yy, yz), new TDVector(xz, yz, zz) );
        TDMatrix minv = m.inverseTransposed(); // m is self-transpose
        TDVector n0 = new TDVector( -xn, -yn, -zn );
        TDVector n1 = minv.timesV( n0 ); // n1 = (a,b,c)
        if ( n1.z < 0 ) { n1.x = - n1.x; n1.y = - n1.y; n1.z = - n1.z; } // make Nz positive (upward)
        n1.normalize();
        // TDLog.v( TAG + "Plane normal " + n1.x + " " + n1.y + " " + n1.z );

        // TDVector z0 = new TDVector( 0, 0, 1 );
        // TDVector stk = z0.cross( n1 );  // strike = ( -n1.y, n1.x, 0 );
        // stk.normalized();
        // TDVector dip = n1.cross( stk ); // dip
        // float astk = TDMath.atan2d( stk.x, stk.y ); // TDMath.atan2d( -n1.y, n1.x );
        // float adip = TDMath.acosd( dip.z ); // TDMath.asind( n1.z );

        float astk = TDMath.atan2d( n1.x, n1.y ); // dip-direction: Y=North, X=East
        if ( astk < 0 ) astk += 360;
        float adip = 90 - TDMath.asind( n1.z );   // dip inclination

        strike_dip = String.format( Locale.US, strike_fmt, astk, adip );
        // TDLog.v("BED strike/dip " + strike_dip );
        // TDToast.make( strike_dip );
        if ( b0.mComment != null && b0.mComment.length() > 0 ) {
	  if ( b0.mComment.matches( ".*" + strike_regex + ".*" ) ) {
	    // TDLog.v( TAG + "Strike regex is contained");
            b0.mComment = b0.mComment.replaceAll( strike_regex, strike_dip );
	  } else {
            b0.mComment = b0.mComment + " " + strike_dip;
          }
  	} else {
          b0.mComment = strike_dip;
	}
	// TDLog.v( TAG + "Comment <<" + b0.mComment + ">>");
	mApp_mData.updateShotComment( b0.mId, TDInstance.sid, b0.mComment );
	b0.invalidate();
      }
    }
    // clearMultiSelect( );
    // mList.invalidate();
    return strike_dip;
  }
  
  // NOTE called only by ShotEditDialog.saveDBlock() with to.length() == 0 ie to == "" and blk splay shot
  // update stations for all splay blocks with same from as this block
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
        int ret = -1;
        if ( blk.isTampered() ) flag |= DBlock.FLAG_TAMPERED;
        flag |= blk.cavwayBits();
        ret = mApp_mData.updateShotNameAndData( blk.mId, TDInstance.sid, from, to, extend, flag, leg, comment );

        if ( ret == -1 ) {
          TDToast.makeBad( R.string.no_db );
        // } else if ( ret == -2 ) {
        //   TDToast.make( R.string.makes_cycle );
        // } else {
          // // update same shots of the given block: SHOULD NOT HAPPEN
          // List< DBlock > blk_list = mApp_mData.selectShotsAfterId( blk.mId, TDInstance.sid, 0L );
          // for ( DBlock blk1 : blk_list ) {
          //   if ( ! blk1.isRelativeDistance( blk ) ) break;
          //   mApp_mData.updateShotLeg( blk1.mId, TDInstance.sid, LegType.Extra );
          // }
        }
      } else {
        b.setBlockName( from, to );
        updateShotName( b.mId, from, to ); // FIXME use
      }
      mDataAdapter.updateBlockView( b.mId );
    }
  }

  // ------------------------------------------------------------------------

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        onBackPressed();
        return true;
      case KeyEvent.KEYCODE_MENU:   // HARDWARE MENU (82)
        UserManualActivity.showHelpPage( mActivity, getResources().getString( HELP_PAGE ));
        return true;
      // case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      // case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        // TDLog.e( "key down: code " + code );
    }
    return false;
  }

  // ---------------------------------------------------------

  private void setMenuAdapter( Resources res )
  {
    int k = 0;
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<>(mActivity, R.layout.menu );

    menu_adapter.add( res.getString( menus[k++] ) );                // menu_survey
    menu_adapter.add( res.getString( menus[k++] ) );                // menu_close
    if ( TDLevel.overBasic  ) {                                     // menu_recover
      if ( TDLevel.overAdvanced ) {
        menu_adapter.add( res.getString( menus[10] ) ); // k + 8
      } else {
        menu_adapter.add( res.getString( menus[k] ) );
      }
      k++;
    }
    if ( TDLevel.overNormal ) menu_adapter.add( res.getString( menus[k] ) ); k++; // menu_photo  
    if ( TDLevel.overExpert ) menu_adapter.add( res.getString( menus[k] ) ); k++; // menu_audio  
    if ( TDSetting.mWithSensors && TDLevel.overNormal ) menu_adapter.add( res.getString( menus[k] ) ); k++; // menu_sensor
    if ( TDLevel.overBasic ) menu_adapter.add( res.getString( menus[k] ) ); k++; // menu_3d
    if ( TDLevel.overNormal && ! TDInstance.isDivingMode() ) menu_adapter.add( res.getString( menus[k] ) ); k++; // menu_device
    menu_adapter.add( res.getString( menus[k++] ) );  // menu_options
    menu_adapter.add( res.getString( menus[k++] ) );  // menu_help
    mMenu.setAdapter( menu_adapter );
    mMenu.invalidate();
  }

  private boolean closeMenu()
  {
    if ( onMenu ) {
      mMenu.setVisibility( View.GONE );
      onMenu = false;
      return true;
    }
    return false;
  }


  void recomputeItems( String st, int pos )
  {
    // TDLog.v("list recompute items");
    if ( mFlagSplay ) {
      if ( ! mShowSplay.remove( st ) ) {
        mShowSplay.add( st );
      }
      // TDLog.v("update display : recompute items");
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

  /** update the connection status feedbacks
   * @param status  new status
   */
  public void setConnectionStatus( int status )
  { 
    if ( TDInstance.isDivingMode() ) return;
    if ( TDInstance.getDeviceA() == null ) {
      mBTstatus = ConnectionState.CONN_DISCONNECTED;
      // mButton1[ BTN_DOWNLOAD ].setVisibility( View.GONE );
      TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload_no );
      TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], mBMbluetooth_no );
    } else {
      // TDLog.v( TAG + "set button, status " + mBTstatus + " -> " + status );
      if ( status != mBTstatus ) {
        mBTstatus = status;
        // mButton1[ BTN_DOWNLOAD ].setVisibility( View.VISIBLE );
        switch ( status ) {
          case ConnectionState.CONN_CONNECTED:
            TDFeedback.notifyFeedback( this, true );
            TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload_on );
            TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], (TDInstance.isDeviceBric() ? mBMbluetooth : mBMbluetooth_no ) );
            break;
          case ConnectionState.CONN_WAITING:
            TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload_wait );
            TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], (TDInstance.isDeviceBric() ? mBMbluetooth : mBMbluetooth_no ) );
            break;
          default:
            TDFeedback.notifyFeedback( this, false );
            TDandroid.setButtonBackground( mButton1[BTN_DOWNLOAD], mBMdownload );
            TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], mBMbluetooth );
        }
      }
    }
  }

  public void enableBluetoothButton( boolean enable )
  {
    if ( TDInstance.isDivingMode() ) return;
    if ( TDInstance.hasBleDevice() ) enable = true;
    TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], (enable ? mBMbluetooth : mBMbluetooth_no) );
    mButton1[BTN_BLUETOOTH].setEnabled( enable );
  }

  // void enableSketchButton( boolean enabled )
  // {
  //   mApp.mEnableZip = enabled;
  //   mButton1[ BTN_PLOT - boff ].setEnabled( enabled ); // FIXME PLOT BUTTON 
  //   TDandroid.setButtonBackground( mButton1[ BTN_PLOT - boff ], (enabled ? mBMplot : mBMplot_no) );
  // }

  // ------------------------------------------------------------------
  /** renumber shots after a given one
   * @param bid      ID of shot from which to renumber
   * no need to synchronize
   */
  void renumberShotsFrom( long bid ) // 20251205
  {
    List< DBlock > shots = mApp_mData.selectAllShotsAfter( bid, TDInstance.sid, TDStatus.NORMAL );
    TDLog.v("Renumber: shots from " + bid + " size " + shots.size() );
    if ( shots.isEmpty() ) return;
    // N.B. this set is the leg stations, but it should be ok
    Set< String > stations = mApp_mData.selectAllStationsBefore(  bid, TDInstance.sid );  
    TDLog.v("Renumber: stations before" + bid + " size " + stations.size() );
    if ( ! stations.isEmpty() ) {
      String last_station = null;
      for ( String station : stations ) {
        if ( last_station == null || StationName.compareNames( last_station, station ) > 0 ) last_station = station;
      }
      TDLog.v("Renumber: max station " + last_station );
      HashMap< String, String > station_map = new HashMap<>();
      for ( DBlock b : shots ) {
        String fi = null;
        String ti = null;
        String f = b.mFrom;
        if ( ! TDString.isNullOrEmpty( f ) ) {
          fi = station_map.get( f );
          if ( fi == null ) {
            fi = NativeName.incrementName( last_station, stations );
            last_station = fi;
            stations.add( fi );
            station_map.put( f, fi );
          }
        } 
        String t = b.mTo;
        if ( ! TDString.isNullOrEmpty( t ) ) {
          ti = station_map.get( t );
          if ( ti == null ) {
            ti = NativeName.incrementName( last_station, stations );
            last_station = ti;
            stations.add( ti );
            station_map.put( t, ti );
          }
        } 
        if ( fi != null || ti != null ) {
          if ( fi == null ) fi = TDString.EMPTY;
          // if ( ti == null ) ti = TDString.EMPTY
          mApp_mData.updateShotName( b.mId, TDInstance.sid, fi, ti );
        }
      }
    }
  }

  /** renumber shots after a given one
   * @param blk      shot after which to renumber
   * no need to synchronize
   */
  private void renumberShotsAfter( DBlock blk )
  {
    // TDLog.v( TAG + "renumber shots after " + blk.mLength + " " + blk.mBearing + " " + blk.mClino );
    // NEED TO FORWARD to the APP to change the stations accordingly
 
    List< DBlock > shots;
    // backsight and tripod seem to be OK
    // if ( StationPolicy.doTripod() || StationPolicy.doBacksight() ) {
    //   shots = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
    // } else {
      shots = mApp_mData.selectAllShotsAfter( blk.mId, TDInstance.sid, TDStatus.NORMAL );
    // }
    // TDLog.v( TAG + "shots " + shots.size() );
    mApp.assignStationsAfter( blk, shots /*, stations */ );

    // DEBUG re-assign all the stations
    // List< DBlock > shots = mApp_mData.selectAllShots( TDInstance.sid, TDStatus.NORMAL );
    // mApp.assign Stations( shots );

    // TDLog.v("update display : renumber shots after");
    updateDisplay();
  }


  // merge this block to the following (or second following) block if this is a leg
  // if success update FROM/TO of the block
  long mergeToNextLeg( DBlock blk )
  {
    long id = mApp_mData.mergeToNextLeg( blk, TDInstance.sid );
    // TDLog.v( TAG + "merge next leg: block " + blk.mId + " leg " + id );
    if ( id >= 0 && id != blk.mId ) {
      // mDataAdapter.updateBlockName( id, "", "" ); // name has already been updated in DB
      // TDLog.v("update display : merge to next leg");
      updateDisplay(); // FIXME change only block with id and blk.mId
    }
    return id;
  }

  // ------------------------------------------------------------------
  public void doProjectionDialog( String name, String start )
  {
    new ProjectionDialog( mActivity, this, TDInstance.sid, name, start ).show();
  }

  /**
   * @param name    profile name
   * @param start   origin station
   * @param azimuth plane azimuth
   * @param oblique oblique projection relative to azimuth (0 perpendicular)
   */
  void doProjectedProfile( String name, String start, int azimuth, int oblique )
  {
    makeNewPlot( name, start, false, azimuth, oblique );
  }

  void deletePlot( long pid1, long pid2 )
  {
    mApp_mData.deletePlot( pid1, TDInstance.sid );
    mApp_mData.deletePlot( pid2, TDInstance.sid );
    // FIXME NOTIFY
  }

  public boolean hasSurveyPlot( String name )
  {
    return mApp_mData.hasSurveyPlot( TDInstance.sid, name+"p" );
  }
 
  /**
   * @param name    profile name
   * @param start   origin station
   * @param extended whether profile is extended
   * @param azimuth plane azimuth
   * @param oblique oblique projection angle relative to azimuth (0 perpendicular)
   */
  public void makeNewPlot( String name, String start, boolean extended, int azimuth, int oblique )
  {
    long mPIDp = mApp.insert2dPlot( TDInstance.sid, name, start, extended, azimuth, oblique );

    if ( mPIDp >= 0 ) {
      long mPIDs = mPIDp + 1L; // FIXME !!! this is true but not guaranteed
      startDrawingWindow( start, name+"p", mPIDp, name+"s", mPIDs, PlotType.PLOT_PLAN, start, false ); // default no-landscape
    // } else {
    //   TDToast.makeBad( R.string.plot_duplicate_name );
    }
    // updateDisplay( );
  }

/* FIXME_SKETCH_3D 
  public void makeNewSketch3d( String name, String st1, String st2 )
  {
    // FIXME xoffset yoffset, east south and vert (downwards)
    if ( st2 != null ) {
      if ( ! mApp_mData.hasShot( TDInstance.sid, st1, st2 ) ) {
        TDToast.makeBad( R.string.no_shot_between_stations );
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
      TDToast.makeBad( R.string.no_to_station );
    }
  }
 
  void startSketchWindow( String name )
  {
    if ( TDInstance.sid < 0 ) {
      TDToast.make( R.string.no_survey );
      return;
    }

    if ( ! mApp_mData.hasSketch3d( TDInstance.sid, name ) ) {
      TDToast.makeBad( R.string.no_sketch );
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
 * END_SKETCH_3D */

  // called either by a long-tap on plot button 
  //        or a highlights
  void startExistingPlot( String name, long type, String station ) // name = plot/sketch3d name
  {
    // TDLog.Log( TDLog.LOG_SHOT, "start Existing Plot \"" + name + "\" type " + type + " sid " + TDInstance.sid );
    if ( type != PlotType.PLOT_SKETCH_3D ) {
      PlotInfo plot1 =  mApp_mData.getPlotInfo( TDInstance.sid, name+"p" );
      if ( plot1 != null ) {
        TDInstance.setRecentPlot( name, type );
        PlotInfo plot2 =  mApp_mData.getPlotInfo( TDInstance.sid, name+"s" );
        startDrawingWindow( plot1.start, plot1.name, plot1.id, plot2.name, plot2.id, type, station, plot1.isLandscape() );
        return;
      } else {
        TDInstance.setRecentPlot( null, 0L );
      }
/* FIXME_SKETCH_3D *
    } else {
      Sketch3dInfo sketch = mApp_mData.getSketch3dInfo( TDInstance.sid, name );
      if ( sketch != null ) {
        startSketchWindow( sketch.name );
        return;
      }
 * END_SKETCH_3D */
    }
    TDToast.makeBad( R.string.plot_not_found );
  }

  /** start the plot-sketching window
   * @param start      origin station
   * @param plot1_name plan view name
   * @param plot1_id   plan view id
   * @param plot2_name profile view name
   * @param plot2_id   profile view id
   * @param type       type of plot to show in the window
   * @param station    station where to center the plot on opening
   * @param landscape  whether to use landscape presentation
   */
  private void startDrawingWindow( String start, String plot1_name, long plot1_id,
                                   String plot2_name, long plot2_id, long type, String station, boolean landscape )
  {
    if ( TDInstance.sid < 0 || plot1_id < 0 || plot2_id < 0 ) {
      TDToast.makeWarn( R.string.no_survey );
      return;
    }
    for ( int k = 0; k < 5; ++k ) {
      if ( BrushManager.hasSymbolLibraries() ) break;
      TDUtil.slowDown( 251 );
    }
    if ( ! BrushManager.hasSymbolLibraries() ) {
      TDToast.makeWarn( R.string.no_symbols );
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

  /** react to a change in the configuration
   * @param new_cfg   new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration new_cfg )
  {
    super.onConfigurationChanged( new_cfg );
    TDLocale.resetTheLocale();
  }

  /** called by Drawing Shot Dialog to change shot color
   *   @param blk   data block
   *   @param color color (0 to clear)
   */
  void updateBlockColor( DBlock blk, int color )
  {
    blk.setPaintColor( color );
    mApp_mData.updateShotColor( blk.mId, TDInstance.sid, color );
    updateShotList( mMyBlocks, mMyPhotos );
  }

  // ------------------------------------------------------------------
  /** start the audio dialog for a data block
   * @param audio audio info (used only as check)
   * @param blk   data block
   *
   * @note called by ShotEditDialog to open a dialog with "audio" (old) or null (new)
   *                 AudioListDialog to open a dialog with "audio" (old)
   */
  void startAudio( AudioInfo audio, DBlock blk )
  {
    long audio_id = (audio == null) ? mApp_mData.getAudioIdByItem( TDInstance.sid, blk.mId, MediaInfo.TYPE_SHOT )
                                    : audio.getId();
    // TDLog.v( TAG + "start audio id " + audio_id + " block " + blk.mId );
    (new AudioDialog( mActivity, /* this */ null, audio_id, blk, blk.mId, MediaInfo.TYPE_SHOT )).show();
  }

  /** open dialog to recalibrate shots from the given ID onwards
   * @param shot_id shot ID
   */
  void recalibrate( long shot_id )
  {
    // if ( TDInstance.isDivingMode ) { // this should not occur
    //   TDLog.e("No recalibrate in diving-mode");
    //   return;
    // }
    (new RecalibrateDialog( this, this, shot_id )).show();
  }

  /** recompute bearing/clino of survey shots from one shot onwards - only Cavway
   * @param shot_id  first shot to recompute
   * @param calib_name name of the calibration that is used - must be Cavway calibration
   * NOTE this is not for diving-mode
   */
  void doRecalibrate( long shot_id, String calib_name )
  {
    // TODO
    CalibInfo ci = mApp.mDData.selectCalibInfo( calib_name );
    if ( ci == null ) {
      TDToast.make( R.string.recalibrate_none );
      return;
    }
    if ( ci.sensors != 2 ) {
      TDToast.make( R.string.recalibrate_no_cavway );
      return;
    }
    String coeff_str = mApp.mDData.selectCalibCoeff( ci.getId() );
    if ( coeff_str == null ) {
      TDToast.make( R.string.recalibrate_no_coeff );
      return;
    }
    byte[] coeff1 = CalibAlgo.stringToCoeff( coeff_str, 1 ); // 1: first set
    // TDLog.v("coeff size " + coeff1.length );
    TDMatrix mG1 = new TDMatrix();
    TDMatrix mM1 = new TDMatrix();
    TDVector vG1 = new TDVector();
    TDVector vM1 = new TDVector();
    TDVector nL1 = new TDVector();
    CalibAlgo.coeffToG( coeff1, vG1, mG1 );
    CalibAlgo.coeffToM( coeff1, vM1, mM1 );
    CalibAlgo.coeffToNL( coeff1, nL1 );
    byte[] coeff2 = new byte[ CalibTransform.COEFF_DIM ];
    if ( coeff2 == null ) {
      TDLog.e("Failed to create coeff2");
      return;
    }
    System.arraycopy( coeff1, CalibTransform.COEFF_DIM, coeff2, 0, CalibTransform.COEFF_DIM );
    TDMatrix mG2 = new TDMatrix();
    TDMatrix mM2 = new TDMatrix();
    TDVector vG2 = new TDVector();
    TDVector vM2 = new TDVector();
    TDVector nL2 = new TDVector();
    CalibAlgo.coeffToG( coeff2, vG2, mG2 );
    CalibAlgo.coeffToM( coeff2, vM2, mM2 );
    CalibAlgo.coeffToNL( coeff2, nL2 );
    // mG1.dump("MG1");
    // mG2.dump("MG2");

    List< RawDBlock > shots = mApp_mData.selectAllShotsRawDataAfter( shot_id, TDInstance.sid );
    for ( RawDBlock b : shots ) {
      float FV = TDUtil.FV;
      if ( b.mRawMx != 0 && b.mRawMy != 0 && b.mRawMz != 0 && b.mRawGx != 0 && b.mRawGy != 0 && b.mRawGz != 0 ) {
        TDVector g1 = new TDVector( MemoryData.longToSignedInt1( b.mRawGx )/FV, MemoryData.longToSignedInt1( b.mRawGy )/FV, MemoryData.longToSignedInt1( b.mRawGz )/FV );
        TDVector m1 = new TDVector( MemoryData.longToSignedInt1( b.mRawMx )/FV, MemoryData.longToSignedInt1( b.mRawMy )/FV, MemoryData.longToSignedInt1( b.mRawMz )/FV );
        float b1 = computeB( mG1, vG1, mM1, vM1, nL1, g1, m1 );
        float c1 = computeC( mG1, vG1, nL1, g1 );
        TDVector g2 = new TDVector( MemoryData.longToSignedInt2( b.mRawGx )/FV, MemoryData.longToSignedInt2( b.mRawGy )/FV, MemoryData.longToSignedInt2( b.mRawGz )/FV );
        TDVector m2 = new TDVector( MemoryData.longToSignedInt2( b.mRawMx )/FV, MemoryData.longToSignedInt2( b.mRawMy )/FV, MemoryData.longToSignedInt2( b.mRawMz )/FV );
        float b2 = computeB( mG2, vG2, mM2, vM2, nL2, g2, m2 );
        float c2 = computeC( mG2, vG2, nL2, g2 );
        TDVector v1 = new TDVector( TDMath.cosd( c1 ) * TDMath.sind( b1 ), TDMath.cosd( c1 ) * TDMath.cosd( b1 ), TDMath.sind( c1 ) );
        TDVector v2 = new TDVector( TDMath.cosd( c2 ) * TDMath.sind( b2 ), TDMath.cosd( c2 ) * TDMath.cosd( b2 ), TDMath.sind( c2 ) );
        TDVector v = v1.plus( v2 );
        // v.normalize();
        float b0 = TDMath.atan2d( v.x, v.y );  if ( b0 < 0 ) b0 += 360;
        float c0 = TDMath.atan2d( v.z, TDMath.sqrt( v.x*v.x + v.y*v.y ) );
        // TDLog.v( "Shot " + b.mId + " B " + b.mBearing + " " + b1 + " " + b2 + " " + b0 + " C " + b.mClino + " " + c1 + " " + c2 + " " + c0 );
        mApp_mData.updateShotBearingClino( b.mId, TDInstance.sid, b0, c0 );
      }
    }
  }
    
  /** @return the clino
   * @param mG    AG calibration matrix
   * @param vG    BG calibration vector
   * @param nL    non-linear coeffs (unused)
   * @param g0    sensor raw values
   */
  private float computeC( TDMatrix mG, TDVector vG, TDVector nL, TDVector g0 )
  {
    TDVector g = vG.plus( mG.timesV( g0 ) );
    return TDMath.atan2d( - g.x, TDMath.sqrt(g.y*g.y + g.z*g.z ) );
  }

  /** @return the bearing
   * @param mG    AG calibration matrix
   * @param vG    BG calibration vector
   * @param nL    non-linear coeffs (unused)
   * @param mM    AM calibration matrix
   * @param vM    BM calibration vector
   * @param g0    G sensor raw values
   * @param m0    M sensor raw values
   */
  private float computeB( TDMatrix mG, TDVector vG, TDMatrix mM, TDVector vM, TDVector nL, TDVector g0, TDVector m0 )
  {
    TDVector g = vG.plus( mG.timesV( g0 ) );  g.normalize();
    TDVector m = vM.plus( mM.timesV( m0 ) );  m.normalize();
    float sd = g.y * m.z - g.z * m.y; // this is ec[0];
    float cd = m.x - g.x * g.dot( m );
    return TDMath.atan2d( sd, cd );
  }

  /** refresh the colors of the shot views
   */
  void refreshShotViews()
  {
    int cnt = mList.getChildCount();
    // TDLog.v("refresh shot child views " + cnt );
    for ( int i = 0; i < cnt; ++i ) { // refresh view colors
      mDataAdapter.refreshViewColors( mList.getChildAt( i ) );
    } 
  }


  // FIXME trying to do inline station editing, but there is no view on focus
  // public boolean dispatchTouchEvent( MotionEvent ev )
  // {
  //   if ( TDSetting.mEditableStations ) {
  //     if ( ev.getAction() == MotionEvent.ACTION_DOWN ) {
  //       View v = getCurrentFocus();
  //       if ( v != null ) {
  //         TDLog.v("Motion event ACTION DOWN " + v.toString() );
  //       } else {
  //         TDLog.v("Motion event ACTION DOWN no view on focus" );
  //       }
  //       // if ( v instanceof EditText ) {
  //       //   return v.performClick();
  //       // }
  //     }
  //   }
  //   return super.dispatchTouchEvent( ev );
  // }

  // this is for onClick defined in the res file - no help
  // public void doStationClick( View v ) 
  // {
  //   TDLog.v("do station click " );
  // }
}
