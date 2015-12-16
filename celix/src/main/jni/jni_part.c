#include <jni.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <libgen.h>
#include <curl/curl.h>
#include <android/log.h>
#include <unistd.h>
#include <android/sensor.h>
#include <android/looper.h>

#include "framework.h"
#include "linked_list_iterator.h"



#define DEFAULT_CONFIG_FILE "config.properties"

#define  LOG_TAG    "celix"
#define  LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define  LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)


static JavaVM *gJavaVM;
static jobject gObject;

int running = 0;

typedef struct {
    const char* cbName;
    const char* cbSignature;
    jmethodID cbMethod;
} callback_t;

callback_t cb[] = {
        {
                "confirmLogChanged", "(Ljava/lang/String;)V",
        },
        {
                "bundleChanged", "(Ljava/lang/String;)V",
        },
        {
                "setCelixRunning", "(Z)V",
        },
};

struct framework * framework;

static int pfd[2];
static pthread_t thr;
static const char *tag = "myapp";

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
void confirmLogChanged(char* buf) {
    JNIEnv* je;
    int isAttached = 0;
    int status = (*gJavaVM)->GetEnv(gJavaVM, (void**) &je, JNI_VERSION_1_4);
    if(status < 0) {
        status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &je, NULL);

        if(status < 0) {
            LOGE("callback_handler: failed to attach current thread");
        }
        isAttached = 1;
    }

    jstring jstr = (*je)->NewStringUTF(je, buf);
    (*je)->CallVoidMethod(je, gObject, cb[0].cbMethod, jstr);

    if(isAttached)
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
}

