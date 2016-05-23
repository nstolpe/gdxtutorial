#ifdef GL_ES
#define LOWP lowp
precision mediump float;
#else
#define LOWP
#endif

uniform sampler2D u_texture;
uniform vec2 u_size;
uniform float u_tiltPercentage;
uniform vec2 u_dimension;
varying vec2 v_texCoords;


void main() {
	float y = v_texCoords.y;
	float[9] weights;
	weights[0] = 0.063327;
	weights[1] = 0.093095;
	weights[2] = 0.122589;
	weights[3] = 0.144599;
	weights[4] = 0.152781;
	weights[5] = 0.144599;
	weights[6] = 0.122589;
	weights[7] = 0.093095;
	weights[8] = 0.063327;

	vec2 offset = vec2(1.0, 1.0) / u_size;
	float cuttoffHigh = 1.0 - (u_size.y * offset.y * u_tiltPercentage / 2.0);
	float cuttoffLow = 0.0 + (u_size.y * offset.y * u_tiltPercentage / 2.0);

	if (y < cuttoffLow || y > cuttoffHigh) {
		float cutoffDistance = y < cuttoffLow ? cuttoffLow - y : y - cuttoffHigh;
		vec2 multiplier = u_dimension / u_size * cutoffDistance * 10.0;
		vec4 sum = texture2D(u_texture, v_texCoords) * weights[4];

		sum += texture2D(u_texture, v_texCoords + -3.0 * multiplier) * weights[1];
		sum += texture2D(u_texture, v_texCoords + -2.0 * multiplier) * weights[2];
		sum += texture2D(u_texture, v_texCoords + -4.0 * multiplier) * weights[0];
		sum += texture2D(u_texture, v_texCoords + -1.0 * multiplier) * weights[3];
		sum += texture2D(u_texture, v_texCoords +  1.0 * multiplier) * weights[5];
		sum += texture2D(u_texture, v_texCoords +  2.0 * multiplier) * weights[6];
		sum += texture2D(u_texture, v_texCoords +  3.0 * multiplier) * weights[7];
		sum += texture2D(u_texture, v_texCoords +  4.0 * multiplier) * weights[8];

		gl_FragColor = sum;
	} else {
		gl_FragColor = texture2D(u_texture, v_texCoords);
	}
}