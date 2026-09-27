package com.itgu.Controller;

import com.itgu.Pojo.ModelParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/model")
@Slf4j
public class ModelController {

    @PostMapping("/updateParams")
    public ResponseEntity<String> updateParams(@RequestBody ModelParams params) {

        try {

            log.info("收到更新参数请求: 学习率={}, 批量大小={}, 迭代次数={}",
                    params.getLearningRate(), params.getBatchSize(), params.getEpochs());
            return ResponseEntity.ok("参数接收成功！");
        } catch (Exception e) {
            log.error("接收参数失败", e);
            return ResponseEntity.status(500).body("接收参数失败: " + e.getMessage());
        }
    }



}

