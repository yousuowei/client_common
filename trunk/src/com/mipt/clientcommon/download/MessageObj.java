package com.mipt.clientcommon.download;

import java.io.File;

class MessageObj {
    protected DownloadTask tsk;
    protected String downloadUrl;
    protected File apkFile;

    public MessageObj() {
	this(null, null);
    }

    public MessageObj(DownloadTask tsk, String downloadUrl) {
	this(tsk, downloadUrl, null);
    }

    public MessageObj(DownloadTask tsk, String downloadUrl, File apkFile) {
	this.tsk = tsk;
	this.downloadUrl = downloadUrl;
	this.apkFile = apkFile;
    }
}
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        