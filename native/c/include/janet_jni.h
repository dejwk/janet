/*******************************************************************************
 * janet_jni.h - wrappers for JNI functions
 * (C) 2000 Dawid Kurzyniec
 */

#include <jni.h>

#ifndef JANET_JNI_VERSION
#ifdef JNI_VERSION_1_2
#define JANET_JNIEXT_1_2
#endif
#endif

/* Version Information */

#define JNI_GET_VERSION          (*_janet_jnienv)->GetVersion(_janet_jnienv)

/* Class Operations */

#define JNI_DEFINE_CLASS(loader, buf, buflen) \
   (*_janet_jnienv)->DefineClass(_janet_jnienv, loader, buf, buflen)

#define JNI_FIND_CLASS(name) \
   (*_janet_jnienv)->FindClass(_janet_jnienv, name)

#define JNI_GET_SUPERCLASS(clazz) \
   (*_janet_jnienv)->GetSuperclass(_janet_jnienv, clazz)

#define JNI_IS_ASSIGNABLE_FROM(clazz1, clazz2) \
   (*_janet_jnienv)->IsAssignableFrom(_janet_jnienv, clazz1, clazz2)

/* Exceptions */

#define JNI_THROW(e) \
   (*_janet_jnienv)->Throw(_janet_jnienv, e)

#define JNI_THROW_NEW(clazz, message) \
   (*_janet_jnienv)->ThrowNew(_janet_jnienv, clazz, message)

#define JNI_EXCEPTION_OCCURRED() \
   (*_janet_jnienv)->ExceptionOccurred(_janet_jnienv)

#define JNI_EXCEPTION_DESCRIBE() \
   (*_janet_jnienv)->ExceptionDescribe(_janet_jnienv)

#define JNI_EXCEPTION_CLEAR() \
   (*_janet_jnienv)->ExceptionClear(_janet_jnienv)

#define JNI_FATAL_ERROR(msg) \
   (*_janet_jnienv)->FatalError(_janet_jnienv, msg)

/* Global and Local References */

#define JNI_NEW_GLOBAL_REF(obj) \
   (*_janet_jnienv)->NewGlobalRef(_janet_jnienv, obj)

#define JNI_DELETE_GLOBAL_REF(globalRef) \
   (*_janet_jnienv)->DeleteGlobalRef(_janet_jnienv, globalRef)

#define JNI_DELETE_LOCAL_REF(localRef) \
   (*_janet_jnienv)->DeleteLocalRef(_janet_jnienv, localRef)

/* Object Operations */

#define JNI_ALLOC_OBJECT(clazz) \
   (*_janet_jnienv)->AllocObject(_janet_jnienv, clazz)

#define JNI_NEW_OBJECT(clazz, methodID, args) \
   (*_janet_jnienv)->NewObject(_janet_jnienv, clazz, methodID _J2N_UNWRAP(args))

#define JNI_NEW_OBJECT_A(clazz, methodID, args) \
   (*_janet_jnienv)->NewObjectA(_janet_jnienv, clazz, methodID, args)

#define JNI_NEW_OBJECT_V(clazz, methodID, args) \
   (*_janet_jnienv)->NewObjectV(_janet_jnienv, clazz, methodID, args)

#define JNI_GET_OBJECT_CLASS(obj) \
   (*_janet_jnienv)->GetObjectClass(_janet_jnienv, obj)

#define JNI_IS_INSTANCE_OF(obj, clazz) \
   (*_janet_jnienv)->IsInstanceOf(_janet_jnienv, obj, clazz)

#define JNI_IS_SAME_OBJECT(ref1, ref2) \
   (*_janet_jnienv)->IsSameObject(_janet_jnienv, ref1, ref2)

/* Accessing Fields of Objects */

#define JNI_GET_FIELD_ID(clazz, name, sig) \
   (*_janet_jnienv)->GetFieldID(_janet_jnienv, clazz, name, sig)

#define JNI_GET_FIELD(type, obj, fieldID) \
   (*_janet_jnienv)->_J2N_TFNAME(Get, type, Field)(_janet_jnienv, obj, fieldID)

#define JNI_SET_FIELD(type, obj, fieldID, value) \
   (*_janet_jnienv)->_J2N_TFNAME(Set, type, Field)(_janet_jnienv, obj, fieldID, value)

