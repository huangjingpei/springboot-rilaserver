const http = require('http');

// 测试配置
const config = {
    host: '192.168.3.4',
    port: 8080
};

// 测试函数
function testAPI(path) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: config.host,
            port: config.port,
            path: path,
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        };

        const req = http.request(options, (res) => {
            let responseData = '';
            
            res.on('data', (chunk) => {
                responseData += chunk;
            });
            
            res.on('end', () => {
                console.log(`\n=== GET ${path} ===`);
                console.log(`状态码: ${res.statusCode}`);
                
                if (responseData) {
                    try {
                        const jsonData = JSON.parse(responseData);
                        console.log('响应数据:', JSON.stringify(jsonData, null, 2));
                        resolve(jsonData);
                    } catch (e) {
                        console.log('响应数据:', responseData);
                        resolve(responseData);
                    }
                } else {
                    resolve(null);
                }
            });
        });

        req.on('error', (error) => {
            console.error(`请求错误: ${error.message}`);
            reject(error);
        });
        
        req.end();
    });
}

// 执行测试
async function runTests() {
    console.log('开始调试升级API...\n');
    
    try {
        // 测试1: 使用较低的当前版本号
        console.log('1. 测试当前版本1.0.0，平台1');
        await testAPI('/api/v1/updates/latest?currentVersion=1.0.0&platform=1');
        
        // 测试2: 使用不同的平台
        console.log('\n2. 测试当前版本1.0.0，平台windows-x64');
        await testAPI('/api/v1/updates/latest?currentVersion=1.0.0&platform=windows-x64');
        
        // 测试3: 使用相同的版本号
        console.log('\n3. 测试当前版本1.2.3，平台1');
        await testAPI('/api/v1/updates/latest?currentVersion=1.2.3&platform=1');
        
        // 测试4: 版本验证
        console.log('\n4. 验证版本号1.2.3格式');
        await testAPI('/api/v1/updates/validate-version?version=1.2.3');
        
        // 测试5: 强制更新查询
        console.log('\n5. 查询平台1的强制更新');
        await testAPI('/api/v1/updates/mandatory/1');
        
        // 测试6: 查询平台windows-x64的强制更新
        console.log('\n6. 查询平台windows-x64的强制更新');
        await testAPI('/api/v1/updates/mandatory/windows-x64');
        
        console.log('\n✅ 调试测试完成！');
        
    } catch (error) {
        console.error('\n❌ 测试失败:', error.message);
    }
}

// 运行测试
runTests(); 