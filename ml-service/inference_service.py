# inference_service.py
#
# Machine-readable inference entry for Spring Boot.
#
# Contract:
#   stdin/argv : image path
#   stdout     : JSON only
#   stderr     : errors/debug information
#
# Usage:
#   python inference_service.py /path/to/image.jpg

from pathlib import Path
import json
import sys

import torch
from PIL import Image
from torchvision import transforms

from config import ROCK_NAMES
from model import build_model


BASE_DIR = Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "models" / "best_rock_model_v2.pth"

TOP_K = 3


inference_transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225],
    ),
])


def load_model(device):
    if not MODEL_PATH.exists():
        raise FileNotFoundError(
            f"Model not found: {MODEL_PATH}"
        )

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


def preprocess_image(image_path):
    image_path = Path(image_path)

    if not image_path.exists():
        raise FileNotFoundError(
            f"Image not found: {image_path}"
        )

    image = Image.open(image_path).convert("RGB")

    return inference_transform(image).unsqueeze(0)


def predict(model, image_tensor, device):
    image_tensor = image_tensor.to(device)

    with torch.no_grad():
        logits = model(image_tensor)
        probabilities = torch.softmax(logits, dim=1)

        top_probabilities, top_indices = torch.topk(
            probabilities,
            k=TOP_K,
            dim=1,
        )

    top_probabilities = top_probabilities[0].cpu().tolist()
    top_indices = top_indices[0].cpu().tolist()

    results = []

    for class_id, probability in zip(
        top_indices,
        top_probabilities,
    ):
        results.append({
            "classId": class_id,
            "name": ROCK_NAMES[class_id],
            "confidence": round(probability, 6),
        })

    return results


def main():
    if len(sys.argv) != 2:
        print(
            "Usage: python inference_service.py <image_path>",
            file=sys.stderr,
        )
        sys.exit(2)

    try:
        image_path = sys.argv[1]

        device = torch.device(
            "cuda" if torch.cuda.is_available() else "cpu"
        )

        model = load_model(device)

        image_tensor = preprocess_image(image_path)

        results = predict(
            model,
            image_tensor,
            device,
        )

        # stdout MUST contain JSON only.
        print(
            json.dumps(
                results,
                ensure_ascii=False,
            )
        )

    except Exception as exc:
        print(
            f"Inference failed: {exc}",
            file=sys.stderr,
        )
        sys.exit(1)


if __name__ == "__main__":
    main()
