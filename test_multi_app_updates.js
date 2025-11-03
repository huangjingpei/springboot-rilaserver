const http = require('http');

// 测试配置
const config = {
    host: '192.168.3.4',
    port: 8080,
    token: 'eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJodWFuZ2ppbmdwZWlAZ21haWwuY29tfHJlZ2lzdGVyIiwiaWF0IjoxNzUzNjY3Nzg4LCJleHAiOjE3NTM3NTQxODh9.TnF4Met5U7GrRs_htBSRwkzCPGMe2ECU50xcbFygGlc8iLJKBXuawi-mgTAS65YZ'
};

// 测试应用管理API
async function testAppManagement() {
    console.log('\n=== 测试应用管理API ===');
    
    // 1. 获取所有应用
    console.log('\n1. 获取所有应用');
    await makeRequest('/api/v1/updates/apps', 'GET');
    
    // 2. 获取激活的应用
    console.log('\n2. 获取激活的应用');
    await makeRequest('/api/v1/updates/apps/active', 'GET');
    
    // 3. 根据应用标识符获取应用
    console.log('\n3. 根据应用标识符获取应用');
    await makeRequest('/api/v1/updates/apps/app/rila-live-assistant', 'GET');
    
    // 4. 获取应用分类
    console.log('\n4. 获取应用分类');
    await makeRequest('/api/v1/updates/apps/categories', 'GET');
    
    // 5. 获取应用标签
    console.log('\n5. 获取应用标签');
    await makeRequest('/api/v1/updates/apps/tags', 'GET');
    
    // 6. 根据分类获取应用
    console.log('\n6. 根据分类获取应用');
    await makeRequest('/api/v1/updates/apps/category/live-streaming', 'GET');
    
    // 7. 搜索应用
    console.log('\n7. 搜索应用');
    await makeRequest('/api/v1/updates/apps/search?keyword=rila', 'GET');
}

// 测试升级检查API
async function testUpdateCheck() {
    console.log('\n=== 测试升级检查API ===');
    
    // 1. 检查rila-live-assistant的更新
    console.log('\n1. 检查rila-live-assistant的更新');
    await makeRequest('/api/v1/updates/latest?appId=rila-live-assistant&currentVersion=1.2.2&platform=windows', 'GET');
    
    // 2. 检查rila-stream-manager的更新
    console.log('\n2. 检查rila-stream-manager的更新');
    await makeRequest('/api/v1/updates/latest?appId=rila-stream-manager&currentVersion=2.0.5&platform=linux', 'GET');
    
    // 3. 检查不存在的应用
    console.log('\n3. 检查不存在的应用');
    await makeRequest('/api/v1/updates/latest?appId=non-existent-app&currentVersion=1.0.0&platform=windows', 'GET');
}

// 测试升级包管理API
async function testUpdatePackageManagement() {
    console.log('\n=== 测试升级包管理API ===');
    
    // 1. 获取所有升级包
    console.log('\n1. 获取所有升级包');
    await makeRequest('/api/v1/updates?page=0&size=10', 'GET');
    
    // 2. 根据应用和平台获取升级包
    console.log('\n2. 根据应用和平台获取升级包');
    await makeRequest('/api/v1/updates/app/rila-live-assistant/platform/windows?page=0&size=10', 'GET');
    
    // 3. 获取强制更新的版本
    console.log('\n3. 获取强制更新的版本');
    await makeRequest('/api/v1/updates/mandatory/rila-stream-manager/windows', 'GET');
    
    // 4. 验证版本号格式
    console.log('\n4. 验证版本号格式');
    await makeRequest('/api/v1/updates/validate-version?version=1.2.3', 'GET');
    
    // 5. 比较版本号
    console.log('\n5. 比较版本号');
    await makeRequest('/api/v1/updates/compare-versions?version1=1.2.3&version2=1.2.4', 'GET');
}

// 通用HTTP请求函数
function makeRequest(path, method, data = null) {
    return new Promise((resolve, reject) => {
        const options = {
            hostname: config.host,
            port: config.port,
            path: path,
            method: method,
            headers: {
                'Authorization': `Bearer ${config.token}`,
                'Accept': 'application/json',
                'Content-Type': 'application/json'
            }
        };

        const req = http.request(options, (res) => {
            let responseData = '';
            
            res.on('data', (chunk) => {
                responseData += chunk;
            });
            
            res.on('end', () => {
                console.log(`${method} ${path} - 状态码: ${res.statusCode}`);
                
                if (responseData) {
                    try {
                        const jsonData = JSON.parse(responseData);
                        console.log('响应数据:', JSON.stringify(jsonData, null, 2));
                    } catch (e) {
                        console.log('响应数据:', responseData);
                    }
                }
                
                resolve({ statusCode: res.statusCode, data: responseData });
            });
        });

        req.on('error', (error) => {
            console.error(`请求错误: ${error.message}`);
            reject(error);
        });
        
        if (data) {
            req.write(JSON.stringify(data));
        }
        
        req.end();
    });
}

// 执行测试
async function runTests() {
    console.log('开始测试多应用升级系统...\n');
    
    try {
        // 测试应用管理API
        await testAppManagement();
        
        // 等待1秒
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // 测试升级检查API
        await testUpdateCheck();
        
        // 等待1秒
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        // 测试升级包管理API
        await testUpdatePackageManagement();
        
        console.log('\n✅ 多应用升级系统测试完成！');
        
        // 总结
        console.log('\n=== 测试总结 ===');
        console.log('✅ 应用管理API测试完成');
        console.log('✅ 升级检查API测试完成');
        console.log('✅ 升级包管理API测试完成');
        console.log('\n📋 新功能特性：');
        console.log('- 支持多个应用的独立升级管理');
        console.log('- 应用分类和标签管理');
        console.log('- 应用版本管理（当前版本、推荐版本、最低版本）');
        console.log('- 应用状态管理（激活、公开、强制更新）');
        console.log('- 基于应用标识符的升级检查');
        console.log('- 完整的应用CRUD操作');
        
    } catch (error) {
        console.error('\n❌ 测试失败:', error.message);
    }
}

// 运行测试
runTests(); 