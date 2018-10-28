package cn.onlyloveyd.lazyapkshare.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.res.AssetManager;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.channels.FileChannel;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * 文 件 名: FileUtils
 * 创建日期: 2018/6/26 07:26
 * 邮   箱: onlyloveyd@gmail.com
 * 博   客: https://onlyloveyd.cn
 * 描   述：文件操作工具类
 *
 * @author: yidong
 */
public class FileUtils {

    /**
     * 保存字符串到文件
     *
     * @param sourceStr  待保存字符串
     * @param targetPath 文件路径
     * @return 如果保存成功将返回true，否则返回false
     */
    public static boolean writeStringToFile(String sourceStr, String targetPath) {
        if (TextUtils.isEmpty(targetPath)) {
            return false;
        } else {
            File targetFile = new File(targetPath);
            return writeStringToFile(sourceStr, targetFile);
        }
    }

    /**
     * 保存字符串到文件
     *
     * @param sourceStr  待保存字符串
     * @param targetFile 目标文件
     * @return 如果保存成功将返回true，否则返回false
     */
    public static boolean writeStringToFile(String sourceStr, File targetFile) {
        if (targetFile == null) {
            return false;
        }
        File dir = targetFile.getParentFile();
        if (!dir.exists() && !dir.mkdirs()) {
            // 人衰怪系统了
            return false;
        }
        if (sourceStr == null) {
            sourceStr = "";
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(targetFile);
            fw.write(sourceStr);
            fw.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(fw);
        }
        return false;
    }

    /**
     * 从应用资源目录读取字符串文件，注意：文件不能过大
     *
     * @param fileName 文件名称，必须是相对全路径，比如"/img/user.xml"
     * @return 读取的字符串，如果失败将返回null
     */
    public static String readStringFromAsset(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        if (assetManager != null) {
            InputStream is = null;
            try {
                is = assetManager.open(fileName);
                byte[] buffer = new byte[4096];
                int len;
                StringBuilder builder = new StringBuilder();
                while ((len = is.read(buffer)) != -1) {
                    builder.append(new String(buffer, 0, len));
                }
                return builder.toString();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                closeStream(is);
            }
        }
        return null;
    }

    /**
     * 将asset资源文件拷贝到指定目录
     *
     * @param context       上下文
     * @param assetFileName 资源文件名称
     * @param dest          目标文件绝对路径
     * @param isCover       是否覆盖
     * @return 如果拷贝成功返回true
     */
    public static boolean copyFileFromAsset(Context context, String assetFileName, File dest, boolean isCover) {
        if (TextUtils.isEmpty(assetFileName) || dest == null) {
            return false;
        }
        File destParent = dest.getParentFile();
        if (!destParent.exists() && !destParent.mkdirs()) {
            return false;
        }
        if (dest.exists() && !isCover) {
            return false;
        }
        AssetManager assetManager = context.getAssets();
        if (assetManager == null) {
            return false;
        }
        InputStream is = null;
        FileOutputStream os = null;
        try {
            is = assetManager.open(assetFileName);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[4096];
            int len;
            while ((len = is.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(is);
            closeStream(os);
        }
        return false;
    }

    /**
     * 从文件中读取字符串
     *
     * @param parent 文件目录
     * @param child  文件名称
     * @return 文件内容字符串
     */
    public static String readStringFromFile(String parent, String child) {
        if (TextUtils.isEmpty(parent) || TextUtils.isEmpty(child)) {
            return null;
        } else {
            File file = new File(parent, child);
            return readStringFromFile(file);
        }
    }

    /**
     * 从文件中读取字符串
     *
     * @param filePath 文件绝对路径
     * @return 文件内容字符串
     */
    public static String readStringFromFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        } else {
            File file = new File(filePath);
            return readStringFromFile(file);
        }
    }

