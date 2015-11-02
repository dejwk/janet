/**
 * janet_base.h
 * This file is the part of the Janet framework
 */

#include <jni.h>
#include <janet_jni.h>

/**
 * Class loading
 */

typedef struct _janet_cls_struct {
  jclass id;
  int weak;
  const char* name;
} _janet_cls;

typedef struct _janet_fld_struct {
  jfieldID id;
  _janet_cls* cls;
  int is_static;
  const char* name;
  const char* signature;
} _janet_fld;

typedef struct _janet_mth_struct {
  jmethodID id;
  _janet_cls* cls;
  int is_static;
  const char* name;
  const char* signature;
} _janet_mth;

typedef struct _janet_str_struct {
  jstring strref;
  const char* utf;
} _janet_str;

extern _janet_cls _jc_janet_classes[];
extern _janet_mth _jm_janet_methods[];
extern _janet_fld _jf_janet_fields[];

#define _JANET_CLS_OBJECT                    (_jc_janet_classes[0].id)
#define _JANET_CLS_THROWABLE                 (_jc_janet_classes[1].id)
#define _JANET_ERR_UNKNOWN                   (_jc_janet_classes[2].id)
#define _JANET_ERR_INTERNAL                  (_jc_janet_classes[3].id)
#define _JANET_ERR_OUT_OF_MEMORY             (_jc_janet_classes[4].id)
#define _JANET_ERR_NO_CLASS_DEF_FOUND        (_jc_janet_classes[5].id)
#define _JANET_ERR_CLASS_FORMAT              (_jc_janet_classes[6].id)
#define _JANET_ERR_CLASS_CIRCULARITY         (_jc_janet_classes[7].id)
#define _JANET_ERR_EXCEPTION_IN_INITIALIZER  (_jc_janet_classes[8].id)
#define _JANET_ERR_NO_SUCH_FIELD             (_jc_janet_classes[9].id)
#define _JANET_ERR_NO_SUCH_METHOD            (_jc_janet_classes[10].id)
#define _JANET_EXC_NULL_POINTER              (_jc_janet_classes[11].id)
#define _JANET_EXC_ARRAY_INDEX_OUT_OF_BOUNDS (_jc_janet_classes[12].id)
#define _JANET_EXC_NEGATIVE_ARRAY_SIZE       (_jc_janet_classes[13].id)
#define _JANET_EXC_ARITHMETIC                (_jc_janet_classes[14].id)
#define _JANET_EXC_CLASS_CAST                (_jc_janet_classes[15].id)
#define _JANET_CLS_STRING                    (_jc_janet_classes[16].id)

#define _JANET_MTH_OBJECT_HASH_CODE          (_jm_janet_methods[0].id)
#define _JANET_MTH_THROWABLE_GET_MESSAGE     (_jm_janet_methods[1].id)
#define _JANET_INIT_OUT_OF_MEMORY            (_jm_janet_methods[2].id)
#define _JANET_INIT_NULL_POINTER_EXCEPTION   (_jm_janet_methods[3].id)
#define _JANET_MTH_STRING_INTERN             (_jm_janet_methods[4].id)



#define JANET_LINK_MODE_LAZY   0
#define JANET_LINK_MODE_MEDIUM 1
#define JANET_LINK_MODE_EAGER  2

#ifndef JANET_LINK_MODE
#define JANET_LINK_MODE JANET_LINK_MODE_LAZY
#endif

#define _JANET_CLASS(idx) (_janet_depclasses[idx].id)
#define _JANET_METHOD(idx) (_janet_depmethods[idx].id)
#define _JANET_FIELD(idx) (_janet_depfields[idx].id)
#define _JANET_STRING(idx) (_janet_depstrings[idx].strref)

#define _JANET_LOAD_CLASS(idx) \
   do { \
      if (!_JANET_CLASS(idx) &&\
            !_j3_janet_loadClass(_janet_jnienv, &_janet_depclasses[idx],\
                                 _JANET__FILE__, _JANET__LINE__))\
         return;\
   } while (0)

