# 岩石智能识别系统

基于 **Android + Spring Boot + FastAPI + PyTorch** 的岩石图像识别项目。

Android 客户端负责用户登录、图片上传和识别结果展示；Spring Boot 负责 JWT 鉴权、数据库访问和业务接口；模型部分使用 PyTorch ResNet18，并通过 FastAPI 提供独立的推理服务。

目前模型支持 **28 类岩石图像分类**。

---

## 1. 系统结构

```text
Android Client
      │
      │ HTTP / Multipart
      ▼
Spring Boot :8081
      │
      │ JWT 鉴权
      │ Java HttpClient
      ▼
FastAPI :8000
      │
      │ 图像预处理
      ▼
PyTorch ResNet18
      │
      ▼
Top-K 识别结果
classId / name / confidence
```

项目目录：

```text
rock-identification-system/
│
├── android/                     # Android 客户端
│   ├── app/
│   ├── gradle/
│   ├── gradlew
│   └── build.gradle
│
├── backend/                     # Spring Boot 后端
│   ├── src/
│   ├── gradle/
│   ├── gradlew
│   └── build.gradle
│
├── ml-service/                  # 模型训练与推理
│   ├── models/
│   │   └── best_rock_model_v2.pth
│   ├── model.py                 # ResNet18 模型定义
│   ├── train.py                 # 模型训练
│   ├── evaluate_v2.py           # 模型评估
│   ├── error_analysis.py        # 误差分析
│   ├── inference.py             # 本地推理
│   ├── inference_api.py         # FastAPI 推理接口
│   ├── benchmark.py             # 推理耗时测试
│   └── requirements.txt
│
├── .gitignore
└── README.md
```

---

## 2. 技术栈

| 模块           | 技术                                      |
| -------------- | ----------------------------------------- |
| Android 客户端 | Java、Android SDK、Retrofit、OkHttp       |
| 后端           | Java 17、Spring Boot、JWT、MyBatis、MySQL |
| 模型服务       | Python、FastAPI、Uvicorn                  |
| 模型           | PyTorch、torchvision、ResNet18            |
| 模型评估       | scikit-learn                              |
| 项目构建       | Gradle                                    |
| 服务通信       | HTTP、REST、JSON、Multipart/Form-Data     |

---

## 3. 主要功能

### Android 客户端

- 用户注册、登录
- JWT 登录状态管理
- 岩石图片上传
- 识别结果展示
- 用户资料相关功能

### Spring Boot 后端

- 用户注册、登录接口
- JWT 鉴权
- MySQL 数据访问
- 岩石识别接口
- 用户资料及反馈相关接口
- Spring Boot → FastAPI 模型调用

### 模型服务

- ResNet18 训练
- 28 类岩石分类
- Top-K 推理
- Accuracy / Precision / Recall / F1 评估
- 类别混淆及错误样本分析
- FastAPI `/predict` 推理接口
- CPU 推理耗时测试

---

## 4. 模型训练

### 4.1 数据集

当前实验使用的数据集共包含：

```text
类别数：28
图片数：1581
```

训练时按照类别进行分层划分，并使用固定随机种子：

```text
训练集：1265 张
验证集： 316 张
总计：  1581 张
```

训练数据集未提交到仓库。

运行 `train.py` 前，可以通过环境变量 `ROCK_DATASET_DIR` 指定本地数据集目录：

```bash
export ROCK_DATASET_DIR=/path/to/rock_images
python train.py
```

如果未设置 `ROCK_DATASET_DIR`，默认读取：

```text
ml-service/data/rock_images/
```

数据集图片文件名需要包含类别编号，训练脚本根据文件名中 `_` 之前的数字解析类别标签。

### 4.2 模型

模型使用 ImageNet 预训练的 ResNet18，并将分类部分修改为 28 类输出。

训练过程中使用：

- ImageNet 预训练权重
- 图像归一化
- 数据增强
- Adam
- Learning Rate Scheduler
- Early Stopping
- 全网络 Fine-tuning

最终使用的 checkpoint：

```text
ml-service/models/best_rock_model_v2.pth
```

### 4.3 验证结果

固定分层验证集上的结果：

| 指标               |   结果 |
| ------------------ | -----: |
| Accuracy           | 46.84% |
| Weighted Precision | 47.55% |
| Weighted Recall    | 46.84% |
| Weighted F1        | 45.97% |

以上指标来自固定验证集，**不是独立测试集结果**。

当前数据集只有 1581 张图片，同时包含 28 个类别。误差分析中可以观察到部分类别之间存在混淆，也存在高置信度错误预测。

---

## 5. 模型服务重构

