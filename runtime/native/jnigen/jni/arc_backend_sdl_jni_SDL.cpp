#include <arc_backend_sdl_jni_SDL.h>
#include <arc_backend_sdl_jni_SDLGL.h>

//@line:9


    #include "SDL.h"

    JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1Init(JNIEnv* env, jclass clazz, jint flags) {


//@line:133

        return SDL_Init(flags);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1InitSubSystem(JNIEnv* env, jclass clazz, jint flags) {


//@line:137

        return SDL_InitSubSystem(flags);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1QuitSubSystem(JNIEnv* env, jclass clazz, jint flags) {


//@line:141

        SDL_QuitSubSystem(flags);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1WasInit(JNIEnv* env, jclass clazz, jint flags) {


//@line:145

        return SDL_WasInit(flags);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1Quit(JNIEnv* env, jclass clazz) {


//@line:149

        SDL_Quit();
    

}

static inline jboolean wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1SetHint
(JNIEnv* env, jclass clazz, jstring obj_name, jstring obj_value, char* name, char* value) {

//@line:153

       return (SDL_SetHint(name, value)==SDL_TRUE);
    
}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetHint(JNIEnv* env, jclass clazz, jstring obj_name, jstring obj_value) {
	char* name = (char*)env->GetStringUTFChars(obj_name, 0);
	char* value = (char*)env->GetStringUTFChars(obj_value, 0);

	jboolean JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1SetHint(env, clazz, obj_name, obj_value, name, value);

	env->ReleaseStringUTFChars(obj_name, name);
	env->ReleaseStringUTFChars(obj_value, value);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetCompiledVersion(JNIEnv* env, jclass clazz, jintArray obj_values) {
	int* values = (int*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:157

        SDL_version compiled;
        SDL_VERSION(&compiled);
        values[0] = compiled.major;
        values[1] = compiled.minor;
        values[2] = compiled.patch;
    
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetVersion(JNIEnv* env, jclass clazz, jintArray obj_values) {
	int* values = (int*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:165

        SDL_version compiled;
        SDL_GetVersion(&compiled);
        values[0] = compiled.major;
        values[1] = compiled.minor;
        values[2] = compiled.patch;
    
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

JNIEXPORT jstring JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetError(JNIEnv* env, jclass clazz) {


//@line:173

        return env->NewStringUTF(SDL_GetError());
    

}

static inline jint wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1SetClipboardText
(JNIEnv* env, jclass clazz, jstring obj_text, char* text) {

//@line:177

        return SDL_SetClipboardText(text);
    
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetClipboardText(JNIEnv* env, jclass clazz, jstring obj_text) {
	char* text = (char*)env->GetStringUTFChars(obj_text, 0);

	jint JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1SetClipboardText(env, clazz, obj_text, text);

	env->ReleaseStringUTFChars(obj_text, text);

	return JNI_returnValue;
}

JNIEXPORT jstring JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetClipboardText(JNIEnv* env, jclass clazz) {


//@line:181

        return env->NewStringUTF(SDL_GetClipboardText());
    

}

static inline jlong wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1CreateWindow
(JNIEnv* env, jclass clazz, jstring obj_title, jint w, jint h, jint flags, char* title) {

//@line:185

        return (jlong)SDL_CreateWindow(title, SDL_WINDOWPOS_UNDEFINED, SDL_WINDOWPOS_UNDEFINED, w, h, flags);
    
}

JNIEXPORT jlong JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1CreateWindow(JNIEnv* env, jclass clazz, jstring obj_title, jint w, jint h, jint flags) {
	char* title = (char*)env->GetStringUTFChars(obj_title, 0);

	jlong JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1CreateWindow(env, clazz, obj_title, w, h, flags, title);

	env->ReleaseStringUTFChars(obj_title, title);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1DestroyWindow(JNIEnv* env, jclass clazz, jlong handle) {


//@line:189

        SDL_DestroyWindow((SDL_Window*)handle);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetWindowIcon(JNIEnv* env, jclass clazz, jlong handle, jlong surface) {


//@line:193

        SDL_SetWindowIcon((SDL_Window*)handle, (SDL_Surface*)surface);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1RestoreWindow(JNIEnv* env, jclass clazz, jlong handle) {


//@line:197

        SDL_RestoreWindow((SDL_Window*)handle);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1MaximizeWindow(JNIEnv* env, jclass clazz, jlong handle) {


//@line:201

        SDL_MaximizeWindow((SDL_Window*)handle);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1MinimizeWindow(JNIEnv* env, jclass clazz, jlong handle) {


//@line:205

        SDL_MinimizeWindow((SDL_Window*)handle);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetWindowFullscreen(JNIEnv* env, jclass clazz, jlong handle, jint flags) {


//@line:209

        return SDL_SetWindowFullscreen((SDL_Window*)handle, flags);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetWindowBordered(JNIEnv* env, jclass clazz, jlong handle, jboolean bordered) {


//@line:213

        SDL_SetWindowBordered((SDL_Window*)handle, (SDL_bool)bordered);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetWindowSize(JNIEnv* env, jclass clazz, jlong handle, jint w, jint h) {


//@line:217

        SDL_SetWindowSize((SDL_Window*)handle, w, h);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetWindowPosition(JNIEnv* env, jclass clazz, jlong handle, jint x, jint y) {


//@line:221

        SDL_SetWindowPosition((SDL_Window*)handle, x, y);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetWindowDisplayIndex(JNIEnv* env, jclass clazz, jlong handle) {


//@line:225

        return SDL_GetWindowDisplayIndex((SDL_Window*)handle);
    

}

static inline jint wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1GetDisplayUsableBounds
(JNIEnv* env, jclass clazz, jint display, jintArray obj_xywh, int* xywh) {

//@line:229

        SDL_Rect bounds;
        int result = SDL_GetDisplayUsableBounds(display, &bounds);

        xywh[0] = bounds.x;
        xywh[1] = bounds.y;
        xywh[2] = bounds.w;
        xywh[3] = bounds.h;

        return result;
    
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetDisplayUsableBounds(JNIEnv* env, jclass clazz, jint display, jintArray obj_xywh) {
	int* xywh = (int*)env->GetPrimitiveArrayCritical(obj_xywh, 0);

	jint JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1GetDisplayUsableBounds(env, clazz, display, obj_xywh, xywh);

	env->ReleasePrimitiveArrayCritical(obj_xywh, xywh, 0);

	return JNI_returnValue;
}

static inline jint wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1GetDisplayBounds
(JNIEnv* env, jclass clazz, jint display, jintArray obj_xywh, int* xywh) {

//@line:241

        SDL_Rect bounds;
        int result = SDL_GetDisplayBounds(display, &bounds);

        xywh[0] = bounds.x;
        xywh[1] = bounds.y;
        xywh[2] = bounds.w;
        xywh[3] = bounds.h;

        return result;
    
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetDisplayBounds(JNIEnv* env, jclass clazz, jint display, jintArray obj_xywh) {
	int* xywh = (int*)env->GetPrimitiveArrayCritical(obj_xywh, 0);

	jint JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1GetDisplayBounds(env, clazz, display, obj_xywh, xywh);

	env->ReleasePrimitiveArrayCritical(obj_xywh, xywh, 0);

	return JNI_returnValue;
}

static inline jint wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1GetCurrentDisplayMode
(JNIEnv* env, jclass clazz, jint display, jintArray obj_wh, int* wh) {

//@line:253

        SDL_DisplayMode mode;
        int result = SDL_GetCurrentDisplayMode(display, &mode);

        wh[0] = mode.w;
        wh[1] = mode.h;

        return result;
    
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetCurrentDisplayMode(JNIEnv* env, jclass clazz, jint display, jintArray obj_wh) {
	int* wh = (int*)env->GetPrimitiveArrayCritical(obj_wh, 0);

	jint JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1GetCurrentDisplayMode(env, clazz, display, obj_wh, wh);

	env->ReleasePrimitiveArrayCritical(obj_wh, wh, 0);

	return JNI_returnValue;
}

static inline jint wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1GetDesktopDisplayMode
(JNIEnv* env, jclass clazz, jint display, jintArray obj_wh, int* wh) {

//@line:263

        SDL_DisplayMode mode;
        int result = SDL_GetDesktopDisplayMode(display, &mode);

        wh[0] = mode.w;
        wh[1] = mode.h;

        return result;
    
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetDesktopDisplayMode(JNIEnv* env, jclass clazz, jint display, jintArray obj_wh) {
	int* wh = (int*)env->GetPrimitiveArrayCritical(obj_wh, 0);

	jint JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1GetDesktopDisplayMode(env, clazz, display, obj_wh, wh);

	env->ReleasePrimitiveArrayCritical(obj_wh, wh, 0);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetWindowAlwaysOnTop(JNIEnv* env, jclass clazz, jlong handle, jboolean onTop) {


//@line:273

        SDL_SetWindowAlwaysOnTop((SDL_Window*)handle, (SDL_bool)onTop);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetNumVideoDisplays(JNIEnv* env, jclass clazz) {


//@line:277

        return SDL_GetNumVideoDisplays();
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GetWindowFlags(JNIEnv* env, jclass clazz, jlong handle) {


//@line:281

        return SDL_GetWindowFlags((SDL_Window*)handle);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetWindowTitle(JNIEnv* env, jclass clazz, jlong handle, jstring obj_title) {
	char* title = (char*)env->GetStringUTFChars(obj_title, 0);


//@line:285

        SDL_SetWindowTitle((SDL_Window*)handle, title);
    
	env->ReleaseStringUTFChars(obj_title, title);

}

static inline jlong wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1CreateRGBSurfaceFrom
(JNIEnv* env, jclass clazz, jobject obj_bytes, jint width, jint height, char* bytes) {

//@line:290

        return (jlong)SDL_CreateRGBSurfaceFrom(bytes, width, height, 32, 4 * width, 0x000000ff, 0x0000ff00, 0x00ff0000, 0xff000000);
    
}

JNIEXPORT jlong JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1CreateRGBSurfaceFrom(JNIEnv* env, jclass clazz, jobject obj_bytes, jint width, jint height) {
	char* bytes = (char*)(obj_bytes?env->GetDirectBufferAddress(obj_bytes):0);

	jlong JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1CreateRGBSurfaceFrom(env, clazz, obj_bytes, width, height, bytes);


	return JNI_returnValue;
}

JNIEXPORT jlong JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1CreateColorCursor(JNIEnv* env, jclass clazz, jlong surface, jint hotx, jint hoty) {


//@line:294

        return (jlong)SDL_CreateColorCursor((SDL_Surface*)surface, hotx, hoty);
    

}

JNIEXPORT jlong JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1CreateSystemCursor(JNIEnv* env, jclass clazz, jint type) {


//@line:298

        return (jlong)SDL_CreateSystemCursor((SDL_SystemCursor)type);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetCursor(JNIEnv* env, jclass clazz, jlong handle) {


//@line:302

        SDL_SetCursor((SDL_Cursor*)handle);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1FreeCursor(JNIEnv* env, jclass clazz, jlong handle) {


//@line:306

        SDL_FreeCursor((SDL_Cursor*)handle);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1FreeSurface(JNIEnv* env, jclass clazz, jlong handle) {


//@line:310

        SDL_FreeSurface((SDL_Surface*)handle);
     

}

static inline jint wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1ShowSimpleMessageBox
(JNIEnv* env, jclass clazz, jint flags, jstring obj_title, jstring obj_message, char* title, char* message) {

//@line:314

        return SDL_ShowSimpleMessageBox(flags, title, message, NULL);
    
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1ShowSimpleMessageBox(JNIEnv* env, jclass clazz, jint flags, jstring obj_title, jstring obj_message) {
	char* title = (char*)env->GetStringUTFChars(obj_title, 0);
	char* message = (char*)env->GetStringUTFChars(obj_message, 0);

	jint JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1ShowSimpleMessageBox(env, clazz, flags, obj_title, obj_message, title, message);

	env->ReleaseStringUTFChars(obj_title, title);
	env->ReleaseStringUTFChars(obj_message, message);

	return JNI_returnValue;
}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1StartTextInput(JNIEnv* env, jclass clazz) {


//@line:318

        SDL_StartTextInput();
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1StopTextInput(JNIEnv* env, jclass clazz) {


//@line:322

        SDL_StopTextInput();
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1SetTextInputRect(JNIEnv* env, jclass clazz, jint x, jint y, jint w, jint h) {


//@line:326

        SDL_Rect rect;
        rect.x = x;
        rect.y = y;
        rect.w = w;
        rect.h = h;
        SDL_SetTextInputRect(&rect);
    

}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1IsTextInputActive(JNIEnv* env, jclass clazz) {


//@line:335

        return (jboolean)SDL_IsTextInputActive();
    

}

static inline jboolean wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1PollEvent
(JNIEnv* env, jclass clazz, jintArray obj_data, int* data) {

//@line:342

        SDL_Event e;
        if(SDL_PollEvent(&e)){
            switch(e.type){
                case SDL_QUIT:
                    data[0] = 0;
                    break;
                case SDL_WINDOWEVENT:
                    data[0] = 1;
                    data[1] = e.window.event;
                    data[2] = e.window.data1;
                    data[3] = e.window.data2;
                    break;
                case SDL_MOUSEMOTION:
                    data[0] = 2;
                    data[1] = e.motion.x;
                    data[2] = e.motion.y;
                    break;
                case SDL_MOUSEBUTTONDOWN:
                case SDL_MOUSEBUTTONUP:
                    data[0] = 3;
                    data[1] = (e.type == SDL_MOUSEBUTTONDOWN);
                    data[2] = e.button.x;
                    data[3] = e.button.y;
                    data[4] = e.button.button;
                    break;
                case SDL_MOUSEWHEEL:
                    data[0] = 4;
                    data[1] = e.wheel.x;
                    data[2] = e.wheel.y;
                    break;
                case SDL_KEYDOWN:
                case SDL_KEYUP:
                    data[0] = 5;
                    data[1] = (e.type == SDL_KEYDOWN);
                    data[2] = e.key.keysym.sym;
                    data[3] = e.key.repeat;
                    data[4] = e.key.keysym.scancode;
                    data[5] = e.key.keysym.mod;
                    data[6] = e.key.timestamp;
                    break;
                case SDL_TEXTINPUT:
                    data[0] = 6;
                    for(int i = 0; i < 32; i ++){
                        data[i + 1] = e.text.text[i];
                        if(e.text.text[i] == '\0'){
                            break;
                        }
                    }
                    break;
                case SDL_TEXTEDITING:
                    data[0] = 8;
                    data[1] = e.edit.start;
                    data[2] = e.edit.length;
                    for(int i = 0; i < 32; i ++){
                        data[i + 3] = e.edit.text[i];
                        if(e.edit.text[i] == '\0'){
                            break;
                        }
                    }

                    break;
                default:
                    data[0] = 7;
                    break;
            }
            return 1;
        }
        return 0;
    
}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1PollEvent(JNIEnv* env, jclass clazz, jintArray obj_data) {
	int* data = (int*)env->GetPrimitiveArrayCritical(obj_data, 0);

	jboolean JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1PollEvent(env, clazz, obj_data, data);

	env->ReleasePrimitiveArrayCritical(obj_data, data, 0);

	return JNI_returnValue;
}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GL_1SetAttribute(JNIEnv* env, jclass clazz, jint attribute, jint value) {


//@line:413

        return SDL_GL_SetAttribute((SDL_GLattr)attribute, value);
    

}

static inline jboolean wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1GL_1ExtensionSupported
(JNIEnv* env, jclass clazz, jstring obj_exte, char* exte) {

//@line:417

        return SDL_GL_ExtensionSupported(exte);
    
}

JNIEXPORT jboolean JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GL_1ExtensionSupported(JNIEnv* env, jclass clazz, jstring obj_exte) {
	char* exte = (char*)env->GetStringUTFChars(obj_exte, 0);

	jboolean JNI_returnValue = wrapped_Java_arc_backend_sdl_jni_SDL_SDL_1GL_1ExtensionSupported(env, clazz, obj_exte, exte);

	env->ReleaseStringUTFChars(obj_exte, exte);

	return JNI_returnValue;
}

JNIEXPORT jlong JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GL_1CreateContext(JNIEnv* env, jclass clazz, jlong window) {


//@line:421

        return (jlong)SDL_GL_CreateContext((SDL_Window*)window);
    

}

JNIEXPORT jint JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GL_1SetSwapInterval(JNIEnv* env, jclass clazz, jint on) {


//@line:425

        return SDL_GL_SetSwapInterval(on);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GL_1SwapWindow(JNIEnv* env, jclass clazz, jlong window) {


//@line:429

        SDL_GL_SwapWindow((SDL_Window*)window);
    

}

JNIEXPORT void JNICALL Java_arc_backend_sdl_jni_SDL_SDL_1GL_1GetDrawableSize(JNIEnv* env, jclass clazz, jlong window, jintArray obj_values) {
	int* values = (int*)env->GetPrimitiveArrayCritical(obj_values, 0);


//@line:433

        int w, h;
        SDL_GL_GetDrawableSize((SDL_Window*)window, &w, &h);
        values[0] = w;
        values[1] = h;
    
	env->ReleasePrimitiveArrayCritical(obj_values, values, 0);

}

