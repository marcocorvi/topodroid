/* @file SketchActivity.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid main 3d sketch activity
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
import android.content.pm.ActivityInfo;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.SubMenu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import android.hardware.SensorManager;

import android.util.FloatMath;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.util.List;
import java.util.ArrayList;
// import java.util.Locale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import android.util.Log;

/**
 */
public class SketchActivity extends ItemDrawer
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
{
  static final String TAG = "DistoX";

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
  private int mPointCnt; // counter of points in the currently drawing line

  // private boolean mAllSymbols; // whether the library has all the symbols of the plot
  // private boolean mDoMakeSurface; // whether a surface can be safely made

  // private int mSectionType;     // current section type
  // private boolean mIsInSection; // whether the user is drawing a section curve

  // private Button selectBtn;

  private Button[] mButton1; // primary: draw edit select refs surface download note info one step ( shot join )
  private Button[] mButton2; // draw:    draw edit select undo redo symbol 
  private Button[] mButton3; // edit     draw edit select refineC refineS cut stretch extrude
  private Button[] mButton4; // select   draw edit select next prev cutP refineP
  HorizontalButtonView mButtonView1;
  HorizontalButtonView mButtonView2;
  HorizontalButtonView mButtonView3;
  HorizontalButtonView mButtonView4;
  private int mNrButton1 = 9;          // main-primary
  private int mNrButton2 = 6;          // draw
  private int mNrButton3 = 8;          // edit
  private int mNrButton4 = 7;          // select
  HorizontalListView mListView;
  ListView   mMenu;
  Button     mImage;
  ArrayAdapter< String > mMenuAdapter;
  boolean onMenu;

  private static int izonsok[] = { 
                        R.drawable.iz_edit_ok, // 0
                        R.drawable.iz_eraser_ok,
                        R.drawable.iz_select_ok };
                      
  private static int izons[] = { 
                        R.drawable.iz_edit, // 0
                        R.drawable.iz_eraser,
                        R.drawable.iz_select,
                        R.drawable.iz_one,           // 3
                        R.drawable.iz_mode,          // 4
                        R.drawable.iz_plan,          // 
                        R.drawable.iz_download,      // 
                        R.drawable.iz_note,
                        R.drawable.iz_info,          // 8 

                        R.drawable.iz_undo,          // 9 <--
                        R.drawable.iz_redo,
                        R.drawable.iz_list,          // 11

                        R.drawable.iz_refinet,       // 12
                        R.drawable.iz_refinec,       // 13
                        R.drawable.iz_refines,       
                        R.drawable.iz_cut,
                        R.drawable.iz_stretch,       // 15

                        R.drawable.iz_back,          // 17
                        R.drawable.iz_forw,
                        R.drawable.iz_refinep,
                        R.drawable.iz_note           // 20 Point: delete(cut), Leg: step
                     };

  private static int menus[] = {
                        R.string.menu_export,       // 21 <-- menus
                        R.string.menu_palette, 
                        R.string.menu_delete,
                        R.string.menu_options,
                        R.string.menu_head,
                        R.string.menu_help           // 26
                      };

  private static int help_icons[] = {
                        R.string.help_draw, // 0
                        R.string.help_eraser,
                        R.string.help_edit,
                        R.string.help_one_model,     // 3
                        R.string.help_refs,          // 4
                        R.string.help_surface,
                        R.string.help_download,
                        R.string.help_note,          // 7
                        R.string.help_stats,         // 8 ---------------
                        R.string.help_undo,
                        R.string.help_redo,          // 
                        R.string.help_symbol_plot,   // 11 ----------------
                        R.string.help_refine_triangle, // 12
                        R.string.help_refine_center,
                        R.string.help_refine_sides,
                        R.string.help_cut_model,     // 15
                        R.string.help_stretch_model, // 16
                        R.string.help_previous,
                        R.string.help_next,
                        R.string.help_refine_point,
                        R.string.help_note_plot
                      };
  private static int help_menus[] = {
                        R.string.help_save_model,    // 21 menus
                        R.string.help_symbol,        // 22
                        R.string.help_trash_model,
                        R.string.help_prefs,
                        R.string.help_head_model,
                        R.string.help_help
                      };

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


  // ---------------------------------------------------------------------------
  // helper private methods 

/*
  private void setSymbolButton()
  {
      Resources res = getResources();
      switch ( mSymbol ) {
      case SketchDef.SYMBOL_POINT:
        symbolBtn.setText( res.getString(R.string.btn_point ) );
        break;
      case SketchDef.SYMBOL_AREA:
        symbolBtn.setText( res.getString(R.string.btn_area ) );
        break;
      // case SketchDef.SYMBOL_LINE:
      default:
        symbolBtn.setText( res.getString(R.string.btn_line ) );
        break;
      }
  }
*/  

  @Override
  protected void setTheTitle()
  {
    Resources res = getResources();
    // String dir = mInfo.getDirectionString();
    String symbol_name = // ( mIsInSection) ? "section" :
        ( mSymbol == SketchDef.SYMBOL_POINT )? res.getString(R.string.POINT) + DrawingBrushPaths.mPointLib.getAnyPointName( mCurrentPoint )
      : ( mSymbol == SketchDef.SYMBOL_LINE )? res.getString(R.string.LINE)
      : res.getString(R.string.AREA) + DrawingBrushPaths.mAreaLib.getAreaName( mCurrentArea );

    setTitle( String.format( res.getString( R.string.title_sketch), 
      mInfo.getShotString(),
      mInfo.getDirectionString(),
        ( mMode == SketchDef.MODE_MOVE )? "MOVE"
      : ( mMode == SketchDef.MODE_DRAW )? "DRAW" 
      : ( mMode == SketchDef.MODE_EDIT )? "EDIT"
      // : ( mMode == SketchDef.MODE_HEAD )? "HEAD"
      : "NONE",
      ( mMode == SketchDef.MODE_DRAW )? symbol_name : ""
    ) );


    // switch ( mSelect ) {
    //   case SketchDef.SELECT_SECTION:
    //     selectBtn.setText( res.getString(R.string.btn_section ) );
    //     // selectBtn.setBackgroundColor( 0xff99ff99 );
    //     break;
    //   case SketchDef.SELECT_STEP:
    //     selectBtn.setText( res.getString(R.string.btn_step ) );
    //     // selectBtn.setBackgroundColor( 0xffff9999 );
    //     break;
    //   case SketchDef.SELECT_SHOT:
    //     selectBtn.setText( res.getString(R.string.btn_shot ) );
    //     // selectBtn.setBackgroundColor( 0xffff3333 );
    //     break;
    //   case SketchDef.SELECT_JOIN:
    //     selectBtn.setText( res.getString(R.string.btn_join ) );
    //     // selectBtn.setBackgroundColor( 0xffff3333 );
    //     break;
    // }

    switch ( mMode ) {
      case SketchDef.MODE_MOVE:
        mListView.setAdapter( mButtonView1.mAdapter );
        break;
      case SketchDef.MODE_DRAW:
        mListView.setAdapter( mButtonView2.mAdapter );
        break;
      case SketchDef.MODE_EDIT:
        mSymbol = SketchDef.SYMBOL_LINE;
        mListView.setAdapter( mButtonView3.mAdapter );
        break;
      case SketchDef.MODE_SELECT:
        mListView.setAdapter( mButtonView4.mAdapter );
        break;
      // case SketchDef.MODE_HEAD:
      //   mListView.setAdapter( mButtonView1.mAdapter );
      //   break;
    }
    mListView.invalidate();
  }

  void setMode( int mode )
  {
    mMode = mode;
    setTheTitle();
  }

  private void alertMakeSurface( )
  {
    new TopoDroidAlertDialog( this, getResources(),
                      getResources().getString( R.string.make_surface ),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          // doMakeSurfaceDialog( );
          doMakeSurface( );
        }
      }
    );
  }

  // private void alertMissingSymbols()
  // {
  //   new TopoDroidAlertDialog( this, getResources(),
  //                     getResources().getString( R.string.missing_symbols ),
  //     new DialogInterface.OnClickListener() {
  //       @Override
  //       public void onClick( DialogInterface dialog, int btn ) {
  //         mAllSymbols = true;
  //       }
  //     } );
  // }

  // -------------------------------------------------------------------------
  // SYMBOL-CHOICE methods
  
  public void areaSelected( int k ) 
  {
    if ( k >= 0 && k < DrawingBrushPaths.mAreaLib.mAnyAreaNr ) {
      mSymbol = SketchDef.SYMBOL_AREA;
      mCurrentArea = k;
    }
    setTheTitle();
  }

  public void lineSelected( int k ) 
  {
    if ( k >= 0 && k < DrawingBrushPaths.mLineLib.mAnyLineNr ) {
      mSymbol = SketchDef.SYMBOL_LINE;
      mCurrentLine = k;
    }
    setTheTitle();
  }

  public void pointSelected( int p )
  {
    if ( p >= 0 && p < DrawingBrushPaths.mPointLib.mAnyPointNr ) {
      mSymbol = SketchDef.SYMBOL_POINT;
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
    mInfo.resetZoom3d( mApp.mDisplayWidth/(10*mApp.mScaleFactor),
                       mApp.mDisplayHeight/(10*mApp.mScaleFactor),
                       10 * mApp.mScaleFactor );
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
  //   final Activity currentActivity  = this;
  //   Handler saveHandler = new Handler(){
  //        @Override
  //        public void handleMessage(Message msg) {
  //   //         new TopoDroidAlertDialog( this, getResources(), "Saving sketch",
  //   //         new DialogInterface.OnClickListener() {
  //   //             public void onClick(DialogInterface dialog, int which) {
  //   //                 return;
  //   //             }
  //   //         });
  //        }
  //   } ;
  //   Bitmap bitmap = mSketchSurface.getBitmap();
  //   if ( bitmap == null ) {
  //     Toast.makeText( this, R.string.null_bitmap, Toast.LENGTH_SHORT ).show();
  //   } else {
  //     new ExportBitmapToFile(this, saveHandler, mSketchSurface.getBitmap(), mFullName ).execute();
  //     Toast.makeText( this, getString(R.string.saved_file_) + mFullName + ".png", Toast.LENGTH_SHORT ).show();
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
  //      // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "ExportBitmapToFile " + mFullName );
  //   }

  //   @Override
  //   protected Boolean doInBackground(Intent... arg0)
  //   {
  //     try {
  //       String filename = mApp.getPngFileWithExt( mFullName );
  //       TopoDroidApp.checkPath( filename );
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

  void saveTh3()
  {
    if ( doSaveTh3( false /* ! mAllSymbols */ ) ) {
      Toast.makeText( this, getString(R.string.saved_file_) + mFullName + ".th2", Toast.LENGTH_SHORT ).show();
    }
  }

  private class SaveTh3File extends AsyncTask<Intent,Void,Boolean>
  {
    private Context mContext;
    private Handler mHandler;
    private SketchModel mModel;
    private String mFullName;

    public SaveTh3File( Context context, Handler handler, SketchModel model, String name )
    {
       mContext  = context;
       mHandler  = handler;
       mModel    = model;
       mFullName = name;
       // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "SaveTh3File " + mFullName );
    }

    @Override
    protected Boolean doInBackground(Intent... arg0)
    {
      try {
        String filename = TopoDroidPath.getTh3FileWithExt( mFullName );
        TopoDroidApp.checkPath( filename );
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

  // void doSaveTh3AndReload()
  // {
  //   if ( mFullName != null && mSketchSurface != null ) {
  //     Toast.makeText( this, "save th3 and reload ... wait", Toast.LENGTH_SHORT ).show();
  //     Handler saveHandler = new Handler(){
  //       @Override
  //       public void handleMessage(Message msg) {
  //         String filename = TopoDroidPath.getTh3FileWithExt( mFullName );
  //         mModel.loadTh3( filename, null, mPainter );
  //         mSketchSurface.setModel( mModel );
  //         mSketchSurface.isDrawing = true;
  //         Toast.makeText( mSketchSurface.getContext(), "save th3 and reload done", Toast.LENGTH_SHORT ).show();
  //       }
  //     };
  //     new SaveTh3File(this, saveHandler, mModel, mFullName ).execute();
  //   } else {
  //     Toast.makeText( this, "FAIL save th3 and reload", Toast.LENGTH_SHORT ).show();
  //   }
  // }

  boolean doSaveTh3( boolean not_all_symbols )
  {
    // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, " savingTh3 " + mFullName );
    if ( mFullName != null && mSketchSurface != null ) {
      // if ( not_all_symbols ) {
      //   alertMissingSymbols();
      // }
      // if ( mAllSymbols ) {
        Handler saveHandler = new Handler(){
             @Override
             public void handleMessage(Message msg) {
        //        new TopoDroidAlertDialog( this, getResources(), "Saving sketch",
        //           DialogInterface.OnClickListener() {
        //             public void onClick(DialogInterface dialog, int which) {
        //                 return;
        //             }
        //         });
             }
        } ;
        new SaveTh3File(this, saveHandler, mModel, mFullName ).execute();
      // }
    }
    return false;
  }

  private class SaveDxfFile extends AsyncTask<Intent,Void,Boolean>
  {
      private Context mContext;
      private Handler mHandler;
      private SketchModel mModel;
      private String mFullName;

      public SaveDxfFile( Context context, Handler handler, SketchModel model, String name )
      {
         mContext  = context;
         mHandler  = handler;
         mModel    = model;
         mFullName = name;
         // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "SaveDxfFile " + mFullName );
      }

      @Override
      protected Boolean doInBackground(Intent... arg0)
      {
        try {
          String filename = TopoDroidPath.getDxfFileWithExt( mFullName );
          TopoDroidApp.checkPath( filename );
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
        Handler saveHandler = new Handler(){
             @Override
             public void handleMessage(Message msg) { }
        } ;
        new SaveDxfFile(this, saveHandler, mModel, mFullName ).execute();
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
    mDataDownloader = null;
    // mInfo.zoom = mApp.mScaleFactor;    // canvas zoom

    // setCurrentPaint();
    mCurrentBrush = new DrawingPenBrush();
    mCurrentPoint = 0; // DrawingBrushPaths.POINT_LABEL;
    mCurrentLine  = 0; // DrawingBrushPaths.mLineLib.mLineWallIndex;
    mCurrentArea  = 0; // DrawingBrushPaths.AREA_WATER;

    // mSectionType = SketchSection.SECTION_NONE;
    // mIsInSection = false;

    mSketchSurface = (SketchDrawingSurface) findViewById(R.id.sketchSurface);
    mSketchSurface.previewPath = new DrawingPath( DrawingPath.DRAWING_PATH_LINE );
    mSketchSurface.previewPath.mPath = new Path();
    mSketchSurface.previewPath.setPaint( mPainter.previewPaint );
    mSketchSurface.setOnTouchListener(this);
    // mSketchSurface.setOnLongClickListener(this);
    // mSketchSurface.setBuiltInZoomControls(true);

    DrawingBrushPaths.makePaths( getResources() );

    mData        = mApp.mData; // new DataHelper( this ); 
    Bundle extras = getIntent().getExtras();
    mSid         = extras.getLong(   mApp.TOPODROID_SURVEY_ID );
    String name  = extras.getString( mApp.TOPODROID_SKETCH_NAME );
    mFullName    = mApp.mySurvey + "-" + name;
    // mCompass     = null;

    mInfo      = mApp.mData.getSketch3dInfo( mSid, name );
    mPid         = mInfo.id;
    mInfo.xcenter = mApp.mDisplayWidth/2;
    mInfo.ycenter = mApp.mDisplayHeight/2;

    List<DistoXDBlock> list = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
    if ( list.size() == 0 ) {
      Toast.makeText( this, R.string.few_data, Toast.LENGTH_SHORT ).show();
      finish();
    } else {
      prepareReferences( list );
      computeReferenceFrame( false );

      // mAllSymbols  = true; // by default there are all the symbols

      // now try to load drawings from therion file
      String filename = TopoDroidPath.getTh3FileWithExt( mFullName );
      // SymbolsPalette missingSymbols = new SymbolsPalette();
      // mAllSymbols = 
        mModel.loadTh3( filename, null /* missingSymbols */, mPainter );
      // if ( ! mAllSymbols ) {
      //   // FIXME FIXME
      //   String msg = missingSymbols.getMessage( getResources() );
      //   (new MissingDialog( this, msg )).show();
      // }

      setSurfaceTransform( 0, 0 );
      // mSketchSurface.refresh(); // do not do this --> segfault
      mSketchSurface.setThreadRunning( true );
    }

    // setSymbolButton();
    // selectBtn = (Button) findViewById( R.id.btn_select );
    // selectBtn.setOnClickListener( this );

    mListView = (HorizontalListView) findViewById(R.id.listview);
    int size = mApp.setListViewHeight( mListView );
    // icons00   = ( TopoDroidSetting.mSizeButtons == 2 )? ixons : icons;
    // icons00ok = ( TopoDroidSetting.mSizeButtons == 2 )? ixonsok : iconsok;

    mButton1 = new Button[ mNrButton1 ];
    for ( int k=0; k<mNrButton1; ++k ) {
      mButton1[k] = new Button( this );
      mButton1[k].setPadding(0,0,0,0);
      mButton1[k].setOnClickListener( this );
      // mButton1[k].setBackgroundResource( icons00[k] );
      mApp.setButtonBackground( mButton1[k], size, izons[k] );
    }


    mButton2 = new Button[ mNrButton2 ];
    for ( int k=0; k<mNrButton2; ++k ) {
      mButton2[k] = new Button( this );
      mButton2[k].setPadding(0,0,0,0);
      mButton2[k].setOnClickListener( this );
      if ( k == 0 ) {
        // mButton2[k].setBackgroundResource( icons00ok[k] );
        mApp.setButtonBackground( mButton2[k], size, izonsok[k] );
      } else if ( k < 3 ) {
        // mButton2[k].setBackgroundResource( icons00[k] );
        mApp.setButtonBackground( mButton2[k], size, izons[k] );
      } else {
        // mButton2[k].setBackgroundResource( icons00[9-3+k] );
        mApp.setButtonBackground( mButton2[k], size, izons[9-3+k] );
      }
    }

    mButton3 = new Button[ mNrButton3 ];
    for ( int k=0; k<mNrButton3; ++k ) {
      mButton3[k] = new Button( this );
      mButton3[k].setPadding(0,0,0,0);
      mButton3[k].setOnClickListener( this );
      if ( k == 1 ) {
        // mButton3[k].setBackgroundResource( icons00ok[k] );
        mApp.setButtonBackground( mButton3[k], size, izonsok[k] );
      } else if ( k < 3 ) {
        // mButton3[k].setBackgroundResource( icons00[k] );
        mApp.setButtonBackground( mButton3[k], size, izons[k] );
      } else {
        // mButton3[k].setBackgroundResource( icons00[12-3+k] );
        mApp.setButtonBackground( mButton3[k], size, izons[12-3+k] );
      }
    }

    mButton4 = new Button[ mNrButton4 ];
    for ( int k=0; k<mNrButton4; ++k ) {
      mButton4[k] = new Button( this );
      mButton4[k].setPadding(0,0,0,0);
      mButton4[k].setOnClickListener( this );
      if ( k == 2 ) {
        // mButton4[k].setBackgroundResource( icons00ok[k] );
        mApp.setButtonBackground( mButton4[k], size, izonsok[k] );
      } else if ( k < 3 ) {
        // mButton4[k].setBackgroundResource( icons00[k] );
        mApp.setButtonBackground( mButton4[k], size, izons[k] );
      } else {
        // mButton4[k].setBackgroundResource( icons00[17-3+k] );
        mApp.setButtonBackground( mButton4[k], size, izons[17-3+k] );
      }
    }
    mButtonView1 = new HorizontalButtonView( mButton1 );  // MOVE
    mButtonView2 = new HorizontalButtonView( mButton2 );  // DRAW
    mButtonView3 = new HorizontalButtonView( mButton3 );  // EDIT
    mButtonView4 = new HorizontalButtonView( mButton4 );  // SELECT

    // mListView.setAdapter( mButtonView1.mAdapter ); // done by setTheTitle
    setTheTitle();

    mInfo.resetDirection(); // azi = 0, clino = 0, and compute triad versors
    resetZoom();
    if ( mModel.mCurrentSurface == null ) {
      doMakeSurface( );
    }

    mImage = (Button) findViewById( R.id.handle );
    mImage.setOnClickListener( this );
    // mImage.setBackgroundResource( ( TopoDroidSetting.mSizeButtons == 2 )? R.drawable.ix_menu : R.drawable.ic_menu );
    mApp.setButtonBackground( mImage, size, R.drawable.iz_menu );
    mMenu = (ListView) findViewById( R.id.menu );
    setMenuAdapter();
    closeMenu();
    mMenu.setOnItemClickListener( this );

    mTimer   = null;
  } 

  @Override
  protected synchronized void onStart()
  {
    super.onStart();
    if ( mDataDownloader != null ) {
      mDataDownloader.registerLister( this );
    }
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
    mSketchSurface.isDrawing = false;
    mData.updateSketch( mPid, mSid, mInfo.st1, mInfo.st2, 
                        mInfo.xoffset_top, mInfo.yoffset_top, mInfo.zoom_top, 
                        mInfo.xoffset_side, mInfo.yoffset_side, mInfo.zoom_side, 
                        mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d, 
                        mInfo.east, mInfo.south, mInfo.vert,
                        mInfo.azimuth, mInfo.clino );
    // Toast.makeText( this, R.string.saving_wait, Toast.LENGTH_SHORT ).show();
    // if ( mAllSymbols ) {
      doSaveTh3( false ); // do not alert-dialog on mAllSymbols: in case do not save 
    // } else {
    //   Toast.makeText( this, R.string.missing_save, Toast.LENGTH_SHORT ).show();
    // }
    super.onPause();
  }

  @Override
  protected synchronized void onStop() 
  {   
    if ( mDataDownloader != null ) {
      mDataDownloader.unregisterLister( this );
      mDataDownloader.onPause();
      mApp.disconnectRemoteDevice( false );
    }
    super.onStop();
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


  private void addStationName( NumStation st )
  {
    // mSketchSurface.addStation( st.name, st.e, st.s, st.v ); 
    SketchStationName stn = new SketchStationName( st.name, st.e, st.s, st.v );
    stn.mPaint = DrawingBrushPaths.fixedStationPaint;
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
                         DistoXDBlock blk, boolean splay, boolean is_reference )
  {
    x1 = Sketch3dInfo.mXScale * (x1 - mInfo.east);
    x2 = Sketch3dInfo.mXScale * (x2 - mInfo.east);
    y1 = Sketch3dInfo.mXScale * (y1 - mInfo.south);
    y2 = Sketch3dInfo.mXScale * (y2 - mInfo.south);
    z1 = Sketch3dInfo.mXScale * (z1 - mInfo.vert);
    z2 = Sketch3dInfo.mXScale * (z2 - mInfo.vert);
   
    SketchFixedPath path = null;
    if ( splay ) {
      path = new SketchFixedPath( DrawingPath.DRAWING_PATH_SPLAY, blk, DrawingBrushPaths.fixedSplayPaint );
    } else {
      path = new SketchFixedPath( DrawingPath.DRAWING_PATH_FIXED, blk,
                             is_reference? DrawingBrushPaths.highlightPaint : DrawingBrushPaths.fixedShotPaint );
    }
    if ( path != null ) {
      // path.setEndPoints( x1, y1, x2, y2 ); // scene coords
      path.set3dMidpoint( (x1+x2)/2, (y1+y2)/2, (z1+z2)/2 );
      path.addPoint( x1, y1, z1 );
      path.addPoint( x2, y2, z2 );
      mModel.addFixedPath( path );
    }
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
    DistoXDBlock blk = shot.getFirstBlock();
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

  private void prepareReferences( List<DistoXDBlock> list )
  {
    // mSketchSurface.clearReferences();
    mNum = new DistoXNum( list, mInfo.start, null ); // FIXME null: no barrier
    if ( (! mNum.surveyAttached) && TopoDroidSetting.mCheckAttached ) {
      Toast.makeText( this, R.string.survey_not_attached, Toast.LENGTH_SHORT ).show();
    }
    mModel = new SketchModel( mInfo, mNum, mPainter );
    mSketchSurface.setModel( mModel );
  }

  void recreateNum( List<DistoXDBlock> list )
  {
    mNum = new DistoXNum( list, mInfo.start, null ); // FIXME null: no barrier
    mModel.setNum( mNum );
  }

  void updateNum( ArrayList<DistoXDBlock> list )
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
  
  // private void doSelectAt( float x_scene, float y_scene )
  // {
  //   // Log.v( "DistoX", "doSelectAt at " + x_scene + " " + y_scene );
  //   float d0 = TopoDroidApp.mCloseness;

  //   // SketchPointPath point = mSketchSurface.getPointAt( x_scene, y_scene );
  //   // if ( point != null ) {
  //   //   // new DrawingPointDialog( this, point ).show();
  //   //   return;
  //   // } 

  //   // SketchLinePath line = mSketchSurface.getLineAt( x_scene, y_scene );
  //   // if ( line != null ) {
  //   //   // new DrawingLineDialog( this, line ).show();
  //   //   return;
  //   // }
  //   // 
  //   // SketchAreaPath area = mSketchSurface.getAreaAt( x_scene, y_scene );
  //   // if ( area != null ) {
  //   //   // new DrawingAreaDialog( this, area ).show();
  //   //   return;
  //   // }
  // }

  private SketchFixedPath doSelectShotAt( float x_scene, float y_scene )
  {
    return mModel.selectShotAt( x_scene, y_scene );
  }

  private SketchStationName doSelectStationAt( float x_scene, float y_scene )
  {
    return mModel.selectStationAt( x_scene, y_scene );
  }

  // private boolean doSelectSectionBasePointAt( float x_scene, float y_scene )
  // {
  //   return mModel.selectSectionBasePointAt( x_scene, y_scene );
  // }

  private SketchTriangle doSelectTriangleAt( float x_scene, float y_scene, SketchTriangle tri )
  {
    return mModel.selectTriangleAt( x_scene, y_scene, tri );
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
  //   // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "refresh surface");
  //   mSketchSurface.refresh();
  // }

    
  // --------------------------------------------------------------------------
  // TOUCH

  // private void dumpEvent( WrapMotionEvent ev )
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
  

  private float spacing( WrapMotionEvent ev )
  {
    int np = ev.getPointerCount();
    if ( np < 2 ) return 0.0f;
    float x = ev.getX(1) - ev.getX(0);
    float y = ev.getY(1) - ev.getY(0);
    return FloatMath.sqrt(x*x + y*y);
  }

  // private float direction( WrapMotionEvent ev )
  // {
  //   int np = ev.getPointerCount();
  //   if ( np < 2 ) return 0.0f;
  //   float x = ev.getX(1) - ev.getX(0);
  //   float y = ev.getY(1) - ev.getY(0);
  //   return (float)Math.atan2( y, x );
  // }

  // private float position( WrapMotionEvent ev ) // vertical position
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

    float d0 = TopoDroidSetting.mCloseCutoff + TopoDroidSetting.mCloseness / mInfo.zoom_3d;

    WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "SketchActivity onTouch() " );
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
        // setTitle( R.string.title_move );
        mSaveX = x_canvas;
        mSaveY = y_canvas;
        return false;
      } else if ( mMode == SketchDef.MODE_DRAW || mMode == SketchDef.MODE_EDIT ) {
        mStartX = x_scene;
        mStartY = y_scene;
        // TopoDroidLog.Log( TopoDroidLog.LOG_PLOT, "onTouch ACTION_DOWN symbol " + mSymbol );
        mPointCnt = 0;
        if ( mSymbol == SketchDef.SYMBOL_LINE || mSymbol == SketchDef.SYMBOL_AREA ) {
          // mSketchSurface.isDrawing = true;
          mCurrentLinePath = new DrawingLinePath( mCurrentLine );
          mCurrentLinePath.addStartPoint( x_scene, y_scene );
          mCurrentBrush.mouseDown( mSketchSurface.previewPath.mPath, x_canvas, y_canvas );
        } else { // SketchDef.SYMBOL_POINT
          mSaveX = x_canvas;
          mSaveY = y_canvas;
        }
      } else if ( mMode == SketchDef.MODE_SELECT ) {
        mSaveX = x_canvas;
        mSaveY = y_canvas;
        float xs = mInfo.canvasToSceneX( x_canvas );
        float ys = mInfo.canvasToSceneY( y_canvas );
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
    }
    // ============================== MOVE ===============================
    else if (action == MotionEvent.ACTION_MOVE)
    {
      if ( mTouchMode == SketchDef.TOUCH_MOVE) {
        float x_shift = x_canvas - mSaveX; // compute shift
        float y_shift = y_canvas - mSaveY;
        if ( mMode == SketchDef.MODE_DRAW || mMode == SketchDef.MODE_EDIT ) {
          if ( mSymbol == SketchDef.SYMBOL_LINE || mSymbol == SketchDef.SYMBOL_AREA ) {
            if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > TopoDroidSetting.mLineSegment ) {
              // mSketchSurface.isDrawing = true;
              if ( ++mPointCnt % TopoDroidSetting.mLineType == 0 ) {
                mCurrentLinePath.addPoint( x_scene, y_scene );
                mSaveX = x_canvas;                 // reset start
                mSaveY = y_canvas;
              }
              mCurrentBrush.mouseMove( mSketchSurface.previewPath.mPath, x_canvas, y_canvas );
            }
          }
        } else if ( mMode == SketchDef.MODE_MOVE 
                 || ( mMode == SketchDef.MODE_SELECT && ! mMoveSelected ) ) {
          if ( Math.abs( x_shift ) < 20 && Math.abs( y_shift ) < 20 ) {
            mInfo.rotateBy3d( x_shift/6, -y_shift/6 );
            setTheTitle();
            mModel.setTransform( mInfo.xoffset_3d, mInfo.yoffset_3d, mInfo.zoom_3d );
            mSaveX = x_canvas;                 // reset start
            mSaveY = y_canvas;
          }
        } else if ( mMode == SketchDef.MODE_SELECT && mMoveSelected ) { // moving the selected vertex
          // TODO
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
          if ( mSymbol == SketchDef.SYMBOL_LINE || mSymbol == SketchDef.SYMBOL_AREA ) {
            mCurrentBrush.mouseUp( mSketchSurface.previewPath.mPath, x_canvas, y_canvas );
            mSketchSurface.previewPath.mPath = new Path();
            if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > TopoDroidSetting.mLineSegment || (mPointCnt % TopoDroidSetting.mLineType) > 0 ) {
              mCurrentLinePath.addPoint( x_scene, y_scene );
            }
            if ( mPointCnt > TopoDroidSetting.mLineType ) {
              SketchLinePath line = null;
              if ( mSymbol == SketchDef.SYMBOL_LINE ) {
                line = new SketchLinePath( DrawingPath.DRAWING_PATH_LINE, mCurrentLine, mInfo.st1, mInfo.st2, mPainter );
              } else if ( mSymbol == SketchDef.SYMBOL_AREA ) {
                line = new SketchLinePath( DrawingPath.DRAWING_PATH_AREA, mCurrentArea, mInfo.st1, mInfo.st2, mPainter );
              }
              ArrayList< LinePoint > pts = new ArrayList< LinePoint >();
              for ( LinePoint p = mCurrentLinePath.mFirst; p != null; p = p.mNext ) { pts.add( p ); }
              SketchTriangle tri = null;
              int np = pts.size();
              LinePoint p1 = pts.get(0);
              LinePoint p2 = pts.get(np-1);
              for (LinePoint p : pts ) {
                // find point on the triangulated surface and add it to the line
                tri = doSelectTriangleAt( p.mX, p.mY, tri );
                if ( tri != null /* && mInfo.isForward( tri ) */ ) {
                  Vector q1 = tri.get3dPoint( p.mX, p.mY );
                  line.addLinePoint( mInfo.east + q1.x, mInfo.south + q1.y, mInfo.vert + q1.z );
                }                  
              }

              if ( mSymbol == SketchDef.SYMBOL_LINE ) {
                p1 = pts.get(0);
                p2 = pts.get(pts.size()-1);
                float len = FloatMath.sqrt( (p2.mX-p1.mX)*(p2.mX-p1.mX) + (p2.mY-p1.mY)*(p2.mY-p1.mY) );
                if ( len < SketchDef.CLOSE_GAP ) {
                  line.close();
                }
              } else if ( mSymbol == SketchDef.SYMBOL_AREA ) {
                // Log.v("DistoX", "add area type " + mCurrentArea );
                line.close();
              }
              mModel.addSketchPath( line );
            }
          } else { // SketchDef.SYMBOL_POINT
            if ( Math.abs( x_shift ) < 16 && Math.abs( y_shift ) < 16 ) {
              // Log.v("DistoX", "point get triangle at " + x_scene + " " + y_scene );
              SketchTriangle tri = doSelectTriangleAt( x_scene, y_scene, null );
              if ( tri != null /* && mInfo.isForward( tri ) */ ) {
                Vector p = tri.get3dPoint( x_scene, y_scene );
                // Log.v("DistoX", "new point " + mCurrentPoint + " at " + p.x + " " + p.y + " " + p.z );
                SketchPointPath path = new SketchPointPath( mCurrentPoint, mInfo.st1, mInfo.st2, p.x, p.y, p.z );
                SymbolPointLibrary point_lib = DrawingBrushPaths.mPointLib;
                if ( point_lib.canRotate(mCurrentPoint) ) {
                  float angle = (float)( point_lib.getPointOrientation( mCurrentPoint ) );
                  // Log.v("DistoX", "point " + mCurrentPoint + " angle " + angle );
                  angle *= (float)(Math.PI/180.0);
                  // angles 0:upward 90;rightward 180:downward 270:leftward
                  // scene: x is rightward, y downward
                  // p1 is the 3D point of the orientation (from p to p1)
                  Vector p1 = tri.get3dPoint( x_scene + 0.1f * FloatMath.sin(angle),
                                              y_scene - 0.1f * FloatMath.cos(angle) );
                  path.setOrientation( p1, mInfo );
                }
                mModel.addPoint( path );
                if ( point_lib.pointHasText( mCurrentPoint ) ) {
                  // TODO text dialog
                  new DrawingLabelDialog( this, this, x_scene, y_scene ).show();
                }
              // } else {
              //   Log.v("DistoX", "no triangle found");
              }
            }
          }
          
        }
        else if ( mMode == SketchDef.MODE_EDIT )
        {

          if ( mSymbol == SketchDef.SYMBOL_LINE /* || mSymbol == SketchDef.SYMBOL_AREA */ ) {
            mCurrentBrush.mouseUp( mSketchSurface.previewPath.mPath, x_canvas, y_canvas );
            mSketchSurface.previewPath.mPath = new Path();
            if ( Math.sqrt( x_shift*x_shift + y_shift*y_shift ) > TopoDroidSetting.mLineSegment || (mPointCnt % TopoDroidSetting.mLineType) > 0 ) {
              mCurrentLinePath.addPoint( x_scene, y_scene );
            }

            if ( mPointCnt > TopoDroidSetting.mLineType ) {
              if ( mEdit == SketchDef.EDIT_NONE ) {
                // SketchLinePath edit_line =
                //   new SketchLinePath( DrawingPath.DRAWING_PATH_LINE, mCurrentLine, mInfo.st1, mInfo.st2, mPainter );

                // close mCurrentLinePath adding a point equal to the start one (scene coords)
                mCurrentLinePath.addPoint( mStartX, mStartY );

                boolean skipped_triangle = false;
                // mModel.setEditLine( null );

                LinePoint p1 = mCurrentLinePath.mFirst;
	        SketchTriangle tri1 = doSelectTriangleAt( p1.mX, p1.mY, null );
                SketchTriangle tri10 = tri1;
                Vector w10 = null;
                Vector w20 = null;
                Vector w120 = null;

                if ( tri1 != null /* && mInfo.isForward( tri1 ) */ ) {
                  ArrayList< SketchRefinement > refines = new ArrayList<SketchRefinement>();
                  // ArrayList< PointF > border = new ArrayList< PointF >();
                  SketchTriangle tri2 = tri1;
                  SketchVertex u1 = null, u2 = null; // previous side of tri2
                  SketchVertex w1 = null, w2 = null; // current side of tri2
                  Vector u12 = null;
                  Vector w12 = null;
                  PointF border_point = new PointF(0,0);

                  for ( LinePoint p2 = p1.mNext; p2 != null; p2 = p2.mNext ) { 
                    tri2 = doSelectTriangleAt( p2.mX, p2.mY, tri2 );
                    if ( tri2 != null && tri2 != tri1 ) {
                      int i1 = tri1.i;
                      if ( i1 != tri2.i && i1 != tri2.j && i1 != tri2.k ) i1 = -1;
                      int j1 = tri1.j;
                      if ( j1 != tri2.i && j1 != tri2.j && j1 != tri2.k ) j1 = -1;
                      int k1 = tri1.k;
                      if ( k1 != tri2.i && k1 != tri2.j && k1 != tri2.k ) k1 = -1;
                      if ( i1 >= 0 ) {
                        if ( j1 >= 0 ) { // side v1--v2 of tri1
                          w1 = tri1.v1;
                          w2 = tri1.v2;
                          // intersect p1--p2 with tri2.p1--tri2.p2
                          float t = Geometry2D.intersect( p1, p2, tri1.p1, tri1.p2, border_point );
                          // w12 = addPointToEditLine( edit_line, w1, w2, t );
                          w12 = makeEditLinePoint( w1, w2, t );
                        } else if ( k1 >= 0 ) { // side v3--v1
                          w1 = tri1.v3;
                          w2 = tri1.v1;
                          float t = Geometry2D.intersect( p1, p2, tri1.p3, tri1.p1, border_point );
                          // w12 = addPointToEditLine( edit_line, w1, w2, t );
                          w12 = makeEditLinePoint( w1, w2, t );
                        } else { // vertex v1
                          w1 = tri1.v1;
                          w2 = tri1.v1;
                          // w12 = addPointToEditLine( edit_line, w1 );
                          w12 = makeEditLinePoint( w1 );
                          // border_point.x = tri1.p1.x;
                          // border_point.y = tri1.p1.y;
                        }
                      } else if ( j1 >= 0 ) {
                        if ( k1 >= 0 ) { // side v2--v3
                          w1 = tri1.v2;
                          w2 = tri1.v3;
                          float t = Geometry2D.intersect( p1, p2, tri1.p2, tri1.p3, border_point );
                          // w12 = addPointToEditLine( edit_line, w1, w2, t );
                          w12 = makeEditLinePoint( w1, w2, t );
                        } else { // vertex v2
                          w1 = tri1.v2;
                          w2 = tri1.v2;
                          // w12 = addPointToEditLine( edit_line, w1 );
                          w12 = makeEditLinePoint( w1 );
                          // border_point.x = tri1.p2.x;
                          // border_point.y = tri1.p2.y;
                        }
                      } else if ( k1 >= 0 ) { // vertex v3
                        w1 = tri1.v3;
                        w2 = tri1.v3;
                        // w12 = addPointToEditLine( edit_line, w1 );
                        w12 = makeEditLinePoint( w1 );
                        // border_point.x = tri1.p3.x;
                        // border_point.y = tri1.p3.y;
                      } else {
                        Log.v("DistoX", "WARNING edit_line jumps over a triangle");
                        skipped_triangle = true;
                      }
                      // border.add( new PointF( border_point.x, border_point.y ) );
                      if ( u1 != null ) {
                        if ( u2 == w1 ) {        // u1 --(u12)--> u2=w1 --(w12)--> w2
                          refines.add( new SketchRefinement( tri1, u2, w12, u12 ) );
                        } else if ( u1 == w2 ) { // w1 --(w12)--> w2=u1 --(u12)--> u2
                          refines.add( new SketchRefinement( tri1, u1, u12, w12 ) );
                        }
                      } else {
                        w10 = w1;
                        w20 = w2;
                        w120 = w12;
                      }
                      tri1 = tri2;
                      u1   = w2;
                      u2   = w1;
                      u12  = w12;
                    }
                    p1 = p2;
                  }
                  if ( u2 == w10 ) { // u1 --> u2=w1 --> w2
                    refines.add( new SketchRefinement( tri10, u2, w120, u12 ) );
                  } else if ( u1 == w20 ) { // w1 --> w2=u1 --> u2
                    refines.add( new SketchRefinement( tri10, u1, u12, w120 ) );
                  }
                  if ( skipped_triangle ) {
                    Toast.makeText( this, R.string.few_line_points, Toast.LENGTH_SHORT ).show();
                  } else {

                    // Log.v("DistoX", "refinements " + refines.size() );
                    // NOW GET the inside triangles. extend border a little
                    // float x = 0;  
                    // float y = 0;
                    // for ( PointF p : border ) { x += p.x; y += p.y; }
                    // x /= border.size();
                    // y /= border.size();
                    // // StringWriter sw = new StringWriter();
                    // // PrintWriter pw = new PrintWriter( sw );
                    // for ( PointF p : border ) {
                    //   p.x += 0.1f * (p.x - x );
                    //   p.y += 0.1f * (p.y - y );
                    //   // pw.format(Locale.ENGLISH, "%.2f %.2f ", p.x, p.y );
                    // }
                    // Log.v( "DistoX", " border " + sw.getBuffer().toString() );

                    mModel.setRefinement( refines );
                    // mModel.setEditLine( edit_line );
                    // mModel.setBorder( border );

                    // mModel.doRefinement( );

                    // Log.v( "DistoX", "border " + border.size() + " inside triangles " + ntin );
                  }
                }
              } else if ( mEdit == SketchDef.EDIT_STRETCH /* || mEdit == SketchDef.EDIT_EXTRUDE */ ) {
                // TODO convert mCurrentLinePath (DrawingLinePath) to 3D line
                ArrayList<Vector> pts = new ArrayList<Vector>();

                LinePoint p1 = mCurrentLinePath.mFirst;
                float x = mInfo.canvasToSceneX( p1.mX );
                float y = mInfo.canvasToSceneY( p1.mY );
                pts.add( mInfo.sceneToWorld( x, y ) );
                for ( LinePoint p2 = p1.mNext; p2 != null; p2 = p2.mNext ) {
                  x = mInfo.canvasToSceneX( p2.mX );
                  y = mInfo.canvasToSceneY( p2.mY );
                  pts.add( mInfo.sceneToWorld( x, y ) );
                }

                // if ( mEdit == SketchDef.EDIT_EXTRUDE ) { // line is the path along which to extrude the new surface
                //   mModel.makeExtrude( pts );
                // } else if ( mEdit == SketchDef.EDIT_STRETCH ) {
                  mModel.makeStretch( pts );
                // }
                mEdit = SketchDef.EDIT_NONE;
              }
              // NOTE EDIT_CUT is done immediately on the surface insideTriangles
            }
          }
        } else if ( mMode ==  SketchDef.MODE_SELECT ) {
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
    new TopoDroidAlertDialog( this, getResources(),
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
      //   // FIXME (new SketchSelectDialog(this, this)).show();
      // } else 

      if ( ( b == mButton2[0] && mMode == SketchDef.MODE_DRAW ) || 
                  ( b == mButton3[1] && mMode == SketchDef.MODE_EDIT ) ||
                  ( b == mButton4[2] && mMode == SketchDef.MODE_SELECT ) ) { 
        setMode( SketchDef.MODE_MOVE );
      } else if ( b == mButton1[0] || b == mButton3[0] ||  b == mButton4[0] ) { // 0 --> DRAW
        setMode( SketchDef.MODE_DRAW );
      } else if ( b == mButton1[1] || b == mButton2[1] ||  b == mButton4[1] ) { // 1--> EDIT
        setMode( SketchDef.MODE_EDIT ); 
      } else if ( b == mButton1[2] || b == mButton2[2] ||  b == mButton3[2] ) { // 2--> SELECT
        setMode( SketchDef.MODE_SELECT );

      } else if ( b == mButton1[3] ) { // one
        mInfo.resetDirection(); // azi = 0, clino = 0, and compute triad versors
        resetZoom();
        // doSaveTh3AndReload( ); // save to th3
        // mSketchSurface.isDrawing = true;
      } else if ( b == mButton1[4] ) { // display mode. cycle (NGBH, SINGLE, ALL)
        (new SketchModeDialog( this, mModel )).show();
      } else if ( b == mButton1[5] ) { // surface
        if ( mModel.mCurrentSurface != null ) {
          alertMakeSurface( );
        } else {
          doMakeSurface( );
        }
      } else if ( b == mButton1[6] ) { // download
        if ( mApp.mDevice != null ) {
          // TODO if there is an empty shot use it, else try to download the data
          //      with the Asynch task that download the data.
          //      if there is an empty shot assign it
          setTitleColor( TopoDroidConst.COLOR_CONNECTED );
          ArrayList<ILister> listers = new ArrayList<ILister>();
          listers.add( this );
          new DistoXRefresh( mApp, listers ).execute();
        } else {
          Toast.makeText( this, R.string.device_none, Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton1[7] ) { // notes
        (new DistoXAnnotations( this, mData.getSurveyFromId(mSid) )).show();
      } else if ( b == mButton1[8] ) { // info
        new DistoXStatDialog( mSketchSurface.getContext(), mNum ).show();

      } else if ( b == mButton2[3] ) { // undo
        mModel.undo();
      } else if ( b == mButton2[4] ) { // redo
        mModel.redo();
      } else if ( b == mButton2[5] ) { // symbols
        new ItemPickerDialog(this, this, mType ).show();

      } else if ( b == mButton3[3] ) { // refine triangles
        //   extrudeRegion();
        // Log.v("DistoX", "refine to max side ");
        int split = mModel.refineToMaxSide( TopoDroidSetting.mSketchSideSize );
        if ( split == 0 ) { 
          Toast.makeText( this, getString(R.string.sketch_no_split), Toast.LENGTH_SHORT ).show();
        }
      } else if ( b == mButton3[4] ) { // refine_center
        mModel.refineSurfaceAtCenters();
      } else if ( b == mButton3[5] ) { // refine_sides
        mModel.refineSurfaceAtSides();
      } else if ( b == mButton3[6] && mCurrentLinePath != null ) { // cut
        cutRegion();
      } else if ( b == mButton3[7] && mCurrentLinePath != null ) { // stretch
        stretchRegion();

      } else if ( b == mButton4[3] ) { 
        // TODO previous
      } else if ( b == mButton4[4] ) { 
        // TODO next
      } else if ( b == mButton4[5] ) { 
        mModel.refineSurfaceAtSelectedVertex();
      } else if ( b == mButton4[6] ) { 
        // TODO edit item
      }
    }

    // void setSelect( int select )
    // {
    //   mSelect = select;
    //   setMode( ( select != SketchDef.SELECT_NONE )? SketchDef.MODE_EDIT : SketchDef.MODE_MOVE );
    // }

    // ----------------------------------------------------------------
    // SECTION

    // void setSectionType( int type )
    // { 
    //   mModel.setSectionType( type );
    //   mSectionType = type;
    // }

    void startSectionDialog()
    {
    }

    void addSection()
    {
    }

    // ----------------------------------------------------------------

    // void setHeading( float heading )
    public void setBearingAndClino( float heading, float clino )
    {
      mInfo.resetDirection( heading, 0.0f ); // from the side
      // mCompass = null;
      setMode( SketchDef.MODE_MOVE );
    }


    // ----------------------------------------------
    /* MENU

    private MenuItem mMIsave;
    private MenuItem mMIoptions;
    private MenuItem mMIdelete;
    private MenuItem mMIsymbol;
    private MenuItem mMIhead;
    private MenuItem mMIhelp;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
      mMIsave    = menu.add( R.string.menu_export );
      mMIsymbol  = menu.add( R.string.menu_palette );
      mMIdelete  = menu.add( R.string.menu_delete );
      mMIoptions = menu.add( R.string.menu_options );
      mMIhead    = menu.add( R.string.menu_head );
      mMIhelp    = menu.add( R.string.menu_help );

      mMIsave.setIcon( R.drawable.ic_export0 );
      mMIsymbol.setIcon( R.drawable.ic_symbol );
      mMIdelete.setIcon( R.drawable.ic_trash );
      mMIoptions.setIcon( R.drawable.ic_pref );
      mMIhead.setIcon( R.drawable.ic_head );
      mMIhelp.setIcon( R.drawable.ic_help );

      return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
      // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "SketchActivity onOptionsItemSelected() " + item.toString() );

      if (item == mMIdelete ) {
        // TODO ask for confirmation: however file th3 is not deleted
        askDelete();
      } else if ( item == mMIoptions ) { // OPTIONS DIALOG
        Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
        optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SKETCH );
        startActivity( optionsIntent );
      } else if (item == mMIsymbol ) {
        DrawingBrushPaths.makePaths( getResources() );
        (new SymbolEnableDialog( this, this )).show();
      } else if (item == mMIsave ) {
        new SketchSaveDialog( this, this ).show();
        // saveTh3( );
      } else if (item == mMIhead ) {
        // setMode( SketchDef.MODE_HEAD );
        setMode( SketchDef.MODE_MOVE );
        // SensorManager sm = (SensorManager)getSystemService( Context.SENSOR_SERVICE );
        // mCompass = new SketchCompassSensor( this, sm, TopoDroidSetting.mCompassReadings );
        mTimer = new TimerTask( this, this );
        mTimer.execute();
      } else if (item == mMIhelp ) {
        int nn = mNrButton1 + mNrButton2 - 3 + mNrButton3 - 3 + mNrButton4 - 3;
        (new HelpDialog(this, izons, menus, help_icons, help_menus, nn, 6 ) ).show();
      }
      return true;
    }

    */

  private void setMenuAdapter()
  {
    Resources res = getResources();
    mMenuAdapter = new ArrayAdapter<String>(this, R.layout.menu );
    mMenuAdapter.add( res.getString( menus[0] ) );
    mMenuAdapter.add( res.getString( menus[1] ) );
    mMenuAdapter.add( res.getString( menus[2] ) );
    mMenuAdapter.add( res.getString( menus[3] ) );
    mMenuAdapter.add( res.getString( menus[4] ) );
    mMenuAdapter.add( res.getString( menus[5] ) );
    mMenu.setAdapter( mMenuAdapter );
    mMenu.invalidate();
  }

  private void closeMenu()
  {
    mMenu.setVisibility( View.GONE );
    onMenu = false;
  }

  void doDelete()
  {
    mData.deleteSketch( mPid, mSid );
    finish();
  }

  private void askDelete()
  {
    new TopoDroidAlertDialog( this, getResources(),
                      getResources().getString( R.string.sketch_delete ),
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          doDelete();
        }
    } );
  }

  @Override 
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
  {
    if ( mMenu == (ListView)parent ) {
      closeMenu();
      int p = 0;
      if ( p++ == pos ) { // EXPORT
        new SketchSaveDialog( this, this ).show();
      } else if ( p++ == pos ) { // PALETTE 
        DrawingBrushPaths.makePaths( getResources() );
        (new SymbolEnableDialog( this, this )).show();
      } else if ( p++ == pos ) { // DELETE
        askDelete();
      } else if ( p++ == pos ) { // 
        Intent optionsIntent = new Intent( this, TopoDroidPreferences.class );
        optionsIntent.putExtra( TopoDroidPreferences.PREF_CATEGORY, TopoDroidPreferences.PREF_CATEGORY_SKETCH );
        startActivity( optionsIntent );
      } else if ( p++ == pos ) { // 
        setMode( SketchDef.MODE_MOVE );
        // SensorManager sm = (SensorManager)getSystemService( Context.SENSOR_SERVICE );
        // mCompass = new SketchCompassSensor( this, sm, TopoDroidSetting.mCompassReadings );
        mTimer = new TimerTask( this, this );
        mTimer.execute();
      } else if ( p++ == pos ) { // 
        int nn = mNrButton1 + mNrButton2 - 3 + mNrButton3 - 3 + mNrButton4 - 3;
        (new HelpDialog(this, izons, menus, help_icons, help_menus, nn, 6 ) ).show();
      }
    }
  }

  // ------------------------------------------------------------------------
  // HIGHLIGHT - EXTRUSION


  // void highlightRegion() { mModel.highlightLineRegion( ); }

  private void cutRegion() 
  { 
    mModel.doRefinement();
    mModel.makeCut( );
  }

  // private void extrudeRegion() { prepareEditRegion( SketchDef.EDIT_EXTRUDE ); }

  private void stretchRegion() { prepareEditRegion( SketchDef.EDIT_STRETCH ); }

  private void prepareEditRegion( int edit )
  {
    mModel.doRefinement();
    mEdit   = edit;
    mSymbol = SketchDef.SYMBOL_LINE;
    mCurrentLine = 0;
    mTouchMode = SketchDef.TOUCH_MOVE;
    setMode( SketchDef.MODE_EDIT );
    // computeReferenceFrame( false );
    // setSurfaceTransform( 0, 0 );
  }


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

  /** quadrants start point
   * @param q    quadrant
   * @param xc   center X coord
   * @param yc   center Y coord
   * @param r    radius
   */
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
  
  public void doMakeSurface( )
  {
    // FIXME CONVEX HULL SURFACE
    // mModel.makeSurface();
    mModel.makeConvexSurface();
  }

  public void removeSurface( boolean with_sections )
  {
    mModel.removeSurface( with_sections );
  }

  // private void doMakeSurfaceDialog( )
  // {
  //   // new SketchSurfaceDialog( mSketchSurface.getContext(), this ).show();
  //   mModel.computeShapeSize();
  //   new SketchShape( mSketchSurface.getContext(), mModel.mShapeSize, this ).show();
  // }

  // ----------------- ILister interface --------------------------------------------
  
  @Override
  public void updateBlockList( DistoXDBlock blk )
  {
    mApp.mShotActivity.updateBlockList( blk );
    // FIXME FIXME updateDisplay( true );
  }

  @Override
  public void refreshDisplay( int nr, boolean toast ) 
  {
    setTitleColor( TopoDroidConst.COLOR_NORMAL );
    if ( nr >= 0 ) {
      if ( nr > 0 ) {
        List<DistoXDBlock> list = mData.selectAllShots( mSid, TopoDroidApp.STATUS_NORMAL );
        recreateNum( list );
      }
      Toast.makeText( this, getString(R.string.read_) + nr + getString(R.string.data), Toast.LENGTH_SHORT ).show();
    } else if ( nr < 0 ) {
      Toast.makeText( this, mApp.DistoXConnectionError[ -nr ], Toast.LENGTH_SHORT ).show();
    }
  }
   
  public void setConnectionStatus( boolean connected )
  {
    /* TODO */
  }

  public void notifyDisconnected()
  {
  }

}
