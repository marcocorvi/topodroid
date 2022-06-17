/* @file SetupDoneDialog.java
 *
 * @author marco corvi
 * @date jun 2018
 *
 * @brief TopoDroid final setup dialog: done
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.ui.MyDialog;

import android.content.Context;
// import android.content.res.Resources;

import android.os.Bundle;
import android.view.View;
// import android.widget.LinearLayout;
// import android.view.ViewGroup;
// import android.view.Display;
// import android.util.DisplayMetrics;
// import android.widget.TextView;
import android.widget.Button;
// import java.util.Locale;

class SetupDoneDialog extends MyDialog
                      implements View.OnClickListener
{
  private final MainWindow mParent;
  private int   mSetup; // my setup index

  private Button  mBtnDone;


  SetupDoneDialog( Context context, MainWindow parent, int setup )
  {
    super( context, null, R.string.SetupDoneDialog ); // null app
    mParent = parent;
    mSetup  = setup;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.setup_done_dialog, R.string.setup_done_title );
    mBtnDone   = (Button) findViewById( R.id.btn_done );
    mBtnDone.setOnClickListener( this );
  }

// ----------------------------------------------------------------------------

   @Override
   public void onClick(View view)
   {
     Button b = (Button)view;
     if ( b == mBtnDone ) {
       mParent.doNextSetup( -1 );
     }
     dismiss();
   }

   @Override
   public void onBackPressed()
   {
     mParent.doNextSetup( mSetup + 1 );
     dismiss();
   }

}
