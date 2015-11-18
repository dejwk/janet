#include <janet_base.h>
#include <string.h>
#include <stdlib.h>
#include <stdarg.h>

#define ENV     _janet_jnienv

#define CLASSES _jc_janet_classes
#define FIELDS  _jf_janet_fields
#define METHODS _jm_janet_methods

#define ABS(x) ((x) >= 0 ? (x) : (-x))

#define CLSSIZE 17
_janet_cls CLASSES[] = {
    { 0, 0, "java/lang/Object" },
    { 0, 0, "java/lang/Throwable" },
    { 0, 0, "java/lang/UnknownError" },
    { 0, 0, "java/lang/InternalError" },
    { 0, 0, "java/lang/OutOfMemoryError" },
    { 0, 0, "java/lang/NoClassDefFoundError" },
    { 0, 0, "java/lang/ClassFormatError" },
    { 0, 0, "java/lang/ClassCircularityError" },
    { 0, 0, "java/lang/ExceptionInInitializerError" },
    { 0, 0, "java/lang/NoSuchFieldError" },
    { 0, 0, "java/lang/NoSuchMethodError" },
    { 0, 0, "java/lang/NullPointerException" },
    { 0, 0, "java/lang/ArrayIndexOutOfBoundsException" },
    { 0, 0, "java/lang/NegativeArraySizeException" },
    { 0, 0, "java/lang/ArithmeticException" },
    { 0, 0, "java/lang/ClassCastException" },
    { 0, 0, "java/lang/String" },
};
#define CLASS(n) (&CLASSES[n])

#define FLDSIZE 0
#undef FIELDS
#define FIELDS ((void*)0) /* no fields used */

#define MTHSIZE 5
_janet_mth METHODS[] = {
    { 0, CLASS(0), 0, "hashCode", "()I" },
    { 0, CLASS(1), 0, "getMessage", "()Ljava/lang/String;" },
    { 0, CLASS(4), 0, "<init>", "(Ljava/lang/String;)V" },
    { 0, CLASS(11), 0, "<init>", "(Ljava/lang/String;)V" },
    { 0, CLASS(16), 0, "intern", "()Ljava/lang/String;" },
};

#define METHOD(n) (&METHODS[n])

static jobject outOfMemoryError;

#ifndef NDEBUG
jboolean _janet_assertionFailed = JNI_FALSE;
#endif

/* Hashing */

static jint radkeNumbers[] = {
    0x00000003, 0x00000003, 0x00000007, 0x0000000B,
    0x00000013, 0x0000002B, 0x00000043, 0x0000008B,
    0x00000107, 0x0000020B, 0x00000407, 0x0000080F,
    0x00001003, 0x0000201B, 0x0000401B, 0x0000800B,
    0x00010003, 0x00020027, 0x00040003, 0x0008003B,
    0x00100007, 0x0020003B, 0x0040000F, 0x0080000B,
    0x0100002B, 0x02000023, 0x0400000F, 0x08000033,
    0x10000003, 0x2000000B, 0x40000003, 0
};

/* Hashing Java references */
/*
typedef struct _janet_multirefHashTable_struct {
    jint size;
    jint fill;
    jint treshold;
    _janet_multiref2ref *data;
} _janet_multirefHashTable;

#define HT_REF _janet_multirefHashTable

#define HASH_REF(ref) ((jint)(void*)(ref))
*/
/* Hashing Java arrays */

#define HT_ARR _janet_arrHashTable

/**
 * Functions
 */

static int createErrorObj(JNIEnv *);
static void releaseErrorObj(JNIEnv *);


/**
 * Class loading
 */

static int refcount = 0;

int _j1_janet_init(JNIEnv* ENV) {
    do {
	if (refcount++) return 1;
	if (!(_j6_janet_loadClasses(ENV, CLASSES, CLSSIZE,
				    _JANET__FILE__, _JANET__LINE__))) break;
	if (!(_j7_janet_loadMembers(ENV, FIELDS, FLDSIZE, METHODS, MTHSIZE, ((void*)0), 0,
				    _JANET__FILE__, _JANET__LINE__))) break;
	if (!(createErrorObj(ENV))) break;
	return 1;
    } while(0);
    
    _JANET_ASSERT(JNI_EXCEPTION_CHECK());
    return 1;
}

