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
package com.jpexs.decompiler.flash.abc.avm2.parser.script;

import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.AVM2Code;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.instructions.construction.ConstructSuperIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.jumps.JumpIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnValueIns;
import com.jpexs.decompiler.flash.abc.avm2.instructions.other.ReturnVoidIns;
import com.jpexs.decompiler.flash.abc.avm2.model.AVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ApplyTypeAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.BooleanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.FloatValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.GetDescendantsAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NanAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ReturnVoidAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThrowAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.WithObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForEachInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.ForInAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.clauses.TryAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.operations.IfCondition;
import com.jpexs.decompiler.flash.abc.avm2.parser.AVM2ParseException;
import com.jpexs.decompiler.flash.abc.types.ABCException;
import com.jpexs.decompiler.flash.abc.types.ClassInfo;
import com.jpexs.decompiler.flash.abc.types.InstanceInfo;
import com.jpexs.decompiler.flash.abc.types.MetadataInfo;
import com.jpexs.decompiler.flash.abc.types.MethodBody;
import com.jpexs.decompiler.flash.abc.types.MethodInfo;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.abc.types.ScriptInfo;
import com.jpexs.decompiler.flash.abc.types.ValueKind;
import com.jpexs.decompiler.flash.abc.types.traits.Trait;
import com.jpexs.decompiler.flash.abc.types.traits.TraitClass;
import com.jpexs.decompiler.flash.abc.types.traits.TraitFunction;
import com.jpexs.decompiler.flash.abc.types.traits.TraitMethodGetterSetter;
import com.jpexs.decompiler.flash.abc.types.traits.TraitSlotConst;
import com.jpexs.decompiler.flash.abc.types.traits.Traits;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.Loop;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.DuplicateItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import com.jpexs.decompiler.graph.model.UnboundedTypeItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author JPEXS
 */
public class AVM2SourceGenerator implements SourceGenerator {

    public final ABC abc;

    public List<ABC> allABCs;

    public static final int MARK_E_START = 0;

    public static final int MARK_E_END = 1;

    public static final int MARK_E_TARGET = 2;

    public static final int MARK_E_FINALLYPART = 3;

    private AVM2Instruction ins(int instructionCode, int... operands) {
        return new AVM2Instruction(0, instructionCode, operands);
    }

