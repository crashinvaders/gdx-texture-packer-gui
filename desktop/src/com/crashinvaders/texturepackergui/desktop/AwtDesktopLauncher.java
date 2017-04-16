package com.crashinvaders.texturepackergui.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglCanvas;
import com.crashinvaders.common.awt.ImageTools;
import com.crashinvaders.texturepackergui.App;
import com.crashinvaders.texturepackergui.AppParams;
import com.crashinvaders.texturepackergui.DragDropManager;
import com.github.czyzby.autumn.fcs.scanner.DesktopClassScanner;
import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.List;

public class AwtDesktopLauncher {
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				Arguments arguments = new Arguments();
				try {
					CmdLineParser parser = new CmdLineParser(arguments);
					parser.parseArgument(args);
				} catch (CmdLineException e) {
					System.out.println("Error: " + e.getLocalizedMessage());
					return;
				}

				final LwjglApplicationConfiguration configuration = new LwjglApplicationConfiguration();
				configuration.title = "LibGDX Texture Packer GUI";
				configuration.addIcon("icon128.png", Files.FileType.Internal);
				configuration.addIcon("icon32.png", Files.FileType.Internal);
				configuration.addIcon("icon16.png", Files.FileType.Internal);
				configuration.preferencesDirectory = ".gdxtexturepackergui";
				configuration.width = 1024;
				configuration.height = 600;

				AppParams appParams = new AppParams();
				appParams.startupProject = arguments.project;

				App app = new App(new DesktopClassScanner(), appParams);
//				LwjglCanvas canvas = new LwjglCanvas(new WindowParamsPersistingApplicationWrapper(app, configuration), configuration);
				LwjglCanvas canvas = new LwjglCanvas(app, configuration);

				new MainFrame(app, canvas.getCanvas());
			}
		});
	}

	private static class MainFrame extends JFrame {
		private static final Color colorFill = new Color(37, 37, 38);

		private final JLayeredPane pane;

		public MainFrame(App app, Canvas canvas) {
			super("LibGDX Texture Packer GUI");

			setIconImage(ImageTools.loadImage("icon128.png"));

			addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					System.out.println("MainFrame.windowClosed");
					System.exit(0);
				}
			});

			// Frame layout
			{
				setSize(1024, 768);
				setLocation(0, 0);
//				setExtendedState(JFrame.MAXIMIZED_BOTH);
				setLocationRelativeTo(null);
				setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
				setVisible(true);
				setBackground(colorFill);

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
			private final DragOverRunnable dragOverRunnable;

			private boolean dragHandling;

			public FileDropTarget(DragDropManager dragDropManager) throws HeadlessException {
				this.dragDropManager = dragDropManager;
				this.dragOverRunnable = new DragOverRunnable(dragDropManager);
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
                    final List<File> droppedFiles = (List<File>) evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
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

	private static class Arguments {
		@Argument
		File project;
	}
}