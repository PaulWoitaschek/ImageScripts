import argparse

import os
from pathlib import Path
from subprocess import call


def resize(file: str):
    output = Path(file).with_suffix(".png")
    call(["rsvg-convert", "-a", "-f", "png", file, "-w", "2048", "-h", "2048", "-o", output])


parser = argparse.ArgumentParser(description='Convert SVG to PNG')
parser.add_argument('--input', required=True, type=str, help="The image you want to convert")
args = parser.parse_args()
image = os.path.abspath(args.input)
if not os.path.exists(image):
    parser.error("Image " + image + " does not exist.")
resize(image)
