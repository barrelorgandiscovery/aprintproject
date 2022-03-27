!include "MUI.nsh"

!include "x64.nsh"
!include "logiclib.nsh"

Name "APrint Studio UNKNOWN"

Icon "aprinticon.ico"
OutFile "APrintStudioInstall.exe"

CRCCheck on
XPStyle on

BrandingText "APrint Studio - Barrel Organ Discovery"

Var APPNAME

Function .onInit
        # the plugins dir is automatically deleted when the installer exits
        
        ${IfNot} ${RunningX64} 
		   Abort "Must be installed on a 64-bit host"
		${EndIf}
        

		SetRegView 64
        
        InitPluginsDir
        
        File /oname=$PLUGINSDIR\splash.bmp "..\images\splash-aprint-studio-2020-beta.bmp"
        #optional
        File /oname=$PLUGINSDIR\splash.wav "button-19.wav"

       
        advsplash::show 1000 600 400 -1 $PLUGINSDIR\splash

        Pop $0          ; $0 has '1' if the user closed the splash screen early,
                        ; '0' if everything closed normally, and '-1' if some error occurred.
		Delete $PLUGINSDIR\splash.bmp
		
		!insertmacro MUI_LANGDLL_DISPLAY
	    !insertmacro INSTALLOPTIONS_EXTRACT "customfolder.ini"
	    !insertmacro INSTALLOPTIONS_EXTRACT "customappname.ini"
        !insertmacro INSTALLOPTIONS_WRITE "customfolder.ini" "Field 1" "State" "$DOCUMENTS\.." 
        !insertmacro INSTALLOPTIONS_WRITE "customappname.ini" "Field 1" "State" "APrint Studio" 
        StrCpy $APPNAME "APrint Studio"	
FunctionEnd

;Function LaunchLink
;  ExecShell "" "$SMPROGRAMS\APrint Studio\APrint Studio.lnk"
;FunctionEnd


; Request application privileges for Windows Vista
RequestExecutionLevel admin

; The default installation directory
InstallDir "$PROGRAMFILES64\APrint Studio"

;-----------------------------------

; MUI Settings / Icons
!define MUI_ICON "${NSISDIR}\Contrib\Graphics\Icons\orange-install-nsis.ico"
!define MUI_UNICON "${NSISDIR}\Contrib\Graphics\Icons\orange-uninstall-nsis.ico"
 
; MUI Settings / Header
!define MUI_HEADERIMAGE
!define MUI_HEADERIMAGE_RIGHT
!define MUI_HEADERIMAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Header\orange-r-nsis.bmp"
!define MUI_HEADERIMAGE_UNBITMAP "${NSISDIR}\Contrib\Graphics\Header\orange-uninstall-r-nsis.bmp"
 
; MUI Settings / Wizard
!define MUI_WELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange-nsis.bmp"
!define MUI_UNWELCOMEFINISHPAGE_BITMAP "${NSISDIR}\Contrib\Graphics\Wizard\orange-uninstall-nsis.bmp"


;--------------------------------

; Pages

!insertmacro MUI_PAGE_LICENSE "Licence.rtf"
!insertmacro MUI_PAGE_COMPONENTS

!insertmacro MUI_PAGE_DIRECTORY

Var APRINTDATAS
Page custom AskCustomFolder CheckFolder
Function AskCustomFolder
  !insertmacro MUI_HEADER_TEXT "Choix Folder des donnees" "Selectionnez le repertoire dans lequel seront sauvegard�s les fichiers"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "customfolder.ini"
FunctionEnd

Function CheckFolder
   !insertmacro INSTALLOPTIONS_READ $R0 "customfolder.ini" "Field 1" "State" 
   StrCmp $R0 "" 0 +3
      MessageBox MB_ICONEXCLAMATION|MB_OK "Veuillez indiquer le repertoire"
      Abort
   IfFileExists $R0 +4 +3
      MessageBox  MB_ICONEXCLAMATION|MB_OK "Le repertoire n'existe pas"
      Abort
   ; create folder
   CreateDirectory $R0

   ; all ok
   StrCpy $APRINTDATAS $R0

FunctionEnd

Page custom AskAppName CheckAppName
Function AskAppName
  !insertmacro MUI_HEADER_TEXT "Application Name" "Nom utilis� pour la cr�ation des raccourcis du menu demarrer"
  !insertmacro MUI_INSTALLOPTIONS_DISPLAY "customappname.ini"
FunctionEnd

Function CheckAppName
   !insertmacro INSTALLOPTIONS_READ $R0 "customappname.ini" "Field 1" "State" 
   StrCpy $APPNAME $R0
FunctionEnd

 
!insertmacro MUI_PAGE_INSTFILES

