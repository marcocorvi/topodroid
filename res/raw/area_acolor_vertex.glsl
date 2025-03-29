// area_acolor_vertex.glsl - lint: appear to be unused

uniform mat4 uMVPMatrix;

attribute vec4 aPosition;

attribute vec4 aColor;
varying   vec4 vColor;

void main() {
  gl_Position = uMVPMatrix * aPosition;
  vColor = aColor;
}
