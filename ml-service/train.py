# train.py

from pathlib import Path
import copy
import random
import time

import numpy as np
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

OUTPUT_MODEL_PATH = (
    BASE_DIR
    / "models"
    / "best_rock_model_v2.pth"
)

SEED = 42
VAL_RATIO = 0.20

BATCH_SIZE = 32
MAX_EPOCHS = 30
LEARNING_RATE = 1e-4
PATIENCE = 6


# ============================================================
# 2. 固定随机种子
# ============================================================

def set_seed(seed):
    random.seed(seed)
    np.random.seed(seed)
    torch.manual_seed(seed)

    if torch.cuda.is_available():
        torch.cuda.manual_seed_all(seed)


# ============================================================
# 3. 扫描数据集
# ============================================================

def collect_samples(data_dir):
    samples = []

    valid_suffixes = {".jpg", ".jpeg", ".png"}

    for path in Path(data_dir).iterdir():
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

        samples.append((path, label))

    # 固定基础顺序，保证后续划分可复现
    samples.sort(key=lambda x: x[0].name)

    return samples


# ============================================================
# 4. 按类别分层划分
# ============================================================

def stratified_split(samples, val_ratio, seed):
    samples_by_class = {
        class_id: []
        for class_id in range(NUM_CLASSES)
    }

    for sample in samples:
        path, label = sample
        samples_by_class[label].append(sample)

    rng = random.Random(seed)

    train_samples = []
    val_samples = []

    for class_id in range(NUM_CLASSES):
        class_samples = samples_by_class[class_id].copy()

        rng.shuffle(class_samples)

        class_size = len(class_samples)

        # 每类约 20% 进入 validation
        val_size = round(class_size * val_ratio)

        # 防止极端情况下某类完全没有 validation 样本
        val_size = max(1, val_size)

        val_samples.extend(class_samples[:val_size])
        train_samples.extend(class_samples[val_size:])

    # 再固定打乱一次，避免类别成块排列
    rng.shuffle(train_samples)
    rng.shuffle(val_samples)

    return train_samples, val_samples


# ============================================================
# 5. Dataset
# ============================================================

class RockDataset(Dataset):
    def __init__(self, samples, transform=None):
        self.samples = samples
        self.transform = transform

    def __len__(self):
        return len(self.samples)

    def __getitem__(self, index):
        path, label = self.samples[index]

        image = Image.open(path).convert("RGB")

        if self.transform:
            image = self.transform(image)

        return image, label


# ============================================================
# 6. Train / Validation Transform
# ============================================================

train_transform = transforms.Compose([
    transforms.RandomResizedCrop(
        224,
        scale=(0.8, 1.0),
    ),
    transforms.RandomHorizontalFlip(),
    transforms.RandomRotation(15),

    transforms.ToTensor(),

    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225],
    ),
])


val_transform = transforms.Compose([
    transforms.Resize((224, 224)),

    transforms.ToTensor(),

    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225],
    ),
])


# ============================================================
# 7. 模型
# ============================================================

