/* @file SketchLoader.java
 *
 * @author marco corvi
 * @date may 2017
 *
 * @brief TopoDroid 3d-sketch loader
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.lang.ref.WeakReference;

// import android.widget.Toast;
import android.os.AsyncTask;


class SketchLoader extends AsyncTask< String, Integer, Integer >
{
  private final WeakReference<SketchWindow>  mParent; // FIXME LEAK context
  private final SketchModel   mModel;
  private final String        mFullName;
  private final SketchPainter mPainter;
  private static SketchLoader running = null;

  SketchLoader( SketchWindow parent, SketchModel model, String fullname, SketchPainter painter )
  {
    mParent   = new WeakReference<SketchWindow>( parent );
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
    // if ( false ) {
    //   filename = TDPath.getTh3FileWithExt( mFullName );
    //   // mAllSymbols = 
    //   mModel.loadTh3( filename, null /* missingSymbols */, mPainter );
    // } else {
      filename = TDPath.getTdr3FileWithExt( mFullName );
      mModel.loadTdr3( filename, null /* missingSymbols */, mPainter );
    // }

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
      if ( mParent.get() != null && ! mParent.get().isFinishing() )
        mParent.get().handleSketchLoaderResult( r );
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