static int thread_func(void)
{
    ssize_t rdsz;
    char buf[512];
    while((rdsz = read(pfd[0], buf, sizeof buf - 1)) > 0) {
        if(buf[rdsz - 1] == '\n') --rdsz;
        buf[rdsz] = 0;  /* add null-terminator */
        __android_log_write(ANDROID_LOG_DEBUG, tag, buf);
        confirmLogChanged(buf);
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

void* installBundle(void* bundleLocation) {
    // Install bundle
    char* location = (char*) bundleLocation;
    printf("Installing bundle '%s' \n", getBundleName(location));
    bundle_pt fw_bundle = NULL;
    bundle_context_pt context = NULL;
    bundle_pt current = NULL;

    framework_getFrameworkBundle(framework, &fw_bundle);
    bundle_getContext(fw_bundle, &context);
    if (bundleContext_installBundle(context, location, &current) != CELIX_SUCCESS)
    {
        printf("Failed to install bundle %s \n", location);
    }
}

void* installStartBundle(void* bundleLocation) {
    // Install bundle
    char* location = (char*) bundleLocation;
    printf("Installing bundle '%s' \n", getBundleName(location));
    bundle_pt fw_bundle = NULL;
    bundle_context_pt context = NULL;
    bundle_pt current = NULL;

    framework_getFrameworkBundle(framework, &fw_bundle);
    bundle_getContext(fw_bundle, &context);
    if (bundleContext_installBundle(context, location, &current) != CELIX_SUCCESS)
    {
        printf("Failed to install bundle %s \n", location);
    } else {
        if (bundle_startWithOptions(current, 0) != CELIX_SUCCESS) {
            printf("Failed to start bundle %s \n", location);
        }
    }
}


void* startBundle(void* bundleLocation) {
    char* location = (char*) bundleLocation;
    printf("Starting bundle '%s' \n", getBundleName(location));

    bundle_pt fw_bundle = NULL;
    bundle_pt current = NULL;

    current = framework_getBundle(framework, location);

    if (bundle_startWithOptions(current, 0) != CELIX_SUCCESS) {
        printf("Failed to start bundle %s \n", location);
    }

}

void* startBundleById(long id) {
    printf("Starting bundle with id %ld \n", id);
    bundle_pt fw_bundle = NULL;
    bundle_context_pt context = NULL;
    bundle_pt bundle = NULL;

    framework_getFrameworkBundle(framework, &fw_bundle);
    bundle_getContext(fw_bundle, &context);

    bundleContext_getBundleById(context, id, &bundle);
    if (bundle_startWithOptions(bundle,0) != CELIX_SUCCESS) {
        printf("Starting bundle with id %ld failed \n", id);
    }
}

void* stopBundle(void* bundleLocation) {
    char* location = (char*) bundleLocation;
    printf("Stopping bundle '%s' \n", getBundleName(location));

    bundle_pt current = NULL;

    current = framework_getBundle(framework, location);


    if (bundle_stopWithOptions(current, 0) != CELIX_SUCCESS) {
        printf("Failed to stop bundle %s \n", location);
    }

}

void* stopBundleById(long id) {
    printf("Stopping bundle with id %ld \n", id);

    bundle_pt bundle = framework_getBundleById(framework, id);

    if (bundle_stopWithOptions(bundle, 0) != CELIX_SUCCESS) {
        printf("Stopping bundle with id %ld failed \n", id);
    }


}

void* deleteBundle(void* bundleLocation) {
    // Install bundle
    char* location = (char*) bundleLocation;
    printf("Deleting bundle '%s' \n", getBundleName(location));

    bundle_pt current = NULL;

    current = framework_getBundle(framework, location);

    if (bundle_uninstall(current) != CELIX_SUCCESS) {
        printf("Failed to delete bundle %s \n", location);
    }
}

void* deleteBundleById(long id) {
    printf("Deleting bundle with id %ld \n", id);

    bundle_pt bundle = framework_getBundleById(framework, id);

    if (bundle_uninstall(bundle) != CELIX_SUCCESS) {
        printf("Delete bundle with id %ld failed \n", id);
    }

}

char * psCommand_stateString(bundle_state_e state);

void* callback_to_bundleChanged(long id, bundle_state_e state, char * name, char * location) {
    char * stateString = psCommand_stateString(state);

    char str[256];
    snprintf(str, 256, "%ld %s %s %s", id, stateString, name, location);
    puts(str);
    JNIEnv* je;
    int isAttached = 0;
    int status = (*gJavaVM)->GetEnv(gJavaVM, (void**) &je, JNI_VERSION_1_4);
    if(status < 0) {
        status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &je, NULL);

        if(status < 0) {
            LOGE("callback_handler: failed to attach current thread");
        }
        isAttached = 1;
    }

    jstring jstr = (*je)->NewStringUTF(je, str);
    (*je)->CallVoidMethod(je, gObject, cb[1].cbMethod, jstr);

    if(isAttached)
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
}

void* log_changedBundle(void *listener, bundle_event_pt event)
{
    bundle_event_type_e type = event->type;
    long id = event->bundleId;
    char * name = event->bundleSymbolicName;
    char * location = "";

    if (type == OSGI_FRAMEWORK_BUNDLE_EVENT_INSTALLED) {
        bundle_pt bundle = framework_getBundleById(framework, id);

        if (bundle) {
            bundle_archive_pt archive = NULL;
            bundle_getArchive(bundle, &archive);
            if (archive) {
                bundleArchive_getLocation(archive, &location);
            }
        }
    }

    callback_to_bundleChanged(id, type, name, location);
}

void* callback_celix_changed(bool is_running) {
    JNIEnv* je;
    int isAttached = 0;
    int status = (*gJavaVM)->GetEnv(gJavaVM, (void**) &je, JNI_VERSION_1_4);
    if(status < 0) {
        status = (*gJavaVM)->AttachCurrentThread(gJavaVM, &je, NULL);

        if(status < 0) {
            LOGE("callback_handler: failed to attach current thread");
        }
        isAttached = 1;
    }

    jboolean result = false;
    if(is_running) {
        result = true;
    }
    (*je)->CallVoidMethod(je, gObject, cb[2].cbMethod, result);

    if(isAttached)
        (*gJavaVM)->DetachCurrentThread(gJavaVM);
}
/*
void* test_producerAdded(void *handle, service_reference_pt reference, void *service) {
    producer_service_pt producerService = (producer_service_pt)service;
    producerService->setSampleRate(producerService->producer, 10);
}

void* test_producerRemoved(void *handle, service_reference_pt reference, void *service) {

}
 */


void* startCelix(void* param) {
	properties_pt config = NULL;
	char *autoStart = NULL;
    char *ownIP = NULL;
	char* propertyString = (char*) param;
    bundle_pt fwBundle = NULL;

	// Before doing anything else, let's setup Curl
	curl_global_init(CURL_GLOBAL_NOTHING);

    printf("received propertyString is %s \n", propertyString);

    config = properties_load(propertyString);

	// Make sure we've read it and that nothing went wrong with the file access...
	if (config == NULL) {

		printf("Error: invalid or non-existing configuration file: \"%s\"!\n", DEFAULT_CONFIG_FILE);
	}
    else {
        autoStart = properties_get(config, "cosgi.auto.start.1");
        ownIP = properties_get(config, "RSA_IP");
        printf("Device IP: '%s' \n", ownIP);
        framework = NULL;
        celix_status_t status = CELIX_SUCCESS;
        status = framework_create(&framework, config);
        if (status == CELIX_SUCCESS) {
            printf("framework succesfully created \n");
            status = fw_init(framework);
            if (status == CELIX_SUCCESS) {

                printf("framework succesfully initiated \n");
                // Start the system bundle
                framework_getFrameworkBundle(framework, &fwBundle);

                bundle_context_pt fwbundlecontext = NULL;
                bundle_getContext(fwBundle, &fwbundlecontext);
                //Add listeners to bundle- and framework events
                bundle_listener_pt bundleListener = calloc(1,sizeof(bundleListener));
                bundleListener->bundleChanged = log_changedBundle;
                bundleContext_addBundleListener(fwbundlecontext, bundleListener);

                bundle_start(fwBundle);

                /*
                service_tracker_pt producerTracker = NULL;
                service_tracker_customizer_pt producerCustomizer = NULL;
                serviceTrackerCustomizer_create(fwbundlecontext, NULL, test_producerAdded, NULL, test_producerRemoved, &producerCustomizer);
                serviceTracker_create(fwbundlecontext, INAETICS_DEMONSTRATOR_API__PRODUCER_SERVICE_NAME, producerCustomizer, producerTracker);
                serviceTracker_open(producerTracker);
                */

                callback_celix_changed(true);

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
                        printf("bundle from %s sucessfully installed\n", location);
                        arrayList_add(installed, current);
                        if (bundle_startWithOptions(current,0) == CELIX_SUCCESS) {
                            printf("bundle from %s succesfully started\n", location);
                        } else {
                            printf("failed to start bundle from %s\n", location);
                        }
                    } else {
                        printf("Could not install bundle from %s\n", location);
                    }
                    linkedListIterator_remove(iter);
                }
                linkedListIterator_destroy(iter);
                linkedList_destroy(bundles);

                arrayList_destroy(installed);

                framework_waitForStop(framework);
                framework_destroy(framework);
                framework = NULL;
                callback_celix_changed(false);

            }
        }

        if (status != CELIX_SUCCESS) {
            printf("Problem creating framework\n");
        }
    }
	// Cleanup Curl
	curl_global_cleanup();

}

