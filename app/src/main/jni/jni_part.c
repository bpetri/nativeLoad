#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <libgen.h>
#include <curl/curl.h>
#include <android/log.h>
#include <unistd.h>

#include "framework.h"
#include "linked_list_iterator.h"

#define DEFAULT_CONFIG_FILE "config.properties"

#define  LOG_TAG    "celix"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


static JavaVM *gJavaVM;
static jobject gObject;
static jclass gClass;

typedef struct {
    const char* cbName;
    const char* cbSignature;
    jmethodID cbMethod;
} callback_t;

/*
 * Declaration of callbacks methods, with regard to the callback_t structure:
 */
callback_t cb[] = {
  {
          "confirmCelixStart", "()V",
  },
  {
          "confirmCelixStop", "()V",
  },
  {
          "confirmBundleInstalled", "(Ljava/lang/String;)V"
  },
  {
          "confirmBundleStart", "(Ljava/lang/String;)V"
  },
  {
          "confirmBundleStop", "(Ljava/lang/String;)V"
  },
  {
          "confirmBundleDeleted", "(Ljava/lang/String;)V"
  },
  {
          "confirmGetBundles", "([Ljava/lang/String;)V"
  },
 };

JNIEXPORT jboolean JNICALL Java_com_example_bjoern_nativeload_JNICommunicator_initJni(JNIEnv*, jobject);
JNIEXPORT jint JNICALL Java_com_example_bjoern_nativeload_JNICommunicator_startCelix(JNIEnv*, jclass, jstring);
JNIEXPORT jint JNICALL Java_com_example_bjoern_nativeload_JNICommunicator_stopCelix(JNIEnv*, jobject);
JNIEXPORT jint JNICALL Java_com_example_bjoern_nativeload_JNICommunicator_printCMessage(JNIEnv*, jobject);

int running = 0;

struct framework * framework;

static int pfd[2];
static pthread_t thr;
static const char *tag = "myapp";

jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv *env;
    gJavaVM = vm;
    LOGI("JNI_OnLoad called");
    start_logger("printf");
    if ( (*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("Failed to get the environment using GetEnv()");
        return -1;
    }
    return JNI_VERSION_1_4;
}

static int thread_func(void)
{
    ssize_t rdsz;
    char buf[512];
    while((rdsz = read(pfd[0], buf, sizeof buf - 1)) > 0) {
        if(buf[rdsz - 1] == '\n') --rdsz;
        buf[rdsz] = 0;  /* add null-terminator */
        __android_log_write(ANDROID_LOG_DEBUG, tag, buf);
    }
    return 0;
}

int start_logger(const char *app_name)
{
    tag = app_name;

    /* make stdout line-buffered and stderr unbuffered */
    setvbuf(stdout, 0, _IOLBF, 0);
    setvbuf(stderr, 0, _IONBF, 0);

    /* create the pipe and redirect stdout and stderr */
    pipe(pfd);
    dup2(pfd[1], 1);
    dup2(pfd[1], 2);

    /* spawn the logging thread */
    if(pthread_create(&thr, 0, thread_func, 0) == -1)
        return -1;
    pthread_detach(thr);
    return 0;
}

char *getBundleName(char* location) {
    char *copy = strdup(location);
    char result[256];
    char * pch;
    pch = strtok (copy,"/");
    while (pch != NULL)
    {
        strcpy(result, pch);
        pch = strtok (NULL, "/");
    }
    free(copy);
    return strdup(result);
}

void confirmCelixStart() {
   	JNIEnv* je;
	int isAttached = 0;
	int status = (*gJavaVM)->GetEnv(gJavaVM, (void **) &je, JNI_VERSION_1_4);

	if(status < 0) {
		status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &je, NULL);

		if(status < 0) {
            		LOGE("callback_handler: failed to attach current thread");
        	}
        	isAttached = 1;
	}


    	(*je)->CallVoidMethod(je, gObject, cb[0].cbMethod);

    	if(isAttached)
        	(*gJavaVM)->DetachCurrentThread(gJavaVM);
}


void confirmCelixStop() {
    JNIEnv* je;
    int isAttached = 0;
    int status = (*gJavaVM)->GetEnv(gJavaVM, (void **) &je, JNI_VERSION_1_4);

    if(status < 0) {
        status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &je, NULL);

        if(status < 0) {
            LOGE("callback_handler: failed to attach current thread");
        }
        isAttached = 1;
    }


    (*je)->CallVoidMethod(je, gObject, cb[1].cbMethod);

    if(isAttached)
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
}

