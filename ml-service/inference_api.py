import io
from contextlib import asynccontextmanager

import torch
from fastapi import FastAPI, File, HTTPException, UploadFile
from PIL import Image
from torchvision import transforms

from config import ROCK_NAMES
from model import build_model


DEVICE = torch.device("cpu")

BASE_DIR = __import__("pathlib").Path(__file__).resolve().parent
MODEL_PATH = BASE_DIR / "models" / "best_rock_model_v2.pth"

TOP_K = 3


transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(
        mean=[0.485, 0.456, 0.406],
        std=[0.229, 0.224, 0.225],
    ),
])


model = None


@asynccontextmanager
async def lifespan(app: FastAPI):
    """
    服务启动时加载模型一次。
    后续所有 /predict 请求复用同一个模型。
    """
    global model

    print(f"Loading model: {MODEL_PATH}")

    model = build_model()

    checkpoint = torch.load(
        MODEL_PATH,
        map_location=DEVICE,
        weights_only=True,
    )

    model.load_state_dict(checkpoint)
    model.to(DEVICE)
    model.eval()

    print("Model loaded successfully.")

    yield

    model = None


app = FastAPI(
    title="Rock Recognition Inference Service",
    version="1.0.0",
    lifespan=lifespan,
)


@app.get("/health")
def health():
    return {
        "status": "ok",
        "modelLoaded": model is not None,
    }


@app.post("/predict")
async def predict(image: UploadFile = File(...)):
    if model is None:
        raise HTTPException(
            status_code=503,
            detail="Model is not loaded.",
        )

    try:
        image_bytes = await image.read()
        pil_image = Image.open(io.BytesIO(image_bytes)).convert("RGB")

        tensor = transform(pil_image).unsqueeze(0).to(DEVICE)

        with torch.inference_mode():
            logits = model(tensor)
            probabilities = torch.softmax(logits, dim=1)[0]

        top_probs, top_indices = torch.topk(
            probabilities,
            k=min(TOP_K, len(ROCK_NAMES)),
        )

        results = []

        for prob, index in zip(top_probs.tolist(), top_indices.tolist()):
            results.append({
                "classId": index,
                "name": ROCK_NAMES[index],
                "confidence": round(prob, 6),
            })

        return results

    except Exception as exc:
        raise HTTPException(
            status_code=400,
            detail=f"Inference failed: {exc}",
        ) from exc
