package com.bank.shellx.ui;

import android.Manifest;
import android.os.Bundle;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.bank.shellx.R;
import com.bank.shellx.ui.adapter.FragmentAdapter;
import com.bank.shellx.ui.fragment.FunctionsFragment;
import com.bank.shellx.ui.fragment.IndexFragment;
import com.bank.shellx.utils.ui.intentInterface.DataTransmitter;
import com.permissionx.guolindev.PermissionX;

import java.util.ArrayList;
import java.util.List;

public class IndexActivity extends FragmentActivity {

    private static final String TAG = "IndexActivity";

    private ViewPager2 vp;
    DataTransmitter<ViewPager2> f1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.index);
        setViewPager();

    }

    //设置viewpager的操作
    private void setViewPager() {
        vp = findViewById(R.id.index_viewpager);
        List<Fragment> fragments = new ArrayList<>();
        f1 = new IndexFragment();
        fragments.add((Fragment)f1);
        fragments.add(new FunctionsFragment());
        vp.setAdapter(new FragmentAdapter(this, fragments));
    }

    @Override
    protected void onStart() {
        super.onStart();
        PermissionX.init(this)
                .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE)
                .request((allGranted, grantedList, deniedList) -> {
                    if (!allGranted) {
                        Toast.makeText(IndexActivity.this, "您拒绝了如下权限：" + deniedList, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        f1.onTransfer(vp);
    }
}
