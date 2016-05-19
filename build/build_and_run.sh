thisDir=`dirname $0`
PROJECT_DIR="$thisDir/.."


echo "Prepare out dir ..."
if [ ! -d "$PROJECT_DIR"/out ]; then
    mkdir "$PROJECT_DIR"/out
fi
OUT_DIR="$PROJECT_DIR"/out/icq/
if [ -d "$OUT_DIR" ]; then
    rm -rf "$OUT_DIR"
fi
mkdir "$OUT_DIR"
cp -r "$PROJECT_DIR"/libs/*.jar $OUT_DIR

echo "Find *.jar files ..."
icqFiles=`find $OUT_DIR -name *.jar`
icqLibs=""
for lib in $icqFiles
do
    lib=`basename $lib`
    if [ "$icqLibs" != "" ]; then
        icqLibs="$icqLibs:"
    fi
    icqLibs="$icqLibs$lib"
done

pushd "$OUT_DIR"
    echo "Compile ..."
    APP_SRC="../../IcqLib/src/app"
    LIB_SRC="../../IcqLib/src/lib"
    javac -d "." \
        -classpath  "$icqLibs" \
        -sourcepath "$APP_SRC:$LIB_SRC" \
        -source "1.8" -target "1.8" \
        "$APP_SRC"/ru/flashpress/icqapp/IcqApp.java

    result=$?
    echo "  compile error code: $result"
    if [ "$result" != "0" ]; then
        exit 1;
    fi

    echo "Running ..."
    java -cp  "$icqLibs:." ru.flashpress.icqapp.IcqApp $@ 
popd