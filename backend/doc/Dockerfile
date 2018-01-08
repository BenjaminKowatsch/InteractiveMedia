FROM node:6-alpine

# copy project file
COPY ./package.json .

# disable progress bar to increase speed of npm install
RUN npm set progress=false

# install ALL node_modules, including 'devDependencies'
RUN npm install

# copy app sources
COPY . .

# execute apidoc generation
CMD ["npm", "run", "generate-apidoc"]