项目原版本由 Spring Boot 使用 `ProcessBuilder` 调用 Python：

```text
Spring Boot
      ↓
ProcessBuilder
      ↓
启动 Python
      ↓
加载模型
      ↓
图片推理
      ↓
返回结果
```

这种方式会在请求过程中启动 Python 进程并加载模型。

当前版本将模型推理部分改为独立的 FastAPI 服务：

```text
Spring Boot
      ↓
HTTP
      ↓
FastAPI
      ↓
已加载的 PyTorch 模型
      ↓
Top-K JSON
```

FastAPI 在服务启动时加载 checkpoint，后续 `/predict` 请求复用已经加载的模型进行推理。

Spring Boot 使用 Java `HttpClient` 请求 FastAPI，不再通过 `ProcessBuilder` 为每次识别启动 Python 推理脚本。

---

## 6. FastAPI 推理接口

### 6.1 健康检查

```http
GET /health
```

正常返回：

```json
{
  "status": "ok",
  "modelLoaded": true
}
```

`modelLoaded` 表示模型是否已经完成加载。

### 6.2 图片预测

```http
POST /predict
Content-Type: multipart/form-data
```

请求字段：

```text
image
```

使用一张岩石图片进行实际测试时返回：

```json
[
  {
    "classId": 0,
    "name": "砾岩",
    "confidence": 0.977696
  },
  {
    "classId": 17,
    "name": "大理岩",
    "confidence": 0.006803
  },
  {
    "classId": 9,
    "name": "碳酸盐岩",
    "confidence": 0.005868
  }
]
```

这里的 `confidence` 是模型对当前样本相应类别的预测置信度，不表示模型整体准确率。

---

## 7. Spring Boot 识别接口

Android 客户端通过 Spring Boot 的 `/rock/recognize` 接口进行岩石识别：

```http
POST /rock/recognize
Authorization: Bearer <JWT>
Content-Type: multipart/form-data
```

接口目前保留以下字段：

```text
image
hardness
composition
texture
color
```

其中 `hardness`、`composition`、`texture`、`color` 是原 Android 接口保留的兼容字段。

当前 ResNet18 仅使用图片作为模型输入，上述四个属性没有参与当前模型计算。

请求链路：

```text
Android
   ↓
Spring Boot /rock/recognize
   ↓
JWT 校验
   ↓
MultipartFile
   ↓
临时图片
   ↓
Java HttpClient
   ↓
FastAPI /predict
   ↓
ResNet18
   ↓
Top-K
   ↓
Spring Boot ApiResponse
```

一次实际端到端测试返回：

```json
{
  "code": 200,
  "message": "图片识别成功",
  "data": [
    {
      "classId": 0,
      "confidence": 0.977696,
      "name": "砾岩"
    },
    {
      "classId": 17,
      "confidence": 0.006803,
      "name": "大理岩"
    },
    {
      "classId": 9,
      "confidence": 0.005868,
      "name": "碳酸盐岩"
    }
  ]
}
```

---

## 8. 性能测试

### 8.1 模型 Forward

CPU 环境下进行模型 Forward 测试：

```text
Batch Size：1
输入尺寸：3 × 224 × 224
Warm-up：10 次
测试次数：100 次
```

结果：

| 指标    |     耗时 |
| ------- | -------: |
| Average | 23.33 ms |
| P50     | 23.28 ms |
| P95     | 26.33 ms |

这里统计的是模型 Forward 时间，不包含图片磁盘 I/O、图片解码、图像预处理和 HTTP 通信。

### 8.2 `/rock/recognize` 本地端到端测试

对 Spring Boot `/rock/recognize` 进行了 1000 次本地串行请求：

```text
请求数：1000
成功数：1000
```

结果：

| 指标    |      耗时 |
| ------- | --------: |
| Average |  94.71 ms |
| P50     |  86.28 ms |
| P95     | 139.22 ms |
| P99     | 167.19 ms |

该测试覆盖：

```text
JWT
→ Multipart 请求处理
→ Spring Boot
→ FastAPI HTTP 请求
→ 图片解码与预处理
→ PyTorch 推理
→ Top-K
→ JSON 返回
```

该测试为本机串行请求，不包含 Android 客户端耗时和公网网络延迟，也不是并发压测。

---

## 9. 本地运行

项目完整识别链路需要依次启动：

```text
MySQL
  ↓
FastAPI
  ↓
Spring Boot
  ↓
Android
```

### 9.1 FastAPI

进入模型服务目录：

```bash
cd ml-service
```

创建 Python 虚拟环境：

```bash
python -m venv .venv
source .venv/bin/activate
```

安装依赖：

