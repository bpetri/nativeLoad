package celix.com.example.mylibrary;

/**
 * Created by mjansen on 28-10-15.
 */
public class Celix {

    static {
        System.loadLibrary("celix_utils");
        System.loadLibrary("celix_framework");
        System.loadLibrary("jni_part");
    }

    public Celix() {
        initJni();
    }

    public native int initJni();


}
