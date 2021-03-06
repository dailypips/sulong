/*
 * Copyright (c) 2016, Oracle and/or its affiliates.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of
 * conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other materials provided
 * with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to
 * endorse or promote products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED
 * AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.oracle.truffle.llvm.nodes.impl.func;

import java.util.List;
import java.util.Map;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.CompilationFinal;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import com.oracle.truffle.api.RootCallTarget;
import com.oracle.truffle.api.Truffle;
import com.oracle.truffle.api.frame.FrameDescriptor;
import com.oracle.truffle.api.frame.FrameSlot;
import com.oracle.truffle.api.frame.VirtualFrame;
import com.oracle.truffle.api.nodes.DirectCallNode;
import com.oracle.truffle.api.nodes.ExplodeLoop;
import com.oracle.truffle.api.nodes.RootNode;
import com.oracle.truffle.llvm.nodes.impl.base.LLVMContext;
import com.oracle.truffle.llvm.nodes.impl.base.LLVMFrameUtil;
import com.oracle.truffle.llvm.nodes.impl.base.LLVMLanguage;
import com.oracle.truffle.llvm.runtime.LLVMExitException;
import com.oracle.truffle.llvm.runtime.LLVMLogger;
import com.oracle.truffle.llvm.runtime.options.LLVMBaseOptionFacade;
import com.oracle.truffle.llvm.types.LLVMAddress;
import com.oracle.truffle.llvm.types.LLVMFunctionDescriptor;

/**
 * The global entry point initializes the global scope and starts execution with the main function.
 */
public class LLVMGlobalRootNode extends RootNode {

    private final DirectCallNode main;
    @CompilationFinal private final Object[] arguments;
    private final LLVMContext context;
    // FIXME instead make the option system "PE safe"
    private final boolean printNativeStats = LLVMBaseOptionFacade.printNativeCallStats();
    private final int executionCount = LLVMBaseOptionFacade.getExecutionCount();
    private final boolean printExecutionTime = LLVMBaseOptionFacade.printExecutionTime();
    private final FrameSlot stackPointerSlot;
    private long startExecutionTime;
    private long endExecutionTime;

    public LLVMGlobalRootNode(FrameSlot stackSlot, FrameDescriptor descriptor, LLVMContext context, CallTarget main, Object... arguments) {
        super(LLVMLanguage.class, null, descriptor);
        this.stackPointerSlot = stackSlot;
        this.context = context;
        this.main = Truffle.getRuntime().createDirectCallNode(main);
        this.arguments = arguments;
    }

    @Override
    @ExplodeLoop
    public Object execute(VirtualFrame frame) {
        LLVMAddress stackPointer = context.getStack().getUpperBounds();
        try {
            Object result = null;
            for (int i = 0; i < executionCount; i++) {
                frame.setObject(stackPointerSlot, stackPointer);
                Object[] realArgs = new Object[arguments.length + LLVMCallNode.ARG_START_INDEX];
                realArgs[0] = LLVMFrameUtil.getAddress(frame, stackPointerSlot);
                System.arraycopy(arguments, 0, realArgs, LLVMCallNode.ARG_START_INDEX, arguments.length);
                if (printExecutionTime) {
                    startExecutionTime = System.currentTimeMillis();
                }
                result = main.call(frame, realArgs);
                if (printExecutionTime) {
                    endExecutionTime = System.currentTimeMillis();
                    printExecutionTime();
                }

                executeDestructorFunctions();
                if (i != executionCount - 1) {
                    executeStaticInits();
                }
            }
            return result;
        } catch (LLVMExitException e) {
            return e.getReturnCode();
        } finally {
            if (printNativeStats) {
                printNativeCallStats(context);
            }
        }
    }

    @TruffleBoundary
    private void printExecutionTime() {
        long executionTime = endExecutionTime - startExecutionTime;
        LLVMLogger.unconditionalInfo("execution time: " + executionTime + " ms");
    }

    @TruffleBoundary
    private void executeStaticInits() {
        List<RootCallTarget> staticInits = context.getStaticInitializers();
        for (RootCallTarget callTarget : staticInits) {
            callTarget.call(staticInits);
        }
    }

    @TruffleBoundary
    private void executeDestructorFunctions() {
        List<RootCallTarget> destructorFunctions = context.getDestructorFunctions();
        for (RootCallTarget callTarget : destructorFunctions) {
            callTarget.call(destructorFunctions);
        }
    }

    @TruffleBoundary
    private static void printNativeCallStats(LLVMContext context) {
        Map<LLVMFunctionDescriptor, Integer> nativeFunctionCallSites = context.getNativeFunctionLookupStats();
        // Checkstyle: stop
        if (!nativeFunctionCallSites.isEmpty()) {
            System.out.println("==========================");
            System.out.println("native function sites:");
            System.out.println("==========================");
            for (LLVMFunctionDescriptor function : nativeFunctionCallSites.keySet()) {
                String output = String.format("%15s: %3d", function.getName(), nativeFunctionCallSites.get(function));
                System.out.println(output);
            }
            System.out.println("==========================");
        }
        // Checkstyle: resume
    }

}
