# benchmark.py
#
# Final CPU inference benchmark
#
# Measures:
#   model forward latency only
#
# Excludes:
#   disk I/O
#   PIL decoding
#   Resize / Normalize preprocessing
#
# Input:
#   batch size = 1
#   3 × 224 × 224
#
# Model:
#   best_rock_model_v2.pth

from pathlib import Path
import time
import statistics

import torch
import torch.nn as nn
from PIL import Image
from torchvision import models, transforms

from config import NUM_CLASSES


# ============================================================
# 1. 配置
# ============================================================

BASE_DIR = Path(__file__).resolve().parent

MODEL_PATH = (
    BASE_DIR
    / "models"
    / "best_rock_model_v2.pth"
)

IMAGE_PATH = (
    BASE_DIR.parent
    / "code"
    / "backend"
    / "dida_rock"
    / "src"
    / "main"
    / "resources"
    / "static"
    / "cnnpythonproject"
    / "rock_images"
    / "0_1.jpg"
)

WARMUP_RUNS = 10
BENCHMARK_RUNS = 100


# ============================================================
# 2. Transform
# ============================================================

transform = transforms.Compose([
    transforms.Resize((224, 224)),

    transforms.ToTensor(),

    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225],
    ),
])


# ============================================================
# 3. Model
# ============================================================

def build_model():
    model = models.resnet18(
        weights=None
    )

    model.fc = nn.Sequential(
        nn.Linear(
            model.fc.in_features,
            512,
        ),
        nn.ReLU(),
        nn.Dropout(0.5),
        nn.Linear(
            512,
            NUM_CLASSES,
        ),
    )

    return model


# ============================================================
# 4. Percentile
# ============================================================

def percentile(
    values,
    percentile_value,
):
    values = sorted(values)

    if not values:
        raise ValueError(
            "values 不能为空"
        )

    index = (
        percentile_value
        / 100
        * (len(values) - 1)
    )

    lower_index = int(index)
    upper_index = min(
        lower_index + 1,
        len(values) - 1,
    )

    fraction = (
        index - lower_index
    )

    return (
        values[lower_index]
        * (1 - fraction)
        +
        values[upper_index]
        * fraction
    )


# ============================================================
# 5. Main
# ============================================================

def main():
    if not MODEL_PATH.exists():
        raise FileNotFoundError(
            f"找不到模型：{MODEL_PATH}"
        )

    if not IMAGE_PATH.exists():
        raise FileNotFoundError(
            f"找不到测试图片：{IMAGE_PATH}"
        )

    device = torch.device(
        "cuda"
        if torch.cuda.is_available()
        else "cpu"
    )

    # --------------------------------------------------------
    # Load model
    # --------------------------------------------------------

    model = build_model()

    state_dict = torch.load(
        MODEL_PATH,
        map_location=device,
        weights_only=True,
    )

    model.load_state_dict(
        state_dict
    )

    model.to(device)
    model.eval()

    # --------------------------------------------------------
    # Preprocess ONCE
    #
    # Benchmark 不包含图片读取和预处理
    # --------------------------------------------------------

    image = Image.open(
        IMAGE_PATH
    ).convert("RGB")

    image_tensor = (
        transform(image)
        .unsqueeze(0)
        .to(device)
    )

    # --------------------------------------------------------
    # Warm-up
    # --------------------------------------------------------

    with torch.no_grad():
        for _ in range(
            WARMUP_RUNS
        ):
            _ = model(
                image_tensor
            )

    # CUDA 情况下需要同步
    if device.type == "cuda":
        torch.cuda.synchronize()

    # --------------------------------------------------------
    # Benchmark
    # --------------------------------------------------------

    latencies_ms = []

    with torch.no_grad():

        for _ in range(
            BENCHMARK_RUNS
        ):

            if device.type == "cuda":
                torch.cuda.synchronize()

            start = time.perf_counter()

            _ = model(
                image_tensor
            )

            if device.type == "cuda":
                torch.cuda.synchronize()

            end = time.perf_counter()

            latency_ms = (
                (end - start)
                * 1000
            )

            latencies_ms.append(
                latency_ms
            )

    # --------------------------------------------------------
    # Statistics
    # --------------------------------------------------------

    average_latency = (
        statistics.mean(
            latencies_ms
        )
    )

    p50_latency = percentile(
        latencies_ms,
        50,
    )

    p95_latency = percentile(
        latencies_ms,
        95,
    )

    min_latency = min(
        latencies_ms
    )

    max_latency = max(
        latencies_ms
    )

    # --------------------------------------------------------
    # Output
    # --------------------------------------------------------

    print("=" * 70)
    print("Final Inference Benchmark")
    print("=" * 70)

    print(
        f"Model          : "
        f"{MODEL_PATH.name}"
    )

    print(
        f"Device         : "
        f"{device}"
    )

    print(
        f"Image          : "
        f"{IMAGE_PATH.name}"
    )

    print(
        "Input size     : "
        "224 x 224"
    )

    print(
        "Batch size     : "
        "1"
    )

    print(
        f"Warm-up runs   : "
        f"{WARMUP_RUNS}"
    )

    print(
        f"Benchmark runs : "
        f"{BENCHMARK_RUNS}"
    )

    print(
        "Scope          : "
        "model forward only"
    )

    print("=" * 70)

    print(
        f"Average latency : "
        f"{average_latency:.2f} ms"
    )

    print(
        f"P50 latency     : "
        f"{p50_latency:.2f} ms"
    )

    print(
        f"P95 latency     : "
        f"{p95_latency:.2f} ms"
    )

    print(
        f"Min latency     : "
        f"{min_latency:.2f} ms"
    )

    print(
        f"Max latency     : "
        f"{max_latency:.2f} ms"
    )

    print("=" * 70)


if __name__ == "__main__":
    main()
