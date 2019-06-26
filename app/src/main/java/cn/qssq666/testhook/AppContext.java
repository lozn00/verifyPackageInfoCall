package cn.qssq666.testhook;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;

import com.swift.sandhook.SandHookConfig;
import com.swift.sandhook.xposedcompat.XposedCompat;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.PasswordAuthentication;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;

public class AppContext extends Application {
    public static final String TAG = "AppContext";

    public static Context createContext() {
        try {
            Class cls = Class.forName("android.app.ActivityThread");
            Method declaredMethod = cls.getDeclaredMethod("currentActivityThread", new Class[0]);
            declaredMethod.setAccessible(true);
            Object invoke = declaredMethod.invoke(null, new Object[0]);
            Field declaredField = cls.getDeclaredField("mBoundApplication");
            declaredField.setAccessible(true);
            Object obj = declaredField.get(invoke);
            Field declaredField2 = obj.getClass().getDeclaredField("info");
            declaredField2.setAccessible(true);
            obj = declaredField2.get(obj);
            Method declaredMethod2 = Class.forName("android.app.ContextImpl").getDeclaredMethod("createAppContext", new Class[]{cls, obj.getClass()});
            declaredMethod2.setAccessible(true);
            Object invoke2 = declaredMethod2.invoke(null, new Object[]{invoke, obj});
            if (invoke2 instanceof Context) {
                return (Context) invoke2;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void hook(Context context) {
        Class<?> activityThreadClass = null;
        try {
//            activityThreadClass = Class.forName("android.app.ActivityThread");
/*            Object currentActivityThread = activityThreadClass.getDeclaredMethod("currentActivityThread", new Class[0]).invoke(null, new Object[0]);
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            Object sPackageManager = sPackageManagerField.get(currentActivityThread);
            Class<?> querySignClass;*/
       /*     if (sPackageManager == null) {
            } else {
                querySignClass = sPackageManager.getClass();
            }*/

            PackageManager packageManager = context.getPackageManager();
            Class<? extends PackageManager> querySignClass = packageManager.getClass();
            //    private final IPackageManager mPM;
            Field mPMField = querySignClass.getDeclaredField("mPM");
            mPMField.setAccessible(true);
            Object ipcManager = mPMField.get(packageManager);
            Log.w(TAG,"queryinfo:"+querySignClass.getName()+","+ipcManager.getClass().getName());
            final Method getPackageInfo = ipcManager.getClass().getMethod("getPackageInfo", String.class, int.class,int.class);

            XposedBridge.hookMethod(getPackageInfo, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.w(TAG, "ipc getPackageInfo call hook ");
                    super.beforeHookedMethod(param);
                }
            });
            final Method getPackageInfoManager = packageManager.getClass().getMethod("getPackageInfoAsUser", String.class, int.class,int.class);
            XposedBridge.hookMethod(getPackageInfoManager, new XC_MethodHook() {

                @Override
                protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                    Log.w(TAG, "packageManager getPackageInfo call hook ");
                    super.beforeHookedMethod(param);
                }
            });


        } catch (Throwable e) {
            Log.w(TAG, "fetch packageManager fail", e);
        }
        XposedHelpers.findAndHookMethod(TextView.class, "setText", CharSequence.class, new XC_MethodHook() {
            @Override
            protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                param.args[0]="ss";
            }

            @Override
            public void callBeforeHookedMethod(MethodHookParam param) throws Throwable {
                super.callBeforeHookedMethod(param);
            }
        });
    }

    static {
//        XposedCompat.cacheDir = getCacheDir();

//for load xp module(sandvxp)
        Context context = createContext();
        SandHookConfig.compiler = false;
        SandHookConfig.DEBUG=true;
        XposedCompat.cacheDir = context.getCacheDir();
        if (XposedCompat.cacheDir == null || TextUtils.isEmpty(XposedCompat.cacheDir.getAbsolutePath())) {

            XposedCompat.cacheDir = new File(String.format("/data/data/%s/cache", BuildConfig.APPLICATION_ID));
        }

        XposedCompat.context = context;
        XposedCompat.classLoader = AppContext.class.getClassLoader();
        XposedCompat.isFirstApplication = true;
//do hook
        hook(context);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
