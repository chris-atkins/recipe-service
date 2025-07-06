docker run \
  --name "mongodb_test" \
  --runtime "runc" \
  --log-driver "json-file" \
  --restart "" \
  --publish "0.0.0.0:27017:27017/tcp" \
  --expose "27017/tcp" \
  --detach \
  "mongo:4.0.10"