/* @file OverviewWindow.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid sketch overview activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import com.topodroid.utils.TDMath;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDTag;
import com.topodroid.utils.TDStatus;
import com.topodroid.utils.TDColor;
import com.topodroid.num.TDNum;
import com.topodroid.num.NumStation;
import com.topodroid.num.NumShot;
import com.topodroid.num.NumSplay;
import com.topodroid.math.Point2D;
import com.topodroid.ui.MyButton;
import com.topodroid.ui.MyHorizontalListView;
import com.topodroid.ui.MyHorizontalButtonView;
import com.topodroid.ui.MotionEventWrap;
import com.topodroid.help.HelpDialog;
import com.topodroid.help.UserManualActivity;
import com.topodroid.prefs.TDSetting;
import com.topodroid.prefs.TDPrefCat;
import com.topodroid.common.PlotType;

import android.util.Log;

import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Configuration;

import android.util.TypedValue;

import android.graphics.PointF;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.Matrix;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import android.view.MotionEvent;
import android.view.View;
import android.view.KeyEvent;

import android.widget.Button;
import android.widget.ZoomButtonsController;
import android.widget.ZoomButtonsController.OnZoomListener;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

import java.util.List;
import java.util.ArrayList;

/**
 */
public class OverviewWindow extends ItemDrawer
                             implements View.OnTouchListener
                                      , View.OnClickListener
                                      , OnItemClickListener
                                      , OnZoomListener
                                      , IZoomer
				      , IExporter
{
  private static final int[] izons = {
                        R.drawable.iz_measure,       // 0
                        R.drawable.iz_mode,          // 1
                        R.drawable.iz_angle,   // 2
			R.drawable.iz_undo,    // 3
                        // FIXME_OVER R.drawable.iz_plan,          // 3
                        // R.drawable.iz_menu,          // 4
                        // R.drawable.iz_measure_on,
                        // R.drawable.iz_polyline
			R.drawable.iz_empty
                      };
  // FIXME_OVER private static int BTN_PLOT = 2;

  private static final int[] menus = {
                        R.string.menu_close,
			R.string.menu_export,
                        R.string.menu_options,
                        R.string.menu_help
                     };

  private static final int[] help_icons = {
                        R.string.help_measure,
                        R.string.help_refs,
                        R.string.help_measure_type,
                        // FIXME_OVER R.string.help_toggle_plot,
                        R.string.help_measure_undo,
                      };
  private static final int[] help_menus = {
                        R.string.help_close,
			R.string.help_save_plot,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  private static final int HELP_PAGE = R.string.OverviewWindow;

  // FIXME_OVER BitmapDrawable mBMextend;
  // FIXME_OVER BitmapDrawable mBMplan;
  private BitmapDrawable mBMselect;
  private BitmapDrawable mBMselectOn;
  private BitmapDrawable mBMcontinueNo;
  private BitmapDrawable mBMcontinueOn;

  private final int IC_SELECT   = 0;
  private final int IC_CONTINUE = 2;
  private final int IC_UNDO     = 3;

  private float mDDtotal = 0;
  private int mTotal = 0;

  private float mUnitRuler = 1;

  private TopoDroidApp mApp;
  private DataHelper mData;
  // private DrawingUtil mDrawingUtil;

  // long getSID() { return TDInstance.sid; }
  // String getSurvey() { return TDInstance.survey; }
  private DrawingSurface  mOverviewSurface;

  private TDNum mNum;
  private Path mCrossPath;
  private Path mCirclePath;

  // String mName1;  // first name (PLAN)
  // String mName2;  // second name (EXTENDED)

  private boolean mZoomBtnsCtrlOn = false;
  private ZoomButtonsController mZoomBtnsCtrl = null;
  private View mZoomView;
    // ZoomControls mZoomCtrl;
    // ZoomButton mZoomOut;
    // ZoomButton mZoomIn;

    private static final float ZOOM_INC = 1.4f;
    private static final float ZOOM_DEC = 1.0f/ZOOM_INC;

    static final private int MODE_MOVE = 2;
    static final private int MODE_ZOOM = 4;

    final static private int MEASURE_OFF   = 0;
    final static private int MEASURE_START = 1;
    final static private int MEASURE_ON    = 2;
    

    private int mTouchMode = MODE_MOVE;
    private int mOnMeasure = MEASURE_OFF;
    private boolean mIsContinue = false;
    private ArrayList< Point2D > mMeasurePts;

    private float mSaveX;
    private float mSaveY;
    private PointF mOffset  = new PointF( 0f, 0f );
    private PointF mDisplayCenter;
    private float mZoom  = 1.0f;

    private long mSid;     // survey id
    private long mType;    // current plot type
    private boolean mLandscape;
    // private String mFrom;
    // private PlotInfo mPlot1;

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
      mOverviewSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
      // mOverviewSurface.refresh();
      // mZoomCtrl.hide();
      // mZoomBtnsCtrl.setVisible( false );
    }

    // private void resetZoom() 
    // {
    //   int w = mOverviewSurface.width();
    //   int h = mOverviewSurface.height();
    //   mOffset.x = w/4;
    //   mOffset.y = h/4;
    //   mZoom = mApp.mScaleFactor;
    //   // TDLog.Log(TDLog.LOG_PLOT, "zoom one " + mZoom + " off " + mOffset.x + " " + mOffset.y );
    //   if ( mType == PlotType.PLOT_PLAN ) {
    //     float zx = w/(mNum.surveyEmax() - mNum.surveyEmin());
    //     float zy = h/(mNum.surveySmax() - mNum.surveySmin());
    //     mZoom = (( zx < zy )? zx : zy)/40;
    //   } else if ( PlotType.isProfile( mType ) ) { // FIXME OK PROFILE
    //     float zx = w/(mNum.surveyHmax() - mNum.surveyHmin());
    //     float zy = h/(mNum.surveyVmax() - mNum.surveyVmin());
    //     mZoom = (( zx < zy )? zx : zy)/40;
    //   } else {
    //     mZoom = mApp.mScaleFactor;
    //     mOffset.x = 0.0f;
    //     mOffset.y = 0.0f;
    //   }
    //     
    //   // TDLog.Log(TDLog.LOG_PLOT, "zoom one to " + mZoom );
    //     
    //   mOverviewSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    //   // mOverviewSurface.refresh();
    // }

    public void zoomIn()  { changeZoom( ZOOM_INC ); }
    public void zoomOut() { changeZoom( ZOOM_DEC ); }
    // public void zoomOne() { resetZoom( ); }

    // public void zoomView( )
    // {
    //   // TDLog.Log( TDLog.LOG_PLOT, "zoomView ");
    //   DrawingZoomDialog zoom = new DrawingZoomDialog( mOverviewSurface.getContext(), this );
    //   zoom.show();
    // }


    // splay = false
    // selectable = false
    private void addFixedLine( DBlock blk, float x1, float y1, float x2, float y2, 
                               // float xoff, float yoff,
                               boolean splay )
    {
      DrawingPath dpath = null;
      if ( splay ) {
        dpath = new DrawingPath( DrawingPath.DRAWING_PATH_SPLAY, blk, mOverviewSurface.scrapIndex() );
        if ( blk.mClino > TDSetting.mVertSplay ) {
          dpath.setPathPaint( BrushManager.paintSplayXBdot );
        } else if ( blk.mClino < -TDSetting.mVertSplay ) {
          dpath.setPathPaint( BrushManager.paintSplayXBdash );
        } else {
          dpath.setPathPaint( BrushManager.paintSplayXB );
        }
      } else {
        dpath = new DrawingPath( DrawingPath.DRAWING_PATH_FIXED, blk, mOverviewSurface.scrapIndex() );
        dpath.setPathPaint( BrushManager.fixedShotPaint );
      }
      // DrawingUtil.makeDrawingPath( dpath, x1, y1, x2, y2, xoff, yoff );
      DrawingUtil.makeDrawingPath( dpath, x1, y1, x2, y2, splay );
      mOverviewSurface.addFixedPath( dpath, splay, false ); // false: non-selectable
    }

    // --------------------------------------------------------------------------------------

    // @Override // overridden method is empty
    // public void setTheTitle()
    // {
    //   // setTitle( res.getString( R.string.title_move ) );
    // }

  private void computeReferences( int type,
                                  // float xoff, float yoff,
                                  float zoom )
  {
    // Log.v("DistoX", "Overview compute reference. off " + xoff + " " + yoff + " zoom " + zoom );
    // FIXME_OVER
    // mOverviewSurface.clearReferences( type );
    // mOverviewSurface.setManager( DrawingSurface.DRAWING_OVERVIEW, type ); 
    mOverviewSurface.newReferences( DrawingSurface.DRAWING_OVERVIEW, type ); 
   
    mOverviewSurface.addScaleRef( DrawingSurface.DRAWING_OVERVIEW, type );

    // float xoff = 0; float yoff = 0;

    if ( type == PlotType.PLOT_PLAN ) {
      DrawingUtil.addGrid( mNum.surveyEmin(), mNum.surveyEmax(), mNum.surveySmin(), mNum.surveySmax(), mOverviewSurface );
                           // xoff, yoff, mOverviewSurface );
    } else {
      DrawingUtil.addGrid( mNum.surveyHmin(), mNum.surveyHmax(), mNum.surveyVmin(), mNum.surveyVmax(), mOverviewSurface );
                           // xoff, yoff, mOverviewSurface );
    }

    List< NumStation > stations = mNum.getStations();
    List< NumShot >    shots    = mNum.getShots();
    List< NumSplay >   splays   = mNum.getSplays();
    // Log.v("DistoX", "Overview stations " + stations.size() + " shots " + shots.size() + " splays " + splays.size() );

    if ( type == PlotType.PLOT_PLAN ) {
      for ( NumShot sh : shots ) {
        NumStation st1 = sh.from;
        NumStation st2 = sh.to;
        addFixedLine( sh.getFirstBlock(), st1.e, st1.s, st2.e, st2.s, false );
                      // xoff, yoff, false );
      }
      for ( NumSplay sp : splays ) {
        if ( Math.abs( sp.getBlock().mClino ) < TDSetting.mSplayVertThrs ) {
          NumStation st = sp.from;
          addFixedLine( sp.getBlock(), st.e, st.s, sp.e, sp.s, true );
                        // xoff, yoff, true );
        }
      }
      for ( NumStation st : stations ) {
        DrawingStationName dst;
        // dst = mOverviewSurface.addDrawingStationName( null, st, DrawingUtil.toSceneX(st.e,st.s) - xoff,
        //                                                   DrawingUtil.toSceneY(st.e,st.s) - yoff, true, null );
        dst = mOverviewSurface.addDrawingStationName( null, st, DrawingUtil.toSceneX(st.e,st.s), DrawingUtil.toSceneY(st.e,st.s), true, null, null );
      }
    } else { // if ( PlotType.isProfile( type ) // FIXME OK PROFILE
      for ( NumShot sh : shots ) {
        if  ( ! sh.mIgnoreExtend ) {
          NumStation st1 = sh.from;
          NumStation st2 = sh.to;
          addFixedLine( sh.getFirstBlock(), st1.h, st1.v, st2.h, st2.v, false );
                        // xoff, yoff, false );
        }
      } 
      for ( NumSplay sp : splays ) {
        NumStation st = sp.from;
        addFixedLine( sp.getBlock(), st.h, st.v, sp.h, sp.v, true );
                      // xoff, yoff, true );
      }
      for ( NumStation st : stations ) {
        DrawingStationName dst;
        // dst = mOverviewSurface.addDrawingStationName( null, st, DrawingUtil.toSceneX(st.h,st.v) - xoff,
        //                                                   DrawingUtil.toSceneY(st.h,st.v) - yoff, true, null );
        dst = mOverviewSurface.addDrawingStationName( null, st, DrawingUtil.toSceneX(st.h,st.v), DrawingUtil.toSceneY(st.h,st.v), true, null, null );
      }
    }

    mOverviewSurface.commitReferences();

    // FIXME mCheckExtend
    // if ( (! mNum.surveyAttached) && TDSetting.mCheckAttached ) {
    //   TDToast.makeBad( R.string.survey_not_attached );
    // }
  }
    

    // ------------------------------------------------------------------------------
    // BUTTON BAR
  
    private Button[] mButton1;  // primary
    private int mNrButton1 = 4; // main-primary
    private MyHorizontalListView mListView;
    private MyHorizontalButtonView mButtonView1;
    private ListView   mMenu;
    private Button     mImage;
    private boolean onMenu;

    private List< DBlock > mBlockList = null;
  
    public float zoom() { return mZoom; }


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

    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
      super.onCreate(savedInstanceState);

      TDandroid.setScreenOrientation( this );

      mUnitRuler = TDSetting.mUnitMeasure;
      if ( mUnitRuler < 0 ) mUnitRuler = TDSetting.mUnitGrid;

      mCrossPath = new Path();
      mCrossPath.moveTo(10,10);
      mCrossPath.lineTo(-10,-10);
      mCrossPath.moveTo(10,-10);
      mCrossPath.lineTo(-10,10);
      mCirclePath = new Path();
      mCirclePath.addCircle( 0, 0, 10, Path.Direction.CCW );
      mCirclePath.moveTo(-10, 0);
      mCirclePath.lineTo(10, 0);
      mCirclePath.moveTo(0, -10);
      mCirclePath.lineTo(0, 10);

      mMeasurePts = new ArrayList< Point2D >();

      // Display display = getWindowManager().getDefaultDisplay();
      // DisplayMetrics dm = new DisplayMetrics();
      // display.getMetrics( dm );
      // int width = dm widthPixels;
      int width = getResources().getDisplayMetrics().widthPixels;

      setContentView(R.layout.overview_activity);
      mApp = (TopoDroidApp)getApplication();
      mActivity = this;
      Resources res = getResources();
      // mZoom = mApp.mScaleFactor;    // canvas zoom

      mDisplayCenter = new PointF(TopoDroidApp.mDisplayWidth  / 2, TopoDroidApp.mDisplayHeight / 2);

      mOverviewSurface = (DrawingSurface) findViewById(R.id.drawingSurface);
      mOverviewSurface.setZoomer( this );
      mOverviewSurface.setOnTouchListener(this);

      mZoomView = (View) findViewById(R.id.zoomView );
      mZoomBtnsCtrl = new ZoomButtonsController( mZoomView );

      switchZoomCtrl( TDSetting.mZoomCtrl );

      mListView = (MyHorizontalListView) findViewById(R.id.listview);
      mListView.setEmptyPlacholder( true );
      /* int size = */ TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

      mButton1 = new Button[ mNrButton1 + 1 ];
      for ( int k=0; k < mNrButton1; ++k ) {
        mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
        if ( k == 0 ) {
          mBMselect = MyButton.getButtonBackground( mApp, res, izons[k] );
        } else if ( k == 2 ) {
          mBMcontinueNo = MyButton.getButtonBackground( mApp, res, izons[k] );
        }  
        // FIXME_OVER } else if ( k == 2 ) { // IC_PLAN = 2;
        // FIXME_OVER   mBMplan = bm;
      }
      mButton1[mNrButton1] = MyButton.getButton( mActivity, this, R.drawable.iz_empty );
      mBMselectOn   = MyButton.getButtonBackground( mApp, res, R.drawable.iz_measure_on );
      mBMcontinueOn = MyButton.getButtonBackground( mApp, res, R.drawable.iz_polyline );
      // FIXME_OVER mBMextend  = MyButton.getButtonBackground( mApp, res, izons[IC_EXTEND] ); 

      mButtonView1 = new MyHorizontalButtonView( mButton1 );
      mListView.setAdapter( mButtonView1.mAdapter );

      setTheTitle();

      mData         = TopoDroidApp.mData; // new DataHelper( this );
      Bundle extras = getIntent().getExtras();
      if ( extras == null ) { finish(); return; } // extra can be null [ Galaxy S7, Galaxy A30s ] 
      mSid          = extras.getLong( TDTag.TOPODROID_SURVEY_ID );
      // mFrom      = extras.getString( TDTag.TOPODROID_PLOT_FROM );
      mZoom         = extras.getFloat( TDTag.TOPODROID_PLOT_ZOOM );
      mType         = (int)extras.getLong( TDTag.TOPODROID_PLOT_TYPE );
      mLandscape    = extras.getBoolean( TDTag.TOPODROID_PLOT_LANDSCAPE );
      // // mDrawingUtil = mLandscape ? (new DrawingUtilLandscape()) : (new DrawingUtilPortrait());
      // mDrawingUtil = new DrawingUtilPortrait();

      // Log.v("DistoX", "Overview from " + mFrom + " Type " + mType + " Zoom " + mZoom );

      mImage = (Button) findViewById( R.id.handle );
      mImage.setOnClickListener( this );
      TDandroid.setButtonBackground( mImage, MyButton.getButtonBackground( mApp, res, R.drawable.iz_menu ) );
      mMenu = (ListView) findViewById( R.id.menu );
      setMenuAdapter( res );
      closeMenu();
      mMenu.setOnItemClickListener( this );

      doStart();
      // Log.v("DistoX", "Overview offset " + mOffset.x + " " + mOffset.y );

      mOffset.x   += extras.getFloat( TDTag.TOPODROID_PLOT_XOFF );
      mOffset.y   += extras.getFloat( TDTag.TOPODROID_PLOT_YOFF );
      mOverviewSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    }

    @Override
    protected synchronized void onResume()
    {
      super.onResume();
      // mApp.resetLocale(); // FIXME-LOCALE
      doResume();
    }

    @Override
    protected synchronized void onPause() 
    { 
      super.onPause();
      // Log.v("DistoX", "Drawing Activity onPause " + ((mDataDownloader!=null)?"with DataDownloader":"") );
      doPause();
    }

    // @Override // only calls super method
    // protected synchronized void onStart()
    // {
    //   super.onStart();
    // }

    @Override
    protected synchronized void onStop()
    {
      super.onStop();
      mOverviewSurface.setDisplayMode( mOverviewSurface.getDisplayMode() & DisplayMode.DISPLAY_OVERVIEW );
    }

    private void doResume()
    {
      // PlotInfo info = mApp.mData.getPlotInfo( mSid, mName );
      // mOffset.x = info.xoffset;
      // mOffset.y = info.yoffset;
      // mZoom     = info.zoom;
      mOverviewSurface.isDrawing = true;
      switchZoomCtrl( TDSetting.mZoomCtrl );
    }

    private void doPause()
    {
      switchZoomCtrl( 0 );
      mOverviewSurface.isDrawing = false;
    }

