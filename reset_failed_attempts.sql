-- 重置用户失败登录次数
UPDATE users 
SET failed_login_attempts = 0, 
    account_locked_until = NULL 
WHERE user_id = 'huangjingpei@gmail.com';

-- 查看更新结果
SELECT user_id, failed_login_attempts, account_locked_until, status 
FROM users 
WHERE user_id = 'huangjingpei@gmail.com'; 