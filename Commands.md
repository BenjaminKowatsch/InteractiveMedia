# Usefule commands

build backend and run api tests
`docker-compose -f docker-compose.local.yml build && docker-compose -f docker-compose.backend-testing-api.yml rm -fsv && docker-compose -f docker-compose.backend-testing-api.yml build && docker-compose -f docker-compose.backend-testing-api.yml up --abort-on-container-exit`

build local backend with disabled cache for unit tests
`docker-compose -f docker-compose.local.yml build --build-arg CACHEBUST=$(date +%s) backend`

build and run local stack 
`docker-compose -f docker-compose.local.yml up --build`

generate api docs
`docker build -f backend/doc/Dockerfile -t apidoc:latest backend && docker run apidoc:latest && rm -r /vagrant/generated_doc  && docker cp $(docker ps --latest --format "{{.ID}}"):/doc ./generated_doc`

remove all unreachable docker layers
`docker images -q -f dangling=true | xargs docker rmi`

remove all unreachable docker volumes
`docker volume ls -qf dangling=true | xargs -r docker volume rm`

cleanup all docker resources (containers, volumes, images, networks)
`docker system prune`
