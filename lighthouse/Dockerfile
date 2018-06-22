FROM alpeware/chrome-headless-trunk

ENV URL https://example.com

RUN apt-get update
RUN apt-get install -y curl
RUN curl -sL https://deb.nodesource.com/setup_8.x | sudo -E bash -
RUN apt-get install -y nodejs
RUN npm install -g lighthouse

ENTRYPOINT ["/usr/bin/dumb-init", "--"]

ADD ./start.sh /usr/bin/run.sh 
ADD ./config.js /usr/bin/config.js 

RUN chmod go+x /usr/bin/run.sh 

RUN mkdir /results

WORKDIR /results

VOLUME /results

CMD ["/usr/bin/run.sh"]
