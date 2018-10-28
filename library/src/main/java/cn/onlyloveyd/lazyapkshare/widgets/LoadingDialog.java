package cn.onlyloveyd.lazyapkshare.widgets;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.widget.TextView;

import cn.onlyloveyd.lazyapkshare.R;


/**
 * 文 件 名: LoadingDialog
 * 创建日期: 2018/6/26 07:26
 * 邮   箱: onlyloveyd@gmail.com
 * 博   客: https://onlyloveyd.cn
 * 描   述：加载框
 *
 * @author: yidong
 */
public class LoadingDialog extends Dialog {
    private TextView mLoadingTextTv;
    private String mLoadingText;

    public LoadingDialog(@NonNull Context context) {
        super(context, R.style.LoadingDialog);
    }

    public LoadingDialog(@NonNull Context context, boolean cancelable) {
        super(context, R.style.LoadingDialog);
        setCancelable(cancelable);
    }

    public static LoadingDialog show(Context context, boolean cancelable) {
        LoadingDialog dialog = new LoadingDialog(context);
        dialog.setCancelable(cancelable);
        dialog.show();
        return dialog;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_loading);
        mLoadingTextTv = findViewById(R.id.loading_text);
        if (!TextUtils.isEmpty(mLoadingText)) {
            mLoadingTextTv.setText(mLoadingText);
        }
    }

    public void setLoadingText(String loadingText) {
        this.mLoadingText = loadingText;
        if (mLoadingTextTv != null) {
            if (TextUtils.isEmpty(mLoadingText)) {
                mLoadingTextTv.setText(R.string.loading_dialog_text);
            } else {
                mLoadingTextTv.setText(loadingText);
            }
        }
    }
}
