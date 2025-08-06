#ifndef LOGINBRIDGE_H
#define LOGINBRIDGE_H

#include <QObject>
#include <QString>
#include <QSettings>
#include <QVariantMap>
#include "WebSocketClient.h"

class LoginBridge : public QObject
{
    Q_OBJECT

public:
    explicit LoginBridge(QObject *parent = nullptr);

public slots:
    // 登录相关回调（被JavaScript调用）
    void onLoginSuccess(const QString &token);
    void onLoginError(const QString &message);
    
    // 注册相关回调（被JavaScript调用）
    void onRegisterSuccess(const QString &message);
    void onRegisterError(const QString &message);
    
    // JavaScript可调用的方法
    void saveCredentials(const QString &userId, const QString &password);
    QVariantMap loadCredentials();
    void clearCredentials();
    
    // 检查是否有保存的凭据
    bool hasSavedCredentials();
    
    // 打开注册页面
    void openRegisterPage();
    
    // 返回登录页面
    void backToLogin();
    
    // 加载登录页面（供JavaScript调用）
    void loadLoginPage();
    
    // WebSocket相关方法
    void connectWebSocket(const QString &token, const QString &roomId = "default", int role = 3);
    void disconnectWebSocket();
    void sendBulletMessage(const QString &content);
    bool isWebSocketConnected() const;

signals:
    // 登录相关信号
    void loginTokenReceived(const QString &token);
    void loginFailed(const QString &message);
    
    // 注册相关信号
    void registerSucceeded(const QString &message);
    void registerFailed(const QString &message);
    
    // 当凭据加载完成时发出
    void credentialsLoaded(const QString &userId, const QString &password, bool rememberMe);
    
    // 当请求打开注册页面时发出
    void registerPageRequested();
    
    // 当请求返回登录页面时发出
    void backToLoginRequested();
    
    // WebSocket相关信号
    void webSocketConnected();
    void webSocketDisconnected();
    void webSocketError(const QString &error);
    void bulletMessageReceived(const QString &userId, int assignedId, const QString &content);
    void userEventReceived(const QString &action, const QString &userId, int assignedId, const QString &role);
    void streamStatusReceived(const QString &status);

private:
    QSettings *m_settings;
    WebSocketClient *m_webSocketClient;
};

#endif // LOGINBRIDGE_H 