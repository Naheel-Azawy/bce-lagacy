#!/bin/sh
if [[ $(whoami) == root ]]; then
BCE_PATH="/opt/bce/"
DESKTOP_PATH="/usr/share/applications/"
BIN_PATH="/bin/bce"
mkdir -p $BCE_PATH
mkdir -p $DESKTOP_PATH
cp bce.jar $BCE_PATH
cp ic_512.png $BCE_PATH
echo "[Desktop Entry]
Name=Basic Computer Emulator
Comment=BCE
Exec=java -jar $BCE_PATH/bce.jar
Icon=$BCE_PATH/ic_512.png
Type=Application
Terminal=false
Categories=Development;IDE;" > "$DESKTOP_PATH/bce.desktop"
echo "#!/bin/sh
java -jar $BCE_PATH/bce.jar \$@" > $BIN_PATH
chmod +x $BIN_PATH
echo "Done!"
else
echo "Cannot install without root permissions"
fi
