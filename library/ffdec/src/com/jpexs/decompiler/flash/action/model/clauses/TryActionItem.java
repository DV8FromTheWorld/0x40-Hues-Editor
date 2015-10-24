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

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.ActionItem;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.parser.script.ActionSourceGenerator;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf7.ActionTry;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.Block;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.List;

public class TryActionItem extends ActionItem implements Block {

    public List<GraphTargetItem> tryCommands;

    public List<GraphTargetItem> catchExceptions;

    public List<List<GraphTargetItem>> catchCommands;

    public List<GraphTargetItem> finallyCommands;

    @Override
    public List<List<GraphTargetItem>> getSubs() {
        List<List<GraphTargetItem>> ret = new ArrayList<>();
        if (tryCommands != null) {
            ret.add(tryCommands);
        }
        ret.addAll(catchCommands);
        if (finallyCommands != null) {
            ret.add(finallyCommands);
        }
        return ret;
    }

    public TryActionItem(List<GraphTargetItem> tryCommands, List<GraphTargetItem> catchExceptions, List<List<GraphTargetItem>> catchCommands, List<GraphTargetItem> finallyCommands) {
        super(null, NOPRECEDENCE);
        this.tryCommands = tryCommands;
        this.catchExceptions = catchExceptions;
        this.catchCommands = catchCommands;
        this.finallyCommands = finallyCommands;
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        writer.append("try");
        appendBlock(null, writer, localData, tryCommands);
        for (int e = 0; e < catchExceptions.size(); e++) {
            writer.newLine();
            writer.append("catch");
            if (writer.getFormatting().spaceBeforeParenthesesCatchParentheses) {
                writer.append(" ");
            }
            writer.append("(");
            catchExceptions.get(e).toStringNoQuotes(writer, localData);
            writer.append(")");
            List<GraphTargetItem> commands = catchCommands.get(e);
            appendBlock(null, writer, localData, commands);
        }
        if (finallyCommands.size() > 0) {
            writer.newLine();
            writer.append("finally");
            appendBlock(null, writer, localData, finallyCommands);
        }
        return writer;
    }

    @Override
    public List<ContinueItem> getContinues() {
        List<ContinueItem> ret = new ArrayList<>();
        for (GraphTargetItem ti : tryCommands) {
            if (ti instanceof ContinueItem) {
                ret.add((ContinueItem) ti);
            }
            if (ti instanceof Block) {
                ret.addAll(((Block) ti).getContinues());
            }
        }
        if (finallyCommands != null) {
            for (GraphTargetItem ti : finallyCommands) {
                if (ti instanceof ContinueItem) {
                    ret.add((ContinueItem) ti);
                }
                if (ti instanceof Block) {
                    ret.addAll(((Block) ti).getContinues());
                }
            }
        }
        for (List<GraphTargetItem> commands : catchCommands) {
            for (GraphTargetItem ti : commands) {
                if (ti instanceof ContinueItem) {
                    ret.add((ContinueItem) ti);
                }
                if (ti instanceof Block) {
                    ret.addAll(((Block) ti).getContinues());
                }
            }
        }
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
        List<Action> tryCommandsA = asGenerator.toActionList(asGenerator.generate(localData, tryCommands));
        List<Action> finallyCommandsA = finallyCommands == null ? null : asGenerator.toActionList(asGenerator.generate(localData, finallyCommands));
        List<Action> catchCommandsA = null;
        String catchName = null;
        if (catchExceptions != null) {
            if (!catchExceptions.isEmpty()) {
                catchName = catchExceptions.get(0).toStringNoQuotes(LocalData.create(new ConstantPool(asGenerator.getConstantPool())));
            }

        }
        int catchSize = 0;
        if (catchCommands != null && !catchCommands.isEmpty()) {
            catchCommandsA = asGenerator.toActionList(asGenerator.generate(localData, catchCommands.get(0)));
            catchSize = Action.actionsToBytes(catchCommandsA, false, SWF.DEFAULT_VERSION).length;
            tryCommandsA.add(new ActionJump(catchSize));
        }
        int finallySize = 0;
        if (finallyCommandsA != null) {
            finallySize = Action.actionsToBytes(finallyCommandsA, false, SWF.DEFAULT_VERSION).length;
        }
        int trySize = Action.actionsToBytes(tryCommandsA, false, SWF.DEFAULT_VERSION).length;
        ret.add(new ActionTry(false, finallyCommands != null, catchCommands != null, catchName, 0, trySize, catchSize, finallySize, SWF.DEFAULT_VERSION));
        ret.addAll(tryCommandsA);
        if (catchCommandsA != null) {
            ret.addAll(catchCommandsA);
        }
        if (finallyCommandsA != null) {
            ret.addAll(finallyCommandsA);
        }
        return ret;
    }

    @Override
    public boolean hasReturnValue() {
        return false;
    }
}
