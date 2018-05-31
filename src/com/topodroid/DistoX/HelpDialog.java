/** @file HelpDialog.java
 *
 * @author marco corvi
 * @date nov 2013
 *
 * @brief TopoDroid help dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import java.util.ArrayList;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

import android.widget.Button;
// import android.widget.TextView;
import android.widget.ListView;

// import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemClickListener;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

// import android.util.Log;

class HelpDialog extends MyDialog
                 implements OnClickListener
                          , OnLongClickListener
{
  private ListView    mList;
  private HelpAdapter mAdapter;

  private int mIcons[];
  private int mMenus[];
  private int mIconTexts[];
  private int mMenuTexts[];
  private int mNr0;
  private int mNr1;
  private String mPage;

  private Button mBtnManual;

  // TODO list of help entries
  HelpDialog( Context context, int icons[], int menus[], int texts1[], int texts2[], int n0, int n1, String page )
  {
    super( context, R.string.HelpDialog ); 
    mIcons = icons;
    mMenus = menus;
    mIconTexts = texts1;
    mMenuTexts = texts2;
    mNr0 = n0;
    mNr1 = n1; // offset of menus
    mPage = page;
    // Log.v("DistoX", "HELP buttons " + mNr0 + " menus " + mNr1 );
  }

  @Override
  public void onCreate( Bundle savedInstanceState )
  {
    super.onCreate( savedInstanceState );
    initLayout(R.layout.help_dialog, R.string.HELP );

    mBtnManual = (Button) findViewById( R.id.button_manual );
    mBtnManual.setOnClickListener( this );
    mBtnManual.setOnLongClickListener( this );

    mList = (ListView) findViewById(R.id.help_list);
    // mList.setOnItemClickListener( this );
    mList.setDividerHeight( 2 );

    // Log.v( TopoDroidApp.TAG, "HelpDialog ... createAdapters" );
    createAdapter();
    mList.setAdapter( mAdapter );
    mList.invalidate();
  }

  private void createAdapter()
  {
    // Log.v("DistoX", "HELP create adapter mNr0 " + mNr0 );
    mAdapter = new HelpAdapter( mContext, this, R.layout.item, new ArrayList<HelpEntry>() );
    // int np = mIcons.length;
    for ( int i=0; i<mNr0; ++i ) {
      mAdapter.add( new HelpEntry( mContext, mIcons[i], mIconTexts[i], false ) );
    }
    if ( mMenus != null ) {
      // int nm = mMenus.length;
      for ( int i=0; i<mNr1; ++i ) {
        mAdapter.add( new HelpEntry( mContext, mMenus[i], mMenuTexts[i], true ) );
      }
    }
  }

  @Override 
  public void onClick( View v ) 
  {
    dismiss();
    UserManualActivity.showHelpPage( mContext, mPage );
  }

  @Override 
  public boolean onLongClick( View v ) 
  {
    dismiss();
    UserManualActivity.showHelpPage( mContext, null );
    return true;
  }

}

