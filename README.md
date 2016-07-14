#Overview
This application is built using Play Framework 2.5.4. If you need instructions outside of what is in this README please visit: https://www.playframework.com/documentation/2.5.x/Home.

#Installation
Execute the following from a directory where you would like the source to reside:
```
git clone https://github.com/vikpj/github-api-cache.git
activator dist
```

#Deployment
Execute the following from the project's root directory:
```
cp target/universal/netflix-1.0.zip <DEPLOYMENT_BASE_DIRECTORY>
cd <DEPLOYMENT_BASE_DIRECTORY>
unzip netflix-1.0.zip
```

#Configuration
All of the configuration for the service is located in `<DEPLOYMENT_BASE_DIRECTORY>/netflix-1.0/conf/application.conf`

##Changing the port
1. Look for the line defining the value for `http.port` and change the value to the desired port number.

##Changing the GitHub Api Token
1. Look for the line defining the value for `github.api_token` and change the value to the desired api token.
 
#Usage
To start the service execute the script as follows:
```
<DEPLOYMENT_BASE_DIRECTORY>/netflix-1.0/bin/netflix
```

To stop the service kill the process with the PID inside: `<DEPLOYMENT_BASE_DIRECTORY>/netflix-1.0/bin/netflix/RUNNING_PID`
```
kill `cat <DEPLOYMENT_BASE_DIRECTORY>/netflix-1.0/bin/netflix/RUNNING_PID`
```
