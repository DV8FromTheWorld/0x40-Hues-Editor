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
package com.jpexs.decompiler.flash.action;

import com.jpexs.decompiler.flash.DisassemblyListener;
import com.jpexs.decompiler.flash.SWFInputStream;
import com.jpexs.decompiler.flash.action.deobfuscation.ActionDeobfuscator;
import com.jpexs.decompiler.flash.action.deobfuscation.ActionDeobfuscatorSimple;
import com.jpexs.decompiler.flash.action.model.ConstantPool;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.special.ActionDeobfuscateJump;
import com.jpexs.decompiler.flash.action.special.ActionEnd;
import com.jpexs.decompiler.flash.action.special.ActionNop;
import com.jpexs.decompiler.flash.action.special.ActionStore;
import com.jpexs.decompiler.flash.action.swf4.ActionEquals;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ActionJump;
import com.jpexs.decompiler.flash.action.swf4.ActionPush;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.action.swf5.ActionDefineFunction;
import com.jpexs.decompiler.flash.action.swf5.ActionEquals2;
import com.jpexs.decompiler.flash.action.swf5.ActionStoreRegister;
import com.jpexs.decompiler.flash.action.swf7.ActionDefineFunction2;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.ecma.EcmaScript;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.exporters.modes.ScriptExportMode;
import com.jpexs.decompiler.flash.helpers.SWFDecompilerPlugin;
import com.jpexs.decompiler.graph.Graph;
import com.jpexs.decompiler.graph.GraphSourceItemContainer;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.NotCompileTimeItem;
import com.jpexs.decompiler.graph.TranslateException;
import com.jpexs.decompiler.graph.TranslateStack;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.helpers.CancellableWorker;
import com.jpexs.helpers.Helper;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class for reading data from SWF file
 *
 * @author JPEXS
 */
public class ActionListReader {

    private static final Logger logger = Logger.getLogger(ActionListReader.class.getName());

