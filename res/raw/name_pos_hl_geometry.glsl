// name_pos_hl_geometry

layout (lines, max_vertices = 4) out;
  
void main() {    
    gl_Position = gl_in[0].gl_Position + vec4(-0.1, 0.0, 0.0, 0.0); 
    EmitVertex();
    gl_Position = gl_in[0].gl_Position + vec4( 0.1, 0.0, 0.0, 0.0);
    EmitVertex();
    
    gl_Position = gl_in[0].gl_Position + vec4(0.0, -0.1, 0.0, 0.0); 
    EmitVertex();
    gl_Position = gl_in[0].gl_Position + vec4(0.0,  0.1, 0.0, 0.0);
    EmitVertex();

    EndPrimitive();
}    
