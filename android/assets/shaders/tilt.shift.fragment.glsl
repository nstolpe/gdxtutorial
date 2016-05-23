#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

uniform sampler2D u_texture;
uniform vec2 u_size;
uniform float u_tiltPercentage;
varying vec2 v_texCoords;

void main() {
	vec2 offset = vec2(1.0, 1.0) / u_size;
	float cuttoffHigh = 1.0 - (u_size.y * offset.y * u_tiltPercentage / 2.0);
	float cuttoffLow = 0.0 + (u_size.y * offset.y * u_tiltPercentage / 2.0);

	if (v_texCoords.y < cuttoffLow || v_texCoords.y > cuttoffHigh) {
		vec3 sum = vec3(0.0,0.0,0.0);
		sum += texture2D(u_texture, v_texCoords + vec2(-1.0, 1.0) * offset).rgb * 0.107035f;
		sum += texture2D(u_texture, v_texCoords + vec2(0.0, 1.0) * offset).rgb * 0.113092f;
		sum += texture2D(u_texture, v_texCoords + vec2(1.0, 1.0) * offset).rgb * 0.107035f;
		sum += texture2D(u_texture, v_texCoords + vec2(-1.0, 0.0) * offset).rgb * 0.113092f;
		sum += texture2D(u_texture, v_texCoords).rgb * 0.119491f;
		sum += texture2D(u_texture, v_texCoords + vec2(1.0, 0.0) * offset).rgb * 0.113092f;
		sum += texture2D(u_texture, v_texCoords + vec2(-1.0, -1.0) * offset).rgb * 0.107035f;
		sum += texture2D(u_texture, v_texCoords + vec2(0.0, -1.0) * offset).rgb * 0.113092f;
		sum += texture2D(u_texture, v_texCoords + vec2(1.0, -1.0) * offset).rgb * 0.107035f;

//		sum += texture2D(u_texture, v_texCoords + vec2(1.0, -1.0) / u_size).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(1.0, 0.0) / u_size).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(1.0, 1.0) / u_size).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(-1.0, 0.0) / u_size).rgb;
//		sum += texture2D(u_texture, v_texCoords).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(1.0, 0.0) / u_size).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(-1.0, -1.0) / u_size).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(0.0, -1.0) / u_size).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(1.0, -1.0) / u_size).rgb;
		gl_FragColor = vec4(sum, 1.0f);
//		vec3 sum = vec3(0.0);
//		for (float x = 1.0; x <= 9.0; x++) {
//			for (float y = 1.0; y <= 9.0; y++) {
//				sum += texture2D(u_texture, v_texCoords + vec2(x, y) * offset).rgb;
//			}
//		}
//		gl_FragColor = vec4(sum / 81.0, 1.0);
	} else {
//		gl_FragColor = vec4(texture2D(u_texture, v_texCoords).rgb, 1.0);
//		vec3 sum = texture2D(u_texture, v_texCoords).rgb;

//		sum += texture2D(u_texture, v_texCoords + vec2(-1.0, 1.0) * offset).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(0.0, 1.0) * offset).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(1.0, 1.0) * offset).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(-1.0, 0.0) * offset).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(1.0, 0.0) * offset).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(-1.0, -1.0) * offset).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(0.0, -1.0) * offset).rgb;
//		sum += texture2D(u_texture, v_texCoords + vec2(1.0, -1.0) * offset).rgb;
//		gl_FragColor = vec4(sum / 5.0, 1.0f);



		gl_FragColor = texture2D(u_texture, v_texCoords);
//		gl_FragColor = texture2D(u_texture, v_texCoords) * u_tiltPercentage;
	}
}