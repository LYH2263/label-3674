package com.label.community.service;

import com.label.community.common.BusinessException;
import com.label.community.config.RoleConstants;
import com.label.community.config.TransactionManager;
import com.label.community.dao.MessageDao;
import com.label.community.dao.UserDao;
import com.label.community.dto.LoginRequest;
import com.label.community.dto.RegisterRequest;
import com.label.community.model.User;
import com.label.community.security.JwtUtil;
import com.label.community.security.PasswordUtil;
import com.label.community.security.SensitiveCryptoUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

public class AuthService {
    private static final Set<String> ALLOWED_ROLES = Set.of(
        RoleConstants.RESIDENT,
        RoleConstants.PROPERTY_ADMIN,
        RoleConstants.SERVICE_PROVIDER
    );
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1\\d{10}$");
    private static final Pattern IDCARD_PATTERN = Pattern.compile("^\\d{17}[0-9Xx]$");

    private final UserDao userDao = new UserDao();
    private final MessageDao messageDao = new MessageDao();

    public Map<String, Object> register(RegisterRequest request) {
        if (request == null) {
            throw new BusinessException(400, 40010, "请求参数不能为空");
        }
        validateRegisterRequest(request);
        Optional<User> exist = userDao.findByUsername(request.getUsername());
        if (exist.isPresent()) {
            throw new BusinessException(409, 40901, "用户名已存在");
        }

        String encryptedIdCard = null;
        boolean verified = false;
        if (RoleConstants.RESIDENT.equals(request.getRole())) {
            encryptedIdCard = SensitiveCryptoUtil.encrypt(request.getIdCardNo());
            verified = true;
        }

        User user = new User();
        user.setUsername(request.getUsername().trim());
        user.setPasswordHash(PasswordUtil.hash(request.getPassword()));
        user.setRole(request.getRole());
        user.setFullName(request.getFullName().trim());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setIdCardEnc(encryptedIdCard);
        user.setRealNameVerified(verified);
        user.setStatus("ACTIVE");

        Long userId = TransactionManager.runInTransaction(connection -> {
            Long id = userDao.insert(connection, user);
            messageDao.createMessage(
                connection,
                id,
                null,
                "SYSTEM",
                "欢迎加入智能社区",
                "账号创建成功。您可在个人中心查看报修、活动和消息通知。"
            );
            return id;
        });

        String token = JwtUtil.createToken(userId, user.getUsername(), user.getRole());
        return buildAuthPayload(userId, user.getUsername(), user.getRole(), user.getFullName(), token, verified);
    }

    public Map<String, Object> login(LoginRequest request) {
        if (request == null || request.getUsername() == null || request.getPassword() == null) {
            throw new BusinessException(400, 40011, "用户名或密码不能为空");
        }

        User user = userDao.findByUsername(request.getUsername().trim())
            .orElseThrow(() -> new BusinessException(401, 40102, "用户名或密码错误"));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new BusinessException(403, 40301, "账号已被禁用");
        }

        boolean matched = PasswordUtil.matches(request.getPassword(), user.getPasswordHash());
        if (!matched) {
            throw new BusinessException(401, 40103, "用户名或密码错误");
        }

        if (!user.getPasswordHash().startsWith("$2")) {
            TransactionManager.runInTransaction(connection -> {
                userDao.updatePasswordHash(connection, user.getId(), PasswordUtil.hash(request.getPassword()));
                return null;
            });
        }

        String token = JwtUtil.createToken(user.getId(), user.getUsername(), user.getRole());
        return buildAuthPayload(user.getId(), user.getUsername(), user.getRole(), user.getFullName(), token, user.isRealNameVerified());
    }

    public Map<String, Object> getProfileSnapshot(Long userId) {
        User user = userDao.findById(userId)
            .orElseThrow(() -> new BusinessException(404, 40401, "用户不存在"));
        Map<String, Object> map = new HashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("role", user.getRole());
        map.put("fullName", user.getFullName());
        map.put("phone", user.getPhone());
        map.put("email", user.getEmail());
        map.put("realNameVerified", user.isRealNameVerified());
        if (user.getIdCardEnc() != null && !user.getIdCardEnc().isBlank()) {
            String raw = SensitiveCryptoUtil.decrypt(user.getIdCardEnc());
            if (raw.length() > 6) {
                map.put("idCardMasked", raw.substring(0, 2) + "************" + raw.substring(raw.length() - 4));
            } else {
                map.put("idCardMasked", "******");
            }
        }
        return map;
    }

    private Map<String, Object> buildAuthPayload(Long userId, String username, String role, String fullName, String token, boolean verified) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("token", token);
        Map<String, Object> user = new HashMap<>();
        user.put("id", userId);
        user.put("username", username);
        user.put("role", role);
        user.put("fullName", fullName);
        user.put("realNameVerified", verified);
        payload.put("user", user);
        return payload;
    }

    private void validateRegisterRequest(RegisterRequest request) {
        String username = request.getUsername() == null ? "" : request.getUsername().trim();
        String password = request.getPassword() == null ? "" : request.getPassword();
        String role = request.getRole() == null ? "" : request.getRole().trim();
        String fullName = request.getFullName() == null ? "" : request.getFullName().trim();

        if (username.length() < 4 || username.length() > 24) {
            throw new BusinessException(400, 40012, "用户名长度需在4-24位之间");
        }
        if (password.length() < 6 || password.length() > 32) {
            throw new BusinessException(400, 40013, "密码长度需在6-32位之间");
        }
        if (!ALLOWED_ROLES.contains(role)) {
            throw new BusinessException(400, 40014, "角色非法");
        }
        if (fullName.length() < 2 || fullName.length() > 32) {
            throw new BusinessException(400, 40015, "姓名长度需在2-32位之间");
        }

        if (request.getPhone() != null && !request.getPhone().isBlank() && !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            throw new BusinessException(400, 40016, "手机号格式不正确");
        }

        if (RoleConstants.RESIDENT.equals(role)) {
            String idCard = request.getIdCardNo() == null ? "" : request.getIdCardNo().trim();
            if (!IDCARD_PATTERN.matcher(idCard).matches()) {
                throw new BusinessException(400, 40017, "居民注册需填写有效实名认证身份证号");
            }
        }
    }
}