    private AVM2Instruction ins(InstructionDefinition def, int... operands) {
        return new AVM2Instruction(0, def, operands);
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, FalseItem item) throws CompilationException {
        return GraphTargetItem.toSourceMerge(localData, this, ins(AVM2Instructions.PushFalse));
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TrueItem item) throws CompilationException {
        return GraphTargetItem.toSourceMerge(localData, this, ins(AVM2Instructions.PushTrue));
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, GetDescendantsAVM2Item item) throws CompilationException {
        int[] nssa = new int[item.openedNamespaces.size()];
        for (int i = 0; i < item.openedNamespaces.size(); i++) {
            nssa[i] = item.openedNamespaces.get(i);
        }
        int nsset = abc.constants.getNamespaceSetId(new NamespaceSet(nssa), true);

        return GraphTargetItem.toSourceMerge(localData, this,
                item.object,
                ins(AVM2Instructions.GetDescendants, abc.constants.getMultinameId(new Multiname(Multiname.MULTINAME, abc.constants.getStringId(item.nameStr, true), 0, nsset, 0, new ArrayList<>()), true))
        );
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, AndItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generateToActionList(localData, item.leftSide));
        if (!("" + item.leftSide.returnType()).equals("Boolean")) {
            ret.add(ins(AVM2Instructions.ConvertB));
        }
        ret.add(ins(AVM2Instructions.Dup));
        List<AVM2Instruction> andExpr = generateToActionList(localData, item.rightSide);
        andExpr.add(0, ins(AVM2Instructions.Pop));
        int andExprLen = insToBytes(andExpr).length;
        ret.add(ins(AVM2Instructions.IfFalse, andExprLen));
        ret.addAll(andExpr);
        return ret;

    }

    private byte[] insToBytes(List<AVM2Instruction> code) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            for (AVM2Instruction instruction : code) {

                baos.write(instruction.getBytes());

            }
            return baos.toByteArray();
        } catch (IOException ex) {
            Logger.getLogger(AVM2SourceGenerator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return SWFInputStream.BYTE_ARRAY_EMPTY;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, OrItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generateToActionList(localData, item.leftSide));
        if (!("" + item.leftSide.returnType()).equals("Boolean")) {
            ret.add(ins(AVM2Instructions.ConvertB));
        }
        ret.add(ins(AVM2Instructions.Dup));
        List<AVM2Instruction> orExpr = generateToActionList(localData, item.rightSide);
        orExpr.add(0, ins(AVM2Instructions.Pop));
        int orExprLen = insToBytes(orExpr).length;
        ret.add(ins(AVM2Instructions.IfTrue, orExprLen));
        ret.addAll(orExpr);
        return ret;
    }

    public List<AVM2Instruction> toInsList(List<GraphSourceItem> items) {
        List<AVM2Instruction> ret = new ArrayList<>();
        for (GraphSourceItem s : items) {
            if (s instanceof AVM2Instruction) {
                ret.add((AVM2Instruction) s);
            }
        }
        return ret;
    }

    private List<AVM2Instruction> nonempty(List<AVM2Instruction> list) {
        if (list == null) {
            return new ArrayList<>();
        }
        return list;
    }

    private List<GraphSourceItem> condition(SourceGeneratorLocalData localData, GraphTargetItem t, int offset) throws CompilationException {
        if (t instanceof IfCondition) {
            IfCondition ic = (IfCondition) t;
            return GraphTargetItem.toSourceMerge(localData, this, ic.getLeftSide(), ic.getRightSide(), ins(ic.getIfDefinition(), offset));
        }
        return GraphTargetItem.toSourceMerge(localData, this, t, ins(AVM2Instructions.IfTrue, offset));
    }

    private List<GraphSourceItem> notCondition(SourceGeneratorLocalData localData, GraphTargetItem t, int offset) throws CompilationException {
        if (t instanceof IfCondition) {
            IfCondition ic = (IfCondition) t;
            return GraphTargetItem.toSourceMerge(localData, this, ic.getLeftSide(), ic.getRightSide(), ins(ic.getIfNotDefinition(), offset));
        }
        return GraphTargetItem.toSourceMerge(localData, this, t, ins(AVM2Instructions.IfFalse, offset));
    }

    private List<GraphSourceItem> generateIf(SourceGeneratorLocalData localData, GraphTargetItem expression, List<GraphTargetItem> onTrueCmds, List<GraphTargetItem> onFalseCmds, boolean ternar) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        //ret.addAll(notCondition(localData, expression));
        List<AVM2Instruction> onTrue = null;
        List<AVM2Instruction> onFalse = null;
        if (ternar) {
            onTrue = toInsList(onTrueCmds.get(0).toSource(localData, this));
        } else {
            onTrue = generateToInsList(localData, onTrueCmds);
        }

        if (onFalseCmds != null && !onFalseCmds.isEmpty()) {
            if (ternar) {
                onFalse = toInsList(onFalseCmds.get(0).toSource(localData, this));
            } else {
                onFalse = generateToInsList(localData, onFalseCmds);
            }
        }
        AVM2Instruction ajmp = null;
        if (onFalse != null) {
            if (!((!nonempty(onTrue).isEmpty())
                    && ((onTrue.get(onTrue.size() - 1).definition instanceof ContinueJumpIns)
                    || ((onTrue.get(onTrue.size() - 1).definition instanceof BreakJumpIns))))) {
                ajmp = ins(AVM2Instructions.Jump, 0);
                onTrue.add(ajmp);
            }
        }

        byte[] onTrueBytes = insToBytes(onTrue);
        int onTrueLen = onTrueBytes.length;

        ret.addAll(notCondition(localData, expression, onTrueLen));
        ret.addAll(onTrue);

        if (onFalse != null) {
            byte[] onFalseBytes = insToBytes(onFalse);
            int onFalseLen = onFalseBytes.length;
            if (ajmp != null) {
                ajmp.operands[0] = onFalseLen;
            }
            ret.addAll(onFalse);
        }
        return ret;
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, XMLFilterAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        final Reference<Integer> counterReg = new Reference<>(0);
        final Reference<Integer> collectionReg = new Reference<>(0);
        final Reference<Integer> xmlListReg = new Reference<>(0);
        List<GraphSourceItem> xmlListSetTemp = AssignableAVM2Item.setTemp(localData, this, xmlListReg);
        ret.addAll(GraphTargetItem.toSourceMerge(localData, this,
                ins(AVM2Instructions.PushByte, 0),
                AssignableAVM2Item.setTemp(localData, this, counterReg),
                item.object,
                ins(AVM2Instructions.CheckFilter),
                NameAVM2Item.generateCoerce(localData, this, TypeItem.UNBOUNDED),
                AssignableAVM2Item.setTemp(localData, this, collectionReg),
                ins(AVM2Instructions.GetLex, abc.constants.getMultinameId(new Multiname(Multiname.QNAME, abc.constants.getStringId("XMLList", true), abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId("", true)), 0, true), 0, 0, new ArrayList<>()), true)),
                ins(AVM2Instructions.PushString, abc.constants.getStringId("", true)),
                ins(AVM2Instructions.Construct, 1),
                xmlListSetTemp
        ));
        final Reference<Integer> tempVal1 = new Reference<>(0);
        final Reference<Integer> tempVal2 = new Reference<>(0);

        List<AVM2Instruction> forBody = toInsList(GraphTargetItem.toSourceMerge(localData, this,
                ins(AVM2Instructions.Label),
                AssignableAVM2Item.getTemp(localData, this, collectionReg),
                AssignableAVM2Item.getTemp(localData, this, counterReg),
                ins(AVM2Instructions.NextValue),
                AssignableAVM2Item.dupSetTemp(localData, this, tempVal1),
                AssignableAVM2Item.dupSetTemp(localData, this, tempVal2),
                ins(AVM2Instructions.PushWith)
        ));
        localData.scopeStack.add(new LocalRegAVM2Item(null, tempVal2.getVal(), null));
        forBody.addAll(toInsList(item.value.toSource(localData, this)));
        List<AVM2Instruction> trueBody = new ArrayList<>();
        trueBody.addAll(toInsList(AssignableAVM2Item.getTemp(localData, this, xmlListReg)));
        trueBody.addAll(toInsList(AssignableAVM2Item.getTemp(localData, this, counterReg)));
        trueBody.addAll(toInsList(AssignableAVM2Item.getTemp(localData, this, tempVal1)));
        int[] nss = new int[item.openedNamespaces.size()];
        for (int i = 0; i < item.openedNamespaces.size(); i++) {
            nss[i] = item.openedNamespaces.get(i);
        }
        trueBody.add(ins(AVM2Instructions.SetProperty, abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, abc.constants.getNamespaceSetId(new NamespaceSet(nss), true), 0, new ArrayList<>()), true)));
        forBody.add(ins(AVM2Instructions.IfFalse, insToBytes(trueBody).length));
        forBody.addAll(trueBody);
        forBody.add(ins(AVM2Instructions.PopScope));
        localData.scopeStack.remove(localData.scopeStack.size() - 1);
        forBody.addAll(toInsList(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(tempVal2, tempVal1))));

        int forBodyLen = insToBytes(forBody).length;
        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, forBodyLen);
        ret.add(forwardJump);

        List<AVM2Instruction> expr = new ArrayList<>();
        expr.add(ins(AVM2Instructions.HasNext2, collectionReg.getVal(), counterReg.getVal()));
        AVM2Instruction backIf = ins(AVM2Instructions.IfTrue, 0);
        expr.add(backIf);

        int exprLen = insToBytes(expr).length;
        backIf.operands[0] = -(exprLen + forBodyLen);

        ret.addAll(forBody);
        ret.addAll(expr);
        ret.addAll(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(collectionReg, counterReg)));
        ret.addAll(AssignableAVM2Item.getTemp(localData, this, xmlListReg));
        ret.addAll(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(xmlListReg)));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, IfItem item) throws CompilationException {
        return generateIf(localData, item.expression, item.onTrue, item.onFalse, false);
    }

    private void fixSwitch(List<AVM2Instruction> code, int breakOffset, long loopId) {
        fixLoop(code, breakOffset, Integer.MAX_VALUE, loopId);
    }

    private void fixLoop(List<AVM2Instruction> code, int breakOffset, int continueOffset, long loopId) {
        int pos = 0;
        for (int a = 0; a < code.size(); a++) {
            AVM2Instruction ins = code.get(a);
            pos += ins.getBytesLength();
            if (ins.definition instanceof JumpIns) {
                if (ins.definition instanceof ContinueJumpIns) {
                    if (continueOffset != Integer.MAX_VALUE) {
                        ins.operands[0] = (-pos + continueOffset);
                        ins.definition = AVM2Code.instructionSet[AVM2Instructions.Jump];
                    }
                }
                if (ins.definition instanceof BreakJumpIns) {
                    ins.operands[0] = (-pos + breakOffset);
                    ins.definition = AVM2Code.instructionSet[AVM2Instructions.Jump];
                }
            }
        }
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TernarOpItem item) throws CompilationException {
        List<GraphTargetItem> onTrue = new ArrayList<>();
        onTrue.add(item.onTrue);
        List<GraphTargetItem> onFalse = new ArrayList<>();
        onFalse.add(item.onFalse);
        return generateIf(localData, item.expression, onTrue, onFalse, true);
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, WhileItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> whileExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>(item.expression);
        GraphTargetItem lastItem = null;
        if (!ex.isEmpty()) {
            lastItem = ex.remove(ex.size() - 1);
            while (lastItem instanceof CommaExpressionItem) {
                CommaExpressionItem cei = (CommaExpressionItem) lastItem;
                ex.addAll(cei.commands);
                lastItem = ex.remove(ex.size() - 1);
            }
            whileExpr.addAll(generateToInsList(localData, ex));
        }
        List<AVM2Instruction> whileBody = generateToInsList(localData, item.commands);
        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, 0);
        ret.add(forwardJump);
        whileBody.add(0, ins(AVM2Instructions.Label));
        ret.addAll(whileBody);
        int whileBodyLen = insToBytes(whileBody).length;
        forwardJump.operands[0] = whileBodyLen;
        whileExpr.addAll(toInsList(condition(localData, lastItem, 0)));
        int whileExprLen = insToBytes(whileExpr).length;
        whileExpr.get(whileExpr.size() - 1).operands[0] = -(whileExprLen + whileBodyLen); //Assuming last is if instruction
        ret.addAll(whileExpr);
        fixLoop(whileBody, whileBodyLen + whileExprLen, whileBodyLen, item.loop.id);
        return ret;
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForEachInAVM2Item item) throws CompilationException {
        return generateForIn(localData, item.loop, item.expression.collection, (AssignableAVM2Item) item.expression.object, item.commands, true);
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForInAVM2Item item) throws CompilationException {
        return generateForIn(localData, item.loop, item.expression.collection, (AssignableAVM2Item) item.expression.object, item.commands, false);
    }

    public List<GraphSourceItem> generateForIn(SourceGeneratorLocalData localData, Loop loop, GraphTargetItem collection, AssignableAVM2Item assignable, List<GraphTargetItem> commands, final boolean each) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        final Reference<Integer> counterReg = new Reference<>(0);
        final Reference<Integer> collectionReg = new Reference<>(0);

        if (assignable instanceof UnresolvedAVM2Item) {
            assignable = (AssignableAVM2Item) ((UnresolvedAVM2Item) assignable).resolved;
        }

        ret.addAll(GraphTargetItem.toSourceMerge(localData, this,
                ins(AVM2Instructions.PushByte, 0),
                AssignableAVM2Item.setTemp(localData, this, counterReg),
                collection,
                NameAVM2Item.generateCoerce(localData, this, TypeItem.UNBOUNDED),
                AssignableAVM2Item.setTemp(localData, this, collectionReg)
        ));

        GraphTargetItem assigned = new GraphTargetItem() {

            @Override
            public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
                return null;
            }

            @Override
            public boolean hasReturnValue() {
                return true;
            }

            @Override
            public GraphTargetItem returnType() {
                return TypeItem.UNBOUNDED;
            }

            @Override
            public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
                return toSourceMerge(localData, generator,
                        AssignableAVM2Item.getTemp(localData, generator, collectionReg),
                        AssignableAVM2Item.getTemp(localData, generator, counterReg),
                        ins(each ? AVM2Instructions.NextValue : AVM2Instructions.NextName)
                );
            }
        };
        assignable.setAssignedValue(assigned);

        List<AVM2Instruction> forBody = toInsList(GraphTargetItem.toSourceMerge(localData, this,
                ins(AVM2Instructions.Label),
                assignable.toSourceIgnoreReturnValue(localData, this)
        ));

        forBody.addAll(generateToInsList(localData, commands));
        int forBodyLen = insToBytes(forBody).length;

        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, forBodyLen);
        ret.add(forwardJump);

        List<AVM2Instruction> expr = new ArrayList<>();
        expr.add(ins(AVM2Instructions.HasNext2, collectionReg.getVal(), counterReg.getVal()));
        AVM2Instruction backIf = ins(AVM2Instructions.IfTrue, 0);
        expr.add(backIf);

        int exprLen = insToBytes(expr).length;
        backIf.operands[0] = -(exprLen + forBodyLen);

        fixLoop(forBody, forBodyLen + exprLen, forBodyLen, loop.id);
        ret.addAll(forBody);
        ret.addAll(expr);
        ret.addAll(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(collectionReg, counterReg)));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DoWhileItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> whileExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>(item.expression);
        GraphTargetItem lastItem = null;
        if (!ex.isEmpty()) {
            lastItem = ex.remove(ex.size() - 1);
            while (lastItem instanceof CommaExpressionItem) {
                CommaExpressionItem cei = (CommaExpressionItem) lastItem;
                ex.addAll(cei.commands);
                lastItem = ex.remove(ex.size() - 1);
            }
            whileExpr.addAll(generateToInsList(localData, ex));
        }
        List<AVM2Instruction> dowhileBody = generateToInsList(localData, item.commands);
        List<AVM2Instruction> labelBody = new ArrayList<>();
        labelBody.add(ins(AVM2Instructions.Label));
        int labelBodyLen = insToBytes(labelBody).length;

        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, labelBodyLen);
        ret.add(forwardJump);
        ret.addAll(labelBody);
        ret.addAll(dowhileBody);
        int dowhileBodyLen = insToBytes(dowhileBody).length;
        whileExpr.addAll(toInsList(condition(localData, lastItem, 0)));
        int dowhileExprLen = insToBytes(whileExpr).length;
        whileExpr.get(whileExpr.size() - 1).operands[0] = -(dowhileExprLen + dowhileBodyLen + labelBodyLen); //Assuming last is if instruction
        ret.addAll(whileExpr);
        fixLoop(dowhileBody, dowhileBodyLen + dowhileExprLen, dowhileBodyLen, item.loop.id);
        return ret;
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, WithAVM2Item item) throws CompilationException {

        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(item.scope.toSource(localData, this));
        Reference<Integer> tempReg = new Reference<>(0);
        ret.addAll(AssignableAVM2Item.dupSetTemp(localData, this, tempReg));
        localData.scopeStack.add(new WithObjectAVM2Item(null, new LocalRegAVM2Item(null, tempReg.getVal(), null)));
        ret.add(ins(AVM2Instructions.PushWith));
        ret.addAll(generate(localData, item.items));
        ret.add(ins(AVM2Instructions.PopScope));
        ret.addAll(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(tempReg)));
        localData.scopeStack.remove(localData.scopeStack.size() - 1);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ForItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        List<AVM2Instruction> forExpr = new ArrayList<>();

        List<GraphTargetItem> ex = new ArrayList<>();
        if (item.expression != null) {
            ex.add(item.expression);
        } else {
            ex.add(new BooleanAVM2Item(null, true));
        }
        GraphTargetItem lastItem = null;
        if (!ex.isEmpty()) {
            lastItem = ex.remove(ex.size() - 1);
            while (lastItem instanceof CommaExpressionItem) {
                CommaExpressionItem cei = (CommaExpressionItem) lastItem;
                ex.addAll(cei.commands);
                lastItem = ex.remove(ex.size() - 1);
            }
            forExpr.addAll(generateToInsList(localData, ex));
        }
        List<AVM2Instruction> forBody = generateToInsList(localData, item.commands);
        List<AVM2Instruction> forFinalCommands = generateToInsList(localData, item.finalCommands);

        ret.addAll(generateToInsList(localData, item.firstCommands));

        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, 0);
        ret.add(forwardJump);
        forBody.add(0, ins(AVM2Instructions.Label));
        ret.addAll(forBody);
        ret.addAll(forFinalCommands);
        int forBodyLen = insToBytes(forBody).length;
        int forFinalCLen = insToBytes(forFinalCommands).length;
        forwardJump.operands[0] = forBodyLen + forFinalCLen;
        forExpr.addAll(toInsList(condition(localData, lastItem, 0)));
        int forExprLen = insToBytes(forExpr).length;
        forExpr.get(forExpr.size() - 1).operands[0] = -(forExprLen + forBodyLen + forFinalCLen); //Assuming last is if instruction
        ret.addAll(forExpr);
        fixLoop(forBody, forBodyLen + forFinalCLen + forExprLen, forBodyLen, item.loop.id);
        return ret;
    }

    private long uniqLast = 0;

    public String uniqId() {
        uniqLast++;
        return "" + uniqLast;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, SwitchItem item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        Reference<Integer> switchedReg = new Reference<>(0);
        AVM2Instruction forwardJump = ins(AVM2Instructions.Jump, 0);
        ret.add(forwardJump);

        List<AVM2Instruction> cases = new ArrayList<>();
        cases.addAll(toInsList(new IntegerValueAVM2Item(null, (long) item.caseValues.size()).toSource(localData, this)));
        int cLen = insToBytes(cases).length;
        List<AVM2Instruction> caseLast = new ArrayList<>();
        caseLast.add(0, ins(AVM2Instructions.Jump, cLen));
        caseLast.addAll(0, toInsList(new IntegerValueAVM2Item(null, (long) item.caseValues.size()).toSource(localData, this)));
        int cLastLen = insToBytes(caseLast).length;
        caseLast.add(0, ins(AVM2Instructions.Jump, cLastLen));
        cases.addAll(0, caseLast);

        List<AVM2Instruction> preCases = new ArrayList<>();
        preCases.addAll(toInsList(item.switchedObject.toSource(localData, this)));
        preCases.addAll(toInsList(AssignableAVM2Item.setTemp(localData, this, switchedReg)));

        for (int i = item.caseValues.size() - 1; i >= 0; i--) {
            List<AVM2Instruction> sub = new ArrayList<>();
            sub.addAll(toInsList(new IntegerValueAVM2Item(null, (long) i).toSource(localData, this)));
            sub.add(ins(AVM2Instructions.Jump, insToBytes(cases).length));
            int subLen = insToBytes(sub).length;

            cases.addAll(0, sub);
            cases.add(0, ins(AVM2Instructions.IfStrictNe, subLen));
            cases.addAll(0, toInsList(AssignableAVM2Item.getTemp(localData, this, switchedReg)));
            cases.addAll(0, toInsList(item.caseValues.get(i).toSource(localData, this)));
        }
        cases.addAll(0, preCases);

        AVM2Instruction lookupOp = new AVM2Instruction(0, AVM2Instructions.LookupSwitch, new int[item.caseValues.size() + 1 + 1 + 1]);
        cases.addAll(toInsList(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(switchedReg))));
        List<AVM2Instruction> bodies = new ArrayList<>();
        List<Integer> bodiesOffsets = new ArrayList<>();
        int defOffset;
        int casesLen = insToBytes(cases).length;
        bodies.addAll(generateToInsList(localData, item.defaultCommands));
        bodies.add(0, ins(AVM2Instructions.Label));
        bodies.add(ins(new BreakJumpIns(item.loop.id), 0));  //There could be two breaks when default clause ends with break, but official compiler does this too, so who cares...
        defOffset = -(insToBytes(bodies).length + casesLen);
        for (int i = item.caseCommands.size() - 1; i >= 0; i--) {
            bodies.addAll(0, generateToInsList(localData, item.caseCommands.get(i)));
            bodies.add(0, ins(AVM2Instructions.Label));
            bodiesOffsets.add(0, -(insToBytes(bodies).length + casesLen));
        }
        lookupOp.operands[0] = defOffset;
        lookupOp.operands[1] = item.valuesMapping.size();
        lookupOp.operands[2 + item.caseValues.size()] = defOffset;
        for (int i = 0; i < item.valuesMapping.size(); i++) {
            lookupOp.operands[2 + i] = bodiesOffsets.get(item.valuesMapping.get(i));
        }

        forwardJump.operands[0] = insToBytes(bodies).length;
        ret.addAll(bodies);
        ret.addAll(cases);
        ret.add(lookupOp);
        fixSwitch(toInsList(ret), insToBytes(toInsList(ret)).length, uniqLast);
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, NotItem item) throws CompilationException {
        /*if (item.getOriginal() instanceof Inverted) {
         GraphTargetItem norig = ((Inverted) item).invert();
         return norig.toSource(localData, this);
         }*/
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(item.getOriginal().toSource(localData, this));
        ret.add(ins(AVM2Instructions.Not));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, DuplicateItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.add(ins(AVM2Instructions.Dup));
        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, BreakItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        AVM2Instruction abreak = ins(new BreakJumpIns(item.loopId), 0);
        ret.add(abreak);
        return ret;
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, FunctionAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        int scope = 0;
        if (!item.functionName.isEmpty()) {
            ret.add(ins(AVM2Instructions.NewObject, 0));
            ret.add(ins(AVM2Instructions.PushWith));
            scope = localData.scopeStack.size();
            localData.scopeStack.add(new PropertyAVM2Item(null, item.functionName, abc, allABCs, new ArrayList<>(), localData.callStack));
        }
        ret.add(ins(AVM2Instructions.NewFunction, method(true, false, localData.callStack, localData.pkg, item.needsActivation, item.subvariables, 0 /*Set later*/, item.hasRest, item.line, localData.currentClass, null, false, localData, item.paramTypes, item.paramNames, item.paramValues, item.body, item.retType)));
        if (!item.functionName.isEmpty()) {
            ret.add(ins(AVM2Instructions.Dup));
            ret.add(ins(AVM2Instructions.GetScopeObject, scope));
            ret.add(ins(AVM2Instructions.Swap));
            ret.add(ins(AVM2Instructions.SetProperty, abc.constants.getMultinameId(new Multiname(Multiname.QNAME, abc.constants.getStringId(item.functionName, true), abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId(localData.pkg, true)), 0, true), 0, 0, new ArrayList<>()), true)));
            ret.add(ins(AVM2Instructions.PopScope));
            localData.scopeStack.remove(localData.scopeStack.size() - 1);
        }
        return ret;
    }

    private static int currentFinId = 1;

    private static int finId() {
        return currentFinId++;
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TryAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();

        boolean newFinallyReg = false;
        List<ABCException> newex = new ArrayList<>();
        int aloneFinallyEx = -1;
        int finallyEx = -1;
        for (NameAVM2Item e : item.catchExceptions2) {
            ABCException aex = new ABCException();
            aex.name_index = abc.constants.getMultinameId(new Multiname(Multiname.QNAME, abc.constants.getStringId(e.getVariableName(), true), abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId("", true)), 0, true), 0, 0, new ArrayList<>()), true);
            aex.type_index = typeName(localData, e.type);
            newex.add(aex);
        }
        int finId = 0;
        if (item.finallyCommands != null) {
            if (item.catchExceptions2.isEmpty()) {
                ABCException aex = new ABCException();
                aex.name_index = 0;
                aex.type_index = 0;
                newex.add(aex);
                aloneFinallyEx = newex.size() - 1;
            }
            ABCException aex = new ABCException();
            aex.name_index = 0;
            aex.type_index = 0;
            newex.add(aex);
            finallyEx = newex.size() - 1;
            if (localData.finallyRegister == -1) {
                localData.finallyRegister = getFreeRegister(localData);
                killRegister(localData, localData.finallyRegister); //reuse for catches
                newFinallyReg = true;
            }
            finId = finId();
        }

        if (finallyEx > -1) {
            localData.finallyCatches.add(finId);
        }
        List<AVM2Instruction> tryCmds = generateToInsList(localData, item.tryCommands);

        //int i = firstId + item.catchCommands.size() - 1;
        List<AVM2Instruction> catches = new ArrayList<>();
        Reference<Integer> tempReg = new Reference<>(0);

        List<Integer> currentExceptionIds = new ArrayList<>();
        List<List<AVM2Instruction>> catchCmds = new ArrayList<>();
        for (int c = 0; c < item.catchCommands.size(); c++) {
            int i = localData.exceptions.size();
            localData.exceptions.add(newex.get(c));

            currentExceptionIds.add(i);

            //Reference<Integer> tempReg=new Reference<>(0);
            List<AVM2Instruction> catchCmd = new ArrayList<>();
            catchCmd.add(ins(AVM2Instructions.NewCatch, i));
            catchCmd.addAll(toInsList(AssignableAVM2Item.dupSetTemp(localData, this, tempReg)));
            catchCmd.add(ins(AVM2Instructions.Dup));
            catchCmd.add(ins(AVM2Instructions.PushScope));
            catchCmd.add(ins(AVM2Instructions.Swap));
            catchCmd.add(ins(AVM2Instructions.SetSlot, 1));

            for (AssignableAVM2Item a : item.catchVariables.get(c)) {
                GraphTargetItem r = a;
                if (r instanceof UnresolvedAVM2Item) {
                    r = ((UnresolvedAVM2Item) r).resolvedRoot;
                }
                if (r instanceof NameAVM2Item) {
                    NameAVM2Item n = (NameAVM2Item) r;
                    if (item.catchExceptions2.get(c).getVariableName().equals(n.getVariableName())) {
                        n.setSlotScope(localData.scopeStack.size());
                    }
                }
            }
            localData.scopeStack.add(new LocalRegAVM2Item(null, tempReg.getVal(), null));
            catchCmd.addAll(generateToInsList(localData, item.catchCommands.get(c)));
            localData.scopeStack.remove(localData.scopeStack.size() - 1);
            catchCmd.add(ins(AVM2Instructions.PopScope));
            catchCmd.addAll(toInsList(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(tempReg))));
            catchCmds.add(catchCmd);
        }
        for (int c = item.catchCommands.size() - 1; c >= 0; c--) {
            List<AVM2Instruction> preCatches = new ArrayList<>();
            /*preCatches.add(ins(AVM2Instructions.GetLocal0));
             preCatches.add(ins(AVM2Instructions.PushScope));
             preCatches.add(AssignableAVM2Item.generateGetLoc(localData.activationReg));
             preCatches.add(ins(AVM2Instructions.PushScope));*/
            for (GraphTargetItem s : localData.scopeStack) {
                preCatches.addAll(toInsList(s.toSource(localData, this)));
                if (s instanceof WithObjectAVM2Item) {
                    preCatches.add(ins(AVM2Instructions.PushWith));
                } else {
                    preCatches.add(ins(AVM2Instructions.PushScope));
                }
            }

            //catchCmds.add(catchCmd);
            preCatches.addAll(catchCmds.get(c));
            catches.addAll(0, preCatches);
            catches.add(0, new ExceptionMarkAVM2Instruction(currentExceptionIds.get(c), MARK_E_TARGET));
            catches.add(0, ins(AVM2Instructions.Jump, insToBytes(catches).length));
        }

        if (aloneFinallyEx > -1) {
            localData.exceptions.add(newex.get(aloneFinallyEx));
            aloneFinallyEx = localData.exceptions.size() - 1;

        }
        if (finallyEx > -1) {
            localData.exceptions.add(newex.get(finallyEx));
            finallyEx = localData.exceptions.size() - 1;
        }

        for (int i : currentExceptionIds) {
            ret.add(new ExceptionMarkAVM2Instruction(i, MARK_E_START));
        }
        if (aloneFinallyEx > -1) {
            ret.add(new ExceptionMarkAVM2Instruction(aloneFinallyEx, MARK_E_START));
        }
        if (finallyEx > -1) {
            ret.add(new ExceptionMarkAVM2Instruction(finallyEx, MARK_E_START));
        }

        ret.addAll(tryCmds);

        for (int i : currentExceptionIds) {
            ret.add(new ExceptionMarkAVM2Instruction(i, MARK_E_END));
        }
        if (aloneFinallyEx > -1) {
            ret.add(new ExceptionMarkAVM2Instruction(aloneFinallyEx, MARK_E_END));
        }

        if (aloneFinallyEx > -1) {
            List<AVM2Instruction> preCatches = new ArrayList<>();
            for (GraphTargetItem s : localData.scopeStack) {
                preCatches.addAll(toInsList(s.toSource(localData, this)));
                if (s instanceof WithObjectAVM2Item) {
                    preCatches.add(ins(AVM2Instructions.PushWith));
                } else {
                    preCatches.add(ins(AVM2Instructions.PushScope));
                }
            }
            preCatches.add(ins(AVM2Instructions.NewCatch, aloneFinallyEx));
            preCatches.addAll(toInsList(AssignableAVM2Item.dupSetTemp(localData, this, tempReg)));
            preCatches.add(ins(AVM2Instructions.PushScope));
            preCatches.add(ins(AVM2Instructions.Throw));
            preCatches.add(ins(AVM2Instructions.PopScope));
            preCatches.addAll(toInsList(AssignableAVM2Item.killTemp(localData, this, Arrays.asList(tempReg))));
            catches.add(ins(AVM2Instructions.Jump, insToBytes(preCatches).length));
            catches.add(new ExceptionMarkAVM2Instruction(aloneFinallyEx, MARK_E_TARGET));
            catches.addAll(preCatches);
        }
        AVM2Instruction finSwitch = null;
        AVM2Instruction pushDefIns = ins(AVM2Instructions.PushByte, 0);

        int defPos = 0;
        if (finallyEx > -1) {
            List<AVM2Instruction> preCatches = new ArrayList<>();
            preCatches.add(0, new ExceptionMarkAVM2Instruction(finallyEx, MARK_E_TARGET));
            for (GraphTargetItem s : localData.scopeStack) {
                preCatches.addAll(toInsList(s.toSource(localData, this)));
                if (s instanceof WithObjectAVM2Item) {
                    preCatches.add(ins(AVM2Instructions.PushWith));
                } else {
                    preCatches.add(ins(AVM2Instructions.PushScope));
                }
            }
            preCatches.add(ins(AVM2Instructions.NewCatch, finallyEx));
            preCatches.addAll(toInsList(AssignableAVM2Item.dupSetTemp(localData, this, tempReg)));
            preCatches.add(ins(AVM2Instructions.PushScope));
            preCatches.add(ins(AVM2Instructions.PopScope));
            Reference<Integer> tempReg2 = new Reference<>(0);
            preCatches.add(ins(AVM2Instructions.Kill, tempReg.getVal()));
            preCatches.add(ins(AVM2Instructions.CoerceA));
            preCatches.addAll(toInsList(AssignableAVM2Item.setTemp(localData, this, tempReg2)));
            preCatches.add(pushDefIns);

            List<AVM2Instruction> finallySwitchCmds = new ArrayList<>();

            finSwitch = new AVM2Instruction(0, AVM2Instructions.LookupSwitch, new int[1 + 1 + 1]);
            finSwitch.operands[0] = finSwitch.getBytesLength();
            finSwitch.operands[1] = 0; //switch cnt

            List<AVM2Instruction> preFinallySwitch = new ArrayList<>();
            preFinallySwitch.add(ins(AVM2Instructions.Label));
            preFinallySwitch.add(ins(AVM2Instructions.Pop));
            int preFinallySwitchLen = insToBytes(preFinallySwitch).length;

            finallySwitchCmds.add(ins(AVM2Instructions.Label));
            finallySwitchCmds.addAll(toInsList(AssignableAVM2Item.getTemp(localData, this, tempReg2)));
            finallySwitchCmds.add(ins(AVM2Instructions.Kill, tempReg2.getVal()));
            finallySwitchCmds.add(ins(AVM2Instructions.Throw));
            finallySwitchCmds.add(ins(AVM2Instructions.PushByte, 255));
            finallySwitchCmds.add(ins(AVM2Instructions.PopScope));
            finallySwitchCmds.add(ins(AVM2Instructions.Kill, tempReg.getVal()));

            int finSwitchLen = insToBytes(finallySwitchCmds).length;

            preCatches.add(ins(AVM2Instructions.Jump, preFinallySwitchLen + finSwitchLen));
            AVM2Instruction fjump = ins(AVM2Instructions.Jump, 0);
            fjump.operands[0] = insToBytes(preCatches).length + preFinallySwitchLen + finSwitchLen;

            preCatches.add(0, fjump);
            preCatches.add(0, new ExceptionMarkAVM2Instruction(finallyEx, MARK_E_END));
            preCatches.add(0, ins(AVM2Instructions.PushByte, 255));

            finallySwitchCmds.add(new ExceptionMarkAVM2Instruction(finallyEx, MARK_E_FINALLYPART));

            int oldReg = localData.finallyRegister;
            localData.finallyRegister = getFreeRegister(localData);
            Integer cnt = localData.finallyCounter.get(finId);
            if (cnt == null) {
                cnt = -1;
            }
            defPos = cnt;
            cnt++; //Skip default clause (throw)
            localData.finallyCounter.put(finId, cnt);
            finallySwitchCmds.addAll(generateToInsList(localData, item.finallyCommands));
            killRegister(localData, localData.finallyRegister);
            localData.finallyRegister = oldReg;
            finSwitchLen = insToBytes(finallySwitchCmds).length;

            finSwitch.operands[2] = -finSwitchLen;
            preCatches.addAll(preFinallySwitch);
            preCatches.addAll(finallySwitchCmds);
            preCatches.add(finSwitch);

            catches.addAll(preCatches);
            AssignableAVM2Item.killTemp(localData, this, Arrays.asList(tempReg, tempReg2));
        }

        ret.addAll(catches);
        //localData.exceptions.addAll(newex);

        if (finallyEx > -1) {
            localData.finallyCatches.remove(localData.finallyCatches.size() - 1);
        }
        if (newFinallyReg) {
            localData.finallyRegister = -1;
            killRegister(localData, localData.finallyRegister);
        }
        int pos = 0;
        int finallyPos = 0;
        int switchPos = 0;
        for (int s = 0; s < ret.size(); s++) {
            GraphSourceItem src = ret.get(s);
            if (src == finSwitch) {
                switchPos = pos;
            }
            if (src instanceof AVM2Instruction) {
                AVM2Instruction ins = (AVM2Instruction) src;
                if (ins instanceof ExceptionMarkAVM2Instruction) {
                    ExceptionMarkAVM2Instruction em = (ExceptionMarkAVM2Instruction) ins;
                    if (em.exceptionId == finallyEx && em.markType == MARK_E_FINALLYPART) {
                        finallyPos = pos;
                        ret.remove(s);
                        s--;
                        continue;
                    }
                }
                pos += ins.getBytesLength();
            }

        }

        if (finSwitch != null) {
            pos = 0;
            int defLoc = finSwitch.operands[2];
            List<Integer> switchLoc = new ArrayList<>();
            boolean wasDef = false;
            for (int s = 0; s < ret.size(); s++) {
                GraphSourceItem src = ret.get(s);
                if (src instanceof AVM2Instruction) {
                    AVM2Instruction ins = (AVM2Instruction) src;
                    if (ins.definition instanceof FinallyJumpIns) {
                        FinallyJumpIns fji = (FinallyJumpIns) ins.definition;
                        if (fji.getClauseId() == finId) {
                            List<AVM2Instruction> bet = new ArrayList<>();
                            bet.add(ins(AVM2Instructions.Label));
                            bet.add(ins(AVM2Instructions.Pop));
                            int betLen = insToBytes(bet).length;
                            if (wasDef) {
                                ins.operands[0] = 0;
                            } else {
                                ins.operands[0] = finallyPos - (pos + ins.getBytesLength());
                            }
                            ins.definition = AVM2Code.instructionSet[AVM2Instructions.Jump];
                            switchLoc.add(pos + ins.getBytesLength() + betLen - switchPos);
                        }
                    }
                    pos += ins.getBytesLength();
                }
                if (defPos == switchLoc.size() - 1) {
                    switchLoc.add(defLoc);
                    wasDef = true;
                }
            }
            finSwitch.operands = new int[1 + 1 + switchLoc.size()];
            pushDefIns.operands[0] = defPos + 1;
            int afterLoc = finSwitch.getBytesLength();
            finSwitch.operands[0] = afterLoc;
            finSwitch.operands[1] = switchLoc.size() - 1;
            for (int j = 0; j < switchLoc.size(); j++) {
                finSwitch.operands[2 + j] = switchLoc.get(j);
            }
        }

        return ret;
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ContinueItem item) {
        List<GraphSourceItem> ret = new ArrayList<>();
        AVM2Instruction acontinue = ins(new ContinueJumpIns(item.loopId), 0);
        ret.add(acontinue);
        return ret;
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ReturnValueAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(item.value.toSource(localData, this));
        if (!localData.finallyCatches.isEmpty()) {
            ret.add(ins(AVM2Instructions.CoerceA));
            ret.add(AssignableAVM2Item.generateSetLoc(localData.finallyRegister));
            for (int i = localData.finallyCatches.size() - 1; i >= 0; i--) {
                if (i < localData.finallyCatches.size() - 1) {
                    ret.add(ins(AVM2Instructions.Label));
                }
                int clauseId = localData.finallyCatches.get(i);
                Integer cnt = localData.finallyCounter.get(clauseId);
                if (cnt == null) {
                    cnt = -1;
                }
                cnt++;
                localData.finallyCounter.put(clauseId, cnt);
                ret.addAll(new IntegerValueAVM2Item(null, (long) cnt).toSource(localData, this));
                ret.add(ins(new FinallyJumpIns(clauseId), 0));
                ret.add(ins(AVM2Instructions.Label));
                ret.add(ins(AVM2Instructions.Pop));
            }
            ret.add(ins(AVM2Instructions.Label));
            ret.add(AssignableAVM2Item.generateGetLoc(localData.finallyRegister));
            ret.add(ins(AVM2Instructions.Kill, localData.finallyRegister));
        }
        ret.add(ins(AVM2Instructions.ReturnValue));
        return ret;
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ReturnVoidAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        if (!localData.finallyCatches.isEmpty()) {

            for (int i = 0; i < localData.finallyCatches.size(); i++) {
                if (i > 0) {
                    ret.add(ins(AVM2Instructions.Label));
                }
                int clauseId = localData.finallyCatches.get(i);
                Integer cnt = localData.finallyCounter.get(clauseId);
                if (cnt == null) {
                    cnt = -1;
                }
                cnt++;
                localData.finallyCounter.put(clauseId, cnt);
                ret.addAll(new IntegerValueAVM2Item(null, (long) cnt).toSource(localData, this));
                ret.add(ins(new FinallyJumpIns(clauseId), 0));
                ret.add(ins(AVM2Instructions.Label));
                ret.add(ins(AVM2Instructions.Pop));
            }
            ret.add(ins(AVM2Instructions.Label));
        }
        ret.add(ins(AVM2Instructions.ReturnVoid));
        return ret;
    }

    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, ThrowAVM2Item item) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(item.value.toSource(localData, this));
        ret.add(ins(AVM2Instructions.Throw));
        return ret;
    }

    private List<AVM2Instruction> generateToInsList(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) throws CompilationException {
        return toInsList(generate(localData, commands));
    }

    private List<AVM2Instruction> generateToActionList(SourceGeneratorLocalData localData, GraphTargetItem command) throws CompilationException {
        return toInsList(command.toSource(localData, this));
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, List<GraphTargetItem> commands) throws CompilationException {
        List<GraphSourceItem> ret = new ArrayList<>();
        for (GraphTargetItem item : commands) {
            ret.addAll(item.toSourceIgnoreReturnValue(localData, this));
        }
        return ret;
    }

    public HashMap<String, Integer> getRegisterVars(SourceGeneratorLocalData localData) {
        return localData.registerVars;
    }

    public void setRegisterVars(SourceGeneratorLocalData localData, HashMap<String, Integer> value) {
        localData.registerVars = value;
    }

    public void setInFunction(SourceGeneratorLocalData localData, int value) {
        localData.inFunction = value;
    }

    public int isInFunction(SourceGeneratorLocalData localData) {
        return localData.inFunction;
    }

    public boolean isInMethod(SourceGeneratorLocalData localData) {
        return localData.inMethod;
    }

    public void setInMethod(SourceGeneratorLocalData localData, boolean value) {
        localData.inMethod = value;
    }

    public int getForInLevel(SourceGeneratorLocalData localData) {
        return localData.forInLevel;
    }

    public void setForInLevel(SourceGeneratorLocalData localData, int value) {
        localData.forInLevel = value;
    }

    public int getTempRegister(SourceGeneratorLocalData localData) {
        HashMap<String, Integer> registerVars = getRegisterVars(localData);
        int tmpReg = 0;
        for (int i = 0; i < 256; i++) {
            if (!registerVars.containsValue(i)) {
                tmpReg = i;
                break;
            }
        }
        return tmpReg;
    }

    public AVM2SourceGenerator(ABC abc, List<ABC> allABCs) {
        this.abc = abc;
        this.allABCs = allABCs;
    }

    public ABC getABC() {
        return abc;
    }

    public void generateClass(List<DottedChain> importedClasses, List<AssignableAVM2Item> sinitVariables, boolean staticNeedsActivation, List<GraphTargetItem> staticInit, List<Integer> openedNamespaces, int namespace, int initScope, DottedChain pkg, ClassInfo classInfo, InstanceInfo instanceInfo, SourceGeneratorLocalData localData, boolean isInterface, String name, String superName, GraphTargetItem extendsVal, List<GraphTargetItem> implementsStr, GraphTargetItem constructor, List<GraphTargetItem> traitItems, Reference<Integer> class_index) throws AVM2ParseException, CompilationException {
        localData.currentClass = name;
        localData.pkg = pkg;
        if (extendsVal == null && !isInterface) {
            extendsVal = new TypeItem(DottedChain.OBJECT);
        }
        ParsedSymbol s = null;

        instanceInfo.name_index = traitName(namespace, name);

        Trait[] it = generateTraitsPhase1(name, superName, false, localData, traitItems, instanceInfo.instance_traits, class_index);
        Trait[] st = generateTraitsPhase1(name, superName, true, localData, traitItems, classInfo.static_traits, class_index);
        generateTraitsPhase2(importedClasses, pkg, traitItems, it, openedNamespaces, localData);
        generateTraitsPhase2(importedClasses, pkg, traitItems, st, openedNamespaces, localData);
        generateTraitsPhase3(initScope, isInterface, name, superName, false, localData, traitItems, instanceInfo.instance_traits, it, new HashMap<>(), class_index);
        generateTraitsPhase3(initScope, isInterface, name, superName, true, localData, traitItems, classInfo.static_traits, st, new HashMap<>(), class_index);
        int init = 0;
        if (constructor == null || isInterface) {
            instanceInfo.iinit_index = init = method(false, isInterface, new ArrayList<>(), pkg, false, new ArrayList<>(), initScope + 1, false, 0, isInterface ? null : name, extendsVal != null ? extendsVal.toString() : null, true, localData, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), TypeItem.UNBOUNDED/*?? FIXME*/);
        } else {
            MethodAVM2Item m = (MethodAVM2Item) constructor;
            instanceInfo.iinit_index = init = method(false, false, new ArrayList<>(), pkg, m.needsActivation, m.subvariables, initScope + 1, m.hasRest, m.line, name, extendsVal != null ? extendsVal.toString() : null, true, localData, m.paramTypes, m.paramNames, m.paramValues, m.body, TypeItem.UNBOUNDED/*?? FIXME*/);
        }

        //Class initializer
        int staticMi = method(false, false, new ArrayList<>(), pkg, staticNeedsActivation, sinitVariables, initScope + (implementsStr.isEmpty() ? 0 : 1), false, 0, isInterface ? null : name, superName, false, localData, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), staticInit, TypeItem.UNBOUNDED);
        MethodBody sinitBody = abc.findBody(staticMi);

        List<AVM2Instruction> sinitcode = new ArrayList<>();
        List<AVM2Instruction> initcode = new ArrayList<>();
        for (GraphTargetItem ti : traitItems) {
            if ((ti instanceof SlotAVM2Item) || (ti instanceof ConstAVM2Item)) {
                GraphTargetItem val = null;
                boolean isStatic = false;
                int ns = -1;
                String tname = null;
                boolean isConst = false;
                if (ti instanceof SlotAVM2Item) {
                    val = ((SlotAVM2Item) ti).value;
                    isStatic = ((SlotAVM2Item) ti).isStatic();
                    ns = ((SlotAVM2Item) ti).getNamespace();
                    tname = ((SlotAVM2Item) ti).var;
                }
                if (ti instanceof ConstAVM2Item) {
                    val = ((ConstAVM2Item) ti).value;
                    isStatic = ((ConstAVM2Item) ti).isStatic();
                    ns = ((ConstAVM2Item) ti).getNamespace();
                    tname = ((ConstAVM2Item) ti).var;
                    isConst = true;
                }
                if (isStatic && val != null) {
                    sinitcode.add(ins(AVM2Instructions.FindProperty, traitName(ns, tname)));
                    sinitcode.addAll(toInsList(val.toSource(localData, this)));
                    sinitcode.add(ins(isConst ? AVM2Instructions.InitProperty : AVM2Instructions.SetProperty, traitName(ns, tname)));
                }
                if (!isStatic && val != null) {
                    //do not init basic values, that can be stored in trait
                    if (!(val instanceof IntegerValueAVM2Item) && !(val instanceof StringAVM2Item) && !(val instanceof BooleanAVM2Item) && !(val instanceof NullAVM2Item) && !(val instanceof UndefinedAVM2Item)) {
                        initcode.add(ins(AVM2Instructions.GetLocal0));
                        initcode.addAll(toInsList(val.toSource(localData, this)));
                        initcode.add(ins(isConst ? AVM2Instructions.InitProperty : AVM2Instructions.SetProperty, traitName(ns, tname)));
                    }
                }
            }
        }
        MethodBody initBody = null;
        if (!isInterface) {
            initBody = abc.findBody(init);
            initBody.getCode().code.addAll(constructor == null ? 0 : 2, initcode);//after getlocal0,pushscope

            if (sinitBody.getCode().code.get(sinitBody.getCode().code.size() - 1).definition instanceof ReturnVoidIns) {
                sinitBody.getCode().code.addAll(2, sinitcode); //after getlocal0,pushscope
            }
        }
        sinitBody.markOffsets();
        sinitBody.autoFillStats(abc, initScope + (implementsStr.isEmpty() ? 0 : 1), true);

        classInfo.cinit_index = staticMi;
        if (!isInterface) {
            initBody.autoFillStats(abc, initScope + 1, true);
        }
        instanceInfo.interfaces = new int[implementsStr.size()];
        for (int i = 0; i < implementsStr.size(); i++) {
            instanceInfo.interfaces[i] = superIntName(localData, implementsStr.get(i));
        }
    }

    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, CommaExpressionItem item) throws CompilationException {
        if (item.commands.isEmpty()) {
            return new ArrayList<>();
        }

        //We need to handle commands and last expression separately, otherwise last expression result will be popped
        List<GraphTargetItem> cmds = new ArrayList<>(item.commands);
        GraphTargetItem lastExpr = cmds.remove(cmds.size() - 1);
        List<GraphSourceItem> ret = new ArrayList<>();
        ret.addAll(generate(localData, cmds));
        ret.addAll(lastExpr.toSource(localData, this));
        return ret;
    }

    public int generateClass(int namespace, ClassInfo ci, InstanceInfo ii, int initScope, DottedChain pkg, SourceGeneratorLocalData localData, AVM2Item cls, Reference<Integer> class_index) throws AVM2ParseException, CompilationException {
        /*ClassInfo ci = new ClassInfo();
         InstanceInfo ii = new InstanceInfo();
         abc.class_info.add(ci);
         abc.instance_info.add(ii);
         */
        if (cls instanceof ClassAVM2Item) {
            ClassAVM2Item cai = (ClassAVM2Item) cls;
            generateClass(cai.importedClasses, cai.sinitVariables, cai.staticInitActivation, cai.staticInit, cai.openedNamespaces, namespace, initScope, pkg, ci, ii, localData, false, cai.className, cai.extendsOp == null ? "Object" : cai.extendsOp.toString(), cai.extendsOp, cai.implementsOp, cai.constructor, cai.traits, class_index);
            if (!cai.isDynamic) {
                ii.flags |= InstanceInfo.CLASS_SEALED;
            }
            if (cai.isFinal) {
                ii.flags |= InstanceInfo.CLASS_FINAL;
            }
            ii.flags |= InstanceInfo.CLASS_PROTECTEDNS;
            ii.protectedNS = cai.protectedNs;
        }
        if (cls instanceof InterfaceAVM2Item) {
            InterfaceAVM2Item iai = (InterfaceAVM2Item) cls;
            ii.flags |= InstanceInfo.CLASS_INTERFACE;
            ii.flags |= InstanceInfo.CLASS_SEALED;
            generateClass(iai.importedClasses, new ArrayList<>(), false, new ArrayList<>(), iai.openedNamespaces, namespace, initScope, pkg, ci, ii, localData, true, iai.name, null, null, iai.superInterfaces, null, iai.methods, class_index);
        }

        return abc.instance_info.size() - 1;
    }

    public int traitName(int namespace, String var) {
        return abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str(var), namespace, 0, 0, new ArrayList<>()), true);
    }

    public int typeName(SourceGeneratorLocalData localData, GraphTargetItem type) throws CompilationException {
        if (type instanceof UnboundedTypeItem) {
            return 0;
        }
        if (("" + type).equals("*")) {
            return 0;
        }

        return resolveType(localData, type, abc, allABCs);
        /*
         TypeItem nameItem = (TypeItem) type;
         name = nameItem.fullTypeName;
         if (name.contains(".")) {
         pkg = name.substring(0, name.lastIndexOf('.'));
         name = name.substring(name.lastIndexOf('.') + 1);
         }
         if (!nameItem.subtypes.isEmpty()) { //It's vector => TypeName
         List<Integer> params = new ArrayList<>();
         for (GraphTargetItem p : nameItem.subtypes) {
         params.add(typeName(localData, p));//abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str(p), namespace(Namespace.KIND_PACKAGE, ppkg), 0, 0, new ArrayList<Integer>()), true));
         }
         int qname = abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str(name), namespace(Namespace.KIND_PACKAGE, pkg), 0, 0, new ArrayList<Integer>()), true);
         return abc.constants.getMultinameId(new Multiname(Multiname.TYPENAME, 0, 0, 0, qname, params), true);
         } else {
         return abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str(name), namespace(Namespace.KIND_PACKAGE, pkg), 0, 0, new ArrayList<Integer>()), true);
         }*/
    }

    public int ident(GraphTargetItem name) {
        if (name instanceof NameAVM2Item) {
            return str(((NameAVM2Item) name).getVariableName());
        }
        throw new RuntimeException("no ident"); //FIXME
    }

    public int namespace(int nsKind, String name) {
        return abc.constants.getNamespaceId(new Namespace(nsKind, str(name)), 0, true);
    }

    public int str(String name) {
        return abc.constants.getStringId(name, true);
    }

    public int propertyName(GraphTargetItem name) {
        if (name instanceof NameAVM2Item) {
            NameAVM2Item va = (NameAVM2Item) name;
            return abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str(va.getVariableName()), namespace(Namespace.KIND_PACKAGE, ""), 0, 0, new ArrayList<>()), true);
        }
        throw new RuntimeException("no prop"); //FIXME
    }

    public int getFreeRegister(SourceGeneratorLocalData localData) {
        for (int i = 0;; i++) {
            if (!localData.registerVars.containsValue(i)) {
                localData.registerVars.put("__TEMP__" + i, i);
                return i;
            }
        }
    }

    public boolean killRegister(SourceGeneratorLocalData localData, int i) {
        String key = null;
        for (String k : localData.registerVars.keySet()) {
            if (localData.registerVars.get(k) == i) {
                key = k;
                break;
            }
        }
        if (key != null) {
            localData.registerVars.remove(key);
            return true;
        }
        return false;
    }

    public int method(boolean subMethod, boolean isInterface, List<MethodBody> callStack, DottedChain pkg, boolean needsActivation, List<AssignableAVM2Item> subvariables, int initScope, boolean hasRest, int line, String className, String superType, boolean constructor, SourceGeneratorLocalData localData, List<GraphTargetItem> paramTypes, List<String> paramNames, List<GraphTargetItem> paramValues, List<GraphTargetItem> body, GraphTargetItem retType) throws CompilationException {
        //Reference<Boolean> hasArgs = new Reference<>(Boolean.FALSE);
        //calcRegisters(localData,needsActivation,paramNames,subvariables,body, hasArgs);
        SourceGeneratorLocalData newlocalData = new SourceGeneratorLocalData(new HashMap<>(), 1, true, 0);
        newlocalData.currentClass = className;
        newlocalData.pkg = localData.pkg;
        newlocalData.callStack.addAll(localData.callStack);
        newlocalData.traitUsages = localData.traitUsages;
        newlocalData.currentScript = localData.currentScript;
        newlocalData.documentClass = localData.documentClass;
        newlocalData.subMethod = subMethod;
        localData = newlocalData;

        localData.activationReg = 0;

        for (int i = 0; i < subvariables.size(); i++) {
            AssignableAVM2Item an = subvariables.get(i);
            if (an instanceof UnresolvedAVM2Item) {
                UnresolvedAVM2Item n = (UnresolvedAVM2Item) an;
                if (n.resolved == null) {
                    String fullClass = localData.getFullClass();
                    GraphTargetItem res = n.resolve(new TypeItem(fullClass), paramTypes, paramNames, abc, allABCs, callStack, subvariables);
                    if (res instanceof AssignableAVM2Item) {
                        subvariables.set(i, (AssignableAVM2Item) res);
                    } else {
                        subvariables.remove(i);
                        i--;
                    }
                }
            }
        }

        for (int t = 0; t < paramTypes.size(); t++) {
            GraphTargetItem an = paramTypes.get(t);
            if (an instanceof UnresolvedAVM2Item) {
                UnresolvedAVM2Item n = (UnresolvedAVM2Item) an;
                if (n.resolved == null) {
                    String fullClass = localData.getFullClass();
                    GraphTargetItem res = n.resolve(new TypeItem(fullClass), paramTypes, paramNames, abc, allABCs, callStack, subvariables);
                    paramTypes.set(t, res);
                }
            }
        }

        boolean hasArguments = false;
        List<String> slotNames = new ArrayList<>();
        List<String> slotTypes = new ArrayList<>();
        slotNames.add("--first");
        slotTypes.add("-");

        List<String> registerNames = new ArrayList<>();
        List<String> registerTypes = new ArrayList<>();
        if (className != null) {
            String fullClassName = pkg == null || pkg.isEmpty() ? className : pkg.toRawString() + "." + className;
            registerTypes.add(fullClassName);
            localData.scopeStack.add(new LocalRegAVM2Item(null, registerNames.size(), null));
            registerNames.add("this");

        } else {
            registerTypes.add("global");
            registerNames.add("this");
        }
        for (GraphTargetItem t : paramTypes) {
            registerTypes.add(t.toString());
            slotTypes.add(t.toString());
        }
        registerNames.addAll(paramNames);
        slotNames.addAll(paramNames);
        /*for (GraphTargetItem p : paramTypes) {
         slotTypes.add("" + p);
         }*/
        if (hasRest) {
            slotTypes.add("*");
        }
        localData.registerVars.clear();
        for (AssignableAVM2Item an : subvariables) {
            if (an instanceof NameAVM2Item) {
                NameAVM2Item n = (NameAVM2Item) an;
                if (n.getVariableName().equals("arguments") & !n.isDefinition()) {
                    registerNames.add("arguments");
                    registerTypes.add("Object");
                    hasArguments = true;
                    break;
                }
            }
        }
        int paramRegCount = registerNames.size();

        if (needsActivation) {
            registerNames.add("+$activation");
            localData.activationReg = registerNames.size() - 1;
            registerTypes.add("Object");
            localData.scopeStack.add(new LocalRegAVM2Item(null, localData.activationReg, null));
        }

        String mask = Configuration.registerNameFormat.get();
        mask = mask.replace("%d", "([0-9]+)");
        Pattern pat = Pattern.compile(mask);

        //Two rounds
        for (int round = 1; round <= 2; round++) {
            for (AssignableAVM2Item an : subvariables) {
                if (an instanceof NameAVM2Item) {
                    NameAVM2Item n = (NameAVM2Item) an;
                    if (n.isDefinition()) {
                        if (!needsActivation || (n.getSlotScope() <= 0)) {
                            String varName = n.getVariableName();
                            Matcher m = pat.matcher(varName);
                            //In first round, make all register that match standard loc_xx register
                            if ((round == 1) && (m.matches())) {
                                String regIndexStr = m.group(1);
                                int regIndex = Integer.parseInt(regIndexStr);
                                while (registerNames.size() <= regIndex + 1) {
                                    String standardName = String.format(mask, registerNames.size() - 1);
                                    registerNames.add(standardName);
                                    registerTypes.add("*");
                                    slotNames.add(standardName);
                                    slotTypes.add("*");
                                }
                                registerNames.set(regIndex, varName);
                                registerTypes.set(regIndex, varName);
                                slotNames.set(regIndex, varName);
                                slotTypes.set(regIndex, varName);
                            } //in second round the rest
                            else if (round == 2 && !m.matches()) {
                                registerNames.add(n.getVariableName());
                                registerTypes.add(n.type.toString());
                                slotNames.add(n.getVariableName());
                                slotTypes.add(n.type.toString());
                            }
                        }
                    }
                }
            }
        }

        int slotScope = subMethod ? 0 : 1;

        for (AssignableAVM2Item an : subvariables) {
            if (an instanceof NameAVM2Item) {
                NameAVM2Item n = (NameAVM2Item) an;
                String variableName = n.getVariableName();
                if (variableName != null) {
                    boolean isThisOrSuper = variableName.equals("this") || variableName.equals("super");
                    if (!isThisOrSuper && needsActivation) {
                        if (n.getSlotNumber() <= 0) {
                            n.setSlotNumber(slotNames.indexOf(variableName));
                            n.setSlotScope(slotScope);
                        }
                    } else {
                        if (isThisOrSuper) {
                            n.setRegNumber(0);
                        } else {
                            n.setRegNumber(registerNames.indexOf(variableName));
                        }
                    }
                }
            }
        }

        for (int i = 0; i < registerNames.size(); i++) {
            if (needsActivation && i > localData.activationReg) {
                break;
            }
            localData.registerVars.put(registerNames.get(i), i);
        }
        List<NameAVM2Item> declarations = new ArrayList<>();
        loopn:
        for (AssignableAVM2Item an : subvariables) {
            if (an instanceof NameAVM2Item) {
                NameAVM2Item n = (NameAVM2Item) an;

                if (needsActivation) {
                    if (n.getSlotScope() != slotScope) {
                        continue;
                    } else {
                        if (n.getSlotNumber() < paramRegCount) {
                            continue;
                        }
                    }
                }
                for (NameAVM2Item d : declarations) {
                    if (n.getVariableName() != null && n.getVariableName().equals(d.getVariableName())) {
                        continue loopn;
                    }
                }

                for (GraphTargetItem it : body) { //search first level of commands
                    if (it instanceof NameAVM2Item) {
                        NameAVM2Item n2 = (NameAVM2Item) it;
                        if (n2.isDefinition() && n2.getAssignedValue() != null && n2.getVariableName().equals(n.getVariableName())) {
                            continue loopn;
                        }
                        if (!n2.isDefinition() && n2.getVariableName() != null && n2.getVariableName().equals(n.getVariableName())) { //used earlier than defined
                            break;
                        }
                    }
                }
                if (n.unresolved) {
                    continue;
                }
                if (n.redirect != null) {
                    continue;
                }
                if (n.getNs() != null) {
                    continue;
                }

                String variableName = n.getVariableName();
                if ("this".equals(variableName) || "super".equals(variableName) || paramNames.contains(variableName) || "arguments".equals(variableName)) {
                    continue;
                }

                NameAVM2Item d = new NameAVM2Item(n.type, n.line, n.getVariableName(), NameAVM2Item.getDefaultValue("" + n.type), true, n.openedNamespaces);
                //no index
                if (needsActivation) {
                    if (d.getSlotNumber() <= 0) {
                        d.setSlotNumber(n.getSlotNumber());
                        d.setSlotScope(n.getSlotScope());
                    }
                } else {
                    d.setRegNumber(n.getRegNumber());
                }
                declarations.add(d);
            }
        }

        int[] param_types = new int[paramTypes.size()];
        ValueKind[] optional = new ValueKind[paramValues.size()];
        //int[] param_names = new int[paramNames.size()];
        for (int i = 0; i < paramTypes.size(); i++) {
            param_types[i] = typeName(localData, paramTypes.get(i));
            //param_names[i] = str(paramNames.get(i));
        }

        for (int i = 0; i < paramValues.size(); i++) {
            optional[i] = getValueKind(Namespace.KIND_NAMESPACE/*FIXME*/, paramTypes.get(paramTypes.size() - paramValues.size() + i), paramValues.get(i));
            if (optional[i] == null) {
                throw new CompilationException("Default value must be compiletime constant", line);
            }
        }

        MethodInfo mi = new MethodInfo(param_types, constructor ? 0 : typeName(localData, retType), 0/*name_index*/, 0, optional, new int[0]/*no param_names*/);
        if (hasArguments) {
            mi.setFlagNeed_Arguments();
        }
        //No param names like in official
        /*
         if (!paramNames.isEmpty()) {
         mi.setFlagHas_paramnames();
         }*/
        if (!paramValues.isEmpty()) {
            mi.setFlagHas_optional();
        }
        if (hasRest) {
            mi.setFlagNeed_rest();
        }

        int mindex;
        if (!isInterface) {
            MethodBody mbody = new MethodBody(abc);

            if (needsActivation) {
                int slotId = 1;
                for (int i = 1; i < slotNames.size(); i++) {
                    TraitSlotConst tsc = new TraitSlotConst();
                    tsc.slot_id = slotId++;
                    tsc.name_index = abc.constants.getMultinameId(new Multiname(Multiname.QNAME, abc.constants.getStringId(slotNames.get(i), true), abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE_INTERNAL, abc.constants.getStringId(pkg, true)), 0, true), 0, 0, new ArrayList<>()), true);
                    tsc.type_index = typeName(localData, new TypeItem(slotTypes.get(i)));
                    mbody.traits.traits.add(tsc);
                }
                for (int i = 1; i < paramRegCount; i++) {
                    NameAVM2Item param = new NameAVM2Item(new TypeItem(registerTypes.get(i)), 0, registerNames.get(i), null, false, new ArrayList<>());
                    param.setRegNumber(i);
                    NameAVM2Item d = new NameAVM2Item(new TypeItem(registerTypes.get(i)), 0, registerNames.get(i), param, true, new ArrayList<>());
                    d.setSlotScope(slotScope);
                    d.setSlotNumber(slotNames.indexOf(registerNames.get(i)));
                    declarations.add(d);
                }
            }
            if (body != null) {
                body.addAll(0, declarations);
            }

            localData.exceptions = new ArrayList<>();
            localData.callStack.add(mbody);
            List<GraphSourceItem> src = body == null ? new ArrayList<>() : generate(localData, body);

            mbody.method_info = abc.addMethodInfo(mi);
            mi.setBody(mbody);
            List<AVM2Instruction> mbodyCode = toInsList(src);
            mbody.setCode(new AVM2Code());
            mbody.getCode().code = mbodyCode;

            if (needsActivation) {
                if (localData.traitUsages.containsKey(mbody)) {
                    List<Integer> usages = localData.traitUsages.get(mbody);
                    for (int i = 0; i < mbody.traits.traits.size(); i++) {
                        if (usages.contains(i)) {
                            TraitSlotConst tsc = (TraitSlotConst) mbody.traits.traits.get(i);
                            GraphTargetItem type = TypeItem.UNBOUNDED;
                            if (tsc.type_index > 0) {
                                type = new TypeItem(abc.constants.constant_multiname.get(tsc.type_index).getNameWithNamespace(abc.constants));
                            }
                            NameAVM2Item d = new NameAVM2Item(type, 0, tsc.getName(abc).getName(abc.constants, null, true), NameAVM2Item.getDefaultValue("" + type), true, new ArrayList<>());
                            d.setSlotNumber(tsc.slot_id);
                            d.setSlotScope(slotScope);
                            mbodyCode.addAll(0, toInsList(d.toSourceIgnoreReturnValue(localData, this)));
                        }
                    }
                }

                List<AVM2Instruction> acts = new ArrayList<>();
                acts.add(ins(AVM2Instructions.NewActivation));
                acts.add(ins(AVM2Instructions.Dup));
                acts.add(AssignableAVM2Item.generateSetLoc(localData.activationReg));
                acts.add(ins(AVM2Instructions.PushScope));

                mbodyCode.addAll(0, acts);
            }

            if (constructor) {
                List<ABC> abcs = new ArrayList<>();
                abcs.add(abc);
                abcs.addAll(allABCs);

                int parentConstMinAC = 0;

                for (ABC a : abcs) {
                    int ci = a.findClassByName(superType);
                    if (ci > -1) {
                        MethodInfo pmi = a.method_info.get(a.instance_info.get(ci).iinit_index);
                        parentConstMinAC = pmi.param_types.length;
                        if (pmi.flagHas_optional()) {
                            parentConstMinAC -= pmi.optional.length;
                        }

                    }
                }
                int ac = -1;
                for (AVM2Instruction ins : mbodyCode) {
                    if (ins.definition instanceof ConstructSuperIns) {
                        ac = ins.operands[0];
                        if (parentConstMinAC > ac) {
                            throw new CompilationException("Parent constructor call requires different number of arguments", line);
                        }

                    }
                }
                if (ac == -1) {
                    if (parentConstMinAC == 0) {
                        mbodyCode.add(0, new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
                        mbodyCode.add(1, new AVM2Instruction(0, AVM2Instructions.ConstructSuper, new int[]{0}));

                    } else {
                        throw new CompilationException("Parent constructor must be called", line);
                    }
                }
            }
            if (className != null && !subMethod) {
                mbodyCode.add(0, new AVM2Instruction(0, AVM2Instructions.GetLocal0, null));
                mbodyCode.add(1, new AVM2Instruction(0, AVM2Instructions.PushScope, null));
            }
            boolean addRet = false;
            if (!mbodyCode.isEmpty()) {
                InstructionDefinition lastDef = mbodyCode.get(mbodyCode.size() - 1).definition;
                if (!((lastDef instanceof ReturnVoidIns) || (lastDef instanceof ReturnValueIns))) {
                    addRet = true;
                }
            } else {
                addRet = true;
            }
            if (addRet) {
                if (retType.toString().equals("*") || retType.toString().equals("void") || constructor) {
                    mbodyCode.add(new AVM2Instruction(0, AVM2Instructions.ReturnVoid, null));
                } else {
                    mbodyCode.add(new AVM2Instruction(0, AVM2Instructions.PushUndefined, null));
                    mbodyCode.add(new AVM2Instruction(0, AVM2Instructions.ReturnValue, null));
                }
            }
            mbody.exceptions = localData.exceptions.toArray(new ABCException[localData.exceptions.size()]);
            int offset = 0;
            for (int i = 0; i < mbodyCode.size(); i++) {
                AVM2Instruction ins = mbodyCode.get(i);
                if (ins instanceof ExceptionMarkAVM2Instruction) {
                    ExceptionMarkAVM2Instruction m = (ExceptionMarkAVM2Instruction) ins;
                    switch (m.markType) {
                        case MARK_E_START:
                            mbody.exceptions[m.exceptionId].start = offset;
                            break;
                        case MARK_E_END:
                            mbody.exceptions[m.exceptionId].end = offset;
                            break;
                        case MARK_E_TARGET:
                            mbody.exceptions[m.exceptionId].target = offset;
                            break;
                    }
                    mbodyCode.remove(i);
                    i--;
                    continue;
                }
                offset += ins.getBytesLength();
            }

            mbody.markOffsets();
            mbody.autoFillStats(abc, initScope, className != null);
            abc.addMethodBody(mbody);
            mindex = mbody.method_info;
        } else {
            mindex = abc.addMethodInfo(mi);
        }

        return mindex;
    }

    public ValueKind getValueKind(int ns, GraphTargetItem type, GraphTargetItem val) {

        if (val instanceof BooleanAVM2Item) {
            BooleanAVM2Item bi = (BooleanAVM2Item) val;
            if (bi.value) {
                return new ValueKind(0, ValueKind.CONSTANT_True);
            } else {
                return new ValueKind(0, ValueKind.CONSTANT_False);
            }
        }

        boolean isNs = false;
        if (type instanceof NameAVM2Item) {
            if (((NameAVM2Item) type).getVariableName().equals("namespace")) {
                isNs = true;
            }
        }

        if ((type instanceof TypeItem) && (((TypeItem) type).fullTypeName.equals("Namespace"))) {
            isNs = true;
        }

        if (val instanceof StringAVM2Item) {
            StringAVM2Item sval = (StringAVM2Item) val;
            if (isNs) {
                return new ValueKind(namespace(Namespace.KIND_NAMESPACE, sval.value), ValueKind.CONSTANT_Namespace);
            } else {
                return new ValueKind(str(sval.value), ValueKind.CONSTANT_Utf8);
            }
        }
        if (val instanceof IntegerValueAVM2Item) {
            return new ValueKind(abc.constants.getIntId(((IntegerValueAVM2Item) val).value, true), ValueKind.CONSTANT_Int);
        }
        if (val instanceof FloatValueAVM2Item) {
            return new ValueKind(abc.constants.getDoubleId(((FloatValueAVM2Item) val).value, true), ValueKind.CONSTANT_Double);
        }
        if (val instanceof NanAVM2Item) {
            return new ValueKind(abc.constants.getDoubleId(Double.NaN, true), ValueKind.CONSTANT_Double);
        }
        if (val instanceof NullAVM2Item) {
            return new ValueKind(0, ValueKind.CONSTANT_Null);
        }
        if (val instanceof UndefinedAVM2Item) {
            return new ValueKind(0, ValueKind.CONSTANT_Undefined);
        }
        return null;
    }

    private int genNs(List<DottedChain> importedClasses, DottedChain pkg, String custom, int namespace, List<Integer> openedNamespaces, SourceGeneratorLocalData localData, int line) throws CompilationException {
        if (custom != null) {
            PropertyAVM2Item prop = new PropertyAVM2Item(null, custom, abc, allABCs, openedNamespaces, new ArrayList<>());
            Reference<ValueKind> value = new Reference<>(null);
            prop.resolve(localData, new Reference<>(null), new Reference<>(null), new Reference<>(0), value);
            boolean resolved = true;
            if (value.getVal() == null) {
                resolved = false;
            }
            if (!resolved) {

                DottedChain fullCustom = null;
                for (DottedChain imp : importedClasses) {
                    if (imp.getLast().equals(custom)) {
                        fullCustom = imp;
                        break;
                    }
                }

                List<ABC> aas = new ArrayList<>();
                aas.add(abc);
                aas.addAll(allABCs);
                for (ABC a : aas) {
                    for (ScriptInfo si : a.script_info) {
                        for (Trait t : si.traits.traits) {
                            Multiname m = t.getName(a);
                            if (fullCustom != null && fullCustom.equals(m.getNameWithNamespace(a.constants))) {
                                if (t instanceof TraitSlotConst) {
                                    if (((TraitSlotConst) t).isNamespace()) {
                                        Namespace ns = a.constants.getNamespace(((TraitSlotConst) t).value_index);
                                        return abc.constants.getNamespaceId(new Namespace(ns.kind, abc.constants.getStringId(ns.getName(a.constants), true)), 0, true);
                                    }
                                }
                            }
                        }
                    }
                }

                throw new CompilationException("Namespace not defined", line);
            }
            namespace = value.getVal().value_index;
        }
        return namespace;
    }

    public void generateTraitsPhase2(List<DottedChain> importedClasses, DottedChain pkg, List<GraphTargetItem> items, Trait[] traits, List<Integer> openedNamespaces, SourceGeneratorLocalData localData) throws CompilationException {
        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (traits[k] == null) {
                continue;
            } else if (item instanceof InterfaceAVM2Item) {
                traits[k].name_index = traitName(((InterfaceAVM2Item) item).namespace, ((InterfaceAVM2Item) item).name);
            } else if (item instanceof ClassAVM2Item) {
                traits[k].name_index = traitName(((ClassAVM2Item) item).namespace, ((ClassAVM2Item) item).className);
            } else if ((item instanceof MethodAVM2Item) || (item instanceof GetterAVM2Item) || (item instanceof SetterAVM2Item)) {
                traits[k].name_index = traitName(genNs(importedClasses, pkg, ((MethodAVM2Item) item).customNamespace, ((MethodAVM2Item) item).namespace, openedNamespaces, localData, ((MethodAVM2Item) item).line), ((MethodAVM2Item) item).functionName);
            } else if (item instanceof FunctionAVM2Item) {
                traits[k].name_index = traitName(((FunctionAVM2Item) item).namespace, ((FunctionAVM2Item) item).functionName);
            } else if (item instanceof ConstAVM2Item) {
                traits[k].name_index = traitName(genNs(importedClasses, pkg, ((ConstAVM2Item) item).customNamespace, ((ConstAVM2Item) item).getNamespace(), openedNamespaces, localData, ((ConstAVM2Item) item).line), ((ConstAVM2Item) item).var);
            } else if (item instanceof SlotAVM2Item) {
                traits[k].name_index = traitName(genNs(importedClasses, pkg, ((SlotAVM2Item) item).customNamespace, ((SlotAVM2Item) item).getNamespace(), openedNamespaces, localData, ((SlotAVM2Item) item).line), ((SlotAVM2Item) item).var);
            }
        }

        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (traits[k] == null) {
                continue;
            }
            if (item instanceof ClassAVM2Item) {
                InstanceInfo instanceInfo = abc.instance_info.get(((TraitClass) traits[k]).class_info);
                instanceInfo.name_index = abc.constants.getMultinameId(new Multiname(Multiname.QNAME, abc.constants.getStringId(((ClassAVM2Item) item).className, true),
                        abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId(pkg, true)), 0, true), 0, 0, new ArrayList<>()), true);

                if (((ClassAVM2Item) item).extendsOp != null) {
                    instanceInfo.super_index = typeName(localData, ((ClassAVM2Item) item).extendsOp);
                } else {
                    instanceInfo.super_index = abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str("Object"), namespace(Namespace.KIND_PACKAGE, ""), 0, 0, new ArrayList<>()), true);
                }
                instanceInfo.interfaces = new int[((ClassAVM2Item) item).implementsOp.size()];
                for (int i = 0; i < ((ClassAVM2Item) item).implementsOp.size(); i++) {
                    instanceInfo.interfaces[i] = superIntName(localData, ((ClassAVM2Item) item).implementsOp.get(i));
                }
            }
            if (item instanceof InterfaceAVM2Item) {
                InstanceInfo instanceInfo = abc.instance_info.get(((TraitClass) traits[k]).class_info);
                instanceInfo.name_index = abc.constants.getMultinameId(new Multiname(Multiname.QNAME, abc.constants.getStringId(((InterfaceAVM2Item) item).name, true),
                        abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId(pkg, true)), 0, true), 0, 0, new ArrayList<>()), true);

                instanceInfo.interfaces = new int[((InterfaceAVM2Item) item).superInterfaces.size()];
                for (int i = 0; i < ((InterfaceAVM2Item) item).superInterfaces.size(); i++) {
                    GraphTargetItem un = ((InterfaceAVM2Item) item).superInterfaces.get(i);
                    instanceInfo.interfaces[i] = superIntName(localData, un);
                }
            }
        }
    }

    public int superIntName(SourceGeneratorLocalData localData, GraphTargetItem un) throws CompilationException {
        if (un instanceof UnresolvedAVM2Item) {
            ((UnresolvedAVM2Item) un).resolve(null, new ArrayList<>(), new ArrayList<>(), abc, allABCs, new ArrayList<>(), new ArrayList<>());
            un = ((UnresolvedAVM2Item) un).resolved;
        }
        if (!(un instanceof TypeItem)) { //not applyType
            throw new CompilationException("Invalid type", 0);
        }
        TypeItem sup = (TypeItem) un;
        int propId = resolveType(localData, sup, abc, allABCs);
        int[] nss = new int[]{abc.constants.constant_multiname.get(propId).namespace_index};
        return abc.constants.getMultinameId(new Multiname(Multiname.MULTINAME, abc.constants.constant_multiname.get(propId).name_index, 0, abc.constants.getNamespaceSetId(new NamespaceSet(nss), true), 0, new ArrayList<>()), true);

    }

    public int[] generateMetadata(List<Map.Entry<String, Map<String, String>>> metadata) {
        int ret[] = new int[metadata.size()];
        for (int i = 0; i < metadata.size(); i++) {
            Map.Entry<String, Map<String, String>> en = metadata.get(i);
            int keys[] = new int[en.getValue().size()];
            int values[] = new int[en.getValue().size()];
            int j = 0;
            for (String key : en.getValue().keySet()) {
                keys[j] = abc.constants.getStringId(key, true);
                values[j] = abc.constants.getStringId(en.getValue().get(key), true);
                j++;
            }
            MetadataInfo mi = new MetadataInfo(abc.constants.getStringId(en.getKey(), true), keys, values);
            ret[i] = abc.metadata_info.size();
            abc.metadata_info.add(mi);
        }
        return ret;
    }

    public void generateTraitsPhase3(int methodInitScope, boolean isInterface, String className, String superName, boolean generateStatic, SourceGeneratorLocalData localData, List<GraphTargetItem> items, Traits ts, Trait[] traits, Map<Trait, Integer> initScopes, Reference<Integer> class_index) throws AVM2ParseException, CompilationException {
        //Note: Names must be generated first before accesed in inner subs
        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (traits[k] == null) {
                continue;
            }
            if (item instanceof InterfaceAVM2Item) {
                generateClass(((InterfaceAVM2Item) item).namespace, abc.class_info.get(((TraitClass) traits[k]).class_info), abc.instance_info.get(((TraitClass) traits[k]).class_info), initScopes.get(traits[k]), ((InterfaceAVM2Item) item).pkg, localData, (InterfaceAVM2Item) item, class_index);
            }

            if (item instanceof ClassAVM2Item) {
                generateClass(((ClassAVM2Item) item).namespace, abc.class_info.get(((TraitClass) traits[k]).class_info), abc.instance_info.get(((TraitClass) traits[k]).class_info), initScopes.get(traits[k]), ((ClassAVM2Item) item).pkg, localData, (ClassAVM2Item) item, class_index);
            }
            if ((item instanceof MethodAVM2Item) || (item instanceof GetterAVM2Item) || (item instanceof SetterAVM2Item)) {
                MethodAVM2Item mai = (MethodAVM2Item) item;
                if (mai.isStatic() != generateStatic) {
                    continue;
                }
                ((TraitMethodGetterSetter) traits[k]).method_info = method(false, isInterface, new ArrayList<>(), mai.pkg, mai.needsActivation, mai.subvariables, methodInitScope + (mai.isStatic() ? 0 : 1), mai.hasRest, mai.line, className, superName, false, localData, mai.paramTypes, mai.paramNames, mai.paramValues, mai.body, mai.retType);
            } else if (item instanceof FunctionAVM2Item) {
                FunctionAVM2Item fai = (FunctionAVM2Item) item;
                ((TraitFunction) traits[k]).method_info = method(false, isInterface, new ArrayList<>(), fai.pkg, fai.needsActivation, fai.subvariables, methodInitScope, fai.hasRest, fai.line, className, superName, false, localData, fai.paramTypes, fai.paramNames, fai.paramValues, fai.body, fai.retType);
            }
        }
    }

    public Trait[] generateTraitsPhase1(String className, String superName, boolean generateStatic, SourceGeneratorLocalData localData, List<GraphTargetItem> items, Traits ts, Reference<Integer> classIndex) throws AVM2ParseException, CompilationException {
        Trait[] traits = new Trait[items.size()];
        int slot_id = 1;
        int disp_id = 3; //1 and 2 are for constructor
        for (int k = 0; k < items.size(); k++) {
            GraphTargetItem item = items.get(k);
            if (item instanceof InterfaceAVM2Item) {
                TraitClass tc = new TraitClass();
                ClassInfo ci = new ClassInfo();
                InstanceInfo ii = new InstanceInfo();
                /*abc.class_info.add(ci);
                 abc.instance_info.add(ii);*/
                tc.class_info = classIndex.getVal();
                abc.addClass(ci, ii, classIndex.getVal());
                classIndex.setVal(classIndex.getVal() + 1);
                ii.flags |= InstanceInfo.CLASS_INTERFACE;
                //tc.class_info = abc.instance_info.size() - 1;
                tc.kindType = Trait.TRAIT_CLASS;
                //tc.name_index = traitName(((InterfaceAVM2Item) item).namespace, ((InterfaceAVM2Item) item).name);
                tc.slot_id = 0; //?
                ts.traits.add(tc);
                traits[k] = tc;
                traits[k].metadata = generateMetadata(((InterfaceAVM2Item) item).metadata);
            }

            if (item instanceof ClassAVM2Item) {
                TraitClass tc = new TraitClass();
                ClassInfo ci = new ClassInfo();
                InstanceInfo ii = new InstanceInfo();
                /*abc.class_info.add(ci);
                 abc.instance_info.add(instanceInfo);*/
                tc.class_info = classIndex.getVal();
                abc.addClass(ci, ii, classIndex.getVal());
                classIndex.setVal(classIndex.getVal() + 1);
                //tc.class_info = abc.instance_info.size() - 1;

                /*instanceInfo.name_index = abc.constants.addMultiname(new Multiname(Multiname.QNAME, abc.constants.getStringId(((ClassAVM2Item) item).className, true),
                 abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId(pkg.packageName, true)), 0, true), 0, 0, new ArrayList<Integer>()));
                 */

                /*if (((ClassAVM2Item) item).extendsOp != null) {
                 instanceInfo.super_index = typeName(localData, ((ClassAVM2Item) item).extendsOp);
                 } else {
                 instanceInfo.super_index = abc.constants.getMultinameId(new Multiname(Multiname.QNAME, str("Object"), namespace(Namespace.KIND_PACKAGE, ""), 0, 0, new ArrayList<Integer>()), true);
                 }*/
                tc.kindType = Trait.TRAIT_CLASS;
                // tc.name_index = traitName(((ClassAVM2Item) item).namespace, ((ClassAVM2Item) item).className);
                tc.slot_id = slot_id++;
                ts.traits.add(tc);
                traits[k] = tc;
                traits[k].metadata = generateMetadata(((ClassAVM2Item) item).metadata);

            }
            if ((item instanceof SlotAVM2Item) || (item instanceof ConstAVM2Item)) {
                TraitSlotConst tsc = new TraitSlotConst();
                tsc.kindType = (item instanceof SlotAVM2Item) ? Trait.TRAIT_SLOT : Trait.TRAIT_CONST;
                String var = null;
                GraphTargetItem val = null;
                GraphTargetItem type = null;
                boolean isNamespace = false;
                int namespace = 0;
                boolean isStatic = false;
                int metadata[] = new int[0];
                if (item instanceof SlotAVM2Item) {
                    SlotAVM2Item sai = (SlotAVM2Item) item;
                    if (sai.isStatic() != generateStatic) {
                        continue;
                    }
                    var = sai.var;
                    val = sai.value;
                    type = sai.type;
                    isStatic = sai.isStatic();
                    namespace = sai.getNamespace();
                    metadata = generateMetadata(((SlotAVM2Item) item).metadata);
                }
                if (item instanceof ConstAVM2Item) {
                    ConstAVM2Item cai = (ConstAVM2Item) item;
                    if (cai.isStatic() != generateStatic) {
                        continue;
                    }
                    var = cai.var;
                    val = cai.value;
                    type = cai.type;
                    namespace = cai.getNamespace();
                    isNamespace = type.toString().equals("Namespace");
                    isStatic = cai.isStatic();
                    metadata = generateMetadata(((ConstAVM2Item) item).metadata);
                }
                if (isNamespace) {
                    tsc.name_index = traitName(namespace, var);
                }
                tsc.type_index = isNamespace ? 0 : (type == null ? 0 : typeName(localData, type));

                ValueKind vk = getValueKind(namespace, type, val);
                if (vk == null) {
                    tsc.value_kind = ValueKind.CONSTANT_Undefined;
                } else {
                    tsc.value_kind = vk.value_kind;
                    tsc.value_index = vk.value_index;
                }
                tsc.slot_id = isStatic ? slot_id++ : 0;
                ts.traits.add(tsc);
                traits[k] = tsc;
                traits[k].metadata = metadata;
            }
            if ((item instanceof MethodAVM2Item) || (item instanceof GetterAVM2Item) || (item instanceof SetterAVM2Item)) {
                MethodAVM2Item mai = (MethodAVM2Item) item;
                if (mai.isStatic() != generateStatic) {
                    continue;
                }
                TraitMethodGetterSetter tmgs = new TraitMethodGetterSetter();
                tmgs.kindType = (item instanceof GetterAVM2Item) ? Trait.TRAIT_GETTER : ((item instanceof SetterAVM2Item) ? Trait.TRAIT_SETTER : Trait.TRAIT_METHOD);
                //tmgs.name_index = traitName(((MethodAVM2Item) item).namespace, ((MethodAVM2Item) item).functionName);
                tmgs.disp_id = mai.isStatic() ? disp_id++ : 0; //For a reason, there is disp_id only for static methods (or not?)
                if (mai.isFinal() || mai.isStatic()) {
                    tmgs.kindFlags |= Trait.ATTR_Final;
                }
                if (mai.isOverride()) {
                    tmgs.kindFlags |= Trait.ATTR_Override;
                }
                ts.traits.add(tmgs);

                traits[k] = tmgs;
                traits[k].metadata = generateMetadata(((MethodAVM2Item) item).metadata);
            } else if (item instanceof FunctionAVM2Item) {
                TraitFunction tf = new TraitFunction();
                tf.slot_id = slot_id++;
                tf.kindType = Trait.TRAIT_FUNCTION;
                //tf.name_index = traitName(((FunctionAVM2Item) item).namespace, ((FunctionAVM2Item) item).functionName);
                ts.traits.add(tf);
                traits[k] = tf;
                traits[k].metadata = generateMetadata(((FunctionAVM2Item) item).metadata);
            }
        }

        return traits;
    }

    public ScriptInfo generateScriptInfo(SourceGeneratorLocalData localData, List<GraphTargetItem> commands, int classPos) throws AVM2ParseException, CompilationException {
        Reference<Integer> class_index = new Reference<>(classPos);
        ScriptInfo si = new ScriptInfo();
        localData.currentScript = si;
        Trait[] traitArr = generateTraitsPhase1(null, null, true, localData, commands, si.traits, class_index);
        generateTraitsPhase2(new ArrayList<>(), null/*FIXME*/, commands, traitArr, new ArrayList<>(), localData);
        MethodInfo mi = new MethodInfo(new int[0], 0, 0, 0, new ValueKind[0], new int[0]);
        MethodBody mb = new MethodBody(abc);
        mb.method_info = abc.addMethodInfo(mi);
        mb.setCode(new AVM2Code());
        List<AVM2Instruction> mbCode = mb.getCode().code;
        mbCode.add(ins(AVM2Instructions.GetLocal0));
        mbCode.add(ins(AVM2Instructions.PushScope));

        int traitScope = 2;

        Map<Trait, Integer> initScopes = new HashMap<>();

        for (Trait t : si.traits.traits) {
            if (t instanceof TraitClass) {
                TraitClass tc = (TraitClass) t;
                List<Integer> parents = new ArrayList<>();
                if (localData.documentClass) {
                    mbCode.add(ins(AVM2Instructions.GetScopeObject, 0));
                    traitScope++;
                } else {
                    NamespaceSet nsset = new NamespaceSet(new int[]{abc.constants.constant_multiname.get(tc.name_index).namespace_index});
                    mbCode.add(ins(AVM2Instructions.FindPropertyStrict, abc.constants.getMultinameId(new Multiname(Multiname.MULTINAME, abc.constants.constant_multiname.get(tc.name_index).name_index, 0, abc.constants.getNamespaceSetId(nsset, true), 0, new ArrayList<>()), true)));
                }
                if (abc.instance_info.get(tc.class_info).isInterface()) {
                    mbCode.add(ins(AVM2Instructions.PushNull));
                } else {

                    parentNamesAddNames(abc, allABCs, abc.instance_info.get(tc.class_info).name_index, parents, new ArrayList<>(), new ArrayList<>());
                    for (int i = parents.size() - 1; i >= 0; i--) {
                        mbCode.add(ins(AVM2Instructions.GetLex, parents.get(i)));
                        mbCode.add(ins(AVM2Instructions.PushScope));
                        traitScope++;
                    }
                    mbCode.add(ins(AVM2Instructions.GetLex, parents.get(0)));
                }
                mbCode.add(ins(AVM2Instructions.NewClass, tc.class_info));
                if (!abc.instance_info.get(tc.class_info).isInterface()) {
                    for (int i = parents.size() - 1; i >= 1; i--) {
                        mbCode.add(ins(AVM2Instructions.PopScope));
                    }
                }
                mbCode.add(ins(AVM2Instructions.InitProperty, tc.name_index));
                initScopes.put(t, traitScope);
                traitScope = 1;
            }
        }

        mbCode.add(ins(AVM2Instructions.ReturnVoid));
        mb.autoFillStats(abc, 1, false);
        abc.addMethodBody(mb);
        si.init_index = mb.method_info;
        localData.pkg = null; //FIXME: pkg.packageName;
        generateTraitsPhase3(1/*??*/, false, null, null, true, localData, commands, si.traits, traitArr, initScopes, class_index);
        return si;
    }

    public static void parentNamesAddNames(ABC abc, List<ABC> allABCs, int name_index, List<Integer> indices, List<String> names, List<String> namespaces) {
        List<Integer> cindices = new ArrayList<>();

        List<ABC> outABCs = new ArrayList<>();
        parentNames(abc, allABCs, name_index, cindices, names, namespaces, outABCs);
        for (int i = 0; i < cindices.size(); i++) {
            ABC a = outABCs.get(i);
            int m = cindices.get(i);
            if (a == abc) {
                indices.add(m);
                continue;
            }
            Multiname superName = a.constants.constant_multiname.get(m);
            indices.add(
                    abc.constants.getMultinameId(
                            new Multiname(Multiname.QNAME,
                                    abc.constants.getStringId(superName.getName(a.constants, null, true), true),
                                    abc.constants.getNamespaceId(new Namespace(superName.getNamespace(a.constants).kind, abc.constants.getStringId(superName.getNamespace(a.constants).getName(a.constants), true)), 0, true), 0, 0, new ArrayList<>()), true)
            );
        }
    }

    public static GraphTargetItem getTraitReturnType(ABC abc, Trait t) {
        if (t instanceof TraitSlotConst) {
            TraitSlotConst tsc = (TraitSlotConst) t;
            if (tsc.type_index == 0) {
                return TypeItem.UNBOUNDED;
            }
            return PropertyAVM2Item.multinameToType(tsc.type_index, abc.constants);
        }
        if (t instanceof TraitMethodGetterSetter) {
            TraitMethodGetterSetter tmgs = (TraitMethodGetterSetter) t;
            if (tmgs.kindType == Trait.TRAIT_GETTER) {
                return PropertyAVM2Item.multinameToType(abc.method_info.get(tmgs.method_info).ret_type, abc.constants);
            }
            if (tmgs.kindType == Trait.TRAIT_SETTER) {
                if (abc.method_info.get(tmgs.method_info).param_types.length > 0) {
                    return PropertyAVM2Item.multinameToType(abc.method_info.get(tmgs.method_info).param_types[0], abc.constants);
                } else {
                    return TypeItem.UNBOUNDED;
                }
            }
        }
        if (t instanceof TraitFunction) {
            return new TypeItem(DottedChain.FUNCTION);
        }
        return TypeItem.UNBOUNDED;
    }

    public static boolean searchPrototypeChain(boolean instanceOnly, List<ABC> abcs, DottedChain pkg, String obj, String propertyName, Reference<String> outName, Reference<DottedChain> outNs, Reference<DottedChain> outPropNs, Reference<Integer> outPropNsKind, Reference<Integer> outPropNsIndex, Reference<GraphTargetItem> outPropType, Reference<ValueKind> outPropValue) {

        for (ABC abc : abcs) {
            if (!instanceOnly) {
                for (ScriptInfo ii : abc.script_info) {
                    if (ii.deleted) {
                        continue;
                    }
                    for (Trait t : ii.traits.traits) {
                        if (Objects.equals(pkg, t.getName(abc).getNamespace(abc.constants).getName(abc.constants))) {
                            if (propertyName.equals(t.getName(abc).getName(abc.constants, null, true))) {
                                outName.setVal(obj);
                                outNs.setVal(pkg);
                                outPropNs.setVal(t.getName(abc).getNamespace(abc.constants).getName(abc.constants));
                                outPropNsKind.setVal(t.getName(abc).getNamespace(abc.constants).kind);
                                outPropNsIndex.setVal(abc.constants.getNamespaceSubIndex(t.getName(abc).namespace_index));
                                outPropType.setVal(getTraitReturnType(abc, t));
                                if (t instanceof TraitSlotConst) {
                                    TraitSlotConst tsc = (TraitSlotConst) t;
                                    outPropValue.setVal(new ValueKind(tsc.value_index, tsc.value_kind));
                                }
                                return true;
                            }
                        }
                    }
                }
            }
            for (int i = 0; i < abc.instance_info.size(); i++) {
                InstanceInfo ii = abc.instance_info.get(i);
                if (ii.deleted) {
                    continue;
                }
                Multiname clsName = ii.getName(abc.constants);
                if (obj.equals(clsName.getName(abc.constants, null, true))) {
                    if (Objects.equals(pkg, clsName.getNamespace(abc.constants).getName(abc.constants))) {
                        //class found

                        for (Trait t : ii.instance_traits.traits) {
                            if (t.getName(abc) == null) { //in traits phase 2
                                continue;
                            }
                            if (propertyName.equals(t.getName(abc).getName(abc.constants, null, true))) {
                                outName.setVal(obj);
                                outNs.setVal(pkg);
                                outPropNs.setVal(t.getName(abc).getNamespace(abc.constants).getName(abc.constants));
                                outPropNsKind.setVal(t.getName(abc).getNamespace(abc.constants).kind);
                                outPropNsIndex.setVal(abc.constants.getNamespaceSubIndex(t.getName(abc).namespace_index));
                                outPropType.setVal(getTraitReturnType(abc, t));
                                if (t instanceof TraitSlotConst) {
                                    TraitSlotConst tsc = (TraitSlotConst) t;
                                    outPropValue.setVal(new ValueKind(tsc.value_index, tsc.value_kind));
                                }
                                return true;
                            }
                        }

                        if (!instanceOnly) {
                            for (Trait t : abc.class_info.get(i).static_traits.traits) {
                                if (t.getName(abc) == null) { //in traits phase 2
                                    continue;
                                }
                                if (propertyName.equals(t.getName(abc).getName(abc.constants, null, true))) {
                                    outName.setVal(obj);
                                    outNs.setVal(pkg);
                                    outPropNs.setVal(t.getName(abc).getNamespace(abc.constants).getName(abc.constants));
                                    outPropNsKind.setVal(t.getName(abc).getNamespace(abc.constants).kind);
                                    outPropNsIndex.setVal(abc.constants.getNamespaceSubIndex(t.getName(abc).namespace_index));
                                    outPropType.setVal(getTraitReturnType(abc, t));
                                    if (t instanceof TraitSlotConst) {
                                        TraitSlotConst tsc = (TraitSlotConst) t;
                                        outPropValue.setVal(new ValueKind(tsc.value_index, tsc.value_kind));
                                    }
                                    return true;
                                }
                            }
                        }

                        Multiname superName = abc.constants.constant_multiname.get(ii.super_index);
                        if (superName != null) {
                            return searchPrototypeChain(instanceOnly, abcs, superName.getNamespace(abc.constants).getName(abc.constants), superName.getName(abc.constants, null, true), propertyName, outName, outNs, outPropNs, outPropNsKind, outPropNsIndex, outPropType, outPropValue);
                        } else {
                            return false;
                        }
                    }
                }
            }
        }
        return false;
    }

    public static void parentNames(ABC abc, List<ABC> allABCs, int name_index, List<Integer> indices, List<String> names, List<String> namespaces, List<ABC> outABCs) {
        indices.add(name_index);
        names.add(abc.constants.constant_multiname.get(name_index).getName(abc.constants, null, true));
        namespaces.add(abc.constants.constant_multiname.get(name_index).getNamespace(abc.constants).getName(abc.constants).toRawString());
        Multiname mname = abc.constants.constant_multiname.get(name_index);

        outABCs.add(abc);

        List<ABC> abcs = new ArrayList<>();
        abcs.add(abc);
        abcs.addAll(allABCs);

        for (ABC a : abcs) {
            for (int i = 0; i < a.instance_info.size(); i++) {
                Multiname m = a.constants.constant_multiname.get(a.instance_info.get(i).name_index);
                if (m.getName(a.constants, null, true).equals(mname.getName(abc.constants, null, true))) {

                    if (m.getNamespace(a.constants).hasName(mname.getNamespace(abc.constants).getName(abc.constants).toRawString(), a.constants)) {
                        //Multiname superName = a.constants.constant_multiname.get(a.instance_info.get(i).super_index);
                        abcs.remove(a);
                        if (a.instance_info.get(i).super_index != 0) {
                            parentNames(a, abcs, a.instance_info.get(i).super_index, indices, names, namespaces, outABCs);
                        }
                        /*parentNames(abc,allABCs,abc.constants.getMultinameId(
                         new Multiname(superName.kind,
                         abc.constants.getStringId(superName.getName(a.constants, new ArrayList<>()),true),
                         abc.constants.getNamespaceId(new Namespace(superName.getNamespace(a.constants).kind, abc.constants.getStringId(superName.getNamespace(a.constants).getName(a.constants),true)),0,true), 0, 0, new ArrayList<Integer>()), true),indices,names,namespaces,outABCs);*/
                        return;
                    }
                }
            }
        }
    }

    /* public void calcRegisters(Reference<Integer> activationReg, SourceGeneratorLocalData localData, boolean needsActivation, List<String> funParamNames,List<NameAVM2Item> funSubVariables,List<GraphTargetItem> funBody, Reference<Boolean> hasArguments) throws ParseException {

     }*/
    /*public int resolveType(String objType) {
     if (objType.equals("*")) {
     return 0;
     }
     List<ABC> abcs = new ArrayList<>();
     abcs.add(abc);
     abcs.addAll(allABCs);
     for (ABC a : abcs) {
     int ci = a.findClassByName(objType);
     if (ci != -1) {
     Multiname tname = a.instance_info.get(ci).getName(a.constants);
     return abc.constants.getMultinameId(new Multiname(tname.kind,
     abc.constants.getStringId(tname.getName(a.constants, new ArrayList<>()), true),
     abc.constants.getNamespaceId(new Namespace(tname.getNamespace(a.constants).kind, abc.constants.getStringId(tname.getNamespace(a.constants).getName(a.constants), true)), 0, true), 0, 0, new ArrayList<Integer>()), true);
     }
     }
     return 0;
     }*/
    @Override
    public List<GraphSourceItem> generate(SourceGeneratorLocalData localData, TypeItem item) throws CompilationException {
        String currentFullClassName = localData.getFullClass();

        if (localData.documentClass && item.toString().equals(currentFullClassName)) {
            int slotId = 0;
            int c = abc.findClassByName(currentFullClassName);
            for (Trait t : localData.currentScript.traits.traits) {
                if (t instanceof TraitClass) {
                    TraitClass tc = (TraitClass) t;
                    if (tc.class_info == c) {
                        slotId = tc.slot_id;
                        break;
                    }
                }
            }
            return GraphTargetItem.toSourceMerge(localData, this, ins(AVM2Instructions.GetGlobalScope), ins(AVM2Instructions.GetSlot, slotId));
        } else {
            return GraphTargetItem.toSourceMerge(localData, this, ins(AVM2Instructions.GetLex, resolveType(localData, item, abc, allABCs)));
        }
    }

    public static int resolveType(SourceGeneratorLocalData localData, GraphTargetItem item, ABC abc, List<ABC> allABCs) throws CompilationException {
        int name_index = 0;
        GraphTargetItem typeItem = null;

        if (item instanceof UnresolvedAVM2Item) {
            String fullClass = localData.getFullClass();
            item = ((UnresolvedAVM2Item) item).resolve(new TypeItem(fullClass), new ArrayList<>(), new ArrayList<>(), abc, allABCs, new ArrayList<>(), new ArrayList<>());
        }
        if (item instanceof TypeItem) {
            typeItem = item;
        } else if (item instanceof ApplyTypeAVM2Item) {
            typeItem = ((ApplyTypeAVM2Item) item).object;
        } else {
            throw new CompilationException("Invalid type:" + item + " (" + item.getClass().getName() + ")", 0/*??*/);
        }
        if (typeItem instanceof UnresolvedAVM2Item) {
            String fullClass = localData.getFullClass();
            typeItem = ((UnresolvedAVM2Item) typeItem).resolve(new TypeItem(fullClass), new ArrayList<>(), new ArrayList<>(), abc, allABCs, new ArrayList<>(), new ArrayList<>());
        }

        if (!(typeItem instanceof TypeItem)) {
            throw new CompilationException("Invalid type", 0/*??*/);
        }

        TypeItem type = (TypeItem) typeItem;

        DottedChain dname = type.fullTypeName;
        String pkg = dname.getWithoutLast().toRawString();
        String name = dname.getLast();
        for (InstanceInfo ii : abc.instance_info) {
            Multiname mname = abc.constants.constant_multiname.get(ii.name_index);
            if (mname != null && name.equals(mname.getName(abc.constants, null, true))) {
                if (mname.getNamespace(abc.constants).hasName(pkg, abc.constants)) {
                    name_index = ii.name_index;
                    break;
                }
            }
        }
        for (int i = 1; i < abc.constants.constant_multiname.size(); i++) {
            Multiname mname = abc.constants.constant_multiname.get(i);
            if (mname != null && name.equals(mname.getName(abc.constants, null, true))) {
                if (mname.getNamespace(abc.constants) != null && pkg.equals(mname.getNamespace(abc.constants).getName(abc.constants))) {
                    name_index = i;
                    break;
                }
            }
        }
        if (name_index == 0) {
            if (pkg.isEmpty() && localData.currentScript != null /*FIXME!*/) {
                for (Trait t : localData.currentScript.traits.traits) {
                    if (t.getName(abc).getName(abc.constants, null, true).equals(name)) {
                        name_index = t.name_index;
                        break;
                    }
                }
            }
            if (name_index == 0) {
                name_index = abc.constants.getMultinameId(new Multiname(Multiname.QNAME, abc.constants.getStringId(name, true), abc.constants.getNamespaceId(new Namespace(Namespace.KIND_PACKAGE, abc.constants.getStringId(pkg, true)), 0, true), 0, 0, new ArrayList<>()), true);
            }
        }

        if (item instanceof ApplyTypeAVM2Item) {
            ApplyTypeAVM2Item atype = (ApplyTypeAVM2Item) item;
            List<Integer> params = new ArrayList<>();
            for (GraphTargetItem s : atype.params) {
                params.add(resolveType(localData, s, abc, allABCs));
            }
            return abc.constants.getMultinameId(new Multiname(Multiname.TYPENAME, 0, 0, 0, name_index, params), true);
        }

        return name_index;
    }

    @Override
    public List<GraphSourceItem> generateDiscardValue(SourceGeneratorLocalData localData, GraphTargetItem item) throws CompilationException {
        List<GraphSourceItem> ret = item.toSource(localData, this);
        ret.add(ins(AVM2Instructions.Pop));
        return ret;
    }
}
