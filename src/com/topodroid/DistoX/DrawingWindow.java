/* @file DrawingWindow.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid main drawing activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;



import java.io.File;
// import java.io.FileWriter;
import java.io.PrintWriter;
// import java.io.BufferedWriter;
// import java.io.FileOutputStream;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

import java.util.concurrent.RejectedExecutionException;
// import java.util.Deque; // only API-9
//
import android.app.Activity;

import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
// /* fixme-23 */
// import android.os.Build;
// import android.os.StrictMode;
// import java.lang.reflect.Method;

// import android.view.Menu;
// import android.view.SubMenu;
// import android.view.MenuItem;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
// import android.view.ViewGroup;
// import android.view.Display;
// import android.util.DisplayMetrics;
// import android.view.ContextMenu;
// import android.view.ContextMenu.ContextMenuInfo;
//
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Button;
// import android.widget.ZoomControls;
// import android.widget.ZoomButton;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Toast;

import android.provider.MediaStore;

import android.graphics.Bitmap;
// import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.RectF;
// import android.graphics.Path;
// import android.graphics.Path.Direction;

import android.net.Uri;

// import android.util.SparseArray;
import android.util.Log;

/**
 */
public class DrawingWindow extends ItemDrawer
                             implements View.OnTouchListener
                                      , View.OnClickListener
                                      , View.OnLongClickListener
                                      , OnItemClickListener
                                      , OnItemSelectedListener
                                      , OnZoomListener
                                      , ILabelAdder
                                      , ILister
                                      , IZoomer
                                      , IExporter
                                      , IFilterClickHandler
                                      , IJoinClickHandler
                                      , IPhotoInserter
                                      , IAudioInserter
{
  private static final int[] izons_ok = {
                        R.drawable.iz_edit_ok, // 0
                        R.drawable.iz_eraser_ok,
                        R.drawable.iz_select_ok };

  private static final int IC_DOWNLOAD  = 3;
  private static final int IC_BLUETOOTH = 4;
  private static final int IC_PLAN      = 7;
  private static final int IC_DIAL      = 8;
  private static final int IC_CONT_NONE = 12;  // index of continue-no icon
  private static final int IC_PREV      = 13;
  private static final int IC_NEXT      = 14;
  private static final int IC_JOIN      = 15;
  private static final int IC_BORDER_NO = 18;
  private static final int IC_ERASE_ALL = 20;
  private static final int IC_MEDIUM    = 21;

  private static final int IC_MENU          = 21+1;
  private static final int IC_EXTEND        = 21+2;
  private static final int IC_JOIN_NO       = 21+3;
  private static final int IC_CONT_START    = 21+4;     // index of continue icon
  private static final int IC_CONT_END      = 21+5;     // index of continue icon
  private static final int IC_CONT_BOTH     = 21+6;     // index of continue icon
  private static final int IC_CONT_CONTINUE = 21+7;     // index of continue icon
  private static final int IC_ADD           = 21+8;
  private static final int IC_BORDER_OK     = 21+9;
  private static final int IC_BORDER_BOX    = 21+10;
  private static final int IC_ERASE_POINT   = 21+11;
  private static final int IC_ERASE_LINE    = 21+12;
  private static final int IC_ERASE_AREA    = 21+13;
  private static final int IC_SMALL         = 21+14;
  private static final int IC_LARGE         = 21+15;
  private static final int IC_SELECT_ALL    = 21+16;
  private static final int IC_SELECT_POINT  = 21+17;
  private static final int IC_SELECT_LINE   = 21+18;
  private static final int IC_SELECT_AREA   = 21+19;
  private static final int IC_SELECT_SHOT   = 21+20;
  private static final int IC_SELECT_STATION= 21+21;
  private static final int IC_CONT_OFF      = 21+22;
  private static final int IC_DELETE_OFF    = 17;
  private static final int IC_DELETE_ON     = 21+23;

  private static final int BTN_DOWNLOAD = 3;  // index of mButton1 download button
  private static final int BTN_BLUETOOTH = 4; // index of mButton1 bluetooth button
  private static final int BTN_PLOT = 7;      // index of mButton1 plot button
  private static final int BTN_DIAL = 8;      // index of mButton1 azimuth button (level > normal)

  private static final int BTN_CONT = 6;      // index of mButton2 continue button (level > normal)
  private static final int BTN_JOIN = 5;      // index of mButton3 join button
  private static final int BTN_REMOVE = 7;    // index of mButton3 remove
  private static final int BTN_BORDER = 8;    // line border-editing (leve > advanced)

  private static final int BTN_SELECT_MODE = 3; // select-mode button
  private static final int BTN_SELECT_PREV = 3; // select-mode button
  private static final int BTN_SELECT_NEXT = 4; // select-mode button
  private static final int BTN_DELETE      = 7; // select-mode button

  private static final int BTN_ERASE_MODE = 5; // erase-mode button
  private static final int BTN_ERASE_SIZE = 6; // erase-size button

  private static final int[] izons = {
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,

                        R.drawable.iz_download,      // 3 MOVE Nr 3+6
                        R.drawable.iz_bt,
                        R.drawable.iz_mode,          // 5
                        R.drawable.iz_note,          // 6
                        R.drawable.iz_plan,          // 7
                        R.drawable.iz_dial,          // 8

                        R.drawable.iz_undo,          // 9 DRAW Nr 3+4
                        R.drawable.iz_redo,          // 10
                        R.drawable.iz_tools,         // 11
                        R.drawable.iz_cont_none,     // 12

                        R.drawable.iz_back,          // 13 EDIT Nr 3+6
                        R.drawable.iz_forw,
                        R.drawable.iz_join,
                        R.drawable.iz_attrib,          
                        R.drawable.iz_delete_off,    // 17
                        R.drawable.iz_range_no,      // 18

                        R.drawable.iz_select_all,    // only for help
                        R.drawable.iz_erase_all,     // 20 ERASE Nr 3+2
                        R.drawable.iz_medium,        // 21

                        R.drawable.iz_menu,          // 21+1
                        R.drawable.iz_extended,      // 21+2
                        R.drawable.iz_join_no,       // 21+3
                        R.drawable.iz_cont_start,    // 21+4
                        R.drawable.iz_cont_end,      // 21+5
                        R.drawable.iz_cont_both,
                        R.drawable.iz_cont_continue,
                        R.drawable.iz_plus,           // 21+8
                        R.drawable.iz_range_ok,       // 21+9
                        R.drawable.iz_range_box,      // 21+10
                        R.drawable.iz_erase_point,    // 21+11
                        R.drawable.iz_erase_line,     // 21+12
                        R.drawable.iz_erase_area,     // 21+13
                        R.drawable.iz_small,          // 21+14
                        R.drawable.iz_large,          // 21+15
                        R.drawable.iz_select_all,     // 21+16 all
                        R.drawable.iz_select_point,   // 21+17 point
                        R.drawable.iz_select_line,    // 21+18 line
                        R.drawable.iz_select_area,    // 21+19 area
                        R.drawable.iz_select_shot,    // 21+20 shot
                        R.drawable.iz_select_station, // 21+21 station
                        R.drawable.iz_cont_off,       // 21+22 continuation off
			R.drawable.iz_delete,         // 21+23 do delete
                      };
  private static final int[] menus = {
                        R.string.menu_switch,
                        R.string.menu_export,     // 0
                        R.string.menu_stats,      // 1
                        R.string.menu_reload,
                        R.string.menu_zoom_fit,
                        R.string.menu_rename_delete,
                        R.string.menu_palette,    // 6
                        R.string.menu_overview,
                        R.string.menu_options,
                        R.string.menu_help,
                        R.string.menu_area,       // 10
                        R.string.menu_close       // 11
                     };

  private static final int MENU_AREA  = 10;
  private static final int MENU_CLOSE = 11;

  private static final int[] help_icons = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_download,
                        R.string.help_remote,
                        R.string.help_refs,
                        R.string.help_note,
                        R.string.help_toggle_plot,
                        R.string.help_azimuth,
                        R.string.help_undo,
                        R.string.help_redo,
                        R.string.help_symbol_plot,
                        R.string.help_continue,
                        R.string.help_previous,
                        R.string.help_next,
                        R.string.help_line_point, 
                        R.string.help_note_plot,
                        R.string.help_delete_item,
                        R.string.help_range,
                        R.string.help_select_mode,
                        R.string.help_erase_mode,
                        R.string.help_erase_size
                      };
  private static final int[] help_menus = {
                        R.string.help_plot_switch,
                        R.string.help_save_plot,
                        R.string.help_stats,
                        R.string.help_recover,
                        R.string.help_zoom_fit,
                        R.string.help_plot_rename,
                        R.string.help_symbol,
                        R.string.help_overview,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  private static final int HELP_PAGE = R.string.DrawingWindow;

  private final static int DISMISS_NONE   = 0;
  private final static int DISMISS_EDIT   = 1;
  private final static int DISMISS_FILTER = 2;
  private final static int DISMISS_JOIN   = 3;
  private final static int DISMISS_BT     = 4;

  private TopoDroidApp mApp;
  private DataHelper   mApp_mData;
  private DataDownloader mDataDownloader;
  // private DrawingUtil mDrawingUtil;
  private boolean mLandscape;
  private boolean audioCheck;
  // private DataHelper mData;
  private Activity mActivity = null;

  // long getSID() { return TDInstance.sid; }
  // String getSurvey() { return TDInstance.survey; }

  private DistoXNum mNum;
  private float mDecl;
  private String mFormatClosure;

  private String mSectionName;
  private String mMoveTo; // station of highlighted splay

  private static BezierInterpolator mBezierInterpolator = new BezierInterpolator();
  private DrawingSurface  mDrawingSurface;
  private DrawingLinePath mCurrentLinePath;
  private DrawingLinePath mLastLinePath = null;
  private DrawingAreaPath mCurrentAreaPath;
  // private DrawingPath mFixedDrawingPath;
  // private Paint mCurrentPaint;
  private DrawingBrush mCurrentBrush;
  // private Path  mCurrentPath;

  // LinearLayout popup_layout = null;
  private PopupWindow mPopupEdit   = null;
  private PopupWindow mPopupFilter = null;
  private PopupWindow mPopupJoin   = null;

  // ShotNewDialog mShotNewDialog = null;

  // private boolean canRedo;
  private int mPointCnt; // counter of points in the currently drawing line

  // private boolean mIsNotMultitouch;


  // ERASE - EDIT mode and size
  private int mEraseMode  = Drawing.FILTER_ALL;
  private int mSelectMode = Drawing.FILTER_ALL;

  private int mEraseScale  = 0;
  private int mSelectScale = 0;

  private float mEraseSize  = 1.0f * TDSetting.mEraseness;
  private float mSelectSize = 1.0f * TDSetting.mSelectness;

  // protected static int mEditRadius = 0; 
  private int mDoEditRange = 0; // 0 no, 1 smooth, 2 boxed

  private boolean mEditMove;    // whether moving the selected point
  private boolean mShiftMove;   // whether to move the canvas in point-shift mode
  boolean mShiftDrawing;        // whether to shift the drawing (this is set only thru the DrawingModeDialog)
  private EraseCommand mEraseCommand = null;

  private int mHotItemType     = -1;
  private boolean mHasSelected = false;
  private boolean inLinePoint  = false;

  // ZOOM
  static final float ZOOM_INC = 1.4f;
  static final float ZOOM_DEC = 1.0f/ZOOM_INC;
  private ZoomButtonsController mZoomBtnsCtrl = null;
  private boolean mZoomBtnsCtrlOn = false;
  // FIXME_ZOOM_CTRL ZoomControls mZoomCtrl = null;
  // ZoomButton mZoomOut;
  // ZoomButton mZoomIn;
  private float oldDist;  // zoom pointer-sapcing
  private View mZoomView;

  // window mode
  static final int MODE_DRAW  = 1;
  static final int MODE_MOVE  = 2;
  static final int MODE_EDIT  = 3;
  static final int MODE_ZOOM  = 4; // used only for touchMode
  static final int MODE_SHIFT = 5; // change point symbol position
  static final int MODE_ERASE = 6;
  static final int MODE_ROTATE = 7; // selected point rotate
  static final int MODE_SPLIT = 8;  // split the plot

  // line join-continue
  static final private int CONT_OFF   = -1; // continue off
  static final         int CONT_NONE  = 0;  // no continue
  static final private int CONT_START = 1;  // continue: join to existing line
  static final private int CONT_END   = 2;  // continue: join to existing line
  static final private int CONT_BOTH  = 3;  // continue: join to existing line
  static final private int CONT_CONTINUE  = 4;  // continue: continue existing line
  // static final private int CONT_MAX   = 5;

  private int mMode         = MODE_MOVE;
  private int mTouchMode    = MODE_MOVE;
  private int mContinueLine = CONT_NONE;
  // private float mDownX;
  // private float mDownY;
  private float mSaveX;
  private float mSaveY;
  private float mSave0X;
  private float mSave0Y;
  private float mSave1X;
  private float mSave1Y;
  private float mStartX; // line shift scene start point
  private float mStartY;

  // private boolean mAllSymbols; // whether the library has all the symbols of the plot

  // -------------------------------------------------------------
  // STATUS items

  private String mName;   // current-plot name
  private String mName1;          // first name (PLAN)
  private String mName2;          // second name (EXTENDED/PROJECTED)
  private String mName3;          // third name (SECTION)
  private String mFullName1;      // accessible by the SaveThread
  private String mFullName2;
  private String mFullName3;

  private PlotInfo mPlot1;
  private PlotInfo mPlot2;
  private PlotInfo mPlot3;
  private PlotInfo mOutlinePlot1 = null;
  private PlotInfo mOutlinePlot2 = null;

  private long mSid;  // survey id
  private long mPid1; // plot id
  private long mPid2;
  private long mPid3;

  private long mPid;  // current plot id
  private long mType; // current plot type
  private String mFrom;
  private String mTo;   // TO station for sections
  private float mAzimuth = 0.0f;
  private float mClino   = 0.0f;
  private float mIntersectionT = -1.0f; // intersection abscissa for leg xsections
  private int   mSectionSkip = 0;       // number of splays to skip in section refresh
  private PointF mOffset  = new PointF( 0f, 0f );
  private PointF mDisplayCenter;
  private float mZoom  = 1.0f;

  private boolean mModified; // whether the sketch has been modified 
  private long mBackupTime;  // last time of backup

  private float mBorderRight      = 4096;
  private float mBorderLeft       = 0;
  private float mBorderInnerRight = 4096;
  private float mBorderInnerLeft  = 0;
  private float mBorderBottom     = 4096;
    
  // PLOT SPLIT
  private String mSplitName;
  // private DrawingStationName mSplitStation;
  private String mSplitStationName;
  private ArrayList< PointF > mSplitBorder = null;
  private boolean mSplitRemove;

  // ----------------------------------------------------------------
  // BUTTONS and MENU

  // private Button mButtonHelp;
  private int mButtonSize;
  private Button[] mButton1; // primary
  private Button[] mButton2; // draw
  private Button[] mButton3; // edit
  private Button[] mButton5; // eraser
  private static final int NR_BUTTON1 = 9;
  private static final int NR_BUTTON2 = 7;
  private static final int NR_BUTTON3 = 9;
  private static final int NR_BUTTON5 = 7;
  private int mNrButton1 = NR_BUTTON1; // main-primary [8: if level <= normal]
  private int mNrButton2 = NR_BUTTON2; // draw
  private int mNrButton3 = NR_BUTTON3; // edit [8 if level <= advanced]
  private int mNrButton5 = NR_BUTTON5; // erase
  private HorizontalButtonView mButtonView1;
  private HorizontalButtonView mButtonView2;
  private HorizontalButtonView mButtonView3;
  private HorizontalButtonView mButtonView5;

  private BitmapDrawable mBMbluetooth;
  private BitmapDrawable mBMbluetooth_no;
  private BitmapDrawable mBMdownload;
  private BitmapDrawable mBMdownload_on;
  private BitmapDrawable mBMdownload_wait;
  private BitmapDrawable mBMjoin;
  private BitmapDrawable mBMjoin_no;
  private BitmapDrawable mBMedit_box = null;
  private BitmapDrawable mBMedit_ok  = null;
  private BitmapDrawable mBMedit_no  = null;
  private BitmapDrawable mBMplan;
  private BitmapDrawable mBMextend;
  private BitmapDrawable mBMcont_none;
  private BitmapDrawable mBMcont_start;
  private BitmapDrawable mBMcont_end;
  private BitmapDrawable mBMcont_both;
  private BitmapDrawable mBMcont_continue;
  private BitmapDrawable mBMcont_off;
  private BitmapDrawable mBMdelete_off;
  private BitmapDrawable mBMdelete_on;
  private BitmapDrawable mBMadd;
  private BitmapDrawable mBMleft;
  private BitmapDrawable mBMright;
  private BitmapDrawable mBMsplayNone;       // grey bg
  private BitmapDrawable mBMsplayFront;
  private BitmapDrawable mBMsplayBack;
  private BitmapDrawable mBMsplayBoth;
  private BitmapDrawable mBMsplayNoneBlack;  // black bg
  private BitmapDrawable mBMsplayFrontBlack;
  private BitmapDrawable mBMsplayBackBlack;
  private BitmapDrawable mBMsplayBothBlack;
  private BitmapDrawable mBMeraseAll;
  private BitmapDrawable mBMerasePoint;
  private BitmapDrawable mBMeraseLine;
  private BitmapDrawable mBMeraseArea;
  private BitmapDrawable mBMsmall;
  private BitmapDrawable mBMmedium;
  private BitmapDrawable mBMlarge;
  private BitmapDrawable mBMprev;
  private BitmapDrawable mBMnext;
  private BitmapDrawable mBMselectAll;
  private BitmapDrawable mBMselectPoint;
  private BitmapDrawable mBMselectLine;
  private BitmapDrawable mBMselectArea;
  private BitmapDrawable mBMselectShot;
  private BitmapDrawable mBMselectStation;
  // FIXME_AZIMUTH_DIAL 1,2
  private Bitmap mBMdial;
  private MyTurnBitmap mDialBitmap; // use global MyDialBitmap

  private HorizontalListView mListView;
  private ListView   mMenu;
  private Button     mImage;
  // HOVER
  // MyMenuAdapter mMenuAdapter;
  // private ArrayAdapter< String > mMenuAdapter;
  private boolean onMenu;

  private int mNrSaveTh2Task = 0; // current number of save tasks

  Set<String> getStationNames() { return mApp_mData.selectAllStations( TDInstance.sid ); }

  // ----------------------------------------------------------
  // PLOT NAME(S)

  String getName() { return (mName != null)? mName : ""; }

  String getPlotName() 
  {
    if ( PlotInfo.isAnySection( mType ) ) {
      return mName3;
    } else if ( PlotInfo.isProfile( mType ) ) {
      return mName2.substring(0, mName2.length()-1);
    } else if ( mType == PlotInfo.PLOT_PLAN ) { 
      return mName1.substring(0, mName1.length()-1);
    }
    return "";
  }

  String getPlotStation()
  {
    if ( PlotInfo.isProfile( mType ) ) {
      return mPlot2.start;
    } else if ( mType == PlotInfo.PLOT_PLAN ) { 
      return mPlot1.start;
    }
    return mPlot3.start; // FIXME or should it be null ?
  }

  void renamePlot( String name ) 
  {
    if ( name == null || name.length() == 0 ) {
      return;
    }
    if ( PlotInfo.isAnySection( mType ) ) {
      TDLog.Error("X-Sections rename not implemented");
    } else if ( PlotInfo.isProfile( mType ) || mType == PlotInfo.PLOT_PLAN ) { 
      String name1 = name + "p";
      String name2 = name + "s";
      // Log.v("DistoX", "rename plot to: " + name1 + " " + name2 );
      // check if plot name name2 exists
      if ( mApp_mData.getPlotInfo( TDInstance.sid, name2 ) == null &&
           mApp_mData.getPlotInfo( TDInstance.sid, name1 ) == null ) {
        mApp_mData.updatePlotName( TDInstance.sid, mPid1, name1 );
        mApp_mData.updatePlotName( TDInstance.sid, mPid2, name2 );
        mName1 = name1;
        mName2 = name2;
        mPlot1.name = name1;
        mPlot2.name = name2;
        mName = ( PlotInfo.isProfile( mType ) )?  mName2 : mName1;
        // rename files
        String fullName1 = TDInstance.survey + "-" + mName1;
        String fullName2 = TDInstance.survey + "-" + mName2;

        TDPath.renamePlotFiles( mFullName1, fullName1 );
        TDPath.renamePlotFiles( mFullName2, fullName2 );

        mFullName1 = fullName1;
        mFullName2 = fullName2;
        // mApp.mShotWindow.setRecentPlot( name, mType );
        TDInstance.setRecentPlot( name, mType );
      } else {
        TDToast.makeBad( R.string.plot_duplicate_name );
        // Log.v("DistoX", "plot name already exists");
      }
    }
  }

  long getPlotType()   { return mType; }

  boolean isExtendedProfile() { return mType == PlotInfo.PLOT_EXTENDED; }

  boolean isAnySection() { return PlotInfo.isAnySection( mType ); }

  boolean isLandscape() { return mLandscape; }

  // ----------------------------------------------------------------

  public float zoom() { return mZoom; }

  private void modified()
  {
    long now = System.currentTimeMillis()/1000;
    if ( now < mBackupTime ) {
      // Log.v("DistoX", "time " + now + " < " + mBackupTime );
      return;
    }
    if ( mModified ) {
      // Log.v("DistoX", "already modified true");
      return;
    }
    mModified = true;
    mBackupTime = System.currentTimeMillis()/1000 + TDSetting.mBackupInterval;
    startSaveTdrTask( mType, PlotSave.MODIFIED, TDSetting.mBackupNumber, 1 );
  }

  private void resetModified()
  {
    mModified = false;
    mBackupTime = System.currentTimeMillis()/1000 + TDSetting.mBackupInterval;
    // Log.v("DistoX", "reset modified false, time " + mBackupTime );
  }

  // -------------------------------------------------------------------
  // ZOOM CONTROLS

  @Override
  public void onVisibilityChanged(boolean visible)
  {
    if ( mZoomBtnsCtrlOn && mZoomBtnsCtrl != null ) {
      mZoomBtnsCtrl.setVisible( visible || ( TDSetting.mZoomCtrl > 1 ) );
    }
  }

  @Override
  public void onZoom( boolean zoomin )
  {
    if ( zoomin ) changeZoom( ZOOM_INC );
    else changeZoom( ZOOM_DEC );
  }

  private void changeZoom( float f ) 
  {
    float zoom = mZoom;
    mZoom     *= f;
    // Log.v( TopoDroidApp.TAG, "zoom " + mZoom );
    mOffset.x -= mDisplayCenter.x*(1/zoom-1/mZoom);
    mOffset.y -= mDisplayCenter.y*(1/zoom-1/mZoom);
    mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );
    // mZoomCtrl.hide();
    // if ( mZoomBtnsCtrlOn ) mZoomBtnsCtrl.setVisible( false );
  }

  public void zoomIn()  { changeZoom( ZOOM_INC ); }
  public void zoomOut() { changeZoom( ZOOM_DEC ); }
  // public void zoomOne() { resetZoom( ); }

  // public void zoomView( )
  // {
  //   // TDLog.Log( TDLog.LOG_PLOT, "zoomView ");
  //   DrawingZoomDialog zoom = new DrawingZoomDialog( mActivity, this );
  //   zoom.show();
  // }

  // -----------------------------------------------------------------
  @Override
  public void lineSelected( int k, boolean update_recent )
  {
    super.lineSelected( k, update_recent );
    if ( TDLevel.overNormal ) {
      if ( BrushManager.mLineLib.getLineGroup( mCurrentLine ) == null ) {
        setButtonContinue( CONT_OFF );
      } else {
        setButtonContinue( mContinueLine ); // was CONT_NONE
      }
    }
  }

  private void resetFixedPaint( )
  {
    mDrawingSurface.resetFixedPaint( mApp, BrushManager.fixedShotPaint );
  }

  
  // used for the North line
  private void addFixedSpecial( float x1, float y1, float x2, float y2 ) // float xoff, float yoff )
  {
    DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH, null );
    dpath.setPathPaint( BrushManager.highlightPaint );
    // DrawingUtil.makePath( dpath, x1, y1, x2, y2, xoff, yoff );
    DrawingUtil.makePath( dpath, x1, y1, x2, y2 );
    mDrawingSurface.setNorthPath( dpath );
    // mLastLinePath = null;
  }

  // used to add legs and splays
  // @param extend  used only for splays
  private void addFixedLine( long type, DBlock blk, float x1, float y1, float x2, float y2,
                             // float xoff, float yoff, 
                             float extend, boolean splay, boolean selectable )
  {
    DrawingPath dpath = null;
    if ( splay ) {
      dpath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk );
      dpath.mExtend = extend; // save extend into path
      if ( PlotInfo.isProfile( type ) ) {
        if ( TDSetting.mDashSplay ) {
          dpath.setSplayPaintPlan( blk, extend, BrushManager.darkBluePaint, BrushManager.deepBluePaint );
        } else {
          dpath.setSplayPaintProfile( blk, BrushManager.darkBluePaint, BrushManager.deepBluePaint );
        }
      } else {
        dpath.setSplayPaintPlan( blk, extend, BrushManager.deepBluePaint, BrushManager.darkBluePaint );
      }
    } else {
      dpath = new DrawingPath( DrawingPath.DRAWING_PATH_FIXED, blk );
      dpath.setPathPaint( BrushManager.fixedShotPaint );
      if ( blk != null ) {
	if ( blk.isMultiBad() ) {
          dpath.setPathPaint( BrushManager.fixedOrangePaint );
        } else if ( TopoDroidApp.mShotWindow != null && TopoDroidApp.mShotWindow.isBlockMagneticBad( blk ) ) {
          dpath.setPathPaint( BrushManager.fixedRedPaint );
        } else if ( /* TDSetting.mSplayColor && */ blk.isRecent( ) ) { 
          dpath.setPathPaint( BrushManager.fixedBluePaint );
	}
      }
    }
    // DrawingUtil.makePath( dpath, x1, y1, x2, y2, xoff, yoff );
    DrawingUtil.makePath( dpath, x1, y1, x2, y2 );
    mDrawingSurface.addFixedPath( dpath, splay, selectable );
  }


  // used for splays in x-sections
  // the DBlock comes from a query in the DB and it is not the DBlock in the plan/profile
  //     therefore coloring the splays of those blocks does not affect the X-Section splay coloring
  //
  // @param a    angle between splay and normal to the plane
  // @param blue true for splays at TO station
  //
  private void addFixedSectionSplay( DBlock blk, float x1, float y1, float x2, float y2, float a,
                                     // float xoff, float yoff, 
                                     boolean blue )
  {
    // Log.v("DistoX", "Section splay angle " + a + " " + TDSetting.mVertSplay );
    DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk );
    dpath.mExtend = a; 
    Paint paint = blk.getPaint();
    if ( paint != null ) {
      dpath.setPathPaint( paint );
    } else if ( blue ) {
      if ( blk.isXSplay() ) {
        dpath.setPathPaint( BrushManager.paintSplayLRUD );    // GREEN
      } else if ( a > TDSetting.mSectionSplay ) {
        dpath.setPathPaint( BrushManager.paintSplayXVdot );  // BLUE dashed-4  -- -- -- --
      } else if ( a < -TDSetting.mSectionSplay ) {
        dpath.setPathPaint( BrushManager.paintSplayXVdash );  // BLUE dashed-3  --- --- ---
      } else {
        dpath.setPathPaint( BrushManager.paintSplayXViewed );   // BLUE
      }
    } else {
      if ( blk.isXSplay() ) {
        dpath.setPathPaint( BrushManager.paintSplayLRUD );    // GREEN
      } else if ( a > TDSetting.mSectionSplay ) {
        dpath.setPathPaint( BrushManager.paintSplayXBdot );   // LIGHT_BLUE dashed-4
      } else if ( a < -TDSetting.mSectionSplay ) {
        dpath.setPathPaint( BrushManager.paintSplayXBdash );   // LIGHT_BLUE dashed-3
      } else {
        dpath.setPathPaint( BrushManager.paintSplayXB );    // LIGHT_BLUE
      }
    }
    // dpath.setPathPaint( blue? BrushManager.paintSplayXViewed : BrushManager.paintSplayXB );
    // DrawingUtil.makePath( dpath, x1, y1, x2, y2, xoff, yoff );
    DrawingUtil.makePath( dpath, x1, y1, x2, y2 );
    mDrawingSurface.addFixedPath( dpath, true, false ); // true SPLAY false SELECTABLE
  }

  // --------------------------------------------------------------------------------------
  // final static String titleLandscape = " L ";
  // final static String titlePortrait  = " P ";

  @Override
  public void setTheTitle()
  {
    StringBuilder sb = new StringBuilder();
    if ( TDSetting.mConnectionMode == TDSetting.CONN_MODE_MULTI ) {
      sb.append( "{" );
      if ( TDInstance.device != null ) sb.append( TDInstance.device.getNickname() );
      sb.append( "} " );
    }
    // sb.append( mApp.getConnectionStateTitleStr() ); // IF_COSURVEY
    // sb.append( mLandscape ? titleLandscape : titlePortrait );
    sb.append(" ");
    
    Resources res = getResources();
    if ( mMode == MODE_DRAW ) { 
      if ( mSymbol == Symbol.POINT ) {
        sb.append( String.format( res.getString(R.string.title_draw_point), 
                                 BrushManager.mPointLib.getSymbolName(mCurrentPoint) ) );
      } else if ( mSymbol == Symbol.LINE ) {
        sb.append( String.format( res.getString(R.string.title_draw_line),
                                 BrushManager.mLineLib.getSymbolName(mCurrentLine) ) );
      } else  {  // if ( mSymbol == Symbol.LINE ) 
        sb.append( String.format( res.getString(R.string.title_draw_area),
                                 BrushManager.mAreaLib.getSymbolName(mCurrentArea) ) );
      }
      // boolean visible = ( mSymbol == Symbol.LINE && mCurrentLine == BrushManager.mLineLib.mLineWallIndex );
      boolean visible = ( mSymbol == Symbol.LINE );
      if ( TDLevel.overNormal ) {
        mButton2[ BTN_CONT ].setVisibility( visible? View.VISIBLE : View.GONE );
      }
    } else if ( mMode == MODE_MOVE ) {
      sb.append( res.getString( R.string.title_move ) );
    } else if ( mMode == MODE_EDIT ) {
      sb.append( res.getString( R.string.title_edit ) );
    } else if ( mMode == MODE_SHIFT ) {
      sb.append( res.getString( R.string.title_shift ) );
    } else if ( mMode == MODE_ERASE ) {
      sb.append( res.getString( R.string.title_erase ) );
    } else if ( mMode == MODE_SPLIT ) {
      sb.append( res.getString( R.string.title_split ) );
    }
    // if ( ! mDrawingSurface.isSelectable() ) {
    //   sb.append( mActivity.getTitle() + " [!s]" );
    // }
    mActivity.setTitle( sb.toString() );
  }

  // --------------------------------------------------------------

  // private void AlertMissingSymbols()
  // {
  //   TopoDroidAlertDialog.makeAlert( mActivity, getResources(), R.string.missing-symbols,
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         mAllSymbols = true;
  //       }
  //     }
  //   );
  // }

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

  void doClose()
  {
    super.onBackPressed();
  }

  // doSaveTdr( ) is already called by onPause
  @Override
  public void onBackPressed () // askClose
  {
    if ( dismissPopups() != DISMISS_NONE ) return;
    if ( PlotInfo.isAnySection( mType ) ) {
      mModified = true; // force saving
      startSaveTdrTask( mType, PlotSave.SAVE, TDSetting.mBackupNumber+2, TDPath.NR_BACKUP );
      popInfo();
      doStart( false, -1 );
    } else {
      if ( doubleBack ) {
        if ( doubleBackToast != null ) doubleBackToast.cancel();
        doubleBackToast = null;
        super.onBackPressed();
      } else {
        doubleBack = true;
        doubleBackToast = TDToast.makeToast( R.string.double_back );
        doubleBackHandler.postDelayed( doubleBackRunnable, 1000 );
      }
    }
  }

  void switchExistingPlot( String plot_name, long plot_type ) // context of current SID
  {
    Log.v("DistoXC", "switchExistingPlot " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    mLastLinePath = null;
    doSaveTdr( );
  }

  // called by doPause 
  private void doSaveTdr( )
  {
    if ( mDrawingSurface != null ) {
      // Log.v("DistoX", "do save type " + mType );
      mModified = true; // force saving: mModified is checked before spawning the saving task
      startSaveTdrTask( mType, PlotSave.SAVE, TDSetting.mBackupNumber+2, TDPath.NR_BACKUP );

      // if ( not_all_symbols ) AlertMissingSymbols();
      // if ( mAllSymbols ) {
      //   // TDToast.make( R.string.sketch_saving );
      //   startSaveTdrTask( mType, PlotSave.SAVE, TDSetting.mBackupNumber+2, TDPath.NR_BACKUP );
      // } else { // mAllSymbols is false: FIXME what to do ?
      //  TDToast.makeLong( "NOT SAVING " + mFullName1 + " " + mFullName2 );
      // }
    }
    resetModified();
  }

  // static private Handler saveHandler = null;

  String getOrigin() { return mPlot1.start; }

  PlotSaveData makePlotSaveData( int tt, int suffix, int rotate )
  {
    if ( tt == 1 && mPlot1 != null )
      return new PlotSaveData( mNum, /* mDrawingUtil, */ mPlot1, mDrawingSurface.getManager( mPlot1.type ), mName1, mFullName1, 0, suffix, rotate );
    if ( tt == 2 && mPlot2 != null )
      return new PlotSaveData( mNum, /* mDrawingUtil, */ mPlot2, mDrawingSurface.getManager( mPlot2.type ), mName2, mFullName2, (int)mPlot2.azimuth, suffix, rotate );
    if ( tt == 3 && mPlot3 != null )
      return new PlotSaveData( mNum, /* mDrawingUtil, */ mPlot3, mDrawingSurface.getManager( mPlot3.type ), mName3, mFullName3, 0, suffix, rotate );
    return null;
  }

  // called by doSaveTdr and doSaveTh2
  // @param type      plot type (-1 to save both plan and profile)
  // @param suffix    plot save mode (see PlotSave) - called only with TOGGLE, SAVE, MODIFIED
  // @param maxTasks
  // @param rotate    backup_rotate
  private void startSaveTdrTask( final long type, int suffix, int maxTasks, int rotate )
  {
    PlotSaveData psd1 = null;
    PlotSaveData psd2 = null;
    if ( type == -1 ) {
      psd2 = makePlotSaveData( 2, suffix, rotate );
      psd1 = makePlotSaveData( 1, suffix, rotate );
    } else if ( PlotInfo.isProfile( type ) ) {
      psd1 = makePlotSaveData( 2, suffix, rotate );
    } else if ( type == PlotInfo.PLOT_PLAN ) {
      psd1 = makePlotSaveData( 1, suffix, rotate );
    } else {
      psd1 = makePlotSaveData( 3, suffix, rotate );
    }
    doStartSaveTdrTask( psd1, psd2, suffix, maxTasks, rotate );
  }

  private void doStartSaveTdrTask( final PlotSaveData psd1, final PlotSaveData psd2, int suffix, int maxTasks, int rotate )
  {
    if ( psd1 == null ) return;
    int r = ( rotate == 0 )? 0 : psd1.rotate;
    Handler saveHandler = null;

    // Log.v("DistoX", "start save Th2 task. type " + type + " suffix " + suffix 
    //                 + " maxTasks " + maxTasks + " rotate " + rotate ); 
    if ( suffix != PlotSave.EXPORT ) {
      if ( ! mModified ) return;
      if ( mNrSaveTh2Task > maxTasks ) return;
      saveHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
          -- mNrSaveTh2Task;
          if ( mModified ) {
            doStartSaveTdrTask( psd1, psd2, PlotSave.HANDLER, TDSetting.mBackupNumber, 0 ); 
          } else {
            // mApp.mShotWindow.enableSketchButton( true );
            TopoDroidApp.mEnableZip = true;
          }
        }
      };
      ++ mNrSaveTh2Task;

      // mApp.mShotWindow.enableSketchButton( false );
      TopoDroidApp.mEnableZip = false;
      resetModified();
    } else {
      // Log.v("DISTOX", "exporting plot ...");
      saveHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
          // mApp.mShotWindow.enableSketchButton( true );
          TopoDroidApp.mEnableZip = true;
        }
      };
    }
    // TDUtil.slowDown( 10 );

    if ( psd2 != null ) {
      TDLog.Log( TDLog.LOG_IO, "save plot [2] " + psd2.fname );
      try { 
        (new SavePlotFileTask( mActivity, this, null, psd2.num, /* psd2.util, */ psd2.cm, psd2.fname, psd2.type, psd2.azimuth, psd2.suffix, r )).execute();
      } catch ( RejectedExecutionException e ) { 
        TDLog.Error("rejected exec save plot " + psd2.fname );
      }
    }
    try { 
      TDLog.Log( TDLog.LOG_IO, "save plot [1] " + psd1.fname );
      (new SavePlotFileTask( mActivity, this, saveHandler, psd1.num, /* psd1.util, */ psd1.cm, psd1.fname, psd1.type, psd1.azimuth, psd1.suffix, r )).execute();
    } catch ( RejectedExecutionException e ) { 
      TDLog.Error("rejected exec save plot " + psd1.fname );
      -- mNrSaveTh2Task;
    }
  }

  // ---------------------------------------------------------------------------------------

  private void moveTo( int type, String move_to )
  {
    // if ( move_to == null ) return; // move_to guaranteed non-null
    if ( mNum == null ) return; // WHY ??? unexpected crash report
    NumStation st = mNum.getStation( move_to );
    if ( st != null ) {
      if ( type == PlotInfo.PLOT_PLAN ) {
        mZoom     = mPlot1.zoom;
        mOffset.x = TopoDroidApp.mDisplayWidth/(2 * mZoom)  - DrawingUtil.toSceneX( st.e, st.s );
        mOffset.y = TopoDroidApp.mDisplayHeight/(2 * mZoom) - DrawingUtil.toSceneY( st.e, st.s );
        saveReference( mPlot1, mPid1 );
        // resetReference( mPlot1 );
        // mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );
        // return;
      } else if ( type == PlotInfo.PLOT_EXTENDED ) {
        mZoom     = mPlot2.zoom;
        mOffset.x = TopoDroidApp.mDisplayWidth/(2 * mZoom)  - DrawingUtil.toSceneX( st.h, st.v );
        mOffset.y = TopoDroidApp.mDisplayHeight/(2 * mZoom) - DrawingUtil.toSceneY( st.h, st.v );
        saveReference( mPlot2, mPid2 );
        // resetReference( mPlot2 );
        // return;
      } else { // if ( type == PlotInfo.PLOT_PROJECTED ) 
        float cosp = TDMath.cosd( mPlot2.azimuth );
        float sinp = TDMath.sind( mPlot2.azimuth );
        mZoom     = mPlot2.zoom;
	float xx = st.e * cosp + st.s * sinp;
        mOffset.x = TopoDroidApp.mDisplayWidth/(2 * mZoom)  - DrawingUtil.toSceneX( xx, st.v );
        mOffset.y = TopoDroidApp.mDisplayHeight/(2 * mZoom) - DrawingUtil.toSceneY( xx, st.v );
        saveReference( mPlot2, mPid2 );
        // return;
      }
    }
  }

  // this is called only for PLAN / PROFILE
  private boolean computeReferences( int type, String name,
                                  // float xoff, float yoff,
                                  float zoom, boolean can_toast )
  {
    // Log.v("DistoX", "compute references() zoom " + zoom + " landscape " + mLandscape );
    if ( ! PlotInfo.isSketch2D( type ) ) return false;
    mDrawingSurface.clearReferences( type );
    Log.v("DistoXC", "computeReference " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    mLastLinePath = null;

    // float xoff = 0; float yoff = 0;

    float cosp = 0;
    float sinp = 0;

    if ( type == PlotInfo.PLOT_PLAN ) {
      mDrawingSurface.setManager( DrawingSurface.DRAWING_PLAN, type );
      if ( mNum != null ) {
        DrawingUtil.addGrid( mNum.surveyEmin(), mNum.surveyEmax(), mNum.surveySmin(), mNum.surveySmax(), mDrawingSurface );
      } else {
        DrawingUtil.addGrid( -50, 50, -50 , 50, mDrawingSurface );
      }
      mDrawingSurface.addScaleRef( DrawingSurface.DRAWING_PLAN, type );
    } else {
      mDrawingSurface.setManager( DrawingSurface.DRAWING_PROFILE, type );
      if ( mNum != null ) {
        DrawingUtil.addGrid( mNum.surveyHmin(), mNum.surveyHmax(), mNum.surveyVmin(), mNum.surveyVmax(), mDrawingSurface );
      } else {
        DrawingUtil.addGrid( -50, 50, -50 , 50, mDrawingSurface );
      }
      mDrawingSurface.addScaleRef( DrawingSurface.DRAWING_PROFILE, type );
      if ( type == PlotInfo.PLOT_PROJECTED ) {
        cosp = TDMath.cosd( mPlot2.azimuth );
        sinp = TDMath.sind( mPlot2.azimuth );
      }
    }
    if ( mNum == null ) return false;

    List< NumStation > stations = mNum.getStations();
    List< NumShot > shots       = mNum.getShots();
    List< NumSplay > splays     = mNum.getSplays();

    String parent = ( TDInstance.xsections? null : name );

    if ( type == PlotInfo.PLOT_PLAN ) {
      for ( NumShot sh : shots ) {
        NumStation st1 = sh.from;
        NumStation st2 = sh.to;
        if ( st1.show() && st2.show() ) {
          addFixedLine( type, sh.getFirstBlock(), st1.e, st1.s, st2.e, st2.s, sh.getReducedExtend(), false, true );
                        // xoff, yoff, false, true );
        }
      }
      for ( NumSplay sp : splays ) {
        if ( Math.abs( sp.getBlock().mClino ) < TDSetting.mSplayVertThrs ) { // include only splays with clino below mSplayVertThrs
          NumStation st = sp.from;
          if ( st.show() ) {
            DBlock blk = sp.getBlock();
            if ( ! blk.isNoPlan() ) {
              addFixedLine( type, blk, st.e, st.s, sp.e, sp.s, sp.getReducedExtend(), true, true );
                          // xoff, yoff, true, true );
            }
          }
        }
      }
      // N.B. this is where TDInstance.xsections is necessary: to decide which xsections to check for stations
      //      could use PlotInfo.isXSectionPrivate and PlotInfo.getXSectionParent
      List< PlotInfo > xsections = mApp_mData.selectAllPlotSectionsWithType( TDInstance.sid, 0, PlotInfo.PLOT_X_SECTION, parent );
      List< CurrentStation > saved = null;
      if ( TDSetting.mSavedStations ) saved = mApp_mData.getStations( TDInstance.sid );
      for ( NumStation st : stations ) {
        if ( st.show() ) {
          DrawingStationName dst;
          dst = mDrawingSurface.addDrawingStationName( name, st,
                  DrawingUtil.toSceneX(st.e, st.s), DrawingUtil.toSceneY(st.e, st.s), true, xsections, saved );
        }
      }
    } else if ( type == PlotInfo.PLOT_EXTENDED ) {
      for ( NumShot sh : shots ) {
        if  ( ! sh.mIgnoreExtend ) {
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
	  DBlock blk = sh.getFirstBlock();
          if ( blk != null && st1.mHasCoords && st2.mHasCoords && st1.show() && st2.show() ) {
            addFixedLine( type, blk, st1.h, st1.v, st2.h, st2.v, sh.getReducedExtend(), false, true );
                          // xoff, yoff, false, true );
          }
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        if ( st.mHasCoords && st.show() ) {
          DBlock blk = sp.getBlock();
          if ( ! blk.isNoProfile() ) {
            addFixedLine( type, blk, st.h, st.v, sp.h, sp.v, sp.getReducedExtend(), true, true );
                        // xoff, yoff, true, true );
          }
        }
      }
      List< PlotInfo > xhsections = mApp_mData.selectAllPlotSectionsWithType( TDInstance.sid, 0, PlotInfo.PLOT_XH_SECTION, parent );
      List< CurrentStation > saved = null;
      if ( TDSetting.mSavedStations ) saved = mApp_mData.getStations( TDInstance.sid );
      for ( NumStation st : stations ) {
        if ( st.mHasCoords && st.show() ) {
          DrawingStationName dst;
          dst = mDrawingSurface.addDrawingStationName( name, st,
                  DrawingUtil.toSceneX(st.h, st.v), DrawingUtil.toSceneY(st.h, st.v), true, xhsections, saved );
        }
      }
    } else { // if ( type == PlotInfo.PLOT_PROJECTED ) 
      float h1, h2;
      for ( NumShot sh : shots ) {
        // Log.v("DistoX", "shot " + sh.from.name + "-" + sh.to.name + " from " + sh.from.show() + " to " + sh.to.show() );
        NumStation st1 = sh.from;
        NumStation st2 = sh.to;
        if ( st1.show() && st2.show() ) {
          h1 = st1.e * cosp + st1.s * sinp;
          h2 = st2.e * cosp + st2.s * sinp;
          // addFixedLine( type, sh.getFirstBlock(), h1, (float)(st1.v), h2, (float)(st2.v), xoff, yoff, sh.getReducedExtend(), false, true );
          addFixedLine( type, sh.getFirstBlock(), h1, st1.v, h2, st2.v, sh.getReducedExtend(), false, true );
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        if ( st.show() ) {
          DBlock blk = sp.getBlock();
          if ( ! blk.isNoProfile() ) {
            h1 = st.e * cosp + st.s * sinp;
            h2 = sp.e * cosp + sp.s * sinp;
            // addFixedLine( type, sp.getBlock(), h1, (float)(st.v), h2, (float)(sp.v), xoff, yoff, sp.getReducedExtend(), true, true );
            addFixedLine( type, blk, h1, st.v, h2, sp.v, sp.getReducedExtend(), true, true );
          }
        }
      }
      List< PlotInfo > xhsections = mApp_mData.selectAllPlotSectionsWithType( TDInstance.sid, 0, PlotInfo.PLOT_XH_SECTION, parent );
      List< CurrentStation > saved = null;
      if ( TDSetting.mSavedStations ) saved = mApp_mData.getStations( TDInstance.sid );
      for ( NumStation st : stations ) {
        if ( st.show() ) {
          DrawingStationName dst;
          h1 = st.e * cosp + st.s * sinp;
          dst = mDrawingSurface.addDrawingStationName( name, st,
                  DrawingUtil.toSceneX(h1, st.v), DrawingUtil.toSceneY(h1, st.v), true, xhsections, saved );
        // } else {
        //   Log.v("DistoX", "station not showing " + st.name );
        }
      }
    }

    if ( can_toast ) {
      if ( (! mNum.surveyAttached) && TDSetting.mCheckAttached ) {
        if ( (! mNum.surveyExtend) && TDSetting.mCheckExtend && type == PlotInfo.PLOT_EXTENDED ) {
          TDToast.makeWarn( R.string.survey_not_attached_extend );
        } else {
          TDToast.makeWarn( R.string.survey_not_attached );
        }
      } else if ( (! mNum.surveyExtend) && TDSetting.mCheckExtend && type == PlotInfo.PLOT_EXTENDED ) {
        TDToast.makeWarn( R.string.survey_not_extend );
      }
    }
    return true;
  }

  // --------------------------------------------------------------

  /** set the reference azimuth 
   * and the Extend Button image according to the reference azimuth or fixed one
   * @param azimuth       reference azimuth value
   * @param fixed_extend  fixed extend: -1 (left) 1 (right) 0 (use azimuth)
   */
  public void setRefAzimuth( float azimuth, long fixed_extend )
  {
    TDAzimuth.mFixedExtend = fixed_extend;
    TDAzimuth.mRefAzimuth = azimuth;
    if ( ! TDLevel.overAdvanced ) return;
    if ( BTN_DIAL >= mButton1.length ) return;

    if ( TDAzimuth.mFixedExtend == 0 ) {
      // FIXME_AZIMUTH_DIAL 2
      // android.graphics.Matrix m = new android.graphics.Matrix();
      // m.postRotate( azimuth - 90 );
      // Bitmap bm1 = Bitmap.createScaledBitmap( mBMdial, mButtonSize, mButtonSize, true );
      // Bitmap bm2 = Bitmap.createBitmap( bm1, 0, 0, mButtonSize, mButtonSize, m, true);
      Bitmap bm2 = mDialBitmap.getBitmap( TDAzimuth.mRefAzimuth, mButtonSize );

      TDandroid.setButtonBackground( mButton1[BTN_DIAL], new BitmapDrawable( getResources(), bm2 ) );
    } else if ( TDAzimuth.mFixedExtend == -1L ) {
      TDandroid.setButtonBackground( mButton1[BTN_DIAL], mBMleft );
    } else {
      TDandroid.setButtonBackground( mButton1[BTN_DIAL], mBMright );
    } 
  }

  // set the button3 by the type of the hot-item
  private void setButton3Item( SelectionPoint pt )
  {
    boolean deletable = false;
    inLinePoint  = false;
    BitmapDrawable bm = mBMjoin_no;
    String title = getResources().getString( R.string.title_edit );
    if ( pt != null ) {
      mHotItemType = pt.type();
      DrawingPath item = pt.mItem;
      switch ( mHotItemType ) {
        case DrawingPath.DRAWING_PATH_FIXED:
          mActivity.setTitle( title + " " + item.mBlock.mFrom + "=" + item.mBlock.mTo );
          break;
        case DrawingPath.DRAWING_PATH_SPLAY:
          mActivity.setTitle( title + " " + item.mBlock.mFrom + "-." );
          break;
        case DrawingPath.DRAWING_PATH_POINT:
          mActivity.setTitle( title + " " + BrushManager.mPointLib.getSymbolName( ((DrawingPointPath)item).mPointType ) );
	  deletable = true;
          break;
        case DrawingPath.DRAWING_PATH_LINE:
          mActivity.setTitle( title + " " + BrushManager.mLineLib.getSymbolName( ((DrawingLinePath)item).mLineType ) );
          inLinePoint = true;
          bm = mBMjoin;
	  deletable = true;
          break;
        case DrawingPath.DRAWING_PATH_AREA:
          mActivity.setTitle( title + " " + BrushManager.mAreaLib.getSymbolName( ((DrawingAreaPath)item).mAreaType ) );
          inLinePoint = true;
          bm = mBMjoin;
	  deletable = true;
          break;
        case DrawingPath.DRAWING_PATH_STATION:
          title = getResources().getString( R.string.title_edit_user_station );
          mActivity.setTitle( title + " " + ((DrawingStationPath)item).name() );
	  deletable = true;
          break;
        case DrawingPath.DRAWING_PATH_NAME:
          title = getResources().getString( R.string.title_edit_station );
          mActivity.setTitle( title + " " + ((DrawingStationName)item).name() );
          break;
        default:
          mActivity.setTitle( title );
      }
    } else {
      mHotItemType = -1;
      mActivity.setTitle( title );
    }
    TDandroid.setButtonBackground( mButton3[ BTN_JOIN ], bm );
    TDandroid.setButtonBackground( mButton3[ BTN_DELETE ], (deletable ? mBMdelete_on : mBMdelete_off) );
  }

  private void setButton3PrevNext( )
  {
    if ( mHasSelected ) {
      TDandroid.setButtonBackground( mButton3[ BTN_SELECT_PREV ], mBMprev );
      TDandroid.setButtonBackground( mButton3[ BTN_SELECT_NEXT ], mBMnext );
    } else {
      setButtonFilterMode( mSelectMode, Drawing.CODE_SELECT );
      setButtonSelectSize( mSelectScale );
    }
  }

  // must be called only if TDLevel.overNormal
  private void setButtonContinue( int continue_line )
  {
    mContinueLine = continue_line;
    if ( mSymbol == Symbol.LINE /* && mCurrentLine == BrushManager.mLineLib.mLineWallIndex */ ) {
      mButton2[ BTN_CONT ].setVisibility( View.VISIBLE );
      switch ( mContinueLine ) {
        case CONT_NONE:
          TDandroid.setButtonBackground( mButton2[ BTN_CONT ], mBMcont_none  );
          break;
        case CONT_START:
          TDandroid.setButtonBackground( mButton2[ BTN_CONT ], mBMcont_start  );
          break;
        case CONT_END:
          TDandroid.setButtonBackground( mButton2[ BTN_CONT ], mBMcont_end   );
          break;
        case CONT_BOTH:
          TDandroid.setButtonBackground( mButton2[ BTN_CONT ], mBMcont_both  );
          break;
        case CONT_CONTINUE:
          TDandroid.setButtonBackground( mButton2[ BTN_CONT ], mBMcont_continue  );
          break;
        case CONT_OFF:
          TDandroid.setButtonBackground( mButton2[ BTN_CONT ], mBMcont_off  );
      }
    } else {
      mButton2[ BTN_CONT ].setVisibility( View.GONE );
    }
  }

  public void setButtonJoinMode( int join_mode, int code )
  {
    if ( TDLevel.overNormal ) setButtonContinue( join_mode );
  }

  public void setButtonFilterMode( int filter_mode, int code )
  {
    if ( code == Drawing.CODE_ERASE ) {
      mEraseMode = filter_mode;
      switch ( mEraseMode ) {
        case Drawing.FILTER_ALL:
          TDandroid.setButtonBackground( mButton5[ BTN_ERASE_MODE ], mBMeraseAll );
          break;
        case Drawing.FILTER_POINT:
          TDandroid.setButtonBackground( mButton5[ BTN_ERASE_MODE ], mBMerasePoint );
          break;
        case Drawing.FILTER_LINE:
          TDandroid.setButtonBackground( mButton5[ BTN_ERASE_MODE ], mBMeraseLine );
          break;
        case Drawing.FILTER_AREA:
          TDandroid.setButtonBackground( mButton5[ BTN_ERASE_MODE ], mBMeraseArea );
          break;
      }
    } else if ( code == Drawing.CODE_SELECT ) {
      mSelectMode = filter_mode;
      mDrawingSurface.setSelectMode( mSelectMode );
      switch ( mSelectMode ) {
        case Drawing.FILTER_ALL:
          TDandroid.setButtonBackground( mButton3[ BTN_SELECT_MODE ], mBMselectAll );
          break;
        case Drawing.FILTER_POINT:
          TDandroid.setButtonBackground( mButton3[ BTN_SELECT_MODE ], mBMselectPoint );
          break;
        case Drawing.FILTER_LINE:
          TDandroid.setButtonBackground( mButton3[ BTN_SELECT_MODE ], mBMselectLine );
          break;
        case Drawing.FILTER_AREA:
          TDandroid.setButtonBackground( mButton3[ BTN_SELECT_MODE ], mBMselectArea );
          break;
        case Drawing.FILTER_SHOT:
          TDandroid.setButtonBackground( mButton3[ BTN_SELECT_MODE ], mBMselectShot );
          break;
        case Drawing.FILTER_STATION:
          TDandroid.setButtonBackground( mButton3[ BTN_SELECT_MODE ], mBMselectStation );
          break;
      }
    }
  } 

  private void setButtonEraseSize( int scale )
  {
    mEraseScale = scale % Drawing.SCALE_MAX;
    switch ( mEraseScale ) {
      case Drawing.SCALE_SMALL:
        mEraseSize = 0.5f * TDSetting.mEraseness;
        TDandroid.setButtonBackground( mButton5[ BTN_ERASE_SIZE ], mBMsmall );
        break;
      case Drawing.SCALE_MEDIUM:
        mEraseSize = 1.0f * TDSetting.mEraseness;
        TDandroid.setButtonBackground( mButton5[ BTN_ERASE_SIZE ], mBMmedium );
        break;
      case Drawing.SCALE_LARGE:
        mEraseSize = 2.0f * TDSetting.mEraseness;
        TDandroid.setButtonBackground( mButton5[ BTN_ERASE_SIZE ], mBMlarge );
        break;
    }
  }

  private void setButtonDelete( boolean on ) 
  {
    TDandroid.setButtonBackground( mButton3[ BTN_DELETE ], (on ? mBMdelete_on : mBMdelete_off) );
  }

  private void setButtonSelectSize( int scale )
  {
    mSelectScale = scale % Drawing.SCALE_MAX;
    switch ( mSelectScale ) {
      case Drawing.SCALE_SMALL:
        mSelectSize = 0.5f * TDSetting.mSelectness;
        TDandroid.setButtonBackground( mButton3[ BTN_SELECT_NEXT ], mBMsmall );
        break;
      case Drawing.SCALE_MEDIUM:
        mSelectSize = 1.0f * TDSetting.mSelectness;
        TDandroid.setButtonBackground( mButton3[ BTN_SELECT_NEXT ], mBMmedium );
        break;
      case Drawing.SCALE_LARGE:
        mSelectSize = 2.0f * TDSetting.mSelectness;
        TDandroid.setButtonBackground( mButton3[ BTN_SELECT_NEXT ], mBMlarge );
        break;
    }
  }

  // this method is a callback to let other objects tell the activity to use zooms or not
  private void switchZoomCtrl( int ctrl )
  {
    // Log.v("DistoX", "DEBUG switchZoomCtrl " + ctrl + " ctrl is " + ((mZoomBtnsCtrl == null )? "null" : "not null") );
    if ( mZoomBtnsCtrl == null ) return;
    mZoomBtnsCtrlOn = (ctrl > 0);
    switch ( ctrl ) {
      case 0:
        mZoomBtnsCtrl.setOnZoomListener( null );
        mZoomBtnsCtrl.setVisible( false );
        mZoomBtnsCtrl.setZoomInEnabled( false );
        mZoomBtnsCtrl.setZoomOutEnabled( false );
        mZoomView.setVisibility( View.GONE );
        break;
      case 1:
        mZoomView.setVisibility( View.VISIBLE );
        mZoomBtnsCtrl.setOnZoomListener( this );
        mZoomBtnsCtrl.setVisible( false );
        mZoomBtnsCtrl.setZoomInEnabled( true );
        mZoomBtnsCtrl.setZoomOutEnabled( true );
        break;
      case 2:
        mZoomView.setVisibility( View.VISIBLE );
        mZoomBtnsCtrl.setOnZoomListener( this );
        mZoomBtnsCtrl.setVisible( true );
        mZoomBtnsCtrl.setZoomInEnabled( true );
        mZoomBtnsCtrl.setZoomOutEnabled( true );
        break;
    }
  }

  private long mSavedType;

  private void resetStatus()
  {
    mSectionName  = null; 
    mLastLinePath = null;
    mShiftDrawing = false;
    // mContinueLine = TDSetting.mContinueLine; // do not reset cont-mode
    resetModified();
    setMode( MODE_MOVE );
    mTouchMode    = MODE_MOVE;
    setMenuAdapter( getResources(), mType );
  }

  private int mSavedMode;
  // private int mSplayMode;
  
  private void popInfo()
  {
    PlotInfo plot = ( mSavedType == PlotInfo.PLOT_PLAN )? mPlot1 : mPlot2;
    mType    = plot.type;
    mName    = plot.name;
    mFrom    = plot.start; 
    mTo      = "";
    mAzimuth = plot.azimuth;
    mClino   = plot.clino;
    mDrawingSurface.setDisplayMode( mSavedMode );
    // Log.v("DistoX", "pop " + mType + " " + mName + " from " + mFrom + " A " + mAzimuth + " C " + mClino );
    resetStatus();
    // FIXME_SK mButton1[ BTN_DOWNLOAD ].setVisibility( View.VISIBLE );
    // FIXME_SK mButton1[ BTN_BLUETOOTH ].setVisibility( View.VISIBLE );

    // mButton1[ BTN_PLOT ].setVisibility( View.VISIBLE );
    if ( ! TDLevel.overExpert ) mButton1[BTN_PLOT].setOnLongClickListener( this );
    if ( TDLevel.overNormal && BTN_DIAL < mButton1.length ) mButton1[ BTN_DIAL ].setVisibility( View.VISIBLE );
  }

  private void updateSplays( int mode )
  {
    mApp.mSplayMode = mode;
    switch ( mode ) {
      case 0:
        TDandroid.setButtonBackground( mButton1[ BTN_PLOT ], (mApp.mShowSectionSplays? mBMsplayNone : mBMsplayNoneBlack) );
        if ( PlotInfo.isSection( mType ) ) mDrawingSurface.hideStationSplays( mTo );
        mDrawingSurface.hideStationSplays( mFrom );
        break;
      case 1:
        TDandroid.setButtonBackground( mButton1[ BTN_PLOT ], (mApp.mShowSectionSplays? mBMsplayFront : mBMsplayFrontBlack) );
        if ( PlotInfo.isSection( mType ) ) mDrawingSurface.showStationSplays( mTo );
        mDrawingSurface.hideStationSplays( mFrom );
        break;
      case 2:
        TDandroid.setButtonBackground( mButton1[ BTN_PLOT ], (mApp.mShowSectionSplays? mBMsplayBoth : mBMsplayBothBlack) );
        if ( PlotInfo.isSection( mType ) ) mDrawingSurface.showStationSplays( mTo );
        mDrawingSurface.showStationSplays( mFrom );
        break;
      case 3:
        TDandroid.setButtonBackground( mButton1[ BTN_PLOT ], (mApp.mShowSectionSplays? mBMsplayBack : mBMsplayBackBlack) );
        if ( PlotInfo.isSection( mType ) ) mDrawingSurface.hideStationSplays( mTo );
        mDrawingSurface.showStationSplays( mFrom );
        break;
    }
    mDrawingSurface.setSplayAlpha( mApp.mShowSectionSplays ); // not necessary ?
  }


  private void pushInfo( long type, String name, String from, String to, float azimuth, float clino, float tt )
  {
    // Log.v("DistoX", "push info " + type + " " + name + " from " + from + " " + to + " A " + azimuth + " C " + clino + " TT " + tt );
    mSavedType = mType;
    mName = mName3 = name;
    mFullName3 = TDInstance.survey + "-" + mName;
    mType    = type;
    mFrom    = from;
    mTo      = to;
    mAzimuth = azimuth;
    mClino   = clino;
    mSavedMode = mDrawingSurface.getDisplayMode();
    // mDrawingSurface.setDisplayMode( DisplayMode.DISPLAY_SECTION | ( mSavedMode & DisplayMode.DISPLAY_SCALEBAR ) );
    mDrawingSurface.setDisplayMode( DisplayMode.DISPLAY_SECTION & mSavedMode );
    resetStatus();
    doStart( true, tt );
    updateSplays( mApp.mSplayMode );

    // FIXME_SK mButton1[ BTN_DOWNLOAD ].setVisibility( View.GONE );
    // FIXME_SK mButton1[ BTN_BLUETOOTH ].setVisibility( View.GONE );

    // mButton1[ BTN_PLOT ].setVisibility( View.GONE );
    if ( ! TDLevel.overExpert ) mButton1[BTN_PLOT].setOnLongClickListener( null );
    if ( TDLevel.overNormal && BTN_DIAL < mButton1.length ) mButton1[ BTN_DIAL ].setVisibility( View.GONE );
  }

  private void makeButtons( )
  {
    Resources res = getResources();
    if ( ! TDLevel.overAdvanced ) -- mNrButton1; // AZIMUTH requires expert level
    mButton1 = new Button[ mNrButton1 + 1 ]; // MOVE
    int off = 0;
    int ic = 0;
    for ( int k=0; k<mNrButton1; ++k ) {
      ic = ( k <3 )? k : off+k;
      mButton1[k] = MyButton.getButton( mActivity, this, izons[ic] );
      if ( ic == IC_DOWNLOAD )  { mBMdownload = MyButton.getButtonBackground( mApp, res, izons[ic] ); }
      else if ( ic == IC_BLUETOOTH ) { mBMbluetooth = MyButton.getButtonBackground( mApp, res, izons[ic] ); }
      else if ( ic == IC_PLAN ) { mBMplan     = MyButton.getButtonBackground( mApp, res, izons[ic] ); }
    }
    mButton1[ mNrButton1 ] = MyButton.getButton( mActivity,this, R.drawable.iz_empty );
    // FIXME_AZIMUTH_DIAL 1,2
    mBMdial          = BitmapFactory.decodeResource( res, R.drawable.iz_dial_transp ); 
    mDialBitmap      = TopoDroidApp.getDialBitmap( res );

    mBMextend        = MyButton.getButtonBackground( mApp, res, izons[IC_EXTEND] ); 
    mBMdownload_on   = MyButton.getButtonBackground( mApp, res, R.drawable.iz_download_on );
    mBMdownload_wait = MyButton.getButtonBackground( mApp, res, R.drawable.iz_download_wait );
    mBMleft          = MyButton.getButtonBackground( mApp, res, R.drawable.iz_left );
    mBMright         = MyButton.getButtonBackground( mApp, res, R.drawable.iz_right );
    mBMbluetooth_no  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_bt_no );
    setRefAzimuth( TDAzimuth.mRefAzimuth, TDAzimuth.mFixedExtend );
    mBMsplayNone     = MyButton.getButtonBackground( mApp, res, R.drawable.iz_splay_none );
    mBMsplayFront    = MyButton.getButtonBackground( mApp, res, R.drawable.iz_splay_front );
    mBMsplayBack     = MyButton.getButtonBackground( mApp, res, R.drawable.iz_splay_back );
    mBMsplayBoth     = MyButton.getButtonBackground( mApp, res, R.drawable.iz_splay_both );
    mBMsplayNoneBlack  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_splay_none_black );
    mBMsplayFrontBlack = MyButton.getButtonBackground( mApp, res, R.drawable.iz_splay_front_black );
    mBMsplayBackBlack  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_splay_back_black );
    mBMsplayBothBlack  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_splay_both_black );

    if ( ! TDLevel.overNormal ) -- mNrButton2;
    mButton2 = new Button[ mNrButton2 + 1 ]; // DRAW
    off = (NR_BUTTON1 - 3); 
    for ( int k=0; k<mNrButton2; ++k ) {
      ic = ( k < 3 )? k : off+k;
      mButton2[k] = MyButton.getButton( mActivity, this, ((k==0)? izons_ok[ic] : izons[ic]) );
      if ( ic == IC_CONT_NONE ) mBMcont_none = MyButton.getButtonBackground( mApp, res, ((k==0)? izons_ok[ic] : izons[ic]));
    }
    mButton2[ mNrButton2 ] = mButton1[ mNrButton1 ];
    mBMcont_continue  = MyButton.getButtonBackground( mApp, res, izons[IC_CONT_CONTINUE] );
    mBMcont_start = MyButton.getButtonBackground( mApp, res, izons[IC_CONT_START] );
    mBMcont_end   = MyButton.getButtonBackground( mApp, res, izons[IC_CONT_END] );
    mBMcont_both  = MyButton.getButtonBackground( mApp, res, izons[IC_CONT_BOTH] );
    mBMcont_off   = MyButton.getButtonBackground( mApp, res, izons[IC_CONT_OFF] );
    mBMdelete_off = MyButton.getButtonBackground( mApp, res, izons[IC_DELETE_OFF] );
    mBMdelete_on  = MyButton.getButtonBackground( mApp, res, izons[IC_DELETE_ON] );

    if ( ! TDLevel.overExpert ) -- mNrButton3;
    mButton3 = new Button[ mNrButton3 + 1 ];      // EDIT
    off = (NR_BUTTON1 - 3) + (NR_BUTTON2 - 3); 
    for ( int k=0; k<mNrButton3; ++k ) {
      ic = ( k < 3 )? k : off+k;
      mButton3[k] = MyButton.getButton( mActivity, this, ((k==2)? izons_ok[ic] : izons[ic]) );
      if ( ic == IC_JOIN ) 
        mBMjoin = MyButton.getButtonBackground( mApp, res, ((k==2)? izons_ok[ic] : izons[ic]) );
    }
    if ( TDLevel.overExpert ) {
      if ( BTN_BORDER < mButton3.length ) {
        mButton3[ BTN_BORDER ].setPadding(4,4,4,4);
        mButton3[ BTN_BORDER ].setTextColor( 0xffffffff );
      }
      mBMedit_box= MyButton.getButtonBackground( mApp, res, izons[IC_BORDER_BOX] );
      mBMedit_ok = MyButton.getButtonBackground( mApp, res, izons[IC_BORDER_OK] ); 
      mBMedit_no = MyButton.getButtonBackground( mApp, res, izons[IC_BORDER_NO] );
    }
    mButton3[ mNrButton3 ] = mButton1[ mNrButton1 ];
    mBMjoin_no = MyButton.getButtonBackground( mApp, res, izons[IC_JOIN_NO] );
    mBMadd     = MyButton.getButtonBackground( mApp, res, izons[IC_ADD] );


    mButton5 = new Button[ mNrButton5 + 1 ];    // ERASE
    off = 9 - 3; // (mNrButton1-3) + (mNrButton2-3) + (mNrButton3-3);
    for ( int k=0; k<mNrButton5; ++k ) {
      ic = ( k < 3 )? k : off+k;
      mButton5[k] = MyButton.getButton( mActivity, this, ((k==1)? izons_ok[ic] : izons[ic] ) );
    }
    mButton5[ mNrButton5 ] = mButton1[ mNrButton1 ];
    mBMeraseAll   = MyButton.getButtonBackground( mApp, res, izons[IC_ERASE_ALL] );
    mBMerasePoint = MyButton.getButtonBackground( mApp, res, izons[IC_ERASE_POINT] );
    mBMeraseLine  = MyButton.getButtonBackground( mApp, res, izons[IC_ERASE_LINE] );
    mBMeraseArea  = MyButton.getButtonBackground( mApp, res, izons[IC_ERASE_AREA] );
    setButtonFilterMode( mEraseMode, Drawing.CODE_ERASE );

    mBMprev        = MyButton.getButtonBackground( mApp, res, izons[IC_PREV] );
    mBMnext        = MyButton.getButtonBackground( mApp, res, izons[IC_NEXT] );
    mBMselectAll   = MyButton.getButtonBackground( mApp, res, izons[IC_SELECT_ALL] );
    mBMselectPoint = MyButton.getButtonBackground( mApp, res, izons[IC_SELECT_POINT] );
    mBMselectLine  = MyButton.getButtonBackground( mApp, res, izons[IC_SELECT_LINE] );
    mBMselectArea  = MyButton.getButtonBackground( mApp, res, izons[IC_SELECT_AREA] );
    mBMselectShot  = MyButton.getButtonBackground( mApp, res, izons[IC_SELECT_SHOT] );
    mBMselectStation=MyButton.getButtonBackground( mApp, res, izons[IC_SELECT_STATION] );
    setButtonFilterMode( mSelectMode, Drawing.CODE_SELECT );

    mBMsmall  = MyButton.getButtonBackground( mApp, res, izons[IC_SMALL] );
    mBMmedium = MyButton.getButtonBackground( mApp, res, izons[IC_MEDIUM] );
    mBMlarge  = MyButton.getButtonBackground( mApp, res, izons[IC_LARGE] );
    setButtonEraseSize( Drawing.SCALE_MEDIUM );
    setButtonSelectSize( Drawing.SCALE_MEDIUM );

    mButtonView1 = new HorizontalButtonView( mButton1 );
    mButtonView2 = new HorizontalButtonView( mButton2 );
    mButtonView3 = new HorizontalButtonView( mButton3 );
    mButtonView5 = new HorizontalButtonView( mButton5 );
  }

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // TDLog.TimeStart();
    // Log.v("DistoX", "onCreate()" );

    mApp  = (TopoDroidApp)getApplication();
    mActivity = this;
    mApp_mData = TopoDroidApp.mData; // new DataHelper( this ); 

    mFormatClosure = getResources().getString(R.string.format_closure );

    audioCheck = TDandroid.checkMicrophone( mActivity );

    mZoomBtnsCtrlOn = (TDSetting.mZoomCtrl > 1);  // do before setting content
    mPointScale = DrawingPointPath.SCALE_M;

    // Display display = getWindowManager().getDefaultDisplay();
    // DisplayMetrics dm = new DisplayMetrics();
    // display.getMetrics( dm );
    // int width = dm widthPixels;
    int width = getResources().getDisplayMetrics().widthPixels;

    // mIsNotMultitouch = ! TDandroid.checkMultitouch( this );

    setContentView(R.layout.drawing_activity);
    mDataDownloader   = mApp.mDataDownloader; // new DataDownloader( this, mApp );
    mZoom             = TopoDroidApp.mScaleFactor;    // canvas zoom
    mBorderRight      = TopoDroidApp.mDisplayWidth * 15 / 16;
    mBorderLeft       = TopoDroidApp.mDisplayWidth / 16;
    mBorderInnerRight = TopoDroidApp.mDisplayWidth * 3 / 4;
    mBorderInnerLeft  = TopoDroidApp.mDisplayWidth / 4;
    mBorderBottom     = TopoDroidApp.mDisplayHeight * 7 / 8;

    mDisplayCenter = new PointF(TopoDroidApp.mDisplayWidth  / 2, TopoDroidApp.mDisplayHeight / 2);

    // setCurrentPaint();
    mCurrentBrush = new DrawingPenBrush();

    mDrawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
    mDrawingSurface.setZoomer( this );
    mDrawingSurface.makePreviewPath( DrawingPath.DRAWING_PATH_LINE, DrawingWindow.getPreviewPaint() );
    mDrawingSurface.setOnTouchListener(this);
    // mDrawingSurface.setOnLongClickListener(this);
    // mDrawingSurface.setBuiltInZoomControls(true);

    mZoomView = (View) findViewById(R.id.zoomView );
    mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
    // FIXME_ZOOM_CTRL mZoomCtrl = (ZoomControls) mZoomBtnsCtrl.getZoomControls();
    // ViewGroup vg = mZoomBtnsCtrl.getContainer();
    // switchZoomCtrl( TDSetting.mZoomCtrl );

    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlacholder(true);
    mButtonSize = TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    // mImage.setBackgroundResource( icons00[ IC_MENU ] );
    TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( mApp, getResources(), izons[IC_MENU] ) );
    mMenu = (ListView) findViewById( R.id.menu );
    // HOVER
    mMenu.setOnItemClickListener( this );

    // mEraseScale = 0;  done in makeButtons()
    // mSelectScale = 0;
    makeButtons( );

    if ( ! TDLevel.overNormal ) {
      mButton1[2].setVisibility( View.GONE );
      mButton2[2].setVisibility( View.GONE );
      mButton3[2].setVisibility( View.GONE );
      mButton5[2].setVisibility( View.GONE );
    } else {
      mButton3[2].setOnLongClickListener( this ); // options
      mButton2[0].setOnLongClickListener( this );
      mButton5[1].setOnLongClickListener( this );
    }
    if ( TDLevel.overAdvanced ) {
      mButton1[BTN_DOWNLOAD].setOnLongClickListener( this );
    }
    if ( TDLevel.overBasic ) {
      if ( BTN_PLOT   < mButton1.length ) mButton1[BTN_PLOT].setOnLongClickListener( this );
      if ( BTN_REMOVE < mButton3.length ) mButton3[BTN_REMOVE].setOnLongClickListener( this );
    }
 
    setConnectionStatus( mDataDownloader.getStatus() );
    mListView.setAdapter( mButtonView1.mAdapter );
    // mListView.invalidate();

    // redoBtn.setEnabled(false);
    // undoBtn.setEnabled(false); // let undo always be there

    BrushManager.makePaths( mApp, getResources() );
    setTheTitle();

    // mBezierInterpolator = new BezierInterpolator( );

    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      mSid   = extras.getLong( TDTag.TOPODROID_SURVEY_ID );
      // mDecl = mApp_mData.getSurveyDeclination( mSid );
      mDecl = 0; // FIXME do not correct declination in sketches

      mName1 = extras.getString( TDTag.TOPODROID_PLOT_NAME );
      mName2 = extras.getString( TDTag.TOPODROID_PLOT_NAME2 );
      mFullName1 = TDInstance.survey + "-" + mName1;
      mFullName2 = TDInstance.survey + "-" + mName2;
      mFullName3 = null;
      mType = extras.getLong( TDTag.TOPODROID_PLOT_TYPE );

      mName    = (mType == PlotInfo.PLOT_PLAN)? mName1 : mName2;
      mFrom    = extras.getString( TDTag.TOPODROID_PLOT_FROM );
      mTo      = extras.getString( TDTag.TOPODROID_PLOT_TO );
      mAzimuth = extras.getFloat( TDTag.TOPODROID_PLOT_AZIMUTH );
      mClino   = extras.getFloat( TDTag.TOPODROID_PLOT_CLINO );
      mMoveTo  = extras.getString( TDTag.TOPODROID_PLOT_MOVE_TO );
      mLandscape = extras.getBoolean( TDTag.TOPODROID_PLOT_LANDSCAPE );
    } 
    // // mDrawingUtil = mLandscape ? (new DrawingUtilLandscape()) : ( new DrawingUtilPortrait());
    // mDrawingUtil = new DrawingUtilPortrait();

    if ( mMoveTo != null && mMoveTo.length() == 0 ) mMoveTo = null; // test for Xiaomi readmi note
    mSectionName  = null; // resetStatus
    mLastLinePath = null;
    mShiftDrawing = false;
    mContinueLine = TDSetting.mContinueLine;
    resetModified();

    // if ( PlotInfo.isSection( mType ) ) { 
    //   mTo      = extras.getString( TDTag.TOPODROID_PLOT_TO );  // to station ( null for X-section)
    //   mAzimuth = (float)extras.getLong( TDTag.TOPODROID_PLOT_AZIMUTH );
    //   mClino   = (float)extras.getLong( TDTag.TOPODROID_PLOT_CLINO );
    //   // Log.v("DistoX", "X-Section " + mFrom + "-" + mTo + " azimuth " + mAzimuth + " clino " + mClino  );
    // } else if ( PlotInfo.isXSection( mType ) ) {
    //   mTo = null;
    //   mAzimuth = (float)extras.getLong( TDTag.TOPODROID_PLOT_AZIMUTH );
    //   mClino   = (float)extras.getLong( TDTag.TOPODROID_PLOT_CLINO );
    // }

    // TDLog.TimeEnd( "on create" );

    doStart( true, -1 );

    setMenuAdapter( getResources(), mType );
    closeMenu();

    if ( mDataDownloader != null ) {
      mApp.registerLister( this );
    } 

    // if ( mApp.hasHighlighted() ) {
    //   // Log.v("DistoX", "drawing window [2] highlighted " + mApp.getHighlightedSize() );
    //   mDrawingSurface.highlights( mApp );
    //   mApp.mShotWindow.clearMultiSelect();
    // }

    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on create done");
  }

  // ==============================================================

  // called by PlotListDialog
  void switchNameAndType( String name, long tt ) // SWITCH
  {
    // mApp.mShotWindow.setRecentPlot( name, tt );
    TDInstance.setRecentPlot( name, tt );

    PlotInfo p1 = mApp_mData.getPlotInfo( TDInstance.sid, name+"p" );
    if ( mPid1 == p1.id ) {
      if ( tt != mType ) { // switch plot type
        startSaveTdrTask( mType, PlotSave.TOGGLE, TDSetting.mBackupNumber+2, TDPath.NR_BACKUP ); 
        // mDrawingSurface.clearDrawing();
        switchPlotType();
      }
      return;
    }
    startSaveTdrTask( mType, PlotSave.SAVE, TDSetting.mBackupNumber+2, TDPath.NR_BACKUP );
    // if necessary save default export
    //
    mZoom     = TopoDroidApp.mScaleFactor;    // canvas zoom
    mOffset.x = 0;
    mOffset.y = 0;
    if ( p1 != null ) {
      // PlotInfo plot2 =  mApp_mData.getPlotInfo( TDInstance.sid, name+"s" );
      mName1 = name+"p";
      mName2 = name+"s";
      mFullName1 = TDInstance.survey + "-" + mName1;
      mFullName2 = TDInstance.survey + "-" + mName2;
      mFullName3 = null;
      mType      = tt;
      mLandscape = p1.isLandscape();
      // // mDrawingUtil = mLandscape ? (new DrawingUtilLandscape()) : ( new DrawingUtilPortrait());
      // mDrawingUtil = new DrawingUtilPortrait();
      mName      = (mType == PlotInfo.PLOT_PLAN)? mName1 : mName2;

      mFrom    = p1.start;
      mTo      = "";
      mAzimuth = 0;
      mClino   = 0;
      mMoveTo  = null;
      mSectionName  = null; // resetStatus
      mLastLinePath = null;
      mShiftDrawing = false;
      // mContinueLine = TDSetting.mContinueLine; 
      resetModified();

      doStart( true, -1 );
    }
  }

  @Override
  protected synchronized void onResume()
  {
    super.onResume();
    // mApp.resetLocale(); FIXME-LOCALE
    // Log.v("DistoX", "Drawing Activity onResume " + ((mDataDownloader!=null)?"with DataDownloader":"") );
    doResume();
    if ( mDataDownloader != null ) {
      mDataDownloader.onResume();
      setConnectionStatus( mDataDownloader.getStatus() );
    }
    // TDLog.TimeEnd( "drawing activity ready" );
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on resume done");
  }

  @Override
  protected synchronized void onPause() 
  { 
    // Log.v("DistoX", "Drawing Activity onPause " );
    doPause();
    super.onPause();
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on pause done");
  }

  @Override
  protected synchronized void onStart()
  {
    super.onStart();
    // Log.v("DistoX", "Drawing Activity onStart " + ((mDataDownloader!=null)?"with DataDownloader":"") );
    loadRecentSymbols( mApp_mData );
     mOutlinePlot1 = null;
     mOutlinePlot2 = null;
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on start done");
  }

  @Override
  protected synchronized void onStop()
  {
    super.onStop();
    // Log.v("DistoX", "Drawing Activity onStop ");
    saveRecentSymbols( mApp_mData );
    // doStop();
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on stop done");
  }

  @Override
  protected synchronized void onDestroy()
  {
    super.onDestroy();
    // Log.v("DistoX", "Drawing activity onDestroy");
    if ( mDataDownloader != null ) {
      mApp.unregisterLister( this );
    }
    // if ( mDataDownloader != null ) { // data-download management is left to ShotWindow
    //   mDataDownloader.onStop();
    //   mApp.disconnectRemoteDevice( false );
    // }
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on destroy done");
  }

  private void doResume() // restoreInstanceFromData
  {
    // Log.v("DistoX", "doResume()" );
    String mode = mApp_mData.getValue( "DISTOX_PLOT_MODE" );
    DrawingCommandManager.setDisplayMode( DisplayMode.parseString( mode ) );

    PlotInfo info = mApp_mData.getPlotInfo( mSid, mName );
    mOffset.x = info.xoffset;
    mOffset.y = info.yoffset;
    mZoom     = info.zoom;
    mDrawingSurface.isDrawing = true;
    // Log.v("DistoXC", "doResume " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    mLastLinePath = null; // necessary ???
    switchZoomCtrl( TDSetting.mZoomCtrl );
    // Log.v("DistoX", "do Resume. offset " + mOffset.x + " " + mOffset.y + " zoom " + mZoom );
    setPlotType( mType );
  }

  private void doPause()
  {
    switchZoomCtrl( 0 );
    mDrawingSurface.isDrawing = false;
    if ( mPid >= 0 ) {
      try {
        mApp_mData.updatePlot( mPid, mSid, mOffset.x, mOffset.y, mZoom );
      } catch ( IllegalStateException e ) {
        TDLog.Error("cannot save plot state: " + e.getMessage() );
      }
    }
    // TODO exec this line in a Thread
    (new Thread() {
       public void run() {
         mApp_mData.setValue( "DISTOX_PLOT_MODE", DisplayMode.toString( DrawingCommandManager.getDisplayMode() ) );
       }
    } ).start();
    doSaveTdr( ); // do not alert-dialog on mAllSymbols
  }

  // private void doStop()
  // {
  //   // TDLog.Log( TDLog.LOG_PLOT, "doStop type " + mType + " modified " + mModified );
  // }