array_list_pt printBundles() {
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
            char * location = NULL;

            bundle_getArchive(bundle, &archive);
            bundleArchive_getId(archive, &id);
            bundleArchive_getLocation(archive, &location);
            bundle_getState(bundle, &state);
            bundle_getCurrentModule(bundle, &module);
            module_getSymbolicName(module, &name);
            stateString = psCommand_stateString(state);

            char str[512];
            snprintf(str, 512, "%ld %s %s %s", id,  stateString, name, location);
            arrayList_add(retvals, strdup(str));
        }
        return retvals;

    }
    return NULL;
}

char * psCommand_stateString(bundle_state_e state) {
    switch (state) {
        case OSGI_FRAMEWORK_BUNDLE_EVENT_STARTED:
            return "Active";
        case OSGI_FRAMEWORK_BUNDLE_EVENT_INSTALLED:
            return "Installed";
        case OSGI_FRAMEWORK_BUNDLE_EVENT_RESOLVED:
            return "Resolved";
        case OSGI_FRAMEWORK_BUNDLE_EVENT_STARTING:
            return "Starting";
        case OSGI_FRAMEWORK_BUNDLE_EVENT_STOPPING:
            return "Stopping";
        case OSGI_FRAMEWORK_BUNDLE_EVENT_STOPPED:
            return "Resolved";
        case OSGI_FRAMEWORK_BUNDLE_EVENT_UNINSTALLED:
            return "Deleted";
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
    printf("Stopping %s\n", name);
    //-----------------------


    bundle_stop(fwBundle);

    return 0;
}
JNIEXPORT jint JNICALL Java_apache_celix_Celix_initCallback(JNIEnv* je, jobject thiz)
{
    gObject = (jobject)(*je)->NewGlobalRef(je,thiz);
    jclass clazz = (*je)->GetObjectClass(je, thiz);
    if (!clazz) {
        LOGE("Callback handler : failed to get object class");
    } else {
        int i = sizeof cb / sizeof cb[0];
        while(i--) {
            cb[i].cbMethod = (*je)->GetMethodID(je, clazz, cb[i].cbName, cb[i].cbSignature);
        }
    }
    start_logger("printf");
}




JNIEXPORT jobjectArray JNICALL Java_apache_celix_Celix_printBundles(JNIEnv *je, jobject thiz)
{
    pthread_t thread;
    array_list_pt values = printBundles();
    jclass stringClass = (*je)->FindClass(je,"java/lang/String");

    jobjectArray ret;
    if (values) {
        int i;
        ret = (*je)->NewObjectArray(je, arrayList_size(values), stringClass, 0);
        for (i = 0; i < arrayList_size(values); i++) {
            (*je)->SetObjectArrayElement(je, ret, i,
                                         (*je)->NewStringUTF(je, arrayList_get(values, i)));
            free(arrayList_get(values, i));
            // Dont do anything with freed values.
        }
        arrayList_destroy(values);
    } else {
        ret = (*je)->NewObjectArray(je, 0, stringClass, 0);
    }
    return ret;
}
JNIEXPORT jint JNICALL Java_apache_celix_Celix_startCelix(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *propertyString = (*je)->GetStringUTFChars(je, i, NULL);
	pthread_t thread;
	return pthread_create( &thread, NULL, startCelix, (void*) propertyString);
}

JNIEXPORT jint JNICALL Java_apache_celix_Celix_bundleInstall(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, installBundle, (void*) locationString);
}

