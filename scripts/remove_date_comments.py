#!/usr/bin/env python3
"""Remove @date lines and date-only Javadoc blocks from source files."""
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
SCAN_DIRS = [
    ROOT / "bbs-springboot",
    ROOT / "src",
    ROOT / "tests",
    ROOT / "bbs-vue-ui" / "src",
]
EXTENSIONS = {".java", ".cs", ".vue", ".js", ".ts"}

DATE_LINE = re.compile(r"^\s*(\*\s*)?@date\b.*$|^\s*//\s*@date\b.*$", re.IGNORECASE)
DATE_ONLY_BLOCK = re.compile(
    r"/\*\*\s*\n\s*\*\s*@date[^\n]*\n\s*\*/\s*\n?",
    re.MULTILINE,
)
EMPTY_JAVADOC = re.compile(
    r"/\*\*\s*\n(?:\s*\*\s*\n)*\s*\*/\s*\n?",
    re.MULTILINE,
)
TRAILING_EMPTY_JAVADOC_LINE = re.compile(r"\n\s*\*\s*\n(\s*\*/)", re.MULTILINE)


def strip_date_lines(text: str) -> str:
    lines = text.splitlines(keepends=True)
    out = []
    for line in lines:
        stripped = line.rstrip("\r\n")
        if DATE_LINE.match(stripped):
            continue
        out.append(line)
    return "".join(out)


def collapse_empty_javadocs(text: str) -> str:
    prev = None
    while prev != text:
        prev = text
        text = DATE_ONLY_BLOCK.sub("", text)
        text = EMPTY_JAVADOC.sub("", text)
        text = TRAILING_EMPTY_JAVADOC_LINE.sub(r"\n\1", text)
    return text


def process_file(path: Path) -> bool:
    original = path.read_text(encoding="utf-8")
    updated = collapse_empty_javadocs(strip_date_lines(original))
    if updated != original:
        path.write_text(updated, encoding="utf-8", newline="\n")
        return True
    return False


def main() -> None:
    changed = []
    for base in SCAN_DIRS:
        if not base.exists():
            continue
        for path in base.rglob("*"):
            if path.suffix.lower() not in EXTENSIONS:
                continue
            if any(part in {"node_modules", "target", "dist", "bin", "obj"} for part in path.parts):
                continue
            if process_file(path):
                changed.append(path)
    print(f"Updated {len(changed)} files")
    for p in sorted(changed)[:20]:
        print(f"  {p.relative_to(ROOT)}")
    if len(changed) > 20:
        print(f"  ... and {len(changed) - 20} more")


if __name__ == "__main__":
    main()
