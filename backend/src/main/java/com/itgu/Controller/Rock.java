package com.itgu.Controller;

import com.alibaba.fastjson.JSONArray;
import com.itgu.Service.PyUtils;
import com.itgu.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@RequestMapping("/rock")
@RestController
@RequiredArgsConstructor
public class Rock {

    private final PyUtils pyUtils;

    @PostMapping("/recognize")
    public ApiResponse<JSONArray> getRecon(
            @RequestParam("image") MultipartFile image,
            @RequestParam("hardness") float hardness,
            @RequestParam("composition") String composition,
            @RequestParam("texture") String texture,
            @RequestParam("color") String color
    ) throws Exception {

        // ----------------------------------------------------
        // 1. 检查上传文件
        // ----------------------------------------------------

        if (image == null || image.isEmpty()) {
            throw new IllegalArgumentException(
                    "上传图片不能为空"
            );
        }

        String originalFilename =
                image.getOriginalFilename();

        String extension = ".jpg";

        if (originalFilename != null
                && !originalFilename.isBlank()) {

            int dotIndex =
                    originalFilename.lastIndexOf('.');

            if (dotIndex >= 0
                    && dotIndex < originalFilename.length() - 1) {

                extension =
                        originalFilename.substring(dotIndex);
            }
        }

        /*
         * 目前为了兼容旧 Android API，
         * hardness / composition / texture / color
         * 仍然作为 HTTP 参数接收。
         *
         * 新模型只使用图像进行推理，
         * 这些人工特征不再参与模型预测。
         */

        log.info(
                "收到旧版辅助特征参数: "
                        + "hardness={}, "
                        + "composition={}, "
                        + "texture={}, "
                        + "color={}",
                hardness,
                composition,
                texture,
                color
        );

        Path tempImage = null;

        try {

            // ------------------------------------------------
            // 2. 创建临时图片
            // ------------------------------------------------

            tempImage =
                    Files.createTempFile(
                            "rock-recognition-",
                            extension
                    );

            image.transferTo(
                    tempImage.toFile()
            );

            log.debug(
                    "识别临时图片已创建: {}",
                    tempImage
            );

            // ------------------------------------------------
            // 3. 调用独立 FastAPI / PyTorch 推理服务
            // ------------------------------------------------

            String inferenceJson =
                    pyUtils.searchRock(
                            tempImage
                                    .toAbsolutePath()
                                    .toString()
                    );

            // ------------------------------------------------
            // 4. 推理结果 JSON → Java JSONArray
            // ------------------------------------------------

            JSONArray results =
                    JSONArray.parseArray(
                            inferenceJson
                    );

            if (results == null) {
                throw new IllegalStateException(
                        "推理服务返回结果无法解析"
                );
            }

            log.info(
                    "岩石识别完成，Top-K 数量: {}",
                    results.size()
            );

            // ------------------------------------------------
            // 5. 返回 Android
            // ------------------------------------------------

            return ApiResponse.success(
                    "图片识别成功",
                    results
            );

        } finally {

            // ------------------------------------------------
            // 6. 无论识别成功或失败，都清理临时图片
            // ------------------------------------------------

            if (tempImage != null) {

                try {

                    boolean deleted =
                            Files.deleteIfExists(
                                    tempImage
                            );

                    if (deleted) {
                        log.debug(
                                "识别临时图片已删除: {}",
                                tempImage
                        );
                    }

                } catch (Exception e) {

                    /*
                     * 临时文件删除失败不应该覆盖
                     * 原本的推理异常或接口返回。
                     */

                    log.warn(
                            "识别临时图片删除失败: {}",
                            tempImage,
                            e
                    );
                }
            }
        }
    }
}