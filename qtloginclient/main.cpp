#include <QApplication>
#include <QWebEngineView>
#include "mainwindow.h"

int main(int argc, char *argv[])
{
    QApplication app(argc, argv);
    
    // 设置应用程序信息
    app.setApplicationName("用户登录系统");
    app.setApplicationVersion("1.0");
    
    // 创建主窗口
    MainWindow window;
    window.show();
    
    return app.exec();
} 