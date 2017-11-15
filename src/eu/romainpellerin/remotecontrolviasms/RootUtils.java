package eu.romainpellerin.remotecontrolviasms;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;


public class RootUtils {

    private final static String COMMAND_SU = "su";
    private final static String[] COMMAND_REQUEST_PRIVILEGES = {"id"};
    private final static String[] COMMAND_GPS_ENABLE = {"cd /system/bin" ,"settings put secure location_providers_allowed +gps"};
    private final static String[] COMMAND_MOBILE_DATA_ENABLE = {"svc data enable\n "};

    private static void execute(String[] commands) {
        try {
            if (null != commands && commands.length > 0) {
                Process suProcess = Runtime.getRuntime().exec(COMMAND_SU);

                DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                // Execute commands that require root access
                for (String currCommand : commands) {
                    os.writeBytes(currCommand + "\n");
                    os.flush();
                }

                os.writeBytes("exit\n");
                os.flush();
            }
        } catch (IOException ex) {
            Log.w("ROOT", "Can't get root access", ex);
        } catch (SecurityException ex) {
            Log.w("ROOT", "Can't get root access", ex);
        } catch (Exception ex) {
            Log.w("ROOT", "Error executing internal operation", ex);
        }
    }

    static void requestRootPrivileges() {
        execute(COMMAND_REQUEST_PRIVILEGES);
    }

    public static void enableMobileData() {
        execute(COMMAND_MOBILE_DATA_ENABLE);
    }

    public static void enableGps() {
        execute(COMMAND_GPS_ENABLE);
    }

    public static boolean isRooted() {
        return checkRootMethod1() || checkRootMethod2() || checkRootMethod3();
    }

    private static boolean checkRootMethod1() {
        String buildTags = android.os.Build.TAGS;
        return buildTags != null && buildTags.contains("test-keys");
    }

    private static boolean checkRootMethod2() {
        String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/system/xbin/su", "/data/local/xbin/su", "/data/local/bin/su", "/system/sd/xbin/su",
                "/system/bin/failsafe/su", "/data/local/su", "/su/bin/su"};
        for (String path : paths) {
            if (new File(path).exists()) return true;
        }
        return false;
    }

    private static boolean checkRootMethod3() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(new String[] { "/system/xbin/which", "su" });
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream()));
            return in.readLine() != null;
        } catch (Throwable t) {
            return false;
        } finally {
            if (process != null) process.destroy();
        }
    }
}

