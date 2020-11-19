import os
import platform
from xml.dom import minidom
import pathlib
from datetime import *
from winreg import *
import tkinter as tk
from tkinter import filedialog, messagebox

def main():
	# Set the path of the current script file
	path = pathlib.Path(__file__).parent.absolute()
	xmlfileName = "AutorunScript.xml"

	# choose the file from filedialog
	root = tk.Tk()
	root.withdraw()
	filez = filedialog.askopenfilename(multiple=True, filetypes=[('.ifc', '.ifc'),('.ifczip', '.ifczip'),('.zip', '.zip')], title="Select IFC files")
	ifcfiles = list(filez)

	# if no IFC file is selected the routine will not be carried out
	if len(ifcfiles) == 0:
		messagebox.showinfo("Error", "No IFC files selected - will shutdown routine")
		quit();

	# check the subfolders
	list_subfolders_with_paths = [f.path for f in os.scandir(str(path)) if f.is_dir()]

	print(list_subfolders_with_paths)

	classificationFolderPath = ""
	rulesetsFolderPath = ""

	for folder in list_subfolders_with_paths:

		if "\\classification" in folder:
			classificationFolderPath = folder
		if "\\rulesets" in folder:
			rulesetsFolderPath = folder

	print("Classification folder found = " + classificationFolderPath)
	print("Ruleset folder found = " + rulesetsFolderPath)

	rulesetFiles = []
	if rulesetsFolderPath != "":
		for file in os.listdir(rulesetsFolderPath):
			if file.endswith(".cset"):
				rulesetFiles.append(rulesetsFolderPath + "\\" + file)
	else:
		# get the ruleset files
		filez = filedialog.askopenfilename(multiple=True, filetypes=[('.cset', '.cset'),], title="Select CSET files")
		rulesetFiles = list(filez)

	# automatically choose all the files in the folder 
	# ifcfiles = Tkinter.Tk().tk.splitlist(filez) 
	# Get all IFC files in the path
	#ifcfiles = [os.path.join(root, name)
	#	for root, dirs, files in os.walk(path)
	#		for name in files
	#			if name.endswith((".ifc", ".ifczip"))]

	classificationFiles = []
	if classificationFolderPath != "":
		for file in os.listdir(classificationFolderPath):
			if file.endswith(".classification"):
				classificationFiles.append(classificationFolderPath + "\\" + file)
	else:
		# get the classification files
		filez = filedialog.askopenfilename(multiple=True, filetypes=[('.classification', '.classification'),], title="Select Classifications files")
		classificationFiles = list(filez)

	print('We found the following files: \n')

	message = "You want to proceed? \n \n"


	for file in ifcfiles:
		print(file)
		message = message + file + "\n"

	for file in rulesetFiles:
		print(file)
		message = message + file + "\n"

	for file in classificationFiles:
		print(file)
		message = message + file + "\n"

	print("\n")

	result = messagebox.askquestion("Want to proceed?", message, icon='warning', type='yesno')
	if result == 'no':
		quit()		

	# Example: <openclassification file="C:\Users\Public\Solibri\SOLIBRI\Classifications\Space Usage.classification"/>

	xmlFile = open(xmlfileName,"w") 
	 
	xmlFile.write("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?> \n") 
	xmlFile.write("<batch name=\"Simple Batch\" default=\"root\"> \n") 
	xmlFile.write("\t <target name=\"root\"> \n") 

	# ifcfiles
	for file in ifcfiles:
		xmlFile.write("\t \t <openmodel ") 
		xmlFile.write("file=\"" + file + "\"") 
		xmlFile.write(" /> \n") 
		
	# rulesetfiles
	for file in rulesetFiles:
		xmlFile.write("\t \t <openruleset ") 
		xmlFile.write("file=\"" + file + "\"") 
		xmlFile.write(" /> \n") 

	# Classifications
	for file in classificationFiles:
		xmlFile.write("\t \t <openclassification ") 
		xmlFile.write("file=\"" + file + "\"") 
		xmlFile.write(" /> \n") 

	# Execute all ruleset checks 
	xmlFile.write("\t \t <check /> \n")
	# Auto-Create issues from the checking results 
	xmlFile.write("\t \t <autocomment zoom=\"TRUE\" maxsnapshotsincategory=\"2\"/>  \n")
	# Create a presentation from the issues
	xmlFile.write("\t \t <createpresentation />  \n")

	# Coordination Report
	xmlFile.write("\t \t <coordinationreport file=\"" + str(path) + "\\" + os.path.basename(ifcfiles[0])[0:3] + "_CoordinationReport.xlsx\" />\n")

	# BCF Report
	xmlFile.write("\t \t <bcfreport file=\"" + str(path) + "\\" + os.path.basename(ifcfiles[0])[0:3] + "_BCFReport.bcfzip\" />\n")

	# Save the project SMC file
	xmlFile.write("\t \t <savemodel file=\"" + str(path) + "\\" + os.path.basename(ifcfiles[0])[0:3] + "_Project.smc\" /> \n")

	xmlFile.write("\t \t <closemodel /> \n")
	xmlFile.write("\t \t <exit /> \n")

	xmlFile.write("\t </target> \n") 
	xmlFile.write("</batch> \n") 
	           
	xmlFile.close() 

	if( platform.system() == "Windows"):
		# Find the Solibri installation path for Windows 
		try:
		    root_key=OpenKey(HKEY_LOCAL_MACHINE, r'SOFTWARE\Solibri\SMC', 0, KEY_READ)
		    [SolibriInstallationPath,regtype]=(QueryValueEx(root_key,"home"))
		    CloseKey(root_key)
		    if (""==SolibriInstallationPath):
		        raise WindowsError
		except WindowsError:
		    print("Error")
		command = "\"" + str(SolibriInstallationPath) + "\" \"" + str(path) + "\\" + str(xmlfileName) + "\"" + "-J-splash:none"
		os.system('cmd /k \"' + str(command) + '\"') 

	if( platform.system() == "OSX"):
		command = "Open Solibri.app --args " + str(path) + "\\" + str(xmlfileName) + "\"" + "-J-splash:none"

	root.destroy()
	quit()

if __name__ == '__main__':
    main()