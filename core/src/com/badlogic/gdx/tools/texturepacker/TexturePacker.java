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

package com.badlogic.gdx.tools.texturepacker;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData;
import com.badlogic.gdx.graphics.g2d.TextureAtlas.TextureAtlasData.Region;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/** @author Nathan Sweet */
public class TexturePacker {
	public static final Array<Pixmap.Format> availableEncodings = Array.with(
			Pixmap.Format.RGBA8888,
			Pixmap.Format.RGB888,
			Pixmap.Format.RGBA4444,
			Pixmap.Format.RGB565,
			Pixmap.Format.Alpha
	);

	private final PageFileWriter pageFileWriter;
	private final Settings settings;
	private final Packer packer;
	private final ImageProcessor imageProcessor;
	private final Array<InputImage> inputImages = new Array();
	private File rootDir;

	/**
	 * @param rootDir Used to strip the root directory prefix from image file names, can be null.  */
	public TexturePacker(Settings settings, PageFileWriter pageFileWriter, File rootDir) {
		this.pageFileWriter = pageFileWriter;
		this.rootDir = rootDir;
		this.settings = settings;

		if (settings.pot) {
			if (settings.maxWidth != MathUtils.nextPowerOfTwo(settings.maxWidth))
				throw new RuntimeException("If pot is true, maxWidth must be a power of two: " + settings.maxWidth);
			if (settings.maxHeight != MathUtils.nextPowerOfTwo(settings.maxHeight))
				throw new RuntimeException("If pot is true, maxHeight must be a power of two: " + settings.maxHeight);
		}

		if (settings.grid)
			packer = new OrderedGridPacker(settings);
		else
			packer = new MaxRectsPacker(settings);
		imageProcessor = new ImageProcessor(rootDir, settings);
	}

	public TexturePacker(Settings settings, PageFileWriter pageFileWriter) {
		this(settings, pageFileWriter, null);
	}

	public void addImage (File file) {
		addImage(file, null);
	}

	public void addImage (File file, String name) {
		InputImage inputImage = new InputImage();
		inputImage.file = file;
		inputImage.name = name;
		inputImages.add(inputImage);
	}

	public void addImage (BufferedImage image, String name) {
		InputImage inputImage = new InputImage();
		inputImage.image = image;
		inputImage.name = name;
		inputImages.add(inputImage);
	}

	/** Predefined ninepatch */
	public void addImage (File file, String name, int[] splits, int[] pads) {
		InputImage inputImage = new InputImage();
		inputImage.file = file;
		inputImage.name = name;
		inputImage.ninePatchProps = new InputImage.NinePatchProps();
		inputImage.ninePatchProps.splits = splits;

		boolean hasPads = false;
		for (int i = 0; i < pads.length; i++) {
			if (pads[i] > 0) {
				hasPads = true;
				break;
			}
		}
		if (hasPads) {
			inputImage.ninePatchProps.pads = pads;
		}

		inputImages.add(inputImage);
	}

	public void pack (File outputDir, String packFileName) {
		if (packFileName.endsWith(settings.atlasExtension))
			packFileName = packFileName.substring(0, packFileName.length() - settings.atlasExtension.length());
		outputDir.mkdirs();

		for (int i = 0, n = settings.scale.length; i < n; i++) {
			imageProcessor.setScale(settings.scale[i]);
			for (InputImage inputImage : inputImages) {
				if (inputImage.file != null) {
					if (inputImage.ninePatchProps != null) {
						imageProcessor.addImageNinePatch(inputImage.file, inputImage.name,
								inputImage.ninePatchProps.splits, inputImage.ninePatchProps.pads);
					} else {
						imageProcessor.addImage(inputImage.file, inputImage.name);
					}
				} else {
					imageProcessor.addImage(inputImage.image, inputImage.name);
				}
			}

			Array<Page> pages = packer.pack(imageProcessor.getImages());

			String scaledPackFileName = settings.getScaledPackFileName(packFileName, i);
			writeImages(outputDir, scaledPackFileName, pages);
			try {
				writePackFile(outputDir, scaledPackFileName, pages);
			} catch (IOException ex) {
				throw new RuntimeException("Error writing pack file.", ex);
			}
			imageProcessor.clear();
		}
	}

