package com.crashinvaders.texturepackergui.desktop.launchers.awt;

import com.badlogic.gdx.Gdx;
import com.crashinvaders.common.awt.ImageTools;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.DragDropManager;

import javax.swing.*;
import java.awt.*;
import java.awt.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.*;

class MainFrame extends JFrame {
    private static final Color colorFill = new Color(37, 37, 38);

    private final JLayeredPane pane;

    public MainFrame(App app, Canvas canvas, LwjglCanvasConfiguration config) {
        super(config.title);

        setIconImage(ImageTools.loadImage(config.iconFilePath));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                FramePropertiesPersister.saveFrameProperties(MainFrame.this);
                System.exit(0);
            }
        });

        // Frame layout
        {
            setBackground(colorFill);
            setSize(1024, 768);
            setLocation(0, 0);
//            setExtendedState(JFrame.MAXIMIZED_BOTH);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

            FramePropertiesPersister.loadFrameProperties(this);

            setVisible(true);

            pane = new JLayeredPane();
            pane.setBackground(colorFill);
            pane.setLayout(new BorderLayout());
            pane.add(canvas, BorderLayout.CENTER, 0);

            getContentPane().add(pane, BorderLayout.CENTER);

            pane.setTransferHandler(new TransferHandler(null));
            pane.setDropTarget(new FileDropTarget(app.getDragDropManager()));
        }
    }

    private static class FileDropTarget extends DropTarget {
        private final DragDropManager dragDropManager;
        private final FileDropTarget.DragOverRunnable dragOverRunnable;

        private boolean dragHandling;

        public FileDropTarget(DragDropManager dragDropManager) throws HeadlessException {
            this.dragDropManager = dragDropManager;
            this.dragOverRunnable = new FileDropTarget.DragOverRunnable(dragDropManager);
        }

        @Override
public synchronized void dragEnter(DropTargetDragEvent evt) {
super.dragEnter(evt);

if (!evt.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) { return; }

dragHandling = true;

            final int x = evt.getLocation().x;
            final int y = evt.getLocation().y;
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    dragDropManager.onDragStarted(x, y);
                }
            });
}

        @Override
public synchronized void dragOver(DropTargetDragEvent evt) {
super.dragOver(evt);

if (!dragHandling) return;

            dragOverRunnable.x = evt.getLocation().x;
            dragOverRunnable.y = evt.getLocation().y;
            if (!dragOverRunnable.scheduled) {
                dragOverRunnable.scheduled = true;
                Gdx.app.postRunnable(dragOverRunnable);
            }
}

        @Override
public synchronized void dragExit(DropTargetEvent evt) {
super.dragExit(evt);

            if (!dragHandling) return;

            finishDragHandling();
}

        public synchronized void drop(DropTargetDropEvent evt) {
            if (!dragHandling) return;

            finishDragHandling();

try {
evt.acceptDrop(DnDConstants.ACTION_COPY);
final java.util.List<File> droppedFiles = (java.util.List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                final int x = evt.getLocation().x;
                final int y = evt.getLocation().y;
                Gdx.app.postRunnable(new Runnable() {
                    @Override public void run() {
                        dragDropManager.handleFileDrop(x, y, droppedFiles);
                    }
                });
} catch (Exception ex) {
ex.printStackTrace();
}
}

private void finishDragHandling() {
            if (!dragHandling) return;

            dragHandling = false;
            Gdx.app.postRunnable(new Runnable() {
                @Override public void run() {
                    dragDropManager.onDragFinished();
                }
            });
        }

private static class DragOverRunnable implements Runnable {
            final DragDropManager dragDropManager;
            int x;
            int y;
            boolean scheduled;

            public DragOverRunnable(DragDropManager dragDropManager) {
                this.dragDropManager = dragDropManager;
            }

            @Override
            public void run() {
                dragDropManager.onDragMoved(x, y);
                scheduled = false;
            }
        }
    }
}
