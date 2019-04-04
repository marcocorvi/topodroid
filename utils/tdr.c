#include <stdio.h>
#include <stdlib.h>
#include <arpa/inet.h>

int sc = sizeof( char );
int si = sizeof( int );
int sf = sizeof( float );
uint32_t s32 = sizeof( uint32_t );

int VERSION = 0;

int toLE( int * pi )
{
  unsigned char * ch = (unsigned char*)pi;
  return (int)( ( ((int)ch[0])<<24 ) | ( ((int)ch[1]) << 16 ) | ( ((int)ch[2]) << 8 ) | ( ((int)ch[3]) ) );
}

void readString( FILE * fp )
{
  long pos = ftell( fp );
  char ch2[2];
  int j, len, c0, c1;
  char ch;
  fread( ch2, sc, 2, fp ); // skip 2 bytes
  c0 = ch2[0]; if ( c0 < 0 ) c0 += 256;
  c1 = ch2[1]; if ( c1 < 0 ) c1 += 256;
  len = ( c0 << 8 ) | c1;
  printf("%ld= [length %d] \n", pos, len);
  for ( j=0; j<len; ++j ) {
    fread( &ch, sc, 1, fp );
    printf("%c %02X ", ch, ch );
  }
  printf("\n");
}

void read4ch( FILE * fp, int skip )
{
  char ch;
  int j;
  for ( j=0; j<skip; ++j ) {
    fread( &ch, sc, 1, fp );
    printf(" %02x ", ch );
  }
}

int readInt( FILE * fp )
{
  uint32_t i32;
  fread( &i32, s32, 1, fp ); 
  return ntohl( i32 );
}

float readFloat( FILE * fp )
{
  uint32_t i32;
  int i;
  float * pf;
  fread( &i32, s32, 1, fp ); 
  i = ntohl( i32 ); 
  pf = (float *)&i;
  return *pf;
}

void readVersion( FILE * fp )
{
  long pos = ftell( fp );
  int v = readInt( fp );
  VERSION = v;
  printf("%ld= V: %d\n", pos, v );
}

void readScrap( FILE * fp )
{
  long pos = ftell( fp );
  int type, j, k;
  printf("%ld= S <", pos);
  readString( fp );
  type = readInt( fp );
  printf("> TYPE %d \n", type );
  for ( int k=0; k<3; ++k ) {
    // j = readInt( fp );
    // printf("%d ", j );
    readString( fp );
    printf("\n");
    // read4ch( fp, 2 ); printf("\n");
  }
}

void readBBox( FILE * fp )
{
  long pos = ftell( fp );
  float x1 = readFloat( fp );
  float y1 = readFloat( fp );
  float x2 = readFloat( fp );
  float y2 = readFloat( fp );
  int north = readInt( fp );
  printf("%ld= bbox %.2f %.2f %.2f %.2f (north %d)\n", pos, x1, y1, x2, y2, north );
  if ( north == 1 ) {
    x1 = readFloat( fp );
    y1 = readFloat( fp );
    x2 = readFloat( fp );
    y2 = readFloat( fp );
    printf("     %.2f %.2f %.2f %.2f\n", x1, y1, x2, y2 );
  }
}

void readPoint( FILE * fp )
{
  long pos = ftell( fp );
  int scale, lvl;
  float orient;
  float cx = readFloat( fp );
  float cy = readFloat( fp ); 
  printf("%ld= P: %.3f %.3f ", pos, cx, cy );
  readString( fp );
  orient = readFloat( fp );
  scale  = readInt( fp );
  lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  printf(" az. %.2f lvl %02x scale %d options <", orient, lvl, scale );
  readString( fp );  // text
  readString( fp );  // options
  printf(">\n");
}
  
void readLabel( FILE * fp )
{
  int lvl, scale;
  float azi;
  long pos = ftell( fp );
  float cx = readFloat( fp );
  float cy = readFloat( fp ); 
  azi   = readFloat( fp ); // orientation
  scale = readInt( fp );   
  lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  printf("%ld= T: %.3f %.3f az. %.1f scale %d lvl %02x Text: ", pos, cx, cy, azi, scale, lvl );
  readString( fp );      // text
  printf(" options <");
  readString( fp );      // options
  printf(">\n");
}

void readLinePoint( FILE * fp )
{
  long pos = ftell( fp );
  char ch;
  int lvl;
  float x = readFloat( fp );
  float y = readFloat( fp );
  fread( &ch, sc, 1, fp );
  lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  printf("%ld= %.2f %.2f %d %02x", pos, x, y, ch, lvl );
  if ( ch == 1 ) {
    x = readFloat( fp );
    y = readFloat( fp );
    printf(" %.2f %.2f", x, y );
    x = readFloat( fp );
    y = readFloat( fp );
    printf(" %.2f %.2f", x, y );
  }
  printf("\n");
}

