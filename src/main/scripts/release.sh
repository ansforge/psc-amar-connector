#!/bin/bash -e
#
# Copyright © 2022-2024 Agence du Numérique en Santé (ANS) (https://esante.gouv.fr)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

echo Wiping previous rabbitMQ test container.
if [ $(docker ps | grep "sec-psc-rabbitmq" | wc -l) -eq 1 ]; then
    sudo docker stop "sec-psc-rabbitmq"
else
    echo "No running 'sec-psc-rabbitmq' container"
fi
if [ $(docker ps -a | grep "sec-psc-rabbitmq" | wc -l) -eq 1 ]; then
    sudo docker rm "sec-psc-rabbitmq"
elif [ $(docker ps -a | grep "sec-psc-rabbitmq" | wc -l) -ge 1 ]; then
    echo Several containers match. We don't know what we're doing >&2
    exit 2
else
    echo "No 'sec-psc-rabbitmq' container to wipe"
fi
echo Starting rabbitMQ test container.
. $(dirname $0)/launch_test_helpers.sh
sleep 10
echo Performing release
cd $(dirname $0)/../../..
rm release.properties && echo Removed stale release.properties || echo No previous release files
find . -type f -name "pom.xml.releaseBackup" -delete -print
mvn release:prepare -DautoVersionSubmodules=true -DtagNameFormat=@{version}
VERSION=$(grep scm.tag= release.properties | sed -e "s/scm.tag=//g")
echo $VERSION
cat /etc/no_way_this_exists
echo Stopping/wiping rabbitMQ test container.