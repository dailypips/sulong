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
package com.oracle.truffle.llvm.parser.factories;

import com.oracle.truffle.llvm.nodes.base.LLVMExpressionNode;
import com.oracle.truffle.llvm.nodes.impl.base.floating.LLVMDoubleNode;
import com.oracle.truffle.llvm.nodes.impl.base.floating.LLVMFloatNode;
import com.oracle.truffle.llvm.nodes.impl.base.integers.LLVMI32Node;
import com.oracle.truffle.llvm.nodes.impl.base.integers.LLVMI64Node;
import com.oracle.truffle.llvm.nodes.impl.base.integers.LLVMI8Node;
import com.oracle.truffle.llvm.nodes.impl.others.LLVMValueProfilingNodeFactory.LLVMDoubleProfiledValueNodeGen;
import com.oracle.truffle.llvm.nodes.impl.others.LLVMValueProfilingNodeFactory.LLVMFloatProfiledValueNodeGen;
import com.oracle.truffle.llvm.nodes.impl.others.LLVMValueProfilingNodeFactory.LLVMI32ProfiledValueNodeGen;
import com.oracle.truffle.llvm.nodes.impl.others.LLVMValueProfilingNodeFactory.LLVMI64ProfiledValueNodeGen;
import com.oracle.truffle.llvm.nodes.impl.others.LLVMValueProfilingNodeFactory.LLVMI8ProfiledValueNodeGen;
import com.oracle.truffle.llvm.parser.LLVMBaseType;

public class LLVMValueProfileFactory {

    public static LLVMExpressionNode createValueProfiledNode(LLVMExpressionNode argNode, LLVMBaseType paramType) {
        switch (paramType) {
            case I8:
                return LLVMI8ProfiledValueNodeGen.create((LLVMI8Node) argNode);
            case I32:
                return LLVMI32ProfiledValueNodeGen.create((LLVMI32Node) argNode);
            case I64:
                return LLVMI64ProfiledValueNodeGen.create((LLVMI64Node) argNode);
            case FLOAT:
                return LLVMFloatProfiledValueNodeGen.create((LLVMFloatNode) argNode);
            case DOUBLE:
                return LLVMDoubleProfiledValueNodeGen.create((LLVMDoubleNode) argNode);
            default:
                return argNode;
        }
    }

}