#define _JANET_LOAD_FIELD(idx) \
   do { \
      if (!_JANET_FIELD(idx) &&\
            !_j4_janet_loadField(_janet_jnienv, &_janet_depfields[idx],\
                                 _JANET__FILE__, _JANET__LINE__))\
         return;\
   } while (0)

#define _JANET_LOAD_METHOD(idx) \
   do { \
      if (!_JANET_METHOD(idx) &&\
            !_j5_janet_loadMethod(_janet_jnienv, &_janet_depmethods[idx],\
                                  _JANET__FILE__, _JANET__LINE__))\
         return;\
   } while (0)

#define _JANET_LOAD_STRING(idx) \
   do { \
      if (!_JANET_STRING(idx) &&\
            !_j9_janet_loadString(_janet_jnienv, &_janet_depstrings[idx],\
                                  _JANET__FILE__, _JANET__LINE__))\
         return;\
   } while (0)
     

#ifndef NDEBUG
#define _JANET__FILE__ __FILE__
#define _JANET__LINE__ __LINE__
#define _JANET_ASSERT(x) ((x) || _ja_janet_assert(_janet_jnienv, #x, __FILE__, __LINE__))
extern jboolean _janet_assertionFailed;
int _ja_janet_assert(JNIEnv*, const char*, const char*, unsigned int);
#else
#define _JANET__FILE__ ((void*)0)
#define _JANET__LINE__ (-1)
#define _JANET_ASSERT(x) ((void*)0)
#endif

#define _JANET_INIT() _j1_janet_init(_janet_jnienv)
#define _JANET_FINALIZE() _j2_janet_finalize(_janet_jnienv);

#define _JANET_LINK(classes, classno, fields, fieldsno, methods, methodsno, strings, stringsno) \
   if (JANET_LINK_MODE >= JANET_LINK_MODE_MEDIUM) {\
      _j6_janet_loadClasses(_janet_jnienv, classes, classno,\
                            _JANET__FILE__, _JANET__LINE__);\
   }\
   if (JANET_LINK_MODE >= JANET_LINK_MODE_EAGER) {\
      _j7_janet_loadMembers(_janet_jnienv, fields, fieldsno, methods, methodsno,\
			    strings, stringsno, \
                            _JANET__FILE__, _JANET__LINE__);\
   }

#define _JANET_UNLINK(classes, classno) \
   _j8_janet_releaseClasses(_janet_jnienv, classes, classno);




#ifndef JANET_USE_FAST_ARRAYS
#define JANET_USE_FAST_ARRAYS 0
#endif

/**
 * Hashing references
 */

/*struct _janet_multirefHashTable_struct;*/

struct _janet_arrHashTable_struct;

typedef struct arrHashEntry_struct {
    jarray ref;
    jint hashcode;
    int refcount;
    jint length;
    void* jptr;
    jboolean isCopy;
    void* ptr;
    struct _janet_arrHashTable_struct* htab;
    void (JNICALL *releasef)();
} _janet_arr;

typedef struct _janet_multiref_struct {
    jobject ref;
    int refcount;
    _janet_arr* arr;     /* for primitive-type arrays only */
    jint arrlength;      /* for arrays only */
    const jchar* struni; /* for strings only */
    const char* strutf;  /* for strings only */
  /*    struct _janet_multirefHashTable_struct* htab; */
} _janet_multiref;

typedef _janet_multiref* _janet_multiref_ptr;

/**
 * Hashing arrays
 */

typedef struct _janet_arrHashTable_struct {
    int sizeidx;
    jint fill;
    jint treshold;
    struct _janet_arrHashTable_struct *next;
    _janet_arr *data;
    int dynamic;
} _janet_arrHashTable;

#define _JANET__CAT3(a, b, c) a##b##c
#define _JANET__CAT5(a, b, c, d, e) a##b##c##d##e

#define _JANET_JPTR__BUILD_FGET(Type) \
    _JANET__CAT3((*ENV)->Get, Type, ArrayElements)\

