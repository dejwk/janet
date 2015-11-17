/*******************************************************************************
 * janet_jni.h - wrappers for JNI functions
 * (C) 2000 - 2015 Dawid Kurzyniec
 */

#include <jni.h>

#ifndef JANET_JNI_VERSION
#define JANET_JNIEXT_1_2
#endif

#ifdef __cplusplus
#define JNI_CALL0(method) \
   _janet_jnienv->method()
#define JNI_CALL1(method, a) \
   _janet_jnienv->method(a)
#define JNI_CALL2(method, a, b) \
   _janet_jnienv->method(a, b)
#define JNI_CALL3(method, a, b, c) \
   _janet_jnienv->method(a, b, c)
#define JNI_CALL4(method, a, b, c, d) \
   _janet_jnienv->method(a, b, c, d)
#else
#define JNI_CALL0(method) \
   (*_janet_jnienv)->method(_janet_jnienv)
#define JNI_CALL1(method, a) \
   (*_janet_jnienv)->method(_janet_jnienv, a)
#define JNI_CALL2(method, a, b) \
   (*_janet_jnienv)->method(_janet_jnienv, a, b)
#define JNI_CALL3(method, a, b, c) \
   (*_janet_jnienv)->method(_janet_jnienv, a, b, c)
#define JNI_CALL4(method, a, b, c, d) \
   (*_janet_jnienv)->method(_janet_jnienv, a, b, c, d)
#endif

/* Version Information */

#define JNI_GET_VERSION JNI_CALL0(GetVersion)

/* Class Operations */

#define JNI_DEFINE_CLASS(loader, buf, buflen) \
   JNI_CALL3(DefineClass, loader, buf, buflen)

#define JNI_FIND_CLASS(name) \
   JNI_CALL1(FindClass, name)

#define JNI_GET_SUPERCLASS(clazz) \
   JNI_CALL(GetSuperclass, clazz)

#define JNI_IS_ASSIGNABLE_FROM(clazz1, clazz2) \
   JNI_CALL2(IsAssignableFrom, clazz1, clazz2)

/* Exceptions */

#define JNI_THROW(e) \
   JNI_CALL1(Throw, e)

#define JNI_THROW_NEW(clazz, message) \
   JNI_CALL2(ThrowNew, clazz, message)

#define JNI_EXCEPTION_OCCURRED() \
   JNI_CALL0(ExceptionOccurred)

#define JNI_EXCEPTION_DESCRIBE() \
   JNI_CALL0(ExceptionDescribe)

#define JNI_EXCEPTION_CLEAR() \
   JNI_CALL0(ExceptionClear)

#define JNI_FATAL_ERROR(msg) \
   JNI_CALL1(FatalError, msg)

/* Global and Local References */

#define JNI_NEW_GLOBAL_REF(obj) \
   JNI_CALL1(NewGlobalRef, obj)

#define JNI_DELETE_GLOBAL_REF(globalRef) \
   JNI_CALL1(DeleteGlobalRef, globalRef)

#define JNI_DELETE_LOCAL_REF(localRef) \
   JNI_CALL1(DeleteLocalRef, localRef)

/* Object Operations */

#define JNI_ALLOC_OBJECT(clazz) \
   JNI_CALL1(AllocObject, clazz)

#define JNI_NEW_OBJECT(clazz, methodID, args) \
   JNI_CALL3(NewObject, clazz, methodID _J2N_UNWRAP(args))

#define JNI_NEW_OBJECT_A(clazz, methodID, args) \
   JNI_CALL3(NewObjectA, clazz, methodID, args)

#define JNI_NEW_OBJECT_V(clazz, methodID, args) \
   JNI_CALL3(NewObjectV, clazz, methodID, args)

#define JNI_GET_OBJECT_CLASS(obj) \
   JNI_CALL1(GetObjectClass, obj)

#define JNI_IS_INSTANCE_OF(obj, clazz) \
   JNI_CALL2(IsInstanceOf, obj, clazz)

#define JNI_IS_SAME_OBJECT(ref1, ref2) \
   JNI_CALL2(IsSameObject, ref1, ref2)

/* Accessing Fields of Objects */

#define JNI_GET_FIELD_ID(clazz, name, sig) \
   JNI_CALL3(GetFieldID, clazz, name, sig)

#define JNI_GET_FIELD(type, obj, fieldID) \
   JNI_CALL2(_J2N_TFNAME(Get, type, Field), obj, fieldID)

#define JNI_SET_FIELD(type, obj, fieldID, value) \
   JNI_CALL3(_J2N_TFNAME(Set, type, Field), obj, fieldID, value)

/* Calling Instance Methods */

#define JNI_GET_METHOD_ID(clazz, name, sig) \
   JNI_CALL3(GetMethodID, clazz, name, sig)

#define JNI_CALL_METHOD(type, obj, methodID, args) \
   JNI_CALL3(_J2N_TFNAME(Call, type, Method), obj, methodID \
                                          _J2N_UNWRAP(args))