void _j2_janet_finalize(JNIEnv *ENV) {
    if (--refcount) return;
    releaseErrorObj(ENV);
    _j8_janet_releaseClasses(ENV, CLASSES, CLSSIZE);
}

int _j3_janet_loadClass(JNIEnv *ENV, _janet_cls* cls,
			const char* filename, unsigned int lineno) {
    jclass auxcls = JNI_FIND_CLASS(cls->name);
    if (!auxcls) {
	const char* errdesc = "can't be load - unknown error";
	jthrowable e = JNI_EXCEPTION_OCCURRED();
	if (e) {
	    if (_JANET_ERR_CLASS_FORMAT  &&
		JNI_IS_INSTANCE_OF(e, _JANET_ERR_CLASS_FORMAT)) {
		errdesc = "has not been recognized by the Java VM as a "
		    "valid Java class";
	    } else if (_JANET_ERR_CLASS_CIRCULARITY && 
		       JNI_IS_INSTANCE_OF(e, _JANET_ERR_CLASS_CIRCULARITY)) {
		errdesc = "is its own superclass";
	    } else if (_JANET_ERR_NO_CLASS_DEF_FOUND &&
		       JNI_IS_INSTANCE_OF(e, _JANET_ERR_NO_CLASS_DEF_FOUND)) {
		errdesc = "cannot be found by the Java VM";
	    } else if (_JANET_ERR_OUT_OF_MEMORY &&
		       JNI_IS_INSTANCE_OF(e, _JANET_ERR_OUT_OF_MEMORY)) {
		errdesc = "cannot be loaded as the Java VM ran out of memory";
	    }
	}
	_je_janet_throw(ENV, 0,
			filename, lineno, "class %.128s %.128s",
			cls->name, errdesc);
	return 0;
    }
    cls->id = cls->weak ? JNI_NEW_WEAK_GLOBAL_REF(auxcls)
	                : JNI_NEW_GLOBAL_REF(auxcls);
    if (!cls->id) {
	_je_janet_throw(ENV, 0, filename, lineno,
			"unable to make global reference to class %.128s",
			cls->name);
	return 0;
    }
    return 1;
}

int _j4_janet_loadField(JNIEnv* ENV, _janet_fld* fld,
			const char* filename, unsigned int lineno) {
    if (!fld->cls->id) {
	if (!_j3_janet_loadClass(_janet_jnienv, fld->cls,
				 filename, lineno)) return 0;
    }
    if (fld->is_static) {
	fld->id = JNI_GET_STATIC_FIELD_ID(fld->cls->id, fld->name, fld->signature);
    } else {
	fld->id = JNI_GET_FIELD_ID(fld->cls->id, fld->name, fld->signature);
    }
    if (!fld->id) {
	const char* errdesc = "unknown error";
	jthrowable e = JNI_EXCEPTION_OCCURRED();
	if (e) {
	    if (_JANET_ERR_NO_SUCH_FIELD &&
		JNI_IS_INSTANCE_OF(e, _JANET_ERR_NO_SUCH_FIELD)) {
		errdesc = "field can't be found";
	    } else if (_JANET_ERR_EXCEPTION_IN_INITIALIZER &&
		       JNI_IS_INSTANCE_OF(e, _JANET_ERR_EXCEPTION_IN_INITIALIZER)) {
		errdesc = "exception occured during initialization of the class";
	    } else if (_JANET_ERR_OUT_OF_MEMORY &&
		       JNI_IS_INSTANCE_OF(e, _JANET_ERR_OUT_OF_MEMORY)) {
		errdesc = "out of memory";
	    }
	}
	_je_janet_throw(ENV, 0,
			filename, lineno, "cannnot link field %.64s ("
			"%.64s) in class %.128s: %.128s", fld->name,
			fld->signature, fld->cls->name, errdesc);
	return 0;
    }
    return 1;
}

