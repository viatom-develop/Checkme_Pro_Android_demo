package com.checkme.update;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.annotation.StyleRes;
import android.support.v4.app.DialogFragment;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.checkme.newazur.R;

/**
 * Created by gongguopei on 2017/12/26.
 */
public class DialogHelper<T extends View> extends DialogFragment{

    private View contentView;
    private SparseArray<T> views;

    private int gravity = Gravity.CENTER;
    @StyleRes
    private int style = R.style.CustomDialogTheme;
    private int bottomMargin = 0;

    public DialogHelper() {
        views = new SparseArray<>();
    }

    public static DialogHelper newInstance(Context context, @LayoutRes int layoutResId){
        DialogHelper dialogHelper = new DialogHelper();
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        dialogHelper.contentView = inflater.inflate(layoutResId, null);
        return dialogHelper;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), style);
//        Dialog dialog = builder.create();
        Dialog dialog = new Dialog(getActivity(), style);
        if(contentView != null) {
            dialog.setContentView(contentView);
        }


        Window window = dialog.getWindow();
        if(window != null) {
            window.setGravity(gravity);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams params = window.getAttributes();
            params.y = bottomMargin;
            window.setAttributes(params);
        }

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();

        Window window = getDialog().getWindow();
        if(window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    public boolean isShowing() {
        if(getDialog() != null) {
            return getDialog().isShowing();
        }
        return false;
    }

    public void closeDialog() {
        Dialog dialog = getDialog();
        if(dialog != null) {
            dialog.dismiss();
        }

//        dismiss();
    }

    public DialogHelper setDialogCancelable(boolean cancelable) {
        super.setCancelable(cancelable);
        return this;
    }

    public DialogHelper setGravity(int gravity) {
        this.gravity = gravity;
        return this;
    }

    public DialogHelper setStyle(@StyleRes int style) {
        this.style = style;
        return this;
    }

    public DialogHelper setBottomMargin(int bottomMargin) {
        this.bottomMargin = bottomMargin;
        return this;
    }

    @SuppressWarnings("unchecked")
    public T getView(@IdRes int viewId) {
        T view = views.get(viewId);
        if(view == null) {
            view = (T) contentView.findViewById(viewId);
            views.put(viewId, view);
        }
        return view;
    }

    @SuppressWarnings("unchecked")
    public DialogHelper addListener(@IdRes int viewId, View.OnClickListener listener) {
        T tmpView = views.get(viewId);
        if(tmpView == null) {
            tmpView = (T) contentView.findViewById(viewId);
            views.put(viewId, tmpView);
        }
        if(tmpView != null) {
            tmpView.setOnClickListener(listener);
        }

        return this;
    }

    public DialogHelper setVisible(@IdRes int viewId,  int visibility) {
        View view = getView(viewId);
        if(view != null) {
            view.setVisibility(visibility);
        }
        return this;
    }

    public DialogHelper setText(@IdRes int viewId, CharSequence text) {
        TextView view = (TextView) getView(viewId);
        view.setText(text);
        return this;
    }

    public DialogHelper setText(@IdRes int viewId, @StringRes int stringId) {
        TextView view = (TextView) getView(viewId);
        view.setText(stringId);
        return this;
    }
}
