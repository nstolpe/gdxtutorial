package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
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

/**
 * Created by nils on 5/27/16.
 */
public class TurnEngineScreen  extends AbstractScreen {
	public PerspectiveCamera camera;
	public CameraInputController camController;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public Array<ModelInstance> mobSpheres = new Array<ModelInstance>();
	public ModelInstance plane;

	public ModelBatch modelBatch;

	public Environment environment;
	public ModelInstance playerSphere;
	public Texture tex;

	Vector3 origin = new Vector3(0, 2, 0);

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

		Vector3 currentPos = new Vector3();

		for(ModelInstance ms : mobSpheres) {
			ms.transform.getTranslation(currentPos);
			if (currentPos.dst(origin) >= 0) {
				Vector3 direction = origin.sub(currentPos).nor();
				System.out.println(direction);
				ms.transform.translate(direction.x * delta * 20, 0, direction.z * delta * 20);
			} else {
				ms.transform.setTranslation(origin);
			}
		}
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
		setupPlane();
		setupSpheres();
	}

	public void setupSpheres() {
		playerSphere = new ModelInstance(assetManager.get("models/sphere.g3dj", Model.class));
		playerSphere.transform.setTranslation(0, 2, 0);
		instances.add(playerSphere);

		tex = new Texture(Gdx.files.internal("models/sphere-purple.png"), true);
		tex.setFilter(Texture.TextureFilter.MipMapLinearNearest, Texture.TextureFilter.Nearest);
		TextureAttribute texAttr = new TextureAttribute(TextureAttribute.Diffuse, tex);

		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				ModelInstance sphere = new ModelInstance(assetManager.get("models/sphere.g3dj", Model.class));
				sphere.getMaterial("skin").set(texAttr);
				sphere.transform.setTranslation(i * 20, 2, j * 20);
				instances.add(sphere);
				mobSpheres.add(sphere);
			}
		}
	}

	public void setupPlane() {
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