#define JNI_CALL_METHOD_A(type, obj, methodID, args) \
   JNI_CALL3(_J2N_TFNAME(Call, type, MethodA), obj, methodID, args)

#define JNI_CALL_METHOD_V(type, obj, methodID, args) \
   JNI_CALL3(_J2N_TFNAME(Call, type, MethodV), obj, methodID, args)

#define JNI_CALL_NONVIRTUAL_METHOD(type, obj, clazz, methodID, args) \
   JNI_CALL4(_J2N_TFNAME(CallNonvirtual, type, Method), obj, clazz, methodID \
                                                    _J2N_UNWRAP(args))

#define JNI_CALL_NONVIRTUAL_METHOD_A(type, obj, clazz, methodID, args) \
   JNI_CALL4(_J2N_TFNAME(CallNonvirtual, type, MethodA), obj, clazz, \
                                                     methodID, args)

#define JNI_CALL_NONVIRTUAL_METHOD_V(type, obj, clazz, methodID, args) \
   JNI_CALL4(_J2N_TFNAME(CallNonvirtual, type, MethodV), obj, clazz, \
                                                     methodID, args)

/* Accessing Static Fields */

#define JNI_GET_STATIC_FIELD_ID(clazz, name, sig) \
   JNI_CALL3(GetStaticFieldID, clazz, name, sig)

#define JNI_GET_STATIC_FIELD(type, clazz, fieldID) \
   JNI_CALL2(_J2N_TFNAME(GetStatic, type, Field), clazz, fieldID)

#define JNI_SET_STATIC_FIELD(type, clazz, fieldID, value) \
   JNI_CALL3(_J2N_TFNAME(SetStatic, type, Field), clazz, fieldID, value)

/* Calling Static Methods */

#define JNI_GET_STATIC_METHOD_ID(clazz, name, sig) \
   JNI_CALL3(GetStaticMethodID, clazz, name, sig)

#define JNI_CALL_STATIC_METHOD(type, clazz, methodID, args) \
   JNI_CALL3(_J2N_TFNAME(CallStatic, type, Method), clazz, methodID, \
                                                _J2N_UNWRAP(args))

#define JNI_CALL_STATIC_METHOD_A(type, clazz, methodID, args) \
   JNI_CALL3(_J2N_TFNAME(CallStatic, type, MethodA), clazz, methodID, args)

#define JNI_CALL_STATIC_METHOD_V(type, clazz, methodID, args) \
   JNI_CALL3(_J2N_TFNAME(CallStatic, type, MethodV), clazz, methodID, args)

/* String Operations */

#define JNI_NEW_STRING(unicodeChars, len) \
   JNI_CALL2(NewString, unicodeChars, len)

#define JNI_GET_STRING_LENGTH(string) \
   JNI_CALL1(GetStringLength, (jstring)(string))

#define JNI_GET_STRING_CHARS(string, isCopy) \
   JNI_CALL2(GetStringChars, string, isCopy)

#define JNI_RELEASE_STRING_CHARS(string, chars) \
   JNI_CALL2(ReleaseStringChars, string, chars)

#define JNI_NEW_STRING_UTF(bytes) \
   JNI_CALL1(NewStringUTF, bytes)

#define JNI_GET_STRING_UTF_LENGTH(string) \
   JNI_CALL1(GetStringUTFLength, string)

#define JNI_GET_STRING_UTF_CHARS(string, isCopy) \
   JNI_CALL2(GetStringUTFChars, string, isCopy)

#define JNI_RELEASE_STRING_UTF_CHARS(string, utf) \
   JNI_CALL2(ReleaseStringUTFChars, string, utf)

/* Array Operations */

#define JNI_GET_ARRAY_LENGTH(array) \
   JNI_CALL1(GetArrayLength, array)

#define JNI_NEW_OBJECT_ARRAY(length, elementClass, initialElement) \
   JNI_CALL3(NewObjectArray, length, elementClass, initialElement)

#define JNI_GET_OBJECT_ARRAY_ELEMENT(array, index) \
   JNI_CALL2(GetObjectArrayElement, array, index)

#define JNI_SET_OBJECT_ARRAY_ELEMENT(array, index, value) \
   JNI_CALL3(SetObjectArrayElement, array, index, value)

#define JNI_NEW_ARRAY(type, length) \
   JNI_CALL2(_J2N_TFNAME(New, type, Array), length)


#define JNI_GET_BOOLEAN_ARRAY_ELEMENTS(array, isCopy) \
   JNI_CALL2(GetBooleanArrayElements, array, isCopy)
#define JNI_GET_BYTE_ARRAY_ELEMENTS(array, isCopy) \
   JNI_CALL2(GetByteArrayElements, array, isCopy)
#define JNI_GET_SHORT_ARRAY_ELEMENTS(array, isCopy) \
   JNI_CALL2(GetShortArrayElements, array, isCopy)
