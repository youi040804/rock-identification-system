# inference.py
#
# Final inference pipeline
#
# Model:
#   ResNet18 + custom FC
#   28-class rock classification
#
# Usage:
#   python inference.py <image_path>
#   python inference.py <image_path> --top-k 5
#
# Example:
#   python inference.py \
#   "../code/backend/dida_rock/src/main/resources/static/cnnpythonproject/rock_images/0_1.jpg"

from pathlib import Path
import argparse

import torch
import torch.nn as nn
from PIL import Image
from torchvision import models, transforms

from config import ROCK_NAMES, NUM_CLASSES


# ============================================================
# 1. 路径
# ============================================================

BASE_DIR = Path(__file__).resolve().parent

MODEL_PATH = (
    BASE_DIR
    / "models"
    / "best_rock_model_v2.pth"
)


# ============================================================
# 2. 图像预处理
#
# 必须与 validation / training inference 保持一致
# ============================================================

inference_transform = transforms.Compose([
    transforms.Resize((224, 224)),

    transforms.ToTensor(),

    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225],
    ),
])


# ============================================================
# 3. Model Architecture
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
# 4. Load Model
# ============================================================

def load_model(device):
    if not MODEL_PATH.exists():
        raise FileNotFoundError(
            f"找不到模型：{MODEL_PATH}"
        )

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

    return model


# ============================================================
# 5. Image Preprocessing
# ============================================================

def preprocess_image(image_path):
    image_path = Path(image_path)

    if not image_path.exists():
        raise FileNotFoundError(
            f"找不到图片：{image_path}"
        )

    image = Image.open(
        image_path
    ).convert("RGB")

    tensor = inference_transform(
        image
    )

    # C × H × W
    # →
    # 1 × C × H × W
    tensor = tensor.unsqueeze(0)

    return tensor


# ============================================================
# 6. Prediction
# ============================================================

def predict(
    model,
    image_tensor,
    device,
    top_k=3,
):
    image_tensor = image_tensor.to(
        device
    )

    top_k = min(
        top_k,
        NUM_CLASSES,
    )

    with torch.no_grad():
        logits = model(
            image_tensor
        )

        probabilities = torch.softmax(
            logits,
            dim=1,
        )

        top_probabilities, top_indices = (
            torch.topk(
                probabilities,
                k=top_k,
                dim=1,
            )
        )

    top_probabilities = (
        top_probabilities[0]
        .cpu()
        .tolist()
    )

    top_indices = (
        top_indices[0]
        .cpu()
        .tolist()
    )

    results = []

    for rank, (
        class_id,
        probability,
    ) in enumerate(
        zip(
            top_indices,
            top_probabilities,
        ),
        start=1,
    ):
        results.append({
            "rank": rank,
            "class_id": class_id,
            "name": ROCK_NAMES[class_id],
            "confidence": probability,
        })

    return results


# ============================================================
# 7. CLI
# ============================================================

def main():
    parser = argparse.ArgumentParser(
        description=(
            "28-class rock image "
            "classification inference"
        )
    )

    parser.add_argument(
        "image",
        type=str,
        help="Path to input rock image",
    )

    parser.add_argument(
        "--top-k",
        type=int,
        default=3,
        help="Number of predictions to return",
    )

    args = parser.parse_args()

    if args.top_k <= 0:
        raise ValueError(
            "--top-k 必须大于 0"
        )

    device = torch.device(
        "cuda"
        if torch.cuda.is_available()
        else "cpu"
    )

    model = load_model(
        device
    )

    image_tensor = preprocess_image(
        args.image
    )

    results = predict(
        model,
        image_tensor,
        device,
        top_k=args.top_k,
    )

    print("=" * 70)
    print("Rock Classification Inference")
    print("=" * 70)

    print(
        f"Model  : {MODEL_PATH.name}"
    )

    print(
        f"Device : {device}"
    )

    print(
        f"Image  : {Path(args.image).name}"
    )

    print("=" * 70)
    print()

    print(
        f"Top-{len(results)} Prediction"
    )

    print("-" * 70)

    for item in results:
        print(
            f"Top-{item['rank']}: "
            f"[{item['class_id']:02d}] "
            f"{item['name']} | "
            f"{item['confidence'] * 100:.2f}%"
        )

    print("-" * 70)


if __name__ == "__main__":
    main()
