#!/usr/bin/python

# Recursively calls avocado on all xml files in a folder

import glob
import sys
from subprocess import call

folder = sys.argv[1]
for filename in glob.iglob(folder + '**/*.xml', recursive=True):
    call(["avocado", filename])
