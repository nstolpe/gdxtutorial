package com.hh.gdxtutorial.singletons;

import aurelienribon.tweenengine.TweenManager;

/**
 * Created by nils on 6/24/16.
 */
public class Manager {
	private static Manager instance = new Manager();
	public TweenManager tweenManager = new TweenManager();

	private Manager() {}

	public static Manager getInstance() {
		return instance;
	}
}
