package wanghaisheng.com.facemyage.mrdialog;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.ref.WeakReference;

import wanghaisheng.com.facemyage.R;

public class MrHUD {

    private MrHUDDialog dialog;
    private WeakReference<Context> contextRef;
    private MrHUDCallback callback;

    public static int dismissDelay = MrHUD.DISMISS_DELAY_SHORT;
    public static final int DISMISS_DELAY_SHORT = 2000;
    public static final int DISMISS_DELAY_MIDIUM = 4000;
    public static final int DISMISS_DELAY_LONG = 6000;
    public static final int DIALOG_TYPE_ERROR = 0x001;
    public static final int DIALOG_TYPE_INFO = 0x002;
    public static final int DIALOG_TYPE_SUCCESS = 0x003;

    public MrHUD(Context context) {
        contextRef = new WeakReference<>(context);

        dialog = MrHUDDialog.createDialog(contextRef.get());

    }

    private void setDialog(String msg, int resId, boolean cancelable) {
        if(dialog == null) {
            dialog = MrHUDDialog.createDialog(contextRef.get());
        }

        dialog.setMessage(msg);
        dialog.setImage(resId);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(cancelable);        // back键是否可dimiss对话框
    }

    public void showLoadingMessage(String msg, boolean cancelable, MrHUDCallback callback) {
        this.callback = callback;
        showLoadingMessage(msg, cancelable);
    }

    public void showLoadingMessage(String msg, boolean cancelable) {
        dismiss();
        setDialog(msg, R.drawable.dialog_loading, cancelable);
        if (dialog != null) dialog.show();
    }

    public void showErrorMessage(String msg, MrHUDCallback callback) {
        this.callback = callback;
        showErrorMessage(msg);
    }

    public void showErrorMessage(String msg) {
        dismiss();
        setDialog( msg, R.drawable.dialog_err, true);

        if (dialog != null) {
            Log.d("mrhud ","show dialog  ");
            dialog.show();
            dismissAfterSeconds();
        }
    }


    public void showSuccessMessage(String msg, MrHUDCallback callback) {
        this.callback = callback;
        showSuccessMessage(msg);
    }

    public void showSuccessMessage(String msg) {
        dismiss();
        setDialog(msg, R.drawable.dialog_success, true);
        if (dialog != null) {
            dialog.show();
            dismissAfterSeconds();
        }
    }

    public void showInfoMessage(String msg, MrHUDCallback callback) {
        this.callback = callback;
        showInfoMessage(msg);
    }

    public void showInfoMessage(String msg) {
        dismiss();
        setDialog(msg, R.drawable.dialog_info, true);
        if (dialog != null) {
            dialog.show();
            dismissAfterSeconds();
        }
    }

    public void dismiss() {
        if (isContextValid() && dialog != null && dialog.isShowing())
            dialog.dismiss();
        dialog = null;
    }

    /**
     * 计时关闭对话框
     */
    private void dismissAfterSeconds() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(dismissDelay);
                    handler.sendEmptyMessage(0);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private DismissHandler handler = new DismissHandler(MrHUD.this);

    private static class DismissHandler extends Handler{
        WeakReference<MrHUD> mrhudRef;

        public DismissHandler(MrHUD mrHUD) {
            mrhudRef = new WeakReference<MrHUD>(mrHUD);
        }

        public void handleMessage(Message msg) {
            if (msg.what == 0) {
                mrhudRef.get().dismiss();
                if (mrhudRef.get().callback != null) {
                    mrhudRef.get().callback.onMrHUDMissed();
                    mrhudRef.get().callback = null;
                }
            }
        }
    }


    /**
     * 判断parent view是否还存在
     * 若不存在不能调用dismis，或setDialog等方法
     *
     * @return
     */
    private boolean isContextValid() {
        if (contextRef == null)
            return false;
        if (contextRef.get() instanceof Activity) {
            Activity act = (Activity) contextRef.get();
            if (act.isFinishing())
                return false;
        }
        return true;
    }


    public interface MrHUDCallback {
        void onMrHUDMissed();
    }
}
