/** @file ItemDrawer.java
 *
 * @author marco corvi
 * @date oct 2014
 *
 * @brief TopoDroid label adder interfare
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 */
package com.topodroid.DistoX;

import android.app.Activity;

import android.util.Log;

public class ItemDrawer extends Activity
{
  public static final int SYMBOL_POINT = 1;
  public static final int SYMBOL_LINE  = 2;
  public static final int SYMBOL_AREA  = 3;

  int mCurrentPoint;
  int mCurrentLine;
  int mCurrentArea;
  protected int mPointScale;
  protected int mLinePointStep = 1;

  int mSymbol = SYMBOL_LINE; // kind of symbol being drawn

  // -----------------------------------------------------------
  static Symbol mRecentPoint[] = { null, null, null, null, null, null };
  static Symbol mRecentLine[]  = { null, null, null, null, null, null };
  static Symbol mRecentArea[]  = { null, null, null, null, null, null };
  static final int NR_RECENT = 6; // max is 6

  void setPointScale( int scale )
  {
    if ( scale >= DrawingPointPath.SCALE_XS && scale <= DrawingPointPath.SCALE_XL ) mPointScale = scale;
  }

  int getPointScale() { return mPointScale; }

  // --------------------------------------------------------------
  // MOST RECENT SYMBOLS
  // recent symbols are stored with their filenames
  //
  // update of the "recent" arrays is done either with symbol index, or with symbol itself
  // load and save is done using a string of symbol filenames (separated by space)

  static void updateRecentPoint( int point )
  {
    // if ( DrawingBrushPaths.mPointLib == null ) return;
    updateRecent( DrawingBrushPaths.mPointLib.getSymbolByIndex( point ), mRecentPoint );
  }

  static void updateRecentLine( int line )
  {
    // if ( DrawingBrushPaths.mLineLib == null ) return;
    updateRecent( DrawingBrushPaths.mLineLib.getSymbolByIndex( line ), mRecentLine );
  }

  static void updateRecentArea( int area )
  {
    // if ( DrawingBrushPaths.mAreaLib == null ) return;
    updateRecent( DrawingBrushPaths.mAreaLib.getSymbolByIndex( area ), mRecentArea );
  }

  static void updateRecentPoint( Symbol point ) { updateRecent( point, mRecentPoint ); }

  static void updateRecentLine( Symbol line ) { updateRecent( line, mRecentLine ); }

  static void updateRecentArea( Symbol area ) { updateRecent( area, mRecentArea ); }

  private static void updateRecent( Symbol symbol, Symbol symbols[] )
  {
    if ( symbol == null ) return;
    for ( int k=0; k<NR_RECENT; ++k ) {
      if ( symbol == symbols[k] ) {
        for ( ; k > 0; --k ) symbols[k] = symbols[k-1];
        symbols[0] = symbol;
        break;
      }
    }
    if ( symbols[0] != symbol ) {
      for ( int k = NR_RECENT-1; k > 0; --k ) symbols[k] = symbols[k-1];
      symbols[0] = symbol;
    }
  }

  // recent symbols are stored with their th_names
  //
  protected void loadRecentSymbols( DataHelper data )
  {
    DrawingBrushPaths.mPointLib.setRecentSymbols( mRecentPoint );
    DrawingBrushPaths.mLineLib.setRecentSymbols( mRecentLine );
    DrawingBrushPaths.mAreaLib.setRecentSymbols( mRecentArea );

    String names = data.getValue( "recent_points" );
    if ( names != null ) {
      String points[] = names.split(" ");
      for ( String point : points ) {
        updateRecent( DrawingBrushPaths.mPointLib.getSymbolByFilename( point ), mRecentPoint );
      }
    }
    names = data.getValue( "recent_lines" );
    if ( names != null ) {
      String lines[] = names.split(" ");
      for ( String line : lines ) {
        updateRecent( DrawingBrushPaths.mLineLib.getSymbolByFilename( line ), mRecentLine );
      }
    }
    names = data.getValue( "recent_areas" );
    if ( names != null ) {
      String areas[] = names.split(" ");
      for ( String area : areas ) {
        updateRecent( DrawingBrushPaths.mAreaLib.getSymbolByFilename( area ), mRecentArea );
      }
    }
  }

  protected void saveRecentSymbols( DataHelper data )
  {
    StringBuilder points = new StringBuilder( mRecentPoint[0].mThName );
    for ( int k=1; k<NR_RECENT; ++k ) {
      if ( mRecentPoint[k] == null ) break;
      points.append( " " + mRecentPoint[k].mThName );
    }
    data.setValue( "recent_points", points.toString() );

    StringBuilder lines = new StringBuilder( mRecentLine[0].mThName );
    for ( int k=1; k<NR_RECENT; ++k ) {
      if ( mRecentLine[k] == null ) break;
      lines.append( " " + mRecentLine[k].mThName );
    }
    data.setValue( "recent_lines", lines.toString() );

    StringBuilder areas = new StringBuilder( mRecentArea[0].mThName );
    for ( int k=1; k<NR_RECENT; ++k ) {
      if ( mRecentArea[k] == null ) break;
      lines.append( mRecentArea[k].mThName );
    }
    data.setValue( "recent_areas", areas.toString() );
  }

  // ----------------------------------------------------------------------
  // SELECTION

    public void areaSelected( int k, boolean update_recent ) 
    {
      mSymbol = SYMBOL_AREA;
      if ( k >= 0 && k < DrawingBrushPaths.mAreaLib.mSymbolNr ) {
        mCurrentArea = k;
      }
      setTheTitle();
      if ( update_recent ) {
        updateRecentArea( mCurrentArea );
      }
      mLinePointStep = TopoDroidSetting.mLineType;
    }

    public void lineSelected( int k, boolean update_recent ) 
    {
      mSymbol = SYMBOL_LINE;
      if ( k >= 0 && k < DrawingBrushPaths.mLineLib.mSymbolNr ) {
        mCurrentLine = k;
      }
      setTheTitle();
      if ( update_recent ) {
        updateRecentLine( mCurrentLine );
      }
      mLinePointStep = TopoDroidSetting.mLineType * DrawingBrushPaths.mLineLib.getStyleX( mCurrentLine );
    }

    public void pointSelected( int p, boolean update_recent )
    {
      mSymbol = SYMBOL_POINT;
      if ( p >= 0 && p < DrawingBrushPaths.mPointLib.mSymbolNr ) {
        mCurrentPoint = p;
      }
      setTheTitle();
      if ( update_recent ) {
        updateRecentPoint( mCurrentPoint );
      }
    }


    protected void setTheTitle() 
    {
      // Log.v("DistoX", "ItemDrawer::setTheTitle() ");
    }

}
