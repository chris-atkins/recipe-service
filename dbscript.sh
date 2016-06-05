#!/bin/sh
docker exec -it mongodb mongo admin
db.createUser({ user: 'chris-recipe-app', pwd: 'lIN1ntlknt2O3', roles: ["root"]});
exit
