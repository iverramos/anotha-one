@echo off
rem Build and run. Run from the project root.
if not exist out mkdir out
(for /r src %%i in (*.java) do @echo "%%i") > sources.txt
javac -d out @sources.txt && java -cp out Main
