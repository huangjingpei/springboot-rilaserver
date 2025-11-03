const axios = require('axios');
const fs = require('fs');

// 配置
const BASE_URL = 'http://127.0.0.1:8080'; // 替换为你的服务器地址
const API_BASE = `${BASE_URL}/api/v1/updates`;

// 下载参数
const downloadParams = {
    appId: 'RilaLive',
    version: '1.9.7',
    platform: 'Windows'
};

async function testDownload() {
    try {
        console.log('🧪 测试文件下载...');
        console.log('📋 参数:', downloadParams);
        
        // 方法1: 使用 download 接口
        console.log('\n📥 方法1: 使用 download 接口');
        const downloadUrl = `${API_BASE}/download/${downloadParams.version}?appId=${downloadParams.appId}&platform=${downloadParams.platform}`;
        console.log('🔗 下载地址:', downloadUrl);
        
        try {
            const response = await axios.get(downloadUrl, {
                responseType: 'stream',
                timeout: 30000
            });
            
            console.log('✅ 下载成功!');
            console.log('📊 响应信息:', {
                status: response.status,
                headers: response.headers,
                contentLength: response.headers['content-length']
            });
            
            // 保存文件
            const fileName = `downloaded-${downloadParams.version}.exe`;
            const writer = fs.createWriteStream(fileName);
            response.data.pipe(writer);
            
            writer.on('finish', () => {
                console.log(`💾 文件已保存: ${fileName}`);
            });
            
        } catch (error) {
            console.error('❌ 下载失败:', error.message);
        }
        
        // 方法2: 使用 file 接口
        console.log('\n📥 方法2: 使用 file 接口');
        const fileUrl = `${API_BASE}/file/${downloadParams.version}?appId=${downloadParams.appId}&platform=${downloadParams.platform}`;
        console.log('🔗 文件地址:', fileUrl);
        
        try {
            const fileResponse = await axios.get(fileUrl, {
                responseType: 'stream',
                timeout: 30000
            });
            
            console.log('✅ 文件访问成功!');
            console.log('📊 响应信息:', {
                status: fileResponse.status,
                headers: fileResponse.headers,
                contentLength: fileResponse.headers['content-length']
            });
            
        } catch (error) {
            console.error('❌ 文件访问失败:', error.message);
        }
        
    } catch (error) {
        console.error('❌ 测试失败:', error.message);
    }
}

// 测试更新检查
async function testUpdateCheck() {
    try {
        console.log('\n🔍 测试更新检查...');
        
        const response = await axios.get(`${API_BASE}/latest`, {
            params: {
                appId: 'RilaLive',
                currentVersion: '1.9.6',
                platform: 'Windows'
            }
        });
        
        console.log('✅ 更新检查结果:');
        console.log(JSON.stringify(response.data, null, 2));
        
        if (response.data.hasUpdate) {
            console.log('\n📥 可用下载地址:');
            console.log(`完整URL: ${BASE_URL}${response.data.downloadUrl}`);
            console.log(`文件URL: ${BASE_URL}/api/v1/updates/file/${response.data.latestVersion}?appId=RilaLive&platform=Windows`);
        }
        
    } catch (error) {
        console.error('❌ 更新检查失败:', error.message);
    }
}

// 主函数
async function main() {
    console.log('🚀 开始下载测试');
    console.log('=' * 50);
    
    await testUpdateCheck();
    await testDownload();
    
    console.log('\n🎉 测试完成！');
}

// 运行测试
if (require.main === module) {
    main();
}

module.exports = { testDownload, testUpdateCheck }; 