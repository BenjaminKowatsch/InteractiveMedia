#
# ---- Base Node ----
FROM ebiven/vue-cli AS base-build

# Define working directory
WORKDIR /app

# copy project file
COPY package.json .


#
# ---- Unit tests and linters ----
FROM base-build AS build


ENV CLIENT_ID "908170470873-8151s7cvldroebdl7mcpg2houdgljgfg.apps.googleusercontent.com"
ENV APP_ID "1971379956470536"
ENV SPOTIFY_CLIENT_ID "1e7dcb239e2a49c4a7176cd1e21f3ea7"
ENV FACEBOOK_API_VERSION "v2.9"
#ENV ORIGIN_URL "http://localhost:8088"
ENV JWT_SIMPLE_SECRET "A3xFr93Yl2qTn5"

# disable progress bar to increase speed of npm install
RUN npm set progress=false

# npm granular control over what you see with recursive commands such as ls
RUN npm config set depth 0

# install ALL node_modules, including 'devDependencies'
RUN npm install --only=dev

# add current folder
COPY . .

#add origin_url and web_service_url adress from docker-compose file (prod or local)
ARG ORIGIN_URL
ARG WEB_SERVICE_URL

RUN echo $WEB_SERVICE_URL
RUN echo $ORIGIN_URL
# Build the static frontend files
RUN npm run build

# RUN npm run lint
# RUN npm run setup
# RUN npm run unit-test


#
# ---- Release ----
FROM nginx:stable-alpine AS release

RUN apk add --no-cache tini

# # Remove the default Nginx configuration
RUN rm -v /etc/nginx/nginx.conf
RUN rm -v /etc/nginx/conf.d/*.conf

# Copy configuration files from the current directory
ADD nginx/nginx.conf /etc/nginx/
ADD nginx/default.conf /etc/nginx/conf.d/

# Add static sources from build container
COPY --from=build /app/dist /var/www/html

# expose port
EXPOSE 80

# Set tini as entrypoint
ENTRYPOINT ["/sbin/tini", "--"]

CMD ["nginx"]