// ----------------------------------------------------------------------------

  // called by updateDisplay if the type is not plan/profile
  private void doRestart( )
  {
    Log.v("DistoXC", "doRestart " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    mLastLinePath = null;
    List<DBlock> list = null;
    if ( PlotInfo.isSection( mType ) ) {
      list = mApp_mData.selectAllShotsAtStations( mSid, mFrom, mTo );
    } else if ( PlotInfo.isXSection( mType ) ) { 
      // N.B. mTo can be null
      list = mApp_mData.selectShotsAt( mSid, mFrom, false ); // select only splays
    }
    if ( list != null && list.size() > mSectionSkip ) {
      makeSectionReferences( list, mIntersectionT, mSectionSkip );
    }
  }

  // @param tt   used only by leg x-sections when created to insert leg intersection point
  // called by onCreate, switchPlotType, onBackPressed and pushInfo
  private void doStart( boolean do_load, float tt )
  {
    // Log.v("DistoXC", "doStart " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    assert( mLastLinePath == null); // not needed - guaranteed by callers
    mIntersectionT = tt;
    // Log.v("DistoX", "do start() tt " + tt );
    // TDLog.Log( TDLog.LOG_PLOT, "do Start() " + mName1 + " " + mName2 );
    mCurrentPoint = ( BrushManager.mPointLib.isSymbolEnabled( "label" ) )? 1 : 0;
    mCurrentLine  = ( BrushManager.mLineLib.isSymbolEnabled( "wall" ) )? 1 : 0;
    mCurrentArea  = ( BrushManager.mAreaLib.isSymbolEnabled( "water" ) )? 1 : 0;
    // mContinueLine = TDSetting.mContinueLine; // do not reset
    if ( TDLevel.overNormal ) setButtonContinue( mContinueLine );

    List<DBlock> list = null;
    if ( PlotInfo.isSection( mType ) ) {
      list = mApp_mData.selectAllShotsAtStations( mSid, mFrom, mTo );
    } else if ( PlotInfo.isXSection( mType ) ) { 
      // N.B. mTo can be null
      list = mApp_mData.selectShotsAt( mSid, mFrom, false ); // select only splays
    } else {
      list = mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );
    }

    // TDLog.TimeEnd( "before load" );

    if ( do_load ) {
      if ( ! loadFiles( mType, list ) ) {
        TDToast.makeBad( R.string.plot_not_found );
	finish();
      }
    }

    setPlotType( mType );
    // TDLog.TimeEnd( "after load" );

    // There are four types of sections:
    // SECTION and H_SECTION: mFrom != null, mTo != null, splays and leg
    // X_SECTION, XH_SECTION: mFrom != null, mTo == null, splays only 

    if ( PlotInfo.isAnySection( mType ) ) {
      DrawingUtil.addGrid( -10, 10, -10, 10, 0.0f, 0.0f, mDrawingSurface );
      makeSectionReferences( list, tt, 0 );
    // } else {
    //   Log.v("DistoX", "try to highlight [1] ");
    //   if ( mApp.hasHighlighted() ) mDrawingSurface.highlights( mApp ); 
    }
    // TDLog.TimeEnd("do start done");

    mDrawingSurface.setSelectMode( mSelectMode );
  }

  // private void makeXSectionLegPoint( float x, float y )
  // {
  //   DrawingSpecialPath path = new DrawingSpecialPath( DrawingSpecialPath.SPECIAL_DOT, DrawingUtil.toSceneX(x,y), DrawingUtil.toSceneY(x,y), DrawingLevel.LEVEL_DEFAULT );
  //   mDrawingSurface.addDrawingPath( path );
  // }
  
  // called by doRestart, doStart, doRecover
  private void makeSectionReferences( List<DBlock> list, float tt, int skip )
  {
    // Log.v("DistoXC", "makeSectionReferences " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    assert( mLastLinePath == null); // not needed - guaranteed by callers
    // Log.v("DistoX", "Section " + mClino + " " + mAzimuth );
    // DrawingUtil.addGrid( -10, 10, -10, 10, 0.0f, 0.0f, mDrawingSurface ); // FIXME_SK moved out
    float xfrom=0;
    float yfrom=0;
    float xto=0;
    float yto=0;
    // normal, horizontal and cross-product
    float mc = mClino   * TDMath.DEG2RAD;
    float ma = mAzimuth * TDMath.DEG2RAD;
    // canvas X-axis, unit horizontal axis: 90 degrees to the right of the azimuth
    //   azimuth = 0 (north) --> horizontal = ( 0N, 1E)
    //   azimuth = 90 (east) --> horizontal = (-1N, 0E)
    //   etc.
    // canvas UP-axis: this is X0 ^ X1 : it goes up in the section plane 
    // canvas Y-axis = - UP-axis

    // FIXME_VECTOR
    // float X0 = (float)Math.cos( mc ) * (float)Math.cos( ma );  // X = North
    // float Y0 = (float)Math.cos( mc ) * (float)Math.sin( ma );  // Y = East
    // float Z0 = (float)Math.sin( mc );                          // Z = Up
    // float X1 = - (float)Math.sin( ma ); // X1 goes to the left in the section plane !!!
    // float Y1 =   (float)Math.cos( ma ); 
    // float Z1 = 0;
    // // float X2 = - (float)Math.sin( mc ) * (float)Math.cos( ma );
    // // float Y2 = - (float)Math.sin( mc ) * (float)Math.sin( ma );
    // // float Z2 =   (float)Math.cos( ma );
    // float X2 = Y0 * Z1 - Y1 * Z0; 
    // float Y2 = Z0 * X1 - Z1 * X0;
    // float Z2 = X0 * Y1 - X1 * Y0;
    Vector V0 = new Vector( ma, mc );
    Vector V1 = new Vector( - (float)Math.sin( ma ), (float)Math.cos( ma ), 0 );
    Vector V2 = V0.cross( V1 );

    float dist = 0;
    DBlock blk = null;
    float xn = 0;  // X-North // Rotate as NORTH is upward
    float yn = -1; // Y-North
    if ( skip == 0 ) {
      if ( PlotInfo.isSection( mType ) ) {
        if ( mType == PlotInfo.PLOT_H_SECTION ) {
          if ( Math.abs( mClino ) > TDSetting.mHThreshold ) { // north arrow == (1,0,0), 5 m long in the CS plane
            // FIXME_VECTOR
            // xn =  X1;
            // yn = -X2;
            xn =  V1.x;
            yn = -V2.x; 

            float d = 5 / (float)Math.sqrt(xn*xn + yn*yn);
            if ( mClino > 0 ) xn = -xn;
            // FIXME_NORTH addFixedSpecial( xn*d, yn*d, 0, 0, 0, 0 ); 
            // addFixedSpecial( 0, -d, 0, 0, 0, 0 ); // NORTH is upward
            // if ( mLandscape ) {
            //   addFixedSpecial( -d, 0, 0, 0 ); // NORTH is leftward
            // } else {
              addFixedSpecial( 0, -d, 0, 0 ); // NORTH is upward
            // }
          }
        }
        for ( DBlock b : list ) {
          if ( b.isSplay() ) continue;
          if ( mFrom.equals( b.mFrom ) && mTo.equals( b.mTo ) ) { // FROM --> TO
            dist = b.mLength;
            blk = b;
            break;
          } else if ( mFrom.equals( b.mTo ) && mTo.equals( b.mFrom ) ) { // TO --> FROM
            dist = - b.mLength;
            blk = b;
            break;
          }
        }
        if ( blk != null ) {
          // FIXME_VECTOR
          // float bc = blk.mClino * TDMath.DEG2RAD;
          // float bb = blk.mBearing * TDMath.DEG2RAD;
          // float X = (float)Math.cos( bc ) * (float)Math.cos( bb );
          // float Y = (float)Math.cos( bc ) * (float)Math.sin( bb );
          // float Z = (float)Math.sin( bc );
          // xfrom = -dist * (X1 * X + Y1 * Y + Z1 * Z); // neg. because it is the FROM point
          // yfrom =  dist * (X2 * X + Y2 * Y + Z2 * Z);
	  Vector v = new Vector( blk.mBearing * TDMath.DEG2RAD, blk.mClino * TDMath.DEG2RAD );
          xfrom = -dist * v.dot(V1); // neg. because it is the FROM point
          yfrom =  dist * v.dot(V2);

          if ( mType == PlotInfo.PLOT_H_SECTION ) { // Rotate as NORTH is upward
            float xx = -yn * xfrom + xn * yfrom;
            yfrom = -xn * xfrom - yn * yfrom;
            xfrom = xx;
          }
          // addFixedLine( mType, blk, xfrom, yfrom, xto, yto, 0, 0, false, false ); // not-splay, not-selecteable
          addFixedLine( mType, blk, xfrom, yfrom, xto, yto, blk.getReducedExtend(), false, false ); // not-splay, not-selecteable
          mDrawingSurface.addDrawingStationName( mFrom, DrawingUtil.toSceneX(xfrom, yfrom), DrawingUtil.toSceneY(xfrom, yfrom) );
          mDrawingSurface.addDrawingStationName( mTo, DrawingUtil.toSceneX(xto, yto), DrawingUtil.toSceneY(xto, yto) );
          if ( tt >= 0 && tt <= 1 ) {
            float xtt = xfrom + tt * ( xto - xfrom );
            float ytt = yfrom + tt * ( yto - yfrom );
            if ( mLandscape ) { float t=xtt; xtt=-ytt; ytt=t; }
            // Log.v("DistoX", "TT " + tt + " " + xtt + " " + xfrom + " " + xto );
            // makeXSectionLegPoint( xtt, ytt );
            DrawingSpecialPath path = new DrawingSpecialPath( DrawingSpecialPath.SPECIAL_DOT, DrawingUtil.toSceneX(xtt,ytt), DrawingUtil.toSceneY(xtt,ytt), DrawingLevel.LEVEL_DEFAULT );
            mDrawingSurface.addDrawingPath( path );
          }
        }
      } else { // if ( PlotInfo.isXSection( mType ) ) 
        mDrawingSurface.addDrawingStationName( mFrom, DrawingUtil.toSceneX(xfrom, yfrom), DrawingUtil.toSceneY(xfrom, yfrom) );
      }
    }

    int cnt = 0;
    for ( DBlock b : list ) { // repeat for splays
      ++cnt;
      if ( cnt < skip || ! b.isSplay() ) continue;
  
      int splay_station = 3; // could use a boolean
      if ( b.mFrom.equals( mFrom ) ) {
        splay_station = 1;
        // if ( TDSetting.mSectionStations == 2 ) continue;
      } else if ( b.mFrom.equals( mTo ) ) {
        splay_station = 2;
        // if ( TDSetting.mSectionStations == 1 ) continue;
      } else {
        continue;
      }

      float d = b.mLength;
      // FIXME_VECTOR
      // float bc = b.mClino * TDMath.DEG2RAD;
      // float bb = b.mBearing * TDMath.DEG2RAD;
      // float X = (float)Math.cos( bc ) * (float)Math.cos( bb ); // North
      // float Y = (float)Math.cos( bc ) * (float)Math.sin( bb ); // East
      // float Z = (float)Math.sin( bc );                       // Up
      // float x =  d * (X1 * X + Y1 * Y + Z1 * Z);
      // float y = -d * (X2 * X + Y2 * Y + Z2 * Z);
      // float a = 90 - (float)(Math.acos(X0 * X + Y0 * Y + Z0 * Z) * TDMath.RAD2DEG); // cos-angle with the normal
      Vector v = new Vector(  b.mBearing * TDMath.DEG2RAD, b.mClino * TDMath.DEG2RAD);
      float x =  d * v.dot(V1);
      float y = -d * v.dot(V2);
      float a = 90 - (float)(Math.acos( v.dot(V0) ) * TDMath.RAD2DEG); // cos-angle with the normal
      
      if ( mType == PlotInfo.PLOT_H_SECTION ) { // Rotate as NORTH is upward
        float xx = -yn * x + xn * y;
        y = -xn * x - yn * y;
        x = xx;
      }
      // Log.v("DistoX", "splay " + d + " " + b.mBearing + " " + b.mClino + " coord " + X + " " + Y + " " + Z );
      if ( splay_station == 1 ) {
        // N.B. this must be guaranteed for X_SECTION
        // addFixedSectionSplay( b, xfrom, yfrom, xfrom+x, yfrom+y, 0, 0, false );
        addFixedSectionSplay( b, xfrom, yfrom, xfrom+x, yfrom+y, a, false );
      } else { // if ( splay_station == 2
        // addFixedSectionSplay( b, xto, yto, xto+x, yto+y, 0, 0, true );
        addFixedSectionSplay( b, xto, yto, xto+x, yto+y, a, true );
      }
    }
    mSectionSkip = cnt;
    // mDrawingSurface.setScaleBar( mCenter.x, mCenter.y ); // (90,160) center of the drawing
  }

  private boolean loadFiles( long type, List<DBlock> list )
  {
    // Log.v("DistoXC", "loadFiles " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    assert( mLastLinePath == null ); // guaranteed when called
    // Log.v("DistoX", "load files()" );
    
    // String filename1  = null;
    String filename1b = null;
    // String filename2  = null;
    String filename2b = null;
    // String filename3  = null;
    String filename3b = null;

    if ( PlotInfo.isSketch2D( type ) ) {
      // Log.v( "DistoX", "load files type " + type + " " + mName1 + " " + mName2 );
      mPlot1 = mApp_mData.getPlotInfo( mSid, mName1 );
      mPlot2 = mApp_mData.getPlotInfo( mSid, mName2 );
      if ( mPlot1 == null ) return false;
      if ( mPlot2 == null ) return false;
      mPid1  = mPlot1.id;
      mPid2  = mPlot2.id;
      // Log.v("DistoX", "Plot2 type " + mPlot2.type + " azimuth " + mPlot2.azimuth );
      mPid = mPid1;
      // filename1  = TDPath.getTh2FileWithExt( mFullName1 );
      filename1b = TDPath.getTdrFileWithExt( mFullName1 );
      // filename2  = TDPath.getTh2FileWithExt( mFullName2 );
      filename2b = TDPath.getTdrFileWithExt( mFullName2 );
    } else {
      // Log.v( "DistoX", "load files type " + type + " " + mName3 );
      mPlot3 = mApp_mData.getPlotInfo( mSid, mName3 );
      if ( mPlot3 == null ) return false;
      mPid3  = mPlot3.id;
      // filename3  = TDPath.getTh2FileWithExt( mFullName3 );
      filename3b = TDPath.getTdrFileWithExt( mFullName3 );
    }

    // mAllSymbols  = true; // by default there are all the symbols
    SymbolsPalette missingSymbols = null; // new SymbolsPalette(); 
    // missingSymbols = palette of missing symbols
    // if there are missing symbols mAllSymbols is false and the MissingDialog is shown
    //    (the dialog just warns the user about missing symbols, maybe a Toast would be enough)
    // when the sketch is saved, mAllSymbols is checked ( see doSaveTdr )
    // if there are not all symbols the user is asked if he/she wants to save anyways

    if ( PlotInfo.isSketch2D( type ) ) {
      if ( list.size() > 0 ) {
        mNum = new DistoXNum( list, mPlot1.start, mPlot1.view, mPlot1.hide, mDecl, mFormatClosure );
      } else {
        mNum = null;
        // TDToast.makeBad( R.string.few_data );
        // if ( mPid1 >= 0 ) mApp_mData.dropPlot( mPid1, mSid );
        // if ( mPid2 >= 0 ) mApp_mData.dropPlot( mPid2, mSid );
        // finish();
      }

      if ( ! mDrawingSurface.resetManager( DrawingSurface.DRAWING_PLAN, mFullName1, false ) ) {
        // mAllSymbols =
        mDrawingSurface.modeloadDataStream( filename1b, /* filename1, */ missingSymbols );
        DrawingSurface.addManagerToCache( mFullName1 );
      }
      if ( ! mDrawingSurface.resetManager( DrawingSurface.DRAWING_PROFILE, mFullName2, PlotInfo.isExtended(mPlot2.type) ) ) {
        // mAllSymbols = mAllSymbols &&
        mDrawingSurface.modeloadDataStream( filename2b, /* filename2, */ missingSymbols );
        DrawingSurface.addManagerToCache( mFullName2 );
      }
      
      String parent = ( TDInstance.xsections? null : mName);
      List<PlotInfo> xsection_plan = mApp_mData.selectAllPlotSectionsWithType( TDInstance.sid, 0, PlotInfo.PLOT_X_SECTION,  parent );
      List<PlotInfo> xsection_ext  = mApp_mData.selectAllPlotSectionsWithType( TDInstance.sid, 0, PlotInfo.PLOT_XH_SECTION, parent );

      computeReferences( mPlot2.type, mPlot2.name, mZoom, true );
      computeReferences( mPlot1.type, mPlot1.name, mZoom, false );
      if ( mNum == null ) {
        TDToast.makeBad( R.string.survey_no_data_reduction );
      }

      doMoveTo();

      mDrawingSurface.setStationXSections( xsection_plan, xsection_ext, mPlot2.type );
      mDrawingSurface.linkAllSections();
    } else {
      mTo = ( PlotInfo.isSection( type ) )? mPlot3.view : "";
      mDrawingSurface.resetManager( DrawingSurface.DRAWING_SECTION, null, false );
      // mAllSymbols =
      mDrawingSurface.modeloadDataStream( filename3b, /* filename3, */ missingSymbols );
      mDrawingSurface.addScaleRef( DrawingSurface.DRAWING_SECTION, (int)type );
    }

    // if ( ! mAllSymbols ) {
    //   String msg = missingSymbols.getMessage( getResources() );
    //   // TDLog.Log( TDLog.LOG_PLOT, "Missing " + msg );
    //   TDToast.makeLong( "Missing symbols \n" + msg );
    //   // (new MissingDialog( mActivity, this, msg )).show();
    //   // finish();
    // }
    return true;
  }

  // called by doResume and doStart
  private void setPlotType( long type )
  {
    // Log.v("DistoXC", "setPlotType " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    assert( mLastLinePath == null );
    if ( PlotInfo.isProfile( type ) ) {
      setPlotType2( false );
    } else if ( type == PlotInfo.PLOT_PLAN ) { 
      setPlotType1( false );
    } else {
      setPlotType3();
    }
  }

  private void updateReference()
  {
    Log.v("DistoXC", "updateReference " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    // mLastLinePath = null; // should not be necessary
    // Log.v("DistoX", "update Reference()" );
    if ( mType == PlotInfo.PLOT_PLAN ) {
      saveReference( mPlot1, mPid1 );
    } else if ( PlotInfo.isProfile( mType ) ) {
      saveReference( mPlot2, mPid2 );
    }
  }

  // called by updateReefernce and moveTo
  private void saveReference( PlotInfo plot, long pid )
  {
    // Log.v("DistoX", "save Reference()" );
    // Log.v("DistoX", "save pid " + pid + " ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
    plot.xoffset = mOffset.x;
    plot.yoffset = mOffset.y;
    plot.zoom    = mZoom;
    mApp_mData.updatePlot( pid, mSid, mOffset.x, mOffset.y, mZoom );
  }

  private void resetReference( PlotInfo plot )
  {
    Log.v("DistoXC", "resetReference " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    mLastLinePath = null;
    mOffset.x = plot.xoffset; 
    mOffset.y = plot.yoffset; 
    mZoom     = plot.zoom;    
    // Log.v("DistoX", "reset ref " + mOffset.x + " " + mOffset.y + " " + mZoom ); // DATA_DOWNLOAD
    mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );
  }

  // ----------------------------------------------------
  // previewPaint is not thread safe, but it is ok if two threads make two preview paints
  // eventually only one remains
  static private Paint previewPaint = null;
  static public  Paint getPreviewPaint()
  {
    if ( previewPaint != null ) return previewPaint;
    Paint paint = new Paint();
    paint.setColor(0xFFC1C1C1);
    paint.setStyle(Paint.Style.STROKE);
    paint.setStrokeJoin(Paint.Join.ROUND);
    paint.setStrokeCap(Paint.Cap.ROUND);
    paint.setStrokeWidth( BrushManager.WIDTH_PREVIEW );
    previewPaint = paint;
    return paint;
  }

  // called only onTouchUp
  // x,y scene points
  private void doSelectAt( float x, float y, float size )
  {
      // Log.v("DistoXC", "doSelectAt " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      if ( mLandscape ) { float t=x; x=-y; y=t; }
      // Log.v("DistoX", "select at: edit-range " + mDoEditRange + " mode " + mMode + " At " + x + " " + y );
      if ( mMode == MODE_EDIT ) {
        if ( TDLevel.overExpert ) {
	  // PATH_MULTISELECTION
          // if ( mDrawingSurface.isMultiselection() ) {
          //   mDrawingSurface.addItemAt( x, y, mZoom, size );
	  //   return;
          // }
          if ( mDoEditRange > 0 ) {
            // mDoEditRange = false;
            // TDandroid.setButtonBackground( mButton3[ BTN_BORDER ], mBMedit_no );
            if ( mDrawingSurface.setRangeAt( x, y, mZoom, mDoEditRange, size ) ) {
              mMode = MODE_SHIFT;
              return;
            }
          }
        } 
        // float d0 = TopoDroidApp.mCloseCutoff + TopoDroidApp.mSelectness / mZoom;
        SelectionSet selection = mDrawingSurface.getItemsAt( x, y, mZoom, mSelectMode, size );
        // Log.v( TopoDroidApp.TAG, "selection at " + x + " " + y + " items " + selection.size() );
        // Log.v( TopoDroidApp.TAG, " zoom " + mZoom + " radius " + d0 );
        mHasSelected = mDrawingSurface.hasSelected();
        setButton3PrevNext();
        if ( mHasSelected ) {
          if ( mDoEditRange == 0 ) {
            mMode = MODE_SHIFT;
          }
          setButton3Item( selection.mHotItem );
        } else {
          setButton3Item( null );
        }
      } 
    }

    // x,y scene points
    private void doEraseAt( float x, float y )
    {
      if ( mLandscape ) { float t=x; x=-y; y=t; }
      mDrawingSurface.eraseAt( x, y, mZoom, mEraseCommand, mEraseMode, mEraseSize );
      modified();
    }

    void updateBlockName( DBlock block, String from, String to )
    {
      // if ( mFullName2 == null ) return; // nothing for PLOT_SECTION or PLOT_H_SECTION
      if ( PlotInfo.isAnySection( mType ) )  return;
      // FIXME if ( from == null || to == null ) return;

      if ( block.mFrom == null ) {
        if (from == null) return;
      } else {
        if ( block.mFrom.equals(from) ) return;
      }
      if ( block.mTo == null ) {
        if ( to == null ) return;
      } else {
        if ( block.mTo.equals( to )) return;
      }

      block.mFrom = from;
      block.mTo   = to;
      mApp_mData.updateShotName( block.mId, mSid, from, to, true );
      doComputeReferences( true );
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );

      modified();
    }
 
    void updateBlockComment( DBlock block, String comment ) 
    {
      if ( comment.equals( block.mComment ) ) return;
      block.mComment = comment;
      mApp_mData.updateShotComment( block.mId, mSid, comment, true ); // true = forward
    }
    
    void updateBlockFlag( DBlock blk, long flag, DrawingPath shot )
    {
      if ( blk.getFlag() == flag ) return;
      blk.resetFlag( flag );
      // the next is really necessary only if flag || mFlag is FLAG_COMMENTED:
      if ( PlotInfo.isProfile( mType ) ) {
        if ( TDSetting.mDashSplay ) {
          shot.setSplayPaintPlan( blk, blk.getReducedIntExtend(), BrushManager.darkBluePaint, BrushManager.deepBluePaint );
        } else {
          shot.setSplayPaintProfile( blk, BrushManager.darkBluePaint, BrushManager.deepBluePaint );
        }
      } else {
        shot.setSplayPaintPlan( blk, blk.getReducedIntExtend(), BrushManager.deepBluePaint, BrushManager.darkBluePaint );
      }
      mApp_mData.updateShotFlag( blk.mId, mSid, flag, true );
    }
    
    void clearBlockSplayLeg( DBlock blk, DrawingPath shot )
    {
      // Log.v("DistoX", "clear splay leg " + blk.mId + "/" + mSid + " reset shot paint ");
      blk.setTypeSplay();
      if ( shot.mBlock != null ) shot.mBlock.setTypeSplay();
      mApp_mData.updateShotLeg( blk.mId, mSid, LegType.NORMAL, false );
      // the next is really necessary only if flag || mFlag is FLAG_COMMENTED:
      if ( PlotInfo.isProfile( mType ) ) {
        if ( TDSetting.mDashSplay ) {
          shot.setSplayPaintPlan( blk, blk.getReducedIntExtend(), BrushManager.darkBluePaint, BrushManager.deepBluePaint );
        } else {
          shot.setSplayPaintProfile( blk, BrushManager.darkBluePaint, BrushManager.deepBluePaint );
        }
      } else {
        shot.setSplayPaintPlan( blk, blk.getReducedIntExtend(), BrushManager.deepBluePaint, BrushManager.darkBluePaint );
      }
    }

    // called be DrawingShotDialog and onTouch
    void updateBlockExtend( DBlock block, int extend, float stretch )
    {
      // if ( ! block.isSplay() ) extend -= DBlock.EXTEND_FVERT;
      if ( block.getExtend() == extend && block.hasStretch( stretch ) ) return;
      block.setExtend( extend, stretch );
      mApp_mData.updateShotExtend( block.mId, mSid, extend, stretch, true );
      recomputeProfileReference();
    }

    // only PLOT_EXTENDED ( not PLOT_PROJECTED )
    // used only when a shot extend is changed
    private void recomputeProfileReference()
    {
      // Log.v("DistoXC", "recomputeProfileReference " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      if ( mType == PlotInfo.PLOT_EXTENDED ) { 
        List<DBlock> list = mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );
        mNum = new DistoXNum( list, mPlot1.start, mPlot1.view, mPlot1.hide, mDecl, mFormatClosure );
	if ( mNum != null ) {
          computeReferences( (int)mType, mName, TopoDroidApp.mScaleFactor, false );
          mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );
          modified();
	}
      } 
    }

    private void deleteSplay( DrawingPath p, SelectionPoint sp, DBlock blk )
    {
      mDrawingSurface.deleteSplay( p, sp ); 
      mApp_mData.deleteShot( blk.mId, TDInstance.sid, TDStatus.DELETED, true );
      TopoDroidApp.mShotWindow.updateDisplay(); // FIXME ???
    }

    void deletePoint( DrawingPointPath point )
    {
      if ( point == null ) return;
      // Log.v("DistoXC", "deletePoint " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      mDrawingSurface.deletePath( point ); 
      // Log.v("DistoX", "delete point type " + point.mPointType );
      if ( BrushManager.isPointPhoto( point.mPointType ) ) {
        DrawingPhotoPath photo = (DrawingPhotoPath)point;
        mApp_mData.deletePhoto( TDInstance.sid, photo.mId );
        TDUtil.deleteFile( TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString( photo.mId ) ) );
      } else if ( BrushManager.isPointAudio( point.mPointType ) ) {
        DrawingAudioPath audio = (DrawingAudioPath)point;
        mApp_mData.deleteAudio( TDInstance.sid, audio.mId );

        TDUtil.deleteFile( TDPath.getSurveyAudioFile( TDInstance.survey, Long.toString( audio.mId ) ) );
      } else if ( BrushManager.isPointSection( point.mPointType ) ) {
        mDrawingSurface.clearXSectionOutline( point.getOption( "-scrap" ) );
      }
      modified();
    }

    void splitLine( DrawingLinePath line, LinePoint point )
    {
      mDrawingSurface.splitLine( line, point );
      // Log.v("DistoXC", "splitLine " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      modified();
    }

    void /*boolean*/ removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp ) 
    {
      boolean ret = mDrawingSurface.removeLinePoint(line, point, sp); 
      if ( ret ) {
        modified();
      }
      // return ret;
    }


    // @param xs_id      section-line id 
    // @param scrap_name xsection scrap_name = survey_name + "-" + xsection_id
    void deleteLine( DrawingLinePath line ) 
    { 
      if ( line.mLineType == BrushManager.mLineLib.mLineSectionIndex ) {
        deleteSectionLine( line );
      } else {
        mDrawingSurface.deletePath( line );
      }
      // Log.v("DistoXC", "deleteLine " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      modified();
    }

    private void deleteSectionLine( DrawingLinePath line )
    {
      String xs_id = line.getOption( "-id" );
      String scrap_name = TDInstance.survey + "-" + xs_id;
      mDrawingSurface.deleteSectionLine( line, scrap_name );
      TDPath.deletePlotFileWithBackups( TDPath.getTh2File( scrap_name + ".th2" ) );
      TDPath.deletePlotFileWithBackups( TDPath.getTdrFile( scrap_name + ".tdr" ) );
      TDPath.deleteFile( TDPath.getJpgFile( TDInstance.survey, xs_id + ".jpg" ) );

      // section point is deleted automatically
      // deleteSectionPoint( xs_id ); // delete section point and possibly clear section outline
      mDrawingSurface.clearXSectionOutline( scrap_name ); // clear outline if any
     
      PlotInfo plot = mApp_mData.getPlotInfo( TDInstance.sid, xs_id );
      if ( plot != null ) {
        mApp_mData.dropPlot( plot.id, TDInstance.sid );
      } else {
        TDLog.Error("Delete section line. No plot NAME " + xs_id + " SID " + TDInstance.sid );
      }
    }

    void sharpenLine( DrawingLinePath line )
    {
      // Log.v("DistoXC", "sharpenLine " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      mDrawingSurface.sharpenPointLine( line );
      modified();
    }

    void reduceLine( DrawingLinePath line, int decimation )
    {
      // Log.v("DistoXC", "reduceLine " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      mDrawingSurface.reducePointLine( line, decimation );
      modified();
    }

    void rockLine( DrawingLinePath line )
    {
      // Log.v("DistoXC", "rockLine " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      mDrawingSurface.rockPointLine( line );
      modified();
    }

    void closeLine( DrawingLinePath line )
    {
      // Log.v("DistoXC", "closeLine " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      mDrawingSurface.closePointLine( line );
      modified();
    }

    void reduceArea( DrawingAreaPath area, int decimation )
    {
      // Log.v("DistoXC", "reduceArea " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      mDrawingSurface.reducePointLine( area, decimation );
      modified();
    }


    void deleteArea( DrawingAreaPath area )
    {
      // Log.v("DistoXC", "deleteArea " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      mDrawingSurface.deletePath( area );
      modified();
    }

  // ----------------------------------------------------------------
  /*
  private void dumpEvent( MotionEventWrap ev )
  {
    String name[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "PTR_DOWN", "PTR_UP", "7?", "8?", "9?" };
    StringBuilder sb = new StringBuilder();
    int action = ev.getAction();
    int actionCode = action & MotionEvent.ACTION_MASK;
    sb.append( "Event action_").append( name[actionCode] );
    if ( actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP ) {
      sb.append( "(pid " ).append( action>>MotionEvent.ACTION_POINTER_ID_SHIFT ).append( ")" );
    }
    sb.append( " [" );
    for (int i=0; i<ev.getPointerCount(); ++i ) {
      sb.append( "#" ).append( i );
      sb.append( "(pid " ).append( ev.getPointerId(i) ).append( ")=" ).append( (int)(ev.getX(i)) ).append( "." ).append( (int)(ev.getY(i)) );
      if ( i+1 < ev.getPointerCount() ) sb.append( ":" );
    }
    sb.append( "]" );
    // TDLog.Log(TDLog.LOG_PLOT, sb.toString() );
  }
  */

  private float spacing( MotionEventWrap ev )
  {
    int np = ev.getPointerCount();
    if ( np < 2 ) return 0.0f;
    float x = ev.getX(1) - ev.getX(0);
    float y = ev.getY(1) - ev.getY(0);
    return (float)Math.sqrt(x*x + y*y);
  }

  private void saveEventPoint( MotionEventWrap ev )
  {
    int np = ev.getPointerCount();
    if ( np >= 1 ) {
      mSave0X = ev.getX(0);
      mSave0Y = ev.getY(0);
      if ( np >= 2 ) {
        mSave1X = ev.getX(1);
        mSave1Y = ev.getY(1);
      } else {
        mSave1X = mSave0X;
        mSave1Y = mSave0Y;
      } 
    }
  }

  private void shiftByEvent( MotionEventWrap ev )
  {
    float x0 = 0.0f;
    float y0 = 0.0f;
    float x1 = 0.0f;
    float y1 = 0.0f;
    int np = ev.getPointerCount();
    if ( np >= 1 ) {
      x0 = ev.getX(0);
      y0 = ev.getY(0);
      if ( np >= 2 ) {
        x1 = ev.getX(1);
        y1 = ev.getY(1);
      } else {
        x1 = x0;
        y1 = y0;
      } 
    }
    float x_shift = ( x0 - mSave0X + x1 - mSave1X ) / 2;
    float y_shift = ( y0 - mSave0Y + y1 - mSave1Y ) / 2;
    mSave0X = x0;
    mSave0Y = y0;
    mSave1X = x1;
    mSave1Y = y1;
  
    if ( Math.abs( x_shift ) < TDSetting.mMinShift && Math.abs( y_shift ) < TDSetting.mMinShift ) {
      mOffset.x += x_shift / mZoom;                // add shift to offset
      mOffset.y += y_shift / mZoom; 
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );
    }
  }

  private void moveCanvas( float x_shift, float y_shift )
  {
    if ( Math.abs( x_shift ) < TDSetting.mMinShift && Math.abs( y_shift ) < TDSetting.mMinShift ) {
      mOffset.x += x_shift / mZoom;                // add shift to offset
      mOffset.y += y_shift / mZoom; 
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );
    }
  }

  public void checkZoomBtnsCtrl()
  {
    // if ( mZoomBtnsCtrl == null ) return; // not necessary
    if ( TDSetting.mZoomCtrl == 2 && ! mZoomBtnsCtrl.isVisible() ) {
      mZoomBtnsCtrl.setVisible( true );
    }
  }

  // lp1    is the line (being drawn) to modify
  // lp2    is used to get the line to join/continue
  // return true is the line lp1 must be added to the sketch
  private boolean tryToJoin( DrawingLinePath lp1, DrawingLinePath lp2 )
  {
    if ( lp1 == null ) return false;
    if ( lp2 == null ) return true;

    if ( mContinueLine == CONT_CONTINUE ) {
      if ( mLastLinePath != null
           && mCurrentLine == mLastLinePath.mLineType 
           && mDrawingSurface.modifyLine( mLastLinePath, lp2, mZoom, mSelectSize ) ) {
        return false;
      }
      DrawingLinePath line = null;
      line = mDrawingSurface.getLineToContinue( lp2.mFirst, mCurrentLine, mZoom, mSelectSize );
      if ( line != null && mCurrentLine == line.mLineType ) { // continue line with the current line
        mDrawingSurface.addLineToLine( lp2, line );
        return false;
      }
    // } else if ( mContinueLine == CONT_CONTINUE_END ) {
    //   DrawingLinePath line = null;
    //   line = mDrawingSurface.getLineToContinue( lp2.mFirst, mCurrentLine, mZoom, mSelectSize );
    //   if ( line != null && mCurrentLine == line.mLineType ) { // continue line with the current line
    //     lp2.reversePath();
    //     mDrawingSurface.addLineToLine( lp2, line );
    //     return false;
    //   }
    } else {
      DrawingLinePath line1 = null;
      DrawingLinePath line2 = null;
      if ( mContinueLine == CONT_START || mContinueLine == CONT_BOTH ) {
        line1 = mDrawingSurface.getLineToContinue( lp2.mFirst, mCurrentLine, mZoom, mSelectSize );
      }
      if ( mContinueLine == CONT_END || mContinueLine == CONT_BOTH ) {
        line2 = mDrawingSurface.getLineToContinue( lp2.mLast, mCurrentLine, mZoom, mSelectSize );
      }
      if ( line1 != null ) {
        float d1 = line1.mFirst.distance( lp1.mFirst );
        float d2 = line1.mLast.distance( lp1.mFirst );
        if ( d1 < d2 ) {
          // line.reversePath();
          lp1.moveFirstTo( line1.mFirst.x, line1.mFirst.y );
        } else {
          lp1.moveFirstTo( line1.mLast.x, line1.mLast.y );
        }
      }
      if ( line2 != null ) {
        float d1 = line2.mFirst.distance( lp1.mLast );
        float d2 = line2.mLast.distance( lp1.mLast );
        if ( d1 < d2 ) {
          // line.reversePath();
          lp1.moveLastTo( line2.mFirst.x, line2.mFirst.y );
        } else {
          lp1.moveLastTo( line2.mLast.x, line2.mLast.y );
        }
      }
    }
    return true;
  }

  // -------------------------------------------------------------------------
  private void startErasing( float xs, float ys, float xc, float yc )
  {
    // Log.v("DistoXC", "startErasing " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    assert( mLastLinePath == null );
    // Log.v("DistoX", "Erase at " + xs + " " + ys );
    if ( mTouchMode == MODE_MOVE ) {
      mEraseCommand =  new EraseCommand();
      mDrawingSurface.setEraser( xc, yc, mEraseSize );
      doEraseAt( xs, ys );
    }
  }

  private void finishErasing()
  {
    mDrawingSurface.endEraser();
    if ( mEraseCommand != null && mEraseCommand.size() > 0 ) {
      mEraseCommand.completeCommand();
      mDrawingSurface.addEraseCommand( mEraseCommand );
      mEraseCommand = null;
    }
  }

  private boolean pointerDown = false;
  private boolean threePointers = false;

  public boolean onTouch( View view, MotionEvent rawEvent )
  {
    dismissPopups();
    checkZoomBtnsCtrl();

    MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
    // TDLog.Log( TDLog.LOG_INPUT, "Drawing Activity onTouch() " );
    // dumpEvent( event );

    int act = event.getAction();
    int action = act & MotionEvent.ACTION_MASK;
    int id = 0;

    if (action == MotionEvent.ACTION_POINTER_DOWN) {
      threePointers = (event.getPointerCount() >= 3);
      if ( mTouchMode == MODE_MOVE ) {
        if ( mMode == MODE_ERASE ) {
	  finishErasing();
	}
      }
      mTouchMode = MODE_ZOOM;
      oldDist = spacing( event );
      saveEventPoint( event );
      pointerDown = true;
      return true;
    } else if ( action == MotionEvent.ACTION_POINTER_UP) {
      int np = event.getPointerCount();
      threePointers = (np > 3);
      if ( np > 2 ) return true;
      mTouchMode = MODE_MOVE;
      id = 1 - ((act & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT);
      // int idx = rawEvent.findPointerIndex( id );
      if ( mSymbol != Symbol.POINT ) {
        action = MotionEvent.ACTION_DOWN; // force next case
      }
      /* fall through */
    }
    float x_canvas = event.getX(id);
    float y_canvas = event.getY(id);
    float x_scene = x_canvas/mZoom - mOffset.x;
    float y_scene = y_canvas/mZoom - mOffset.y;

    if (action == MotionEvent.ACTION_DOWN) { // ---------------------------------------- DOWN
      return onTouchDown( x_canvas, y_canvas, x_scene, y_scene );

    } else if ( action == MotionEvent.ACTION_MOVE ) { // ------------------------------- MOVE
      return onTouchMove( x_canvas, y_canvas, x_scene, y_scene, event );

    } else if (action == MotionEvent.ACTION_UP) { // ----------------------------------- UP
      return onTouchUp( x_canvas, y_canvas, x_scene, y_scene );
    }
    return true;
  }

  private boolean onTouchUp( float xc, float yc, float xs, float ys )
  {
    if ( onMenu ) {
      closeMenu();
      return true;
    }

    // Log.v("DistoX", "on touch up. mode " + mMode + " " + mTouchMode );
    if ( mTouchMode == MODE_ZOOM || mTouchMode == MODE_ROTATE ) {
      mTouchMode = MODE_MOVE;
    } else {
      float x_shift = xc - mSaveX; // compute shift
      float y_shift = yc - mSaveY;
      if ( mMode == MODE_DRAW ) {
        if ( mSymbol == Symbol.LINE || mSymbol == Symbol.AREA ) {

          mCurrentBrush.mouseUp( mDrawingSurface.getPreviewPath(), xc, yc );
          mDrawingSurface.resetPreviewPath();

          if ( mSymbol == Symbol.LINE ) {
            if (    ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2 || ( mPointCnt % mLinePointStep ) > 0 ) {
              if ( mCurrentLinePath != null ) mCurrentLinePath.addPoint( xs, ys );
            }
            if ( mLandscape ) mCurrentLinePath.landscapeToPortrait();
          } else if ( mSymbol == Symbol.AREA ) {
            // Log.v("DistoX",
            //       "DX " + (xs - mCurrentAreaPath.mFirst.x) + " DY " + (ys - mCurrentAreaPath.mFirst.y ) );
            if (    PlotInfo.isVertical( mType )
                 && BrushManager.mAreaLib.isCloseHorizontal( mCurrentArea ) 
                 && Math.abs( ys - mCurrentAreaPath.mFirst.y ) < 10  // 10 == 0.5 meter
              ) {
              DrawingAreaPath area = new DrawingAreaPath( mCurrentAreaPath.mAreaType,
                                                          mCurrentAreaPath.mAreaCnt, 
                                                          mCurrentAreaPath.mPrefix, 
                                                          TDSetting.mAreaBorder );
              if ( xs - mCurrentAreaPath.mFirst.x > 20 ) { // 20 == 1.0 meter // CLOSE BOTTOM SURFACE
                LinePoint lp = mCurrentAreaPath.mFirst; 
                float yy = lp.y;
                mCurrentAreaPath.addPoint( xs, yy-0.001f );
                area.addStartPoint( lp.x, lp.y );
                for ( lp = lp.mNext; lp != null; lp = lp.mNext ) {
                  if ( lp.y <= yy ) {
                    area.addPoint( lp.x, yy );
                    break;
                  } else {
                    area.addPoint( lp.x, lp.y );
                  }
                }
                mCurrentAreaPath = area; // area is empty if not recreated
              } else if ( mCurrentAreaPath.mFirst.x - xs > 20 ) { // 20 == 1.0 meter // CLOSE TOP SURFACE
                LinePoint lp = mCurrentAreaPath.mFirst; 
                float yy = lp.y;
                mCurrentAreaPath.addPoint( xs, yy-0.001f );
                area.addStartPoint( lp.x, lp.y );
                for ( lp = lp.mNext; lp != null; lp = lp.mNext ) {
                  if ( lp.y >= yy ) {
                    area.addPoint( lp.x, yy );
                    break;
                  } else {
                    area.addPoint( lp.x, lp.y );
                  }
                }
                mCurrentAreaPath = area; // area is empty if not recreated
              }
            } else {  
              if (    ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2
                   || ( mPointCnt % mLinePointStep ) > 0 ) {
                mCurrentAreaPath.addPoint( xs, ys );
              }
            }
    	    if ( mLandscape ) mCurrentAreaPath.landscapeToPortrait();
          }
          
          if ( mPointCnt > mLinePointStep || mLinePointStep == POINT_MAX ) {
            if ( ! ( mSymbol == Symbol.LINE && mCurrentLine == BrushManager.mLineLib.mLineSectionIndex ) 
                 && TDSetting.isLineStyleComplex()
                 && ( mSymbol == Symbol.AREA || ! BrushManager.mLineLib.isStyleStraight( mCurrentLine ) )
               ) {
              int nPts = (mSymbol == Symbol.LINE )? mCurrentLinePath.size() 
                                                  : mCurrentAreaPath.size() ;
              if ( nPts > 1 ) {
		if ( TDSetting.isLineStyleBezier() ) {
                  ArrayList< Point2D > pts = new ArrayList<>(); // [ nPts ];
                  LinePoint lp = (mSymbol == Symbol.LINE )? mCurrentLinePath.mFirst : mCurrentAreaPath.mFirst;
                  for ( ; lp != null; lp = lp.mNext ) {
                    pts.add( new Point2D( lp.x, lp.y ) );
                  }

                  mBezierInterpolator.fitCurve( pts, nPts, TDSetting.mLineAccuracy, TDSetting.mLineCorner );
                  ArrayList< BezierCurve > curves = mBezierInterpolator.getCurves();
                  int k0 = curves.size();
                  // TDLog.Log( TDLog.LOG_PLOT, " Bezier size " + k0 );
                  if ( k0 > 0 ) {
                    BezierCurve c = curves.get(0);
                    Point2D p0 = c.getPoint(0);
                    if ( mSymbol == Symbol.LINE ) {
                      DrawingLinePath lp1 = new DrawingLinePath( mCurrentLine );
                      lp1.addStartPoint( p0.x, p0.y );
                      for (int k=0; k<k0; ++k) {
                        c = curves.get(k);
                        Point2D p1 = c.getPoint(1);
                        Point2D p2 = c.getPoint(2);
                        Point2D p3 = c.getPoint(3);
                        lp1.addPoint3(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y );
                      }
                      boolean addline = true;
                      if ( mContinueLine > CONT_NONE && mCurrentLine != BrushManager.mLineLib.mLineSectionIndex ) {
                        addline = tryToJoin( lp1, mCurrentLinePath );
                      }
                      if ( addline ) {
                        lp1.computeUnitNormal();
                        if ( mSymbol == Symbol.LINE && BrushManager.mLineLib.isClosed( mCurrentLine ) ) {
                          // mCurrentLine == lp1.mLineType 
                          lp1.setClosed( true );
                          lp1.closePath();
                        }
                        mDrawingSurface.addDrawingPath( lp1 );
                        mLastLinePath = lp1;
                      // } else {
                      //   mLastLinePath = ???
                      }
                    } else { //  mSymbol == Symbol.AREA
                      DrawingAreaPath ap = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(), mName+"-a", TDSetting.mAreaBorder ); 
                      ap.addStartPoint( p0.x, p0.y );
                      for (int k=0; k<k0; ++k) {
                        c = curves.get(k);
                        Point2D p1 = c.getPoint(1);
                        Point2D p2 = c.getPoint(2);
                        Point2D p3 = c.getPoint(3);
                        ap.addPoint3(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y );
                      }
                      ap.closePath();
                      ap.shiftShaderBy( mOffset.x, mOffset.y, mZoom );
                      mDrawingSurface.addDrawingPath( ap );
                      mLastLinePath = null;
                    }
                  }
                } else { // if ( TDSetting.isLineStyleSimplified() ) 
		  Weeder weeder = new Weeder();
                  LinePoint lp = (mSymbol == Symbol.LINE )? mCurrentLinePath.mFirst : mCurrentAreaPath.mFirst;
                  for ( ; lp != null; lp = lp.mNext ) {
                    weeder.addPoint( lp.x, lp.y );
                  }
		  // get pixels from meters
		  // float dist = mZoom*DrawingUtil.SCALE_FIX*TDSetting.mWeedDistance;
		  float dist = DrawingUtil.SCALE_FIX*TDSetting.mWeedDistance; // N.B. no zoom
		  float len  = mZoom*DrawingUtil.SCALE_FIX*TDSetting.mWeedLength;
		  // Log.v("DistoXX", "Weed dist " + dist + " len " + len );

                  ArrayList< Point2D > points = weeder.simplify( dist, len );

                  int k0 = points.size();
                  // TDLog.Log( TDLog.LOG_PLOT, " Bezier size " + k0 );
                  if ( k0 > 1 ) {
                    Point2D p0 = points.get(0);
                    if ( mSymbol == Symbol.LINE ) {
                      DrawingLinePath lp1 = new DrawingLinePath( mCurrentLine );
                      lp1.addStartPoint( p0.x, p0.y );
                      for (int k=1; k<k0; ++k) {
                        p0 = points.get(k);
                        lp1.addPoint(p0.x, p0.y );
                      }
                      boolean addline = true;
                      if ( mContinueLine > CONT_NONE && mCurrentLine != BrushManager.mLineLib.mLineSectionIndex ) {
                        addline = tryToJoin( lp1, mCurrentLinePath );
                      }
                      if ( addline ) {
                        lp1.computeUnitNormal();
                        if ( mSymbol == Symbol.LINE && BrushManager.mLineLib.isClosed( mCurrentLine ) ) {
                          // mCurrentLine == lp1.mLineType 
                          lp1.setClosed( true );
                          lp1.closePath();
                        }
                        mDrawingSurface.addDrawingPath( lp1 );
                        mLastLinePath = lp1;
                      // } else {
                      //   mLastLinePath = ???
                      }
                    } else { //  mSymbol == Symbol.AREA
                      DrawingAreaPath ap = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(), mName+"-a", TDSetting.mAreaBorder ); 
                      ap.addStartPoint( p0.x, p0.y );
                      for (int k=1; k<k0; ++k) {
                        p0 = points.get(k);
                        ap.addPoint(p0.x, p0.y );
                      }
                      ap.closePath();
                      ap.shiftShaderBy( mOffset.x, mOffset.y, mZoom );
                      mDrawingSurface.addDrawingPath( ap );
                      mLastLinePath = null;
                    }
                  }
                }
              }
            }
            else
            {
              if ( mSymbol == Symbol.LINE && mCurrentLinePath != null ) {
                // N.B.
                // section direction is in the direction of the tick
                // and splay reference are taken from the station the section looks towards
                // section line points: right-end -- left-end -- tick-end
                //
                if ( mCurrentLinePath.mLineType == BrushManager.mLineLib.mLineSectionIndex ) {
                  mLastLinePath = null;
                  doSectionLine( mCurrentLinePath );
                } else { // not section line
                  boolean addline= true;
                  if ( mContinueLine > CONT_NONE && mCurrentLine != BrushManager.mLineLib.mLineSectionIndex ) {
                    addline = tryToJoin( mCurrentLinePath, mCurrentLinePath );
                  }
                  if ( addline ) {
                    mCurrentLinePath.computeUnitNormal();
                    if ( mSymbol == Symbol.LINE && BrushManager.mLineLib.isClosed( mCurrentLine ) ) {
                      // mCurrentLine == mCurrentLinePath.mLineType
                      mCurrentLinePath.setClosed( true );
                      mCurrentLinePath.closePath();
                    }
                    mDrawingSurface.addDrawingPath( mCurrentLinePath );
                    mLastLinePath = mCurrentLinePath;
                  // } else {
                  //   mLastLinePath = ???
                  }
                }
                mCurrentLinePath = null;
              } else if ( mSymbol == Symbol.AREA && mCurrentAreaPath != null ) {
                mCurrentAreaPath.closePath();
                mCurrentAreaPath.shiftShaderBy( mOffset.x, mOffset.y, mZoom );
                mDrawingSurface.addDrawingPath( mCurrentAreaPath );
                mCurrentAreaPath = null;
                mLastLinePath = null;
              }
            }
            // undoBtn.setEnabled(true);
            // redoBtn.setEnabled(false);
            // canRedo = false;
          }
        }
        else
        { // Symbol.POINT
          mLastLinePath = null;
          if ( ! pointerDown ) {
	    float radius = TDSetting.mPointingRadius;
    	    if ( BrushManager.isPointOrientable( mCurrentPoint ) ) radius *= 4;
	    float shift = Math.abs( x_shift ) + Math.abs( y_shift );
	    if ( shift < radius ) {
              xs = mSaveX/mZoom - mOffset.x;
              ys = mSaveY/mZoom - mOffset.y;
              // Log.v("DistoXO", "insert point type " + mCurrentPoint + " x " + x_shift + " y " + y_shift + " R " + radius );
              if ( BrushManager.isPointLabel( mCurrentPoint ) ) {
                new DrawingLabelDialog( mActivity, this, xs, ys ).show();
              } else if ( BrushManager.isPointPhoto( mCurrentPoint ) ) {
                new DrawingPhotoDialog( mActivity, this, xs, ys ).show();
              } else if ( BrushManager.isPointAudio( mCurrentPoint ) ) {
	        if ( audioCheck ) {
                  addAudioPoint( xs, ys );
	        } else {
                  TDToast.makeWarn( R.string.no_feature_audio );
                }
              } else {
    	        if ( mLandscape ) {
                  DrawingPointPath point = new DrawingPointPath( mCurrentPoint, -ys, xs, mPointScale, null, null );
    	          if ( BrushManager.isPointOrientable( mCurrentPoint ) ) {
		    if ( shift > TDSetting.mPointingRadius ) {
		      float angle = TDMath.atan2d( x_shift, -y_shift );
                      point.setOrientation( angle );
		      // Log.v("DistoXO", "L orientation " + angle + " shift " + shift + " radius " + radius );
		    }
                    if ( ! BrushManager.isPointLabel( mCurrentPoint ) ) point.rotateBy( 90 );
    	          }
                  mDrawingSurface.addDrawingPath( point );
    	        } else {
                  DrawingPointPath point = new DrawingPointPath( mCurrentPoint, xs, ys, mPointScale, null, null ); // no text, no options
    	          if ( BrushManager.isPointOrientable( mCurrentPoint ) ) {
		    if ( shift > TDSetting.mPointingRadius ) {
		      float angle = TDMath.atan2d( x_shift, -y_shift );
                      point.setOrientation( angle );
		      // Log.v("DistoXO", "P orientation " + angle + " shift " + shift + " radius " + radius );
		    }
                  }
                  mDrawingSurface.addDrawingPath( point );
    	        }
                // undoBtn.setEnabled(true);
                // redoBtn.setEnabled(false);
                // canRedo = false;
              }
	    }
          }
        }
        pointerDown = false;
        modified();
      } else if ( mMode == MODE_EDIT ) {
        if ( Math.abs(mStartX - xc) < TDSetting.mPointingRadius 
          && Math.abs(mStartY - yc) < TDSetting.mPointingRadius ) {
          doSelectAt( xs, ys, mSelectSize );
        }
        mEditMove = false;
      } else if ( mMode == MODE_SHIFT ) {
        if ( TDLevel.overExpert && mType == PlotInfo.PLOT_EXTENDED ) {
          SelectionPoint hot = mDrawingSurface.hotItem();
          if ( hot != null ) {
            DrawingPath path = hot.mItem;
    	    if ( path.mType == DrawingPath.DRAWING_PATH_FIXED ) { // FIXME_EXTEND
    	      DBlock blk = path.mBlock;
    	      float msz = TopoDroidApp.mDisplayWidth/(mZoom*DrawingUtil.SCALE_FIX); // TDSetting.mMinShift / 2;
    	      if ( mLandscape ) {
    	        float y = (path.y1 + path.y2)/2; // midpoin (scene)
    	        if ( Math.abs( y - xs ) < msz ) {
    	          float x = (path.x1 + path.x2)/2; // midpoin (scene)
    	          // Log.v("DistoX", "blk scene " + x + " " + y + " tap " + xs + " " + ys);
    	          if ( Math.abs( x + ys ) < 2.5f*msz ) {
    	            int extend = (-ys + msz < x)? -1 : (-ys - msz > x)? 1 : 0;
                    updateBlockExtend( blk, extend, DBlock.STRETCH_NONE ); // FIXME_STRETCH equal extend checked by the method
    	          }
    	        }
    	      } else {
    	        float y = (path.y1 + path.y2)/2; // midpoin (scene)
    	        if ( Math.abs( y - ys ) < msz ) {
    	          float x = (path.x1 + path.x2)/2; // midpoin (scene)
		  float dx = x - xs;
    	          // Log.v("DistoX", "blk scene dx " + dx + " msz " + msz + " zoom " + mZoom );
    	          if ( Math.abs( x - xs ) < 2.5f*msz ) {
    	            int extend = (xs + msz < x)? -1 : (xs - msz > x)? 1 : 0;
                    updateBlockExtend( blk, extend, DBlock.STRETCH_NONE ); // FIXME_STRETCH equal extend checked by the method
    	          }
    	        }
    	      }
    	    }
          }
        }
        if ( mShiftMove ) {
          if ( Math.abs(mStartX - xc) < TDSetting.mPointingRadius
            && Math.abs(mStartY - yc) < TDSetting.mPointingRadius ) {
            // mEditMove = false;
	    // PATH_MULTISELECTION
	    if ( mDrawingSurface.isMultiselection() ) {
	      // TODO
	    } else {
              clearSelected();
	    }
          }
        }
        mShiftMove = false;
      } else if ( mMode == MODE_ERASE ) {
	finishErasing();
      } else if ( mMode == MODE_SPLIT ) {
        mDrawingSurface.resetPreviewPath();
        mSplitBorder.add( new PointF( xs, ys ) );
        // Log.v("DistoX-S", "*** split border size " + mSplitBorder.size() );
        doSplitPlot( );
        setMode( MODE_MOVE );
      } else { // MODE_MOVE 
/* F for the moment do not create X-Sections
        if ( Math.abs(xc - mDownX) < 10 && Math.abs(yc - mDownY) < 10 ) {
          // check if there is a station: only PLAN and EXTENDED or PROFILE
          if ( PlotInfo.isSketch2D( mType ) ) {
            DrawingStationName sn = mDrawingSurface.getStationAt( xs, ys, mSelectSize );
            if ( sn != null ) {
              boolean barrier = mNum.isBarrier( sn.mName );
              boolean hidden  = mNum.isHidden( sn.mName );
              List< DBlock > legs = mApp_mData.selectShotsAt( TDInstance.sid, sn.name(), true ); // select "independent" legs
              // new DrawingStationDialog( mActivity, this, mApp, sn, barrier, hidden, // TDInstance.xsections, // legs ).show();
              openXSection( sn, sn.mName, mType );
            }
          }
        }
*/
      }
    }
    return true;
  }

  private boolean onTouchDown( float xc, float yc, float xs, float ys )
  {
    mDrawingSurface.endEraser();
    float d0 = TDSetting.mCloseCutoff + mSelectSize / mZoom;
    // Log.v("DistoX", "on touch down. mode " + mMode + " " + mTouchMode );

    // TDLog.Log( TDLog.LOG_PLOT, "DOWN at X " + xc + " [" +mBorderInnerLeft + " " + mBorderInnerRight + "] Y " 
    //                                          + yc + " / " + mBorderBottom );
    if ( yc > mBorderBottom ) {
      if ( mZoomBtnsCtrlOn && xc > mBorderInnerLeft && xc < mBorderInnerRight ) {
        mTouchMode = MODE_ZOOM;
        mZoomBtnsCtrl.setVisible( true );
        // mZoomCtrl.show( );
      } else if ( TDSetting.mSideDrag ) {
        mTouchMode = MODE_ZOOM;
      }
    } else if ( TDSetting.mSideDrag && ( xc > mBorderRight || xc < mBorderLeft ) ) {
      mTouchMode = MODE_ZOOM;
      SelectionPoint sp = mDrawingSurface.hotItem();
      if ( sp != null && sp.type() == DrawingPath.DRAWING_PATH_POINT ) {
        DrawingPointPath path = (DrawingPointPath)(sp.mItem);
        if ( BrushManager.isPointOrientable(path.mPointType) ) {
          mTouchMode = MODE_ROTATE;
          mStartY = yc;
        }
      }
    }

    if ( mMode == MODE_DRAW ) {
      // TDLog.Log( TDLog.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
      mPointCnt = 0;
      if ( mSymbol == Symbol.LINE ) {
        mCurrentLinePath = new DrawingLinePath( mCurrentLine );
        mCurrentLinePath.addStartPoint( xs, ys );
        mCurrentBrush.mouseDown( mDrawingSurface.getPreviewPath(), xc, yc );
      } else if ( mSymbol == Symbol.AREA ) {
        // TDLog.Log( TDLog.LOG_PLOT, "onTouch ACTION_DOWN area type " + mCurrentArea );
        mCurrentAreaPath = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(),
          mName+"-a", TDSetting.mAreaBorder );
        mCurrentAreaPath.addStartPoint( xs, ys );
        // Log.v("DistoX", "start area start " + xs + " " + ys );
        mCurrentBrush.mouseDown( mDrawingSurface.getPreviewPath(), xc, yc );
      // } else { // Symbol.POINT
        // mSaveX = xc; // FIXME-000
        // mSaveY = yc;
      }
      mSaveX = xc; // FIXME-000
      mSaveY = yc;

    } else if ( mMode == MODE_MOVE ) {
      setTheTitle( );
      mSaveX = xc; // FIXME-000
      mSaveY = yc;
      // mDownX = xc;
      // mDownY = yc;
      return false;

    } else if ( mMode == MODE_ERASE ) {
      startErasing( xs, ys, xc, yc );
    } else if ( mMode == MODE_EDIT ) {
      mStartX = xc;
      mStartY = yc;
      mEditMove = true;
      SelectionPoint pt = mDrawingSurface.hotItem();
      if ( pt != null ) {
        if ( mLandscape ) {
          mEditMove = ( pt.distance( -ys, xs ) < d0 );
        } else {
          mEditMove = ( pt.distance( xs, ys ) < d0 );
        }
      } 
      // doSelectAt( xs, ys, mSelectSize );
      mSaveX = xc;
      mSaveY = yc;
      // return false;

    } else if ( mMode == MODE_SHIFT ) {
      mShiftMove = true; // whether to move canvas in point-shift mode
                         // false if moving the hot point
      mStartX = xc;
      mStartY = yc;
      // PATH_MULTISELECTION
      if ( mDrawingSurface.isMultiselection() ) {
        // Log.v("DistoX", "on touch down add item at " + xs + " " + ys );
        mDrawingSurface.addItemAt( xs, ys, mZoom, mSelectSize );
      } else {
        SelectionPoint pt = mDrawingSurface.hotItem();
        if ( pt != null ) {
          if ( mLandscape ) {
            if ( pt.distance( -ys, xs ) < d0*4 ) {
              mShiftMove = false;
              mStartX = xs;  // save start position
              mStartY = ys;
            }
          } else {
            if ( pt.distance( xs, ys ) < d0*4 ) {
              mShiftMove = false;
              mStartX = xs;  // save start position
              mStartY = ys;
            }
          }
        }
      }
      mSaveX = xc; // FIXME-000
      mSaveY = yc;
      // return false;

    } else if ( mMode == MODE_SPLIT ) {
      mCurrentBrush.mouseDown( mDrawingSurface.getPreviewPath(), xc, yc );
      mSplitBorder.add( new PointF( xs, ys ) );
      // Log.v("DistoX-S", "*** split start border size " + mSplitBorder.size() );
      mSaveX = xc; 
      mSaveY = yc;
    }
    return true;
  }

  private boolean onTouchMove( float xc, float yc, float xs, float ys, MotionEventWrap event )
  {
    // Log.v(  TopoDroidApp.TAG, "action MOVE mode " + mMode + " touch-mode " + mTouchMode);
    if ( mTouchMode == MODE_MOVE) {
      float x_shift = xc - mSaveX; // compute shift
      float y_shift = yc - mSaveY;
      boolean save = true; // FIXME-000
      // mSaveX = xc; 
      // mSaveY = yc;
      if ( mMode == MODE_DRAW ) {
        if ( mSymbol == Symbol.LINE ) {
          if ( ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2 ) {
            if ( ++mPointCnt % mLinePointStep == 0 ) {
              if ( mCurrentLinePath != null ) mCurrentLinePath.addPoint( xs, ys );
            }
            mCurrentBrush.mouseMove( mDrawingSurface.getPreviewPath(), xc, yc );
          } else {
            save = false;
          }
        } else if ( mSymbol == Symbol.AREA ) {
          if ( ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2 ) {
            if ( ++mPointCnt % mLinePointStep == 0 ) {
              mCurrentAreaPath.addPoint( xs, ys );
              // Log.v("DistoX", "start area add " + xs + " " + ys );
            }
            mCurrentBrush.mouseMove( mDrawingSurface.getPreviewPath(), xc, yc );
          } else {
            save = false;
          }
        } else if ( mSymbol == Symbol.POINT ) {
          // if ( ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2 ) {
          //   pointerDown = 0;
          // }
	  save = false;
        }
      } else if (  mMode == MODE_MOVE 
               || (mMode == MODE_EDIT && mEditMove ) 
               || (mMode == MODE_SHIFT && mShiftMove) ) {
        moveCanvas( x_shift, y_shift );
      } else if ( mMode == MODE_SHIFT ) {
        // if NOT PATH_MULTISELECTION
        if ( ! mDrawingSurface.isMultiselection() ) {
          // mDrawingSurface.shiftHotItem( xs - mStartX, ys - mStartY, mEditRadius * 10 / mZoom );
          if ( mLandscape ) {
            mDrawingSurface.shiftHotItem( -ys + mStartY, xs - mStartX );
          } else {
            mDrawingSurface.shiftHotItem( xs - mStartX, ys - mStartY );
          }
          mStartX = xs;
          mStartY = ys;
          modified();
	}
      } else if ( mMode == MODE_ERASE ) {
        if ( mEraseCommand != null ) {
          mDrawingSurface.setEraser( xc, yc, mEraseSize );
          doEraseAt( xs, ys );
	}
      } else if ( mMode == MODE_SPLIT ) {
        if ( ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2 ) {
          mCurrentBrush.mouseMove( mDrawingSurface.getPreviewPath(), xc, yc );
          mSplitBorder.add( new PointF( xs, ys ) );
          // Log.v("DistoX-S", "*** split ... border size " + mSplitBorder.size() );
        } else {
          save = false;
        }
      }
      if ( save ) { // FIXME-000
        mSaveX = xc; 
        mSaveY = yc;
      }
    } else if ( mTouchMode == MODE_ROTATE ) {
      mDrawingSurface.rotateHotItem( 180 * ( yc - mStartY ) / TopoDroidApp.mDisplayHeight );
      mStartX = xc; // xs;
      mStartY = yc; // ys;
      modified();
    } else { // mTouchMode == MODE_ZOOM
      float newDist = spacing( event );
      float factor = ( newDist > 16.0f && oldDist > 16.0f )? newDist/oldDist : 0 ;

      if ( mMode == MODE_MOVE && mShiftDrawing ) {
        float x_shift = xc - mSaveX; // compute shift
        float y_shift = yc - mSaveY;
        if ( TDLevel.overNormal ) {
          if ( Math.abs( x_shift ) < TDSetting.mMinShift && Math.abs( y_shift ) < TDSetting.mMinShift ) {
    	    if ( mLandscape ) {
              mDrawingSurface.shiftDrawing( -y_shift/mZoom, x_shift/mZoom );
    	    } else {
              mDrawingSurface.shiftDrawing( x_shift/mZoom, y_shift/mZoom );
    	    }
            modified();
          }
        // } else {
        //   moveCanvas( x_shift, y_shift );
        }
        if ( factor > 0.05f && factor < 4.0f ) {
          if ( threePointers ) {
            mDrawingSurface.scaleDrawing( 1+(factor-1)*0.01f );
          } else {
            changeZoom( factor );
            oldDist = newDist;
          }
        }
        mSaveX = xc;
        mSaveY = yc;
      } else {
        if ( factor > 0.05f && factor < 4.0f ) {
          changeZoom( factor );
          oldDist = newDist;
        }
        shiftByEvent( event );
      }
    }
    return true;
  }


  private void doSectionLine( DrawingLinePath currentLine )
  {
    // Log.v("DistoXC", "doSectionLine " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    // assert( mLastLinePath == null ); // obvious
    currentLine.addOption("-direction both");
    currentLine.makeStraight( );
    boolean h_section = PlotInfo.isProfile( mType );
    
    // NOTE here l1 is the end-point and l2 the start-point (not considering the tick)
    //         |
    //         L2 --------- L1
    //      The azimuth reference is North-East same as bearing
    //         L1->L2 = atan2( (L2-L1).x, -(L2-l1).y )  Y is point downward North upward
    //         azimuth = dir(L1->L2) + 90
    //
    LinePoint l2 = currentLine.mFirst; // .mNext;
    LinePoint l1 = l2.mNext;
    // Log.v("DistoX", "section line L1 " + l1.x + " " + l1.y + " L2 " + l2.x + " " + l2.y );

    List< DrawingPathIntersection > paths = mDrawingSurface.getIntersectionShot( l1, l2 );
    int nr_legs = paths.size() ; // 0 no-leg, 1 ok, 2 too many legs
    String from = null;
    String to   = null;
    float azimuth = 0;
    float clino = 0;
    float tt = -1;
    if ( paths.size() > 0 ) {
      currentLine.computeUnitNormal();

      // orientation of the section-line
      azimuth = TDMath.in360( 90 + (float)(Math.atan2( l2.x-l1.x, -l2.y+l1.y ) * TDMath.RAD2DEG ) );

      if ( nr_legs == 1 ) {
        DrawingPathIntersection pi = paths.get(0);
        DrawingPath p = pi.path;
        tt = pi.tt;
        // Log.v("DistoX", "assign tt " + tt );
        DBlock blk = p.mBlock;

        // Float result = Float.valueOf(0);
        // p.intersect( l1.x, l1.y, l2.x, l2.y, result );
        // float intersection = result.floatValue();
        // // p.log();

        from = blk.mFrom;
        to   = blk.mTo;
        if ( h_section ) { // xsection in profile view
          int extend = 1;
          if ( azimuth < 180 ) {
            clino = 90 - azimuth;
            // extend = 1;
          } else {
            clino = azimuth - 270;
            extend = -1;
          }
    
          float dc = TDMath.in360( (extend == blk.getExtend())? clino - blk.mClino : 180 - clino - blk.mClino );
          if ( dc > 90 && dc <= 270 ) { // exchange FROM-TO 
            azimuth = blk.mBearing + 180; if ( azimuth >= 360 ) azimuth -= 360;
            from = blk.mTo;
            to   = blk.mFrom;
            tt   = 1 - tt;
          } else {
            azimuth = blk.mBearing;
          }
          // if ( extend != blk.getExtend() ) {
          //   azimuth = blk.mBearing + 180; if ( azimuth >= 360 ) azimuth -= 360;
          // }
        } else { // xsection in plan view ( clino = 0 )
          float da = TDMath.in360( azimuth - blk.mBearing );
          if ( da > 90 && da <= 270 ) { // exchange FROM-TO 
            from = blk.mTo;
            to   = blk.mFrom;
            tt   = 1 - tt;
          }
        }
      } else if ( nr_legs > 1 ) { // many legs
        // TDToast.makeWarn( R.string.too_many_leg_intersection );
        if ( h_section ) { // xsection in profile view
          // nothing 
        } else {
          nr_legs = 1; // ok
          // these have already been computed before the if-test
          // azimuth = TDMath.in360( 90 + (float)(Math.atan2( l2.x-l1.x, -l2.y+l1.y ) * TDMath.RAD2DEG ) );
        }
      }
    }
    // Log.v("DistoX", "new section " + from + " - " + to );
    // cross-section does not exists yet
    if ( nr_legs == 0 ) {
      TDToast.makeWarn( R.string.no_leg_intersection );
    } else if ( nr_legs == 1 ) {
      String section_id = mApp_mData.getNextSectionId( TDInstance.sid );
      currentLine.addOption( "-id " + section_id );
      mDrawingSurface.addDrawingPath( currentLine );

      if ( TDSetting.mAutoSectionPt && section_id != null ) {
        float x5 = currentLine.mLast.x + currentLine.mDx * 20; 
        float y5 = currentLine.mLast.y + currentLine.mDy * 20; 
        // FIXME_LANDSCAPE if ( mLandscape ) { float t=x5; x5=-y5; y5=t; }
        // FIXME String scrap_option = "-scrap " /* + TDInstance.survey + "-" */ + section_id;
        String scrap_option = "-scrap " + TDInstance.survey + "-" + section_id;
        DrawingPointPath section_pt = new DrawingPointPath( BrushManager.mPointLib.mPointSectionIndex,
                                                        x5, y5, DrawingPointPath.SCALE_M, 
                                                        null, // no text 
                                                        scrap_option );
	section_pt.setLink( currentLine );
        mDrawingSurface.addDrawingPath( section_pt );
      }

      // Log.v("DistoX", "line section dialog TT " + tt );
      new DrawingLineSectionDialog( mActivity, this, /* mApp, */ h_section, false, section_id, currentLine, from, to, azimuth, clino, tt ).show();

    } else { // many legs in profile view
      TDToast.makeWarn( R.string.too_many_leg_intersection );
    }
  }

  // -------------------------------------------------------------

    // add a therion label point (ILabelAdder)
    public void addLabel( String label, float x, float y, int level )
    {
      // Log.v("DistoXC", "addLabel " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      if ( label != null && label.length() > 0 ) {
	if ( mLandscape ) { float t=x; x=-y; y=t; }
        DrawingLabelPath label_path = new DrawingLabelPath( label, x, y, mPointScale, null );
	label_path.setOrientation( BrushManager.mPointLib.getPointOrientation( mCurrentPoint ) ); // FIX Asenov
	label_path.mLandscape = mLandscape;
        label_path.mLevel = level;
        mDrawingSurface.addDrawingPath( label_path );
        modified();
      } 
    }

  private String mMediaComment = null;
  private long  mMediaId = -1L;
  private float mMediaX, mMediaY;

  public void insertPhoto( )
  {
    // Log.v("DistoXC", "insertPhoto " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    assert( mLastLinePath == null );
    mApp_mData.insertPhoto( TDInstance.sid, mMediaId, -1, "", TDUtil.currentDate(), mMediaComment ); // FIXME TITLE has to go
    // FIXME NOTIFY ? no
    // photo file is "survey/id.jpg"
    // String filename = TDInstance.survey + "/" + Long.toString( mMediaId ) + ".jpg";
    DrawingPhotoPath photo = new DrawingPhotoPath( mMediaComment, mMediaX, mMediaY, mPointScale, null, mMediaId );
    photo.mLandscape = mLandscape;
    mDrawingSurface.addDrawingPath( photo );
    modified();
  }

  // NOTE this was used to let QCamCompass tell the DrawingWindow the photo azimuth/clino
  //      but it messes up the azimuth/clino set by the section line
  //      DO NOT USE IT
  // public void notifyAzimuthClino( long pid, float azimuth, float clino )
  // {
  //   mApp_mData.updatePlotAzimuthClino( TDInstance.sid, pid, azimuth, clino );
  // }

  private void doTakePhoto( File imagefile, boolean insert, long pid )
  {
    if ( TDandroid.checkCamera( mApp ) ) { // hasPhoto
      new QCamCompass( this,
            	         (new MyBearingAndClino( mApp, imagefile )),
                       // this, pid, // pid non-negative if notify azimuth/clino // DO NOT USE THIS
        	         ( insert ? this : null), // ImageInserter
        	         true, false).show();  // true = with_box, false=with_delay
    } else {
      boolean ok = TDandroid.checkStrictMode();
      // boolean ok = true;
      // /* fixme-23 */
      // if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.N ) { // build version 24
      //   try {
      //     Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposed");
      //     m.invoke( null );
      //   } catch ( Exception e ) { ok = false; }
      // }
      // /* */
      if ( ok ) {
        try {
          Uri outfileuri = Uri.fromFile( imagefile );
          Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
          intent.putExtra( MediaStore.EXTRA_OUTPUT, outfileuri );
          intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
          if ( insert ) {
            mActivity.startActivityForResult( intent, TDRequest.CAPTURE_IMAGE_DRAWWINDOW );
          } else {
            mActivity.startActivity( intent );
          }
        } catch ( ActivityNotFoundException e ) {
          TDToast.makeBad( R.string.no_capture_app );
        }
      } else {
        TDToast.makeBad( "NOT IMPLEMENTED YET" );
      }
    }
  }

  public void addPhotoPoint( String comment, float x, float y )
  {
    // Log.v("DistoXC", "addPhoto " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    assert( mLastLinePath == null );
    mMediaComment = (comment == null)? "" : comment;
    mMediaId = mApp_mData.nextPhotoId( TDInstance.sid );
    if ( mLandscape ) {
      mMediaX = -y;
      mMediaY = x;
    } else {
      mMediaX = x;
      mMediaY = y;
    }
    File imagefile = new File( TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mMediaId) ) );
    // TODO TD_XSECTION_PHOTO
    doTakePhoto( imagefile, true, -1L ); // with inserter, no pid
  }

    private void addAudioPoint( float x, float y )
    {
      // Log.v("DistoXC", "addAudio " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      mMediaComment = "";
      if ( ! audioCheck ) {
	// TODO TDToast.makeWarn( R.string.no_feature_audio );
	return;
      }
      mMediaId = mApp_mData.nextAudioNegId( TDInstance.sid );
      if ( mLandscape ) {
        mMediaX = -y;
        mMediaY = x;
      } else {
        mMediaX = x;
        mMediaY = y;
      }
      File file = new File( TDPath.getSurveyAudioFile( TDInstance.survey, Long.toString(mMediaId) ) );
      // TODO RECORD AUDIO
      new AudioDialog( mActivity, this, mMediaId ).show();
    }

    public void deletedAudio( long bid )
    {
      // Log.v("DistoXC", "deleteAudio " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      DrawingAudioPath audio = mDrawingSurface.getAudioPoint( bid );
      deletePoint( audio ); // if audio == null doesn't do anything
    }

    // public void startRecordAudio( long bid )
    // {
    //   // nothing
    // }

    public void stopRecordAudio( long bid )
    {
      DrawingAudioPath audio = mDrawingSurface.getAudioPoint( bid );
      if ( audio == null ) {
        // assert bid == mMediaId
        audio = new DrawingAudioPath( mMediaX, mMediaY, mPointScale, null, bid );
	audio.mLandscape = mLandscape;
        mDrawingSurface.addDrawingPath( audio );
        modified();
      }
    }

    void setCurrentStationName( String name ) { mApp.setCurrentStationName( name ); }

    // delete at-station xsection
    void deleteXSection( DrawingStationName st, String name, long type ) 
    {
      // Log.v("DistoXC", "deleteXSection " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      long xtype = -1;
      String xs_id = null; // xsection_id eg, xs-2 (xsection at station 2)
      if ( type == PlotInfo.PLOT_PLAN ) {
        xs_id = "xs-" + name;
        xtype = PlotInfo.PLOT_X_SECTION;
      } else if ( PlotInfo.isProfile( type ) ) {
        xs_id = "xh-" + name;
        xtype = PlotInfo.PLOT_XH_SECTION;
      } else {
	TDLog.Error("No at-station section to delete. Plot type " + type + " Name " + name + " SID "  + TDInstance.sid );
        return;
      }

      st.resetXSection();
      mApp_mData.deletePlotByName( xs_id, TDInstance.sid );
      // drop the files
      TDUtil.deleteFile( TDPath.getSurveyPlotTdrFile( TDInstance.survey, xs_id ) );
      TDUtil.deleteFile( TDPath.getSurveyPlotTh2File( TDInstance.survey, xs_id ) );
      // TODO delete backup files

      deleteSectionPoint( xs_id ); 
    }

    // delete section point and possibly the xsection outline
    private void deleteSectionPoint( String xs_id )
    {
      // Log.v("DistoXC", "deleteSectionPoint " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      if ( xs_id == null ) return;
      String scrap_name = TDInstance.survey + "-" + xs_id;
      mDrawingSurface.deleteSectionPoint( scrap_name );   // this section-point delete cannot be undone
      mDrawingSurface.clearXSectionOutline( scrap_name ); // clear outline if any
    }

    private long getXSectionType( long type )
    {
      if ( type == PlotInfo.PLOT_PLAN ) return PlotInfo.PLOT_X_SECTION;
      if ( PlotInfo.isProfile( type ) ) return PlotInfo.PLOT_XH_SECTION;
      return PlotInfo.PLOT_NULL;
    }

    private String getXSectionName( String st_name, long type )
    {
      if ( type == PlotInfo.PLOT_PLAN ) return "xs-" + st_name;
      if ( PlotInfo.isProfile( type ) ) return "xh-" + st_name;
      return null;
    }

    // st_name = station name
    // type = parent type
    String getXSectionNick( String st_name, long type )
    {
      // parent name = mName
      String xs_id = getXSectionName( st_name, type );
      if ( xs_id == null ) return "";
      if ( ! TDInstance.xsections ) xs_id = xs_id + "-" + mName;

      // Log.v("DistoXX", "xsection nick for <" + xs_id + ">" );

      PlotInfo plot = mApp_mData.getPlotInfo( TDInstance.sid, xs_id );
      if ( plot != null ) return plot.nick;
      return null;
    }

    // X-SECTION at station B where A--B--C
    // @param st_name station name
    // @param type type of the plot where the x-section is defined
    // @param azimuth clino  section plane direction
    //        direct: azimuth = average azimuth of AB and BC
    //                clino   = average clino of AB and BC 
    //        inverse opposite
    //
    // if plot type = PLAN
    //    clino = 0
    //
    // if plot type = PROFILE
    //    clino = -90, 0, +90  according to horiz
    //
    void openXSection( DrawingStationName st, String st_name, long type,
                       float azimuth, float clino, boolean horiz, String nick )
    {
      // Log.v("DistoXC", "openXSection " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      // Log.v("DistoXX", "XSection nick <" + nick + "> st_name <" + st_name + "> plot " + mName );
      // parent plot name = mName
      String xs_id = getXSectionName( st_name, type );
      if ( xs_id == null ) return;
      if ( ! TDInstance.xsections ) xs_id = xs_id + "-" + mName;
      long xtype = getXSectionType( type );

      // Log.v("DistoXX", "open xsection <" + xs_id + "> nick <" + nick + ">" );

      PlotInfo plot = mApp_mData.getPlotInfo( TDInstance.sid, xs_id );

      if ( plot == null  ) { // if there does not exist xsection xs-name create it
        // TDToast.makeWarn( R.string.too_many_legs_xsection );
        if ( azimuth >= 360 ) azimuth -= 360;

        if ( PlotInfo.isProfile( type ) ) {
          if ( horiz ) {
            clino = ( clino > 0 ) ? 90 : -90;
          } else {
            clino = 0;
          }
          // clino = ( clino >  TDSetting.mVertSplay )?  90 : ( clino < -TDSetting.mVertSplay )? -90 : 0;
        } else { // type == PlotInfo.PLOT_PLAN
          clino = 0;
        }
        // Log.v("DistoXX", "new at-station X-section " + xs_id + " st_name " + st_name + " nick <" + nick + ">" );

        mApp.insert2dSection( TDInstance.sid, xs_id, xtype, st_name, "", azimuth, clino, (TDInstance.xsections? null : mName), nick );
        plot = mApp_mData.getPlotInfo( TDInstance.sid, xs_id );

        // add x-section to station-name

        st.setXSection( azimuth, clino, type );
        if ( TDSetting.mAutoSectionPt ) { // insert section point
          float x5 = st.getXSectionX( 4 ); // FIXME offset
          float y5 = st.getXSectionY( 4 );
	  if ( mLandscape ) { float t=x5; x5=-y5; y5=t; }
	  // FIXME String scrap_option = "-scrap " /* + TDInstance.survey + "-" */ + xs_id;
	  String scrap_option = "-scrap " + TDInstance.survey + "-" + xs_id;
	  DrawingPointPath section_pt = new DrawingPointPath( BrushManager.mPointLib.mPointSectionIndex,
							    x5, y5, DrawingPointPath.SCALE_M, 
							    null, scrap_option ); // no text
	  section_pt.setLink( st );
	  mDrawingSurface.addDrawingPath( section_pt );
        }
      } else {
        updatePlotNick( plot, nick );
      }
      if ( plot != null ) {
        pushInfo( plot.type, plot.name, plot.start, "", plot.azimuth, plot.clino, -1 );
        zoomFit( mDrawingSurface.getBitmapBounds() );
      }
    }

    // update section-line x-section nick
    // also at-station
    void updatePlotNick( PlotInfo plot, String nick )
    {
      if ( nick == null || plot == null ) return;
      if ( ! nick.equals( plot.nick ) ) {
        mApp_mData.updatePlotNick( plot.id, mSid, nick );
      }
    }

    void toggleStationSplays( String st_name, boolean on, boolean off ) { mDrawingSurface.toggleStationSplays( st_name, on, off ); }
    boolean isStationSplaysOn( String st_name ) { return mDrawingSurface.isStationSplaysOn( st_name ); }
    boolean isStationSplaysOff( String st_name ) { return mDrawingSurface.isStationSplaysOff( st_name ); }

    void toggleStationHidden( String st_name, boolean is_hidden )
    {
      String hide = mPlot1.hide.trim();
      // Log.v("DistoX", "toggle station " + st_name + " hidden " + is_hidden + " hide: <" + hide + ">" );
      String new_hide = ""; // empty string
      boolean add = false;
      boolean drop = false;
      if ( /* hide == null || */ hide.length() == 0 ) {
        add = true;
        drop = false;
      } else {
        String[] hidden = hide.split( "\\s+" );
        StringBuilder sb = new StringBuilder();
        int k = 0;
        for (; k < hidden.length; ++k ) {
          if ( hidden[k].length() > 0 ) {
            if ( hidden[k].equals( st_name ) ) { // N.B. hidden[k] != null
              drop = true;
            } else {
              sb.append(" ").append( hidden[k] );
              // new_hide = new_hide + " " + hidden[k];
            }
          }
        }
        if ( sb.length() > 0 ) new_hide = sb.toString().trim();
        // if ( new_hide.length() > 0 ) new_hide = new_hide.trim();
        add = ! drop;
      }
      int h = 0;

      if ( add && ! is_hidden ) {
        if ( /* hide == null || */ hide.length() == 0 ) {
          hide = st_name;
        } else {
          hide = hide + " " + st_name;
        }
        // Log.v( "DistoX", "addStationHidden " + st_name + " hide <" + hide + ">" );
        mApp_mData.updatePlotHide( mPid1, mSid, hide );
        mApp_mData.updatePlotHide( mPid2, mSid, hide );
        mPlot1.hide = hide;
        mPlot2.hide = hide;
        h = 1; // hide
      } else if ( drop && is_hidden ) {
        mApp_mData.updatePlotHide( mPid1, mSid, new_hide );
        mApp_mData.updatePlotHide( mPid2, mSid, new_hide );
        mPlot1.hide = new_hide;
        mPlot2.hide = new_hide;
        h = -1; // un-hide
        // Log.v( "DistoX", "dropStationHidden " + st_name + " hide <" + new_hide + ">" );
      }
      // Log.v("DistoX", "toggle station hidden: hide <" + hide + "> H " + h );

      if ( h != 0 ) {
        mNum.setStationHidden( st_name, h );
        recomputeReferences( mZoom );
      }
    }
    //  mNum.setStationHidden( st_name, (hidden? -1 : +1) ); // if hidden un-hide(-1), else hide(+1)

    void toggleStationBarrier( String st_name, boolean is_barrier ) 
    {
      String view = mPlot1.view.trim();
      // Log.v("DistoX", "toggle station " + st_name + " barrier " + is_barrier + " view: <" + view + ">" );
      String new_view = ""; // empty string
      boolean add = false;
      boolean drop = false;
      if ( view == null ) { // always false
        add = true;
        drop = false;
      } else {
        String[] barrier = view.split( " " );
        StringBuilder sb = new StringBuilder();
        int k = 0;
        for (; k < barrier.length; ++k ) {
          if ( barrier[k].length() > 0 ) {
            if ( barrier[k].equals( st_name ) ) { // N.B. barrier[k] != null
              drop = true;
            } else {
              sb.append(" ").append( barrier[k] );
              // new_view = new_view + " " + barrier[k];
            }
          }
        }
        if ( sb.length() > 0 ) new_view = sb.toString().trim();
        // if ( new_view.length() > 0 ) new_view = new_view.trim();
        add = ! drop;
      }
      int h = 0;

      if ( add && ! is_barrier ) {
        if ( /* view == null || */ view.length() == 0 ) {
          view = st_name;
        } else {
          view = view + " " + st_name;
        }
        // Log.v( "DistoX", "addStationBarrier " + st_name + " view <" + view + ">" );
        mApp_mData.updatePlotView( mPid1, mSid, view );
        mApp_mData.updatePlotView( mPid2, mSid, view );
        mPlot1.view = view;
        mPlot2.view = view;
        h = 1;
      } else if ( drop && is_barrier ) {
        mApp_mData.updatePlotView( mPid1, mSid, new_view );
        mApp_mData.updatePlotView( mPid2, mSid, new_view );
        mPlot1.view = new_view;
        mPlot2.view = new_view;
        h = -1;
      }
      // Log.v("DistoX", "toggle station barrier: view <" + view + "> H " + h );

      if ( h != 0 ) {
        mNum.setStationBarrier( st_name, h );
        recomputeReferences( mZoom );
      }
    }
   
    /** add a therion station point
     * @param st    (user) station point
     */
    public void addStationPoint( DrawingStationName st )
    {
      // Log.v("DistoXC", "addStationPoint " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      DrawingStationPath path = new DrawingStationPath( st, DrawingPointPath.SCALE_M );
      mDrawingSurface.addDrawingStationPath( path );
      modified();
    }

    /** delete a station point
     * @param st    (user) station point
     * @param path  path to drop
     */
    public void removeStationPoint( DrawingStationName st, DrawingStationPath path )
    {
      // Log.v("DistoXC", "reoveStationPoint " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null);
      mDrawingSurface.removeDrawingStationPath( path );
      modified();
    }


    void doDelete()
    {
      mApp_mData.deletePlot( mPid1, mSid );
      if ( mPid2 >= 0 ) mApp_mData.deletePlot( mPid2, mSid );
      // mApp.mShotWindow.setRecentPlot( null, 0 );
      TDInstance.setRecentPlot( null, 0 );
      finish();
    }

    /** delete confirmation dialog
     */
    void askDelete()
    {
      TopoDroidAlertDialog.makeAlert( mActivity, getResources(), R.string.plot_delete,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doDelete();
          }
        }
      );
    }

    /** set the drawing window mode (change the list of buttons)
     * @param mode    drawing window mode
     */
    private void setMode( int mode )
    {
      // if ( mMode == mode ) return;
      mMode = mode;
      Log.v("DistoXC", "setMode " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      mLastLinePath = null;
      switch ( mMode ) {
        case MODE_MOVE:
          setTheTitle();
          mDrawingSurface.setDisplayPoints( false );
          mListView.setAdapter( mButtonView1.mAdapter );
          mListView.invalidate();
          break;
        case MODE_DRAW:
          setTheTitle();
          mDrawingSurface.setDisplayPoints( false );
          mListView.setAdapter( mButtonView2.mAdapter );
          mListView.invalidate();
          break;
        case MODE_ERASE:
          setTheTitle();
          mDrawingSurface.setDisplayPoints( false );
          mListView.setAdapter( mButtonView5.mAdapter );
          mListView.invalidate();
          break;
        case MODE_EDIT:
          clearSelected();
          setTheTitle();
          mDrawingSurface.setDisplayPoints( true );
          mListView.setAdapter( mButtonView3.mAdapter );
          mListView.invalidate();
          break;
        default:
          break;
      }
    }

    /** erase mode popup menu
     * @param b     button
     * @param modes ...
     * @param nr    number of modes
     * @param code  code of the filter-listener 
     * @param dismiss if dismiss is JOIN don't make popup
     */
    private void makePopupJoin( View b, int[] modes, int nr, final int code, int dismiss )
    {
      if ( dismiss == DISMISS_JOIN ) return;

      final Context context = this;
      LinearLayout popup_layout = new LinearLayout(mActivity);
      popup_layout.setOrientation(LinearLayout.VERTICAL);
      int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
      int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;

      String text;
      int len = 0;
      Button[] tv = new Button[nr];
      for ( int k=0; k<nr; ++k ) {
        text = getString( modes[k] );
        len = ( len < text.length() )? text.length() : len;
        tv[k] = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight, new JoinClickListener( this, k, code ) );
      }
      FontMetrics fm = tv[0].getPaint().getFontMetrics();
      // Log.v("DistoX", "metrics TOP " + fm.top + " ASC. " + fm.ascent + " BOT " + fm.bottom + " LEAD " + fm.leading ); 
      int w = (int)( Math.abs( ( len ) * fm.ascent ) * 0.6);
      int h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
      for ( int k=0; k<nr; ++k ) {
        tv[k].setWidth( w );
      }
      // Log.v( TopoDroidApp.TAG, "popup width " + w );
      mPopupJoin = new PopupWindow( popup_layout, w, h ); 
      mPopupJoin.showAsDropDown(b); 
    }

    /** filter dropdown menu
     * @param b     button
     * @param modes ...
     * @param nr    number of modes
     * @param code  code of the filter-listener 
     * @param dismiss if dismiss is FILTER don't make popup
     */
    private void makePopupFilter( View b, int[] modes, int nr, final int code, int dismiss )
    {
      if ( dismiss == DISMISS_FILTER ) return;

      final Context context = this;
      LinearLayout popup_layout = new LinearLayout(mActivity);
      popup_layout.setOrientation(LinearLayout.VERTICAL);
      int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
      int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;

      String text;
      int len = 0;
      int w = 0, h = 0;
      Button[] tv = new Button[nr];
      for ( int k=0; k<nr; ++k ) {
        text = getString( modes[k] );
        len = text.length();
        tv[k] = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight, new FilterClickListener( this, k, code ) );
        if ( k == 0 ) {
          FontMetrics fm = tv[0].getPaint().getFontMetrics();
          // Log.v("DistoX", "metrics TOP " + fm.top + " ASC. " + fm.ascent + " BOT " + fm.bottom + " LEAD " + fm.leading ); 
          w = (int)( Math.abs( ( len + 1 ) * fm.ascent ) * 0.6);
          h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
        }
        tv[k].setWidth( w );
      }
      // Log.v( TopoDroidApp.TAG, "popup width " + w );
      mPopupFilter = new PopupWindow( popup_layout, w, h ); 
      // mPopupEdit = new PopupWindow( popup_layout, popup_layout.getHeight(), popup_layout.getWidth() );
      mPopupFilter.showAsDropDown(b); 
    }

    /** line/area editing
     * @param b       button
     * @param dismiss ...
     */
    private void makePopupEdit( View b, int dismiss )
    {
      if ( dismiss == DISMISS_EDIT ) return;

      final Context context = this;
      LinearLayout popup_layout = new LinearLayout(mActivity);
      popup_layout.setOrientation(LinearLayout.VERTICAL);
      int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
      int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;

      // ----- MOVE POINT TO THE NEAREST CLOSE POINT
      //
      String text;
      int len = 0; 
      Button myTextView0 = null;
      Button myTextView1 = null;
      Button myTextView2 = null;
      Button myTextView3 = null;
      Button myTextView4 = null;
      Button myTextView5 = null;
      Button myTextView6 = null;
      Button myTextView7 = null;
      Button myTextView8 = null; // PATH_MULTISELECTION

      if ( mDrawingSurface.isMultiselection() ) {
        int type = mDrawingSurface.getMultiselectionType();
        // ----- REMOVE MULTISELECTION ITEMS
        text = getString(R.string.popup_delete);
        if ( len < text.length() ) len = text.length();
        myTextView0 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              mDrawingSurface.deleteMultiselection();
              modified();
              dismissPopupEdit();
            }
          } );

	if ( type != DrawingPath.DRAWING_PATH_POINT ) { // DRAWING_PATH_LINE or DRAWING_PATH_AREA
          // DECIMATE
          text = getString(R.string.popup_decimate);
          if ( len < text.length() ) len = text.length();
          myTextView1 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                mDrawingSurface.decimateMultiselection();
                modified();
                dismissPopupEdit();
              }
            } );
        }

	if ( type == DrawingPath.DRAWING_PATH_LINE ) {
          // JOIN
          text = getString(R.string.popup_join);
          if ( len < text.length() ) len = text.length();
          myTextView2 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                mDrawingSurface.joinMultiselection( TDSetting.mSelectness/2 );
                modified();
                dismissPopupEdit();
              }
            } );
        }

	// CLEAR MULTISELECTION
        text = getString(R.string.popup_multiselect);
        if ( len < text.length() ) len = text.length();
        myTextView8 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              mDrawingSurface.resetMultiselection();
              dismissPopupEdit();
	    }
          } );

      } else {
        text = getString(R.string.popup_join_pt);
        if ( len < text.length() ) len = text.length();
        myTextView0 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              if ( mHotItemType == DrawingPath.DRAWING_PATH_POINT ||
                   mHotItemType == DrawingPath.DRAWING_PATH_LINE ||
                   mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // SNAP to nearest point POINT/LINE/AREA
                if ( mDrawingSurface.moveHotItemToNearestPoint( TDSetting.mSelectness/2 ) ) {
                  modified();
                } else {
                  TDToast.makeBad( R.string.failed_snap_to_point );
                }
              }
              dismissPopupEdit();
            }
          } );
  
        // ----- SNAP LINE to splays AREA BORDER to close line
        //
        if ( TDLevel.overExpert && TDSetting.mLineSnap ) {
          if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) {
            text = getString( R.string.popup_snap_to_splays );
            if ( len < text.length() ) len = text.length();
            myTextView1 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
              new View.OnClickListener( ) {
                public void onClick(View v) {
                  // if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) { // SNAP to nearest splays [LINE] 
                    switch ( mDrawingSurface.snapHotItemToNearestSplays( TDSetting.mCloseCutoff + 3*mSelectSize / mZoom ) ) {
                      case 0:  // normal
                        modified();
                        break;
                      case -1:
                      case -2:
                      case -3: // no splay close enough
                        TDToast.makeBad( R.string.failed_snap_to_splays );
                        break;
                      default:
                        break;
                    }
                  // }
                  dismissPopupEdit();
                }
              } );
          } else if ( mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
            text = getString( R.string.popup_snap_ln );
            if ( len < text.length() ) len = text.length();
            myTextView1 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
              new View.OnClickListener( ) {
                public void onClick(View v) {
                  // if ( mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // SNAP to nearest line [AREA]
                    switch ( mDrawingSurface.snapHotItemToNearestLine() ) {
                      case 1:  // single point copy
                      case 0:  // normal
                      case -1: // no hot point
                      case -2: // not snapping area border
                        modified();
                        break;
                      case -3: // no line close enough
                        TDToast.makeBad( R.string.failed_snap_to_line );
                        break;
                      default:
                        break;
                    }
                  // }
                  dismissPopupEdit();
                }
              } );
          } 
        }

        if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
          // ----- DUPLICATE LINE/AREA POINT
          //
          text = getString(R.string.popup_split_pt);
          if ( len > text.length() ) len = text.length();
          myTextView2 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // split point LINE/AREA
                  mDrawingSurface.splitHotItem();
                  modified();
                }
                dismissPopupEdit();
              }
            } );

          // ----- REMOVE LINE/AREA POINT
          //
          text = getString(R.string.popup_remove_pt);
          if ( len < text.length() ) len = text.length();
          myTextView6 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // remove pt
                  SelectionPoint sp = mDrawingSurface.hotItem();
                  if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                    DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                    if ( line.size() > 2 ) {
                      removeLinePoint( line, sp.mPoint, sp );
                      line.retracePath();
                      modified();
                    }
                  }
                }
                dismissPopupEdit();
              }
            } );

          if ( TDLevel.overExpert && TDSetting.mLineCurve ) {
            // ----- MAKE LINE/AREA SEGMENT STRAIGHT
            text = getString(R.string.popup_sharp_pt);
            if ( len < text.length() ) len = text.length();
            myTextView4 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
              new View.OnClickListener( ) {
                public void onClick(View v) {
                  if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
                    // make segment straight LINE/AREA
                    SelectionPoint sp = mDrawingSurface.hotItem();
                    if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                      sp.mPoint.has_cp = false;
                      DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                      line.retracePath();
                      modified();
                    }
                  }
                  dismissPopupEdit();
                }
              } );

            // ----- MAKE LINE/AREA SEGMENT SMOOTH (CURVED, WITH CONTROL POINTS)
            text = getString(R.string.popup_curve_pt);
            if ( len < text.length() ) len = text.length();
            myTextView5 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
              new View.OnClickListener( ) {
                public void onClick(View v) {
                  if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
                    // make segment curved LINE/AREA
                    SelectionPoint sp = mDrawingSurface.hotItem();
                    if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                      LinePoint lp0 = sp.mPoint;
                      LinePoint lp2 = lp0.mPrev; 
                      if ( ! lp0.has_cp && lp2 != null ) {
                        float dx = (lp0.x - lp2.x)/3;
                        float dy = (lp0.y - lp2.y)/3;
                        if ( Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01 ) {
                          lp0.x1 = lp2.x + dx;
                          lp0.y1 = lp2.y + dy;
                          lp0.x2 = lp0.x - dx;
                          lp0.y2 = lp0.y - dy;
                          lp0.has_cp = true;
                          DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                          line.retracePath();
                        }
                      }
                      modified();
                    }
                  }
                  dismissPopupEdit();
                }
              } );

          }
        }

        if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) {
          // ----- CUT LINE AT SELECTED POINT AND SPLIT IT IN TWO LINES
          text = getString(R.string.popup_split_ln);
          if ( len < text.length() ) len = text.length();
          myTextView3 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) { // split-line LINE
                  SelectionPoint sp = mDrawingSurface.hotItem();
                  if ( sp != null && sp.type() == DrawingPath.DRAWING_PATH_LINE ) {
                    splitLine( (DrawingLinePath)(sp.mItem), sp.mPoint );
                    modified();
                  }
                }
                dismissPopupEdit();
              }
            } );

          // ATTACH LINE TO LINE
          if ( TDLevel.overExpert ) {
            text = getString(R.string.popup_append_line);
            if ( len < text.length() ) len = text.length();
            myTextView7 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
              new View.OnClickListener( ) {
                public void onClick(View v) {
                  if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) {
                    if ( mDrawingSurface.appendHotItemToNearestLine() ) {
                      modified();
                    } else {
                      TDToast.makeBad( R.string.failed_append_to_line );
                    }
                  }
                  dismissPopupEdit();
                }
              } );
          }
        }

        // PATH_MULTISELECTION
        if ( TDLevel.overExpert && TDSetting.mPathMultiselect ) {
          if (    mHotItemType == DrawingPath.DRAWING_PATH_POINT
               || mHotItemType == DrawingPath.DRAWING_PATH_LINE 
               || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
            text = getString(R.string.popup_multiselect);
            if ( len < text.length() ) len = text.length();
            myTextView8 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                // Log.v("DistoX", "start multi selection");
                mDrawingSurface.startMultiselection();
                dismissPopupEdit();
              }
            } );
          }
        }

      }

      FontMetrics fm = myTextView0.getPaint().getFontMetrics();
      // Log.v("DistoX", "font metrics TOP " + fm.top + " ASC. " + fm.ascent + " BOT " + fm.bottom + " LEAD " + fm.leading ); 
      int w = (int)( Math.abs( ( len + 1 ) * fm.ascent ) * 0.6);
      int h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
      // int h1 = (int)( myTextView0.getHeight() * 7 * 1.1 ); this is 0
      myTextView0.setWidth( w );
      if ( myTextView1 != null ) myTextView1.setWidth( w );
      if ( myTextView2 != null ) myTextView2.setWidth( w );
      if ( myTextView3 != null ) myTextView3.setWidth( w );
      if ( myTextView4 != null ) myTextView4.setWidth( w );
      if ( myTextView5 != null ) myTextView5.setWidth( w );
      if ( myTextView6 != null ) myTextView6.setWidth( w );
      if ( myTextView7 != null ) myTextView7.setWidth( w ); // APPEND LINE TO LINE
      if ( myTextView8 != null ) myTextView8.setWidth( w ); // PATH_MULTISELECTION
      
      mPopupEdit = new PopupWindow( popup_layout, w, h ); 
      // mPopupEdit = new PopupWindow( popup_layout, popup_layout.getHeight(), popup_layout.getWidth() );
      mPopupEdit.showAsDropDown(b); 
    }

    private boolean dismissPopupEdit()
    {
      if ( mPopupEdit != null ) {
        mPopupEdit.dismiss();
        mPopupEdit = null;
        return true;
      }
      return false;
    }

    public boolean dismissPopupFilter()
    {
      if ( mPopupFilter != null ) {
        mPopupFilter.dismiss();
        mPopupFilter = null;
        return true;
      }
      return false;
    }

    public boolean dismissPopupJoin()
    {
      if ( mPopupJoin != null ) {
        mPopupJoin.dismiss();
        mPopupJoin = null;
        return true;
      }
      return false;
    }

    private int dismissPopups() 
    {
      if ( dismissPopupEdit() )         return DISMISS_EDIT;
      if ( dismissPopupFilter() )       return DISMISS_FILTER;
      if ( dismissPopupJoin() )         return DISMISS_JOIN;
      if ( CutNPaste.dismissPopupBT() ) return DISMISS_BT;
      return DISMISS_NONE;
    }

    // -----------------------------------------------------------------------------------------

    private void switchPlotType()
    {
      Log.v("DistoXC", "switchPlotType " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      mLastLinePath = null; // necessary
      doSaveTdr( ); // this sets mModified = false after spawning the saving task
      updateReference();
      if ( mType == PlotInfo.PLOT_PLAN ) {
        setPlotType2( false );
      } else if ( PlotInfo.isProfile( mType ) ) {
        setPlotType1( false );
      }
    }

    // called by doRecover and setPlotType
    private void setPlotType3( )
    {
      assert( mLastLinePath == null);
      // Log.v("DistoX", "set plot type 2 mType " + mType );
      mPid  = mPid3;
      mName = mName3;
      mType = mPlot3.type;
      // TDandroid.setButtonBackground( mButton1[ BTN_PLOT ], mBMextend );
      mDrawingSurface.setManager( DrawingSurface.DRAWING_SECTION, (int)mType );
      resetReference( mPlot3 );
    } 

    private void setPlotType2( boolean compute )
    {
      assert( mLastLinePath == null);
      // Log.v("DistoX", "set plot type 2 mType " + mType );
      mPid  = mPid2;
      mName = mName2;
      mType = mPlot2.type;
      TDandroid.setButtonBackground( mButton1[ BTN_PLOT ], mBMextend );
      mDrawingSurface.setManager( DrawingSurface.DRAWING_PROFILE, (int)mType );
      if ( compute && mNum != null ) {
        computeReferences( mPlot2.type, mPlot2.name, TopoDroidApp.mScaleFactor, false );
      }
      resetReference( mPlot2 );
      if ( TopoDroidApp.mShotWindow != null ) {
        // mApp.mShotWindow.mRecentPlotType = mType;
        TDInstance.recentPlotType = mType;
      } else {
        TDLog.Error("Null app mShotWindow on recent plot type2");
      }
    } 

    // called by setPlotType, switchPlotType and doRecover
    private void setPlotType1( boolean compute )
    {
      assert( mLastLinePath == null);
      // Log.v("DistoX", "set plot type 1 mType " + mType );
      mPid  = mPid1;
      mName = mName1;
      mType = mPlot1.type;
      TDandroid.setButtonBackground( mButton1[ BTN_PLOT ], mBMplan );
      mDrawingSurface.setManager( DrawingSurface.DRAWING_PLAN, (int)mType );
      if ( compute && mNum != null ) {
        computeReferences( mPlot1.type, mPlot1.name, TopoDroidApp.mScaleFactor, false );
      }
      resetReference( mPlot1 );
      if ( TopoDroidApp.mShotWindow != null ) {
        // mApp.mShotWindow.mRecentPlotType = mType;
        TDInstance.recentPlotType = mType;
      } else {
        TDLog.Error("Null app mShotWindow on recent plot type1");
      }
    }

    private void flipBlock( DBlock blk )
    {
      if ( blk != null && blk.flipExtendAndStretch() ) {
        mApp_mData.updateShotExtend( blk.mId, mSid, blk.getFullExtend(), blk.getStretch(), true );
      }
    }

    // flip the profile sketch left/right
    // @param flip_shots whether to flip also the shots extend
    // @note barrier and hiding shots are not flipped
    public void flipProfile( boolean flip_shots )
    {
      // Log.v("DistoXC", "flipProfile " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      mDrawingSurface.flipProfile( mZoom );
      if ( flip_shots ) {
        DBlock blk;
        for ( NumShot sh : mNum.getShots() ) {
          if ( sh.from.show() && sh.to.show() ) {
            flipBlock( sh.getFirstBlock() );
          }
        }
        for ( NumSplay sp : mNum.getSplays() ) {
          if ( sp.from.show() ) {
            flipBlock( sp.getBlock() );
          }
        }
      }
      recomputeProfileReference();
    }


  // this is the same as in ShotWindow
  void doBluetooth( Button b, int dismiss )
  {
    if ( dismiss == DISMISS_BT ) return;
    if ( ! mDataDownloader.isDownloading() ) {
	// FIXME
      if ( TDLevel.overExpert && TDInstance.distoType() == Device.DISTO_X310 
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

    public boolean onLongClick( View view ) 
    {
      Button b = (Button)view;
      if ( TDLevel.overAdvanced && b == mButton1[ BTN_DOWNLOAD ] ) {
        if (   TDSetting.mConnectionMode == TDSetting.CONN_MODE_MULTI
            && ! mDataDownloader.isDownloading() 
            && TopoDroidApp.mDData.getDevices().size() > 1 ) {
          (new DeviceSelectDialog( this, mApp, mDataDownloader, this )).show();
        } else {
          mDataDownloader.toggleDownload();
          setConnectionStatus( mDataDownloader.getStatus() );
          mDataDownloader.doDataDownload( );
        }
      } else if ( b == mButton1[ BTN_PLOT ] ) {
	if ( PlotInfo.isSketch2D( mType ) ) {
          if ( TDLevel.overBasic && mType == PlotInfo.PLOT_EXTENDED ) {
            new DrawingProfileFlipDialog( mActivity, this ).show();
          } else {
            return false; // not consumed
	  }
	} else if ( TDLevel.overExpert ) {
	  mApp.mShowSectionSplays = ! mApp.mShowSectionSplays;
	  // Log.v("DistoX", "toggle section splays " + mShowSectionSplays );
	  mDrawingSurface.setSplayAlpha( mApp.mShowSectionSplays );
          updateSplays( mApp.mSplayMode );
	}
      } else if ( TDLevel.overBasic && b == mButton3[ BTN_REMOVE ] ) {
        SelectionPoint sp = mDrawingSurface.hotItem();
        if ( sp != null ) {
          int t = sp.type();
          String name = null;
          if ( t == DrawingPath.DRAWING_PATH_POINT ) {
            DrawingPointPath pp = (DrawingPointPath)sp.mItem;
            askDeleteItem( pp, t, BrushManager.getPointName( pp.mPointType ) );
          } else if ( t == DrawingPath.DRAWING_PATH_LINE ) {
            DrawingLinePath lp = (DrawingLinePath)sp.mItem;
            if ( lp.size() <= 2 ) {
              askDeleteItem( lp, t, BrushManager.mLineLib.getSymbolName( lp.mLineType ) );
            } else {
              removeLinePoint( lp, sp.mPoint, sp );
              lp.retracePath();
              modified();
            }
          } else if ( t == DrawingPath.DRAWING_PATH_AREA ) {
            DrawingAreaPath ap = (DrawingAreaPath)sp.mItem;
            if ( ap.size() <= 3 ) {
              askDeleteItem( ap, t, BrushManager.mAreaLib.getSymbolName( ap.mAreaType ) );
            } else {
              removeLinePoint( ap, sp.mPoint, sp );
              ap.retracePath();
              modified();
            }
          }
        }
      } else if ( TDLevel.overNormal && b == mButton2[0] ) { // drawing properties
        Intent intent = new Intent( mActivity, TDPrefActivity.class );
        intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_PLOT_DRAW );
        mActivity.startActivity( intent );
      } else if ( TDLevel.overNormal && b == mButton5[1] ) { // erase properties
        Intent intent = new Intent( mActivity, TDPrefActivity.class );
        intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_PLOT_ERASE );
        mActivity.startActivity( intent );
      } else if ( TDLevel.overNormal && b == mButton3[2] ) { // edit properties
        Intent intent = new Intent( mActivity, TDPrefActivity.class );
        intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_PLOT_EDIT );
        mActivity.startActivity( intent );
      }
      return true;
    }

    private void clearSelected()
    {
      // Log.v("DistoXC", "clearSelected " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      // assert( mLastLinePath == null ); // not needed
      mHasSelected = false;
      mDrawingSurface.clearSelected();
      mMode = MODE_EDIT;
      setButton3PrevNext();
      setButton3Item( null );
    }

    public void onClick(View view)
    {
      if ( onMenu ) {
        closeMenu();
        return;
      }
      // TDLog.Log( TDLog.LOG_INPUT, "DrawingWindow onClick() " + view.toString() );
      // TDLog.Log( TDLog.LOG_PLOT, "DrawingWindow onClick() point " + mCurrentPoint + " symbol " + mSymbol );
      int dismiss = dismissPopups();

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
      int k1 = 3;
      int k2 = 3;
      int k3 = 3;
      int k5 = 3;
      if ( ( b == mButton2[0] && mMode == MODE_DRAW ) || 
           ( b == mButton5[1] && mMode == MODE_ERASE ) || 
           ( b == mButton3[2] && ( mMode == MODE_EDIT || mMode == MODE_SHIFT ) ) ) { 
	// PATH_MULTISELECTION
        if ( mDrawingSurface.isMultiselection() ) mDrawingSurface.resetMultiselection();
        setMode( MODE_MOVE );
      } else if ( b == mButton1[0] || b == mButton3[0] || b == mButton5[0] ) { // 0 --> DRAW
        setMode( MODE_DRAW );
      } else if ( b == mButton1[1] || b == mButton2[1] || b == mButton3[1] ) { // 1--> ERASE
        setMode( MODE_ERASE );
        mListView.invalidate();
      } else if ( b == mButton1[2] || b == mButton2[2] || b == mButton5[2] ) { // 2 --> EDIT
        if ( TDLevel.overBasic ) {
          setMode( MODE_EDIT );
        }
      
      // if ( b == mButton1[0] || b == mButton2[0] || b == mButton3[0] || b == mButton5[0] ) {
      //   makeModePopup( b );

      } else if ( b == mButton1[k1++] ) { // DOWNLOAD
        setConnectionStatus( 2 );
        resetFixedPaint();
        updateReference();
        if ( TDInstance.device == null ) {
          // DBlock last_blk = null; // mApp_mData.selectLastLegShot( TDInstance.sid );
          (new ShotNewDialog( mActivity, mApp, this, null, -1L )).show();
        } else {
          mDataDownloader.toggleDownload();
          setConnectionStatus( mDataDownloader.getStatus() );
          mDataDownloader.doDataDownload( );
        }
      } else if ( b == mButton1[k1++] ) { // BLUETOOTH
        doBluetooth( b, dismiss );
      } else if ( b == mButton1[k1++] ) { // DISPLAY MODE 
        new DrawingModeDialog( mActivity, this, mDrawingSurface ).show();

      } else if ( b == mButton1[k1++] ) { //  NOTE
        (new DistoXAnnotations( mActivity, mApp_mData.getSurveyFromId(mSid) )).show();

      } else if ( b == mButton1[k1++] ) { // TOGGLE PLAN/EXTENDED
        if ( PlotInfo.isSketch2D( mType ) ) { 
          startSaveTdrTask( mType, PlotSave.TOGGLE, TDSetting.mBackupNumber+2, TDPath.NR_BACKUP ); 
          // mDrawingSurface.clearDrawing();
          switchPlotType();
        } else if ( PlotInfo.isSection( mType ) ) {
          updateSplays( (mApp.mSplayMode + 1)%4 );
        } else if ( PlotInfo.isXSection( mType ) ) {
          updateSplays( (mApp.mSplayMode + 2)%4 );
        }
      } else if ( TDLevel.overAdvanced && b == mButton1[k1++] ) { //  AZIMUTH
        if ( PlotInfo.isSketch2D( mType ) ) { 
          if ( TDSetting.mAzimuthManual ) {
            setRefAzimuth( 0, - TDAzimuth.mFixedExtend );
          } else {
            // FIXME_AZIMUTH_DIAL 1
            (new AzimuthDialDialog( mActivity, this, TDAzimuth.mRefAzimuth, mBMdial )).show();
            // (new AzimuthDialDialog( mActivity, this, TDAzimuth.mRefAzimuth, mDialBitmap )).show();
          }
        }

      } else if ( b == mButton2[k2++] || b == mButton5[k5++] ) { // UNDO
        mDrawingSurface.undo();
        if ( ! mDrawingSurface.hasMoreUndo() ) {
          // undoBtn.setEnabled( false );
        }
        // redoBtn.setEnabled( true );
        // canRedo = true;/
        modified();
      } else if ( b == mButton2[k2++] || b == mButton5[k5++] ) { // REDO
        if ( mDrawingSurface.hasMoreRedo() ) {
          mDrawingSurface.redo();
        }
      } else if ( b == mButton2[k2++] ) { // pointBtn
        if ( TDSetting.mPickerType == TDSetting.PICKER_RECENT ) { 
          new ItemRecentDialog(mActivity, this, mType ).show();
        } else {
          new ItemPickerDialog(mActivity, this, mType, mSymbol ).show();
        }
      } else if ( TDLevel.overNormal && b == mButton2[k2++] ) { //  CONT continuation popup menu
        if ( mSymbol == Symbol.LINE && BrushManager.mLineLib.getLineGroup( mCurrentLine ) != null ) {
          // setButtonContinue( (mContinueLine+1) % CONT_MAX );
          makePopupJoin( b, Drawing.mJoinModes, 5, 0, dismiss );
        }

      } else if ( b == mButton3[k3++] ) { // PREV
        if ( mHasSelected ) {
          SelectionPoint pt = mDrawingSurface.prevHotItem( );
          if ( mDoEditRange == 0 ) mMode = MODE_SHIFT;
          setButton3Item( pt );
        } else {
          makePopupFilter( b, Drawing.mSelectModes, 6, Drawing.CODE_SELECT, dismiss );
        }
      } else if ( b == mButton3[k3++] ) { // NEXT
        if ( mHasSelected ) {
          SelectionPoint pt = mDrawingSurface.nextHotItem( );
          if ( mDoEditRange == 0 ) mMode = MODE_SHIFT;
          setButton3Item( pt );
        } else {
          setButtonSelectSize( mSelectScale + 1 ); // toggle select size
        }
      } else if ( b == mButton3[k3++] ) { // ITEM/POINT EDITING: move, split, remove, etc.
        // Log.v( TopoDroidApp.TAG, "Button3[5] inLinePoint " + inLinePoint );
        if ( inLinePoint ) {
          makePopupEdit( b, dismiss );
        } else {
          // SelectionPoint sp = mDrawingSurface.hotItem();
          // if ( sp != null && sp.mItem.mType == DrawingPath.DRAWING_PATH_NAME ) {
          //   DrawingStationName sn = (DrawingStationName)(sp.mItem);
          //   new DrawingBarrierDialog( this, this, sn.name(), mNum.isBarrier( sn.name() ) ).show();
          // }
        }
      } else if ( b == mButton3[k3++] ) { // EDIT ITEM PROPERTIES
        SelectionPoint sp = mDrawingSurface.hotItem();
        if ( sp != null ) {
          int flag = 0;
          switch ( sp.type() ) {
            case DrawingPath.DRAWING_PATH_NAME:
              DrawingStationName sn = (DrawingStationName)(sp.mItem);
              DrawingStationPath path = mDrawingSurface.getStationPath( sn.name() );
              boolean barrier = mNum.isBarrier( sn.name() );
              boolean hidden  = mNum.isHidden( sn.name() );
              List< DBlock > legs = mApp_mData.selectShotsAt( TDInstance.sid, sn.name(), true ); // select "independent" legs
              new DrawingStationDialog( mActivity, this, mApp, sn, path, barrier, hidden, /* TDInstance.xsections, */ legs ).show();
              break;
            case DrawingPath.DRAWING_PATH_POINT:
              DrawingPointPath point = (DrawingPointPath)(sp.mItem);
              // Log.v("DistoX", "edit point type " + point.mPointType );
              if ( BrushManager.isPointPhoto( point.mPointType ) ) {
                new DrawingPhotoEditDialog( mActivity, /* this, mApp, */ (DrawingPhotoPath)point ).show();
              } else if ( BrushManager.isPointAudio( point.mPointType ) ) {
                if ( audioCheck ) {
                  DrawingAudioPath audio = (DrawingAudioPath)point;
                  new AudioDialog( mActivity, this, audio.mId ).show();
                } else {
	          // TODO TDToast.makeWarn( R.string.no_feature_audio );
		}
              } else {
                new DrawingPointDialog( mActivity, this, point ).show();
              }
              // mModified = true;
              break;
            case DrawingPath.DRAWING_PATH_LINE:
              DrawingLinePath line = (DrawingLinePath)(sp.mItem);
              if ( line.mLineType == BrushManager.mLineLib.mLineSectionIndex ) {
                // Log.v("DistoX", "edit section line " ); // default azimuth = 0 clino = 0
                // cross-section exists already
                boolean h_section = PlotInfo.isProfile( mType ); // not really necessary
                String id = line.getOption( "-id" );
                if ( id != null ) {
                  new DrawingLineSectionDialog( mActivity, this, /* mApp, */ h_section, true, id, line, null, null, 0, 0, -1 ).show();
                } else {
                  TDLog.Error("edit section line with null id" );
                }
              } else {
                new DrawingLineDialog( mActivity, this, line, sp.mPoint ).show();
              }
              // mModified = true;
              break;
            case DrawingPath.DRAWING_PATH_AREA:
              new DrawingAreaDialog( mActivity, this, (DrawingAreaPath)(sp.mItem) ).show();
              // mModified = true;
              break;
            case DrawingPath.DRAWING_PATH_FIXED:
              DrawingPath p = sp.mItem;
              if ( p != null && p.mBlock != null ) {
                flag = mNum.canBarrierHidden( p.mBlock.mFrom, p.mBlock.mTo );
              }
            case DrawingPath.DRAWING_PATH_SPLAY:
              new DrawingShotDialog( mActivity, this, sp.mItem, flag ).show();
              break;
          }
        }
        clearSelected();
      } else if ( b == mButton3[k3++] ) { // EDIT ITEM DELETE
        SelectionPoint sp = mDrawingSurface.hotItem();
        if ( sp != null ) {
          int t = sp.type();
          if ( t == DrawingPath.DRAWING_PATH_POINT ||
               t == DrawingPath.DRAWING_PATH_LINE  ||
               t == DrawingPath.DRAWING_PATH_AREA  ) {
            String name = "";
            DrawingPath p = sp.mItem;
            switch ( t ) {
              case DrawingPath.DRAWING_PATH_POINT:
                name = BrushManager.getPointName( ((DrawingPointPath)p).mPointType );
                break;
              case DrawingPath.DRAWING_PATH_LINE:
                name = BrushManager.mLineLib.getSymbolName( ((DrawingLinePath)p).mLineType );
                break;
              case DrawingPath.DRAWING_PATH_AREA:
                name = BrushManager.mAreaLib.getSymbolName( ((DrawingAreaPath)p).mAreaType );
                break;
            }
            askDeleteItem( p, t, name );
          } else if ( t == DrawingPath.DRAWING_PATH_SPLAY ) {
            if ( PlotInfo.isSketch2D( mType ) ) { 
              DrawingPath p = sp.mItem;
              DBlock blk = p.mBlock;
              if ( blk != null ) {
                askDeleteSplay( p, sp, blk );
              }
            }
          }
        }
      } else if ( TDLevel.overExpert && b == mButton3[ k3++ ] ) { // RANGE EDIT
        mDoEditRange = ( mDoEditRange + 1 ) % 3;
        if ( BTN_BORDER < mButton3.length ) {
          switch ( mDoEditRange ) {
            case 0:
              TDandroid.setButtonBackground( mButton3[ BTN_BORDER ], mBMedit_no );
              break;
            case 1:
              TDandroid.setButtonBackground( mButton3[ BTN_BORDER ], mBMedit_ok );
              break;
            case 2:
              TDandroid.setButtonBackground( mButton3[ BTN_BORDER ], mBMedit_box );
              break;
          }
        }
      } else if ( b == mButton5[k5++] ) { // ERASE MODE
        makePopupFilter( b, Drawing.mEraseModes, 4, Drawing.CODE_ERASE, dismiss ); // pulldown menu to select erase mode
      } else if ( b == mButton5[k5++] ) { // ERASE SIZE
        setButtonEraseSize( mEraseScale + 1 ); // toggle erase size
      }
    }

    private void askDeleteItem( final DrawingPath p, final int t, final String name )
    {
      TopoDroidAlertDialog.makeAlert( mActivity, getResources(), 
                                String.format( getResources().getString( R.string.item_delete ), name ), 
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            switch( t ) {
              case DrawingPath.DRAWING_PATH_POINT:
                deletePoint( (DrawingPointPath)p );
                break;
              case DrawingPath.DRAWING_PATH_LINE:
                deleteLine( (DrawingLinePath)p );
                break;
              case DrawingPath.DRAWING_PATH_AREA:
                deleteArea( (DrawingAreaPath)p );
                break;
              default:
                break;
            }
          }
        }
      );
    }

    private void askDeleteSplay( final DrawingPath p, final SelectionPoint sp, final DBlock blk )
    {
      TopoDroidAlertDialog.makeAlert( mActivity, getResources(), 
                                String.format( getResources().getString( R.string.splay_delete ), blk.Name() ), 
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            deleteSplay( p, sp, blk );
          }
        }
      );
    }


    private long prepareXSection( String id, long type,
                                 String from, String to, String nick, float azimuth, float clino )
    {
      mCurrentLine = BrushManager.mLineLib.mLineWallIndex;
      if ( ! BrushManager.mLineLib.isSymbolEnabled( "wall" ) ) mCurrentLine = 0;
      // Log.v("DistoXC", "prepareXSection " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
      assert( mLastLinePath == null );
      setTheTitle();

      if ( id == null || id.length() == 0 ) return -1;
      mSectionName = id;
      long pid = mApp_mData.getPlotId( TDInstance.sid, mSectionName );
      if ( pid < 0 ) { 
        // Log.v("DistoXX", "prepare xsection <" + mSectionName + "> nick <" + nick + ">" );
        pid = mApp.insert2dSection( TDInstance.sid, mSectionName, type, from, to, azimuth, clino, ( TDInstance.xsections? null : mName), nick );
      }
      return pid;
    }

    void makePhotoXSection( DrawingLinePath line, String id, long type,
                           String from, String to, String nick, float azimuth, float clino )
    {
      long pid = prepareXSection( id, type, from, to, nick, azimuth, clino );
      if ( pid >= 0 ) {
        // imageFile := PHOTO_DIR / surveyId / photoId .jpg
        File imagefile = new File( TDPath.getSurveyJpgFile( TDInstance.survey, id ) );
        // TODO TD_XSECTION_PHOTO
        doTakePhoto( imagefile, false, pid ); // without inserter
      }
    }

    // X-Section from a section-line
    // @param line    "section" line
    // @param id      section ID, eg "xx0"
    // @param type    either PLOT_SECTION or PLOT_H_SECTION
    // @param from    from station, eg "1"
    // @param to      to station, eg "2"
    // @param azimuth section azimuth
    // @param clino   section clino
    // @param tt      intersection abscissa
    void makePlotXSection( DrawingLinePath line, String id, long type, String from, String to, String nick,
                          float azimuth, float clino, float tt )
    {
      // Log.v("DistoX", "make section: " + id + " <" + from + "-" + to + "> azimuth " + azimuth + " clino " + clino + " tt " + tt );
      long pid = prepareXSection( id, type, from, to, nick, azimuth, clino );
      if ( pid >= 0 ) {
        // Log.v("DistoX", "push info: " + type + " <" + mSectionName + "> TT " + tt );
        pushInfo( type, mSectionName, from, to, azimuth, clino, tt );
        zoomFit( mDrawingSurface.getBitmapBounds() );
      }
    }

    // @param scrapname fullname of the scrap
    // name can be the scrap-name or the section-name (plot name)
    // called only by DrawingPointDialog 
    void openSectionDraw( String scrapname )
    { 
      // remove survey name from scrap-name (if necessary)
      String name = scrapname.replace( TDInstance.survey + "-", "" );
      // Log.v("DistoX", "scrapname " + scrapname + " plot name " + name );

      PlotInfo pi = mApp_mData.getPlotInfo( TDInstance.sid, name );
      if ( pi != null ) {
        pushInfo( pi.type, pi.name, pi.start, pi.view, pi.azimuth, pi.clino, -1 );
        zoomFit( mDrawingSurface.getBitmapBounds() );
      }
    }

    // --------------------------------------------------------------------------

    private void savePng( long type ) // , boolean toast )
    {
      if ( PlotInfo.isAnySection( type ) ) { 
	String fullname = mFullName3;
        DrawingCommandManager manager = mDrawingSurface.getManager( type );
        doSavePng( manager, type, fullname ); // , toast );
      } else {
	String fullname1 = mFullName1;
	String fullname2 = mFullName2;
        // Nota Bene OK for projected profile (to check)
        DrawingCommandManager manager1 = mDrawingSurface.getManager( PlotInfo.PLOT_PLAN );
        DrawingCommandManager manager2 = mDrawingSurface.getManager( PlotInfo.PLOT_EXTENDED );
        doSavePng( manager1, (int)PlotInfo.PLOT_PLAN, fullname1 ); // , toast );
        doSavePng( manager2, (int)PlotInfo.PLOT_EXTENDED, fullname2 ); // , toast );
      }
    }

    private void doSavePng( DrawingCommandManager manager, long type, final String filename ) // , boolean toast )
    {
      if ( manager == null ) {
	// if ( toast ) 
	  TDToast.makeBad( R.string.null_bitmap );
	return;
      }
      Bitmap bitmap = manager.getBitmap( );
      if ( bitmap == null ) {
        // if ( toast )
	  TDToast.makeBad( R.string.null_bitmap );
	return;
      }
      float scale = manager.getBitmapScale();
      String format = getResources().getString( R.string.saved_file_2 );
      new ExportBitmapToFile( format, bitmap, scale, filename, true /* toast */ ).execute();
    }

    // used also by SavePlotFileTask
    void doSaveCsx( String origin, PlotSaveData psd1, PlotSaveData psd2, boolean toast )
    {
      TopoDroidApp.exportSurveyAsCsxAsync( mActivity, origin, psd1, psd2, toast );
    }

    // used to save "dxf", "svg"
    // called only by doExport
    private void saveWithExt( long type, String ext ) // , boolean toast )
    {
      DistoXNum num = mNum;
      TDLog.Log( TDLog.LOG_IO, "export plot type " + type + " with extension " + ext );
      if ( PlotInfo.isAnySection( type ) ) { 
	DrawingCommandManager manager = mDrawingSurface.getManager( type );
	String fullname = mFullName3;
        if ( "csx".equals( ext ) ) {
          doSavePng( manager, type, fullname ); // , toast );
        } else {
          doSaveWithExt( num, /* mDrawingUtil, */ manager, type, fullname, ext, true ); // toast );
        }
      } else {
	DrawingCommandManager manager1 = mDrawingSurface.getManager( mPlot1.type );
	DrawingCommandManager manager2 = mDrawingSurface.getManager( mPlot2.type );
	String fullname1 = mFullName1;
	String fullname2 = mFullName2;
        doSaveWithExt( num, /* mDrawingUtil, */ manager1, mPlot1.type, fullname1, ext, true); // toast );
        doSaveWithExt( num, /* mDrawingUtil, */ manager2, mPlot2.type, fullname2, ext, true); // toast );
      }
    }

    // ext file extension (--> saving class)
    // ext can be dxf, svg
    // FIXME OK PROFILE
    // used also by SavePlotFileTask
    void doSaveWithExt( DistoXNum num, /* DrawingUtil util, */ DrawingCommandManager manager, long type, final String filename, final String ext, boolean toast )
    {
      TDLog.Log( TDLog.LOG_IO, "save with ext: " + filename + " ext " + ext );
      // mActivity = context (only to toast)
      new ExportPlotToFile( mActivity, num, /* util, */ manager, type, filename, ext, toast ).execute();
    }

    // private rotateBackups( String filename )
    // {
    //   String filename2 = filename + "4"; // last backup
    //   File file2 = new File( filename2 );
    //   for ( int i=3; i>=0; --i ) { 
    //     File file1 = new File( filename + Integer.toString(i) );
    //     if ( file1.exists() ) file1.renameTo( file2 );
    //     file2 = file1;
    //   }
    //   File file = new File( filename );
    //   if ( file.exists() ) file.renameTo( file2 );
    // }


  // static private Handler th2Handler = null;

  // called (indirectly) only by ExportDialog: save as th2 even if there are missing symbols
  // no backup_rotate (rotate = 0)
  //
  private void doSaveTh2( long type, final boolean toast )
  {
    DrawingCommandManager manager = mDrawingSurface.getManager( type );
    if ( manager == null ) return;
    Handler th2Handler = null;

    int suffix = PlotSave.EXPORT;
    int azimuth = 0;
    String name = null;
    if ( type == PlotInfo.PLOT_PLAN ) {
      name = mFullName1;
    } else if ( PlotInfo.isProfile( type ) ) {
      azimuth = (int)mPlot2.azimuth;
      name = mFullName2;
    } else {
      name = mFullName3;
    }
    final String filename = name;
    TDLog.Log( TDLog.LOG_IO, "save th2: " + filename );
    if ( toast ) {
      th2Handler = new Handler(){
        @Override public void handleMessage(Message msg) {
          if (msg.what == 661 ) {
            TDToast.make( String.format( getString(R.string.saved_file_1), (filename + ".th2") ) );
          } else {
            TDToast.makeBad( R.string.saving_file_failed );
          }
        }
      };
    } else {
      th2Handler = new Handler(){
        @Override public void handleMessage(Message msg) { }
      };
    }
    try { 
      // Log.v("DistoXX", "save th2 origin " + mPlot1.xoffset + " " + mPlot1.yoffset + " toTherion " + TDSetting.mToTherion );
      (new SavePlotFileTask( mActivity, this, th2Handler, /* mApp, */ mNum, /* mDrawingUtil, */ manager, name, type, azimuth, suffix, 0 )).execute();
    } catch ( RejectedExecutionException e ) { }
  }

  
  // @Override
  // public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo info )
  // {
  //   super.onCreateContextMenu( menu, v, info );
  //   getMenuInflater().inflate( R.menu.popup, menu );
  //   menu.setHeaderTitle( "Context Menu" );
  //   Log.v( TopoDroidApp.TAG, "on Create Context Menu view " + v.toString()  );
  // }

  // @Override
  // public boolean onContextItemSelected( MenuItem item )
  // {
  //   switch ( item.getItemId() ) {
  //     // case ...:
  //     //   break;
  //     default:
  //       break;
  //   }
  //   return super.onOptionsItemSelected( item );
  // }

  private void doMoveTo()
  {
    if ( mMoveTo != null ) {
      // Log.v("DistoX", "do move to" );
      moveTo( mPlot1.type, mMoveTo );
      moveTo( mPlot2.type, mMoveTo );
      mMoveTo = null;
    }
  }

  // called by updateBlockName and refreshDisplay
  private void doComputeReferences( boolean reset )
  {
    Log.v("DistoXC", "doComputeRefenrences " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    // mLastLinePath = null; // not needed
    // Log.v("DistoX", "do Compute References() type " + mType );
    List<DBlock> list = mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );
    mNum = new DistoXNum( list, mPlot1.start, mPlot1.view, mPlot1.hide, mDecl, mFormatClosure );
    // doMoveTo();
    if ( mNum != null ) {
      if ( mType == (int)PlotInfo.PLOT_PLAN ) {
        computeReferences( mPlot2.type, mPlot2.name, TopoDroidApp.mScaleFactor, true );
        computeReferences( mPlot1.type, mPlot1.name, TopoDroidApp.mScaleFactor, false );
        if ( reset ) resetReference( mPlot1 );
      } else if ( PlotInfo.isProfile( mType ) ) {
        computeReferences( mPlot1.type, mPlot1.name, TopoDroidApp.mScaleFactor, false );
        computeReferences( mPlot2.type, mPlot2.name, TopoDroidApp.mScaleFactor, true );
        if ( reset ) resetReference( mPlot2 );
      }
    }
  }

  public void refreshDisplay( int nr, boolean toast )
  {
    // Log.v("DistoX", "refresh Display() type " + mType + " nr " + nr ); // DATA_DOWNLOAD
    mActivity.setTitleColor( TDColor.TITLE_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        doComputeReferences( false );
      }
      if ( toast ) {
        if ( TDInstance.device.mType == Device.DISTO_X310 ) nr /= 2;
        TDToast.make( getResources().getQuantityString(R.plurals.read_data, nr, nr ) );
      }
    } else { // ( nr < 0 )
      if ( toast ) {
        // TDToast.makeBad( getString(R.string.read_fail_with_code) + nr );
        TDToast.makeBad( mApp.DistoXConnectionError[ -nr ] );
      }
    }
  }

  private void updateDisplay( /* boolean compute, boolean reference */ ) // always called with true, false
  {
    // Log.v("DistoX", "updateDisplay() type " + mType + " reference " + reference );
    // if ( compute ) {
      // List<DBlock> list = mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );
      // mNum = new DistoXNum( list, mPlot1.start, mPlot1.view, mPlot1.hide, mDecl, mFormatClosure );
      // // doMoveTo();
      // computeReferences( (int)mPlot1.type, mPlot1.name 0.0f, 0.0f, mApp.mScaleFactor, false );
      // if ( mPlot2 != null ) {
      //   computeReferences( (int)mPlot2.type, mPlot2.name 0.0f, 0.0f, mApp.mScaleFactor, false );
      // }
    // }
    if ( mType != (int)PlotInfo.PLOT_PLAN && ! PlotInfo.isProfile( mType ) ) {
      // FIXME_SK resetReference( mPlot3 );
      // Log.v("DistoX", "update display() type " + mType + " section skip " + mSectionSkip );
      doRestart();
      updateSplays( mApp.mSplayMode );
    } else {
      List<DBlock> list = mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );
      mNum = new DistoXNum( list, mPlot1.start, mPlot1.view, mPlot1.hide, mDecl, mFormatClosure );
      recomputeReferences( TopoDroidApp.mScaleFactor );
      // if ( mType == (int)PlotInfo.PLOT_PLAN ) {
      //   if ( mPlot2 != null ) {
      //     computeReferences( (int)mPlot2.type, mPlot2.name, mApp.mScaleFactor, false );
      //   }
      //   computeReferences( (int)mPlot1.type, mPlot1.name, mApp.mScaleFactor, false );
      //   // resetReference( mPlot1 ); // DATA_DOWNLOAD
      // } else if ( PlotInfo.isProfile( mType ) ) {
      //   computeReferences( (int)mPlot1.type, mPlot1.name, mApp.mScaleFactor, false );
      //   if ( mPlot2 != null ) {
      //     computeReferences( (int)mPlot2.type, mPlot2.name, mApp.mScaleFactor, false );
      //   }
      //   // resetReference( mPlot2 ); // DATA_DOWNLOAD
      // } else {
      // }
    }
  }

  private void zoomFit( RectF b )
  {
    float tb = (b.top + b.bottom)/2;
    float lr = (b.left + b.right)/2;
    if ( mLandscape ) {
      float w = b.bottom - b.top;
      float h = b.right - b.left;
      float wZoom = (float) ( mDrawingSurface.getMeasuredWidth() * 0.9 ) / ( 1 + w );
      float hZoom = (float) ( ( ( mDrawingSurface.getMeasuredHeight() - mListView.getHeight() ) * 0.9 ) / ( 1 + h ));
      mZoom = ( hZoom < wZoom ) ? hZoom : wZoom;
      if ( mZoom < 0.1f ) mZoom = 0.1f;
      mOffset.y = ( TopoDroidApp.mDisplayHeight + mListView.getHeight() - DrawingUtil.CENTER_Y )/(2*mZoom) + lr;
      mOffset.x = ( TopoDroidApp.mDisplayWidth - DrawingUtil.CENTER_X )/(2*mZoom) - tb;
    } else {
      float w = b.right - b.left;
      float h = b.bottom - b.top;
      float wZoom = (float) ( mDrawingSurface.getMeasuredWidth() * 0.9 ) / ( 1 + w );
      float hZoom = (float) ( ( ( mDrawingSurface.getMeasuredHeight() - mListView.getHeight() ) * 0.9 ) / ( 1 + h ));
      mZoom = ( hZoom < wZoom ) ? hZoom : wZoom;
      if ( mZoom < 0.1f ) mZoom = 0.1f;
      mOffset.x = ( TopoDroidApp.mDisplayWidth - DrawingUtil.CENTER_X )/(2*mZoom) - lr;
      mOffset.y = ( TopoDroidApp.mDisplayHeight + mListView.getHeight() - DrawingUtil.CENTER_Y )/(2*mZoom) - tb;
    }
    // Log.v("DistoX", "W " + w + " H " + h + " zoom " + mZoom + " X " + mOffset.x + " Y " + mOffset.y );
    mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );
  }

  // called when the data reduction changes (hidden/barrier)
  private void recomputeReferences( float zoom )
  {
    if ( mNum == null ) return;
    Log.v("DistoXC", "recomputeRefenrences " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    // mLastLinePath = null; // not needed

    if ( mType == (int)PlotInfo.PLOT_PLAN ) {
      if ( mPlot2 != null ) computeReferences( mPlot2.type, mPlot2.name, zoom, false );
    } else if ( PlotInfo.isProfile( mType ) ) {
      computeReferences( mPlot1.type, mPlot1.name, zoom, false );
    }
    computeReferences( (int)mType, mName, zoom, false );
  }

  @Override
  public void updateBlockList( CalibCBlock blk ) { }
  
  // forward adding data to the ShotWindow
  @Override
  public void updateBlockList( DBlock blk ) 
  {
    // Log.v("DistoX", "Drawing window: update Block List block id " + blk.mId + ": " + blk.mFrom + " - " + blk.mTo ); // DATA_DOWNLOAD
    TopoDroidApp.mShotWindow.updateBlockList( blk ); // FIXME_EXTEND this is needed to update sketch splays immediately on download
    updateDisplay( /* true, true */ );
  }

  @Override
  public void updateBlockList( long blk_id )
  {
    // Log.v("DistoX", "Drawing window: update Block List block id " + blk_id ); // DATA_DOWNLOAD
    TopoDroidApp.mShotWindow.updateBlockList( blk_id ); // FIXME_EXTEND this is needed to update sketch splays immediately on download
    updateDisplay( /* true, true */ );
  }

  @Override
  public boolean onSearchRequested()
  {
    // TDLog.Error( "search requested" );
    Intent intent = new Intent( mActivity, TDPrefActivity.class );
    intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_CATEGORY_PLOT );
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
      case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
      case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  // ---------------------------------------------------------
  // MENU

  private void setMenuAdapter( Resources res, long type )
  {
    ArrayAdapter< String > mMenuAdapter = new ArrayAdapter<>(mActivity, R.layout.menu );
    // HOVER
    // mMenuAdapter = new MyMenuAdapter( this, this, mMenu, R.layout.menu, new ArrayList< MyMenuItem >() );

    if ( PlotInfo.isSketch2D( type ) && TDLevel.overNormal ) {
      mMenuAdapter.add( res.getString( menus[0] ) ); // SWITCH/CLOSE
    } else {
      mMenuAdapter.add( res.getString( menus[MENU_CLOSE] ) );  // CLOSE
    }
    mMenuAdapter.add( res.getString( menus[1] ) );  // EXPORT
    if ( PlotInfo.isAnySection( type ) ) {
      mMenuAdapter.add( res.getString( menus[MENU_AREA] ) );  // AREA
    } else {
      mMenuAdapter.add( res.getString( menus[2] ) );  // INFO
    }
    if ( TDLevel.overNormal ) {
      mMenuAdapter.add( res.getString( menus[3] ) );  // RELOAD
      mMenuAdapter.add( res.getString( menus[4] ) );  // ZOOM_FIT
    }
    if ( TDLevel.overAdvanced && PlotInfo.isSketch2D( type ) ) {
      mMenuAdapter.add( res.getString( menus[5] ) ); // RENAME/DELETE
    }
    mMenuAdapter.add( res.getString( menus[6] ) ); // PALETTE
    if ( TDLevel.overBasic && PlotInfo.isSketch2D( type ) ) {
      mMenuAdapter.add( res.getString( menus[7] ) ); // OVERVIEW
    }
    mMenuAdapter.add( res.getString( menus[8] ) ); // OPTIONS
    mMenuAdapter.add( res.getString( menus[9] ) ); // HELP
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    // HOVER
    // mMenuAdapter.resetBgColor();
    onMenu = false;
  }

  private void doZoomFit()
  {
    // FIXME for big sketches this leaves out some bits at the ends
    // maybe should increse the bitmap bounds by a small factor ...
    RectF b = mDrawingSurface.getBitmapBounds();
    zoomFit( b );
  }

  void centerAtStation( String station )
  {
    NumStation st = mNum.getStation( station );
    if ( st == null ) {
      TDToast.makeBad( R.string.missing_station );
    } else {
      moveTo( mPlot1.type, station );
      moveTo( mPlot2.type, station );
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );
    }
  }

  void setOrientation( int orientation )
  {
    boolean landscape = (orientation == PlotInfo.ORIENTATION_LANDSCAPE);
    if ( landscape != mLandscape ) {
      mLandscape = landscape;
      // if ( mLandscape ) {
      //         float t = mOffset.x; mOffset.x = mOffset.y;  mOffset.y = -t;
      // } else {
      //         float t = mOffset.x; mOffset.x = -mOffset.y;  mOffset.y = t;
      // }
      mApp_mData.updatePlotOrientation( TDInstance.sid, mPid, mLandscape ? 1 : 0 );
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );
      doZoomFit();
      setTheTitle();
    } else {
      doZoomFit();
    }
  }

  private void handleMenu( int pos )
  {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) {
        if ( PlotInfo.isSketch2D( mType ) ) { // SWITCH/CLOSE
          if ( TDLevel.overNormal ) {
            new PlotListDialog( mActivity, null, mApp, this ).show();
          } else {
            super.onBackPressed();
          }
        } else { // CLOSE
          super.onBackPressed();
        }
      } else if ( p++ == pos ) { // EXPORT
        new ExportDialog( mActivity, this, TDConst.mPlotExportTypes, R.string.title_plot_save ).show();
      } else if ( p++ == pos ) { // INFO / AREA
        if ( PlotInfo.isAnySection( mType ) ) {
          float area = mDrawingSurface.computeSectionArea() / (DrawingUtil.SCALE_FIX * DrawingUtil.SCALE_FIX);
          Resources res = getResources();
          String msg = String.format( res.getString( R.string.section_area ), area );
          TopoDroidAlertDialog.makeAlert( mActivity, res, msg, R.string.button_ok, -1, null, null );
	} else {
          if ( mNum != null ) {
            float azimuth = -1;
            if ( mPlot2 !=  null && PlotInfo.PLOT_PROJECTED == mPlot2.type ) {
              azimuth = mPlot2.azimuth;
            }
            new DistoXStatDialog( mActivity, mNum, mPlot1.start, azimuth, mApp_mData.getSurveyStat( TDInstance.sid ) ).show();
          } else {
            TDToast.makeBad( R.string.no_data_reduction );
	  }
	}
      } else if ( TDLevel.overNormal && p++ == pos ) { // RECOVER RELOAD
        if ( PlotInfo.isProfile( mType ) ) {
          ( new PlotRecoverDialog( mActivity, this, mFullName2, mType ) ).show();
        } else if ( mType == PlotInfo.PLOT_PLAN ) {
          ( new PlotRecoverDialog( mActivity, this, mFullName1, mType ) ).show();
        } else {
          ( new PlotRecoverDialog( mActivity, this, mFullName3, mType ) ).show();
        }
      } else if ( TDLevel.overNormal && p++ == pos ) { // ZOOM_FIT / ORIENTATION
	if ( TDLevel.overExpert ) {
          ( new PlotZoomFitDialog( mActivity, this ) ).show();
	} else {
	  doZoomFit();
	}
      } else if ( TDLevel.overAdvanced && PlotInfo.isSketch2D( mType ) && p++ == pos ) { // RENAME/DELETE
        //   askDelete();
        (new PlotRenameDialog( mActivity, this /*, mApp */ )).show();

      } else if ( p++ == pos ) { // PALETTE
        BrushManager.makePaths( mApp, getResources() );
        (new SymbolEnableDialog( mActivity )).show();

      } else if ( TDLevel.overBasic && PlotInfo.isSketch2D( mType ) && p++ == pos ) { // OVERVIEW
        if ( mType == PlotInfo.PLOT_PROJECTED ) {
          TDToast.makeBad( R.string.no_profile_overview );
        } else {
          updateReference();
          Intent intent = new Intent( this, OverviewWindow.class );
          intent.putExtra( TDTag.TOPODROID_SURVEY_ID, mSid );
          intent.putExtra( TDTag.TOPODROID_PLOT_FROM, mFrom );
          intent.putExtra( TDTag.TOPODROID_PLOT_ZOOM, mZoom );
          intent.putExtra( TDTag.TOPODROID_PLOT_TYPE, mType );
          intent.putExtra( TDTag.TOPODROID_PLOT_LANDSCAPE, mLandscape );
          intent.putExtra( TDTag.TOPODROID_PLOT_XOFF, mOffset.x );
          intent.putExtra( TDTag.TOPODROID_PLOT_YOFF, mOffset.y );
          mActivity.startActivity( intent );
        }
      } else if ( p++ == pos ) { // OPTIONS
        updateReference();
        Intent intent = new Intent( mActivity, TDPrefActivity.class );
        intent.putExtra( TDPrefActivity.PREF_CATEGORY, TDPrefActivity.PREF_CATEGORY_PLOT );
        mActivity.startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        // 1 for select-tool
        int nn = 1 + NR_BUTTON1 + NR_BUTTON2 - 3 + NR_BUTTON5 - 5 + ( TDLevel.overBasic? mNrButton3 - 3: 0 );
        // Log.v("DistoX", "Help menu, nn " + nn );
        new HelpDialog(mActivity, izons, menus, help_icons, help_menus, nn, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
      }
  }

  // interface IExporter
  public void doExport( String export_type )
  {
    int index = TDConst.plotExportIndex( export_type );
    switch ( index ) {
      case TDConst.DISTOX_EXPORT_TH2: doSaveTh2( mType, true ); break;
      case TDConst.DISTOX_EXPORT_CSX: 
        if ( ! PlotInfo.isAnySection( mType ) ) { // FIXME x-sections are saved PNG for CSX
          if ( mPlot1 != null ) {
            String origin = mPlot1.start;
	    int suffix    = PlotSave.EXPORT;
	    PlotSaveData psd1 = makePlotSaveData( 1, suffix, 0 );
	    PlotSaveData psd2 = makePlotSaveData( 2, suffix, 0 );
            doSaveCsx( origin, psd1, psd2, true );
	  }
          break;
        } // else fall-through and savePng
      case TDConst.DISTOX_EXPORT_PNG: savePng( mType ); break; // , true ); break;
      case TDConst.DISTOX_EXPORT_DXF: saveWithExt( mType, "dxf" ); break; // , true ); break;
      case TDConst.DISTOX_EXPORT_SVG: saveWithExt( mType, "svg" ); break; // , true ); break;
      case TDConst.DISTOX_EXPORT_SHP: saveWithExt( mType, "shp" ); break; // , true ); break;
      case TDConst.DISTOX_EXPORT_XVI: saveWithExt( mType, "xvi" ); break; // , true ); break;
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) { // MENU
      handleMenu( pos );
    }
  }

  void doRecover( String filename, long type )
  {
    // Log.v("DistoXC", "doRecover " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    mLastLinePath = null; // absolutely necessary
    float x = mOffset.x;
    float y = mOffset.y;
    float z = mZoom;
    String tdr  = TDPath.getTdrFile( filename );
    // String th2  = TDPath.getTh2File( filename );
    TDLog.Log( TDLog.LOG_IO, "reload tdr " + filename + " file " + tdr );
    // Log.v("DistoX", "recover " + type + " <" + filename + "> TRD " + tdr + " TH2 " + th2 );
    if ( type == PlotInfo.PLOT_PLAN ) {
      mDrawingSurface.resetManager( DrawingSurface.DRAWING_PLAN, null, false );
      mDrawingSurface.modeloadDataStream( tdr, /* th2, */ null ); // no missing symbols
      mDrawingSurface.linkSections();
      DrawingSurface.addManagerToCache( mFullName1 );
      setPlotType1( true );
    } else if ( PlotInfo.isProfile( type ) ) {
      mDrawingSurface.resetManager( DrawingSurface.DRAWING_PROFILE, null, PlotInfo.isExtended(type) );
      mDrawingSurface.modeloadDataStream( tdr, /* th2, */ null );
      mDrawingSurface.linkSections();
      DrawingSurface.addManagerToCache( mFullName2 );
      // now switch to extended view FIXME-VIEW
      setPlotType2( true );
    } else {
      mDrawingSurface.resetManager( DrawingSurface.DRAWING_SECTION, null, false );
      mDrawingSurface.modeloadDataStream( tdr, /* th2, */ null );
      // DrawingSurface.addManagerToCache( mFullName2 ); // sections are not cached
      setPlotType3( );
      DrawingUtil.addGrid( -10, 10, -10, 10, 0.0f, 0.0f, mDrawingSurface );
      makeSectionReferences( mApp_mData.selectAllShots( mSid, TDStatus.NORMAL ), -1, 0 );
    }
    mOffset.x = x;
    mOffset.y = y;
    mZoom     = z;
    mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom, mLandscape );
  }

  static void exportAsCsx( long sid, PrintWriter pw, String survey, String cave, String branch, /* String session, */ PlotSaveData psd1, PlotSaveData psd2 )
  {
    // Log.v("DistoX", "export as CSX <<" + cave + ">>" );
    List< PlotInfo > all_sections = TopoDroidApp.mData.selectAllPlotsSection( sid, TDStatus.NORMAL );
    ArrayList< PlotInfo > sections1 = new ArrayList<>(); // plan xsections
    ArrayList< PlotInfo > sections2 = new ArrayList<>(); // profile xsections

    pw.format("  <plan>\n");
    if ( psd1 != null ) {
      DrawingSurface.exportAsCsx( pw, PlotInfo.PLOT_PLAN, survey, cave, branch, /* session, */ psd1.cm, all_sections, sections1 /* , psd1.util */ );
    }
    pw.format("    <plot />\n");
    pw.format("  </plan>\n");
    
    pw.format("  <profile>\n");
    if ( psd2 != null ) {
      DrawingSurface.exportAsCsx( pw, PlotInfo.PLOT_EXTENDED, survey, cave, branch, /* session, */ psd2.cm, all_sections, sections2 /* , psd2.util */ ); 
    }
    pw.format("    <plot />\n");
    pw.format("  </profile>\n");

    pw.format("    <crosssections>\n");
    if ( psd1 != null ) {
      for ( PlotInfo section1 : sections1 ) {
        pw.format("    <crosssection id=\"%s\" design=\"0\" crosssection=\"%d\">\n", section1.name, section1.csxIndex );
        // exportCsxXSection( pw, section1, survey, cave, branch, /* session, */ mDrawingUtil );
        exportCsxXSection( pw, section1, survey, cave, branch /* , session */ /* , psd1.util */ );
        pw.format("    </crosssection>\n" );
      }
    }
    if ( psd2 != null ) {
      for ( PlotInfo section2 : sections2 ) {
        pw.format("    <crosssection id=\"%s\" design=\"1\" crosssection=\"%d\">\n", section2.name, section2.csxIndex );
        // exportCsxXSection( pw, section2, survey, cave, branch, /* session, */ mDrawingUtil );
        exportCsxXSection( pw, section2, survey, cave, branch /* , session */ /* , psd2.util */ );
        pw.format("    </crosssection>\n" );
      }
    }
    pw.format("    </crosssections>\n");
  }

  private static void exportCsxXSection( PrintWriter pw, PlotInfo section, String survey, String cave, String branch
		  /* , String session */ /* , DrawingUtil drawingUtil */ )
  {
    // String name = section.name; // binding name
    // open xsection file
    String filename = TDPath.getSurveyPlotTdrFile( survey, section.name );
    DrawingIO.doExportCsxXSection( pw, filename, survey, cave, branch, /* session, */ section.name /* , drawingUtil */ ); // bind=section.name
  }

  public void setConnectionStatus( int status )
  { 
    if ( TDInstance.device == null ) {
      TDandroid.setButtonBackground( mButton1[ BTN_DOWNLOAD ], mBMadd );
      TDandroid.setButtonBackground( mButton1[ BTN_BLUETOOTH ], mBMbluetooth_no );
    } else {
      switch ( status ) {
        case 1:
          TDandroid.setButtonBackground( mButton1[ BTN_DOWNLOAD ], mBMdownload_on );
          TDandroid.setButtonBackground( mButton1[ BTN_BLUETOOTH ], mBMbluetooth_no );
          break;
        case 2:
          TDandroid.setButtonBackground( mButton1[ BTN_DOWNLOAD ], mBMdownload_wait );
          TDandroid.setButtonBackground( mButton1[ BTN_BLUETOOTH ], mBMbluetooth_no );
          break;
        default:
          TDandroid.setButtonBackground( mButton1[ BTN_DOWNLOAD ], mBMdownload );
          TDandroid.setButtonBackground( mButton1[ BTN_BLUETOOTH ], mBMbluetooth );
      }
    }
  }

  public void enableBluetoothButton( boolean enable )
  {
    TDandroid.setButtonBackground( mButton1[BTN_BLUETOOTH], enable ? mBMbluetooth : mBMbluetooth_no );
    mButton1[BTN_BLUETOOTH].setEnabled( enable );
  }


