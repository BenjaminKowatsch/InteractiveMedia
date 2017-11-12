FROM ebiven/vue-cli

# Define working directory
WORKDIR /code

# Create new directory /tmp/dependencies to install node dependencies
# Will enable Docker to cache the data
RUN mkdir -p /tmp/dependencies

# Copy current package.json to /tmp/dependencies
ADD ./package.json /tmp/dependencies

# Enter directory and install node dependencies
RUN cd /tmp/dependencies && npm install

RUN cp -a /tmp/dependencies/node_modules /code/
# Copy the node_modules into the directory /code
# Build the static frontend files
CMD  npm run build
