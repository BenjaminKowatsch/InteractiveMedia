#
# ---- Base ----
FROM node:6-alpine AS base

# Set working directory
WORKDIR /app

# install tini
RUN apk add --no-cache tini

# set tini as entrypoint
ENTRYPOINT ["/sbin/tini", "--"]

# copy project file
COPY package.json .


#
# ---- Dependencies ----
FROM base AS dependencies

# disable progress bar to increase speed of npm install
RUN npm set progress=false

# install only node packages required for production
RUN npm install --only=production

# copy production node_modules aside
RUN cp -R node_modules prod_node_modules

# install ALL node_modules, including 'devDependencies'
RUN npm install 


#
# ---- Unit tests and linters ----
FROM dependencies AS test

# copy app sources
COPY . .

# run static code analysis
RUN npm run static-code-analysis

# provide ability to disable caching after this line
# reason: if you do not make any changes, docker will use its cache and hence will not run unit tests
# provide the following build argument to disable cache: --build-arg CACHEBUST=$(date +%s)
# source: https://github.com/moby/moby/issues/1996#issuecomment-185872769
ARG CACHEBUST=1

# run unit tests
RUN npm run unit-test


# ---- Release ----
FROM base AS release

# copy production node_modules from intermediary container
COPY --from=dependencies /app/prod_node_modules ./node_modules

# copy app sources
COPY . .

# set starting point
CMD npm run start
