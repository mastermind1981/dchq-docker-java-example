#!/bin/bash

echo database_driverClassName=$database_driverClassName >> /opt/ibm/wlp/usr/servers/defaultServer/server.env
echo database_url=$database_url >> /opt/ibm/wlp/usr/servers/defaultServer/server.env
echo database_username=$database_username >> /opt/ibm/wlp/usr/servers/defaultServer/server.env
echo database_password=$database_password >> /opt/ibm/wlp/usr/servers/defaultServer/server.env
