/* @file TopoDroidAlertDialog.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid alert dialog
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.TDX;

import android.content.DialogInterface;
import android.app.AlertDialog;
// import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Resources;

// import android.widget.TextView;

public class TopoDroidAlertDialog 
{
  /** make alert dialog with OK / CANCEL buttons
   * @param context   context
   * @param res       resources
   * @param message   display message code
   * @param pos       OK callback
   */  
  public static void makeAlert( Context context, Resources res, int message, DialogInterface.OnClickListener pos )
  {
    makeAlert( context, res, res.getString(message), R.string.button_cancel, R.string.button_ok, null, pos );
  }

  /** make alert dialog with OK / CANCEL buttons
   * @param context   context
   * @param res       resources
   * @param message   display message string
   * @param pos       OK callback
   */  
  public static void makeAlert( Context context, Resources res, String message, DialogInterface.OnClickListener pos )
  {
    makeAlert( context, res, message, R.string.button_cancel, R.string.button_ok, null, pos );
  }

  /** make alert dialog with only OK button
   * @param context   context
   * @param res       resources
   * @param message   display message code
   */  
  public static void makeAlert( Context context, Resources res, int message )
  {
    makeAlert( context, res, res.getString(message), -1, R.string.button_ok, null, null );
  }

    // AlertDialog.Builder alert_builder = new AlertDialog.Builder( context );
    // alert_builder.setMessage( message );
    // alert_builder.setPositiveButton( res.getString( R.string.button_cancel ), 
    //     new DialogInterface.OnClickListener() {
    //       @Override
    //       public void onClick( DialogInterface dialog, int btn ) { }
    //     }
    // );
    // alert_builder.setNegativeButton( res.getString( R.string.button_ok ), pos );
    // AlertDialog alert = alert_builder.create();
    // // alert.getWindow().setBackgroundDrawableResource( R.color.alert_background );
    // alert.getWindow().setBackgroundDrawableResource( R.drawable.alert_bg );
    // alert.show();

  /** make alert dialog with OK / CANCEL buttons
   * @param context    context
   * @param res        resources
   * @param message    display message string
   * @param ok         OK string (null to hide)
   * @param no         CANCEL string (null to hide)
   * @param ok_handler OK callback (null to default to nothing)
   * @param no_handler CANCEL callback (null to default to nothing)
   */  
  public static void makeAlert( Context context, Resources res, String message, 
             String ok, 
             String no,
             DialogInterface.OnClickListener ok_handler,
             DialogInterface.OnClickListener no_handler )
  {
      AlertDialog.Builder alert_builder = new AlertDialog.Builder( context );
      alert_builder.setMessage( message );
      if ( ok != null ) {
        if ( ok_handler == null ) {
          ok_handler = new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { }
          };
        } 
        alert_builder.setNegativeButton( ok, ok_handler );
      }

      if ( no != null ) {
        if ( no_handler == null ) {
          no_handler = new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { }
          };
        } 
        alert_builder.setPositiveButton( no, no_handler );
      }

      AlertDialog alert = alert_builder.create();
      
      // NEEDED API-11 for custom background color
      if ( TDandroid.ABOVE_API_24 ) {
        alert.getWindow().setBackgroundDrawableResource( R.drawable.alert_bg );
      } else {
        alert.getWindow().setBackgroundDrawableResource( R.color.alert_background );
      }

      alert.show();
  }

  /** make alert dialog with OK / CANCEL buttons
   * @param context    context
   * @param res        resources
   * @param message    display message string
   * @param ok         OK string code (negative to hide)
   * @param no         CANCEL string code (negative to hide)
   * @param ok_handler OK callback (null to default to nothing)
   * @param no_handler CANCEL callback (null to default to nothing)
   */  
  public static void makeAlert( Context context, Resources res, String message, 
             int ok, 
             int no,
             DialogInterface.OnClickListener ok_handler,
             DialogInterface.OnClickListener no_handler )
  {
      AlertDialog.Builder alert_builder = new AlertDialog.Builder( context );
      alert_builder.setMessage( message );
      if ( ok >= 0 ) {
        if ( ok_handler == null ) {
          ok_handler = new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { }
          };
        } 
        alert_builder.setNegativeButton( res.getString(ok), ok_handler );
      }

      if ( no >= 0 ) {
        if ( no_handler == null ) {
          no_handler = new DialogInterface.OnClickListener() {
            @Override public void onClick( DialogInterface dialog, int btn ) { }
          };
        } 
        alert_builder.setPositiveButton( res.getString(no), no_handler );
      }

      AlertDialog alert = alert_builder.create();
      // NEEDED API-11 for custom background color
      if ( TDandroid.ABOVE_API_24 ) {
        alert.getWindow().setBackgroundDrawableResource( R.drawable.alert_bg );
      } else {
        alert.getWindow().setBackgroundDrawableResource( R.color.alert_background );
      }
      alert.show();
  }

}
