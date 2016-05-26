package com.hh.gdxtutorial;

import com.badlogic.gdx.Game;
import com.hh.gdxtutorial.screens.CelShaderScreen;
import com.hh.gdxtutorial.screens.GaussianBlurShaderScreen;

public class Tutorial extends Game {

	@Override
	public void create () {
//		setScreen(new CelShaderScreen());
		setScreen(new GaussianBlurShaderScreen());
//		setScreen(new TiltShiftShaderScreen());
	}

}
