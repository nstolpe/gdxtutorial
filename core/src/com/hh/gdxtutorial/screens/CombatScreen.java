package com.hh.gdxtutorial.screens;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.ModelLoader;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffectLoader;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.entity.components.*;
import com.hh.gdxtutorial.entity.systems.ModelBatchRenderer;
import com.hh.gdxtutorial.entity.systems.TurnSystem;
import com.hh.gdxtutorial.screens.input.DemoInputController;
import com.hh.gdxtutorial.shaders.PerPixelShaderProvider;

/**
 * Created by nils on 5/27/16.
 */
public class CombatScreen extends FpsScreen {
	public Engine engine = new Engine();
	public DemoInputController camController;

	public AssetManager assetManager;

	public ModelBatch modelBatch;

	public Environment environment;

	private Label turnLabel;

	private ModelBatchRenderer modelBatchRenderer;
	// particle
	private ParticleSystem particleSystem;
	private ParticleEffect effect;
	// particle
	private float rotperc = 0;
	private Quaternion rotquat = new Quaternion(0,1,0,0);

	/**
	 * Setup input, 3d environment, the modelBatch and the assetManager.
	 */
	@Override
	public void show() {
		super.show();

		// declare camController and set it as the input processor.
		camController = new DemoInputController(camera);
		multiplexer.addProcessor(camController);

		modelBatch = new ModelBatch(new PerPixelShaderProvider());

		environment = new Environment();
		environment.add(new DirectionalLight().set(1.0f, 1.0f, 1.0f, -0.5f, -0.6f, -0.7f));
		environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, 0.4f, 1.0f, -0.3f));

		// gets rid of the moire effect
		ModelLoader.ModelParameters param = new ModelLoader.ModelParameters();
		param.textureParameter.minFilter = Texture.TextureFilter.MipMapLinearNearest;
		param.textureParameter.genMipMaps = true;


		assetManager = new AssetManager();
		assetManager.load("models/plane.g3dj", Model.class, param);
		assetManager.load("models/mask.ghost.white.g3dj", Model.class, param);
		assetManager.load("models/mask.ghost.red.g3dj", Model.class, param);

		// particle
		particleSystem = new ParticleSystem();
		BillboardParticleBatch billboardParticleBatch = new BillboardParticleBatch();
		billboardParticleBatch.setCamera(camera);
		particleSystem.add(billboardParticleBatch);
		ParticleEffectLoader.ParticleEffectLoadParameter loadParam = new ParticleEffectLoader.ParticleEffectLoadParameter(particleSystem.getBatches());
		assetManager.load("effects/blast.blue.pfx", ParticleEffect.class, loadParam);
		assetManager.load("effects/blast.red.pfx", ParticleEffect.class, loadParam);
		// \particle
	}
	/**
	 * Adds extra turn data to the 2d stage, super gets the fps info.
	 */
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
		MessageManager.getInstance().update();

		if (loading && assetManager.update()) doneLoading();

		stringBuilder.setLength(0);
		stringBuilder.append(" Turn: ").append(engine.getSystem(TurnSystem.class) == null ? "" : engine.getSystem(TurnSystem.class).turnCount + ": " + engine.getSystem(TurnSystem.class).activeIndex());
		turnLabel.setText(stringBuilder);

		engine.update(delta);

		// @TODO get this test stuff out of here
		// testing attaching the emitter to the ghost.
		if (!loading) {
			Entity p = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).get(0);
			ModelInstance i = Mappers.MODEL_INSTANCE.get(p).instance();
			effect.setTransform(new Matrix4(Mappers.POSITION.get(p).position().cpy().add(i.getNode("emit.root").translation), new Quaternion(), new Vector3(1.0f, 1.0f, 1.0f)));