	private void writeImages (File outputDir, String scaledPackFileName, Array<Page> pages) {
		File packFileNoExt = new File(outputDir, scaledPackFileName);
		File packDir = packFileNoExt.getParentFile();
		String imageName = packFileNoExt.getName();

		int fileIndex = 0;
		for (Page page : pages) {
			int width = page.width, height = page.height;
			int paddingX = settings.paddingX;
			int paddingY = settings.paddingY;
			if (settings.duplicatePadding) {
				paddingX /= 2;
				paddingY /= 2;
			}
			width -= settings.paddingX;
			height -= settings.paddingY;
			if (settings.edgePadding) {
				page.x = paddingX;
				page.y = paddingY;
				width += paddingX * 2;
				height += paddingY * 2;
			}
			if (settings.pot) {
				width = MathUtils.nextPowerOfTwo(width);
				height = MathUtils.nextPowerOfTwo(height);
			}
			width = Math.max(settings.minWidth, width);
			height = Math.max(settings.minHeight, height);
			page.imageWidth = width;
			page.imageHeight = height;

			File outputFile;
			while (true) {
				outputFile = new File(packDir, imageName + (fileIndex++ == 0 ? "" : fileIndex) + "." + pageFileWriter.getFileExtension());
				if (!outputFile.exists()) break;
			}
			new FileHandle(outputFile).parent().mkdirs();
			page.imageName = outputFile.getName();

			BufferedImage canvas = new BufferedImage(width, height, getBufferedImageType(settings.format));
			Graphics2D g = (Graphics2D)canvas.getGraphics();

			if (!settings.silent) System.out.println("Writing " + canvas.getWidth() + "x" + canvas.getHeight() + ": " + outputFile);

			for (Rect rect : page.outputRects) {
				BufferedImage image = rect.getImage(imageProcessor);
				int iw = image.getWidth();
				int ih = image.getHeight();
				int rectX = page.x + rect.x, rectY = page.y + page.height - rect.y - rect.height;
				if (settings.duplicatePadding) {
					int amountX = settings.paddingX / 2;
					int amountY = settings.paddingY / 2;
					if (rect.rotated) {
						// Copy corner pixels to fill corners of the padding.
						for (int i = 1; i <= amountX; i++) {
							for (int j = 1; j <= amountY; j++) {
								plot(canvas, rectX - j, rectY + iw - 1 + i, image.getRGB(0, 0));
								plot(canvas, rectX + ih - 1 + j, rectY + iw - 1 + i, image.getRGB(0, ih - 1));
								plot(canvas, rectX - j, rectY - i, image.getRGB(iw - 1, 0));
								plot(canvas, rectX + ih - 1 + j, rectY - i, image.getRGB(iw - 1, ih - 1));
							}
						}
						// Copy edge pixels into padding.
						for (int i = 1; i <= amountY; i++) {
							for (int j = 0; j < iw; j++) {
								plot(canvas, rectX - i, rectY + iw - 1 - j, image.getRGB(j, 0));
								plot(canvas, rectX + ih - 1 + i, rectY + iw - 1 - j, image.getRGB(j, ih - 1));
							}
						}
						for (int i = 1; i <= amountX; i++) {
							for (int j = 0; j < ih; j++) {
								plot(canvas, rectX + j, rectY - i, image.getRGB(iw - 1, j));
								plot(canvas, rectX + j, rectY + iw - 1 + i, image.getRGB(0, j));
							}
						}
					} else {
						// Copy corner pixels to fill corners of the padding.
						for (int i = 1; i <= amountX; i++) {
							for (int j = 1; j <= amountY; j++) {
								plot(canvas, rectX - i, rectY - j, image.getRGB(0, 0));
								plot(canvas, rectX - i, rectY + ih - 1 + j, image.getRGB(0, ih - 1));
								plot(canvas, rectX + iw - 1 + i, rectY - j, image.getRGB(iw - 1, 0));
								plot(canvas, rectX + iw - 1 + i, rectY + ih - 1 + j, image.getRGB(iw - 1, ih - 1));
							}
						}
						// Copy edge pixels into padding.
						for (int i = 1; i <= amountY; i++) {
							copy(image, 0, 0, iw, 1, canvas, rectX, rectY - i, rect.rotated);
							copy(image, 0, ih - 1, iw, 1, canvas, rectX, rectY + ih - 1 + i, rect.rotated);
						}
						for (int i = 1; i <= amountX; i++) {
							copy(image, 0, 0, 1, ih, canvas, rectX - i, rectY, rect.rotated);
							copy(image, iw - 1, 0, 1, ih, canvas, rectX + iw - 1 + i, rectY, rect.rotated);
						}
					}
				}
				copy(image, 0, 0, iw, ih, canvas, rectX, rectY, rect.rotated);
				if (settings.debug) {
					g.setColor(Color.magenta);
					g.drawRect(rectX, rectY, rect.width - settings.paddingX - 1, rect.height - settings.paddingY - 1);
				}
			}

			if (settings.bleed && !settings.premultiplyAlpha && pageFileWriter.isBleedingSupported()) {
				canvas = new ColorBleedEffect().processImage(canvas, settings.bleedIterations);
				g = (Graphics2D)canvas.getGraphics();
			}

			if (settings.debug) {
				g.setColor(Color.magenta);
				g.drawRect(0, 0, width - 1, height - 1);
			}

			try {
				pageFileWriter.saveToFile(settings, canvas, outputFile);
			} catch (IOException ex) {
				throw new RuntimeException("Error writing file: " + outputFile, ex);
			}
		}
	}

