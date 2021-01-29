package com.bank.shellx.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.shellx.R;
import com.bank.shellx.utils.javaBean.PackagesBean;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

//获取应用包名字符串的recyclerview的适配器
public class pickPackageNameAdapter extends RecyclerView.Adapter<pickPackageNameAdapter.ViewHolder> {

    private final AppCompatActivity activity;
    private final EditText editText;
    private List<PackagesBean> pkgs;

    public pickPackageNameAdapter(AppCompatActivity activity, List<PackagesBean> pkgs, EditText ediText) {
        this.activity = activity;
        this.pkgs = pkgs;
        this.editText = ediText;
    }

    @Override
    public pickPackageNameAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.package_name_item,parent,false));
    }

    @Override
    public void onBindViewHolder(pickPackageNameAdapter.ViewHolder holder, int position) {
        final PackagesBean pkg = pkgs.get(position);
        holder.pkgLabel.setText(pkg.getLabel());
        holder.pkgName.setText(pkg.getName());
        holder.pkgIcon.setBackground(pkg.getIcon());
        holder.itemView.setOnClickListener(
                v -> {
                    editText.setText(pkg.getName());
                    activity.finish();
                }
        );
    }

    @Override
    public int getItemCount() {
        return pkgs.size();
    }

    //内部viewholder类
    static class ViewHolder extends RecyclerView.ViewHolder{

        ShapeableImageView pkgIcon;
        TextView pkgLabel,pkgName;

        public ViewHolder(View itemView) {
            super(itemView);
            pkgIcon = itemView.findViewById(R.id.pkg_icon);
            pkgLabel = itemView.findViewById(R.id.pkg_label);
            pkgName = itemView.findViewById(R.id.pkg_name);
        }
    }
}