void readLine( FILE * fp )
{
  long pos = ftell( fp );
  int k , np;
  char closed, reversed;
  int outline, lvl;
  printf("%ld= L: ", pos);
  readString( fp );                // name
  fread( &closed, sc, 1, fp );
  fread( &reversed, sc, 1, fp );
  outline = readInt( fp );
  lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  printf(" Closed %d Rev. %d Outline %d Lvl %02x: <", closed, reversed, outline, lvl );
  readString( fp );     // options
  np = readInt( fp );   // nr. points
  printf("> NP %d \n", np );
  for ( int k=0; k<np; ++k ) readLinePoint( fp );
}

void readArea( FILE * fp )
{
  long pos = ftell( fp );
  char ch;
  int cnt, np, lvl;
  float orient;
  printf("%ld= A: ", pos);
  readString( fp );         // name
  printf(" - ");
  readString( fp );         // prefix
  cnt = readInt( fp );      // counter
  fread( &ch, sc, 1, fp );  // visibility
  orient = readFloat( fp );
  lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  np = readInt( fp );       // nr points
  printf(" # %d ch %d az. %.2f lvl %02x NP %d\n", cnt, ch, orient, lvl, np );
  for ( int k=0; k<np; ++k ) readLinePoint( fp );
}

void readStation( FILE * fp )
{
  int lvl, section;
  long pos = ftell( fp );
  float x = readFloat( fp ); // position
  float y = readFloat( fp );
  printf("%ld= St: ", pos);
  readString( fp );          // name
  lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  printf(" %.2f %.2f lvl %02x\n", x, y, lvl );
  section = readInt();
  if ( section != 0 ) {
    float a = readFloat( fp ); // azimuth
    float c = readFloat( fp ); // clino
  }
}

void readUserStation( FILE * fp )
{
  long pos = ftell( fp );
  float x = readFloat( fp );   // position
  float y = readFloat( fp );
  int s = readInt( fp );       // scale
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  printf("%ld= USt: ", pos);
  readString( fp );            // name
  printf(" (scale %d) %.2f %.2f lvl %02x\n", s, x, y, lvl );
}

void readSpecial( FILE * fp )
{
  long pos = ftell( fp );
  int s   = readInt( fp );   // type
  float x = readFloat( fp ); // center X
  float y = readFloat( fp ); // center Y
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  printf("%ld= Special (type %d) at %.2f %.2f lvl %02x\n", pos, s, x, y, lvl );
}

void readSpecialPoint( FILE * fp, const char * type ) 
{
  long pos = ftell( fp );
  float x = readFloat( fp );
  float y = readFloat( fp );
  float o = 0; 
  if ( VERSION > 207043 ) o = readFloat( fp ); // orientation 
  int s = readInt( fp ); // scale
  int lvl = (VERSION >= 401090 )? readInt( fp ) : 0xff;
  printf("%ld= Special point %s lvl %02x: ", pos, type, lvl );
  readString( fp ); // point text
  readString( fp ); // options
  int id = readInt( fp );
  printf(" (orient. %.2f scale %d) at %.2f %.2f id %d\n", o, s, x, y, id );
}


int main( int argc, char ** argv )
{
  long pos = 0;
  FILE * fp;
  int done = 0;
  char ch;
  char str[128];
  int  i, j;
  float f;

  if ( argc <= 1 ) {
    printf("Usage: %s filename\n", argv[0] );
    return 0;
  }

  fp = fopen( argv[1], "r" );
  if ( fp == NULL ) {
    printf("Cannot open file \"%s\"\n", argv[1] );
    return 0;
  }

  while ( done == 0 && fread( &ch, sc, 1, fp ) != 0 ) {
    switch ( ch ) {
      case 'V':
        readVersion( fp );
        break;
      case 'S':
        readScrap( fp );
        break;
      case 'I':
        readBBox( fp );
        break;
      case 'P': // point
        readPoint( fp );
        break;
      case 'T': // text label
        readLabel( fp );
        break;
      case 'L': // line
        readLine( fp );
        break;
      case 'A': // area
        readArea( fp );
        break;
      case 'U': // user station
        readUserStation( fp );
        break;
      case 'X': // station name
        readStation( fp );
        break;
      case 'J': // special
	readSpecial( fp );
      case 'Y': // photo
	readSpecialPoint( fp, "Photo" );
	break;
      case 'Z': // audio
	readSpecialPoint( fp, "Audio" );
	break;
      case 'F':
        printf("%ld= F-CHAR %02x <%c>\n", pos, ch, ch );
        break;
      default:
      case 'E':
        printf("%ld= E-CHAR %02x <%c>\n", pos, ch, ch );
        // done = 1;
        break;
    }
    pos = ftell( fp );
  }
  fclose( fp );
  return 0;
}


