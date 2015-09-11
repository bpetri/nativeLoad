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
 };



JNIEXPORT jboolean JNICALL Java_com_example_bjoern_nativeload_MainActivity_initJni(JNIEnv*, jobject);
JNIEXPORT jint JNICALL Java_com_example_bjoern_nativeload_MainActivity_startCelix(JNIEnv*, jclass, jstring);
JNIEXPORT jint JNICALL Java_com_example_bjoern_nativeload_MainActivity_stopCelix(JNIEnv*, jobject);
JNIEXPORT jint JNICALL Java_com_example_bjoern_nativeload_MainActivity_printCMessage(JNIEnv*, jobject);

int running = 0;

struct framework * framework;


jint JNI_OnLoad(JavaVM* vm, void* reserved)
{
    JNIEnv *env;
    gJavaVM = vm;
    LOGI("JNI_OnLoad called");
    if ( (*vm)->GetEnv(vm, (void**) &env, JNI_VERSION_1_4) != JNI_OK) {
        LOGE("Failed to get the environment using GetEnv()");
        return -1;
    }
    return JNI_VERSION_1_4;
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


void* startCelix(void* param) {
	properties_pt config = NULL;
	char *autoStart = NULL;
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
                        //----------------------

                        bundle_archive_pt archive = NULL;
                        long id;
                        char * stateString = NULL;
                        module_pt module = NULL;
                        char * name = NULL;

                        bundle_getArchive(current, &archive);
                        bundleArchive_getId(archive, &id);
                        bundle_getCurrentModule(current, &module);
                        module_getSymbolicName(module, &name);
                        LOGI("  %-5ld %-12s %s\n", id, stateString, name);

                        //----------------------
                        arrayList_add(installed, current);
                    } else {
                        LOGI("Could not install bundle from %s\n", location);
                    }
                    linkedListIterator_remove(iter);
                }
                linkedListIterator_destroy(iter);
                linkedList_destroy(bundles);

                for (i = 0; i < arrayList_size(installed); i++) {
                    bundle_pt bundle = (bundle_pt) arrayList_get(installed, i);
                    bundle_startWithOptions(bundle, 0);
                }

                arrayList_destroy(installed);

		confirmCelixStart();

                framework_waitForStop(framework);
                framework_destroy(framework);
                properties_destroy(config);
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





JNIEXPORT jboolean JNICALL Java_com_example_bjoern_nativeload_MainActivity_initJni(JNIEnv* je, jobject thiz)
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


JNIEXPORT jint JNICALL Java_com_example_bjoern_nativeload_MainActivity_startCelix(JNIEnv* je, jclass jc, jstring i)
{
    	// convert Java string to UTF-8
    printf("Dit is een message van C");
    	const char *propertyString = (*je)->GetStringUTFChars(je, i, NULL);
	pthread_t thread;
	return pthread_create( &thread, NULL, startCelix, (void*) propertyString);
}


JNIEXPORT jint JNICALL Java_com_example_bjoern_nativeload_MainActivity_stopCelix(JNIEnv* je, jobject thiz)
{
	bundle_pt fwBundle = NULL;
	
	framework_getFrameworkBundle(framework, &fwBundle);

    //-----------------------
    bundle_archive_pt archive = NULL;
    long id;
    char * stateString = NULL;
    module_pt module = NULL;
    char * name = NULL;

    bundle_getArchive(fwBundle, &archive);
    bundleArchive_getId(archive, &id);
    bundle_getCurrentModule(fwBundle, &module);
    module_getSymbolicName(module, &name);
    LOGI("Stopping  %-5ld %s\n", id, name);
    //-----------------------


	bundle_stop(fwBundle);
        //framework_destroy(framework);
	
	return 0;
}

JNIEXPORT jint JNICALL Java_com_example_bjoern_nativeload_MainActivity_printCMessage(JNIEnv *env, jobject obj) {
    int pipes[2];
    pipe(pipes);
    dup2(pipes[1], STDOUT_FILENO);
    FILE *inputFile = fdopen(pipes[0], "r");
    char readBuffer[256];
    while (1) {
        fgets(readBuffer, sizeof(readBuffer), inputFile);
        __android_log_write(2, "stdout", readBuffer);
    }
}


