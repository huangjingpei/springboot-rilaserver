const http = require('http');

// 测试配置
const config = {
    host: '192.168.3.4',
    port: 8080,
    token: 'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJodWFuZ2ppbmdwZWlAZ21haWwuY29tfHJlZ2lzdGVyIiwiaWF0IjoxNzUzNjY3Nzg4LCJleHAiOjE3NTM3NTQxODh9.TnF4Met5U7GrRs_htBSRwkzCPGMe2ECU50xcbFygGlc8iLJKBXuawi-mgTAS65YZ'
};

// 测试获取推流地址
function testGetPushUrl() {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: config.host,
            port: config.port,
            path: '/zlm/getPushUrl',
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${config.token}`,
                'Accept': 'application/json'
            }
        };

        const req = http.request(options, (res) => {
            let responseData = '';
            
            res.on('data', (chunk) => {
                responseData += chunk;
            });
            
            res.on('end', () => {
                console.log(`\n=== 测试获取推流地址 ===`);
                console.log(`状态码: ${res.statusCode}`);
                
                if (responseData) {
                    try {
                        const jsonData = JSON.parse(responseData);
                        console.log('响应数据:', JSON.stringify(jsonData, null, 2));
                        
                        if (jsonData.success && jsonData.data) {
                            console.log('✅ 成功获取推流地址');
                            console.log('推流ID:', jsonData.data.streamId);
                            console.log('推流地址:', jsonData.data.pushUrl);
                        } else {
                            console.log('❌ 获取推流地址失败');
                            console.log('错误信息:', jsonData.message);
                            console.log('错误代码:', jsonData.code);
                            if (jsonData.currentStreams !== undefined) {
                                console.log('当前流数:', jsonData.currentStreams);
                                console.log('最大流数:', jsonData.maxStreams);
                            }
                        }
                        
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

// 测试流ID解析
function testStreamIdParse() {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: config.host,
            port: config.port,
            path: '/zlm/testStreamIdParse',
            method: 'GET',
            headers: {
                'Authorization': `Bearer ${config.token}`,
                'Accept': 'application/json'
            }
        };

        const req = http.request(options, (res) => {
            let responseData = '';
            
            res.on('data', (chunk) => {
                responseData += chunk;
            });
            
            res.on('end', () => {
                console.log(`\n=== 测试流ID解析 ===`);
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
    console.log('开始测试流限制功能...\n');
    
    try {
        // 测试1: 检查流ID解析
        console.log('1. 测试流ID解析');
        await testStreamIdParse();
        
        // 等待1秒
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // 测试2: 第一次获取推流地址
        console.log('\n2. 第一次获取推流地址');
        const result1 = await testGetPushUrl();
        
        // 等待2秒
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        // 测试3: 第二次获取推流地址（应该触发流限制）
        console.log('\n3. 第二次获取推流地址（测试流限制）');
        const result2 = await testGetPushUrl();
        
        // 等待2秒
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        // 测试4: 第三次获取推流地址（应该自动删除最早的流）
        console.log('\n4. 第三次获取推流地址（测试自动删除）');
        const result3 = await testGetPushUrl();
        
        console.log('\n✅ 测试完成！');
        
        // 分析结果
        console.log('\n=== 测试结果分析 ===');
        if (result1 && result1.success) {
            console.log('✅ 第一次获取推流地址成功');
        }
        
        if (result2 && !result2.success) {
            console.log('✅ 第二次获取推流地址被限制（符合预期）');
        }
        
        if (result3 && result3.success) {
            console.log('✅ 第三次获取推流地址成功（自动删除最早的流）');
        } else if (result3 && !result3.success) {
            console.log('❌ 第三次获取推流地址仍然失败');
        }
        
    } catch (error) {
        console.error('\n❌ 测试失败:', error.message);
    }
}

// 运行测试
runTests(); 