/* Calling Instance Methods */

#define JNI_GET_METHOD_ID(clazz, name, sig) \
   (*_janet_jnienv)->GetMethodID(_janet_jnienv, clazz, name, sig)

#define JNI_CALL_METHOD(type, obj, methodID, args) \
   (*_janet_jnienv)->_J2N_TFNAME(Call, type, Method)(_janet_jnienv, obj, methodID \
                                          _J2N_UNWRAP(args))

#define JNI_CALL_METHOD_A(type, obj, methodID, args) \
   (*_janet_jnienv)->_J2N_TFNAME(Call, type, MethodA)(_janet_jnienv, obj, methodID, args)

#define JNI_CALL_METHOD_V(type, obj, methodID, args) \
   (*_janet_jnienv)->_J2N_TFNAME(Call, type, MethodV)(_janet_jnienv, obj, methodID, args)

#define JNI_CALL_NONVIRTUAL_METHOD(type, obj, clazz, methodID, args) \
   (*_janet_jnienv)->_J2N_TFNAME(CallNonvirtual, type, Method)(_janet_jnienv, obj, clazz, methodID \
                                                    _J2N_UNWRAP(args))

#define JNI_CALL_NONVIRTUAL_METHOD_A(type, obj, clazz, methodID, args) \
   (*_janet_jnienv)->_J2N_TFNAME(CallNonvirtual, type, MethodA)(_janet_jnienv, obj, clazz, \
                                                     methodID, args)

#define JNI_CALL_NONVIRTUAL_METHOD_V(type, obj, clazz, methodID, args) \
   (*_janet_jnienv)->_J2N_TFNAME(CallNonvirtual, type, MethodV)(_janet_jnienv, obj, clazz, \
                                                     methodID, args)

/* Accessing Static Fields */

#define JNI_GET_STATIC_FIELD_ID(clazz, name, sig) \
   (*_janet_jnienv)->GetStaticFieldID(_janet_jnienv, clazz, name, sig)

#define JNI_GET_STATIC_FIELD(type, clazz, fieldID) \
   (*_janet_jnienv)->_J2N_TFNAME(GetStatic, type, Field)(_janet_jnienv, clazz, fieldID)

#define JNI_SET_STATIC_FIELD(type, clazz, fieldID, value) \
   (*_janet_jnienv)->_J2N_TFNAME(SetStatic, type, Field)(_janet_jnienv, clazz, fieldID, value)

/* Calling Static Methods */

#define JNI_GET_STATIC_METHOD_ID(clazz, name, sig) \
   (*_janet_jnienv)->GetStaticMethodID(_janet_jnienv, clazz, name, sig)

#define JNI_CALL_STATIC_METHOD(type, clazz, methodID, args) \
   (*_janet_jnienv)->_J2N_TFNAME(CallStatic, type, Method)(_janet_jnienv, clazz, methodID \
                                                _J2N_UNWRAP(args))

#define JNI_CALL_STATIC_METHOD_A(type, clazz, methodID, args) \
   (*_janet_jnienv)->_J2N_TFNAME(CallStatic, type, MethodA)(_janet_jnienv, clazz, methodID, args)

#define JNI_CALL_STATIC_METHOD_V(type, clazz, methodID, args) \
   (*_janet_jnienv)->_J2N_TFNAME(CallStatic, type, MethodV)(_janet_jnienv, clazz, methodID, args)

/* String Operations */

#define JNI_NEW_STRING(unicodeChars, len) \
   (*_janet_jnienv)->NewString(_janet_jnienv, unicodeChars, len)

#define JNI_GET_STRING_LENGTH(string) \
   (*_janet_jnienv)->GetStringLength(_janet_jnienv, string)

#define JNI_GET_STRING_CHARS(string, isCopy) \
   (*_janet_jnienv)->GetStringChars(_janet_jnienv, string, isCopy)

#define JNI_RELEASE_STRING_CHARS(string, chars) \
   (*_janet_jnienv)->ReleaseStringChars(_janet_jnienv, string, chars)

#define JNI_NEW_STRING_UTF(bytes) \
   (*_janet_jnienv)->NewStringUTF(_janet_jnienv, bytes)

