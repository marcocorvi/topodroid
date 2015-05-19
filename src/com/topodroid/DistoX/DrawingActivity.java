/* @file DrawingActivity.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid main drawing activity
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Activity;
import android.content.Context;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.PackageManager;

import android.util.TypedValue;

import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.PointF;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
// import android.view.Menu;
// import android.view.SubMenu;
// import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.view.ViewGroup;
import android.view.Display;
import android.util.DisplayMetrics;
// import android.view.ContextMenu;
// import android.view.ContextMenu.ContextMenuInfo;
import android.widget.Button;
import android.widget.ZoomControls;
import android.widget.ZoomButton;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import android.util.FloatMath;

import android.provider.MediaStore;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import android.util.Log;

/**
 */
public class DrawingActivity extends ItemDrawer
                             implements View.OnTouchListener
                                      , View.OnClickListener
                                      // , View.OnLongClickListener
                                      , OnItemClickListener
                                      , OnItemSelectedListener
                                      , OnZoomListener
                                      , ILabelAdder
                                      , ILister
                                      , IZoomer
{
  private static int izons_ok[] = { 
                        R.drawable.iz_edit_ok, // 0
                        R.drawable.iz_eraser_ok,
                        R.drawable.iz_select_ok };

  private static int IC_DOWNLOAD = 3;
  private static int IC_JOIN     = 15;
  private static int IC_JOIN_NO  = 20;
  private static int IC_PLAN     = 6;
  private static int IC_DIAL     = 7;
  private static int IC_MENU     = 18;
  private static int IC_EXTEND   = 19;
  private static int IC_CONTINUE_NO = 12;  // index of continue-no icon
  private static int IC_CONTINUE = 21;     // index of continue icon
  private static int IC_ADD = 22;

  private static int BTN_DOWNLOAD = 3;  // index of mButton1 download button
  private static int BTN_BLUETOOTH = 4;
  private static int BTN_JOIN = 5;
  private static int BTN_PLOT = 6;      // index of mButton1 plot button
  private static int BTN_DIAL = 7;      // index of mButton1 azimuth button
  private static int BTN_CONTINUE = 6;  // index of mButton2 continue button

  BitmapDrawable mBMdownload;
  BitmapDrawable mBMdownload_on;
  BitmapDrawable mBMdownload_wait;
  BitmapDrawable mBMjoin;
  BitmapDrawable mBMjoin_no;
  BitmapDrawable mBMplan;
  BitmapDrawable mBMextend;
  BitmapDrawable mBMcontinue_no;
  BitmapDrawable mBMcontinue;
  BitmapDrawable mBMadd;

  BitmapDrawable mBMleft;
  BitmapDrawable mBMright;
  Bitmap mBMdial;

  private static int izons[] = { 
                        R.drawable.iz_edit,          // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,
                        R.drawable.iz_download,      // 3 MOVE Nr 9
                        R.drawable.iz_bt,
                        R.drawable.iz_mode,          // 5
                        R.drawable.iz_plan,          // 6
                        R.drawable.iz_dial,          // 7
                        R.drawable.iz_note,          // 8
                        R.drawable.iz_undo,          // 9 DRAW Nr 7
                        R.drawable.iz_redo,
                        R.drawable.iz_tools,         // 11
                        R.drawable.iz_continue_no,
                        R.drawable.iz_back,          // 13 EDIT Nr 7
                        R.drawable.iz_forw,
                        R.drawable.iz_join,
                        R.drawable.iz_note,          // 16
                        0,
                        R.drawable.iz_menu,          // 18
                        R.drawable.iz_extended,
                        R.drawable.iz_join_no,
                        R.drawable.iz_continue,   // 21
                        R.drawable.iz_plus,
                      };
  private static int menus[] = {
                        R.string.menu_export,
                        R.string.menu_stats,
                        R.string.menu_reload,
                        R.string.menu_delete,
                        R.string.menu_palette,
                        R.string.menu_overview,
                        R.string.menu_options,
                        R.string.menu_help
                     };

  private static int help_icons[] = { 
                        R.string.help_draw,
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_download,
                        R.string.help_remote,
                        R.string.help_refs,
                        R.string.help_toggle_plot,
                        R.string.help_azimuth,
                        R.string.help_note,
                        R.string.help_undo,
                        R.string.help_redo,
                        R.string.help_symbol_plot,
                        R.string.help_continue,
                        R.string.help_previous,
                        R.string.help_next,
                        R.string.help_line_point, // R.string.help_to_point,
                        R.string.help_note_plot
                      };
  private static int help_menus[] = {
                        R.string.help_save_plot,
                        R.string.help_stats,
                        R.string.help_recover,
                        R.string.help_trash,
                        R.string.help_symbol,
                        R.string.help_overview,
                        R.string.help_prefs,
                        R.string.help_help
                      };
    private TopoDroidApp mApp;
    private DataDownloader mDataDownloader;

    private PlotInfo mPlot1;
    private PlotInfo mPlot2;

    long getSID() { return mApp.mSID; }
    String getSurvey() { return mApp.mySurvey; }

    // 0: no bezier, plain path
    // 1: bezier interpolator

    private String mSectionName;

    private static BezierInterpolator mBezierInterpolator = new BezierInterpolator();
    private DrawingSurface  mDrawingSurface;
    private DrawingLinePath mCurrentLinePath;
    private DrawingAreaPath mCurrentAreaPath;
    private DrawingPath mFixedDrawingPath;
    // private Paint mCurrentPaint;
    LinearLayout popup_layout = null;
    PopupWindow popup_window = null;
    // PopupWindow popup_mode_window = null;

    // private boolean canRedo;
    private DistoXNum mNum;
    private int mPointCnt; // counter of points in the currently drawing line

    private boolean mIsNotMultitouch;

    private DrawingBrush mCurrentBrush;
    private Path  mCurrentPath;

    private String mName;
    String mName1;  // first name (PLAN)
    private String mName2;  // second name (EXTENDED)
    private String mFullName1;
    private String mFullName2;

    private boolean mEditMove;    // whether moving the selected point
    private boolean mShiftMove;   // whether to move the canvas in point-shift mode
    boolean mShiftDrawing;        // whether to shift the drawing

    ZoomButtonsController mZoomBtnsCtrl;
    View mZoomView;
    ZoomControls mZoomCtrl;
    // ZoomButton mZoomOut;
    // ZoomButton mZoomIn;
    private float oldDist;  // zoom pointer-sapcing

    private static final float ZOOM_INC = 1.4f;
    private static final float ZOOM_DEC = 1.0f/ZOOM_INC;

    public static final int MODE_DRAW  = 1;
    public static final int MODE_MOVE  = 2;
    public static final int MODE_EDIT  = 3;
    public static final int MODE_ZOOM  = 4; // used only for touchMode
    public static final int MODE_SHIFT = 5; // change point symbol position
    public static final int MODE_ERASE = 6;

    public int mMode   = MODE_MOVE;
    private int mTouchMode = MODE_MOVE;
    private boolean mContinueLine = false;
    private float mSaveX;
    private float mSaveY;
    private float mSave0X;
    private float mSave0Y;
    private float mSave1X;
    private float mSave1Y;
    private float mStartX; // line shift scene start point
    private float mStartY;
    private PointF mOffset  = new PointF( 0f, 0f );
    // private PointF mOffset0 = new PointF( 0f, 0f );
    private PointF mDisplayCenter;
    private float mZoom  = 1.0f;

    private DataHelper mData;
    private long mSid;  // survey id
    private long mPid1; // plot id
    private long mPid2; // plot id
    private long mPid;  // current plot id
    private long mType;  // current plot type
    private String mFrom;
    private String mTo;   // TO station for sections
    private float mAzimuth = 0.0f;
    private float mClino   = 0.0f;
    private boolean mModified; // whether the sketch has been modified 

    private boolean mAllSymbols; // whether the library has all the symbols of the plot

    boolean isSection() { return mType == PlotInfo.PLOT_SECTION || mType == PlotInfo.PLOT_H_SECTION; }
    boolean isXSection() { return mType == PlotInfo.PLOT_X_SECTION || mType == PlotInfo.PLOT_XH_SECTION; }
    boolean isPhoto() { return mType == PlotInfo.PLOT_PHOTO; }
    boolean isSketch2D() { return mType == PlotInfo.PLOT_PLAN || mType == PlotInfo.PLOT_EXTENDED; }
    boolean isSketch3D() { return mType == PlotInfo.PLOT_SKETCH_3D; }

    @Override
    public void onVisibilityChanged(boolean visible)
    {
      mZoomBtnsCtrl.setVisible( visible );
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
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      // mDrawingSurface.refresh();
      // mZoomCtrl.hide();
      // mZoomBtnsCtrl.setVisible( false );
    }

    // private void resetZoom() 
    // {
    //   int w = mDrawingSurface.width();
    //   int h = mDrawingSurface.height();
    //   mOffset.x = w/4;
    //   mOffset.y = h/4;
    //   mZoom = mApp.mScaleFactor;
    //   // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, "zoom one " + mZoom + " off " + mOffset.x + " " + mOffset.y );
    //   if ( mType == PlotInfo.PLOT_PLAN ) {
    //     float zx = w/(mNum.surveyEmax() - mNum.surveyEmin());
    //     float zy = h/(mNum.surveySmax() - mNum.surveySmin());
    //     mZoom = (( zx < zy )? zx : zy)/40;
    //   } else if ( mType == PlotInfo.PLOT_EXTENDED ) {
    //     float zx = w/(mNum.surveyHmax() - mNum.surveyHmin());
    //     float zy = h/(mNum.surveyVmax() - mNum.surveyVmin());
    //     mZoom = (( zx < zy )? zx : zy)/40;
    //   } else {
    //     mZoom = mApp.mScaleFactor;
    //     mOffset.x = 0.0f;
    //     mOffset.y = 0.0f;
    //   }
    //     
    //   // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, "zoom one to " + mZoom );
    //     
    //   mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
    //   // mDrawingSurface.refresh();
    // }

    public void zoomIn()  { changeZoom( ZOOM_INC ); }
    public void zoomOut() { changeZoom( ZOOM_DEC ); }
    // public void zoomOne() { resetZoom( ); }

    // public void zoomView( )
    // {
    //   // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "zoomView ");
    //   DrawingZoomDialog zoom = new DrawingZoomDialog( mDrawingSurface.getContext(), this );
    //   zoom.show();
    // }

    // void setType( int type )
    // {
    //   mSymbol = SYMBOL_POINT;
    //   setTheTitle();
    // }

    static final float SCALE_FIX = 20.0f;  // N.B. DO NOT CHANGE (would break backward compat.)

    public static final float CENTER_X = 100f;
    public static final float CENTER_Y = 120f;

    // private static final PointF mCenter = new PointF( CENTER_X, CENTER_Y );

    static float toSceneX( float x ) { return CENTER_X + x * SCALE_FIX; }
    static float toSceneY( float y ) { return CENTER_Y + y * SCALE_FIX; }

    static float sceneToWorldX( float x ) { return (x - CENTER_X)/SCALE_FIX; }
    static float sceneToWorldY( float y ) { return (y - CENTER_Y)/SCALE_FIX; }

    private void resetFixedPaint( )
    {
      mDrawingSurface.resetFixedPaint( DrawingBrushPaths.fixedShotPaint );
    }
    
    private void makePath( DrawingPath dpath, float x1, float y1, float x2, float y2, float xoff, float yoff )
    {
      dpath.mPath = new Path();
      x1 = toSceneX( x1 );
      y1 = toSceneY( y1 );
      x2 = toSceneX( x2 );
      y2 = toSceneY( y2 );
      dpath.setEndPoints( x1, y1, x2, y2 ); // this sets the midpoint only
      dpath.mPath.moveTo( x1 - xoff, y1 - yoff );
      dpath.mPath.lineTo( x2 - xoff, y2 - yoff );
    }

    private void addFixedSpecial( float x1, float y1, float x2, float y2, float xoff, float yoff )
    {
      DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH );
      dpath.setPaint( DrawingBrushPaths.highlightPaint );
      makePath( dpath, x1, y1, x2, y2, xoff, yoff );
      mDrawingSurface.setNorthPath( dpath );
    }

    private void addFixedLine( DistoXDBlock blk, float x1, float y1, float x2, float y2, float xoff, float yoff, 
                               boolean splay, boolean selectable )
    {
      DrawingPath dpath = null;
      if ( splay ) {
        dpath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk );
        if ( Math.abs(blk.mClino) > TopoDroidSetting.mVertSplay ) {
          dpath.setPaint( DrawingBrushPaths.fixedSplay3Paint );
        } else {
          dpath.setPaint( DrawingBrushPaths.fixedSplayPaint );
        }
      } else {
        dpath = new DrawingPath( DrawingPath.DRAWING_PATH_FIXED, blk );
        dpath.setPaint( blk.isMultiBad() ? DrawingBrushPaths.fixedRedPaint
                        : blk.isRecent( mApp.mSecondLastShotId )? DrawingBrushPaths.fixedBluePaint 
                        : DrawingBrushPaths.fixedShotPaint );
      }
      makePath( dpath, x1, y1, x2, y2, xoff, yoff );
      mDrawingSurface.addFixedPath( dpath, selectable );
    }

    private void addFixedSectionSplay( DistoXDBlock blk, float x1, float y1, float x2, float y2, float xoff, float yoff, 
                               boolean blue )
    {
      DrawingPath dpath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk );
      dpath.setPaint( blue? DrawingBrushPaths.fixedSplay2Paint : DrawingBrushPaths.fixedSplayPaint );
      makePath( dpath, x1, y1, x2, y2, xoff, yoff );
      mDrawingSurface.addFixedPath( dpath, false );
    }

    public void addGrid( float xmin, float xmax, float ymin, float ymax, float xoff, float yoff )
    {
      xmin -= 10.0f;
      xmax += 10.0f;
      ymin -= 10.0f;
      ymax += 10.0f;
      float x1 = (float)(toSceneX( xmin ) - xoff);
      float x2 = (float)(toSceneX( xmax ) - xoff);
      float y1 = (float)(toSceneY( ymin ) - yoff);
      float y2 = (float)(toSceneY( ymax ) - yoff);
      // mDrawingSurface.setBounds( toSceneX( xmin ), toSceneX( xmax ), toSceneY( ymin ), toSceneY( ymax ) );

      DrawingPath dpath = null;
      for ( int x = (int)Math.round(xmin); x < xmax; x += 1 ) {
        float x0 = (float)(toSceneX( x ) - xoff);
        dpath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID );
        dpath.setPaint( (Math.abs(x%10)==5)? DrawingBrushPaths.fixedGrid10Paint : DrawingBrushPaths.fixedGridPaint );
        dpath.mPath  = new Path();
        dpath.mPath.moveTo( x0, y1 );
        dpath.mPath.lineTo( x0, y2 );
        mDrawingSurface.addGridPath( dpath );
      }
      for ( int y = (int)Math.round(ymin); y < ymax; y += 1 ) {
        float y0 = (float)(toSceneY( y ) - yoff);
        dpath = new DrawingPath( DrawingPath.DRAWING_PATH_GRID );
        dpath.setPaint( (Math.abs(y%10)==5)? DrawingBrushPaths.fixedGrid10Paint : DrawingBrushPaths.fixedGridPaint );
        dpath.mPath  = new Path();
        dpath.mPath.moveTo( x1, y0 );
        dpath.mPath.lineTo( x2, y0 );
        mDrawingSurface.addGridPath( dpath );
      }
    }

    // --------------------------------------------------------------------------------------

    @Override
    protected void setTheTitle()
    {
      String s1 = mApp.getConnectionStateTitleStr();
      Resources res = getResources();
      if ( mMode == MODE_DRAW ) { 
        if ( mSymbol == SYMBOL_POINT ) {
          setTitle( s1 + String.format( res.getString(R.string.title_draw_point), 
                                   DrawingBrushPaths.mPointLib.getAnyPointName(mCurrentPoint) ) );
        } else if ( mSymbol == SYMBOL_LINE ) {
          setTitle( s1 + String.format( res.getString(R.string.title_draw_line),
                                   DrawingBrushPaths.getLineName(mCurrentLine) ) );
        } else  {  // if ( mSymbol == SYMBOL_LINE ) 
          setTitle( s1 + String.format( res.getString(R.string.title_draw_area),
                                   DrawingBrushPaths.mAreaLib.getAreaName(mCurrentArea) ) );
        }
        // setButtonContinue( false ); // replaced with these two lines
        // mContinueLine = mContinueLine && mCurrentLine == DrawingBrushPaths.mLineLib.mLineWallIndex;
        boolean visible = ( mSymbol == SYMBOL_LINE && mCurrentLine == DrawingBrushPaths.mLineLib.mLineWallIndex );
        mButton2[ BTN_CONTINUE ].setVisibility( visible? View.VISIBLE : View.GONE );
      } else if ( mMode == MODE_MOVE ) {
        setTitle( s1 + res.getString( R.string.title_move ) );
      } else if ( mMode == MODE_EDIT ) {
        setTitle( s1 + res.getString( R.string.title_edit ) );
      } else if ( mMode == MODE_SHIFT ) {
        setTitle( s1 + res.getString( R.string.title_shift ) );
      } else if ( mMode == MODE_ERASE ) {
        setTitle( s1 + res.getString( R.string.title_erase ) );
      }
      if ( ! mDrawingSurface.isSelectable() ) {
        setTitle( s1 + getTitle() + " [!s]" );
      }
    }

    // private void AlertMissingSymbols()
    // {
    //   new TopoDroidAlertDialog( this, getResources(),
    //                     getResources().getString( R.string.missing_symbols ),
    //     new DialogInterface.OnClickListener() {
    //       @Override
    //       public void onClick( DialogInterface dialog, int btn ) {
    //         mAllSymbols = true;
    //       }
    //     }
    //   );
    // }

    // called by doStop (which is called by onStop)
    private void doSaveTh2( ) 
    {
      Log.v("DistoX", "doSaveTh2() type " + mType + " modified " + mModified );
      TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "Save Th2 " + mFullName1 + " " + mFullName2 );
      if ( mFullName1 != null && mDrawingSurface != null ) {
        // if ( not_all_symbols ) AlertMissingSymbols();
        if ( mAllSymbols ) {
          // Toast.makeText( this, R.string.sketch_saving, Toast.LENGTH_SHORT ).show();
          startSaveTh2Task();
        } else { // mAllSymbols is false
          // FIXME what to do ?
         Toast.makeText( this,
           "NOT SAVING " + mFullName1 + " " + mFullName2, Toast.LENGTH_LONG ).show();
        }
      }
    }


    // called by doSaveTh2 and saveTh2
    private void startSaveTh2Task()
    {
      if ( ! mModified ) return;
      // final Activity currentActivity = this; // if Toast
      Handler saveHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
          // Log.v("DistoX", "handle message " + msg.what );
          if ( msg.what == 660 ) {
            mApp.mShotActivity.enableSketchButton( true );
          } else if ( msg.what == 661 ) { // saving return true
            mModified = false;
            mApp.mShotActivity.enableSketchButton( true );
          } else {
            TopoDroidLog.Log( TopoDroidLog.LOG_ERR, "handle save th2 message " + msg.what);
          }
        }
      };
      mApp.mShotActivity.enableSketchButton( false );
      if ( mType == PlotInfo.PLOT_EXTENDED ) {
        (new SaveTh2File(this, saveHandler, mApp, mDrawingSurface, mFullName2, mType )).execute();
      } else {
        (new SaveTh2File(this, saveHandler, mApp, mDrawingSurface, mFullName1, mType )).execute();
      }
    }

    // private void immediateSaveTh2( )
    // {
    //   Log.v("DistoX", "immSaveTh2() type " + mType + " modified " + mModified );
    //   // if ( mModifed )  FIXME-MODIFIED
    //   {
    //     // Log.v( TopoDroidApp.TAG, " savingTh2 " + mFullName1 + " " + mFullName2 + " do save ");
    //     Handler handler = new Handler(){
    //       @Override
    //       public void handleMessage(Message msg) {
    //         mApp.mShotActivity.enableSketchButton( true );
    //       }
    //     };
    //     mApp.mShotActivity.enableSketchButton( false );
    //     // if ( mType = PlotInfo.PLOT_EXTENDED ) {
    //     //   (new SaveTh2File(this, handler, mApp, mDrawingSurface, null, mFullName2 )).execute();
    //     // } else {
    //     //   (new SaveTh2File(this, handler, mApp, mDrawingSurface, mFullName1, null )).execute();
    //     // }
    //     (new SaveTh2File(this, handler, mApp, mDrawingSurface, mFullName1, mFullName2 )).execute();
    //   }
    // }

  private void computeReferences( int type, float xoff, float yoff, float zoom )
  {
    if ( type != PlotInfo.PLOT_PLAN && type != PlotInfo.PLOT_EXTENDED ) return;

    mDrawingSurface.clearReferences( type );
    mDrawingSurface.setManager( type );

    if ( type == PlotInfo.PLOT_PLAN ) {
      addGrid( mNum.surveyEmin(), mNum.surveyEmax(), mNum.surveySmin(), mNum.surveySmax(), xoff, yoff );
    } else {
      addGrid( mNum.surveyHmin(), mNum.surveyHmax(), mNum.surveyVmin(), mNum.surveyVmax(), xoff, yoff );
    }

    // Log.v("DistoX", "reference offset " + xoff + " " + yoff );
    // Log.v("DistoX", "num " + mNum.surveyEmin() + " " + mNum.surveyEmax()
    //                  + " " + mNum.surveySmin() + " " + mNum.surveySmax() );

    List< NumStation > stations = mNum.getStations();
    List< NumShot > shots   = mNum.getShots();
    List< NumSplay > splays = mNum.getSplays();
    // Log.v( TopoDroidApp.TAG, "stations " + stations.size() + " legs " + shots.size() );
    // Log.v( TopoDroidApp.TAG, "compute refs. offs " + xoff + " " + yoff + " zoom " + zoom );
    if ( type == PlotInfo.PLOT_PLAN ) {
      for ( NumShot sh : shots ) {
        NumStation st1 = sh.from;
        NumStation st2 = sh.to;
        addFixedLine( sh.getFirstBlock(), (float)(st1.e), (float)(st1.s), (float)(st2.e), (float)(st2.s), 
                      xoff, yoff, false, true );
        // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, 
        //   "add line " + (float)(st1.e) + " " + (float)(st1.s) + " " + (float)(st2.e) + " " + (float)(st2.s) );
      }
      for ( NumSplay sp : splays ) {
        if ( Math.abs( sp.getBlock().mClino ) < TopoDroidSetting.mSplayVertThrs ) {
          NumStation st = sp.from;
          addFixedLine( sp.getBlock(), (float)(st.e), (float)(st.s), (float)(sp.e), (float)(sp.s), 
                        xoff, yoff, true, true );
        }
      }
      for ( NumStation st : stations ) {
        DrawingStationName dst;
        dst = mDrawingSurface.addDrawingStation( st, toSceneX(st.e) - xoff, toSceneY(st.s) - yoff, true );
      }
    } else { // if ( type == PlotInfo.PLOT_EXTENDED && 
      for ( NumShot sh : shots ) {
        if  ( ! sh.mIgnoreExtend ) {
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
          addFixedLine( sh.getFirstBlock(), (float)(st1.h), (float)(st1.v), (float)(st2.h), (float)(st2.v), 
                      xoff, yoff, false, true );
          // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, "line " + toSceneX(st1.h) + " " + toSceneY(st1.v) + " - " + toSceneX(st2.h) + " " + toSceneY(st2.v) );
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        addFixedLine( sp.getBlock(), (float)(st.h), (float)(st.v), (float)(sp.h), (float)(sp.v), 
                      xoff, yoff, true, true );
      }
      for ( NumStation st : stations ) {
        DrawingStationName dst;
        dst = mDrawingSurface.addDrawingStation( st, toSceneX(st.h) - xoff, toSceneY(st.v) - yoff, true );
      }
    }

    if ( (! mNum.surveyAttached) && TopoDroidSetting.mCheckAttached ) {
      Toast.makeText( this, R.string.survey_not_attached, Toast.LENGTH_SHORT ).show();
    }

  }
    

  // private Button mButtonHelp;
  private int mButtonSize;
  private Button[] mButton1; // primary
  private Button[] mButton2; // draw
  private Button[] mButton3; // edit
  private Button[] mButton5; // eraser
  private int mNrButton1 = 9;          // main-primary
  private int mNrButton2 = 7;          // draw
  private int mNrButton3 = 7;          // edit
  private int mNrButton5 = 3;          // erase
  HorizontalListView mListView;
  HorizontalButtonView mButtonView1;
  HorizontalButtonView mButtonView2;
  HorizontalButtonView mButtonView3;
  // HorizontalButtonView mButtonView4;
  HorizontalButtonView mButtonView5;
  ListView   mMenu;
  Button     mImage;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;

  List<DistoXDBlock> mList = null;

  int mHotItemType = -1;
  private boolean inLinePoint = false;

  public float zoom() { return mZoom; }


  // private class SaveTimerTask extends java.util.TimerTask
  // {
  //     @Override
  //     public void run() {
  //       (new SaveTh2File(this, null, mApp, mDrawingSurface, mFullName1, mFullName2 )).execute();
  //     }
  // }

  // SaveTimerTask mSaveTask = null;
  // Timer mSaveTimer = null;
                      
  // --------------------------------------------------------------

  public void setRefAzimuth( float azimuth, long fixed_extend )
  {
    mApp.mFixedExtend = fixed_extend;
    mApp.mRefAzimuth = azimuth;
    if ( mApp.mFixedExtend == 0 ) {
      android.graphics.Matrix m = new android.graphics.Matrix();
      m.postRotate( azimuth - 90 );
      Bitmap bm1 = Bitmap.createScaledBitmap( mBMdial, mButtonSize, mButtonSize, true );
      Bitmap bm2 = Bitmap.createBitmap( bm1, 0, 0, mButtonSize, mButtonSize, m, true);
      mButton1[BTN_DIAL].setBackgroundDrawable( new BitmapDrawable( getResources(), bm2 ) );
    } else if ( mApp.mFixedExtend == -1L ) {
      mButton1[BTN_DIAL].setBackgroundDrawable( mBMleft );
    } else {
      mButton1[BTN_DIAL].setBackgroundDrawable( mBMright );
    } 
  }

  // set the button3 by the type of the hot-item
  private void setButton3( int type )
  {
    mHotItemType = type;
    if (    type == DrawingPath.DRAWING_PATH_POINT 
         || type == DrawingPath.DRAWING_PATH_LINE 
         || type == DrawingPath.DRAWING_PATH_AREA 
         || type == DrawingPath.DRAWING_PATH_STATION ) {
      inLinePoint = true;
      // mButton3[ BTN_JOIN ].setBackgroundResource( icons00[ IC_JOIN ] );
      mButton3[ BTN_JOIN ].setBackgroundDrawable( mBMjoin );
    } else {
      inLinePoint = false;
      // mButton3[ BTN_JOIN ].setBackgroundResource( icons00[ IC_JOIN_NO ] );
      mButton3[ BTN_JOIN ].setBackgroundDrawable( mBMjoin_no );
    }
  }

  private void setButtonContinue( boolean continue_line )
  {
    mContinueLine = continue_line;
    if ( mSymbol == SYMBOL_LINE && mCurrentLine == DrawingBrushPaths.mLineLib.mLineWallIndex ) {
      mButton2[ BTN_CONTINUE ].setVisibility( View.VISIBLE );
      // mButton2[ BTN_CONTINUE ].setBackgroundResource( icons00[ ( mContinueLine ? IC_CONTINUE : IC_CONTINUE_NO ) ] );
      mButton2[ BTN_CONTINUE ].setBackgroundDrawable( mContinueLine ? mBMcontinue : mBMcontinue_no  );
    } else {
      mButton2[ BTN_CONTINUE ].setVisibility( View.GONE );
    }
  }

  // used only be setTheTitle
  // void setButtonContinue( boolean visible )
  // {
  //   if ( mSymbol != SYMBOL_LINE || mCurrentLine != DrawingBrushPaths.mLineLib.mLineWallIndex ) 
  //   visible = false;
  //   mButton2[ BTN_CONTINUE ].setVisibility( visible? View.VISIBLE : View.GONE );
  // }

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
      super.onCreate(savedInstanceState);

      // Display display = getWindowManager().getDefaultDisplay();
      // DisplayMetrics dm = new DisplayMetrics();
      // display.getMetrics( dm );
      // int width = dm widthPixels;
      int width = getResources().getDisplayMetrics().widthPixels;

      mIsNotMultitouch = ! getPackageManager().hasSystemFeature( PackageManager.FEATURE_TOUCHSCREEN_MULTITOUCH );
      mSectionName = null;
      mShiftDrawing = false;
      mContinueLine = false;
      mModified = false;

      setContentView(R.layout.drawing_activity);
      mApp = (TopoDroidApp)getApplication();
      mDataDownloader = mApp.mDataDownloader; // new DataDownloader( this, mApp );
      mZoom = mApp.mScaleFactor;    // canvas zoom

      mDisplayCenter = new PointF(mApp.mDisplayWidth  / 2, mApp.mDisplayHeight / 2);

      // setCurrentPaint();
      mCurrentBrush = new DrawingPenBrush();

      mDrawingSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
      mDrawingSurface.setZoomer( this );
      mDrawingSurface.previewPath = new DrawingPath( DrawingPath.DRAWING_PATH_LINE );
      mDrawingSurface.previewPath.mPath = new Path();
      mDrawingSurface.previewPath.setPaint( getPreviewPaint() );
      mDrawingSurface.setOnTouchListener(this);
      // mDrawingSurface.setOnLongClickListener(this);
      // mDrawingSurface.setBuiltInZoomControls(true);


      if ( mIsNotMultitouch ) {
        mZoomView = (View) findViewById(R.id.zoomView );
        mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );
        mZoomBtnsCtrl.setOnZoomListener( this );
        mZoomBtnsCtrl.setVisible( true );
        mZoomBtnsCtrl.setZoomInEnabled( true );
        mZoomBtnsCtrl.setZoomOutEnabled( true );
        mZoomCtrl = (ZoomControls) mZoomBtnsCtrl.getZoomControls();
        // ViewGroup vg = mZoomBtnsCtrl.getContainer();
      }

      mListView = (HorizontalListView) findViewById(R.id.listview);
      mButtonSize = mApp.setListViewHeight( mListView );

      mButton1 = new Button[ mNrButton1 ]; // MOVE
      int off = 0;
      int ic = 0;
      for ( int k=0; k<mNrButton1; ++k ) {
        mButton1[k] = new Button( this );
        mButton1[k].setPadding(0,0,0,0);
        mButton1[k].setOnClickListener( this );
        ic = ( k <3 )? k : off+k;
        BitmapDrawable bm2 = mApp.setButtonBackground( mButton1[k], mButtonSize, izons[ ic ] );
        if ( ic == IC_DOWNLOAD ) {
          mBMdownload = bm2;
        } else if ( ic == IC_PLAN ) {
          mBMplan = bm2;
        }
      }
      mBMdial = BitmapFactory.decodeResource( getResources(), izons[IC_DIAL] );
      mBMextend  = mApp.setButtonBackground( null, mButtonSize, izons[IC_EXTEND] ); 
      mBMdownload_on = mApp.setButtonBackground( null, mButtonSize, R.drawable.iz_download_on );
      mBMdownload_wait = mApp.setButtonBackground( null, mButtonSize, R.drawable.iz_download_wait );
      mBMleft  = mApp.setButtonBackground( null, mButtonSize, R.drawable.iz_left );
      mBMright = mApp.setButtonBackground( null, mButtonSize, R.drawable.iz_right );
      setRefAzimuth( mApp.mRefAzimuth, mApp.mFixedExtend );

      mButton2 = new Button[ mNrButton2 ]; // DRAW
      off = (mNrButton1 - 3); 
      for ( int k=0; k<mNrButton2; ++k ) {
        mButton2[k] = new Button( this );
        mButton2[k].setPadding(0,0,0,0);
        mButton2[k].setOnClickListener( this );
        ic = ( k < 3 )? k : off+k;
        if ( k == 0 ) {
          BitmapDrawable bm2 = mApp.setButtonBackground( mButton2[k], mButtonSize, izons_ok[ic] );
        } else {
          BitmapDrawable bm2 = mApp.setButtonBackground( mButton2[k], mButtonSize, izons[ic] );
          if ( ic == IC_CONTINUE_NO ) mBMcontinue_no = bm2;
        }
      }
      mBMcontinue  = mApp.setButtonBackground( null, mButtonSize, izons[IC_CONTINUE] );

      mButton3 = new Button[ mNrButton3 ];      // EDIT
      off = (mNrButton1-3) + (mNrButton2-3); 
      for ( int k=0; k<mNrButton3; ++k ) {
        mButton3[k] = new Button( this );
        mButton3[k].setPadding(0,0,0,0);
        mButton3[k].setOnClickListener( this );
        ic = ( k < 3 )? k : off+k;
        if ( k == 2 ) {
          BitmapDrawable bm2 = mApp.setButtonBackground( mButton3[k], mButtonSize, izons_ok[ic] );
        } else {
          BitmapDrawable bm2 = mApp.setButtonBackground( mButton3[k], mButtonSize, izons[ic] );
          if ( ic == IC_JOIN ) mBMjoin = bm2;
        }
      }
      mBMjoin_no = mApp.setButtonBackground( null, mButtonSize, izons[IC_JOIN_NO] );
      mBMadd     = mApp.setButtonBackground( null, mButtonSize, izons[IC_ADD] );

      mButton5 = new Button[ mNrButton5 ];    // ERASE
      off = (mNrButton1-3) + (mNrButton2-3) + (mNrButton3-3);
      for ( int k=0; k<mNrButton5; ++k ) {
        mButton5[k] = new Button( this );
        mButton5[k].setPadding(0,0,0,0);
        mButton5[k].setOnClickListener( this );
        ic = ( k < 3 )? k : off+k;
        if ( k == 1 ) {
          BitmapDrawable bm2 = mApp.setButtonBackground( mButton5[k], mButtonSize, izons_ok[ic] );
        } else {
          BitmapDrawable bm2 = mApp.setButtonBackground( mButton5[k], mButtonSize, izons[ic] );
        }
      }
      if ( ! TopoDroidSetting.mLevelOverNormal ) {
         mButton1[2].setVisibility( View.GONE );
         mButton2[2].setVisibility( View.GONE );
         mButton3[2].setVisibility( View.GONE );
         mButton5[2].setVisibility( View.GONE );
       }
 
      // set button1[download] icon
      setConnectionStatus( mDataDownloader.getStatus() );

      mButtonView1 = new HorizontalButtonView( mButton1 );
      mButtonView2 = new HorizontalButtonView( mButton2 );
      mButtonView3 = new HorizontalButtonView( mButton3 );
      mButtonView5 = new HorizontalButtonView( mButton5 );
      mListView.setAdapter( mButtonView1.mAdapter );

      // redoBtn.setEnabled(false);
      // undoBtn.setEnabled(false); // let undo always be there

      DrawingBrushPaths.makePaths( getResources() );
      setTheTitle();

      mData        = mApp.mData; // new DataHelper( this ); 
      Bundle extras = getIntent().getExtras();
      mSid         = extras.getLong(   TopoDroidTag.TOPODROID_SURVEY_ID );
      mName1       = extras.getString( TopoDroidTag.TOPODROID_PLOT_NAME );
      mName2       = extras.getString( TopoDroidTag.TOPODROID_PLOT_NAME2 );
      mFrom        = extras.getString( TopoDroidTag.TOPODROID_PLOT_FROM );  // from station or X-section station
      mAzimuth = 0.0f;
      mClino   = 0.0f;
      mFullName1   = mApp.mySurvey + "-" + mName1;
      if ( mName2 != null && mName2.length() > 0 ) {
        mFullName2   = mApp.mySurvey + "-" + mName2;
      } else {
        mName2 = null;
        mFullName2 = null;
      }
      mName = mName1;

      mType = (int)extras.getLong( TopoDroidTag.TOPODROID_PLOT_TYPE ); 
      if ( isSection() ) { 
        mTo    = extras.getString( TopoDroidTag.TOPODROID_PLOT_TO );  // to station ( null for X-section)
        mAzimuth = (float)extras.getLong( TopoDroidTag.TOPODROID_PLOT_AZIMUTH );
        mClino   = (float)extras.getLong( TopoDroidTag.TOPODROID_PLOT_CLINO );
        // Log.v("DistoX", "X-Section " + mFrom + "-" + mTo + " azimuth " + mAzimuth + " clino " + mClino  );
      } else if ( isXSection() ) {
        mTo = null;
        mAzimuth = (float)extras.getLong( TopoDroidTag.TOPODROID_PLOT_AZIMUTH );
        mClino   = (float)extras.getLong( TopoDroidTag.TOPODROID_PLOT_CLINO );
      }

      // mBezierInterpolator = new BezierInterpolator( );

      mImage = (Button) findViewById( R.id.handle );
      mImage.setOnClickListener( this );
      // mImage.setBackgroundResource( icons00[ IC_MENU ] );
      mApp.setButtonBackground( mImage, mButtonSize, izons[IC_MENU] );
      mMenu = (ListView) findViewById( R.id.menu );
      setMenuAdapter();
      closeMenu();
      mMenu.setOnItemClickListener( this );

      doStart();
    }

    @Override
    protected synchronized void onResume()
    {
      super.onResume();
      doResume();
      // Log.v("DistoX", "Drawing Activity onResume " + ((mDataDownloader!=null)?"with DataDownloader":"") );
      if ( mDataDownloader != null ) mDataDownloader.onResume();
      setConnectionStatus( mDataDownloader.getStatus() );
    }

    @Override
    protected synchronized void onPause() 
    { 
      super.onPause();
      // Log.v("DistoX", "Drawing Activity onPause " + ((mDataDownloader!=null)?"with DataDownloader":"") );
      doPause();
    }

    @Override
    protected synchronized void onStart()
    {
      super.onStart();
      // Log.v("DistoX", "Drawing Activity onStart " + ((mDataDownloader!=null)?"with DataDownloader":"") );
      if ( mDataDownloader != null ) {
        mApp.registerLister( this );
      }
      // if ( mSaveTask != null ) mSaveTask.cancel();
      // mSaveTask = new SaveTimerTask();
      // mSaveTimer = new Timer();
      // mSaveTimer.schedule( mSaveTask, 10000, 60000 );
    }

    @Override
    protected synchronized void onStop()
    {
      super.onStop();
      // Log.v("DistoX", "Drawing Activity onStart " + ((mDataDownloader!=null)?"with DataDownloader":"") );
      if ( mDataDownloader != null ) {
        mApp.unregisterLister( this );
        mDataDownloader.onStop();
        mApp.disconnectRemoteDevice( false );
      }
      doStop();
    }

    private void doResume()
    {
      PlotInfo info = mApp.mData.getPlotInfo( mSid, mName );
      mOffset.x = info.xoffset;
      mOffset.y = info.yoffset;
      mZoom     = info.zoom;
      mDrawingSurface.isDrawing = true;
    }

    private void doPause()
    {
      if ( mIsNotMultitouch ) mZoomBtnsCtrl.setVisible(false);
      mDrawingSurface.isDrawing = false;
      if ( mPid >= 0 ) mData.updatePlot( mPid, mSid, mOffset.x, mOffset.y, mZoom );
    }

    private void doStop()
    {
      Log.v("DistoX", "doStop type " + mType + " modified " + mModified );
      // if ( mSaveTimer != null ) mSaveTimer.cancel();
      // mSaveTimer =  null;
      // if ( mSaveTask != null ) mSaveTask.cancel();
      // mSaveTask = null;
      doSaveTh2( ); // do not alert-dialog on mAllSymbols
    }

