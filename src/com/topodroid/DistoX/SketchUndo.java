/* @file SketchUndo.java
 *
 * @author marco corvi
 * @date jan 2013
 *
 * @brief TopoDroid 3d sketch: undo struct
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.util.Log;

/**
 */
class SketchUndo
{
  static final int UNDO_PATH = 0;
  static final int UNDO_SECTION = 1;
  // private static final String TAG = "DistoX";

  int mType;
  SketchPath mPath;
  // SketchSection mSection;

  SketchUndo mNext;  // next undo in the chain

  SketchUndo( SketchUndo next, SketchPath path )
  {
    mType = UNDO_PATH;
    mPath = path;
    // mSection = null;
    mNext = next;
  }

  // public SketchUndo( SketchUndo next, SketchSection section )
  // {
  //   mType = UNDO_SECTION;
  //   mPath = null;
  //   mSection = section;
  //   mNext = next;
  // }

}
