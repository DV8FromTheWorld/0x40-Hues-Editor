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
package com.jpexs.decompiler.flash.action.model;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.swf4.RegisterNumber;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphPart;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphSourceItemPos;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.List;
import java.util.Set;

public class StoreRegisterActionItem extends ActionItem implements SetTypeActionItem {

    public RegisterNumber register;

    //public GraphTargetItem value;
    public boolean define = false;

    public boolean temporary = false;

    @Override
    public GraphPart getFirstPart() {
        return value.getFirstPart();
    }

    @Override
    public void setValue(GraphTargetItem value) {
        this.value = value;
    }

    private int tempRegister = -1;

    @Override
    public int getTempRegister() {
        return tempRegister;
    }

    @Override
    public void setTempRegister(int tempRegister) {
        this.tempRegister = tempRegister;
    }

    @Override
    public GraphTargetItem getValue() {
        return value;
    }

    public StoreRegisterActionItem(GraphSourceItem instruction, RegisterNumber register, GraphTargetItem value, boolean define) {
        super(instruction, PRECEDENCE_ASSIGMENT, value);
        this.register = register;
        this.define = define;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (temporary) {
            value.toString(writer, localData);
        } else {
            if (define) {
                writer.append("var ");
            }
            writer.append(register.translate()).append(" = ");
            value.toString(writer, localData);
        }
        return writer;
    }

    @Override
    public GraphTargetItem getObject() {
        return new DirectValueActionItem(getSrc(), -1, register, null);
    }

    @Override
    public List<GraphSourceItemPos> getNeededSources() {
        List<GraphSourceItemPos> ret = super.getNeededSources();
        ret.addAll(value.getNeededSources());
        return ret;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSourceMerge(localData, generator, value, new ActionStoreRegister(register.number));
    }

    @Override
    public boolean hasReturnValue() {
        return true;
    }

    @Override
    public boolean isCompileTime(Set<GraphTargetItem> dependencies) {
        if (dependencies.contains(value)) {
            return false;
        }
        dependencies.add(value);
        return value.isCompileTime(dependencies);
    }

    @Override
    public Object getResult() {
        return value.getResult();
    }
}
