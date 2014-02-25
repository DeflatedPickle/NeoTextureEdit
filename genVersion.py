#!/usr/bin/python
import subprocess
import sys

filenameJava = 'src/com/mystictri/neotexture/TextureVersion.java'

p = subprocess.Popen(["git", "describe", "--abbrev=8" , "--always"], stdout=subprocess.PIPE)
out, err = p.communicate()

version = out.strip()
newStringJava  = '''package com.mystictri.neotexture;
public final class TextureVersion {
    public static final String version = "0.6.5-devel-''' + version + '''";
}
''' 

oldStringJava = ''
try:
	f = open(filenameJava, 'r+')
	if (f):
		oldStringJava = f.read()
		f.close()
except IOError:
	print "Creating " + filenameJava

if (oldStringJava != newStringJava):
    print ("genVersion.py: creating " + filenameJava + " " + version)
    
    f = open(filenameJava, 'w+')
    f.write(newStringJava)
    f.close()
