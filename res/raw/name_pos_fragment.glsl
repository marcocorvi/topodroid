// name_pos_fragment.glsl

precision mediump float;

uniform vec4 uColor;
varying float vAlpha;

void main()
{
  gl_FragColor = uColor;
  gl_FragColor.a = vAlpha;
}
