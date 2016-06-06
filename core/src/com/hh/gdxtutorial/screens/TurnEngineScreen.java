package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.engines.turn.Actor;
import com.hh.gdxtutorial.engines.turn.TurnEngine;
import com.hh.gdxtutorial.screens.input.TurnInputController;

/**
 * Created by nils on 5/27/16.
 */
public class TurnEngineScreen extends FpsScreen {
	public TurnInputController camController;

	public AssetManager assetManager;
	public Array<ModelInstance> instances = new Array<ModelInstance>();
	public Array<Actor> actors = new Array<Actor>();
	public ModelInstance plane;

	public ModelBatch modelBatch;

	public Environment environment;
	public Texture tex;

	protected Label turnLabel;

	public TurnEngine turnEngine = new TurnEngine();

	@Override
	public void show() {
		super.show();

		// declare camController and set it as the input processor.
		camController = new TurnInputController(camera);
		multiplexer.addProcessor(camController);

		modelBatch = new ModelBatch();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, 0.5f, -1.0f));

		assetManager = new AssetManager();
		assetManager.load("models/plane.g3dj", Model.class);
		assetManager.load("models/sphere.g3dj", Model.class);
	}
	@Override
	public void initStage() {
		super.initStage();
		turnLabel = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
		table.row();
		table.add(turnLabel).expandY().bottom();
	}
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		camController.update();

		if (loading && assetManager.update()) doneLoading();

		MessageManager.getInstance().update();
		turnEngine.update(delta);
		runModelBatch(modelBatch, camera, instances, environment);

		stringBuilder.setLength(0);
		stringBuilder.append(" Turn: ").append(turnEngine.turnCount);
		turnLabel.setText(stringBuilder);
		super.render(delta);
	}
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
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
		instances.add(plane);
	}

	@Override
	public void dispose() {
		super.dispose();
		assetManager.dispose();
		modelBatch.dispose();
		instances.clear();
		tex.dispose();
	}
}
