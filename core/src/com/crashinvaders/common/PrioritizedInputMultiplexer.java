package com.crashinvaders.common;

import com.badlogic.gdx.InputProcessor;

import java.util.Comparator;

/** Same old InputMultiplexer but ordering processors by passed priorities. */
public class PrioritizedInputMultiplexer implements InputProcessor {
    private final Comparator<Wrapper> comparator;
    private ValueArrayMap<InputProcessor, Wrapper> processors = new ValueArrayMap<>(4);

    private int maxPointers = 1;    // Single touch by default

    public PrioritizedInputMultiplexer() {
        comparator = new WrapperComparator();
    }

    public int getMaxPointers() {
        return maxPointers;
    }

    public void setMaxPointers(int maxPointers) {
        this.maxPointers = maxPointers;
    }

    public void addProcessor (InputProcessor processor, int priority) {
		if (processor == null) throw new NullPointerException("processor cannot be null");
		processors.put(processor, new Wrapper(processor, priority));
        processors.sort(comparator);
	}

	public void removeProcessor (InputProcessor processor) {
        processors.remove(processor);
	}

	/** @return the number of processors in this multiplexer */
	public int size () {
		return processors.size();
	}

	public void clear () {
		processors.clear();
	}

	@Override
    public boolean keyDown (int keycode) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.getValueAt(i).keyDown(keycode)) return true;
		return false;
	}

	@Override
	public boolean keyUp (int keycode) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.getValueAt(i).keyUp(keycode)) return true;
		return false;
	}

	@Override
	public boolean keyTyped (char character) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.getValueAt(i).keyTyped(character)) return true;
		return false;
	}

	@Override
	public boolean touchDown (int screenX, int screenY, int pointer, int button) {
        if (pointer >= maxPointers) return false;

		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.getValueAt(i).touchDown(screenX, screenY, pointer, button)) return true;
		return false;
	}

	@Override
	public boolean touchUp (int screenX, int screenY, int pointer, int button) {
        if (pointer >= maxPointers) return false;

		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.getValueAt(i).touchUp(screenX, screenY, pointer, button)) return true;
		return false;
	}

	@Override
	public boolean touchDragged (int screenX, int screenY, int pointer) {
        if (pointer >= maxPointers) return false;

		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.getValueAt(i).touchDragged(screenX, screenY, pointer)) return true;
		return false;
	}

	@Override
	public boolean mouseMoved (int screenX, int screenY) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.getValueAt(i).mouseMoved(screenX, screenY)) return true;
		return false;
	}

	@Override
	public boolean scrolled (int amount) {
		for (int i = 0, n = processors.size(); i < n; i++)
			if (processors.getValueAt(i).scrolled(amount)) return true;
		return false;
	}

    private static class Wrapper implements InputProcessor {
        private final InputProcessor processor;
        private final int priority;

        public Wrapper(InputProcessor processor, int priority) {
            this.processor = processor;
            this.priority = priority;
        }
        @Override
        public boolean keyDown(int keycode) {
            return processor.keyDown(keycode);
        }
        @Override
        public boolean keyUp(int keycode) {
            return processor.keyUp(keycode);
        }
        @Override
        public boolean keyTyped(char character) {
            return processor.keyTyped(character);
        }
        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return processor.touchDown(screenX, screenY, pointer, button);
        }
        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return processor.touchUp(screenX, screenY, pointer, button);
        }
        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return processor.touchDragged(screenX, screenY, pointer);
        }
        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return processor.mouseMoved(screenX, screenY);
        }
        @Override
        public boolean scrolled(int amount) {
            return processor.scrolled(amount);
        }

        @Override
        public String toString() {
            if (processor != null) {
                return processor.toString();
            }
            return super.toString();
        }
    }

    private static class WrapperComparator implements Comparator<Wrapper> {
        @Override
        public int compare(Wrapper l, Wrapper r) {
            return Integer.compare(l.priority, r.priority);
        }
    }
}
