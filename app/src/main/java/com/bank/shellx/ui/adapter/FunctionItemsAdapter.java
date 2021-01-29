package com.bank.shellx.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bank.shellx.R;
import com.bank.shellx.utils.javaBean.FunctionsBean;
import com.google.android.material.button.MaterialButton;

import java.util.List;

import drawthink.expandablerecyclerview.adapter.BaseRecyclerViewAdapter;
import drawthink.expandablerecyclerview.bean.RecyclerViewData;
import drawthink.expandablerecyclerview.holder.BaseViewHolder;

public class FunctionItemsAdapter extends BaseRecyclerViewAdapter<String, FunctionsBean,FunctionItemsAdapter.ButtonViewHolder> {

    private Context ctx;
    private List datas;
    private LayoutInflater mInflater;

    public FunctionItemsAdapter(Context ctx, List<RecyclerViewData> datas) {
        super(ctx, datas);
        mInflater = LayoutInflater.from(ctx);
        this.ctx = ctx;
        this.datas = datas;
    }
    @Override
    public View getGroupView(ViewGroup parent) {
        return mInflater.inflate(R.layout.function_title,parent,false);
    }

    @Override
    public View getChildView(ViewGroup parent) {
        return mInflater.inflate(R.layout.function_item,parent,false);
    }

    @Override
    public ButtonViewHolder createRealViewHolder(Context ctx, View view, int viewType) {
        return new ButtonViewHolder(ctx, view, viewType);
    }

    @Override
    public void onBindGroupHolder(ButtonViewHolder holder, int groupPos, int position, String groupData) {
        holder.tv.setText(groupData);
    }

    @Override
    public void onBindChildpHolder(ButtonViewHolder holder, int groupPos, int childPos, int position, final FunctionsBean childData) {
        holder.mb.setOnClickListener(
                v -> childData.getCallBack().onClick()
        );
        holder.mb.setIcon(ctx.getResources().getDrawable(childData.getIcon(),null));
        holder.mb.setText(childData.getName());
    }

    public static class ButtonViewHolder extends BaseViewHolder {

        MaterialButton mb;
        TextView tv;

        public ButtonViewHolder(Context ctx, View itemView, int viewType) {
            super(ctx, itemView, viewType);
            mb = itemView.findViewById(R.id.function_item);
            tv = itemView.findViewById(R.id.function_title);
        }

        @Override
        public int getChildViewResId() {
            return R.id.function_child;
    }

        @Override
        public int getGroupViewResId() {
            return R.id.function_group;
        }
    }
}
