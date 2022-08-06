/*******************************************************************************
 * Copyright 2011 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.crashinvaders.common.scene2d;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.*;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.*;
import com.kotcrab.vis.ui.FocusManager;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.adapter.ArrayAdapter;
import com.kotcrab.vis.ui.widget.ListView;
import com.kotcrab.vis.ui.widget.ListViewStyle;
import com.kotcrab.vis.ui.widget.VisScrollPane;

import static com.badlogic.gdx.scenes.scene2d.actions.Actions.*;

/** This is a *poorly* patched version of {@link com.badlogic.gdx.scenes.scene2d.ui.SelectBox}
 * to allow items to be any kind of widgets (the original SelectBox allows only textual items).
 * The drop-down view is implemented with VisUI's {@link ListView} and quite flexible.
 * Item views should be customized through a provided {@link ViewProducer}.
 * @see SelectBox
 * @author mzechner
 * @author Nathan Sweet
 * @author metaphore */
public class CustomSelectBox<T, V extends Actor> extends WidgetGroup implements Disableable {
	static final Vector2 temp = new Vector2();

	private Style style;
	private final Array<T> items = new Array<>();
	private final SelectBoxItemList<T, V> itemList;
	private float prefWidth, prefHeight;
	private ClickListener clickListener;
	boolean disabled;
	boolean selectedPrefWidth;

	private final ViewProducer<T, V> viewProducer;

	private V selectedView = null;
	private T selectedItem = null;

	private boolean programmaticChangeEvents = true;

	public CustomSelectBox(ViewProducer<T, V> viewProducer) {
		this(viewProducer, VisUI.getSkin());
	}

	public CustomSelectBox(ViewProducer<T, V> viewProducer, Skin skin) {
		this(viewProducer, skin.get(Style.class));
	}

	public CustomSelectBox(ViewProducer<T, V> viewProducer, String styleName) {
		this(viewProducer, VisUI.getSkin().get(styleName, Style.class));
	}

	public CustomSelectBox(ViewProducer<T, V> viewProducer, Skin skin, String styleName) {
		this(viewProducer, skin.get(styleName, Style.class));
	}

	public CustomSelectBox(ViewProducer<T, V> viewProducer, Style style) {
		setStyle(style);
		setSize(getPrefWidth(), getPrefHeight());

		this.viewProducer = viewProducer;
		this.itemList = new SelectBoxItemList<>(this, viewProducer);

		addListener(clickListener = new ClickListener() {
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				if (pointer == 0 && button != 0) return false;
				if (isDisabled()) return false;
				if (itemList.hasParent())
					hideScrollPane();
				else
					showScrollPane();
				return true;
			}
		});

