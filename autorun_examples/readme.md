# Autorun Python script

* This folder contains an example project how to use the Solibri Autorun function in combination with a Python script to further automate the work processes in Solibri
* This tutorial mainly is written for Windows and most likely needs some updates to work for macOS or other OS

## Prerequisites

* Having Python installed --> https://www.python.org/ (recommended version: Python 3.5)
* Having a Solibri Office or Solibri Site license as Autorun ist not available for Solibri Anywhere
* Having Solibri (Office) installed (later than 9.10.8 as it must contain the Autorun extension)
	* If Autorun is not included in your Solibri version or license it will simply raise an error message window when trying to startup with the autorun script

## Distribution

* the script can be executed directly or with the start.bat command 
* besides, pyinstaller can be used to build a standalone executable, which can then be copied in any place where autorun is required (python script embedded). For this, please execute the create_exe.bat (pip & pyinstaller required)
  * https://pypi.org/project/pip/ 
  * https://www.pyinstaller.org/

## Performance tips 
* In order to optimize the running engine your should :
	* Go to the solibri installation folder (on Windows usually C:\Program Files\Solibri\SOLIBRI) and change the first entry (usually -Xmx8192m) in the Solibri.vmoptions file to -Xmx16384m given that you have 16 GB of RAM available on your machine (see also here --> https://www.solibri.com/learn/smc-out-of-memory) 
* Turn off all additional features in the 3D view on the running instance Solibri installation (Antialiasing, ...)

