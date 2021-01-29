package com.bank.shellx.web.website;

import android.content.res.AssetManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

public class FolderAssetManager {

    AssetManager assetManager;
    String rootPath;

    public FolderAssetManager(AssetManager assetManager,String rootPath){
        this.assetManager = assetManager;
        this.rootPath = rootPath;
    }

    public InputStream open(String path) throws IOException {
        return this.assetManager.open(Paths.get(this.rootPath,path).toString());
    }

    public InputStream openInRootPath(String path) throws IOException {
        return this.assetManager.open(path);
    }
}
