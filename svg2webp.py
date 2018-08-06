import argparse

import os
from os import path
from pathlib import Path
from subprocess import call


def resize(file: str):
    dir_name = path.dirname(file)
    png_resized = path.join(dir_name, "temp.png")
    try:
        call(["rsvg-convert", "-a", "-f", "png", file, "-w", "2048", "-h", "2048", "-o", png_resized])
        output = Path(file).with_suffix(".webp")
        call(["cwebp", png_resized, "-lossless", "-o", output])
    finally:
        os.remove(png_resized)


parser = argparse.ArgumentParser(description='Convert SVG to WebP')
parser.add_argument('--input', required=True, type=str, help="The image you want to convert")
args = parser.parse_args()
if not os.path.exists(args.input):
    parser.error("Image does not exist.")
resize(args.input)
