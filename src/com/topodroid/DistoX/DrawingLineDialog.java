/* @file DrawingLineDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid sketch line attributes editing dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

// import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
// import android.widget.RadioButton;
// import android.widget.RadioGroup;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.LinearLayout;
import android.widget.AdapterView;
// import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;

import android.view.View;

class DrawingLineDialog extends MyDialog
                        implements View.OnClickListener
                        , AdapterView.OnItemSelectedListener
{
  private DrawingLinePath mLine;
  private DrawingWindow mParent;
  private LinePoint mPoint;
  private int mType;
  private int mTypeSection;

  private Spinner mETtype;
  private EditText mEToptions;
 
  private CheckBox mBtnOutlineOut;
  private CheckBox mBtnOutlineIn;
  // private RadioButton mBtnOutlineNone;

  private Button mBtnOk;
  // private Button mBtnCancel;

  private MyCheckBox mReversed;
  private MyCheckBox mBtnSharp;  // sharp reduce and rock are mutully exclusive
  private MyStateBox mBtnReduce;
  private MyCheckBox mBtnRock = null;
  private MyCheckBox mBtnClose;

  private CheckBox mCBbase  = null;
  private CheckBox mCBfloor = null;
  private CheckBox mCBfill  = null;
  private CheckBox mCBceil  = null;
  private CheckBox mCBarti  = null;
  // private CheckBox mCBform  = null;
  // private CheckBox mCBwater = null;
  // private CheckBox mCBtext  = null;

  private boolean mDoOptions;

  DrawingLineDialog( Context context, DrawingWindow parent, DrawingLinePath line, LinePoint lp )
  {
    super( context, R.string.DrawingLineDialog );
    mParent  = parent;
    mLine  = line;
    mPoint = lp;
    mType  = mLine.mLineType;
    mTypeSection = BrushManager.mLineLib.mLineSectionIndex;
    mDoOptions = TDLevel.overAdvanced;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);

    String title = String.format( mParent.getResources().getString( R.string.title_draw_line ), BrushManager.getLineName( mLine.mLineType ) );
    initLayout( R.layout.drawing_line_dialog, title );

    mETtype = (Spinner) findViewById( R.id.line_type );
    // mETtype.setText( BrushManager.getLineThName( mLine.mLineType ) );

    // FIXME TODO simplify using getSymbolNamesNoSection()
    try {
      ArrayAdapter adapter = new ArrayAdapter<>( mContext, R.layout.menu, BrushManager.mLineLib.getSymbolNames() );
      String section = BrushManager.getLineName( mTypeSection );
      if ( section != null ) adapter.remove( section );
      mETtype.setAdapter( adapter );
      mETtype.setSelection( ( mType < mTypeSection )? mType : mType-1 );
      mETtype.setOnItemSelectedListener( this );
    } catch ( UnsupportedOperationException e ) {
      TDLog.Error( e.getMessage() );
    }

    mEToptions = (EditText) findViewById( R.id.line_options );
    if ( mDoOptions ) {
      String options = mLine.getOptionString();
      if ( options != null ) mEToptions.setText( options );
    } else {
      mEToptions.setVisibility( View.GONE );
    }

    mBtnOutlineOut  = (CheckBox) findViewById( R.id.line_outline_out );
    mBtnOutlineIn   = (CheckBox) findViewById( R.id.line_outline_in );
    // mBtnOutlineNone = (RadioButton) findViewById( R.id.line_outline_none );

    if ( mLine.mOutline == DrawingLinePath.OUTLINE_OUT ) {
      mBtnOutlineOut.setChecked( true );
    } else if ( mLine.mOutline == DrawingLinePath.OUTLINE_IN ) {
      mBtnOutlineIn.setChecked( true );
    // } else if ( mLine.mOutline == DrawingLinePath.OUTLINE_NONE ) {
    //   mBtnOutlineNone.setChecked( true );
    }

    // mReversed = (CheckBox) findViewById( R.id.line_reversed );

    mBtnOutlineOut.setOnClickListener( this );
    mBtnOutlineIn.setOnClickListener( this );

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnOk.setOnClickListener( this );
    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );
    ( (Button) findViewById( R.id.button_cancel ) ).setOnClickListener( this );

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    mReversed  = new MyCheckBox( mContext, size, R.drawable.iz_reverse_ok, R.drawable.iz_reverse_no );
    mBtnSharp  = new MyCheckBox( mContext, size, R.drawable.iz_sharp_ok, R.drawable.iz_sharp_no );
    // mBtnReduce = new MyCheckBox( mContext, size, R.drawable.iz_reduce_ok,  R.drawable.iz_reduce_no  );
    mBtnReduce = new MyStateBox( mContext, R.drawable.iz_reduce_no,  R.drawable.iz_reduce_ok, R.drawable.iz_reduce_ok2 );
    mBtnClose  = new MyCheckBox( mContext, size, R.drawable.iz_close_ok, R.drawable.iz_close_no );
    mReversed.setChecked( mLine.isReversed() );
    mBtnClose.setChecked( mLine.isPathClosed() );

    LinearLayout.LayoutParams lp = TDLayout.getLayoutParams( 0, 10, 20, 10 );

    LinearLayout layout3 = (LinearLayout)findViewById( R.id.layout3 );
    layout3.addView( mReversed, lp );
    layout3.addView( mBtnSharp, lp );
    layout3.addView( mBtnReduce, lp );
    if ( TDLevel.overAdvanced && TDSetting.mLineStraight ) {
      mBtnRock = new MyCheckBox( mContext, size, R.drawable.iz_rock_ok,  R.drawable.iz_rock_no  );
      layout3.addView( mBtnRock, lp );
      mBtnRock.setOnClickListener( this );
    }
    layout3.addView( mBtnClose, lp );

    mBtnSharp.setOnClickListener( this );
    mBtnReduce.setOnClickListener( this );

    // TODO sharp reduce rock must be exclusive

    if ( TDSetting.mWithLevels > 0 ) {
      setCBlayers();
    } else {
      LinearLayout ll = (LinearLayout) findViewById( R.id.layer_layout );
      ll.setVisibility( View.GONE );
    }
  }

  private void setCBlayers()
  {
    mCBbase  = (CheckBox) findViewById( R.id.cb_layer_base  );
    mCBfloor = (CheckBox) findViewById( R.id.cb_layer_floor );
    mCBfill  = (CheckBox) findViewById( R.id.cb_layer_fill  );
    mCBceil  = (CheckBox) findViewById( R.id.cb_layer_ceil  );
    mCBarti  = (CheckBox) findViewById( R.id.cb_layer_arti  );
    // mCBform  = (CheckBox) findViewById( R.id.cb_layer_form  );
    // mCBwater = (CheckBox) findViewById( R.id.cb_layer_water );
    // mCBtext  = (CheckBox) findViewById( R.id.cb_layer_text  );
    int level = mLine.mLevel;
    mCBbase .setChecked( ( level & DrawingLevel.LEVEL_BASE  ) == DrawingLevel.LEVEL_BASE  );
    mCBfloor.setChecked( ( level & DrawingLevel.LEVEL_FLOOR ) == DrawingLevel.LEVEL_FLOOR );
    mCBfill .setChecked( ( level & DrawingLevel.LEVEL_FILL  ) == DrawingLevel.LEVEL_FILL  );
    mCBceil .setChecked( ( level & DrawingLevel.LEVEL_CEIL  ) == DrawingLevel.LEVEL_CEIL  );
    mCBarti .setChecked( ( level & DrawingLevel.LEVEL_ARTI  ) == DrawingLevel.LEVEL_ARTI  );
    // mCBform .setChecked( ( level & DrawingLevel.LEVEL_FORM  ) == DrawingLevel.LEVEL_FORM  );
    // mCBwater.setChecked( ( level & DrawingLevel.LEVEL_WATER ) == DrawingLevel.LEVEL_WATER );
    // mCBtext .setChecked( ( level & DrawingLevel.LEVEL_TEXT  ) == DrawingLevel.LEVEL_TEXT  );
  }

  private void setLevel()
  {
    int level = 0;
    if ( mCBbase .isChecked() ) level |= DrawingLevel.LEVEL_BASE;
    if ( mCBfloor.isChecked() ) level |= DrawingLevel.LEVEL_FLOOR;
    if ( mCBfill .isChecked() ) level |= DrawingLevel.LEVEL_FILL;
    if ( mCBceil .isChecked() ) level |= DrawingLevel.LEVEL_CEIL;
    if ( mCBarti .isChecked() ) level |= DrawingLevel.LEVEL_ARTI;
    // if ( mCBform .isChecked() ) level |= DrawingLevel.LEVEL_FORM;
    // if ( mCBwater.isChecked() ) level |= DrawingLevel.LEVEL_WATER;
    // if ( mCBtext .isChecked() ) level |= DrawingLevel.LEVEL_TEXT;
    mLine.mLevel = level;
  }

  @Override
  public void onItemSelected( AdapterView av, View v, int pos, long id ) 
  { 
    mType = ( pos >= mTypeSection )? pos+1 : pos;
    // av.setSelection( pos );
  }

  @Override
  public void onNothingSelected( AdapterView av ) { mType = mLine.mLineType; }

  @Override
  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingLineDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOutlineIn ) {
      mBtnOutlineOut.setChecked( false );
      return;
    } else if ( b == mBtnOutlineOut ) {
      mBtnOutlineIn.setChecked( false );
      return;
    } else if ( b == mBtnSharp ) {
      if ( mBtnSharp.toggleState() ) {
	mBtnReduce.setState( 0 );  // false
        if ( mBtnRock != null ) mBtnRock.setState( false );
      }
      return;
    } else if ( b == mBtnReduce ) {
      int reduce = (mBtnReduce.getState() + 1) % 3;
      mBtnReduce.setState( reduce );
      // if ( mBtnReduce.toggleState() )
      if ( reduce > 0 ) {
	mBtnSharp.setState( false );
        if ( mBtnRock != null ) mBtnRock.setState( false );
      }
      return;
    } else if ( mBtnRock != null && b == mBtnRock ) {
      if ( mBtnRock.toggleState() ) {
	mBtnReduce.setState( 0 );
        mBtnSharp.setState( false );
      }
      return;
    
    } else if ( b == mBtnOk ) {
      if ( mType != mLine.mLineType && mType != mTypeSection ) mLine.setLineType( mType );

      if ( mDoOptions && mEToptions.getText() != null ) {
        String options = mEToptions.getText().toString().trim();
        if ( options.length() > 0 ) mLine.setOptions( options );
      }
      if ( mBtnOutlineOut.isChecked() ) mLine.mOutline = DrawingLinePath.OUTLINE_OUT;
      else if ( mBtnOutlineIn.isChecked() ) mLine.mOutline = DrawingLinePath.OUTLINE_IN;
      else /* if ( mBtnOutlineNone.isChecked() ) */ mLine.mOutline = DrawingLinePath.OUTLINE_NONE;

      mLine.setReversed( mReversed.isChecked() );

      int reduce = mBtnReduce.getState();
      if ( mBtnSharp.isChecked() ) {
        mParent.sharpenLine( mLine );
      } else if ( reduce > 0 ) {
	mParent.reduceLine( mLine, reduce );
      } else if ( mBtnRock != null && mBtnRock.isChecked() ) {
        mParent.rockLine( mLine );
      }

      if ( mBtnClose.isChecked() ) {
        mParent.closeLine( mLine );
        mLine.setClosed( true );
      } else {
        mLine.setClosed( false );
      }

      if ( TDSetting.mWithLevels  > 0 ) setLevel();
 
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

}

