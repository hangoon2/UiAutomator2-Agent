package com.onycom.uiautomator2.controller;

import android.annotation.TargetApi;
import android.app.UiAutomation;
import android.graphics.Point;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import java.util.List;

import com.onycom.uiautomator2.model.ScreenOrientation;
import com.onycom.uiautomator2.model.internal.CustomUiDevice;
import com.onycom.uiautomator2.utils.Logger;

import static com.onycom.uiautomator2.utils.Device.getUiDevice;

public class DeviceManager {

    public enum SettingPermission {
        AllSettings,        // 모든 설정 진입 허용
        PartialSettings,    // 부분적인 설정 진입 허용
        None,               // 모든 설정 진입 불가
    }

    private static DeviceManager device = null;

    private ManualManager manualManager = null;
    private AutomationManager autoManager = null;

    private UiAutomation.OnAccessibilityEventListener originalListener = null;

    private SettingPermission settingPermission = SettingPermission.AllSettings;

    private boolean bDefaultLandscape = false;
    private boolean controlMode = true;

    private DeviceManager() {

    }

    public static DeviceManager getInstance() {
        if(device == null) {
            device = new DeviceManager();
        }

        return device;
    }

    public void initialize() {
        // 회전을 0으로 초기화
        Logger.info( "디바이스 현재 회전 상태 : " + getUiDevice().getDisplayRotation() );

        CustomUiDevice.getInstance().setOrientationSync(ScreenOrientation.ROTATION_0);
        Logger.info( "초기화 이후 디바이스 현재 회전 상태 : " + getUiDevice().getDisplayRotation() );
        if( getUiDevice().getDisplayWidth() >= getUiDevice().getDisplayHeight() ) {
            bDefaultLandscape = true;
        }

        manualManager = new ManualManager();
        autoManager = new AutomationManager();

        manualManager.startTouchEventManager();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            originalListener = com.onycom.uiautomator2.core.UiAutomation.getInstance().getOnAccessibilityEventListener();

            com.onycom.uiautomator2.core.UiAutomation.getInstance().setOnAccessibilityEventListener( new AccessibilityEventListenerImpl() );
        }
    }

    public void close() {
        manualManager.close();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && originalListener != null) {
            Logger.info("OnAccessibilityEventListener Initialize ........");
            com.onycom.uiautomator2.core.UiAutomation.getInstance().setOnAccessibilityEventListener(originalListener);
        }
    }

    public String dumpHierarchyData(final String fileName) {
        return autoManager.dumpHierarchyData(fileName);
    }

    public void screenRotate(final boolean bLandScape) {
        if(bLandScape) {
            CustomUiDevice.getInstance().setOrientation(ScreenOrientation.ROTATION_90);
        } else {
            CustomUiDevice.getInstance().setOrientation(ScreenOrientation.ROTATION_0);
        }
    }

    public boolean searchObject(final AutomationManager.UiObjectItem objItem) {
        return autoManager.searchObject(objItem);
    }

    public boolean selectObject(final AutomationManager.UiObjectItem objItem) {
        return autoManager.selectObject(objItem);
    }

    public void setPlayStop(final boolean bPlayStop) {
        autoManager.setPlayStop(bPlayStop);
    }

    public void sendKey(final int keyCode, final boolean bLong) {
        if(!controlMode) return;

        manualManager.sendKey(keyCode, bLong);
    }

    public void sendMotionEvent(final int type, final Point[] pt) {
        if(!controlMode) return;


    }

    public void swipe(final Point startPt, final Point endPt) {
        if(!controlMode) return;


    }

    public void tap(final Point pt) {
        if(!controlMode) return;

        manualManager.touchDown(pt.x, pt.y);
        manualManager.touchUp(pt.x, pt.y);
    }

    public void touchDown(final Point pt) {
        if(!controlMode) return;

        manualManager.touchDown(pt.x, pt.y);
    }

    public void touchMove(final Point pt) {
        if(!controlMode) return;

        manualManager.touchMove(pt.x, pt.y);
    }

    public void touchUp(final Point pt) {
        if(!controlMode) return;

        manualManager.touchUp(pt.x, pt.y);
    }

    /* ////////////////////////////////////////////////////////
    //              설정 진입 제한 및 SW 업데이트 차단              //
    //////////////////////////////////////////////////////// */

    public void setSettingPermission(SettingPermission permission) {
        this.settingPermission = permission;
    }

    public SettingPermission getSettingPermission() {
        return settingPermission;
    }

    private boolean isPowerOffScreen(AccessibilityNodeInfo root) {
        boolean ret = false;

        return ret;
    }

    private void checkPowerOff(AccessibilityNodeInfo root) {

    }

    private void findCancelButton(AccessibilityNodeInfo root, String text, List<AccessibilityNodeInfo> buttons) {

    }

    private boolean isSoftwareUpdatePopup(AccessibilityNodeInfo root) {
        boolean ret = false;

        return ret;
    }

    private void checkSWUpdate(AccessibilityNodeInfo root) {

    }

    private void checkSettingPermission(String packageName, String activityName) {

    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    private class AccessibilityEventListenerImpl implements UiAutomation.OnAccessibilityEventListener {

        @Override
        public void onAccessibilityEvent(AccessibilityEvent event) {
            if( event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED ||
            event.getEventType() == AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED ) {
//               Logger.info( "OnAccessibilityEvent Accured : " + event.getEventType() );
                AccessibilityNodeInfo root = event.getSource();

                checkSWUpdate(root);
                checkPowerOff(root);
            }

            if( event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                event.getPackageName() != null ) {

            }

            if(originalListener != null) {
                originalListener.onAccessibilityEvent(event);
            }
        }
    }

}