```bash
pip install -r requirements.txt
```

启动服务：

```bash
python -m uvicorn inference_api:app \
  --host 127.0.0.1 \
  --port 8000
```

检查服务：

```bash
curl http://127.0.0.1:8000/health
```

正常返回：

```json
{
  "status": "ok",
  "modelLoaded": true
}
```

### 9.2 Spring Boot

后端使用以下环境变量：

| 变量               | 说明                       |
| ------------------ | -------------------------- |
| `DB_URL`           | MySQL JDBC 地址            |
| `DB_USERNAME`      | MySQL 用户名               |
| `DB_PASSWORD`      | MySQL 密码                 |
| `MAIL_USERNAME`    | SMTP 用户名                |
| `MAIL_PASSWORD`    | SMTP 凭据                  |
| `JWT_SECRET`       | JWT 签名密钥，至少 32 字节 |
| `INFERENCE_URL`    | FastAPI `/predict` 地址    |
| `USER_AVATAR_PATH` | 用户头像保存目录           |

`application.properties` 中提供了以下默认值：

```text
DB_URL=jdbc:mysql://localhost:3306/didarock
INFERENCE_URL=http://127.0.0.1:8000/predict
USER_AVATAR_PATH=useravatar/
```

数据库用户名、数据库密码、邮件服务凭据和 JWT 签名密钥需要通过环境变量提供。

例如：

```bash
export DB_USERNAME=<your-db-username>
export DB_PASSWORD=<your-db-password>
export MAIL_USERNAME=<your-mail-username>
export MAIL_PASSWORD=<your-mail-credential>
export JWT_SECRET=<your-jwt-secret>
```

不要将真实凭据提交到仓库。

启动后端：

```bash
cd backend
./gradlew bootRun
```

默认端口：

```text
8081
```

### 9.3 Android

使用 Android Studio 打开：

```text
android/
```

当前开发环境的后端地址为：

```text
http://10.0.2.2:8081/
```

Android Emulator 使用 `10.0.2.2` 访问宿主机。

如果使用真机，需要修改：

```text
android/app/src/main/java/com/itgu/rock/tools/ApiConfig.java
```

中的 `BASE_URL`，使其指向手机能够访问的后端地址。

---

## 10. 模型训练

如果需要重新训练模型，首先准备本地数据集，然后进入：

```bash
cd ml-service
```

指定数据集目录：

```bash
export ROCK_DATASET_DIR=/path/to/rock_images
```

运行：

```bash
python train.py
```

如果没有设置 `ROCK_DATASET_DIR`，训练脚本默认读取：

```text
ml-service/data/rock_images/
```

训练完成后，最佳 checkpoint 保存到：

```text
ml-service/models/best_rock_model_v2.pth
```

数据集本身不包含在当前仓库中。

---

## 11. 构建与运行验证

当前版本已完成以下验证：

```text
Android
└── assembleDebug
    └── BUILD SUCCESSFUL

Spring Boot
└── clean test
    └── BUILD SUCCESSFUL

FastAPI
├── 服务启动
├── checkpoint 加载
├── GET /health → 200
└── POST /predict → Top-K

Spring Boot → FastAPI
└── POST /rock/recognize
    ├── JWT 校验
    ├── Spring Boot 接收图片
    ├── FastAPI /predict
    ├── PyTorch 推理
    └── HTTP 200 + Top-K
```

---

## 12. 配置与仓库说明

数据库密码、邮件服务凭据和 JWT 签名密钥等敏感配置没有写入当前仓库，由环境变量提供。

仓库根目录 `.gitignore` 排除了：

```text
.env
本地配置文件
Android local.properties
Gradle 构建目录
Python 虚拟环境
Python 缓存
运行时上传文件
训练数据集
实验 checkpoint
```

训练数据集不提交到仓库。

模型目录中的实验 checkpoint 默认忽略，当前推理服务实际使用的：

```text
ml-service/models/best_rock_model_v2.pth
```

作为最终 checkpoint 保留在仓库中。

---

## 13. 已知限制

- 当前数据集包含 1581 张图片和 28 个类别。
- 当前模型指标来自固定分层验证集，没有单独的测试集。
- 数据集未随仓库发布，重新训练需要自行指定本地数据集目录。
- 误差分析中存在类别混淆和高置信度错误预测。
- 当前性能数据来自本地运行环境。
- 1000 次接口测试采用串行请求，没有进行并发压测。
- Android 当前默认使用 Emulator 的宿主机地址，真机运行需要修改 `BASE_URL`。
- 当前模型仅使用图像作为输入，接口中保留的硬度、成分、纹理和颜色字段没有参与当前模型计算。