#define _JANET_JPTR__BUILD_FRELEASE(Type) \
    _JANET__CAT3((*ENV)->Release, Type, ArrayElements)\

#define _JANET_JPTR_FGET(type) \
    _JANET_JPTR__BUILD_FGET(_JANET_JPTR_TYPE_(type))

#define _JANET_JPTR_FRELEASE(type) \
    _JANET_JPTR__BUILD_FRELEASE(_JANET_JPTR_TYPE_(type))

#define _JANET_IMPL_JPTR(type) \
    void* _jjp##type##_janet(JNIEnv* ENV, _janet_arr* ref, \
	                     const char* filename, int lineno) { \
    _JANET_ASSERT(ref); _JANET_ASSERT(ref->ref); _JANET_ASSERT(!ref->jptr && !ref->ptr); \
    ref->jptr = _JANET_JPTR_FGET(type)(ENV, ref->ref, &ref->isCopy); \
    if (!ref->jptr) { \
	_je_janet_throw(ENV, _JANET_ERR_OUT_OF_MEMORY, filename, lineno, errcnt2); \
	return 0; \
    } \
    ref->releasef = _JANET_JPTR_FRELEASE(type);\
    return ref->jptr; }

#define _JANET_JPTR_TYPE_(type) _JANET_JPTR_TYPE_##type
#define _JANET_JPTR_TYPE_Z  Boolean
#define _JANET_JPTR_TYPE_B  Byte
#define _JANET_JPTR_TYPE_C  Char
#define _JANET_JPTR_TYPE_S  Short
#define _JANET_JPTR_TYPE_I  Int
#define _JANET_JPTR_TYPE_J  Long
#define _JANET_JPTR_TYPE_F  Float
#define _JANET_JPTR_TYPE_D  Double

#define _JANET_REFARRAY_LENGTH(jref) \
    (_JANET_ASSERT(jref && jref->ref), \
        (jref->length >= 0 ? jref->length : \
        (jref->length = JNI_GET_ARRAY_LENGTH(jref->ref))))

