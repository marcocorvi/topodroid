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
package com.topodroid.TDX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDsafUri;
import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDColor;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDRequest;
import com.topodroid.utils.TDLocale;
import com.topodroid.utils.TDUtil;
import com.topodroid.utils.StringPair;
import com.topodroid.num.TDNum;
import com.topodroid.num.NumStation;
import com.topodroid.num.NumShot;
import com.topodroid.num.NumSplay;
// import com.topodroid.mag.Geodetic;
import com.topodroid.math.TDVector;
import com.topodroid.math.Point2D;
// import com.topodroid.math.BezierCurve;
// import com.topodroid.math.BezierInterpolator;
// import com.topodroid.dln.DLNWall;
// import com.topodroid.dln.DLNSide;
// import com.topodroid.dln.DLNSite;
// import com.topodroid.dln.DLNSideList;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.ui.MyTurnBitmap;
import com.topodroid.ui.ItemButton;
import com.topodroid.ui.MotionEventWrap;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;
import com.topodroid.dev.ConnectionState;
import com.topodroid.dev.DataType;
// import com.topodroid.dev.ConnectionState;
import com.topodroid.common.PlotType;
import com.topodroid.common.ExtendType;
import com.topodroid.common.LegType;
import com.topodroid.common.SymbolType;
import com.topodroid.common.PointScale;
import com.topodroid.io.th.DrawingTh; // TH2EDIT

import java.io.File;
import java.io.FileReader; // TH2EDIT
import java.io.FileWriter; // TH2EDIT
import java.io.FileInputStream; // TH2EDIT
// import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Locale; // TH2EDIT

import java.util.concurrent.RejectedExecutionException;
// import java.util.Deque; // REQUIRES API-9
//
import android.app.Activity;

import android.content.Context;
// import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Configuration;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
// import android.os.AsyncTask;
// /* fixme-23 */
// import java.lang.reflect.Method;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
//
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.PopupWindow;
import android.widget.Button;
import android.widget.Toast;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

// import android.provider.MediaStore;

// import android.print.PrintAttributes;
// import android.print.pdf.PrintedPdfDocument; // API-19
import android.graphics.pdf.PdfDocument;
import android.graphics.pdf.PdfDocument.Page;
import android.graphics.pdf.PdfDocument.PageInfo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Paint;
// import android.graphics.Canvas;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.RectF;
// import android.graphics.Rect;
import android.graphics.Path;

import android.net.Uri;

/**
 */
public class DrawingWindow extends ItemDrawer
                           implements View.OnTouchListener
                                    , View.OnClickListener
                                    , View.OnLongClickListener
                                    , OnItemClickListener
                                    , OnItemSelectedListener
                                    , OnZoomListener
                                    // , ILabelAdder
                                    , ILister
                                    , IZoomer
                                    , IExporter
                                    , IFilterClickHandler
                                    // , IJoinClickHandler
                                    , IPhotoInserter
                                    , IAudioInserter
{
  public static final int ZOOM_TRANSLATION_1 = -50; // was -42
  // public static final int ZOOM_TRANSLATION_3 = -200;
  private static int ZOOM_TRANSLATION   = ZOOM_TRANSLATION_1;
 
  private static final int[] izons_ok = {
                        R.drawable.iz_edit_ok, // 0
                        R.drawable.iz_eraser_ok,
                        R.drawable.iz_select_ok };

  private static final int IC_DOWNLOAD     =  3;
  private static final int IC_BLUETOOTH    =  4;
  // private static final int IC_REFS         =  5;
  private static final int IC_PLAN         =  6;
  // private static final int IC_NOTE         =  7;
  // private static final int IC_AZIMUTH      =  8;
  // private static final int IC_REFRESH      =  9;
  // private static final int IC_UNDO         = 10;
  // private static final int IC_REDO         = 11;
  private static final int IC_TOOLS_LINE   = 12;
  private static final int IC_SPLAYS_LINE  = 13;
  private static final int IC_JOIN_NONE    = 14;  // index of continue-no icon
  private static final int IC_PREV         = 15;
  private static final int IC_NEXT         = 16;
  private static final int IC_DELETE_OFF   = 17;
  private static final int IC_ATTRIBS      = 18;
  // private static final int IC_SNAP         = 19;
  private static final int IC_RANGE_NO     = 20;
  // private static final int IC_SELECT_ALL   = 21;
  private static final int IC_ERASE_ALL    = 22;
  private static final int IC_MEDIUM       = 23;

  private static final int IC_MENU          = IC_MEDIUM+1;
  private static final int IC_EXTEND        = IC_MEDIUM+2;
  // // 2023-03-10 DROPPED 2023-09-24 REINSTATED
  // private static final int IC_SNAP_NO       = IC_MEDIUM+3;
  // private static final int IC_JOIN_START    = IC_MEDIUM+4;     // index of continue icon
  // private static final int IC_JOIN_END      = IC_MEDIUM+5;     // index of continue icon
  private static final int IC_JOIN_BOTH     = IC_MEDIUM+6;     // index of continue icon
  // private static final int IC_JOIN_CONTINUE = IC_MEDIUM+7;     // index of continue icon
  private static final int IC_ADD             = IC_MEDIUM+8;
  private static final int IC_RANGE_OK        = IC_MEDIUM+9;
  private static final int IC_RANGE_BOX       = IC_MEDIUM+10;
  private static final int IC_RANGE_ITEM      = IC_MEDIUM+11;
  private static final int IC_ERASE_POINT     = IC_MEDIUM+12;
  private static final int IC_ERASE_LINE      = IC_MEDIUM+13;
  private static final int IC_ERASE_AREA      = IC_MEDIUM+14;
  private static final int IC_SMALL           = IC_MEDIUM+15;
  private static final int IC_LARGE           = IC_MEDIUM+16;
  private static final int IC_SELECT_ALL      = IC_MEDIUM+17;
  private static final int IC_SELECT_POINT    = IC_MEDIUM+18;
  private static final int IC_SELECT_LINE     = IC_MEDIUM+19;
  private static final int IC_SELECT_AREA     = IC_MEDIUM+20;
  private static final int IC_SELECT_SHOT     = IC_MEDIUM+21;
  private static final int IC_SELECT_STATION  = IC_MEDIUM+22;
  private static final int IC_JOIN_OFF        = IC_MEDIUM+23;
  private static final int IC_DELETE_ON       = IC_MEDIUM+24;
  // private static final int IC_DIAL_ON         = IC_MEDIUM+25; // UNUSED
  private static final int IC_SPLAYS_POINT    = IC_MEDIUM+26;
  private static final int IC_TOOLS_POINT     = IC_MEDIUM+27;
  private static final int IC_TOOLS_AREA      = IC_MEDIUM+28;
  private static final int IC_TOOLS_LINE_TILDE = IC_MEDIUM+29;
  private static final int IC_TOOLS_AREA_TILDE = IC_MEDIUM+30;
  private static final int IC_MENU_RED        = IC_MEDIUM+31;

  private static final int BTN_DOWNLOAD  = 3; // index of mButton1 download button
  private static final int BTN_BLUETOOTH = 4; // index of mButton1 bluetooth button
  private static final int BTN_PLAN      = 6; // index of mButton1 plot button
  private static final int BTN_DIAL      = 8; // index of mButton1 azimuth button (level > normal)
  private static final int BTN_REFRESH   = 9; // index of mButton1 azimuth button (level > normal)

  private static final int BTN_TOOL   = 5;    // index of mButton2 tools
  private static final int BTN_SPLAYS = 6;    // index of mButton2 splays
  private static final int BTN_JOIN2  = 7;    // index of mButton2 join button (level > normal) // TH2EDIT was final

  private static final int BTN_REMOVE = 5;    // index of mButton3 remove
  private static final int BTN_ATTRIB = 6;    // index of mButton3 attributes
  // private static final int BTN_JOIN3  = 7;    // index of mButton3 join button (unused)
  private static final int BTN_BORDER = 8;    // line border-editing (level > advanced)

  private static final int BTN_SELECT_MODE = 3; // select-mode button
  private static final int BTN_SELECT_PREV = 3; // select-mode button
  private static final int BTN_SELECT_NEXT = 4; // select-mode button
  private static final int BTN_ITEM_EDIT   = 6; // select button item-edit properties FIXME same as BTN_ATTRIB

  private static final int BTN_ERASE_MODE = 5; // erase-mode button
  private static final int BTN_ERASE_SIZE = 6; // erase-size button

  private static final int[] izons = {
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,

                        R.drawable.iz_download,      // 3 MOVE Nr 3+6
                        R.drawable.iz_bt,
                        R.drawable.iz_mode,          // 5
                        R.drawable.iz_plan,          // 6
                        R.drawable.iz_note,          // 7
                        R.drawable.iz_dial,          // 8
                        R.drawable.iz_refresh,       // 9

                        R.drawable.iz_undo,          // 10 DRAW Nr 3+5
                        R.drawable.iz_redo,          // 11
                        R.drawable.iz_tools_line,    // 12
                        R.drawable.iz_splays_line,   // 13
                        R.drawable.iz_cont_none,     // 14

                        R.drawable.iz_back,          // 15 EDIT Nr 3+6
                        R.drawable.iz_forw,
                        R.drawable.iz_delete_off,    // 17
                        R.drawable.iz_attrib,          
                        R.drawable.iz_join,
                        R.drawable.iz_range_no,      // 20

                        R.drawable.iz_select_all,    // only for help
                        R.drawable.iz_erase_all,     // 22 ERASE Nr 3+2
                        R.drawable.iz_medium,        // 23

                        R.drawable.iz_menu,          // 23+1
                        R.drawable.iz_extended,      // 23+2
                        -1, // R.drawable.iz_join_no,      // 23+3 // 2023-03-10  no-point-actions (unused)
                        -1, // R.drawable.iz_cont_start,   // 23+4 
                        -1, // R.drawable.iz_cont_end,     // 23+5
                        R.drawable.iz_cont_both,
                        -1, // R.drawable.iz_cont_continue,
                        R.drawable.iz_plus,            // 23+8
                        R.drawable.iz_range_ok,        // 23+9
                        R.drawable.iz_range_box,       // 23+10
                        R.drawable.iz_range_item,      // 23+11
                        R.drawable.iz_erase_point,     // 23+12
                        R.drawable.iz_erase_line,      // 23+13
                        R.drawable.iz_erase_area,      // 23+14
                        R.drawable.iz_small,           // 23+15
                        R.drawable.iz_large,           // 23+16
                        R.drawable.iz_select_all,      // 23+17 all
                        R.drawable.iz_select_point,    // 23+18 point
                        R.drawable.iz_select_line,     // 23+19 line
                        R.drawable.iz_select_area,     // 23+20 area
                        R.drawable.iz_select_shot,     // 23+21 shot
                        R.drawable.iz_select_station,  // 23+22 station
                        R.drawable.iz_cont_off,        // 23+23 continuation off // 2023-03-10
			R.drawable.iz_delete,          // 23+24 do delete
                        R.drawable.iz_dial_on,         // 23+25 set dial
                        R.drawable.iz_splays_point,    // 23+26
                        R.drawable.iz_tools_point,     // 23+27
                        R.drawable.iz_tools_area,      // 23+28
                        R.drawable.iz_tools_line_cont, // 23+28
                        R.drawable.iz_tools_area_cont, // 23+30 // 2023-03-10
                        R.drawable.iz_menu_red,        // 23+31
                      };
  private static final int[] menus = {
                        R.string.menu_switch,     // 0
                        R.string.menu_export,     // 1
                        R.string.menu_stats,      // 2
                        R.string.menu_reload,
                        R.string.menu_zoom_fit,
                        R.string.menu_plot_search,     // 5 SEARCH
                        R.string.menu_rename_delete,
                        R.string.menu_plot_scrap,
                        R.string.menu_palette,    // 8
                        R.string.menu_overview,
                        R.string.menu_options,
                        R.string.menu_help,
                        R.string.menu_area,       // 12
                        R.string.menu_close,      // 13
                        R.string.menu_save,       // 14 TH2EDIT
                        R.string.menu_open        // 15 TH2EDIT
                     };

  private static final int MENU_AREA  = 12;
  private static final int MENU_CLOSE = 13;
  private static final int MENU_SAVE  = 14; // TH2EDIT
  private static final int MENU_OPEN  = 15; // TH2EDIT
/*
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
*/
  private static final int NR_BUTTON1 = 10; // move
  private static final int NR_BUTTON2 =  8; // draw 2023-09-23
  private static final int NR_BUTTON3 =  9; // edit
  private static final int NR_BUTTON5 =  7; // erase

  private static final int[] izons_move = {
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,
                        R.drawable.iz_download,      // 3 MOVE Nr 3+6
                        R.drawable.iz_bt,
                        R.drawable.iz_mode,          // 5
                        R.drawable.iz_plan,          // 6
                        R.drawable.iz_note,          // 7
                        R.drawable.iz_dial,          // 8
                        R.drawable.iz_refresh
  };

  private static final int[] help_icons_move = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_download,
                        R.string.help_remote,
                        R.string.help_refs,
                        R.string.help_toggle_plot,
                        R.string.help_note,
                        R.string.help_azimuth,
                        R.string.help_refresh
                      };


  private static final int[] izons_draw = {
                        R.drawable.iz_edit_ok,          // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,
                        R.drawable.iz_undo,          // 9 DRAW Nr 3+4
                        R.drawable.iz_redo,          // 10
                        R.drawable.iz_tools,         // 11
                        R.drawable.iz_splays_line,
                        R.drawable.iz_cont_none
  };
  private static final int[] help_icons_draw = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_undo,
                        R.string.help_redo,
                        R.string.help_symbol_plot,
                        R.string.help_splays,
                        R.string.help_join
                      };

  private static final int[] izons_edit = {
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select_ok,
                        R.drawable.iz_back,          // 13 EDIT Nr 3+6
                        R.drawable.iz_forw,
                        R.drawable.iz_join,
                        R.drawable.iz_attrib,          
                        R.drawable.iz_delete_off,    // 17
                        R.drawable.iz_range_no,
                        R.drawable.iz_select_all,
                        R.drawable.iz_medium
  };
  private static final int[] help_icons_edit = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_previous,
                        R.string.help_next,
                        R.string.help_line_point, 
                        R.string.help_note_plot,
                        R.string.help_delete_item,
                        R.string.help_range,
                        R.string.help_select_mode,
                        R.string.help_select_size
                      };


  private static final int[] izons_erase = {
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser_ok,
                        R.drawable.iz_select,
                        R.drawable.iz_undo,          // 9 DRAW Nr 3+4
                        R.drawable.iz_redo,          // 10
                        R.drawable.iz_erase_all,
                        R.drawable.iz_medium
  };

  private static final int[] help_icons_erase = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_undo,
                        R.string.help_redo,
                        R.string.help_erase_mode,
                        R.string.help_erase_size
                      };

  private static final int[] help_menus = {
                        R.string.help_plot_switch,
                        R.string.help_save_plot,
                        R.string.help_stats,
                        R.string.help_recover,
                        R.string.help_zoom_fit,
                        R.string.help_plot_search,
                        R.string.help_plot_rename,
                        R.string.help_plot_scrap,
                        R.string.help_symbol,
                        R.string.help_overview,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  private static final int HELP_PAGE = R.string.DrawingWindow;

  private final static int DISMISS_NONE   = 0;
  private final static int DISMISS_EDIT   = 1;
  private final static int DISMISS_FILTER = 2;
  private final static int DISMISS_JOIN   = 3; // 2023-03-10 DROPPED 2023-09-23 REINSTATED
  private final static int DISMISS_BT     = 4;

  private final static boolean COMPUTE_NO  = false;  // do not compute references
  private final static boolean COMPUTE_YES = true;   // compute references
  private final static boolean PARAMS_YES  = true;   // update display params

  private TopoDroidApp mApp;
  private DataHelper   mApp_mData;
  private DataDownloader mDataDownloader;
  private MediaManager   mMediaManager;
  private boolean mTh2Edit = false;  // whether to skip saving at modifications // TH2EDIT
  private float mStylusSizeDpi = 1000;  // stylus size in dpi

  private boolean mLandscape;
  private boolean audioCheck;
  // private DataHelper mData;
  private Activity mActivity = null;
  private int mBTstatus; // status of bluetooth buttons (download and reset)

  // long getSID() { return TDInstance.sid; }
  // String getSurvey() { return TDInstance.survey; }

  private TDNum mNum;
  private float mDecl = 0;
  private String mFormatClosure;  // loop closure report format
  // private int nr_multi_bad;    // number of bad-sibling leg shots - TODO move to TDNum
  private ArrayList< StringPair > mMultiBad;
  private int nr_magnetic_bad; // number of bad-magnetic leg shots

  private String mSectionName;
  private String mMoveTo; // station of highlighted splay

  // private static BezierInterpolator mBezierInterpolator = new BezierInterpolator();
  private DrawingSurface  mDrawingSurface;
  private DrawingLinePath mCurrentLinePath;
  // private DrawingLinePath mLastLinePath = null;
  private DrawingAreaPath mCurrentAreaPath = null;
  // private DrawingPath mFixedDrawingPath;
  // private Paint mCurrentPaint;
  private DrawingBrush mCurrentBrush;
  // private Path  mCurrentPath;
  private DrawingPointPath mSectionPt = null;

  // private static boolean mRecentToolsForward = true;

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

  private float mEraseSize  = TDSetting.mEraseness;
  private float mSelectSize = TDSetting.mSelectness;

  // protected static int mEditRadius = 0; 
  private int mDoEditRange = SelectionRange.RANGE_POINT; // 0 no, 1 smooth, 2 boxed

  private boolean mRotateAzimuth; // whether to rotate azimuth button
  private boolean mPointerDown = false;
  private boolean mEditMove;      // whether moving the selected point
  private boolean mShiftMove;     // whether to move the canvas in point-shift mode

  // TODO SHIFT should go in the DrawingState
  private boolean mShiftDrawing;  // whether to shift the drawing 
  private boolean mScrapOnly;    // whether to shift only the current scrap

  private EraseCommand mEraseCommand = null;

  // used only by the DrawingModeDialog
  void setShiftDrawing( boolean shift_drawing, boolean scrap_only ) 
  { 
    mShiftDrawing = shift_drawing;
    mScrapOnly    = scrap_only;
  }
  boolean isShiftDrawing() { return mShiftDrawing; }
  boolean isShiftScrap()   { return mScrapOnly; }

  private int mHotItemType     = -1;
  private DrawingPath mHotPath = null;
  private boolean mHasSelected = false;
  private boolean hasPointActions  = false;

  // ZOOM
  static final float ZOOM_INC = 1.4f;
  static final float ZOOM_DEC = 1.0f/ZOOM_INC;
  private ZoomButtonsController mZoomBtnsCtrl = null;
  private boolean mZoomBtnsCtrlOn = false;
 
  // FIXED_ZOOM
  // private int mFixedZoom = 0; // 0= variable, 1= 1:100, 2= 1:200
  // private static final float[] gZoom = { 1.0f, 0.10f, 0.20f, 0.30f, 0.40f, 0.50f };

  // FIXME_ZOOM_CTRL ZoomControls mZoomCtrl = null;
  // ZoomButton mZoomOut;
  // ZoomButton mZoomIn;
  private float oldDist;  // zoom pointer-spacing
  private View mZoomView;
  private float mZoomTranslate = 48; // pixels
  private LinearLayout mLayoutTools;
  private LinearLayout mLayoutToolsP;
  private LinearLayout mLayoutToolsL;
  private LinearLayout mLayoutToolsA;
  private LinearLayout mLayoutScale;
  // private ItemButton[] mBtnRecent;
  private ItemButton[] mBtnRecentP;
  private ItemButton[] mBtnRecentL;
  private ItemButton[] mBtnRecentA;
  private SeekBar      mScaleBar;

  // window mode
  // static final int MODE_NONE  = 0; // initial mode
  static final int MODE_DRAW  = 1;
  static final int MODE_MOVE  = 2;
  static final int MODE_EDIT  = 3;
  static final int MODE_ZOOM  = 4; // used only for touchMode
  static final int MODE_SHIFT = 5; // change point symbol position
  static final int MODE_ERASE = 6;
  static final int MODE_ROTATE = 7; // selected point rotate
  static final int MODE_SPLIT_SKETCH = 8;  // split the plot
  static final int MODE_SPLIT_SCRAP  = 9;  // split the scrap

  private int mMode         = MODE_MOVE; // MODE_NONE;
  private int mTouchMode    = MODE_MOVE;

  private class DrawingState
  {
    // private static final int JOIN_SKIP  = -2; // skip button and variable update
    public static final int JOIN_OFF   = -1; // continue off
    public static final int JOIN_NONE  = 0;  // no continue
    // private static final int JOIN_START = 1;  // continue: join to existing line
    // private static final int JOIN_END   = 2;  // continue: join to existing line
    public static final int JOIN_BOTH  = 3;  // continue: join to existing line
    // private static final int JOIN_CONTINUE  = 4;  // continue: continue existing line
    // static final private int CONT_MAX   = 5;

    private int mJoinLine     = JOIN_NONE;
    // private int mJoinSave     = JOIN_NONE;
    // private int mJoinArea     = JOIN_NONE;
    private boolean mRetraceLine    = false;
    private boolean mRetraceArea    = false;
    private boolean mCanRetraceLine = true;
    private boolean mCanRetraceArea = true;
    private boolean mCanJoinLine    = true;

    // void debug( String msg )
    // {
    //   TDLog.v("DS " + msg + " R " + mRetraceLine + "/" + mRetraceArea + " [" + mCanRetraceLine + "] J " + mJoinLine + " [" + mCanJoinLine );
    // }

    void reset()
    {
      mJoinLine = TDLevel.overBasic ? JOIN_NONE : JOIN_OFF; 
      // mJoinArea = JOIN_NONE;
      mRetraceLine = false;
      mRetraceArea = false;
      mCanRetraceLine = true;
      mCanRetraceArea = true;
      mCanJoinLine    = true;
      // debug("reset");
    }

    boolean isJoinLine()  { return mCanJoinLine && mJoinLine > 0; }
    boolean hasJoinLine() { return mCanJoinLine && mJoinLine >= 0; }
    // boolean isJoinLineOff(); { return mJoinLine == JOIN_OFF;  }
    boolean isJoinLineNone() { return mJoinLine == JOIN_NONE; }
    boolean isJoinLineBoth() { return mJoinLine == JOIN_BOTH; }

    void setCanJoinLine( int line )
    {
      if ( line < 0 ) return;
      mCanJoinLine = BrushManager.isLineJoinable( line );
      // if ( mCanJoinLine ) {
      //   // mJoinLine = mJoinSave;
      // } else {
      //   TDLog.v("line " + line + " cannot be joined ");
      // }
    }

    // boolean canJoinLine(){ return mCanJoinLine; }

    // void setJoinLineOff()  { mJoinLine = JOIN_OFF;  }
    // void setJoinLineNone() { mJoinLine = JOIN_NONE; }
    // void setJoinLineCont() { mJoinLine = JOIN_BOTH; }

    void setJoinLine( int join ) 
    { 
      assert( join >= 0 );
      if ( mJoinLine != JOIN_OFF ) {
        // mJoinSave = mJoinLine;
      }
      if ( mCanJoinLine ) {
        mJoinLine = join;
      } else if ( mJoinLine != JOIN_OFF ) {
        mJoinLine = JOIN_OFF;
      }
    }

    // void toggleJoinLine()
    // {
    //   if ( mJoinLine == JOIN_NONE ) {
    //     setJoinLine( JOIN_BOTH );
    //   } else if ( mJoinLine == JOIN_BOTH ) {
    //     setJoinLine( JOIN_NONE );
    //   }
    // }

    void setRetraceLine( boolean retrace ) { mRetraceLine = retrace; }
    void setRetraceArea( boolean retrace ) { mRetraceArea = retrace; }

    boolean isRetraceLine() { return mRetraceLine; }
    boolean isRetraceArea() { return mRetraceArea; }

    void setCanRetraceLine() 
    {
      if ( TDLevel.overTester || ! TDSetting.isLineStyleComplex() ) {
        mCanRetraceLine = ( mJoinLine <= 0 ); // 20250216
      } else {
        mCanRetraceLine = false;
      }
    }
    void setCanRetraceArea()
    {
      mCanRetraceArea = ( TDLevel.overTester || ! TDSetting.isLineStyleComplex() );
    }

    boolean canRetraceLine() { return mCanRetraceLine; }
    boolean canRetraceArea() { return mCanRetraceArea; }

    void switchRetraceLine() { mRetraceLine = ! mRetraceLine; }
    void switchRetraceArea() { mRetraceArea = ! mRetraceArea; }
  };

  private DrawingState mDrawingState = new DrawingState();

  // private float mDownX;
  // private float mDownY;
  private float mSaveX;
  private float mSaveY;
  private float mSave0X;
  private float mSave0Y;
  private float mSave1X;
  private float mSave1Y;
  private float mSave2X;
  private float mSave2Y;
  private float mStartX; // line shift scene start point
  private float mStartY;
  private float mRotateScale = 180 / TopoDroidApp.mDisplayHeight;

  // private boolean mAllSymbols; // whether the library has all the symbols of the plot

  // -------------------------------------------------------------
  // STATUS items

  private String mName;      // current-plot name
  private String mName1;     // first name (PLAN)
  private String mName2;     // second name (EXTENDED/PROJECTED)
  private String mName3;     // third name (SECTION)
  private String mFullName1; // accessible by the SaveThread
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
  private ArrayList< String > mFroms = new ArrayList<>(); // multileg stations
  private ArrayList< String > mTos   = new ArrayList<>();
  private float mAzimuth = 0.0f;
  private float mClino   = 0.0f;
  private float mIntersectionT = -1.0f; // intersection abscissa for leg xsections
  private PointF mOffset  = new PointF( 0f, 0f );
  private PointF mDisplayCenter;
  private float mZoom  = 1.0f;
  private PointF mSavedOffset  = new PointF( 0f, 0f );
  private float mSavedZoom  = 1.0f;

  private boolean mModified; // whether the sketch has been modified 

  // PLOT SPLIT
  private String mSplitName;
  // private DrawingStationName mSplitStation;
  private String mSplitStationName;
  private ArrayList< PointF > mSplitBorder = null;
  private List< DrawingPath > mSplitPaths  = null;
  private boolean mSplitRemove = false;
  private boolean mSplitCreate = false;

  // ----------------------------------------------------------------
  // BUTTONS and MENU

  // private Button mButtonHelp;
  private int mButtonSize;
  private Button[] mButton1; // primary
  private Button[] mButton1x; // primary x-sections
  private Button[] mButton2; // draw
  private Button[] mButton3; // edit
  private Button[] mButton5; // eraser
  private int mNrButton1 = TDLevel.overNormal? NR_BUTTON1 : 8; // main-primary [8: if level <= normal]
  private int mNrButton1x;
  private int mNrButton2 = TDLevel.overNormal? NR_BUTTON2 : 7; // draw
  private int mNrButton3 = TDLevel.overAdvanced ? NR_BUTTON3 : ( TDLevel.overNormal ? 8 : 6); // edit [6 if level <= normal, 8 if level <= advanced]
  private int mNrButton5 = NR_BUTTON5; // erase
  private MyHorizontalButtonView mButtonView1;
  private MyHorizontalButtonView mButtonView1x;
  private MyHorizontalButtonView mButtonView2;
  private MyHorizontalButtonView mButtonView3;
  private MyHorizontalButtonView mButtonView5;

  private BitmapDrawable mBMbluetooth;
  private BitmapDrawable mBMbluetooth_no;
  private BitmapDrawable mBMdownload;
  private BitmapDrawable mBMdownload_on;
  private BitmapDrawable mBMdownload_wait;
  // private BitmapDrawable mBMsnap;
  // private BitmapDrawable mBMjoin_no;
  private BitmapDrawable mBMedit_item = null;
  private BitmapDrawable mBMedit_box  = null;
  private BitmapDrawable mBMedit_ok   = null;
  private BitmapDrawable mBMedit_no   = null;
  private BitmapDrawable mBMplan;
  private BitmapDrawable mBMextend;
  // // 2023-03-10 DROPPED - 2023-09-23 REINSTATED
  private BitmapDrawable mBMcont_none;
  // private BitmapDrawable mBMcont_start;
  // private BitmapDrawable mBMcont_end;
  private BitmapDrawable mBMcont_both;
  // private BitmapDrawable mBMcont_continue;
  private BitmapDrawable mBMcont_off;
  private BitmapDrawable mBMsplays_line;
  private BitmapDrawable mBMsplays_point;
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
  private BitmapDrawable mBMmenured;
  private BitmapDrawable mBMmenublue;
  private BitmapDrawable mBMprev;
  private BitmapDrawable mBMnext;
  private BitmapDrawable mBMselectAll;
  private BitmapDrawable mBMselectPoint;
  private BitmapDrawable mBMselectLine;
  private BitmapDrawable mBMselectArea;
  private BitmapDrawable mBMselectShot;
  private BitmapDrawable mBMselectStation;
  private BitmapDrawable mBMtoolsPoint;
  private BitmapDrawable mBMtoolsLine;
  private BitmapDrawable mBMtoolsLineCont;
  private BitmapDrawable mBMtoolsArea;
  private BitmapDrawable mBMtoolsAreaCont;
  // FIXME_AZIMUTH_DIAL 1,2
  private Bitmap mBMdial;
  private Bitmap mDialOn;
  private MyTurnBitmap mDialBitmap; // use global dial bitmap

  private MyHorizontalListView mListView;
  private ListView   mMenu;
  private Button     mMenuImage;
  private boolean onMenu;

  private int mNrSaveTh2Task = 0; // current number of save tasks

  private void setButton1( int k, BitmapDrawable bm ) { if ( k < mNrButton1 ) TDandroid.setButtonBackground( mButton1[ k ], bm ); }
  private void setButton2( int k, BitmapDrawable bm ) { if ( k < mNrButton2 ) TDandroid.setButtonBackground( mButton2[ k ], bm ); }
  private void setButton3( int k, BitmapDrawable bm ) { if ( k < mNrButton3 ) TDandroid.setButtonBackground( mButton3[ k ], bm ); }
  private void setButton5( int k, BitmapDrawable bm ) { if ( k < mNrButton5 ) TDandroid.setButtonBackground( mButton5[ k ], bm ); }

  /** @return the name (ILister interface)
   */
  public String name() { return "DrawingWindow"; }

  // ----------------------------------------------------------

  /** @return the set of station names
   */
  Set<String> getStationNames() { return mApp_mData.selectAllStations( TDInstance.sid ); }

  /** test if the drawing window is in draw_edit
   * @return ...
   */
  boolean isNotModeEdit() { return mMode != MODE_EDIT; }

  // PLOT NAME(S) ----------------------------------------------------------

  /** @return the active plot name
   */
  String getName() { return (mName != null)? mName : ""; }

  /** @return the plot name (by the active type)
   */
  String getPlotName() 
  {
    if ( PlotType.isAnySection( mType ) ) {
      return mName3;
    } else if ( PlotType.isProfile( mType ) ) {
      return mName2.substring(0, mName2.length()-1);
    } else if ( mType == PlotType.PLOT_PLAN ) { 
      return mName1.substring(0, mName1.length()-1);
    }
    return "";
  }

  /** @return the plot origin (by the active type)
   */
  String getPlotStation()
  {
    if ( PlotType.isProfile( mType ) ) {
      return mPlot2.start;
    } else if ( mType == PlotType.PLOT_PLAN ) { 
      return mPlot1.start;
    }
    return mPlot3.start; // FIXME or should it be null ?
  }

  /** rename a plot
   * @param name   new plot name
   */
  void renamePlot( String name ) 
  {
    if ( TDString.isNullOrEmpty( name ) ) {
      return;
    }
    if ( PlotType.isAnySection( mType ) ) {
      TDLog.e("X-Sections rename not implemented");
    } else if ( PlotType.isProfile( mType ) || mType == PlotType.PLOT_PLAN ) { 
      String name1 = name + "p";
      String name2 = name + "s";
      // TDLog.v( "rename plot to: " + name1 + " " + name2 );
      // check if plot name name2 exists
      if ( mApp_mData.getPlotInfo( TDInstance.sid, name2 ) == null &&
           mApp_mData.getPlotInfo( TDInstance.sid, name1 ) == null ) {
        mApp_mData.updatePlotName( TDInstance.sid, mPid1, name1 );
        mApp_mData.updatePlotName( TDInstance.sid, mPid2, name2 );
        mName1 = name1;
        mName2 = name2;
        mPlot1.name = name1;
        mPlot2.name = name2;
        mName = ( PlotType.isProfile( mType ) )?  mName2 : mName1;
        // rename files
        String fullName1 = TDInstance.survey + "-" + mName1;
        String fullName2 = TDInstance.survey + "-" + mName2;

        TDPath.renamePlotFiles( mFullName1, fullName1 );
        TDPath.renamePlotFiles( mFullName2, fullName2 );

        mFullName1 = fullName1;
        mFullName2 = fullName2;
        // TopoDroidApp.mShotWindow.setRecentPlot( name, mType );
        TDInstance.setRecentPlot( name, mType );
      } else {
        TDToast.makeBad( R.string.plot_duplicate_name );
        // TDLog.v( "plot name already exists");
      }
    }
  }

  /** change the origin of the plot
   * @param station   name of the new origin
   */
  void setPlotOrigin( String station )
  {
    if ( PlotType.isAnySection( mType ) ) return;
    mApp_mData.updatePlotOrigin( TDInstance.sid, mPid1, station );
    mApp_mData.updatePlotOrigin( TDInstance.sid, mPid2, station );
    List< DBlock > list = mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );
    String old_station = mPlot1.start;
    mPlot1.start = station;
    mPlot2.start = station;
    mMultiBad = new ArrayList< StringPair >();
    mNum = new TDNum( list, mPlot1.start, mDecl, mFormatClosure );
    // mNum.setBarrierAndHidden( mPlot1.view, mPlot1.hide ); 20250321
    if ( TDSetting.mLoopClosure == TDSetting.LOOP_SELECTIVE ) setMenuImageRed( mNum.nrInaccurateLoops > 0 );
    mNum.setBarrierAndHidden( mPlot2.view, mPlot2.hide ); // 20250321
    computeReferences( mNum, mPlot2.type, mPlot2.name, mZoom, false );

    mNum.setBarrierAndHidden( mPlot1.view, mPlot1.hide ); // 20250321
    computeReferences( mNum, mPlot1.type, mPlot1.name, mZoom, false );

    NumStation st = mNum.getStation( old_station );
    if ( st != null ) {
      mDrawingSurface.shiftXSections( st );
    // } else {
    //   // TDLog.v("not found old station " + old_station + " new " + station );
    }
  }

  /** @return the active plot type
   */
  long getPlotType()   { return mType; }

  /** @return true if the active plot is extended profile
   */
  boolean isExtendedProfile() { return mType == PlotType.PLOT_EXTENDED; }

  /** @return true if the active plot is an xsection
   */
  boolean isAnySection() { return PlotType.isAnySection( mType ); }

  /** @return true if in landscape presentation
   */
  boolean isLandscape() { return mLandscape; }

  /** @return zoom value
   */
  public float zoom() { return mZoom; }

  // ----------------------------------------------------------------
  private Handler saveHandler = new Handler();

  private final Runnable saveRunnable = new Runnable() {
    @Override 
    public void run() {
      startSaveTdrTask( mType, PlotSave.MODIFIED, TDSetting.mBackupNumber, 1 );
      mModified = false;
    }
  };

  /** mark the plot "modified"
   */
  private void modified()
  {
    if ( mTh2Edit ) return; // TH2EDIT
    if ( ! mModified ) {
      mModified = true;
      saveHandler.postDelayed( saveRunnable, TDSetting.mBackupInterval * 1000L ); // Backup Interval is in seconds
    }
  }

  /** clear the "modified" flag
   */
  private void resetModified()
  {
    mModified = false;
    if ( mTh2Edit ) return; // TH2EDIT
    if ( saveHandler != null && saveRunnable != null ) {
      saveHandler.removeCallbacks( saveRunnable );
    }
  }

  // -------------------------------------------------------------------
  // ZOOM CONTROLS

  /** react to a change of visibility of zoom -controls
   * @param visible  whether controls should become visible
   */
  @Override
  public void onVisibilityChanged(boolean visible)
  {
    if ( mDrawingSurface.isFixedZoom() ) return;
    if ( mZoomBtnsCtrlOn && mZoomBtnsCtrl != null ) {
      mZoomBtnsCtrl.setVisible( visible || ( TDSetting.mZoomCtrl > 1 ) );
    }
  }

  /** react to the user change of zoom
   * @param zoomin   whether to zoom-in (increase the zoom)
   */
  @Override
  public void onZoom( boolean zoomin )
  {
    if ( zoomin ) changeZoom( ZOOM_INC );
    else changeZoom( ZOOM_DEC );
  }

  private void adjustOffset( float old_zoom, float new_zoom ) 
  {
    mOffset.x -= mDisplayCenter.x*(1/old_zoom-1/new_zoom);
    mOffset.y -= mDisplayCenter.y*(1/old_zoom-1/new_zoom);
  }

  /** change the zoom
   * @param f  zoom scale factor (current zoom is multiplied by f)
   */
  private void changeZoom( float f ) 
  {
    // TDLog.v("CHANGE ZOOM fixed-zoom " + mDrawingSurface.getFixedZoom() );
    if ( mDrawingSurface.isFixedZoom() ) return;
    if ( f < 0.05f || f > 4.0f ) return;
    float zoom = mZoom;
    mZoom     *= f;
    // TDLog.v( "zoom " + mZoom );
    // mOffset.x -= mDisplayCenter.x*(1/zoom-1/mZoom);
    // mOffset.y -= mDisplayCenter.y*(1/zoom-1/mZoom);
    adjustOffset( zoom, mZoom );
    // TDLog.v( "change zoom " + mOffset.x + " " + mOffset.y + " " + mZoom );
    mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    // mZoomCtrl.hide();
    // if ( mZoomBtnsCtrlOn ) mZoomBtnsCtrl.setVisible( false );
  }

  /** @return the value of the fixed-zoom (0 = zoom not fixed)
   * @note used by PlotZoomFitDialog
   */
  int getFixedZoom() { return mDrawingSurface.getFixedZoom(); }
  
  /** set zoom fixed
   * @param fixed_zoom    fixed value: 0 = not-fixed, 1 .. 5 fixed-zoom value
   * @note called by PlotZoomFitDialog
   */
  void setFixedZoom( int fixed_zoom )
  {
    fixed_zoom = ( fixed_zoom < 0 )? 0 : ( fixed_zoom > 5 )? 5 : fixed_zoom;
    mDrawingSurface.setFixedZoom( fixed_zoom );
    if ( fixed_zoom > 0 ) {  // compute the zoom value
      float density = TopoDroidApp.getDensity() - TDSetting.mGraphPaperScale;

      float dp1cm = density * fixed_zoom / 2.54f; // dot_per_1cm
      float zoom = 1600 / dp1cm; // 32 = 40 / 1.25

      // TDLog.v("ZOOM set zoom " + mZoom + " -> " + zoom + " density " + density + " dp1cm " + dp1cm + " adjust " + TDSetting.mGraphPaperScale );
      // mOffset.x *= mZoom / zoom;
      // mOffset.y *= mZoom / zoom;
      adjustOffset( mZoom, zoom );
      mZoom = zoom;
      // TDLog.v( "fixed zoom " + mOffset.x + " " + mOffset.y + " " + mZoom );
      mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
      // updateDisplay();
    }
  }

  /** increase zoom
   */
  public void zoomIn()  { changeZoom( ZOOM_INC ); }

  /** decrease zoom
   */
  public void zoomOut() { changeZoom( ZOOM_DEC ); }

  // public void zoomOne() { resetZoom( ); }

  // public void zoomView( )
  // {
  //   // TDLog.Log( TDLog.LOG_PLOT, "zoomView ");
  //   DrawingZoomDialog zoom = new DrawingZoomDialog( mActivity, this );
  //   zoom.show();
  // }

  // -----------------------------------------------------------------
  // SPLAY+LEG REFERENCE

  private void resetFixedPaint( )
  {
    mDrawingSurface.resetFixedPaint( mApp, BrushManager.fixedShotPaint );
  }
  
  /** add an angle for the north declination
   * @param x1    X coord of first point (0)
   * @param y1    Y coord of first point
   * @param x2    X coord of second point (0)
   * @param y2    Y coord of second point
   * @param decl  declination [degrees]
   * @note used by H-Sections for the North line
   */
  private void addFixedSpecial( float x1, float y1, float x2, float y2, float decl ) // float xoff, float yoff )
  {
    DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH, null, -1 );
    dpath.setPathPaint( BrushManager.highlightPaint );
    DrawingUtil.makeDrawingPathWithAngle( dpath, x1, y1, x2, y2, decl ); // xoff, yoff ); // LEG PATH
    mDrawingSurface.setNorthPath( dpath );
    // mLastLinePath = null;
  }

  /** used to add legs and splays
   * @param type    plot type
   * @param blk     data block
   * @param x1,y1   first endpoint
   * @param x2,y2   second endpoint
   * @param cosine  used only for splays: cosine of the angle (splay,leg) or (splay,plane)
   * @param splay   whether the shot is a splay
   * @param selectable whether the shot is selectable
   */
  private DrawingPath addFixedLine( long type, DBlock blk, double x1, double y1, double x2, double y2,
                             float cosine, boolean splay, boolean selectable )
  {
    DrawingPath path = null;
    if ( splay ) { 
      DrawingSplayPath dpath = makeFixedSplay( type, blk, (float)x1, (float)y1, (float)x2, (float)y2, cosine );
      mDrawingSurface.addFixedSplayPath( dpath, selectable );
      path = dpath; // not needed UNUSED
    } else { // DrawingPath
      path = makeFixedLeg( type, blk, (float)x1, (float)y1, (float)x2, (float)y2 );
      mDrawingSurface.addFixedLegPath( path, selectable );
    }
    return path;
  }

  /** used to append legs and splays to the respective list
   * @param type    plot type
   * @param blk     data block
   * @param x1,y1   first endpoint
   * @param x2,y2   second endpoint
   * @param cosine  used only for splays
   * @param splay   whether the shot is a splay
   * @param selectable whether the shot is selectable
   */
  private DrawingPath appendFixedLine( long type, DBlock blk, double x1, double y1, double x2, double y2,
                                float cosine, boolean splay, boolean selectable )
  {
    DrawingPath path = null;
    int typ = PlotType.isPlan( type )? DrawingSurface.DRAWING_PLAN : DrawingSurface.DRAWING_PROFILE;
    if ( splay ) { 
      DrawingSplayPath dpath = makeFixedSplay( type, blk, (float)x1, (float)y1, (float)x2, (float)y2, cosine );
      mDrawingSurface.appendFixedSplayPath( typ, dpath, selectable );
      path = dpath; // not needed UNUSED
    } else { // DrawingPath
      path = makeFixedLeg( type, blk, (float)x1, (float)y1, (float)x2, (float)y2 );
      mDrawingSurface.appendFixedLegPath( typ, path, selectable );
    }
    return path;
  }

  /** @return a drawing splay path
   * @param type    plot type
   * @param blk     data block
   * @param x1,y1   first endpoint
   * @param x2,y2   second endpoint
   * @param cosine  ... (used for dot/dash drawing)
   */
  private DrawingSplayPath makeFixedSplay( long type, DBlock blk, float x1, float y1, float x2, float y2, float cosine )
  {
    DrawingSplayPath dpath = null;
    dpath = new DrawingSplayPath( blk, mDrawingSurface.scrapIndex() );
    dpath.setCosine( cosine ); // save cosine into path
    dpath.setSplayPathPaint( type, blk );
    DrawingUtil.makeDrawingSplayPath( dpath, x1, y1, x2, y2 );
    return dpath;
  }
  
  /** add a string pair
   * @param f   first string
   * @param s   second string
   * @return true if the string pair has been added to the collection
   */
  private boolean addStringPair( String f, String s )
  {
    if ( f == null || f.length() == 0 ) return false;  
    if ( s == null || s.length() == 0 ) return false;  
    for ( StringPair sp : mMultiBad ) {
      if ( sp.equals( f, s ) ) return false;
    }
    mMultiBad.add( new StringPair( f, s ) );
    // ++ nr_multi_bad;
    return true;
  }

  /** create a DrawingPath for a leg
   * @return a drawing (leg) path
   * @param type    plot type
   * @param blk     data block
   * @param x1      first endpoint X coord [world]
   * @param y1      first endpoint Y coord [world]
   * @param x2      second endpoint X coord [world]
   * @param y2      second endpoint Y coord [world]
   */
  private DrawingPath makeFixedLeg( long type, DBlock blk, float x1, float y1, float x2, float y2 )
  {
    DrawingPath dpath = null;
    // TDLog.v("DATA " + "make fixed path " + blk.mId + " <" + blk.mFrom + "-" + blk.mTo + "> commented " + blk.isCommented() + " tampered " + blk.isTampered() );
    dpath = new DrawingPath( DrawingPath.DRAWING_PATH_FIXED, blk, mDrawingSurface.scrapIndex() );
    dpath.setPathPaint( BrushManager.fixedShotPaint );
    if ( blk != null ) {
      if ( blk.isMultiBad() ) {
        addStringPair( blk.mFrom, blk.mTo );
        // dpath.setPathPaint( BrushManager.fixedOrangePaint );
        dpath.setPathPaint( BrushManager.wavyPaint );
      } else if ( TopoDroidApp.mShotWindow != null && TopoDroidApp.mShotWindow.isBlockMagneticBad( blk ) ) {
        ++ nr_magnetic_bad;
        dpath.setPathPaint( BrushManager.fixedRedPaint );
      } else if ( /* TDSetting.mSplayColor && */ blk.isRecent( ) ) { 
        dpath.setPathPaint( BrushManager.fixedBluePaint );
      }
    }
    // DrawingUtil.makeDrawingPath( dpath, x1, y1, x2, y2, xoff, yoff );
    DrawingUtil.makeDrawingPath( dpath, x1, y1, x2, y2 ); // LEG PATH
    return dpath;
  }

  /** used for splays in x-sections
   * the DBlock comes from a query in the DB and it is not the DBlock in the plan/profile
   *     therefore coloring the splays of those blocks does not affect the X-Section splay coloring
   * @param blk    data block
   * @param x1,y1  first endpoint
   * @param x2,y2  second endpoint
   * @param angle  angle between splay and the plane [degrees]
   * @param blue   true for splays at TO station
   */
  private void addFixedSectionSplay( DBlock blk, float x1, float y1, float x2, float y2, float angle,
                                     // float xoff, float yoff, 
                                     boolean blue )
  {
    // TDLog.v("add fixed section splay " + blue );
    DrawingSplayPath dpath = new DrawingSplayPath( blk, mDrawingSurface.scrapIndex() );
    dpath.setCosine( angle ); 
    Paint paint = blk.getPaint();
    if ( paint != null ) {
      dpath.setPathPaint( paint );
    } else if ( blue ) {
      if ( blk.isXSplay() ) {
        dpath.setPathPaint( BrushManager.paintSplayLRUD );    // GREEN
      } else if ( angle > TDSetting.mSectionSplay ) {
        dpath.setPathPaint( BrushManager.paintSplayXVdot );   // MID_BLUE dashed-4  -- -- -- --
      } else if ( angle < -TDSetting.mSectionSplay ) {
        dpath.setPathPaint( BrushManager.paintSplayXVdash );  // MID_BLUE dashed-3  --- --- ---
      } else {
        dpath.setPathPaint( BrushManager.paintSplayXViewed ); // MID_BLUE
      }
    } else {
      if ( blk.isXSplay() ) {
        dpath.setPathPaint( BrushManager.paintSplayLRUD );    // GREEN
      } else if ( angle > TDSetting.mSectionSplay ) {
        dpath.setPathPaint( BrushManager.paintSplayXBdot );   // LIGHT_BLUE dashed-4
      } else if ( angle < -TDSetting.mSectionSplay ) {
        dpath.setPathPaint( BrushManager.paintSplayXBdash );  // LIGHT_BLUE dashed-3
      } else {
        dpath.setPathPaint( BrushManager.paintSplayXB );      // LIGHT_BLUE
      }
    }
    // dpath.setPathPaint( blue? BrushManager.paintSplayXViewed : BrushManager.paintSplayXB );
    // DrawingUtil.makeDrawingPath( dpath, x1, y1, x2, y2, xoff, yoff );
    DrawingUtil.makeDrawingSplayPath( dpath, x1, y1, x2, y2 );
    mDrawingSurface.addFixedSplayPath( dpath, false ); // false SELECTABLE
  }

  // --------------------------------------------------------------------------------------
  // final static String titleLandscape = " L ";
  // final static String titlePortrait  = " P ";

  /** @return true if the index refers to the point "picture" 
   * @param index   point index (in the symbol library)
   * @note picture point is always forbidden
   */
  @Override
  public boolean forbidPointPicture( int index )
  {
    // return PlotType.isAnySection( mType ) && BrushManager.isPointSection( index );
    return BrushManager.isPointPicture( index );
  }

  /** @return true if the index refers to the point "section" and the plot is a xsection
   * @param index   point index (in the symbol library)
   * @note section point is always forbidden
   */
  @Override
  public boolean forbidPointSection( int index )
  {
    // return PlotType.isAnySection( mType ) && BrushManager.isPointSection( index );
    return BrushManager.isPointSection( index );
  }

  /** @return true if the index refers to the line "section" and the plot is a xsection
   * @param index   line index (in the symbol library)
   */
  @Override
  public boolean forbidLineSection( int index )
  {
    return PlotType.isAnySection( mType ) && BrushManager.isLineSection( index );
  }

  /** select a point symbol
   * @param k       index of selected point-tool in the symbol-library array
   * @param update_recent ...
   */
  @Override
  public void pointSelected( int k, boolean update_recent )
  {
    if ( PlotType.isAnySection( mType ) && BrushManager.isPointSection( k ) ) {
      TDToast.makeToast( R.string.error_no_section_in_section );
      return;
    }
    super.pointSelected( k, update_recent );
    setButtonJoin();
    setButtonTool();
  }

  /** select a line symbol
   * @param k       index of selected line-tool in the symbol-library array
   * @param update_recent ...
   */
  @Override
  public void lineSelected( int k, boolean update_recent )
  {
    if ( forbidLineSection( k ) ) {
      TDToast.makeToast( R.string.error_no_section_in_section );
      return;
    }
    super.lineSelected( k, update_recent );
    if ( ! TDLevel.overNormal ) return; // 2023-03-10 DROPPED 2023-09-23 REINSTATED
    mDrawingState.setCanJoinLine( mCurrentLine );
    // mDrawingState.setJoinLine( mDrawingState.getJoinLine() );
    setButtonJoin();
    setButtonTool();
    // mDrawingState.debug("line select");
  }

  /** select a area symbol
   * @param k       index of selected area-tool in the symbol-library array
   * @param update_recent ...
   */
  @Override
  public void areaSelected( int k, boolean update_recent )
  {
    super.areaSelected( k, update_recent );
    setButtonJoin();
    setButtonTool();
    // mDrawingState.debug("area select");
  }

  /** called by Drawing Shot Dialog to change shot color
   *   @param blk   data block
   *   @param color color (0 to clear)
   */
  void updateBlockColor( DBlock blk, int color )
  {
    blk.setPaintColor( color );
    mApp_mData.updateShotColor( blk.mId, TDInstance.sid, color );
  }

  /** react to a change in the configuration
   * @param new_cfg   new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration new_cfg )
  {
    super.onConfigurationChanged( new_cfg );
    // TDLog.v( "PLOT config changed " + mOffset.x + " " + mOffset.y + " " + mZoom + " orientation " + new_cfg.orientation );
    TDLocale.resetTheLocale();
    mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    // setMenuAdapter( getResources(), mType );
  }

  /** set the title of the window
   */
  @Override
  public void setTheTitle()
  {
    StringBuilder sb = new StringBuilder();
    sb.append(mName);
    sb.append(": ");
    
    if ( TDSetting.isConnectionModeMulti() /* || TDSetting.isConnectionModeDouble() */ ) {
      sb.append( "{" );
      if ( TDInstance.getDeviceA() != null ) sb.append( TDInstance.getDeviceA().getNickname() );
      sb.append( "} " );
    }
    // sb.append( mApp.getConnectionStateTitleStr() ); // IF_COSURVEY
    // sb.append( mLandscape ? titleLandscape : titlePortrait );
    // sb.append(" ");
 
    Resources res = getResources();
    if ( mMode == MODE_DRAW ) { 
      if ( mSymbol == SymbolType.POINT ) {
        sb.append( String.format( res.getString(R.string.title_draw_point), BrushManager.getPointName( mCurrentPoint ) ) );
      } else if ( mSymbol == SymbolType.LINE ) {
        sb.append( String.format( res.getString(R.string.title_draw_line), BrushManager.getLineName( mCurrentLine ) ) );
      } else  {  // if ( mSymbol == SymbolType.AREA ) 
        sb.append( String.format( res.getString(R.string.title_draw_area), BrushManager.getAreaName( mCurrentArea ) ) );
      }
      // 2023-03-10 
      // boolean visible = ( mSymbol == SymbolType.LINE && mCurrentLine == BrushManager.getLineWallIndex() );
      // boolean visible = ( mSymbol == SymbolType.LINE || mSymbol == SymbolType.AREA );
      // if ( TDLevel.overAdvanced && BTN_JOIN2 < mNrButton2 ) {
      //   mButton2[ BTN_JOIN2 ].setVisibility( visible? View.VISIBLE : View.GONE );
      // }
    } else if ( mMode == MODE_MOVE ) {
      sb.append( res.getString( R.string.title_move ) );
    } else if ( mMode == MODE_EDIT ) {
      sb.append( res.getString( R.string.title_edit ) );
    } else if ( mMode == MODE_SHIFT ) {
      sb.append( res.getString( R.string.title_shift ) );
    } else if ( mMode == MODE_ERASE ) {
      sb.append( res.getString( R.string.title_erase ) );
    } else if ( mMode == MODE_SPLIT_SKETCH ) {
      sb.append( res.getString( R.string.title_split ) );
    }
    // if ( ! mDrawingSurface.isSelectable() ) {
    //   sb.append( mActivity.getTitle() + " [!s]" );
    // }
    if ( TDSetting.WITH_IMMUTABLE && ! TDInstance.isSurveyMutable ) { // IMMUTABLE
      mActivity.setTitleColor( 0xffff3333 );
    }
    mActivity.setTitle( sb.toString() );
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

  void doClose()
  {
    // TDLog.v( "menu close ...");
    super.onBackPressed();
  }

  // @note doSaveTdr( ) is already called by onPause
  @Override
  public void onBackPressed () // askClose
  {
    if ( dismissPopups() != DISMISS_NONE ) return;
    if ( mTh2Edit ) { // TH2EDIT
      super.onBackPressed();
    } else {
      if ( PlotType.isAnySection( mType ) ) {
        // Modified = true; // force saving
        startSaveTdrTask( mType, PlotSave.SAVE, TDSetting.mBackupNumber+2, TDPath.NR_BACKUP );
        popInfo();
        doStart( false, -1, null );
        // FIXME_POP-INFO recomputeReferences( mNum, mZoom );
        // if ( TDSetting.mFixmeXSection && mSectionPt != null ) {
          // TDLog.v("setXSectionOutline name " + mName3 + " full " + mFullName3 + " at " + mSectionPt.cx + " " + mSectionPt.cy );
          setXSectionOutline( mFullName3, mSectionPt.mScrap, true, mSectionPt.cx, mSectionPt.cy );
          mSectionPt = null; 
        // }
      } else {
        if ( TDSetting.mSingleBack ) {
          super.onBackPressed();
        } else if ( doubleBack ) {
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
  }

  void switchExistingPlot( String plot_name, long plot_type ) // context of current SID
  {
    // mLastLinePath = null;
    doSaveTdr( );
  }

  /** start the task to save the TDR file
   * @note called by doPause 
   */
  private void doSaveTdr( )
  {
    if ( mDrawingSurface != null ) {
      // TDLog.v( "do save TDR type " + mType );
      // Modified = true; // force saving: Modified is checked before spawning the saving task
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

  /** get the plot origin station
   * @return the name of the plot origin station 
   */
  String getOrigin() { return mPlot1.start; }

  /** @return the plot save data for the specified plot type
   * @param tt        plot type
   * @param save_mode plot save mode
   * @param rotate    number of backup rotates
   * @note called by SavePlotFileTask
   */
  PlotSaveData makePlotSaveData( int tt, int save_mode, int rotate )
  {
    if ( tt == 1 && mPlot1 != null )
      return new PlotSaveData( mNum, mPlot1, mDrawingSurface.getManager( mPlot1.type ), mName1, mFullName1, 0, save_mode, rotate );
    if ( tt == 2 && mPlot2 != null )
      return new PlotSaveData( mNum, mPlot2, mDrawingSurface.getManager( mPlot2.type ), mName2, mFullName2, (int)mPlot2.azimuth, save_mode, rotate );
    if ( tt == 3 && mPlot3 != null )
      return new PlotSaveData( mNum, mPlot3, mDrawingSurface.getManager( mPlot3.type ), mName3, mFullName3, 0, save_mode, rotate );
    return null;
  }

  /** prepare struct and forwards to doStartSaveTdrTask
   * @param type      plot type (-1 to save both plan and profile)
   * @param save_mode plot save mode (see PlotSave) - called only with TOGGLE, SAVE, MODIFIED
   * @param maxTasks  max number of tasks (?)
   * @param rotate    backup_rotate
   * @note called by doSaveTdr and doSaveTh2
   */
  private void startSaveTdrTask( final long type, int save_mode, int maxTasks, int rotate )
  {
    if ( TDSetting.WITH_IMMUTABLE ) {
      // if ( ! mApp_mData.isSurveyMutable( TDInstance.sid, "startSaveTdrTask" ) ) 
      if ( ! TDInstance.isSurveyMutable ) { 
        // TDToast.makeWarn("IMMUTABLE SURVEY plot not saved");
        TDLog.v("IMMUTABLE SURVEY plot not saved");
        return;
      }
    }
    if ( ( save_mode == PlotSave.TOGGLE || save_mode == PlotSave.MODIFIED ) && ! mModified ) {
      // TDLog.v("SAVE TDR: save_mode toggle or modified, but not modified ");
      return;
    }
    // TDLog.v( "start save TDR task - save_mode " + save_mode + " modified " + mModified );
    PlotSaveData psd1 = null;
    PlotSaveData psd2 = null;
    if ( type == -1 ) {
      psd2 = makePlotSaveData( 2, save_mode, rotate );
      psd1 = makePlotSaveData( 1, save_mode, rotate );
    } else if ( PlotType.isProfile( type ) ) {
      psd1 = makePlotSaveData( 2, save_mode, rotate );
    } else if ( type == PlotType.PLOT_PLAN ) {
      psd1 = makePlotSaveData( 1, save_mode, rotate );
    } else {
      psd1 = makePlotSaveData( 3, save_mode, rotate );
    }
    doStartSaveTdrTask( psd1, psd2, save_mode, maxTasks, rotate );
  }

  /** start the TDR saving task
   * @param psd1      plan plot save data
   * @param psd2      profile plot save data
   * @param save_mode plot save mode (see PlotSave) - called only with TOGGLE, SAVE, MODIFIED
   * @param maxTasks  max number of tasks (?)
   * @param rotate    backup_rotate
   */
  private void doStartSaveTdrTask( final PlotSaveData psd1, final PlotSaveData psd2, int save_mode, int maxTasks, int rotate )
  {
    if ( psd1 == null ) return;
    int r = ( rotate == 0 )? 0 : psd1.rotate;
    Handler saveHandler = null;

    switch ( save_mode ) {
      case PlotSave.EXPORT:
        // TDLog.v( "EXPORT plot ... " + maxTasks );
        saveHandler = new Handler(){
          @Override
          public void handleMessage(Message msg) {
            // TopoDroidApp.mShotWindow.enableSketchButton( true );
            TopoDroidApp.mEnableZip = true;
          }
        };
        break;
      case PlotSave.SAVE:
        // TDLog.v( "SAVE plot ... " + maxTasks );
        saveHandler = new Handler(){
          @Override
          public void handleMessage(Message msg) {
            // TopoDroidApp.mShotWindow.enableSketchButton( true );
            TopoDroidApp.mEnableZip = true;
          }
        };
        break;
      case PlotSave.TOGGLE:
      case PlotSave.MODIFIED:
        // TDLog.v( "BACKUP plot ... " + maxTasks + " nr tasks " + mNrSaveTh2Task + " modified " + mModified );
        if ( ! mModified ) return;
        if ( mNrSaveTh2Task > maxTasks ) return;
        saveHandler = new Handler() {
          @Override
          public void handleMessage(Message msg) {
            -- mNrSaveTh2Task;
            if ( mModified ) {
              doStartSaveTdrTask( psd1, psd2, PlotSave.HANDLER, TDSetting.mBackupNumber, 0 ); 
            } else {
              // TopoDroidApp.mShotWindow.enableSketchButton( true );
              TopoDroidApp.mEnableZip = true;
            }
          }
        };
        ++ mNrSaveTh2Task;

        // TopoDroidApp.mShotWindow.enableSketchButton( false );
        TopoDroidApp.mEnableZip = false;
    }
    resetModified();
    // TDUtil.slowDown( 10 );

    // TDLog.v( "saving plot(s) ... ");
    if ( psd2 != null ) {
      // TDLog.Log( TDLog.LOG_IO, "save plot [2] " + psd2.fname );
      try { 
        (new SavePlotFileTask( mActivity, null, this, null, psd2.num, /* psd2.util, */ psd2.cm, psd2.plot, psd2.filename, psd2.type, psd2.azimuth, psd2.clino, psd2.suffix, r, mTh2Edit )).execute(); // TH2EDIT
      } catch ( RejectedExecutionException e ) { 
        TDLog.e("rejected exec save plot " + psd2.filename );
      }
    }
    try { 
      // TDLog.Log( TDLog.LOG_IO, "save plot [1] " + psd1.fname );
      (new SavePlotFileTask( mActivity, null, this, saveHandler, psd1.num, /* psd1.util, */ psd1.cm, psd1.plot, psd1.filename, psd1.type, psd1.azimuth, psd1.clino, psd1.suffix, r, mTh2Edit )).execute(); // TH2EDIT
    } catch ( RejectedExecutionException e ) { 
      TDLog.e("rejected exec save plot " + psd1.filename );
      -- mNrSaveTh2Task;
    }
  }

  // ---------------------------------------------------------------------------------------

  /** execute a "move to" on both plan and profile view
   * @note called by loadFile
   */
  private void doMoveTo()
  {
    if ( mMoveTo != null ) {
      moveTo( mPlot1.type, mMoveTo, true );
      moveTo( mPlot2.type, mMoveTo, true );
      mMoveTo = null;
    }
  }

  /** move to a station
   * @param type    plot type
   * @param move_to station name
   * @param set_zoom whether to set zoom
   * @note called by doMoveTo and centerAtStation
   */
  private void moveTo( int type, String move_to, boolean set_zoom )
  {
    // if ( move_to == null ) return; // move_to guaranteed non-null
    if ( mNum == null ) return; // WHY ??? unexpected crash report
    NumStation st = mNum.getStation( move_to );
    if ( st != null ) {
      if ( type == PlotType.PLOT_PLAN ) {
        if ( set_zoom) mZoom = mPlot1.zoom;
        mOffset.x = TopoDroidApp.mDisplayWidth/(2 * mZoom)  - DrawingUtil.toSceneX( st.e, st.s );
        mOffset.y = TopoDroidApp.mDisplayHeight/(2 * mZoom) - DrawingUtil.toSceneY( st.e, st.s );
        saveReference( mPlot1, mPid1 );
        // resetReference( mPlot1 );
        // mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
        // return;
        // TDLog.v( "PLAN offset at " + mOffset.x + " " + mOffset.y );
      } else if ( type == PlotType.PLOT_EXTENDED ) {
        if ( set_zoom ) mZoom = mPlot2.zoom;
        mOffset.x = TopoDroidApp.mDisplayWidth/(2 * mZoom)  - DrawingUtil.toSceneX( st.h, st.v );
        mOffset.y = TopoDroidApp.mDisplayHeight/(2 * mZoom) - DrawingUtil.toSceneY( st.h, st.v );
        saveReference( mPlot2, mPid2 );
        // resetReference( mPlot2 );
        // return;
        // TDLog.v( "EXT offset at " + mOffset.x + " " + mOffset.y );
      } else { // if ( type == PlotType.PLOT_PROJECTED ) 
        // double cosp = TDMath.cosDd( mPlot2.azimuth );
        // double sinp = TDMath.sinDd( mPlot2.azimuth );
        // OBLIQUE
        float sina = TDMath.sind( mPlot2.azimuth );
        float cosa = TDMath.cosd( mPlot2.azimuth ); // N~ = ( cosp, sinp )
        float sinb = TDMath.sind( mPlot2.azimuth + mPlot2.clino ); // P  = ( sinb, cosb )
        float cosb = TDMath.cosd( mPlot2.azimuth + mPlot2.clino ); 
        float gamma = ( - sinb * cosa + cosb * sina ) / ( sinb * sina + cosb * cosa );
        float cosp = cosa + gamma * sina;
        float sinp = sina - gamma * cosa;

        if ( set_zoom ) mZoom = mPlot2.zoom;
	double xx = st.e * cosp + st.s * sinp;
        mOffset.x = TopoDroidApp.mDisplayWidth/(2 * mZoom)  - DrawingUtil.toSceneX( xx, st.v );
        mOffset.y = TopoDroidApp.mDisplayHeight/(2 * mZoom) - DrawingUtil.toSceneY( xx, st.v );
        saveReference( mPlot2, mPid2 );
        // return;
        // TDLog.v( "PROJ offset at " + mOffset.x + " " + mOffset.y );
      }
      highlightStation( move_to );
    }
  }

  /** compute the plot references
   * @param num     data reduction
   * @param type    plot type
   * @param name    plot name
   * @param zoom    zoom factor
   // * @param can_toast whether the method can toast
   * @note this is called only for PLAN / PROFILE
   */
  private boolean computeReferences( TDNum num, int type, String name,
                                  // float xoff, float yoff,
                                  float zoom, boolean can_toast )
  {
    // TDLog.v( "PLOT compute references() zoom " + zoom + " landscape " + mLandscape );
    if ( ! PlotType.isSketch2D( type ) ) return false;
    if ( num == null ) return false;
    // mLastLinePath = null;
    // float xoff = 0; float yoff = 0;
    float cosp = 0;
    float sinp = 0;

    float e1=-50, e2=50, s1=-50, s2=50;
    int manager_type = DrawingSurface.DRAWING_PLAN;
    if ( type == PlotType.PLOT_PLAN ) {
      manager_type = DrawingSurface.DRAWING_PLAN; // already assigned
      e1 = num.surveyEmin();
      e2 = num.surveyEmax();
      s1 = num.surveySmin();
      s2 = num.surveySmax();
    } else {
      manager_type = DrawingSurface.DRAWING_PROFILE;
      e1 = num.surveyHmin();
      e2 = num.surveyHmax();
      s1 = num.surveyVmin();
      s2 = num.surveyVmax();
    }

    mDrawingSurface.newReferences( manager_type, type );
    // mDrawingSurface.clearReferences( type );
    // mDrawingSurface.setManager( manager_type, type );

    DrawingUtil.addGrid( e1, e2, s1, s2, mDrawingSurface );
    float decl = ( type == PlotType.PLOT_PLAN )? mApp_mData.getSurveyDeclination( mSid ) : 0;
    mDrawingSurface.addScaleRef( manager_type, type, decl );

    if ( type == PlotType.PLOT_PROJECTED ) {
      // cosp = TDMath.cosd( mPlot2.azimuth );
      // sinp = TDMath.sind( mPlot2.azimuth );
      // OBLIQUE
      float sina = TDMath.sind( mPlot2.azimuth );
      float cosa = TDMath.cosd( mPlot2.azimuth ); // N~ = ( cosp, sinp )
      float sinb = TDMath.sind( mPlot2.azimuth + mPlot2.clino ); // P  = ( sinb, cosb )
      float cosb = TDMath.cosd( mPlot2.azimuth + mPlot2.clino ); 
      float gamma = ( - sinb * cosa + cosb * sina ) / ( sinb * sina + cosb * cosa );
      cosp = cosa + gamma * sina;
      sinp = sina - gamma * cosa;
    }

    List< NumStation > stations = num.getStations();
    List< NumShot > shots       = num.getShots();
    List< NumSplay > splays     = num.getSplays();

    String parent = ( TDInstance.xsections? null : name );

    mMultiBad.clear(); // nr_multi_bad = 0;
    nr_magnetic_bad = 0;
    if ( PlotType.isPlan( type ) ) { // -------------- PLAN VIEW ------------------------------
      for ( NumShot sh : shots ) {
        DBlock blk = sh.getFirstBlock();
        // if ( ! blk.isCommented() ) { // FIXME_COMMENTED
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
          if ( st1.show() && st2.show() ) {
            DrawingPath path = addFixedLine( type, blk, st1.e, st1.s, st2.e, st2.s, sh.getReducedExtend(), false, true );
            if ( sh.isBadLoop() ) {
              path.setPathPaint( BrushManager.badLoopPaint );
            }
          }
        // }
      }
      for ( NumSplay sp : splays ) {
        if ( Math.abs( sp.getBlock().mClino ) < TDSetting.mSplayVertThrs ) { // include only splays with clino below mSplayVertThrs
          NumStation st = sp.from;
          if ( st.show() ) {
            DBlock blk = sp.getBlock();
            if ( ! ( blk.isNoPlan() /* || blk.isCommented() */ ) ) { // FIXME_COMMENTED
              // TDLog.v("SPLAY cosine " + sp.getCosine() );
              addFixedLine( type, blk, st.e, st.s, sp.e, sp.s, sp.getCosine(), true, true );
            }
          }
        }
      }
      // N.B. this is where TDInstance.xsections is necessary: to decide which xsections to check for stations
      //      could use PlotType.isStationSectionPrivate and PlotInfo.getXSectionParent
      List< PlotInfo > xsections = mApp_mData.selectAllPlotSectionsWithType( TDInstance.sid, 0, PlotType.PLOT_X_SECTION, parent );
      List< StationInfo > saved = TDSetting.mSavedStations ? mApp_mData.getStations( TDInstance.sid ) : null;
      for ( NumStation st : stations ) {
        if ( st.show() ) {
          // DrawingStationName dst =
          mDrawingSurface.addDrawingStationName( name, st,
                  DrawingUtil.toSceneX(st.e, st.s), DrawingUtil.toSceneY(st.e, st.s), true, xsections, saved );
        }
      }
    }
    else if ( type == PlotType.PLOT_EXTENDED ) // ------------- EXTENDED PROFILE -----------------
    {
      for ( NumShot sh : shots ) {
        if  ( ! sh.mIgnoreExtend ) {
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
	  DBlock blk = sh.getFirstBlock();
          if ( blk != null /* && ! blk.isCommented() */ ) { // FIXME_COMMENTED
            if ( st1.hasExtend() && st2.hasExtend() && st1.show() && st2.show() ) {
              DrawingPath path = addFixedLine( type, blk, st1.h, st1.v, st2.h, st2.v, sh.getReducedExtend(), false, true );
              if ( sh.isBadLoop() ) {
                path.setPathPaint( BrushManager.badLoopPaint );
              }
            }
          }
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        if ( st.hasExtend() && st.show() ) {
          DBlock blk = sp.getBlock();
          if ( ! ( blk.isNoProfile() /* || blk.isCommented() */ ) ) { // FIXME_COMMENTED
            addFixedLine( type, blk, st.h, st.v, sp.h, sp.v, sp.getCosine(), true, true );
          }
        }
      }
      List< PlotInfo > xhsections = mApp_mData.selectAllPlotSectionsWithType( TDInstance.sid, 0, PlotType.PLOT_XH_SECTION, parent );
      List< StationInfo > saved = TDSetting.mSavedStations ? mApp_mData.getStations( TDInstance.sid ) : null;
      for ( NumStation st : stations ) {
        // TDLog.v("EXTEND station " + st.name + " has extend " + st.hasExtend() );
        if ( st.hasExtend() && st.show() ) {
          // DrawingStationName dst =
          mDrawingSurface.addDrawingStationName( name, st, DrawingUtil.toSceneX(st.h, st.v), DrawingUtil.toSceneY(st.h, st.v), true, xhsections, saved );
        }
      }
    } else { // if ( type == PlotType.PLOT_PROJECTED ) // ------------- PROJECTED PROFILE ---------------
      double h1, h2;
      for ( NumShot sh : shots ) {
        // TDLog.v( "shot " + sh.from.name + "-" + sh.to.name + " from " + sh.from.show() + " to " + sh.to.show() );
        NumStation st1 = sh.from;
        NumStation st2 = sh.to;
        if ( st1.show() && st2.show() ) {
          h1 = st1.e * cosp + st1.s * sinp;
          h2 = st2.e * cosp + st2.s * sinp;
          DBlock blk = sh.getFirstBlock();
          // if ( ! blk.isCommented() ) { // FIXME_COMMENTED
            DrawingPath path = addFixedLine( type, blk, h1, st1.v, h2, st2.v, sh.getReducedExtend(), false, true );
            if ( sh.isBadLoop() ) {
              path.setPathPaint( BrushManager.badLoopPaint );
            }
          // }
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        if ( st.show() ) {
          DBlock blk = sp.getBlock();
          if ( ! ( blk.isNoProfile() /* || blk.isCommented() */ ) ) { // FIXME_COMMENTED
            h1 = st.e * cosp + st.s * sinp;
            h2 = sp.e * cosp + sp.s * sinp;
            // cosine of the angle between the splay and the direction of projection
            float cosine = TDMath.sind( blk.mBearing ) * sinp + TDMath.cosd( blk.mBearing ) * cosp; // instead of sp.getCosine()
            // TDLog.v("splay " + blk.mBearing + " cosine " + cosine + " " + cosp + " " + sinp );
            addFixedLine( type, blk, h1, st.v, h2, sp.v, cosine, true, true );
          }
        }
      }
      List< PlotInfo > xhsections = mApp_mData.selectAllPlotSectionsWithType( TDInstance.sid, 0, PlotType.PLOT_XH_SECTION, parent );
      List< StationInfo > saved = TDSetting.mSavedStations ? mApp_mData.getStations( TDInstance.sid ) : null;
      for ( NumStation st : stations ) {
        if ( st.show() ) {
          h1 = st.e * cosp + st.s * sinp;
          // DrawingStationName dst =
          mDrawingSurface.addDrawingStationName( name, st, DrawingUtil.toSceneX(h1, st.v), DrawingUtil.toSceneY(h1, st.v), true, xhsections, saved );
        // } else {
        //   // TDLog.v("PLOT station not showing " + st.name );
        }
      }
    }

    mDrawingSurface.commitReferences();

    if ( can_toast ) {
      setMenuImageRed( ! num.surveyAttached || ! num.surveyExtend || mMultiBad.size() > 0 /* || nr_magnetic_bad > 0 */ ); // nr_multi_bad > 0
    }

    // if ( can_toast ) {
    //   if ( (! num.surveyAttached) && TDSetting.mCheckAttached ) {
    //     if ( (! num.surveyExtend) && TDSetting.mCheckExtend && type == PlotType.PLOT_EXTENDED ) {
    //       TDToast.makeWarn( R.string.survey_not_attached_extend );
    //     } else {
    //       TDToast.makeWarn( R.string.survey_not_attached );
    //     }
    //   } else if ( (! num.surveyExtend) && TDSetting.mCheckExtend && type == PlotType.PLOT_EXTENDED ) {
    //     TDToast.makeWarn( R.string.survey_not_extend );
    //   } else if ( mMultiBad.size() > 0 ) { //  ( nr_multi_bad > 0 )
    //     TDToast.makeWarn( R.string.survey_bad_siblings );
    //   // } else if ( nr_magnetic_bad > 0 ) {
    //   //   TDToast.makeWarn( R.string.survey_bad_magnetic );
    //   }
    // }
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
    if ( ! TDLevel.overNormal ) return;
    if ( BTN_DIAL < mNrButton1 ) {
      if ( TDAzimuth.mFixedExtend == 0 ) {
        // FIXME_AZIMUTH_DIAL 2
        // android.graphics.Matrix m = new android.graphics.Matrix();
        // m.postRotate( azimuth - 90 );
        // Bitmap bm1 = Bitmap.createScaledBitmap( mBMdial, mButtonSize, mButtonSize, true );
        // Bitmap bm2 = Bitmap.createBitmap( bm1, 0, 0, mButtonSize, mButtonSize, m, true);
        // FIXME_AZIMUTH_DIAL 1
        Bitmap bm2 = mDialBitmap.getBitmap( TDAzimuth.mRefAzimuth, mButtonSize );
        setButton1( BTN_DIAL, new BitmapDrawable( getResources(), bm2 ) );
      } else if ( TDAzimuth.mFixedExtend == -1L ) {
        setButton1( BTN_DIAL, mBMleft );
      } else {
        setButton1( BTN_DIAL, mBMright );
      } 
    }
  }

  /** set the button3 by the type of the hot-item
   * @param pt   hot item
   * @note this is the only point where mHotItemType is set
   */
  private void setButton3Item( SelectionPoint pt )
  {
    boolean deletable = false;
    hasPointActions  = false;
    // BitmapDrawable bm = mBMjoin_no;
    String title = getResources().getString( R.string.title_edit );
    setScaleToolbar( null );
    if ( pt != null ) {
      mHotItemType = pt.type();
      mHotPath     = pt.mItem;
      // TDLog.v("set button 3 item - type " + mHotItemType );
      // DrawingPath item = pt.mItem;
      switch ( mHotItemType ) {
        case DrawingPath.DRAWING_PATH_FIXED:
          mActivity.setTitle( title + " " + mHotPath.mBlock.mFrom + "=" + mHotPath.mBlock.mTo );
          break;
        case DrawingPath.DRAWING_PATH_SPLAY:
          mActivity.setTitle( title + " " + mHotPath.mBlock.mFrom + "-." );
          break;
        case DrawingPath.DRAWING_PATH_POINT:
          mActivity.setTitle( title + " " + BrushManager.getPointName( ((DrawingPointPath)mHotPath).mPointType ) );
          hasPointActions = true;
	  deletable = true;
          if ( mHotPath instanceof DrawingPointPath ) {
            setScaleToolbar( (DrawingPointPath)mHotPath );
          } else {
            setScaleToolbar( null );
          }
          break;
        case DrawingPath.DRAWING_PATH_LINE:
          mActivity.setTitle( title + " " + BrushManager.getLineName( ((DrawingLinePath)mHotPath).mLineType ) );
          hasPointActions = true;
          // bm = mBMsnap;
	  deletable = true;
          break;
        case DrawingPath.DRAWING_PATH_AREA:
          mActivity.setTitle( title + " " + BrushManager.getAreaName( ((DrawingAreaPath)mHotPath).mAreaType ) );
          hasPointActions = true;
          // bm = mBMsnap;
	  deletable = true;
          break;
        case DrawingPath.DRAWING_PATH_STATION:
          title = getResources().getString( R.string.title_edit_user_station );
          mActivity.setTitle( title + " " + ((DrawingStationUser)mHotPath).name() );
	  deletable = true;
          break;
        case DrawingPath.DRAWING_PATH_NAME:
          title = getResources().getString( R.string.title_edit_station );
          mActivity.setTitle( title + " " + ((DrawingStationName)mHotPath).getName() );
          break;
        default:
          mActivity.setTitle( title );
      }
    } else {
      mHotPath     = null;
      mHotItemType = -1;
      mActivity.setTitle( title );
    }
    setButton3( BTN_REMOVE, (deletable ? mBMdelete_on : mBMdelete_off) );
    // if ( TDLevel.overNormal ) setButton3( BTN_JOIN3, bm );
  }

  /** set the button3 to display "prev/next" 
   */
  private void setButton3PrevNext( )
  {
    if ( mHasSelected ) {
      setButton3( BTN_SELECT_PREV, mBMprev );
      setButton3( BTN_SELECT_NEXT, mBMnext );
    } else {
      setButtonFilterMode( mSelectMode, Drawing.CODE_SELECT );
      setButtonSelectSize( mSelectScale );
    }
  }

  // 2203-03-10 DROPPED 2023-09-23 RENINSTATED
  /** set the button "continue"
   * @param join_line    type of line-join
   * @note must be called only if TDLevel.overNormal
   * @note called only by toggleButtonContinue
   */
  private void setJoinLine( int join_line )
  {
    if ( ! TDLevel.overNormal ) return; // should not be necessary
    if ( BTN_JOIN2 < mNrButton2 ) { 
      if ( mSymbol == SymbolType.LINE ) {
        if ( ! mDrawingState.isRetraceLine() ) {
          mDrawingState.setJoinLine( join_line );
        }
        mDrawingState.setCanRetraceLine();
      } else {
        mDrawingState.setCanRetraceArea();
      }
      setButtonJoin( );
      setButtonTool();
    }
  }

  /** set the JOIN button (2) according to the drawing style
   *  The drawing style consists of:
   *    mSymbol        symbol type
   *    mRetraceLine   whether retracing a line
   *    mRetraceArea   whether retracing an area
   *    mJoinLine      whether joining line
   */  
  private void setButtonJoin( )
  {
    if ( BTN_JOIN2 < mNrButton2 ) {
      if ( mSymbol != SymbolType.LINE ) {
        mButton2[ BTN_JOIN2 ].setVisibility( View.GONE );
      } else {
        if ( ! mDrawingState.hasJoinLine() ) {
          setButton2( BTN_JOIN2, mBMcont_off );
        } else if ( mDrawingState.isJoinLine() ) {
          setButton2( BTN_JOIN2, mBMcont_both );
        } else {
         setButton2( BTN_JOIN2, mBMcont_none );
        }
        mButton2[ BTN_JOIN2 ].setVisibility( View.VISIBLE );
      }
    }
    // mDrawingState.debug("BTN JOIN");
  }

  /** set the TOOL button (2)
   */
  private void setButtonTool()
  {
    if ( mSymbol == SymbolType.POINT ) {
      setButton2( BTN_TOOL, mBMtoolsPoint );
    } else if ( mSymbol == SymbolType.LINE ) {
      setButton2( BTN_TOOL, mDrawingState.isRetraceLine() ? mBMtoolsLineCont : mBMtoolsLine );
    } else if ( mSymbol == SymbolType.AREA ) {
      setButton2( BTN_TOOL, mDrawingState.isRetraceArea() ? mBMtoolsAreaCont : mBMtoolsArea );
    }
  }

 
  /** toggle the continue flag and update the button
   */
  private void toggleButtonContinue()
  {
    if ( ! TDLevel.overNormal ) return;
    if ( mSymbol != SymbolType.LINE ) return;
    if ( mDrawingState.isRetraceLine() ) return;
    if ( mDrawingState.isJoinLineNone() ) {
      setJoinLine( DrawingState.JOIN_BOTH );
    } else if ( mDrawingState.isJoinLineBoth() ) {
      setJoinLine( DrawingState.JOIN_NONE );
    }
    // mDrawingState.debug("tog JOIN");
    // setButtonJoin(); // already in setJoinLine
  }

  // /** reset the continue flags to false
  //  */
  // public void resetContinue()
  // {
  //   mDrawingState.reset();
  //   switch ( mSymbol ) {
  //     case SymbolType.LINE: setButton2( BTN_TOOL, mBMtoolsLine ); break;
  //     case SymbolType.AREA: setButton2( BTN_TOOL, mBMtoolsArea ); break;
  //   }
  // }

  /** toggle the continue flag of the current symbol
   */
  private boolean toggleButtonRetrace()
  { 
    if ( mSymbol == SymbolType.LINE ) {
      if ( mDrawingState.canRetraceLine() ) {
        mDrawingState.switchRetraceLine();
        // setJoinLine( mJoinLine ); 
        setButtonJoin();
        setButtonTool();
        // mDrawingState.debug("tog RETRACE");
        return true;
      }
    } else if ( mSymbol == SymbolType.AREA ) {
      mDrawingState.switchRetraceArea();
      setButtonJoin();
      setButtonTool();
      // mDrawingState.debug("tog RETRACE");
      return true; 
    }
    return false;
  }

  /** set button "filter"
   * @param filter_mode  type of filter
   * @param code         either ERASE or SELECT
   */
  public void setButtonFilterMode( int filter_mode, int code )
  {
    if ( code == Drawing.CODE_ERASE ) {
      mEraseMode = filter_mode;
      switch ( mEraseMode ) {
        case Drawing.FILTER_ALL:
          setButton5( BTN_ERASE_MODE, mBMeraseAll );
          break;
        case Drawing.FILTER_POINT:
          setButton5( BTN_ERASE_MODE, mBMerasePoint );
          break;
        case Drawing.FILTER_LINE:
          setButton5( BTN_ERASE_MODE, mBMeraseLine );
          break;
        case Drawing.FILTER_AREA:
          setButton5( BTN_ERASE_MODE, mBMeraseArea );
          break;
      }
    } else if ( code == Drawing.CODE_SELECT ) {
      mSelectMode = filter_mode;
      mDrawingSurface.setSelectMode( mSelectMode );
      switch ( mSelectMode ) {
        case Drawing.FILTER_ALL:
          setButton3( BTN_SELECT_MODE, mBMselectAll );
          break;
        case Drawing.FILTER_POINT:
          setButton3( BTN_SELECT_MODE, mBMselectPoint );
          break;
        case Drawing.FILTER_LINE:
          setButton3( BTN_SELECT_MODE, mBMselectLine );
          break;
        case Drawing.FILTER_AREA:
          setButton3( BTN_SELECT_MODE, mBMselectArea );
          break;
        case Drawing.FILTER_SHOT:
          setButton3( BTN_SELECT_MODE, mBMselectShot );
          break;
        case Drawing.FILTER_STATION:
          setButton3( BTN_SELECT_MODE, mBMselectStation );
          break;
      }
    }
  } 

  /** set button "size" to display a given scale
   * @param scale    scale
   */
  private void setButtonEraseSize( int scale )
  {
    mEraseScale = scale % Drawing.SCALE_MAX;
    switch ( mEraseScale ) {
      case Drawing.SCALE_SMALL:
        mEraseSize = 0.5f * TDSetting.mEraseness;
        setButton5( BTN_ERASE_SIZE, mBMsmall );
        break;
      case Drawing.SCALE_MEDIUM:
        mEraseSize = TDSetting.mEraseness;
        setButton5( BTN_ERASE_SIZE, mBMmedium );
        break;
      case Drawing.SCALE_LARGE:
        mEraseSize = 2.0f * TDSetting.mEraseness;
        setButton5( BTN_ERASE_SIZE, mBMlarge );
        break;
    }
  }

  /** set button "delete" on/off
   * @param on    ON or OFF
   */
  private void setButtonDelete( boolean on ) 
  {
    setButton3( BTN_REMOVE, (on ? mBMdelete_on : mBMdelete_off) );
  }

  /** set button "size" to display a given scale
   * @param scale    scale
   */
  private void setButtonSelectSize( int scale )
  {
    mSelectScale = scale % Drawing.SCALE_MAX;
    switch ( mSelectScale ) {
      case Drawing.SCALE_SMALL:
        mSelectSize = 0.5f * TDSetting.mSelectness;
        setButton3( BTN_SELECT_NEXT, mBMsmall );
        break;
      case Drawing.SCALE_MEDIUM:
        mSelectSize = TDSetting.mSelectness;
        setButton3( BTN_SELECT_NEXT, mBMmedium );
        break;
      case Drawing.SCALE_LARGE:
        mSelectSize = 2.0f * TDSetting.mSelectness;
        setButton3( BTN_SELECT_NEXT, mBMlarge );
        break;
    }
  }

  /** set the menu image wit red dots, indicating data issues
   */
  private void setMenuImageRed( boolean red )
  {
    if ( red ) {
      mMenuImage.setOnLongClickListener( this );
      TDandroid.setButtonBackground( mMenuImage, mBMmenured );
    } else {
      // mMenuImage.setOnLongClickListener( null );
      // TDandroid.setButtonBackground( mMenuImage, mBMmenublue );
    }
  } 

  /** switch the ZOOM controls
   * @param ctrl      type of controls
   * @note this method is a callback to let other objects tell the activity to use zooms or not
   */
  private void switchZoomCtrl( int ctrl )
  {
    // TDLog.v( "DEBUG switchZoomCtrl " + ctrl + " ctrl is " + ((mZoomBtnsCtrl == null )? "null" : "not null") );
    // FIXED_ZOOM 
    if ( mDrawingSurface.isFixedZoom() ) return;

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

  /** prepare the buttons of the top bars
   */
  private void makeButtons( )
  {
   
    Resources res = getResources();
    // if ( ! TDSetting.mWithLineJoin && ! mTh2Edit ) { // 20250216
    //   TDLog.v("not with line join");
    //   mNrButton2 --;
    // }

    // TDLog.v("Make Buttons 1: " + mNrButton1 + " 2: " + mNrButton2 + " 3: " + mNrButton3 + " 5: " + mNrButton5 );

    // if ( ! TDLevel.overNormal ) mNrButton1 -= 2; // AZIMUTH, REFRESH requires advanced level
    mNrButton1x = mNrButton1 - 1;
    mButton1  = new Button[ mNrButton1  + 1 ]; // MOVE
    mButton1x = new Button[ mNrButton1x + 1 ]; // MOVE
    int off = 0;
    int ic = 0;
    int kx = 0;
    for ( int k=0; k<mNrButton1; ++k ) {
      ic = ( k <3 )? k : off+k;
      mButton1[k]  = MyButton.getButton( mActivity, this, izons[ic] );
      if ( k != BTN_DIAL ) {
        mButton1x[kx++] = mButton1[k];
      }
      if ( ic == IC_DOWNLOAD )  { mBMdownload = MyButton.getButtonBackground( this, res, izons[ic] ); }
      else if ( ic == IC_BLUETOOTH ) { mBMbluetooth = MyButton.getButtonBackground( this, res, izons[ic] ); }
      else if ( ic == IC_PLAN ) { mBMplan     = MyButton.getButtonBackground( this, res, izons[ic] ); }
    }
    mButton1[ mNrButton1 ]   = MyButton.getButton( mActivity, null, R.drawable.iz_empty );
    mButton1x[ mNrButton1x ] = MyButton.getButton( mActivity, null, R.drawable.iz_empty );

    // FIXME_AZIMUTH_DIAL 1,2
    mBMdial          = BitmapFactory.decodeResource( res, R.drawable.iz_dial_transp ); 
    mDialOn          = BitmapFactory.decodeResource( res, R.drawable.iz_dial_on ); 
    mDialBitmap      = MyTurnBitmap.getTurnBitmap( res );

    mBMextend        = MyButton.getButtonBackground( this, res, izons[IC_EXTEND] ); 
    mBMdownload_on   = MyButton.getButtonBackground( this, res, R.drawable.iz_download_on );
    mBMdownload_wait = MyButton.getButtonBackground( this, res, R.drawable.iz_download_wait );
    mBMleft          = MyButton.getButtonBackground( this, res, R.drawable.iz_left );
    mBMright         = MyButton.getButtonBackground( this, res, R.drawable.iz_right );
    mBMbluetooth_no  = MyButton.getButtonBackground( this, res, R.drawable.iz_bt_no );
    setRefAzimuth( TDAzimuth.mRefAzimuth, TDAzimuth.mFixedExtend );
    mBMsplayNone     = MyButton.getButtonBackground( this, res, R.drawable.iz_splay_none );
    mBMsplayFront    = MyButton.getButtonBackground( this, res, R.drawable.iz_splay_front );
    mBMsplayBack     = MyButton.getButtonBackground( this, res, R.drawable.iz_splay_back );
    mBMsplayBoth     = MyButton.getButtonBackground( this, res, R.drawable.iz_splay_both );
    mBMsplayNoneBlack  = MyButton.getButtonBackground( this, res, R.drawable.iz_splay_none_black );
    mBMsplayFrontBlack = MyButton.getButtonBackground( this, res, R.drawable.iz_splay_front_black );
    mBMsplayBackBlack  = MyButton.getButtonBackground( this, res, R.drawable.iz_splay_back_black );
    mBMsplayBothBlack  = MyButton.getButtonBackground( this, res, R.drawable.iz_splay_both_black );

    // if ( ! TDLevel.overNormal ) -- mNrButton2;
    mButton2 = new Button[ mNrButton2 + 1 ]; // DRAW
    off = (NR_BUTTON1 - 3); 
    // TDLog.v("Button 2: " + mNrButton2 + " off " + off );
    for ( int k=0; k<mNrButton2; ++k ) {
      ic = ( k < 3 )? k : off+k;
      if ( mTh2Edit && k == BTN_SPLAYS ) ++ ic; // TH2EDIT 
      mButton2[k] = MyButton.getButton( mActivity, this, ((k==0)? izons_ok[ic] : izons[ic]) );
      if ( ic == IC_JOIN_NONE ) mBMcont_none = MyButton.getButtonBackground( this, res, ((k==0)? izons_ok[ic] : izons[ic]));
    }
    mButton2[ mNrButton2 ] = mButton1[ mNrButton1 ];

    // 2023-03-10 DROPPED 2023-09-23 REINSTATED
    // if ( TDSetting.mWithLineJoin ) { // 20250216
      // mBMcont_continue  = MyButton.getButtonBackground( this, res, izons[IC_JOIN_CONTINUE] );
      // mBMcont_start = MyButton.getButtonBackground( this, res, izons[IC_JOIN_START] );
      // mBMcont_end   = MyButton.getButtonBackground( this, res, izons[IC_JOIN_END] );
      mBMcont_both  = MyButton.getButtonBackground( this, res, izons[IC_JOIN_BOTH] );
      mBMcont_off   = MyButton.getButtonBackground( this, res, izons[IC_JOIN_OFF] );
    // }

    mBMdelete_off    = MyButton.getButtonBackground( this, res, izons[IC_DELETE_OFF] );
    mBMdelete_on     = MyButton.getButtonBackground( this, res, izons[IC_DELETE_ON] );
    mBMsplays_line   = MyButton.getButtonBackground( this, res, izons[IC_SPLAYS_LINE] );
    mBMsplays_point  = MyButton.getButtonBackground( this, res, izons[IC_SPLAYS_POINT] );
    mBMtoolsPoint    = MyButton.getButtonBackground( this, res, izons[IC_TOOLS_POINT] );
    mBMtoolsLine     = MyButton.getButtonBackground( this, res, izons[IC_TOOLS_LINE] );
    mBMtoolsArea     = MyButton.getButtonBackground( this, res, izons[IC_TOOLS_AREA] );
    mBMtoolsLineCont = MyButton.getButtonBackground( this, res, izons[IC_TOOLS_LINE_TILDE] );
    mBMtoolsAreaCont = MyButton.getButtonBackground( this, res, izons[IC_TOOLS_AREA_TILDE] );
    // mBMsnap         = MyButton.getButtonBackground( this, res, izons[IC_SNAP] );

    // if ( ! TDLevel.overNormal ) mNrButton3 -= 2; // item_properties point_actions 
    // if ( ! TDLevel.overExpert ) mNrButton3 -= 1; // line_range
    mButton3 = new Button[ mNrButton3 + 1 ];      // EDIT
    off = (NR_BUTTON1 - 3) + (NR_BUTTON2 - 3); 
    // TDLog.v("off3 " + off + " nr " + mNrButton3 );
    for ( int k=0; k<mNrButton3; ++k ) {
      ic = ( k < 3 )? k : off+k;
      mButton3[k] = MyButton.getButton( mActivity, this, ((k==2)? izons_ok[ic] : izons[ic]) );
      // if ( ic == IC_SNAP ) mBMsnap = MyButton.getButtonBackground( this, res, ((k==2)? izons_ok[ic] : izons[ic]) );
    }
    if ( TDLevel.overExpert ) {
      if ( BTN_BORDER < mNrButton3 ) {
        mButton3[ BTN_BORDER ].setPadding(4,4,4,4);
        mButton3[ BTN_BORDER ].setTextColor( 0xffffffff );
      }
      mBMedit_item = MyButton.getButtonBackground( this, res, izons[IC_RANGE_ITEM] );
      mBMedit_box  = MyButton.getButtonBackground( this, res, izons[IC_RANGE_BOX] );
      mBMedit_ok   = MyButton.getButtonBackground( this, res, izons[IC_RANGE_OK] ); 
      mBMedit_no   = MyButton.getButtonBackground( this, res, izons[IC_RANGE_NO] );
    }
    mButton3[ mNrButton3 ] = mButton1[ mNrButton1 ];

    // mBMjoin_no = MyButton.getButtonBackground( this, res, izons[IC_SNAP_NO] );
    mBMadd     = MyButton.getButtonBackground( this, res, izons[IC_ADD] );


    mButton5 = new Button[ mNrButton5 + 1 ];    // ERASE
    off = 10 - 3; // (mNrButton1-3) + (mNrButton2-3) + (mNrButton3-3);
    for ( int k=0; k<mNrButton5; ++k ) {
      ic = ( k < 3 )? k : off+k;
      mButton5[k] = MyButton.getButton( mActivity, this, ((k==1)? izons_ok[ic] : izons[ic] ) );
    }
    mButton5[ mNrButton5 ] = mButton1[ mNrButton1 ];

    mBMeraseAll   = MyButton.getButtonBackground( this, res, izons[IC_ERASE_ALL] );
    mBMerasePoint = MyButton.getButtonBackground( this, res, izons[IC_ERASE_POINT] );
    mBMeraseLine  = MyButton.getButtonBackground( this, res, izons[IC_ERASE_LINE] );
    mBMeraseArea  = MyButton.getButtonBackground( this, res, izons[IC_ERASE_AREA] );
    setButtonFilterMode( mEraseMode, Drawing.CODE_ERASE );

    mBMprev        = MyButton.getButtonBackground( this, res, izons[IC_PREV] );
    mBMnext        = MyButton.getButtonBackground( this, res, izons[IC_NEXT] );
    mBMselectAll   = MyButton.getButtonBackground( this, res, izons[IC_SELECT_ALL] );
    mBMselectPoint = MyButton.getButtonBackground( this, res, izons[IC_SELECT_POINT] );
    mBMselectLine  = MyButton.getButtonBackground( this, res, izons[IC_SELECT_LINE] );
    mBMselectArea  = MyButton.getButtonBackground( this, res, izons[IC_SELECT_AREA] );
    mBMselectShot  = MyButton.getButtonBackground( this, res, izons[IC_SELECT_SHOT] );
    mBMselectStation=MyButton.getButtonBackground( this, res, izons[IC_SELECT_STATION] );
    setButtonFilterMode( mSelectMode, Drawing.CODE_SELECT );

    mBMsmall  = MyButton.getButtonBackground( this, res, izons[IC_SMALL] );
    mBMmedium = MyButton.getButtonBackground( this, res, izons[IC_MEDIUM] );
    mBMlarge  = MyButton.getButtonBackground( this, res, izons[IC_LARGE] );

    mBMmenured = MyButton.getButtonBackground( this, res, izons[IC_MENU_RED] );
    // mBMmenublue = MyButton.getButtonBackground( this, res, izons[IC_MENU] );

    setButtonEraseSize( Drawing.SCALE_MEDIUM );
    setButtonSelectSize( Drawing.SCALE_MEDIUM );

    mButtonView1  = new MyHorizontalButtonView( mButton1 );
    mButtonView1x = new MyHorizontalButtonView( mButton1x );
    mButtonView2  = new MyHorizontalButtonView( mButton2 );
    mButtonView3  = new MyHorizontalButtonView( mButton3 );
    mButtonView5  = new MyHorizontalButtonView( mButton5 );
  }

  /** set the params of the tools toolbar
   * @NOTE called by TopoDroidApp when toolbar size setting is changed
   */
  public void setToolsToolbarParams()
  {
    float scale = 8 * TDSetting.mItemButtonSize;
    {
      ViewGroup.LayoutParams lp0;
      lp0 = mLayoutToolsP.getLayoutParams();
      lp0.height = (int)(scale * Float.parseFloat( getResources().getString( R.string.dimyl ) ) ) + 8; // 4 pxl on both sides 
      // TDLog.v("LP height " + lp0.height );
      mLayoutToolsP.setLayoutParams( lp0 );

      lp0 = mLayoutToolsL.getLayoutParams();
      lp0.height = (int)(scale * Float.parseFloat( getResources().getString( R.string.dimyl ) ) ) + 8; // 4 pxl on both sides 
      mLayoutToolsL.setLayoutParams( lp0 );

      lp0 = mLayoutToolsA.getLayoutParams();
      lp0.height = (int)(scale * Float.parseFloat( getResources().getString( R.string.dimyl ) ) ) + 8; // 4 pxl on both sides 
      mLayoutToolsA.setLayoutParams( lp0 );
    }
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 0, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp.setMargins( 0, 0, 0, 0 );
    lp.weight = 16;
    lp.gravity = 0x10; // LinearLayout.LayoutParams.center_vertical;

    mLayoutToolsP.removeAllViews( );
    mLayoutToolsL.removeAllViews( );
    mLayoutToolsA.removeAllViews( );
    for ( int k = 0; k<=NR_RECENT; ++k ) {
      mLayoutToolsP.addView( mBtnRecentP[k], lp );
      mLayoutToolsL.addView( mBtnRecentL[k], lp );
      mLayoutToolsA.addView( mBtnRecentA[k], lp );
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    getWindow().getDecorView().setSystemUiVisibility( TDSetting.mUiVisibility );

    TDandroid.setScreenOrientation( this );

    // TDLog.TimeStart();
    // TDLog.v( "Drawing Window on create" );

    mApp  = (TopoDroidApp)getApplication();
    mActivity = this;
    mApp_mData = TopoDroidApp.mData; 
    mMediaManager  = new MediaManager( mApp_mData );
    // mStylusSizeDpi = TDSetting.mStylusSize * TopoDroidApp.getDisplayDensityDpi() / 25; // STYLUS_MM
    mStylusSizeDpi = TDSetting.mStylusSize;
    // TDLog.v("STYLUS size " + mStylusSizeDpi + " flag " + TDSetting.mStylusOnly );

    mFormatClosure = getResources().getString(R.string.format_closure );

    audioCheck = TDandroid.checkMicrophone( mActivity );
    // TDLog.v( "Microphone perm : " + audioCheck );

    mZoomBtnsCtrlOn = (TDSetting.mZoomCtrl > 1);  // do before setting content
    mPointScale = PointScale.SCALE_M;

    // mIsNotMultitouch = ! TDandroid.checkMultitouch( this );

    setContentView(R.layout.drawing_activity);
    mDataDownloader   = mApp.mDataDownloader; // new DataDownloader( this, mApp );
    mZoom             = TopoDroidApp.mScaleFactor;    // canvas zoom

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
    // if ( TDSetting.mTripleToolbar ) {
    //   ZOOM_TRANSLATION = ZOOM_TRANSLATION_3;
    // } else {
    //   ZOOM_TRANSLATION = ZOOM_TRANSLATION_1;
    // }
    mZoomView.setTranslationY( ZOOM_TRANSLATION );

    mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
    // FIXME_ZOOM_CTRL mZoomCtrl = (ZoomControls) mZoomBtnsCtrl.getZoomControls();
    // ViewGroup vg = mZoomBtnsCtrl.getContainer();
    // switchZoomCtrl( TDSetting.mZoomCtrl );

    mListView = (MyHorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlaceholder(true);
    mButtonSize = TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    mZoomTranslate = mButtonSize;

    mMenuImage = (Button) findViewById( R.id.handle );
    mMenuImage.setOnClickListener( this );
    mBMmenublue = MyButton.getButtonBackground( this, getResources(), izons[IC_MENU] );
    TDandroid.setButtonBackground( mMenuImage, mBMmenublue );
    mMenu = (ListView) findViewById( R.id.menu );
    mMenu.setOnItemClickListener( this );

    // redoBtn.setEnabled(false);
    // undoBtn.setEnabled(false); // let undo always be there

    // mBezierInterpolator = new BezierInterpolator( );

    String pathname = null;
    Bundle extras = getIntent().getExtras();
    if ( extras != null ) {
      mSid  = extras.getLong( TDTag.TOPODROID_SURVEY_ID );
      // TDLog.v("Th2 survey ID " + mSid );
      mDecl = 0; // FIXME do not correct declination in sketches
      if ( mSid < 0 ) { // TH2EDIT load th2 file
        mDataDownloader = null;
        mTh2Edit = true;
        TDSetting.mAutoStations = false;
        mNrButton1 = 3; // move
        mNrButton2 = 6; // draw
        // BTN_JOIN2   = 7;
      } else {
        // mDecl = mApp_mData.getSurveyDeclination( mSid );

        mName1 = extras.getString( TDTag.TOPODROID_PLOT_NAME );
        mName2 = extras.getString( TDTag.TOPODROID_PLOT_NAME2 );
        mFullName1 = TDInstance.survey + "-" + mName1;
        mFullName2 = TDInstance.survey + "-" + mName2;
        mFullName3 = null;
        mType = extras.getLong( TDTag.TOPODROID_PLOT_TYPE );
        // TDLog.v( "PLOT name1 " + mName1 + " (" + mFullName1 + ") name2 " + mName2 + " (" + mFullName2 + ")" );

        mName    = (mType == PlotType.PLOT_PLAN)? mName1 : mName2;
        mFrom    = extras.getString( TDTag.TOPODROID_PLOT_FROM );
        mTo      = extras.getString( TDTag.TOPODROID_PLOT_TO );
        mAzimuth = extras.getFloat( TDTag.TOPODROID_PLOT_AZIMUTH );
        mClino   = extras.getFloat( TDTag.TOPODROID_PLOT_CLINO );
        mMoveTo  = extras.getString( TDTag.TOPODROID_PLOT_MOVE_TO );
        mLandscape = extras.getBoolean( TDTag.TOPODROID_PLOT_LANDSCAPE );
        // TDLog.v( "from " + mFrom );
      }
    } 
    // // mDrawingUtil = mLandscape ? (new DrawingUtilLandscape()) : ( new DrawingUtilPortrait());
    // mDrawingUtil = new DrawingUtilPortrait();

    // mEraseScale = 0;  done in makeButtons()
    // mSelectScale = 0;
    makeButtons( );

    if ( ! TDLevel.overBasic ) {
      mButton1[2].setVisibility( View.GONE );
      mButton2[2].setVisibility( View.GONE );
      mButton3[2].setVisibility( View.GONE );
      mButton5[2].setVisibility( View.GONE );
    } else {
      mButton3[2].setOnLongClickListener( this ); // options
      mButton2[0].setOnLongClickListener( this );
      mButton5[1].setOnLongClickListener( this );
    }
    // setConnectionStatus( mDataDownloader.getStatus() ); // 20201123 this is done in onResume
    if ( TDLevel.overBasic ) {
      if ( BTN_PLAN   < mNrButton1 ) mButton1[BTN_PLAN].setOnLongClickListener( this );
      if ( BTN_REMOVE < mNrButton3 ) mButton3[BTN_REMOVE].setOnLongClickListener( this );
    }
    if ( TDLevel.overNormal ) {
      if ( BTN_TOOL < mNrButton2 ) mButton2[ BTN_TOOL ].setOnLongClickListener( this );
    }
    if ( TDLevel.overAdvanced ) {
      if ( BTN_DOWNLOAD  < mNrButton1 && mDataDownloader != null ) mButton1[BTN_DOWNLOAD].setOnLongClickListener( this );
      if ( BTN_DIAL      < mNrButton1 ) mButton1[BTN_DIAL].setOnLongClickListener( this );
      if ( BTN_ITEM_EDIT < mNrButton3 ) mButton3[BTN_ITEM_EDIT].setOnLongClickListener( this );
    }
    mListView.setAdapter( mButtonView1.mAdapter );
    // mListView.invalidate();

    if ( mMoveTo != null && mMoveTo.length() == 0 ) mMoveTo = null; // test for Xiaomi redmi note
    mSectionName  = null; // resetStatus
    // mLastLinePath = null;
    mShiftDrawing = false;
    mScrapOnly   = false;
    mDrawingState.reset();
    resetModified();

    // if ( PlotType.isLegSection( mType ) ) { 
    //   mTo      = extras.getString( TDTag.TOPODROID_PLOT_TO );  // to station ( null for X-section)
    //   mAzimuth = (float)extras.getLong( TDTag.TOPODROID_PLOT_AZIMUTH );
    //   mClino   = (float)extras.getLong( TDTag.TOPODROID_PLOT_CLINO );
    //   // TDLog.v( "X-Section " + mFrom + "-" + mTo + " azimuth " + mAzimuth + " clino " + mClino  );
    // } else if ( PlotType.isStationSection( mType ) ) {
    //   mTo = null;
    //   mAzimuth = (float)extras.getLong( TDTag.TOPODROID_PLOT_AZIMUTH );
    //   mClino   = (float)extras.getLong( TDTag.TOPODROID_PLOT_CLINO );
    // }

    // TDLog.TimeEnd( "on create" );

    // TDLog.v( "on create" );
    if ( mCurrentPoint < 0 ) mCurrentPoint = ( BrushManager.isPointEnabled(  SymbolLibrary.LABEL  ) )?  1 : 0;
    if ( mCurrentLine < 0 )  mCurrentLine  = ( BrushManager.isLineEnabled( SymbolLibrary.WALL ) )?  1 : 0;
    if ( mCurrentArea < 0 )  mCurrentArea  = ( BrushManager.isAreaEnabled( SymbolLibrary.WATER ) )?  1 : 0;

    if ( ! mTh2Edit ) { // TH2EDIT
      doStart( true, -1, null );
    } else {
      resetReference( null, false );
      mDrawingSurface.resetManager( DrawingSurface.DRAWING_SECTION, null, false );
      mDrawingSurface.addScaleRef( DrawingSurface.DRAWING_SECTION, (int)PlotType.PLOT_NULL, 0 );
    }

    mLayoutTools  = (LinearLayout) findViewById( R.id.layout_tools  );
    mLayoutToolsP = (LinearLayout) findViewById( R.id.layout_tool_p );
    mLayoutToolsL = (LinearLayout) findViewById( R.id.layout_tool_l );
    mLayoutToolsA = (LinearLayout) findViewById( R.id.layout_tool_a );
    mLayoutScale  = (LinearLayout) findViewById( R.id.layout_scale  );
    mScaleBar     = (SeekBar)findViewById( R.id.scalebar );
    mScaleBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
      public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
        if ( fromUser ) {
          if ( setPointScaleProgress( progress ) ) {
            seekbar.setProgress( 100 );
          }
        }
      }
      public void onStartTrackingTouch(SeekBar seekbar) { }
      public void onStopTrackingTouch(SeekBar seekbar) { }
    } );
    mLayoutTools.setVisibility( View.INVISIBLE );

    resetRecentTools();

    // setBtnRecentAll(); done on Start
    // mRecentTools = mRecentLine; // done on Start

    if ( mDataDownloader != null ) {
      mApp.registerLister( this );
    } 

    mBTstatus = ConnectionState.CONN_DISCONNECTED;
    TopoDroidApp.mDrawingWindow = this;

    // if ( mApp.hasHighlighted() ) {
    //   // TDLog.v( "drawing window [2] highlighted " + mApp.getHighlightedSize() );
    //   mDrawingSurface.highlights( mApp );
    //   TopoDroidApp.mShotWindow.clearMultiSelect();
    // }

    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on create done");

    setTheTitle();
  }

  /** reset the recent toolbars
   * @NOTE called by TopoDroidApp when the symbol-size setting is changed
   */
  void resetRecentTools()
  {
    // TDLog.v("RESET recent tools - symbol size " + TDSetting.mSymbolSize );
    mBtnRecentP = new ItemButton[ NR_RECENT + 1 ];
    mBtnRecentL = new ItemButton[ NR_RECENT + 1 ];
    mBtnRecentA = new ItemButton[ NR_RECENT + 1 ];
    for ( int k = 0; k<NR_RECENT; ++k ) {
      mBtnRecentP[k] = new ItemButton( this );
      mBtnRecentP[k].setOnClickListener(
        new View.OnClickListener() {
          @Override public void onClick( View v ) {
            for ( int k = 0; k<NR_RECENT; ++k ) if ( v == mBtnRecentP[k] ) {
              if ( setCurrentPoint( k, false ) ) {
                setHighlight( SymbolType.POINT, k );
              } else {
                TDToast.makeWarn( R.string.section_point_not_allowed );
              }
              break;
            }
          }
        }
      );
      mBtnRecentL[k] = new ItemButton( this );
      mBtnRecentL[k].setOnClickListener(
        new View.OnClickListener() {
          @Override public void onClick( View v ) {
            for ( int k = 0; k<NR_RECENT; ++k ) if ( v == mBtnRecentL[k] ) {
              if ( setCurrentLine( k, false ) ) {
                setHighlight( SymbolType.LINE, k );
              } else {
                TDToast.makeWarn( R.string.section_line_not_allowed );
              }
              break;
            }
          }
        }
      );
      mBtnRecentA[k] = new ItemButton( this );
      mBtnRecentA[k].setOnClickListener(
        new View.OnClickListener() {
          @Override public void onClick( View v ) {
            for ( int k = 0; k<NR_RECENT; ++k ) if ( v == mBtnRecentA[k] ) {
              setCurrentArea( k, false );
              setHighlight( SymbolType.AREA, k );
              break;
            }
          }
        }
      );
    }
    mBtnRecentP[NR_RECENT] = new ItemButton( this );
    mBtnRecentL[NR_RECENT] = new ItemButton( this );
    mBtnRecentA[NR_RECENT] = new ItemButton( this );

    // mBtnRecentP[NR_RECENT].setText( ">>" );
    // mBtnRecentL[NR_RECENT].setText( ">>" );
    // mBtnRecentA[NR_RECENT].setText( ">>" );

    Path path = new Path(); // double-arrow ">>"
    path.moveTo( 0, 8 ); path.lineTo(  8, 0 ); path.lineTo( 0, -8 );
    path.moveTo( 8, 8 ); path.lineTo( 16, 0 ); path.lineTo( 8, -8 );

    mBtnRecentP[NR_RECENT].resetPaintPath( BrushManager.labelPaint, path, 2, 2 ); 
    mBtnRecentP[NR_RECENT].invalidate();
    mBtnRecentP[NR_RECENT].setOnClickListener(
      new View.OnClickListener() {
        @Override public void onClick( View v ) { startItemPickerDialog( SymbolType.POINT ); }
      }
    );
    mBtnRecentL[NR_RECENT].resetPaintPath( BrushManager.labelPaint, path, 2, 2 );
    mBtnRecentL[NR_RECENT].invalidate();
    mBtnRecentL[NR_RECENT].setOnClickListener(
      new View.OnClickListener() {
        @Override public void onClick( View v ) { startItemPickerDialog( SymbolType.LINE ); }
      }
    );
    mBtnRecentA[NR_RECENT].resetPaintPath( BrushManager.labelPaint, path, 2, 2 );
    mBtnRecentA[NR_RECENT].invalidate();
    mBtnRecentA[NR_RECENT].setOnClickListener(
      new View.OnClickListener() {
        @Override public void onClick( View v ) { startItemPickerDialog( SymbolType.AREA ); }
      }
    );

    setToolsToolbarParams();
  }

  // ------------------------------------- PUSH / POP INFO --------------------------------
  private long mSavedType;
  private int mSavedMode;
  // private int mSplayMode;

  /** reset the status mode(s) to the default values
   * @note called by popInfo pushInfo
   */
  private void resetStatus()
  {
    mSectionName  = null; 
    // mLastLinePath = null;
    mShiftDrawing = false;
    mScrapOnly   = false;
    mDrawingState.reset();
    resetModified();
    setMode( MODE_MOVE ); // this setTheTitle() as well, and clearHotPath( INVISIBLE )
    mTouchMode    = MODE_MOVE;
    setMenuAdapter( getResources(), mType );
  }
  
  /** restore the saved status
   * @note called by onBackPressed() and doStart()
   */
  private void popInfo()
  {
    // TODO save plot offset and zoom
    if ( mPid3 >= 0 ) {
      // TDLog.v( "update xsection pid " + mPid3 + " X " + mOffset.x + " Y " + mOffset.y + " zoom " + mZoom );
      try {
        mApp_mData.updatePlot( mPid3, mSid, mOffset.x, mOffset.y, mZoom );
      } catch ( IllegalStateException e ) {
        TDLog.e("cannot save plot state: " + e.getMessage() );
      }
    }

    mOffset.x = mSavedOffset.x;
    mOffset.y = mSavedOffset.y;
    mZoom     = mSavedZoom;
    PlotInfo plot = ( mSavedType == PlotType.PLOT_PLAN )? mPlot1 : mPlot2;
    mType    = plot.type;
    mName    = plot.name;
    mFrom    = plot.start; 
    mTo      = "";
    mAzimuth = plot.azimuth;
    mClino   = plot.clino;
    mDrawingSurface.setDisplayMode( mSavedMode );
    // TDLog.v( "pop " + mType + " " + mName + " from " + mFrom + " A " + mAzimuth + " C " + mClino );
    resetStatus();

    // DO NOT CALL resetReference( plot ); THIS LINE IS ENOUGH
    // TDLog.v("PLOT INFO pop: " + mOffset.x + " " + mOffset.y + " " + mZoom );
    mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );

    // FIXME_SK mButton1[ BTN_DOWNLOAD ].setVisibility( View.VISIBLE );
    // FIXME_SK mButton1[ BTN_BLUETOOTH ].setVisibility( View.VISIBLE );

    // mButton1[ BTN_PLAN ].setVisibility( View.VISIBLE );
    if ( ! TDLevel.overExpert && BTN_PLAN < mNrButton1 ) mButton1[ BTN_PLAN ].setOnLongClickListener( this );
    if ( TDLevel.overNormal ) {
      mListView.setAdapter( mButtonView1.mAdapter );
      // if ( BTN_DIAL < mNrButton1 ) mButton1[ BTN_DIAL ].setVisibility( View.VISIBLE );
      // if ( BTN_REFRESH < mNrButton1 ) mButton1[ BTN_REFRESH ].setVisibility( View.VISIBLE );
    }
  }

  /** push the status info when a xsection is opened
   * @param type    xsection type
   * @param name    xsection name
   * @param from    xsection from string
   * @param to      xsection to string
   * @param azimuth xsection azimuth
   * @param clino   xsection clino
   * @param tt      xsection leg-intersection, only for single-leg xsections
   * @param center  xsection center, only for multileg xsections
   */
  private void pushInfo( long type, String name, String from, String to, float azimuth, float clino, float tt, Vector3D center )
  {
    // TDLog.v( "push info " + type + " " + name + " from " + from + " " + to + " A " + azimuth + " C " + clino + " TT " + tt );
    mSavedType = mType;
    mSavedOffset.x = mOffset.x;
    mSavedOffset.y = mOffset.y;
    mSavedZoom     = mZoom;
    // TDLog.v( "PLOT INFO push: " + mOffset.x + " " + mOffset.y + " " + mZoom );

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
    doStart( true, tt, center );
    updateSplays( mApp.mSplayMode );

    // FIXME_SK mButton1[ BTN_DOWNLOAD ].setVisibility( View.GONE );
    // FIXME_SK mButton1[ BTN_BLUETOOTH ].setVisibility( View.GONE );

    // mButton1[ BTN_PLAN ].setVisibility( View.GONE );
    if ( ! TDLevel.overExpert && BTN_PLAN < mNrButton1 ) mButton1[BTN_PLAN].setOnLongClickListener( null );
    if ( TDLevel.overNormal ) {
      mListView.setAdapter( mButtonView1x.mAdapter );
      // if (  BTN_DIAL < mNrButton1 ) mButton1[ BTN_DIAL ].setVisibility( View.GONE );
      // if (  BTN_REFRESH < mNrButton1 ) mButton1[ BTN_REFRESH ].setVisibility( View.GONE );
    }
  }

  /** update the display of the splays - for XSections
   * @param mode  splay display mode
   */
  private void updateSplays( int mode )
  {
    mApp.mSplayMode = mode;
    switch ( mode ) {
      case 0: 
        // TDLog.v("hide splays at FROM and at TO");
        setButton1( BTN_PLAN, (mApp.mShowSectionSplays? mBMsplayNone : mBMsplayNoneBlack) );
        if ( PlotType.isMultilegSection( mType, mTo ) ) {
          for ( String from : mFroms ) mDrawingSurface.hideStationSplays( from );
          for ( String to   : mTos ) mDrawingSurface.hideStationSplays( to );
        } else {
          if ( PlotType.isLegSection( mType ) ) mDrawingSurface.hideStationSplays( mTo );
          mDrawingSurface.hideStationSplays( mFrom );
        }
        break;
      case 1: 
        // TDLog.v("hide splays at FROM show splays at TO");
        setButton1( BTN_PLAN, (mApp.mShowSectionSplays? mBMsplayFront : mBMsplayFrontBlack) );
        if ( PlotType.isMultilegSection( mType, mTo ) ) {
          for ( String from : mFroms ) mDrawingSurface.hideStationSplays( from );
          for ( String to   : mTos ) mDrawingSurface.showStationSplays( to );
        } else {
          if ( PlotType.isLegSection( mType ) ) mDrawingSurface.showStationSplays( mTo );
          mDrawingSurface.hideStationSplays( mFrom );
        }
        break;
      case 2: 
        // TDLog.v("show splays at FROM and at TO");
        setButton1( BTN_PLAN, (mApp.mShowSectionSplays? mBMsplayBoth : mBMsplayBothBlack) );
        if ( PlotType.isMultilegSection( mType, mTo ) ) {
          for ( String from : mFroms ) mDrawingSurface.showStationSplays( from );
          for ( String to   : mTos ) mDrawingSurface.showStationSplays( to );
        } else {
          if ( PlotType.isLegSection( mType ) ) mDrawingSurface.showStationSplays( mTo );
          mDrawingSurface.showStationSplays( mFrom );
        }
        break;
      case 3: 
        // TDLog.v("show splays at FROM, hide splays at TO");
        setButton1( BTN_PLAN, (mApp.mShowSectionSplays? mBMsplayBack : mBMsplayBackBlack) );
        if ( PlotType.isMultilegSection( mType, mTo ) ) {
          for ( String from : mFroms ) mDrawingSurface.showStationSplays( from );
          for ( String to   : mTos ) mDrawingSurface.hideStationSplays( to );
        } else {
          if ( PlotType.isLegSection( mType ) ) mDrawingSurface.hideStationSplays( mTo );
          mDrawingSurface.showStationSplays( mFrom );
        }
        break;
    }
    mDrawingSurface.setSplayAlpha( mApp.mShowSectionSplays ); // not necessary ?
  }

  /** switch to another plot
   * @param name   the other plot name
   * @param tt   the other plot type
   * @note called by PlotListDialog
   */
  void switchNameAndType( String name, long tt ) // SWITCH
  {
    if ( mTh2Edit ) return;

    // TopoDroidApp.mShotWindow.setRecentPlot( name, tt );
    TDInstance.setRecentPlot( name, tt );

    PlotInfo p1 = mApp_mData.getPlotInfo( TDInstance.sid, name+"p" );
    // TDLog.v( "PLOT switch name " + name + " info " + p1.name + " " + p1.id + " pid " + mPid1 + " " + mPid2 );
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
    if ( p1 != null ) { // always true
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
      mName      = (mType == PlotType.PLOT_PLAN)? mName1 : mName2;

      mFrom    = p1.start;
      mTo      = "";
      mAzimuth = 0;
      mClino   = 0;
      mMoveTo  = null;
      mSectionName  = null; // resetStatus
      // mLastLinePath = null;
      mShiftDrawing = false;
      mScrapOnly   = false;
      mDrawingState.reset();
      resetModified();

      doStart( true, -1, null );
    }
  }

  // ==============================================================
  /** start the item picker dialog
   */
  void startItemPickerDialog()
  {
    int symbol = mSymbol;
    if ( mRecentTools == mRecentPoint )     { symbol = SymbolType.POINT; }
    else if ( mRecentTools == mRecentLine ) { symbol = SymbolType.LINE; }
    else if ( mRecentTools == mRecentArea ) { symbol = SymbolType.AREA; }
    startItemPickerDialog( symbol );
  }

  /** start the item picker dialog
   * @param symbol  initial symbol class that is shown in the dialog
   */
  private void startItemPickerDialog( int symbol )
  {
    new ItemPickerDialog( mActivity, this, mType, symbol ).show();
  }

  // ==============================================================

  /** lifecycle: RESUME
   */
  @Override
  protected synchronized void onResume()
  {
    super.onResume();
    TDLog.v( "Drawing Activity on Resume " );
    // TDLog.v( "Drawing Activity onResume " + ((mDataDownloader!=null)?"with DataDownloader":"") );
    doResume();
    if ( mDataDownloader != null ) {
      mDataDownloader.onResume();
      setConnectionStatus( mDataDownloader.getStatus() );
    }
    // TDLog.TimeEnd( "drawing activity ready" );
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on resume done");
  }

  /** lifecycle: PAUSE
   */
  @Override
  protected synchronized void onPause() 
  { 
    TDLog.v( "Drawing Activity onPause " );
    doPause();
    super.onPause();
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on pause done");
  }

  /** lifecycle: START
   */
  @Override
  protected synchronized void onStart()
  {
    super.onStart();
    TDLog.v("Drawing Activity on Start " );
    TDLocale.resetTheLocale();
    loadRecentSymbols( mApp_mData );
    mOutlinePlot1 = null;
    mOutlinePlot2 = null;
    // setBtnRecentAll(); // FIXME this is done onResume
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on start done");
    setMenuAdapter( getResources(), mType );
    closeMenu();
  }

  /** lifecycle: STOP
   */
  @Override
  protected synchronized void onStop()
  {
    super.onStop();
    TDLog.v("Drawing Activity onStop ");
    saveRecentSymbols( mApp_mData );
    // doStop();
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on stop done");
  }

  /** lifecycle: DESTROY
   */
  @Override
  protected synchronized void onDestroy()
  {
    super.onDestroy();
    // TDLog.v( "Drawing activity onDestroy");
    if ( mDataDownloader != null ) {
      mApp.unregisterLister( this );
    }
    TopoDroidApp.mDrawingWindow = null;
    // if ( mDataDownloader != null ) { // data-download management is left to ShotWindow
    //   mDataDownloader.onStop();
    //   mApp.disconnectRemoteDevice( false );
    // }
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on destroy done");
  }

  /** lifecycle: implement RESUME
   */
  private void doResume() // restoreInstanceFromData
  {
    // TDLog.v( "doResume()" );
    // TDLog.v("restore drawing display mode");
    String mode = mApp_mData.getValue( "DISTOX_PLOT_MODE" );
    DrawingCommandManager.setDisplayMode( DisplayMode.parseString( mode ) );
    // mLastLinePath = null; // necessary ??? these two were at the end
    switchZoomCtrl( TDSetting.mZoomCtrl );

    if ( ! mTh2Edit ) { // TH2EDIT
      PlotInfo info = mApp_mData.getPlotInfo( mSid, mName );
      mOffset.x = info.xoffset;
      mOffset.y = info.yoffset;
      mZoom     = info.zoom;
      // TDLog.v("PLOT resume: " + mOffset.x + " " + mOffset.y + " " + mZoom );
      setPlotType( mType, PARAMS_YES );
    }
    mDrawingSurface.setDrawing( true );
    // TDLog.v( "do Resume. offset " + mOffset.x + " " + mOffset.y + " zoom " + mZoom );
  }

  /** lifecycle: implement PAUSE
   */
  private void doPause() // saveInstanceToData
  {
    switchZoomCtrl( 0 );
    mDrawingSurface.setDrawing( false );
    if ( mPid >= 0 ) {
      try {
        // TDLog.v("PLOT pause: " + mOffset.x + " " + mOffset.y + " " + mZoom );
        mApp_mData.updatePlot( mPid, mSid, mOffset.x, mOffset.y, mZoom );
      } catch ( IllegalStateException e ) {
        TDLog.e("cannot save plot state: " + e.getMessage() );
      }
    }
    // TODO exec this line in a Thread
    // TDLog.v("save drawing display mode");
    (new Thread() {
       public void run() {
         mApp_mData.setValue( "DISTOX_PLOT_MODE", DisplayMode.toString( DrawingCommandManager.getDisplayMode() ) );
       }
    } ).start();
    doSaveTdr( ); // do not alert-dialog on mAllSymbols
  }

  // private void doStop()
  // {
  //   // TDLog.Log( TDLog.LOG_PLOT, "doStop type " + mType + " modified " + Modified );
  // }

// ----------------------------------------------------------------------------
  /** @return the list of legs and splays given a set of IDs of the legs
   * @param from    string with the leg IDs
   */
  private List<DBlock> getMultilegShots( String from )
  {
    mFroms.clear();
    mTos.clear();
    ArrayList< DBlock > list = new ArrayList< >();
    TreeSet< String > stations = new TreeSet<>();
    String[] ids = from.split(" ");
    for ( String id : ids ) {
      if ( id.length() == 0 ) continue;
      DBlock blk = mApp_mData.selectShot( Long.parseLong(id), mSid );
      if ( blk != null ) { 
        // TDLog.v("PLOT leg " + id + ": " + blk.mFrom + " " + blk.mTo );
        stations.add( blk.mFrom );
        stations.add( blk.mTo );
        if ( TDMath.angleDifference( mAzimuth, blk.mBearing) < 90.0f ) {
          mFroms.add( blk.mFrom );
          mTos.add( blk.mTo );
        } else {
          mTos.add( blk.mFrom );
          mFroms.add( blk.mTo );
        }
        list.add( blk );
      }
    }
    List< DBlock > list0 = mApp_mData.selectAllSplaysAtStations( mSid, stations );
    list.addAll( list0 ); // for ( DBlock blk0 : list0 ) list.add( blk0 );
    // TDLog.v("PLOT multileg list size " + list.size() + " stations " + stations.size() + " splays " + list0.size() + " froms " + mFroms.size() + " tos " + mTos.size() );
    return list;
  }

  private List<DBlock> getXSectionShots( long type, String from, String to )
  {
    List< DBlock > list = null;
    if ( PlotType.isLegSection( type ) ) {
      if ( to != null && to.length() > 0 ) { // single leg xsection
        list = mApp_mData.selectAllShotsAtStations( mSid, from, to );
        // TDLog.v("Leg-Xsection select all shots at " + mFrom + " " + mTo + " : " + list.size() );
      } else { // multileg xsection
        list = getMultilegShots( from );
      }
    } else if ( PlotType.isStationSection( type ) ) { 
      // N.B. mTo can be null
      list = mApp_mData.selectShotsAt( mSid, from, false ); // select only splays
      // TDLog.v("Station-Xsection select all shots at " + mFrom + " : " + list.size() );
    }
    return list;
  }

  /** restart a xsection
   * @note called by updateDisplay if the type is not plan/profile
   */
  private void doRestart( )
  {
    // mLastLinePath = null;
    List< DBlock > list = getXSectionShots( mType, mFrom, mTo );
    if ( list != null && list.size() > 0 ) {
      if ( PlotType.isMultilegSection( mType, mTo ) ) {
        // TDLog.v("PLOT restart multileg list " + list.size() );
        makeMultilegSectionReferences( list, mPlot3.center );
      } else {
        if ( mIntersectionT != mPlot3.intercept ) {
          // TDLog.v( "do restart section - update intercept T " + mIntersectionT + " " + mPlot3.intercept);
          mPlot3.intercept = mIntersectionT;
          mApp_mData.updatePlotIntercept( mPlot3.id, TDInstance.sid, mIntersectionT );
        }
        makeSinglelegSectionReferences( list, mPlot3.intercept );
      }
    }
  }

  /** start the sketch display 
   * @param do_load whether to load plot from file
   * @param tt      used only by leg x-sections when created to insert leg intersection point
   * @param center  plot center - for multileg x-section
   * @note called by onCreate, switchPlotType, onBackPressed and pushInfo
   * 
   * FIXME null ptr in 5.1.40 on ANDROID-11 at line 2507 
   */
  private void doStart( boolean do_load, float tt, Vector3D center )
  {
    if ( mApp_mData == null ) {
      TDLog.e("DrawingWindow start with null DB");
      finish();
      return;
    }
    // assert( mLastLinePath == null); // not needed - guaranteed by callers
    mIntersectionT = tt;
    // TDLog.Log( TDLog.LOG_PLOT, "do Start() " + mName1 + " " + mName2 );

    // mCurrentPoint = ( BrushManager.isPointEnabled(  SymbolLibrary.LABEL  ) )?  1 : 0;
    // mCurrentLine  = ( BrushManager.isLineEnabled( SymbolLibrary.WALL ) )?  1 : 0;
    // mCurrentArea  = ( BrushManager.isAreaEnabled( SymbolLibrary.WATER ) )?  1 : 0;

    // mDrawingState.reset();

    setButtonJoin();
    setButtonTool();

    boolean is_section = PlotType.isAnySection( mType );
    List< DBlock > list = is_section ?  getXSectionShots( mType, mFrom, mTo ) : mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );

    // TDLog.TimeEnd( "before load" );

    if ( do_load ) {
      if ( ! loadFiles( mType, list ) ) {
        TDToast.makeBad( R.string.plot_not_found );
        if  ( tt >= 0 ) { // if failed to load x-section file
          popInfo();
          doStart( false, -1, null );
          // FIXME_POP-INFO recomputeReferences( mNum, mZoom );
          return;
        } else {
	  finish();
        }
      }
    }

    setPlotType( mType, do_load ); // if loaded update display-params
    // TDLog.TimeEnd( "after load" );

    // There are four types of sections:
    // SECTION and H_SECTION: mFrom != null, mTo != null, splays and leg
    // X_SECTION, XH_SECTION: mFrom != null, mTo == null, splays only 

    if ( is_section ) {
      // pointSelected( mCurrentPoint, true );
      // areaSelected( mCurrentArea, true );
      if ( BrushManager.isLineSection( mCurrentLine ) ) { // line must be last
        mCurrentLine  = ( BrushManager.isLineEnabled( SymbolLibrary.WALL ) )?  1 : 0;
        lineSelected( mCurrentLine, true );
        int k = getCurrentLineIndex();       // FIXME SECTION LINE
        // TDLog.v("highlight index " + k );
        setHighlight( SymbolType.LINE, k );
      } else {
        // TDLog.v("current line " + mCurrentLine + " not section" );
      }

      if ( PlotType.isMultilegSection( mType, mTo ) ) {
        // TDLog.v("PLOT start multileg list " + list.size() );
        if ( center != null ) {
          // TDLog.v("PLOT do start center " + center.x + " " + center.y + " " + center.z );
          mPlot3.center = center;
          mApp_mData.updatePlotCenter( mPlot3.id, mSid, center );
        }
        makeMultilegSectionReferences( list, mPlot3.center );
      } else {
        // TDLog.v("do start section T " + tt + " " + mPlot3.intercept );
        if ( tt != mPlot3.intercept ) {
          // TDLog.v( "do start section - update plot intercept T " + tt + " " + mPlot3.intercept );
          mApp_mData.updatePlotIntercept( mPlot3.id, mSid, tt );
          mPlot3.intercept = tt;
        }
        // FIXME MOVED_BACK_IN DrawingUtil.addGrid( -10, 10, -10, 10, 0.0f, 0.0f, mDrawingSurface );
        makeSinglelegSectionReferences( list, mPlot3.intercept );
      } 
    }
    // TDLog.TimeEnd("do start done");

    mDrawingSurface.setSelectMode( mSelectMode );
  }

  // private void makeXSectionLegPoint( float x, float y )
  // {
  //   DrawingSpecialPath path = new DrawingSpecialPath( DrawingSpecialPath.SPECIAL_DOT, DrawingUtil.toSceneX(x,y), DrawingUtil.toSceneY(x,y), DrawingLevel.LEVEL_DEFAULT );
  //   mDrawingSurface.addDrawingPath( path );
  // }

  /** make the reference for a leg/at-station xsection
   * @param list   list of shots of the section
   * @param tt     abscissa of the leg intercept
   * @note called by doRestart, doStart, doRecover
   */
  private void makeSinglelegSectionReferences( List< DBlock > list, float tt )
  {

    // assert( mLastLinePath == null); // not needed - guaranteed by callers
    mDrawingSurface.newReferences( DrawingSurface.DRAWING_SECTION, (int)mType );
    // TDLog.v( "section list " + list.size() + " tt " + tt + " azimuth " + mAzimuth + " clino " + mClino );
    // 2023-01-31 use survey declination for PLOT_H_SECTION
    float decl = ( mType == PlotType.PLOT_H_SECTION )? mApp_mData.getSurveyDeclination( mSid ) : 0;
    mDrawingSurface.addScaleRef( DrawingSurface.DRAWING_SECTION, (int)mType, decl );
    float xfrom=0;
    float yfrom=0;
    float zfrom=0;
    float xto=0;
    float yto=0;
    float zto=0;
    // normal, horizontal and cross-product
    float mc = mClino   * TDMath.DEG2RAD;
    float ma = mAzimuth * TDMath.DEG2RAD;
    // canvas X-axis, unit horizontal axis: 90 degrees to the right of the azimuth
    //   azimuth = 0 (north) --> horizontal = ( 0N, 1E)
    //   azimuth = 90 (east) --> horizontal = (-1N, 0E)
    //   etc.
    // canvas UP-axis: this is X0 ^ X1 : it goes up in the section plane 
    // canvas Y-axis = - UP-axis

    TDVector V0 = new TDVector( (float)(Math.cos(ma)*Math.cos(mc)), (float)(Math.sin(ma)*Math.cos(mc)), (float)Math.sin(mc) ); // normal to the x-section plane
    // V1,V2 are the frame of reference in the x-section plane
    TDVector V1 = new TDVector( - (float)Math.sin( ma ), (float)Math.cos( ma ), 0 );
    TDVector V2 = V0.cross( V1 );

    float dist = 0;
    DBlock blk = null;
    float xn = 0;  // X-North // Rotate as NORTH is upward
    float yn = -1; // Y-North

    float xc = 0; // center
    float yc = 0;

    if ( PlotType.isLegSection( mType ) ) {
      if ( mType == PlotType.PLOT_H_SECTION ) {
        if ( Math.abs( mClino ) > TDSetting.mHThreshold ) { // north arrow == (1,0,0), 5 m long in the CS plane
          // 2023-01-31 north is shown in scalebar
          xn =  V1.x;
          yn = -V2.x; 
          float d = 2 / (float)Math.sqrt(xn*xn + yn*yn);
          // if ( mClino > 0 ) xn = -xn;
          if ( mClino > 0 ) decl = -decl;
          // TDLog.v("H-SECTION clino " + mClino + ": " + xn + " " + yn + " decl " + decl );
          // if ( mLandscape ) {
          //   addFixedSpecial( -d, 0, 0, 0, decl);
          // } else {
            addFixedSpecial( 0, -d, 0, 0, decl ); 
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
      if ( blk != null ) { // test blk.isCommenetd() ?
        float dfrom = dist;
        float dto   = 0;
        if ( tt >= 0 && tt <= 1 ) {
          dfrom = dist * tt;
          dto   = dist * ( 1 - tt );

          DrawingSpecialPath path = new DrawingSpecialPath( DrawingSpecialPath.SPECIAL_DOT, DrawingUtil.toSceneX(xc,yc), DrawingUtil.toSceneY(xc,yc),
                                                            DrawingLevel.LEVEL_DEFAULT, mDrawingSurface.scrapIndex() );
          mDrawingSurface.addDrawingDotPath( path );
        }

        TDVector v = blk.getUnitVector( );
        xfrom = -dfrom * v.dot(V1); // neg. because it is the FROM point
        yfrom =  dfrom * v.dot(V2);
        zfrom =  dfrom * v.dot(V0);
        xto =  dto * v.dot(V1);
        yto = -dto * v.dot(V2);
        zto = -dto * v.dot(V0);

        // TDLog.v( "PLOT tt " + tt + " X " + xfrom + " " + xto + " Y " + yfrom + " " + yto );

        if ( mType == PlotType.PLOT_H_SECTION ) { // Rotate as NORTH is upward
          float xx = -yn * xfrom + xn * yfrom;
          yfrom = -xn * xfrom - yn * yfrom;
          xfrom = xx;
          xx  = -yn * xto + xn * yto;
          yto = -xn * xto - yn * yto;
          xto = xx;
        }
        addFixedLine( mType, blk, xfrom, yfrom, xto, yto, blk.getReducedExtend(), false, false ); // not-splay, not-selectable
        mDrawingSurface.addDrawingStationName( mFrom, DrawingUtil.toSceneX(xfrom, yfrom), DrawingUtil.toSceneY(xfrom, yfrom) );
        mDrawingSurface.addDrawingStationName( mTo, DrawingUtil.toSceneX(xto, yto), DrawingUtil.toSceneY(xto, yto) );

      }
    } else { // if ( PlotType.isStationSection( mType ) ) 
      mDrawingSurface.addDrawingStationName( mFrom, DrawingUtil.toSceneX(xfrom, yfrom), DrawingUtil.toSceneY(xfrom, yfrom) );
    }
    
    // TDLog.v( "From " + xfrom + " " + yfrom + " " + zfrom );
    // TDLog.v( "To   " + xto + " " + yto + " " + zto );

    // using distance of splay endpoint from xsection plane
    // V0 = normal to the plane

    int cnt = 0;
    float x0 = 0;
    float y0 = 0;
    float z0 = 0;
    for ( DBlock b : list ) { // repeat for splays
      ++cnt;
      if ( ! b.isSplay() ) {
        // TDLog.v("cnt " + cnt + " is splay " + b.isSplay() + " " + b.getBlockType() );
        continue;
      }
  
      int splay_station = 3; // could use a boolean
      if ( b.mFrom.equals( mFrom ) ) {
        splay_station = 1;
        x0 = xfrom;
        y0 = yfrom;
        z0 = zfrom;
        // if ( TDSetting.mSectionStations == 2 ) continue;
      } else if ( b.mFrom.equals( mTo ) ) {
        splay_station = 2;
        x0 = xto;
        y0 = yto;
        z0 = zto;
        // if ( TDSetting.mSectionStations == 1 ) continue;
      } else {
        continue;
      }

      float d = b.mLength;
      TDVector v = b.getUnitVector( );

      float x =  d * v.dot(V1);
      float y = -d * v.dot(V2);
      // float a = 90 - (float)(Math.acos( v.dot(V0) ) * TDMath.RAD2DEG); // cos-angle with the normal

      // splay endpoint
      x0 += d * v.dot(V1);
      y0 += d * v.dot(V2);
      z0 += d * v.dot(V0);
      float d0 = ( x0*x0 + y0*y0 + z0*z0 );
      float a = (d0 > 0)? 90 - TDMath.acosd( z0 / TDMath.sqrt( d0 ) ) : 90; // angle with plane [degrees]
      
      if ( mType == PlotType.PLOT_H_SECTION ) { // Rotate as NORTH is upward
        float xx = -yn * x + xn * y;
        y = -xn * x - yn * y;
        x = xx;
      }
      // TDLog.v( "splay " + d + " " + b.mBearing + " " + b.mClino + " coord " + X + " " + Y + " " + Z );
      if ( splay_station == 1 ) {
        // N.B. this must be guaranteed for X_SECTION
        // addFixedSectionSplay( b, xfrom, yfrom, xfrom+x, yfrom+y, 0, 0, false );
        addFixedSectionSplay( b, xfrom, yfrom, xfrom+x, yfrom+y, a, false );
      } else { // if ( splay_station == 2
        // addFixedSectionSplay( b, xto, yto, xto+x, yto+y, 0, 0, true );
        addFixedSectionSplay( b, xto, yto, xto+x, yto+y, a, true );
      }
    }
    // mSectionSkip = cnt;
    // mDrawingSurface.setScaleBar( mCenter.x, mCenter.y ); // (90,160) center of the drawing

    // TDLog.v("X0 " + x0 + " " + y0 + " grid unit " + TDSetting.mUnitGrid );
    DrawingUtil.addGrid( -10, 10, -10, 10, xc, yc, 0, 0, mDrawingSurface ); // FIXME_SK moved out
    mDrawingSurface.commitReferences();
  }

  /** make the reference for a multileg xsection
   * @param list   list of shots of the section
   * @param center center
   * @note called by doRestart, doStart, doRecover
   */
  private void makeMultilegSectionReferences( List< DBlock > list, Vector3D center )
  {
    // assert( mLastLinePath == null); // not needed - guaranteed by callers
    // TDLog.v("PLOT multileg make section reference " + mAzimuth + " " + mClino + " list " + list.size() );
    mDrawingSurface.newReferences( DrawingSurface.DRAWING_SECTION, (int)mType );
    DrawingUtil.addGrid( -10, 10, -10, 10, 0.0f, 0.0f, mDrawingSurface ); // FIXME_SK moved out
    mDrawingSurface.addScaleRef( DrawingSurface.DRAWING_SECTION, (int)mType, 0 );
    float xfrom=0;
    float yfrom=0;
    float zfrom=0;
    float xto=0;
    float yto=0;
    float zto=0;
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

    TDVector V0 = new TDVector( (float)(Math.cos(ma)*Math.cos(mc)), (float)(Math.sin(ma)*Math.cos(mc)), (float)Math.sin(mc) ); // normal to the x-section plane
    // V1,V2 are the frame of reference in the x-section plane
    TDVector V1 = new TDVector( - (float)Math.sin( ma ), (float)Math.cos( ma ), 0 ); // N,E,Up
    TDVector V2 = V0.cross( V1 ); 

    float dist = 0;
    DBlock blk = null;
    // float xn = 0;  // X-North // Rotate as NORTH is upward
    // float yn = -1; // Y-North
    float xtt = (float)center.x; // x-section center East
    float ytt = (float)center.y; // South
    float ztt = (float)center.z; // Down
    for ( DBlock b : list ) {
      if ( b.isSplay() ) { // test b.isCommented() ? // FIXME_COMMENTED
        // TDLog.v("multileg splay block " + b.mFrom );
        NumStation st_f = mNum.getStation( b.mFrom );
        NumSplay sp = mNum.getSplayOf( b );
        if ( st_f != null && sp != null ) {
          TDVector vf = new TDVector( ytt - (float)st_f.s, (float)st_f.e - xtt, ztt - (float)st_f.v ); // N,E,Up
          TDVector vt = new TDVector( ytt - (float)sp.s,   (float)sp.e - xtt,   ztt - (float)sp.v );
          TDVector vft = new TDVector( (float)st_f.s - (float)sp.s, (float)sp.e - (float)st_f.e, (float)st_f.v - (float)sp.v );
          float cosine = vft.dot(V0)/vft.length();
          xfrom =   vf.dot(V1); 
          yfrom = - vf.dot(V2);
          xto   =   vt.dot(V1); 
          yto   = - vt.dot(V2);
          // TDLog.v("leg " + b.mFrom + " " + xfrom + " " + yfrom + " - " + b.mTo + " " + xto + " " + yto + " cosine " + cosine );
          addFixedLine( mType, b, xfrom, yfrom, xto, yto, cosine, true, false ); // splay, not-selectable
        } else {
          TDLog.e( "splay block without station " + b.mFrom );
        }
      } else {
        // TDLog.v("multileg leg block " + b.mFrom + " " + b.mTo );
        NumStation st_f = mNum.getStation( b.mFrom );
        NumStation st_t = mNum.getStation( b.mTo );
        if ( st_f != null && st_t != null ) {
          TDVector vf = new TDVector( ytt - (float)st_f.s, (float)st_f.e - xtt, ztt - (float)st_f.v ); // N,E,Up
          TDVector vt = new TDVector( ytt - (float)st_t.s, (float)st_t.e - xtt, ztt - (float)st_t.v );
          float af = vf.dot( V0 );
          float at = vt.dot( V0 );
          // intercept   F ----- X ----- T
          //                af      at
          float tt = Math.abs( af / ( af - at ) );

          xfrom =   vf.dot(V1); 
          yfrom = - vf.dot(V2);
          xto   =   vt.dot(V1); 
          yto   = - vt.dot(V2);
          // TDLog.v("leg " + b.mFrom + " " + xfrom + " " + yfrom + " - " + b.mTo + " " + xto + " " + yto );
          addFixedLine( mType, b, xfrom, yfrom, xto, yto, 1.0f, false, false ); // cosine 1.0 not used, not-splay, not-selectable
          mDrawingSurface.addDrawingStationName( b.mFrom, DrawingUtil.toSceneX(xfrom, yfrom), DrawingUtil.toSceneY(xfrom, yfrom) );
          mDrawingSurface.addDrawingStationName( b.mTo, DrawingUtil.toSceneX(xto, yto), DrawingUtil.toSceneY(xto, yto) );
          if ( tt >= 0 && tt <= 1 ) {
            float xt = xfrom + tt * ( xto - xfrom );
            float yt = yfrom + tt * ( yto - yfrom );
            // float zt = zfrom + tt * ( zto - zfrom );
            // zfrom =  dist * v.dot(V0);
            if ( mLandscape ) { float t=xtt; xtt=-ytt; ytt=t; }
            DrawingSpecialPath path = new DrawingSpecialPath( DrawingSpecialPath.SPECIAL_DOT, DrawingUtil.toSceneX(xt,yt), DrawingUtil.toSceneY(xt,yt), DrawingLevel.LEVEL_DEFAULT, mDrawingSurface.scrapIndex() );
            mDrawingSurface.addDrawingDotPath( path );
          }
        } else {
          TDLog.e( "leg block without station " + b.mFrom + " " + b.mTo );
        }
      }
    }
    mDrawingSurface.commitReferences();
  }

  /** read the plot from file(s) - the file name(s) is taken from the plot names stored in the object
   * @param type    plot type
   * @param list    shots list
   * @return true on success
   */
  private boolean loadFiles( long type, List< DBlock > list )
  {
    // TDLog.v( "load files - type " + type );
    // assert( mLastLinePath == null ); // guaranteed when called
    // String filename1  = null;
    String filename1b = null;
    // String filename2  = null;
    String filename2b = null;
    // String filename3  = null;
    String filename3b = null;

    if ( PlotType.isSketch2D( type ) ) {
      // TDLog.v( "load files type " + type + " " + mName1 + " " + mName2 );
      mPlot1 = mApp_mData.getPlotInfo( mSid, mName1 );
      mPlot2 = mApp_mData.getPlotInfo( mSid, mName2 );
      if ( mPlot1 == null ) return false;
      if ( mPlot2 == null ) return false;
      mPid1  = mPlot1.id;
      mPid2  = mPlot2.id;
      // TDLog.v( "Plot2 type " + mPlot2.type + " azimuth " + mPlot2.azimuth );
      mPid = mPid1;
      // filename1  = TDPath.getTh2FileWithExt( mFullName1 );
      filename1b = TDPath.getTdrFileWithExt( mFullName1 );
      // filename2  = TDPath.getTh2FileWithExt( mFullName2 );
      filename2b = TDPath.getTdrFileWithExt( mFullName2 );
      // TDLog.v( "PLOT load files " + filename1b + " " + filename2b );
    } else {
      mPlot3 = mApp_mData.getPlotInfo( mSid, mName3 );
      if ( mPlot3 == null ) return false;
      mPid3  = mPlot3.id;
      // TDLog.v( "load files type " + type + " " + mName3 + " pid " + mPid3 );
      // filename3  = TDPath.getTh2FileWithExt( mFullName3 );
      filename3b = TDPath.getTdrFileWithExt( mFullName3 );
      // TDLog.Log( TDLog.LOG_PLOT, "load file " + filename3b );
    }

    // mAllSymbols  = true; // by default there are all the symbols
    // FIXME_MISSING SymbolsPalette missingSymbols = null; // new SymbolsPalette(); 
    // missingSymbols = palette of missing symbols
    // if there are missing symbols mAllSymbols is false and the MissingDialog is shown
    //    (the dialog just warns the user about missing symbols, maybe a Toast would be enough)
    // when the sketch is saved, mAllSymbols is checked ( see doSaveTdr )
    // if there are not all symbols the user is asked if he/she wants to save anyways

    if ( PlotType.isSketch2D( type ) ) {
      if ( list.size() > 0 ) {
        // TDLog.v( "data reduction " + list.size() + " start at " + mPlot1.start );
        mMultiBad = new ArrayList< StringPair >();
        mNum = new TDNum( list, mPlot1.start, mDecl, mFormatClosure );
        if ( type == PlotType.PLOT_PLAN ) { // 20250321
          mNum.setBarrierAndHidden( mPlot1.view, mPlot1.hide );
        } else {
          mNum.setBarrierAndHidden( mPlot2.view, mPlot2.hide );
        }
        if ( TDSetting.mLoopClosure == TDSetting.LOOP_SELECTIVE ) setMenuImageRed( mNum.nrInaccurateLoops > 0 );
      } else {
        mNum = null;
        // TDToast.makeBad( R.string.few_data );
        // if ( mPid1 >= 0 ) mApp_mData.dropPlot( mPid1, mSid );
        // if ( mPid2 >= 0 ) mApp_mData.dropPlot( mPid2, mSid );
        // finish();
      }

      if ( ! mDrawingSurface.resetManager( DrawingSurface.DRAWING_PLAN, mFullName1, false ) ) {
        // TDLog.v( "modeload data stream 1 " + mName1 + " " + mFullName1);
        // mAllSymbols =
        mDrawingSurface.modeloadDataStream( filename1b, mFullName1, false /*, FIXME-MISSING missingSymbols */ );
        // DrawingSurface.addManagerToCache( mFullName1 );
      }
      if ( ! mDrawingSurface.resetManager( DrawingSurface.DRAWING_PROFILE, mFullName2, PlotType.isExtended(mPlot2.type) ) ) {
        // TDLog.v( "modeload data stream 2");
        // mAllSymbols = mAllSymbols &&
        mDrawingSurface.modeloadDataStream( filename2b, mFullName2, false /*, FIXME-MISSING missingSymbols */ );
        // DrawingSurface.addManagerToCache( mFullName2 );
      }
      
      String parent = ( TDInstance.xsections? null : mName);
      List< PlotInfo > xsection_plan = mApp_mData.selectAllPlotSectionsWithType( TDInstance.sid, 0, PlotType.PLOT_X_SECTION,  parent );
      List< PlotInfo > xsection_ext  = mApp_mData.selectAllPlotSectionsWithType( TDInstance.sid, 0, PlotType.PLOT_XH_SECTION, parent );

      if ( mNum == null ) {
        TDToast.makeBad( R.string.survey_no_data_reduction );
      } else {
        mNum.setBarrierAndHidden( mPlot2.view, mPlot2.hide ); // 20250321
        computeReferences( mNum, mPlot2.type, mPlot2.name, mZoom, true );

        mNum.setBarrierAndHidden( mPlot1.view, mPlot1.hide ); // 20250321
        computeReferences( mNum, mPlot1.type, mPlot1.name, mZoom, false );
      }

      doMoveTo();

      mDrawingSurface.setStationXSections( xsection_plan, xsection_ext, mPlot2.type );
      mDrawingSurface.linkAllSections( mName1, mName2 );
      // if ( TDSetting.mFixmeXSection ) { // add all section point XSection outlines 
        // the XSection outlines are stored in the command manager: they must be loaded separately for command managers 1 and 2
        mDrawingSurface.setAllXSectionOutlines( this, 1 );
        mDrawingSurface.setAllXSectionOutlines( this, 2 );
      // }
    } else { // X_SECTION
      resetReference( mPlot3, true );
      mTo = ( PlotType.isLegSection( type ) )? mPlot3.view : "";
      mDrawingSurface.resetManager( DrawingSurface.DRAWING_SECTION, null, false );
      // mAllSymbols =
      mDrawingSurface.modeloadDataStream( filename3b, null, false /*, FIXME-MISSING missingSymbols */ );
      mDrawingSurface.addScaleRef( DrawingSurface.DRAWING_SECTION, (int)type, 0 );
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

  /** set the plot type
   * @param type    plot type
   * @param params  whether to update display params
   * called by doResume and doStart
   */
  private void setPlotType( long type, boolean params )
  {
    // assert( mLastLinePath == null );
    if ( PlotType.isProfile( type ) ) {
      setPlotType2( COMPUTE_NO, params );
    } else if ( type == PlotType.PLOT_PLAN ) { 
      setPlotType1( COMPUTE_NO, params );
    } else {
      setPlotType3( params );
    }
  }

  /** save the current reference
   */
  private void updateReference()
  {
    // mLastLinePath = null; // should not be necessary
    // TDLog.v( "update Reference()" );
    if ( mType == PlotType.PLOT_PLAN ) {
      saveReference( mPlot1, mPid1 );
    } else if ( PlotType.isProfile( mType ) ) {
      saveReference( mPlot2, mPid2 );
    }
  }

  /** save the current reference in the plot info struct (and the database)
   * @param plot    sketch info struct
   * @param pid     plot ID
   * @note called by updateReference and moveTo
   */
  private void saveReference( PlotInfo plot, long pid )
  {
    // TDLog.v( "save Reference()" );
    // TDLog.v( "PLOT save " + pid + " ref: " + mOffset.x + " " + mOffset.y + " " + mZoom );
    plot.xoffset = mOffset.x;
    plot.yoffset = mOffset.y;
    plot.zoom    = mZoom;
    mApp_mData.updatePlot( pid, mSid, mOffset.x, mOffset.y, mZoom );
  }

  /** restore the current reference from the plot info struct
   * @param plot    sketch info struct
   * @param params  whether to update XY-zoom params by the plot
   */
  private void resetReference( PlotInfo plot, boolean params )
  {
    // mLastLinePath = null;
    if ( params && plot != null ) { // TH2EDIT no "&& plot != null"
      mOffset.x = plot.xoffset; 
      mOffset.y = plot.yoffset; 
      mZoom     = plot.zoom;
    }
    // TDLog.v( "PLOT reset ref " + params + ": " + mOffset.x + " " + mOffset.y + " " + mZoom );
    mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
  }

  // ----------------------------------------------------
  // previewPaint is not thread safe, but it is ok if two threads make two preview paints
  // eventually only one remains
  static private Paint previewPaint = null;

  /** @return the preview paint
   * @note the preview paint is a static object created when this method is called the first time
   */
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

  /** select at a scene point (x,y)
   * @param x    X-coord of the scene point
   * @param y    Y-coord of the scene point
   * @param size selection radius
   * @note called only onTouchUp
   */
  private void doSelectAt( float x, float y, float size )
  {
    // assert( mLastLinePath == null );
    if ( mLandscape ) { float t=x; x=-y; y=t; }
    // TDLog.v( "select at: edit-range " + mDoEditRange + " mode " + mMode + " At " + x + " " + y );
    if ( mMode == MODE_EDIT ) {
      if ( TDLevel.overExpert ) {
        // PATH_MULTISELECTION
        // if ( mDrawingSurface.isMultiselection() ) {
        //   mDrawingSurface.addItemAt( x, y, mZoom, size );
        //   return;
        // }
        if ( SelectionRange.isRange( mDoEditRange ) ) {
          // setButton3( BTN_BORDER, mBMedit_no );
          if ( mDrawingSurface.setRangeAt( x, y, mZoom, mDoEditRange, size ) ) {
            mMode = MODE_SHIFT;
            return;
          }
        }
      } 
      // float d0 = TopoDroidApp.mCloseCutoff + TopoDroidApp.mSelectness / mZoom;
      SelectionSet selection = mDrawingSurface.getItemsAt( x, y, mZoom, mSelectMode, size );
      mHasSelected = mDrawingSurface.hasSelected();
      setButton3PrevNext();
      // TDLog.v( "selection at " + x + " " + y + " items " + selection.size() + " has selected " + mHasSelected );
      // TDLog.v( " zoom " + mZoom + " radius " + d0 );
      if ( mHasSelected ) {
        if ( SelectionRange.isPoint( mDoEditRange ) ) {
          mMode = MODE_SHIFT;
	} else if ( SelectionRange.isItem( mDoEditRange ) ) {
          mDrawingSurface.setRangeAt( x, y, mZoom, mDoEditRange, size );
          mMode = MODE_SHIFT;
        }
        setButton3Item( selection.mHotItem );
      } else {
        setButton3Item( null );
      }
    } 
  }

  /** execute an erase action
   * @param x    X coordinate of the erase action (scene frame)
   * @param y    Y coordinate of the erase action (scene frame)
   */
  private void doEraseAt( float x, float y )
  {
    if ( mLandscape ) { float t=x; x=-y; y=t; }
    mDrawingSurface.eraseAt( x, y, mZoom, mEraseCommand, mEraseMode, mEraseSize );
    modified();
  }

  /** update the name of a data block
   * @param block   shot data block
   * @param from    new FROM station 
   * @param to      new TO station 
   */
  void updateBlockName( DBlock block, String from, String to )
  {
    // if ( mFullName2 == null ) return; // nothing for PLOT_SECTION or PLOT_H_SECTION
    if ( PlotType.isAnySection( mType ) )  return;
    // FIXME if ( from == null || to == null ) return;

    if ( from == null ) from = "";
    if ( to   == null ) to = "";
    if ( from.equals(block.mFrom) && to.equals( block.mTo ) ) return;

    block.mFrom = from;
    block.mTo   = to;
    if ( mApp_mData.checkSiblings( block.mId, mSid, from, to, block.mLength, block.mBearing, block.mClino ) ) { // bad sibling
      TDToast.makeWarn( R.string.bad_sibling );
    }
    mApp_mData.updateShotName( block.mId, mSid, from, to );
    doComputeReferences( true );
    // TDLog.v( "PLOT update blk name " + mOffset.x + " " + mOffset.y + " " + mZoom );
    mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    modified();
  }
 
  /** update the comment of a data block
   * @param block   shot data block
   * @param comment new comment
   */
  void updateBlockComment( DBlock block, String comment ) 
  {
    if ( comment.equals( block.mComment ) ) return;
    block.mComment = comment;
    mApp_mData.updateShotComment( block.mId, mSid, comment );
  }

  /** update the flag of a data block
   * @param blk   shot data block
   * @param flag  new flag (without cavway bits)
   * @param shot  drawing path of the data block
   * @note called by DrawingShotDialog with the flag 
   */
  void updateBlockFlag( DBlock blk, long flag, DrawingPath shot )
  {
    TDLog.v("update block flag: " + blk.getFlag() + "/" + blk.cavwayFlag() + " -> " + flag );
    if ( blk.getFlag() == flag ) return;
    flag = blk.resetFlag( flag ); // return the new shot flag, including old cavway bits
    // the next is really necessary only if flag || mFlag is FLAG_COMMENTED:
    if ( shot instanceof DrawingSplayPath ) {
      ((DrawingSplayPath)shot).setSplayPathPaint( mType, blk );
    } else {
      /* nothing yet */
    }
    mApp_mData.updateShotFlag( blk.mId, mSid, flag );
  }
  
  /** update the data block leg to NORMAL
   * @param blk   shot data block
   * @param shot  drawing path of the data block
   */
  void clearBlockSplayLeg( DBlock blk, DrawingPath shot )
  {
    // TDLog.v( "clear splay leg " + blk.mId + "/" + mSid + " reset shot paint ");
    blk.setTypeSplay();
    if ( shot.mBlock != null ) shot.mBlock.setTypeSplay();
    mApp_mData.updateShotLeg( blk.mId, mSid, LegType.NORMAL );
    // the next is really necessary only if flag || mFlag is FLAG_COMMENTED:
    if ( shot instanceof DrawingSplayPath ) {
      ((DrawingSplayPath)shot).setSplayPathPaint( mType, blk );
    }
  }

  /** update the "extend" of a data block
   * @param block   shot data block
   * @param extend  integer extend
   * @param stretch fractional part of the extend
   * @note called be DrawingShotDialog and onTouch
   */
  void updateBlockExtend( DBlock block, int extend, float stretch )
  {
    // if ( ! block.isSplay() ) extend -= ExtendType.EXTEND_FVERT;
    if ( block.getIntExtend() == extend && block.hasStretch( stretch ) ) return;
    block.setExtend( extend, stretch );
    mApp_mData.updateShotExtend( block.mId, mSid, extend, stretch );
    recomputeProfileReference();
  }

  /** recompute the plot centerline in profile view - only PLOT_EXTENDED ( not PLOT_PROJECTED )
   * @note used only when a shot extend is changed
   */
  private void recomputeProfileReference()
  {
    // assert( mLastLinePath == null );
    if ( mType == PlotType.PLOT_EXTENDED ) { 
      List< DBlock > list = mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );
      mMultiBad = new ArrayList< StringPair >();
      mNum = new TDNum( list, mPlot1.start, mDecl, mFormatClosure );
      mNum.setBarrierAndHidden( mPlot2.view, mPlot2.hide ); // 20250321

      if ( TDSetting.mLoopClosure == TDSetting.LOOP_SELECTIVE ) setMenuImageRed( mNum.nrInaccurateLoops > 0 );
      mDrawingSurface.clearShotsAndStations( (int)mType );
      computeReferences( mNum, (int)mType, mName, TopoDroidApp.mScaleFactor, false );
      // TDLog.v( "PLOT profile ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
      mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
      modified();
    } 
  }

  /** delete a splay
   * @param p    drawing path of the splay
   * @param sp   selected point of the shot
   * @param blk  shot data block
   */
  private void deleteSplay( DrawingSplayPath p, SelectionPoint sp, DBlock blk )
  {
    mDrawingSurface.deleteSplay( p, sp ); 
    mApp_mData.deleteShot( blk.mId, TDInstance.sid, TDStatus.DELETED );
    if ( TopoDroidApp.mShotWindow != null ) {
      TopoDroidApp.mShotWindow.updateDisplay(); // FIXME ???
    }
  }

  /** delete a drawing point
   * @param point   drawing point item
   */
  private void deletePoint( DrawingPointPath point )
  {
    if ( point == null ) return;
    // assert( mLastLinePath == null );
    mDrawingSurface.deletePath( point ); 
    // TDLog.v( "delete point type " + point.mPointType );
    if ( point instanceof DrawingPhotoPath ) { 
      DrawingPhotoPath photo = (DrawingPhotoPath)point;
      if ( photo.mPicture != null ) mDrawingSurface.deletePath( photo.mPicture ); // delete associated picture
      photo.destructor();
    } else if ( point instanceof DrawingAudioPath ) { 
      DrawingAudioPath audio = (DrawingAudioPath)point;
      audio.destructor();
    // } else if ( point instanceof DrawingSensorPath ) { 
    //   DrawingSensorPath sensor = (DrawingSensorPath)point;
    //   mApp_mData.deleteSensorRecord( TDInstance.sid, sensor.mId );
    } else if ( BrushManager.isPointSection( point.mPointType ) ) {
      String section = point.getOption( TDString.OPTION_SCRAP );
      if ( section != null ) {
        TDLog.v("Delete section point: Clear XSection outline: " + section );
        mDrawingSurface.clearXSectionOutline( TDUtil.replacePrefix( TDInstance.survey, section ) );
      }
    }
    modified();
  }

  /** split a line
   * @param line   drawing line item
   * @param point  splitting point
   */
  private void splitLine( DrawingLinePath line, LinePoint point )
  {
    mDrawingSurface.splitLine( line, point );
    // assert( mLastLinePath == null );
    modified();
  }

  /** remove a line point
   * @param line   drawing line item
   * @param point  point to remove
   * @param sp     selected point of the point to remove
   */
  private void removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp ) 
  {
    if (  mDrawingSurface.removeLinePoint(line, point, sp) ) {
      modified();
    }
  }

  /** remove a line point
   * @param line   drawing line item
   * @param point  point to remove
   * @note used to remove the last point of a line
   */
  void removeLinePointFromSelection( DrawingLinePath line, LinePoint point )
  {
    if (  mDrawingSurface.removeLinePointFromSelection(line, point ) ) {
      modified();
    }
  }

  /** delete a line
   * @param line   drawing line item to delete
   */
  void deleteLine( DrawingLinePath line ) 
  { 
    if ( BrushManager.isLineSection( line.mLineType ) ) {
      deleteSectionLine( line );
    } else {
      mDrawingSurface.deletePath( line );
    }
    // assert( mLastLinePath == null );
    modified();
  }

  /** delete a xsection line
   * @param line   drawing line item to delete
   */
  private void deleteSectionLine( DrawingLinePath line )
  {
    String xs_id = line.getOption( "-id" );
    String scrap_name = TDInstance.survey + "-" + xs_id;
    mDrawingSurface.deleteSectionLine( line, scrap_name );
    // TDPath.deletePlotFileWithBackups( TDPath.getTh2File( scrap_name + ".th2" ) );
    TDPath.deletePlotFileWithBackups( TDPath.getTdrFile( scrap_name + ".tdr" ) );
    String jpg_path = TDPath.getJpgFile( xs_id + ".jpg" );
    if ( TDFile.hasTopoDroidFile( jpg_path ) ) {
      // TODO delete record from photos table: WHAT IS THE ID ?
      // mApp_mData.dropPhoto( TDInstance.sid, id, MediaInfo.TYPE_XSECTION );
      TDFile.deleteFile( jpg_path );
      String png_path = jpg_path.replace(".jpg", ".png");
      if ( TDFile.hasTopoDroidFile( png_path ) ) {
        TDFile.deleteFile( png_path );
      }
    }
    // section point is deleted automatically
    // deleteSectionPoint( xs_id ); // delete section point and possibly clear section outline
    mDrawingSurface.clearXSectionOutline( scrap_name ); // clear outline if any
    PlotInfo plot = mApp_mData.getPlotInfo( TDInstance.sid, xs_id );
    if ( plot != null ) {
      mApp_mData.dropPlot( plot.id, TDInstance.sid );
    } else {
      TDLog.e("Delete section line. No plot NAME " + xs_id + " SID " + TDInstance.sid );
    }
  }

  /** make a line sharper
   * @param line   drawing line item to sharpen
   */
  void sharpenLine( DrawingLinePath line )
  {
    // assert( mLastLinePath == null );
    mDrawingSurface.sharpenPointLine( line );
    modified();
  }

  /** reduce the points of a line 
   * @param line         drawing line item to reduce
   * @param decimation   out of how many points to keep one
   */
  void reduceLine( DrawingLinePath line, int decimation )
  {
    // assert( mLastLinePath == null );
    mDrawingSurface.reducePointLine( line, decimation );
    modified();
  }

  /** make a line "rocky"
   * @param line   drawing line item to "rock"
   * @note called from drawing line dialog
   */
  void rockLine( DrawingLinePath line )
  {
    // assert( mLastLinePath == null );
    mDrawingSurface.rockPointLine( line );
    modified();
  }

  /** make a line closed
   * @param line   drawing line item to close
   */
  void closeLine( DrawingLinePath line )
  {
    // assert( mLastLinePath == null );
    mDrawingSurface.closePointLine( line );
    modified();
  }

  /** reduce the points of an area border
   * @param area         drawing area item to reduce
   * @param decimation   out of how many points to keep one
   */
  void reduceArea( DrawingAreaPath area, int decimation )
  {
    // assert( mLastLinePath == null );
    mDrawingSurface.reducePointLine( area, decimation );
    modified();
  }

  /** delete an area
   * @param area   drawing area item to delete
   */
  private void deleteArea( DrawingAreaPath area )
  {
    // assert( mLastLinePath == null );
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
      // normal stylus has TYPE 1 and PRESSURE 1.0
      // sumsung pen has TYPE 2 and variable PRESSURE between 0.0 and 1.0
      sb.append(" ").append( ev.getToolType(i) );
      sb.append(" ").append( ev.getPressure(i) );
    }
    sb.append( "]" );
    TDLog.v( sb.toString() );
  }
  */

  /** @return the spacing between the first two pointers in the event
   * @param ev    screen event
   */
  private float spacing( MotionEventWrap ev )
  {
    int np = ev.getPointerCount();
    if ( np < 2 ) return 0.0f;
    float x = ev.getX(1) - ev.getX(0);
    float y = ev.getY(1) - ev.getY(0);
    return (float)Math.sqrt(x*x + y*y);
  }

  /** save the display coords of an event pointer(s)
   * @param ev    screen event
   */
  private void saveEventPoint( MotionEventWrap ev )
  {
    int np = ev.getPointerCount();
    if ( np >= 1 ) {
      mSave0X = ev.getX(0);
      mSave0Y = ev.getY(0);
      if ( np >= 2 ) {
        mSave1X = ev.getX(1);
        mSave1Y = ev.getY(1);
        if ( np >= 3 ) {
          mSave2X = ev.getX(2);
          mSave2Y = ev.getY(2);
        } else {
          // mSave2X = (mSave1X + mSave0X)/2;
          // mSave2Y = (mSave1Y + mSave0Y)/2;
        }
      } else {
        mSave1X = mSave0X;
        mSave1X = mSave0X;
        // mSave2X = mSave0X;
        // mSave2Y = mSave0Y;
      } 
    }
  }

  /** shift the canvas according to an event
   * @param ev    screen event
   */
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
      // TDLog.v( "PLOT shift event " + mOffset.x + " " + mOffset.y + " " + mZoom );
      mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    }
  }

  /** apply an affine transformation to the drawing
   * @param ev    screen event defining the transformation parameters
   *
   * x0 = a saveX0 + b saveY0 + c
   * x1 = a saveX1 + b saveY1 + c
   * x2 = a saveX2 + b saveY2 + c
   * 
   * let M = | saveX0  saveY0  1 |
   *         | saveX1  saveY1  1 |
   *         | saveX2  saveY2  1 |
   * then (a,b,c) = M^-1 ( x1, x1, x2 )
   */
  private int affineTransformByEvent( MotionEventWrap ev )
  {
    int np = ev.getPointerCount();
    if ( np < 3 ) return -1;
    float x0 = ev.getX(0);
    float y0 = ev.getY(0);
    if ( Math.abs( x0 - mSave0X ) > 32 || Math.abs( y0 - mSave0Y ) > 32 ) return -10;
    float x1 = ev.getX(1);
    float y1 = ev.getY(1);
    if ( Math.abs( x1 - mSave1X ) > 32 || Math.abs( y1 - mSave1Y ) > 32 ) return -11;
    float x2 = ev.getX(2);
    float y2 = ev.getY(2);
    if ( Math.abs( x2 - mSave2X ) > 32 || Math.abs( y2 - mSave2Y ) > 32 ) return -12;
    float det12 = mSave1X * mSave2Y - mSave1Y * mSave2X;
    float det20 = mSave2X * mSave0Y - mSave2Y * mSave0X;
    float det01 = mSave0X * mSave1Y - mSave0Y * mSave1X;
    float det = det12 + det20 + det01;
    if ( Math.abs(det) < 0.01 ) return -2;
    // M^-1 = | (saveY1 - saveY2)  (saveY2 - saveY0)  (saveY0 - saveY1) |
    //        | (saveX2 - saveX1)  (saveX0 - saveX2)  (saveX1 - saveX0) | / det
    //        |       det12              det20              det01       | 
    float minv00 = mSave1Y - mSave2Y; 
    float minv01 = mSave2Y - mSave0Y; 
    float minv02 = mSave0Y - mSave1Y; 
    float minv10 = mSave2X - mSave1X; 
    float minv11 = mSave0X - mSave2X; 
    float minv12 = mSave1X - mSave0X; 
    float a = (minv00 * x0 + minv01 * x1 + minv02 * x2)/det;
    float b = (minv10 * x0 + minv11 * x1 + minv12 * x2)/det;
    float d = (minv00 * y0 + minv01 * y1 + minv02 * y2)/det;
    float e = (minv10 * y0 + minv11 * y1 + minv12 * y2)/det;
    float c = 0;
    float f = 0;
    if ( ! TDSetting.mFullAffine ) {
      a = a * e - b * d;
      if ( a < 0.10f ) return -3; // sqrt(0.10) ~ 0.32
      if ( a > 10.0f ) return -3; // sqrt(10) ~ 3.2
      e = a = TDMath.sqrt( a );
      b = d = 0;
    } else {
      // float minv20 = det12;
      // float minv21 = det20;
      // float minv22 = det01;
      // c = (minv20 * x0 + minv21 * x1 + minv22 * x2)/det;
      // f = (minv20 * y0 + minv21 * y1 + minv22 * y2)/det;
      c = (det12 * x0 + det20 * x1 + det01 * x2)/det;
      f = (det12 * y0 + det20 * y1 + det01 * y2)/det;
    } 
    // apply affine transform to sketch
    TDLog.v("Affine Transform C " + c + " F " + f ); 
    mDrawingSurface.affineTransformDrawing( a, b, c, d, e, f, mScrapOnly );
    return 0;
  }

  /** shift the canvas
   * @param x_shift   X shift
   * @param y_shift   Y shift
   */
  private void moveCanvas( float x_shift, float y_shift )
  {
    if ( Math.abs( x_shift ) < TDSetting.mMinShift && Math.abs( y_shift ) < TDSetting.mMinShift ) {
      mOffset.x += x_shift / mZoom;                // add shift to offset
      mOffset.y += y_shift / mZoom; 
      // TDLog.v( "PLOT move event " + mOffset.x + " " + mOffset.y + " " + mZoom );
      mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    }
  }

  /** make the zoom controls visible (if enabled)
   */
  public void checkZoomBtnsCtrl()
  {
    // if ( mZoomBtnsCtrl == null ) return; // not necessary
    // FIXED_ZOOM 
    if ( mDrawingSurface.isFixedZoom() ) return;
    if ( TDSetting.mZoomCtrl == 2 && ! mZoomBtnsCtrl.isVisible() ) {
      mZoomBtnsCtrl.setVisible( true );
    }
  }

  // /** add a line to the drawing
  //  * @param lp1  line to add
  //  */
  // private void addLine( DrawingLinePath lp1 )
  // {
  //   lp1.computeUnitNormal();
  //   if ( mSymbol == SymbolType.LINE && TDSetting.mLineClose && BrushManager.isLineClosed( mCurrentLine ) ) {
  //     // mCurrentLine == lp1.mLineType 
  //     lp1.setClosed( true );
  //     lp1.closePath();
  //   }
  //   mDrawingSurface.addDrawingPath( lp1 );
  //   mLastLinePath = lp1;
  // }

  /** try to join two areas
   * @param ap1    is the area (being drawn) 
   * @param ap2    is used to get the area to extend - initialized to the current areapath
   * @return true if the area ap1 has been added to the sketch
   */
  private boolean tryAndJoinArea( DrawingAreaPath ap1, DrawingAreaPath ap2 )
  {
    if ( ! mDrawingState.isRetraceArea() ) return false;
    if ( ! TDLevel.overTester && TDSetting.isLineStyleComplex() ) return false; // FIXME ONLY DEBUG
    if ( ap1 == null ) return false;
    if ( ap2 == null ) return false;
    LinePoint p1 = ap2.first();
    LinePoint p2 = ap2.last();
    return mDrawingSurface.getAreaToContinue( ap1, p1, p2, mCurrentArea, mZoom, mSelectSize );
  }

  /** try to join two lines
   * @param lp1    is the line (being drawn) 
   * @param lp2    is used to get the line to extend - initialized to the current linepath
   * @return true if the line lp1 has been added to the sketch
   */
  private boolean tryAndJoinLine( DrawingLinePath lp1, DrawingLinePath lp2 )
  {
    if ( mDrawingState.isRetraceLine() ) return tryAndRetraceLine( lp1, lp2 );
    if ( mDrawingState.isJoinLine() ) {
      // if ( tryAndContinueLine( lp1, lp2 ) ) {
      //   addLine( lp1 );
      // }
      // return true; // nothing to add
      tryAndContinueLine( lp1, lp2 );
    }
    return false;
  }

  private boolean tryAndRetraceLine( DrawingLinePath lp1, DrawingLinePath lp2 )
  {
    // if ( ! mDrawingState.isRetraceLine() ) return false;
    if ( ! TDLevel.overTester && TDSetting.isLineStyleComplex() ) return false; // FIXME ONLY DEBUG
    if ( lp1 == null ) return false;
    if ( lp2 == null ) return false;
    // TDLog.v("Try and JOIN LINE lp1 " + lp1.size() + " lp2 " + lp2.size() );
    LinePoint p1 = lp2.first();
    LinePoint p2 = lp2.last();
    // float size = TDSetting.isLineStyleComplex()? Float.MAX_VALUE : mSelectSize;
    return mDrawingSurface.getLineToContinue( lp1, p1, p2, mCurrentLine, mZoom, mSelectSize );
  }

  // 2023-03-10 DROPPED 2023-09-23 REINSTATED
  /** try to join/continue two lines
   * @param lp1    is the line (being drawn) to modify - a copy of the current linepath
   * @param lp2    is used to get the line to join/continue - initialized to the current linepath
   * @return true is the line lp1 must be added to the sketch
   */
  private boolean tryAndContinueLine( DrawingLinePath lp1, DrawingLinePath lp2 )
  {
    if ( lp1 == null ) return false;
    if ( lp2 == null ) return true;
    DrawingLinePath line1 = mDrawingSurface.getLineToContinue( lp2.mFirst, mCurrentLine, mZoom, mSelectSize );
    DrawingLinePath line2 = mDrawingSurface.getLineToContinue( lp2.mLast, mCurrentLine, mZoom, mSelectSize );
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
    // TDLog.v( "Try to join  LP2 " + lp2.toDebugString() + " TRUE" );
    return true;
  }

  // -------------------------------------------------------------------------
  /** begin an erase command
   * @param xs   scene X coord
   * @param ys   scene Y coord
   * @param xc   canvas X coord 
   * @param yc   canvas Y coord
   */
  private void startErasing( float xs, float ys, float xc, float yc )
  {
    // assert( mLastLinePath == null );
    // TDLog.v( "Erase at " + xs + " " + ys );
    if ( mTouchMode == MODE_MOVE ) {
      mEraseCommand =  new EraseCommand();
      mDrawingSurface.setEraser( xc, yc, mEraseSize );
      doEraseAt( xs, ys );
    }
  }

  /** complete an erase command
   */
  private void finishErasing()
  {
    mDrawingSurface.endEraser();
    if ( mEraseCommand != null && mEraseCommand.size() > 0 ) {
      mEraseCommand.completeCommand();
      mDrawingSurface.addEraseCommand( mEraseCommand );
      mEraseCommand = null;
    }
  }

  /** react to a touch event
   * @param view  touched view
   * @param rawEvent raw event
   * @return ...
   *
   * @note Studio: onTouch() should call View#performClick when a click is detected
   */
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

    if ( TDSetting.mStylusOnly ) {
      int np = event.getPointerCount();
      for ( id = 0; id < np; ++id ) {
        // could use also getTouchMajor() however these methods are not supported by most devices
        TDLog.v("STYLUS tool " + id + "/" + np + " size " + rawEvent.getSize( id ) + " major " + rawEvent.getToolMajor( id ) ); 
        if ( rawEvent.getToolMajor( id ) < mStylusSizeDpi ) {
          break;
        }
      }
      if ( id == np ) {
        TDLog.v("STYLUS only: no point found");
        return true;
      }
      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        action = MotionEvent.ACTION_DOWN;
      } else if ( action == MotionEvent.ACTION_POINTER_UP) {
        action = MotionEvent.ACTION_UP;
      } else {
        // TDLog.v("on touch - unhandled action " + action );
      }
      // TDLog.v("STYLUS action " + action + " of " + act );
    } else {
      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        if ( mTouchMode == MODE_MOVE ) {
          if ( mMode == MODE_ERASE ) {
            finishErasing();
          }
        }
        mTouchMode = MODE_ZOOM;
        oldDist = spacing( event );
        saveEventPoint( event );
        mPointerDown = true;
        return true;
      } else if ( action == MotionEvent.ACTION_POINTER_UP) {
        int np = event.getPointerCount();
        if ( np > 2 ) return true;
        mTouchMode = MODE_MOVE;
        id = 1 - ((act & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT);
        // int idx = rawEvent.findPointerIndex( id );
        if ( mSymbol != SymbolType.POINT ) {
          action = MotionEvent.ACTION_DOWN; // force next case
        }
        /* fall through */
      }
    }

    float x_canvas = event.getX(id);
    float y_canvas = event.getY(id);
    float x_scene = x_canvas/mZoom - mOffset.x;
    float y_scene = y_canvas/mZoom - mOffset.y;
    // TDLog.v("STYLUS action " + action + " at " + x_canvas + " " + y_canvas + " scene " + x_scene + " " + y_scene );

    if (action == MotionEvent.ACTION_DOWN) { // ---------------------------------------- DOWN
      return onTouchDown( x_canvas, y_canvas, x_scene, y_scene );

    } else if ( action == MotionEvent.ACTION_MOVE ) { // ------------------------------- MOVE
      return onTouchMove( x_canvas, y_canvas, x_scene, y_scene, event );

    } else if (action == MotionEvent.ACTION_UP) { // ----------------------------------- UP
      return onTouchUp( x_canvas, y_canvas, x_scene, y_scene );
    } else {
      TDLog.e("on touch - unhandled action " + action );
    }
    return true;
  }

  boolean HBXP_PointDown = false; // HBXP
  /** react to a touch-up event
   * @param xc   canvas X coord 
   * @param yc   canvas Y coord
   * @param xs   scene X coord
   * @param ys   scene Y coord
   * @return ...
   */
  private boolean onTouchUp( float xc, float yc, float xs, float ys )
  {
    if ( onMenu ) {
      closeMenu();
      return true;
    }

    if ( mRotateAzimuth ) {
      mRotateAzimuth = false;
      if ( BTN_DIAL < mNrButton1 ) {
        Bitmap bm2 = mDialBitmap.getBitmap( TDAzimuth.mRefAzimuth, mButtonSize );
        setButton1( BTN_DIAL, new BitmapDrawable( getResources(), bm2 ) );
      }
    }

    // TDLog.v( "on touch up. mode " + mMode + " " + mTouchMode );
    if ( mTouchMode == MODE_ZOOM || mTouchMode == MODE_ROTATE ) {
      mTouchMode = MODE_MOVE;
    } else {
      float x_shift = xc - mSaveX; // compute shift
      float y_shift = yc - mSaveY;
      if ( mMode == MODE_DRAW ) {
        float squared_shift = x_shift*x_shift + y_shift*y_shift;

        if ( mSymbol == SymbolType.LINE || mSymbol == SymbolType.AREA ) {

          mCurrentBrush.mouseUp( mDrawingSurface.getPreviewPath(), xc, yc );
          mDrawingSurface.resetPreviewPath();

          if ( mSymbol == SymbolType.LINE ) {
            if ( mCurrentLinePath != null ) { // SAFETY CHECK
              if ( squared_shift > TDSetting.mLineSegment2 || ( mPointCnt % mLinePointStep ) > 0 ) {
                if ( ( ! TDSetting.mStylusOnly ) || squared_shift < 25 * TDSetting.mLineSegment2 ) { // STYLUS_ONLY: to intercept artifacts
                  mCurrentLinePath.addPoint( xs, ys ); // a jump in stylus points five times the line spacing
                }
              }
              if ( mLandscape ) mCurrentLinePath.landscapeToPortrait();
            }
          } else if ( mSymbol == SymbolType.AREA ) {
            if ( mCurrentAreaPath != null ) { // SAFETY CHECK
              // TDLog.v( "DX " + (xs - mCurrentAreaPath.mFirst.x) + " DY " + (ys - mCurrentAreaPath.mFirst.y ) );
              if (    PlotType.isVertical( mType )
                   && BrushManager.isAreaCloseHorizontal( mCurrentArea ) 
                   && Math.abs( ys - mCurrentAreaPath.mFirst.y ) < 10  // 10 == 0.5 meter
                ) {
                tryCloseCurrentArea( xs );
              } else {  
                // TDLog.v("NO CLOSE " + (ys - mCurrentAreaPath.mFirst.y) );
                if ( squared_shift > TDSetting.mLineSegment2 || ( mPointCnt % mLinePointStep ) > 0 ) {
                  mCurrentAreaPath.addPoint( xs, ys );
                }
              }
    	      if ( mLandscape ) mCurrentAreaPath.landscapeToPortrait();
            } 
          }
          
          if ( mPointCnt > mLinePointStep || mLinePointStep == POINT_MAX ) {
            /*{{ IF DEF_FILTER_NO_START*/
              // TDLog.v("Using DEF FILTER START");
              if ( mSymbol == SymbolType.LINE && BrushManager.isLineSection( mCurrentLine ) ) { // if ( BrushManager.isLineSection(  mCurrentLinePath.mLineType ) )
                // mLastLinePath = null;
                doSectionLine( mCurrentLinePath );
                mCurrentLinePath = null;
              } else {
                boolean add = true;
                if ( mSymbol == SymbolType.LINE ) {
                  boolean closed_line = TDSetting.mLineClose && BrushManager.isLineClosed( mCurrentLine );
                  boolean joined_line = false;
                  if ( ! closed_line ) {
                    joined_line = tryAndJoinLine( mCurrentLinePath, mCurrentLinePath );
                  }
                  if ( ! joined_line ) {
                    // TODO this is useful for STYLUS-ONLY, it might be better to skip for complex line modes
                    if ( /* TDSetting.mStylusOnly && */ TDSetting.mLineEnds > 0 ) mCurrentLinePath.dropEndPoints( TDSetting.mLineEnds );

                    DrawingLinePath lp1 = new DrawingLinePath( mCurrentLine, mDrawingSurface.scrapIndex() );
                    lp1.setOptions( BrushManager.getLineDefaultOptions( mCurrentLine ) );
                    if ( BrushManager.isLineStraight( mCurrentLine ) ) {
                      lp1.addStartPoint( mCurrentLinePath.mFirst.x, mCurrentLinePath.mFirst.y );
                      float nx = BrushManager.getLineStyleX( mCurrentLine ) * 2.0f;
                      // TDLog.v("line is straight " + nx);
                      if ( nx > 0 ) { 
                        LinePoint lp = mCurrentLinePath.mFirst;
                        float x = 0;
                        while ( lp != mCurrentLinePath.mLast ) {
                          LinePoint lpn = lp.mNext;
                          x += lp.distance( lpn );
                          if ( x > nx ) { 
                            if ( lpn == mCurrentLinePath.mLast ) {
                              lp1.addPoint( lpn.x, lpn.y );
                              break;
                            } else {
                              lp1.addPoint( lp.x, lp.y );
                            }
                            x = 0;
                          } 
                          lp = lpn;
                        }
                        if ( lp == mCurrentLinePath.mLast ) {
                          lp1.addPoint( lp.x, lp.y );
                        }
                      } else {
                        lp1.addPoint( mCurrentLinePath.mLast.x, mCurrentLinePath.mLast.y );
                      }
                      // add == true;
                    } else {
                      if ( TDSetting.isLineStyleBezier() ) {
                        add = DrawingPointLineFilter.bezier( mCurrentLinePath.mFirst, mCurrentLinePath.mLast, lp1 );
                      } else if ( TDSetting.isLineStyleSimplified() ) {
                        add = DrawingPointLineFilter.weeding( mCurrentLinePath.mFirst, mCurrentLinePath.mLast, lp1, mZoom );
                      } else {
                        // add = DrawingPointLineFilter.copy( mCurrentLinePath.mFirst, mCurrentLinePath.mLast, lp1 );
                        lp1 = mCurrentLinePath;
                      }
                    }
                    if ( lp1 != null ) {
                      // TDLog.v("line nr points " + lp1.size() );
                      if ( closed_line ) {
                        if ( lp1.size() > 3 ) {
                          lp1.setClosed( true );
                          lp1.closePath();
                        } else {
                          add = false;
                        }
                      }
                      if ( add && lp1.size() > 1 ) {
                        lp1.computeUnitNormal();
                        mDrawingSurface.addDrawingPath( lp1 );
                        // mLastLinePath = lp1;
                      }
                    }
                  }
                  mCurrentLinePath = null;
                } else { // if ( mSymbol == SymbolType.AREA )
                  if ( ! tryAndJoinArea( mCurrentAreaPath, mCurrentAreaPath ) ) {
                    DrawingAreaPath ap = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(), mName+"-a", TDSetting.mAreaBorder, mDrawingSurface.scrapIndex() ); 
                    ap.setOptions( BrushManager.getAreaDefaultOptions( mCurrentArea ) );
                    if ( TDSetting.isLineStyleBezier() ) {
                      add = DrawingPointLineFilter.bezier( mCurrentAreaPath.mFirst, mCurrentAreaPath.mLast, ap );
                    } else if ( TDSetting.isLineStyleSimplified() ) {
                      add = DrawingPointLineFilter.weeding( mCurrentAreaPath.mFirst, mCurrentAreaPath.mLast, ap, mZoom );
                    } else {
                      // add = DrawingPointLineFilter.copy( mCurrentAreaPath.mFirst, mCurrentAreaPath.mLast, ap );
                      ap = mCurrentAreaPath;
                    }
                    if ( add && ap != null && ap.size() > 2 ) {
                      ap.closePath();
                      ap.shiftShaderBy( mOffset.x, mOffset.y, mZoom );
                      mDrawingSurface.addDrawingPath( ap );
                    }
                  }
                  mCurrentAreaPath = null;
                  // mLastLinePath = null;
                }
              }
            /*{{ END DEF FILTER_START */
            /*{{ IF DEF_NO_FILTER_START
              TDLog.v("Using DEF FILTER NO START");
              if ( ! ( mSymbol == SymbolType.LINE && BrushManager.isLineSection( mCurrentLine ) ) 
                   && TDSetting.isLineStyleComplex()
                   && ( mSymbol == SymbolType.AREA || ! BrushManager.isLineStraight( mCurrentLine ) )
                 ) {
                int nPts = (mSymbol == SymbolType.LINE )? mCurrentLinePath.size() : mCurrentAreaPath.size() ; // FIXME may NullPointerException
                if ( nPts > 1 ) {
                  if ( TDSetting.isLineStyleBezier() ) {
                    ArrayList< Point2D > pts = new ArrayList<>(); // [ nPts ];
                    LinePoint lp = (mSymbol == SymbolType.LINE )? mCurrentLinePath.mFirst : mCurrentAreaPath.mFirst;
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
                      if ( mSymbol == SymbolType.LINE ) {
                        DrawingLinePath lp1 = new DrawingLinePath( mCurrentLine, mDrawingSurface.scrapIndex() );
                        lp1.setOptions( BrushManager.getLineDefaultOptions( mCurrentLine ) );
                        lp1.addStartPoint( p0.x, p0.y );
                        for (int k=0; k<k0; ++k) {
                          c = curves.get(k);
                          Point2D p1 = c.getPoint(1);
                          Point2D p2 = c.getPoint(2);
                          Point2D p3 = c.getPoint(3);
                          lp1.addPoint3(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y );
                        }
                        // boolean add_line = true; // 2023-03-10 DROPPED
                        // if ( mJoinLine > JOIN_NONE && ! BrushManager.isLineSection( mCurrentLine ) ) {
                        //   add_line = tryAndContinue( lp1, mCurrentLinePath );
                        // }
                        // if ( add_line )
                        if ( ! tryAndJoin( lp1, mCurrentLinePath ) ) {
                          addLine( lp1 );
                        // } else {
                        //   mLastLinePath = ???
                        }
                      } else { //  mSymbol == SymbolType.AREA
                        DrawingAreaPath ap = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(), mName+"-a", TDSetting.mAreaBorder, mDrawingSurface.scrapIndex() ); 
                        ap.setOptions( BrushManager.getAreaDefaultOptions( mCurrentArea ) );
                        ap.addStartPoint( p0.x, p0.y );
                        for (int k=0; k<k0; ++k) {
                          c = curves.get(k);
                          Point2D p1 = c.getPoint(1);
                          Point2D p2 = c.getPoint(2);
                          Point2D p3 = c.getPoint(3);
                          ap.addPoint3(p1.x, p1.y, p2.x, p2.y, p3.x, p3.y );
                        }
                        if ( ! tryAndJoin( ap, mCurrentAreaPath ) ) {
                          ap.closePath();
                          ap.shiftShaderBy( mOffset.x, mOffset.y, mZoom );
                          mDrawingSurface.addDrawingPath( ap );
                        }
                        mLastLinePath = null;
                        // mCurrentAreaPath = null;
                      }
                    }
                  } else { // if ( TDSetting.isLineStyleSimplified() ) 
                    Weeder weeder = new Weeder();
                    LinePoint lp = (mSymbol == SymbolType.LINE )? mCurrentLinePath.mFirst : mCurrentAreaPath.mFirst;
                    for ( ; lp != null; lp = lp.mNext ) {
                      weeder.addPoint( lp.x, lp.y );
                    }
                    // get pixels from meters
                    // float dist = mZoom*DrawingUtil.SCALE_FIX*TDSetting.mWeedDistance;
                    float dist = DrawingUtil.SCALE_FIX*TDSetting.mWeedDistance; // N.B. no zoom
                    float len  = mZoom*DrawingUtil.SCALE_FIX*TDSetting.mWeedLength;
                    // TDLog.v( "Weed dist " + dist + " len " + len );
                    ArrayList< Point2D > points = weeder.simplify( dist, len );
                    int k0 = points.size();
                    // TDLog.Log( TDLog.LOG_PLOT, " Bezier size " + k0 );
                    if ( k0 > 1 ) {
                      Point2D p0 = points.get(0);
                      if ( mSymbol == SymbolType.LINE ) {
                        DrawingLinePath lp1 = new DrawingLinePath( mCurrentLine, mDrawingSurface.scrapIndex() );
                        lp1.setOptions( BrushManager.getLineDefaultOptions( mCurrentLine ) );
                        lp1.addStartPoint( p0.x, p0.y );
                        for (int k=1; k<k0; ++k) {
                          p0 = points.get(k);
                          lp1.addPoint(p0.x, p0.y );
                        }
                        // boolean add_line = true; // 2023-03-10 DROPPEB
                        // if ( mJoinLine > JOIN_NONE && ! BrushManager.isLineSection( mCurrentLine ) ) {
                        //   add_line = tryAndContinue( lp1, mCurrentLinePath );
                        // }
                        // if ( add_line ) 
                        if ( ! tryAndJoin( lp1, mCurrentLinePath ) ) {
                          addLine( lp1 );
                        // } else {
                        //   mLastLinePath = ???
                        }
                      } else { //  mSymbol == SymbolType.AREA
                        DrawingAreaPath ap = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(), mName+"-a", TDSetting.mAreaBorder, mDrawingSurface.scrapIndex() ); 
                        ap.setOptions( BrushManager.getAreaDefaultOptions( mCurrentArea ) );
                        ap.addStartPoint( p0.x, p0.y );
                        for (int k=1; k<k0; ++k) {
                          p0 = points.get(k);
                          ap.addPoint(p0.x, p0.y );
                        }
                        if ( ! tryAndJoin( ap, mCurrentAreaPath ) ) {
                          ap.closePath();
                          ap.shiftShaderBy( mOffset.x, mOffset.y, mZoom );
                          mDrawingSurface.addDrawingPath( ap );
                        }
                        mLastLinePath = null;
                        // mCurrentAreaPath = null;
                      }
                    }
                  }
                }
              }
              else
              {
                if ( mSymbol == SymbolType.LINE ) {
                  if ( mCurrentLinePath != null ) {
                    // N.B.
                    // section direction is in the direction of the tick
                    // and splay reference are taken from the station the section looks towards
                    // section line points: right-end -- left-end -- tick-end
                    //
                    if ( BrushManager.isLineSection(  mCurrentLinePath.mLineType ) ) {
                      mLastLinePath = null;
                      doSectionLine( mCurrentLinePath );
                    } else { // not section line
                      // boolean add_line= true; // 2023-03-10 DROPPED
                      // if ( mJoinLine > JOIN_NONE && ! BrushManager.isLineSection( mCurrentLine ) ) {
                      //   add_line = tryAndContinue( mCurrentLinePath, mCurrentLinePath );
                      // }
                      // if ( add_line ) 
                      if ( ! tryAndJoin( mCurrentLinePath, mCurrentLinePath ) ) {
                        addLine( mCurrentLinePath );
                      // } else {
                      //   mLastLinePath = ???
                      }
                    }
                    mCurrentLinePath = null;
                  }
                } else if ( mSymbol == SymbolType.AREA ) {
                  if ( mCurrentAreaPath != null ) {
                    if ( ! tryAndJoin( mCurrentAreaPath, mCurrentAreaPath ) ) {
                      mCurrentAreaPath.closePath();
                      mCurrentAreaPath.shiftShaderBy( mOffset.x, mOffset.y, mZoom );
                      mDrawingSurface.addDrawingPath( mCurrentAreaPath );
                    }
                    mCurrentAreaPath = null;
                    mLastLinePath = null;
                  }
                }
              }
            /*{{ END DEF NO_FILTER_START */

            // undoBtn.setEnabled(true);
            // redoBtn.setEnabled(false);
            // canRedo = false;
          }
        }
        else
        { // SymbolType.POINT
          // mLastLinePath = null;
          if ( (! mPointerDown) && (!HBXP_PointDown)) { // HBXP
            float radius = ( ( BrushManager.isPointOrientable( mCurrentPoint ) )? 6 : 2 ) * TDSetting.mPointingRadius;
	    float shift = Math.abs( x_shift ) + Math.abs( y_shift );
	    if ( shift < radius ) {
              xs = mSaveX/mZoom - mOffset.x;
              ys = mSaveY/mZoom - mOffset.y;
              if ( BrushManager.isPointLabel( mCurrentPoint ) ) {
                new DrawingLabelDialog( mActivity, this, xs, ys ).show();
              } else if ( BrushManager.isPointPhoto( mCurrentPoint ) ) {
                new DrawingPhotoDialog( mActivity, this, mPid, xs, ys ).show();
              } else if ( BrushManager.isPointAudio( mCurrentPoint ) ) {
	        if ( audioCheck ) {
                  addAudioPoint( mPid, xs, ys );
	        } else {
                  TDToast.makeWarn( R.string.no_feature_audio );
                }
              } else {
    	        if ( mLandscape ) {
                  DrawingPointPath point = new DrawingPointPath( mCurrentPoint, -ys, xs, mPointScale, mDrawingSurface.scrapIndex() );
    	          if ( BrushManager.isPointOrientable( mCurrentPoint ) ) {
		    if ( shift > TDSetting.mPointingRadius ) {
		      float angle = TDMath.atan2d( x_shift, -y_shift );
                      point.setOrientation( angle );
		      // TDLog.v("L orientation " + angle + " shift " + shift + " radius " + radius );
		    }
                    if ( ! BrushManager.isPointLabel( mCurrentPoint ) ) point.rotateBy( 90 );
    	          }
                  mDrawingSurface.addDrawingPath( point );
    	        } else {
                  DrawingPointPath point = new DrawingPointPath( mCurrentPoint, xs, ys, mPointScale, mDrawingSurface.scrapIndex() ); // no text, no options
    	          if ( BrushManager.isPointOrientable( mCurrentPoint ) ) {
		    if ( shift > TDSetting.mPointingRadius ) {
		      float angle = TDMath.atan2d( x_shift, -y_shift );
                      point.setOrientation( angle );
		      // TDLog.v("P orientation " + angle + " shift " + shift + " radius " + radius );
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
        mPointerDown = false;
        modified();
      } else if ( mMode == MODE_EDIT ) {
        if ( Math.abs(mStartX - xc) < TDSetting.mPointingRadius 
          && Math.abs(mStartY - yc) < TDSetting.mPointingRadius ) {
          doSelectAt( xs, ys, mSelectSize );
        }
        mEditMove = false;
      } else if ( mMode == MODE_SHIFT ) {
        if ( TDLevel.overExpert && mType == PlotType.PLOT_EXTENDED ) {
          SelectionPoint hot = mDrawingSurface.hotItem();
          if ( hot != null ) {
            DrawingPath path = hot.mItem;
    	    if ( path.mType == DrawingPath.DRAWING_PATH_FIXED ) { // FIXME_EXTEND
    	      DBlock blk = path.mBlock;
    	      float msz = TopoDroidApp.mDisplayWidth/(mZoom*DrawingUtil.SCALE_FIX); // TDSetting.mMinShift / 2;
    	      if ( mLandscape ) {
    	        float y = (path.y1 + path.y2)/2; // midpoint (scene)
    	        if ( Math.abs( y - xs ) < msz ) {
    	          float x = (path.x1 + path.x2)/2; // midpoint (scene)
    	          // TDLog.v( "blk scene " + x + " " + y + " tap " + xs + " " + ys);
    	          if ( Math.abs( x + ys ) < 2.5f*msz ) {
    	            int extend = (-ys + msz < x)? -1 : (-ys - msz > x)? 1 : 0;
                    updateBlockExtend( blk, extend, ExtendType.STRETCH_NONE ); // FIXME_STRETCH equal extend checked by the method
    	          }
    	        }
    	      } else {
    	        float y = (path.y1 + path.y2)/2; // midpoint (scene)
    	        if ( Math.abs( y - ys ) < msz ) {
    	          float x = (path.x1 + path.x2)/2; // midpoint (scene)
		  float dx = x - xs;
    	          // TDLog.v( "blk scene dx " + dx + " msz " + msz + " zoom " + mZoom );
    	          if ( Math.abs( x - xs ) < 2.5f*msz ) {
    	            int extend = (xs + msz < x)? -1 : (xs - msz > x)? 1 : 0;
                    updateBlockExtend( blk, extend, ExtendType.STRETCH_NONE ); // FIXME_STRETCH equal extend checked by the method
    	          }
    	        }
    	      }
    	    }
          }
        }
        if ( mShiftMove ) {
          if ( Math.abs(mStartX - xc) < TDSetting.mPointingRadius && Math.abs(mStartY - yc) < TDSetting.mPointingRadius ) {
            // mEditMove = false;
	    // PATH_MULTISELECTION
	    if ( ! mDrawingSurface.isMultiselection() ) {
              clearSelected();
            // } else {
            //   TODO
	    }
          }
        }
        mShiftMove = false;
      } else if ( mMode == MODE_ERASE ) {
	finishErasing();
      } else if ( mMode == MODE_SPLIT_SKETCH || mMode == MODE_SPLIT_SCRAP) {
        mDrawingSurface.resetPreviewPath();
        mSplitBorder.add( new PointF( xs, ys ) );
        // TDLog.v("*** split border size " + mSplitBorder.size() );
        if ( mMode == MODE_SPLIT_SKETCH ) {
          doSplitPlot( );
        } else if ( mMode == MODE_SPLIT_SCRAP) {
          doSplitScrap( );
        }
        setMode( MODE_MOVE ); // this setTheTitle() as well
      // } else { // MODE_MOVE
/* F for the moment do not create X-Sections
        if ( Math.abs(xc - mDownX) < 10 && Math.abs(yc - mDownY) < 10 ) {
          // check if there is a station: only PLAN and EXTENDED or PROFILE
          if ( PlotType.isSketch2D( mType ) ) {
            DrawingStationName sn = mDrawingSurface.getStationAt( xs, ys, mSelectSize );
            if ( sn != null ) {
              boolean barrier = mNum.isBarrier( sn.mName );
              boolean hidden  = mNum.isHidden( sn.mName );
              List< DBlock > legs = mApp_mData.selectShotsAt( TDInstance.sid, sn.getName(), true ); // select "independent" legs
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

  /** react to a touch-down event
   * @param xc   canvas X coord 
   * @param yc   canvas Y coord
   * @param xs   scene X coord
   * @param ys   scene Y coord
   * @return ...
   */
  private boolean onTouchDown( float xc, float yc, float xs, float ys )
  {
    HBXP_PointDown = false; // HBXP
    mDrawingSurface.endEraser();
    float d0 = TDSetting.mCloseCutoff + mSelectSize / mZoom;
    // TDLog.v( "on touch down. mode " + mMode + " " + mTouchMode );

    // TDLog.Log( TDLog.LOG_PLOT, "DOWN at X " + xc + " [" +TopoDroidApp.mBorderInnerLeft + " " + TopoDroidApp.mBorderInnerRight + "] Y " 
    // TDLog.v( "DOWN at X " + xc + " [" +TopoDroidApp.mBorderInnerLeft + " " + TopoDroidApp.mBorderInnerRight + "] Y " + yc + " [" + TopoDroidApp.mBorderTop + " " + TopoDroidApp.mBorderBottom + "]" );

    // float bottom = TopoDroidApp.mBorderBottom - mZoomTranslate;
    // if ( mMode == MODE_DRAW ) bottom += ZOOM_TRANSLATION;

    if ( yc > TopoDroidApp.mBorderBottom ) {
      if ( ( ! mDrawingSurface.isFixedZoom() ) && mZoomBtnsCtrlOn && xc > TopoDroidApp.mBorderInnerLeft && xc < TopoDroidApp.mBorderInnerRight ) {
        mTouchMode = MODE_ZOOM;
        mZoomBtnsCtrl.setVisible( true );
        // mZoomCtrl.show( );
      } else if ( TDSetting.mSideDrag && ( xc > TopoDroidApp.mBorderRight || xc < TopoDroidApp.mBorderLeft ) ) {
        mTouchMode = MODE_ZOOM;
      }
    } else if ( TDSetting.mSideDrag && (yc < TopoDroidApp.mBorderTop) && ( xc > TopoDroidApp.mBorderRight || xc < TopoDroidApp.mBorderLeft ) ) {
      mTouchMode = MODE_ZOOM;
      SelectionPoint sp = mDrawingSurface.hotItem();
      if ( sp != null && sp.type() == DrawingPath.DRAWING_PATH_POINT ) {
        DrawingPointPath path = (DrawingPointPath)(sp.mItem);
        if ( BrushManager.isPointOrientable(path.mPointType) ) {
          mTouchMode = MODE_ROTATE;
          mRotateScale = ((xc > TopoDroidApp.mBorderRight)? 180 : -180) / TopoDroidApp.mDisplayHeight;
          mStartY = yc;
        }
      }
    }

    if ( mMode == MODE_DRAW ) {
      // TDLog.Log( TDLog.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
      mPointCnt = 0;
      if ( mSymbol == SymbolType.LINE ) {
        mCurrentLinePath = new DrawingLinePath( mCurrentLine, mDrawingSurface.scrapIndex() );
        // mCurrentLinePat.setOptions( BrushManager.getLineDefaultOptions( mCurrentLine ) );
        mCurrentLinePath.addStartPoint( xs, ys );
        mCurrentBrush.mouseDown( mDrawingSurface.getPreviewPath(), xc, yc );
      } else if ( mSymbol == SymbolType.AREA ) {
        // TDLog.Log( TDLog.LOG_PLOT, "onTouch ACTION_DOWN area type " + mCurrentArea );
        mCurrentAreaPath = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(), mName+"-a", TDSetting.mAreaBorder, mDrawingSurface.scrapIndex() );
        // mCurrentAreaPat.setOptions( BrushManager.getAreaDefaultOptions( mCurrentArea ) );
        mCurrentAreaPath.addStartPoint( xs, ys );
        // TDLog.v( "start area start " + xs + " " + ys );
        mCurrentBrush.mouseDown( mDrawingSurface.getPreviewPath(), xc, yc );
      // } else { // SymbolType.POINT
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
        // TDLog.v( "on touch down add item at " + xs + " " + ys );
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

    } else if ( mMode == MODE_SPLIT_SKETCH || mMode == MODE_SPLIT_SCRAP ) {
      mCurrentBrush.mouseDown( mDrawingSurface.getPreviewPath(), xc, yc );
      mSplitBorder.add( new PointF( xs, ys ) );
      // TDLog.v("*** split start border size " + mSplitBorder.size() );
      mSaveX = xc; 
      mSaveY = yc;
    }
    return true;
  }

  /** react to a touch-move event
   * @param xc   canvas X coord 
   * @param yc   canvas Y coord
   * @param xs   scene X coord
   * @param ys   scene Y coord
   * @param event motion event
   * @return ...
   */
  private boolean onTouchMove( float xc, float yc, float xs, float ys, MotionEventWrap event )
  {
    // TDLog.v( "STYLUS action MOVE mode " + mMode + " touch " + mTouchMode + " (" + xc + " " + yc + ") " );
    if ( mTouchMode == MODE_MOVE) {
      float x_shift = xc - mSaveX; // compute shift
      float y_shift = yc - mSaveY;
      boolean save = true; // FIXME-000
      // mSaveX = xc; 
      // mSaveY = yc;
      if ( mMode == MODE_DRAW ) {
        float squared_shift = x_shift*x_shift + y_shift*y_shift;
        if ( TDSetting.mStylusOnly ) { // STYLUS_ONLY: to intercept artifacts
          if ( squared_shift > 25 * TDSetting.mLineSegment2 ) return false; // a jump in stylus points five times the line spacing
        }
        if ( mSymbol == SymbolType.LINE ) {
          if ( squared_shift > TDSetting.mLineSegment2 ) {
            if ( ++mPointCnt % mLinePointStep == 0 ) {
              if ( mCurrentLinePath != null ) mCurrentLinePath.addPoint( xs, ys );
            }
            mCurrentBrush.mouseMove( mDrawingSurface.getPreviewPath(), xc, yc );
          } else {
            save = false;
          }
        } else if ( mSymbol == SymbolType.AREA ) {
          if ( squared_shift > TDSetting.mLineSegment2 ) {
            if ( ++mPointCnt % mLinePointStep == 0 ) {
              mCurrentAreaPath.addPoint( xs, ys );
              // TDLog.v( "start area add " + xs + " " + ys );
            }
            mCurrentBrush.mouseMove( mDrawingSurface.getPreviewPath(), xc, yc );
          } else {
            save = false;
          }
        } else if ( mSymbol == SymbolType.POINT ) {
          // if ( squared_shift > TDSetting.mLineSegment2 ) {
          //   mPointerDown = 0;
          // }

          // HBXP copy and modification from onTouchUp
          if (true) // HBXP geek settings enable FIXME drop test
          { // SymbolType.POINT
            // mLastLinePath = null;
            if ( ! mPointerDown ) {
              float angle = 0;
              float radius = ( ( BrushManager.isPointOrientable( mCurrentPoint ) )? 6 : 2 ) * TDSetting.mPointingRadius;
              float shift = Math.abs( x_shift ) + Math.abs( y_shift );
              if ( shift > radius ) { // HBXP if big move, short move is original function
                xs = mSaveX/mZoom - mOffset.x;
                ys = mSaveY/mZoom - mOffset.y;
                if ( BrushManager.isPointLabel( mCurrentPoint ) ) {
                  //new DrawingLabelDialog( mActivity, this, xs, ys ).show(); // HBXP ? dummy text create
                } else if ( BrushManager.isPointPhoto( mCurrentPoint ) ) {
                  //new DrawingPhotoDialog( mActivity, this, xs, ys ).show(); // HBXP
                } else if ( BrushManager.isPointAudio( mCurrentPoint ) ) {
                  if ( audioCheck ) {
                    //addAudioPoint( xs, ys ); // HBXP
                  } else {
                    TDToast.makeWarn( R.string.no_feature_audio );
                  }
                } else {
                  if ( mLandscape ) {
                    DrawingPointPath point = new DrawingPointPath( mCurrentPoint, -ys, xs, mPointScale, mDrawingSurface.scrapIndex() );
                    if ( BrushManager.isPointOrientable( mCurrentPoint ) ) {
                      if ( shift > TDSetting.mPointingRadius ) {
                        angle = TDMath.atan2d( x_shift, -y_shift );
                        point.setOrientation( angle );
                        //TDLog.v(" HBXP L orientation " + angle + " shift " + shift + " radius " + radius );
                      }
                      if ( ! BrushManager.isPointLabel( mCurrentPoint ) ) point.rotateBy( 90 );
                    }
                    if ( !HBXP_PointDown ){ // HBXP To put it down just once
                      mDrawingSurface.addDrawingPath( point );
                      HBXP_PointDown = true;
                      mHotPath = point;
                    } else {
                      if ( mHotPath != null )
                        if ( mHotPath instanceof DrawingPointPath ) {
                          int scale = (int) (shift / 150) - 2; if (scale>2)scale =2;// HBXP ?150
                          //TDLog.v(" HBXP orientation " + angle + " shift " + shift + " radius " + radius + " scale " + scale );
                          ((DrawingPointPath) mHotPath).setScale(scale);
                          ((DrawingPointPath) mHotPath).setOrientation( angle );
                        }
                    }
                  } else {
                    DrawingPointPath point = new DrawingPointPath( mCurrentPoint, xs, ys, mPointScale, mDrawingSurface.scrapIndex() ); // no text, no options
                    if ( BrushManager.isPointOrientable( mCurrentPoint ) ) {
                      if ( shift > TDSetting.mPointingRadius ) {
                        angle = TDMath.atan2d( x_shift, -y_shift );
                        point.setOrientation( angle );
                        // TDLog.v("P orientation " + angle + " shift " + shift + " radius " + radius );
                      }
                    }
                    if ( !HBXP_PointDown ){
                      mDrawingSurface.addDrawingPath( point );
                      HBXP_PointDown = true;
                      mHotPath = point;
                    } else {
                      if ( mHotPath != null )
                        if ( mHotPath instanceof DrawingPointPath ) {
                          int scale = (int) (shift / 150) - 2; if (scale>2) scale = 2;// HBXP ?150
                          //TDLog.v(" HBXP 2 orientation " + angle + " shift " + shift + " radius " + radius + " scale " + scale );
                          ((DrawingPointPath) mHotPath).setScale(scale);
                          ((DrawingPointPath) mHotPath).setOrientation( angle );
                        }
                    }
                  }
                  // undoBtn.setEnabled(true);
                  // redoBtn.setEnabled(false);
                  // canRedo = false;
                }
              }
            }
          }
          // HBXP

          save = false;
        }
      } else if (  mMode == MODE_MOVE && mRotateAzimuth ) {
        TDAzimuth.mRefAzimuth = TDMath.in360( TDAzimuth.mRefAzimuth + x_shift/2 );
        setButtonAzimuth();
        // TDLog.v("rotated azimuth by " + x_shift + ": " + TDAzimuth.mRefAzimuth );
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
      } else if ( mMode == MODE_SPLIT_SKETCH || mMode == MODE_SPLIT_SCRAP ) {
        if ( ( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment2 ) {
          mCurrentBrush.mouseMove( mDrawingSurface.getPreviewPath(), xc, yc );
          mSplitBorder.add( new PointF( xs, ys ) );
          // TDLog.v("*** split ... border size " + mSplitBorder.size() );
        } else {
          save = false;
        }
      }
      if ( save ) { // FIXME-000
        mSaveX = xc; 
        mSaveY = yc;
      }
    } else if ( mTouchMode == MODE_ROTATE ) {
      mDrawingSurface.rotateHotItem( mRotateScale * ( yc - mStartY ) );
      mStartX = xc; // xs;
      mStartY = yc; // ys;
      modified();
    } else { // mTouchMode == MODE_ZOOM
      float newDist = spacing( event );
      float factor = ( newDist > 32.0f && oldDist > 32.0f )? newDist/oldDist : 0 ;

      if ( mMode == MODE_MOVE && mShiftDrawing ) {
        float x_shift = xc - mSaveX; // compute shift
        float y_shift = yc - mSaveY;
        if ( TDLevel.overNormal ) {
          if ( Math.abs( x_shift ) < TDSetting.mMinShift && Math.abs( y_shift ) < TDSetting.mMinShift ) {
    	    if ( mLandscape ) {
              mDrawingSurface.shiftDrawing( -y_shift/mZoom, x_shift/mZoom, mScrapOnly );
    	    } else {
              mDrawingSurface.shiftDrawing( x_shift/mZoom, y_shift/mZoom, mScrapOnly );
    	    }
            modified();
          }
        // } else {
        //   moveCanvas( x_shift, y_shift );
        }
        if ( event.getPointerCount() == 3 ) {
            int ret = affineTransformByEvent( event );
            TDLog.v("affine transform returns " + ret );
            // mDrawingSurface.scaleDrawing( 1+(factor-1)*0.01f, mScrapOnly );
            saveEventPoint( event );
        } else {
          changeZoom( factor );
          oldDist = newDist;
        }
        mSaveX = xc;
        mSaveY = yc;
      } else { // MOVE but not shift-drawing
        changeZoom( factor );
        oldDist = newDist;
        shiftByEvent( event );
      }
    }
    return true;
  }

  /** try to close the current area horizontally
   * @param xs ...
   */
  private void tryCloseCurrentArea( float xs )
  {
    DrawingAreaPath area = new DrawingAreaPath( mCurrentAreaPath.mAreaType,
                                                mCurrentAreaPath.mAreaCnt, 
                                                mCurrentAreaPath.mPrefix, 
                                                TDSetting.mAreaBorder, 
                                                mDrawingSurface.scrapIndex() );
    area.setOptions( BrushManager.getAreaDefaultOptions( mCurrentArea ) );
    if ( xs - mCurrentAreaPath.mFirst.x > 20 ) { // 20 == 1.0 meter // CLOSE BOTTOM SURFACE
      // TDLog.v("CLOSE BOTTOM " + (ys - mCurrentAreaPath.mFirst.y) );
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
      // TDLog.v("CLOSE TOP " + (ys - mCurrentAreaPath.mFirst.y) );
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
  }

  /** @return the xsection clino or more than 90 if error
   * @param clino input xsection clino
   */
  private float getXSectionClino( float clino )
  {
    if ( TDSetting.mSlantXSection ) {
      if ( clino > 0 ) {
        return  10 * (int)((5+clino)/10);
      } else {
        return -10 * (int)((5-clino)/10);
      }
    } else {
      if ( clino > TDSetting.mHThreshold ) {
        if ( clino > 80 ) return 90;
      } else if ( clino < -TDSetting.mHThreshold ) {
        if ( clino < -80 ) return -90;
      } else {
        if ( Math.abs(clino) < 10 ) return 0;
      }
      TDToast.makeWarn( R.string.error_profile_slant_section );
      return 999;
    }
  }

  /** ... section-line
   * @param currentLine  current line
   */
  private void doSectionLine( DrawingLinePath currentLine )
  {
    // assert( mLastLinePath == null ); // obvious
    currentLine.addOption("-direction both");
    currentLine.makeStraight( );
    boolean h_section = PlotType.isProfile( mType );
    boolean h_section_projected = PlotType.isProjected( mType ); // HBXx

    if ( h_section ) currentLine.clampOrientation( TDSetting.mSlantXSection ? 10 : 18 ); // either 18 or 30

    // NOTE here l1 is the end-point and l2 the start-point (not considering the tick)
    //         |
    //         L2 --------- L1
    //      The azimuth reference is North-East same as bearing
    //         L1->L2 = atan2( (L2-L1).x, -(L2-l1).y )  Y is point downward North upward
    //         azimuth = dir(L1->L2) + 90
    //
    LinePoint l2 = currentLine.mFirst; // .mNext;
    LinePoint l1 = l2.mNext;
    // TDLog.v( "section line L1 " + l1.x + " " + l1.y + " L2 " + l2.x + " " + l2.y );

    List< DrawingPathIntersection > paths = mDrawingSurface.getIntersectionShot( l1, l2 );
    int nr_legs = paths.size() ; // 0 no-leg, 1 ok, 2 too many legs

    if ( nr_legs == 0 ) { // FAILURE: no leg
      TDToast.makeWarn( R.string.no_leg_intersection );
      return;
    }

    String from = null;
    String to   = null;
    float azimuth = 0;
    float clino   = 0;
    float tt      = -1;  // no intercept

    currentLine.computeUnitNormal();

    // orientation of the section-line:
    // line_azimuth:
    //              ^ 180
    //     270 <----+-----> 90
    //              v 0
    // azimuth:
    //              ^ 270
    //       0 <----+-----> 180
    //              v 90
    float line_azimuth = TDMath.in360( (float)(Math.atan2( l2.x-l1.x, -l2.y+l1.y ) * TDMath.RAD2DEG ) );
    azimuth = TDMath.in360( 90 + line_azimuth );
    DBlock blk = null;
    Vector3D center = null; // centroid of the intersection
    // TDLog.v("do Section Line: section " + h_section + " projected " + h_section_projected + " legs " + nr_legs + " azimuth " + azimuth + " line " + line_azimuth );

    if ( nr_legs == 1 ) {
      DrawingPathIntersection pi = paths.get(0);

      DrawingPath p = pi.path;
      tt = pi.tt;
      // TDLog.v( "assign tt " + tt );
      blk = p.mBlock;

      // Float result = Float.valueOf(0);
      // p.intersect( l1.x, l1.y, l2.x, l2.y, result );
      // float intersection = result.floatValue();
      // // p.log();

      from = blk.mFrom;
      to   = blk.mTo;
      if ( h_section ) { // xsection in profile view
        // clino:                  azimuth (for projected view)
        //      -90    ^ 0   90                  ^
        //       <-----+---->             <------+----->
        //      -90    v 0   90        plot-90   v  plot+90
        int extend = 1; // used only for extended profile
        if ( azimuth < 180 ) {
          clino = getXSectionClino( 90 - azimuth );
        } else {
          clino = getXSectionClino( azimuth - 270 );
          extend = -1;
        } 
        if ( clino > 91 ) return;
        if ( h_section_projected ) { // HBXx
          if ( azimuth < 180) {
            azimuth = (int) TDMath.add90(mPlot2.azimuth); //HBXx
          } else {
            azimuth = (int) TDMath.sub90(mPlot2.azimuth); //HBXx
          }
        } else {
          float dc = TDMath.in360((extend == blk.getIntExtend()) ? clino - blk.mClino : 180 - clino - blk.mClino);
          if ( dc > 90 && dc <= 270 ) { // exchange FROM-TO
            azimuth = TDMath.add180(blk.mBearing);
            from = blk.mTo;
            to = blk.mFrom;
            tt = 1 - tt;
          } else {
            azimuth = blk.mBearing;
          }
          // if ( extend != blk.getIntExtend() ) {
          //   azimuth = TDMath.add180( blk.mBearing );
          // }
        } //HBXx
        // TDLog.v( "single-leg xsection clino " + clino + " azimuth " + azimuth ); 
      } else { // xsection in plan view ( clino = 0 )
        float da = TDMath.in360( azimuth - blk.mBearing );
        if ( da > 90 && da <= 270 ) { // exchange FROM-TO 
          from = blk.mTo;
          to   = blk.mFrom;
          tt   = 1 - tt;
        }
      }
      // TDLog.v( "new leg xsection " + from + " - " + to + " intercept " + tt );
    } else if ( nr_legs > 1 ) { // always true
        if ( h_section ) { // FAILURE: xsection in profile view and many legs
          if ( ! h_section_projected ) { //original HBXx
            TDToast.makeWarn(R.string.too_many_leg_intersection);
            return;
          } else { // HBX xsection on projected profile
            // clino:                  azimuth:
            //      -90    ^ 0   90                  ^
            //       <-----+---->             <------+----->
            //      -90    v 0   90        plot-90   v  plot+90
            if ( azimuth < 180 ) {
              clino = getXSectionClino( 90 - azimuth );
            } else {
              clino = getXSectionClino( azimuth - 270 );
            } 
            if ( clino > 91 ) return;
            if ( azimuth < 180 ) {
              azimuth = (int) TDMath.add90( mPlot2.azimuth ); // DOWN section-line
            } else {
              azimuth = (int) TDMath.sub90( mPlot2.azimuth ); // UP section-line
            }
            // TDLog.v( "multi-leg xsection clino " + clino + " azimuth " + azimuth ); 

            StringBuilder sb = new StringBuilder();

            double x = 0; // FIXME the centroid is not used yet
            double y = 0;
            double z = 0;
            int cnt = 0;
            for (DrawingPathIntersection path : paths) {
              DBlock b = path.path.mBlock;
              float t = path.tt;
              NumStation st_f = mNum.getStation(b.mFrom);
              NumStation st_t = mNum.getStation(b.mTo);
              if (st_f != null && st_t != null) {
                if (cnt > 0) sb.append(" ");
                sb.append(Long.toString(b.mId));
                x += st_f.e + t * (st_t.e - st_f.e); // eastward
                y += st_f.s + t * (st_t.s - st_f.s); // southward
                z += st_f.v + t * (st_t.v - st_f.v); // downward
                cnt++;
              }
            }
            if (cnt > 0) {
              tt = 2; // multileg intercept
              center = new Vector3D(x / cnt, y / cnt, z / cnt); // 3D (E,S,V) centroid of the intersections
              from = sb.toString();
              // TDLog.v( "new multileg xsection " + from + " " + center.x + " " + center.y + " " + center.z );
            } else {
              TDToast.makeWarn(R.string.too_many_leg_intersection); // FIXME bad intersections
              return;
            }
          }
        } else { // if plan view
          StringBuilder sb = new StringBuilder();

          double x = 0; // FIXME the centroid is not used yet
          double y = 0;
          double z = 0;
          int cnt = 0;
          for (DrawingPathIntersection path : paths) {
            DBlock b = path.path.mBlock;
            float t = path.tt;
            NumStation st_f = mNum.getStation(b.mFrom);
            NumStation st_t = mNum.getStation(b.mTo);
            if (st_f != null && st_t != null) {
              if (cnt > 0) sb.append(" ");
              sb.append(Long.toString(b.mId));
              x += st_f.e + t * (st_t.e - st_f.e); // eastward
              y += st_f.s + t * (st_t.s - st_f.s); // southward
              z += st_f.v + t * (st_t.v - st_f.v); // downward
              cnt++;
            }
          }
          if (cnt > 0) {
            tt = 2; // multileg intercept
            center = new Vector3D(x / cnt, y / cnt, z / cnt); // 3D (E,S,V) centroid of the intersections
            from = sb.toString();
            // TDLog.v( "new multileg xsection " + from + " " + center.x + " " + center.y + " " + center.z );
          } else {
            TDToast.makeWarn(R.string.too_many_leg_intersection); // FIXME bad intersections
            return;
          }
        }
      }
      // cross-section does not exist yet
    String section_id = mApp_mData.getNextSectionId( TDInstance.sid ); // xxN where N is a number
    currentLine.addOption( "-id " + section_id );
    mDrawingSurface.addDrawingPath( currentLine );

    if ( TDSetting.mAutoSectionPt && section_id != null ) {
      float x5 = currentLine.mLast.x + currentLine.mDx * 20; 
      float y5 = currentLine.mLast.y + currentLine.mDy * 20; 
      // FIXME_LANDSCAPE if ( mLandscape ) { float t=x5; x5=-y5; y5=t; }
      // FIXME String scrap_option = "-scrap " /* + TDInstance.survey + "-" */ + section_id;
      String scrap_option = TDString.OPTION_SCRAP + " " + TDInstance.survey + "-" + section_id;
      mSectionPt = new DrawingPointPath( BrushManager.getPointSectionIndex(),
                                                      x5, y5, PointScale.SCALE_M, 
                                                      null, // no text 
                                                      scrap_option, mDrawingSurface.scrapIndex() );
      mSectionPt.setLink( currentLine );
      mDrawingSurface.addDrawingPath( mSectionPt );
    }

    // TDLog.v( "PLOT line section dialog TT " + tt + " line type " + mCurrentLine );
    new DrawingLineSectionDialog( mActivity, this, h_section, false, section_id, currentLine, from, to, azimuth, clino, tt, center ).show();
  }

  // -------------------------------------------------------------

  /** insert a therion label point (ILabelAdder)
   * @param label  text
   * @param x      X coord
   * @param y      Y coord
   * @param level  canvas level of the point
   */
  public void addLabel( String label, float x, float y, int level )
  {
    // assert( mLastLinePath == null );
    if ( label != null && label.length() > 0 ) {
      if ( mLandscape ) { float t=x; x=-y; y=t; }
      DrawingLabelPath label_path = new DrawingLabelPath( label, x, y, mPointScale, null, mDrawingSurface.scrapIndex() );
      label_path.setOrientation( BrushManager.getPointOrientation( mCurrentPoint ) ); // FIX Asenov
      label_path.mLandscape = mLandscape;
      label_path.mLevel = level;
      mDrawingSurface.addDrawingPath( label_path );
      modified();
    } 
  }

  // private String mMediaComment = null;
  // private long  mMediaId = -1L;
  // private int   mMediaCamera = PhotoInfo.CAMERA_UNDEFINED;

  /** create a "photo" point item
   */
  private void createPhotoPoint()
  {
    float x = mMediaManager.getX();
    float y = mMediaManager.getY();
    float s = mMediaManager.getPhotoSize();
    long id = mMediaManager.getPhotoId();
    int scrap = mDrawingSurface.scrapIndex();
    DrawingPhotoPath photo = new DrawingPhotoPath( mMediaManager.getComment(), x, y, mPointScale, 
      "", // String.format(Locale.US, "-size %.2f", mMediaManager.getPhotoSize() ),
      id, scrap );
    photo.mLandscape = mLandscape;
    mDrawingSurface.addDrawingPath( photo );

    DrawingPicturePath picture = new DrawingPicturePath( s, x+1, y+1, id, scrap );
    picture.setLink( photo );
    photo.setPicture( picture );
    mDrawingSurface.addDrawingPath( picture );

    modified();
  }

  // public boolean insertPhoto( Bitmap bitmap )
  // {
  //   assert( mLastLinePath == null );
  //   if ( mMediaManager.savePhotoFile( bitmap, 90 ) ) { // compression = 90
  //     // // FIXME TITLE has to go
  //     // mApp_mData.insertPhotoRecord( TDInstance.sid, mMediaId, -1, "", TDUtil.currentDate(), mMediaComment, mMediaCamera );
  //     // // FIXME NOTIFY ? no
  //     createPhotoPoint();
  //     return true;
  //   } else {
  //     // TDLog.v("PLOT PHOTO failed to save photo");
  //   }
  //   return false;
  // }

  /** insert a photo
   * @note from IPhotoInserter
   */
  public boolean insertPhoto( )
  {
    TDLog.v("Drawing Window insert photo type PLOT, id " + mMediaManager.getPhotoId() );
    mApp_mData.insertPhotoRecord( TDInstance.sid, mMediaManager.getPhotoId(), mMediaManager.getItemId(), "", TDUtil.currentDateTime(), 
      mMediaManager.getComment(), mMediaManager.getCamera(), mMediaManager.getCode(), MediaInfo.TYPE_PLOT, PhotoInfo.FORMAT_JPEG );
    // FIXME NOTIFY ? no
    createPhotoPoint();
    return true;
  }

  public void insertPhotoBitmap( Bitmap bitmap )
  {
    TDLog.e("PLOT TODO insert photo bitmap");
  }

  // NOTE this was used to let QCamCompass tell the DrawingWindow the photo azimuth/clino
  //      but it messes up the azimuth/clino set by the section line
  //      DO NOT USE IT
  // public void notifyAzimuthClino( long pid, float azimuth, float clino )
  // {
  //   mApp_mData.updatePlotAzimuthClino( TDInstance.sid, pid, azimuth, clino );
  // }

  /** take a photo
   * @param imagefile   photo image file
   * @param insert      whether to insert the photo-point item
   * @param pid         reference item ID (x-section plot ID, or -1)
   * @param type        reference item type
   * @note this method returns before the QCamCompass is finished
   */
  private void doTakePointPhoto( String imagefile, boolean insert, long pid, int type )
  {
    TDLog.v("do take point photo: <" + imagefile + "> insert " + insert + " pid " + pid + " type " + type );
    // if ( TDandroid.AT_LEAST_API_21 && TDandroid.checkCamera( mApp ) ) { // canTakeasPhoto
      boolean with_box = true; // ! insert;
      mMediaManager.setCamera( PhotoInfo.CAMERA_TOPODROID );
      if ( pid > 0 ) mMediaManager.setReferenceItem( pid, type );
      MyBearingAndClino callback = new MyBearingAndClino( mApp, imagefile );
      new QCamCompass( this, this, callback, (insert ? this : null), with_box, false, PhotoInfo.CAMERA_TOPODROID, mMediaManager ).show(); // false=with_delay
      // }

    // } else {
    //   try {
    //     Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
    //     if ( intent.resolveActivity( getPackageManager() ) != null ) {
    //       if ( insert ) {
    //         mMediaManager.setCamera( PhotoInfo.CAMERA_INTENT );
    //         mActivity.startActivityForResult( intent, TDRequest.CAPTURE_IMAGE_DRAWWINDOW );
    //       } else {
    //         mActivity.startActivity( intent );
    //       }
    //     } else {
    //       TDToast.makeBad( R.string.no_capture_app );
    //     }
    //   } catch ( ActivityNotFoundException e ) {
    //     TDToast.makeBad( R.string.no_capture_app );
    //   }
    // }
  }

  /** insert a "photo" point
   * @param pid      plot ID
   * @param comment  photo comment
   * @param size   photo size (horizontal width) [m]
   * @param x      X coord
   * @param y      Y coord
   * @param camera camera type (API)
   # @param code   geomorphology code
   */
  public void addPhotoPoint( long pid, String comment, float size, float x, float y, int camera, String code )
  {
    // assert( mLastLinePath == null );
    if ( mLandscape ) {
      mMediaManager.setPoint( -y, x );
    } else {
      mMediaManager.setPoint( x, y );
    }
    mMediaManager.prepareNextPhoto( pid, "", ((comment == null)? "" : comment), size, camera, ((code == null)? "" : code), MediaInfo.TYPE_PLOT );
    // mMediaComment = (comment == null)? "" : comment;
    // mMediaId = mApp_mData.nextPhotoId( TDInstance.sid );
    // File imagefile = TDFile.getFile( TDPath.getSurveyJpgFile( TDInstance.survey, Long.toString(mMediaId) ) );
    doTakePointPhoto( mMediaManager.getImageFilepath(), true, -1L, MediaInfo.TYPE_PLOT ); // with inserter, no pid
  }

  /** insert a "audio" point
   * @param pid    plot ID 
   * @param x      X coord
   * @param y      Y coord
   */
  private void addAudioPoint( long pid, float x, float y )
  {
    // TDLog.v("add Audio Point " + x + " " + y );
    // assert( mLastLinePath == null );
    // mMediaComment = ""; // audio point do not have comment
    if ( ! audioCheck ) {
      TDToast.makeWarn( R.string.no_feature_audio );
      return;
    }
    if ( mLandscape ) {
      mMediaManager.setPoint( -y, x );
    } else {
      mMediaManager.setPoint( x, y );
    }
    long audio_id = mMediaManager.prepareNextAudio( pid, "", MediaInfo.TYPE_PLOT );
    // mMediaId = mApp_mData.nextAudioId( TDInstance.sid, MediaInfo.TYPE_PLOT );
    // File file = TDFile.getFile( TDPath.getSurveyWavFile( TDInstance.survey, Long.toString(mMediaId) ) );
    // TODO RECORD AUDIO
    new AudioDialog( mActivity, this, audio_id, null, pid, MediaInfo.TYPE_PLOT ).show();
  }

  /** delete an audio record
   * @param audio_id   audio ID
   * @note from IAudioInserter
   */
  public void deletedAudio( long audio_id )
  {
    // assert( mLastLinePath == null );
    DrawingAudioPath audio = mDrawingSurface.getAudioPoint( audio_id );
    deletePoint( audio ); // if audio == null doesn't do anything
  }

  // @from IAudioInserter
  // public void startRecordAudio( long bid )
  // {
  //   // nothing
  // }

  /** stop recording an audio 
   * @param audio_id    ID of the recording audio
   *
   * @note from IAudioInserter
   */
  public void stopRecordAudio( long audio_id )
  {
    DrawingAudioPath audio = mDrawingSurface.getAudioPoint( audio_id );
    if ( audio == null ) {
      // assert bid == mMediaManager.getAudioId()
      audio = new DrawingAudioPath( mMediaManager.getX(), mMediaManager.getY(), mPointScale, null, audio_id, mDrawingSurface.scrapIndex() );
      audio.mLandscape = mLandscape;
      mDrawingSurface.addDrawingPath( audio );
      modified();
    }
  }

  /** set the name of the current station
   * @param name      name
   * @param st        station name item
   */
  void setCurrentStationName( String name, DrawingStationName st )
  {
    List< StationInfo > saved = TDSetting.mSavedStations ? mApp_mData.getStations( TDInstance.sid ) : null;
    mApp.setCurrentStationName( name );
    mDrawingSurface.setCurrentStation( st, saved );
  }

  // /** clear the current station - reset its paint
  //  */
  // void clearCurrentStation() { mDrawingSurface.clearCurrentStation(); }

  /** delete at-station xsection
   * @param st   station
   * @param name xsection index
   * @param type parent sketch type (PLAN or profile)
   */
  void deleteXSection( DrawingStationName st, String name, long type ) 
  {
    // assert( mLastLinePath == null );
    long xtype = -1;
    String xs_id = null; // xsection_id eg, xs-2 (xsection at station 2)
    if ( type == PlotType.PLOT_PLAN ) {
      xs_id = "xs-" + name;
      xtype = PlotType.PLOT_X_SECTION;
    } else if ( PlotType.isProfile( type ) ) {
      xs_id = "xh-" + name;
      xtype = PlotType.PLOT_XH_SECTION;
    } else {
      TDLog.e("No at-station section to delete. Plot type " + type + " Name " + name + " SID "  + TDInstance.sid );
      return;
    }

    st.resetXSection();
    mApp_mData.deletePlotByName( xs_id, TDInstance.sid );
    // drop the files
    TDFile.deleteFile( TDPath.getSurveyPlotTdrFile( TDInstance.survey, xs_id ) );
    // TDFile.deleteFile( TDPath.getSurveyPlotTh2File( TDInstance.survey, xs_id ) );
    // TODO delete backup files

    deleteSectionPoint( xs_id ); 
  }

  /** delete section point and possibly the xsection outline
   * @param xs_id    X-section name
   */
  private void deleteSectionPoint( String xs_id )
  {
    // assert( mLastLinePath == null );
    if ( xs_id == null ) return;
    String scrap_name = TDInstance.survey + "-" + xs_id;
    mDrawingSurface.deleteSectionPoint( scrap_name );   // this section-point delete cannot be undone
    mDrawingSurface.clearXSectionOutline( scrap_name ); // clear outline if any
  }

  /** @return the station xsection comment according to the parent sketch type
   * @param st_name   station name
   * @param type      parent sketch type (PLAN or profile)
   */
  String getXSectionNick( String st_name, long type )
  {
    // parent name = mName
    String xs_id = PlotType.getXSectionName( st_name, type );
    if ( xs_id == null ) return "";
    if ( ! TDInstance.xsections ) xs_id = xs_id + "-" + mName;

    // TDLog.v( "xsection comment for <" + xs_id + ">" );

    PlotInfo plot = mApp_mData.getPlotInfo( TDInstance.sid, xs_id );
    if ( plot != null ) return plot.nick;
    return null;
  }

  /** open a station xsection - at station B where A--B--C
   * @param st_name   station name 
   * @param type      type of the parent plot where the x-section is defined
   * @param azimuth   section plane direction
   *        direct: azimuth = average azimuth of AB and BC, inverse: the opposite
   * @param clino     section plane inclination
   *        direct: clino   = average clino of AB and BC, inverse: the opposite
   * @note plot type = PLAN ==> clino = 0; plot type = PROFILE ==> clino = -90, 0, +90  according to horiz
   * @param horiz  (?) whether the section is horizontal 
   * @param nick   section comment
   *
   * @note if the xsection does not exist it is created
   */
  void openXSection( DrawingStationName st, String st_name, long type, float azimuth, float clino, boolean horiz, String nick )
  {
    // assert( mLastLinePath == null );
    // TDLog.v( "PLOT open XSection nick <" + nick + "> st_name <" + st_name + "> plot " + mName );
    // TDLog.v( "PLOT open xsection - current line " + mCurrentLine );
    // parent plot name = mName
    String xs_id = PlotType.getXSectionName( st_name, type );
    if ( xs_id == null ) return;
    if ( ! TDInstance.xsections ) xs_id = xs_id + "-" + mName;
    long xtype = PlotType.getXSectionType( type );

    // TDLog.v( "open xsection <" + xs_id + "> nick <" + nick + ">" );

    PlotInfo plot = mApp_mData.getPlotInfo( TDInstance.sid, xs_id );

    if ( plot == null  ) { // if there does not exist xsection xs-name create it
      // TDToast.makeWarn( R.string.too_many_legs_xsection );
      if ( azimuth >= 360 ) azimuth -= 360;

      if ( PlotType.isProfile( type ) ) {
        if ( horiz ) {
          clino = ( clino > 0 ) ? 90 : -90;
        } else {
          clino = 0;
        }
        // clino = ( clino >  TDSetting.mVertSplay )?  90 : ( clino < -TDSetting.mVertSplay )? -90 : 0;
      } else { // type == PlotType.PLOT_PLAN
        clino = 0;
      }
      // TDLog.v( "new at-station X-section " + xs_id + " st_name " + st_name + " nick <" + nick + ">" );

      long pid = mApp.insert2dSection( TDInstance.sid, xs_id, xtype, st_name, "", azimuth, clino, (TDInstance.xsections? null : mName), nick );
      // plot = mApp_mData.getPlotInfo( TDInstance.sid, pid );
      plot = mApp_mData.getPlotInfo( TDInstance.sid, xs_id );

      // add x-section to station-name

      st.setXSection( azimuth, clino, type );
      if ( TDSetting.mAutoSectionPt ) { // insert xsection point in the plot
        float x5 = st.getXSectionX( 4 ); // FIXME offset
        float y5 = st.getXSectionY( 4 );
        if ( mLandscape ) { float t=x5; x5=-y5; y5=t; }
        // FIXME String scrap_option = "-scrap " /* + TDInstance.survey + "-" */ + xs_id;
        String scrap_option = TDString.OPTION_SCRAP + " " + TDInstance.survey + "-" + xs_id;
        mSectionPt = new DrawingPointPath( BrushManager.getPointSectionIndex(),
      						    x5, y5, PointScale.SCALE_M, 
      						    null, scrap_option, mDrawingSurface.scrapIndex() ); // no text
        mSectionPt.setLink( st );
        mDrawingSurface.addDrawingPath( mSectionPt );
      }
    } else {
      updatePlotNick( plot, nick );
    }
    if ( plot != null ) {
      pushInfo( plot.type, plot.name, plot.start, "", plot.azimuth, plot.clino, -1, null );
      // zoomFit( mDrawingSurface.getBitmapBounds( 1.0f ) );
    }
  }

  /** update section-line x-section comment - also at-station
   * @param plot   plot info
   * @param nick   xsection comment 
   */
  void updatePlotNick( PlotInfo plot, String nick )
  {
    if ( nick == null || plot == null ) return;
    if ( ! nick.equals( plot.nick ) ) {
      mApp_mData.updatePlotNick( plot.id, mSid, nick );
    }
  }

  /** toggle splay display at a station
   * @param st_name   station name
   * @param on        whether to add the station to the ON list
   * @param off       whether to add the station to the OFF list
   */
  void toggleStationSplays( String st_name, boolean on, boolean off ) { mDrawingSurface.toggleStationSplays( st_name, on, off ); }

  /** @return true if the stations on the ON list
   * @param st_name   station name
   */
  boolean isStationSplaysOn( String st_name ) { return mDrawingSurface.isStationSplaysOn( st_name ); }

  /** @return true if the stations on the OFF list
   * @param st_name   station name
   */
  boolean isStationSplaysOff( String st_name ) { return mDrawingSurface.isStationSplaysOff( st_name ); }

  /** toggle hidden flag of a station
   * @param st_name    station name
   * @param is_hidden  whether the station is hidden (?)
   */
  void toggleStationHidden( String st_name, boolean is_hidden )
  {
    PlotInfo plot = ( mType == PlotType.PLOT_PLAN )? mPlot1 : mPlot2; // 20250321
    String hide = plot.hide.trim(); // 20250321 mPlot1.hide.trim();
    // TDLog.v( "toggle station " + st_name + " hidden " + is_hidden + " hide: <" + hide + ">" );
    String new_hide = ""; // empty string
    boolean add = false;
    boolean drop = false;
    if ( /* hide == null || */ hide.length() == 0 ) {
      add = true;
      drop = false; // already assigned
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
      // TDLog.v( "addStationHidden " + st_name + " hide <" + hide + ">" );
      // mApp_mData.updatePlotHide( mPid1, mSid, hide ); 20250321
      // mApp_mData.updatePlotHide( mPid2, mSid, hide );
      // mPlot1.hide = hide;
      // mPlot2.hide = hide;
      mApp_mData.updatePlotHide( mPid, mSid, hide );
      plot.hide = hide;
      h = 1; // hide
    } else if ( drop && is_hidden ) {
      // mApp_mData.updatePlotHide( mPid1, mSid, new_hide ); 20250321
      // mApp_mData.updatePlotHide( mPid2, mSid, new_hide );
      // mPlot1.hide = new_hide;
      // mPlot2.hide = new_hide;
      mApp_mData.updatePlotHide( mPid, mSid, new_hide );
      plot.hide = new_hide;
      h = -1; // un-hide
      // TDLog.v( "dropStationHidden " + st_name + " hide <" + new_hide + ">" );
    }
    // TDLog.v( "toggle station hidden: hide <" + hide + "> H " + h );

    if ( h != 0 ) {
      // TDLog.v("clear shots and stations" );
      mDrawingSurface.clearShotsAndStations( );
      mNum.setStationHidden( st_name, h );
      recomputeReferences( mNum, mZoom );
    }
  }
  //  mNum.setStationHidden( st_name, (hidden? -1 : +1) ); // if hidden un-hide(-1), else hide(+1)

  /** toggle hidden flag of a station
   * @param st_name    station name
   * @param is_barrier whether the station is barrier (?)
   */
  void toggleStationBarrier( String st_name, boolean is_barrier ) 
  {
    PlotInfo plot = ( mType == PlotType.PLOT_PLAN )? mPlot1 : mPlot2; // 20250321
    String view = plot.view.trim(); // 20250321 mPlot1.view.trim();
    // TDLog.v( "toggle station " + st_name + " barrier " + is_barrier + " view: <" + view + ">" );
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
      // TDLog.v( "addStationBarrier " + st_name + " view <" + view + ">" );
      // mApp_mData.updatePlotView( mPid1, mSid, view ); 20250321
      // mApp_mData.updatePlotView( mPid2, mSid, view );
      // mPlot1.view = view;
      // mPlot2.view = view;
      mApp_mData.updatePlotView( mPid, mSid, view );
      plot.view = view;
      h = 1;
    } else if ( drop && is_barrier ) {
      // mApp_mData.updatePlotView( mPid1, mSid, new_view ); 20250321
      // mApp_mData.updatePlotView( mPid2, mSid, new_view );
      // mPlot1.view = new_view;
      // mPlot2.view = new_view;
      mApp_mData.updatePlotView( mPid, mSid, new_view );
      plot.view = new_view;
      h = -1;
    }
    // TDLog.v( "toggle station barrier: view <" + view + "> H " + h );

    if ( h != 0 ) {
      // TDLog.v("clear shots and stations" );
      mDrawingSurface.clearShotsAndStations( );
      mNum.setStationBarrier( st_name, h );
      recomputeReferences( mNum, mZoom );
    }
  }
  
  /** add a therion station point
   * @param st    (user) station point
   */
  public void addStationPoint( DrawingStationName st )
  {
    // assert( mLastLinePath == null );
    DrawingStationUser path = new DrawingStationUser( st, PointScale.SCALE_M, mDrawingSurface.scrapIndex() );
    mDrawingSurface.addDrawingStationUser( path );
    modified();
  }

    /** delete a station point
     * @param st    (user) station point
     * @param path  path to drop
     */
    public void removeStationPoint( DrawingStationName st, DrawingStationUser path )
    {
      // assert( mLastLinePath == null);
      mDrawingSurface.removeDrawingStationUser( path );
      modified();
    }

    /** implement the plot delete
     */
    private void doDelete()
    {
      mApp_mData.deletePlot( mPid1, mSid );
      if ( mPid2 >= 0 ) mApp_mData.deletePlot( mPid2, mSid );
      // TopoDroidApp.mShotWindow.setRecentPlot( null, 0 );
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

    /** scrap delete confirmation dialog
     */
    void askDeleteScrap()
    {
      TopoDroidAlertDialog.makeAlert( mActivity, getResources(), R.string.scrap_delete,
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doScrapDelete();
          }
        }
      );
    }

    /** clear the hot path and show/hide the tool layout
     * @param visibility  tool layout visibility
     */
    private void clearHotPath( int visibility )
    {
      // TDLog.v("clear hot item - visibility " + visibility );
      mHotPath     = null;
      mHotItemType = -1;
      if ( visibility == View.VISIBLE ) setToolsToolbars();
      mLayoutTools.setVisibility( visibility );
    }

    /** set the drawing window mode (change the list of buttons)
     * @param mode    drawing window mode
     */
    private void setMode( int mode )
    {
      if ( mMode == mode ) return;

      if ( mMode == MODE_DRAW ) {  // this has annoying glitches 
        mZoomView.setTranslationY( 0 );
        // mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
      } else if ( mode == MODE_DRAW ) {
        mZoomView.setTranslationY( ZOOM_TRANSLATION );
        // mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
      }

      mMode = mode;
      // mLastLinePath = null;
      switch ( mMode ) {
        case MODE_MOVE:
          clearHotPath( View.INVISIBLE );
          mDrawingSurface.setDisplayPoints( false );
          mListView.setAdapter( mButtonView1.mAdapter );
          mListView.invalidate();
          break;
        case MODE_DRAW:
          clearHotPath( View.VISIBLE );
          mDrawingSurface.setDisplayPoints( false );
          mListView.setAdapter( mButtonView2.mAdapter );
          mListView.invalidate();
          break;
        case MODE_ERASE:
          clearHotPath( View.INVISIBLE );
          mDrawingSurface.setDisplayPoints( false );
          mListView.setAdapter( mButtonView5.mAdapter );
          mListView.invalidate();
          break;
        case MODE_EDIT:
          clearSelected();
          clearHotPath( View.INVISIBLE );
          mDrawingSurface.setDisplayPoints( true );
          mListView.setAdapter( mButtonView3.mAdapter );
          mListView.invalidate();
          break;
        default:
          break;
      }
      setTheTitle();
    }

    // // 2023-03-10 DROPPED 
    // /** erase mode popup menu
    //  * @param b     button
    //  * @param modes ...
    //  * @param nr    number of modes
    //  * @param code  code of the filter-listener 
    //  * @param dismiss if dismiss is JOIN don't make popup
    //  */
    // private void makePopupJoin( View b, int[] modes, int nr, final int code, int dismiss )
    // {
    //   if ( dismiss == DISMISS_JOIN ) return;
    //   final Context context = this;
    //   LinearLayout popup_layout = new LinearLayout(mActivity);
    //   popup_layout.setOrientation(LinearLayout.VERTICAL);
    //   int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
    //   int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;
    //   String text;
    //   float w = 0;
    //   Button[] tv = new Button[nr];
    //   for ( int k=0; k<nr; ++k ) {
    //     text = getString( modes[k] );
    //     tv[k] = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight, new JoinClickListener( this, k, code ) );
    //     float ww = ( tv[k].getPaint().measureText( text ) );
    //     if ( ww > w ) w = ww;
    //   }
    //   int iw = (int)(w + 10);
    //   // TDLog.v("FONT W " + w + " " + TopoDroidApp.mDisplayWidth );
    //   // if ( w > TopoDroidApp.mDisplayWidth / 2 ) w = (int)TopoDroidApp.mDisplayWidth / 2;
    //   for ( int k=0; k<nr; ++k ) {
    //     tv[k].setWidth( iw );
    //   }
    //   FontMetrics fm = tv[0].getPaint().getFontMetrics();
    //   int ih = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
    //   mPopupJoin = new PopupWindow( popup_layout, iw, ih ); 
    //   mPopupJoin.showAsDropDown(b); 
    // }

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
      float w = 0;
      Button[] tv = new Button[nr];
      for ( int k=0; k<nr; ++k ) {
        text = getString( modes[k] );
        tv[k] = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight, new FilterClickListener( this, k, code ) );
        float ww = ( tv[k].getPaint().measureText( text ) );
        if ( ww > w ) w = ww;
      }
      int iw = (int)(w + 10);
      // TDLog.v("FONT W " + w + " " + TopoDroidApp.mDisplayWidth );
      // if ( w > TopoDroidApp.mDisplayWidth / 2 ) w = (int)TopoDroidApp.mDisplayWidth / 2;
      for ( int k=0; k<nr; ++k ) {
        tv[k].setWidth( iw );
      }

      FontMetrics fm = tv[0].getPaint().getFontMetrics();
      int ih = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
      mPopupFilter = new PopupWindow( popup_layout, iw, ih ); 
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
      float w = 0, ww; 
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
        myTextView0 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              mDrawingSurface.deleteMultiselection();
              modified();
              dismissPopupEdit();
            }
          } );
        ww = myTextView0.getPaint().measureText( text );
        if ( ww > w ) w = ww;

	if ( type != DrawingPath.DRAWING_PATH_POINT ) { // DRAWING_PATH_LINE or DRAWING_PATH_AREA
          // DECIMATE
          text = getString(R.string.popup_decimate);
          myTextView1 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                mDrawingSurface.decimateMultiselection();
                modified();
                dismissPopupEdit();
              }
            } );
          ww = myTextView1.getPaint().measureText( text );
          if ( ww > w ) w = ww;
        }

	if ( type == DrawingPath.DRAWING_PATH_LINE ) {
          // JOIN
          text = getString(R.string.popup_join);
          myTextView2 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                mDrawingSurface.joinMultiselection( TDSetting.mSelectness/2 );
                modified();
                dismissPopupEdit();
              }
            } );
          ww = myTextView2.getPaint().measureText( text );
          if ( ww > w ) w = ww;
        }

	if ( mDrawingSurface.scrapNumber() > 1 ) { // STORE MULTISELECTION AND FINISH
          text = getString(R.string.popup_store);
          myTextView3 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                mDrawingSurface.storeMultiselection();
                dismissPopupEdit();
              }
            } );
          ww = myTextView3.getPaint().measureText( text );
          if ( ww > w ) w = ww;
        }

	// CLEAR MULTISELECTION AND FINISH
        text = getString(R.string.popup_finish);
        myTextView8 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              mDrawingSurface.resetMultiselection();
              dismissPopupEdit();
	    }
          } );
        ww = myTextView8.getPaint().measureText( text );
        if ( ww > w ) w = ww;

      } else {
        text = getString(R.string.popup_join_pt);
        myTextView0 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
          new View.OnClickListener( ) {
            public void onClick(View v) {
              if ( mHotItemType == DrawingPath.DRAWING_PATH_POINT ||
                   mHotItemType == DrawingPath.DRAWING_PATH_LINE ||
                   mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // SNAP to nearest point POINT/LINE/AREA
                if ( mDrawingSurface.moveHotItemToNearestPoint( TDSetting.mSelectness/2 ) ) {
                  clearSelected();
                  modified();
                } else {
                  TDToast.makeBad( R.string.failed_snap_to_point );
                }
              }
              dismissPopupEdit();
            }
          } );
        ww = myTextView0.getPaint().measureText( text );
        if ( ww > w ) w = ww;
  
        // ----- SNAP LINE to splays AREA BORDER to close line
        //
        if ( TDLevel.overExpert && TDSetting.mLineSnap ) {
          if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) {
            text = getString( R.string.popup_snap_to_splays );
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
            ww = myTextView1.getPaint().measureText( text );
            if ( ww > w ) w = ww;
          } else if ( mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
            text = getString( R.string.popup_snap_ln );
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
            ww = myTextView1.getPaint().measureText( text );
            if ( ww > w ) w = ww;
          } 
        }

        if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
          // ----- DUPLICATE LINE/AREA POINT - INSERT LINE/AREA POINTS IN RANGE
          //
          final boolean point_wise = SelectionRange.isPoint( mDoEditRange ); // 20230118 local var "point_wise"
          text = getString( point_wise? R.string.popup_split_pt : R.string.popup_split_pts );
          // TDLog.v("pintwise " + point_wise + " text " + text + " composite " + TDSetting.mCompositeActions );
          myTextView2 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // LINE/AREA
                  if ( point_wise ) {
                    mDrawingSurface.splitPointHotItem(); // split point 
                  } else {
                    mDrawingSurface.insertPointsHotItem(); // insert points in range
                  }
                  modified();
                }
                dismissPopupEdit();
              }
            } );
          // if ( TDLevel.overExpert && TDSetting.mCompositeActions && point_wise ) {
          //   myTextView2.setOnLongClickListener( new View.OnLongClickListener() {
          //     public boolean onLongClick( View v ) {
          //       if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // LINE/AREA
          //         mDrawingSurface.moveHotItemToNearestPoint( TDSetting.mSelectness/2 );
          //         mDrawingSurface.splitPointHotItem();
          //         modified();
          //       }
          //       dismissPopupEdit();
          //       return true;
          //     }
          //   } );
          // }
          ww = myTextView2.getPaint().measureText( text );
          if ( ww > w ) w = ww;

          // ----- REMOVE LINE/AREA POINT
          //
          text = getString(R.string.popup_remove_pt);
          myTextView6 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // remove pt
                  SelectionPoint sp = mDrawingSurface.hotItem();
                  if ( sp != null ) {
                    int t = sp.type();
                    DrawingPointLinePath linepath = (DrawingPointLinePath)sp.mItem;
                    if ( t == DrawingPath.DRAWING_PATH_LINE ) {
                      if ( linepath.size() > 2 ) {
                        removeLinePoint( linepath, sp.mPoint, sp );
                        linepath.retracePath();
                      } else { 
                        DrawingLinePath lp = (DrawingLinePath)linepath;
                        askDeleteItem( lp, t, BrushManager.getLineName( lp.mLineType ) );
                      }
                    } else if ( t == DrawingPath.DRAWING_PATH_AREA ) {
                      if ( linepath.size() > 3 ) {
                        removeLinePoint( linepath, sp.mPoint, sp );
                        linepath.retracePath();
                      } else {
                        DrawingAreaPath ap = (DrawingAreaPath)linepath;
                        askDeleteItem( ap, t, BrushManager.getAreaName( ap.mAreaType ) );
                      }
                    }
                    modified();
                  }
                }
                dismissPopupEdit();
              }
            } );
          ww = myTextView6.getPaint().measureText( text );
          if ( ww > w ) w = ww;

          if ( TDLevel.overExpert && TDSetting.mLineCurve ) {
            // ----- MAKE LINE/AREA SEGMENT STRAIGHT
            text = getString(R.string.popup_sharp_pt);
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
            ww = myTextView4.getPaint().measureText( text );
            if ( ww > w ) w = ww;

            // ----- MAKE LINE/AREA SEGMENT SMOOTH (CURVED, WITH CONTROL POINTS)
            text = getString(R.string.popup_curve_pt);
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
            ww = myTextView5.getPaint().measureText( text );
            if ( ww > w ) w = ww;

          }
        }

        if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) {
          // ----- CUT LINE AT SELECTED POINT AND SPLIT IT IN TWO LINES
          text = getString(R.string.popup_split_ln);
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
          ww = myTextView3.getPaint().measureText( text );
          if ( ww > w ) w = ww;

          // ATTACH LINE TO LINE
          if ( TDLevel.overExpert ) {
            text = getString(R.string.popup_append_line);
            myTextView7 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
              new View.OnClickListener( ) {
                public void onClick(View v) {
                  if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) {
                    // modified();
                    // new AsyncTask<Void, Void, Boolean >()
                    // {
                    //   @Override public Boolean doInBackground( Void ... v )
                    //   {
                    //     return mDrawingSurface.appendHotItemToNearestLine();
                    //   }
                    //   @Override protected void onPostExecute( Boolean b ) 
                    //   { 
                    //     if ( ! b ) TDToast.makeBad( R.string.failed_append_to_line );
                    //   }
                    // }.execute();
                    if ( mDrawingSurface.appendHotItemToNearestLine() ) {
                      modified();
                    } else {
                      TDToast.makeBad( R.string.failed_append_to_line );
                    }
                  }
                  dismissPopupEdit();
                }
              } );
            ww = myTextView7.getPaint().measureText( text );
            if ( ww > w ) w = ww;
          }
        }

        // PATH_MULTISELECTION
        if ( TDLevel.overExpert && TDSetting.mPathMultiselect ) {
          if (    mHotItemType == DrawingPath.DRAWING_PATH_POINT
               || mHotItemType == DrawingPath.DRAWING_PATH_LINE 
               || mHotItemType == DrawingPath.DRAWING_PATH_AREA 
               || mDrawingSurface.hasStoredMultiselection()
              ) {
            text = getString( mDrawingSurface.hasStoredMultiselection()?  R.string.popup_restore : R.string.popup_multiselect );
            myTextView8 = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight,
            new View.OnClickListener( ) {
              public void onClick(View v) {
                if ( mDrawingSurface.hasStoredMultiselection() ) {
                  // TDLog.v("restore multiselection");
                  mDrawingSurface.restoreMultiselection();
                  modified();
                } else {
                  // TDLog.v( "start multi selection");
                  mDrawingSurface.startMultiselection();
                }
                dismissPopupEdit();
              }
            } );
            ww = myTextView8.getPaint().measureText( text );
            if ( ww > w ) w = ww;
          }
        }

      }
      int iw = (int)(w + 10);
      // TDLog.v("FONT W " + w + " " + TopoDroidApp.mDisplayWidth );
      // if ( w > TopoDroidApp.mDisplayWidth / 2 ) w = (int)TopoDroidApp.mDisplayWidth / 2;

      myTextView0.setWidth( iw );
      if ( myTextView1 != null ) myTextView1.setWidth( iw );
      if ( myTextView2 != null ) myTextView2.setWidth( iw );
      if ( myTextView3 != null ) myTextView3.setWidth( iw );
      if ( myTextView4 != null ) myTextView4.setWidth( iw );
      if ( myTextView5 != null ) myTextView5.setWidth( iw );
      if ( myTextView6 != null ) myTextView6.setWidth( iw );
      if ( myTextView7 != null ) myTextView7.setWidth( iw ); // APPEND LINE TO LINE
      if ( myTextView8 != null ) myTextView8.setWidth( iw ); // PATH_MULTISELECTION
      
      FontMetrics fm = myTextView0.getPaint().getFontMetrics();
      int ih = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
      mPopupEdit = new PopupWindow( popup_layout, iw, ih ); 
      // mPopupEdit = new PopupWindow( popup_layout, popup_layout.getHeight(), popup_layout.getWidth() );
      mPopupEdit.showAsDropDown(b); 
    }

    /** dismiss the EDIT popup
     */
    private boolean dismissPopupEdit()
    {
      if ( mPopupEdit != null ) {
        mPopupEdit.dismiss();
        mPopupEdit = null;
        return true;
      }
      return false;
    }

    /** dismiss the erase/select FILTER popup
     */
    public boolean dismissPopupFilter()
    {
      if ( mPopupFilter != null ) {
        mPopupFilter.dismiss();
        mPopupFilter = null;
        return true;
      }
      return false;
    }

    // 2023-03-10 DROPPED 
    // /** dismiss the line JOIN popup
    //  */
    // private boolean dismissPopupJoin()
    // {
    //   if ( mPopupJoin != null ) {
    //     mPopupJoin.dismiss();
    //     mPopupJoin = null;
    //     return true;
    //   }
    //   return false;
    // }

    /** dismiss the popups
     */
    private int dismissPopups() 
    {
      if ( dismissPopupEdit() )         return DISMISS_EDIT;
      if ( dismissPopupFilter() )       return DISMISS_FILTER;
      // if ( dismissPopupJoin() )         return DISMISS_JOIN;
      if ( CutNPaste.dismissPopupBT() ) return DISMISS_BT;
      return DISMISS_NONE;
    }

    // -----------------------------------------------------------------------------------------

    /** switch plot type between PLAN and PROFILE
     */
    private void switchPlotType()
    {
      // TDLog.v( "switch plot type ");
      // mLastLinePath = null; // necessary
      if ( mModified ) doSaveTdr( ); // this sets Modified = false after spawning the saving task
      updateReference();
      if ( mType == PlotType.PLOT_PLAN ) {
        setPlotType2( COMPUTE_NO, PARAMS_YES );
      } else if ( PlotType.isProfile( mType ) ) {
        setPlotType1( COMPUTE_NO, PARAMS_YES );
      }
    }

    /** set the plot as of type 3
     * @param params   whether to update XY-zoom values by the plot
     * @note called by doRecover and setPlotType
     */
    private void setPlotType3( boolean params )
    {
      // assert( mLastLinePath == null);
      if ( mPlot3 == null ) {
        TDLog.e( "set plot xsection: null plot" );
        return;
      }
      // TDLog.v( "PLOT set type 3 mType " + mType );
      mPid  = mPid3;
      mName = mName3;
      mType = mPlot3.type;
      // TDLog.v( "set plot type 3 mType " + mType + " " + mName + " pid " + mPid3 );
      // setButton1( BTN_PLAN, mBMextend );
      mDrawingSurface.setManager( DrawingSurface.DRAWING_SECTION, (int)mType );
      resetReference( mPlot3, params );
      setTheTitle();
    } 

    /** set the plot as of type 2
     * @param params   whether to update XY-zoom values by the plot
     * @param compute ...
     */
    private void setPlotType2( boolean compute, boolean params )
    {
      // assert( mLastLinePath == null);
      if ( mPlot2 == null ) return;
      // TDLog.v( "PLOT set type 2 mType " + mType );
      mPid  = mPid2;
      mName = mName2;
      mType = mPlot2.type; // FIXME if ( mPlot2 == null ) { what ? }
      setButton1( BTN_PLAN, mBMextend );
      mDrawingSurface.setManager( DrawingSurface.DRAWING_PROFILE, (int)mType );
      if ( compute && mNum != null ) {
        mNum.setBarrierAndHidden( mPlot2.view, mPlot2.hide ); // 20250321
        computeReferences( mNum, mPlot2.type, mPlot2.name, TopoDroidApp.mScaleFactor, false );
      }
      resetReference( mPlot2, params );
      TDInstance.recentPlotType = mType;
      // if ( TopoDroidApp.mShotWindow != null ) {
      //   // TopoDroidApp.mShotWindow.mRecentPlotType = mType;
      // } else {
      //   TDLog.e("Null app mShotWindow on recent plot type2");
      // }
      setTheTitle();
    } 

    /** set the plot as of type 2
     * @param params   whether to update XY-zoom values by the plot
     * @param compute ...
     * called by setPlotType, switchPlotType and doRecover
     */
    private void setPlotType1( boolean compute, boolean params )
    {
      // assert( mLastLinePath == null);
      if ( mPlot1 == null ) return;
      // TDLog.v( "PLOT set type 1 mType " + mType );
      mPid  = mPid1;
      mName = mName1;
      mType = mPlot1.type;
      setButton1( BTN_PLAN, mBMplan );
      mDrawingSurface.setManager( DrawingSurface.DRAWING_PLAN, (int)mType );
      if ( compute && mNum != null ) {
        mNum.setBarrierAndHidden( mPlot1.view, mPlot1.hide ); // 20250321
        computeReferences( mNum, mPlot1.type, mPlot1.name, TopoDroidApp.mScaleFactor, false );
      }
      resetReference( mPlot1, params );
      TDInstance.recentPlotType = mType;
      // if ( TopoDroidApp.mShotWindow != null ) {
      //   // TopoDroidApp.mShotWindow.mRecentPlotType = mType;
      // } else {
      //   TDLog.e("Null app mShotWindow on recent plot type1");
      // }
      setTheTitle();
    }

    /** flip a shot "extend"
     * @param blk   shot
     */
    private void flipBlock( DBlock blk )
    {
      if ( blk != null && blk.flipExtendAndStretch() ) {
        mApp_mData.updateShotExtend( blk.mId, mSid, blk.getIntExtend(), blk.getStretch() );
      }
    }

    /** flip the profile sketch left/right
     * @param flip_shots whether to flip also the shots extend
     * @param scrap      whether to flip only the current scrap
     * @note barrier and hiding shots are not flipped
     */
    public void flipProfile( boolean flip_shots, boolean scrap )
    {
      // assert( mLastLinePath == null );
      mDrawingSurface.flipProfile( mZoom, scrap );
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
  private void doBluetooth( Button b, int dismiss )
  {
    if ( dismiss == DISMISS_BT ) return;
    mApp.doBluetoothButton( mActivity, this, b, -1 );
  }

  /** set the icon of the "azimuth" button
   */
  private void setButtonAzimuth()
  {
    if ( BTN_DIAL < mNrButton1 ) { // && mRotateAzimuth
      Bitmap bm2 = AzimuthDialog.getRotatedBitmap( TDAzimuth.mRefAzimuth, mDialOn );
      mButton1[ BTN_DIAL ].setBackgroundDrawable( new BitmapDrawable( getResources(), bm2 ) ); // DEPRECATED API-16
    }
  }

  /** set the icon of the "range" button
   */
  private void setButtonRange()
  {
    if ( BTN_BORDER < mNrButton3 ) {
      switch ( mDoEditRange ) {
        case SelectionRange.RANGE_POINT:
          setButton3( BTN_BORDER, mBMedit_no );
          break;
        case SelectionRange.RANGE_SOFT:
          setButton3( BTN_BORDER, mBMedit_ok );
          break;
        case SelectionRange.RANGE_HARD:
          setButton3( BTN_BORDER, mBMedit_box );
          break;
        case SelectionRange.RANGE_ITEM:
          setButton3( BTN_BORDER, mBMedit_item );
          break;
      }
    }
  }

  @Override
  public boolean onLongClick( View view ) 
  {
    Button b = (Button)view;
    if ( TDLevel.overAdvanced && BTN_DOWNLOAD < mNrButton1 && b == mButton1[ BTN_DOWNLOAD ] ) {
      if ( mDataDownloader != null ) { // TH2EDIT added this test 
        if (  ! mDataDownloader.isDownloading() && TDSetting.isConnectionModeMulti() && TopoDroidApp.mDData.getDevices().size() > 1 ) {
          if ( TDSetting.mSecondDistoX && TDInstance.getDeviceB() != null ) {
            mApp.switchSecondDevice();
            setTheTitle();
            // TDToast.make( String.format( getResources().getString(R.string.using), TDInstance.deviceNickname() ) );
          } else {
            (new DeviceSelectDialog( this, mApp, mDataDownloader, this )).show();
          }
        } else {
          mDataDownloader.toggleDownloading();
          setConnectionStatus( mDataDownloader.getStatus() );
          mDataDownloader.doDataDownload( mApp.mListerSet, DataType.DATA_SHOT );
        }
      }
    } else if ( TDLevel.overAdvanced && BTN_DIAL < mNrButton1 && b == mButton1[ BTN_DIAL ] ) {
      if ( /* TDLevel.overAdvanced && */ mType == PlotType.PLOT_PLAN && TDAzimuth.mFixedExtend == 0 ) {
        mRotateAzimuth = true;
        setButtonAzimuth();
      } else {
        onClick( view );
      }
    } else if ( BTN_PLAN < mNrButton1 && b == mButton1[ BTN_PLAN ] ) {
      if ( PlotType.isSketch2D( mType ) ) {
        if ( /* TDLevel.overBasic && */ mType == PlotType.PLOT_EXTENDED ) {
          new DrawingProfileFlipDialog( mActivity, this ).show();
        } else {
          return false; // not consumed
        }
      } else if ( TDLevel.overExpert ) {
        mApp.mShowSectionSplays = ! mApp.mShowSectionSplays;
        // TDLog.v( "toggle section splays " + mShowSectionSplays );
        mDrawingSurface.setSplayAlpha( mApp.mShowSectionSplays );
        updateSplays( mApp.mSplayMode );
      }
    } else if ( BTN_TOOL < mNrButton2 && b == mButton2[ BTN_TOOL ] /* && ! TDSetting.mTripleToolbar */ ) {
      if ( ! toggleButtonRetrace() ) {
        onClick( view ); // execute rotateRecentToolset();
      }

    } else if ( TDLevel.overBasic && BTN_REMOVE < mNrButton3 && b == mButton3[ BTN_REMOVE ] ) {
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
            askDeleteItem( lp, t, BrushManager.getLineName( lp.mLineType ) );
          } else {
            removeLinePoint( lp, sp.mPoint, sp );
            lp.retracePath();
            modified();
          }
        } else if ( t == DrawingPath.DRAWING_PATH_AREA ) {
          DrawingAreaPath ap = (DrawingAreaPath)sp.mItem;
          if ( ap.size() <= 3 ) {
            askDeleteItem( ap, t, BrushManager.getAreaName( ap.mAreaType ) );
          } else {
            removeLinePoint( ap, sp.mPoint, sp );
            ap.retracePath();
            modified();
          }
        }
      }
    } else if ( TDLevel.overAdvanced && BTN_ITEM_EDIT < mNrButton3 && b == mButton3[ BTN_ITEM_EDIT ] ) { // item edit dialog
      SelectionPoint sp = mDrawingSurface.hotItem();
      if ( sp != null ) {
        DrawingPath item = sp.mItem;
        if ( item != null ) {
          // if ( item instanceof DrawingPointPath && ! TDSetting.mFixmeXSection ) {
          //   DrawingPointPath point = (DrawingPointPath)item;
          //   if ( BrushManager.isPointSection( point.mPointType ) ) {
          //     String section = point.getOption(TDString.OPTION_SCRAP);
          //     // TDLog.v("POINT section <" + section_name + ">");
          //     if ( section != null ) {
          //       String section_name = TDUtil.replacePrefix( TDInstance.survey, section );
          //       openXSectionDraw( section_name );
          //     } else {
          //       onClick( view ); 
          //     }
          //   } else {
          //     onClick( view ); 
          //   }
          // } else {
            onClick( view ); 
          // } 
        }
      }
    } else if ( b == mMenuImage ) { // MENU long click
      // TDLog.v("MENU LONG CLICK");
      int bad_loops = mNum.nrInaccurateLoops;
      if ( mMultiBad.size() == 0 /* && nr_magnetic_bad == 0 */ && mNum.surveyExtend && mNum.surveyAttached && (bad_loops == 0) ) { // nr_multi_bad
        onClick( view );
      } else {
        // onClick( view );
        new ReductionErrorsDialog( this, /* this, */ mMultiBad, nr_magnetic_bad, mNum ).show();
      }
    } else if ( TDLevel.overNormal && b == mButton2[0] ) { // drawing properties
      Intent intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
      intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_PLOT_DRAW );
      mActivity.startActivity( intent );
    } else if ( TDLevel.overNormal && b == mButton5[1] ) { // erase properties
      Intent intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
      intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_PLOT_ERASE );
      mActivity.startActivity( intent );
    } else if ( TDLevel.overNormal && b == mButton3[2] ) { // edit properties
      Intent intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
      intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_PLOT_EDIT );
      mActivity.startActivity( intent );
    }
    return true;
  }

  /** clear the selection of item/point
   */
  private void clearSelected()
  {
    // assert( mLastLinePath == null ); // not needed
    mHasSelected = false;
    mDrawingSurface.clearSelected();
    mMode = MODE_EDIT;
    setButton3PrevNext();
    setButton3Item( null );
  }

  /** ON CLICK: react to a user tap
   * @param view   tapped view
   */
  @Override
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
    if ( b == mMenuImage ) {
      if ( mMenu.getVisibility() == View.VISIBLE ) {
        mMenu.setVisibility( View.GONE );
        onMenu = false;
      } else {
        mMenu.setVisibility( View.VISIBLE );
        onMenu = true;
      }
      return;
    }
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

    } else if ( mMode == MODE_DRAW ) {
      int k2 = 3;
      if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // UNDO
        mDrawingSurface.undo();
        // if ( ! mDrawingSurface.hasMoreUndo() ) {
        //   // undoBtn.setEnabled( false );
        // }
        // redoBtn.setEnabled( true );
        // canRedo = true;/
        // mLastLinePath = null;
        modified();
      } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // REDO
        if ( mDrawingSurface.hasMoreRedo() ) {
          mDrawingSurface.redo();
          // mLastLinePath = null;
        }
      } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // TOOLS
        // if ( ! TDSetting.mTripleToolbar ) {
          rotateRecentToolset();
        // } else {
        //   new ItemPickerDialog(mActivity, this, mType, mSymbol ).show();
        // }
      } else if ( ( ! mTh2Edit) && k2 < mNrButton2 && b == mButton2[k2++] ) { // SPLAYS TH2EDIT
        toggleSplayMode();
      } else if ( TDLevel.overNormal && k2 < mNrButton2 && b == mButton2[k2++] ) { //  CONT continuation popup menu
        toggleButtonContinue(); 
      }

    } else if ( mMode == MODE_ERASE ) {
      int k5 = 3;
      if ( k5 < mNrButton5 && b == mButton5[k5++] ) { // UNDO same as in mButton2[]
        mDrawingSurface.undo();
        // if ( ! mDrawingSurface.hasMoreUndo() ) {
        //   // undoBtn.setEnabled( false );
        // }
        // redoBtn.setEnabled( true );
        // canRedo = true;/
        // mLastLinePath = null;
        modified();
      } else if ( k5 < mNrButton5 && b == mButton5[k5++] ) { // REDO same as in mButton2[]
        if ( mDrawingSurface.hasMoreRedo() ) {
          mDrawingSurface.redo();
          // mLastLinePath = null;
        }
      } else if ( k5 < mNrButton5 && b == mButton5[k5++] ) { // ERASE MODE
        makePopupFilter( b, Drawing.mEraseModes, 4, Drawing.CODE_ERASE, dismiss ); // pull-down menu to select erase mode
      } else if ( k5 < mNrButton5 && b == mButton5[k5++] ) { // ERASE SIZE
        setButtonEraseSize( mEraseScale + 1 ); // toggle erase size
      }

    } else if ( mMode == MODE_EDIT || mMode == MODE_SHIFT ) {
      int k3 = 3;
      if ( k3 < mNrButton3 && b == mButton3[k3++] ) { // PREV
        if ( mHasSelected ) {
          SelectionPoint pt = mDrawingSurface.prevHotItem( );
          if ( SelectionRange.isPointOrItem( mDoEditRange ) ) mMode = MODE_SHIFT;
          setButton3Item( pt );
        } else {
          makePopupFilter( b, Drawing.mSelectModes, 6, Drawing.CODE_SELECT, dismiss );
        }
      } else if ( k3 < mNrButton3 && b == mButton3[k3++] ) { // NEXT
        if ( mHasSelected ) {
          SelectionPoint pt = mDrawingSurface.nextHotItem( );
          if ( SelectionRange.isPointOrItem( mDoEditRange ) ) mMode = MODE_SHIFT;
          setButton3Item( pt );
        } else {
          setButtonSelectSize( mSelectScale + 1 ); // toggle select size
        }
      } else if ( k3 < mNrButton3 && b == mButton3[k3++] ) { // EDIT ITEM DELETE
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
                name = BrushManager.getLineName( ((DrawingLinePath)p).mLineType );
                break;
              case DrawingPath.DRAWING_PATH_AREA:
                name = BrushManager.getAreaName( ((DrawingAreaPath)p).mAreaType );
                break;
            }
            askDeleteItem( p, t, name );
          } else if ( t == DrawingPath.DRAWING_PATH_SPLAY ) {
            if ( PlotType.isSketch2D( mType ) && ( sp.mItem instanceof DrawingSplayPath ) ) { 
              DrawingSplayPath p = (DrawingSplayPath)(sp.mItem);
              DBlock blk = p.mBlock;
              if ( blk != null ) {
                askDeleteSplay( p, sp, blk );
              }
            }
          }
        }
      } else if ( k3 < mNrButton3 && b == mButton3[k3++] ) { // EDIT ITEM PROPERTIES
        SelectionPoint sp = mDrawingSurface.hotItem();
        if ( sp != null ) {
          DrawingPath item = sp.mItem;
          if ( item != null ) {
            if ( item instanceof DrawingStationName ) {
              DrawingStationName st = (DrawingStationName)(item);
              DrawingStationUser path = mDrawingSurface.getStationPath( st.getName() );
              boolean barrier = mNum.isBarrier( st.getName() );
              boolean hidden  = mNum.isHidden( st.getName() );
              List< DBlock > legs = mApp_mData.selectShotsAt( TDInstance.sid, st.getName(), true ); // select "independent" legs
              new DrawingStationDialog( mActivity, this, mApp, st, path, barrier, hidden, /* TDInstance.xsections, */ legs ).show();
            } else if ( item instanceof DrawingPointPath ) {
              DrawingPointPath point = (DrawingPointPath)(item);
              // TDLog.v( "edit point type " + point.mPointType );
              if ( point instanceof DrawingPhotoPath ) { // BrushManager.isPointPhoto( point.mPointType )
                new DrawingPhotoEditDialog( mActivity, (DrawingPhotoPath)point ).show();
              } else if ( point instanceof DrawingPicturePath ) { // BrushManager.isPointPhoto( point.mPointType )
                // TODO
                DrawingPhotoPath photo = (DrawingPhotoPath)(point.getLink() );
                if ( photo != null ) {
                  new DrawingPhotoEditDialog( mActivity, photo ).show();
                } else {
                  TDLog.e("Picture without photo");
                }
              } else if ( point instanceof DrawingAudioPath ) { // BrushManager.isPointAudio( point.mPointType )
                if ( audioCheck ) {
                  DrawingAudioPath audio = (DrawingAudioPath)point;
                  new AudioDialog( mActivity, this, audio.mId, null, mPid, MediaInfo.TYPE_PLOT ).show(); // null DBlock
                } else {
                  TDToast.makeWarn( R.string.no_feature_audio );
                }
              } else if ( BrushManager.isPointSection( point.mPointType ) ) { // open x-section sketch
                // if ( TDSetting.mFixmeXSection ) {
                  String section = point.getOption(TDString.OPTION_SCRAP);
                  if ( section != null ) {
                    String section_name = TDUtil.replacePrefix( TDInstance.survey, section );
                    openXSectionDraw( section_name );
                  }
                // } else {
                //   new DrawingPointSectionDialog( mActivity, this, point ).show();
                // }
              } else {
                new DrawingPointDialog( mActivity, this, point ).show();
              }
              // modified()
            } else if ( item instanceof DrawingLinePath ) {
              DrawingLinePath line = (DrawingLinePath)(item);
              if ( BrushManager.isLineSection( line.mLineType ) ) {
                // cross-section exists already
                boolean h_section = PlotType.isProfile( mType ); // not really necessary
                String id = line.getOption( "-id" );
                // TDLog.v( "edit section line, id <" + id + ">" ); // default azimuth = 0 clino = 0
                if ( id != null ) {
                  new DrawingLineSectionDialog( mActivity, this, h_section, true, id, line, null, null, 0, 0, -1, null ).show();
                } else {
                  TDLog.e("edit section line with null id" );
                }
              } else {
                new DrawingLineDialog( mActivity, this, line, sp.mPoint ).show();
              }
              // modified()
            } else if ( item instanceof DrawingAreaPath ) {
                new DrawingAreaDialog( mActivity, this, (DrawingAreaPath)(item) ).show();
                // modified()
            } else {
              // TDLog.v( "centerline path type " + sp.type() );
              if ( sp.type() == DrawingPath.DRAWING_PATH_FIXED ) {
                int flag = ( item.mBlock != null )? mNum.canBarrierHidden( item.mBlock.mFrom, item.mBlock.mTo ) : 0;
                new DrawingShotDialog( mActivity, this, item, flag ).show();
              } else if ( sp.type() == DrawingPath.DRAWING_PATH_SPLAY ) {
                new DrawingShotDialog( mActivity, this, item, 0 ).show();
              }
            }
          } else {
            TDLog.e("selected point has null item");
          }
        }
        clearSelected();
      } else if ( k3 < mNrButton3 && b == mButton3[k3++] ) { // ITEM/POINT EDITING: move, split, remove, etc.
        // TDLog.v( "Button3[5] hasPointActions " + hasPointActions );
        SelectionPoint sp = mDrawingSurface.hotItem();
        DrawingPath item = ( sp != null )? sp.mItem : null;
        DrawingPointPath point = ( item != null && item instanceof DrawingPointPath )? (DrawingPointPath) item : null;
        if ( point != null && BrushManager.isPointSection( point.mPointType ) ) { // open x-section sketch
          String xsection_name = TDUtil.replacePrefix( TDInstance.survey, point.getOption(TDString.OPTION_SCRAP) ); 
          openXSectionDraw( xsection_name ); 
        } else if ( hasPointActions ) {
          makePopupEdit( b, dismiss );
        // } else {
          // SelectionPoint sp = mDrawingSurface.hotItem();
          // if ( sp != null && sp.mItem.mType == DrawingPath.DRAWING_PATH_NAME ) {
          //   DrawingStationName sn = (DrawingStationName)(sp.mItem);
          //   new DrawingBarrierDialog( this, this, sn.getName(), mNum.isBarrier( sn.getName() ) ).show();
          // }
        }
      } else if ( TDLevel.overExpert && k3 < mNrButton3 && b == mButton3[ k3++ ] ) { // RANGE EDIT
        mDoEditRange = SelectionRange.rotateType( mDoEditRange );
        setButtonRange();
      }
    } else if ( ! mTh2Edit ) { // TH2EDIT
      int k1 = 3;
      if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // DOWNLOAD
        // setConnectionStatus( ConnectionState.CONN_WAITING ); // FIXME DistoXDOWN was not commented
        resetFixedPaint();
        updateReference();
        if ( TDInstance.getDeviceA() == null ) {
          DBlock last_blk = mApp_mData.selectLastLegShot( TDInstance.sid );
          (new ShotNewDialog( mActivity, mApp, this, last_blk, -1L )).show();
          // (new ShotNewDialog( mActivity, mApp, this, null, -1L )).show();
        } else {
          mDataDownloader.toggleDownloading();
          // setConnectionStatus( mDataDownloader.getStatus() ); // FIXME DistoXDOWN was not commented
          mDataDownloader.doDataDownload( mApp.mListerSet, DataType.DATA_SHOT );
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // BLUETOOTH
        // TDLog.v("Button BLUETOOTH");
        doBluetooth( b, dismiss );
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // DISPLAY MODE 
        // TDLog.v("Button MODE");
        new DrawingModeDialog( mActivity, this, mDrawingSurface ).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // TOGGLE PLAN/EXTENDED
        // TDLog.v("Button PLAV/EXTENDED");
        if ( PlotType.isSketch2D( mType ) ) { 
          // TDLog.v( "saving TOGGLE ...");
          startSaveTdrTask( mType, PlotSave.TOGGLE, TDSetting.mBackupNumber+2, TDPath.NR_BACKUP ); 
          // mDrawingSurface.clearDrawing();
          switchPlotType();
        } else if ( PlotType.isLegSection( mType ) ) {
          updateSplays( (mApp.mSplayMode + 1)%4 );
        } else if ( PlotType.isStationSection( mType ) ) {
          updateSplays( (mApp.mSplayMode + 2)%4 );
        }
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { //  NOTE
        // TDLog.v("Button NOTE");
        (new DialogAnnotations( mActivity, mApp_mData.getSurveyFromId(mSid) )).show();

      } else if ( TDLevel.overNormal && k1 < mNrButton1 && b == mButton1[k1++] ) { //  AZIMUTH
        // TDLog.v("Button AZIMUTH");
        if ( PlotType.isSketch2D( mType ) ) { 
          if ( TDSetting.mAzimuthManual ) {
            setRefAzimuth( 0, - TDAzimuth.mFixedExtend ); // flip fixed extend left/right
          } else {
            (new AzimuthDialog( mActivity, this, TDAzimuth.mRefAzimuth, mBMdial )).show(); // FIXME_AZIMUTH_DIAL 1
            // (new AzimuthDialog( mActivity, this, TDAzimuth.mRefAzimuth, mDialBitmap )).show(); // FIXME_AZIMUTH_DIAL 2
          }
        }
      } else if ( TDLevel.overNormal && k1 < mNrButton1 && b == mButton1[k1++] ) { //  REFRESH
        // TDLog.v("Button REFRESH");
        updateDisplay();
        TDToast.make( R.string.display_refresh );
      }
    }

  }

  /** toggle the splay display mode
   */
  private void toggleSplayMode()
  {
    if ( BTN_SPLAYS < mNrButton2 ) {
      if ( DrawingSplayPath.toggleSplayMode() == DrawingSplayPath.SPLAY_MODE_LINE ) {
        setButton2( BTN_SPLAYS, mBMsplays_line );
      } else {
        setButton2( BTN_SPLAYS, mBMsplays_point );
      }
    }
  }

  /** ask confirmation to delete an item
   * @param p    item to delete
   * @param t    ... (not used)
   * @param name item name
   */
  private void askDeleteItem( final DrawingPath p, final int t, final String name )
  {
    TopoDroidAlertDialog.makeAlert( mActivity, getResources(), String.format( getResources().getString( R.string.item_delete ), name ), 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          if ( p instanceof DrawingPointPath ) {
            deletePoint( (DrawingPointPath)p );
          } else if ( p instanceof DrawingLinePath ) {
            deleteLine( (DrawingLinePath)p );
          } else if ( p instanceof DrawingAreaPath ) {
            deleteArea( (DrawingAreaPath)p );
          }
          // switch( t ) {
          //   case DrawingPath.DRAWING_PATH_POINT:
          //     deletePoint( (DrawingPointPath)p );
          //     break;
          //   case DrawingPath.DRAWING_PATH_LINE:
          //     deleteLine( (DrawingLinePath)p );
          //     break;
          //   case DrawingPath.DRAWING_PATH_AREA:
          //     deleteArea( (DrawingAreaPath)p );
          //     break;
          //   default:
          //     break;
          // }
        }
      }
    );
  }

  /** ask confirmation to delete a splay
   * @param p    splay path to delete
   * @param sp   selection point corresponding to the splay path
   * @param blk  splay data block
   */
  private void askDeleteSplay( final DrawingSplayPath p, final SelectionPoint sp, final DBlock blk )
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

  /** prepare a (leg) xsection - retrieve the ID or create a new section plot and return the ID
   * @param id    section name, eg, xx0
   * @param type  parent plot type
   * @param from  FROM station
   * @param to    TO station
   * @param nick  xsection comment
   * @param azimuth section azimuth
   * @param clino   section clino
   * @return the xsection plot ID
   */
  private long prepareXSection( String id, long type, String from, String to, String nick, float azimuth, float clino )
  {
    // mCurrentLine = BrushManager.getLineWallIndex();
    // if ( ! BrushManager.isLineEnabled( SymbolLibrary.WALL ) ) mCurrentLine = 0;

    // assert( mLastLinePath == null );
    setTheTitle();

    if ( id == null || id.length() == 0 ) return -1;
    mSectionName = id;
    long pid = mApp_mData.getPlotId( TDInstance.sid, mSectionName );
    if ( pid < 0 ) { 
      // TDLog.v("insert 2D section");
      pid = mApp.insert2dSection( TDInstance.sid, mSectionName, type, from, to, azimuth, clino, ( TDInstance.xsections? null : mName), nick );
    }
    return pid;
  }

  /** make a photo X-Section from a section-line
   * @param line    "section" line
   * @param id      section ID, eg "xx0"
   * @param type    either PLOT_SECTION or PLOT_H_SECTION
   * @param from    from station, eg "1"
   * @param to      to station, eg "2"
   * @param nick    xsection comment
   * @param azimuth section azimuth
   * @param clino   section clino
   */
  void makePhotoXSection( DrawingLinePath line, String id, long type, String from, String to, String nick, float azimuth, float clino )
  {
    TDLog.v("make photo x-section " + id );
    long pid = prepareXSection( id, type, from, to, nick, azimuth, clino );
    if ( pid >= 0 ) {
      // imageFile := survey / photo / sectionId .jpg
      String image_filepath = TDPath.getSurveyJpgFile( TDInstance.survey, id ); // 20230118 local var "image_filepath"
      // File imagefile = TDFile.getTopoDroidFile( image_filepath );
      // TODO TD_XSECTION_PHOTO
      mMediaManager.setComment( nick ); // save xsection comment in the photo
      doTakePointPhoto( image_filepath, false, pid, MediaInfo.TYPE_XSECTION ); // without inserter
    }
  }

  /** make a X-Section from a section-line
   * @param line    "section" line
   * @param id      section ID, eg "xx0"
   * @param type    either PLOT_SECTION or PLOT_H_SECTION
   * @param from    from station, eg "1"
   * @param to      to station, eg "2"
   * @param nick    xsection comment
   * @param azimuth section azimuth
   * @param clino   section clino
   * @param tt      intersection abscissa (leg xsection), 2 (multileg xsection)
   * @param center  center (multileg xsection)
   */
  void makePlotXSection( DrawingLinePath line, String id, long type, String from, String to, String nick, float azimuth, float clino, float tt, Vector3D center )
  {
    // TDLog.v( "PLOT make XSection: " + id + " <" + from + "-" + to + "> azimuth " + azimuth + " clino " + clino + " tt " + tt );
    // TDLog.v( "PLOT make xsection: " + mCurrentLine );
    long pid = prepareXSection( id, type, from, to, nick, azimuth, clino );
    if ( pid >= 0 ) {
      // TDLog.v( "push info: " + type + " <" + mSectionName + "> TT " + tt );
      if ( tt <= 1.0 ) {
        mApp_mData.updatePlotIntercept( pid, TDInstance.sid, tt );
      } else {
        mApp_mData.updatePlotCenter( pid, TDInstance.sid, center );
      }
      pushInfo( type, mSectionName, from, to, azimuth, clino, tt, center );
    } 
    // zoomFit( mDrawingSurface.getBitmapBounds( 1.0f ) );
  }

  /** open the xsection scrap in the window
   * @param scrapname fullname of the scrap
   * the name can be the scrap-name or the section-name (plot name)
   * @note called only by DrawingPointDialog and onLongClick()
   */
  void openXSectionDraw( String scrapname )
  { 
    // remove survey name from scrap-name (if necessary)
    int pos = TDInstance.survey.length() + 1; // TDInstance.survey + "-" (at the beginning)
    String name = scrapname.substring( pos );
    // TDLog.v( "PLOT open xsection: scrapname " + scrapname + " plot name " + name );
    // TDLog.v( "PLOT open section: current line " + mCurrentLine );

    PlotInfo pi = mApp_mData.getPlotInfo( TDInstance.sid, name );
    if ( pi != null ) {
      Vector3D center = (pi.intercept <= 1 ) ? null : pi.center;
      pushInfo( pi.type, pi.name, pi.start, pi.view, pi.azimuth, pi.clino, pi.intercept, center );
      // zoomFit( mDrawingSurface.getBitmapBounds( 1.0f ) );
    }
  }

  // --------------------------------------------------------------------------
  // NO_PNM
  
  // /** save the sketch as PNM image
  //  * @param uri   output URI
  //  * @param type  plot type
  //  */
  // private void savePnm( Uri uri, long type )
  // {
  //   String fullname = null;
  //   if ( PlotType.isAnySection( type ) ) { 
  //     fullname = mFullName3;
  //   } else if ( PlotType.isProfile( type ) ) { 
  //     fullname = mFullName2;
  //   } else {
  //     fullname = mFullName1;
  //   }
  //   // TDLog.v("save PNM. uri " + ( (uri!=null)? uri.toString() : "null" ) );
  // 
  //   if ( fullname != null ) {
  //     DrawingCommandManager manager = mDrawingSurface.getManager( type );
  //     if ( PlotType.isAnySection( type ) ) { 
  //       doSavePnm( uri, manager, type, fullname );
  //     } else if ( PlotType.isProfile( type ) ) { 
  //       // Nota Bene OK for projected profile (to check)
  //       doSavePnm( uri, manager, (int)PlotType.PLOT_EXTENDED, fullname );
  //     } else {
  //       // doSavePnm( manager, (int)PlotType.PLOT_PLAN, fullname );
  //       doSavePnm( uri, manager, type, fullname );
  //     }
  //   }
  // }

  // /** internal save the sketch as PNM image
  //  * @param uri       output URI
  //  * @param manager   plot items 
  //  * @param type      plot type
  //  * @param filename  ...
  //  */
  // private void doSavePnm( Uri uri, DrawingCommandManager manager, long type, final String filename )
  // {
  //   if ( manager == null ) {
  //     TDToast.makeBad( R.string.null_bitmap );
  //     return;
  //   }
  //   Bitmap bitmap = manager.getBitmap( );
  //   if ( bitmap == null ) {
  //     TDToast.makeBad( R.string.null_bitmap );
  //     return;
  //   }
  //   float scale = manager.getBitmapScale();
  //   String format = getResources().getString( R.string.saved_file_2 );
  //   // if ( ! TDSetting.mExportUri ) uri = null; // FIXME_URI
  //   // TDLog.v( "do save PNG - uri " + ((uri!=null)? uri.toString() : "null") + " filename " + filename );
  //   new ExportBitmapToPnm( uri, format, bitmap, scale, filename, true ).execute();
  // }

  // --------------------------------------------------------------------------
  // NO_PNG

  // /** save the sketch as PNG image
  //  * @param uri   output URI
  //  * @param type  plot type
  //  */
  // private void savePng( Uri uri, long type )
  // {
  //   String fullname = null;
  //   if ( PlotType.isAnySection( type ) ) { 
  //     fullname = mFullName3;
  //   } else if ( PlotType.isProfile( type ) ) { 
  //     fullname = mFullName2;
  //   } else {
  //     fullname = mFullName1;
  //   }
  //   // TDLog.v("save PNG. uri " + ( (uri!=null)? uri.toString() : "null" ) );
  // 
  //   if ( fullname != null ) {
  //     DrawingCommandManager manager = mDrawingSurface.getManager( type );
  //     if ( PlotType.isAnySection( type ) ) { 
  //       doSavePng( uri, manager, type, fullname );
  //     } else if ( PlotType.isProfile( type ) ) { 
  //       // Nota Bene OK for projected profile (to check)
  //       doSavePng( uri, manager, (int)PlotType.PLOT_EXTENDED, fullname );
  //     } else {
  //       // doSavePng( manager, (int)PlotType.PLOT_PLAN, fullname );
  //       doSavePng( uri, manager, type, fullname );
  //     }
  //   }
  // }

  // /** internal save the sketch as PNG image
  //  * @param uri       output URI
  //  * @param manager   plot items 
  //  * @param type      plot type
  //  * @param filename  ...
  //  */
  // private void doSavePng( Uri uri, DrawingCommandManager manager, long type, final String filename )
  // {
  //   if ( manager == null ) {
  //     TDToast.makeBad( R.string.null_bitmap );
  //     return;
  //   }
  //   Bitmap bitmap = manager.getBitmap( );
  //   if ( bitmap == null ) {
  //     TDToast.makeBad( R.string.null_bitmap );
  //     return;
  //   }
  //   float scale = manager.getBitmapScale();
  //   String format = getResources().getString( R.string.saved_file_2 );
  //   // if ( ! TDSetting.mExportUri ) uri = null; // FIXME_URI
  //   // TDLog.v( "do save PNG - uri " + ((uri!=null)? uri.toString() : "null") + " filename " + filename );
  //   new ExportBitmapToFile( uri, format, bitmap, scale, filename, true ).execute();
  // }

  // PDF ------------------------------------------------------------------
  /** save as PDF file
   * @param uri      export URI
   * @param type     plot type
   */
  private void savePdf( Uri uri, long type ) 
  {
    String fullname = null;
    if ( PlotType.isAnySection( type ) ) { 
      fullname = mFullName3;
    } else if ( PlotType.isProfile( type ) ) { 
      fullname = mFullName2;
    } else {
      fullname = mFullName1;
    }

    if ( fullname != null ) {
      DrawingCommandManager manager = mDrawingSurface.getManager( type );
      // if ( ! TDSetting.mExportUri ) uri = null; // FIXME_URI
      doSavePdf( uri, manager, fullname );
    }
  }

  // TODO with background task
  /** save as PDF file
   * @param uri      export URI
   * @param manager  drawing items
   * @param fullname plot fullname, for the toast and for filesystem-based export
   */
  private void doSavePdf( Uri uri, DrawingCommandManager manager, final String fullname )
  {
    if ( manager == null ) {
      TDToast.makeBad( R.string.null_bitmap );
      return;
    }
    if ( TDandroid.BELOW_API_19 ) { // Android-4.4 (KITKAT)
      TDToast.makeBad( R.string.no_feature_pdf );
      return;
    }
    // TDPath.getPdfDir();
    // TDLog.v( "PDF export <" + fullname + ">");
    ParcelFileDescriptor pfd = TDsafUri.docWriteFileDescriptor( uri );
    if ( pfd == null ) return;
    try {
      // OutputStream fos = (pfd != null)? TDsafUri.docFileOutputStream( pfd ) : new FileOutputStream( TDPath.getPdfFileWithExt( fullname ) );
      OutputStream fos = TDsafUri.docFileOutputStream( pfd );

      // PrintAttributes.Builder builder = new PrintAttributes.Builder();
      // builder.setColorMode( PrintAttributes.COLOR_MODE_COLOR );
      // if ( TDandroid.AT_LEAST_API_23 ) builder.setDuplexMode( PrintAttributes.DUPLEX_MODE_NONE ); // not less than Android-6 (M)
      // builder.setMediaSize( PrintAttributes.MediaSize.ISO_A2 ); // 420 x 594 ( 16.54 x 23.39 )
      // builder.setMinMargins( PrintAttributes.Margins.NO_MARGINS );
      // // TDLog.v( "display " + TopoDroidApp.mDisplayWidth + " x " + TopoDroidApp.mDisplayHeight );
      // builder.setResolution( new PrintAttributes.Resolution( "300", "300 dpi", 300, 300 ) );
      // PrintedPdfDocument pdf = new PrintedPdfDocument( TDInstance.context, builder.build() );

      PageInfo info = getPdfPage( manager );
      PdfDocument pdf = new PdfDocument( );
      Page page = pdf.startPage( info );

      // page.getCanvas().drawColor( 0 ); // TDSetting.mBitmapBgcolor );
      // page.getCanvas().drawRect( new RectF(0,0,zw,zh), BrushManager.blackPaint ); // TDSetting.mBitmapBgcolor );
      manager.executeAll( page.getCanvas(), -1.0f, null, true ); // zoom is 1.0, true = inverted_color
      // manager.executeAll( page.getCanvas(), -1.0f, null ); // zoom is 1.0
      pdf.finishPage( page );
      pdf.writeTo( fos );
      pdf.close();
      fos.close();
      TDToast.make( String.format( getResources().getString(R.string.saved_file_1), fullname ) );
    // } catch ( NoSuchMethodException e ) {
    } catch ( IOException e ) {
      TDLog.e("failed PDF export " + e.getMessage() );
    } finally {
      TDsafUri.closeFileDescriptor( pfd );
    }
  }

  // CSX ------------------------------------------------------------------
  /** save as cSurvey - used also by SavePlotFileTask
   * @param uri     export URI or null (to export in private folder)
   * @param origin  origin station (?)
   * @param psd1    plan plot save-data
   * @param psd2    profile plot save-data
   * @param toast   whether to toast
   * @note used also by SavePlotFileTask
   */
  void doSaveCsx( Uri uri, String origin, PlotSaveData psd1, PlotSaveData psd2, boolean toast )
  {
    // TDLog.v( "save csx");
    // if ( ! TDSetting.mExportUri ) uri = null; // FIXME_URI
    TopoDroidApp.exportSurveyAsCsxAsync( mActivity, uri, origin, psd1, psd2, toast );
  }

  /** used to save "dxf", "svg" - called only by doExport
   * @param uri   export URI
   * @param type  export type
   * @param ext   extension
   */
  private void saveWithExt( Uri uri, long type, String ext ) 
  {
    TDNum num = mNum;
    // TDLog.Log( TDLog.LOG_IO, "export plot type " + type + " with extension " + ext );
    // TDLog.v( "save with ext. plot type " + type + " with extension " + ext );
    // if ( ! TDSetting.mExportUri ) uri = null; // FIXME_URI
    // if ( "png".equals( ext ) ) { // NO_PNG
    //   savePng( uri, type );
    // } else
    if ( "pdf".equals( ext ) ) {
      savePdf( uri, type );
    } else {
      if ( PlotType.isAnySection( type ) ) { 
        DrawingCommandManager manager = mDrawingSurface.getManager( type );
        String fullname = mFullName3;
        if ( "csx".equals( ext ) /* || "png".equals( ext ) */ ) { // NO_PNG
          // doSavePng( uri, manager, type, fullname );  // NO_PNG
        } else {
          doSaveWithExt( uri, num, manager, type, fullname, ext, true ); 
        }
      } else {
        if ( ext.equals("c3d") ) {
          DrawingCommandManager manager = mDrawingSurface.getManager( type );
          if ( PlotType.isProfile( type ) ) {
            doSaveWithExt( uri, num, manager, type, mFullName2, ext, true ); 
          } else if ( type == PlotType.PLOT_PLAN ) {
            doSaveWithExt( uri, num, manager, type, mFullName1, ext, true ); 
          } else {
            // TDLog.v( "save xsection as c3d");
            doSaveWithExt( uri, num, manager, type, mFullName3, ext, true ); 
          }
        } else {
          if ( PlotType.isProfile( type ) ) {
            DrawingCommandManager manager2 = mDrawingSurface.getManager( mPlot2.type );
            String fullname2 = mFullName2;
            doSaveWithExt( uri, num, manager2, mPlot2.type, fullname2, ext, true); 
          } else if ( type == PlotType.PLOT_PLAN ) {
            DrawingCommandManager manager1 = mDrawingSurface.getManager( mPlot1.type );
            String fullname1 = mFullName1;
            doSaveWithExt( uri, num, manager1, mPlot1.type, fullname1, ext, true); 
          }
        }
      }
    }
  }

  // ext file extension (--> saving class)
  // ext can be dxf, svg
  // FIXME OK PROFILE
  // used also by SavePlotFileTask
  void doSaveWithExt( Uri uri, TDNum num, DrawingCommandManager manager, long type, final String filename, final String ext, boolean toast )
  {
    // TDLog.Log( TDLog.LOG_IO, "save with ext: " + filename + " ext " + ext );
    // TDLog.v( "SAVE with ext: filename " + filename + " ext " + ext );
    // mActivity = context (only to toast)
    SurveyInfo info  = mApp_mData.selectSurveyInfo( mSid );
    PlotInfo   plot  = null;
    FixedInfo  fixed = null;
    GeoReference station = null;

    if ( type == PlotType.PLOT_PLAN && ext.equals("shz") ) {
      String origin = num.getOriginStation();
      station = TDExporter.getGeolocalizedStation( mSid, mApp_mData, 1.0f, true, origin, true );
    // } else if ( ext.equals("c3d") ) { // NO_C3D
    //   // c3d export uses plot and fixed instead of station
    //   plot  = PlotType.isAnySection(type) ? mPlot3 : PlotType.isProfile( type )? mPlot2 : mPlot1;
    //   List<FixedInfo> fixeds = mApp_mData.selectAllFixed( mSid, TDStatus.NORMAL );
    //   if ( fixeds != null && fixeds.size() > 0 ) fixed = fixeds.get( 0 );
    //   // TDLog.v("C3D saving " + filename + " fixeds " + fixeds.size() + " fixed " + fixed );
    //   if ( fixed == null ) fixed = new FixedInfo( -1, num.getOriginStation(), 0, 0, 0, 0, "", 0, -1, -1 ); // NOTE ACCURACY -1 (unset)
    }
    // if ( ! TDSetting.mExportUri ) uri = null; // FIXME_URI
    new ExportPlotToFile( mActivity, uri, info, plot, fixed, num, manager, type, filename, ext, toast, station ).execute();
  }

  // static private Handler th2Handler = null;

  /** save in therion format (.th2)
   * @param uri     output URI
   * @param type    type of ?
   * @param toast   whether to toast
   *
   * called (indirectly) only by ExportDialogPlot: save as th2 even if there are missing symbols
   * no backup_rotate (rotate = 0)
   */
  private void doSaveTh2( Uri uri, long type, final boolean toast )
  {
    DrawingCommandManager manager = mDrawingSurface.getManager( type );
    if ( manager == null ) return;
    Handler th2Handler = null;

    int save_mode = PlotSave.EXPORT;
    int azimuth = 0;
    int oblique = 0;
    String name = null;
    PlotInfo info = null;
    if ( type == PlotType.PLOT_PLAN ) {
      name = mFullName1;
      info = mPlot1;
    } else if ( PlotType.isProfile( type ) ) {
      azimuth = (int)mPlot2.azimuth;
      oblique = (int)mPlot2.clino;
      name = mFullName2;
      info = mPlot2;
    } else {
      name = mFullName3;
      info = mPlot3;
    }
    final String filename = name;
    // TDLog.v( "save th2: type " + type + " file " + filename );
    if ( toast ) {
      th2Handler = new Handler(){
        @Override public void handleMessage(Message msg) {
          if (msg.what == 661 ) {
            TDToast.make( String.format( getString(R.string.saved_file_1), filename ) ); 
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
      // TDLog.v( "save th2 origin " + mPlot1.xoffset + " " + mPlot1.yoffset + " toTherion " + TDSetting.mToTherion );
      // if ( ! TDSetting.mExportUri ) uri = null; // FIXME_URI
      if ( mTh2Edit ) { // TH2EDIT 
        // set type by the scrap projection 
        (new SavePlotFileTask( mActivity, uri, this, th2Handler, null, manager, null, name, type, 0, 0, PlotSave.EXPORT, 0, true )).execute();
      } else {
        (new SavePlotFileTask( mActivity, uri, this, th2Handler, mNum, manager, info, name, type, azimuth, oblique, save_mode, 0, false )).execute();
      }
    } catch ( RejectedExecutionException e ) {
      TDLog.e("Sketch saving exec rejected");
    }
  }

  
  // @Override
  // public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo info )
  // {
  //   super.onCreateContextMenu( menu, v, info );
  //   getMenuInflater().inflate( R.menu.popup, menu );
  //   menu.setHeaderTitle( "Context Menu" );
  //   // TDLog.v( "PLOT on Create Context Menu view " + v.toString()  );
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

  /** (re)compute the reference, for both plan and profile
   * @param reset whether to reset the reference
   * @note called by updateBlockName and refreshDisplay - hence false params for resetReference
   */
  private void doComputeReferences( boolean reset )
  {
    // TDLog.v( "PLOT compute ref type " + mType + " reset " + reset );
    List< DBlock > list = mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );
    mMultiBad = new ArrayList< StringPair >();
    mNum = new TDNum( list, mPlot1.start, mDecl, mFormatClosure );
    // mNum.setBarrierAndHidden( mPlot1.view, mPlot1.hide ); 20250321
    if ( TDSetting.mLoopClosure == TDSetting.LOOP_SELECTIVE ) setMenuImageRed( mNum.nrInaccurateLoops > 0 );
    if ( mType == (int)PlotType.PLOT_PLAN ) {
      mNum.setBarrierAndHidden( mPlot2.view, mPlot2.hide ); // 20250321
      computeReferences( mNum, mPlot2.type, mPlot2.name, TopoDroidApp.mScaleFactor, true );

      mNum.setBarrierAndHidden( mPlot1.view, mPlot1.hide ); // 20250321
      computeReferences( mNum, mPlot1.type, mPlot1.name, TopoDroidApp.mScaleFactor, false );

      if ( reset ) resetReference( mPlot1, false );
    } else if ( PlotType.isProfile( mType ) ) {
      mNum.setBarrierAndHidden( mPlot1.view, mPlot1.hide ); // 20250321
      computeReferences( mNum, mPlot1.type, mPlot1.name, TopoDroidApp.mScaleFactor, false );

      mNum.setBarrierAndHidden( mPlot2.view, mPlot2.hide ); // 20250321
      computeReferences( mNum, mPlot2.type, mPlot2.name, TopoDroidApp.mScaleFactor, true );

      if ( reset ) resetReference( mPlot2, false );
    }
  }

  /** refresh the display
   * @param nr    error code (if negative), OK if positive
   * @param toast whether to toast a message
   */
  public void refreshDisplay( int nr, boolean toast )
  {
    mActivity.setTitleColor( TDColor.TITLE_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        doComputeReferences( false );
      }
      if ( toast ) {
        if ( TDInstance.isDeviceX310() ) nr /= 2;
        TDToast.make( getResources().getQuantityString(R.plurals.read_data, nr, nr ) );
      }
    } else { // ( nr < 0 )
      if ( toast ) {
        // TDToast.makeBad( getString(R.string.read_fail_with_code) + nr );
        TDToast.makeBad( mApp.DistoXConnectionError[ -nr ] );
      }
    }
  }

  /** update the shot+leg reference
   * @note called only by updateBlockList()
   * boolean compute = true
   * boolean reference = false
   */
  private void updateDisplay( )
  {
    if ( mType != (int)PlotType.PLOT_PLAN && ! PlotType.isProfile( mType ) ) {
      // FIXME_SK resetReference( mPlot3 );
      doRestart();
      updateSplays( mApp.mSplayMode );
    } else {
      List< DBlock > list = mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );
      mMultiBad = new ArrayList< StringPair >();
      TDNum num = new TDNum( list, mPlot1.start, mDecl, mFormatClosure );
      mNum.setBarrierAndHidden( mPlot1.view, mPlot1.hide );
      if ( TDSetting.mLoopClosure == TDSetting.LOOP_SELECTIVE ) setMenuImageRed( num.nrInaccurateLoops > 0 );
      recomputeReferences( num, TopoDroidApp.mScaleFactor );
      mNum = num;
    }
  }

  /** incremental update of the shot+leg reference
   * @param blk_id    last data block ID
   * @param got_leg   whether a leg has been received
   */
  private void incrementalUpdateDisplay( long blk_id, boolean got_leg )
  {
    if ( mType != (int)PlotType.PLOT_PLAN && ! PlotType.isProfile( mType ) ) return;
    if ( mNum == null ) {
      List< DBlock > list = mApp_mData.selectAllShots( mSid, TDStatus.NORMAL );
      mMultiBad = new ArrayList< StringPair >();
      TDNum num = new TDNum( list, mPlot1.start, mDecl, mFormatClosure );
      mNum.setBarrierAndHidden( mPlot1.view, mPlot1.hide );
      if ( TDSetting.mLoopClosure == TDSetting.LOOP_SELECTIVE ) setMenuImageRed( num.nrInaccurateLoops > 0 );
      recomputeReferences( num, TopoDroidApp.mScaleFactor );
      mNum = num;
    } else {
      DBlock blk = mApp_mData.selectShot( blk_id, mSid );
      DBlock leg = got_leg ? mApp_mData.selectLastLegShot( mSid ) : null;
      boolean ret = mNum.appendData( blk, leg, mFormatClosure );
      // TDLog.v("DATA " + "drawing window calls append data " + blk.mId + " ret " + ret );
      if ( ret ) {
        if ( got_leg ) { // drop last splay - insert last leg
          mMultiBad.clear(); // nr_multi_bad = 0;
          nr_magnetic_bad = 0;
          mNum.dropLastSplay();
          mDrawingSurface.dropLastSplayPath( mPlot1.type );
          mDrawingSurface.dropLastSplayPath( mPlot2.type );
          // 
          NumShot sh = mNum.getLastShot();
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
          NumStation st0 = StationPolicy.isSurveyBackward() ? st1 : st2;
          if ( st1.show() && st2.show() ) {
            // DBlock blk1 = sh.getFirstBlock(); // same as leg
            // TDLog.v("DATA " + "LEG blk " + blk.mId + " blk1 " + blk1.mId + " leg " + leg.mId );
            appendFixedLine( PlotType.PLOT_PLAN, leg, st1.e, st1.s, st2.e, st2.s, sh.getReducedExtend(), false, true );
            mDrawingSurface.appendDrawingStationName( mPlot1.type, st0, DrawingUtil.toSceneX(st0.e, st0.s), DrawingUtil.toSceneY(st0.e, st0.s), true );
            if ( PlotType.isExtended( mPlot2.type ) ) {
              if ( st1.hasExtend() && st2.hasExtend() ) {
                appendFixedLine( mPlot2.type, leg, st1.h, st1.v, st2.h, st2.v, sh.getReducedExtend(), false, true );
              }
              mDrawingSurface.appendDrawingStationName( mPlot2.type, st0, DrawingUtil.toSceneX(st0.h, st0.v), DrawingUtil.toSceneY(st0.h, st0.v), true );
            } else if ( PlotType.isProfile( mPlot2.type ) ) {
              double cosp = TDMath.cosDd( mPlot2.azimuth );
              double sinp = TDMath.sinDd( mPlot2.azimuth );
              double h1 = st1.e * cosp + st1.s * sinp;
              double h2 = st2.e * cosp + st2.s * sinp;
              double h0 = StationPolicy.isSurveyBackward() ? h1 : h2;
              appendFixedLine( mPlot2.type, leg, h1, st1.v, h2, st2.v, sh.getReducedExtend(), false, true );
              mDrawingSurface.appendDrawingStationName( mPlot2.type, st0, DrawingUtil.toSceneX(h0, st0.v), DrawingUtil.toSceneY(h0, st0.v), true );
            } 
          }
          if ( mMultiBad.size() > 0 ) { // nr_multi_bad > 0
            setMenuImageRed( true );
            // TDToast.makeWarn( R.string.survey_bad_siblings );
          // } else if ( nr_magnetic_bad > 0 ) {
          //   TDToast.makeWarn( R.string.survey_bad_magnetic );
          }
        } else { // insert last splay
          NumSplay sp = mNum.getLastSplay();
          if ( Math.abs( sp.getBlock().mClino ) < TDSetting.mSplayVertThrs ) { // include only splays with clino below mSplayVertThrs
            NumStation st = sp.from;
            if ( st.show() ) {
              DBlock blk2 = sp.getBlock();
              if ( ! blk2.isNoPlan() ) appendFixedLine( mPlot1.type, blk2, st.e, st.s, sp.e, sp.s, sp.getCosine(), true, true );
              if ( ! blk2.isNoProfile() ) {
                if ( PlotType.isExtended( mPlot2.type ) ) {
                  if ( st.hasExtend() ) {
                    appendFixedLine( mPlot2.type, blk2, st.h, st.v, sp.h, sp.v, sp.getCosine(), true, true );
                  }
                } else if ( PlotType.isProfile( mPlot2.type ) ) {
                  double cosp = TDMath.cosDd( mPlot2.azimuth );
                  double sinp = TDMath.sinDd( mPlot2.azimuth );
                  double h1 = st.e * cosp + st.s * sinp;
                  double h2 = sp.e * cosp + sp.s * sinp;
                  float cosine = (float)( TDMath.sind( blk2.mBearing ) * sinp + TDMath.cosd( blk2.mBearing ) * cosp ); // instead of sp.getCosine()
                  appendFixedLine( mPlot2.type, blk2, h1, st.v, h2, sp.v, cosine, true, true );
                }
              }
            }
          }
        }
      }
    }
  }

  /** fit the view to the sketch
   * @param b    fitting rectangle
   */
  private boolean zoomFit( RectF b )
  {
    float tb = (b.top + b.bottom)/2;
    float lr = (b.left + b.right)/2;
    if ( mLandscape ) {
      float w = b.bottom - b.top;
      float h = b.right - b.left;
      float wZoom = (float) ( mDrawingSurface.getMeasuredWidth() * 0.9 ) / ( 1 + w );
      float hZoom = (float) ( ( ( mDrawingSurface.getMeasuredHeight() - mListView.getHeight() ) * 0.9 ) / ( 1 + h ));
      float zoom = Math.min(hZoom, wZoom);
      if ( zoom <= 0.0f ) return false;
      mZoom = zoom;
      // if ( TDSetting.mZoomLowerBound > 0.0f && mZoom < TDSetting.mZoomLowerBound ) {
      //   mZoom = TDSetting.mZoomLowerBound;
      // }
      mOffset.y = ( TopoDroidApp.mDisplayHeight + mListView.getHeight() - DrawingUtil.CENTER_Y )/(2*mZoom) + lr;
      mOffset.x = ( TopoDroidApp.mDisplayWidth - DrawingUtil.CENTER_X )/(2*mZoom) - tb;
    } else {
      float w = b.right - b.left;
      float h = b.bottom - b.top;
      float wZoom = (float) ( mDrawingSurface.getMeasuredWidth() * 0.9 ) / ( 1 + w );
      float hZoom = (float) ( ( ( mDrawingSurface.getMeasuredHeight() - mListView.getHeight() ) * 0.9 ) / ( 1 + h ));
      float zoom = Math.min(hZoom, wZoom);
      if ( zoom <= 0.0f ) return false;
      mZoom = zoom;
      // TDLog.v("Zoom Fit W " + w + " H " + h + " zoom " + mZoom );
      // if ( TDSetting.mZoomLowerBound > 0.0f && mZoom < TDSetting.mZoomLowerBound ) {
      //   mZoom = TDSetting.mZoomLowerBound;
      // }
      mOffset.x = ( TopoDroidApp.mDisplayWidth - DrawingUtil.CENTER_X )/(2*mZoom) - lr;
      mOffset.y = ( TopoDroidApp.mDisplayHeight + mListView.getHeight() - DrawingUtil.CENTER_Y )/(2*mZoom) - tb;
    }
    // TDLog.v( "W " + w + " H " + h + " zoom " + mZoom + " X " + mOffset.x + " Y " + mOffset.y );
    // TDLog.v( "display " + TopoDroidApp.mDisplayWidth + " " + TopoDroidApp.mDisplayHeight );
    // TDLog.v( "PLOT zoom fit " + mOffset.x + " " + mOffset.y + " zoom " + mZoom + " tb " + tb + " lr " + lr );
    mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    return true;
  }

  // called when the data reduction changes (hidden/barrier)
  private void recomputeReferences( TDNum num, float zoom )
  {
    if ( num == null ) return;
    // mLastLinePath = null; // not needed

    // if ( mType == (int)PlotType.PLOT_PLAN ) { 20250321
    //   if ( mPlot2 != null ) computeReferences( num, mPlot2.type, mPlot2.name, zoom, false );
    // } else if ( PlotType.isProfile( mType ) ) {
    //   computeReferences( num, mPlot1.type, mPlot1.name, zoom, false );
    // }
    computeReferences( num, (int)mType, mName, zoom, false );
  }

  /** forward the update request to the Shot Window
   * @param blk_id    data block id
   */
  @Override
  public void updateBlockList( long blk_id )
  {
    // TDLog.v("DRAWING " + TDLog.threadId() + " update block list" );
    if ( TopoDroidApp.mShotWindow != null ) {
      TopoDroidApp.mShotWindow.updateBlockList( blk_id ); // FIXME_EXTEND needed to update sketch splays immediately on download
    }
  }

  /** handle data update notifications
   * @param blk_id  update starting with this blk id
   * @param got_leg whether the latest splay is actually a leg
   */
  public void notifyUpdateDisplay( long blk_id, boolean got_leg )
  {
    // TDLog.v("DATA " + "drawing window: notified update display id " + blk_id ); 
    if ( StationPolicy.isSurveyBackward1() ) { // splays+backward can update display only on receiving a leg
      if ( got_leg ) updateDisplay( );
    } else if ( TDLevel.overExpert ) { // tester-level uses incremental update
      incrementalUpdateDisplay( blk_id, got_leg );
    } else {
      updateDisplay( );
    }
  }

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
      case KeyEvent.KEYCODE_VOLUME_UP:   // (24)
        takeScreenshot( mDrawingSurface );
        return true;
      case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        TDLog.e( "key down: code " + code );
    }
    return false;
  }

  // ---------------------------------------------------------
  // MENU

  /** initialize the menu list
   * @param res      app resources
   * @param type     plot type
   */
  private void setMenuAdapter( Resources res, long type )
  {
    TDLog.v("Drawing Activity set menu adapter");
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<>(mActivity, R.layout.menu );
    if ( PlotType.isSketch2D( type ) && TDLevel.overNormal && ! mTh2Edit ) { // TH2EDIT
      menu_adapter.add( res.getString( menus[0] ) ); // SWITCH/CLOSE
    } else {
      menu_adapter.add( res.getString( menus[MENU_CLOSE] ) );  // CLOSE
    }
    if ( mTh2Edit ) { // TH2EDIT
      menu_adapter.add( res.getString( menus[MENU_SAVE] ) );  // TH2EDIT SAVE
    } else {
      menu_adapter.add( res.getString( menus[1] ) );  // EXPORT
    }
    if ( ! mTh2Edit ) { // TH2EDIT
      if ( PlotType.isAnySection( type ) ) {
        menu_adapter.add( res.getString( menus[MENU_AREA] ) );  // AREA
      } else {
        menu_adapter.add( res.getString( menus[2] ) );  // INFO
      }
    }
    if ( TDLevel.overNormal ) {
      if ( mTh2Edit ) { // TH2EDIT
        menu_adapter.add( res.getString( menus[MENU_OPEN] ) );  // TH2EDIT OPEN
      } else {
        menu_adapter.add( res.getString( menus[3] ) );  // RELOAD
      }
      menu_adapter.add( res.getString( menus[4] ) );  // ZOOM-FIT
    }
    if ( TDLevel.overTester ) {
      menu_adapter.add( res.getString( menus[5] ) ); // STATION SEARCH and HIGHLIGHT
    }
    if ( TDLevel.overAdvanced && (! mTh2Edit) && PlotType.isSketch2D( type ) ) { // TH2EDIT
      menu_adapter.add( res.getString( menus[6] ) ); // RENAME/DELETE
    }
    if ( TDLevel.overAdvanced && ( PlotType.isSketch2D( type ) || mTh2Edit ) ) { // TH2EDIT
      menu_adapter.add( res.getString( menus[7] ) ); // SCRAPS
    }
    menu_adapter.add( res.getString( menus[8] ) ); // PALETTE
    if ( TDLevel.overBasic && (! mTh2Edit) && PlotType.isSketch2D( type ) ) { // TH2EDIT
      menu_adapter.add( res.getString( menus[9] ) ); // OVERVIEW
    }
    menu_adapter.add( res.getString( menus[10] ) ); // OPTIONS
    menu_adapter.add( res.getString( menus[11] ) ); // HELP
    mMenu.setAdapter( menu_adapter );
    mMenu.invalidate();
  }

  /** close the menu popup
   */
  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  /** change the zoom to fit the drawing to the screen
   */
  private boolean doZoomFit()
  {
    if ( mDrawingSurface.isFixedZoom() ) return false;
    // FIXME FIXED_ZOOM for big sketches this leaves out some bits at the ends
    // maybe should increase the bitmap bounds by a small factor ...
    RectF b = mDrawingSurface.getBitmapBounds( 1.0f );
    return zoomFit( b );
  }

  /** center the view at a station
   * @param station   station name
   */
  void centerAtStation( String station )
  {
    NumStation st = mNum.getStation( station );
    if ( st == null ) {
      TDToast.makeBad( R.string.missing_station );
    } else {
      // TDLog.v( "center at station " + station );
      // moveTo( mPlot1.type, station, false );
      // moveTo( mPlot2.type, station, false );
      moveTo( (int)mType, station, false );
      // TDLog.v( "PLOT center station " + mOffset.x + " " + mOffset.y + " " + mZoom );
      mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    }
  }

  /** set the view orientation
   * @param orientation   requested view-orientation
   * @note called from ZoomFitDialog
   */
  void setOrientation( int orientation )
  {
    boolean ret = false;
    boolean landscape = (orientation == PlotInfo.ORIENTATION_LANDSCAPE);
    if ( landscape != mLandscape ) {
      mLandscape = landscape;
      // if ( mLandscape ) {
      //         float t = mOffset.x; mOffset.x = mOffset.y;  mOffset.y = -t;
      // } else {
      //         float t = mOffset.x; mOffset.x = -mOffset.y;  mOffset.y = t;
      // }
      mApp_mData.updatePlotOrientation( TDInstance.sid, mPid, mLandscape ? 1 : 0 );
      // TDLog.v( "PLOT orientation " + mOffset.x + " " + mOffset.y + " " + mZoom );
      mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
      ret = doZoomFit();
      if ( ret ) setTheTitle();
    } else {
      ret = doZoomFit();
    }
    if ( ! ret ) {
      TDToast.makeWarn( R.string.failed_zoomfit );
    }
  }

  private void handleMenu( int pos )
  {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) {
        if ( ! mTh2Edit && PlotType.isSketch2D( mType ) ) { // SWITCH - CLOSE TH2EDIT
          if ( TDLevel.overNormal ) {
            new PlotListDialog( mActivity, null, mApp, this ).show();
          } else {
            super.onBackPressed();
          }
        } else { // close
          super.onBackPressed();
        }
      } else if ( p++ == pos ) { // EXPORT - SAVE
        if ( mTh2Edit ) { // TH2EDIT export Therion
          // int th_pos = 0; // Therion index, unused? - 20230118 local var "th_pos"
          // TDLog.v("DRAW save therion");
          // API_19
          selectFromProvider( TDConst.SURVEY_FORMAT_TH2, TDRequest.REQUEST_GET_EXPORT, Intent.ACTION_CREATE_DOCUMENT );
        } else {
          String plotname1 = TDInstance.survey + "-" + mName;
          String plotname2 = null;
          if ( TDLevel.overExpert ) {
            if ( PlotType.isProfile( mType ) ) {
              plotname2 = TDInstance.survey + "-" + mName1;
            } else if (  PlotType.isPlan( mType ) ) {
              plotname2 = TDInstance.survey + "-" + mName2;
            } 
          }
          // TDLog.v("export " + plotname1 + " " + plotname2 );
          new ExportDialogPlot( mActivity, this, TDConst.mPlotExportTypes, R.string.title_plot_save, plotname1, plotname2 ).show();
        }
      } else if ( ( ! mTh2Edit ) && p++ == pos ) { // TH2EDIT INFO - AREA
        if ( PlotType.isAnySection( mType ) ) {
          float area = mDrawingSurface.computeSectionArea() / (DrawingUtil.SCALE_FIX * DrawingUtil.SCALE_FIX);
          Resources res = getResources();
          String msg = String.format( res.getString( R.string.section_area ), area );
          TopoDroidAlertDialog.makeAlert( mActivity, res, msg, R.string.button_ok, -1, null, null );
	} else {
          if ( mNum != null ) {
            float azimuth = -1;
            float oblique = 0;
            if ( isProfileProjected() ) {
              azimuth = mPlot2.azimuth;
              oblique = mPlot2.clino;
            }
            new DrawingStatDialog( mActivity, mNum, mPlot1.start, azimuth, oblique, mApp.getSurveyStat( TDInstance.sid ) ).show();
          } else {
            TDToast.makeBad( R.string.no_data_reduction );
	  }
	}
      } else if ( TDLevel.overNormal && p++ == pos ) { // RECOVER RELOAD - OPEN
        if ( mTh2Edit ) { // TH2EDIT API_19
          selectFromProvider( TDConst.SURVEY_FORMAT_TH2, TDRequest.REQUEST_GET_IMPORT, Intent.ACTION_OPEN_DOCUMENT );
        } else {
          Intent intent = new Intent( this, PlotReloadWindow.class );
          intent.putExtra( TDTag.TOPODROID_SURVEY_ID, mSid );
          intent.putExtra( TDTag.TOPODROID_PLOT_FROM, mFrom );
          intent.putExtra( TDTag.TOPODROID_PLOT_ZOOM, mZoom );
          intent.putExtra( TDTag.TOPODROID_PLOT_TYPE, mType );
          intent.putExtra( TDTag.TOPODROID_PLOT_LANDSCAPE, mLandscape );
          intent.putExtra( TDTag.TOPODROID_PLOT_XOFF, mOffset.x );
          intent.putExtra( TDTag.TOPODROID_PLOT_YOFF, mOffset.y );
          if ( PlotType.isProfile( mType ) ) {
            intent.putExtra( TDTag.TOPODROID_PLOT_FILENAME, mFullName2 );
            // ( new PlotRecoverDialog( mActivity, this, mFullName2, mType ) ).show();
          } else if ( mType == PlotType.PLOT_PLAN ) {
            intent.putExtra( TDTag.TOPODROID_PLOT_FILENAME, mFullName1 );
            // ( new PlotRecoverDialog( mActivity, this, mFullName1, mType ) ).show();
          } else {
            intent.putExtra( TDTag.TOPODROID_PLOT_FILENAME, mFullName3 );
            // ( new PlotRecoverDialog( mActivity, this, mFullName3, mType ) ).show();
          }
          startActivityForResult( intent, TDRequest.PLOT_RELOAD );
        }
      } else if ( TDLevel.overNormal && p++ == pos ) { // ZOOM-FIT / ORIENTATION
	if ( TDLevel.overExpert ) {
          ( new PlotZoomFitDialog( mActivity, this, mTh2Edit ) ).show(); // TH2EDIT added last param
	} else {
	  doZoomFit();
	}
      } else if ( TDLevel.overTester && p++ == pos ) { // STATION SEARCH and HIGHLIGHT
        ( new PlotSearchDialog( mActivity, this ) ).show();
      } else if ( TDLevel.overAdvanced && (! mTh2Edit) && PlotType.isSketch2D( mType ) && p++ == pos ) { // TH2EDIT RENAME - DELETE - SPLIT - OUTLINE - MERGE
        //   askDelete();
        boolean scrap_copy = (mSplitPaths != null);
        boolean has_outline = mDrawingSurface.hasPlotOutline();
        TDLog.v("RENAME etc. scrap_copy " + scrap_copy + " has_outline " + has_outline );
        (new PlotRenameDialog( mActivity, this, scrap_copy, has_outline )).show();
      } else if ( TDLevel.overAdvanced && ( PlotType.isSketch2D( mType ) || mTh2Edit ) && p++ == pos ) { // TH2EDIT SCRAPS
        (new PlotScrapsDialog( mActivity, this )).show();

      } else if ( p++ == pos ) { // PALETTE
        (new SymbolEnableDialog( mActivity )).show();

      } else if ( TDLevel.overBasic && (! mTh2Edit) && PlotType.isSketch2D( mType ) && p++ == pos ) { // TH2EDIT OVERVIEW
        if ( mType == PlotType.PLOT_PROJECTED ) {
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
        Intent intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
        intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_PLOT );
        mActivity.startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        // 1 for select-tool
        // int nn = 1 + NR_BUTTON1 + NR_BUTTON2 - 3 + NR_BUTTON5 - 5 + ( TDLevel.overBasic? mNrButton3 - 3: 0 );
        // TDLog.v( "Help menu, nn " + nn );
        switch ( mMode ) {
          case MODE_DRAW:
            new HelpDialog(mActivity, izons_draw, menus, help_icons_draw, help_menus, NR_BUTTON2, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
            break;
          case MODE_ERASE:
            new HelpDialog(mActivity, izons_erase, menus, help_icons_erase, help_menus, NR_BUTTON5, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
            break;
          case MODE_EDIT:
            new HelpDialog(mActivity, izons_edit, menus, help_icons_edit, help_menus, NR_BUTTON3, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
            break;
          default: // MODE_MOVE MODE_SPLIT_SKETCH MODE_SPLIT_SCRAP
            new HelpDialog(mActivity, izons_move, menus, help_icons_move, help_menus, NR_BUTTON1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
        }
      }
  }

  /** @return true if the profile is projected
   */
  private boolean isProfileProjected()
  {
    return ( mPlot2 !=  null && PlotType.PLOT_PROJECTED == mPlot2.type );
  }

  static private int     mExportIndex;
  static private String  mExportExt;

  /**
   * @param export_type  export type
   * @param filename     export filename
   * @param prefix       station names export-prefix (not used)
   * @param first        not-used
   * @param second       whether to export the second view instead of the current view (only for plan or profile)
   * @note called from ExportDialogPlot to do the export
   */
  public void doExport( String export_type, String filename, String prefix, long first, boolean second ) // EXPORT
  {
    if ( export_type == null ) return;
    mExportIndex  = TDConst.plotExportIndex( export_type );
    mExportExt    = TDConst.plotExportExt( export_type );
    // TDLog.v( "EXPORT do type " + export_type + " index " + mExportIndex + " ext " + mExportExt + " filename " + filename );
    // if ( TDSetting.mExportUri ) {
      // if ( mExportIndex == TDConst.SURVEY_FORMAT_C3D ) { // Cave3D NO_C3D
      //   saveWithExt( null, mType, mExportExt );
      // } else {
        // APP_OUT_DIR
        // Intent intent = new Intent( Intent.ACTION_CREATE_DOCUMENT );
        // intent.setType( TDConst.getMimeType( mExportIndex ) );
        // intent.addCategory(Intent.CATEGORY_OPENABLE);
        // intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        // // intent.putExtra( "exporttype", index ); // index is not returned to the app
        // intent.putExtra( Intent.EXTRA_TITLE, filename );
        // startActivityForResult( Intent.createChooser(intent, getResources().getString( R.string.export_plot_title ) ), TDRequest.REQUEST_GET_EXPORT );
        Uri uri = Uri.fromFile( new File( TDPath.getOutFile( filename ) ) );
        // TDLog.v("EXPORT " + TDPath.getOutFile( filename ) );
        if ( uri != null ) {
          doUriExport( uri, second );
        }
      // }
    // } else {
    //   if ( mExportIndex == TDConst.SURVEY_FORMAT_TH2 ) {
    //     doSaveTh2( null, mType, true );
    //   } else if (mExportIndex == TDConst.SURVEY_FORMAT_CSX ) {
    //     if ( ! PlotType.isAnySection( mType ) ) { // FIXME x-sections are saved PNG for CSX
    //       if ( mPlot1 != null ) {
    //         String origin = mPlot1.start;
    //         int save_mode    = PlotSave.EXPORT;
    //         PlotSaveData psd1 = makePlotSaveData( 1, save_mode, 0 );
    //         PlotSaveData psd2 = makePlotSaveData( 2, save_mode, 0 );
    //         doSaveCsx( null, origin, psd1, psd2, true );
    //       }
    //     }
    //   } else {
    //     saveWithExt( null, mType, mExportExt );
    //   }
    // }
  }

  /** 
   * @param uri     export URI
   * @param second  whether to export the second view (only for plan or profile)
   */
  private void doUriExport( Uri uri, boolean second ) 
  {
    // if ( ! TDSetting.mExportUri ) return;
    // TDLog.v( "do URI export. index " + mExportIndex );
    // int mExportIndex = TDConst.plotExportIndex( export_type );
    long type = mType;
    if ( second ) {
      if ( PlotType.isProfile( mType ) ) {
        type = mPlot1.type;
      } else if ( PlotType.isPlan( mType ) ) {
        type = mPlot2.type;
      }
    }
    switch ( mExportIndex ) {
      case TDConst.SURVEY_FORMAT_TH2: doSaveTh2( uri, type, true ); break;
      case TDConst.SURVEY_FORMAT_CSX: 
        if ( ! PlotType.isAnySection( type ) ) { // FIXME x-sections are saved PNG for CSX
          if ( mPlot1 != null ) {
            String origin = mPlot1.start;
	    int save_mode    = PlotSave.EXPORT;
	    PlotSaveData psd1 = makePlotSaveData( 1, save_mode, 0 );
	    PlotSaveData psd2 = makePlotSaveData( 2, save_mode, 0 );
            doSaveCsx( uri, origin, psd1, psd2, true );
	  }
          break;
        } // else fall-through and savePng
      // case TDConst.SURVEY_FORMAT_PNG: savePng( uri, type ); break; // NO_PNG
      // case TDConst.SURVEY_FORMAT_PNM: savePnm( uri, type ); break; // NO_PNM
      case TDConst.SURVEY_FORMAT_DXF: saveWithExt( uri, type, "dxf" ); break;
      case TDConst.SURVEY_FORMAT_SVG: saveWithExt( uri, type, "svg" ); break;
      case TDConst.SURVEY_FORMAT_SHP: saveWithExt( uri, type, "shz" ); break;
      case TDConst.SURVEY_FORMAT_XVI: saveWithExt( uri, type, "xvi" ); break;
      // case TDConst.SURVEY_FORMAT_TNL: saveWithExt( uri, type, "xml" ); break; // NO_TUNNEL
      // case TDConst.SURVEY_FORMAT_C3D: // NO_C3D
      //   // TDLog.v("export c3d");
      //   // if ( ! PlotType.isAnySection( type ) )
      //     saveWithExt( uri, type, "c3d" );
      //   break;
      case TDConst.SURVEY_FORMAT_PDF: savePdf( uri, type ); break; 
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
    // mLastLinePath = null; // absolutely necessary
    float x = mOffset.x;
    float y = mOffset.y;
    float z = mZoom;
    String tdr  = TDPath.getTdrFile( filename );
    // TDLog.Log( TDLog.LOG_IO, "reload file " + filename + " path " + tdr );
    // TDLog.v("PLOT recover " + type + " <" + filename + "> TRD " + tdr );
    if ( type == PlotType.PLOT_PLAN ) {
      if ( mPlot1 != null ) {
        mDrawingSurface.resetManager( DrawingSurface.DRAWING_PLAN, null, false );
        mDrawingSurface.modeloadDataStream( tdr, mFullName1, true /*, null */ ); // no missing symbols, true = linkSections
        // DrawingSurface.addManagerToCache( mFullName1 );
        setPlotType1( COMPUTE_YES, PARAMS_YES );
      } else {
        TDLog.e("null Plot 1");
      }
    } else if ( PlotType.isProfile( type ) ) {
      if ( mPlot2 != null ) {
        mDrawingSurface.resetManager( DrawingSurface.DRAWING_PROFILE, null, PlotType.isExtended(type) );
        mDrawingSurface.modeloadDataStream( tdr, mFullName2, true /*, null */ ); // true = linkSections
        // DrawingSurface.addManagerToCache( mFullName2 );
        // now switch to extended view FIXME-VIEW
        setPlotType2( COMPUTE_YES, PARAMS_YES );
      } else {
        TDLog.e("null Plot 2");
      }
    } else {
      // TDLog.v("doRecover section" );
      if ( mPlot3 != null ) {
        mDrawingSurface.resetManager( DrawingSurface.DRAWING_SECTION, null, false );
        mDrawingSurface.modeloadDataStream( tdr, null, false /*, null */ ); // sections are not cached
        setPlotType3( true ); // true reset display-params
        // FIXME MOVED_BACK_IN DrawingUtil.addGrid( -10, 10, -10, 10, 0.0f, 0.0f, mDrawingSurface );
        List< DBlock > list = getXSectionShots( mType, mFrom, mTo );
        if ( list != null && list.size() > 0 /* mSectionSkip */ ) {
          if ( PlotType.isMultilegSection( mType, mTo ) ) {
            // TDLog.v("PLOT recover multileg list " + list.size() );
            makeMultilegSectionReferences( list, mPlot3.center );
          } else {
            // float tt = mApp_mData.selectPlotIntercept( mSid, mPlot3.id );
            makeSinglelegSectionReferences( list, mPlot3.intercept );
          }
        }
      } else {
        TDLog.e("null Plot 3");
      }
    }
    mOffset.x = x;
    mOffset.y = y;
    mZoom     = z;
    // TDLog.v( "PLOT recover " + mOffset.x + " " + mOffset.y + " " + mZoom );
    mDrawingSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
  }

  /** export sketch as CSX file
   * @param sid     survey ID
   * @param pw      output printer
   * @param survey  survey name
   * @param cave    CSX cave name
   * @param branch  CSX branch name
   * @param psd1    plan view save-data
   * @param psd2    profile view save-data
   */
  static void exportAsCsx( long sid, PrintWriter pw, String survey, String cave, String branch, /* String session, */ PlotSaveData psd1, PlotSaveData psd2 )
  {
    List< PlotInfo > all_sections = TopoDroidApp.mData.selectAllPlotsSection( sid, TDStatus.NORMAL );
    ArrayList< PlotInfo > sections1 = new ArrayList<>(); // plan xsections
    ArrayList< PlotInfo > sections2 = new ArrayList<>(); // profile xsections
    pw.format("  <plan>\n");
    if ( psd1 != null ) {
      // if ( TDSetting.mExportTcsx ) { 
        DrawingSurface.exportAsTCsx( pw, PlotType.PLOT_PLAN, survey, cave, branch, /* session, */ psd1.cm, all_sections, sections1 /* , psd1.util */ );
      // } else {
      //   DrawingSurface.exportAsCsx( pw, PlotType.PLOT_PLAN, survey, cave, branch, /* session, */ psd1.cm, all_sections, sections1 /* , psd1.util */ );
      // }
    }
    pw.format("    <plot />\n");
    pw.format("  </plan>\n");
    
    pw.format("  <profile>\n");
    if ( psd2 != null ) {
      // if ( TDSetting.mExportTcsx ) { 
        DrawingSurface.exportAsTCsx( pw, PlotType.PLOT_EXTENDED, survey, cave, branch, /* session, */ psd2.cm, all_sections, sections2 /* , psd2.util */ ); 
      // } else {
      //   DrawingSurface.exportAsCsx( pw, PlotType.PLOT_EXTENDED, survey, cave, branch, /* session, */ psd2.cm, all_sections, sections2 /* , psd2.util */ ); 
      // }
    }
    pw.format("    <plot />\n");
    pw.format("  </profile>\n");
    // if ( TDSetting.mExportTcsx ) 
      // exportTCsxXSection( pw, section1, survey, cave, branch /* , session */ /* , psd1.util */ );
      // exportTCsxXSection( pw, section2, survey, cave, branch /* , session */ /* , psd2.util */ );

    // if ( ! TDSetting.mExportTcsx ) { 
    //   pw.format("    <crosssections>\n");
    //   if ( psd1 != null ) {
    //     for ( PlotInfo section1 : sections1 ) {
    //       pw.format("    <crosssection id=\"%s\" design=\"0\" crosssection=\"%d\">\n", section1.name, section1.csxIndex );
    //       exportCsxXSection( pw, section1, survey, cave, branch /* , session */ /* , psd1.util */ );
    //       pw.format("    </crosssection>\n" );
    //     }
    //   }
    //   if ( psd2 != null ) {
    //     for ( PlotInfo section2 : sections2 ) {
    //       pw.format("    <crosssection id=\"%s\" design=\"1\" crosssection=\"%d\">\n", section2.name, section2.csxIndex );
    //       exportCsxXSection( pw, section2, survey, cave, branch /* , session */ /* , psd2.util */ );
    //       pw.format("    </crosssection>\n" );
    //     }
    //   }
    //   pw.format("    </crosssections>\n");
    // }
  }

  // private static void exportCsxXSection( PrintWriter pw, PlotInfo section, String survey, String cave, String branch
  //       	  /* , String session */ /* , DrawingUtil drawingUtil */ )
  // {
  //   // String name = section.name; // binding name
  //   // open xsection file
  //   String filename = TDPath.getSurveyPlotTdrFile( survey, section.name );
  //   DrawingIO.doExportCsxXSection( pw, filename, survey, cave, branch, /* session, */ section.name /* , drawingUtil */ ); // bind=section.name
  // }

  // ----------------------------------------------------------------------------------
  // NEW CSURVEY EXPORT
  static void exportAsTCsx( long sid, PrintWriter pw, String survey, String cave, String branch, /* String session, */ PlotSaveData psd1, PlotSaveData psd2 )
  {
    // TDLog.v( "export as CSX <<" + cave + ">>" );
    List< PlotInfo > all_sections = TopoDroidApp.mData.selectAllPlotsSection( sid, TDStatus.NORMAL );
    ArrayList< PlotInfo > sections1 = new ArrayList<>(); // plan xsections
    ArrayList< PlotInfo > sections2 = new ArrayList<>(); // profile xsections

    pw.format("  <plan>\n");
    if ( psd1 != null ) {
      DrawingSurface.exportAsTCsx( pw, PlotType.PLOT_PLAN, survey, cave, branch, /* session, */ psd1.cm, all_sections, sections1 /* , psd1.util */ );
    }
    pw.format("  </plan>\n");
    
    pw.format("  <profile>\n");
    if ( psd2 != null ) {
      DrawingSurface.exportAsTCsx( pw, PlotType.PLOT_EXTENDED, survey, cave, branch, /* session, */ psd2.cm, all_sections, sections2 /* , psd2.util */ ); 
    }
    pw.format("  </profile>\n");

    // pw.format("    <crosssections>\n");
    // if ( psd1 != null ) {
    //   for ( PlotInfo section1 : sections1 ) {
    //     pw.format("    <crosssection id=\"%s\" design=\"0\" crosssection=\"%d\">\n", section1.name, section1.csxIndex );
    //     // exportCsxXSection( pw, section1, survey, cave, branch, /* session, */ mDrawingUtil );
    //     exportTCsxXSection( pw, section1, survey, cave, branch /* , session */ /* , psd1.util */ );
    //     pw.format("    </crosssection>\n" );
    //   }
    // }
    // if ( psd2 != null ) {
    //   for ( PlotInfo section2 : sections2 ) {
    //     pw.format("    <crosssection id=\"%s\" design=\"1\" crosssection=\"%d\">\n", section2.name, section2.csxIndex );
    //     // exportCsxXSection( pw, section2, survey, cave, branch, /* session, */ mDrawingUtil );
    //     exportTCsxXSection( pw, section2, survey, cave, branch /* , session */ /* , psd2.util */ );
    //     pw.format("    </crosssection>\n" );
    //   }
    // }
    // pw.format("    </crosssections>\n");
  }

  // private static void exportTCsxXSection( PrintWriter pw, PlotInfo section, String survey, String cave, String branch /* , String session */ )
  // {
  //   // String name = section.name; // binding name
  //   // open xsection file
  //   // String filename = TDPath.getSurveyPlotTdrFile( survey, section.name );
  //   String filename = survey + "-" + section.name + ".tdr";
  //   DrawingIO.doExportTCsxXSection( pw, filename, survey, cave, branch, /* session, */ section.name /* , drawingUtil */ ); // bind=section.name
  // }

  // ----------------------------------------------------------------------------------

  public void setConnectionStatus( int status )
  { 
    if ( TDInstance.getDeviceA() == null ) {
      mBTstatus = ConnectionState.CONN_DISCONNECTED;
      setButton1( BTN_DOWNLOAD, mBMadd );
      setButton1( BTN_BLUETOOTH, mBMbluetooth_no );
    } else {
      if ( status != mBTstatus ) {
        mBTstatus = status;
        switch ( status ) {
          case ConnectionState.CONN_CONNECTED:
            setButton1( BTN_DOWNLOAD, mBMdownload_on );
            if ( TDInstance.isDeviceBric() ) {
              setButton1( BTN_BLUETOOTH, mBMbluetooth );
            } else {
              setButton1( BTN_BLUETOOTH, mBMbluetooth_no );
            }
            break;
          case ConnectionState.CONN_WAITING:
            setButton1( BTN_DOWNLOAD,  mBMdownload_wait );
            setButton1( BTN_BLUETOOTH, mBMbluetooth_no );
            break;
          default:
            setButton1( BTN_DOWNLOAD,  mBMdownload );
            setButton1( BTN_BLUETOOTH, mBMbluetooth );
        }
      }
    }
  }

  public void enableBluetoothButton( boolean enable )
  {
    if ( TDInstance.isDivingMode() ) return;
    if ( TDInstance.hasBleDevice() ) enable = true;
    setButton1( BTN_BLUETOOTH, enable ? mBMbluetooth : mBMbluetooth_no );
    if ( BTN_BLUETOOTH < mNrButton1 ) mButton1[BTN_BLUETOOTH].setEnabled( enable );
  }

// -------------------------------------------------------------
// AUTOWALLS

  // this is for auto-walls of x-sections - OLD
  // void drawWallsAt( float x0, float y0 )
  // {
  //   if ( TDSetting.mWallsType == TDSetting.WALLS_NONE ) return;
  //   if ( ! PlotType.isLegSection( mType ) ) return;
  //   ArrayList< DLNSite > sites = null;
  //   sites = new ArrayList<>();
  //   List< DrawingPath > splays = mDrawingSurface.getSplays();
  //   // float len2 = 0;
  //   for ( DrawingPath sp : splays ) {
  //     // float dx = sp.x2 - sp.x1;
  //     // float dy = sp.y2 - sp.y1;
  //     // float l2 = dx * dx + dy * dy;
  //     // if ( l2 > len2 ) len2 = l2;
  //     // len = 6.28 * TDMath.sqrt( len2 );
  //     // if ( allSplay ) {
  //       if (sites != null) {
  //         // sites.add( new DLNSite( sp.x1, sp.y1 ) );
  //         sites.add( new DLNSite( sp.x2, sp.y2 ) );
  //       }
  //     // } else {
  //     //   if (pos != null) pos.add( new PointF(sp.x2, sp.y2) );
  //     //   // if (neg != null) neg.add( new PointF(u,v) );
  //     // }
  //     makeDlnWall( sites, x0, y0, x0, y0 /*, len, uu, vv */ );
  //     modified();
  //     return;
  //   }
  // }

  /* AUTOWALLS
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
    if ( mType == PlotType.PLOT_PLAN ) {
      x0 = (float)st1.e;
      y0 = (float)st1.s;
      x1 = (float)st2.e;
      y1 = (float)st2.s;
    } else {
      x0 = (float)st1.h;
      y0 = (float)st1.v;
      x1 = (float)st2.h;
      y1 = (float)st2.v;
    }
    float x2 = x1 - x0;
    float y2 = y1 - y0;
    float x22  = x2 * x2;
    float len2 = x2 * x2 + y2 * y2 + 0.0001f;
    float len  = TDMath.sqrt( len2 );
    PointF uu = new PointF( (x2 / len), (y2 / len) );
    PointF vv = new PointF( -uu.y, uu.x );

    // TDLog.v( "X0 " + x0 + " " + y0 + " X1 " + x1 + " " + y1 );
    // TDLog.v( "U " + uu.x + " " + uu.y + " V " + vv.x + " " + vv.y );

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
    if ( PlotType.isPlan( mType ) ) {
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        boolean ok = false;
        if ( st == st1 ) {
          if ( Math.abs( sp.getBlock().mClino - cl ) < TDSetting.mWallsPlanThr ) {
            xs = (float)sp.e;
            ys = (float)sp.s;
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
            xs = (float)sp.e;
            ys = (float)sp.s;
            if ( allSplay ) { 
              ok = true;
            } else {
              xs -= x0;
              ys -= y0;
              double proj = ( xs*x2 + ys*y2 )/len2;
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
            float u = ( xs * uu.x + ys * uu.y );
            float v = ( xs * vv.x + ys * vv.y );
            if ( v > 0 ) {
              if (pos != null) pos.add( new PointF(u,v) );
            } else {
              if (neg != null) neg.add( new PointF(u,v) );
            }
          }
        } 
      }
    } else if ( PlotType.isProfile( mType ) ) { // PLOT_EXTENDED || PLOT_PROJECTED
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        if ( st == st1 || st == st2 ) {
          boolean ok = false;
          if ( Math.abs( sp.getBlock().mClino ) > TDSetting.mWallsExtendedThr ) { // FIXME
            xs = (float)sp.h;
            ys = (float)sp.v;
            if ( allSplay ) { 
              ok = true;
            } else {
              xs -= x0;
              ys -= y0;
              double proj = ( xs*x2 )/ x22;
              ok = ( proj >= 0 && proj <= 1 );
            }
            if ( ok ) {
              if ( allSplay ) {
                if (sites != null) sites.add( new DLNSite( xs, ys ) );
              } else {
                float u = ( xs * uu.x + ys * uu.y );
                float v = ( xs * vv.x + ys * vv.y );
                // TDLog.v("WALL Splay " + x2 + " " + y2 + " --> " + u + " " + v);
                if ( v > 0 ) { // if ( allSplay || v > 0 ) // allSplay is false
                  if (pos != null) pos.add( new PointF(u,v) );
                } else {
                  if (neg != null) neg.add( new PointF(u,v) );
                }
              }
            }
          }
        }
      }
    } else { // PlotType.isLegSection( mType )
      return;
    }
    // (x0,y0) (x1,y1) are the segment endpoints
    // len is its length
    // uu is the unit vector from 0 to 1
    // vv is the orthogonal unit vector
    if ( TDSetting.mWallsType == TDSetting.WALLS_CONVEX ) {
      makeWall( pos, x0, y0, x1, y1, len, uu, vv );
      makeWall( neg, x0, y0, x1, y1, len, uu, vv );
    } else if ( TDSetting.mWallsType == TDSetting.WALLS_DLN ) {
      makeDlnWall( sites, x0, y0, x1, y1 ); // , len, uu, vv 
    }
    modified();
  }
  */ 

  /* AUTOWALLS
  private void addPointsToWallLine( DrawingLinePath line, float x0, float y0, float xx, float yy )
  {
    double ll = Math.sqrt( (xx-x0)*(xx-x0) + (yy-y0)*(yy-y0) ) / 20;
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
  */

  /* AUTOWALLS
  private void makeDlnWall( ArrayList< DLNSite > sites, float x0, float y0, float x1, float y1 ) // , float len, PointF uu, PointF vv
  {
    DLNWall dln_wall = new DLNWall( new Point2D(x0,y0), new Point2D(x1,y1) );
    dln_wall.compute( sites );
    if ( dln_wall.mPosHull.size() > 0 ) {
      DLNSideList hpos = dln_wall.mPosHull.get(0);
      DLNSide side = hpos.side;
      float xx = DrawingUtil.toSceneX( side.mP1.x, side.mP1.y );
      float yy = DrawingUtil.toSceneY( side.mP1.x, side.mP1.y );
      int idx = BrushManager.getLineWallIndex();
      DrawingLinePath path = new DrawingLinePath( idx, mDrawingSurface.scrapIndex() );
      path.setOptions( BrushManager.getLineDefaultOptions( idx ) );

      path.addStartPoint( xx, yy );
      for ( DLNSideList hp : dln_wall.mPosHull ) {
        side = hp.side;
        float xx2 = DrawingUtil.toSceneX( side.mP2.x, side.mP2.y );
        float yy2 = DrawingUtil.toSceneY( side.mP2.x, side.mP2.y );
        addPointsToWallLine( path, xx, yy, xx2, yy2 );
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
      int idx = BrushManager.getLineWallIndex();
      DrawingLinePath path = new DrawingLinePath( idx, mDrawingSurface.scrapIndex() );
      path.setOptions( BrushManager.getLineDefaultOptions( idx ) );

      path.addStartPoint( xx, yy );
      for ( DLNSideList hn : dln_wall.mNegHull ) {
        side = hn.side;
        float xx2 = DrawingUtil.toSceneX( side.mP2.x, side.mP2.y );
        float yy2 = DrawingUtil.toSceneY( side.mP2.x, side.mP2.y );
        addPointsToWallLine( path, xx, yy, xx2, yy2 );
        xx = xx2;
        yy = yy2;
      } 
      // FIXME_LANDSCAPE if ( mLandscape ) path.landscapeToPortrait();
      path.computeUnitNormal();
      mDrawingSurface.addDrawingPath( path );
    }
  }
  */

  /* AUTOWALLS - OLD
  void makeDlnWall( ArrayList< DLNSite > sites, double x0, double y0, double x1, double y1, double len, PointF uu, PointF vv )
  {
    DLNWall dln_wall = new DLNWall( new Point2D(x0,y0), new Point2D(x1,y1) );
    dln_wall.compute( sites );
    DLNSideList hull = dln_wall.getBorderHead();
    DLNSide side = hull.side;
    float xx = DrawingUtil.toSceneX( side.mP1.x, side.mP1.y );
    float yy = DrawingUtil.toSceneY( side.mP1.x, side.mP1.y );
    DrawingLinePath path = new DrawingLinePath( BrushManager.getLineWallIndex(), mDrawingSurface.scrapIndex() );
    path.setOptions( BrushManager.getLineDefaultOptions( idx ) );
    path.addStartPoint( xx, yy );
    int size = dln_wall.hullSize();
    for ( int k=0; k<size; ++k ) {
      float xx2 = DrawingUtil.toSceneX( side.mP2.x, side.mP2.y );
      float yy2 = DrawingUtil.toSceneY( side.mP2.x, side.mP2.y );
      addPointsToWallLine( path, xx, yy, xx2, yy2 );
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

  /* AUTOWALLS
  private void makeWall( ArrayList< PointF > pts, float x0, float y0, float x1, float y1, float len, PointF uu, PointF vv )
  {
    if ( pts == null ) return; // safety check
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
        int idx = BrushManager.getLineWallIndex();
        DrawingLinePath path = new DrawingLinePath( idx, mDrawingSurface.scrapIndex() );
        path.setOptions( BrushManager.getLineDefaultOptions( idx ) );

        path.addStartPoint( x0, y0 );
        addPointsToWallLine( path, x0, y0, xx, yy );
        addPointsToWallLine( path, xx, yy, x1, y1 );
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
      int idx = BrushManager.getLineWallIndex();
      DrawingLinePath path = new DrawingLinePath( idx, mDrawingSurface.scrapIndex() );
      path.setOptions( BrushManager.getLineDefaultOptions( idx ) );

      path.addStartPoint( xx, yy );
      for ( int k=1; k<pts.size(); ++k ) {
        p1 = pts.get(k);
	x2 = x0 + uu.x * p1.x + vv.x * p1.y;
	y2 = y0 + uu.y * p1.x + vv.y * p1.y;
        float xx2 = DrawingUtil.toSceneX( x2, y2 );
        float yy2 = DrawingUtil.toSceneY( x2, y2 );
        addPointsToWallLine( path, xx, yy, xx2, yy2 );
        xx = xx2;
        yy = yy2;
      }
      if ( mLandscape ) path.landscapeToPortrait();
      path.computeUnitNormal();
      mDrawingSurface.addDrawingPath( path );
    }
  }
  */

  /* AUTOWALLS
  // sort the points on the list by increasing X
  // @param pts list of points
  private void sortPointsOnX( ArrayList< PointF > pts ) 
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
      double x0 = pts.get(0).x;
      double y0 = Math.abs( pts.get(0).y );
      for ( int k = 0; k < pts.size()-1; ++k ) {
        int hh = k+1;
        double x1 = pts.get(hh).x;
        double y1 = Math.abs( pts.get(hh).y );
        double s0 = (y1-y0)/(x1-x0); // N.B. x1 >= x0 + 0.1
          for ( int h=hh+1; h<pts.size(); ++h ) {
          x1 = pts.get(h).x;
          y1 = Math.abs( pts.get(h).y );
          double s1 = (y1-y0)/(x1-x0); 
          if ( s1 > s0 + TDSetting.mWallsConcave ) { // allow small concavities
            hh = h;
          }
        }
        // for ( int h=hh-1; h>k; --h ) pts.remove(h);
        pts.subList( k+1, hh ).clear();
      }
    }
  }
  */

  // --------------------------------------------------------------------------

  /** react to the result of a child activity
   * @param reqCode    request code
   * @param resCode    result code
   * @param intent     returned intent (and data)
   */
  @Override
  protected void onActivityResult( int reqCode, int resCode, Intent intent )
  {
    switch ( reqCode ) {
      // case TDRequest.QCAM_COMPASS_DRAWWINDOW: // not used
      //   if ( resCode == Activity.RESULT_OK ) {
      //     try {
      //       Bundle extras = intent.getExtras();
      //       float b = Float.parseFloat( extras.getString( TDTag.TOPODROID_BEARING ) );
      //       float c = Float.parseFloat( extras.getString( TDTag.TOPODROID_CLINO ) );
      //       mShotNewDialog.setBearingAndClino( b, c, 0, 3, 0 ); // orientation 0
      //     } catch ( NumberFormatException e ) { }
      //   }
      //   mShotNewDialog = null;
      //   break;

      // case TDRequest.CAPTURE_IMAGE_DRAWWINDOW:
      //   if ( resCode == Activity.RESULT_OK ) {
      //     Bundle extras = intent.getExtras();
      //     Bitmap bitmap = (Bitmap) extras.get("data");
      //     if ( mMediaManager.savePhotoFile( bitmap, 90 ) ) { // compression = 90
      //       // // FIXME TITLE has to go
      //       // mApp_mData.insertPhotoRecord( TDInstance.sid, mMediaId, -1, "", TDUtil.currentDate(), mMediaComment, mMediaCamera );
      //       // // FIXME NOTIFY ? no
      //       createPhotoPoint();
      //     } else {
      //       // TDLog.e("failed to save photo");
      //       TDLog.e("failed to save photo");
      //     }
      //   }
      //   break;

      case TDRequest.PLOT_RELOAD:
        if ( resCode == Activity.RESULT_OK ) {
          Bundle extras = (intent != null)? intent.getExtras() : null;
          if ( extras == null ) return;
          long type = extras.getLong( TDTag.TOPODROID_PLOT_TYPE );
          String filename = extras.getString( TDTag.TOPODROID_PLOT_FILENAME );
          // TDLog.v("RELOAD result " + filename );
          doRecover( filename, type );
        }
        break;
      // APP_OUT_DIR
      // case TDRequest.REQUEST_GET_EXPORT:
      //   if ( /* TDSetting.mExportUri && */ resCode == Activity.RESULT_OK ) {
      //     // int index = intent.getIntExtra( "exporttype", -1 );
      //     Uri uri = intent.getData();
      //     // TDLog.v( "URI Export " + mExportIndex + " uri " + uri.toString() );
      //     if ( uri != null ) doUriExport( uri, false );
      //   }
      //   break;
      case TDRequest.REQUEST_GET_IMPORT: // TH2EDIT handle a th2 import
        // TDLog.v("DRAW Import");
        if ( resCode == Activity.RESULT_OK ) {
          if ( ! TDLevel.overExpert ) {
            finish();
          }
          Uri uri = intent.getData();   // import uri
          String filename = uri.getLastPathSegment();
          // TDLog.v( "DRAW URI to import: " + uri.toString() + " filename <" + filename + ">" );
          // int ros = filename.indexOf(":"); // drop the "content" header
          // if ( ros >= 0 ) filename = filename.substring( ros+1 ); 
          int qos_1 = filename.lastIndexOf("/") + 1;
          mFullName3 = ( qos_1 > 0 )? filename.substring( qos_1 ) : filename;
          int pos   = mFullName3.lastIndexOf("."); 
          String ext = (pos >= 0 )? mFullName3.substring( pos ).toLowerCase( Locale.getDefault() ) : ""; // extension with leading '.'
          mName3 = (pos > 0 )? mFullName3.substring( 0, pos ) : mFullName3;
          // TDLog.v( "DRAW import fullname " + mFullName3 );
          String type = TDPath.checkDrawEditType( ext );
          if ( type != null ) {
            // TDLog.v( "DRAW import reader type <" + type + "> filename " + mFullName3 );
            if ( type.equals(".th2") || type.equals(".tdr") ) { // N.B. type is extension with '.'
              mDrawingSurface.clearDrawing();
              if ( ! importReader( uri, type ) ) {
                TDLog.e("DRAW import failed " + mFullName3 );
                // finish();
              }
            } else {
              TDLog.e("DRAW import unsupported " + ext);
              TDToast.makeBad( String.format( getResources().getString( R.string.unsupported_extension ), ext ) );
              // finish();
            }
          // } else if ( (type = TDPath.checkImportTypeStream( ext ) ) != null ) {
          //   // TDLog.v( "import stream type " + type + " name " + name );
          //   // importStream( uri, name, type );
          } else {
            TDToast.makeBad( String.format( getResources().getString( R.string.unsupported_extension ), ext ) );
            // finish();
          }
        } else {
          TDLog.e("DRAW import canceled");
          // finish();
        }
        break;
      case TDRequest.REQUEST_GET_EXPORT: // TH2EDIT handle a th2 export
        // TDLog.v("DRAW export fullname " + mFullName3 );
        if ( resCode == Activity.RESULT_OK ) {
          if ( TDLevel.overExpert ) {
            finish();
          }
          Uri uri = intent.getData();   // import uri
          String filename = uri.getLastPathSegment();
          // TDLog.v( "DRAW URI to export: " + uri.toString() + " filename <" + filename + ">" );
          // int ros = filename.indexOf(":"); // drop the "content" header
          // if ( ros >= 0 ) filename = filename.substring( ros+1 ); 
          int pos   = filename.lastIndexOf("."); 
          String ext  = (pos >= 0 )? filename.substring( pos ).toLowerCase( Locale.getDefault() ) : ""; // extension with leading '.'
          // int qos_1 = filename.lastIndexOf("/") + 1;
          // String name = (pos > qos_1 )? filename.substring( qos_1, pos ) : filename.substring( qos_1 );
          // TDLog.v( "DRAW export ext " + ext );
          String type = TDPath.checkDrawEditType( ext );
          if ( type != null ) {
            // TDLog.v( "DRAW export reader type " + type + " filename " + filename );
            if ( type.equals(".th2") ) { // N.B. type is extension with '.'
              // String pathname = (new File( uri )).getAbsolutePath();
              exportWriter( uri );
            } else {
              // TDLog.e("DRAW export unsupported extension " + ext);
              TDToast.makeBad( String.format( getResources().getString( R.string.unsupported_extension ), ext ) );
              // finish();
            }
          // } else if ( (type = TDPath.checkImportTypeStream( ext ) ) != null ) {
          //   // TDLog.v( "import stream type " + type + " name " + name );
          //   // importStream( uri, name, type );
          } else {
            TDToast.makeBad( String.format( getResources().getString( R.string.unsupported_extension ), ext ) );
            // finish();
          }
        } else {
          TDLog.e("DRAW export canceled");
          // finish();
        }
        break;
    }
  }

  // TH2EDIT
  /** read a th2 file
   * @param uri   sketch file URI
   * @param type  file type, either ".th2" or ".tdr"
   * @return true if success
   */
  private boolean importReader( Uri uri, String type )
  {
    // mTh2Edit = true;
    // TDLog.v("DRAW import reader load th2 file " + mName3 + " type " + mType );
    ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( uri );
    if ( pfd != null ) {
      if ( type.equals(".th2") ) {
        FileReader fr = TDsafUri.docFileReader( pfd );
        return DrawingTh.doLoadTherion( mDrawingSurface, fr, 0, 0 );
      } else if ( type.equals(".tdr") ) {
        FileInputStream fis = TDsafUri.docFileInputStream( pfd );
        mDrawingSurface.modeloadFileStream( fis, "TODO" );
      }
    } else {
      TDLog.e("DRAW null fd");
    }
    return false;
  }

  // TH2EDIT
  /** save a th2 file
   * @param uri   th2 file URI
   * @return true if success
   */
  private boolean exportWriter( Uri uri )
  {
    // mTh2Edit = true;
    // TDLog.v("DRAW export writer th2 file - mType " + mType );
    ParcelFileDescriptor pfd = TDsafUri.docReadFileDescriptor( uri );
    if ( pfd != null ) {
      FileWriter fw = TDsafUri.docFileWriter( pfd );
      // DrawingTh.doLoadTherion( mDrawingSurface, fw, 0, 0 );
      doSaveTh2( uri, mType, true ); // true = toast
      return true;
    } else {
      TDLog.e("DRAW null fd");
    }
    return false;
  }

  // TH2EDIT
  /** get the import stream from the data provider
   * @param index    file format index (@see TDConst.surveyImportFormatIndex)
   * @param request  request code
   * @param mode     intent document mode
   * this method saves the import parameters and starts a choice of a file (of the given type)
   */
  private void selectFromProvider( int index, int request, String mode ) // IMPORT
  {
    if ( index < 0 || index >= TDConst.getMimeTypeLength() ) {
      TDLog.e("Bad import index " + index );
      TDToast.makeBad( String.format( getResources().getString( R.string.index_oob ), index ) );
      return;
    } 
    // TDLog.v( "DRAW selectFromProvider runs on " + TDLog.threadId() );
    Intent intent = new Intent( mode );
    intent.setType( TDConst.getMimeType( index ) );
    // TDLog.v( "Import from provider. index " + index + " mime " + TDConst.getMimeType( index ) );
    intent.addCategory(Intent.CATEGORY_OPENABLE);
    if ( request == TDRequest.REQUEST_GET_EXPORT ) {
      intent.putExtra( Intent.EXTRA_TITLE, mFullName3 );
    }
    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION); // API_19
    // intent.putExtra( "importtype", index ); // extra is not returned to the app
    startActivityForResult( Intent.createChooser(intent, getResources().getString( R.string.title_import_th2 ) ), request );
  }

  // ------------------------------------------------------------------
  // SCRAPS, X-SECTIONS, OUTLINES 

  // /** @return the index of the active scrap of the current plot
  //  */
  // int getScrapIndex() { return mDrawingSurface.scrapIndex(); }

  // /** @return the maximum index of the scraps of the current plot
  //  */
  // int getScrapMaxIndex() { return mDrawingSurface.scrapMaxIndex(); }

  /** @return the number of the scraps of the current plot
   */
  int getScrapNumber() { return mDrawingSurface.scrapNumber(); }

  /** @return the number (index in the list) of the current scrap
   */
  int getCurrentScrapNumber() { return mDrawingSurface.currentScrapNumber(); }

  /** switch to the next scrap of the current ploy
   */
  void scrapNext() { mDrawingSurface.toggleScrapIndex( mTh2Edit, 1 ); } // TH2EDIT no first param

  /** switch to the previous scrap of the current ploy
   */
  void scrapPrev() { mDrawingSurface.toggleScrapIndex( mTh2Edit, -1 ); } // TH2EDIT no first param

  /** make a new scrap in the current plot 
   */
  void scrapNew() 
  { 
    int scrap_idx = mDrawingSurface.newScrapIndex( false ); // TH2EDIT no false param
    // TDLog.v("new scrap index " + scrap_idx );
    mApp_mData.updatePlotMaxScrap( mSid, mPid, scrap_idx );
  }

  /** ask whether to delete the current scrap
   */
  void scrapDelete() { askDeleteScrap(); }

  /** delete the current scrap
   */
  void doScrapDelete() { mDrawingSurface.deleteCurrentScrap( mTh2Edit ); }

  /** prepare for the dialog about the scrap outline:
   * select the plots with same type as the current plot and open the dialog
   * @note apparently there is not difference whether the type is PLAN or PROFILE
   */
  void scrapOutlineDialog()
  {
    if ( mType != PlotType.PLOT_PLAN && mType != PlotType.PLOT_EXTENDED ) {
      TDLog.e( "outline bad scrap type " + mType );
      return;
    }
    String name = ( mType == PlotType.PLOT_PLAN )? mPlot1.name : mPlot2.name;
    List< PlotInfo > plots = mApp_mData.selectAllPlotsWithType( TDInstance.sid, TDStatus.NORMAL, mType );
    for ( PlotInfo plot : plots ) {
      if ( plot.name.equals( name ) ) {
        plots.remove( plot );
        break;
      }
    }
    if ( plots.size() == 0 ) {
      TDLog.e( "outline no other scraps" );
      return;
    }
    if ( mType == PlotType.PLOT_PLAN ) {
      new ScrapOutlineDialog( this, this, mApp, plots ).show();
    } else { // ( mType == PlotType.PLOT_EXTENDED ) 
      new ScrapOutlineDialog( this, this, mApp, plots ).show();
    }
  }

  /** add a scrap to the plot
   * @param plot  (current) plot info
   */
  void addPlot( PlotInfo plot )
  {
    // assert( mLastLinePath == null );

    mDrawingSurface.clearPlotOutline();
    if ( mNum == null || plot == null ) {
      // TDLog.v("null num or plot");
      return;
    }
    NumStation st  = mNum.getStation( plot.start );
    if ( st == null ) {
      // TDLog.v("null plot start station");
      return;
    }
    float xdelta = 0;
    float ydelta = 0;
    NumStation st0;
    if ( mType == PlotType.PLOT_PLAN ) {
      mOutlinePlot1 = plot;
      st0 = mNum.getStation( mPlot1.start );
      xdelta = (float)(st.e - st0.e); // FIXME SCALE FACTORS ???
      ydelta = (float)(st.s - st0.s);
    } else if ( mType == PlotType.PLOT_EXTENDED ) {
      mOutlinePlot2 = plot;
      st0 = mNum.getStation( mPlot2.start );
      xdelta = (float)(st.h - st0.h);
      ydelta = (float)(st.v - st0.v);
    } else {
      return;
    }
    xdelta *= DrawingUtil.SCALE_FIX;
    ydelta *= DrawingUtil.SCALE_FIX;

    String fullName = TDInstance.survey + "-" + plot.name;
    String tdr = TDPath.getTdrFileWithExt( fullName );
    // TDLog.v("add outline " + tdr + " delta " + xdelta + " " + ydelta );
    mDrawingSurface.addScrapDataStream( tdr, xdelta, ydelta );
  }

  /** @return true if the plot has the xsection outline
   * @param name xsection scrap_name = survey_name + "-" + xsection_id
   *                      tdr_path = tdr_dir + scrap_name + ".tdr"
   */
  boolean hasXSectionOutline( String name ) { return mDrawingSurface.hasXSectionOutline( name ); }

  // TODO move this to DrawingSurface ?
  /** add/drop the outline of a xsection
   * @param name     xsection name
   * @param scrap_id id of the scrap of the section point
   * @param on_off   whether to add or to drop
   * @param x        X coordinate of the point where to put the xsection (canvas frame)
   * @param y        Y coordinate
   */
  void setXSectionOutline( String name, int scrap_id, boolean on_off, float x, float y )
  { 
    // assert( mLastLinePath == null );

    mDrawingSurface.clearXSectionOutline( name );
    // TDLog.v("XSECTION set " + name + " on/off " + on_off + " " + x + " " + y );
    if ( on_off ) {
      String tdr = TDPath.getTdrFileWithExt( name );
      // TDLog.v("XSECTION set " + name + " on_off " + on_off + " tdr-file " + tdr );
      mDrawingSurface.setXSectionOutline( name, scrap_id, tdr, x-DrawingUtil.CENTER_X, y-DrawingUtil.CENTER_Y );
    }
  }

  // ------------------------------------------------------------------
  // SPLIT AND MERGE
  // here we are guaranteed that "name" can be used for a new plot name
  // and the survey has station "station"

  /** split the current plot moving/copying items to a new plot
   * @param name    name of the new plot
   * @param station origin station of the new plot
   * @param remove  whether to move or copy the items: if remove is true the items are moved to the new plot
   */
  void splitPlot( String name, String station, boolean remove ) 
  {
    // mLastLinePath = null; // absolutely necessary

    // TDLog.v("split plot " + name + " station " + station );
    // get the DrawingStation of station
    mSplitName = name;
    // mSplitStation = mDrawingSurface.getStation( station );
    mSplitStationName = station;
    mSplitRemove  = remove;
    // mSplitCreate  = true;
    // if ( mSplitStation != null ) { // do not check origin station
      if ( mSplitBorder == null ) {
        mSplitBorder = new ArrayList<>();
      } else {
        mSplitBorder.clear();
      }
      mMode = MODE_SPLIT_SKETCH;
      mTouchMode = MODE_MOVE;
      TDLog.v("*** split SKETCH mode");
    // } else {
    //   TDToast.makeBad("Missing station " + station );
    // }
    // TDLog.v("mode " + mMode + " touch-mode " + mTouchMode );
  }

  /** 
   * @param remove  whether to remove items from the source scrap
   * @param create  whether to create a new scrap and put the items in there
   */
  void splitScrap( boolean remove, boolean create )
  {
    mSplitName = null;
    mSplitStationName = null;
    mSplitRemove = remove;
    mSplitCreate = create;
    // mSplitScrap  = mDrawingSurface.getCurrentScrap();
    if ( mSplitBorder == null ) {
      mSplitBorder = new ArrayList<>();
    } else {
      mSplitBorder.clear();
    }
    mMode = MODE_SPLIT_SCRAP;
    mTouchMode = MODE_MOVE;
    TDLog.v("*** split SCRAP mode");
  }

  /** marge the plot of the outline in the current plot
   */
  void mergePlotOutline()
  {
    // merge is called in MOVE mode
    // assert( mLastLinePath == null );
    if ( mType == PlotType.PLOT_PLAN ) {
      if ( mOutlinePlot1 == null ) return;
      mDrawingSurface.clearPlotOutline();
      doMergePlot( mOutlinePlot1 );
      mOutlinePlot1 = null;
    } else if ( mType == PlotType.PLOT_EXTENDED ) {
      if ( mOutlinePlot2 == null ) return;
      mDrawingSurface.clearPlotOutline();
      doMergePlot( mOutlinePlot2 );
      mOutlinePlot2 = null;
    }
  }

  /** clear the drawing item buffer
   */
  void clearSplitBuffer()
  {
    mSplitPaths = null;
  }

  /** copy the drawing item buffer to the current scrap
   * @param clear   whether the clear the buffer after copying it
   */
  void pasteSplitBufferToScrap( boolean clear )
  {
    if ( mSplitPaths != null && mSplitPaths.size() > 0 ) {
      TDLog.v("merge split paths " + mSplitPaths.size() + " removed " + mSplitRemove );
      boolean copy = true;
      int scrap_nr = mDrawingSurface.currentScrapNumber();
      if ( ! mSplitRemove ) { // avoid double items
        DrawingPath path = mSplitPaths.get(0);
        if ( path.mScrap == scrap_nr ) copy = false;
      }
      if ( copy ) {
        for ( DrawingPath path : mSplitPaths ) {
          // avoid double copy of paths
          path.mScrap = scrap_nr;
          mDrawingSurface.addDrawingPath( path ); // INSERT PATH
        }
        if ( clear ) mSplitPaths = null;
      }
    } 
  }

  /** merge the items of another plot into the current plot
   * @param plt   the other plot info
   * @note called by mergeOutlineScrap
   *       called only with mType PLOT_PLAN or PLOT_EXTENDED
   */
  private void doMergePlot( PlotInfo plt )
  {
    // assert( mLastLinePath == null); // obvious
    if ( plt.type != mType ) return;
    NumStation st1 = mNum.getStation( plt.start );
    NumStation st0 = mNum.getOrigin(); // start-station has always coords (0,0)
    if ( st1 == null || st0 == null ) return;

    float xdelta = 0.0f;
    float ydelta = 0.0f;
    if ( mType == PlotType.PLOT_PLAN ) {
      xdelta = (float)( st1.e - st0.e ); // FIXME SCALE FACTORS ???
      ydelta = (float)( st1.s - st0.s );
    } else if ( mType == PlotType.PLOT_EXTENDED ) {
      xdelta = (float)( st1.h - st0.h );
      ydelta = (float)( st1.v - st0.v );
    } else {
      return;
    }
    xdelta *= DrawingUtil.SCALE_FIX;
    ydelta *= DrawingUtil.SCALE_FIX;
    String fullName = TDInstance.survey + "-" + plt.name;
    String tdr = TDPath.getTdrFileWithExt( fullName );
    boolean ret = mDrawingSurface.addLoadDataStream( tdr, xdelta, ydelta, /* null, */ null ); // do not save plot name in paths
  }

  /** split the current plot: selected items are moved or copied to a new plot
   * @note mSplitBorder: border path of the items to move/copy to the new plot
   * @note mSplitRemove: whether to remove the paths from the current plot
   */
  private void doSplitPlot( )
  {
    // mSplitCreate = false;
    if ( mSplitBorder.size() <= 3 ) { // too few points: nothing to split
      TDToast.makeWarn( R.string.split_nothing );
      return;
    }
    List< DrawingPath > paths = mDrawingSurface.splitPaths( mSplitBorder, mSplitRemove );
    if ( TDUtil.isEmpty(paths) ) { // nothing to split
      TDToast.makeWarn( R.string.split_nothing );
      return;
    }
    // mLastLinePath = null;
    boolean extended = (mPlot2.type == PlotType.PLOT_EXTENDED);
    int azimuth = (int)mPlot2.azimuth; 
    int oblique = (int)mPlot2.clino;
    long pid = mApp.insert2dPlot( TDInstance.sid, mSplitName, mSplitStationName, extended, azimuth, oblique );
    String name = mSplitName + ( ( mType == PlotType.PLOT_PLAN )? "p" : "s" );
    String fullname = TDInstance.survey + "-" + name;
    // TDLog.v("Split Plot " + paths.size() + " paths to <" + name + ">" );
    PlotInfo info = mApp_mData.getPlotInfo( TDInstance.sid, name );
    (new SavePlotFileTask( mActivity, null, this, null, mNum, paths, info, fullname, mType, azimuth, oblique ) ).execute();
    // TODO
    // [1] create the database record
    // [2] save the Tdr for the new plot and remove the items from the commandManager
  }

  /** split the current scrap: selected items are moved or copied to a new scrap
   * @note mSplitBorder: border path of the items to move/copy to the new scrap
   * @note mSplitRemove: whether to remove the paths from the current scrap
   */
  private void doSplitScrap( )
  {
    if ( mSplitBorder.size() <= 3 ) { // too few points: nothing to split
      TDToast.makeWarn( R.string.split_nothing );
      return;
    }
    List< DrawingPath > paths = mDrawingSurface.splitPaths( mSplitBorder, mSplitRemove );
    if ( TDUtil.isEmpty( paths ) ) { // nothing to split
      TDToast.makeWarn( R.string.split_nothing );
      return;
    } 
    if ( mSplitPaths == null ) {
      mSplitPaths = paths;
    } else {
      for ( DrawingPath path : paths ) {
        if ( ! mSplitPaths.contains( path ) ) mSplitPaths.add( path );
      }
    }
    TDLog.v("DO SPLIT SCRAP: border " + mSplitBorder.size() + " paths " + mSplitPaths.size() + " create " + mSplitCreate );
    if ( mSplitCreate ) { // make a new scrap and put items there
      scrapNew();
      pasteSplitBufferToScrap( true ); // true = clear buffer
      mSplitCreate = false;
    }
  }

  // -------------------------------------------------------
  // TOOLSET 

  /** rotate the toolset in the bar of the recent tools
   */
  void rotateRecentToolset( )
  { 
    // TDLog.v("rotate recent toolset");
    // mDrawingState.debug("rotate");
    if ( mRecentTools == mRecentPoint ) {
      mRecentTools = mRecentLine;
      mRecentDimX  = 
      mRecentDimY  = Float.parseFloat( getResources().getString( R.string.dimxp ) );
    } else if ( mRecentTools == mRecentLine ) {
      mRecentTools = mRecentArea;
      mRecentDimX  = Float.parseFloat( getResources().getString( R.string.dimxl ) );
      mRecentDimY  = Float.parseFloat( getResources().getString( R.string.dimyl ) );
    } else if ( mRecentTools == mRecentArea ) {
      mRecentTools = mRecentPoint;
      // mRecentDimX  = Float.parseFloat( getResources().getString( R.string.dimxl ) );
      // mRecentDimY  = Float.parseFloat( getResources().getString( R.string.dimyl ) );
    }
    setToolsToolbars();
  }

  /** switch to the current toolbar
   * @note called by rotateRecentToolset() setBtnRecent() setBtnRecentAll()
   */
  private void setToolsToolbars()
  {
    // TDLog.v("set tools toolbars - visible P " + mCurrentPoint + " L " + mCurrentLine + " A " + mCurrentArea );
    int k = -1;
    // ZOOM_TRANSLATION = ZOOM_TRANSLATION_1;
    mZoomView.setTranslationY( ZOOM_TRANSLATION );
    if ( mRecentTools == mRecentPoint ) {
      mLayoutToolsP.setVisibility( View.VISIBLE );
      mLayoutToolsL.setVisibility( View.GONE );
      mLayoutToolsA.setVisibility( View.GONE );
      mLayoutScale.setVisibility( View.GONE );
      k = getCurrentPointIndex();
      // TDLog.v("Set tools toolbars: Current point index " + k );
      pointSelected( mCurrentPoint, false );
      setHighlight( SymbolType.POINT, k );
      setButton2( BTN_TOOL, mBMtoolsPoint );
    } else if ( mRecentTools == mRecentLine ) {
      mLayoutToolsP.setVisibility( View.GONE );
      mLayoutToolsL.setVisibility( View.VISIBLE );
      mLayoutToolsA.setVisibility( View.GONE );
      mLayoutScale.setVisibility( View.GONE );
      k = getCurrentLineIndex();
      // TDLog.v("Set tools toolbars: Current line index " + k );
      lineSelected( mCurrentLine, false );
      setHighlight( SymbolType.LINE, k );
      setButton2( BTN_TOOL, mDrawingState.isRetraceLine() ? mBMtoolsLineCont : mBMtoolsLine );
    } else {
      mLayoutToolsP.setVisibility( View.GONE );
      mLayoutToolsL.setVisibility( View.GONE );
      mLayoutToolsA.setVisibility( View.VISIBLE );
      mLayoutScale.setVisibility( View.GONE );
      k = getCurrentAreaIndex();
      // TDLog.v("Set tools toolbars: Current area index " + k );
      areaSelected( mCurrentArea, false );
      setHighlight( SymbolType.AREA, k );
      setButton2( BTN_TOOL, mDrawingState.isRetraceArea() ? mBMtoolsAreaCont : mBMtoolsArea );
    }
    mLayoutTools.invalidate();
  }

  /**
   * @param path  selected (point) path, or null
   */
  private void setScaleToolbar( DrawingPointPath path )
  {
    if ( path != null ) {
      int progress = 20 + 35 * ( 2 + path.getScale() );
      mScaleBar.setProgress( progress );
      // TDLog.v("set scale bar - progress " + progress + " scale " + path.getScale() + " visible " );
      mLayoutTools.setVisibility( View.VISIBLE );
      mLayoutToolsP.setVisibility( View.GONE );
      mLayoutToolsL.setVisibility( View.GONE );
      mLayoutToolsA.setVisibility( View.GONE );
      mLayoutScale.setVisibility( View.VISIBLE );
    } else {
      // TDLog.v("set scale bar - invisible " );
      mLayoutTools.setVisibility( View.INVISIBLE );
    }
  }

  /** set the hot-item point scale
   * @param progress  scalebar progress value
   * @return true if the progress need to be reset
   */
  boolean setPointScaleProgress( int progress )
  {
    if ( mHotPath == null ) return false;
    if ( mHotPath instanceof DrawingPicturePath ) {
      DrawingPicturePath picture = (DrawingPicturePath)mHotPath;
      float scale = 1 + 0.001f * (progress - 100);
      picture.scalePhotoSize( scale );
      return true;
    } else if ( mHotPath instanceof DrawingPointPath ) {
      int scale = (int)((progress-1)/ 40) - 2;
      ((DrawingPointPath)mHotPath).setScale( scale );
      // TDLog.v("set point scale " + progress + " scale " + scale );
    }
    return false;
  }
    
  void setPointScale( int scale )
  {
    if ( scale >= PointScale.SCALE_XS && scale <= PointScale.SCALE_XL ) {
      mPointScale = scale;
    }
  }

  // --------------------- from ItemDrawer
  /** set the recent symbol buttons of a symbol class
   * @param symbol   symbol claas
   */
  @Override
  public void setBtnRecent( int symbol ) // ItemButton[] mBtnRecent, Symbol[] mRecentTools, float sx, float sy )
  {
    // TDLog.v("set btn recent " + symbol );
    int index = -1;
    switch ( symbol ) {
      case SymbolType.POINT: 
        mRecentTools = mRecentPoint;
        mSymbol = symbol;
        setButtonRecent( mBtnRecentP, mRecentPoint );
        index = getCurrentPointIndex();
        break;
      case SymbolType.LINE: 
        mRecentTools = mRecentLine;
        mSymbol = symbol;
        setButtonRecent( mBtnRecentL, mRecentLine  );
        index = getCurrentLineIndex();
        // TDLog.v("set btn recent line: current " + mCurrentLine + " index " + index );
        break;
      case SymbolType.AREA: 
        mRecentTools = mRecentArea;
        mSymbol = symbol;
        setButtonRecent( mBtnRecentA, mRecentArea  );
        index = getCurrentAreaIndex();
        break;
    }
    setToolsToolbars();
    // setHighlight( symbol, index ); // already done in setToolsToolbars
  }

  /** get the index of the current point tool
   * @return index of the current point tool
   */
  private int getCurrentPointIndex()
  {
    for ( int k=0; k < NR_RECENT; ++k ) {
      if ( mCurrentPoint == BrushManager.getPointIndex( mRecentPoint[k] ) ) return k;
    }
    return -1;
  }

  /** get the index of the current line tool
   * @return index of the current line tool in the recent array
   */
  private int getCurrentLineIndex()
  {
    // TDLog.v("get current line index: current line " + mCurrentLine );
    for ( int k=0; k < NR_RECENT; ++k ) {
      if ( mCurrentLine == BrushManager.getLineIndex( mRecentLine[k] ) ) return k;
    }
    return -1;
  }

  /** get the index of the current area tool
   * @return index of the current area tool
   */
  private int getCurrentAreaIndex()
  {
    for ( int k=0; k < NR_RECENT; ++k ) {
      if ( mCurrentArea == BrushManager.getAreaIndex( mRecentArea[k] ) ) return k;
    }
    return -1;
  }


  /** set the recent buttons of all the symbol classes
   */
  private void setBtnRecentAll()
  {
    // TDLog.v("set btn recent all" );
    setButtonRecent( mBtnRecentP, mRecentPoint );
    setButtonRecent( mBtnRecentL, mRecentLine  );
    setButtonRecent( mBtnRecentA, mRecentArea  );

    mRecentTools = mRecentLine; // by default the drawing tool is the wall-line
    if ( mCurrentLine < 0 ) mCurrentLine = ( BrushManager.isLineEnabled( SymbolLibrary.WALL ) )?  1 : 0;
    setToolsToolbars();
  }

  /** set the recent tools buttons
   * @param buttons   array of buttons
   * @param recents   array of recent tools
   */
  private void setButtonRecent( ItemButton[] buttons, Symbol[] recents )
  {
    int kk = 0;
    for ( int k=0; k<NR_RECENT; ++k ) {
      Symbol p = recents[k];
      if ( p == null || buttons[k] == null ) break;
      if ( p.isPoint() && p.isSection() ) continue;
      if ( p.isPoint() ) TDLog.v("SET button point " + p.getThName() );
      buttons[kk].resetPaintPath( p.getPaint(), p.getScaledPath(), mRecentDimX, mRecentDimY );
      buttons[kk].invalidate();
      ++kk;
    }
  }

  // /** set the recent points tools buttons - merged in setButtonRecent using isPoint()
  //  * @param buttons   array of buttons
  //  * @param recents   array of recent tools
  //  * @note a special method for points is necessary to skip the "section" point
  //  */
  // private void setButtonRecentPoints( ItemButton[] buttons, Symbol[] recents )
  // {
  //   int kk = 0;
  //   for ( int k=0; k<NR_RECENT; ++k ) {
  //     Symbol p = recents[k];
  //     if ( p == null || buttons[kk] == null ) break;
  //     if ( p.isSection() ) continue;
  //     buttons[kk].resetPaintPath( p.getPaint(), p.getPath(), mRecentDimX, mRecentDimY );
  //     buttons[kk].invalidate();
  //     ++ kk;
  //   }
  // }

  private int highlightType = SymbolType.UNDEF; // type of current symbol highlighted
  private int highlightIndex = -1;              // index of current symbol highlighted

  /** highlight a symbol in the tools-bar
   * @param type  tools type to highlight
   * @param index index of the tool button to highlight -  if negative no tool is highlighted
   *
   * @note this method sets also highlightType and highlightIndex
   */
  private void setHighlight( int type, int index )
  {
    // TDLog.v("PLOT highlight " + type + " from " + highlightType + "/" + highlightIndex + " to " + index );
    if ( highlightIndex >= 0 && highlightIndex < NR_RECENT ) { // clear previous highlight
      switch ( highlightType ) { // switch off highlighted symbol
        case SymbolType.POINT:
          mBtnRecentP[ highlightIndex ].highlight( false );
          break;
        case SymbolType.LINE:
          mBtnRecentL[ highlightIndex ].highlight( false );
          break;
        case SymbolType.AREA:
          mBtnRecentA[ highlightIndex ].highlight( false );
          break;
      }
    }
    if ( index < 0 || index >= NR_RECENT ) {
      highlightIndex = -1;
      highlightType  = SymbolType.UNDEF;
    } else {
      highlightIndex = index;
      highlightType  = type;
      switch ( highlightType ) {
        case SymbolType.POINT:
          mBtnRecentP[ highlightIndex ].highlight( true );
          break;
        case SymbolType.LINE:
          mBtnRecentL[ highlightIndex ].highlight( true );
          break;
        case SymbolType.AREA:
          mBtnRecentA[ highlightIndex ].highlight( true );
          break;
      }
    }
  }
   
  /** set the current point symbol
   * @param k    index in the recent point array
   * @param update_recent whether to update the array of recent symbols
   */
  // @Override
  private boolean setCurrentPoint( int k, boolean update_recent )
  {
    int index = BrushManager.getPointIndex( mRecentPoint[k] );
    if ( index < 0 ) return false;
    if ( forbidPointSection( index ) || forbidPointPicture( index ) ) return false;
    mCurrentPoint = index;
    pointSelected( index, update_recent );
    updateAge( k, mRecentPointAge );
    return true;
  }

  /** set the current line symbol
   * @param k    index in the recent line array
   * @param update_recent whether to update the array of recent symbols
   */
  // @Override
  private boolean setCurrentLine( int k, boolean update_recent )
  {
    int current = BrushManager.getLineIndex( mRecentLine[k] );
    if ( current < 0 ) return false;
    if ( forbidLineSection( current ) ) return false;
    // TDLog.v("set line " + k + " update " + update_recent + " current " + current );
    mCurrentLine = current;
    lineSelected( current, update_recent );
    updateAge( k, mRecentLineAge );
    return true;
  }

  /** set the current area symbol
   * @param k    index in the recent area array
   * @param update_recent whether to update the array of recent symbols
   */
  // @Override
  private boolean setCurrentArea( int k, boolean update_recent )
  {
    int index = BrushManager.getAreaIndex( mRecentArea[k] );
    if ( index < 0 ) return false;
    mCurrentArea = index;
    areaSelected( index, update_recent );
    updateAge( k, mRecentAreaAge );
    return true;
  }

  /** set the recent symbols buttons, after the recent symbols have been loaded
   */
  @Override
  public void onRecentSymbolsLoaded()
  {
    // TDLog.v("on recent symbols loaded");
    mRecentDimX  = Float.parseFloat( getResources().getString( R.string.dimxl ) );
    mRecentDimY  = Float.parseFloat( getResources().getString( R.string.dimyl ) );
    setBtnRecentAll( );
  }

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

  
  /** highlight a station name
   * @param name  name to highight, or null to clear
   */
  void highlightStation( String name ) // STATION SEARCH and HIGHLIGHT
  {
    mDrawingSurface.highlightStation( name );
  }


}
