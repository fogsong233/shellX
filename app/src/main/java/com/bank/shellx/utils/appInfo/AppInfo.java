package com.bank.shellx.utils.appInfo;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.bank.shellx.utils.javaBean.PackagesBean;

import java.util.ArrayList;
import java.util.List;

public class AppInfo {
    public static List<PackagesBean> getPackagesInfoList(PackageManager pm){
        List<PackagesBean> result = new ArrayList<>();
        PackagesBean pb;
        for (PackageInfo pi : pm.getInstalledPackages(0)){
            pb = new PackagesBean();
            pb.setLabel(pi.applicationInfo.loadLabel(pm).toString());
            pb.setIcon(pi.applicationInfo.loadIcon(pm));
            pb.setName(pi.packageName);
            pb.setSystemApp(!((pi.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0));
            result.add(pb);
        }
        return result;
    }
}
