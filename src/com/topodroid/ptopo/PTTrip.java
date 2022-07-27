/* @file PTTrip.java
 *
 * @author marco corvi
 * @date march 2010
 *
 * @brief PocketTopo file IO
 * --------------------------------------------------------
 *  Copyright This software is distributed under GPL-3.0 or later
 *  See the file COPYING.
 * --------------------------------------------------------
 */
package com.topodroid.ptopo;

import java.io.InputStream;
import java.io.OutputStream;


public class PTTrip
{
    static final long SEC_PER_DAY  =  86400;   /* 60*60*24 */
    static final long SEC_PER_YEAR =(SEC_PER_DAY*365);
    static final long SEC_PER_LEAP =(SEC_PER_DAY*366);
    static final long SEC_PER_31   =(SEC_PER_DAY*31);
    static final long SEC_PER_30   =(SEC_PER_DAY*30);
    static final long SEC_PER_29   =(SEC_PER_DAY*29);
    static final long SEC_PER_28   =(SEC_PER_DAY*28);
    static final long DAY_OFFSET   =  25567;   /* 70*365 + 17 */
    static final long SEC_OFFSET = 2208988800L;  /* 86400 * (70*365 + 17) */
    static final long NANO2SEC   = 10000000L;   /* 100 ns to 1 s */

    private long _time; //!< ticks [100 ns] since 1.01.01 00:00
    public  int _day;
    public  int _month;
    public  int _year;
    private short _declination; //!< full circle 2^16
    private PTString _comment;

    PTTrip()
    {
      _time = SEC_OFFSET;
      setDate();
      _declination = (short)0;
      _comment = new PTString();
    }

    // -------------------------------------------


    public String comment() { return _comment.value(); }

    void setComment( String str ) { _comment.set( str ); }

    public boolean hasComment() { return _comment.size() > 0; }

    public float declination()
    { 
      float ret = (float)(_declination) * PTFile.INT16_2_DEG;
      if ( ret > 180.0f ) ret -= 360.0f;
      return ret;
    }

    void setDeclination( float d ) 
    {
      // assert( d >= -180.0 && d < 180.0 );
      if ( d < 0.0f ) d += 360.0f;
      _declination = (short)( d * PTFile.DEG_2_INT16 );
    }
      
    // -----------------------------------------------

    private void setDate( )
    {
      long days = (_time/NANO2SEC) / SEC_PER_DAY;
      int y = 1;
      boolean leap = ( (y % 4) == 0 && ( (y % 100) != 0 || (y % 400) == 0 ) );
      while ( days > (leap ? 366L : 365L ) ) {
        days -= (leap)? 366L : 365L;
        ++ y;
        leap = ( (y % 4) == 0 && ( (y % 100) != 0 || (y % 400) == 0 ) );
      }
      // assert( days <= 365U );
      long m28 = (leap)? 29L : 28L;
      int m = 1;
      if ( days > 31L ) {
        ++m; days -= 31L;
        if ( days > m28 ) {
          ++m; days -= m28;
          if ( days > 31L ) {
            ++m; days -= 31L;
            if ( days > 30L ) {
              ++m; days -= 30L;
              if ( days > 31L ) {
                ++m; days -= 31L;
                if ( days > 30L ) {
                  ++m; days -= 30L;
                  if ( days > 31L ) {
                    ++m; days -= 31L;
                    if ( days > 31L ) {
                      ++m; days -= 31L;
                      if ( days > 30L ) {
                        ++m; days -= 30L;
                        if ( days > 31L ) {
                          ++m; days -= 31L;
                          if ( days > 30L ) {
                            ++m; days -= 30L;
      } } } } } } } } } } }
      // assert( days <= 31U );
      _day = (int)days;
      _month = m;
      _year = y;
    }
    
    void setTime( int y, int m, int d )
    {
      _day = d;
      _month = m;
      _year = y;
      long days = d;
      boolean leap = ( (y % 4) == 0 && ( (y % 100) != 0 || (y % 400) == 0 ) );
      while ( y > 1 ) {
        if ( leap ) ++days;
        days += 365L;
        -- y;
        leap = ( (y % 4) == 0 && ( (y % 100) != 0 || (y % 400) == 0 ) );
      }
      if (m > 1 ) days += 31L;
      if (m > 2 ) days += (leap) ? 29L : 28L; 
      if ( m > 3 ) days += 31L;
      if ( m > 4 ) days += 30L;
      if ( m > 5 ) days += 31L;
      if ( m > 6 ) days += 30L;
      if ( m > 7 ) days += 31L;
      if ( m > 8 ) days += 31L;
      if ( m > 9 ) days += 30L;
      if ( m > 10 ) days += 31L;
      if ( m > 11 ) days += 30L;
      _time = days * NANO2SEC * SEC_PER_DAY;
      // printf("Trip set date %d %d %d -- %llx\n", y, m, d, _time );
    }

    void read( InputStream fs )
    {
      _time = PTFile.readLong( fs );
      setDate();
      _comment.read( fs );
      _declination = PTFile.readShort( fs );
    }

    void write( OutputStream fs )
    {
      PTFile.writeLong( fs, _time );
      _comment.write( fs );
      PTFile.writeShort( fs, _declination );
    }

    // void print( )
    // {
    //   float declination = ((float)_declination) * PTFile.INT16_2_DEG;
    //   if ( declination > 180.0f ) declination -= 360.0f;
    //   tdlog.v( "trip: date " + _year + "-" + _month + "-" + _day + " decl. " + declination );
    //   _comment.print();
    // }

/*
void 
PTtrip::printTherion( OutputStream fp )
{
  int y, m, d;
  getDate( y, m, d );
  float declination = ((float)_declination) * INT16_2_DEG;
  if ( declination > 180.0 ) declination -= 360.0;
  
  fprintf(fp, "  centerline\n");
  if ( _comment.size() > 0 ) {
    fprintf(fp, "    # %s\n", _comment.value() );
  }
  fprintf(fp, "    date %4d.%02d.%02d\n", y, m, d );
  if ( _declination != 0 ) {
    fprintf(fp, "    declination %.3f degrees\n", declination );  // Locale.US
  }
  fprintf(fp, "    data normal from to length compass clino\n");
  fprintf(fp, "    extend right\n");
}
*/

}


