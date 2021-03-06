package com.hh.ghostengine.entity.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.particles.ParticleSystem;
import com.badlogic.gdx.graphics.g3d.particles.batches.BillboardParticleBatch;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Disposable;
import com.hh.ghostengine.ai.Messages;
import com.hh.ghostengine.entity.components.EffectsComponent;
import com.hh.ghostengine.entity.components.Mappers;
import com.hh.ghostengine.entity.components.ModelInstanceComponent;
import com.hh.ghostengine.entity.components.PositionComponent;

/**
 * An entity system that renders all entities with a PositionComponent and ModelInstanceComponent.
 */
public class ModelBatchRenderer extends EntitySystem implements Disposable, Telegraph {
	protected ImmutableArray<Entity> entities;
	protected ModelBatch modelBatch;
	protected Family family = Family.all(PositionComponent.class, ModelInstanceComponent.class).get();

	public Color clearColor = new Color(0.0f, 0.0f, 0.0f, 1.0f);
	public Camera camera;
	public Environment env;

	public AssetManager assetManager;
	public ParticleSystem particleSystem = new ParticleSystem();
	protected Vector3 tmpVec = new Vector3();
	protected Matrix4 tmpMat;
	protected Quaternion tmpQuat = new Quaternion();


	/**
	 * An entity system for processing a modelbatch and its models. Not processing on default
	 * to avoid asset loading errors.
	 * @param modelBatch
	 * @param camera
	 * @param env
	 */
	public ModelBatchRenderer(ModelBatch modelBatch, Camera camera, Environment env) {
		super();
		this.setProcessing(false);
		this.modelBatch = modelBatch;
		this.camera = camera;
		this.env = env;
		assetManager = new AssetManager();
		MessageManager.getInstance().addListener(this, Messages.SCREEN_RESIZE);
		BillboardParticleBatch billboardParticleBatch = new BillboardParticleBatch();
		billboardParticleBatch.setCamera(camera);
		particleSystem.add(billboardParticleBatch);
	}
	/**
	 * Triggered when SCREEN_RESIZE message is received.
	 * @param dimensions
	 */
	public void resize(Vector2 dimensions) {
		camera.viewportWidth = dimensions.x;
		camera.viewportHeight = dimensions.y;
		camera.update();
	}
	/**
	 * Run the modelBatch
	 * @param deltaTime
	 */
	@Override
	public void update(float deltaTime) {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
		modelBatch.begin(camera);
		// @TODO this updating should be somewhere else, probably an entity system.
		for (Entity e : entities) {
			ModelInstance instance = Mappers.MODEL_INSTANCE.get(e).instance();
			Mappers.MODEL_INSTANCE.get(e).controller().update(deltaTime);

			instance.transform.set(Mappers.POSITION.get(e).position(), Mappers.ROTATION.get(e).rotation());

			modelBatch.render(instance, env);

			if (Mappers.EFFECTS.has(e)) {
				EffectsComponent.Effect blast = Mappers.EFFECTS.get(e).getEffect("blast");
				//@TODO make the quat and v3 members to reuse.
				tmpMat = new Matrix4(blast.position, tmpQuat, tmpVec.set(1,1,1));
				blast.effect.setTransform(tmpMat);
			}
			tmpVec.set(Vector3.Zero);
		}

		// particle systems
		particleSystem.update(); // technically not necessary for rendering
		particleSystem.begin();
		particleSystem.draw();
		particleSystem.end();
		modelBatch.render(particleSystem);
		// \particle systems

		modelBatch.end();
	}
	@Override
	public void addedToEngine(Engine engine) {
		entities = engine.getEntitiesFor(family);

		EffectsComponent.Effect blast;
		ModelInstanceComponent m;
		for (Entity e : entities) {
			m = Mappers.MODEL_INSTANCE.get(e);
			if (m.instance.getAnimation("skeleton|rest") != null) {
				m.controller().setAnimation("skeleton|rest", -1);
				blast = Mappers.EFFECTS.get(e).getEffect("blast");
				blast.effect.translate(m.instance.getNode("attach.projectile").translation);
				blast.effect.init();
				blast.emitter.setEmissionMode(RegularEmitter.EmissionMode.Disabled);
				particleSystem.add(blast.effect);
			}
		}
	}

	@Override
	public void removedFromEngine(Engine engine) {
		System.out.println("ModelBatchRenderer removed from engine");
	}
	/**
	 * Disposables are managed here.
	 */
	@Override
	public void dispose() {
		modelBatch.dispose();
		assetManager.dispose();
	}
	/**
	 * Handles incoming messages
	 * @param msg
	 * @return
	 */
	@Override
	public boolean handleMessage(Telegram msg) {
		switch (msg.message) {
			case Messages.SCREEN_RESIZE:
				resize((Vector2) msg.extraInfo);
				break;
			default:
				return false;
		}
		return true;
	}
}
