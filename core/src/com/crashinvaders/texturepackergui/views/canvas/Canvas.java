package com.crashinvaders.texturepackergui.views.canvas;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.crashinvaders.texturepackergui.services.model.PackModel;
import com.crashinvaders.texturepackergui.views.canvas.widgets.InfoPanel;
import com.crashinvaders.texturepackergui.views.canvas.widgets.preview.PreviewHolder;
import com.github.czyzby.lml.parser.LmlParser;
import com.github.czyzby.lml.parser.impl.tag.AbstractNonParentalActorLmlTag;
import com.github.czyzby.lml.parser.tag.LmlActorBuilder;
import com.github.czyzby.lml.parser.tag.LmlTag;
import com.github.czyzby.lml.parser.tag.LmlTagProvider;
import com.kotcrab.vis.ui.widget.VisImageTextButton;
import com.kotcrab.vis.ui.widget.VisTable;

public class Canvas extends WidgetGroup {

	private final Rectangle widgetAreaBounds = new Rectangle();
	private final Rectangle scissorBounds = new Rectangle();

	private Callback callback;

	private PreviewHolder previewHolder;
	private InfoPanel infoPanel;

	private AtlasModel atlas;
	private int pageIndex = 0;

	public Canvas(Skin skin) {
		// Layout
		{
			// Background
			{
				Image backgroundFill = new Image(skin.getTiledDrawable("custom/transparent-light"));
				backgroundFill.setFillParent(true);
				addActor(backgroundFill);
			}

			// Page preview
			{
				previewHolder = new PreviewHolder(skin);
				previewHolder.setListener(new PreviewHolder.Listener() {
					@Override
					public void onZoomChanged(int percentage) {
						infoPanel.setZoomLevel(percentage);
					}
				});

				addActor(previewHolder);
			}

			// Page buttons
			{
				VisImageTextButton btnNextPage = new VisImageTextButton("Next page", "default");
				{
					btnNextPage.align(Align.left);
					btnNextPage.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            showNextPage();
                        }
                    });
					btnNextPage.setFocusBorderEnabled(false);

					VisImageTextButton.VisImageTextButtonStyle style = btnNextPage.getStyle();
					style.imageUp = skin.getDrawable("icon-arrow-right");
					btnNextPage.setStyle(style);
				}

				VisImageTextButton btnPrevPage = new VisImageTextButton("Previous page", "default");
				{
					btnPrevPage.align(Align.left);
					btnPrevPage.addListener(new ClickListener() {
                        @Override
                        public void clicked(InputEvent event, float x, float y) {
                            showPrevPage();
                        }
                    });
					btnPrevPage.setFocusBorderEnabled(false);

					VisImageTextButton.VisImageTextButtonStyle style = btnPrevPage.getStyle();
					style.imageUp = skin.getDrawable("icon-arrow-left");
					btnPrevPage.setStyle(style);
				}



				VisTable table = new VisTable();
				table.defaults().right().fillX();
				table.add(btnNextPage);
				table.row().padTop(6f);
				table.add(btnPrevPage);

				Container container = new Container<>(table);
				container.setFillParent(true);
				container.align(Align.topRight);
				container.padTop(10f);
				addActor(container);
			}

			// Info pane
			{
				infoPanel = new InfoPanel(skin);

				Container container = new Container<>(infoPanel);
				container.setFillParent(true);
				container.align(Align.bottomLeft);
				addActor(container);
			}
		}
	}

	// Apply scissors
	@Override
	public void draw(Batch batch, float parentAlpha) {
		batch.flush();
		getStage().calculateScissors(widgetAreaBounds.set(getX(), getY(), getWidth(), getHeight()), scissorBounds);
		if (ScissorStack.pushScissors(scissorBounds)) {
			super.draw(batch, parentAlpha);
			batch.flush();
			ScissorStack.popScissors();
		}
	}

	public void reloadPack(PackModel pack) {
		String atlasPath = null;
		if (pack != null) {
			atlasPath = pack.getAtlasPath();
		}

//		// Check if atlas the same
//		if (atlas != null && atlas.getAtlasPath().equals(atlasPath)) return;

		pageIndex = 0;
		previewHolder.reset();
		infoPanel.setPagesAmount(0);
		if (atlas != null) {
			atlas.dispose();
			atlas = null;
		}

		if (atlasPath != null) {
			FileHandle packFile = Gdx.files.absolute(atlasPath);
			if (packFile != null && packFile.exists() && !packFile.isDirectory()) {
				try {
					atlas = new AtlasModel(packFile);

					previewHolder.setPage(atlas, pageIndex);
					infoPanel.setCurrentPage(pageIndex + 1);
					infoPanel.setPagesAmount(atlas.getPages().size);

				} catch (GdxRuntimeException ex) {
					if (atlas != null) {
						atlas.dispose();
						atlas = null;
					}
					callback.atlasError(pack);
				}
			}
		}
	}

	public void setCallback(Callback callback) {
		this.callback = callback;
	}

	private void showNextPage() {
		if (atlas == null || atlas.getPages().size == 0) return;

		pageIndex = pageIndex +1 >= atlas.getPages().size ? 0 : pageIndex+1;

		previewHolder.setPage(atlas, pageIndex);
		infoPanel.setCurrentPage(pageIndex +1);
	}

	private void showPrevPage() {
		if (atlas == null || atlas.getPages().size == 0) return;

		pageIndex = pageIndex -1 < 0 ? atlas.getPages().size-1 : pageIndex-1;

		previewHolder.setPage(atlas, pageIndex);
		infoPanel.setCurrentPage(pageIndex +1);
	}

	public interface Callback {
		void atlasError(PackModel pack);
	}

	public static class CanvasLmlTagProvider implements LmlTagProvider {
		@Override
		public LmlTag create(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
			return new CanvasLmlTag(parser, parentTag, rawTagData);
		}
	}

	public static class CanvasLmlTag extends AbstractNonParentalActorLmlTag {
		public CanvasLmlTag(final LmlParser parser, final LmlTag parentTag, final StringBuilder rawTagData) {
			super(parser, parentTag, rawTagData);
		}

		@Override
		protected Actor getNewInstanceOfActor(LmlActorBuilder builder) {
			return new Canvas(getSkin(builder));
		}
	}
}
