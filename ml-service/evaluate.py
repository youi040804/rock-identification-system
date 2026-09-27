# evaluate.py

from pathlib import Path
from collections import Counter

import torch
from PIL import Image
from torch.utils.data import Dataset, DataLoader, random_split
from torchvision import transforms
from sklearn.metrics import (
    accuracy_score,
    precision_score,
    recall_score,
    f1_score,
)

from config import ROCK_NAMES, NUM_CLASSES
from model import build_model


# ============================================================
# 路径配置
# ============================================================

BASE_DIR = Path(__file__).resolve().parent

MODEL_PATH = BASE_DIR / "models" / "best_rock_model.pth"

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


# ============================================================
# 数据集
# ============================================================

class RockDataset(Dataset):
    def __init__(self, img_dir, transform=None):
        self.img_dir = Path(img_dir)
        self.transform = transform

        self.samples = []

        valid_suffixes = {".jpg", ".jpeg", ".png"}

        for path in self.img_dir.iterdir():
            if path.suffix.lower() not in valid_suffixes:
                continue

            try:
                label = int(path.stem.split("_")[0])
            except (ValueError, IndexError):
                print(f"[WARN] 无法解析标签，跳过：{path.name}")
                continue

            if not 0 <= label < NUM_CLASSES:
                print(f"[WARN] 标签超出范围，跳过：{path.name}")
                continue

            self.samples.append((path, label))

        # 保证每次运行时样本顺序一致
        self.samples.sort(key=lambda x: x[0].name)

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, index):
        path, label = self.samples[index]

        image = Image.open(path).convert("RGB")

        if self.transform:
            image = self.transform(image)

        return image, label


# ============================================================
# 验证集预处理
# 必须与 V3 验证阶段保持一致
# ============================================================

val_transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225],
    ),
])


# ============================================================
# 数据完整性检查
# ============================================================

def inspect_dataset(dataset):
    labels = [label for _, label in dataset.samples]
    counts = Counter(labels)

    print("=" * 60)
    print("Dataset Inspection")
    print("=" * 60)

    print(f"Dataset path : {DATA_DIR}")
    print(f"Total images : {len(dataset)}")
    print(f"Classes      : {NUM_CLASSES}")
    print()

    for class_id in range(NUM_CLASSES):
        print(
            f"[{class_id:02d}] "
            f"{ROCK_NAMES[class_id]:<6} : "
            f"{counts[class_id]} images"
        )

    print("=" * 60)

    expected_total = NUM_CLASSES * 60

    if len(dataset) == expected_total and all(
        counts[i] == 60 for i in range(NUM_CLASSES)
    ):
        print(
            f"[OK] 数据集完整："
            f"{NUM_CLASSES} 类 × 60 张 = {expected_total} 张"
        )
    else:
        print("[WARN] 数据集数量与预期不一致")

    print()


# ============================================================
# 模型加载
# ============================================================

def load_model(device):
    model = build_model()

    state_dict = torch.load(
        MODEL_PATH,
        map_location=device,
        weights_only=True,
    )

    model.load_state_dict(state_dict)
    model.to(device)
    model.eval()

    return model


# ============================================================
# 模型评估
# ============================================================

def evaluate(model, loader, device):
    y_true = []
    y_pred = []

    with torch.no_grad():
        for images, labels in loader:
            images = images.to(device)

            logits = model(images)
            predictions = torch.argmax(logits, dim=1)

            y_true.extend(labels.numpy().tolist())
            y_pred.extend(predictions.cpu().numpy().tolist())

    accuracy = accuracy_score(y_true, y_pred)

    precision = precision_score(
        y_true,
        y_pred,
        average="weighted",
        zero_division=0,
    )

    recall = recall_score(
        y_true,
        y_pred,
        average="weighted",
        zero_division=0,
    )

    f1 = f1_score(
        y_true,
        y_pred,
        average="weighted",
        zero_division=0,
    )

    return accuracy, precision, recall, f1

