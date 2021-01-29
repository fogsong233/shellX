package com.bank.shellx.utils.ui.fuctionItemsBuilders;

import android.content.Intent;

import com.bank.shellx.R;
import com.bank.shellx.application.MyApplication;
import com.bank.shellx.ui.CustomAdbActivity;
import com.bank.shellx.ui.customUtilsUi.PickPackageActivity;
import com.bank.shellx.utils.javaBean.FunctionsBean;
import com.bank.shellx.utils.regex.StringParse;

import java.util.ArrayList;
import java.util.List;

import drawthink.expandablerecyclerview.bean.RecyclerViewData;

//功能列表静态功能data的持有
public class StaticItems {

    public static List getStaticItemsData(){
        List<RecyclerViewData> data = new ArrayList<>();
        data.add(getCommandItem());
        data.add(getWatchFunctionItem());
        return data;

    }
    //获取命令行功能列表
    private static RecyclerViewData getCommandItem(){
        List<FunctionsBean> data = new ArrayList<>();
        //dpi
        data.add(new FunctionsBean(
                "修改dpi",
                R.mipmap.dpi,
                () -> {}
        ));

        //卸载应用
        data.add(new FunctionsBean(
                "卸载应用",
                R.mipmap.uninstall,
                () -> StringParse.metaCommand("wm density {dpi:key}")
        ));
        return new RecyclerViewData("命令行工具",data,false);
    }

    private static RecyclerViewData getWatchFunctionItem(){
        List<FunctionsBean> data = new ArrayList<>();
        //查看包名
        data.add(new FunctionsBean(
                "查看包名",
                R.mipmap.pkg,
                () -> {
                    Intent i = new Intent(MyApplication.getContext(), PickPackageActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    MyApplication.getContext().startActivity(i);
                }
        ));
        //获取手表信息
        data.add(new FunctionsBean(
                "手表信息",
                R.mipmap.information,
                () -> CustomAdbActivity.start("测试","{an:pkg} {{aa:a}} wm {dpi:key}")
        ));
        return new RecyclerViewData("手表工具",data,false);
    }
}
