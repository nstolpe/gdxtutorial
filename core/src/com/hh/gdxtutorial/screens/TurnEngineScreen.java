package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/**
 * Created by nils on 5/27/16.
 */
public class TurnEngineScreen  extends AbstractScreen {
	public PerspectiveCamera camera;
	public CameraInputController camController;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public ModelInstance plane;

	public ModelBatch modelBatch;

	public Environment environment;

	@Override
	public void show() {
		clear = new Color(1.0f, 0.0f, 0.0f, 1.0f);

		// declare and configure the camera.
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(5.0f, 5.0f, 5.0f);
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

		plane = new ModelInstance(assetManager.get("models/plane.g3dj", Model.class));
		plane.transform.setTranslation(0.0f, 0.0f, 0.0f);
		plane.transform.setToRotation(new Vector3(1.0f, 0.0f, 0.0f), -90);
		instances.add(plane);
		ModelInstance sphere = new ModelInstance(assetManager.get("models/sphere.g3dj", Model.class));
		instances.add(sphere);
	}
}