	static private void plot (BufferedImage dst, int x, int y, int argb) {
		if (0 <= x && x < dst.getWidth() && 0 <= y && y < dst.getHeight()) dst.setRGB(x, y, argb);
	}

	static private void copy (BufferedImage src, int x, int y, int w, int h, BufferedImage dst, int dx, int dy, boolean rotated) {
		if (rotated) {
			for (int i = 0; i < w; i++)
				for (int j = 0; j < h; j++)
					plot(dst, dx + j, dy + w - i - 1, src.getRGB(x + i, y + j));
		} else {
			for (int i = 0; i < w; i++)
				for (int j = 0; j < h; j++)
					plot(dst, dx + i, dy + j, src.getRGB(x + i, y + j));
		}
	}

	private void writePackFile (File outputDir, String scaledPackFileName, Array<Page> pages) throws IOException {
		File packFile = new File(outputDir, scaledPackFileName + settings.atlasExtension);
		File packDir = packFile.getParentFile();
		packDir.mkdirs();

		if (packFile.exists()) {
			// Make sure there aren't duplicate names.
			TextureAtlasData textureAtlasData = new TextureAtlasData(new FileHandle(packFile), new FileHandle(packFile), false);
			for (Page page : pages) {
				for (Rect rect : page.outputRects) {
					String rectName = Rect.getAtlasName(rect.name, settings.flattenPaths);

					for (Region region : textureAtlasData.getRegions()) {
						if (region.name.equals(rectName)) {
							throw new GdxRuntimeException(
								"A region with the name \"" + rectName + "\" has already been packed: " + rect.name);
						}
					}
				}
			}
		}

		Writer writer = new OutputStreamWriter(new FileOutputStream(packFile, true), "UTF-8");
		for (Page page : pages) {
			writer.write("\n" + page.imageName + "\n");
			writer.write("size: " + page.imageWidth + "," + page.imageHeight + "\n");
			writer.write("format: " + settings.format + "\n");
			writer.write("filter: " + settings.filterMin + "," + settings.filterMag + "\n");
			writer.write("repeat: " + getRepeatValue() + "\n");

			page.outputRects.sort();
			for (Rect rect : page.outputRects) {
				writeRect(writer, page, rect, rect.name);
				Array<Alias> aliases = new Array(rect.aliases.toArray());
				aliases.sort();
				for (Alias alias : aliases) {
					Rect aliasRect = new Rect();
					aliasRect.set(rect);
					alias.apply(aliasRect);
					writeRect(writer, page, aliasRect, alias.name);
				}
			}
		}
		writer.close();
	}

	private void writeRect (Writer writer, Page page, Rect rect, String name) throws IOException {
		writer.write(Rect.getAtlasName(name, settings.flattenPaths) + "\n");
		writer.write("  rotate: " + rect.rotated + "\n");
		writer.write("  xy: " + (page.x + rect.x) + ", " + (page.y + page.height - rect.height - rect.y) + "\n");

		writer.write("  size: " + rect.regionWidth + ", " + rect.regionHeight + "\n");
		if (rect.splits != null) {
			writer.write("  split: " //
				+ rect.splits[0] + ", " + rect.splits[1] + ", " + rect.splits[2] + ", " + rect.splits[3] + "\n");
		}
		if (rect.pads != null) {
			if (rect.splits == null) writer.write("  split: 0, 0, 0, 0\n");
			writer.write("  pad: " + rect.pads[0] + ", " + rect.pads[1] + ", " + rect.pads[2] + ", " + rect.pads[3] + "\n");
		}
		writer.write("  orig: " + rect.originalWidth + ", " + rect.originalHeight + "\n");
		writer.write("  offset: " + rect.offsetX + ", " + (rect.originalHeight - rect.regionHeight - rect.offsetY) + "\n");
		writer.write("  index: " + rect.index + "\n");
	}

	private String getRepeatValue () {
		if (settings.wrapX == TextureWrap.Repeat && settings.wrapY == TextureWrap.Repeat) return "xy";
		if (settings.wrapX == TextureWrap.Repeat && settings.wrapY == TextureWrap.ClampToEdge) return "x";
		if (settings.wrapX == TextureWrap.ClampToEdge && settings.wrapY == TextureWrap.Repeat) return "y";
		return "none";
	}

