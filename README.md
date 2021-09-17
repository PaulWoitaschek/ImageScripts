# Android Image Scripts

This repository is a collection of image scripts for creating android image assets.

## Installation

* You need to have `cwebp` in your path -> [Installation](https://developers.google.com/speed/webp/download?hl=de)
* Install the pip requirements: `pip install -r requirements.txt`

## Usage

### Vector Images

```bash
./svg2xml.sh --help

# This creates a my_file.xml android vector resource
./svg2xml.sh my_file.svg
```

### WebP

```bash
./create_webp.sh --help

# This creates image.webp files in the density bucket folders (drawable-xhdpi, drawable-xxhdpi, ...)
./create_webp.sh -dp=300 image.png
```
