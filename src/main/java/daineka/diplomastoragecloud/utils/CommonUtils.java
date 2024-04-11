package daineka.diplomastoragecloud.utils;

public class CommonUtils {
    public static String createID() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
