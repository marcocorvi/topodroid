/* @file FirmwareDialog.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid DistoX X310 device firmware dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 *
 * this class is intsantiated only by DeviceActivity
 */
package com.topodroid.DistoX;

import android.util.Log;

import java.io.File;
// import java.io.FileInputStream;
// import java.io.DataInputStream;
// import java.io.IOException;

import android.os.Bundle;

import android.content.Context;
import android.content.res.Resources;
// import android.content.Intent;
import android.content.DialogInterface;
// import android.content.DialogInterface.OnCancelListener;
// import android.content.DialogInterface.OnDismissListener;

import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.EditText;
// import android.widget.TextView;

import android.text.method.KeyListener;

class FirmwareDialog extends MyDialog
                     implements View.OnClickListener
{
  private RadioButton mBtnDump;
  private RadioButton mBtnUpload;
  private Button mBtnOK;
  // private Button mBtnClose;

  private EditText mETfile;

  private final TopoDroidApp   mApp;
  private Resources mRes;
  private KeyListener    mETkeyListener;

  FirmwareDialog( Context context, Resources res, TopoDroidApp app )
  {
    super( context, R.string.FirmwareDialog );
    mRes     = res;
    mApp     = app;
  }

  @Override
  public void onCreate( Bundle bundle )
  {
    super.onCreate( bundle );

    initLayout( R.layout.firmware_dialog, R.string.firmware_title );

    mETfile  = (EditText) findViewById( R.id.firmware_file );

    mBtnUpload = (RadioButton) findViewById(R.id.firmware_upload );
    mBtnDump   = (RadioButton) findViewById(R.id.firmware_dump );
    mBtnOK = (Button) findViewById(R.id.firmware_ok);
    // mBtnClose = (Button) findViewById(R.id.firmware_close);

    mETkeyListener = mETfile.getKeyListener();
    mETfile.setOnClickListener( this );
    // mETfile.setEnabled( false );
    mETfile.setFocusable( false );
    mETfile.setFocusableInTouchMode( false );
    // mETfile.setClickable( true );
    mETfile.setKeyListener( null );

    mBtnUpload.setOnClickListener( this );
    mBtnDump.setOnClickListener( this );
    mBtnOK.setOnClickListener( this );
    // mBtnClose.setOnClickListener( this );
    
  }

  void setFile( String filename )
  {
    mETfile.setText( filename );
  }

  @Override
  public void onClick( View view )
  {
    switch ( view.getId() ) {
      case R.id.firmware_file:
        if ( mBtnUpload.isChecked() ) {
          (new FirmwareFileDialog( mContext, this )).show(); // select file from bin directory
        }
        break;
      case R.id.firmware_upload:
        // mETfile.setEnabled( false );
        mETfile.setFocusable( false );
        mETfile.setFocusableInTouchMode( false );
        // mETfile.setClickable( true );
        mETfile.setKeyListener( null );
        break;
      case R.id.firmware_dump:
        // mETfile.setEnabled( true );
        mETfile.setFocusable( true );
        mETfile.setFocusableInTouchMode( true );
        // mETfile.setClickable( true );
        mETfile.setKeyListener( mETkeyListener );
        break;
      case R.id.firmware_ok:
        String filename = null;
        if ( mETfile.getText() != null ) { 
          filename = mETfile.getText().toString().trim();
          if ( filename.length() == 0 ) filename = null;
        }
        if ( filename == null ) {
          TDToast.makeBad( R.string.firmware_file_missing );
          return;
        }
        if ( mBtnDump.isChecked() ) {
          TDLog.LogFile( "Firmware dump to " + filename );
          File fp = new File( TDPath.getBinFile( filename ) );
          if ( fp.exists() ) {
            TDToast.makeBad( R.string.firmware_file_exists );
            return;    
          }
          askDump( filename );
        } else if ( mBtnUpload.isChecked() ) {
          TDLog.LogFile( "Firmware upload from " + filename );
          File fp = new File( TDPath.getBinFile( filename ) );
          if ( ! fp.exists() ) {
            TDLog.Error( "inexistent upload firmware file " + filename );
            return;    
          }
          int fw = FirmwareUtils.readFirmwareFirmware( fp ); // guass firmware version
          TDLog.LogFile( "Detected Firmware version " + fw );
          // Log.v("DistoX-FW", "Detected Firmware version " + fw );
	  boolean check = (fw > 0) && FirmwareUtils.firmwareChecksum( fw, fp );
          askUpload( filename, fw, check );
        }
        break;
    }
  }

  private void askDump( final String filename )
  {
    TopoDroidAlertDialog.makeAlert( mContext, mRes, R.string.ask_dump,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          TDLog.LogFile( "Firmware dumping to file " + filename );
          int ret = mApp.dumpFirmware( filename );
          TDLog.LogFile( "Firmware dump to " + filename + " result: " + ret );
          TDToast.makeLong( String.format( mRes.getString(R.string.firmware_file_dumped), filename, ret ) );
          // finish(); 
        }
      }
    );
  }

  // @param fw   firmware version
  private void askUpload( final String filename, int fw, boolean check )
  {
    boolean compatible = (fw == 2100 || fw == 2200 || fw == 2300 || fw == 2400 || fw == 2500 || fw == 2412 || fw == 2501 || fw == 2512 );
    TDLog.LogFile( "FW/HW compatible " + compatible + " FW check " + check );
    // Log.v("DistoX-FW", "FW " + fw + " compatible " + compatible + " check " + check );

    String title = mRes.getString( (compatible && check)? R.string.ask_upload : R.string.ask_upload_not_compatible );
    TopoDroidAlertDialog.makeAlert( mContext, mRes, title,
      new DialogInterface.OnClickListener() {
        @Override
        public void onClick( DialogInterface dialog, int btn ) {
          String pathname = TDPath.getBinFile( filename );
          TDLog.LogFile( "Firmware uploading from path " + pathname );
          File file = new File( pathname ); // file must exists
          long len = file.length();
          int ret  = mApp.uploadFirmware( filename );
          TDLog.LogFile( "Firmware upload result: written " + ret + " bytes of " + len );
          TDToast.makeLong( String.format( mRes.getString(R.string.firmware_file_uploaded), filename, ret, len ) );
          // finish(); 
        }
      }
    );
  }

}
