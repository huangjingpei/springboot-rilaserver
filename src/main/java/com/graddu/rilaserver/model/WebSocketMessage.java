package net.enjoy.springboot.registrationlogin.model;

import lombok.Data;

@Data
public class WebSocketMessage {
    private String type;        // 消息类型：userEvent, bullet, userList, error
    private String message;     // 消息内容
    private Integer assignedId; // 用户ID
    private String role;        // 用户角色
    private String action;      // 动作：join, leave
    private String content;     // 弹幕内容
    private Object users;       // 用户列表
} 