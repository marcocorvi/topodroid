/* @file DanglingShotDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid dalngling shots listing dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.num.TDNum;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;

import java.util.List;
// import java.util.ArrayList;

import android.os.Bundle;
import android.content.Context;
// import android.content.res.Resources;

import android.view.View;
// import android.view.View.OnClickListener;

import android.widget.TextView;
import android.widget.Button;
import android.widget.LinearLayout;

class DanglingShotsDialog extends MyDialog
                          implements View.OnClickListener
{
  private TDNum mNum;

  // private Button mBtnBack;

  /** cstr
   * @param context   context
   * @param num       data reduction 
   */
  DanglingShotsDialog( Context context, TDNum num )
  {
    super( context, R.string.DanglingShotsDialog );
    mNum    = num;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    initLayout( R.layout.dangling_shots_dialog, R.string.title_dangling );

    // Resources res = mContext.getResources();

    LinearLayout list = (LinearLayout) findViewById( R.id.list );
    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 10, 10, 20, 20 );

    List< DBlock > blks = mNum.getUnattached();
    for ( DBlock blk : blks ) {
      TextView tv = new TextView( mContext );
      tv.setText( blk.toShortStringNormal( true ) ); // FIXME DIVING
      list.addView( tv, lp );
    }

    // mBtnBack = (Button) findViewById(R.id.btn_back);
    // mBtnBack.setOnClickListener( this );
    ( (Button) findViewById(R.id.btn_back) ).setOnClickListener( this );
  }

  /** respont to user tap - dismiss this dialog
   * @param view tapped view
   */
  @Override
  public void onClick(View view)
  {
    // Button b = (Button)view;
    // if ( b == mBtnBack ) {
    //   /* nothing */
    // }
    dismiss();
  }
}
        

