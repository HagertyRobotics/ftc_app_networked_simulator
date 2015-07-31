/* Copyright (c) 2014, 2015 Qualcomm Technologies Inc

All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted (subject to the limitations in the disclaimer below) provided that
the following conditions are met:

Redistributions of source code must retain the above copyright notice, this list
of conditions and the following disclaimer.

Redistributions in binary form must reproduce the above copyright notice, this
list of conditions and the following disclaimer in the documentation and/or
other materials provided with the distribution.

Neither the name of Qualcomm Technologies Inc nor the names of its contributors
may be used to endorse or promote products derived from this software without
specific prior written permission.

NO EXPRESS OR IMPLIED LICENSES TO ANY PARTY'S PATENT RIGHTS ARE GRANTED BY THIS
LICENSE. THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
"AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. */

package com.qualcomm.ftcrobotcontroller;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.qualcomm.analytics.Analytics;
import com.qualcomm.ftccommon.DbgLog;
import com.qualcomm.ftccommon.FtcRobotControllerService;
import com.qualcomm.ftccommon.FtcRobotControllerService.FtcRobotControllerBinder;
import com.qualcomm.ftccommon.Restarter;
import com.qualcomm.ftccommon.UpdateUI;
import com.qualcomm.modernrobotics.ModernRoboticsHardwareFactory;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.HardwareFactory;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.hardware.mock.MockDeviceManager;
import com.qualcomm.robotcore.hardware.mock.MockHardwareFactory;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.Dimmer;
import com.qualcomm.robotcore.util.ImmersiveMode;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class FtcRobotControllerActivity extends Activity {

  private static final int REQUEST_CONFIG_WIFI_CHANNEL = 1;
  private static final boolean USE_MOCK_HARDWARE_FACTORY = false;
  private static final int NUM_GAMEPADS = 2;

  protected static final String VIEW_LOGS_ACTION = "com.qualcomm.ftcrobotcontroller.VIEW_LOGS";

  protected SharedPreferences preferences;

  protected UpdateUI.Callback callback;
  protected Context context;
  private Utility utility;
  private boolean launched;

  protected TextView textDeviceName;
  protected TextView textWifiDirectStatus;
  protected TextView textRobotStatus;
  protected TextView[] textGamepad = new TextView[NUM_GAMEPADS];
  protected TextView textOpMode;
  protected TextView textErrorMessage;
  protected ImmersiveMode immersion;

  protected UpdateUI updateUI;
  protected BatteryChecker batteryChecker;
  protected Dimmer dimmer;
  protected LinearLayout entireScreenLayout;

  protected FtcRobotControllerService controllerService;

  protected FtcEventLoop eventLoop;

  protected class RobotRestarter implements Restarter {

    public void requestRestart() {
      requestRobotRestart();
    }

  }

  protected ServiceConnection connection = new ServiceConnection() {
    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
      FtcRobotControllerBinder binder = (FtcRobotControllerBinder) service;
      onServiceBind(binder.getService());
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
      controllerService = null;
    }
  };

  @Override
  protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    if (UsbManager.ACTION_USB_ACCESSORY_ATTACHED.equals(intent.getAction())) {
      // a new USB device has been attached
      DbgLog.msg("USB Device attached; app restart may be needed");
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_ftc_controller);

    context = this;
    utility = new Utility(this);
    entireScreenLayout = (LinearLayout) findViewById(R.id.entire_screen);

    textDeviceName = (TextView) findViewById(R.id.textDeviceName);
    textWifiDirectStatus = (TextView) findViewById(R.id.textWifiDirectStatus);
    textRobotStatus = (TextView) findViewById(R.id.textRobotStatus);
    textOpMode = (TextView) findViewById(R.id.textOpMode);
    textErrorMessage = (TextView) findViewById(R.id.textErrorMessage);
    textGamepad[0] = (TextView) findViewById(R.id.textGamepad1);
    textGamepad[1] = (TextView) findViewById(R.id.textGamepad2);
    immersion = new ImmersiveMode(getWindow().getDecorView());
    dimmer = new Dimmer(this);
    dimmer.longBright();
    Restarter restarter = new RobotRestarter();
    Analytics analytics = new Analytics(this);

    updateUI = new UpdateUI(this, dimmer);
    updateUI.setRestarter(restarter);
    updateUI.setTextViews(textWifiDirectStatus, textRobotStatus,
        textGamepad, textOpMode, textErrorMessage, textDeviceName);
    callback = updateUI.new Callback();

    PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
    preferences = PreferenceManager.getDefaultSharedPreferences(this);

    launched = false;

    hittingMenuButtonBrightensScreen();
  }

  @Override
  protected void onStart() {
    super.onStart();

    // save 4MB of logcat to the SD card
    RobotLog.writeLogcatToDisk(this, 4 * 1024);

    Intent intent = new Intent(this, FtcRobotControllerService.class);
    bindService(intent, connection, Context.BIND_AUTO_CREATE);

    utility.updateHeader(Utility.NO_FILE, R.string.pref_hardware_config_filename, R.id.active_filename, R.id.included_header);

    callback.wifiDirectUpdate(WifiDirectAssistant.Event.DISCONNECTED);

    entireScreenLayout.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        dimmer.handleDimTimer();
        return false;
      }
    });
  }

  @Override
  protected void onResume() {
    super.onResume();
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  protected void onStop() {
    super.onStop();

    if (controllerService != null) unbindService(connection);

    RobotLog.cancelWriteLogcatToDisk(this);
  }

  @Override
  public void onWindowFocusChanged(boolean hasFocus){
    super.onWindowFocusChanged(hasFocus);
    // When the window loses focus (e.g., the action overflow is shown),
    // cancel any pending hide action. When the window gains focus,
    // hide the system UI.
    if (hasFocus) {
      if (ImmersiveMode.apiOver19()){
        // Immersive flag only works on API 19 and above.
        immersion.hideSystemUI();
      }
    } else {
      immersion.cancelSystemUIHide();
    }
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.ftc_robot_controller, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_restart_robot:
        dimmer.handleDimTimer();
        Toast.makeText(context, "Restarting Robot", Toast.LENGTH_SHORT).show();
        requestRobotRestart();
        return true;
      case R.id.action_settings:
        startActivity(new Intent(getBaseContext(), FtcRobotControllerSettingsActivity.class));
        return true;
      case R.id.action_about:
        startActivity(new Intent(getBaseContext(), AboutActivity.class));
        return true;
      case R.id.action_exit_app:
        finish();
        return true;
      case R.id.action_view_logs:
        Intent viewLogsIntent = new Intent(VIEW_LOGS_ACTION);
        viewLogsIntent.putExtra(ViewLogsActivity.FILENAME, RobotLog.getLogFilename(this));
        startActivity(viewLogsIntent);
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // don't destroy assets on screen rotation
  }
  @Override
  protected void onActivityResult(int request, int result, Intent intent) {
    if (request == REQUEST_CONFIG_WIFI_CHANNEL) {
      if (result == RESULT_OK) {
        Toast toast = Toast.makeText(context, "Configuration Complete", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER, 0, 0);
        showToast(toast);
      }
    }
  }

  public void onServiceBind(FtcRobotControllerService service) {
    DbgLog.msg("Bound to Ftc Controller Service");
    controllerService = service;
    updateUI.setControllerService(controllerService);

    callback.wifiDirectUpdate(controllerService.getWifiDirectStatus());
    callback.robotUpdate(controllerService.getRobotStatus());
    requestRobotSetup();
  }

  private void requestRobotSetup() {
    if (controllerService == null) return;

    FileInputStream fis = fileSetup();
    // if we can't find the file, don't try and build the robot.
    if (fis == null) { return; }

    HardwareFactory factory;

    if (USE_MOCK_HARDWARE_FACTORY) {
      // TODO: temp testing code. This will be removed in a future release
      try {
        factory = buildMockHardware();
      } catch (RobotCoreException e) {
        DbgLog.logStacktrace(e);
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        return;
      } catch (InterruptedException e) {
        DbgLog.logStacktrace(e);
        Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        return;
      }
  } else {
      // Modern Robotics Factory for use with Modern Robotics hardware
      ModernRoboticsHardwareFactory modernroboticsFactory = new ModernRoboticsHardwareFactory(context);
      modernroboticsFactory.setXmlInputStream(fis);
      factory = modernroboticsFactory;
    }

    eventLoop = new FtcEventLoop(factory, callback);

    controllerService.setCallback(callback);
    controllerService.setupRobot(eventLoop);

    long milliseconds = 180000; //milliseconds
    batteryChecker = new BatteryChecker(this, eventLoop, milliseconds);
    batteryChecker.startBatteryMonitoring();
  }

  private FileInputStream fileSetup() {
    boolean hasConfigFile = preferences.contains(getString(R.string.pref_hardware_config_filename));
    String activeFilename = utility.getFilenameFromPrefs(R.string.pref_hardware_config_filename, Utility.NO_FILE);
    if (!launched) {
      if (!hasConfigFile ||
          activeFilename.equalsIgnoreCase(Utility.NO_FILE) ||
          activeFilename.toLowerCase().contains(Utility.UNSAVED.toLowerCase())) {
        utility.saveToPreferences(Utility.NO_FILE, R.string.pref_hardware_config_filename);
        DbgLog.msg("No default config file, so launching Hardware Wizard");
        launched = true;
        startActivity(new Intent(getBaseContext(), FtcLoadFileActivity.class));
        return null;
      }
    }

    utility.updateHeader(Utility.NO_FILE, R.string.pref_hardware_config_filename, R.id.active_filename, R.id.included_header);

    final String filename = Utility.CONFIG_FILES_DIR
        + utility.getFilenameFromPrefs(R.string.pref_hardware_config_filename, Utility.NO_FILE) + Utility.FILE_EXT;

    FileInputStream fis;
    try {
      fis = new FileInputStream(filename);
    } catch (FileNotFoundException e) {
      String msg = "Cannot open robot configuration file - " + filename;
      utility.complainToast(msg, context);
      DbgLog.msg(msg);
      return null;
    }
    return fis;
  }

  // TODO: temp testing code. This will be removed in a future release
  private MockHardwareFactory buildMockHardware() throws RobotCoreException, InterruptedException{
    DeviceManager dm = new MockDeviceManager(null, null);
    DcMotorController mc = dm.createUsbDcMotorController(new SerialNumber("MC"));
    DcMotorController mc2 = dm.createUsbDcMotorController(new SerialNumber("MC2"));
    ServoController sc = dm.createUsbServoController(new SerialNumber("SC"));

    HardwareMap hwMap = new HardwareMap();
    hwMap.dcMotor.put("left", new DcMotor(mc, 1));
    hwMap.dcMotor.put("right", new DcMotor(mc, 2));
    hwMap.dcMotor.put("flag", new DcMotor(mc2, 1));
    hwMap.dcMotor.put("arm", new DcMotor(mc2, 2));
    hwMap.servo.put("a", new Servo(sc, 1));
    hwMap.servo.put("b", new Servo(sc, 6));

    hwMap.appContext = this;

    return new MockHardwareFactory(hwMap);
  }

  private void requestRobotShutdown() {
    if (controllerService == null) return;
    controllerService.shutdownRobot();
    batteryChecker.endBatteryMonitoring();
  }

  private void requestRobotRestart() {
    requestRobotShutdown();
    requestRobotSetup();
  }

  protected void hittingMenuButtonBrightensScreen() {
    ActionBar actionBar = getActionBar();
    if (actionBar != null) {
      actionBar.addOnMenuVisibilityListener(new ActionBar.OnMenuVisibilityListener() {
        @Override
        public void onMenuVisibilityChanged(boolean isVisible) {
          if (isVisible) {
            dimmer.handleDimTimer();
          }
        }
      });
    }
  }

  public void showToast(final Toast toast) {
    runOnUiThread(new Runnable() {
      @Override
      public void run() {
        toast.show();
      }
    });
  }
}
