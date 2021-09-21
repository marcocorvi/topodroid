// line_zcolor_vertex.glsl

uniform mat4 uMVPMatrix;

uniform float uPointSize;
attribute vec4 aPosition;
attribute vec4 aColor;

uniform   vec4 uColor;
varying   vec4 vColor;

uniform   float uZMin;
uniform   float uZDelta;

void main() {
  gl_Position = uMVPMatrix * aPosition;
  gl_PointSize = uPointSize;
  vec4 color;
  color.x = ( aPosition.y - uZMin )/uZDelta;
  color.z = 1.0 - vColor.x;
  color.y = 0.0; // 0.3 * vColor.z;
  color.a = aColor.a;
  vColor = color;
}
