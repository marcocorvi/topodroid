/* @file CalibExport.java
 *
 * @author marco corvi
 * @date dec 2021 - from TDExport
 *
 * @grief topodroid calib import export
 * --------------------------------------------------------
 *  copyright this software is distributed under gpl-3.0 or later
 *  see the file copying.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

import com.topodroid.TDX.DeviceHelper;
import com.topodroid.TDX.TDPath;
import com.topodroid.utils.TDUtil;
// import com.topodroid.calib.CalibInfo;
import com.topodroid.utils.TDFile;
import com.topodroid.utils.TDLog;
import com.topodroid.utils.TDVersion;
import com.topodroid.math.TDVector;
import com.topodroid.math.TDMatrix;

import java.io.File; // PRIVATE FILE
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.FileReader;
import java.io.BufferedReader;
// import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Locale;
import java.util.List;

public class CalibExport
{
  /** export the calibration data and coefficients
   * @param cid    calibration id
   * @param data   database helper
   * @param ci     calibration info
   * @param calib_name   calibration name
   * @return ???
   *
   * the calibration coeff are exported on 4+4 lines:
   * the first four lines are: BG, AG.x, AG.y, AG.z
   * The next four lines are for M.
   *
   * Recall that the transformation is ( see TDMatrix::timesV )
   *    gs[x] = BG[x] + AG.x * gr
   *    gs[y] = BG[y] + AG.y * gr
   *    gs[z] = BG[z] + AG.z * gr
   */
  public static String exportCalibAsCsv( long cid, DeviceHelper data, CalibInfo ci, String calib_name )
  {
    // TDLog.v("calib export: id " + ci.getId() + " cid " + cid );
    boolean two_sensors = ( ci.sensors == 2 ); // TWO_SENSORS
    try {
      // TDLog.Log( TDLog.LOG_IO, "export calibration " + name );
      // TDPath.checkPath( filename );
      // BufferedWriter bw = TDFile.getMSwriter( "ccsv", calib_name + ".csv", "text/csv" );
      BufferedWriter bw = new BufferedWriter( new FileWriter( TDPath.getCcsvFile( calib_name + ".csv" ) ) );
      PrintWriter pw = new PrintWriter( bw );

      pw.format("# %s created by TopoDroid v %s\n\n", TDUtil.getDateString("yyyy.MM.dd"), TDVersion.string() );

      pw.format("# %s\n", ci.name );
      pw.format("# %s\n", ci.date );
      pw.format("# %s\n", ci.device );
      pw.format("# %s\n", ci.comment );
      pw.format("# %d\n", ci.algo );
      pw.format("# %d\n", ci.sensors );

      List< CBlock > list = data.selectAllGMs( cid, 1, true ); // status 1: all shots, true: negative_grp too
      // TDLog.v("calib export: data " + list.size() );
      for ( CBlock b : list ) {
        b.computeBearingAndClino();
        pw.format(Locale.US, "%d, %d, %d, %d, %d, %d, %d, %d, %.2f, %.2f, %.2f, %.4f, %d\n",
          b.mId, b.gx, b.gy, b.gz, b.mx, b.my, b.mz, b.mGroup, b.mBearing, b.mClino, b.mRoll, b.mError, b.mStatus );
        if ( two_sensors ) { // TWO_SENSORS add a second line for the second sensor set
          pw.format(Locale.US, "%d, %d, %d, %d, %d, %d\n", b.gx2, b.gy2, b.gz2, b.mx2, b.my2, b.mz2 );
        }
      }
  
      CalibResult res = new CalibResult();
      data.selectCalibResult( cid, res );
      pw.format(Locale.US, "# error %.2f stddev %.2f max %.2f delta %.2f iter %d\n", res.error, res.stddev, res.max_error, res.delta_bh, res.iterations );
      // FIXME TWO_SENOSRS errors are not recorded separatedly
      // if ( two_sensors ) {
      //   data.selectCalibError( cid, res, true );
      //   pw.format(Locale.US, "# error %.2f stddev %.2f max %.2f delta %.2f iter %d\n", res.error, res.stddev, res.max_error, res.delta_bh, res.iterations );
      // }

      String coeff_str = data.selectCalibCoeff( cid );
      if ( coeff_str != null ) {
        byte[] coeff1 = CalibAlgo.stringToCoeff( coeff_str, 1 ); // 1: first set
        TDLog.v("coeff size " + coeff1.length );
        TDMatrix mG = new TDMatrix();
        TDMatrix mM = new TDMatrix();
        TDVector vG = new TDVector();
        TDVector vM = new TDVector();
        TDVector nL = new TDVector();
        CalibAlgo.coeffToG( coeff1, vG, mG );
        CalibAlgo.coeffToM( coeff1, vM, mM );
        CalibAlgo.coeffToNL( coeff1, nL );
        pw.format(Locale.US, "# GB %.4f %.4f %.4f\n", vG.x,   vG.y,   vG.z );
        pw.format(Locale.US, "# GA %.4f %.4f %.4f\n", mG.x.x, mG.x.y, mG.x.z );
        pw.format(Locale.US, "#    %.4f %.4f %.4f\n", mG.y.x, mG.y.y, mG.y.z );
        pw.format(Locale.US, "#    %.4f %.4f %.4f\n", mG.z.x, mG.z.y, mG.z.z );
        pw.format(Locale.US, "# MB %.4f %.4f %.4f\n", vM.x,   vM.y,   vM.z );
        pw.format(Locale.US, "# MA %.4f %.4f %.4f\n", mM.x.x, mM.x.y, mM.x.z );
        pw.format(Locale.US, "#    %.4f %.4f %.4f\n", mM.y.x, mM.y.y, mM.y.z );
        pw.format(Locale.US, "#    %.4f %.4f %.4f\n", mM.z.x, mM.z.y, mM.z.z );
        pw.format(Locale.US, "# NL %.4f %.4f %.4f\n", nL.x,   nL.y,   nL.z );
        if ( two_sensors ) { // TWO_SENSORS
          // byte[] coeff2 = CalibAlgo.stringToCoeff( coeff_str, 2 ); // 2: second set
          byte[] coeff2 = new byte[ CalibTransform.COEFF_DIM ];
          System.arraycopy( coeff1, CalibTransform.COEFF_DIM, coeff2, 0, CalibTransform.COEFF_DIM );
          if ( coeff2 != null ) {
            CalibAlgo.coeffToG( coeff2, vG, mG );
            CalibAlgo.coeffToM( coeff2, vM, mM );
            CalibAlgo.coeffToNL( coeff2, nL );
            pw.format(Locale.US, "# GB %.4f %.4f %.4f\n", vG.x,   vG.y,   vG.z );
            pw.format(Locale.US, "# GA %.4f %.4f %.4f\n", mG.x.x, mG.x.y, mG.x.z );
            pw.format(Locale.US, "#    %.4f %.4f %.4f\n", mG.y.x, mG.y.y, mG.y.z );
            pw.format(Locale.US, "#    %.4f %.4f %.4f\n", mG.z.x, mG.z.y, mG.z.z );
            pw.format(Locale.US, "# MB %.4f %.4f %.4f\n", vM.x,   vM.y,   vM.z );
            pw.format(Locale.US, "# MA %.4f %.4f %.4f\n", mM.x.x, mM.x.y, mM.x.z );
            pw.format(Locale.US, "#    %.4f %.4f %.4f\n", mM.y.x, mM.y.y, mM.y.z );
            pw.format(Locale.US, "#    %.4f %.4f %.4f\n", mM.z.x, mM.z.y, mM.z.z );
            pw.format(Locale.US, "# NL %.4f %.4f %.4f\n", nL.x,   nL.y,   nL.z );
          }
        }
      }
      bw.flush();
      bw.close();
      return ci.name;
    } catch ( IOException e ) {
      TDLog.e( "Failed CSV export: " + e.getMessage() );
      return null;
    }
  }

  static private String nextLineAtPos( BufferedReader br, int pos ) throws IOException
  {
    String line = br.readLine();
    if ( line == null ) return "";
    if ( line.length() <= pos ) return "";
    return line.substring( pos );
  }

  /** import calibration from a file
   * @param data database
   * @param file calibration file (private storage)
   * @param device_name  name of the device - for consistency check
   * @return error-code (0: success)
   *
   * Calib file format
   * line-1 must contain string "TopoDroid"
   * line-2 skipped
   * line-3 contains name at pos 2: must not match any calib name already in the db
   * line-4 contains date at pos 2 format yyyy.mm.dd
   * line-5 contained device MAC at pos 2: must match current device
   * line-6 contains comment starting at pos 2
   * line-7 contains algo at pos 2: 0 unset, 1 linear, 2 non-linear
   * next data lines follow, each with at least 8 entries:
   *   id, gx, gy, gz, mx, my, mz, group
   * data reading ends at end-of-file or at a line with fewer entries
   */
  // static int importCalibFromCsv( DeviceHelper data, String filename, String device_name )
  static public  int importCalibFromCsv( DeviceHelper data, File file, String device_name ) // PRIVATE FILE
  {
    int version = 0; // TopoDroid version of the CSV file
    int sensors = 1; // default one sensor set
    boolean two_sensors = false;
    int ret = 0;
    try {
      // TDPath.checkPath( filename );
      // FileReader fr = TDFile.getFileReader( filename );
      // TDLog.Log( TDLog.LOG_IO, "import calibration file " + file.getPath() );
      FileReader fr = TDFile.getFileReader( file );
      BufferedReader br = new BufferedReader( fr );
    
      String line = br.readLine();
      if ( line == null || ! line.contains("TopoDroid") ) {
        ret = -1; // NOT TOPODROID CSV
      } else {
        line.trim();
        int pos = line.indexOf("TopoDroid v" );
        String v_str = line.substring( pos+12 );
        String[] vals = v_str.split("\\.");
        // TDLog.v("<" + line + "> pos " + pos + " " + v_str + " " + vals.length );
        int v1 = Integer.parseInt( vals[0] );
        int v2 = Integer.parseInt( vals[1] );
        int v3 = Integer.parseInt( vals[2] );
        version = v1 * 100000 + v2 * 1000 + v3;
        TDLog.v("CSV version " + version );
 
        br.readLine(); // skip empty line
        String name = nextLineAtPos( br, 2 );
        if ( data.hasCalibName( name ) ) {
          ret = -2; // CALIB NAME ALREADY EXISTS
        } else {
          String date = nextLineAtPos( br, 2 );
          if ( date == null || date.length() < 10 ) {
            date = TDUtil.currentDate();
          }
          String device = nextLineAtPos( br, 2 );
          if ( ! device.equals( device_name ) ) {
            ret = -3; // DEVICE MISMATCH
          } else {
            String comment = nextLineAtPos( br, 2 );
            long algo = 0L;
            line = br.readLine();
            if ( line != null && line.charAt(0) == '#' ) {
              try {
                algo = Long.parseLong( line.substring(2) );
              } catch ( NumberFormatException e ) { TDLog.v("Error " + e.getMessage() ); }
              line = br.readLine();
              if ( version > 602078 && line != null && line.charAt(0) == '#' ) { // TWO_SENSORS
                try {
                  sensors = Integer.parseInt( line.substring(2) );
                  two_sensors = ( sensors == 2 );
                } catch ( NumberFormatException e ) { TDLog.v("Error " + e.getMessage() ); }
                line = br.readLine();
              }

                
            }
            if ( line == null ) {
              ret = -4;
            } else {
              long cid = data.insertCalibInfo( name, date, device, comment, algo, sensors ); // TWO_SENSORS
              while ( line != null ) {
                if ( ! line.startsWith("#") ) {
                  // FIXME
                  //   (1) replace ' '* with nothing
                  //   (2) split on ','
                  line = line.replaceAll( " ", "" );
                  vals = line.split(",");
                  if ( vals.length > 7 ) {
                    // TDLog.v("Calib " + vals.length + " <" + vals[1] + "><" + vals[2] + "><" + vals[3] + ">" );
                    try {
                      long gx = Long.parseLong( vals[1] );
                      long gy = Long.parseLong( vals[2] );
                      long gz = Long.parseLong( vals[3] );
                      long mx = Long.parseLong( vals[4] );
                      long my = Long.parseLong( vals[5] );
                      long mz = Long.parseLong( vals[6] );
                      long gid = data.insertGM( cid, gx, gy, gz, mx, my, mz );
                      String grp = vals[7].trim();
                      data.updateGMName( gid, cid, grp );  // this sets the "group"
                      if ( vals.length > 12 ) { // status can be only 0:normal or 1:delete
                        if ( Integer.parseInt( vals[12] ) == 1 ) {
                          data.deleteGM( cid, gid, true ); // this sets the "status" flag
                        }
                      }
                      if ( two_sensors ) {
                        line = br.readLine();
                        line = line.replaceAll( " ", "" );
                        vals = line.split(",");
                        gx = Long.parseLong( vals[1] );
                        gy = Long.parseLong( vals[2] );
                        gz = Long.parseLong( vals[3] );
                        mx = Long.parseLong( vals[4] );
                        my = Long.parseLong( vals[5] );
                        mz = Long.parseLong( vals[6] );
                        data.updateGMsecond( gid, cid, gx, gy, gz, mx, my, mz );
                      }
                    } catch ( NumberFormatException e ) { 
                      TDLog.e( e.getMessage() );
                    }
                  } else {
                    // ERROR
                  }
                }
                line = br.readLine();
              }
            }
          }
        }
      }
      fr.close();
    } catch ( IOException e ) {
      TDLog.e( "Failed calib CSV import: " + e.getMessage() );
      ret = -5; // IO Exception
    }
    return ret;
  }

}
