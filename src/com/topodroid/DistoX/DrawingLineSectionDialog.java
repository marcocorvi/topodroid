/* @file DrawingLineSectionDialog.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid sketch line section dialog 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.DistoX;

// import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDFile;
import com.topodroid.ui.MyCheckBox;
import com.topodroid.ui.MyDialog;
import com.topodroid.ui.TDLayout;
import com.topodroid.ui.TDImage;
import com.topodroid.prefs.TDSetting;
import com.topodroid.common.PlotType;

// import java.io.File; // JPEG FILE
// import java.io.IOException;

import android.os.Bundle;
import android.content.Context;

import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.ImageView;
import android.view.View;

class DrawingLineSectionDialog extends MyDialog
                               implements View.OnClickListener
{
  private DrawingLinePath mLine;
  private DrawingWindow mParent;
  // private TopoDroidApp    mApp; // unused
  private String mFilename = null;

  // private TextView mTVtype;
  private TextView mTVoptions;
  // private CheckBox mReversed;
  private String mId;
  private PlotInfo mPlotInfo;
  private String  mFrom;
  private String  mTo;
  private String  mNick;
  private float   mAzimuth;
  private float   mClino;

  private MyCheckBox mBtnFoto  = null;
  private MyCheckBox mBtnDraw  = null;
  private MyCheckBox mBtnErase = null;
  private MyCheckBox mBtnSave  = null;

  private Button   mBtnCancel;
  private EditText mETnick;
  private ImageView mIVimage;   // photo image
  private boolean mHSection;
  private boolean mExists;
  private boolean hasPhoto;
  private float mTT; // intersection abscissa

  private TDImage mTdImage = null;

  
  DrawingLineSectionDialog( Context context,
                            DrawingWindow parent, // TopoDroidApp app, 
                            boolean h_section, boolean exists, String id,
                            DrawingLinePath line, String from, String to, float azimuth, float clino, float tt0 )
  {
    super( context, R.string.DrawingLineSectionDialog );
    mParent = parent;
    // mApp  = app;
    mExists = exists;      // whether the section exists or it is being created
    mHSection = h_section; // if the line has "-id" the h_section is taken from the PlotInfo
    mLine = line;
    mFrom = from;
    mTo   = to;
    mNick = null;
    mAzimuth = azimuth;
    mClino = clino;
    mTT = tt0;
    hasPhoto = TDandroid.checkCamera( context );

    // read section id from the line options
    mId = mLine.getOption( "-id" );
    if ( mId == null ) {  // line does not have ID yet 
      mId = id;
      // mLine.addOption( "-id " + mId );
      mPlotInfo = null;
      // TDLog.v( "Drawing Line Section Dialog ID was null: set to " + mId );
    } else {
      // TDLog.v( "Drawing Line Section Dialog ID: " + mId );
      mPlotInfo = TopoDroidApp.mData.getPlotInfo( TDInstance.sid, mId );
      if ( mPlotInfo != null ) { // extra careful
        mFrom     = mPlotInfo.start;
        mTo       = mPlotInfo.view;
        mNick     = mPlotInfo.nick;
        mAzimuth  = mPlotInfo.azimuth;
        mClino    = mPlotInfo.clino;
        mHSection = (mPlotInfo.type == PlotType.PLOT_H_SECTION);
      }
    }
    // TDLog.v( "line id " + mId );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    // cannot use initLayout
    if ( mFrom != null && mTo != null ) {
      initLayout( R.layout.drawing_line_section_dialog,
        String.format( mParent.getResources().getString( R.string.title_draw_line ), mLine.getThName( ) ) + " " + mFrom + " " + mTo );
    } else {
      initLayout( R.layout.drawing_line_section_dialog, 
        String.format( mParent.getResources().getString( R.string.title_draw_line_no_stations ), mLine.getThName( ) ) );
    }

    mTVoptions = (TextView) findViewById( R.id.line_options );
    mTVoptions.setText( String.format( mContext.getResources().getString( R.string.fmt_id ), mId ) );

    TextView tv_azimuth = (TextView) findViewById( R.id.line_azimuth );
    TextView tv_date    = (TextView) findViewById( R.id.line_date );

    mETnick = (EditText) findViewById( R.id.line_nick );
    if ( mNick != null && mNick.length() > 0 ) {
      mETnick.setText( mNick );
    }

    // mReversed = (CheckBox) findViewById( R.id.line_reversed );
    // mReversed.setChecked( mLine.mReversed );

    int size = TDSetting.mSizeButtons; // TopoDroidApp.getScaledSize( mContext );
    LinearLayout button_list = (LinearLayout)findViewById( R.id.button_list );
    button_list.setMinimumHeight( size + 20 );

    mIVimage = (ImageView) findViewById( R.id.line_image );
    // mBtnFoto = (Button) findViewById( R.id.button_foto );
    if ( hasPhoto ) {
      mBtnFoto = new MyCheckBox( mContext, size, R.drawable.iz_camera, R.drawable.iz_camera ); 
      button_list.addView( mBtnFoto );
      TDLayout.setMargins( mBtnFoto, 0, -10, 40, 10 );
      mBtnFoto.setOnClickListener( this );
    // } else {
    //   /* mBtnFoto.setVisibility( View.GONE ); */
    }

    // mBtnDraw = (Button) findViewById( R.id.button_draw );
    mBtnDraw = new MyCheckBox( mContext, size, R.drawable.iz_plot, R.drawable.iz_plot );
    button_list.addView( mBtnDraw );
    TDLayout.setMargins( mBtnDraw, 0, -10, 40, 10 );
    mBtnDraw.setOnClickListener( this );

    if ( mPlotInfo != null ) { // check the photo
      String subdir = TDInstance.survey + "/photo"; // "photo/" + TDInstance.survey;
      String filename = mPlotInfo.name + ".jpg";
      mFilename = TDPath.getSurveyJpgFile( TDInstance.survey, mPlotInfo.name );
      // File imagefile = TDFile.getTopoDroidFile( mFilename ); // JPEG FILE
      if ( TDFile.hasMSfile( subdir, filename ) ) { // if ( imagefile.exists() )
	mTdImage = new TDImage( mFilename );
        tv_azimuth.setText( String.format( mContext.getResources().getString( R.string.photo_azimuth_clino ), mTdImage.azimuth(), mTdImage.clino() ) );
        String date = mTdImage.date();
        tv_date.setText( (date != null)? date : "" );

	if ( mTdImage.fillImageView( mIVimage, mTdImage.width()/8, mTdImage.height()/8, true ) ) {
          mIVimage.setOnClickListener( this );
        } else {
          mIVimage.setVisibility( View.GONE );
        }
        // mBtnFoto.setBackgroundResource( R.drawable.ic_camera_no );
      } else {
        tv_azimuth.setVisibility( View.GONE );
        tv_date.setVisibility( View.GONE );
      }
    }

    // mBtnErase = (Button) findViewById( R.id.button_erase );
    mBtnErase = new MyCheckBox( mContext, size, R.drawable.iz_delete, R.drawable.iz_delete );
    button_list.addView( mBtnErase );
    TDLayout.setMargins( mBtnErase, 0, -10, 40, 10 );
    mBtnErase.setOnClickListener( this );
    // if ( mExists ) mBtnErase.setTextColor( 0xffff0000 );

    // mBtnSave = (Button) findViewById( R.id.button_save );
    if ( mPlotInfo != null ) {
      mBtnSave = new MyCheckBox( mContext, size, R.drawable.iz_save, R.drawable.iz_save );
      button_list.addView( mBtnSave );
      TDLayout.setMargins( mBtnSave, 0, -10, 40, 10 );
      mBtnSave.setOnClickListener( this );
    }
    mBtnCancel = (Button) findViewById( R.id.button_cancel );
    mBtnCancel.setOnClickListener( this );

  }

  public void onClick(View v) 
  {
    // TDLog.Log( TDLog.LOG_INPUT, "Drawing Line Section Dialog onClick() " + b.getText().toString() );

    if ( v.getId() == R.id.line_image ) {
      if ( mTdImage != null ) {
        (new PhotoDialog( mContext, mFilename )).show();
      }
      return;
    } else if ( v.getId() == R.id.button_cancel ) {
      onBackPressed();
      return;
    } else {
      long type = mHSection ? PlotType.PLOT_H_SECTION : PlotType.PLOT_SECTION;
      mNick = ( mETnick.getText() != null )? mETnick.getText().toString() : "";
      MyCheckBox cb = (MyCheckBox)v;
      if ( cb == mBtnFoto ) {
        mParent.makePhotoXSection( mLine, mId, type, mFrom, mTo, mNick, mAzimuth, mClino );
      } else if ( cb == mBtnDraw ) {
        mParent.makePlotXSection( mLine, mId, type, mFrom, mTo, mNick, mAzimuth, mClino, mTT );
      } else if ( cb == mBtnErase ) {
        mParent.deleteLine( mLine );
      } else if ( cb == mBtnSave ) {
	if ( mPlotInfo == null ) return;
        mParent.updatePlotNick( mPlotInfo, mNick );
      }
    }
    recycleImage();
    dismiss();
  }

  private void recycleImage()
  {
    if ( mTdImage != null ) mTdImage.recycleImages();
    mTdImage = null;
  }

  @Override
  public void onBackPressed()
  {    
    if ( ! mExists ) {
      // if pressed BACK and the section did not exist, tell the parent to delete the "section" line
      mParent.deleteLine( mLine );
    }
    recycleImage();
    dismiss();
  }

}

