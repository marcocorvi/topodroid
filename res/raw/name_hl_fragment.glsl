// name_hl_fragment.glsl

precision mediump float;

uniform vec4 uColor;

void main()
{
  // gl_FragColor = vec4( 1.0, 0.3, 0.0, 1.0 );
  gl_FragColor = uColor;
}


