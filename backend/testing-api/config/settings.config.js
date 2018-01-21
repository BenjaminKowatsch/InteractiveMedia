'use strict';

const settings = {
  host: 'http://backend:8081',
  url: {
    users: {
      base: '/v1/users',
      register: '/v1/users'
    },
    test: {
      base: '/v1/test',
      authentication: '/v1/test/authentication',
      authorization: '/v1/test/authorization',
      notification: '/v1/test/notification'
    },
    admin: {
      base: '/v1/admin',
    },
    groups: {
      base: '/v1/groups',
    },
    objectstore: {
      base: '/v1/object-store',
    },
    status: {
      base: '/v1/status',
    },
    version: {
      base: '/v1/version',
    },
    base: '/v1',
    unknown: '/unknown',
  },
  facebook: {
    'urlAppToken': process.env.FACEBOOK_URL_APP_TOKEN,
    'appId': process.env.FACEBOOK_APP_ID,
  },
  version: {
    current: '0.1.0',
  },
  mongoDb: {
    connect: {
      url: 'mongodb://mongo/debtsquared',
      options: {
        bufferMaxEntries: 0,
        autoReconnect: true,
      },
    },
  },
};

module.exports = settings;
