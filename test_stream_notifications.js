const WebSocket = require('ws');
const http = require('http');

// 测试配置
const config = {
    host: '192.168.3.4',
    port: 8080,
    token: 'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJodWFuZ2ppbmdwZWlAZ21haWwuY29tfHJlZ2lzdGVyIiwiaWF0IjoxNzUzNjY3Nzg4LCJleHAiOjE3NTM3NTQxODh9.TnF4Met5U7GrRs_htBSRwkzCPGMe2ECU50xcbFygGlc8iLJKBXuawi-mgTAS65YZ'
};

// 创建WebSocket连接
function createWebSocketConnection(role, deviceId) {
    return new Promise((resolve, reject) => {
        const roomId = 'huangjingpei@gmail.com';
        const wsUrl = `ws://${config.host}:${config.port}/ws?token=${config.token}&roomId=${roomId}&deviceId=${deviceId}`;
        
        console.log(`连接WebSocket (${role}): ${wsUrl}`);
        
        const ws = new WebSocket(wsUrl);
        
        ws.on('open', () => {
            console.log(`✅ ${role} WebSocket连接成功`);
            resolve(ws);
        });
        
        ws.on('message', (data) => {
            try {
                const message = JSON.parse(data.toString());
                console.log(`📨 ${role} 收到消息:`, JSON.stringify(message, null, 2));
                
                // 检查推流开始通知
                if (message.type === 'streamEvent' && message.event === 'publishStarted') {
                    console.log(`🎉 ${role} 收到推流开始通知!`);
                    console.log('推流地址:', message.data.pushUrl);
                    console.log('播放地址:', message.data.rtmpUrl);
                }
                
                // 检查推流结束通知
                if (message.type === 'streamEvent' && message.event === 'publishStopped') {
                    console.log(`🔚 ${role} 收到推流结束通知!`);
                    console.log('流ID:', message.data.streamId);
                }
            } catch (e) {
                console.log(`📨 ${role} 收到消息:`, data.toString());
            }
        });
        
        ws.on('error', (error) => {
            console.error(`❌ ${role} WebSocket连接错误:`, error.message);
            reject(error);
        });
        
        ws.on('close', (code, reason) => {
            console.log(`🔌 ${role} WebSocket连接关闭:`, code, reason.toString());
        });
    });
}

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

// 执行测试
async function runTests() {
    console.log('开始测试推流通知功能...\n');
    
    let anchorWs = null;
    let proxyWs = null;
    
    try {
        // 测试1: 创建多个WebSocket连接
        console.log('1. 创建WebSocket连接');
        anchorWs = await createWebSocketConnection('anchor', 'anchor-device-' + Date.now());
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        proxyWs = await createWebSocketConnection('proxy', 'proxy-device-' + Date.now());
        await new Promise(resolve => setTimeout(resolve, 2000));
        
        // 测试2: 获取推流地址（触发推流开始通知）
        console.log('\n2. 获取推流地址（触发推流开始通知）');
        const result1 = await testGetPushUrl();
        
        // 等待3秒接收推流开始通知
        console.log('\n3. 等待接收推流开始通知...');
        await new Promise(resolve => setTimeout(resolve, 3000));
        
        // 测试3: 模拟推流结束（这里需要手动触发，或者等待一段时间）
        console.log('\n4. 等待推流结束通知...');
        console.log('注意：推流结束通知通常由ZLMediaKit的hook触发');
        console.log('如果没有自动触发，可能需要手动停止推流');
        await new Promise(resolve => setTimeout(resolve, 5000));
        
        console.log('\n✅ 测试完成！');
        
        // 分析结果
        console.log('\n=== 测试结果分析 ===');
        if (result1 && result1.success) {
            console.log('✅ 成功获取推流地址');
            console.log('推流ID:', result1.data.streamId);
        }
        
        console.log('\n📋 检查日志中的通知信息：');
        console.log('- 推流开始通知应该发送给proxy角色');
        console.log('- 推流结束通知应该发送给proxy角色');
        console.log('- 如果通知数量为0，说明没有找到合适的观众');
        
    } catch (error) {
        console.error('\n❌ 测试失败:', error.message);
    } finally {
        // 关闭连接
        if (anchorWs && anchorWs.readyState === WebSocket.OPEN) {
            console.log('\n5. 关闭anchor连接...');
            anchorWs.close();
        }
        if (proxyWs && proxyWs.readyState === WebSocket.OPEN) {
            console.log('6. 关闭proxy连接...');
            proxyWs.close();
        }
    }
}

// 运行测试
runTests(); 