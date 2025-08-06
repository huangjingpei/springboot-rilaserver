const fetch = require('node-fetch');

async function testLoginAPI() {
    console.log('=== 登录API测试 ===\n');
    
    const testCases = [
        {
            name: '最小请求（只有userId和password）',
            data: {
                userId: 'test@example.com',
                password: 'password123'
            }
        },
        {
            name: '完整请求',
            data: {
                userId: 'test@example.com',
                password: 'password123',
                deviceId: 'test-device-001',
                deviceName: '测试设备',
                deviceType: 'web',
                type: 'register'
            }
        },
        {
            name: '错误密码',
            data: {
                userId: 'test@example.com',
                password: 'wrongpassword'
            }
        },
        {
            name: '不存在的用户',
            data: {
                userId: 'nonexistent@example.com',
                password: 'password123'
            }
        }
    ];
    
    for (const testCase of testCases) {
        console.log(`\n--- ${testCase.name} ---`);
        
        try {
            const response = await fetch('http://127.0.0.1:8080/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json; charset=UTF-8',
                    'Accept': 'application/json; charset=UTF-8'
                },
                body: JSON.stringify(testCase.data)
            });
            
            console.log(`状态码: ${response.status} ${response.statusText}`);
            console.log('响应头:');
            response.headers.forEach((value, key) => {
                console.log(`  ${key}: ${value}`);
            });
            
            const responseText = await response.text();
            console.log('响应内容:');
            console.log(responseText);
            
        } catch (error) {
            console.error('请求失败:', error.message);
        }
    }
}

// 运行测试
testLoginAPI().catch(console.error); 