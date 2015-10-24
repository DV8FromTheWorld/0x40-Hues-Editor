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

import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;

/**
 *
 * @author JPEXS
 */
public class NextValueAVM2Item extends AVM2Item {

    GraphTargetItem index;

    GraphTargetItem obj;

    public NextValueAVM2Item(AVM2Instruction instruction, GraphTargetItem index, GraphTargetItem obj) {
        super(instruction, NOPRECEDENCE);
        this.index = index;
        this.obj = obj;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("§§nextvalue");
        writer.spaceBeforeCallParenthesies(2);
        writer.append("(");
        index.toString(writer, localData);
        writer.append(",");
        obj.toString(writer, localData);
        return writer.append(")");
    }

    @Override
    public GraphTargetItem returnType() {
        return TypeItem.UNBOUNDED;
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }
}
