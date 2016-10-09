package com.hh.ghostengine.globals;

import aurelienribon.tweenengine.TweenManager;
import com.badlogic.ashley.core.Engine;

/**
 * Singleton that lazy loads various classes that can be used globally.
 */
public class Manager {
	private static Manager instance = new Manager();
	private TweenManager tweenManager;
	private Engine engine;

	private Manager() {}

	public static Manager getInstance() {
		return instance;
	}

	public void update(float deltaTime) {
		if (tweenManager != null) tweenManager.update(deltaTime);
		if (engine != null) engine.update(deltaTime);
	}

	/**
	 * Access or instantiate and access tweenManager
	 * @return
	 */
	public TweenManager tweenManager() {
		if (tweenManager == null) tweenManager = new TweenManager();
		return tweenManager;
	}
	/**
	 * Access or instantiate and access tweenManager
	 * @return
	 */
	public Engine engine() {
		if (engine == null) engine = new Engine();
		return engine;
	}
}