int _j5_janet_loadMethod(JNIEnv* ENV, _janet_mth* mth,
			 const char* filename, unsigned int lineno)
{
    if (!mth->cls->id) {
	if (!_j3_janet_loadClass(_janet_jnienv, mth->cls,
				 filename, lineno)) return 0;
    }
    if (mth->is_static) {
	mth->id = JNI_GET_STATIC_METHOD_ID(mth->cls->id, mth->name, mth->signature);
    } else {
	mth->id = JNI_GET_METHOD_ID(mth->cls->id, mth->name, mth->signature);
    }
    if (!mth->id) {
	const char* errdesc = "unknown error";
	jthrowable e = JNI_EXCEPTION_OCCURRED();
	if (e) {
	    if (_JANET_ERR_NO_SUCH_METHOD &&
		JNI_IS_INSTANCE_OF(e, _JANET_ERR_NO_SUCH_METHOD)) {
		errdesc = "method can't be found";
	    } else if (_JANET_ERR_EXCEPTION_IN_INITIALIZER &&
		       JNI_IS_INSTANCE_OF(e, _JANET_ERR_EXCEPTION_IN_INITIALIZER)) {
		errdesc = "exception occured during initialization of the class";
	    } else if (_JANET_ERR_OUT_OF_MEMORY &&
		       JNI_IS_INSTANCE_OF(e, _JANET_ERR_OUT_OF_MEMORY)) {
		errdesc = "out of memory";
	    }
	}
	_je_janet_throw(ENV, 0,
			filename, lineno, "cannnot link method %.64s"
			"%.64s in class %.128s: %.128s", mth->name,
			mth->signature, mth->cls->name, errdesc);
	return 0;
    }
    return 1;
}

int _j9_janet_loadString(JNIEnv* ENV, _janet_str* str,
			 const char* filename, unsigned int lineno)
{
    jstring s1, s2;
    do {
        if (!(s1 = JNI_NEW_STRING_UTF(str->utf))) break;
        if (!(s2 = (*ENV)->CallNonvirtualObjectMethod(ENV, s1, _JANET_CLS_STRING, 
						      _JANET_MTH_STRING_INTERN))) break;
        if (!(str->strref = JNI_NEW_GLOBAL_REF(s2))) break;
	JNI_DELETE_LOCAL_REF(s1);
	JNI_DELETE_LOCAL_REF(s2);
        return 1;
    } while(0);
    _je_janet_throw(ENV, 0,
		    filename, lineno, "cannnot link string literal \""
		    "%.256s\"", str->utf);
    return 0;
}    


int _j6_janet_loadClasses(JNIEnv *ENV,
			  _janet_cls* classes, int size,
			  const char* filename, unsigned int lineno) {
  while (size--) {
    if (!_j3_janet_loadClass(ENV, classes++, 
			     filename, lineno)) return 0;
  }
  return 1;
}

int _j7_janet_loadMembers(JNIEnv *ENV,
			  _janet_fld* fields, int fldsize,
			  _janet_mth* methods, int mthsize,
			  _janet_str* strings, int strsize,
			  const char* filename, unsigned int lineno) {
    while (fldsize--) {
	if (!_j4_janet_loadField(ENV, fields++, filename, lineno)) return 0;
    }
    while (mthsize--) {
	if (!_j5_janet_loadMethod(ENV, methods++, filename, lineno)) return 0;
    }
    while (strsize--) {
        if (!_j9_janet_loadString(ENV, strings++, filename, lineno)) return 0;
    }
    return 1;
}
			   
void _j8_janet_releaseClasses(JNIEnv *ENV,
			      _janet_cls* classes, int size) {
    while (size-- > 0) {
	if (classes->id) {
	    if (classes->weak) {
		JNI_DELETE_WEAK_GLOBAL_REF(classes->id);
	    } else {
		JNI_DELETE_GLOBAL_REF(classes->id);
	    }
	    classes->id = 0;
	}
	classes++;
    }
}

