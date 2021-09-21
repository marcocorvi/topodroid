// name_hl_vertex.glsl

uniform mat4 uMVPMatrix;

attribute vec4 aPosition;
attribute vec2 aDelta;
uniform float uTextSize;

void main() {
  vec4 tmp1 = uMVPMatrix * aPosition;
  float t = 1.2 * uTextSize;
  vec4 tmp2 = t * vec4(aDelta.xy, 0.0, 0.0 );
  gl_Position = tmp1 + tmp2; 
}