#define _JANET_ARRAY_COPY(ref, JTYPE, TYPE) \
    do { _JANET_ASSERT(ref && ref->jptr);\
        { jint size = _JANET_REFARRAY_LENGTH(ref); \
          JTYPE* ptrfrom = (JTYPE*)ref->jptr; \
          TYPE* ptrto = (TYPE*)malloc(size * sizeof(TYPE)); \
          if (!ptrto) { \
	      _je_janet_throw(ENV, _JANET_ERR_OUT_OF_MEMORY, filename, lineno, \
                  "failed to hard-copy an array of %16.d " #JTYPE " values " \
		  "to " #TYPE "[]", size); \
              ref->ptr = 0; \
          } else {\
              ref->ptr = ptrto; \
              while (size--) *ptrto++ = *ptrfrom++; \
          } \
         }} while(0)

/*
#define _JANET_DECLARE_ARRAY_CONVERT(JTYPE, TYPE) \
    static void* _janet_arrcnv_##JTYPE##_to_##TYPE##(JNIEnv*, _janet_arr*, \
                                                     const char*, int);
#define _JANET_IMPLEMENT_ARRAY_CONVERT(JTYPE, TYPE) \
    void* _janet_arrcnv_##JTYPE##_to_##TYPE##(JNIEnv* ENV, _janet_arr* ref, \
            const char* filename, int lineno) { \
        union { JTYPE a; TYPE b; } u; \
        if (sizeof(JTYPE) == sizeof(TYPE) && \
	       (u.a = (JTYPE)1, u.a == (JTYPE)u.b) && \
               (u.b = (TYPE)1, u.b = (TYPE)u.a) && \
               (((JTYPE)-1 < (JTYPE)0 && (TYPE)-1 < (TYPE)0) || \
                ((JTYPE)-1 > (JTYPE)0 && (TYPE)-1 > (TYPE)0)) && \
               (u.a = ((JTYPE)1) / ((JTYPE)17), u.a == (JTYPE)u.b) && \
               (u.b = ((TYPE)1) / ((TYPE)17), u.b == (TYPE)u.a)) { \
            return ref->ptr = (TYPE*)ref->jptr; \
 	} \
        _JANET_ARRAY_COPY(ref, JTYPE, TYPE); \
    }

#define _JANET_ARRAY_CONVERT_FN(JTYPE, TYPE) \
    _janet_arrcnv_##JTYPE##_to_##TYPE
*/

#ifdef __CPLUSPLUS
extern "C" {
#endif

int _j1_janet_init(JNIEnv*);
void _j2_janet_finalize(JNIEnv*);

int _j3_janet_loadClass(JNIEnv*, _janet_cls*, const char*, unsigned int);
int _j4_janet_loadField(JNIEnv*, _janet_fld*, const char*, unsigned int);
int _j5_janet_loadMethod(JNIEnv*, _janet_mth*, const char*, unsigned int);

int _j6_janet_loadClasses(JNIEnv*, _janet_cls*, int, const char*, unsigned int);
int _j7_janet_loadMembers(JNIEnv *, _janet_fld*, int, _janet_mth*, int, 
			  _janet_str*, int,
			  const char*, unsigned int);
void _j8_janet_releaseClasses(JNIEnv*, _janet_cls*, int);

int _j9_janet_loadString(JNIEnv*, _janet_str*, const char*, unsigned int);

void _je_janet_throw(JNIEnv*, jthrowable, const char*, unsigned int,
		     const char*, ...);
jthrowable _je1_janet_newException(JNIEnv*, jthrowable, const char*, unsigned int,
				   const char*, ...);

#ifdef JANET_JNIEXT_1_2
void* _jjp_critical_janet(JNIEnv*, _janet_arr*, const char*, int);
#endif

void* _jcpZ_janet_arrcnv(JNIEnv*, _janet_arr*, const char*, int);
void* _jcpB_janet_arrcnv(JNIEnv*, _janet_arr*, const char*, int);
void* _jcpC_janet_arrcnv(JNIEnv*, _janet_arr*, const char*, int);
void* _jcpS_janet_arrcnv(JNIEnv*, _janet_arr*, const char*, int);
void* _jcpI_janet_arrcnv(JNIEnv*, _janet_arr*, const char*, int);
void* _jcpJ_janet_arrcnv(JNIEnv*, _janet_arr*, const char*, int);
void* _jcpF_janet_arrcnv(JNIEnv*, _janet_arr*, const char*, int);
void* _jcpD_janet_arrcnv(JNIEnv*, _janet_arr*, const char*, int);

void* _jjpZ_janet(JNIEnv*, _janet_arr*, const char*, int);
void* _jjpB_janet(JNIEnv*, _janet_arr*, const char*, int);
void* _jjpC_janet(JNIEnv*, _janet_arr*, const char*, int);
void* _jjpS_janet(JNIEnv*, _janet_arr*, const char*, int);
void* _jjpI_janet(JNIEnv*, _janet_arr*, const char*, int);
void* _jjpJ_janet(JNIEnv*, _janet_arr*, const char*, int);
void* _jjpF_janet(JNIEnv*, _janet_arr*, const char*, int);
void* _jjpD_janet(JNIEnv*, _janet_arr*, const char*, int);

void* _jcp_janet_cptr(JNIEnv*, _janet_arr*,
		      void* (*)(JNIEnv*, _janet_arr*, const char*, int),
		      const char*, int);

/* Hashing references */

  /* _janet_ref2ref* _jrh1_janet_putRef(JNIEnv*, struct _janet_refHashTable_struct*, jobject); */


/* Hashing primitive type arrays */

_janet_arr* _jh1_janet_putArray(JNIEnv*, struct _janet_arrHashTable_struct*, jarray);
void _jh2_janet_rmArray(JNIEnv*, _janet_arr*);
void _jh3_releaseHashTable(JNIEnv*, _janet_arrHashTable*);
void _jm1_releaseMonitors(JNIEnv*, jobject*, int);


#ifdef __CPLUSPLUS
}
#endif
