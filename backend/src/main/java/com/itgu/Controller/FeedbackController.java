package com.itgu.Controller;

import com.itgu.Mapper.FeedbackRepository;
import com.itgu.dto.ApiResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;
@RestController
@RequestMapping("/feedback")
public class FeedbackController {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Value("${feedback.image.path}")
    private String feedbackImagePath;

    @Value("${feedback.db.prefix}")
    private String feedbackDbPrefix;

    // 获取反馈列表（过滤掉 is_deleted=1 和 in_training_set=1 的）
    @GetMapping("/list")
    public List<com.itgu.Pojo.Feedback> getFeedbackList() {
        return feedbackRepository.findByIsDeletedAndInTrainingSet(0, 0);
    }

    // 上传反馈
    @PostMapping("/submit")
    public ResponseEntity<?> uploadFeedback(@RequestParam("rockName") String rockName,
                                            @RequestParam("image") MultipartFile image) {
        if (image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "上传失败：文件为空"));
        }

        try {
            // 保存图片到 feedback_image 文件夹
            File folder = new File(feedbackImagePath);
            if (!folder.exists()) folder.mkdirs();

            // 保留真实文件后缀
            String originalName = image.getOriginalFilename();
            String ext = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";

            String filename = System.currentTimeMillis() + "_" + UUID.randomUUID() + ext;
            File dest = new File(folder, filename);
            image.transferTo(dest);

            // 存入数据库
            com.itgu.Pojo.Feedback feedback = new com.itgu.Pojo.Feedback();
            feedback.setRockName(rockName);
            feedback.setImagePath(feedbackDbPrefix  + filename); // 前端访问路径
            feedbackRepository.save(feedback);

            // 返回 JSON
            return ResponseEntity.ok(ApiResponse.success(
                    "上传成功",
                    Map.of(
                            "rockName", rockName,
                            "imagePath", feedbackDbPrefix  + filename
                    )
            ));

        } catch (IOException e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("上传失败: " + e.getMessage(), null));

        }
    }

    // 获取图片
    @GetMapping("/image")
    public ResponseEntity<Resource> getFeedbackImage(@RequestParam String filename,
                                                     @RequestHeader("Authorization") String authHeader,
                                                     HttpSession session) {
        String tokenFromHeader = authHeader.replace("Bearer ", "");
        String sessionToken = (String) session.getAttribute("Token");
        if (sessionToken == null || !sessionToken.equals(tokenFromHeader)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Path path = Paths.get(feedbackImagePath + filename);
        try {
            Resource resource = new UrlResource(path.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 删除反馈（逻辑 + 物理删除）
    @PutMapping("/delete/{id}")
    public ResponseEntity<?> deleteFeedback(@PathVariable Long id) {
        return feedbackRepository.findById(id)
                .map(feedback -> {
                    feedback.setIsDeleted(1); // 标记删除
                    feedbackRepository.save(feedback);


                    return ResponseEntity.ok(ApiResponse.success("删除成功", null));
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("反馈不存在", null)));
    }

    // 补充至训练集
    @PutMapping("/addToTraining/{id}")
    public ResponseEntity<?> addToTraining(@PathVariable Long id,
         @Value("${train.image.path}") String trainImagePath) {
        return feedbackRepository.findById(id)
                .map(feedback -> {
                    feedback.setInTrainingSet(1);
                    feedbackRepository.save(feedback);

                    String sourcePathStr = feedbackImagePath + Paths.get(feedback.getImagePath()).getFileName();
                    String targetFolderStr = trainImagePath;
                    try {
                        Path sourcePath = Paths.get(sourcePathStr);
                        Path targetFolder = Paths.get(targetFolderStr);
                        if (!Files.exists(targetFolder)) Files.createDirectories(targetFolder);

                        Path targetPath = targetFolder.resolve(sourcePath.getFileName());
                        Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                        return ResponseEntity.ok(ApiResponse.success("已补充至训练集", null));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("操作失败: " + e.getMessage(), null));

                    }
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(ApiResponse.error("反馈不存在", null)));
    }
}
