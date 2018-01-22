#
# ---- Build ----
FROM ebiven/vue-cli AS build

# define working directory
WORKDIR /app

# copy project file
COPY package.json .

# disable progress bar to increase speed of npm install
RUN npm set progress=false

# install only 'devDependencies'
RUN npm install --only=dev

# import build arguments as env vars during build
ARG WEB_SERVICE_URL

# copy app sources
COPY . .

# build the static frontend files
RUN npm run build


#
# ---- Release ----
FROM nginx:stable-alpine AS release

# install tini
RUN apk add --no-cache tini

# remove the default nginx configuration
RUN rm -v /etc/nginx/nginx.conf
RUN rm -v /etc/nginx/conf.d/*.conf

# copy configuration files from the current directory
COPY nginx/nginx.conf /etc/nginx/
COPY nginx/default.conf /etc/nginx/conf.d/

# add static sources from build container
COPY --from=build /app/dist /var/www/html

# set tini as entrypoint
ENTRYPOINT ["/sbin/tini", "--"]

# start nginx on container start
CMD ["nginx"]
