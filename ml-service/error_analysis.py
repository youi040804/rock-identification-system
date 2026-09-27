# error_analysis.py
#
# Error Analysis for Experiment 0
#
# Best model:
#   models/best_rock_model_v2.pth
#
# Validation:
#   Same stratified 80/20 split
#   seed = 42
#
# Outputs:
#   1. Overall Accuracy
#   2. Per-class Accuracy
#   3. Most common prediction for each true class
#   4. Top confusion pairs
#   5. Misclassified image examples

from pathlib import Path
from collections import Counter, defaultdict
import random

import torch
import torch.nn as nn
from PIL import Image
from torch.utils.data import Dataset, DataLoader
from torchvision import models, transforms

from config import ROCK_NAMES, NUM_CLASSES


# ============================================================
# 1. 配置
# ============================================================

BASE_DIR = Path(__file__).resolve().parent

DATA_DIR = (
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
)

MODEL_PATH = (
    BASE_DIR
    / "models"
    / "best_rock_model_v2.pth"
)

SEED = 42
VAL_RATIO = 0.20
BATCH_SIZE = 32


# ============================================================
# 2. 数据扫描
# ============================================================

def collect_samples(data_dir):
    samples = []

    valid_suffixes = {
        ".jpg",
        ".jpeg",
        ".png",
    }

    for path in Path(data_dir).iterdir():
        if path.suffix.lower() not in valid_suffixes:
            continue

        try:
            label = int(
                path.stem.split("_")[0]
            )
        except (ValueError, IndexError):
            continue

        if 0 <= label < NUM_CLASSES:
            samples.append(
                (path, label)
            )

    samples.sort(
        key=lambda x: x[0].name
    )

    return samples


# ============================================================
# 3. 与训练完全一致的 Stratified Split
# ============================================================

def stratified_split(
    samples,
    val_ratio,
    seed,
):
    samples_by_class = {
        class_id: []
        for class_id in range(NUM_CLASSES)
    }

    for sample in samples:
        _, label = sample

        samples_by_class[label].append(
            sample
        )

    rng = random.Random(seed)

    train_samples = []
    val_samples = []

    for class_id in range(NUM_CLASSES):
        class_samples = (
            samples_by_class[class_id].copy()
        )

        rng.shuffle(class_samples)

        val_size = round(
            len(class_samples)
            * val_ratio
        )

        val_size = max(
            1,
            val_size,
        )

        val_samples.extend(
            class_samples[:val_size]
        )

        train_samples.extend(
            class_samples[val_size:]
        )

    rng.shuffle(train_samples)
    rng.shuffle(val_samples)

    return (
        train_samples,
        val_samples,
    )


# ============================================================
# 4. Dataset
#
# 除 image / label 外，把图片路径也返回，
# 这样错误分析时可以定位具体文件。
# ============================================================

class RockDataset(Dataset):
    def __init__(
        self,
        samples,
        transform=None,
    ):
        self.samples = samples
        self.transform = transform

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, index):
        path, label = self.samples[index]

        image = Image.open(
            path
        ).convert("RGB")

        if self.transform:
            image = self.transform(image)

        return (
            image,
            label,
            str(path),
        )


# ============================================================
# 5. Validation Transform
# ============================================================

val_transform = transforms.Compose([
    transforms.Resize((224, 224)),

    transforms.ToTensor(),

    transforms.Normalize(
        mean=[
            0.485,
            0.456,
            0.406,
        ],
        std=[
            0.229,
            0.224,
            0.225,
        ],
    ),
])


# ============================================================
# 6. Model
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
# 7. Main
# ============================================================

