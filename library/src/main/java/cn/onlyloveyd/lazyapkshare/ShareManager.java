package cn.onlyloveyd.lazyapkshare;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import java.io.File;

import cn.onlyloveyd.lazyapkshare.utils.AppInfo;
import cn.onlyloveyd.lazyapkshare.utils.FileUtils;
import cn.onlyloveyd.lazyapkshare.utils.LazyFileProvider;
import cn.onlyloveyd.lazyapkshare.widgets.LoadingDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * 文 件 名: ShareManager
 * 创建日期: 2018/6/26 07:26
 * 邮   箱: onlyloveyd@gmail.com
 * 博   客: https://onlyloveyd.cn
 * 描   述：ShareManager 应用抽取分享
 *
 * @author: yidong
 */
public class ShareManager {
    private PackageManager packageManager;
    private PackageInfo packageInfo;
    private Context context;
    private LoadingDialog loadingDialog;

    public ShareManager(Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
        loadingDialog = new LoadingDialog(context);
        loadingDialog.setLoadingText("正在分享");
    }

    public void shareApp(final String packageName) {
        Observer<Uri> observer = new Observer<Uri>() {
            @Override
            public void onSubscribe(Disposable d) {
                loadingDialog.show();
            }

            @Override
            public void onNext(Uri uri) {
                loadingDialog.dismiss();
                if (uri != null) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_SEND);
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    intent.setType("application/vnd.android.package-archive");
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(
                            Intent.createChooser(
                                    intent,
                                    String.format("发送", context.getString(R.string.app_name))));
                }
            }

            @Override
            public void onError(Throwable e) {
                loadingDialog.dismiss();
                e.printStackTrace();
            }

            @Override
            public void onComplete() {
                loadingDialog.dismiss();
            }
        };

        Observable<Uri> observable = Observable.create(new ObservableOnSubscribe<Uri>() {
            @Override
            public void subscribe(ObservableEmitter<Uri> emitter) {
                try {
                    packageInfo = packageManager.getPackageInfo(packageName,
                            PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                    throw new IllegalArgumentException("package name does not exist!!!");
                }
                AppInfo appInfo =
                        new AppInfo(
                                packageManager.getApplicationLabel(
                                        packageInfo.applicationInfo).toString(),
                                packageInfo.packageName,
                                packageInfo.versionName,
                                packageInfo.applicationInfo.sourceDir,
                                packageInfo.applicationInfo.dataDir,
                                packageManager.getApplicationIcon(packageInfo.applicationInfo),
                                false);

                File initialFile = new File(appInfo.getSource());
                File targetFile = new File(
                        Environment.getExternalStorageDirectory().getAbsolutePath()
                                + "/Base/base.apk");
                if (!targetFile.exists()) {
                    targetFile.mkdir();
                }
                Uri uri = null;
                boolean isSuccess = FileUtils.copyFile(initialFile, targetFile, true);
                if (isSuccess) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        uri =
                                LazyFileProvider.getUriForFile(
                                        context,
                                        packageName + LazyFileProvider.LAZY_FILE_PROVIDER_AUTHORITY,
                                        targetFile);
                    } else {
                        uri = Uri.fromFile(targetFile);
                    }
                }
                emitter.onNext(uri);
            }
        });
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }


}
