#bin/bash
git commit -a -m "New deployment"
git push
mvn clean deploy -Dmaven.test.skip=true