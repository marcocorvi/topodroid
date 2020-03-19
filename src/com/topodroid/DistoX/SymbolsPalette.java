/* @file SymbolsPalette.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid missing drawing symbols collections
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

// import java.util.ArrayList;
import java.util.TreeSet;
// import java.util.Collections;
// import java.util.Iterator;
// import java.util.List;

// import android.content.res.Resources;

/**
 */
class SymbolsPalette
{
  TreeSet< String > mPalettePoint;  // filenames == th_names without "u:" if the case
  TreeSet< String > mPaletteLine;
  TreeSet< String > mPaletteArea;

  SymbolsPalette( )
  {
    mPalettePoint = new TreeSet< String >();
    mPaletteLine  = new TreeSet< String >();
    mPaletteArea  = new TreeSet< String >();
  }

  void resetSymbolLists()
  {
    mPalettePoint.clear();
    mPaletteLine.clear();
    mPaletteArea.clear();
  }

  // N.B. TreeSet.add adds the type if it is not already present
  void addPointFilename( String type ) { mPalettePoint.add( type ); }

  void addLineFilename( String type ) { mPaletteLine.add( type ); }

  void addAreaFilename( String type ) { mPaletteArea.add( type ); }

  boolean isOK() { return mPalettePoint.size() == 0 && mPaletteLine.size() == 0 && mPaletteArea.size() == 0; }


  // public String getMessage( Resources res )
  // {
  //   String prev = "";
  //   StringWriter sw = new StringWriter();
  //   PrintWriter pw  = new PrintWriter( sw );
  //   pw.format( "%s\n",  res.getString( R.string.missing-warning ) );
  //   if ( mPalettePoint.size() > 0 ) {
  //     pw.format( "%s:", res.getString( R.string.missing-point ) );
  //     for ( String p : mPalettePoint ) {
  //       if ( ! p.equals(prev) ) pw.format( " %s", p );
  //     }
  //     pw.format( "\n");
  //   }
  //   if ( mPaletteLine.size() > 0 ) {
  //     pw.format( "%s:", res.getString( R.string.missing-line ) );
  //     prev = "";
  //     for ( String p : mPaletteLine ) {
  //       if ( ! p.equals(prev) ) pw.format( " %s", p );
  //     }
  //     pw.format( "\n");
  //   }
  //   if ( mPaletteArea.size() > 0 ) {
  //     pw.format( "%s:", res.getString( R.string.missing-area ) );
  //     prev = "";
  //     for ( String p : mPaletteArea ) {
  //       if ( ! p.equals(prev) ) pw.format( " %s", p );
  //     }
  //     pw.format( "\n");
  //   }
  //   pw.format( "%s\n",  res.getString( R.string.missing-hint ) );
  //   return sw.getBuffer().toString();
  // }
}
