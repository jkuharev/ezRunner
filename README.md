# ezRunner
Graphical User Interface for wrapping command line processes

# License agreement: 'THE BEER-WARE LICENSE'
~~~
  The software application ezRunner was written by Jørg Kuharev.
  
  As long as you retain this notice you can do whatever you want
  with this application.
  
  If we meet some day & you think that ezRunner is worth it, 
  you can buy me a beer in return.
~~~
**© Jørg Kuharev, 2023**


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

## app.autoRunOnDropEvent=[ true | false ]
This allows to execute commands directly after the drop event.

## app.autoResetFileQueue=[ true | false ]
This allows to keep or clean the file queue after the execution.

## exec.command=[ executable name ]
This is either an internal command e.g. echo
or the path / name of executable file e.g. notepad.exe
Note: use CMD /C on Windows machines before internal commands!

## exec.params.userKeys=[ list of keys ]
List of parametr keys separated by ",", e.g. "user,location,mydate"
(without quotes). Every defined key will be replaced in the command line 
parameters with its value before process executing.
Values for the keys are also taken from the config file,
so please define them yourself:
e.g.
user=John
location=Office
mydate=2018-09-04

Note:
don't use predefined internal keys in the list of user keys.

## exec.params.internalKeys=file,baseName,date,time,timestamp
These are predefined internal keys. Internal keys are displayed in the config for reference only. Their values are populated at execution time,
i.e. file is the drag-and-dropped file, fileBaseName is the file name without extension;
date, time and timestamp are set to current system time. 
Key values can be accessed as `$file`, `$baseName`, `$date`, `$time`, `$timeStamp` in exec params string.

### exec.params.format.date=yyyyMMdd
format for internal key date

### exec.params.format.time=HHmmss
format for internal key time

### exec.params.format.timeStamp=yyyyMMdd-HHmmss
format for internal key timeStamp

## exec.params=[ all of the command line arguments ]
A string that will be used to build command line arguments.
The given string will be parsed before execution.
All appearances of parameter keys with preceeding "$" will
be replaced by the appropriate values, e.g. $file, $user, etc..
The replacement procedure is done from left to right following
the order of keys in **executableParameterKeys**.

## app.droppableFileFilter=[ regular expression | substring ]
This allows to filter dropped files.
Play around to find an appropriate value for your application.

## app.onRunEvent.createBatchFile=[ true | false ]
This allows to auto-generate a batch file with individual 
command lines for the series of multiple drag-and-dropped files.

## app.batchFile.namePattern=./$timestamp_ezRunner.bat
File name pattern for the batch file.

## app.onRunEvent.executeCommand=[ true | false ]
The generated command line is only really executed if this is true.
False is appropriate if we are focusing on creating batch files.

## exec.workingDir=[ . | any absolute path ]
This allows to execute the command line in a specific path.
Set it to an existing folder on your needs, or set it to "."
to automatically change working directory to drag-and-dropped file's 
parent folder.