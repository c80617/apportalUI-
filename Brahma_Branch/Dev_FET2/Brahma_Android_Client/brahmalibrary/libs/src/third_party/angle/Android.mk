LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := angle-common
LOCAL_SRC_FILES := \
    src/common/Float16ToFloat32.cpp \
    src/common/MemoryBuffer.cpp \
    src/common/angleutils.cpp \
    src/common/debug.cpp \
    src/common/mathutil.cpp \
    src/common/matrix_utils.h \
    src/common/string_utils.cpp \
    src/common/tls.cpp \
    src/common/utilities.cpp
  
LOCAL_C_INCLUDES := $(LOCAL_PATH)/include $(LOCAL_PATH)/src $(LOCAL_PATH)/src/common/third_party/base 

LOCAL_CFLAGS := \
    -DDCHECK_ALWAYS_ON=1 -D_GNU_SOURCE \
    -DCOMPONENT_BUILD \
    -D_DEBUG -DDYNAMIC_ANNOTATIONS_ENABLED=1 \
    -DANGLE_ENABLE_DEBUG_ANNOTATIONS -DANGLE_ENABLE_RELEASE_ASSERTS

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_CFLAGS += -DANGLE_IS_32_BIT_CPU
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_CFLAGS += -DANGLE_IS_64_BIT_CPU
endif

LOCAL_STATIC_LIBRARIES  :=
LOCAL_SHARED_LIBRARIES  :=
LOCAL_LDFLAGS :=
LOCAL_LDLIBS :=

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE := angle-preprocessor
LOCAL_SRC_FILES := \
    src/compiler/preprocessor/DiagnosticsBase.cpp \
    src/compiler/preprocessor/DirectiveHandlerBase.cpp \
    src/compiler/preprocessor/DirectiveParser.cpp \
    src/compiler/preprocessor/ExpressionParser.cpp \
    src/compiler/preprocessor/Input.cpp \
    src/compiler/preprocessor/Lexer.cpp \
    src/compiler/preprocessor/Macro.cpp \
    src/compiler/preprocessor/MacroExpander.cpp \
    src/compiler/preprocessor/Preprocessor.cpp \
    src/compiler/preprocessor/Token.cpp \
    src/compiler/preprocessor/Tokenizer.cpp

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include $(LOCAL_PATH)/src $(LOCAL_PATH)/src/common/third_party/base 

LOCAL_CFLAGS := \
    -DDCHECK_ALWAYS_ON=1 -D_GNU_SOURCE \
    -DCOMPONENT_BUILD \
    -D_DEBUG -DDYNAMIC_ANNOTATIONS_ENABLED=1 \
    -DANGLE_ENABLE_DEBUG_ANNOTATIONS -DANGLE_ENABLE_RELEASE_ASSERTS

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_CFLAGS += -DANGLE_IS_32_BIT_CPU
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_CFLAGS += -DANGLE_IS_64_BIT_CPU
endif

LOCAL_STATIC_LIBRARIES :=
LOCAL_SHARED_LIBRARIES :=
LOCAL_LDFLAGS :=
LOCAL_LDLIBS :=

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(BUILD_STATIC_LIBRARY)


include $(CLEAR_VARS)

