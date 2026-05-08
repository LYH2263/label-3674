package com.label.community.service;

import com.label.community.common.BusinessException;
import com.label.community.config.TransactionManager;
import com.label.community.dao.MessageDao;
import com.label.community.dao.PropertyDao;
import com.label.community.dto.VisitorCreateRequest;
import com.label.community.util.TimeUtil;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PropertyService {
    private static final Set<String> ALLOWED_CHANNEL = Set.of("ALIPAY", "WECHAT", "BANK_CARD");
    private final PropertyDao propertyDao = new PropertyDao();
    private final MessageDao messageDao = new MessageDao();

    public Map<String, Object> overview(Long userId) {
        Map<String, Object> map = new HashMap<>();
        map.put("payments", propertyDao.listPaymentsByUser(userId));
        map.put("paymentTransactions", propertyDao.listTransactionsByUser(userId));
        map.put("parkingSlots", propertyDao.listParkingSlots());
        map.put("visitors", propertyDao.listVisitorsByResident(userId));
        return map;
    }

    public void createVisitor(Long userId, VisitorCreateRequest request) {
        if (request == null || request.getVisitorName() == null || request.getVisitorName().isBlank()) {
            throw new BusinessException(400, 40060, "访客姓名不能为空");
        }
        LocalDateTime visitTime;
        try {
            visitTime = TimeUtil.parseDateTime(request.getVisitTime());
        } catch (Exception ex) {
            throw new BusinessException(400, 40061, "到访时间格式不正确");
        }
        TransactionManager.runInTransaction(connection -> {
            propertyDao.createVisitor(connection, userId, request.getVisitorName().trim(), visitTime);
            messageDao.createMessage(connection, userId, null, "SYSTEM", "访客登记完成", "访客「" + request.getVisitorName().trim() + "」已登记，访问时间为 " + request.getVisitTime());
            return null;
        });
    }

    public Map<String, Object> createPayIntent(Long userId, Long paymentId, String channel) {
        String finalChannel = channel == null ? "ALIPAY" : channel.trim().toUpperCase();
        if (!ALLOWED_CHANNEL.contains(finalChannel)) {
            throw new BusinessException(400, 40070, "支付渠道不支持");
        }
        Map<String, Object> payment = propertyDao.findPayment(userId, paymentId);
        if (payment == null) {
            throw new BusinessException(404, 40450, "物业费账单不存在");
        }
        if ("PAID".equals(payment.get("payStatus"))) {
            throw new BusinessException(400, 40071, "该物业费已缴纳");
        }

        String payNo = "PAY" + UUID.randomUUID().toString().replace("-", "").substring(0, 18).toUpperCase();
        TransactionManager.runInTransaction(connection -> {
            propertyDao.createPaymentTransaction(
                connection,
                paymentId,
                userId,
                payNo,
                finalChannel,
                (java.math.BigDecimal) payment.get("amount")
            );
            return null;
        });

        Map<String, Object> payload = new HashMap<>();
        payload.put("paymentId", paymentId);
        payload.put("payNo", payNo);
        payload.put("channel", finalChannel);
        payload.put("amount", payment.get("amount"));
        payload.put("status", "INITIATED");
        payload.put("qrPayload", "payNo:" + payNo + "|paymentId:" + paymentId);
        return payload;
    }

    public void confirmPay(Long userId, Long paymentId, String payNo) {
        if (payNo == null || payNo.isBlank()) {
            throw new BusinessException(400, 40072, "支付流水号不能为空");
        }
        Map<String, Object> tx = propertyDao.findTransaction(userId, paymentId, payNo.trim());
        if (tx == null) {
            throw new BusinessException(404, 40451, "支付流水不存在");
        }
        if ("SUCCESS".equals(tx.get("status"))) {
            throw new BusinessException(400, 40073, "该支付流水已完成");
        }
        Map<String, Object> payment = propertyDao.findPayment(userId, paymentId);
        if (payment == null) {
            throw new BusinessException(404, 40450, "物业费账单不存在");
        }

        TransactionManager.runInTransaction(connection -> {
            propertyDao.markTransactionSuccess(connection, payNo.trim());
            propertyDao.markPaymentSuccess(connection, paymentId, userId);
            messageDao.createMessage(connection, userId, null, "SYSTEM", "物业费缴纳成功", "物业费账单 #" + paymentId + " 已完成在线缴纳。");
            return null;
        });
    }
}