	private int getBufferedImageType (Format format) {
		switch (settings.format) {
		case RGBA8888:
		case RGBA4444:
			return BufferedImage.TYPE_INT_ARGB;
		case RGB565:
		case RGB888:
			return BufferedImage.TYPE_INT_RGB;
		case Alpha:
			return BufferedImage.TYPE_BYTE_GRAY;
		default:
			throw new RuntimeException("Unsupported format: " + settings.format);
		}
	}

	/** @author Nathan Sweet */
	static public class Page {
		public String imageName;
		public Array<Rect> outputRects, remainingRects;
		public float occupancy;
		public int x, y, width, height, imageWidth, imageHeight;
	}

	/** @author Regnarock
	 * @author Nathan Sweet */
	static public class Alias implements Comparable<Alias> {
		public String name;
		public int index;
		public int[] splits;
		public int[] pads;
		public int offsetX, offsetY, originalWidth, originalHeight;

		public Alias (Rect rect) {
			name = rect.name;
			index = rect.index;
			splits = rect.splits;
			pads = rect.pads;
			offsetX = rect.offsetX;
			offsetY = rect.offsetY;
			originalWidth = rect.originalWidth;
			originalHeight = rect.originalHeight;
		}

		public void apply (Rect rect) {
			rect.name = name;
			rect.index = index;
			rect.splits = splits;
			rect.pads = pads;
			rect.offsetX = offsetX;
			rect.offsetY = offsetY;
			rect.originalWidth = originalWidth;
			rect.originalHeight = originalHeight;
		}

		public int compareTo (Alias o) {
			return name.compareTo(o.name);
		}
	}

	/** @author Nathan Sweet */
	static public class Rect implements Comparable<Rect> {
		public String name;
		public int offsetX, offsetY, regionWidth, regionHeight, originalWidth, originalHeight;
		public int x, y;
		public int width, height; // Portion of page taken by this region, including padding.
		public int index;
		public boolean rotated;
		public Set<Alias> aliases = new HashSet<Alias>();
		public int[] splits;
		public int[] pads;
		public boolean canRotate = true;

		private boolean isPatch;
		private BufferedImage image;
		private File file;
		int score1, score2;

		//TODO probably redundant
		boolean programmaticPatch = false;

		Rect (BufferedImage source, int left, int top, int newWidth, int newHeight, boolean isPatch) {
			image = new BufferedImage(source.getColorModel(),
				source.getRaster().createWritableChild(left, top, newWidth, newHeight, 0, 0, null),
				source.getColorModel().isAlphaPremultiplied(), null);
			offsetX = left;
			offsetY = top;
			regionWidth = newWidth;
			regionHeight = newHeight;
			originalWidth = source.getWidth();
			originalHeight = source.getHeight();
			width = newWidth;
			height = newHeight;
			this.isPatch = isPatch;
		}

		/** Clears the image for this rect, which will be loaded from the specified file by {@link #getImage(ImageProcessor)}. */
		public void unloadImage (File file) {
			this.file = file;
			image = null;
		}

		public BufferedImage getImage (ImageProcessor imageProcessor) {
			if (image != null) return image;

			BufferedImage image;
			try {
				image = ImageIO.read(file);
			} catch (IOException ex) {
				throw new RuntimeException("Error reading image: " + file, ex);
			}
			if (image == null) throw new RuntimeException("Unable to read image: " + file);
			String name = this.name;
			if (isPatch) name += ".9";
			return imageProcessor.processImage(image, name).getImage(null);
		}

		Rect () {
		}

		Rect (Rect rect) {
			x = rect.x;
			y = rect.y;
			width = rect.width;
			height = rect.height;
		}

		void set (Rect rect) {
			name = rect.name;
			image = rect.image;
			offsetX = rect.offsetX;
			offsetY = rect.offsetY;
			regionWidth = rect.regionWidth;
			regionHeight = rect.regionHeight;
			originalWidth = rect.originalWidth;
			originalHeight = rect.originalHeight;
			x = rect.x;
			y = rect.y;
			width = rect.width;
			height = rect.height;
			index = rect.index;
			rotated = rect.rotated;
			aliases = rect.aliases;
			splits = rect.splits;
			pads = rect.pads;
			canRotate = rect.canRotate;
			score1 = rect.score1;
			score2 = rect.score2;
			file = rect.file;
			isPatch = rect.isPatch;
		}

		public int compareTo (Rect o) {
			return name.compareTo(o.name);
		}

