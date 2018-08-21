#!/usr/bin/python

# Recursively calls svgo an all svg files in a folder

import glob
import sys
from subprocess import call

folder = sys.argv[1]
for filename in glob.iglob(folder + '**/*.svg', recursive=True):
    call(["svgo", filename])
