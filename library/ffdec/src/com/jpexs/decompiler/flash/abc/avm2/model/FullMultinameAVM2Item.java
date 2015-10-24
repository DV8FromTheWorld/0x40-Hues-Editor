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

import com.jpexs.decompiler.flash.abc.avm2.AVM2ConstantPool;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.types.Namespace;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.graph.DottedChain;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.LocalData;
import java.util.HashMap;
import java.util.List;

public class FullMultinameAVM2Item extends AVM2Item {

    public int multinameIndex;

    public GraphTargetItem name;

    public GraphTargetItem namespace;

    public FullMultinameAVM2Item(AVM2Instruction instruction, int multinameIndex, GraphTargetItem name) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = null;
    }

    public FullMultinameAVM2Item(AVM2Instruction instruction, int multinameIndex) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = null;
        this.namespace = null;
    }

    public FullMultinameAVM2Item(AVM2Instruction instruction, int multinameIndex, GraphTargetItem name, GraphTargetItem namespace) {
        super(instruction, PRECEDENCE_PRIMARY);
        this.multinameIndex = multinameIndex;
        this.name = name;
        this.namespace = namespace;
    }

    public boolean isRuntime() {
        return (name != null) || (namespace != null);
    }

    public boolean isXML(AVM2ConstantPool constants, HashMap<Integer, String> localRegNames, List<DottedChain> fullyQualifiedNames) throws InterruptedException {
        String cname;
        if (name != null) {
            cname = name.toString(LocalData.create(constants, localRegNames, fullyQualifiedNames));
        } else {
            cname = (constants.getMultiname(multinameIndex).getName(constants, fullyQualifiedNames, true));
        }
        String cns = "";
        if (namespace != null) {
            cns = namespace.toString(LocalData.create(constants, localRegNames, fullyQualifiedNames));
        } else {
            Namespace ns = constants.getMultiname(multinameIndex).getNamespace(constants);
            if ((ns != null) && (ns.name_index != 0)) {
                cns = ns.getName(constants).toPrintableString(true);
            }
        }
        return cname.equals("XML") && cns.isEmpty();
    }

    @Override
    public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
        if (namespace != null) {
            namespace.toString(writer, localData);
            writer.append("::");
        } else {
            /*Namespace ns = constants.getMultiname(multinameIndex).getNamespace(constants);
             if ((ns != null)&&(ns.name_index!=0)) {
             ret =  hilight(ns.getName(constants) + "::")+ret;
             }*/
        }
        if (name != null) {
            writer.append("[");
            name.toString(writer, localData);
            writer.append("]");
        } else {
            AVM2ConstantPool constants = localData.constantsAvm2;
            List<DottedChain> fullyQualifiedNames = localData.fullyQualifiedNames;
            if (multinameIndex > 0 && multinameIndex < constants.constant_multiname.size()) {
                writer.append(constants.getMultiname(multinameIndex).getName(constants, fullyQualifiedNames, false));
            } else {
                writer.append("§§unknown_multiname");
            }
        }
        return writer;
    }

    public boolean compareSame(FullMultinameAVM2Item other) {
        if (multinameIndex != other.multinameIndex) {
            return false;
        }
        GraphTargetItem tiName = name;
        if (name != null) {
            name = name.getThroughDuplicate();
        }
        while (tiName instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) tiName).computedValue != null) {
                tiName = ((LocalRegAVM2Item) tiName).computedValue.getThroughNotCompilable().getThroughDuplicate();
            } else {
                break;
            }
        }

        GraphTargetItem tiName2 = other.name;
        if (tiName2 != null) {
            tiName2 = tiName2.getThroughDuplicate();
        }
        while (tiName2 instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) tiName2).computedValue != null) {
                tiName2 = ((LocalRegAVM2Item) tiName2).computedValue.getThroughNotCompilable().getThroughDuplicate();
            } else {
                break;
            }
        }
        if (tiName != tiName2) {
            return false;
        }

        GraphTargetItem tiNameSpace = namespace;
        if (tiNameSpace != null) {
            tiNameSpace = tiNameSpace.getThroughDuplicate();
        }
        while (tiNameSpace instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) tiNameSpace).computedValue != null) {
                tiNameSpace = ((LocalRegAVM2Item) tiNameSpace).computedValue.getThroughNotCompilable().getThroughDuplicate();
            }
        }

        GraphTargetItem tiNameSpace2 = other.namespace;
        if (tiNameSpace2 != null) {
            tiNameSpace2 = tiNameSpace2.getThroughDuplicate();
        }
        while (tiNameSpace2 instanceof LocalRegAVM2Item) {
            if (((LocalRegAVM2Item) tiNameSpace2).computedValue != null) {
                tiNameSpace2 = ((LocalRegAVM2Item) tiNameSpace2).computedValue.getThroughNotCompilable().getThroughDuplicate();
            }
        }
        if (tiNameSpace != tiNameSpace2) {
            return false;
        }
        return true;
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
