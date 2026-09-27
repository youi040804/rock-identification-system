# model.py

import torch.nn as nn
from torchvision import models

from config import NUM_CLASSES


def build_model():
    """
    重建 best_rock_model.pth 对应的 ResNet18 网络结构。
    推理阶段不需要下载 ImageNet 预训练权重，
    因为随后会加载已经训练好的完整 state_dict。
    """
    model = models.resnet18(weights=None)

    model.fc = nn.Sequential(
        nn.Linear(model.fc.in_features, 512),
        nn.ReLU(),
        nn.Dropout(0.5),
        nn.Linear(512, NUM_CLASSES),
    )

    return model