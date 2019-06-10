#
# Patrice Freydiere - Barrel Organ Discovery 2019
# update launch - software
#

from os import listdir
from os.path import isfile,join,exists
import subprocess
import os
import sys
import shutil

def chooseOneJar():
    files = [f for f in listdir(".") if (isfile(f) and f[-4:]=='.jar')]
    files = sorted(files, reverse = True)
    print "files to evaluate :"
    print files
    if len(files) > 0:
        return files[0]

    return None

def copyUpdated():
    source_dir = "/media"
    for root, dirs, files in os.walk(source_dir):
        for filename in files:
            if filename[-4:] != '.jar':
                continue
            source_path = os.path.join(root, filename)
            destination_path = os.path.join('.', filename)
            if not exists(destination_path):
                print('Copying {} to {}'.format(source_path, destination_path))
                shutil.copy(source_path, destination_path)


copyUpdated()
choosenJar = chooseOneJar()
print "launch " + choosenJar
subprocess.call(["java", "-jar" , choosenJar])


