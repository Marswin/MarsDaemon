/*
 * File        : daemon_api21.c
 * Author      : Mars Kwok
 * Date        : Jul. 21, 2015
 * Description : common method here
 *
 * Copyright (C) Mars Kwok<Marswin89@gmail.com>
 *
 */
#include <stdlib.h>
#include <stdio.h>
#include <sys/inotify.h>
#include <fcntl.h>
#include <sys/stat.h>

#include "log.h"

/**
 *  get the android version code
 */
int get_version(){
	char value[8] = "";
    __system_property_get("ro.build.version.sdk", value);
    return atoi(value);
}

/**
 *  stitch three string to one
 */
char *str_stitching(const char *str1, const char *str2, const char *str3){
	char *result;
	result = (char*) malloc(strlen(str1) + strlen(str2) + strlen(str3) + 1);
	if (!result){
		return NULL;
	}
	strcpy(result, str1);
	strcat(result, str2);
	strcat(result, str3);
    return result;
}

/**
 * get android context
 */
jobject get_context(JNIEnv* env, jobject jobj){
    jclass thiz_cls = (*env)->GetObjectClass(env, jobj);
    jfieldID context_field = (*env)->GetFieldID(env, thiz_cls, "mContext", "Landroid/content/Context;");
    return (*env)->GetObjectField(env, jobj, context_field);
}


char* get_package_name(JNIEnv* env, jobject jobj){
    jobject context_obj = get_context(env, jobj);
    jclass context_cls = (*env)->GetObjectClass(env, context_obj);
    jmethodID getpackagename_method = (*env)->GetMethodID(jobj, context_cls, "getPackageName", "()Ljava/lang/String;");
    jstring package_name = (jstring)(*env)->CallObjectMethod(env, context_obj, getpackagename_method);
    return (char*)(*env)->GetStringUTFChars(env, package_name, 0);
}


/**
 * call java callback
 */
void java_callback(JNIEnv* env, jobject jobj, char* method_name){
	jclass cls = (*env)->GetObjectClass(env, jobj);
	jmethodID cb_method = (*env)->GetMethodID(env, cls, method_name, "()V");
	(*env)->CallVoidMethod(env, jobj, cb_method);
}

/**
 * start a android service
 */
void start_service(char* package_name, char* service_name){
    pid_t pid = fork();
    if(pid < 0){
        //error, do nothing...
    }else if(pid == 0){
        if(package_name == NULL || service_name == NULL){
            exit(EXIT_SUCCESS);
        }
        int version = get_version();
        char* pkg_svc_name = str_stitching(package_name, "/", service_name);
        if (version >= 17 || version == 0) {
            execlp("am", "am", "startservice", "--user", "0", "-n", pkg_svc_name, (char *) NULL);
        } else {
            execlp("am", "am", "startservice", "-n", pkg_svc_name, (char *) NULL);
        }
        exit(EXIT_SUCCESS);
    }else{
        waitpid(pid, NULL, 0);
    }
}
