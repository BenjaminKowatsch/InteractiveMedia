#
# ---- Base ----
FROM node:6-alpine AS base

# install git
# git is temporally required to clone a repo instead of an npm package for chai-http
# officical npm release of chai-http is expected in december 2017
# https://github.com/visionmedia/superagent/issues/1069#issuecomment-346325114
RUN apk --update add git openssh && rm -rf /var/lib/apt/lists/* && rm /var/cache/apk/*

# Set working directory
WORKDIR /app

# copy project file
COPY package.json .

# disable progress bar to increase speed of npm install
RUN npm set progress=false

# install all dependencies
RUN npm install 

# copy app sources
COPY . .

# run api test on container start
CMD ["npm", "run", "test"]
