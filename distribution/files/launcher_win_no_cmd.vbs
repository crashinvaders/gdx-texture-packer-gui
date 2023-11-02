scriptDir = CreateObject("Scripting.FileSystemObject").GetParentFolderName(WScript.ScriptFullName)
strArgs = "javaw -Xms64m -Xmx2048m -jar """ + scriptDir + "/gdx-texture-packer.jar"" "
For Each arg In Wscript.Arguments
  strArgs = strArgs + arg
Next
CreateObject("Wscript.Shell").Run strArgs, 0, false
