/*
 * File        : daemon_api21.c
 * Author      : Mars Kwok
 * Date        : Jul. 21, 2015
 * Description : This is native process to watch parent process.
 *
 * Copyright (C) Mars Kwok<Marswin89@gmail.com>
 *
 */
#include <stdlib.h>
#include <unistd.h>

#include "log.h"
#include "constant.h"


/**
 *  get the android version code
 */
int get_version(){
	char value[8] = "";
    __system_property_get("ro.build.version.sdk", value);
    return atoi(value);
}

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



int main(int argc, char *argv[]){
	pid_t pid = fork();
	if(pid == 0){
		setsid();
		int pipe_fd1[2];
		int pipe_fd2[2];
		char* pkg_name;
		char* svc_name;
		if(argc < 13){
			LOGE("daemon parameters error");
			return ;
		}
		int i;
		for (i = 0; i < argc; i ++){
			if(argv[i] == NULL){
				continue;
			}
			if (!strcmp(PARAM_PKG_NAME, argv[i])){
				pkg_name = argv[i + 1];
			}else if (!strcmp(PARAM_SVC_NAME, argv[i]))	{
				svc_name = argv[i + 1];
			}else if (!strcmp(PARAM_PIPE_1_READ, argv[i])){
				char* p1r = argv[i + 1];
				pipe_fd1[0] = atoi(p1r);
			}else if (!strcmp(PARAM_PIPE_1_WRITE, argv[i]))	{
				char* p1w = argv[i + 1];
				pipe_fd1[1] = atoi(p1w);
			}else if (!strcmp(PARAM_PIPE_2_READ, argv[i]))	{
				char* p2r = argv[i + 1];
				pipe_fd2[0] = atoi(p2r);
			}else if (!strcmp(PARAM_PIPE_2_WRITE, argv[i]))	{
				char* p2w = argv[i + 1];
				pipe_fd2[1] = atoi(p2w);
			}
		}

		close(pipe_fd1[0]);
		close(pipe_fd2[1]);

		char r_buf[100];
		int r_num;
		memset(r_buf,0, sizeof(r_buf));

		r_num=read(pipe_fd2[0], r_buf, 100);
		LOGE("Watch >>>>PARENT<<<< Dead !!");
		int count = 0;
		while(count < 50){
			start_service(pkg_name, svc_name);
			usleep(100000);
			count++;
		}
	}else{
		exit(EXIT_SUCCESS);
	}
}
