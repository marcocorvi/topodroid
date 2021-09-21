// surface_gray_fragment.glsl

precision mediump float;

uniform float uAlpha;
uniform vec4  uColor;

varying float vAlpha;

void main()
{
  gl_FragColor = vAlpha * uColor;
  // gl_FragColor.a *= vAlpha;
  gl_FragColor.a = uAlpha;
}

