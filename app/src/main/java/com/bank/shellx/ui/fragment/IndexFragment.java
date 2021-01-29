package com.bank.shellx.ui.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.bank.shellx.R;
import com.bank.shellx.ui.adb.AdbShellActivity;
import com.bank.shellx.utils.netWork.IPManager;
import com.bank.shellx.utils.ui.intentInterface.DataTransmitter;
import com.google.android.material.button.MaterialButton;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rxhttp.wrapper.param.RxHttp;

//主页面fragment
public class IndexFragment extends Fragment implements DataTransmitter<ViewPager2> {

    MaterialButton toFunctions, appDownload, command;
    TextView ip;
    ViewPager2 vp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_index, container, false);
    }

    @SuppressLint("CheckResult")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView tv = (TextView) view.findViewById(R.id.one_text);
        ip = (TextView) view.findViewById(R.id.ip_text);
        toFunctions = view.findViewById(R.id.index_apps);
        appDownload = view.findViewById(R.id.index_cloud);
        command = view.findViewById(R.id.index_cmd);
        toFunctions.setOnClickListener(
                v -> {
                    vp.setCurrentItem(1);
                }
        );
        command.setOnClickListener(
                v -> {
                    AdbShellActivity.start(getContext(),"192.168.0.100",5555);
                }
        );
        //获取一言
        RxHttp.get("https://v1.hitokoto.cn")
                .add("encode", "text")
                .add("max_length", 7)
                .add("min_length", 4)
                .asString()
                .observeOn(Schedulers.io())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(tv::setText, Throwable::printStackTrace);


    }

    @Override
    public void onTransfer(ViewPager2 vp) {
        this.vp = vp;
    }

    @SuppressLint("CheckResult")
    @Override
    public void onResume() {
        super.onResume();
        //获取ip
        Observable.create(
                (ObservableOnSubscribe<String>) emitter -> {
                    emitter.onNext(IPManager.getIPAddress(getContext()));
                }).subscribe(ip::setText);
    }
}
