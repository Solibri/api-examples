@echo off
call mvn -f info-examples\pom.xml install
call mvn -f info-template\pom.xml install
call mvn -f rule-examples\pom.xml install
call mvn -f rule-template\pom.xml install
call mvn -f view-examples\pom.xml install
call mvn -f view-template\pom.xml install