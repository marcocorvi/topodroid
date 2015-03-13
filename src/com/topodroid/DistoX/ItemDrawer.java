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

    int mSymbol = SYMBOL_LINE; // kind of symbol being drawn

    // ----------------------------------------------------------------------
    // SELECTION

    public void areaSelected( int k ) 
    {
      mSymbol = SYMBOL_AREA;
      if ( k >= 0 && k < DrawingBrushPaths.mAreaLib.mAnyAreaNr ) {
        mCurrentArea = k;
      }
      setTheTitle();
    }

    public void lineSelected( int k ) 
    {
      mSymbol = SYMBOL_LINE;
      if ( k >= 0 && k < DrawingBrushPaths.mLineLib.mAnyLineNr ) {
        mCurrentLine = k;
      }
      setTheTitle();
    }

    public void pointSelected( int p )
    {
      mSymbol = SYMBOL_POINT;
      if ( p >= 0 && p < DrawingBrushPaths.mPointLib.mAnyPointNr ) {
        mCurrentPoint = p;
      }
      setTheTitle();
    }

    protected void setTheTitle() 
    {
      Log.v("DistoX", "ItemDrawer::setTheTitle() ");
    }

}
