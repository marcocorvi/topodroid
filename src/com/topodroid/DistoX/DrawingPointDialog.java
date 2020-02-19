/* @file DrawingPointDialog.java
 *
 * @author marco corvi
 * @date june 2012
 *
 * @brief TopoDroid sketch point attributes editing dialog
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
import android.widget.RadioButton;
// import android.widget.RadioGroup;
// import android.widget.ImageView;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.view.View;
// import android.view.ViewGroup.LayoutParams;

// import android.graphics.Paint;

// import android.util.Log;

class DrawingPointDialog extends MyDialog
                         implements View.OnClickListener
{
  private final DrawingPointPath mPoint;
  private final DrawingWindow  mParent;
  private final boolean mOrientable;
  private final int mPointType;

  // private TextView mTVtype;
  private EditText mEToptions;
  private EditText mETtext;
  private RadioButton mBtnScaleXS;
  private RadioButton mBtnScaleS;
  private RadioButton mBtnScaleM;
  private RadioButton mBtnScaleL;
  private RadioButton mBtnScaleXL;

  private CheckBox mCBxsection; // to display xsection outline
  private Button   mBTdraw;
  private boolean  mHasXSectionOutline;
  private boolean  mDoOptions;

  private CheckBox mCBbase  = null;
  private CheckBox mCBfloor = null;
  private CheckBox mCBfill  = null;
  private CheckBox mCBceil  = null;
  private CheckBox mCBarti  = null;
  // private CheckBox mCBform  = null;
  // private CheckBox mCBwater = null;
  // private CheckBox mCBtext  = null;

  private MyOrientationWidget mOrientationWidget;
 
  private Button   mBtnOk;
  private Button   mBtnCancel;

  private String mXSectionName; // full section name = scrap-name

  DrawingPointDialog( Context context, DrawingWindow parent, DrawingPointPath point )
  {
    super( context, R.string.DrawingPointDialog );
    mParent = parent;
    mPoint  = point;
    mPointType = mPoint.mPointType;
    mOrientable = BrushManager.isPointOrientable( mPointType );
    mXSectionName = null;
    mHasXSectionOutline = false;
    mDoOptions = BrushManager.pointHasText( mPointType ) || TDLevel.overAdvanced;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    initLayout(R.layout.drawing_point_dialog, 
      "POINT " + BrushManager.getPointName( mPoint.mPointType ) );

    // mTVtype = (TextView) findViewById( R.id.point_type );
    mEToptions = (EditText) findViewById( R.id.point_options );
    if ( mDoOptions ) {
      if ( mPoint.mOptions != null ) mEToptions.setText( mPoint.mOptions );
    } else {
      mEToptions.setVisibility( View.GONE );
    }
    mETtext    = (EditText) findViewById( R.id.point_text );

    mCBxsection = (CheckBox) findViewById( R.id.point_xsection );
    mBTdraw     = (Button) findViewById( R.id.button_draw );
    
    if ( BrushManager.pointHasTextOrValue( mPoint.mPointType ) ) {
      String text = mPoint.getPointText();
      mETtext.setText( (text == null)? "" : text );
    } else {
      mETtext.setEnabled( false );
    }

    mOrientationWidget = new MyOrientationWidget( this, mOrientable, mPoint.mOrientation );


    if ( BrushManager.isPointSection( mPoint.mPointType ) ) {
      // FIXME SECTION_RENAME
      // scrap option contains only section nickname (no survey prefix)
      mXSectionName = mPoint.getOption("-scrap"); 

      // String[] vals = mPoint.mOptions.split(" ");
      // for ( int k = 0; k < vals.length; ++k ) {
      //   if ( vals[k].equals("-scrap") ) {
      //     for ( ++k; k < vals.length; ++k ) {
      //       if ( vals[k].length() > 0 ) break;
      //     }
      //     if ( k < vals.length ) mXSectionName = vals[k];
      //   }
      // }
      if ( mXSectionName != null ) {
	// FIXME SECTION_RENAME
	// mXSectionName = mApp.mSurvey + "-" + mXSectionName;
        mHasXSectionOutline = mParent.hasXSectionOutline( mXSectionName );
	if ( TDLevel.overAdvanced ) {
          mCBxsection.setChecked( mHasXSectionOutline );
	} else {
          mCBxsection.setVisibility( View.GONE );
	}
        mBTdraw.setOnClickListener( this );
      }
    } else {
      mCBxsection.setChecked( false );
      mCBxsection.setVisibility( View.GONE );
      mBTdraw.setVisibility( View.GONE );
    }

    mBtnScaleXS = (RadioButton) findViewById( R.id.point_scale_xs );
    mBtnScaleS  = (RadioButton) findViewById( R.id.point_scale_s  );
    mBtnScaleM  = (RadioButton) findViewById( R.id.point_scale_m  );
    mBtnScaleL  = (RadioButton) findViewById( R.id.point_scale_l  );
    mBtnScaleXL = (RadioButton) findViewById( R.id.point_scale_xl );
    switch ( mPoint.getScale() ) {
      case DrawingPointPath.SCALE_XS: mBtnScaleXS.setChecked( true ); break;
      case DrawingPointPath.SCALE_S:  mBtnScaleS.setChecked( true ); break;
      case DrawingPointPath.SCALE_M:  mBtnScaleM.setChecked( true ); break;
      case DrawingPointPath.SCALE_L:  mBtnScaleL.setChecked( true ); break;
      case DrawingPointPath.SCALE_XL: mBtnScaleXL.setChecked( true ); break;
    }

    if ( TDSetting.mWithLevels > 1 ) {
      setCBlayers();
    } else {
      LinearLayout ll = (LinearLayout) findViewById( R.id.layer_layout );
      ll.setVisibility( View.GONE );
    }

    mBtnOk = (Button) findViewById( R.id.button_ok );
    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnOk.setOnClickListener( this );
    mBtnCancel.setOnClickListener( this );
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
    int level = mPoint.mLevel;
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
    mPoint.mLevel = level;
  }

  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingPointDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOk ) {
      if ( mDoOptions && mEToptions.getText() != null ) {
        mPoint.mOptions = mEToptions.getText().toString().trim();
      }
      if ( mBtnScaleXS.isChecked() )      mPoint.setScale( DrawingPointPath.SCALE_XS );
      else if ( mBtnScaleS.isChecked() )  mPoint.setScale( DrawingPointPath.SCALE_S  );
      else if ( mBtnScaleM.isChecked() )  mPoint.setScale( DrawingPointPath.SCALE_M  );
      else if ( mBtnScaleL.isChecked() )  mPoint.setScale( DrawingPointPath.SCALE_L  );
      else if ( mBtnScaleXL.isChecked() ) mPoint.setScale( DrawingPointPath.SCALE_XL );

      if ( mXSectionName != null ) {
        if ( TDLevel.overAdvanced && mHasXSectionOutline != mCBxsection.isChecked() ) {
          mParent.setXSectionOutline( mXSectionName, mCBxsection.isChecked(), mPoint.cx, mPoint.cy );
        }
      }

      if ( mOrientable ) {
        mPoint.setOrientation( mOrientationWidget.mOrient );
        // Log.v("DistoX", "Point type " + mPoint.mPointType + " orientation " + mPoint.mOrientation );
      }
      if ( BrushManager.pointHasTextOrValue( mPoint.mPointType ) ) {
        mPoint.setPointText( mETtext.getText().toString().trim() );
      }

      if ( TDSetting.mWithLevels > 1 ) setLevel();

    } else if ( b == mBTdraw ) {
      mParent.openSectionDraw( mXSectionName );
    // } else if ( b == mBtnCancel ) {
      // nothing
    }
    dismiss();
  }

}