/**
 * Throwing exceptions
 */

static int buildErrDesc(JNIEnv *ENV, 
			char* buf,
			const char* filename, unsigned int lineno,
			const char* msg, va_list va) {
    char* ptr = buf;

    ptr += sprintf(ptr, "\nJanet: ");
    
    if (filename) {
	ptr += sprintf(ptr, "%s:%u: ", filename, lineno);
    }
    
    ptr += vsprintf(ptr, msg, va);
    /*ptr += sprintf(ptr, "\n");*/

    return ptr - buf;
}

static void throw(JNIEnv *ENV, jclass excls,
		  const char* filename, unsigned int lineno,
		  const char* msg, va_list va) {
    char buf[1024];

    char* ptr = buf;
    jthrowable e;
    jmethodID errmth;
    int exclslocal = 0;

    ptr += buildErrDesc(ENV, buf, filename, lineno, msg, va);

    e = JNI_EXCEPTION_OCCURRED();
    if (e) {
        jstring s = 0;
	JNI_EXCEPTION_CLEAR();
	if (!excls) {
	    excls = JNI_GET_OBJECT_CLASS(e);
	    exclslocal = 1;
	}
	
	errmth = _JANET_MTH_THROWABLE_GET_MESSAGE
	    ? _JANET_MTH_THROWABLE_GET_MESSAGE 
	    : JNI_GET_METHOD_ID(excls, "getMessage", "()Ljava/lang/String;");

	if (errmth) s = (*ENV)->CallObjectMethod(ENV, e, errmth);
	if (s) {
	    const char* msg = JNI_GET_STRING_UTF_CHARS(s, 0);
	    if (msg) {
		ptr += sprintf(ptr, "\nOriginal message: %.256s", msg);
		JNI_RELEASE_STRING_UTF_CHARS(s, msg);
	    }
	    JNI_DELETE_LOCAL_REF(s);
	}
	JNI_DELETE_LOCAL_REF(e);
    }
    
    if (!excls) excls = _JANET_ERR_UNKNOWN;
    
    if (excls) {
	int result = JNI_THROW_NEW(excls, buf);
	if (exclslocal) JNI_DELETE_LOCAL_REF(excls);
	if (result >= 0) return;
    }
    if (outOfMemoryError && JNI_THROW(outOfMemoryError) >= 0) return;
    
    JNI_FATAL_ERROR(buf);
}


void _je_janet_throw(JNIEnv *ENV, jclass excls,
		     const char* filename, unsigned int lineno,
		     const char* msg, ...) {
    va_list va;
    
    va_start(va, msg);
    throw(ENV, excls, filename, lineno, msg, va);
    va_end(va);
}

jthrowable _je1_janet_newException(JNIEnv* ENV, jclass excls,
				   const char* filename, unsigned int lineno,
				   const char* msg, ...) {
    va_list va;
    jthrowable t;

    va_start(va, msg);

   /*
    * throwing and catching the exception.
    * it is not the most efficient solution,
    * but throwing exceptions do not have to be efficient.
    * Below is simple and guarantees that exception is always returned
    * as a local (not global) reference and may be safely
    * disposed using DeleteLocalRef 
    */

    throw(ENV, excls, filename, lineno, msg, va);
    va_end(va);

    t = JNI_EXCEPTION_OCCURRED();
    _JANET_ASSERT(t);
    JNI_EXCEPTION_CLEAR();
    return t;
}

#ifndef NDEBUG
int _ja_janet_assert(JNIEnv* _janet_jnienv, const char* condition, 
		     const char* filename, unsigned int lineno) {
  char buf[1024];
  sprintf(buf, "%.64s:%d:Janet assertion failed: %.128s\n", filename, lineno, condition);
  fflush(0);
  /* _je_janet_throw(ENV, _JANET_ERR_INTERNAL, filename, lineno, 
		  "assertion failed: %.128s", contition); */
  _janet_assertionFailed = JNI_TRUE;
  JNI_FATAL_ERROR(buf);
  return 1;
}
#endif

