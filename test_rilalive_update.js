const axios = require('axios');

// 配置
const BASE_URL = 'http://127.0.0.1:8080';
const API_BASE = `${BASE_URL}/api/v1/updates`;

// 测试参数
const testParams = {
    appId: 'RilaLive',
    currentVersion: '1.7.9',
    platform: 'Windows'
};

async function testRilaLiveUpdate() {
    try {
        console.log('🧪 测试 RilaLive 更新检查...');
        console.log('📋 参数:', testParams);
        
        const response = await axios.get(`${API_BASE}/latest`, {
            params: testParams
        });
        
        console.log('✅ 响应结果:');
        console.log(JSON.stringify(response.data, null, 2));
        
        // 分析结果
        if (response.data.hasUpdate) {
            console.log('🎉 发现可用更新！');
            console.log(`📦 最新版本: ${response.data.latestVersion}`);
            console.log(`📝 更新说明: ${response.data.releaseNotes}`);
            console.log(`🔗 下载链接: ${response.data.downloadUrl}`);
        } else {
            console.log('ℹ️ 当前已是最新版本');
        }
        
        return response.data;
        
    } catch (error) {
        console.error('❌ 测试失败:', error.message);
        
        if (error.response) {
            console.error('📊 错误详情:', {
                status: error.response.status,
                data: error.response.data
            });
        }
        
        throw error;
    }
}

// 测试应用列表
async function testGetApps() {
    try {
        console.log('\n📱 获取应用列表...');
        
        const response = await axios.get(`${API_BASE}/apps`, {
            params: { page: 0, size: 10 }
        });
        
        console.log('✅ 应用列表:');
        console.log(JSON.stringify(response.data, null, 2));
        
        return response.data;
        
    } catch (error) {
        console.error('❌ 获取应用列表失败:', error.message);
        throw error;
    }
}

// 测试升级包列表
async function testGetUpdatePackages() {
    try {
        console.log('\n📦 获取升级包列表...');
        
        const response = await axios.get(`${API_BASE}`, {
            params: { page: 0, size: 10 }
        });
        
        console.log('✅ 升级包列表:');
        console.log(JSON.stringify(response.data, null, 2));
        
        return response.data;
        
    } catch (error) {
        console.error('❌ 获取升级包列表失败:', error.message);
        throw error;
    }
}

// 主测试函数
async function runTests() {
    console.log('🚀 开始 RilaLive 更新测试');
    console.log('=' * 50);
    
    try {
        // 1. 测试更新检查
        await testRilaLiveUpdate();
        
        // 2. 测试应用列表
        await testGetApps();
        
        // 3. 测试升级包列表
        await testGetUpdatePackages();
        
        console.log('\n🎉 所有测试完成！');
        
    } catch (error) {
        console.error('\n❌ 测试过程中出现错误:', error.message);
    }
}

// 运行测试
if (require.main === module) {
    runTests();
}

module.exports = { testRilaLiveUpdate, testGetApps, testGetUpdatePackages }; 