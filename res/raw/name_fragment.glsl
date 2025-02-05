// name_fragment.glsl

precision mediump float;

uniform sampler2D uTexUnit;

varying vec2 vTexture;
// uniform float uAlpha;
varying float vAlpha;

void main()
{
  gl_FragColor = texture2D( uTexUnit, vTexture );
  gl_FragColor.a = vAlpha;
}

