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
package com.jpexs.decompiler.flash.abc.avm2.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AlchemyLoadAVM2Item extends AVM2Item {

    private final char type;

    private final int size;

    private final GraphTargetItem ofs;

    public AlchemyLoadAVM2Item(GraphSourceItem instruction, GraphTargetItem ofs, char type, int size) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.ofs = ofs;
        this.type = type;
        this.size = size;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("l").append(type).append(size).append("(");
        ofs.toString(writer, localData);
        return writer.append(")");
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        String ts = "" + type + size;
        int code = 0;
        switch (ts) {
            case "i8":
                code = AVM2Instructions.Li8;
                break;
            case "i16":
                code = AVM2Instructions.Li16;
                break;
            case "i32":
                code = AVM2Instructions.Li32;
                break;
            case "f":
                code = AVM2Instructions.Lf32;
                break;
            case "f32":
                code = AVM2Instructions.Lf64;
                break;
        }
        return toSourceMerge(localData, generator, ofs, ins(code));
    }

    @Override
    public GraphTargetItem returnType() {
        switch (type) {
            case 'i':
                return new TypeItem(DottedChain.INT);
            case 'f':
                return new TypeItem(DottedChain.NUMBER);
        }
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
