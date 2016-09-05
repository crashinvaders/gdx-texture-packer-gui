Set oShell = CreateObject ("Wscript.Shell") 
Dim strArgs
strArgs = "cmd /c launcher.bat"
oShell.Run strArgs, 0, false