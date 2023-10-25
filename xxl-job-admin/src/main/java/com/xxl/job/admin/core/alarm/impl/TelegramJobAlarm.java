package com.xxl.job.admin.core.alarm.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xxl.job.admin.core.alarm.JobAlarm;
import com.xxl.job.admin.core.model.XxlJobInfo;
import com.xxl.job.admin.core.model.XxlJobLog;
import com.xxl.job.admin.core.util.JacksonUtil;
import com.xxl.job.core.biz.model.ReturnT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
public class TelegramJobAlarm implements JobAlarm {

    @Value("${telegram.chatId:empty}")
    private String chatId;

    @Value("${telegram.token:empty}")
    private String token;

    @Override
    public boolean doAlarm(XxlJobInfo info, XxlJobLog jobLog) {
        String url = "https://api.telegram.org/bot"+token+"/sendMessage";
        String text = getSendText(jobLog);
        sendMsg(url,chatId,text);
        return true;
    }

    private String getSendText(XxlJobLog jobLog) {
        // alarmContent
        String alarmContent = "Job Handler=" + jobLog.getExecutorHandler();
        if (jobLog.getTriggerCode() != ReturnT.SUCCESS_CODE) {
            alarmContent += "<br/>TriggerMsg=<br/>" + jobLog.getTriggerMsg();
        }
        if (jobLog.getHandleCode()>0 && jobLog.getHandleCode() != ReturnT.SUCCESS_CODE) {
            alarmContent += "<br/>HandleCode=" + jobLog.getHandleMsg();
        }
        return alarmContent ;
    }

    private static void sendMsg(String url,String chatId,String text){
        // HTTP请求类构建
        RestTemplate restTemplate = new RestTemplate();
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(
                new Proxy(
                        Proxy.Type.HTTP,
                        new InetSocketAddress("127.0.0.1", 1080)  //设置代理服务
                )
        );
        restTemplate.setRequestFactory(requestFactory);
        // 构建body
        HashMap<String, String> params = new HashMap<>();
        params.put("chat_id",chatId);
        params.put("text",text);
        // params.put("parse_mode","MarkdownV2");
        String data = JacksonUtil.writeValueAsString(params);

        // 发送请求
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> requestEntity = new HttpEntity<>(data,headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
        System.out.println(response);
    }

    public static void main(String[] args) {

    }
}