		@Override
		public boolean equals (Object obj) {
			if (this == obj) return true;
			if (obj == null) return false;
			if (getClass() != obj.getClass()) return false;
			Rect other = (Rect)obj;
			if (name == null) {
				if (other.name != null) return false;
			} else if (!name.equals(other.name)) return false;
			return true;
		}

		@Override
		public String toString () {
			return name + (index != -1 ? "_" + index : "") + "[" + x + "," + y + " " + width + "x" + height + "]";
		}

		static public String getAtlasName (String name, boolean flattenPaths) {
			return flattenPaths ? new FileHandle(name).name() : name;
		}
	}

	/** @author Nathan Sweet */
	static public class Settings {
		public boolean pot = true;
		public int paddingX = 2, paddingY = 2;
		public boolean edgePadding = true;
		public boolean duplicatePadding = false;
		public boolean rotation;
		public int minWidth = 16, minHeight = 16;
		public int maxWidth = 2048, maxHeight = 2048;
		public boolean square = false;
		public boolean stripWhitespaceX, stripWhitespaceY;
		public int alphaThreshold;
		public TextureFilter filterMin = TextureFilter.Nearest, filterMag = TextureFilter.Nearest;
		public TextureWrap wrapX = TextureWrap.ClampToEdge, wrapY = TextureWrap.ClampToEdge;
		public Format format = Format.RGBA8888;
		public boolean alias = true;
		public boolean ignoreBlankImages = true;
		public boolean fast;
		public boolean debug;
		public boolean silent;
		public boolean combineSubdirectories;
		public boolean ignore;
		public boolean flattenPaths;
		public boolean premultiplyAlpha;
		public boolean useIndexes = true;
		public boolean bleed = true;
		public int bleedIterations = 2;
		public boolean limitMemory = true;
		public boolean grid;
		public float[] scale = {1};
		public String[] scaleSuffix = {""};
		public String atlasExtension = ".atlas";

		public Settings () {
		}

		/** @see #set(Settings) */
		public Settings (Settings settings) {
			set(settings);
		}

		/** Copies values from another instance to the current one */
		public void set (Settings settings) {
			fast = settings.fast;
			rotation = settings.rotation;
			pot = settings.pot;
			minWidth = settings.minWidth;
			minHeight = settings.minHeight;
			maxWidth = settings.maxWidth;
			maxHeight = settings.maxHeight;
			paddingX = settings.paddingX;
			paddingY = settings.paddingY;
			edgePadding = settings.edgePadding;
			duplicatePadding = settings.duplicatePadding;
			alphaThreshold = settings.alphaThreshold;
			ignoreBlankImages = settings.ignoreBlankImages;
			stripWhitespaceX = settings.stripWhitespaceX;
			stripWhitespaceY = settings.stripWhitespaceY;
			alias = settings.alias;
			format = settings.format;
			filterMin = settings.filterMin;
			filterMag = settings.filterMag;
			wrapX = settings.wrapX;
			wrapY = settings.wrapY;
			debug = settings.debug;
			silent = settings.silent;
			combineSubdirectories = settings.combineSubdirectories;
			ignore = settings.ignore;
			flattenPaths = settings.flattenPaths;
			premultiplyAlpha = settings.premultiplyAlpha;
			square = settings.square;
			useIndexes = settings.useIndexes;
			bleed = settings.bleed;
			bleedIterations = settings.bleedIterations;
			limitMemory = settings.limitMemory;
			grid = settings.grid;
			scale = Arrays.copyOf(settings.scale, settings.scale.length);
			scaleSuffix = Arrays.copyOf(settings.scaleSuffix, settings.scaleSuffix.length);
			atlasExtension = settings.atlasExtension;
		}

		public String getScaledPackFileName (String packFileName, int scaleIndex) {
			// Use suffix if not empty string.
			if (scaleSuffix[scaleIndex].length() > 0)
				packFileName += scaleSuffix[scaleIndex];
			else {
				// Otherwise if scale != 1 or multiple scales, use subdirectory.
				float scaleValue = scale[scaleIndex];
				if (scale.length != 1) {
					packFileName = (scaleValue == (int)scaleValue ? Integer.toString((int)scaleValue) : Float.toString(scaleValue))
						+ "/" + packFileName;
				}
			}
			return packFileName;
		}
	}

	static public interface Packer {
		public Array<Page> pack(Array<Rect> inputRects);
	}

	static final class InputImage {
		File file;
		String name;
		BufferedImage image;
		NinePatchProps ninePatchProps;

		public static class NinePatchProps {
			public int[] splits;
			public int[] pads;
		}
	}
}
