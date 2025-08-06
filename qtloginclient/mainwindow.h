#ifndef MAINWINDOW_H
#define MAINWINDOW_H

#include <QMainWindow>
#include <QWebEngineView>
#include <QVBoxLayout>
#include <QWidget>
#include <QWebChannel>
#include "LoginBridge.h"

class MainWindow : public QMainWindow
{
    Q_OBJECT

public:
    MainWindow(QWidget *parent = nullptr);
    ~MainWindow();

private slots:
    void onLoginTokenReceived(const QString &token);
    void onLoginFailed(const QString &message);
    void onRegisterSucceeded(const QString &message);
    void onRegisterFailed(const QString &message);
    void onPageLoadFinished(bool success);
    
    // WebSocket相关槽函数
    void onWebSocketConnected();
    void onWebSocketDisconnected();
    void onWebSocketError(const QString &error);
    void onBulletMessageReceived(const QString &userId, int assignedId, const QString &content);
    void onUserEventReceived(const QString &action, const QString &userId, int assignedId, const QString &role);
    void onStreamStatusReceived(const QString &status);


private:
    QWebEngineView *m_webView;
    LoginBridge *m_loginBridge;
    QWebChannel *m_webChannel;
    bool m_isPageLoading;
    void setupWebChannel();
    void loadAuthPage();
    void loadLoginPage();
    void loadRegisterPage();

};

#endif // MAINWINDOW_H
