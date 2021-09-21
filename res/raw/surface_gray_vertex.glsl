// surface_gray_vertex.glsl

uniform mat4 uMVPMatrix;
uniform mat4 uMVMatrixInvT;

attribute vec4 aPosition;
attribute vec4 aNormal;
uniform   vec4 uLight;

// uniform float uAlpha;
varying float vAlpha;

void main()
{
  // vAlpha   = min( uAlpha * max( dot( uLight, normalize(uMVMatrixInvT * aNormal) ), 0.0 ), 1.0 );
  vAlpha   = max( dot( uLight, normalize(uMVMatrixInvT * aNormal) ), 0.0 );
  gl_Position = uMVPMatrix * aPosition;
}
