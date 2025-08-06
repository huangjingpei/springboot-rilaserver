#include "mainwindow.h"
#include <QWebEngineProfile>
#include <QWebEnginePage>
#include <QWebChannel>
#include <QMessageBox>
#include <QApplication>
#include <QDir>
#include <QWebEngineSettings>
#include <QTimer>
#include <QFile>
#include <QColor>

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
    , m_webView(nullptr)
    , m_loginBridge(nullptr)
    , m_webChannel(nullptr)
    , m_isPageLoading(false)
{
    // 创建主窗口
    setWindowTitle("用户登录系统");
    resize(1280, 720);

    // 创建中央部件
    QWidget *centralWidget = new QWidget(this);
    setCentralWidget(centralWidget);

    // 创建布局
    QVBoxLayout *layout = new QVBoxLayout(centralWidget);
    layout->setContentsMargins(0, 0, 0, 0);

    // 创建WebView
    m_webView = new QWebEngineView(this);

    // 配置WebView设置
    QWebEngineSettings *settings = m_webView->settings();
    settings->setAttribute(QWebEngineSettings::LocalContentCanAccessRemoteUrls, true);
    settings->setAttribute(QWebEngineSettings::LocalContentCanAccessFileUrls, true);
    settings->setAttribute(QWebEngineSettings::AllowRunningInsecureContent, true);

    // 配置持久化存储
    QString appDir = QApplication::applicationDirPath();
    QString dataPath = appDir + "/webdata";
    QDir().mkpath(dataPath);

    QWebEngineProfile *profile = new QWebEngineProfile("PersistentProfile", this);
    profile->setPersistentStoragePath(dataPath);
    profile->setPersistentCookiesPolicy(QWebEngineProfile::AllowPersistentCookies);

    m_webView->setPage(new QWebEnginePage(profile, this));

    layout->addWidget(m_webView);

    // 创建LoginBridge
    m_loginBridge = new LoginBridge(this);

    // 设置WebChannel
    setupWebChannel();

    // 加载登录页面
    loadAuthPage();

    // 连接页面加载完成信号
    connect(m_webView, &QWebEngineView::loadFinished, this, &MainWindow::onPageLoadFinished);
}

MainWindow::~MainWindow()
{
}

void MainWindow::setupWebChannel()
{
    // 创建WebChannel
    m_webChannel = new QWebChannel(this);

    // 注册LoginBridge对象
    m_webChannel->registerObject(QStringLiteral("loginBridge"), m_loginBridge);

    // 将WebChannel设置到WebView
    m_webView->page()->setWebChannel(m_webChannel);

    // 连接信号
    connect(m_loginBridge, &LoginBridge::loginTokenReceived,
            this, &MainWindow::onLoginTokenReceived);
    connect(m_loginBridge, &LoginBridge::loginFailed,
            this, &MainWindow::onLoginFailed);
    connect(m_loginBridge, &LoginBridge::registerSucceeded,
            this, &MainWindow::onRegisterSucceeded);
    connect(m_loginBridge, &LoginBridge::registerFailed,
            this, &MainWindow::onRegisterFailed);
    
    // 连接WebSocket信号
    connect(m_loginBridge, &LoginBridge::webSocketConnected,
            this, &MainWindow::onWebSocketConnected);
    connect(m_loginBridge, &LoginBridge::webSocketDisconnected,
            this, &MainWindow::onWebSocketDisconnected);
    connect(m_loginBridge, &LoginBridge::webSocketError,
            this, &MainWindow::onWebSocketError);
    connect(m_loginBridge, &LoginBridge::bulletMessageReceived,
            this, &MainWindow::onBulletMessageReceived);
    connect(m_loginBridge, &LoginBridge::userEventReceived,
            this, &MainWindow::onUserEventReceived);
    connect(m_loginBridge, &LoginBridge::streamStatusReceived,
            this, &MainWindow::onStreamStatusReceived);
}

void MainWindow::loadAuthPage()
{
    qDebug() << "Loading user auth page...";
    if (m_isPageLoading) {
        qDebug() << "Page is already loading, ignoring request";
        return;
    }
    m_isPageLoading = true;
    m_webView->page()->setBackgroundColor(QColor(248, 249, 250));
    QString path = ":/resources/user-auth-qt.html";
    if (!QFile::exists(path)) {
        m_webView->setHtml("<html><body style='background-color: #f8f9fa;'><div style='text-align:center;padding:50px;color:red;'>用户认证页面文件未找到</div></body></html>");
        m_isPageLoading = false;
        return;
    }
    QUrl url("qrc:///resources/user-auth-qt.html");
    m_webView->load(url);
}

