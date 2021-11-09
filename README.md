# Android Image Scripts

This repository is a collection of image scripts for creating android image assets.

## Installation

* You need to have `cwebp` in your path -> [Installation](https://developers.google.com/speed/webp/download?hl=de)
* Install the pip requirements: `pip install -r requirements.txt`
* Run `./gradlew installDist`
* Add `./build/install/android_images/bin` to your path

## Usage

### Android Vector Images

```bash
android_images svg2xml --help

# This creates a my_file.xml android vector resource
android_images svg2xml my_file.svg
```

### WebP

```bash
android_images create_webp --help

# This creates image.webp files in the density bucket folders (drawable-xhdpi, drawable-xxhdpi, ...)
android_images create_webp -dp=300 image.png
```
