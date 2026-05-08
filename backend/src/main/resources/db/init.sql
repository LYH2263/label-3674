SET NAMES utf8mb4;
SET character_set_client = utf8mb4;
SET character_set_connection = utf8mb4;
SET character_set_results = utf8mb4;

CREATE TABLE IF NOT EXISTS users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role VARCHAR(32) NOT NULL,
  full_name VARCHAR(64) NOT NULL,
  phone VARCHAR(32),
  email VARCHAR(128),
  real_name_verified TINYINT(1) NOT NULL DEFAULT 0,
  id_card_enc VARCHAR(512),
  status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS repair_orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(120) NOT NULL,
  description TEXT NOT NULL,
  image_url VARCHAR(255),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  assigned_provider_id BIGINT,
  rating INT,
  rating_comment VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_repair_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS activities (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(120) NOT NULL,
  description TEXT NOT NULL,
  location VARCHAR(120) NOT NULL,
  start_time DATETIME NOT NULL,
  organizer_id BIGINT NOT NULL,
  organizer_role VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_activity_user FOREIGN KEY (organizer_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS activity_signups (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  sign_status VARCHAR(32) NOT NULL DEFAULT 'REGISTERED',
  signed_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_activity_user (activity_id, user_id),
  CONSTRAINT fk_signup_activity FOREIGN KEY (activity_id) REFERENCES activities(id),
  CONSTRAINT fk_signup_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS activity_reviews (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  activity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  rating INT NOT NULL,
  content VARCHAR(255) NOT NULL,
  photo_url VARCHAR(255),
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_activity_review_activity FOREIGN KEY (activity_id) REFERENCES activities(id),
  CONSTRAINT fk_activity_review_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS messages (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  receiver_id BIGINT NOT NULL,
  sender_id BIGINT,
  message_type VARCHAR(32) NOT NULL,
  title VARCHAR(120) NOT NULL,
  content TEXT NOT NULL,
  is_read TINYINT(1) NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_message_receiver FOREIGN KEY (receiver_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS notices (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(120) NOT NULL,
  content TEXT NOT NULL,
  is_emergency TINYINT(1) NOT NULL DEFAULT 0,
  created_by BIGINT NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_notice_creator FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS property_payments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  amount DECIMAL(10,2) NOT NULL,
  pay_status VARCHAR(32) NOT NULL,
  paid_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pay_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS property_payment_transactions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  payment_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  pay_no VARCHAR(64) NOT NULL UNIQUE,
  amount DECIMAL(10,2) NOT NULL,
  channel VARCHAR(32) NOT NULL,
  status VARCHAR(32) NOT NULL,
  paid_at DATETIME,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_pay_tx_payment FOREIGN KEY (payment_id) REFERENCES property_payments(id),
  CONSTRAINT fk_pay_tx_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS neighborhood_posts (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  category VARCHAR(32) NOT NULL,
  title VARCHAR(120) NOT NULL,
  content TEXT NOT NULL,
  price DECIMAL(10,2),
  contact VARCHAR(80),
  status VARCHAR(16) NOT NULL DEFAULT 'OPEN',
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  CONSTRAINT fk_neighborhood_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS parking_slots (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  slot_no VARCHAR(40) NOT NULL UNIQUE,
  status VARCHAR(32) NOT NULL,
  owner_user_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_slot_owner FOREIGN KEY (owner_user_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS visitor_records (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  resident_id BIGINT NOT NULL,
  visitor_name VARCHAR(80) NOT NULL,
  visit_time DATETIME NOT NULL,
  status VARCHAR(32) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT fk_visitor_resident FOREIGN KEY (resident_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS operation_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT,
  username VARCHAR(64),
  action VARCHAR(120),
  path VARCHAR(200),
  method VARCHAR(10),
  status_code INT,
  duration_ms BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users (id, username, password_hash, role, full_name, phone, email, real_name_verified, id_card_enc, status)
VALUES
  (1, 'resident_demo', '123456', 'RESIDENT', '张晨', '13800001111', 'resident@community.local', 1, NULL, 'ACTIVE'),
  (2, 'property_admin', '123456', 'PROPERTY_ADMIN', '李敏', '13800002222', 'property@community.local', 1, NULL, 'ACTIVE'),
  (3, 'service_pro', '123456', 'SERVICE_PROVIDER', '王涛', '13800003333', 'service@community.local', 1, NULL, 'ACTIVE')
ON DUPLICATE KEY UPDATE username = VALUES(username);

INSERT INTO repair_orders (id, user_id, title, description, status, assigned_provider_id)
VALUES
  (1, 1, '楼道照明故障', '2号楼3单元楼道灯连续两晚不亮，请尽快处理。', 'IN_PROGRESS', 3),
  (2, 1, '门禁识别异常', '地库门禁识别车牌速度较慢，经常失败。', 'PENDING', NULL)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO activities (id, title, description, location, start_time, organizer_id, organizer_role)
VALUES
  (1, '周末亲子阅读会', '社区图书角举办亲子阅读会，欢迎携带绘本参与。', '社区活动室A', DATE_ADD(NOW(), INTERVAL 2 DAY), 2, 'PROPERTY_ADMIN'),
  (2, '旧物互换日', '邻里互助旧物交换，倡导绿色生活。', '中央广场', DATE_ADD(NOW(), INTERVAL 5 DAY), 1, 'RESIDENT')
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO activity_signups (id, activity_id, user_id, sign_status)
VALUES
  (1, 1, 1, 'REGISTERED')
ON DUPLICATE KEY UPDATE sign_status = VALUES(sign_status);

INSERT INTO messages (id, receiver_id, sender_id, message_type, title, content, is_read)
VALUES
  (1, 1, 2, 'SYSTEM', '物业提醒', '您所在楼栋本周三将进行电梯例检，请留意公告。', 0),
  (2, 1, 3, 'PRIVATE', '报修进展', '您的报修单 #1 已由服务商接单，预计今晚处理。', 0),
  (3, 2, 1, 'PRIVATE', '活动咨询', '请问周末阅读会是否需要提前签到？', 1)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO notices (id, title, content, is_emergency, created_by)
VALUES
  (1, '春季消防演练通知', '本周五下午进行消防应急演练，请居民配合指引。', 0, 2),
  (2, '临时停水通知', '今晚22:00-24:00进行主管道维护，期间将暂停供水。', 1, 2)
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO property_payments (id, user_id, amount, pay_status, paid_at)
VALUES
  (1, 1, 320.00, 'PAID', NOW()),
  (2, 1, 320.00, 'UNPAID', NULL)
ON DUPLICATE KEY UPDATE amount = VALUES(amount);

INSERT INTO property_payment_transactions (id, payment_id, user_id, pay_no, amount, channel, status, paid_at)
VALUES
  (1, 1, 1, 'TXN-DEMO-0001', 320.00, 'ALIPAY', 'SUCCESS', NOW())
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO parking_slots (id, slot_no, status, owner_user_id)
VALUES
  (1, 'A-101', 'OCCUPIED', 1),
  (2, 'A-102', 'FREE', NULL)
ON DUPLICATE KEY UPDATE status = VALUES(status);

INSERT INTO visitor_records (id, resident_id, visitor_name, visit_time, status)
VALUES
  (1, 1, '赵磊', DATE_ADD(NOW(), INTERVAL 1 DAY), 'APPROVED')
ON DUPLICATE KEY UPDATE visitor_name = VALUES(visitor_name);

INSERT INTO neighborhood_posts (id, user_id, category, title, content, price, contact, status)
VALUES
  (1, 1, 'NEIGHBOR_CIRCLE', '周末遛狗互助', '本周六下午一起遛狗，欢迎带宠物交流。', NULL, 'resident_demo', 'OPEN'),
  (2, 1, 'SECOND_HAND', '转让九成新婴儿推车', '仅使用三个月，支持当面验货。', 280.00, 'resident_demo', 'OPEN'),
  (3, 3, 'SKILL_SWAP', '可提供家电上门检测', '擅长小家电故障排查，可技能互换。', NULL, 'service_pro', 'OPEN'),
  (4, 2, 'LOST_FOUND', '失物招领：黑色钥匙包', '在2号楼大厅拾到，请联系物业前台认领。', NULL, 'property_admin', 'OPEN')
ON DUPLICATE KEY UPDATE title = VALUES(title);

INSERT INTO activity_reviews (id, activity_id, user_id, rating, content, photo_url)
VALUES
  (1, 1, 1, 5, '活动组织很有秩序，孩子很喜欢。', NULL)
ON DUPLICATE KEY UPDATE rating = VALUES(rating);
