Set oShell = CreateObject ("Wscript.Shell") 
Dim strArgs
strArgs = "cmd /c launcher.bat "
For Each arg In Wscript.Arguments
  strArgs = strArgs + arg
Next
oShell.Run strArgs, 0, false