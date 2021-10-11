Unicode True

;--------------------------------
;Include Modern UI

  !include "MUI2.nsh"

  !define MUI_ICON "installer-icon.ico"

;--------------------------------
;Other DLLs

  !include "FileAssoc.nsh"

;--------------------------------
;General

  ;Name and file
  Name "GDX Texture Packer"
  OutFile "output/${FILENAME}.exe"

  ;Default installation folder
  InstallDir "$PROGRAMFILES\GdxTexturePacker"
  
  ;Get installation folder from registry if available
  InstallDirRegKey HKCU "Software\GdxTexturePacker" ""

  ;Request application privileges for Windows Vista
  RequestExecutionLevel admin

;--------------------------------
;Interface Settings

  !define MUI_ABORTWARNING

;--------------------------------
;Pages

  !insertmacro MUI_PAGE_LICENSE "license.txt"
  !insertmacro MUI_PAGE_COMPONENTS
  !insertmacro MUI_PAGE_DIRECTORY
  !insertmacro MUI_PAGE_INSTFILES
  
  !insertmacro MUI_UNPAGE_CONFIRM
  !insertmacro MUI_UNPAGE_INSTFILES
  
;--------------------------------
;Languages
 
  !insertmacro MUI_LANGUAGE "English"

;--------------------------------
;Installer Sections

Section "Main application" AppInstall
SectionIn RO

  SetOutpath "$INSTDIR"

  ;Include the natives stripped JAR and rename it ot matche universal JAR name.
  File "/oname=$INSTDIR\gdx-texturepacker.jar" "output\gdx-texturepacker-windows.jar"

  File /r /x "*.sh" "files\*.*"

  ;Store installation folder
  WriteRegStr HKCU "Software\GdxTexturePacker" "" $INSTDIR
  
  ;Create uninstaller
  WriteUninstaller "$INSTDIR\Uninstall.exe"

  ;Create files association (http://nsis.sourceforge.net/FileAssoc)
  !insertmacro APP_ASSOCIATE "tpproj" "GdxTexturePacker.Project" "GDX Texture Packer project" "$INSTDIR\icon.ico,0" "Open with GDX Texture Packer" "$INSTDIR\launcher_win.bat $\"%1$\""

  ;Delete legacy symlink files
  Delete "$SMPROGRAMS\GdxTexturePacker.lnk"
  Delete "$DESKTOP\GdxTexturePacker.lnk"
  ;;Delete legacy run script files
  Delete "$INSTDIR\launcher.bat"
  Delete "$INSTDIR\launcher.sh"

SectionEnd

Section "Start Menu shortcuts" StartMenuShortcuts

    createShortCut "$SMPROGRAMS\GDX Texture Packer.lnk" "$INSTDIR\launcher_no_cmd.vbs" "" "$INSTDIR\icon.ico" 0

# default sec end
SectionEnd

Section "Desktop shortcuts" DesktopShortcuts
 
    createShortCut "$DESKTOP\GDX Texture Packer.lnk" "$INSTDIR\launcher_no_cmd.vbs" "" "$INSTDIR\icon.ico" 0
 
# default sec end
SectionEnd

;--------------------------------
;Uninstaller Section

Section "Uninstall"

  RMDir /r "$INSTDIR"
  RMDir /r "$PROFILE\.gdxtexturepackergui"

  ;Legacy link files
  Delete "$SMPROGRAMS\GdxTexturePacker.lnk"
  Delete "$DESKTOP\GdxTexturePacker.lnk"
  ;Modern link files
  Delete "$SMPROGRAMS\GDX Texture Packer.lnk"
  Delete "$DESKTOP\GDX Texture Packer.lnk"

  DeleteRegKey /ifempty HKCU "Software\GdxTexturePacker"

  ;Unregister file extension
  !insertmacro APP_UNASSOCIATE "tpproj" "GdxTexturePacker.Project"

SectionEnd

;--------------------------------
;Descriptions

  ;Language strings
  LangString DESC_AppInstall ${LANG_ENGLISH} "Installs the GDX Texture Packer application."
  LangString DESC_StartMenuShortcuts ${LANG_ENGLISH} "Places a GDX Texture Packer folder containing shortcuts in the start menu."
  LangString DESC_DesktopShortcuts ${LANG_ENGLISH} "Places a GDX Texture Packer shortcut on the desktop."

  ;Assign language strings to sections
  !insertmacro MUI_FUNCTION_DESCRIPTION_BEGIN
    !insertmacro MUI_DESCRIPTION_TEXT ${AppInstall} $(DESC_AppInstall)
    !insertmacro MUI_DESCRIPTION_TEXT ${StartMenuShortcuts} $(DESC_StartMenuShortcuts)
    !insertmacro MUI_DESCRIPTION_TEXT ${DesktopShortcuts} $(DESC_DesktopShortcuts)
  !insertmacro MUI_FUNCTION_DESCRIPTION_END