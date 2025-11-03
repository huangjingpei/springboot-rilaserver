const WebSocket = require('ws');

// 测试配置
const config = {
    host: '192.168.3.4',
    port: 8080,
    token: 'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJodWFuZ2ppbmdwZWlAZ21haWwuY29tfHJlZ2lzdGVyIiwiaWF0IjoxNzUzNjY3Nzg4LCJleHAiOjE3NTM3NTQxODh9.TnF4Met5U7GrRs_htBSRwkzCPGMe2ECU50xcbFygGlc8iLJKBXuawi-mgTAS65YZ'
};

// 创建观众WebSocket连接
function createViewerConnection() {
    return new Promise((resolve, reject) => {
        const roomId = 'huangjingpei@gmail.com';
        const deviceId = 'viewer-test-device-' + Date.now();
        const wsUrl = `ws://${config.host}:${config.port}/ws?token=${config.token}&roomId=${roomId}&deviceId=${deviceId}`;
        
        console.log('连接观众WebSocket:', wsUrl);
        
        const ws = new WebSocket(wsUrl);
        
        ws.on('open', () => {
            console.log('✅ 观众WebSocket连接成功');
            resolve(ws);
        });
        
        ws.on('message', (data) => {
            try {
                const message = JSON.parse(data.toString());
                console.log('📨 观众收到消息:', JSON.stringify(message, null, 2));
                
                // 检查是否是推流开始通知
                if (message.type === 'streamEvent' && message.event === 'publishStarted') {
                    console.log('🎉 观众收到推流开始通知!');
                    console.log('推流地址:', message.data.pushUrl);
                    console.log('播放地址:', message.data.rtmpUrl);
                }
            } catch (e) {
                console.log('📨 观众收到消息:', data.toString());
            }
        });
        
        ws.on('error', (error) => {
            console.error('❌ 观众WebSocket连接错误:', error.message);
            reject(error);
        });
        
        ws.on('close', (code, reason) => {
            console.log('🔌 观众WebSocket连接关闭:', code, reason.toString());
        });
    });
}

// 测试推流通知API
async function testStreamNotification() {
    const http = require('http');
    
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
                console.log(`\n=== 测试推流地址获取 ===`);
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
    console.log('开始测试观众WebSocket通知功能...\n');
    
    let viewerWs = null;
    
    try {
        // 测试1: 创建观众WebSocket连接
        console.log('1. 创建观众WebSocket连接');
        viewerWs = await createViewerConnection();
        
        // 等待3秒让连接稳定
        console.log('\n2. 等待连接稳定...');
        await new Promise(resolve => setTimeout(resolve, 3000));
        
        // 测试3: 获取推流地址（这会触发推流开始通知）
        console.log('\n3. 测试获取推流地址');
        await testStreamNotification();
        
        // 等待5秒接收通知
        console.log('\n4. 等待接收推流通知...');
        await new Promise(resolve => setTimeout(resolve, 5000));
        
        console.log('\n✅ 测试完成！');
        
    } catch (error) {
        console.error('\n❌ 测试失败:', error.message);
    } finally {
        // 关闭连接
        if (viewerWs && viewerWs.readyState === WebSocket.OPEN) {
            console.log('\n5. 关闭观众连接...');
            viewerWs.close();
        }
    }
}

// 运行测试
runTests(); 