#define JNI_GET_STRING_UTF_LENGTH(string) \
   (*_janet_jnienv)->GetStringUTFLength(_janet_jnienv, string)

#define JNI_GET_STRING_UTF_CHARS(string, isCopy) \
   (*_janet_jnienv)->GetStringUTFChars(_janet_jnienv, string, isCopy)

#define JNI_RELEASE_STRING_UTF_CHARS(string, utf) \
   (*_janet_jnienv)->ReleaseStringUTFChars(_janet_jnienv, string, utf)

/* Array Operations */

#define JNI_GET_ARRAY_LENGTH(array) \
   (*_janet_jnienv)->GetArrayLength(_janet_jnienv, array)

#define JNI_NEW_OBJECT_ARRAY(length, elementClass, initialElement) \
   (*_janet_jnienv)->NewObjectArray(_janet_jnienv, length, elementClass, initialElement)

#define JNI_GET_OBJECT_ARRAY_ELEMENT(array, index) \
   (*_janet_jnienv)->GetObjectArrayElement(_janet_jnienv, array, index)

#define JNI_SET_OBJECT_ARRAY_ELEMENT(array, index, value) \
   (*_janet_jnienv)->SetObjectArrayElement(_janet_jnienv, array, index, value)

#define JNI_NEW_ARRAY(type, length) \
   (*_janet_jnienv)->_J2N_TFNAME(New, type, Array)(_janet_jnienv, length)


#define JNI_GET_BOOLEAN_ARRAY_ELEMENTS(array, isCopy) \
   (*_janet_jnienv)->GetBooleanArrayElements(_janet_jnienv, array, isCopy)
#define JNI_GET_BYTE_ARRAY_ELEMENTS(array, isCopy) \
   (*_janet_jnienv)->GetByteArrayElements(_janet_jnienv, array, isCopy)
#define JNI_GET_SHORT_ARRAY_ELEMENTS(array, isCopy) \
   (*_janet_jnienv)->GetShortArrayElements(_janet_jnienv, array, isCopy)
#define JNI_GET_CHAR_ARRAY_ELEMENTS(array, isCopy) \
   (*_janet_jnienv)->GetCharArrayElements(_janet_jnienv, array, isCopy)
#define JNI_GET_INT_ARRAY_ELEMENTS(array, isCopy) \
   (*_janet_jnienv)->GetIntArrayElements(_janet_jnienv, array, isCopy)
#define JNI_GET_LONG_ARRAY_ELEMENTS(array, isCopy) \
   (*_janet_jnienv)->GetLongArrayElements(_janet_jnienv, array, isCopy)
#define JNI_GET_FLOAT_ARRAY_ELEMENTS(array, isCopy) \
   (*_janet_jnienv)->GetFloatArrayElements(_janet_jnienv, array, isCopy)
#define JNI_GET_DOUBLE_ARRAY_ELEMENTS(array, isCopy) \
   (*_janet_jnienv)->GetDoubleArrayElements(_janet_jnienv, array, isCopy)


#define JNI_RELEASE_BOOLEAN_ARRAY_ELEMENTS(array, elems, mode) \
   (*_janet_jnienv)->GetBooleanArrayElements(_janet_jnienv, array, elems, mode)
#define JNI_RELEASE_BYTE_ARRAY_ELEMENTS(array, elems, mode) \
   (*_janet_jnienv)->GetByteArrayElements(_janet_jnienv, array, elems, mode)
#define JNI_RELEASE_SHORT_ARRAY_ELEMENTS(array, elems, mode) \
   (*_janet_jnienv)->GetShortArrayElements(_janet_jnienv, array, elems, mode)
#define JNI_RELEASE_CHAR_ARRAY_ELEMENTS(array, elems, mode) \
   (*_janet_jnienv)->GetCharArrayElements(_janet_jnienv, array, elems, mode)
#define JNI_RELEASE_INT_ARRAY_ELEMENTS(array, elems, mode) \
   (*_janet_jnienv)->GetIntArrayElements(_janet_jnienv, array, elems, mode)
#define JNI_RELEASE_LONG_ARRAY_ELEMENTS(array, elems, mode) \
   (*_janet_jnienv)->GetLongArrayElements(_janet_jnienv, array, elems, mode)
