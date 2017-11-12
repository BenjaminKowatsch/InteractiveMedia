FROM node:6-alpine

# Set working directory
WORKDIR /app

# Set environment variables
ENV PORT 8081
ENV MONGODB_URL "mongodb://mongo/launometer"
ENV ORIGIN  "http://localhost:8080"
ENV GOOGLE_OAUTH_CLIENT_ID "908170470873-2o9m09r9cgp2n75gia4a3mbprlkli0j3.apps.googleusercontent.com"
ENV FACEBOOK_APP_TOKEN "1971379956470536|hIQ5dRDon3DJq2YzH1NU41byB-w"
ENV JWT_SIMPLE_SECRET "A3xFr93Yl2qTn5"

# Create new directory /tmp/dependencies to install node dependencies
# Will enable Docker to cache the data
RUN mkdir -p /tmp/dependencies

# Copy current package.json to /tmp/dependencies
ADD ./package.json /tmp/dependencies

# Enter directory and install node dependencies
RUN cd /tmp/dependencies &&\
    npm install
    #--no-bin-links &&\
#    npm install --only=dev

# Copy the node_modules into the directory /app
RUN cp -a /tmp/dependencies/node_modules /app/

# Copy current directory including source code
ADD . /app

# Run unit tests
#RUN npm test

# Delete dev dependencies
#RUN npm prune --production

# Start and initialize backend
CMD ["node", "index.js"]
