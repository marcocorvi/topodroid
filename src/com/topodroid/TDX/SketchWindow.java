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
import android.graphics.Paint.FontMetrics;
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
                        R.drawable.iz_refresh,
                        R.drawable.iz_undo,          // 6
                        R.drawable.iz_redo,          // 7
                        R.drawable.iz_small,         // 8
                        R.drawable.iz_medium,        // 9
                        R.drawable.iz_large,         // 10
                        R.drawable.iz_back,          // 11
                        R.drawable.iz_forw,          // 12
                        R.drawable.iz_delete,        // 13
                        R.drawable.iz_delete_off,
                        R.drawable.iz_section_ok,    // 15
                        R.drawable.iz_section_no,    // 16
                        R.drawable.iz_menu,
  };
  private static final int IC_SMALL      = 8;
  private static final int IC_MEDIUM     = 9;
  private static final int IC_LARGE      = 10;
  private static final int IC_PREV       = 11;
  private static final int IC_NEXT       = 12;
  private static final int IC_DELETE_OK  = 13;
  private static final int IC_DELETE_NO  = 14;
  private static final int IC_SECTION_OK = 15;
  private static final int IC_SECTION_NO = 16;
  private static final int IC_MENU       = 17;


  private static final int NR_BUTTON1 = 6;
  private static final int NR_BUTTON2 = 5;
  private static final int NR_BUTTON3 = 7;
  private static final int NR_BUTTON5 = 6;
  private static final int mNrMove = 6;
  private static final int[] izons_move = {
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,
                        R.drawable.iz_mode,          // 3
                        R.drawable.iz_note,          // 4
                        R.drawable.iz_refresh
  };
  private static final int[] help_icons_move = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_refs,
                        R.string.help_note,
                        R.string.help_refresh
  };

  private static final int mNrDraw = 5;
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

  private static final int mNrEdit = 7;
  private static final int[] izons_edit = {
                        R.string.help_edit,
                        R.drawable.iz_eraser,
                        R.drawable.iz_select_ok,     // 2
                        R.drawable.iz_back,          // 
                        R.drawable.iz_forw,
                        R.drawable.iz_medium,
                        R.drawable.iz_delete_off,
                        R.drawable.iz_section_no,
  };
  private static final int[] help_icons_edit = {
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_previous,
                        R.string.help_next,
                        R.string.help_select_size,
                        R.string.help_delete_item,
                        R.string.help_section_item
  };
  private static final int BTN_SELECT_PREV = 3;
  private static final int BTN_SELECT_NEXT = 4;
  private static final int BTN_SELECT_SIZE = 5;
  private static final int BTN_REMOVE      = 6;
  private static final int BTN_SECTION     = 7;

  private static final int mNrErase = 6;
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
  private Activity mActivity = null;
  private boolean mVertical = true;  // whether sections are vertical (or horizontal)

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
  private SketchPoint[] mSelection = new SketchPoint[2];

  private int mEraseScale  = 0;
  private int mSelectScale = 0;

  private float mEraseSize  = TDSetting.mEraseness;
  private float mSelectSize = TDSetting.mSelectness;

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
  // static final int MODE_ROTATE = 7; // selected point rotate

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
  private float mZoom  = 1.0f;
  private Point2D mOffset = new Point2D();

  // ----------------------------------------------------------------
  // BUTTONS and MENU

  // private Button mButtonHelp;
  private int mButtonSize;
  private Button[] mButton1; // primary
  private Button[] mButton2; // draw
  private Button[] mButton3; // edit
  private Button[] mButton5; // eraser
  private int mNrButton1 = NR_BUTTON1;
  private int mNrButton2 = NR_BUTTON2;
  private int mNrButton3 = NR_BUTTON3;
  private int mNrButton5 = NR_BUTTON5; // erase
  private MyHorizontalButtonView mButtonView1;
  private MyHorizontalButtonView mButtonView2;
  private MyHorizontalButtonView mButtonView3;
  private MyHorizontalButtonView mButtonView5;

  private BitmapDrawable mBMjoin;
  private BitmapDrawable mBMjoin_no;
  private BitmapDrawable mBMedit_item = null;
  private BitmapDrawable mBMedit_box  = null;
  private BitmapDrawable mBMedit_ok   = null;
  private BitmapDrawable mBMedit_no   = null;
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
  private BitmapDrawable mBMprev;
  private BitmapDrawable mBMnext;
  private BitmapDrawable mBMsectionOk;
  private BitmapDrawable mBMsectionNo;

  private MyHorizontalListView mListView;
  private ListView   mMenu;
  private Button     mMenuImage;
  private boolean    onMenu;

  private int mNrSaveTh2Task = 0; // current number of save tasks

  private void setButton1( int k, BitmapDrawable bm ) { if ( k < mNrButton1 ) TDandroid.setButtonBackground( mButton1[ k ], bm ); }
  private void setButton2( int k, BitmapDrawable bm ) { if ( k < mNrButton2 ) TDandroid.setButtonBackground( mButton2[ k ], bm ); }
  private void setButton3( int k, BitmapDrawable bm ) { if ( k < mNrButton3 ) TDandroid.setButtonBackground( mButton3[ k ], bm ); }
  private void setButton5( int k, BitmapDrawable bm ) { if ( k < mNrButton5 ) TDandroid.setButtonBackground( mButton5[ k ], bm ); }

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
  public float zoom() { return mZoom; }

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
    float zoom = mZoom;
    mZoom     *= f;
    // TDLog.v( "zoom " + mZoom );
    // mOffset.x -= mDisplayCenter.x*(1/zoom-1/mZoom);
    // mOffset.y -= mDisplayCenter.y*(1/zoom-1/mZoom);
    adjustOffset( zoom, mZoom );
    // TDLog.v( "change zoom " + mOffset.x + " " + mOffset.y + " " + mZoom );
    // mSketchSurface.setZoom( this, mOffset.x, mOffset.y, mZoom );
    mSketchSurface.setTransform( this, mOffset.x, mOffset.y, mZoom );

    // mZoomCtrl.hide();
    // if ( mZoomBtnsCtrlOn ) mZoomBtnsCtrl.setVisible( false );
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

  private void makeReference()
  {
    TDLog.v("make reference");
    TopoGL topoGL = ((TopoDroidApp)getApplication()).mTopoGL;
    if ( topoGL == null ) {
      finish();
    }
    Cave3DShot leg = topoGL.mSketchLeg;
    if ( leg == null ) {
      finish();
    }
    TDVector v1 = new TDVector();
    TDVector v2 = leg.toTDVector(); // (E,N,Up)
    addFixedLeg( v1, v2 );
    for ( Cave3DShot sp : topoGL.mSketchSplaysFrom ) {
      TDVector v = sp.toTDVector(); // .plus( v1 );
      addFixedSplay( v1, v );
    }
    for ( Cave3DShot sp : topoGL.mSketchSplaysTo ) {
      TDVector v = sp.toTDVector().plus( v2 );
      addFixedSplay( v2, v );
    }
    // TODO make the grid

    mSketchSurface.doneReference();
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

  /** used to add legs and splays
   * @param v1    leg first endpoint (E,N,Up)
   * @param v2    leg second endpoint (E,N,Up)
   */
  private SketchPath addFixedLeg( TDVector v1, TDVector v2 )
  {
    SketchFixedPath path = new SketchFixedPath( SketchPath.SKETCH_PATH_LEG, BrushManager.fixedShotPaint, v1, v2 );
    mSketchSurface.addFixedLegPath( path );
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
    // TDLog.v( "PLOT config changed " + mOffset.x + " " + mOffset.y + " " + mZoom + " orientation " + new_cfg.orientation );
    TDLocale.resetTheLocale();
    mSketchSurface.setTransform( this, mOffset.x, mOffset.y, mZoom );
    // setMenuAdapter( getResources() );
  }

  /** set the title of the window
   */
  @Override
  public void setTheTitle()
  {
    StringBuilder sb = new StringBuilder();
    // sb.append(mName);
    // sb.append(": ");
    
    Resources res = getResources();
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
    String title = getResources().getString( R.string.title_edit );
    mActivity.setTitle( title );
    setButton3( BTN_REMOVE, (deletable ? mBMdeleteOn : mBMdeleteOff) );
  }

  /** set the button3 to display "prev/next" 
   */
  private void setButton3PrevNext( )
  {
    if ( mHasSelected ) {
      setButton3( BTN_SELECT_PREV, mBMprev );
      setButton3( BTN_SELECT_NEXT, mBMnext );
    } else {
      setButtonSelectSize( mSelectScale );
    }
  }

  // /** set the button "continue"
  //  * @param continue_line    type of line-continuation
  //  * @note must be called only if TDLevel.overNormal
  //  */
  // private void setButtonContinue( int continue_line )
  // {
  //   mContinueLine = continue_line;
  //   if ( BTN_CONT < mNrButton2 ) {
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
    // TDLog.v("Buttons " + mNrButton1 + " " + mNrButton2 + " " + mNrButton3 + " " + mNrButton5 );

    // if ( ! TDLevel.overNormal ) mNrButton1 -= 2; // AZIMUTH, REFRESH requires advanced level
    mButton1 = new Button[ mNrButton1 + 1 ]; // MOVE
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
    }
    mButton1[ mNrButton1 ] = MyButton.getButton( mActivity, null, R.drawable.iz_empty );

    mButton2 = new Button[ mNrButton2 + 1 ]; // DRAW
    for ( int k=0; k<mNrButton2; ++k ) {
      mButton2[k] = MyButton.getButton( mActivity, this, izons_draw[k] );
    }
    mButton2[ mNrButton2 ] = mButton1[ mNrButton1 ];

    mButton3 = new Button[ mNrButton3 + 1 ];      // EDIT
    for ( int k=0; k<mNrButton3; ++k ) {
      mButton3[k] = MyButton.getButton( mActivity, this, izons_edit[k] );
    }
    mButton3[ mNrButton3 ] = mButton1[ mNrButton1 ];

    mButton5 = new Button[ mNrButton5 + 1 ];    // ERASE
    for ( int k=0; k<mNrButton5; ++k ) {
      mButton5[k] = MyButton.getButton( mActivity, this, izons_erase[k] );
    }
    mButton5[ mNrButton5 ] = mButton1[ mNrButton1 ];

    mBMprev     = MyButton.getButtonBackground( this, res, izons[IC_PREV] );
    mBMnext     = MyButton.getButtonBackground( this, res, izons[IC_NEXT] );

    mBMsmall    = MyButton.getButtonBackground( this, res, izons[IC_SMALL] );
    mBMmedium   = MyButton.getButtonBackground( this, res, izons[IC_MEDIUM] );
    mBMlarge    = MyButton.getButtonBackground( this, res, izons[IC_LARGE] );

    mBMsectionOk = MyButton.getButtonBackground( this, res, izons[IC_SECTION_OK] );
    mBMsectionNo = MyButton.getButtonBackground( this, res, izons[IC_SECTION_NO] );

    mBMdeleteOn  = MyButton.getButtonBackground( this, res, izons[IC_DELETE_OK] );
    mBMdeleteOff = MyButton.getButtonBackground( this, res, izons[IC_DELETE_NO] );

    setButtonEraseSize( Drawing.SCALE_MEDIUM );
    setButtonSelectSize( Drawing.SCALE_MEDIUM );

    mButtonView1 = new MyHorizontalButtonView( mButton1 );
    mButtonView2 = new MyHorizontalButtonView( mButton2 );
    mButtonView3 = new MyHorizontalButtonView( mButton3 );
    mButtonView5 = new MyHorizontalButtonView( mButton5 );
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

  private void clearSelection()
  {
    mSelection[0] = null;
    mSelection[1] = null;
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

    clearSelection();

    // audioCheck = TDandroid.checkMicrophone( mActivity );
    // TDLog.v( "Microphone perm : " + audioCheck );

    mZoomBtnsCtrlOn = (TDSetting.mZoomCtrl > 1);  // do before setting content
    mPointScale = PointScale.SCALE_M;

    // mIsNotMultitouch = ! TDandroid.checkMultitouch( this );

    setContentView(R.layout.sketch_activity);
    mZoom             = TopoDroidApp.mScaleFactor;    // canvas zoom

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
    mTouchMode    = MODE_MOVE;
    setMenuAdapter( getResources() );
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
    makeReference();
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
    // TDLog.v("restore drawing display mode");
    String mode = mApp_mData.getValue( "DISTOX_PLOT_MODE" );
    SketchCommandManager.setDisplayMode( DisplayMode.parseString( mode ) );
    switchZoomCtrl( TDSetting.mZoomCtrl );

    // TODO FIXME
    // PlotInfo info = mApp_mData.getPlotInfo( mSid, mName );
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
    //     // TDLog.v("PLOT pause: " + mOffset.x + " " + mOffset.y + " " + mZoom );
    //     mApp_mData.updatePlot( mPid, mSid, mOffset.x, mOffset.y, mZoom );
    //   } catch ( IllegalStateException e ) {
    //     TDLog.Error("cannot save plot state: " + e.getMessage() );
    //   }
    // }
    (new Thread() {
       public void run() {
         mApp_mData.setValue( "DISTOX_PLOT_MODE", DisplayMode.toString( SketchCommandManager.getDisplayMode() ) );
       }
    } ).start();
    doSaveTdr( ); // do not alert-dialog on mAllSymbols
  }

  // private void doStop()
  // {
  //   // TDLog.Log( TDLog.LOG_PLOT, "doStop modified " + Modified );
  // }

