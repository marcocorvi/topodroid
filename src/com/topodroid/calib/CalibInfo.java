/* @file CalibInfo.java
 *
 * @author marco corvi
 * @date may 2012
 *
 * @brief TopoDroid calib info (name, date, comment etc)
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.calib;

// import com.topodroid.utils.TDLog;

public class CalibInfo
{
  public final static int ALGO_AUTO       = 0;
  public final static int ALGO_LINEAR     = 1;
  public final static int ALGO_NON_LINEAR = 2;
  // final static int ALGO_MINIMUM    = 3;

  final private long id;          //!< database ID
  final public String name;       //!< name
  final public String date;       //!< date
  final public String device;     //!< device address
  final public String comment;    //!< comment
  final public int    algo;       //!< calibration algo
  final public float  dip;        //!< magnetic dip [deg]

  public CalibInfo( Long _id, String _name, String _date, String _device, String _comment, int _algo, float _dip ) 
  {
    id = _id;
    name = _name;
    date = _date;
    device = _device;
    comment = _comment;
    algo = _algo;
    dip  = _dip;
  }

  // void debug()
  // {
  //   // TDLog.v("CALIB " + id + " " + name + " " + date + " " + device ); // this is DEBUG
  // }
}