def main():

    device = torch.device(
        "cuda"
        if torch.cuda.is_available()
        else "cpu"
    )

    print("=" * 80)
    print("Rock Classification Error Analysis")
    print("=" * 80)

    print(
        f"Model  : {MODEL_PATH.name}"
    )

    print(
        f"Device : {device}"
    )

    print("=" * 80)
    print()

    # --------------------------------------------------------
    # Validation split
    # --------------------------------------------------------

    samples = collect_samples(
        DATA_DIR
    )

    _, val_samples = stratified_split(
        samples,
        VAL_RATIO,
        SEED,
    )

    print(
        f"Total dataset      : "
        f"{len(samples)}"
    )

    print(
        f"Validation samples : "
        f"{len(val_samples)}"
    )

    print(
        f"Random seed        : "
        f"{SEED}"
    )

    print()

    val_dataset = RockDataset(
        val_samples,
        transform=val_transform,
    )

    val_loader = DataLoader(
        val_dataset,
        batch_size=BATCH_SIZE,
        shuffle=False,
        num_workers=0,
    )

    # --------------------------------------------------------
    # Load checkpoint
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
    # 保存预测结果
    # --------------------------------------------------------

    results = []

    with torch.no_grad():

        for (
            images,
            labels,
            paths,
        ) in val_loader:

            images = images.to(device)

            logits = model(
                images
            )

            probabilities = torch.softmax(
                logits,
                dim=1,
            )

            confidence, predictions = (
                torch.max(
                    probabilities,
                    dim=1,
                )
            )

            for (
                true_label,
                pred_label,
                conf,
                path,
            ) in zip(
                labels.tolist(),
                predictions.cpu().tolist(),
                confidence.cpu().tolist(),
                paths,
            ):

                results.append({
                    "true": true_label,
                    "pred": pred_label,
                    "confidence": conf,
                    "path": Path(path),
                })

    # --------------------------------------------------------
    # Overall Accuracy
    # --------------------------------------------------------

    correct = sum(
        item["true"] == item["pred"]
        for item in results
    )

    accuracy = (
        correct / len(results)
    )

    print("=" * 80)
    print("Overall")
    print("=" * 80)

    print(
        f"Correct  : "
        f"{correct}/{len(results)}"
    )

    print(
        f"Accuracy : "
        f"{accuracy * 100:.2f}%"
    )

    print("=" * 80)
    print()

    # --------------------------------------------------------
    # Per-Class Analysis
    # --------------------------------------------------------

    class_results = defaultdict(
        list
    )

    for item in results:
        class_results[
            item["true"]
        ].append(item)

    print("=" * 80)
    print("Per-Class Error Analysis")
    print("=" * 80)

    for class_id in range(
        NUM_CLASSES
    ):

        items = class_results[
            class_id
        ]

        total = len(items)

        correct_count = sum(
            item["pred"] == class_id
            for item in items
        )

        class_accuracy = (
            correct_count / total
            if total > 0
            else 0.0
        )

        wrong_predictions = [
            item["pred"]
            for item in items
            if item["pred"] != class_id
        ]

        wrong_counter = Counter(
            wrong_predictions
        )

        print(
            f"[{class_id:02d}] "
            f"{ROCK_NAMES[class_id]}"
        )

        print(
            f"     Accuracy : "
            f"{correct_count}/{total} "
            f"({class_accuracy * 100:.2f}%)"
        )

        if wrong_counter:

            print(
                "     Most confused with:"
            )

            for (
                pred_id,
                count,
            ) in wrong_counter.most_common(
                3
            ):

                print(
                    f"       -> "
                    f"[{pred_id:02d}] "
                    f"{ROCK_NAMES[pred_id]} "
                    f": {count}"
                )

        else:
            print(
                "     No misclassification"
            )

        print()

    # --------------------------------------------------------
    # Global Confusion Pairs
    # --------------------------------------------------------

    confusion_counter = Counter()

    for item in results:

        if (
            item["true"]
            != item["pred"]
        ):
            confusion_counter[
                (
                    item["true"],
                    item["pred"],
                )
            ] += 1

    print("=" * 80)
    print("Top Confusion Pairs")
    print("=" * 80)

    for rank, (
        (true_id, pred_id),
        count,
    ) in enumerate(
        confusion_counter.most_common(20),
        start=1,
    ):

        print(
            f"{rank:02d}. "
            f"[{true_id:02d}] "
            f"{ROCK_NAMES[true_id]}"
            f"  ->  "
            f"[{pred_id:02d}] "
            f"{ROCK_NAMES[pred_id]}"
            f"  :  {count}"
        )

    print("=" * 80)
    print()

    # --------------------------------------------------------
    # High-Confidence Errors
    #
    # 这个尤其重要：
    # 模型不仅预测错，而且非常确信自己是对的。
    # --------------------------------------------------------

    wrong_results = [
        item
        for item in results
        if item["true"] != item["pred"]
    ]

    wrong_results.sort(
        key=lambda x: x["confidence"],
        reverse=True,
    )

    print("=" * 80)
    print("Top High-Confidence Errors")
    print("=" * 80)

    for rank, item in enumerate(
        wrong_results[:20],
        start=1,
    ):

        true_id = item["true"]
        pred_id = item["pred"]

        print(
            f"{rank:02d}. "
            f"{item['path'].name}"
        )

        print(
            f"    True : "
            f"[{true_id:02d}] "
            f"{ROCK_NAMES[true_id]}"
        )

        print(
            f"    Pred : "
            f"[{pred_id:02d}] "
            f"{ROCK_NAMES[pred_id]}"
        )

        print(
            f"    Confidence : "
            f"{item['confidence'] * 100:.2f}%"
        )

    print("=" * 80)


if __name__ == "__main__":
    main()