void confirmInstallBundle(char* location) {
    LOGI("Bundle '%s' installed", getBundleName(location));
    JNIEnv* je;
    int isAttached = 0;
    int status = (*gJavaVM)->GetEnv(gJavaVM, (void **) &je, JNI_VERSION_1_4);

    if(status < 0) {
        status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &je, NULL);

        if(status < 0) {
            LOGE("callback_handler: failed to attach current thread");
        }
        isAttached = 1;
    }

    jstring jstr = (*je)->NewStringUTF(je,location);
    (*je)->CallVoidMethod(je, gObject, cb[2].cbMethod, jstr);

    if(isAttached)
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
}

void confirmStartBundle(char* location) {
    LOGI("Bundle '%s' started", getBundleName(location));
    JNIEnv* je;
    int isAttached = 0;
    int status = (*gJavaVM)->GetEnv(gJavaVM, (void **) &je, JNI_VERSION_1_4);

    if(status < 0) {
        status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &je, NULL);

        if(status < 0) {
            LOGE("callback_handler: failed to attach current thread");
        }
        isAttached = 1;
    }

    jstring jstr = (*je)->NewStringUTF(je,location);
    (*je)->CallVoidMethod(je, gObject, cb[3].cbMethod, jstr);
    if(isAttached)
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
}

void confirmStopBundle(char* location) {
    LOGI("Bundle '%s' stopped", getBundleName(location));
    JNIEnv* je;
    int isAttached = 0;
    int status = (*gJavaVM)->GetEnv(gJavaVM, (void **) &je, JNI_VERSION_1_4);

    if(status < 0) {
        status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &je, NULL);

        if(status < 0) {
            LOGE("callback_handler: failed to attach current thread");
        }
        isAttached = 1;
    }

    jstring jstr = (*je)->NewStringUTF(je,location);
    (*je)->CallVoidMethod(je, gObject, cb[4].cbMethod, jstr);

    if(isAttached)
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
}

void confirmDeleteBundle(char* location) {
    LOGI("Bundle '%s' deleted", getBundleName(location));
    JNIEnv* je;
    int isAttached = 0;
    int status = (*gJavaVM)->GetEnv(gJavaVM, (void **) &je, JNI_VERSION_1_4);

    if(status < 0) {
        status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &je, NULL);

        if(status < 0) {
            LOGE("callback_handler: failed to attach current thread");
        }
        isAttached = 1;
    }

    jstring jstr = (*je)->NewStringUTF(je,location);
    (*je)->CallVoidMethod(je, gObject, cb[5].cbMethod, jstr);

    if(isAttached)
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
}


void* installBundle(void* bundleLocation) {
    // Install bundle
    char* location = (char*) bundleLocation;
    LOGI("Installing bundle '%s'", getBundleName(location));
    bundle_pt fw_bundle = NULL;
    bundle_context_pt context = NULL;
    bundle_pt current = NULL;

    framework_getFrameworkBundle(framework, &fw_bundle);
    bundle_getContext(fw_bundle, &context);
    if (bundleContext_installBundle(context, location, &current) == CELIX_SUCCESS)
    {
        confirmInstallBundle(location);
    }
}

void* startBundle(void* bundleLocation) {
    char* location = (char*) bundleLocation;
    LOGI("Starting bundle '%s'", getBundleName(location));

    bundle_pt fw_bundle = NULL;
    bundle_pt current = NULL;

    current = framework_getBundle(framework, location);

    if (bundle_startWithOptions(current, 0) == CELIX_SUCCESS) {
        confirmStartBundle(location);
    }

}

void* stopBundle(void* bundleLocation) {
    char* location = (char*) bundleLocation;
    LOGI("Stopping bundle '%s'", getBundleName(location));

    bundle_pt current = NULL;

    current = framework_getBundle(framework, location);


    if (bundle_stopWithOptions(current, 0) == CELIX_SUCCESS) {
        confirmStopBundle(location);
    }

}

void* deleteBundle(void* bundleLocation) {
    // Install bundle
    char* location = (char*) bundleLocation;
    LOGI("Deleting bundle '%s'", getBundleName(location));

    bundle_pt current = NULL;

    current = framework_getBundle(framework, location);

    if (bundle_uninstall(current) == CELIX_SUCCESS) {
        confirmDeleteBundle(location);
    } else {
        LOGI("Failed to delete bundle %s", location);
    }
}