//			rotperc += delta;
//			if (rotperc >= 1) {
//				rotperc = 0;
////				rotquat = rotquat.cpy().conjugate() / rotquat.cpy().nor().mul(rotquat.cpy().nor();
//				if (rotquat.y == 1)
//					rotquat.set(0,0,0,1);
//				else
//					rotquat.set(0,1,0,0);
//			}
//			Mappers.ROTATION.get(p).rotation.slerp(rotquat, rotperc / 16);
		}
		Quaternion ooop = new Quaternion(delta, 0, 0, 1);
		System.out.println("original");
		System.out.println(ooop);
		System.out.println("normalized");
		System.out.println(ooop.nor());
		// particle
		modelBatch.begin(camera);
		particleSystem.update(); // technically not necessary for rendering
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();
		modelBatch.render(particleSystem);
		modelBatch.end();
		// \particle
		super.render(delta);
	}

	/**
	 * Dispatches the SCREEN_RESIZE message to anything that's listening.
	 * Current Listeners: CelRenderer
	 * Calls super for the FpsScreen overlay.
	 * @param width
	 * @param height
	 */
	@Override
	public void resize(int width, int height) {
		super.resize(width, height);
		MessageManager.getInstance().dispatchMessage(0, Messages.SCREEN_RESIZE, new Vector2(width, height));
	}

	/**
	 * Setup the models for the scene and add the entity systems that rely on actors.
	 * The render system can be changed here, ModelBatch pass is the current.
	 */
	@Override
	public void doneLoading() {
		super.doneLoading();
		setupScene();
		setupActors();
		engine.addSystem(new TurnSystem());
		modelBatchRenderer = new ModelBatchRenderer(modelBatch, camera, environment);
		engine.addSystem(modelBatchRenderer);
		modelBatchRenderer.setProcessing(true);
		Entity p = engine.getEntitiesFor(Family.all(PlayerComponent.class).get()).get(0);
		ModelInstance i = Mappers.MODEL_INSTANCE.get(p).instance();
		// particle
		ParticleEffect originalEffect = assetManager.get("effects/blast.red.pfx", ParticleEffect.class);
		// we cannot use the originalEffect, we must make a copy each time we create new particle effect
		effect = originalEffect.copy();
		effect.translate(i.getNode("emit.root").translation);
		effect.init();
//		effect.start();  // optional: particle will begin playing immediately
		particleSystem.add(effect);
		// particle
	}

	public void setupActors() {
		ModelInstance instance = new ModelInstance(assetManager.get("models/mask.ghost.red.g3dj", Model.class));
		Entity player = new Entity()
			.add(new PositionComponent(new Vector3(1, 0, 1)))
			.add(new DirectionComponent(new Vector3(0, -1, 0)))
			.add(new RotationComponent(new Quaternion(new Vector3(0, -1, 0), 180).nor()))
			.add(new ModelInstanceComponent(instance))
			.add(new InitiativeComponent(MathUtils.random(10)))
			.add(new PlayerComponent());
//			lookAt(Mappers.POSITION.get(player).position(), new Vector3(1, 0, 3).nor(), Mappers.ROTATION.get(player).rotation());
//		Mappers.MODEL_INSTANCE.get(player).instance().transform.setToLookAt(new Vector3(20, 0, 20), new Vector3(0,1,0));
		// @TODO move this to an entity system.
		if (Mappers.MODEL_INSTANCE.get(player).instance().getAnimation("skeleton|rest") != null)
			Mappers.MODEL_INSTANCE.get(player).controller().setAnimation("skeleton|rest", -1);

		engine.addEntity(player);


		// create and position the mobs spheres/Actors
		for (int i = -1; i <= 1; i += 2) {
			for (int j = -1; j <= 1; j += 2) {
				ModelInstance mi = new ModelInstance(assetManager.get("models/mask.ghost.white.g3dj", Model.class));
				Entity mob = new Entity()
						.add(new PositionComponent(new Vector3(i * 20, 0, j * 20)))
						.add(new RotationComponent(new Quaternion(new Vector3(0, -1, 0), 0)))
						.add(new DirectionComponent(new Vector3(0, -1, 0)))
						.add(new ModelInstanceComponent(mi))
						.add(new InitiativeComponent(MathUtils.random(10)))
						.add(new AiComponent());
				engine.addEntity(mob);
			// @TODO move this to an entity system.
			if (Mappers.MODEL_INSTANCE.get(mob).instance().getAnimation("skeleton|rest") != null)
				Mappers.MODEL_INSTANCE.get(mob).controller().setAnimation("skeleton|rest", -1);
			}
		}
	}
	public void lookAt(Vector3 origin, Vector3 target, Quaternion rotation) {
		Vector3 up = new Vector3(0, 1, 0);
		origin = origin.cpy().nor();
		target = target.cpy().nor();
		float dot = origin.dot(target);
		if (Math.abs(dot + 1) < 0.000000001f) {
			rotation.set(up.scl(-1), 180);
			return;
		}
		if (Math.abs(dot - 1) < 0.000000001f) {
			rotation.set(up, 180);
			return;
		}

		float rotAngle = (float) Math.acos(dot);
		Vector3 rotAxis = new Vector3(origin).crs(target).nor();

		rotation.setFromAxisRad(rotAxis, rotAngle);
	}
	public void face(Vector3 origin, Vector3 target, Vector3 direction, Quaternion rotation) {
		Vector3 tmpVec = new Vector3();
		Vector3 up = new Vector3(0, 1, 0);
		origin = origin.cpy().nor();
		target = target.cpy().nor();
//		float dot = origin.dot(target);

		tmpVec.set(target).sub(origin).nor();
		if (!tmpVec.isZero()) {
			float dot = tmpVec.dot(up); // up and direction must ALWAYS be orthonormal vectors
			if (Math.abs(dot - 1) < 0.000000001f) {
				// Collinear
				up.set(direction).scl(-1);
			} else if (Math.abs(dot + 1) < 0.000000001f) {
				// Collinear opposite
				up.set(direction);
			}
			direction.set(tmpVec);
//			normalizeUp();
		}
	}
	public void setupScene() {
		Entity p = new Entity()
				.add(new ModelInstanceComponent( new ModelInstance(assetManager.get("models/plane.g3dj", Model.class))))
				.add(new RotationComponent(new Quaternion()))
				.add(new PositionComponent(new Vector3(0.0f, 0.0f, 0.0f)));

		engine.addEntity(p);
	}

	@Override
	public void dispose() {
		super.dispose();
		assetManager.dispose();
		modelBatchRenderer.dispose();
	}
}
