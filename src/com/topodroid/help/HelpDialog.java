/* @file HelpDialog.java
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
package com.topodroid.help;

import com.topodroid.ui.MyDialog;
import com.topodroid.TDX.R;

import java.util.ArrayList;

import android.os.Bundle;
import android.content.Context;

import android.widget.Button;
import android.widget.ListView;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class HelpDialog extends MyDialog
                 implements OnClickListener
                          , OnLongClickListener
{
  private ListView    mList;
  private HelpAdapter mAdapter;

  private final int[] mIcons;
  private final int[] mMenus;
  private final int[] mIconTexts;
  private final int[] mMenuTexts;
  private final int mNr0;
  private final int mNr1;
  private final String mPage;

  private Button mBtnManual;

  // TODO list of help entries
  /** cstr
   * @param context     context
   * @param icons       icons
   * @param menus       menus
   * @param texts1      icons descriptions
   * @param texts2      menus descriptions
   * @param n0          ...
   * @param n1          ...
   * @param page        man page (to be linked)
   */
  public HelpDialog( Context context, int[] icons, int[] menus, int[] texts1, int[] texts2, int n0, int n1, String page )
  {
    super( context, R.string.HelpDialog ); 
    mIcons = icons;
    mMenus = menus;
    mIconTexts = texts1;
    mMenuTexts = texts2;
    mNr0 = n0;
    mNr1 = n1; // offset of menus
    mPage = page;
    // TDLog.v( "HELP buttons " + mNr0 + " menus " + mNr1 );
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

    // TDLog.v( "HelpDialog ... createAdapters" );
    createAdapter();
    mList.setAdapter( mAdapter );
    mList.invalidate();
  }

  /** create the items (icons and menus) adapters
   */
  private void createAdapter()
  {
    // TDLog.v( "HELP create adapter mNr0 " + mNr0 );
    mAdapter = new HelpAdapter( mContext, /* this, */ R.layout.item, new ArrayList< HelpEntry >() );
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

  /** react to a user tap - only the taps on "man book" are taken
   *  open the associated man page
   * @param v  tapped view
   */
  @Override 
  public void onClick( View v ) 
  {
    dismiss();
    UserManualActivity.showHelpPage( mContext, mPage );
  }

  /** react to a user long tap - only the taps on "man book" are taken
   *  open the namual with the start page
   * @param v  tapped view
   */
  @Override 
  public boolean onLongClick( View v ) 
  {
    dismiss();
    UserManualActivity.showHelpPage( mContext, null );
    return true;
  }

}

