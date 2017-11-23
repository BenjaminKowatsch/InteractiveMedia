#
# ---- Base Node ----
FROM node:6-alpine AS base

# Set working directory
WORKDIR /app

# copy project file
COPY package.json .

# disable progress bar to increase speed of npm install
RUN npm set progress=false

# install all dependencies
RUN npm install

# add current folder
COPY . .

# run api test on container start
CMD ["npm", "run", "test"]
