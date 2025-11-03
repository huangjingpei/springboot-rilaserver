const axios = require('axios');

// 配置
const BASE_URL = 'http://114.66.39.8:8080';
const API_BASE = `${BASE_URL}/api/v1/updates`;

// 正确的参数
const correctParams = {
    appId: 'RilaLive',           // ✅ 应用标识符
    version: '1.9.7',            // ✅ 版本号
    platform: 'Windows'           // ✅ 平台
};

// 错误的参数（你之前使用的）
const wrongParams = {
    appId: 'RilaLive64.exe',     // ❌ 这是文件名，不是应用ID
    version: '1.9.7',
    platform: 'Windows'
};

async function testCorrectDownload() {
    try {
        console.log('🧪 测试正确的下载地址...');
        
        // 1. 先检查更新
        console.log('\n📋 步骤1: 检查更新');
        const updateResponse = await axios.get(`${API_BASE}/latest`, {
            params: {
                appId: correctParams.appId,
                currentVersion: '1.9.6',
                platform: correctParams.platform
            }
        });
        
        console.log('✅ 更新检查结果:');
        console.log(JSON.stringify(updateResponse.data, null, 2));
        
        if (updateResponse.data.hasUpdate) {
            // 2. 构建正确的下载地址
            console.log('\n📥 步骤2: 构建下载地址');
            const correctDownloadUrl = `${API_BASE}/download/${correctParams.version}?appId=${correctParams.appId}&platform=${correctParams.platform}`;
            console.log('✅ 正确的下载地址:', correctDownloadUrl);
            
            // 3. 测试下载
            console.log('\n📥 步骤3: 测试下载');
            try {
                const downloadResponse = await axios.get(correctDownloadUrl, {
                    responseType: 'stream',
                    timeout: 30000
                });
                
                console.log('✅ 下载成功!');
                console.log('📊 响应信息:', {
                    status: downloadResponse.status,
                    contentLength: downloadResponse.headers['content-length'],
                    contentType: downloadResponse.headers['content-type']
                });
                
            } catch (error) {
                console.error('❌ 下载失败:', error.message);
                if (error.response) {
                    console.error('📊 错误详情:', {
                        status: error.response.status,
                        data: error.response.data
                    });
                }
            }
        }
        
    } catch (error) {
        console.error('❌ 测试失败:', error.message);
    }
}

async function testWrongDownload() {
    try {
        console.log('\n🧪 测试错误的下载地址（你之前使用的）...');
        
        const wrongDownloadUrl = `${API_BASE}/download/${wrongParams.version}?appId=${wrongParams.appId}&platform=${wrongParams.platform}`;
        console.log('❌ 错误的下载地址:', wrongDownloadUrl);
        
        try {
            const response = await axios.get(wrongDownloadUrl, {
                responseType: 'stream',
                timeout: 30000
            });
            
            console.log('✅ 意外成功!');
            
        } catch (error) {
            console.log('❌ 如预期失败:', error.message);
            if (error.response) {
                console.log('📊 错误详情:', error.response.data);
            }
        }
        
    } catch (error) {
        console.error('❌ 测试失败:', error.message);
    }
}

// 主函数
async function main() {
    console.log('🚀 开始下载地址测试');
    console.log('=' * 50);
    
    await testCorrectDownload();
    await testWrongDownload();
    
    console.log('\n📋 总结:');
    console.log('✅ 正确的参数: appId=RilaLive (应用标识符)');
    console.log('❌ 错误的参数: appId=RilaLive64.exe (文件名)');
    console.log('✅ 正确的URL: http://114.66.39.8:8080/api/v1/updates/download/1.9.7?appId=RilaLive&platform=Windows');
    
    console.log('\n🎉 测试完成！');
}

// 运行测试
if (require.main === module) {
    main();
}

module.exports = { testCorrectDownload, testWrongDownload }; 