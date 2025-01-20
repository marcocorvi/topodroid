// name_pos_hl_vertex.glsl

uniform mat4 uMVPMatrix;

uniform float uPointSize;
attribute vec4 aPosition;
attribute float aVisible;
varying float vAlpha;

void main() {
  gl_Position  = uMVPMatrix * aPosition;
  gl_PointSize = uPointSize;
  vAlpha = aVisible;
}
