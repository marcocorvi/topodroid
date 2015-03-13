/** @file DrawingLineSectionDialog.java
 *
 * @author marco corvi
 * @date jan 2014
 *
 * @brief TopoDroid sketch line section dialog 
 * --------------------------------------------------------
 *  Copyright This sowftare is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * CHANGES
 * 20140328 dropped reversed
 */
package com.topodroid.DistoX;

import java.io.StringWriter;
import java.io.PrintWriter;
import java.io.File;

import android.app.Dialog;
import android.os.Bundle;

import android.content.Context;

import android.widget.TextView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.CheckBox;
import android.view.View;

import android.widget.ImageView;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.util.Log;

public class DrawingLineSectionDialog extends Dialog
                               implements View.OnClickListener
{
  private DrawingLinePath mLine;
  private DrawingActivity mParent;
  private TopoDroidApp    mApp;

  // private TextView mTVtype;
  private TextView mTVoptions;
  // private CheckBox mReversed;
  String mId;
  PlotInfo mPlotInfo;
  String  mFrom;
  String  mTo;
  float   mAzimuth;
  float   mClino;

  private Button   mBtnFoto;
  private Button   mBtnDraw;
  private Button   mBtnErase;
  // private Button   mBtnCancel;
  private ImageView mIVimage;   // photo image
  boolean mHSection;
  boolean mExists;

  public DrawingLineSectionDialog( DrawingActivity context, TopoDroidApp app, boolean h_section, boolean exists,
                                   DrawingLinePath line, String from, String to, float azimuth, float clino )
  {
    super( context );
    mParent = context;
    mApp  = app;
    mExists = exists; // whether the section exists or it is being created
    mHSection = h_section;
    mLine = line;
    mFrom = from;
    mTo   = to;
    mAzimuth = azimuth;
    mClino = clino;

    // read section id from the line options
    mId   = null;
    String[] vals = mLine.getOptions();
    for (int k = 0; k<vals.length - 1; ++k ) {
      if ( vals[k].equals( "-id" ) ) {
        mId = vals[k+1];
        break;
      }
    } 
    if ( mId == null ) {
      mId = TopoDroidApp.mData.getNextSectionId( mParent.getSID() );
      String option = "-id " + mId;
      mLine.addOption( option );
      mPlotInfo = null;
      // Log.v( "DistoX", "DrawingLineSectionDialog ID was null: set to " + mId );
    } else {
      // Log.v( "DistoX", "DrawingLineSectionDialog ID: " + mId );
      mPlotInfo = TopoDroidApp.mData.getPlotInfo( mParent.getSID(), mId );
      if ( mPlotInfo != null ) { // extra careful
        mFrom     = mPlotInfo.start;
        mTo       = mPlotInfo.view;
        mAzimuth  = mPlotInfo.azimuth;
        mClino    = mPlotInfo.clino;
        mHSection = (mPlotInfo.type == PlotInfo.PLOT_H_SECTION);
      }
    }
    // Log.v( TopoDroidApp.TAG, "line id " + mId );
  }

// -------------------------------------------------------------------
  @Override
  protected void onCreate(Bundle savedInstanceState) 
  {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.drawing_line_section_dialog);

    setTitle( String.format( mParent.getResources().getString( R.string.title_draw_line ),
              DrawingBrushPaths.getLineThName( mLine.mLineType ) ) + " " + mFrom + " " + mTo );

    mTVoptions = (TextView) findViewById( R.id.line_options );
    mTVoptions.setText( "ID " + mId );

    mIVimage      = (ImageView) findViewById( R.id.line_image );

    // mReversed = (CheckBox) findViewById( R.id.line_reversed );
    // mReversed.setChecked( mLine.mReversed );

    mBtnFoto = (Button) findViewById( R.id.button_foto );
    mBtnDraw = (Button) findViewById( R.id.button_draw );

    mBtnDraw.setOnClickListener( this );
    mBtnFoto.setOnClickListener( this );
    if ( mPlotInfo != null ) { // check the photo
      String filename = TopoDroidPath.getSurveyJpgFile( mApp.mySurvey, mPlotInfo.name );
      File imagefile = new File( filename );
      if ( imagefile.exists() ) {
        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inJustDecodeBounds = true;
        BitmapFactory.decodeFile( filename, bfo );
        int required_size = TopoDroidSetting.mThumbSize;
        int scale = 1;
        while ( bfo.outWidth/scale/2 > required_size || bfo.outHeight/scale/2 > required_size ) {
          scale *= 2;
        }
        bfo.inJustDecodeBounds = false;
        bfo.inSampleSize = scale;
        Bitmap image = BitmapFactory.decodeFile( filename, bfo );
        if ( image != null ) {
          int w2 = image.getWidth() / 8;
          int h2 = image.getHeight() / 8;
          Bitmap image2 = Bitmap.createScaledBitmap( image, w2, h2, true );
          mIVimage.setImageBitmap( image2 );
          // mIVimage.setHeight( h2 );
          // mIVimage.setWidth( w2 );
        }
        // mBtnFoto.setBackgroundResource( R.drawable.ic_camera_no );
      }
    }

    mBtnErase = (Button) findViewById( R.id.button_erase );
    mBtnErase.setOnClickListener( this );
    if ( mExists ) mBtnErase.setTextColor( 0xffff0000 );

    // mBtnCancel = (Button) findViewById( R.id.button_cancel );
    // mBtnCancel.setOnClickListener( this );
  }

  public void onClick(View v) 
  {
    Button b = (Button)v;
    // TopoDroidLog.Log( TopoDroidLog.LOG_INPUT, "DrawingLineSectionDialog onClick() " + b.getText().toString() );
    long type = mHSection ? PlotInfo.PLOT_H_SECTION : PlotInfo.PLOT_SECTION;

    if ( b == mBtnFoto ) {
      mParent.makeSectionPhoto( mLine, mId, type, mFrom, mTo, mAzimuth, mClino );
    } else if ( b == mBtnDraw ) {
      mParent.makeSectionDraw( mLine, mId, type, mFrom, mTo, mAzimuth, mClino );
    } else if ( b == mBtnErase ) {
      mParent.deleteLine( mLine, mId );
    // } else if ( b == mBtnCancel ) {
    //   /* nothing */
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    if ( ! mExists ) {
      mParent.deleteLine( mLine, mId );
    }
    dismiss();
  }

}

