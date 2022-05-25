/* @file DrawingPointSectionDialog.java
 *
 * @author marco corvi
 * @date march 2021
 *
 * @brief TopoDroid sketch section point attributes editing dialog (extracted from DrawingPointDialog)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDString;
import com.topodroid.utils.TDUtil;
// import com.topodroid.ui.MyOrientationWidget;
import com.topodroid.ui.MyDialog;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PointScale;

import android.os.Bundle;
import android.content.Context;

// import android.widget.EditText;
import android.widget.TextView;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.view.View;

class DrawingPointSectionDialog extends MyDialog
                         implements View.OnClickListener
{
  private DrawingWindow  mParent;
  private DrawingPointPath mPoint;
  private int mPointType;
  private boolean  mHasXSectionOutline;
  private String mXSectionName; // full section name = scrap-name

  // GUI widgets
  // private TextView mTVtype;
  private TextView mEToptions;
  // private EditText mEToptions;
  private RadioButton mBtnScaleXS;
  private RadioButton mBtnScaleS;
  private RadioButton mBtnScaleM;
  private RadioButton mBtnScaleL;
  private RadioButton mBtnScaleXL;

  private CheckBox mCBxsection; // to display xsection outline
  private Button   mBTdraw;

  private CheckBox mCBbase  = null; // canvas levels
  private CheckBox mCBfloor = null;
  private CheckBox mCBfill  = null;
  private CheckBox mCBceil  = null;
  private CheckBox mCBarti  = null;
  // private CheckBox mCBform  = null;
  // private CheckBox mCBwater = null;
  // private CheckBox mCBtext  = null;

  private Button   mBtnOk;
  private Button   mBtnCancel;

  /** cstr
   * @param context context
   * @param parent  sketch window
   * @param point   section point
   */
  DrawingPointSectionDialog( Context context, DrawingWindow parent, DrawingPointPath point )
  {
    super( context, R.string.DrawingPointSectionDialog );
    mParent = parent;
    mPoint  = point;
    mPointType = mPoint.mPointType;
    mXSectionName = null;
    mHasXSectionOutline = false;
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    
    initLayout(R.layout.drawing_point_section_dialog, 
      "POINT " + BrushManager.getPointName( mPoint.mPointType ) );

    // mTVtype = (TextView) findViewById( R.id.point_type );
    mEToptions = (TextView) findViewById( R.id.point_options );
    // mEToptions = (EditText) findViewById( R.id.point_options );
    if ( mPoint.mOptions != null ) mEToptions.setText( mPoint.mOptions );

    mCBxsection = (CheckBox) findViewById( R.id.point_xsection );
    mBTdraw     = (Button) findViewById( R.id.button_draw );
    
    // if ( BrushManager.isPointSection( mPoint.mPointType ) ) { // MUST BE TRUE
      // FIXME SECTION_RENAME
      // scrap option contains only section nickname (no survey prefix)
      mXSectionName = TDUtil.replacePrefix( TDInstance.survey, mPoint.getOption(TDString.OPTION_SCRAP) ); 
      // TDLog.v("Section name " + mXSectionName );
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
    // } else {
    //   mCBxsection.setChecked( false );
    //   mCBxsection.setVisibility( View.GONE );
    //   mBTdraw.setVisibility( View.GONE );
    // }

    mBtnScaleXS = (RadioButton) findViewById( R.id.point_scale_xs );
    mBtnScaleS  = (RadioButton) findViewById( R.id.point_scale_s  );
    mBtnScaleM  = (RadioButton) findViewById( R.id.point_scale_m  );
    mBtnScaleL  = (RadioButton) findViewById( R.id.point_scale_l  );
    mBtnScaleXL = (RadioButton) findViewById( R.id.point_scale_xl );
    switch ( mPoint.getScale() ) {
      case PointScale.SCALE_XS: mBtnScaleXS.setChecked( true ); break;
      case PointScale.SCALE_S:  mBtnScaleS.setChecked( true ); break;
      case PointScale.SCALE_M:  mBtnScaleM.setChecked( true ); break;
      case PointScale.SCALE_L:  mBtnScaleL.setChecked( true ); break;
      case PointScale.SCALE_XL: mBtnScaleXL.setChecked( true ); break;
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

  /** sel the layer checkboxes
   */
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

  /** set the leyers from the checkboxes
   */
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

  /** perform user tap
   * @param v   tapped view
   */
  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TDLog.Log( TDLog.LOG_INPUT, "DrawingPointSectionDialog onClick() " + b.getText().toString() );

    if ( b == mBtnOk ) {
      if ( mBtnScaleXS.isChecked() )      mPoint.setScale( PointScale.SCALE_XS );
      else if ( mBtnScaleS.isChecked() )  mPoint.setScale( PointScale.SCALE_S  );
      else if ( mBtnScaleM.isChecked() )  mPoint.setScale( PointScale.SCALE_M  );
      else if ( mBtnScaleL.isChecked() )  mPoint.setScale( PointScale.SCALE_L  );
      else if ( mBtnScaleXL.isChecked() ) mPoint.setScale( PointScale.SCALE_XL );

      // mPoint.mOptions = mEToptions.getText().toString();

      if ( mXSectionName != null ) {
        if ( TDLevel.overAdvanced && mHasXSectionOutline != mCBxsection.isChecked() ) {
          mParent.setXSectionOutline( mXSectionName, mCBxsection.isChecked(), mPoint.cx, mPoint.cy );
        }
      }

      if ( TDSetting.mWithLevels > 1 ) setLevel();

    } else if ( b == mBTdraw ) {
      mParent.openXSectionDraw( mXSectionName );
    // } else if ( b == mBtnCancel ) {
      // nothing
    }
    dismiss();
  }

}

