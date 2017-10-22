FROM node:6-alpine

# Set working directory
WORKDIR /app

# Create new directory /tmp/dependencies to install node dependencies
# Will enable Docker to cache the data
RUN mkdir -p /tmp/dependencies

# Copy current package.json to /tmp/dependencies
ADD ./package.json /tmp/dependencies

# Enter directory and install node dependencies
RUN cd /tmp/dependencies && npm install

# Copy the node_modules into the directory /app
# Build the static frontend files
CMD cp -a /tmp/dependencies/node_modules /app/ && cd /app/ && node index.js
