-- ============================================
-- 换型矩阵测试数据SQL脚本
-- 用于手动测试换型矩阵功能
-- ============================================

-- 1. 清空现有测试数据（可选）
DELETE FROM md_setup_matrix WHERE remark LIKE '%测试%';

-- ============================================
-- 2. 冲压工艺 - 模具换型矩阵
-- ============================================
INSERT INTO md_setup_matrix (process_type, from_key, to_key, setup_minutes, setup_cost, status, remark, created_by, updated_by)
VALUES
(1, 'MOLD_A', 'MOLD_B', 30, 200.00, 1, '冲压模具A到B切换测试数据', 'test_user', 'test_user'),
(1, 'MOLD_A', 'MOLD_C', 45, 300.00, 1, '冲压模具A到C切换测试数据', 'test_user', 'test_user'),
(1, 'MOLD_B', 'MOLD_A', 35, 220.00, 1, '冲压模具B到A切换测试数据', 'test_user', 'test_user'),
(1, 'MOLD_B', 'MOLD_C', 25, 180.00, 1, '冲压模具B到C切换测试数据', 'test_user', 'test_user'),
(1, 'MOLD_C', 'MOLD_A', 40, 280.00, 1, '冲压模具C到A切换测试数据', 'test_user', 'test_user'),
(1, 'MOLD_C', 'MOLD_B', 28, 190.00, 1, '冲压模具C到B切换测试数据', 'test_user', 'test_user');

-- ============================================
-- 3. 焊装工艺 - 夹具换型矩阵
-- ============================================
INSERT INTO md_setup_matrix (process_type, from_key, to_key, setup_minutes, setup_cost, status, remark, created_by, updated_by)
VALUES
(2, 'FIX_X1', 'FIX_X2', 20, 150.00, 1, '焊装夹具X1到X2切换测试数据', 'test_user', 'test_user'),
(2, 'FIX_X1', 'FIX_Y1', 35, 250.00, 1, '焊装夹具X1到Y1切换测试数据', 'test_user', 'test_user'),
(2, 'FIX_X2', 'FIX_X1', 18, 140.00, 1, '焊装夹具X2到X1切换测试数据', 'test_user', 'test_user'),
(2, 'FIX_X2', 'FIX_Y1', 30, 220.00, 1, '焊装夹具X2到Y1切换测试数据', 'test_user', 'test_user'),
(2, 'FIX_Y1', 'FIX_X1', 32, 240.00, 1, '焊装夹具Y1到X1切换测试数据', 'test_user', 'test_user'),
(2, 'FIX_Y1', 'FIX_X2', 25, 200.00, 1, '焊装夹具Y1到X2切换测试数据', 'test_user', 'test_user');

-- ============================================
-- 4. 涂装工艺 - 颜色换型矩阵（重点测试）
-- ============================================
INSERT INTO md_setup_matrix (process_type, from_key, to_key, setup_minutes, setup_cost, status, remark, created_by, updated_by)
VALUES
-- 红色切换
(3, '红色', '白色', 15, 100.00, 1, '红色到白色需要清洗喷枪', 'test_user', 'test_user'),
(3, '红色', '黑色', 20, 150.00, 1, '红色到黑色需要更长清洗时间', 'test_user', 'test_user'),
(3, '红色', '蓝色', 18, 120.00, 1, '红色到蓝色切换', 'test_user', 'test_user'),
(3, '红色', '银色', 16, 110.00, 1, '红色到银色切换', 'test_user', 'test_user'),

-- 白色切换
(3, '白色', '红色', 10, 80.00, 1, '白色到红色较快', 'test_user', 'test_user'),
(3, '白色', '黑色', 12, 90.00, 1, '白色到黑色切换', 'test_user', 'test_user'),
(3, '白色', '蓝色', 15, 100.00, 1, '白色到蓝色切换', 'test_user', 'test_user'),
(3, '白色', '银色', 14, 95.00, 1, '白色到银色切换', 'test_user', 'test_user'),

-- 黑色切换
(3, '黑色', '红色', 25, 200.00, 1, '黑色到红色需要彻底清洗', 'test_user', 'test_user'),
(3, '黑色', '白色', 22, 180.00, 1, '黑色到白色需要彻底清洗', 'test_user', 'test_user'),
(3, '黑色', '蓝色', 20, 150.00, 1, '黑色到蓝色切换', 'test_user', 'test_user'),
(3, '黑色', '银色', 18, 130.00, 1, '黑色到银色切换', 'test_user', 'test_user'),

-- 蓝色切换
(3, '蓝色', '红色', 16, 110.00, 1, '蓝色到红色切换', 'test_user', 'test_user'),
(3, '蓝色', '白色', 14, 95.00, 1, '蓝色到白色切换', 'test_user', 'test_user'),
(3, '蓝色', '黑色', 18, 130.00, 1, '蓝色到黑色切换', 'test_user', 'test_user'),
(3, '蓝色', '银色', 12, 85.00, 1, '蓝色到银色切换', 'test_user', 'test_user'),

-- 银色切换
(3, '银色', '红色', 17, 115.00, 1, '银色到红色切换', 'test_user', 'test_user'),
(3, '银色', '白色', 10, 75.00, 1, '银色到白色较快', 'test_user', 'test_user'),
(3, '银色', '黑色', 19, 140.00, 1, '银色到黑色切换', 'test_user', 'test_user'),
(3, '银色', '蓝色', 13, 90.00, 1, '银色到蓝色切换', 'test_user', 'test_user');

-- ============================================
-- 5. 总装工艺 - 配置换型矩阵
-- ============================================
INSERT INTO md_setup_matrix (process_type, from_key, to_key, setup_minutes, setup_cost, status, remark, created_by, updated_by)
VALUES
(4, 'BASE', 'PREMIUM', 10, 50.00, 1, '基础配置到高级配置切换', 'test_user', 'test_user'),
(4, 'BASE', 'LUXURY', 15, 80.00, 1, '基础配置到豪华配置切换', 'test_user', 'test_user'),
(4, 'PREMIUM', 'BASE', 8, 40.00, 1, '高级配置到基础配置切换', 'test_user', 'test_user'),
(4, 'PREMIUM', 'LUXURY', 12, 60.00, 1, '高级配置到豪华配置切换', 'test_user', 'test_user'),
(4, 'LUXURY', 'BASE', 18, 100.00, 1, '豪华配置到基础配置切换', 'test_user', 'test_user'),
(4, 'LUXURY', 'PREMIUM', 10, 55.00, 1, '豪华配置到高级配置切换', 'test_user', 'test_user');

-- ============================================
-- 6. 验证数据
-- ============================================
-- 查询各工艺类型的换型矩阵数量
SELECT 
    process_type,
    CASE process_type
        WHEN 1 THEN '冲压'
        WHEN 2 THEN '焊装'
        WHEN 3 THEN '涂装'
        WHEN 4 THEN '总装'
    END AS process_name,
    COUNT(*) AS matrix_count
FROM md_setup_matrix
WHERE deleted = 0 AND status = 1
GROUP BY process_type
ORDER BY process_type;

-- 查询涂装工艺的完整换型矩阵
SELECT 
    from_key AS '源颜色',
    to_key AS '目标颜色',
    setup_minutes AS '换型时间(分钟)',
    setup_cost AS '换型成本',
    remark AS '备注'
FROM md_setup_matrix
WHERE process_type = 3 AND deleted = 0 AND status = 1
ORDER BY from_key, to_key;

