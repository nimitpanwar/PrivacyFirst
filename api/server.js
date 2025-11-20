require('dotenv').config();
const express = require('express');
const mongoose = require('mongoose');
const cors = require('cors');
const os = require('os');
const { exec, execFile } = require('child_process');

const authRoutes = require('./routes/auth');
const whitelistRoutes = require('./routes/whitelist');
const authMiddleware = require('./middleware/authMiddleware');

const app = express();
app.use(cors({ origin: process.env.CORS_ORIGIN || true, credentials: true }));
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

app.use('/api', authRoutes);
app.use('/api/whitelist', whitelistRoutes);

// Public landing page (no auth) so visiting the IP root shows content
app.get('/', (req, res) => {
  res.send(`<!doctype html><html><head><meta charset="utf-8"><title>API</title></head><body><h2>API Server</h2><p>Endpoints:</p><ul><li><a href="/api/login">/api/login</a> (login page)</li><li><a href="/api/whitelist">/api/whitelist</a></li></ul></body></html>`);
});

const PORT = parseInt(process.env.PORT, 10) || 5001;
mongoose.set('strictQuery', false);

async function start() {
  try {
    await mongoose.connect(process.env.MONGO_URI, { });
    console.log('MongoDB connected');
    // Simple request logger to help debugging
    app.use((req, res, next) => {
      console.log(`${new Date().toISOString()} ${req.method} ${req.url}`);
      next();
    });

    // Attempt to listen on PORT, if EADDRINUSE try the next ports up to +10
    const maxRetries = 10;
    let attempt = 0;

    function tryListen(port) {
      const server = app.listen(port, '0.0.0.0', () => {
        const ifaces = os.networkInterfaces();
        const addresses = [];
        for (const name of Object.keys(ifaces)) {
          for (const iface of ifaces[name]) {
            if (iface.family === 'IPv4' && !iface.internal) addresses.push(iface.address);
          }
        }

        console.log('Server running on:');
        console.log(`- http://localhost:${port}`);
        addresses.forEach(a => console.log(`- http://${a}:${port}`));
        console.log('Bound to 0.0.0.0 so the server is reachable from the network (if firewall/hosting allows).');

        // Try to open the server root in Chrome (or default browser) for convenience.
        const targetHost = addresses[0] || 'localhost';
        const targetUrl = `http://${targetHost}:${port}/`;

        function launchBrowser(binaryPath, args = []) {
          try {
            execFile(binaryPath, [...args, targetUrl], (err) => {
              if (err) console.error(`Failed to open browser (${binaryPath})`, err);
            });
          } catch (err) {
            console.error('Error launching browser', err);
          }
        }

        exec('command -v google-chrome || command -v google-chrome-stable || command -v chromium-browser || command -v chromium', (err, stdout) => {
          const bin = stdout && stdout.toString().trim();
          if (bin) {
            console.log(`Opening browser: ${bin} ${targetUrl}`);
            launchBrowser(bin);
          } else {
            exec(`xdg-open ${targetUrl}`, (openErr) => {
              if (openErr) console.error('Failed to open URL with xdg-open', openErr);
              else console.log('Opened default browser via xdg-open');
            });
          }
        });
      });

      server.on('error', (err) => {
        if (err.code === 'EADDRINUSE') {
          console.warn(`Port ${port} in use, trying ${port + 1}`);
          attempt += 1;
          if (attempt <= maxRetries) {
            tryListen(port + 1);
          } else {
            console.error('No available ports found, giving up.');
            process.exit(1);
          }
        } else {
          console.error('Server error', err);
          process.exit(1);
        }
      });
    }

    tryListen(PORT);
  } catch (err) {
    console.error('Failed to connect to MongoDB', err);
    process.exit(1);
  }
}

start();
