package com.softwarevax.flume.client.web;

import com.softwarevax.flume.agent.entity.AgentEntity;
import com.softwarevax.flume.client.web.service.FlumeClientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class FlumeClientController {

    @Autowired
    private FlumeClientService service;

    /**
     * 由client决定在哪里运行，也可以指定attachTo，在哪个client运行
     * @param entity 任务实体
     * @return 提交任务并启动的状态
     */
    @PostMapping("/submit")
    public String submit(@RequestBody AgentEntity entity) {
        return service.submit(entity);
    }

    /**
     * 只能在当前client运行
     * @param entity 任务实体
     * @return 提交任务并启动的状态
     */
    @PostMapping("/start")
    public String start(@RequestBody AgentEntity entity) {
        return service.start(entity);
    }

    /**
     * 由client找到agent，并停止
     * @param agentName 任务名称
     * @return 停止任务的状态
     */
    @PostMapping("/stop")
    public String stop(String agentName) {
        return service.stop(agentName);
    }
}
