package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.AnimationController;
import com.badlogic.gdx.utils.Array;
import com.hh.gdxtutorial.screens.input.DemoInputController;

/**
 * Created by nils on 5/27/16.
 */
public class AnimationChainScreen extends FpsScreen {
	public DemoInputController camController;
	public AnimationController animationController;

	public AssetManager assetManager;
	public ModelBatch modelBatch;
	public Array<ModelInstance> instances = new Array<ModelInstance>();

	public Environment environment;

	@Override
	public void show() {
		super.show();

		// declare camController and set it as the input processor.
		camController = new DemoInputController(camera);
		multiplexer.addProcessor(camController);

		modelBatch = new ModelBatch();

		environment = new Environment();
		environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
//		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -0.5f, 0.5f, -1.0f));

		ModelLoader.ModelParameters texParam = new ModelLoader.ModelParameters();
		texParam.textureParameter.minFilter = Texture.TextureFilter.MipMapLinearNearest;
		texParam.textureParameter.genMipMaps = true;

		assetManager = new AssetManager();
		assetManager.load("models/mask.ghost.white.g3dj", Model.class,texParam);
	}
	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

		camController.update();
		if (animationController != null) animationController.update(delta);

		if (loading && assetManager.update()) doneLoading();

		modelBatch.begin(camera);
		modelBatch.render(instances, environment);
		modelBatch.end();

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
		ModelInstance instance = new ModelInstance(assetManager.get("models/mask.ghost.white.g3dj", Model.class));
		instances.add(instance);
		animationController = new AnimationController(instance);
		initAnimationSequence();
	}

	public void initAnimationSequence() {
		animationController.setAnimation("skeleton|attack.pre", new AnimationController.AnimationListener() {
			@Override
			public void onEnd(AnimationController.AnimationDesc animation) {
				animationController.setAnimation("skeleton|attack", new AnimationController.AnimationListener() {
					@Override
					public void onEnd(AnimationController.AnimationDesc animation) {
						animationController.setAnimation("skeleton|attack.post", new AnimationController.AnimationListener() {
							@Override
							public void onEnd(AnimationController.AnimationDesc animation) {
								initAnimationSequence();
							}

							@Override
							public void onLoop(AnimationController.AnimationDesc animation) {

							}
						});
					}

					@Override
					public void onLoop(AnimationController.AnimationDesc animation) {

					}
				});
			}

			@Override
			public void onLoop(AnimationController.AnimationDesc animation) {

			}
		});
	}
	@Override
	public void dispose() {
		super.dispose();
		assetManager.dispose();
		modelBatch.dispose();
	}
}
