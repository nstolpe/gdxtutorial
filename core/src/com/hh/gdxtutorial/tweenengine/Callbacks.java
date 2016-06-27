package com.hh.gdxtutorial.tweenengine;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.ai.msg.MessageManager;

/**
 * Created by nils on 6/27/16.
 */
public class Callbacks {
	public static TweenCallback dispatchMessageCallback(int msg) {
		final int cpy = msg;
		return new TweenCallback(){
			@Override
			public void onEvent(int type, BaseTween<?> source) {
				switch (type) {
					case COMPLETE:
						MessageManager.getInstance().dispatchMessage(cpy);
						break;
					default:
						break;
				}
			}
		};
	}
}