// ----------------------------------------------------------------------------

    private void doStart()
    {
      // TDLog.Log( TDLog.LOG_PLOT, "do Start " + mName1 + " " + mName2 );
      // mBlockList = mData.selectAllLegShots( mSid, TDStatus.NORMAL );
      mBlockList = mData.selectAllShots( mSid, TDStatus.NORMAL );
      if ( mBlockList.size() == 0 ) {
        TDToast.makeBad( R.string.few_data );
        finish();
      } else {
        loadFiles( mType ); 
      }
    }

    // boolean mAllSymbols = true;

    private void loadFiles( long type )
    {
      // List< PlotInfo > plots = mApp.mData.selectAllPlotsWithType( mSid, TDStatus.NORMAL, type, landscape );
      List< PlotInfo > plots = TopoDroidApp.mData.selectAllPlotsWithType( mSid, TDStatus.NORMAL, type );

      // Log.v( "DistoX", "Overview plots " + plots.size() );

      // if ( plots.size() < 1 ) { // N.B. this should never happpen
      //   TDToast.makeBad( R.string.few_plots );
      //   finish();
      //   return;
      // }
      // mAllSymbols  = true; // by default there are all the symbols
      // SymbolsPalette missingSymbols = new SymbolsPalette(); 

      NumStation mStartStation = null;

      mOverviewSurface.resetManager( DrawingSurface.DRAWING_OVERVIEW, null, false ); // is_extended = false

      for ( int k=0; k<plots.size(); ++k ) {
        PlotInfo plot = plots.get(k);
        // Log.v( "DistoX", "plot " + plot.name );

        String start = plot.start;
        float xdelta = 0.0f;
        float ydelta = 0.0f;
        if ( k == 0 ) {
          String view  = plot.view;
          // mPlot1 = plot;
          // mPid = plot.id;
          // NOTE Overview only for plan or extended plots
          // float decl = mData.getSurveyDeclination( mSid );
          mNum = new TDNum( mBlockList, start, null, null, 0.0f, null ); // null formatClosure
          mStartStation = mNum.getStation( start );
          // computeReferences( (int)type, mOffset.x, mOffset.y, mZoom );
          computeReferences( (int)type, mZoom );
          // Log.v( "DistoX", "Overview num stations " + mNum.stationsNr() + " shots " + mNum.shotsNr() );
        } else {
          NumStation st = mNum.getStation( start );
          if ( st == null ) continue;
          if ( type == PlotType.PLOT_PLAN ) {
            xdelta = st.e - mStartStation.e; // FIXME SCALE FACTORS ???
            ydelta = st.s - mStartStation.s;
          } else {
            xdelta = st.h - mStartStation.h;
            ydelta = st.v - mStartStation.v;
          }
        }
        xdelta *= DrawingUtil.SCALE_FIX;
        ydelta *= DrawingUtil.SCALE_FIX;
        // Log.v( "DistoX", " delta " + xdelta + " " + ydelta );

        // now try to load drawings from therion file
        String fullName = TDInstance.survey + "-" + plot.name;
        // Log.v( "DistoXX", "load tdr file " + fullName );

        String tdr = TDPath.getTdrFileWithExt( fullName );
        mOverviewSurface.addLoadDataStream( tdr, xdelta, ydelta, /* null, */ fullName ); // save plot fullname in paths
      }

      // if ( ! mAllSymbols ) {
      //   String msg = missingSymbols.getMessage( getResources() );
      //   TDLog.Log( TDLog.LOG_PLOT, "Missing " + msg );
      //   TDToast.makeBad( "Missing symbols \n" + msg );
      //   // (new MissingDialog( this, this, msg )).show();
      //   // finish();
      // }

      // // resetZoom();
      // resetReference( mPlot1 );
   }

   // called only by export menu
   private void saveWithExt( final String ext )
   {
     TDNum num = mNum;
     final String fullname = TDInstance.survey + ( (mType == PlotType.PLOT_PLAN )? "-p" : "-s" );
     // TDLog.Log( TDLog.LOG_IO, "export plot type " + mType + " with extension " + ext );
     // Log.v( "DistoXX", "export th2 file " + fullname );
     DrawingCommandManager manager = mOverviewSurface.getManager( DrawingSurface.DRAWING_OVERVIEW );

     if ( ext.equals("th2") ) {
       Handler th2Handler = new Handler() {
          @Override public void handleMessage(Message msg) {
            if (msg.what == 661 ) {
              TDToast.make( String.format( getString(R.string.saved_file_1), (fullname + "." + ext) ) );
            } else {
              TDToast.makeBad( R.string.saving_file_failed );
            }
          }
       };
       // parent can be null because this is user-requested EXPORT
       // azimuth = 0
       // rotate  = 0
       (new SavePlotFileTask( this, null, th2Handler, mNum, manager, fullname, mType, 0, PlotSave.OVERVIEW, 0 )).execute();
     } else {
       GeoReference station = null;
       if ( mType == PlotType.PLOT_PLAN && ext.equals("shp") ) {
        String origin = mNum.getOriginStation();
        station = TDExporter.getGeolocalizedStation( mSid, mData, 1.0f, true, origin );
       }
       SurveyInfo info = mData.selectSurveyInfo( mSid );
       // null PlotInfo, null FixedInfo
       (new ExportPlotToFile( this, info, null, null, mNum, manager, mType, fullname, ext, true, station )).execute();
     }
   }

  // interface IExporter
  public void doExport( String export_type )
  {
    int index = TDConst.plotExportIndex( export_type );
    switch ( index ) {
      case TDConst.DISTOX_EXPORT_TH2: saveWithExt( "th2" ); break;
      case TDConst.DISTOX_EXPORT_DXF: saveWithExt( "dxf" ); break; 
      case TDConst.DISTOX_EXPORT_SVG: saveWithExt( "svg" ); break;
      case TDConst.DISTOX_EXPORT_SHP: saveWithExt( "shp" ); break;
      case TDConst.DISTOX_EXPORT_XVI: saveWithExt( "xvi" ); break;
    }
  }

   // private void saveReference( PlotInfo plot, long pid )
   // {
   //   // Log.v("DistoX", "save ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
   //   plot.xoffset = mOffset.x;
   //   plot.yoffset = mOffset.y;
   //   plot.zoom    = mZoom;
   //   mData.updatePlot( pid, mSid, mOffset.x, mOffset.y, mZoom );
   // }

   // private void resetReference( PlotInfo plot )
   // {
   //   mOffset.x = plot.xoffset; 
   //   mOffset.y = plot.yoffset; 
   //   mZoom     = plot.zoom;    
   //   // Log.v("DistoX", "reset ref " + mOffset.x + " " + mOffset.y + " " + mZoom );
   //   mOverviewSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
   //   // mOverviewSurface.refresh();
   // }

  private float mSave0X, mSave0Y;
  private float mSave1X, mSave1Y;

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

  private float spacing(MotionEventWrap ev)
  {
    int np = ev.getPointerCount();
    if ( np < 2 ) return 0.0f;
    float x = ev.getX(1) - ev.getX(0);
    float y = ev.getY(1) - ev.getY(0);
    return TDMath.sqrt(x*x + y*y);
  }

  private void saveEventPoint(MotionEventWrap ev)
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

  
  private void shiftByEvent(MotionEventWrap ev)
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
      mOverviewSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
    }

  }

  private void moveCanvas( float x_shift, float y_shift )
  {
    if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
      mOffset.x += x_shift / mZoom;                // add shift to offset
      mOffset.y += y_shift / mZoom; 
      mOverviewSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
      // mOverviewSurface.refresh();
    }
  }


  private float oldDist = 0;
  private float mStartX = 0;
  private float mStartY = 0;
  private float mBaseX = 0;
  private float mBaseY = 0;

  
  private float deltaX( float x1, float x0 ) 
  { return (  (x1 - x0) / DrawingUtil.SCALE_FIX ) / mUnitRuler; }

  private float deltaY( float y1, float y0 )
  { return ( -(y1 - y0) / DrawingUtil.SCALE_FIX ) / mUnitRuler; }

  private double angleBase( float bx, float by )
  {
    double ba = Math.atan2( bx, by ) * 180 / Math.PI;
    if ( ba < 0 ) ba += 360;

    if ( mType == PlotType.PLOT_PLAN ) {
      /* nothing */
    } else {
      if ( ba <= 180 ) {
        ba = 90 - ba;
      } else {
        ba = ba - 270;
      } 
    }
    ba *= TDSetting.mUnitAngle;
    return ba;
  }

  public boolean onTouch( View view, MotionEvent rawEvent )
  {
    float d0 = TDSetting.mCloseCutoff + TDSetting.mSelectness / mZoom;
    checkZoomBtnsCtrl();

    MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingWindow onTouch() " );
    // dumpEvent( event );

    float x_canvas = event.getX();
    float y_canvas = event.getY();
    Log.v("DistoX-TOUCH", "touch canvas " + x_canvas + " " + y_canvas + " center_y " + (DrawingUtil.CENTER_Y*2-20) ); 

    // if ( mZoomBtnsCtrlOn && y_canvas > DrawingUtil.CENTER_Y*2-20 ) {
    //   mZoomBtnsCtrl.setVisible( true );
    //   // mZoomCtrl.show( );
    // }

    float x_scene = x_canvas/mZoom - mOffset.x;
    float y_scene = y_canvas/mZoom - mOffset.y;
    // Log.v("DistoX", "touch scene " + x_scene + " " + y_scene );

    int action = event.getAction() & MotionEvent.ACTION_MASK;

    if (action == MotionEvent.ACTION_POINTER_DOWN) {
      if ( mIsContinue ) {
        setOnMeasure( MEASURE_OFF );
      } else {
        if ( mOnMeasure == MEASURE_ON ) mOnMeasure = MEASURE_START;
      }
      mTouchMode = MODE_ZOOM;
      oldDist = spacing( event );
      saveEventPoint( event );
    } else if ( action == MotionEvent.ACTION_POINTER_UP) {
      if ( mIsContinue ) {
        setOnMeasure( MEASURE_START );
      } else {
        if ( mOnMeasure == MEASURE_START ) mOnMeasure = MEASURE_ON;
      }
      mTouchMode = MODE_MOVE;
      /* nothing */

    // ---------------------------------------- DOWN

    } else if (action == MotionEvent.ACTION_DOWN) {
      // check side-drag and zoom controls
      if ( y_canvas > TopoDroidApp.mBorderBottom ) {
        if ( mZoomBtnsCtrlOn && x_canvas > TopoDroidApp.mBorderInnerLeft && x_canvas < TopoDroidApp.mBorderInnerRight ) {
          mTouchMode = MODE_ZOOM;
          mZoomBtnsCtrl.setVisible( true );
          // mZoomCtrl.show( );
          return true;
        } else if ( TDSetting.mSideDrag ) {
          mTouchMode = MODE_ZOOM;
          return true;
        }
      } else if ( TDSetting.mSideDrag && ( x_canvas > TopoDroidApp.mBorderRight || x_canvas < TopoDroidApp.mBorderLeft ) ) {
        mTouchMode = MODE_ZOOM;
        return true;
      }

      mSaveX = x_canvas; // FIXME-000
      mSaveY = y_canvas;
      if ( mOnMeasure == MEASURE_START ) {
        mStartX = x_canvas/mZoom - mOffset.x;
        mStartY = y_canvas/mZoom - mOffset.y;
        mBaseX = mStartX;
        mBaseY = mStartY;
        mOnMeasure = MEASURE_ON;
        // add reference point
        DrawingPath path1 = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH, null, -1 );
        path1.setPathPaint( BrushManager.highlightPaint );
        path1.makePath( mCirclePath, new Matrix(), mStartX, mStartY );
        // Log.v("DistoX", "first ref " + mStartX + " " + mStartY );
        mOverviewSurface.setFirstReference( path1 );
        if ( mIsContinue ) {
          mTotal   = 0;
          mDDtotal = 0;
          DrawingPath path = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH, null, -1 );
          path.setPathPaint( BrushManager.fixedBluePaint );
          path.makePath( null, new Matrix(), mStartX, mStartY ); // default path 
          // path.mPath.moveTo( mStartX, mStartY ); FIXME-PATH
          mOverviewSurface.setSecondReference( path );
	  mMeasurePts.clear();
	  mMeasurePts.add( new Point2D( mStartX, mStartY ) );
        }
      } else if ( mOnMeasure == MEASURE_ON ) {
        // FIXME use scene values
        float x = x_canvas/mZoom - mOffset.x;
        float y = y_canvas/mZoom - mOffset.y;

        // segment displacement
        float dx = deltaX(x, mStartX);
        float dy = deltaY(y, mStartY);

        // total displacement, with respect to base
        float bx = deltaX(x, mBaseX);
        float by = deltaY(y, mBaseY);

        // angle with respect to base
        double ba = angleBase( bx, by );
        float dd = TDMath.sqrt( dx * dx + dy * dy );
        float bb = TDMath.sqrt( bx * bx + by * by );

        String format = ( mType == PlotType.PLOT_PLAN )?
          getResources().getString( R.string.format_measure_plan ) :
          getResources().getString( R.string.format_measure_profile );

        if ( mIsContinue ) {
          mDDtotal += dd;
          mTotal   ++;
          mOverviewSurface.addSecondReference( x, y );
          mStartX = x;
          mStartY = y;
	  mMeasurePts.add( new Point2D( mStartX, mStartY ) );
        } else {
          mDDtotal = dd;
          mTotal   = 1;
          // replace target point
          DrawingPath path = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH, null, -1 );
          path.setPathPaint( BrushManager.fixedBluePaint );
          path.makePath( mCrossPath, new Matrix(), x, y );
          path.mPath.moveTo( mStartX, mStartY );
          path.mPath.lineTo( x, y );
          mOverviewSurface.setSecondReference( path );
        }
        mActivity.setTitle( String.format( format, bb, mDDtotal, bx, by, ba ) );
      }
    // ---------------------------------------- MOVE

    } else if ( action == MotionEvent.ACTION_MOVE ) {
      if ( mTouchMode == MODE_MOVE) {
        float x_shift = x_canvas - mSaveX; // compute shift
        float y_shift = y_canvas - mSaveY;
        if ( mOnMeasure == MEASURE_OFF ) {
          if ( Math.abs( x_shift ) < 60 && Math.abs( y_shift ) < 60 ) {
            mOffset.x += x_shift / mZoom;                // add shift to offset
            mOffset.y += y_shift / mZoom; 
            mOverviewSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
          }
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
        shiftByEvent( event );
      }

    // ---------------------------------------- UP

    } else if (action == MotionEvent.ACTION_UP) {
      if ( onMenu ) {
        closeMenu();
        return true;
      }

      if ( mTouchMode == MODE_ZOOM ) {
        mTouchMode = MODE_MOVE;
      // } else {
        // NOTHING
        // if ( mOnMeasure == MEASURE_OFF ) {
        //   // float x_shift = x_canvas - mSaveX; // compute shift
        //   // float y_shift = y_canvas - mSaveY;
        // } else {
        // }
      }
    }
    return true;
  }


  private Button makeButton( String text )
  {
    Button myTextView = new Button( mActivity );
    myTextView.setHeight( 42 );

    myTextView.setText( text );
    myTextView.setTextColor( TDColor.WHITE );
    myTextView.setTextSize( TypedValue.COMPLEX_UNIT_DIP, 16 );
    myTextView.setBackgroundColor( TDColor.VERYDARK_GRAY );
    myTextView.setSingleLine( true );
    myTextView.setGravity( 0x03 ); // left
    myTextView.setPadding( 4, 4, 4, 4 );
    // Log.v(TopoDroidApp.TAG, "makeButton " + text );
    return myTextView;
  }

    /* FIXME_OVER
    private void switchPlotType()
    {
      if ( mType == PlotType.PLOT_PLAN ) {
        // saveReference( mPlot1, mPid1 );
        // mPid  = mPid2;
        mType = PlotInfo.mPlot2.type; 
        TDandroid.setButtonBackground( mButton1[ BTN_PLOT ], mBMextend );
        mOverviewSurface.setManager( DrawingSurface.DRAWING_PLAN, (int)mType ); 
        resetReference( mPlot2 );
      } else if ( mType == PlotType.PLOT_EXTENDED ) { // PROJECTED not supported on overview
        // saveReference( mPlot2, mPid2 );
        // mPid  = mPid1;
        // mName = mName1;
        mType = mPlot1.type;
        TDandroid.setButtonBackground( mButton1[ BTN_PLOT ], mBMplan );
        mOverviewSurface.setManager( DrawingSurface.DRAWING_PROFILE, (int)mType );
        resetReference( mPlot1 );
      }
    }
    */

    private void setOnMeasure( int measure )
    {
      mOnMeasure = measure;
      if ( mOnMeasure == MEASURE_OFF ) {
        TDandroid.setButtonBackground( mButton1[IC_SELECT], mBMselect );
        mOverviewSurface.setFirstReference( null );
        mOverviewSurface.setSecondReference( null );
      } else if ( mOnMeasure == MEASURE_START ) {
        TDandroid.setButtonBackground( mButton1[IC_SELECT], mBMselectOn );
        mDDtotal = 0;
        mTotal = 0;
        mOverviewSurface.setSecondReference( null );
      }
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
      if ( b == mButton1[0] ) { // measure
        if ( mOnMeasure == MEASURE_OFF ) {
          setOnMeasure( MEASURE_START );
        } else {
          setOnMeasure( MEASURE_OFF );
        }
      } else if ( b == mButton1[1] ) { // references
        new OverviewModeDialog( mActivity, this, mOverviewSurface ).show();
      } else if ( b == mButton1[2] ) { // continue
        toggleIsContinue( );
      } else if ( b == mButton1[3] ) { // undo
	undoMeasurePoint();

      // FIXME_OVER } else if ( b == mButton1[2] ) { // toggle plan/extended
      // FIXME_OVER   switchPlotType();
      }
    }

  private void undoMeasurePoint()
  {
    if ( ! mIsContinue ) return;
    int sz = mMeasurePts.size() - 1;
    if ( sz <= 0 ) return;
    Point2D pt1 = mMeasurePts.get(sz);
    Point2D pt0 = mMeasurePts.get(sz-1);
    float dx = deltaX( pt1.x, pt0.x );
    float dy = deltaX( pt1.y, pt0.y );
    float bx = deltaX( pt1.x, mBaseX );
    float by = deltaX( pt1.y, mBaseY );
    double ba = angleBase( bx, by );
    float dd = TDMath.sqrt( dx * dx + dy * dy );
    float bb = TDMath.sqrt( bx * bx + by * by );
    mDDtotal -= dd;
    mTotal   --;
    mStartX = pt0.x;
    mStartY = pt0.y;
    mMeasurePts.remove( sz );
    DrawingPath path = new DrawingPath( DrawingPath.DRAWING_PATH_NORTH, null, -1 );
    path.setPathPaint( BrushManager.fixedBluePaint );
    path.makePath( null, new Matrix(), mBaseX, mBaseY ); // default-path
    // path.mPath.moveTo( mBaseX, mBaseY ); FIXME-PATH
    mOverviewSurface.setSecondReference( path );
    for ( int k=1; k<sz; ++k ) {
      Point2D pt = mMeasurePts.get(k);
      mOverviewSurface.addSecondReference( pt.x, pt.y );
    }

    String format = ( mType == PlotType.PLOT_PLAN )?
      getResources().getString( R.string.format_measure_plan ) :
      getResources().getString( R.string.format_measure_profile );

    mActivity.setTitle( String.format( format, bb, mDDtotal, bx, by, ba ) );
  }

  private void toggleIsContinue( )
  {
    mIsContinue = ! mIsContinue;
    TDandroid.setButtonBackground( mButton1[IC_CONTINUE], (mIsContinue? mBMcontinueOn : mBMcontinueNo) );
  }


  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
        return true;
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


  private void setMenuAdapter( Resources res )
  {
    ArrayAdapter< String > menu_adapter = new ArrayAdapter<>(mActivity, R.layout.menu );

    menu_adapter.add( res.getString( menus[0] ) );
    if ( TDLevel.overExpert ) menu_adapter.add( res.getString( menus[1] ) );
    menu_adapter.add( res.getString( menus[2] ) );
    menu_adapter.add( res.getString( menus[3] ) );
    mMenu.setAdapter( menu_adapter );
    mMenu.invalidate();
  }

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
    } else if ( TDLevel.overExpert && p++ == pos ) { // EXPORT THERION
      new ExportDialogPlot( mActivity, this, TDConst.mOverviewExportTypes, R.string.title_plot_save, 1 ).show();
      // saveWithExt( "th2" );
    } else if ( p++ == pos ) { // OPTIONS
      Intent intent = new Intent( mActivity, com.topodroid.prefs.TDPrefActivity.class );
      intent.putExtra( TDPrefCat.PREF_CATEGORY, TDPrefCat.PREF_CATEGORY_PLOT );
      mActivity.startActivity( intent );
    } else if ( p++ == pos ) { // HELP
      new HelpDialog(mActivity, izons, menus, help_icons, help_menus, mNrButton1, help_menus.length, getResources().getString( HELP_PAGE ) ).show();
    }
  }


  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
    }
  }

  public void checkZoomBtnsCtrl()
  {
    if ( TDSetting.mZoomCtrl == 2 && ! mZoomBtnsCtrl.isVisible() ) {
      mZoomBtnsCtrl.setVisible( true );
    }
  }

  @Override
  public void onConfigurationChanged( Configuration new_cfg )
  {
    super.onConfigurationChanged( new_cfg );
    mOverviewSurface.setTransform( this, mOffset.x, mOffset.y, mZoom, mLandscape );
  }

}
