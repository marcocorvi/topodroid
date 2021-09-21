// surface_vertex.glsl

uniform mat4 uMVPMatrix;
uniform mat4 uMVMatrixInvT;

attribute vec4 aPosition;
attribute vec4 aNormal;
attribute vec2 aTexCoord;
uniform   vec4 uLight;

varying vec2 vTexture;
varying float vAlpha;

void main()
{
  vTexture = aTexCoord;
  vAlpha   = min( 0.3 + 0.7*max( dot( uLight, normalize(uMVMatrixInvT * aNormal) ), 0.0 ), 1.0);
  gl_Position = uMVPMatrix * aPosition;
}