    /**
     * Reads list of actions from the stream. Reading ends with
     * ActionEndFlag(=0) or end of the stream.
     *
     * @param listeners
     * @param sis
     * @param version
     * @param ip
     * @param endIp
     * @param path
     * @return List of actions
     * @throws IOException
     * @throws java.lang.InterruptedException
     * @throws java.util.concurrent.TimeoutException
     */
    public static ActionList readActionListTimeout(final List<DisassemblyListener> listeners, final SWFInputStream sis, final int version, final int ip, final int endIp, final String path) throws IOException, InterruptedException, TimeoutException {
        try {
            final int deobfuscationMode = Configuration.autoDeobfuscate.get() ? (Configuration.deobfuscationOldMode.get() ? 0 : 1) : -1;
            ActionList actions = CancellableWorker.call(new Callable<ActionList>() {

                @Override
                public ActionList call() throws IOException, InterruptedException {
                    return readActionList(listeners, sis, version, ip, endIp, path, deobfuscationMode);
                }
            }, Configuration.decompilationTimeoutSingleMethod.get(), TimeUnit.SECONDS);

            return actions;
        } catch (ExecutionException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof InterruptedException) {
                throw (InterruptedException) cause;
            } else if (cause instanceof InterruptedException) {
                throw (IOException) cause;
            } else {
                logger.log(Level.SEVERE, null, ex);
            }
        }
        return new ActionList();
    }

    /**
     * Reads list of actions from the stream. Reading ends with
     * ActionEndFlag(=0) or end of the stream.
     *
     * @param listeners
     * @param sis
     * @param version
     * @param ip
     * @param endIp
     * @param path
     * @param deobfuscationMode
     * @return List of actions
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    public static ActionList readActionList(List<DisassemblyListener> listeners, SWFInputStream sis, int version, int ip, int endIp, String path, int deobfuscationMode) throws IOException, InterruptedException {
        // Map of the actions. Use TreeMap to sort the keys in ascending order
        // actionMap and nextOffsets should contain exaclty the same keys
        Map<Long, Action> actionMap = new TreeMap<>();
        Map<Long, Long> nextOffsets = new HashMap<>();
        Action entryAction = readActionListAtPos(listeners, null,
                sis, actionMap, nextOffsets,
                ip, 0, endIp, path, false, new ArrayList<>());

        if (actionMap.isEmpty()) {
            return new ActionList();
        }

        List<Long> addresses = new ArrayList<>(actionMap.keySet());

        // add end action
        Action lastAction = actionMap.get(addresses.get(addresses.size() - 1));
        long endAddress;
        if (!(lastAction instanceof ActionEnd)) {
            Action aEnd = new ActionEnd();
            aEnd.setAddress(nextOffsets.get(lastAction.getAddress()));
            endAddress = aEnd.getAddress();
            actionMap.put(aEnd.getAddress(), aEnd);
            nextOffsets.put(endAddress, endAddress + 1);
        }

        ActionList actions = fixActionList(new ActionList(actionMap.values()), nextOffsets, version);

        // jump to the entry action when it is diffrent from the first action in the map
        if (entryAction != actions.get(0)) {
            ActionJump jump = new ActionDeobfuscateJump(0);
            actions.addAction(0, jump);
            jump.setJumpOffset((int) (entryAction.getAddress() - jump.getTotalActionLength()));
        }

        if (SWFDecompilerPlugin.fireActionListParsed(actions, sis.getSwf())) {
            actions = fixActionList(actions, null, version);
        }

        if (deobfuscationMode == 0) {
            try {
                actions = deobfuscateActionListOld(listeners, actions, version, 0, path);
                updateActionLengths(actions, version);
            } catch (OutOfMemoryError | StackOverflowError | TranslateException ex) {
                // keep orignal (not deobfuscated) actions
                logger.log(Level.SEVERE, null, ex);
            }
        } else if (deobfuscationMode == 1) {
            try {
                new ActionDeobfuscatorSimple().actionListParsed(actions, sis.getSwf());
                new ActionDeobfuscator().actionListParsed(actions, sis.getSwf());
            } catch (ThreadDeath | InterruptedException ex) {
                throw ex;
            } catch (Throwable ex) {
                // keep orignal (not deobfuscated) actions
                logger.log(Level.SEVERE, "Deobfuscation failed in: " + path, ex);
            }
        }

        return actions;
    }

    public static ActionList fixActionList(ActionList actions, Map<Long, Long> nextOffsets, int version) {
        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(actions, containerLastActions);

        ActionList ret = new ActionList();

        if (nextOffsets != null) {
            int index = 0;
            while (index != -1 && index < actions.size()) {
                Action action = actions.get(index);
                ret.add(action);
                index++;
                if (index < actions.size()) {
                    long nextAddress = nextOffsets.get(action.getAddress());
                    if (actions.get(index).getAddress() != nextAddress) {
                        if (!action.isExit() && !(action instanceof ActionJump)) {
                            ActionJump jump = new ActionDeobfuscateJump(0);
                            jump.setAddress(action.getAddress());
                            int size = jump.getTotalActionLength();
                            jump.setJumpOffset((int) (nextAddress - action.getAddress() - size));
                            ret.add(jump);
                        }
                    }
                }
            }
        } else {
            ret.addAll(actions);
        }

        // Map for storing the targers of the "jump" actions
        // "jump" action can be ActionIf, ActionJump and any ActionStore
        Map<Action, Action> jumps = new HashMap<>();
        getJumps(ret, jumps);

        updateActionLengths(ret, version);
        updateAddresses(ret, 0);
        long endAddress = ret.get(ret.size() - 1).getAddress();

        updateJumps(ret, jumps, containerLastActions, endAddress);
        updateActionStores(ret, jumps);
        updateContainerSizes(ret, containerLastActions);

        return ret;
    }

    public static List<Action> getOriginalActions(SWFInputStream sis, int startIp, int endIp) throws IOException, InterruptedException {
        // Map of the actions. Use TreeMap to sort the keys in ascending order
        Map<Long, Action> actionMap = new TreeMap<>();
        Map<Long, Long> nextOffsets = new HashMap<>();
        readActionListAtPos(new ArrayList<>(), null,
                sis, actionMap, nextOffsets,
                startIp, startIp, endIp + 1, "", false, new ArrayList<>());

        return new ArrayList<>(actionMap.values());
    }

    /**
     * Reads list of actions from the stream. Reading ends with
     * ActionEndFlag(=0) or end of the stream.
     *
     * @param listeners
     * @param actions
     * @param version
     * @param ip
     * @param path
     * @return List of actions
     * @throws IOException
     * @throws java.lang.InterruptedException
     */
    private static ActionList deobfuscateActionListOld(List<DisassemblyListener> listeners, ActionList actions, int version, int ip, String path) throws IOException, InterruptedException {
        if (actions.isEmpty()) {
            return actions;
        }

        Action lastAction = actions.get(actions.size() - 1);
        int endIp = (int) lastAction.getAddress();

        List<Action> retMap = new ArrayList<>(endIp);
        for (int i = 0; i < endIp; i++) {
            retMap.add(null);
        }
        List<Action> actionMap = new ArrayList<>(endIp + 1);
        for (int i = 0; i <= endIp; i++) {
            actionMap.add(null);
        }
        for (Action a : actions) {
            actionMap.set((int) a.getAddress(), a);
        }

        int maxRecursionLevel = 0;
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if (a instanceof ActionIf || a instanceof GraphSourceItemContainer) {
                maxRecursionLevel++;
            }
            if (a instanceof ActionIf) {
                ActionIf aif = (ActionIf) a;
                aif.ignoreUsed = false;
                aif.jumpUsed = false;
            }
        }

        deobfustaceActionListAtPosRecursiveOld(listeners,
                new ArrayList<>(),
                new HashMap<>(),
                new ActionLocalData(),
                new TranslateStack(path),
                new ConstantPool(),
                actionMap, ip, retMap, ip, endIp, path,
                new HashMap<>(), false,
                new HashMap<>(),
                version, 0, maxRecursionLevel);

        ActionList ret = new ActionList();
        Action last = null;
        for (Action a : retMap) {
            if (a != last && a != null) {
                ret.add(a);
            }
            last = a;
        }
        ret.removeNops();
        ActionList reta = new ActionList();
        for (Object o : ret) {
            if (o instanceof Action) {
                reta.add((Action) o);
            }
        }
        return reta;
    }

    private static long getNearAddress(ActionList actions, long address, boolean next) {
        int min = 0;
        int max = actions.size() - 1;

        while (max >= min) {
            int mid = (min + max) / 2;
            long midValue = actions.get(mid).getAddress();
            if (midValue == address) {
                return address;
            } else if (midValue < address) {
                min = mid + 1;
            } else {
                max = mid - 1;
            }
        }

        return next
                ? (min < actions.size() ? actions.get(min).getAddress() : -1)
                : (max >= 0 ? actions.get(max).getAddress() : -1);
    }

    private static Map<Long, Action> actionListToMap(List<Action> actions) {
        Map<Long, Action> map = new HashMap<>(actions.size());
        for (Action a : actions) {
            long address = a.getAddress();
            // There are multiple actions in the same address (2nd action is a jump for obfuscated code)
            // So this check is required
            if (!map.containsKey(address)) {
                map.put(a.getAddress(), a);
            }
        }
        return map;
    }

    private static void getJumps(List<Action> actions, Map<Action, Action> jumps) {
        Map<Long, Action> actionMap = actionListToMap(actions);
        for (Action a : actions) {
            long target = -1;
            if (a instanceof ActionIf) {
                ActionIf aIf = (ActionIf) a;
                target = aIf.getAddress() + a.getTotalActionLength() + aIf.getJumpOffset();
            } else if (a instanceof ActionJump) {
                ActionJump aJump = (ActionJump) a;
                target = aJump.getAddress() + a.getTotalActionLength() + aJump.getJumpOffset();
            } else if (a instanceof ActionStore) {
                ActionStore aStore = (ActionStore) a;
                int storeSize = aStore.getStoreSize();
                // skip storeSize + 1 actions (+1 is the current action)
                Action targetAction = a;
                for (int i = 0; i <= storeSize; i++) {
                    long address = targetAction.getAddress() + targetAction.getTotalActionLength();
                    targetAction = actionMap.get(address);
                    if (targetAction == null) {
                        break;
                    }
                }
                jumps.put(a, targetAction);
            }
            if (target >= 0) {
                Action targetAction = actionMap.get(target);
                jumps.put(a, targetAction);
            }
        }
    }

    public static List<Action> getContainerLastActions(ActionList actions, Action action) {
        GraphSourceItemContainer container = (GraphSourceItemContainer) action;
        List<Long> sizes = container.getContainerSizes();
        long endAddress = action.getAddress() + container.getHeaderSize();
        List<Action> lasts = new ArrayList<>(sizes.size());
        for (long size : sizes) {
            endAddress += size;
            long lastActionAddress = getNearAddress(actions, endAddress - 1, false);
            Action lastAction = null;
            if (lastActionAddress != -1) {
                lastAction = actions.getByAddress(lastActionAddress);
            }
            lasts.add(lastAction);
        }
        return lasts;
    }

    private static void getContainerLastActions(ActionList actions, Map<Action, List<Action>> lastActions) {
        for (Action a : actions) {
            if (a instanceof GraphSourceItemContainer) {
                lastActions.put(a, getContainerLastActions(actions, a));
            }
        }
    }

    private static long updateAddresses(List<Action> actions, long address) {
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            a.setAddress(address);
            int length = a.getTotalActionLength();
            if ((i != actions.size() - 1) && (a instanceof ActionEnd)) {
                // placeholder for jump action
                length = new ActionDeobfuscateJump(0).getTotalActionLength();
            }
            address += length;
        }
        return address;
    }

    private static void updateActionLengths(List<Action> actions, int version) {
        for (int i = 0; i < actions.size(); i++) {
            actions.get(i).updateLength(version);
        }
    }

    private static void updateActionStores(List<Action> actions, Map<Action, Action> jumps) {
        Map<Long, Action> actionMap = actionListToMap(actions);
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if (a instanceof ActionStore) {
                ActionStore aStore = (ActionStore) a;
                Action nextActionAfterStore = jumps.get(a);
                Action a1 = a;
                List<Action> store = new ArrayList<>();
                while (true) {
                    long address = a1.getAddress() + a1.getTotalActionLength();
                    a1 = actionMap.get(address);
                    if (a1 == null || a1 == nextActionAfterStore) {
                        break;
                    }
                    store.add(a1);
                }
                aStore.setStore(store);
            }
        }
    }

    private static void updateContainerSizes(List<Action> actions, Map<Action, List<Action>> containerLastActions) {
        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if (a instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer container = (GraphSourceItemContainer) a;
                List<Action> lastActions = containerLastActions.get(a);
                long startAddress = a.getAddress() + container.getHeaderSize();
                for (int j = 0; j < lastActions.size(); j++) {
                    Action lastAction = lastActions.get(j);
                    int length = (int) (lastAction.getAddress() + lastAction.getTotalActionLength() - startAddress);
                    container.setContainerSize(j, length);
                    startAddress += length;
                }
            }
        }
    }

    private static void replaceJumpTargets(Map<Action, Action> jumps, Action oldTarget, Action newTarget) {
        for (Action a : jumps.keySet()) {
            if (jumps.get(a) == oldTarget) {
                jumps.put(a, newTarget);
            }
        }
    }

    private static void replaceContainerLastActions(Map<Action, List<Action>> containerLastActions, Action oldTarget, Action newTarget) {
        for (Action a : containerLastActions.keySet()) {
            List<Action> targets = containerLastActions.get(a);
            for (int i = 0; i < targets.size(); i++) {
                if (targets.get(i) == oldTarget) {
                    targets.set(i, newTarget);
                }
            }
        }
    }

    private static void updateJumps(List<Action> actions, Map<Action, Action> jumps, Map<Action, List<Action>> containerLastActions, long endAddress) {
        if (actions.isEmpty()) {
            return;
        }

        for (int i = 0; i < actions.size(); i++) {
            Action a = actions.get(i);
            if ((i != actions.size() - 1) && (a instanceof ActionEnd)) {
                ActionJump aJump = new ActionDeobfuscateJump(0);
                aJump.setJumpOffset((int) (endAddress - a.getAddress() - aJump.getTotalActionLength()));
                aJump.setAddress(a.getAddress());
                replaceJumpTargets(jumps, a, aJump);
                replaceContainerLastActions(containerLastActions, a, aJump);
                a = aJump;
                actions.set(i, a);
            } else if (a instanceof ActionIf) {
                ActionIf aIf = (ActionIf) a;
                Action target = jumps.get(a);
                long offset;
                if (target != null) {
                    offset = target.getAddress() - a.getAddress() - a.getTotalActionLength();
                } else {
                    offset = endAddress - a.getAddress() - a.getTotalActionLength();
                }
                aIf.setJumpOffset((int) offset);
            } else if (a instanceof ActionJump) {
                ActionJump aJump = (ActionJump) a;
                Action target = jumps.get(a);
                long offset;
                if (target != null) {
                    offset = target.getAddress() - a.getAddress() - a.getTotalActionLength();
                } else {
                    offset = endAddress - a.getAddress() - a.getTotalActionLength();
                }
                aJump.setJumpOffset((int) offset);
            }
        }
    }

    /**
     * Removes an action from the action list, and updates all references This
     * method will keep the inner actions of the container when you remove the
     * container
     *
     * @param actions
     * @param index
     * @param version
     * @param removeWhenLast
     * @return
     */
    public static boolean removeAction(ActionList actions, int index, int version, boolean removeWhenLast) {

        if (index < 0 || actions.size() <= index) {
            return false;
        }

        long startIp = actions.get(0).getAddress();
        Action lastAction = actions.get(actions.size() - 1);
        long endAddress = lastAction.getAddress() + lastAction.getTotalActionLength();

        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(actions, containerLastActions);

        Map<Action, Action> jumps = new HashMap<>();
        getJumps(actions, jumps);

        Action prevAction = index > 0 ? actions.get(index - 1) : null;
        Action nextAction = index + 1 < actions.size() ? actions.get(index + 1) : null;
        Action actionToRemove = actions.get(index);
        for (Action a : containerLastActions.keySet()) {
            List<Action> lastActions = containerLastActions.get(a);
            for (int i = 0; i < lastActions.size(); i++) {
                if (lastActions.get(i) == actionToRemove) {
                    if (!removeWhenLast) {
                        return false;
                    }
                    lastActions.set(i, prevAction);
                }
            }
        }
        for (Action a : jumps.keySet()) {
            Action targetAction = jumps.get(a);
            if (targetAction == actionToRemove) {
                jumps.put(a, nextAction);
            }
        }
        if (containerLastActions.containsKey(actionToRemove)) {
            containerLastActions.remove(actionToRemove);
        }
        if (jumps.containsKey(actionToRemove)) {
            jumps.remove(actionToRemove);
        }

        actions.remove(index);

        updateActionLengths(actions, version);
        updateAddresses(actions, startIp);
        updateJumps(actions, jumps, containerLastActions, endAddress);
        updateActionStores(actions, jumps);
        updateContainerSizes(actions, containerLastActions);

        return true;
    }

    /**
     * Removes multiple actions from the action list, and updates all references
     * This
     * method will keep the inner actions of the container when you remove the
     * container
     *
     * @param actions
     * @param actionsToRemove
     * @param version
     * @param removeWhenLast
     * @return
     */
    public static boolean removeActions(ActionList actions, List<Action> actionsToRemove, int version, boolean removeWhenLast) {

        long startIp = actions.get(0).getAddress();
        Action lastAction = actions.get(actions.size() - 1);
        long endAddress = lastAction.getAddress() + lastAction.getTotalActionLength();

        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(actions, containerLastActions);

        Map<Action, Action> jumps = new HashMap<>();
        getJumps(actions, jumps);

        for (Action actionToRemove : actionsToRemove) {
            int index = actions.indexOf(actionToRemove);
            Action prevAction = index > 0 ? actions.get(index - 1) : null;
            Action nextAction = index + 1 < actions.size() ? actions.get(index + 1) : null;
            for (Action a : containerLastActions.keySet()) {
                List<Action> lastActions = containerLastActions.get(a);
                for (int i = 0; i < lastActions.size(); i++) {
                    if (lastActions.get(i) == actionToRemove) {
                        if (!removeWhenLast) {
                            return false;
                        }
                        lastActions.set(i, prevAction);
                    }
                }
            }

            for (Action a : jumps.keySet()) {
                Action targetAction = jumps.get(a);
                if (targetAction == actionToRemove) {
                    jumps.put(a, nextAction);
                }
            }
            if (containerLastActions.containsKey(actionToRemove)) {
                containerLastActions.remove(actionToRemove);
            }
            if (jumps.containsKey(actionToRemove)) {
                jumps.remove(actionToRemove);
            }

            actions.remove(index);
        }

        updateActionLengths(actions, version);
        updateAddresses(actions, startIp);
        updateJumps(actions, jumps, containerLastActions, endAddress);
        updateActionStores(actions, jumps);
        updateContainerSizes(actions, containerLastActions);

        return true;
    }

    /**
     * Adds an action to the action list to the specified location, and updates
     * all references
     *
     * @param actions
     * @param index
     * @param action
     * @param version
     * @param addToContainer
     * @param replaceJump
     * @return
     */
    public static boolean addAction(ActionList actions, int index, Action action,
            int version, boolean addToContainer, boolean replaceJump) {

        if (index < 0 || actions.size() < index) {
            return false;
        }

        long startIp = actions.get(0).getAddress();
        Action lastAction = actions.get(actions.size() - 1);
        if (!(lastAction instanceof ActionEnd)) {
            Action aEnd = new ActionEnd();
            aEnd.setAddress(lastAction.getAddress() + lastAction.getTotalActionLength());
            actions.add(aEnd);
            lastAction = aEnd;
        }

        long endAddress = lastAction.getAddress();

        Map<Action, List<Action>> containerLastActions = new HashMap<>();
        getContainerLastActions(actions, containerLastActions);

        Map<Action, Action> jumps = new HashMap<>();
        List<Action> tempActions = new ArrayList<>(actions);
        tempActions.add(action);
        getJumps(tempActions, jumps);

        Action prevAction = actions.get(index);
        if (addToContainer) {
            for (Action a : containerLastActions.keySet()) {
                List<Action> lastActions = containerLastActions.get(a);
                for (int i = 0; i < lastActions.size(); i++) {
                    if (lastActions.get(i) == prevAction) {
                        lastActions.set(i, action);
                    }
                }
            }
        }

        if (replaceJump) {
            for (Action a : jumps.keySet()) {
                Action targetAction = jumps.get(a);
                if (targetAction == prevAction) {
                    jumps.put(a, action);
                }
            }
        }

        actions.add(index, action);

        updateActionLengths(actions, version);
        updateAddresses(actions, startIp);
        updateJumps(actions, jumps, containerLastActions, endAddress);
        updateActionStores(actions, jumps);
        updateContainerSizes(actions, containerLastActions);

        return true;
    }

    private static Action readActionListAtPos(List<DisassemblyListener> listeners, ConstantPool cpool,
            SWFInputStream sis, Map<Long, Action> actions, Map<Long, Long> nextOffsets,
            long ip, long startIp, long endIp, String path, boolean indeterminate, List<Long> visitedContainers) throws IOException {

        Action entryAction = null;

        if (visitedContainers.contains(ip)) {
            return null;
        }
        visitedContainers.add(ip);

        Queue<Long> jumpQueue = new LinkedList<>();
        jumpQueue.add(ip);
        while (!jumpQueue.isEmpty()) {
            ip = jumpQueue.remove();
            if (ip < startIp) {
                continue;
            }

            while (endIp == -1 || endIp > ip) {
                sis.seek((int) ip);

                Action a;
                if ((a = sis.readAction()) == null) {
                    break;
                }

                int actionLengthWithHeader = a.getTotalActionLength();

                // unknown action, replace with jump
                if (a instanceof ActionNop) {
                    ActionJump aJump = new ActionDeobfuscateJump(0);
                    int jumpLength = aJump.getTotalActionLength();
                    aJump.setAddress(a.getAddress());
                    //FIXME! This offset can be larger than SI16 value!
                    aJump.setJumpOffset(actionLengthWithHeader - jumpLength);
                    a = aJump;
                    actionLengthWithHeader = a.getTotalActionLength();
                }

                if (entryAction == null) {
                    entryAction = a;
                }

                Action existingAction = actions.get(ip);
                if (existingAction != null) {
                    break;
                }

                actions.put(ip, a);
                nextOffsets.put(ip, ip + actionLengthWithHeader);

                long pos = sis.getPos();
                long length = pos + sis.available();
                for (int i = 0; i < listeners.size(); i++) {
                    listeners.get(i).progressReading(pos, length);
                }

                a.setAddress(ip);

                if (a instanceof ActionPush && cpool != null) {
                    ((ActionPush) a).constantPool = cpool.constants;
                } else if (a instanceof ActionConstantPool) {
                    cpool = new ConstantPool(((ActionConstantPool) a).constantPool);
                } else if (a instanceof ActionIf) {
                    ActionIf aIf = (ActionIf) a;
                    long nIp = ip + actionLengthWithHeader + aIf.getJumpOffset();
                    if (nIp >= 0) {
                        jumpQueue.add(nIp);
                    }
                } else if (a instanceof ActionJump) {
                    ActionJump aJump = (ActionJump) a;
                    long nIp = ip + actionLengthWithHeader + aJump.getJumpOffset();
                    if (nIp >= 0) {
                        jumpQueue.add(nIp);
                    }
                    break;
                } else if (a instanceof GraphSourceItemContainer) {
                    GraphSourceItemContainer cnt = (GraphSourceItemContainer) a;
                    String cntName = cnt.getName();
                    String newPath = path + (cntName == null ? "" : "/" + cntName);
                    for (long size : cnt.getContainerSizes()) {
                        if (size != 0) {
                            long ip2 = ip + actionLengthWithHeader;
                            long endIp2 = ip + actionLengthWithHeader + size;
                            readActionListAtPos(listeners, cpool,
                                    sis, actions, nextOffsets,
                                    ip2, startIp, endIp2, newPath, indeterminate, visitedContainers);
                            actionLengthWithHeader += size;
                        }
                    }
                }

                ip += actionLengthWithHeader;

                if (a.isExit()) {
                    break;
                }
            }
        }
        return entryAction;
    }

    public static void fixConstantPools(List<DisassemblyListener> listeners, ActionList actions) {
        Action lastAction = actions.get(actions.size() - 1);
        int endIp = (int) lastAction.getAddress();
        List<Action> actionMap = new ArrayList<>(endIp);
        for (int i = 0; i <= endIp; i++) {
            actionMap.add(null);
        }
        for (Action a : actions) {
            actionMap.set((int) a.getAddress(), a);
        }

        try {
            fixConstantPools(listeners, new ConstantPool(), actionMap, new TreeMap<>(), 0, 0, endIp, null, true, new ArrayList<>());
        } catch (IOException ex) {
            // ignore
        }
    }

    private static void fixConstantPools(List<DisassemblyListener> listeners, ConstantPool cpool,
            List<Action> actions, Map<Integer, Action> actionMap,
            int ip, int startIp, int endIp, String path, boolean indeterminate, List<Integer> visitedContainers) throws IOException {

        if (visitedContainers.contains(ip)) {
            return;
        }
        visitedContainers.add(ip);

        Queue<Integer> jumpQueue = new LinkedList<>();
        jumpQueue.add(ip);
        while (!jumpQueue.isEmpty()) {
            ip = jumpQueue.remove();
            if (ip < startIp) {
                continue;
            }

            while (endIp == -1 || endIp > ip) {
                Action a;
                if ((a = actions.get(ip)) == null) {
                    break;
                }

                int actionLengthWithHeader = a.getTotalActionLength();

                // unknown action, replace with jump
                if (a instanceof ActionNop) {
                    ActionJump aJump = new ActionDeobfuscateJump(0);
                    int jumpLength = aJump.getTotalActionLength();
                    aJump.setAddress(a.getAddress());
                    //FIXME! This offset can be larger than SI16 value!
                    aJump.setJumpOffset(actionLengthWithHeader - jumpLength);
                    a = aJump;
                    actionLengthWithHeader = a.getTotalActionLength();
                }

                Action existingAction = actionMap.get(ip);
                if (existingAction != null) {
                    break;
                }

                actionMap.put(ip, a);

                if (listeners != null) {
                    for (int i = 0; i < listeners.size(); i++) {
                        listeners.get(i).progressReading(ip, actions.size());
                    }
                }

                a.setAddress(ip);

                if (a instanceof ActionPush && cpool != null) {
                    ((ActionPush) a).constantPool = cpool.constants;
                } else if (a instanceof ActionConstantPool) {
                    cpool = new ConstantPool(((ActionConstantPool) a).constantPool);
                } else if (a instanceof ActionIf) {
                    ActionIf aIf = (ActionIf) a;
                    int nIp = ip + actionLengthWithHeader + aIf.getJumpOffset();
                    if (nIp >= 0) {
                        jumpQueue.add(nIp);
                    }
                } else if (a instanceof ActionJump) {
                    ActionJump aJump = (ActionJump) a;
                    int nIp = ip + actionLengthWithHeader + aJump.getJumpOffset();
                    if (nIp >= 0) {
                        jumpQueue.add(nIp);
                    }
                    break;
                } else if (a instanceof GraphSourceItemContainer) {
                    GraphSourceItemContainer cnt = (GraphSourceItemContainer) a;
                    String cntName = cnt.getName();
                    String newPath = path + (cntName == null ? "" : "/" + cntName);
                    for (long size : cnt.getContainerSizes()) {
                        if (size != 0) {
                            int ip2 = ip + actionLengthWithHeader;
                            int endIp2 = ip + actionLengthWithHeader + (int) size;
                            fixConstantPools(listeners, cpool, actions, actionMap, ip2, startIp, endIp2, newPath, indeterminate, visitedContainers);
                            actionLengthWithHeader += size;
                        }
                    }
                }

                ip += actionLengthWithHeader;

                if (a.isExit()) {
                    break;
                }
            }
        }
    }

    private static void deobfustaceActionListAtPosRecursiveOld(List<DisassemblyListener> listeners, List<GraphTargetItem> output, HashMap<Long, List<GraphSourceItemContainer>> containers, ActionLocalData localData, TranslateStack stack, ConstantPool cpool, List<Action> actions, int ip, List<Action> ret, int startIp, int endip, String path, Map<Integer, Integer> visited, boolean indeterminate, Map<Integer, HashMap<String, GraphTargetItem>> decisionStates, int version, int recursionLevel, int maxRecursionLevel) throws IOException, InterruptedException {
        boolean debugMode = false;
        boolean decideBranch = false;

        if (recursionLevel > maxRecursionLevel + 1) {
            throw new TranslateException("deobfustaceActionListAtPosRecursive max recursion level reached.");
        }

        Action a;
        Scanner sc = null;
        loopip:
        while (((endip == -1) || (endip > ip)) && (a = actions.get(ip)) != null) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            int actionLen = a.getTotalActionLength();
            if (!visited.containsKey(ip)) {
                visited.put(ip, 0);
            }
            int curVisited = visited.get(ip);
            curVisited++;
            visited.put(ip, curVisited);
            for (int i = 0; i < listeners.size(); i++) {
                listeners.get(i).progressDeobfuscating(ip, actions.size());
            }
            int info = a.getTotalActionLength();

            if (a instanceof ActionPush) {
                if (cpool != null) {
                    ((ActionPush) a).constantPool = cpool.constants;
                }
            }

            if (debugMode) {
                String atos = a.getASMSource(new ActionList(), new HashSet<>(), ScriptExportMode.PCODE);
                if (a instanceof GraphSourceItemContainer) {
                    atos = a.toString();
                }
                System.err.println("readActionListAtPos ip: " + (ip - startIp) + " (0x" + Helper.formatAddress(ip - startIp) + ") " + " action(len " + a.actionLength + "): " + atos + (a.isIgnored() ? " (ignored)" : "") + " stack:" + Helper.stackToString(stack, LocalData.create(cpool)) + " " + Helper.byteArrToString(a.getBytes(version)));
                System.err.print("variables: ");
                for (Map.Entry<String, GraphTargetItem> v : localData.variables.entrySet()) {
                    System.err.print("'" + v + "' = " + v.getValue().toString(LocalData.create(cpool)) + ", ");
                }
                System.err.println();
                String add = "";
                if (a instanceof ActionIf) {
                    add = " change: " + ((ActionIf) a).getJumpOffset();
                }
                if (a instanceof ActionJump) {
                    add = " change: " + ((ActionJump) a).getJumpOffset();
                }
                System.err.println(add);
            }

            int newip = -1;

            if (a instanceof ActionConstantPool) {
                if (cpool == null) {
                    cpool = new ConstantPool();
                }
                cpool.setNew(((ActionConstantPool) a).constantPool);
            }
            ActionIf aif = null;
            boolean goaif = false;
            if (!a.isIgnored()) {
                String varname = null;
                if (a instanceof StoreTypeAction) {
                    StoreTypeAction sta = (StoreTypeAction) a;
                    varname = sta.getVariableName(stack, cpool);
                }

                try {
                    if (a instanceof ActionIf) {
                        aif = (ActionIf) a;

                        GraphTargetItem top = stack.pop();
                        int nip = ip + actionLen + aif.getJumpOffset();

                        if (decideBranch) {
                            System.out.print("newip " + nip + ", ");
                            System.out.print("Action: jump(j),ignore(i),compute(c)?");
                            if (sc == null) {
                                sc = new Scanner(System.in);
                            }
                            String next = sc.next();
                            switch (next) {
                                case "j":
                                    newip = nip;
                                    break;
                                case "i":
                                    break;
                                case "c":
                                    goaif = true;
                                    break;
                            }
                        } else if (top.isCompileTime() && (!top.hasSideEffect())) {
                            if (debugMode) {
                                System.err.print("is compiletime -> ");
                            }
                            if (EcmaScript.toBoolean(top.getResult())) {
                                newip = nip;
                                aif.jumpUsed = true;
                                if (debugMode) {
                                    System.err.println("jump");
                                }
                            } else {
                                aif.ignoreUsed = true;
                                if (debugMode) {
                                    System.err.println("ignore");
                                }
                            }
                        } else {
                            if (debugMode) {
                                System.err.println("goaif");
                            }
                            goaif = true;
                        }
                    } else if (a instanceof ActionJump) {
                        newip = ip + actionLen + ((ActionJump) a).getJumpOffset();
                    } else if (!(a instanceof GraphSourceItemContainer)) {
                        //return in for..in,   TODO:Handle this better way
                        if (((a instanceof ActionEquals) || (a instanceof ActionEquals2)) && (stack.size() == 1) && (stack.peek() instanceof DirectValueActionItem)) {
                            stack.push(new DirectValueActionItem(null, 0, new Null(), new ArrayList<>()));
                        }
                        if ((a instanceof ActionStoreRegister) && stack.isEmpty()) {
                            stack.push(new DirectValueActionItem(null, 0, new Null(), new ArrayList<>()));
                        }
                        a.translate(localData, stack, output, Graph.SOP_USE_STATIC/*Graph.SOP_SKIP_STATIC*/, path);
                    }
                } catch (RuntimeException ex) {
                    logger.log(Level.SEVERE, "Disassembly exception", ex);
                    break;
                }

                HashMap<String, GraphTargetItem> vars = localData.variables;
                if (varname != null) {
                    GraphTargetItem varval = vars.get(varname);
                    if (varval != null && varval.isCompileTime() && indeterminate) {
                        vars.put(varname, new NotCompileTimeItem(null, varval));
                    }
                }
            }
            for (int i = 0; i < actionLen; i++) {
                ret.set(ip + i, a);
            }

            if (a instanceof GraphSourceItemContainer) {
                GraphSourceItemContainer cnt = (GraphSourceItemContainer) a;
                if (a instanceof Action) {
                    long endAddr = a.getAddress() + cnt.getHeaderSize();
                    String cntName = cnt.getName();
                    List<List<GraphTargetItem>> output2s = new ArrayList<>();
                    for (long size : cnt.getContainerSizes()) {
                        if (size == 0) {
                            output2s.add(new ArrayList<>());
                            continue;
                        }
                        ActionLocalData localData2;
                        List<GraphTargetItem> output2 = new ArrayList<>();
                        if ((cnt instanceof ActionDefineFunction) || (cnt instanceof ActionDefineFunction2)) {
                            localData2 = new ActionLocalData();
                        } else {
                            localData2 = localData;
                        }
                        deobfustaceActionListAtPosRecursiveOld(listeners, output2, containers, localData2, new TranslateStack(path), cpool, actions, (int) endAddr, ret, startIp, (int) (endAddr + size), path + (cntName == null ? "" : "/" + cntName), visited, indeterminate, decisionStates, version, recursionLevel + 1, maxRecursionLevel);
                        output2s.add(output2);
                        endAddr += size;
                    }
                    cnt.translateContainer(output2s, stack, output, localData.regNames, localData.variables, localData.functions);
                    ip = (int) endAddr;
                    continue;
                }
            }

            if (a instanceof ActionEnd) {
                break;
            }
            if (goaif) {
                aif.ignoreUsed = true;
                aif.jumpUsed = true;
                indeterminate = true;

                HashMap<String, GraphTargetItem> vars = localData.variables;
                boolean stateChanged = false;
                if (decisionStates.containsKey(ip)) {
                    HashMap<String, GraphTargetItem> oldstate = decisionStates.get(ip);
                    if (oldstate.size() != vars.size()) {
                        stateChanged = true;
                    } else {
                        for (String k : vars.keySet()) {
                            if (!oldstate.containsKey(k)) {
                                stateChanged = true;
                                break;
                            }
                            if (!vars.get(k).isCompileTime() && oldstate.get(k).isCompileTime()) {
                                stateChanged = true;
                                break;
                            }
                        }
                    }
                }
                HashMap<String, GraphTargetItem> curstate = new HashMap<>();
                curstate.putAll(vars);
                decisionStates.put(ip, curstate);

                if ((!stateChanged) && curVisited > 1) {
                    List<Integer> branches = new ArrayList<>();
                    branches.add(ip + actionLen + aif.getJumpOffset());
                    branches.add(ip + actionLen);
                    for (int br : branches) {
                        int visc = 0;
                        if (visited.containsKey(br)) {
                            visc = visited.get(br);
                        }
                        if (visc == 0) {//<curVisited){
                            ip = br;
                            continue loopip;
                        }
                    }
                    break loopip;
                }

                TranslateStack subStack = (TranslateStack) stack.clone();
                ActionLocalData subLocalData = new ActionLocalData(new HashMap<>(localData.regNames),
                        new HashMap<>(localData.variables), new HashMap<>(localData.functions));
                deobfustaceActionListAtPosRecursiveOld(listeners, output, containers, subLocalData, subStack, cpool, actions, ip + actionLen + aif.getJumpOffset(), ret, startIp, endip, path, visited, indeterminate, decisionStates, version, recursionLevel + 1, maxRecursionLevel);
            }

            if (newip > -1) {
                ip = newip;
            } else {
                ip += info;
            }

            if (a.isExit()) {
                break;
            }
        }
        for (DisassemblyListener listener : listeners) {
            listener.progressDeobfuscating(ip, actions.size());
        }
    }
}
