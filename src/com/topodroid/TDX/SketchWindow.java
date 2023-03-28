/* @file SketchWindow.java
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
import com.topodroid.utils.TDVersion;
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
import com.topodroid.ui.ItemButton;
import com.topodroid.ui.MotionEventWrap;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;
// import com.topodroid.dev.ConnectionState;
import com.topodroid.common.PlotType;
import com.topodroid.common.SymbolType;
import com.topodroid.common.PointScale;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.Locale; // TH2EDIT

import java.util.concurrent.RejectedExecutionException;
// import java.util.Deque; // REQUIRES API-9

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
// import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.RectF;
// import android.graphics.Rect;
import android.graphics.Path;

import android.net.Uri;

/**
 */
public class SketchWindow extends ItemDrawer
                          implements View.OnTouchListener
                                    , View.OnClickListener
                                    , OnItemClickListener
                                    , OnZoomListener
                                    // , ILabelAdder
                                    // , IZoomer
                                    // , IExporter
                                    // , IJoinClickHandler
{
  public static final int ZOOM_TRANSLATION = -50; // was -42
 
  private static final int[] izons_ok = {
                        R.drawable.iz_edit_ok, // 0
                        R.drawable.iz_eraser_ok,
                        R.drawable.iz_select_ok };

  private static final int[] izons = {
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,
                        R.drawable.iz_mode,          // 3
                        R.drawable.iz_note,          // 4
                        R.drawable.iz_wall,          // 5
                        R.drawable.iz_refresh,
                        R.drawable.iz_undo,          // 7
                        R.drawable.iz_redo,          // 8
                        R.drawable.iz_small,         // 9
                        R.drawable.iz_medium,        // 10
                        R.drawable.iz_large,         // 11
                        // R.drawable.iz_back,          // 11
                        // R.drawable.iz_forw,          // 12
                        R.drawable.iz_delete,        // 12
                        R.drawable.iz_delete_off,
                        R.drawable.iz_section_ok,    // 14
                        R.drawable.iz_section_no,    // 15
                        R.drawable.iz_open,          // 16
                        R.drawable.iz_clear,         // 17
                        R.drawable.iz_select_station,
                        R.drawable.iz_select_line,
                        R.drawable.iz_menu,
  };
  private static final int IC_SMALL          =  9;
  private static final int IC_MEDIUM         = 10;
  private static final int IC_LARGE          = 11;
  // private static final int IC_PREV        = 11;
  // private static final int IC_NEXT        = 12;
  private static final int IC_DELETE_OK      = 12;
  private static final int IC_DELETE_NO      = 13;
  private static final int IC_SECTION_OK     = 14;
  private static final int IC_SECTION_NO     = 15;
  private static final int IC_SECTION_OPEN   = 16;
  private static final int IC_SELECT_STATION = 18;
  private static final int IC_SELECT_LINE    = 19;
  private static final int IC_MENU           = 20;


  private static final int NR_BUTTON1 = 7; // MOVE
  private static final int NR_BUTTON2 = 5; // DRAW
  private static final int NR_BUTTON3 = 8; // SELECT
  private static final int NR_BUTTON5 = 6; // ERASE

  // private static final int mNrMove = 7;
  private static final int[] izons_move = {
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,
                        R.drawable.iz_mode,          // 3
                        R.drawable.iz_note,          // 4
                        R.drawable.iz_wall,
                        R.drawable.iz_refresh
  };
  private static final int[] help_icons_move = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_refs,
                        R.string.help_note,
                        R.string.help_walls_3d, // FIXME help_walls
                        R.string.help_refresh
  };

  // private static final int mNrDraw = 5;
  private static final int[] izons_draw = {
                        R.drawable.iz_edit_ok,       // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,
                        R.drawable.iz_undo,          // 
                        R.drawable.iz_redo           //
  };
  private static final int[] help_icons_draw = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_undo,
                        R.string.help_redo
  };

  // private static final int mNrEdit = 7;
  private static final int[] izons_select = {
                        R.drawable.iz_edit,
                        R.drawable.iz_eraser,
                        R.drawable.iz_select_ok,     // 2
                        // R.drawable.iz_back,          // 
                        // R.drawable.iz_forw,
                        R.drawable.iz_select_line,
                        R.drawable.iz_medium,
                        R.drawable.iz_delete_off,
                        R.drawable.iz_section_no,
                        R.drawable.iz_clear,
  };
  private static final int[] help_icons_select = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        // R.string.help_previous,
                        // R.string.help_next,
                        R.string.help_select_mode,
                        R.string.help_select_size,
                        R.string.help_delete_item,
                        R.string.help_section_item,
                        R.string.help_select_clear
  };
  // private static final int BTN_SELECT_PREV = 3;
  // private static final int BTN_SELECT_NEXT = 4;
  private static final int BTN_SELECT      = 2;
  private static final int BTN_SELECT_MODE = 3;
  private static final int BTN_SELECT_SIZE = 4;
  private static final int BTN_REMOVE      = 5;
  private static final int BTN_SECTION     = 6;

  // private static final int mNrErase = 6;
  private static final int[] izons_erase = {
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser_ok,
                        R.drawable.iz_select,
                        R.drawable.iz_undo,          // 3
                        R.drawable.iz_redo,          //
                        R.drawable.iz_medium         // 5
  };
  private static final int[] help_icons_erase = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_undo,
                        R.string.help_redo,
                        R.string.help_erase_size
  };
  private static final int BTN_ERASE_SIZE = 5;

  private static final int HELP_PAGE = R.string.SketchWindow;

  private TopoDroidApp mApp;
  private DataHelper mApp_mData;
  private TopoGL mTopoGL     = null;
  private Activity mActivity = null;
  private boolean mVertical  = true; // whether sections are vertical (or horizontal)
  private int mMaxSection = 0;       // number of last section - 0: leg-section
  private int mCurSection = 0;       // current section
  private String mSketchName;        // sketch name

  boolean getVertical() { return mVertical; }

  // long getSID() { return TDInstance.sid; }
  // String getSurvey() { return TDInstance.survey; }

  // private static BezierInterpolator mBezierInterpolator = new BezierInterpolator();
  private SketchSurface  mSketchSurface;
  // private DrawingAreaPath mCurrentAreaPath = null;
  // private SketchPath mFixedDrawingPath;
  // private Paint mCurrentPaint;

  // private static boolean mRecentToolsForward = true;

  // private boolean canRedo;
  private int mPointCnt; // counter of points in the currently drawing line
  private PointF mDisplayCenter;

  // private boolean mIsNotMultitouch;

  // ERASE - EDIT mode and size

  private int mEraseScale  = 1; // set in makeButtons
  private int mSelectScale = 1;
  private float mEraseSize  = TDSetting.mEraseness;
  private float mSelectSize = TDSetting.mSelectness;

  private boolean mSelectStation;

  // protected static int mEditRadius = 0; 

  private boolean mPointerDown = false;
  // private boolean mEditMove;      // whether moving the selected point
  // private boolean mShiftMove;     // whether to move the canvas in point-shift mode
  private EraseCommand mEraseCommand = null;

  private boolean mHasSelected = false;
  // private boolean hasPointActions  = false;

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
  // private LinearLayout mLayoutToolsP;
  private LinearLayout mLayoutToolsL;
  // private LinearLayout mLayoutToolsA;
  // private LinearLayout mLayoutScale;
  // private ItemButton[] mBtnRecent;
  // private ItemButton[] mBtnRecentP;
  private ItemButton[] mBtnRecentL;
  // private ItemButton[] mBtnRecentA;
  // private SeekBar      mScaleBar;

  // window mode
  static final int MODE_NONE  = 0; // initial mode
  static final int MODE_DRAW  = 1;
  static final int MODE_MOVE  = 2;
  static final int MODE_EDIT  = 3;
  static final int MODE_ZOOM  = 4; // used only for touchMode
  // static final int MODE_SHIFT = 5; // change point symbol position
  static final int MODE_ERASE = 6;
  static final int MODE_ROTATE = 7; 

  // line join-continue
  // private static final int CONT_OFF   = -1; // continue off
  // public  static final int CONT_NONE  = 0;  // no continue
  // private static final int CONT_START = 1;  // continue: join to existing line
  // private static final int CONT_END   = 2;  // continue: join to existing line
  // private static final int CONT_BOTH  = 3;  // continue: join to existing line
  // private static final int CONT_CONTINUE  = 4;  // continue: continue existing line
  // static final private int CONT_MAX   = 5;

  private int mMode         = MODE_NONE;
  private int mTouchMode    = MODE_MOVE;
  // private int mContinueLine = CONT_NONE;
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
  // private float mRotateScale = 180 / TopoDroidApp.mDisplayHeight;

  // private boolean mAllSymbols; // whether the library has all the symbols of the plot

  // -------------------------------------------------------------
  // STATUS items

  // private long mSid;  // survey id
  // private long mPid;  // current plot id
  private Point2D mOffset = new Point2D();
  private TDVector mU = null;

  // ----------------------------------------------------------------
  // BUTTONS and MENU

  // private Button mButtonHelp;
  private int mButtonSize;
  private Button[] mButton1; // primary
  private Button[] mButton2; // draw
  private Button[] mButton3; // edit
  private Button[] mButton5; // eraser
  // private int mNrButton1 = NR_BUTTON1;
  // private int mNrButton2 = NR_BUTTON2;
  // private int mNrButton3 = NR_BUTTON3;
  // private int mNrButton5 = NR_BUTTON5; // erase
  private MyHorizontalButtonView mButtonView1;
  private MyHorizontalButtonView mButtonView2;
  private MyHorizontalButtonView mButtonView3;
  private MyHorizontalButtonView mButtonView5;

  private BitmapDrawable mBMselect;
  private BitmapDrawable mBMback;
  // private BitmapDrawable mBMjoin;
  // private BitmapDrawable mBMjoin_no;
  // private BitmapDrawable mBMedit_item = null;
  // private BitmapDrawable mBMedit_box  = null;
  // private BitmapDrawable mBMedit_ok   = null;
  // private BitmapDrawable mBMedit_no   = null;
  // private BitmapDrawable mBMcont_none;
  // private BitmapDrawable mBMcont_start;
  // private BitmapDrawable mBMcont_end;
  // private BitmapDrawable mBMcont_both;
  // private BitmapDrawable mBMcont_continue;
  // private BitmapDrawable mBMcont_off;
  // private BitmapDrawable mBMsplays_line;
  // private BitmapDrawable mBMsplays_point;
  private BitmapDrawable mBMdeleteOff;
  private BitmapDrawable mBMdeleteOn;
  // private BitmapDrawable mBMadd;
  // private BitmapDrawable mBMeraseAll;
  // private BitmapDrawable mBMerasePoint;
  // private BitmapDrawable mBMeraseLine;
  // private BitmapDrawable mBMeraseArea;
  private BitmapDrawable mBMsmall;
  private BitmapDrawable mBMmedium;
  private BitmapDrawable mBMlarge;
  // private BitmapDrawable mBMmenured;
  private BitmapDrawable mBMmenublue;
  // private BitmapDrawable mBMprev;
  // private BitmapDrawable mBMnext;
  private BitmapDrawable mBMsectionOk;
  private BitmapDrawable mBMsectionNo;
  private BitmapDrawable mBMsectionOpen;
  private BitmapDrawable mBMselectStation;;
  private BitmapDrawable mBMselectLine;;
  // private BitmapDrawable mBMempty;
  private BitmapDrawable mBMwall;

  private MyHorizontalListView mListView;
  private ListView   mMenu;
  private Button     mMenuImage;
  private boolean    onMenu;

  private int mNrSaveTh2Task = 0; // current number of save tasks

  private void setButton1( int k, BitmapDrawable bm ) { if ( k < NR_BUTTON1 ) TDandroid.setButtonBackground( mButton1[ k ], bm ); }
  private void setButton2( int k, BitmapDrawable bm ) { if ( k < NR_BUTTON2 ) TDandroid.setButtonBackground( mButton2[ k ], bm ); }
  private void setButton3( int k, BitmapDrawable bm ) { if ( k < NR_BUTTON3 ) TDandroid.setButtonBackground( mButton3[ k ], bm ); }
  private void setButton5( int k, BitmapDrawable bm ) { if ( k < NR_BUTTON5 ) TDandroid.setButtonBackground( mButton5[ k ], bm ); }

  // ----------------------------------------------------------

  /** test if the drawing window is in draw_edit
   * @return ...
   */
  boolean isNotModeEdit() { return mMode != MODE_EDIT; }

  // -------------------------- SAVE --------------------------------------
  private boolean mModified; // whether the sketch has been modified 
  private Handler saveHandler = new Handler();

  private final Runnable saveRunnable = new Runnable() {
    @Override 
    public void run() {
      TDLog.v("TODO start save tdr task [1]");
      // startSaveTdrTask( PlotSave.MODIFIED, TDSetting.mBackupNumber, 1 );
      mModified = false;
    }
  };

  /** mark the plot "modified"
   */
  private void modified()
  {
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
    if ( saveHandler != null && saveRunnable != null ) {
      saveHandler.removeCallbacks( saveRunnable );
    }
  }

  // -------------------------------------------------------------------
  // ZOOM CONTROLS

  /** @return zoom value
   */
  public float xOffset() { return mOffset.x; }
  public float yOffset() { return mOffset.y; }

  /** react to a change of visibility of zoom -controls
   * @param visible  whether controls should become visible
   */
  @Override
  public void onVisibilityChanged(boolean visible)
  {
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
    if ( f < 0.05f || f > 4.0f ) return;
    float zoom_old = mSketchSurface.getZoom();
    float zoom_new = zoom_old * f;
    // TDLog.v( "zoom " + zoom );
    adjustOffset( zoom_old, zoom_new );
    // TDLog.v( "change zoom " + mOffset.x + " " + mOffset.y + " " + zoom_new );
    mSketchSurface.setTransform( this, mOffset.x, mOffset.y, zoom_new );
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

  /**
   * @param load   whether to try and load from file
   */
  private void makeReference( Cave3DShot leg, boolean load )
  {
    // TDLog.v("SKETCH make reference");
    setSelectMode( false );
    mSketchName = leg.from + "-" + leg.to;
    TDVector v1 = new TDVector();
    TDVector v2 = leg.toTDVector(); // (E,N,Up)
    mU = v2.getUnitVector();

    float theta = TDMath.atan2d( v2.z, TDMath.sqrt( v2.x*v2.x + v2.y*v2.y) );
    // TDLog.v("SKETCH leg " + leg.from + "-" + leg.to + " L " + leg.len + " A " + leg.ber + " C " + leg.cln );
    // TDLog.v("SKETCH V2: E " + v2.x + " N " + v2.y + " Up " + v2.z + " theta " + theta );
    addFixedLeg( v1, v2 );
    Cave3DStation st1 = leg.from_station;
    Cave3DStation st2 = leg.to_station;
    addStation( v1, st1.getShortName(), leg.from, null, false );
    addStation( v2, st2.getShortName(), leg.to,   null, false );
    for ( Cave3DShot sp : mTopoGL.getSplaysAt( st1 ) ) {
      TDVector v = sp.toTDVector(); // .plus( v1 );
      addFixedSplay( v1, v );
    }
    for ( Cave3DShot sp : mTopoGL.getSplaysAt( st2 ) ) {
      TDVector v = sp.toTDVector().plus( v2 );
      addFixedSplay( v2, v );
    }
    for ( Cave3DShot lg : mTopoGL.getLegsAt( st1 ) ) {
      if ( lg != leg ) {
        if ( lg.from_station == st1 ) {
          TDVector v = v1.plus( lg.toTDVector() );
          addFixedNghbleg( v1, v );
          addStation( v, lg.to_station.getShortName(), lg.to, lg.from, true );
          // TDLog.v("SKETCH station " + lg.from + "-" + lg.to + " L " + lg.len + " A " + lg.ber + " C " + lg.cln + " " + v.x + " " + v.y + " " + v.z );
        } else if ( lg.to_station == st1 ) {
          TDVector v = v1.minus( lg.toTDVector() );
          addFixedNghbleg( v1, v );
          addStation( v, lg.from_station.getShortName(), lg.from, lg.to, false );
          // TDLog.v("SKETCH station " + lg.from + "-" + lg.to + " L " + lg.len + " A " + lg.ber + " C " + lg.cln + " " + v.x + " " + v.y + " " + v.z );
        }
      }
    }
    for ( Cave3DShot lg : mTopoGL.getLegsAt( st2 ) ) {
      if ( lg != leg ) {
        if ( lg.from_station == st2 ) {
          TDVector v = v2.plus( lg.toTDVector() );
          addFixedNghbleg( v2, v );
          addStation( v, lg.to_station.getShortName(), lg.to, lg.from, true );
          // TDLog.v("SKETCH station " + lg.from + "-" + lg.to + " L " + lg.len + " A " + lg.ber + " C " + lg.cln + " " + v.x + " " + v.y + " " + v.z );
        } else if ( lg.to_station == st2 ) {
          TDVector v = v2.minus( lg.toTDVector() );
          addFixedNghbleg( v2, v );
          addStation( v, lg.from_station.getShortName(), lg.from,  lg.to, false );
          // TDLog.v("SKETCH station " + lg.from + "-" + lg.to + " L " + lg.len + " A " + lg.ber + " C " + lg.cln + " " + v.x + " " + v.y + " " + v.z );
        }
      }
    }

    // TODO make the grid
    int i1 = -3;
    int i2 =  3;
    int j1 =  0;
    int j2 = (int)( v2.length() + 1);
    float z = ( v1.z < v2.z )? v1.z : v2.z;
    // TDVector G = new TDVector( 0, 0, z );
    TDVector L = new TDVector( v2.x, v2.y, 0 ); 
    L.normalize();
    TDVector H = new TDVector( L.y, -L.x, 0 );
    for ( int i = i1; i<=i2; ++i ) {
      addFixedGrid ( new TDVector( i*H.x + j1*L.x, i*H.y + j1*L.y, z ), new TDVector( i*H.x + j2*L.x, i*H.y + j2*L.y, z ), 1 );
    }
    for ( int j=j1; j<=j2; ++j ) {
      addFixedGrid ( new TDVector( i1*H.x + j*L.x, i1*H.y + j*L.y, z ), new TDVector( i2*H.x + j*L.x, i2*H.y + j*L.y, z ), 1 );
    }
    mSketchSurface.doneReference( theta );

    if ( load ) {
      String filename = TDPath.getC3dFile( mSketchName );
      boolean ret = doLoad( filename );
      // TDLog.v("SKETCH loading " + filename + " return " + ret );
    }
  }

  private boolean doLoad( String filename )
  {
    if ( ! TDFile.hasTopoDroidFile( filename ) ) return false;
    boolean ret = false;
    int what;
    int version = 0;
    String name = "";
    DataInputStream dis = null;
    try {
      dis = TDFile.getTopoDroidFileInputStream( filename );
      while ( ( what = dis.read() ) != 'E' ) {
        switch ( what ) {
          case 'V':
            version = dis.readInt();
            break;
          case 'S':
            name = dis.readUTF();
            assert( name.equals( mSketchName ) );
            mVertical = (dis.readInt() == 0);
            mMaxSection = mSketchSurface.fromDataStream( dis, version, mVertical );
            break;
          case 'E':
            break;
        }
      }
      ret = true;
    } catch ( IOException e ) {
      // TODO
    } finally {
      try {
        if ( dis != null ) dis.close();
      } catch ( IOException e ) { /* ignore */ }
    }
    mCurSection = 0;
    // TDLog.v("READ sketch " + name + " - max " + mMaxSection );
    return ret;
  }

  /**
   * @param filename   filename
   * @param name       sketch name
   */
  private boolean doSave( String filename, String name )
  {
    boolean ret = false;
    DataOutputStream dos = null;
    try {
      dos = TDFile.getTopoDroidFileOutputStream( filename );
      dos.write( 'V' ); // version
      dos.writeInt( TDVersion.code() );
      dos.write( 'S' );
      dos.writeUTF( name );
      dos.writeInt( mVertical? 0 : 1 ); 
      mSketchSurface.toDataStream( dos ); // forward to sketch command manager
      dos.write( 'E' );
      dos.flush();
    } catch ( IOException e ) {
      // TODO
    } finally {
      try {
        if ( dos != null ) dos.close();
      } catch ( IOException e ) { /* ignore */ }
    }
    return ret;
  }
      
      


  // /** set the projection 
  //  * @param C    center
  //  * @param X    X (rightward) unit vector
  //  * @param Y    Y (downward) unit vector
  //  * @param z    zoom 
  //  */
  // private void setProjection( TDVector C, TDVector X, TDVector Y, float z )
  // {
  //   mSketchSurface.setProjection( C, X, Y, z );
  // }

  /** add a new station
   * @param v     station vector
   * @param name  station name
   * @param fullname station fullname
   * @param from  other station on the leg (null for the leg stations) [fullname]
   * @param forward true if the station is TO-station of the leg (false if FROM is null)
   */
  private SketchPath addStation( TDVector v, String name, String fullname, String from, boolean forward )
  { 
    SketchStationPath path = new SketchStationPath( BrushManager.labelPaint, v, name, fullname, from, forward );
    mSketchSurface.addStationPath( path );
    return path;
  }

  /** used to add legs and splays
   * @param v1    leg first endpoint (E,N,Up)
   * @param v2    leg second endpoint (E,N,Up)
   * @note this starts a new reference in the command manager
   */
  private SketchPath addFixedLeg( TDVector v1, TDVector v2 )
  {
    SketchFixedPath path = new SketchFixedPath( SketchPath.SKETCH_PATH_LEG, BrushManager.fixedShotPaint, v1, v2 );
    mSketchSurface.addFixedLegPath( path );
    return path;
  }

  /** used to add legs and splays
   * @param v1    leg first endpoint (E,N,Up)
   * @param v2    leg second endpoint (E,N,Up)
   * @note this starts a new reference in the command manager
   */
  private SketchPath addFixedNghbleg( TDVector v1, TDVector v2 )
  {
    SketchFixedPath path = new SketchFixedPath( SketchPath.SKETCH_PATH_NGHB, BrushManager.fixedShotPaint, v1, v2 );
    mSketchSurface.addFixedNghblegPath( path );
    return path;
  }

  /** used to add legs and splays
   * @param v1    splay first endpoint (E,N,Up)
   * @param v2    splay second endpoint (E,N,Up)
   */
  private SketchPath addFixedSplay( TDVector v1, TDVector v2 )
  {
    SketchFixedPath path = new SketchFixedPath( SketchPath.SKETCH_PATH_SPLAY, BrushManager.fixedBluePaint, v1, v2 );
    mSketchSurface.addFixedSplayPath( path );
    return path;
  }

  /** used to add legs and splays
   * @param v1    grid-line first endpoint (E,N,Up)
   * @param v2    grid-line second endpoint (E,N,Up)
   * @param k     grip order (1, 2, or 3)
   */
  private SketchPath addFixedGrid( TDVector v1, TDVector v2, int k )
  {
    Paint paint = BrushManager.fixedGridPaint;
    if ( k == 2 ) { paint = BrushManager.fixedGrid10Paint; }
    else if ( k == 3 ) { paint = BrushManager.fixedGrid100Paint; }

    SketchFixedPath path = new SketchFixedPath( SketchPath.SKETCH_PATH_GRID, paint, v1, v2 );
    mSketchSurface.addGridPath( path, k );
    return path;
  }


  // --------------------------------------------------------------------------------------

  /** select a line symbol
   * @param k       index of selected line-tool in the symbol-library array
   * @param update_recent ...
   */
  @Override
  public void lineSelected( int k, boolean update_recent )
  {
    super.lineSelected( k, update_recent );
  }

  /** react to a change in the configuration
   * @param new_cfg   new configuration
   */
  @Override
  public void onConfigurationChanged( Configuration new_cfg )
  {
    super.onConfigurationChanged( new_cfg );
    TDLocale.resetTheLocale();
    mSketchSurface.setTransform( this, mOffset.x, mOffset.y );
    // setMenuAdapter( getResources() );
  }

  /** set the title of the window
   */
  @Override
  public void setTheTitle()
  {
    Resources res = getResources();
    StringBuilder sb = new StringBuilder();
    // sb.append(mName);
    // sb.append(": ");
    sb.append( res.getString( mVertical? R.string.ctitle_sketch_v : R.string.ctitle_sketch_h ) ).append(" ").append( mSketchName ).append(" ");
    
    if ( mMode == MODE_DRAW ) { 
      // if ( mSymbol == SymbolType.POINT ) {
      //   sb.append( String.format( res.getString(R.string.title_draw_point), BrushManager.getPointName( mCurrentPoint ) ) );
      // } else if ( mSymbol == SymbolType.LINE ) {
        sb.append( String.format( res.getString(R.string.title_draw_line), BrushManager.getLineName( mCurrentLine ) ) );
      // } else  {  // if ( mSymbol == SymbolType.AREA ) 
      //   sb.append( String.format( res.getString(R.string.title_draw_area), BrushManager.getAreaName( mCurrentArea ) ) );
      // }
      // // boolean visible = ( mSymbol == SymbolType.LINE && mCurrentLine == BrushManager.getLineWallIndex() );
      // boolean visible = ( mSymbol == SymbolType.LINE );
      // mButton2[ BTN_CONT ].setVisibility( visible? View.VISIBLE : View.GONE );
    } else if ( mMode == MODE_MOVE ) {
      sb.append( res.getString( R.string.title_move ) );
    } else if ( mMode == MODE_EDIT ) {
      sb.append( res.getString( R.string.title_edit ) );
    } else if ( mMode == MODE_ERASE ) {
      sb.append( res.getString( R.string.title_erase ) );
    }
    sb.append( String.format(Locale.US, ": %.0f %.0f", mSketchSurface.getLegViewRotationAlpha(), mSketchSurface.getLegViewRotationBeta() ) );
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
    // if ( dismissPopups() != DISMISS_NONE ) return;
    if ( doubleBack ) {
      if ( doubleBackToast != null ) doubleBackToast.cancel();
      doubleBackToast = null;
      // TDLog.v( "double back pressed ...");
      super.onBackPressed();
    } else {
      doubleBack = true;
      doubleBackToast = TDToast.makeToast( R.string.double_back );
      doubleBackHandler.postDelayed( doubleBackRunnable, 1000 );
    }
  }

  /** start the task to save the TDR file
   * @note called by doPause 
   */
  private void doSaveTdr( )
  {
    if ( mSketchSurface != null ) {
      TDLog.v("TODO start save tdr task [3]");
      // Modified = true; // force saving: Modified is checked before spawning the saving task
      // startSaveTdrTask( PlotSave.SAVE, TDSetting.mBackupNumber+2, TDPath.NR_BACKUP );
    }
    resetModified();
  }

  // static private Handler saveHandler = null;

  // ---------------------------------------------------------------------------------------


  /** set the button3 by the type of the hot-item
   * @param pt   hot item
   */
  private void setButton3Item( SketchPoint pt )
  {
    boolean deletable = ( pt != null );
    setButton3( BTN_REMOVE, (deletable ? mBMdeleteOn : mBMdeleteOff) );
  }

  // /** set the button3 to display "prev/next" 
  //  */
  // private void setButton3PrevNext( )
  // {
  //   if ( mHasSelected ) {
  //     setButton3( BTN_SELECT_PREV, mBMprev );
  //     setButton3( BTN_SELECT_NEXT, mBMnext );
  //   } else {
  //     setButtonSelectSize( mSelectScale );
  //   }
  // }

  // /** set the button "continue"
  //  * @param continue_line    type of line-continuation
  //  * @note must be called only if TDLevel.overNormal
  //  */
  // private void setButtonContinue( int continue_line )
  // {
  //   mContinueLine = continue_line;
  //   if ( BTN_CONT < NR_BUTTON2 ) {
  //     if ( mSymbol == SymbolType.LINE /* && mCurrentLine == BrushManager.getLineWallIndex() */ ) {
  //       mButton2[ BTN_CONT ].setVisibility( View.VISIBLE );
  //       switch ( mContinueLine ) {
  //         case CONT_NONE:  setButton2( BTN_CONT, mBMcont_none  ); break;
  //         case CONT_START: setButton2( BTN_CONT, mBMcont_start ); break;
  //         case CONT_END:   setButton2( BTN_CONT, mBMcont_end   ); break;
  //         case CONT_BOTH:  setButton2( BTN_CONT, mBMcont_both  ); break;
  //         case CONT_CONTINUE: setButton2( BTN_CONT, mBMcont_continue  ); break;
  //         case CONT_OFF: setButton2( BTN_CONT, mBMcont_off  );
  //       }
  //     } else {
  //       mButton2[ BTN_CONT ].setVisibility( View.GONE );
  //     }
  //   }
  // }

  /** set button "size" to display a given scale
   * @param scale    scale
   */
  private void setButtonEraseSize( int scale )
  {
    mEraseScale = scale % Drawing.SCALE_MAX;
    // TDLog.v("SKETCH erase scale " + mEraseScale );
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
    setButton3( BTN_REMOVE, (on ? mBMdeleteOn : mBMdeleteOff) );
  }

  /** set button "size" to display a given scale
   * @param scale    scale
   */
  private void setButtonSelectSize( int scale )
  {
    mSelectScale = scale % Drawing.SCALE_MAX;
    // TDLog.v("SKETCH select scale " + mSelectScale );
    switch ( mSelectScale ) {
      case Drawing.SCALE_SMALL:
        mSelectSize = 0.5f * TDSetting.mSelectness;
        setButton3( BTN_SELECT_SIZE, mBMsmall );
        break;
      case Drawing.SCALE_MEDIUM:
        mSelectSize = TDSetting.mSelectness;
        setButton3( BTN_SELECT_SIZE, mBMmedium );
        break;
      case Drawing.SCALE_LARGE:
        mSelectSize = 2.0f * TDSetting.mSelectness;
        setButton3( BTN_SELECT_SIZE, mBMlarge );
        break;
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
    // TDLog.v("Buttons " + NR_BUTTON1 + " " + NR_BUTTON2 + " " + NR_BUTTON3 + " " + NR_BUTTON5 );

    mButton1 = new Button[ NR_BUTTON1 + 1 ]; // MOVE
    for ( int k=0; k<NR_BUTTON1; ++k ) {
      mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
    }
    mButton1[ NR_BUTTON1 ] = MyButton.getButton( mActivity, null, R.drawable.iz_empty );

    mButton2 = new Button[ NR_BUTTON2 + 1 ]; // DRAW
    for ( int k=0; k<NR_BUTTON2; ++k ) {
      mButton2[k] = MyButton.getButton( mActivity, this, izons_draw[k] );
    }
    mButton2[ NR_BUTTON2 ] = mButton1[ NR_BUTTON1 ];

    mButton3 = new Button[ NR_BUTTON3 + 1 ];      // EDIT
    for ( int k=0; k<NR_BUTTON3; ++k ) {
      mButton3[k] = MyButton.getButton( mActivity, this, izons_select[k] );
    }
    mButton3[ NR_BUTTON3 ] = mButton1[ NR_BUTTON1 ];

    mButton5 = new Button[ NR_BUTTON5 + 1 ];    // ERASE
    for ( int k=0; k<NR_BUTTON5; ++k ) {
      mButton5[k] = MyButton.getButton( mActivity, this, izons_erase[k] );
    }
    mButton5[ NR_BUTTON5 ] = mButton1[ NR_BUTTON1 ];

    // mBMprev     = MyButton.getButtonBackground( this, res, izons[IC_PREV] );
    // mBMnext     = MyButton.getButtonBackground( this, res, izons[IC_NEXT] );

    mBMselect = MyButton.getButtonBackground( this, res, izons_draw[2] );
    mBMback   = MyButton.getButtonBackground( this, res, R.drawable.iz_back );

    mBMsmall    = MyButton.getButtonBackground( this, res, izons[IC_SMALL] );
    mBMmedium   = MyButton.getButtonBackground( this, res, izons[IC_MEDIUM] );
    mBMlarge    = MyButton.getButtonBackground( this, res, izons[IC_LARGE] );

    mBMsectionOk = MyButton.getButtonBackground( this, res, izons[IC_SECTION_OK] );
    mBMsectionNo = MyButton.getButtonBackground( this, res, izons[IC_SECTION_NO] );
    mBMsectionOpen = MyButton.getButtonBackground( this, res, izons[IC_SECTION_OPEN] );
    mBMselectStation = MyButton.getButtonBackground( this, res, izons[IC_SELECT_STATION] );
    mBMselectLine    = MyButton.getButtonBackground( this, res, izons[IC_SELECT_LINE] );
    // mBMempty         = MyButton.getButtonBackground( this, res, R.drawable.iz_empty );

    mBMdeleteOn  = MyButton.getButtonBackground( this, res, izons[IC_DELETE_OK] );
    mBMdeleteOff = MyButton.getButtonBackground( this, res, izons[IC_DELETE_NO] );

    setButtonEraseSize( Drawing.SCALE_MEDIUM );
    setButtonSelectSize( Drawing.SCALE_MEDIUM );

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mButtonView2 = new MyHorizontalButtonView( mButton2 );
    mButtonView3 = new MyHorizontalButtonView( mButton3 );
    mButtonView5 = new MyHorizontalButtonView( mButton5 );
  }

  void setDrawEraseEditButtons( boolean leg )
  {
    if ( leg ) {
      mButton3[2].setVisibility( View.VISIBLE );
      mButton3[2].setOnClickListener( this );
    } else {
      mButton3[2].setVisibility( View.GONE );
      mButton3[2].setOnClickListener( null );
    }
    setMode( MODE_MOVE );
    mTouchMode = MODE_MOVE;
  }

  /** set the params of the tools toolbar
   */
  public void setToolsToolbarParams()
  {
    float scale = 8 * TDSetting.mItemButtonSize;
    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams( 0, LinearLayout.LayoutParams.WRAP_CONTENT );
    lp.setMargins( 0, 0, 0, 0 );
    lp.weight = 16;
    lp.gravity = 0x10; // LinearLayout.LayoutParams.center_vertical;
    {
      ViewGroup.LayoutParams lp0;
      // lp0 = mLayoutToolsP.getLayoutParams();
      // lp0.height = (int)(scale * Float.parseFloat( getResources().getString( R.string.dimyl ) ) ) + 8; // 4 pxl on both sides 
      // mLayoutToolsP.setLayoutParams( lp0 );

      lp0 = mLayoutToolsL.getLayoutParams();
      lp0.height = (int)(scale * Float.parseFloat( getResources().getString( R.string.dimyl ) ) ) + 8; // 4 pxl on both sides 
      mLayoutToolsL.setLayoutParams( lp0 );

      // lp0 = mLayoutToolsA.getLayoutParams();
      // lp0.height = (int)(scale * Float.parseFloat( getResources().getString( R.string.dimyl ) ) ) + 8; // 4 pxl on both sides 
      // mLayoutToolsA.setLayoutParams( lp0 );
    }
    // mLayoutToolsP.removeAllViews( );
    mLayoutToolsL.removeAllViews( );
    // mLayoutToolsA.removeAllViews( );
    for ( int k = 0; k<=NR_RECENT; ++k ) {
      // mLayoutToolsP.addView( mBtnRecentP[k], lp );
      mLayoutToolsL.addView( mBtnRecentL[k], lp );
      // mLayoutToolsA.addView( mBtnRecentA[k], lp );
    }
  }

  @Override
  public void onCreate( Bundle savedInstanceState ) 
  {
    super.onCreate(savedInstanceState);

    TDandroid.setScreenOrientation( this );

    // TDLog.TimeStart();
    // TDLog.v( "Drawing Window on create" );

    mApp  = (TopoDroidApp)getApplication();
    mActivity = this;
    mApp_mData = TopoDroidApp.mData; 

    // audioCheck = TDandroid.checkMicrophone( mActivity );
    // TDLog.v( "Microphone perm : " + audioCheck );

    mZoomBtnsCtrlOn = (TDSetting.mZoomCtrl > 1);  // do before setting content
    mPointScale = PointScale.SCALE_M;

    // mIsNotMultitouch = ! TDandroid.checkMultitouch( this );

    setContentView( R.layout.sketch_activity );

    mDisplayCenter = new PointF(TopoDroidApp.mDisplayWidth  / 2, TopoDroidApp.mDisplayHeight / 2);

    // setCurrentPaint();

    mSketchSurface = (SketchSurface) findViewById(R.id.sketchSurface);
    mSketchSurface.setParent( this );
    mSketchSurface.setOnTouchListener(this);
    // mSketchSurface.setBuiltInZoomControls(true);

    mZoomView = (View) findViewById(R.id.zoomView );
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


    Bundle extras = getIntent().getExtras();
    if ( extras != null ) { // TODO handle extras 
      mVertical = extras.getBoolean( "TOPOGL_SKETCH_VERTICAL" );
    } 
    // // mDrawingUtil = mLandscape ? (new DrawingUtilLandscape()) : ( new DrawingUtilPortrait());
    // mDrawingUtil = new DrawingUtilPortrait();

    // mEraseScale = 0;  done in makeButtons()
    // mSelectScale = 0;
    makeButtons( );

    mListView.setAdapter( mButtonView1.mAdapter );
    // mListView.invalidate();

    // mContinueLine = TDSetting.mContinueLine;
    resetModified();

    // TDLog.TimeEnd( "on create" );

    // TDLog.v( "on create" );
    // if ( mCurrentPoint < 0 ) mCurrentPoint = ( BrushManager.isPointEnabled(  SymbolLibrary.LABEL  ) )?  1 : 0;
    if ( mCurrentLine < 0 )  mCurrentLine  = ( BrushManager.isLineEnabled( SymbolLibrary.WALL ) )?  1 : 0;
    // if ( mCurrentArea < 0 )  mCurrentArea  = ( BrushManager.isAreaEnabled( SymbolLibrary.WATER ) )?  1 : 0;

    doStart( );

    mLayoutTools  = (LinearLayout) findViewById( R.id.layout_tools  );
    // mLayoutToolsP = (LinearLayout) findViewById( R.id.layout_tool_p );
    mLayoutToolsL = (LinearLayout) findViewById( R.id.layout_tool_l );
    // mLayoutToolsA = (LinearLayout) findViewById( R.id.layout_tool_a );
    // mLayoutScale  = (LinearLayout) findViewById( R.id.layout_scale  );
    // mScaleBar     = (SeekBar)findViewById( R.id.scalebar );
    // mScaleBar.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
    //   public void onProgressChanged( SeekBar seekbar, int progress, boolean fromUser) {
    //     if ( fromUser ) {
    //       setPointScaleProgress( progress );
    //     }
    //   }
    //   public void onStartTrackingTouch(SeekBar seekbar) { }
    //   public void onStopTrackingTouch(SeekBar seekbar) { }
    // } );
    mLayoutTools.setVisibility( View.INVISIBLE );

    // mBtnRecentP = new ItemButton[ NR_RECENT + 1 ];
    mBtnRecentL = new ItemButton[ NR_RECENT + 1 ];
    // mBtnRecentA = new ItemButton[ NR_RECENT + 1 ];
    for ( int k = 0; k<NR_RECENT; ++k ) {
      // mBtnRecentP[k] = new ItemButton( this );
      // mBtnRecentP[k].setOnClickListener(
      //   new View.OnClickListener() {
      //     @Override public void onClick( View v ) {
      //       for ( int k = 0; k<NR_RECENT; ++k ) if ( v == mBtnRecentP[k] ) {
      //         if ( setCurrentPoint( k, false ) ) {
      //           setHighlight( SymbolType.POINT, k );
      //         } else {
      //           TDToast.makeWarn( R.string.section_point_not_allowed );
      //         }
      //         break;
      //       }
      //     }
      //   }
      // );
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
      // mBtnRecentA[k] = new ItemButton( this );
      // mBtnRecentA[k].setOnClickListener(
      //   new View.OnClickListener() {
      //     @Override public void onClick( View v ) {
      //       for ( int k = 0; k<NR_RECENT; ++k ) if ( v == mBtnRecentA[k] ) {
      //         setCurrentArea( k, false );
      //         setHighlight( SymbolType.AREA, k );
      //         break;
      //       }
      //     }
      //   }
      // );
    }
    // mBtnRecentP[NR_RECENT] = new ItemButton( this );
    mBtnRecentL[NR_RECENT] = new ItemButton( this );
    // mBtnRecentA[NR_RECENT] = new ItemButton( this );

    // mBtnRecentP[NR_RECENT].setText( ">>" );
    mBtnRecentL[NR_RECENT].setText( ">>" );
    // mBtnRecentA[NR_RECENT].setText( ">>" );

    Path path = new Path(); // double-arrow ">>"
    path.moveTo( 0, 8 ); path.lineTo(  8, 0 ); path.lineTo( 0, -8 );
    path.moveTo( 8, 8 ); path.lineTo( 16, 0 ); path.lineTo( 8, -8 );

    // mBtnRecentP[NR_RECENT].resetPaintPath( BrushManager.labelPaint, path, 2, 2 ); 
    // mBtnRecentP[NR_RECENT].invalidate();
    // mBtnRecentP[NR_RECENT].setOnClickListener(
    //   new View.OnClickListener() {
    //     @Override public void onClick( View v ) { startItemPickerDialog( SymbolType.POINT ); }
    //   }
    // );
    mBtnRecentL[NR_RECENT].resetPaintPath( BrushManager.labelPaint, path, 2, 2 );
    mBtnRecentL[NR_RECENT].invalidate();
    mBtnRecentL[NR_RECENT].setOnClickListener(
      new View.OnClickListener() {
        @Override public void onClick( View v ) { startItemPickerDialog( SymbolType.LINE ); }
      }
    );
    // mBtnRecentA[NR_RECENT].resetPaintPath( BrushManager.labelPaint, path, 2, 2 );
    // mBtnRecentA[NR_RECENT].invalidate();
    // mBtnRecentA[NR_RECENT].setOnClickListener(
    //   new View.OnClickListener() {
    //     @Override public void onClick( View v ) { startItemPickerDialog( SymbolType.AREA ); }
    //   }
    // );

    setToolsToolbarParams();
    // setBtnRecentAll(); done on Start
    // mRecentTools = mRecentLine; // done on Start

    // TopoDroidApp.mSketchWindow = this;

    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on create done");

    setTheTitle();
  }

  // ------------------------------------- PUSH / POP INFO --------------------------------

  /** reset the status mode(s) to the default values
   */
  private void resetStatus()
  {
    // mContinueLine = TDSetting.mContinueLine; // do not reset cont-mode
    resetModified();
    setMode( MODE_MOVE ); // this setTheTitle() as well, and clearHotPath( INVISIBLE )
    mTouchMode = MODE_MOVE;
    // setMenuAdapter( getResources() );
  }

  // /** update splays for a xsection
  //  * @param v1   first xsection crosspoint (E,N,Up)
  //  * @param v2   second xsection crosspoint
  //  * @param vertical  whether the section is vertical (or horizontal)
  //  * @param thr       xseplay distance threshold from the section plane
  //  */
  // private void updateSplaysAt( TDVector v1, TDVector v2, boolean vertical, float thr )
  // {
  //   float dx = v2.x - v1.x;
  //   float dy = v2.y - v1.y;
  //   float dz = v2.z - v1.z;
  //   float dh = TDMath.sqrt( dx*dx + dy*dy );
  //   if ( vertical ) {
  //     TDVector S = new TDVector(      0,     0, -1 );
  //     TDVector H = new TDVector(  dx/dh, dy/dh,  0 );
  //     TDVector N = new TDVector( -dy/dh, dx/dh,  0 ); // H ^ S
  //     for ( Cave3DShot sp : mSplays ) {
  //       TDVector v = sp.toTDVector().minus( v1 );
  //       float z = N.dot( v );
  //       if ( Math.abs( z ) < thr ) {
  //         float x = H.dot( v );
  //         float y = S.dot( v ); // downward
  //         // TODO add splay point at (x,y)
  //       }
  //     }
  //   } else {
  //     TDVector N = new TDVector(      0,     0, -1 ); // downward
  //     TDVector H = new TDVector(  dx/dh,  dy/dh, 0 );
  //     TDVector S = new TDVector(  dy/dh, -dx/dh, 0 ); // N ^ H
  //     for ( Cave3DShot sp : mSplays ) {
  //       TDVector v = sp.toTDVector().minus( v1 );
  //       float z = N.dot( v );
  //       if ( Math.abs( z ) < thr ) {
  //         float x = H.dot( v );
  //         float y = S.dot( v ); // downward
  //         // TODO add splay point at (x,y)
  //       }
  //     }
  //   }
  // }

  // /** compute the splay projection in the plane of the leg (V1,V2):
  //  *  |Y       V2
  //  *  |       /
  //  *  |      /_
  //  *  |     /  `- H
  //  *  |   V1
  //  *  +---------------- X
  //  * 
  //  * @param V1  first vector (E,N,Up)
  //  * @param V2  second vector
  //  */
  // private void updateSplays( TDVector v1, TDVector v2 )
  // {
  //   float dx = v2.x - v1.x;
  //   float dy = v2.y - v1.y;
  //   float dz = v2.z - v1.z;
  //   float dh = TDMath.sqrt( dx*dx + dy*dy );
  //   float dd = TDMath.sqrt( dh*dh + dz*dz );
  //   
  //   TDVector H = new TDVector( dy/dh, -dx/dh, 0 );
  //   TDVector Y = new TDVector( dx/dd,  dy/dd, dz/dd ); // unit vector along (dx,dy,dz)
  //   TDVector N = Y.cross( H );  // downward normal
  //   for ( Cave3DShot sp : mSplays ) {
  //     TDVector v = sp.toTDVector(); // (E, N, Up)
  //     float x =  H.dot( v );
  //     float y = -Y.dot( v ); // downward
  //     // TODO add splay line at (x,y)
  //   }
  // }
  
  // ==============================================================
  /** start the item picker dialog
   */
  void startItemPickerDialog()
  {
    int symbol = mSymbol;
    if ( mRecentTools == mRecentLine ) { symbol = SymbolType.LINE; }
    // else if ( mRecentTools == mRecentPoint )     { symbol = SymbolType.POINT; }
    // else if ( mRecentTools == mRecentArea ) { symbol = SymbolType.AREA; }
    startItemPickerDialog( symbol );
  }

  /** start the item picker dialog
   * @param symbol  initial symbol class that is shown in the dialog
   */
  private void startItemPickerDialog( int symbol )
  {
    new ItemPickerDialog( mActivity, this, 0, symbol ).show(); // plot type is not used
  }

  // ==============================================================

  /** lifecycle: RESUME
   */
  @Override
  protected synchronized void onResume()
  {
    super.onResume();
    doResume();
    // TDLog.TimeEnd( "drawing activity ready" );
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on resume done");
  }

  /** lifecycle: PAUSE
   */
  @Override
  protected synchronized void onPause() 
  { 
    // TDLog.v( "Drawing Activity onPause " );
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
    // TDLog.v("Drawing Activity on Start " );
    TDLocale.resetTheLocale();
    loadRecentSymbols( mApp_mData );
    setBtnRecentAll(); 
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on start done");
    setMenuAdapter( getResources() );
    closeMenu();

    // TODO set leg and splays
    mTopoGL = ((TopoDroidApp)getApplication()).mTopoGL;
    if ( mTopoGL == null ) {
      finish();
    }
    Cave3DShot leg = mTopoGL.getSketchLeg();
    if ( leg == null ) {
      finish();
    }
    makeReference( leg, true );
  }

  /** lifecycle: STOP
   */
  @Override
  protected synchronized void onStop()
  {
    super.onStop();
    // TDLog.v("Drawing Activity onStop ");
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
    TopoDroidApp.mDrawingWindow = null;
    // TDLog.Log( TDLog.LOG_PLOT, "drawing activity on destroy done");
  }

  /** lifecycle: implement RESUME
   */
  private void doResume() // restoreInstanceFromData
  {
    // TDLog.v( "doResume()" );
    // String mode = mApp_mData.getValue( "DISTOX_SKETCH_MODE" );
    // TDLog.v("restore drawing display mode " + mode );
    // mSketchSurface.setDisplayMode( DisplayMode.parseString( mode ) );
    switchZoomCtrl( TDSetting.mZoomCtrl );

    // TODO FIXME
    // SketchInfo info = mApp_mData.getSketchInfo( mSid, mName );
    // mOffset.x = info.xoffset;
    // mOffset.y = info.yoffset;
    // mZoom     = info.zoom;

    mSketchSurface.setDrawing( true );
  }

  /** lifecycle: implement PAUSE
   */
  private void doPause() // saveInstanceToData
  {
    switchZoomCtrl( 0 );
    mSketchSurface.setDrawing( false );
    // TODO FIXME
    // if ( mPid >= 0 ) {
    //   try {
    //     // TDLog.v("PLOT pause: " + mOffset.x + " " + mOffset.y + " " + zoom );
    //     mApp_mData.updateSketch( mPid, mSid, mOffset.x, mOffset.y, zoom );
    //   } catch ( IllegalStateException e ) {
    //     TDLog.Error("cannot save plot state: " + e.getMessage() );
    //   }
    // }

    // TODO FIXME
    // (new Thread() {
    //    public void run() {
    //      mApp_mData.setValue( "DISTOX_SKETCH_MODE", DisplayMode.toString( mSketchWindow.getDisplayMode() ) );
    //    }
    // } ).start();

    // TODO FIXME
    // doSaveTdr( ); // do not alert-dialog on mAllSymbols
  }

  // private void doStop()
  // {
  //   // TDLog.Log( TDLog.LOG_PLOT, "doStop modified " + Modified );
  // }

// ----------------------------------------------------------------------------

  /** start the sketch display 
   * @param do_load whether to load plot from file
   * @note called by onCreate, switchSketchType, onBackPressed 
   * 
   * FIXME null ptr in 5.1.40 on ANDROID-11 at line 2507 
   */
  private void doStart( )
  {
    if ( mApp_mData == null ) {
      TDLog.Error("DrawingWindow start with null DB");
      finish();
      return;
    }
    float zoom = mSketchSurface.getZoom();
    mOffset.x = ( TopoDroidApp.mDisplayWidth )/(2*zoom);
    mOffset.y = ( TopoDroidApp.mDisplayHeight + mListView.getHeight() )/(2*zoom);
    mSketchSurface.setTransform( this, mOffset.x, mOffset.y );
  }

  // ----------------------------------------------------

  /** select at a canvas point (x,y)
   * @param xc    X-coord (canvas)
   * @param yc    Y-coord 
   * @param size selection radius
   * @note called only onTouchUp
   */
  private void doSelectAt( float xc, float yc, float size )
  {
    // if ( mLandscape ) { float t=x; x=-y; y=t; }
    if ( mMode == MODE_EDIT ) {
      // TDLog.v("SKETCH select at " + xc, + " " + yc + " ...");
      int selection = mSketchSurface.getItemAt( xc, yc, size, mSelectStation );
      if ( selection == 4 ) {
        setButton3( BTN_SECTION, mBMsectionOk );
      } else if ( selection == 3 ) {
        setButton3( BTN_SECTION, mBMsectionOpen );
      } else if ( selection == 2 ) {
        setButton3( BTN_SECTION, mBMsectionOk );
      } else {
        setButton3( BTN_SECTION, mBMsectionNo );
      } 
      // setButton3PrevNext();
    } 
  }

  /** execute an erase action
   * @param xc    X coordinate of the erase action (canvas)
   * @param yc    Y coordinate of the erase action
   */
  private void doEraseAt( float xc, float yc )
  {
    // if ( mLandscape ) { float t=x; x=-y; y=t; }
    mSketchSurface.eraseAt( xc, yc, mEraseCommand, mEraseSize );
    modified();
  }

  /** delete a line
   * @param line   drawing line item to delete
   */
  void deleteLine( SketchLinePath line ) 
  { 
    mSketchSurface.deleteLine( line );
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
      float zoom = mSketchSurface.getZoom();
      mOffset.x += x_shift / zoom;                // add shift to offset
      mOffset.y += y_shift / zoom; 
      // TDLog.v( "PLOT shift event " + mOffset.x + " " + mOffset.y + " " + zoom );
      mSketchSurface.setTransform( this, mOffset.x, mOffset.y );
    }
  }

  /** shift the canvas
   * @param x_shift   X shift
   * @param y_shift   Y shift
   */
  private void moveCanvas( float x_shift, float y_shift )
  {
    if ( Math.abs( x_shift ) < TDSetting.mMinShift && Math.abs( y_shift ) < TDSetting.mMinShift ) {
      float zoom = mSketchSurface.getZoom();
      mOffset.x += x_shift / zoom;                // add shift to offset
      mOffset.y += y_shift / zoom; 
      // TDLog.v( "PLOT move event " + mOffset.x + " " + mOffset.y + " " + zoom );
      mSketchSurface.setTransform( this, mOffset.x, mOffset.y );
    }
  }

  /** make the zoom controls visible (if enabled)
   */
  public void checkZoomBtnsCtrl()
  {
    // if ( mZoomBtnsCtrl == null ) return; // not necessary
    if ( TDSetting.mZoomCtrl == 2 && ! mZoomBtnsCtrl.isVisible() ) {
      mZoomBtnsCtrl.setVisible( true );
    }
  }

  // -------------------------------------------------------------------------
  /** begin an erase command
   * @param xc   canvas X coord 
   * @param yc   canvas Y coord
   */
  private void startErasing( float xc, float yc )
  {
    // TDLog.v( "Erase at " + xc + " " + yc );
    if ( mTouchMode == MODE_MOVE ) {
      mEraseCommand =  new EraseCommand();
      mSketchSurface.setEraser( xc, yc, mEraseSize );
      doEraseAt( xc, yc );
    }
  }

  /** complete an erase command
   */
  private void finishErasing()
  {
    mSketchSurface.endEraser();
    if ( mEraseCommand != null && mEraseCommand.size() > 0 ) {
      mEraseCommand.completeCommand();
      mSketchSurface.addEraseCommand( mEraseCommand );
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
    // dismissPopups();
    checkZoomBtnsCtrl();

    MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
    // TDLog.Log( TDLog.LOG_INPUT, "Drawing Activity onTouch() " );
    // dumpEvent( event );

    int act = event.getAction();
    int action = act & MotionEvent.ACTION_MASK;
    int id = 0;
    boolean threePointers = false;

    if ( TDSetting.mStylusOnly ) {
      int np = event.getPointerCount();
      for ( id = 0; id < np; ++id ) {
        // TDLog.v("STYLUS tool " + id + " size " + rawEvent.getSize( id ) + " " + rawEvent.getToolMajor( id ) + " " + TDSetting.mStylusSize );
        if ( rawEvent.getToolMajor( id ) < TDSetting.mStylusSize ) {
          break;
        }
      }
      if ( id == np ) return true;
      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        action = MotionEvent.ACTION_DOWN;
      } else if ( action == MotionEvent.ACTION_POINTER_UP) {
        action = MotionEvent.ACTION_UP;
      }
    } else {
      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        threePointers = (event.getPointerCount() == 3);
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
        threePointers = (np > 3);
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

    if (action == MotionEvent.ACTION_DOWN) { // ---------------------------------------- DOWN
      return onTouchDown( x_canvas, y_canvas );

    } else if ( action == MotionEvent.ACTION_MOVE ) { // ------------------------------- MOVE
      return onTouchMove( x_canvas, y_canvas, event, threePointers );

    } else if (action == MotionEvent.ACTION_UP) { // ----------------------------------- UP
      return onTouchUp( x_canvas, y_canvas );
    }
    return true;
  }

  boolean HBXP_PointDown = false; // HBXP
  /** react to a touch-up event
   * @param xc   canvas X coord 
   * @param yc   canvas Y coord
   * @return ...
   */
  private boolean onTouchUp( float xc, float yc )
  {
    if ( onMenu ) {
      closeMenu();
      return true;
    }

    // TDLog.v( "on touch up. mode " + mMode + " " + mTouchMode );
    if ( mTouchMode == MODE_ZOOM || mTouchMode == MODE_ROTATE ) {
      mTouchMode = MODE_MOVE;
    } else {
      float x_shift = xc - mSaveX; // compute shift
      float y_shift = yc - mSaveY;
      if ( mMode == MODE_DRAW ) {
        float squared_shift = x_shift*x_shift + y_shift*y_shift;

        if ( squared_shift > TDSetting.mLineSegment2 || ( mPointCnt % mLinePointStep ) > 0 ) {
          if ( ( ! TDSetting.mStylusOnly ) || squared_shift < 10 * TDSetting.mLineSegment2 ) {
            // TDLog.v("SKETCH DRAW final point " + xc + " " + yc );
            mSketchSurface.addPointToCurrentPath( new Point2D(xc, yc) );
          }
        }
        
        mSketchSurface.endCurrentPath(); 
        ArrayList< Point2D > cpath = mSketchSurface.getCurrentPath();
        if ( cpath.size() > 1 ) {
          if ( TDSetting.isLineStyleSimplified() ) {
	    Weeder weeder = new Weeder();
            for ( Point2D lp : cpath ) weeder.addPoint( lp.x, lp.y );
	    // get pixels from meters
	    float dist = DrawingUtil.SCALE_FIX*TDSetting.mWeedDistance; // N.B. no zoom
	    float len  = mSketchSurface.getZoom() * DrawingUtil.SCALE_FIX*TDSetting.mWeedLength;
            cpath = weeder.simplify( dist, len );
          } 
          if ( cpath.size() > 1 ) {
            int id  = mSketchSurface.getSectionNextLineId();
            int sid = mSketchSurface.getSectionId();
            // TDLog.v("SKETCH new line " + id + "." + sid + " size " + cpath.size() );
            SketchLinePath lp1 = new SketchLinePath( id, sid, SketchSurface.getSectionLinePaint( mCurSection ) );
            for ( Point2D p0 : cpath ) {
              TDVector vec = mSketchSurface.toTDVector( p0.x, p0.y );
              SketchPoint pt = lp1.appendPoint( vec );
              // mSketchSurface.setPolarCoords( pt ); // POLAR
            }
            mSketchSurface.addLinePath( mCurSection, lp1 );
          }
        }
        mSketchSurface.resetPreviewPath();
        // undoBtn.setEnabled(true);
        // redoBtn.setEnabled(false);
        // canRedo = false;
        mPointerDown = false;
        modified();
      } else if ( mMode == MODE_EDIT ) {
        // TDLog.v("SKETCH touch up on edit at " + xc + " " + yc + " radius " + TDSetting.mPointingRadius );
        if ( Math.abs(mStartX - xc) < TDSetting.mPointingRadius && Math.abs(mStartY - yc) < TDSetting.mPointingRadius ) {
          doSelectAt( xc, yc, mSelectSize );
        }
        // mEditMove = false;
      } else if ( mMode == MODE_ERASE ) {
	finishErasing();
      }
    }
    return true;
  }

  /** react to a touch-down event
   * @param xc   canvas X coord 
   * @param yc   canvas Y coord
   * @return ...
   */
  private boolean onTouchDown( float xc, float yc )
  {
    HBXP_PointDown = false; // HBXP
    mSketchSurface.endEraser();
    float d0 = TDSetting.mCloseCutoff + mSelectSize / mSketchSurface.getZoom();
    // TDLog.v( "on touch down. mode " + mMode + " " + mTouchMode );

    // TDLog.v( "DOWN at X " + xc + " [" +TopoDroidApp.mBorderInnerLeft + " " + TopoDroidApp.mBorderInnerRight + "] Y " + yc + " [" + TopoDroidApp.mBorderTop + " " + TopoDroidApp.mBorderBottom + "]" );

    // float bottom = TopoDroidApp.mBorderBottom - mZoomTranslate;
    // if ( mMode == MODE_DRAW ) bottom += ZOOM_TRANSLATION;

    if ( yc > TopoDroidApp.mBorderBottom ) {
      if ( mZoomBtnsCtrlOn && xc > TopoDroidApp.mBorderInnerLeft && xc < TopoDroidApp.mBorderInnerRight ) {
        mTouchMode = MODE_ZOOM;
        mZoomBtnsCtrl.setVisible( true );
        // mZoomCtrl.show( );
      } else if ( TDSetting.mSideDrag && ( xc > TopoDroidApp.mBorderRight || xc < TopoDroidApp.mBorderLeft ) ) {
        mMode = MODE_MOVE;
        mTouchMode = MODE_ROTATE;
      }
    } else if ( TDSetting.mSideDrag && (yc < TopoDroidApp.mBorderTop) && ( xc > TopoDroidApp.mBorderRight || xc < TopoDroidApp.mBorderLeft ) ) {
      mMode = MODE_MOVE;
      mTouchMode = MODE_ROTATE;
    }

    if ( mMode == MODE_DRAW ) {
      // TDLog.Log( TDLog.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
      mPointCnt = 0;
      mSketchSurface.startCurrentPath();
      // TDLog.v("SKETCH DRAW start point " + xc + " " + yc );
      mSketchSurface.addPointToCurrentPath( new Point2D( xc, yc ) );
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
      startErasing( xc, yc );
    } else if ( mMode == MODE_EDIT ) {
      mStartX = xc;
      mStartY = yc;
      // mEditMove = true;
      // TDLog.v("SKETCH touch down on edit at " + xc + " " + yc );
      // doSelectAt( xc, yc, mSelectSize );
      mSaveX = xc;
      mSaveY = yc;
      // return false;

    //   mSaveX = xc; // FIXME-000
    //   mSaveY = yc;
    //   // return false;
    }
    return true;
  }

  /** react to a touch-move event
   * @param xc   canvas X coord 
   * @param yc   canvas Y coord
   * @param event motion event
   * @param threePointers whether the event has three pointers
   * @return ...
   */
  private boolean onTouchMove( float xc, float yc, MotionEventWrap event, boolean threePointers )
  {
    // TDLog.v( "action MOVE mode " + mMode + " touch-mode " + mTouchMode);
    if ( mTouchMode == MODE_MOVE) {
      float x_shift = xc - mSaveX; // compute shift
      float y_shift = yc - mSaveY;
      boolean save = true; // FIXME-000
      // mSaveX = xc; 
      // mSaveY = yc;
      if ( mMode == MODE_DRAW ) {
        float squared_shift = x_shift*x_shift + y_shift*y_shift;
        if ( TDSetting.mStylusOnly ) {
          if ( squared_shift > 10 * TDSetting.mLineSegment2 ) return false;
        }
        if ( squared_shift > TDSetting.mLineSegment2 ) {
          if ( ++mPointCnt % mLinePointStep == 0 ) {
            // TDLog.v("SKETCH DRAW add point " + xc + " " + yc );
            mSketchSurface.addPointToCurrentPath( new Point2D( xc, yc ) );
          }
        } else {
          save = false;
        }
      } else if ( mMode == MODE_MOVE ) {
        moveCanvas( x_shift, y_shift );
      } else if ( mMode == MODE_ERASE ) {
        if ( mEraseCommand != null ) {
          mSketchSurface.setEraser( xc, yc, mEraseSize );
          doEraseAt( xc, yc );
	}
      }
      if ( save ) { // FIXME-000
        mSaveX = xc; 
        mSaveY = yc;
      }
    } else if ( mTouchMode == MODE_ROTATE ) {
      mSketchSurface.changeAlpha( ((yc > mSaveY)? +1 : -1), ((xc > mSaveX)? +1 : -1) );
      setTheTitle();
      mSaveX = xc;
      mSaveY = yc;
    } else { // mTouchMode == MODE_ZOOM
      float newDist = spacing( event );
      float factor = ( newDist > 32.0f && oldDist > 32.0f )? newDist/oldDist : 0 ;
      changeZoom( factor );
      oldDist = newDist;
      shiftByEvent( event );
    }
    return true;
  }

  // -------------------------------- DELETE ------------------------

  /** implement the plot delete
   */
  private void doDelete()
  {
    TDLog.v("TODO doDelete");
    finish();
  }

  /** delete confirmation dialog
   */
  private void askDelete()
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
    if ( mMode == mode ) return;

    if ( mMode == MODE_DRAW ) {  // this has annoying glitches 
      mZoomView.setTranslationY( 0 );
      // mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
    } else if ( mode == MODE_DRAW ) {
      mZoomView.setTranslationY( ZOOM_TRANSLATION );
      // mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
    }

    mMode = mode;
    switch ( mMode ) {
      case MODE_MOVE:
        // clearHotPath( View.INVISIBLE );
        mSketchSurface.setDisplayPoints( false );
        mListView.setAdapter( mButtonView1.mAdapter );
        mListView.invalidate();
        break;
      case MODE_DRAW:
        // clearHotPath( View.VISIBLE );
        mSketchSurface.setDisplayPoints( false );
        mListView.setAdapter( mButtonView2.mAdapter );
        mListView.invalidate();
        break;
      case MODE_ERASE:
        // clearHotPath( View.INVISIBLE );
        mSketchSurface.setDisplayPoints( false );
        mListView.setAdapter( mButtonView5.mAdapter );
        mListView.invalidate();
        break;
      case MODE_EDIT:
        clearSelected();
        // clearHotPath( View.INVISIBLE );
        mSketchSurface.setDisplayPoints( true );
        mListView.setAdapter( mButtonView3.mAdapter );
        mListView.invalidate();
        break;
      default:
        break;
    }
    setTheTitle();
  }

  // -----------------------------------------------------------------------------------------

  /** clear the selection of item/point
   */
  private void clearSelected()
  {
    mHasSelected = false;
    mSketchSurface.clearSelected();
    mMode = MODE_EDIT;
    // setButton3PrevNext();
    setButton3Item( null );
  }

  /** react to a user tap
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
    // int dismiss = dismissPopups();

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
         ( b == mButton3[2] && ( mMode == MODE_EDIT /* || mMode == MODE_SHIFT */ ) ) ) { 
      if ( mCurSection > 0 ) {
        mCurSection = mSketchSurface.closeSection();

      }
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
      if ( k2 < NR_BUTTON2 && b == mButton2[k2++] ) { // UNDO
        mSketchSurface.undo(); // TODO return value unused
        // if ( ! mSketchSurface.hasMoreUndo() ) {
        //   // undoBtn.setEnabled( false );
        // }
        // redoBtn.setEnabled( true );
        // canRedo = true;/
        modified();
      } else if ( k2 < NR_BUTTON2 && b == mButton2[k2++] ) { // REDO
        if ( mSketchSurface.hasMoreRedo() ) {
          mSketchSurface.redo(); // TODO return value unused
        }
      // } else if ( k2 < NR_BUTTON2 && b == mButton2[k2++] ) { // TOOLS
      //   if ( ! TDSetting.mTripleToolbar ) {
      //     rotateRecentToolset();
      //   } else {
      //     new ItemPickerDialog(mActivity, this, 0, mSymbol ).show();
      //   }
      }

    } else if ( mMode == MODE_ERASE ) {
      int k5 = 3;
      if ( k5 < NR_BUTTON5 && b == mButton5[k5++] ) { // UNDO same as in mButton2[]
        mSketchSurface.undo();
        // if ( ! mSketchSurface.hasMoreUndo() ) {
        //   // undoBtn.setEnabled( false );
        // }
        // redoBtn.setEnabled( true );
        // canRedo = true;/
        modified();
      } else if ( k5 < NR_BUTTON5 && b == mButton5[k5++] ) { // REDO same as in mButton2[]
        if ( mSketchSurface.hasMoreRedo() ) {
          mSketchSurface.redo();
        }
      } else if ( k5 < NR_BUTTON5 && b == mButton5[k5++] ) { // ERASE SIZE
        setButtonEraseSize( mEraseScale + 1 ); // toggle erase size
      }

    } else if ( mMode == MODE_EDIT /* || mMode == MODE_SHIFT */ ) {
      int k3 = 3;
      // if ( k3 < NR_BUTTON3 && b == mButton3[k3++] ) { // PREV
      //   TDLog.v("TODO item previous");
      // } else if ( k3 < NR_BUTTON3 && b == mButton3[k3++] ) { // NEXT
      //   TDLog.v("TODO item next");
      // } else 
      if ( k3 < NR_BUTTON3 && b == mButton3[k3++] ) { // SELECT MODE
        setSelectMode( ! mSelectStation );
      } else if ( k3 < NR_BUTTON3 && b == mButton3[k3++] ) { // SELECT SIZE
        setButtonSelectSize( mSelectScale + 1 ); // toggle select size
      } else if ( k3 < NR_BUTTON3 && b == mButton3[k3++] ) { // SELECT ITEM DELETE
        TDLog.v("TODO item delete");
        // askDeleteItem( p, t, "section");
      } else if ( k3 < NR_BUTTON3 && b == mButton3[k3++] ) { // SECTION
        switch ( mSketchSurface.hasSelected() ) {
          case SketchCommandManager.SELECT_POINTS:
            SketchPoint[] pts = mSketchSurface.getSelectedPoints();
            ++ mMaxSection;
            SketchSection section = new SketchSection( mMaxSection, pts[0], pts[1], mVertical );
            mSketchSurface.addSection( section );
            openSection( section );
            // TDLog.v("SKETCH open section " + mCurSection + " " + mMaxSection );
            break;
          case SketchCommandManager.SELECT_STATION: // SWITCH LEG_VIEW
            SketchStationPath station = mSketchSurface.getSelectedStation();
            // TDLog.v("SKETCH open new leg " + station.from() + " " + station.fullname() + " " + station.isForward() );
            station.dump( "open leg" );
            Cave3DShot sketchLeg = null;
            if ( station.isForward() ) {
              sketchLeg = mTopoGL.setSketchLeg( station.from(), station.fullname() );
            } else { 
              sketchLeg = mTopoGL.setSketchLeg( station.fullname(), station.from() );
            }
            if ( sketchLeg != null ) {
              // TDLog.v("SKETHC switch ...");
              String filename = TDPath.getC3dFile( mSketchName );
              doSave( filename, mSketchName );
              makeReference( sketchLeg, true );
              resetStatus();
            }
            break;
          case SketchCommandManager.SELECT_SECTION: 
            openSection( mSketchSurface.getSelectedSection() );
            break;
          case SketchCommandManager.SELECT_OFF:
            tryCloseSection();
            // TDLog.v("SKETCH close section - current " + mCurSection );
            break;
        }
      } else if ( k3 < NR_BUTTON3 && b == mButton3[k3++] ) { // CLEAR SELECTION
        mSketchSurface.clearSelected();
        setButton3( BTN_SECTION, mBMsectionNo );
      }

    } else {
      int k1 = 3;
      if ( k1 < NR_BUTTON1 && b == mButton1[k1++] ) { // DISPLAY MODE 
        TDLog.v("TODO display mode");
        new SketchModeDialog( mActivity, this, mSketchSurface ).show();
      } else if ( k1 < NR_BUTTON1 && b == mButton1[k1++] ) { //  NOTE
        long mSid = TDInstance.sid;
        if ( mSid >= 0 ) {
          (new DialogAnnotations( mActivity, mApp_mData.getSurveyFromId(mSid) )).show();
        }
      } else if ( k1 < NR_BUTTON1 && b == mButton1[k1++] ) { //  WALLS
        if ( mU != null ) mSketchSurface.makeWall( mU );
      } else if ( TDLevel.overNormal && k1 < NR_BUTTON1 && b == mButton1[k1++] ) { //  REFRESH
        TDLog.v("TODO updateDisplay(); ");
      }
    }
  }

  private void openSection( SketchSection section )
  {
    if ( section == null ) return;
    mCurSection = mSketchSurface.openSection( section );
    setButton3( BTN_SELECT, mBMback );
    resetModified();
  }

  private boolean tryCloseSection()
  {
    if ( mCurSection > 0 ) {
      mCurSection = mSketchSurface.closeSection();
      setButton3( BTN_SELECT, mBMselect );
      resetModified();
      return true;
    }
    return false;
  }

  private void setSelectMode( boolean select_station )
  {
    mSelectStation = select_station;
    setButton3( BTN_SELECT_MODE, ( mSelectStation? mBMselectStation : mBMselectLine ) );
  }

  /** ask confirmation to delete an item
   * @param p    item to delete
   * @param t    ... (not used)
   * @param name item name
   */
  private void askDeleteItem( final SketchPath p, final int t, final String name )
  {
    TopoDroidAlertDialog.makeAlert( mActivity, getResources(), String.format( getResources().getString( R.string.item_delete ), name ), 
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          // if ( p instanceof DrawingPointPath ) {
          //   deletePoint( (DrawingPointPath)p );
          // } else
          if ( p instanceof SketchLinePath ) {
            deleteLine( (SketchLinePath)p );
          // } else if ( p instanceof DrawingAreaPath ) {
          //   deleteArea( (DrawingAreaPath)p );
          }
        }
      }
    );
  }

  // --------------------------------------------------------------------------

  // /** refresh the display
  //  * @param nr    error code (if negative), OK if positive
  //  * @param toast whether to toast a message
  //  */
  // public void refreshDisplay( int nr, boolean toast )
  // {
  //   mActivity.setTitleColor( TDColor.TITLE_NORMAL );
  //   TDLog.v("TODO refreshDisplay");
  // }

  /** fit the view to the sketch
   * @param b    fitting rectangle
   */
  private void zoomFit( RectF b )
  {
    float tb = (b.top + b.bottom)/2;
    float lr = (b.left + b.right)/2;
    float w = b.right - b.left;
    float h = b.bottom - b.top;
    float wZoom = (float) ( mSketchSurface.getMeasuredWidth() * 0.9 ) / ( 1 + w*w );
    float hZoom = (float) ( ( ( mSketchSurface.getMeasuredHeight() - mListView.getHeight() ) * 0.9 ) / ( 1 + h*h ));
    float zoom = Math.min(hZoom, wZoom);
    // TDLog.v("SKETCH w " + w + " h " + h + " wZoom " + wZoom + " hZoom " + hZoom );
    if ( zoom < 0.1f ) zoom = 0.1f;
    mOffset.x = ( TopoDroidApp.mDisplayWidth )/(2*zoom) - lr;
    mOffset.y = ( TopoDroidApp.mDisplayHeight + mListView.getHeight() )/(2*zoom) - tb;
    mSketchSurface.setTransform( this, mOffset.x, mOffset.y, zoom );
    // TDLog.v( "SKETCH display " + TopoDroidApp.mDisplayWidth + " " + TopoDroidApp.mDisplayHeight );
    // TDLog.v( "SKETCH zoom fit W " + w + " H " + h + " zoom " + mSketchSurface.getZoom() + " X " + mOffset.x + " Y " + mOffset.y );
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
      case KeyEvent.KEYCODE_VOLUME_DOWN: // (25)
      default:
        TDLog.Error( "key down: code " + code );
    }
    return false;
  }

  // ----------------------------- MENU ----------------------------

  private static final int mNrMenus = 7;

  private static final int[] menus = {
                        R.string.menu_close,      // 0
                        R.string.menu_export,     // 1
                        R.string.menu_zoom_fit,
                        R.string.menu_sections,
                        R.string.menu_delete,
                        R.string.menu_options,
                        R.string.menu_help,
  };

  private static final int[] help_menus = {
                        R.string.help_plot_close,
                        R.string.help_save_plot,
                        R.string.help_zoom_fit,
                        R.string.help_section_type,
                        R.string.help_plot_delete,
                        R.string.help_prefs,
                        R.string.help_help
  };

  /** initialize the menu list
   * @param res      app resources
   */
  private void setMenuAdapter( Resources res )
  {
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<>(mActivity, R.layout.menu );
    for ( int k = 0; k < mNrMenus; ++k ) {
      menu_adapter.add( res.getString( menus[k] ) ); 
    }
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

  private void handleMenu( int pos )
  {
    closeMenu();
    int p = 0;
    if ( p++ == pos ) { // CLOSE SECTION
      if ( tryCloseSection() ) return;
      super.onBackPressed();
    } else if ( p++ == pos ) { // EXPORT
      String filename = TDPath.getC3dFile( mSketchName );
      doSave( filename, mSketchName );
    } else if ( p++ == pos ) { // ZOOM-FIT 
      doZoomFit();
    } else if ( p++ == pos ) { // SECTIONS TYPE
      mVertical = ! mVertical;
      setTheTitle();
    } else if ( p++ == pos ) { // DELETE
      askDelete();
    } else if ( p++ == pos ) { // OPTIONS
      TDLog.v("TODO menu OPTIONS");
      // updateReference();
      // Intent intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
      // intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_PLOT );
      // mActivity.startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      // 1 for select-tool
      // int nn = 1 + NR_BUTTON1 + NR_BUTTON2 - 3 + NR_BUTTON5 - 5 + ( TDLevel.overBasic? NR_BUTTON3 - 3: 0 );
      // TDLog.v( "Help menu, nn " + nn );
      switch ( mMode ) {
        case MODE_DRAW:
          new HelpDialog(mActivity, izons_draw, menus, help_icons_draw, help_menus, NR_BUTTON2, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
          break;
        case MODE_ERASE:
          new HelpDialog(mActivity, izons_erase, menus, help_icons_erase, help_menus, NR_BUTTON5, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
          break;
        case MODE_EDIT:
          new HelpDialog(mActivity, izons_select, menus, help_icons_select, help_menus, NR_BUTTON3, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
          break;
        default: // MODE_MOVE
          new HelpDialog(mActivity, izons_move, menus, help_icons_move, help_menus, NR_BUTTON1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
      }
    }
  }


  // ------------------------------- ZOOM FIT -----------------------------

  /** change the zoom to fit the drawing to the screen
   */
  private void doZoomFit()
  {
    // if ( mSketchSurface.isFixedZoom() ) return;
    // FIXME FIXED_ZOOM for big sketches this leaves out some bits at the ends
    // maybe should increase the bitmap bounds by a small factor ...
    // TDLog.v("SKETCH doZoomFit");
    RectF b = mSketchSurface.getBitmapBounds( 1.0f );
    zoomFit( b );
  }


  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) { // MENU
      handleMenu( pos );
    }
  }

  // --------------------------------------------------------------------------

  /** react to the result of a child activity
   * @param reqCode    request code
   * @param resCode    result code
   * @param intent     returned intent (and data)
   */
  @Override
  protected void onActivityResult( int reqCode, int resCode, Intent intent )
  {
    TDLog.v("TODO onActivityResult request " + reqCode + " result " + resCode );
  }

  // ------------------------------ TOOLSET -------------------------
  // /** rotate the toolset in the bar of the recent tools
  //  */
  // void rotateRecentToolset( )
  // { 
  //   // TDLog.v("rotate recent toolset");
  //   if ( mRecentToolsForward ) {
  //     if ( mRecentTools == mRecentPoint ) {
  //       mRecentTools = mRecentLine;
  //       mRecentDimX  = 
  //       mRecentDimY  = Float.parseFloat( getResources().getString( R.string.dimxp ) );
  //     } else if ( mRecentTools == mRecentLine ) {
  //       mRecentTools = mRecentArea;
  //       mRecentDimX  = Float.parseFloat( getResources().getString( R.string.dimxl ) );
  //       mRecentDimY  = Float.parseFloat( getResources().getString( R.string.dimyl ) );
  //     } else if ( mRecentTools == mRecentArea ) {
  //       mRecentTools = mRecentPoint;
  //       // mRecentDimX  = Float.parseFloat( getResources().getString( R.string.dimxl ) );
  //       // mRecentDimY  = Float.parseFloat( getResources().getString( R.string.dimyl ) );
  //     }
  //   } else {
  //     if ( mRecentTools == mRecentPoint ) {
  //       mRecentTools = mRecentArea;
  //       // mRecentDimX  = Float.parseFloat( getResources().getString( R.string.dimxl ) );
  //       // mRecentDimY  = Float.parseFloat( getResources().getString( R.string.dimyl ) );
  //     } else if ( mRecentTools == mRecentLine ) {
  //       mRecentTools = mRecentPoint;
  //       mRecentDimX  = Float.parseFloat( getResources().getString( R.string.dimxl ) );
  //       mRecentDimY  = Float.parseFloat( getResources().getString( R.string.dimyl ) );
  //     } else if ( mRecentTools == mRecentArea ) {
  //       mRecentTools = mRecentLine;
  //       mRecentDimX  = 
  //       mRecentDimY  = Float.parseFloat( getResources().getString( R.string.dimxp ) );
  //     }
  //   }
  //   // setBtnRecent( Symbol.??? );
  //   setToolsToolbars();
  // }

  /** switch to the current toolbar
   * @note called by rotateRecentToolset() setBtnRecent() setBtnRecentAll()
   */
  private void setToolsToolbars()
  {
    // TDLog.v("set tools toolbars - visible ");
    int k = -1;
    mZoomView.setTranslationY( ZOOM_TRANSLATION );
    // if ( mRecentTools == mRecentPoint ) {
    //   mLayoutToolsP.setVisibility( View.VISIBLE );
    //   mLayoutToolsL.setVisibility( View.GONE );
    //   mLayoutToolsA.setVisibility( View.GONE );
    //   mLayoutScale.setVisibility( View.GONE );
    //   k = getCurrentPointIndex();
    //   pointSelected( mCurrentPoint, false );
    //   setHighlight( SymbolType.POINT, k );
    //   setButton2( BTN_TOOL, mBMtoolsPoint );
    // } else if ( mRecentTools == mRecentLine ) {
    //   mLayoutToolsP.setVisibility( View.GONE );
    //   mLayoutToolsL.setVisibility( View.VISIBLE );
    //   mLayoutToolsA.setVisibility( View.GONE );
    //   mLayoutScale.setVisibility( View.GONE );
      k = getCurrentLineIndex();
      // TDLog.v("Set tools toolbars: Current line index " + k );
      lineSelected( mCurrentLine, false );
      setHighlight( SymbolType.LINE, k );
    //   setButton2( BTN_TOOL, mBMtoolsLine );
    // } else {
    //   mLayoutToolsP.setVisibility( View.GONE );
    //   mLayoutToolsL.setVisibility( View.GONE );
    //   mLayoutToolsA.setVisibility( View.VISIBLE );
    //   mLayoutScale.setVisibility( View.GONE );
    //   k = getCurrentAreaIndex();
    //   areaSelected( mCurrentArea, false );
    //   setHighlight( SymbolType.AREA, k );
    //   setButton2( BTN_TOOL, mBMtoolsArea );
    // }
    mLayoutTools.invalidate();
  }

  // /**
  //  * @param path  selected (point) path, or null
  //  */
  // private void setScaleToolbar( DrawingPointPath path )
  // {
  //   if ( path != null ) {
  //     int progress = 20 + 35 * ( 2 + path.getScale() );
  //     mScaleBar.setProgress( progress );
  //     // TDLog.v("set scale bar - progress " + progress + " scale " + path.getScale() + " visible " );
  //     mLayoutTools.setVisibility( View.VISIBLE );
  //     mLayoutToolsP.setVisibility( View.GONE );
  //     mLayoutToolsL.setVisibility( View.GONE );
  //     mLayoutToolsA.setVisibility( View.GONE );
  //     mLayoutScale.setVisibility( View.VISIBLE );
  //   } else {
  //     // TDLog.v("set scale bar - invisible " );
  //     mLayoutTools.setVisibility( View.INVISIBLE );
  //   }
  // }

  // /** set the hot-item point scale
  //  * @param progress  scalebar progress value
  //  */
  // void setPointScaleProgress( int progress )
  // {
  //   if ( mHotPath == null ) return;
  //   if ( mHotPath instanceof DrawingPointPath ) {
  //     int scale = (int)(progress / 40) - 2;
  //     ((DrawingPointPath)mHotPath).setScale( scale );
  //     // TDLog.v("set point scale " + progress + " scale " + scale );
  //   }
  // }

  // void setPointScale( int scale )
  // {
  //   if ( scale >= PointScale.SCALE_XS && scale <= PointScale.SCALE_XL ) {
  //     mPointScale = scale;
  //   }
  // }

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
      // case SymbolType.POINT: 
      //   mRecentTools = mRecentPoint;
      //   mSymbol = symbol;
      //   setButtonRecent( mBtnRecentP, mRecentPoint );
      //   index = getCurrentPointIndex();
      //   break;
      case SymbolType.LINE: 
        mRecentTools = mRecentLine;
        mSymbol = symbol;
        setButtonRecent( mBtnRecentL, mRecentLine  );
        index = getCurrentLineIndex();
        // TDLog.v("set btn recent line: current " + mCurrentLine + " index " + index );
        break;
      // case SymbolType.AREA: 
      //   mRecentTools = mRecentArea;
      //   mSymbol = symbol;
      //   setButtonRecent( mBtnRecentA, mRecentArea  );
      //   index = getCurrentAreaIndex();
      //   break;
    }
    setToolsToolbars();
    // setHighlight( symbol, index ); // already done in setToolsToolbars
  }

  // /** get the index of the current point tool
  //  * @return index of the current point tool
  //  */
  // private int getCurrentPointIndex()
  // {
  //   for ( int k=0; k < NR_RECENT; ++k ) {
  //     if ( mCurrentPoint == BrushManager.getPointIndex( mRecentPoint[k] ) ) return k;
  //   }
  //   return -1;
  // }

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

  // /** get the index of the current area tool
  //  * @return index of the current area tool
  //  */
  // private int getCurrentAreaIndex()
  // {
  //   for ( int k=0; k < NR_RECENT; ++k ) {
  //     if ( mCurrentArea == BrushManager.getAreaIndex( mRecentArea[k] ) ) return k;
  //   }
  //   return -1;
  // }


  /** set the recent buttons of all the symbol classes
   */
  private void setBtnRecentAll()
  {
    // TDLog.v("set btn recent all" );
    // setButtonRecent( mBtnRecentP, mRecentPoint );
    setButtonRecent( mBtnRecentL, mRecentLine  );
    // setButtonRecent( mBtnRecentA, mRecentArea  );

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
      buttons[kk].resetPaintPath( p.getPaint(), p.getPath(), mRecentDimX, mRecentDimY );
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
        // case SymbolType.POINT:
        //   mBtnRecentP[ highlightIndex ].highlight( false );
        //   break;
        case SymbolType.LINE:
          mBtnRecentL[ highlightIndex ].highlight( false );
          break;
        // case SymbolType.AREA:
        //   mBtnRecentA[ highlightIndex ].highlight( false );
        //   break;
      }
    }
    if ( index < 0 || index >= NR_RECENT ) {
      highlightIndex = -1;
      highlightType  = SymbolType.UNDEF;
    } else {
      highlightIndex = index;
      highlightType  = type;
      switch ( highlightType ) {
        // case SymbolType.POINT:
        //   mBtnRecentP[ highlightIndex ].highlight( true );
        //   break;
        case SymbolType.LINE:
          mBtnRecentL[ highlightIndex ].highlight( true );
          break;
        // case SymbolType.AREA:
        //   mBtnRecentA[ highlightIndex ].highlight( true );
        //   break;
      }
    }
  }
   
  // /** set the current point symbol
  //  * @param k    index in the recent point array
  //  * @param update_recent whether to update the array of recent symbols
  //  */
  // // @Override
  // private boolean setCurrentPoint( int k, boolean update_recent )
  // {
  //   int index = BrushManager.getPointIndex( mRecentPoint[k] );
  //   if ( index < 0 ) return false;
  //   if ( forbidPointSection( index ) ) return false;
  //   mCurrentPoint = index;
  //   pointSelected( index, update_recent );
  //   updateAge( k, mRecentPointAge );
  //   return true;
  // }

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
    // TDLog.v("AGE set line " + k + " update " + update_recent + " current " + current );
    mCurrentLine = current;
    lineSelected( current, update_recent );
    updateAge( k, mRecentLineAge );
    return true;
  }

  // /** set the current area symbol
  //  * @param k    index in the recent area array
  //  * @param update_recent whether to update the array of recent symbols
  //  */
  // // @Override
  // private boolean setCurrentArea( int k, boolean update_recent )
  // {
  //   int index = BrushManager.getAreaIndex( mRecentArea[k] );
  //   if ( index < 0 ) return false;
  //   mCurrentArea = index;
  //   areaSelected( index, update_recent );
  //   updateAge( k, mRecentAreaAge );
  //   return true;
  // }

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

}
