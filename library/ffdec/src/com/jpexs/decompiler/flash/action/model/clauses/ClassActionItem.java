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
package com.jpexs.decompiler.flash.action.model.clauses;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.parser.script.VariableActionItem;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.Helper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ClassActionItem extends ActionItem implements Block {

    public List<GraphTargetItem> functions;

    public List<GraphTargetItem> staticFunctions;

    public GraphTargetItem extendsOp;

    public List<GraphTargetItem> implementsOp;

    public GraphTargetItem className;

    public GraphTargetItem constructor;

    public List<MyEntry<GraphTargetItem, GraphTargetItem>> vars;

    public List<MyEntry<GraphTargetItem, GraphTargetItem>> staticVars;

    public Set<String> uninitializedVars;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (functions != null) {
            ret.add(functions);
        }
        if (staticFunctions != null) {
            ret.add(staticFunctions);
        }
        return ret;
    }

    public ClassActionItem(GraphTargetItem className, GraphTargetItem extendsOp, List<GraphTargetItem> implementsOp, GraphTargetItem constructor, List<GraphTargetItem> functions, List<MyEntry<GraphTargetItem, GraphTargetItem>> vars, List<GraphTargetItem> staticFunctions, List<MyEntry<GraphTargetItem, GraphTargetItem>> staticVars) {
        super(null, NOPRECEDENCE);
        this.className = className;
        this.functions = functions;
        this.vars = vars;
        this.extendsOp = extendsOp;
        this.implementsOp = implementsOp;
        this.staticFunctions = staticFunctions;
        this.staticVars = staticVars;
        this.constructor = constructor;

        List<GraphTargetItem> allFunc = new ArrayList<>(functions);
        if (constructor != null) {
            allFunc.add(constructor);
        }
        this.uninitializedVars = new HashSet<>();
        List<GraphTargetItem> allUsages = new ArrayList<>();
        for (GraphTargetItem it : allFunc) {
            if (it instanceof FunctionActionItem) {
                FunctionActionItem f = (FunctionActionItem) it;
                detectUnitializedVars(f.actions, allUsages);
            }
        }
        Set<String> allMembers = new HashSet<>();
        for (GraphTargetItem it : allUsages) {
            allMembers.add(it.toStringNoQuotes(LocalData.empty));
        }
        uninitializedVars.addAll(allMembers);
        for (MyEntry<GraphTargetItem, GraphTargetItem> v : vars) {
            String s = v.getKey().toStringNoQuotes(LocalData.empty);
            if (uninitializedVars.contains(s)) {
                uninitializedVars.remove(s);
            }
        }
    }

    private boolean isThis(GraphTargetItem item) {
        if (item instanceof VariableActionItem) {
            return "this".equals(((VariableActionItem) item).getVariableName());
        }
        return false;
    }

    private void detectUnitializedVars(GraphTargetItem item, List<GraphTargetItem> ret) {
        if (item == null) {
            return;
        }

        if (item instanceof GetMemberActionItem) {
            GetMemberActionItem gm = (GetMemberActionItem) item;
            if (isThis(gm.object)) {
                ret.add(gm.memberName);
            } else {
                detectUnitializedVars(gm.object, ret);
            }
        }
        if (item instanceof SetMemberActionItem) {
            SetMemberActionItem sm = (SetMemberActionItem) item;
            if (isThis(sm.object)) {
                ret.add(sm.objectName);
            } else {
                detectUnitializedVars(sm.object, ret);
            }
        }

        if (item instanceof Block) {
            Block bl = (Block) item;
            for (List<GraphTargetItem> list : bl.getSubs()) {
                detectUnitializedVars(list, ret);
            }
        }
        detectUnitializedVars(item.getAllSubItems(), ret);
    }

    private void detectUnitializedVars(List<GraphTargetItem> items, List<GraphTargetItem> ret) {
        for (GraphTargetItem it : items) {
            detectUnitializedVars(it, ret);
        }
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("class ");
        className.toStringNoQuotes(writer, localData);
        if (extendsOp != null) {
            writer.append(" extends ");
            extendsOp.toStringNoQuotes(writer, localData);
        }
        if (!implementsOp.isEmpty()) {
            writer.append(" implements ");
            boolean first = true;
            for (GraphTargetItem t : implementsOp) {
                if (!first) {
                    writer.append(", ");
                }
                first = false;
                Action.getWithoutGlobal(t).toString(writer, localData);
            }
        }
        writer.startBlock();

        if (constructor != null) {
            constructor.toString(writer, localData).newLine();
        }

        for (MyEntry<GraphTargetItem, GraphTargetItem> item : vars) {
            writer.append("var ");
            item.getKey().toStringNoQuotes(writer, localData);
            writer.append(" = ");
            item.getValue().toString(writer, localData);
            writer.append(";").newLine();
        }
        for (String v : uninitializedVars) {
            writer.append("var ");
            writer.append(v);
            writer.append(";").newLine();
        }
        for (MyEntry<GraphTargetItem, GraphTargetItem> item : staticVars) {
            writer.append("static var ");
            item.getKey().toStringNoQuotes(writer, localData);
            writer.append(" = ");
            item.getValue().toString(writer, localData);
            writer.append(";").newLine();
        }

        for (GraphTargetItem f : functions) {
            f.toString(writer, localData).newLine();
        }
        for (GraphTargetItem f : staticFunctions) {
            writer.append("static ");
            f.toString(writer, localData).newLine();
        }

        return writer.endBlock();
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        return ret;
    }

    @Override
    public boolean needsSemicolon() {
        return false;
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ActionSourceGenerator asGenerator = (ActionSourceGenerator) generator;
        SourceGeneratorLocalData localData2 = Helper.deepCopy(localData);
        asGenerator.setInMethod(localData2, true);
        ret.addAll(asGenerator.generateTraits(localData2, false, className, extendsOp, implementsOp, constructor, functions, vars, staticFunctions, staticVars));
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