;  !define MUI_FINISHPAGE_NOAUTOCLOSE
;    !define MUI_FINISHPAGE_RUN
;    !define MUI_FINISHPAGE_RUN_NOTCHECKED
;    !define MUI_FINISHPAGE_RUN_TEXT "Start APrint Studio 2016"
;    !define MUI_FINISHPAGE_RUN_FUNCTION "LaunchLink"
    
  !insertmacro MUI_PAGE_FINISH


!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES


!insertmacro MUI_LANGUAGE "French"

;--------------------------------
ReserveFile "customfolder.ini"
ReserveFile "customappname.ini"
!insertmacro MUI_RESERVEFILE_INSTALLOPTIONS


;--------------------------------

; The stuff to install
Section "!APrint Studio" 

  ; Set output path to the installation directory.
  SetOutPath $INSTDIR
  
  ; Put file there
  File "..\build\aprint.jar"
  File /r "D:\windows\Java\jdk1.8.0_25_x64\jre"
  File "aprinticon.ico"
  
  CreateDirectory "$SMPROGRAMS\$APPNAME"
  CreateShortCut "$SMPROGRAMS\$APPNAME\$APPNAME.lnk" "$INSTDIR\jre\bin\javaw.exe" \
  '-Xmx4g -server -Dmainfolder="$APRINTDATAS" -cp aprint.jar org.barrelorgandiscovery.gui.aprintng.APrintApplicationBootStrap' "$INSTDIR\aprinticon.ico" 0 SW_SHOWMAXIMIZED
  CreateShortCut "$APPNAME.lnk" "$INSTDIR\jre\bin\javaw.exe" \
  '-Xmx4g -server -Dmainfolder="$APRINTDATAS" -cp aprint.jar org.barrelorgandiscovery.gui.aprintng.APrintApplicationBootStrap' "$INSTDIR\aprinticon.ico" 0 SW_SHOWMAXIMIZED
  CreateDirectory "$SMPROGRAMS\$APPNAME\uninstall"
   CreateShortCut "$SMPROGRAMS\$APPNAME\uninstall\Uninstall.lnk" "$INSTDIR\ap-uninst.exe" ; use defaults for parameters, icon, etc.
  ; this one will use notepad's icon, start it minimized, and give it a hotkey (of Ctrl+Shift+Q)
  WriteUninstaller "ap-uninst.exe"
  
  ; purge the instrument cache directory
  RMDir /r "$APRINTDATAS\aprintstudio\private.cache"
  
  ; remove old extensions
  Delete   "$APRINTDATAS\aprintstudio\*.extension"
  Delete   "$APRINTDATAS\aprintstudio\*.extensionlazy"
  
  
SectionEnd ; end the section


Section /o "Advanced - Source and Developper documentation" 
	SetOutPath "$INSTDIR\dev"
	File "..\build\aprint-gui-javadoc.jar"
	File "..\build\aprint-core-javadoc.jar"
	
SectionEnd ; end the section

;Section /o "Extension - Acces Web au pilotage APrint Studio"
;	SetOutPath "$DOCUMENTS\..\aprintstudio"
;	File /r "..\offlineinstall-extensions\WebServer\*.*"
	
;SectionEnd ; end the section


Section /o "Extension - Reconnaissance de disques et cartons"
    CreateDirectory "$APRINTDATAS\aprintstudio"
	SetOutPath "$APRINTDATAS\aprintstudio"
	File /r "..\offlineinstall-extensions\DiskAndBookRecognition\*.*"
	
SectionEnd ; end the section

Section /o "Extension - Percage de cartons"
    CreateDirectory "$APRINTDATAS"
	SetOutPath "$APRINTDATAS\aprintstudio"
	File /r "..\offlineinstall-extensions\Punch\*.*"
	
SectionEnd ; end the section

Section /o "Extension - Scan de cartons"
    CreateDirectory "$APRINTDATAS"
	SetOutPath "$APRINTDATAS\aprintstudio"
	File /r "..\offlineinstall-extensions\Scan\*.*"
	
SectionEnd ; end the section



Section /o "Scripts"
    CreateDirectory "$APRINTDATAS\aprintstudio\quickscripts"
	SetOutPath "$APRINTDATAS\aprintstudio\quickscripts"
	File "officialscripts\*.aprintbookgroovyscript"

SectionEnd

Section /o "Scripts sample development (learning)"
    CreateDirectory "$APRINTDATAS\aprintstudio\quickscripts"
	SetOutPath "$APRINTDATAS\aprintstudio\quickscripts"
	File "officialscriptsdev\*.aprintbookgroovyscript"

SectionEnd

;--------------------------------

; Uninstaller

Section "Uninstall"

  RMDir /r "$INSTDIR"
 
 
  Delete "$SMPROGRAMS\$APPNAME\*.*"
  RMDir /r "$SMPROGRAMS\$APPNAME"

SectionEnd




