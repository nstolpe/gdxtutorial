package com.hh.gdxtutorial.singletons;

import aurelienribon.tweenengine.TweenManager;

/**
 * Created by nils on 6/24/16.
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
