-- 插入测试数据
INSERT INTO community (id, name, address, created_at) VALUES 
(1, '前进路社区', '某某市某某区前进路123号', NOW()),
(2, '幸福社区', '某某市某某区幸福路456号', NOW());

INSERT INTO grid_group (id, community_id, name, description, created_at) VALUES 
(1, 1, '前进小区A栋', '前进路社区A栋网格群', NOW()),
(2, 1, '前进小区B栋', '前进路社区B栋网格群', NOW()),
(3, 2, '幸福小区1号楼', '幸福社区1号楼网格群', NOW());

INSERT INTO elder (id, name, age, gender, grid_group_id, status, created_at) VALUES 
(1, '张大爷', 85, '男', 1, 'active', NOW()),
(2, '李奶奶', 82, '女', 1, 'active', NOW()),
(3, '王大爷', 88, '男', 2, 'active', NOW()),
(4, '赵奶奶', 80, '女', 2, 'active', NOW()),
(5, '刘大爷', 83, '男', 3, 'active', NOW());

INSERT INTO account (id, elder_id, balance, created_at, updated_at) VALUES 
(1, 1, 1000.00, NOW(), NOW()),
(2, 2, 800.00, NOW(), NOW()),
(3, 3, 1200.00, NOW(), NOW()),
(4, 4, 600.00, NOW(), NOW()),
(5, 5, 900.00, NOW(), NOW());

INSERT INTO child (id, name, phone, created_at) VALUES 
(1, '张小明', '13800138001', NOW()),
(2, '李小华', '13800138002', NOW()),
(3, '王小刚', '13800138003', NOW()),
(4, '赵小美', '13800138004', NOW()),
(5, '刘小强', '13800138005', NOW());

INSERT INTO elder_child (id, elder_id, child_id, created_at) VALUES 
(1, 1, 1, NOW()),
(2, 2, 2, NOW()),
(3, 3, 3, NOW()),
(4, 4, 4, NOW()),
(5, 5, 5, NOW());

-- 插入一些代购请求示例
INSERT INTO purchase_request (id, elder_id, content, estimated_amount, status, created_at) VALUES 
(1, 1, '购买蔬菜和水果', 50.00, 'pending', NOW()),
(2, 2, '购买大米和食用油', 80.00, 'confirmed', NOW()),
(3, 3, '购买日常用品', 30.00, 'pending', NOW());

-- 插入一些紧急事件示例
INSERT INTO emergency_event (id, elder_id, event_type, status, created_at) VALUES 
(1, 1, '紧急求助', 'handled', NOW()),
(2, 3, '身体不适', 'pending', NOW());

-- 插入一些刷脸记录示例
INSERT INTO face_check_record (id, elder_id, check_time, result) VALUES 
(1, 1, NOW(), 'success'),
(2, 2, NOW(), 'success'),
(3, 4, NOW(), 'success');

-- 插入一些账单示例
INSERT INTO bill (id, elder_id, amount, bill_type, description, created_at) VALUES 
(1, 1, 45.50, 'purchase', '代购蔬菜水果', NOW()),
(2, 2, 75.00, 'purchase', '代购大米食用油', NOW()),
(3, 1, 100.00, 'recharge', '账户充值', NOW());

-- 插入一些服务评价示例
INSERT INTO service_review (id, elder_id, child_id, score, comment, review_month, created_at) VALUES 
(1, 1, 1, 5, '服务很好，很满意', '2024-12', NOW()),
(2, 2, 2, 4, '服务不错，继续努力', '2024-12', NOW());
