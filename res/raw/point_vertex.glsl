// point_vertex.glsl

uniform mat4 uMVPMatrix;

uniform float uPointSize;
attribute vec4 aPosition;
attribute vec4 aColor;
varying   vec4 vColor;

void main() {
  gl_Position = uMVPMatrix * aPosition;
  gl_PointSize = uPointSize;
  vColor   = aColor;
  vColor.a = 1.0f;
}