def diagnose_full_dataset(model, loader, device):
    class_correct = [0] * NUM_CLASSES
    class_total = [0] * NUM_CLASSES

    total_correct = 0
    total_samples = 0

    with torch.no_grad():
        for images, labels in loader:
            images = images.to(device)
            labels_device = labels.to(device)

            logits = model(images)
            predictions = torch.argmax(logits, dim=1)

            total_correct += (
                predictions == labels_device
            ).sum().item()

            total_samples += labels.size(0)

            predictions = predictions.cpu()

            for label, prediction in zip(labels, predictions):
                class_id = label.item()

                class_total[class_id] += 1

                if prediction.item() == class_id:
                    class_correct[class_id] += 1

    overall_accuracy = total_correct / total_samples

    print()
    print("Full Dataset Diagnostic")
    print("=" * 60)
    print(f"Total samples : {total_samples}")
    print(f"Correct       : {total_correct}")
    print(f"Accuracy      : {overall_accuracy * 100:.2f}%")
    print("=" * 60)

    print()
    print("Per-Class Accuracy")
    print("=" * 60)

    for class_id in range(NUM_CLASSES):
        total = class_total[class_id]
        correct = class_correct[class_id]

        accuracy = correct / total if total > 0 else 0.0

        print(
            f"[{class_id:02d}] "
            f"{ROCK_NAMES[class_id]:<6} "
            f"{correct:>2}/{total:<2} "
            f"{accuracy * 100:>6.2f}%"
        )

    print("=" * 60)


# ============================================================
# Main
# ============================================================

def main():
    if not DATA_DIR.exists():
        raise FileNotFoundError(
            f"找不到数据集目录：{DATA_DIR}"
        )

    if not MODEL_PATH.exists():
        raise FileNotFoundError(
            f"找不到模型：{MODEL_PATH}"
        )

    device = torch.device(
        "cuda" if torch.cuda.is_available() else "cpu"
    )

    print(f"Device: {device}\n")

    # --------------------------------------------------------
    # 1. 加载完整数据集
    # --------------------------------------------------------

    full_dataset = RockDataset(
        DATA_DIR,
        transform=val_transform,
    )

    inspect_dataset(full_dataset)

    # --------------------------------------------------------
    # 2. 尽可能复现历史 V3 的 80 / 20 split
    # --------------------------------------------------------

    train_size = int(0.8 * len(full_dataset))
    val_size = len(full_dataset) - train_size

    _, val_dataset = random_split(
        full_dataset,
        [train_size, val_size],
        generator=torch.Generator().manual_seed(42),
    )

    print("Validation Split")
    print("=" * 60)
    print(f"Train samples : {train_size}")
    print(f"Val samples   : {val_size}")
    print(f"Random seed   : 42")
    print("=" * 60)
    print()

    val_loader = DataLoader(
        val_dataset,
        batch_size=32,
        shuffle=False,
        num_workers=0,
    )

    # --------------------------------------------------------
    # 3. 加载模型
    # --------------------------------------------------------

    model = load_model(device)

    # --------------------------------------------------------
    # 4. 评估
    # --------------------------------------------------------

    accuracy, precision, recall, f1 = evaluate(
        model,
        val_loader,
        device,
    )

    print("Evaluation Results")
    print("=" * 60)
    print(f"Accuracy  : {accuracy * 100:.2f}%")
    print(f"Precision : {precision * 100:.2f}%")
    print(f"Recall    : {recall * 100:.2f}%")
    print(f"F1 Score  : {f1 * 100:.2f}%")
    print("=" * 60)


    # --------------------------------------------------------
    # 5. 完整数据集诊断
    # 注意：该结果不能作为独立测试集指标
    # --------------------------------------------------------

    full_loader = DataLoader(
        full_dataset,
        batch_size=32,
        shuffle=False,
        num_workers=0,
    )

    diagnose_full_dataset(
        model,
        full_loader,
        device,
    )
if __name__ == "__main__":
    main()