void* startCelix(void* param) {
	properties_pt config = NULL;
	char *autoStart = NULL;
    char *ownIP = NULL;
	char* propertyString = (char*) param;
    bundle_pt fwBundle = NULL;

	// Before doing anything else, let's setup Curl
	curl_global_init(CURL_GLOBAL_NOTHING);

    LOGI("received propertyString is %s", propertyString);

    config = properties_load(propertyString);

	// Make sure we've read it and that nothing went wrong with the file access...
	if (config == NULL) {

		LOGI("Error: invalid or non-existing configuration file: \"%s\"!\n", DEFAULT_CONFIG_FILE);
	}
    else {
        autoStart = properties_get(config, "cosgi.auto.start.1");
        ownIP = properties_get(config, "RSA_IP");
        LOGI("Device IP: '%s'", ownIP);
        framework = NULL;
        celix_status_t status = CELIX_SUCCESS;
        status = framework_create(&framework, config);
        if (status == CELIX_SUCCESS) {
            LOGI("framework sucessfully created");
            status = fw_init(framework);
            if (status == CELIX_SUCCESS) {

                LOGI("framework sucessfully initiated");
                // Start the system bundle
                framework_getFrameworkBundle(framework, &fwBundle);
                bundle_start(fwBundle);

                char delims[] = " ";
                char *result = NULL;
                char *save_ptr = NULL;
                linked_list_pt bundles;
                array_list_pt installed = NULL;
                bundle_pt bundle = NULL;
                bundle_context_pt context = NULL;
                linked_list_iterator_pt iter = NULL;
                unsigned int i;

                linkedList_create(&bundles);
                result = strtok_r(autoStart, delims, &save_ptr);
                while (result != NULL) {
                    char * location = strdup(result);
                    linkedList_addElement(bundles, location);
                    result = strtok_r(NULL, delims, &save_ptr);
                }
                // First install all bundles
                // Afterwards start them
                arrayList_create(&installed);
                framework_getFrameworkBundle(framework, &bundle);
                bundle_getContext(bundle, &context);
                iter = linkedListIterator_create(bundles, 0);
                while (linkedListIterator_hasNext(iter)) {
                    bundle_pt current = NULL;
                    char * location = (char *) linkedListIterator_next(iter);
                    if (bundleContext_installBundle(context, location, &current) == CELIX_SUCCESS) {
                        // Only add bundle if it is installed correctly
                        LOGI("bundle from %s sucessfully installed\n", location);
                        confirmInstallBundle(location);
                        arrayList_add(installed, current);
                        if (bundle_startWithOptions(current,0) == CELIX_SUCCESS) {
                            confirmStartBundle(location);
                        }

                    } else {
                        LOGI("Could not install bundle from %s\n", location);
                    }
                    linkedListIterator_remove(iter);
                }
                linkedListIterator_destroy(iter);
                linkedList_destroy(bundles);

                arrayList_destroy(installed);

		confirmCelixStart();

                framework_waitForStop(framework);
                framework_destroy(framework);
                framework = NULL;
            }
        }

        if (status != CELIX_SUCCESS) {
            LOGI("Problem creating framework\n");
        }
    }
	// Cleanup Curl
	curl_global_cleanup();

	confirmCelixStop();

}

void * confirmGetBundles(array_list_pt values) {
    JNIEnv* je;
    int isAttached = 0;
    int status = (*gJavaVM)->GetEnv(gJavaVM, (void **) &je, JNI_VERSION_1_4);

    if(status < 0) {
        status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &je, NULL);

        if(status < 0) {
            LOGE("callback_handler: failed to attach current thread");
        }
        isAttached = 1;
    }

    jclass stringClass = (*je)->FindClass(je,"java/lang/String");

    jobjectArray ret;
    if (values) {
        int i;
        ret = (*je)->NewObjectArray(je,arrayList_size(values),stringClass,0);
        for (i = 0; i < arrayList_size(values); i++) {
            (*je)->SetObjectArrayElement(je, ret, i, (*je)->NewStringUTF(je,arrayList_get(values,i)));
            free(arrayList_get(values,i));
            // Dont do anything with freed values.
        }
        arrayList_destroy(values);
        (*je)->CallVoidMethod(je, gObject, cb[6].cbMethod, ret);
    }

    if(isAttached)
        (*gJavaVM)->DetachCurrentThread(gJavaVM);

}
char * psCommand_stateString(bundle_state_e state);

