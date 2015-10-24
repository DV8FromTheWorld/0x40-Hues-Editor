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

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instructions;
import com.jpexs.decompiler.flash.abc.types.Multiname;
import com.jpexs.decompiler.flash.abc.types.NamespaceSet;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.SourceGenerator;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class NamespacedAVM2Item extends AssignableAVM2Item {

    public GraphTargetItem ns;

    public String name;

    public GraphTargetItem nameItem;

    public GraphTargetItem obj;

    public boolean attr;

    public List<Integer> openedNamespaces;

    public NamespacedAVM2Item(GraphTargetItem ns, String name, GraphTargetItem nameItem, GraphTargetItem obj, boolean attr, List<Integer> openedNamespaces, GraphTargetItem storeValue) {
        super(storeValue);
        this.ns = ns;
        this.nameItem = nameItem;
        this.name = name;
        this.obj = obj;
        this.attr = attr;
        this.openedNamespaces = openedNamespaces;
    }

    private int allNsSet(ABC abc) {
        int[] nssa = new int[openedNamespaces.size()];
        for (int i = 0; i < openedNamespaces.size(); i++) {
            nssa[i] = openedNamespaces.get(i);
        }
        return abc.constants.getNamespaceSetId(new NamespaceSet(nssa), true);
    }

    @Override
    public AssignableAVM2Item copy() {
        return new NamespacedAVM2Item(ns, name, nameItem, obj, attr, openedNamespaces, assignedValue);
    }

    @Override
    public List<GraphSourceItem> toSourceChange(SourceGeneratorLocalData localData, SourceGenerator generator, boolean post, boolean decrement, boolean needsReturn) throws CompilationException {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        boolean isInteger = returnType().toString().equals("int");
        Reference<Integer> ns_temp = new Reference<>(-1);
        Reference<Integer> name_temp = new Reference<>(-1);
        Reference<Integer> ret_temp = new Reference<>(-1);
        /*if (name == null && index != null) {
         return toSourceMerge(localData, generator,
         ns, generateCoerce(generator, new TypeItem("Namespace")), index, ins(AVM2Instructions.ConvertS),
         ins(AVM2Instructions.FindPropertyStrict, g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<Integer>()), true)),
         dupSetTemp(localData, generator, name_temp),
         ns, generateCoerce(generator, new TypeItem("Namespace")),
         dupSetTemp(localData, generator, ns_temp),
         ins(AVM2Instructions.GetProperty, g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
         !isInteger ? ins(AVM2Instructions.ConvertD) : null,
         //End get original
         (!post) ? (decrement ? ins(isInteger ? AVM2Instructions.DecrementI : AVM2Instructions.Decrement) : ins(isInteger ? AVM2Instructions.IncrementI : AVM2Instructions.Increment())) : null,
         needsReturn ? ins(AVM2Instructions.Dup) : null,
         (post) ? (decrement ? ins(isInteger ? AVM2Instructions.DecrementI : AVM2Instructions.Decrement) : ins(isInteger ? AVM2Instructions.IncrementI : AVM2Instructions.Increment)) : null,
         setTemp(localData, generator, ret_temp),
         getTemp(localData, generator, name_temp),
         getTemp(localData, generator, ns_temp),
         getTemp(localData, generator, ret_temp),
         ins(AVM2Instructions.SetProperty, g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<Integer>()), true)),
         killTemp(localData, generator, Arrays.asList(ret_temp, name_temp, ns_temp)));
         } else
         */
        if (name != null) {
            return toSourceMerge(localData, generator,
                    ns, NameAVM2Item.generateCoerce(localData, generator, new TypeItem(DottedChain.NAMESPACE)),
                    ins(AVM2Instructions.FindPropertyStrict, g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(name, true), 0, 0, 0, new ArrayList<>()), true)),
                    dupSetTemp(localData, generator, name_temp),
                    ns, NameAVM2Item.generateCoerce(localData, generator, new TypeItem(DottedChain.NAMESPACE)),
                    dupSetTemp(localData, generator, ns_temp),
                    //Start get original
                    //getTemp(localData, generator, ns_temp), generateCoerce(generator, "Namespace"), ins(AVM2Instructions.FindPropertyStrict, g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAME, g.abc.constants.getStringId(variableName, true), 0, 0, 0, new ArrayList<Integer>()), true)),
                    //getTemp(localData, generator, ns_temp), generateCoerce(generator, "Namespace"),
                    ins(AVM2Instructions.GetProperty, g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<>()), true)),
                    !isInteger ? ins(AVM2Instructions.ConvertD) : null,
                    //End get original
                    (!post) ? (decrement ? ins(isInteger ? AVM2Instructions.DecrementI : AVM2Instructions.Decrement) : ins(isInteger ? AVM2Instructions.IncrementI : AVM2Instructions.Increment)) : null,
                    needsReturn ? ins(AVM2Instructions.Dup) : null,
                    (post) ? (decrement ? ins(isInteger ? AVM2Instructions.DecrementI : AVM2Instructions.Decrement) : ins(isInteger ? AVM2Instructions.IncrementI : AVM2Instructions.Increment)) : null,
                    setTemp(localData, generator, ret_temp),
                    getTemp(localData, generator, name_temp),
                    getTemp(localData, generator, ns_temp),
                    getTemp(localData, generator, ret_temp),
                    ins(AVM2Instructions.SetProperty, g.abc.constants.getMultinameId(new Multiname(Multiname.MULTINAMEL, 0, 0, allNsSet(g.abc), 0, new ArrayList<>()), true)),
                    killTemp(localData, generator, Arrays.asList(ret_temp, name_temp, ns_temp))
            );
        } else {
            return new ArrayList<>();
        }
    }

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

    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator, boolean needsReturn, boolean call, List<GraphTargetItem> callargs, boolean delete, boolean construct) throws CompilationException {
        AVM2SourceGenerator g = (AVM2SourceGenerator) generator;
        Reference<Integer> ns_temp = new Reference<>(-1);
        Reference<Integer> index_temp = new Reference<>(-1);
        Reference<Integer> ret_temp = new Reference<>(-1);

        Reference<Integer> obj_temp = new Reference<>(-1);

        if (name == null) {
            if (assignedValue != null) {
                return toSourceMerge(localData, generator,
                        obj == null ? ns : null, obj == null ? NameAVM2Item.generateCoerce(localData, generator, new TypeItem(DottedChain.NAMESPACE)) : null, nameItem, ins(AVM2Instructions.ConvertS), obj != null ? obj : ins(AVM2Instructions.FindPropertyStrict, g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<>()), true)),
                        ns, NameAVM2Item.generateCoerce(localData, generator, new TypeItem(DottedChain.NAMESPACE)), nameItem, ins(AVM2Instructions.ConvertS), assignedValue,
                        needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                        ins(AVM2Instructions.SetProperty, g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<>()), true)),
                        needsReturn ? getTemp(localData, generator, ret_temp) : null,
                        killTemp(localData, generator, Arrays.asList(ns_temp, index_temp, ret_temp))
                );
            } else {
                return toSourceMerge(localData, generator,
                        obj == null ? ns : null, obj == null ? NameAVM2Item.generateCoerce(localData, generator, new TypeItem(DottedChain.NAMESPACE)) : null, nameItem, ins(AVM2Instructions.ConvertS), obj != null ? obj : ins(AVM2Instructions.FindPropertyStrict, g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<>()), true)),
                        call ? dupSetTemp(localData, generator, obj_temp) : null,
                        ns, NameAVM2Item.generateCoerce(localData, generator, new TypeItem(DottedChain.NAMESPACE)), nameItem, ins(AVM2Instructions.ConvertS),
                        construct ? callargs : null,
                        ins(construct ? AVM2Instructions.ConstructProp : delete ? AVM2Instructions.DeleteProperty : AVM2Instructions.GetProperty, g.abc.constants.getMultinameId(new Multiname(Multiname.RTQNAMEL, 0, 0, 0, 0, new ArrayList<>()), true), construct ? callargs.size() : null),
                        call ? getTemp(localData, generator, obj_temp) : null,
                        call ? callargs : null,
                        call ? ins(AVM2Instructions.Call, callargs.size()) : null,
                        needsReturn ? null : ins(AVM2Instructions.Pop),
                        killTemp(localData, generator, Arrays.asList(obj_temp))
                );
            }
        } else {
            if (assignedValue != null) {
                return toSourceMerge(localData, generator,
                        obj == null ? ns : null, obj == null ? NameAVM2Item.generateCoerce(localData, generator, new TypeItem(DottedChain.NAMESPACE)) : null, obj != null ? obj : ins(AVM2Instructions.FindPropertyStrict, g.abc.constants.getMultinameId(new Multiname(attr ? Multiname.RTQNAMEA : Multiname.RTQNAME, g.abc.constants.getStringId(name, true), 0, 0, 0, new ArrayList<>()), true)),
                        ns, NameAVM2Item.generateCoerce(localData, generator, new TypeItem(DottedChain.NAMESPACE)), assignedValue,
                        needsReturn ? dupSetTemp(localData, generator, ret_temp) : null,
                        ins(AVM2Instructions.SetProperty, g.abc.constants.getMultinameId(new Multiname(attr ? Multiname.RTQNAMEA : Multiname.RTQNAME, g.abc.constants.getStringId(name, true), 0, 0, 0, new ArrayList<>()), true)),
                        needsReturn ? getTemp(localData, generator, ret_temp) : null,
                        killTemp(localData, generator, Arrays.asList(ns_temp, index_temp, ret_temp))
                );
            } else {
                return toSourceMerge(localData, generator,
                        obj == null ? ns : null, obj == null ? NameAVM2Item.generateCoerce(localData, generator, new TypeItem(DottedChain.NAMESPACE)) : null, obj != null ? obj : ins(AVM2Instructions.FindPropertyStrict, g.abc.constants.getMultinameId(new Multiname(attr ? Multiname.RTQNAMEA : Multiname.RTQNAME, g.abc.constants.getStringId(name, true), 0, 0, 0, new ArrayList<>()), true)),
                        call ? dupSetTemp(localData, generator, obj_temp) : null,
                        ns, NameAVM2Item.generateCoerce(localData, generator, new TypeItem(DottedChain.NAMESPACE)),
                        construct ? callargs : null,
                        ins(construct ? AVM2Instructions.ConstructProp : delete ? AVM2Instructions.DeleteProperty : AVM2Instructions.GetProperty, g.abc.constants.getMultinameId(new Multiname(attr ? Multiname.RTQNAMEA : Multiname.RTQNAME, g.abc.constants.getStringId(name, true), 0, 0, 0, new ArrayList<>()), true), construct ? callargs.size() : null),
                        call ? getTemp(localData, generator, obj_temp) : null,
                        call ? callargs : null,
                        call ? ins(AVM2Instructions.Call, callargs.size()) : null,
                        needsReturn ? null : ins(AVM2Instructions.Pop),
                        killTemp(localData, generator, Arrays.asList(obj_temp))
                );
            }
        }
    }

    @Override
    public List<GraphSourceItem> toSource(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, true, false, new ArrayList<>(), false, false);
    }

    @Override
    public List<GraphSourceItem> toSourceIgnoreReturnValue(SourceGeneratorLocalData localData, SourceGenerator generator) throws CompilationException {
        return toSource(localData, generator, false, false, new ArrayList<>(), false, false);
    }
}
