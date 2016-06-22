package com.hh.gdxtutorial.entity.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.g3d.particles.ParticleEffect;
import com.badlogic.gdx.graphics.g3d.particles.emitters.RegularEmitter;
import com.badlogic.gdx.utils.ArrayMap;

/**
 * Created by nils on 6/22/16.
 */
public class EffectsComponent implements Component {
	private ArrayMap<String, Effect> effects;

	public EffectsComponent() {

	}

	public EffectsComponent(ArrayMap<String, Effect> effects) {
		this.effects = effects;
	}

	public EffectsComponent addEffect(String key, Effect effect) {
		if (effects == null)
			effects = new ArrayMap<String, Effect>();

		effects.put(key, effect);
		return this;
	}

	public EffectsComponent dropEffect(String key) {
		effects.removeKey(key);
		return this;
	}

	public EffectsComponent dropEffect(Effect effect) {
		effects.removeValue(effect, true);
		return this;
	}

	public Effect getEffect(String key) {
		return effects.get(key);
	}

	public static class Effect {
		public String nodeKey;
		public ParticleEffect effect;
		public RegularEmitter emitter;
		public RegularEmitter.EmissionMode mode;

		public Effect() {}
		public Effect(String nodeKey, ParticleEffect effect, RegularEmitter emitter) {
			this(nodeKey, effect, emitter, RegularEmitter.EmissionMode.Disabled);
		}

		public Effect(String nodeKey, ParticleEffect effect, RegularEmitter emitter, RegularEmitter.EmissionMode mode) {
			this.nodeKey = nodeKey;
			this.effect = effect;
			this.emitter = emitter;
			this.mode = RegularEmitter.EmissionMode.Disabled;
		}
	}
}
