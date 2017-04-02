ASE="/Applications/Aseprite.app/Contents/MacOS/aseprite"
ASE_FILE="../resources/icon.ase"
IMG_DIR="macIcons.iconset"

function renderIcon() {
	NAME="${IMG_DIR}/${1}.png"
	SIZE=$2
	let SCALE=${SIZE}/16
	$ASE -b $ASE_FILE --scale $SCALE --save-as $NAME
}

mkdir $IMG_DIR
renderIcon "icon_512x512@2x" 1024
renderIcon "icon_512x512" 512
renderIcon "icon_256x256@2x" 512
renderIcon "icon_256x256" 256
renderIcon "icon_128x128@2x" 256
renderIcon "icon_128x128" 128
renderIcon "icon_32x32@2x" 64
renderIcon "icon_32x32" 32
renderIcon "icon_16x16@2x" 32
renderIcon "icon_16x16" 16
iconutil -c icns -o "macApp/Resources/icons.icns" $IMG_DIR
rm -r $IMG_DIR