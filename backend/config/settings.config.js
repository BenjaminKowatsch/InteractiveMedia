'use strict';

// Define configuration options in separate node module
const settings = {
  'port': process.env.PORT,
  'mongodbURL': process.env.MONGODB_URL,
  'origins': process.env.ORIGINS.toString().split('::::'),
  'googleOAuthClientID': process.env.GOOGLE_OAUTH_CLIENT_ID,
  'facebookUrlAppToken': process.env.FACEBOOK_URL_APP_TOKEN,
  'jwtSimpleSecret': process.env.JWT_SIMPLE_SECRET,
  'minioAccessKey': process.env.MINIO_ACCESS_KEY,
  'minioSecretKey': process.env.MINIO_SECRET_KEY,
  'minioBucketName': process.env.MINIO_BUCKET_NAME,
  'minioObjectPrefix': process.env.MINIO_OBJECT_PREFIX,
  'minioEndpoint': process.env.MINIO_ENDPOINT,
  'minioEndpointPort': Number(process.env.MINIO_ENDPOINT_PORT),
  'logLevel': process.env.LOGLEVEL || 'info',
  'adminUsername': process.env.ADMIN_USERNAME || 'admin',
  'adminPassword': process.env.ADMIN_PASSWORD || '8C6976E5B5410415BDE908BD4DEE15DFB167A9C873FC4BB8A81F6F2AB448A918',
  'adminEmail': process.env.ADMIN_EMAIL || 'admin@example.com',
  'fcmServerKey': process.env.FCM_SERVER_KEY
};
module.exports = settings;
