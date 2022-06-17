/* @file TdmConfigDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid Manager dialog to enter the filename of a project
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.tdm;

import com.topodroid.ui.MyDialog;
import com.topodroid.utils.TDString;
import com.topodroid.TDX.R;

import android.os.Bundle;
// import android.app.Dialog;
// import android.app.Activity;
import android.content.Context;
// import android.content.Intent;

import android.view.View;
// import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;


public class TdmConfigDialog extends MyDialog 
                             implements View.OnClickListener
{
    private EditText mLabel;
    private Button mBtnOK;
    private Button mBtnCancel;

    private TdManagerActivity mActivity;

    public TdmConfigDialog( Context context, TdManagerActivity activity )
    {
      super(context, null, R.string.TdmConfigDialog); // null app
      mActivity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
      super.onCreate(savedInstanceState);
      initLayout( R.layout.tdconfig_dialog, R.string.title_tdconfig );

      mLabel     = (EditText) findViewById(R.id.label_text);
      mBtnOK     = (Button) findViewById(R.id.label_ok);
      mBtnCancel = (Button) findViewById(R.id.label_cancel);

      mBtnOK.setOnClickListener( this );
      mBtnCancel.setOnClickListener( this );

    }

    public void onClick(View view)
    {
      if (view.getId() == R.id.label_ok ) {
        String name = mLabel.getText().toString();
        if ( name != null && name.length() > 0 ) {
          name = TDString.spacesToUnderscore( name );
          if ( ! name.endsWith( ".tdconfig" ) ) {
            name = name + ".tdconfig";
          }
          mActivity.addTdmConfig( name );
        }
      // } else ( view.getId() == R.id.label_cancel ) {
      //   // nothing
      }
      dismiss();
    }
}
        

