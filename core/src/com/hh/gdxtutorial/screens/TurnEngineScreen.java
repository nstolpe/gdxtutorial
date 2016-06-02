package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.engines.turn.Actor;
import com.hh.gdxtutorial.engines.turn.TurnEngine;

/**
 * Created by nils on 5/27/16.
 */
public class TurnEngineScreen extends AbstractScreen {
	public PerspectiveCamera camera;
	public CameraInputController camController;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public Array<Actor> actors = new Array<Actor>();
	public ModelInstance plane;

	public ModelBatch modelBatch;

	public Environment environment;
	public Texture tex;

	public Vector3 origin = new Vector3(0, 2, 0);

	public TurnEngine turnEngine = new TurnEngine();

	@Override
	public void show() {
		clear = new Color(1.0f, 0.0f, 0.0f, 1.0f);

		// declare and configure the camera.
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(20.0f, 20.0f, 20.0f);
		camera.lookAt(0, 0, 0);
		camera.near = 1;
		camera.far = 1000;
		camera.update();
		// declare camController and set it as the input processor.
		camController = new CameraInputController(camera);
		Gdx.input.setInputProcessor(camController);

		modelBatch = new ModelBatch();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, 0.5f, -1.0f));

		assetManager = new AssetManager();
		assetManager.load("models/plane.g3dj", Model.class);
		assetManager.load("models/sphere.g3dj", Model.class);
	}
	@Override
	public void render(float delta) {
		camController.update();
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		if (loading && assetManager.update()) doneLoading();

		MessageManager.getInstance().update();
		turnEngine.update(delta);
		runModelBatch(modelBatch, camera, instances, environment);
	}
	@Override
	public void resize(int width, int height) {
		camera.viewportWidth = width;
		camera.viewportHeight = height;
		camera.update();
	}
	@Override
	public void doneLoading() {
		super.doneLoading();
		setupScene();
		setupActors();
	}


	public void setupActors() {
		// create the player sphere/Actor, set it's position, add to actors and instances.
		Actor player = new Actor(new ModelInstance(assetManager.get("models/sphere.g3dj", Model.class)), Actor.PLAYER);
		player.position.set(0, 2, 0);
		instances.add(player.instance);
		actors.add(player);

		// create texture for mobs
		tex = new Texture(Gdx.files.internal("models/sphere-purple.png"), true);
		tex.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Nearest);
		TextureAttribute texAttr = new TextureAttribute(TextureAttribute.Diffuse, tex);

		// create and position the mobs spheres/Actors
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				Actor mob = new Actor(new ModelInstance(assetManager.get("models/sphere.g3dj", Model.class)), Actor.MOB);
				mob.instance.getMaterial("skin").set(texAttr);
				mob.position.set(i * 20, 2, j * 20);
				actors.add(mob);
				instances.add(mob.instance);
			}
		}

		turnEngine.actors.addAll(actors);
		turnEngine.start();
	}

	public void setupScene() {
		plane = new ModelInstance(assetManager.get("models/plane.g3dj", Model.class));
		plane.transform.setTranslation(0.0f, 0.0f, 0.0f);
		plane.transform.setToRotation(new Vector3(1.0f, 0.0f, 0.0f), -90);
		instances.add(plane);
	}

	@Override
	public void dispose() {
		assetManager.dispose();
		modelBatch.dispose();
		tex.dispose();
	}
}