void * printBundles(void* param) {
    if (framework != NULL) {
        array_list_pt bundles = framework_getBundles(framework);
        array_list_pt retvals;
        arrayList_create(&retvals);
        int i;
        for (i = 0; i < arrayList_size(bundles); i++) {
            bundle_pt bundle = (bundle_pt) arrayList_get(bundles, i);

            bundle_archive_pt archive = NULL;
            long id;
            bundle_state_e state;
            char * stateString = NULL;
            module_pt module = NULL;
            char * name = NULL;

            bundle_getArchive(bundle, &archive);
            bundleArchive_getId(archive, &id);
            bundle_getState(bundle, &state);
            bundle_getCurrentModule(bundle, &module);
            module_getSymbolicName(module, &name);
            stateString = psCommand_stateString(state);
            char str[100];
            snprintf(str, 100, "%ld %s %s", id, stateString, name);
            arrayList_add(retvals, strdup(str));
        }
        confirmGetBundles(retvals);

    }
    return NULL;
}

char * psCommand_stateString(bundle_state_e state) {
    switch (state) {
        case OSGI_FRAMEWORK_BUNDLE_ACTIVE:
            return "Active";
        case OSGI_FRAMEWORK_BUNDLE_INSTALLED:
            return "Installed";
        case OSGI_FRAMEWORK_BUNDLE_RESOLVED:
            return "Resolved";
        case OSGI_FRAMEWORK_BUNDLE_STARTING:
            return "Starting";
        case OSGI_FRAMEWORK_BUNDLE_STOPPING:
            return "Stopping";
        default:
            return "Unknown";
    }
}

void* stopCelix(void* param) {

    bundle_pt fwBundle = NULL;

    framework_getFrameworkBundle(framework, &fwBundle);

    //-----------------------
    module_pt module = NULL;
    char * name = NULL;

    bundle_getCurrentModule(fwBundle, &module);
    module_getSymbolicName(module, &name);
    LOGI("Stopping %s\n", name);
    //-----------------------


    bundle_stop(fwBundle);

    return 0;
}

JNIEXPORT jboolean JNICALL Java_com_inaetics_demonstrator_JNICommunicator_initJni(JNIEnv* je, jobject thiz)
{
	jboolean retVal = true;

	LOGI("init native function called");
	int status;
	int isAttached = 0;
	gObject = (jobject)(*je)->NewGlobalRef(je, thiz);
	jclass clazz = (*je)->GetObjectClass(je, thiz);
	gClass = (jclass)(*je)->NewGlobalRef(je, clazz);
	if (!clazz) {
		LOGE("callback_handler: failed to get object Class");
		retVal = false;
		goto failure;
	}
	int i = sizeof cb / sizeof cb[0];
	LOGI("getting methodIDs of %d configured callback methods", i);
	while(i--) {
		LOGI("Method %d is %s with signature %s", i, cb[i].cbName, cb[i].cbSignature);
		cb[i].cbMethod = (*je)->GetMethodID(je, clazz, cb[i].cbName, cb[i].cbSignature);
		if(!cb[i].cbMethod) {
			retVal = false;
			LOGE("callback_handler: failed to get method ID");
			goto failure;
		}
	}
failure:
	return retVal;
}

JNIEXPORT jint JNICALL Java_com_inaetics_demonstrator_JNICommunicator_printBundles(JNIEnv *env, jobject thiz)
{
    pthread_t thread;
    return pthread_create( &thread, NULL, printBundles, (void*) NULL);
}
JNIEXPORT jint JNICALL Java_com_inaetics_demonstrator_JNICommunicator_startCelix(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *propertyString = (*je)->GetStringUTFChars(je, i, NULL);
	pthread_t thread;
	return pthread_create( &thread, NULL, startCelix, (void*) propertyString);
}

JNIEXPORT jint JNICALL Java_com_inaetics_demonstrator_JNICommunicator_installBundle(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, installBundle, (void*) locationString);
}

JNIEXPORT jint JNICALL Java_com_inaetics_demonstrator_JNICommunicator_startBundle(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, startBundle, (void*) locationString);
}

JNIEXPORT jint JNICALL Java_com_inaetics_demonstrator_JNICommunicator_stopBundle(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, stopBundle, (void*) locationString);
}

JNIEXPORT jint JNICALL Java_com_inaetics_demonstrator_JNICommunicator_deleteBundle(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, deleteBundle, (void*) locationString);
}

JNIEXPORT jint JNICALL Java_com_inaetics_demonstrator_JNICommunicator_stopCelix(JNIEnv* je, jobject thiz)
{
    // convert Java string to UTF-8
//    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, stopCelix, (void*) NULL);
}


