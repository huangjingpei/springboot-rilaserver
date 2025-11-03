const axios = require('axios');
const FormData = require('form-data');
const fs = require('fs');
const path = require('path');

// 配置
const BASE_URL = 'http://localhost:8080';
const API_ENDPOINT = `${BASE_URL}/api/v1/updates`;

// 上传配置
const uploadConfig = {
    appId: 'test-app-1',
    version: '1.3.0',
    platform: 'WINDOWS',
    releaseNotes: '修复了一些bug，新增功能C',
    isMandatory: false,
    description: '版本1.3.0更新',
    filePath: './test_files/sample-update.exe'
};

async function uploadUpdateFile() {
    try {
        console.log('🚀 开始上传升级包文件...');
        
        // 检查文件是否存在
        if (!fs.existsSync(uploadConfig.filePath)) {
            throw new Error(`文件不存在: ${uploadConfig.filePath}`);
        }
        
        // 创建 FormData
        const formData = new FormData();
        
        // 添加文件
        formData.append('file', fs.createReadStream(uploadConfig.filePath));
        
        // 添加其他参数
        formData.append('appId', uploadConfig.appId);
        formData.append('version', uploadConfig.version);
        formData.append('platform', uploadConfig.platform);
        formData.append('releaseNotes', uploadConfig.releaseNotes);
        formData.append('isMandatory', uploadConfig.isMandatory);
        formData.append('description', uploadConfig.description);
        
        // 发送请求
        const response = await axios.post(API_ENDPOINT, formData, {
            headers: {
                ...formData.getHeaders(),
                'Authorization': 'Bearer YOUR_JWT_TOKEN' // 如果需要认证
            },
            maxContentLength: Infinity,
            maxBodyLength: Infinity,
            timeout: 30000 // 30秒超时
        });
        
        console.log('✅ 上传成功！');
        console.log('📋 响应数据:', JSON.stringify(response.data, null, 2));
        
        return response.data;
        
    } catch (error) {
        console.error('❌ 上传失败:', error.message);
        
        if (error.response) {
            console.error('📊 错误详情:', {
                status: error.response.status,
                data: error.response.data
            });
        }
        
        throw error;
    }
}

// 批量上传示例
async function batchUpload() {
    const uploads = [
        {
            appId: 'test-app-1',
            version: '1.3.0',
            platform: 'WINDOWS',
            filePath: './test_files/sample-update.exe',
            releaseNotes: 'Windows版本1.3.0更新'
        },
        {
            appId: 'mobile-app-1',
            version: '1.7.0',
            platform: 'ANDROID',
            filePath: './test_files/sample-update.apk',
            releaseNotes: 'Android版本1.7.0更新'
        }
    ];
    
    console.log('📦 开始批量上传...');
    
    for (const upload of uploads) {
        try {
            console.log(`\n📤 上传: ${upload.appId} v${upload.version} (${upload.platform})`);
            await uploadUpdateFile(upload);
        } catch (error) {
            console.error(`❌ 上传失败: ${upload.appId}`, error.message);
        }
    }
    
    console.log('\n🎉 批量上传完成！');
}

// 验证上传结果
async function verifyUpload() {
    try {
        console.log('🔍 验证上传结果...');
        
        // 检查升级包列表
        const response = await axios.get(`${BASE_URL}/api/v1/updates`, {
            params: { page: 0, size: 10 }
        });
        
        console.log('📦 升级包列表:', JSON.stringify(response.data, null, 2));
        
        // 检查特定应用的更新
        const updateCheck = await axios.get(`${BASE_URL}/api/v1/updates/latest`, {
            params: {
                appId: 'test-app-1',
                currentVersion: '1.2.0',
                platform: 'WINDOWS'
            }
        });
        
        console.log('📋 更新检查结果:', JSON.stringify(updateCheck.data, null, 2));
        
    } catch (error) {
        console.error('❌ 验证失败:', error.message);
    }
}

// 主函数
async function main() {
    const command = process.argv[2] || 'upload';
    
    switch (command) {
        case 'upload':
            await uploadUpdateFile();
            break;
        case 'batch':
            await batchUpload();
            break;
        case 'verify':
            await verifyUpload();
            break;
        default:
            console.log('使用方法:');
            console.log('  node upload_example.js upload  - 上传单个文件');
            console.log('  node upload_example.js batch   - 批量上传');
            console.log('  node upload_example.js verify  - 验证上传结果');
    }
}

// 运行示例
if (require.main === module) {
    main().catch(console.error);
}

module.exports = { uploadUpdateFile, batchUpload, verifyUpload }; 