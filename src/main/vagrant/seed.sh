#!/bin/bash
wget http://archive.cloudera.com/cdh4/one-click-install/precise/amd64/cdh4-repository_1.0_all.deb &> /dev/null
dpkg --install cdh4-repository_1.0_all.deb
sudo apt-get update
sudo apt-get -y install git
sudo apt-get -y install openjdk-7-jdk
wget http://repo.scala-sbt.org/scalasbt/sbt-native-packages/org/scala-sbt/sbt/0.13.1/sbt.deb &> /dev/null
sudo dpkg --install --force-depends sbt.deb
sudo apt-get -y -f install
sudo apt-get -y install redis-server
sudo sed -i -e "s/bind 127.0.0.1/bind 0.0.0.0/" /etc/redis/redis.conf
sudo service redis-server restart
sudo apt-get -y install zookeeper-server
sudo service zookeeper-server init
sudo service zookeeper-server start

wget https://dl.dropboxusercontent.com/u/882953/zipkin-example.tar.gz &>/dev/null
tar zxvf zipkin-example.tar.gz
sudo chown -R vagrant:vagrant zipkin-example
chmod a+x ./zipkin-example/start.sh
./zipkin-example/start.sh

