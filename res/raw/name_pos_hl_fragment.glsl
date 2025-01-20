// name_pos_hl_fragment.glsl
// same as name_hl_fragment

precision mediump float;

uniform vec4 uColor;
varying float vAlpha;

void main()
{
  // gl_FragColor = vec4( 1.0, 0.0, 0.0, 1.0 );
  gl_FragColor = uColor;
  gl_FragColor.a = vAlpha;
}


