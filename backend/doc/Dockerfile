FROM node:6.12.0

# Set working directory
WORKDIR /app

# copy project file
COPY ./package.json .

# disable progress bar to increase speed of npm install
RUN npm set progress=false

# npm granular control over what you see with recursive commands such as ls
RUN npm config set depth 0

# install ALL node_modules, including 'devDependencies'
RUN npm install --no-bin-links

# copy app sources
COPY . .

# execute apidoc generation
CMD gulp apidoc
