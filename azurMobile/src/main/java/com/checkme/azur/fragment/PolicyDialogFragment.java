package com.checkme.azur.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.checkme.newazur.R;

import static com.checkme.newazur.R.id.btn_agree;
import static com.checkme.newazur.R.id.btn_disagree;
import static com.checkme.newazur.R.id.privacy_policy;
import static com.checkme.newazur.R.id.privacy_policy_text;
import static com.checkme.newazur.R.id.viatom_term;
import static com.checkme.newazur.R.id.viatom_term_text;

/**
 * Created by lili on 2018/5/24.
 */

public class PolicyDialogFragment extends DialogFragment {

    private OnAgreeClickedListener listener;
  /*  @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        //final Window window = getDialog().getWindow();
       // getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        View view = inflater.inflate(R.layout.fragment_dialog_policy, container);
        //window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));//注意此处
        //window.setLayout(-1, -2);
        return view;
    }
*/
    @Override
    public void onAttach(Activity context) {
        super.onAttach(context);

        if (context instanceof PolicyDialogFragment.OnAgreeClickedListener) {
            listener = (PolicyDialogFragment.OnAgreeClickedListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            DisplayMetrics dm = new DisplayMetrics();
            getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
            dialog.getWindow().setLayout((int) (dm.widthPixels * 0.9), ViewGroup.LayoutParams.WRAP_CONTENT);
            //dialog.getWindow().setLayout((int) (dm.heightPixels * 0.65), ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
//        if (listener != null) {
//            listener.onAgreeClicked(true);
//        }

        listener = null;
    }

    public interface OnAgreeClickedListener {
        void onAgreeClicked(boolean b);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.fragment_dialog_policy, null);
        final CheckBox checkbox1 = (CheckBox)view.findViewById(viatom_term);
        final CheckBox checkbox2 = (CheckBox)view.findViewById(privacy_policy);
        final Button btnagree = (Button) view.findViewById(btn_agree);
        Button btndisgree = (Button) view.findViewById(btn_disagree);

        TextView text1 = (TextView) view.findViewById(viatom_term_text);
        TextView text2 = (TextView) view.findViewById(privacy_policy_text);
        text1.setMovementMethod(LinkMovementMethod.getInstance());
        text2.setMovementMethod(LinkMovementMethod.getInstance());

        checkbox1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){


            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b&&checkbox2.isChecked()){
                    btnagree.setBackgroundResource(R.color.default_bkg);
                }
                else {
                    btnagree.setBackgroundResource(R.color.DarkGray);
                }
            }
        });

        checkbox2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){


            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b&&checkbox1.isChecked()){
                    btnagree.setBackgroundResource(R.color.default_bkg);
                }
                else {
                    btnagree.setBackgroundResource(R.color.DarkGray);
                }
            }
        });

        if(checkbox1.isChecked()&&checkbox2.isChecked()){

            btnagree.setClickable(true);
        }

        setCancelable(false);//设置点击除dialog外(包括点击返回键)不消失,setCanceledOnTouchOutside(false)设置点击除dialog外(不包括点击返回键)不消失
        btnagree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(checkbox1.isChecked()&&checkbox2.isChecked()) {
                    listener.onAgreeClicked(true);
                    dismiss();
                }
            }
        });
        btndisgree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        builder.setView(view);
        return builder.create();

    }

    /*private void initListener() {

    }*/
}
