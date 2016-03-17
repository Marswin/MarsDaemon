# MarsDaemon

It is a lite library, you can make your project depend it easily, and your project will be UNDEAD.

  * support to keep alive from Android_API 9 to Android_API 23
  * support to keep alive in most of devices(contains Sumsung\Huawei\Meizu\Mi\Nexus..)
  * support to keep alive in FORCE_CLOSE from SystemSettings and MEMORY_CLEAN from third-part apps (such like CleanMaster\360 and so on)
  * support to keep BOOT_RECEIVER work well simplely

my Blog in  Chinese here:
[http://blog.csdn.net/marswin89/article/details/50917098](http://blog.csdn.net/marswin89/article/details/50917098)


### Version
1.0

## Installation
#### STEP1
make your project depend on LibMarsdaemon, and regist 2 Service and 2 BroadcastReceiver in your manifests in 2 different process.

```sh
<service android:name=".Service1" android:process=":process1"/>
<receiver android:name=".Receiver1" android:process=":process1"/>
<service android:name=".Service2" android:process=":process2"/>
<receiver android:name=".Receiver2" android:process=":process2"/>
```

Service1 is the Service which you want to be undead, you can do somethings in it.

But the others is used by Marsdaemon, so DONNOT do anything inside.

#### STEP2
make your application extends DaemonApplication and override the method getDaemonConfigurations(). Return back the confugirations.
```sh
@Override
protected DaemonConfigurations getDaemonConfigurations() {
    DaemonConfigurations.DaemonConfiguration configuration1 = new DaemonConfigurations.DaemonConfiguration("com.marswin89.marsdaemon.demo:process1", Service1.class.getCanonicalName(), Receiver1.class.getCanonicalName());
    DaemonConfigurations.DaemonConfiguration configuration2 = new DaemonConfigurations.DaemonConfiguration("com.marswin89.marsdaemon.demo:process2", Service2.class.getCanonicalName(), Receiver2.class.getCanonicalName());
    DaemonConfigurations.DaemonListener listener = new MyDaemonListener();
    //return new DaemonConfigurations(configuration1, configuration2);//listener can be null
    return new DaemonConfigurations(configuration1, configuration2, listener);
}
```
if you want to override attachBaseContext you will find it had been defined final by me. you can override attachBaseContextByDaemon instead it.

see more details in MyApplication1 in Demo

##### if your application has extends another application, you should create a DaemonClient and perfrom it in attachBaseContext(), DONOT forget perform super.attachBaseContext() before!

```sh
private DaemonClient mDaemonClient;
@Override
protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    mDaemonClient = new DaemonClient(createDaemonConfigurations());
    mDaemonClient.onAttachBaseContext(base);
}
```
see more details in MyApplication2 in DemoMarsdaemon 

#### STEP3
Launch the Service once, and try to kill it.

##
##
##
## Contact me
Email: Marswin89@gmail.com


##
##
##

License
----

Copyright (C) 2015, Mars Kwok

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

