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
package com.jpexs.decompiler.flash.action.parser.script;

import com.jpexs.decompiler.flash.SourceGeneratorLocalData;
import com.jpexs.decompiler.flash.action.Action;
import com.jpexs.decompiler.flash.action.model.AsciiToCharActionItem;
import com.jpexs.decompiler.flash.action.model.CallActionItem;
import com.jpexs.decompiler.flash.action.model.CallFunctionActionItem;
import com.jpexs.decompiler.flash.action.model.CallMethodActionItem;
import com.jpexs.decompiler.flash.action.model.CastOpActionItem;
import com.jpexs.decompiler.flash.action.model.CharToAsciiActionItem;
import com.jpexs.decompiler.flash.action.model.CloneSpriteActionItem;
import com.jpexs.decompiler.flash.action.model.DefineLocalActionItem;
import com.jpexs.decompiler.flash.action.model.DeleteActionItem;
import com.jpexs.decompiler.flash.action.model.DirectValueActionItem;
import com.jpexs.decompiler.flash.action.model.EvalActionItem;
import com.jpexs.decompiler.flash.action.model.FSCommandActionItem;
import com.jpexs.decompiler.flash.action.model.FunctionActionItem;
import com.jpexs.decompiler.flash.action.model.GetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.GetTimeActionItem;
import com.jpexs.decompiler.flash.action.model.GetURL2ActionItem;
import com.jpexs.decompiler.flash.action.model.GetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.GetVersionActionItem;
import com.jpexs.decompiler.flash.action.model.GotoFrame2ActionItem;
import com.jpexs.decompiler.flash.action.model.InitArrayActionItem;
import com.jpexs.decompiler.flash.action.model.InitObjectActionItem;
import com.jpexs.decompiler.flash.action.model.LoadMovieActionItem;
import com.jpexs.decompiler.flash.action.model.LoadMovieNumActionItem;
import com.jpexs.decompiler.flash.action.model.LoadVariablesActionItem;
import com.jpexs.decompiler.flash.action.model.LoadVariablesNumActionItem;
import com.jpexs.decompiler.flash.action.model.MBAsciiToCharActionItem;
import com.jpexs.decompiler.flash.action.model.MBCharToAsciiActionItem;
import com.jpexs.decompiler.flash.action.model.MBStringExtractActionItem;
import com.jpexs.decompiler.flash.action.model.MBStringLengthActionItem;
import com.jpexs.decompiler.flash.action.model.NewMethodActionItem;
import com.jpexs.decompiler.flash.action.model.NewObjectActionItem;
import com.jpexs.decompiler.flash.action.model.NextFrameActionItem;
import com.jpexs.decompiler.flash.action.model.PlayActionItem;
import com.jpexs.decompiler.flash.action.model.PostDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.PostIncrementActionItem;
import com.jpexs.decompiler.flash.action.model.PrevFrameActionItem;
import com.jpexs.decompiler.flash.action.model.PrintActionItem;
import com.jpexs.decompiler.flash.action.model.PrintAsBitmapActionItem;
import com.jpexs.decompiler.flash.action.model.PrintAsBitmapNumActionItem;
import com.jpexs.decompiler.flash.action.model.PrintNumActionItem;
import com.jpexs.decompiler.flash.action.model.RandomNumberActionItem;
import com.jpexs.decompiler.flash.action.model.RemoveSpriteActionItem;
import com.jpexs.decompiler.flash.action.model.ReturnActionItem;
import com.jpexs.decompiler.flash.action.model.SetMemberActionItem;
import com.jpexs.decompiler.flash.action.model.SetVariableActionItem;
import com.jpexs.decompiler.flash.action.model.StartDragActionItem;
import com.jpexs.decompiler.flash.action.model.StopActionItem;
import com.jpexs.decompiler.flash.action.model.StopAllSoundsActionItem;
import com.jpexs.decompiler.flash.action.model.StopDragActionItem;
import com.jpexs.decompiler.flash.action.model.StringExtractActionItem;
import com.jpexs.decompiler.flash.action.model.StringLengthActionItem;
import com.jpexs.decompiler.flash.action.model.TargetPathActionItem;
import com.jpexs.decompiler.flash.action.model.ThrowActionItem;
import com.jpexs.decompiler.flash.action.model.ToIntegerActionItem;
import com.jpexs.decompiler.flash.action.model.ToNumberActionItem;
import com.jpexs.decompiler.flash.action.model.ToStringActionItem;
import com.jpexs.decompiler.flash.action.model.ToggleHighQualityActionItem;
import com.jpexs.decompiler.flash.action.model.TraceActionItem;
import com.jpexs.decompiler.flash.action.model.TypeOfActionItem;
import com.jpexs.decompiler.flash.action.model.UnLoadMovieActionItem;
import com.jpexs.decompiler.flash.action.model.UnLoadMovieNumActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.ClassActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.ForInActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.IfFrameLoadedActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.InterfaceActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.TellTargetActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.TryActionItem;
import com.jpexs.decompiler.flash.action.model.clauses.WithActionItem;
import com.jpexs.decompiler.flash.action.model.operations.AddActionItem;
import com.jpexs.decompiler.flash.action.model.operations.AndActionItem;
import com.jpexs.decompiler.flash.action.model.operations.BitAndActionItem;
import com.jpexs.decompiler.flash.action.model.operations.BitOrActionItem;
import com.jpexs.decompiler.flash.action.model.operations.BitXorActionItem;
import com.jpexs.decompiler.flash.action.model.operations.DivideActionItem;
import com.jpexs.decompiler.flash.action.model.operations.EqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.GeActionItem;
import com.jpexs.decompiler.flash.action.model.operations.GtActionItem;
import com.jpexs.decompiler.flash.action.model.operations.InstanceOfActionItem;
import com.jpexs.decompiler.flash.action.model.operations.LShiftActionItem;
import com.jpexs.decompiler.flash.action.model.operations.LeActionItem;
import com.jpexs.decompiler.flash.action.model.operations.LtActionItem;
import com.jpexs.decompiler.flash.action.model.operations.ModuloActionItem;
import com.jpexs.decompiler.flash.action.model.operations.MultiplyActionItem;
import com.jpexs.decompiler.flash.action.model.operations.NeqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.OrActionItem;
import com.jpexs.decompiler.flash.action.model.operations.PreDecrementActionItem;
import com.jpexs.decompiler.flash.action.model.operations.PreIncrementActionItem;
import com.jpexs.decompiler.flash.action.model.operations.RShiftActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StrictEqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StrictNeqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringAddActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringEqActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringGeActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringGtActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringLeActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringLtActionItem;
import com.jpexs.decompiler.flash.action.model.operations.StringNeActionItem;
import com.jpexs.decompiler.flash.action.model.operations.SubtractActionItem;
import com.jpexs.decompiler.flash.action.model.operations.URShiftActionItem;
import com.jpexs.decompiler.flash.action.parser.ActionParseException;
import com.jpexs.decompiler.flash.action.swf4.ActionIf;
import com.jpexs.decompiler.flash.action.swf4.ConstantIndex;
import com.jpexs.decompiler.flash.action.swf5.ActionConstantPool;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.flash.helpers.GraphTextWriter;
import com.jpexs.decompiler.flash.helpers.collections.MyEntry;
import com.jpexs.decompiler.graph.CompilationException;
import com.jpexs.decompiler.graph.GraphSourceItem;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.TypeItem;
import com.jpexs.decompiler.graph.model.AndItem;
import com.jpexs.decompiler.graph.model.BlockItem;
import com.jpexs.decompiler.graph.model.BreakItem;
import com.jpexs.decompiler.graph.model.CommaExpressionItem;
import com.jpexs.decompiler.graph.model.ContinueItem;
import com.jpexs.decompiler.graph.model.DoWhileItem;
import com.jpexs.decompiler.graph.model.ForItem;
import com.jpexs.decompiler.graph.model.IfItem;
import com.jpexs.decompiler.graph.model.LocalData;
import com.jpexs.decompiler.graph.model.NotItem;
import com.jpexs.decompiler.graph.model.OrItem;
import com.jpexs.decompiler.graph.model.ParenthesisItem;
import com.jpexs.decompiler.graph.model.SwitchItem;
import com.jpexs.decompiler.graph.model.TernarOpItem;
import com.jpexs.decompiler.graph.model.WhileItem;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class ActionScript2Parser {

    private final int swfVersion;

    public ActionScript2Parser(int swfVersion) {
        this.swfVersion = swfVersion;
    }

    private long uniqLast = 0;

    private final boolean debugMode = false;

    private String uniqId() {
        uniqLast++;
        return "" + uniqLast;
    }

    private List<GraphTargetItem> commands(boolean inFunction, boolean inMethod, int forinlevel, List<VariableActionItem> variables) throws IOException, ActionParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        if (debugMode) {
            System.out.println("commands:");
        }
        GraphTargetItem cmd = null;
        while ((cmd = command(inFunction, inMethod, forinlevel, true, variables)) != null) {
            ret.add(cmd);
        }
        if (debugMode) {
            System.out.println("/commands");
        }
        return ret;
    }

    private GraphTargetItem type(List<VariableActionItem> variables) throws IOException, ActionParseException {
        GraphTargetItem ret = null;

        ParsedSymbol s = lex();
        expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.STRING_OP);
        ret = new VariableActionItem(s.value.toString(), null, false);
        variables.add((VariableActionItem) ret);
        s = lex();
        while (s.type == SymbolType.DOT) {
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.STRING_OP, SymbolGroup.GLOBALFUNC);
            ret = new GetMemberActionItem(null, ret, pushConst(s.value.toString()));
            s = lex();
        }
        lexer.pushback(s);
        return ret;
    }

    /*private GraphTargetItem variable(boolean inFunction, boolean inMethod, List<VariableActionItem> variables) throws IOException, ActionParseException {
     GraphTargetItem ret = null;
     ParsedSymbol s = lex();
     expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP);
     ret = new VariableActionItem(s.value.toString(), null, false);
     variables.add((VariableActionItem) ret);
     ret = (member(ret, inFunction, inMethod, variables));
     return ret;
     }*/
    private void expected(ParsedSymbol symb, int line, Object... expected) throws IOException, ActionParseException {
        boolean found = false;
        for (Object t : expected) {
            if (symb.type == t) {
                found = true;
            }
            if (symb.group == t) {
                found = true;
            }
        }
        if (!found) {
            String expStr = "";
            boolean first = true;
            for (Object e : expected) {
                if (!first) {
                    expStr += " or ";
                }
                expStr += e;
                first = false;
            }
            throw new ActionParseException("" + expStr + " expected but " + symb.type + " found", line);
        }
    }

    private ParsedSymbol expectedType(Object... type) throws IOException, ActionParseException {
        ParsedSymbol symb = lex();
        expected(symb, lexer.yyline(), type);
        return symb;
    }

    private ParsedSymbol lex() throws IOException, ActionParseException {
        ParsedSymbol ret = lexer.lex();
        if (debugMode) {
            System.out.println(ret);
        }
        return ret;
    }

    private List<GraphTargetItem> call(boolean inFunction, boolean inMethod, List<VariableActionItem> variables) throws IOException, ActionParseException {
        List<GraphTargetItem> ret = new ArrayList<>();
        //expected(SymbolType.PARENT_OPEN); //MUST BE HANDLED BY CALLER
        ParsedSymbol s = lex();
        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            ret.add(expression(inFunction, inMethod, true, variables));
            s = lex();
            expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
        }
        return ret;
    }

    private FunctionActionItem function(boolean withBody, String functionName, boolean isMethod, List<VariableActionItem> variables) throws IOException, ActionParseException {
        GraphTargetItem ret = null;
        ParsedSymbol s = null;
        expectedType(SymbolType.PARENT_OPEN);
        s = lex();
        List<String> paramNames = new ArrayList<>();

        while (s.type != SymbolType.PARENT_CLOSE) {
            if (s.type != SymbolType.COMMA) {
                lexer.pushback(s);
            }
            s = lex();
            expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
            paramNames.add(s.value.toString());
            s = lex();
            if (s.type == SymbolType.COLON) {
                type(variables);
                s = lex();
            }

            if (!s.isType(SymbolType.COMMA, SymbolType.PARENT_CLOSE)) {
                expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.PARENT_CLOSE);
            }
        }
        List<GraphTargetItem> body = null;
        List<VariableActionItem> subvariables = new ArrayList<>();
        if (withBody) {
            expectedType(SymbolType.CURLY_OPEN);

            body = commands(true, isMethod, 0, subvariables);
            expectedType(SymbolType.CURLY_CLOSE);
        }

        return new FunctionActionItem(null, functionName, paramNames, body, constantPool, -1, subvariables);
    }

    private GraphTargetItem traits(boolean isInterface, GraphTargetItem nameStr, GraphTargetItem extendsStr, List<GraphTargetItem> implementsStr, List<VariableActionItem> variables) throws IOException, ActionParseException {

        GraphTargetItem ret = null;
        /*for (int i = 0; i < nameStr.size() - 1; i++) {
         List<GraphTargetItem> notBody = new ArrayList<>();
         List<String> globalClassTypeStr = new ArrayList<>();
         globalClassTypeStr.add("_global");
         for (int j = 0; j <= i; j++) {
         globalClassTypeStr.add(nameStr.get(j));
         }

         List<GraphTargetItem> val = new ArrayList<>();
         val.add(new ActionPush((Long) 0L));
         val.add(pushConst("Object"));
         val.add(new ActionNewObject());
         notBody.addAll(typeToActions(globalClassTypeStr, val));
         ret.addAll(typeToActions(globalClassTypeStr, null));
         ret.add(new ActionNot());
         ret.add(new ActionNot());
         ret.add(new ActionIf(GraphTargetItem.actionsToBytes(notBody, false, SWF.DEFAULT_VERSION).length));
         ret.addAll(notBody);
         }
         List<GraphTargetItem> ifbody = new ArrayList<>();
         List<String> globalClassTypeStr = new ArrayList<>();
         globalClassTypeStr.add("_global");
         globalClassTypeStr.addAll(nameStr);*/

        ParsedSymbol s = null;
        FunctionActionItem constr = null;
        List<GraphTargetItem> staticFunctions = new ArrayList<>();
        List<MyEntry<GraphTargetItem, GraphTargetItem>> staticVars = new ArrayList<>();
        List<GraphTargetItem> functions = new ArrayList<>();
        List<MyEntry<GraphTargetItem, GraphTargetItem>> vars = new ArrayList<>();

        String classNameStr = "";
        if (nameStr instanceof GetMemberActionItem) {
            GetMemberActionItem mem = (GetMemberActionItem) nameStr;
            if (mem.memberName instanceof VariableActionItem) {
                classNameStr = ((VariableActionItem) mem.memberName).getVariableName();
            } else if (mem.memberName instanceof DirectValueActionItem) {
                classNameStr = ((DirectValueActionItem) mem.memberName).toStringNoQuotes(LocalData.empty);
            }
        } else if (nameStr instanceof VariableActionItem) {
            VariableActionItem var = (VariableActionItem) nameStr;
            classNameStr = var.getVariableName();
        }

        looptrait:
        while (true) {
            s = lex();
            boolean isStatic = false;
            while (s.isType(SymbolType.STATIC, SymbolType.PUBLIC, SymbolType.PRIVATE)) {
                if (s.type == SymbolType.STATIC) {
                    isStatic = true;
                }
                s = lex();
            }
            switch (s.type) {
                case FUNCTION:
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolGroup.GLOBALFUNC);
                    String fname = s.value.toString();
                    if (fname.equals(classNameStr)) { //constructor
                        constr = (function(!isInterface, "", true, variables));
                    } else {
                        if (!isInterface) {
                            if (isStatic) {
                                FunctionActionItem ft = function(!isInterface, "", true, variables);
                                ft.calculatedFunctionName = pushConst(fname);
                                staticFunctions.add(ft);
                            } else {
                                FunctionActionItem ft = function(!isInterface, "", true, variables);
                                ft.calculatedFunctionName = pushConst(fname);
                                functions.add(ft);
                            }
                        }
                    }
                    break;
                case VAR:
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    String ident = s.value.toString();
                    s = lex();
                    if (s.type == SymbolType.COLON) {
                        type(variables);
                        s = lex();
                    }
                    if (s.type == SymbolType.ASSIGN) {
                        if (isStatic) {
                            staticVars.add(new MyEntry<>(pushConst(ident), expression(false, false, true, variables)));
                        } else {
                            vars.add(new MyEntry<>(pushConst(ident), expression(false, false, true, variables)));
                        }
                        s = lex();
                    }
                    if (s.type != SymbolType.SEMICOLON) {
                        lexer.pushback(s);
                    }
                    break;
                default:
                    lexer.pushback(s);
                    break looptrait;

            }
        }

        if (isInterface) {
            return new InterfaceActionItem(nameStr, implementsStr);
        } else {
            return new ClassActionItem(nameStr, extendsStr, implementsStr, constr, functions, vars, staticFunctions, staticVars);
        }
    }

    private GraphTargetItem expressionCommands(ParsedSymbol s, boolean inFunction, boolean inMethod, int forinlevel, List<VariableActionItem> variables) throws IOException, ActionParseException {
        if (debugMode) {
            System.out.println("expressionCommands:");
        }
        GraphTargetItem ret = null;
        switch (s.type) {
            case GETVERSION:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new GetVersionActionItem(null);
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBORD:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBCharToAsciiActionItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBCHR:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBAsciiToCharActionItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBLENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBStringLengthActionItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBSUBSTRING:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem val1 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem index1 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem len1 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new MBStringExtractActionItem(null, val1, index1, len1);
                break;
            case SUBSTR:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem val2 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem index2 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem len2 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StringExtractActionItem(null, val2, index2, len2);
                break;
            case LENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new StringLengthActionItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case RANDOM:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new RandomNumberActionItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case INT:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new ToIntegerActionItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case NUMBER_OP:
                ParsedSymbol sopn = s;
                s = lex();
                if (s.type == SymbolType.DOT) {
                    lexer.pushback(s);
                    VariableActionItem vi = new VariableActionItem(sopn.value.toString(), null, false);
                    variables.add(vi);
                    ret = vi; //memberOrCall(vi, inFunction, inMethod, variables);
                } else {
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    ret = new ToNumberActionItem(null, expression(inFunction, inMethod, true, variables));
                    expectedType(SymbolType.PARENT_CLOSE);
                }
                break;
            case STRING_OP:
                ParsedSymbol sop = s;
                s = lex();
                if (s.type == SymbolType.DOT) {
                    lexer.pushback(s);
                    VariableActionItem vi2 = new VariableActionItem(sop.value.toString(), null, false);
                    variables.add(vi2);
                    ret = vi2; //memberOrCall(vi2, inFunction, inMethod, variables);
                } else {
                    expected(s, lexer.yyline(), SymbolType.PARENT_OPEN);
                    ret = new ToStringActionItem(null, expression(inFunction, inMethod, true, variables));
                    expectedType(SymbolType.PARENT_CLOSE);
                    //ret = memberOrCall(ret, inFunction, inMethod, variables);
                }
                break;
            case ORD:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new CharToAsciiActionItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case CHR:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new AsciiToCharActionItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case DUPLICATEMOVIECLIP:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem src3 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem tar3 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem dep3 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new CloneSpriteActionItem(null, src3, tar3, dep3);
                break;
            case GETTIMER:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new GetTimeActionItem(null);
                break;
            default:
                return null;
        }
        if (debugMode) {
            System.out.println("/expressionCommands");
        }
        return ret;
    }

    private GraphTargetItem command(boolean inFunction, boolean inMethod, int forinlevel, boolean mustBeCommand, List<VariableActionItem> variables) throws IOException, ActionParseException {
        LexBufferer buf = new LexBufferer();
        lexer.addListener(buf);
        GraphTargetItem ret = null;
        if (debugMode) {
            System.out.println("command:");
        }
        ParsedSymbol s = lex();
        if (s.type == SymbolType.EOF) {
            return null;
        }
        switch (s.type) {
            case FSCOMMAND:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new FSCommandActionItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case CALL:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new CallActionItem(null, (expression(inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case LENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new StringLengthActionItem(null, (expression(inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case MBLENGTH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new MBStringLengthActionItem(null, (expression(inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case SET:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem name1 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem value1 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new SetVariableActionItem(null, name1, value1);
                break;
            case WITH:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem wvar = expression(inFunction, inMethod, false, variables);//(variable(inFunction, inMethod, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> wcmd = commands(inFunction, inMethod, forinlevel, variables);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new WithActionItem(null, wvar, wcmd);
                break;
            case DELETE:
                GraphTargetItem varDel = expression(inFunction, inMethod, false, variables);//variable(inFunction, inMethod, variables);
                if (varDel instanceof GetMemberActionItem) {
                    GetMemberActionItem gm = (GetMemberActionItem) varDel;
                    ret = new DeleteActionItem(null, gm.object, gm.memberName);
                } else if (varDel instanceof VariableActionItem) {
                    variables.remove(varDel);
                    ret = new DeleteActionItem(null, null, pushConst(((VariableActionItem) varDel).getVariableName()));
                } else {
                    throw new ActionParseException("Not a property", lexer.yyline());
                }
                break;
            case TRACE:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new TraceActionItem(null, (expression(inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;

            case GETURL:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem url = (expression(inFunction, inMethod, true, variables));
                s = lex();
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                int getuMethod = 1;
                GraphTargetItem target = null;
                if (s.type == SymbolType.COMMA) {
                    target = (expression(inFunction, inMethod, true, variables));
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        s = lex();
                        expected(s, lexer.yyline(), SymbolType.STRING);
                        if (s.value.equals("GET")) {
                            getuMethod = 1;
                        } else if (s.value.equals("POST")) {
                            getuMethod = 2;
                        } else {
                            throw new ActionParseException("Invalid method, \"GET\" or \"POST\" expected.", lexer.yyline());
                        }
                    } else {
                        lexer.pushback(s);
                    }
                } else {
                    lexer.pushback(s);
                    target = new DirectValueActionItem(null, 0, "", new ArrayList<>());
                }
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new GetURL2ActionItem(null, url, target, getuMethod);
                break;
            case GOTOANDSTOP:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem gtsFrame = expression(inFunction, inMethod, true, variables);
                s = lex();
                if (s.type == SymbolType.COMMA) { //Handle scene?
                    s = lex();
                    gtsFrame = expression(inFunction, inMethod, true, variables);
                } else {
                    lexer.pushback(s);
                }
                ret = new GotoFrame2ActionItem(null, gtsFrame, false, false, 0);
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case NEXTFRAME:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new NextFrameActionItem(null);
                break;
            case PLAY:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new PlayActionItem(null);
                break;
            case PREVFRAME:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new PrevFrameActionItem(null);
                break;
            case TELLTARGET:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem tellTarget = expression(inFunction, inMethod, true, variables);
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> tellcmds = commands(inFunction, inMethod, forinlevel, variables);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new TellTargetActionItem(null, tellTarget, tellcmds);
                break;
            case STOP:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopActionItem(null);
                break;
            case STOPALLSOUNDS:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopAllSoundsActionItem(null);
                break;
            case TOGGLEHIGHQUALITY:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new ToggleHighQualityActionItem(null);
                break;

            case STOPDRAG:
                expectedType(SymbolType.PARENT_OPEN);
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StopDragActionItem(null);
                break;

            case TARGETPATH:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new TargetPathActionItem(null, (expression(inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;

            case UNLOADMOVIE:
            case UNLOADMOVIENUM:
                SymbolType unloadType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem unTargetOrNum = expression(inFunction, inMethod, true, variables);
                expectedType(SymbolType.PARENT_CLOSE);
                if (unloadType == SymbolType.UNLOADMOVIE) {
                    ret = new UnLoadMovieActionItem(null, unTargetOrNum);
                }
                if (unloadType == SymbolType.UNLOADMOVIENUM) {
                    ret = new UnLoadMovieNumActionItem(null, unTargetOrNum);
                }
                break;
            case PRINT:
            case PRINTASBITMAP:
            case PRINTASBITMAPNUM:
            case PRINTNUM:
                SymbolType printType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem printTarget = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem printBBox = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);

                switch (printType) {
                    case PRINT:
                        ret = new PrintActionItem(null, printTarget, printBBox);
                        break;
                    case PRINTNUM:
                        ret = new PrintNumActionItem(null, printTarget, printBBox);
                        break;
                    case PRINTASBITMAP:
                        ret = new PrintAsBitmapActionItem(null, printTarget, printBBox);
                        break;
                    case PRINTASBITMAPNUM:
                        ret = new PrintAsBitmapNumActionItem(null, printTarget, printBBox);
                        break;
                }
                break;
            case LOADVARIABLES:
            case LOADMOVIE:
            case LOADVARIABLESNUM:
            case LOADMOVIENUM:
                SymbolType loadType = s.type;
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem url2 = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.COMMA);
                GraphTargetItem targetOrNum = (expression(inFunction, inMethod, true, variables));

                s = lex();
                expected(s, lexer.yyline(), SymbolType.PARENT_CLOSE, SymbolType.COMMA);
                int lvmethod = 1;
                if (s.type == SymbolType.COMMA) {
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.STRING);
                    if (s.value.equals("POST")) {
                        lvmethod = 2;
                    } else if (s.value.equals("GET")) {
                        lvmethod = 1;
                    } else {
                        throw new ActionParseException("Invalid method, \"GET\" or \"POST\" expected.", lexer.yyline());
                    }
                } else {
                    lexer.pushback(s);
                }
                expectedType(SymbolType.PARENT_CLOSE);
                switch (loadType) {
                    case LOADVARIABLES:
                        ret = new LoadVariablesActionItem(null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADMOVIE:
                        ret = new LoadMovieActionItem(null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADVARIABLESNUM:
                        ret = new LoadVariablesNumActionItem(null, url2, targetOrNum, lvmethod);
                        break;
                    case LOADMOVIENUM:
                        ret = new LoadMovieNumActionItem(null, url2, targetOrNum, lvmethod);
                        break;
                }
                break;
            case GOTOANDPLAY:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem gtpFrame = expression(inFunction, inMethod, true, variables);
                s = lex();
                if (s.type == SymbolType.COMMA) { //Handle scene?
                    s = lex();
                    gtpFrame = expression(inFunction, inMethod, true, variables);
                } else {
                    lexer.pushback(s);
                }
                ret = new GotoFrame2ActionItem(null, gtpFrame, false, true, 0);
                expectedType(SymbolType.PARENT_CLOSE);
                break;

            case REMOVEMOVIECLIP:
                expectedType(SymbolType.PARENT_OPEN);
                ret = new RemoveSpriteActionItem(null, (expression(inFunction, inMethod, true, variables)));
                expectedType(SymbolType.PARENT_CLOSE);
                break;
            case STARTDRAG:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem dragTarget = (expression(inFunction, inMethod, true, variables));
                GraphTargetItem lockCenter = null;
                GraphTargetItem constrain = null;
                GraphTargetItem x1 = null;
                GraphTargetItem y1 = null;
                GraphTargetItem x2 = null;
                GraphTargetItem y2 = null;
                s = lex();
                if (s.type == SymbolType.COMMA) {
                    lockCenter = (expression(inFunction, inMethod, true, variables));
                    s = lex();
                    if (s.type == SymbolType.COMMA) {
                        constrain = new DirectValueActionItem(null, 0, 1L, new ArrayList<>());
                        x1 = (expression(inFunction, inMethod, true, variables));
                        s = lex();
                        if (s.type == SymbolType.COMMA) {
                            y1 = (expression(inFunction, inMethod, true, variables));
                            s = lex();
                            if (s.type == SymbolType.COMMA) {
                                x2 = (expression(inFunction, inMethod, true, variables));
                                s = lex();
                                if (s.type == SymbolType.COMMA) {
                                    y2 = (expression(inFunction, inMethod, true, variables));
                                } else {
                                    lexer.pushback(s);
                                    y2 = new DirectValueActionItem(null, 0, 0L, new ArrayList<>());
                                }
                            } else {
                                lexer.pushback(s);
                                x2 = new DirectValueActionItem(null, 0, 0L, new ArrayList<>());
                                y2 = new DirectValueActionItem(null, 0, 0L, new ArrayList<>());
                            }
                        } else {
                            lexer.pushback(s);
                            x2 = new DirectValueActionItem(null, 0, 0L, new ArrayList<>());
                            y2 = new DirectValueActionItem(null, 0, 0L, new ArrayList<>());
                            y1 = new DirectValueActionItem(null, 0, 0L, new ArrayList<>());

                        }
                    } else {
                        lexer.pushback(s);
                        constrain = new DirectValueActionItem(null, 0, 0L, new ArrayList<>());
                        //ret.add(new ActionPush(Boolean.FALSE));
                    }
                } else {
                    lockCenter = new DirectValueActionItem(null, 0, 0L, new ArrayList<>());
                    constrain = new DirectValueActionItem(null, 0, 0L, new ArrayList<>());
                    lexer.pushback(s);
                }
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new StartDragActionItem(null, dragTarget, lockCenter, constrain, x1, y1, x2, y2);
                break;

            case IFFRAMELOADED:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem iflExpr = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                List<GraphTargetItem> iflComs = commands(inFunction, inMethod, forinlevel, variables);
                expectedType(SymbolType.CURLY_CLOSE);
                ret = new IfFrameLoadedActionItem(iflExpr, iflComs, null);
                break;
            case CLASS:
                GraphTargetItem classTypeStr = type(variables);
                s = lex();
                GraphTargetItem extendsTypeStr = null;
                if (s.type == SymbolType.EXTENDS) {
                    extendsTypeStr = type(variables);
                    s = lex();
                }
                List<GraphTargetItem> implementsTypeStrs = new ArrayList<>();
                if (s.type == SymbolType.IMPLEMENTS) {
                    do {
                        GraphTargetItem implementsTypeStr = type(variables);
                        implementsTypeStrs.add(implementsTypeStr);
                        s = lex();
                    } while (s.type == SymbolType.COMMA);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                ret = (traits(false, classTypeStr, extendsTypeStr, implementsTypeStrs, variables));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case INTERFACE:
                GraphTargetItem interfaceTypeStr = type(variables);
                s = lex();
                List<GraphTargetItem> intExtendsTypeStrs = new ArrayList<>();

                if (s.type == SymbolType.EXTENDS) {
                    do {
                        GraphTargetItem intExtendsTypeStr = type(variables);
                        intExtendsTypeStrs.add(intExtendsTypeStr);
                        s = lex();
                    } while (s.type == SymbolType.COMMA);
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_OPEN);
                ret = (traits(true, interfaceTypeStr, null, intExtendsTypeStrs, variables));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case FUNCTION:
                s = lexer.lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolGroup.GLOBALFUNC);
                ret = (function(true, s.value.toString(), false, variables));
                break;
            case VAR:
                s = lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                String varIdentifier = s.value.toString();
                s = lex();
                if (s.type == SymbolType.COLON) {
                    type(variables);
                    s = lex();
                    //TODO: handle value type
                }

                if (s.type == SymbolType.ASSIGN) {
                    GraphTargetItem varval = (expression(inFunction, inMethod, true, variables));
                    ret = new VariableActionItem(varIdentifier, varval, true);
                    variables.add((VariableActionItem) ret);
                } else {
                    ret = new VariableActionItem(varIdentifier, null, true);
                    variables.add((VariableActionItem) ret);
                    lexer.pushback(s);
                }
                break;
            case CURLY_OPEN:
                ret = new BlockItem(null, commands(inFunction, inMethod, forinlevel, variables));
                expectedType(SymbolType.CURLY_CLOSE);
                break;
            case INCREMENT: //preincrement
            case DECREMENT: //predecrement
                GraphTargetItem varincdec = expression(inFunction, inMethod, false, variables);
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementActionItem(null, varincdec);
                } else if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementActionItem(null, varincdec);
                }
                break;
            case SUPER: //constructor call
                ParsedSymbol ss2 = lex();
                if (ss2.type == SymbolType.PARENT_OPEN) {
                    List<GraphTargetItem> args = call(inFunction, inMethod, variables);
                    VariableActionItem supItem = new VariableActionItem(s.value.toString(), null, false);
                    variables.add(supItem);
                    ret = new CallMethodActionItem(null, supItem, new DirectValueActionItem(null, 0, new Undefined(), constantPool), args);
                } else {//no costructor call, but it could be calling parent methods... => handle in expression
                    lexer.pushback(ss2);
                    lexer.pushback(s);
                }
                break;
            case IF:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem ifExpr = (expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                GraphTargetItem onTrue = command(inFunction, inMethod, forinlevel, true, variables);
                List<GraphTargetItem> onTrueList = new ArrayList<>();
                onTrueList.add(onTrue);
                s = lex();
                List<GraphTargetItem> onFalseList = null;
                if (s.type == SymbolType.ELSE) {
                    onFalseList = new ArrayList<>();
                    onFalseList.add(command(inFunction, inMethod, forinlevel, true, variables));
                } else {
                    lexer.pushback(s);
                }
                ret = new IfItem(null, ifExpr, onTrueList, onFalseList);
                break;
            case WHILE:
                expectedType(SymbolType.PARENT_OPEN);
                List<GraphTargetItem> whileExpr = new ArrayList<>();
                whileExpr.add(commaExpression(inFunction, inMethod, forinlevel, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                List<GraphTargetItem> whileBody = new ArrayList<>();
                whileBody.add(command(inFunction, inMethod, forinlevel, true, variables));
                ret = new WhileItem(null, null, whileExpr, whileBody);
                break;
            case DO:
                List<GraphTargetItem> doBody = new ArrayList<>();
                doBody.add(command(inFunction, inMethod, forinlevel, true, variables));
                expectedType(SymbolType.WHILE);
                expectedType(SymbolType.PARENT_OPEN);
                List<GraphTargetItem> doExpr = new ArrayList<>();
                doExpr.add(commaExpression(inFunction, inMethod, forinlevel, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                ret = new DoWhileItem(null, null, doBody, doExpr);
                break;
            case FOR:
                expectedType(SymbolType.PARENT_OPEN);
                s = lex();
                boolean forin = false;
                GraphTargetItem collection = null;
                String objIdent = null;
                VariableActionItem item = null;
                int innerExprReg = 0;
                boolean define = false;
                if (s.type == SymbolType.VAR || s.type == SymbolType.IDENTIFIER) {
                    ParsedSymbol s2 = null;
                    ParsedSymbol ssel = s;
                    if (s.type == SymbolType.VAR) {
                        s2 = lex();
                        ssel = s2;
                        define = true;
                    }

                    if (ssel.type == SymbolType.IDENTIFIER) {
                        objIdent = ssel.value.toString();

                        ParsedSymbol s3 = lex();
                        if (s3.type == SymbolType.IN) {
                            if (inFunction) {
                                /*for (int i = 0; i < 256; i++) {
                                 if (!registerVars.containsValue(i)) {
                                 registerVars.put(objIdent, i);
                                 innerExprReg = i;
                                 break;
                                 }
                                 }*/
                            }

                            item = new VariableActionItem(objIdent, null, define);

                            item.setStoreValue(new GraphTargetItem() {

                                @Override
                                public GraphTextWriter appendTo(GraphTextWriter writer, LocalData localData) throws InterruptedException {
                                    return writer;
                                }

                                @Override
                                public boolean hasReturnValue() {
                                    return false;
                                }

                                @Override
                                public GraphTargetItem returnType() {
                                    return TypeItem.UNBOUNDED;
                                }

                                //toSource is Empty
                            });

                            variables.add(item);

                            collection = expression(inFunction, inMethod, true, variables);
                            forin = true;
                        } else {
                            lexer.pushback(s3);
                            if (s2 != null) {
                                lexer.pushback(s2);
                            }
                            lexer.pushback(s);
                        }
                    } else {
                        if (s2 != null) {
                            lexer.pushback(s2);
                        }
                        lexer.pushback(s);
                    }
                } else {
                    lexer.pushback(s);
                }
                List<GraphTargetItem> forFinalCommands = new ArrayList<>();
                GraphTargetItem forExpr = null;
                List<GraphTargetItem> forFirstCommands = new ArrayList<>();
                if (!forin) {
                    GraphTargetItem fc = command(inFunction, inMethod, forinlevel, true, variables);
                    if (fc != null) { //can be empty command
                        forFirstCommands.add(fc);
                    }
                    forExpr = (expression(inFunction, inMethod, true, variables));
                    expectedType(SymbolType.SEMICOLON);
                    GraphTargetItem fcom = command(inFunction, inMethod, forinlevel, true, variables);
                    if (fcom != null) {
                        forFinalCommands.add(fcom);
                    }
                }
                expectedType(SymbolType.PARENT_CLOSE);
                List<GraphTargetItem> forBody = new ArrayList<>();
                forBody.add(command(inFunction, inMethod, forin ? forinlevel + 1 : forinlevel, true, variables));
                if (forin) {
                    ret = new ForInActionItem(null, null, item, collection, forBody);
                } else {
                    ret = new ForItem(null, null, forFirstCommands, forExpr, forFinalCommands, forBody);
                }
                break;
            case SWITCH:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem switchExpr = expression(inFunction, inMethod, true, variables);
                expectedType(SymbolType.PARENT_CLOSE);
                expectedType(SymbolType.CURLY_OPEN);
                s = lex();
                //ret.addAll(switchExpr);
                /*int exprReg = 0;
                 for (int i = 0; i < 256; i++) {
                 if (!registerVars.containsValue(i)) {
                 registerVars.put("__switch" + uniqId(), i);
                 exprReg = i;
                 break;
                 }
                 }*/
                List<List<ActionIf>> caseIfs = new ArrayList<>();
                List<List<GraphTargetItem>> caseCmds = new ArrayList<>();
                List<GraphTargetItem> caseExprsAll = new ArrayList<>();
                List<Integer> valueMapping = new ArrayList<>();
                int pos = 0;
                while (s.type == SymbolType.CASE) {
                    List<GraphTargetItem> caseExprs = new ArrayList<>();
                    while (s.type == SymbolType.CASE) {
                        GraphTargetItem curCaseExpr = expression(inFunction, inMethod, true, variables);
                        caseExprs.add(curCaseExpr);
                        expectedType(SymbolType.COLON);
                        s = lex();
                        caseExprsAll.add(curCaseExpr);
                        valueMapping.add(pos);
                    }
                    pos++;
                    lexer.pushback(s);
                    List<GraphTargetItem> caseCmd = commands(inFunction, inMethod, forinlevel, variables);
                    caseCmds.add(caseCmd);
                    s = lex();
                }
                List<GraphTargetItem> defCmd = new ArrayList<>();
                if (s.type == SymbolType.DEFAULT) {
                    expectedType(SymbolType.COLON);
                    defCmd = commands(inFunction, inMethod, forinlevel, variables);
                    s = lexer.lex();
                }
                expected(s, lexer.yyline(), SymbolType.CURLY_CLOSE);
                ret = new SwitchItem(null, null, switchExpr, caseExprsAll, caseCmds, defCmd, valueMapping);
                break;
            case BREAK:
                ret = new BreakItem(null, 0); //? There is no more than 1 level continue/break in AS1/2
                break;
            case CONTINUE:
                ret = new ContinueItem(null, 0); //? There is no more than 1 level continue/break in AS1/2
                break;
            case RETURN:
                GraphTargetItem retexpr = expression(inFunction, inMethod, true, variables);
                if (retexpr == null) {
                    retexpr = new DirectValueActionItem(null, 0, new Undefined(), new ArrayList<>());
                }
                ret = new ReturnActionItem(null, retexpr);
                break;
            case TRY:
                List<GraphTargetItem> tryCommands = new ArrayList<>();
                tryCommands.add(command(inFunction, inMethod, forinlevel, true, variables));
                s = lex();
                boolean found = false;
                List<List<GraphTargetItem>> catchCommands = null;
                List<GraphTargetItem> catchExceptions = new ArrayList<>();
                if (s.type == SymbolType.CATCH) {
                    expectedType(SymbolType.PARENT_OPEN);
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.STRING);
                    catchExceptions.add(pushConst((String) s.value));
                    expectedType(SymbolType.PARENT_CLOSE);
                    catchCommands = new ArrayList<>();
                    List<GraphTargetItem> cc = new ArrayList<>();
                    cc.add(command(inFunction, inMethod, forinlevel, true, variables));
                    catchCommands.add(cc);
                    s = lex();
                    found = true;
                }
                List<GraphTargetItem> finallyCommands = null;
                if (s.type == SymbolType.FINALLY) {
                    finallyCommands = new ArrayList<>();
                    finallyCommands.add(command(inFunction, inMethod, forinlevel, true, variables));
                    found = true;
                    s = lex();
                }
                if (!found) {
                    expected(s, lexer.yyline(), SymbolType.CATCH, SymbolType.FINALLY);
                }
                lexer.pushback(s);
                ret = new TryActionItem(tryCommands, catchExceptions, catchCommands, finallyCommands);
                break;
            case THROW:
                ret = new ThrowActionItem(null, expression(inFunction, inMethod, true, variables));
                break;
            case SEMICOLON: //empty command
                if (debugMode) {
                    System.out.println("/command");
                }
                return null;
            default:
                GraphTargetItem valcmd = expressionCommands(s, inFunction, inMethod, forinlevel, variables);
                if (valcmd != null) {
                    ret = valcmd;
                    break;
                }
                lexer.pushback(s);
                ret = expression(inFunction, inMethod, true, variables);
        }
        if (debugMode) {
            System.out.println("/command");
        }
        lexer.removeListener(buf);
        if (ret == null) {  //can be popped expression
            buf.pushAllBack(lexer);
            ret = expression(inFunction, inMethod, true, variables);
        }
        s = lex();
        if ((s != null) && (s.type != SymbolType.SEMICOLON)) {
            lexer.pushback(s);
        }

        return ret;

    }

    private GraphTargetItem expression(boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableActionItem> variables) throws IOException, ActionParseException {
        if (debugMode) {
            System.out.println("expression:");
        }
        GraphTargetItem prim = expressionPrimary(false, inFunction, inMethod, allowRemainder, variables);
        if (prim == null) {
            return null;
        }
        GraphTargetItem ret = expression1(prim, GraphTargetItem.NOPRECEDENCE, inFunction, inMethod, allowRemainder, variables);
        if (debugMode) {
            System.out.println("/expression");
        }
        return ret;
    }

    private ParsedSymbol peekLex() throws IOException, ActionParseException {
        ParsedSymbol lookahead = lex();
        lexer.pushback(lookahead);
        return lookahead;
    }

    private static final String[] operatorIdentifiers = new String[]{"add", "eq", "ne", "lt", "ge", "gt", "le"};

    private boolean isBinaryOperator(ParsedSymbol s) {
        if (s.type == SymbolType.IDENTIFIER && Arrays.asList(operatorIdentifiers).contains(s.value.toString())) {
            return true;
        }
        return s.type.isBinary();
    }

    private int getSymbPrecedence(ParsedSymbol s) {
        if (s.type == SymbolType.IDENTIFIER && Arrays.asList(operatorIdentifiers).contains(s.value.toString())) {
            switch (s.value.toString()) {
                case "add":
                    return GraphTargetItem.PRECEDENCE_ADDITIVE;
                case "eq":
                case "ne":
                    return GraphTargetItem.PRECEDENCE_EQUALITY;
                case "lt":
                case "ge":
                case "gt":
                case "le":
                    return GraphTargetItem.PRECEDENCE_RELATIONAL;
            }
        }
        return s.type.getPrecedence();
    }

    private GraphTargetItem expression1(GraphTargetItem lhs, int min_precedence, boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableActionItem> variables) throws IOException, ActionParseException {
        ParsedSymbol op;
        GraphTargetItem rhs;
        GraphTargetItem mhs = null;
        ParsedSymbol lookahead = peekLex();
        if (debugMode) {
            System.out.println("expression1:");
        }
        //Note: algorithm from http://en.wikipedia.org/wiki/Operator-precedence_parser
        //with relation operators reversed as we have precedence in reverse order
        while (isBinaryOperator(lookahead) && getSymbPrecedence(lookahead) <= /* >= on wiki */ min_precedence) {
            op = lookahead;
            lex();

            //Note: Handle ternar operator as Binary
            //http://stackoverflow.com/questions/13681293/how-can-i-incorporate-ternary-operators-into-a-precedence-climbing-algorithm
            if (op.type == SymbolType.TERNAR) {
                if (debugMode) {
                    System.out.println("ternar-middle:");
                }
                mhs = expression(inFunction, inMethod, allowRemainder, variables);
                expectedType(SymbolType.COLON);
                if (debugMode) {
                    System.out.println("/ternar-middle");
                }
            }

            rhs = expressionPrimary(allowRemainder, inFunction, inMethod, allowRemainder, variables);
            if (rhs == null) {
                lexer.pushback(op);
                break;
            }

            lookahead = peekLex();
            while ((isBinaryOperator(lookahead) && getSymbPrecedence(lookahead) < /* > on wiki */ getSymbPrecedence(op))
                    || (lookahead.type.isRightAssociative() && getSymbPrecedence(lookahead) == getSymbPrecedence(op))) {
                rhs = expression1(rhs, getSymbPrecedence(lookahead), inFunction, inMethod, allowRemainder, variables);
                lookahead = peekLex();
            }

            switch (op.type) {

                case TERNAR:
                    lhs = new TernarOpItem(null, lhs, mhs, rhs);
                    break;
                case SHIFT_LEFT:
                    lhs = new LShiftActionItem(null, lhs, rhs);
                    break;
                case SHIFT_RIGHT:
                    lhs = new RShiftActionItem(null, lhs, rhs);
                    break;
                case USHIFT_RIGHT:
                    lhs = new URShiftActionItem(null, lhs, rhs);
                    break;
                case BITAND:
                    lhs = new BitAndActionItem(null, lhs, rhs);
                    break;
                case BITOR:
                    lhs = new BitOrActionItem(null, lhs, rhs);
                    break;
                case DIVIDE:
                    lhs = new DivideActionItem(null, lhs, rhs);
                    break;
                case MODULO:
                    lhs = new ModuloActionItem(null, lhs, rhs);
                    break;
                case EQUALS:
                    lhs = new EqActionItem(null, lhs, rhs, true/*FIXME SWF version?*/);
                    break;
                case STRICT_EQUALS:
                    lhs = new StrictEqActionItem(null, lhs, rhs);
                    break;
                case NOT_EQUAL:
                    lhs = new NeqActionItem(null, lhs, rhs, true/*FIXME SWF version?*/);
                    break;
                case STRICT_NOT_EQUAL:
                    lhs = new StrictNeqActionItem(null, lhs, rhs);
                    break;
                case LOWER_THAN:
                    lhs = new LtActionItem(null, lhs, rhs, true/*FIXME SWF version?*/);
                    break;
                case LOWER_EQUAL:
                    lhs = new LeActionItem(null, lhs, rhs);
                    break;
                case GREATER_THAN:
                    lhs = new GtActionItem(null, lhs, rhs);
                    break;
                case GREATER_EQUAL:
                    lhs = new GeActionItem(null, lhs, rhs, true/*FIXME SWF version?*/);
                    break;
                case AND:
                    lhs = new AndItem(null, lhs, rhs);
                    break;
                case OR:
                    lhs = new OrItem(null, lhs, rhs);
                    break;
                case FULLAND:
                    lhs = new AndActionItem(null, lhs, rhs);
                    break;
                case FULLOR:
                    lhs = new OrActionItem(null, lhs, rhs);
                    break;
                case MINUS:
                    lhs = new SubtractActionItem(null, lhs, rhs);
                    break;
                case MULTIPLY:
                    lhs = new MultiplyActionItem(null, lhs, rhs);
                    break;
                case PLUS:
                    lhs = new AddActionItem(null, lhs, rhs, true);
                    break;
                case XOR:
                    lhs = new BitXorActionItem(null, lhs, rhs);
                    break;
                case AS:

                    break;
                case INSTANCEOF:
                    lhs = new InstanceOfActionItem(null, lhs, rhs);
                    break;
                case IS:

                    break;

                case ASSIGN:
                case ASSIGN_BITAND:
                case ASSIGN_BITOR:
                case ASSIGN_DIVIDE:
                case ASSIGN_MINUS:
                case ASSIGN_MODULO:
                case ASSIGN_MULTIPLY:
                case ASSIGN_PLUS:
                case ASSIGN_SHIFT_LEFT:
                case ASSIGN_SHIFT_RIGHT:
                case ASSIGN_USHIFT_RIGHT:
                case ASSIGN_XOR:
                    GraphTargetItem assigned = rhs;
                    switch (lookahead.type) {
                        case ASSIGN:
                            //assigned = assigned;
                            break;
                        case ASSIGN_BITAND:
                            assigned = new BitAndActionItem(null, lhs, assigned);
                            break;
                        case ASSIGN_BITOR:
                            assigned = new BitOrActionItem(null, lhs, assigned);
                            break;
                        case ASSIGN_DIVIDE:
                            assigned = new DivideActionItem(null, lhs, assigned);
                            break;
                        case ASSIGN_MINUS:
                            assigned = new SubtractActionItem(null, lhs, assigned);
                            break;
                        case ASSIGN_MODULO:
                            assigned = new ModuloActionItem(null, lhs, assigned);
                            break;
                        case ASSIGN_MULTIPLY:
                            assigned = new MultiplyActionItem(null, lhs, assigned);
                            break;
                        case ASSIGN_PLUS:
                            assigned = new AddActionItem(null, lhs, assigned, true/*TODO:SWF version?*/);
                            break;
                        case ASSIGN_SHIFT_LEFT:
                            assigned = new LShiftActionItem(null, lhs, assigned);
                            break;
                        case ASSIGN_SHIFT_RIGHT:
                            assigned = new RShiftActionItem(null, lhs, assigned);
                            break;
                        case ASSIGN_USHIFT_RIGHT:
                            assigned = new URShiftActionItem(null, lhs, assigned);
                            break;
                        case ASSIGN_XOR:
                            assigned = new BitXorActionItem(null, lhs, assigned);
                            break;
                    }
                    if (lhs instanceof VariableActionItem) {
                        ((VariableActionItem) lhs).setStoreValue(assigned);
                        ((VariableActionItem) lhs).setDefinition(false);
                        lhs = lhs;
                    } else if (lhs instanceof GetMemberActionItem) {
                        lhs = new SetMemberActionItem(null, ((GetMemberActionItem) lhs).object, ((GetMemberActionItem) lhs).memberName, assigned);
                    } else {
                        throw new ActionParseException("Invalid assignment", lexer.yyline());
                    }
                    break;
                case IDENTIFIER:
                    switch (lookahead.value.toString()) {
                        case "add":
                            lhs = new StringAddActionItem(null, lhs, rhs);
                            break;
                        case "eq":
                            lhs = new StringEqActionItem(null, lhs, rhs);
                            break;
                        case "ne":
                            lhs = new StringNeActionItem(null, lhs, rhs);
                            break;
                        case "lt":
                            lhs = new StringLtActionItem(null, lhs, rhs);
                            break;
                        case "ge":
                            lhs = new StringGeActionItem(null, lhs, rhs);
                            break;
                        case "gt":
                            lhs = new StringGtActionItem(null, lhs, rhs);
                            break;
                        case "le":
                            lhs = new StringLeActionItem(null, lhs, rhs);
                            break;
                    }
                    break;
            }
        }

        switch (lookahead.type) {
            case INCREMENT: //postincrement
                lex();
                if (!(lhs instanceof VariableActionItem) && !(lhs instanceof GetMemberActionItem)) {
                    throw new ActionParseException("Invalid assignment", lexer.yyline());
                }
                lhs = new PostIncrementActionItem(null, lhs);
                break;
            case DECREMENT: //postdecrement
                lex();
                if (!(lhs instanceof VariableActionItem) && !(lhs instanceof GetMemberActionItem)) {
                    throw new ActionParseException("Invalid assignment", lexer.yyline());
                }
                lhs = new PostDecrementActionItem(null, lhs);
                break;
            default:
                if (lhs instanceof ParenthesisItem) {
                    if (isType(((ParenthesisItem) lhs).value)) {
                        GraphTargetItem expr2 = expression(inFunction, inMethod, true, variables);
                        if (expr2 != null) {
                            lhs = new CastOpActionItem(null, ((ParenthesisItem) lhs).value, expr2);
                        }
                    }
                }
        }
        if (debugMode) {
            System.out.println("/expression1");
        }
        return lhs;
    }

    private boolean isType(GraphTargetItem item) {
        if (item == null) {
            return false;
        }
        while (item instanceof GetMemberActionItem) {
            item = ((GetMemberActionItem) item).object;
        }
        if (item instanceof VariableActionItem) {
            return true;
        }
        return false;
    }

    private int brackets(List<GraphTargetItem> ret, boolean inFunction, boolean inMethod, List<VariableActionItem> variables) throws IOException, ActionParseException {
        ParsedSymbol s = lex();
        int arrCnt = 0;
        if (s.type == SymbolType.BRACKET_OPEN) {
            s = lex();

            while (s.type != SymbolType.BRACKET_CLOSE) {
                if (s.type != SymbolType.COMMA) {
                    lexer.pushback(s);
                }
                arrCnt++;
                ret.add(expression(inFunction, inMethod, true, variables));
                s = lex();
                if (!s.isType(SymbolType.COMMA, SymbolType.BRACKET_CLOSE)) {
                    expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.BRACKET_CLOSE);
                }
            }
        } else {
            lexer.pushback(s);
            return -1;
        }
        return arrCnt;
    }

    private GraphTargetItem commaExpression(boolean inFunction, boolean inMethod, int forInLevel, List<VariableActionItem> variables) throws IOException, ActionParseException {
        GraphTargetItem cmd = null;
        List<GraphTargetItem> expr = new ArrayList<>();
        ParsedSymbol s;
        do {
            cmd = command(inFunction, inMethod, forInLevel, false, variables);
            if (cmd != null) {
                expr.add(cmd);
            }
            s = lex();
        } while (s.type == SymbolType.COMMA && cmd != null);
        lexer.pushback(s);
        if (cmd == null) {
            expr.add(expression(inFunction, inMethod, true, variables));
        } else {
            if (!cmd.hasReturnValue()) {
                throw new ActionParseException("Expression expected", lexer.yyline());
            }
        }
        return new CommaExpressionItem(null, expr);
    }

    private GraphTargetItem expressionPrimary(boolean allowEmpty, boolean inFunction, boolean inMethod, boolean allowRemainder, List<VariableActionItem> variables) throws IOException, ActionParseException {
        if (debugMode) {
            System.out.println("primary:");
        }
        boolean allowMemberOrCall = false;
        GraphTargetItem ret = null;
        ParsedSymbol s = lex();

        switch (s.type) {
            case NEGATE:
                versionRequired(s, 5);
                ret = expressionPrimary(false, inFunction, inMethod, false, variables);
                ret = new BitXorActionItem(null, ret, new DirectValueActionItem(4.294967295E9));

                break;
            case MINUS:
                s = lex();
                if (s.isType(SymbolType.DOUBLE)) {
                    ret = new DirectValueActionItem(null, 0, -(double) (Double) s.value, new ArrayList<>());

                } else if (s.isType(SymbolType.INTEGER)) {
                    ret = new DirectValueActionItem(null, 0, -(long) (Long) s.value, new ArrayList<>());

                } else {
                    lexer.pushback(s);
                    GraphTargetItem num = expressionPrimary(false, inFunction, inMethod, true, variables);
                    if ((num instanceof DirectValueActionItem)
                            && (((DirectValueActionItem) num).value instanceof Long)) {
                        ((DirectValueActionItem) num).value = -(Long) ((DirectValueActionItem) num).value;
                        ret = num;
                    } else if ((num instanceof DirectValueActionItem)
                            && (((DirectValueActionItem) num).value instanceof Double)) {
                        Double d = (Double) ((DirectValueActionItem) num).value;
                        if (d.isInfinite()) {
                            ((DirectValueActionItem) num).value = Double.NEGATIVE_INFINITY;
                        } else {
                            ((DirectValueActionItem) num).value = -d;
                        }
                        ret = (num);
                    } else if ((num instanceof DirectValueActionItem)
                            && (((DirectValueActionItem) num).value instanceof Float)) {
                        ((DirectValueActionItem) num).value = -(Float) ((DirectValueActionItem) num).value;
                        ret = (num);
                    } else {;
                        ret = (new SubtractActionItem(null, new DirectValueActionItem(null, 0, (Long) 0L, new ArrayList<>()), num));
                    }
                }
                break;
            case TYPEOF:
                ret = new TypeOfActionItem(null, expressionPrimary(false, inFunction, inMethod, false, variables));
                allowMemberOrCall = true;
                break;
            case TRUE:
                ret = new DirectValueActionItem(null, 0, Boolean.TRUE, new ArrayList<>());

                break;
            case NULL:
                ret = new DirectValueActionItem(null, 0, new Null(), new ArrayList<>());

                break;
            case UNDEFINED:
                ret = new DirectValueActionItem(null, 0, new Undefined(), new ArrayList<>());
                break;
            case FALSE:
                ret = new DirectValueActionItem(null, 0, Boolean.FALSE, new ArrayList<>());

                break;
            case CURLY_OPEN: //Object literal
                s = lex();
                List<GraphTargetItem> objectNames = new ArrayList<>();
                List<GraphTargetItem> objectValues = new ArrayList<>();
                while (s.type != SymbolType.CURLY_CLOSE) {
                    if (s.type != SymbolType.COMMA) {
                        lexer.pushback(s);
                    }
                    s = lex();
                    expected(s, lexer.yyline(), SymbolType.IDENTIFIER);
                    objectNames.add(0, pushConst((String) s.value));
                    expectedType(SymbolType.COLON);
                    objectValues.add(0, expression(inFunction, inMethod, true, variables));
                    s = lex();
                    if (!s.isType(SymbolType.COMMA, SymbolType.CURLY_CLOSE)) {
                        expected(s, lexer.yyline(), SymbolType.COMMA, SymbolType.CURLY_CLOSE);
                    }
                }
                ret = new InitObjectActionItem(null, objectNames, objectValues);
                allowMemberOrCall = true;
                break;
            case BRACKET_OPEN: //Array literal or just brackets
                lexer.pushback(s);
                List<GraphTargetItem> inBrackets = new ArrayList<>();
                int arrCnt = brackets(inBrackets, inFunction, inMethod, variables);
                ret = new InitArrayActionItem(null, inBrackets);
                allowMemberOrCall = true;
                break;
            case FUNCTION:
                s = lex();
                String fname = "";
                if (s.isType(SymbolType.IDENTIFIER, SymbolGroup.GLOBALFUNC)) {
                    fname = s.value.toString();
                } else {
                    lexer.pushback(s);
                }
                ret = function(true, fname, false, variables);
                allowMemberOrCall = true;
                break;
            case STRING:
                ret = pushConst(s.value.toString());
                allowMemberOrCall = true;
                break;
            case NEWLINE:
                ret = new DirectValueActionItem(null, 0, "\r", new ArrayList<>());
                allowMemberOrCall = true;
                break;
            case NAN:
                ret = new DirectValueActionItem(null, 0, Double.NaN, new ArrayList<>());

                break;
            case INFINITY:
                ret = new DirectValueActionItem(null, 0, Double.POSITIVE_INFINITY, new ArrayList<>());

                break;
            case INTEGER:
            case DOUBLE:
                ret = new DirectValueActionItem(null, 0, s.value, new ArrayList<>());

                break;
            case DELETE:
                GraphTargetItem varDel = expressionPrimary(false, inFunction, inMethod, false, variables);
                if (varDel instanceof GetMemberActionItem) {
                    GetMemberActionItem gm = (GetMemberActionItem) varDel;
                    ret = new DeleteActionItem(null, gm.object, gm.memberName);
                } else {
                    throw new ActionParseException("Not a property", lexer.yyline());
                }
                break;
            case INCREMENT:
            case DECREMENT: //preincrement
                GraphTargetItem prevar = expressionPrimary(false, inFunction, inMethod, false, variables);//variable(inFunction, inMethod, variables);
                if (s.type == SymbolType.INCREMENT) {
                    ret = new PreIncrementActionItem(null, prevar);
                }
                if (s.type == SymbolType.DECREMENT) {
                    ret = new PreDecrementActionItem(null, prevar);
                }

                break;
            case NOT:
                ret = new NotItem(null, expressionPrimary(false, inFunction, inMethod, false, variables));

                break;
            case PARENT_OPEN:
                ret = new ParenthesisItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                //ret = memberOrCall(ret, inFunction, inMethod, variables);
                allowMemberOrCall = true;
                break;
            case NEW:
                GraphTargetItem newvar = expressionPrimary(false, inFunction, inMethod, false, variables);//variable(inFunction, inMethod, variables);
                if (newvar instanceof CallMethodActionItem) {
                    CallMethodActionItem ca = (CallMethodActionItem) newvar;
                    ret = new NewMethodActionItem(null, ca.scriptObject, ca.methodName, ca.arguments);
                } else if (newvar instanceof CallFunctionActionItem) {
                    CallFunctionActionItem cf = (CallFunctionActionItem) newvar;
                    ret = new NewObjectActionItem(null, cf.functionName, cf.arguments);
                } else {
                    throw new ActionParseException("Invalid new item", lexer.yyline());
                }

                break;
            case EVAL:
                expectedType(SymbolType.PARENT_OPEN);
                GraphTargetItem evar = new EvalActionItem(null, expression(inFunction, inMethod, true, variables));
                expectedType(SymbolType.PARENT_CLOSE);
                //evar = memberOrCall(evar, inFunction, inMethod, variables);
                ret = evar;
                allowMemberOrCall = true;

                break;
            case IDENTIFIER:
            case THIS:
            case SUPER:
                if (s.value.equals("not")) {
                    ret = new NotItem(null, expressionPrimary(false, inFunction, inMethod, false, variables));
                } else {
                    ret = new VariableActionItem(s.value.toString(), null, false);
                    variables.add((VariableActionItem) ret);
                    allowMemberOrCall = true;
                }

                break;
            default:
                GraphTargetItem excmd = expressionCommands(s, inFunction, inMethod, -1, variables);
                if (excmd != null) {
                    //?
                    ret = excmd;
                    allowMemberOrCall = true; //?
                    break;
                }
                lexer.pushback(s);
        }

        if (allowMemberOrCall && ret != null) {
            ret = memberOrCall(ret, inFunction, inMethod, variables);
        }
        if (debugMode) {
            System.out.println("/primary");
        }
        return ret;
    }

    private GraphTargetItem memberOrCall(GraphTargetItem ret, boolean inFunction, boolean inMethod, List<VariableActionItem> variables) throws IOException, ActionParseException {
        ParsedSymbol op = lex();
        while (op.isType(SymbolType.PARENT_OPEN, SymbolType.BRACKET_OPEN, SymbolType.DOT)) {
            if (op.type == SymbolType.PARENT_OPEN) {
                List<GraphTargetItem> args = call(inFunction, inMethod, variables);
                if (ret instanceof GetMemberActionItem) {
                    GetMemberActionItem mem = (GetMemberActionItem) ret;
                    ret = new CallMethodActionItem(null, mem.object, mem.memberName, args);
                } else if (ret instanceof VariableActionItem) {
                    VariableActionItem var = (VariableActionItem) ret;
                    ret = new CallFunctionActionItem(null, pushConst(var.getVariableName()), args);
                } else {
                    ret = new CallFunctionActionItem(null, ret, args);
                }
            }
            if (op.type == SymbolType.BRACKET_OPEN) {
                GraphTargetItem rhs = expression(inFunction, inMethod, false, variables);
                ret = new GetMemberActionItem(null, ret, rhs);
                expectedType(SymbolType.BRACKET_CLOSE);
            }
            if (op.type == SymbolType.DOT) {
                ParsedSymbol s = lex();
                expected(s, lexer.yyline(), SymbolType.IDENTIFIER, SymbolType.THIS, SymbolType.SUPER, SymbolType.STRING_OP, SymbolGroup.GLOBALFUNC);

                ret = new GetMemberActionItem(null, ret, pushConst(s.value.toString()));
            }
            op = lex();
        }
        lexer.pushback(op);
        return ret;
    }

    private DirectValueActionItem pushConst(String s) throws IOException, ActionParseException {
        int index = constantPool.indexOf(s);
        if (index == -1) {
            if (ActionConstantPool.calculateSize(constantPool) + ActionConstantPool.calculateSize(s) <= 0xffff) {
                // constant pool is not full
                constantPool.add(s);
                index = constantPool.indexOf(s);
            }
        }

        if (index == -1) {
            return new DirectValueActionItem(null, 0, s, constantPool);
        }

        return new DirectValueActionItem(null, 0, new ConstantIndex(index), constantPool);
    }

    private ActionScriptLexer lexer = null;

    private List<String> constantPool;

    public List<GraphTargetItem> treeFromString(String str, List<String> constantPool) throws ActionParseException, IOException {
        List<GraphTargetItem> retTree = new ArrayList<>();
        this.constantPool = constantPool;
        lexer = new ActionScriptLexer(new StringReader(str));

        List<VariableActionItem> vars = new ArrayList<>();
        retTree.addAll(commands(false, false, 0, vars));
        for (VariableActionItem v : vars) {
            String varName = v.getVariableName();
            GraphTargetItem stored = v.getStoreValue();
            if (v.isDefinition()) {
                v.setBoxedValue(new DefineLocalActionItem(null, pushConst(varName), stored));
            } else {
                if (stored != null) {
                    v.setBoxedValue(new SetVariableActionItem(null, pushConst(varName), stored));
                } else {
                    v.setBoxedValue(new GetVariableActionItem(null, pushConst(varName)));
                }
            }
        }
        if (lexer.lex().type != SymbolType.EOF) {
            throw new ActionParseException("Parsing finished before end of the file", lexer.yyline());
        }
        return retTree;
    }

    public List<Action> actionsFromTree(List<GraphTargetItem> tree, List<String> constantPool) throws CompilationException {
        ActionSourceGenerator gen = new ActionSourceGenerator(swfVersion, constantPool);
        List<Action> ret = new ArrayList<>();
        SourceGeneratorLocalData localData = new SourceGeneratorLocalData(
                new HashMap<>(), 0, Boolean.FALSE, 0);
        List<GraphSourceItem> srcList = gen.generate(localData, tree);
        for (GraphSourceItem s : srcList) {
            if (s instanceof Action) {
                ret.add((Action) s);
            }
        }
        ret.add(0, new ActionConstantPool(constantPool));
        return ret;
    }

    public List<Action> actionsFromString(String s) throws ActionParseException, IOException, CompilationException {
        List<String> constantPool = new ArrayList<>();
        List<GraphTargetItem> tree = treeFromString(s, constantPool);
        return actionsFromTree(tree, constantPool);
    }

    private void versionRequired(ParsedSymbol s, int min) throws ActionParseException {
        versionRequired(s.value.toString(), min, Integer.MAX_VALUE);
    }

    private void versionRequired(ParsedSymbol s, int min, int max) throws ActionParseException {
        versionRequired(s.value.toString(), min, max);
    }

    private void versionRequired(String type, int min, int max) throws ActionParseException {
        if (min == max && swfVersion != min) {
            throw new ActionParseException(type + " requires SWF version " + min, lexer.yyline());
        }
        if (swfVersion < min) {
            throw new ActionParseException(type + " requires at least SWF version " + min, lexer.yyline());
        }
        if (swfVersion > max) {
            throw new ActionParseException(type + " requires SWF version lower than " + max, lexer.yyline());
        }
    }
}
