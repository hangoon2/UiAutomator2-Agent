
### uiautomator2-server

[![Codacy Badge](https://api.codacy.com/project/badge/Grade/bd33cddf5f0b4ae2af8dbced5afe6b49)](https://app.codacy.com/app/dpgraham/appium-uiautomator2-server?utm_source=github.com&utm_medium=referral&utm_content=appium/appium-uiautomator2-server&utm_campaign=badger)
[![Build Status](https://travis-ci.org/appium/appium-uiautomator2-server.svg?branch=master)](https://travis-ci.org/appium/appium-uiautomator2-server)

A netty server that runs on the device listening for commands and executes using UiAutomator V2.

### building project
build the android project using below commands

`gradle clean assembleServerDebug assembleServerDebugAndroidTest`


### Starting server
push both src and test apks to the device and execute the instrumentation tests.

`adb shell am instrument -w com.onycom.uiautomator2.server.test/androidx.test.runner.AndroidJUnitRunner`
