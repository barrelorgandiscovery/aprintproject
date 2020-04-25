# APrintPerfoCommander

APrint Perfo commander is a standalone application permitting to punch files, from raspberry pi, and associated touch screens.



## Automatic update - Launch.py

Because the original SDCard is huge in size we adopt a software update handled by copying file inplace from USB drive.

The launch does the following elements :

- look of there are .jar files on usb keys at startup
- copy the jar files into the home directory
- choose the most recent jar and launch it

all update files are named with -update.jar suffix, they are found and sorted by date in reverse order, then the most recent one is taken, and copied to home directory.