def build_training_model():
    # 使用 ImageNet 预训练权重
    model = models.resnet18(
        weights=models.ResNet18_Weights.DEFAULT
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
# 8. 单个 Epoch
# ============================================================

def train_one_epoch(
    model,
    loader,
    criterion,
    optimizer,
    device,
):
    model.train()

    running_loss = 0.0
    correct = 0
    total = 0

    for images, labels in loader:
        images = images.to(device)
        labels = labels.to(device)

        optimizer.zero_grad()

        logits = model(images)

        loss = criterion(
            logits,
            labels,
        )

        loss.backward()
        optimizer.step()

        running_loss += (
            loss.item()
            * images.size(0)
        )

        predictions = torch.argmax(
            logits,
            dim=1,
        )

        correct += (
            predictions == labels
        ).sum().item()

        total += labels.size(0)

    epoch_loss = running_loss / total
    epoch_accuracy = correct / total

    return epoch_loss, epoch_accuracy


# ============================================================
# 9. Validation
# ============================================================

def validate(
    model,
    loader,
    criterion,
    device,
):
    model.eval()

    running_loss = 0.0
    correct = 0
    total = 0

    with torch.no_grad():
        for images, labels in loader:
            images = images.to(device)
            labels = labels.to(device)

            logits = model(images)

            loss = criterion(
                logits,
                labels,
            )

            running_loss += (
                loss.item()
                * images.size(0)
            )

            predictions = torch.argmax(
                logits,
                dim=1,
            )

            correct += (
                predictions == labels
            ).sum().item()

            total += labels.size(0)

    epoch_loss = running_loss / total
    epoch_accuracy = correct / total

    return epoch_loss, epoch_accuracy


# ============================================================
# 10. Main
# ============================================================

def main():
    set_seed(SEED)

    if not DATA_DIR.exists():
        raise FileNotFoundError(
            f"找不到数据集：{DATA_DIR}"
        )

    device = torch.device(
        "cuda"
        if torch.cuda.is_available()
        else "cpu"
    )

    print("=" * 70)
    print("Rock Classification Training")
    print("=" * 70)
    print(f"Device       : {device}")
    print(f"Random seed  : {SEED}")
    print(f"Max epochs   : {MAX_EPOCHS}")
    print(f"Batch size   : {BATCH_SIZE}")
    print(f"Learning rate: {LEARNING_RATE}")
    print(f"Patience     : {PATIENCE}")
    print("=" * 70)
    print()

    # --------------------------------------------------------
    # 数据
    # --------------------------------------------------------

    samples = collect_samples(DATA_DIR)

    train_samples, val_samples = stratified_split(
        samples,
        VAL_RATIO,
        SEED,
    )

    print("Dataset Split")
    print("=" * 70)
    print(f"Total : {len(samples)}")
    print(f"Train : {len(train_samples)}")
    print(f"Val   : {len(val_samples)}")
    print("=" * 70)
    print()

    # --------------------------------------------------------
    # 独立 Dataset
    #
    # 注意：
    # train_dataset 和 val_dataset 是两个不同对象，
    # 不再出现历史代码中共享 transform 的问题。
    # --------------------------------------------------------

    train_dataset = RockDataset(
        train_samples,
        transform=train_transform,
    )

    val_dataset = RockDataset(
        val_samples,
        transform=val_transform,
    )

    train_loader = DataLoader(
        train_dataset,
        batch_size=BATCH_SIZE,
        shuffle=True,
        num_workers=0,
    )

    val_loader = DataLoader(
        val_dataset,
        batch_size=BATCH_SIZE,
        shuffle=False,
        num_workers=0,
    )

    # --------------------------------------------------------
    # 模型
    # --------------------------------------------------------

    model = build_training_model()
    model.to(device)

    criterion = nn.CrossEntropyLoss()

    optimizer = torch.optim.Adam(
        model.parameters(),
        lr=LEARNING_RATE,
    )

    scheduler = torch.optim.lr_scheduler.ReduceLROnPlateau(
        optimizer,
        mode="min",
        factor=0.5,
        patience=2,
    )

    # --------------------------------------------------------
    # Early Stopping
    # --------------------------------------------------------

    best_val_loss = float("inf")
    best_val_accuracy = 0.0

    best_model_state = None

    epochs_without_improvement = 0

    training_start = time.perf_counter()

    # --------------------------------------------------------
    # Training Loop
    # --------------------------------------------------------

    for epoch in range(
        1,
        MAX_EPOCHS + 1,
    ):
        epoch_start = time.perf_counter()

        train_loss, train_accuracy = train_one_epoch(
            model,
            train_loader,
            criterion,
            optimizer,
            device,
        )

        val_loss, val_accuracy = validate(
            model,
            val_loader,
            criterion,
            device,
        )

        scheduler.step(val_loss)

        current_lr = optimizer.param_groups[0]["lr"]

        epoch_time = (
            time.perf_counter()
            - epoch_start
        )

        print(
            f"Epoch {epoch:02d}/{MAX_EPOCHS} | "
            f"Train Loss: {train_loss:.4f} | "
            f"Train Acc: {train_accuracy * 100:6.2f}% | "
            f"Val Loss: {val_loss:.4f} | "
            f"Val Acc: {val_accuracy * 100:6.2f}% | "
            f"LR: {current_lr:.2e} | "
            f"{epoch_time:.1f}s"
        )

        # ----------------------------------------------------
        # 按 Validation Loss 保存最佳模型
        # ----------------------------------------------------

        if val_loss < best_val_loss:
            best_val_loss = val_loss
            best_val_accuracy = val_accuracy

            best_model_state = copy.deepcopy(
                model.state_dict()
            )

            epochs_without_improvement = 0

            print(
                f"  -> Best checkpoint updated "
                f"(Val Loss: {best_val_loss:.4f}, "
                f"Val Acc: {best_val_accuracy * 100:.2f}%)"
            )

        else:
            epochs_without_improvement += 1

            print(
                f"  -> No improvement "
                f"({epochs_without_improvement}/{PATIENCE})"
            )

        if epochs_without_improvement >= PATIENCE:
            print()
            print(
                f"Early stopping triggered "
                f"at epoch {epoch}."
            )
            break

    # --------------------------------------------------------
    # 保存最佳 checkpoint
    # --------------------------------------------------------

    if best_model_state is None:
        raise RuntimeError(
            "训练过程中没有产生有效 checkpoint"
        )

    torch.save(
        best_model_state,
        OUTPUT_MODEL_PATH,
    )

    total_time = (
        time.perf_counter()
        - training_start
    )

    print()
    print("=" * 70)
    print("Training Finished")
    print("=" * 70)
    print(
        f"Best Val Loss : "
        f"{best_val_loss:.4f}"
    )
    print(
        f"Best Val Acc  : "
        f"{best_val_accuracy * 100:.2f}%"
    )
    print(
        f"Training Time : "
        f"{total_time / 60:.2f} min"
    )
    print(
        f"Saved Model   : "
        f"{OUTPUT_MODEL_PATH}"
    )
    print("=" * 70)


if __name__ == "__main__":
    main()