/**
 * Hashing Java references
 */
#if 0
_janet_ref2ref* _jrh1_janet_putRef(JNIEnv* ENV, 
				   HT_REF* htab, jobject newref) {
    jint hkey;
    jint htab_size;
    jint j;
    _janet_ref2ref* firstempty = 0;
    _janet_ref2ref* ref = 0;

    hkey = HASH_REF(ref) % htab->size;
    j = -htab->size;

    while ((ref = &htab->data[hkey])->ref) { /* while not empty */
	if (ref->ref == newref) { /* found */
	    ref->refcount++;
	    return ref;
	}
	if (!firstempty && ref->refcount < 0) {
	    firstempty = ref; /* empty place */
	}
	j += 2;
	_JANET_ASSERT(j<htab_size); /* as always treshold < htab_size */
	hkey += ABS(j);
	if (hkey > htab_size) hkey -= htab_size;
    }
    
    /* not found -> must be added to hashtable */
    _JANET_ASSERT(htab->fill < htab->treshold);
    if (!firstempty) firstempty = ref;

    htab->fill++;
    firstempty->ref = newref;
    firstempty->refcount = 1;
    return firstempty;
}

_janet_ref2ref* _jrh2_janet_rmRef(_janet_ref2ref* ref) {
    
}
#endif

/**
 * Hashing Java arrays
 */

#define HTSIZE(htab) radkeNumbers[htab->sizeidx]
  
_janet_arr* _jh1_janet_putArray(JNIEnv* ENV, 
				HT_ARR* htab, jarray ref) {
    jint hashcode;
    unsigned int hkey;
    int htab_size;
    jint j;
    _janet_arr* firstempty = 0;
    _janet_arr* arr;

    if (!ref) return 0;

    hashcode = (*ENV)->CallIntMethod(ENV, ref, _JANET_MTH_OBJECT_HASH_CODE);
    /* find the array in hashtables */
    for (; htab; htab = htab->next) {
	htab_size = HTSIZE(htab);
	hkey = (unsigned int)hashcode % htab_size;
	j = -htab_size;

	while ((arr = &htab->data[hkey])->ref) { /* while not empty */

	    if (!firstempty && arr->refcount < 0 && htab->fill < htab->treshold) {
		_JANET_ASSERT(!arr->jptr && !arr->ptr);
		firstempty = arr; /* empty place */
		firstempty->htab = htab;
	    } else if (arr->hashcode == hashcode) { /* possibly found */
		if (JNI_IS_SAME_OBJECT(arr->ref, ref)) { /* indeed found */
		    arr->refcount++;
		    return arr;
		}
	    }
	    j += 2;
	    _JANET_ASSERT(j<htab_size); /* as always treshold < htab_size */
	    hkey += ABS(j);
	    if (hkey > htab_size) hkey -= htab_size;
	}
    
	/* empty cell found -> must search in subsequent hashtables */
	if (!firstempty && htab->fill < htab->treshold) {
	    _JANET_ASSERT(!arr->jptr && !arr->ptr);
	    firstempty = arr;
	    firstempty->htab = htab;
	}
    }

    /* array not found in hashtables -> new place is required */

    if (!firstempty) {
	jint newsize;
	_janet_arrHashTable* new_htab;
	_janet_arr* new_data;

	/* all hashtables are filled - we must allocate next one */

	if (!(newsize = radkeNumbers[htab->sizeidx+1])) return 0;

	if (!(new_htab = (HT_ARR*)malloc(sizeof(HT_ARR)))) return 0;
	
	if (!(new_data = (_janet_arr*)calloc(newsize, sizeof(_janet_arr*)))) {
	    free(new_htab);
	    return 0;
	}

	new_htab->sizeidx = htab->sizeidx+1;
	new_htab->fill = 0;
	new_htab->treshold = (jint)(((float)newsize) * 0.75);
	new_htab->next = 0;
	new_htab->data = new_data;
	new_htab->dynamic = 1;

	firstempty = &new_htab->data[hashcode % newsize];
	firstempty->htab = new_htab;

	htab->next = new_htab;
	htab = new_htab;
    } else {
        htab = firstempty->htab;
    }

    htab->fill++;
    firstempty->ref = ref;
    firstempty->hashcode = hashcode;
    firstempty->refcount = 1;
    firstempty->jptr = 0;
    firstempty->ptr = 0;
    firstempty->length = -1;
    return firstempty;
}

