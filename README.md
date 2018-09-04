# ezRunner
Graphical User Interface for wrapping command line processes

# License agreement: 'THE BEER-WARE LICENSE'
  As long as you retain this notice you can do
  whatever you want with this application.
  If we meet some day & you think that ezRunner
  is worth it, you can buy me a beer in return.
  (c) Joerg Kuharev, 2018

# Quick start
ezRunner will create a config file during its first execution
which will allow to control the behavior of the application.
If you change some values in the config file, you can force
ezRunner to reread the config by clicking "RESET".
Once the config was modified and reread, you can drag-and-drop
files from your file system browser over the ezRunner.
The number of files to drag-and-drop is not limited,
and will accumulate until next reset.
ezRunner will execute the predefined and parsed command line
for every file in the queue separately when clicking "RUN".

# Settings
## application.autoRunOnDropEvent=[ true | false ]
This allows to execute commands directly after the drop event.

## application.resetFilesAfterExecution=[ true | false ]
This allows to keep or clean the file queue after the execution.

## executableCommand=[ executable name ]
This is either an internal command e.g. echo
or the path / name of executable file e.g. notepad.exe
Note: use CMD /C on Windows machines before internal commands!

## executableParameterKeys=[ list of keys ]
List of parametr keys separated by ",", e.g. "file,user,location,date"
(without quotes). Every defined key will be replaced in the command line 
parameters with its value before process executing.
Values for the keys are also taken from the config file,
so please define them yourself:
e.g.
user=John
location=Office
date=2018-09-04

Note:
The key "file" will be replaced automatically by the dropped files
independently of the its definition in the config file.

## executableParameters=[ all of the command line arguments ]
A string that will be used to build command line arguments.
The given string will be parsed before execution.
All appearances of parameter keys with preceeding "$" will
be replaced by the appropriate values, e.g. $file, $user, etc..
The replacement procedure is done from left to right following
the order of keys in **executableParameterKeys**.

## fileAcceptanceFilter=[ regular expression | substring ]
This allows to filter dropped files.
Play around to find an appropriate value for your application.

## fileParameterKey=file
This allows to use a different key than "file" in case
you want to use "file" for something else.
