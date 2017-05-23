/* @file SketchLoader.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid 3d-sketch loader
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.widget.Toast;
import android.os.AsyncTask;


public class SketchLoader extends AsyncTask< String, Integer, Integer >
{
  private SketchWindow  mParent;
  private SketchModel   mModel;
  private String        mFullName;
  private SketchPainter mPainter;
  private static SketchLoader running = null;

  SketchLoader( SketchWindow parent, SketchModel model, String fullname, SketchPainter painter )
  {
    mParent   = parent;
    mModel    = model;
    mFullName = fullname;
    mPainter  = painter;
  }

// -------------------------------------------------------------------
  @Override
  protected Integer doInBackground( String... statuses )
  {
    if ( ! lock() ) return null;
    int ret = 0;

    String filename;
    if ( false ) {
      filename = TDPath.getTh3FileWithExt( mFullName );
      // mAllSymbols = 
      mModel.loadTh3( filename, null /* missingSymbols */, mPainter );
    } else {
      filename = TDPath.getTdr3FileWithExt( mFullName );
      mModel.loadTdr3( filename, null /* missingSymbols */, mPainter );
    }

    return ret;
  }

  @Override
  protected void onProgressUpdate( Integer... values)
  {
    super.onProgressUpdate( values );
  }

  @Override
  protected void onPostExecute( Integer res )
  {
    if ( res != null ) {
      int r = res.intValue();
      mParent.handleSketchLoaderResult( r );
    }
    unlock();
  }

  private synchronized boolean lock()
  {
    if ( running != null ) return false;
    running = this;
    return true;
  }

  private synchronized void unlock()
  {
    if ( running == this ) running = null;
  }

}
