const http = require('http');
const fs = require('fs');
const path = require('path');

// 测试配置
const config = {
    host: '192.168.3.4',
    port: 8080,
    token: 'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJodWFuZ2ppbmdwZWlAZ21haWwuY29tfHJlZ2lzdGVyIiwiaWF0IjoxNzUzNjY3Nzg4LCJleHAiOjE3NTM3NTQxODh9.TnF4Met5U7GrRs_htBSRwkzCPGMe2ECU50xcbFygGlc8iLJKBXuawi-mgTAS65YZ'
};

// 检查文件是否存在
function checkFileExists(filePath) {
    const fullPath = path.resolve(filePath);
    const exists = fs.existsSync(fullPath);
    const stats = exists ? fs.statSync(fullPath) : null;
    
    console.log(`文件: ${filePath}`);
    console.log(`完整路径: ${fullPath}`);
    console.log(`存在: ${exists}`);
    if (exists) {
        console.log(`大小: ${(stats.size / 1024 / 1024).toFixed(2)} MB`);
        console.log(`类型: ${stats.isFile() ? '文件' : '目录'}`);
    }
    console.log('---');
    
    return exists ? stats.size : 0;
}

// 测试上传配置
async function testUploadConfig() {
    console.log('检查上传配置...\n');
    
    // 检查可能的文件路径
    const possibleFiles = [
        'RilaAssit.exe',
        'Rila-LiveAssistant.exe',
        'RilaAssistant.exe'
    ];
    
    let fileSize = 0;
    let filePath = '';
    
    for (const file of possibleFiles) {
        const size = checkFileExists(file);
        if (size > 0) {
            fileSize = size;
            filePath = file;
            break;
        }
    }
    
    if (fileSize === 0) {
        console.log('❌ 未找到可上传的文件');
        return;
    }
    
    console.log(`✅ 找到文件: ${filePath} (${(fileSize / 1024 / 1024).toFixed(2)} MB)`);
    
    // 检查文件大小是否超过限制
    const maxSize = 500 * 1024 * 1024; // 500MB
    if (fileSize > maxSize) {
        console.log(`❌ 文件大小超过限制: ${(fileSize / 1024 / 1024).toFixed(2)} MB > 500 MB`);
        return;
    }
    
    console.log(`✅ 文件大小在限制范围内`);
    
    // 测试API配置
    console.log('\n测试API配置...');
    
    const options = {
        hostname: config.host,
        port: config.port,
        path: '/api/v1/updates',
        method: 'POST',
        headers: {
            'Authorization': `Bearer ${config.token}`,
            'Content-Type': 'multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW'
        }
    };
    
    const req = http.request(options, (res) => {
        console.log(`状态码: ${res.statusCode}`);
        console.log(`响应头:`, res.headers);
        
        let data = '';
        res.on('data', (chunk) => {
            data += chunk;
        });
        
        res.on('end', () => {
            if (data) {
                try {
                    const jsonData = JSON.parse(data);
                    console.log('响应数据:', JSON.stringify(jsonData, null, 2));
                } catch (e) {
                    console.log('响应数据:', data);
                }
            }
        });
    });
    
    req.on('error', (error) => {
        console.error('请求错误:', error.message);
    });
    
    req.end();
}

// 运行测试
testUploadConfig(); 