#!/usr/bin/env python3
import argparse
import json
import sys
from datetime import datetime, timezone
from pathlib import Path

def load_export(path: Path):
    with path.open("r", encoding="utf-8") as f:
        data = json.load(f)
    # Some exports are a list, some wrap in {"conversations": [...]}
    if isinstance(data, dict) and "conversations" in data:
        return data["conversations"]
    if isinstance(data, list):
        return data
    raise ValueError("Unrecognized conversations.json structure")

def pick_conversation(convos, title_query: str):
    # case-insensitive substring match on title
    matches = [c for c in convos if title_query.lower() in (c.get("title") or "").lower()]
    if not matches:
        return None, []
    # If multiple, sort by update/create time and take the latest
    def ts(c):
        return c.get("update_time") or c.get("create_time") or 0
    matches.sort(key=ts, reverse=True)
    return matches[0], matches

def epoch_to_str(ts):
    if not ts:
        return ""
    try:
        return datetime.fromtimestamp(ts, tz=timezone.utc).astimezone().strftime("%Y-%m-%d %H:%M:%S")
    except Exception:
        return str(ts)

def extract_text_from_message(msg):
    """
    Handles various export formats:
    - {"content": {"content_type": "text", "parts": ["..."]}}
    - {"content": {"content_type": "multimodal_text", "parts": [{"type":"text","text":"..."}]}}
    - {"content": {"content_type": "code", "text":"..."}}
    We join all text parts with double newlines.
    """
    content = msg.get("content") or {}
    parts = []

    # Newer format: list of parts (strings or dicts)
    if isinstance(content.get("parts"), list):
        for p in content["parts"]:
            if isinstance(p, str):
                parts.append(p)
            elif isinstance(p, dict):
                # multimodal parts
                if p.get("type") == "text" and "text" in p:
                    parts.append(p["text"])
                elif "text" in p:
                    parts.append(p["text"])
                elif "string" in p:
                    parts.append(p["string"])
    # Older/other formats
    if not parts:
        if "text" in content and isinstance(content["text"], str):
            parts.append(content["text"])

    # Attachments (images/files) — keep filenames/ids as placeholders
    atts = msg.get("attachments") or []
    for a in atts:
        name = a.get("name") or a.get("id") or "attachment"
        parts.append(f"[Attachment: {name}]")

    return "\n\n".join([p for p in parts if p is not None and str(p).strip()])

def flatten_conversation(conv):
    """
    Flattens the mapping tree into a chronological list of (time, role, text).
    """
    mapping = conv.get("mapping") or {}
    rows = []
    for node in mapping.values():
        msg = node.get("message")
        if not msg:
            continue
        author = (msg.get("author") or {}).get("role")
        if author not in ("user", "assistant", "tool", "system"):
            continue
        text = extract_text_from_message(msg)
        # Skip purely empty messages
        if text is None:
            text = ""
        rows.append({
            "time": msg.get("create_time") or node.get("create_time") or 0,
            "role": author,
            "text": text
        })

    # Sort by timestamp (export sometimes has None/0; keep them first but stable)
    rows.sort(key=lambda r: (0 if not r["time"] else r["time"]))
    return rows

def write_txt(conv, rows, out_path: Path):
    title = conv.get("title") or "Untitled"
    header = [
        f"# Conversation: {title}",
        f"# Created: {epoch_to_str(conv.get('create_time'))}",
        f"# Updated: {epoch_to_str(conv.get('update_time'))}",
        "",
        "----------------------------------------",
        ""
    ]
    with out_path.open("w", encoding="utf-8") as f:
        f.write("\n".join(header))
        for r in rows:
            ts = epoch_to_str(r["time"])
            role = r["role"].upper()
            text = r["text"].strip()
            f.write(f"[{ts}] {role}:\n{text}\n\n")

def main():
    ap = argparse.ArgumentParser(description="Extract a single ChatGPT conversation to plain text.")
    ap.add_argument("-i", "--input", required=True, help="Path to conversations.json")
    ap.add_argument("-t", "--title", required=True, help="Title (or substring) of the conversation, e.g., 'ANDROID EXPERT'")
    ap.add_argument("-o", "--output", default="conversation.txt", help="Output .txt path")
    args = ap.parse_args()

    try:
        convos = load_export(Path(args.input))
    except Exception as e:
        print(f"Failed to load export: {e}", file=sys.stderr)
        sys.exit(1)

    conv, matches = pick_conversation(convos, args.title)
    if not conv:
        print(f"No conversations found with title containing: {args.title!r}", file=sys.stderr)
        # Help user discover nearby titles
        titles = sorted({(c.get('title') or '').strip() for c in convos if c.get('title')})
        suggestions = [t for t in titles if args.title.lower()[:4] in t.lower()]
        if suggestions:
            print("Did you mean one of:", *suggestions, sep="\n  - ", file=sys.stderr)
        sys.exit(2)

    if len(matches) > 1:
        print("Multiple matches found. Exporting the most recent. Others were:", file=sys.stderr)
        for m in matches[1:5]:
            print(" -", m.get("title"), "@", epoch_to_str(m.get("update_time") or m.get("create_time")), file=sys.stderr)

    rows = flatten_conversation(conv)
    out_path = Path(args.output)
    write_txt(conv, rows, out_path)
    print(f"✅ Exported '{conv.get('title')}' → {out_path.resolve()} (messages: {len(rows)})")

if __name__ == "__main__":
    main()