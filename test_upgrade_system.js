const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');

// 配置
const BASE_URL = 'http://localhost:8080';
const API_BASE = `${BASE_URL}/api/v1/updates`;

// 测试数据
const TEST_APPS = [
    {
        appId: 'test-app-1',
        currentVersion: '1.0.0',
        platform: 'WINDOWS'
    },
    {
        appId: 'test-app-2',
        currentVersion: '2.0.0',
        platform: 'WINDOWS'
    },
    {
        appId: 'mobile-app-1',
        currentVersion: '1.5.0',
        platform: 'ANDROID'
    }
];

// 颜色输出
const colors = {
    green: '\x1b[32m',
    red: '\x1b[31m',
    yellow: '\x1b[33m',
    blue: '\x1b[34m',
    reset: '\x1b[0m'
};

function log(message, color = 'reset') {
    console.log(`${colors[color]}${message}${colors.reset}`);
}

// 测试工具函数
async function testAPI(name, testFunction) {
    try {
        log(`\n🧪 测试: ${name}`, 'blue');
        const result = await testFunction();
        log(`✅ ${name} - 成功`, 'green');
        return { success: true, result };
    } catch (error) {
        log(`❌ ${name} - 失败: ${error.message}`, 'red');
        return { success: false, error: error.message };
    }
}

// 1. 检查应用更新
async function testCheckUpdate() {
    const app = TEST_APPS[0];
    const response = await axios.get(`${API_BASE}/latest`, {
        params: {
            appId: app.appId,
            currentVersion: app.currentVersion,
            platform: app.platform
        }
    });
    
    console.log('📋 更新检查结果:', JSON.stringify(response.data, null, 2));
    return response.data;
}

// 2. 获取应用列表
async function testGetApps() {
    const response = await axios.get(`${API_BASE}/apps`, {
        params: { page: 0, size: 10 }
    });
    
    console.log('📱 应用列表:', JSON.stringify(response.data, null, 2));
    return response.data;
}

// 3. 获取激活的应用
async function testGetActiveApps() {
    const response = await axios.get(`${API_BASE}/apps/active`, {
        params: { page: 0, size: 10 }
    });
    
    console.log('✅ 激活应用列表:', JSON.stringify(response.data, null, 2));
    return response.data;
}

// 4. 获取强制更新
async function testGetMandatoryUpdates() {
    const app = TEST_APPS[1]; // test-app-2 有强制更新
    const response = await axios.get(`${API_BASE}/mandatory/${app.appId}/${app.platform}`);
    
    console.log('⚠️ 强制更新列表:', JSON.stringify(response.data, null, 2));
    return response.data;
}

// 5. 获取升级包列表
async function testGetUpdatePackages() {
    const response = await axios.get(`${API_BASE}`, {
        params: { page: 0, size: 10 }
    });
    
    console.log('📦 升级包列表:', JSON.stringify(response.data, null, 2));
    return response.data;
}

// 6. 测试版本比较
async function testVersionComparison() {
    const response = await axios.get(`${API_BASE}/compare-versions`, {
        params: {
            version1: '1.0.0',
            version2: '1.2.0'
        }
    });
    
    console.log('🔍 版本比较结果:', JSON.stringify(response.data, null, 2));
    return response.data;
}

// 7. 测试版本验证
async function testVersionValidation() {
    const validVersion = await axios.get(`${API_BASE}/validate-version`, {
        params: { version: '1.2.3' }
    });
    
    const invalidVersion = await axios.get(`${API_BASE}/validate-version`, {
        params: { version: 'invalid' }
    });
    
    console.log('✅ 有效版本验证:', JSON.stringify(validVersion.data, null, 2));
    console.log('❌ 无效版本验证:', JSON.stringify(invalidVersion.data, null, 2));
    
    return { valid: validVersion.data, invalid: invalidVersion.data };
}

// 8. 测试应用搜索
async function testAppSearch() {
    const response = await axios.get(`${API_BASE}/apps/search`, {
        params: {
            keyword: '测试',
            page: 0,
            size: 10
        }
    });
    
    console.log('🔍 应用搜索结果:', JSON.stringify(response.data, null, 2));
    return response.data;
}

// 9. 测试获取应用分类
async function testGetCategories() {
    const response = await axios.get(`${API_BASE}/apps/categories`);
    
    console.log('📂 应用分类:', JSON.stringify(response.data, null, 2));
    return response.data;
}

// 10. 测试获取应用标签
async function testGetTags() {
    const response = await axios.get(`${API_BASE}/apps/tags`);
    
    console.log('🏷️ 应用标签:', JSON.stringify(response.data, null, 2));
    return response.data;
}

// 11. 测试获取平台列表
async function testGetPlatforms() {
    const response = await axios.get(`${API_BASE}/platforms`);
    
    console.log('🖥️ 支持平台:', JSON.stringify(response.data, null, 2));
    return response.data;
}

// 12. 测试下载链接生成
async function testDownloadUrl() {
    const app = TEST_APPS[0];
    const version = '1.2.0';
    
    try {
        const response = await axios.get(`${API_BASE}/download/${version}`, {
            params: {
                appId: app.appId,
                platform: app.platform
            },
            maxRedirects: 0,
            validateStatus: function (status) {
                return status >= 200 && status < 400;
            }
        });
        
        console.log('📥 下载链接测试:', response.status);
        return { status: response.status };
    } catch (error) {
        console.log('📥 下载链接测试:', error.response?.status || 'ERROR');
        return { status: error.response?.status || 'ERROR' };
    }
}

// 主测试函数
async function runAllTests() {
    log('🚀 开始升级系统全面测试', 'green');
    log('=' * 50, 'blue');
    
    const tests = [
        { name: '检查应用更新', fn: testCheckUpdate },
        { name: '获取应用列表', fn: testGetApps },
        { name: '获取激活应用', fn: testGetActiveApps },
        { name: '获取强制更新', fn: testGetMandatoryUpdates },
        { name: '获取升级包列表', fn: testGetUpdatePackages },
        { name: '版本比较', fn: testVersionComparison },
        { name: '版本验证', fn: testVersionValidation },
        { name: '应用搜索', fn: testAppSearch },
        { name: '获取应用分类', fn: testGetCategories },
        { name: '获取应用标签', fn: testGetTags },
        { name: '获取平台列表', fn: testGetPlatforms },
        { name: '下载链接测试', fn: testDownloadUrl }
    ];
    
    const results = [];
    
    for (const test of tests) {
        const result = await testAPI(test.name, test.fn);
        results.push({ name: test.name, ...result });
    }
    
    // 测试结果统计
    const successCount = results.filter(r => r.success).length;
    const totalCount = results.length;
    
    log('\n📊 测试结果统计', 'yellow');
    log(`✅ 成功: ${successCount}/${totalCount}`, 'green');
    log(`❌ 失败: ${totalCount - successCount}/${totalCount}`, 'red');
    
    if (successCount === totalCount) {
        log('🎉 所有测试通过！升级系统运行正常', 'green');
    } else {
        log('⚠️ 部分测试失败，请检查系统配置', 'yellow');
    }
    
    return results;
}

// 运行测试
if (require.main === module) {
    runAllTests().catch(error => {
        log(`❌ 测试运行失败: ${error.message}`, 'red');
        process.exit(1);
    });
}

module.exports = { runAllTests }; 