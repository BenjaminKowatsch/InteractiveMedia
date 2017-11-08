FROM ebiven/vue-cli

# Install nginx
RUN apt-get update
RUN apt-get install -y nginx
RUN service nginx stop

# Define working directory
WORKDIR /code

# Create new directory /tmp/dependencies to install node dependencies
# Will enable Docker to cache the data
RUN mkdir -p /tmp/dependencies

# Copy current package.json to /tmp/dependencies
ADD ./package.json /tmp/dependencies

# Enter directory and install node dependencies
RUN cd /tmp/dependencies && npm install --no-bin-links

# Copy the node_modules into the directory /code
RUN cp -a /tmp/dependencies/node_modules /code/

# Add current directory into container
ADD . /code

# Build the static frontend files
RUN cd /code/ && npm run build

# Copy the generated static files into nginx document root
RUN cp -a /code/dist/. /var/www/html/

# Remove the default Nginx configuration file
RUN rm -v /etc/nginx/nginx.conf

# Copy a configuration file from the current directory
ADD nginx.conf /etc/nginx/

# Expos port
EXPOSE 80

CMD  nginx
