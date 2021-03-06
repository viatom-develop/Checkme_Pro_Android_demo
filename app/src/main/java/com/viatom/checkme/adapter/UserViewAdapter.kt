package com.viatom.checkme.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.viatom.checkme.R;
import com.viatom.checkme.bean.UserBean;
import com.viatom.checkme.utils.Constant;

import java.util.ArrayList;
import java.util.List;

public class UserViewAdapter extends RecyclerView.Adapter<UserViewAdapter.ViewHolder> {
    public List<UserBean> mUserData;
    private LayoutInflater mInflater;
    private userClickListener mClickListener;
    private Context mContext;
    private RecyclerView recyclerView;

    // data is passed into the constructor
    public UserViewAdapter(Context context, RecyclerView r) {
        this.mInflater = LayoutInflater.from(context);
        this.mUserData = new ArrayList<>();
        this.recyclerView = r;
        mContext = context;
    }


    // inflates the cell layout from xml when needed
    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.item_user_view, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each cell
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bleName.setText(mUserData.get(position).getName());
        holder.head.setImageResource(Constant.ICO_IMG[mUserData.get(position).getIco() - 1]);
    }

    public void addUser(UserBean userBean) {
        mUserData.add(userBean);
        notifyDataSetChanged();
    }

    public void addUserS(UserBean userBean) {
        mUserData.add(userBean);
    }

    public void setUser(int position) {
        for (int k = 0; k < mUserData.size(); k++) {
            ViewHolder v = (ViewHolder) recyclerView.findViewHolderForAdapterPosition(k);
            if (v == null) return;
            if (v.gaga == null) return;
            if (k != position) {
                v.gaga.setBackgroundColor(Color.parseColor("#0000F0"));
            } else {
                v.gaga.setBackgroundColor(Color.parseColor("#FF00FF"));
            }
        }

    }


    // total number of cells
    @Override
    public int getItemCount() {
        return mUserData.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView bleName;
        LinearLayout gaga;
        ImageView head;

        ViewHolder(View itemView) {
            super(itemView);
            head = itemView.findViewById(R.id.head);
            bleName = itemView.findViewById(R.id.userName);
            gaga = itemView.findViewById(R.id.gaga);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            setUser(getAdapterPosition());
            if (mClickListener != null)
                mClickListener.onUserItemClick(mUserData.get(getAdapterPosition()), getAdapterPosition());
        }
    }


    // allows clicks events to be caught
    public void setClickListener(userClickListener userClickListener) {
        this.mClickListener = userClickListener;
    }

    public interface userClickListener {
        void onUserItemClick(UserBean userBean, int position);
    }
}