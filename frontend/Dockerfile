#
# ---- Base Node ----
FROM ebiven/vue-cli AS base-build

# Define working directory
WORKDIR /app

# copy project file
COPY package.json .


#
# ---- Dependencies ----
FROM base-build AS dependencies

# disable progress bar to increase speed of npm install
RUN npm set progress=false

# npm granular control over what you see with recursive commands such as ls
RUN npm config set depth 0

# install ALL node_modules, including 'devDependencies'
RUN npm install --no-bin-links


#
# ---- Unit tests and linters ----
FROM dependencies AS build

# add current folder
COPY . .

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
