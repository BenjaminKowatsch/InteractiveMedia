'use strict';

const express = require('express');
const router = express.Router();

const adminController = require('../controllers/admin.controller');

router.post('/add', adminController.addAdmin);

module.exports = router;
