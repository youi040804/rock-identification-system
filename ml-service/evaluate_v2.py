# train.py
#
# Experiment 2:
#   ImageNet pretrained ResNet18
#   Freeze conv1 / bn1 / layer1 / layer2 / layer3
#   Fine-tune layer4 + custom FC classifier
#   Differential learning rates:
#       layer4 = 1e-5
#       fc     = 1e-4
#   Adam + weight decay
#   Stratified 80/20 split
#   Early Stopping

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

from config import NUM_CLASSES


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
    / "best_rock_model_partial.pth"
)

SEED = 42
VAL_RATIO = 0.20

BATCH_SIZE = 32
MAX_EPOCHS = 30
PATIENCE = 6

# Differential Learning Rates
LAYER4_LR = 1e-5
FC_LR = 1e-4

WEIGHT_DECAY = 1e-4


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
            print(
                f"[WARN] 无法解析标签，跳过：{path.name}"
            )
            continue

        if not 0 <= label < NUM_CLASSES:
            print(
                f"[WARN] 标签超出范围，跳过：{path.name}"
            )
            continue

        samples.append((path, label))

    samples.sort(
        key=lambda x: x[0].name
    )

    return samples


# ============================================================
# 4. Stratified Split
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
        samples_by_class[label].append(sample)

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

    return train_samples, val_samples


# ============================================================
# 5. Dataset
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

        return image, label


# ============================================================
# 6. Transform
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
# 7. Model
# ============================================================

def build_training_model():
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
# 8. Partial Fine-tuning
# ============================================================

def configure_partial_finetuning(model):
    # 先冻结整个网络
    for param in model.parameters():
        param.requires_grad = False

    # 解冻 ResNet18 最后一组 residual blocks
    for param in model.layer4.parameters():
        param.requires_grad = True

    # 解冻自定义分类头
    for param in model.fc.parameters():
        param.requires_grad = True


# ============================================================
# 9. Train
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

    return (
        running_loss / total,
        correct / total,
    )


# ============================================================
# 10. Validation
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

    return (
        running_loss / total,
        correct / total,
    )


# ============================================================
# 11. Main
# ============================================================

