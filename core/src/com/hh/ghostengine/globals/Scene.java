package com.hh.ghostengine.globals;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.environment.PointLight;
import com.badlogic.gdx.graphics.g3d.environment.SpotLight;
import com.badlogic.gdx.utils.*;
import com.hh.ghostengine.entity.components.*;

/**
 * Created by nils on 10/9/16.
 */
public class Scene {
	private Scene() {}

	public static void SetupScene(FileHandle file) {
		JsonValue sceneData = new JsonReader().parse(file);
		Environment environment = new Environment();
		Entity scene = new Entity();

		scene.add(new EnvironmentComponent(environment));

		addLighting(scene, sceneData.get("lights"));
		Manager.getInstance().engine().addEntity(scene);
	}

	private static void addLighting(Entity scene, JsonValue lights) {
		if (!lights.get("ambient").isNull()) addAmbient(scene, lights.get("ambient"));

		addDirectionalLights(lights.get("directional"), scene);

		addPointLights(lights.get("point"), scene);

		addSpotLights(lights.get("point"), scene);
	}

	private static void addSpotLights(JsonValue lights, Entity scene) {
		Environment environment = Mappers.ENVIRONMENT.get(scene).environment;

		for (JsonValue entry = lights.child; entry != null; entry = entry.next) {
			SpotLight light = spotLight(entry);
			SpotLightComponent component = new SpotLightComponent(light);
			scene.add(component);
			environment.add(light);
		}
	}
	private static void addPointLights(JsonValue lights, Entity scene) {
		Environment environment = Mappers.ENVIRONMENT.get(scene).environment;

		for (JsonValue entry = lights.child; entry != null; entry = entry.next) {
			PointLight light = pointLight(entry);
			PointLightComponent component = new PointLightComponent(light);
			scene.add(component);
			environment.add(light);
		}
	}

	private static void addDirectionalLights(JsonValue lights, Entity scene) {
		Environment environment = Mappers.ENVIRONMENT.get(scene).environment;

		for (JsonValue entry = lights.child; entry != null; entry = entry.next) {
			DirectionalLight light = directionalLight(entry);
			DirectionalLightComponent component = new DirectionalLightComponent(light);
			scene.add(component);
			environment.add(light);
		}
	}

	private static SpotLight spotLight(JsonValue lightData) {
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
		return new SpotLight().set(r, g, b, posX, posY, posZ, dirX, dirY, dirZ, intensity, cutoffAngle, exponent);
	}

	private static PointLight pointLight(JsonValue lightData) {
		float r = lightData.get("color").get("r").asFloat();
		float g = lightData.get("color").get("g").asFloat();
		float b = lightData.get("color").get("b").asFloat();
		float x = lightData.get("position").get("x").asFloat();
		float y = lightData.get("position").get("y").asFloat();
		float z = lightData.get("position").get("z").asFloat();
		float intensity = lightData.get("intensity").asFloat();
		return new PointLight().set(r, g, b, x, y, z, intensity);
	}

	private static DirectionalLight directionalLight(JsonValue lightData) {
		float r = lightData.get("color").get("r").asFloat();
		float g = lightData.get("color").get("g").asFloat();
		float b = lightData.get("color").get("b").asFloat();
		float dirX = lightData.get("direction").get("x").asFloat();
		float dirY = lightData.get("direction").get("y").asFloat();
		float dirZ = lightData.get("direction").get("z").asFloat();
		return new DirectionalLight().set(r, g, b, dirX, dirY, dirZ);
	}

	private static void addAmbient(Entity scene, JsonValue lightData) {
		float r = lightData.get("r").asFloat();
		float g = lightData.get("g").asFloat();
		float b = lightData.get("b").asFloat();
		float a = lightData.get("a").asFloat();

		ColorAttribute light = new ColorAttribute(ColorAttribute.AmbientLight, r, g, b, a);
		AmbientComponent component = new AmbientComponent(light);

		scene.add(component);
		Mappers.ENVIRONMENT.get(scene).environment.set(light);
	}
}
