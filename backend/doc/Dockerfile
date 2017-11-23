#
# ---- Base Node ----
FROM node:6-alpine AS base

# install tini
RUN apk add --no-cache tini

# Set working directory
WORKDIR /app

# Set tini as entrypoint
ENTRYPOINT ["/sbin/tini", "--"]

# copy project file
COPY ./package.json .


#
# ---- Dependencies ----
FROM base AS dependencies

# disable progress bar to increase speed of npm install
RUN npm set progress=false

# npm granular control over what you see with recursive commands such as ls
RUN npm config set depth 0

# install ALL node_modules, including 'devDependencies'
RUN npm install --no-bin-links

# copy app sources
COPY . .

# set starting point
CMD gulp apidoc