LOCAL_MODULE := angle-translator
LOCAL_SRC_FILES := \
    src/compiler/translator/ShaderLang.cpp \
    src/compiler/translator/ShaderVars.cpp \
    \
    src/compiler/translator/BuiltInFunctionEmulator.cpp \
    src/compiler/translator/Cache.cpp \
    src/compiler/translator/CallDAG.cpp \
    src/compiler/translator/CodeGen.cpp \
    src/compiler/translator/Compiler.cpp \
    src/compiler/translator/DeferGlobalInitializers.cpp \
    src/compiler/translator/Diagnostics.cpp \
    src/compiler/translator/DirectiveHandler.cpp \
    src/compiler/translator/EmulatePrecision.cpp \
    src/compiler/translator/FlagStd140Structs.cpp \
    src/compiler/translator/ForLoopUnroll.cpp \
    src/compiler/translator/InfoSink.cpp \
    src/compiler/translator/Initialize.cpp \
    src/compiler/translator/InitializeDll.cpp \
    src/compiler/translator/InitializeParseContext.cpp \
    src/compiler/translator/InitializeVariables.cpp \
    src/compiler/translator/IntermNode.cpp \
    src/compiler/translator/IntermTraverse.cpp \
    src/compiler/translator/Intermediate.cpp \
    src/compiler/translator/LoopInfo.cpp \
    src/compiler/translator/Operator.cpp \
    src/compiler/translator/ParseContext.cpp \
    src/compiler/translator/PoolAlloc.cpp \
    src/compiler/translator/PruneEmptyDeclarations.cpp \
    src/compiler/translator/RecordConstantPrecision.cpp \
    src/compiler/translator/RegenerateStructNames.cpp \
    src/compiler/translator/RemovePow.cpp \
    src/compiler/translator/RewriteDoWhile.cpp \
    src/compiler/translator/ScalarizeVecAndMatConstructorArgs.cpp \
    src/compiler/translator/SearchSymbol.cpp \
    src/compiler/translator/SymbolTable.cpp \
    src/compiler/translator/Types.cpp \
    src/compiler/translator/UnfoldShortCircuitAST.cpp \
    src/compiler/translator/ValidateGlobalInitializer.cpp \
    src/compiler/translator/ValidateLimitations.cpp \
    src/compiler/translator/ValidateMaxParameters.cpp \
    src/compiler/translator/ValidateOutputs.cpp \
    src/compiler/translator/ValidateSwitch.cpp \
    src/compiler/translator/VariableInfo.cpp \
    src/compiler/translator/VariablePacker.cpp \
    src/compiler/translator/blocklayout.cpp \
    src/compiler/translator/depgraph/DependencyGraph.cpp \
    src/compiler/translator/depgraph/DependencyGraphBuilder.cpp \
    src/compiler/translator/depgraph/DependencyGraphOutput.cpp \
    src/compiler/translator/depgraph/DependencyGraphTraverse.cpp \
    src/compiler/translator/glslang_lex.cpp \
    src/compiler/translator/glslang_tab.cpp \
    src/compiler/translator/intermOut.cpp \
    src/compiler/translator/timing/RestrictFragmentShaderTiming.cpp \
    src/compiler/translator/timing/RestrictVertexShaderTiming.cpp \
    src/compiler/translator/util.cpp \
    src/third_party/compiler/ArrayBoundsClamper.cpp \
    \
    src/compiler/translator/OutputESSL.cpp \
    src/compiler/translator/TranslatorESSL.cpp \
    \
    src/compiler/translator/BuiltInFunctionEmulatorGLSL.cpp \
    src/compiler/translator/ExtensionGLSL.cpp \
    src/compiler/translator/OutputGLSL.cpp \
    src/compiler/translator/OutputGLSLBase.cpp \
    src/compiler/translator/TranslatorGLSL.cpp \
    src/compiler/translator/VersionGLSL.cpp \

LOCAL_C_INCLUDES := $(LOCAL_PATH)/include $(LOCAL_PATH)/src $(LOCAL_PATH)/src/common/third_party/base 

LOCAL_CFLAGS := \
    -DANGLE_ENABLE_ESSL -DANGLE_ENABLE_GLSL \
    -DDCHECK_ALWAYS_ON=1 -D_GNU_SOURCE \
    -DCOMPONENT_BUILD \
    -D_DEBUG -DDYNAMIC_ANNOTATIONS_ENABLED=1 \
    -DANGLE_ENABLE_RELEASE_ASSERTS

ifeq ($(TARGET_ARCH_ABI),armeabi-v7a)
    LOCAL_CFLAGS += -DANGLE_IS_32_BIT_CPU
endif

ifeq ($(TARGET_ARCH_ABI),arm64-v8a)
    LOCAL_CFLAGS += -DANGLE_IS_64_BIT_CPU
endif

LOCAL_SHARED_LIBRARIES :=

LOCAL_EXPORT_C_INCLUDES := $(LOCAL_PATH)/include

include $(BUILD_STATIC_LIBRARY)
