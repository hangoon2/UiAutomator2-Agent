package com.onycom.uiautomator2.server.test;

import android.Manifest;
import android.os.SystemClock;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import com.onycom.uiautomator2.common.exceptions.SessionRemovedException;
import com.onycom.uiautomator2.server.ServerInstrumentation;
import com.onycom.uiautomator2.utils.Logger;

@RunWith(AndroidJUnit4.class)
public class UiAutomator2Server {
    private static ServerInstrumentation serverInstrumentation;

    /**
     * Starts the server on the device.
     * !!! This class is the main entry point for UIA2 driver package.
     * !!! Do not rename or move it unless you know what you are doing.
     */
    @Test
    public void startServer() {
        // dump 파일등을 저장하기 위한 외부 스토리지 저장 권한 요청
        ServerInstrumentation.request(Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (serverInstrumentation == null) {
            serverInstrumentation = ServerInstrumentation.getInstance();
            Logger.info("[UiAutomator2 Server]", " Starting Server");
            try {
                while (!serverInstrumentation.isServerStopped()) {
                    serverInstrumentation.startServer();

                    SystemClock.sleep(1000);
                }
            } catch (SessionRemovedException e) {
                //Ignoring SessionRemovedException
            }
        }
    }
}

