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

	vec2 offset = vec2(1.0, 1.0) / u_size;
	float cuttoffHigh = 1.0 - (u_size.y * offset.y * u_tiltPercentage / 2.0);
	float cuttoffLow = 0.0 + (u_size.y * offset.y * u_tiltPercentage / 2.0);

	if (y < cuttoffLow || y > cuttoffHigh) {
		float cutoffDistance = y < cuttoffLow ? cuttoffLow - y : y - cuttoffHigh;
		vec2 multiplier = u_dimension / u_size * cutoffDistance * 10.0;

		// 9 tap gaussian
		vec4 sum = texture2D(u_texture, v_texCoords) * 0.152781;
		sum += texture2D(u_texture, v_texCoords + -4.0 * multiplier) * 0.063327;
		sum += texture2D(u_texture, v_texCoords + -3.0 * multiplier) * 0.093095;
		sum += texture2D(u_texture, v_texCoords + -2.0 * multiplier) * 0.122589;
		sum += texture2D(u_texture, v_texCoords + -1.0 * multiplier) * 0.144599;
		sum += texture2D(u_texture, v_texCoords +  1.0 * multiplier) * 0.144599;
		sum += texture2D(u_texture, v_texCoords +  2.0 * multiplier) * 0.122589;
		sum += texture2D(u_texture, v_texCoords +  3.0 * multiplier) * 0.093095;
		sum += texture2D(u_texture, v_texCoords +  4.0 * multiplier) * 0.063327;

		gl_FragColor = sum;
	} else {
		gl_FragColor = texture2D(u_texture, v_texCoords);
	}
}