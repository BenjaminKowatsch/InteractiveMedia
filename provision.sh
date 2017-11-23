#!/usr/bin/env bash

echo "==> start provisioning"

echo "==> install docker"
apt-get update
apt-get install -y apt-transport-https ca-certificates curl gnupg2 software-properties-common
curl -fsSL https://download.docker.com/linux/$(. /etc/os-release; echo "$ID")/gpg | sudo apt-key add -
add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/$(. /etc/os-release; echo "$ID") $(lsb_release -cs) stable"
apt-get update
apt-get install -y docker-ce

echo "==> verify docker is running"
docker run hello-world

echo "==> install docker compose"
curl -L https://github.com/docker/compose/releases/download/1.16.1/docker-compose-`uname -s`-`uname -m` -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

echo "==> verify docker compose is running"
docker-compose --version

echo "==> create mongodb data directory"
mkdir -p /data

echo "==> allow ssh PasswordAuthentication"
sed -i '$ d' /etc/ssh/sshd_config
service ssh restart
service sshd restart