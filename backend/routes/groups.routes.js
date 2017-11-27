const express = require('express');
const router = express.Router();

const groupsController = require('../controllers/groups.controller');

//base route: host:8081/groups

router.get('/', groupsController.getAll);
router.get('/:groupid', groupsController.getById);

module.exports = router;
