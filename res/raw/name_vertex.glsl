// name_vertex.glsl

uniform mat4 uMVPMatrix;

attribute vec4 aPosition;
attribute vec2 aDelta;
attribute vec2 aTexCoord;
uniform float uTextSize;
varying vec2 vTexture;

void main() {
  vTexture = aTexCoord;
  vec4 tmp1 = uMVPMatrix * aPosition;
  // vec4 tmp2 = uTextSize * vec4(aDelta.xy, 0.0, 1.0/tmp1.a);
  vec4 tmp2 = uTextSize * vec4(aDelta.xy, 0.0, 0.0 );
  gl_Position = tmp1 + tmp2; // variable scale, in 2D, and at variable position along the segment
}
