#!/bin/sh
if [[ $(whoami) == root ]]; then
SCS_PATH="/opt/scs/"
DESKTOP_PATH="/usr/share/applications/"
BIN_PATH="/bin/scs"
mkdir -p $SCS_PATH
mkdir -p $DESKTOP_PATH
cp scs.jar $SCS_PATH
cp ic_512.png $SCS_PATH
echo "[Desktop Entry]
Name=Simple Computer Simulator
Comment=SCS
Exec=java -jar $SCS_PATH/scs.jar
Icon=$SCS_PATH/ic_512.png
Type=Application
Terminal=false
Categories=Development;IDE;" > "$DESKTOP_PATH/scs.desktop"
echo "#!/bin/sh
java -jar $SCS_PATH/scs.jar \$@" > $BIN_PATH
chmod +x $BIN_PATH
echo "Done!"
else
echo "Cannot install without root permissions"
fi
