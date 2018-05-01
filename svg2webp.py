#!/usr/bin/python

import glob
import os
from os import path
from pathlib import Path
from subprocess import call

for file in glob.glob("*.svg"):
    dir_name = path.dirname(file)
    resized_png = path.join(dir_name, "temp.png")
    try:
        call(["rsvg-convert", "-a", "-f", "png", file, "-w", "2048", "-h", "2048", "-o", resized_png])
        output = Path(file).with_suffix(".webp")
        call(["cwebp", resized_png, "-lossless", "-o", output])
    finally:
        os.remove(resized_png)
