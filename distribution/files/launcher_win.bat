@ECHO OFF
SET JAR_PATH="%~dp0gdx-texture-packer.jar"
java -Xms64m -Xmx2048m -jar %JAR_PATH% %*
