// line_ucolor_vertex.glsl

uniform mat4 uMVPMatrix;

uniform float uPointSize;
attribute vec4 aPosition;

attribute vec4 aColor;
uniform vec4 uColor;
varying vec4 vColor;

void main() {
  gl_Position = uMVPMatrix * aPosition;
  gl_PointSize = uPointSize;
  vColor = uColor;
  vColor.a *= aColor.a;
}
