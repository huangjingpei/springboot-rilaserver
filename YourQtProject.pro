QT += core gui webenginewidgets webchannel

greaterThan(QT_MAJOR_VERSION, 4): QT += widgets

CONFIG += c++17

TARGET = YourQtProject
TEMPLATE = app

# 源文件
SOURCES += \
    main.cpp \
    mainwindow.cpp \
    loginbridge.cpp

# 头文件
HEADERS += \
    mainwindow.h \
    loginbridge.h

# 资源文件
RESOURCES += \
    resources.qrc

# 默认规则使 moc 处理所有头文件
QT += core-private

# 编译选项
DEFINES += QT_DEPRECATED_WARNINGS

# 平台特定设置
win32 {
    # Windows 特定设置
}

unix:!macx {
    # Linux 特定设置
}

macx {
    # macOS 特定设置
} 