#define JNI_RELEASE_FLOAT_ARRAY_ELEMENTS(array, elems, mode) \
   (*_janet_jnienv)->GetFloatArrayElements(_janet_jnienv, array, elems, mode)
#define JNI_RELEASE_DOUBLE_ARRAY_ELEMENTS(array, elems, mode) \
   (*_janet_jnienv)->GetDoubleArrayElements(_janet_jnienv, array, elems, mode)



#define JNI_GET_ARRAY_ELEMENTS(type, array, isCopy) \
   (*_janet_jnienv)->_J2N_TFNAME(Get, type, ArrayElements)(_janet_jnienv, array, isCopy)

#define JNI_RELEASE_ARRAY_ELEMENTS(type, array, elems, mode) \
   (*_janet_jnienv)->_J2N_TFNAME(Release, type, ArrayElements)(_janet_jnienv, array, elems, mode)

#define JNI_GET_ARRAY_REGION(type, array, start, len, buf) \
   (*_janet_jnienv)->_J2N_TFNAME(Get, type, ArrayRegion)(_janet_jnienv, array, start, len, buf)

#define JNI_SET_ARRAY_REGION(type, array, start, len, buf) \
   (*_janet_jnienv)->_J2N_TFNAME(Set, type, ArrayRegion)(_janet_jnienv, array, start, len, buf)

/* Registering Native Methods */

#define JNI_REGISTER_NATIVES(clazz, methods, nMethods) \
   (*_janet_jnienv)->RegisterNatives(_janet_jnienv, clazz, methods, nMethods)

#define JNI_UNREGISTER_NATIVES(clazz) \
   (*_janet_jnienv)->UnegisterNatives(_janet_jnienv, clazz)

/* Monitor Operations */

#define JNI_MONITOR_ENTER(obj)       (*_janet_jnienv)->MonitorEnter(_janet_jnienv, obj)
#define JNI_MONITOR_EXIT(obj)        (*_janet_jnienv)->MonitorExit(_janet_jnienv, obj)

/* Java VM Interface */

#define JNI_GET_JAVA_VM(vm)   (*_janet_jnienv)->GetJavaVM(_janet_jnienv, vm)



/* JNI 1.2 enhancements */

#ifdef JANET_JNIEXT_1_2
#define JNI_EXCEPTION_CHECK(aux) \
   (*_janet_jnienv)->ExceptionCheck(_janet_jnienv)

#define JNI_NEW_WEAK_GLOBAL_REF(obj) \
   (*_janet_jnienv)->NewWeakGlobalRef(_janet_jnienv, obj)

#define JNI_DELETE_WEAK_GLOBAL_REF(obj) \
   (*_janet_jnienv)->DeleteWeakGlobalRef(_janet_jnienv, obj)

#define JNI_GET_PRIMITIVE_ARRAY_CRITICAL(arr, isCopy) \
   (*_janet_jnienv)->GetPrimitiveArrayCritical(_janet_jnienv, arr, isCopy)

#define JNI_RELEASE_PRIMITIVE_ARRAY_CRITICAL(arr, carr, mode) \
   (*_janet_jnienv)->ReleasePrimitiveArrayCritical(_janet_jnienv, arr, carr, mode)

#define JNI_GET_STRING_CRITICAL(str, isCopy) \
   (*_janet_jnienv)->GetStringCritical(_janet_jnienv, str, isCopy)

#define JNI_RELEASE_STRING_CRITICAL(str, cstr) \
   (*_janet_jnienv)->ReleaseStringCritical(_janet_jnienv, str, cstr)

#else

#define JNI_EXCEPTION_CHECK(aux) \
   ((aux = JNI_EXCEPTION_OCCURRED) ? (JNI_DELETE_LOCAL_REF(aux), JNI_TRUE) \
                                   : JNI_FALSE)

/* warning: this version do NOT throw an OutOfMemoryError */
#define JNI_NEW_WEAK_GLOBAL_REF(obj) \
   JNI_NEW_GLOBAL_REF(obj)

/* warning: this version do NOT throw an OutOfMemoryError */
#define JNI_DELETE_WEAK_GLOBAL_REF(obj) \
   JNI_DELETE_GLOBAL_REF(obj)

#endif



/* Janet enhancements */
