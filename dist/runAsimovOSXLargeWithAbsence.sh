#bin/bash

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"
SOURCE="$(readlink "$SOURCE")"
[[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"


ps -ef | grep mongod | grep -v grep
if ([ $?  -eq "0" ]); then
echo "Detected running mongoDB instance will use this for persitence"
else
echo "Did not detect mongoDB instance will download and install it before run of ASIMOV"
if [ ! -f $DIR/mongodb/bin/mongod ]; then
if [ ! -f mongodb-osx-x86_64-2.6.7.tgz ]; then
curl -O http://downloads.mongodb.org/osx/mongodb-osx-x86_64-2.6.7.tgz
fi
if [ ! -f mongodb-osx-x86_64-2.6.7.tgz ]; then
    echo "Failed to download mongo please install and start manualy"
    exit 1;
else
tar -zxvf mongodb-osx-x86_64-2.6.7.tgz
mkdir -p mongodb
cp -R -n mongodb-osx-x86_64-2.6.7/ mongodb
rm mongodb-osx-x86_64-2.6.7.tgz
rm -Rf mongodb-osx-x86_64-2.6.7
mkdir -p $DIR/data/db
echo "Installed mongodb starting it now"
fi
else
echo "Detected mongodb installation starting it now"
fi
$DIR/mongodb/bin/mongod --dbpath $DIR/data/db &
fi
echo "Starting ASIMOV, when finished the results will be displayed in your web browser."
if (java -XX:PermSize=512m -XX:MaxPermSize=2048m -Xms256m -Xmx2048m -jar ASIMOV.jar usecase_large_with_absence.xml 7 example_output_absence); then
    open -n "$DIR/example_output/gui/html/index.html"
else
echo "An error occured while running ASIMOV."
exit 1;
fi
