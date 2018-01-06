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
COPY package.json .


#
# ---- Dependencies ----
FROM base AS dependencies

# disable progress bar to increase speed of npm install
RUN npm set progress=false

# npm granular control over what you see with recursive commands such as ls
RUN npm config set depth 0

# install only node packages required for production
RUN npm install --only=production

#ARG ORIGIN

# copy production node_modules aside
RUN cp -R node_modules prod_node_modules

# install ALL node_modules, including 'devDependencies'
RUN npm install 

#
# ---- Unit tests and linters ----
FROM dependencies AS test

# add current folder
COPY . .

#add origin adress from docker-compose file (prod or local)

RUN npm run static-code-analysis
# RUN npm run lint
# RUN npm run setup
RUN npm run unit-test

# ---- Release ----
FROM base AS release

# Set environment variables
ENV PORT 8081
ENV MONGODB_URL "mongodb://mongo/debtsquared"
#ENV ORIGIN "http://localhost:8088"
ENV GOOGLE_OAUTH_CLIENT_ID "908170470873-8151s7cvldroebdl7mcpg2houdgljgfg.apps.googleusercontent.com"
ENV FACEBOOK_URL_APP_TOKEN "1971379956470536%7ChIQ5dRDon3DJq2YzH1NU41byB-w"
ENV JWT_SIMPLE_SECRET "A3xFr93Yl2qTn5"
ENV MINIO_ENDPOINT "object-storage"
ENV MINIO_ENDPOINT_PORT "9000"
ENV MINIO_ACCESS_KEY "AKIAIOSFODNN7EXAMPLE"
ENV MINIO_SECRET_KEY "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY"
ENV MINIO_BUCKET_NAME "bucket"
ENV MINIO_OBJECT_PREFIX "public"

# copy production node_modules
COPY --from=dependencies /app/prod_node_modules ./node_modules

# copy app sources
COPY . .


# expose port
EXPOSE $PORT

# set starting point
CMD npm run start