void _jh2_janet_rmArray(JNIEnv* ENV, _janet_arr* arr) {
    _JANET_ASSERT(!arr->refcount);
    if (arr->ptr) {
        if (arr->ptr != arr->jptr) free(arr->ptr);
	arr->ptr = 0;
    }
    if (arr->jptr) {
	arr->releasef(ENV, arr->ref, arr->jptr, 0);
	arr->jptr = 0;
    }
    arr->releasef = 0;
    arr->htab->fill--;
    arr->refcount = -1; /* mark as zombie */
    arr->length = -1;
}

void _jh3_releaseHashTable(JNIEnv* ENV, HT_ARR* htab) {
    HT_ARR* htab_next;
    int i;
    _janet_arr* data;
    do {
        data = htab->data;
        for(i=HTSIZE(htab); i; --i) {
            if (data->ref) {
                data->refcount = 0;
                _jh2_janet_rmArray(_janet_jnienv, data);
	    }
	    data++;
        }
	htab_next = htab->next;
	if (htab->dynamic) free(htab);
	htab = htab_next;
    } while(htab);
}

void _jm1_releaseMonitors(JNIEnv* ENV, jobject* monitors, int size) {
    while (size--) {
        if (*monitors) {
            JNI_MONITOR_EXIT(*monitors);
            *monitors = 0;
        }
        monitors++;
    }
}

/* static const char* errcnt1 = "null array reference"; */
static const char* errcnt2 = "failed to get contents of an array";

_JANET_IMPL_JPTR(Z)
_JANET_IMPL_JPTR(B)
_JANET_IMPL_JPTR(C)
_JANET_IMPL_JPTR(S)
_JANET_IMPL_JPTR(I)
_JANET_IMPL_JPTR(J)
_JANET_IMPL_JPTR(F)
_JANET_IMPL_JPTR(D)

void* _jjp_critical_janet(JNIEnv* ENV, _janet_arr* ref, 
			  const char* filename, int lineno)
{
    _JANET_ASSERT(ref); _JANET_ASSERT(ref->ref); _JANET_ASSERT(!ref->jptr && !ref->ptr);
    ref->jptr = JNI_GET_PRIMITIVE_ARRAY_CRITICAL(ref->ref, 0);
    if (!ref->jptr) {
	_je_janet_throw(ENV, _JANET_ERR_OUT_OF_MEMORY, filename, lineno, errcnt2);
	return 0;
    }
    ref->releasef = (*ENV)->ReleasePrimitiveArrayCritical;
    return ref->jptr;
}


void* _jcp_janet_cptr(JNIEnv* ENV, _janet_arr* ref,
		      void* (*cfn)(JNIEnv*, _janet_arr*, const char*, int),
		      const char* filename, int lineno) {
    if (!ref) {
	_je_janet_throw(ENV, _JANET_EXC_NULL_POINTER,
			filename, lineno, "null array reference");
	return 0;
    }
    _JANET_ASSERT(ref->ref), _JANET_ASSERT(ref->jptr), _JANET_ASSERT(!ref->ptr);
    return ref->ptr = (*cfn)(ENV, ref, filename, lineno);
}

typedef jarray (JNICALL *jniNewPrimArrF)(JNIEnv*, jsize);

static jobject new_array(JNIEnv*, int, int, jint[], jclass[], jniNewPrimArrF,
			 const char *, int);

/* By JVM spec 4.4.1 */
#define MAX_ARRAY_DEPTH 255

