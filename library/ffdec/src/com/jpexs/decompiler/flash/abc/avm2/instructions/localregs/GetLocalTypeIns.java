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
package com.jpexs.decompiler.flash.abc.avm2.instructions.localregs;

import com.jpexs.decompiler.flash.abc.ABC;
import com.jpexs.decompiler.flash.abc.AVM2LocalData;
import com.jpexs.decompiler.flash.abc.avm2.instructions.AVM2Instruction;
import com.jpexs.decompiler.flash.abc.avm2.instructions.InstructionDefinition;
import com.jpexs.decompiler.flash.abc.avm2.model.ClassAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.LocalRegAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ScriptAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.ThisAVM2Item;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.NotCompileTimeItem;
import com.jpexs.decompiler.graph.TranslateStack;
import java.util.List;

public abstract class GetLocalTypeIns extends InstructionDefinition {

    public GetLocalTypeIns(int instructionCode, String instructionName, int[] operands, boolean canThrow) {
        super(instructionCode, instructionName, operands, canThrow);
    }

    @Override
    public void translate(AVM2LocalData localData, TranslateStack stack, AVM2Instruction ins, List<GraphTargetItem> output, String path) {

        int regId = getRegisterId(ins);

        if (regId == 0) {
            if ((localData.classIndex >= localData.abc.instance_info.size()) || localData.classIndex < 0) {
                stack.push(new ScriptAVM2Item(localData.scriptIndex));
                return;
            }
            if (localData.isStatic) {
                stack.push(new ClassAVM2Item(localData.abc.instance_info.get(localData.classIndex).getName(localData.constants)));
            } else {
                stack.push(new ThisAVM2Item(ins, localData.abc.instance_info.get(localData.classIndex).getName(localData.constants)));
            }
            return;
        }

        GraphTargetItem computedValue = localData.localRegs.get(regId);
        int assignCount = 0;
        if (localData.localRegAssignmentIps.containsKey(regId)) {
            assignCount = localData.localRegAssignmentIps.get(regId);
        }
        if (assignCount > 5) { //Do not allow change register more than 5 - for deobfuscation
            computedValue = new NotCompileTimeItem(ins, computedValue);
        }
        /*if (!isRegisterCompileTime(regId, ip, refs, code)) {
         computedValue = new NotCompileTimeAVM2Item(ins, computedValue);
         }
         if (computedValue == null) {
         if (!localRegNames.containsKey(regId)) {
         computedValue = new UndefinedAVM2Item(null); //In some obfuscated code there seems to be reading of undefined registers
         }
         }*/
        stack.push(new LocalRegAVM2Item(ins, regId, computedValue));
    }

    @Override
    public int getStackPushCount(AVM2Instruction ins, ABC abc) {
        return 1;
    }

    public abstract int getRegisterId(AVM2Instruction ins);
}
