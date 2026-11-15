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

    def motion(self, name, gesture, crop, seconds):
        remote = f"/sdcard/liquidglass-{name}.mp4"
        raw = self.output / f"{name}-raw.mp4"
        recording = subprocess.Popen(
            self.adb_args + ["shell", "screenrecord", "--size", f"{WIDTH}x{HEIGHT}", "--time-limit", str(seconds + 2), remote]
        )
        try:
            time.sleep(.7)
            self.adb("shell", gesture)
            if recording.wait(timeout=20) != 0:
                raise RuntimeError(f"Could not record {name}")
        finally:
            if recording.poll() is None:
                recording.terminate()
                recording.wait(timeout=5)
        self.adb("pull", remote, raw)
        pattern = f"crop={crop[0]}:{crop[1]}:{crop[2]}:{crop[3]},scale=560:-2:flags=lanczos"
        run("ffmpeg", "-v", "error", "-y", "-i", raw, "-t", seconds, "-vf", pattern,
            "-c:v", "libx264", "-pix_fmt", "yuv420p", "-movflags", "+faststart", self.output / f"{name}.mp4")
        run("ffmpeg", "-v", "error", "-y", "-i", raw, "-t", seconds, "-filter_complex",
            f"fps=16,{pattern},split[a][b];[a]palettegen[p];[b][p]paletteuse=dither=sierra2_4a",
            "-loop", "0", self.output / f"{name}.gif")
        raw.unlink()

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
        self.motion("button", f"input touchscreen swipe {x} {y} {x} {y} 550",
                    (WIDTH - 48, 480, 24, max(0, y - 378) // 2 * 2), seconds=4)
        self.node("Session in progress")
        self.tap("Finish session")

        x0, y0 = self.center("Overview")
        x1, _ = self.center("Controls")
        x2, _ = self.center("Materials")
        # Release off-center so the landing is visible after the finger lifts.
        stop = round(x2 - (x2 - x1) * .22)
        points = [round(x0 + (stop - x0) * step / 7) for step in range(1, 8)]
        gesture = [f"input touchscreen motionevent DOWN {x0} {y0}", "sleep .4"]
        for x in points:
            gesture.extend([f"input touchscreen motionevent MOVE {x} {y0}", "sleep .06"])
        gesture.extend(["sleep .2", f"input touchscreen motionevent UP {stop} {y0}"])
        self.motion("tabs", "; ".join(gesture), (WIDTH, 338, 0, HEIGHT - 338), seconds=9)
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