jobject _ja_janet_new_array(JNIEnv* ENV, int depth, 
			    const char *filename, int lineno,
			    ...) {
    va_list va;
    int i;
    jint lengths[MAX_ARRAY_DEPTH];
    jclass types[MAX_ARRAY_DEPTH];
    jniNewPrimArrF basef = 0;
    
    /* copy parameters from vararg to regular arrays to enable recursion */
    va_start(va, lineno);
    for (i=0; i<depth; i++) {
        lengths[i] = va_arg(va, jint);
	types[i] = va_arg(va, jclass);
    }
    if (!types[depth-1]) { /* primitive type array */
        basef = va_arg(va, jniNewPrimArrF);
    }
    va_end(va);

    return new_array(ENV, 0, depth, lengths, types, basef, filename, lineno);
}

static jobject new_array(JNIEnv* ENV, int level, int depth,
			 jint lengths[], jclass types[], jniNewPrimArrF basef,
			 const char *filename, int lineno)
{
  jarray arr;
  jboolean failure = JNI_FALSE;

  _JANET_ASSERT(level >= 0 && level < depth);

  if (types[level]) {
      arr = JNI_NEW_OBJECT_ARRAY((jsize)lengths[level], types[level], 0);
  } else { /* primitive type, use basef */
      arr = basef(ENV, lengths[level]);
  }

  if (arr) {
    if (level+1 < depth) { /* create subarrays */
      int i;
      jarray subarray;
      for (i=0; i<lengths[level]; i++) {
	subarray = new_array(ENV, level+1, depth, lengths, types, basef,
			     filename, lineno);
	if (subarray) {
	  JNI_SET_OBJECT_ARRAY_ELEMENT(arr, i, subarray);
	  _JANET_ASSERT(!JNI_EXCEPTION_CHECK());
	  JNI_DELETE_LOCAL_REF(subarray);
	  subarray = 0;
	} else {
	  failure = JNI_TRUE;
	  break; /* exception thrown from subarray code */
	}
      }
    }
  } else {
    failure = JNI_TRUE;
  }

  if (failure) {
    if (arr) {
      JNI_DELETE_LOCAL_REF(arr);
      arr = 0;
    }
    /* throw an exception, if not already thrown */
    if (!JNI_EXCEPTION_CHECK()) {
      _je_janet_throw(ENV, _JANET_ERR_OUT_OF_MEMORY, filename, lineno,
		      "cannot create an array");
    }
    return 0;
  }

  return arr;
}











/**
 * Inner
 */
 
int createErrorObj(JNIEnv *ENV) {
    do {
	jstring descr;
	jobject err;
	
	if (!(descr = JNI_NEW_STRING_UTF("unable to create "
					 "exception object"))) break;
	if (!(err = (*ENV)->NewObject(ENV, _JANET_ERR_OUT_OF_MEMORY,
				      _JANET_INIT_OUT_OF_MEMORY, descr))) break;
	if (!(outOfMemoryError = JNI_NEW_GLOBAL_REF(err))) break;
	
	return 1;
	
    } while (0);

    return 0;
}

void releaseErrorObj(JNIEnv *ENV) {
    if (outOfMemoryError) {
	JNI_DELETE_GLOBAL_REF(outOfMemoryError);
    }
}

/**
 * Default array conversion functions
 */

/* boolean to uchar*/
void* _jcpZ_janet_arrcnv(JNIEnv* ENV,
        _janet_arr* ref, const char* filename, int lineno) {
    _JANET_ASSERT(ref && ref->jptr);
    if (sizeof(jboolean) == sizeof(unsigned char)) {
	return ref->ptr = ref->jptr;
    }
    _JANET_ARRAY_COPY(ref, jboolean, unsigned char);
    return ref->ptr;
}

/* byte to schar */
void* _jcpB_janet_arrcnv(JNIEnv* ENV,
        _janet_arr* ref, const char* filename, int lineno) {
    _JANET_ASSERT(ref && ref->jptr);
    if (sizeof(jbyte) == sizeof(signed char)) {
	return ref->ptr = ref->jptr;
    }
    _JANET_ARRAY_COPY(ref, jbyte, signed char);
    return ref->ptr;
}

