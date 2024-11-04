/* @file TeamDialog.java
 *
 * @author marco corvi
 * @date may 2024
 *
 * @brief TopoDroid team names dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDUtil;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.prefs.TDSetting;


// import android.app.Dialog;
import android.os.Bundle;
// import android.os.Environment;

import android.content.Context;

import android.view.View;
// import android.view.View.OnClickListener;
// import android.view.Window;
// import android.view.WindowManager;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.util.List;
import java.util.ArrayList;

class TeamDialog extends MyDialog
                  implements View.OnClickListener
{
  private Button mBtnConfirm;
  // private Button mBtnClose;
  private Button mBtnPlus;

  private final ITeamText mParent;
  private ArrayList< EditText > mNames;
  private LinearLayout mTeamLayout;

  /** cstr
   * @param ctx    context
   * @param parent parent window
   */
  TeamDialog( Context ctx, ITeamText parent, List<String> names )
  {
    super( ctx, null, R.string.TeamDialog ); // null app

    mParent  = parent;
    mNames = new ArrayList< EditText >();
    if ( names != null ) {
      for ( String name : names ) {
        EditText et = new EditText( mContext );
        et.setText( name );
        mNames.add( et );
      }
    }
    if ( mNames.size() == 0 ) {
      EditText et = new EditText( mContext );
      mNames.add( et );
    }
  }


  @Override
  protected void onCreate( Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    initLayout( R.layout.team_dialog, R.string.title_team_dialog );

    // mBtnClose = (Button) findViewById( R.id.button_cancel );
    // mBtnClose.setOnClickListener( this );

    mBtnConfirm = (Button) findViewById( R.id.button_ok );
    mBtnConfirm.setOnClickListener( this );

    mBtnPlus = (Button) findViewById( R.id.button_plus );
    mBtnPlus.setOnClickListener( this );

    mTeamLayout = (LinearLayout) findViewById( R.id.team_layout );
    for ( EditText et : mNames ) {
      mTeamLayout.addView( et );
    }
    mTeamLayout.invalidate();
  }


  /** implements user taps
   * @param v   tapped view
   */
  public void onClick(View v) 
  {
    if ( v.getId() == R.id.button_ok ) {
      StringBuilder sb = new StringBuilder();
      boolean add_sep = false;
      for ( EditText et : mNames ) {
        CharSequence text = et.getText();
        if ( text != null ) {
          String team = text.toString().trim();
          if ( ! team.isEmpty() ) {
            if ( add_sep ) sb.append("; ");
            TDLog.v("TEAM text <" + team + ">" );
            sb.append( team );
            add_sep = true;
          }
        }
      }
      TDLog.v("TEAM complete text <" + sb.toString() + ">" );
      mParent.setTeamText( sb.toString() );
      dismiss();
    } else if ( v.getId() == R.id.button_plus ) {
      EditText et = new EditText( mContext );
      mNames.add( et );
      mTeamLayout.addView( et );
      mTeamLayout.invalidate();
    }
  }

  @Override
  public void onBackPressed()
  {
    super.onBackPressed();
  }

}

