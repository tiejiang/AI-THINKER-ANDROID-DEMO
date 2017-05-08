package com.bear.aithinker.a20camera.tool;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bear.aithinker.a20camera.R;


/**
 * Created by Administrator on 2016/7/15 0015.
 */

public class DialogManager {

    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg
     * @return
     */
    public static Dialog createLoadingDialog(Context context, String msg) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.dialog_view);// 加载布局
        // main.xml中的ImageView
        ImageView spaceshipImage = (ImageView) v.findViewById(R.id.img);
        TextView tipTextView = (TextView) v.findViewById(R.id.tipTextView);// 提示文字
        // 加载动画
        Animation hyperspaceJumpAnimation = AnimationUtils.loadAnimation(
                context, R.anim.loading_animation);
        // 使用ImageView显示动画
        spaceshipImage.startAnimation(hyperspaceJumpAnimation);
        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loading_dialog);// 创建自定义样式dialog

        loadingDialog.setCancelable(true);// 不可以用“返回键”取消
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT));// 设置布局
        return loadingDialog;

    }

    public static ProgressDialog progressDialog(Context context, String msg) {
        ProgressDialog mpDialog = new ProgressDialog(context);
        mpDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);//设置风格为圆形进度条
        mpDialog.setMessage(msg);
        mpDialog.setIndeterminate(false);//设置进度条是否为不明确
        mpDialog.setCancelable(true);//设置进度条是否可以按退回键取消
//        设置点击进度对话框外的区域对话框不消失
        mpDialog.setCanceledOnTouchOutside(false);
        return mpDialog;
    }

    /***
     * Gets the no network dialog.
     *
     * @param ctx
     * @return the no network dialog
     */
    public static Dialog getNoNetworkDialog(Context ctx) {
        Dialog dialog = new Dialog(ctx, R.style.noBackgroundDialog);
        LayoutInflater layoutInflater = LayoutInflater.from(ctx);
        View contentView = layoutInflater.inflate(R.layout.dialog_no_network,
                null);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(contentView);
        return dialog;
    }

    /**
     * Show dialog.
     *
     * @param ctx
     *            the ctx
     * @param dialog
     *            the dialog
     */
    public static void showDialog(Context ctx, Dialog dialog) {
        if (dialog != null && !dialog.isShowing() && ctx != null
                && !((Activity) ctx).isFinishing()) {
            dialog.show();
        }
    }

    /**
     * 隐藏dialog，加了context生命判断，避免窗口句柄泄漏.
     *
     * @param ctx
     *            dialog依赖的activity
     * @param dialog
     *            欲隐藏的dialog
     */
    public static void dismissDialog(Activity ctx, Dialog dialog) {
        if (dialog != null && dialog.isShowing() && ctx != null
                && !ctx.isFinishing())
            dialog.dismiss();
    }


}