/* char to ushort */
void* _jcpC_janet_arrcnv(JNIEnv* ENV,
        _janet_arr* ref, const char* filename, int lineno) {
    _JANET_ASSERT(ref && ref->jptr);
    if (sizeof(jchar) == sizeof(unsigned short)) {
	return ref->ptr = ref->jptr;
    }
    _JANET_ARRAY_COPY(ref, jchar, unsigned short);
    return ref->ptr;
}

/* short to short */
void* _jcpS_janet_arrcnv(JNIEnv* ENV,
        _janet_arr* ref, const char* filename, int lineno) {
    _JANET_ASSERT(ref && ref->jptr);
    if (sizeof(jshort) == sizeof(short)) {
	return ref->ptr = ref->jptr;
    }
    _JANET_ARRAY_COPY(ref, jshort, short);
    return ref->ptr;
}

/* int to int */
void* _jcpI_janet_arrcnv(JNIEnv* ENV,
        _janet_arr* ref, const char* filename, int lineno) {
    _JANET_ASSERT(ref && ref->jptr);
    if (sizeof(jint) == sizeof(int)) {
	return ref->ptr = ref->jptr;
    }
    _JANET_ARRAY_COPY(ref, jint, int);
    return ref->ptr;
}

/* long to long */
void* _jcpJ_janet_arrcnv(JNIEnv* ENV,
        _janet_arr* ref, const char* filename, int lineno) {
    _JANET_ASSERT(ref && ref->jptr);
    if (sizeof(jlong) == sizeof(long)) {
	return ref->ptr = ref->jptr;
    }
    _JANET_ARRAY_COPY(ref, jlong, long);
    return ref->ptr;
}

/* float to float */
void* _jcpF_janet_arrcnv(JNIEnv* ENV,
        _janet_arr* ref, const char* filename, int lineno) {
    union { jfloat a; float b; } u;
    _JANET_ASSERT(ref && ref->jptr);
    if (sizeof(jfloat) == sizeof(float) &&
            (u.a = ((jfloat)1.0) / ((jfloat)17.0), u.a == (jfloat)u.b) &&
            (u.b = ((float)1.0) / ((float)17.0), u.b == (float)u.a)) {
	return ref->ptr = ref->jptr;
    }
    _JANET_ARRAY_COPY(ref, jfloat, float);
    return ref->ptr;
}

/* double to double */
void* _jcpD_janet_arrcnv(JNIEnv* ENV,
        _janet_arr* ref, const char* filename, int lineno) {
    union { jdouble a; double b; } u;
    _JANET_ASSERT(ref && ref->jptr);
    if (sizeof(jdouble) == sizeof(double) &&
            (u.a = ((jdouble)1.0) / ((jdouble)17.0), u.a == (jdouble)u.b) &&
            (u.b = ((double)1.0) / ((double)17.0), u.b == (double)u.a)) {
	return ref->ptr = ref->jptr;
    }
    _JANET_ARRAY_COPY(ref, jdouble, double);
    return ref->ptr;
}

/*

static char* dump(char *buf, char *bufend, const char *name, const char *signature) {
    if (*signature == '(') { method
	signature++;
	const char* endparams = strchr(signature, ')');
	if (buf==bufend) return buf;
	*buf++ = ' ';
	while (*name && (buf < bufend)) *buf++ = *name++;
	if (buf==bufend) return buf;
	*buf++ = ' ';
	if (buf==bufend) return buf;
	*buf++ = '(';
	if (buf==bufend) return buf;
	while (*signature != ')' && buf!=bufend) {
	    buf = dump(buf, bufend, 0, signature
	
	buf = dump(buf, bufend, 0, endparams+1);
	if (bufend > buf) strncpy(buf, " ", bufend-buf);
	strncat(buf, 
	
	
}
*/
