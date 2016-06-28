package com.hh.gdxtutorial.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.msg.MessageManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.hh.gdxtutorial.ai.Messages;

/**
 * Extend from this class if you want to have an FPS 2d overlay. Call each implemented method
 * from the derived class via super.
 *
 * Make sure `super.render(delta)` comes at the end of the derived class's `render()` call so
 * the 2d stage is drawn on top.
 */
public abstract class FpsScreen extends AbstractScreen {
	protected Stage stage;
	protected PerspectiveCamera camera;
	protected BitmapFont font;
	protected Label label;
	protected StringBuilder stringBuilder;
	protected Button mainMenuScreenButton;
	protected TextButtonStyle buttonStyle;
	protected Table table;
	protected InputMultiplexer multiplexer;

	/**
	 * Sets up everything we need to draw the FPS overlay.
	 */
	@Override
	public void show() {
		stringBuilder = new StringBuilder();
		initCamera();
		initStage();

		multiplexer = new InputMultiplexer();
		multiplexer.addProcessor(stage);
		Gdx.input.setInputProcessor(multiplexer);
	}

	protected void initCamera() {
		camera = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
		camera.position.set(20.0f, 20.0f, 20.0f);
		camera.lookAt(0, 0, 0);
		camera.near = 1;
		camera.far = 1000;
		camera.update();
	}
	protected void initStage() {
		stage = new Stage(new ScreenViewport());
		font = new BitmapFont();
		label = new Label(" ", new Label.LabelStyle(font, Color.WHITE));
		buttonStyle = new TextButton.TextButtonStyle();
		buttonStyle.font = font;
		buttonStyle.fontColor = new Color(1.0f, 0.0f, 0.0f, 1.0f);
		mainMenuScreenButton = new TextButton("MainMenuScreen", buttonStyle);

		mainMenuScreenButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				MessageManager.getInstance().dispatchMessage(0, Messages.CHANGE_SCREEN, new MainMenuScreen());
			}
		});

		table = new Table();
		table.left().top();
		table.setFillParent(true);

		table.add(label);
		table.add(mainMenuScreenButton).expandX().right();

		stage.addActor(table);
	}

	/**
	 * Resizes the stage. Should be called from end of derived render().
	 */
	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	/**
	 * Draws the stage.
	 */
	@Override
	public void render(float delta) {
		stringBuilder.setLength(0);
		stringBuilder.append(" FPS: ").append(Gdx.graphics.getFramesPerSecond());
		label.setText(stringBuilder);
		stage.draw();
	}

	/**
	 * Get rid of our disposables.
	 */
	@Override
	public void dispose() {
		font.dispose();
		stage.dispose();
	}
}