// ----------------------------------------------------------------------------

  /** start the sketch display 
   * @param do_load whether to load plot from file
   * @note called by onCreate, switchPlotType, onBackPressed 
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

    TDLog.v("TODO TODO TODO do start"); 
  }

  // ----------------------------------------------------

  /** select at a scene point (x,y)
   * @param x    X-coord of the scene point
   * @param y    Y-coord of the scene point
   * @param size selection radius
   * @note called only onTouchUp
   */
  private void doSelectAt( float x, float y, float size )
  {
    // if ( mLandscape ) { float t=x; x=-y; y=t; }
    if ( mMode == MODE_EDIT ) {
      // float d0 = TopoDroidApp.mCloseCutoff + TopoDroidApp.mSelectness / mZoom;
      SketchPoint selection = mSketchSurface.getItemAt( x, y, mZoom, size );
      if ( mSelection[0] == null ) {
        mSelection[0] = selection;
        mSelection[1] = null;
      } else if ( mSelection[0] != null && selection == null ) {
        mSelection[0] = null;
        mSelection[1] = null;
      } else {
        mSelection[1] = selection;
      }
      // TODO update section button according to mSelection[1]
      setButton3( BTN_SECTION, (( mSelection[1] == null )? mBMsectionNo : mBMsectionOk) );
      setButton3PrevNext();
    } 
  }

  /** execute an erase action
   * @param x    X coordinate of the erase action (scene frame)
   * @param y    Y coordinate of the erase action (scene frame)
   */
  private void doEraseAt( float x, float y )
  {
    // if ( mLandscape ) { float t=x; x=-y; y=t; }
    mSketchSurface.eraseAt( x, y, mZoom, mEraseCommand, mEraseSize );
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
      mOffset.x += x_shift / mZoom;                // add shift to offset
      mOffset.y += y_shift / mZoom; 
      // TDLog.v( "PLOT shift event " + mOffset.x + " " + mOffset.y + " " + mZoom );
      mSketchSurface.setTransform( this, mOffset.x, mOffset.y, mZoom );
    }
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
      mSketchSurface.setTransform( this, mOffset.x, mOffset.y, mZoom );
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
   * @param xs   scene X coord
   * @param ys   scene Y coord
   * @param xc   canvas X coord 
   * @param yc   canvas Y coord
   */
  private void startErasing( float xs, float ys, float xc, float yc )
  {
    // TDLog.v( "Erase at " + xs + " " + ys );
    if ( mTouchMode == MODE_MOVE ) {
      mEraseCommand =  new EraseCommand();
      mSketchSurface.setEraser( xc, yc, mEraseSize );
      doEraseAt( xs, ys );
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
    float x_scene = x_canvas/mZoom - mOffset.x;
    float y_scene = y_canvas/mZoom - mOffset.y;

    if (action == MotionEvent.ACTION_DOWN) { // ---------------------------------------- DOWN
      return onTouchDown( x_canvas, y_canvas, x_scene, y_scene );

    } else if ( action == MotionEvent.ACTION_MOVE ) { // ------------------------------- MOVE
      return onTouchMove( x_canvas, y_canvas, x_scene, y_scene, event, threePointers );

    } else if (action == MotionEvent.ACTION_UP) { // ----------------------------------- UP
      return onTouchUp( x_canvas, y_canvas, x_scene, y_scene );
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

    // TDLog.v( "on touch up. mode " + mMode + " " + mTouchMode );
    if ( mTouchMode == MODE_ZOOM /* || mTouchMode == MODE_ROTATE */ ) {
      mTouchMode = MODE_MOVE;
    } else {
      float x_shift = xc - mSaveX; // compute shift
      float y_shift = yc - mSaveY;
      if ( mMode == MODE_DRAW ) {
        float squared_shift = x_shift*x_shift + y_shift*y_shift;

        mSketchSurface.resetPreviewPath();

        if ( squared_shift > TDSetting.mLineSegment2 || ( mPointCnt % mLinePointStep ) > 0 ) {
          if ( ( ! TDSetting.mStylusOnly ) || squared_shift < 10 * TDSetting.mLineSegment2 ) {
            mSketchSurface.addPointToCurrentPath( new Point2D(xs, ys) );
          }
        }
        
        ArrayList< Point2D > cpath = mSketchSurface.getCurrentPath();
        if ( cpath.size() > 1 ) {
          if ( TDSetting.isLineStyleSimplified() ) {
	    Weeder weeder = new Weeder();
            for ( Point2D lp : cpath ) weeder.addPoint( lp.x, lp.y );
	    // get pixels from meters
	    // float dist = mZoom*DrawingUtil.SCALE_FIX*TDSetting.mWeedDistance;
	    float dist = DrawingUtil.SCALE_FIX*TDSetting.mWeedDistance; // N.B. no zoom
	    float len  = mZoom*DrawingUtil.SCALE_FIX*TDSetting.mWeedLength;
            cpath = weeder.simplify( dist, len );
          } 
          if ( cpath.size() > 1 ) {
            SketchLinePath lp1 = new SketchLinePath( BrushManager.errorPaint );
            for ( Point2D p0 : cpath ) {
              lp1.appendPoint( mSketchSurface.toTDVector( p0.x, p0.y ) );
            }
            mSketchSurface.addLinePath( lp1 );
          }
        }
        // undoBtn.setEnabled(true);
        // redoBtn.setEnabled(false);
        // canRedo = false;
        mSketchSurface.endCurrentPath(); 
        mPointerDown = false;
        modified();
      } else if ( mMode == MODE_EDIT ) {
        if ( Math.abs(mStartX - xc) < TDSetting.mPointingRadius 
          && Math.abs(mStartY - yc) < TDSetting.mPointingRadius ) {
          doSelectAt( xs, ys, mSelectSize );
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
   * @param xs   scene X coord
   * @param ys   scene Y coord
   * @return ...
   */
  private boolean onTouchDown( float xc, float yc, float xs, float ys )
  {
    HBXP_PointDown = false; // HBXP
    mSketchSurface.endEraser();
    float d0 = TDSetting.mCloseCutoff + mSelectSize / mZoom;
    // TDLog.v( "on touch down. mode " + mMode + " " + mTouchMode );

    // TDLog.Log( TDLog.LOG_PLOT, "DOWN at X " + xc + " [" +TopoDroidApp.mBorderInnerLeft + " " + TopoDroidApp.mBorderInnerRight + "] Y " 
    // TDLog.v( "DOWN at X " + xc + " [" +TopoDroidApp.mBorderInnerLeft + " " + TopoDroidApp.mBorderInnerRight + "] Y " + yc + " [" + TopoDroidApp.mBorderTop + " " + TopoDroidApp.mBorderBottom + "]" );

    // float bottom = TopoDroidApp.mBorderBottom - mZoomTranslate;
    // if ( mMode == MODE_DRAW ) bottom += ZOOM_TRANSLATION;

    if ( yc > TopoDroidApp.mBorderBottom ) {
      if ( mZoomBtnsCtrlOn && xc > TopoDroidApp.mBorderInnerLeft && xc < TopoDroidApp.mBorderInnerRight ) {
        mTouchMode = MODE_ZOOM;
        mZoomBtnsCtrl.setVisible( true );
        // mZoomCtrl.show( );
      } else if ( TDSetting.mSideDrag && ( xc > TopoDroidApp.mBorderRight || xc < TopoDroidApp.mBorderLeft ) ) {
        mTouchMode = MODE_ZOOM;
      }
    } else if ( TDSetting.mSideDrag && (yc < TopoDroidApp.mBorderTop) && ( xc > TopoDroidApp.mBorderRight || xc < TopoDroidApp.mBorderLeft ) ) {
      mTouchMode = MODE_ZOOM;
    }

    if ( mMode == MODE_DRAW ) {
      // TDLog.Log( TDLog.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
      mPointCnt = 0;
      mSketchSurface.startCurrentPath();
      // mCurrentLinePat.setOptions( BrushManager.getLineDefaultOptions( mCurrentLine ) );
      mSketchSurface.addPointToCurrentPath( new Point2D( xs, ys ) );
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
      // mEditMove = true;
      TDLog.v("TODO touch down on edit ...");
      // SelectionPoint pt = mSketchSurface.hotItem();
      // if ( pt != null ) {
      //   // if ( mLandscape ) {
      //   //   mEditMove = ( pt.distance( -ys, xs ) < d0 );
      //   // } else {
      //   //   mEditMove = ( pt.distance( xs, ys ) < d0 );
      //   // }
      // } 
      // // doSelectAt( xs, ys, mSelectSize );
      mSaveX = xc;
      mSaveY = yc;
      // return false;

    // } else if ( mMode == MODE_SHIFT ) {
    //   mShiftMove = true; // whether to move canvas in point-shift mode
    //                      // false if moving the hot point
    //   mStartX = xc;
    //   mStartY = yc;
    //   SelectionPoint pt = mSketchSurface.hotItem();
    //   if ( pt != null ) {
    //     // if ( mLandscape ) {
    //     //   if ( pt.distance( -ys, xs ) < d0*4 ) {
    //     //     mShiftMove = false;
    //     //     mStartX = xs;  // save start position
    //     //     mStartY = ys;
    //     //   }
    //     // } else {
    //       if ( pt.distance( xs, ys ) < d0*4 ) {
    //         mShiftMove = false;
    //         mStartX = xs;  // save start position
    //         mStartY = ys;
    //       }
    //     // }
    //   }
    //   mSaveX = xc; // FIXME-000
    //   mSaveY = yc;
    //   // return false;
    }
    return true;
  }

  /** react to a touch-move event
   * @param xc   canvas X coord 
   * @param yc   canvas Y coord
   * @param xs   scene X coord
   * @param ys   scene Y coord
   * @param event motion event
   * @param threePointers whether the event has three pointers
   * @return ...
   */
  private boolean onTouchMove( float xc, float yc, float xs, float ys, MotionEventWrap event, boolean threePointers )
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
            mSketchSurface.addPointToCurrentPath( new Point2D( xs, ys ) );
          }
        } else {
          save = false;
        }
      } else if (  mMode == MODE_MOVE ) {
        moveCanvas( x_shift, y_shift );
      } else if ( mMode == MODE_ERASE ) {
        if ( mEraseCommand != null ) {
          mSketchSurface.setEraser( xc, yc, mEraseSize );
          doEraseAt( xs, ys );
	}
      }
      if ( save ) { // FIXME-000
        mSaveX = xc; 
        mSaveY = yc;
      }
    // } else if ( mTouchMode == MODE_ROTATE ) {
    //   mSketchSurface.rotateHotItem( mRotateScale * ( yc - mStartY ) );
    //   mStartX = xc; // xs;
    //   mStartY = yc; // ys;
    //   modified();
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
    setButton3PrevNext();
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
        mSketchSurface.undo();
        // if ( ! mSketchSurface.hasMoreUndo() ) {
        //   // undoBtn.setEnabled( false );
        // }
        // redoBtn.setEnabled( true );
        // canRedo = true;/
        modified();
      } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // REDO
        if ( mSketchSurface.hasMoreRedo() ) {
          mSketchSurface.redo();
        }
      // } else if ( k2 < mNrButton2 && b == mButton2[k2++] ) { // TOOLS
      //   if ( ! TDSetting.mTripleToolbar ) {
      //     rotateRecentToolset();
      //   } else {
      //     new ItemPickerDialog(mActivity, this, 0, mSymbol ).show();
      //   }
      }

    } else if ( mMode == MODE_ERASE ) {
      int k5 = 3;
      if ( k5 < mNrButton5 && b == mButton5[k5++] ) { // UNDO same as in mButton2[]
        mSketchSurface.undo();
        // if ( ! mSketchSurface.hasMoreUndo() ) {
        //   // undoBtn.setEnabled( false );
        // }
        // redoBtn.setEnabled( true );
        // canRedo = true;/
        modified();
      } else if ( k5 < mNrButton5 && b == mButton5[k5++] ) { // REDO same as in mButton2[]
        if ( mSketchSurface.hasMoreRedo() ) {
          mSketchSurface.redo();
        }
      } else if ( k5 < mNrButton5 && b == mButton5[k5++] ) { // ERASE SIZE
        setButtonEraseSize( mEraseScale + 1 ); // toggle erase size
      }

    } else if ( mMode == MODE_EDIT /* || mMode == MODE_SHIFT */ ) {
      int k3 = 3;
      if ( k3 < mNrButton3 && b == mButton3[k3++] ) { // PREV
        TDLog.v("TODO item previous");
      } else if ( k3 < mNrButton3 && b == mButton3[k3++] ) { // NEXT
        TDLog.v("TODO item next");
      } else if ( k3 < mNrButton3 && b == mButton3[k3++] ) { // EDIT SIZE
        TDLog.v("TODO select size");
      } else if ( k3 < mNrButton3 && b == mButton3[k3++] ) { // EDIT ITEM DELETE
        TDLog.v("TODO item delete");
        // askDeleteItem( p, t, "section");
      } else if ( k3 < mNrButton3 && b == mButton3[k3++] ) { // SECTION
        TDLog.v("TODO section");
      }

    } else {
      int k1 = 3;
      if ( k1 < mNrButton1 && b == mButton1[k1++] ) { // DISPLAY MODE 
        TDLog.v("TODO mode");
        // new SketchModeDialog( mActivity, this, mSketchSurface ).show();
      } else if ( k1 < mNrButton1 && b == mButton1[k1++] ) { //  NOTE
        long mSid = TDInstance.sid;
        if ( mSid >= 0 ) {
          (new DialogAnnotations( mActivity, mApp_mData.getSurveyFromId(mSid) )).show();
        }
      } else if ( TDLevel.overNormal && k1 < mNrButton1 && b == mButton1[k1++] ) { //  REFRESH
        TDLog.v("TODO updateDisplay(); ");
      }
    }
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
    // if ( mLandscape ) {
    //   float w = b.bottom - b.top;
    //   float h = b.right - b.left;
    //   float wZoom = (float) ( mSketchSurface.getMeasuredWidth() * 0.9 ) / ( 1 + w );
    //   float hZoom = (float) ( ( ( mSketchSurface.getMeasuredHeight() - mListView.getHeight() ) * 0.9 ) / ( 1 + h ));
    //   mZoom = Math.min(hZoom, wZoom);
    //   if ( mZoom < 0.1f ) mZoom = 0.1f;
    //   mOffset.y = ( TopoDroidApp.mDisplayHeight + mListView.getHeight() - DrawingUtil.CENTER_Y )/(2*mZoom) + lr;
    //   mOffset.x = ( TopoDroidApp.mDisplayWidth - DrawingUtil.CENTER_X )/(2*mZoom) - tb;
    // } else {
      float w = b.right - b.left;
      float h = b.bottom - b.top;
      float wZoom = (float) ( mSketchSurface.getMeasuredWidth() * 0.9 ) / ( 1 + w );
      float hZoom = (float) ( ( ( mSketchSurface.getMeasuredHeight() - mListView.getHeight() ) * 0.9 ) / ( 1 + h ));
      mZoom = Math.min(hZoom, wZoom);
      if ( mZoom < 0.1f ) mZoom = 0.1f;
      mOffset.x = ( TopoDroidApp.mDisplayWidth - DrawingUtil.CENTER_X )/(2*mZoom) - lr;
      mOffset.y = ( TopoDroidApp.mDisplayHeight + mListView.getHeight() - DrawingUtil.CENTER_Y )/(2*mZoom) - tb;
    // }
    // TDLog.v( "W " + w + " H " + h + " zoom " + mZoom + " X " + mOffset.x + " Y " + mOffset.y );
    // TDLog.v( "display " + TopoDroidApp.mDisplayWidth + " " + TopoDroidApp.mDisplayHeight );
    // TDLog.v( "PLOT zoom fit " + mOffset.x + " " + mOffset.y + " zoom " + mZoom + " tb " + tb + " lr " + lr );
    mSketchSurface.setTransform( this, mOffset.x, mOffset.y, mZoom );
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

  private static final int mNrMenus = 6;

  private static final int[] menus = {
                        R.string.menu_close,      // 0
                        R.string.menu_export,     // 1
                        R.string.menu_zoom_fit,
                        R.string.menu_delete,
                        R.string.menu_options,
                        R.string.menu_help,
  };

  private static final int[] help_menus = {
                        R.string.help_plot_close,
                        R.string.help_save_plot,
                        R.string.help_zoom_fit,
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
    if ( p++ == pos ) { // CLOSE
      super.onBackPressed();
    } else if ( p++ == pos ) { // EXPORT
      TDLog.v("TODO menu EXPORT");
    } else if ( p++ == pos ) { // ZOOM-FIT 
      doZoomFit();
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
      // int nn = 1 + NR_BUTTON1 + NR_BUTTON2 - 3 + NR_BUTTON5 - 5 + ( TDLevel.overBasic? mNrButton3 - 3: 0 );
      // TDLog.v( "Help menu, nn " + nn );
      switch ( mMode ) {
        case MODE_DRAW:
          new HelpDialog(mActivity, izons_draw, menus, help_icons_draw, help_menus, mNrDraw, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
          break;
        case MODE_ERASE:
          new HelpDialog(mActivity, izons_erase, menus, help_icons_erase, help_menus, mNrErase, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
          break;
        case MODE_EDIT:
          new HelpDialog(mActivity, izons_edit, menus, help_icons_edit, help_menus, mNrEdit, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
          break;
        default: // MODE_MOVE
          new HelpDialog(mActivity, izons_move, menus, help_icons_move, help_menus, mNrMove, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
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
    // RectF b = mSketchSurface.getBitmapBounds( 1.0f );
    // zoomFit( b );
    TDLog.v("TODO doZoomFit");
  }

  /**
   * @param export_type  export type
   * @param filename     export filename
   * @param prefix       station names export-prefix (not used)
   * @param second       whether to export the second view instead of the current view (only for plan or profile)
   * @note called from ExportDialogPlot to do the export
   */
  public void doExport( String export_type, String filename, String prefix, boolean second ) // EXPORT
  {
    TDLog.v("TODO doExport");
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
