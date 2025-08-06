-- 调整应用下载量，使其更合理
-- 将下载量设置为1000-50000之间的随机值

UPDATE apps 
SET download_count = FLOOR(1000 + RAND() * 49000)
WHERE is_active = true;

-- 为一些应用设置更高的下载量（热门应用）
UPDATE apps 
SET download_count = FLOOR(50000 + RAND() * 200000)
WHERE is_active = true AND id IN (1, 2, 3, 4, 5);

-- 为一些应用设置较低的下载量（新应用）
UPDATE apps 
SET download_count = FLOOR(100 + RAND() * 1000)
WHERE is_active = true AND id IN (6, 7, 8, 9, 10);

-- 显示调整后的结果
SELECT 
    id,
    name,
    download_count,
    CASE 
        WHEN download_count >= 50000 THEN '热门'
        WHEN download_count >= 10000 THEN '较热'
        WHEN download_count >= 1000 THEN '一般'
        ELSE '新应用'
    END as popularity
FROM apps 
WHERE is_active = true
ORDER BY download_count DESC; 