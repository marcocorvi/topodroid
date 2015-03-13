/** @file GMGroupsDialog.java
 */
package com.topodroid.DistoX;

import android.app.Dialog;
import android.os.Bundle;
import android.content.Context;

import android.widget.TextView;
import android.widget.CheckBox;
import android.widget.Button;

import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;

class GMGroupsDialog extends Dialog
                     implements OnClickListener
{
  private Context mContext;
  private GMActivity mParent;

  private CheckBox mCBreset;
  private Button mBtnOK;

  private String mPolicy;

  GMGroupsDialog( Context context, GMActivity parent, String policy )
  {
    super( context );
    mContext = context;
    mParent  = parent;
    mPolicy  = policy;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    setContentView(R.layout.gm_groups_dialog );
    getWindow().setLayout( LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT );

    setTitle( mContext.getResources().getString( R.string.group_title ) );
    
    mBtnOK = (Button) findViewById( R.id.group_ok );
    mBtnOK.setOnClickListener( this );
    
    TextView policy = (TextView) findViewById( R.id.group_policy );
    policy.setText( mPolicy );

    mCBreset = (CheckBox) findViewById( R.id.group_reset );
    mCBreset.setChecked( false );
  }
    
  @Override
  public void onClick( View v ) 
  {
    Button b = (Button)v;
    if ( b == mBtnOK ) {
      if ( mCBreset.isChecked() ) {
        mParent.resetGroups();
      } else {
        mParent.computeGroups();
      }
    }
    dismiss();
  }
}
