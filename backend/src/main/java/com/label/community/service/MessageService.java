package com.label.community.service;

import com.label.community.config.TransactionManager;
import com.label.community.dao.MessageDao;
import com.label.community.model.MessageItem;
import com.label.community.websocket.NotificationWebSocketEndpoint;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class MessageService {
    private final MessageDao messageDao = new MessageDao();

    public List<MessageItem> list(Long userId) {
        return messageDao.listByReceiver(userId);
    }

    public void markRead(Long userId, Long messageId) {
        TransactionManager.runInTransaction(connection -> {
            messageDao.markRead(connection, messageId, userId);
            return null;
        });
        NotificationWebSocketEndpoint.pushToUser(userId, Map.of(
            "event", "message_read",
            "messageId", messageId,
            "unread", messageDao.countUnread(userId),
            "time", LocalDateTime.now().toString()
        ));
    }
}
