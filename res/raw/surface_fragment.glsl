// surface_fragment.glsl

precision mediump float;

uniform sampler2D uTexUnit;
uniform float uAlpha;

varying vec2 vTexture;
varying float vAlpha;

void main()
{
  gl_FragColor = vAlpha * texture2D( uTexUnit, vTexture );
  // gl_FragColor.a = vAlpha * uAlpha;
  gl_FragColor.a = uAlpha;
}

