@Echo Off

Set TempFile=Java_FileList.txt
Set ResultFile=ResultFile.txt
If Exist %TempFile% Del %TempFile%
If Exist %ResultFile% Del %ResultFile%
Rem Dir *.java /b>%TempFile%
latest -r -t *.java>%TempFile%
For /F "tokens=*" %%I in (%TempFile%) Do Call :FindStr %%I
If Exist %TempFile% Del %TempFile%
If Exist %ResultFile% Type %ResultFile%
Goto :EOF

:FindStr
Rem Echo Grep -n "int y = mHeight - 1; y > 0; y--" %1>>%ResultFile%
Rem Grep -n "int y = mHeight - 1; y > 0; y--" %1>>%ResultFile%
Rem Echo Grep -n "int y = mHeight - 1; y >= 0; y--" %1>>%ResultFile%
Rem Grep -n "int y = mHeight - 1; y >= 0; y--" %1>>%ResultFile%

Echo Grep -n "StatusReturned" %1>>%ResultFile%
Grep -n "StatusReturned" %1>>%ResultFile%

Goto :EOF
