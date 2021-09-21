// line_scolor_vertex.glsl

uniform mat4 uMVPMatrix;

uniform float uPointSize;
attribute vec4 aPosition;
attribute vec4 aColor;
attribute float aDColor;

varying   vec4 vColor;

void main() {
  gl_Position = uMVPMatrix * aPosition;
  gl_PointSize = uPointSize;
  vec4 color;
  color.x = aDColor;
  color.z = 1.0 - aDColor;
  color.y = 0.0; // 0.3 * aDColor;
  color.a = aColor.a;
  vColor = color;
}
