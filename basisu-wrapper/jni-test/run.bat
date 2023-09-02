@echo off
call mkdir build
call cd build
call cmake .. -G "MinGW Makefiles"
call cmake --build .
call cd ..
call .\build\basisu-wrapper-test.exe