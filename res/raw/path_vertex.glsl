// path_vertex.glsl

uniform mat4 uMVPMatrix;

attribute vec4 aPosition;
// uniform float uPointSize;

void main() {
  gl_Position  = uMVPMatrix * aPosition;
  // gl_PointSize = uPointSize;
}
