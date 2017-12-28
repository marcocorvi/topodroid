/** @file DrawingAreaDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid sketch line attributes editing dialog
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

public class DrawingAreaDialog extends MyDialog
                               implements View.OnClickListener, AdapterView.OnItemSelectedListener
{
  private DrawingAreaPath mArea;
  private DrawingWindow mParent;
  private boolean mOrientable;

  private CheckBox mCBvisible;
  private Spinner mETtype;
  private int mType;

  private OrientationWidget mOrientationWidget; 

  private Button mBtnOk;
  private Button mBtnCancel;

  public DrawingAreaDialog( Context context, DrawingWindow parent, DrawingAreaPath line )
  {
    super( context, R.string.DrawingAreaDialog );
    mParent = parent;
    mArea = line;
    mType  = mArea.mAreaType;
    mOrientable = BrushManager.mAreaLib.isSymbolOrientable( mType );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    String title = String.format( mParent.getResources().getString( R.string.title_draw_area ),
                                  BrushManager.mAreaLib.getSymbolName( mArea.mAreaType ) );
    initLayout( R.layout.drawing_area_dialog, title );

    mOrientationWidget = new OrientationWidget( this, mOrientable, mArea.mOrientation );

    mETtype = (Spinner) findViewById( R.id.area_type );
    ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, BrushManager.mAreaLib.getSymbolNames() );
    mETtype.setAdapter( adapter );
    mETtype.setSelection( mType );
    mETtype.setOnItemSelectedListener( this );


    mCBvisible = (CheckBox) findViewById( R.id.area_visible );
    mCBvisible.setChecked( mArea.isVisible() );

    // NOTE area do not have options

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnOk.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );
  }

  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) { mType = pos; }

  @Override
  public void onNothingSelected( AdapterView av ) { mType = mArea.mAreaType; }


  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingAreaDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOk ) {
      if ( mType != mArea.mAreaType ) mArea.setAreaType( mType );

      mArea.setVisible( mCBvisible.isChecked() );
      if ( mOrientable ) {
        mArea.setOrientation( mOrientationWidget.mOrient );
      }
    } else if ( b == mBtnCancel ) {
      // nothing
    }
    dismiss();
  }

}

