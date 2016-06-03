package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Array;

public abstract class FpsScreen extends AbstractScreen {
	protected boolean loading = true;
	protected Color clear = new Color(0.0f, 0.0f, 0.0f, 1.0f);

	protected Stage stage;
	protected BitmapFont font;
	protected Label label;
	protected StringBuilder stringBuilder;

	@Override
	public void show() {
		stage = new Stage();
		font = new BitmapFont();
		label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
		stage.addActor(label);
		stringBuilder = new StringBuilder();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void render(float delta) {
		stringBuilder.setLength(0);
		stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
		label.setText(stringBuilder);
		stage.draw();
	}

	@Override
	public void hide() {
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		font.dispose();
		stage.dispose();
	}

	public void doneLoading() {
		loading = false;
	}
}