void MainWindow::onPageLoadFinished(bool success)
{
    qDebug() << "Page load finished, success:" << success;
    
    if (success) {
        qDebug() << "Page loaded successfully";
        
        // 标记页面加载完成
        m_isPageLoading = false;
        
        // 重新设置WebChannel，确保新页面可以访问Qt Bridge
        // 每次页面加载完成后都需要重新设置，因为新页面会丢失WebChannel连接
        qDebug() << "Setting WebChannel for new page";
        m_webView->page()->setWebChannel(m_webChannel);
        
        // 重置页面的凭据加载状态，确保新页面可以正确加载凭据
        m_webView->page()->runJavaScript("if(typeof credentialsLoaded !== 'undefined') { credentialsLoaded = false; console.log('Reset credentialsLoaded flag'); }");

        // 凭据加载由HTML页面自己处理，避免重复调用
    } else {
        qDebug() << "Failed to load page";
        m_isPageLoading = false;
    }
}

void MainWindow::onLoginTokenReceived(const QString &token)
{
    // 显示成功消息
    QMessageBox::information(this, "登录成功", "登录成功！欢迎使用RILA系统。");

    // 在这里您可以处理登录成功后的逻辑
    qDebug() << "MainWindow received token:" << token;
    
    // 自动连接WebSocket（可选）
    // m_loginBridge->connectWebSocket(token, "default", 3); // 3 = Viewer角色
    
    // 可以在这里添加跳转到主应用界面的逻辑
    // 例如：close(); 或者 hide(); 然后显示主窗口
}

void MainWindow::onLoginFailed(const QString &message)
{
    // 显示登录失败消息
    QMessageBox::warning(this, "登录失败", 
                        QString("登录失败：%1\n\n请检查您的用户名和密码是否正确。").arg(message));
    
    qDebug() << "Login failed:" << message;
}

void MainWindow::onRegisterSucceeded(const QString &message)
{
    // 显示注册成功消息
    QMessageBox::information(this, "注册成功", 
                           QString("注册成功：%1\n\n请使用新账户登录。").arg(message));
    
    qDebug() << "Registration successful:" << message;
}

void MainWindow::onRegisterFailed(const QString &message)
{
    // 显示注册失败消息
    QMessageBox::warning(this, "注册失败", 
                        QString("注册失败：%1\n\n请检查输入信息是否正确。").arg(message));
    
    qDebug() << "Registration failed:" << message;
}

void MainWindow::onWebSocketConnected()
{
    qDebug() << "[MainWindow] WebSocket连接成功";
    // 可以在这里更新UI状态，显示连接状态等
}

void MainWindow::onWebSocketDisconnected()
{
    qDebug() << "[MainWindow] WebSocket连接断开";
    // 可以在这里更新UI状态
}

void MainWindow::onWebSocketError(const QString &error)
{
    qDebug() << "[MainWindow] WebSocket错误:" << error;
    // 可以选择是否向用户显示错误信息
    // QMessageBox::warning(this, "WebSocket错误", error);
}

void MainWindow::onBulletMessageReceived(const QString &userId, int assignedId, const QString &content)
{
    qDebug() << "[MainWindow] 收到弹幕消息: userId=" << userId 
             << ", assignedId=" << assignedId << ", content=" << content;
    
    // 在这里可以将弹幕消息显示在界面上
    // 或者转发给HTML页面处理
}

void MainWindow::onUserEventReceived(const QString &action, const QString &userId, 
                                    int assignedId, const QString &role)
{
    qDebug() << "[MainWindow] 用户事件: action=" << action 
             << ", userId=" << userId << ", assignedId=" << assignedId << ", role=" << role;
    
    // 处理用户进入/离开事件
    if (action == "join") {
        qDebug() << "用户" << userId << "(" << role << ")加入房间";
    } else if (action == "leave") {
        qDebug() << "用户" << userId << "(" << role << ")离开房间";
    }
}

void MainWindow::onStreamStatusReceived(const QString &status)
{
    qDebug() << "[MainWindow] 收到推流状态:" << status;
    
    // 可以解析JSON并更新UI显示推流状态
    // 或者转发给HTML页面处理
}
