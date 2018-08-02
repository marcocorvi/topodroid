/* @file SketchWindow.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid main 3d sketch activity
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.content.pm.ActivityInfo;

// import android.graphics.Bitmap;
// import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
// import android.graphics.PointF;
// import android.graphics.Path;
// import android.graphics.Path.Direction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
// import android.os.Message;
// import android.view.Menu;
// import android.view.SubMenu;
// import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
// import android.view.ViewGroup;
import android.view.KeyEvent;
// for FRAGMENT
// import android.view.ViewGroup;
// import android.view.LayoutInflater;

import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Button;
// import android.widget.ZoomControls;
// import android.widget.ZoomButton;
// import android.widget.ZoomButtonsController;
// import android.widget.ZoomButtonsController.OnZoomListener;
// import android.widget.Toast;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

// import android.hardware.SensorManager;

// import java.io.File;
import java.io.FileWriter;
// import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;

import java.util.List;
import java.util.ArrayList;
// import java.util.Locale;

// import android.graphics.Bitmap;
// import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

// import android.util.Log;

/**
 */
public class SketchWindow extends ItemDrawer
                            implements View.OnTouchListener
                                     , View.OnClickListener
                                     , OnItemClickListener
                                     // , View.OnLongClickListener
                                     // , DrawingPointPickerDialog.OnPointSelectedListener FIXME
                                     // , DrawingLinePickerDialog.OnLineSelectedListener
                                     // , DrawingAreaPickerDialog.OnAreaSelectedListener
                                     // , OnZoomListener
                                     , ILabelAdder
                                     , ILister
                                     , IBearingAndClino
                                     , IExporter
                                     , IFilterClickHandler
{
  static final String TAG = "DistoX";

  private boolean mLoaded = false;

  private TopoDroidApp mApp;
  private DataDownloader mDataDownloader;
  private Sketch3dInfo mInfo;
  private SketchPainter mPainter;

  private SketchDrawingSurface mSketchSurface;

  private DrawingLinePath mCurrentLinePath;
  // private DrawingAreaPath mCurrentAreaPath;

  private static final long mType = PlotInfo.PLOT_SKETCH_3D;

  // private boolean canRedo;
  private DistoXNum mNum;
  private float mDecl;
  private int mPointCnt; // counter of points in the currently drawing line

  // private boolean mAllSymbols; // whether the library has all the symbols of the plot
  // private boolean mDoMakeSurface; // whether a surface can be safely made

  // private int mSectionType;     // current section type
  // private boolean mIsInSection; // whether the user is drawing a section curve

  // private Button selectBtn;

  private Button[] mButton1; // primary: draw edit select refs surface download note info one step ( shot join )
  private Button[] mButton2; // draw:    draw edit select undo redo symbol 
  // private Button[] mButton3; // edit     draw edit select refineC refineS cut stretch extrude
  private Button[] mButton4; // select   draw edit select next prev cutP refineP

  HorizontalButtonView mButtonView1;
  HorizontalButtonView mButtonView2;
  // HorizontalButtonView mButtonView3;
  HorizontalButtonView mButtonView4;

  private int mNrButton1 = 7;          // main-primary: 2 + 5 = 7
  private int mNrButton2 = 5;          // draw          2 + 3 = 5
  // private int mNrButton3 = 6;       // edit
  private int mNrButton4 = 6;          // select        2 + 4 = 6
  HorizontalListView mListView;
  ListView   mMenu;
  Button     mImage;
  // HOVER 
  // MyMenuAdapter mMenuAdapter;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;

  static int BTN_DRAW   = 0;
  static int BTN_SELECT = 1;
  static int BTN_SELECT_MODE = 2;
  static int BTN_SELECT_SIZE = 3;

  private BitmapDrawable mBMedit_ok;
  private BitmapDrawable mBMedit_no;
  private BitmapDrawable mBMselect_ok;
  private BitmapDrawable mBMselect_no;

  private BitmapDrawable mBMsmall;
  private BitmapDrawable mBMmedium;
  private BitmapDrawable mBMlarge;

  // private BitmapDrawable mBMprev;
  // private BitmapDrawable mBMnext;

  private BitmapDrawable mBMselectAll;
  private BitmapDrawable mBMselectPoint;
  private BitmapDrawable mBMselectLine;
  private BitmapDrawable mBMselectArea;
  private BitmapDrawable mBMselectShot;
  private BitmapDrawable mBMselectStation;

  private static int izons[] = { 
                        R.drawable.iz_edit,   // DRAW
                        R.drawable.iz_select, // SELECT / STEP
                        // R.drawable.iz_move,  
                        // R.drawable.iz_contour,
                        // R.drawable.iz_one,           // 
                        R.drawable.iz_mode,          // MOVE buttons
                        R.drawable.iz_plan,          // 
                        R.drawable.iz_download,      // 
                        R.drawable.iz_note,
                        R.drawable.iz_info,          //  

                        R.drawable.iz_undo,          // DRAW buttons
                        R.drawable.iz_redo,
                        R.drawable.iz_palette,       

                        // R.drawable.iz_refinet,       // 13
                        // R.drawable.iz_refinec,       // 14
                        // R.drawable.iz_refines,       
                        // R.drawable.iz_cut,
                        // R.drawable.iz_stretch,       // 16

                        R.drawable.iz_erase_all,     // 
                        R.drawable.iz_medium,
                        R.drawable.iz_back,          // SELECT buttons
                        R.drawable.iz_forw,
                        // R.drawable.iz_refinep,
                        // R.drawable.iz_note           // 21 Point: delete(cut), Leg: step
                     };

  private static int izons_ok[] = { 
                        R.drawable.iz_edit_ok, 
                        // R.drawable.iz_eraser_ok,
                        R.drawable.iz_select_ok };

  private static int GREEN_BTN = 2;

  private static int menus[] = {
                        R.string.menu_export,       // 21 <-- menus
                        R.string.menu_palette, 
                        R.string.menu_zoom_fit,
                        R.string.menu_head,
                        R.string.menu_delete,
                        R.string.menu_clear,
                        R.string.menu_options,
                        R.string.menu_help           // 26
                      };

  private static int help_icons[] = {
                        R.string.help_draw,          // GREEN_BTN
                        // R.string.help_contour,
                        R.string.help_edit,
                        // R.string.help_move,

                        R.string.help_refs,          // BUTTON 1
                        R.string.help_surface,
                        R.string.help_download,
                        R.string.help_note,      
                        R.string.help_stats,      

                        R.string.help_undo,          // BUTTON 2
                        R.string.help_redo,        
                        R.string.help_symbol_plot,   

                        // R.string.help_refine_triangle, // BUTTON 3
                        // R.string.help_refine_center,
                        // R.string.help_refine_sides,
                        // R.string.help_cut_model,    
                        // R.string.help_stretch_model,

                        R.string.help_previous,     // BUTTON 4
                        R.string.help_next,
                        // R.string.help_refine_point,
                        // R.string.help_note_plot
                      };

  private static int help_menus[] = {
                        R.string.help_save_model,   
                        R.string.help_symbol,      
                        R.string.help_one_model,   
                        R.string.help_head_model,
                        R.string.help_trash_model,
                        R.string.help_clear_model,
                        R.string.help_prefs,
                        R.string.help_help
                      };

  private static final int HELP_PAGE = R.string.SketchWindow;

  private int mSelectMode = Drawing.FILTER_ALL;
  private int mSelectScale = 0;

  private DrawingBrush mCurrentBrush;
  // private Path  mCurrentPath;

  private String mFullName;
    
  TimerTask mTimer;

  // ZoomButtonsController mZoomBtnsCtrl;
  // View mZoomView;
  // ZoomControls mZoomCtrl;

  private float oldDist;  // zoom pointer-spacing

  public int mMode       = SketchDef.MODE_MOVE;
  private int mTouchMode = SketchDef.TOUCH_MOVE;
  private int mEdit      = SketchDef.EDIT_NONE;

  // private int mSelect    = SketchDef.SELECT_NONE;

  private float mSaveX;
  private float mSaveY;
  private float mStartX; // start point (scene coords)
  private float mStartY;
  private boolean mMoveSelected; // whether it is moving the selected vertex/point

  private DataHelper mData;
  private long mSid; // survey id
  private long mPid; // sketch id

  private SketchModel mModel;
  // private SketchCompassSensor mCompass;

  private float mSelectSize;


  // ---------------------------------------------------------------------------
  // helper private methods 

/*
  private void setSymbolButton()
  {
      Resources res = getResources();
      switch ( mSymbol ) {
      case Symbol.POINT:
        symbolBtn.setText( res.getString(R.string.btn_point ) );
        break;
      case Symbol.AREA:
        symbolBtn.setText( res.getString(R.string.btn_area ) );
        break;
      // case Symbol.LINE:
      default:
        symbolBtn.setText( res.getString(R.string.btn_line ) );
        break;
      }
  }
*/  

  @Override
  public void setTheTitle()
  {
    Resources res = getResources();
    // String dir = mInfo.getDirectionString();
    String symbol_name = // ( mIsInSection) ? "section" :
        ( mSymbol == Symbol.POINT )? res.getString(R.string.POINT) + BrushManager.mPointLib.getSymbolName( mCurrentPoint )
      : ( mSymbol == Symbol.LINE )? res.getString(R.string.LINE)
      : res.getString(R.string.AREA) + BrushManager.mAreaLib.getSymbolName( mCurrentArea );

    setTitle( String.format( res.getString( R.string.title_sketch), 
      mInfo.getShotString(),
      mInfo.getDirectionString(),
        ( mMode == SketchDef.MODE_MOVE )? res.getString( R.string.title_move )
      : ( mMode == SketchDef.MODE_DRAW )? res.getString( R.string.title_draw )
      // : ( mMode == SketchDef.MODE_EDIT )? res.getString( R.string.title_contour )
      : ( mMode == SketchDef.MODE_SELECT )? res.getString( R.string.title_select )
      // : ( mMode == SketchDef.MODE_STEP )? res.getString( R.string.title_step )
      // : ( mMode == SketchDef.MODE_HEAD )? res.getString( R.string.title_head )
      : "--",
      ( mMode == SketchDef.MODE_DRAW )? symbol_name : ""
    ) );


    // switch ( mSelect ) {
    //   case SketchDef.SELECT_SECTION:
    //     selectBtn.setText( res.getString(R.string.btn_section ) );
    //     // selectBtn.setBackgroundColor( TDColor.GREEN );
    //     break;
    //   case SketchDef.SELECT_STEP:
    //     selectBtn.setText( res.getString(R.string.btn_step ) );
    //     // selectBtn.setBackgroundColor( TDColor.ORANGE );
    //     break;
    //   case SketchDef.SELECT_SHOT:
    //     selectBtn.setText( res.getString(R.string.btn_shot ) );
    //     // selectBtn.setBackgroundColor( TDColor.FIXED_RED );
    //     break;
    //   case SketchDef.SELECT_JOIN:
    //     selectBtn.setText( res.getString(R.string.btn_join ) );
    //     // selectBtn.setBackgroundColor( TDColor.BLUE );
    //     break;
    // }

    switch ( mMode ) {
      case SketchDef.MODE_MOVE:
        mListView.setAdapter( mButtonView1.mAdapter );
        break;
      case SketchDef.MODE_DRAW:
        mListView.setAdapter( mButtonView2.mAdapter );
        break;
      // case SketchDef.MODE_EDIT:
      //   mSymbol = Symbol.LINE;
      //   mListView.setAdapter( mButtonView3.mAdapter );
      //   break;
      case SketchDef.MODE_SELECT:
        mListView.setAdapter( mButtonView4.mAdapter );
        break;
      // case SketchDef.MODE_STEP:
      //   mListView.setAdapter( mButtonView4.mAdapter );
      //   break;

      // case SketchDef.MODE_HEAD:
      //   mListView.setAdapter( mButtonView1.mAdapter );
      //   break;
    }
    mListView.invalidate();
  }

  void setMode( int mode )
  {
    if ( mode == mMode ) mode = SketchDef.MODE_MOVE;
    mMode = mode;
    mModel.mActivityMode = mMode;
    setTheTitle();
  }

  private void alertMakeSurface( )
  {
    TopoDroidAlertDialog.makeAlert( mActivity, getResources(), R.string.make_surface,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doMakeSurface( true );
        }
      }
    );
  }

  // private void alertMissingSymbols()
  // {
  //   TopoDroidAlertDialog.makeAlert( mActivity, getResources(), R.string.missing-symbols,
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         mAllSymbols = true;
  //       }
  //     } );
  // }

  // -------------------------------------------------------------------------
  // SYMBOL-CHOICE methods
  
  public void areaSelected( int k, boolean update_recent )
  {
    if ( k >= 0 && k < BrushManager.mAreaLib.mSymbolNr ) {
      mSymbol = Symbol.AREA;
      mCurrentArea = k;
    }
    setTheTitle();
  }

  public void lineSelected( int k, boolean update_recent ) 
  {
    if ( k >= 0 && k < BrushManager.mLineLib.mSymbolNr ) {
      mSymbol = Symbol.LINE;
      mCurrentLine = k;
    }
    setTheTitle();
  }

  public void pointSelected( int p, boolean update_recent )
  {
    if ( p >= 0 && p < BrushManager.mPointLib.mSymbolNr ) {
      mSymbol = Symbol.POINT;
      mCurrentPoint = p;
    }
    setTheTitle();
  }

  // ---------------------------------------------------------------
  // ZOOM button controls - and methods 

  // @Override
  // public void onVisibilityChanged(boolean visible)
  // {
  //   mZoomBtnsCtrl.setVisible( visible );
  // }

  // private void rotateInfoBy( float angle ) 
  // { mInfo.azimuth += angle; }
  // private void tiltInfoBy( float clino )
  // {
  //   clino += mInfo.clino;
  //   if ( clino > 90 ) clino = 90;
  //   if ( clino < -90 ) clino = -90;
  //   mInfo.clino = clino;
  // }

  private void changeZoom( float f ) 
  {
    mInfo.changeZoom3d( f ); // , mDisplayCenter );
    mModel.setTransform( mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d );
  }

  /** set info offsets and zoom
   * and set the model transform (matrix) accordingly
   */
  private void resetZoom() 
  {
    mInfo.resetZoom3d( TopoDroidApp.mDisplayWidth/(10*TopoDroidApp.mScaleFactor),
                       TopoDroidApp.mDisplayHeight/(10*TopoDroidApp.mScaleFactor),
                       10 * TopoDroidApp.mScaleFactor );
    mModel.setTransform( mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d );
  }

  // public void zoomIn()  { changeZoom( SketchDef.ZOOM_INC ); }
  // public void zoomOut() { changeZoom( SketchDef.ZOOM_DEC ); }
  public void zoomOne() { resetZoom( ); }

  // @Override
  // public void onZoom( boolean zoomin )
  // {
  //   if ( zoomin ) {
  //     zoomIn();
  //   } else {
  //     zoomOut();
  //   }
  // }

  // -----------------------------------------------------------------------------------
  // OUTPUT

  // void savePng()
  // {
  //   Bitmap bitmap = mSketchSurface.getBitmap();
  //   if ( bitmap == null ) {
  //     TDToast.make( R.string.null_bitmap );
  //   } else {
  //     new ExportBitmapToFile( mActivity, mSaveHandler, mSketchSurface.getBitmap(), mFullName ).execute();
  //     TDToast.make( getString(R.string.saved_file_1) + mFullName + ".png" );
  //   }
  // }

  // private class ExportBitmapToFile extends AsyncTask<Intent,Void,Boolean> 
  // {
  //   private Context mContext;
  //   private Handler mHandler;
  //   private Bitmap mBitmap;
  //   private String mFullName;

  //   public ExportBitmapToFile( Context context, Handler handler, Bitmap bitmap, String name )
  //   {
  //      mContext  = context;
  //      mBitmap   = bitmap;
  //      mHandler  = handler;
  //      mFullName = name;
  //      // TDLog.Log( TDLog.LOG_PLOT, "ExportBitmapToFile " + mFullName );
  //   }

  //   @Override
  //   protected Boolean doInBackground(Intent... arg0)
  //   {
  //     try {
  //       String filename = mApp.getPngFileWithExt( mFullName );
  //       TDPath.checkPath( filename );
  //       final FileOutputStream out = new FileOutputStream( filename );
  //       mBitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
  //       out.flush();
  //       out.close();
  //       return true;
  //     } catch (Exception e) {
  //       e.printStackTrace();
  //     }
  //     //mHandler.post(completeRunnable);
  //     return false;
  //   }


  //   @Override
  //   protected void onPostExecute(Boolean bool) {
  //       super.onPostExecute(bool);
  //       if ( bool ){
  //           mHandler.sendEmptyMessage(1);
  //       }
  //   }
  // }

  class SaveTh3File extends AsyncTask<Intent,Void,Boolean> // FIXME LEAK
  {
    private Context mContext;
    private Handler mHandler;
    private SketchModel mModel;
    private String mFullName;

    SaveTh3File( Context context, Handler handler, SketchModel model, String name )
    {
       mContext  = context;
       mHandler  = handler;
       mModel    = model;
       mFullName = name;
       // TDLog.Log( TDLog.LOG_PLOT, "Save Th3 File " + mFullName );
    }

    @Override
    protected Boolean doInBackground(Intent... arg0)
    {
      try {
        String filename = TDPath.getTh3FileWithExt( mFullName );
	// TDLog.Log( TDLog.LOG_IO, "save sketch th3 file " + filename );
        TDPath.checkPath( filename );
        FileWriter writer = new FileWriter( filename );
        BufferedWriter out = new BufferedWriter( writer );
        mModel.exportTherion( out, mFullName, PlotInfo.projName[ (int)mType ] );
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
      if ( bool ){
          mHandler.sendEmptyMessage(1);
      }
    }
  }

  private class SaveTdr3File extends AsyncTask<Intent,Void,Boolean> // FIXME LEAK
  {
    private Context mContext;
    private Handler mHandler;
    private SketchModel mModel;
    private String mFullName;

    SaveTdr3File( Context context, Handler handler, SketchModel model, String name )
    {
       mContext  = context;
       mHandler  = handler;
       mModel    = model;
       mFullName = name;
       // TDLog.Log( TDLog.LOG_PLOT, "Save Tdr3 File " + mFullName );
    }

    @Override
    protected Boolean doInBackground(Intent... arg0)
    {
      try {
        String filename = TDPath.getTdr3FileWithExt( mFullName );
        TDPath.checkPath( filename );
        FileOutputStream fos = new FileOutputStream( filename );
        BufferedOutputStream bos = new BufferedOutputStream( fos );
        mModel.exportTdr( bos, mFullName, PlotInfo.projName[ (int)mType ] );
        fos.flush();
        fos.close();
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
      if ( bool ){
          mHandler.sendEmptyMessage(1);
      }
    }
  }

  // void doSaveTh3AndReload()
  // {
  //   if ( mFullName != null && mSketchSurface != null ) {
  //     TDToast.make( "save th3 and reload ... wait" );
  //     Handler saveHandler = new Handler(){
  //       @Override
  //       public void handleMessage(Message msg) {
  //         String filename = TDPath.getTh3FileWithExt( mFullName );
  //         mModel.loadTh3( filename, null, mPainter );
  //         mSketchSurface.setModel( mModel );
  //         mSketchSurface.isDrawing = true;
  //         TDToast.make( "save th3 and reload done" );
  //       }
  //     };
  //     new SaveTh3File(this, saveHandler, mModel, mFullName ).execute();
  //   } else {
  //     TDToast.make( "FAIL save th3 and reload" );
  //   }
  // }

  static Handler mSaveHandler = new Handler();
  // {
  //   @Override
  //   public void handleMessage(Message msg) { }
  // } ;

  private void doSaveTh3( )
  {
    if ( mFullName != null && mSketchSurface != null ) {
      new SaveTh3File( mActivity, mSaveHandler, mModel, mFullName ).execute();
    }
  }

  private void doSaveTdr3( )
  {
    if ( mFullName != null && mSketchSurface != null ) {
      new SaveTdr3File( mActivity, mSaveHandler, mModel, mFullName ).execute();
    }
  }

  private class SaveDxfFile extends AsyncTask<Intent,Void,Boolean> // FIXME LEAK
  {
      private Context mContext;
      private Handler mHandler;
      private SketchModel mModel;
      private String mFullName;

      SaveDxfFile( Context context, Handler handler, SketchModel model, String name )
      {
         mContext  = context;
         mHandler  = handler;
         mModel    = model;
         mFullName = name;
         // TDLog.Log( TDLog.LOG_PLOT, "SaveDxfFile " + mFullName );
      }

      @Override
      protected Boolean doInBackground(Intent... arg0)
      {
        try {
          String filename = TDPath.getDxfFileWithExt( mFullName );
          TDPath.checkPath( filename );
          FileWriter writer = new FileWriter( filename );
          PrintWriter out = new PrintWriter( writer );
          SketchDxf.write( out, mFullName, mModel );
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
          if ( bool ){
              mHandler.sendEmptyMessage(1);
          }
      }
  }

  void doSaveDxf()
  {
    if ( mFullName != null && mSketchSurface != null ) {
      // if ( not_all_symbols ) {
      //   alertMissingSymbols();
      // }
      // if ( mAllSymbols ) {
        new SaveDxfFile( mActivity, mSaveHandler, mModel, mFullName ).execute();
      // }
    }
  }

  // -----------------------------------------------------------------------------------
  // LIFECYCLE of the activity


  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    setRequestedOrientation( ActivityInfo.SCREEN_ORIENTATION_PORTRAIT );

    mPainter = new SketchPainter();

    setContentView(R.layout.sketch_activity);
    mApp = (TopoDroidApp)getApplication();
    mDataDownloader = null; // FIXME set the DataDownloader
    mActivity = this;
    // mInfo.zoom = mApp.mScaleFactor;    // canvas zoom

    // setCurrentPaint();
    mCurrentBrush = new DrawingPenBrush();
    mCurrentPoint = 0; // BrushManager.POINT_LABEL;
    mCurrentLine  = 0; // BrushManager.mLineLib.mLineWallIndex;
    mCurrentArea  = 0; // BrushManager.AREA_WATER;

    mSelectSize = TDSetting.mSelectness;

    // mSectionType = SketchSection.SECTION_NONE;
    // mIsInSection = false;

    mSketchSurface = (SketchDrawingSurface) findViewById(R.id.sketchSurface);
    mSketchSurface.makePreviewPath( DrawingPath.DRAWING_PATH_LINE, DrawingWindow.getPreviewPaint() );
    mSketchSurface.setOnTouchListener(this);
    // mSketchSurface.setOnLongClickListener(this);
    // mSketchSurface.setBuiltInZoomControls(true);

    BrushManager.makePaths( mApp, getResources() );

    mData        = TopoDroidApp.mData; // new DataHelper( this ); 
    Bundle extras = getIntent().getExtras();
    String name = "";
    if ( extras != null ) {
      mSid = extras.getLong(   TDTag.TOPODROID_SURVEY_ID );
      name = extras.getString( TDTag.TOPODROID_SKETCH_NAME );
    }
    mFullName    = TDInstance.survey + "-" + name;
    // mCompass     = null;
    // mDecl = mData.getSurveyDeclination( mSid );
    mDecl = 0.0f;

    mInfo      = mData.getSketch3dInfo( mSid, name );
    mPid       = mInfo.id;
    mInfo.xcenter = TopoDroidApp.mDisplayWidth/2;
    mInfo.ycenter = TopoDroidApp.mDisplayHeight/2;

    List<DBlock> list = mData.selectAllShots( mSid, TDStatus.NORMAL );
    if ( list.size() == 0 ) {
      TDToast.make( R.string.few_data );
      finish();
    } else {
      prepareReferences( list );
      computeReferenceFrame( false );

      // mAllSymbols  = true; // by default there are all the symbols

      // // now try to load drawings from therion file
      // SymbolsPalette missingSymbols = new SymbolsPalette();
      mLoaded = false;
      new SketchLoader( this, mModel, mFullName, mPainter ).execute();
     

      // if ( ! mAllSymbols ) { // FIXME FIXME
      //   String msg = missingSymbols.getMessage( getResources() );
      //   (new MissingDialog( mActivity, msg )).show();
      // }

      setSurfaceTransform( 0, 0 );
      // mSketchSurface.refresh(); // do not do this --> segfault
      mSketchSurface.setThreadRunning( true ); // FIXME necessary ?
    }

    // setSymbolButton();
    // selectBtn = (Button) findViewById( R.id.btn_select );
    // selectBtn.setOnClickListener( this );

    mListView = (HorizontalListView) findViewById(R.id.listview);
    mListView.setEmptyPlacholder( true );
    /* int size = */ TopoDroidApp.setListViewHeight( getApplicationContext(), mListView );

    Resources res = getResources();
    mButton1 = new Button[ mNrButton1 ];
    int off = 0;
    int ic  = 0;
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = MyButton.getButton( mActivity, this, izons[k] );
    }

    mButton2 = new Button[ mNrButton2 ];
    off = mNrButton1 - GREEN_BTN;
    for ( int k=0; k<mNrButton2; ++k ) {
      ic = ( k < GREEN_BTN )? k : off+k;
      mButton2[k] = MyButton.getButton( mActivity, this, ((k==0)? izons_ok[k] : izons[ic] ) );
    }

    // mButton3 = new Button[ mNrButton3 ];
    // off = mNrButton1 - GREEN_BTN + mNrButton2 - GREEN_BTN;
    // for ( int k=0; k<mNrButton3; ++k ) {
    //   ic = ( k == 0 )? GREEN_BTN : off+k;
    //   mButton3[k] = MyButton.getButton( mActivity, this, ((k==.)? izons_ok[k] : izons[ic] ) );
    // }

    mButton4 = new Button[ mNrButton4 ];
    off = mNrButton1 - GREEN_BTN + mNrButton2 - GREEN_BTN /* + mNrButton3 - GREEN_BTN */ ;
    for ( int k=0; k<mNrButton4; ++k ) {
      ic = ( k < GREEN_BTN )? k : off+k;
      mButton4[k] = MyButton.getButton( mActivity, this, ((k==1)? izons_ok[k] : izons[ic] ) );
    }
    mButtonView1 = new HorizontalButtonView( mButton1 );  // MOVE
    mButtonView2 = new HorizontalButtonView( mButton2 );  // DRAW
    // mButtonView3 = new HorizontalButtonView( mButton3 );  // EDIT
    mButtonView4 = new HorizontalButtonView( mButton4 );  // SELECT

    mBMedit_ok  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_bt_no );
    mBMedit_no  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_bt_no );
    // mListView.setAdapter( mButtonView1.mAdapter ); // done by setTheTitle
    setTheTitle();

    mBMselectAll   = MyButton.getButtonBackground( mApp, res, R.drawable.iz_select_all );
    mBMselectPoint = MyButton.getButtonBackground( mApp, res, R.drawable.iz_select_point );
    mBMselectLine  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_select_line );
    mBMselectArea  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_select_area );
    mBMselectShot  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_select_shot );
    mBMselectStation=MyButton.getButtonBackground( mApp, res, R.drawable.iz_select_station );
    setButtonFilterMode( mSelectMode, Drawing.CODE_SELECT );

    mBMsmall  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_small  );
    mBMmedium = MyButton.getButtonBackground( mApp, res, R.drawable.iz_medium );
    mBMlarge  = MyButton.getButtonBackground( mApp, res, R.drawable.iz_large  );
    setButtonSelectSize( Drawing.SCALE_MEDIUM );

    mInfo.resetDirection(); // azi = 0, clino = 0, and compute triad versors
    resetZoom();
    if ( mModel.mCurrentSurface == null ) {
      doMakeSurface( false );
    }

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    mImage.setBackgroundDrawable( MyButton.getButtonBackground( mApp, res, R.drawable.iz_menu ) );
    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter( res );
    closeMenu();
    // HOVER
    mMenu.setOnItemClickListener( this );

    mTimer   = null;

    if ( mDataDownloader != null ) {
      mApp.registerLister( this );
    }
  } 

  public void setButtonFilterMode( int filter_mode, int code )
  {
    if ( code == Drawing.CODE_SELECT ) {
      mSelectMode = filter_mode;
      // FIXME mSketchSurface.setSelectMode( mSelectMode );
      switch ( mSelectMode ) {
        case Drawing.FILTER_ALL:
          mButton4[ BTN_SELECT_MODE ].setBackgroundDrawable( mBMselectAll );
          break;
        case Drawing.FILTER_POINT:
          mButton4[ BTN_SELECT_MODE ].setBackgroundDrawable( mBMselectPoint );
          break;
        case Drawing.FILTER_LINE:
          mButton4[ BTN_SELECT_MODE ].setBackgroundDrawable( mBMselectLine );
          break;
        case Drawing.FILTER_AREA:
          mButton4[ BTN_SELECT_MODE ].setBackgroundDrawable( mBMselectArea );
          break;
        case Drawing.FILTER_SHOT:
          mButton4[ BTN_SELECT_MODE ].setBackgroundDrawable( mBMselectShot );
          break;
        case Drawing.FILTER_STATION:
          mButton4[ BTN_SELECT_MODE ].setBackgroundDrawable( mBMselectStation );
          break;
      }
    }
  } 

  private void setButtonSelectSize( int scale )
  {
    mSelectScale = scale % Drawing.SCALE_MAX;
    switch ( mSelectScale ) {
      case Drawing.SCALE_SMALL:
        mSelectSize = 0.5f * TDSetting.mSelectness;
        mButton4[ BTN_SELECT_SIZE ].setBackgroundDrawable( mBMsmall );
        break;
      case Drawing.SCALE_MEDIUM:
        mSelectSize = 1.0f * TDSetting.mSelectness;
        mButton4[ BTN_SELECT_SIZE ].setBackgroundDrawable( mBMmedium );
        break;
      case Drawing.SCALE_LARGE:
        mSelectSize = 2.0f * TDSetting.mSelectness;
        mButton4[ BTN_SELECT_SIZE ].setBackgroundDrawable( mBMlarge );
        break;
    }
  }
  
  void handleSketchLoaderResult( int r )
  {
    mLoaded = true;
    mModel.mDisplayMode = SketchDef.DISPLAY_NGBH;
  }

  @Override
  protected synchronized void onStart()
  {
    super.onStart();
    loadRecentSymbols( TopoDroidApp.mData );
  }

  @Override
  protected synchronized void onResume()
  {
    super.onResume();
    mSketchSurface.isDrawing = true;
  }


  @Override
  protected synchronized void onPause() 
  { 
    mSketchSurface.stopDrawing();
    mData.updateSketch( mPid, mSid, mInfo.st1, mInfo.st2, 
                        mInfo.xoffset_top, mInfo.yoffset_top, mInfo.zoom_top, 
                        mInfo.xoffset_side, mInfo.yoffset_side, mInfo.zoom_side, 
                        mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d, 
                        mInfo.east, mInfo.south, mInfo.vert,
                        mInfo.azimuth, mInfo.clino );
    // TDToast.make( R.string.saving_wait );
    doSaveTdr3();
    super.onPause();
  }

  @Override
  protected synchronized void onStop() 
  {   
    if ( mDataDownloader != null ) {
      // mApp.unregisterLister( this );
      mDataDownloader.onStop();
      mApp.disconnectRemoteDevice( false );
    }
    saveRecentSymbols( TopoDroidApp.mData );
    super.onStop();
  }

  @Override
  protected synchronized void onDestroy() 
  {   
    if ( mDataDownloader != null ) {
      mApp.unregisterLister( this );
    }
    super.onDestroy();
  }

  // ----------------------------------------------------------------------
  //  ADD ITEMS

  @Override
  public void addLabel( String label, float x, float y )
  {
    if ( label != null && label.length() > 0 ) {
      mModel.setLabelToLastPoint( label );
    }
  }

  @Override
  public void addPhotoPoint( String text, float x, float y )
  {
    // TODO
  }

  private void addStationName( NumStation st )
  {
    // mSketchSurface.addStation( st.name, st.e, st.s, st.v ); 
    SketchStationName stn = new SketchStationName( st.name, st.e, st.s, st.v );
    stn.mPaint = BrushManager.fixedStationPaint;
    mModel.addFixedStation( stn );
  }

  // convert coordinates relative to base point (e,s,v) of the sketch info
  // x east rightward
  // y south downward
  // z vertical downward
  // on input (x1,y1,z1) and (x2,y2,z2) are world coords (as computed by num)
  // then they are referred to the mInfo origin
  private void addFixed( float x1, float y1, float z1, 
                         float x2, float y2, float z2, 
                         DBlock blk, boolean splay, boolean is_reference )
  {
    x1 = Sketch3dInfo.mXScale * (x1 - mInfo.east);
    x2 = Sketch3dInfo.mXScale * (x2 - mInfo.east);
    y1 = Sketch3dInfo.mXScale * (y1 - mInfo.south);
    y2 = Sketch3dInfo.mXScale * (y2 - mInfo.south);
    z1 = Sketch3dInfo.mXScale * (z1 - mInfo.vert);
    z2 = Sketch3dInfo.mXScale * (z2 - mInfo.vert);
   
    SketchFixedPath path = null;
    if ( splay ) {
      path = new SketchFixedPath( DrawingPath.DRAWING_PATH_SPLAY, blk,
                                  BrushManager.fixedSplayPaint,
                                  null );
    } else {
      path = new SketchFixedPath( DrawingPath.DRAWING_PATH_FIXED, blk,
                             is_reference? BrushManager.highlightPaint : BrushManager.fixedShotPaint,
                             BrushManager.fixedBluePaint );
    }
    // if ( path != null ) { // ALWAYS true
      // path.setEndPoints( x1, y1, x2, y2 ); // scene coords
      path.set3dMidpoint( (x1+x2)/2, (y1+y2)/2, (z1+z2)/2 );
      path.addPoint( x1, y1, z1 );
      path.addPoint( x2, y2, z2 );
      mModel.addFixedPath( path );
    // }
  }

  // ------------------------------------------------------------------------------------
  // FRAME and REFERENCE

  private void computeReferenceFrame( boolean set_origin )
  {
    // get the two stations
    // clear Reference Frame
    mSketchSurface.clearReferences();
    NumStation station1 = mNum.getStation( mInfo.st1 );
    NumStation station2 = mNum.getStation( mInfo.st2 );
    NumShot    shot = mNum.getShot( mInfo.st1, mInfo.st2 );
    DBlock blk = shot.getFirstBlock();
    mInfo.setStations( station1, station2, blk, set_origin );
    // resetZoom();

    List<NumShot>  shots  = mNum.getShots();
    List<NumSplay> splays = mNum.getSplays();

    addStationName( station1 );
    addStationName( station2 );
    // List<NumStation> stations = mNum.getStations();
    // for ( NumStation station : stations ) {
    //   addStationName( station );
    // }

    for ( NumSplay splay : splays ) {
      if ( station1.equals(splay.from) ) {
        addFixed( station1.e, station1.s, station1.v, splay.e, splay.s, splay.v, splay.getBlock(), true, false );
      } else if ( station2.equals(splay.from) ) {
        addFixed( station2.e, station2.s, station2.v, splay.e, splay.s, splay.v, splay.getBlock(), true, false );
      }
    }
    for ( NumShot sh : shots ) {
      // addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.getFirstBlock(), false, false );
      if ( sh.from.equals(station1) ) {
        if ( ! sh.to.equals(station2) ) {
          addStationName( sh.to );
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.getFirstBlock(), false, false );
        } else {
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.getFirstBlock(), false, true );
        }
      } else if ( sh.from.equals(station2) ) {
        if ( ! sh.to.equals(station1) ) {
          addStationName( sh.to );
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.getFirstBlock(), false, false );
        } else {
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.getFirstBlock(), false, true );
        }
      } else if ( sh.to.equals(station1) ) {
        if ( ! sh.from.equals(station2) ) {
          addStationName( sh.from );
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.getFirstBlock(), false, false );
        } else {
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.getFirstBlock(), false, true );
        }
      } else if ( sh.to.equals(station2) ) {
        if ( ! sh.from.equals(station1) ) {
          addStationName( sh.from );
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.getFirstBlock(), false, false );
        } else {
          addFixed( sh.from.e, sh.from.s, sh.from.v, sh.to.e, sh.to.s, sh.to.v, sh.getFirstBlock(), false, true );
        }
      }
    }
  }

  private void prepareReferences( List<DBlock> list )
  {
    // mSketchSurface.clearReferences();
    mNum = new DistoXNum( list, mInfo.start, null, null, mDecl ); // FIXME null: no barrier no hiding
    // N.B. mCheckExtend does not metter for 3D
    if ( (! mNum.surveyAttached) && TDSetting.mCheckAttached ) {
      TDToast.make( R.string.survey_not_attached );
    }
    mModel = new SketchModel( mInfo, mNum, mPainter );
    mSketchSurface.setModel( mModel );
  }

  void recreateNum( List<DBlock> list )
  {
    mNum = new DistoXNum( list, mInfo.start, null, null, mDecl ); // FIXME null: no barrier no hiding
    mModel.setNum( mNum );
  }

  void updateNum( ArrayList<DBlock> list )
  {
    // FIXME mNum.addNewData( list );
    computeReferenceFrame( false ); // do not change origin
  }

  private void setSurfaceTransform( float x_shift, float y_shift )
  {
    mInfo.shiftOffset3d( x_shift, y_shift );
    mModel.setTransform( mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d );
  }

  // -------------------------------------------------------------------------
  // SELECT methods
  
  private void doSelectAt( float x_scene, float y_scene, float size )
  {
    // Log.v( "DistoX", "doSelectAt at " + x_scene + " " + y_scene );
    if ( mSelectMode == Drawing.FILTER_ALL || mSelectMode == Drawing.FILTER_POINT ) { 
      // SketchPointPath point = mSketchSurface.getPointAt( x_scene, y_scene, size ); // TODO
      // if ( point != null ) {
      //   // new DrawingPointDialog( mActivity, this, point ).show();
      //   return;
      // } 
    }

    if ( mSelectMode == Drawing.FILTER_ALL || mSelectMode == Drawing.FILTER_LINE ) { 
      // SketchLinePath line = mSketchSurface.getLineAt( x_scene, y_scene, size ); // TODO
      // if ( line != null ) {
      //   // new DrawingLineDialog( mActivity, this, line ).show();
      //   return;
      // }
    }
      
    if ( mSelectMode == Drawing.FILTER_ALL || mSelectMode == Drawing.FILTER_AREA ) { 
      // SketchAreaPath area = mSketchSurface.getAreaAt( x_scene, y_scene, size ); // TODO
      // if ( area != null ) {
      //   // new DrawingAreaDialog( mActivity, this, area ).show();
      //   return;
      // }
    }
  }

  private SketchFixedPath doSelectShotAt( float x_scene, float y_scene, float size )
  {
    // if ( mSelectMode == Drawing.FILTER_SHOT ) { 
      return mModel.selectShotAt( x_scene, y_scene, size );
    // }
    // return null;
  }

  private SketchStationName doSelectStationAt( float x_scene, float y_scene, float size )
  {
    return mModel.selectStationAt( x_scene, y_scene, size );
  }

  // private boolean doSelectSectionBasePointAt( float x_scene, float y_scene )
  // {
  //   return mModel.selectSectionBasePointAt( x_scene, y_scene );
  // }

  private SketchTriangle doSelectTriangleAt( float x_scene, float y_scene, SketchTriangle tri, float size )
  {
    return mModel.selectTriangleAt( x_scene, y_scene, tri, size );
  }
    
  // --------------------------------------------------------------------------
  // DELETE

  // void deletePoint( DrawingPointPath point ) 
  // {
  //   mSketchSurface.deletePath( point );
  // }

  // void deleteLine( DrawingLinePath line ) 
  // {
  //   mSketchSurface.deletePath( line );
  // }

  // void deleteArea( DrawingAreaPath area ) 
  // {
  //   mSketchSurface.deletePath( area );
  // }

  // void refreshSurface()
  // {
  //   // TDLog.Log( TDLog.LOG_PLOT, "refresh surface");
  //   mSketchSurface.refresh();
  // }

    
  // --------------------------------------------------------------------------
  // TOUCH

  // private void dumpEvent( MotionEventWrap ev )
  // {
  //   String name[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE", "PTR_DOWN", "PTR_UP", "7?", "8?", "9?" };
  //   StringBuilder sb = new StringBuilder();
  //   int action = ev.getAction();
  //   int actionCode = action & MotionEvent.ACTION_MASK;
  //   sb.append( "Event action_").append( name[actionCode] );
  //   if ( actionCode == MotionEvent.ACTION_POINTER_DOWN || actionCode == MotionEvent.ACTION_POINTER_UP ) {
  //     sb.append( "(pid " ).append( action>>MotionEvent.ACTION_POINTER_ID_SHIFT ).append( ")" );
  //   }
  //   sb.append( " [" );
  //   for (int i=0; i<ev.getPointerCount(); ++i ) {
  //     sb.append( "#" ).append( i );
  //     sb.append( "(pid " ).append( ev.getPointerId(i) ).append( ")=" ).append( (int)(ev.getX(i)) ).append( "." ).append( (int)(ev.getY(i)) );
  //     if ( i+1 < ev.getPointerCount() ) sb.append( ":" );
  //   }
  //   sb.append( "]" );
  //   // Log.v( TopoDroidApp.TAG, sb.toString() );
  // }
  

  private float spacing( MotionEventWrap ev )
  {
    int np = ev.getPointerCount();
    if ( np < 2 ) return 0.0f;
    float x = ev.getX(1) - ev.getX(0);
    float y = ev.getY(1) - ev.getY(0);
    return TDMath.sqrt(x*x + y*y);
  }

  // private float direction( MotionEventWrap ev )
  // {
  //   int np = ev.getPointerCount();
  //   if ( np < 2 ) return 0.0f;
  //   float x = ev.getX(1) - ev.getX(0);
  //   float y = ev.getY(1) - ev.getY(0);
  //   return TDMath.atan2( y, x );
  // }

  // private float position( MotionEventWrap ev ) // vertical position
  // {
  //   int np = ev.getPointerCount();
  //   if ( np == 0 ) return 0.0f;
  //   if ( np == 1 ) return ev.getY(0);
  //   return ( ev.getY(1) + ev.getY(0) ) / 2;
  // }

  // private Vector addPointToEditLine( SketchLinePath line, SketchVertex v1, SketchVertex v2, float t )
  // { 
  //   return line.addLinePoint( mInfo.east  + v1.x + t * (v2.x-v1.x),
  //                             mInfo.south + v1.y + t * (v2.y-v1.y),
  //                             mInfo.vert  + v1.z + t * (v2.z-v1.z) );
  // }

  // private Vector addPointToEditLine( SketchLinePath line, SketchVertex v1 )
  // { 
  //   return line.addLinePoint( mInfo.east  + v1.x,
  //                             mInfo.south + v1.y,
  //                             mInfo.vert  + v1.z );
  // }

  private Vector makeEditLinePoint( SketchVertex v1, SketchVertex v2, float t )
  { 
    return new Vector( mInfo.east  + v1.x + t * (v2.x-v1.x),
                       mInfo.south + v1.y + t * (v2.y-v1.y),
                       mInfo.vert  + v1.z + t * (v2.z-v1.z) );
  }

  private Vector makeEditLinePoint( SketchVertex v1 )
  { 
    return new Vector( mInfo.east  + v1.x,
                       mInfo.south + v1.y,
                       mInfo.vert  + v1.z );
  }

  public boolean onTouch( View view, MotionEvent rawEvent )
  {
    if ( onMenu ) {
      closeMenu();
      return true;
    }
    dismissPopups();

    float d0 = TDSetting.mCloseCutoff + mSelectSize / mInfo.zoom_3d;

    MotionEventWrap event = MotionEventWrap.wrap(rawEvent);
    // TDLog.Log( TDLog.LOG_INPUT, "SketchWindow onTouch() " );
    // dumpEvent( event );

    float x_canvas = event.getX();
    float y_canvas = event.getY();

    float x_scene = mInfo.canvasToSceneX( x_canvas );
    float y_scene = mInfo.canvasToSceneY( y_canvas );

    // Log.v("DistoX", "canvas pt " + x_canvas + " " + y_canvas + " scene " + x_scene + " " + y_scene );
    int action = event.getAction() & MotionEvent.ACTION_MASK;

    if (action == MotionEvent.ACTION_POINTER_DOWN) {
      mTouchMode = SketchDef.TOUCH_ZOOM;
      oldDist = spacing( event );
    }
    else if ( action == MotionEvent.ACTION_POINTER_UP)
    {
      mTouchMode = SketchDef.TOUCH_MOVE;
      /* nothing */
    }

    // ============================== DOWN ===============================
    else if (action == MotionEvent.ACTION_DOWN)
    {
      if ( mMode == SketchDef.MODE_MOVE /* || mMode == SketchDef.MODE_EDIT */ ) {
        // mActivity.setTitle( R.string.title_move );
        mSaveX = x_canvas;
        mSaveY = y_canvas;
        return false;
      } else if ( mMode == SketchDef.MODE_DRAW /* || mMode == SketchDef.MODE_EDIT */ ) {
        mStartX = x_scene;
        mStartY = y_scene;
        // TDLog.Log( TDLog.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
        mPointCnt = 0;
        if ( mSymbol == Symbol.LINE || mSymbol == Symbol.AREA ) {
          // mSketchSurface.isDrawing = true;
          mCurrentLinePath = new DrawingLinePath( mCurrentLine );
          mCurrentLinePath.addStartPoint( x_scene, y_scene );
          mCurrentBrush.mouseDown( mSketchSurface.getPreviewPath(), x_canvas, y_canvas );
        } else { // Symbol.POINT
          mSaveX = x_canvas;
          mSaveY = y_canvas;
        }
      } else if ( mMode == SketchDef.MODE_SELECT ) {
        mSaveX = x_canvas;
        mSaveY = y_canvas;
        float xs = mInfo.canvasToSceneX( x_canvas );
        float ys = mInfo.canvasToSceneY( y_canvas );
        if ( mModel.mDisplayMode == SketchDef.DISPLAY_NONE ) { // MODE_STEP
          SketchFixedPath path = doSelectShotAt( x_scene, y_scene, mSelectSize ); 
          if ( path == null ) {
            TDToast.make( R.string.shot_not_found );
          } else {
            DBlock blk = path.mBlock;
            if ( blk != null ) {
              // float a = mInfo.azimuth;
              // float c = mInfo.clino;
              // float x = mInfo.xoffset_3d;
              // float y = mInfo.yoffset_3d;
              // float z = mInfo.zoom_3d;

              mInfo.st1 = blk.mFrom;
              mInfo.st2 = blk.mTo;
              computeReferenceFrame( false );
              if ( ! mModel.hasSurface( mInfo.st1, mInfo.st2 ) ) {
                doMakeSurface( false );
              }

              // mInfo.resetDirection(); // azi = 0, clino = 0, and compute triad versors
              // resetZoom();
              // mInfo.shiftOffset3d( x, y );
              // mInfo.resetZoom3d( x, y, z );
              // mInfo.rotateBy3d( a, c );
            }
          }
        } else {
          // Log.v("DistoX", "getVertexAt scene " + xs + " " + ys + " d " + d0 );
          SketchVertex v = mModel.getVertexAt( xs, ys, d0 );
          if ( v != null ) {
            SketchVertex v1 = mModel.getSelectedVertex();
            if ( v1 == null ) {
              mMoveSelected = false;
              // Log.v("DistoX", "set selected vertex " + v.index );
              mModel.setSelectedVertex( v );
            } else if ( v == v1 ) {
              mMoveSelected = true;
            } else {
              mMoveSelected = false;
              // Log.v("DistoX", "set selected vertex null ");
              mModel.setSelectedVertex( null );
            } 
          }
          // SelectionPoint pt = mModel.hotItem();
          // if ( pt != null ) {
          //   mMoveSelected = ( pt.distance( x_scene, y_scene ) < d0 );
          // }
          // doSelectAt( x_scene, y_scene );
          mSaveX = x_canvas;
          mSaveY = y_canvas;
        }
      // } else if ( mMode == SketchDef.MODE_STEP ) { // MODE_SELECT
      //   SketchFixedPath path = doSelectShotAt( x_scene, y_scene, mSelectSize ); 
      //   if ( path == null ) {
      //     TDToast.make( R.string.shot_not_found );
      //   } else {
      //     DBlock blk = path.mBlock;
      //     if ( blk != null ) {
      //       // float a = mInfo.azimuth;
      //       // float c = mInfo.clino;
      //       // float x = mInfo.xoffset_3d;
      //       // float y = mInfo.yoffset_3d;
      //       // float z = mInfo.zoom_3d;

      //       mInfo.st1 = blk.mFrom;
      //       mInfo.st2 = blk.mTo;
      //       computeReferenceFrame( false );

      //       // mInfo.resetDirection(); // azi = 0, clino = 0, and compute triad versors
      //       // resetZoom();
      //       // mInfo.shiftOffset3d( x, y );
      //       // mInfo.resetZoom3d( x, y, z );
      //       // mInfo.rotateBy3d( a, c );
      //     }
      //   }
      }
    }
    // ============================== MOVE ===============================
    else if (action == MotionEvent.ACTION_MOVE)
    {
      if ( mTouchMode == SketchDef.TOUCH_MOVE) {
        float x_shift = x_canvas - mSaveX; // compute shift
        float y_shift = y_canvas - mSaveY;
        if ( mMode == SketchDef.MODE_DRAW /* || mMode == SketchDef.MODE_EDIT */ ) {
          if ( mSymbol == Symbol.LINE || mSymbol == Symbol.AREA ) {
            if ( x_shift*x_shift + y_shift*y_shift > TDSetting.mLineSegment2 ) {
              ++mPointCnt; // add all points
              mCurrentLinePath.addPoint( x_scene, y_scene );
              mSaveX = x_canvas;                 // reset start
              mSaveY = y_canvas;
              mCurrentBrush.mouseMove( mSketchSurface.getPreviewPath(), x_canvas, y_canvas );
            }
          }
        } else if ( mMode == SketchDef.MODE_MOVE 
                 || ( mMode == SketchDef.MODE_SELECT && ! mMoveSelected ) ) {
          if ( Math.abs( x_shift ) < 20 && Math.abs( y_shift ) < 20 ) {
            mInfo.rotateBy3d( -x_shift/6, y_shift/6 );
            setTheTitle();
            mModel.setTransform( mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d );
            mSaveX = x_canvas;                 // reset start
            mSaveY = y_canvas;
          }
        } else if ( mMode == SketchDef.MODE_SELECT && mMoveSelected ) { // moving the selected vertex
          // TODO
        // } else if ( mMode == SketchDef.MODE_STEP ) {
        //   // nothing
        }
      } else { // mTouchMode == SketchDef.TOUCH_ZOOM
        float newDist = spacing( event );
        if ( newDist > 16.0f && oldDist > 16.0f ) {
          float factor = newDist/oldDist;
          if ( factor > 0.05f && factor < 4.0f ) {
            changeZoom( factor );
            oldDist = newDist;
          } 
          if ( Math.abs(factor-1.0f) <= 0.05f ) { // move
            float x_shift = x_canvas - mSaveX; // compute shift
            float y_shift = y_canvas - mSaveY;
            if ( (Math.abs(x_shift) + Math.abs(y_shift)) < 200 ) { 
              setSurfaceTransform( x_shift, y_shift );
              mSaveX = x_canvas;
              mSaveY = y_canvas;
            }
          }
        }
      }
    }
    // ============================== UP   ===============================
    else if (action == MotionEvent.ACTION_UP)
    {
      if ( mTouchMode == SketchDef.TOUCH_ZOOM ) {
        mTouchMode = SketchDef.TOUCH_MOVE;
      } else {
        float x_shift = x_canvas - mSaveX; // compute shift
        float y_shift = y_canvas - mSaveY;

        if ( mMode == SketchDef.MODE_MOVE )
        {
          /* nothing */
        }
        else if ( mMode == SketchDef.MODE_DRAW )
        {
          // normal draw
          if ( mSymbol == Symbol.LINE || mSymbol == Symbol.AREA ) {
            mCurrentBrush.mouseUp( mSketchSurface.getPreviewPath(), x_canvas, y_canvas );
            mSketchSurface.resetPreviewPath();

            if ( x_shift*x_shift + y_shift*y_shift > TDSetting.mLineSegment2 ) {
              mCurrentLinePath.addPoint( x_scene, y_scene );
            }
            if ( mPointCnt > TDSetting.mLineType ) {
              SketchLinePath line = null;
              if ( mSymbol == Symbol.LINE ) {
                line = new SketchLinePath( DrawingPath.DRAWING_PATH_LINE, mCurrentLine, mInfo.st1, mInfo.st2, mPainter );
              } else if ( mSymbol == Symbol.AREA ) {
                line = new SketchLinePath( DrawingPath.DRAWING_PATH_AREA, mCurrentArea, mInfo.st1, mInfo.st2, mPainter );
              }
              ArrayList< LinePoint > pts = new ArrayList<>();
              for ( LinePoint p = mCurrentLinePath.mFirst; p != null; p = p.mNext ) { pts.add( p ); }
              SketchTriangle tri = null;
              int np = pts.size();
              LinePoint p1 = pts.get(0);
              LinePoint p2 = pts.get(np-1);
              for (LinePoint p : pts ) {
                // find point on the triangulated surface and add it to the line
                tri = doSelectTriangleAt( p.x, p.y, tri, mSelectSize );
                if ( tri != null /* && mInfo.isForward( tri ) */ ) {
                  Vector q1 = tri.get3dPoint( p.x, p.y );
                  line.addLinePoint( mInfo.east + q1.x, mInfo.south + q1.y, mInfo.vert + q1.z );
                }                  
              }

              if ( mSymbol == Symbol.LINE ) {
                p1 = pts.get(0);
                p2 = pts.get(pts.size()-1);
                float len = TDMath.sqrt( (p2.x-p1.x)*(p2.x-p1.x) + (p2.y-p1.y)*(p2.y-p1.y) );
                if ( len < SketchDef.CLOSE_GAP ) {
                  line.close();
                }
              } else if ( mSymbol == Symbol.AREA ) {
                // Log.v("DistoX", "add area type " + mCurrentArea );
                line.close();
              }
              mModel.addSketchPath( line );
            }
          } else { // Symbol.POINT
            if ( Math.abs( x_shift ) < 16 && Math.abs( y_shift ) < 16 ) {
              // Log.v("DistoX", "point get triangle at " + x_scene + " " + y_scene );
              SketchTriangle tri = doSelectTriangleAt( x_scene, y_scene, null, mSelectSize );
              if ( tri != null /* && mInfo.isForward( tri ) */ ) {
                Vector p = tri.get3dPoint( x_scene, y_scene );
                // Log.v("DistoX", "new point " + mCurrentPoint + " at " + p.x + " " + p.y + " " + p.z );
                SketchPointPath path = new SketchPointPath( mCurrentPoint, mInfo.st1, mInfo.st2, p.x, p.y, p.z );
                SymbolPointLibrary point_lib = BrushManager.mPointLib;
                if ( point_lib.isSymbolOrientable( mCurrentPoint ) ) {
                  float angle = (float)( point_lib.getPointOrientation( mCurrentPoint ) ); // degrees
                  // Log.v("DistoX", "point " + mCurrentPoint + " angle " + angle );
                  // angles 0:upward 90;rightward 180:downward 270:leftward
                  // scene: x is rightward, y downward
                  // p1 is the 3D point of the orientation (from p to p1)
                  Vector p1 = tri.get3dPoint( x_scene + 0.1f * TDMath.sind(angle),
                                              y_scene - 0.1f * TDMath.cosd(angle) );
                  path.setOrientation( p1, mInfo );
                }
                mModel.addPoint( path );
                if ( mCurrentPoint == point_lib.mPointLabelIndex ) { // text dialog
                  new DrawingLabelDialog( mActivity, this, x_scene, y_scene ).show();
                }
              // } else {
              //   Log.v("DistoX", "no triangle found");
              }
            }
          }
          
        }
        // else if ( mMode == SketchDef.MODE_EDIT )
        // {

        //   if ( mSymbol == Symbol.LINE /* || mSymbol == Symbol.AREA */ ) {
        //     mCurrentBrush.mouseUp( mSketchSurface.getPreviewPath(), x_canvas, y_canvas );
        //     mSketchSurface.resetPreviewPath();
        //
        //     if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > TDSetting.mLineSegment || (mPointCnt % TDSetting.mLineType) > 0 ) {
        //       mCurrentLinePath.addPoint( x_scene, y_scene );
        //     }

        //     if ( mPointCnt > TDSetting.mLineType ) {
        //       if ( mEdit == SketchDef.EDIT_NONE ) {
        //         // SketchLinePath edit_line =
        //         //   new SketchLinePath( DrawingPath.DRAWING_PATH_LINE, mCurrentLine, mInfo.st1, mInfo.st2, mPainter );

        //         // close mCurrentLinePath adding a point equal to the start one (scene coords)
        //         mCurrentLinePath.addPoint( mStartX, mStartY );

        //         boolean skipped_triangle = false;
        //         // mModel.setEditLine( null );

        //         LinePoint p1 = mCurrentLinePath.mFirst;
	//         SketchTriangle tri1 = doSelectTriangleAt( p1.x, p1.y, null, mSelectSize );
        //         SketchTriangle tri10 = tri1;
        //         Vector w10 = null;
        //         Vector w20 = null;
        //         Vector w120 = null;

        //         if ( tri1 != null /* && mInfo.isForward( tri1 ) */ ) {
        //           ArrayList< SketchRefinement > refines = new ArrayList<>();
        //           // ArrayList< PointF > border = new ArrayList<>();
        //           SketchTriangle tri2 = tri1;
        //           SketchVertex u1 = null, u2 = null; // previous side of tri2
        //           SketchVertex w1 = null, w2 = null; // current side of tri2
        //           Vector u12 = null;
        //           Vector w12 = null;
        //           PointF border_point = new PointF(0,0);

        //           for ( LinePoint p2 = p1.mNext; p2 != null; p2 = p2.mNext ) { 
        //             tri2 = doSelectTriangleAt( p2.x, p2.y, tri2, mSelectSize );
        //             if ( tri2 != null && tri2 != tri1 ) {
        //               int i1 = tri1.i;
        //               if ( i1 != tri2.i && i1 != tri2.j && i1 != tri2.k ) i1 = -1;
        //               int j1 = tri1.j;
        //               if ( j1 != tri2.i && j1 != tri2.j && j1 != tri2.k ) j1 = -1;
        //               int k1 = tri1.k;
        //               if ( k1 != tri2.i && k1 != tri2.j && k1 != tri2.k ) k1 = -1;
        //               if ( i1 >= 0 ) {
        //                 if ( j1 >= 0 ) { // side v1--v2 of tri1
        //                   w1 = tri1.v1;
        //                   w2 = tri1.v2;
        //                   // intersect p1--p2 with tri2.p1--tri2.p2
        //                   float t = Point2D.intersect( p1, p2, tri1.p1, tri1.p2, border_point );
        //                   // w12 = addPointToEditLine( edit_line, w1, w2, t );
        //                   w12 = makeEditLinePoint( w1, w2, t );
        //                 } else if ( k1 >= 0 ) { // side v3--v1
        //                   w1 = tri1.v3;
        //                   w2 = tri1.v1;
        //                   float t = Point2D.intersect( p1, p2, tri1.p3, tri1.p1, border_point );
        //                   // w12 = addPointToEditLine( edit_line, w1, w2, t );
        //                   w12 = makeEditLinePoint( w1, w2, t );
        //                 } else { // vertex v1
        //                   w1 = tri1.v1;
        //                   w2 = tri1.v1;
        //                   // w12 = addPointToEditLine( edit_line, w1 );
        //                   w12 = makeEditLinePoint( w1 );
        //                   // border_point.x = tri1.p1.x;
        //                   // border_point.y = tri1.p1.y;
        //                 }
        //               } else if ( j1 >= 0 ) {
        //                 if ( k1 >= 0 ) { // side v2--v3
        //                   w1 = tri1.v2;
        //                   w2 = tri1.v3;
        //                   float t = Point2D.intersect( p1, p2, tri1.p2, tri1.p3, border_point );
        //                   // w12 = addPointToEditLine( edit_line, w1, w2, t );
        //                   w12 = makeEditLinePoint( w1, w2, t );
        //                 } else { // vertex v2
        //                   w1 = tri1.v2;
        //                   w2 = tri1.v2;
        //                   // w12 = addPointToEditLine( edit_line, w1 );
        //                   w12 = makeEditLinePoint( w1 );
        //                   // border_point.x = tri1.p2.x;
        //                   // border_point.y = tri1.p2.y;
        //                 }
        //               } else if ( k1 >= 0 ) { // vertex v3
        //                 w1 = tri1.v3;
        //                 w2 = tri1.v3;
        //                 // w12 = addPointToEditLine( edit_line, w1 );
        //                 w12 = makeEditLinePoint( w1 );
        //                 // border_point.x = tri1.p3.x;
        //                 // border_point.y = tri1.p3.y;
        //               } else {
        //                 Log.v("DistoX", "WARNING edit_line jumps over a triangle");
        //                 skipped_triangle = true;
        //               }
        //               // border.add( new PointF( border_point.x, border_point.y ) );
        //               if ( u1 != null ) {
        //                 if ( u2 == w1 ) {        // u1 --(u12)--> u2=w1 --(w12)--> w2
        //                   refines.add( new SketchRefinement( tri1, u2, w12, u12 ) );
        //                 } else if ( u1 == w2 ) { // w1 --(w12)--> w2=u1 --(u12)--> u2
        //                   refines.add( new SketchRefinement( tri1, u1, u12, w12 ) );
        //                 }
        //               } else {
        //                 w10 = w1;
        //                 w20 = w2;
        //                 w120 = w12;
        //               }
        //               tri1 = tri2;
        //               u1   = w2;
        //               u2   = w1;
        //               u12  = w12;
        //             }
        //             p1 = p2;
        //           }
        //           if ( u2 == w10 ) { // u1 --> u2=w1 --> w2
        //             refines.add( new SketchRefinement( tri10, u2, w120, u12 ) );
        //           } else if ( u1 == w20 ) { // w1 --> w2=u1 --> u2
        //             refines.add( new SketchRefinement( tri10, u1, u12, w120 ) );
        //           }
        //           if ( skipped_triangle ) {
        //             TDToast.make( R.string.few_line_points );
        //           } else {
        //             // Log.v("DistoX", "refinements " + refines.size() );
        //             // NOW GET the inside triangles. extend border a little
        //             // float x = 0;  
        //             // float y = 0;
        //             // for ( PointF p : border ) { x += p.x; y += p.y; }
        //             // x /= border.size();
        //             // y /= border.size();
        //             // // StringWriter sw = new StringWriter();
        //             // // PrintWriter pw = new PrintWriter( sw );
        //             // for ( PointF p : border ) {
        //             //   p.x += 0.1f * (p.x - x );
        //             //   p.y += 0.1f * (p.y - y );
        //             //   // pw.format(Locale.US, "%.2f %.2f ", p.x, p.y );
        //             // }
        //             // Log.v( "DistoX", " border " + sw.getBuffer().toString() );

        //             mModel.setRefinement( refines );
        //             // mModel.setEditLine( edit_line );
        //             // mModel.setBorder( border );

        //             // mModel.doRefinement( );

        //             // Log.v( "DistoX", "border " + border.size() + " inside triangles " + ntin );
        //           }
        //         }
        //       } else if ( mEdit == SketchDef.EDIT_STRETCH /* || mEdit == SketchDef.EDIT_EXTRUDE */ ) {
        //         // TODO convert mCurrentLinePath (DrawingLinePath) to 3D line
        //         ArrayList<Vector> pts = new ArrayList<>();

        //         LinePoint p1 = mCurrentLinePath.mFirst;
        //         float x = mInfo.canvasToSceneX( p1.x );
        //         float y = mInfo.canvasToSceneY( p1.y );
        //         pts.add( mInfo.sceneToWorld( x, y ) );
        //         for ( LinePoint p2 = p1.mNext; p2 != null; p2 = p2.mNext ) {
        //           x = mInfo.canvasToSceneX( p2.x );
        //           y = mInfo.canvasToSceneY( p2.y );
        //           pts.add( mInfo.sceneToWorld( x, y ) );
        //         }

        //         // if ( mEdit == SketchDef.EDIT_EXTRUDE ) { // line is the path along which to extrude the new surface
        //         //   mModel.makeExtrude( pts );
        //         // } else if ( mEdit == SketchDef.EDIT_STRETCH ) {
        //           mModel.makeStretch( pts );
        //         // }
        //         mEdit = SketchDef.EDIT_NONE;
        //       }
        //       // NOTE EDIT_CUT is done immediately on the surface insideTriangles
        //     }
        //   }
        // } 
        else if ( mMode ==  SketchDef.MODE_SELECT )
        {
          if ( mMoveSelected ) {
            // TODO ???
          }
        }
      }
    }
    return true;
  }


  private void askDeleteLine( final SketchLinePath line )
  {
    TopoDroidAlertDialog.makeAlert( mActivity, getResources(),
                           getResources().getString( R.string.line_delete ) + " ?",
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doDeleteLine( line );
        }
    } );
  }

  void doDeleteLine( SketchLinePath line )
  {
    mModel.deleteLine( line );
  }

  // ----------------------------------------------------------------------
  // CLICK

  public void onClick(View view)
  {
    if ( onMenu ) {
      closeMenu();
      return;
    }
    dismissPopups();

    if ( mTimer != null ) mTimer.mRun = false;

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

    // if ( b == selectBtn ) {
    //   if ( mSelect == SketchDef.SELECT_SECTION ) {
    //     setSelect( SketchDef.SELECT_STEP );
    //   } else if ( mSelect == SketchDef.SELECT_STEP ) {
    //     setSelect( SketchDef.SELECT_SHOT );
    //   } else if ( mSelect == SketchDef.SELECT_SHOT ) {
    //     setSelect( SketchDef.SELECT_JOIN );
    //   } else if ( mSelect == SketchDef.SELECT_JOIN ) {
    //     setSelect( SketchDef.SELECT_SECTION );
    //   }
    //   // FIXME (new SketchSelectDialog(mActivity, this)).show();
    // } else 
    int k1 = 2;
    int k2 = 2;
    int k4 = 2;

    if ( b == mButton1[0] || b == mButton2[0] || /* b == mButton3[0] || */ b == mButton4[0] ) { // mode DRAW
      // makePopupMode( b ); 
      setMode( SketchDef.MODE_DRAW );
    } else if ( b == mButton1[1] || b == mButton2[1] || /* b == mButton3[1] || */ b == mButton4[1] ) { // mode SELECT
      setMode( SketchDef.MODE_SELECT );

// MODE_MOVE 
    } else if ( b == mButton1[k1++] ) { // DISPLAY MODE. cycle (NGBH, SINGLE, ALL, NONE)
      if ( mLoaded ) {
        new SketchModeDialog( mActivity, mModel ).show();
      } else {
        TDToast.make( R.string.sketch3d_loading );
      }
    } else if ( b == mButton1[k1++] ) { // SURFACE
      if ( mLoaded ) {
        if ( mModel.mCurrentSurface != null ) {
          alertMakeSurface( );
        } else {
          doMakeSurface( true );
        }
      } else {
        TDToast.make( R.string.sketch3d_loading );
      }
    } else if ( b == mButton1[k1++] ) { // DOWNLOAD
      if ( TDInstance.device != null ) {
        // TODO if there is an empty shot use it, else try to download the data
        //      with the Asynch task that download the data.
        //      if there is an empty shot assign it
        mActivity.setTitleColor( TDColor.CONNECTED );
        ListerHandler handler = new ListerHandler( this ); // FIXME_LISTER
        new DataDownloadTask( mApp, handler, null ).execute();
      } else {
        TDToast.make( R.string.device_none );
      }
    } else if ( b == mButton1[k1++] ) { // NOTES
      (new DistoXAnnotations( mActivity, mData.getSurveyFromId(mSid) )).show();
    } else if ( b == mButton1[k1++] ) { // INFO
      // float azimuth = -1;
      new DistoXStatDialog( mActivity, mNum, mInfo.start, -1, mData.getSurveyStat( TDInstance.sid ) ).show();

// MODE_DRAW
    } else if ( b == mButton2[k2++] ) { // UNDO
      mModel.undo();
    } else if ( b == mButton2[k2++] ) { // REDO
      mModel.redo();
    } else if ( b == mButton2[k2++] ) { // PALETTE
      if ( TDSetting.mPickerType == TDSetting.PICKER_RECENT ) { 
        new ItemRecentDialog(mActivity, this, mType ).show();
      } else {
        new ItemPickerDialog(mActivity, this, mType, mSymbol ).show();
      }

// MODE_EDIT
    // } else if ( b == mButton3[k3++] ) { // refine triangles
    //   //   extrudeRegion();
    //   // Log.v("DistoX", "refine to max side ");
    //   int split = mModel.refineToMaxSide( TDSetting.mSketchSideSize );
    //   if ( split == 0 ) { 
    //     TDToast.make( R.string.sketch_no_split );
    //   }
    // } else if ( b == mButton3[k3++] ) { // refine_center
    //   mModel.refineSurfaceAtCenters();
    // } else if ( b == mButton3[k3++] ) { // refine_sides
    //   mModel.refineSurfaceAtSides();
    // } else if ( b == mButton3[k3++] && mCurrentLinePath != null ) { // cut
    //   cutRegion();
    // } else if ( b == mButton3[k3++] && mCurrentLinePath != null ) { // stretch
    //   stretchRegion();

// MODE_SELECT
    } else if ( b == mButton4[k4++] ) { // SELECT MODE
      makePopupFilter( b, Drawing.mSelectModes, 
                       ( (mModel.mDisplayMode == SketchDef.DISPLAY_NONE)? 5 : 4), Drawing.CODE_SELECT );
    } else if ( b == mButton4[k4++] ) { // SELECT SIZE
      setButtonSelectSize( mSelectScale + 1 ); // toggle select size
    } else if ( b == mButton4[k4++] ) { 
      // TODO previous
    } else if ( b == mButton4[k4++] ) { 
      // TODO next
    // } else if ( b == mButton4[k4++] ) { 
    //   mModel.refineSurfaceAtSelectedVertex();

    }
  }

  // -------------------------------------------------------
  // POPUP

  private boolean dismissPopups()
  {
    return dismissPopupFilter() 
           // || dismissPopupMode()
    ;
  }

  PopupWindow mPopupFilter = null;

  public boolean dismissPopupFilter()
  {
    if ( mPopupFilter != null ) {
      mPopupFilter.dismiss();
      mPopupFilter = null;
      return true;
    }
    return false;
  }

  private void makePopupFilter( View b, int[] modes, int nr, final int code )
  {
    if ( mPopupFilter != null ) return;

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
      tv[k] = CutNPaste.makePopupButton( mActivity, text, popup_layout, lWidth, lHeight, 
              new FilterClickListener( this, k, code ) );
      if ( k == 0 ) {
        FontMetrics fm = tv[0].getPaint().getFontMetrics();
        // Log.v("DistoX", "metrics TOP " + fm.top + " ASC. " + fm.ascent + " BOT " + fm.bottom + " LEAD " + fm.leading ); 
        w = (int)( Math.abs( ( len + 1 ) * fm.ascent ) * 0.7);
        h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
      }
      tv[k].setWidth( w );
    }
    // Log.v( TopoDroidApp.TAG, "popup width " + w );
    mPopupFilter = new PopupWindow( popup_layout, w, h ); 
    // mPopupEdit = new PopupWindow( popup_layout, popup_layout.getHeight(), popup_layout.getWidth() );
    mPopupFilter.showAsDropDown(b); 
  }

  // void setSelect( int select )
  // {
  //   mSelect = select;
  //   setMode( ( select != SketchDef.SELECT_NONE )? SketchDef.MODE_EDIT : SketchDef.MODE_MOVE );
  // }


    // ----------------------------------------------------------------

    // void setHeading( float heading )
    public void setBearingAndClino( float heading, float clino, int orientation )
    {
      mInfo.resetDirection( heading, 0.0f ); // from the side
      // mCompass = null;
      setMode( SketchDef.MODE_MOVE );
    }

    public void setJpegData( byte[] data ) { }


  // ----------------------------------------------
  // MENU

  private void setMenuAdapter( Resources res )
  {
    // HOVER
    // mMenuAdapter = new MyMenuAdapter( mActivity, this, mMenu, R.layout.menu, new ArrayList< MyMenuItem >() );
    mMenuAdapter = new ArrayAdapter<>(mActivity, R.layout.menu );
    for ( int k=0; k<menus.length; ++k ) {
      mMenuAdapter.add( res.getString( menus[k] ) );
    }
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

  private void handleMenu( int pos )
  {
    closeMenu();
    int p = 0;
    if ( p++ == pos ) { // EXPORT
      // new SketchSaveDialog( mActivity, this ).show();
      if ( mLoaded ) {
        new ExportDialog( mActivity, this, TDConst.mSketchExportTypes, R.string.title_plot_save ).show();
      } else {
        TDToast.make( R.string.sketch3d_loading );
      }
    } else if ( p++ == pos ) { // PALETTE 
      BrushManager.makePaths( mApp, getResources() );
      (new SymbolEnableDialog( mActivity, mApp )).show();
    } else if ( p++ == pos ) { // ZOOM ONE
      mInfo.resetDirection(); // azi = 0, clino = 0, and compute triad versors
      resetZoom();
    } else if ( p++ == pos ) { // HEADING
      setMode( SketchDef.MODE_MOVE );
      // SensorManager sm = (SensorManager)getSystemService( Context.SENSOR_SERVICE );
      // mCompass = new SketchCompassSensor( mActivity, sm, TDSetting.mCompassReadings );
      mTimer = new TimerTask( mActivity, this, TimerTask.Y_AXIS, TDSetting.mTimerWait, 10 );
      mTimer.execute();
    } else if ( p++ == pos ) { // DELETE
      if ( mLoaded ) {
        askDelete();
      } else {
        TDToast.make( R.string.sketch3d_loading );
      }
    } else if ( p++ == pos ) { // CLEAR
      if ( mLoaded ) {
        askClear();
      } else {
        TDToast.make( R.string.sketch3d_loading );
      }
    } else if ( p++ == pos ) { // SETTINGS
      Intent optionsIntent = new Intent( mActivity, TopoDroidPreferences.class );
      optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SKETCH );
      mActivity.startActivity( optionsIntent );
    } else if ( p++ == pos ) { // HELP
      int nn = mNrButton1 + mNrButton2 - GREEN_BTN + /* mNrButton3 - GREEN_BTN */ + mNrButton4 - GREEN_BTN;
      new HelpDialog(mActivity, izons, menus, help_icons, help_menus, nn, menus.length, getResources().getString( HELP_PAGE ) ).show();
    }
  }

  void doDelete()
  {
    mData.deleteSketch( mPid, mSid );
    finish();
  }

  private void askDelete()
  {
    TopoDroidAlertDialog.makeAlert( mActivity, getResources(), R.string.sketch_delete,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doDelete();
        }
    } );
  }

  void doClear()
  {
    mModel.removeAllSurfaces();
  }

  private void askClear()
  {
    TopoDroidAlertDialog.makeAlert( mActivity, getResources(), R.string.sketch_clear,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doClear();
        }
    } );
  }

  // interface IExporter
  public void doExport( String type )
  {
    int index = TDConst.sketchExportIndex( type );
    switch ( index ) {
      case TDConst.DISTOX_EXPORT_TH3: doSaveTh3(); break;
      case TDConst.DISTOX_EXPORT_DXF: doSaveDxf(); break;
    }
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      handleMenu( pos );
    }
  }

  // ------------------------------------------------------------------------
  // HIGHLIGHT - EXTRUSION


  // void highlightRegion() { mModel.highlightLineRegion( ); }

  // MODE_EDIT
  // private void cutRegion() 
  // { 
  //   mModel.doRefinement();
  //   mModel.makeCut( );
  // }

  // private void extrudeRegion() { prepareEditRegion( SketchDef.EDIT_EXTRUDE ); }

  // MODE_EDIT
  // private void stretchRegion() { prepareEditRegion( SketchDef.EDIT_STRETCH ); }

  // MODE_EDIT
  // private void prepareEditRegion( int edit )
  // {
  //   mModel.doRefinement();
  //   mEdit   = edit;
  //   mSymbol = Symbol.LINE;
  //   mCurrentLine = 0;
  //   mTouchMode = SketchDef.TOUCH_MOVE;
  //   setMode( SketchDef.MODE_EDIT );
  //   // computeReferenceFrame( false );
  //   // setSurfaceTransform( 0, 0 );
  // }


  // ------------------------------------------------------------------------------
  // MAKE SURFACE
  
  /** (x,y) quadrant ( 0:TL 1:BL 2:BR 3:TR )
   *         0  |  3
   *        ----+----
   *         1  |  2
   */
  // int getQuad( float x, float y )
  // {
  //   if ( x < 0 ) {
  //     return ( y < 0 )? 0 : 1;
  //   } 
  //   return ( y > 0 )? 2 : 3;
  // }

  // /** quadrants start point
  //  * @param q    quadrant
  //  * @param xc   center X coord
  //  * @param yc   center Y coord
  //  * @param r    radius
  //  */
  // PointF getQuadStart( int q, float xc, float yc, float r )
  // {
  //   switch ( q ) {
  //     case 0: return new PointF( xc,   yc-r ); // Top Left
  //     case 1: return new PointF( xc-r, yc   ); // Bottom Left
  //     case 2: return new PointF( xc,   yc+r ); // Bottom Right
  //     case 3: return new PointF( xc+r, yc   ); // Top Right
  //   }
  //   return null;
  // }
  
  public void doMakeSurface( boolean toast )
  {
    if ( TDSetting.mSketchModelType == 1 ) {
      mModel.makeSurface( mModel.SURFACE_CONVEX_HULL );
    } else if ( TDSetting.mSketchModelType == 2 ) {
      mModel.makeSurface( mModel.SURFACE_POWERCRUST );
    } else if ( toast ) {
      TDToast.make( "no surface type selected" );
    }
  }

  // UNUSED
  // public void removeCurrentSurface( boolean with_sections )
  // {
  //   mModel.removeCurrentSurface( with_sections );
  // }

  @Override
  public boolean onSearchRequested()
  {
    // TDLog.Error( "search requested" );
    Intent intent = new Intent( mActivity, TopoDroidPreferences.class );
    intent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SKETCH );
    mActivity.startActivity( intent );
    return true;
  }

  @Override
  public boolean onKeyDown( int code, KeyEvent event )
  {
    if ( dismissPopups() ) return true;

    switch ( code ) {
      case KeyEvent.KEYCODE_BACK: // HARDWARE BACK (4)
        super.onBackPressed();
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
  // ----------------- ILister interface --------------------------------------------

  @Override
  public void setRefAzimuth( float azimuth, long fixed_extend ) { }
  
  @Override
  public void updateBlockList( CalibCBlock blk ) { }

  // forward to the ShotWindow
  @Override
  public void updateBlockList( DBlock blk )
  {
    mApp.mShotWindow.updateBlockList( blk );
    // FIXME FIXME updateDisplay( true, true );
  }

  @Override
  public void updateBlockList( long blk_id )
  {
    mApp.mShotWindow.updateBlockList( blk_id );
    // FIXME FIXME updateDisplay( true, true );
  }

  @Override
  public void refreshDisplay( int nr, boolean toast ) 
  {
    mActivity.setTitleColor( TDColor.TITLE_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        List<DBlock> list = mData.selectAllShots( mSid, TDStatus.NORMAL );
        recreateNum( list );
      }
      TDToast.make( getResources().getQuantityString(R.plurals.read_data, nr, nr ) );
    } else if ( nr < 0 ) {
      TDToast.make( mApp.DistoXConnectionError[ -nr ] );
    }
  }
   
  @Override
  public void setConnectionStatus( int status )
  {
    /* TODO */
  }

  // --------------------------------------------------
  // MODE

  // private PopupWindow mPopupMode = null;

  /** mode popup
   * @ param b button
   *
  private void makePopupMode( View b )
  {
    if ( mPopupMode != null ) return;

    final Context context = this;
    LinearLayout popup_layout = new LinearLayout(this);
    popup_layout.setOrientation(LinearLayout.VERTICAL);
    int lHeight = LinearLayout.LayoutParams.WRAP_CONTENT;
    int lWidth  = LinearLayout.LayoutParams.WRAP_CONTENT;

    String text = getString(R.string.title_move);
    int len = text.length();
    Button myTextView0 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          dismissPopupMode();
          setMode( SketchDef.MODE_MOVE );
        }
      } );
  
    text = getString(R.string.title_draw);
    if ( len < text.length() ) len = text.length();
    Button myTextView1 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          dismissPopupMode();
          setMode( SketchDef.MODE_DRAW );
        }
      } );
  
    // text = getString(R.string.title_contour);
    // if ( len < text.length() ) len = text.length();
    // Button myTextView2 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
    //   new View.OnClickListener( ) {
    //     public void onClick(View v) {
    //       dismissPopupMode();
    //       setMode( SketchDef.MODE_EDIT );
    //     }
    //   } );

    text = getString(R.string.title_select);
    if ( len > text.length() ) len = text.length();
    Button myTextView3 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          dismissPopupMode();
          setMode( SketchDef.MODE_SELECT );
        }
      } );

    text = getString(R.string.title_step);
    if ( len > text.length() ) len = text.length();
    Button myTextView4 = CutNPaste.makePopupButton( this, text, popup_layout, lWidth, lHeight,
      new View.OnClickListener( ) {
        public void onClick(View v) {
          dismissPopupMode();
          setMode( SketchDef.MODE_STEP );
        }
      } );


    FontMetrics fm = myTextView0.getPaint().getFontMetrics();
    // Log.v("DistoX", "font metrics TOP " + fm.top + " ASC. " + fm.ascent + " BOT " + fm.bottom + " LEAD " + fm.leading ); 
    int w = (int)( Math.abs( ( len + 1 ) * fm.ascent ) * 0.7);
    int h = (int)( (Math.abs(fm.top) + Math.abs(fm.bottom) + Math.abs(fm.leading) ) * 7 * 1.70);
    // int h1 = (int)( myTextView0.getHeight() * 7 * 1.1 ); this is 0
    myTextView0.setWidth( w );
    myTextView1.setWidth( w );
    // myTextView2.setWidth( w );
    myTextView3.setWidth( w );
    myTextView4.setWidth( w );
    // Log.v( TopoDroidApp.TAG, "popup width " + w );
    mPopupMode = new PopupWindow( popup_layout, w, h ); 
    // mPopupMode = new PopupWindow( popup_layout, popup_layout.getHeight(), popup_layout.getWidth() );
    mPopupMode.showAsDropDown(b); 
  }

  private boolean dismissPopupMode()
  {
    if ( mPopupMode != null ) {
      mPopupMode.dismiss();
      mPopupMode = null;
      return true;
    }
    return false;
  }

*/

  public void enableBluetoothButton( boolean enable )
  {
    // TODO
    // mButton1[BTN_BLUETOOTH].setBackgroundDrawable( enable ? mBMbluetooth : mBMbluetooth_no );
    // mButton1[BTN_BLUETOOTH].setEnabled( enable );
  }

}
