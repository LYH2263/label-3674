package com.label.community.listener;

import com.label.community.config.DbConfig;
import com.label.community.service.SystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@WebListener
public class AppLifecycleListener implements ServletContextListener {
    private static final Logger log = LoggerFactory.getLogger(AppLifecycleListener.class);
    private final SystemService systemService = new SystemService();
    private ScheduledExecutorService scheduler;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        sce.getServletContext().setSessionTimeout(30);
        DbConfig.dataSource();
        systemService.refreshHotDataCache();

        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(() -> {
            try {
                MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
                MemoryUsage heap = memoryMXBean.getHeapMemoryUsage();
                long usedMb = heap.getUsed() / (1024 * 1024);
                long maxMb = heap.getMax() / (1024 * 1024);
                log.info("system monitor: onlineUsers={}, heapUsed={}MB/{}MB", OnlineUserTracker.onlineUsers(), usedMb, maxMb);
                systemService.refreshHotDataCache();
            } catch (Exception ex) {
                log.warn("monitor task failed: {}", ex.getMessage());
            }
        }, 20, 60, TimeUnit.SECONDS);

        log.info("Smart Community application started");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        DbConfig.shutdown();
        log.info("Smart Community application stopped");
    }
}
