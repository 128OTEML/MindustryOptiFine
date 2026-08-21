#include <arc_backend_sdl_jni_SDLGL.h>

//@line:10


    #define GLEW_STATIC

    #include "GL/glew.h"

    //copied from ios openGL source, I have no idea what I'm doing

    static jclass bufferClass;
    static jclass byteBufferClass;
    static jclass charBufferClass;
    static jclass shortBufferClass;
    static jclass intBufferClass;
    static jclass longBufferClass;
    static jclass floatBufferClass;
    static jclass doubleBufferClass;
    static jclass OOMEClass;
    static jclass UOEClass;
    static jclass IAEClass;

    static jmethodID positionID;


    static void nativeClassInitBuffer(JNIEnv *_env){
        jclass bufferClassLocal = _env->FindClass("java/nio/Buffer");
        bufferClass = (jclass) _env->NewGlobalRef(bufferClassLocal);

        byteBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/ByteBuffer"));
        charBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/CharBuffer"));
        shortBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/ShortBuffer"));
        intBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/IntBuffer"));
        longBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/LongBuffer"));
        floatBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/FloatBuffer"));
        doubleBufferClass = (jclass) _env->NewGlobalRef(_env->FindClass("java/nio/DoubleBuffer"));

        positionID = _env->GetMethodID(bufferClass, "position","()I");
        if(positionID == 0) _env->ThrowNew(IAEClass, "Couldn't fetch position() method");
    }

    static void nativeClassInit(JNIEnv *_env){
        nativeClassInitBuffer(_env);

        jclass IAEClassLocal =
            _env->FindClass("java/lang/IllegalArgumentException");
        jclass OOMEClassLocal =
             _env->FindClass("java/lang/OutOfMemoryError");
        jclass UOEClassLocal =
             _env->FindClass("java/lang/UnsupportedOperationException");

        IAEClass = (jclass) _env->NewGlobalRef(IAEClassLocal);
        OOMEClass = (jclass) _env->NewGlobalRef(OOMEClassLocal);
        UOEClass = (jclass) _env->NewGlobalRef(UOEClassLocal);
    }

    static jint getElementSizeShift(JNIEnv *_env, jobject buffer) {
        if(_env->IsInstanceOf(buffer, byteBufferClass)) return 0;
        if(_env->IsInstanceOf(buffer, floatBufferClass)) return 2;
        if(_env->IsInstanceOf(buffer, shortBufferClass)) return 1;

        if(_env->IsInstanceOf(buffer, charBufferClass)) return 1;
        if(_env->IsInstanceOf(buffer, intBufferClass)) return 2;
        if(_env->IsInstanceOf(buffer, longBufferClass)) return 3;
        if(_env->IsInstanceOf(buffer, doubleBufferClass)) return 3;

        _env->ThrowNew(IAEClass, "buffer type unkown! (Not a ByteBuffer, ShortBuffer, etc.)");
        return 0;
    }

    inline jint getBufferPosition(JNIEnv *env, jobject buffer){
        jint ret = env->CallIntMethodA(buffer, positionID, 0);
        return  ret;
    }

    static void *getDirectBufferPointer(JNIEnv *_env, jobject buffer) {
        if (!buffer) {
            return NULL;
        }
        void* buf = _env->GetDirectBufferAddress(buffer);
        if (buf) {
            jint position = getBufferPosition(_env, buffer);
            jint elementSizeShift = getElementSizeShift(_env, buffer);
            buf = ((char*) buf) + (position << elementSizeShift);
        } else {
            _env->ThrowNew(IAEClass, "Must use a native order direct Buffer");
        }
        return buf;
    }

    JNIEXPORT jstring JNICALL Java_arc_backend_sdl_jni_SDLGL_init(JNIEnv* env, jclass clazz) {


//@line:101

        nativeClassInit(env);

        GLenum glewError = glewInit();
        if(glewError != GLEW_OK){
            return env->NewStringUTF((const char*)glewGetErrorString(glewError));
        }

        if(glGenFramebuffers != 0 || glGenFramebuffersEXT != 0){
            //no error message
            return NULL;
        }else{
            return env->NewStringUTF("Missing framebuffer_object extension.");
        }
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glActiveTexture(JNIEnv* env, jclass clazz, jint texture) {


//@line:120

        glActiveTexture(texture);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBindTexture(JNIEnv* env, jclass clazz, jint target, jint texture) {


//@line:124

        glBindTexture(target, texture);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBlendFunc(JNIEnv* env, jclass clazz, jint sfactor, jint dfactor) {


//@line:128

        glBlendFunc(sfactor, dfactor);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glClear(JNIEnv* env, jclass clazz, jint mask) {


//@line:132

        glClear(mask);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glClearColor(JNIEnv* env, jclass clazz, jfloat red, jfloat green, jfloat blue, jfloat alpha) {


//@line:136

        glClearColor(red, green, blue, alpha);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glClearDepthf(JNIEnv* env, jclass clazz, jfloat depth) {


//@line:141

        glClearDepthf(depth);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glClearStencil(JNIEnv* env, jclass clazz, jint s) {


//@line:145

        glClearStencil(s);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glColorMask(JNIEnv* env, jclass clazz, jboolean red, jboolean green, jboolean blue, jboolean alpha) {


//@line:149

        glColorMask(red, green, blue, alpha);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glCompressedTexImage2D(JNIEnv* env, jclass clazz, jint target, jint level, jint internalformat, jint width, jint height, jint border, jint imageSize, jobject obj_data) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);


//@line:153

        glCompressedTexImage2D(target, level, internalformat, width, height, border, imageSize, data);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glCompressedTexSubImage2D(JNIEnv* env, jclass clazz, jint target, jint level, jint xoffset, jint yoffset, jint width, jint height, jint format, jint imageSize, jobject obj_data) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);


//@line:157

        glCompressedTexSubImage2D(target, level, xoffset, yoffset, width, height, format, imageSize, data);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glCopyTexImage2D(JNIEnv* env, jclass clazz, jint target, jint level, jint internalformat, jint x, jint y, jint width, jint height, jint border) {


//@line:161

        glCopyTexImage2D(target, level, internalformat, x, y, width, height, border);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glCopyTexSubImage2D(JNIEnv* env, jclass clazz, jint target, jint level, jint xoffset, jint yoffset, jint x, jint y, jint width, jint height) {


//@line:165

        glCopyTexSubImage2D(target, level, xoffset, yoffset, x, y, width, height);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glCullFace(JNIEnv* env, jclass clazz, jint mode) {


//@line:169

        glCullFace(mode);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDeleteTexture(JNIEnv* env, jclass clazz, jint texture) {


//@line:173

        GLuint b = texture;
        glDeleteTextures(1, &b);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDepthFunc(JNIEnv* env, jclass clazz, jint func) {


//@line:178

        glDepthFunc(func);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDepthMask(JNIEnv* env, jclass clazz, jboolean flag) {


//@line:182

        glDepthMask(flag);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDepthRangef(JNIEnv* env, jclass clazz, jfloat zNear, jfloat zFar) {


//@line:186

        glDepthRangef(zNear, zFar);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDisable(JNIEnv* env, jclass clazz, jint cap) {


//@line:190

        glDisable(cap);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDrawArrays(JNIEnv* env, jclass clazz, jint mode, jint first, jint count) {


//@line:194

        glDrawArrays(mode, first, count);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDrawElements__IIILjava_nio_Buffer_2(JNIEnv* env, jclass clazz, jint mode, jint count, jint type, jobject obj_indices) {
	unsigned char* indices = (unsigned char*)(obj_indices?env->GetDirectBufferAddress(obj_indices):0);


//@line:198

        glDrawElements(mode, count, type, indices);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glEnable(JNIEnv* env, jclass clazz, jint cap) {


//@line:202

        glEnable(cap);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glFinish(JNIEnv* env, jclass clazz) {


//@line:206

        glFinish();
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glFlush(JNIEnv* env, jclass clazz) {


//@line:210

        glFlush();
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glFrontFace(JNIEnv* env, jclass clazz, jint mode) {


//@line:214

        glFrontFace(mode);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glGenTexture(JNIEnv* env, jclass clazz) {


//@line:218

        GLuint result;
        glGenTextures(1, &result);
        return result;
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetError(JNIEnv* env, jclass clazz) {


//@line:224

        return glGetError();
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetIntegerv(JNIEnv* env, jclass clazz, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:228

        glGetIntegerv(pname, params);
    

}

JNIEXPORT jstring JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetString(JNIEnv* env, jclass clazz, jint name) {


//@line:232

        return env->NewStringUTF((const char*)glGetString(name));
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glHint(JNIEnv* env, jclass clazz, jint target, jint mode) {


//@line:236

        glHint(target, mode);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glLineWidth(JNIEnv* env, jclass clazz, jfloat width) {


//@line:240

        glLineWidth(width);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glPixelStorei(JNIEnv* env, jclass clazz, jint pname, jint param) {


//@line:244

        glPixelStorei(pname, param);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glPolygonOffset(JNIEnv* env, jclass clazz, jfloat factor, jfloat units) {


//@line:248

        glPolygonOffset(factor, units);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glReadPixels(JNIEnv* env, jclass clazz, jint x, jint y, jint width, jint height, jint format, jint type, jobject obj_pixels) {
	unsigned char* pixels = (unsigned char*)(obj_pixels?env->GetDirectBufferAddress(obj_pixels):0);


//@line:252

        glReadPixels(x, y, width, height, format, type, pixels);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glScissor(JNIEnv* env, jclass clazz, jint x, jint y, jint width, jint height) {


//@line:256

        glScissor(x, y, width, height);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glStencilFunc(JNIEnv* env, jclass clazz, jint func, jint ref, jint mask) {


//@line:260

        glStencilFunc(func, ref, mask);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glStencilMask(JNIEnv* env, jclass clazz, jint mask) {


//@line:264

        glStencilMask(mask);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glStencilOp(JNIEnv* env, jclass clazz, jint fail, jint zfail, jint zpass) {


//@line:268

        glStencilOp(fail, zfail, zpass);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTexImage2D(JNIEnv* env, jclass clazz, jint target, jint level, jint internalformat, jint width, jint height, jint border, jint format, jint type, jobject obj_pixels) {
	unsigned char* pixels = (unsigned char*)(obj_pixels?env->GetDirectBufferAddress(obj_pixels):0);


//@line:272

        glTexImage2D(target, level, internalformat, width, height, border, format, type, pixels);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTexParameterf(JNIEnv* env, jclass clazz, jint target, jint pname, jfloat param) {


//@line:276

        glTexParameterf(target, pname, param);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTexSubImage2D(JNIEnv* env, jclass clazz, jint target, jint level, jint xoffset, jint yoffset, jint width, jint height, jint format, jint type, jobject obj_pixels) {
	unsigned char* pixels = (unsigned char*)(obj_pixels?env->GetDirectBufferAddress(obj_pixels):0);


//@line:280

        glTexSubImage2D(target, level, xoffset, yoffset, width, height, format, type, pixels);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glViewport(JNIEnv* env, jclass clazz, jint x, jint y, jint width, jint height) {


//@line:284

        glViewport(x, y, width, height);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glAttachShader(JNIEnv* env, jclass clazz, jint program, jint shader) {


//@line:288

        glAttachShader(program, shader);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBindAttribLocation(JNIEnv* env, jclass clazz, jint program, jint index, jstring obj_name) {
	char* name = (char*)env->GetStringUTFChars(obj_name, 0);


//@line:292

        glBindAttribLocation(program, index, name);
    
	env->ReleaseStringUTFChars(obj_name, name);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBindBuffer(JNIEnv* env, jclass clazz, jint target, jint buffer) {


//@line:296

        glBindBuffer(target, buffer);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBindFramebuffer(JNIEnv* env, jclass clazz, jint target, jint framebuffer) {


//@line:300

        if(glBindFramebuffer){
            glBindFramebuffer(target, framebuffer);
            return;
        }

        glBindFramebufferEXT(target, framebuffer);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBindRenderbuffer(JNIEnv* env, jclass clazz, jint target, jint renderbuffer) {


//@line:309

        if(glBindRenderbuffer){
            glBindRenderbuffer(target, renderbuffer);
            return;
        }

        glBindRenderbufferEXT(target, renderbuffer);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBlendColor(JNIEnv* env, jclass clazz, jfloat red, jfloat green, jfloat blue, jfloat alpha) {


//@line:318

        glBlendColor(red, green, blue, alpha);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBlendEquation(JNIEnv* env, jclass clazz, jint mode) {


//@line:322

        glBlendEquation(mode);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBlendEquationSeparate(JNIEnv* env, jclass clazz, jint modeRGB, jint modeAlpha) {


//@line:326

        glBlendEquationSeparate(modeRGB, modeAlpha);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBlendFuncSeparate(JNIEnv* env, jclass clazz, jint srcRGB, jint dstRGB, jint srcAlpha, jint dstAlpha) {


//@line:330

        glBlendFuncSeparate(srcRGB, dstRGB, srcAlpha, dstAlpha);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBufferData(JNIEnv* env, jclass clazz, jint target, jint size, jobject obj_data, jint usage) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);


//@line:334

        glBufferData(target, size, data, usage);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBufferSubData(JNIEnv* env, jclass clazz, jint target, jint offset, jint size, jobject obj_data) {
	unsigned char* data = (unsigned char*)(obj_data?env->GetDirectBufferAddress(obj_data):0);


//@line:338

        glBufferSubData(target, offset, size, data);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glCheckFramebufferStatus(JNIEnv* env, jclass clazz, jint target) {


//@line:342

        if(glCheckFramebufferStatus){
            return glCheckFramebufferStatus(target);
        }

        return glCheckFramebufferStatusEXT(target);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glCompileShader(JNIEnv* env, jclass clazz, jint shader) {


//@line:350

        glCompileShader(shader);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glCreateProgram(JNIEnv* env, jclass clazz) {


//@line:354

        return glCreateProgram();
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glCreateShader(JNIEnv* env, jclass clazz, jint type) {


//@line:358

        return glCreateShader(type);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDeleteBuffer(JNIEnv* env, jclass clazz, jint buffer) {


//@line:362

        GLuint b = buffer;
        glDeleteBuffers(1, &b);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDeleteFramebuffer(JNIEnv* env, jclass clazz, jint framebuffer) {


//@line:367

        if(glDeleteFramebuffers){
            GLuint b = framebuffer;
            glDeleteFramebuffers(1, &b);
            return;
        }

        GLuint b = framebuffer;
        glDeleteFramebuffersEXT(1, &b);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDeleteProgram(JNIEnv* env, jclass clazz, jint program) {


//@line:378

        glDeleteProgram(program);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDeleteRenderbuffer(JNIEnv* env, jclass clazz, jint renderbuffer) {


//@line:382

        GLuint b = renderbuffer;

        if(glDeleteRenderbuffers){
            glDeleteRenderbuffers(1, &b);
            return;
        }

        glDeleteRenderbuffersEXT(1, &b);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDeleteShader(JNIEnv* env, jclass clazz, jint shader) {


//@line:393

        glDeleteShader(shader);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDetachShader(JNIEnv* env, jclass clazz, jint program, jint shader) {


//@line:397

        glDetachShader(program, shader);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDisableVertexAttribArray(JNIEnv* env, jclass clazz, jint index) {


//@line:401

        glDisableVertexAttribArray(index);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDrawElements__IIII(JNIEnv* env, jclass clazz, jint mode, jint count, jint type, jint indices) {


//@line:405

        glDrawElements(mode, count, type, (const void*)indices);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glEnableVertexAttribArray(JNIEnv* env, jclass clazz, jint index) {


//@line:409

        glEnableVertexAttribArray(index);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glFramebufferRenderbuffer(JNIEnv* env, jclass clazz, jint target, jint attachment, jint renderbuffertarget, jint renderbuffer) {


//@line:413

        if(glFramebufferRenderbuffer){
            glFramebufferRenderbuffer(target, attachment, renderbuffertarget, renderbuffer);
            return;
        }

        glFramebufferRenderbufferEXT(target, attachment, renderbuffertarget, renderbuffer);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glFramebufferTexture2D(JNIEnv* env, jclass clazz, jint target, jint attachment, jint textarget, jint texture, jint level) {


//@line:422

        if(glFramebufferTexture2D){
            glFramebufferTexture2D(target, attachment, textarget, texture, level);
            return;
        }

        glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glGenBuffer(JNIEnv* env, jclass clazz) {


//@line:431

        GLuint result;
        glGenBuffers(1, &result);
        return result;
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGenerateMipmap(JNIEnv* env, jclass clazz, jint target) {


//@line:437

        if(glGenerateMipmap){
            glGenerateMipmap(target);
            return;
        }

        glGenerateMipmapEXT(target);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glGenFramebuffer(JNIEnv* env, jclass clazz) {


//@line:446

        if(glGenFramebuffers){
            GLuint result;
            glGenFramebuffers(1, &result);
            return result;
        }

        GLuint result;
        glGenFramebuffersEXT(1, &result);
        return result;
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glGenRenderbuffer(JNIEnv* env, jclass clazz) {


//@line:458

        if(glGenRenderbuffers){
            GLuint result;
            glGenRenderbuffers(1, &result);
            return result;
        }

        GLuint result;
        glGenRenderbuffersEXT(1, &result);
        return result;
    

}

JNIEXPORT jstring JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetActiveAttrib(JNIEnv* env, jclass clazz, jint program, jint index, jobject size, jobject type) {


//@line:470

        char cname[2048];
	    void* sizePtr = getDirectBufferPointer( env, size );
	    void* typePtr = getDirectBufferPointer( env, type );
	    glGetActiveAttrib( program, index, 2048, NULL, (GLint*)sizePtr, (GLenum*)typePtr, cname );

        return env->NewStringUTF(cname);
    

}

JNIEXPORT jstring JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetActiveUniform(JNIEnv* env, jclass clazz, jint program, jint index, jobject size, jobject type) {


//@line:479

        char cname[2048];
        void* sizePtr = getDirectBufferPointer( env, size );
        void* typePtr = getDirectBufferPointer( env, type );
        glGetActiveUniform( program, index, 2048, NULL, (GLint*)sizePtr, (GLenum*)typePtr, cname );
        return env->NewStringUTF(cname);
    

}

static inline jint wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetAttribLocation
(JNIEnv* env, jclass clazz, jint program, jstring obj_name, char* name) {

//@line:487

        return glGetAttribLocation(program, name);
    
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetAttribLocation(JNIEnv* env, jclass clazz, jint program, jstring obj_name) {
	char* name = (char*)env->GetStringUTFChars(obj_name, 0);

	jint JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetAttribLocation(env, clazz, program, obj_name, name);

	env->ReleaseStringUTFChars(obj_name, name);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetBooleanv(JNIEnv* env, jclass clazz, jint pname, jobject obj_params) {
	unsigned char* params = (unsigned char*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:491

        glGetBooleanv(pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetBufferParameteriv(JNIEnv* env, jclass clazz, jint target, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:495

        glGetBufferParameteriv(target, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetFloatv(JNIEnv* env, jclass clazz, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:499

        glGetFloatv(pname, params);
    

}

static inline void wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetFramebufferAttachmentParameteriv
(JNIEnv* env, jclass clazz, jint target, jint attachment, jint pname, jobject obj_params, int* params) {

//@line:503

        if(glGetFramebufferAttachmentParameteriv){
            glGetFramebufferAttachmentParameteriv(target, attachment, pname, params);
            return;
        }

        glGetFramebufferAttachmentParameterivEXT(target, attachment, pname, params);
    
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetFramebufferAttachmentParameteriv(JNIEnv* env, jclass clazz, jint target, jint attachment, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);

	wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetFramebufferAttachmentParameteriv(env, clazz, target, attachment, pname, obj_params, params);


	return;
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetProgramiv(JNIEnv* env, jclass clazz, jint program, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:512

        glGetProgramiv(program, pname, params);
    

}

JNIEXPORT jstring JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetProgramInfoLog(JNIEnv* env, jclass clazz, jint program) {


//@line:516

        char info[1024*10]; // FIXME 10k limit should suffice
        int length = 0;
        glGetProgramInfoLog( program, 1024*10, &length, info );
        return env->NewStringUTF(info);
    

}

static inline void wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetRenderbufferParameteriv
(JNIEnv* env, jclass clazz, jint target, jint pname, jobject obj_params, int* params) {

//@line:523

        if(glGetRenderbufferParameteriv){
            glGetRenderbufferParameteriv(target, pname, params);
            return;
        }

        glGetRenderbufferParameterivEXT(target, pname, params);
    
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetRenderbufferParameteriv(JNIEnv* env, jclass clazz, jint target, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);

	wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetRenderbufferParameteriv(env, clazz, target, pname, obj_params, params);


	return;
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetShaderiv(JNIEnv* env, jclass clazz, jint shader, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:532

        glGetShaderiv(shader, pname, params);
    

}

JNIEXPORT jstring JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetShaderInfoLog(JNIEnv* env, jclass clazz, jint shader) {


//@line:536

        char info[1024*10]; // FIXME 10k limit should suffice
        int length = 0;
        glGetShaderInfoLog( shader, 1024*10, &length, info );
        return env->NewStringUTF( info );
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetShaderPrecisionFormat(JNIEnv* env, jclass clazz, jint shadertype, jint precisiontype, jobject obj_range, jobject obj_precision) {
	int* range = (int*)(obj_range?env->GetDirectBufferAddress(obj_range):0);
	int* precision = (int*)(obj_precision?env->GetDirectBufferAddress(obj_precision):0);


//@line:543

        glGetShaderPrecisionFormat(shadertype, precisiontype, range, precision);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetTexParameterfv(JNIEnv* env, jclass clazz, jint target, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:547

        glGetTexParameterfv(target, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetTexParameteriv(JNIEnv* env, jclass clazz, jint target, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:551

        glGetTexParameteriv(target, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetUniformfv(JNIEnv* env, jclass clazz, jint program, jint location, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:555

        glGetUniformfv(program, location, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetUniformiv(JNIEnv* env, jclass clazz, jint program, jint location, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:559

        glGetUniformiv(program, location, (GLint*)params);
    

}

static inline jint wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetUniformLocation
(JNIEnv* env, jclass clazz, jint program, jstring obj_name, char* name) {

//@line:563

        return glGetUniformLocation(program, name);
    
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetUniformLocation(JNIEnv* env, jclass clazz, jint program, jstring obj_name) {
	char* name = (char*)env->GetStringUTFChars(obj_name, 0);

	jint JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetUniformLocation(env, clazz, program, obj_name, name);

	env->ReleaseStringUTFChars(obj_name, name);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetVertexAttribfv(JNIEnv* env, jclass clazz, jint index, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:567

        glGetVertexAttribfv(index, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetVertexAttribiv(JNIEnv* env, jclass clazz, jint index, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:571

        glGetVertexAttribiv(index, pname, params);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsBuffer(JNIEnv* env, jclass clazz, jint buffer) {


//@line:575

        return glIsBuffer(buffer);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsEnabled(JNIEnv* env, jclass clazz, jint cap) {


//@line:579

        return glIsEnabled(cap);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsFramebuffer(JNIEnv* env, jclass clazz, jint framebuffer) {


//@line:583

        if(glIsFramebuffer){
            return glIsFramebuffer(framebuffer);
        }

        return glIsFramebufferEXT(framebuffer);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsProgram(JNIEnv* env, jclass clazz, jint program) {


//@line:591

        return glIsProgram(program);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsRenderbuffer(JNIEnv* env, jclass clazz, jint renderbuffer) {


//@line:595

        if(glIsRenderbuffer){
            return glIsRenderbuffer(renderbuffer);
        }

        return glIsRenderbufferEXT(renderbuffer);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsShader(JNIEnv* env, jclass clazz, jint shader) {


//@line:603

        return glIsShader(shader);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsTexture(JNIEnv* env, jclass clazz, jint texture) {


//@line:607

        return glIsTexture(texture);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glLinkProgram(JNIEnv* env, jclass clazz, jint program) {


//@line:611

        glLinkProgram(program);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glReleaseShaderCompiler(JNIEnv* env, jclass clazz) {


//@line:615

        glReleaseShaderCompiler();
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glRenderbufferStorage(JNIEnv* env, jclass clazz, jint target, jint internalformat, jint width, jint height) {


//@line:619

        if(glRenderbufferStorage){
            glRenderbufferStorage(target, internalformat, width, height);
            return;
        }

        glRenderbufferStorageEXT(target, internalformat, width, height);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glSampleCoverage(JNIEnv* env, jclass clazz, jfloat value, jboolean invert) {


//@line:628

        glSampleCoverage(value, invert);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glShaderSource(JNIEnv* env, jclass clazz, jint shader, jstring obj_string) {
	char* string = (char*)env->GetStringUTFChars(obj_string, 0);


//@line:632

        glShaderSource(shader, 1, &string, NULL);
    
	env->ReleaseStringUTFChars(obj_string, string);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glStencilFuncSeparate(JNIEnv* env, jclass clazz, jint face, jint func, jint ref, jint mask) {


//@line:636

        glStencilFuncSeparate(face, func, ref, mask);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glStencilMaskSeparate(JNIEnv* env, jclass clazz, jint face, jint mask) {


//@line:640

        glStencilMaskSeparate(face, mask);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glStencilOpSeparate(JNIEnv* env, jclass clazz, jint face, jint fail, jint zfail, jint zpass) {


//@line:644

        glStencilOpSeparate(face, fail, zfail, zpass);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTexParameterfv(JNIEnv* env, jclass clazz, jint target, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:648

        glTexParameterfv(target, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTexParameteri(JNIEnv* env, jclass clazz, jint target, jint pname, jint param) {


//@line:652

        glTexParameteri(target, pname, param);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTexParameteriv(JNIEnv* env, jclass clazz, jint target, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:656

        glTexParameteriv(target, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform1f(JNIEnv* env, jclass clazz, jint location, jfloat x) {


//@line:660

        glUniform1f(location, x);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform1fv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_v) {
	float* v = (float*)(obj_v?env->GetDirectBufferAddress(obj_v):0);


//@line:664

        glUniform1fv(location, count, v);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform1fv__II_3FI(JNIEnv* env, jclass clazz, jint location, jint count, jfloatArray obj_v, jint offset) {
	float* v = (float*)env->GetPrimitiveArrayCritical(obj_v, 0);


//@line:668

        glUniform1fv(location, count, (GLfloat*)&v[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_v, v, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform1i(JNIEnv* env, jclass clazz, jint location, jint x) {


//@line:672

        glUniform1i(location, x);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform1iv__IILjava_nio_IntBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_v) {
	int* v = (int*)(obj_v?env->GetDirectBufferAddress(obj_v):0);


//@line:676

        glUniform1iv(location, count, v);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform1iv__II_3II(JNIEnv* env, jclass clazz, jint location, jint count, jintArray obj_v, jint offset) {
	int* v = (int*)env->GetPrimitiveArrayCritical(obj_v, 0);


//@line:680

        glUniform1iv(location, count, (GLint*)&v[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_v, v, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform2f(JNIEnv* env, jclass clazz, jint location, jfloat x, jfloat y) {


//@line:684

        glUniform2f(location, x, y);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform2fv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_v) {
	float* v = (float*)(obj_v?env->GetDirectBufferAddress(obj_v):0);


//@line:688

        glUniform2fv(location, count, v);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform2fv__II_3FI(JNIEnv* env, jclass clazz, jint location, jint count, jfloatArray obj_v, jint offset) {
	float* v = (float*)env->GetPrimitiveArrayCritical(obj_v, 0);


//@line:692

        glUniform2fv(location, count, (GLfloat*)&v[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_v, v, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform2i(JNIEnv* env, jclass clazz, jint location, jint x, jint y) {


//@line:696

        glUniform2i(location, x, y);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform2iv__IILjava_nio_IntBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_v) {
	int* v = (int*)(obj_v?env->GetDirectBufferAddress(obj_v):0);


//@line:700

        glUniform2iv(location, count, v);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform2iv__II_3II(JNIEnv* env, jclass clazz, jint location, jint count, jintArray obj_v, jint offset) {
	int* v = (int*)env->GetPrimitiveArrayCritical(obj_v, 0);


//@line:704

        glUniform2iv(location, count, (GLint*)&v[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_v, v, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform3f(JNIEnv* env, jclass clazz, jint location, jfloat x, jfloat y, jfloat z) {


//@line:708

        glUniform3f(location, x, y, z);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform3fv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_v) {
	float* v = (float*)(obj_v?env->GetDirectBufferAddress(obj_v):0);


//@line:712

        glUniform3fv(location, count, v);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform3fv__II_3FI(JNIEnv* env, jclass clazz, jint location, jint count, jfloatArray obj_v, jint offset) {
	float* v = (float*)env->GetPrimitiveArrayCritical(obj_v, 0);


//@line:716

        glUniform3fv(location, count, (GLfloat*)&v[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_v, v, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform3i(JNIEnv* env, jclass clazz, jint location, jint x, jint y, jint z) {


//@line:720

        glUniform3i(location, x, y, z);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform3iv__IILjava_nio_IntBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_v) {
	int* v = (int*)(obj_v?env->GetDirectBufferAddress(obj_v):0);


//@line:724

        glUniform3iv(location, count, v);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform3iv__II_3II(JNIEnv* env, jclass clazz, jint location, jint count, jintArray obj_v, jint offset) {
	int* v = (int*)env->GetPrimitiveArrayCritical(obj_v, 0);


//@line:728

        glUniform3iv(location, count, (GLint*)&v[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_v, v, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform4f(JNIEnv* env, jclass clazz, jint location, jfloat x, jfloat y, jfloat z, jfloat w) {


//@line:732

        glUniform4f(location, x, y, z, w);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform4fv__IILjava_nio_FloatBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_v) {
	float* v = (float*)(obj_v?env->GetDirectBufferAddress(obj_v):0);


//@line:736

        glUniform4fv(location, count, v);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform4fv__II_3FI(JNIEnv* env, jclass clazz, jint location, jint count, jfloatArray obj_v, jint offset) {
	float* v = (float*)env->GetPrimitiveArrayCritical(obj_v, 0);


//@line:740

        glUniform4fv(location, count, (GLfloat*)&v[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_v, v, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform4i(JNIEnv* env, jclass clazz, jint location, jint x, jint y, jint z, jint w) {


//@line:744

        glUniform4i(location, x, y, z, w);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform4iv__IILjava_nio_IntBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_v) {
	int* v = (int*)(obj_v?env->GetDirectBufferAddress(obj_v):0);


//@line:748

        glUniform4iv(location, count, v);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform4iv__II_3II(JNIEnv* env, jclass clazz, jint location, jint count, jintArray obj_v, jint offset) {
	int* v = (int*)env->GetPrimitiveArrayCritical(obj_v, 0);


//@line:752

        glUniform4iv(location, count, (GLint*)&v[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_v, v, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix2fv__IIZLjava_nio_FloatBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jobject obj_value) {
	float* value = (float*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:756

        glUniformMatrix2fv(location, count, transpose, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix2fv__IIZ_3FI(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jfloatArray obj_value, jint offset) {
	float* value = (float*)env->GetPrimitiveArrayCritical(obj_value, 0);


//@line:760

        glUniformMatrix2fv(location, count, transpose, (GLfloat*)&value[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_value, value, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix3fv__IIZLjava_nio_FloatBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jobject obj_value) {
	float* value = (float*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:764

        glUniformMatrix3fv(location, count, transpose, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix3fv__IIZ_3FI(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jfloatArray obj_value, jint offset) {
	float* value = (float*)env->GetPrimitiveArrayCritical(obj_value, 0);


//@line:768

        glUniformMatrix3fv(location, count, transpose, (GLfloat*)&value[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_value, value, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix4fv__IIZLjava_nio_FloatBuffer_2(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jobject obj_value) {
	float* value = (float*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:772

        glUniformMatrix4fv(location, count, transpose, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix4fv__IIZ_3FI(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jfloatArray obj_value, jint offset) {
	float* value = (float*)env->GetPrimitiveArrayCritical(obj_value, 0);


//@line:776

        glUniformMatrix4fv(location, count, transpose, (GLfloat*)&value[offset]);
    
	env->ReleasePrimitiveArrayCritical(obj_value, value, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUseProgram(JNIEnv* env, jclass clazz, jint program) {


//@line:780

        glUseProgram(program);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glValidateProgram(JNIEnv* env, jclass clazz, jint program) {


//@line:784

        glValidateProgram(program);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttrib1f(JNIEnv* env, jclass clazz, jint indx, jfloat x) {


//@line:788

        glVertexAttrib1f(indx, x);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttrib1fv(JNIEnv* env, jclass clazz, jint indx, jobject obj_values) {
	float* values = (float*)(obj_values?env->GetDirectBufferAddress(obj_values):0);


//@line:792

        glVertexAttrib1fv(indx, values);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttrib2f(JNIEnv* env, jclass clazz, jint indx, jfloat x, jfloat y) {


//@line:796

        glVertexAttrib2f(indx, x, y);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttrib2fv(JNIEnv* env, jclass clazz, jint indx, jobject obj_values) {
	float* values = (float*)(obj_values?env->GetDirectBufferAddress(obj_values):0);


//@line:800

        glVertexAttrib2fv(indx, values);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttrib3f(JNIEnv* env, jclass clazz, jint indx, jfloat x, jfloat y, jfloat z) {


//@line:804

        glVertexAttrib3f(indx, x, y, z);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttrib3fv(JNIEnv* env, jclass clazz, jint indx, jobject obj_values) {
	float* values = (float*)(obj_values?env->GetDirectBufferAddress(obj_values):0);


//@line:808

        glVertexAttrib3fv(indx, values);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttrib4f(JNIEnv* env, jclass clazz, jint indx, jfloat x, jfloat y, jfloat z, jfloat w) {


//@line:812

        glVertexAttrib4f(indx, x, y, z, w);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttrib4fv(JNIEnv* env, jclass clazz, jint indx, jobject obj_values) {
	float* values = (float*)(obj_values?env->GetDirectBufferAddress(obj_values):0);


//@line:816

        glVertexAttrib4fv(indx, values);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttribPointer__IIIZILjava_lang_Object_2(JNIEnv* env, jclass clazz, jint indx, jint size, jint type, jboolean normalized, jint stride, jobject ptr) {


//@line:820

        void* dataPtr = getDirectBufferPointer( env, ptr );
        glVertexAttribPointer(indx, size, type, normalized, stride, dataPtr);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttribPointer__IIIZII(JNIEnv* env, jclass clazz, jint indx, jint size, jint type, jboolean normalized, jint stride, jint ptr) {


//@line:825

        glVertexAttribPointer(indx, size, type, normalized, stride, (const void*)ptr);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glReadBuffer(JNIEnv* env, jclass clazz, jint mode) {


//@line:832

        glReadBuffer(mode);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDrawRangeElements__IIIIII(JNIEnv* env, jclass clazz, jint mode, jint start, jint end, jint count, jint type, jint offset) {


//@line:836

        glDrawRangeElements(mode, start, end, count, type, (void*)offset);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDrawRangeElements__IIIIILjava_nio_Buffer_2(JNIEnv* env, jclass clazz, jint mode, jint start, jint end, jint count, jint type, jobject obj_indices) {
	unsigned char* indices = (unsigned char*)(obj_indices?env->GetDirectBufferAddress(obj_indices):0);


//@line:840

        glDrawRangeElements(mode, start, end, count, type, indices);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTexImage3D__IIIIIIIIII(JNIEnv* env, jclass clazz, jint target, jint level, jint internalformat, jint width, jint height, jint depth, jint border, jint format, jint type, jint offset) {


//@line:844

        glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, (void*)offset);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTexImage3D__IIIIIIIIILjava_nio_Buffer_2(JNIEnv* env, jclass clazz, jint target, jint level, jint internalformat, jint width, jint height, jint depth, jint border, jint format, jint type, jobject obj_pixels) {
	unsigned char* pixels = (unsigned char*)(obj_pixels?env->GetDirectBufferAddress(obj_pixels):0);


//@line:848

        glTexImage3D(target, level, internalformat, width, height, depth, border, format, type, pixels);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTexSubImage3D__IIIIIIIIIII(JNIEnv* env, jclass clazz, jint target, jint level, jint xoffset, jint yoffset, jint zoffset, jint width, jint height, jint depth, jint format, jint type, jint offset) {


//@line:852

        glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, (void*)offset);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTexSubImage3D__IIIIIIIIIILjava_nio_Buffer_2(JNIEnv* env, jclass clazz, jint target, jint level, jint xoffset, jint yoffset, jint zoffset, jint width, jint height, jint depth, jint format, jint type, jobject obj_pixels) {
	unsigned char* pixels = (unsigned char*)(obj_pixels?env->GetDirectBufferAddress(obj_pixels):0);


//@line:856

        glTexSubImage3D(target, level, xoffset, yoffset, zoffset, width, height, depth, format, type, pixels);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glCopyTexSubImage3D(JNIEnv* env, jclass clazz, jint target, jint level, jint xoffset, jint yoffset, jint zoffset, jint x, jint y, jint width, jint height) {


//@line:860

        glCopyTexSubImage3D(target, level, xoffset, yoffset, zoffset, x, y, width, height);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGenQueries(JNIEnv* env, jclass clazz, jint n, jobject obj_ids) {
	int* ids = (int*)(obj_ids?env->GetDirectBufferAddress(obj_ids):0);


//@line:864

        glGenQueries(n, (GLuint*)ids);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDeleteQueries(JNIEnv* env, jclass clazz, jint n, jobject obj_ids) {
	int* ids = (int*)(obj_ids?env->GetDirectBufferAddress(obj_ids):0);


//@line:868

        glDeleteQueries(n, (GLuint*)ids);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsQuery(JNIEnv* env, jclass clazz, jint id) {


//@line:872

        return glIsQuery(id);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBeginQuery(JNIEnv* env, jclass clazz, jint target, jint id) {


//@line:876

        glBeginQuery(target, id);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glEndQuery(JNIEnv* env, jclass clazz, jint target) {


//@line:880

        glEndQuery(target);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetQueryiv(JNIEnv* env, jclass clazz, jint target, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:884

        glGetQueryiv(target, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetQueryObjectuiv(JNIEnv* env, jclass clazz, jint id, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:888

        glGetQueryObjectuiv(id, pname, (GLuint*)params);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glUnmapBuffer(JNIEnv* env, jclass clazz, jint target) {


//@line:892

        return glUnmapBuffer(target);
    

}

JNIEXPORT jobject JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetBufferPointerv(JNIEnv* env, jclass clazz, jint target, jint pname) {


//@line:896

        env->ThrowNew(IAEClass, "Unsupported method");
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDrawBuffers(JNIEnv* env, jclass clazz, jint n, jobject obj_bufs) {
	int* bufs = (int*)(obj_bufs?env->GetDirectBufferAddress(obj_bufs):0);


//@line:900

        glDrawBuffers(n, (GLenum*)bufs);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix2x3fv(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jobject obj_value) {
	float* value = (float*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:904

        glUniformMatrix2x3fv(location, count, transpose, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix3x2fv(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jobject obj_value) {
	float* value = (float*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:908

        glUniformMatrix3x2fv(location, count, transpose, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix2x4fv(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jobject obj_value) {
	float* value = (float*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:912

        glUniformMatrix2x4fv(location, count, transpose, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix4x2fv(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jobject obj_value) {
	float* value = (float*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:916

        glUniformMatrix4x2fv(location, count, transpose, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix3x4fv(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jobject obj_value) {
	float* value = (float*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:920

        glUniformMatrix3x4fv(location, count, transpose, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformMatrix4x3fv(JNIEnv* env, jclass clazz, jint location, jint count, jboolean transpose, jobject obj_value) {
	float* value = (float*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:924

        glUniformMatrix4x3fv(location, count, transpose, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBlitFramebuffer(JNIEnv* env, jclass clazz, jint srcX0, jint srcY0, jint srcX1, jint srcY1, jint dstX0, jint dstY0, jint dstX1, jint dstY1, jint mask, jint filter) {


//@line:928

        glBlitFramebuffer(srcX0, srcY0, srcX1, srcY1, dstX0, dstY0, dstX1, dstY1, mask, filter);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glRenderbufferStorageMultisample(JNIEnv* env, jclass clazz, jint target, jint samples, jint internalformat, jint width, jint height) {


//@line:932

        glRenderbufferStorageMultisample(target, samples, internalformat, width, height);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glFramebufferTextureLayer(JNIEnv* env, jclass clazz, jint target, jint attachment, jint texture, jint level, jint layer) {


//@line:936

        glFramebufferTextureLayer(target, attachment, texture, level, layer);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glFlushMappedBufferRange(JNIEnv* env, jclass clazz, jint target, jint offset, jint length) {


//@line:940

        glFlushMappedBufferRange(target, offset, length);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBindVertexArray(JNIEnv* env, jclass clazz, jint array) {


//@line:944

        glBindVertexArray(array);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDeleteVertexArrays(JNIEnv* env, jclass clazz, jint n, jobject obj_arrays) {
	int* arrays = (int*)(obj_arrays?env->GetDirectBufferAddress(obj_arrays):0);


//@line:948

        glDeleteVertexArrays(n, (GLuint*)arrays);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGenVertexArrays(JNIEnv* env, jclass clazz, jint n, jobject obj_arrays) {
	int* arrays = (int*)(obj_arrays?env->GetDirectBufferAddress(obj_arrays):0);


//@line:952

        glGenVertexArrays(n, (GLuint*)arrays);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsVertexArray(JNIEnv* env, jclass clazz, jint array) {


//@line:956

        return glIsVertexArray(array);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBeginTransformFeedback(JNIEnv* env, jclass clazz, jint primitiveMode) {


//@line:960

        glBeginTransformFeedback(primitiveMode);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glEndTransformFeedback(JNIEnv* env, jclass clazz) {


//@line:964

        glEndTransformFeedback();
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBindBufferRange(JNIEnv* env, jclass clazz, jint target, jint index, jint buffer, jint offset, jint size) {


//@line:968

        glBindBufferRange(target, index, buffer, offset, size);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBindBufferBase(JNIEnv* env, jclass clazz, jint target, jint index, jint buffer) {


//@line:972

        glBindBufferBase(target, index, buffer);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glTransformFeedbackVaryings(JNIEnv* env, jclass clazz, jint program, jobjectArray varyings, jint bufferMode) {


//@line:976

        env->ThrowNew(IAEClass, "Unsupported method");
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttribIPointer(JNIEnv* env, jclass clazz, jint index, jint size, jint type, jint stride, jint offset) {


//@line:980

        glVertexAttribIPointer(index, size, type, stride, (void*)offset);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetVertexAttribIiv(JNIEnv* env, jclass clazz, jint index, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:984

        glGetVertexAttribIiv(index, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetVertexAttribIuiv(JNIEnv* env, jclass clazz, jint index, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:988

        glGetVertexAttribIuiv(index, pname, (GLuint*)params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttribI4i(JNIEnv* env, jclass clazz, jint index, jint x, jint y, jint z, jint w) {


//@line:992

        glVertexAttribI4i(index, x, y, z, w);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttribI4ui(JNIEnv* env, jclass clazz, jint index, jint x, jint y, jint z, jint w) {


//@line:996

        glVertexAttribI4ui(index, x, y, z, w);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetUniformuiv(JNIEnv* env, jclass clazz, jint program, jint location, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:1000

        glGetUniformuiv(program, location, (GLuint*)params);
    

}

static inline jint wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetFragDataLocation
(JNIEnv* env, jclass clazz, jint program, jstring obj_name, char* name) {

//@line:1004

        return glGetFragDataLocation(program, name);
    
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetFragDataLocation(JNIEnv* env, jclass clazz, jint program, jstring obj_name) {
	char* name = (char*)env->GetStringUTFChars(obj_name, 0);

	jint JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetFragDataLocation(env, clazz, program, obj_name, name);

	env->ReleaseStringUTFChars(obj_name, name);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform1uiv(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_value) {
	int* value = (int*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:1008

        glUniform1uiv(location, count, (GLuint*)value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform3uiv(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_value) {
	int* value = (int*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:1012

        glUniform3uiv(location, count, (GLuint*)value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniform4uiv(JNIEnv* env, jclass clazz, jint location, jint count, jobject obj_value) {
	int* value = (int*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:1016

        glUniform4uiv(location, count, (GLuint*)value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glClearBufferiv(JNIEnv* env, jclass clazz, jint buffer, jint drawbuffer, jobject obj_value) {
	int* value = (int*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:1020

        glClearBufferiv(buffer, drawbuffer, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glClearBufferuiv(JNIEnv* env, jclass clazz, jint buffer, jint drawbuffer, jobject obj_value) {
	int* value = (int*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:1024

        glClearBufferuiv(buffer, drawbuffer, (GLuint*)value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glClearBufferfv(JNIEnv* env, jclass clazz, jint buffer, jint drawbuffer, jobject obj_value) {
	float* value = (float*)(obj_value?env->GetDirectBufferAddress(obj_value):0);


//@line:1028

        glClearBufferfv(buffer, drawbuffer, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glClearBufferfi(JNIEnv* env, jclass clazz, jint buffer, jint drawbuffer, jfloat depth, jint stencil) {


//@line:1032

        glClearBufferfi(buffer, drawbuffer, depth, stencil);
    

}

JNIEXPORT jstring JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetStringi(JNIEnv* env, jclass clazz, jint name, jint index) {


//@line:1036

        return env->NewStringUTF((const char*)glGetStringi(name, index));
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glCopyBufferSubData(JNIEnv* env, jclass clazz, jint readTarget, jint writeTarget, jint readOffset, jint writeOffset, jint size) {


//@line:1040

        glCopyBufferSubData(readTarget, writeTarget, readOffset, writeOffset, size);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetUniformIndices(JNIEnv* env, jclass clazz, jint program, jobjectArray uniformNames, jobject obj_uniformIndices) {
	int* uniformIndices = (int*)(obj_uniformIndices?env->GetDirectBufferAddress(obj_uniformIndices):0);


//@line:1044

        env->ThrowNew(IAEClass, "Unsupported method");
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetActiveUniformsiv(JNIEnv* env, jclass clazz, jint program, jint uniformCount, jobject obj_uniformIndices, jint pname, jobject obj_params) {
	int* uniformIndices = (int*)(obj_uniformIndices?env->GetDirectBufferAddress(obj_uniformIndices):0);
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:1048

        glGetActiveUniformsiv(program, uniformCount, (GLuint*)uniformIndices, pname, (GLint*)params);
    

}

static inline jint wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetUniformBlockIndex
(JNIEnv* env, jclass clazz, jint program, jstring obj_uniformBlockName, char* uniformBlockName) {

//@line:1052

        return glGetUniformBlockIndex(program, uniformBlockName);
    
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetUniformBlockIndex(JNIEnv* env, jclass clazz, jint program, jstring obj_uniformBlockName) {
	char* uniformBlockName = (char*)env->GetStringUTFChars(obj_uniformBlockName, 0);

	jint JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDLGL_glGetUniformBlockIndex(env, clazz, program, obj_uniformBlockName, uniformBlockName);

	env->ReleaseStringUTFChars(obj_uniformBlockName, uniformBlockName);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetActiveUniformBlockiv(JNIEnv* env, jclass clazz, jint program, jint uniformBlockIndex, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:1056

        glGetActiveUniformBlockiv(program, uniformBlockIndex, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetActiveUniformBlockName(JNIEnv* env, jclass clazz, jint program, jint uniformBlockIndex, jobject obj_length, jobject obj_uniformBlockName) {
	unsigned char* length = (unsigned char*)(obj_length?env->GetDirectBufferAddress(obj_length):0);
	unsigned char* uniformBlockName = (unsigned char*)(obj_uniformBlockName?env->GetDirectBufferAddress(obj_uniformBlockName):0);


//@line:1060

        env->ThrowNew(IAEClass, "Unsupported method");
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glUniformBlockBinding(JNIEnv* env, jclass clazz, jint program, jint uniformBlockIndex, jint uniformBlockBinding) {


//@line:1064

        glUniformBlockBinding(program, uniformBlockIndex, uniformBlockBinding);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDrawArraysInstanced(JNIEnv* env, jclass clazz, jint mode, jint first, jint count, jint instanceCount) {


//@line:1068

        glDrawArraysInstanced(mode, first, count, instanceCount);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDrawElementsInstanced(JNIEnv* env, jclass clazz, jint mode, jint count, jint type, jint indicesOffset, jint instanceCount) {


//@line:1072

        glDrawElementsInstanced(mode, count, type, (void*)indicesOffset, instanceCount);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetInteger64v(JNIEnv* env, jclass clazz, jint pname, jobject obj_params) {
	long long* params = (long long*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:1076

        glGetInteger64v(pname, (GLint64*)params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetBufferParameteri64v(JNIEnv* env, jclass clazz, jint target, jint pname, jobject obj_params) {
	long long* params = (long long*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:1080

        glGetBufferParameteri64v(target, pname, (GLint64*)params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGenSamplers(JNIEnv* env, jclass clazz, jint count, jobject obj_samplers) {
	int* samplers = (int*)(obj_samplers?env->GetDirectBufferAddress(obj_samplers):0);


//@line:1084

        glGenSamplers(count, (GLuint*)samplers);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDeleteSamplers(JNIEnv* env, jclass clazz, jint count, jobject obj_samplers) {
	int* samplers = (int*)(obj_samplers?env->GetDirectBufferAddress(obj_samplers):0);


//@line:1088

        glDeleteSamplers(count, (GLuint*)samplers);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsSampler(JNIEnv* env, jclass clazz, jint sampler) {


//@line:1092

        return glIsSampler(sampler);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBindSampler(JNIEnv* env, jclass clazz, jint unit, jint sampler) {


//@line:1096

        glBindSampler(unit, sampler);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glSamplerParameteri(JNIEnv* env, jclass clazz, jint sampler, jint pname, jint param) {


//@line:1100

        glSamplerParameteri(sampler, pname, param);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glSamplerParameteriv(JNIEnv* env, jclass clazz, jint sampler, jint pname, jobject obj_param) {
	int* param = (int*)(obj_param?env->GetDirectBufferAddress(obj_param):0);


//@line:1104

        glSamplerParameteriv(sampler, pname, param);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glSamplerParameterf(JNIEnv* env, jclass clazz, jint sampler, jint pname, jfloat param) {


//@line:1108

        glSamplerParameterf(sampler, pname, param);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glSamplerParameterfv(JNIEnv* env, jclass clazz, jint sampler, jint pname, jobject obj_param) {
	float* param = (float*)(obj_param?env->GetDirectBufferAddress(obj_param):0);


//@line:1112

        glSamplerParameterfv(sampler, pname, param);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetSamplerParameteriv(JNIEnv* env, jclass clazz, jint sampler, jint pname, jobject obj_params) {
	int* params = (int*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:1116

        glGetSamplerParameteriv(sampler, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGetSamplerParameterfv(JNIEnv* env, jclass clazz, jint sampler, jint pname, jobject obj_params) {
	float* params = (float*)(obj_params?env->GetDirectBufferAddress(obj_params):0);


//@line:1120

        glGetSamplerParameterfv(sampler, pname, params);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glVertexAttribDivisor(JNIEnv* env, jclass clazz, jint index, jint divisor) {


//@line:1124

        glVertexAttribDivisor(index, divisor);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glBindTransformFeedback(JNIEnv* env, jclass clazz, jint target, jint id) {


//@line:1128

        glBindTransformFeedback(target, id);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glDeleteTransformFeedbacks(JNIEnv* env, jclass clazz, jint n, jobject obj_ids) {
	int* ids = (int*)(obj_ids?env->GetDirectBufferAddress(obj_ids):0);


//@line:1132

        glDeleteTransformFeedbacks(n, (GLuint*)ids);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glGenTransformFeedbacks(JNIEnv* env, jclass clazz, jint n, jobject obj_ids) {
	int* ids = (int*)(obj_ids?env->GetDirectBufferAddress(obj_ids):0);


//@line:1136

        glGenTransformFeedbacks(n, (GLuint*)ids);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDLGL_glIsTransformFeedback(JNIEnv* env, jclass clazz, jint id) {


//@line:1140

        return glIsTransformFeedback(id);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glPauseTransformFeedback(JNIEnv* env, jclass clazz) {


//@line:1144

        glPauseTransformFeedback();
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glResumeTransformFeedback(JNIEnv* env, jclass clazz) {


//@line:1148

        glResumeTransformFeedback();
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glProgramParameteri(JNIEnv* env, jclass clazz, jint program, jint pname, jint value) {


//@line:1152

        glProgramParameteri(program, pname, value);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glInvalidateFramebuffer(JNIEnv* env, jclass clazz, jint target, jint numAttachments, jobject obj_attachments) {
	int* attachments = (int*)(obj_attachments?env->GetDirectBufferAddress(obj_attachments):0);


//@line:1156

        glInvalidateFramebuffer(target, numAttachments, (GLenum*)attachments);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDLGL_glInvalidateSubFramebuffer(JNIEnv* env, jclass clazz, jint target, jint numAttachments, jobject obj_attachments, jint x, jint y, jint width, jint height) {
	int* attachments = (int*)(obj_attachments?env->GetDirectBufferAddress(obj_attachments):0);


//@line:1160

        glInvalidateSubFramebuffer(target, numAttachments, (GLenum*)attachments, x, y, width, height);
    

}

