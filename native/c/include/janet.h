/**
 * janet.h
 * Routines for Janet-C support
 * This file is the part of the Janet framework
 */

#include "janet_base.h"
#include <setjmp.h>

#ifdef __cplusplus
extern "C" {
#endif

/* for long-life (multi) refs */

/* internal */

#define _JANET_DECLARE_MULTIREFS(size) \
   size_t _janet_multirefs_size = (size); \
   _janet_multiref _janet_multirefs[size] = { { 0, 0, 0, 0, 0, 0 } }; \
   unsigned int _janet_multiref_pos = 0

#define _JANET_INSTALL_MULTIREF(jref) \
    _janet_install_multiref(_janet_jnienv, _janet_multirefs, \
        _janet_multirefs_size, &_janet_multiref_pos, (jref))

/*#define _JANET_ARRAY_PUT(arr) \
    _jh1_janet_putArray(_janet_jnienv, _janet_arrhtable, arr)
*/


/**
 * Internals:
 *
 *      install_multiref - when jobject is assigned to the multiref variable
 *      inc_multiref     - when multiref is assigned to local variable
 *      dec_multiref     - when local variable goes out of scope
 *      array_install    - when hashtable array info is atached to the multiref
 *
 */

static _janet_multiref* _janet_install_multiref(JNIEnv* _janet_jnienv,
                                                _janet_multiref* arr_ref,
                                                size_t size, 
                                                unsigned int *pos,
                                                jobject jobj)
{
    if (!jobj) return 0;
    for(;;) {
        if ((*pos) >= size) *pos = 0;
        if (!arr_ref[*pos].refcount) break;
        ++(*pos);
    }
    if (arr_ref[*pos].ref) JNI_DELETE_LOCAL_REF(arr_ref[*pos].ref);
    arr_ref[*pos].ref = jobj;
    arr_ref[*pos].arr = 0;
    arr_ref[*pos].arrlength = -1;
    return &arr_ref[*pos++];
}

static _janet_multiref* _janet_inc_multiref(JNIEnv* _janet_jnienv,
					    _janet_multiref* pref)
{
    if (!pref) return 0;
    _JANET_ASSERT(pref->ref);
    ++pref->refcount;
    return pref;
}

#define _JANET_DEC_MULTIREF(pref) \
    _janet_dec_multiref(_janet_jnienv, pref)

static void _janet_destroy_multiref(JNIEnv* _janet_jnienv, _janet_multiref* pref)
{
    if (!pref) return;
    _JANET_ASSERT(pref->ref);
    /* first, array contents (if any) */
    if (pref->arr) {
        _JANET_ASSERT(pref->arr->ref && pref->arr->refcount > 0);
        if (!--pref->arr->refcount) {
            /* no other multiref pointing to this array */
            _jh2_janet_rmArray(_janet_jnienv, pref->arr);
        } else {
            /* there are others; set ref to 0 in order to
                   avoid DELETE_LOCAL_REF later on this array */
                if (pref->ref == pref->arr->ref) pref->ref = 0;
            }
        pref->arr = 0;
    }
    /* next, string contents (if any) */
    if (pref->struni) {
        JNI_RELEASE_STRING_CHARS((jstring)pref->ref, pref->struni);
        pref->struni = 0;
    }
    if (pref->strutf) {
      JNI_RELEASE_STRING_UTF_CHARS((jstring)pref->ref, pref->strutf);
      pref->strutf = 0;
    }

    pref->arrlength = -1;
}

static void _janet_dec_multiref(JNIEnv* _janet_jnienv, _janet_multiref* pref)
{
    if (!pref) return;
    _JANET_ASSERT(pref->ref && pref->refcount > 0);
    if (!--pref->refcount) { /* refcount reached 0 -> release resources */
        _janet_destroy_multiref(_janet_jnienv, pref);
    }
}

#ifdef __cplusplus
// Top-level guard to release any resources acquired in the function that
// have not yet been cleared. It allows us to omit _JANET_DESTRUCT at the
// top-level scope.
class _JanetMultirefDeleter {
    public:
        _JanetMultirefDeleter(JNIEnv* env, _janet_multiref* refs, size_t size)
            : env_(env), refs_(refs), size_(size) {}

        ~_JanetMultirefDeleter() {
            for (int i = 0; i < size_; ++i) {
                if (refs_[i].ref && refs_[i].refcount > 0) {
                    _janet_destroy_multiref(env_, &refs_[i]);
                }
            }
        }

    private:
        JNIEnv* env_;
        _janet_multiref* refs_;
        size_t size_;
};

#define _JANET_DECLARE_MULTIREF_DELETER \
    _JanetMultirefDeleter _janet_multiref_deleter( \
        _janet_jnienv, _janet_multirefs, _janet_multirefs_size)

#endif

static void _janet_array_install(JNIEnv* _janet_jnienv, 
                                 _janet_arrHashTable* _janet_arrhtable,
                                 _janet_multiref* pref)
{
    _JANET_ASSERT(pref);
    if (pref->arr) return;
    pref->arr = _jh1_janet_putArray(_janet_jnienv, _janet_arrhtable, (jarray)pref->ref);
    _JANET_ASSERT(pref->arr->length < 0 || pref->arrlength < 0 ||
           pref->arr->length == pref->arrlength);
    if (pref->arrlength >= 0) pref->arr->length = pref->arrlength;
}
    
/*
static _janet_arr* _janet_array_release(JNIEnv* _janet_jnienv, _janet_multiref* pref)
{
    _JANET_ASSERT(pref && pref-> arr && pref->arr->refcount > 0);
    if (!--pref->arr->refcount) {
        _jh2_janet_rmArray(_janet_jnienv, pref->arr);
        pref->arr = 0;
    }
    return pref->arr;
}
*/

jobject _ja_janet_new_array(JNIEnv*, int, const char *, int, ...);

#define _JANET_CREATE_ARRAY _ja_janet_new_array

#define _JANET_MULTIARRAY_GET_LENGTH(pref) \
    _janet_multiarray_get_length(_janet_jnienv, pref)

static jint _janet_multiarray_get_length(JNIEnv* _janet_jnienv, _janet_multiref* pref)
{
    _JANET_ASSERT(pref);
    if (pref->arrlength < 0) {
        if (pref->arr) {
            _JANET_ASSERT(pref->arr->ref);
            if (pref->arr->length < 0) {
                pref->arr->length = JNI_GET_ARRAY_LENGTH(pref->arr->ref);
            }
            pref->arrlength = pref->arr->length;
        } else {
            pref->arrlength = JNI_GET_ARRAY_LENGTH((jarray)pref->ref);
        }
    }
    _JANET_ASSERT(!pref->arr || pref->arrlength == pref->arr->length);
    return pref->arrlength;
}

#define _JANET_ARRAY_CHECK_BOUNDS(ref, idx) \
    _JANET_GUARDED_LOCAL_CALL( \
        _janet_array_check_bounds(_janet_jnienv, \
                                  idx, JNI_GET_ARRAY_LENGTH(ref), \
                                  _JANET__FILE__, _JANET__LINE__))

#define _JANET_MULTIARRAY_CHECK_BOUNDS(pref, idx) \
    _JANET_GUARDED_LOCAL_CALL( \
        _janet_array_check_bounds(_janet_jnienv, \
                                  idx, _JANET_MULTIARRAY_GET_LENGTH(pref), \
                                  _JANET__FILE__, _JANET__LINE__))


/*
#define _JANET_INC_MULTIREF(pref) \
    ((pref ? (_JANET_ASSERT(pref->ref), ++pref->refcount) : 0), pref)

#define _JANET_DEC_MULTIREF(pref) \
    ((pref ? (_JANET_ASSERT(pref->ref), _JANET_ASSERT(pref->refcount > 0), \
             (_JANET_ARRAY_RELEASE(pref->arr), \
             (!--pref->refcount && (!pref->arr || pref->arr->ref != pref->ref)) \
                  ? (JNI_DELETE_LOCAL_REF(pref->ref), pref->ref = ((void*)0)) \
                  : 0)) \
	 : 0), pref)
*/



/* dereferencing */

#define _JANET_DEREF(pref) (pref ? pref->ref : 0)



#define _JANET_SIMPLE_COMPARE(r1, r2) \
    _janet_simple_compare(_janet_jnienv, r1, r2)

#define _JANET_MULTIREF_COMPARE(r1, r2) \
    _janet_multiref_compare(_janet_jnienv, r1, r2)





/* simple assignment -> no macro is needed */
            
/* assignment to local variable */




/* for short-life refs */
/*
#define _JANET_RELEASE(var) \
    ((var) ? (JNI_DELETE_LOCAL_REF(var), (var) = 0) : 0)

#define _JANET_ASSIGN(var, ref) \
    ((var ? JNI_DELETE_LOCAL_REF(var) : 0), var = ref)
*/


/*
 * For use:
 *      
 *      
 */

#define _JANET_ASSIGN_SIMPLE2SIMPLE(pl, pr) \
   _janet_assign_simple2simple(_janet_jnienv, (jobject*)(&pl), pr)

#define _JANET_ASSIGN_SIMPLE2MULTI(pl, pr) \
   (pl = _JANET_INSTALL_MULTIREF(pr))

#define _JANET_ASSIGN_MULTI2MULTI(pl, pr) \
   (pl = pr)

#define _JANET_ASSIGN_SIMPLE2LOCV(pl, pr) \
   _janet_assign_simple2locv(_janet_jnienv, _janet_multirefs, \
                             _janet_multirefs_size, \
                             &_janet_multiref_pos, \
                             &pl, pr)

#define _JANET_ASSIGN_MULTI2LOCV(pl, pr) \
   _janet_assign_multi2locv(_janet_jnienv, &pl, pr)


#define _JANET_ARRAY_GET_JPTR(ref, type) \
    _janet_array_get_jptr(_janet_jnienv, _janet_arrhtable, ref, \
                          _jjp##type##_janet, \
                          _JANET__FILE__, _JANET__LINE__, JANET_USE_FAST_ARRAYS)

#define _JANET_ARRAY_GET_CPTR(ref, type) \
    _janet_array_get_cptr(_janet_jnienv, _janet_arrhtable, ref, \
                          _jjp##type##_janet, _jcp##type##_janet_arrcnv, \
                          _JANET__FILE__, _JANET__LINE__, JANET_USE_FAST_ARRAYS)

#define _JANET_STRING_GET_UNICODE(pstr) \
    _janet_string_get_unicode(_janet_jnienv, pstr)

#define _JANET_STRING_GET_UTF(pstr) \
    _janet_string_get_utf(_janet_jnienv, pstr)
    
#define _JANET_CAST_RTCHECK(obj, cls) \
    _JANET_GUARDED_LOCAL_CALL( \
        _janet_cast_rtcheck(_janet_jnienv, obj, cls, \
                            _JANET__FILE__, _JANET__LINE__))

#define _JANET_IS_INSTANCE_OF(obj, cls) \
    _janet_is_instance_of(_janet_jnienv, obj, cls)

#define _JANET_MONITOR_ENTER(idx, obj) \
    JNI_MONITOR_ENTER(_janet_monitors[idx] = obj)

#define _JANET_MONITOR_EXIT(idx) \
    (JNI_MONITOR_EXIT(_janet_monitors[idx]), _janet_monitors[idx] = 0)


#define _JANET_INTEGER_DIVISION(l, r) \
    (_JANET_LOCAL_ENSURE_DIVISOR_NOT_ZERO(r), \
     (l) / (r))








static jobject _janet_assign_simple2simple(JNIEnv* _janet_jnienv,
					   volatile jobject *pl, jobject pr)
{
    if (*pl) JNI_DELETE_LOCAL_REF(*pl);
    return *pl = pr;
}

static _janet_multiref* _janet_assign_simple2locv(JNIEnv* _janet_jnienv, 
                                                  _janet_multiref* _janet_multirefs,
                                                  int _janet_multirefs_size,
                                                  unsigned int* _janet_multiref_pos,
                                                  _janet_multiref *volatile *pl,
                                                  jobject pr)
{
    if (*pl && pr && (*pl)->ref == pr) return *pl;
    _janet_dec_multiref(_janet_jnienv, *pl);
    *pl = _janet_install_multiref(_janet_jnienv,
                                  _janet_multirefs, _janet_multirefs_size, 
                                  _janet_multiref_pos, pr);
    _janet_inc_multiref(_janet_jnienv, *pl);
    return *pl;
}

static _janet_multiref* _janet_assign_multi2locv(JNIEnv* _janet_jnienv, 
                                                 _janet_multiref *volatile *pl,
                                                 _janet_multiref *pr)
{
    if (*pl && pr && (*pl)->ref == pr->ref) return *pl;
    _janet_dec_multiref(_janet_jnienv, *pl);
    *pl = pr;
    _janet_inc_multiref(_janet_jnienv, *pl);
    return *pl;
}


static int _janet_simple_compare(JNIEnv* _janet_jnienv, 
                                 jobject r1, jobject r2)
{
    return r1 == r2 || JNI_IS_SAME_OBJECT(r1, r2);
}


static int _janet_multiref_compare(JNIEnv* _janet_jnienv, 
                                   _janet_multiref* r1, _janet_multiref* r2)
{
    if (r1 == r2) return JNI_TRUE;
    if (!r1 || !r2) return JNI_FALSE; /* null and not-null */
    if ((r1->arr ? r1->arr->ref : r1->ref) == 
        (r2->arr ? r2->arr->ref : r2->ref)) return JNI_TRUE;
    return JNI_IS_SAME_OBJECT(r1->ref, r2->ref);
}

/*
#define _JANET_LOCV_ASSIGN_MULTIREF(psrc, ptgt) \
    (!(psrc && ptgt && psrc->ref == ptgt->ref) ? \
        (_janet_dec_multiref(psrc), \
         (psrc) = (ptgt), \
         _janet_inc_multiref(ptgt) : psrc)

#define _JANET_LOCV_ASSIGN_NEW_MULTIREF(psrc, jtgt) \
    (_JANET_DEC_MULTIREF(psrc), \
     (psrc) = _JANET_INSTALL_MULTIREF(jtgt), \
     _JANET_INC_MULTIREF(psrc))
*/
/* assignment to primitive type array variables */
/*
static void _janet_assign_simple2arrcnt(JNIEnv* _janet_jnienv, 
                                        _janet_multiref** pl, jobject pr)
{
    _janet_assign_simple2multi(_janet_jnienv, pl, pr);
    _janet_array_install(*pl);
}
*/
/*
static void _janet_assign_multi2arrcnt(JNIEnv* _janet_jnienv, 
                                       _janet_multiref** pl, _janet_multiref* pr)
{
    _janet_assign_multi2multi(_janet_jnienv, pl, pr);
    _janet_array_install(*pl);
}
*/
/*
#define _JANET_PARR_ASSIGN_MULTIREF(psrc, ptgt) \
    (!(psrc && ptgt && psrc->ref == ptgt->ref) ? \
        (_JANET_DEC_MULTIREF(psrc), \
         _JANET_ARRAY_RELEASE(psrc->arr), \
         (psrc) = (ptgt), \
         _JANET_INC_MULTIREF(psrc), \
         _JANET_ARRAY_ADDREF(psrc->arr)) : psrc)

#define _JANET_PARR_ASSIGN_NEW_MULTIREF(psrc, jtgt) \
    (_JANET_DEC_MULTIREF(psrc), \
     _JANET_ARRAY_RELEASE(psrc->arr), \
     (psrc) = _JANET_INSTALL_MULTIREF(jtgt), \
     _JANET_INC_MULTIREF(psrc), \
     psrc->arr = _JANET_ARRAY_PUT(jtgt)) : psrc)
*/
/* for &arr and #&arr */

static void* _janet_array_get_jptr(JNIEnv* _janet_jnienv,
                                   _janet_arrHashTable* _janet_arrhtable,
                                   _janet_multiref* ref,
                                   void* (*jptrfun)(JNIEnv*, _janet_arr*,
                                                    const char*, int),
                                   const char* filename, int lineno, int fastarrays)
{
    _janet_array_install(_janet_jnienv, _janet_arrhtable, ref);
    _JANET_ASSERT(ref->arr);
    if (ref->arr->jptr) return ref->arr->jptr;
    if (fastarrays) {
      return _jjp_critical_janet(_janet_jnienv, ref->arr, filename, lineno);
    }
    return (*jptrfun)(_janet_jnienv, ref->arr, filename, lineno);
}

static void* _janet_array_get_cptr(JNIEnv* _janet_jnienv,
                                   _janet_arrHashTable* _janet_arrhtable,
                                   _janet_multiref* ref,
                                   void* (*jptrfun)(JNIEnv*, _janet_arr*,
                                                    const char*, int),
                                   void* (*cnvfun)(JNIEnv*, _janet_arr*, 
                                                   const char*, int),
                                   const char* filename, int lineno, int fastarrays)
{
    _janet_array_install(_janet_jnienv, _janet_arrhtable, ref);
    _JANET_ASSERT(ref->arr);
    if (ref->arr->ptr) return ref->arr->ptr;
    _janet_array_get_jptr(_janet_jnienv, _janet_arrhtable, ref, jptrfun,
                          filename, lineno, fastarrays);
    _JANET_ASSERT(ref->arr->jptr);
    return _jcp_janet_cptr(_janet_jnienv, ref->arr, cnvfun, filename, lineno);
}

static const jchar* _janet_string_get_unicode(JNIEnv* _janet_jnienv,
					      _janet_multiref* pstr)
{
    return pstr->struni ? pstr->struni 
                        : (pstr->struni = JNI_GET_STRING_CHARS((jstring)pstr->ref, 0));
}

static const char* _janet_string_get_utf(JNIEnv* _janet_jnienv,
				         _janet_multiref* pstr)
{
    return pstr->strutf ? pstr->strutf 
                        : (pstr->strutf = JNI_GET_STRING_UTF_CHARS((jstring)pstr->ref, 0));
}


     /*
#define _JANET_ARRAY_GET_JPTR(ref, type) \
     (_JANET_ASSERT(ref->arr), \
      (ref->arr->jptr \
         ? ref->arr->jptr \
         : _jjp##type##_janet(_janet_jnienv, ref->arr, \
	                      _JANET__FILE__, _JANET__LINE__)))
     
#define _JANET_ARRAY_GET_CPTR(ref, type) \
    (_JANET_ASSERT(ref->arr), \
     (ref->arr->ptr \
        ? ref->arr->ptr \
        : _JANET_ARRAY_GET_JPTR(ref->arr, type), \
          _jcp_janet_cptr(_janet_jnienv, ref->arr, _jcp##type##_janet_arrcnv, \
                          _JANET__FILE__, _JANET__LINE__)))
     */
/* used for variable assignment; both operands are "_janet_arr"s */
/*
#define _JANET_ARRAY_ASSIGN(srche, tgthe) \
    (!(srche && tgthe && srche->ref == tgthe->ref) \
       ? (_JANET_ARRAY_RELEASE(srche), \
             (srche) = (tgthe), _JANET_ARRAY_ADDREF(srche)) \
       : srche)
*/
/* used for variable assignment when second operand is Java reference */
/*
#define _JANET_ARRAY_ASSIGN_NEW(srche, tgt) \
    _JANET_ARRAY_RELEASE(srche), \
    (srche) = _JANET_ARRAY_PUT(tgt)
*/
/* internal */

/*
#define _JANET_ARRAY_ADDREF(arrhe) \
    (arrhe ? (_JANET_ASSERT(arrhe->ref && arrhe->refcount > 0), \
              ++arrhe->refcount, arrhe) \
           : 0)

#define _JANET_ARRAY_RELEASE(arrhe) \
    (arrhe ? (_JANET_ASSERT(arrhe->refcount > 0), \
              (!--arrhe->refcount ? _jh2_janet_rmArray(_janet_jnienv, arrhe) : 0), \
               arrhe = 0) : 0)
*/     


/**
 * Exceptions
 */


/* structure for local exception info */

struct _janet_exstruct {
  /*  int isglobal; */
  volatile jthrowable catched;
  volatile int jmpmark;
  jmp_buf jmpbuf;
};

/* one global (outermost) exception structure */

/* static struct _janet_exstruct _janet_ex = { -1 }; */

/* declarations */

#define _JANET_DECLARE_LOCAL_ABRUPT_STATEMENTS_V \
    volatile jthrowable _janet_exception = 0;    \
    volatile int _janet_return_in_progress = 0   

#define _JANET_DECLARE_LOCAL_ABRUPT_STATEMENTS(type) \
    volatile jthrowable _janet_exception = 0;        \
    volatile int _janet_return_in_progress = 0;      \
    volatile type _janet_ret = (type)0


/* throwing exceptions and early returns */

#define _JANET_THROW_LOCAL(e)                                             \
   ((_janet_exception ? (JNI_DELETE_LOCAL_REF(_janet_exception), 0) : 0), \
    _janet_exception = e,                                                 \
    _JANET_LOCAL_PROPAGATE_EXCEPTION())

#define _JANET_RETURN_LOCAL(retval)                    \
   ((_janet_ret = (retval)), _janet_return_in_progress = JNI_TRUE, _JANET_LOCAL_PROPAGATE_RETURN())

#define _JANET_THROW_GLOBAL(e, ret)              \
   do {                                          \
      JNI_THROW(e);                              \
      return ret;                                \
   } while(0)

#define _JANET_THROW_GLOBAL_V(e) _JANET_GLOBAL_THROW(e, )
#define _JANET_THROW_GLOBAL_0(e) _JANET_GLOBAL_THROW(e, 0)

#define _JANET_RETURN_GLOBAL_V()                   \
   return

#define _JANET_RETURN_GLOBAL_0(ret)                   \
   return ret

/* eating exceptions */

static void _janet_eat_exception(JNIEnv* _janet_jnienv, volatile jthrowable* t) {
    if (*t) JNI_DELETE_LOCAL_REF(*t);
    *t = JNI_EXCEPTION_OCCURRED();
    _JANET_ASSERT(*t);
    JNI_EXCEPTION_CLEAR();
}

#define _JANET_EXCEPTION_EAT_AND_CHECK \
   (JNI_EXCEPTION_CHECK() \
      ? (_janet_eat_exception(_janet_jnienv, &_janet_exception), 1) \
      : (_janet_exception ? 1 : 0))


/* handling exceptions */

#define _JANET_LOCAL_HANDLE_EXCEPTION() \
   (_JANET_EXCEPTION_EAT_AND_CHECK ? _JANET_LOCAL_PROPAGATE_EXCEPTION() : (void)0)


/* auxiliary declarations */

#define _JANET_TRY_MARKER            0
#define _JANET_CATCH_MARKER          1
#define _JANET_FINALLY_MARKER        2

#define _JANET_EARLY_RETURN_MARKER   0x8000


/* propagating exceptions and early returns */

#define _JANET_LOCAL_PROPAGATE_EXCEPTION()        \
   longjmp(_janet_ex.jmpbuf, _janet_ex.jmpmark+1)

#define _JANET_LOCAL_PROPAGATE_RETURN()           \
   longjmp(_janet_ex.jmpbuf, (_janet_ex.jmpmark+1) | _JANET_EARLY_RETURN_MARKER)

#define _JANET_GLOBAL_PROPAGATE_EXCEPTION(ret)    \
   do { _JANET_ASSERT(_janet_exception);          \
        JNI_THROW(_janet_exception);              \
        return ret;                               \
   } while(0)

#define _JANET_GLOBAL_PROPAGATE_EXCEPTION_V() _JANET_GLOBAL_PROPAGATE_EXCEPTION( )
#define _JANET_GLOBAL_PROPAGATE_EXCEPTION_0() _JANET_GLOBAL_PROPAGATE_EXCEPTION(0)

#define _JANET_GLOBAL_PROPAGATE_RETURN(ret)          \
   return ret


/* and, conditional propagating exceptions and early returns */

   /*
#define _JANET_LOCAL_PROPAGATE_PENDING()                              \
   do {                                                               \
      if (_janet_exception) _JANET_LOCAL_PROPAGATE_EXCEPTION();       \
      if (_janet_return_in_progress) _JANET_LOCAL_PROPAGATE_RETURN(); \
   } while(0)

#define _JANET_GLOBAL_PROPAGATE_PENDING_(ret0, ret)                       \
   do {                                                                   \
      if (_janet_exception) _JANET_GLOBAL_PROPAGATE_EXCEPTION(ret0);      \
      if (_janet_return_in_progress) _JANET_GLOBAL_PROPAGATE_RETURN(ret); \
   } while(0)

#define _JANET_GLOBAL_PROPAGATE_PENDING_V() _JANET_GLOBAL_PROPAGATE_PENDING_( , )
#define _JANET_GLOBAL_PROPAGATE_PENDING() _JANET_GLOBAL_PROPAGATE_PENDING_(0, _janet_ret)
*/

#define _JANET_PROPAGATE_PENDING_LOCAL()                                 \
   do {                                                                  \
      if (_janet_exception) _JANET_LOCAL_PROPAGATE_EXCEPTION();          \
      if (_janet_return_in_progress) _JANET_LOCAL_PROPAGATE_RETURN();    \
   } while(0)

#define _JANET_PROPAGATE_PENDING_GLOBAL(ret, retval)                      \
   do {                                                                   \
      if (_janet_exception) _JANET_GLOBAL_PROPAGATE_EXCEPTION(retval);    \
      if (_janet_return_in_progress) _JANET_GLOBAL_PROPAGATE_RETURN(ret); \
   } while(0)

/*
#define _JANET_THROW(e) \
   do { \
      if (_janet_ex.isglobal) { \
         JNI_THROW(e); \
         return; \
      } else { \
         _JANET_LOCAL_THROW(e); \
      } \
   } while(0)
*/

/* auxiliary */

   
/* try, catch, finally */

#define _JANET_OPEN_BRACKET {
#define _JANET_CLOSE_BRACKET }


#define _JANET_EXCEPTION_CONTEXT_BEGIN \
    _JANET_OPEN_BRACKET struct _janet_exstruct _janet_ex = { 0, 0 }; 

#define _JANET_TRY                                      \
   if (!(_janet_ex.jmpmark = setjmp(_janet_ex.jmpbuf)))

#define _JANET_CATCH(excls, exobj)                            \
   else if (_janet_ex.jmpmark == _JANET_CATCH_MARKER &&       \
            (_JANET_ASSERT(_janet_exception),                 \
             JNI_IS_INSTANCE_OF(_janet_exception, excls)) &&  \
             (_JANET_ASSERT(_janet_ex.catched == 0),          \
              exobj = _janet_ex.catched = _janet_exception,   \
              _janet_exception = 0, 1))

#define _JANET_CATCH_MULTIREF(excls, exobj)                              \
   else if (_janet_ex.jmpmark == _JANET_CATCH_MARKER &&                  \
            (_JANET_ASSERT(_janet_exception),                            \
             JNI_IS_INSTANCE_OF(_janet_exception, excls)) &&             \
             (_JANET_ASSERT(_janet_ex.catched == 0),                     \
              _janet_ex.catched = _janet_exception,                      \
              _JANET_LOCV_ASSIGN_NEW_MULTIREF(exobj, _janet_ex.catched), \
              _janet_exception = 0, 1))

#define _JANET_FINALLY                              \
   if ((_janet_ex.jmpmark & ~_JANET_EARLY_RETURN_MARKER) <= _JANET_FINALLY_MARKER)

#define _JANET_DESTRUCT

#define _JANET_END_TRY                         \
   if (_janet_ex.catched) {                    \
      JNI_DELETE_LOCAL_REF(_janet_ex.catched); \
      _janet_ex.catched = 0;                   \
   }

#define _JANET_EXCEPTION_CONTEXT_END_LOCAL                   \
   _JANET_CLOSE_BRACKET _JANET_PROPAGATE_PENDING_LOCAL();

#define _JANET_EXCEPTION_CONTEXT_END_GLOBAL_0                            \
   _JANET_CLOSE_BRACKET _JANET_PROPAGATE_PENDING_GLOBAL(_janet_ret, 0);
 
#define _JANET_EXCEPTION_CONTEXT_END_GLOBAL_V                            \
   _JANET_CLOSE_BRACKET _JANET_PROPAGATE_PENDING_GLOBAL( , );





#define _JANET_NEW_EXCEPTION _je1_janet_newException



/**
 * Synchronization
 */

#define _JANET_SYNCHRONIZED(obj) \
   { \
      struct janet_exstruct _janet_ex = { 0, 0 }; \
      jobject _janetsobj = obj; \
      if (setjmp(_janet_ex.jmpbuf) == 0) { \
         JNI_MONITOR_ENTER(_janetsobj);

#define _JANET_END_SYNCHRONIZED \
      } \
      JNI_MONITOR_EXIT(_janetsobj); /* can throw exception */ \
      _JANET_EXCEPTION_EAT_AND_CHECK; \
   } \
   if (_janet_exception) _JANET_PROPAGATE;



/**
 * Verification
 */

#define _JANET_GLOBAL_ENSURE_NOT_NULL(ref, msg)         \
   do {                                                 \
       if (ref) {                                       \
	   _JANET_GLOBAL_THROW(_JANET_NEW_EXCEPTION(    \
               _janet_jnienv,                           \
               _JANET_EXC_NULL_POINTER,                 \
               _JANET__FILE__, _JANET__LINE__,          \
	       (msg)));                                 \
       }                                                \
   } while(0)

#define _JANET_LOCAL_ENSURE_ARRSIZE_NONNEGATIVE(idx, val)     \
   ((val >= 0) ? 0 : (_JANET_THROW_LOCAL(_JANET_NEW_EXCEPTION( \
                         _janet_jnienv, \
                         _JANET_EXC_NEGATIVE_ARRAY_SIZE, \
                         _JANET__FILE__, _JANET__LINE__, \
		         "%d: %d", idx, val)), 0))


#define _JANET_LOCAL_ENSURE_NOT_NULL(ref, msg) \
   ((ref) ? 0 : (_JANET_THROW_LOCAL(_JANET_NEW_EXCEPTION( \
                     _janet_jnienv, \
                     _JANET_EXC_NULL_POINTER, \
                     _JANET__FILE__, _JANET__LINE__, \
		     (msg))), 0))

#define _JANET_LOCAL_ENSURE_DIVISOR_NOT_ZERO(div) \
   ((div) ? 0 : (_JANET_THROW_LOCAL(_JANET_NEW_EXCEPTION( \
                     _janet_jnienv, \
                     _JANET_EXC_ARITHMETIC, \
                     _JANET__FILE__, _JANET__LINE__, \
		     "division by zero")), 0))


#define _JANET_GUARDED_LOCAL_CALL(fun) \
   (_JANET_ASSERT(!_janet_exception), \
    (_janet_exception = (fun)) ? (_JANET_LOCAL_PROPAGATE_EXCEPTION(), 0) : 0)
    



/*
#define _JANET_PUT_REF(ref) \
*/    


#define _JANET_PUT_ARRAY(jarr) \
    _jh1_janet_putArray(_janet_jnienv, _janet_arrays, jarr)

#define _JANET_RM_ARRAY(varr) \
    (_jh2_janet_rmArray(varr), varr = 0)

/*
#define _JANET_ASSIGN_ARRAY(varr, jarr) \
   ((varr ? _JANET_RM_ARRAY(varr) : 0), (varr = _JANET_PUT_ARRAY(jarr))
*/










static jthrowable _janet_array_check_bounds(JNIEnv* _janet_jnienv, 
					    jint idx, jint arrlength,
					    const char* filename, unsigned int lineno)
{
    if (idx >= 0 && idx < arrlength) return 0;
    /* idx is out of bounds */
    return _JANET_NEW_EXCEPTION(_janet_jnienv, _JANET_EXC_ARRAY_INDEX_OUT_OF_BOUNDS,
				filename, lineno,
				"Array index out of range: %d", idx);
}

static jthrowable _janet_cast_rtcheck(JNIEnv* _janet_jnienv, 
				      jobject obj, _janet_cls* cls,
				      const char* filename, unsigned int lineno)
{
    if (JNI_IS_INSTANCE_OF(obj, cls->id)) return 0;
    return _JANET_NEW_EXCEPTION(_janet_jnienv, _JANET_EXC_CLASS_CAST,
				filename, lineno,
				"Object is not of type %.128s", cls->name);
}

/* returns false for null */
static jboolean _janet_is_instance_of(JNIEnv* _janet_jnienv,
				      jobject obj, jclass cls)
{
    return obj && JNI_IS_INSTANCE_OF(obj, cls);
}

#ifdef __cplusplus
} // extern "C"
#endif
