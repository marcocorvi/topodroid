// line_acolor_fragment.glsl

precision mediump float;

// uniform float uAlpha;
varying vec4 vColor;

void main()
{
  gl_FragColor = vColor;
  // gl_FragColor.a = uAlpha;
}
