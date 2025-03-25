Set oWS = WScript.CreateObject("WScript.Shell")
sLinkFile = oWS.ExpandEnvironmentStrings("%USERPROFILE%") & "\Desktop\CodeLense.lnk"
Set oLink = oWS.CreateShortcut(sLinkFile)
oLink.TargetPath = oWS.CurrentDirectory & "\CodeLense.bat"
oLink.WorkingDirectory = oWS.CurrentDirectory
oLink.Description = "CodeLense Java and Python Compiler"
oLink.Save
