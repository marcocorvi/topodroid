// triangle_fragment.glsl

precision mediump float;

uniform vec4 uColor;
varying float vAlpha;

void main()
{
  gl_FragColor = uColor;
  gl_FragColor.a = vAlpha;
  // gl_FragColor.a = 0.5;
}
