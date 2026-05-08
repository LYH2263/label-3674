package com.label.community.service;

import com.label.community.config.HotDataCache;
import com.label.community.dao.NoticeDao;

public class SystemService {
    private final NoticeDao noticeDao = new NoticeDao();

    public void refreshHotDataCache() {
        HotDataCache.refreshNotices(noticeDao.latest(8));
    }
}
