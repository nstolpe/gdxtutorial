package com.hh.ghostengine.globals;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.*;

/**
 * Created by nils on 10/9/16.
 */
public class Scene {
	public String model;
	public Vector3 position;
	public Quaternion rotation;

	private Scene() {}

	public static void SetupScene(FileHandle file) {
		JsonValue scene = new JsonReader().parse(file);
		Environment environment = new Environment();
		setUpLights(environment, scene.get("lights"));
	}

	private static void setUpLights(Environment environment, JsonValue lights) {
		if (!lights.get("ambient").isNull()) addAmbient(environment, lights.get("ambient"));

		JsonValue directional = lights.get("directional");
		for (JsonValue entry = directional.child; entry != null; entry = entry.next)
			addDirectionalLight(environment, entry);

		JsonValue point = lights.get("point");
		for (JsonValue entry = point.child; entry != null; entry = entry.next)
			addPointLight(environment, entry);

		JsonValue spot = lights.get("spot");
		for (JsonValue entry = spot.child; entry != null; entry = entry.next)
			addSpotLight(environment, entry);
	}

	private static void addAmbient(Environment environment, JsonValue ambient) {
		float r = ambient.get("r").asFloat();
		float g = ambient.get("g").asFloat();
		float b = ambient.get("b").asFloat();
		float a = ambient.get("a").asFloat();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, r, g, b, a));
	}

	private static void addDirectionalLight(Environment environment, JsonValue light) {
		float r = light.get("color").get("r").asFloat();
		float g = light.get("color").get("g").asFloat();
		float b = light.get("color").get("b").asFloat();
		float dirX = light.get("direction").get("x").asFloat();
		float dirY = light.get("direction").get("y").asFloat();
		float dirZ = light.get("direction").get("z").asFloat();
		DirectionalLight directionalLight = new DirectionalLight().set(r, g, b, dirX, dirY, dirZ);
		environment.add(directionalLight);
	}
	private static void addPointLight(Environment environment, JsonValue light) {
		float r = light.get("color").get("r").asFloat();
		float g = light.get("color").get("g").asFloat();
		float b = light.get("color").get("b").asFloat();
		float x = light.get("position").get("x").asFloat();
		float y = light.get("position").get("y").asFloat();
		float z = light.get("position").get("z").asFloat();
		float intensity = light.get("intensity").asFloat();
		PointLight pointLight = new PointLight().set(r, g, b, x, y, z, intensity);
		environment.add(pointLight);
	}
	private static void addSpotLight(Environment environment, JsonValue light) {
		float r = light.get("color").get("r").asFloat();
		float g = light.get("color").get("g").asFloat();
		float b = light.get("color").get("b").asFloat();
		float posX = light.get("position").get("posX").asFloat();
		float posY = light.get("position").get("posY").asFloat();
		float posZ = light.get("position").get("posZ").asFloat();
		float dirX = light.get("position").get("dirX").asFloat();
		float dirY = light.get("position").get("dirY").asFloat();
		float dirZ = light.get("position").get("dirZ").asFloat();
		float intensity = light.get("intensity").asFloat();
		float cutoffAngle = light.get("cutoffAngle").asFloat();
		float exponent = light.get("exponent").asFloat();
		SpotLight spotLight = new SpotLight().set(r, g, b, posX, posY, posZ, dirX, dirY, dirZ, intensity, cutoffAngle, exponent);
		environment.add(spotLight);
	}
	class Lights {
		public ColorAttribute ambient;
		public Array<DirectionalLight> directional;
		public Array<PointLight> point;
		public Array<SpotLight> spot;
	}


}
