package com.itgu.Controller;

import com.itgu.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Map;

@RestController
@RequestMapping("/train")
public class addTrainController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("${train.image.path}")
    private String savePath;

    @Value("${train.db.prefix}")
    private String dbPathPrefix;

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<?>> uploadTrainImage(
            @RequestParam("image") MultipartFile image,
            @RequestParam("rockName") String rockName) {
        try {
            String originalName = image.getOriginalFilename();
            String ext = "";

            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            } else {
                ext = ".jpg"; // 默认 jpg
            }

            // 保存图片到磁盘
            String fileName = System.currentTimeMillis() + "_" + originalName + ext;
            File dest = new File(savePath + fileName);
            image.transferTo(dest);

            // 写入数据库的 feedbacks 表
            String sql = "INSERT INTO feedbacks (rock_name, image_path, is_deleted, in_training_set) " +
                    "VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(sql, rockName, dbPathPrefix + fileName, 0, 1);

            //  返回统一 JSON
            return ResponseEntity.ok(ApiResponse.success(
                    "添加成功",
                    Map.of(
                            "rockName", rockName,
                            "imagePath", dbPathPrefix + fileName
                    )
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("添加失败", e.getMessage()));
        }
    }
}
