package com.hh.gdxtutorial;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.hh.gdxtutorial.ai.Messages;
import com.hh.gdxtutorial.screens.*;

public class Tutorial extends Game implements Telegraph {

	@Override
	public void create () {
		MessageManager.getInstance().addListener(this, Messages.CHANGE_SCREEN);
		setScreen(new MainMenuScreen());
		setScreen(new CombatScreen());
//		setScreen(new SscceScreen());
	}

	/**
	 * Handles one type of incoming message that sets the screen.
	 * @param msg
	 * @return
	 */
	@Override
	public boolean handleMessage(Telegram msg) {
		if (!(msg.extraInfo instanceof Screen)) {
			// *TODO Throw error. Or at least log warming.
			return false;
		}
		setScreen((Screen) msg.extraInfo);
		return true;
	}
}
