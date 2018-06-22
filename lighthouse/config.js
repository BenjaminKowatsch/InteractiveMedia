/*
module.exports = {
  extends: 'lighthouse:default',
  settings: {
    onlyAudits: [
      'first-meaningful-paint',
      'speed-index-metric',
      'estimated-input-latency',
      'first-interactive',
      'consistently-interactive',
    ],
  },
};
*/
/*
module.exports = {
  extends: 'lighthouse:default',
  settings: {
    throttlingMethod: 'devtools',
    onlyCategories: ['performance'],
  },
};
 */

module.exports = {
    extends: 'lighthouse:default',
    settings: {
        skipAudits: ['is-on-https']
    }
};