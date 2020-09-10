package com.onycom.uiautomator2.controller;

import android.annotation.TargetApi;
import android.app.UiAutomation;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.view.Display;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.onycom.uiautomator2.core.UiAutomatorBridge;
import com.onycom.uiautomator2.model.AppiumUIA2Driver;
import com.onycom.uiautomator2.model.AutomationInfo;
import com.onycom.uiautomator2.model.ScreenOrientation;
import com.onycom.uiautomator2.model.internal.CustomUiDevice;
import com.onycom.uiautomator2.utils.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.onycom.uiautomator2.utils.Device.getUiDevice;

import static androidx.test.core.app.ApplicationProvider.getApplicationContext;

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
        // 화면 정보 덤프를 위해 빈 capabilities를 전달하여 Session을 생성한다.
        Map<String, Object> capabilities = new HashMap<String, Object>();
        AppiumUIA2Driver.getInstance().initializeSession(capabilities);
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

        Point pt = DeviceManager.deviceSize();
        Logger.info("Device Size : " + pt.x + ", " + pt.y);
        if(pt.x >= pt.y) {
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

    public boolean searchObject(final AutomationInfo info) {
        return autoManager.searchObject(info);
    }

    public boolean selectObject(final AutomationInfo info) {
        return autoManager.selectObject(info);
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

        manualManager.sendMotionEvent( type, pt, getUiDevice().getDisplayWidth(), getUiDevice().getDisplayHeight() );
    }

    public void swipe(final Point startPt, final Point endPt) {
        if(!controlMode) return;

        manualManager.swipe(startPt, endPt);
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

        List<AccessibilityNodeInfo> controls = root.findAccessibilityNodeInfosByText("전원 끄기");
        if( controls != null && controls.isEmpty() == false ) {
            controls = root.findAccessibilityNodeInfosByText("다시 시작");
            if( controls != null && controls.isEmpty() == false ) {
                ret = true;
            }
        }

        return ret;
    }

    private void checkPowerOff(AccessibilityNodeInfo root) {
        if(root != null) {
            if( isPowerOffScreen(root) ) {
                controlMode = false;
                manualManager.cancelPowerOff();
                controlMode = true;
            }
        }
    }

    private void findCancelButton(AccessibilityNodeInfo root, String text, List<AccessibilityNodeInfo> buttons) {
        List<AccessibilityNodeInfo> cancels = root.findAccessibilityNodeInfosByText(text);
        if( cancels != null && cancels.isEmpty() == false ) {
            for(int i = 0; i < cancels.size(); i++) {
                AccessibilityNodeInfo cancel = cancels.get(i);
                if( cancel.getClassName().toString().equals("android.widget.Button") ) {
                    buttons.add(cancel);
                }
            }
        }
    }

    private boolean isSoftwareUpdatePopup(AccessibilityNodeInfo root) {
        boolean ret = false;

        List<AccessibilityNodeInfo> exceptions = root.findAccessibilityNodeInfosByText("Google 앱 업데이트");
        if( exceptions != null && !exceptions.isEmpty() ) {
            return false;
        }

        List<AccessibilityNodeInfo> popups = root.findAccessibilityNodeInfosByText("소프트웨어 업데이트");
        // 특정 디바이스에서 '소프트웨어 업데이트' 시 해당 문구만 있는 경우가 존재했으나,
        // 해당 문구 자체가 너무 일반적으로 사용하여 예외 상황이 많이 발생하여 해당 문구 체크하지 않도록 수정
//        if( popups == null || popups.isEmpty() ) {
//            popups = root.findAccessibilityNodeInfosByText("업데이트");
//        }

        if( popups == null || popups.isEmpty() ) {
            popups = root.findAccessibilityNodeInfosByText("보안 업데이트 사용 가능");
        }

        if( popups == null || popups.isEmpty() ) {
            popups = root.findAccessibilityNodeInfosByText("시스템 업데이트");
        }

        if( popups != null && popups.isEmpty() == false ) {
            List<AccessibilityNodeInfo> buttons = new ArrayList<AccessibilityNodeInfo>();

            findCancelButton(root, "취소", buttons);
            findCancelButton(root, "나중에", buttons);
            findCancelButton(root, "지금 설치", buttons);
            findCancelButton(root, "업데이트", buttons);
            findCancelButton(root, "지금 다시 시작", buttons);
            findCancelButton(root, "다운로드 및 설치", buttons);

            if( buttons.isEmpty() == false ) {
                ret = true;
            }
        }

        return ret;
    }

    private void checkSWUpdate(AccessibilityNodeInfo root) {
        if(root != null) {
            if( isSoftwareUpdatePopup(root) ) {
                controlMode = false;
                manualManager.cancelSWUpdate();
                controlMode = true;
            }
        }
    }

    private void checkSettingPermission(String packageName, String activityName) {
        SettingPermission permission = DeviceManager.getInstance().getSettingPermission();
        boolean goHome = false;

        if(permission == SettingPermission.None) {
            // 모든 설정 진입 불가
            if( packageName.contains("settings") ) {
                goHome = true;
            }
        } else if(permission == SettingPermission.PartialSettings) {
            Logger.info("PACKAGE : " + packageName + ", " + activityName);
            // 부분적인 설정 진입 허용
            if( !activityName.equals("com.onycom.wtf_lock")
                && ( activityName.equals(".settings")
                    || activityName.contains("com.android.phone.CallFeaturesSetting")
                    || activityName.contains("com.sec.android.app.controlpanel.activity.JobManagerActivity")
                    || activityName.contains("com.lge.systemui.act.ButtonListChangeOrderActivity")
                    || activityName.contains("multitasking.activity.MultiTaskingActivity")
                    || activityName.contains("com.lge.lmk.activities.LmkMainActivity")
                    || activityName.contains("com.lge.systemui.SettingsSelectActivity")
                    || activityName.contains("com.lge.systemui.act.QuickSettingsEditActivity")
                    || activityName.contains("com.lge.systemui.ButtonListChangeOrderActivity")
                    || activityName.contains("wifi")
                    || activityName.contains("apkmanager.activity")		// vega soft upgrade
                    || activityName.contains("Uninstaller")
                    || activityName.contains("uninstaller")
                    || activityName.contains("Fota")
                    || activityName.contains("fota")	// Samsung & LG system upgrade
                    || activityName.contains("VEGASettingsFavorites")
                    || activityName.contains("WFDClientActivity")
                    || activityName.contains("com.lge.systemui.act.NotificationEditActivity")
                    || activityName.contains("AppManager")
                    || activityName.contains("com.wssyncmldm.ui.XUIInstallConfirmActivity")	// S4 Active upgrade
                    || activityName.contains("SystemUpdate")	// Nexus 4 update
                    || activityName.contains("taskmanager")		// Samsung S5 task manager
                    || activityName.contains("Pattern")				// Samsung S5 pattern mode
                    || activityName.contains("com.sec.android.app.myfiles")
                    || activityName.contains("com.lge.filemanager.view")
                    || activityName.contains("com.huawei.systemmanager.SystemManagerMainActivity")
                    || activityName.contains("com.samsung.android.spay")
                    || activityName.contains("com.lge.updatecenter")					// LG(V20, G4) update
                    || activityName.contains("com.google.android.gms")				// Nexus 5X update
                    || activityName.contains("com.samsung.android.sm")
                    || activityName.contains("com.sec.android.fotaclient")			// Samsung Note3, Zoom2
                    || activityName.contains("com.sonyericsson.updatecenter")	// Xperia Z2 update
                    || activityName.contains("com.swsyncmldm") 						// Samsung Note4, Note5, Note8, A7 2016 update 팝업 내리기
                    || activityName.contains("com.onycom.appium.start") )			// appium 연결용 App
                && ( !activityName.contains("settings.LanguageSettings")
                    && !activityName.contains("LanguageSettings")
                    && !activityName.contains("DisplaySettings")
                    && !activityName.contains("Power")
                    && !activityName.contains("SoundSettings")
                    && !activityName.contains("LocationSettingsActivity")
                    && !activityName.contains("Account")
                    && !activityName.contains("account")
                    && !activityName.contains("LocalePickerActivity")
                    && !activityName.contains("ModePreview")
                    && !activityName.contains("SubSettings")
                    && !activityName.contains("LockScreenWallpaper")
                    && !activityName.contains("GoogleSettingsActivity")
                    && !activityName.contains("apps.plus")
                    && !activityName.contains("app.shealth")
                    && !activityName.contains("SetupWizardActivity")
                    && !activityName.contains("com.cjs.cgv")
                    && !activityName.contains("LocalePicker")
                    && !activityName.contains("SecuritySettings")
                    && !activityName.contains("NotificationSettingsActivity")		// 특정 디바이스(IM 100, Nexus 6P, LUNA S, Sol Prime 등)들에서 소리설정이 해당 activity 명으로 되어있음
                    && !activityName.contains("Wifi") ) ) {
                goHome = true;
            }
        }

        if(goHome) {
            controlMode = false;
            manualManager.goHome();
            controlMode = true;
        }
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
                String packageName = event.getPackageName().toString();
                ComponentName cn = new ComponentName(  packageName, event.getClassName().toString() );
                String activityName = cn.getClassName();

                checkSettingPermission(packageName, activityName);
            }

            if(originalListener != null) {
                originalListener.onAccessibilityEvent(event);
            }
        }
    }

    public static Point deviceSize() {
        Point size = new Point();

        Display display = UiAutomatorBridge.getInstance().getDefaultDisplay();
        display.getRealSize(size);

        return size;
    }

}