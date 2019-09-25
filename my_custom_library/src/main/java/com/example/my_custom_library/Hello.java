package com.example.my_custom_library;

import android.content.Context;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.HttpsURLConnection;
import dalvik.system.PathClassLoader;

public class Hello {
    private static ClassLoader loader;
    private static String loadingTime;

    public static void setModuleLoader(Context context){
        try {
            final long startTime = System.currentTimeMillis();
            ModuleLoader.downloadDexFile(context, new callback() {
                @Override
                public void done(File dex) throws Exception {
                    loader = new PathClassLoader(dex.getAbsolutePath(), context.getClassLoader());

                    final long endTime = System.currentTimeMillis();
                    loadingTime = "Loading time: " + (endTime - startTime) + "ms";
                }
            });
        } catch (Exception ex){

        }
    }

    public static void getText(Context context, ResultCallback callback){
        try {
            final long startTime = System.currentTimeMillis();
            ModuleLoader.newLoad(loader, context, (text) -> {
                final long endTime = System.currentTimeMillis();
                String time = (endTime - startTime) + "ms";
                callback.done(loadingTime + "\nFingerprintGeneration Time: " + time);
            });
        } catch (Exception ex){
            ex.printStackTrace();
        }
    }
}

class ModuleLoader {

    static void newLoad(ClassLoader loader, Context context, ResultCallback callback) throws Exception {
        Class<?> clazz = loader.loadClass("com.simpl.android.fingerprint.SimplFingerprint");
        Method init = clazz.getMethods()[9];
        Method getInstance = clazz.getMethod("getInstance");
        Method generateFingerprint = clazz.getMethods()[3];

        init.invoke(null, context, "9515127528", "kamalnrf@gmail.com");
        Object simplFingerprint = getInstance.invoke(null);

        String simplFPListner = "com.simpl.android.fingerprint.SimplFingerprintListener";
        Class<?> SimplFingerprintListener = loader.loadClass(simplFPListner);

        Object proxyFPListener = Proxy.newProxyInstance(
                SimplFingerprintListener.getClassLoader(),
                new Class[]{SimplFingerprintListener},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                        callback.done((String) objects[0]);
                        return null;
                    }
                }
        );

        generateFingerprint.invoke(simplFingerprint, proxyFPListener);
    }

    static void load(final String cls, final Context context, ResultCallback callback) throws Exception {
        downloadDexFile(context, new callback() {
            @Override
            public void done(File dex) throws Exception{
                //DexFile dexFile = new DexFile(copyDexFlie(context));
                //dex = copyDexFlie(context);
                ClassLoader loader = new PathClassLoader(dex.getAbsolutePath(), context.getClassLoader());
                Class<?> clazz = loader.loadClass(cls);
                Method init = clazz.getMethods()[9];
                Method getInstance = clazz.getMethod("getInstance");
                Method generateFingerprint = clazz.getMethods()[3];

                init.invoke(null, context, "9515127528", "kamalnrf@gmail.com");
                Object simplFingerprint = getInstance.invoke(null);

                String simplFPListner = "com.simpl.android.fingerprint.SimplFingerprintListener";
                Class<?> SimplFingerprintListener = loader.loadClass(simplFPListner);

                Object proxyFPListener = Proxy.newProxyInstance(
                        SimplFingerprintListener.getClassLoader(),
                        new Class[]{SimplFingerprintListener},
                        new InvocationHandler() {
                            @Override
                            public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
                                callback.done((String) objects[0]);
                                return null;
                            }
                        }
                );

                generateFingerprint.invoke(simplFingerprint, proxyFPListener);

            }
        });
    }


    public static File downloadDexFile(Context context, final callback callback) throws Exception {
        final File dex = new File(context.getFilesDir(),"/dexFile.dex");
        dex.createNewFile();

        Executor exe = Executors.newSingleThreadExecutor();

        exe.execute(() -> {
            try {
                URL url = new URL("https://089148e3.ngrok.io/dex");
                HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();
                conn.setDoOutput(true);

                conn.getResponseCode();
                InputStream is = conn.getInputStream();
                OutputStream out = new BufferedOutputStream(new FileOutputStream(dex));
                out.write(readStreamToEnd(is));

                is.close();
                out.flush();
                out.close();

                //String hash = MD5Hash(respones);
                callback.done(dex);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        return null;
    }

    private static byte[] readStreamToEnd(final InputStream is) throws IOException {
        final ByteArrayOutputStream bos = new ByteArrayOutputStream();
        if (is != null) {
            final byte[] buff = new byte[1024];
            int read;
            do {
                bos.write(buff, 0, (read = is.read(buff)) < 0 ? 0 : read);
            } while (read >= 0);
            is.close();
        }
        return bos.toByteArray();
    }
}

interface callback {
    void done(File dex) throws Exception;
}