#define JNI_GET_CHAR_ARRAY_ELEMENTS(array, isCopy) \
   JNI_CALL2(GetCharArrayElements, array, isCopy)
#define JNI_GET_INT_ARRAY_ELEMENTS(array, isCopy) \
   JNI_CALL2(GetIntArrayElements, array, isCopy)
#define JNI_GET_LONG_ARRAY_ELEMENTS(array, isCopy) \
   JNI_CALL2(GetLongArrayElements, array, isCopy)
#define JNI_GET_FLOAT_ARRAY_ELEMENTS(array, isCopy) \
   JNI_CALL2(GetFloatArrayElements, array, isCopy)
#define JNI_GET_DOUBLE_ARRAY_ELEMENTS(array, isCopy) \
   JNI_CALL2(GetDoubleArrayElements, array, isCopy)


#define JNI_RELEASE_BOOLEAN_ARRAY_ELEMENTS(array, elems, mode) \
   JNI_CALL3(GetBooleanArrayElements, array, elems, mode)
#define JNI_RELEASE_BYTE_ARRAY_ELEMENTS(array, elems, mode) \
   JNI_CALL3(GetByteArrayElements, array, elems, mode)
#define JNI_RELEASE_SHORT_ARRAY_ELEMENTS(array, elems, mode) \
   JNI_CALL3(GetShortArrayElements, array, elems, mode)
#define JNI_RELEASE_CHAR_ARRAY_ELEMENTS(array, elems, mode) \
   JNI_CALL3(GetCharArrayElements, array, elems, mode)
#define JNI_RELEASE_INT_ARRAY_ELEMENTS(array, elems, mode) \
   JNI_CALL3(GetIntArrayElements, array, elems, mode)
#define JNI_RELEASE_LONG_ARRAY_ELEMENTS(array, elems, mode) \
   JNI_CALL3(GetLongArrayElements, array, elems, mode)
#define JNI_RELEASE_FLOAT_ARRAY_ELEMENTS(array, elems, mode) \
   JNI_CALL3(GetFloatArrayElements, array, elems, mode)
#define JNI_RELEASE_DOUBLE_ARRAY_ELEMENTS(array, elems, mode) \
   JNI_CALL3(GetDoubleArrayElements, array, elems, mode)

#define JNI_GET_ARRAY_ELEMENTS(type, array, isCopy) \
   JNI_CALL2(_J2N_TFNAME(Get, type, ArrayElements), array, isCopy)

#define JNI_RELEASE_ARRAY_ELEMENTS(type, array, elems, mode) \
   JNI_CALL3(_J2N_TFNAME(Release, type, ArrayElements), array, elems, mode)

#define JNI_GET_ARRAY_REGION(type, array, start, len, buf) \
   JNI_CALL4(_J2N_TFNAME(Get, type, ArrayRegion), array, start, len, buf)

#define JNI_SET_ARRAY_REGION(type, array, start, len, buf) \
   JNI_CALL4(_J2N_TFNAME(Set, type, ArrayRegion), array, start, len, buf)

/* Registering Native Methods */

#define JNI_REGISTER_NATIVES(clazz, methods, nMethods) \
   JNI_CALL3(RegisterNatives, clazz, methods, nMethods)

#define JNI_UNREGISTER_NATIVES(clazz) \
   JNI_CALL1(UnegisterNatives, clazz)

/* Monitor Operations */

#define JNI_MONITOR_ENTER(obj)       JNI_CALL1(MonitorEnter, obj)
#define JNI_MONITOR_EXIT(obj)        JNI_CALL1(MonitorExit, obj)

/* Java VM Interface */

#define JNI_GET_JAVA_VM(vm)   JNI_CALL1(GetJavaVM, vm)

/* JNI 1.2 enhancements */

#define JNI_EXCEPTION_CHECK() \
   JNI_CALL0(ExceptionCheck)

#define JNI_NEW_WEAK_GLOBAL_REF(obj) \
   JNI_CALL1(NewWeakGlobalRef, obj)

#define JNI_DELETE_WEAK_GLOBAL_REF(obj) \
   JNI_CALL1(DeleteWeakGlobalRef, obj)

#define JNI_GET_PRIMITIVE_ARRAY_CRITICAL(arr, isCopy) \
   JNI_CALL2(GetPrimitiveArrayCritical, arr, isCopy)

#define JNI_RELEASE_PRIMITIVE_ARRAY_CRITICAL(arr, carr, mode) \
   JNI_CALL3(ReleasePrimitiveArrayCritical, arr, carr, mode)

#define JNI_GET_STRING_CRITICAL(str, isCopy) \
   JNI_CALL2(GetStringCritical, str, isCopy)

#define JNI_RELEASE_STRING_CRITICAL(str, cstr) \
   JNI_CALL2(ReleaseStringCritical, str, cstr)

/* Janet enhancements */
