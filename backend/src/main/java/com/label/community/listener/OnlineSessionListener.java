package com.label.community.listener;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

@WebListener
public class OnlineSessionListener implements HttpSessionListener, HttpSessionAttributeListener {
    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        Object logged = se.getSession().getAttribute("LOGIN_USER_ID");
        if (logged != null) {
            OnlineUserTracker.onLogoutSession();
        }
    }

    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        if ("LOGIN_USER_ID".equals(event.getName())) {
            OnlineUserTracker.onLoginSession();
        }
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {
        if ("LOGIN_USER_ID".equals(event.getName())) {
            OnlineUserTracker.onLogoutSession();
        }
    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {
        if ("LOGIN_USER_ID".equals(event.getName())) {
            // Replaced value does not change online count.
        }
    }
}
