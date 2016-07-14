#Overview
This application is built using Play Framework 2.5.4. If you need instructions outside of what is in this README please visit: https://www.playframework.com/documentation/2.5.x/Home. The main purpose of this app is to cache GitHub Api responses. It additionally provides some post processing of the Netflix repos list by providing an API by which to access the repos sorted by certain metrics.

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

#Routes
Any requests to routes not defined below will be forwarded to Netflix and the response will be cached.

```
Path: /healthcheck
Method: GET
Path Variables:
Parameters:
```
```
Path: /view/top/:count/forks
Method: GET
Path Variables:
count: Integer

Parameters: 
hash: String ?= null
page: Integer ?= 1
```
```
Path: /view/top/:count/updated
Method: GET
Path Variables:
count: Integer

Parameters: 
hash: String ?= null
page: Integer ?= 1
```
```
Path: /view/top/:count/open_issues
Method: GET
Path Variables:
count: Integer

Parameters: 
hash: String ?= null
page: Integer ?= 1
```
```
Path: /view/top/:count/stars
Method: GET
Path Variables:
count: Integer

Parameters: 
hash: String ?= null
page: Integer ?= 1
```
```
Path: /view/top/:count/watchers
Method: GET
Path Variables:
count: Integer

Parameters: 
hash: String ?= null
page: Integer ?= 1
```
