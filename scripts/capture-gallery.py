#!/usr/bin/env python3
"""Capture the sample UI and real gestures on a single disposable Android emulator."""

import argparse
from pathlib import Path
import re
import subprocess
import time
import xml.etree.ElementTree as ET


PACKAGE = "io.github.lingmulongtai.liquidglass.sample"
WIDTH = 864
HEIGHT = 1872


def run(*args, timeout=45):
    return subprocess.check_output(list(map(str, args)), timeout=timeout)


class Gallery:
    def __init__(self, output):
        devices = run("adb", "devices").decode().splitlines()
        emulators = [line.split()[0] for line in devices if re.fullmatch(r"emulator-\d+\s+device", line)]
        if len(emulators) != 1:
            raise RuntimeError("Connect exactly one test emulator")
        self.adb_args = ["adb", "-s", emulators[0]]
        self.output = output
        self.output.mkdir(parents=True, exist_ok=True)
        if self.adb("shell", "getprop", "ro.kernel.qemu").strip() != b"1":
            raise RuntimeError("The capture device must be an emulator")

    def adb(self, *args, timeout=45):
        return run(*self.adb_args, *args, timeout=timeout)

    def tree(self):
        self.adb("shell", "uiautomator", "dump", "/sdcard/liquidglass-gallery.xml")
        return ET.fromstring(self.adb("shell", "cat", "/sdcard/liquidglass-gallery.xml"))

    def node(self, label, attribute="text"):
        for _ in range(6):
            try:
                nodes = [n for n in self.tree().iter("node") if n.get(attribute) == label]
                if nodes:
                    return max(nodes, key=lambda node: self.bounds(node)[1])
            except (ET.ParseError, subprocess.SubprocessError):
                pass
            time.sleep(.7)
        (self.output / "missing-control.xml").write_bytes(self.adb("shell", "cat", "/sdcard/liquidglass-gallery.xml"))
        self.still("missing-control")
        raise AssertionError(f"Gallery control missing: {label}")

    @staticmethod
    def bounds(node):
        return tuple(map(int, re.findall(r"\d+", node.get("bounds", ""))))

    def center(self, label, attribute="text"):
        l, t, r, b = self.bounds(self.node(label, attribute))
        return (l + r) // 2, (t + b) // 2

    def tap(self, label, attribute="text"):
        self.adb("shell", "input", "tap", *self.center(label, attribute))
        time.sleep(.45)

    def still(self, filename):
        path = self.output / (filename + ".png")
        data = self.adb("exec-out", "screencap", "-p")
        if not data.startswith(b"\x89PNG\r\n\x1a\n"):
            raise RuntimeError("Android did not return a PNG")
        path.write_bytes(data)

    def recorder(self, *args):
        result = self.adb("emu", "screenrecord", *args).decode()
        if "KO:" in result:
            raise RuntimeError(result.strip())

    def motion(self, name, gesture, crop, tail):
        raw = self.output.resolve() / f"{name}-raw.webm"
        # The host recorder avoids running the encoder inside the Android guest.
        self.recorder("start", "--size", f"{WIDTH}x{HEIGHT}", "--fps", 60,
                      "--bit-rate", "10M", "--time-limit", 12, raw)
        try:
            time.sleep(.45)
            self.adb("shell", "input", "touchscreen", *gesture)
            time.sleep(tail)
        finally:
            self.recorder("stop")
        self.export_motion(name, raw, crop)

    def export_motion(self, name, raw, crop):
        video = self.output / f"{name}.mp4"
        pattern = f"crop={crop[0]}:{crop[1]}:{crop[2]}:{crop[3]}"
        run("ffmpeg", "-v", "error", "-y", "-i", raw, "-vf", f"{pattern},fps=60",
            "-c:v", "libx264", "-crf", "17", "-pix_fmt", "yuv420p", "-movflags", "+faststart",
            video)
        scale = "scale=560:-2:flags=lanczos"
        run("ffmpeg", "-v", "error", "-y", "-i", video, "-vf", scale,
            "-c:v", "libwebp_anim", "-q:v", "85", "-loop", "0", self.output / f"{name}.webp")
        run("ffmpeg", "-v", "error", "-y", "-i", video, "-vf", scale,
            "-frames:v", 1, "-c:v", "libwebp", "-q:v", "88", self.output / f"{name}-poster.webp")

    def capture(self, apk):
        self.adb("shell", "wm", "size", f"{WIDTH}x{HEIGHT}")
        self.adb("shell", "wm", "density", "320")
        self.adb("shell", "cmd", "overlay", "enable-exclusive", "--category",
                 "com.android.internal.systemui.navbar.gestural")
        self.adb("shell", "input", "keyevent", "224")
        self.adb("shell", "wm", "dismiss-keyguard")
        self.adb("install", "-r", apk, timeout=120)
        self.adb("shell", "am", "start", "-W", "-n", f"{PACKAGE}/.MainActivity")
        self.node("Start session")
        self.still("overview")

        x, y = self.center("Start session")
        self.motion("button", ("swipe", x, y, x, y, 550),
                    (WIDTH - 48, 480, 24, max(0, y - 378) // 2 * 2), tail=.85)
        self.node("Session in progress")
        self.tap("Finish session")

        x0, y0 = self.center("Overview")
        x1, _ = self.center("Controls")
        x2, _ = self.center("Materials")
        # Release off-center so the landing is visible after the finger lifts.
        stop = round(x2 - (x2 - x1) * .22)
        self.motion("tabs", ("draganddrop", x0, y0, stop, y0, 2400),
                    (WIDTH, 338, 0, HEIGHT - 338), tail=.85)
        self.node("Container material")
        self.tap("Use light theme", "content-desc")
        self.node("Use dark theme", "content-desc")
        self.still("materials")

        self.tap("Use dark theme", "content-desc")
        self.tap("Controls")
        self.node("Lens haptics")
        self.tap("45 min")
        self.node("45 minutes")
        self.still("controls")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("apk", type=Path)
    parser.add_argument("output", type=Path)
    args = parser.parse_args()
    Gallery(args.output).capture(args.apk)


if __name__ == "__main__":
    main()
