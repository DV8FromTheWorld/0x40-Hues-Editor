/*
 *  Copyright (C) 2010-2015 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.tags.base;

import com.jpexs.decompiler.flash.exporters.commonshape.Matrix;
import com.jpexs.decompiler.flash.exporters.commonshape.SVGExporter;
import com.jpexs.decompiler.flash.types.ColorTransform;
import com.jpexs.helpers.SerializableImage;
import java.awt.Shape;
import java.io.IOException;

/**
 *
 * @author JPEXS
 */
public interface DrawableTag extends BoundedTag {

    public static final int PARAMETER_FRAME = 1;

    public static final int PARAMETER_TIME = 2;

    public static final int PARAMETER_RATIO = 4;

    public int getUsedParameters();

    public Shape getOutline(int frame, int time, int ratio, RenderContext renderContext, Matrix transformation);

    public void toImage(int frame, int time, int ratio, RenderContext renderContext, SerializableImage image, Matrix transformation, ColorTransform colorTransform);

    public void toSVG(SVGExporter exporter, int ratio, ColorTransform colorTransform, int level, double zoom) throws IOException;

    public void toHtmlCanvas(StringBuilder result, double unitDivisor);

    public int getNumFrames();

    public boolean isSingleFrame();
}
