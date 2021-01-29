package com.bank.shellx.web.website;

public interface WebsiteStartListener {

    void onWebSiteStartSuccess();

    void onWebSiteStartFailed(Throwable e);
}
