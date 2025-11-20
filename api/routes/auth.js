require('dotenv').config();
const express = require('express');
const jwt = require('jsonwebtoken');

const router = express.Router();

console.log('routes/auth loaded');


// Serve a tiny HTML login page on GET so visiting in a browser shows a form
router.get('/login', (req, res) => {
  // Serve a simple non-JS form so CSP or blocked inline scripts won't prevent submission
  res.send(`<!doctype html>
<html lang="en">
<head>
  <meta charset="utf-8">
  <meta name="viewport" content="width=device-width,initial-scale=1">
  <title>Login</title>
  <style>body{font-family:Arial,Helvetica,sans-serif;padding:24px}label{display:block;margin:8px 0 4px}input{padding:8px;width:280px}button{margin-top:12px;padding:8px 12px}</style>
</head>
<body>
  <h2>Login</h2>
  <form method="POST" action="/api/login">
    <label for="username">Username</label>
    <input id="username" name="username" required />
    <label for="password">Password</label>
    <input id="password" name="password" type="password" required />
    <button type="submit">Sign In</button>
  </form>
  <p style="margin-top:12px;color:#555">After submit you'll see the JSON response (token) in the browser.</p>
</body>
</html>`);
});

router.post('/login', (req, res) => {
  const { username, password } = req.body || {};

  if (!username || !password) {
    return res.status(400).json({ message: 'username and password required' });
  }

  if (
    username !== process.env.ADMIN_USERNAME ||
    password !== process.env.ADMIN_PASSWORD
  ) {
    return res.status(401).json({ message: 'Invalid credentials' });
  }

  const payload = { role: 'admin', username };
  const token = jwt.sign(payload, process.env.JWT_SECRET, {
    expiresIn: process.env.JWT_EXPIRES_IN || '1h'
  });

  return res.json({ token, expiresIn: process.env.JWT_EXPIRES_IN || '1h' });
});

module.exports = router;
