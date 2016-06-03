package com.hh.gdxtutorial;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.ai.msg.Telegram;
import com.badlogic.gdx.ai.msg.Telegraph;
import com.hh.gdxtutorial.screens.*;

public class Tutorial extends Game implements Telegraph {

	@Override
	public void create () {
		MessageManager.getInstance().addListener(this, 0x02);
		setScreen(new MainMenuScreen());
	}

	@Override
	public boolean handleMessage(Telegram msg) {
		if (CelShaderScreen.class == msg.extraInfo) {
			setScreen(new CelShaderScreen());
			return true;
		} else if (GaussianBlurShaderScreen.class == msg.extraInfo) {
			setScreen(new GaussianBlurShaderScreen());
			return true;
		} else if (TiltShiftShaderScreen.class == msg.extraInfo) {
			setScreen(new TiltShiftShaderScreen());
			return true;
		} else if (TurnEngineScreen.class == msg.extraInfo) {
			setScreen(new TurnEngineScreen());
			return true;
		} else {
			return false;
		}
	}
}