def main():
    set_seed(SEED)

    if not DATA_DIR.exists():
        raise FileNotFoundError(
            f"找不到数据集：{DATA_DIR}"
        )

    OUTPUT_MODEL_PATH.parent.mkdir(
        parents=True,
        exist_ok=True,
    )

    device = torch.device(
        "cuda"
        if torch.cuda.is_available()
        else "cpu"
    )

    print("=" * 70)
    print(
        "Rock Classification Training "
        "- Experiment 2"
    )
    print("=" * 70)

    print(f"Device        : {device}")
    print(
        "Strategy      : "
        "Partial fine-tuning (layer4 + fc)"
    )
    print(f"Random seed   : {SEED}")
    print(f"Max epochs    : {MAX_EPOCHS}")
    print(f"Batch size    : {BATCH_SIZE}")
    print(f"Layer4 LR     : {LAYER4_LR}")
    print(f"FC LR         : {FC_LR}")
    print(f"Weight decay  : {WEIGHT_DECAY}")
    print(f"Patience      : {PATIENCE}")

    print("=" * 70)
    print()

    # --------------------------------------------------------
    # Dataset
    # --------------------------------------------------------

    samples = collect_samples(
        DATA_DIR
    )

    train_samples, val_samples = (
        stratified_split(
            samples,
            VAL_RATIO,
            SEED,
        )
    )

    print("Dataset Split")
    print("=" * 70)

    print(f"Total : {len(samples)}")
    print(f"Train : {len(train_samples)}")
    print(f"Val   : {len(val_samples)}")

    print("=" * 70)
    print()

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
    # Model
    # --------------------------------------------------------

    model = build_training_model()

    configure_partial_finetuning(
        model
    )

    model.to(device)

    # --------------------------------------------------------
    # 参数检查
    # --------------------------------------------------------

    total_params = sum(
        p.numel()
        for p in model.parameters()
    )

    trainable_params = sum(
        p.numel()
        for p in model.parameters()
        if p.requires_grad
    )

    frozen_params = (
        total_params
        - trainable_params
    )

    layer4_params = sum(
        p.numel()
        for p in model.layer4.parameters()
        if p.requires_grad
    )

    fc_params = sum(
        p.numel()
        for p in model.fc.parameters()
        if p.requires_grad
    )

    print("Model Parameters")
    print("=" * 70)

    print(
        f"Total parameters     : "
        f"{total_params:,}"
    )

    print(
        f"Trainable parameters : "
        f"{trainable_params:,}"
    )

    print(
        f"Frozen parameters    : "
        f"{frozen_params:,}"
    )

    print(
        f"Layer4 parameters    : "
        f"{layer4_params:,}"
    )

    print(
        f"FC parameters        : "
        f"{fc_params:,}"
    )

    print("=" * 70)
    print()

    # --------------------------------------------------------
    # Loss
    # --------------------------------------------------------

    criterion = nn.CrossEntropyLoss()

    # --------------------------------------------------------
    # Differential Learning Rates
    #
    # layer4:
    #   已经具有 ImageNet pretrained features
    #   → 小 LR，避免破坏已有表示
    #
    # fc:
    #   新初始化 classifier
    #   → 更大的 LR
    # --------------------------------------------------------

    optimizer = torch.optim.Adam(
        [
            {
                "params": model.layer4.parameters(),
                "lr": LAYER4_LR,
            },
            {
                "params": model.fc.parameters(),
                "lr": FC_LR,
            },
        ],
        weight_decay=WEIGHT_DECAY,
    )

    scheduler = (
        torch.optim.lr_scheduler.ReduceLROnPlateau(
            optimizer,
            mode="min",
            factor=0.5,
            patience=2,
        )
    )

    # --------------------------------------------------------
    # Early Stopping
    # --------------------------------------------------------

    best_val_loss = float("inf")
    best_val_accuracy = 0.0
    best_model_state = None
    best_epoch = 0

    epochs_without_improvement = 0

    training_start = (
        time.perf_counter()
    )

    # --------------------------------------------------------
    # Training Loop
    # --------------------------------------------------------

    for epoch in range(
        1,
        MAX_EPOCHS + 1,
    ):
        epoch_start = (
            time.perf_counter()
        )

        train_loss, train_accuracy = (
            train_one_epoch(
                model,
                train_loader,
                criterion,
                optimizer,
                device,
            )
        )

        val_loss, val_accuracy = (
            validate(
                model,
                val_loader,
                criterion,
                device,
            )
        )

        scheduler.step(
            val_loss
        )

        current_layer4_lr = (
            optimizer.param_groups[0]["lr"]
        )

        current_fc_lr = (
            optimizer.param_groups[1]["lr"]
        )

        epoch_time = (
            time.perf_counter()
            - epoch_start
        )

        print(
            f"Epoch {epoch:02d}/{MAX_EPOCHS} | "
            f"Train Loss: {train_loss:.4f} | "
            f"Train Acc: "
            f"{train_accuracy * 100:6.2f}% | "
            f"Val Loss: {val_loss:.4f} | "
            f"Val Acc: "
            f"{val_accuracy * 100:6.2f}% | "
            f"L4 LR: {current_layer4_lr:.2e} | "
            f"FC LR: {current_fc_lr:.2e} | "
            f"{epoch_time:.1f}s"
        )

        # ----------------------------------------------------
        # Best checkpoint by Validation Loss
        # ----------------------------------------------------

        if val_loss < best_val_loss:
            best_val_loss = val_loss
            best_val_accuracy = val_accuracy
            best_epoch = epoch

            best_model_state = (
                copy.deepcopy(
                    model.state_dict()
                )
            )

            epochs_without_improvement = 0

            print(
                "  -> Best checkpoint updated "
                f"(Epoch: {best_epoch}, "
                f"Val Loss: "
                f"{best_val_loss:.4f}, "
                f"Val Acc: "
                f"{best_val_accuracy * 100:.2f}%)"
            )

        else:
            epochs_without_improvement += 1

            print(
                "  -> No improvement "
                f"("
                f"{epochs_without_improvement}"
                f"/{PATIENCE}"
                f")"
            )

        if (
            epochs_without_improvement
            >= PATIENCE
        ):
            print()

            print(
                "Early stopping triggered "
                f"at epoch {epoch}."
            )

            break

    # --------------------------------------------------------
    # Save
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

    print(
        "Training Finished "
        "- Experiment 2"
    )

    print("=" * 70)

    print(
        f"Best Epoch    : "
        f"{best_epoch}"
    )

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
