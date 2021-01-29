package com.bank.shellx.ui.customUtilsUi;

import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bank.shellx.R;
import com.bank.shellx.application.MyApplication;
import com.bank.shellx.ui.adapter.pickPackageNameAdapter;
import com.bank.shellx.utils.appInfo.AppInfo;

//选择包名的工具util
public class PickPackageActivity extends AppCompatActivity {

    private RecyclerView rv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pick_package);
        rv = findViewById(R.id.pick_pkg_recycler_view);
        //获取editText
        EditText e = null;
        if (MyApplication.getEditText() !=null){
             e = MyApplication.getEditText();
            MyApplication.setEditText(null);
        }else{
            finish();
        }
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(new pickPackageNameAdapter(this, AppInfo.getPackagesInfoList(getPackageManager()),e));
    }
}