    /**
     * 从文件中读取字符串
     *
     * @param file 目标文件
     * @return 文件内容字符串
     */
    public static String readStringFromFile(File file) {
        if (file == null || !file.exists() || !file.isFile()) {
            return null;
        }
        BufferedReader bufferedReader = null;
        try {
            FileReader fileReader = new FileReader(file);
            bufferedReader = new BufferedReader(fileReader);
            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = bufferedReader.readLine()) != null) {
                builder.append(line);
            }
            return builder.toString();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(bufferedReader);
        }
        return null;
    }

    /**
     * 删除所有子文件
     *
     * @param filePath 需要删除文件的目录
     */
    public static void deleteAllChildFiles(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        deleteAllChildFiles(file);
    }

    /**
     * 删除所有子文件
     *
     * @param file 需要删除文件的目录
     */
    public static void deleteAllChildFiles(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) {
            return;
        }
        File[] files = file.listFiles();
        if (files == null || files.length == 0) {
            return;
        }
        for (File childFile : files) {
            deleteAllFile(childFile);
        }
    }

    public static void deleteAllFile(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        deleteAllFile(file);
    }

    /**
     * 删除指定文件包括子目录
     *
     * @param file 需要删除的文件
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void deleteAllFile(File file) {
        if (file == null || !file.exists()) {
            return;
        }
        if (file.isFile()) {
            file.delete();
            return;
        }
        File[] files = file.listFiles();
        for (File f : files) {
            deleteAllFile(f);
        }
        file.delete();
    }

    /**
     * 关闭文件流
     *
     * @param closeable 待关闭对象
     */
    public static void closeStream(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拷贝文件
     *
     * @param sourcePath 需要拷贝的文件
     * @param destPath   目标路径
     * @param isCover    如果目标存在是否覆盖
     * @return 如果拷贝成功返回true，否则返回false
     */
    public static boolean copyFile(String sourcePath, String destPath, boolean isCover) {
        if (TextUtils.isEmpty(sourcePath) || TextUtils.isEmpty(destPath)) {
            return false;
        }
        File sourceFile = new File(sourcePath);
        File destFile = new File(destPath);
        return copyFile(sourceFile, destFile, isCover);
    }

    /**
     * 拷贝文件
     *
     * @param source  需要拷贝的文件
     * @param dest    拷贝到的文件
     * @param isCover 如果目标存在是否覆盖
     * @return 如果拷贝成功返回true，否则返回false
     */
    public static boolean copyFile(File source, File dest, boolean isCover) {
        if (source == null || dest == null || !source.exists() || !source.isFile()) {
            return false;
        }
        File destParent = dest.getParentFile();
        if (!destParent.exists() && !destParent.mkdirs()) {
            return false;
        }
        if (dest.exists() && !isCover) {
            return false;
        }
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeStream(outputChannel);
            closeStream(inputChannel);
        }
        return false;
    }

    /**
     * 获取文件大小
     *
     * @param path 文件路径
     * @return 文件大小，单位byte
     */
    public static long getFileSize(String path) {
        if (TextUtils.isEmpty(path)) {
            return 0;
        }
        File file = new File(path);
        if (!file.exists()) {
            return 0;
        }
        return file.length();
    }

    /**
     * 将文件名转成uuid形式，保留文件后缀
     *
     * @param sourceName 需要转换的文件
     * @return 转换后的文件名
     */
    public static String getUUIdFileName(String sourceName) {
        if (TextUtils.isEmpty(sourceName)) {
            return "";
        }
        int index = sourceName.lastIndexOf('.');
        String postfix = "";
        if (index > 0 && index < sourceName.length()) {
            postfix = sourceName.substring(index);
        }
        UUID uuid = UUID.randomUUID();
        String name = uuid.toString().replace("-", "");
        return name + postfix;
    }

    /**
     * 判断文件是否存在
     *
     * @param path 文件路径
     * @return 如果存在将返回true，否者返回false
     */
    public static boolean fileExists(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        File file = new File(path);
        return file.exists();
    }

    /**
     * 解压文件
     *
     * @param zipFile zip压缩文件
     * @return 如果解压成功返回true，否者返回false
     */
    public static boolean unzip(File zipFile) {
        if (!zipFile.exists()) {
            return false;
        }

        String dir = zipFile.getParent();
        ZipFile zip = null;
        byte[] buffer = new byte[4096];
        int len;
        try {
            zip = new ZipFile(zipFile);
            Enumeration entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                if (entry.isDirectory()) {
                    File childDir = new File(dir, entry.getName().replace("../", "_"));
                    childDir.mkdirs();
                    continue;
                }
                OutputStream os = new FileOutputStream(new File(dir, entry.getName().replace("../", "_")));
                InputStream is = zip.getInputStream(entry);
                while ((len = is.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
                closeStream(os);
                closeStream(is);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (zip != null) {
                try {
                    zip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * 通过FileProvider Uri 获取文件路径
     *
     * @param context
     * @param uri
     * @return
     */
    private String getFPUriToPath(Context context, Uri uri) {
        try {
            List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
            if (packs != null) {
                String fileProviderClassName = FileProvider.class.getName();
                for (PackageInfo pack : packs) {
                    ProviderInfo[] providers = pack.providers;
                    if (providers != null) {
                        for (ProviderInfo provider : providers) {
                            if (uri.getAuthority().equals(provider.authority)) {
                                if (provider.name.equalsIgnoreCase(fileProviderClassName)) {
                                    Class<FileProvider> fileProviderClass = FileProvider.class;
                                    try {
                                        Method getPathStrategy = fileProviderClass.getDeclaredMethod("getPathStrategy", Context.class, String.class);
                                        getPathStrategy.setAccessible(true);
                                        Object invoke = getPathStrategy.invoke(null, context, uri.getAuthority());
                                        if (invoke != null) {
                                            String PathStrategyStringClass = FileProvider.class.getName() + "$PathStrategy";
                                            Class<?> PathStrategy = Class.forName(PathStrategyStringClass);
                                            Method getFileForUri = PathStrategy.getDeclaredMethod("getFileForUri", Uri.class);
                                            getFileForUri.setAccessible(true);
                                            Object invoke1 = getFileForUri.invoke(invoke, uri);
                                            if (invoke1 instanceof File) {
                                                String filePath = ((File) invoke1).getAbsolutePath();
                                                return filePath;
                                            }
                                        }
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
