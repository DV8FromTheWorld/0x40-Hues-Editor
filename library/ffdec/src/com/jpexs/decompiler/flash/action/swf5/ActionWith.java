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
package com.jpexs.decompiler.flash.action.swf5;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SWFOutputStream;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.ActionList;
import com.jpexs.decompiler.flash.action.model.clauses.WithActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.parser.pcode.FlasmLexer;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class ActionWith extends Action implements GraphSourceItemContainer {

    public int codeSize;

    public int version;

    public ActionWith(int codeSize) {
        super(0x94, 2);
        this.codeSize = codeSize;
    }

    @Override
    public boolean parseDivision(long size, FlasmLexer lexer) {
        codeSize = (int) (size - getHeaderSize());
        return false;
    }

    public ActionWith(int actionLength, SWFInputStream sis, int version) throws IOException {
        super(0x94, actionLength);
        codeSize = sis.readUI16("codeSize");
        this.version = version;
    }

    public ActionWith(FlasmLexer lexer) throws IOException, ActionParseException {
        super(0x94, 2);
        lexBlockOpen(lexer);
    }

    @Override
    protected void getContentBytes(SWFOutputStream sos) throws IOException {
        sos.writeUI16(codeSize);
    }

    /**
     * Gets the length of action converted to bytes
     *
     * @return Length
     */
    @Override
    protected int getContentBytesLength() {
        return 2;
    }

    @Override
    public String getASMSource(ActionList container, Set<Long> knownAddreses, ScriptExportMode exportMode) {
        return "With {";
    }

    @Override
    public void translateContainer(List<List<GraphTargetItem>> content, TranslateStack stack, List<GraphTargetItem> output, HashMap<Integer, String> regNames, HashMap<String, GraphTargetItem> variables, HashMap<String, GraphTargetItem> functions) {
        output.add(new WithActionItem(this, stack.pop(), content.get(0)));
    }

    @Override
    public List<Long> getContainerSizes() {
        List<Long> ret = new ArrayList<>();
        ret.add((Long) (long) codeSize);
        return ret;
    }

    @Override
    public void setContainerSize(int index, long size) {
        if (index == 0) {
            codeSize = (int) size;
        } else {
            throw new IllegalArgumentException("Index must be 0.");
        }
    }

    @Override
    public String getASMSourceBetween(int pos) {
        return "";
    }

    @Override
    public long getHeaderSize() {
        return getBytesLength(version);
    }

    @Override
    public HashMap<Integer, String> getRegNames() {
        return new HashMap<>();
    }

    @Override
    public String getName() {
        return null;
    }
}
