# Getting Started

### How to create mongodb with docker

```
sudo docker run -d -e MONGO_INITDB_ROOT_USERNAME=root -e MONGO_INITDB_ROOT_PASSWORD=password -p 27017:27017 mongo
```

### Create db inside docker mongo container
```
sudo docker exec -i -t containerId /bin/bash
mongo -u root -p
use tempdb
```

### How to run

- From project root run following command
- `./gradlew bootRun`

### CURL command to get all customer

```
curl -X GET \
  http://localhost:8080/customers \
  -H 'Accept: */*' \
  -H 'Cache-Control: no-cache' \
  -H 'Host: localhost:8080' \
  -H 'cache-control: no-cache'
```

### CURL command to add new customer

```
curl -X POST \
  http://localhost:8080/customers \
  -H 'Accept: */*' \
  -H 'Cache-Control: no-cache' \
  -H 'Content-Type: application/json' \
  -H 'Host: localhost:8080' \
  -d '{
	"firstName" : "Mad",
	"lastName" : "Max"
}'
```