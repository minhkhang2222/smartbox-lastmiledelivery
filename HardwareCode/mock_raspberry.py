"""Mô phỏng Raspberry Pi gửi face embedding tới SmartBox API.

Ví dụ:
    python mock_raspberry.py
    python mock_raspberry.py --embedding-file embedding.json

File embedding có thể là một JSON array 512 số, hoặc object:
    {"embedding": [0.1, ...]}
"""

from __future__ import annotations

import argparse
import json
import math
import os
import random
import sys
from pathlib import Path
from typing import Any
from urllib.error import HTTPError, URLError
from urllib.request import Request, urlopen


DEFAULT_API_URL = "https://api.smartboxeiu.site/api/face-auth/unlock"
EMBEDDING_SIZE = 512
DEVICE_TYPE = "RASPBERRY"
DEVICE_ID = "b3e80147-a2f4-4d77-8b90-18e6d522d202"
DEFAULT_USER_AGENT = (
    "Mozilla/5.0 (X11; Linux aarch64) AppleWebKit/537.36 "
    "(KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36 SmartBox-Raspberry/1.0"
)


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Giả lập Raspberry Pi xác thực khuôn mặt")
    parser.add_argument(
        "--url",
        default=os.getenv("SMARTBOX_FACE_AUTH_URL", DEFAULT_API_URL),
        help="Endpoint face-auth",
    )
    parser.add_argument(
        "--embedding-file",
        type=Path,
        help="File JSON chứa embedding thật để test trường hợp nhận diện thành công",
    )
    parser.add_argument(
        "--seed",
        type=int,
        default=2026,
        help="Seed tạo vector giả ổn định (mặc định: 2026)",
    )
    parser.add_argument(
        "--timeout",
        type=float,
        default=15.0,
        help="HTTP timeout tính bằng giây",
    )
    parser.add_argument(
        "--user-agent",
        default=os.getenv("SMARTBOX_USER_AGENT", DEFAULT_USER_AGENT),
        help="User-Agent gửi qua Cloudflare",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Chỉ in payload tóm tắt, không gọi API",
    )
    return parser.parse_args()


def validate_embedding(values: Any) -> list[float]:
    if not isinstance(values, list) or len(values) != EMBEDDING_SIZE:
        raise ValueError(f"embedding phải là JSON array có đúng {EMBEDDING_SIZE} phần tử")

    embedding = [float(value) for value in values]
    if not all(math.isfinite(value) for value in embedding):
        raise ValueError("embedding không được chứa NaN hoặc Infinity")
    return embedding


def load_embedding(path: Path) -> list[float]:
    with path.open("r", encoding="utf-8") as file:
        data = json.load(file)
    if isinstance(data, dict):
        data = data.get("embedding")
    return validate_embedding(data)


def create_fake_embedding(seed: int) -> list[float]:
    generator = random.Random(seed)
    values = [generator.uniform(-1.0, 1.0) for _ in range(EMBEDDING_SIZE)]
    norm = math.sqrt(sum(value * value for value in values))
    return [value / norm for value in values]


def print_payload_summary(payload: dict[str, Any]) -> None:
    embedding = payload["embedding"]
    print("Payload:")
    print(f"  deviceId: {payload['deviceId']}")
    print(f"  deviceType: {payload['deviceType']}")
    print(f"  embedding length: {len(embedding)}")
    print(f"  embedding preview: {embedding[:5]} ...")


def post_json(
    url: str,
    payload: dict[str, Any],
    timeout: float,
    user_agent: str,
) -> tuple[int, Any]:
    body = json.dumps(payload).encode("utf-8")
    request = Request(
        url,
        data=body,
        headers={
            "Content-Type": "application/json",
            "Accept": "application/json",
            "User-Agent": user_agent,
            "X-SmartBox-Client": "raspberry-mock/1.0",
        },
        method="POST",
    )

    try:
        with urlopen(request, timeout=timeout) as response:
            response_body = response.read().decode("utf-8")
            return response.status, parse_response(response_body)
    except HTTPError as error:
        response_body = error.read().decode("utf-8", errors="replace")
        return error.code, parse_response(response_body)


def parse_response(body: str) -> Any:
    try:
        return json.loads(body)
    except json.JSONDecodeError:
        return body


def main() -> int:
    # Giúp thông báo tiếng Việt hiển thị đúng trên Windows và Raspberry Pi.
    if hasattr(sys.stdout, "reconfigure"):
        sys.stdout.reconfigure(encoding="utf-8", errors="replace")
        sys.stderr.reconfigure(encoding="utf-8", errors="replace")

    args = parse_args()
    try:
        embedding = (
            load_embedding(args.embedding_file)
            if args.embedding_file
            else create_fake_embedding(args.seed)
        )
        payload = {
            "deviceId": DEVICE_ID,
            "deviceType": DEVICE_TYPE,
            "embedding": embedding,
        }
        print_payload_summary(payload)

        if args.dry_run:
            print("Dry-run: chưa gửi request.")
            return 0

        print(f"POST {args.url}")
        status, response = post_json(args.url, payload, args.timeout, args.user_agent)
        print(f"HTTP {status}")
        print(json.dumps(response, ensure_ascii=False, indent=2) if not isinstance(response, str) else response)
        return 0 if 200 <= status < 300 else 1
    except URLError as error:
        print(f"Không gọi được API: {error.reason}", file=sys.stderr)
        return 1
    except (OSError, ValueError, json.JSONDecodeError) as error:
        print(f"Lỗi dữ liệu: {error}", file=sys.stderr)
        return 2


if __name__ == "__main__":
    raise SystemExit(main())
