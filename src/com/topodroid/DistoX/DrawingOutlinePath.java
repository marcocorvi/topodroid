/* @file DrawingOutlinePath.java
 *
 * @author marco corvi
 * @date sept 2017
 *
 * @brief TopoDroid drawing: outline-path
 *
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Matrix;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Iterator;
// import java.util.List;
import java.util.ArrayList;
import java.util.Locale;

import android.util.Log;

/**
 */
public class DrawingOutlinePath
{
  private String mScrap;  // scrap name
  DrawingLinePath mPath;

  public DrawingOutlinePath( String name, DrawingLinePath path )
  {
    mScrap = name;
    mPath  = path;
  }

  boolean isScrap( String name ) { return mScrap.equals( name ); }

}

