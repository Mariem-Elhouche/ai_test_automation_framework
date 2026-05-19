"""
ci_healing_stub.py — Standalone self-healing stub for CI pipeline.
Runs a simple HTTP server that responds to /heal POST requests.
No database, no AI models — just text/attribute DOM matching.
"""

import json
import re
from html.parser import HTMLParser
from http.server import HTTPServer, BaseHTTPRequestHandler
from typing import Any, Optional


class DOMExtractor(HTMLParser):
    def __init__(self) -> None:
        super().__init__()
        self.elements: list[dict[str, Any]] = []
        self._tag: str = ""
        self._attrs: dict[str, str] = {}
        self._text: list[str] = []
        self._skip = False

    def handle_starttag(self, tag: str, attrs: list[tuple[str, str | None]]) -> None:
        if tag in ("script", "style"):
            self._skip = True
            return
        self._tag = tag
        self._attrs = {k: v for k, v in attrs if v is not None}
        self._text = []

    def handle_endtag(self, tag: str) -> None:
        if tag in ("script", "style"):
            self._skip = False
            return
        if tag == self._tag and self._tag:
            text = " ".join(t.strip() for t in self._text if t.strip())
            if text or self._attrs:
                self.elements.append({"tag": self._tag, "text": text, "attrs": dict(self._attrs)})
            self._tag = ""
            self._attrs = {}
            self._text = []

    def handle_data(self, data: str) -> None:
        if not self._skip:
            self._text.append(data)


def match_element(elements: list[dict], old_element: dict) -> Optional[dict[str, str]]:
    target_text = (old_element.get("text") or "").strip().lower()
    attrs = old_element.get("attributes") or {}
    candidates: list[tuple[float, dict[str, str]]] = []

    for el in elements:
        el_text = (el.get("text") or "").strip().lower()
        el_attrs = el.get("attrs") or {}
        score = 0.0

        if target_text and el_text == target_text:
            score = 1.0
        elif target_text and (target_text in el_text or el_text in target_text):
            score = 0.8

        for key in ("aria-label", "placeholder", "title", "name", "data-testid"):
            target_val = (attrs.get(key) or "").strip().lower()
            el_val = (el_attrs.get(key) or "").strip().lower()
            if target_val and el_val == target_val:
                score = max(score, 0.9)
            elif target_val and (target_val in el_val or el_val in target_val):
                score = max(score, 0.7)

        old_tag = (old_element.get("element_type") or "").strip().lower()
        if old_tag and el.get("tag", "").lower() == old_tag and score > 0:
            score = min(score + 0.1, 1.0)

        if score > 0:
            el_id = el_attrs.get("id")
            if el_id and not el_id.startswith("f_"):
                xpath = f"//{el['tag']}[@id='{el_id}']"
            else:
                safe = el_text.replace("'", "&apos;")
                xpath = f"//{el['tag']}[contains(text(), '{safe}')]"
            candidates.append((score, {"type": "xpath", "value": xpath}))

    if not candidates:
        return None
    candidates.sort(key=lambda x: -x[0])
    return candidates[0][1]


class HealingHandler(BaseHTTPRequestHandler):
    def do_GET(self) -> None:
        if self.path == "/health":
            self._respond(200, {"status": "ok"})
        else:
            self._respond(404, {"error": "Not found. POST to /heal"})

    def do_POST(self) -> None:
        if self.path != "/heal":
            self._respond(404, {"error": "Not found"})
            return

        length = int(self.headers.get("Content-Length", 0))
        body = self.rfile.read(length) if length else b"{}"

        try:
            req = json.loads(body)
            current_dom = req.get("current_dom")
            old_element = req.get("old_element")

            if not current_dom or not old_element:
                self._respond(200, {
                    "success": False,
                    "error": "Missing current_dom or old_element",
                    "new_locator": None,
                    "score": 0.0,
                })
                return

            parser = DOMExtractor()
            parser.feed(current_dom)
            parser.close()

            new_locator = match_element(parser.elements, old_element)

            if new_locator:
                self._respond(200, {
                    "success": True,
                    "score": 0.95,
                    "new_locator": new_locator,
                    "error": None,
                    "details": {"matching_strategy": "ci_stub"},
                })
            else:
                self._respond(200, {
                    "success": False,
                    "error": "No matching element found",
                    "new_locator": None,
                    "score": 0.0,
                })
        except Exception as e:
            self._respond(500, {"success": False, "error": str(e)})

    def _respond(self, code: int, data: dict) -> None:
        self.send_response(code)
        self.send_header("Content-Type", "application/json")
        self.send_header("ngrok-skip-browser-warning", "true")
        self.end_headers()
        self.wfile.write(json.dumps(data).encode())


if __name__ == "__main__":
    port = 8090
    server = HTTPServer(("0.0.0.0", port), HealingHandler)
    print(f"[CI Healing Stub] Listening on http://0.0.0.0:{port}")
    server.serve_forever()
