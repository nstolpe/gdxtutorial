package com.hh.ghostengine.globals;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.utils.*;
import com.hh.ghostengine.entity.components.AmbientComponent;
import com.hh.ghostengine.entity.components.DirectionalLightComponent;
import com.hh.ghostengine.entity.components.PointLightComponent;
import com.hh.ghostengine.entity.components.SpotLightComponent;

/**
 * Created by nils on 10/9/16.
 */
public class Scene {
	private Scene() {}

	public static void SetupScene(FileHandle file) {
		JsonValue sceneData = new JsonReader().parse(file);
		Environment environment = new Environment();
		Entity scene = new Entity();
		setUpLights(scene, environment, sceneData.get("lights"));
	}

	private static void setUpLights(Entity scene, Environment environment, JsonValue lights) {
		if (!lights.get("ambient").isNull()) addAmbient(scene, environment, lights.get("ambient"));

		JsonValue directional = lights.get("directional");
		for (JsonValue entry = directional.child; entry != null; entry = entry.next)
			addDirectionalLight(scene, environment, entry);

		JsonValue point = lights.get("point");
		for (JsonValue entry = point.child; entry != null; entry = entry.next)
			addPointLight(scene, environment, entry);

		JsonValue spot = lights.get("spot");
		for (JsonValue entry = spot.child; entry != null; entry = entry.next)
			addSpotLight(scene, environment, entry);
	}

	private static void addAmbient(Entity scene, Environment environment, JsonValue lightData) {
		float r = lightData.get("r").asFloat();
		float g = lightData.get("g").asFloat();
		float b = lightData.get("b").asFloat();
		float a = lightData.get("a").asFloat();
		ColorAttribute light = new ColorAttribute(ColorAttribute.AmbientLight, r, g, b, a);
		AmbientComponent component = new AmbientComponent(light);
		scene.add(component);
		environment.set(light);
	}

	private static void addDirectionalLight(Entity scene, Environment environment, JsonValue lightData) {
		float r = lightData.get("color").get("r").asFloat();
		float g = lightData.get("color").get("g").asFloat();
		float b = lightData.get("color").get("b").asFloat();
		float dirX = lightData.get("direction").get("x").asFloat();
		float dirY = lightData.get("direction").get("y").asFloat();
		float dirZ = lightData.get("direction").get("z").asFloat();
		DirectionalLight light = new DirectionalLight().set(r, g, b, dirX, dirY, dirZ);
		DirectionalLightComponent component = new DirectionalLightComponent(light);
		scene.add(component);
		environment.add(light);
	}
	private static void addPointLight(Entity scene, Environment environment, JsonValue lightData) {
		float r = lightData.get("color").get("r").asFloat();
		float g = lightData.get("color").get("g").asFloat();
		float b = lightData.get("color").get("b").asFloat();
		float x = lightData.get("position").get("x").asFloat();
		float y = lightData.get("position").get("y").asFloat();
		float z = lightData.get("position").get("z").asFloat();
		float intensity = lightData.get("intensity").asFloat();
		PointLight light = new PointLight().set(r, g, b, x, y, z, intensity);
		PointLightComponent component = new PointLightComponent(light);
		scene.add(component);
		environment.add(light);
	}
	private static void addSpotLight(Entity scene, Environment environment, JsonValue lightData) {
		float r = lightData.get("color").get("r").asFloat();
		float g = lightData.get("color").get("g").asFloat();
		float b = lightData.get("color").get("b").asFloat();
		float posX = lightData.get("position").get("posX").asFloat();
		float posY = lightData.get("position").get("posY").asFloat();
		float posZ = lightData.get("position").get("posZ").asFloat();
		float dirX = lightData.get("position").get("dirX").asFloat();
		float dirY = lightData.get("position").get("dirY").asFloat();
		float dirZ = lightData.get("position").get("dirZ").asFloat();
		float intensity = lightData.get("intensity").asFloat();
		float cutoffAngle = lightData.get("cutoffAngle").asFloat();
		float exponent = lightData.get("exponent").asFloat();
		SpotLight light = new SpotLight().set(r, g, b, posX, posY, posZ, dirX, dirY, dirZ, intensity, cutoffAngle, exponent);
		SpotLightComponent component = new SpotLightComponent(light);
		scene.add(component);
		environment.add(light);
	}
}
