package com.itgu.Service.impt;

import com.itgu.Service.PyUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class PyUtilsImpt implements PyUtils {

    /*
     * Python FastAPI 常驻推理服务地址。
     *
     * 例如：
     * http://127.0.0.1:8000/predict
     */
    @Value("${app.inference.url}")
    private String inferenceUrl;

    private static final Duration REQUEST_TIMEOUT =
            Duration.ofSeconds(30);

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    @Override
    public String searchRock(String imagePath) {

        Path image = Paths.get(imagePath)
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(image)) {
            throw new IllegalArgumentException(
                    "待识别图片不存在: " + image
            );
        }

        try {
            log.info(
                    "开始调用 Python 推理服务，图片: {}",
                    image
            );

            String boundary =
                    "----RockBoundary" + UUID.randomUUID();

            byte[] requestBody =
                    buildMultipartBody(image, boundary);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(inferenceUrl))
                    .timeout(REQUEST_TIMEOUT)
                    .header(
                            "Content-Type",
                            "multipart/form-data; boundary=" + boundary
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofByteArray(
                                    requestBody
                            )
                    )
                    .build();

            long startTime = System.nanoTime();

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString(
                                    StandardCharsets.UTF_8
                            )
                    );

            long elapsedMillis =
                    (System.nanoTime() - startTime) / 1_000_000;

            log.info(
                    "Python 推理服务响应: status={}, elapsed={}ms",
                    response.statusCode(),
                    elapsedMillis
            );

            String responseBody = response.body();

            if (response.statusCode() != 200) {
                throw new IllegalStateException(
                        "Python 推理服务调用失败，HTTP "
                                + response.statusCode()
                                + ", body="
                                + responseBody
                );
            }

            if (responseBody == null
                    || responseBody.isBlank()) {

                throw new IllegalStateException(
                        "Python 推理服务返回空响应"
                );
            }

            log.info(
                    "Python 推理结果: {}",
                    responseBody
            );

            return responseBody.trim();

        } catch (IOException e) {

            throw new IllegalStateException(
                    "无法连接 Python 推理服务: "
                            + inferenceUrl,
                    e
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IllegalStateException(
                    "调用 Python 推理服务时被中断",
                    e
            );
        }
    }

    /**
     * 构造 multipart/form-data 请求体。
     *
     * FastAPI 接口要求字段名为：
     *
     *     image
     *
     * 与：
     *
     *     UploadFile = File(...)
     *
     * 对应。
     */
    private byte[] buildMultipartBody(
            Path image,
            String boundary
    ) throws IOException {

        byte[] imageBytes = Files.readAllBytes(image);

        String filename =
                image.getFileName().toString();

        String contentType =
                Files.probeContentType(image);

        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        String header =
                "--" + boundary + "\r\n"
                + "Content-Disposition: form-data; "
                + "name=\"image\"; "
                + "filename=\"" + filename + "\"\r\n"
                + "Content-Type: "
                + contentType
                + "\r\n\r\n";

        String footer =
                "\r\n--"
                + boundary
                + "--\r\n";

        byte[] headerBytes =
                header.getBytes(StandardCharsets.UTF_8);

        byte[] footerBytes =
                footer.getBytes(StandardCharsets.UTF_8);

        byte[] body =
                new byte[
                        headerBytes.length
                                + imageBytes.length
                                + footerBytes.length
                ];

        System.arraycopy(
                headerBytes,
                0,
                body,
                0,
                headerBytes.length
        );

        System.arraycopy(
                imageBytes,
                0,
                body,
                headerBytes.length,
                imageBytes.length
        );

        System.arraycopy(
                footerBytes,
                0,
                body,
                headerBytes.length + imageBytes.length,
                footerBytes.length
        );

        return body;
    }
}