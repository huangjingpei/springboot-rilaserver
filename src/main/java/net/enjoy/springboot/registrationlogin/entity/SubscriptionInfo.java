package net.enjoy.springboot.registrationlogin.entity;

public class SubscriptionInfo {

    //TODO 该类里面定义推流的stream id 来自StreamInfo里面的
    // 当前已经有多少订阅，
    // 该流支持的最大订阅数据
    // 是否来自公网的流媒体服务器（主流），如果不是那么就是来自局域网内的流媒体服务器（转发流）
    // 如果主流没有订阅的情况下，那么优先拉主流，反之优先拉转发流
    // 使用公网服务器的策略，优先局域网，当局域网不满足，再公网，当公网达到最大限制就返回给服务器禁止拉流
    // 拉流的真实地址

}
