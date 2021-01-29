package com.bank.shellx.io.rxio;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

//一个io封装的rxjava类

public class RxIO {

    //wirte mode
    public static int WRITE_APPEND = 1;
    public static int WRITE_COVER = 2;

    public static String CUSTOM_COMMAND = "command.json";

    //读取sd卡文件（需权限）
    public static Observable<FileInputStream> open(String path) throws IOException {
        return RxIO.open(new File(path));
    }

    public static Observable<FileInputStream> open(Uri path) throws IOException {
        return RxIO.open(new File(Objects.requireNonNull(path.getPath())));
    }

    public static Observable<FileInputStream> open(final File file) throws IOException {
        if (!file.exists()) {
            file.createNewFile();
        }
        return Observable.create(
                new ObservableOnSubscribe<FileInputStream>() {
                    @Override
                    public void subscribe(ObservableEmitter<FileInputStream> e) throws FileNotFoundException {
                        e.onNext(new FileInputStream(file));
                        e.onComplete();
                    }
                }
        ).observeOn(Schedulers.io());
    }

    //数据读取方法：
    public static Observable<String> getData(final Context ctx, final String name) {
        return Observable.create(
                new ObservableOnSubscribe<String>() {
                    @Override
                    public void subscribe(ObservableEmitter<String> emitter) throws Exception {
                        FileInputStream fileStream = null;
                        try {
                            fileStream = ctx.openFileInput(name);
                            int length = fileStream.available();//获取文件长度
                            byte[] buffer = new byte[length];//创建byte数组用于读入数据
                            emitter.onNext(buffer.toString());
                            fileStream.close();
                        } catch (IOException e) {
                            emitter.onError(e);
                        }
                    }
                }
        ).observeOn(Schedulers.io());
    }

    public static Observer<String> setData(final Context ctx,final String name){
        return new Observer<String>(){

            Disposable d;
            @Override
            public void onSubscribe(Disposable d) {
                this.d = d;
            }

            @Override
            public void onNext(String s) {
                FileOutputStream fileOutputStream;
                try{
                    fileOutputStream = ctx.openFileOutput(name,Context.MODE_PRIVATE);
                    fileOutputStream.write(s.getBytes());
                    fileOutputStream.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        };
    }
}
