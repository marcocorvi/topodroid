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
 */
package com.topodroid.DistoX;

import java.io.File;
import java.io.IOException;

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

import android.media.ExifInterface;

import android.util.Log;

public class DrawingLineSectionDialog extends MyDialog
                               implements View.OnClickListener
{
  private DrawingLinePath mLine;
  private DrawingWindow mParent;
  private TopoDroidApp    mApp;

  // private TextView mTVtype;
  private TextView mTVoptions;
  // private CheckBox mReversed;
  String mId;
  PlotInfo mPlotInfo;
  String  mFrom;
  String  mTo;
  String  mNick;
  float   mAzimuth;
  float   mClino;
  private int mOrientation = 0;

  private Button   mBtnFoto;
  private Button   mBtnDraw;
  private Button   mBtnErase;
  // private Button   mBtnCancel;
  private EditText mETnick;
  private ImageView mIVimage;   // photo image
  boolean mHSection;
  boolean mExists;
  String  mFilename;
  private boolean hasPhoto;
  
  public DrawingLineSectionDialog( Context context,
                                   DrawingWindow parent, TopoDroidApp app, boolean h_section, boolean exists, String id,
                                   DrawingLinePath line, String from, String to, float azimuth, float clino )
  {
    super( context, R.string.DrawingLineSectionDialog );
    mParent = parent;
    mApp  = app;
    mExists = exists;      // whether the section exists or it is being created
    mHSection = h_section; // if the line has "-id" the h_section is taken from the PlotInfo
    mLine = line;
    mFrom = from;
    mTo   = to;
    mNick = null;
    mAzimuth = azimuth;
    mClino = clino;
    mFilename = null;
    hasPhoto = FeatureChecker.checkCamera( context );

    // read section id from the line options
    mId = mLine.getOption( "-id" );
    if ( mId == null ) {  // line does not have ID yet 
      mId = id;
      // mLine.addOption( "-id " + mId );
      mPlotInfo = null;
      // Log.v( "DistoX", "Drawing Line Section Dialog ID was null: set to " + mId );
    } else {
      // Log.v( "DistoX", "Drawing Line Section Dialog ID: " + mId );
      mPlotInfo = TopoDroidApp.mData.getPlotInfo( mParent.getSID(), mId );
      if ( mPlotInfo != null ) { // extra careful
        mFrom     = mPlotInfo.start;
        mTo       = mPlotInfo.view;
        mNick     = mPlotInfo.nick;
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
              BrushManager.mLineLib.getSymbolThName( mLine.mLineType ) ) + " " + mFrom + " " + mTo );

    mTVoptions = (TextView) findViewById( R.id.line_options );
    mTVoptions.setText( "ID " + mId );

    TextView tv_azimuth = (TextView) findViewById( R.id.line_azimuth );
    TextView tv_date    = (TextView) findViewById( R.id.line_date );

    mETnick = (EditText) findViewById( R.id.line_nick );
    if ( mNick != null && mNick.length() > 0 ) {
      mETnick.setText( mNick );
    }

    // mReversed = (CheckBox) findViewById( R.id.line_reversed );
    // mReversed.setChecked( mLine.mReversed );

    mIVimage = (ImageView) findViewById( R.id.line_image );
    mBtnFoto = (Button) findViewById( R.id.button_foto );
    if ( hasPhoto ) {
      mBtnFoto.setOnClickListener( this );
    } else {
      mBtnFoto.setVisibility( View.GONE );
    }
    mBtnDraw = (Button) findViewById( R.id.button_draw );
    mBtnDraw.setOnClickListener( this );
    if ( mPlotInfo != null ) { // check the photo
      mFilename = TDPath.getSurveyJpgFile( mApp.mySurvey, mPlotInfo.name );
      File imagefile = new File( mFilename );
      if ( imagefile.exists() ) {
        try {
          ExifInterface exif = new ExifInterface( mFilename );
          mOrientation = exif.getAttributeInt( ExifInterface.TAG_ORIENTATION, 0 );
          float bearing = 0;
          float clino = 0;
          // mAzimuth = exif.getAttribute( "GPSImgDirection" );
          String b = exif.getAttribute( ExifInterface.TAG_GPS_LONGITUDE );
          if ( b != null ) {
            int k = b.indexOf('/');
            if ( k > 0 ) bearing = Integer.parseInt( b.substring(0,k) ) / 100.0f;
          }
          String c = exif.getAttribute( ExifInterface.TAG_GPS_LATITUDE );
          if ( c != null ) {
            int k = c.indexOf('/');
            if ( k > 0 ) clino = Integer.parseInt( c.substring(0,k) ) / 100.0f;
          }
          // Log.v("DistoX", "Long <" + bearing + "> Lat <" + clino + ">" );
          tv_azimuth.setText(
            String.format( mContext.getResources().getString( R.string.photo_azimuth_clino ), bearing, clino ) );
          String date = exif.getAttribute( ExifInterface.TAG_DATETIME );
          tv_date.setText( (date != null)? date : "" );
        } catch ( IOException e ) {
          // should not happen
        }

        BitmapFactory.Options bfo = new BitmapFactory.Options();
        bfo.inJustDecodeBounds = true;
        BitmapFactory.decodeFile( mFilename, bfo );
        int required_size = TDSetting.mThumbSize;
        int scale = 1;
        while ( bfo.outWidth/scale/2 > required_size || bfo.outHeight/scale/2 > required_size ) {
          scale *= 2;
        }
        bfo.inJustDecodeBounds = false;
        bfo.inSampleSize = scale;
        Bitmap image = BitmapFactory.decodeFile( mFilename, bfo );
        if ( image != null ) {
          int w2 = image.getWidth() / 8;
          int h2 = image.getHeight() / 8;
          Bitmap image2 = Bitmap.createScaledBitmap( image, w2, h2, true );
          if ( image2 != null ) {
            MyBearingAndClino.applyOrientation( mIVimage, image2, mOrientation );
            // mIVimage.setHeight( h2 );
            // mIVimage.setWidth( w2 );
            mIVimage.setOnClickListener( this );
          } else {
            mIVimage.setVisibility( View.GONE );
          }
        }

        // mBtnFoto.setBackgroundResource( R.drawable.ic_camera_no );
      } else {
        tv_azimuth.setVisibility( View.GONE );
        tv_date.setVisibility( View.GONE );
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
    // TDLog.Log( TDLog.LOG_INPUT, "Drawing Line Section Dialog onClick() " + b.getText().toString() );
    long type = mHSection ? PlotInfo.PLOT_H_SECTION : PlotInfo.PLOT_SECTION;
    mNick = ( mETnick.getText() != null )? mETnick.getText().toString() : "";

    switch ( v.getId() ) {
      case R.id.button_foto:
        mParent.makeSectionPhoto( mLine, mId, type, mFrom, mTo, mNick, mAzimuth, mClino );
        break;
      case R.id.button_draw:
        mParent.makeSectionDraw( mLine, mId, type, mFrom, mTo, mNick, mAzimuth, mClino );
        break;
      case R.id.button_erase:
        mParent.deleteLine( mLine );
        break;
      case R.id.line_image:
        mApp.viewPhoto( mContext, mFilename );
        break;
      default: // R.id.button_cancel
        /* nothing */
    }
    dismiss();
  }

  @Override
  public void onBackPressed()
  {
    if ( ! mExists ) {
      // if pressed BACK and the section did not exist, tell the parent to delete the "section" line
      mParent.deleteLine( mLine );
    }
    dismiss();
  }

}

