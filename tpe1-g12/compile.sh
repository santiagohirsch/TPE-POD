#!/bin/bash

target_dir="server/target"
client_dir="client/target"
temp_dir="tmp"

mvn clean package

mkdir -p "$temp_dir"

cp "$target_dir/tpe1-g12-server-1.0-SNAPSHOT-bin.tar.gz" "$temp_dir/"
cp "$client_dir/tpe1-g12-client-1.0-SNAPSHOT-bin.tar.gz" "$temp_dir/"
cd "$temp_dir"

# Server
tar -xzf "tpe1-g12-server-1.0-SNAPSHOT-bin.tar.gz"
chmod +x tpe1-g12-server-1.0-SNAPSHOT/run-server.sh
sed -i -e 's/\r$//' tpe1-g12-server-1.0-SNAPSHOT/*.sh
rm "tpe1-g12-server-1.0-SNAPSHOT-bin.tar.gz"

# Client
tar -xzf "tpe1-g12-client-1.0-SNAPSHOT-bin.tar.gz"
chmod +x tpe1-g12-client-1.0-SNAPSHOT/*-client.sh
sed -i -e 's/\r$//' tpe1-g12-client-1.0-SNAPSHOT/*.sh
rm "tpe1-g12-client-1.0-SNAPSHOT-bin.tar.gz"