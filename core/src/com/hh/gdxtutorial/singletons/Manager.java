package com.hh.gdxtutorial.singletons;

import aurelienribon.tweenengine.TweenManager;

/**
 * Singleton that lazy loads various classes that can be used globally.
 */
public class Manager {
	private static Manager instance = new Manager();
	private TweenManager tweenManager;

	private Manager() {}

	public static Manager getInstance() {
		return instance;
	}

	public void update(float deltaTime) {
		if (tweenManager != null) tweenManager.update(deltaTime);
	}

	/**
	 * Access or instantiate and access tweenManager
	 * @return
	 */
	public TweenManager tweenManager() {
		if (tweenManager == null) tweenManager = new TweenManager();
		return tweenManager;
	}
}
