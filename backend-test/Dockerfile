FROM node:6-alpine

# Adding the current directory into the '/app' folder in container
# Due to the '/app' folder Docker will cache the files downloaded by the command 'npm install'
ADD . /app
WORKDIR /app
RUN cd /app

CMD npm test