// -------------------------------------------------------------
// AUTO WALLS

  void drawWallsAt( DBlock blk )
  {
    if ( TDSetting.mWallsType == TDSetting.WALLS_NONE ) return;

    String station1 = blk.mFrom;
    String station2 = blk.mTo;
    float cl = blk.mClino;
    float br = blk.mBearing;

    NumStation st1 = mNum.getStation( station1 );
    NumStation st2 = mNum.getStation( station2 );
    float x0, y0, x1, y1;
    if ( mType == PlotInfo.PLOT_PLAN ) {
      x0 = st1.e;
      y0 = st1.s;
      x1 = st2.e;
      y1 = st2.s;
    } else {
      x0 = st1.h;
      y0 = st1.v;
      x1 = st2.h;
      y1 = st2.v;
    }
    float x2 = x1 - x0;
    float y2 = y1 - y0;
    float x22  = x2 * x2;
    float len2 = x2 * x2 + y2 * y2 + 0.0001f;
    float len  = (float)Math.sqrt( len2 );
    PointF uu = new PointF( x2 / len, y2 / len );
    PointF vv = new PointF( -uu.y, uu.x );

    // Log.v("DistoX", "X0 " + x0 + " " + y0 + " X1 " + x1 + " " + y1 );
    // Log.v("DistoX", "U " + uu.x + " " + uu.y + " V " + vv.x + " " + vv.y );

    boolean allSplay = ( TDSetting.mWallsType == TDSetting.WALLS_DLN );

    ArrayList< PointF > pos = null;
    ArrayList< PointF > neg = null;
    ArrayList< DLNSite > sites = null;
    if ( TDSetting.mWallsType == TDSetting.WALLS_CONVEX ) {
      pos = new ArrayList<>(); // positive v
      neg = new ArrayList<>(); // negative v
    } else {
      sites = new ArrayList<>();
      sites.add( new DLNSite( x0, y0 ) );
      sites.add( new DLNSite( x1, y1 ) );
    }
    List< NumSplay > splays = mNum.getSplays();
    float xs=0, ys=0;
    if ( mType == PlotInfo.PLOT_PLAN ) {
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        boolean ok = false;
        if ( st == st1 ) {
          if ( Math.abs( sp.getBlock().mClino - cl ) < TDSetting.mWallsPlanThr ) {
            xs = sp.e;
            ys = sp.s;
            if ( allSplay ) { 
              ok = true;
            } else {
              xs -= x0;
              ys -= y0;
              float proj = ( xs*x2 + ys*y2 )/len2;
              ok = ( proj >= 0 && proj <= 1 );
            }
          }
        } else if ( st == st2 ) {
          if ( Math.abs( sp.getBlock().mClino + cl ) < TDSetting.mWallsPlanThr ) {
            xs = sp.e;
            ys = sp.s;
            if ( allSplay ) { 
              ok = true;
            } else {
              xs -= x0;
              ys -= y0;
              float proj = ( xs*x2 + ys*y2 )/len2;
              ok = ( proj >= 0 && proj <= 1 );
            }
          }
        }
        if ( ok ) {
          if ( allSplay ) {
            if (sites != null) sites.add( new DLNSite( xs, ys ) );
          } else {
            // xs = (float)(sp.e) - x0;
            // yv = (float)(sp.s) - y0;
            float u = xs * uu.x + ys * uu.y;
            float v = xs * vv.x + ys * vv.y;
            if ( v > 0 ) {
              if (pos != null) pos.add( new PointF(u,v) );
            } else {
              if (neg != null) neg.add( new PointF(u,v) );
            }
          }
        } 
      }
    } else { // PLOT_EXTENDED || PLOT_PROJECTED
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        if ( st == st1 || st == st2 ) {
          boolean ok = false;
          if ( Math.abs( sp.getBlock().mClino ) > TDSetting.mWallsExtendedThr ) { // FIXME
            xs = sp.h;
            ys = sp.v;
            if ( allSplay ) { 
              ok = true;
            } else {
              xs -= x0;
              ys -= y0;
              float proj = ( xs*x2 )/ x22;
              ok = ( proj >= 0 && proj <= 1 );
            }
            if ( ok ) {
              if ( allSplay ) {
                if (sites != null) sites.add( new DLNSite( xs, ys ) );
              } else {
                float u = xs * uu.x + ys * uu.y;
                float v = xs * vv.x + ys * vv.y;
                // Log.v("WALL", "Splay " + x2 + " " + y2 + " --> " + u + " " + v);
                if ( /* allSplay || */ v > 0 ) { // allSplay is false
                  if (pos != null) pos.add( new PointF(u,v) );
                } else {
                  if (neg != null) neg.add( new PointF(u,v) );
                }
              }
            }
          }
        }
      }
    }
    // (x0,y0) (x1,y1) are the segment endpoints
    // len is its length
    // uu is the unit vector from 0 to 1
    // vv is the orthogonal unit vector
    if ( TDSetting.mWallsType == TDSetting.WALLS_CONVEX ) {
      makeWall( pos, x0, y0, x1, y1, len, uu, vv );
      makeWall( neg, x0, y0, x1, y1, len, uu, vv );
    } else if ( TDSetting.mWallsType == TDSetting.WALLS_DLN ) {
      makeDlnWall( sites, x0, y0, x1, y1, len, uu, vv );
    }
    modified();
  }

  void addPointsToLine( DrawingLinePath line, float x0, float y0, float xx, float yy )
  {
    float ll = (float)Math.sqrt( (xx-x0)*(xx-x0) + (yy-y0)*(yy-y0) ) / 20;
    if ( ll > TDSetting.mWallsXStep ) {
      int n = 1 + (int)ll;
      float dx = (xx-x0) / n;
      float dy = (yy-y0) / n;
      for ( int k=1; k<n; ++k ) {
        line.addPoint( x0+k*dx, y0+k*dy );
      }
    }
    line.addPoint( xx, yy );
  }

  void makeDlnWall( ArrayList<DLNSite> sites, float x0, float y0, float x1, float y1, float len, PointF uu, PointF vv )
  {
    DLNWall dln_wall = new DLNWall( new Point2D(x0,y0), new Point2D(x1,y1) );
    dln_wall.compute( sites );
    if ( dln_wall.mPosHull.size() > 0 ) {
      DLNSideList hpos = dln_wall.mPosHull.get(0);
      DLNSide side = hpos.side;
      float xx = DrawingUtil.toSceneX( side.mP1.x, side.mP1.y );
      float yy = DrawingUtil.toSceneY( side.mP1.x, side.mP1.y );
      DrawingLinePath path = new DrawingLinePath( BrushManager.mLineLib.mLineWallIndex );
      path.addStartPoint( xx, yy );
      for ( DLNSideList hp : dln_wall.mPosHull ) {
        side = hp.side;
        float xx2 = DrawingUtil.toSceneX( side.mP2.x, side.mP2.y );
        float yy2 = DrawingUtil.toSceneY( side.mP2.x, side.mP2.y );
        addPointsToLine( path, xx, yy, xx2, yy2 );
        xx = xx2;
        yy = yy2;
      } 
      // FIXME_LANDSCAPE if ( mLandscape ) path.landscapeToPortrait();
      path.computeUnitNormal();
      mDrawingSurface.addDrawingPath( path );
    }
    if ( dln_wall.mNegHull.size() > 0 ) {
      DLNSideList hneg = dln_wall.mNegHull.get(0);
      DLNSide side = hneg.side;
      float xx = DrawingUtil.toSceneX( side.mP1.x, side.mP1.y );
      float yy = DrawingUtil.toSceneY( side.mP1.x, side.mP1.y );
      DrawingLinePath path = new DrawingLinePath( BrushManager.mLineLib.mLineWallIndex );
      path.addStartPoint( xx, yy );
      for ( DLNSideList hn : dln_wall.mNegHull ) {
        side = hn.side;
        float xx2 = DrawingUtil.toSceneX( side.mP2.x, side.mP2.y );
        float yy2 = DrawingUtil.toSceneY( side.mP2.x, side.mP2.y );
        addPointsToLine( path, xx, yy, xx2, yy2 );
        xx = xx2;
        yy = yy2;
      } 
      // FIXME_LANDSCAPE if ( mLandscape ) path.landscapeToPortrait();
      path.computeUnitNormal();
      mDrawingSurface.addDrawingPath( path );
    }
  }

  /*
  void makeDlnWall( ArrayList<DLNSite> sites, float x0, float y0, float x1, float y1, float len, PointF uu, PointF vv )
  {
    DLNWall dln_wall = new DLNWall( new Point2D(x0,y0), new Point2D(x1,y1) );
    dln_wall.compute( sites );
    DLNSideList hull = dln_wall.getBorderHead();
    DLNSide side = hull.side;
    float xx = DrawingUtil.toSceneX( side.mP1.x, side.mP1.y );
    float yy = DrawingUtil.toSceneY( side.mP1.x, side.mP1.y );
    DrawingLinePath path = new DrawingLinePath( BrushManager.mLineLib.mLineWallIndex );
    path.addStartPoint( xx, yy );
    int size = dln_wall.hullSize();
    for ( int k=0; k<size; ++k ) {
      float xx2 = DrawingUtil.toSceneX( side.mP2.x, side.mP2.y );
      float yy2 = DrawingUtil.toSceneY( side.mP2.x, side.mP2.y );
      addPointsToLine( path, xx, yy, xx2, yy2 );
      xx = xx2;
      yy = yy2;
      hull = hull.next;
      side = hull.side;
    } 
    // FIXME_LANDSCAPE if ( mLandscape ) path.landscapeToPortrait();
    path.computeUnitNormal();
    mDrawingSurface.addDrawingPath( path );
  }
  */

  void makeWall( ArrayList<PointF> pts, float x0, float y0, float x1, float y1, float len, PointF uu, PointF vv )
  {
    int size = pts.size();
    float xx, yy;
    if ( size == 0 ) { // no wall
      return;
    } else if ( size == 1 ) {
      PointF p = pts.get(0);
      if ( p.x > 0 && p.x < len ) { // wall from--p--to
	    float x2 = x0 + uu.x * p.x + vv.x * p.y;
	    float y2 = y0 + uu.y * p.x + vv.y * p.y;
        xx = DrawingUtil.toSceneX( x2, y2 ); 
        yy = DrawingUtil.toSceneY( x2, y2 );
        x0 = DrawingUtil.toSceneX( x0, y0 );
        y0 = DrawingUtil.toSceneY( x0, y0 );
        x1 = DrawingUtil.toSceneX( x1, y1 );
        y1 = DrawingUtil.toSceneY( x1, y1 );
        DrawingLinePath path = new DrawingLinePath( BrushManager.mLineLib.mLineWallIndex );
        path.addStartPoint( x0, y0 );
        addPointsToLine( path, x0, y0, xx, yy );
        addPointsToLine( path, xx, yy, x1, y1 );
        if ( mLandscape ) path.landscapeToPortrait();
        path.computeUnitNormal();
        mDrawingSurface.addDrawingPath( path );
      }
    } else {
      sortPointsOnX( pts );
      PointF p1 = pts.get(0);
      float x2 = x0 + uu.x * p1.x + vv.x * p1.y;
      float y2 = y0 + uu.y * p1.x + vv.y * p1.y;
      xx = DrawingUtil.toSceneX( x2, y2 );
      yy = DrawingUtil.toSceneY( x2, y2 );
      DrawingLinePath path = new DrawingLinePath( BrushManager.mLineLib.mLineWallIndex );
      path.addStartPoint( xx, yy );
      for ( int k=1; k<pts.size(); ++k ) {
        p1 = pts.get(k);
	x2 = x0 + uu.x * p1.x + vv.x * p1.y;
	y2 = y0 + uu.y * p1.x + vv.y * p1.y;
        float xx2 = DrawingUtil.toSceneX( x2, y2 );
        float yy2 = DrawingUtil.toSceneY( x2, y2 );
        addPointsToLine( path, xx, yy, xx2, yy2 );
        xx = xx2;
        yy = yy2;
      }
      if ( mLandscape ) path.landscapeToPortrait();
      path.computeUnitNormal();
      mDrawingSurface.addDrawingPath( path );
    }
  }

  // sort the points on the list by increasing X
  // @param pts list of points
  private void sortPointsOnX( ArrayList<PointF> pts ) 
  {
    int size = pts.size();
    if ( size < 2 ) return;
    boolean repeat = true;
    PointF p1, p2;
    while ( repeat ) {
      repeat = false;
      for ( int k = 1; k < size; ++ k ) {
        p1 = pts.get(k-1);
        p2 = pts.get(k);
        if ( p2.x < p1.x ) {
          float x = p1.x; p1.x = p2.x; p2.x = x;
          float y = p1.y; p1.y = p2.y; p2.y = y;
          repeat = true;
        }
      }
    }

    // remove points with X close to a nearby and smaller Y
    for ( int k = 1; k < pts.size(); ++ k ) {
      p1 = pts.get(k-1);
      p2 = pts.get(k);
      if ( (p2.x - p1.x) < TDSetting.mWallsXClose ) { 
        if ( Math.abs(p2.y) < Math.abs(p1.y) ) { // remove p2
          pts.remove( k );
        } else {
          pts.remove( k-1 ); // no need to move k backward
        }
      } else {
        ++k;
      }
    }
    
    // convex-hull: remove points "inside" (with smaller |Y| )
    if ( size > 2 ) {
      float x0 = pts.get(0).x;
      float y0 = Math.abs( pts.get(0).y );
      for ( int k = 0; k < pts.size()-1; ++k ) {
        int hh = k+1;
        float x1 = pts.get(hh).x;
        float y1 = Math.abs( pts.get(hh).y );
        float s0 = (y1-y0)/(x1-x0); // N.B. x1 >= x0 + 0.1
          for ( int h=hh+1; h<pts.size(); ++h ) {
          x1 = pts.get(h).x;
          y1 = Math.abs( pts.get(h).y );
          float s1 = (y1-y0)/(x1-x0); 
          if ( s1 > s0 + TDSetting.mWallsConcave ) { // allow small concavities
            hh = h;
          }
        }
        for ( int h=hh-1; h>k; --h ) pts.remove(h);
      }
    }
  }

  @Override
  protected void onActivityResult( int reqCode, int resCode, Intent data )
  {
    switch ( reqCode ) {
      // case TDRequest.QCAM_COMPASS_DRAWWINDOW: // not used
      //   if ( resCode == Activity.RESULT_OK ) {
      //     try {
      //       Bundle extras = data.getExtras();
      //       float b = Float.parseFloat( extras.getString( TDTag.TOPODROID_BEARING ) );
      //       float c = Float.parseFloat( extras.getString( TDTag.TOPODROID_CLINO ) );
      //       mShotNewDialog.setBearingAndClino( b, c, 0 ); // orientation 0
      //     } catch ( NumberFormatException e ) { }
      //   }
      //   mShotNewDialog = null;
      //   break;
      case TDRequest.CAPTURE_IMAGE_DRAWWINDOW:
        if ( resCode == Activity.RESULT_OK ) {
          insertPhoto();
        }
        break;
    }
  }


  // ------------------------------------------------------------------
  // SCRAP OUTLINE 

  void scrapOutlineDialog()
  {
    if ( mType != PlotInfo.PLOT_PLAN && mType != PlotInfo.PLOT_EXTENDED ) {
      TDLog.Error( "outline bad scrap type " + mType );
      return;
    }
    String name = ( mType == PlotInfo.PLOT_PLAN )? mPlot1.name : mPlot2.name;
    List< PlotInfo > plots = mApp_mData.selectAllPlotsWithType( TDInstance.sid, TDStatus.NORMAL, mType );
    for ( PlotInfo plot : plots ) {
      if ( plot.name.equals( name ) ) {
        plots.remove( plot );
        break;
      }
    }
    if ( plots.size() == 0 ) {
      TDLog.Error( "outline no other scraps" );
      return;
    }
    if ( mType == PlotInfo.PLOT_PLAN ) {
      new ScrapOutlineDialog( this, this, mApp, plots ).show();
    } else { // ( mType == PlotInfo.PLOT_EXTENDED ) 
      new ScrapOutlineDialog( this, this, mApp, plots ).show();
    }
  }

  void addScrap( PlotInfo plot )
  {
    // Log.v("DistoXC", "addScrap " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    assert( mLastLinePath == null );

    mDrawingSurface.clearScrapOutline();
    if ( mNum == null || plot == null ) {
      // Log.v("DistoX0", "null num or plot");
      return;
    }
    NumStation st  = mNum.getStation( plot.start );
    if ( st == null ) {
      // Log.v("DistoX0", "null plot start station");
      return;
    }
    float xdelta = 0;
    float ydelta = 0;
    NumStation st0;
    if ( mType == PlotInfo.PLOT_PLAN ) {
      mOutlinePlot1 = plot;
      st0 = mNum.getStation( mPlot1.start );
      xdelta = st.e - st0.e; // FIXME SCALE FACTORS ???
      ydelta = st.s - st0.s;
    } else if ( mType == PlotInfo.PLOT_EXTENDED ) {
      mOutlinePlot2 = plot;
      st0 = mNum.getStation( mPlot2.start );
      xdelta = st.h - st0.h;
      ydelta = st.v - st0.v;
    } else {
      return;
    }
    xdelta *= DrawingUtil.SCALE_FIX;
    ydelta *= DrawingUtil.SCALE_FIX;

    String fullName = TDInstance.survey + "-" + plot.name;
    String tdr = TDPath.getTdrFileWithExt( fullName );
    // Log.v("DistoX0", "add outline " + tdr + " delta " + xdelta + " " + ydelta );
    mDrawingSurface.addScrapDataStream( tdr, xdelta, ydelta );
  }

  // ------------------------------------------------------------------
  // SPLIT AND MERGE
  // here we are guaranteed that "name" can be used for a new plot name
  // and the survey has station "station"
  void splitPlot( String name, String station, boolean remove ) 
  {
    // Log.v("DistoXC", "splitPlot " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    mLastLinePath = null; // absolutely necessary

    // Log.v("DistoX-S", "split plot " + name + " station " + station );
    // get the DrawingStation of station
    mSplitName = name;
    // mSplitStation = mDrawingSurface.getStation( station );
    mSplitStationName = station;
    mSplitRemove  = remove;
    // if ( mSplitStation != null ) { // do not check origin station
      if ( mSplitBorder == null ) {
        mSplitBorder = new ArrayList<>();
      } else {
        mSplitBorder.clear();
      }
      mMode = MODE_SPLIT;
      mTouchMode = MODE_MOVE;
      // Log.v("DistoX-S", "*** split mode");
    // } else {
    //   TDToast.makeBad("Missing station " + station );
    // }
    // Log.v("DistoX-S", "mode " + mMode + " touch-mode " + mTouchMode );
  }

  void mergeOutlineScrap()
  {
    // merge is aclled in MOVE mode
    // Log.v("DistoXC", "mergeOutlineScrap " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    assert( mLastLinePath == null );

    if ( mType == PlotInfo.PLOT_PLAN ) {
      if ( mOutlinePlot1 == null ) return;
      mDrawingSurface.clearScrapOutline();
      doMergePlot( mOutlinePlot1 );
      mOutlinePlot1 = null;
    } else if ( mType == PlotInfo.PLOT_EXTENDED ) {
      if ( mOutlinePlot2 == null ) return;
      mDrawingSurface.clearScrapOutline();
      doMergePlot( mOutlinePlot2 );
      mOutlinePlot2 = null;
    }
  }

  // void mergePlot()
  // {
  //   List<PlotInfo> plots = mApp_mData.selectAllPlotsWithType( TDInstance.sid, TDStatus.NORMAL, mType );
  //   if ( plots.size() <= 1 ) { // nothing to merge in
  //     return;
  //   }
  //   for ( PlotInfo plt : plots ) {
  //     if ( plt.name.equals( mName ) ) {
  //       plots.remove( plt );
  //       break;
  //     }
  //   }
  //   new PlotMergeDialog( mActivity, this, plots ).show();
  // }

  // called by mergeOutlineScrap
  // called only with mType PLOT_PLAN or PLOT_EXTENDED
  private void doMergePlot( PlotInfo plt )
  {
    // assert( mLastLinePath == null); // obvious

    if ( plt.type != mType ) return;
    NumStation st1 = mNum.getStation( plt.start );
    NumStation st0 = mNum.mStartStation; // start-station has always coords (0,0)
    if ( st1 == null || st0 == null ) return;

    float xdelta = 0.0f;
    float ydelta = 0.0f;
    if ( mType == PlotInfo.PLOT_PLAN ) {
      xdelta = st1.e - st0.e; // FIXME SCALE FACTORS ???
      ydelta = st1.s - st0.s;
    } else if ( mType == PlotInfo.PLOT_EXTENDED ) {
      xdelta = st1.h - st0.h;
      ydelta = st1.v - st0.v;
    } else {
      return;
    }
    xdelta *= DrawingUtil.SCALE_FIX;
    ydelta *= DrawingUtil.SCALE_FIX;
    String fullName = TDInstance.survey + "-" + plt.name;
    String tdr = TDPath.getTdrFileWithExt( fullName );
    boolean ret = mDrawingSurface.addloadDataStream( tdr, /* null, */ xdelta, ydelta, null, null ); // do not save plot name in paths
  }

  // remove: whether to remove the paths from the current plot
  private void doSplitPlot( )
  {
    if ( mSplitBorder.size() <= 3 ) { // too few points: nothing to split
      TDToast.makeWarn( R.string.split_nothing );
      return;
    }
    List<DrawingPath> paths = mDrawingSurface.splitPlot( mSplitBorder, mSplitRemove );
    if ( paths.size() == 0 ) { // nothing to split
      TDToast.makeWarn( R.string.split_nothing );
      return;
    }
    Log.v("DistoXC", "doSplitPlot " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    mLastLinePath = null;

    boolean extended = (mPlot2.type == PlotInfo.PLOT_EXTENDED);
    int azimuth = (int)mPlot2.azimuth; 
    long pid = mApp.insert2dPlot( TDInstance.sid, mSplitName, mSplitStationName, extended, azimuth );
    String name = mSplitName + ( ( mType == PlotInfo.PLOT_PLAN )? "p" : "s" );
    String fullname = TDInstance.survey + "-" + name;
    // PlotInfo plot = mApp_mData.getPlotInfo( TDInstance.sid, name );
    (new SavePlotFileTask( mActivity, this, null, /* mApp, */ mNum, /* mDrawingUtil, */ paths, fullname, mType, azimuth ) ).execute();
    // TODO
    // [1] create the database record
    // [2] save the Tdr for the new plot and remove the items from the commandManager
  }


  // @param name xsection scrap_name = survey_name + "-" + xsection_id
  //                      tdr_path = tdr_dir + scrap_name + ".tdr"
  boolean hasXSectionOutline( String name ) { return mDrawingSurface.hasXSectionOutline( name ); }

  void setXSectionOutline( String name, boolean on_off, float x, float y )
  { 
    // Log.v("DistoXC", "setXSectionOutline " + ( (mLastLinePath != null)? mLastLinePath.mLineType : "null" ) );
    assert( mLastLinePath == null );

    mDrawingSurface.clearXSectionOutline( name );
    // Log.v("DistoXX", "XSECTION set " + name + " on/off " + on_off + " " + x + " " + y );
    if ( on_off ) {
      String tdr = TDPath.getTdrFileWithExt( name );
      // Log.v("DistoXX", "XSECTION set " + name + " on_off " + on_off + " tdr-file " + tdr );
      mDrawingSurface.setXSectionOutline( name, tdr, x-DrawingUtil.CENTER_X, y-DrawingUtil.CENTER_Y );
    }
  }

  // called by DrawingShotDialog to change shot color
  //   @param blk   data block
  //   @param color color (0 to clear)
  void updateBlockColor( DBlock blk, int color )
  {
    blk.setPaintColor( color );
    mApp_mData.updateShotColor( blk.mId, TDInstance.sid, color, false ); // do not forward color
  }


}
