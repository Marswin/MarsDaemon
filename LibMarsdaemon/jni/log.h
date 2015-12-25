/*
 * File        : daemon_below20.c
 * Author      : Guoyang3
 * Date        : Aug. 14, 2015
 * Description : for easy log.
 */

#include <jni.h>
#include <android/log.h>

#define TAG		"Daemon"

#define LOGI(...)	__android_log_print(ANDROID_LOG_INFO, TAG, __VA_ARGS__)
#define LOGD(...)	__android_log_print(ANDROID_LOG_DEBUG, TAG, __VA_ARGS__)
#define LOGW(...)	__android_log_print(ANDROID_LOG_WARN, TAG, __VA_ARGS__)
#define	LOGE(...)	__android_log_print(ANDROID_LOG_ERROR, TAG, __VA_ARGS__)