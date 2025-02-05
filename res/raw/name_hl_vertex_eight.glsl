// name_hl_vertex.glsl

uniform mat4 uMVPMatrix;

attribute vec4 aPosition;
attribute vec2 aDelta;
// attribute float aVisible;
uniform float uTextSize;
// varying float vAlpha;

void main() {
  vec4 tmp1 = uMVPMatrix * aPosition;
  float t = 1.2 * uTextSize;
  // vAlpha  = aVisible;
  vec4 tmp2 = t * vec4(aDelta.xy, 0.0, 0.0 );
  gl_Position = tmp1 + tmp2; 
}
