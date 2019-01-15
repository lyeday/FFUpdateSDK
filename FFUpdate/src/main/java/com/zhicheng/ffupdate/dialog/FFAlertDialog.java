package com.zhicheng.ffupdate.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.zhicheng.ffupdate.R;

/**
 * Name:    FFAlertDialog
 * Author:  wuzhicheng
 * Time:    2019/1/14  09:09
 * Version: 1.0
 * Description: this is FFAlertDialog class.
 */
public class FFAlertDialog extends Dialog implements View.OnClickListener {

    private static final String TAG = "FFAlertDialog";
    public interface OnClickListener{
        void onClick(Dialog dialog, int i);
    }

    private String title = null;
    private String message = null;
    private String cancelTitle = null;
    private String sureTitle = null;
    private OnClickListener negativeListener = null;
    private OnClickListener positiveListener = null;

    private TextView mTitleView;
    private View mTopLineView;
    private TextView mContentView;
    private View mBottomLineView;
    private View mBtnLineView;
    private TextView mCancelView;
    private TextView mSureView;
    private LinearLayout mBtnsLayout;

    public FFAlertDialog(@NonNull Context context) {
        super(context);
    }

    public FFAlertDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    public FFAlertDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_custom);
        initUI();
        initListener();
    }

    private void initListener() {
        mSureView.setOnClickListener(this);
        mCancelView.setOnClickListener(this);
    }

    private void initUI() {
        mTitleView = findViewById(R.id.tv_dialog_title);
        mTopLineView = findViewById(R.id.v_dialog_top_line);
        mContentView = findViewById(R.id.tv_dialog_message);
        mBottomLineView = findViewById(R.id.v_dialog_bottom_line);
        mCancelView = findViewById(R.id.tv_dialog_cancel);
        mSureView = findViewById(R.id.tv_dialog_sure);
        mBtnsLayout = findViewById(R.id.ll_dialog_btns);
        mBtnLineView = findViewById(R.id.v_dialog_btn_line);
    }



    public FFAlertDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public FFAlertDialog setMessage(String message) {
        this.message = message;
        return this;
    }

    public FFAlertDialog setNegativeButton(String title,OnClickListener negativeListener) {
        this.negativeListener = negativeListener;
        this.cancelTitle = title;
        return this;
    }

    public FFAlertDialog setPositiveButton(String title,OnClickListener positiveListener) {
        this.positiveListener = positiveListener;
        this.sureTitle = title;
        return this;
    }

    public FFAlertDialog setCancelAble(boolean cancelAble){
        setCancelable(cancelAble);
        return this;
    }

    public void show(){
        super.show();
        if (title != null){
            mTitleView.setText(title);
        }else{
            mTitleView.setText("提示");
        }
        if (message != null){
            mContentView.setText(message);
            mContentView.setVisibility(View.VISIBLE);
        }else {
            mTopLineView.setVisibility(View.GONE);
            mContentView.setVisibility(View.GONE);
        }

        if (cancelTitle == null && sureTitle == null){
            mBtnsLayout.setVisibility(View.GONE);
            mBottomLineView.setVisibility(View.GONE);
        }else{
            mBtnsLayout.setVisibility(View.VISIBLE);
            mBottomLineView.setVisibility(View.VISIBLE);
            if (cancelTitle == null || sureTitle == null){
                mBtnLineView.setVisibility(View.GONE);
            }else{
                mBtnLineView.setVisibility(View.VISIBLE);
            }
            if (cancelTitle!=null){
                mCancelView.setText(cancelTitle);
                mCancelView.setVisibility(View.VISIBLE);
            }else {
                mCancelView.setVisibility(View.GONE);
            }
            if (sureTitle!=null){
                mSureView.setText(sureTitle);
                mSureView.setVisibility(View.VISIBLE);
            }else{
                mSureView.setVisibility(View.GONE);
            }
        }
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        getWindow().setAttributes(layoutParams);
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    @Override
    public void onClick(View view) {
        if (view == mCancelView && negativeListener != null){
            negativeListener.onClick(this,view.getId());
        }else
        if (view == mSureView && positiveListener != null){
            positiveListener.onClick(this,view.getId());
        }
        this.dismiss();
    }
}
