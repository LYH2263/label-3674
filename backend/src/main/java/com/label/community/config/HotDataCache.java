package com.label.community.config;

import com.label.community.model.NoticeItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class HotDataCache {
    private static final CopyOnWriteArrayList<NoticeItem> HOT_NOTICES = new CopyOnWriteArrayList<>();

    private HotDataCache() {
    }

    public static void refreshNotices(List<NoticeItem> notices) {
        HOT_NOTICES.clear();
        HOT_NOTICES.addAll(notices);
    }

    public static List<NoticeItem> notices() {
        return Collections.unmodifiableList(new ArrayList<>(HOT_NOTICES));
    }
}