// ----------------------------------------------------------------------------

    private void resetCurrentIndices()
    {
      mCurrentPoint = 1; // DrawingBrushPaths.POINT_LABEL;
      mCurrentLine  = 1; // DrawingBrushPaths.mLineLib.mLineWallIndex;
      mCurrentArea  = 1; // DrawingBrushPaths.AREA_WATER;
      if ( ! DrawingBrushPaths.mPointLib.hasPoint( "label" ) ) mCurrentPoint = 0;
      if ( ! DrawingBrushPaths.mLineLib.hasLine( "wall" ) ) mCurrentLine = 0;
      if ( ! DrawingBrushPaths.mAreaLib.hasArea( "water" ) ) mCurrentArea = 0;
      setButtonContinue( false );
    }

    private void doStart()
    {
      TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "do Start() " + mName1 + " " + mName2 );
      resetCurrentIndices();

      if ( isSection() ) {
        mList = mData.selectAllShotsAtStations( mSid, mFrom, mTo );
      } else if ( isXSection() ) { 
        // N.B. mTo can be null
        mList = mData.selectShotsAt( mSid, mFrom, false ); // select only splays
      } else {
        mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
      }

      loadFiles( mType ); 

      // SECTION and H_SECTION: mFrom != null, mTo != null, splays and leg
      // X_SECTION, XH_SECTION: mFrom != null, mTo == null, splays only 
      if ( isSection() || isXSection() ) {
        addGrid( -10, 10, -10, 10, 0.0f, 0.0f );
        float xfrom=0;
        float yfrom=0;
        float xto=0;
        float yto=0;
        // normal, horizontal and cross-product
        float mc = mClino * TopoDroidUtil.GRAD2RAD;
        float ma = mAzimuth * TopoDroidUtil.GRAD2RAD;
        float X0 = FloatMath.cos( mc ) * FloatMath.cos( ma );  // X = North
        float Y0 = FloatMath.cos( mc ) * FloatMath.sin( ma );  // Y = East
        float Z0 = FloatMath.sin( mc );                        // Z = Up
        float X1 = - FloatMath.sin( ma ); // X1 goes to the left in the section plane !!!
        float Y1 =   FloatMath.cos( ma ); 
        float Z1 = 0;
        // float X2 = - FloatMath.sin( mc ) * FloatMath.cos( ma );
        // float Y2 = - FloatMath.sin( mc ) * FloatMath.sin( ma );
        // float Z2 =   FloatMath.cos( ma );
        float X2 = Y0 * Z1 - Y1 * Z0;  // this is X0 ^ X1 : it goes up in the section plane
        float Y2 = Z0 * X1 - Z1 * X0;
        float Z2 = X0 * Y1 - X1 * Y0;

        float dist = 0;
        DistoXDBlock blk = null;
        if ( isSection() ) {
          for ( DistoXDBlock b : mList ) {
            if ( b.mType == DistoXDBlock.BLOCK_SPLAY ) continue;
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
            float bc = blk.mClino * TopoDroidUtil.GRAD2RAD;
            float bb = blk.mBearing * TopoDroidUtil.GRAD2RAD;
            float X = FloatMath.cos( bc ) * FloatMath.cos( bb );
            float Y = FloatMath.cos( bc ) * FloatMath.sin( bb );
            float Z = FloatMath.sin( bc );
            xfrom = -dist * (float)(X1 * X + Y1 * Y + Z1 * Z); // neg. because it is the FROM point
            yfrom =  dist * (float)(X2 * X + Y2 * Y + Z2 * Z);
             
            addFixedLine( blk, xfrom, yfrom, xto, yto, 0, 0, false, false ); // not-splay, not-selecteable
            mDrawingSurface.addDrawingStation( mFrom, toSceneX(xfrom), toSceneY(yfrom) );
            mDrawingSurface.addDrawingStation( mTo, toSceneX(xto), toSceneY(yto) );
          }
        } else { // if ( isXSection() ) }
          mDrawingSurface.addDrawingStation( mFrom, toSceneX(xfrom), toSceneY(yfrom) );
        }

        for ( DistoXDBlock b : mList ) { // repeat for splays
          if ( b.mType != DistoXDBlock.BLOCK_SPLAY ) continue;
          float d = b.mLength;
          float bc = b.mClino * TopoDroidUtil.GRAD2RAD;
          float bb = b.mBearing * TopoDroidUtil.GRAD2RAD;
          float X = FloatMath.cos( bc ) * FloatMath.cos( bb );
          float Y = FloatMath.cos( bc ) * FloatMath.sin( bb );
          float Z = FloatMath.sin( bc );
          float x =  d * (float)(X1 * X + Y1 * Y + Z1 * Z);
          float y = -d * (float)(X2 * X + Y2 * Y + Z2 * Z);
          Log.v("DistoX", "splay " + d + " " + b.mBearing + " " + b.mClino + " coord " + X + " " + Y + " " + Z );
          if ( b.mFrom.equals( mFrom ) ) {
            // N.B. this must be guaranteed for X_SECTION
            x += xfrom;
            y += yfrom;
            addFixedSectionSplay( b, xfrom, yfrom, x, y, 0, 0, false );
            // Log.v("DistoX", "Splay(F) " + x + " " + y );
          } else { // if ( b.mFrom.equals( mTo ) ) 
            x += xto;
            y += yto;
            addFixedSectionSplay( b, xto, yto, x, y, 0, 0, true );
            // Log.v("DistoX", "Splay(T) " + x + " " + y );
          }
        }

        // mDrawingSurface.setScaleBar( mCenter.x, mCenter.y ); // (90,160) center of the drawing

        if ( mType == PlotInfo.PLOT_H_SECTION ) {
          if ( Math.abs( mClino ) > TopoDroidSetting.mHThreshold ) { // north arrow == (1,0,0), 5 m long in the CS plane
            float x =  (float)(X1);
            float y = -(float)(X2);
            float d = 5 / FloatMath.sqrt(x*x + y*y);
            if ( mClino > 0 ) x = -x;
            addFixedSpecial( x*d, y*d, 0, 0, 0, 0 ); 
            // Log.v("AZIMUTH", "North " + x + " " + y );
          }
        }
      }
    }

    private void loadFiles( long type )
    {
      // Log.v( TopoDroidApp.TAG, "load " + mName1 + " " + mName2 );
      mPlot1 = mApp.mData.getPlotInfo( mSid, mName1 );
      mPid1        = mPlot1.id;
      if ( mName2 != null ) {
        mPlot2 = mApp.mData.getPlotInfo( mSid, mName2 );
        mPid2        = mPlot2.id;
      } else {
        mPlot2 = null;
        mPid2 = -1;
      }
      mPid = mPid1;
      // Log.v( TopoDroidApp.TAG, "loadFiles pid " + mName1 + " " + mName2 );

      String start = mPlot1.start;
      String view  = mPlot1.view;
      mType        = mPlot1.type;
      // Log.v( TopoDroidApp.TAG, "loadFiles start <" + start + "> view <" + view + ">" );

      mAllSymbols  = true; // by default there are all the symbols

      if ( isSection() ) {
        mTo = view;
      } else if ( isXSection() ) { 
        mTo = "";
      } else {
        if ( mList.size() == 0 ) {
          Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
          if ( mPid1 >= 0 ) mApp.mData.dropPlot( mPid1, mSid );
          if ( mPid2 >= 0 ) mApp.mData.dropPlot( mPid2, mSid );
          finish();
        } else {
          mNum = new DistoXNum( mList, start, view );
          computeReferences( (int)PlotInfo.PLOT_PLAN, mOffset.x, mOffset.y, mZoom );
          computeReferences( (int)PlotInfo.PLOT_EXTENDED, mOffset.x, mOffset.y, mZoom );
        }
      }

      // now try to load drawings from therion file
      TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "load th2 file " + mFullName1 + " " + mFullName2 );

      String filename1 = TopoDroidPath.getTh2FileWithExt( mFullName1 );
      String filename2 = null;
      if ( mFullName2 != null ) {
        filename2 = TopoDroidPath.getTh2FileWithExt( mFullName2 );
      }

      // Toast.makeText( this, R.string.sketch_loading, Toast.LENGTH_SHORT ).show();
      SymbolsPalette missingSymbols = new SymbolsPalette(); 
      //
      // missingSymbols = palette of missing symbols
      // if there are missing symbols mAllSymbols is false and the MissingDialog is shown
      //    (the dialog just warns the user about missing symbols, maybe a Toast would be enough)
      // when the sketch is saved, mAllSymbols is checked ( see doSaveTh2 )
      // if there are not all symbols the user is asked if he/she wants to save anyways
      //
      mAllSymbols = mDrawingSurface.loadTherion( filename1, filename2, missingSymbols );

      if ( ! mAllSymbols ) {
        String msg = missingSymbols.getMessage( getResources() );
        TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "Missing " + msg );
        Toast.makeText( this, "Missing symbols \n" + msg, Toast.LENGTH_LONG ).show();
        // (new MissingDialog( this, this, msg )).show();
        // finish();
      }

      // resetZoom();
      resetReference( mPlot1 );

      if ( type == PlotInfo.PLOT_EXTENDED ) {
        switchPlotType();
      }
   }

   private void saveReference( PlotInfo plot, long pid )
   {
     // Log.v("DistoX", "save ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
     plot.xoffset = mOffset.x;
     plot.yoffset = mOffset.y;
     plot.zoom    = mZoom;
     mData.updatePlot( pid, mSid, mOffset.x, mOffset.y, mZoom );
   }

   private void resetReference( PlotInfo plot )
   {
     mOffset.x = plot.xoffset; 
     mOffset.y = plot.yoffset; 
     mZoom     = plot.zoom;    
     // Log.v("DistoX", "reset ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
     mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
     // mDrawingSurface.refresh();
   }

    // private void setCurrentPaint()
    // {
    //   mCurrentPaint = new Paint();
    //   mCurrentPaint.setDither(true);
    //   mCurrentPaint.setColor(0xFFFFFF00);
    //   mCurrentPaint.setStyle(Paint.Style.STROKE);
    //   mCurrentPaint.setStrokeJoin(Paint.Join.ROUND);
    //   mCurrentPaint.setStrokeCap(Paint.Cap.ROUND);
    //   mCurrentPaint.setStrokeWidth( WIDTH_CURRENT );
    // }

    private Paint getPreviewPaint()
    {
      final Paint previewPaint = new Paint();
      previewPaint.setColor(0xFFC1C1C1);
      previewPaint.setStyle(Paint.Style.STROKE);
      previewPaint.setStrokeJoin(Paint.Join.ROUND);
      previewPaint.setStrokeCap(Paint.Cap.ROUND);
      previewPaint.setStrokeWidth( DrawingBrushPaths.WIDTH_PREVIEW );
      return previewPaint;
    }

    private void doSelectAt( float x_scene, float y_scene )
    {
      if ( mMode == MODE_EDIT ) {
        // float d0 = TopoDroidApp.mCloseCutoff + TopoDroidApp.mCloseness / mZoom;
        SelectionSet selection = mDrawingSurface.getItemsAt( x_scene, y_scene, mZoom );
        // Log.v( TopoDroidApp.TAG, "selection at " + x_scene + " " + y_scene + " items " + selection.size() );
        // Log.v( TopoDroidApp.TAG, " zoom " + mZoom + " radius " + d0 );
        if ( selection.mPoints.size() > 0 ) {
          mMode = MODE_SHIFT;
          setButton3( selection.mHotItem.type() );
        }
      }
    }

    private void doEraseAt( float x_scene, float y_scene )
    {
      int ret = mDrawingSurface.eraseAt( x_scene, y_scene, mZoom );
      mModified = true;
      // if ( ret > 0 ) {
      //   Log.v( TopoDroidApp.TAG, "erase at " + x_scene + " " + y_scene + " = " + ret );
      // }
    }

    void updateBlockName( DistoXDBlock block, String from, String to )
    {
      // if ( mFullName2 == null ) return; // nothing for PLOT_SECTION or PLOT_H_SECTION
      if ( isSection() )  return;

      if ( ( ( block.mFrom == null && from == null ) || block.mFrom.equals(from) ) && 
           ( ( block.mTo == null && to == null ) || block.mTo.equals(to) ) ) return;

      mModified = true;
      block.mFrom = from;
      block.mTo   = to;
      mData.updateShotName( block.mId, mSid, from, to, true );
      // float x = mOffset.x; 
      // float y = mOffset.y; 
      // float z = mZoom;    
      // mOffset.x = 0.0f;
      // mOffset.y = 0.0f;
      // mZoom = mApp.mScaleFactor;    // canvas zoom
      mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
      mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
      computeReferences( (int)PlotInfo.PLOT_PLAN, 0.0f, 0.0f, mApp.mScaleFactor );
      computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
      // mOffset.x = x; 
      // mOffset.y = y; 
      // mZoom = z;    
      mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      // mDrawingSurface.refresh();
    }
 
    void updateBlockComment( DistoXDBlock block, String comment ) 
    {
      if ( comment.equals( block.mComment ) ) return;
      block.mComment = comment;
      mData.updateShotComment( block.mId, mSid, comment, true ); // true = forward
    }
    
    void updateBlockFlag( DistoXDBlock block, long flag )
    {
      if ( block.mFlag == flag ) return;
      block.mFlag = flag;
      mData.updateShotFlag( block.mId, mSid, flag, true );
    }

    // called only be DrawingShotDialog
    void updateBlockExtend( DistoXDBlock block, long extend )
    {
      if ( block.mExtend == extend ) return;
      block.mExtend = extend;
      mData.updateShotExtend( block.mId, mSid, extend, true );
      if ( mType == PlotInfo.PLOT_EXTENDED ) {
        mModified = true;
        // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, "updateBlockExtend off " + mOffset.x + " " + mOffset.y + " zoom " + mZoom );
        // float x = mOffset.x; 
        // float y = mOffset.y; 
        // float z = mZoom;    
        // mOffset.x = 0.0f;
        // mOffset.y = 0.0f;
        // mZoom = mApp.mScaleFactor;    // canvas zoom
        mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
        computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
        // mOffset.x = x; 
        // mOffset.y = y; 
        // mZoom = z;    
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
        // mDrawingSurface.refresh();
      }
    }

    void deletePoint( DrawingPointPath point ) 
    {
      mModified = true;
      mDrawingSurface.deletePath( point ); 
    }

    void splitLine( DrawingLinePath line, LinePoint point )
    {
      mModified = true;
      mDrawingSurface.splitLine( line, point );
    }

    boolean removeLinePoint( DrawingPointLinePath line, LinePoint point, SelectionPoint sp ) 
    {
      mModified = true;
      return mDrawingSurface.removeLinePoint(line, point, sp); 
    }

    // @param name  section-line name 
    void deleteLine( DrawingLinePath line, String name ) 
    { 
      mModified = true;
      mDrawingSurface.deletePath( line );
      if ( line.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
        String filename = TopoDroidPath.getTh2File( mApp.mySurvey + "-" + name + ".th2" );
        File file = new File( filename );
        if ( file.exists() ) file.delete(); 
        // Log.v("DistoX", "delete th2 file " + filename );

        String filepath = filename + ".bck";
        file = new File( filepath );
        if ( file.exists() ) file.delete(); 
        for ( int i=0; i<SaveTh2File.NR_BACKUP; ++i ) {
          filepath = filename + ".bck" + Integer.toString(i);
          file = new File( filepath );
          if ( file.exists() ) file.delete(); 
        }

        filename = TopoDroidPath.getJpgFile( mApp.mySurvey, name + ".jpg" );
        file = new File( filename );
        if ( file.exists() ) file.delete(); 
        // Log.v("DistoX", "delete jpg file " + filename );
       
        PlotInfo plot = mData.getPlotInfo( mApp.mSID, name );
        if ( plot != null ) mData.dropPlot( plot.id, mApp.mSID );
      }
    }

    void sharpenLine( DrawingLinePath line, boolean reduce )
    {
      mModified = true;
      mDrawingSurface.sharpenLine( line, reduce );
    }

    void deleteArea( DrawingAreaPath area )
    {
      mModified = true;
      mDrawingSurface.deletePath( area );
    }

    // void refreshSurface()
    // {
    //   // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "refresh surface");
    //   mDrawingSurface.refresh();
    // }

    
    private void dumpEvent( WrapMotionEvent ev )
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
      // TopoDroidLog.Log(TopoDroidLog.LOG_PLOT, sb.toString() );
    }
    

    float spacing( WrapMotionEvent ev )
    {
      int np = ev.getPointerCount();
      if ( np < 2 ) return 0.0f;
      float x = ev.getX(1) - ev.getX(0);
      float y = ev.getY(1) - ev.getY(0);
      return FloatMath.sqrt(x*x + y*y);
    }

    void saveEventPoint( WrapMotionEvent ev )
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

    
    void shiftByEvent( WrapMotionEvent ev )
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
    
      if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
        mOffset.x += x_shift / mZoom;                // add shift to offset
        mOffset.y += y_shift / mZoom; 
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
      }
    }

    private void moveCanvas( float x_shift, float y_shift )
    {
      if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
        mOffset.x += x_shift / mZoom;                // add shift to offset
        mOffset.y += y_shift / mZoom; 
        mDrawingSurface.setTransform( mOffset.x, mOffset.y, mZoom );
        // mDrawingSurface.refresh();
      }
    }


    public boolean onTouch( View view, MotionEvent rawEvent )
    {
      dismissPopup();
      // dismissModePopup();

      float d0 = TopoDroidSetting.mCloseCutoff + TopoDroidSetting.mCloseness / mZoom;

      WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
      // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingActivity onTouch() " );
      // dumpEvent( event );

      float x_canvas = event.getX();
      float y_canvas = event.getY();
      // Log.v("DistoX", "touch canvas " + x_canvas + " " + y_canvas ); 

      if ( mIsNotMultitouch && y_canvas > CENTER_Y*2-20 ) {
        mZoomBtnsCtrl.setVisible( true );
        // mZoomCtrl.show( );
      }
      float x_scene = x_canvas/mZoom - mOffset.x;
      float y_scene = y_canvas/mZoom - mOffset.y;
      // Log.v("DistoX", "touch scene " + x_scene + " " + y_scene );

      int action = event.getAction() & MotionEvent.ACTION_MASK;

      if (action == MotionEvent.ACTION_POINTER_DOWN) {
        mTouchMode = MODE_ZOOM;
        oldDist = spacing( event );
        saveEventPoint( event );
      } else if ( action == MotionEvent.ACTION_POINTER_UP) {
        mTouchMode = MODE_MOVE;
        /* nothing */

      // ---------------------------------------- DOWN

      } else if (action == MotionEvent.ACTION_DOWN) {

        if ( mMode == MODE_DRAW ) {
          // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
          mPointCnt = 0;
          if ( mSymbol == SYMBOL_LINE ) {
            mCurrentLinePath = new DrawingLinePath( mCurrentLine );
            mCurrentLinePath.addStartPoint( x_scene, y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
          } else if ( mSymbol == SYMBOL_AREA ) {
            // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "onTouch ACTION_DOWN area type " + mCurrentArea );
            mCurrentAreaPath = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(), true );
            mCurrentAreaPath.addStartPoint( x_scene, y_scene );
            mCurrentBrush.mouseDown( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
          } else { // SYMBOL_POINT
            // mSaveX = x_canvas; // FIXME-000
            // mSaveY = y_canvas;
          }
          mSaveX = x_canvas; // FIXME-000
          mSaveY = y_canvas;
        } else if ( mMode == MODE_EDIT ) {
          mStartX = x_canvas;
          mStartY = y_canvas;
          mEditMove = true;
          SelectionPoint pt = mDrawingSurface.hotItem();
          if ( pt != null ) {
            mEditMove = ( pt.distance( x_scene, y_scene ) < d0 );
          } 
          // doSelectAt( x_scene, y_scene );
          mSaveX = x_canvas;
          mSaveY = y_canvas;
          // return false;
        } else if ( mMode == MODE_SHIFT ) {
          mShiftMove = true; // whether to move canvas in point-shift mode
          mStartX = x_canvas;
          mStartY = y_canvas;

          SelectionPoint pt = mDrawingSurface.hotItem();
          if ( pt != null ) {
            if ( pt.distance( x_scene, y_scene ) < d0 ) {
              mShiftMove = false;
              mStartX = x_scene;  // save start position
              mStartY = y_scene;
            }
          }
          mSaveX = x_canvas; // FIXME-000
          mSaveY = y_canvas;
          // return false;
        } else if ( mMode == MODE_ERASE ) {
          doEraseAt(  x_scene, y_scene );
        } else if ( mMode == MODE_MOVE ) {
          setTheTitle( );
          mSaveX = x_canvas; // FIXME-000
          mSaveY = y_canvas;
          return false;
        }

      // ---------------------------------------- MOVE

      } else if ( action == MotionEvent.ACTION_MOVE ) {
        // Log.v(  TopoDroidApp.TAG, "action MOVE mode " + mMode + " touch-mode " + mTouchMode);
        if ( mTouchMode == MODE_MOVE) {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          boolean save = true; // FIXME-000
          // mSaveX = x_canvas; 
          // mSaveY = y_canvas;
          if ( mMode == MODE_DRAW ) {
            if ( mSymbol == SYMBOL_LINE ) {
              if ( FloatMath.sqrt( x_shift*x_shift + y_shift*y_shift ) > TopoDroidSetting.mLineSegment ) {
                if ( ++mPointCnt % TopoDroidSetting.mLineType == 0 ) {
                  mCurrentLinePath.addPoint( x_scene, y_scene );
                }
                mCurrentBrush.mouseMove( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
              } else {
                save = false;
              }
            } else if ( mSymbol == SYMBOL_AREA ) {
              if ( FloatMath.sqrt( x_shift*x_shift + y_shift*y_shift ) > TopoDroidSetting.mLineSegment ) {
                if ( ++mPointCnt % TopoDroidSetting.mLineType == 0 ) {
                  mCurrentAreaPath.addPoint( x_scene, y_scene );
                }
                mCurrentBrush.mouseMove( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
              } else {
                save = false;
              }
            }
          } else if (  mMode == MODE_MOVE 
                   || (mMode == MODE_EDIT && mEditMove ) 
                   || (mMode == MODE_SHIFT && mShiftMove) ) {
            moveCanvas( x_shift, y_shift );
          } else if ( mMode == MODE_SHIFT ) {
            mModified = true;
            mDrawingSurface.shiftHotItem( x_scene - mStartX, y_scene - mStartY );
            mStartX = x_scene;
            mStartY = y_scene;
          } else if ( mMode == MODE_ERASE ) {
            doEraseAt( x_scene, y_scene );
          }
          if ( save ) { // FIXME-000
            mSaveX = x_canvas; 
            mSaveY = y_canvas;
          }
        } else { // mTouchMode == MODE_ZOOM
          float newDist = spacing( event );
          if ( newDist > 16.0f && oldDist > 16.0f ) {
            float factor = newDist/oldDist;
            if ( factor > 0.05f && factor < 4.0f ) {
              changeZoom( factor );
              oldDist = newDist;
            }
          }
          if ( mMode == MODE_MOVE && mShiftDrawing ) {
            float x_shift = x_canvas - mSaveX; // compute shift
            float y_shift = y_canvas - mSaveY;
            if ( TopoDroidSetting.mLevelOverNormal ) {
              if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
                mModified = true;
                mDrawingSurface.shiftDrawing( x_shift/mZoom, y_shift/mZoom );
              }
            // } else {
            //   moveCanvas( x_shift, y_shift );
            }
            mSaveX = x_canvas;
            mSaveY = y_canvas;
          } else {
            shiftByEvent( event );
          }
        }

      // ---------------------------------------- UP

      } else if (action == MotionEvent.ACTION_UP) {
        if ( onMenu ) {
          closeMenu();
          return true;
        }

        if ( mTouchMode == MODE_ZOOM ) {
          mTouchMode = MODE_MOVE;
        } else {
          float x_shift = x_canvas - mSaveX; // compute shift
          float y_shift = y_canvas - mSaveY;
          if ( mMode == MODE_DRAW ) {
            mModified = true;
            if ( mSymbol == SYMBOL_LINE || mSymbol == SYMBOL_AREA ) {
              // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "onTouch ACTION_UP line style " + mApp.mLineStyle );
              // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, 
              //   "  path size " + ((mSymbol == SYMBOL_LINE )? mCurrentLinePath.size() : mCurrentAreaPath.size()) );

              mCurrentBrush.mouseUp( mDrawingSurface.previewPath.mPath, x_canvas, y_canvas );
              mDrawingSurface.previewPath.mPath = new Path();

              if (    FloatMath.sqrt( x_shift*x_shift + y_shift*y_shift ) > TopoDroidSetting.mLineSegment
                   || (mPointCnt % TopoDroidSetting.mLineType) > 0 ) {
                if ( mSymbol == SYMBOL_LINE ) {
                  mCurrentLinePath.addPoint( x_scene, y_scene );
                } else if ( mSymbol == SYMBOL_AREA ) {
                  mCurrentAreaPath.addPoint( x_scene, y_scene );
                }
              }
              if ( mPointCnt > TopoDroidSetting.mLineType ) {
                // TopoDroidLog.Log( TopoDroidLog.LOG_DEBUG, "Line type " + mCurrentLinePath.mLineType );
                if ( ! ( mSymbol == SYMBOL_LINE && mCurrentLinePath.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) 
                     &&  ( TopoDroidSetting.mLineStyle == TopoDroidSetting.LINE_STYLE_BEZIER ) ) {
                  int nPts = (mSymbol == SYMBOL_LINE )? mCurrentLinePath.size() : mCurrentAreaPath.size() ;
                  if ( nPts > 1 ) {
                    ArrayList< BezierPoint > pts = new ArrayList< BezierPoint >(); // [ nPts ];
                    // ArrayList< LinePoint > lp = 
                    //   (mSymbol == SYMBOL_LINE )? mCurrentLinePath.mPoints : mCurrentAreaPath.mPoints ;
                    // for (int k=0; k<nPts; ++k ) {
                    //   pts.add( new BezierPoint( lp.get(k).mX, lp.get(k).mY ) );
                    // }
                    LinePoint lp = (mSymbol == SYMBOL_LINE )? mCurrentLinePath.mFirst : mCurrentAreaPath.mFirst;
                    for ( ; lp != null; lp = lp.mNext ) {
                      pts.add( new BezierPoint( lp.mX, lp.mY ) );
                    }

                    mBezierInterpolator.fitCurve( pts, nPts, TopoDroidSetting.mLineAccuracy, TopoDroidSetting.mLineCorner );
                    ArrayList< BezierCurve > curves = mBezierInterpolator.getCurves();
                    int k0 = curves.size();
                    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, " Bezier size " + k0 );
                    if ( k0 > 0 ) {
                      BezierCurve c = curves.get(0);
                      BezierPoint p0 = c.getPoint(0);
                      if ( mSymbol == SYMBOL_LINE ) {
                        DrawingLinePath bezier_path = new DrawingLinePath( mCurrentLine );
                        bezier_path.addStartPoint( p0.mX, p0.mY );
                        for (int k=0; k<k0; ++k) {
                          c = curves.get(k);
                          BezierPoint p1 = c.getPoint(1);
                          BezierPoint p2 = c.getPoint(2);
                          BezierPoint p3 = c.getPoint(3);
                          bezier_path.addPoint3(p1.mX, p1.mY, p2.mX, p2.mY, p3.mX, p3.mY );
                        }
                        if ( mContinueLine ) {
                          DrawingLinePath line = mDrawingSurface.getLineToContinue( mCurrentLinePath.mFirst, mCurrentLine );
                          if ( line != null ) {
                            // Log.v( "DistoX", "continuing line ");
                            if ( line.mFirst.distance( mCurrentLinePath.mFirst ) < 20 ) line.reversePath();
                            mDrawingSurface.addLineToLine( mCurrentLinePath, line );
                          } else {
                            mDrawingSurface.addDrawingPath( bezier_path );
                          }
                          // setButtonContinue();
                        } else {
                          mDrawingSurface.addDrawingPath( bezier_path );
                        }
                      } else { //  mSymbol == SYMBOL_AREA
                        DrawingAreaPath bezier_path = new DrawingAreaPath( mCurrentArea, mDrawingSurface.getNextAreaIndex(), true ); 
                        bezier_path.addStartPoint( p0.mX, p0.mY );
                        for (int k=0; k<k0; ++k) {
                          c = curves.get(k);
                          BezierPoint p1 = c.getPoint(1);
                          BezierPoint p2 = c.getPoint(2);
                          BezierPoint p3 = c.getPoint(3);
                          bezier_path.addPoint3(p1.mX, p1.mY, p2.mX, p2.mY, p3.mX, p3.mY );
                        }
                        bezier_path.close();
                        mDrawingSurface.addDrawingPath( bezier_path );
                      }
                    }
                  }
                } else {
                  if ( mSymbol == SYMBOL_LINE ) {
                    // N.B.
                    // section direction is in the direction of the tick
                    // and splay reference are taken from the station the section looks towards
                    // section line points: right-end -- left-end -- tick-end
                    //
                    if ( mCurrentLinePath.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
                      mCurrentLinePath.addOption("-direction both");
                      mCurrentLinePath.makeStraight( true ); // true = with arrow
                      boolean h_section = ( mType == PlotInfo.PLOT_EXTENDED );
                     
                      // NOTE here l1 is the end-point and l2 the start-point (not considering the tick)
                      //         |
                      //         L2 --------- L1
                      //      The azimuth reference is North-East same as bearing
                      //         L1->L2 = atan2( (L2-L1).x, -(L2-l1).y )  Y is point downward North upward
                      //         azimuth = dir(L1->L2) + 90
                      //
                      LinePoint l2 = mCurrentLinePath.mFirst.mNext;
                      LinePoint l1 = l2.mNext;
                      // Log.v("DistoX", "L1 " + l1.mX + " " + l1.mY + " L2 " + l2.mX + " " + l2.mY );

                      List< DrawingPath > paths = mDrawingSurface.getIntersectionShot( l1, l2 );
                      if ( paths.size() > 0 ) {
                        mDrawingSurface.addDrawingPath( mCurrentLinePath );

                        String from = "-1";
                        String to   = "-1";
                        float clino = 0;

                        float azimuth = 90 + (float)(Math.atan2( l2.mX-l1.mX, -l2.mY+l1.mY ) * TopoDroidUtil.RAD2GRAD );
                        if ( azimuth >= 360.0f ) azimuth -= 360;
                        if ( azimuth < 0.0f ) azimuth += 360;

                        DistoXDBlock blk = null;
                        float intersection = 0;
                        if ( paths.size() > 1 ) {
                          Toast.makeText( this, R.string.too_many_leg_intersection, Toast.LENGTH_SHORT ).show();
                        } else {
                          DrawingPath p = paths.get(0);
                          blk = p.mBlock;
                          Float result = new Float(0);
                          p.intersect( l1.mX, l1.mY, l2.mX, l2.mY, result );
                          intersection = result.floatValue();
                          // p.log();
                        }

                        if ( blk != null ) {
                          from = blk.mFrom;
                          to   = blk.mTo;
                          if ( h_section ) {
                            int extend = 1;
                            if ( azimuth < 180 ) {
                              clino = 90 - azimuth;
                              // extend = 1;
                            } else {
                              clino = azimuth - 270;
                              extend = -1;
                            }
                            float dc = (extend == blk.mExtend)? clino - blk.mClino : 180 - clino - blk.mClino ;
                            if ( dc < 0 ) dc += 360;
                            if ( dc > 360 ) dc -= 360;
                            if ( dc > 90 && dc <= 270 ) { // exchange FROM-TO 
                              azimuth = blk.mBearing + 180; if ( azimuth >= 360 ) azimuth -= 360;
                              from = blk.mTo;
                              to   = blk.mFrom;
                            } else {
                              azimuth = blk.mBearing;
                            }
                            // if ( extend != blk.mExtend ) {
                            //   azimuth = blk.mBearing + 180; if ( azimuth >= 360 ) azimuth -= 360;
                            // }
                          } else {
                            float da = azimuth - blk.mBearing;
                            if ( da < 0 ) da += 360;
                            if ( da > 360 ) da -= 360;
                            if ( da > 90 && da <= 270 ) { // exchange FROM-TO 
                              from = blk.mTo;
                              to   = blk.mFrom;
                            }
                          }
                        } else { // null block
                          azimuth = 90 + (float)(Math.atan2( l2.mX-l1.mX, -l2.mY+l1.mY ) * TopoDroidUtil.RAD2GRAD );
                          if ( azimuth >= 360.0f ) azimuth -= 360;
                          if ( azimuth < 0.0f ) azimuth += 360;
                        }
                        // Log.v("DistoX", "new section " + from + " - " + to );
                        // cross-section does not exists yet
                        new DrawingLineSectionDialog( this, mApp, h_section, false, mCurrentLinePath, from, to, azimuth, clino ).show();
                      } else { // empty path list
                        Toast.makeText( this, R.string.no_leg_intersection, Toast.LENGTH_SHORT ).show(); 
                      }
                    } else {
                      if ( mContinueLine ) {
                        DrawingLinePath line = mDrawingSurface.getLineToContinue( mCurrentLinePath.mFirst, mCurrentLine );
                        if ( line != null ) {
                          // Log.v( "DistoX", "continuing line ");
                          if ( line.mFirst.distance( mCurrentLinePath.mFirst ) < 20 ) line.reversePath();
                          mDrawingSurface.addLineToLine( mCurrentLinePath, line );
                        } else {
                          mDrawingSurface.addDrawingPath( mCurrentLinePath );
                        }
                        // setButtonContinue();
                      } else {
                        mDrawingSurface.addDrawingPath( mCurrentLinePath );
                      }
                    }
                  } else { //  mSymbol == SYMBOL_AREA
                    mCurrentAreaPath.close();
                    mDrawingSurface.addDrawingPath( mCurrentAreaPath );
                  }
                }
                // undoBtn.setEnabled(true);
                // redoBtn.setEnabled(false);
                // canRedo = false;
              }
              // if ( mSymbol == SYMBOL_LINE ) {
              //   // Log.v( TopoDroidApp.TAG, "line type " + mCurrentLinePath.mLineType );
              //   if ( mCurrentLinePath.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
              //     // keep only first and last point
              //     // remove line points are put the new ones: FIXME delete and add it again
              //     mDrawingSurface.addDrawingPath( mCurrentLinePath );
              //   }
              // }
            } else { // SYMBOL_POINT
              if ( Math.abs( x_shift ) < 16 && Math.abs( y_shift ) < 16 ) {
                if ( DrawingBrushPaths.mPointLib.pointHasText(mCurrentPoint) ) {
                  DrawingLabelDialog label = new DrawingLabelDialog( mDrawingSurface.getContext(), this, x_scene, y_scene );
                  label.show();
                } else {
                  mDrawingSurface.addDrawingPath( 
                    new DrawingPointPath( mCurrentPoint, x_scene, y_scene, DrawingPointPath.SCALE_M, null ) );

                  // undoBtn.setEnabled(true);
                  // redoBtn.setEnabled(false);
                  // canRedo = false;
                }
              }
            }
          } else if ( mMode == MODE_EDIT ) {
            if ( Math.abs(mStartX - x_canvas) < 10 && Math.abs(mStartY - y_canvas) < 10 ) {
              doSelectAt( x_scene, y_scene );
            }
            mEditMove = false;
          } else if ( mMode == MODE_SHIFT ) {
            if ( mShiftMove ) {
              if ( Math.abs(mStartX - x_canvas) < 10 && Math.abs(mStartY - y_canvas) < 10 ) {
                // mEditMove = false;
                mMode = MODE_EDIT;
                mDrawingSurface.clearSelected();
                setButton3( -1 );
              }
            }
            mShiftMove = false;
          } else { // MODE_MOVE 
            //   return false; // long click
            // }
            /* nothing */
          }
        }
      }
      return true;
    }



    // add a therion label point (ILabelAdder)
    public void addLabel( String label, float x, float y )
    {
      if ( label != null && label.length() > 0 ) {
        mModified = true;
        DrawingLabelPath label_path = new DrawingLabelPath( label, x, y, DrawingPointPath.SCALE_M, null );
        mDrawingSurface.addDrawingPath( label_path );
      } 
    }

    void setCurrentStationName( String name ) { mApp.setCurrentStationName( name ); }

    // @param name station name
    void openXSection( String name ) 
    {
      Log.v("DistoX", "start X section");
      String xsname = "xs-" + name;
      PlotInfo plot = mData.getPlotInfo( mApp.mSID, xsname );
      if ( plot == null  ) { // if there does not exist xsection xs-name create it
        float azimuth = 0;
        float clino = 0;
        List< DistoXDBlock > legs = mData.selectShotsAt( mApp.mSID, name, true ); // select legs
        if ( legs.size() == 1 ) {
          azimuth = legs.get(0).mBearing; 
        } else if ( legs.size() == 2 ) {
          float b0 = legs.get(0).mBearing;
          float b1 = legs.get(1).mBearing;
          azimuth = (b1 + b0);
          if ( Math.abs( b0 - b1 ) > 180 ) azimuth += 180; // (b1 + b0+360)/2 = (b1 + b0)/2 + 180
          if ( azimuth >= 360 ) azimuth -= 360;
        } else {
          Log.v("DistoX", "X_SECTION Too many legs" );
          // Toast
          return;
        }
        Log.v("DistoX", "new X section " + azimuth );
        long pid = mApp.insert2dSection( mApp.mSID, xsname, PlotInfo.PLOT_X_SECTION, name, "", azimuth, clino );
        plot = mData.getPlotInfo( mApp.mSID, xsname );
      }
      if ( plot != null ) {
        Log.v("DistoX", "invoke X section " + plot.name + " <" + plot.start + "> " + plot.azimuth );
        Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_SURVEY_ID, mApp.mSID );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_NAME, plot.name );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_TYPE, plot.type );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_FROM, plot.start );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_TO,   "" );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_AZIMUTH, plot.azimuth );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_CLINO,   plot.clino );
        startActivity( drawIntent );
      }
    }

    void toggleStationBarrier( String name, boolean is_barrier ) 
    {
      String view = mPlot1.view;
      String new_view = "";
      boolean add = false;
      boolean drop = false;
      if ( view == null ) {
        add = true;
        drop = false;
      } else {
        String[] barrier = view.split( " " );
        int k = 0;
        for (; k < barrier.length; ++k ) {
          if ( barrier[k].equals( name ) ) {
            drop = true;
          } else {
            new_view = new_view + " " + barrier[k];
          }
        }
        new_view.trim();
        add = ! drop;
      }
      if ( add && ! is_barrier ) {
        if ( view == null || view.length() == 0 ) {
          view = name;
        } else {
          view = view + " " + name;
        }
        // Log.v( TopoDroidApp.TAG, "addStationBarrier " + name + " view <" + view + ">" );
        mData.updatePlotView( mPid1, mSid, view );
        mData.updatePlotView( mPid2, mSid, view );
        mPlot1.view = view;
        mPlot2.view = view;
        // FIXME recompute num
        mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
        computeReferences( (int)PlotInfo.PLOT_PLAN, 0.0f, 0.0f, mApp.mScaleFactor );
        computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
      } else if ( drop && is_barrier ) {
        mData.updatePlotView( mPid1, mSid, new_view );
        mData.updatePlotView( mPid2, mSid, new_view );
        mPlot1.view = new_view;
        mPlot2.view = new_view;
        // FIXME recompute num
        mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
        computeReferences( (int)PlotInfo.PLOT_PLAN, 0.0f, 0.0f, mApp.mScaleFactor );
        computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
      }
    }
   
    // add a therion station point
    public void addStationPoint( DrawingStationName st )
    {
      mModified = true;
      mDrawingSurface.addDrawingPath( new DrawingStationPath( st, DrawingPointPath.SCALE_M ) );
    }

    void doDelete()
    {
      mData.deletePlot( mPid1, mSid );
      mData.deletePlot( mPid2, mSid );
      finish();
    }

    private void askDelete()
    {
      new TopoDroidAlertDialog( this, getResources(),
                        getResources().getString( R.string.plot_delete ),
        new DialogInterface.OnClickListener() {
          @Override
          public void onClick( DialogInterface dialog, int btn ) {
            doDelete();
          }
        }
      );
    }

    private Button makeButton( String text, int color, int size )
    {
      Button myTextView = new Button( this );
      myTextView.setHeight( 3*size );

      myTextView.setText( text );
      myTextView.setTextColor( color );
      myTextView.setTextSize( TypedValue.COMPLEX_UNIT_DIP, size );
      myTextView.setBackgroundColor( 0xff333333 );
      myTextView.setSingleLine( true );
      myTextView.setGravity( 0x03 ); // left
      myTextView.setPadding( 4, 4, 4, 4 );
      // Log.v(TopoDroidApp.TAG, "makeButton " + text );
      return myTextView;
    }

    public void onBackPressed()
    {
      if ( popup_window != null ) {
        dismissPopup();
        // dismissModePopup();
        return;
      } 
      // finish();
      super.onBackPressed();
    }

    private void setMode( int mode )
    {
      mMode = mode;
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
          setTheTitle();
          mDrawingSurface.setDisplayPoints( true );
          mListView.setAdapter( mButtonView3.mAdapter );
          mListView.invalidate();
          break;
        default:
          break;
      }
    }
  
    // private void makeModePopup( View b )
    // {
    //   final Context context = this;
    //   popup_layout = new LinearLayout(this);
    //   popup_layout.setOrientation(LinearLayout.VERTICAL);
    //   int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
    //   int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;

    //   // ===== MOVE
    //   String text = getString(R.string.popup_mode_move );
    //   int len = text.length();
    //   Button myTextView0 = makeButton( text, 0xff00ff00, 24 );
    //   popup_layout.addView(myTextView0, new LinearLayout.LayoutParams(lHeight, lWidth));
    //   myTextView0.setOnClickListener( new View.OnClickListener( ) {
    //     public void onClick(View v) {
    //       setMode( MODE_MOVE );
    //       dismissModePopup();
    //     }
    //   } );
    //   // ===== DRAW
    //   text = getString(R.string.popup_mode_draw);
    //   if ( len < text.length() ) len = text.length();
    //   Button myTextView1 = makeButton( text, 0xff00ff00, 24 );
    //   popup_layout.addView(myTextView1, new LinearLayout.LayoutParams(lHeight, lWidth));
    //   myTextView1.setOnClickListener( new View.OnClickListener( ) {
    //     public void onClick(View v) {
    //       setMode( MODE_DRAW );
    //       dismissModePopup();
    //     }
    //   } );
    //   // ===== ERASE
    //   text = getString(R.string.popup_mode_erase);
    //   if ( len < text.length() ) len = text.length();
    //   Button myTextView2 = makeButton( text, 0xff00ff00, 24 );
    //   popup_layout.addView(myTextView2, new LinearLayout.LayoutParams(lHeight, lWidth));
    //   myTextView2.setOnClickListener( new View.OnClickListener( ) {
    //     public void onClick(View v) {
    //       setMode( MODE_ERASE );
    //       dismissModePopup();
    //     }
    //   } );
    //   // ===== EDIT
    //   Button myTextView3 = null;
    //   if ( TopoDroidSetting.mLevelOverBasic ) {
    //     text = getString(R.string.popup_mode_edit);
    //     if ( len < text.length() ) len = text.length();
    //     myTextView3 = makeButton( text, 0xff00ff00, 24 );
    //     popup_layout.addView(myTextView3, new LinearLayout.LayoutParams(lHeight, lWidth));
    //     myTextView3.setOnClickListener( new View.OnClickListener( ) {
    //       public void onClick(View v) {
    //         setMode( MODE_EDIT );
    //         dismissModePopup();
    //       }
    //     } );
    //   } 

    //   FontMetrics fm = myTextView0.getPaint().getFontMetrics();
    //   int w = (int)( Math.abs( ( len + 1 ) * fm.ascent ) * 0.7);
    //   int h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.30);
    //   myTextView0.setWidth( w );
    //   myTextView1.setWidth( w );
    //   myTextView2.setWidth( w );
    //   if ( myTextView3 != null ) myTextView3.setWidth( w );
    //   // Log.v( TopoDroidApp.TAG, "popup width " + w );
    //   popup_mode_window = new PopupWindow( popup_layout, w, h ); // popup_layout.getHeight(), popup_layout.getWidth() );
    //   popup_mode_window.showAsDropDown(b); 
    // }

    // private void dismissModePopup()
    // {
    //   if ( popup_mode_window != null ) {
    //     popup_mode_window.dismiss();
    //     popup_mode_window = null;
    //   }
    // }
 
    /** line/area editing
     * @param b button
     */
    private void makePopup( View b )
    {
        final Context context = this;
        popup_layout = new LinearLayout(this);
        popup_layout.setOrientation(LinearLayout.VERTICAL);
        int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
        int lWidth = LinearLayout.LayoutParams.WRAP_CONTENT;

        // ----- MOVE POINT TO THE NEAREST CLOSE POINT
        //
        String text = getString(R.string.popup_join_pt);
        int len = text.length();
        Button myTextView0 = makeButton( text, 0xffffffff, 16 );
        popup_layout.addView(myTextView0, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView0.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_POINT ||
                 mHotItemType == DrawingPath.DRAWING_PATH_LINE ||
                 mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // move to nearest point POINT/LINE/AREA
              if ( mDrawingSurface.moveHotItemToNearestPoint() ) {
                mModified = true;
              } else {
                Toast.makeText( context, R.string.failed_snap_to_point, Toast.LENGTH_SHORT ).show();
              }
            }
            dismissPopup();
          }
        } );
  
        // ----- SNAP AREA BORDER TO CLOSE LINE
        //
        text = getString(R.string.popup_snap_ln);
        if ( len < text.length() ) len = text.length();
        Button myTextView1 = makeButton( text, 0xffffffff, 16 );
        popup_layout.addView(myTextView1, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView1.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // snap to nearest line
              switch ( mDrawingSurface.snapHotItemToNearestLine() ) {
                case 1:  // single point copy
                case 0:  // normal
                case -1: // no hot point
                case -2: // not snapping area border
                  mModified = true;
                  break;
                case -3: // no line close enough
                  Toast.makeText( context, R.string.failed_snap_to_line, Toast.LENGTH_SHORT ).show();
                  break;
                default:
                  break;
              }
            }
            dismissPopup();
          }
        } );
  
        // ----- SPLIT LINE/AREA POINT IN TWO
        //
        text = getString(R.string.popup_split_pt);
        if ( len > text.length() ) len = text.length();
        Button myTextView2 = makeButton( text, 0xffffffff, 16 );
        popup_layout.addView(myTextView2, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView2.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // split point LINE/AREA
              mModified = true;
              mDrawingSurface.splitHotItem();
            }
            dismissPopup();
          }
        } );

        // ----- CUT LINE AT SELECTED POINT AND SPLIT IT IN TWO LINES
        //
        text = getString(R.string.popup_split_ln);
        if ( len < text.length() ) len = text.length();
        Button myTextView3 = makeButton( text, 0xffffffff, 16 );
        popup_layout.addView(myTextView3, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView3.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE ) { // split-line LINE
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && sp.type() == DrawingPath.DRAWING_PATH_LINE ) {
                mModified = true;
                splitLine( (DrawingLinePath)(sp.mItem), sp.mPoint );
              }
            }
            dismissPopup();
          }
        } );

        // ----- MAKE LINE SEGMENT STRAIGHT
        //
        text = getString(R.string.popup_sharp_pt);
        if ( len < text.length() ) len = text.length();
        Button myTextView4 = makeButton( text, 0xffffffff, 16 );
        popup_layout.addView(myTextView4, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView4.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
              // make segment straight LINE/AREA
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                mModified = true;
                sp.mPoint.has_cp = false;
                DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                line.retracePath();
                // mDrawingSurface.refresh();
              }
            }
            dismissPopup();
          }
        } );

        // ----- MAKE LINE SEGMENT SMOOTH (CURVED, WITH CONTROL POINTS)
        //
        text = getString(R.string.popup_curve_pt);
        if ( len < text.length() ) len = text.length();
        Button myTextView5 = makeButton( text, 0xffffffff, 16 );
        popup_layout.addView(myTextView5, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView5.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) {
              // make segment curved LINE/AREA
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                mModified = true;
                LinePoint lp0 = sp.mPoint;
                LinePoint lp2 = lp0.mPrev; 
                if ( ! lp0.has_cp && lp2 != null ) {
                  float dx = (lp0.mX - lp2.mX)/3;
                  float dy = (lp0.mY - lp2.mY)/3;
                  if ( Math.abs(dx) > 0.01 || Math.abs(dy) > 0.01 ) {
                    lp0.mX1 = lp2.mX + dx;
                    lp0.mY1 = lp2.mY + dy;
                    lp0.mX2 = lp0.mX - dx;
                    lp0.mY2 = lp0.mY - dy;
                    lp0.has_cp = true;
                    DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                    line.retracePath();
                  }
                }
                // mDrawingSurface.refresh();
              }
            }
            dismissPopup();
          }
        } );

        // ----- REMOVE LINE/AREA POINT
        //
        text = getString(R.string.popup_remove_pt);
        if ( len < text.length() ) len = text.length();
        Button myTextView6 = makeButton( text, 0xffffffff, 16 );
        popup_layout.addView(myTextView6, new LinearLayout.LayoutParams(lHeight, lWidth));
        myTextView6.setOnClickListener( new View.OnClickListener( ) {
          public void onClick(View v) {
            if ( mHotItemType == DrawingPath.DRAWING_PATH_LINE || mHotItemType == DrawingPath.DRAWING_PATH_AREA ) { // remove pt
              SelectionPoint sp = mDrawingSurface.hotItem();
              if ( sp != null && ( sp.type() == DrawingPath.DRAWING_PATH_LINE || sp.type() == DrawingPath.DRAWING_PATH_AREA ) ) {
                DrawingPointLinePath line = (DrawingPointLinePath)sp.mItem;
                if ( line.size() > 2 ) {
                  mModified = true;
                  removeLinePoint( line, sp.mPoint, sp );
                  line.retracePath();
                }
              }
            }
            dismissPopup();
          }
        } );

        FontMetrics fm = myTextView0.getPaint().getFontMetrics();
        int w = (int)( Math.abs( ( len + 1 ) * fm.ascent ) * 0.7);
        int h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.30);
        myTextView0.setWidth( w );
        myTextView1.setWidth( w );
        myTextView2.setWidth( w );
        myTextView3.setWidth( w );
        myTextView4.setWidth( w );
        myTextView5.setWidth( w );
        myTextView6.setWidth( w );
        // Log.v( TopoDroidApp.TAG, "popup width " + w );
        popup_window = new PopupWindow( popup_layout, w, h ); // popup_layout.getHeight(), popup_layout.getWidth() );
        popup_window.showAsDropDown(b); 
    }

    private void dismissPopup()
    {
      if ( popup_window != null ) {
        popup_window.dismiss();
        popup_window = null;
      }
    }

    private void switchPlotType()
    {
      if ( mType == PlotInfo.PLOT_PLAN ) {
        saveReference( mPlot1, mPid1 );
        setPlotType2();
      } else if ( mType == PlotInfo.PLOT_EXTENDED ) {
        saveReference( mPlot2, mPid2 );
        setPlotType1();
      }
      mModified = false;
    }

    private void setPlotType2( )
    {
      mPid  = mPid2;
      mName = mName2;
      mType = (int)PlotInfo.PLOT_EXTENDED;
      mButton1[ BTN_PLOT ].setBackgroundDrawable( mBMextend );
      mDrawingSurface.setManager( mType );
      resetReference( mPlot2 );
    } 

    private void setPlotType1()
    {
      mPid  = mPid1;
      mName = mName1;
      mType = (int)PlotInfo.PLOT_PLAN;
      mButton1[ BTN_PLOT ].setBackgroundDrawable( mBMplan );
      mDrawingSurface.setManager( mType );
      resetReference( mPlot1 );
    }

    public void onClick(View view)
    {
      if ( onMenu ) {
        closeMenu();
        return;
      }
      // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingActivity onClick() " + view.toString() );
      // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "DrawingActivity onClick() point " + mCurrentPoint + " symbol " + mSymbol );
      dismissPopup();
      // dismissModePopup();

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
      // int k5 = 3; // no normal mButton5
      if ( ( b == mButton2[0] && mMode == MODE_DRAW ) || 
           ( b == mButton5[1] && mMode == MODE_ERASE ) || 
           ( b == mButton3[2] && ( mMode == MODE_EDIT || mMode == MODE_SHIFT ) ) ) { 
        setMode( MODE_MOVE );
      } else if ( b == mButton1[0] || b == mButton3[0] || b == mButton5[0] ) { // 0 --> DRAW
        setMode( MODE_DRAW );
      } else if ( b == mButton1[1] || b == mButton2[1] || b == mButton3[1] ) { // 1--> ERASE
        setMode( MODE_ERASE );
        mListView.invalidate();
      } else if ( b == mButton1[2] || b == mButton2[2] || b == mButton5[2] ) { // 2 --> EDIT
        if ( TopoDroidSetting.mLevelOverBasic ) {
          setMode( MODE_EDIT );
        }
      
      // if ( b == mButton1[0] || b == mButton2[0] || b == mButton3[0] || b == mButton5[0] ) {
      //   makeModePopup( b );

      } else if ( b == mButton1[k1++] ) { // download
        resetFixedPaint();
        if ( mType == (int)PlotInfo.PLOT_PLAN ) {
          saveReference( mPlot1, mPid1 );
        } else if ( mType == (int)PlotInfo.PLOT_EXTENDED ) {
          saveReference( mPlot2, mPid2 );
        }
        if ( mApp.mDevice == null ) {
          // DistoXDBlock last_blk = null; // mApp.mData.selectLastLegShot( mApp.mSID );
          (new ShotNewDialog( this, mApp, this, null, -1L )).show();
        } else {
          mDataDownloader.toggleDownload();
          setConnectionStatus( mDataDownloader.getStatus() );
          mDataDownloader.doDataDownload( );
        }
      } else if ( b == mButton1[k1++] ) { // BLUETOOTH
        new DeviceRemote( this, this, mApp ).show();
      } else if ( b == mButton1[k1++] ) { // DISPLAY MODE 
        new DrawingModeDialog( this, this, mDrawingSurface ).show();
      } else if ( b == mButton1[k1++] ) { // TOGGLE PLAN/EXTENDED
        if ( ! isSection() ) { 
          startSaveTh2Task(); // immediateSaveTh2( ); 
          // mDrawingSurface.clearDrawing();
          switchPlotType();
        }
      } else if ( b == mButton1[k1++] ) { //  AZIMUTH
        if ( TopoDroidSetting.mAzimuthManual ) {
          setRefAzimuth( 0, - mApp.mFixedExtend );
        } else {
          (new AzimuthDialDialog( this, this, mApp.mRefAzimuth, mBMdial )).show();
        }

      } else if ( b == mButton1[k1++] ) { //  NOTE
        (new DistoXAnnotations( this, mData.getSurveyFromId(mSid) )).show();

      } else if ( b == mButton2[k2++] ) { // UNDO
        mDrawingSurface.undo();
        if ( mDrawingSurface.hasMoreUndo() == false ) {
          // undoBtn.setEnabled( false );
        }
        // redoBtn.setEnabled( true );
        // canRedo = true;/
      } else if ( b == mButton2[k2++] ) { // REDO
        if ( mDrawingSurface.hasMoreRedo() ) {
          mDrawingSurface.redo();
        }
      } else if ( b == mButton2[k2++] ) { // pointBtn
        new ItemPickerDialog(this, this, mType ).show();
      } else if ( b == mButton2[k2++] ) { //  continueBtn
        setButtonContinue( ! mContinueLine );

      } else if ( b == mButton3[k3++] ) { // prev
        mMode = MODE_SHIFT;
        SelectionPoint pt = mDrawingSurface.prevHotItem();
        if ( pt != null ) setButton3( pt.type() );
      } else if ( b == mButton3[k3++] ) { // next
        mMode = MODE_SHIFT;
        SelectionPoint pt = mDrawingSurface.nextHotItem();
        if ( pt != null ) setButton3( pt.type() );
      } else if ( b == mButton3[k3++] ) { // item/point editing: move, split, remove, etc.
        // Log.v( TopoDroidApp.TAG, "Button3[5] inLinePoint " + inLinePoint );
        if ( inLinePoint ) {
          makePopup( b );
        } else {
          // SelectionPoint sp = mDrawingSurface.hotItem();
          // if ( sp != null && sp.mItem.mType == DrawingPath.DRAWING_PATH_NAME ) {
          //   DrawingStationName sn = (DrawingStationName)(sp.mItem);
          //   new DrawingBarrierDialog( this, this, sn.mName, mNum.isBarrier( sn.mName ) ).show();
          // }
        }
      } else if ( b == mButton3[k3++] ) { // edit item properties
        SelectionPoint sp = mDrawingSurface.hotItem();
        if ( sp != null ) {
          switch ( sp.type() ) {
            case DrawingPath.DRAWING_PATH_NAME:
              DrawingStationName sn = (DrawingStationName)(sp.mItem);
              new DrawingStationDialog( this, this, sn, mNum.isBarrier( sn.mName ) ).show();
              break;
            case DrawingPath.DRAWING_PATH_POINT:
              new DrawingPointDialog( this, (DrawingPointPath)(sp.mItem) ).show();
              break;
            case DrawingPath.DRAWING_PATH_LINE:
              DrawingLinePath line = (DrawingLinePath)(sp.mItem);
              if ( line.mLineType == DrawingBrushPaths.mLineLib.mLineSectionIndex ) {
                // Log.v("DistoX", "edit section line " );
                // default azimuth = 0
                // default clino = 0
                // cross-section exists already
                boolean h_section = ( mType == PlotInfo.PLOT_EXTENDED ); // not really necessary
                new DrawingLineSectionDialog( this, mApp, h_section, true, line, null, null, 0, 0 ).show();
              } else {
                new DrawingLineDialog( this, line, sp.mPoint ).show();
              }
              break;
            case DrawingPath.DRAWING_PATH_AREA:
              new DrawingAreaDialog( this, (DrawingAreaPath)(sp.mItem) ).show();
              break;
            case DrawingPath.DRAWING_PATH_FIXED:
            case DrawingPath.DRAWING_PATH_SPLAY:
              new DrawingShotDialog( this, this, (DrawingPath)(sp.mItem) ).show();
              break;
          }
        }
        mDrawingSurface.clearSelected();
        mMode = MODE_EDIT;

      }

    }



    void makeSectionPhoto( DrawingLinePath line, String id, long type,
                           String from, String to, float azimuth, float clino )
    {
      mCurrentLine = DrawingBrushPaths.mLineLib.mLineWallIndex;
      if ( ! DrawingBrushPaths.mLineLib.hasLine( "wall" ) ) mCurrentLine = 0;
      setTheTitle();

      if ( id == null || id.length() == 0 ) return;
      mSectionName = id;
      long pid = mApp.mData.getPlotId( mApp.mSID, mSectionName );

      if ( pid < 0 ) { 
        pid = mApp.insert2dSection( mApp.mSID, mSectionName, type, from, to, azimuth, clino );
      }
      if ( pid >= 0 ) {
        // imageFile := PHOTO_DIR / surveyId / photoId .jpg
        File imagefile = new File( TopoDroidPath.getSurveyJpgFile( mApp.mySurvey, id ) );
        try {
          Uri outfileuri = Uri.fromFile( imagefile );
          Intent intent = new Intent( android.provider.MediaStore.ACTION_IMAGE_CAPTURE );
          intent.putExtra( MediaStore.EXTRA_OUTPUT, outfileuri );
          intent.putExtra( "outputFormat", Bitmap.CompressFormat.JPEG.toString() );
          // startActivityForResult( intent, ShotActivity.CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE );
          startActivity( intent );
        } catch ( ActivityNotFoundException e ) {
          Toast.makeText( this, R.string.no_capture_app, Toast.LENGTH_SHORT ).show();
        }
      }
    }

    // @param line   "section" line
    // @param id     section ID
    // @param type   either PLOT_SECTION or PLOT_H_SECTION
    // @param from   from station
    // @param to     to station
    // @param azimuth
    // @param clino
    void makeSectionDraw( DrawingLinePath line, String id, long type, String from, String to, float azimuth, float clino )
    {
      // Log.v("DistoX", "make section: " + id + " <" + from + "-" + to + "> azimuth " + azimuth + " clino " + clino );

      mCurrentLine = DrawingBrushPaths.mLineLib.mLineWallIndex;
      if ( ! DrawingBrushPaths.mLineLib.hasLine( "wall" ) ) mCurrentLine = 0;
      setTheTitle();

      if ( id == null || id.length() == 0 ) return;
      mSectionName = id;
      long pid = mApp.mData.getPlotId( mApp.mSID, mSectionName );

      if ( pid < 0 ) { 
        // pid = mApp.mData.insertPlot( mApp.mSID, -1L, mSectionName, type, 0L, from, to, 
        //                              0, 0, TopoDroidApp.mScaleFactor, azimuth, clino, false ); // forward or not ?
        pid = mApp.insert2dSection( mApp.mSID, mSectionName, type, from, to, azimuth, clino );
      }
      if ( pid >= 0 ) {
        Intent drawIntent = new Intent( Intent.ACTION_VIEW ).setClass( this, DrawingActivity.class );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_SURVEY_ID, mApp.mSID );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_NAME, mSectionName );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_TYPE, type );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_FROM, from );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_TO,   to );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_AZIMUTH, (long)azimuth );
        drawIntent.putExtra( TopoDroidTag.TOPODROID_PLOT_CLINO, (long)clino );
        startActivity( drawIntent );
      }
    }

    private class ExportBitmapToFile extends AsyncTask<Intent,Void,Boolean> 
    {
        private Context mContext;
        private Handler mHandler;
        private Bitmap mBitmap;
        private String mFullName;

        public ExportBitmapToFile( Context context, Handler handler, Bitmap bitmap, String name )
        {
           mContext  = context;
           mBitmap   = bitmap;
           mHandler  = handler;
           mFullName = name;
           // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "ExportBitmapToFile " + mFullName );
        }

        @Override
        protected Boolean doInBackground(Intent... arg0)
        {
          try {
            String filename = TopoDroidPath.getPngFileWithExt( mFullName );
            TopoDroidApp.checkPath( filename );
            final FileOutputStream out = new FileOutputStream( filename );
            mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
            out.flush();
            out.close();
            return true;
          } catch (Exception e) {
            e.printStackTrace();
          }
          //mHandler.post(completeRunnable);
          return false;
        }


        @Override
        protected void onPostExecute(Boolean bool) {
            super.onPostExecute(bool);
            mHandler.sendEmptyMessage( bool? 1 : 0 );
        }
    }


    private class ExportToFile extends AsyncTask<Intent,Void,Boolean> 
    {
        private Context mContext;
        private DrawingCommandManager mCommand;
        private DistoXNum mNum;
        private long mType;
        private Handler mHandler;
        private String mFullName;
        private String mExt; // extension

        public ExportToFile( Context context, Handler handler, DrawingCommandManager command,
                             DistoXNum num, long type, String name, String ext )
        {
           mContext  = context;
           mCommand  = command;
           mNum = num;
           mType = type;
           mHandler  = handler;
           mFullName = name;
           mExt = ext;
        }

        @Override
        protected Boolean doInBackground(Intent... arg0)
        {
          try {
            String filename = null;
            if ( mExt.equals("dxf") ) {
              filename = TopoDroidPath.getDxfFileWithExt( mFullName );
            } else if ( mExt.equals("svg") ) {
              filename = TopoDroidPath.getSvgFileWithExt( mFullName );
            }
            if ( filename != null ) {
              // final FileOutputStream out = new FileOutputStream( filename );
              TopoDroidApp.checkPath( filename );
              final FileWriter fw = new FileWriter( filename );
              BufferedWriter bw = new BufferedWriter( fw );
              if ( mExt.equals("dxf") ) {
                DrawingDxf.write( bw, mNum, mCommand, mType );
              } else if ( mExt.equals("svg") ) {
                DrawingSvg.write( bw, mNum, mCommand, mType );
              }
              fw.flush();
              fw.close();
              return true;
            }
          } catch (Exception e) {
            e.printStackTrace();
          }
          //mHandler.post(completeRunnable);
          return false;
        }


        @Override
        protected void onPostExecute(Boolean bool) 
        {
          super.onPostExecute(bool);
          mHandler.sendEmptyMessage( bool? 1 : 0 );
        }
    }

    // --------------------------------------------------------

    void savePng()
    {
      if ( isSection() ) { 
        doSavePng( mType, mFullName1 ); // FIXME
      } else {
        doSavePng( (int)PlotInfo.PLOT_PLAN, mFullName1 ); // FIXME
        doSavePng( (int)PlotInfo.PLOT_EXTENDED, mFullName2 );
      }
    }

    void doSavePng( long type, final String filename )
    {
      final Activity currentActivity  = this;
      Handler saveHandler = new Handler(){
           @Override
           public void handleMessage(Message msg) {
             if (msg.what == 1 ) {
               Toast.makeText( currentActivity, 
                     getString(R.string.saved_file_) + " " + filename + ".png", Toast.LENGTH_SHORT ).show();
             } else {
               Toast.makeText( currentActivity, R.string.saving_file_failed, Toast.LENGTH_SHORT ).show();
             }
           }
      } ;
      Bitmap bitmap = mDrawingSurface.getBitmap( type );
      if ( bitmap == null ) {
        Toast.makeText( this, R.string.null_bitmap, Toast.LENGTH_SHORT ).show();
      } else {
        new ExportBitmapToFile(this, saveHandler, bitmap, filename ).execute();
      }
    }

    void saveCsx()
    {
      mApp.exportSurveyAsCsx( this, mPlot1.start );
    }

    void saveWithExt( String ext )
    {
      if ( isSection() ) { 
        doSaveWithExt( mType, mFullName1, ext ); // FIXME
      } else {
        doSaveWithExt( PlotInfo.PLOT_PLAN, mFullName1, ext ); // FIXME
        doSaveWithExt( PlotInfo.PLOT_EXTENDED, mFullName2, ext );
      }
    }

    // ext file extension (--> saving class)
    private void doSaveWithExt( long type, final String filename, final String ext )
    {
      final Activity currentActivity  = this;
      Handler saveHandler = new Handler(){
           @Override
           public void handleMessage(Message msg) {
             if (msg.what == 1 ) {
               Toast.makeText( currentActivity, 
                 getString(R.string.saved_file_) + " " + filename + "." + ext, Toast.LENGTH_SHORT ).show();
             } else {
               Toast.makeText( currentActivity, 
                 getString(R.string.saving_file_failed), Toast.LENGTH_SHORT ).show();
             }
           }
      } ;
      if ( type == PlotInfo.PLOT_EXTENDED ) {
        new ExportToFile(this, saveHandler, mDrawingSurface.mCommandManager2, mNum, type, filename, ext ).execute();
      } else {
        new ExportToFile(this, saveHandler, mDrawingSurface.mCommandManager1, mNum, type, filename, ext ).execute();
      }
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

    // called only by PlotSaveDialog: save as th2 even if there are missing symbols
    void saveTh2()
    {
      Log.v("DistoX", "saveTh2() type " + mType + " modified " + mModified );
      TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "saveTh2 back up " + mFullName1 + " " + mFullName2 );
      // String filename  = TopoDroidPath.getTh2FileWithExt( mFullName1 ) + ".bck";
      // rotateBackup( filename );
      // if ( mFullName2 != null ) {
      //   filename = TopoDroidPath.getTh2FileWithExt( mFullName2 ) + ".bck";
      //   rotateBackup( filename );
      // }
      // doSaveTh2( ! mAllSymbols );
      startSaveTh2Task();
    }

  
  // @Override
  // public void onCreateContextMenu( ContextMenu menu, View v, ContextMenuInfo info )
  // {
  //   super.onCreateContextMenu( menu, v, info );
  //   getMenuInflater().inflate( R.menu.popup, menu );
  //   menu.setHeaderTitle( "Context Menu" );
  //   Log.v( TopoDroidApp.TAG, "onCreateContextMenu view " + v.toString()  );
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

  public void refreshDisplay( int nr, boolean toast )
  {
    setTitleColor( TopoDroidConst.COLOR_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
        if ( mType == (int)PlotInfo.PLOT_PLAN ) {
          computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
          computeReferences( (int)PlotInfo.PLOT_PLAN, 0.0f, 0.0f, mApp.mScaleFactor );
          resetReference( mPlot1 );
        } else if ( mType == (int)PlotInfo.PLOT_EXTENDED ) {
          computeReferences( (int)PlotInfo.PLOT_PLAN, 0.0f, 0.0f, mApp.mScaleFactor );
          computeReferences( (int)PlotInfo.PLOT_EXTENDED, 0.0f, 0.0f, mApp.mScaleFactor );
          resetReference( mPlot2 );
        }
      }
      if ( toast ) {
        Toast.makeText( this, String.format( getString(R.string.read_data), nr ), Toast.LENGTH_SHORT ).show();
        // Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_SHORT ).show();
      }
    } else if ( nr < 0 ) {
      if ( toast ) {
        // Toast.makeText( this, getString(R.string.read_fail_with_code) + nr, Toast.LENGTH_SHORT ).show();
        Toast.makeText( this, mApp.DistoXConnectionError[ -nr ], Toast.LENGTH_SHORT ).show();
      }
    }
  }

  public void updateDisplay( boolean compute )
  {
    // Log.v( TopoDroidApp.TAG, "update display: list " + mList.size() );
    if ( compute ) {
      mList = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
      mNum = new DistoXNum( mList, mPlot1.start, mPlot1.view );
      computeReferences( (int)mType, 0.0f, 0.0f, mApp.mScaleFactor );
    }
    if ( mType == (int)PlotInfo.PLOT_PLAN ) {
      resetReference( mPlot1 );
    } else if ( mType == (int)PlotInfo.PLOT_EXTENDED ) {
      resetReference( mPlot2 );
    }
  }

  // forward adding data to the ShotActivity
  @Override
  public void updateBlockList( DistoXDBlock blk )
  {
    mApp.mShotActivity.updateBlockList( blk );
    // mButton1[ BTN_DOWNLOAD ].setBackgroundDrawable( mBMdownload );
    // recomputing the whole reference is inefficient: an incremental data reduction would be more efficient
    updateDisplay( true );
  }

  // ---------------------------------------------------------
  // MENU

  private void setMenuAdapter()
  {
    Resources res = getResources();
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    mMenuAdapter.add( res.getString( menus[0] ) );
    mMenuAdapter.add( res.getString( menus[1] ) );
    if ( TopoDroidSetting.mLevelOverBasic  ) mMenuAdapter.add( res.getString( menus[2] ) );
    mMenuAdapter.add( res.getString( menus[3] ) );
    mMenuAdapter.add( res.getString( menus[4] ) );
    mMenuAdapter.add( res.getString( menus[5] ) );
    mMenuAdapter.add( res.getString( menus[6] ) );
    mMenuAdapter.add( res.getString( menus[7] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) { // EXPORT
        new PlotSaveDialog( this, this ).show();
      } else if ( p++ == pos ) { // INFO
        if ( mNum != null ) {
          new DistoXStatDialog( mDrawingSurface.getContext(), mNum ).show();
        }
      } else if ( p++ == pos ) { // RECOVER
        // askRecover();
        if ( mType == PlotInfo.PLOT_EXTENDED ) {
          ( new PlotRecoverDialog( this, this, mFullName2, 2 ) ).show();
        } else {
          ( new PlotRecoverDialog( this, this, mFullName1, 1 ) ).show();
        }
      } else if ( TopoDroidSetting.mLevelOverBasic && p++ == pos ) { // DELETE
        askDelete();
      } else if ( p++ == pos ) { // PALETTE
        DrawingBrushPaths.makePaths( getResources() );
        (new SymbolEnableDialog( this, this )).show();
      } else if ( p++ == pos ) { // OVERVIEW
        startSaveTh2Task(); // immediateSaveTh2( ); 
        try {
          Thread.sleep(100);
        } catch ( InterruptedException e ) { /* ignore */ }
        Intent intent = new Intent( this, OverviewActivity.class );
        intent.putExtra( TopoDroidTag.TOPODROID_SURVEY_ID, mSid );
        intent.putExtra( TopoDroidTag.TOPODROID_PLOT_FROM, mFrom );
        intent.putExtra( TopoDroidTag.TOPODROID_PLOT_ZOOM, mZoom );
        intent.putExtra( TopoDroidTag.TOPODROID_PLOT_TYPE, mType );
        intent.putExtra( TopoDroidTag.TOPODROID_PLOT_XOFF, mOffset.x );
        intent.putExtra( TopoDroidTag.TOPODROID_PLOT_YOFF, mOffset.y );
        startActivity( intent );
      } else if ( p++ == pos ) { // OPTIONS
        Intent intent = new Intent( this, TopoDroidPreferences.class );
        intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_PLOT );
        startActivity( intent );
      } else if ( p++ == pos ) { // HELP
        int nn = mNrButton1 + mNrButton2 - 3 + mNrButton5 - 3 + ( TopoDroidSetting.mLevelOverBasic? mNrButton3 - 3: 0 );
        (new HelpDialog(this, izons, menus, help_icons, help_menus, nn, 8 ) ).show();
      }
    }
  }

  // private void askRecover()
  // {
  //   new TopoDroidAlertDialog( this, getResources(), getResources().getString( R.string.plot_recover ),
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         doRecover( mFullName1, mFullName2 );
  //       }
  //     }
  //   );
  // }

  // FIXME BACKUP
  void doRecover( String filename, int type )
  {
    // Log.v("DistoX", "recover " + type + " " + filename );

    String filename1 = TopoDroidPath.getTh2File( filename );
    SymbolsPalette missingSymbols = new SymbolsPalette();
    if ( type == 1 ) {
      mDrawingSurface.loadTherion( filename1, null, missingSymbols );
      setPlotType1();
    } else {
      mDrawingSurface.loadTherion( null, filename1, missingSymbols );
      // TODO now switch to extended view FIXME-VIEW
      setPlotType2();
    }
  }

  void exportAsCsx( PrintWriter pw )
  {
    pw.format("  <plan>\n");
    mDrawingSurface.exportAsCsx( pw, PlotInfo.PLOT_PLAN );
    pw.format("    <plot />\n");
    pw.format("  </plan>\n");
    pw.format("  <profile>\n");
    mDrawingSurface.exportAsCsx( pw, PlotInfo.PLOT_EXTENDED );
    pw.format("    <plot />\n");
    pw.format("  </profile>\n");
  }

  public void setConnectionStatus( int status )
  { 
    if ( mApp.mDevice == null ) {
      // mButton1[ BTN_DOWNLOAD ].setVisibility( View.GONE );
      mButton1[ BTN_DOWNLOAD ].setBackgroundDrawable( mBMadd );
    } else {
      // mButton1[ BTN_DOWNLOAD ].setVisibility( View.VISIBLE );
      switch ( status ) {
        case 1:
          mButton1[ BTN_DOWNLOAD ].setBackgroundDrawable( mBMdownload_on );
          break;
        case 2:
          mButton1[ BTN_DOWNLOAD ].setBackgroundDrawable( mBMdownload_wait );
          break;
        default:
          mButton1[ BTN_DOWNLOAD ].setBackgroundDrawable( mBMdownload );
      }
    }
  }

}