JNIEXPORT jint JNICALL Java_apache_celix_Celix_bundleInstallStart(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, installStartBundle, (void*) locationString);
}

JNIEXPORT jint JNICALL Java_apache_celix_Celix_bundleStart(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, startBundle, (void*) locationString);
}

JNIEXPORT jint JNICALL Java_apache_celix_Celix_bundleStartById(JNIEnv* je, jclass jc, jlong javaId)
{
    long id = (long) javaId;
    pthread_t thread;
    return pthread_create( &thread, NULL, startBundleById, (void*) id);

}

JNIEXPORT jint JNICALL Java_apache_celix_Celix_bundleStop(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, stopBundle, (void*) locationString);
}

JNIEXPORT jint JNICALL Java_apache_celix_Celix_bundleStopById(JNIEnv* je, jclass jc, jlong javaId)
{
    long id = (long) javaId;
    pthread_t thread;
    return pthread_create( &thread, NULL, stopBundleById, (void*) id);

}

JNIEXPORT jint JNICALL Java_apache_celix_Celix_bundleDelete(JNIEnv* je, jclass jc, jstring i)
{
    // convert Java string to UTF-8
    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, deleteBundle, (void*) locationString);
}

JNIEXPORT jint JNICALL Java_apache_celix_Celix_bundleDeleteById(JNIEnv* je, jclass jc, jlong javaId)
{
    long id = (long) javaId;
    pthread_t thread;
    return pthread_create( &thread, NULL, deleteBundleById, (void*) id);

}

JNIEXPORT jint JNICALL Java_apache_celix_Celix_stopCelix(JNIEnv* je, jobject thiz)
{
    // convert Java string to UTF-8
//    const char *locationString = (*je)->GetStringUTFChars(je, i, NULL);
    pthread_t thread;
    return pthread_create( &thread, NULL, stopCelix, (void*) NULL);
}

ASensorEventQueue* sensorEventQueue;
void* useAccsensor(void* param)
{
    ASensorManager* sensorManager;
    const ASensor* accSensor;
    ALooper* looper = ALooper_forThread();
    if (looper == NULL) {
        looper = ALooper_prepare(ALOOPER_PREPARE_ALLOW_NON_CALLBACKS);
    }
    sensorManager = ASensorManager_getInstance();
    accSensor = ASensorManager_getDefaultSensor(sensorManager, ASENSOR_TYPE_ACCELEROMETER);
    sensorEventQueue = ASensorManager_createEventQueue(sensorManager, looper, 1,NULL, NULL );
    ASensorEventQueue_enableSensor(sensorEventQueue, accSensor);
    int a = ASensor_getMinDelay(accSensor);
    LOGI("min delay accsensor: %d",a);
    ASensorEventQueue_setEventRate(sensorEventQueue, accSensor, a*2);
    ASensorEvent event;
    int accCounter = 0;
    int prevx, prevy, prevz;
    float prevSpeed;
    while (1) {
        while (ASensorEventQueue_getEvents(sensorEventQueue, &event, 1) > 0) {
            if (event.type == ASENSOR_TYPE_ACCELEROMETER) {
                if (accCounter == 15) {
                    float x,y,z;
                    x = abs(event.acceleration.x);
                    y = abs(event.acceleration.y);
                    z = abs(event.acceleration.z);
//                    LOGI("accelerometer: x=%f y=%f z=%f",x , y, z);

                    float speed = abs(x + y + z - prevx - prevy - prevz);
                    printf("speed: %f\n",speed);
                    if ( speed >= 20) {
                        LOGI("Shake detected!");
                    }
                    prevx = x;
                    prevy = y;
                    prevz = z;
                    accCounter = 0;
                }
                accCounter++;
            }

        }
    }

}

JNIEXPORT jint JNICALL Java_apache_celix_Celix_test(JNIEnv* je, jobject thiz)
{
    pthread_t thread;
    return pthread_create( &thread, NULL, useAccsensor, (void*) NULL);
}