		// VisUI focus functionality.
		addListener(new InputListener() {
			@Override
			public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
				FocusManager.resetFocus(getStage());
				return false;
			}
		});
	}

	protected void setStage (Stage stage) {
		if (stage == null) itemList.hide();
		super.setStage(stage);
	}

	public void setStyle (Style style) {
		if (style == null) throw new IllegalArgumentException("style cannot be null.");
		this.style = style;

		if (itemList != null) {
			itemList.listController.getScrollPane().setStyle(style.listViewStyle.scrollPaneStyle);
		}
		invalidateHierarchy();
	}

	/** Returns the select box's style. Modifying the returned style may not have an effect until {@link #setStyle(Style)}
	 * is called. */
	public Style getStyle () {
		return style;
	}

	/** Set the backing Array that makes up the choices available in the SelectBox */
	public void setItems (T... newItems) {
		if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");
		float oldPrefWidth = getPrefWidth();

		items.clear();
		items.addAll(newItems);
		validateSelectedItem();
		itemList.listAdapter.clear();
		itemList.listAdapter.addAll(items);

		invalidate();
		if (oldPrefWidth != getPrefWidth()) invalidateHierarchy();
	}

	/** Sets the items visible in the select box. */
	public void setItems (Array<T> newItems) {
		if (newItems == null) throw new IllegalArgumentException("newItems cannot be null.");
		float oldPrefWidth = getPrefWidth();

		if (newItems != items) {
			items.clear();
			items.addAll(newItems);
		}
		validateSelectedItem();
		itemList.listAdapter.clear();
		itemList.listAdapter.addAll(items);

		invalidate();
		if (oldPrefWidth != getPrefWidth()) invalidateHierarchy();
	}

	public void clearItems () {
		if (items.size == 0) return;
		items.clear();
		setSelected(null);
		itemList.listAdapter.clear();
		invalidateHierarchy();
	}

	/** Returns the internal items array. If modified, {@link #setItems(Array)} must be called to reflect the changes. */
	public Array<T> getItems () {
		return items;
	}

	public void setProgrammaticChangeEvents (boolean programmaticChangeEvents) {
		this.programmaticChangeEvents = programmaticChangeEvents;
	}

	public void layout () {
		Drawable bg = style.background;

		@Null T selectedItem = this.selectedItem;
		@Null V selectedView = this.selectedView;

		if (bg != null) {
			prefHeight = Math.max(bg.getTopHeight() + bg.getBottomHeight(), bg.getMinHeight());
		} else if (selectedView != null) {
			if (selectedItem instanceof Layout) {
				prefHeight = ((Layout)selectedView).getPrefHeight();
			} else {
				prefHeight = selectedView.getHeight();
			}
		} else {
			prefHeight = 24;
		}

		if (selectedPrefWidth) {
			prefWidth = 0;
			if (bg != null) prefWidth = bg.getLeftWidth() + bg.getRightWidth();
			if (selectedView != null) {
				if (selectedItem instanceof Layout) {
					prefWidth = ((Layout)selectedView).getPrefWidth();
				} else {
					prefWidth = selectedView.getWidth();
				}
			}
		} else {
			float maxItemWidth = 0;
			for (int i = 0; i < items.size; i++) {
				V itemView = itemList.getView(items.get(i));
				float prefWidth = itemView.getWidth();
				if (itemView instanceof Layout) {
					prefWidth = ((Layout)itemView).getPrefWidth();
				}
				maxItemWidth = Math.max(prefWidth, maxItemWidth);
			}

			prefWidth = maxItemWidth;
			if (bg != null) prefWidth = Math.max(prefWidth + bg.getLeftWidth() + bg.getRightWidth(), bg.getMinWidth());
		}

		if (selectedView != null) {
			selectedView.setPosition(0f, 0f, Align.bottomLeft);

			Drawable background = getBackgroundDrawable();
			float width = prefWidth;
			float height = prefHeight;
			float x = 0f;
			float y = 0f;
			if (background != null) {
				width -= background.getLeftWidth() + background.getRightWidth();
				height -= background.getBottomHeight() + background.getTopHeight();
				x += background.getLeftWidth();
				y += (int)(height / 2f + background.getBottomHeight());
			} else {
				y += (int)(height / 2f);
			}

			selectedView.setWidth(width);
			selectedView.setHeight(height);
			selectedView.setPosition(x, y, Align.left);
		}
	}

	/** Returns appropriate background drawable from the style based on the current select box state. */
	protected @Null Drawable getBackgroundDrawable () {
		if (isDisabled() && style.backgroundDisabled != null) return style.backgroundDisabled;
		if (itemList.hasParent() && style.backgroundOpen != null) return style.backgroundOpen;
		if (isOver() && style.backgroundOver != null) return style.backgroundOver;
		return style.background;
	}

	public void draw (Batch batch, float parentAlpha) {
		validate();

		Drawable background = getBackgroundDrawable();

		Color color = getColor();
		float x = getX(), y = getY();
		float width = getWidth(), height = getHeight();

		batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);
		if (background != null) background.draw(batch, x, y, width, height);

		super.draw(batch, parentAlpha);
	}

	/** Returns the selected item, or null. */
	public @Null T getSelected () {
		return selectedItem;
	}

	/** Sets the selection to the item. */
	public void setSelected(@Null T item) {
		if (this.selectedItem == item)
			return;

		if (item != null && !items.contains(item, true)) {
			throw new IllegalArgumentException("Item must be added to the select box item collection prior selection.");
		}

		if (this.selectedItem != null) {
			removeActor(this.selectedView);
			this.selectedView = null;
			this.selectedItem = null;
		}

		if (item != null) {
			this.selectedView = viewProducer.createView(item);
			addActor(this.selectedView);
		}

		this.selectedItem = item;

		invalidateHierarchy();

		// Fire change event.
		if (programmaticChangeEvents) {
			ChangeEvent changeEvent = Pools.obtain(ChangeEvent.class);
			try {
				this.fire(changeEvent);
			} finally {
				Pools.free(changeEvent);
			}
		}
	}

	/** @return The index of the first selected item. The top item has an index of 0. Nothing selected has an index of -1. */
	public int getSelectedIndex () {
		return selectedItem == null ? -1 : items.indexOf(selectedItem, false);
	}

	/** Sets the selection to only the selected index. */
	public void setSelectedIndex (int index) {
		setSelected(items.get(index));
	}

	/** When true the pref width is based on the selected item. */
	public void setSelectedPrefWidth (boolean selectedPrefWidth) {
		this.selectedPrefWidth = selectedPrefWidth;
	}

	public boolean getSelectedPrefWidth () {
		return selectedPrefWidth;
	}

	public void setDisabled (boolean disabled) {
		if (disabled && !this.disabled) hideScrollPane();
		this.disabled = disabled;
	}

	public boolean isDisabled () {
		return disabled;
	}

	public float getPrefWidth () {
		validate();
		return prefWidth;
	}

	public float getPrefHeight () {
		validate();
		return prefHeight;
	}

	/** @deprecated Use {@link #showScrollPane()}. */
	@Deprecated
	public void showList () {
		showScrollPane();
	}

	public void showScrollPane () {
		if (items.size == 0) return;
		if (getStage() != null) itemList.show(getStage());
	}

	/** @deprecated Use {@link #hideScrollPane()}. */
	@Deprecated
	public void hideList () {
		hideScrollPane();
	}

	public void hideScrollPane () {
		itemList.hide();
	}

	/** Returns the list shown when the select box is open. */
	public ListView<T> getListView() {
		return itemList.listController;
	}

	/** Disables scrolling of the list shown when the select box is open. */
	public void setScrollingDisabled (boolean y) {
		itemList.getList().getScrollPane().setScrollingDisabled(true, y);
		invalidateHierarchy();
	}

	/** Returns the scroll pane containing the list that is shown when the select box is open. */
	public SelectBoxItemList getItemList() {
		return itemList;
	}

	public boolean isOver () {
		return clickListener.isOver();
	}

	public ClickListener getClickListener () {
		return clickListener;
	}

	protected void onShow (Actor scrollPane, boolean below) {
		scrollPane.getColor().a = 0;
		scrollPane.addAction(fadeIn(0.3f, Interpolation.fade));
	}

	protected void onHide (Actor scrollPane) {
		scrollPane.getColor().a = 1;
		scrollPane.addAction(sequence(fadeOut(0.15f, Interpolation.fade), Actions.removeActor()));
	}

	private void validateSelectedItem() {
		// Check if the selected item is still present amongst the all items.
		if (selectedItem != null && !items.contains(selectedItem, true)) {
			setSelected(null);
		}

		// If there are items available and no current selection, make sure to select the first item.
		if (selectedItem == null && items.size > 0) {
			setSelected(items.first());
		}
	}

	/** The item list pane shown when a select box is open.
	 * @author Nathan Sweet
	 * @author metaphore */
	public static class SelectBoxItemList<T, V extends Actor> extends Container<Table> {
		final CustomSelectBox<T, V> selectBox;
		int maxListCount;
		private final Vector2 stagePosition = new Vector2();
		final ListView<T> listController;
		final Table listMainTable;
		final ListItemAdapter<T, V> listAdapter;
		private final ArrayAdapter.ListSelection<T, V> listSelection;
		private InputListener hideListener;
		private Actor previousScrollFocus;

		public SelectBoxItemList(final CustomSelectBox<T, V> selectBox, ViewProducer<T, V> viewProducer) {
//			super(null, selectBox.style.scrollStyle);
			this.selectBox = selectBox;

			this.listAdapter = new ListItemAdapter<>(viewProducer);
			listAdapter.setSelectionMode(ArrayAdapter.SelectionMode.SINGLE);

			this.listSelection = listAdapter.getSelectionManager();

			listController = new ListView<>(listAdapter, selectBox.style.listViewStyle);
			listMainTable = listController.getMainTable();
			listMainTable.setTouchable(Touchable.disabled);

			VisScrollPane scrollPane = listController.getScrollPane();
			scrollPane.setOverscroll(false, false);
			scrollPane.setFadeScrollBars(false);
			scrollPane.setScrollingDisabled(true, false);

			this.setActor(listMainTable);
			this.fill();
			this.prefWidth(new Value() {
				@Override
				public float get(Actor context) {
					return listMainTable.getPrefWidth();
				}
			});
			this.prefHeight(new Value() {
				@Override
				public float get(Actor context) {
					return listMainTable.getMinHeight();
				}
			});

			listController.setItemClickListener((T selected) -> {
				selectBox.setSelected(selected);
				hide();
			});

			listMainTable.addListener(new ClickListener() {
				private final Vector2 tmpVec2 = new Vector2();

				// Change visual selection on mouse hover.
				public boolean mouseMoved (InputEvent event, float x, float y) {
					int index = -1;
					for (int i = 0; i < listAdapter.size(); i++) {
						T item = listAdapter.get(i);
						V view = listAdapter.getView(item);

						Vector2 localCoord = listMainTable.localToActorCoordinates(view, tmpVec2.set(x, y));

						Actor hit = view.hit(localCoord.x, localCoord.y, false);
						if (hit != null) {
							index = i;
							break;
						}
					}

					if (index != -1) listSelection.select(listAdapter.get(index));

					return true;
				}
			});

			addListener(new InputListener() {
				public void exit (InputEvent event, float x, float y, int pointer, @Null Actor toActor) {
					if (toActor == null || !isAscendantOf(toActor)) {
						listSelection.select(selectBox.getSelected());
					}
				}
			});

			hideListener = new InputListener() {
				public boolean touchDown (InputEvent event, float x, float y, int pointer, int button) {
					Actor target = event.getTarget();
					if (isAscendantOf(target)) return false;
					listSelection.select(selectBox.getSelected());
					hide();
					return false;
				}

				public boolean keyDown (InputEvent event, int keycode) {
					switch (keycode) {
					case Keys.NUMPAD_ENTER:
					case Keys.ENTER:
						selectBox.setSelected(selectBox.getSelected());
						// Fall thru.
					case Keys.ESCAPE:
						hide();
						event.stop();
						return true;
					}
					return false;
				}
			};
		}

		public void show (Stage stage) {
			if (listMainTable.isTouchable()) return;

			stage.addActor(this);
			stage.addCaptureListener(hideListener);
			//TODO AC: Implement list view key listener.
//			stage.addListener(listController.getKeyListener());

			selectBox.localToStageCoordinates(stagePosition.set(0, 0));

			listMainTable.pack();
			listMainTable.setWidth(selectBox.getWidth());
			float height = listMainTable.getPrefHeight();

			float heightBelow = stagePosition.y;
			float heightAbove = stage.getHeight() - heightBelow - selectBox.getHeight();
			boolean below = true;
			if (height > heightBelow) {
				if (heightAbove > heightBelow) {
					below = false;
					height = Math.min(height, heightAbove);
				} else
					height = heightBelow;
			}

			if (below)
				setY(stagePosition.y - height);
			else
				setY(stagePosition.y + selectBox.getHeight());
			setX(stagePosition.x);
			setHeight(height);
			validate();
			float width = Math.max(getPrefWidth(), selectBox.getWidth());
			setWidth(width);

			validate();
			//TODO AC: Implement scrolling to the currently selected element.
//			scrollTo(0, listController.getHeight() - selectBox.getSelectedIndex() * itemHeight - itemHeight / 2, 0, 0, true, true);
//			updateVisualScroll();

			previousScrollFocus = null;
			Actor actor = stage.getScrollFocus();
			if (actor != null && !actor.isDescendantOf(this)) previousScrollFocus = actor;
			stage.setScrollFocus(this);

			listSelection.select(selectBox.getSelected());
			listMainTable.setTouchable(Touchable.enabled);
			clearActions();
			selectBox.onShow(this, below);
		}

		public void hide () {
			if (!listMainTable.isTouchable() || !hasParent()) return;
			listMainTable.setTouchable(Touchable.disabled);

			Stage stage = getStage();
			if (stage != null) {
				stage.removeCaptureListener(hideListener);
				//TODO AC: Remove the list view key listener.
//				stage.removeListener(listController.getKeyListener());
				if (previousScrollFocus != null && previousScrollFocus.getStage() == null) previousScrollFocus = null;
				Actor actor = stage.getScrollFocus();
				if (actor == null || isAscendantOf(actor)) stage.setScrollFocus(previousScrollFocus);
			}

			clearActions();
			selectBox.onHide(this);
		}

		public void draw (Batch batch, float parentAlpha) {
			selectBox.localToStageCoordinates(temp.set(0, 0));
			if (!temp.equals(stagePosition)) hide();
			super.draw(batch, parentAlpha);
		}

		public void act (float delta) {
			super.act(delta);
			toFront();
		}

		protected void setStage (Stage stage) {
			Stage oldStage = getStage();
			if (oldStage != null) {
				oldStage.removeCaptureListener(hideListener);
				//TODO AC: Remove the list view key listener.
//				oldStage.removeListener(listController.getKeyListener());
			}
			super.setStage(stage);
		}

		public ListView<T> getList () {
			return listController;
		}

		public CustomSelectBox<T, V> getSelectBox () {
			return selectBox;
		}

		public V getView(T item) {
			return listAdapter.getView(item);
		}
	}

	public interface ViewProducer<T, V extends Actor> {
		V createView(T item);
		void updateView(V view, T item);
		void selectView(V view);
		void deselectView(V view);
	}

	private static class ListItemAdapter<T, V extends Actor> extends ArrayAdapter<T, V> {

		private final ViewProducer<T, V> viewProducer;

		public ListItemAdapter(ViewProducer<T, V> viewProducer) {
			super(new Array<>());
			this.viewProducer = viewProducer;
		}

		@Override
		protected V createView(T item) {
			return viewProducer.createView(item);
		}

		@Override
		protected void updateView(V view, T item) {
			viewProducer.updateView(view, item);
		}

		@Override
		protected void selectView(V view) {
			viewProducer.selectView(view);
		}

		@Override
		protected void deselectView(V view) {
			viewProducer.deselectView(view);
		}
	}

	/** The style for a custom select box, see {@link CustomSelectBox}.
	 * @author mzechner
	 * @author Nathan Sweet
	 * @author metaphore */
	public static class Style {
		public @Null Drawable background;
		public @Null Drawable backgroundOver, backgroundOpen, backgroundDisabled;
		public ListViewStyle listViewStyle;

		public Style() {
		}

		public Style(@Null Drawable background, ListViewStyle listViewStyle) {
			this.background = background;
			this.listViewStyle = listViewStyle;
		}

		public Style(Style style) {
			background = style.background;
			backgroundOver = style.backgroundOver;
			backgroundOpen = style.backgroundOpen;
			backgroundDisabled = style.backgroundDisabled;
			listViewStyle = new ListViewStyle(style.listViewStyle);
		}
	}
}
