package com.bank.shellx.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bank.shellx.utils.ui.ActivityCollector;


//基础baseactivity
public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCollector.addActivity(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ActivityCollector.removeActivity(this);
    }

    protected void finishAllActivity(){
        ActivityCollector.finishAll();
    }

    protected void toast(String msg){
        runOnUiThread(() -> Toast.makeText(this,msg,Toast.LENGTH_SHORT).show());

    }

    protected Intent makeIntent(Class cls){
        return new Intent(this,cls);
    }
}
