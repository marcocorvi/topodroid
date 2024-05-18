/* @file ItemDrawer.java
 *
 * @author marco corvi
 * @date oct 2014
 *
 * @brief TopoDroid label adder interface
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.SymbolType;
import com.topodroid.common.PointScale;

import android.app.Activity;

import android.graphics.pdf.PdfDocument.PageInfo;
import android.graphics.RectF;
import android.graphics.Bitmap;
import android.graphics.Canvas;

import java.io.File;
import java.io.FileOutputStream;

abstract class ItemDrawer extends Activity
{
  static final int POINT_MAX = 32678;
  static final int PDF_MARGIN = 40;

  protected Activity mActivity = null;

  int mCurrentPoint = -1;
  int mCurrentLine  = -1;
  int mCurrentArea  = -1;
  protected int mPointScale;
  protected int mLinePointStep = 1;

  protected int mSymbol = SymbolType.LINE; // kind of symbol being drawn

  // -----------------------------------------------------------
  static final int NR_RECENT = 6; // max is 6
  static Symbol[] mRecentPoint = { null, null, null, null, null, null };
  static Symbol[] mRecentLine  = { null, null, null, null, null, null };
  static Symbol[] mRecentArea  = { null, null, null, null, null, null };
  static int[] mRecentPointAge = { 6, 5, 4, 3, 2, 1 };
  static int[] mRecentLineAge  = { 6, 5, 4, 3, 2, 1 };
  static int[] mRecentAreaAge  = { 6, 5, 4, 3, 2, 1 };
  static Symbol[] mRecentTools = mRecentLine;
  static float mRecentDimX;
  static float mRecentDimY;

  void setPointScale( int scale )
  {
    if ( scale >= PointScale.SCALE_XS && scale <= PointScale.SCALE_XL ) mPointScale = scale;
  }

  int getPointScale() { return mPointScale; }

  // --------------------------------------------------------------
  // MOST RECENT SYMBOLS
  // recent symbols are stored with their filenames
  //
  // update of the "recent" arrays is done either with symbol index, or with symbol itself
  // load and save is done using a string of symbol filenames (separated by space)

  /** update the array of recent points
   * @param point  index of the point in the point library
   * @note section point is excluded from the "recent points" toolbar
   */
  static void updateRecentPoint( int point )
  {
    if ( BrushManager.isPointSection( point ) ) return;
    if ( BrushManager.isPointPicture( point ) ) return;
    updateRecent( BrushManager.getPointByIndex( point ), mRecentPoint, mRecentPointAge );
  }

  /** update the array of recent lines
   * @param line  index of the line in the line library
   */
  static void updateRecentLine( int line )
  {
    updateRecent( BrushManager.getLineByIndex( line ), mRecentLine, mRecentLineAge );
  }

  /** update the array of recent areas
   * @param area  index of the area in the area library
   */
  static void updateRecentArea( int area )
  {
    updateRecent( BrushManager.getAreaByIndex( area ), mRecentArea, mRecentAreaAge );
  }

  /** update the array of recent points
   * @param point  point symbol
   * @note section point is excluded from the "recent points" toolbar
   */
  static void updateRecentPoint( Symbol point ) 
  {
    if ( point.isSection() ) return;
    updateRecent( point, mRecentPoint, mRecentPointAge );
  }

  /** update the array of recent lines
   * @param line  line symbol
   */
  static void updateRecentLine( Symbol line ) { updateRecent( line, mRecentLine, mRecentLineAge ); }

  /** update the array of recent areas
   * @param area  area symbol
   */
  static void updateRecentArea( Symbol area ) { updateRecent( area, mRecentArea, mRecentAreaAge ); }

  /** update a set of recent symbols
   * @param symbol    symbol to update
   * @param symbols   set of recent symbols
   * @param ages      set of recent symbols ages
   * @note used by RecentSymbolsTask
   */
  static void updateRecent( Symbol symbol, Symbol[] symbols, int[] ages )
  {
    if ( symbol == null ) return;
    int kmin = 0;
    for ( int k=0; k<NR_RECENT; ++k ) {
      if ( symbol == symbols[k] ) {
        updateAge( k, ages );
        return;
      } else if ( ages[k] < ages[kmin] ) {
        kmin = k;
      }
    }
    symbols[kmin] = symbol;
    updateAge( kmin, ages );
  }

  /** update a recent symbol age
   * @param kk   index of the recent symbol
   * @param ages      set of recent symbols ages
   */
  static void updateAge( int kk, int[] ages )
  {
    // TDLog.v("AGE kk " + kk );
    int amax = ages[kk];
    for ( int k=0; k<NR_RECENT; ++k ) {
      if ( k != kk && ages[kk] < ages[k] ) { 
        if ( amax < ages[k] ) amax = ages[k];
        -- ages[k];
      }
    }
    ages[kk] = amax;
    // TDLog.v("AGE " + ages[0] + " " + ages[1] + " " + ages[2] + " " + ages[3] + " " + ages[4] + " " + ages[5] );
  }

  /** load the recent symbols sets from the database
   * @param db   database helper
   * @note recent symbols are stored with their th_names
   */
  protected void loadRecentSymbols( DataHelper db )
  {
    ( new RecentSymbolsTask( this, this, db, RecentSymbolsTask.LOAD ) ).execute();

  }

  /** save the recent symbols sets to the database
   * @param db   database helper
   */
  protected void saveRecentSymbols( DataHelper db )
  {
    ( new RecentSymbolsTask( this, this, db, RecentSymbolsTask.SAVE ) ).execute();
  }

  // ----------------------------------------------------------------------
  // TOOL SELECTION

  /** react to the selection of an area symbol
   * @param k      symbol index
   * @param update_recent whether to update the recent symbols set
   */
  public void areaSelected( int k, boolean update_recent ) 
  {
    // TDLog.v("Item drawer point selected: " + k + " update " + update_recent );
    mSymbol = SymbolType.AREA;
    if ( k >= 0 && k < BrushManager.getAreaLibSize() ) {
      mCurrentArea = k;
      if ( TDSetting.mWithLevels > 0 ) {
        if ( ! DrawingLevel.isVisible( BrushManager.getAreaLevel( k ) ) ) {
          mCurrentArea = 0; // BrushManager.mAreaLib.mAreaUserIndex;
        }
      }
    }
    setTheTitle();
    if ( update_recent ) {
      updateRecentArea( mCurrentArea );
      setBtnRecent( SymbolType.AREA );
    }
    mLinePointStep = TDSetting.mLineType;
  }

  /** react to the selection of a line symbol
   * @param k      symbol index
   * @param update_recent whether to update the recent symbols set
   */
  public void lineSelected( int k, boolean update_recent ) 
  {
    // TDLog.v("Item drawer line selected " + k + " update recent " + update_recent );
    mSymbol = SymbolType.LINE;
    if ( k >= 0 && k < BrushManager.getLineLibSize() ) {
      mCurrentLine = k;
      if ( TDSetting.mWithLevels > 0 ) {
        if ( ! DrawingLevel.isVisible( BrushManager.getLineLevel( k ) ) ) {
          // TDLog.v("Item drawer line selected " + k + " is not visible");
          mCurrentLine = 0; // BrushManager.mLineLib.mLineUserIndex;
        }
      }
    }
    setTheTitle();
    if ( update_recent ) {
      // TDLog.v("Item drawer update recent: current line " + mCurrentLine );
      updateRecentLine( mCurrentLine );
      setBtnRecent( SymbolType.LINE );
    }
    mLinePointStep = BrushManager.getLineStyleX( mCurrentLine );
    if ( mLinePointStep != POINT_MAX ) mLinePointStep *= TDSetting.mLineType;
  }

  /** react to the selection of a point symbol
   * @param p      symbol index
   * @param update_recent whether to update the recent symbols set
   */
  public void pointSelected( int p, boolean update_recent )
  {
    mSymbol = SymbolType.POINT;
    // TDLog.v("Item drawer point selected: " + p + " update " + update_recent );
    if ( p >= 0 && p < BrushManager.getPointLibSize() ) {
      mCurrentPoint = p;
      if ( TDSetting.mWithLevels > 0 ) {
        if ( ! DrawingLevel.isVisible( BrushManager.getPointLevel( p ) ) ) {
          mCurrentPoint = 0; // BrushManager.mPointLib.mPointUserIndex;
        }
      }
    }
    setTheTitle();
    if ( update_recent ) {
      updateRecentPoint( mCurrentPoint );
      setBtnRecent( SymbolType.POINT );
    }
  }

  /** set the recent symbols button - empty, to be overridden
   * @param symbol   symbol index
   */
  public void setBtnRecent( int symbol ) { }

  /** set the window tithe - empty, to be overridden
   */
  public void setTheTitle() { }

  // public boolean setCurrentPoint( int k, boolean update_recent ) { }
  // public boolean setCurrentLine( int k, boolean update_recent ) { }
  // public boolean setCurrentArea( int k, boolean update_recent ) { }

  /** react to a notified that recent symbols are loaded
   */
  public void onRecentSymbolsLoaded() { } 

  /** @return whether the point of the given index is "section" and is forbidden
   * @param i   point index
   * @note overridden by DrawingWindow
   */
  public boolean forbidPointSection( int i ) { return false; }

  /** @return whether the line of the given index is "section" and is forbidden
   * @param j   line index
   * @note overridden by DrawingWindow
   */
  public boolean forbidLineSection( int j ) { return false; }


  protected PageInfo getPdfPage( DrawingCommandManager manager )
  {
    float scale = TDSetting.mToPdf;
    RectF bnds = manager.getBitmapBounds( scale );
    bnds = new  RectF((bnds.left   - PDF_MARGIN * scale),
                      (bnds.top    - PDF_MARGIN * scale),
                      (bnds.right  + PDF_MARGIN * scale),
                      (bnds.bottom + PDF_MARGIN * scale)); // HBX
    int zw = (int)(bnds.right - bnds.left); // margin 40 + 80 6.1.76 HBX
    int zh = (int)(bnds.bottom - bnds.top); // HBX
    // TDLog.v( "rect " + bnds.right + " " + bnds.left + " == " + bnds.bottom + " " + bnds.top + " W " + zw + " H " + zh );
    PageInfo.Builder builder = new PageInfo.Builder( zw, zh, 1 ); // API_19
    return builder.create(); // API_19
  }

  private long mScreenshotTime = 0;

  protected void takeScreenshot( DrawingSurface drawing_surface )
  {
    long millis = TDUtil.time();
    if ( millis < mScreenshotTime ) return;
    mScreenshotTime = millis + 1500;
    try {
      // create bitmap screen capture
      // View v1 = getWindow().getDecorView().getRootView();
      // v1.setDrawingCacheEnabled(true);
      // Bitmap bitmap = Bitmap.createBitmap(v1.getDrawingCache());
      // v1.setDrawingCacheEnabled(false);

      Bitmap bitmap = Bitmap.createBitmap( (int)(TopoDroidApp.mDisplayWidth), (int)(TopoDroidApp.mDisplayHeight), Bitmap.Config.ARGB_4444 );
      Canvas canvas = new Canvas( bitmap );
      if ( drawing_surface.drawCanvas( canvas ) ) {
        String now = TDUtil.currentDateTimeFull();
        String path = TDPath.getOutFile( now + ".png" );
        File imageFile = new File(path);
        FileOutputStream outputStream = new FileOutputStream(imageFile);
        int quality = 100;
        // bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        bitmap.compress(Bitmap.CompressFormat.PNG, quality, outputStream);
        outputStream.flush();
        outputStream.close();
        TDToast.make( String.format( getResources().getString( R.string.screenshot_saved ), path ) );
      } else {
        TDLog.e( "failed drawing canvas" );
      }
    } catch (Throwable e) {
      // Several error may come out with file handling or DOM
      e.printStackTrace();
      TDToast.makeWarn( R.string.screenshot_failed );
    }
  }


}
