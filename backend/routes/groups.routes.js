const express = require('express');
const router = express.Router();

const groupsController = require('../controllers/groups.controller');
const authorizationService = require('../services/authorizationService');
const authenticationService = require('../services/authenticationService');

//base route: host:8081/groups

router.get('/', authenticationService.isAuthenticated, authorizationService.isAuthorizedAdmin, groupsController.getAll);

router.get('/:groupid', groupsController.getById);

router.post('/group', authenticationService.isAuthenticated, groupsController.createNewGroup);

module.exports = router;
