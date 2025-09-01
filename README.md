# Android Image Scripts

This repository is a collection of image scripts for creating android image assets.

## Android Vector Drawables

### Requirements:

Install svgo :
`npm install -g svgo`

```bash
./svg2xml.sh --help

# This creates a my_file.xml android vector resource
./svg2xml.sh my_file.svg
```

## WebP

### Requirements:

* You need to have `cwebp` in your path -> [Installation cwebp](https://developers.google.com/speed/webp/download?hl=de)

```bash
./webp.sh --help

# This creates image.webp files in the density bucket folders (drawable-xhdpi, drawable-xxhdpi, ...)
./webp.sh -dp=300 image.png
```
