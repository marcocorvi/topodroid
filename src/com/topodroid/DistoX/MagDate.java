/* @file MagDate.java
 *
 * @author marco corvi
 * @date nov 2011
 *
 * @brief TopoDroid World Magnetic Model 
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 * Implemented after GeomagneticLibrary.c by
 *  National Geophysical Data Center
 *  NOAA EGC/2
 *  325 Broadway
 *  Boulder, CO 80303 USA
 *  Attn: Susan McLean
 *  Phone:  (303) 497-6478
 *  Email:  Susan.McLean@noaa.gov
 */
package com.topodroid.DistoX;

// MAGtype_Date;
class MagDate
{
    static private int MonthDays[] = { 0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
    private int Year;
    private int Month;
    private int Day;
    double DecimalYear; /* decimal years */

  MagDate( int y, int m, int d )
  {
    Year = y;
    Month = m;
    Day = d;
    DecimalYear = toDecimalYear( y, m, d );
  }

  MagDate( double dy )
  {
    DecimalYear = dy;
    Year = (int)(dy);
    double f = dy - Year; // fraction
    // int ExtraDay = 0;
    int yds = 365;
    if ((Year % 4 == 0 && Year % 100 != 0) || Year % 400 == 0) {
      MonthDays[2] = 29;
      // ExtraDay = 1;
      yds = 366;
    } else {
      MonthDays[2] = 28;
    }
    Day = (int)(yds * f);
    Month = 1;
    while ( Day > MonthDays[Month]) {
    	Day -= MonthDays[Month];
    	Month ++;
    }
  }

  // MAG_DateToYear
  private double toDecimalYear( int year, int month, int day )
  {
    double yds = 365.0;
    if ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0) {
      MonthDays[2] = 29;
      yds = 366.0;
    } else {
      MonthDays[2] = 28;
    }
    
    int temp = 0; /*Total number of days */
    for ( int i = 1; i <= month; i++) temp += MonthDays[i - 1];
    temp += day;
    return ( year + (temp - 1) / yds );
  }
}
