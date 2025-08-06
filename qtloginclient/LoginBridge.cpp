#include "loginbridge.h"
#include <QDebug>
#include <QCryptographicHash>
#include <QJsonDocument>
#include <QJsonObject>

LoginBridge::LoginBridge(QObject *parent)
    : QObject(parent)
    , m_settings(new QSettings("YourCompany", "LoginApp", this))
    , m_webSocketClient(new WebSocketClient(this))
{
    // 连接WebSocket信号
    connect(m_webSocketClient, &WebSocketClient::connected, 
            this, &LoginBridge::webSocketConnected);
    connect(m_webSocketClient, &WebSocketClient::disconnected, 
            this, &LoginBridge::webSocketDisconnected);
    connect(m_webSocketClient, &WebSocketClient::connectionError, 
            this, &LoginBridge::webSocketError);
    connect(m_webSocketClient, &WebSocketClient::bulletMessageReceived, 
            this, &LoginBridge::bulletMessageReceived);
    connect(m_webSocketClient, &WebSocketClient::userEventReceived, 
            this, &LoginBridge::userEventReceived);
    connect(m_webSocketClient, &WebSocketClient::streamStatusReceived, 
            [this](const QJsonObject &status) {
                QJsonDocument doc(status);
                emit streamStatusReceived(doc.toJson(QJsonDocument::Compact));
            });
}

void LoginBridge::onLoginSuccess(const QString &token)
{
    qDebug() << "Login successful! Token received:" << token;
    
    // 发出信号，通知Qt应用程序登录成功
    emit loginTokenReceived(token);
}

void LoginBridge::onLoginError(const QString &message)
{
    qDebug() << "Login failed:" << message;
    
    // 发出信号，通知Qt应用程序登录失败
    emit loginFailed(message);
}

void LoginBridge::onRegisterSuccess(const QString &message)
{
    qDebug() << "Registration successful:" << message;
    
    // 发出信号，通知Qt应用程序注册成功
    emit registerSucceeded(message);
}

void LoginBridge::onRegisterError(const QString &message)
{
    qDebug() << "Registration failed:" << message;
    
    // 发出信号，通知Qt应用程序注册失败
    emit registerFailed(message);
}

void LoginBridge::saveCredentials(const QString &userId, const QString &password)
{
    try {
        // 使用简单的Base64编码（注意：这不是加密，只是基本混淆）
        QByteArray encodedUserId = userId.toUtf8().toBase64();
        QByteArray encodedPassword = password.toUtf8().toBase64();
        
        // 保存到QSettings
        m_settings->setValue("savedUserId", QString::fromUtf8(encodedUserId));
        m_settings->setValue("savedPassword", QString::fromUtf8(encodedPassword));
        m_settings->setValue("rememberMe", true);
        
        qDebug() << "Credentials saved successfully";
    } catch (const std::exception &e) {
        qDebug() << "Failed to save credentials:" << e.what();
    }
}

QVariantMap LoginBridge::loadCredentials()
{
    QVariantMap result;
    
    try {
        QString encodedUserId = m_settings->value("savedUserId").toString();
        QString encodedPassword = m_settings->value("savedPassword").toString();
        bool rememberMe = m_settings->value("rememberMe", false).toBool();
        
        if (!encodedUserId.isEmpty() && !encodedPassword.isEmpty() && rememberMe) {
            // 解码
            QByteArray userIdBytes = QByteArray::fromBase64(encodedUserId.toUtf8());
            QByteArray passwordBytes = QByteArray::fromBase64(encodedPassword.toUtf8());
            
            QString userId = QString::fromUtf8(userIdBytes);
            QString password = QString::fromUtf8(passwordBytes);
            
            result["userId"] = userId;
            result["password"] = password;
            result["rememberMe"] = rememberMe;
            result["hasCredentials"] = true;
            
            qDebug() << "Credentials loaded successfully";
        } else {
            result["hasCredentials"] = false;
        }
    } catch (const std::exception &e) {
        qDebug() << "Failed to load credentials:" << e.what();
        result["hasCredentials"] = false;
    }
    
    return result;
}

void LoginBridge::clearCredentials()
{
    try {
        m_settings->remove("savedUserId");
        m_settings->remove("savedPassword");
        m_settings->remove("rememberMe");
        
        qDebug() << "Credentials cleared successfully";
    } catch (const std::exception &e) {
        qDebug() << "Failed to clear credentials:" << e.what();
    }
}

bool LoginBridge::hasSavedCredentials()
{
    QString encodedUserId = m_settings->value("savedUserId").toString();
    QString encodedPassword = m_settings->value("savedPassword").toString();
    bool rememberMe = m_settings->value("rememberMe", false).toBool();
    
    return !encodedUserId.isEmpty() && !encodedPassword.isEmpty() && rememberMe;
}

void LoginBridge::openRegisterPage()
{
    qDebug() << "Opening register page...";
    // 这里可以发出信号通知主窗口打开注册页面
    // 或者直接在这里处理注册页面的逻辑
    emit registerPageRequested();
}

void LoginBridge::backToLogin()
{
    qDebug() << "Back to login page...";
    // 发出信号通知主窗口返回登录页面
    emit backToLoginRequested();
}

void LoginBridge::loadLoginPage()
{
    qDebug() << "Load login page requested from JavaScript...";
    // 发出信号通知主窗口加载登录页面
    emit backToLoginRequested();
}

void LoginBridge::connectWebSocket(const QString &token, const QString &roomId, int role)
{
    qDebug() << "Connecting to WebSocket with token:" << token << "roomId:" << roomId << "role:" << role;
    
    WebSocketClient::UserRole userRole = static_cast<WebSocketClient::UserRole>(role);
    m_webSocketClient->connectToServer(token, "", roomId, userRole);
}

void LoginBridge::disconnectWebSocket()
{
    qDebug() << "Disconnecting from WebSocket...";
    m_webSocketClient->disconnectFromServer();
}

void LoginBridge::sendBulletMessage(const QString &content)
{
    qDebug() << "Sending bullet message:" << content;
    m_webSocketClient->sendBulletMessage(content);
}

bool LoginBridge::isWebSocketConnected() const
{
    return m_webSocketClient && 
           (m_webSocketClient->property("connectionState").toInt() == WebSocketClient::Connected);
} 