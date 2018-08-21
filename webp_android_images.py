import argparse
import os
from enum import Enum
from pathlib import Path

from PIL import Image


class ImageSize(Enum):
    MDPI = 1
    HDPI = 1.5
    XHDPI = 2
    XXHDPI = 3
    XXXHDPI = 4

    def folder_name(self) -> str:
        return "drawable-" + self.name.lower()

    def px(self, dp: float) -> int:
        return (dp * self.value).__round__().__int__()


def convert(name: str, dp: int):
    webp_name = Path(name).stem + ".webp"
    image = Image.open(name)
    for size in ImageSize:
        folder_name = os.path.join(os.getcwd(), size.folder_name())
        if not os.path.exists(folder_name):
            os.mkdir(folder_name)
        target = os.path.join(folder_name, webp_name)
        width = size.px(dp)
        height = int(round(width * (image.size[1] / image.size[0])))
        resized = image.resize((width, height))

        lossy = os.path.join(folder_name, "lossy_" + webp_name)
        lossless = os.path.join(folder_name, "lossless_" + webp_name)

        resized.save(fp=lossy)
        resized.save(fp=lossless, lossless=True)

        # keep the smaller
        if os.path.getsize(lossless) < os.path.getsize(lossy):
            os.remove(lossy)
            os.rename(lossless, target)
        else:
            os.remove(lossless)
            os.rename(lossy, target)


parser = argparse.ArgumentParser(description='Create android drawables')
parser.add_argument('--input', required=True, type=str, help="The image you want to convert")
parser.add_argument('--dp', required=True, type=int, help="The final dp size")
args = parser.parse_args()
if args.dp <= 0:
    parser.error("dp must be > 0")
if not os.path.exists(args.input):
    parser.error("Image {} does not exist.".format(args.input))

convert(args.input, args.dp)
